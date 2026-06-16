package io.github.dondindondev.agentprojectmemory.graph;

import java.util.List;
import java.util.Objects;

public record GraphWarning(
    String id,
    String category,
    String severity,
    String message,
    GraphSourceRef sourceRef,
    GraphDerivation derivation,
    List<String> evidenceIds) {
  public GraphWarning {
    id = requireText(id, "id");
    category = requireText(category, "category");
    severity = requireText(severity, "severity");
    message = requireText(message, "message");
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
