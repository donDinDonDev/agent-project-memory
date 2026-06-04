package io.github.dondindondev.agentprojectmemory.analyzer.jpa;

import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import io.github.dondindondev.agentprojectmemory.analyzer.JavaSourceParser;
import io.github.dondindondev.agentprojectmemory.analyzer.ScanPathContainment;
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
import java.util.stream.Stream;

public final class JpaEntityAnalyzer {
  private static final String ENTITY = "Entity";
  private static final String TABLE = "Table";
  private static final String ID = "Id";
  private static final String MAPPED_SUPERCLASS = "MappedSuperclass";
  private static final String HIGH_CONFIDENCE = "high";
  private static final String TARGET_RESOLUTION = "declared_type_only";
  private static final String UNCERTAINTY = "target_type_not_resolved";
  private static final String SOURCE_KIND_DECLARED = "declared";
  private static final String SOURCE_KIND_MAPPED_SUPERCLASS = "mapped_superclass";
  private static final List<String> RELATIONSHIP_ANNOTATIONS = List.of(
      "@ManyToOne",
      "@OneToMany",
      "@OneToOne",
      "@ManyToMany");
  private static final Comparator<JpaIdentifierFieldFact> IDENTIFIER_FIELD_ORDER = Comparator
      .comparing(JpaIdentifierFieldFact::sourceKind)
      .thenComparing(JpaIdentifierFieldFact::declaringClass)
      .thenComparing(JpaIdentifierFieldFact::fieldName)
      .thenComparing(JpaIdentifierFieldFact::javaType);
  private static final Comparator<JpaRelationshipFact> RELATIONSHIP_ORDER = Comparator
      .comparing(JpaRelationshipFact::fieldName)
      .thenComparing(JpaRelationshipFact::annotation)
      .thenComparing(JpaRelationshipFact::javaType);
  private static final Comparator<JpaEntityFact> ENTITY_ORDER = Comparator
      .comparing(JpaEntityFact::className)
      .thenComparing(JpaEntityFact::id);

  public JpaEntityAnalysis analyze(Path repositoryRoot, List<Path> sourceRoots) throws IOException {
    Objects.requireNonNull(repositoryRoot, "repositoryRoot");
    Objects.requireNonNull(sourceRoots, "sourceRoots");

    Path normalizedRepositoryRoot = repositoryRoot.toAbsolutePath().normalize();
    Path canonicalRepositoryRoot = ScanPathContainment.canonicalRoot(normalizedRepositoryRoot);
    List<JavaTypeSource> javaTypes = new ArrayList<>();
    List<JpaEntityFact> entities = new ArrayList<>();
    List<JpaEntityEvidence> evidence = new ArrayList<>();

    for (Path sourceRoot : sourceRoots) {
      Path normalizedSourceRoot = normalizeSourceRoot(normalizedRepositoryRoot, sourceRoot);
      if (!ScanPathContainment.isDirectoryUnderRoot(
          canonicalRepositoryRoot,
          normalizedSourceRoot)) {
        continue;
      }

      for (Path javaFile : javaFiles(canonicalRepositoryRoot, normalizedSourceRoot)) {
        javaTypes.addAll(javaTypes(normalizedRepositoryRoot, javaFile));
      }
    }

    Map<String, MappedSuperclassSource> mappedSuperclasses = mappedSuperclasses(javaTypes);
    for (JavaTypeSource javaType : javaTypes) {
      analyzeJavaType(javaType, mappedSuperclasses, entities, evidence);
    }

    return new JpaEntityAnalysis(
        entities.stream().sorted(ENTITY_ORDER).toList(),
        evidence);
  }

  private List<JavaTypeSource> javaTypes(
      Path repositoryRoot,
      Path javaFile) throws IOException {
    CompilationUnit compilationUnit = JavaSourceParser.parse(javaFile);
    String packageName = compilationUnit.getPackageDeclaration()
        .map(packageDeclaration -> packageDeclaration.getName().asString())
        .orElse("");
    String sourcePath = repositoryRelativePath(repositoryRoot, javaFile);
    List<String> sourceLines = Files.readAllLines(javaFile);
    List<JavaTypeSource> javaTypes = new ArrayList<>();

    for (ClassOrInterfaceDeclaration type : compilationUnit.findAll(ClassOrInterfaceDeclaration.class)) {
      if (type.isInterface()) {
        continue;
      }

      javaTypes.add(new JavaTypeSource(
          compilationUnit,
          packageName,
          sourcePath,
          sourceLines,
          type,
          qualifiedClassName(packageName, type)));
    }

    return javaTypes;
  }

