package io.github.dondindondev.agentprojectmemory.analyzer.jpa;

import java.util.List;

public record JpaEmbeddableFact(
    String id,
    String className,
    String sourcePath,
    List<JpaEntityFieldFact> fields,
    List<String> evidenceIds) {
  public JpaEmbeddableFact {
    fields = List.copyOf(fields);
    evidenceIds = List.copyOf(evidenceIds);
  }
}
