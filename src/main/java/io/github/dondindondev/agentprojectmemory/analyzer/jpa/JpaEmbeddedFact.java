package io.github.dondindondev.agentprojectmemory.analyzer.jpa;

import java.util.List;

public record JpaEmbeddedFact(
    String annotation,
    String javaType,
    String targetResolution,
    String targetClassName,
    String supportType,
    String confidence,
    String uncertainty,
    List<String> evidenceIds) {
  public JpaEmbeddedFact {
    evidenceIds = List.copyOf(evidenceIds);
  }
}
