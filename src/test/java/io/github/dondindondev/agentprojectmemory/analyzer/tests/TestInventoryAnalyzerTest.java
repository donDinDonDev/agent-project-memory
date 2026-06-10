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
        () -> assertEquals("inferred", subject.relationStatus()),
        () -> assertEquals("naming_convention", subject.relationType()),
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
  void importedProductionClassInfersTestedSubject() throws Exception {
    TestInventoryAnalysis analysis = analyzeFixture();

    TestClassFact test = test(analysis, "com.example.imported.ImportedSubjectSpec");
    TestedSubjectFact subject = subject(
        test,
        "inferred",
        "test_import",
        "com.example.subject.ImportedSubject");

    assertAll(
        () -> assertEquals("medium", subject.confidence()),
        () -> assertNull(subject.uncertainty()),
        () -> assertTrue(subject.evidenceIds().stream()
            .map(evidenceId -> evidence(analysis, evidenceId))
            .anyMatch(record -> "import com.example.subject.ImportedSubject".equals(record.symbolName()))),
        () -> assertTrue(subject.evidenceIds().stream()
            .map(evidenceId -> evidence(analysis, evidenceId))
            .anyMatch(record -> "com.example.subject.ImportedSubject".equals(record.symbolName()))));
  }

  @Test
  void fieldTypeAndSliceClassLiteralInferTestedSubjects() throws Exception {
    TestInventoryAnalysis analysis = analyzeFixture();

    TestClassFact springBootSlice = test(analysis, "com.example.web.SpringSlice");
    TestClassFact webMvcSlice = test(analysis, "com.example.web.WebControllerSlice");

    TestedSubjectFact fieldSubject = subject(
        springBootSlice,
        "inferred",
        "test_field_type",
        "com.example.web.ProjectMapController");
    TestedSubjectFact sliceSubject = subject(
        webMvcSlice,
        "inferred",
        "spring_test_slice_class_literal",
        "com.example.web.ProjectMapController");

    assertAll(
        () -> assertEquals("medium", fieldSubject.confidence()),
        () -> assertNull(fieldSubject.uncertainty()),
        () -> assertTrue(fieldSubject.evidenceIds().stream()
            .map(evidenceId -> evidence(analysis, evidenceId))
            .anyMatch(record -> "ProjectMapController".equals(record.symbolName())
                && record.id().contains(":field:projectMapController:type:"))),
        () -> assertEquals("medium", sliceSubject.confidence()),
        () -> assertTrue(sliceSubject.evidenceIds().stream()
            .map(evidenceId -> evidence(analysis, evidenceId))
            .anyMatch(record -> "@WebMvcTest".equals(record.symbolName()))));
  }

  @Test
  void noMatchUnsupportedAndNotDetectedSubjectCasesAreStatused() throws Exception {
    TestInventoryAnalysis analysis = analyzeFixture();

    TestedSubjectFact missing = subjectStatus(
        test(analysis, "com.example.web.MissingControllerTest"),
        "not_detected",
        "naming_convention",
        "MissingController");
    TestedSubjectFact unsupported = subjectStatus(
        test(analysis, "com.example.web.UnsupportedFieldTypeTest"),
        "unsupported",
        "test_field_type",
        "List<ProjectMapController>");
    TestedSubjectFact noSignal = subjectStatus(
        test(analysis, "com.example.web.HealthSpec"),
        "not_detected",
        "not_detected",
        null);

    assertAll(
        () -> assertNull(missing.className()),
        () -> assertEquals("low", missing.confidence()),
        () -> assertEquals("no_matching_production_class", missing.uncertainty()),
        () -> assertNull(unsupported.className()),
        () -> assertEquals("unsupported_subject_reference", unsupported.uncertainty()),
        () -> assertEquals("no_supported_subject_signal", noSignal.uncertainty()));
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
        () -> assertEquals(
            List.of("healthIsAvailable", "healthParameterized", "healthRepeats"),
            test.methods().stream().map(TestMethodFact::methodName).toList()),
        () -> assertTrue(junitJupiter.evidenceIds().stream()
            .map(evidenceId -> evidence(analysis, evidenceId))
            .anyMatch(record -> "@Test".equals(record.symbolName())
                && "healthIsAvailable".equals(record.methodName()))));
  }

  @Test
  void boundedTestMethodInventoryIncludesOnlySupportedDirectJUnitTestAnnotations() throws Exception {
    TestInventoryAnalysis analysis = analyzeFixture();

    TestClassFact healthSpec = test(analysis, "com.example.web.HealthSpec");
    TestClassFact legacyTest = test(analysis, "com.example.web.LegacyControllerTest");

    assertAll(
        () -> assertEquals(
            List.of("@Test", "@ParameterizedTest", "@RepeatedTest"),
            healthSpec.methods().stream().map(TestMethodFact::testAnnotation).toList()),
        () -> assertTrue(healthSpec.methods().stream()
            .allMatch(method -> "test".equals(method.methodKind()))),
        () -> assertTrue(healthSpec.methods().stream()
            .allMatch(method -> method.displayName() == null)),
        () -> assertFalse(healthSpec.methods().stream()
            .map(TestMethodFact::methodName)
            .anyMatch("setUp"::equals)),
        () -> assertEquals(
            List.of("usesJUnitFour"),
            legacyTest.methods().stream().map(TestMethodFact::methodName).toList()),
        () -> assertEquals(
            List.of("@Test"),
            legacyTest.methods().stream().map(TestMethodFact::testAnnotation).toList()));
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
        () -> assertEquals("framework", junitJupiter.signalKind()),
        () -> assertEquals("framework", junitFour.signalKind()),
        () -> assertEquals("framework", springTest.signalKind()),
        () -> assertSignalEvidenceResolves(analysis, junitJupiter),
        () -> assertSignalEvidenceResolves(analysis, junitFour),
        () -> assertSignalEvidenceResolves(analysis, springTest),
        () -> assertTrue(springTest.evidenceIds().stream()
            .map(evidenceId -> evidence(analysis, evidenceId))
            .anyMatch(record -> "@SpringBootTest".equals(record.symbolName()))));
  }

  @Test
  void springTestSliceSignalsAreDetectedAsSourceVisibleAnnotations() throws Exception {
    TestInventoryAnalysis analysis = analyzeFixture();

    TestClassFact springBootSlice = test(analysis, "com.example.web.SpringSlice");
    TestClassFact webMvcSlice = test(analysis, "com.example.web.WebControllerSlice");
    TestClassFact dataJpaSlice = test(analysis, "com.example.web.DataRepositorySlice");

    assertAll(
        () -> assertEquals(
            List.of("@SpringBootTest"),
            springBootSlice.springTestSlices().stream().map(TestSpringSliceFact::annotation).toList()),
        () -> assertEquals(
            List.of("spring_boot_test"),
            springBootSlice.springTestSlices().stream().map(TestSpringSliceFact::sliceKind).toList()),
        () -> assertEquals(
            List.of("@WebMvcTest"),
            webMvcSlice.springTestSlices().stream().map(TestSpringSliceFact::annotation).toList()),
        () -> assertEquals(
            List.of("web_mvc_test"),
            webMvcSlice.springTestSlices().stream().map(TestSpringSliceFact::sliceKind).toList()),
        () -> assertEquals(
            List.of("@DataJpaTest"),
            dataJpaSlice.springTestSlices().stream().map(TestSpringSliceFact::annotation).toList()),
        () -> assertEquals(
            List.of("data_jpa_test"),
            dataJpaSlice.springTestSlices().stream().map(TestSpringSliceFact::sliceKind).toList()),
        () -> assertTrue(springBootSlice.springTestSlices().stream()
            .allMatch(slice -> "spring_test_slice".equals(slice.signalKind()))),
        () -> assertTrue(webMvcSlice.springTestSlices().stream()
            .flatMap(slice -> slice.evidenceIds().stream())
            .map(evidenceId -> evidence(analysis, evidenceId))
            .anyMatch(record -> "@WebMvcTest".equals(record.symbolName()))));
  }

  @Test
  void mockSignalsAreDetectedAsConservativeAnnotationSignals() throws Exception {
    TestInventoryAnalysis analysis = analyzeFixture();

    TestClassFact springBootSlice = test(analysis, "com.example.web.SpringSlice");
    TestClassFact webMvcSlice = test(analysis, "com.example.web.WebControllerSlice");

    assertAll(
        () -> assertEquals(
            List.of("@MockBean"),
            springBootSlice.mockSignals().stream().map(TestMockSignalFact::annotation).toList()),
        () -> assertEquals(
            List.of("projectMapController"),
            springBootSlice.mockSignals().stream().map(TestMockSignalFact::targetName).toList()),
        () -> assertEquals(
            List.of("@MockBean", "@SpyBean"),
            webMvcSlice.mockSignals().stream().map(TestMockSignalFact::annotation).toList()),
        () -> assertEquals(
            List.of("controller", "spyController"),
            webMvcSlice.mockSignals().stream().map(TestMockSignalFact::targetName).toList()),
        () -> assertTrue(webMvcSlice.mockSignals().stream()
            .allMatch(signal -> "mock_annotation".equals(signal.signalKind()))),
        () -> assertTrue(webMvcSlice.mockSignals().stream()
            .allMatch(signal -> "field".equals(signal.targetKind()))),
        () -> assertTrue(webMvcSlice.mockSignals().stream()
            .flatMap(signal -> signal.evidenceIds().stream())
            .map(evidenceId -> evidence(analysis, evidenceId))
            .anyMatch(record -> "@SpyBean".equals(record.symbolName()))));
  }

  @Test
  void springTestSignalsRequireResolvedExternalOrigin() throws Exception {
    TestInventoryAnalysis analysis = analyzeSpoofedOriginsFixture();

    TestClassFact sourceDeclared = test(analysis, "com.example.web.SourceDeclaredSpringBootTest");
    TestClassFact unresolvedSimple = test(analysis, "com.example.web.UnresolvedSimpleSpringTest");
    TestClassFact sourceDeclaredJupiter = test(analysis, "com.example.web.SourceDeclaredJupiterTest");
    TestClassFact sourceDeclaredMockBean = test(analysis, "com.example.web.SourceDeclaredMockBeanTest");

    assertAll(
        () -> assertEquals("analyzed", analysis.analysisStatus()),
        () -> assertTrue(sourceDeclared.frameworkSignals().isEmpty()),
        () -> assertTrue(unresolvedSimple.frameworkSignals().isEmpty()),
        () -> assertTrue(sourceDeclaredJupiter.frameworkSignals().isEmpty()),
        () -> assertTrue(sourceDeclared.springTestSlices().isEmpty()),
        () -> assertTrue(unresolvedSimple.springTestSlices().isEmpty()),
        () -> assertTrue(sourceDeclaredMockBean.mockSignals().isEmpty()),
        () -> assertTrue(sourceDeclaredJupiter.methods().isEmpty()),
        () -> assertFalse(analysis.evidence().stream()
            .anyMatch(record -> "@SpringBootTest".equals(record.symbolName()))),
        () -> assertFalse(analysis.evidence().stream()
            .anyMatch(record -> "@MockBean".equals(record.symbolName()))),
        () -> assertFalse(analysis.evidence().stream()
            .anyMatch(record -> "@Test".equals(record.symbolName()))),
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
            .allMatch(subject -> "ambiguous".equals(subject.relationStatus()))),
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
        () -> assertEquals(
            List.of("not_detected"),
            nestedTest.testedSubjects().stream().map(TestedSubjectFact::relationStatus).toList()),
        () -> assertEquals("no_supported_subject_signal", nestedTest.testedSubjects().get(0).uncertainty()),
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
      for (TestSpringSliceFact slice : test.springTestSlices()) {
        assertFalse(slice.evidenceIds().isEmpty());
        assertTrue(evidenceIds.containsAll(slice.evidenceIds()));
      }
      for (TestMockSignalFact signal : test.mockSignals()) {
        assertFalse(signal.evidenceIds().isEmpty());
        assertTrue(evidenceIds.containsAll(signal.evidenceIds()));
      }
      for (TestMethodFact method : test.methods()) {
        assertFalse(method.evidenceIds().isEmpty());
        assertTrue(evidenceIds.containsAll(method.evidenceIds()));
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

  private TestedSubjectFact subject(
      TestClassFact test,
      String relationStatus,
      String relationType,
      String className) {
    return test.testedSubjects().stream()
        .filter(candidate -> relationStatus.equals(candidate.relationStatus()))
        .filter(candidate -> relationType.equals(candidate.relationType()))
        .filter(candidate -> className.equals(candidate.className()))
        .findFirst()
        .orElseThrow();
  }

  private TestedSubjectFact subjectStatus(
      TestClassFact test,
      String relationStatus,
      String relationType,
      String candidateReference) {
    return test.testedSubjects().stream()
        .filter(candidate -> relationStatus.equals(candidate.relationStatus()))
        .filter(candidate -> relationType.equals(candidate.relationType()))
        .filter(candidate -> Objects.equals(candidateReference, candidate.candidateReference()))
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
