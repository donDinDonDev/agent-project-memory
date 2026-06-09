package io.github.dondindondev.agentprojectmemory.analyzer.jpa;

import java.util.List;

public record JpaColumnFact(
    String name,
    Boolean nullable,
    Boolean unique,
    Integer length,
    Integer precision,
    Integer scale,
    Boolean insertable,
    Boolean updatable,
    List<String> evidenceIds) {
  public JpaColumnFact {
    evidenceIds = List.copyOf(evidenceIds);
  }
}
