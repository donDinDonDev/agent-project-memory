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
        () -> assertEquals(List.of("GET"), endpoint.httpMethods()),
        () -> assertEquals(SpringMvcHttpMethodSemantics.DECLARED, endpoint.httpMethodSemantics()),
        () -> assertEquals(List.of("/health"), endpoint.paths()),
        () -> assertEquals("String", endpoint.declaredResponseType()),
        () -> assertFalse(endpoint.evidenceIds().isEmpty()));
  }

  @Test
  void controllerAnnotationIsDetected() throws Exception {
    SpringMvcEndpointAnalysis analysis = analyzeFixture();

    SpringMvcEndpointFact endpoint = endpoint(analysis, "com.example.web.UiController", "home");

    assertAll(
        () -> assertEquals(List.of("GET"), endpoint.httpMethods()),
        () -> assertEquals(SpringMvcHttpMethodSemantics.DECLARED, endpoint.httpMethodSemantics()),
        () -> assertEquals(List.of("/home"), endpoint.paths()),
        () -> assertEquals("String", endpoint.declaredResponseType()));
  }

  @Test
  void requestMappingWithoutMethodUsesExplicitUndeclaredMethodSemantics() throws Exception {
    SpringMvcEndpointAnalysis analysis = analyzeFixture();

    SpringMvcEndpointFact endpoint = endpoint(
        analysis,
        "com.example.web.HttpMethodMappingController",
        "requestMappingWithoutMethod");

    assertAll(
        () -> assertEquals(List.of(), endpoint.httpMethods()),
        () -> assertEquals(SpringMvcHttpMethodSemantics.NOT_DECLARED, endpoint.httpMethodSemantics()),
        () -> assertEquals(List.of("/coverage/request"), endpoint.paths()),
        () -> assertTrue(evidenceForEndpoint(analysis, endpoint).stream()
            .anyMatch(evidence -> "@RequestMapping".equals(evidence.annotationSymbol())
                && "requestMappingWithoutMethod".equals(evidence.methodName()))));
  }

  @Test
  void requestMappingWithGetMethodIsDetected() throws Exception {
    SpringMvcEndpointAnalysis analysis = analyzeFixture();

    SpringMvcEndpointFact endpoint = endpoint(
        analysis,
        "com.example.web.HttpMethodMappingController",
        "requestMappingGet");

    assertAll(
        () -> assertEquals(List.of("GET"), endpoint.httpMethods()),
        () -> assertEquals(SpringMvcHttpMethodSemantics.DECLARED, endpoint.httpMethodSemantics()),
        () -> assertEquals(List.of("/coverage"), endpoint.paths()),
        () -> assertTrue(evidenceForEndpoint(analysis, endpoint).stream()
            .anyMatch(evidence -> "@RequestMapping".equals(evidence.annotationSymbol())
                && "requestMappingGet".equals(evidence.methodName()))));
  }

  @Test
  void requestMappingWithMethodArrayPreservesAllMethods() throws Exception {
    SpringMvcEndpointAnalysis analysis = analyzeFixture();

    SpringMvcEndpointFact endpoint = endpoint(
        analysis,
        "com.example.web.HttpMethodMappingController",
        "requestMappingMultipleMethods");

    assertAll(
        () -> assertEquals(List.of("GET", "POST"), endpoint.httpMethods()),
        () -> assertEquals(SpringMvcHttpMethodSemantics.DECLARED, endpoint.httpMethodSemantics()),
        () -> assertEquals(List.of("/coverage/request-both"), endpoint.paths()));
  }

  @Test
  void postPutPatchAndDeleteMappingsAreDetected() throws Exception {
    SpringMvcEndpointAnalysis analysis = analyzeFixture();

    SpringMvcEndpointFact post = endpoint(
        analysis,
        "com.example.web.HttpMethodMappingController",
        "postMappingValueAlias");
    SpringMvcEndpointFact put = endpoint(
        analysis,
        "com.example.web.HttpMethodMappingController",
        "putMappingPathAlias");
    SpringMvcEndpointFact patch = endpoint(
        analysis,
        "com.example.web.HttpMethodMappingController",
        "patchMapping");
    SpringMvcEndpointFact delete = endpoint(
        analysis,
        "com.example.web.HttpMethodMappingController",
        "deleteMappingArray");

    assertAll(
        () -> assertEquals(List.of("POST"), post.httpMethods()),
        () -> assertEquals(List.of("/coverage/post-value"), post.paths()),
        () -> assertEquals(List.of("PUT"), put.httpMethods()),
        () -> assertEquals(List.of("/coverage/put-path"), put.paths()),
        () -> assertEquals(List.of("PATCH"), patch.httpMethods()),
        () -> assertEquals(List.of("/coverage/patch"), patch.paths()),
        () -> assertEquals(List.of("DELETE"), delete.httpMethods()),
        () -> assertEquals(List.of("/coverage/delete", "/coverage/remove"), delete.paths()));
  }

  @Test
  void valueAndPathAliasesAreSupportedForMethodMappings() throws Exception {
    SpringMvcEndpointAnalysis analysis = analyzeFixture();

    SpringMvcEndpointFact get = endpoint(
        analysis,
        "com.example.web.HttpMethodMappingController",
        "getMappingPathAlias");
    SpringMvcEndpointFact post = endpoint(
        analysis,
        "com.example.web.HttpMethodMappingController",
        "postMappingValueAlias");

    assertAll(
        () -> assertEquals(List.of("/coverage/get-path"), get.paths()),
        () -> assertEquals(List.of("/coverage/post-value"), post.paths()));
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
  void requestMappingWithNonLiteralPathIsSkipped() throws Exception {
    SpringMvcEndpointAnalysis analysis = analyzeFixture();

    assertFalse(hasEndpoint(
        analysis,
        "com.example.web.HttpMethodMappingController",
        "nonLiteralRequestMappingPath"));
  }

  @Test
  void unsupportedCustomComposedAnnotationIsIgnored() throws Exception {
    SpringMvcEndpointAnalysis analysis = analyzeFixture();

    assertFalse(hasEndpoint(
        analysis,
        "com.example.web.HttpMethodMappingController",
        "customComposedMapping"));
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
