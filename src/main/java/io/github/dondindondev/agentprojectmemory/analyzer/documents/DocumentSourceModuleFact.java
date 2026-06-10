package io.github.dondindondev.agentprojectmemory.analyzer.documents;

import java.util.List;

public record DocumentSourceModuleFact(
    String id,
    String moduleId,
    int moduleOrder,
    String modulePath,
    List<String> evidenceIds) {
  public DocumentSourceModuleFact {
    evidenceIds = List.copyOf(evidenceIds);
  }
}