  private Map<String, MappedSuperclassSource> mappedSuperclasses(List<JavaTypeSource> javaTypes) {
    Map<String, MappedSuperclassSource> mappedSuperclasses = new LinkedHashMap<>();

    for (JavaTypeSource javaType : javaTypes) {
      Optional<AnnotationExpr> mappedSuperclassAnnotation =
          findAnnotation(javaType.type().getAnnotations(), MAPPED_SUPERCLASS);
      if (mappedSuperclassAnnotation.isEmpty()) {
        continue;
      }

      JpaEntityEvidence mappedSuperclassEvidence = annotationEvidence(
          javaType.sourcePath(),
          javaType.className(),
          mappedSuperclassAnnotation.orElseThrow(),
          javaType.sourceLines(),
          null);
      List<JpaEntityEvidence> evidence = new ArrayList<>();
      evidence.add(mappedSuperclassEvidence);
      List<JpaIdentifierFieldFact> identifierFields = identifierFields(
          javaType,
          SOURCE_KIND_MAPPED_SUPERCLASS,
          List.of(mappedSuperclassEvidence.id()),
          evidence);

      mappedSuperclasses.put(
          javaType.className(),
          new MappedSuperclassSource(javaType, identifierFields, evidence));
    }

    return mappedSuperclasses;
  }

  private void analyzeJavaType(
      JavaTypeSource javaType,
      Map<String, MappedSuperclassSource> mappedSuperclasses,
      List<JpaEntityFact> entities,
      List<JpaEntityEvidence> evidence) {
    ClassOrInterfaceDeclaration type = javaType.type();
    Optional<AnnotationExpr> entityAnnotation = findAnnotation(type.getAnnotations(), ENTITY);
    if (entityAnnotation.isEmpty()) {
      return;
    }

    List<JpaEntityEvidence> entityEvidence = new ArrayList<>();
    entityEvidence.add(annotationEvidence(
        javaType.sourcePath(),
        javaType.className(),
        entityAnnotation.orElseThrow(),
        javaType.sourceLines(),
        null));

    Optional<AnnotationExpr> tableAnnotation = findAnnotation(type.getAnnotations(), TABLE);
    tableAnnotation
        .map(annotation -> annotationEvidence(
            javaType.sourcePath(),
            javaType.className(),
            annotation,
            javaType.sourceLines(),
            null))
        .ifPresent(entityEvidence::add);
    String tableName = tableAnnotation
        .flatMap(this::tableName)
        .orElse(null);

    List<JpaIdentifierFieldFact> identifierFields = new ArrayList<>(identifierFields(
        javaType,
        SOURCE_KIND_DECLARED,
        List.of(),
        evidence));
    MappedSuperclassTraversal mappedSuperclassTraversal = mappedSuperclassTraversal(
        javaType,
        mappedSuperclasses);
    identifierFields.addAll(mappedSuperclassTraversal.identifierFields());
    evidence.addAll(mappedSuperclassTraversal.evidence());
    List<JpaRelationshipFact> relationships = relationships(
        javaType.sourcePath(),
        javaType.className(),
        type,
        javaType.sourceLines(),
        evidence);
    List<String> evidenceIds = entityEvidence.stream()
        .map(JpaEntityEvidence::id)
        .toList();

    entities.add(new JpaEntityFact(
        "entity:" + javaType.className(),
        javaType.className(),
        tableName,
        identifierFields.stream().sorted(IDENTIFIER_FIELD_ORDER).toList(),
        relationships.stream().sorted(RELATIONSHIP_ORDER).toList(),
        evidenceIds));
    evidence.addAll(entityEvidence);
  }

