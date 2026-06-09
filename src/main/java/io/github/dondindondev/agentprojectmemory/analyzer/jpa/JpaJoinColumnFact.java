package io.github.dondindondev.agentprojectmemory.analyzer.jpa;

import java.util.List;

public record JpaJoinColumnFact(
    String name,
    String referencedColumnName,
    Boolean nullable,
    Boolean unique,
    Boolean insertable,
    Boolean updatable,
    List<String> evidenceIds) {
  public JpaJoinColumnFact {
    evidenceIds = List.copyOf(evidenceIds);
  }
}
