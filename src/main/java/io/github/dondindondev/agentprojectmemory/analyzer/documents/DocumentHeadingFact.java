package io.github.dondindondev.agentprojectmemory.analyzer.documents;

public record DocumentHeadingFact(
    String id,
    int level,
    String title,
    String anchor,
    int lineStart,
    int lineEnd) {
}
