package io.github.dondindondev.agentprojectmemory.analyzer.tests;

import java.util.List;

public record TestedSubjectFact(
    String className,
    String supportType,
    String confidence,
    String uncertainty,
    List<String> evidenceIds) {
  public TestedSubjectFact {
    evidenceIds = List.copyOf(evidenceIds);
  }
}
