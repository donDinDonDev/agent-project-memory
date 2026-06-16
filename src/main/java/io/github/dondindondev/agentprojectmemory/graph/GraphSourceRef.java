package io.github.dondindondev.agentprojectmemory.graph;

import java.util.Objects;

public record GraphSourceRef(
    String artifact,
    String section,
    String id) {
  public GraphSourceRef {
    artifact = requireText(artifact, "artifact");
    section = requireText(section, "section");
  }

  private static String requireText(String value, String fieldName) {
    Objects.requireNonNull(value, fieldName);
    if (value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " must not be blank.");
    }
    return value;
  }
}
