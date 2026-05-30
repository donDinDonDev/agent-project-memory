package io.github.dondindondev.agentprojectmemory.analyzer.jpa;

import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import io.github.dondindondev.agentprojectmemory.analyzer.JavaSourceParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public final class JpaEntityAnalyzer {
  private static final String ENTITY = "Entity";
  private static final String TABLE = "Table";
  private static final String ID = "Id";
  private static final String HIGH_CONFIDENCE = "high";
  private static final String TARGET_RESOLUTION = "declared_type_only";
  private static final String UNCERTAINTY = "target_type_not_resolved";
  private static final List<String> RELATIONSHIP_ANNOTATIONS = List.of(
      "@ManyToOne",
      "@OneToMany",
      "@OneToOne",
      "@ManyToMany");
  private static final Comparator<JpaIdentifierFieldFact> IDENTIFIER_FIELD_ORDER = Comparator
      .comparing(JpaIdentifierFieldFact::fieldName)
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
    List<JpaEntityFact> entities = new ArrayList<>();
    List<JpaEntityEvidence> evidence = new ArrayList<>();

    for (Path sourceRoot : sourceRoots) {
      Path normalizedSourceRoot = normalizeSourceRoot(normalizedRepositoryRoot, sourceRoot);
      if (!Files.isDirectory(normalizedSourceRoot)) {
        continue;
      }

      for (Path javaFile : javaFiles(normalizedSourceRoot)) {
        analyzeJavaFile(normalizedRepositoryRoot, javaFile, entities, evidence);
      }
    }

    return new JpaEntityAnalysis(
        entities.stream().sorted(ENTITY_ORDER).toList(),
        evidence);
  }

  private void analyzeJavaFile(
      Path repositoryRoot,
      Path javaFile,
      List<JpaEntityFact> entities,
      List<JpaEntityEvidence> evidence) throws IOException {
    CompilationUnit compilationUnit = JavaSourceParser.parse(javaFile);
    String packageName = compilationUnit.getPackageDeclaration()
        .map(packageDeclaration -> packageDeclaration.getName().asString())
        .orElse("");
    String sourcePath = repositoryRelativePath(repositoryRoot, javaFile);
    List<String> sourceLines = Files.readAllLines(javaFile);

    for (ClassOrInterfaceDeclaration type : compilationUnit.findAll(ClassOrInterfaceDeclaration.class)) {
      if (type.isInterface()) {
        continue;
      }

      Optional<AnnotationExpr> entityAnnotation = findAnnotation(type.getAnnotations(), ENTITY);
      if (entityAnnotation.isEmpty()) {
        continue;
      }

      String className = qualifiedClassName(packageName, type);
      List<JpaEntityEvidence> entityEvidence = new ArrayList<>();
      entityEvidence.add(annotationEvidence(
          sourcePath,
          className,
          entityAnnotation.orElseThrow(),
          sourceLines,
          null));

      Optional<AnnotationExpr> tableAnnotation = findAnnotation(type.getAnnotations(), TABLE);
      tableAnnotation
          .map(annotation -> annotationEvidence(sourcePath, className, annotation, sourceLines, null))
          .ifPresent(entityEvidence::add);
      String tableName = tableAnnotation
          .flatMap(this::tableName)
          .orElse(null);

      List<JpaIdentifierFieldFact> identifierFields = identifierFields(
          sourcePath,
          className,
          type,
          sourceLines,
          evidence);
      List<JpaRelationshipFact> relationships = relationships(
          sourcePath,
          className,
          type,
          sourceLines,
          evidence);
      List<String> evidenceIds = entityEvidence.stream()
          .map(JpaEntityEvidence::id)
          .toList();

      entities.add(new JpaEntityFact(
          "entity:" + className,
          className,
          tableName,
          identifierFields.stream().sorted(IDENTIFIER_FIELD_ORDER).toList(),
          relationships.stream().sorted(RELATIONSHIP_ORDER).toList(),
          evidenceIds));
      evidence.addAll(entityEvidence);
    }
  }

  private List<JpaIdentifierFieldFact> identifierFields(
      String sourcePath,
      String className,
      ClassOrInterfaceDeclaration type,
      List<String> sourceLines,
      List<JpaEntityEvidence> evidence) {
    List<JpaIdentifierFieldFact> identifierFields = new ArrayList<>();

    for (FieldDeclaration field : type.getFields()) {
      Optional<AnnotationExpr> idAnnotation = findAnnotation(field.getAnnotations(), ID);
      if (idAnnotation.isEmpty()) {
        continue;
      }

      for (VariableDeclarator variable : field.getVariables()) {
        JpaEntityEvidence idEvidence = annotationEvidence(
            sourcePath,
            className,
            idAnnotation.orElseThrow(),
            sourceLines,
            "field:" + variable.getNameAsString());
        evidence.add(idEvidence);
        identifierFields.add(new JpaIdentifierFieldFact(
            variable.getNameAsString(),
            variable.getType().asString(),
            List.of(idEvidence.id())));
      }
    }

    return identifierFields;
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

  private List<Path> javaFiles(Path sourceRoot) throws IOException {
    try (Stream<Path> paths = Files.walk(sourceRoot)) {
      return paths
          .filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".java"))
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
}
