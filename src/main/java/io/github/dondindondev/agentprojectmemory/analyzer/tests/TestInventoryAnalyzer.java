package io.github.dondindondev.agentprojectmemory.analyzer.tests;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import io.github.dondindondev.agentprojectmemory.analyzer.EvidenceExcerpts;
import io.github.dondindondev.agentprojectmemory.analyzer.JavaSourceOrigins;
import io.github.dondindondev.agentprojectmemory.analyzer.JavaSourceParser;
import io.github.dondindondev.agentprojectmemory.analyzer.ScanPathContainment;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public final class TestInventoryAnalyzer {
  private static final String ANALYZED = "analyzed";
  private static final String NOT_DETECTED = "not_detected";
  private static final String TEST_FILE_SOURCE_TYPE = "test_file";
  private static final String CODE_SYMBOL_SOURCE_TYPE = "code_symbol";
  private static final String ANNOTATION_SOURCE_TYPE = "annotation";
  private static final String HIGH_CONFIDENCE = "high";
  private static final String MEDIUM_CONFIDENCE = "medium";
  private static final String LOW_CONFIDENCE = "low";
  private static final String SUPPORT_TYPE_INFERRED = "inferred";
  private static final String RELATION_STATUS_INFERRED = "inferred";
  private static final String RELATION_STATUS_NOT_DETECTED = "not_detected";
  private static final String RELATION_STATUS_AMBIGUOUS = "ambiguous";
  private static final String RELATION_STATUS_UNSUPPORTED = "unsupported";
  private static final String RELATION_TYPE_NAMING_CONVENTION = "naming_convention";
  private static final String RELATION_TYPE_TEST_IMPORT = "test_import";
  private static final String RELATION_TYPE_TEST_FIELD_TYPE = "test_field_type";
  private static final String RELATION_TYPE_SPRING_SLICE_CLASS_LITERAL =
      "spring_test_slice_class_literal";
  private static final String RELATION_TYPE_NOT_DETECTED = "not_detected";
  private static final String SIGNAL_KIND_FRAMEWORK = "framework";
  private static final String SIGNAL_KIND_SPRING_TEST_SLICE = "spring_test_slice";
  private static final String SIGNAL_KIND_MOCK_ANNOTATION = "mock_annotation";
  private static final String METHOD_KIND_TEST = "test";
  private static final String TARGET_KIND_TYPE = "type";
  private static final String TARGET_KIND_FIELD = "field";
  private static final String AMBIGUOUS_SUBJECT_NAME = "ambiguous_subject_name";
  private static final String NO_MATCHING_PRODUCTION_CLASS = "no_matching_production_class";
  private static final String UNSUPPORTED_SUBJECT_REFERENCE = "unsupported_subject_reference";
  private static final String NO_SUPPORTED_SUBJECT_SIGNAL = "no_supported_subject_signal";
  private static final List<String> TEST_SUFFIXES = List.of("Test", "Tests", "IT");
  private static final Set<String> JAVA_LANG_SIMPLE_TYPES = Set.of(
      "Boolean",
      "Byte",
      "Character",
      "CharSequence",
      "Class",
      "Double",
      "Enum",
      "Exception",
      "Float",
      "Integer",
      "Long",
      "Number",
      "Object",
      "RuntimeException",
      "Short",
      "String",
      "StringBuilder",
      "StringBuffer",
      "Throwable",
      "Void");
  private static final Map<String, Set<String>> JUNIT_TEST_METHOD_ANNOTATION_ORIGINS = Map.ofEntries(
      Map.entry("Test", Set.of("org.junit.jupiter.api.Test", "org.junit.Test")),
      Map.entry("ParameterizedTest", Set.of("org.junit.jupiter.params.ParameterizedTest")),
      Map.entry("RepeatedTest", Set.of("org.junit.jupiter.api.RepeatedTest")),
      Map.entry("TestFactory", Set.of("org.junit.jupiter.api.TestFactory")),
      Map.entry("TestTemplate", Set.of("org.junit.jupiter.api.TestTemplate")));
  private static final Map<String, Set<String>> JUNIT_NESTED_ANNOTATION_ORIGINS = Map.of(
      "Nested",
      Set.of("org.junit.jupiter.api.Nested"));
  private static final Map<String, Set<String>> JUNIT_RUN_WITH_ANNOTATION_ORIGINS = Map.of(
      "RunWith",
      Set.of("org.junit.runner.RunWith"));
  private static final Set<String> SPRING_TEST_CLASS_MARKER_ANNOTATIONS = Set.of(
      "SpringBootTest",
      "WebMvcTest",
      "DataJpaTest",
      "ContextConfiguration");
  private static final Map<String, Set<String>> SPRING_TEST_SLICE_ANNOTATION_ORIGINS = Map.ofEntries(
      Map.entry("SpringBootTest", Set.of("org.springframework.boot.test.context.SpringBootTest")),
      Map.entry("WebMvcTest", Set.of("org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest")),
      Map.entry("DataJpaTest", Set.of("org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest")),
      Map.entry("ContextConfiguration", Set.of("org.springframework.test.context.ContextConfiguration")));
  private static final Map<String, String> SPRING_TEST_SLICE_KINDS = Map.of(
      "SpringBootTest", "spring_boot_test",
      "WebMvcTest", "web_mvc_test",
      "DataJpaTest", "data_jpa_test",
      "ContextConfiguration", "context_configuration");
  private static final Map<String, Set<String>> SPRING_TEST_MOCK_ANNOTATION_ORIGINS = Map.of(
      "MockBean",
      Set.of("org.springframework.boot.test.mock.mockito.MockBean"),
      "SpyBean",
      Set.of("org.springframework.boot.test.mock.mockito.SpyBean"));
  private static final Map<String, String> SPRING_TEST_MOCK_SIGNALS = Map.of(
      "MockBean", "spring_boot_mockbean_annotation",
      "SpyBean", "spring_boot_spybean_annotation");
  private static final Map<String, Set<String>> SPRING_TEST_ANNOTATION_ORIGINS = Map.ofEntries(
      Map.entry("SpringBootTest", Set.of("org.springframework.boot.test.context.SpringBootTest")),
      Map.entry("WebMvcTest", Set.of("org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest")),
      Map.entry("DataJpaTest", Set.of("org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest")),
      Map.entry("ContextConfiguration", Set.of("org.springframework.test.context.ContextConfiguration")),
      Map.entry("ActiveProfiles", Set.of("org.springframework.test.context.ActiveProfiles")),
      Map.entry("DirtiesContext", Set.of("org.springframework.test.annotation.DirtiesContext")),
      Map.entry("Sql", Set.of("org.springframework.test.context.jdbc.Sql")),
      Map.entry("MockBean", Set.of("org.springframework.boot.test.mock.mockito.MockBean")),
      Map.entry("SpyBean", Set.of("org.springframework.boot.test.mock.mockito.SpyBean")),
      Map.entry("TestConfiguration", Set.of("org.springframework.boot.test.context.TestConfiguration")));
  private static final Comparator<TestClassFact> TEST_CLASS_ORDER = Comparator
      .comparing(TestClassFact::className)
      .thenComparing(TestClassFact::sourcePath);
  private static final Comparator<TestFrameworkSignalFact> FRAMEWORK_SIGNAL_ORDER = Comparator
      .comparing(TestFrameworkSignalFact::name);
  private static final Comparator<TestedSubjectFact> TESTED_SUBJECT_ORDER = Comparator
      .comparing(TestedSubjectFact::relationStatus)
      .thenComparing(TestedSubjectFact::relationType)
      .thenComparing(subject -> subject.className() == null ? "" : subject.className())
      .thenComparing(subject -> subject.candidateReference() == null ? "" : subject.candidateReference())
      .thenComparing(subject -> subject.supportType() == null ? "" : subject.supportType())
      .thenComparing(TestedSubjectFact::confidence)
      .thenComparing(subject -> subject.uncertainty() == null ? "" : subject.uncertainty());

  public TestInventoryAnalysis analyze(
      Path repositoryRoot,
      List<Path> productionSourceRoots,
      List<Path> testSourceRoots) throws IOException {
    Objects.requireNonNull(repositoryRoot, "repositoryRoot");
    Objects.requireNonNull(productionSourceRoots, "productionSourceRoots");
    Objects.requireNonNull(testSourceRoots, "testSourceRoots");

    Path normalizedRepositoryRoot = repositoryRoot.toAbsolutePath().normalize();
    Path canonicalRepositoryRoot = ScanPathContainment.canonicalRoot(normalizedRepositoryRoot);
    List<Path> existingTestRoots = existingRoots(
        normalizedRepositoryRoot,
        canonicalRepositoryRoot,
        testSourceRoots);
    if (existingTestRoots.isEmpty()) {
      return new TestInventoryAnalysis(NOT_DETECTED, List.of(), List.of());
    }

    ProductionClassIndex productionClassIndex = productionClassIndex(
        normalizedRepositoryRoot,
        canonicalRepositoryRoot,
        productionSourceRoots);
    Set<String> sourceDeclaredTypeNames = sourceDeclaredTypeNames(
        normalizedRepositoryRoot,
        canonicalRepositoryRoot,
        productionSourceRoots,
        testSourceRoots);
    List<TestClassFact> tests = new ArrayList<>();
    Map<String, TestInventoryEvidence> evidence = new LinkedHashMap<>();

    for (Path testRoot : existingTestRoots) {
      for (Path javaFile : javaFiles(canonicalRepositoryRoot, testRoot)) {
        analyzeTestFile(
            normalizedRepositoryRoot,
            javaFile,
            productionClassIndex,
            sourceDeclaredTypeNames,
            tests,
            evidence);
      }
    }

    return new TestInventoryAnalysis(
        ANALYZED,
        tests.stream().sorted(TEST_CLASS_ORDER).toList(),
        List.copyOf(evidence.values()));
  }

  private void analyzeTestFile(
      Path repositoryRoot,
      Path javaFile,
      ProductionClassIndex productionClassIndex,
      Set<String> sourceDeclaredTypeNames,
      List<TestClassFact> tests,
      Map<String, TestInventoryEvidence> evidence) throws IOException {
    CompilationUnit compilationUnit = JavaSourceParser.parse(javaFile);
    String packageName = compilationUnit.getPackageDeclaration()
        .map(packageDeclaration -> packageDeclaration.getName().asString())
        .orElse("");
    String sourcePath = repositoryRelativePath(repositoryRoot, javaFile);
    List<String> sourceLines = Files.readAllLines(javaFile, StandardCharsets.UTF_8);
    Map<String, ImportDeclaration> importsBySimpleName = importsBySimpleName(compilationUnit);
    Map<String, String> singleTypeImportsBySimpleName = JavaSourceOrigins.singleTypeImportsBySimpleName(
        compilationUnit);

    for (ClassOrInterfaceDeclaration type : compilationUnit.findAll(ClassOrInterfaceDeclaration.class)) {
      if (type.isInterface()) {
        continue;
      }

      if (!shouldEmitTestClass(type, importsBySimpleName, sourceDeclaredTypeNames)) {
        continue;
      }

      String className = qualifiedClassName(packageName, type);
      TestInventoryEvidence classEvidence = testClassEvidence(sourcePath, className, type, sourceLines);
      evidence.putIfAbsent(classEvidence.id(), classEvidence);

      List<TestMethodFact> testMethods = testMethods(
          sourcePath,
          className,
          type,
          singleTypeImportsBySimpleName,
          sourceDeclaredTypeNames,
          sourceLines,
          evidence);
      boolean nestedClass = isNestedClass(type);
      List<FrameworkEvidence> frameworkEvidence = frameworkEvidence(
          sourcePath,
          className,
          type,
          !nestedClass,
          importsBySimpleName,
          sourceDeclaredTypeNames,
          sourceLines);
      frameworkEvidence.forEach(record -> evidence.putIfAbsent(record.evidence().id(), record.evidence()));
      List<TestSpringSliceFact> springTestSlices = springTestSlices(
          sourcePath,
          className,
          type,
          singleTypeImportsBySimpleName,
          sourceDeclaredTypeNames,
          sourceLines,
          evidence);
      List<TestMockSignalFact> mockSignals = mockSignals(
          sourcePath,
          className,
          type,
          singleTypeImportsBySimpleName,
          sourceDeclaredTypeNames,
          sourceLines,
          evidence);

      List<TestedSubjectFact> testedSubjects = testedSubjects(
          sourcePath,
          packageName,
          className,
          type.getNameAsString(),
          type,
          importsBySimpleName,
          singleTypeImportsBySimpleName,
          !nestedClass,
          sourceDeclaredTypeNames,
          classEvidence,
          productionClassIndex,
          sourceLines,
          evidence);

      tests.add(new TestClassFact(
          className,
          sourcePath,
          frameworkSignals(frameworkEvidence),
          springTestSlices,
          mockSignals,
          testMethods,
          testedSubjects,
          List.of(classEvidence.id())));
    }
  }

  private Map<String, ImportDeclaration> importsBySimpleName(CompilationUnit compilationUnit) {
    Map<String, ImportDeclaration> imports = new LinkedHashMap<>();
    for (ImportDeclaration importDeclaration : compilationUnit.getImports()) {
      String importName = importDeclaration.getNameAsString();
      if (importDeclaration.isAsterisk()) {
        imports.putIfAbsent(importName + ".*", importDeclaration);
        continue;
      }
      imports.putIfAbsent(simpleName(importName), importDeclaration);
    }
    return imports;
  }

  private List<FrameworkEvidence> frameworkEvidence(
      String sourcePath,
      String className,
      ClassOrInterfaceDeclaration type,
      boolean includeImportEvidence,
      Map<String, ImportDeclaration> importsBySimpleName,
      Set<String> sourceDeclaredTypeNames,
      List<String> sourceLines) {
    List<FrameworkEvidence> evidence = new ArrayList<>();
    Set<String> seen = new LinkedHashSet<>();

    if (includeImportEvidence) {
      for (ImportDeclaration importDeclaration : importsBySimpleName.values()) {
        if (importDeclaration.isStatic() || importDeclaration.isAsterisk()) {
          continue;
        }
        frameworkName(Optional.of(importDeclaration.getNameAsString()), null, sourceDeclaredTypeNames)
            .ifPresent(frameworkName -> {
              TestInventoryEvidence importEvidence = importEvidence(
                  sourcePath,
                  className,
                  importDeclaration,
                  sourceLines);
              if (seen.add(importEvidence.id())) {
                evidence.add(new FrameworkEvidence(frameworkName, importEvidence));
              }
            });
      }
    }

    for (AnnotationLocation annotationLocation : annotationLocations(type)) {
      AnnotationExpr annotation = annotationLocation.annotation();
      Optional<String> resolvedAnnotationName = resolvedAnnotationName(annotation, importsBySimpleName);
      Optional<String> frameworkName = frameworkName(
          resolvedAnnotationName,
          simpleAnnotationName(annotation),
          sourceDeclaredTypeNames);
      if (frameworkName.isEmpty()) {
        continue;
      }

      TestInventoryEvidence annotationEvidence = annotationEvidence(
          sourcePath,
          className,
          annotationLocation.methodName(),
          annotation,
          sourceLines);
      if (seen.add(annotationEvidence.id())) {
        evidence.add(new FrameworkEvidence(frameworkName.orElseThrow(), annotationEvidence));
      }
    }

    return evidence;
  }

  private List<AnnotationLocation> annotationLocations(ClassOrInterfaceDeclaration type) {
    List<AnnotationLocation> annotations = new ArrayList<>();
    type.getAnnotations().forEach(annotation -> annotations.add(new AnnotationLocation(annotation, null)));
    for (MethodDeclaration method : type.getMethods()) {
      method.getAnnotations().forEach(annotation -> annotations.add(new AnnotationLocation(
          annotation,
          method.getNameAsString())));
    }
    return annotations;
  }

  private Optional<String> resolvedAnnotationName(
      AnnotationExpr annotation,
      Map<String, ImportDeclaration> importsBySimpleName) {
    String annotationName = annotation.getNameAsString();
    if (annotationName.contains(".")) {
      return Optional.of(annotationName);
    }

    ImportDeclaration importDeclaration = importsBySimpleName.get(annotationName);
    if (importDeclaration == null || importDeclaration.isStatic() || importDeclaration.isAsterisk()) {
      return Optional.empty();
    }
    return Optional.of(importDeclaration.getNameAsString());
  }

  private Optional<String> frameworkName(
      Optional<String> resolvedName,
      String simpleAnnotationName,
      Set<String> sourceDeclaredTypeNames) {
    if (resolvedName.isEmpty()) {
      return Optional.empty();
    }

    String name = resolvedName.orElseThrow();
    if (sourceDeclaredTypeNames.contains(name)) {
      return Optional.empty();
    }
    if (name.startsWith("org.junit.jupiter.")) {
      return Optional.of("JUnit Jupiter");
    }
    if (name.startsWith("org.junit.") && !name.startsWith("org.junit.jupiter.")) {
      return Optional.of("JUnit 4");
    }
    if (isSupportedSpringTestAnnotationOrigin(name, simpleAnnotationName, sourceDeclaredTypeNames)) {
      return Optional.of("Spring Test");
    }
    return Optional.empty();
  }

  private boolean isSupportedSpringTestAnnotationOrigin(
      String resolvedName,
      String simpleAnnotationName,
      Set<String> sourceDeclaredTypeNames) {
    return JavaSourceOrigins.supportedTypeSimpleName(
            resolvedName,
            Map.of(),
            SPRING_TEST_ANNOTATION_ORIGINS,
            sourceDeclaredTypeNames)
        .filter(resolvedSimpleName -> simpleAnnotationName == null
            || resolvedSimpleName.equals(simpleAnnotationName))
        .isPresent();
  }

  private boolean shouldEmitTestClass(
      ClassOrInterfaceDeclaration type,
      Map<String, ImportDeclaration> importsBySimpleName,
      Set<String> sourceDeclaredTypeNames) {
    return hasClearTestName(type.getNameAsString())
        || hasDirectTestClassSignal(type, importsBySimpleName, sourceDeclaredTypeNames);
  }

  private boolean hasClearTestName(String simpleName) {
    return subjectSimpleName(simpleName).isPresent();
  }

  private boolean hasDirectTestClassSignal(
      ClassOrInterfaceDeclaration type,
      Map<String, ImportDeclaration> importsBySimpleName,
      Set<String> sourceDeclaredTypeNames) {
    for (AnnotationLocation annotationLocation : annotationLocations(type)) {
      if (isTestClassMarker(annotationLocation, importsBySimpleName, sourceDeclaredTypeNames)) {
        return true;
      }
    }
    return false;
  }

  private boolean isTestClassMarker(
      AnnotationLocation annotationLocation,
      Map<String, ImportDeclaration> importsBySimpleName,
      Set<String> sourceDeclaredTypeNames) {
    AnnotationExpr annotation = annotationLocation.annotation();
    Optional<String> resolvedName = resolvedAnnotationName(annotation, importsBySimpleName);
    String simpleName = simpleAnnotationName(annotation);
    if (annotationLocation.methodName() != null) {
      return isSupportedAnnotation(
          annotation,
          importsBySimpleName,
          JUNIT_TEST_METHOD_ANNOTATION_ORIGINS,
          sourceDeclaredTypeNames);
    }

    return isJUnitNestedAnnotation(annotation, importsBySimpleName, sourceDeclaredTypeNames)
        || isJUnitFourRunWithAnnotation(annotation, importsBySimpleName, sourceDeclaredTypeNames)
        || isSpringTestClassMarkerAnnotation(resolvedName, simpleName, sourceDeclaredTypeNames);
  }

  private boolean isSupportedAnnotation(
      AnnotationExpr annotation,
      Map<String, ImportDeclaration> importsBySimpleName,
      Map<String, Set<String>> supportedOrigins,
      Set<String> sourceDeclaredTypeNames) {
    return JavaSourceOrigins.supportedTypeSimpleName(
            annotation.getNameAsString(),
            singleTypeImportNames(importsBySimpleName),
            supportedOrigins,
            sourceDeclaredTypeNames)
        .isPresent();
  }

  private boolean isJUnitNestedAnnotation(
      AnnotationExpr annotation,
      Map<String, ImportDeclaration> importsBySimpleName,
      Set<String> sourceDeclaredTypeNames) {
    return isSupportedAnnotation(
        annotation,
        importsBySimpleName,
        JUNIT_NESTED_ANNOTATION_ORIGINS,
        sourceDeclaredTypeNames);
  }

  private boolean isJUnitFourRunWithAnnotation(
      AnnotationExpr annotation,
      Map<String, ImportDeclaration> importsBySimpleName,
      Set<String> sourceDeclaredTypeNames) {
    return isSupportedAnnotation(
        annotation,
        importsBySimpleName,
        JUNIT_RUN_WITH_ANNOTATION_ORIGINS,
        sourceDeclaredTypeNames);
  }

  private boolean isSpringTestClassMarkerAnnotation(
      Optional<String> resolvedName,
      String simpleName,
      Set<String> sourceDeclaredTypeNames) {
    return SPRING_TEST_CLASS_MARKER_ANNOTATIONS.contains(simpleName)
        && resolvedName
            .map(name -> isSupportedSpringTestAnnotationOrigin(name, simpleName, sourceDeclaredTypeNames))
            .orElse(false);
  }

  private TestInventoryEvidence testClassEvidence(
      String sourcePath,
      String className,
      ClassOrInterfaceDeclaration type,
      List<String> sourceLines) {
    Integer lineStart = classDeclarationLine(type);
    Integer lineEnd = lineStart;

    return new TestInventoryEvidence(
        evidenceId(sourcePath, className, "test_file", lineStart, lineEnd),
        TEST_FILE_SOURCE_TYPE,
        sourcePath,
        className,
        null,
        className,
        lineStart,
        lineEnd,
        singleLineExcerpt(type, sourceLines, lineStart),
        HIGH_CONFIDENCE);
  }

  private TestInventoryEvidence importEvidence(
      String sourcePath,
      String className,
      ImportDeclaration importDeclaration,
      List<String> sourceLines) {
    String importName = importDeclaration.getNameAsString()
        + (importDeclaration.isAsterisk() ? ".*" : "");
    String importSymbol = importDeclaration.isStatic()
        ? "import static " + importName
        : "import " + importName;
    Integer lineStart = importDeclaration.getRange().map(range -> range.begin.line).orElse(null);
    Integer lineEnd = importDeclaration.getRange().map(range -> range.end.line).orElse(null);

    return new TestInventoryEvidence(
        evidenceId(sourcePath, className, "import:" + importName, lineStart, lineEnd),
        CODE_SYMBOL_SOURCE_TYPE,
        sourcePath,
        className,
        null,
        importSymbol,
        lineStart,
        lineEnd,
        excerpt(importDeclaration, sourceLines),
        HIGH_CONFIDENCE);
  }

  private TestInventoryEvidence annotationEvidence(
      String sourcePath,
      String className,
      String methodName,
      AnnotationExpr annotation,
      List<String> sourceLines) {
    return annotationEvidence(sourcePath, className, methodName, null, annotation, sourceLines);
  }

  private TestInventoryEvidence annotationEvidence(
      String sourcePath,
      String className,
      String methodName,
      String ownerDiscriminator,
      AnnotationExpr annotation,
      List<String> sourceLines) {
    String annotationSymbol = "@" + simpleAnnotationName(annotation);
    Integer lineStart = annotation.getRange().map(range -> range.begin.line).orElse(null);
    Integer lineEnd = annotation.getRange().map(range -> range.end.line).orElse(null);
    String owner = ownerDiscriminator == null
        ? methodName == null ? className : className + "#" + methodName
        : className + ":" + ownerDiscriminator;

    return new TestInventoryEvidence(
        evidenceId(sourcePath, owner, annotationSymbol, lineStart, lineEnd),
        ANNOTATION_SOURCE_TYPE,
        sourcePath,
        className,
        methodName,
        annotationSymbol,
        lineStart,
        lineEnd,
        excerpt(annotation, sourceLines),
        HIGH_CONFIDENCE);
  }

  private List<TestMethodFact> testMethods(
      String sourcePath,
      String className,
      ClassOrInterfaceDeclaration type,
      Map<String, String> singleTypeImportsBySimpleName,
      Set<String> sourceDeclaredTypeNames,
      List<String> sourceLines,
      Map<String, TestInventoryEvidence> evidence) {
    List<TestMethodFact> methods = new ArrayList<>();
    for (MethodDeclaration method : type.getMethods()) {
      Optional<AnnotationExpr> testAnnotation = supportedTestMethodAnnotation(
          method,
          singleTypeImportsBySimpleName,
          sourceDeclaredTypeNames);
      if (testAnnotation.isEmpty()) {
        continue;
      }

      TestInventoryEvidence methodEvidence = annotationEvidence(
          sourcePath,
          className,
          method.getNameAsString(),
          testAnnotation.orElseThrow(),
          sourceLines);
      evidence.putIfAbsent(methodEvidence.id(), methodEvidence);
      methods.add(new TestMethodFact(
          method.getNameAsString(),
          methodEvidence.symbolName(),
          METHOD_KIND_TEST,
          null,
          List.of(methodEvidence.id())));
    }
    return List.copyOf(methods);
  }

  private List<TestSpringSliceFact> springTestSlices(
      String sourcePath,
      String className,
      ClassOrInterfaceDeclaration type,
      Map<String, String> singleTypeImportsBySimpleName,
      Set<String> sourceDeclaredTypeNames,
      List<String> sourceLines,
      Map<String, TestInventoryEvidence> evidence) {
    List<TestSpringSliceFact> slices = new ArrayList<>();
    for (AnnotationExpr annotation : type.getAnnotations()) {
      Optional<String> supportedSimpleName = JavaSourceOrigins.supportedTypeSimpleName(
          annotation.getNameAsString(),
          singleTypeImportsBySimpleName,
          SPRING_TEST_SLICE_ANNOTATION_ORIGINS,
          sourceDeclaredTypeNames);
      if (supportedSimpleName.isEmpty()) {
        continue;
      }

      TestInventoryEvidence annotationEvidence = annotationEvidence(
          sourcePath,
          className,
          null,
          annotation,
          sourceLines);
      evidence.putIfAbsent(annotationEvidence.id(), annotationEvidence);
      String simpleName = supportedSimpleName.orElseThrow();
      slices.add(new TestSpringSliceFact(
          annotationEvidence.symbolName(),
          SPRING_TEST_SLICE_KINDS.get(simpleName),
          SIGNAL_KIND_SPRING_TEST_SLICE,
          List.of(annotationEvidence.id())));
    }
    return slices.stream()
        .sorted(Comparator
            .comparing(TestSpringSliceFact::sliceKind)
            .thenComparing(TestSpringSliceFact::annotation)
            .thenComparing(slice -> String.join("\n", slice.evidenceIds())))
        .toList();
  }

  private List<TestMockSignalFact> mockSignals(
      String sourcePath,
      String className,
      ClassOrInterfaceDeclaration type,
      Map<String, String> singleTypeImportsBySimpleName,
      Set<String> sourceDeclaredTypeNames,
      List<String> sourceLines,
      Map<String, TestInventoryEvidence> evidence) {
    List<TestMockSignalFact> mockSignals = new ArrayList<>();
    mockSignals.addAll(typeLevelMockSignals(
        sourcePath,
        className,
        type,
        singleTypeImportsBySimpleName,
        sourceDeclaredTypeNames,
        sourceLines,
        evidence));
    mockSignals.addAll(fieldLevelMockSignals(
        sourcePath,
        className,
        type,
        singleTypeImportsBySimpleName,
        sourceDeclaredTypeNames,
        sourceLines,
        evidence));
    return mockSignals.stream()
        .sorted(Comparator
            .comparing(TestMockSignalFact::targetKind)
            .thenComparing(TestMockSignalFact::targetName)
            .thenComparing(TestMockSignalFact::mockSignal)
            .thenComparing(TestMockSignalFact::annotation)
            .thenComparing(signal -> String.join("\n", signal.evidenceIds())))
        .toList();
  }

  private List<TestMockSignalFact> typeLevelMockSignals(
      String sourcePath,
      String className,
      ClassOrInterfaceDeclaration type,
      Map<String, String> singleTypeImportsBySimpleName,
      Set<String> sourceDeclaredTypeNames,
      List<String> sourceLines,
      Map<String, TestInventoryEvidence> evidence) {
    List<TestMockSignalFact> mockSignals = new ArrayList<>();
    for (AnnotationExpr annotation : type.getAnnotations()) {
      mockSignal(
          sourcePath,
          className,
          null,
          null,
          TARGET_KIND_TYPE,
          className,
          annotation,
          singleTypeImportsBySimpleName,
          sourceDeclaredTypeNames,
          sourceLines,
          evidence)
          .ifPresent(mockSignals::add);
    }
    return mockSignals;
  }

  private List<TestMockSignalFact> fieldLevelMockSignals(
      String sourcePath,
      String className,
      ClassOrInterfaceDeclaration type,
      Map<String, String> singleTypeImportsBySimpleName,
      Set<String> sourceDeclaredTypeNames,
      List<String> sourceLines,
      Map<String, TestInventoryEvidence> evidence) {
    List<TestMockSignalFact> mockSignals = new ArrayList<>();
    for (FieldDeclaration field : type.getFields()) {
      String targetName = field.getVariables().stream()
          .findFirst()
          .map(VariableDeclarator::getNameAsString)
          .orElse("<unknown>");
      for (AnnotationExpr annotation : field.getAnnotations()) {
        mockSignal(
            sourcePath,
            className,
            null,
            "field:" + targetName,
            TARGET_KIND_FIELD,
            targetName,
            annotation,
            singleTypeImportsBySimpleName,
            sourceDeclaredTypeNames,
            sourceLines,
            evidence)
            .ifPresent(mockSignals::add);
      }
    }
    return mockSignals;
  }

  private Optional<TestMockSignalFact> mockSignal(
      String sourcePath,
      String className,
      String methodName,
      String ownerDiscriminator,
      String targetKind,
      String targetName,
      AnnotationExpr annotation,
      Map<String, String> singleTypeImportsBySimpleName,
      Set<String> sourceDeclaredTypeNames,
      List<String> sourceLines,
      Map<String, TestInventoryEvidence> evidence) {
    Optional<String> supportedSimpleName = JavaSourceOrigins.supportedTypeSimpleName(
        annotation.getNameAsString(),
        singleTypeImportsBySimpleName,
        SPRING_TEST_MOCK_ANNOTATION_ORIGINS,
        sourceDeclaredTypeNames);
    if (supportedSimpleName.isEmpty()) {
      return Optional.empty();
    }

    TestInventoryEvidence annotationEvidence = annotationEvidence(
        sourcePath,
        className,
        methodName,
        ownerDiscriminator,
        annotation,
        sourceLines);
    evidence.putIfAbsent(annotationEvidence.id(), annotationEvidence);
    String simpleName = supportedSimpleName.orElseThrow();
    return Optional.of(new TestMockSignalFact(
        annotationEvidence.symbolName(),
        SPRING_TEST_MOCK_SIGNALS.get(simpleName),
        SIGNAL_KIND_MOCK_ANNOTATION,
        targetKind,
        targetName,
        List.of(annotationEvidence.id())));
  }

  private Optional<AnnotationExpr> supportedTestMethodAnnotation(
      MethodDeclaration method,
      Map<String, String> singleTypeImportsBySimpleName,
      Set<String> sourceDeclaredTypeNames) {
    return method.getAnnotations().stream()
        .filter(annotation -> JavaSourceOrigins.supportedTypeSimpleName(
                annotation.getNameAsString(),
                singleTypeImportsBySimpleName,
                JUNIT_TEST_METHOD_ANNOTATION_ORIGINS,
                sourceDeclaredTypeNames)
            .isPresent())
        .findFirst();
  }

  private List<TestFrameworkSignalFact> frameworkSignals(List<FrameworkEvidence> evidence) {
    Map<String, List<String>> evidenceIdsByFramework = new LinkedHashMap<>();
    for (FrameworkEvidence record : evidence) {
      evidenceIdsByFramework
          .computeIfAbsent(record.frameworkName(), ignored -> new ArrayList<>())
          .add(record.evidence().id());
    }

    return evidenceIdsByFramework.entrySet().stream()
        .map(entry -> new TestFrameworkSignalFact(
            entry.getKey(),
            SIGNAL_KIND_FRAMEWORK,
            entry.getValue().stream().sorted().toList()))
        .sorted(FRAMEWORK_SIGNAL_ORDER)
        .toList();
  }

  private List<TestedSubjectFact> testedSubjects(
      String sourcePath,
      String packageName,
      String className,
      String testSimpleName,
      ClassOrInterfaceDeclaration type,
      Map<String, ImportDeclaration> importsBySimpleName,
      Map<String, String> singleTypeImportsBySimpleName,
      boolean includeImportSubjectReferences,
      Set<String> sourceDeclaredTypeNames,
      TestInventoryEvidence testClassEvidence,
      ProductionClassIndex productionClassIndex,
      List<String> sourceLines,
      Map<String, TestInventoryEvidence> evidence) {
    Map<TestedSubjectKey, TestedSubjectAccumulator> testedSubjects = new LinkedHashMap<>();
    Optional<String> subjectSimpleName = subjectSimpleName(testSimpleName);
    subjectSimpleName.ifPresent(candidate -> addSubjectReference(
        testedSubjects,
        RELATION_TYPE_NAMING_CONVENTION,
        candidate,
        List.of(testClassEvidence.id()),
        productionClassIndex,
        evidence));

    if (includeImportSubjectReferences) {
      addImportSubjectReferences(
          sourcePath,
          className,
          importsBySimpleName,
          productionClassIndex,
          sourceLines,
          testedSubjects,
          evidence);
    }
    addFieldTypeSubjectReferences(
        sourcePath,
        packageName,
        className,
        type,
        singleTypeImportsBySimpleName,
        productionClassIndex,
        sourceLines,
        testedSubjects,
        evidence);
    addSpringSliceClassLiteralSubjectReferences(
        sourcePath,
        packageName,
        className,
        type,
        singleTypeImportsBySimpleName,
        sourceDeclaredTypeNames,
        productionClassIndex,
        sourceLines,
        testedSubjects,
        evidence);

    if (testedSubjects.isEmpty()) {
      addStatusOnlySubject(
          testedSubjects,
          RELATION_STATUS_NOT_DETECTED,
          RELATION_TYPE_NOT_DETECTED,
          null,
          LOW_CONFIDENCE,
          NO_SUPPORTED_SUBJECT_SIGNAL,
          List.of(testClassEvidence.id()));
    }

    return testedSubjects.values().stream()
        .map(TestedSubjectAccumulator::toFact)
        .sorted(TESTED_SUBJECT_ORDER)
        .toList();
  }

  private void addImportSubjectReferences(
      String sourcePath,
      String className,
      Map<String, ImportDeclaration> importsBySimpleName,
      ProductionClassIndex productionClassIndex,
      List<String> sourceLines,
      Map<TestedSubjectKey, TestedSubjectAccumulator> testedSubjects,
      Map<String, TestInventoryEvidence> evidence) {
    Set<String> seenImports = new LinkedHashSet<>();
    for (ImportDeclaration importDeclaration : importsBySimpleName.values()) {
      if (importDeclaration.isStatic() || importDeclaration.isAsterisk()) {
        continue;
      }
      String importName = importDeclaration.getNameAsString();
      if (!seenImports.add(importName)
          || !productionClassIndex.byClassName().containsKey(importName)) {
        continue;
      }

      TestInventoryEvidence importEvidence = importEvidence(
          sourcePath,
          className,
          importDeclaration,
          sourceLines);
      evidence.putIfAbsent(importEvidence.id(), importEvidence);
      addSubjectReference(
          testedSubjects,
          RELATION_TYPE_TEST_IMPORT,
          importName,
          List.of(importEvidence.id()),
          productionClassIndex,
          evidence);
    }
  }

  private void addFieldTypeSubjectReferences(
      String sourcePath,
      String packageName,
      String className,
      ClassOrInterfaceDeclaration type,
      Map<String, String> singleTypeImportsBySimpleName,
      ProductionClassIndex productionClassIndex,
      List<String> sourceLines,
      Map<TestedSubjectKey, TestedSubjectAccumulator> testedSubjects,
      Map<String, TestInventoryEvidence> evidence) {
    for (FieldDeclaration field : type.getFields()) {
      for (VariableDeclarator variable : field.getVariables()) {
        TestInventoryEvidence fieldTypeEvidence = fieldTypeEvidence(
            sourcePath,
            className,
            variable,
            sourceLines);
        SubjectReference fieldReference = subjectReference(
            packageName,
            singleTypeImportsBySimpleName,
            variable.getType(),
            productionClassIndex,
            true);
        if (fieldReference.status().equals(SubjectReferenceStatus.NOT_CANDIDATE)) {
          continue;
        }

        evidence.putIfAbsent(fieldTypeEvidence.id(), fieldTypeEvidence);
        if (fieldReference.status().equals(SubjectReferenceStatus.UNSUPPORTED)) {
          addStatusOnlySubject(
              testedSubjects,
              RELATION_STATUS_UNSUPPORTED,
              RELATION_TYPE_TEST_FIELD_TYPE,
              fieldReference.referenceName(),
              LOW_CONFIDENCE,
              UNSUPPORTED_SUBJECT_REFERENCE,
              List.of(fieldTypeEvidence.id()));
          continue;
        }

        addSubjectReference(
            testedSubjects,
            RELATION_TYPE_TEST_FIELD_TYPE,
            fieldReference.referenceName(),
            List.of(fieldTypeEvidence.id()),
            productionClassIndex,
            evidence);
      }
    }
  }

  private void addSpringSliceClassLiteralSubjectReferences(
      String sourcePath,
      String packageName,
      String className,
      ClassOrInterfaceDeclaration type,
      Map<String, String> singleTypeImportsBySimpleName,
      Set<String> sourceDeclaredTypeNames,
      ProductionClassIndex productionClassIndex,
      List<String> sourceLines,
      Map<TestedSubjectKey, TestedSubjectAccumulator> testedSubjects,
      Map<String, TestInventoryEvidence> evidence) {
    for (AnnotationExpr annotation : type.getAnnotations()) {
      Optional<String> supportedSimpleName = JavaSourceOrigins.supportedTypeSimpleName(
          annotation.getNameAsString(),
          singleTypeImportsBySimpleName,
          SPRING_TEST_SLICE_ANNOTATION_ORIGINS,
          sourceDeclaredTypeNames);
      if (supportedSimpleName.isEmpty()) {
        continue;
      }

      List<ClassExpr> classExpressions = annotation.findAll(ClassExpr.class);
      if (classExpressions.isEmpty()) {
        continue;
      }

      TestInventoryEvidence annotationEvidence = annotationEvidence(
          sourcePath,
          className,
          null,
          annotation,
          sourceLines);
      evidence.putIfAbsent(annotationEvidence.id(), annotationEvidence);
      for (ClassExpr classExpression : classExpressions) {
        SubjectReference classLiteralReference = subjectReference(
            packageName,
            singleTypeImportsBySimpleName,
            classExpression.getType(),
            productionClassIndex,
            true);
        if (classLiteralReference.status().equals(SubjectReferenceStatus.NOT_CANDIDATE)) {
          continue;
        }
        if (classLiteralReference.status().equals(SubjectReferenceStatus.UNSUPPORTED)) {
          addStatusOnlySubject(
              testedSubjects,
              RELATION_STATUS_UNSUPPORTED,
              RELATION_TYPE_SPRING_SLICE_CLASS_LITERAL,
              classLiteralReference.referenceName(),
              LOW_CONFIDENCE,
              UNSUPPORTED_SUBJECT_REFERENCE,
              List.of(annotationEvidence.id()));
          continue;
        }

        addSubjectReference(
            testedSubjects,
            RELATION_TYPE_SPRING_SLICE_CLASS_LITERAL,
            classLiteralReference.referenceName(),
            List.of(annotationEvidence.id()),
            productionClassIndex,
            evidence);
      }
    }
  }

  private SubjectReference subjectReference(
      String packageName,
      Map<String, String> singleTypeImportsBySimpleName,
      Type type,
      ProductionClassIndex productionClassIndex,
      boolean allowSamePackageNoMatch) {
    if (type.isArrayType()) {
      Type componentType = type.asArrayType().getComponentType();
      if (containsPotentialProductionReference(componentType, productionClassIndex)) {
        return new SubjectReference(
            SubjectReferenceStatus.UNSUPPORTED,
            type.toString());
      }
      return new SubjectReference(SubjectReferenceStatus.NOT_CANDIDATE, null);
    }
    if (!type.isClassOrInterfaceType()) {
      return new SubjectReference(SubjectReferenceStatus.NOT_CANDIDATE, null);
    }

    ClassOrInterfaceType classType = type.asClassOrInterfaceType();
    if (classType.getTypeArguments().isPresent()
        && !classType.getTypeArguments().orElseThrow().isEmpty()) {
      if (containsPotentialProductionReference(type, productionClassIndex)) {
        return new SubjectReference(
            SubjectReferenceStatus.UNSUPPORTED,
            type.toString());
      }
      return new SubjectReference(SubjectReferenceStatus.NOT_CANDIDATE, null);
    }

    String referenceName = classType.getNameWithScope();
    String simpleName = JavaSourceOrigins.simpleName(referenceName);
    if (!isPotentialSubjectSimpleName(simpleName)) {
      return new SubjectReference(SubjectReferenceStatus.NOT_CANDIDATE, null);
    }
    if (referenceName.contains(".")) {
      if (productionClassIndex.byClassName().containsKey(referenceName)) {
        return new SubjectReference(SubjectReferenceStatus.SUPPORTED, referenceName);
      }
      return new SubjectReference(SubjectReferenceStatus.NOT_CANDIDATE, null);
    }

    String importedQualifiedName = singleTypeImportsBySimpleName.get(simpleName);
    if (importedQualifiedName != null) {
      if (productionClassIndex.byClassName().containsKey(importedQualifiedName)) {
        return new SubjectReference(SubjectReferenceStatus.SUPPORTED, importedQualifiedName);
      }
      return new SubjectReference(SubjectReferenceStatus.NOT_CANDIDATE, null);
    }

    if (!allowSamePackageNoMatch) {
      return new SubjectReference(SubjectReferenceStatus.NOT_CANDIDATE, null);
    }
    return new SubjectReference(
        SubjectReferenceStatus.SUPPORTED,
        packageName.isBlank() ? simpleName : packageName + "." + simpleName);
  }

  private boolean containsPotentialProductionReference(
      Type type,
      ProductionClassIndex productionClassIndex) {
    if (type.isArrayType()) {
      return containsPotentialProductionReference(
          type.asArrayType().getComponentType(),
          productionClassIndex);
    }
    if (!type.isClassOrInterfaceType()) {
      return false;
    }

    ClassOrInterfaceType classType = type.asClassOrInterfaceType();
    String referenceName = classType.getNameWithScope();
    String simpleName = JavaSourceOrigins.simpleName(referenceName);
    if (isPotentialSubjectSimpleName(simpleName)
        && productionClassIndex.bySimpleName().containsKey(simpleName)) {
      return true;
    }
    return classType.getTypeArguments()
        .map(arguments -> arguments.stream()
            .anyMatch(argument -> containsPotentialProductionReference(argument, productionClassIndex)))
        .orElse(false);
  }

  private boolean isPotentialSubjectSimpleName(String simpleName) {
    return !simpleName.isBlank()
        && Character.isUpperCase(simpleName.charAt(0))
        && !JAVA_LANG_SIMPLE_TYPES.contains(simpleName);
  }

  private void addSubjectReference(
      Map<TestedSubjectKey, TestedSubjectAccumulator> testedSubjects,
      String relationType,
      String candidateReference,
      List<String> sourceEvidenceIds,
      ProductionClassIndex productionClassIndex,
      Map<String, TestInventoryEvidence> evidence) {
    List<ProductionClass> candidates = candidateReference.contains(".")
        ? productionClassIndex.byClassName().getOrDefault(candidateReference, List.of())
        : productionClassIndex.bySimpleName().getOrDefault(candidateReference, List.of());
    if (candidates.isEmpty()) {
      addStatusOnlySubject(
          testedSubjects,
          RELATION_STATUS_NOT_DETECTED,
          relationType,
          candidateReference,
          LOW_CONFIDENCE,
          NO_MATCHING_PRODUCTION_CLASS,
          sourceEvidenceIds);
      return;
    }

    boolean ambiguous = candidates.size() > 1;
    for (ProductionClass candidate : candidates) {
      evidence.putIfAbsent(candidate.evidence().id(), candidate.evidence());
      List<String> relationEvidenceIds = new ArrayList<>(sourceEvidenceIds);
      relationEvidenceIds.add(candidate.evidence().id());
      addSubject(
          testedSubjects,
          ambiguous ? RELATION_STATUS_AMBIGUOUS : RELATION_STATUS_INFERRED,
          relationType,
          candidate.className(),
          null,
          null,
          SUPPORT_TYPE_INFERRED,
          ambiguous ? LOW_CONFIDENCE : MEDIUM_CONFIDENCE,
          ambiguous ? AMBIGUOUS_SUBJECT_NAME : null,
          relationEvidenceIds);
    }
  }

  private void addStatusOnlySubject(
      Map<TestedSubjectKey, TestedSubjectAccumulator> testedSubjects,
      String relationStatus,
      String relationType,
      String candidateReference,
      String confidence,
      String uncertainty,
      List<String> evidenceIds) {
    addSubject(
        testedSubjects,
        relationStatus,
        relationType,
        null,
        null,
        candidateReference,
        null,
        confidence,
        uncertainty,
        evidenceIds);
  }

  private void addSubject(
      Map<TestedSubjectKey, TestedSubjectAccumulator> testedSubjects,
      String relationStatus,
      String relationType,
      String className,
      String targetModuleId,
      String candidateReference,
      String supportType,
      String confidence,
      String uncertainty,
      List<String> evidenceIds) {
    TestedSubjectKey key = new TestedSubjectKey(
        relationStatus,
        relationType,
        className,
        targetModuleId,
        candidateReference,
        supportType,
        confidence,
        uncertainty);
    testedSubjects
        .computeIfAbsent(
            key,
            ignored -> new TestedSubjectAccumulator(
                relationStatus,
                relationType,
                className,
                targetModuleId,
                candidateReference,
                supportType,
                confidence,
                uncertainty))
        .addEvidenceIds(evidenceIds);
  }

  private TestInventoryEvidence fieldTypeEvidence(
      String sourcePath,
      String className,
      VariableDeclarator variable,
      List<String> sourceLines) {
    Type type = variable.getType();
    Integer lineStart = type.getRange()
        .or(() -> variable.getRange())
        .map(range -> range.begin.line)
        .orElse(null);
    Integer lineEnd = type.getRange()
        .or(() -> variable.getRange())
        .map(range -> range.end.line)
        .orElse(null);
    String symbolName = type.toString();
    return new TestInventoryEvidence(
        evidenceId(
            sourcePath,
            className + ":field:" + variable.getNameAsString(),
            "type:" + symbolName,
            lineStart,
            lineEnd),
        CODE_SYMBOL_SOURCE_TYPE,
        sourcePath,
        className,
        null,
        symbolName,
        lineStart,
        lineEnd,
        singleLineExcerpt(variable, sourceLines, lineStart),
        HIGH_CONFIDENCE);
  }

  private Optional<String> subjectSimpleName(String testSimpleName) {
    for (String suffix : TEST_SUFFIXES) {
      if (testSimpleName.endsWith(suffix) && testSimpleName.length() > suffix.length()) {
        return Optional.of(testSimpleName.substring(0, testSimpleName.length() - suffix.length()));
      }
    }
    return Optional.empty();
  }

  private ProductionClassIndex productionClassIndex(
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      List<Path> productionSourceRoots) throws IOException {
    Map<String, List<ProductionClass>> classesBySimpleName = new LinkedHashMap<>();
    Map<String, List<ProductionClass>> classesByClassName = new LinkedHashMap<>();
    for (Path sourceRoot : existingRoots(
        repositoryRoot,
        canonicalRepositoryRoot,
        productionSourceRoots)) {
      for (Path javaFile : javaFiles(canonicalRepositoryRoot, sourceRoot)) {
        for (ProductionClass productionClass : productionClasses(repositoryRoot, javaFile)) {
          classesBySimpleName
              .computeIfAbsent(productionClass.simpleName(), ignored -> new ArrayList<>())
              .add(productionClass);
          classesByClassName
              .computeIfAbsent(productionClass.className(), ignored -> new ArrayList<>())
              .add(productionClass);
        }
      }
    }

    classesBySimpleName.replaceAll((ignored, productionClasses) -> productionClasses.stream()
        .sorted(Comparator.comparing(ProductionClass::className))
        .toList());
    classesByClassName.replaceAll((ignored, productionClasses) -> productionClasses.stream()
        .sorted(Comparator.comparing(ProductionClass::className))
        .toList());
    return new ProductionClassIndex(classesBySimpleName, classesByClassName);
  }

  private Set<String> sourceDeclaredTypeNames(
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      List<Path> productionSourceRoots,
      List<Path> testSourceRoots) throws IOException {
    Set<String> declaredTypeNames = new LinkedHashSet<>();
    List<Path> sourceRoots = new ArrayList<>();
    sourceRoots.addAll(existingRoots(repositoryRoot, canonicalRepositoryRoot, productionSourceRoots));
    sourceRoots.addAll(existingRoots(repositoryRoot, canonicalRepositoryRoot, testSourceRoots));
    for (Path sourceRoot : sourceRoots.stream()
        .distinct()
        .sorted(Comparator.comparing(path -> path.toAbsolutePath().normalize().toString()))
        .toList()) {
      for (Path javaFile : javaFiles(canonicalRepositoryRoot, sourceRoot)) {
        CompilationUnit compilationUnit = JavaSourceParser.parse(javaFile);
        String packageName = compilationUnit.getPackageDeclaration()
            .map(packageDeclaration -> packageDeclaration.getName().asString())
            .orElse("");
        declaredTypeNames.addAll(JavaSourceOrigins.declaredTypeNames(compilationUnit, packageName));
      }
    }
    return declaredTypeNames;
  }

  private List<ProductionClass> productionClasses(Path repositoryRoot, Path javaFile) throws IOException {
    CompilationUnit compilationUnit = JavaSourceParser.parse(javaFile);
    String packageName = compilationUnit.getPackageDeclaration()
        .map(packageDeclaration -> packageDeclaration.getName().asString())
        .orElse("");
    String sourcePath = repositoryRelativePath(repositoryRoot, javaFile);
    List<String> sourceLines = Files.readAllLines(javaFile, StandardCharsets.UTF_8);
    List<ProductionClass> productionClasses = new ArrayList<>();

    for (ClassOrInterfaceDeclaration type : compilationUnit.findAll(ClassOrInterfaceDeclaration.class)) {
      if (type.isInterface()) {
        continue;
      }

      String className = qualifiedClassName(packageName, type);
      Integer lineStart = classDeclarationLine(type);
      Integer lineEnd = lineStart;
      TestInventoryEvidence evidence = new TestInventoryEvidence(
          evidenceId(sourcePath, className, "code_symbol", lineStart, lineEnd),
          CODE_SYMBOL_SOURCE_TYPE,
          sourcePath,
          className,
          null,
          className,
          lineStart,
          lineEnd,
          singleLineExcerpt(type, sourceLines, lineStart),
          HIGH_CONFIDENCE);
      productionClasses.add(new ProductionClass(type.getNameAsString(), className, evidence));
    }

    return productionClasses;
  }

  private List<Path> existingRoots(
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      List<Path> sourceRoots) {
    return sourceRoots.stream()
        .map(sourceRoot -> normalizeSourceRoot(repositoryRoot, sourceRoot))
        .filter(sourceRoot -> ScanPathContainment.isDirectoryUnderRoot(
            canonicalRepositoryRoot,
            sourceRoot))
        .sorted(Comparator.comparing(path -> path.toAbsolutePath().normalize().toString()))
        .toList();
  }

  private Path normalizeSourceRoot(Path repositoryRoot, Path sourceRoot) {
    Objects.requireNonNull(sourceRoot, "sourceRoot");
    if (sourceRoot.isAbsolute()) {
      return sourceRoot.toAbsolutePath().normalize();
    }
    return repositoryRoot.resolve(sourceRoot).normalize();
  }

  private List<Path> javaFiles(Path canonicalRepositoryRoot, Path sourceRoot) throws IOException {
    try (Stream<Path> paths = Files.walk(sourceRoot)) {
      return paths
          .filter(path -> ScanPathContainment.isRegularFileUnderRoot(canonicalRepositoryRoot, path)
              && path.getFileName().toString().endsWith(".java"))
          .sorted(Comparator.comparing(path -> path.toAbsolutePath().normalize().toString()))
          .toList();
    }
  }

  private String repositoryRelativePath(Path repositoryRoot, Path javaFile) {
    Path relativePath = repositoryRoot.relativize(javaFile.toAbsolutePath().normalize());
    return relativePath.toString().replace(javaFile.getFileSystem().getSeparator(), "/");
  }

  private String qualifiedClassName(String packageName, ClassOrInterfaceDeclaration type) {
    return type.getFullyQualifiedName()
        .orElseGet(() -> packageName.isBlank()
            ? type.getNameAsString()
            : packageName + "." + type.getNameAsString());
  }

  private boolean isNestedClass(ClassOrInterfaceDeclaration type) {
    Optional<Node> parent = type.getParentNode();
    while (parent.isPresent()) {
      Node parentNode = parent.orElseThrow();
      if (parentNode instanceof ClassOrInterfaceDeclaration) {
        return true;
      }
      parent = parentNode.getParentNode();
    }
    return false;
  }

  private String simpleAnnotationName(AnnotationExpr annotation) {
    return simpleName(annotation.getNameAsString());
  }

  private String simpleName(String name) {
    int lastDot = name.lastIndexOf('.');
    if (lastDot >= 0) {
      return name.substring(lastDot + 1);
    }
    return name;
  }

  private Map<String, String> singleTypeImportNames(Map<String, ImportDeclaration> importsBySimpleName) {
    Map<String, String> imports = new LinkedHashMap<>();
    for (Map.Entry<String, ImportDeclaration> entry : importsBySimpleName.entrySet()) {
      ImportDeclaration importDeclaration = entry.getValue();
      if (!importDeclaration.isStatic() && !importDeclaration.isAsterisk()) {
        imports.putIfAbsent(entry.getKey(), importDeclaration.getNameAsString());
      }
    }
    return imports;
  }

  private String evidenceId(
      String sourcePath,
      String owner,
      String symbol,
      Integer lineStart,
      Integer lineEnd) {
    String lineRange = lineStart == null || lineEnd == null ? "unknown" : lineStart + "-" + lineEnd;
    return "ev:" + sourcePath + ":" + lineRange + ":" + owner + ":" + symbol;
  }

  private String excerpt(com.github.javaparser.ast.Node node, List<String> sourceLines) {
    return EvidenceExcerpts.sourceRange(node, sourceLines);
  }

  private String singleLineExcerpt(
      com.github.javaparser.ast.Node node,
      List<String> sourceLines,
      Integer preferredLine) {
    return EvidenceExcerpts.singleLine(node, sourceLines, preferredLine);
  }

  private Integer classDeclarationLine(ClassOrInterfaceDeclaration type) {
    return type.getName().getRange()
        .map(range -> range.begin.line)
        .orElseGet(() -> type.getRange().map(range -> range.begin.line).orElse(null));
  }

  private record AnnotationLocation(AnnotationExpr annotation, String methodName) {
  }

  private record FrameworkEvidence(String frameworkName, TestInventoryEvidence evidence) {
  }

  private record SubjectReference(SubjectReferenceStatus status, String referenceName) {
  }

  private enum SubjectReferenceStatus {
    SUPPORTED,
    UNSUPPORTED,
    NOT_CANDIDATE
  }

  private record TestedSubjectKey(
      String relationStatus,
      String relationType,
      String className,
      String targetModuleId,
      String candidateReference,
      String supportType,
      String confidence,
      String uncertainty) {
  }

  private static final class TestedSubjectAccumulator {
    private final String relationStatus;
    private final String relationType;
    private final String className;
    private final String targetModuleId;
    private final String candidateReference;
    private final String supportType;
    private final String confidence;
    private final String uncertainty;
    private final LinkedHashSet<String> evidenceIds = new LinkedHashSet<>();

    private TestedSubjectAccumulator(
        String relationStatus,
        String relationType,
        String className,
        String targetModuleId,
        String candidateReference,
        String supportType,
        String confidence,
        String uncertainty) {
      this.relationStatus = relationStatus;
      this.relationType = relationType;
      this.className = className;
      this.targetModuleId = targetModuleId;
      this.candidateReference = candidateReference;
      this.supportType = supportType;
      this.confidence = confidence;
      this.uncertainty = uncertainty;
    }

    private void addEvidenceIds(List<String> evidenceIds) {
      this.evidenceIds.addAll(evidenceIds);
    }

    private TestedSubjectFact toFact() {
      return new TestedSubjectFact(
          relationStatus,
          relationType,
          className,
          targetModuleId,
          candidateReference,
          supportType,
          confidence,
          uncertainty,
          List.copyOf(evidenceIds));
    }
  }

  private record ProductionClassIndex(
      Map<String, List<ProductionClass>> bySimpleName,
      Map<String, List<ProductionClass>> byClassName) {
  }

  private record ProductionClass(
      String simpleName,
      String className,
      TestInventoryEvidence evidence) {
  }
}
