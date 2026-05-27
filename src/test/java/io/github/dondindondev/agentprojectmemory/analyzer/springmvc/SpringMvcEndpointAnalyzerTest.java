package io.github.dondindondev.agentprojectmemory.analyzer.springmvc;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

final class SpringMvcEndpointAnalyzerTest {
  private final SpringMvcEndpointAnalyzer analyzer = new SpringMvcEndpointAnalyzer();

  @Test
  void simpleRestControllerWithGetMappingIsDetected() throws Exception {
    SpringMvcEndpointAnalysis analysis = analyzeFixture();

    SpringMvcEndpointFact endpoint = endpoint(analysis, "com.example.web.SimpleRestController", "health");

    assertAll(
        () -> assertEquals("GET", endpoint.httpMethod()),
        () -> assertEquals(List.of("/health"), endpoint.paths()),
        () -> assertEquals("String", endpoint.declaredResponseType()),
        () -> assertFalse(endpoint.evidenceIds().isEmpty()));
  }

  @Test
  void controllerAnnotationIsDetected() throws Exception {
    SpringMvcEndpointAnalysis analysis = analyzeFixture();

    SpringMvcEndpointFact endpoint = endpoint(analysis, "com.example.web.UiController", "home");

    assertAll(
        () -> assertEquals("GET", endpoint.httpMethod()),
        () -> assertEquals(List.of("/home"), endpoint.paths()),
        () -> assertEquals("String", endpoint.declaredResponseType()));
  }

  @Test
  void classLevelRequestMappingCombinesWithMethodLevelGetMapping() throws Exception {
    SpringMvcEndpointAnalysis analysis = analyzeFixture();

    SpringMvcEndpointFact endpoint = endpoint(
        analysis,
        "com.example.web.ClassLevelMappingController",
        "orders");

    assertAll(
        () -> assertEquals(List.of("/api/v1/orders"), endpoint.paths()),
        () -> assertTrue(evidenceForEndpoint(analysis, endpoint).stream()
            .anyMatch(evidence -> "@RequestMapping".equals(evidence.annotationSymbol()))),
        () -> assertTrue(evidenceForEndpoint(analysis, endpoint).stream()
            .anyMatch(evidence -> "@GetMapping".equals(evidence.annotationSymbol()))));
  }

  @Test
  void getMappingWithConstantPathIsSkipped() throws Exception {
    SpringMvcEndpointAnalysis analysis = analyzeFixture();

    assertFalse(hasEndpoint(analysis, "com.example.web.PathVariantsController", "constantPath"));
    assertFalse(analysis.endpoints().stream()
        .filter(endpoint -> endpoint.controllerClass().equals("com.example.web.PathVariantsController"))
        .flatMap(endpoint -> endpoint.paths().stream())
        .anyMatch("/"::equals));
  }

  @Test
  void multipleLiteralMethodPathsArePreserved() throws Exception {
    SpringMvcEndpointAnalysis analysis = analyzeFixture();

    SpringMvcEndpointFact endpoint = endpoint(
        analysis,
        "com.example.web.PathVariantsController",
        "multipleMethodPaths");

    assertEquals(List.of("/alpha", "/beta"), endpoint.paths());
  }

  @Test
  void multipleClassLevelAndMethodLevelPathsProduceDeterministicCombinations() throws Exception {
    SpringMvcEndpointAnalysis analysis = analyzeFixture();

    SpringMvcEndpointFact endpoint = endpoint(
        analysis,
        "com.example.web.MultipleClassLevelPathsController",
        "orders");

    assertEquals(
        List.of("/api/orders", "/api/purchases", "/internal/orders", "/internal/purchases"),
        endpoint.paths());
  }

  @Test
  void rootClassLevelPathCombinesCleanlyWithMethodPath() throws Exception {
    SpringMvcEndpointAnalysis analysis = analyzeFixture();

    SpringMvcEndpointFact endpoint = endpoint(
        analysis,
        "com.example.web.RootClassLevelMappingController",
        "orders");

    assertEquals(List.of("/orders"), endpoint.paths());
  }

