package io.github.dondindondev.agentprojectmemory.analyzer.maven;

import java.util.List;

public record MavenMetadataValue(
    String value,
    String valueKind,
    List<String> evidenceIds) {
  public MavenMetadataValue {
    evidenceIds = List.copyOf(evidenceIds);
  }

  public static MavenMetadataValue notDeclared() {
    return new MavenMetadataValue(null, "not_declared", List.of());
  }

  public static MavenMetadataValue unsupported(List<String> evidenceIds) {
    return new MavenMetadataValue(null, "unsupported", evidenceIds);
  }
}
