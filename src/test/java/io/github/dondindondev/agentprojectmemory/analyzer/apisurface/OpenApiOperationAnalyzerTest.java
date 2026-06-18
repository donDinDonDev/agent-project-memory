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

final class OpenApiOperationAnalyzerTest {
  @TempDir
  private Path tempDir;

  private final OpenApiSpecDiscoveryAnalyzer discoveryAnalyzer = new OpenApiSpecDiscoveryAnalyzer();
  private final OpenApiOperationAnalyzer operationAnalyzer = new OpenApiOperationAnalyzer();

  @Test
  void extractsMinimalYamlAndJsonOperationsWithSpecEvidence() throws Exception {
    Path repositoryRoot = repository("operations");
    writeFile(repositoryRoot.resolve("services/alpha/src/main/resources/openapi.yaml"), """
        openapi: 3.0.3
        paths:
          /orders/{id}:
            get:
              operationId: getOrder
              tags:
                - Orders
                - Public
              responses:
                '200':
                  description: OK
        """);
    writeFile(repositoryRoot.resolve("docs/openapi.json"), """
        {
          "openapi": "3.1.0",
          "paths": {
            "/reports/{type}:export": {
              "post": {
                "operationId": "exportReport",
                "tags": ["Reports"]
              }
            }
          }
        }
        """);

    OpenApiOperationAnalysis analysis = operationAnalyzer.analyze(
        repositoryRoot,
        discovery(repositoryRoot, List.of(supportedModule("module:services/alpha", "services/alpha")))
            .specFiles());
    List<OpenApiOperationFact> operations = analysis.operations();

    assertAll(
        () -> assertEquals("analyzed", analysis.analysisStatus()),
        () -> assertEquals(2, operations.size()),
        () -> assertEquals("module:services/alpha", operations.get(0).moduleId()),
        () -> assertEquals("GET", operations.get(0).httpMethod()),
        () -> assertEquals("/orders/{id}", operations.get(0).path()),
        () -> assertEquals("getOrder", operations.get(0).operationId()),
        () -> assertEquals(List.of("Orders", "Public"), operations.get(0).tags()),
        () -> assertEquals("not_analyzed", operations.get(0).implementationStatus()),
        () -> assertEquals(
            "openapi_operation:module:services/alpha:spec:services/alpha/src/main/resources/openapi.yaml:operation:get:/orders/{id}",
            operations.get(0).id()),
        () -> assertEquals(null, operations.get(1).moduleId()),
        () -> assertEquals("POST", operations.get(1).httpMethod()),
        () -> assertEquals("/reports/{type}:export", operations.get(1).path()),
        () -> assertEquals(
            "openapi_operation:unscoped:spec:docs/openapi.json:operation:post:/reports/{type}%3Aexport",
            operations.get(1).id()),
        () -> assertTrue(operations.get(0).evidenceIds().get(0)
            .contains(":4-4:api_spec:operation%3Aget%3A/orders/{id}")),
        () -> assertTrue(analysis.evidence().stream()
            .anyMatch(evidence -> "operation get /orders/{id}; operationId getOrder; tags Orders,Public"
                .equals(evidence.excerpt()))),
        () -> assertEquals(List.of(), analysis.warnings()),
        () -> assertEvidenceIdsResolve(analysis));
  }

  @Test
  void sortsOperationFactsDeterministicallyRatherThanUsingSpecMapOrder() throws Exception {
    Path repositoryRoot = repository("operation-sorting");
    writeFile(repositoryRoot.resolve("src/main/resources/openapi.yml"), """
        openapi: 3.0.0
        paths:
          /zeta:
            post:
              operationId: postZeta
          /alpha:
            get:
              operationId: getAlpha
            patch:
              operationId: patchAlpha
        """);

    OpenApiOperationAnalysis analysis = operationAnalyzer.analyze(
        repositoryRoot,
        discovery(repositoryRoot, List.of(supportedModule("module:.", "."))).specFiles());

    assertEquals(
        List.of("GET /alpha", "PATCH /alpha", "POST /zeta"),
        analysis.operations().stream()
            .map(operation -> operation.httpMethod() + " " + operation.path())
            .toList());
  }

  @Test
  void boundsOperationIdAndTags() throws Exception {
    Path repositoryRoot = repository("bounded-values");
    writeFile(repositoryRoot.resolve("src/main/resources/openapi.yml"), """
        openapi: 3.0.0
        paths:
          /bounded:
            get:
              operationId: %s
              tags:
                - Tag1
                - Tag2
                - Tag3
                - Tag4
                - Tag5
                - Tag6
                - Tag7
                - Tag8
                - Tag9
                - %s
                - ""
        """.formatted("x".repeat(161), "y".repeat(121)));

    OpenApiOperationAnalysis analysis = operationAnalyzer.analyze(
        repositoryRoot,
        discovery(repositoryRoot, List.of(supportedModule("module:.", "."))).specFiles());

    OpenApiOperationFact operation = analysis.operations().get(0);

    assertAll(
        () -> assertEquals(null, operation.operationId()),
        () -> assertEquals(
            List.of("Tag1", "Tag2", "Tag3", "Tag4", "Tag5", "Tag6", "Tag7", "Tag8"),
            operation.tags()),
        () -> assertTrue(analysis.evidence().get(0).excerpt().length() <= 243),
        () -> assertFalse(analysis.evidence().get(0).excerpt().contains("Tag9")),
        () -> assertEvidenceIdsResolve(analysis));
  }

