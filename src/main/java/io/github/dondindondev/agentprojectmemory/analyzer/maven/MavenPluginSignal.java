package io.github.dondindondev.agentprojectmemory.analyzer.maven;

import java.util.List;

public record MavenPluginSignal(
    String signal,
    List<String> evidenceIds) {
  public MavenPluginSignal {
    evidenceIds = List.copyOf(evidenceIds);
  }
}
