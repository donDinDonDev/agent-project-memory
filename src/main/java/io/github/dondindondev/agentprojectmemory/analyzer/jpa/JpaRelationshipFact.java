package io.github.dondindondev.agentprojectmemory.analyzer.jpa;

import java.util.List;

public record JpaRelationshipFact(
    String fieldName,
    String annotation,
    String javaType,
    String targetResolution,
    String uncertainty,
    List<String> evidenceIds) {
  public JpaRelationshipFact {
    evidenceIds = List.copyOf(evidenceIds);
  }
}
