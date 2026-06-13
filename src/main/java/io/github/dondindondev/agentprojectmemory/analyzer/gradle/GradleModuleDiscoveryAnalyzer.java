package io.github.dondindondev.agentprojectmemory.analyzer.gradle;

import io.github.dondindondev.agentprojectmemory.analyzer.ScanDiagnostic;
import io.github.dondindondev.agentprojectmemory.analyzer.ScanPathContainment;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenModuleItem;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class GradleModuleDiscoveryAnalyzer {
  private static final String ANALYSIS_STATUS_ANALYZED = "analyzed";
  private static final String ANALYSIS_STATUS_NOT_DETECTED = "not_detected";
  private static final String MODULE_SUPPORTED = "supported";
  private static final String MODULE_UNSUPPORTED = "unsupported";
  private static final String ROOT_MODULE_ID = "module:.";
  private static final String ROOT_MODULE_PATH = ".";
  private static final String ROOT_GRADLE_PROJECT_PATH = ":";
  private static final String BUILD_FILE_SOURCE_TYPE = "build_file";
  private static final String HIGH_CONFIDENCE = "high";
  private static final String MAIN_SOURCE_ROOT = "src/main/java";
  private static final String TEST_SOURCE_ROOT = "src/test/java";
  private static final String MAIN_RESOURCE_ROOT = "src/main/resources";
  private static final String TEST_RESOURCE_ROOT = "src/test/resources";
  private static final String CATEGORY_GRADLE_MODULE = "gradle_module";
  private static final String SIGNAL_UNSUPPORTED_MODULE = "unsupported_module";
  private static final List<String> ROOT_GRADLE_FILES = List.of(
      "settings.gradle",
      "settings.gradle.kts",
      "build.gradle",
      "build.gradle.kts");
  private static final Comparator<GradleBuildFileEvidence> EVIDENCE_ORDER = Comparator
      .comparing(GradleBuildFileEvidence::sourcePath)
      .thenComparing(evidence -> evidence.lineStart() == null ? Integer.MAX_VALUE : evidence.lineStart())
      .thenComparing(evidence -> evidence.lineEnd() == null ? Integer.MAX_VALUE : evidence.lineEnd())
      .thenComparing(evidence -> nullSafe(evidence.className()))
      .thenComparing(evidence -> nullSafe(evidence.methodName()))
      .thenComparing(GradleBuildFileEvidence::symbolName)
      .thenComparing(GradleBuildFileEvidence::id);

  public GradleModuleDiscoveryAnalysis analyze(Path repositoryRoot) throws IOException {
    Objects.requireNonNull(repositoryRoot, "repositoryRoot");

    Path normalizedRepositoryRoot = repositoryRoot.toAbsolutePath().normalize();
    Path canonicalRepositoryRoot = ScanPathContainment.canonicalRoot(normalizedRepositoryRoot);
    List<ScanDiagnostic> diagnostics = new ArrayList<>();
    Map<String, GradleBuildFileEvidence> evidence = new LinkedHashMap<>();
    List<GradleBuildFileItem> rootBuildFiles = new ArrayList<>();

    for (String rootBuildFileName : ROOT_GRADLE_FILES) {
      Path rootBuildFile = normalizedRepositoryRoot.resolve(rootBuildFileName).normalize();
      Optional<GradleBuildFileEvidence> buildFileEvidence = buildFileEvidence(
          canonicalRepositoryRoot,
          rootBuildFile,
          rootBuildFileName,
          diagnostics);
      if (buildFileEvidence.isEmpty()) {
        continue;
      }

      GradleBuildFileEvidence evidenceRecord = buildFileEvidence.orElseThrow();
      evidence.putIfAbsent(evidenceRecord.id(), evidenceRecord);
      rootBuildFiles.add(new GradleBuildFileItem(
          rootBuildFileName,
          rootBuildFileRole(rootBuildFileName),
          buildFileLanguage(rootBuildFileName),
          List.of(evidenceRecord.id())));
    }

    if (rootBuildFiles.isEmpty()) {
      return new GradleModuleDiscoveryAnalysis(
          ANALYSIS_STATUS_NOT_DETECTED,
          List.of(),
          List.of(),
          List.of(),
          List.of(),
          diagnostics);
    }

    List<String> sourceRoots = detectedModuleRoots(
        normalizedRepositoryRoot,
        canonicalRepositoryRoot,
        MAIN_SOURCE_ROOT);
    List<String> testRoots = detectedModuleRoots(
        normalizedRepositoryRoot,
        canonicalRepositoryRoot,
        TEST_SOURCE_ROOT);
    List<String> resourceRoots = detectedResourceRoots(
        normalizedRepositoryRoot,
        canonicalRepositoryRoot);
    boolean supported = !sourceRoots.isEmpty() || !testRoots.isEmpty() || !resourceRoots.isEmpty();
    MavenModuleItem rootModule = new MavenModuleItem(
        ROOT_MODULE_ID,
        ROOT_MODULE_PATH,
        null,
        sourceRoots,
        testRoots,
        supported ? MODULE_SUPPORTED : MODULE_UNSUPPORTED,
        "scan_root",
        ROOT_MODULE_PATH,
        List.of(),
        List.of(),
        List.of("gradle"),
        ROOT_GRADLE_PROJECT_PATH,
        rootBuildFiles.stream()
            .map(buildFile -> new GradleBuildFileItem(
                buildFile.path(),
                moduleBuildFileRole(buildFile.role()),
                buildFile.language(),
                buildFile.evidenceIds()))
            .toList());

    List<GradleModuleWarning> warnings = supported
        ? List.of()
        : List.of(unsupportedModuleWarning(rootBuildFiles));

    return new GradleModuleDiscoveryAnalysis(
        ANALYSIS_STATUS_ANALYZED,
        List.of(rootModule),
        warnings,
        evidence.values().stream()
            .sorted(EVIDENCE_ORDER)
            .toList(),
        List.copyOf(rootBuildFiles),
        diagnostics);
  }

  private Optional<GradleBuildFileEvidence> buildFileEvidence(
      Path canonicalRepositoryRoot,
      Path buildFile,
      String sourcePath,
      List<ScanDiagnostic> diagnostics) throws IOException {
    if (!Files.exists(buildFile, LinkOption.NOFOLLOW_LINKS)) {
      return Optional.empty();
    }
    if (!ScanPathContainment.isRegularFileUnderRootNoFollow(canonicalRepositoryRoot, buildFile)) {
      GradleBuildFileInput.addBuildFileReadSkippedDiagnostic(diagnostics, sourcePath);
      return Optional.empty();
    }

    List<String> lines;
    try {
      lines = GradleBuildFileInput.readBuildFileLines(buildFile);
    } catch (IOException | SecurityException exception) {
      if (exception instanceof IOException ioException
          && GradleBuildFileInput.isBuildFileSizeLimitExceeded(ioException)) {
        GradleBuildFileInput.addBuildFileSizeLimitDiagnostic(diagnostics, sourcePath);
        return Optional.empty();
      }
      GradleBuildFileInput.addBuildFileReadSkippedDiagnostic(diagnostics, sourcePath);
      return Optional.empty();
    }

    Integer line = lines.isEmpty() ? null : 1;
    String lineRange = line == null ? "unknown" : line + "-" + line;
    String symbolName = buildFileSymbolName(sourcePath);
    return Optional.of(new GradleBuildFileEvidence(
        "ev:" + sourcePath + ":" + lineRange + ":build_file:" + symbolName,
        BUILD_FILE_SOURCE_TYPE,
        sourcePath,
        null,
        null,
        symbolName,
        line,
        line,
        buildFileExcerpt(sourcePath),
        HIGH_CONFIDENCE));
  }

  private List<String> detectedModuleRoots(
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      String rootName) {
    Path root = repositoryRoot.resolve(rootName).normalize();
    if (!ScanPathContainment.isDirectoryUnderRoot(canonicalRepositoryRoot, root)) {
      return List.of();
    }
    return List.of(rootName);
  }

  private List<String> detectedResourceRoots(
      Path repositoryRoot,
      Path canonicalRepositoryRoot) {
    List<String> roots = new ArrayList<>();
    roots.addAll(detectedModuleRoots(repositoryRoot, canonicalRepositoryRoot, MAIN_RESOURCE_ROOT));
    roots.addAll(detectedModuleRoots(repositoryRoot, canonicalRepositoryRoot, TEST_RESOURCE_ROOT));
    return roots.stream().sorted().toList();
  }

  private GradleModuleWarning unsupportedModuleWarning(List<GradleBuildFileItem> rootBuildFiles) {
    List<String> evidenceIds = rootBuildFiles.stream()
        .flatMap(buildFile -> buildFile.evidenceIds().stream())
        .toList();
    return new GradleModuleWarning(
        "warning:gradle_module:unsupported_module:.",
        CATEGORY_GRADLE_MODULE,
        SIGNAL_UNSUPPORTED_MODULE,
        ROOT_MODULE_ID,
        "Gradle project has accepted root build files but no supported Java source, test, or resource roots; the analyzer does not inspect this module.",
        rootBuildFiles.get(0).path(),
        evidenceIds);
  }

  private String rootBuildFileRole(String path) {
    if (path.startsWith("settings.gradle")) {
      return "settings";
    }
    return "root_project_build";
  }

  private String moduleBuildFileRole(String rootBuildFileRole) {
    if ("root_project_build".equals(rootBuildFileRole)) {
      return "project_build";
    }
    return rootBuildFileRole;
  }

  private String buildFileLanguage(String path) {
    if (path.endsWith(".gradle.kts")) {
      return "kotlin_dsl";
    }
    return "groovy_dsl";
  }

  private String buildFileSymbolName(String path) {
    if (path.startsWith("settings.gradle")) {
      return "gradle:settings";
    }
    return "gradle:build";
  }

  private String buildFileExcerpt(String path) {
    if (path.startsWith("settings.gradle")) {
      return "Gradle settings file detected: " + path;
    }
    return "Gradle project build file detected: " + path;
  }

  private static String nullSafe(String value) {
    return value == null ? "" : value;
  }
}
