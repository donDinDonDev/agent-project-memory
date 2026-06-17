package io.github.dondindondev.agentprojectmemory.query;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.dondindondev.agentprojectmemory.OutputRedactor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class ProjectMemoryLookupRenderer {
  private static final int MAX_TEXT_CHARS = 4096;
  private static final String SOURCE_REF_ID_FIELD = "source_ref.id";
  private static final Set<String> SYMBOL_FIELDS = Set.of(
      "annotation",
      "annotation_symbol",
      "candidate_reference",
      "class_name",
      "controller_class",
      "declaring_class",
      "field_name",
      "generic_type",
      "handler_method",
      "java_type",
      "method_name",
      "mock_signal",
      "operation_id",
      "request_body_type",
      "response_type",
      "symbol_name",
      "target_class_name",
      "target_name",
      "test_annotation",
      "type_name");
  private static final Set<String> CLASS_SYMBOL_FIELDS = Set.of(
      "class_name",
      "controller_class",
      "declaring_class",
      "target_class_name");

  public LookupResult renderEvidence(ProjectMemoryArtifacts artifacts, String evidenceId) {
    JsonNode evidence = artifacts.evidenceById().get(evidenceId);
    if (evidence == null) {
      return LookupResult.noResult("No evidence record matched the requested id.");
    }

    List<String> lines = header(artifacts, "explain evidence", 1);
    lines.add("1. " + text(evidence.path("id")));
    field(lines, "   ", "source_type", evidence.path("source_type"));
    field(lines, "   ", "path", evidence.path("path"));
    field(lines, "   ", "class_name", evidence.path("class_name"));
    field(lines, "   ", "method_name", evidence.path("method_name"));
    field(lines, "   ", "symbol_name", evidence.path("symbol_name"));
    field(lines, "   ", "line_start", evidence.path("line_start"));
    field(lines, "   ", "line_end", evidence.path("line_end"));
    field(lines, "   ", "excerpt", evidence.path("excerpt"));
    field(lines, "   ", "confidence", evidence.path("confidence"));
    return LookupResult.found(finish(lines));
  }

  public LookupResult renderFind(ProjectMemoryArtifacts artifacts, FindKind kind, String term) {
    List<MatchRow> matches = new ArrayList<>();
    if (kind == FindKind.FACT) {
      collectProjectMapFactMatches(artifacts.projectMap(), "", term, matches);
      collectGraphFactMatches(artifacts, term, matches);
    } else {
      collectProjectMapSymbolMatches(artifacts.projectMap(), "", term, matches);
      collectEvidenceSymbolMatches(artifacts, term, matches);
    }

    if (matches.isEmpty()) {
      return LookupResult.noResult("No exact " + kind.label() + " match found.");
    }

    List<String> lines = header(artifacts, "find " + kind.label(), matches.size());
    for (int index = 0; index < matches.size(); index++) {
      appendMatch(lines, index + 1, matches.get(index));
    }
    return LookupResult.found(finish(lines));
  }

  private void collectProjectMapFactMatches(
      JsonNode node,
      String pointer,
      String term,
      List<MatchRow> matches) {
    if (node == null) {
      return;
    }
    if (node.isObject()) {
      if (matchesText(node.path("id"), term)) {
        matches.add(MatchRow.projectMap(pointer, "id", term, node));
      }
      if (isModuleRow(pointer, node) && matchesText(node.path("module_id"), term)) {
        matches.add(MatchRow.projectMap(pointer, "module_id", term, node));
      }
      node.fields().forEachRemaining(field ->
          collectProjectMapFactMatches(
              field.getValue(),
              childPointer(pointer, field.getKey()),
              term,
              matches));
      return;
    }
    if (node.isArray()) {
      for (int index = 0; index < node.size(); index++) {
        collectProjectMapFactMatches(node.get(index), childPointer(pointer, index), term, matches);
      }
    }
  }

  private boolean isModuleRow(String pointer, JsonNode node) {
    return pointer.startsWith("/project/modules/items/")
        && node.has("module_id")
        && node.has("module_path")
        && node.has("support_status");
  }

  private void collectGraphFactMatches(
      ProjectMemoryArtifacts artifacts,
      String term,
      List<MatchRow> matches) {
    if (!artifacts.hasProjectGraph()) {
      return;
    }
    collectGraphRows(artifacts.projectGraph().path("nodes"), "/nodes", term, matches);
    collectGraphRows(artifacts.projectGraph().path("edges"), "/edges", term, matches);
    collectGraphRows(
        artifacts.projectGraph().path("relation_statuses"),
        "/relation_statuses",
        term,
        matches);
    collectGraphRows(artifacts.projectGraph().path("warnings"), "/warnings", term, matches);
  }

  private void collectGraphRows(
      JsonNode rows,
      String pointer,
      String term,
      List<MatchRow> matches) {
    if (!rows.isArray()) {
      return;
    }
    for (int index = 0; index < rows.size(); index++) {
      JsonNode row = rows.get(index);
      if (matchesText(row.path("id"), term)) {
        matches.add(MatchRow.graph(childPointer(pointer, index), "id", term, row));
      }
      if (matchesText(row.at("/source_ref/id"), term)) {
        matches.add(MatchRow.graph(childPointer(pointer, index), "source_ref.id", term, row));
      }
    }
  }

  private void collectProjectMapSymbolMatches(
      JsonNode node,
      String pointer,
      String term,
      List<MatchRow> matches) {
    if (node == null) {
      return;
    }
    if (node.isObject()) {
      node.fields().forEachRemaining(field -> {
        String fieldName = field.getKey();
        JsonNode value = field.getValue();
        if (SYMBOL_FIELDS.contains(fieldName) && matchesText(value, term)) {
          matches.add(MatchRow.projectMap(pointer, fieldName, term, node));
        }
        if (CLASS_SYMBOL_FIELDS.contains(fieldName)
            && value.isTextual()
            && term.equals(simpleName(value.asText()))) {
          matches.add(MatchRow.projectMap(pointer, fieldName + ".simple_name", term, node));
        }
        collectProjectMapSymbolMatches(
            value,
            childPointer(pointer, fieldName),
            term,
            matches);
      });
      return;
    }
    if (node.isArray()) {
      for (int index = 0; index < node.size(); index++) {
        collectProjectMapSymbolMatches(node.get(index), childPointer(pointer, index), term, matches);
      }
    }
  }

  private void collectEvidenceSymbolMatches(
      ProjectMemoryArtifacts artifacts,
      String term,
      List<MatchRow> matches) {
    for (int index = 0; index < artifacts.evidenceRecords().size(); index++) {
      JsonNode evidence = artifacts.evidenceRecords().get(index);
      if (matchesText(evidence.path("symbol_name"), term)) {
        matches.add(MatchRow.evidence(childPointer("/records", index), "symbol_name", term, evidence));
      }
    }
  }

  private void appendMatch(List<String> lines, int number, MatchRow match) {
    lines.add(number + ". " + title(match.row()));
    lines.add(
        "   navigation: "
            + match.artifact()
            + "#"
            + (match.pointer().isBlank() ? "/" : match.pointer())
            + " (not evidence)");
    field(lines, "   ", "matched_field", match.matchedField());
    field(
        lines,
        "   ",
        "matched_value",
        match.matchedValue(),
        shouldRedactField(match.matchedField()));
    appendCommonFields(lines, match.row());
    if ("project-graph.json".equals(match.artifact())) {
      appendSourceRef(lines, match.row().path("source_ref"));
      appendDerivation(lines, match.row().path("derivation"));
    }
    lines.add("   evidence_ids: " + evidenceIds(match.row()));
  }

  private void appendCommonFields(List<String> lines, JsonNode row) {
    commonField(lines, row, "kind");
    commonField(lines, row, "type");
    commonField(lines, row, "relation_family");
    commonField(lines, row, "claim_category");
    commonField(lines, row, "module_id");
    commonField(lines, row, "api_surface_category");
    commonField(lines, row, "implementation_status");
    commonField(lines, row, "relation_status");
    commonField(lines, row, "entity_relation_status");
    commonField(lines, row, "support_type");
    commonField(lines, row, "confidence");
    commonField(lines, row, "uncertainty");
    commonField(lines, row, "source_type");
    commonField(lines, row, "path");
    commonField(lines, row, "class_name");
    commonField(lines, row, "controller_class");
    commonField(lines, row, "method_name");
    commonField(lines, row, "handler_method");
    commonField(lines, row, "field_name");
    commonField(lines, row, "operation_id");
    commonField(lines, row, "symbol_name");
  }

  private void commonField(List<String> lines, JsonNode row, String fieldName) {
    JsonNode value = row.path(fieldName);
    if (!value.isMissingNode()) {
      field(lines, "   ", fieldName, value);
    }
  }

  private void appendSourceRef(List<String> lines, JsonNode sourceRef) {
    if (!sourceRef.isObject()) {
      return;
    }
    lines.add(
        "   source_ref: artifact="
            + text(sourceRef.path("artifact"))
            + " section="
            + text(sourceRef.path("section"))
            + " id="
            + text(sourceRef.path("id"), SOURCE_REF_ID_FIELD)
            + " (not evidence)");
  }

  private void appendDerivation(List<String> lines, JsonNode derivation) {
    if (!derivation.isObject()) {
      return;
    }
    lines.add(
        "   derivation: kind="
            + text(derivation.path("kind"))
            + " artifact="
            + text(derivation.path("artifact"))
            + " section="
            + text(derivation.path("section"))
            + " (not evidence)");
  }

  private List<String> header(ProjectMemoryArtifacts artifacts, String query, int resultCount) {
    List<String> lines = new ArrayList<>();
    lines.add("Query: " + query);
    lines.add(
        "Source artifacts: project-map.json schema_version="
            + safe(artifacts.projectMapSchemaVersion())
            + ", evidence-index.jsonl records="
            + artifacts.evidenceRecords().size());
    if (artifacts.hasProjectGraph()) {
      lines.add(
          "Optional graph: project-graph.json graph_schema_version="
              + safe(artifacts.projectGraphSchemaVersion()));
    }
    lines.add("Results: " + resultCount);
    if (resultCount > 0) {
      lines.add("");
    }
    return lines;
  }

  private boolean matchesText(JsonNode node, String term) {
    return node != null && node.isTextual() && node.asText().equals(term);
  }

  private String evidenceIds(JsonNode node) {
    List<String> values = new ArrayList<>();
    addEvidenceIds(values, node.path("evidence_ids"));
    addEvidenceIds(values, node.path("declaration_evidence_ids"));
    addEvidenceIds(values, node.path("pom_evidence_ids"));
    return values.isEmpty() ? "none" : String.join(", ", values);
  }

  private void addEvidenceIds(List<String> values, JsonNode node) {
    if (!node.isArray()) {
      return;
    }
    for (JsonNode value : node) {
      values.add(text(value));
    }
  }

  private void field(List<String> lines, String indent, String name, String value) {
    field(lines, indent, name, value, shouldRedactField(name));
  }

  private void field(List<String> lines, String indent, String name, JsonNode value) {
    lines.add(indent + name + ": " + text(value, name));
  }

  private void field(
      List<String> lines,
      String indent,
      String name,
      String value,
      boolean redact) {
    lines.add(indent + name + ": " + safe(value, redact));
  }

  private String title(JsonNode row) {
    String id = rawText(row.path("id"));
    if (id != null) {
      return safe(id);
    }
    String moduleId = rawText(row.path("module_id"));
    if (moduleId != null) {
      return safe(moduleId);
    }
    String className = rawText(row.path("class_name"));
    if (className != null) {
      return safe(className);
    }
    String symbolName = rawText(row.path("symbol_name"));
    if (symbolName != null) {
      return safe(symbolName);
    }
    return "matched row";
  }

  private String rawText(JsonNode node) {
    return node != null && node.isTextual() ? node.asText() : null;
  }

  private String text(JsonNode node) {
    if (node == null || node.isMissingNode() || node.isNull()) {
      return "null";
    }
    if (node.isTextual()) {
      return safe(node.asText());
    }
    if (node.isNumber() || node.isBoolean()) {
      return node.asText();
    }
    return safe(node.toString());
  }

  private String text(JsonNode node, String fieldName) {
    if (node == null || node.isMissingNode() || node.isNull()) {
      return "null";
    }
    if (node.isTextual()) {
      return safe(node.asText(), shouldRedactField(fieldName));
    }
    if (node.isNumber() || node.isBoolean()) {
      return node.asText();
    }
    return safe(node.toString(), shouldRedactField(fieldName));
  }

  private String finish(List<String> lines) {
    return String.join("\n", lines) + "\n";
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

  private String simpleName(String value) {
    int dot = value.lastIndexOf('.');
    int hash = value.lastIndexOf('#');
    int separator = Math.max(dot, hash);
    return separator >= 0 && separator + 1 < value.length()
        ? value.substring(separator + 1)
        : value;
  }

  private String safe(String value) {
    return safe(value, false);
  }

  private String safe(String value, boolean redact) {
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

  private boolean shouldRedactField(String fieldName) {
    return SOURCE_REF_ID_FIELD.equals(fieldName)
        || OutputRedactor.shouldRedactFreeTextField(fieldName)
        || OutputRedactor.isCredentialKey(fieldName);
  }

  public enum FindKind {
    FACT("fact"),
    SYMBOL("symbol");

    private final String label;

    FindKind(String label) {
      this.label = label;
    }

    String label() {
      return label;
    }
  }

  public record LookupResult(boolean found, String output, String noResultMessage) {
    static LookupResult found(String output) {
      return new LookupResult(true, output, null);
    }

    static LookupResult noResult(String message) {
      return new LookupResult(false, "", message);
    }
  }

  private record MatchRow(
      String artifact,
      String pointer,
      String matchedField,
      String matchedValue,
      JsonNode row) {
    static MatchRow projectMap(String pointer, String matchedField, String matchedValue, JsonNode row) {
      return new MatchRow("project-map.json", pointer, matchedField, matchedValue, row);
    }

    static MatchRow graph(String pointer, String matchedField, String matchedValue, JsonNode row) {
      return new MatchRow("project-graph.json", pointer, matchedField, matchedValue, row);
    }

    static MatchRow evidence(String pointer, String matchedField, String matchedValue, JsonNode row) {
      return new MatchRow("evidence-index.jsonl", pointer, matchedField, matchedValue, row);
    }
  }
}
