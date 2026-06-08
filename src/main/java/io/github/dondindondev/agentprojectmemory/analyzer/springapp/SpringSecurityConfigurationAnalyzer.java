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
import io.github.dondindondev.agentprojectmemory.analyzer.warnings.AnalysisWarningAnalysis;
import io.github.dondindondev.agentprojectmemory.analyzer.warnings.AnalysisWarningEvidence;
import io.github.dondindondev.agentprojectmemory.analyzer.warnings.AnalysisWarningFact;
import java.io.IOException;
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

public final class SpringSecurityConfigurationAnalyzer {
  public static final String CATEGORY_SPRING_SECURITY = "spring_security";
  public static final String SIGNAL_SECURITY_CONFIGURATION_ANNOTATION =
      "security_configuration_annotation";
  public static final String SIGNAL_SECURITY_FILTER_CHAIN_BEAN = "security_filter_chain_bean";

  private static final String HIGH_CONFIDENCE = "high";
  private static final String ANNOTATION_SOURCE_TYPE = "annotation";
  private static final String CODE_SYMBOL_SOURCE_TYPE = "code_symbol";
  private static final String BEAN = "Bean";
  private static final String SECURITY_FILTER_CHAIN = "SecurityFilterChain";
  private static final String SECURITY_ANNOTATION_MESSAGE =
      "Spring Security configuration annotation detected as a source-visible inspection hint "
          + "and change-risk signal; the analyzer does not evaluate security policy, endpoint "
          + "protection, authentication, authorization, filter-chain order, vulnerability, or "
          + "correctness.";
  private static final String SECURITY_FILTER_CHAIN_BEAN_MESSAGE =
      "SecurityFilterChain @Bean method detected as a source-visible Spring Security "
          + "configuration inspection hint and change-risk signal; the analyzer does not "
          + "evaluate security policy, endpoint protection, authentication, authorization, "
          + "filter-chain order, vulnerability, or correctness.";
  private static final Map<String, Set<String>> SUPPORTED_ANNOTATION_ORIGINS =
      Map.ofEntries(
          Map.entry(
              "EnableWebSecurity",
              Set.of("org.springframework.security.config.annotation.web.configuration.EnableWebSecurity")),
          Map.entry(
              "EnableMethodSecurity",
              Set.of("org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity")),
          Map.entry(
              "EnableGlobalMethodSecurity",
              Set.of(
                  "org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity")),
          Map.entry(
              "EnableWebFluxSecurity",
              Set.of("org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity")),
          Map.entry(
              "EnableReactiveMethodSecurity",
              Set.of(
                  "org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity")),
          Map.entry(
              BEAN,
              Set.of("org.springframework.context.annotation.Bean")));
  private static final Map<String, Set<String>> SUPPORTED_TYPE_ORIGINS = Map.of(
      SECURITY_FILTER_CHAIN,
      Set.of("org.springframework.security.web.SecurityFilterChain"));
  private static final Comparator<AnalysisWarningFact> WARNING_ORDER = Comparator
      .comparing(AnalysisWarningFact::category)
      .thenComparing(AnalysisWarningFact::signal)
      .thenComparing(AnalysisWarningFact::sourcePath)
      .thenComparing(AnalysisWarningFact::id);

