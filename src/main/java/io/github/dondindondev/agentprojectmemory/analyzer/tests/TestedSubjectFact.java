package io.github.dondindondev.agentprojectmemory.analyzer.tests;

import java.util.List;

public record TestedSubjectFact(
    String relationStatus,
    String relationType,
    String className,
    String targetModuleId,
    String candidateReference,
    String supportType,
    String confidence,
    String uncertainty,
    List<String> evidenceIds) {
  public TestedSubjectFact {
    evidenceIds = List.copyOf(evidenceIds);
  }
}
