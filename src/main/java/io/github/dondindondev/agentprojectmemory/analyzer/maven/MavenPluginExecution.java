package io.github.dondindondev.agentprojectmemory.analyzer.maven;

import java.util.List;

public record MavenPluginExecution(
    String executionId,
    MavenMetadataValue phase,
    List<MavenMetadataValue> goals,
    List<String> evidenceIds) {
  public MavenPluginExecution {
    goals = List.copyOf(goals);
    evidenceIds = List.copyOf(evidenceIds);
  }
}
