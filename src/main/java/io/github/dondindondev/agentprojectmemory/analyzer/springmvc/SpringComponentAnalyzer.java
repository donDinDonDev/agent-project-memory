package io.github.dondindondev.agentprojectmemory.analyzer.springmvc;

import com.github.javaparser.Range;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

final class SpringComponentAnalyzer {
  private static final String HIGH_CONFIDENCE = "high";
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
    List<SpringComponentFact> components = new ArrayList<>();
    List<SpringComponentEvidence> evidence = new ArrayList<>();

    for (Path sourceRoot : sourceRoots) {
      Path normalizedSourceRoot = normalizeSourceRoot(normalizedRepositoryRoot, sourceRoot);
      if (!Files.isDirectory(normalizedSourceRoot)) {
        continue;
      }

      for (Path javaFile : javaFiles(normalizedSourceRoot)) {
        analyzeJavaFile(normalizedRepositoryRoot, javaFile, components, evidence);
      }
    }

    return new SpringComponentAnalysis(
        components.stream().sorted(COMPONENT_ORDER).toList(),
        evidence);
  }

  private void analyzeJavaFile(
      Path repositoryRoot,
      Path javaFile,
      List<SpringComponentFact> components,
      List<SpringComponentEvidence> evidence) throws IOException {
    CompilationUnit compilationUnit = StaticJavaParser.parse(javaFile);
    String packageName = compilationUnit.getPackageDeclaration()
        .map(packageDeclaration -> packageDeclaration.getName().asString())
        .orElse("");
    String sourcePath = repositoryRelativePath(repositoryRoot, javaFile);
    List<String> sourceLines = Files.readAllLines(javaFile);

    for (ClassOrInterfaceDeclaration type : compilationUnit.findAll(ClassOrInterfaceDeclaration.class)) {
      if (type.isInterface()) {
        continue;
      }

      List<AnnotationExpr> stereotypeAnnotations = stereotypeAnnotations(type.getAnnotations());
      if (stereotypeAnnotations.isEmpty()) {
        continue;
      }

      String className = qualifiedClassName(packageName, type);
      List<SpringComponentEvidence> componentEvidence = stereotypeAnnotations.stream()
          .map(annotation -> annotationEvidence(sourcePath, className, annotation, sourceLines))
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

  private List<Path> javaFiles(Path sourceRoot) throws IOException {
    try (Stream<Path> paths = Files.walk(sourceRoot)) {
      return paths
          .filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".java"))
          .sorted(Comparator.comparing(path -> path.toAbsolutePath().normalize().toString()))
          .toList();
    }
  }

  private List<AnnotationExpr> stereotypeAnnotations(List<AnnotationExpr> annotations) {
    return annotations.stream()
        .filter(annotation -> SUPPORTED_STEREOTYPES.contains(annotationSymbol(annotation)))
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
    Optional<Range> range = annotation.getRange();
    if (range.isEmpty()) {
      return annotation.toString();
    }

    int start = range.orElseThrow().begin.line;
    int end = range.orElseThrow().end.line;
    if (start < 1 || end < start || end > sourceLines.size()) {
      return annotation.toString();
    }

    return String.join("\n", sourceLines.subList(start - 1, end)).trim();
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
    String name = annotation.getNameAsString();
    int lastDot = name.lastIndexOf('.');
    if (lastDot >= 0) {
      return name.substring(lastDot + 1);
    }
    return name;
  }
}
