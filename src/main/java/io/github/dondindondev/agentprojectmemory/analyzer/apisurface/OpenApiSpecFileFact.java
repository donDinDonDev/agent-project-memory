package io.github.dondindondev.agentprojectmemory.analyzer.apisurface;

import java.util.List;

public record OpenApiSpecFileFact(
    String id,
    String moduleId,
    int moduleOrder,
    String specPath,
    String format,
    String specKind,
    String version,
    List<String> evidenceIds) {
  public OpenApiSpecFileFact {
    evidenceIds = List.copyOf(evidenceIds);
  }
}
