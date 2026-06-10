package io.github.dondindondev.agentprojectmemory.analyzer.tests;

import java.util.List;

public record TestMethodFact(
    String methodName,
    String testAnnotation,
    String methodKind,
    String displayName,
    List<String> evidenceIds) {
  public TestMethodFact {
    evidenceIds = List.copyOf(evidenceIds);
  }
}
