package io.github.dondindondev.agentprojectmemory.analyzer.springapp;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import io.github.dondindondev.agentprojectmemory.analyzer.EvidenceExcerpts;
import io.github.dondindondev.agentprojectmemory.analyzer.JavaSourceOrigins;
import io.github.dondindondev.agentprojectmemory.analyzer.JavaSourceParser;
import io.github.dondindondev.agentprojectmemory.analyzer.ScanPathContainment;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class SpringRepositoryAnalyzer {
  public static final String SURFACE_CATEGORY_REPOSITORY_STEREOTYPE = "spring_repository_stereotype";
  public static final String SURFACE_CATEGORY_SPRING_DATA_INTERFACE =
      "spring_data_repository_interface_signal";
  public static final String SUPPORT_TYPE_EXTRACTED = "extracted";
  public static final String SUPPORT_TYPE_INFERRED = "inferred";
  public static final String DIRECT_REPOSITORY_STEREOTYPE = "direct_repository_stereotype";
  public static final String SPRING_DATA_REPOSITORY_INTERFACE_EXTENSION =
      "spring_data_repository_interface_extension";
  public static final String ENTITY_GENERIC_SUPPORTED = "supported";
  public static final String ENTITY_GENERIC_UNSUPPORTED = "unsupported";
  public static final String ENTITY_RELATION_INFERRED = "inferred";
  public static final String ENTITY_RELATION_NOT_DETECTED = "not_detected";
  public static final String ENTITY_RELATION_AMBIGUOUS = "ambiguous";
  public static final String ENTITY_RELATION_UNSUPPORTED = "unsupported";
  public static final String ENTITY_RELATION_NOT_ANALYZED = "not_analyzed";

  private static final String HIGH_CONFIDENCE = "high";
  private static final String ANNOTATION_SOURCE_TYPE = "annotation";
  private static final String CODE_SYMBOL_SOURCE_TYPE = "code_symbol";
  private static final String REPOSITORY_ANNOTATION_SYMBOL = "@Repository";
  private static final Map<String, Set<String>> SUPPORTED_REPOSITORY_ANNOTATION_ORIGINS = Map.of(
      "Repository",
      Set.of("org.springframework.stereotype.Repository"));
  private static final Map<String, Set<String>> SUPPORTED_SPRING_DATA_REPOSITORY_BASE_TYPES =
      Map.ofEntries(
          Map.entry("Repository", Set.of("org.springframework.data.repository.Repository")),
          Map.entry("CrudRepository", Set.of("org.springframework.data.repository.CrudRepository")),
          Map.entry(
              "PagingAndSortingRepository",
              Set.of("org.springframework.data.repository.PagingAndSortingRepository")),
          Map.entry("JpaRepository", Set.of("org.springframework.data.jpa.repository.JpaRepository")),
          Map.entry("MongoRepository", Set.of("org.springframework.data.mongodb.repository.MongoRepository")));
  private static final Comparator<SpringRepositoryFact> REPOSITORY_ORDER = Comparator
      .comparing(SpringRepositoryFact::sourcePath)
      .thenComparing(SpringRepositoryFact::className)
      .thenComparing(SpringRepositoryFact::surfaceCategory)
      .thenComparing(SpringRepositoryFact::repositorySignal);

  public SpringRepositoryAnalysis analyze(Path repositoryRoot, List<Path> sourceRoots) throws IOException {
    Objects.requireNonNull(repositoryRoot, "repositoryRoot");
    Objects.requireNonNull(sourceRoots, "sourceRoots");

    Path normalizedRepositoryRoot = repositoryRoot.toAbsolutePath().normalize();
    Path canonicalRepositoryRoot = ScanPathContainment.canonicalRoot(normalizedRepositoryRoot);
    List<RepositorySourceFile> sourceFiles = new ArrayList<>();
    Set<String> sourceDeclaredTypeNames = new LinkedHashSet<>();

    for (Path sourceRoot : sourceRoots) {
      Path normalizedSourceRoot = normalizeSourceRoot(normalizedRepositoryRoot, sourceRoot);
      if (!ScanPathContainment.isDirectoryUnderRoot(canonicalRepositoryRoot, normalizedSourceRoot)) {
        continue;
      }
      for (Path javaFile : javaFiles(canonicalRepositoryRoot, normalizedSourceRoot)) {
        RepositorySourceFile sourceFile = sourceFile(normalizedRepositoryRoot, javaFile);
        sourceFiles.add(sourceFile);
        sourceDeclaredTypeNames.addAll(sourceFile.declaredTypeNames());
      }
    }

    List<SpringRepositoryFact> repositories = new ArrayList<>();
    Map<String, SpringRepositoryEvidence> evidence = new java.util.LinkedHashMap<>();
    JavaSourceOrigins.markIncompleteSourceIndexIfNeeded(sourceDeclaredTypeNames);
    for (RepositorySourceFile sourceFile : sourceFiles) {
      analyzeJavaFile(sourceFile, sourceDeclaredTypeNames, repositories, evidence);
    }

    return new SpringRepositoryAnalysis(
        repositories.stream().sorted(REPOSITORY_ORDER).toList(),
        List.copyOf(evidence.values()));
  }

  private void analyzeJavaFile(
      RepositorySourceFile sourceFile,
      Set<String> sourceDeclaredTypeNames,
      List<SpringRepositoryFact> repositories,
      Map<String, SpringRepositoryEvidence> evidence) {
    for (ClassOrInterfaceDeclaration type : sourceFile.compilationUnit().findAll(ClassOrInterfaceDeclaration.class)) {
      String className = qualifiedClassName(sourceFile.packageName(), type);
      repositoryAnnotation(type.getAnnotations(), sourceFile.importsBySimpleName(), sourceDeclaredTypeNames)
          .ifPresent(annotation -> addDirectRepositoryFact(
              sourceFile,
              type,
              className,
              annotation,
              repositories,
              evidence));
      addSpringDataRepositorySignal(
          sourceFile,
          type,
          className,
          sourceDeclaredTypeNames,
          repositories,
          evidence);
    }
  }

  private void addDirectRepositoryFact(
      RepositorySourceFile sourceFile,
      ClassOrInterfaceDeclaration type,
      String className,
      AnnotationExpr annotation,
      List<SpringRepositoryFact> repositories,
      Map<String, SpringRepositoryEvidence> evidence) {
    SpringRepositoryEvidence annotationEvidence = annotationEvidence(
        sourceFile.sourcePath(),
        className,
        annotation,
        sourceFile.sourceLines());
    evidence.putIfAbsent(annotationEvidence.id(), annotationEvidence);

    repositories.add(new SpringRepositoryFact(
        SURFACE_CATEGORY_REPOSITORY_STEREOTYPE,
        SUPPORT_TYPE_EXTRACTED,
        className,
        sourceFile.sourcePath(),
        DIRECT_REPOSITORY_STEREOTYPE,
        List.of(),
        List.of(),
        null,
        null,
        List.of(annotationEvidence.id())));
  }

  private void addSpringDataRepositorySignal(
      RepositorySourceFile sourceFile,
      ClassOrInterfaceDeclaration type,
      String className,
      Set<String> sourceDeclaredTypeNames,
      List<SpringRepositoryFact> repositories,
      Map<String, SpringRepositoryEvidence> evidence) {
    if (!type.isInterface()) {
      return;
    }

    List<SpringDataRepositoryExtendsObservation> extendsObservations = supportedExtendsObservations(
        sourceFile,
        type,
        className,
        sourceDeclaredTypeNames);
    if (extendsObservations.isEmpty()) {
      return;
    }

    SpringRepositoryEvidence declarationEvidence = codeSymbolEvidence(
        sourceFile.sourcePath(),
        className,
        className,
        type.getRange().map(range -> range.begin.line).orElse(null),
        type.getRange().map(range -> range.end.line).orElse(null),
        EvidenceExcerpts.singleLine(
            type,
            sourceFile.sourceLines(),
            type.getRange().map(range -> range.begin.line).orElse(null)));
    evidence.putIfAbsent(declarationEvidence.id(), declarationEvidence);
    extendsObservations.stream()
        .map(SpringDataRepositoryExtendsObservation::extendsEvidence)
        .forEach(record -> evidence.putIfAbsent(record.id(), record));

    List<String> evidenceIds = new ArrayList<>();
    evidenceIds.add(declarationEvidence.id());
    extendsObservations.stream()
        .map(SpringDataRepositoryExtendsObservation::extendsEvidence)
        .map(SpringRepositoryEvidence::id)
        .forEach(evidenceIds::add);

    repositories.add(new SpringRepositoryFact(
        SURFACE_CATEGORY_SPRING_DATA_INTERFACE,
        SUPPORT_TYPE_INFERRED,
        className,
        sourceFile.sourcePath(),
        SPRING_DATA_REPOSITORY_INTERFACE_EXTENSION,
        extendsObservations.stream()
            .map(SpringDataRepositoryExtendsObservation::baseType)
            .toList(),
        extendsObservations.stream()
            .map(SpringDataRepositoryExtendsObservation::entityGeneric)
            .toList(),
        ENTITY_RELATION_NOT_ANALYZED,
        null,
        evidenceIds));
  }

  private List<SpringDataRepositoryExtendsObservation> supportedExtendsObservations(
      RepositorySourceFile sourceFile,
      ClassOrInterfaceDeclaration type,
      String className,
      Set<String> sourceDeclaredTypeNames) {
    List<SpringDataRepositoryExtendsObservation> observations = new ArrayList<>();
    Set<String> seenExtendsTypes = new LinkedHashSet<>();
    for (ClassOrInterfaceType extendedType : type.getExtendedTypes()) {
      Optional<String> supportedQualifiedName = supportedSpringDataRepositoryType(
          extendedType,
          sourceFile.importsBySimpleName(),
          sourceDeclaredTypeNames);
      if (supportedQualifiedName.isEmpty() || !seenExtendsTypes.add(supportedQualifiedName.orElseThrow())) {
        continue;
      }
      Integer lineStart = extendedType.getRange().map(range -> range.begin.line).orElse(null);
      Integer lineEnd = extendedType.getRange().map(range -> range.end.line).orElse(null);
      SpringRepositoryEvidence extendsEvidence = codeSymbolEvidence(
          sourceFile.sourcePath(),
          className,
          "extends:" + supportedQualifiedName.orElseThrow(),
          lineStart,
          lineEnd,
          EvidenceExcerpts.singleLine(extendedType, sourceFile.sourceLines(), lineStart));
      observations.add(new SpringDataRepositoryExtendsObservation(
          supportedQualifiedName.orElseThrow(),
          extendsEvidence,
          entityGeneric(sourceFile, extendedType, extendsEvidence.id())));
    }
    return observations;
  }

  private SpringRepositoryEntityGenericFact entityGeneric(
      RepositorySourceFile sourceFile,
      ClassOrInterfaceType extendedType,
      String evidenceId) {
    Optional<com.github.javaparser.ast.NodeList<Type>> typeArguments = extendedType.getTypeArguments();
    if (typeArguments.isEmpty() || typeArguments.orElseThrow().isEmpty()) {
      return new SpringRepositoryEntityGenericFact(
          null,
          null,
          ENTITY_GENERIC_UNSUPPORTED,
          List.of(evidenceId));
    }

    Type entityType = typeArguments.orElseThrow().get(0);
    Optional<String> qualifiedTypeName = supportedEntityGenericType(
        sourceFile.packageName(),
        sourceFile.importsBySimpleName(),
        entityType);
    return new SpringRepositoryEntityGenericFact(
        entityType.toString(),
        qualifiedTypeName.orElse(null),
        qualifiedTypeName.isPresent() ? ENTITY_GENERIC_SUPPORTED : ENTITY_GENERIC_UNSUPPORTED,
        List.of(evidenceId));
  }

  private Optional<String> supportedEntityGenericType(
      String packageName,
      Map<String, String> importsBySimpleName,
      Type entityType) {
    if (!entityType.isClassOrInterfaceType()) {
      return Optional.empty();
    }

    ClassOrInterfaceType classOrInterfaceType = entityType.asClassOrInterfaceType();
    if (classOrInterfaceType.getTypeArguments().isPresent()
        && !classOrInterfaceType.getTypeArguments().orElseThrow().isEmpty()) {
      return Optional.empty();
    }

    String referenceName = classOrInterfaceType.getNameWithScope();
    String simpleName = JavaSourceOrigins.simpleName(referenceName);
    if (referenceName.contains(".")) {
      return Optional.of(referenceName);
    }

    String importedQualifiedName = importsBySimpleName.get(simpleName);
    if (importedQualifiedName != null) {
      return Optional.of(importedQualifiedName);
    }

    if (packageName.isBlank()) {
      return Optional.of(simpleName);
    }
    return Optional.of(packageName + "." + simpleName);
  }

  private Optional<AnnotationExpr> repositoryAnnotation(
      List<AnnotationExpr> annotations,
      Map<String, String> importsBySimpleName,
      Set<String> sourceDeclaredTypeNames) {
    return annotations.stream()
        .filter(annotation -> JavaSourceOrigins.supportedAnnotationSimpleName(
                annotation,
                importsBySimpleName,
                SUPPORTED_REPOSITORY_ANNOTATION_ORIGINS,
                sourceDeclaredTypeNames)
            .isPresent())
        .findFirst();
  }

  private Optional<String> supportedSpringDataRepositoryType(
      ClassOrInterfaceType type,
      Map<String, String> importsBySimpleName,
      Set<String> sourceDeclaredTypeNames) {
    if (JavaSourceOrigins.isIncompleteSourceIndex(sourceDeclaredTypeNames)) {
      return Optional.empty();
    }

    String referenceName = type.getNameWithScope();
    String simpleName = JavaSourceOrigins.simpleName(referenceName);
    Set<String> supportedQualifiedNames = SUPPORTED_SPRING_DATA_REPOSITORY_BASE_TYPES.get(simpleName);
    if (supportedQualifiedNames == null) {
      return Optional.empty();
    }

    if (referenceName.contains(".")) {
      if (supportedQualifiedNames.contains(referenceName) && !sourceDeclaredTypeNames.contains(referenceName)) {
        return Optional.of(referenceName);
      }
      return Optional.empty();
    }

    String importedQualifiedName = importsBySimpleName.get(simpleName);
    if (importedQualifiedName != null
        && supportedQualifiedNames.contains(importedQualifiedName)
        && !sourceDeclaredTypeNames.contains(importedQualifiedName)) {
      return Optional.of(importedQualifiedName);
    }
    return Optional.empty();
  }

  private RepositorySourceFile sourceFile(
      Path repositoryRoot,
      Path javaFile) throws IOException {
    CompilationUnit compilationUnit = JavaSourceParser.parse(javaFile);
    String packageName = compilationUnit.getPackageDeclaration()
        .map(packageDeclaration -> packageDeclaration.getName().asString())
        .orElse("");
    return new RepositorySourceFile(
        compilationUnit,
        packageName,
        repositoryRelativePath(repositoryRoot, javaFile),
        JavaSourceParser.sourceLines(javaFile),
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
    return JavaSourceParser.javaFiles(canonicalRepositoryRoot, sourceRoot);
  }

  private SpringRepositoryEvidence annotationEvidence(
      String sourcePath,
      String className,
      AnnotationExpr annotation,
      List<String> sourceLines) {
    Integer lineStart = annotation.getRange().map(range -> range.begin.line).orElse(null);
    Integer lineEnd = annotation.getRange().map(range -> range.end.line).orElse(null);
    return new SpringRepositoryEvidence(
        evidenceId(sourcePath, className, REPOSITORY_ANNOTATION_SYMBOL, lineStart, lineEnd),
        ANNOTATION_SOURCE_TYPE,
        sourcePath,
        className,
        null,
        REPOSITORY_ANNOTATION_SYMBOL,
        lineStart,
        lineEnd,
        EvidenceExcerpts.sourceRange(annotation, sourceLines),
        HIGH_CONFIDENCE);
  }

  private SpringRepositoryEvidence codeSymbolEvidence(
      String sourcePath,
      String className,
      String symbolName,
      Integer lineStart,
      Integer lineEnd,
      String excerpt) {
    return new SpringRepositoryEvidence(
        evidenceId(sourcePath, className, symbolName, lineStart, lineEnd),
        CODE_SYMBOL_SOURCE_TYPE,
        sourcePath,
        className,
        null,
        symbolName,
        lineStart,
        lineEnd,
        EvidenceExcerpts.bounded(excerpt),
        HIGH_CONFIDENCE);
  }

  private String evidenceId(
      String sourcePath,
      String className,
      String symbolName,
      Integer lineStart,
      Integer lineEnd) {
    String lineRange = lineStart == null || lineEnd == null ? "unknown" : lineStart + "-" + lineEnd;
    return "ev:" + sourcePath + ":" + lineRange + ":" + className + ":" + symbolName;
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

  private record RepositorySourceFile(
      CompilationUnit compilationUnit,
      String packageName,
      String sourcePath,
      List<String> sourceLines,
      Map<String, String> importsBySimpleName,
      Set<String> declaredTypeNames) {
    private RepositorySourceFile {
      sourceLines = List.copyOf(sourceLines);
      importsBySimpleName = Map.copyOf(importsBySimpleName);
      declaredTypeNames = Set.copyOf(declaredTypeNames);
    }
  }

  private record SpringDataRepositoryExtendsObservation(
      String baseType,
      SpringRepositoryEvidence extendsEvidence,
      SpringRepositoryEntityGenericFact entityGeneric) {
  }
}
