package io.github.dondindondev.agentprojectmemory.graph;

import java.util.List;
import java.util.Objects;

public record GraphDerivation(
    String kind,
    String artifact,
    String section,
    List<String> fields) {
  public GraphDerivation {
    kind = requireText(kind, "kind");
    artifact = requireText(artifact, "artifact");
    section = requireText(section, "section");
    fields = fields == null ? null : List.copyOf(fields);
  }

  public static GraphDerivation withoutFields(String kind, String artifact, String section) {
    return new GraphDerivation(kind, artifact, section, null);
  }

  private static String requireText(String value, String fieldName) {
    Objects.requireNonNull(value, fieldName);
    if (value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " must not be blank.");
    }
    return value;
  }
}
