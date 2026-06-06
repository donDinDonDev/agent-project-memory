package io.github.dondindondev.agentprojectmemory.analyzer.springboot;

import java.util.List;

public record SpringBootApplicationAnalysis(
    List<ModuleSpringBootApplications> modules,
    List<SpringBootApplicationEvidence> evidence) {
  public SpringBootApplicationAnalysis {
    modules = List.copyOf(modules);
    evidence = List.copyOf(evidence);
  }
}
