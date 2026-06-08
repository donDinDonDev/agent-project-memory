package io.github.dondindondev.agentprojectmemory.analyzer.springapp;

import java.util.List;

public record SpringTransactionBoundaryFact(
    String surfaceCategory,
    String supportType,
    String className,
    String methodName,
    String sourcePath,
    String targetKind,
    String annotationSymbol,
    String transactionSignal,
    String idDiscriminator,
    List<String> evidenceIds) {
  public SpringTransactionBoundaryFact {
    evidenceIds = List.copyOf(evidenceIds);
  }
}