  public AnalysisWarningAnalysis analyze(
      Path repositoryRoot,
      List<Path> sourceRoots,
      String modulePathForIds) throws IOException {
    Objects.requireNonNull(repositoryRoot, "repositoryRoot");
    Objects.requireNonNull(sourceRoots, "sourceRoots");

    Path normalizedRepositoryRoot = repositoryRoot.toAbsolutePath().normalize();
    Path canonicalRepositoryRoot = ScanPathContainment.canonicalRoot(normalizedRepositoryRoot);
    List<SecuritySourceFile> sourceFiles = new ArrayList<>();
    Set<String> sourceDeclaredTypeNames = new LinkedHashSet<>();

    for (Path sourceRoot : sourceRoots) {
      Path normalizedSourceRoot = normalizeSourceRoot(normalizedRepositoryRoot, sourceRoot);
      if (!ScanPathContainment.isDirectoryUnderRoot(canonicalRepositoryRoot, normalizedSourceRoot)) {
        continue;
      }
      for (Path javaFile : javaFiles(canonicalRepositoryRoot, normalizedSourceRoot)) {
        SecuritySourceFile sourceFile = sourceFile(normalizedRepositoryRoot, javaFile);
        sourceFiles.add(sourceFile);
        sourceDeclaredTypeNames.addAll(sourceFile.declaredTypeNames());
      }
    }

    List<AnalysisWarningFact> warnings = new ArrayList<>();
    Map<String, AnalysisWarningEvidence> evidence = new LinkedHashMap<>();
    for (SecuritySourceFile sourceFile : sourceFiles) {
      analyzeJavaFile(sourceFile, sourceDeclaredTypeNames, modulePathForIds, warnings, evidence);
    }

    return new AnalysisWarningAnalysis(
        warnings.stream().sorted(WARNING_ORDER).toList(),
        List.copyOf(evidence.values()));
  }

  private void analyzeJavaFile(
      SecuritySourceFile sourceFile,
      Set<String> sourceDeclaredTypeNames,
      String modulePathForIds,
      List<AnalysisWarningFact> warnings,
      Map<String, AnalysisWarningEvidence> evidence) {
    for (TypeDeclaration<?> type : sourceFile.compilationUnit().findAll(TypeDeclaration.class)) {
      String className = qualifiedClassName(sourceFile.packageName(), type);
      addSecurityAnnotationWarnings(
          sourceFile,
          type,
          className,
          sourceDeclaredTypeNames,
          modulePathForIds,
          warnings,
          evidence);
      if (type instanceof ClassOrInterfaceDeclaration classOrInterface) {
        addSecurityFilterChainBeanWarnings(
            sourceFile,
            classOrInterface,
            className,
            sourceDeclaredTypeNames,
            modulePathForIds,
            warnings,
            evidence);
      }
    }
  }

  private void addSecurityAnnotationWarnings(
      SecuritySourceFile sourceFile,
      TypeDeclaration<?> type,
      String className,
      Set<String> sourceDeclaredTypeNames,
      String modulePathForIds,
      List<AnalysisWarningFact> warnings,
      Map<String, AnalysisWarningEvidence> evidence) {
    int annotationIndex = 0;
    for (AnnotationExpr annotation : type.getAnnotations()) {
      Optional<String> simpleName = supportedSecurityAnnotation(
          annotation,
          sourceFile.importsBySimpleName(),
          sourceDeclaredTypeNames);
      if (simpleName.isEmpty()) {
        continue;
      }
      annotationIndex++;
      String annotationSymbol = annotationSymbol(simpleName.orElseThrow());
      AnalysisWarningEvidence annotationEvidence = annotationEvidence(
          sourceFile.sourcePath(),
          className,
          null,
          annotationSymbol,
          annotation);
      evidence.putIfAbsent(annotationEvidence.id(), annotationEvidence);
      warnings.add(new AnalysisWarningFact(
          warningId(
              SIGNAL_SECURITY_CONFIGURATION_ANNOTATION,
              className
                  + ":annotation:"
                  + annotationDiscriminator(simpleName.orElseThrow())
                  + ":decl:"
                  + ordinal(annotationIndex),
              modulePathForIds),
          CATEGORY_SPRING_SECURITY,
          SIGNAL_SECURITY_CONFIGURATION_ANNOTATION,
          SECURITY_ANNOTATION_MESSAGE,
          sourceFile.sourcePath(),
          List.of(annotationEvidence.id())));
    }
  }

