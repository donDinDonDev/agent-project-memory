package io.github.dondindondev.agentprojectmemory.analyzer.jpa;

import java.util.List;

public record JpaGeneratedValueFact(
    String strategy,
    String generator,
    List<String> evidenceIds) {
  public JpaGeneratedValueFact {
    evidenceIds = List.copyOf(evidenceIds);
  }
}
