package io.github.dondindondev.agentprojectmemory.analyzer.tests;

import java.util.List;

public record TestMockSignalFact(
    String annotation,
    String mockSignal,
    String signalKind,
    String targetKind,
    String targetName,
    List<String> evidenceIds) {
  public TestMockSignalFact {
    evidenceIds = List.copyOf(evidenceIds);
  }
}
