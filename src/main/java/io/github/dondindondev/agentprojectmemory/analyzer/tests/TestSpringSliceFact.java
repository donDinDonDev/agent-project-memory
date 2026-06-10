package io.github.dondindondev.agentprojectmemory.analyzer.tests;

import java.util.List;

public record TestSpringSliceFact(
    String annotation,
    String sliceKind,
    String signalKind,
    List<String> evidenceIds) {
  public TestSpringSliceFact {
    evidenceIds = List.copyOf(evidenceIds);
  }
}
