package io.github.dondindondev.agentprojectmemory.generator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

final class EvidenceReferenceRenderer {
  private static final ObjectMapper JSON = new ObjectMapper();
  private static final int MAX_INLINE_EVIDENCE_REFERENCES = 5;

  private EvidenceReferenceRenderer() {
  }

  static Map<String, EvidenceRecord> evidenceById(String evidenceIndexJsonl)
      throws IOException {
    Map<String, EvidenceRecord> evidenceById = new LinkedHashMap<>();
    for (String line : physicalJsonlLines(evidenceIndexJsonl)) {
      if (line.isBlank()) {
        continue;
      }
      JsonNode evidence = JSON.readTree(line);
      EvidenceRecord record = new EvidenceRecord(
          text(evidence, "id"),
          nullableText(evidence, "path"),
          nullableInteger(evidence, "line_start"),
          nullableInteger(evidence, "line_end"),
          nullableText(evidence, "symbol_name"));
      evidenceById.put(record.id(), record);
    }
    return evidenceById;
  }

  static List<String> evidenceIdsInSubtree(JsonNode node) {
    LinkedHashSet<String> ids = new LinkedHashSet<>();
    collectEvidenceIds(node, ids);
    return List.copyOf(ids);
  }

  static List<String> evidencePaths(
      JsonNode node,
      Map<String, EvidenceRecord> evidenceById) {
    LinkedHashSet<String> paths = new LinkedHashSet<>();
    collectEvidencePaths(node, evidenceById, paths);
    return List.copyOf(paths);
  }

  static String evidenceReferenceList(
      List<String> ids,
      Map<String, EvidenceRecord> evidenceById) {
    if (ids.isEmpty()) {
      return "none recorded";
    }

    int visibleCount = Math.min(ids.size(), MAX_INLINE_EVIDENCE_REFERENCES);
    StringJoiner joiner = new StringJoiner(", ");
    for (int i = 0; i < visibleCount; i++) {
      joiner.add(evidenceReference(ids.get(i), evidenceById));
    }
    StringBuilder references = new StringBuilder(joiner.toString());
    appendOmittedEvidenceSuffix(references, ids.size() - visibleCount);
    return references.toString();
  }

  static List<String> stringValues(JsonNode values) {
    if (!values.isArray()) {
      return List.of();
    }

    List<String> strings = new ArrayList<>();
    for (JsonNode value : values) {
      if (!value.isNull()) {
        strings.add(value.asText());
      }
    }
    return strings;
  }

  private static void collectEvidenceIds(JsonNode node, LinkedHashSet<String> ids) {
    if (node == null || node.isMissingNode() || node.isNull()) {
      return;
    }
    if (node.isObject()) {
      node.fields().forEachRemaining(entry -> {
        if (entry.getKey().endsWith("evidence_ids") && entry.getValue().isArray()) {
          ids.addAll(stringValues(entry.getValue()));
        }
        collectEvidenceIds(entry.getValue(), ids);
      });
      return;
    }
    if (node.isArray()) {
      for (JsonNode child : node) {
        collectEvidenceIds(child, ids);
      }
    }
  }

  private static void collectEvidencePaths(
      JsonNode node,
      Map<String, EvidenceRecord> evidenceById,
      LinkedHashSet<String> paths) {
    if (node == null || node.isMissingNode() || node.isNull()) {
      return;
    }
    if (node.isObject()) {
      node.fields().forEachRemaining(entry -> {
        if (entry.getKey().endsWith("evidence_ids") && entry.getValue().isArray()) {
          for (String id : stringValues(entry.getValue())) {
            EvidenceRecord evidence = evidenceById.get(id);
            if (evidence != null && evidence.path() != null && !evidence.path().isBlank()) {
              paths.add(evidence.path());
            }
          }
        }
        collectEvidencePaths(entry.getValue(), evidenceById, paths);
      });
      return;
    }
    if (node.isArray()) {
      for (JsonNode child : node) {
        collectEvidencePaths(child, evidenceById, paths);
      }
    }
  }

  private static List<String> physicalJsonlLines(String jsonl) {
    List<String> lines = new ArrayList<>();
    int start = 0;
    for (int index = 0; index < jsonl.length(); index++) {
      if (jsonl.charAt(index) == '\n') {
        int end = index;
        if (end > start && jsonl.charAt(end - 1) == '\r') {
          end--;
        }
        lines.add(jsonl.substring(start, end));
        start = index + 1;
      }
    }
    if (start < jsonl.length()) {
      int end = jsonl.length();
      if (end > start && jsonl.charAt(end - 1) == '\r') {
        end--;
      }
      lines.add(jsonl.substring(start, end));
    }
    return lines;
  }

  private static void appendOmittedEvidenceSuffix(StringBuilder markdown, int omittedCount) {
    if (omittedCount <= 0) {
      return;
    }
    markdown.append(", ... and ")
        .append(omittedCount)
        .append(" more evidence references in ")
        .append(code("evidence-index.jsonl"));
  }

  private static String evidenceReference(
      String id,
      Map<String, EvidenceRecord> evidenceById) {
    EvidenceRecord evidence = evidenceById.get(id);
    if (evidence == null) {
      return code(id) + " (unresolved evidence record)";
    }
    return code(evidence.location()) + " (" + code(id) + ")";
  }

  private static String text(JsonNode node, String fieldName) {
    JsonNode value = node.path(fieldName);
    if (value.isMissingNode() || value.isNull()) {
      return "";
    }
    return value.asText();
  }

  private static String nullableText(JsonNode node, String fieldName) {
    JsonNode value = node.path(fieldName);
    if (value.isMissingNode() || value.isNull()) {
      return null;
    }
    return value.asText();
  }

  private static Integer nullableInteger(JsonNode node, String fieldName) {
    JsonNode value = node.path(fieldName);
    if (value.isMissingNode() || value.isNull()) {
      return null;
    }
    return value.asInt();
  }

  private static String code(String value) {
    return MarkdownRenderer.inlineCode(value);
  }
}
