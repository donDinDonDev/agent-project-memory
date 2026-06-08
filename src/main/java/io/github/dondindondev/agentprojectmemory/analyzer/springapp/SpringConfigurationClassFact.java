package io.github.dondindondev.agentprojectmemory.analyzer.springapp;

import java.util.List;

public record SpringConfigurationClassFact(
    String surfaceCategory,
    String supportType,
    String className,
    String sourcePath,
    String configurationSignal,
    List<String> evidenceIds) {
  public SpringConfigurationClassFact {
    evidenceIds = List.copyOf(evidenceIds);
  }
}
