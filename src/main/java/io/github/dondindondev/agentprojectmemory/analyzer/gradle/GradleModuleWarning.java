package io.github.dondindondev.agentprojectmemory.analyzer.gradle;

import java.util.List;

public record GradleModuleWarning(
    String id,
    String category,
    String signal,
    String moduleId,
    String message,
    String sourcePath,
    List<String> evidenceIds) {
  public GradleModuleWarning {
    evidenceIds = List.copyOf(evidenceIds);
  }
}
