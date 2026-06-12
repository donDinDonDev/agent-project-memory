package io.github.dondindondev.agentprojectmemory.analyzer.maven;

import io.github.dondindondev.agentprojectmemory.analyzer.ScanDiagnostic;
import java.util.List;

public record MavenModuleDiscoveryAnalysis(
    String analysisStatus,
    List<MavenModuleItem> items,
    List<MavenModuleWarning> warnings,
    List<MavenModuleDiscoveryEvidence> evidence,
    List<ScanDiagnostic> diagnostics) {
  public MavenModuleDiscoveryAnalysis {
    items = List.copyOf(items);
    warnings = List.copyOf(warnings);
    evidence = List.copyOf(evidence);
    diagnostics = List.copyOf(diagnostics);
  }
}
