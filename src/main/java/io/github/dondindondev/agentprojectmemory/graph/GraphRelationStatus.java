package io.github.dondindondev.agentprojectmemory.graph;

import java.util.List;
import java.util.Objects;

public record GraphRelationStatus(
    String id,
    String relationFamily,
    String sourceId,
    String targetId,
    String relationStatus,
    String supportType,
    String confidence,
    String uncertainty,
    GraphDerivation derivation,
    List<String> evidenceIds) {
  public GraphRelationStatus {
    id = requireText(id, "id");
    relationFamily = requireText(relationFamily, "relationFamily");
    relationStatus = requireText(relationStatus, "relationStatus");
    supportType = requireText(supportType, "supportType");
    confidence = requireText(confidence, "confidence");
    derivation = Objects.requireNonNull(derivation, "derivation");
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