  @Test
  void invalidJsonSpecDegradesToWarningWithoutOperations() throws Exception {
    Path repositoryRoot = repository("invalid-json");
    writeFile(repositoryRoot.resolve("src/main/resources/openapi.json"), """
        {"openapi":"3.0.0","paths":{"/orders":{"get":{},"get":{}}}}
        """);

    OpenApiOperationAnalysis analysis = operationAnalyzer.analyze(
        repositoryRoot,
        discovery(repositoryRoot, List.of(supportedModule("module:.", "."))).specFiles());

    assertAll(
        () -> assertEquals("analyzed", analysis.analysisStatus()),
        () -> assertEquals(List.of(), analysis.operations()),
        () -> assertEquals(1, analysis.warnings().size()),
        () -> assertEquals("openapi_spec_parse_error", analysis.warnings().get(0).signal()),
        () -> assertEquals("src/main/resources/openapi.json", analysis.warnings().get(0).sourcePath()),
        () -> assertTrue(analysis.evidence().get(0).excerpt().contains("operation parser warning")),
        () -> assertEvidenceIdsResolve(analysis));
  }

  @Test
  void invalidYamlSpecDegradesToWarningWithoutOperations() throws Exception {
    Path repositoryRoot = repository("invalid-yaml");
    writeFile(repositoryRoot.resolve("src/main/resources/openapi.yml"), """
        openapi: [3.0.0
        paths:
          /orders:
            get: {}
        """);

    OpenApiOperationAnalysis analysis = operationAnalyzer.analyze(
        repositoryRoot,
        discovery(repositoryRoot, List.of(supportedModule("module:.", "."))).specFiles());

    assertAll(
        () -> assertEquals(List.of(), analysis.operations()),
        () -> assertEquals(1, analysis.warnings().size()),
        () -> assertEquals("openapi_spec_parse_error", analysis.warnings().get(0).signal()),
        () -> assertEvidenceIdsResolve(analysis));
  }

  @Test
  void unsupportedSpecShapeDegradesToWarningWithoutOperations() throws Exception {
    Path repositoryRoot = repository("unsupported-shape");
    writeFile(repositoryRoot.resolve("src/main/resources/openapi.yml"), """
        openapi: 3.0.0
        paths:
          - /orders
        """);

    OpenApiOperationAnalysis analysis = operationAnalyzer.analyze(
        repositoryRoot,
        discovery(repositoryRoot, List.of(supportedModule("module:.", "."))).specFiles());

    assertAll(
        () -> assertEquals(List.of(), analysis.operations()),
        () -> assertEquals(1, analysis.warnings().size()),
        () -> assertEquals("openapi_spec_unsupported", analysis.warnings().get(0).signal()),
        () -> assertEvidenceIdsResolve(analysis));
  }

  @Test
  void duplicateNormalizedOperationKeysDegradeToWarningWithoutCollidingFacts() throws Exception {
    Path repositoryRoot = repository("duplicate-normalized-operation");
    writeFile(repositoryRoot.resolve("src/main/resources/openapi.yml"), """
        openapi: 3.0.0
        paths:
          /orders:
            get:
              operationId: lowerCase
            GET:
              operationId: upperCase
          /payments:
            post:
              operationId: lowerPost
            POST:
              operationId: upperPost
        """);

    OpenApiOperationAnalysis analysis = operationAnalyzer.analyze(
        repositoryRoot,
        discovery(repositoryRoot, List.of(supportedModule("module:.", "."))).specFiles());

    assertAll(
        () -> assertEquals(List.of(), analysis.operations()),
        () -> assertEquals(1, analysis.warnings().size()),
        () -> assertEquals("openapi_spec_duplicate_operation", analysis.warnings().get(0).signal()),
        () -> assertEquals("hidden_http_surface", analysis.warnings().get(0).category()),
        () -> assertEvidenceIdsResolve(analysis));
  }

  @Test
  void oversizedSpecDegradesToWarningWithoutSerializingContents() throws Exception {
    Path repositoryRoot = repository("oversized");
    String payload = "A".repeat(1024 * 1024 + 1);
    writeFile(repositoryRoot.resolve("src/main/resources/openapi.yml"), payload);

    OpenApiOperationAnalysis analysis = operationAnalyzer.analyze(
        repositoryRoot,
        discovery(repositoryRoot, List.of(supportedModule("module:.", "."))).specFiles());

    assertAll(
        () -> assertEquals(List.of(), analysis.operations()),
        () -> assertEquals(1, analysis.warnings().size()),
        () -> assertEquals("openapi_spec_unsupported", analysis.warnings().get(0).signal()),
        () -> assertTrue(analysis.evidence().stream().noneMatch(evidence -> evidence.excerpt().contains("AAAA"))),
        () -> assertEvidenceIdsResolve(analysis));
  }

