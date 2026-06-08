package io.github.dondindondev.agentprojectmemory.analyzer.springapp;

public record SpringConfigurationEvidence(
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
