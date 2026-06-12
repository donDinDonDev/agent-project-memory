package io.github.dondindondev.agentprojectmemory.analyzer.maven;

import io.github.dondindondev.agentprojectmemory.analyzer.ScanDiagnostic;
import java.util.List;

public record MavenMetadataAnalysis(
    List<MavenModuleMetadata> modules,
    List<MavenMetadataEvidence> evidence,
    List<ScanDiagnostic> diagnostics) {
  public MavenMetadataAnalysis {
    modules = List.copyOf(modules);
    evidence = List.copyOf(evidence);
    diagnostics = List.copyOf(diagnostics);
  }
}
