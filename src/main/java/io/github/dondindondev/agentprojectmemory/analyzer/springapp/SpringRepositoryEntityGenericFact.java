package io.github.dondindondev.agentprojectmemory.analyzer.springapp;

import java.util.List;

public record SpringRepositoryEntityGenericFact(
    String sourceTypeName,
    String qualifiedTypeName,
    String supportStatus,
    List<String> evidenceIds) {
  public SpringRepositoryEntityGenericFact {
    evidenceIds = List.copyOf(evidenceIds);
  }
}
