package io.github.dondindondev.agentprojectmemory.analyzer.springapp;

import java.util.List;

public record SpringEventListenerFact(
    String surfaceCategory,
    String supportType,
    String className,
    String methodName,
    String sourcePath,
    String targetKind,
    String annotationSymbol,
    String eventListenerSignal,
    String idDiscriminator,
    List<String> evidenceIds) {
  public SpringEventListenerFact {
    evidenceIds = List.copyOf(evidenceIds);
  }
}
