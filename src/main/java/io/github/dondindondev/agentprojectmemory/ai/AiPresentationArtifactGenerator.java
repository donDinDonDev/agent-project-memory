package io.github.dondindondev.agentprojectmemory.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

public final class AiPresentationArtifactGenerator {
  public static final String MANIFEST_PATH = "ai-presentations/manifest.json";
  public static final String BRIEF_PATH = "ai-presentations/brief.md";

  private static final ObjectMapper JSON = new ObjectMapper();
  private static final String AI_PRESENTATION_SCHEMA_VERSION = "1.0";
  private static final String PRESENTATION_SURFACE = "separate_artifact";
  private static final String AUTHORITY = "non_authoritative_presentation";
  private static final String EVIDENCE_POLICY = "references_existing_evidence_only";
  private static final String NETWORK_ACCESS = "disabled";
  private static final String SOURCE_UPLOAD = "disabled";
  private static final String PROMPT_TRANSCRIPT_STATUS = "not_serialized";

  private final AiPresentationProvider provider;

  public AiPresentationArtifactGenerator() {
    this(new MockNoNetworkAiPresentationProvider());
  }

  AiPresentationArtifactGenerator(AiPresentationProvider provider) {
    this.provider = Objects.requireNonNull(provider, "provider");
  }

  public AiPresentationArtifacts generate(
      AiPresentationOptions options,
      String projectMapJson,
      String evidenceIndexJsonl,
      String projectGraphJson) throws IOException {
    Objects.requireNonNull(options, "options");
    Objects.requireNonNull(projectMapJson, "projectMapJson");
    Objects.requireNonNull(evidenceIndexJsonl, "evidenceIndexJsonl");
    if (!options.enabled()) {
      throw new IllegalArgumentException("AI presentation options must be enabled.");
    }
    if (options.providerMode() != provider.providerMode()) {
      throw new IllegalArgumentException("Unsupported AI presentation provider mode.");
    }

    AiPresentationInput input = inputFromArtifacts(
        projectMapJson,
        evidenceIndexJsonl,
        projectGraphJson);
    return new AiPresentationArtifacts(
        manifestJson(options, input),
        provider.renderBrief(input));
  }

  private AiPresentationInput inputFromArtifacts(
      String projectMapJson,
      String evidenceIndexJsonl,
      String projectGraphJson) throws IOException {
    JsonNode projectMap = JSON.readTree(projectMapJson);
    JsonNode graph = projectGraphJson == null || projectGraphJson.isBlank()
        ? null
        : JSON.readTree(projectGraphJson);
    return new AiPresentationInput(
        text(projectMap, "schema_version", "not_recorded"),
        evidenceRecordCount(evidenceIndexJsonl),
        graph == null ? "not_recorded" : text(graph, "graph_schema_version", "not_recorded"),
        graph == null ? 0 : arraySize(graph.path("nodes")),
        graph == null ? 0 : arraySize(graph.path("edges")),
        arraySize(projectMap.path("endpoints")),
        arraySize(projectMap.path("components").path("items")),
        arraySize(projectMap.path("entities").path("items")),
        arraySize(projectMap.path("tests").path("items")),
        arraySize(projectMap.path("documents").path("items")),
        arraySize(projectMap.path("warnings").path("items")));
  }

  private int evidenceRecordCount(String evidenceIndexJsonl) {
    int count = 0;
    String[] lines = evidenceIndexJsonl.split("\\R", -1);
    for (String line : lines) {
      if (!line.isBlank()) {
        count++;
      }
    }
    return count;
  }

  private int arraySize(JsonNode node) {
    return node != null && node.isArray() ? node.size() : 0;
  }

  private String text(JsonNode node, String field, String fallback) {
    if (node == null) {
      return fallback;
    }
    JsonNode value = node.path(field);
    if (!value.isTextual() || value.asText().isBlank()) {
      return fallback;
    }
    return value.asText();
  }

