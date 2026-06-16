package io.github.dondindondev.agentprojectmemory.graph;

import java.util.List;
import java.util.Objects;

public record GraphNode(
    String id,
    String kind,
    String label,
    String claimCategory,
    String moduleId,
    GraphSourceRef sourceRef,
    List<String> evidenceIds) {
  public GraphNode {
    id = requireText(id, "id");
    kind = requireText(kind, "kind");
    label = requireText(label, "label");
    claimCategory = requireText(claimCategory, "claimCategory");
    sourceRef = Objects.requireNonNull(sourceRef, "sourceRef");
    evidenceIds = List.copyOf(Objects.requireNonNull(evidenceIds, "evidenceIds"));
  }

  private static String requireText(String value, String fieldName) {
    Objects.requireNonNull(value, fieldName);
    if (value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " must not be blank.");
    }
    return value;
  }
}
