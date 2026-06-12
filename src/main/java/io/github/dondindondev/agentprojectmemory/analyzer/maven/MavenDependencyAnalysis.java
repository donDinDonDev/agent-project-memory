package io.github.dondindondev.agentprojectmemory.analyzer.maven;

import io.github.dondindondev.agentprojectmemory.analyzer.ScanDiagnostic;
import java.util.List;

public record MavenDependencyAnalysis(
    List<MavenModuleDependencies> modules,
    List<MavenDependencyEvidence> evidence,
    List<ScanDiagnostic> diagnostics) {
  public MavenDependencyAnalysis {
    modules = List.copyOf(modules);
    evidence = List.copyOf(evidence);
    diagnostics = List.copyOf(diagnostics);
  }
}
