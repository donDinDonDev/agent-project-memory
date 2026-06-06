package io.github.dondindondev.agentprojectmemory.analyzer.config;

import java.util.List;

public record ResourceRootFact(
    String id,
    String scope,
    String path,
    List<String> evidenceIds) {
  public ResourceRootFact {
    evidenceIds = List.copyOf(evidenceIds);
  }
}
