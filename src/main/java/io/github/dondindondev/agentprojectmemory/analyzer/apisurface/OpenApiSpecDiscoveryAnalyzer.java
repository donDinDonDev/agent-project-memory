package io.github.dondindondev.agentprojectmemory.analyzer.apisurface;

import io.github.dondindondev.agentprojectmemory.analyzer.EvidenceExcerpts;
import io.github.dondindondev.agentprojectmemory.analyzer.ScanPathContainment;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenModuleItem;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class OpenApiSpecDiscoveryAnalyzer {
  private static final String ANALYSIS_STATUS_ANALYZED = "analyzed";
  private static final String API_SPEC_SOURCE_TYPE = "api_spec";
  private static final String HIGH_CONFIDENCE = "high";
  private static final String MODULE_SUPPORTED = "supported";
  private static final int MAX_HEADER_BYTES = 16 * 1024;
  private static final int MAX_VERSION_LENGTH = 80;
  private static final Set<String> SUPPORTED_SPEC_FILENAMES = Set.of(
      "openapi.yml",
      "openapi.yaml",
      "openapi.json",
      "swagger.yml",
      "swagger.yaml",
      "swagger.json");
  private static final Pattern YAML_OPENAPI_VERSION = Pattern.compile(
      "^\\s*openapi\\s*:\\s*[\"']?([^\\s\"'#]{1," + MAX_VERSION_LENGTH + "})");
  private static final Pattern YAML_SWAGGER_VERSION = Pattern.compile(
      "^\\s*swagger\\s*:\\s*[\"']?([^\\s\"'#]{1," + MAX_VERSION_LENGTH + "})");
  private static final Pattern JSON_OPENAPI_VERSION = Pattern.compile(
      "\"openapi\"\\s*:\\s*\"([^\"]{1," + MAX_VERSION_LENGTH + "})\"");
  private static final Pattern JSON_SWAGGER_VERSION = Pattern.compile(
      "\"swagger\"\\s*:\\s*\"([^\"]{1," + MAX_VERSION_LENGTH + "})\"");
  private static final Comparator<OpenApiSpecFileFact> SPEC_FILE_ORDER = Comparator
      .comparingInt(OpenApiSpecFileFact::moduleOrder)
      .thenComparing(OpenApiSpecFileFact::specPath)
      .thenComparing(OpenApiSpecFileFact::specKind)
      .thenComparing(OpenApiSpecFileFact::format)
      .thenComparing(OpenApiSpecFileFact::id);

  public OpenApiSpecDiscoveryAnalysis analyze(
      Path repositoryRoot,
      List<MavenModuleItem> modules) throws IOException {
    Objects.requireNonNull(repositoryRoot, "repositoryRoot");
    Objects.requireNonNull(modules, "modules");

    Path normalizedRepositoryRoot = repositoryRoot.toAbsolutePath().normalize();
    Path canonicalRepositoryRoot = ScanPathContainment.canonicalRoot(normalizedRepositoryRoot);
    List<SupportedModuleRoot> supportedModuleRoots = supportedModuleRoots(
        normalizedRepositoryRoot,
        canonicalRepositoryRoot,
        modules);
    List<OpenApiSpecFileFact> specFiles = new ArrayList<>();
    List<ApiSpecEvidence> evidence = new ArrayList<>();

    for (Path specFile : repositoryFiles(normalizedRepositoryRoot, canonicalRepositoryRoot)) {
      String fileName = specFile.getFileName().toString();
      if (!SUPPORTED_SPEC_FILENAMES.contains(fileName.toLowerCase(Locale.ROOT))) {
        continue;
      }

      String specPath = repositoryRelativePath(normalizedRepositoryRoot, specFile);
      Optional<SupportedModuleRoot> module = owningModule(specFile, supportedModuleRoots);
      SpecHeader header = specHeader(specFile, fileName);
      String specKind = header.specKind();
      String evidenceId = evidenceId(specPath, header.lineStart(), header.lineEnd(), specKind);
      ApiSpecEvidence specEvidence = new ApiSpecEvidence(
          evidenceId,
          API_SPEC_SOURCE_TYPE,
          specPath,
          null,
          null,
          specKind,
          header.lineStart(),
          header.lineEnd(),
          EvidenceExcerpts.bounded(header.excerpt()),
          HIGH_CONFIDENCE);
      evidence.add(specEvidence);
      specFiles.add(new OpenApiSpecFileFact(
          specFileId(module.map(SupportedModuleRoot::moduleId).orElse(null), specPath),
          module.map(SupportedModuleRoot::moduleId).orElse(null),
          module.map(SupportedModuleRoot::moduleOrder).orElse(Integer.MAX_VALUE),
          specPath,
          format(fileName),
          specKind,
          header.version(),
          List.of(specEvidence.id())));
    }

    return new OpenApiSpecDiscoveryAnalysis(
        ANALYSIS_STATUS_ANALYZED,
        specFiles.stream().sorted(SPEC_FILE_ORDER).toList(),
        evidence);
  }

  private List<SupportedModuleRoot> supportedModuleRoots(
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      List<MavenModuleItem> modules) {
    List<SupportedModuleRoot> roots = new ArrayList<>();
    for (int index = 0; index < modules.size(); index++) {
      MavenModuleItem module = modules.get(index);
      if (!MODULE_SUPPORTED.equals(module.supportStatus())) {
        continue;
      }
      Path moduleRoot = ".".equals(module.modulePath())
          ? repositoryRoot
          : repositoryRoot.resolve(module.modulePath()).normalize();
      Optional<Path> realModuleRoot = ScanPathContainment.realPathUnderRoot(
          canonicalRepositoryRoot,
          moduleRoot);
      if (realModuleRoot.isEmpty() || !Files.isDirectory(realModuleRoot.orElseThrow())) {
        continue;
      }
      roots.add(new SupportedModuleRoot(
          module.moduleId(),
          module.modulePath(),
          index,
          moduleRoot,
          realModuleRoot.orElseThrow()));
    }
    return roots.stream()
        .sorted(Comparator
            .comparingInt((SupportedModuleRoot root) -> root.modulePath().length()).reversed()
            .thenComparingInt(SupportedModuleRoot::moduleOrder))
        .toList();
  }

  private List<Path> repositoryFiles(Path repositoryRoot, Path canonicalRepositoryRoot)
      throws IOException {
    if (!ScanPathContainment.isDirectoryUnderRoot(canonicalRepositoryRoot, repositoryRoot)) {
      return List.of();
    }

    try (Stream<Path> paths = Files.walk(repositoryRoot)) {
      return paths
          .filter(path -> Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)
              && ScanPathContainment.isRegularFileUnderRoot(canonicalRepositoryRoot, path)
              && !isExcluded(repositoryRoot, path))
          .sorted(Comparator.comparing(path -> repositoryRelativePath(repositoryRoot, path)))
          .toList();
    }
  }

  private boolean isExcluded(Path repositoryRoot, Path path) {
    Path relativePath = repositoryRoot.relativize(path.toAbsolutePath().normalize());
    for (Path part : relativePath) {
      String name = part.toString();
      if (".git".equals(name) || ".project-memory".equals(name) || "target".equals(name)) {
        return true;
      }
    }
    return false;
  }

  private Optional<SupportedModuleRoot> owningModule(
      Path specFile,
      List<SupportedModuleRoot> supportedModuleRoots) {
    Path normalizedSpecFile = specFile.toAbsolutePath().normalize();
    for (SupportedModuleRoot moduleRoot : supportedModuleRoots) {
      if (normalizedSpecFile.startsWith(moduleRoot.normalizedPath())) {
        return Optional.of(moduleRoot);
      }
    }
    return Optional.empty();
  }

  private SpecHeader specHeader(Path specFile, String fileName) throws IOException {
    String content = boundedHeaderContent(specFile);
    String lowerFileName = fileName.toLowerCase(Locale.ROOT);
    String fallbackKind = lowerFileName.startsWith("swagger") ? "swagger" : "openapi";
    String fallbackExcerpt = "spec file detected: " + fileName;

    String[] lines = content.split("\\R", -1);
    for (int index = 0; index < lines.length; index++) {
      String line = lines[index];
      Optional<VersionSignal> yamlSignal = yamlSignal(line);
      if (yamlSignal.isPresent()) {
        VersionSignal signal = yamlSignal.orElseThrow();
        return new SpecHeader(
            signal.specKind(),
            signal.version(),
            index + 1,
            index + 1,
            signal.specKind() + " version detected: " + signal.version());
      }
      Optional<VersionSignal> jsonSignal = jsonSignal(line);
      if (jsonSignal.isPresent()) {
        VersionSignal signal = jsonSignal.orElseThrow();
        return new SpecHeader(
            signal.specKind(),
            signal.version(),
            index + 1,
            index + 1,
            signal.specKind() + " version detected: " + signal.version());
      }
    }

    return new SpecHeader(fallbackKind, null, null, null, fallbackExcerpt);
  }

  private String boundedHeaderContent(Path specFile) throws IOException {
    byte[] bytes = new byte[MAX_HEADER_BYTES];
    int bytesRead = 0;
    try (InputStream input = Files.newInputStream(specFile)) {
      while (bytesRead < MAX_HEADER_BYTES) {
        int read = input.read(bytes, bytesRead, MAX_HEADER_BYTES - bytesRead);
        if (read < 0) {
          break;
        }
        bytesRead += read;
      }
    }
    if (bytesRead <= 0) {
      return "";
    }
    return new String(bytes, 0, bytesRead, StandardCharsets.UTF_8);
  }

  private Optional<VersionSignal> yamlSignal(String line) {
    Matcher openApi = YAML_OPENAPI_VERSION.matcher(line);
    if (openApi.find()) {
      return Optional.of(new VersionSignal("openapi", openApi.group(1)));
    }
    Matcher swagger = YAML_SWAGGER_VERSION.matcher(line);
    if (swagger.find()) {
      return Optional.of(new VersionSignal("swagger", swagger.group(1)));
    }
    return Optional.empty();
  }

  private Optional<VersionSignal> jsonSignal(String line) {
    Matcher openApi = JSON_OPENAPI_VERSION.matcher(line);
    if (openApi.find()) {
      return Optional.of(new VersionSignal("openapi", openApi.group(1)));
    }
    Matcher swagger = JSON_SWAGGER_VERSION.matcher(line);
    if (swagger.find()) {
      return Optional.of(new VersionSignal("swagger", swagger.group(1)));
    }
    return Optional.empty();
  }

  private String format(String fileName) {
    String lowerFileName = fileName.toLowerCase(Locale.ROOT);
    if (lowerFileName.endsWith(".yml") || lowerFileName.endsWith(".yaml")) {
      return "yaml";
    }
    if (lowerFileName.endsWith(".json")) {
      return "json";
    }
    return "unknown";
  }

  private String specFileId(String moduleId, String specPath) {
    String scope = moduleId == null || moduleId.isBlank() ? "unscoped" : moduleId;
    return "openapi_spec:" + scope + ":path:" + idKey(specPath);
  }

  private String evidenceId(
      String specPath,
      Integer lineStart,
      Integer lineEnd,
      String symbolName) {
    String lineRange = lineStart == null || lineEnd == null ? "unknown" : lineStart + "-" + lineEnd;
    return "ev:" + idKey(specPath) + ":" + lineRange + ":api_spec:" + idKey(symbolName);
  }

  static String idKey(String value) {
    byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
    StringBuilder key = new StringBuilder();
    for (byte rawByte : bytes) {
      int unsignedByte = rawByte & 0xFF;
      char character = (char) unsignedByte;
      if (isAllowedKeyCharacter(character)) {
        key.append(character);
      } else {
        key.append('%')
            .append(String.format(Locale.ROOT, "%02X", unsignedByte));
      }
    }
    return key.toString();
  }

  private static boolean isAllowedKeyCharacter(char character) {
    return (character >= 'A' && character <= 'Z')
        || (character >= 'a' && character <= 'z')
        || (character >= '0' && character <= '9')
        || character == '.'
        || character == '_'
        || character == '-'
        || character == '~'
        || character == '/'
        || character == '{'
        || character == '}';
  }

  private String repositoryRelativePath(Path repositoryRoot, Path file) {
    Path relativePath = repositoryRoot.relativize(file.toAbsolutePath().normalize());
    return relativePath.toString().replace(file.getFileSystem().getSeparator(), "/");
  }

  private record SupportedModuleRoot(
      String moduleId,
      String modulePath,
      int moduleOrder,
      Path normalizedPath,
      Path realPath) {
  }

  private record SpecHeader(
      String specKind,
      String version,
      Integer lineStart,
      Integer lineEnd,
      String excerpt) {
  }

  private record VersionSignal(String specKind, String version) {
  }
}
