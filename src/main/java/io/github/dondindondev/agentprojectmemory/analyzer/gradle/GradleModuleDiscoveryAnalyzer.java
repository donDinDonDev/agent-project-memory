package io.github.dondindondev.agentprojectmemory.analyzer.gradle;

import io.github.dondindondev.agentprojectmemory.analyzer.EvidenceExcerpts;
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
  private static final String DECLARATION_KIND_SCAN_ROOT = "scan_root";
  private static final String DECLARATION_KIND_GRADLE_SETTINGS_INCLUDE = "gradle_settings_include";
  private static final String BUILD_FILE_SOURCE_TYPE = "build_file";
  private static final String HIGH_CONFIDENCE = "high";
  private static final String MAIN_SOURCE_ROOT = "src/main/java";
  private static final String TEST_SOURCE_ROOT = "src/test/java";
  private static final String MAIN_RESOURCE_ROOT = "src/main/resources";
  private static final String TEST_RESOURCE_ROOT = "src/test/resources";
  private static final String CATEGORY_GRADLE_MODULE = "gradle_module";
  private static final String SIGNAL_INVALID_PROJECT_PATH = "invalid_project_path";
  private static final String SIGNAL_DUPLICATE_PROJECT_PATH = "duplicate_project_path";
  private static final String SIGNAL_MISSING_PROJECT_DIRECTORY = "missing_project_directory";
  private static final String SIGNAL_UNSUPPORTED_MODULE = "unsupported_module";
  private static final String SIGNAL_UNSUPPORTED_DYNAMIC_INCLUDE = "unsupported_dynamic_include";
  private static final String SIGNAL_UNSUPPORTED_PROJECT_DIR_MAPPING = "unsupported_project_dir_mapping";
  private static final List<String> ROOT_GRADLE_FILES = List.of(
      "settings.gradle",
      "settings.gradle.kts",
      "build.gradle",
      "build.gradle.kts");
  private static final List<String> PROJECT_BUILD_FILES = List.of(
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
  private static final Comparator<MavenModuleItem> MODULE_ORDER = Comparator
      .comparingInt(GradleModuleDiscoveryAnalyzer::rootFirst)
      .thenComparing(MavenModuleItem::modulePath);

  public GradleModuleDiscoveryAnalysis analyze(Path repositoryRoot) throws IOException {
    Objects.requireNonNull(repositoryRoot, "repositoryRoot");

    Path normalizedRepositoryRoot = repositoryRoot.toAbsolutePath().normalize();
    Path canonicalRepositoryRoot = ScanPathContainment.canonicalRoot(normalizedRepositoryRoot);
    List<ScanDiagnostic> diagnostics = new ArrayList<>();
    Map<String, GradleBuildFileEvidence> evidence = new LinkedHashMap<>();
    List<GradleBuildFileItem> rootBuildFiles = new ArrayList<>();
    List<AcceptedGradleBuildFile> acceptedRootBuildFiles = new ArrayList<>();

    for (String rootBuildFileName : ROOT_GRADLE_FILES) {
      Path rootBuildFile = normalizedRepositoryRoot.resolve(rootBuildFileName).normalize();
      Optional<AcceptedGradleBuildFile> acceptedBuildFile = acceptedBuildFile(
          canonicalRepositoryRoot,
          rootBuildFile,
          rootBuildFileName,
          diagnostics);
      if (acceptedBuildFile.isEmpty()) {
        continue;
      }

      AcceptedGradleBuildFile accepted = acceptedBuildFile.orElseThrow();
      evidence.putIfAbsent(accepted.evidence().id(), accepted.evidence());
      rootBuildFiles.add(new GradleBuildFileItem(
          rootBuildFileName,
          rootBuildFileRole(rootBuildFileName),
          buildFileLanguage(rootBuildFileName),
          List.of(accepted.evidence().id())));
      acceptedRootBuildFiles.add(accepted);
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

    List<MavenModuleItem> moduleItems = new ArrayList<>();
    List<GradleModuleWarning> warnings = new ArrayList<>();
    List<String> sourceRoots = detectedSourceRoots(
        normalizedRepositoryRoot,
        canonicalRepositoryRoot,
        ROOT_MODULE_PATH);
    List<String> testRoots = detectedTestRoots(
        normalizedRepositoryRoot,
        canonicalRepositoryRoot,
        ROOT_MODULE_PATH);
    List<String> resourceRoots = detectedResourceRoots(
        normalizedRepositoryRoot,
        canonicalRepositoryRoot,
        ROOT_MODULE_PATH);
    boolean supported = !sourceRoots.isEmpty() || !testRoots.isEmpty() || !resourceRoots.isEmpty();
    moduleItems.add(new MavenModuleItem(
        ROOT_MODULE_ID,
        ROOT_MODULE_PATH,
        null,
        sourceRoots,
        testRoots,
        supported ? MODULE_SUPPORTED : MODULE_UNSUPPORTED,
        DECLARATION_KIND_SCAN_ROOT,
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
            .toList()));

    if (!supported) {
      warnings.add(unsupportedRootModuleWarning(rootBuildFiles));
    }

    SettingsDeclarations settingsDeclarations = settingsDeclarations(acceptedRootBuildFiles);
    settingsDeclarations.includes()
        .forEach(include -> evidence.putIfAbsent(include.evidence().id(), include.evidence()));
    settingsDeclarations.unsupportedDeclarations()
        .forEach(declaration -> {
          evidence.putIfAbsent(declaration.evidence().id(), declaration.evidence());
          warnings.add(unsupportedSettingsDeclarationWarning(declaration));
        });

    Map<String, StaticIncludeDeclaration> uniqueIncludes = new LinkedHashMap<>();
    for (StaticIncludeDeclaration include : settingsDeclarations.includes()) {
      if (include.normalizedPath().isEmpty()) {
        warnings.add(invalidProjectPathWarning(include));
        continue;
      }
      String modulePath = include.normalizedPath().orElseThrow().modulePath();
      if (uniqueIncludes.containsKey(modulePath)) {
        warnings.add(duplicateProjectPathWarning(modulePath, include));
        continue;
      }
      uniqueIncludes.put(modulePath, include);
    }

    for (StaticIncludeDeclaration include : uniqueIncludes.values()) {
      addIncludedProjectModule(
          moduleItems,
          warnings,
          evidence,
          diagnostics,
          normalizedRepositoryRoot,
          canonicalRepositoryRoot,
          include);
    }

    List<MavenModuleItem> sortedItems = moduleItems.stream()
        .sorted(MODULE_ORDER)
        .toList();
    Map<String, Integer> moduleOrder = moduleOrder(sortedItems);

    return new GradleModuleDiscoveryAnalysis(
        ANALYSIS_STATUS_ANALYZED,
        sortedItems,
        warnings.stream()
            .sorted(warningOrder(moduleOrder))
            .toList(),
        evidence.values().stream()
            .sorted(EVIDENCE_ORDER)
            .toList(),
        List.copyOf(rootBuildFiles),
        diagnostics);
  }

  private Optional<AcceptedGradleBuildFile> acceptedBuildFile(
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
    GradleBuildFileEvidence evidence = new GradleBuildFileEvidence(
        "ev:" + sourcePath + ":" + lineRange + ":build_file:" + symbolName,
        BUILD_FILE_SOURCE_TYPE,
        sourcePath,
        null,
        null,
        symbolName,
        line,
        line,
        buildFileExcerpt(sourcePath),
        HIGH_CONFIDENCE);
    return Optional.of(new AcceptedGradleBuildFile(sourcePath, lines, evidence));
  }

  private void addIncludedProjectModule(
      List<MavenModuleItem> moduleItems,
      List<GradleModuleWarning> warnings,
      Map<String, GradleBuildFileEvidence> evidence,
      List<ScanDiagnostic> diagnostics,
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      StaticIncludeDeclaration include) throws IOException {
    NormalizedGradleProjectPath normalizedPath = include.normalizedPath().orElseThrow();
    String modulePath = normalizedPath.modulePath();
    Path projectDirectory = repositoryRoot.resolve(modulePath).normalize();
    List<String> declarationEvidenceIds = List.of(include.evidence().id());
    List<GradleBuildFileItem> buildFiles = new ArrayList<>();
    buildFiles.add(new GradleBuildFileItem(
        include.sourcePath(),
        "settings",
        buildFileLanguage(include.sourcePath()),
        declarationEvidenceIds));

    if (existsButEscapesRoot(canonicalRepositoryRoot, projectDirectory)) {
      warnings.add(invalidProjectPathWarning(include));
      return;
    }

    if (!ScanPathContainment.isDirectoryUnderRoot(canonicalRepositoryRoot, projectDirectory)) {
      moduleItems.add(new MavenModuleItem(
          moduleId(modulePath),
          modulePath,
          null,
          List.of(),
          List.of(),
          SIGNAL_MISSING_PROJECT_DIRECTORY,
          DECLARATION_KIND_GRADLE_SETTINGS_INCLUDE,
          modulePath,
          declarationEvidenceIds,
          List.of(),
          List.of("gradle"),
          normalizedPath.gradleProjectPath(),
          buildFiles));
      warnings.add(missingProjectDirectoryWarning(modulePath, include));
      return;
    }

    for (String buildFileName : PROJECT_BUILD_FILES) {
      Path buildFile = projectDirectory.resolve(buildFileName).normalize();
      String sourcePath = modulePath + "/" + buildFileName;
      Optional<AcceptedGradleBuildFile> acceptedBuildFile = acceptedBuildFile(
          canonicalRepositoryRoot,
          buildFile,
          sourcePath,
          diagnostics);
      if (acceptedBuildFile.isEmpty()) {
        continue;
      }
      AcceptedGradleBuildFile accepted = acceptedBuildFile.orElseThrow();
      evidence.putIfAbsent(accepted.evidence().id(), accepted.evidence());
      buildFiles.add(new GradleBuildFileItem(
          sourcePath,
          "project_build",
          buildFileLanguage(sourcePath),
          List.of(accepted.evidence().id())));
    }

    List<String> sourceRoots = detectedSourceRoots(repositoryRoot, canonicalRepositoryRoot, modulePath);
    List<String> testRoots = detectedTestRoots(repositoryRoot, canonicalRepositoryRoot, modulePath);
    List<String> resourceRoots = detectedResourceRoots(repositoryRoot, canonicalRepositoryRoot, modulePath);
    boolean supported = !sourceRoots.isEmpty() || !testRoots.isEmpty() || !resourceRoots.isEmpty();

    moduleItems.add(new MavenModuleItem(
        moduleId(modulePath),
        modulePath,
        null,
        sourceRoots,
        testRoots,
        supported ? MODULE_SUPPORTED : MODULE_UNSUPPORTED,
        DECLARATION_KIND_GRADLE_SETTINGS_INCLUDE,
        modulePath,
        declarationEvidenceIds,
        List.of(),
        List.of("gradle"),
        normalizedPath.gradleProjectPath(),
        buildFiles));

    if (!supported) {
      warnings.add(unsupportedIncludedModuleWarning(modulePath, include, buildFiles));
    }
  }

  private List<String> detectedSourceRoots(
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      String modulePath) {
    return detectedModuleRoots(repositoryRoot, canonicalRepositoryRoot, modulePath, MAIN_SOURCE_ROOT);
  }

  private List<String> detectedTestRoots(
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      String modulePath) {
    return detectedModuleRoots(repositoryRoot, canonicalRepositoryRoot, modulePath, TEST_SOURCE_ROOT);
  }

  private List<String> detectedModuleRoots(
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      String modulePath,
      String rootName) {
    String root = ROOT_MODULE_PATH.equals(modulePath) ? rootName : modulePath + "/" + rootName;
    if (!ScanPathContainment.isDirectoryUnderRoot(canonicalRepositoryRoot, repositoryRoot.resolve(root))) {
      return List.of();
    }
    return List.of(root);
  }

  private List<String> detectedResourceRoots(
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      String modulePath) {
    List<String> roots = new ArrayList<>();
    roots.addAll(detectedModuleRoots(repositoryRoot, canonicalRepositoryRoot, modulePath, MAIN_RESOURCE_ROOT));
    roots.addAll(detectedModuleRoots(repositoryRoot, canonicalRepositoryRoot, modulePath, TEST_RESOURCE_ROOT));
    return roots.stream().sorted().toList();
  }

  private boolean existsButEscapesRoot(Path canonicalRepositoryRoot, Path path) {
    return Files.exists(path)
        && ScanPathContainment.realPathUnderRoot(canonicalRepositoryRoot, path).isEmpty();
  }

  private GradleModuleWarning unsupportedRootModuleWarning(List<GradleBuildFileItem> rootBuildFiles) {
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

  private GradleModuleWarning invalidProjectPathWarning(StaticIncludeDeclaration declaration) {
    return new GradleModuleWarning(
        "warning:gradle_module:invalid_project_path:decl:" + ordinalText(declaration.ordinal()),
        CATEGORY_GRADLE_MODULE,
        SIGNAL_INVALID_PROJECT_PATH,
        null,
        "Gradle settings include path is empty, absolute, escaping, has unsupported segments, or is otherwise outside the v1.1 static subset; this declaration is not analyzed.",
        declaration.sourcePath(),
        List.of(declaration.evidence().id()));
  }

  private GradleModuleWarning duplicateProjectPathWarning(
      String modulePath,
      StaticIncludeDeclaration declaration) {
    return new GradleModuleWarning(
        "warning:gradle_module:duplicate_project_path:" + modulePath
            + ":decl:" + ordinalText(declaration.ordinal()),
        CATEGORY_GRADLE_MODULE,
        SIGNAL_DUPLICATE_PROJECT_PATH,
        moduleId(modulePath),
        "Duplicate Gradle settings include ignored; v1.1 analyzes each normalized Gradle project path once.",
        declaration.sourcePath(),
        List.of(declaration.evidence().id()));
  }

  private GradleModuleWarning missingProjectDirectoryWarning(
      String modulePath,
      StaticIncludeDeclaration declaration) {
    return new GradleModuleWarning(
        "warning:gradle_module:missing_project_directory:" + modulePath,
        CATEGORY_GRADLE_MODULE,
        SIGNAL_MISSING_PROJECT_DIRECTORY,
        moduleId(modulePath),
        "Gradle settings include uses the default project directory mapping, but the project directory is absent; this module is not inspected.",
        declaration.sourcePath(),
        List.of(declaration.evidence().id()));
  }

  private GradleModuleWarning unsupportedIncludedModuleWarning(
      String modulePath,
      StaticIncludeDeclaration declaration,
      List<GradleBuildFileItem> buildFiles) {
    List<String> evidenceIds = new ArrayList<>();
    evidenceIds.add(declaration.evidence().id());
    buildFiles.stream()
        .skip(1)
        .flatMap(buildFile -> buildFile.evidenceIds().stream())
        .forEach(evidenceIds::add);
    String sourcePath = buildFiles.size() > 1 ? buildFiles.get(1).path() : declaration.sourcePath();
    return new GradleModuleWarning(
        "warning:gradle_module:unsupported_module:" + modulePath,
        CATEGORY_GRADLE_MODULE,
        SIGNAL_UNSUPPORTED_MODULE,
        moduleId(modulePath),
        "Gradle project has no supported Java source, test, or resource roots; the analyzer does not inspect this module.",
        sourcePath,
        evidenceIds);
  }

  private GradleModuleWarning unsupportedSettingsDeclarationWarning(UnsupportedSettingsDeclaration declaration) {
    return new GradleModuleWarning(
        "warning:gradle_module:" + declaration.signal() + ":decl:" + ordinalText(declaration.ordinal()),
        CATEGORY_GRADLE_MODULE,
        declaration.signal(),
        null,
        unsupportedSettingsDeclarationMessage(declaration.signal()),
        declaration.sourcePath(),
        List.of(declaration.evidence().id()));
  }

  private String unsupportedSettingsDeclarationMessage(String signal) {
    if (SIGNAL_UNSUPPORTED_PROJECT_DIR_MAPPING.equals(signal)) {
      return "Gradle projectDir remapping is directly visible but outside the v1.1 default project-directory mapping subset; remapped directories are not analyzed.";
    }
    return "Gradle settings declaration is directly visible but outside the v1.1 literal include subset; dynamic or composite-build declarations are not analyzed.";
  }

  private SettingsDeclarations settingsDeclarations(List<AcceptedGradleBuildFile> rootBuildFiles) {
    List<StaticIncludeDeclaration> includes = new ArrayList<>();
    List<UnsupportedSettingsDeclaration> unsupportedDeclarations = new ArrayList<>();
    int[] declarationOrdinal = new int[] {1};
    for (AcceptedGradleBuildFile buildFile : rootBuildFiles) {
      if (!isSettingsBuildFile(buildFile.sourcePath())) {
        continue;
      }
      addSettingsDeclarations(buildFile, includes, unsupportedDeclarations, declarationOrdinal);
    }
    return new SettingsDeclarations(includes, unsupportedDeclarations);
  }

  private void addSettingsDeclarations(
      AcceptedGradleBuildFile buildFile,
      List<StaticIncludeDeclaration> includes,
      List<UnsupportedSettingsDeclaration> unsupportedDeclarations,
      int[] declarationOrdinal) {
    boolean inBlockComment = false;
    for (int index = 0; index < buildFile.lines().size(); index++) {
      CommentStrippingResult stripped = stripGradleComments(buildFile.lines().get(index), inBlockComment);
      inBlockComment = stripped.inBlockComment();
      String code = stripped.code().trim();
      if (code.isEmpty()) {
        continue;
      }
      int lineNumber = index + 1;
      if (startsWithKeyword(code, "includeBuild") || startsWithKeyword(code, "includeFlat")) {
        unsupportedDeclarations.add(unsupportedSettingsDeclaration(
            buildFile.sourcePath(),
            lineNumber,
            declarationOrdinal[0]++,
            SIGNAL_UNSUPPORTED_DYNAMIC_INCLUDE,
            "gradle:include"));
        continue;
      }
      if (startsWithKeyword(code, "include")) {
        Optional<List<String>> literals = staticIncludeLiterals(code);
        if (literals.isEmpty()) {
          unsupportedDeclarations.add(unsupportedSettingsDeclaration(
              buildFile.sourcePath(),
              lineNumber,
              declarationOrdinal[0]++,
              SIGNAL_UNSUPPORTED_DYNAMIC_INCLUDE,
              "gradle:include"));
        } else {
          for (String literal : literals.orElseThrow()) {
            includes.add(staticIncludeDeclaration(
                buildFile.sourcePath(),
                lineNumber,
                declarationOrdinal[0]++,
                literal));
          }
        }
      }
      if (containsUnsupportedProjectDirMapping(code)) {
        unsupportedDeclarations.add(unsupportedSettingsDeclaration(
            buildFile.sourcePath(),
            lineNumber,
            declarationOrdinal[0]++,
            SIGNAL_UNSUPPORTED_PROJECT_DIR_MAPPING,
            "gradle:projectDir"));
      }
    }
  }

  private Optional<List<String>> staticIncludeLiterals(String code) {
    String body = code.substring("include".length()).trim();
    if (body.endsWith(";")) {
      body = body.substring(0, body.length() - 1).trim();
    }
    if (body.startsWith("(")) {
      if (!body.endsWith(")")) {
        return Optional.empty();
      }
      body = body.substring(1, body.length() - 1).trim();
    }
    if (body.isBlank()) {
      return Optional.empty();
    }
    return literalList(body);
  }

  private Optional<List<String>> literalList(String body) {
    List<String> literals = new ArrayList<>();
    int offset = 0;
    while (offset < body.length()) {
      offset = skipWhitespace(body, offset);
      if (offset >= body.length()) {
        return Optional.empty();
      }
      char quote = body.charAt(offset);
      if (quote != '"' && quote != '\'') {
        return Optional.empty();
      }
      int start = offset + 1;
      offset = start;
      while (offset < body.length() && body.charAt(offset) != quote) {
        if (body.charAt(offset) == '\\') {
          return Optional.empty();
        }
        offset++;
      }
      if (offset >= body.length()) {
        return Optional.empty();
      }
      String literal = body.substring(start, offset);
      if (containsInterpolationMarker(literal)) {
        return Optional.empty();
      }
      literals.add(literal);
      offset++;
      offset = skipWhitespace(body, offset);
      if (offset == body.length()) {
        return Optional.of(literals);
      }
      if (body.charAt(offset) != ',') {
        return Optional.empty();
      }
      offset++;
    }
    return Optional.empty();
  }

  private int skipWhitespace(String value, int offset) {
    int next = offset;
    while (next < value.length() && Character.isWhitespace(value.charAt(next))) {
      next++;
    }
    return next;
  }

  private StaticIncludeDeclaration staticIncludeDeclaration(
      String sourcePath,
      int lineNumber,
      int ordinal,
      String rawProjectPath) {
    NormalizedGradleProjectPath normalizedPath = normalizeGradleProjectPath(rawProjectPath);
    String ordinalText = ordinalText(ordinal);
    String symbolName = "gradle:include:decl:" + ordinalText;
    String excerpt = normalizedPath.normalizedPath()
        .map(path -> "Gradle static include declaration detected: " + path.gradleProjectPath())
        .orElse("Gradle static include declaration outside supported project-path subset detected.");
    GradleBuildFileEvidence evidence = settingsDeclarationEvidence(
        sourcePath,
        lineNumber,
        symbolName,
        excerpt);
    return new StaticIncludeDeclaration(
        ordinal,
        sourcePath,
        rawProjectPath,
        normalizedPath.normalizedPath(),
        evidence);
  }

  private UnsupportedSettingsDeclaration unsupportedSettingsDeclaration(
      String sourcePath,
      int lineNumber,
      int ordinal,
      String signal,
      String symbolPrefix) {
    String symbolName = symbolPrefix + ":decl:" + ordinalText(ordinal);
    String excerpt = SIGNAL_UNSUPPORTED_PROJECT_DIR_MAPPING.equals(signal)
        ? "Gradle projectDir mapping outside supported default mapping subset detected."
        : "Gradle settings declaration outside supported literal include subset detected.";
    GradleBuildFileEvidence evidence = settingsDeclarationEvidence(
        sourcePath,
        lineNumber,
        symbolName,
        excerpt);
    return new UnsupportedSettingsDeclaration(ordinal, sourcePath, signal, evidence);
  }

  private GradleBuildFileEvidence settingsDeclarationEvidence(
      String sourcePath,
      int lineNumber,
      String symbolName,
      String excerpt) {
    String lineRange = lineNumber + "-" + lineNumber;
    return new GradleBuildFileEvidence(
        "ev:" + sourcePath + ":" + lineRange + ":build_file:" + symbolName,
        BUILD_FILE_SOURCE_TYPE,
        sourcePath,
        null,
        null,
        symbolName,
        lineNumber,
        lineNumber,
        EvidenceExcerpts.bounded(excerpt),
        HIGH_CONFIDENCE);
  }

  private NormalizedGradleProjectPath normalizeGradleProjectPath(String rawPath) {
    String trimmedPath = rawPath.trim();
    if (trimmedPath.isEmpty()
        || !trimmedPath.equals(rawPath)
        || trimmedPath.contains("/")
        || trimmedPath.contains("\\")
        || trimmedPath.startsWith(":/")
        || trimmedPath.startsWith("/")
        || isWindowsAbsolutePath(trimmedPath)
        || containsControlCharacter(trimmedPath)) {
      return NormalizedGradleProjectPath.invalid();
    }

    String pathWithoutLeadingColon = trimmedPath.startsWith(":")
        ? trimmedPath.substring(1)
        : trimmedPath;
    if (pathWithoutLeadingColon.isEmpty()) {
      return NormalizedGradleProjectPath.invalid();
    }

    String[] segments = pathWithoutLeadingColon.split(":", -1);
    for (String segment : segments) {
      if (segment.isBlank() || ".".equals(segment) || "..".equals(segment)) {
        return NormalizedGradleProjectPath.invalid();
      }
    }
    return new NormalizedGradleProjectPath(
        String.join("/", segments),
        ":" + String.join(":", segments));
  }

  private boolean containsControlCharacter(String value) {
    for (int offset = 0; offset < value.length(); offset++) {
      if (Character.isISOControl(value.charAt(offset))) {
        return true;
      }
    }
    return false;
  }

  private boolean containsInterpolationMarker(String value) {
    return value.indexOf('$') >= 0;
  }

  private boolean isWindowsAbsolutePath(String path) {
    return path.length() >= 3
        && Character.isLetter(path.charAt(0))
        && path.charAt(1) == ':'
        && (path.charAt(2) == '/' || path.charAt(2) == '\\');
  }

  private CommentStrippingResult stripGradleComments(String line, boolean initialBlockComment) {
    StringBuilder code = new StringBuilder();
    boolean inBlockComment = initialBlockComment;
    Character quote = null;
    boolean escaped = false;
    for (int offset = 0; offset < line.length(); offset++) {
      char current = line.charAt(offset);
      char next = offset + 1 < line.length() ? line.charAt(offset + 1) : '\0';
      if (inBlockComment) {
        if (current == '*' && next == '/') {
          inBlockComment = false;
          offset++;
        }
        continue;
      }
      if (quote != null) {
        code.append(current);
        if (escaped) {
          escaped = false;
        } else if (current == '\\') {
          escaped = true;
        } else if (current == quote) {
          quote = null;
        }
        continue;
      }
      if (current == '\'' || current == '"') {
        quote = current;
        code.append(current);
      } else if (current == '/' && next == '/') {
        break;
      } else if (current == '/' && next == '*') {
        inBlockComment = true;
        offset++;
      } else {
        code.append(current);
      }
    }
    return new CommentStrippingResult(code.toString(), inBlockComment);
  }

  private boolean startsWithKeyword(String code, String keyword) {
    return code.startsWith(keyword)
        && (code.length() == keyword.length()
            || !Character.isJavaIdentifierPart(code.charAt(keyword.length())));
  }

  private boolean containsUnsupportedProjectDirMapping(String code) {
    return code.contains("project(")
        && code.contains(".projectDir")
        && code.contains("=");
  }

  private boolean isSettingsBuildFile(String path) {
    return path.endsWith("settings.gradle") || path.endsWith("settings.gradle.kts");
  }

  private String rootBuildFileRole(String path) {
    if (isSettingsBuildFile(path)) {
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
    if (isSettingsBuildFile(path)) {
      return "gradle:settings";
    }
    return "gradle:build";
  }

  private String buildFileExcerpt(String path) {
    if (isSettingsBuildFile(path)) {
      return "Gradle settings file detected: " + path;
    }
    return "Gradle project build file detected: " + path;
  }

  private Comparator<GradleModuleWarning> warningOrder(Map<String, Integer> moduleOrder) {
    return Comparator
        .comparing(GradleModuleWarning::category)
        .thenComparing(GradleModuleWarning::signal)
        .thenComparing(warning -> moduleOrder.getOrDefault(warning.moduleId(), Integer.MAX_VALUE))
        .thenComparing(GradleModuleWarning::sourcePath)
        .thenComparing(GradleModuleWarning::id);
  }

  private Map<String, Integer> moduleOrder(List<MavenModuleItem> moduleItems) {
    Map<String, Integer> order = new LinkedHashMap<>();
    for (int index = 0; index < moduleItems.size(); index++) {
      order.put(moduleItems.get(index).moduleId(), index);
    }
    return order;
  }

  private String moduleId(String modulePath) {
    return "module:" + modulePath;
  }

  private String ordinalText(int ordinal) {
    return String.format("%06d", ordinal);
  }

  private static int rootFirst(MavenModuleItem module) {
    return ROOT_MODULE_PATH.equals(module.modulePath()) ? 0 : 1;
  }

  private static String nullSafe(String value) {
    return value == null ? "" : value;
  }

  private record AcceptedGradleBuildFile(
      String sourcePath,
      List<String> lines,
      GradleBuildFileEvidence evidence) {
    private AcceptedGradleBuildFile {
      lines = List.copyOf(lines);
    }
  }

  private record SettingsDeclarations(
      List<StaticIncludeDeclaration> includes,
      List<UnsupportedSettingsDeclaration> unsupportedDeclarations) {
    private SettingsDeclarations {
      includes = List.copyOf(includes);
      unsupportedDeclarations = List.copyOf(unsupportedDeclarations);
    }
  }

  private record StaticIncludeDeclaration(
      int ordinal,
      String sourcePath,
      String rawProjectPath,
      Optional<NormalizedGradleProjectPath> normalizedPath,
      GradleBuildFileEvidence evidence) {
  }

  private record UnsupportedSettingsDeclaration(
      int ordinal,
      String sourcePath,
      String signal,
      GradleBuildFileEvidence evidence) {
  }

  private record NormalizedGradleProjectPath(
      String modulePath,
      String gradleProjectPath) {
    private static NormalizedGradleProjectPath invalid() {
      return new NormalizedGradleProjectPath(null, null);
    }

    private Optional<NormalizedGradleProjectPath> normalizedPath() {
      return modulePath == null ? Optional.empty() : Optional.of(this);
    }
  }

  private record CommentStrippingResult(
      String code,
      boolean inBlockComment) {
  }
}
