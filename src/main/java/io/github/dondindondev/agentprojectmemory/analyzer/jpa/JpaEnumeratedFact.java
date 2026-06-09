package io.github.dondindondev.agentprojectmemory.analyzer.jpa;

import java.util.List;

public record JpaEnumeratedFact(
    String value,
    List<String> evidenceIds) {
  public JpaEnumeratedFact {
    evidenceIds = List.copyOf(evidenceIds);
  }
}
