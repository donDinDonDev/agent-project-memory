package io.github.dondindondev.agentprojectmemory.analyzer.apisurface;

public record ApiSpecEvidence(
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
