package io.github.dondindondev.agentprojectmemory.graph;

public record ProjectGraphLimits(
    int maxNodes,
    int maxEdges,
    int maxRelationStatuses) {
  public static final ProjectGraphLimits DEFAULT = new ProjectGraphLimits(20_000, 50_000, 10_000);

  public ProjectGraphLimits {
    if (maxNodes < 0 || maxEdges < 0 || maxRelationStatuses < 0) {
      throw new IllegalArgumentException("Graph limits must be non-negative.");
    }
  }
}
