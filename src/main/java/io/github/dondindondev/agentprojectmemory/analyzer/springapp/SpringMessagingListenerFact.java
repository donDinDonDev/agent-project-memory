package io.github.dondindondev.agentprojectmemory.analyzer.springapp;

import java.util.List;

public record SpringMessagingListenerFact(
    String surfaceCategory,
    String supportType,
    String className,
    String methodName,
    String sourcePath,
    String targetKind,
    String annotationSymbol,
    String listenerFramework,
    String listenerSignal,
    String idDiscriminator,
    List<String> evidenceIds) {
  public SpringMessagingListenerFact {
    evidenceIds = List.copyOf(evidenceIds);
  }
}
