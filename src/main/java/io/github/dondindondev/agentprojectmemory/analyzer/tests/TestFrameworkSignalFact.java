package io.github.dondindondev.agentprojectmemory.analyzer.tests;

import java.util.List;

public record TestFrameworkSignalFact(
    String name,
    List<String> evidenceIds) {
  public TestFrameworkSignalFact {
    evidenceIds = List.copyOf(evidenceIds);
  }
}
