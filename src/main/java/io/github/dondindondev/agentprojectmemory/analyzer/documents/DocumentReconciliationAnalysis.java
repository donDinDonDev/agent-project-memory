package io.github.dondindondev.agentprojectmemory.analyzer.documents;

import java.util.List;

public record DocumentReconciliationAnalysis(
    String analysisStatus,
    List<DocumentReconciliationSignal> signals,
    List<DocumentEvidence> evidence) {
  public DocumentReconciliationAnalysis {
    signals = List.copyOf(signals);
    evidence = List.copyOf(evidence);
  }
}
