package io.github.dondindondev.agentprojectmemory.graph;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ProjectGraphJsonSerializer {
  public String serialize(ProjectGraph graph) {
    StringBuilder json = new StringBuilder();
    json.append("{\n");
    appendStringField(json, 1, "graph_schema_version", graph.graphSchemaVersion(), true);
    appendStringField(json, 1, "project_map_schema_version", graph.projectMapSchemaVersion(), true);
    appendStringField(json, 1, "graph_kind", graph.graphKind(), true);
    appendStringArrayField(json, 1, "source_artifacts", graph.sourceArtifacts(), true);
    appendLimits(json, graph.limits(), true);
    appendNodes(json, graph.nodes(), true);
    appendEdges(json, graph.edges(), true);
    appendRelationStatuses(json, graph.relationStatuses(), true);
    appendWarnings(json, graph.warnings(), false);
    json.append("}\n");
    return json.toString();
  }

  private void appendLimits(StringBuilder json, ProjectGraphLimits limits, boolean trailingComma) {
    indent(json, 1);
    json.append("\"limits\": {\n");
    appendIntegerField(json, 2, "max_nodes", limits.maxNodes(), true);
    appendIntegerField(json, 2, "max_edges", limits.maxEdges(), true);
    appendIntegerField(json, 2, "max_relation_statuses", limits.maxRelationStatuses(), false);
    indent(json, 1);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendNodes(StringBuilder json, List<GraphNode> nodes, boolean trailingComma) {
    indent(json, 1);
    json.append("\"nodes\": [");
    if (nodes.isEmpty()) {
      json.append("]");
      appendLineEnding(json, trailingComma);
      return;
    }
    json.append("\n");
    for (int index = 0; index < nodes.size(); index++) {
      appendNode(json, nodes.get(index), index < nodes.size() - 1);
    }
    indent(json, 1);
    json.append("]");
    appendLineEnding(json, trailingComma);
  }

  private void appendNode(StringBuilder json, GraphNode node, boolean trailingComma) {
    indent(json, 2);
    json.append("{\n");
    appendStringField(json, 3, "id", node.id(), true);
    appendStringField(json, 3, "kind", node.kind(), true);
    appendStringField(json, 3, "label", node.label(), true);
    appendStringField(json, 3, "claim_category", node.claimCategory(), true);
    appendNullableStringField(json, 3, "module_id", node.moduleId(), true);
    appendSourceRefField(json, 3, "source_ref", node.sourceRef(), true);
    appendStringArrayField(json, 3, "evidence_ids", node.evidenceIds(), false);
    indent(json, 2);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendEdges(StringBuilder json, List<GraphEdge> edges, boolean trailingComma) {
    indent(json, 1);
    json.append("\"edges\": [");
    if (edges.isEmpty()) {
      json.append("]");
      appendLineEnding(json, trailingComma);
      return;
    }
    json.append("\n");
    for (int index = 0; index < edges.size(); index++) {
      appendEdge(json, edges.get(index), index < edges.size() - 1);
    }
    indent(json, 1);
    json.append("]");
    appendLineEnding(json, trailingComma);
  }

  private void appendEdge(StringBuilder json, GraphEdge edge, boolean trailingComma) {
    indent(json, 2);
    json.append("{\n");
    appendStringField(json, 3, "id", edge.id(), true);
    appendStringField(json, 3, "type", edge.type(), true);
    appendStringField(json, 3, "source_id", edge.sourceId(), true);
    appendStringField(json, 3, "target_id", edge.targetId(), true);
    appendStringField(json, 3, "claim_category", edge.claimCategory(), true);
    appendStringField(json, 3, "relation_status", edge.relationStatus(), true);
    appendStringField(json, 3, "support_type", edge.supportType(), true);
    appendStringField(json, 3, "confidence", edge.confidence(), true);
    appendNullableStringField(json, 3, "uncertainty", edge.uncertainty(), true);
    appendStringMapField(json, 3, "relation_attributes", edge.relationAttributes(), true);
    appendDerivationField(json, 3, "derivation", edge.derivation(), true);
    appendStringArrayField(json, 3, "evidence_ids", edge.evidenceIds(), false);
    indent(json, 2);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendRelationStatuses(
      StringBuilder json,
      List<GraphRelationStatus> statuses,
      boolean trailingComma) {
    indent(json, 1);
    json.append("\"relation_statuses\": [");
    if (statuses.isEmpty()) {
      json.append("]");
      appendLineEnding(json, trailingComma);
      return;
    }
    json.append("\n");
    for (int index = 0; index < statuses.size(); index++) {
      appendRelationStatus(json, statuses.get(index), index < statuses.size() - 1);
    }
    indent(json, 1);
    json.append("]");
    appendLineEnding(json, trailingComma);
  }

  private void appendRelationStatus(
      StringBuilder json,
      GraphRelationStatus status,
      boolean trailingComma) {
    indent(json, 2);
    json.append("{\n");
    appendStringField(json, 3, "id", status.id(), true);
    appendStringField(json, 3, "relation_family", status.relationFamily(), true);
    appendNullableStringField(json, 3, "source_id", status.sourceId(), true);
    appendNullableStringField(json, 3, "target_id", status.targetId(), true);
    appendStringField(json, 3, "relation_status", status.relationStatus(), true);
    appendStringField(json, 3, "support_type", status.supportType(), true);
    appendStringField(json, 3, "confidence", status.confidence(), true);
    appendNullableStringField(json, 3, "uncertainty", status.uncertainty(), true);
    appendStringMapField(json, 3, "relation_attributes", status.relationAttributes(), true);
    appendDerivationField(json, 3, "derivation", status.derivation(), true);
    appendStringArrayField(json, 3, "evidence_ids", status.evidenceIds(), false);
    indent(json, 2);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendWarnings(StringBuilder json, List<GraphWarning> warnings, boolean trailingComma) {
    indent(json, 1);
    json.append("\"warnings\": [");
    if (warnings.isEmpty()) {
      json.append("]");
      appendLineEnding(json, trailingComma);
      return;
    }
    json.append("\n");
    for (int index = 0; index < warnings.size(); index++) {
      appendWarning(json, warnings.get(index), index < warnings.size() - 1);
    }
    indent(json, 1);
    json.append("]");
    appendLineEnding(json, trailingComma);
  }

  private void appendWarning(StringBuilder json, GraphWarning warning, boolean trailingComma) {
    indent(json, 2);
    json.append("{\n");
    appendStringField(json, 3, "id", warning.id(), true);
    appendStringField(json, 3, "category", warning.category(), true);
    appendStringField(json, 3, "severity", warning.severity(), true);
    appendStringField(json, 3, "message", warning.message(), true);
    appendSourceRefField(json, 3, "source_ref", warning.sourceRef(), true);
    appendDerivationField(json, 3, "derivation", warning.derivation(), true);
    appendStringArrayField(json, 3, "evidence_ids", warning.evidenceIds(), false);
    indent(json, 2);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendSourceRefField(
      StringBuilder json,
      int level,
      String fieldName,
      GraphSourceRef sourceRef,
      boolean trailingComma) {
    indent(json, level);
    json.append('"').append(fieldName).append("\": ");
    if (sourceRef == null) {
      json.append("null");
      appendLineEnding(json, trailingComma);
      return;
    }
    json.append("{\n");
    appendStringField(json, level + 1, "artifact", sourceRef.artifact(), true);
    appendStringField(json, level + 1, "section", sourceRef.section(), true);
    appendNullableStringField(json, level + 1, "id", sourceRef.id(), false);
    indent(json, level);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendDerivationField(
      StringBuilder json,
      int level,
      String fieldName,
      GraphDerivation derivation,
      boolean trailingComma) {
    indent(json, level);
    json.append('"').append(fieldName).append("\": ");
    if (derivation == null) {
      json.append("null");
      appendLineEnding(json, trailingComma);
      return;
    }
    json.append("{\n");
    appendStringField(json, level + 1, "kind", derivation.kind(), true);
    appendStringField(json, level + 1, "artifact", derivation.artifact(), true);
    appendStringField(json, level + 1, "section", derivation.section(), derivation.fields() != null);
    if (derivation.fields() != null) {
      appendStringArrayField(json, level + 1, "fields", derivation.fields(), false);
    }
    indent(json, level);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendStringField(
      StringBuilder json,
      int level,
      String fieldName,
      String value,
      boolean trailingComma) {
    indent(json, level);
    json.append('"').append(fieldName).append("\": \"")
        .append(jsonString(value))
        .append('"');
    appendLineEnding(json, trailingComma);
  }

  private void appendNullableStringField(
      StringBuilder json,
      int level,
      String fieldName,
      String value,
      boolean trailingComma) {
    indent(json, level);
    json.append('"').append(fieldName).append("\": ");
    if (value == null) {
      json.append("null");
    } else {
      json.append('"').append(jsonString(value)).append('"');
    }
    appendLineEnding(json, trailingComma);
  }

  private void appendIntegerField(
      StringBuilder json,
      int level,
      String fieldName,
      int value,
      boolean trailingComma) {
    indent(json, level);
    json.append('"').append(fieldName).append("\": ").append(value);
    appendLineEnding(json, trailingComma);
  }

  private void appendStringArrayField(
      StringBuilder json,
      int level,
      String fieldName,
      List<String> values,
      boolean trailingComma) {
    indent(json, level);
    json.append('"').append(fieldName).append("\": [");
    if (values.isEmpty()) {
      json.append("]");
      appendLineEnding(json, trailingComma);
      return;
    }
    json.append("\n");
    for (int index = 0; index < values.size(); index++) {
      indent(json, level + 1);
      json.append('"').append(jsonString(values.get(index))).append('"');
      if (index < values.size() - 1) {
        json.append(",");
      }
      json.append("\n");
    }
    indent(json, level);
    json.append("]");
    appendLineEnding(json, trailingComma);
  }

  private void appendStringMapField(
      StringBuilder json,
      int level,
      String fieldName,
      Map<String, String> values,
      boolean trailingComma) {
    indent(json, level);
    json.append('"').append(fieldName).append("\": {");
    if (values.isEmpty()) {
      json.append("}");
      appendLineEnding(json, trailingComma);
      return;
    }
    json.append("\n");
    List<String> keys = values.keySet().stream().sorted().toList();
    for (int index = 0; index < keys.size(); index++) {
      String key = keys.get(index);
      appendStringField(json, level + 1, key, values.get(key), index < keys.size() - 1);
    }
    indent(json, level);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendLineEnding(StringBuilder json, boolean trailingComma) {
    if (trailingComma) {
      json.append(",");
    }
    json.append("\n");
  }

  private void indent(StringBuilder json, int level) {
    json.append("  ".repeat(level));
  }

  private String jsonString(String value) {
    StringBuilder escaped = new StringBuilder();
    for (int offset = 0; offset < value.length();) {
      int codePoint = value.codePointAt(offset);
      offset += Character.charCount(codePoint);
      switch (codePoint) {
        case '"' -> escaped.append("\\\"");
        case '\\' -> escaped.append("\\\\");
        case '\b' -> escaped.append("\\b");
        case '\f' -> escaped.append("\\f");
        case '\n' -> escaped.append("\\n");
        case '\r' -> escaped.append("\\r");
        case '\t' -> escaped.append("\\t");
        default -> {
          if (codePoint < 0x20 || codePoint == 0x2028 || codePoint == 0x2029) {
            escaped.append(String.format(Locale.ROOT, "\\u%04x", codePoint));
          } else {
            escaped.appendCodePoint(codePoint);
          }
        }
      }
    }
    return escaped.toString();
  }
}
