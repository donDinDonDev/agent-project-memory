package io.github.dondindondev.agentprojectmemory.analyzer.apisurface;

import java.util.List;

public record OpenApiSpecDiscoveryAnalysis(
    String analysisStatus,
    List<OpenApiSpecFileFact> specFiles,
    List<ApiSpecEvidence> evidence) {
  public OpenApiSpecDiscoveryAnalysis {
    specFiles = List.copyOf(specFiles);
    evidence = List.copyOf(evidence);
  }
}
