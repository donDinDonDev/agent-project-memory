package io.github.dondindondev.agentprojectmemory.analyzer.springapp;

import java.util.List;

public record SpringScheduledMethodFact(
    String surfaceCategory,
    String supportType,
    String className,
    String methodName,
    String sourcePath,
    String targetKind,
    String annotationSymbol,
    String scheduledSignal,
    String idDiscriminator,
    List<String> evidenceIds) {
  public SpringScheduledMethodFact {
    evidenceIds = List.copyOf(evidenceIds);
  }
}
