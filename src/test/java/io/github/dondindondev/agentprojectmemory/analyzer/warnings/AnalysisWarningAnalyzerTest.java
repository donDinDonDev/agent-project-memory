package io.github.dondindondev.agentprojectmemory.analyzer.warnings;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class AnalysisWarningAnalyzerTest {
  @TempDir
  private Path tempDir;

  private final AnalysisWarningAnalyzer analyzer = new AnalysisWarningAnalyzer();

  @Test
  void openApiSpecFilenameCreatesHiddenHttpSurfaceWarning() throws Exception {
    AnalysisWarningAnalysis analysis = analyzeFixture();

    AnalysisWarningFact warning = warning(analysis, "openapi_spec_file");
    AnalysisWarningEvidence evidence = evidence(analysis, warning.evidenceIds().get(0));

    assertAll(
        () -> assertEquals("hidden_http_surface", warning.category()),
        () -> assertEquals("src/main/resources/openapi.yml", warning.sourcePath()),
        () -> assertTrue(warning.message().contains("api_surface.openapi.operations")),
        () -> assertEquals("config_file", evidence.sourceType()),
        () -> assertEquals("src/main/resources/openapi.yml", evidence.sourcePath()),
        () -> assertEquals("openapi.yml", evidence.symbolName()),
        () -> assertTrue(evidence.id().contains(":config_file:openapi.yml")));
  }

  @Test
  void jsonOpenApiSpecFilenamesCreateHiddenHttpSurfaceWarnings() throws Exception {
    Path repositoryRoot = tempDir.resolve("json-spec-warnings");
    writeFile(repositoryRoot.resolve("src/main/resources/openapi.json"), "{}");
    writeFile(repositoryRoot.resolve("docs/swagger.json"), "{}");

    AnalysisWarningAnalysis analysis = analyzer.analyze(repositoryRoot, List.of());

    assertAll(
        () -> assertEquals(
            List.of("docs/swagger.json", "src/main/resources/openapi.json"),
            analysis.warnings().stream().map(AnalysisWarningFact::sourcePath).toList()),
        () -> assertEquals(
            List.of("swagger.json", "openapi.json"),
            analysis.evidence().stream().map(AnalysisWarningEvidence::symbolName).toList()),
        () -> assertTrue(analysis.evidence().stream()
            .allMatch(evidence -> "config_file".equals(evidence.sourceType()))));
  }

  @Test
  void symlinkOpenApiSpecWarningPathEntryIsIgnored() throws Exception {
    Path repositoryRoot = tempDir.resolve("symlink-spec-warning");
    writeFile(repositoryRoot.resolve("shared/openapi.yml"), "openapi: 3.0.0");
    Files.createDirectories(repositoryRoot.resolve("src/main/resources"));
    createSymbolicLink(
        repositoryRoot.resolve("src/main/resources/openapi.yml"),
        repositoryRoot.resolve("shared/openapi.yml"));

    AnalysisWarningAnalysis analysis = analyzer.analyze(repositoryRoot, List.of());

    assertEquals(
        List.of("shared/openapi.yml"),
        analysis.warnings().stream().map(AnalysisWarningFact::sourcePath).toList());
  }

  @Test
  void prunesIrrelevantExcludedTreesBeforeCollectingOpenApiWarningCandidates()
      throws Exception {
    Path repositoryRoot = tempDir.resolve("excluded-openapi-warning-tree");
    for (int index = 0; index < 150; index++) {
      writeFile(repositoryRoot.resolve("target/noise-" + index + "/openapi.yml"), """
          openapi: 3.0.0
          """);
      writeFile(repositoryRoot.resolve(".project-memory/noise-" + index + "/swagger.yaml"), """
          swagger: "2.0"
          """);
    }
    writeFile(repositoryRoot.resolve("docs/openapi.yml"), """
        openapi: 3.0.0
        """);

    AnalysisWarningAnalysis analysis = analyzer.analyze(repositoryRoot, List.of());

    assertEquals(
        List.of("docs/openapi.yml"),
        analysis.warnings().stream()
            .filter(warning -> warning.signal().equals("openapi_spec_file"))
            .map(AnalysisWarningFact::sourcePath)
            .toList());
  }

  @Test
  void generatedSourceRootPathsCreatePathSignalWarningsWithoutReadingContents() throws Exception {
    Path repositoryRoot = tempDir.resolve("generated-source-roots");
    writeFile(
        repositoryRoot.resolve(
            "target/generated-sources/openapi/src/main/java/com/example/GeneratedController.java"),
        """
            package com.example;
            // FAKE_GENERATED_SOURCE_SECRET
            @org.springframework.web.bind.annotation.RestController
            class GeneratedController {}
            """);
    Files.createDirectories(repositoryRoot.resolve("target/generated-test-sources"));

    AnalysisWarningAnalysis analysis = analyzer.analyze(repositoryRoot, List.of());
    List<AnalysisWarningFact> generatedWarnings = analysis.warnings().stream()
        .filter(warning -> warning.signal().equals("generated_source_root_path_detected"))
        .toList();

    assertAll(
        () -> assertEquals(
            List.of(
                "target/generated-sources",
                "target/generated-sources/openapi",
                "target/generated-test-sources"),
            generatedWarnings.stream().map(AnalysisWarningFact::sourcePath).toList()),
        () -> assertTrue(generatedWarnings.stream()
            .allMatch(warning -> warning.category().equals("generated_source"))),
        () -> assertTrue(generatedWarnings.stream()
            .allMatch(warning -> warning.message().contains("does not read generated source contents"))),
        () -> assertEquals(
            List.of("path_signal", "path_signal", "path_signal"),
            analysis.evidence().stream().map(AnalysisWarningEvidence::sourceType).toList()),
        () -> assertTrue(analysis.evidence().stream()
            .allMatch(evidence -> evidence.symbolName().equals("generated_source_root_path_detected"))),
        () -> assertFalse(analysis.toString().contains("GeneratedController")),
        () -> assertFalse(analysis.toString().contains("FAKE_GENERATED_SOURCE_SECRET")));
  }

  @Test
  void generatedSourceRootPathWarningsUseStablePathEvidenceAndWarningIds()
      throws Exception {
    Path repositoryRoot = tempDir.resolve("encoded-generated-source-root");
    Files.createDirectories(repositoryRoot.resolve("target/generated-sources"));

    AnalysisWarningAnalysis analysis = analyzer.analyze(repositoryRoot, List.of());
    AnalysisWarningFact warning = warning(analysis, "generated_source_root_path_detected");
    AnalysisWarningEvidence evidence = evidence(analysis, warning.evidenceIds().get(0));

    assertAll(
        () -> assertEquals("target/generated-sources", warning.sourcePath()),
        () -> assertEquals(
            "warning:generated_source:generated_source_root_path_detected:path:target/generated-sources",
            warning.id()),
        () -> assertEquals(
            "ev:target/generated-sources:unknown:path_signal:generated_source_root_path_detected",
            evidence.id()),
        () -> assertFalse(Path.of(evidence.sourcePath()).isAbsolute()),
        () -> assertFalse(evidence.sourcePath().startsWith("./")),
        () -> assertFalse(evidence.sourcePath().contains("..")));
  }

  @Test
  void generatedSourceRootSymlinkSegmentsAreIgnored() throws Exception {
    Path repositoryRoot = tempDir.resolve("generated-source-symlink");
    Path outsideRoot = tempDir.resolve("outside-generated-source");
    writeFile(outsideRoot.resolve("openapi/src/main/java/com/example/GeneratedController.java"), """
        package com.example;
        // FAKE_OUTSIDE_GENERATED_SOURCE_SECRET
        class GeneratedController {}
        """);
    Files.createDirectories(repositoryRoot.resolve("target"));
    createSymbolicLink(repositoryRoot.resolve("target/generated-sources"), outsideRoot);

    AnalysisWarningAnalysis analysis = analyzer.analyze(repositoryRoot, List.of());

    assertAll(
        () -> assertFalse(analysis.warnings().stream()
            .anyMatch(warning -> warning.signal().equals("generated_source_root_path_detected"))),
        () -> assertFalse(analysis.toString().contains("FAKE_OUTSIDE_GENERATED_SOURCE_SECRET")));
  }

  @Test
  void repositoryRestResourceCreatesHiddenHttpSurfaceWarningWithAnnotationEvidence()
      throws Exception {
    AnalysisWarningAnalysis analysis = analyzeFixture();

    AnalysisWarningFact warning = warning(analysis, "repository_rest_resource");
    AnalysisWarningEvidence evidence = evidence(analysis, warning.evidenceIds().get(0));

    assertAll(
        () -> assertEquals("hidden_http_surface", warning.category()),
        () -> assertEquals("src/main/java/com/example/rest/HiddenRepositories.java", warning.sourcePath()),
        () -> assertTrue(warning.message().contains("does not expand endpoints")),
        () -> assertEquals("annotation", evidence.sourceType()),
        () -> assertEquals("com.example.rest.PetRepository", evidence.className()),
        () -> assertEquals("@RepositoryRestResource", evidence.symbolName()),
        () -> assertNotNull(evidence.lineStart()),
        () -> assertTrue(evidence.excerpt().contains("@RepositoryRestResource")));
  }

  @Test
  void sourceDeclaredRepositoryRestResourceAnnotationDoesNotCreateWarning()
      throws Exception {
    Path fixtureRoot = spoofedOriginsFixtureRoot();
    AnalysisWarningAnalysis analysis = analyzer.analyze(
        fixtureRoot,
        List.of(fixtureRoot.resolve("src/main/java")));

    assertAll(
        () -> assertFalse(analysis.warnings().stream()
            .anyMatch(warning -> warning.signal().equals("repository_rest_resource"))),
        () -> assertTrue(analysis.evidence().isEmpty()));
  }

  @Test
  void mavenOpenApiGeneratorPluginCreatesHiddenHttpSurfaceWarning() throws Exception {
    AnalysisWarningAnalysis analysis = analyzeFixture();

    AnalysisWarningFact warning = warning(analysis, "maven_openapi_swagger_codegen_plugin");
    AnalysisWarningEvidence evidence = evidence(analysis, warning.evidenceIds().get(0));
    long pluginWarningCount = analysis.warnings().stream()
        .filter(candidate -> candidate.signal().equals("maven_openapi_swagger_codegen_plugin"))
        .count();

    assertAll(
        () -> assertEquals(1, pluginWarningCount),
        () -> assertEquals("hidden_http_surface", warning.category()),
        () -> assertEquals("pom.xml", warning.sourcePath()),
        () -> assertTrue(warning.message().contains("does not run generation")),
        () -> assertEquals("build_file", evidence.sourceType()),
        () -> assertEquals("pom.xml", evidence.sourcePath()),
        () -> assertEquals("openapi-generator-maven-plugin", evidence.symbolName()),
        () -> assertEquals("<artifactId>openapi-generator-maven-plugin</artifactId>", evidence.excerpt()));
  }

  @Test
  void commentedMavenCodegenArtifactIdDoesNotCreateWarning() throws Exception {
    AnalysisWarningAnalysis analysis = analyzePom("""
        <project>
          <build>
            <plugins>
              <!-- <plugin><artifactId>openapi-generator-maven-plugin</artifactId></plugin> -->
            </plugins>
          </build>
        </project>
        """);

    assertEquals(0, mavenCodegenWarnings(analysis).size());
  }

  @Test
  void dependencyMavenCodegenArtifactIdDoesNotCreateWarning() throws Exception {
    AnalysisWarningAnalysis analysis = analyzePom("""
        <project>
          <dependencies>
            <dependency>
              <artifactId>openapi-generator-maven-plugin</artifactId>
            </dependency>
          </dependencies>
        </project>
        """);

    assertEquals(0, mavenCodegenWarnings(analysis).size());
  }

  @Test
  void buildPluginMavenCodegenArtifactIdCreatesWarning() throws Exception {
    AnalysisWarningAnalysis analysis = analyzePom("""
        <project>
          <build>
            <plugins>
              <plugin>
                <artifactId>openapi-generator-maven-plugin</artifactId>
              </plugin>
            </plugins>
          </build>
        </project>
        """);

    AnalysisWarningFact warning = mavenCodegenWarnings(analysis).get(0);
    AnalysisWarningEvidence evidence = evidence(analysis, warning.evidenceIds().get(0));

    assertAll(
        () -> assertEquals(1, mavenCodegenWarnings(analysis).size()),
        () -> assertEquals("pom.xml", warning.sourcePath()),
        () -> assertEquals("openapi-generator-maven-plugin", evidence.symbolName()),
        () -> assertEquals(5, evidence.lineStart()),
        () -> assertEquals("<artifactId>openapi-generator-maven-plugin</artifactId>", evidence.excerpt()));
  }

  @Test
  void pluginManagementMavenCodegenArtifactIdCreatesWarning() throws Exception {
    AnalysisWarningAnalysis analysis = analyzePom("""
        <project>
          <build>
            <pluginManagement>
              <plugins>
                <plugin>
                  <artifactId>swagger-codegen-maven-plugin</artifactId>
                </plugin>
              </plugins>
            </pluginManagement>
          </build>
        </project>
        """);

    AnalysisWarningFact warning = mavenCodegenWarnings(analysis).get(0);
    AnalysisWarningEvidence evidence = evidence(analysis, warning.evidenceIds().get(0));

    assertAll(
        () -> assertEquals(1, mavenCodegenWarnings(analysis).size()),
        () -> assertEquals("pom.xml", warning.sourcePath()),
        () -> assertEquals("swagger-codegen-maven-plugin", evidence.symbolName()),
        () -> assertEquals("<artifactId>swagger-codegen-maven-plugin</artifactId>", evidence.excerpt()));
  }

  @Test
  void duplicateMavenCodegenPluginArtifactIdDedupesToOneWarning() throws Exception {
    AnalysisWarningAnalysis analysis = analyzePom("""
        <project>
          <build>
            <pluginManagement>
              <plugins>
                <plugin>
                  <artifactId>openapi-generator-maven-plugin</artifactId>
                </plugin>
              </plugins>
            </pluginManagement>
            <plugins>
              <plugin>
                <artifactId>openapi-generator-maven-plugin</artifactId>
              </plugin>
            </plugins>
          </build>
        </project>
        """);

    assertEquals(1, mavenCodegenWarnings(analysis).size());
  }

  @Test
  void oversizedMavenWarningPomDoesNotCreateCodegenWarning() throws Exception {
    String oversizedPadding = "x".repeat(1024 * 1024 + 1);
    AnalysisWarningAnalysis analysis = analyzePom("""
        <project>
          <build>
            <plugins>
              <plugin>
                <artifactId>%s</artifactId>
              </plugin>
              <plugin>
                <artifactId>openapi-generator-maven-plugin</artifactId>
              </plugin>
            </plugins>
          </build>
        </project>
        """.formatted(oversizedPadding));

    assertAll(
        () -> assertEquals(0, mavenCodegenWarnings(analysis).size()),
        () -> assertFalse(analysis.toString().contains("openapi-generator-maven-plugin")));
  }

  @Test
  void oversizedPluginArtifactIdTextIsSkippedAndDoesNotHideLaterBoundedSignals()
      throws Exception {
    String oversizedArtifactId = "x".repeat(10_000);
    AnalysisWarningAnalysis analysis = analyzePom("""
        <project>
          <build>
            <plugins>
              <plugin>
                <artifactId>%s</artifactId>
              </plugin>
              <plugin>
                <artifactId>openapi-generator-maven-plugin</artifactId>
              </plugin>
            </plugins>
          </build>
        </project>
        """.formatted(oversizedArtifactId));

    assertAll(
        () -> assertEquals(1, mavenCodegenWarnings(analysis).size()),
        () -> assertFalse(analysis.toString().contains(oversizedArtifactId.substring(0, 512))));
  }

  @Test
  void mavenWarningPomSymlinkIsIgnoredWithoutReadingTarget() throws Exception {
    Path repositoryRoot = tempDir.resolve("warning-pom-symlink");
    writeFile(repositoryRoot.resolve("shared/pom.xml"), """
        <project>
          <build>
            <plugins>
              <plugin>
                <artifactId>openapi-generator-maven-plugin</artifactId>
              </plugin>
            </plugins>
          </build>
        </project>
        """);
    createSymbolicLink(repositoryRoot.resolve("pom.xml"), repositoryRoot.resolve("shared/pom.xml"));

    AnalysisWarningAnalysis analysis = analyzer.analyze(repositoryRoot, List.of());

    assertAll(
        () -> assertEquals(0, mavenCodegenWarnings(analysis).size()),
        () -> assertTrue(analysis.evidence().stream()
            .noneMatch(evidence -> "pom.xml".equals(evidence.sourcePath()))),
        () -> assertFalse(analysis.toString().contains("openapi-generator-maven-plugin")));
  }

  @Test
  void warningEvidenceIdsResolveInsideAnalysis() throws Exception {
    AnalysisWarningAnalysis analysis = analyzeFixture();
    List<String> evidenceIds = analysis.evidence().stream()
        .map(AnalysisWarningEvidence::id)
        .toList();

    for (AnalysisWarningFact warning : analysis.warnings()) {
      assertTrue(evidenceIds.containsAll(warning.evidenceIds()));
    }
  }

  private AnalysisWarningAnalysis analyzeFixture() throws Exception {
    Path fixtureRoot = fixtureRoot();
    return analyzer.analyze(fixtureRoot, List.of(fixtureRoot.resolve("src/main/java")));
  }

  private AnalysisWarningAnalysis analyzePom(String pomXml) throws Exception {
    Path repositoryRoot = tempDir.resolve("repo-" + System.nanoTime());
    Files.createDirectories(repositoryRoot);
    Files.writeString(repositoryRoot.resolve("pom.xml"), pomXml);
    return analyzer.analyze(repositoryRoot, List.of(repositoryRoot.resolve("src/main/java")));
  }

  private void writeFile(Path path, String content) throws Exception {
    Files.createDirectories(path.getParent());
    Files.writeString(path, content);
  }

  private void createSymbolicLink(Path link, Path target) throws Exception {
    try {
      Files.createSymbolicLink(link, target);
    } catch (UnsupportedOperationException | IOException | SecurityException exception) {
      assumeTrue(false, "symbolic links are unavailable: " + exception.getMessage());
    }
  }

  private Path fixtureRoot() throws Exception {
    return Path.of(Objects.requireNonNull(
        getClass().getResource("/fixtures/hidden-http-warnings")).toURI());
  }

  private Path spoofedOriginsFixtureRoot() throws Exception {
    return Path.of(Objects.requireNonNull(
        getClass().getResource("/fixtures/hidden-http-warnings-spoofed-origins")).toURI());
  }

  private AnalysisWarningFact warning(AnalysisWarningAnalysis analysis, String signal) {
    return analysis.warnings().stream()
        .filter(candidate -> candidate.signal().equals(signal))
        .findFirst()
        .orElseThrow();
  }

  private AnalysisWarningEvidence evidence(AnalysisWarningAnalysis analysis, String evidenceId) {
    return analysis.evidence().stream()
        .filter(candidate -> candidate.id().equals(evidenceId))
        .findFirst()
        .orElseThrow();
  }

  private List<AnalysisWarningFact> mavenCodegenWarnings(AnalysisWarningAnalysis analysis) {
    return analysis.warnings().stream()
        .filter(candidate -> candidate.signal().equals("maven_openapi_swagger_codegen_plugin"))
        .toList();
  }
}
