package io.github.dondindondev.agentprojectmemory.analyzer.tests;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.Test;

final class TestInventoryAnalyzerTest {
  private final TestInventoryAnalyzer analyzer = new TestInventoryAnalyzer();

  @Test
  void namingConventionMatchInfersTestedSubject() throws Exception {
    TestInventoryAnalysis analysis = analyzeFixture();

    TestClassFact test = test(analysis, "com.example.web.ProjectMapControllerTest");
    TestedSubjectFact subject = test.testedSubjects().get(0);

    assertAll(
        () -> assertEquals("analyzed", analysis.analysisStatus()),
        () -> assertEquals("com.example.web.ProjectMapController", subject.className()),
        () -> assertEquals("inferred", subject.supportType()),
        () -> assertEquals("medium", subject.confidence()),
        () -> assertNull(subject.uncertainty()),
        () -> assertEquals(2, subject.evidenceIds().size()),
        () -> assertTrue(subject.evidenceIds().stream()
            .map(evidenceId -> evidence(analysis, evidenceId))
            .anyMatch(record -> "test_file".equals(record.sourceType()))),
        () -> assertTrue(subject.evidenceIds().stream()
            .map(evidenceId -> evidence(analysis, evidenceId))
            .anyMatch(record -> "code_symbol".equals(record.sourceType()))));
  }

  @Test
  void modernInstanceofPatternInTestSourcesIsParsed() throws Exception {
    TestInventoryAnalysis analysis = analyzeModernJavaFixture();

    TestClassFact test = test(analysis, "com.example.modern.ModernControllerTest");

    assertAll(
        () -> assertEquals("analyzed", analysis.analysisStatus()),
        () -> assertEquals("src/test/java/com/example/modern/ModernControllerTest.java", test.sourcePath()),
        () -> assertEquals(
            List.of("JUnit Jupiter"),
            test.frameworkSignals().stream().map(TestFrameworkSignalFact::name).toList()),
        () -> assertEquals(
            List.of("com.example.modern.ModernController"),
            test.testedSubjects().stream().map(TestedSubjectFact::className).toList()));
  }

  @Test
  void annotationBasedTestClassWithNonTestNameIsDetectedViaTestAnnotation() throws Exception {
    TestInventoryAnalysis analysis = analyzeFixture();

    TestClassFact test = test(analysis, "com.example.web.HealthSpec");
    TestFrameworkSignalFact junitJupiter = frameworkSignal(test, "JUnit Jupiter");

    assertAll(
        () -> assertEquals("src/test/java/com/example/web/HealthSpec.java", test.sourcePath()),
        () -> assertTrue(junitJupiter.evidenceIds().stream()
            .map(evidenceId -> evidence(analysis, evidenceId))
            .anyMatch(record -> "@Test".equals(record.symbolName())
                && "healthIsAvailable".equals(record.methodName()))));
  }

  @Test
  void frameworkSignalsAreDetectedWithEvidenceIds() throws Exception {
    TestInventoryAnalysis analysis = analyzeFixture();

    TestFrameworkSignalFact junitJupiter = frameworkSignal(
        test(analysis, "com.example.web.HealthSpec"),
        "JUnit Jupiter");
    TestFrameworkSignalFact junitFour = frameworkSignal(
        test(analysis, "com.example.web.LegacyControllerTest"),
        "JUnit 4");
    TestFrameworkSignalFact springTest = frameworkSignal(
        test(analysis, "com.example.web.SpringSlice"),
        "Spring Test");

    assertAll(
        () -> assertSignalEvidenceResolves(analysis, junitJupiter),
        () -> assertSignalEvidenceResolves(analysis, junitFour),
        () -> assertSignalEvidenceResolves(analysis, springTest),
        () -> assertTrue(springTest.evidenceIds().stream()
            .map(evidenceId -> evidence(analysis, evidenceId))
            .anyMatch(record -> "@SpringBootTest".equals(record.symbolName()))));
  }

