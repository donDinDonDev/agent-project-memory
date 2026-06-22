package io.github.dondindondev.agentprojectmemory.query;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.dondindondev.agentprojectmemory.OutputRedactor;
import java.util.ArrayList;
import java.util.List;

public final class ProjectMemoryAgentContextRenderer {
  private static final int MAX_TEXT_CHARS = 4096;
  private static final int MAX_EVIDENCE_ID_SAMPLE = 5;

  public String render(ProjectMemoryArtifacts artifacts) {
    List<String> lines = new ArrayList<>();
    appendHeader(lines, artifacts);
    appendReadingOrder(lines, artifacts);
    appendFollowUpCommands(lines, artifacts);
    appendOrientation(lines, artifacts);
    appendGraph(lines, artifacts);
    appendBoundaries(lines);
    return String.join("\n", lines) + "\n";
  }

  private void appendHeader(List<String> lines, ProjectMemoryArtifacts artifacts) {
    lines.add("Query: agent-context");
    lines.add(
        "Source artifacts: project-map.json schema_version="
            + safe(artifacts.projectMapSchemaVersion())
            + ", evidence-index.jsonl records="
            + artifacts.evidenceRecords().size());
    if (artifacts.hasProjectGraph()) {
      lines.add(
          "Optional graph: project-graph.json graph_schema_version="
              + safe(artifacts.projectGraphSchemaVersion())
              + " (navigation metadata, not evidence)");
    } else {
      lines.add("Optional graph: not loaded");
    }
    lines.add("Result: deterministic stdout agent context (navigation only, not evidence)");
    lines.add("");
  }

  private void appendReadingOrder(List<String> lines, ProjectMemoryArtifacts artifacts) {
    lines.add("Artifact reading order");
    lines.add("1. project-map.json: generated facts for this no-adapter query surface.");
    lines.add(
        "2. evidence-index.jsonl: existing evidence records; inspect exact records with "
            + "`query <path> explain evidence <evidence-id>`.");
    if (artifacts.hasProjectGraph()) {
      lines.add(
          "3. project-graph.json: optional one-hop navigation metadata; graph derivation "
              + "remains non-evidence.");
    } else {
      lines.add("3. project-graph.json: optional; absent or not requested for source facts.");
    }
    lines.add("");
  }

  private void appendFollowUpCommands(List<String> lines, ProjectMemoryArtifacts artifacts) {
    lines.add("Supported follow-up query commands");
    lines.add("- query <path> list modules");
    lines.add("- query <path> list endpoints");
    lines.add("- query <path> list api-operations");
    lines.add("- query <path> list entities");
    lines.add("- query <path> list tests");
    lines.add("- query <path> explain evidence <evidence-id>");
    lines.add("- query <path> find fact <term>");
    lines.add("- query <path> find symbol <term>");
    lines.add("- query <path> relations <id> [--direction incoming|outgoing|both]"
        + (artifacts.hasProjectGraph() ? "" : " (requires valid project-graph.json)"));
    lines.add("");
  }

  private void appendOrientation(List<String> lines, ProjectMemoryArtifacts artifacts) {
    List<JsonNode> modules = arrayAt(artifacts.projectMap(), "/project/modules/items");
    List<JsonNode> endpoints = arrayAt(artifacts.projectMap(), "/endpoints");
    List<JsonNode> operations = arrayAt(
        artifacts.projectMap(),
        "/api_surface/openapi/operations/items");
    List<JsonNode> entities = arrayAt(artifacts.projectMap(), "/entities/items");
    List<JsonNode> embeddables = arrayAt(artifacts.projectMap(), "/entities/embeddables/items");
    List<JsonNode> repositoryRelations = repositoryRelationRows(artifacts.projectMap());
    List<JsonNode> tests = arrayAt(artifacts.projectMap(), "/tests/items");

    lines.add("Project orientation");
    lines.add("- modules: " + modules.size()
        + firstRow(modules, "module_id", "declaration_evidence_ids", "pom_evidence_ids"));
    lines.add("- source-visible endpoints: " + endpoints.size()
        + firstRow(endpoints, "id", "evidence_ids"));
    lines.add("- spec-backed API operations: " + operations.size()
        + firstRow(operations, "id", "evidence_ids"));
    lines.add("- JPA entities: " + entities.size()
        + firstRow(entities, "id", "evidence_ids"));
    lines.add("- JPA embeddables: " + embeddables.size()
        + firstRow(embeddables, "id", "evidence_ids"));
    lines.add("- repository/entity relation rows: " + repositoryRelations.size()
        + firstRow(repositoryRelations, "id", "evidence_ids"));
    lines.add("- tests: " + tests.size()
        + firstRow(tests, "id", "evidence_ids"));
    lines.add("- evidence records: " + artifacts.evidenceRecords().size()
        + evidenceSample(artifacts.evidenceRecords()));
    lines.add("");
  }

