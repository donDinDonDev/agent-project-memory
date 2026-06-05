package io.github.dondindondev.agentprojectmemory.analyzer.maven;

public record MavenModuleMetadata(
    String moduleId,
    String analysisStatus,
    MavenMetadataValue groupId,
    MavenMetadataValue artifactId,
    MavenMetadataValue version,
    MavenMetadataValue packaging,
    MavenMetadataParent parent) {
}
