package io.github.dondindondev.agentprojectmemory.analyzer.jpa;

import java.util.List;

public record JpaIdentifierFieldFact(
    String fieldName,
    String javaType,
    List<String> evidenceIds) {
  public JpaIdentifierFieldFact {
    evidenceIds = List.copyOf(evidenceIds);
  }
}
