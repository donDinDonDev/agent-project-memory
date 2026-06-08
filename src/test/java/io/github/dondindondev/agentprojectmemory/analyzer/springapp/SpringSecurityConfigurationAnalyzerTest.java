package io.github.dondindondev.agentprojectmemory.analyzer.springapp;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.dondindondev.agentprojectmemory.analyzer.warnings.AnalysisWarningAnalysis;
import io.github.dondindondev.agentprojectmemory.analyzer.warnings.AnalysisWarningEvidence;
import io.github.dondindondev.agentprojectmemory.analyzer.warnings.AnalysisWarningFact;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

final class SpringSecurityConfigurationAnalyzerTest {
  private final SpringSecurityConfigurationAnalyzer analyzer =
      new SpringSecurityConfigurationAnalyzer();

  @Test
  void detectsSecurityConfigurationAnnotationsAndSecurityFilterChainBeans()
      throws Exception {
    AnalysisWarningAnalysis analysis = analyzeFixture("spring-security-signals");

    assertAll(
        () -> assertTrue(hasWarning(
            analysis,
            "security_configuration_annotation",
            "WebSecuritySignals")),
        () -> assertTrue(hasWarning(
            analysis,
            "security_configuration_annotation",
            "MethodSecuritySignals")),
        () -> assertTrue(hasWarning(
            analysis,
            "security_configuration_annotation",
            "WebFluxSecuritySignals")),
        () -> assertTrue(hasWarning(
            analysis,
            "security_configuration_annotation",
            "ReactiveMethodSecuritySignals")),
        () -> assertTrue(hasWarning(
            analysis,
            "security_configuration_annotation",
            "LegacyMethodSecuritySignals")),
        () -> assertTrue(hasWarning(
            analysis,
            "security_filter_chain_bean",
            "apiSecurity")),
        () -> assertTrue(hasWarning(
            analysis,
            "security_filter_chain_bean",
            "actuatorSecurity")));
  }

  @Test
  void ignoresWildcardOnlyUnresolvedAndNonSecurityBeanMethods() throws Exception {
    AnalysisWarningAnalysis analysis = analyzeFixture("spring-security-signals");

    assertAll(
        () -> assertFalse(hasWarning(analysis, "security_configuration_annotation", "WildcardOnly")),
        () -> assertFalse(hasWarning(analysis, "security_configuration_annotation", "Unresolved")),
        () -> assertFalse(hasWarning(analysis, "security_filter_chain_bean", "wildcardOnlySecurity")),
        () -> assertFalse(hasWarning(analysis, "security_filter_chain_bean", "unresolvedSecurity")),
        () -> assertFalse(hasWarning(analysis, "security_filter_chain_bean", "notSecurityFilterChain")));
  }

  @Test
  void sourceDeclaredSpringOriginsDoNotEmitSecurityWarnings() throws Exception {
    AnalysisWarningAnalysis analysis = analyzeFixture("spring-security-spoofed-origins");

    assertAll(
        () -> assertTrue(analysis.warnings().isEmpty()),
        () -> assertTrue(analysis.evidence().isEmpty()));
  }

  @Test
  void warningEvidenceIsAnnotationOrCodeSymbolAndDoesNotSerializePolicyDsl()
      throws Exception {
    AnalysisWarningAnalysis analysis = analyzeFixture("spring-security-signals");

    for (AnalysisWarningFact warning : analysis.warnings()) {
      assertFalse(warning.evidenceIds().isEmpty());
      for (String evidenceId : warning.evidenceIds()) {
        AnalysisWarningEvidence evidence = evidence(analysis, evidenceId);
        assertAll(
            () -> assertTrue(evidence.sourceType().equals("annotation")
                || evidence.sourceType().equals("code_symbol")),
            () -> assertTrue(evidence.sourcePath().startsWith("src/main/java/")),
            () -> assertNotNull(evidence.lineStart()),
            () -> assertNotNull(evidence.lineEnd()),
            () -> assertEquals("high", evidence.confidence()));
      }
    }

    assertAll(
        () -> assertTrue(analysis.evidence().stream()
            .filter(evidence -> "annotation".equals(evidence.sourceType()))
            .allMatch(evidence -> evidence.excerpt().equals(evidence.symbolName()))),
        () -> assertTrue(analysis.evidence().stream()
            .filter(evidence -> "code_symbol".equals(evidence.sourceType()))
            .allMatch(evidence -> evidence.symbolName().equals("SecurityFilterChain"))),
        () -> assertTrue(analysis.evidence().stream()
            .noneMatch(evidence -> evidence.excerpt().contains("authorizeHttpRequests"))),
        () -> assertTrue(analysis.warnings().stream()
            .noneMatch(warning -> warning.message().contains("protected"))));
  }

  @Test
  void securityWarningsAreSortedDeterministically() throws Exception {
    AnalysisWarningAnalysis analysis = analyzeFixture("spring-security-signals");

    assertEquals(
        List.of(
            "security_configuration_annotation:com.example.security.LegacyMethodSecuritySignals",
            "security_configuration_annotation:com.example.security.MethodSecuritySignals",
            "security_configuration_annotation:com.example.security.ReactiveMethodSecuritySignals",
            "security_configuration_annotation:com.example.security.WebFluxSecuritySignals",
            "security_configuration_annotation:com.example.security.WebSecuritySignals",
            "security_filter_chain_bean:com.example.security.WebSecuritySignals#actuatorSecurity",
            "security_filter_chain_bean:com.example.security.WebSecuritySignals#apiSecurity"),
        analysis.warnings().stream()
            .map(warning -> warning.signal()
                + ":"
                + evidence(analysis, warning.evidenceIds().get(0)).className()
                + OptionalMethodLabel.of(evidence(analysis, warning.evidenceIds().get(0)).methodName()))
            .toList());
  }

  private AnalysisWarningAnalysis analyzeFixture(String fixtureName) throws Exception {
    Path fixtureRoot = Path.of(Objects.requireNonNull(
        getClass().getResource("/fixtures/" + fixtureName)).toURI());
    return analyzer.analyze(fixtureRoot, List.of(fixtureRoot.resolve("src/main/java")), ".");
  }

  private boolean hasWarning(
      AnalysisWarningAnalysis analysis,
      String signal,
      String idFragment) {
    return analysis.warnings().stream()
        .filter(warning -> warning.signal().equals(signal))
        .anyMatch(warning -> warning.id().contains(idFragment));
  }

  private AnalysisWarningEvidence evidence(
      AnalysisWarningAnalysis analysis,
      String evidenceId) {
    return analysis.evidence().stream()
        .filter(candidate -> candidate.id().equals(evidenceId))
        .findFirst()
        .orElseThrow();
  }

  private record OptionalMethodLabel(String value) {
    static String of(String methodName) {
      return methodName == null ? "" : "#" + methodName;
    }
  }
}
