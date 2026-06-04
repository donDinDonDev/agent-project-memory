package io.github.dondindondev.agentprojectmemory.analyzer.springmvc;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
  void explicitlyImportedSpringAnnotationsStillEmitEndpointFacts() throws Exception {
    SpringMvcEndpointAnalysis analysis = analyzeFixture();

    SpringMvcEndpointFact endpoint = endpoint(analysis, "com.example.web.SimpleRestController", "health");

    assertAll(
        () -> assertEquals(List.of("/health"), endpoint.paths()),
        () -> assertTrue(evidenceForEndpoint(analysis, endpoint).stream()
            .anyMatch(evidence -> "@RestController".equals(evidence.annotationSymbol()))),
        () -> assertTrue(evidenceForEndpoint(analysis, endpoint).stream()
            .anyMatch(evidence -> "@GetMapping".equals(evidence.annotationSymbol()))));
  }

  @Test
  void fullyQualifiedSpringAnnotationsEmitEndpointFacts() throws Exception {
    SpringMvcEndpointAnalysis analysis = analyzeFixture();

    SpringMvcEndpointFact endpoint = endpoint(
        analysis,
        "com.example.web.FullyQualifiedSpringController",
        "fullyQualified");

    assertAll(
        () -> assertEquals(List.of("GET"), endpoint.httpMethods()),
        () -> assertEquals(List.of("/fully-qualified"), endpoint.paths()),
        () -> assertTrue(evidenceForEndpoint(analysis, endpoint).stream()
            .anyMatch(evidence -> "@RestController".equals(evidence.annotationSymbol()))),
        () -> assertTrue(evidenceForEndpoint(analysis, endpoint).stream()
            .anyMatch(evidence -> "@GetMapping".equals(evidence.annotationSymbol()))));
  }

  @Test
  void localFakeSpringAnnotationsDoNotEmitEndpointFacts() throws Exception {
    SpringMvcEndpointAnalysis analysis = analyzeFixture();

    assertAll(
        () -> assertFalse(hasEndpoint(
            analysis,
            "com.example.fake.FakeSpringAnnotationsController",
            "fake")),
        () -> assertFalse(analysis.evidence().stream()
            .anyMatch(evidence -> evidence.sourcePath().contains("FakeSpringAnnotationsController.java"))));
  }

  @Test
  void wildcardImportedSpringAnnotationsDoNotEmitEndpointFacts() throws Exception {
    SpringMvcEndpointAnalysis analysis = analyzeFixture();

    assertAll(
        () -> assertFalse(hasEndpoint(
            analysis,
            "com.example.wildcard.WildcardAnnotationController",
            "wildcardAnnotation")),
        () -> assertFalse(analysis.evidence().stream()
            .anyMatch(evidence -> evidence.sourcePath().contains("WildcardAnnotationController.java"))));
  }

  @Test
  void directHandlerMappingsRecordMappingSource() throws Exception {
    SpringMvcEndpointAnalysis analysis = analyzeFixture();

    SpringMvcEndpointFact endpoint = endpoint(analysis, "com.example.web.SimpleRestController", "health");
    SpringMvcEndpointMappingSource mappingSource = endpoint.mappingSource();

    assertAll(
        () -> assertEquals("direct_handler_method", mappingSource.kind()),
        () -> assertEquals("com.example.web.SimpleRestController", mappingSource.declaringType()),
        () -> assertEquals("health", mappingSource.declaringMethod()),
        () -> assertEquals("direct", mappingSource.binding()),
        () -> assertNull(mappingSource.uncertainty()),
        () -> assertTrue(mappingSource.evidenceIds().stream()
            .anyMatch(evidenceId -> evidenceId.contains("@GetMapping"))));
  }

  @Test
  void sourceVisibleInterfaceMethodMappingIsDetectedThroughControllerImplementation()
      throws Exception {
    SpringMvcEndpointAnalysis analysis = analyzeFixture();

    SpringMvcEndpointFact endpoint = endpoint(
        analysis,
        "com.example.web.InterfaceBackedController",
        "getInterfaceOrder");
    SpringMvcEndpointMappingSource mappingSource = endpoint.mappingSource();

    assertAll(
        () -> assertEquals(List.of("GET"), endpoint.httpMethods()),
        () -> assertEquals(List.of("/interface/orders/{id}"), endpoint.paths()),
        () -> assertEquals("InterfaceOrderResponse", endpoint.declaredResponseType()),
        () -> assertEquals("source_visible_interface_method", mappingSource.kind()),
        () -> assertEquals("com.example.web.InterfaceOrdersApi", mappingSource.declaringType()),
        () -> assertEquals("getInterfaceOrder", mappingSource.declaringMethod()),
        () -> assertEquals("unique_implemented_interface_method", mappingSource.binding()),
        () -> assertNull(mappingSource.uncertainty()),
        () -> assertTrue(evidenceForEndpoint(analysis, endpoint).stream()
            .anyMatch(evidence -> "annotation".equals(evidence.sourceType())
                && "com.example.web.InterfaceOrdersApi".equals(evidence.className())
                && "getInterfaceOrder".equals(evidence.methodName())
                && "@GetMapping".equals(evidence.annotationSymbol()))),
        () -> assertTrue(evidenceForEndpoint(analysis, endpoint).stream()
            .anyMatch(evidence -> "code_symbol".equals(evidence.sourceType())
                && "com.example.web.InterfaceBackedController".equals(evidence.className()))));
  }

  @Test
  void explicitlyImportedInterfaceMethodMappingIsDetected() throws Exception {
    SpringMvcEndpointAnalysis analysis = analyzeFixture();

    SpringMvcEndpointFact endpoint = endpoint(
        analysis,
        "com.example.web.ImportedApiController",
        "importedOrder");

    assertAll(
        () -> assertEquals(List.of("GET"), endpoint.httpMethods()),
        () -> assertEquals(List.of("/imported/orders"), endpoint.paths()),
        () -> assertEquals("com.example.api.ImportedOrdersApi", endpoint.mappingSource().declaringType()),
        () -> assertEquals("source_visible_interface_method", endpoint.mappingSource().kind()));
  }

  @Test
  void crossPackageSameSimpleNameInterfaceWithoutValidImportIsSkipped() throws Exception {
    SpringMvcEndpointAnalysis analysis = analyzeFixture();

    assertFalse(hasEndpoint(
        analysis,
        "com.example.web.CrossPackageNoImportController",
        "crossPackageOrder"));
  }

  @Test
  void wildcardImportedInterfaceMethodMappingIsSkipped() throws Exception {
    SpringMvcEndpointAnalysis analysis = analyzeFixture();

    assertFalse(hasEndpoint(
        analysis,
        "com.example.web.WildcardImportedController",
        "wildcardOrder"));
  }

  @Test
  void interfaceClassLevelAndMethodLevelMappingsAreCombinedWithControllerPrefix()
      throws Exception {
    SpringMvcEndpointAnalysis analysis = analyzeFixture();

    SpringMvcEndpointFact endpoint = endpoint(
        analysis,
        "com.example.web.InterfaceClassLevelController",
        "createFromInterface");

    assertAll(
        () -> assertEquals(List.of("POST"), endpoint.httpMethods()),
        () -> assertEquals(List.of("/api/interface-class/orders"), endpoint.paths()),
        () -> assertEquals("InterfaceOrderRequest", endpoint.requestBodyType()),
        () -> assertTrue(evidenceForEndpoint(analysis, endpoint).stream()
            .anyMatch(evidence -> "com.example.web.InterfaceClassLevelApi".equals(evidence.className())
                && "@RequestMapping".equals(evidence.annotationSymbol()))),
        () -> assertTrue(endpoint.requestBodyEvidenceIds().stream()
            .allMatch(evidenceId -> analysis.evidence().stream()
                .anyMatch(evidence -> evidence.id().equals(evidenceId)
                    && "com.example.web.InterfaceClassLevelApi".equals(evidence.className())
                    && "@RequestBody".equals(evidence.annotationSymbol())))));
  }

  @Test
  void interfaceParameterAnnotationsAreUsedForRequestMetadata() throws Exception {
    SpringMvcEndpointAnalysis analysis = analyzeFixture();

    SpringMvcEndpointFact endpoint = endpoint(
        analysis,
        "com.example.web.InterfaceBackedController",
        "getInterfaceOrder");
    SpringMvcRequestParameterFact id = requestParameter(endpoint, "path_variable", "id");
    SpringMvcRequestParameterFact expand = requestParameter(endpoint, "request_param", "expand");

    assertAll(
        () -> assertEquals("Long", id.javaType()),
        () -> assertEquals("String", expand.javaType()),
        () -> assertTrue(endpoint.requestParameters().stream()
            .flatMap(parameter -> parameter.evidenceIds().stream())
            .allMatch(evidenceId -> analysis.evidence().stream()
                .anyMatch(evidence -> evidence.id().equals(evidenceId)
                    && "com.example.web.InterfaceOrdersApi".equals(evidence.className())
                    && "getInterfaceOrder".equals(evidence.methodName())))));
  }

  @Test
  void directConcreteMappingAvoidsDuplicateInterfaceDerivedEndpoint() throws Exception {
    SpringMvcEndpointAnalysis analysis = analyzeFixture();

    List<SpringMvcEndpointFact> endpoints = endpoints(
        analysis,
        "com.example.web.DirectDuplicateController",
        "duplicateDirect");
    SpringMvcEndpointFact endpoint = endpoints.get(0);

    assertAll(
        () -> assertEquals(1, endpoints.size()),
        () -> assertEquals(List.of("/controller/direct"), endpoint.paths()),
        () -> assertEquals("direct_handler_method", endpoint.mappingSource().kind()),
        () -> assertFalse(analysis.endpoints().stream()
            .filter(candidate -> candidate.controllerClass().equals(
                "com.example.web.DirectDuplicateController"))
            .flatMap(candidate -> candidate.paths().stream())
            .anyMatch("/interface/direct"::equals)));
  }

  @Test
  void ambiguousInterfaceBindingsAreSkipped() throws Exception {
    SpringMvcEndpointAnalysis analysis = analyzeFixture();

    assertFalse(hasEndpoint(
        analysis,
        "com.example.web.AmbiguousInterfaceController",
        "ambiguous"));
  }

  @Test
  void classpathOnlyOrGeneratedInterfacesRemainOutOfScope() throws Exception {
    SpringMvcEndpointAnalysis analysis = analyzeFixture();

    assertFalse(hasEndpoint(
        analysis,
        "com.example.web.GeneratedApiController",
        "generatedOperation"));
  }

  @Test
  void modernInstanceofPatternInControllerSourceIsParsed() throws Exception {
    SpringMvcEndpointAnalysis analysis = analyzeModernJavaFixture();

    SpringMvcEndpointFact endpoint = endpoint(
        analysis,
        "com.example.modern.ModernController",
        "modern");

    assertAll(
        () -> assertEquals(List.of("GET"), endpoint.httpMethods()),
        () -> assertEquals(List.of("/modern"), endpoint.paths()));
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
  void controllerAnnotationEvidenceIsAttachedToEndpointFacts() throws Exception {
    SpringMvcEndpointAnalysis analysis = analyzeFixture();

    SpringMvcEndpointFact restEndpoint = endpoint(
        analysis,
        "com.example.web.SimpleRestController",
        "health");
    SpringMvcEndpointEvidence restControllerEvidence = controllerAnnotationEvidenceForEndpoint(
        analysis,
        restEndpoint,
        "@RestController");
    SpringMvcEndpointFact controllerEndpoint = endpoint(analysis, "com.example.web.UiController", "home");
    SpringMvcEndpointEvidence controllerEvidence = controllerAnnotationEvidenceForEndpoint(
        analysis,
        controllerEndpoint,
        "@Controller");

    assertAll(
        () -> assertTrue(restEndpoint.evidenceIds().contains(restControllerEvidence.id())),
        () -> assertEquals("com.example.web.SimpleRestController", restControllerEvidence.className()),
        () -> assertNull(restControllerEvidence.methodName()),
        () -> assertTrue(restControllerEvidence.excerpt().contains("@RestController")),
        () -> assertTrue(controllerEndpoint.evidenceIds().contains(controllerEvidence.id())),
        () -> assertEquals("com.example.web.UiController", controllerEvidence.className()),
        () -> assertNull(controllerEvidence.methodName()),
        () -> assertTrue(controllerEvidence.excerpt().contains("@Controller")));
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
  void pathVariableAndRequestParamMetadataAreDetected() throws Exception {
    SpringMvcEndpointAnalysis analysis = analyzeFixture();

    SpringMvcEndpointFact endpoint = endpoint(
        analysis,
        "com.example.web.RequestMetadataController",
        "getOrder");
    SpringMvcRequestParameterFact pathVariable = requestParameter(
        endpoint,
        "path_variable",
        "id");
    SpringMvcRequestParameterFact status = requestParameter(
        endpoint,
        "request_param",
        "status");
    SpringMvcRequestParameterFact page = requestParameter(
        endpoint,
        "request_param",
        "page");

    assertAll(
        () -> assertEquals(List.of("/orders/{id}"), endpoint.paths()),
        () -> assertEquals("Long", pathVariable.javaType()),
        () -> assertEquals("String", status.javaType()),
        () -> assertEquals("int", page.javaType()),
        () -> assertFalse(pathVariable.evidenceIds().isEmpty()),
        () -> assertFalse(status.evidenceIds().isEmpty()),
        () -> assertFalse(page.evidenceIds().isEmpty()));
  }

  @Test
  void requestBodyTypeAndEvidenceAreDetected() throws Exception {
    SpringMvcEndpointAnalysis analysis = analyzeFixture();

    SpringMvcEndpointFact endpoint = endpoint(
        analysis,
        "com.example.web.RequestMetadataController",
        "createOrder");

    assertAll(
        () -> assertEquals("CreateOrderRequest", endpoint.requestBodyType()),
        () -> assertFalse(endpoint.requestBodyEvidenceIds().isEmpty()),
        () -> assertTrue(endpoint.evidenceIds().containsAll(endpoint.requestBodyEvidenceIds())),
        () -> assertTrue(endpoint.requestBodyEvidenceIds().stream()
            .allMatch(evidenceId -> analysis.evidence().stream()
                .anyMatch(evidence -> evidence.id().equals(evidenceId)
                    && "@RequestBody".equals(evidence.annotationSymbol())
                    && "createOrder".equals(evidence.methodName())))));
  }

  @Test
  void requestMetadataEvidenceIdsArePreservedOnEndpointFacts() throws Exception {
    SpringMvcEndpointAnalysis analysis = analyzeFixture();

    SpringMvcEndpointFact endpoint = endpoint(
        analysis,
        "com.example.web.RequestMetadataController",
        "getOrder");
    List<String> metadataEvidenceIds = endpoint.requestParameters().stream()
        .flatMap(parameter -> parameter.evidenceIds().stream())
        .toList();

    assertAll(
        () -> assertEquals(3, metadataEvidenceIds.size()),
        () -> assertEquals(3, metadataEvidenceIds.stream().distinct().count()),
        () -> assertTrue(endpoint.evidenceIds().containsAll(metadataEvidenceIds)),
        () -> assertTrue(metadataEvidenceIds.stream()
            .allMatch(evidenceId -> analysis.evidence().stream()
                .anyMatch(evidence -> evidence.id().equals(evidenceId)
                    && "getOrder".equals(evidence.methodName())
                    && ("@PathVariable".equals(evidence.annotationSymbol())
                        || "@RequestParam".equals(evidence.annotationSymbol()))))));
  }

  @Test
  void nonLiteralRequestParamNameIsSkippedWithoutDroppingEndpoint() throws Exception {
    SpringMvcEndpointAnalysis analysis = analyzeFixture();

    SpringMvcEndpointFact endpoint = endpoint(
        analysis,
        "com.example.web.RequestMetadataController",
        "unsupportedRequestParamName");

    assertAll(
        () -> assertEquals(List.of("/orders/search"), endpoint.paths()),
        () -> assertTrue(endpoint.requestParameters().isEmpty()));
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

  private SpringMvcEndpointAnalysis analyzeModernJavaFixture() throws Exception {
    Path fixtureRoot = modernJavaFixtureRoot();
    return analyzer.analyze(fixtureRoot, List.of(fixtureRoot.resolve("src/main/java")));
  }

  private Path fixtureRoot() throws Exception {
    return Path.of(Objects.requireNonNull(
        getClass().getResource("/fixtures/springmvc-endpoints")).toURI());
  }

  private Path modernJavaFixtureRoot() throws Exception {
    return Path.of(Objects.requireNonNull(
        getClass().getResource("/fixtures/modern-java-syntax")).toURI());
  }

  private SpringMvcEndpointFact endpoint(
      SpringMvcEndpointAnalysis analysis,
      String controllerClass,
      String handlerMethod) {
    return endpoints(analysis, controllerClass, handlerMethod).stream()
        .findFirst()
        .orElseThrow();
  }

  private List<SpringMvcEndpointFact> endpoints(
      SpringMvcEndpointAnalysis analysis,
      String controllerClass,
      String handlerMethod) {
    return analysis.endpoints().stream()
        .filter(candidate -> candidate.controllerClass().equals(controllerClass))
        .filter(candidate -> candidate.handlerMethod().equals(handlerMethod))
        .toList();
  }

  private boolean hasEndpoint(
      SpringMvcEndpointAnalysis analysis,
      String controllerClass,
      String handlerMethod) {
    return analysis.endpoints().stream()
        .anyMatch(candidate -> candidate.controllerClass().equals(controllerClass)
            && candidate.handlerMethod().equals(handlerMethod));
  }

  private SpringMvcRequestParameterFact requestParameter(
      SpringMvcEndpointFact endpoint,
      String source,
      String name) {
    return endpoint.requestParameters().stream()
        .filter(candidate -> candidate.source().equals(source))
        .filter(candidate -> candidate.name().equals(name))
        .findFirst()
        .orElseThrow();
  }

  private List<SpringMvcEndpointEvidence> evidenceForEndpoint(
      SpringMvcEndpointAnalysis analysis,
      SpringMvcEndpointFact endpoint) {
    return analysis.evidence().stream()
        .filter(evidence -> endpoint.evidenceIds().contains(evidence.id()))
        .toList();
  }

  private SpringMvcEndpointEvidence controllerAnnotationEvidenceForEndpoint(
      SpringMvcEndpointAnalysis analysis,
      SpringMvcEndpointFact endpoint,
      String annotationSymbol) {
    return evidenceForEndpoint(analysis, endpoint).stream()
        .filter(evidence -> annotationSymbol.equals(evidence.annotationSymbol()))
        .filter(evidence -> evidence.methodName() == null)
        .findFirst()
        .orElseThrow();
  }
}
