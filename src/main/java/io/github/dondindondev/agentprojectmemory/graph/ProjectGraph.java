package io.github.dondindondev.agentprojectmemory.graph;

import java.util.List;
import java.util.Objects;

public record ProjectGraph(
    String graphSchemaVersion,
    String projectMapSchemaVersion,
    String graphKind,
    List<String> sourceArtifacts,
    ProjectGraphLimits limits,
    List<GraphNode> nodes,
    List<GraphEdge> edges,
    List<GraphRelationStatus> relationStatuses,
    List<GraphWarning> warnings) {
  public ProjectGraph {
    graphSchemaVersion = requireText(graphSchemaVersion, "graphSchemaVersion");
    projectMapSchemaVersion = requireText(projectMapSchemaVersion, "projectMapSchemaVersion");
    graphKind = requireText(graphKind, "graphKind");
    sourceArtifacts = List.copyOf(Objects.requireNonNull(sourceArtifacts, "sourceArtifacts"));
    limits = Objects.requireNonNull(limits, "limits");
    nodes = List.copyOf(Objects.requireNonNull(nodes, "nodes"));
    edges = List.copyOf(Objects.requireNonNull(edges, "edges"));
    relationStatuses = List.copyOf(Objects.requireNonNull(relationStatuses, "relationStatuses"));
    warnings = List.copyOf(Objects.requireNonNull(warnings, "warnings"));
  }

  private static String requireText(String value, String fieldName) {
    Objects.requireNonNull(value, fieldName);
    if (value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " must not be blank.");
    }
    return value;
  }
}
