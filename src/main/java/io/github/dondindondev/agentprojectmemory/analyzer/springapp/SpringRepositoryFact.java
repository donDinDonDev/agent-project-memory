package io.github.dondindondev.agentprojectmemory.analyzer.springapp;

import java.util.List;

public record SpringRepositoryFact(
    String surfaceCategory,
    String supportType,
    String className,
    String sourcePath,
    String repositorySignal,
    List<String> extendsTypes,
    String entityRelationStatus,
    List<String> evidenceIds) {
  public SpringRepositoryFact {
    extendsTypes = List.copyOf(extendsTypes);
    evidenceIds = List.copyOf(evidenceIds);
  }
}
