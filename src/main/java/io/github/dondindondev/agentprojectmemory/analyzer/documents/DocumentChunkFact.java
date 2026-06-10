package io.github.dondindondev.agentprojectmemory.analyzer.documents;

import java.util.List;

public record DocumentChunkFact(
    String id,
    String headingId,
    int lineStart,
    int lineEnd,
    String contentStatus,
    List<String> evidenceIds) {
  public DocumentChunkFact {
    evidenceIds = List.copyOf(evidenceIds);
  }
}