  @Test
  void springTestSignalsRequireResolvedExternalOrigin() throws Exception {
    TestInventoryAnalysis analysis = analyzeSpoofedOriginsFixture();

    TestClassFact sourceDeclared = test(analysis, "com.example.web.SourceDeclaredSpringBootTest");
    TestClassFact unresolvedSimple = test(analysis, "com.example.web.UnresolvedSimpleSpringTest");

    assertAll(
        () -> assertEquals("analyzed", analysis.analysisStatus()),
        () -> assertTrue(sourceDeclared.frameworkSignals().isEmpty()),
        () -> assertTrue(unresolvedSimple.frameworkSignals().isEmpty()),
        () -> assertFalse(analysis.evidence().stream()
            .anyMatch(record -> "@SpringBootTest".equals(record.symbolName()))),
        () -> assertFalse(analysis.evidence().stream()
            .anyMatch(record -> record.symbolName().startsWith(
                "import org.springframework.boot.test.context.SpringBootTest"))));
  }

  @Test
  void ambiguousSubjectNameProducesLowConfidenceAndUncertainty() throws Exception {
    TestInventoryAnalysis analysis = analyzeFixture();

    TestClassFact test = test(analysis, "com.example.alpha.DuplicateServiceTest");

    assertAll(
        () -> assertEquals(
            List.of("com.example.alpha.DuplicateService", "com.example.beta.DuplicateService"),
            test.testedSubjects().stream().map(TestedSubjectFact::className).toList()),
        () -> assertTrue(test.testedSubjects().stream()
            .allMatch(subject -> "inferred".equals(subject.supportType()))),
        () -> assertTrue(test.testedSubjects().stream()
            .allMatch(subject -> "low".equals(subject.confidence()))),
        () -> assertTrue(test.testedSubjects().stream()
            .allMatch(subject -> "ambiguous_subject_name".equals(subject.uncertainty()))));
  }

  @Test
  void helperClassesWithoutTestSignalsOrClearTestNamesAreOmitted() throws Exception {
    TestInventoryAnalysis analysis = analyzeFixture();

    assertAll(
        () -> assertTestNotPresent(analysis, "com.example.support.TestFixtureSupport"),
        () -> assertTestNotPresent(analysis, "com.example.support.TestSupportConfiguration"),
        () -> assertTestNotPresent(analysis, "com.example.web.NestedAndHelperTests.NestedTestConfiguration"));
  }

  @Test
  void nestedTestClassUsesOnlyItsOwnAnnotationSignals() throws Exception {
    TestInventoryAnalysis analysis = analyzeFixture();

    TestClassFact nestedTest = test(analysis, "com.example.web.NestedAndHelperTests.ValidationCases");
    TestFrameworkSignalFact junitJupiter = frameworkSignal(nestedTest, "JUnit Jupiter");

    assertAll(
        () -> assertEquals("src/test/java/com/example/web/NestedAndHelperTests.java", nestedTest.sourcePath()),
        () -> assertTrue(nestedTest.testedSubjects().isEmpty()),
        () -> assertTrue(junitJupiter.evidenceIds().stream()
            .map(evidenceId -> evidence(analysis, evidenceId))
            .anyMatch(record -> "@Nested".equals(record.symbolName()))),
        () -> assertTrue(junitJupiter.evidenceIds().stream()
            .map(evidenceId -> evidence(analysis, evidenceId))
            .anyMatch(record -> "@Test".equals(record.symbolName())
                && "rejectsInvalidInput".equals(record.methodName()))),
        () -> assertTrue(junitJupiter.evidenceIds().stream()
            .map(evidenceId -> evidence(analysis, evidenceId))
            .noneMatch(record -> "code_symbol".equals(record.sourceType())
                && record.symbolName().startsWith("import "))));
  }

