package io.github.dondindondev.agentprojectmemory.analyzer.tests;

import java.util.List;

public record TestClassFact(
    String className,
    String sourcePath,
    List<TestFrameworkSignalFact> frameworkSignals,
    List<TestSpringSliceFact> springTestSlices,
    List<TestMockSignalFact> mockSignals,
    List<TestMethodFact> methods,
    List<TestedSubjectFact> testedSubjects,
    List<String> evidenceIds) {
  public TestClassFact {
    frameworkSignals = List.copyOf(frameworkSignals);
    springTestSlices = List.copyOf(springTestSlices);
    mockSignals = List.copyOf(mockSignals);
    methods = List.copyOf(methods);
    testedSubjects = List.copyOf(testedSubjects);
    evidenceIds = List.copyOf(evidenceIds);
  }
}
