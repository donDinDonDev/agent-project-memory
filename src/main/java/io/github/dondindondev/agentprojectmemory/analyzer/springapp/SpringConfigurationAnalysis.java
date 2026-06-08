package io.github.dondindondev.agentprojectmemory.analyzer.springapp;

import java.util.List;

public record SpringConfigurationAnalysis(
    List<SpringConfigurationClassFact> configurationClasses,
    List<SpringConfigurationPropertiesFact> configurationProperties,
    List<SpringBeanMethodFact> beanMethods,
    List<SpringConfigurationEvidence> evidence) {
  public SpringConfigurationAnalysis {
    configurationClasses = List.copyOf(configurationClasses);
    configurationProperties = List.copyOf(configurationProperties);
    beanMethods = List.copyOf(beanMethods);
    evidence = List.copyOf(evidence);
  }
}
