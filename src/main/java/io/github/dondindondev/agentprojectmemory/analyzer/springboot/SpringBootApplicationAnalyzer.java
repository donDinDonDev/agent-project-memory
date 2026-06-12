package io.github.dondindondev.agentprojectmemory.analyzer.springboot;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.type.Type;
import io.github.dondindondev.agentprojectmemory.analyzer.JavaSourceOrigins;
import io.github.dondindondev.agentprojectmemory.analyzer.JavaSourceParser;
import io.github.dondindondev.agentprojectmemory.analyzer.ScanPathContainment;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenModuleItem;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class SpringBootApplicationAnalyzer {
  private static final String ANALYSIS_STATUS_ANALYZED = "analyzed";
  private static final String ANALYSIS_STATUS_NOT_DETECTED = "not_detected";
  private static final String MODULE_SUPPORTED = "supported";
  private static final String ANNOTATION_SOURCE_TYPE = "annotation";
  private static final String CODE_SYMBOL_SOURCE_TYPE = "code_symbol";
  private static final String HIGH_CONFIDENCE = "high";
  private static final String ANNOTATION_SYMBOL = "@SpringBootApplication";
  private static final String ANNOTATION_ONLY_SIGNAL = "spring_boot_application_annotation_only";
  private static final String WITH_MAIN_METHOD_SIGNAL = "spring_boot_application_with_main_method";
  private static final Map<String, Set<String>> SUPPORTED_APPLICATION_ORIGINS = Map.of(
      "SpringBootApplication",
      Set.of("org.springframework.boot.autoconfigure.SpringBootApplication"));
  private static final Comparator<SpringBootApplicationFact> APPLICATION_ORDER = Comparator
      .comparing(SpringBootApplicationFact::className)
      .thenComparing(SpringBootApplicationFact::sourcePath)
      .thenComparing(SpringBootApplicationFact::id);
  private static final Comparator<SpringBootApplicationEvidence> EVIDENCE_ORDER = Comparator
      .comparing(SpringBootApplicationEvidence::sourcePath)
      .thenComparing(evidence -> evidence.lineStart() == null ? Integer.MAX_VALUE : evidence.lineStart())
      .thenComparing(evidence -> evidence.lineEnd() == null ? Integer.MAX_VALUE : evidence.lineEnd())
      .thenComparing(evidence -> nullSafe(evidence.className()))
      .thenComparing(evidence -> nullSafe(evidence.methodName()))
      .thenComparing(SpringBootApplicationEvidence::symbolName)
      .thenComparing(SpringBootApplicationEvidence::id);

  public SpringBootApplicationAnalysis analyze(Path repositoryRoot, List<MavenModuleItem> modules)
      throws IOException {
    Objects.requireNonNull(repositoryRoot, "repositoryRoot");
    Objects.requireNonNull(modules, "modules");

    Path normalizedRepositoryRoot = repositoryRoot.toAbsolutePath().normalize();
    Path canonicalRepositoryRoot = ScanPathContainment.canonicalRoot(normalizedRepositoryRoot);
    Map<String, SpringBootApplicationEvidence> evidence = new LinkedHashMap<>();
    List<ModuleSpringBootApplications> moduleApplications = new ArrayList<>();

    for (MavenModuleItem module : modules) {
      if (!MODULE_SUPPORTED.equals(module.supportStatus()) || module.sourceRoots().isEmpty()) {
        moduleApplications.add(notDetected(module.moduleId()));
        continue;
      }

      List<Path> sourceRoots = existingSourceRoots(
          normalizedRepositoryRoot,
          canonicalRepositoryRoot,
          module.sourceRoots());
      if (sourceRoots.isEmpty()) {
        moduleApplications.add(notDetected(module.moduleId()));
        continue;
      }

      ModuleAnalysis moduleAnalysis = analyzeModule(
          normalizedRepositoryRoot,
          canonicalRepositoryRoot,
          module.moduleId(),
          sourceRoots);
      moduleAnalysis.evidence().forEach(record -> evidence.putIfAbsent(record.id(), record));
      moduleApplications.add(new ModuleSpringBootApplications(
          module.moduleId(),
          ANALYSIS_STATUS_ANALYZED,
          moduleAnalysis.applications().stream()
              .sorted(APPLICATION_ORDER)
              .toList()));
    }

    return new SpringBootApplicationAnalysis(
        moduleApplications,
        evidence.values().stream()
            .sorted(EVIDENCE_ORDER)
            .toList());
  }

  private ModuleSpringBootApplications notDetected(String moduleId) {
    return new ModuleSpringBootApplications(
        moduleId,
        ANALYSIS_STATUS_NOT_DETECTED,
        List.of());
  }

  private ModuleAnalysis analyzeModule(
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      String moduleId,
      List<Path> sourceRoots) throws IOException {
    List<ApplicationSourceFile> sourceFiles = new ArrayList<>();
    Set<String> sourceDeclaredTypeNames = new java.util.LinkedHashSet<>();
    for (Path sourceRoot : sourceRoots) {
      for (Path javaFile : javaFiles(canonicalRepositoryRoot, sourceRoot)) {
        ApplicationSourceFile sourceFile = sourceFile(repositoryRoot, javaFile);
        sourceFiles.add(sourceFile);
        sourceDeclaredTypeNames.addAll(sourceFile.declaredTypeNames());
      }
    }

    List<SpringBootApplicationFact> applications = new ArrayList<>();
    List<SpringBootApplicationEvidence> evidence = new ArrayList<>();
    JavaSourceOrigins.markIncompleteSourceIndexIfNeeded(sourceDeclaredTypeNames);
    for (ApplicationSourceFile sourceFile : sourceFiles) {
      analyzeJavaFile(sourceFile, moduleId, sourceDeclaredTypeNames, applications, evidence);
    }
    return new ModuleAnalysis(applications, evidence);
  }

  private ApplicationSourceFile sourceFile(Path repositoryRoot, Path javaFile) throws IOException {
    CompilationUnit compilationUnit = JavaSourceParser.parse(javaFile);
    String packageName = compilationUnit.getPackageDeclaration()
        .map(packageDeclaration -> packageDeclaration.getName().asString())
        .orElse("");
    return new ApplicationSourceFile(
        compilationUnit,
        packageName,
        repositoryRelativePath(repositoryRoot, javaFile),
        JavaSourceOrigins.singleTypeImportsBySimpleName(compilationUnit),
        JavaSourceOrigins.declaredTypeNames(compilationUnit, packageName));
  }

  private void analyzeJavaFile(
      ApplicationSourceFile sourceFile,
      String moduleId,
      Set<String> sourceDeclaredTypeNames,
      List<SpringBootApplicationFact> applications,
      List<SpringBootApplicationEvidence> evidence) {
    for (ClassOrInterfaceDeclaration type : sourceFile.compilationUnit().findAll(
        ClassOrInterfaceDeclaration.class)) {
      if (type.isInterface()) {
        continue;
      }

      Optional<AnnotationExpr> applicationAnnotation = applicationAnnotation(
          type,
          sourceFile.importsBySimpleName(),
          sourceDeclaredTypeNames);
      if (applicationAnnotation.isEmpty()) {
        continue;
      }

      String className = qualifiedClassName(sourceFile.packageName(), type);
      SpringBootApplicationEvidence annotationEvidence = annotationEvidence(
          sourceFile.sourcePath(),
          className,
          applicationAnnotation.orElseThrow());
      Optional<SpringBootApplicationEvidence> mainMethodEvidence = supportedMainMethod(type)
          .map(method -> mainMethodEvidence(sourceFile.sourcePath(), className, method));
      List<String> mainMethodEvidenceIds = mainMethodEvidence.stream()
          .map(SpringBootApplicationEvidence::id)
          .toList();
      List<String> evidenceIds = new ArrayList<>();
      evidenceIds.add(annotationEvidence.id());
      evidenceIds.addAll(mainMethodEvidenceIds);

      applications.add(new SpringBootApplicationFact(
          applicationId(moduleId, className),
          className,
          sourceFile.sourcePath(),
          mainMethodEvidence.isPresent() ? WITH_MAIN_METHOD_SIGNAL : ANNOTATION_ONLY_SIGNAL,
          mainMethodEvidence.isPresent(),
          mainMethodEvidenceIds,
          evidenceIds));
      evidence.add(annotationEvidence);
      mainMethodEvidence.ifPresent(evidence::add);
    }
  }

  private Optional<AnnotationExpr> applicationAnnotation(
      ClassOrInterfaceDeclaration type,
      Map<String, String> importsBySimpleName,
      Set<String> sourceDeclaredTypeNames) {
    return type.getAnnotations().stream()
        .filter(annotation -> JavaSourceOrigins.supportedAnnotationSimpleName(
                annotation,
                importsBySimpleName,
                SUPPORTED_APPLICATION_ORIGINS,
                sourceDeclaredTypeNames)
            .isPresent())
        .findFirst();
  }

  private Optional<MethodDeclaration> supportedMainMethod(ClassOrInterfaceDeclaration type) {
    return type.getMethodsByName("main").stream()
        .filter(this::isSupportedMainMethod)
        .sorted(Comparator.comparing(method -> method.getRange()
            .map(range -> range.begin.line)
            .orElse(Integer.MAX_VALUE)))
        .findFirst();
  }

  private boolean isSupportedMainMethod(MethodDeclaration method) {
    if (!method.isStatic() || !method.getType().isVoidType() || method.getParameters().size() != 1) {
      return false;
    }

    Parameter parameter = method.getParameter(0);
    Type parameterType = parameter.getType();
    String parameterTypeName = parameterType.asString();
    if (parameter.isVarArgs()) {
      return "String".equals(parameterTypeName) || "java.lang.String".equals(parameterTypeName);
    }
    return "String[]".equals(parameterTypeName) || "java.lang.String[]".equals(parameterTypeName);
  }

  private SpringBootApplicationEvidence annotationEvidence(
      String sourcePath,
      String className,
      AnnotationExpr annotation) {
    Integer lineStart = annotation.getRange().map(range -> range.begin.line).orElse(null);
    Integer lineEnd = annotation.getRange().map(range -> range.end.line).orElse(null);
    return new SpringBootApplicationEvidence(
        evidenceId(sourcePath, className, ANNOTATION_SYMBOL, lineStart, lineEnd),
        ANNOTATION_SOURCE_TYPE,
        sourcePath,
        className,
        null,
        ANNOTATION_SYMBOL,
        lineStart,
        lineEnd,
        ANNOTATION_SYMBOL,
        HIGH_CONFIDENCE);
  }

  private SpringBootApplicationEvidence mainMethodEvidence(
      String sourcePath,
      String className,
      MethodDeclaration method) {
    Integer lineStart = method.getName().getRange()
        .map(range -> range.begin.line)
        .orElseGet(() -> method.getRange().map(range -> range.begin.line).orElse(null));
    Integer lineEnd = lineStart;
    return new SpringBootApplicationEvidence(
        evidenceId(sourcePath, className + "#main", CODE_SYMBOL_SOURCE_TYPE, lineStart, lineEnd),
        CODE_SYMBOL_SOURCE_TYPE,
        sourcePath,
        className,
        "main",
        "main",
        lineStart,
        lineEnd,
        "method detected: static void main(String[])",
        HIGH_CONFIDENCE);
  }

  private List<Path> existingSourceRoots(
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      List<String> sourceRootPaths) {
    return sourceRootPaths.stream()
        .map(repositoryRoot::resolve)
        .map(Path::normalize)
        .filter(sourceRoot -> ScanPathContainment.isDirectoryUnderRoot(
            canonicalRepositoryRoot,
            sourceRoot))
        .sorted(Comparator.comparing(path -> path.toAbsolutePath().normalize().toString()))
        .toList();
  }

  private List<Path> javaFiles(Path canonicalRepositoryRoot, Path sourceRoot) throws IOException {
    return JavaSourceParser.javaFiles(canonicalRepositoryRoot, sourceRoot);
  }

  private String applicationId(String moduleId, String className) {
    return "spring_boot_application:" + moduleId + ":" + className;
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

  private static String nullSafe(String value) {
    return value == null ? "" : value;
  }

  private record ApplicationSourceFile(
      CompilationUnit compilationUnit,
      String packageName,
      String sourcePath,
      Map<String, String> importsBySimpleName,
      Set<String> declaredTypeNames) {
    private ApplicationSourceFile {
      importsBySimpleName = Map.copyOf(importsBySimpleName);
      declaredTypeNames = Set.copyOf(declaredTypeNames);
    }
  }

  private record ModuleAnalysis(
      List<SpringBootApplicationFact> applications,
      List<SpringBootApplicationEvidence> evidence) {
    private ModuleAnalysis {
      applications = List.copyOf(applications);
      evidence = List.copyOf(evidence);
    }
  }
}
