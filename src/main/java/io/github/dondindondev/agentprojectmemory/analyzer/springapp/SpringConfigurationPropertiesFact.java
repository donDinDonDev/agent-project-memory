package io.github.dondindondev.agentprojectmemory.analyzer.springapp;

import java.util.List;

public record SpringConfigurationPropertiesFact(
    String surfaceCategory,
    String supportType,
    String className,
    String sourcePath,
    String configurationPropertiesSignal,
    String bindingStatus,
    List<String> evidenceIds) {
  public SpringConfigurationPropertiesFact {
    evidenceIds = List.copyOf(evidenceIds);
  }
}
