package io.github.dondindondev.agentprojectmemory.analyzer.maven;

import java.util.List;

public record MavenModuleWarning(
    String id,
    String category,
    String signal,
    String moduleId,
    String message,
    String sourcePath,
    List<String> evidenceIds) {
  public MavenModuleWarning {
    evidenceIds = List.copyOf(evidenceIds);
  }
}
