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
  private static final int MAX_GRAPH_PROJECTION_ROWS = 1000;
  private static final int MAX_PLANNING_HINTS = 256;
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

    ImpactProjection projection = projectImpact(index, directMatches);
    diagnostics.addAll(projection.diagnostics());
    diagnostics = boundedDiagnostics(diagnostics);

    List<String> lines = new ArrayList<>();
    appendHeader(lines, artifacts, input.changedFiles().size(), directMatches.size(),
        projection.graphNeighbors().size(), projection.relationStatuses().size(),
        projection.planningHints().size(), notRepresented.size(), diagnostics.size());
    appendDirectMatches(lines, directMatches);
    appendGraphNeighbors(lines, projection.graphNeighbors());
    appendRelationStatuses(lines, projection.relationStatuses());
    appendPlanningHints(lines, projection.planningHints());
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
    GraphIndex graphIndex = collectGraphIndex(artifacts, projectMapIndex, matches);
    Map<String, List<PlanningHintMatch>> planningHintsBySubjectId =
        collectPlanningHints(artifacts.projectMap());
    return new ImpactIndex(freeze(matches), graphIndex, planningHintsBySubjectId);
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
        Set<String> evidencePaths = new TreeSet<>();
        for (String evidenceId : evidenceIds) {
          JsonNode evidence = artifacts.evidenceById().get(evidenceId);
          String evidencePath = requiredText(evidence, "path", "Malformed evidence-index.jsonl.");
          evidencePaths.add(evidencePath);
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
            if (!evidencePaths.contains(sourcePath)) {
              continue;
            }
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

  private GraphIndex collectGraphIndex(
      ProjectMemoryArtifacts artifacts,
      ProjectMapIndex projectMapIndex,
      Map<String, LinkedHashSet<DirectMatchRow>> matches) throws QueryArtifactException {
    Map<String, JsonNode> nodesById = new HashMap<>();
    JsonNode nodes = artifacts.projectGraph().path("nodes");
    if (!nodes.isArray()) {
      throw new QueryArtifactException("Malformed project-graph.json.");
    }
    for (int index = 0; index < nodes.size(); index++) {
      JsonNode node = nodes.get(index);
      String nodeId = requiredText(node, "id", "Malformed project-graph.json.");
      if (nodesById.putIfAbsent(nodeId, node) != null) {
        throw new QueryArtifactException("Duplicate graph id in project-graph.json.");
      }
      List<String> evidenceIds = collectGraphEvidenceIds(node, artifacts);
      Set<String> sourcePaths = new TreeSet<>();
      Set<String> evidencePaths = new TreeSet<>();
      for (String evidenceId : evidenceIds) {
        JsonNode evidence = artifacts.evidenceById().get(evidenceId);
        String evidencePath = requiredText(evidence, "path", "Malformed evidence-index.jsonl.");
        evidencePaths.add(evidencePath);
        sourcePaths.add(evidencePath);
      }
      String sourceRefId = textOrNull(node.at("/source_ref/id"));
      if (sourceRefId != null) {
        sourcePaths.addAll(projectMapIndex.factPathsById().getOrDefault(sourceRefId, Set.of()));
      }
      Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
      while (fields.hasNext()) {
        Map.Entry<String, JsonNode> field = fields.next();
        for (String sourcePath : sourcePathValues(field.getKey(), field.getValue())) {
          if (evidencePaths.contains(sourcePath)) {
            sourcePaths.add(sourcePath);
          }
        }
      }
      for (String sourcePath : sourcePaths) {
        addMatch(
            matches,
            sourcePath,
            DirectMatchRow.graphNode(sourcePath, nodeId, childPointer("/nodes", index), sourceRefId,
                evidenceIds));
      }
    }

    Map<String, List<JsonNode>> edgesByNodeId = new HashMap<>();
    JsonNode edges = artifacts.projectGraph().path("edges");
    if (!edges.isArray()) {
      throw new QueryArtifactException("Malformed project-graph.json.");
    }
    for (JsonNode edge : edges) {
      String sourceId = requiredText(edge, "source_id", "Malformed project-graph.json.");
      String targetId = requiredText(edge, "target_id", "Malformed project-graph.json.");
      edgesByNodeId.computeIfAbsent(sourceId, ignored -> new ArrayList<>()).add(edge);
      edgesByNodeId.computeIfAbsent(targetId, ignored -> new ArrayList<>()).add(edge);
    }

    Map<String, List<JsonNode>> relationStatusesByNodeId = new HashMap<>();
    JsonNode statuses = artifacts.projectGraph().path("relation_statuses");
    if (!statuses.isArray()) {
      throw new QueryArtifactException("Malformed project-graph.json.");
    }
    for (JsonNode status : statuses) {
      String sourceId = textOrNull(status.path("source_id"));
      String targetId = textOrNull(status.path("target_id"));
      if (sourceId != null) {
        relationStatusesByNodeId.computeIfAbsent(sourceId, ignored -> new ArrayList<>()).add(status);
      }
      if (targetId != null) {
        relationStatusesByNodeId.computeIfAbsent(targetId, ignored -> new ArrayList<>()).add(status);
      }
    }
    return new GraphIndex(
        Map.copyOf(nodesById),
        freezeJsonRows(edgesByNodeId),
        freezeJsonRows(relationStatusesByNodeId));
  }

  private Map<String, List<PlanningHintMatch>> collectPlanningHints(JsonNode projectMap) {
    JsonNode items = projectMap.at("/quality/change_risk_signals/items");
    if (!items.isArray()) {
      return Map.of();
    }
    Map<String, List<PlanningHintMatch>> bySubjectId = new HashMap<>();
    for (int index = 0; index < items.size(); index++) {
      JsonNode item = items.get(index);
      String id = textOrNull(item.path("id"));
      String subjectId = textOrNull(item.path("subject_id"));
      if (id == null || subjectId == null) {
        continue;
      }
      PlanningHintMatch hint = new PlanningHintMatch(
          id,
          subjectId,
          textOrNull(item.path("subject_kind")),
          textOrNull(item.path("subject_name")),
          textOrNull(item.path("signal")),
          textOrNull(item.path("status")),
          textOrNull(item.path("risk_basis")),
          textOrNull(item.path("confidence")),
          textOrNull(item.path("uncertainty")),
          "project-map.json#/quality/change_risk_signals/items/" + index,
          jsonStringArray(item.path("evidence_ids")));
      bySubjectId.computeIfAbsent(subjectId, ignored -> new ArrayList<>()).add(hint);
    }
    return freezePlanningHints(bySubjectId);
  }

  private ImpactProjection projectImpact(ImpactIndex index, List<DirectMatchRow> directMatches) {
    TreeSet<String> startNodeIds = new TreeSet<>();
    TreeSet<String> directSubjectIds = new TreeSet<>();
    for (DirectMatchRow directMatch : directMatches) {
      if (directMatch.nodeId() != null) {
        startNodeIds.add(directMatch.nodeId());
      }
      if (directMatch.factId() != null) {
        directSubjectIds.add(directMatch.factId());
      }
      if (directMatch.sourceRefId() != null) {
        directSubjectIds.add(directMatch.sourceRefId());
      }
    }

    List<GraphNeighborRow> graphNeighbors = new ArrayList<>();
    List<RelationStatusRow> relationStatuses = new ArrayList<>();
    for (String startNodeId : startNodeIds) {
      for (JsonNode edge : index.graphIndex().edgesByNodeId()
          .getOrDefault(startNodeId, List.of())) {
        graphNeighbors.add(graphNeighborRow(index.graphIndex(), startNodeId, edge));
      }
      for (JsonNode status : index.graphIndex().relationStatusesByNodeId()
          .getOrDefault(startNodeId, List.of())) {
        relationStatuses.add(relationStatusRow(startNodeId, status));
      }
    }

    graphNeighbors = graphNeighbors.stream()
        .distinct()
        .sorted(GraphNeighborRow.ORDERING)
        .toList();
    relationStatuses = relationStatuses.stream()
        .distinct()
        .sorted(RelationStatusRow.ORDERING)
        .toList();

    List<DiagnosticRow> diagnostics = new ArrayList<>();
    ProjectionCapResult projectionCap = capProjectionRows(graphNeighbors, relationStatuses);
    graphNeighbors = projectionCap.graphNeighbors();
    relationStatuses = projectionCap.relationStatuses();
    diagnostics.addAll(projectionCap.diagnostics());

    List<PlanningHintRow> planningHints = planningHints(
        index,
        directSubjectIds,
        startNodeIds,
        graphNeighbors);
    if (planningHints.size() > MAX_PLANNING_HINTS) {
      diagnostics.add(new DiagnosticRow(
          "planning_hint_cap_reached",
          null,
          "Only the first " + MAX_PLANNING_HINTS + " planning hints were rendered."));
      planningHints = planningHints.subList(0, MAX_PLANNING_HINTS);
    }

    return new ImpactProjection(
        List.copyOf(graphNeighbors),
        List.copyOf(relationStatuses),
        List.copyOf(planningHints),
        List.copyOf(diagnostics));
  }

  private ProjectionCapResult capProjectionRows(
      List<GraphNeighborRow> graphNeighbors,
      List<RelationStatusRow> relationStatuses) {
    List<ProjectionCandidate> candidates = new ArrayList<>();
    for (GraphNeighborRow row : graphNeighbors) {
      candidates.add(ProjectionCandidate.graphNeighbor(row));
    }
    for (RelationStatusRow row : relationStatuses) {
      candidates.add(ProjectionCandidate.relationStatus(row));
    }
    candidates = candidates.stream()
        .sorted(ProjectionCandidate.ORDERING)
        .toList();
    if (candidates.size() <= MAX_GRAPH_PROJECTION_ROWS) {
      return new ProjectionCapResult(graphNeighbors, relationStatuses, List.of());
    }

    List<GraphNeighborRow> cappedNeighbors = new ArrayList<>();
    List<RelationStatusRow> cappedStatuses = new ArrayList<>();
    for (ProjectionCandidate candidate : candidates.subList(0, MAX_GRAPH_PROJECTION_ROWS)) {
      if (candidate.graphNeighbor() != null) {
        cappedNeighbors.add(candidate.graphNeighbor());
      } else {
        cappedStatuses.add(candidate.relationStatus());
      }
    }
    return new ProjectionCapResult(
        cappedNeighbors.stream().sorted(GraphNeighborRow.ORDERING).toList(),
        cappedStatuses.stream().sorted(RelationStatusRow.ORDERING).toList(),
        List.of(new DiagnosticRow(
            "graph_projection_cap_reached",
            null,
            "Only the first " + MAX_GRAPH_PROJECTION_ROWS + " graph projection rows were rendered.")));
  }

  private List<PlanningHintRow> planningHints(
      ImpactIndex index,
      Set<String> directSubjectIds,
      Set<String> startNodeIds,
      List<GraphNeighborRow> graphNeighbors) {
    TreeSet<String> subjectIds = new TreeSet<>(directSubjectIds);
    Map<String, TreeSet<String>> tiedNodeIdsBySubjectId = new HashMap<>();
    for (String startNodeId : startNodeIds) {
      addNodeSubject(index.graphIndex(), subjectIds, tiedNodeIdsBySubjectId, startNodeId);
    }
    for (GraphNeighborRow graphNeighbor : graphNeighbors) {
      addNodeSubject(index.graphIndex(), subjectIds, tiedNodeIdsBySubjectId,
          graphNeighbor.neighborNodeId());
    }

    Map<String, PlanningHintRow> hintsById = new HashMap<>();
    for (String subjectId : subjectIds) {
      for (PlanningHintMatch hint : index.planningHintsBySubjectId()
          .getOrDefault(subjectId, List.of())) {
        hintsById.putIfAbsent(hint.id(), PlanningHintRow.from(
            hint,
            tiedNodeIdsBySubjectId.getOrDefault(subjectId, new TreeSet<>())));
      }
    }
    return hintsById.values().stream()
        .sorted(PlanningHintRow.ORDERING)
        .toList();
  }

  private void addNodeSubject(
      GraphIndex graphIndex,
      Set<String> subjectIds,
      Map<String, TreeSet<String>> tiedNodeIdsBySubjectId,
      String nodeId) {
    if (nodeId == null) {
      return;
    }
    subjectIds.add(nodeId);
    tiedNodeIdsBySubjectId.computeIfAbsent(nodeId, ignored -> new TreeSet<>()).add(nodeId);
    String sourceRefId = nodeSourceRefId(graphIndex, nodeId);
    if (sourceRefId != null) {
      subjectIds.add(sourceRefId);
      tiedNodeIdsBySubjectId.computeIfAbsent(sourceRefId, ignored -> new TreeSet<>()).add(nodeId);
    }
  }

  private GraphNeighborRow graphNeighborRow(
      GraphIndex graphIndex,
      String startNodeId,
      JsonNode edge) {
    String sourceId = textOrNull(edge.path("source_id"));
    String targetId = textOrNull(edge.path("target_id"));
    String neighborNodeId = startNodeId.equals(sourceId) ? targetId : sourceId;
    JsonNode neighborNode = graphIndex.nodesById().get(neighborNodeId);
    return new GraphNeighborRow(
        startNodeId,
        relationDirection(sourceId, targetId, startNodeId),
        textOrNull(edge.path("id")),
        textOrNull(edge.path("type")),
        sourceId,
        targetId,
        neighborNodeId,
        textOrNull(neighborNode == null ? null : neighborNode.path("kind")),
        textOrNull(neighborNode == null ? null : neighborNode.path("label")),
        textOrNull(neighborNode == null ? null : neighborNode.at("/source_ref/id")),
        textOrNull(edge.path("claim_category")),
        textOrNull(edge.path("relation_status")),
        textOrNull(edge.path("support_type")),
        graphNeighborConfidence(edge),
        textOrNull(edge.path("confidence")),
        textOrNull(edge.path("uncertainty")),
        attributes(edge.path("relation_attributes")),
        derivation(edge.path("derivation")),
        jsonStringArray(edge.path("evidence_ids")));
  }

  private RelationStatusRow relationStatusRow(String startNodeId, JsonNode status) {
    String sourceId = textOrNull(status.path("source_id"));
    String targetId = textOrNull(status.path("target_id"));
    return new RelationStatusRow(
        startNodeId,
        relationDirection(sourceId, targetId, startNodeId),
        textOrNull(status.path("id")),
        textOrNull(status.path("relation_family")),
        sourceId,
        targetId,
        textOrNull(status.path("claim_category")),
        textOrNull(status.path("relation_status")),
        textOrNull(status.path("support_type")),
        "low",
        textOrNull(status.path("confidence")),
        textOrNull(status.path("uncertainty")),
        attributes(status.path("relation_attributes")),
        derivation(status.path("derivation")),
        jsonStringArray(status.path("evidence_ids")));
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

  private Map<String, List<JsonNode>> freezeJsonRows(Map<String, List<JsonNode>> rowsByNodeId) {
    Map<String, List<JsonNode>> result = new HashMap<>();
    for (Map.Entry<String, List<JsonNode>> entry : rowsByNodeId.entrySet()) {
      result.put(entry.getKey(), entry.getValue().stream()
          .sorted(Comparator.comparing(row -> textOrEmpty(row.path("id"))))
          .toList());
    }
    return Map.copyOf(result);
  }

  private Map<String, List<PlanningHintMatch>> freezePlanningHints(
      Map<String, List<PlanningHintMatch>> hintsBySubjectId) {
    Map<String, List<PlanningHintMatch>> result = new HashMap<>();
    for (Map.Entry<String, List<PlanningHintMatch>> entry : hintsBySubjectId.entrySet()) {
      result.put(entry.getKey(), entry.getValue().stream()
          .sorted(PlanningHintMatch.ORDERING)
          .toList());
    }
    return Map.copyOf(result);
  }

  private List<String> jsonStringArray(JsonNode values) {
    if (!values.isArray()) {
      return List.of();
    }
    LinkedHashSet<String> result = new LinkedHashSet<>();
    for (JsonNode value : values) {
      if (value.isTextual() && !value.asText().isBlank()) {
        result.add(value.asText());
      }
    }
    return List.copyOf(result);
  }

  private String nodeSourceRefId(GraphIndex graphIndex, String nodeId) {
    JsonNode node = graphIndex.nodesById().get(nodeId);
    return textOrNull(node == null ? null : node.at("/source_ref/id"));
  }

  private String relationDirection(String sourceId, String targetId, String selectedNodeId) {
    boolean outgoing = selectedNodeId.equals(sourceId);
    boolean incoming = selectedNodeId.equals(targetId);
    if (incoming && outgoing) {
      return "self";
    }
    if (incoming) {
      return "incoming";
    }
    if (outgoing) {
      return "outgoing";
    }
    return "unknown";
  }

  private String graphNeighborConfidence(JsonNode edge) {
    String type = textOrNull(edge.path("type"));
    String claimCategory = textOrNull(edge.path("claim_category"));
    String supportType = textOrNull(edge.path("support_type"));
    String uncertainty = textOrNull(edge.path("uncertainty"));
    if ("owns".equals(type)
        || "declares".equals(type)
        || "structural".equals(claimCategory)
        || "project_map_derivation".equals(supportType)
        || "uncertain".equals(claimCategory)
        || "document_reference".equals(type)
        || uncertainty != null) {
      return "low";
    }
    return "medium";
  }

  private String attributes(JsonNode attributes) {
    if (!attributes.isObject()) {
      return "{}";
    }
    TreeSet<String> values = new TreeSet<>();
    Iterator<Map.Entry<String, JsonNode>> fields = attributes.fields();
    while (fields.hasNext()) {
      Map.Entry<String, JsonNode> field = fields.next();
      values.add(safe(field.getKey()) + "=" + mapValueText(field.getKey(), field.getValue()));
    }
    return values.isEmpty() ? "{}" : String.join(", ", values);
  }

  private String derivation(JsonNode derivation) {
    if (!derivation.isObject()) {
      return "null";
    }
    return "kind="
        + valueText(derivation.path("kind"), false)
        + " artifact="
        + valueText(derivation.path("artifact"), false)
        + " section="
        + valueText(derivation.path("section"), false)
        + " fields="
        + arrayText(derivation.path("fields"))
        + " (not evidence)";
  }

  private String arrayText(JsonNode node) {
    if (!node.isArray()) {
      return "null";
    }
    List<String> values = new ArrayList<>();
    for (JsonNode value : node) {
      values.add(valueText(value, false));
    }
    return values.isEmpty() ? "none" : String.join(", ", values);
  }

  private String mapValueText(String key, JsonNode node) {
    if (node == null || node.isMissingNode() || node.isNull()) {
      return "null";
    }
    String raw = node.isTextual() || node.isNumber() || node.isBoolean()
        ? node.asText()
        : node.toString();
    return safe(OutputRedactor.redactMapValue(key, raw));
  }

  private String valueText(JsonNode node, boolean redact) {
    if (node == null || node.isMissingNode() || node.isNull()) {
      return "null";
    }
    if (node.isTextual() || node.isNumber() || node.isBoolean()) {
      return safe(node.asText(), redact);
    }
    return safe(node.toString(), redact);
  }

  private String textOrEmpty(JsonNode node) {
    String value = textOrNull(node);
    return value == null ? "" : value;
  }

  private void appendHeader(
      List<String> lines,
      ProjectMemoryArtifacts artifacts,
      int changedFileCount,
      int directMatchCount,
      int graphNeighborCount,
      int relationStatusCount,
      int planningHintCount,
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
            + ", graph_neighbor="
            + graphNeighborCount
            + ", relation_status="
            + relationStatusCount
            + ", planning_hint="
            + planningHintCount
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
        fieldLocator(lines, "   ", "evidence_id", row.evidenceId());
      }
      if (row.sourceType() != null) {
        field(lines, "   ", "source_type", row.sourceType(), true);
      }
      if (row.factId() != null) {
        fieldLocator(lines, "   ", "fact_id", row.factId());
      }
      if (row.nodeId() != null) {
        fieldLocator(lines, "   ", "node_id", row.nodeId());
      }
      if (row.sourceRefId() != null) {
        fieldLocator(lines, "   ", "source_ref.id", row.sourceRefId());
      }
      if (row.matchedField() != null) {
        field(lines, "   ", "matched_field", row.matchedField(), false);
      }
      if (row.matchedPath() != null) {
        field(lines, "   ", "matched_path", row.matchedPath(), true);
      }
      if (row.navigation() != null) {
        fieldLocator(lines, "   ", "navigation", row.navigation() + " (not evidence)");
      }
      lines.add("   confidence: high");
      lines.add("   evidence_ids: " + safeLocators(row.evidenceIds()));
    }
    lines.add("");
  }

  private void appendGraphNeighbors(List<String> lines, List<GraphNeighborRow> rows) {
    lines.add("Graph neighbors");
    if (rows.isEmpty()) {
      lines.add("No graph_neighbor rows.");
      lines.add("");
      return;
    }
    for (int index = 0; index < rows.size(); index++) {
      GraphNeighborRow row = rows.get(index);
      lines.add((index + 1) + ". graph_neighbor");
      fieldLocator(lines, "   ", "start_node_id", row.startNodeId());
      field(lines, "   ", "direction", row.direction(), false);
      fieldLocator(lines, "   ", "edge_id", row.edgeId());
      field(lines, "   ", "edge_type", row.edgeType(), false);
      fieldLocator(lines, "   ", "source_id", row.sourceId());
      fieldLocator(lines, "   ", "target_id", row.targetId());
      fieldLocator(lines, "   ", "neighbor_node_id", row.neighborNodeId());
      field(lines, "   ", "neighbor_kind", row.neighborKind(), false);
      field(lines, "   ", "neighbor_label", row.neighborLabel(), true);
      if (row.neighborSourceRefId() != null) {
        fieldLocator(lines, "   ", "neighbor_source_ref.id", row.neighborSourceRefId());
      }
      field(lines, "   ", "claim_category", row.claimCategory(), false);
      field(lines, "   ", "relation_status", row.relationStatus(), false);
      field(lines, "   ", "support_type", row.supportType(), false);
      field(lines, "   ", "confidence", row.confidence(), false);
      field(lines, "   ", "graph_confidence", row.graphConfidence(), false);
      field(lines, "   ", "uncertainty", row.uncertainty(), false);
      field(lines, "   ", "relation_attributes", row.relationAttributes(), true);
      field(lines, "   ", "derivation", row.derivation(), true);
      lines.add("   evidence_ids: " + safeLocators(row.evidenceIds()));
    }
    lines.add("");
  }

  private void appendRelationStatuses(List<String> lines, List<RelationStatusRow> rows) {
    lines.add("Relation statuses");
    if (rows.isEmpty()) {
      lines.add("No relation_status rows.");
      lines.add("");
      return;
    }
    for (int index = 0; index < rows.size(); index++) {
      RelationStatusRow row = rows.get(index);
      lines.add((index + 1) + ". relation_status");
      fieldLocator(lines, "   ", "start_node_id", row.startNodeId());
      field(lines, "   ", "direction", row.direction(), false);
      fieldLocator(lines, "   ", "status_id", row.statusId());
      field(lines, "   ", "relation_family", row.relationFamily(), false);
      fieldLocator(lines, "   ", "source_id", row.sourceId());
      fieldLocator(lines, "   ", "target_id", row.targetId());
      field(lines, "   ", "claim_category", row.claimCategory(), false);
      field(lines, "   ", "relation_status", row.relationStatus(), false);
      field(lines, "   ", "support_type", row.supportType(), false);
      field(lines, "   ", "confidence", row.confidence(), false);
      field(lines, "   ", "status_confidence", row.statusConfidence(), false);
      field(lines, "   ", "uncertainty", row.uncertainty(), false);
      field(lines, "   ", "relation_attributes", row.relationAttributes(), true);
      field(lines, "   ", "derivation", row.derivation(), true);
      lines.add("   evidence_ids: " + safeLocators(row.evidenceIds()));
    }
    lines.add("");
  }

  private void appendPlanningHints(List<String> lines, List<PlanningHintRow> rows) {
    lines.add("Planning hints");
    if (rows.isEmpty()) {
      lines.add("No planning_hint rows.");
      lines.add("");
      return;
    }
    for (int index = 0; index < rows.size(); index++) {
      PlanningHintRow row = rows.get(index);
      lines.add((index + 1) + ". planning_hint");
      fieldLocator(lines, "   ", "hint_id", row.id());
      fieldLocator(lines, "   ", "subject_id", row.subjectId());
      field(lines, "   ", "subject_kind", row.subjectKind(), false);
      field(lines, "   ", "subject_name", row.subjectName(), true);
      field(lines, "   ", "signal", row.signal(), false);
      field(lines, "   ", "status", row.status(), false);
      field(lines, "   ", "risk_basis", row.riskBasis(), false);
      field(lines, "   ", "confidence", row.confidence(), false);
      field(lines, "   ", "source_confidence", row.sourceConfidence(), false);
      field(lines, "   ", "uncertainty", row.uncertainty(), false);
      fieldLocator(lines, "   ", "tied_node_ids", joinIds(row.tiedNodeIds()));
      fieldLocator(lines, "   ", "navigation", row.navigation() + " (not evidence)");
      lines.add("   evidence_ids: " + safeLocators(row.evidenceIds()));
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
    lines.add("- graph_neighbor and relation_status rows are one-hop graph orientation only.");
    lines.add("- planning_hint rows are existing low-confidence quality/change-risk hints only.");
    lines.add("- not_represented means no accepted artifact directly referenced the changed file.");
    lines.add("- no transitive graph traversal, raw diff parsing, Git inspection, source readback, "
        + "scan refresh, generated artifact mutation, impact report write, repository write, "
        + "network access, credential lookup, runtime tracing, call graph, coverage claim, "
        + "correctness claim, vulnerability claim, production-impact claim, "
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

  private void fieldLocator(List<String> lines, String indent, String name, String value) {
    lines.add(indent + name + ": " + safeLocator(value));
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
    String rendered = QueryDisplaySafety.sanitize(value);
    return boundedDisplay(rendered);
  }

  private String safeLocators(List<String> values) {
    if (values.isEmpty()) {
      return "none";
    }
    StringBuilder rendered = new StringBuilder();
    for (String value : values) {
      if (!rendered.isEmpty()) {
        rendered.append(", ");
      }
      rendered.append(safeLocator(value));
      if (rendered.length() > MAX_TEXT_CHARS) {
        break;
      }
    }
    return boundedDisplay(rendered.toString());
  }

  private String safeLocator(String value) {
    if (value == null) {
      return "null";
    }
    String rendered = QueryDisplaySafety.sanitizeLocator(value);
    return boundedDisplay(rendered);
  }

  private String boundedDisplay(String rendered) {
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

  private record ImpactIndex(
      Map<String, List<DirectMatchRow>> matchesByPath,
      GraphIndex graphIndex,
      Map<String, List<PlanningHintMatch>> planningHintsBySubjectId) {
  }

  private record GraphIndex(
      Map<String, JsonNode> nodesById,
      Map<String, List<JsonNode>> edgesByNodeId,
      Map<String, List<JsonNode>> relationStatusesByNodeId) {
  }

  private record ImpactProjection(
      List<GraphNeighborRow> graphNeighbors,
      List<RelationStatusRow> relationStatuses,
      List<PlanningHintRow> planningHints,
      List<DiagnosticRow> diagnostics) {
  }

  private record ProjectionCapResult(
      List<GraphNeighborRow> graphNeighbors,
      List<RelationStatusRow> relationStatuses,
      List<DiagnosticRow> diagnostics) {
  }

  private record ProjectionCandidate(
      String category,
      GraphNeighborRow graphNeighbor,
      RelationStatusRow relationStatus) {
    private static final Comparator<ProjectionCandidate> ORDERING = Comparator
        .comparing(ProjectionCandidate::category)
        .thenComparing(ProjectionCandidate::sortKey);

    static ProjectionCandidate graphNeighbor(GraphNeighborRow row) {
      return new ProjectionCandidate("graph_neighbor", row, null);
    }

    static ProjectionCandidate relationStatus(RelationStatusRow row) {
      return new ProjectionCandidate("relation_status", null, row);
    }

    private String sortKey() {
      if (graphNeighbor != null) {
        return graphNeighbor.sortKey();
      }
      return relationStatus.sortKey();
    }
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

  private record GraphNeighborRow(
      String startNodeId,
      String direction,
      String edgeId,
      String edgeType,
      String sourceId,
      String targetId,
      String neighborNodeId,
      String neighborKind,
      String neighborLabel,
      String neighborSourceRefId,
      String claimCategory,
      String relationStatus,
      String supportType,
      String confidence,
      String graphConfidence,
      String uncertainty,
      String relationAttributes,
      String derivation,
      List<String> evidenceIds) {
    private static final Comparator<GraphNeighborRow> ORDERING = Comparator
        .comparing(GraphNeighborRow::startNodeId)
        .thenComparing(GraphNeighborRow::direction)
        .thenComparing(row -> row.edgeId() == null ? "" : row.edgeId())
        .thenComparing(row -> row.neighborNodeId() == null ? "" : row.neighborNodeId());

    private GraphNeighborRow {
      evidenceIds = List.copyOf(evidenceIds);
    }

    private String sortKey() {
      return startNodeId
          + "\u0000"
          + direction
          + "\u0000"
          + (edgeId == null ? "" : edgeId)
          + "\u0000"
          + (neighborNodeId == null ? "" : neighborNodeId);
    }
  }

  private record RelationStatusRow(
      String startNodeId,
      String direction,
      String statusId,
      String relationFamily,
      String sourceId,
      String targetId,
      String claimCategory,
      String relationStatus,
      String supportType,
      String confidence,
      String statusConfidence,
      String uncertainty,
      String relationAttributes,
      String derivation,
      List<String> evidenceIds) {
    private static final Comparator<RelationStatusRow> ORDERING = Comparator
        .comparing(RelationStatusRow::startNodeId)
        .thenComparing(RelationStatusRow::direction)
        .thenComparing(row -> row.statusId() == null ? "" : row.statusId());

    private RelationStatusRow {
      evidenceIds = List.copyOf(evidenceIds);
    }

    private String sortKey() {
      return startNodeId
          + "\u0000"
          + direction
          + "\u0000"
          + (statusId == null ? "" : statusId);
    }
  }

  private record PlanningHintMatch(
      String id,
      String subjectId,
      String subjectKind,
      String subjectName,
      String signal,
      String status,
      String riskBasis,
      String sourceConfidence,
      String uncertainty,
      String navigation,
      List<String> evidenceIds) {
    private static final Comparator<PlanningHintMatch> ORDERING = Comparator
        .comparing(PlanningHintMatch::subjectId)
        .thenComparing(row -> row.signal() == null ? "" : row.signal())
        .thenComparing(PlanningHintMatch::id);

    private PlanningHintMatch {
      evidenceIds = List.copyOf(evidenceIds);
    }
  }

  private record PlanningHintRow(
      String id,
      String subjectId,
      String subjectKind,
      String subjectName,
      String signal,
      String status,
      String riskBasis,
      String confidence,
      String sourceConfidence,
      String uncertainty,
      String navigation,
      List<String> tiedNodeIds,
      List<String> evidenceIds) {
    private static final Comparator<PlanningHintRow> ORDERING = Comparator
        .comparing(PlanningHintRow::subjectId)
        .thenComparing(row -> row.signal() == null ? "" : row.signal())
        .thenComparing(PlanningHintRow::id);

    private PlanningHintRow {
      tiedNodeIds = List.copyOf(tiedNodeIds);
      evidenceIds = List.copyOf(evidenceIds);
    }

    static PlanningHintRow from(PlanningHintMatch hint, Set<String> tiedNodeIds) {
      return new PlanningHintRow(
          hint.id(),
          hint.subjectId(),
          hint.subjectKind(),
          hint.subjectName(),
          hint.signal(),
          hint.status(),
          hint.riskBasis(),
          "low",
          hint.sourceConfidence(),
          hint.uncertainty(),
          hint.navigation(),
          new ArrayList<>(new TreeSet<>(tiedNodeIds)),
          hint.evidenceIds());
    }
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
