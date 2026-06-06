package io.github.dondindondev.agentprojectmemory.analyzer.config;

import java.util.List;

public record ResourceConfigAnalysis(
    List<ModuleResourceConfig> modules,
    List<ResourceConfigEvidence> evidence) {
  public ResourceConfigAnalysis {
    modules = List.copyOf(modules);
    evidence = List.copyOf(evidence);
  }
}
