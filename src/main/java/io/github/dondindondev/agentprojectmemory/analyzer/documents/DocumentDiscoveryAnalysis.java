package io.github.dondindondev.agentprojectmemory.analyzer.documents;

import java.util.List;

public record DocumentDiscoveryAnalysis(
    String analysisStatus,
    DocumentDiscoveryPolicy discoveryPolicy,
    List<DocumentFileFact> documents) {
  public DocumentDiscoveryAnalysis {
    documents = List.copyOf(documents);
  }
}
