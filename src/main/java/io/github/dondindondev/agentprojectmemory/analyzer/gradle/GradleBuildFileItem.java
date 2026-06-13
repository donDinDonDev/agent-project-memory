package io.github.dondindondev.agentprojectmemory.analyzer.gradle;

import java.util.List;

public record GradleBuildFileItem(
    String path,
    String role,
    String language,
    List<String> evidenceIds) {
  public GradleBuildFileItem {
    evidenceIds = List.copyOf(evidenceIds);
  }
}
