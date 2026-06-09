package io.github.dondindondev.agentprojectmemory.analyzer.springapp;

import java.util.List;

public record SpringRepositoryFact(
    String surfaceCategory,
    String supportType,
    String className,
    String sourcePath,
    String repositorySignal,
    List<String> extendsTypes,
    List<SpringRepositoryEntityGenericFact> entityGenericTypes,
    String entityRelationStatus,
    SpringRepositoryEntityRelationFact entityRelation,
    List<String> evidenceIds) {
  public SpringRepositoryFact {
    extendsTypes = List.copyOf(extendsTypes);
    entityGenericTypes = List.copyOf(entityGenericTypes);
    evidenceIds = List.copyOf(evidenceIds);
  }

  public SpringRepositoryFact withEntityRelation(
      String updatedEntityRelationStatus,
      SpringRepositoryEntityRelationFact updatedEntityRelation) {
    return new SpringRepositoryFact(
        surfaceCategory,
        supportType,
        className,
        sourcePath,
        repositorySignal,
        extendsTypes,
        entityGenericTypes,
        updatedEntityRelationStatus,
        updatedEntityRelation,
        evidenceIds);
  }
}
