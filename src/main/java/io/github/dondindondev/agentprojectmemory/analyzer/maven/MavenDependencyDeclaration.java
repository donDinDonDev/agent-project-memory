package io.github.dondindondev.agentprojectmemory.analyzer.maven;

import java.util.List;

public record MavenDependencyDeclaration(
    String id,
    String declarationKind,
    int declarationOrdinal,
    MavenMetadataValue groupId,
    MavenMetadataValue artifactId,
    MavenMetadataValue version,
    MavenMetadataValue scope,
    MavenMetadataValue optional,
    MavenMetadataValue type,
    MavenMetadataValue classifier,
    List<String> evidenceIds) {
  public MavenDependencyDeclaration {
    evidenceIds = List.copyOf(evidenceIds);
  }
}
