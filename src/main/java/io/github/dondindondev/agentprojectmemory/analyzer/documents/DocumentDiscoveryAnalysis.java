package io.github.dondindondev.agentprojectmemory.analyzer.documents;

import java.util.List;

public record DocumentDiscoveryAnalysis(
    String analysisStatus,
    DocumentDiscoveryPolicy discoveryPolicy,
    List<DocumentFileFact> documents,
    List<DocumentEvidence> evidence) {
  public static DocumentDiscoveryAnalysis notAnalyzed(DocumentDiscoveryPolicy discoveryPolicy) {
    return new DocumentDiscoveryAnalysis("not_analyzed", discoveryPolicy, List.of(), List.of());
  }

  public DocumentDiscoveryAnalysis {
    documents = List.copyOf(documents);
    evidence = List.copyOf(evidence);
  }
}
