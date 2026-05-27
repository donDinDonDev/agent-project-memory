package io.github.dondindondev.agentprojectmemory.analyzer.jpa;

public record JpaEntityEvidence(
    String id,
    String sourcePath,
    String className,
    String methodName,
    String annotationSymbol,
    Integer lineStart,
    Integer lineEnd,
    String excerpt,
    String confidence) {
}
