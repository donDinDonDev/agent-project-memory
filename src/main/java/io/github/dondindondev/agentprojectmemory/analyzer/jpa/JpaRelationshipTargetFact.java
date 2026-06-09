package io.github.dondindondev.agentprojectmemory.analyzer.jpa;

import java.util.List;

public record JpaRelationshipTargetFact(
    String declaredType,
    String targetResolution,
    String targetEntityId,
    String targetModuleId,
    String targetClassName,
    String supportType,
    String confidence,
    String uncertainty,
    List<String> evidenceIds) {
  public JpaRelationshipTargetFact {
    evidenceIds = List.copyOf(evidenceIds);
  }
}
