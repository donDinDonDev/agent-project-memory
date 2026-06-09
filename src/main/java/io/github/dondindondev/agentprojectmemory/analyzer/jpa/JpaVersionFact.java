package io.github.dondindondev.agentprojectmemory.analyzer.jpa;

import java.util.List;

public record JpaVersionFact(List<String> evidenceIds) {
  public JpaVersionFact {
    evidenceIds = List.copyOf(evidenceIds);
  }
}
