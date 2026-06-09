package io.github.dondindondev.agentprojectmemory.analyzer.jpa;

import java.util.List;

public record JpaEntityAnalysis(
    List<JpaEntityFact> entities,
    List<JpaEmbeddableFact> embeddables,
    List<JpaEntityEvidence> evidence) {
  public JpaEntityAnalysis {
    entities = List.copyOf(entities);
    embeddables = List.copyOf(embeddables);
    evidence = List.copyOf(evidence);
  }
}
