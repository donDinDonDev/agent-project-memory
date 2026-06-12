package io.github.dondindondev.agentprojectmemory.analyzer.springmvc;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import io.github.dondindondev.agentprojectmemory.analyzer.EvidenceExcerpts;
import io.github.dondindondev.agentprojectmemory.analyzer.JavaSourceOrigins;
import io.github.dondindondev.agentprojectmemory.analyzer.JavaSourceParser;
import io.github.dondindondev.agentprojectmemory.analyzer.ScanPathContainment;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

final class SpringComponentAnalyzer {
  private static final String HIGH_CONFIDENCE = "high";
  private static final Map<String, Set<String>> SUPPORTED_STEREOTYPE_ORIGINS = Map.ofEntries(
      Map.entry("Component", Set.of("org.springframework.stereotype.Component")),
      Map.entry("Service", Set.of("org.springframework.stereotype.Service")),
      Map.entry("Repository", Set.of("org.springframework.stereotype.Repository")),
      Map.entry("Controller", Set.of("org.springframework.stereotype.Controller")),
      Map.entry("RestController", Set.of("org.springframework.web.bind.annotation.RestController")),
      Map.entry("Configuration", Set.of("org.springframework.context.annotation.Configuration")));
  private static final List<String> SUPPORTED_STEREOTYPES = List.of(
      "@Component",
      "@Service",
      "@Repository",
      "@Controller",
      "@RestController",
      "@Configuration");
  private static final Comparator<SpringComponentFact> COMPONENT_ORDER = Comparator
      .comparing(SpringComponentFact::className)
      .thenComparing(SpringComponentFact::id);

  SpringComponentAnalysis analyze(Path repositoryRoot, List<Path> sourceRoots) throws IOException {
    Objects.requireNonNull(repositoryRoot, "repositoryRoot");
    Objects.requireNonNull(sourceRoots, "sourceRoots");

    Path normalizedRepositoryRoot = repositoryRoot.toAbsolutePath().normalize();
    Path canonicalRepositoryRoot = ScanPathContainment.canonicalRoot(normalizedRepositoryRoot);
    List<SpringComponentFact> components = new ArrayList<>();
    List<SpringComponentEvidence> evidence = new ArrayList<>();
    List<ComponentSourceFile> sourceFiles = new ArrayList<>();
    Set<String> sourceDeclaredTypeNames = new java.util.LinkedHashSet<>();

    for (Path sourceRoot : sourceRoots) {
      Path normalizedSourceRoot = normalizeSourceRoot(normalizedRepositoryRoot, sourceRoot);
      if (!ScanPathContainment.isDirectoryUnderRoot(
          canonicalRepositoryRoot,
          normalizedSourceRoot)) {
        continue;
      }

      for (Path javaFile : javaFiles(canonicalRepositoryRoot, normalizedSourceRoot)) {
        ComponentSourceFile sourceFile = sourceFile(normalizedRepositoryRoot, javaFile);
        sourceFiles.add(sourceFile);
        sourceDeclaredTypeNames.addAll(sourceFile.declaredTypeNames());
      }
    }

    JavaSourceOrigins.markIncompleteSourceIndexIfNeeded(sourceDeclaredTypeNames);
    for (ComponentSourceFile sourceFile : sourceFiles) {
      analyzeJavaFile(sourceFile, sourceDeclaredTypeNames, components, evidence);
    }

    return new SpringComponentAnalysis(
        components.stream().sorted(COMPONENT_ORDER).toList(),
        evidence);
  }

  private ComponentSourceFile sourceFile(
      Path repositoryRoot,
      Path javaFile) throws IOException {
    CompilationUnit compilationUnit = JavaSourceParser.parse(javaFile);
    String packageName = compilationUnit.getPackageDeclaration()
        .map(packageDeclaration -> packageDeclaration.getName().asString())
        .orElse("");
    String sourcePath = repositoryRelativePath(repositoryRoot, javaFile);
    List<String> sourceLines = JavaSourceParser.sourceLines(javaFile);
    Map<String, String> importsBySimpleName = SpringAnnotationOrigins.importsBySimpleName(compilationUnit);

    return new ComponentSourceFile(
        compilationUnit,
        packageName,
        sourcePath,
        sourceLines,
        importsBySimpleName,
        JavaSourceOrigins.declaredTypeNames(compilationUnit, packageName));
  }

