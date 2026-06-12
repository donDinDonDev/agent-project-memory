package io.github.dondindondev.agentprojectmemory.analyzer.maven;

import io.github.dondindondev.agentprojectmemory.analyzer.ScanDiagnostic;
import java.util.List;

public record MavenPluginAnalysis(
    List<MavenModulePlugins> modules,
    List<MavenPluginEvidence> evidence,
    List<ScanDiagnostic> diagnostics) {
  public MavenPluginAnalysis {
    modules = List.copyOf(modules);
    evidence = List.copyOf(evidence);
    diagnostics = List.copyOf(diagnostics);
  }
}
