package io.github.dondindondev.agentprojectmemory.analyzer.documents;

import java.util.List;

public record DocumentHeadingFact(
    String id,
    int level,
    String title,
    String anchor,
    int lineStart,
    int lineEnd,
    List<String> evidenceIds) {
  public DocumentHeadingFact {
    evidenceIds = List.copyOf(evidenceIds);
  }
}
