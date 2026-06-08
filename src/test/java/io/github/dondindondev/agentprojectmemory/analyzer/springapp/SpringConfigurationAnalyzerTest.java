package io.github.dondindondev.agentprojectmemory.analyzer.springapp;

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

final class SpringConfigurationAnalyzerTest {
  private final SpringConfigurationAnalyzer analyzer = new SpringConfigurationAnalyzer();

  @Test
  void detectsDirectConfigurationClassesConfigurationPropertiesAndBeanMethods()
      throws Exception {
    SpringConfigurationAnalysis analysis = analyzeFixture("spring-configuration-signals");

    SpringConfigurationClassFact configurationClass = configurationClass(
        analysis,
        "com.example.config.InventoryConfiguration");
    SpringConfigurationPropertiesFact properties = configurationProperties(
        analysis,
        "com.example.config.InventoryProperties");
    SpringConfigurationPropertiesFact fqcnProperties = configurationProperties(
        analysis,
        "com.example.config.FullyQualifiedOrderProperties");
    SpringConfigurationPropertiesFact recordProperties = configurationProperties(
        analysis,
        "com.example.config.RecordBackedProperties");
    SpringBeanMethodFact beanMethod = beanMethod(
        analysis,
        "com.example.config.InventoryConfiguration",
        "inventoryClient");
    SpringBeanMethodFact fqcnBeanMethod = beanMethod(
        analysis,
        "com.example.config.InventoryConfiguration",
        "inventoryHelper");
    SpringBeanMethodFact beanOnlyMethod = beanMethod(
        analysis,
        "com.example.config.BeanOnlyFactory",
        "standaloneBean");

    assertAll(
        () -> assertEquals("spring_configuration_class", configurationClass.surfaceCategory()),
        () -> assertEquals("extracted", configurationClass.supportType()),
        () -> assertEquals("direct_configuration_class", configurationClass.configurationSignal()),
        () -> assertEquals("spring_configuration_properties_type", properties.surfaceCategory()),
        () -> assertEquals("direct_configuration_properties_type",
            properties.configurationPropertiesSignal()),
        () -> assertEquals("not_analyzed", properties.bindingStatus()),
        () -> assertEquals("not_analyzed", fqcnProperties.bindingStatus()),
        () -> assertEquals("not_analyzed", recordProperties.bindingStatus()),
        () -> assertEquals("spring_bean_method", beanMethod.surfaceCategory()),
        () -> assertEquals("direct_bean_method", beanMethod.beanSignal()),
        () -> assertEquals("not_analyzed", beanMethod.beanNameStatus()),
        () -> assertEquals("direct_bean_method", fqcnBeanMethod.beanSignal()),
        () -> assertEquals("direct_bean_method", beanOnlyMethod.beanSignal()));
  }

  @Test
  void ignoresWildcardOnlyAndUnresolvedSimpleAnnotations() throws Exception {
    SpringConfigurationAnalysis analysis = analyzeFixture("spring-configuration-signals");

    assertAll(
        () -> assertFalse(hasConfigurationClass(
            analysis,
            "com.example.config.WildcardOnlyConfiguration")),
        () -> assertFalse(hasConfigurationProperties(
            analysis,
            "com.example.config.WildcardOnlyProperties")),
        () -> assertFalse(hasConfigurationProperties(
            analysis,
            "com.example.config.UnresolvedConfigurationProperties")),
        () -> assertFalse(hasBeanMethod(
            analysis,
            "com.example.config.WildcardOnlyConfiguration",
            "wildcardOnlyBean")),
        () -> assertFalse(hasBeanMethod(
            analysis,
            "com.example.config.UnresolvedBeanMethod",
            "unresolvedBean")));
  }

  @Test
  void sourceDeclaredSpringOriginsDoNotEmitConfigurationFacts() throws Exception {
    SpringConfigurationAnalysis analysis = analyzeFixture("spring-configuration-spoofed-origins");

    assertAll(
        () -> assertTrue(analysis.configurationClasses().isEmpty()),
        () -> assertTrue(analysis.configurationProperties().isEmpty()),
        () -> assertTrue(analysis.beanMethods().isEmpty()),
        () -> assertTrue(analysis.evidence().isEmpty()));
  }

