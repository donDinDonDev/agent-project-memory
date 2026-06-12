package io.github.dondindondev.agentprojectmemory.analyzer.apisurface;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenModuleItem;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class OpenApiSpecDiscoveryAnalyzerTest {
  @TempDir
  private Path tempDir;

  private final OpenApiSpecDiscoveryAnalyzer analyzer = new OpenApiSpecDiscoveryAnalyzer();

  @Test
  void discoversCommonSpecFilesInDeterministicModuleOrder() throws Exception {
    Path repositoryRoot = repository("sorting");
    writeFile(repositoryRoot.resolve("services/zeta/src/main/resources/swagger.json"), """
        {"swagger": "2.0", "paths": {"/ignored": {"get": {}}}}
        """);
    writeFile(repositoryRoot.resolve("services/alpha/src/main/resources/openapi.yaml"), """
        openapi: 3.0.3
        paths:
          /ignored:
            get: {}
        """);
    writeFile(repositoryRoot.resolve("docs/openapi.yml"), """
        openapi: 3.1.0
        """);

    OpenApiSpecDiscoveryAnalysis analysis = analyzer.analyze(
        repositoryRoot,
        List.of(
            supportedModule("module:services/zeta", "services/zeta"),
            supportedModule("module:services/alpha", "services/alpha")));

    List<OpenApiSpecFileFact> specs = analysis.specFiles();

    assertAll(
        () -> assertEquals("analyzed", analysis.analysisStatus()),
        () -> assertEquals(
            List.of(
                "services/zeta/src/main/resources/swagger.json",
                "services/alpha/src/main/resources/openapi.yaml",
                "docs/openapi.yml"),
            specs.stream().map(OpenApiSpecFileFact::specPath).toList()),
        () -> assertEquals("module:services/zeta", specs.get(0).moduleId()),
        () -> assertEquals("module:services/alpha", specs.get(1).moduleId()),
        () -> assertEquals(null, specs.get(2).moduleId()),
        () -> assertEquals("json", specs.get(0).format()),
        () -> assertEquals("yaml", specs.get(1).format()),
        () -> assertEquals("swagger", specs.get(0).specKind()),
        () -> assertEquals("openapi", specs.get(1).specKind()),
        () -> assertEquals("2.0", specs.get(0).version()),
        () -> assertEquals("3.0.3", specs.get(1).version()),
        () -> assertEquals("openapi_spec:unscoped:path:docs/openapi.yml", specs.get(2).id()),
        () -> assertTrue(specs.get(0).evidenceIds().get(0)
            .endsWith(":api_spec:swagger")),
        () -> assertTrue(specs.get(1).evidenceIds().get(0)
            .endsWith(":api_spec:openapi")),
        () -> assertEvidenceIdsResolve(analysis));
  }

  @Test
  void percentEncodesUnsafePathCharactersInFactAndEvidenceIds() throws Exception {
    Path repositoryRoot = repository("encoded-path");
    writeFile(repositoryRoot.resolve("src/main/resources/api specs:public/openapi.json"), """
        {
          "openapi": "3.0.0"
        }
        """);

    OpenApiSpecDiscoveryAnalysis analysis = analyzer.analyze(
        repositoryRoot,
        List.of(supportedModule("module:.", ".")));
    OpenApiSpecFileFact spec = analysis.specFiles().get(0);
    ApiSpecEvidence evidence = analysis.evidence().get(0);

    assertAll(
        () -> assertEquals("src/main/resources/api specs:public/openapi.json", spec.specPath()),
        () -> assertEquals(
            "openapi_spec:module:.:path:src/main/resources/api%20specs%3Apublic/openapi.json",
            spec.id()),
        () -> assertEquals(
            "ev:src/main/resources/api%20specs%3Apublic/openapi.json:2-2:api_spec:openapi",
            evidence.id()),
        () -> assertEquals("api_spec", evidence.sourceType()),
        () -> assertFalse(Path.of(evidence.sourcePath()).isAbsolute()),
        () -> assertFalse(evidence.sourcePath().startsWith("./")),
        () -> assertFalse(evidence.sourcePath().contains("..")));
  }

  @Test
  void assignsSpecToNearestSupportedNestedModule() throws Exception {
    Path repositoryRoot = repository("nested");
    writeFile(repositoryRoot.resolve("services/parent/nested/src/main/resources/openapi.yml"), """
        openapi: 3.0.1
        """);

    OpenApiSpecDiscoveryAnalysis analysis = analyzer.analyze(
        repositoryRoot,
        List.of(
            supportedModule("module:services/parent", "services/parent"),
            supportedModule("module:services/parent/nested", "services/parent/nested")));

    assertAll(
        () -> assertEquals(1, analysis.specFiles().size()),
        () -> assertEquals("module:services/parent/nested", analysis.specFiles().get(0).moduleId()),
        () -> assertTrue(analysis.specFiles().get(0).id()
            .startsWith("openapi_spec:module:services/parent/nested:path:")));
  }

  @Test
  void ignoresGeneratedOutputTargetAndProjectMemoryPaths() throws Exception {
    Path repositoryRoot = repository("excluded");
    writeFile(repositoryRoot.resolve("target/generated-sources/openapi/openapi.yml"), """
        openapi: 3.0.0
        """);
    writeFile(repositoryRoot.resolve(".project-memory/openapi.yml"), """
        openapi: 3.0.0
        """);
    writeFile(repositoryRoot.resolve("src/main/resources/openapi.yml"), """
        openapi: 3.0.0
        """);

    OpenApiSpecDiscoveryAnalysis analysis = analyzer.analyze(
        repositoryRoot,
        List.of(supportedModule("module:.", ".")));

    assertEquals(
        List.of("src/main/resources/openapi.yml"),
        analysis.specFiles().stream().map(OpenApiSpecFileFact::specPath).toList());
  }

  @Test
  void prunesIrrelevantExcludedTreesBeforeCollectingSpecCandidates() throws Exception {
    Path repositoryRoot = repository("excluded-tree-pruning");
    for (int index = 0; index < 150; index++) {
      writeFile(repositoryRoot.resolve("target/generated-sources/noise-" + index + "/openapi.yml"), """
          openapi: 3.0.0
          """);
      writeFile(repositoryRoot.resolve(".project-memory/noise-" + index + "/swagger.yaml"), """
          swagger: "2.0"
          """);
    }
    writeFile(repositoryRoot.resolve("src/main/resources/openapi.yml"), """
        openapi: 3.0.0
        """);

    OpenApiSpecDiscoveryAnalysis analysis = analyzer.analyze(
        repositoryRoot,
        List.of(supportedModule("module:.", ".")));

    assertEquals(
        List.of("src/main/resources/openapi.yml"),
        analysis.specFiles().stream().map(OpenApiSpecFileFact::specPath).toList());
  }

  @Test
  void symlinkEscapingRepositoryRootIsIgnored() throws Exception {
    Path repositoryRoot = repository("symlink-escape");
    Path outsideRoot = tempDir.resolve("outside");
    writeFile(outsideRoot.resolve("openapi.yml"), """
        openapi: 3.0.0
        external-secret: FAKE_OUTSIDE_OPENAPI_SECRET
        """);
    Files.createDirectories(repositoryRoot.resolve("src/main/resources"));
    createSymbolicLink(repositoryRoot.resolve("src/main/resources/openapi.yml"), outsideRoot.resolve("openapi.yml"));

    OpenApiSpecDiscoveryAnalysis analysis = analyzer.analyze(
        repositoryRoot,
        List.of(supportedModule("module:.", ".")));

    assertAll(
        () -> assertEquals(List.of(), analysis.specFiles()),
        () -> assertEquals(List.of(), analysis.evidence()),
        () -> assertFalse(analysis.toString().contains("FAKE_OUTSIDE_OPENAPI_SECRET")));
  }

  @Test
  void symlinkInsideRepositoryRootIsIgnoredToAvoidPathAndContentMismatch() throws Exception {
    Path repositoryRoot = repository("symlink-inside-root");
    writeFile(repositoryRoot.resolve("shared/openapi.yml"), """
        openapi: 3.0.0
        """);
    Files.createDirectories(repositoryRoot.resolve("services/orders/src/main/resources"));
    createSymbolicLink(
        repositoryRoot.resolve("services/orders/src/main/resources/openapi.yml"),
        repositoryRoot.resolve("shared/openapi.yml"));

    OpenApiSpecDiscoveryAnalysis analysis = analyzer.analyze(
        repositoryRoot,
        List.of(supportedModule("module:services/orders", "services/orders")));

    assertAll(
        () -> assertEquals(List.of("shared/openapi.yml"),
            analysis.specFiles().stream().map(OpenApiSpecFileFact::specPath).toList()),
        () -> assertEquals(null, analysis.specFiles().get(0).moduleId()));
  }

  @Test
  void boundsSpecFileCandidatesBeforeFactMaterialization() throws Exception {
    Path repositoryRoot = repository("spec-candidate-cap");
    writeFile(repositoryRoot.resolve("api/a/openapi.yml"), "openapi: 3.0.0\n");
    writeFile(repositoryRoot.resolve("api/b/openapi.yml"), "openapi: 3.0.1\n");
    writeFile(repositoryRoot.resolve("api/z/openapi.yml"), """
        openapi: 3.0.2
        x-secret: FAKE_SKIPPED_OPENAPI_SECRET
        """);
    OpenApiSpecDiscoveryAnalyzer cappedAnalyzer = new OpenApiSpecDiscoveryAnalyzer(2);

    OpenApiSpecDiscoveryAnalysis analysis = cappedAnalyzer.analyze(
        repositoryRoot,
        List.of(supportedModule("module:.", ".")));

    assertAll(
        () -> assertEquals(
            List.of("api/a/openapi.yml", "api/b/openapi.yml"),
            analysis.specFiles().stream().map(OpenApiSpecFileFact::specPath).toList()),
        () -> assertEquals(2, analysis.evidence().size()),
        () -> assertEvidenceIdsResolve(analysis),
        () -> assertFalse(analysis.toString().contains("api/z/openapi.yml")),
        () -> assertFalse(analysis.toString().contains("FAKE_SKIPPED_OPENAPI_SECRET")));
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

  private MavenModuleItem supportedModule(String moduleId, String modulePath) {
    return new MavenModuleItem(
        moduleId,
        modulePath,
        ".".equals(modulePath) ? "pom.xml" : modulePath + "/pom.xml",
        List.of(),
        List.of(),
        "supported",
        "scan_root",
        modulePath,
        List.of(),
        List.of());
  }

  private void assertEvidenceIdsResolve(OpenApiSpecDiscoveryAnalysis analysis) {
    Set<String> evidenceIds = analysis.evidence().stream()
        .map(ApiSpecEvidence::id)
        .collect(Collectors.toSet());
    List<String> referencedEvidenceIds = analysis.specFiles().stream()
        .flatMap(spec -> spec.evidenceIds().stream())
        .toList();

    assertTrue(evidenceIds.containsAll(referencedEvidenceIds));
  }

  private void createSymbolicLink(Path link, Path target) throws Exception {
    try {
      Files.createSymbolicLink(link, target);
    } catch (UnsupportedOperationException | IOException | SecurityException exception) {
      assumeTrue(false, "symbolic links are unavailable: " + exception.getMessage());
    }
  }
}
