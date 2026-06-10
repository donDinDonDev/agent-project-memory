package io.github.dondindondev.agentprojectmemory.analyzer.documents;

import java.util.List;

public record DocumentFileFact(
    String id,
    String documentKind,
    String format,
    String moduleId,
    int moduleOrder,
    String path,
    String title,
    String titleSource,
    String discoverySource,
    List<String> headings,
    List<String> chunks,
    List<String> evidenceIds) {
  public DocumentFileFact {
    headings = List.copyOf(headings);
    chunks = List.copyOf(chunks);
    evidenceIds = List.copyOf(evidenceIds);
  }
}
