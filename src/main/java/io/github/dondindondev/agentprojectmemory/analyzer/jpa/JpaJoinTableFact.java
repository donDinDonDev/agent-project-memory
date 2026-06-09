package io.github.dondindondev.agentprojectmemory.analyzer.jpa;

import java.util.List;

public record JpaJoinTableFact(
    String name,
    String schema,
    String catalog,
    List<JpaJoinColumnFact> joinColumns,
    List<JpaJoinColumnFact> inverseJoinColumns,
    List<String> evidenceIds) {
  public JpaJoinTableFact {
    joinColumns = List.copyOf(joinColumns);
    inverseJoinColumns = List.copyOf(inverseJoinColumns);
    evidenceIds = List.copyOf(evidenceIds);
  }
}
