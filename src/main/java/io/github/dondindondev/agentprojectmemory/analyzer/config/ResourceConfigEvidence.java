package io.github.dondindondev.agentprojectmemory.analyzer.config;

public record ResourceConfigEvidence(
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
