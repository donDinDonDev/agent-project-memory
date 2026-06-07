package io.github.dondindondev.agentprojectmemory.analyzer.apisurface;

import java.util.List;

public record OpenApiOperationFact(
    String id,
    String moduleId,
    int moduleOrder,
    String apiSurfaceCategory,
    String specPath,
    String httpMethod,
    String path,
    String operationId,
    List<String> tags,
    String implementationStatus,
    List<String> evidenceIds) {
  public OpenApiOperationFact {
    tags = List.copyOf(tags);
    evidenceIds = List.copyOf(evidenceIds);
  }
}
