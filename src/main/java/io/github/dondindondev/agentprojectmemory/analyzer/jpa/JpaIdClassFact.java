package io.github.dondindondev.agentprojectmemory.analyzer.jpa;

import java.util.List;

public record JpaIdClassFact(
    String typeName,
    String fieldMatchingStatus,
    String semanticReconstructionStatus,
    List<String> evidenceIds) {
  public JpaIdClassFact {
    evidenceIds = List.copyOf(evidenceIds);
  }
}
