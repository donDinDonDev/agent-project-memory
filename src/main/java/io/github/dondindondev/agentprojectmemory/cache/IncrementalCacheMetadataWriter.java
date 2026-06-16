package io.github.dondindondev.agentprojectmemory.cache;

import io.github.dondindondev.agentprojectmemory.analyzer.JavaSourceParser;
import io.github.dondindondev.agentprojectmemory.analyzer.ScanPathContainment;
import io.github.dondindondev.agentprojectmemory.analyzer.gradle.GradleBuildFileInput;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenPomInput;
import io.github.dondindondev.agentprojectmemory.profiles.AgentOutputProfile;
import io.github.dondindondev.agentprojectmemory.scanconfig.ScanConfiguration;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class IncrementalCacheMetadataWriter {
  static final String CACHE_SCHEMA_VERSION = "1.0";
  static final String PROJECT_MAP_SCHEMA_VERSION = "1.0";
  static final String CACHE_KIND = "incremental_scan_metadata";
  static final String REUSE_GRANULARITY = "whole_output_set";
  static final String FINGERPRINT_ALGORITHM = "sha256";
  static final String EVIDENCE_POLICY = "cache_is_not_evidence";
  static final String CACHE_DIRECTORY_PATH = "cache/v1";
  static final String MANIFEST_PATH = CACHE_DIRECTORY_PATH + "/manifest.json";
  static final String INPUTS_PATH = CACHE_DIRECTORY_PATH + "/inputs.jsonl";
  static final String OUTPUTS_PATH = CACHE_DIRECTORY_PATH + "/outputs.jsonl";
  private static final String JAVA_SOURCE_ROOT_PATH = "src/main/java";
  private static final String JAVA_TEST_ROOT_PATH = "src/test/java";
  private static final String MAIN_RESOURCE_ROOT_PATH = "src/main/resources";
  private static final String TEST_RESOURCE_ROOT_PATH = "src/test/resources";
  private static final int MAX_CONFIG_BYTES = 64 * 1024;
  private static final int MAX_SPEC_BYTES = 1024 * 1024;
  private static final int MAX_RESOURCE_CONFIG_BYTES = 1024 * 1024;
  private static final int MAX_MARKDOWN_BYTES = 16 * 1024 * 1024;
  static final int MAX_OUTPUT_BYTES = 128 * 1024 * 1024;
  private static final String GENERATED_SOURCE_ROOT_PATH_INPUT_KIND = "generated_source_root_path";
  private static final String GENERATED_SOURCE_ROOT_UNSAFE_PATH_INPUT_KIND =
      "generated_source_root_unsafe_path";
  private static final List<String> GENERATED_SOURCE_FAMILY_PATHS = List.of(
      "target/generated-sources",
      "target/generated-test-sources",
      "build/generated/sources",
      "build/generated/source");
  private static final Set<String> GRADLE_BUILD_FILE_NAMES = Set.of(
      "settings.gradle",
      "settings.gradle.kts",
      "build.gradle",
      "build.gradle.kts");
  private static final Set<String> LOGGING_CONFIG_FILE_NAMES = Set.of(
      "logback.xml",
      "logback-spring.xml",
      "log4j2.xml",
      "log4j2-spring.xml");
  private static final Set<String> DOCUMENT_EXCLUDED_SEGMENTS = Set.of(
      ".git",
      ".project-memory",
      "target",
      "build",
      "out",
      "dist",
      "node_modules",
      "maintainer",
      "internal",
      "private",
      "secrets");
  private static final List<OutputArtifact> BASE_OUTPUTS = List.of(
      new OutputArtifact("project-map.json", "project_map"),
      new OutputArtifact("project-graph.json", "project_graph"),
      new OutputArtifact("evidence-index.jsonl", "evidence_index"),
      new OutputArtifact("endpoints.md", "endpoints_markdown"),
      new OutputArtifact("agent-guide.md", "agent_guide_markdown"));

  public CacheWriteResult write(
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      Path outputDirectory,
      ScanConfiguration scanConfiguration,
      List<AgentOutputProfile> selectedProfiles,
      String toolVersion) throws IOException {
    Objects.requireNonNull(repositoryRoot, "repositoryRoot");
    Objects.requireNonNull(canonicalRepositoryRoot, "canonicalRepositoryRoot");
    Objects.requireNonNull(outputDirectory, "outputDirectory");
    Objects.requireNonNull(scanConfiguration, "scanConfiguration");
    Objects.requireNonNull(selectedProfiles, "selectedProfiles");

    Path normalizedRepositoryRoot = repositoryRoot.toAbsolutePath().normalize();
    Optional<Path> cacheDirectory = ensureCacheDirectory(
        canonicalRepositoryRoot,
        outputDirectory.toAbsolutePath().normalize());
    if (cacheDirectory.isEmpty()) {
      return CacheWriteResult.skippedResult();
    }

    try {
      CacheMetadata metadata = cacheMetadata(
          normalizedRepositoryRoot,
          canonicalRepositoryRoot,
          outputDirectory.toAbsolutePath().normalize(),
          scanConfiguration,
          selectedProfiles,
          toolVersion);
      List<CacheFile> cacheFiles = List.of(
          new CacheFile("inputs.jsonl", inputsJsonl(metadata.inputs())),
          new CacheFile("outputs.jsonl", outputsJsonl(metadata.outputs())),
          new CacheFile("manifest.json", metadata.manifestJson()));
      for (CacheFile file : cacheFiles) {
        Path target = cacheDirectory.orElseThrow().resolve(file.fileName());
        if (!isSafeCacheTarget(canonicalRepositoryRoot, target)) {
          return CacheWriteResult.skippedResult();
        }
      }
      for (CacheFile file : cacheFiles) {
        writeCacheFile(canonicalRepositoryRoot, cacheDirectory.orElseThrow(), file);
      }
      return CacheWriteResult.writtenResult();
    } catch (CacheMetadataUnavailableException exception) {
      return CacheWriteResult.skippedResult();
    }
  }

  CacheMetadata cacheMetadata(
      Path normalizedRepositoryRoot,
      Path canonicalRepositoryRoot,
      Path outputDirectory,
      ScanConfiguration scanConfiguration,
      List<AgentOutputProfile> selectedProfiles,
      String toolVersion) throws CacheMetadataUnavailableException {
    List<AgentOutputProfile> canonicalProfiles = canonicalProfiles(selectedProfiles);
    Map<String, InputFingerprint> inputFingerprints = inputFingerprints(
        normalizedRepositoryRoot,
        canonicalRepositoryRoot,
        scanConfiguration);
    List<OutputFingerprint> outputFingerprints = outputFingerprints(
        canonicalRepositoryRoot,
        outputDirectory.toAbsolutePath().normalize(),
        canonicalProfiles);
    String configSha256 = configSha256(inputFingerprints, scanConfiguration);
    return new CacheMetadata(
        inputFingerprints.values().stream()
            .sorted(Comparator
                .comparing(InputFingerprint::path)
                .thenComparing(InputFingerprint::inputKind))
            .toList(),
        outputFingerprints,
        manifestJson(
            optionFingerprint(scanConfiguration, canonicalProfiles),
            scanConfiguration,
            configSha256,
            canonicalProfiles,
            toolVersion == null || toolVersion.isBlank() ? "unknown" : toolVersion));
  }

  private Optional<Path> ensureCacheDirectory(Path canonicalRepositoryRoot, Path outputDirectory)
      throws IOException {
    if (Files.isSymbolicLink(outputDirectory)
        || ScanPathContainment.realPathUnderRoot(canonicalRepositoryRoot, outputDirectory).isEmpty()) {
      return Optional.empty();
    }

    Path cacheRoot = outputDirectory.resolve("cache").normalize();
    Path cacheDirectory = cacheRoot.resolve("v1").normalize();
    if (!cacheDirectory.startsWith(outputDirectory)) {
      return Optional.empty();
    }
    for (Path directory : List.of(cacheRoot, cacheDirectory)) {
      if (Files.isSymbolicLink(directory)) {
        return Optional.empty();
      }
      if (Files.exists(directory, LinkOption.NOFOLLOW_LINKS)
          && !Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS)) {
        return Optional.empty();
      }
      Files.createDirectories(directory);
      if (Files.isSymbolicLink(directory)
          || !Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS)
          || ScanPathContainment.realPathUnderRoot(canonicalRepositoryRoot, directory).isEmpty()) {
        return Optional.empty();
      }
    }
    return Optional.of(cacheDirectory);
  }

  private Map<String, InputFingerprint> inputFingerprints(
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      ScanConfiguration scanConfiguration) throws CacheMetadataUnavailableException {
    Map<String, InputFingerprint> fingerprints = new LinkedHashMap<>();
    String selectedConfigPath = scanConfiguration.configFilePath();
    if (selectedConfigPath != null) {
      Path config = repositoryRoot.resolve(selectedConfigPath).normalize();
      addFileFingerprint(
          fingerprints,
          repositoryRoot,
          canonicalRepositoryRoot,
          config,
          "scan_config",
          MAX_CONFIG_BYTES);
    }

    try {
      Files.walkFileTree(
          repositoryRoot,
          new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException {
              Path normalized = dir.toAbsolutePath().normalize();
              Optional<String> relativePath = repositoryRelativePath(repositoryRoot, normalized);
              if (relativePath.isEmpty()) {
                return FileVisitResult.SKIP_SUBTREE;
              }
              if (!safeDirectory(repositoryRoot, canonicalRepositoryRoot, normalized)) {
                return FileVisitResult.SKIP_SUBTREE;
              }
              String relative = relativePath.orElseThrow();
              if (!relative.isEmpty() && firstSegment(relative).equals(".project-memory")) {
                return FileVisitResult.SKIP_SUBTREE;
              }
              standardRootInputKind(relative)
                  .ifPresent(inputKind -> addPathFingerprint(fingerprints, relative, inputKind));
              if (isGeneratedSourceFamilyRoot(relative)) {
                addPathFingerprint(fingerprints, relative, GENERATED_SOURCE_ROOT_PATH_INPUT_KIND);
                addGeneratedSourceChildPathFingerprints(
                    fingerprints,
                    repositoryRoot,
                    canonicalRepositoryRoot,
                    normalized);
                return FileVisitResult.SKIP_SUBTREE;
              }
              if (shouldSkipNonGeneratedBuildOutputSubtree(relative)) {
                return FileVisitResult.SKIP_SUBTREE;
              }
              return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
              Path normalized = file.toAbsolutePath().normalize();
              if (attrs == null || !attrs.isRegularFile()) {
                return FileVisitResult.CONTINUE;
              }
              if (Files.isSymbolicLink(normalized)
                  || hasSymbolicLinkSegment(repositoryRoot, normalized)
                  || ScanPathContainment.realPathUnderRoot(canonicalRepositoryRoot, normalized).isEmpty()) {
                return FileVisitResult.CONTINUE;
              }
              Optional<String> relativePath = repositoryRelativePath(repositoryRoot, normalized);
              if (relativePath.isEmpty()) {
                return FileVisitResult.CONTINUE;
              }
              String relative = relativePath.orElseThrow();
              try {
                for (FileInputKind inputKind : inputKinds(relative, selectedConfigPath, scanConfiguration)) {
                  addFileFingerprint(
                      fingerprints,
                      repositoryRoot,
                      canonicalRepositoryRoot,
                      normalized,
                      inputKind.kind(),
                      inputKind.maxBytes());
                }
              } catch (CacheMetadataUnavailableException exception) {
                throw new UncheckedCacheMetadataUnavailableException(exception);
              }
              return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exception) {
              return FileVisitResult.CONTINUE;
            }
          });
    } catch (IOException exception) {
      throw new CacheMetadataUnavailableException();
    } catch (UncheckedCacheMetadataUnavailableException exception) {
      throw exception.cause();
    }
    return fingerprints;
  }

  private void addGeneratedSourceChildPathFingerprints(
      Map<String, InputFingerprint> fingerprints,
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      Path familyRoot) {
    try (var children = Files.list(familyRoot)) {
      children
          .map(path -> path.toAbsolutePath().normalize())
          .map(path -> generatedSourceChildPathFingerprint(
              repositoryRoot,
              canonicalRepositoryRoot,
              path))
          .flatMap(Optional::stream)
          .sorted(Comparator
              .comparing(GeneratedSourceChildPathFingerprint::path)
              .thenComparing(GeneratedSourceChildPathFingerprint::inputKind))
          .forEach(fingerprint -> addPathFingerprint(
              fingerprints,
              fingerprint.path(),
              fingerprint.inputKind()));
    } catch (IOException | SecurityException exception) {
      // Child generated-source roots are metadata-only. If listing fails, the cache can still
      // represent the detected family root path without reading generated content.
    }
  }

  private Optional<GeneratedSourceChildPathFingerprint> generatedSourceChildPathFingerprint(
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      Path path) {
    Optional<String> relativePath = repositoryRelativePath(repositoryRoot, path);
    if (relativePath.isEmpty()) {
      return Optional.empty();
    }
    if (safeDirectory(repositoryRoot, canonicalRepositoryRoot, path)) {
      return Optional.of(new GeneratedSourceChildPathFingerprint(
          relativePath.orElseThrow(),
          GENERATED_SOURCE_ROOT_PATH_INPUT_KIND));
    }
    if (Files.isSymbolicLink(path)
        || hasSymbolicLinkSegment(repositoryRoot, path)
        || ScanPathContainment.realPathUnderRoot(canonicalRepositoryRoot, path).isEmpty()) {
      return Optional.of(new GeneratedSourceChildPathFingerprint(
          relativePath.orElseThrow(),
          GENERATED_SOURCE_ROOT_UNSAFE_PATH_INPUT_KIND));
    }
    return Optional.empty();
  }

  private List<FileInputKind> inputKinds(
      String relativePath,
      String selectedConfigPath,
      ScanConfiguration scanConfiguration) {
    List<FileInputKind> kinds = new ArrayList<>();
    String fileName = fileName(relativePath);
    if ("pom.xml".equals(fileName)) {
      kinds.add(new FileInputKind("maven_pom", MavenPomInput.MAX_POM_BYTES));
    }
    if (GRADLE_BUILD_FILE_NAMES.contains(fileName)) {
      kinds.add(new FileInputKind("gradle_build_file", GradleBuildFileInput.MAX_GRADLE_BUILD_FILE_BYTES));
    }
    if (isJavaSource(relativePath, JAVA_SOURCE_ROOT_PATH)) {
      kinds.add(new FileInputKind("java_source", JavaSourceParser.MAX_JAVA_SOURCE_BYTES));
    }
    if (isJavaSource(relativePath, JAVA_TEST_ROOT_PATH)) {
      kinds.add(new FileInputKind("java_test_source", JavaSourceParser.MAX_JAVA_SOURCE_BYTES));
    }
    if (isSupportedResourceConfig(relativePath)) {
      kinds.add(new FileInputKind("resource_config_file", MAX_RESOURCE_CONFIG_BYTES));
    }
    if (isOpenApiSpec(relativePath)) {
      kinds.add(new FileInputKind("openapi_spec", MAX_SPEC_BYTES));
    }
    if (scanConfiguration.localMarkdownEnabled()
        && isAcceptedLocalMarkdownCandidate(relativePath, scanConfiguration)) {
      kinds.add(new FileInputKind("local_markdown_document", MAX_MARKDOWN_BYTES));
    }
    if (selectedConfigPath != null && selectedConfigPath.equals(relativePath)) {
      kinds.add(new FileInputKind("scan_config", MAX_CONFIG_BYTES));
    }
    return kinds;
  }

  private void addFileFingerprint(
      Map<String, InputFingerprint> fingerprints,
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      Path file,
      String inputKind,
      int maxBytes) throws CacheMetadataUnavailableException {
    Optional<String> relativePath = repositoryRelativePath(repositoryRoot, file);
    if (relativePath.isEmpty()
        || !ScanPathContainment.isRegularFileUnderRootNoFollow(canonicalRepositoryRoot, file)
        || hasSymbolicLinkSegment(repositoryRoot, file)) {
      throw new CacheMetadataUnavailableException();
    }
    try {
      byte[] bytes = ScanPathContainment.readRegularFileBytesNoFollowStable(file, maxBytes);
      InputFingerprint fingerprint = new InputFingerprint(
          relativePath.orElseThrow(),
          inputKind,
          sha256(bytes),
          (long) bytes.length);
      fingerprints.putIfAbsent(fingerprint.key(), fingerprint);
    } catch (IOException | SecurityException exception) {
      throw new CacheMetadataUnavailableException();
    }
  }

  private void addPathFingerprint(
      Map<String, InputFingerprint> fingerprints,
      String path,
      String inputKind) {
    InputFingerprint fingerprint = new InputFingerprint(path, inputKind, null, null);
    fingerprints.putIfAbsent(fingerprint.key(), fingerprint);
  }

  private List<OutputFingerprint> outputFingerprints(
      Path canonicalRepositoryRoot,
      Path outputDirectory,
      List<AgentOutputProfile> selectedProfiles) throws CacheMetadataUnavailableException {
    List<OutputArtifact> artifacts = new ArrayList<>(BASE_OUTPUTS);
    if (!selectedProfiles.isEmpty()) {
      artifacts.add(new OutputArtifact("agent-profiles/manifest.json", "agent_profile_manifest"));
      for (AgentOutputProfile profile : selectedProfiles) {
        artifacts.add(new OutputArtifact(profile.artifactPath(), "agent_profile_markdown"));
      }
    }
    List<OutputFingerprint> fingerprints = new ArrayList<>();
    for (OutputArtifact artifact : artifacts) {
      if (!safeCacheRelativePath(artifact.path())) {
        throw new CacheMetadataUnavailableException();
      }
      Path output = outputDirectory.resolve(artifact.path()).normalize();
      if (!output.startsWith(outputDirectory)
          || hasSymbolicLinkSegment(outputDirectory, output)
          || !ScanPathContainment.isRegularFileUnderRootNoFollow(canonicalRepositoryRoot, output)) {
        throw new CacheMetadataUnavailableException();
      }
      try {
        byte[] bytes = ScanPathContainment.readRegularFileBytesNoFollowStable(output, MAX_OUTPUT_BYTES);
        fingerprints.add(new OutputFingerprint(
            artifact.path(),
            artifact.kind(),
            sha256(bytes),
            (long) bytes.length));
      } catch (IOException | SecurityException exception) {
        throw new CacheMetadataUnavailableException();
      }
    }
    return fingerprints.stream()
        .sorted(Comparator
            .comparing(OutputFingerprint::path)
            .thenComparing(OutputFingerprint::outputKind))
        .toList();
  }

  private String manifestJson(
      String optionFingerprint,
      ScanConfiguration scanConfiguration,
      String configSha256,
      List<AgentOutputProfile> selectedProfiles,
      String toolVersion) {
    StringBuilder json = new StringBuilder();
    json.append("{\n");
    json.append("  \"cache_schema_version\": \"").append(CACHE_SCHEMA_VERSION).append("\",\n");
    json.append("  \"project_map_schema_version\": \"").append(PROJECT_MAP_SCHEMA_VERSION).append("\",\n");
    json.append("  \"cache_kind\": \"").append(CACHE_KIND).append("\",\n");
    json.append("  \"reuse_granularity\": \"").append(REUSE_GRANULARITY).append("\",\n");
    json.append("  \"fingerprint_algorithm\": \"").append(FINGERPRINT_ALGORITHM).append("\",\n");
    json.append("  \"input_fingerprints_path\": \"").append(INPUTS_PATH).append("\",\n");
    json.append("  \"output_fingerprints_path\": \"").append(OUTPUTS_PATH).append("\",\n");
    json.append("  \"tool_version\": \"").append(jsonString(toolVersion)).append("\",\n");
    json.append("  \"option_fingerprint\": \"").append(optionFingerprint).append("\",\n");
    json.append("  \"config_fingerprint\": {\n");
    json.append("    \"status\": \"").append(jsonString(scanConfiguration.configFileStatus())).append("\",\n");
    json.append("    \"path\": ");
    if (scanConfiguration.configFilePath() == null) {
      json.append("null");
    } else {
      json.append('"').append(jsonString(scanConfiguration.configFilePath())).append('"');
    }
    json.append(",\n");
    json.append("    \"sha256\": ");
    if (configSha256 == null) {
      json.append("null\n");
    } else {
      json.append('"').append(configSha256).append("\"\n");
    }
    json.append("  },\n");
    json.append("  \"selected_profiles\": [");
    for (int index = 0; index < selectedProfiles.size(); index++) {
      if (index > 0) {
        json.append(", ");
      }
      json.append('"').append(selectedProfiles.get(index).selector()).append('"');
    }
    json.append("],\n");
    json.append("  \"evidence_policy\": \"").append(EVIDENCE_POLICY).append("\",\n");
    json.append("  \"raw_values_serialized\": false\n");
    json.append("}\n");
    return json.toString();
  }

  private String optionFingerprint(
      ScanConfiguration scanConfiguration,
      List<AgentOutputProfile> selectedProfiles) {
    StringBuilder canonical = new StringBuilder();
    canonical.append("incremental=true\n");
    canonical.append("local_markdown_enabled=").append(scanConfiguration.localMarkdownEnabled()).append('\n');
    canonical.append("config_source=").append(scanConfiguration.configSource()).append('\n');
    canonical.append("config_status=").append(scanConfiguration.configFileStatus()).append('\n');
    canonical.append("profiles=");
    for (int index = 0; index < selectedProfiles.size(); index++) {
      if (index > 0) {
        canonical.append(',');
      }
      canonical.append(selectedProfiles.get(index).selector());
    }
    canonical.append('\n');
    return sha256(canonical.toString().getBytes(StandardCharsets.UTF_8));
  }

  private String configSha256(
      Map<String, InputFingerprint> inputFingerprints,
      ScanConfiguration scanConfiguration) {
    if (scanConfiguration.configFilePath() == null) {
      return null;
    }
    InputFingerprint fingerprint = inputFingerprints.get(
        "scan_config\0" + scanConfiguration.configFilePath());
    return fingerprint == null ? null : fingerprint.contentSha256();
  }

  private String inputsJsonl(List<InputFingerprint> inputs) {
    StringBuilder jsonl = new StringBuilder();
    for (InputFingerprint input : inputs) {
      jsonl.append("{\"cache_schema_version\":\"").append(CACHE_SCHEMA_VERSION)
          .append("\",\"path\":\"").append(jsonString(input.path()))
          .append("\",\"input_kind\":\"").append(input.inputKind())
          .append("\",\"content_sha256\":");
      appendNullableString(jsonl, input.contentSha256());
      jsonl.append(",\"size_bytes\":");
      appendNullableLong(jsonl, input.sizeBytes());
      jsonl.append("}\n");
    }
    return jsonl.toString();
  }

  private String outputsJsonl(List<OutputFingerprint> outputs) {
    StringBuilder jsonl = new StringBuilder();
    for (OutputFingerprint output : outputs) {
      jsonl.append("{\"cache_schema_version\":\"").append(CACHE_SCHEMA_VERSION)
          .append("\",\"path\":\"").append(jsonString(output.path()))
          .append("\",\"output_kind\":\"").append(output.outputKind())
          .append("\",\"content_sha256\":\"").append(output.contentSha256())
          .append("\",\"size_bytes\":").append(output.sizeBytes())
          .append("}\n");
    }
    return jsonl.toString();
  }

  private void writeCacheFile(Path canonicalRepositoryRoot, Path cacheDirectory, CacheFile file)
      throws IOException {
    Path target = cacheDirectory.resolve(file.fileName()).normalize();
    if (!isSafeCacheTarget(canonicalRepositoryRoot, target)) {
      throw new IOException("Cache file target is unsafe.");
    }
    Path tempFile = Files.createTempFile(cacheDirectory, "." + file.fileName() + ".", ".tmp");
    boolean moved = false;
    try {
      if (!ScanPathContainment.isRegularFileUnderRootNoFollow(canonicalRepositoryRoot, tempFile)) {
        throw new IOException("Temporary cache file is unsafe.");
      }
      Files.writeString(tempFile, file.content(), StandardCharsets.UTF_8);
      if (!isSafeCacheTarget(canonicalRepositoryRoot, target)) {
        throw new IOException("Cache file target is unsafe.");
      }
      moveCacheFile(tempFile, target);
      moved = true;
      if (!ScanPathContainment.isRegularFileUnderRootNoFollow(canonicalRepositoryRoot, target)) {
        throw new IOException("Cache file target is unsafe.");
      }
    } finally {
      if (!moved) {
        Files.deleteIfExists(tempFile);
      }
    }
  }

  private void moveCacheFile(Path tempFile, Path target) throws IOException {
    try {
      Files.move(tempFile, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    } catch (AtomicMoveNotSupportedException exception) {
      Files.move(tempFile, target, StandardCopyOption.REPLACE_EXISTING);
    }
  }

  boolean isSafeCacheTarget(Path canonicalRepositoryRoot, Path target) {
    if (Files.isSymbolicLink(target)) {
      return false;
    }
    if (!Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
      return true;
    }
    if (!ScanPathContainment.isRegularFileUnderRootNoFollow(canonicalRepositoryRoot, target)) {
      return false;
    }
    Long linkCount = hardLinkCount(target);
    return linkCount != null && linkCount <= 1;
  }

  Long hardLinkCount(Path target) {
    try {
      Object value = Files.getAttribute(target, "unix:nlink", LinkOption.NOFOLLOW_LINKS);
      if (value instanceof Number number) {
        return number.longValue();
      }
    } catch (IOException | UnsupportedOperationException | SecurityException exception) {
      return null;
    }
    return null;
  }

  private boolean safeDirectory(Path repositoryRoot, Path canonicalRepositoryRoot, Path directory) {
    return directory.startsWith(repositoryRoot)
        && !Files.isSymbolicLink(directory)
        && !hasSymbolicLinkSegment(repositoryRoot, directory)
        && Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS)
        && ScanPathContainment.realPathUnderRoot(canonicalRepositoryRoot, directory).isPresent();
  }

  boolean hasSymbolicLinkSegment(Path root, Path path) {
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

  private Optional<String> repositoryRelativePath(Path repositoryRoot, Path path) {
    Path normalizedRoot = repositoryRoot.toAbsolutePath().normalize();
    Path normalizedPath = path.toAbsolutePath().normalize();
    if (!normalizedPath.startsWith(normalizedRoot)) {
      return Optional.empty();
    }
    Path relative = normalizedRoot.relativize(normalizedPath);
    String pathText = relative.toString().replace(path.getFileSystem().getSeparator(), "/");
    if (pathText.isEmpty()) {
      return Optional.of("");
    }
    if (!safeCacheRelativePath(pathText)) {
      return Optional.empty();
    }
    return Optional.of(pathText);
  }

  boolean safeCacheRelativePath(String path) {
    if (path == null
        || path.isBlank()
        || path.startsWith("/")
        || path.startsWith("./")
        || path.contains("\\")) {
      return false;
    }
    for (String segment : path.split("/", -1)) {
      if (segment.isBlank() || ".".equals(segment) || "..".equals(segment)) {
        return false;
      }
    }
    return true;
  }

  private boolean isGeneratedSourceFamilyRoot(String relativePath) {
    return GENERATED_SOURCE_FAMILY_PATHS.stream()
        .anyMatch(family -> relativePath.equals(family) || relativePath.endsWith("/" + family));
  }

  private boolean shouldSkipNonGeneratedBuildOutputSubtree(String relativePath) {
    if (relativePath.isEmpty()) {
      return false;
    }
    List<String> segments = List.of(relativePath.split("/"));
    int targetIndex = segments.lastIndexOf("target");
    if (targetIndex >= 0) {
      return targetIndex + 1 < segments.size()
          && !"generated-sources".equals(segments.get(targetIndex + 1))
          && !"generated-test-sources".equals(segments.get(targetIndex + 1));
    }
    int buildIndex = segments.lastIndexOf("build");
    if (buildIndex >= 0) {
      if (buildIndex + 1 >= segments.size()) {
        return false;
      }
      if (!"generated".equals(segments.get(buildIndex + 1))) {
        return true;
      }
      return buildIndex + 2 < segments.size()
          && !"sources".equals(segments.get(buildIndex + 2))
          && !"source".equals(segments.get(buildIndex + 2));
    }
    return false;
  }

  private boolean isJavaSource(String path, String sourceRoot) {
    return path.endsWith(".java")
        && (path.startsWith(sourceRoot + "/") || path.contains("/" + sourceRoot + "/"));
  }

  private boolean isSupportedResourceConfig(String path) {
    String fileName = fileName(path);
    return (path.contains("/src/main/resources/") || path.contains("/src/test/resources/")
        || path.startsWith(MAIN_RESOURCE_ROOT_PATH + "/") || path.startsWith(TEST_RESOURCE_ROOT_PATH + "/"))
        && (LOGGING_CONFIG_FILE_NAMES.contains(fileName) || isApplicationConfig(fileName));
  }

  private Optional<String> standardRootInputKind(String path) {
    if (isStandardRootPath(path, JAVA_SOURCE_ROOT_PATH)) {
      return Optional.of("java_source_root_path");
    }
    if (isStandardRootPath(path, JAVA_TEST_ROOT_PATH)) {
      return Optional.of("java_test_root_path");
    }
    if (isStandardRootPath(path, MAIN_RESOURCE_ROOT_PATH)
        || isStandardRootPath(path, TEST_RESOURCE_ROOT_PATH)) {
      return Optional.of("resource_root_path");
    }
    return Optional.empty();
  }

  private boolean isStandardRootPath(String path, String rootPath) {
    return path.equals(rootPath) || path.endsWith("/" + rootPath);
  }

  private boolean isApplicationConfig(String fileName) {
    for (String extension : List.of(".properties", ".yml", ".yaml")) {
      if (fileName.equals("application" + extension)) {
        return true;
      }
      if (fileName.startsWith("application-") && fileName.endsWith(extension)) {
        String profile = fileName.substring("application-".length(), fileName.length() - extension.length());
        return !profile.isBlank();
      }
    }
    return false;
  }

  private boolean isOpenApiSpec(String path) {
    String lower = fileName(path).toLowerCase(Locale.ROOT);
    return lower.equals("openapi.yml")
        || lower.equals("openapi.yaml")
        || lower.equals("openapi.json")
        || lower.equals("swagger.yml")
        || lower.equals("swagger.yaml")
        || lower.equals("swagger.json");
  }

  private boolean isAcceptedLocalMarkdownCandidate(String path, ScanConfiguration scanConfiguration) {
    if (!(path.endsWith(".md") || path.endsWith(".markdown"))) {
      return false;
    }
    if (isDocumentExcluded(path)) {
      return false;
    }
    if (scanConfiguration.documentExcludes().stream().anyMatch(pattern -> pattern.matches(path))) {
      return false;
    }
    if (scanConfiguration.documentIncludes().stream().anyMatch(pattern -> pattern.matches(path))) {
      return true;
    }
    String fileName = fileName(path);
    return "README.md".equals(fileName)
        || "README.markdown".equals(fileName)
        || path.startsWith("docs/")
        || path.startsWith("adr/")
        || path.startsWith("adrs/")
        || path.contains("/docs/")
        || path.contains("/adr/")
        || path.contains("/adrs/");
  }

  private boolean isDocumentExcluded(String path) {
    for (String segment : path.split("/")) {
      String lower = segment.toLowerCase(Locale.ROOT);
      if (segment.startsWith(".")
          || DOCUMENT_EXCLUDED_SEGMENTS.contains(lower)
          || isGeneratedLike(lower)) {
        return true;
      }
    }
    return false;
  }

  private boolean isGeneratedLike(String segment) {
    return "generated".equals(segment)
        || segment.startsWith("generated-")
        || segment.endsWith("-generated")
        || segment.startsWith("generated_")
        || segment.endsWith("_generated");
  }

  private String fileName(String path) {
    int slash = path.lastIndexOf('/');
    return slash < 0 ? path : path.substring(slash + 1);
  }

  private String firstSegment(String path) {
    int slash = path.indexOf('/');
    return slash < 0 ? path : path.substring(0, slash);
  }

  private List<AgentOutputProfile> canonicalProfiles(List<AgentOutputProfile> profiles) {
    List<AgentOutputProfile> selected = new ArrayList<>();
    for (AgentOutputProfile profile : AgentOutputProfile.canonicalOrder()) {
      if (profiles.contains(profile)) {
        selected.add(profile);
      }
    }
    return List.copyOf(selected);
  }

  String sha256(byte[] bytes) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(bytes);
      StringBuilder hex = new StringBuilder("sha256:");
      for (byte value : hash) {
        hex.append(String.format(Locale.ROOT, "%02x", value & 0xff));
      }
      return hex.toString();
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is not available.", exception);
    }
  }

  private String jsonString(String value) {
    StringBuilder escaped = new StringBuilder();
    for (int offset = 0; offset < value.length();) {
      int codePoint = value.codePointAt(offset);
      offset += Character.charCount(codePoint);
      switch (codePoint) {
        case '"' -> escaped.append("\\\"");
        case '\\' -> escaped.append("\\\\");
        case '\b' -> escaped.append("\\b");
        case '\f' -> escaped.append("\\f");
        case '\n' -> escaped.append("\\n");
        case '\r' -> escaped.append("\\r");
        case '\t' -> escaped.append("\\t");
        default -> {
          if (codePoint < 0x20 || codePoint == 0x2028 || codePoint == 0x2029) {
            escaped.append(String.format(Locale.ROOT, "\\u%04x", codePoint));
          } else {
            escaped.appendCodePoint(codePoint);
          }
        }
      }
    }
    return escaped.toString();
  }

  private void appendNullableString(StringBuilder json, String value) {
    if (value == null) {
      json.append("null");
    } else {
      json.append('"').append(value).append('"');
    }
  }

  private void appendNullableLong(StringBuilder json, Long value) {
    if (value == null) {
      json.append("null");
    } else {
      json.append(value);
    }
  }

  public record CacheWriteResult(boolean written) {
    public static CacheWriteResult writtenResult() {
      return new CacheWriteResult(true);
    }

    public static CacheWriteResult skippedResult() {
      return new CacheWriteResult(false);
    }
  }

  record CacheMetadata(
      List<InputFingerprint> inputs,
      List<OutputFingerprint> outputs,
      String manifestJson) {
  }

  record InputFingerprint(
      String path,
      String inputKind,
      String contentSha256,
      Long sizeBytes) {
    private String key() {
      return inputKind + "\0" + path;
    }
  }

  record OutputFingerprint(
      String path,
      String outputKind,
      String contentSha256,
      Long sizeBytes) {
  }

  private record FileInputKind(String kind, int maxBytes) {
  }

  private record GeneratedSourceChildPathFingerprint(String path, String inputKind) {
  }

  private record OutputArtifact(String path, String kind) {
  }

  private record CacheFile(String fileName, String content) {
  }

  static final class CacheMetadataUnavailableException extends Exception {
  }

  private static final class UncheckedCacheMetadataUnavailableException extends RuntimeException {
    private final CacheMetadataUnavailableException cause;

    private UncheckedCacheMetadataUnavailableException(CacheMetadataUnavailableException cause) {
      this.cause = cause;
    }

    private CacheMetadataUnavailableException cause() {
      return cause;
    }
  }
}
