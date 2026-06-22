package io.github.dondindondev.agentprojectmemory.workspace;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.dondindondev.agentprojectmemory.OutputRedactor;
import io.github.dondindondev.agentprojectmemory.analyzer.ScanPathContainment;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public final class WorkspaceMapGenerator {
  private static final String WORKSPACE_SCHEMA_VERSION = "1.0";
  private static final String WORKSPACE_OUTPUT_DIRECTORY = ".project-memory";
  private static final String WORKSPACE_MAP = "workspace-map.json";
  private static final String PROJECT_MAP = "project-map.json";
  private static final String PROJECT_GRAPH = "project-graph.json";
  private static final String EVIDENCE_INDEX = "evidence-index.jsonl";
  private static final String SOURCE_REGISTRY = "source-registry.json";
  private static final int MAX_ARTIFACT_BYTES = 128 * 1024 * 1024;
  private static final int MAX_JSON_NESTING_DEPTH = 256;
  private static final int MAX_JSON_STRING_LENGTH = 1024 * 1024;
  private static final int MAX_SAMPLE_EVIDENCE_ID_LENGTH = 1024;
  private static final int MAX_SAMPLE_EVIDENCE_REFERENCES = 5;
  private static final Set<String> SUPPORTED_PROJECT_MAP_SCHEMAS = Set.of("1.0", "2.0");
  private static final Set<String> SUPPORTED_SOURCE_REGISTRY_SCHEMAS = Set.of("1.0", "1.1", "1.2");
  private static final Set<String> SUPPORTED_EVIDENCE_SOURCE_TYPES = Set.of(
      "annotation",
      "api_spec",
      "build_file",
      "code_symbol",
      "config_file",
      "document",
      "path_signal",
      "test_file");
  private static final Set<String> SENSITIVE_PATH_SEGMENTS = Set.of(
      ".aws",
      ".azure",
      ".docker",
      ".env",
      ".gcp",
      ".gnupg",
      ".kube",
      ".netrc",
      ".npmrc",
      ".pypirc",
      ".ssh",
      "authorized_keys",
      "id_dsa",
      "id_ecdsa",
      "id_ed25519",
      "id_rsa",
      "known_hosts",
      "passwd");
  private static final Pattern EVIDENCE_ID_PATH_KEY =
      Pattern.compile("[A-Za-z0-9._~/%+@=$,()\\[\\]-]+");
  private static final Pattern EVIDENCE_ID_LINE_RANGE =
      Pattern.compile("(?:unknown|[1-9][0-9]*-[1-9][0-9]*)");
  private static final Pattern EVIDENCE_ID_SYMBOL_KEY =
      Pattern.compile("[A-Za-z0-9._~/%+@#=$,()\\[\\]:-]+");
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

  public Result generate(WorkspaceConfiguration configuration) throws IOException {
    Objects.requireNonNull(configuration, "configuration");
    List<WorkspaceDiagnostic> diagnostics = new ArrayList<>();
    List<MemberArtifactSummary> memberSummaries = new ArrayList<>();
    for (WorkspaceMember member : configuration.members()) {
      memberSummaries.add(summarizeMember(member, diagnostics));
    }

    String content = workspaceMapJson(configuration, memberSummaries, diagnostics);
    Path outputDirectory = configuration.workspaceRoot().resolve(WORKSPACE_OUTPUT_DIRECTORY);
    writeGeneratedFile(
        configuration.canonicalWorkspaceRoot(),
        outputDirectory,
        WORKSPACE_MAP,
        content);
    return new Result(memberSummaries.size(), diagnostics.size());
  }

  private MemberArtifactSummary summarizeMember(
      WorkspaceMember member,
      List<WorkspaceDiagnostic> diagnostics) {
    Path artifactRoot = member.canonicalRoot().resolve(WORKSPACE_OUTPUT_DIRECTORY).normalize();
    if (Files.notExists(artifactRoot, LinkOption.NOFOLLOW_LINKS)) {
      diagnostics.add(diagnostic(
          member,
          "member-artifacts-missing",
          WORKSPACE_OUTPUT_DIRECTORY,
          "Member .project-memory artifact root was not found."));
      return MemberArtifactSummary.missing(member);
    }

    if (Files.isSymbolicLink(artifactRoot)
        || !Files.isDirectory(artifactRoot, LinkOption.NOFOLLOW_LINKS)
        || ScanPathContainment.realPathUnderRoot(member.canonicalRoot(), artifactRoot)
            .filter(Files::isDirectory)
            .isEmpty()) {
      diagnostics.add(diagnostic(
          member,
          "member-artifacts-invalid",
          WORKSPACE_OUTPUT_DIRECTORY,
          "Member .project-memory artifact root is not a trusted directory."));
      return MemberArtifactSummary.invalid(member);
    }

    try {
      JsonNode projectMap = readJson(requiredArtifact(artifactRoot, PROJECT_MAP), PROJECT_MAP);
      String projectMapSchema = supportedProjectMapSchema(projectMap);
      EvidenceIndex evidenceIndex = readEvidenceIndex(requiredArtifact(artifactRoot, EVIDENCE_INDEX));
      String graphSchema = optionalGraphSchema(
          member,
          artifactRoot,
          projectMapSchema,
          evidenceIndex.ids(),
          diagnostics).orElse(null);
      String sourceRegistrySchema = optionalSourceRegistrySchema(member, artifactRoot, diagnostics)
          .orElse(null);
      return MemberArtifactSummary.present(
          member,
          projectMapSchema,
          graphSchema,
          sourceRegistrySchema,
          evidenceIndex.records().size(),
          evidenceIndex.sampleIds());
    } catch (WorkspaceArtifactException exception) {
      diagnostics.add(diagnostic(
          member,
          exception.code(),
          exception.artifact(),
          exception.getMessage()));
      return MemberArtifactSummary.invalid(member);
    }
  }

  private Optional<String> optionalGraphSchema(
      WorkspaceMember member,
      Path artifactRoot,
      String projectMapSchema,
      Set<String> evidenceIds,
      List<WorkspaceDiagnostic> diagnostics) {
    try {
      Optional<Path> graph = optionalArtifact(artifactRoot, PROJECT_GRAPH);
      if (graph.isEmpty()) {
        return Optional.empty();
      }
      JsonNode graphRoot = readJson(graph.orElseThrow(), PROJECT_GRAPH);
      String graphSchema = requiredSupportedText(
          graphRoot,
          "graph_schema_version",
          Set.of("1.0"),
          "Unsupported project-graph.json graph_schema_version.");
      String graphProjectMapSchema = requiredText(
          graphRoot,
          "project_map_schema_version",
          "Malformed project-graph.json.");
      if (!projectMapSchema.equals(graphProjectMapSchema)) {
        throw new WorkspaceArtifactException(
            "project-graph-invalid",
            PROJECT_GRAPH,
            "Unsupported project-graph.json project_map_schema_version.");
      }
      validateGraphEvidenceReferences(graphRoot, evidenceIds);
      return Optional.of(graphSchema);
    } catch (WorkspaceArtifactException exception) {
      diagnostics.add(diagnostic(
          member,
          exception.code(),
          exception.artifact(),
          exception.getMessage()));
      return Optional.empty();
    }
  }

  private Optional<String> optionalSourceRegistrySchema(
      WorkspaceMember member,
      Path artifactRoot,
      List<WorkspaceDiagnostic> diagnostics) {
    try {
      Optional<Path> sourceRegistry = optionalArtifact(artifactRoot, SOURCE_REGISTRY);
      if (sourceRegistry.isEmpty()) {
        return Optional.empty();
      }
      JsonNode sourceRegistryRoot = readJson(sourceRegistry.orElseThrow(), SOURCE_REGISTRY);
      return Optional.of(requiredSupportedText(
          sourceRegistryRoot,
          "source_registry_schema_version",
          SUPPORTED_SOURCE_REGISTRY_SCHEMAS,
          "Unsupported source-registry.json source_registry_schema_version."));
    } catch (WorkspaceArtifactException exception) {
      diagnostics.add(diagnostic(
          member,
          exception.code(),
          exception.artifact(),
          exception.getMessage()));
      return Optional.empty();
    }
  }

  private Path requiredArtifact(Path artifactRoot, String fileName)
      throws WorkspaceArtifactException {
    Path artifact = artifactRoot.resolve(fileName).normalize();
    if (!artifact.startsWith(artifactRoot)) {
      throw new WorkspaceArtifactException(
          artifactCode(fileName),
          fileName,
          "Invalid member artifact path.");
    }
    if (Files.notExists(artifact, LinkOption.NOFOLLOW_LINKS)) {
      throw new WorkspaceArtifactException(
          artifactCode(fileName),
          fileName,
          "Required member artifact is missing.");
    }
    return existingArtifactFile(artifactRoot, artifact, fileName);
  }

  private Optional<Path> optionalArtifact(Path artifactRoot, String fileName)
      throws WorkspaceArtifactException {
    Path artifact = artifactRoot.resolve(fileName).normalize();
    if (!artifact.startsWith(artifactRoot)) {
      throw new WorkspaceArtifactException(
          artifactCode(fileName),
          fileName,
          "Invalid member artifact path.");
    }
    if (Files.notExists(artifact, LinkOption.NOFOLLOW_LINKS)) {
      return Optional.empty();
    }
    return Optional.of(existingArtifactFile(artifactRoot, artifact, fileName));
  }

  private Path existingArtifactFile(Path artifactRoot, Path artifact, String fileName)
      throws WorkspaceArtifactException {
    if (Files.isSymbolicLink(artifact) || hasSymbolicLinkSegment(artifactRoot, artifact)) {
      throw new WorkspaceArtifactException(
          artifactCode(fileName),
          fileName,
          fileName + " must not be a symbolic link.");
    }
    if (!ScanPathContainment.isTrustedRegularFileNoFollow(artifact)) {
      throw new WorkspaceArtifactException(
          artifactCode(fileName),
          fileName,
          fileName + " is not a trusted regular file.");
    }
    return artifact;
  }

  private JsonNode readJson(Path artifact, String artifactName) throws WorkspaceArtifactException {
    try {
      JsonNode root = JSON.readTree(readUtf8(artifact, artifactName));
      if (root == null || !root.isObject()) {
        throw new WorkspaceArtifactException(
            artifactCode(artifactName),
            artifactName,
            "Malformed " + artifactName + ".");
      }
      return root;
    } catch (JsonProcessingException exception) {
      throw new WorkspaceArtifactException(
          artifactCode(artifactName),
          artifactName,
          "Malformed " + artifactName + ".");
    } catch (IOException exception) {
      throw new WorkspaceArtifactException(
          artifactCode(artifactName),
          artifactName,
          "Could not read " + artifactName + ".");
    }
  }

  private String readUtf8(Path artifact, String artifactName)
      throws IOException, WorkspaceArtifactException {
    try {
      return new String(
          ScanPathContainment.readRegularFileBytesNoFollowStable(artifact, MAX_ARTIFACT_BYTES),
          StandardCharsets.UTF_8);
    } catch (ScanPathContainment.FileSizeLimitExceededException exception) {
      throw new WorkspaceArtifactException(
          artifactCode(artifactName),
          artifactName,
          artifactName + " exceeds maximum supported size.");
    }
  }

  private String supportedProjectMapSchema(JsonNode projectMap) throws WorkspaceArtifactException {
    return requiredSupportedText(
        projectMap,
        "schema_version",
        SUPPORTED_PROJECT_MAP_SCHEMAS,
        "Unsupported project-map.json schema_version.");
  }

  private EvidenceIndex readEvidenceIndex(Path evidencePath) throws WorkspaceArtifactException {
    String content;
    try {
      content = readUtf8(evidencePath, EVIDENCE_INDEX);
    } catch (IOException exception) {
      throw new WorkspaceArtifactException(
          artifactCode(EVIDENCE_INDEX),
          EVIDENCE_INDEX,
          "Could not read evidence-index.jsonl.");
    }

    List<JsonNode> records = new ArrayList<>();
    Map<String, JsonNode> byId = new LinkedHashMap<>();
    List<String> sampleIds = new ArrayList<>();
    for (String line : content.lines().toList()) {
      if (line.isBlank()) {
        throw malformedEvidenceIndex();
      }
      JsonNode record;
      try {
        record = JSON.readTree(line);
      } catch (JsonProcessingException exception) {
        throw malformedEvidenceIndex();
      }
      if (record == null || !record.isObject() || !hasOnlyFields(record, EVIDENCE_FIELDS)) {
        throw malformedEvidenceIndex();
      }
      JsonNode idNode = record.path("id");
      if (!idNode.isTextual() || idNode.asText().isBlank()) {
        throw malformedEvidenceIndex();
      }
      String id = idNode.asText();
      if (byId.putIfAbsent(id, record) != null) {
        throw new WorkspaceArtifactException(
            artifactCode(EVIDENCE_INDEX),
            EVIDENCE_INDEX,
            "Duplicate evidence id in evidence-index.jsonl.");
      }
      if (sampleIds.size() < MAX_SAMPLE_EVIDENCE_REFERENCES) {
        if (isSafeSampleEvidenceId(id)) {
          sampleIds.add(id);
        }
      }
      records.add(record);
    }
    return new EvidenceIndex(List.copyOf(records), Set.copyOf(byId.keySet()), List.copyOf(sampleIds));
  }

  private boolean isSafeSampleEvidenceId(String evidenceId) {
    if (evidenceId == null
        || evidenceId.isBlank()
        || evidenceId.length() > MAX_SAMPLE_EVIDENCE_ID_LENGTH
        || OutputRedactor.isCredentialKey(evidenceId)
        || !evidenceId.equals(OutputRedactor.redact(evidenceId))
        || !isPrintableAscii(evidenceId)
        || !evidenceId.startsWith("ev:")
        || containsUnsafePathOrIdShape(evidenceId)) {
      return false;
    }

    String withoutPrefix = evidenceId.substring("ev:".length());
    int pathEnd = withoutPrefix.indexOf(':');
    if (pathEnd <= 0) {
      return false;
    }
    int lineRangeEnd = withoutPrefix.indexOf(':', pathEnd + 1);
    if (lineRangeEnd <= pathEnd + 1) {
      return false;
    }
    int sourceTypeEnd = withoutPrefix.indexOf(':', lineRangeEnd + 1);
    if (sourceTypeEnd <= lineRangeEnd + 1 || sourceTypeEnd == withoutPrefix.length() - 1) {
      return false;
    }

    String pathKey = withoutPrefix.substring(0, pathEnd);
    String lineRange = withoutPrefix.substring(pathEnd + 1, lineRangeEnd);
    String sourceType = withoutPrefix.substring(lineRangeEnd + 1, sourceTypeEnd);
    String symbolKey = withoutPrefix.substring(sourceTypeEnd + 1);

    return isSafeEvidencePathKey(pathKey)
        && EVIDENCE_ID_LINE_RANGE.matcher(lineRange).matches()
        && SUPPORTED_EVIDENCE_SOURCE_TYPES.contains(sourceType)
        && EVIDENCE_ID_SYMBOL_KEY.matcher(symbolKey).matches()
        && !containsUnsafePathOrIdShape(pathKey)
        && !containsUnsafePathOrIdShape(symbolKey);
  }

  private boolean isSafeEvidencePathKey(String pathKey) {
    return !pathKey.startsWith("/")
        && !pathKey.startsWith("./")
        && !pathKey.startsWith("../")
        && !pathKey.contains("//")
        && !pathKey.contains("\\")
        && EVIDENCE_ID_PATH_KEY.matcher(pathKey).matches();
  }

  private boolean isPrintableAscii(String value) {
    for (int index = 0; index < value.length(); index++) {
      char current = value.charAt(index);
      if (current < 0x21 || current > 0x7e) {
        return false;
      }
    }
    return true;
  }

  private boolean containsUnsafePathOrIdShape(String value) {
    return containsUnsafePathOrIdShapeCandidate(value)
        || containsUnsafePathOrIdShapeCandidate(percentDecode(value));
  }

  private boolean containsUnsafePathOrIdShapeCandidate(String value) {
    if (value == null || value.isEmpty()) {
      return false;
    }
    String normalized = value.replace('\\', '/');
    String lower = normalized.toLowerCase(Locale.ROOT);
    return containsUnsafeLocalPathPrefix(normalized, lower)
        || containsUnsafePathTraversal(lower)
        || containsUrlLikePath(lower)
        || containsGeneratedArtifactPath(lower)
        || containsSensitivePathSegment(normalized);
  }

  private boolean containsUnsafeLocalPathPrefix(String value, String lower) {
    return containsDelimited(value, "/Users/")
        || containsDelimited(lower, "/home/")
        || containsDelimited(lower, "/root/")
        || containsDelimited(lower, "/private/")
        || containsDelimited(lower, "/var/folders/")
        || containsDelimited(lower, "/tmp/")
        || containsDelimited(lower, "/etc/")
        || containsDelimited(lower, "/proc/")
        || containsDelimited(lower, "/sys/")
        || containsDelimited(lower, "/dev/")
        || containsDelimited(lower, "/volumes/")
        || lower.matches(".*(^|[\\s:=,;()\\[\\]{}\"'`])[a-z]:/.*")
        || lower.startsWith("~/")
        || lower.contains("=~/")
        || lower.contains(":~/");
  }

  private boolean containsDelimited(String value, String token) {
    int index = value.indexOf(token);
    while (index >= 0) {
      if (index == 0 || isBoundary(value.charAt(index - 1))) {
        return true;
      }
      index = value.indexOf(token, index + 1);
    }
    return false;
  }

  private boolean isBoundary(char value) {
    return Character.isWhitespace(value)
        || value == ':'
        || value == '='
        || value == ','
        || value == ';'
        || value == '('
        || value == ')'
        || value == '['
        || value == ']'
        || value == '{'
        || value == '}'
        || value == '"'
        || value == '\''
        || value == '`';
  }

  private boolean containsUnsafePathTraversal(String lower) {
    return lower.startsWith("../")
        || lower.contains("/../")
        || lower.endsWith("/..")
        || lower.startsWith("./")
        || lower.contains("/./")
        || lower.endsWith("/.");
  }

  private boolean containsUrlLikePath(String lower) {
    return lower.matches(".*(^|[\\s:=,;()\\[\\]{}\"'`])[a-z][a-z0-9+.-]*://.*")
        || lower.matches(".*(^|[\\s:=,;()\\[\\]{}\"'`])file:.*");
  }

  private boolean containsGeneratedArtifactPath(String lower) {
    return ".project-memory".equals(lower)
        || lower.startsWith(".project-memory/")
        || lower.contains("/.project-memory/")
        || lower.contains(":.project-memory/")
        || lower.contains("=.project-memory/");
  }

  private boolean containsSensitivePathSegment(String value) {
    for (String segment : value.split("[/:=&#?\\s]+")) {
      if (SENSITIVE_PATH_SEGMENTS.contains(segment.toLowerCase(Locale.ROOT))) {
        return true;
      }
    }
    return false;
  }

  private String percentDecode(String value) {
    StringBuilder decoded = new StringBuilder(value.length());
    for (int index = 0; index < value.length(); index++) {
      char current = value.charAt(index);
      if (current == '%' && index + 2 < value.length()) {
        int high = Character.digit(value.charAt(index + 1), 16);
        int low = Character.digit(value.charAt(index + 2), 16);
        if (high >= 0 && low >= 0) {
          decoded.append(new String(
              new byte[] {(byte) ((high << 4) + low)},
              StandardCharsets.UTF_8));
          index += 2;
          continue;
        }
      }
      decoded.append(current);
    }
    return decoded.toString();
  }

  private WorkspaceArtifactException malformedEvidenceIndex() {
    return new WorkspaceArtifactException(
        artifactCode(EVIDENCE_INDEX),
        EVIDENCE_INDEX,
        "Malformed evidence-index.jsonl.");
  }

  private String requiredSupportedText(
      JsonNode node,
      String fieldName,
      Set<String> supportedValues,
      String errorMessage) throws WorkspaceArtifactException {
    String value = requiredText(node, fieldName, errorMessage);
    if (!supportedValues.contains(value)) {
      throw new WorkspaceArtifactException(
          artifactCodeForField(fieldName),
          artifactForField(fieldName),
          errorMessage);
    }
    return value;
  }

  private String requiredText(JsonNode node, String fieldName, String errorMessage)
      throws WorkspaceArtifactException {
    JsonNode value = node.path(fieldName);
    if (!value.isTextual() || value.asText().isBlank()) {
      throw new WorkspaceArtifactException(
          artifactCodeForField(fieldName),
          artifactForField(fieldName),
          errorMessage);
    }
    return value.asText();
  }

  private void validateGraphEvidenceReferences(JsonNode node, Set<String> evidenceIds)
      throws WorkspaceArtifactException {
    if (node.isObject()) {
      Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
      while (fields.hasNext()) {
        Map.Entry<String, JsonNode> field = fields.next();
        if ("evidence_ids".equals(field.getKey())) {
          validateEvidenceIdsArray(field.getValue(), evidenceIds);
        } else {
          validateGraphEvidenceReferences(field.getValue(), evidenceIds);
        }
      }
    } else if (node.isArray()) {
      for (JsonNode item : node) {
        validateGraphEvidenceReferences(item, evidenceIds);
      }
    }
  }

  private void validateEvidenceIdsArray(JsonNode value, Set<String> evidenceIds)
      throws WorkspaceArtifactException {
    if (!value.isArray()) {
      throw new WorkspaceArtifactException(
          artifactCode(PROJECT_GRAPH),
          PROJECT_GRAPH,
          "Malformed project-graph.json.");
    }
    for (JsonNode id : value) {
      if (!id.isTextual() || !evidenceIds.contains(id.asText())) {
        throw new WorkspaceArtifactException(
            artifactCode(PROJECT_GRAPH),
            PROJECT_GRAPH,
            "Invalid project-graph.json evidence reference.");
      }
    }
  }

  private String workspaceMapJson(
      WorkspaceConfiguration configuration,
      List<MemberArtifactSummary> members,
      List<WorkspaceDiagnostic> diagnostics) throws IOException {
    ObjectNode root = JSON.createObjectNode();
    root.put("workspace_schema_version", WORKSPACE_SCHEMA_VERSION);

    ObjectNode workspace = root.putObject("workspace");
    workspace.put("root_kind", "config_directory");
    ObjectNode configSource = workspace.putObject("config_source");
    configSource.put("path", configuration.configPath());
    configSource.put("path_kind", "workspace_relative_file");
    configSource.put("content_status", "not_serialized");
    workspace.put("member_count", members.size());

    ArrayNode memberItems = root.putArray("members");
    for (MemberArtifactSummary member : members) {
      appendMember(memberItems.addObject(), member);
    }

    ObjectNode relations = root.putObject("relations");
    relations.put("analysis_status", "not_analyzed");
    relations.putArray("items");

    ArrayNode diagnosticItems = root.putArray("diagnostics");
    for (WorkspaceDiagnostic diagnostic : diagnostics) {
      appendDiagnostic(diagnosticItems.addObject(), diagnostic);
    }

    return JSON.writerWithDefaultPrettyPrinter().writeValueAsString(root) + "\n";
  }

  private void appendMember(ObjectNode node, MemberArtifactSummary member) {
    node.put("repo_id", member.member().repoId());
    node.put("root_path", member.member().rootPath());
    node.put("root_path_kind", "workspace_relative_directory");
    node.put("artifact_root", member.member().rootPath() + "/" + WORKSPACE_OUTPUT_DIRECTORY);
    node.put("artifact_status", member.artifactStatus());
    putNullableText(node, "project_map_schema_version", member.projectMapSchemaVersion());
    putNullableText(node, "graph_schema_version", member.graphSchemaVersion());
    putNullableText(node, "source_registry_schema_version", member.sourceRegistrySchemaVersion());
    node.put("evidence_record_count", member.evidenceRecordCount());
    ArrayNode references = node.putArray("sample_evidence_references");
    for (String evidenceId : member.sampleEvidenceIds()) {
      ObjectNode reference = references.addObject();
      reference.put("repo_id", member.member().repoId());
      reference.put("evidence_id", evidenceId);
      reference.put("artifact", EVIDENCE_INDEX);
    }
  }

  private void appendDiagnostic(ObjectNode node, WorkspaceDiagnostic diagnostic) {
    node.put("id", diagnostic.id());
    node.put("severity", diagnostic.severity());
    node.put("category", diagnostic.category());
    node.put("repo_id", diagnostic.repoId());
    node.put("artifact", diagnostic.artifact());
    node.put("message", diagnostic.message());
  }

  private void putNullableText(ObjectNode node, String fieldName, String value) {
    if (value == null) {
      node.putNull(fieldName);
    } else {
      node.put(fieldName, value);
    }
  }

  private WorkspaceDiagnostic diagnostic(
      WorkspaceMember member,
      String code,
      String artifact,
      String message) {
    return new WorkspaceDiagnostic(
        "workspace-diagnostic:" + member.repoId() + ":" + code,
        "warning",
        "workspace_artifact",
        member.repoId(),
        artifact,
        message);
  }

  private void writeGeneratedFile(
      Path canonicalWorkspaceRoot,
      Path outputDirectory,
      String fileName,
      String content) throws IOException {
    Path target = outputDirectory.resolve(fileName);
    ensureGeneratedOutputParent(canonicalWorkspaceRoot, outputDirectory, target);
    validateGeneratedOutputTarget(canonicalWorkspaceRoot, target);

    Path targetParent = target.getParent();
    String tempPrefix = "." + target.getFileName() + ".";
    Path tempFile = Files.createTempFile(targetParent, tempPrefix, ".tmp");
    boolean moved = false;
    try {
      if (!isRegularFileUnderRoot(canonicalWorkspaceRoot, tempFile)) {
        throw new IOException(
            "Temporary output file is not a regular file under scan root: " + tempFile);
      }

      Files.writeString(tempFile, content, StandardCharsets.UTF_8);
      validateGeneratedOutputTarget(canonicalWorkspaceRoot, target);
      moveGeneratedFile(tempFile, target);
      moved = true;

      if (!isRegularFileUnderRoot(canonicalWorkspaceRoot, target)) {
        throw new IOException(
            "Output file target is not a regular file under scan root: " + target);
      }
    } finally {
      if (!moved) {
        Files.deleteIfExists(tempFile);
      }
    }
  }

  private void validateGeneratedOutputTarget(Path canonicalWorkspaceRoot, Path target)
      throws IOException {
    if (Files.isSymbolicLink(target)) {
      throw new IOException("Output file must not be a symbolic link: " + target);
    }

    if (!Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
      return;
    }

    if (!isRegularFileUnderRoot(canonicalWorkspaceRoot, target)) {
      throw new IOException(
          "Output file target is not a regular file under scan root: " + target);
    }

    Long linkCount = hardLinkCount(target);
    if (linkCount != null && linkCount > 1) {
      throw new IOException("Output file must not have multiple hard links: " + target);
    }
  }

  private void ensureGeneratedOutputParent(
      Path canonicalWorkspaceRoot,
      Path outputDirectory,
      Path target) throws IOException {
    Path parent = target.getParent();
    if (parent == null) {
      throw new IOException("Output directory is not contained under scan root: " + target);
    }

    Path normalizedOutputDirectory = outputDirectory.toAbsolutePath().normalize();
    Path normalizedParent = parent.toAbsolutePath().normalize();
    if (!normalizedParent.startsWith(normalizedOutputDirectory)) {
      throw new IOException("Output directory is not contained under scan root: " + parent);
    }

    if (Files.isSymbolicLink(parent)) {
      throw new IOException("Output directory must not be a symbolic link: " + parent);
    }

    if (Files.exists(parent, LinkOption.NOFOLLOW_LINKS)
        && !Files.isDirectory(parent, LinkOption.NOFOLLOW_LINKS)) {
      throw new IOException("Output directory path exists and is not a directory: " + parent);
    }

    Files.createDirectories(parent);

    if (Files.isSymbolicLink(parent)) {
      throw new IOException("Output directory must not be a symbolic link: " + parent);
    }

    if (ScanPathContainment.realPathUnderRoot(canonicalWorkspaceRoot, parent)
        .filter(Files::isDirectory)
        .isEmpty()) {
      throw new IOException("Output directory is not contained under scan root: " + parent);
    }
  }

  private void moveGeneratedFile(Path tempFile, Path target) throws IOException {
    try {
      Files.move(
          tempFile,
          target,
          StandardCopyOption.ATOMIC_MOVE,
          StandardCopyOption.REPLACE_EXISTING);
    } catch (AtomicMoveNotSupportedException ex) {
      Files.move(tempFile, target, StandardCopyOption.REPLACE_EXISTING);
    }
  }

  private Long hardLinkCount(Path target) throws IOException {
    try {
      Object value = Files.getAttribute(target, "unix:nlink", LinkOption.NOFOLLOW_LINKS);
      if (value instanceof Number number) {
        return number.longValue();
      }
      return null;
    } catch (IllegalArgumentException | UnsupportedOperationException ex) {
      return null;
    }
  }

  private boolean isRegularFileUnderRoot(Path canonicalRoot, Path target) {
    return ScanPathContainment.realPathUnderRoot(canonicalRoot, target)
        .filter(Files::isRegularFile)
        .isPresent();
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

  private String artifactCode(String artifact) {
    return switch (artifact) {
      case PROJECT_MAP -> "project-map-invalid";
      case PROJECT_GRAPH -> "project-graph-invalid";
      case EVIDENCE_INDEX -> "evidence-index-invalid";
      case SOURCE_REGISTRY -> "source-registry-invalid";
      default -> "member-artifact-invalid";
    };
  }

  private String artifactCodeForField(String fieldName) {
    return artifactCode(artifactForField(fieldName));
  }

  private String artifactForField(String fieldName) {
    return switch (fieldName) {
      case "schema_version" -> PROJECT_MAP;
      case "graph_schema_version", "project_map_schema_version" -> PROJECT_GRAPH;
      case "source_registry_schema_version" -> SOURCE_REGISTRY;
      default -> "member artifact";
    };
  }

  public record Result(
      int memberCount,
      int diagnosticCount) {
  }

  private record MemberArtifactSummary(
      WorkspaceMember member,
      String artifactStatus,
      String projectMapSchemaVersion,
      String graphSchemaVersion,
      String sourceRegistrySchemaVersion,
      int evidenceRecordCount,
      List<String> sampleEvidenceIds) {
    private static MemberArtifactSummary present(
        WorkspaceMember member,
        String projectMapSchemaVersion,
        String graphSchemaVersion,
        String sourceRegistrySchemaVersion,
        int evidenceRecordCount,
        List<String> sampleEvidenceIds) {
      return new MemberArtifactSummary(
          member,
          "present",
          projectMapSchemaVersion,
          graphSchemaVersion,
          sourceRegistrySchemaVersion,
          evidenceRecordCount,
          List.copyOf(sampleEvidenceIds));
    }

    private static MemberArtifactSummary missing(WorkspaceMember member) {
      return empty(member, "missing");
    }

    private static MemberArtifactSummary invalid(WorkspaceMember member) {
      return empty(member, "invalid");
    }

    private static MemberArtifactSummary empty(WorkspaceMember member, String artifactStatus) {
      return new MemberArtifactSummary(
          member,
          artifactStatus,
          null,
          null,
          null,
          0,
          List.of());
    }
  }

  private record EvidenceIndex(
      List<JsonNode> records,
      Set<String> ids,
      List<String> sampleIds) {
  }

  private record WorkspaceDiagnostic(
      String id,
      String severity,
      String category,
      String repoId,
      String artifact,
      String message) {
  }

  private static final class WorkspaceArtifactException extends Exception {
    private final String code;
    private final String artifact;

    WorkspaceArtifactException(String code, String artifact, String message) {
      super(message);
      this.code = code;
      this.artifact = artifact;
    }

    String code() {
      return code;
    }

    String artifact() {
      return artifact;
    }
  }
}