  private void addSecurityFilterChainBeanWarnings(
      SecuritySourceFile sourceFile,
      ClassOrInterfaceDeclaration type,
      String className,
      Set<String> sourceDeclaredTypeNames,
      String modulePathForIds,
      List<AnalysisWarningFact> warnings,
      Map<String, AnalysisWarningEvidence> evidence) {
    int filterChainBeanIndex = 0;
    for (MethodDeclaration method : type.getMethods()) {
      Optional<AnnotationExpr> beanAnnotation = beanAnnotation(
          method.getAnnotations(),
          sourceFile.importsBySimpleName(),
          sourceDeclaredTypeNames);
      Optional<AnalysisWarningEvidence> returnTypeEvidence = securityFilterChainReturnTypeEvidence(
          sourceFile,
          className,
          method,
          sourceDeclaredTypeNames);
      if (beanAnnotation.isEmpty() || returnTypeEvidence.isEmpty()) {
        continue;
      }
      filterChainBeanIndex++;
      AnalysisWarningEvidence beanEvidence = annotationEvidence(
          sourceFile.sourcePath(),
          className,
          method.getNameAsString(),
          annotationSymbol(BEAN),
          beanAnnotation.orElseThrow());
      evidence.putIfAbsent(beanEvidence.id(), beanEvidence);
      evidence.putIfAbsent(returnTypeEvidence.orElseThrow().id(), returnTypeEvidence.orElseThrow());
      warnings.add(new AnalysisWarningFact(
          warningId(
              SIGNAL_SECURITY_FILTER_CHAIN_BEAN,
              className + "#" + method.getNameAsString() + ":decl:" + ordinal(filterChainBeanIndex),
              modulePathForIds),
          CATEGORY_SPRING_SECURITY,
          SIGNAL_SECURITY_FILTER_CHAIN_BEAN,
          SECURITY_FILTER_CHAIN_BEAN_MESSAGE,
          sourceFile.sourcePath(),
          List.of(beanEvidence.id(), returnTypeEvidence.orElseThrow().id())));
    }
  }

  private Optional<String> supportedSecurityAnnotation(
      AnnotationExpr annotation,
      Map<String, String> importsBySimpleName,
      Set<String> sourceDeclaredTypeNames) {
    return JavaSourceOrigins.supportedAnnotationSimpleName(
            annotation,
            importsBySimpleName,
            SUPPORTED_ANNOTATION_ORIGINS,
            sourceDeclaredTypeNames)
        .filter(simpleName -> !BEAN.equals(simpleName));
  }

  private Optional<AnnotationExpr> beanAnnotation(
      List<AnnotationExpr> annotations,
      Map<String, String> importsBySimpleName,
      Set<String> sourceDeclaredTypeNames) {
    return annotations.stream()
        .filter(candidate -> BEAN.equals(JavaSourceOrigins.supportedAnnotationSimpleName(
                candidate,
                importsBySimpleName,
                SUPPORTED_ANNOTATION_ORIGINS,
                sourceDeclaredTypeNames)
            .orElse(null)))
        .findFirst();
  }

  private Optional<AnalysisWarningEvidence> securityFilterChainReturnTypeEvidence(
      SecuritySourceFile sourceFile,
      String className,
      MethodDeclaration method,
      Set<String> sourceDeclaredTypeNames) {
    Optional<String> simpleName = JavaSourceOrigins.supportedTypeSimpleName(
        method.getType().asString(),
        sourceFile.importsBySimpleName(),
        SUPPORTED_TYPE_ORIGINS,
        sourceDeclaredTypeNames);
    if (simpleName.isEmpty()) {
      return Optional.empty();
    }

    Integer lineStart = method.getType().getRange().map(range -> range.begin.line).orElse(null);
    Integer lineEnd = method.getType().getRange().map(range -> range.end.line).orElse(null);
    return Optional.of(new AnalysisWarningEvidence(
        evidenceId(
            sourceFile.sourcePath(),
            className,
            method.getNameAsString(),
            "return:" + simpleName.orElseThrow(),
            lineStart,
            lineEnd),
        CODE_SYMBOL_SOURCE_TYPE,
        sourceFile.sourcePath(),
        className,
        method.getNameAsString(),
        simpleName.orElseThrow(),
        lineStart,
        lineEnd,
        EvidenceExcerpts.bounded(method.getType().asString()),
        HIGH_CONFIDENCE));
  }

