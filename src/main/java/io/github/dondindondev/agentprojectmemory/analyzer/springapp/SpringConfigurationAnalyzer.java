package io.github.dondindondev.agentprojectmemory.analyzer.springapp;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import io.github.dondindondev.agentprojectmemory.analyzer.EvidenceExcerpts;
import io.github.dondindondev.agentprojectmemory.analyzer.JavaSourceOrigins;
import io.github.dondindondev.agentprojectmemory.analyzer.JavaSourceParser;
import io.github.dondindondev.agentprojectmemory.analyzer.ScanPathContainment;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public final class SpringConfigurationAnalyzer {
  public static final String SURFACE_CATEGORY_CONFIGURATION_CLASS = "spring_configuration_class";
  public static final String SURFACE_CATEGORY_CONFIGURATION_PROPERTIES =
      "spring_configuration_properties_type";
  public static final String SURFACE_CATEGORY_BEAN_METHOD = "spring_bean_method";
  public static final String SUPPORT_TYPE_EXTRACTED = "extracted";
  public static final String DIRECT_CONFIGURATION_CLASS = "direct_configuration_class";
  public static final String DIRECT_CONFIGURATION_PROPERTIES_TYPE =
      "direct_configuration_properties_type";
  public static final String DIRECT_BEAN_METHOD = "direct_bean_method";
  public static final String BINDING_NOT_ANALYZED = "not_analyzed";
  public static final String BEAN_NAME_NOT_ANALYZED = "not_analyzed";

  private static final String HIGH_CONFIDENCE = "high";
  private static final String ANNOTATION_SOURCE_TYPE = "annotation";
  private static final String CONFIGURATION_ANNOTATION_SYMBOL = "@Configuration";
  private static final String CONFIGURATION_PROPERTIES_ANNOTATION_SYMBOL =
      "@ConfigurationProperties";
  private static final String BEAN_ANNOTATION_SYMBOL = "@Bean";
  private static final Map<String, Set<String>> SUPPORTED_ANNOTATION_ORIGINS = Map.of(
      "Bean",
      Set.of("org.springframework.context.annotation.Bean"),
      "Configuration",
      Set.of("org.springframework.context.annotation.Configuration"),
      "ConfigurationProperties",
      Set.of("org.springframework.boot.context.properties.ConfigurationProperties"));
  private static final Comparator<SpringConfigurationClassFact> CONFIGURATION_CLASS_ORDER = Comparator
      .comparing(SpringConfigurationClassFact::sourcePath)
      .thenComparing(SpringConfigurationClassFact::className)
      .thenComparing(SpringConfigurationClassFact::surfaceCategory);
  private static final Comparator<SpringConfigurationPropertiesFact> CONFIGURATION_PROPERTIES_ORDER = Comparator
      .comparing(SpringConfigurationPropertiesFact::sourcePath)
      .thenComparing(SpringConfigurationPropertiesFact::className)
      .thenComparing(SpringConfigurationPropertiesFact::surfaceCategory);
  private static final Comparator<SpringBeanMethodFact> BEAN_METHOD_ORDER = Comparator
      .comparing(SpringBeanMethodFact::sourcePath)
      .thenComparing(SpringBeanMethodFact::className)
      .thenComparing(SpringBeanMethodFact::methodName)
      .thenComparing(SpringBeanMethodFact::idDiscriminator);

  public SpringConfigurationAnalysis analyze(Path repositoryRoot, List<Path> sourceRoots)
      throws IOException {
    Objects.requireNonNull(repositoryRoot, "repositoryRoot");
    Objects.requireNonNull(sourceRoots, "sourceRoots");

    Path normalizedRepositoryRoot = repositoryRoot.toAbsolutePath().normalize();
    Path canonicalRepositoryRoot = ScanPathContainment.canonicalRoot(normalizedRepositoryRoot);
    List<ConfigurationSourceFile> sourceFiles = new ArrayList<>();
    Set<String> sourceDeclaredTypeNames = new LinkedHashSet<>();

    for (Path sourceRoot : sourceRoots) {
      Path normalizedSourceRoot = normalizeSourceRoot(normalizedRepositoryRoot, sourceRoot);
      if (!ScanPathContainment.isDirectoryUnderRoot(canonicalRepositoryRoot, normalizedSourceRoot)) {
        continue;
      }
      for (Path javaFile : javaFiles(canonicalRepositoryRoot, normalizedSourceRoot)) {
        ConfigurationSourceFile sourceFile = sourceFile(normalizedRepositoryRoot, javaFile);
        sourceFiles.add(sourceFile);
        sourceDeclaredTypeNames.addAll(sourceFile.declaredTypeNames());
      }
    }

    List<SpringConfigurationClassFact> configurationClasses = new ArrayList<>();
    List<SpringConfigurationPropertiesFact> configurationProperties = new ArrayList<>();
    List<SpringBeanMethodFact> beanMethods = new ArrayList<>();
    Map<String, SpringConfigurationEvidence> evidence = new java.util.LinkedHashMap<>();
    for (ConfigurationSourceFile sourceFile : sourceFiles) {
      analyzeJavaFile(
          sourceFile,
          sourceDeclaredTypeNames,
          configurationClasses,
          configurationProperties,
          beanMethods,
          evidence);
    }

    return new SpringConfigurationAnalysis(
        configurationClasses.stream().sorted(CONFIGURATION_CLASS_ORDER).toList(),
        configurationProperties.stream().sorted(CONFIGURATION_PROPERTIES_ORDER).toList(),
        beanMethods.stream().sorted(BEAN_METHOD_ORDER).toList(),
        List.copyOf(evidence.values()));
  }

  private void analyzeJavaFile(
      ConfigurationSourceFile sourceFile,
      Set<String> sourceDeclaredTypeNames,
      List<SpringConfigurationClassFact> configurationClasses,
      List<SpringConfigurationPropertiesFact> configurationProperties,
      List<SpringBeanMethodFact> beanMethods,
      Map<String, SpringConfigurationEvidence> evidence) {
    for (TypeDeclaration<?> type : sourceFile.compilationUnit().findAll(TypeDeclaration.class)) {
      String className = qualifiedClassName(sourceFile.packageName(), type);
      if (type instanceof ClassOrInterfaceDeclaration classOrInterface) {
        configurationAnnotation(
                classOrInterface.getAnnotations(),
                sourceFile.importsBySimpleName(),
                sourceDeclaredTypeNames)
            .ifPresent(annotation -> addConfigurationClassFact(
                sourceFile,
                className,
                annotation,
                configurationClasses,
                evidence));
        addBeanMethodFacts(
            sourceFile,
            classOrInterface,
            className,
            sourceDeclaredTypeNames,
            beanMethods,
            evidence);
      }
      configurationPropertiesAnnotation(
              type.getAnnotations(),
              sourceFile.importsBySimpleName(),
              sourceDeclaredTypeNames)
          .ifPresent(annotation -> addConfigurationPropertiesFact(
              sourceFile,
              className,
              annotation,
              configurationProperties,
              evidence));
    }
  }

  private void addConfigurationClassFact(
      ConfigurationSourceFile sourceFile,
      String className,
      AnnotationExpr annotation,
      List<SpringConfigurationClassFact> configurationClasses,
      Map<String, SpringConfigurationEvidence> evidence) {
    SpringConfigurationEvidence annotationEvidence = annotationEvidence(
        sourceFile.sourcePath(),
        className,
        null,
        CONFIGURATION_ANNOTATION_SYMBOL,
        annotation,
        sourceFile.sourceLines());
    evidence.putIfAbsent(annotationEvidence.id(), annotationEvidence);

    configurationClasses.add(new SpringConfigurationClassFact(
        SURFACE_CATEGORY_CONFIGURATION_CLASS,
        SUPPORT_TYPE_EXTRACTED,
        className,
        sourceFile.sourcePath(),
        DIRECT_CONFIGURATION_CLASS,
        List.of(annotationEvidence.id())));
  }

  private void addConfigurationPropertiesFact(
      ConfigurationSourceFile sourceFile,
      String className,
      AnnotationExpr annotation,
      List<SpringConfigurationPropertiesFact> configurationProperties,
      Map<String, SpringConfigurationEvidence> evidence) {
    SpringConfigurationEvidence annotationEvidence = annotationEvidence(
        sourceFile.sourcePath(),
        className,
        null,
        CONFIGURATION_PROPERTIES_ANNOTATION_SYMBOL,
        annotation,
        sourceFile.sourceLines());
    evidence.putIfAbsent(annotationEvidence.id(), annotationEvidence);

    configurationProperties.add(new SpringConfigurationPropertiesFact(
        SURFACE_CATEGORY_CONFIGURATION_PROPERTIES,
        SUPPORT_TYPE_EXTRACTED,
        className,
        sourceFile.sourcePath(),
        DIRECT_CONFIGURATION_PROPERTIES_TYPE,
        BINDING_NOT_ANALYZED,
        List.of(annotationEvidence.id())));
  }

  private void addBeanMethodFacts(
      ConfigurationSourceFile sourceFile,
      ClassOrInterfaceDeclaration type,
      String className,
      Set<String> sourceDeclaredTypeNames,
      List<SpringBeanMethodFact> beanMethods,
      Map<String, SpringConfigurationEvidence> evidence) {
    int beanMethodIndex = 0;
    for (MethodDeclaration method : type.getMethods()) {
      Optional<AnnotationExpr> beanAnnotation = beanAnnotation(
          method.getAnnotations(),
          sourceFile.importsBySimpleName(),
          sourceDeclaredTypeNames);
      if (beanAnnotation.isEmpty()) {
        continue;
      }
      beanMethodIndex++;
      SpringConfigurationEvidence annotationEvidence = annotationEvidence(
          sourceFile.sourcePath(),
          className,
          method.getNameAsString(),
          BEAN_ANNOTATION_SYMBOL,
          beanAnnotation.orElseThrow(),
          sourceFile.sourceLines());
      evidence.putIfAbsent(annotationEvidence.id(), annotationEvidence);

      beanMethods.add(new SpringBeanMethodFact(
          SURFACE_CATEGORY_BEAN_METHOD,
          SUPPORT_TYPE_EXTRACTED,
          className,
          method.getNameAsString(),
          sourceFile.sourcePath(),
          DIRECT_BEAN_METHOD,
          BEAN_NAME_NOT_ANALYZED,
          "decl:" + "%06d".formatted(beanMethodIndex),
          List.of(annotationEvidence.id())));
    }
  }

  private Optional<AnnotationExpr> configurationAnnotation(
      List<AnnotationExpr> annotations,
      Map<String, String> importsBySimpleName,
      Set<String> sourceDeclaredTypeNames) {
    return annotation(annotations, importsBySimpleName, sourceDeclaredTypeNames, "Configuration");
  }

  private Optional<AnnotationExpr> configurationPropertiesAnnotation(
      List<AnnotationExpr> annotations,
      Map<String, String> importsBySimpleName,
      Set<String> sourceDeclaredTypeNames) {
    return annotation(annotations, importsBySimpleName, sourceDeclaredTypeNames, "ConfigurationProperties");
  }

  private Optional<AnnotationExpr> beanAnnotation(
      List<AnnotationExpr> annotations,
      Map<String, String> importsBySimpleName,
      Set<String> sourceDeclaredTypeNames) {
    return annotation(annotations, importsBySimpleName, sourceDeclaredTypeNames, "Bean");
  }

  private Optional<AnnotationExpr> annotation(
      List<AnnotationExpr> annotations,
      Map<String, String> importsBySimpleName,
      Set<String> sourceDeclaredTypeNames,
      String simpleName) {
    return annotations.stream()
        .filter(candidate -> simpleName.equals(JavaSourceOrigins.supportedAnnotationSimpleName(
                candidate,
                importsBySimpleName,
                SUPPORTED_ANNOTATION_ORIGINS,
                sourceDeclaredTypeNames)
            .orElse(null)))
        .findFirst();
  }

  private ConfigurationSourceFile sourceFile(
      Path repositoryRoot,
      Path javaFile) throws IOException {
    CompilationUnit compilationUnit = JavaSourceParser.parse(javaFile);
    String packageName = compilationUnit.getPackageDeclaration()
        .map(packageDeclaration -> packageDeclaration.getName().asString())
        .orElse("");
    return new ConfigurationSourceFile(
        compilationUnit,
        packageName,
        repositoryRelativePath(repositoryRoot, javaFile),
        Files.readAllLines(javaFile),
        JavaSourceOrigins.singleTypeImportsBySimpleName(compilationUnit),
        JavaSourceOrigins.declaredTypeNames(compilationUnit, packageName));
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

  private SpringConfigurationEvidence annotationEvidence(
      String sourcePath,
      String className,
      String methodName,
      String annotationSymbol,
      AnnotationExpr annotation,
      List<String> sourceLines) {
    Integer lineStart = annotation.getRange().map(range -> range.begin.line).orElse(null);
    Integer lineEnd = annotation.getRange().map(range -> range.end.line).orElse(null);
    return new SpringConfigurationEvidence(
        evidenceId(sourcePath, className, methodName, annotationSymbol, lineStart, lineEnd),
        ANNOTATION_SOURCE_TYPE,
        sourcePath,
        className,
        methodName,
        annotationSymbol,
        lineStart,
        lineEnd,
        EvidenceExcerpts.sourceRange(annotation, sourceLines),
        HIGH_CONFIDENCE);
  }

  private String evidenceId(
      String sourcePath,
      String className,
      String methodName,
      String annotationSymbol,
      Integer lineStart,
      Integer lineEnd) {
    String lineRange = lineStart == null || lineEnd == null ? "unknown" : lineStart + "-" + lineEnd;
    String symbolOwner = methodName == null ? className : className + "#" + methodName;
    return "ev:" + sourcePath + ":" + lineRange + ":" + symbolOwner + ":" + annotationSymbol;
  }

  private String repositoryRelativePath(Path repositoryRoot, Path javaFile) {
    Path relativePath = repositoryRoot.relativize(javaFile.toAbsolutePath().normalize());
    return relativePath.toString().replace(javaFile.getFileSystem().getSeparator(), "/");
  }

  private String qualifiedClassName(String packageName, TypeDeclaration<?> type) {
    return type.getFullyQualifiedName()
        .orElseGet(() -> packageName.isBlank()
            ? type.getNameAsString()
            : packageName + "." + type.getNameAsString());
  }

  private record ConfigurationSourceFile(
      CompilationUnit compilationUnit,
      String packageName,
      String sourcePath,
      List<String> sourceLines,
      Map<String, String> importsBySimpleName,
      Set<String> declaredTypeNames) {
    private ConfigurationSourceFile {
      sourceLines = List.copyOf(sourceLines);
      importsBySimpleName = Map.copyOf(importsBySimpleName);
      declaredTypeNames = Set.copyOf(declaredTypeNames);
    }
  }
}
