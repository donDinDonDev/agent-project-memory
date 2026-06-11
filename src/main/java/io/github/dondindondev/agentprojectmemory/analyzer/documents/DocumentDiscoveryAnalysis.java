package io.github.dondindondev.agentprojectmemory.analyzer.documents;

import io.github.dondindondev.agentprojectmemory.analyzer.ScanDiagnostic;
import java.util.List;

public record DocumentDiscoveryAnalysis(
    String analysisStatus,
    DocumentDiscoveryPolicy discoveryPolicy,
    List<DocumentFileFact> documents,
    List<DocumentEvidence> evidence,
    List<ScanDiagnostic> diagnostics) {
  public static DocumentDiscoveryAnalysis notAnalyzed(DocumentDiscoveryPolicy discoveryPolicy) {
    return new DocumentDiscoveryAnalysis("not_analyzed", discoveryPolicy, List.of(), List.of(), List.of());
  }

  public DocumentDiscoveryAnalysis {
    documents = List.copyOf(documents);
    evidence = List.copyOf(evidence);
    diagnostics = List.copyOf(diagnostics);
  }
}
