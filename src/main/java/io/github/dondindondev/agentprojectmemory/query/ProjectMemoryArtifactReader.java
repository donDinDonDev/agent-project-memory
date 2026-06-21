package io.github.dondindondev.agentprojectmemory.query;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.dondindondev.agentprojectmemory.analyzer.ScanPathContainment;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class ProjectMemoryArtifactReader {
  private static final String ARTIFACT_ROOT_NAME = ".project-memory";
  private static final String ARTIFACT_SET = "artifact-set.json";
  private static final String PROJECT_MAP = "project-map.json";
  private static final String EVIDENCE_INDEX = "evidence-index.jsonl";
  private static final String PROJECT_GRAPH = "project-graph.json";
  private static final String ENDPOINTS_MARKDOWN = "endpoints.md";
  private static final String AGENT_GUIDE_MARKDOWN = "agent-guide.md";
  private static final String SOURCE_REGISTRY = "source-registry.json";
  private static final String AGENT_PROFILE_MANIFEST = "agent-profiles/manifest.json";
  private static final String AI_PRESENTATION_MANIFEST = "ai-presentations/manifest.json";
  private static final String CACHE_MANIFEST = "cache/v1/manifest.json";
  private static final String WORKSPACE_MAP = "workspace-map.json";
  private static final Set<String> EXPECTED_ARTIFACT_SET_PATHS = Set.of(
      ARTIFACT_SET,
      PROJECT_MAP,
      PROJECT_GRAPH,
      EVIDENCE_INDEX,
      ENDPOINTS_MARKDOWN,
      AGENT_GUIDE_MARKDOWN,
      SOURCE_REGISTRY,
      AGENT_PROFILE_MANIFEST,
      AI_PRESENTATION_MANIFEST,
      CACHE_MANIFEST,
      WORKSPACE_MAP);
  private static final String SUPPORTED_ARTIFACT_SET_SCHEMA = "1.0";
  private static final String SUPPORTED_ARTIFACT_SET_KIND = "single_repository_scan";
  private static final String SUPPORTED_ARTIFACT_SET_CONTRACT_LINE =
      "v3_artifact_set_manifest_foundation";
  private static final String SUPPORTED_PROJECT_MAP_SCHEMA = "1.0";
  private static final String SUPPORTED_GRAPH_SCHEMA = "1.0";
  private static final int MAX_ARTIFACT_BYTES = 128 * 1024 * 1024;
  private static final int MAX_JSON_NESTING_DEPTH = 256;
  private static final int MAX_JSON_STRING_LENGTH = 1024 * 1024;
  private static final String ARTIFACT_STATUS_PRESENT = "present";
  private static final String ARTIFACT_STATUS_ABSENT = "absent";
  private static final String ARTIFACT_STATUS_MANAGED_SEPARATELY = "managed_separately";
  private static final String ARTIFACT_STATUS_INTENTIONALLY_OUT_OF_SCOPE =
      "intentionally_out_of_scope";
  private static final Set<String> ARTIFACT_STATUS_VALUES = Set.of(
      ARTIFACT_STATUS_PRESENT,
      ARTIFACT_STATUS_ABSENT,
      ARTIFACT_STATUS_MANAGED_SEPARATELY,
      ARTIFACT_STATUS_INTENTIONALLY_OUT_OF_SCOPE);
  private static final Set<String> SUPPORTED_SOURCE_REGISTRY_SCHEMAS = Set.of("1.0", "1.1", "1.2");
  private static final Set<String> EVIDENCE_FIELDS = Set.of(
      "id",
      "source_type",
      "path",
      "class_name",
      "method_name",
      "symbol_name",
      "line_start",
      "line_end",
      "excerpt",
      "confidence");
  private static final ObjectMapper JSON = new ObjectMapper(JsonFactory.builder()
      .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
      .streamReadConstraints(StreamReadConstraints.builder()
          .maxNestingDepth(MAX_JSON_NESTING_DEPTH)
          .maxStringLength(MAX_JSON_STRING_LENGTH)
          .build())
      .build());

  public ProjectMemoryArtifacts load(Path queryPath, GraphRequirement graphRequirement)
      throws QueryArtifactException {
    Objects.requireNonNull(graphRequirement, "graphRequirement");
    Path artifactRoot = resolveArtifactRoot(queryPath);
    Optional<ArtifactSetManifest> artifactSet = readArtifactSetManifest(artifactRoot);
    JsonNode projectMap = readJson(requiredArtifact(artifactRoot, PROJECT_MAP), PROJECT_MAP);
    String projectMapSchema = supportedSchema(
        projectMap,
        PROJECT_MAP,
        "schema_version",
        SUPPORTED_PROJECT_MAP_SCHEMA);
    if (artifactSet.isPresent()
        && !projectMapSchema.equals(artifactSet.orElseThrow().projectMapSchemaVersion())) {
      throw new QueryArtifactException(
          "Mixed artifact set: artifact-set.json project-map schema does not match project-map.json.");
    }
    EvidenceIndex evidenceIndex = readEvidenceIndex(requiredArtifact(artifactRoot, EVIDENCE_INDEX));
    validateProjectMapEvidenceReferences(projectMap, evidenceIndex.ids());
    JsonNode projectGraph = null;
    String graphSchema = null;
    if (graphRequirement != GraphRequirement.NONE || artifactSet.isPresent()) {
      Optional<Path> graphPath = artifactSet.isPresent()
          ? Optional.of(requiredArtifact(artifactRoot, PROJECT_GRAPH))
          : optionalArtifact(artifactRoot, PROJECT_GRAPH);
      if (graphPath.isEmpty()) {
        if (graphRequirement == GraphRequirement.REQUIRED || artifactSet.isPresent()) {
          throw new QueryArtifactException("Missing project-graph.json.");
        }
      } else {
        JsonNode validatedGraph = readJson(graphPath.orElseThrow(), PROJECT_GRAPH);
        String validatedGraphSchema = validateGraph(validatedGraph, evidenceIndex.ids());
        if (artifactSet.isPresent()
            && !validatedGraphSchema.equals(artifactSet.orElseThrow().projectGraphSchemaVersion())) {
          throw new QueryArtifactException(
              "Mixed artifact set: artifact-set.json graph schema does not match project-graph.json.");
        }
        if (graphRequirement != GraphRequirement.NONE) {
          projectGraph = validatedGraph;
          graphSchema = validatedGraphSchema;
        }
      }
    }
    return new ProjectMemoryArtifacts(
        artifactRoot,
        projectMap,
        projectMapSchema,
        evidenceIndex.records(),
        evidenceIndex.byId(),
        projectGraph,
        graphSchema);
  }

  private Path resolveArtifactRoot(Path queryPath) throws QueryArtifactException {
    if (queryPath == null) {
      throw new QueryArtifactException("Missing query path.");
    }
    Path inputPath = queryPath.toAbsolutePath().normalize();
    if (Files.notExists(inputPath, LinkOption.NOFOLLOW_LINKS)) {
      throw new QueryArtifactException("Query path does not exist.");
    }
    if (Files.isSymbolicLink(inputPath)) {
      throw new QueryArtifactException("Query path must not be a symbolic link.");
    }
    if (!Files.isDirectory(inputPath, LinkOption.NOFOLLOW_LINKS)) {
      throw new QueryArtifactException("Query path is not a directory.");
    }

    Path normalizedPath;
    try {
      normalizedPath = inputPath.toRealPath().normalize();
    } catch (IOException exception) {
      throw new QueryArtifactException("Query path does not exist.");
    }
    Path fileName = normalizedPath.getFileName();
    boolean directArtifactRoot = fileName != null && ARTIFACT_ROOT_NAME.equals(fileName.toString());
    Path artifactRoot = directArtifactRoot
        ? normalizedPath
        : normalizedPath.resolve(ARTIFACT_ROOT_NAME).normalize();

    if (Files.notExists(artifactRoot, LinkOption.NOFOLLOW_LINKS)) {
      throw new QueryArtifactException("Missing .project-memory artifact root.");
    }
    if (Files.isSymbolicLink(artifactRoot)
        || (!directArtifactRoot && hasSymbolicLinkSegment(normalizedPath, artifactRoot))) {
      throw new QueryArtifactException("Query artifact root must not be a symbolic link.");
    }
    if (!Files.isDirectory(artifactRoot, LinkOption.NOFOLLOW_LINKS)) {
      throw new QueryArtifactException("Query artifact root is not a directory.");
    }
    return artifactRoot;
  }

  private Optional<ArtifactSetManifest> readArtifactSetManifest(Path artifactRoot)
      throws QueryArtifactException {
    Optional<Path> manifestPath = optionalArtifact(artifactRoot, ARTIFACT_SET);
    if (manifestPath.isEmpty()) {
      return Optional.empty();
    }
    JsonNode manifest = readJson(manifestPath.orElseThrow(), ARTIFACT_SET);
    supportedSchema(
        manifest,
        ARTIFACT_SET,
        "artifact_set_schema_version",
        SUPPORTED_ARTIFACT_SET_SCHEMA);
    requireSupportedText(
        manifest,
        "artifact_set_kind",
        SUPPORTED_ARTIFACT_SET_KIND,
        "Unsupported artifact-set.json artifact_set_kind.");
    requireSupportedText(
        manifest,
        "contract_line",
        SUPPORTED_ARTIFACT_SET_CONTRACT_LINE,
        "Unsupported artifact-set.json contract_line.");
    requireSupportedText(
        manifest,
        "artifact_root",
        ARTIFACT_ROOT_NAME,
        "Unsupported artifact-set.json artifact_root.");
    validateArtifactSetEvidenceBoundary(manifest);

    Map<String, JsonNode> items = artifactInventoryByPath(manifest.path("artifacts"));
    String projectMapSchema = requiredItemSchemaValue(
        items,
        PROJECT_MAP,
        "schema_version",
        true,
        ARTIFACT_STATUS_PRESENT);
    if (!SUPPORTED_PROJECT_MAP_SCHEMA.equals(projectMapSchema)) {
      throw new QueryArtifactException("Unsupported artifact-set.json project-map schema_version.");
    }
    String graphSchema = requiredItemSchemaValue(
        items,
        PROJECT_GRAPH,
        "graph_schema_version",
        true,
        ARTIFACT_STATUS_PRESENT);
    if (!SUPPORTED_GRAPH_SCHEMA.equals(graphSchema)) {
      throw new QueryArtifactException("Unsupported artifact-set.json graph_schema_version.");
    }

    String artifactSetItemSchema = requiredItemSchemaValue(
        items,
        ARTIFACT_SET,
        "artifact_set_schema_version",
        true,
        ARTIFACT_STATUS_PRESENT);
    if (!SUPPORTED_ARTIFACT_SET_SCHEMA.equals(artifactSetItemSchema)) {
      throw new QueryArtifactException("Unsupported artifact-set.json artifact_set_schema_version.");
    }
    requireNoItemSchema(items, EVIDENCE_INDEX, true, ARTIFACT_STATUS_PRESENT);
    requireNoItemSchema(items, ENDPOINTS_MARKDOWN, true, ARTIFACT_STATUS_PRESENT);
    requireNoItemSchema(items, AGENT_GUIDE_MARKDOWN, true, ARTIFACT_STATUS_PRESENT);
    SourceRegistryManifest sourceRegistry = sourceRegistryManifest(items);
    if (ARTIFACT_STATUS_PRESENT.equals(sourceRegistry.status())) {
      throw new QueryArtifactException("Unsupported artifact-set.json source-registry status.");
    }
    OptionalSurfaceManifest agentProfile = optionalSurfaceManifest(
        items,
        AGENT_PROFILE_MANIFEST,
        "manifest_version",
        Set.of("1.0"));
    OptionalSurfaceManifest aiPresentation = optionalSurfaceManifest(
        items,
        AI_PRESENTATION_MANIFEST,
        "ai_presentation_schema_version",
        Set.of("1.0"));
    requireNoItemSchema(items, CACHE_MANIFEST, false, ARTIFACT_STATUS_MANAGED_SEPARATELY);
    requireNoItemSchema(
        items,
        WORKSPACE_MAP,
        false,
        ARTIFACT_STATUS_INTENTIONALLY_OUT_OF_SCOPE);

    String artifactSetId = requiredText(
        manifest,
        "artifact_set_id",
        "Malformed artifact-set.json.");
    String expectedArtifactSetId = artifactSetId(
        projectMapSchema,
        sourceRegistry.schemaVersion(),
        agentProfile.status(),
        aiPresentation.status());
    if (!expectedArtifactSetId.equals(artifactSetId)) {
      throw new QueryArtifactException(
          "Mixed artifact set: artifact-set.json identity does not match artifact inventory.");
    }

    validateArtifactSetPresence(artifactRoot, sourceRegistry, agentProfile, aiPresentation);
    return Optional.of(new ArtifactSetManifest(projectMapSchema, graphSchema));
  }

  private void validateArtifactSetEvidenceBoundary(JsonNode manifest) throws QueryArtifactException {
    JsonNode boundary = manifest.path("evidence_boundary");
    if (!boundary.isObject()) {
      throw new QueryArtifactException("Malformed artifact-set.json.");
    }
    requireSupportedText(
        boundary,
        "authority",
        "contract_provenance_metadata",
        "Unsupported artifact-set.json evidence_boundary.");
    requireSupportedText(
        boundary,
        "evidence_policy",
        "manifest_is_not_evidence",
        "Unsupported artifact-set.json evidence_boundary.");
    requireSupportedText(
        boundary,
        "evidence_artifact",
        EVIDENCE_INDEX,
        "Unsupported artifact-set.json evidence_boundary.");
  }

  private Map<String, JsonNode> artifactInventoryByPath(JsonNode artifacts)
      throws QueryArtifactException {
    if (!artifacts.isArray()) {
      throw new QueryArtifactException("Malformed artifact-set.json.");
    }
    Map<String, JsonNode> items = new LinkedHashMap<>();
    for (JsonNode item : artifacts) {
      if (!item.isObject()) {
        throw new QueryArtifactException("Malformed artifact-set.json.");
      }
      JsonNode path = item.path("path");
      if (!path.isTextual()
          || !isSafeArtifactRelativePath(path.asText())
          || !EXPECTED_ARTIFACT_SET_PATHS.contains(path.asText())) {
        throw new QueryArtifactException("Malformed artifact-set.json.");
      }
      if (items.putIfAbsent(path.asText(), item) != null) {
        throw new QueryArtifactException("Malformed artifact-set.json.");
      }
    }
    return Map.copyOf(items);
  }

  private String requiredItemSchemaValue(
      Map<String, JsonNode> items,
      String path,
      String schemaField,
      boolean required,
      String status) throws QueryArtifactException {
    JsonNode item = requiredManifestItem(items, path, required, status);
    JsonNode schema = item.path("schema");
    if (!schema.isObject()) {
      throw new QueryArtifactException("Malformed artifact-set.json.");
    }
    requireSupportedText(
        schema,
        "field",
        schemaField,
        "Malformed artifact-set.json.");
    JsonNode value = schema.path("value");
    if (!value.isTextual() || value.asText().isBlank()) {
      throw new QueryArtifactException("Malformed artifact-set.json.");
    }
    return value.asText();
  }

  private void requireNoItemSchema(
      Map<String, JsonNode> items,
      String path,
      boolean required,
      String status) throws QueryArtifactException {
    JsonNode item = requiredManifestItem(items, path, required, status);
    if (!item.path("schema").isNull()) {
      throw new QueryArtifactException("Malformed artifact-set.json.");
    }
  }

  private JsonNode requiredManifestItem(
      Map<String, JsonNode> items,
      String path,
      boolean required,
      String status) throws QueryArtifactException {
    JsonNode item = items.get(path);
    if (item == null) {
      throw new QueryArtifactException("Malformed artifact-set.json.");
    }
    JsonNode requiredNode = item.path("required");
    if (!requiredNode.isBoolean() || requiredNode.asBoolean() != required) {
      throw new QueryArtifactException("Malformed artifact-set.json.");
    }
    requireSupportedText(
        item,
        "status",
        status,
        "Malformed artifact-set.json.");
    return item;
  }

  private SourceRegistryManifest sourceRegistryManifest(Map<String, JsonNode> items)
      throws QueryArtifactException {
    JsonNode item = items.get(SOURCE_REGISTRY);
    if (item == null) {
      throw new QueryArtifactException("Malformed artifact-set.json.");
    }
    JsonNode requiredNode = item.path("required");
    if (!requiredNode.isBoolean() || requiredNode.asBoolean()) {
      throw new QueryArtifactException("Malformed artifact-set.json.");
    }
    String status = requiredArtifactStatus(item);
    if (ARTIFACT_STATUS_ABSENT.equals(status)) {
      if (!item.path("schema").isNull()) {
        throw new QueryArtifactException("Malformed artifact-set.json.");
      }
      return new SourceRegistryManifest(status, null);
    }
    if (!ARTIFACT_STATUS_PRESENT.equals(status)) {
      throw new QueryArtifactException("Malformed artifact-set.json.");
    }
    JsonNode schema = item.path("schema");
    if (!schema.isObject()) {
      throw new QueryArtifactException("Malformed artifact-set.json.");
    }
    requireSupportedText(
        schema,
        "field",
        "source_registry_schema_version",
        "Malformed artifact-set.json.");
    String schemaVersion = requiredText(schema, "value", "Malformed artifact-set.json.");
    if (!SUPPORTED_SOURCE_REGISTRY_SCHEMAS.contains(schemaVersion)) {
      throw new QueryArtifactException(
          "Unsupported artifact-set.json source_registry_schema_version.");
    }
    return new SourceRegistryManifest(status, schemaVersion);
  }

  private OptionalSurfaceManifest optionalSurfaceManifest(
      Map<String, JsonNode> items,
      String path,
      String schemaField,
      Set<String> supportedValues) throws QueryArtifactException {
    JsonNode item = items.get(path);
    if (item == null) {
      throw new QueryArtifactException("Malformed artifact-set.json.");
    }
    JsonNode requiredNode = item.path("required");
    if (!requiredNode.isBoolean() || requiredNode.asBoolean()) {
      throw new QueryArtifactException("Malformed artifact-set.json.");
    }
    String status = requiredArtifactStatus(item);
    if (ARTIFACT_STATUS_ABSENT.equals(status)) {
      if (!item.path("schema").isNull()) {
        throw new QueryArtifactException("Malformed artifact-set.json.");
      }
      return new OptionalSurfaceManifest(status);
    }
    if (!ARTIFACT_STATUS_PRESENT.equals(status)) {
      throw new QueryArtifactException("Malformed artifact-set.json.");
    }
    JsonNode schema = item.path("schema");
    if (!schema.isObject()) {
      throw new QueryArtifactException("Malformed artifact-set.json.");
    }
    requireSupportedText(schema, "field", schemaField, "Malformed artifact-set.json.");
    String schemaVersion = requiredText(schema, "value", "Malformed artifact-set.json.");
    if (!supportedValues.contains(schemaVersion)) {
      throw new QueryArtifactException("Unsupported artifact-set.json optional surface schema.");
    }
    return new OptionalSurfaceManifest(status);
  }

  private String requiredArtifactStatus(JsonNode item) throws QueryArtifactException {
    String status = requiredText(item, "status", "Malformed artifact-set.json.");
    if (!ARTIFACT_STATUS_VALUES.contains(status)) {
      throw new QueryArtifactException("Malformed artifact-set.json.");
    }
    return status;
  }

  private void validateArtifactSetPresence(
      Path artifactRoot,
      SourceRegistryManifest sourceRegistry,
      OptionalSurfaceManifest agentProfile,
      OptionalSurfaceManifest aiPresentation) throws QueryArtifactException {
    requiredArtifact(artifactRoot, PROJECT_MAP);
    requiredArtifact(artifactRoot, PROJECT_GRAPH);
    requiredArtifact(artifactRoot, EVIDENCE_INDEX);
    requiredArtifact(artifactRoot, ENDPOINTS_MARKDOWN);
    requiredArtifact(artifactRoot, AGENT_GUIDE_MARKDOWN);
    validateOptionalArtifactPresence(artifactRoot, SOURCE_REGISTRY, sourceRegistry.status());
    validateOptionalArtifactPresence(artifactRoot, AGENT_PROFILE_MANIFEST, agentProfile.status());
    validateOptionalArtifactPresence(artifactRoot, AI_PRESENTATION_MANIFEST, aiPresentation.status());
    if (optionalArtifact(artifactRoot, WORKSPACE_MAP).isPresent()) {
      throw new QueryArtifactException(
          "Mixed artifact set: workspace-map.json presence does not match artifact-set.json.");
    }
  }

  private void validateOptionalArtifactPresence(Path artifactRoot, String path, String status)
      throws QueryArtifactException {
    Optional<Path> artifact = optionalArtifact(artifactRoot, path);
    if (ARTIFACT_STATUS_PRESENT.equals(status) && artifact.isEmpty()) {
      throw new QueryArtifactException(
          "Mixed artifact set: " + path + " presence does not match artifact-set.json.");
    }
    if (ARTIFACT_STATUS_ABSENT.equals(status) && artifact.isPresent()) {
      throw new QueryArtifactException(
          "Mixed artifact set: " + path + " presence does not match artifact-set.json.");
    }
  }

  private String artifactSetId(
      String projectMapSchemaVersion,
      String sourceRegistrySchemaVersion,
      String agentProfileStatus,
      String aiPresentationStatus) {
    String sourceRegistryMarker = sourceRegistrySchemaVersion == null
        ? ARTIFACT_STATUS_ABSENT
        : sourceRegistrySchemaVersion;
    return "artifact-set:single-repository-scan"
        + ":project-map-" + projectMapSchemaVersion
        + ":source-registry-" + sourceRegistryMarker
        + ":agent-profiles-" + agentProfileStatus
        + ":ai-presentations-" + aiPresentationStatus
        + ":cache-managed-separately"
        + ":workspace-out-of-scope";
  }

  private Path requiredArtifact(Path artifactRoot, String fileName) throws QueryArtifactException {
    Path artifact = artifactRoot.resolve(fileName).normalize();
    if (!artifact.startsWith(artifactRoot)) {
      throw new QueryArtifactException("Invalid artifact path.");
    }
    if (Files.notExists(artifact, LinkOption.NOFOLLOW_LINKS)) {
      throw new QueryArtifactException("Missing " + fileName + ".");
    }
    return existingArtifactFile(artifactRoot, artifact, fileName);
  }

  private Optional<Path> optionalArtifact(Path artifactRoot, String fileName) throws QueryArtifactException {
    Path artifact = artifactRoot.resolve(fileName).normalize();
    if (!artifact.startsWith(artifactRoot)) {
      throw new QueryArtifactException("Invalid artifact path.");
    }
    if (Files.notExists(artifact, LinkOption.NOFOLLOW_LINKS)) {
      return Optional.empty();
    }
    return Optional.of(existingArtifactFile(artifactRoot, artifact, fileName));
  }

  private Path existingArtifactFile(Path artifactRoot, Path artifact, String fileName)
      throws QueryArtifactException {
    if (Files.isSymbolicLink(artifact) || hasSymbolicLinkSegment(artifactRoot, artifact)) {
      throw new QueryArtifactException(fileName + " must not be a symbolic link.");
    }
    if (!ScanPathContainment.isTrustedRegularFileNoFollow(artifact)) {
      throw new QueryArtifactException(fileName + " is not a regular file.");
    }
    return artifact;
  }

  private JsonNode readJson(Path artifact, String artifactName) throws QueryArtifactException {
    try {
      JsonNode root = JSON.readTree(readUtf8(artifact, artifactName));
      if (root == null || !root.isObject()) {
        throw new QueryArtifactException("Malformed " + artifactName + ".");
      }
      return root;
    } catch (JsonProcessingException exception) {
      throw new QueryArtifactException("Malformed " + artifactName + ".");
    } catch (IOException exception) {
      throw new QueryArtifactException("Could not read " + artifactName + ".");
    }
  }

  private String readUtf8(Path artifact, String artifactName) throws IOException, QueryArtifactException {
    try {
      return new String(
          ScanPathContainment.readRegularFileBytesNoFollowStable(artifact, MAX_ARTIFACT_BYTES),
          StandardCharsets.UTF_8);
    } catch (ScanPathContainment.FileSizeLimitExceededException exception) {
      throw new QueryArtifactException(artifactName + " exceeds maximum supported size.");
    }
  }

  private EvidenceIndex readEvidenceIndex(Path evidencePath) throws QueryArtifactException {
    String content;
    try {
      content = readUtf8(evidencePath, EVIDENCE_INDEX);
    } catch (IOException exception) {
      throw new QueryArtifactException("Could not read evidence-index.jsonl.");
    }

    List<JsonNode> records = new java.util.ArrayList<>();
    Map<String, JsonNode> byId = new LinkedHashMap<>();
    for (String line : content.lines().toList()) {
      if (line.isBlank()) {
        throw new QueryArtifactException("Malformed evidence-index.jsonl.");
      }
      JsonNode record;
      try {
        record = JSON.readTree(line);
      } catch (JsonProcessingException exception) {
        throw new QueryArtifactException("Malformed evidence-index.jsonl.");
      }
      if (record == null || !record.isObject() || !hasOnlyFields(record, EVIDENCE_FIELDS)) {
        throw new QueryArtifactException("Malformed evidence-index.jsonl.");
      }
      JsonNode idNode = record.path("id");
      if (!idNode.isTextual() || idNode.asText().isBlank()) {
        throw new QueryArtifactException("Malformed evidence-index.jsonl.");
      }
      String id = idNode.asText();
      if (byId.putIfAbsent(id, record) != null) {
        throw new QueryArtifactException("Duplicate evidence id in evidence-index.jsonl.");
      }
      JsonNode pathNode = record.path("path");
      if (!pathNode.isTextual() || !isSafeRepositoryRelativePath(pathNode.asText())) {
        throw new QueryArtifactException("Malformed evidence-index.jsonl path.");
      }
      records.add(record);
    }
    return new EvidenceIndex(List.copyOf(records), Map.copyOf(byId), Set.copyOf(byId.keySet()));
  }

  private void validateProjectMapEvidenceReferences(JsonNode projectMap, Set<String> evidenceIds)
      throws QueryArtifactException {
    if (projectMap.isObject()) {
      Iterator<Map.Entry<String, JsonNode>> fields = projectMap.fields();
      while (fields.hasNext()) {
        Map.Entry<String, JsonNode> field = fields.next();
        if (isEvidenceIdField(field.getKey())) {
          validateProjectMapEvidenceIdsArray(field.getValue(), evidenceIds);
        } else {
          validateProjectMapEvidenceReferences(field.getValue(), evidenceIds);
        }
      }
      return;
    }
    if (projectMap.isArray()) {
      for (JsonNode item : projectMap) {
        validateProjectMapEvidenceReferences(item, evidenceIds);
      }
    }
  }

  private void validateProjectMapEvidenceIdsArray(JsonNode value, Set<String> evidenceIds)
      throws QueryArtifactException {
    if (!value.isArray()) {
      throw new QueryArtifactException("Invalid project-map.json evidence reference.");
    }
    for (JsonNode id : value) {
      if (!id.isTextual() || !evidenceIds.contains(id.asText())) {
        throw new QueryArtifactException("Invalid project-map.json evidence reference.");
      }
    }
  }

  private boolean isEvidenceIdField(String fieldName) {
    return "evidence_ids".equals(fieldName) || fieldName.endsWith("_evidence_ids");
  }

  private String validateGraph(JsonNode graph, Set<String> evidenceIds) throws QueryArtifactException {
    String graphSchema = supportedSchema(
        graph,
        PROJECT_GRAPH,
        "graph_schema_version",
        SUPPORTED_GRAPH_SCHEMA);
    JsonNode projectMapSchema = graph.path("project_map_schema_version");
    if (!projectMapSchema.isTextual()
        || !SUPPORTED_PROJECT_MAP_SCHEMA.equals(projectMapSchema.asText())) {
      throw new QueryArtifactException("Unsupported project-graph.json project_map_schema_version.");
    }

    JsonNode nodes = requiredArray(graph, "nodes");
    JsonNode edges = requiredArray(graph, "edges");
    JsonNode relationStatuses = requiredArray(graph, "relation_statuses");
    JsonNode warnings = requiredArray(graph, "warnings");

    Set<String> nodeIds = new HashSet<>();
    Set<String> graphIds = new HashSet<>();
    collectGraphIds(nodes, nodeIds, graphIds);
    collectGraphIds(edges, null, graphIds);
    collectGraphIds(relationStatuses, null, graphIds);
    collectGraphIds(warnings, null, graphIds);
    validateEdgeReferences(edges, nodeIds);
    validateRelationStatusReferences(relationStatuses, nodeIds);
    validateEvidenceReferences(graph, evidenceIds);
    return graphSchema;
  }

  private String supportedSchema(
      JsonNode root,
      String artifactName,
      String fieldName,
      String supportedValue) throws QueryArtifactException {
    JsonNode schema = root.path(fieldName);
    if (!schema.isTextual() || !supportedValue.equals(schema.asText())) {
      throw new QueryArtifactException("Unsupported " + artifactName + " " + fieldName + ".");
    }
    return schema.asText();
  }

  private void requireSupportedText(
      JsonNode root,
      String fieldName,
      String supportedValue,
      String errorMessage) throws QueryArtifactException {
    JsonNode value = root.path(fieldName);
    if (!value.isTextual() || !supportedValue.equals(value.asText())) {
      throw new QueryArtifactException(errorMessage);
    }
  }

  private JsonNode requiredArray(JsonNode root, String fieldName) throws QueryArtifactException {
    JsonNode value = root.path(fieldName);
    if (!value.isArray()) {
      throw new QueryArtifactException("Malformed project-graph.json.");
    }
    return value;
  }

  private void collectGraphIds(JsonNode rows, Set<String> nodeIds, Set<String> graphIds)
      throws QueryArtifactException {
    for (JsonNode row : rows) {
      if (!row.isObject()) {
        throw new QueryArtifactException("Malformed project-graph.json.");
      }
      JsonNode id = row.path("id");
      if (!id.isTextual() || id.asText().isBlank()) {
        throw new QueryArtifactException("Malformed project-graph.json.");
      }
      String idText = id.asText();
      if (!graphIds.add(idText)) {
        throw new QueryArtifactException("Duplicate graph id in project-graph.json.");
      }
      if (nodeIds != null) {
        nodeIds.add(idText);
      }
    }
  }

  private void validateEdgeReferences(JsonNode edges, Set<String> nodeIds) throws QueryArtifactException {
    for (JsonNode edge : edges) {
      String sourceId = requiredText(edge, "source_id");
      String targetId = requiredText(edge, "target_id");
      if (!nodeIds.contains(sourceId) || !nodeIds.contains(targetId)) {
        throw new QueryArtifactException("Invalid project-graph.json graph reference.");
      }
    }
  }

  private void validateRelationStatusReferences(JsonNode statuses, Set<String> nodeIds)
      throws QueryArtifactException {
    for (JsonNode status : statuses) {
      String sourceId = nullableText(status, "source_id");
      String targetId = nullableText(status, "target_id");
      if ((sourceId != null && !nodeIds.contains(sourceId))
          || (targetId != null && !nodeIds.contains(targetId))) {
        throw new QueryArtifactException("Invalid project-graph.json graph reference.");
      }
    }
  }

  private void validateEvidenceReferences(JsonNode node, Set<String> evidenceIds)
      throws QueryArtifactException {
    if (node.isObject()) {
      Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
      while (fields.hasNext()) {
        Map.Entry<String, JsonNode> field = fields.next();
        if ("evidence_ids".equals(field.getKey())) {
          validateEvidenceIdsArray(field.getValue(), evidenceIds);
        } else {
          validateEvidenceReferences(field.getValue(), evidenceIds);
        }
      }
    } else if (node.isArray()) {
      for (JsonNode item : node) {
        validateEvidenceReferences(item, evidenceIds);
      }
    }
  }

  private void validateEvidenceIdsArray(JsonNode value, Set<String> evidenceIds)
      throws QueryArtifactException {
    if (!value.isArray()) {
      throw new QueryArtifactException("Malformed project-graph.json.");
    }
    for (JsonNode id : value) {
      if (!id.isTextual() || !evidenceIds.contains(id.asText())) {
        throw new QueryArtifactException("Invalid project-graph.json evidence reference.");
      }
    }
  }

  private String requiredText(JsonNode node, String fieldName) throws QueryArtifactException {
    return requiredText(node, fieldName, "Malformed project-graph.json.");
  }

  private String requiredText(JsonNode node, String fieldName, String errorMessage)
      throws QueryArtifactException {
    JsonNode value = node.path(fieldName);
    if (!value.isTextual() || value.asText().isBlank()) {
      throw new QueryArtifactException(errorMessage);
    }
    return value.asText();
  }

  private String nullableText(JsonNode node, String fieldName) throws QueryArtifactException {
    JsonNode value = node.path(fieldName);
    if (value.isNull()) {
      return null;
    }
    if (!value.isTextual() || value.asText().isBlank()) {
      throw new QueryArtifactException("Malformed project-graph.json.");
    }
    return value.asText();
  }

  private boolean hasOnlyFields(JsonNode node, Set<String> expectedFields) {
    Set<String> actual = new HashSet<>();
    Iterator<String> names = node.fieldNames();
    while (names.hasNext()) {
      actual.add(names.next());
    }
    return actual.equals(expectedFields);
  }

  private boolean hasSymbolicLinkSegment(Path root, Path path) {
    Path normalizedRoot = root.toAbsolutePath().normalize();
    Path normalizedPath = path.toAbsolutePath().normalize();
    if (!normalizedPath.startsWith(normalizedRoot)) {
      return true;
    }
    Path current = normalizedRoot;
    for (Path segment : normalizedRoot.relativize(normalizedPath)) {
      current = current.resolve(segment);
      if (Files.isSymbolicLink(current)) {
        return true;
      }
    }
    return false;
  }

  private boolean isSafeRepositoryRelativePath(String path) {
    if (path == null
        || path.isBlank()
        || !path.equals(path.strip())
        || path.startsWith("/")
        || path.startsWith("./")
        || path.contains("\\")
        || path.indexOf('\n') >= 0
        || path.indexOf('\r') >= 0
        || ".project-memory".equals(path)
        || path.startsWith(".project-memory/")
        || path.matches("^[A-Za-z]:[/\\\\].*")
        || path.matches("^[A-Za-z][A-Za-z0-9+.-]*://.*")
        || path.regionMatches(true, 0, "file:", 0, "file:".length())) {
      return false;
    }
    try {
      if (Path.of(path).isAbsolute()) {
        return false;
      }
    } catch (RuntimeException exception) {
      return false;
    }
    for (String segment : path.split("/", -1)) {
      if (segment.isBlank() || ".".equals(segment) || "..".equals(segment)) {
        return false;
      }
    }
    return true;
  }

  private boolean isSafeArtifactRelativePath(String path) {
    if (path == null
        || path.isBlank()
        || !path.equals(path.strip())
        || path.startsWith("/")
        || path.startsWith("./")
        || path.contains("\\")
        || path.indexOf('\n') >= 0
        || path.indexOf('\r') >= 0
        || ".project-memory".equals(path)
        || path.startsWith(".project-memory/")
        || path.matches("^[A-Za-z]:[/\\\\].*")
        || path.matches("^[A-Za-z][A-Za-z0-9+.-]*://.*")
        || path.regionMatches(true, 0, "file:", 0, "file:".length())) {
      return false;
    }
    try {
      if (Path.of(path).isAbsolute()) {
        return false;
      }
    } catch (RuntimeException exception) {
      return false;
    }
    for (String segment : path.split("/", -1)) {
      if (segment.isBlank() || ".".equals(segment) || "..".equals(segment)) {
        return false;
      }
    }
    return true;
  }

  public enum GraphRequirement {
    NONE,
    OPTIONAL,
    REQUIRED
  }

  private record EvidenceIndex(
      List<JsonNode> records,
      Map<String, JsonNode> byId,
      Set<String> ids) {
  }

  private record ArtifactSetManifest(
      String projectMapSchemaVersion,
      String projectGraphSchemaVersion) {
  }

  private record SourceRegistryManifest(
      String status,
      String schemaVersion) {
  }

  private record OptionalSurfaceManifest(String status) {
  }
}
