package io.github.dondindondev.agentprojectmemory.analyzer.gradle;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import io.github.dondindondev.agentprojectmemory.analyzer.ScanDiagnostic;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenModuleItem;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class GradleModuleDiscoveryAnalyzerTest {
  @TempDir
  private Path tempDir;

  private final GradleModuleDiscoveryAnalyzer analyzer = new GradleModuleDiscoveryAnalyzer();

  @Test
  void singleProjectRootGradleFilesDiscoverStandardRoots() throws Exception {
    Path repositoryRoot = repository("single-project-gradle");
    writeFile(repositoryRoot.resolve("settings.gradle.kts"), "rootProject.name = \"orders\"\n");
    writeFile(repositoryRoot.resolve("build.gradle.kts"), "plugins { java }\n");
    Files.createDirectories(repositoryRoot.resolve("src/main/java"));
    Files.createDirectories(repositoryRoot.resolve("src/test/java"));
    Files.createDirectories(repositoryRoot.resolve("src/main/resources"));
    Files.createDirectories(repositoryRoot.resolve("src/test/resources"));

    GradleModuleDiscoveryAnalysis analysis = analyzer.analyze(repositoryRoot);
    MavenModuleItem rootModule = analysis.items().get(0);

    assertAll(
        () -> assertEquals("analyzed", analysis.analysisStatus()),
        () -> assertEquals(1, analysis.items().size()),
        () -> assertEquals("module:.", rootModule.moduleId()),
        () -> assertEquals(".", rootModule.modulePath()),
        () -> assertEquals(List.of("gradle"), rootModule.buildSystems()),
        () -> assertEquals(":", rootModule.gradleProjectPath()),
        () -> assertEquals(List.of("src/main/java"), rootModule.sourceRoots()),
        () -> assertEquals(List.of("src/test/java"), rootModule.testRoots()),
        () -> assertEquals("supported", rootModule.supportStatus()),
        () -> assertEquals(
            List.of("settings.gradle.kts", "build.gradle.kts"),
            analysis.rootBuildFiles().stream().map(GradleBuildFileItem::path).toList()),
        () -> assertEquals(
            List.of("settings", "root_project_build"),
            analysis.rootBuildFiles().stream().map(GradleBuildFileItem::role).toList()),
        () -> assertEquals(
            List.of("settings", "project_build"),
            rootModule.gradleBuildFiles().stream().map(GradleBuildFileItem::role).toList()),
        () -> assertEquals(List.of(), analysis.warnings()),
        () -> assertEquals(List.of(), analysis.diagnostics()),
        () -> assertEvidenceIdsResolve(analysis));
  }

  @Test
  void settingsLiteralIncludesDiscoverSupportedChildProjectsInDeterministicOrder() throws Exception {
    Path repositoryRoot = repository("multi-project-gradle");
    writeFile(
        repositoryRoot.resolve("settings.gradle.kts"),
        """
        rootProject.name = "multi-project-gradle"
        include(":services:orders", "app")
        include("libs:common")
        """);
    writeFile(repositoryRoot.resolve("build.gradle.kts"), "plugins { java }\n");
    writeFile(repositoryRoot.resolve("app/build.gradle.kts"), "plugins { java }\n");
    writeFile(repositoryRoot.resolve("services/orders/build.gradle"), "plugins { id 'java' }\n");
    Files.createDirectories(repositoryRoot.resolve("src/main/java"));
    Files.createDirectories(repositoryRoot.resolve("app/src/main/java"));
    Files.createDirectories(repositoryRoot.resolve("app/src/test/java"));
    Files.createDirectories(repositoryRoot.resolve("services/orders/src/test/resources"));
    Files.createDirectories(repositoryRoot.resolve("libs/common/src/main/resources"));

    GradleModuleDiscoveryAnalysis analysis = analyzer.analyze(repositoryRoot);
    MavenModuleItem rootModule = moduleById(analysis, "module:.");
    MavenModuleItem appModule = moduleById(analysis, "module:app");
    MavenModuleItem commonModule = moduleById(analysis, "module:libs/common");
    MavenModuleItem ordersModule = moduleById(analysis, "module:services/orders");

    assertAll(
        () -> assertEquals("analyzed", analysis.analysisStatus()),
        () -> assertEquals(
            List.of("module:.", "module:app", "module:libs/common", "module:services/orders"),
            analysis.items().stream().map(MavenModuleItem::moduleId).toList()),
        () -> assertEquals(List.of("src/main/java"), rootModule.sourceRoots()),
        () -> assertEquals(":", rootModule.gradleProjectPath()),
        () -> assertEquals("gradle_settings_include", appModule.declarationKind()),
        () -> assertEquals("app", appModule.declaredPath()),
        () -> assertEquals(":app", appModule.gradleProjectPath()),
        () -> assertEquals(List.of("app/src/main/java"), appModule.sourceRoots()),
        () -> assertEquals(List.of("app/src/test/java"), appModule.testRoots()),
        () -> assertEquals(
            List.of("settings.gradle.kts", "app/build.gradle.kts"),
            appModule.gradleBuildFiles().stream().map(GradleBuildFileItem::path).toList()),
        () -> assertEquals("settings", appModule.gradleBuildFiles().get(0).role()),
        () -> assertEquals("project_build", appModule.gradleBuildFiles().get(1).role()),
        () -> assertEquals(":libs:common", commonModule.gradleProjectPath()),
        () -> assertEquals(List.of(), commonModule.sourceRoots()),
        () -> assertEquals(
            List.of("settings.gradle.kts"),
            commonModule.gradleBuildFiles().stream().map(GradleBuildFileItem::path).toList()),
        () -> assertEquals(":services:orders", ordersModule.gradleProjectPath()),
        () -> assertEquals(List.of(), ordersModule.sourceRoots()),
        () -> assertEquals(List.of(), ordersModule.testRoots()),
        () -> assertEquals(List.of(), analysis.warnings()),
        () -> assertEquals(List.of(), analysis.diagnostics()),
        () -> assertEvidenceIdsResolve(analysis));
  }

  @Test
  void settingsWarningsCoverInvalidDuplicateMissingUnsupportedDynamicAndProjectDirMapping()
      throws Exception {
    Path repositoryRoot = repository("gradle-settings-warnings");
    writeFile(
        repositoryRoot.resolve("settings.gradle"),
        """
        include(":app", "app", "../escape", "bad::path", ":missing", ":empty")
        include(projectName)
        includeBuild("../external")
        project(":app").projectDir = file("custom/app")
        """);
    writeFile(repositoryRoot.resolve("build.gradle"), "plugins { id 'java' }\n");
    writeFile(repositoryRoot.resolve("app/build.gradle"), "plugins { id 'java' }\n");
    writeFile(repositoryRoot.resolve("empty/build.gradle.kts"), "plugins { java }\n");
    Files.createDirectories(repositoryRoot.resolve("src/main/java"));
    Files.createDirectories(repositoryRoot.resolve("app/src/main/java"));

    GradleModuleDiscoveryAnalysis analysis = analyzer.analyze(repositoryRoot);
    MavenModuleItem appModule = moduleById(analysis, "module:app");
    MavenModuleItem missingModule = moduleById(analysis, "module:missing");
    MavenModuleItem emptyModule = moduleById(analysis, "module:empty");

    assertAll(
        () -> assertEquals(
            List.of("module:.", "module:app", "module:empty", "module:missing"),
            analysis.items().stream().map(MavenModuleItem::moduleId).toList()),
        () -> assertEquals("supported", appModule.supportStatus()),
        () -> assertEquals("missing_project_directory", missingModule.supportStatus()),
        () -> assertEquals("unsupported", emptyModule.supportStatus()),
        () -> assertEquals(
            List.of(
                "duplicate_project_path",
                "invalid_project_path",
                "invalid_project_path",
                "missing_project_directory",
                "unsupported_dynamic_include",
                "unsupported_dynamic_include",
                "unsupported_module",
                "unsupported_project_dir_mapping"),
            analysis.warnings().stream().map(GradleModuleWarning::signal).toList()),
        () -> assertEquals(
            "warning:gradle_module:duplicate_project_path:app:decl:000002",
            analysis.warnings().get(0).id()),
        () -> assertTrue(analysis.warnings().stream()
            .filter(warning -> "invalid_project_path".equals(warning.signal()))
            .allMatch(warning -> warning.moduleId() == null)),
        () -> assertEquals(List.of(), analysis.diagnostics()),
        () -> assertEvidenceIdsResolve(analysis));
  }

  @Test
  void settingsStringInterpolationIsNotAcceptedAsStaticProjectPath() throws Exception {
    Path repositoryRoot = repository("gradle-interpolation");
    writeFile(
        repositoryRoot.resolve("settings.gradle.kts"),
        """
        include(":app")
        include("$module")
        include(":${module}")
        """);
    writeFile(repositoryRoot.resolve("build.gradle.kts"), "plugins { java }\n");
    Files.createDirectories(repositoryRoot.resolve("src/main/java"));
    Files.createDirectories(repositoryRoot.resolve("app/src/main/java"));
    Files.createDirectories(repositoryRoot.resolve("$module/src/main/java"));
    Files.createDirectories(repositoryRoot.resolve("${module}/src/main/java"));

    GradleModuleDiscoveryAnalysis analysis = analyzer.analyze(repositoryRoot);

    assertAll(
        () -> assertEquals(
            List.of("module:.", "module:app"),
            analysis.items().stream().map(MavenModuleItem::moduleId).toList()),
        () -> assertEquals(
            List.of("unsupported_dynamic_include", "unsupported_dynamic_include"),
            analysis.warnings().stream().map(GradleModuleWarning::signal).toList()),
        () -> assertTrue(analysis.warnings().stream().allMatch(warning -> warning.moduleId() == null)),
        () -> assertEquals(List.of(), analysis.diagnostics()),
        () -> assertEvidenceIdsResolve(analysis));
  }

  @Test
  void rootGradleBuildWithoutSupportedRootsCreatesUnsupportedWarning() throws Exception {
    Path repositoryRoot = repository("unsupported-gradle");
    writeFile(repositoryRoot.resolve("build.gradle"), "plugins { id 'java' }\n");

    GradleModuleDiscoveryAnalysis analysis = analyzer.analyze(repositoryRoot);
    MavenModuleItem rootModule = analysis.items().get(0);
    GradleModuleWarning warning = analysis.warnings().get(0);

    assertAll(
        () -> assertEquals("analyzed", analysis.analysisStatus()),
        () -> assertEquals("unsupported", rootModule.supportStatus()),
        () -> assertEquals(List.of(), rootModule.sourceRoots()),
        () -> assertEquals(List.of(), rootModule.testRoots()),
        () -> assertEquals("warning:gradle_module:unsupported_module:.", warning.id()),
        () -> assertEquals("gradle_module", warning.category()),
        () -> assertEquals("unsupported_module", warning.signal()),
        () -> assertEquals("module:.", warning.moduleId()),
        () -> assertEquals("build.gradle", warning.sourcePath()),
        () -> assertEquals(rootModule.gradleBuildFiles().get(0).evidenceIds(), warning.evidenceIds()),
        () -> assertEvidenceIdsResolve(analysis));
  }

  @Test
  void oversizedRootGradleBuildFileIsSkippedWithDiagnostic() throws Exception {
    Path repositoryRoot = repository("oversized-gradle");
    writeOversizedGradleBuildFile(repositoryRoot.resolve("build.gradle.kts"));

    GradleModuleDiscoveryAnalysis analysis = analyzer.analyze(repositoryRoot);

    assertAll(
        () -> assertEquals("not_detected", analysis.analysisStatus()),
        () -> assertEquals(List.of(), analysis.items()),
        () -> assertEquals(List.of(), analysis.rootBuildFiles()),
        () -> assertEquals(List.of(), analysis.evidence()),
        () -> assertEquals(1, analysis.diagnostics().size()),
        () -> assertGradleSizeDiagnostic(analysis.diagnostics().get(0), "build.gradle.kts"));
  }

  @Test
  void rootGradleBuildFileSymlinkIsSkippedWithoutOutsideEvidence() throws Exception {
    Path repositoryRoot = repository("gradle-symlink");
    Path outsideBuildFile = tempDir.resolve("outside-build.gradle.kts");
    Files.writeString(outsideBuildFile, "// OUTSIDE_GRADLE_SECRET_LINE\nplugins { java }\n");
    createSymbolicLink(repositoryRoot.resolve("build.gradle.kts"), outsideBuildFile);

    GradleModuleDiscoveryAnalysis analysis = analyzer.analyze(repositoryRoot);

    assertAll(
        () -> assertEquals("not_detected", analysis.analysisStatus()),
        () -> assertEquals(List.of(), analysis.items()),
        () -> assertEquals(List.of(), analysis.rootBuildFiles()),
        () -> assertEquals(List.of(), analysis.evidence()),
        () -> assertEquals(1, analysis.diagnostics().size()),
        () -> assertEquals(
            GradleBuildFileInput.DIAGNOSTIC_CODE_GRADLE_BUILD_FILE_READ_SKIPPED,
            analysis.diagnostics().get(0).code()),
        () -> assertEquals("build.gradle.kts", analysis.diagnostics().get(0).path()),
        () -> assertFalse(analysis.evidence().stream()
            .anyMatch(evidence -> evidence.excerpt().contains("OUTSIDE_GRADLE_SECRET_LINE"))));
  }

  @Test
  void rootGradleBuildFileHardlinkIsSkippedWithoutLinkedEvidence() throws Exception {
    Path repositoryRoot = repository("gradle-hardlink");
    Path outsideBuildFile = tempDir.resolve("outside-hardlinked-build.gradle.kts");
    Files.writeString(outsideBuildFile, "// HARDLINKED_GRADLE_SECRET_LINE\nplugins { java }\n");
    createHardLink(repositoryRoot.resolve("build.gradle.kts"), outsideBuildFile);

    GradleModuleDiscoveryAnalysis analysis = analyzer.analyze(repositoryRoot);

    assertAll(
        () -> assertEquals("not_detected", analysis.analysisStatus()),
        () -> assertEquals(List.of(), analysis.items()),
        () -> assertEquals(List.of(), analysis.rootBuildFiles()),
        () -> assertEquals(List.of(), analysis.evidence()),
        () -> assertEquals(1, analysis.diagnostics().size()),
        () -> assertEquals(
            GradleBuildFileInput.DIAGNOSTIC_CODE_GRADLE_BUILD_FILE_READ_SKIPPED,
            analysis.diagnostics().get(0).code()),
        () -> assertEquals("build.gradle.kts", analysis.diagnostics().get(0).path()),
        () -> assertFalse(analysis.toString().contains("HARDLINKED_GRADLE_SECRET_LINE")),
        () -> assertFalse(analysis.toString().contains(tempDir.toString())));
  }

  private Path repository(String name) throws Exception {
    Path repositoryRoot = tempDir.resolve(name);
    Files.createDirectories(repositoryRoot);
    return repositoryRoot;
  }

  private void writeFile(Path path, String content) throws Exception {
    Files.createDirectories(path.getParent());
    Files.writeString(path, content);
  }

  private void writeOversizedGradleBuildFile(Path path) throws Exception {
    writeFile(path, "// " + "x".repeat(GradleBuildFileInput.MAX_GRADLE_BUILD_FILE_BYTES) + "\n");
  }

  private void createSymbolicLink(Path link, Path target) throws Exception {
    try {
      Files.createSymbolicLink(link, target);
    } catch (UnsupportedOperationException | IOException | SecurityException exception) {
      assumeTrue(false, "symbolic links are unavailable: " + exception.getMessage());
    }
  }

  private void createHardLink(Path link, Path existing) throws Exception {
    try {
      Files.createLink(link, existing);
    } catch (UnsupportedOperationException | IOException | SecurityException exception) {
      assumeTrue(false, "hard links are unavailable: " + exception.getMessage());
    }
  }

  private MavenModuleItem moduleById(GradleModuleDiscoveryAnalysis analysis, String moduleId) {
    return analysis.items().stream()
        .filter(item -> item.moduleId().equals(moduleId))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Missing module " + moduleId));
  }

  private void assertGradleSizeDiagnostic(ScanDiagnostic diagnostic, String sourcePath) {
    assertAll(
        () -> assertEquals(
            GradleBuildFileInput.DIAGNOSTIC_CODE_GRADLE_BUILD_FILE_BYTES_CAP_EXCEEDED,
            diagnostic.code()),
        () -> assertEquals("warning", diagnostic.severity()),
        () -> assertEquals("gradle", diagnostic.category()),
        () -> assertEquals(sourcePath, diagnostic.path()),
        () -> assertEquals(GradleBuildFileInput.MAX_GRADLE_BUILD_FILE_BYTES, diagnostic.count()));
  }

  private void assertEvidenceIdsResolve(GradleModuleDiscoveryAnalysis analysis) {
    Set<String> evidenceIds = analysis.evidence().stream()
        .map(GradleBuildFileEvidence::id)
        .collect(Collectors.toSet());
    List<String> referencedEvidenceIds = new ArrayList<>();
    for (GradleBuildFileItem buildFile : analysis.rootBuildFiles()) {
      referencedEvidenceIds.addAll(buildFile.evidenceIds());
    }
    for (MavenModuleItem item : analysis.items()) {
      item.gradleBuildFiles().forEach(buildFile -> referencedEvidenceIds.addAll(buildFile.evidenceIds()));
    }
    for (GradleModuleWarning warning : analysis.warnings()) {
      referencedEvidenceIds.addAll(warning.evidenceIds());
    }

    assertTrue(evidenceIds.containsAll(referencedEvidenceIds));
    assertTrue(analysis.evidence().stream()
        .allMatch(evidence -> !Path.of(evidence.sourcePath()).isAbsolute()
            && !evidence.sourcePath().startsWith("./")));
  }
}
