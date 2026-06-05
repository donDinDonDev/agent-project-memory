package io.github.dondindondev.agentprojectmemory.analyzer.maven;

import java.util.List;

public record MavenDependencyAnalysis(
    List<MavenModuleDependencies> modules,
    List<MavenDependencyEvidence> evidence) {
  public MavenDependencyAnalysis {
    modules = List.copyOf(modules);
    evidence = List.copyOf(evidence);
  }
}
