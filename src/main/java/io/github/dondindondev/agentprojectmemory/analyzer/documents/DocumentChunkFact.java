package io.github.dondindondev.agentprojectmemory.analyzer.documents;

public record DocumentChunkFact(
    String id,
    String headingId,
    int lineStart,
    int lineEnd,
    String contentStatus) {
}
