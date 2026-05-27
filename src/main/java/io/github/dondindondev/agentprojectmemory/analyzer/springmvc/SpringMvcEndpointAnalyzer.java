package io.github.dondindondev.agentprojectmemory.analyzer.springmvc;

import com.github.javaparser.Range;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.Expression;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

final class SpringMvcEndpointAnalyzer {
  private static final String CONTROLLER = "Controller";
  private static final String REST_CONTROLLER = "RestController";
  private static final String REQUEST_MAPPING = "RequestMapping";
  private static final String GET_MAPPING = "GetMapping";
  private static final String HIGH_CONFIDENCE = "high";

  SpringMvcEndpointAnalysis analyze(Path repositoryRoot, List<Path> sourceRoots) throws IOException {
    Objects.requireNonNull(repositoryRoot, "repositoryRoot");
    Objects.requireNonNull(sourceRoots, "sourceRoots");

    Path normalizedRepositoryRoot = repositoryRoot.toAbsolutePath().normalize();
    List<SpringMvcEndpointFact> endpoints = new ArrayList<>();
    List<SpringMvcEndpointEvidence> evidence = new ArrayList<>();

    for (Path sourceRoot : sourceRoots) {
      Path normalizedSourceRoot = normalizeSourceRoot(normalizedRepositoryRoot, sourceRoot);
      if (!Files.isDirectory(normalizedSourceRoot)) {
        continue;
      }

      for (Path javaFile : javaFiles(normalizedSourceRoot)) {
        analyzeJavaFile(normalizedRepositoryRoot, javaFile, endpoints, evidence);
      }
    }

    return new SpringMvcEndpointAnalysis(endpoints, evidence);
  }