  @Test
  void evidenceIdsResolveToAnnotationEvidence() throws Exception {
    SpringConfigurationAnalysis analysis = analyzeFixture("spring-configuration-signals");

    for (SpringConfigurationClassFact configurationClass : analysis.configurationClasses()) {
      assertSingleClassAnnotationEvidence(analysis, configurationClass.evidenceIds(), configurationClass.className());
    }
    for (SpringConfigurationPropertiesFact properties : analysis.configurationProperties()) {
      assertSingleClassAnnotationEvidence(analysis, properties.evidenceIds(), properties.className());
    }
    for (SpringBeanMethodFact beanMethod : analysis.beanMethods()) {
      SpringConfigurationEvidence evidence = evidence(analysis, beanMethod.evidenceIds().get(0));
      assertAll(
          () -> assertEquals("annotation", evidence.sourceType()),
          () -> assertEquals(beanMethod.className(), evidence.className()),
          () -> assertEquals(beanMethod.methodName(), evidence.methodName()),
          () -> assertEquals("@Bean", evidence.symbolName()),
          () -> assertTrue(evidence.sourcePath().startsWith("src/main/java/")),
          () -> assertNotNull(evidence.lineStart()),
          () -> assertNotNull(evidence.lineEnd()),
          () -> assertEquals("high", evidence.confidence()));
    }
  }

  @Test
  void configurationFactsAreSortedDeterministically() throws Exception {
    SpringConfigurationAnalysis analysis = analyzeFixture("spring-configuration-signals");

    assertAll(
        () -> assertEquals(
            List.of("com.example.config.InventoryConfiguration"),
            analysis.configurationClasses().stream()
                .map(SpringConfigurationClassFact::className)
                .toList()),
        () -> assertEquals(
            List.of(
                "com.example.config.FullyQualifiedOrderProperties",
                "com.example.config.InventoryProperties",
                "com.example.config.RecordBackedProperties"),
            analysis.configurationProperties().stream()
                .map(SpringConfigurationPropertiesFact::className)
                .toList()),
        () -> assertEquals(
            List.of(
                "com.example.config.BeanOnlyFactory#standaloneBean",
                "com.example.config.InventoryConfiguration#inventoryClient",
                "com.example.config.InventoryConfiguration#inventoryHelper"),
            analysis.beanMethods().stream()
                .map(bean -> bean.className() + "#" + bean.methodName())
                .toList()));
  }

  private SpringConfigurationAnalysis analyzeFixture(String fixtureName) throws Exception {
    Path fixtureRoot = Path.of(Objects.requireNonNull(
        getClass().getResource("/fixtures/" + fixtureName)).toURI());
    return analyzer.analyze(fixtureRoot, List.of(fixtureRoot.resolve("src/main/java")));
  }

  private SpringConfigurationClassFact configurationClass(
      SpringConfigurationAnalysis analysis,
      String className) {
    return analysis.configurationClasses().stream()
        .filter(candidate -> candidate.className().equals(className))
        .findFirst()
        .orElseThrow();
  }

  private boolean hasConfigurationClass(SpringConfigurationAnalysis analysis, String className) {
    return analysis.configurationClasses().stream()
        .anyMatch(candidate -> candidate.className().equals(className));
  }

  private SpringConfigurationPropertiesFact configurationProperties(
      SpringConfigurationAnalysis analysis,
      String className) {
    return analysis.configurationProperties().stream()
        .filter(candidate -> candidate.className().equals(className))
        .findFirst()
        .orElseThrow();
  }

  private boolean hasConfigurationProperties(SpringConfigurationAnalysis analysis, String className) {
    return analysis.configurationProperties().stream()
        .anyMatch(candidate -> candidate.className().equals(className));
  }

  private SpringBeanMethodFact beanMethod(
      SpringConfigurationAnalysis analysis,
      String className,
      String methodName) {
    return analysis.beanMethods().stream()
        .filter(candidate -> candidate.className().equals(className))
        .filter(candidate -> candidate.methodName().equals(methodName))
        .findFirst()
        .orElseThrow();
  }

  private boolean hasBeanMethod(
      SpringConfigurationAnalysis analysis,
      String className,
      String methodName) {
    return analysis.beanMethods().stream()
        .anyMatch(candidate -> candidate.className().equals(className)
            && candidate.methodName().equals(methodName));
  }

  private void assertSingleClassAnnotationEvidence(
      SpringConfigurationAnalysis analysis,
      List<String> evidenceIds,
      String className) {
    assertEquals(1, evidenceIds.size());
    SpringConfigurationEvidence evidence = evidence(analysis, evidenceIds.get(0));
    assertAll(
        () -> assertEquals("annotation", evidence.sourceType()),
        () -> assertEquals(className, evidence.className()),
        () -> assertNull(evidence.methodName()),
        () -> assertTrue(evidence.symbolName().equals("@Configuration")
            || evidence.symbolName().equals("@ConfigurationProperties")),
        () -> assertTrue(evidence.sourcePath().startsWith("src/main/java/")),
        () -> assertNotNull(evidence.lineStart()),
        () -> assertNotNull(evidence.lineEnd()),
        () -> assertEquals("high", evidence.confidence()));
  }

  private SpringConfigurationEvidence evidence(
      SpringConfigurationAnalysis analysis,
      String evidenceId) {
    return analysis.evidence().stream()
        .filter(candidate -> candidate.id().equals(evidenceId))
        .findFirst()
        .orElseThrow();
  }
}
