package io.github.dondindondev.agentprojectmemory.ai;

public record AiPresentationInput(
    String projectMapSchemaVersion,
    int evidenceRecordCount,
    String graphSchemaVersion,
    int graphNodeCount,
    int graphEdgeCount,
    int endpointCount,
    int componentCount,
    int entityCount,
    int testCount,
    int documentCount,
    int warningCount) {
  public AiPresentationInput {
    projectMapSchemaVersion = notBlankOr(projectMapSchemaVersion, "not_recorded");
    graphSchemaVersion = notBlankOr(graphSchemaVersion, "not_recorded");
  }

  private static String notBlankOr(String value, String fallback) {
    return value == null || value.isBlank() ? fallback : value;
  }
}
