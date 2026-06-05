package io.github.dondindondev.agentprojectmemory.analyzer.maven;

public record MavenMetadataEvidence(
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
