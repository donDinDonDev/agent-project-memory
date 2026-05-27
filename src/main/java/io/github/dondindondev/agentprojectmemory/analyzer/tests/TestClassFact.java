package io.github.dondindondev.agentprojectmemory.analyzer.tests;

import java.util.List;

public record TestClassFact(
    String className,
    String sourcePath,
    List<TestFrameworkSignalFact> frameworkSignals,
    List<TestedSubjectFact> testedSubjects,
    List<String> evidenceIds) {
  public TestClassFact {
    frameworkSignals = List.copyOf(frameworkSignals);
    testedSubjects = List.copyOf(testedSubjects);
    evidenceIds = List.copyOf(evidenceIds);
  }
}