  @Test
  void allTestInventoryEvidenceIdsResolveInsideAnalysis() throws Exception {
    TestInventoryAnalysis analysis = analyzeFixture();
    Set<String> evidenceIds = new HashSet<>(analysis.evidence().stream()
        .map(TestInventoryEvidence::id)
        .toList());

    for (TestClassFact test : analysis.tests()) {
      assertTrue(evidenceIds.containsAll(test.evidenceIds()));
      for (TestFrameworkSignalFact signal : test.frameworkSignals()) {
        assertFalse(signal.evidenceIds().isEmpty());
        assertTrue(evidenceIds.containsAll(signal.evidenceIds()));
      }
      for (TestedSubjectFact subject : test.testedSubjects()) {
        assertFalse(subject.evidenceIds().isEmpty());
        assertTrue(evidenceIds.containsAll(subject.evidenceIds()));
      }
    }
  }

  private TestInventoryAnalysis analyzeFixture() throws Exception {
    Path fixtureRoot = fixtureRoot();
    return analyzer.analyze(
        fixtureRoot,
        List.of(fixtureRoot.resolve("src/main/java")),
        List.of(fixtureRoot.resolve("src/test/java")));
  }

  private TestInventoryAnalysis analyzeModernJavaFixture() throws Exception {
    Path fixtureRoot = modernJavaFixtureRoot();
    return analyzer.analyze(
        fixtureRoot,
        List.of(fixtureRoot.resolve("src/main/java")),
        List.of(fixtureRoot.resolve("src/test/java")));
  }

  private TestInventoryAnalysis analyzeSpoofedOriginsFixture() throws Exception {
    Path fixtureRoot = spoofedOriginsFixtureRoot();
    return analyzer.analyze(
        fixtureRoot,
        List.of(),
        List.of(fixtureRoot.resolve("src/test/java")));
  }

  private Path fixtureRoot() throws Exception {
    return Path.of(Objects.requireNonNull(
        getClass().getResource("/fixtures/test-inventory")).toURI());
  }

  private Path modernJavaFixtureRoot() throws Exception {
    return Path.of(Objects.requireNonNull(
        getClass().getResource("/fixtures/modern-java-syntax")).toURI());
  }

  private Path spoofedOriginsFixtureRoot() throws Exception {
    return Path.of(Objects.requireNonNull(
        getClass().getResource("/fixtures/test-inventory-spoofed-origins")).toURI());
  }

  private TestClassFact test(TestInventoryAnalysis analysis, String className) {
    return analysis.tests().stream()
        .filter(candidate -> candidate.className().equals(className))
        .findFirst()
        .orElseThrow();
  }

  private void assertTestNotPresent(TestInventoryAnalysis analysis, String className) {
    assertTrue(analysis.tests().stream()
        .noneMatch(candidate -> candidate.className().equals(className)));
  }

  private TestFrameworkSignalFact frameworkSignal(TestClassFact test, String name) {
    return test.frameworkSignals().stream()
        .filter(candidate -> candidate.name().equals(name))
        .findFirst()
        .orElseThrow();
  }

  private TestInventoryEvidence evidence(TestInventoryAnalysis analysis, String evidenceId) {
    TestInventoryEvidence evidence = analysis.evidence().stream()
        .filter(candidate -> candidate.id().equals(evidenceId))
        .findFirst()
        .orElseThrow();

    assertAll(
        () -> assertNotNull(evidence.lineStart()),
        () -> assertNotNull(evidence.lineEnd()),
        () -> assertFalse(evidence.excerpt().isBlank()),
        () -> assertEquals("high", evidence.confidence()));
    return evidence;
  }

  private void assertSignalEvidenceResolves(
      TestInventoryAnalysis analysis,
      TestFrameworkSignalFact signal) {
    assertFalse(signal.evidenceIds().isEmpty());
    for (String evidenceId : signal.evidenceIds()) {
      evidence(analysis, evidenceId);
    }
  }
}
