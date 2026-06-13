package io.github.dondindondev.agentprojectmemory.analyzer.gradle;

import io.github.dondindondev.agentprojectmemory.analyzer.ScanDiagnostic;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenModuleItem;
import java.util.List;

public record GradleModuleDiscoveryAnalysis(
    String analysisStatus,
    List<MavenModuleItem> items,
    List<GradleModuleWarning> warnings,
    List<GradleBuildFileEvidence> evidence,
    List<GradleBuildFileItem> rootBuildFiles,
    List<ScanDiagnostic> diagnostics) {
  public GradleModuleDiscoveryAnalysis {
    items = List.copyOf(items);
    warnings = List.copyOf(warnings);
    evidence = List.copyOf(evidence);
    rootBuildFiles = List.copyOf(rootBuildFiles);
    diagnostics = List.copyOf(diagnostics);
  }
}
