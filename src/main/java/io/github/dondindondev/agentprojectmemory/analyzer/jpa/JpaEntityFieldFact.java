package io.github.dondindondev.agentprojectmemory.analyzer.jpa;

import java.util.List;

public record JpaEntityFieldFact(
    String fieldName,
    String javaType,
    String declaringClass,
    String sourceKind,
    String persistenceRole,
    List<String> annotations,
    JpaColumnFact column,
    JpaEnumeratedFact enumerated,
    JpaGeneratedValueFact generatedValue,
    JpaVersionFact version,
    JpaEmbeddedFact embedded,
    List<String> evidenceIds) {
  public JpaEntityFieldFact {
    annotations = List.copyOf(annotations);
    evidenceIds = List.copyOf(evidenceIds);
  }
}
