package io.github.dondindondev.agentprojectmemory.query;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.dondindondev.agentprojectmemory.OutputRedactor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ProjectMemoryRelationRenderer {
  private static final int MAX_TEXT_CHARS = 4096;
  private static final String SOURCE_REF_ID_FIELD = "source_ref.id";

  public ProjectMemoryLookupRenderer.LookupResult render(
      ProjectMemoryArtifacts artifacts,
      String subjectId,
      Direction direction) {
    JsonNode selectedNode = selectedNode(artifacts.projectGraph().path("nodes"), subjectId);
    if (selectedNode == null) {
      return ProjectMemoryLookupRenderer.LookupResult.noResult(
          "No graph node matched the requested id.");
    }

    String selectedNodeId = rawText(selectedNode.path("id"));
    List<JsonNode> edges = matchingRows(
        artifacts.projectGraph().path("edges"),
        selectedNodeId,
        direction);
    List<JsonNode> statuses = matchingRows(
        artifacts.projectGraph().path("relation_statuses"),
        selectedNodeId,
        direction);

    List<String> lines = header(
        artifacts,
        subjectId,
        selectedNode,
        direction,
        edges.size() + statuses.size());
    appendNode(lines, selectedNode);
    appendEdges(lines, selectedNodeId, edges);
    appendStatuses(lines, selectedNodeId, statuses);
    return ProjectMemoryLookupRenderer.LookupResult.found(finish(lines));
  }

  private JsonNode selectedNode(JsonNode nodes, String subjectId) {
    if (!nodes.isArray()) {
      return null;
    }
    for (JsonNode node : nodes) {
      if (subjectId.equals(rawText(node.path("id")))) {
        return node;
      }
    }
    for (JsonNode node : nodes) {
      if (subjectId.equals(rawText(node.at("/source_ref/id")))) {
        return node;
      }
    }
    return null;
  }

  private List<JsonNode> matchingRows(JsonNode rows, String selectedNodeId, Direction direction) {
    if (!rows.isArray()) {
      return List.of();
    }
    List<JsonNode> result = new ArrayList<>();
    for (JsonNode row : rows) {
      if (matchesDirection(row, selectedNodeId, direction)) {
        result.add(row);
      }
    }
    return result;
  }

  private boolean matchesDirection(JsonNode row, String selectedNodeId, Direction direction) {
    boolean outgoing = selectedNodeId.equals(rawText(row.path("source_id")));
    boolean incoming = selectedNodeId.equals(rawText(row.path("target_id")));
    return switch (direction) {
      case INCOMING -> incoming;
      case OUTGOING -> outgoing;
      case BOTH -> incoming || outgoing;
    };
  }

  private List<String> header(
      ProjectMemoryArtifacts artifacts,
      String subjectId,
      JsonNode selectedNode,
      Direction direction,
      int resultCount) {
    List<String> lines = new ArrayList<>();
    lines.add("Query: relations");
    lines.add(
        "Source artifacts: project-map.json schema_version="
            + safe(artifacts.projectMapSchemaVersion())
            + ", evidence-index.jsonl records="
            + artifacts.evidenceRecords().size()
            + ", project-graph.json graph_schema_version="
            + safe(artifacts.projectGraphSchemaVersion()));
    lines.add("Subject: " + safe(subjectId, resolvedBySourceRefId(selectedNode, subjectId)));
    lines.add("Resolved node: " + text(selectedNode.path("id")));
    lines.add("Resolved by: " + resolvedBy(selectedNode, subjectId));
    lines.add("Direction: " + direction.label());
    lines.add("Results: " + resultCount);
    lines.add("");
    return lines;
  }

  private String resolvedBy(JsonNode node, String subjectId) {
    return resolvedBySourceRefId(node, subjectId) ? "source_ref.id" : "node id";
  }

  private boolean resolvedBySourceRefId(JsonNode node, String subjectId) {
    return !subjectId.equals(rawText(node.path("id")))
        && subjectId.equals(rawText(node.at("/source_ref/id")));
  }

  private void appendNode(List<String> lines, JsonNode node) {
    lines.add("Node");
    field(lines, "   ", "id", node.path("id"));
    field(lines, "   ", "kind", node.path("kind"));
    field(lines, "   ", "label", node.path("label"));
    field(lines, "   ", "claim_category", node.path("claim_category"));
    field(lines, "   ", "module_id", node.path("module_id"));
    appendSourceRef(lines, "   ", node.path("source_ref"));
    lines.add("   evidence_ids: " + evidenceIds(node));
  }

  private void appendEdges(List<String> lines, String selectedNodeId, List<JsonNode> edges) {
    lines.add("");
    lines.add("Edges: " + edges.size());
    if (edges.isEmpty()) {
      lines.add("No relation edges found.");
      return;
    }
    for (int index = 0; index < edges.size(); index++) {
      JsonNode edge = edges.get(index);
      lines.add((index + 1) + ". " + text(edge.path("id")));
      field(lines, "   ", "direction", relationDirection(edge, selectedNodeId));
      field(lines, "   ", "type", edge.path("type"));
      field(lines, "   ", "source_id", edge.path("source_id"));
      field(lines, "   ", "target_id", edge.path("target_id"));
      appendRelationFields(lines, edge);
    }
  }

  private void appendStatuses(List<String> lines, String selectedNodeId, List<JsonNode> statuses) {
    lines.add("");
    lines.add("Relation statuses: " + statuses.size());
    if (statuses.isEmpty()) {
      lines.add("No relation status rows found.");
      return;
    }
    for (int index = 0; index < statuses.size(); index++) {
      JsonNode status = statuses.get(index);
      lines.add((index + 1) + ". " + text(status.path("id")));
      field(lines, "   ", "direction", relationDirection(status, selectedNodeId));
      field(lines, "   ", "relation_family", status.path("relation_family"));
      field(lines, "   ", "source_id", status.path("source_id"));
      field(lines, "   ", "target_id", status.path("target_id"));
      appendRelationFields(lines, status);
    }
  }

  private void appendRelationFields(List<String> lines, JsonNode row) {
    field(lines, "   ", "claim_category", row.path("claim_category"));
    field(lines, "   ", "relation_status", row.path("relation_status"));
    field(lines, "   ", "support_type", row.path("support_type"));
    field(lines, "   ", "confidence", row.path("confidence"));
    field(lines, "   ", "uncertainty", row.path("uncertainty"));
    lines.add("   relation_attributes: " + attributes(row.path("relation_attributes")));
    appendDerivation(lines, "   ", row.path("derivation"));
    lines.add("   evidence_ids: " + evidenceIds(row));
  }

  private String relationDirection(JsonNode row, String selectedNodeId) {
    boolean outgoing = selectedNodeId.equals(rawText(row.path("source_id")));
    boolean incoming = selectedNodeId.equals(rawText(row.path("target_id")));
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

  private void appendSourceRef(List<String> lines, String indent, JsonNode sourceRef) {
    if (!sourceRef.isObject()) {
      lines.add(indent + "source_ref: null (not evidence)");
      return;
    }
    lines.add(
        indent
            + "source_ref: artifact="
            + text(sourceRef.path("artifact"))
            + " section="
            + text(sourceRef.path("section"))
            + " id="
            + text(sourceRef.path("id"), SOURCE_REF_ID_FIELD)
            + " (not evidence)");
  }

  private void appendDerivation(List<String> lines, String indent, JsonNode derivation) {
    if (!derivation.isObject()) {
      lines.add(indent + "derivation: null (not evidence)");
      return;
    }
    lines.add(
        indent
            + "derivation: kind="
            + text(derivation.path("kind"))
            + " artifact="
            + text(derivation.path("artifact"))
            + " section="
            + text(derivation.path("section"))
            + " fields="
            + arrayText(derivation.path("fields"))
            + " (not evidence)");
  }

  private String attributes(JsonNode attributes) {
    if (!attributes.isObject()) {
      return "{}";
    }
    List<String> values = new ArrayList<>();
    Iterator<Map.Entry<String, JsonNode>> fields = attributes.fields();
    while (fields.hasNext()) {
      Map.Entry<String, JsonNode> field = fields.next();
      values.add(safe(field.getKey()) + "=" + mapValueText(field.getKey(), field.getValue()));
    }
    return values.isEmpty() ? "{}" : String.join(", ", values);
  }

  private String arrayText(JsonNode node) {
    if (!node.isArray()) {
      return "null";
    }
    List<String> values = new ArrayList<>();
    for (JsonNode value : node) {
      values.add(text(value));
    }
    return values.isEmpty() ? "none" : String.join(", ", values);
  }

  private String evidenceIds(JsonNode node) {
    JsonNode values = node.path("evidence_ids");
    if (!values.isArray() || values.isEmpty()) {
      return "none";
    }
    List<String> ids = new ArrayList<>();
    for (JsonNode value : values) {
      ids.add(text(value));
    }
    return String.join(", ", ids);
  }

  private void field(List<String> lines, String indent, String name, JsonNode value) {
    lines.add(indent + name + ": " + text(value, name));
  }

  private void field(List<String> lines, String indent, String name, String value) {
    lines.add(indent + name + ": " + safe(value, shouldRedactField(name)));
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

  private String mapValueText(String key, JsonNode node) {
    if (node == null || node.isMissingNode() || node.isNull()) {
      return "null";
    }
    String raw = node.isTextual() || node.isNumber() || node.isBoolean()
        ? node.asText()
        : node.toString();
    return safe(OutputRedactor.redactMapValue(key, raw));
  }

  private String finish(List<String> lines) {
    return String.join("\n", lines) + "\n";
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
    return SOURCE_REF_ID_FIELD.equals(fieldName)
        || OutputRedactor.shouldRedactFreeTextField(fieldName)
        || OutputRedactor.isCredentialKey(fieldName);
  }

  public enum Direction {
    INCOMING("incoming"),
    OUTGOING("outgoing"),
    BOTH("both");

    private final String label;

    Direction(String label) {
      this.label = label;
    }

    public String label() {
      return label;
    }

    public static Optional<Direction> fromCliValue(String value) {
      for (Direction direction : values()) {
        if (direction.label.equals(value)) {
          return Optional.of(direction);
        }
      }
      return Optional.empty();
    }
  }
}
