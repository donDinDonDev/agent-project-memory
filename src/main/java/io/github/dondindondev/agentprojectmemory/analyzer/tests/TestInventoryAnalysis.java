package io.github.dondindondev.agentprojectmemory.analyzer.tests;

import java.util.List;

public record TestInventoryAnalysis(
    String analysisStatus,
    List<TestClassFact> tests,
    List<TestInventoryEvidence> evidence) {
  public TestInventoryAnalysis {
    tests = List.copyOf(tests);
    evidence = List.copyOf(evidence);
  }
}
