package io.github.dondindondev.agentprojectmemory.analyzer.warnings;

import java.util.List;

public record AnalysisWarningAnalysis(
    List<AnalysisWarningFact> warnings,
    List<AnalysisWarningEvidence> evidence) {
  public AnalysisWarningAnalysis {
    warnings = List.copyOf(warnings);
    evidence = List.copyOf(evidence);
  }
}
