package io.github.dondindondev.agentprojectmemory.analyzer.maven;

import java.util.List;

public record MavenPluginDeclaration(
    String id,
    String declarationKind,
    int declarationOrdinal,
    MavenMetadataValue groupId,
    MavenMetadataValue artifactId,
    MavenMetadataValue version,
    List<MavenPluginExecution> executions,
    List<MavenPluginSignal> configurationSignals,
    List<MavenPluginSignal> generatorSignals,
    List<String> evidenceIds) {
  public MavenPluginDeclaration {
    executions = List.copyOf(executions);
    configurationSignals = List.copyOf(configurationSignals);
    generatorSignals = List.copyOf(generatorSignals);
    evidenceIds = List.copyOf(evidenceIds);
  }
}
