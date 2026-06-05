package io.github.dondindondev.agentprojectmemory.analyzer.maven;

public record MavenMetadataParent(
    String analysisStatus,
    MavenMetadataValue groupId,
    MavenMetadataValue artifactId,
    MavenMetadataValue version,
    MavenMetadataValue relativePath) {
}
