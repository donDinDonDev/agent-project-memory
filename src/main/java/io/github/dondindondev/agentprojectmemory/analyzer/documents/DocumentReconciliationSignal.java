package io.github.dondindondev.agentprojectmemory.analyzer.documents;

import java.util.List;

public record DocumentReconciliationSignal(
    String id,
    int moduleOrder,
    String moduleId,
    String signal,
    String status,
    String documentId,
    String documentPath,
    String documentChunkId,
    String sourceFactKind,
    String sourceFactId,
    String subjectKind,
    String subjectName,
    String matchBasis,
    String confidence,
    String uncertainty,
    List<String> evidenceIds) {
  public DocumentReconciliationSignal {
    evidenceIds = List.copyOf(evidenceIds);
  }
}
