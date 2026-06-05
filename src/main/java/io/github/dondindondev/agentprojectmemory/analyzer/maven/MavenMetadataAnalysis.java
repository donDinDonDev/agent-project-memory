package io.github.dondindondev.agentprojectmemory.analyzer.maven;

import java.util.List;

public record MavenMetadataAnalysis(
    List<MavenModuleMetadata> modules,
    List<MavenMetadataEvidence> evidence) {
  public MavenMetadataAnalysis {
    modules = List.copyOf(modules);
    evidence = List.copyOf(evidence);
  }
}
