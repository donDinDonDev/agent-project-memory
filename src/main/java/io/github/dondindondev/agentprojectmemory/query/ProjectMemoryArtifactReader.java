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
  private static final String PROJECT_MAP = "project-map.json";
  private static final String EVIDENCE_INDEX = "evidence-index.jsonl";
  private static final String PROJECT_GRAPH = "project-graph.json";
  private static final String SUPPORTED_PROJECT_MAP_SCHEMA = "1.0";
  private static final String SUPPORTED_GRAPH_SCHEMA = "1.0";
  private static final int MAX_ARTIFACT_BYTES = 128 * 1024 * 1024;
  private static final int MAX_JSON_NESTING_DEPTH = 256;
  private static final int MAX_JSON_STRING_LENGTH = 1024 * 1024;
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
    JsonNode projectMap = readJson(requiredArtifact(artifactRoot, PROJECT_MAP), PROJECT_MAP);
    String projectMapSchema = supportedSchema(
        projectMap,
        PROJECT_MAP,
        "schema_version",
        SUPPORTED_PROJECT_MAP_SCHEMA);
    EvidenceIndex evidenceIndex = readEvidenceIndex(requiredArtifact(artifactRoot, EVIDENCE_INDEX));
    JsonNode projectGraph = null;
    String graphSchema = null;
    if (graphRequirement != GraphRequirement.NONE) {
      Optional<Path> graphPath = optionalArtifact(artifactRoot, PROJECT_GRAPH);
      if (graphPath.isPresent()) {
        projectGraph = readJson(graphPath.orElseThrow(), PROJECT_GRAPH);
        graphSchema = validateGraph(projectGraph, evidenceIndex.ids());
      } else if (graphRequirement == GraphRequirement.REQUIRED) {
        throw new QueryArtifactException("Missing project-graph.json.");
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
    Path normalizedPath = queryPath.toAbsolutePath().normalize();
    if (Files.notExists(normalizedPath, LinkOption.NOFOLLOW_LINKS)) {
      throw new QueryArtifactException("Query path does not exist.");
    }
    if (Files.isSymbolicLink(normalizedPath)) {
      throw new QueryArtifactException("Query path must not be a symbolic link.");
    }
    if (!Files.isDirectory(normalizedPath, LinkOption.NOFOLLOW_LINKS)) {
      throw new QueryArtifactException("Query path is not a directory.");
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
    if (!Files.isRegularFile(artifact, LinkOption.NOFOLLOW_LINKS)) {
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
      records.add(record);
    }
    return new EvidenceIndex(List.copyOf(records), Map.copyOf(byId), Set.copyOf(byId.keySet()));
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
    JsonNode value = node.path(fieldName);
    if (!value.isTextual() || value.asText().isBlank()) {
      throw new QueryArtifactException("Malformed project-graph.json.");
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
}
