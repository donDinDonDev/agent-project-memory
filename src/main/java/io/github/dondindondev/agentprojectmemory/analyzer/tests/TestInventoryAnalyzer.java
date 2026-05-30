package io.github.dondindondev.agentprojectmemory.analyzer.tests;

import com.github.javaparser.Range;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import io.github.dondindondev.agentprojectmemory.analyzer.JavaSourceParser;
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
  private static final String AMBIGUOUS_SUBJECT_NAME = "ambiguous_subject_name";
  private static final List<String> TEST_SUFFIXES = List.of("Test", "Tests", "IT");
  private static final Set<String> JUNIT_TEST_METHOD_ANNOTATIONS = Set.of(
      "Test",
      "ParameterizedTest",
      "RepeatedTest",
      "TestFactory",
      "TestTemplate");
  private static final Set<String> SPRING_TEST_CLASS_MARKER_ANNOTATIONS = Set.of(
      "SpringBootTest",
      "WebMvcTest",
      "DataJpaTest",
      "ContextConfiguration");
  private static final Set<String> SPRING_TEST_ANNOTATIONS = Set.of(
      "SpringBootTest",
      "WebMvcTest",
      "DataJpaTest",
      "ContextConfiguration",
      "ActiveProfiles",
      "DirtiesContext",
      "Sql",
      "MockBean",
      "TestConfiguration");
  private static final Comparator<TestClassFact> TEST_CLASS_ORDER = Comparator
      .comparing(TestClassFact::className)
      .thenComparing(TestClassFact::sourcePath);
  private static final Comparator<TestFrameworkSignalFact> FRAMEWORK_SIGNAL_ORDER = Comparator
      .comparing(TestFrameworkSignalFact::name);
  private static final Comparator<TestedSubjectFact> TESTED_SUBJECT_ORDER = Comparator
      .comparing(TestedSubjectFact::className)
      .thenComparing(TestedSubjectFact::supportType)
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
    List<Path> existingTestRoots = existingRoots(normalizedRepositoryRoot, testSourceRoots);
    if (existingTestRoots.isEmpty()) {
      return new TestInventoryAnalysis(NOT_DETECTED, List.of(), List.of());
    }

    Map<String, List<ProductionClass>> productionClassesBySimpleName = productionClassesBySimpleName(
        normalizedRepositoryRoot,
        productionSourceRoots);
    List<TestClassFact> tests = new ArrayList<>();
    Map<String, TestInventoryEvidence> evidence = new LinkedHashMap<>();

    for (Path testRoot : existingTestRoots) {
      for (Path javaFile : javaFiles(testRoot)) {
        analyzeTestFile(
            normalizedRepositoryRoot,
            javaFile,
            productionClassesBySimpleName,
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
      Map<String, List<ProductionClass>> productionClassesBySimpleName,
      List<TestClassFact> tests,
      Map<String, TestInventoryEvidence> evidence) throws IOException {
    CompilationUnit compilationUnit = JavaSourceParser.parse(javaFile);
    String packageName = compilationUnit.getPackageDeclaration()
        .map(packageDeclaration -> packageDeclaration.getName().asString())
        .orElse("");
    String sourcePath = repositoryRelativePath(repositoryRoot, javaFile);
    List<String> sourceLines = Files.readAllLines(javaFile, StandardCharsets.UTF_8);
    Map<String, ImportDeclaration> importsBySimpleName = importsBySimpleName(compilationUnit);

    for (ClassOrInterfaceDeclaration type : compilationUnit.findAll(ClassOrInterfaceDeclaration.class)) {
      if (type.isInterface()) {
        continue;
      }

      if (!shouldEmitTestClass(type, importsBySimpleName)) {
        continue;
      }

      String className = qualifiedClassName(packageName, type);
      TestInventoryEvidence classEvidence = testClassEvidence(sourcePath, className, type, sourceLines);
      evidence.putIfAbsent(classEvidence.id(), classEvidence);

      List<FrameworkEvidence> frameworkEvidence = frameworkEvidence(
          sourcePath,
          className,
          type,
          !isNestedClass(type),
          importsBySimpleName,
          sourceLines);
      frameworkEvidence.forEach(record -> evidence.putIfAbsent(record.evidence().id(), record.evidence()));

      List<TestedSubjectFact> testedSubjects = testedSubjects(
          type.getNameAsString(),
          classEvidence,
          productionClassesBySimpleName,
          evidence);

      tests.add(new TestClassFact(
          className,
          sourcePath,
          frameworkSignals(frameworkEvidence),
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
      List<String> sourceLines) {
    List<FrameworkEvidence> evidence = new ArrayList<>();
    Set<String> seen = new LinkedHashSet<>();

    if (includeImportEvidence) {
      for (ImportDeclaration importDeclaration : importsBySimpleName.values()) {
        frameworkName(importDeclaration.getNameAsString(), null)
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
          resolvedAnnotationName.orElseGet(() -> simpleAnnotationName(annotation)),
          simpleAnnotationName(annotation));
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
    if (importDeclaration == null || importDeclaration.isAsterisk()) {
      return Optional.empty();
    }
    return Optional.of(importDeclaration.getNameAsString());
  }

  private Optional<String> frameworkName(String resolvedName, String simpleAnnotationName) {
    if (resolvedName.startsWith("org.junit.jupiter.")) {
      return Optional.of("JUnit Jupiter");
    }
    if (resolvedName.startsWith("org.junit.") && !resolvedName.startsWith("org.junit.jupiter.")) {
      return Optional.of("JUnit 4");
    }
    if (resolvedName.startsWith("org.springframework.test.")
        || resolvedName.startsWith("org.springframework.boot.test.")) {
      return Optional.of("Spring Test");
    }
    if (simpleAnnotationName != null && SPRING_TEST_ANNOTATIONS.contains(simpleAnnotationName)) {
      return Optional.of("Spring Test");
    }
    return Optional.empty();
  }

  private boolean shouldEmitTestClass(
      ClassOrInterfaceDeclaration type,
      Map<String, ImportDeclaration> importsBySimpleName) {
    return hasClearTestName(type.getNameAsString())
        || hasDirectTestClassSignal(type, importsBySimpleName);
  }

  private boolean hasClearTestName(String simpleName) {
    return subjectSimpleName(simpleName).isPresent();
  }

  private boolean hasDirectTestClassSignal(
      ClassOrInterfaceDeclaration type,
      Map<String, ImportDeclaration> importsBySimpleName) {
    for (AnnotationLocation annotationLocation : annotationLocations(type)) {
      if (isTestClassMarker(annotationLocation, importsBySimpleName)) {
        return true;
      }
    }
    return false;
  }

  private boolean isTestClassMarker(
      AnnotationLocation annotationLocation,
      Map<String, ImportDeclaration> importsBySimpleName) {
    AnnotationExpr annotation = annotationLocation.annotation();
    Optional<String> resolvedName = resolvedAnnotationName(annotation, importsBySimpleName);
    String simpleName = simpleAnnotationName(annotation);
    if (annotationLocation.methodName() != null) {
      return isJUnitTestMethodAnnotation(resolvedName, simpleName);
    }

    return isJUnitNestedAnnotation(resolvedName, simpleName)
        || isJUnitFourRunWithAnnotation(resolvedName, simpleName)
        || isSpringTestClassMarkerAnnotation(resolvedName, simpleName);
  }

  private boolean isJUnitTestMethodAnnotation(Optional<String> resolvedName, String simpleName) {
    return JUNIT_TEST_METHOD_ANNOTATIONS.contains(simpleName)
        && resolvedName.map(name -> name.startsWith("org.junit.")).orElse(false);
  }

  private boolean isJUnitNestedAnnotation(Optional<String> resolvedName, String simpleName) {
    return "Nested".equals(simpleName)
        && resolvedName.map(name -> name.startsWith("org.junit.jupiter.")).orElse(false);
  }

  private boolean isJUnitFourRunWithAnnotation(Optional<String> resolvedName, String simpleName) {
    return "RunWith".equals(simpleName)
        && resolvedName.map(name -> name.startsWith("org.junit.")).orElse(false);
  }

  private boolean isSpringTestClassMarkerAnnotation(Optional<String> resolvedName, String simpleName) {
    return SPRING_TEST_CLASS_MARKER_ANNOTATIONS.contains(simpleName)
        && resolvedName.map(name -> name.startsWith("org.springframework.test.")
            || name.startsWith("org.springframework.boot.test.")).orElse(false);
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
    String annotationSymbol = "@" + simpleAnnotationName(annotation);
    Integer lineStart = annotation.getRange().map(range -> range.begin.line).orElse(null);
    Integer lineEnd = annotation.getRange().map(range -> range.end.line).orElse(null);
    String owner = methodName == null ? className : className + "#" + methodName;

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
            entry.getValue().stream().sorted().toList()))
        .sorted(FRAMEWORK_SIGNAL_ORDER)
        .toList();
  }

  private List<TestedSubjectFact> testedSubjects(
      String testSimpleName,
      TestInventoryEvidence testClassEvidence,
      Map<String, List<ProductionClass>> productionClassesBySimpleName,
      Map<String, TestInventoryEvidence> evidence) {
    Optional<String> subjectSimpleName = subjectSimpleName(testSimpleName);
    if (subjectSimpleName.isEmpty()) {
      return List.of();
    }

    List<ProductionClass> candidates = productionClassesBySimpleName.getOrDefault(
        subjectSimpleName.orElseThrow(),
        List.of());
    if (candidates.isEmpty()) {
      return List.of();
    }

    boolean ambiguous = candidates.size() > 1;
    List<TestedSubjectFact> testedSubjects = new ArrayList<>();
    for (ProductionClass candidate : candidates) {
      evidence.putIfAbsent(candidate.evidence().id(), candidate.evidence());
      testedSubjects.add(new TestedSubjectFact(
          candidate.className(),
          SUPPORT_TYPE_INFERRED,
          ambiguous ? LOW_CONFIDENCE : MEDIUM_CONFIDENCE,
          ambiguous ? AMBIGUOUS_SUBJECT_NAME : null,
          List.of(testClassEvidence.id(), candidate.evidence().id())));
    }
    return testedSubjects.stream().sorted(TESTED_SUBJECT_ORDER).toList();
  }

  private Optional<String> subjectSimpleName(String testSimpleName) {
    for (String suffix : TEST_SUFFIXES) {
      if (testSimpleName.endsWith(suffix) && testSimpleName.length() > suffix.length()) {
        return Optional.of(testSimpleName.substring(0, testSimpleName.length() - suffix.length()));
      }
    }
    return Optional.empty();
  }

  private Map<String, List<ProductionClass>> productionClassesBySimpleName(
      Path repositoryRoot,
      List<Path> productionSourceRoots) throws IOException {
    Map<String, List<ProductionClass>> classesBySimpleName = new LinkedHashMap<>();
    for (Path sourceRoot : existingRoots(repositoryRoot, productionSourceRoots)) {
      for (Path javaFile : javaFiles(sourceRoot)) {
        for (ProductionClass productionClass : productionClasses(repositoryRoot, javaFile)) {
          classesBySimpleName
              .computeIfAbsent(productionClass.simpleName(), ignored -> new ArrayList<>())
              .add(productionClass);
        }
      }
    }

    classesBySimpleName.replaceAll((ignored, productionClasses) -> productionClasses.stream()
        .sorted(Comparator.comparing(ProductionClass::className))
        .toList());
    return classesBySimpleName;
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

  private List<Path> existingRoots(Path repositoryRoot, List<Path> sourceRoots) {
    return sourceRoots.stream()
        .map(sourceRoot -> normalizeSourceRoot(repositoryRoot, sourceRoot))
        .filter(Files::isDirectory)
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

  private List<Path> javaFiles(Path sourceRoot) throws IOException {
    try (Stream<Path> paths = Files.walk(sourceRoot)) {
      return paths
          .filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".java"))
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
    Optional<Range> range = node.getRange();
    if (range.isEmpty()) {
      return node.toString();
    }

    int start = range.orElseThrow().begin.line;
    int end = range.orElseThrow().end.line;
    if (start < 1 || end < start || end > sourceLines.size()) {
      return node.toString();
    }

    return String.join("\n", sourceLines.subList(start - 1, end)).trim();
  }

  private String singleLineExcerpt(
      com.github.javaparser.ast.Node node,
      List<String> sourceLines,
      Integer preferredLine) {
    if (preferredLine != null && preferredLine >= 1 && preferredLine <= sourceLines.size()) {
      return sourceLines.get(preferredLine - 1).trim();
    }

    Optional<Range> range = node.getRange();
    if (range.isEmpty()) {
      return node.toString();
    }

    int line = range.orElseThrow().begin.line;
    if (line < 1 || line > sourceLines.size()) {
      return node.toString();
    }

    return sourceLines.get(line - 1).trim();
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

  private record ProductionClass(
      String simpleName,
      String className,
      TestInventoryEvidence evidence) {
  }
}
