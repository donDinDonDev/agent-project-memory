package io.github.dondindondev.agentprojectmemory.analyzer.config;

import java.util.List;

public record ConfigFileFact(
    String id,
    String path,
    String resourceScope,
    String configKind,
    String format,
    String profileName,
    String profileSource,
    List<String> evidenceIds) {
  public ConfigFileFact {
    evidenceIds = List.copyOf(evidenceIds);
  }
}