  @Test
  void nonLiteralClassLevelPathSkipsEndpointInsteadOfDroppingPrefix() throws Exception {
    SpringMvcEndpointAnalysis analysis = analyzeFixture();

    assertFalse(hasEndpoint(analysis, "com.example.web.NonLiteralClassLevelPathController", "orders"));
  }

  @Test
  void nonControllerClassWithGetMappingIsIgnored() throws Exception {
    SpringMvcEndpointAnalysis analysis = analyzeFixture();

    assertFalse(analysis.endpoints().stream()
        .anyMatch(endpoint -> endpoint.controllerClass().equals("com.example.web.NotAController")));
  }

  @Test
  void evidenceIncludesLineRangeOrPreciseAvailableSourceLocation() throws Exception {
    SpringMvcEndpointAnalysis analysis = analyzeFixture();

    SpringMvcEndpointEvidence evidence = analysis.evidence().stream()
        .filter(candidate -> candidate.sourcePath().endsWith("SimpleRestController.java"))
        .filter(candidate -> "@GetMapping".equals(candidate.annotationSymbol()))
        .findFirst()
        .orElseThrow();

    assertAll(
        () -> assertTrue(evidence.sourcePath().endsWith("src/main/java/com/example/web/SimpleRestController.java")),
        () -> assertNotNull(evidence.lineStart()),
        () -> assertNotNull(evidence.lineEnd()),
        () -> assertTrue(evidence.lineStart() > 0),
        () -> assertTrue(evidence.lineEnd() >= evidence.lineStart()));
  }

  @Test
  void evidenceIncludesUsefulExcerptAndGetMappingSymbol() throws Exception {
    SpringMvcEndpointAnalysis analysis = analyzeFixture();

    SpringMvcEndpointEvidence evidence = analysis.evidence().stream()
        .filter(candidate -> candidate.sourcePath().endsWith("SimpleRestController.java"))
        .filter(candidate -> "@GetMapping".equals(candidate.annotationSymbol()))
        .findFirst()
        .orElseThrow();

    assertAll(
        () -> assertEquals("@GetMapping", evidence.annotationSymbol()),
        () -> assertTrue(evidence.excerpt().contains("@GetMapping(\"/health\")")),
        () -> assertEquals("high", evidence.confidence()));
  }

  private SpringMvcEndpointAnalysis analyzeFixture() throws Exception {
    Path fixtureRoot = fixtureRoot();
    return analyzer.analyze(fixtureRoot, List.of(fixtureRoot.resolve("src/main/java")));
  }

  private Path fixtureRoot() throws Exception {
    return Path.of(Objects.requireNonNull(
        getClass().getResource("/fixtures/springmvc-endpoints")).toURI());
  }

  private SpringMvcEndpointFact endpoint(
      SpringMvcEndpointAnalysis analysis,
      String controllerClass,
      String handlerMethod) {
    return analysis.endpoints().stream()
        .filter(candidate -> candidate.controllerClass().equals(controllerClass))
        .filter(candidate -> candidate.handlerMethod().equals(handlerMethod))
        .findFirst()
        .orElseThrow();
  }

  private boolean hasEndpoint(
      SpringMvcEndpointAnalysis analysis,
      String controllerClass,
      String handlerMethod) {
    return analysis.endpoints().stream()
        .anyMatch(candidate -> candidate.controllerClass().equals(controllerClass)
            && candidate.handlerMethod().equals(handlerMethod));
  }

  private List<SpringMvcEndpointEvidence> evidenceForEndpoint(
      SpringMvcEndpointAnalysis analysis,
      SpringMvcEndpointFact endpoint) {
    return analysis.evidence().stream()
        .filter(evidence -> endpoint.evidenceIds().contains(evidence.id()))
        .toList();
  }
}
