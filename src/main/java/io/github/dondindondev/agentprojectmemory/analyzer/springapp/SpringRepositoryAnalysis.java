package io.github.dondindondev.agentprojectmemory.analyzer.springapp;

import java.util.List;

public record SpringRepositoryAnalysis(
    List<SpringRepositoryFact> repositories,
    List<SpringRepositoryEvidence> evidence) {
  public SpringRepositoryAnalysis {
    repositories = List.copyOf(repositories);
    evidence = List.copyOf(evidence);
  }
}