  private void analyzeJavaFile(
      ComponentSourceFile sourceFile,
      Set<String> sourceDeclaredTypeNames,
      List<SpringComponentFact> components,
      List<SpringComponentEvidence> evidence) {
    for (ClassOrInterfaceDeclaration type : sourceFile.compilationUnit().findAll(ClassOrInterfaceDeclaration.class)) {
      List<AnnotationExpr> stereotypeAnnotations = stereotypeAnnotations(
          type.getAnnotations(),
          sourceFile.importsBySimpleName(),
          sourceDeclaredTypeNames);
      if (stereotypeAnnotations.isEmpty()) {
        continue;
      }

      String className = qualifiedClassName(sourceFile.packageName(), type);
      List<SpringComponentEvidence> componentEvidence = stereotypeAnnotations.stream()
          .map(annotation -> annotationEvidence(
              sourceFile.sourcePath(),
              className,
              annotation,
              sourceFile.sourceLines()))
          .toList();
      List<String> stereotypes = componentEvidence.stream()
          .map(SpringComponentEvidence::annotationSymbol)
          .toList();
      List<String> evidenceIds = componentEvidence.stream()
          .map(SpringComponentEvidence::id)
          .toList();

      components.add(new SpringComponentFact(
          "component:" + className,
          className,
          stereotypes,
          evidenceIds));
      evidence.addAll(componentEvidence);
    }
  }

  private Path normalizeSourceRoot(Path repositoryRoot, Path sourceRoot) {
    Objects.requireNonNull(sourceRoot, "sourceRoot");
    if (sourceRoot.isAbsolute()) {
      return sourceRoot.toAbsolutePath().normalize();
    }
    return repositoryRoot.resolve(sourceRoot).normalize();
  }

  private List<Path> javaFiles(Path canonicalRepositoryRoot, Path sourceRoot) throws IOException {
    return JavaSourceParser.javaFiles(canonicalRepositoryRoot, sourceRoot);
  }

  private List<AnnotationExpr> stereotypeAnnotations(
      List<AnnotationExpr> annotations,
      Map<String, String> importsBySimpleName,
      Set<String> sourceDeclaredTypeNames) {
    return annotations.stream()
        .filter(annotation -> SpringAnnotationOrigins.supportedSimpleName(
                annotation,
                importsBySimpleName,
                SUPPORTED_STEREOTYPE_ORIGINS,
                sourceDeclaredTypeNames)
            .isPresent())
        .sorted(Comparator.comparingInt(annotation -> SUPPORTED_STEREOTYPES.indexOf(
            annotationSymbol(annotation))))
        .toList();
  }

  private SpringComponentEvidence annotationEvidence(
      String sourcePath,
      String className,
      AnnotationExpr annotation,
      List<String> sourceLines) {
    String annotationSymbol = annotationSymbol(annotation);
    Integer lineStart = annotation.getRange().map(range -> range.begin.line).orElse(null);
    Integer lineEnd = annotation.getRange().map(range -> range.end.line).orElse(null);

    return new SpringComponentEvidence(
        evidenceId(sourcePath, className, annotationSymbol, lineStart, lineEnd),
        sourcePath,
        className,
        null,
        annotationSymbol,
        lineStart,
        lineEnd,
        excerpt(annotation, sourceLines),
        HIGH_CONFIDENCE);
  }

  private String evidenceId(
      String sourcePath,
      String className,
      String annotationSymbol,
      Integer lineStart,
      Integer lineEnd) {
    String lineRange = lineStart == null || lineEnd == null ? "unknown" : lineStart + "-" + lineEnd;
    return "ev:" + sourcePath + ":" + lineRange + ":" + className + ":" + annotationSymbol;
  }

  private String excerpt(AnnotationExpr annotation, List<String> sourceLines) {
    return EvidenceExcerpts.sourceRange(annotation, sourceLines);
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

  private String annotationSymbol(AnnotationExpr annotation) {
    return "@" + simpleAnnotationName(annotation);
  }

  private String simpleAnnotationName(AnnotationExpr annotation) {
    return SpringAnnotationOrigins.simpleAnnotationName(annotation);
  }

  private record ComponentSourceFile(
      CompilationUnit compilationUnit,
      String packageName,
      String sourcePath,
      List<String> sourceLines,
      Map<String, String> importsBySimpleName,
      Set<String> declaredTypeNames) {
    private ComponentSourceFile {
      sourceLines = List.copyOf(sourceLines);
      importsBySimpleName = Map.copyOf(importsBySimpleName);
      declaredTypeNames = Set.copyOf(declaredTypeNames);
    }
  }
}
