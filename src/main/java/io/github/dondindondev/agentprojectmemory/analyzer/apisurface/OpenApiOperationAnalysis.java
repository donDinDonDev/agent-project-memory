package io.github.dondindondev.agentprojectmemory.analyzer.apisurface;

import java.util.List;

public record OpenApiOperationAnalysis(
    String analysisStatus,
    List<OpenApiOperationFact> operations,
    List<ApiSpecEvidence> evidence,
    List<OpenApiSpecWarningFact> warnings) {
  public OpenApiOperationAnalysis {
    operations = List.copyOf(operations);
    evidence = List.copyOf(evidence);
    warnings = List.copyOf(warnings);
  }
}
