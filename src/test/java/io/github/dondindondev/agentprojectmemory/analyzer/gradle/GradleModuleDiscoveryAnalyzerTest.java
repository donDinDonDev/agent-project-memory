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