  @Test
  void symlinkPathEntryAfterDiscoveryDegradesToWarningWithoutReadingTarget() throws Exception {
    Path repositoryRoot = repository("symlink-after-discovery");
    Path specPath = repositoryRoot.resolve("src/main/resources/openapi.yml");
    writeFile(specPath, """
        openapi: 3.0.0
        paths:
          /before:
            get: {}
        """);
    List<OpenApiSpecFileFact> discoveredSpecs = discovery(
        repositoryRoot,
        List.of(supportedModule("module:.", "."))).specFiles();
    writeFile(repositoryRoot.resolve("shared/internal-spec.yml"), """
        openapi: 3.0.0
        paths:
          /after:
            get:
              operationId: FAKE_SYMLINK_TARGET_OPERATION
        """);
    Files.delete(specPath);
    createSymbolicLink(specPath, repositoryRoot.resolve("shared/internal-spec.yml"));

    OpenApiOperationAnalysis analysis = operationAnalyzer.analyze(repositoryRoot, discoveredSpecs);

    assertAll(
        () -> assertEquals(List.of(), analysis.operations()),
        () -> assertEquals(1, analysis.warnings().size()),
        () -> assertEquals("openapi_spec_unsupported", analysis.warnings().get(0).signal()),
        () -> assertFalse(analysis.toString().contains("FAKE_SYMLINK_TARGET_OPERATION")),
        () -> assertEvidenceIdsResolve(analysis));
  }

  @Test
  void hardlinkedPathEntryAfterDiscoveryDegradesToWarningWithoutReadingLinkedContent()
      throws Exception {
    Path repositoryRoot = repository("hardlink-after-discovery");
    Path specPath = repositoryRoot.resolve("src/main/resources/openapi.yml");
    writeFile(specPath, """
        openapi: 3.0.0
        paths:
          /before:
            get: {}
        """);
    List<OpenApiSpecFileFact> discoveredSpecs = discovery(
        repositoryRoot,
        List.of(supportedModule("module:.", "."))).specFiles();
    Path linkedSpec = repositoryRoot.resolve("shared/internal-spec.yml");
    writeFile(linkedSpec, """
        openapi: 3.0.0
        paths:
          /after:
            get:
              operationId: FAKE_HARDLINKED_TARGET_OPERATION
        """);
    Files.delete(specPath);
    createHardLink(specPath, linkedSpec);

    OpenApiOperationAnalysis analysis = operationAnalyzer.analyze(repositoryRoot, discoveredSpecs);

    assertAll(
        () -> assertEquals(List.of(), analysis.operations()),
        () -> assertEquals(1, analysis.warnings().size()),
        () -> assertEquals("openapi_spec_unsupported", analysis.warnings().get(0).signal()),
        () -> assertFalse(analysis.toString().contains("FAKE_HARDLINKED_TARGET_OPERATION")),
        () -> assertFalse(analysis.toString().contains(tempDir.toString())),
        () -> assertEvidenceIdsResolve(analysis));
  }

  @Test
  void returnsNotDetectedWhenNoSpecFilesAreAvailable() throws Exception {
    OpenApiOperationAnalysis analysis = operationAnalyzer.analyze(tempDir, List.of());

    assertAll(
        () -> assertEquals("not_detected", analysis.analysisStatus()),
        () -> assertEquals(List.of(), analysis.operations()),
        () -> assertEquals(List.of(), analysis.evidence()),
        () -> assertEquals(List.of(), analysis.warnings()));
  }

  private OpenApiSpecDiscoveryAnalysis discovery(
      Path repositoryRoot,
      List<MavenModuleItem> modules) throws Exception {
    return discoveryAnalyzer.analyze(repositoryRoot, modules);
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

  private void assertEvidenceIdsResolve(OpenApiOperationAnalysis analysis) {
    Set<String> evidenceIds = analysis.evidence().stream()
        .map(ApiSpecEvidence::id)
        .collect(Collectors.toSet());
    List<String> referencedEvidenceIds = java.util.stream.Stream.concat(
            analysis.operations().stream()
                .flatMap(operation -> operation.evidenceIds().stream()),
            analysis.warnings().stream()
                .flatMap(warning -> warning.evidenceIds().stream()))
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

  private void createHardLink(Path link, Path existing) throws Exception {
    try {
      Files.createLink(link, existing);
    } catch (UnsupportedOperationException | IOException | SecurityException exception) {
      assumeTrue(false, "hard links are unavailable: " + exception.getMessage());
    }
  }
}
