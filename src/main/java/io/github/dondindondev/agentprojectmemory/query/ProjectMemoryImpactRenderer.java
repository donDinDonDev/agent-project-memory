package io.github.dondindondev.agentprojectmemory.query;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.dondindondev.agentprojectmemory.OutputRedactor;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

public final class ProjectMemoryImpactRenderer {
  private static final int MAX_CHANGED_FILES = 256;
  private static final int MAX_DIRECT_MATCHES = 500;
  private static final int MAX_DIAGNOSTICS = 256;
  private static final int MAX_TEXT_CHARS = 4096;
  private static final Set<String> RAW_DIFF_PREFIXES = Set.of(
      "diff --git ",
      "index ",
      "--- ",
      "+++ ",
      "@@ ");

  public ImpactInput normalizeChangedFiles(List<String> rawChangedFiles)
      throws ImpactInputException {
    if (rawChangedFiles == null || rawChangedFiles.isEmpty()) {
      throw new ImpactInputException("Missing changed-file value.");
    }

    Set<String> seen = new HashSet<>();
    TreeSet<String> unique = new TreeSet<>();
    List<DiagnosticRow> diagnostics = new ArrayList<>();
    for (String rawChangedFile : rawChangedFiles) {
      String normalized = normalizeChangedFile(rawChangedFile)
          .orElseThrow(() -> new ImpactInputException("Invalid changed-file path."));
      if (!seen.add(normalized)) {
        diagnostics.add(DiagnosticRow.duplicate(normalized));
        continue;
      }
      unique.add(normalized);
    }

    List<String> changedFiles = new ArrayList<>();
    int index = 0;
    for (String changedFile : unique) {
      if (index >= MAX_CHANGED_FILES) {
        diagnostics.add(new DiagnosticRow(
            "input_cap_reached",
            changedFile,
            "Only the first " + MAX_CHANGED_FILES + " changed-file inputs were processed."));
        break;
      }
      changedFiles.add(changedFile);
      index++;
    }
    if (changedFiles.isEmpty()) {
      throw new ImpactInputException("Missing changed-file value.");
    }
    return new ImpactInput(List.copyOf(changedFiles), boundedDiagnostics(diagnostics));
  }

  public String render(ProjectMemoryArtifacts artifacts, ImpactInput input)
      throws QueryArtifactException {
    Objects.requireNonNull(artifacts, "artifacts");
    Objects.requireNonNull(input, "input");
    ImpactIndex index = buildIndex(artifacts);
    List<DiagnosticRow> diagnostics = new ArrayList<>(input.diagnostics());
    List<DirectMatchRow> directMatches = new ArrayList<>();
    List<NotRepresentedRow> notRepresented = new ArrayList<>();

    for (String changedFile : input.changedFiles()) {
      List<DirectMatchRow> matches = index.matchesByPath().getOrDefault(changedFile, List.of());
      if (matches.isEmpty()) {
        notRepresented.add(new NotRepresentedRow(changedFile));
      } else {
        directMatches.addAll(matches);
      }
    }

    directMatches = directMatches.stream()
        .distinct()
        .sorted(DirectMatchRow.ORDERING)
        .toList();
    if (directMatches.size() > MAX_DIRECT_MATCHES) {
      diagnostics.add(new DiagnosticRow(
          "direct_match_cap_reached",
          null,
          "Only the first " + MAX_DIRECT_MATCHES + " direct matches were rendered."));
      directMatches = directMatches.subList(0, MAX_DIRECT_MATCHES);
    }
    diagnostics = boundedDiagnostics(diagnostics);

    List<String> lines = new ArrayList<>();
    appendHeader(lines, artifacts, input.changedFiles().size(), directMatches.size(),
        notRepresented.size(), diagnostics.size());
    appendDirectMatches(lines, directMatches);
    appendNotRepresented(lines, notRepresented);
    appendDiagnostics(lines, diagnostics);
    appendBoundaries(lines);
    return String.join("\n", lines) + "\n";
  }

