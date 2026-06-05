package io.github.dondindondev.agentprojectmemory.analyzer.maven;

import java.util.List;

public record MavenPluginAnalysis(
    List<MavenModulePlugins> modules,
    List<MavenPluginEvidence> evidence) {
  public MavenPluginAnalysis {
    modules = List.copyOf(modules);
    evidence = List.copyOf(evidence);
  }
}
