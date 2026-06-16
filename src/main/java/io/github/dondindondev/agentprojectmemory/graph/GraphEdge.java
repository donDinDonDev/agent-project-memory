package io.github.dondindondev.agentprojectmemory.graph;

import java.util.List;
import java.util.Objects;

public record GraphEdge(
    String id,
    String type,
    String sourceId,
    String targetId,
    String claimCategory,
    String relationStatus,
    String supportType,
    String confidence,
    String uncertainty,
    GraphDerivation derivation,
    List<String> evidenceIds) {
  public GraphEdge {
    id = requireText(id, "id");
    type = requireText(type, "type");
    sourceId = requireText(sourceId, "sourceId");
    targetId = requireText(targetId, "targetId");
    claimCategory = requireText(claimCategory, "claimCategory");
    relationStatus = requireText(relationStatus, "relationStatus");
    supportType = requireText(supportType, "supportType");
    confidence = requireText(confidence, "confidence");
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