  private void analyzeJavaFile(
      Path repositoryRoot,
      Path javaFile,
      List<SpringMvcEndpointFact> endpoints,
      List<SpringMvcEndpointEvidence> evidence) throws IOException {
    CompilationUnit compilationUnit = StaticJavaParser.parse(javaFile);
    String packageName = compilationUnit.getPackageDeclaration()
        .map(packageDeclaration -> packageDeclaration.getName().asString())
        .orElse("");
    String sourcePath = repositoryRelativePath(repositoryRoot, javaFile);
    List<String> sourceLines = Files.readAllLines(javaFile);

    for (ClassOrInterfaceDeclaration type : compilationUnit.findAll(ClassOrInterfaceDeclaration.class)) {
      if (type.isInterface() || !isController(type)) {
        continue;
      }

      String controllerClass = qualifiedClassName(packageName, type.getNameAsString());
      Optional<AnnotationExpr> requestMapping = findAnnotation(type.getAnnotations(), REQUEST_MAPPING);
      ExtractedPaths classPathExtraction = requestMapping
          .map(this::literalPathValues)
          .orElseGet(ExtractedPaths::notDeclared);
      if (classPathExtraction.isDeclaredButUnsupported()) {
        continue;
      }
      List<String> classPaths = classPathExtraction.pathsOrDefaultRoot();
      SpringMvcEndpointEvidence requestMappingEvidence = requestMapping
          .map(annotation -> mappingEvidence(sourcePath, controllerClass, null, annotation, sourceLines))
          .orElse(null);

      for (MethodDeclaration method : type.getMethods()) {
        Optional<AnnotationExpr> getMapping = findAnnotation(method.getAnnotations(), GET_MAPPING);
        if (getMapping.isEmpty()) {
          continue;
        }

        ExtractedPaths methodPathExtraction = literalPathValues(getMapping.orElseThrow());
        if (!methodPathExtraction.hasPaths()) {
          continue;
        }
        SpringMvcEndpointEvidence getMappingEvidence = mappingEvidence(
            sourcePath,
            controllerClass,
            method.getNameAsString(),
            getMapping.orElseThrow(),
            sourceLines);
        addEvidenceIfAbsent(evidence, requestMappingEvidence);
        evidence.add(getMappingEvidence);

        List<String> evidenceIds = new ArrayList<>();
        if (requestMappingEvidence != null) {
          evidenceIds.add(requestMappingEvidence.id());
        }
        evidenceIds.add(getMappingEvidence.id());

        endpoints.add(new SpringMvcEndpointFact(
            controllerClass,
            method.getNameAsString(),
            "GET",
            combinePaths(classPaths, methodPathExtraction.paths()),
            method.getType().asString(),
            evidenceIds));
      }
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

  private boolean isController(ClassOrInterfaceDeclaration type) {
    return findAnnotation(type.getAnnotations(), CONTROLLER).isPresent()
        || findAnnotation(type.getAnnotations(), REST_CONTROLLER).isPresent();
  }

  private Optional<AnnotationExpr> findAnnotation(List<AnnotationExpr> annotations, String simpleName) {
    return annotations.stream()
        .filter(annotation -> simpleAnnotationName(annotation).equals(simpleName))
        .findFirst();
  }

  private String simpleAnnotationName(AnnotationExpr annotation) {
    String name = annotation.getNameAsString();
    int lastDot = name.lastIndexOf('.');
    if (lastDot >= 0) {
      return name.substring(lastDot + 1);
    }
    return name;
  }

  private ExtractedPaths literalPathValues(AnnotationExpr annotation) {
    if (annotation.isSingleMemberAnnotationExpr()) {
      return literalPathValues(annotation.asSingleMemberAnnotationExpr().getMemberValue());
    }

    if (annotation.isNormalAnnotationExpr()) {
      return annotation.asNormalAnnotationExpr().getPairs().stream()
          .filter(pair -> "value".equals(pair.getNameAsString()) || "path".equals(pair.getNameAsString()))
          .findFirst()
          .map(pair -> literalPathValues(pair.getValue()))
          .orElseGet(ExtractedPaths::notDeclared);
    }

    return ExtractedPaths.notDeclared();
  }

  private ExtractedPaths literalPathValues(Expression expression) {
    if (expression.isStringLiteralExpr()) {
      return ExtractedPaths.declared(List.of(expression.asStringLiteralExpr().asString()));
    }

    if (expression.isArrayInitializerExpr()) {
      ArrayInitializerExpr array = expression.asArrayInitializerExpr();
      List<String> paths = new ArrayList<>();
      for (Expression value : array.getValues()) {
        if (!value.isStringLiteralExpr()) {
          return ExtractedPaths.unsupported();
        }
        paths.add(value.asStringLiteralExpr().asString());
      }
      return ExtractedPaths.declared(paths);
    }

    return ExtractedPaths.unsupported();
  }

  private void addEvidenceIfAbsent(
      List<SpringMvcEndpointEvidence> evidence,
      SpringMvcEndpointEvidence candidate) {
    if (candidate == null) {
      return;
    }

    boolean alreadyAdded = evidence.stream()
        .anyMatch(existing -> existing.id().equals(candidate.id()));
    if (!alreadyAdded) {
      evidence.add(candidate);
    }
  }

  private SpringMvcEndpointEvidence mappingEvidence(
      String sourcePath,
      String className,
      String methodName,
      AnnotationExpr annotation,
      List<String> sourceLines) {
    String annotationSymbol = "@" + simpleAnnotationName(annotation);
    Integer lineStart = annotation.getRange().map(range -> range.begin.line).orElse(null);
    Integer lineEnd = annotation.getRange().map(range -> range.end.line).orElse(null);

    return new SpringMvcEndpointEvidence(
        evidenceId(sourcePath, className, methodName, annotationSymbol, lineStart, lineEnd),
        sourcePath,
        className,
        methodName,
        annotationSymbol,
        lineStart,
        lineEnd,
        excerpt(annotation, sourceLines),
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

  private String qualifiedClassName(String packageName, String className) {
    if (packageName.isBlank()) {
      return className;
    }
    return packageName + "." + className;
  }

  private List<String> combinePaths(List<String> classPaths, List<String> methodPaths) {
    LinkedHashSet<String> paths = new LinkedHashSet<>();
    for (String classPath : classPaths) {
      for (String methodPath : methodPaths) {
        paths.add(combinePath(classPath, methodPath));
      }
    }
    return List.copyOf(paths);
  }

  private String combinePath(String classPath, String methodPath) {
    String normalizedClassPath = normalizePath(classPath);
    String normalizedMethodPath = normalizePath(methodPath);

    if (normalizedClassPath.isEmpty() && normalizedMethodPath.isEmpty()) {
      return "/";
    }

    if (normalizedClassPath.isEmpty()) {
      return ensureLeadingSlash(normalizedMethodPath);
    }

    if (normalizedMethodPath.isEmpty() || "/".equals(normalizedMethodPath)) {
      return ensureLeadingSlash(stripTrailingSlash(normalizedClassPath));
    }

    String classSegment = ensureLeadingSlash(stripTrailingSlash(normalizedClassPath));
    String methodSegment = ensureLeadingSlash(normalizedMethodPath);
    if ("/".equals(classSegment)) {
      return methodSegment;
    }
    return classSegment + methodSegment;
  }

  private String normalizePath(String path) {
    if (path == null) {
      return "";
    }
    return path.trim();
  }

  private String ensureLeadingSlash(String path) {
    if (path.isEmpty() || path.startsWith("/")) {
      return path;
    }
    return "/" + path;
  }

  private String stripTrailingSlash(String path) {
    while (path.length() > 1 && path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }
    return path;
  }

  private record ExtractedPaths(boolean pathDeclared, List<String> paths) {
    private ExtractedPaths {
      paths = List.copyOf(paths);
    }

    private static ExtractedPaths notDeclared() {
      return new ExtractedPaths(false, List.of());
    }

    private static ExtractedPaths unsupported() {
      return new ExtractedPaths(true, List.of());
    }

    private static ExtractedPaths declared(List<String> paths) {
      return new ExtractedPaths(true, paths);
    }

    private boolean hasPaths() {
      return !paths.isEmpty();
    }

    private boolean isDeclaredButUnsupported() {
      return pathDeclared && paths.isEmpty();
    }

    private List<String> pathsOrDefaultRoot() {
      if (paths.isEmpty()) {
        return List.of("");
      }
      return paths;
    }
  }
}
