package io.github.dondindondev.agentprojectmemory.analyzer.jpa;

import java.util.List;

public record JpaEntityFact(
    String id,
    String className,
    String tableName,
    List<JpaEntityFieldFact> fields,
    List<JpaIdentifierFieldFact> identifierFields,
    List<JpaRelationshipFact> relationships,
    List<String> evidenceIds) {
  public JpaEntityFact {
    fields = List.copyOf(fields);
    identifierFields = List.copyOf(identifierFields);
    relationships = List.copyOf(relationships);
    evidenceIds = List.copyOf(evidenceIds);
  }
}
