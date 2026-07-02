package io.github.dondindondev.agentprojectmemory.query;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;

final class QueryVerificationText {
  private static final String NAVIGATION_HINT =
      "Verification: query output is navigation/presentation, not evidence. "
          + "Resolve displayed evidence IDs with `query <path> explain evidence <evidence-id>` "
          + "and read back cited source locations for important claims.";

  private QueryVerificationText() {
  }

  static String navigationHint() {
    return NAVIGATION_HINT;
  }

  static boolean hasAnyEvidenceIds(List<JsonNode> rows) {
    for (JsonNode row : rows) {
      if (hasEvidenceIds(row)) {
        return true;
      }
    }
    return false;
  }

  static boolean hasEvidenceIds(JsonNode node) {
    if (node == null || node.isMissingNode() || node.isNull()) {
      return false;
    }
    if (node.isObject()) {
      var fields = node.fields();
      while (fields.hasNext()) {
        Map.Entry<String, JsonNode> field = fields.next();
        if (isEvidenceIdField(field.getKey()) && hasArrayValues(field.getValue())) {
          return true;
        }
        if (hasEvidenceIds(field.getValue())) {
          return true;
        }
      }
      return false;
    }
    if (node.isArray()) {
      for (JsonNode item : node) {
        if (hasEvidenceIds(item)) {
          return true;
        }
      }
    }
    return false;
  }

  private static boolean isEvidenceIdField(String fieldName) {
    return "evidence_ids".equals(fieldName)
        || "declaration_evidence_ids".equals(fieldName)
        || "pom_evidence_ids".equals(fieldName);
  }

  private static boolean hasArrayValues(JsonNode node) {
    return node != null && node.isArray() && !node.isEmpty();
  }
}