  private List<JpaIdentifierFieldFact> identifierFields(
      JavaTypeSource javaType,
      String sourceKind,
      List<String> additionalEvidenceIds,
      List<JpaEntityEvidence> evidence) {
    List<JpaIdentifierFieldFact> identifierFields = new ArrayList<>();

    for (FieldDeclaration field : javaType.type().getFields()) {
      Optional<AnnotationExpr> idAnnotation = findAnnotation(field.getAnnotations(), ID);
      if (idAnnotation.isEmpty()) {
        continue;
      }

      for (VariableDeclarator variable : field.getVariables()) {
        JpaEntityEvidence idEvidence = annotationEvidence(
            javaType.sourcePath(),
            javaType.className(),
            idAnnotation.orElseThrow(),
            javaType.sourceLines(),
            "field:" + variable.getNameAsString());
        evidence.add(idEvidence);
        List<String> evidenceIds = new ArrayList<>();
        evidenceIds.add(idEvidence.id());
        evidenceIds.addAll(additionalEvidenceIds);
        identifierFields.add(new JpaIdentifierFieldFact(
            variable.getNameAsString(),
            variable.getType().asString(),
            javaType.className(),
            sourceKind,
            evidenceIds));
      }
    }

    return identifierFields;
  }

  private MappedSuperclassTraversal mappedSuperclassTraversal(
      JavaTypeSource javaType,
      Map<String, MappedSuperclassSource> mappedSuperclasses) {
    if (javaType.type().getExtendedTypes().isEmpty()) {
      return MappedSuperclassTraversal.empty();
    }

    List<JpaIdentifierFieldFact> identifierFields = new ArrayList<>();
    List<JpaEntityEvidence> evidence = new ArrayList<>();
    LinkedHashSet<String> visitedClasses = new LinkedHashSet<>();
    JavaTypeSource currentContext = javaType;
    ClassOrInterfaceType currentExtendedType = javaType.type().getExtendedTypes().get(0);

    while (true) {
      List<MappedSuperclassSource> matches = superclassCandidates(currentContext, currentExtendedType).stream()
          .map(mappedSuperclasses::get)
          .filter(Objects::nonNull)
          .distinct()
          .toList();
      if (matches.size() != 1) {
        return new MappedSuperclassTraversal(identifierFields, evidence);
      }

      MappedSuperclassSource mappedSuperclass = matches.get(0);
      if (!visitedClasses.add(mappedSuperclass.javaType().className())) {
        return MappedSuperclassTraversal.empty();
      }

      if (!mappedSuperclass.identifierFields().isEmpty()) {
        identifierFields.addAll(mappedSuperclass.identifierFields());
        evidence.addAll(mappedSuperclass.evidence());
      }

      if (mappedSuperclass.javaType().type().getExtendedTypes().isEmpty()) {
        return new MappedSuperclassTraversal(identifierFields, evidence);
      }

      currentContext = mappedSuperclass.javaType();
      currentExtendedType = mappedSuperclass.javaType().type().getExtendedTypes().get(0);
    }
  }

  private List<String> superclassCandidates(
      JavaTypeSource javaType,
      ClassOrInterfaceType extendedType) {
    LinkedHashSet<String> candidates = new LinkedHashSet<>();
    String nameWithScope = extendedType.getNameWithScope();
    if (nameWithScope.contains(".")) {
      candidates.add(nameWithScope);
      return List.copyOf(candidates);
    }

    for (ImportDeclaration importDeclaration : javaType.compilationUnit().getImports()) {
      if (importDeclaration.isStatic()) {
        continue;
      }
      String importName = importDeclaration.getNameAsString();
      if (!importDeclaration.isAsterisk() && importName.endsWith("." + nameWithScope)) {
        candidates.add(importName);
      }
    }

    if (javaType.packageName().isBlank()) {
      candidates.add(nameWithScope);
    } else {
      candidates.add(javaType.packageName() + "." + nameWithScope);
    }
    return List.copyOf(candidates);
  }

