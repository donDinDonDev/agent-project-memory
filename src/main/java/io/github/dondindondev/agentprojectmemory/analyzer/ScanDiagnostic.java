package io.github.dondindondev.agentprojectmemory.analyzer;

public record ScanDiagnostic(
    String id,
    String severity,
    String code,
    String category,
    String message,
    String path,
    Integer count) {
}
