package io.github.dondindondev.agentprojectmemory.cache;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.dondindondev.agentprojectmemory.analyzer.ScanPathContainment;
import io.github.dondindondev.agentprojectmemory.profiles.AgentOutputProfile;
import io.github.dondindondev.agentprojectmemory.scanconfig.ScanConfiguration;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class IncrementalCacheMetadataValidator {
  private static final ObjectMapper JSON = new ObjectMapper();
  private static final int MAX_CACHE_METADATA_BYTES = 64 * 1024 * 1024;

  private final IncrementalCacheMetadataWriter metadataWriter;

  public IncrementalCacheMetadataValidator() {
    this(new IncrementalCacheMetadataWriter());
  }

  IncrementalCacheMetadataValidator(IncrementalCacheMetadataWriter metadataWriter) {
    this.metadataWriter = Objects.requireNonNull(metadataWriter, "metadataWriter");
  }

  public CacheValidationResult validateHit(
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      Path outputDirectory,
      ScanConfiguration scanConfiguration,
      List<AgentOutputProfile> selectedProfiles,
      String toolVersion) {
    try {
      Path normalizedRepositoryRoot = repositoryRoot.toAbsolutePath().normalize();
      Path normalizedOutputDirectory = outputDirectory.toAbsolutePath().normalize();
      Optional<Path> cacheDirectory = safeExistingCacheDirectory(
          canonicalRepositoryRoot,
          normalizedOutputDirectory);
      if (cacheDirectory.isEmpty()) {
        return CacheValidationResult.miss();
      }

      Path cache = cacheDirectory.orElseThrow();
      Path manifestPath = cache.resolve("manifest.json").normalize();
      Path inputsPath = cache.resolve("inputs.jsonl").normalize();
      Path outputsPath = cache.resolve("outputs.jsonl").normalize();
      if (!safeExistingCacheFile(canonicalRepositoryRoot, cache, manifestPath)
          || !safeExistingCacheFile(canonicalRepositoryRoot, cache, inputsPath)
          || !safeExistingCacheFile(canonicalRepositoryRoot, cache, outputsPath)) {
        return CacheValidationResult.miss();
      }

      IncrementalCacheMetadataWriter.CacheMetadata currentMetadata =
          metadataWriter.cacheMetadata(
              normalizedRepositoryRoot,
              canonicalRepositoryRoot,
              normalizedOutputDirectory,
              scanConfiguration,
              selectedProfiles,
              toolVersion);
      if (!manifestMatches(manifestPath, currentMetadata.manifestJson())) {
        return CacheValidationResult.miss();
      }
      if (!parseInputs(inputsPath).equals(currentMetadata.inputs())) {
        return CacheValidationResult.miss();
      }
      if (!parseOutputs(outputsPath).equals(currentMetadata.outputs())) {
        return CacheValidationResult.miss();
      }
      if (!profileManifestMatches(normalizedOutputDirectory, selectedProfiles)) {
        return CacheValidationResult.miss();
      }
      Optional<Integer> diagnosticCount = diagnosticCount(normalizedOutputDirectory);
      return diagnosticCount
          .map(CacheValidationResult::hit)
          .orElseGet(CacheValidationResult::miss);
    } catch (IOException
        | RuntimeException
        | IncrementalCacheMetadataWriter.CacheMetadataUnavailableException exception) {
      return CacheValidationResult.miss();
    }
  }

  private Optional<Path> safeExistingCacheDirectory(Path canonicalRepositoryRoot, Path outputDirectory) {
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
      if (Files.isSymbolicLink(directory)
          || !Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS)
          || metadataWriter.hasSymbolicLinkSegment(outputDirectory, directory)
          || ScanPathContainment.realPathUnderRoot(canonicalRepositoryRoot, directory).isEmpty()) {
        return Optional.empty();
      }
    }
    return Optional.of(cacheDirectory);
  }

  private boolean safeExistingCacheFile(
      Path canonicalRepositoryRoot,
      Path cacheDirectory,
      Path file) {
    return file.startsWith(cacheDirectory)
        && Files.exists(file, LinkOption.NOFOLLOW_LINKS)
        && !metadataWriter.hasSymbolicLinkSegment(cacheDirectory, file)
        && metadataWriter.isSafeCacheTarget(canonicalRepositoryRoot, file);
  }

  private boolean manifestMatches(Path manifestPath, String expectedManifestJson)
      throws IOException {
    JsonNode actual = JSON.readTree(readUtf8(manifestPath, MAX_CACHE_METADATA_BYTES));
    JsonNode expected = JSON.readTree(expectedManifestJson);
    return actual.equals(expected);
  }

  private List<IncrementalCacheMetadataWriter.InputFingerprint> parseInputs(Path inputsPath)
      throws IOException {
    List<IncrementalCacheMetadataWriter.InputFingerprint> inputs = new ArrayList<>();
    Set<String> keys = new HashSet<>();
    for (String line : readUtf8(inputsPath, MAX_CACHE_METADATA_BYTES).lines().toList()) {
      JsonNode node = parseJsonlObject(line);
      if (!hasOnlyFields(
          node,
          "cache_schema_version",
          "path",
          "input_kind",
          "content_sha256",
          "size_bytes")) {
        throw new IOException("Invalid cache input fields.");
      }
      if (!IncrementalCacheMetadataWriter.CACHE_SCHEMA_VERSION.equals(
          node.path("cache_schema_version").asText())) {
        throw new IOException("Invalid cache input schema.");
      }
      String path = requiredText(node, "path");
      String inputKind = requiredText(node, "input_kind");
      if (!metadataWriter.safeCacheRelativePath(path) || inputKind.isBlank()) {
        throw new IOException("Invalid cache input path.");
      }
      String contentSha256 = nullableHash(node, "content_sha256");
      Long sizeBytes = nullableSize(node, "size_bytes");
      if ((contentSha256 == null) != (sizeBytes == null)) {
        throw new IOException("Invalid cache input fingerprint.");
      }
      IncrementalCacheMetadataWriter.InputFingerprint input =
          new IncrementalCacheMetadataWriter.InputFingerprint(
              path,
              inputKind,
              contentSha256,
              sizeBytes);
      if (!keys.add(input.inputKind() + "\0" + input.path())) {
        throw new IOException("Duplicate cache input fingerprint.");
      }
      inputs.add(input);
    }
    return List.copyOf(inputs);
  }

  private List<IncrementalCacheMetadataWriter.OutputFingerprint> parseOutputs(Path outputsPath)
      throws IOException {
    List<IncrementalCacheMetadataWriter.OutputFingerprint> outputs = new ArrayList<>();
    Set<String> keys = new HashSet<>();
    for (String line : readUtf8(outputsPath, MAX_CACHE_METADATA_BYTES).lines().toList()) {
      JsonNode node = parseJsonlObject(line);
      if (!hasOnlyFields(
          node,
          "cache_schema_version",
          "path",
          "output_kind",
          "content_sha256",
          "size_bytes")) {
        throw new IOException("Invalid cache output fields.");
      }
      if (!IncrementalCacheMetadataWriter.CACHE_SCHEMA_VERSION.equals(
          node.path("cache_schema_version").asText())) {
        throw new IOException("Invalid cache output schema.");
      }
      String path = requiredText(node, "path");
      String outputKind = requiredText(node, "output_kind");
      String contentSha256 = requiredHash(node, "content_sha256");
      Long sizeBytes = requiredSize(node, "size_bytes");
      if (!metadataWriter.safeCacheRelativePath(path) || outputKind.isBlank()) {
        throw new IOException("Invalid cache output path.");
      }
      IncrementalCacheMetadataWriter.OutputFingerprint output =
          new IncrementalCacheMetadataWriter.OutputFingerprint(
              path,
              outputKind,
              contentSha256,
              sizeBytes);
      if (!keys.add(output.outputKind() + "\0" + output.path())) {
        throw new IOException("Duplicate cache output fingerprint.");
      }
      outputs.add(output);
    }
    return List.copyOf(outputs);
  }

  private JsonNode parseJsonlObject(String line) throws IOException {
    if (line == null || line.isBlank()) {
      throw new IOException("Invalid blank cache JSONL record.");
    }
    JsonNode node = JSON.readTree(line);
    if (!node.isObject()) {
      throw new IOException("Invalid cache JSONL record.");
    }
    return node;
  }

  private boolean profileManifestMatches(
      Path outputDirectory,
      List<AgentOutputProfile> selectedProfiles) throws IOException {
    if (selectedProfiles.isEmpty()) {
      return true;
    }
    Path manifestPath = outputDirectory.resolve("agent-profiles/manifest.json").normalize();
    JsonNode manifest = JSON.readTree(readUtf8(manifestPath, IncrementalCacheMetadataWriter.MAX_OUTPUT_BYTES));
    JsonNode generatedProfiles = manifest.path("generated_profiles");
    if (!generatedProfiles.isArray() || generatedProfiles.size() != selectedProfiles.size()) {
      return false;
    }
    for (int index = 0; index < selectedProfiles.size(); index++) {
      AgentOutputProfile profile = selectedProfiles.get(index);
      JsonNode generatedProfile = generatedProfiles.get(index);
      if (!profile.selector().equals(generatedProfile.path("name").asText())
          || !profile.artifactPath().equals(generatedProfile.path("artifact_path").asText())) {
        return false;
      }
    }
    return true;
  }

  private Optional<Integer> diagnosticCount(Path outputDirectory) throws IOException {
    Path projectMapPath = outputDirectory.resolve("project-map.json").normalize();
    JsonNode projectMap = JSON.readTree(
        readUtf8(projectMapPath, IncrementalCacheMetadataWriter.MAX_OUTPUT_BYTES));
    if (!IncrementalCacheMetadataWriter.PROJECT_MAP_SCHEMA_VERSION.equals(
        projectMap.path("schema_version").asText())) {
      return Optional.empty();
    }
    JsonNode diagnostics = projectMap.path("scan").path("diagnostics").path("items");
    if (!diagnostics.isArray()) {
      return Optional.empty();
    }
    return Optional.of(diagnostics.size());
  }

  private String readUtf8(Path file, int maxBytes) throws IOException {
    return new String(
        ScanPathContainment.readRegularFileBytesNoFollowStable(file, maxBytes),
        StandardCharsets.UTF_8);
  }

  private boolean hasOnlyFields(JsonNode node, String... expectedFields) {
    Set<String> expected = Set.of(expectedFields);
    Set<String> actual = new HashSet<>();
    Iterator<String> names = node.fieldNames();
    while (names.hasNext()) {
      actual.add(names.next());
    }
    return actual.equals(expected);
  }

  private String requiredText(JsonNode node, String fieldName) throws IOException {
    JsonNode value = node.path(fieldName);
    if (!value.isTextual() || value.asText().isBlank()) {
      throw new IOException("Invalid cache text field.");
    }
    return value.asText();
  }

  private String nullableHash(JsonNode node, String fieldName) throws IOException {
    JsonNode value = node.path(fieldName);
    if (value.isNull()) {
      return null;
    }
    if (!value.isTextual() || !value.asText().startsWith("sha256:")) {
      throw new IOException("Invalid cache hash field.");
    }
    return value.asText();
  }

  private String requiredHash(JsonNode node, String fieldName) throws IOException {
    String value = nullableHash(node, fieldName);
    if (value == null) {
      throw new IOException("Missing cache hash field.");
    }
    return value;
  }

  private Long nullableSize(JsonNode node, String fieldName) throws IOException {
    JsonNode value = node.path(fieldName);
    if (value.isNull()) {
      return null;
    }
    if (!value.canConvertToLong() || value.asLong() < 0) {
      throw new IOException("Invalid cache size field.");
    }
    return value.asLong();
  }

  private Long requiredSize(JsonNode node, String fieldName) throws IOException {
    Long value = nullableSize(node, fieldName);
    if (value == null) {
      throw new IOException("Missing cache size field.");
    }
    return value;
  }

  public record CacheValidationResult(boolean hit, int diagnosticCount) {
    static CacheValidationResult hit(int diagnosticCount) {
      return new CacheValidationResult(true, diagnosticCount);
    }

    static CacheValidationResult miss() {
      return new CacheValidationResult(false, 0);
    }
  }
}
