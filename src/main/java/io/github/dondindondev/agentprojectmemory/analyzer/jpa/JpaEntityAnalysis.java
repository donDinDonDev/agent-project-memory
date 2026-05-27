package io.github.dondindondev.agentprojectmemory.analyzer.jpa;

import java.util.List;

public record JpaEntityAnalysis(
    List<JpaEntityFact> entities,
    List<JpaEntityEvidence> evidence) {
  public JpaEntityAnalysis {
    entities = List.copyOf(entities);
    evidence = List.copyOf(evidence);
  }
}