  private SecuritySourceFile sourceFile(Path repositoryRoot, Path javaFile)
      throws IOException {
    CompilationUnit compilationUnit = JavaSourceParser.parse(javaFile);
    String packageName = compilationUnit.getPackageDeclaration()
        .map(packageDeclaration -> packageDeclaration.getName().asString())
        .orElse("");
    return new SecuritySourceFile(
        compilationUnit,
        packageName,
        repositoryRelativePath(repositoryRoot, javaFile),
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

  private List<Path> javaFiles(Path canonicalRepositoryRoot, Path sourceRoot)
      throws IOException {
    try (Stream<Path> paths = Files.walk(sourceRoot)) {
      return paths
          .filter(path -> ScanPathContainment.isRegularFileUnderRoot(canonicalRepositoryRoot, path)
              && path.getFileName().toString().endsWith(".java"))
          .sorted(Comparator.comparing(path -> path.toAbsolutePath().normalize().toString()))
          .toList();
    }
  }

  private AnalysisWarningEvidence annotationEvidence(
      String sourcePath,
      String className,
      String methodName,
      String annotationSymbol,
      AnnotationExpr annotation) {
    Integer lineStart = annotation.getRange().map(range -> range.begin.line).orElse(null);
    Integer lineEnd = annotation.getRange().map(range -> range.end.line).orElse(null);
    return new AnalysisWarningEvidence(
        evidenceId(sourcePath, className, methodName, annotationSymbol, lineStart, lineEnd),
        ANNOTATION_SOURCE_TYPE,
        sourcePath,
        className,
        methodName,
        annotationSymbol,
        lineStart,
        lineEnd,
        EvidenceExcerpts.bounded(annotationSymbol),
        HIGH_CONFIDENCE);
  }

  private String evidenceId(
      String sourcePath,
      String className,
      String methodName,
      String symbol,
      Integer lineStart,
      Integer lineEnd) {
    String lineRange = lineStart == null || lineEnd == null ? "unknown" : lineStart + "-" + lineEnd;
    String symbolOwner = methodName == null ? className : className + "#" + methodName;
    return "ev:" + sourcePath + ":" + lineRange + ":" + symbolOwner + ":" + symbol;
  }

  private String warningId(String signal, String targetKey, String modulePathForIds) {
    if (modulePathForIds == null || ".".equals(modulePathForIds)) {
      return "warning:" + CATEGORY_SPRING_SECURITY + ":" + signal + ":" + targetKey;
    }
    return "warning:" + CATEGORY_SPRING_SECURITY + ":" + signal
        + ":module:" + modulePathForIds + ":" + targetKey;
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

  private static String annotationSymbol(String simpleName) {
    return "@" + simpleName;
  }

  private static String annotationDiscriminator(String simpleName) {
    StringBuilder discriminator = new StringBuilder();
    for (int index = 0; index < simpleName.length(); index++) {
      char character = simpleName.charAt(index);
      if (Character.isUpperCase(character) && index > 0) {
        discriminator.append('_');
      }
      discriminator.append(Character.toLowerCase(character));
    }
    return discriminator.toString();
  }

  private static String ordinal(int index) {
    return "%06d".formatted(index);
  }

  private record SecuritySourceFile(
      CompilationUnit compilationUnit,
      String packageName,
      String sourcePath,
      Map<String, String> importsBySimpleName,
      Set<String> declaredTypeNames) {
    private SecuritySourceFile {
      importsBySimpleName = Map.copyOf(importsBySimpleName);
      declaredTypeNames = Set.copyOf(declaredTypeNames);
    }
  }
}