  private List<JpaRelationshipFact> relationships(
      String sourcePath,
      String className,
      ClassOrInterfaceDeclaration type,
      List<String> sourceLines,
      List<JpaEntityEvidence> evidence) {
    List<JpaRelationshipFact> relationships = new ArrayList<>();

    for (FieldDeclaration field : type.getFields()) {
      List<AnnotationExpr> relationshipAnnotations = relationshipAnnotations(field.getAnnotations());
      if (relationshipAnnotations.isEmpty()) {
        continue;
      }

      for (AnnotationExpr relationshipAnnotation : relationshipAnnotations) {
        String annotationSymbol = annotationSymbol(relationshipAnnotation);
        for (VariableDeclarator variable : field.getVariables()) {
          JpaEntityEvidence relationshipEvidence = annotationEvidence(
              sourcePath,
              className,
              relationshipAnnotation,
              sourceLines,
              "field:" + variable.getNameAsString());
          evidence.add(relationshipEvidence);
          relationships.add(new JpaRelationshipFact(
              variable.getNameAsString(),
              annotationSymbol,
              variable.getType().asString(),
              TARGET_RESOLUTION,
              UNCERTAINTY,
              List.of(relationshipEvidence.id())));
        }
      }
    }

    return relationships;
  }

  private List<AnnotationExpr> relationshipAnnotations(List<AnnotationExpr> annotations) {
    return annotations.stream()
        .filter(annotation -> RELATIONSHIP_ANNOTATIONS.contains(annotationSymbol(annotation)))
        .sorted(Comparator.comparingInt(annotation -> RELATIONSHIP_ANNOTATIONS.indexOf(
            annotationSymbol(annotation))))
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

  private Optional<AnnotationExpr> findAnnotation(List<AnnotationExpr> annotations, String simpleName) {
    return annotations.stream()
        .filter(annotation -> simpleAnnotationName(annotation).equals(simpleName))
        .findFirst();
  }

  private Optional<String> tableName(AnnotationExpr annotation) {
    if (!annotation.isNormalAnnotationExpr()) {
      return Optional.empty();
    }

    return annotation.asNormalAnnotationExpr().getPairs().stream()
        .filter(pair -> "name".equals(pair.getNameAsString()))
        .findFirst()
        .flatMap(pair -> literalStringValue(pair.getValue()));
  }

  private Optional<String> literalStringValue(Expression expression) {
    if (expression.isStringLiteralExpr()) {
      return Optional.of(expression.asStringLiteralExpr().asString());
    }
    return Optional.empty();
  }

  private JpaEntityEvidence annotationEvidence(
      String sourcePath,
      String className,
      AnnotationExpr annotation,
      List<String> sourceLines,
      String discriminator) {
    String annotationSymbol = annotationSymbol(annotation);
    Integer lineStart = annotation.getRange().map(range -> range.begin.line).orElse(null);
    Integer lineEnd = annotation.getRange().map(range -> range.end.line).orElse(null);

    return new JpaEntityEvidence(
        evidenceId(sourcePath, className, annotationSymbol, lineStart, lineEnd, discriminator),
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
      Integer lineEnd,
      String discriminator) {
    String lineRange = lineStart == null || lineEnd == null ? "unknown" : lineStart + "-" + lineEnd;
    String id = "ev:" + sourcePath + ":" + lineRange + ":" + className + ":" + annotationSymbol;
    if (discriminator == null || discriminator.isBlank()) {
      return id;
    }
    return id + ":" + discriminator;
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

  private record JavaTypeSource(
      CompilationUnit compilationUnit,
      String packageName,
      String sourcePath,
      List<String> sourceLines,
      ClassOrInterfaceDeclaration type,
      String className) {
    private JavaTypeSource {
      sourceLines = List.copyOf(sourceLines);
    }
  }

  private record MappedSuperclassSource(
      JavaTypeSource javaType,
      List<JpaIdentifierFieldFact> identifierFields,
      List<JpaEntityEvidence> evidence) {
    private MappedSuperclassSource {
      identifierFields = List.copyOf(identifierFields);
      evidence = List.copyOf(evidence);
    }
  }

  private record MappedSuperclassTraversal(
      List<JpaIdentifierFieldFact> identifierFields,
      List<JpaEntityEvidence> evidence) {
    private MappedSuperclassTraversal {
      identifierFields = List.copyOf(identifierFields);
      evidence = List.copyOf(evidence);
    }

    private static MappedSuperclassTraversal empty() {
      return new MappedSuperclassTraversal(List.of(), List.of());
    }
  }
}