  private void appendGraph(List<String> lines, ProjectMemoryArtifacts artifacts) {
    lines.add("Graph navigation");
    if (!artifacts.hasProjectGraph()) {
      lines.add("- project-graph.json: not loaded; relation navigation is unavailable.");
      lines.add("- graph derivation, when present in generated artifacts, remains non-evidence.");
      lines.add("");
      return;
    }

    JsonNode graph = artifacts.projectGraph();
    lines.add("- nodes: " + sizeOfArray(graph.path("nodes")));
    lines.add("- edges: " + sizeOfArray(graph.path("edges")));
    lines.add("- relation_statuses: " + sizeOfArray(graph.path("relation_statuses")));
    lines.add("- warnings: " + sizeOfArray(graph.path("warnings")));
    lines.add("- source_ref and derivation fields are navigation metadata, not evidence.");
    lines.add("- relation_status rows remain separate from graph edges.");
    lines.add("");
  }

  private void appendBoundaries(List<String> lines) {
    lines.add("Authority and boundaries");
    lines.add("- agent-context output is navigation and presentation only; it is not evidence.");
    lines.add("- existing evidence IDs are preserved; no evidence records or evidence IDs are created.");
    lines.add("- graph derivation, query output, generated Markdown, profiles, cache metadata, "
        + "adapter diagnostics, AI presentation, downstream agent output, and LLM output remain non-evidence.");
    lines.add("- source-registry.json, adapter context rows, connector records, generated Markdown bodies, "
        + "agent-profiles, ai-presentations, and cache/v1 are not fact input sources.");
    lines.add("- AI presentation artifacts, if present, are optional non-authoritative/non-evidence "
        + "presentation and are not parsed by this command.");
    lines.add("- no source readback, scan execution, generated artifact creation, repository write, "
        + "network access, credential lookup, server/API runtime, or automatic code modification.");
  }

  private List<JsonNode> repositoryRelationRows(JsonNode projectMap) {
    List<JsonNode> rows = new ArrayList<>();
    for (JsonNode repository : arrayAt(projectMap, "/spring_application_surface/repositories/items")) {
      if (repository.has("entity_relation_status")) {
        rows.add(repository);
      }
    }
    return rows;
  }

  private String firstRow(List<JsonNode> rows, String idField, String... evidenceFields) {
    if (rows.isEmpty()) {
      return "";
    }
    JsonNode first = rows.get(0);
    return " (first: "
        + text(first.path(idField), idField)
        + "; evidence_ids: "
        + evidenceIds(first, evidenceFields)
        + ")";
  }

  private String evidenceSample(List<JsonNode> evidenceRecords) {
    if (evidenceRecords.isEmpty()) {
      return "";
    }
    List<String> ids = new ArrayList<>();
    int limit = Math.min(evidenceRecords.size(), MAX_EVIDENCE_ID_SAMPLE);
    for (int index = 0; index < limit; index++) {
      ids.add(text(evidenceRecords.get(index).path("id"), "id"));
    }
    if (evidenceRecords.size() > limit) {
      ids.add("...");
    }
    return " (ids: " + String.join(", ", ids) + ")";
  }

  private String evidenceIds(JsonNode node, String... fieldNames) {
    List<String> ids = new ArrayList<>();
    for (String fieldName : fieldNames) {
      JsonNode field = node.path(fieldName);
      if (!field.isArray()) {
        continue;
      }
      for (JsonNode evidenceId : field) {
        ids.add(text(evidenceId, "id"));
      }
    }
    return ids.isEmpty() ? "none" : String.join(", ", ids);
  }

  private List<JsonNode> arrayAt(JsonNode root, String pointer) {
    return iterableArray(root.at(pointer));
  }

  private List<JsonNode> iterableArray(JsonNode node) {
    if (node == null || !node.isArray()) {
      return List.of();
    }
    List<JsonNode> values = new ArrayList<>();
    for (JsonNode item : node) {
      values.add(item);
    }
    return values;
  }

  private int sizeOfArray(JsonNode node) {
    return node != null && node.isArray() ? node.size() : 0;
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

  private String safe(String value) {
    return safe(value, false);
  }

  private String safe(String value, boolean redact) {
    String rendered = QueryDisplaySafety.sanitize(value);
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
    return OutputRedactor.shouldRedactFreeTextField(fieldName)
        || OutputRedactor.isCredentialKey(fieldName);
  }
}
