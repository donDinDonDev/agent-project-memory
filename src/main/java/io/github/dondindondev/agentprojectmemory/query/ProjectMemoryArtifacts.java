package io.github.dondindondev.agentprojectmemory.query;

import com.fasterxml.jackson.databind.JsonNode;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record ProjectMemoryArtifacts(
    Path artifactRoot,
    JsonNode projectMap,
    String projectMapSchemaVersion,
    List<JsonNode> evidenceRecords,
    Map<String, JsonNode> evidenceById,
    JsonNode projectGraph,
    String projectGraphSchemaVersion) {
  public ProjectMemoryArtifacts {
    artifactRoot = Objects.requireNonNull(artifactRoot, "artifactRoot");
    projectMap = Objects.requireNonNull(projectMap, "projectMap");
    projectMapSchemaVersion =
        Objects.requireNonNull(projectMapSchemaVersion, "projectMapSchemaVersion");
    evidenceRecords = List.copyOf(Objects.requireNonNull(evidenceRecords, "evidenceRecords"));
    evidenceById = Collections.unmodifiableMap(Objects.requireNonNull(evidenceById, "evidenceById"));
  }

  public boolean hasProjectGraph() {
    return projectGraph != null;
  }
}