  private ImpactIndex buildIndex(ProjectMemoryArtifacts artifacts) throws QueryArtifactException {
    Map<String, List<JsonNode>> evidenceByPath = new HashMap<>();
    for (JsonNode evidence : artifacts.evidenceRecords()) {
      String evidenceId = requiredText(evidence, "id", "Malformed evidence-index.jsonl.");
      String evidencePath = requiredText(evidence, "path", "Malformed evidence-index.jsonl.");
      if (normalizeChangedFile(evidencePath).isEmpty()) {
        throw new QueryArtifactException("Malformed evidence-index.jsonl path.");
      }
      evidenceByPath.computeIfAbsent(evidencePath, ignored -> new ArrayList<>()).add(evidence);
      if (!artifacts.evidenceById().containsKey(evidenceId)) {
        throw new QueryArtifactException("Malformed evidence-index.jsonl.");
      }
    }

    ProjectMapIndex projectMapIndex = collectProjectMapIndex(artifacts.projectMap(), artifacts);
    Map<String, LinkedHashSet<DirectMatchRow>> matches = new HashMap<>();
    for (Map.Entry<String, List<JsonNode>> entry : evidenceByPath.entrySet()) {
      for (JsonNode evidence : entry.getValue()) {
        addMatch(matches, entry.getKey(), DirectMatchRow.evidence(entry.getKey(), evidence));
      }
    }
    for (Map.Entry<String, List<FactMatch>> entry : projectMapIndex.factsByPath().entrySet()) {
      for (FactMatch fact : entry.getValue()) {
        addMatch(matches, entry.getKey(), DirectMatchRow.fact(entry.getKey(), fact));
      }
    }
    collectGraphNodeMatches(artifacts, projectMapIndex, matches);
    return new ImpactIndex(freeze(matches));
  }

  private ProjectMapIndex collectProjectMapIndex(
      JsonNode projectMap,
      ProjectMemoryArtifacts artifacts) throws QueryArtifactException {
    Map<String, List<FactMatch>> factsByPath = new HashMap<>();
    Map<String, Set<String>> factPathsById = new HashMap<>();
    Set<String> factIds = new HashSet<>();
    collectProjectMapFacts(
        projectMap,
        "",
        artifacts,
        factsByPath,
        factPathsById,
        factIds);
    return new ProjectMapIndex(factsByPath, factPathsById);
  }

