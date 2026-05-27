package io.github.dondindondev.agentprojectmemory.analyzer.tests;

public record TestInventoryEvidence(
    String id,
    String sourceType,
    String sourcePath,
    String className,
    String methodName,
    String symbolName,
    Integer lineStart,
    Integer lineEnd,
    String excerpt,
    String confidence) {
}
