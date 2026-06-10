package io.github.dondindondev.agentprojectmemory.analyzer.documents;

import java.util.List;

public record DocumentSourceApiFact(
    String id,
    String sourceFactKind,
    String moduleId,
    int moduleOrder,
    String subjectName,
    List<String> pathTokens,
    List<String> evidenceIds) {
  public DocumentSourceApiFact {
    pathTokens = List.copyOf(pathTokens);
    evidenceIds = List.copyOf(evidenceIds);
  }
}
