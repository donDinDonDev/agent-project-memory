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

final class SpringComponentAnalyzerTest {
  private final SpringComponentAnalyzer analyzer = new SpringComponentAnalyzer();

  @Test
  void supportedDirectStereotypesAreDetected() throws Exception {
    SpringComponentAnalysis analysis = analyzeFixture();

    assertAll(
        () -> assertEquals(List.of("@Component"), component(analysis, "PlainComponent").stereotypes()),
        () -> assertEquals(List.of("@Service"), component(analysis, "OrderService").stereotypes()),
        () -> assertEquals(List.of("@Repository"), component(analysis, "OrderRepository").stereotypes()),
        () -> assertEquals(List.of("@Controller"), component(analysis, "PageController").stereotypes()),
        () -> assertEquals(List.of("@RestController"), component(analysis, "ApiController").stereotypes()),
        () -> assertEquals(List.of("@Configuration"), component(analysis, "AppConfiguration").stereotypes()));
  }

  @Test
  void unsupportedAnnotatedClassIsIgnored() throws Exception {
    SpringComponentAnalysis analysis = analyzeFixture();

    assertFalse(analysis.components().stream()
        .anyMatch(component -> component.className().equals("com.example.components.PlainJavaClass")));
  }

  @Test
  void componentIdsAreStableAndUsePackageBasedClassNames() throws Exception {
    SpringComponentAnalysis analysis = analyzeFixture();

    SpringComponentFact service = component(analysis, "OrderService");

    assertAll(
        () -> assertEquals("com.example.components.OrderService", service.className()),
        () -> assertEquals("component:com.example.components.OrderService", service.id()));
  }

  @Test
  void componentsAreSortedByClassNameAndId() throws Exception {
    SpringComponentAnalysis analysis = analyzeFixture();

    assertEquals(
        List.of(
            "com.example.components.ApiController",
            "com.example.components.AppConfiguration",
            "com.example.components.OrderRepository",
            "com.example.components.OrderService",
            "com.example.components.PageController",
            "com.example.components.PlainComponent"),
        analysis.components().stream().map(SpringComponentFact::className).toList());
  }

  @Test
  void evidenceIdsExistAndPointToAnnotationSourceLines() throws Exception {
    SpringComponentAnalysis analysis = analyzeFixture();

    for (SpringComponentFact component : analysis.components()) {
      assertEquals(1, component.evidenceIds().size());
      SpringComponentEvidence evidence = evidence(analysis, component.evidenceIds().get(0));

      assertAll(
          () -> assertEquals(component.className(), evidence.className()),
          () -> assertNull(evidence.methodName()),
          () -> assertTrue(component.stereotypes().contains(evidence.annotationSymbol())),
          () -> assertTrue(evidence.sourcePath()
              .endsWith("src/main/java/com/example/components/StereotypeComponents.java")),
          () -> assertNotNull(evidence.lineStart()),
          () -> assertNotNull(evidence.lineEnd()),
          () -> assertTrue(evidence.lineStart() > 0),
          () -> assertTrue(evidence.lineEnd() >= evidence.lineStart()),
          () -> assertTrue(evidence.excerpt().contains(evidence.annotationSymbol())),
          () -> assertEquals("high", evidence.confidence()));
    }
  }

  private SpringComponentAnalysis analyzeFixture() throws Exception {
    Path fixtureRoot = fixtureRoot();
    return analyzer.analyze(fixtureRoot, List.of(fixtureRoot.resolve("src/main/java")));
  }

  private Path fixtureRoot() throws Exception {
    return Path.of(Objects.requireNonNull(
        getClass().getResource("/fixtures/spring-components")).toURI());
  }

  private SpringComponentFact component(SpringComponentAnalysis analysis, String simpleName) {
    return analysis.components().stream()
        .filter(candidate -> candidate.className().equals("com.example.components." + simpleName))
        .findFirst()
        .orElseThrow();
  }

  private SpringComponentEvidence evidence(SpringComponentAnalysis analysis, String evidenceId) {
    return analysis.evidence().stream()
        .filter(candidate -> candidate.id().equals(evidenceId))
        .findFirst()
        .orElseThrow();
  }
}
