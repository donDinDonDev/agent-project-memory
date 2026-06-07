package io.github.dondindondev.agentprojectmemory.analyzer.apisurface;

import java.util.List;

public record OpenApiSpecWarningFact(
    String id,
    String category,
    String signal,
    String moduleId,
    int moduleOrder,
    String message,
    String sourcePath,
    List<String> evidenceIds) {
  public OpenApiSpecWarningFact {
    evidenceIds = List.copyOf(evidenceIds);
  }
}
