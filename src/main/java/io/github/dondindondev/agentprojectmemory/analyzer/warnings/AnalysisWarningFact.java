package io.github.dondindondev.agentprojectmemory.analyzer.warnings;

import java.util.List;

public record AnalysisWarningFact(
    String id,
    String category,
    String signal,
    String message,
    String sourcePath,
    List<String> evidenceIds) {
  public AnalysisWarningFact {
    evidenceIds = List.copyOf(evidenceIds);
  }
}
