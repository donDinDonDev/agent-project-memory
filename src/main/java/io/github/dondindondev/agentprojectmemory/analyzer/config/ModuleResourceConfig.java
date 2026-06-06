package io.github.dondindondev.agentprojectmemory.analyzer.config;

import java.util.List;

public record ModuleResourceConfig(
    String moduleId,
    String resourceAnalysisStatus,
    String configFileAnalysisStatus,
    List<ResourceRootFact> resourceRoots,
    List<ConfigFileFact> configFiles) {
  public ModuleResourceConfig {
    resourceRoots = List.copyOf(resourceRoots);
    configFiles = List.copyOf(configFiles);
  }
}
