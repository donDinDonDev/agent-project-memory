package io.github.dondindondev.agentprojectmemory.analyzer.warnings;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        () -> assertTrue(warning.message().contains("does not parse specs")),
        () -> assertEquals("config_file", evidence.sourceType()),
        () -> assertEquals("src/main/resources/openapi.yml", evidence.sourcePath()),
        () -> assertEquals("openapi.yml", evidence.symbolName()),
        () -> assertTrue(evidence.id().contains(":config_file:openapi.yml")));
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

  private Path fixtureRoot() throws Exception {
    return Path.of(Objects.requireNonNull(
        getClass().getResource("/fixtures/hidden-http-warnings")).toURI());
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
