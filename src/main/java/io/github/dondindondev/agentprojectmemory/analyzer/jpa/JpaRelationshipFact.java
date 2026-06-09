package io.github.dondindondev.agentprojectmemory.analyzer.jpa;

import java.util.List;

public record JpaRelationshipFact(
    String fieldName,
    String annotation,
    String cardinality,
    String javaType,
    JpaRelationshipTargetFact target,
    String mappedBy,
    String ownershipSignal,
    Boolean optional,
    String fetch,
    List<String> cascade,
    Boolean orphanRemoval,
    List<JpaJoinColumnFact> joinColumns,
    JpaJoinTableFact joinTable,
    List<String> evidenceIds) {
  public JpaRelationshipFact {
    cascade = List.copyOf(cascade);
    joinColumns = List.copyOf(joinColumns);
    evidenceIds = List.copyOf(evidenceIds);
  }
}
