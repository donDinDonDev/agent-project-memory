package io.github.dondindondev.agentprojectmemory.analyzer.springapp;

import java.util.List;

public record SpringRepositoryEntityRelationFact(
    String supportType,
    String relationType,
    String targetEntityId,
    String targetModuleId,
    String targetClassName,
    String genericType,
    String confidence,
    String uncertainty,
    List<String> evidenceIds) {
  public SpringRepositoryEntityRelationFact {
    evidenceIds = List.copyOf(evidenceIds);
  }
}
