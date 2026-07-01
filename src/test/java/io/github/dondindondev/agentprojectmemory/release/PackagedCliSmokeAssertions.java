package io.github.dondindondev.agentprojectmemory.release;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PackagedCliSmokeAssertions {
  private static final ObjectMapper JSON = new ObjectMapper();

  private PackagedCliSmokeAssertions() {}

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      throw new IllegalArgumentException(
          "Expected one argument: path to the generated .project-memory artifact root");
    }
    assertArtifactSet(Path.of(args[0]).resolve("artifact-set.json"));
  }

  static void assertArtifactSet(Path artifactSetPath) throws IOException {
    if (!Files.isRegularFile(artifactSetPath)) {
      throw new IllegalStateException("Missing generated artifact-set.json: " + artifactSetPath);
    }

    JsonNode manifest = JSON.readTree(Files.readString(artifactSetPath));
    requireText(manifest, "artifact_set_schema_version", "1.0");
    requireText(manifest, "artifact_set_kind", "single_repository_scan");
    requireText(manifest, "contract_line", "v3_artifact_set_manifest_foundation");
    requireText(manifest, "artifact_root", ".project-memory");

    JsonNode evidenceBoundary = manifest.path("evidence_boundary");
    requireText(evidenceBoundary, "authority", "contract_provenance_metadata");
    requireText(evidenceBoundary, "evidence_policy", "manifest_is_not_evidence");
    requireText(evidenceBoundary, "evidence_artifact", "evidence-index.jsonl");

    JsonNode artifacts = manifest.path("artifacts");
    if (!artifacts.isArray()) {
      throw new IllegalStateException("artifact-set.json artifacts must be an array");
    }

    requireArtifact(
        artifactNamed(artifacts, "artifact-set.json"),
        "artifact_set_manifest",
        true,
        "present",
        "artifact_set_schema_version",
        "1.0",
        "contract_provenance_metadata",
        "non_evidence_metadata",
        false);
    requireArtifact(
        artifactNamed(artifacts, "project-map.json"),
        "project_map",
        true,
        "present",
        "schema_version",
        "1.0",
        "project_facts",
        "source_facts_reference_evidence_index",
        false);
    requireArtifact(
        artifactNamed(artifacts, "project-graph.json"),
        "project_graph",
        true,
        "present",
        "graph_schema_version",
        "1.0",
        "navigation_metadata",
        "non_evidence_derivation_metadata",
        false);
    requireArtifact(
        artifactNamed(artifacts, "evidence-index.jsonl"),
        "evidence_index",
        true,
        "present",
        null,
        null,
        "source_backed_evidence",
        "authoritative_evidence_index",
        true);
    requireArtifact(
        artifactNamed(artifacts, "endpoints.md"),
        "endpoints_markdown",
        true,
        "present",
        null,
        null,
        "deterministic_markdown_presentation",
        "references_existing_evidence",
        false);
    requireArtifact(
        artifactNamed(artifacts, "agent-guide.md"),
        "agent_guide_markdown",
        true,
        "present",
        null,
        null,
        "deterministic_markdown_presentation",
        "references_existing_evidence",
        false);
    requireOnlyEvidenceIndexIsAuthoritative(artifacts);
  }

  private static void requireArtifact(
      JsonNode artifact,
      String artifactKind,
      boolean required,
      String status,
      String schemaField,
      String schemaValue,
      String authority,
      String evidenceCategory,
      boolean authoritativeEvidence) {
    requireText(artifact, "artifact_kind", artifactKind);
    requireBoolean(artifact, "required", required);
    requireText(artifact, "status", status);
    requireText(artifact, "authority", authority);
    requireText(artifact, "evidence_category", evidenceCategory);
    requireBoolean(artifact, "authoritative_evidence", authoritativeEvidence);

    JsonNode schema = artifact.path("schema");
    if (schemaField == null) {
      if (!schema.isNull()) {
        throw new IllegalStateException(
            artifact.path("path").asText() + " schema must be null");
      }
      return;
    }
    requireText(schema, "field", schemaField);
    requireText(schema, "value", schemaValue);
  }

  private static JsonNode artifactNamed(JsonNode artifacts, String path) {
    for (JsonNode artifact : artifacts) {
      if (path.equals(artifact.path("path").asText())) {
        return artifact;
      }
    }
    throw new IllegalStateException("artifact-set.json missing artifact entry: " + path);
  }

  private static void requireOnlyEvidenceIndexIsAuthoritative(JsonNode artifacts) {
    for (JsonNode artifact : artifacts) {
      boolean authoritative = artifact.path("authoritative_evidence").asBoolean(false);
      if (authoritative && !"evidence-index.jsonl".equals(artifact.path("path").asText())) {
        throw new IllegalStateException(
            artifact.path("path").asText() + " must not be authoritative evidence");
      }
    }
  }

  private static void requireText(JsonNode node, String field, String expected) {
    String actual = node.path(field).asText(null);
    if (!expected.equals(actual)) {
      throw new IllegalStateException(
          "Expected " + field + " to be " + expected + " but was " + actual);
    }
  }

  private static void requireBoolean(JsonNode node, String field, boolean expected) {
    JsonNode value = node.path(field);
    if (!value.isBoolean() || value.asBoolean() != expected) {
      throw new IllegalStateException(
          "Expected " + field + " to be " + expected + " but was " + value);
    }
  }
}
