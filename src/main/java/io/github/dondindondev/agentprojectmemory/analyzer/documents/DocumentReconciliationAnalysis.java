package io.github.dondindondev.agentprojectmemory.analyzer.documents;

import io.github.dondindondev.agentprojectmemory.analyzer.ScanDiagnostic;
import java.util.List;

public record DocumentReconciliationAnalysis(
    String analysisStatus,
    List<DocumentReconciliationSignal> signals,
    List<DocumentEvidence> evidence,
    List<ScanDiagnostic> diagnostics) {
  public DocumentReconciliationAnalysis {
    signals = List.copyOf(signals);
    evidence = List.copyOf(evidence);
    diagnostics = List.copyOf(diagnostics);
  }
}