  private String manifestJson(AiPresentationOptions options, AiPresentationInput input) {
    String providerMode = options.providerModeValue();
    StringBuilder json = new StringBuilder();
    json.append("{\n");
    field(json, 1, "ai_presentation_schema_version", AI_PRESENTATION_SCHEMA_VERSION, true);
    field(json, 1, "presentation_surface", PRESENTATION_SURFACE, true);
    field(json, 1, "provider_mode", providerMode, true);
    field(json, 1, "authority", AUTHORITY, true);
    field(json, 1, "evidence_policy", EVIDENCE_POLICY, true);
    field(json, 1, "network_access", NETWORK_ACCESS, true);
    field(json, 1, "source_upload", SOURCE_UPLOAD, true);
    field(json, 1, "prompt_transcript_status", PROMPT_TRANSCRIPT_STATUS, true);
    indent(json, 1);
    json.append("\"source_artifacts\": [\n");
    indent(json, 2);
    json.append("{\n");
    field(json, 3, "name", "project-map.json", true);
    field(json, 3, "schema_version", input.projectMapSchemaVersion(), false);
    indent(json, 2);
    json.append("},\n");
    indent(json, 2);
    json.append("{\n");
    field(json, 3, "name", "evidence-index.jsonl", true);
    intField(json, 3, "record_count", input.evidenceRecordCount(), false);
    indent(json, 2);
    json.append("},\n");
    indent(json, 2);
    json.append("{\n");
    field(json, 3, "name", "project-graph.json", true);
    field(json, 3, "graph_schema_version", input.graphSchemaVersion(), true);
    booleanField(json, 3, "required", false, false);
    indent(json, 2);
    json.append("}\n");
    indent(json, 1);
    json.append("],\n");
    indent(json, 1);
    json.append("\"generated_presentations\": [\n");
    indent(json, 2);
    json.append("{\n");
    field(json, 3, "name", "brief", true);
    field(json, 3, "artifact_path", BRIEF_PATH, true);
    field(json, 3, "content_kind", "ai_markdown_presentation", true);
    field(json, 3, "authority", AUTHORITY, true);
    field(json, 3, "evidence_policy", EVIDENCE_POLICY, true);
    field(json, 3, "provider_mode", providerMode, false);
    indent(json, 2);
    json.append("}\n");
    indent(json, 1);
    json.append("]\n");
    json.append("}\n");
    return json.toString();
  }

  private void field(
      StringBuilder json,
      int indentLevel,
      String name,
      String value,
      boolean trailingComma) {
    indent(json, indentLevel);
    json.append(jsonString(name)).append(": ").append(jsonString(value));
    lineEnding(json, trailingComma);
  }

  private void intField(
      StringBuilder json,
      int indentLevel,
      String name,
      int value,
      boolean trailingComma) {
    indent(json, indentLevel);
    json.append(jsonString(name)).append(": ").append(value);
    lineEnding(json, trailingComma);
  }

  private void booleanField(
      StringBuilder json,
      int indentLevel,
      String name,
      boolean value,
      boolean trailingComma) {
    indent(json, indentLevel);
    json.append(jsonString(name)).append(": ").append(value);
    lineEnding(json, trailingComma);
  }

  private void lineEnding(StringBuilder json, boolean trailingComma) {
    if (trailingComma) {
      json.append(",");
    }
    json.append("\n");
  }

  private void indent(StringBuilder json, int indentLevel) {
    json.append("  ".repeat(indentLevel));
  }

  private String jsonString(String value) {
    StringBuilder escaped = new StringBuilder();
    escaped.append('"');
    for (int index = 0; index < value.length(); index++) {
      char character = value.charAt(index);
      switch (character) {
        case '"' -> escaped.append("\\\"");
        case '\\' -> escaped.append("\\\\");
        case '\b' -> escaped.append("\\b");
        case '\f' -> escaped.append("\\f");
        case '\n' -> escaped.append("\\n");
        case '\r' -> escaped.append("\\r");
        case '\t' -> escaped.append("\\t");
        default -> {
          if (character < 0x20) {
            escaped.append(String.format(Locale.ROOT, "\\u%04x", (int) character));
          } else {
            escaped.append(character);
          }
        }
      }
    }
    escaped.append('"');
    return escaped.toString();
  }
}