  private void collectProjectMapFacts(
      JsonNode node,
      String pointer,
      ProjectMemoryArtifacts artifacts,
      Map<String, List<FactMatch>> factsByPath,
      Map<String, Set<String>> factPathsById,
      Set<String> factIds) throws QueryArtifactException {
    if (node == null) {
      return;
    }
    if (node.isObject()) {
      Optional<String> factId = factId(pointer, node);
      if (factId.isPresent()) {
        String id = factId.orElseThrow();
        if (!factIds.add(id)) {
          throw new QueryArtifactException("Duplicate generated fact id in project-map.json.");
        }
        List<String> evidenceIds = collectEvidenceIds(node, artifacts);
        Set<String> sourcePaths = new TreeSet<>();
        for (String evidenceId : evidenceIds) {
          JsonNode evidence = artifacts.evidenceById().get(evidenceId);
          String evidencePath = requiredText(evidence, "path", "Malformed evidence-index.jsonl.");
          sourcePaths.add(evidencePath);
          addFactMatch(
              factsByPath,
              evidencePath,
              new FactMatch(
                  id,
                  pointer,
                  "fact_evidence_path",
                  "evidence_ids",
                  null,
                  evidenceIds));
        }
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
          Map.Entry<String, JsonNode> field = fields.next();
          for (String sourcePath : sourcePathValues(field.getKey(), field.getValue())) {
            sourcePaths.add(sourcePath);
            addFactMatch(
                factsByPath,
                sourcePath,
                new FactMatch(
                    id,
                    pointer,
                    "source_reference_path",
                    field.getKey(),
                    sourcePath,
                    evidenceIds));
          }
        }
        if (!sourcePaths.isEmpty()) {
          factPathsById.computeIfAbsent(id, ignored -> new TreeSet<>()).addAll(sourcePaths);
        }
      } else {
        validateEvidenceIds(node, artifacts);
      }

      Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
      while (fields.hasNext()) {
        Map.Entry<String, JsonNode> field = fields.next();
        collectProjectMapFacts(
            field.getValue(),
            childPointer(pointer, field.getKey()),
            artifacts,
            factsByPath,
            factPathsById,
            factIds);
      }
      return;
    }
    if (node.isArray()) {
      for (int index = 0; index < node.size(); index++) {
        collectProjectMapFacts(
            node.get(index),
            childPointer(pointer, index),
            artifacts,
            factsByPath,
            factPathsById,
            factIds);
      }
    }
  }

  private void collectGraphNodeMatches(
      ProjectMemoryArtifacts artifacts,
      ProjectMapIndex projectMapIndex,
      Map<String, LinkedHashSet<DirectMatchRow>> matches) throws QueryArtifactException {
    JsonNode nodes = artifacts.projectGraph().path("nodes");
    if (!nodes.isArray()) {
      throw new QueryArtifactException("Malformed project-graph.json.");
    }
    for (int index = 0; index < nodes.size(); index++) {
      JsonNode node = nodes.get(index);
      String nodeId = requiredText(node, "id", "Malformed project-graph.json.");
      List<String> evidenceIds = collectGraphEvidenceIds(node, artifacts);
      Set<String> sourcePaths = new TreeSet<>();
      for (String evidenceId : evidenceIds) {
        JsonNode evidence = artifacts.evidenceById().get(evidenceId);
        sourcePaths.add(requiredText(evidence, "path", "Malformed evidence-index.jsonl."));
      }
      String sourceRefId = textOrNull(node.at("/source_ref/id"));
      if (sourceRefId != null) {
        sourcePaths.addAll(projectMapIndex.factPathsById().getOrDefault(sourceRefId, Set.of()));
      }
      Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
      while (fields.hasNext()) {
        Map.Entry<String, JsonNode> field = fields.next();
        sourcePaths.addAll(sourcePathValues(field.getKey(), field.getValue()));
      }
      for (String sourcePath : sourcePaths) {
        addMatch(
            matches,
            sourcePath,
            DirectMatchRow.graphNode(sourcePath, nodeId, childPointer("/nodes", index), sourceRefId,
                evidenceIds));
      }
    }
  }

  private Optional<String> factId(String pointer, JsonNode node) {
    JsonNode id = node.path("id");
    if (id.isTextual() && !id.asText().isBlank()) {
      return Optional.of(id.asText());
    }
    if (pointer.startsWith("/project/modules/items/")) {
      JsonNode moduleId = node.path("module_id");
      if (moduleId.isTextual() && !moduleId.asText().isBlank()) {
        return Optional.of(moduleId.asText());
      }
    }
    return Optional.empty();
  }

  private List<String> collectEvidenceIds(JsonNode node, ProjectMemoryArtifacts artifacts)
      throws QueryArtifactException {
    List<String> result = new ArrayList<>();
    Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
    while (fields.hasNext()) {
      Map.Entry<String, JsonNode> field = fields.next();
      if (isEvidenceIdField(field.getKey())) {
        appendEvidenceIds(field.getValue(), artifacts, result);
      }
    }
    return List.copyOf(new LinkedHashSet<>(result));
  }

  private void validateEvidenceIds(JsonNode node, ProjectMemoryArtifacts artifacts)
      throws QueryArtifactException {
    Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
    while (fields.hasNext()) {
      Map.Entry<String, JsonNode> field = fields.next();
      if (isEvidenceIdField(field.getKey())) {
        appendEvidenceIds(field.getValue(), artifacts, new ArrayList<>());
      }
    }
  }

  private List<String> collectGraphEvidenceIds(JsonNode node, ProjectMemoryArtifacts artifacts)
      throws QueryArtifactException {
    List<String> result = new ArrayList<>();
    JsonNode evidenceIds = node.path("evidence_ids");
    if (evidenceIds.isMissingNode()) {
      return List.of();
    }
    appendEvidenceIds(evidenceIds, artifacts, result);
    return List.copyOf(new LinkedHashSet<>(result));
  }

  private void appendEvidenceIds(JsonNode value, ProjectMemoryArtifacts artifacts, List<String> result)
      throws QueryArtifactException {
    if (!value.isArray()) {
      throw new QueryArtifactException("Invalid project-map.json evidence reference.");
    }
    for (JsonNode id : value) {
      if (!id.isTextual() || !artifacts.evidenceById().containsKey(id.asText())) {
        throw new QueryArtifactException("Invalid project-map.json evidence reference.");
      }
      result.add(id.asText());
    }
  }

  private boolean isEvidenceIdField(String fieldName) {
    return "evidence_ids".equals(fieldName) || fieldName.endsWith("_evidence_ids");
  }

  private List<String> sourcePathValues(String fieldName, JsonNode value) {
    if (!isSourcePathField(fieldName)) {
      return List.of();
    }
    List<String> paths = new ArrayList<>();
    if (value.isTextual()) {
      normalizeChangedFile(value.asText()).ifPresent(paths::add);
    } else if (value.isArray()) {
      for (JsonNode item : value) {
        if (item.isTextual()) {
          normalizeChangedFile(item.asText()).ifPresent(paths::add);
        }
      }
    }
    return List.copyOf(new TreeSet<>(paths));
  }

  private boolean isSourcePathField(String fieldName) {
    return "path".equals(fieldName)
        || "file".equals(fieldName)
        || fieldName.endsWith("_path")
        || fieldName.endsWith("_paths")
        || fieldName.endsWith("_file")
        || fieldName.endsWith("_files");
  }

  private Optional<String> normalizeChangedFile(String rawValue) {
    if (rawValue == null || rawValue.isEmpty() || rawValue.isBlank()) {
      return Optional.empty();
    }
    if (!rawValue.equals(rawValue.strip())) {
      return Optional.empty();
    }
    if (rawValue.indexOf('\n') >= 0 || rawValue.indexOf('\r') >= 0) {
      return Optional.empty();
    }
    for (String prefix : RAW_DIFF_PREFIXES) {
      if (rawValue.startsWith(prefix)) {
        return Optional.empty();
      }
    }
    if (rawValue.startsWith("-") || rawValue.startsWith("./") || ".".equals(rawValue)) {
      return Optional.empty();
    }
    if (rawValue.contains("\\") || isUrlLike(rawValue) || isGlobOrRegexLike(rawValue)) {
      return Optional.empty();
    }
    if (".project-memory".equals(rawValue) || rawValue.startsWith(".project-memory/")) {
      return Optional.empty();
    }
    try {
      if (Path.of(rawValue).isAbsolute()) {
        return Optional.empty();
      }
    } catch (InvalidPathException ex) {
      return Optional.empty();
    }
    String[] segments = rawValue.split("/", -1);
    for (String segment : segments) {
      if (segment.isEmpty() || ".".equals(segment) || "..".equals(segment)) {
        return Optional.empty();
      }
    }
    return Optional.of(rawValue);
  }

  private boolean isUrlLike(String value) {
    return value.matches("^[A-Za-z][A-Za-z0-9+.-]*:.*")
        || value.startsWith("www.");
  }

  private boolean isGlobOrRegexLike(String value) {
    return value.indexOf('*') >= 0
        || value.indexOf('?') >= 0
        || value.indexOf('[') >= 0
        || value.indexOf(']') >= 0
        || value.indexOf('{') >= 0
        || value.indexOf('}') >= 0
        || value.indexOf('|') >= 0
        || value.startsWith("^")
        || value.endsWith("$");
  }

  private void appendHeader(
      List<String> lines,
      ProjectMemoryArtifacts artifacts,
      int changedFileCount,
      int directMatchCount,
      int notRepresentedCount,
      int diagnosticCount) {
    lines.add("Query: impact");
    lines.add(
        "Source artifacts: project-map.json schema_version="
            + safe(artifacts.projectMapSchemaVersion())
            + ", evidence-index.jsonl records="
            + artifacts.evidenceRecords().size()
            + ", project-graph.json graph_schema_version="
            + safe(artifacts.projectGraphSchemaVersion()));
    lines.add("Changed files: " + changedFileCount);
    lines.add(
        "Results: direct_match="
            + directMatchCount
            + ", not_represented="
            + notRepresentedCount
            + ", diagnostic="
            + diagnosticCount);
    lines.add("");
  }

  private void appendDirectMatches(List<String> lines, List<DirectMatchRow> rows) {
    lines.add("Direct matches");
    if (rows.isEmpty()) {
      lines.add("No direct matches found.");
      lines.add("");
      return;
    }
    for (int index = 0; index < rows.size(); index++) {
      DirectMatchRow row = rows.get(index);
      lines.add((index + 1) + ". direct_match");
      field(lines, "   ", "changed_file", row.changedFile(), true);
      field(lines, "   ", "match_type", row.matchType(), false);
      field(lines, "   ", "artifact", row.artifact(), false);
      if (row.evidenceId() != null) {
        field(lines, "   ", "evidence_id", row.evidenceId(), true);
      }
      if (row.sourceType() != null) {
        field(lines, "   ", "source_type", row.sourceType(), true);
      }
      if (row.factId() != null) {
        field(lines, "   ", "fact_id", row.factId(), true);
      }
      if (row.nodeId() != null) {
        field(lines, "   ", "node_id", row.nodeId(), true);
      }
      if (row.sourceRefId() != null) {
        field(lines, "   ", "source_ref.id", row.sourceRefId(), true);
      }
      if (row.matchedField() != null) {
        field(lines, "   ", "matched_field", row.matchedField(), false);
      }
      if (row.matchedPath() != null) {
        field(lines, "   ", "matched_path", row.matchedPath(), true);
      }
      if (row.navigation() != null) {
        field(lines, "   ", "navigation", row.navigation() + " (not evidence)", true);
      }
      lines.add("   confidence: high");
      lines.add("   evidence_ids: " + safe(joinIds(row.evidenceIds()), true));
    }
    lines.add("");
  }

  private void appendNotRepresented(List<String> lines, List<NotRepresentedRow> rows) {
    lines.add("Not represented");
    if (rows.isEmpty()) {
      lines.add("No not_represented files.");
      lines.add("");
      return;
    }
    for (int index = 0; index < rows.size(); index++) {
      NotRepresentedRow row = rows.get(index);
      lines.add((index + 1) + ". not_represented");
      field(lines, "   ", "changed_file", row.changedFile(), true);
      lines.add("   reason: no accepted artifact reference");
    }
    lines.add("");
  }

  private void appendDiagnostics(List<String> lines, List<DiagnosticRow> diagnostics) {
    lines.add("Diagnostics");
    if (diagnostics.isEmpty()) {
      lines.add("No diagnostics.");
      lines.add("");
      return;
    }
    for (int index = 0; index < diagnostics.size(); index++) {
      DiagnosticRow diagnostic = diagnostics.get(index);
      lines.add((index + 1) + ". diagnostic");
      field(lines, "   ", "code", diagnostic.code(), false);
      if (diagnostic.changedFile() != null) {
        field(lines, "   ", "changed_file", diagnostic.changedFile(), true);
      }
      field(lines, "   ", "message", diagnostic.message(), true);
    }
    lines.add("");
  }

  private void appendBoundaries(List<String> lines) {
    lines.add("Authority and boundaries");
    lines.add("- impact output is navigation and presentation only; it is not evidence.");
    lines.add("- direct_match rows are direct artifact references, not complete impact analysis.");
    lines.add("- not_represented means no accepted artifact directly referenced the changed file.");
    lines.add("- no graph projection, raw diff parsing, Git inspection, source readback, scan refresh, "
        + "generated artifact mutation, impact report write, repository write, network access, "
        + "credential lookup, runtime tracing, call graph, vulnerability claim, "
        + "business-priority claim, or automatic code modification.");
  }

  private void addFactMatch(
      Map<String, List<FactMatch>> factsByPath,
      String sourcePath,
      FactMatch fact) {
    factsByPath.computeIfAbsent(sourcePath, ignored -> new ArrayList<>()).add(fact);
  }

  private void addMatch(
      Map<String, LinkedHashSet<DirectMatchRow>> matches,
      String changedFile,
      DirectMatchRow row) {
    matches.computeIfAbsent(changedFile, ignored -> new LinkedHashSet<>()).add(row);
  }

  private Map<String, List<DirectMatchRow>> freeze(
      Map<String, LinkedHashSet<DirectMatchRow>> matches) {
    Map<String, List<DirectMatchRow>> result = new HashMap<>();
    for (Map.Entry<String, LinkedHashSet<DirectMatchRow>> entry : matches.entrySet()) {
      result.put(entry.getKey(), entry.getValue().stream()
          .sorted(DirectMatchRow.ORDERING)
          .toList());
    }
    return Map.copyOf(result);
  }

  private static List<DiagnosticRow> boundedDiagnostics(List<DiagnosticRow> diagnostics) {
    return diagnostics.stream()
        .sorted(DiagnosticRow.ORDERING)
        .limit(MAX_DIAGNOSTICS)
        .toList();
  }

  private String joinIds(List<String> ids) {
    if (ids.isEmpty()) {
      return "none";
    }
    return String.join(", ", ids);
  }

  private String requiredText(JsonNode node, String fieldName, String message)
      throws QueryArtifactException {
    JsonNode value = node == null ? null : node.path(fieldName);
    if (value == null || !value.isTextual() || value.asText().isBlank()) {
      throw new QueryArtifactException(message);
    }
    return value.asText();
  }

  private String textOrNull(JsonNode node) {
    return node != null && node.isTextual() && !node.asText().isBlank() ? node.asText() : null;
  }

  private void field(List<String> lines, String indent, String name, String value, boolean redact) {
    lines.add(indent + name + ": " + safe(value, redact));
  }

  private String childPointer(String parent, String fieldName) {
    return (parent == null || parent.isBlank() ? "" : parent) + "/" + escapePointer(fieldName);
  }

  private String childPointer(String parent, int index) {
    return (parent == null || parent.isBlank() ? "" : parent) + "/" + index;
  }

  private String escapePointer(String value) {
    return value.replace("~", "~0").replace("/", "~1");
  }

  private String safe(String value) {
    return safe(value, false);
  }

  private String safe(String value, boolean redact) {
    if (value == null) {
      return "null";
    }
    String rendered = redact ? OutputRedactor.redact(value) : value;
    String bounded = rendered.length() <= MAX_TEXT_CHARS
        ? rendered
        : rendered.substring(0, MAX_TEXT_CHARS) + "...[truncated]";
    StringBuilder result = new StringBuilder(bounded.length());
    for (int index = 0; index < bounded.length(); index++) {
      char ch = bounded.charAt(index);
      if (ch == '\n') {
        result.append("\\n");
      } else if (ch == '\r') {
        result.append("\\r");
      } else if (ch == '\t') {
        result.append("\\t");
      } else if (Character.isISOControl(ch)) {
        result.append(String.format("\\u%04x", (int) ch));
      } else {
        result.append(ch);
      }
    }
    return result.toString();
  }

  public record ImpactInput(List<String> changedFiles, List<DiagnosticRow> diagnostics) {
    public ImpactInput {
      changedFiles = List.copyOf(Objects.requireNonNull(changedFiles, "changedFiles"));
      diagnostics = List.copyOf(Objects.requireNonNull(diagnostics, "diagnostics"));
    }
  }

  public static final class ImpactInputException extends Exception {
    ImpactInputException(String message) {
      super(message);
    }
  }

  public record DiagnosticRow(String code, String changedFile, String message) {
    private static final Comparator<DiagnosticRow> ORDERING = Comparator
        .comparing(DiagnosticRow::code)
        .thenComparing(row -> row.changedFile() == null ? "" : row.changedFile())
        .thenComparing(DiagnosticRow::message);

    static DiagnosticRow duplicate(String changedFile) {
      return new DiagnosticRow(
          "duplicate_changed_file",
          changedFile,
          "Duplicate changed-file input ignored.");
    }
  }

  private record ImpactIndex(Map<String, List<DirectMatchRow>> matchesByPath) {
  }

  private record ProjectMapIndex(
      Map<String, List<FactMatch>> factsByPath,
      Map<String, Set<String>> factPathsById) {
  }

  private record FactMatch(
      String factId,
      String pointer,
      String matchType,
      String matchedField,
      String matchedPath,
      List<String> evidenceIds) {
    private FactMatch {
      evidenceIds = List.copyOf(evidenceIds);
    }
  }

  private record NotRepresentedRow(String changedFile) {
  }

  private record DirectMatchRow(
      String changedFile,
      String matchType,
      String artifact,
      String evidenceId,
      String sourceType,
      String factId,
      String nodeId,
      String sourceRefId,
      String matchedField,
      String matchedPath,
      String navigation,
      List<String> evidenceIds) {
    private static final Comparator<DirectMatchRow> ORDERING = Comparator
        .comparing(DirectMatchRow::changedFile)
        .thenComparing(DirectMatchRow::matchType)
        .thenComparing(DirectMatchRow::artifact)
        .thenComparing(row -> row.evidenceId() == null ? "" : row.evidenceId())
        .thenComparing(row -> row.factId() == null ? "" : row.factId())
        .thenComparing(row -> row.nodeId() == null ? "" : row.nodeId())
        .thenComparing(row -> row.navigation() == null ? "" : row.navigation());

    private DirectMatchRow {
      evidenceIds = List.copyOf(evidenceIds);
    }

    static DirectMatchRow evidence(String changedFile, JsonNode evidence) {
      String evidenceId = evidence.path("id").asText();
      return new DirectMatchRow(
          changedFile,
          "evidence_path",
          "evidence-index.jsonl",
          evidenceId,
          evidence.path("source_type").asText(null),
          null,
          null,
          null,
          "path",
          changedFile,
          "evidence-index.jsonl#/records/" + evidenceId,
          List.of(evidenceId));
    }

    static DirectMatchRow fact(String changedFile, FactMatch fact) {
      return new DirectMatchRow(
          changedFile,
          fact.matchType(),
          "project-map.json",
          null,
          null,
          fact.factId(),
          null,
          null,
          fact.matchedField(),
          fact.matchedPath(),
          "project-map.json#" + (fact.pointer().isBlank() ? "/" : fact.pointer()),
          fact.evidenceIds());
    }

    static DirectMatchRow graphNode(
        String changedFile,
        String nodeId,
        String pointer,
        String sourceRefId,
        List<String> evidenceIds) {
      return new DirectMatchRow(
          changedFile,
          "graph_node",
          "project-graph.json",
          null,
          null,
          null,
          nodeId,
          sourceRefId,
          "node_source",
          changedFile,
          "project-graph.json#" + pointer,
          evidenceIds);
    }
  }
}
