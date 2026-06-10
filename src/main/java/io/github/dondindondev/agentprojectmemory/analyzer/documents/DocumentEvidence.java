package io.github.dondindondev.agentprojectmemory.analyzer.documents;

public record DocumentEvidence(
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
