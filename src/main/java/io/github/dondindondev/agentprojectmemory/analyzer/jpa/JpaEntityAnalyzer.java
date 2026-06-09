package io.github.dondindondev.agentprojectmemory.analyzer.jpa;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import io.github.dondindondev.agentprojectmemory.analyzer.EvidenceExcerpts;
import io.github.dondindondev.agentprojectmemory.analyzer.JavaSourceOrigins;
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
import java.util.Set;
import java.util.stream.Stream;

public final class JpaEntityAnalyzer {
  private static final String ENTITY = "Entity";
  private static final String TABLE = "Table";
  private static final String ID = "Id";
  private static final String MAPPED_SUPERCLASS = "MappedSuperclass";
  private static final String COLUMN = "Column";
  private static final String ENUMERATED = "Enumerated";
  private static final String GENERATED_VALUE = "GeneratedValue";
  private static final String VERSION = "Version";
  private static final String EMBEDDABLE = "Embeddable";
  private static final String EMBEDDED = "Embedded";
  private static final String EMBEDDED_ID = "EmbeddedId";
  private static final String ID_CLASS = "IdClass";
  private static final String JOIN_COLUMN = "JoinColumn";
  private static final String JOIN_TABLE = "JoinTable";
  private static final String HIGH_CONFIDENCE = "high";
  private static final String TARGET_RESOLUTION = "declared_type_only";
  private static final String UNCERTAINTY = "target_type_not_resolved";
  private static final String EMBEDDED_TARGET_RESOLUTION_SOURCE_VISIBLE = "source_visible_embeddable";
  private static final String EMBEDDED_UNCERTAINTY = "embeddable_target_not_resolved";
  private static final String SOURCE_KIND_DECLARED = "declared";
  private static final String SOURCE_KIND_MAPPED_SUPERCLASS = "mapped_superclass";
  private static final String IDENTIFIER_KIND_SIMPLE_ID = "simple_id";
  private static final String IDENTIFIER_KIND_EMBEDDED_ID = "embedded_id";
  private static final String PERSISTENCE_ROLE_BASIC = "basic";
  private static final String PERSISTENCE_ROLE_SIMPLE_ID = "simple_id";
  private static final String PERSISTENCE_ROLE_VERSION = "version";
  private static final String PERSISTENCE_ROLE_EMBEDDED = "embedded";
  private static final String PERSISTENCE_ROLE_EMBEDDED_ID = "embedded_id";
  private static final String SUPPORT_TYPE_INFERRED = "inferred";
  private static final String MEDIUM_CONFIDENCE = "medium";
  private static final String STATUS_NOT_ANALYZED = "not_analyzed";
  private static final String OWNERSHIP_MAPPED_BY_PRESENT = "mapped_by_present";
  private static final String OWNERSHIP_MAPPED_BY_ABSENT = "mapped_by_absent";
  private static final String OWNERSHIP_JOIN_METADATA_PRESENT = "join_metadata_present";
  private static final Map<String, Set<String>> SUPPORTED_JPA_ANNOTATION_ORIGINS = Map.ofEntries(
      Map.entry(ENTITY, Set.of("jakarta.persistence.Entity", "javax.persistence.Entity")),
      Map.entry(TABLE, Set.of("jakarta.persistence.Table", "javax.persistence.Table")),
      Map.entry(ID, Set.of("jakarta.persistence.Id", "javax.persistence.Id")),
      Map.entry(MAPPED_SUPERCLASS, Set.of(
          "jakarta.persistence.MappedSuperclass",
          "javax.persistence.MappedSuperclass")),
      Map.entry(COLUMN, Set.of("jakarta.persistence.Column", "javax.persistence.Column")),
      Map.entry(ENUMERATED, Set.of("jakarta.persistence.Enumerated", "javax.persistence.Enumerated")),
      Map.entry(GENERATED_VALUE, Set.of(
          "jakarta.persistence.GeneratedValue",
          "javax.persistence.GeneratedValue")),
      Map.entry(VERSION, Set.of("jakarta.persistence.Version", "javax.persistence.Version")),
      Map.entry(EMBEDDABLE, Set.of("jakarta.persistence.Embeddable", "javax.persistence.Embeddable")),
      Map.entry(EMBEDDED, Set.of("jakarta.persistence.Embedded", "javax.persistence.Embedded")),
      Map.entry(EMBEDDED_ID, Set.of("jakarta.persistence.EmbeddedId", "javax.persistence.EmbeddedId")),
      Map.entry(ID_CLASS, Set.of("jakarta.persistence.IdClass", "javax.persistence.IdClass")),
      Map.entry(JOIN_COLUMN, Set.of("jakarta.persistence.JoinColumn", "javax.persistence.JoinColumn")),
      Map.entry(JOIN_TABLE, Set.of("jakarta.persistence.JoinTable", "javax.persistence.JoinTable")),
      Map.entry("ManyToOne", Set.of("jakarta.persistence.ManyToOne", "javax.persistence.ManyToOne")),
      Map.entry("OneToMany", Set.of("jakarta.persistence.OneToMany", "javax.persistence.OneToMany")),
      Map.entry("OneToOne", Set.of("jakarta.persistence.OneToOne", "javax.persistence.OneToOne")),
      Map.entry("ManyToMany", Set.of("jakarta.persistence.ManyToMany", "javax.persistence.ManyToMany")));
  private static final Map<String, Set<String>> SUPPORTED_ENUM_TYPE_ORIGINS = Map.of(
      "EnumType", Set.of("jakarta.persistence.EnumType", "javax.persistence.EnumType"));
  private static final Map<String, Set<String>> SUPPORTED_GENERATION_TYPE_ORIGINS = Map.of(
      "GenerationType", Set.of("jakarta.persistence.GenerationType", "javax.persistence.GenerationType"));
  private static final Map<String, Set<String>> SUPPORTED_FETCH_TYPE_ORIGINS = Map.of(
      "FetchType", Set.of("jakarta.persistence.FetchType", "javax.persistence.FetchType"));
  private static final Map<String, Set<String>> SUPPORTED_CASCADE_TYPE_ORIGINS = Map.of(
      "CascadeType", Set.of("jakarta.persistence.CascadeType", "javax.persistence.CascadeType"));
  private static final Set<String> SUPPORTED_JPA_WILDCARD_IMPORT_PACKAGES = Set.of(
      "jakarta.persistence",
      "javax.persistence");
  private static final List<String> FIELD_ANNOTATION_ORDER = List.of(
      "@Column",
      "@Enumerated",
      "@GeneratedValue",
      "@Version",
      "@Embedded",
      "@EmbeddedId");
  private static final List<String> RELATIONSHIP_ANNOTATIONS = List.of(
      "@ManyToOne",
      "@OneToMany",
      "@OneToOne",
      "@ManyToMany");
  private static final Comparator<JpaEntityFieldFact> FIELD_ORDER = Comparator
      .comparing(JpaEntityFieldFact::declaringClass)
      .thenComparing(JpaEntityFieldFact::sourceKind)
      .thenComparing(JpaEntityFieldFact::fieldName)
      .thenComparing(JpaEntityFieldFact::javaType)
      .thenComparing(JpaEntityFieldFact::persistenceRole);
  private static final Comparator<JpaIdentifierFieldFact> IDENTIFIER_FIELD_ORDER = Comparator
      .comparing(JpaIdentifierFieldFact::sourceKind)
      .thenComparing(JpaIdentifierFieldFact::declaringClass)
      .thenComparing(JpaIdentifierFieldFact::fieldName)
      .thenComparing(JpaIdentifierFieldFact::javaType)
      .thenComparing(JpaIdentifierFieldFact::identifierKind);
  private static final Comparator<JpaRelationshipFact> RELATIONSHIP_ORDER = Comparator
      .comparing(JpaRelationshipFact::fieldName)
      .thenComparing(JpaRelationshipFact::cardinality)
      .thenComparing(JpaRelationshipFact::annotation)
      .thenComparing(JpaRelationshipFact::javaType);
  private static final Comparator<JpaEntityFact> ENTITY_ORDER = Comparator
      .comparing(JpaEntityFact::className)
      .thenComparing(JpaEntityFact::id);
  private static final Comparator<JpaEmbeddableFact> EMBEDDABLE_ORDER = Comparator
      .comparing(JpaEmbeddableFact::className)
      .thenComparing(JpaEmbeddableFact::sourcePath)
      .thenComparing(JpaEmbeddableFact::id);

  public JpaEntityAnalysis analyze(Path repositoryRoot, List<Path> sourceRoots) throws IOException {
    Objects.requireNonNull(repositoryRoot, "repositoryRoot");
    Objects.requireNonNull(sourceRoots, "sourceRoots");

    Path normalizedRepositoryRoot = repositoryRoot.toAbsolutePath().normalize();
    Path canonicalRepositoryRoot = ScanPathContainment.canonicalRoot(normalizedRepositoryRoot);
    List<JavaTypeSource> javaTypes = new ArrayList<>();
    List<JpaEntityFact> entities = new ArrayList<>();
    List<JpaEmbeddableFact> embeddables = new ArrayList<>();
    List<JpaEntityEvidence> evidence = new ArrayList<>();
    Set<String> sourceDeclaredTypeNames = new LinkedHashSet<>();

    for (Path sourceRoot : sourceRoots) {
      Path normalizedSourceRoot = normalizeSourceRoot(normalizedRepositoryRoot, sourceRoot);
      if (!ScanPathContainment.isDirectoryUnderRoot(
          canonicalRepositoryRoot,
          normalizedSourceRoot)) {
        continue;
      }

      for (Path javaFile : javaFiles(canonicalRepositoryRoot, normalizedSourceRoot)) {
        javaTypes.addAll(javaTypes(normalizedRepositoryRoot, javaFile, sourceDeclaredTypeNames));
      }
    }

    Map<String, MappedSuperclassSource> mappedSuperclasses = mappedSuperclasses(javaTypes);
    Map<String, EmbeddableSource> embeddableSources = embeddableSources(javaTypes);
    for (EmbeddableSource embeddableSource : embeddableSources.values()) {
      embeddables.add(embeddableFact(embeddableSource, embeddableSources, evidence));
    }
    for (JavaTypeSource javaType : javaTypes) {
      analyzeJavaType(javaType, mappedSuperclasses, embeddableSources, entities, evidence);
    }

    return new JpaEntityAnalysis(
        entities.stream().sorted(ENTITY_ORDER).toList(),
        embeddables.stream().sorted(EMBEDDABLE_ORDER).toList(),
        evidence);
  }

  private List<JavaTypeSource> javaTypes(
      Path repositoryRoot,
      Path javaFile,
      Set<String> sourceDeclaredTypeNames) throws IOException {
    CompilationUnit compilationUnit = JavaSourceParser.parse(javaFile);
    String packageName = compilationUnit.getPackageDeclaration()
        .map(packageDeclaration -> packageDeclaration.getName().asString())
        .orElse("");
    String sourcePath = repositoryRelativePath(repositoryRoot, javaFile);
    List<String> sourceLines = Files.readAllLines(javaFile);
    Map<String, String> importsBySimpleName = JavaSourceOrigins.singleTypeImportsBySimpleName(compilationUnit);
    Set<String> wildcardImportPackages = JavaSourceOrigins.wildcardImportPackages(compilationUnit);
    sourceDeclaredTypeNames.addAll(JavaSourceOrigins.declaredTypeNames(compilationUnit, packageName));
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
          qualifiedClassName(packageName, type),
          importsBySimpleName,
          wildcardImportPackages,
          sourceDeclaredTypeNames));
    }

    return javaTypes;
  }

  private Map<String, MappedSuperclassSource> mappedSuperclasses(List<JavaTypeSource> javaTypes) {
    Map<String, MappedSuperclassSource> mappedSuperclasses = new LinkedHashMap<>();

    for (JavaTypeSource javaType : javaTypes) {
      Optional<AnnotationExpr> mappedSuperclassAnnotation =
          findAnnotation(javaType, javaType.type().getAnnotations(), MAPPED_SUPERCLASS);
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
          Map.of(),
          evidence);

      mappedSuperclasses.put(
          javaType.className(),
          new MappedSuperclassSource(javaType, identifierFields, evidence));
    }

    return mappedSuperclasses;
  }

  private Map<String, EmbeddableSource> embeddableSources(List<JavaTypeSource> javaTypes) {
    Map<String, EmbeddableSource> embeddables = new LinkedHashMap<>();

    for (JavaTypeSource javaType : javaTypes) {
      Optional<AnnotationExpr> embeddableAnnotation =
          findAnnotation(javaType, javaType.type().getAnnotations(), EMBEDDABLE);
      if (embeddableAnnotation.isEmpty()) {
        continue;
      }

      JpaEntityEvidence embeddableEvidence = annotationEvidence(
          javaType.sourcePath(),
          javaType.className(),
          embeddableAnnotation.orElseThrow(),
          javaType.sourceLines(),
          null);
      embeddables.put(
          javaType.className(),
          new EmbeddableSource(javaType, embeddableEvidence));
    }

    return embeddables;
  }

  private JpaEmbeddableFact embeddableFact(
      EmbeddableSource embeddableSource,
      Map<String, EmbeddableSource> embeddableSources,
      List<JpaEntityEvidence> evidence) {
    JavaTypeSource javaType = embeddableSource.javaType();
    evidence.add(embeddableSource.embeddableEvidence());
    List<JpaEntityFieldFact> fields = fields(
        javaType,
        SOURCE_KIND_DECLARED,
        embeddableSources,
        evidence);
    return new JpaEmbeddableFact(
        "embeddable:" + javaType.className(),
        javaType.className(),
        javaType.sourcePath(),
        fields.stream().sorted(FIELD_ORDER).toList(),
        List.of(embeddableSource.embeddableEvidence().id()));
  }

  private void analyzeJavaType(
      JavaTypeSource javaType,
      Map<String, MappedSuperclassSource> mappedSuperclasses,
      Map<String, EmbeddableSource> embeddableSources,
      List<JpaEntityFact> entities,
      List<JpaEntityEvidence> evidence) {
    ClassOrInterfaceDeclaration type = javaType.type();
    Optional<AnnotationExpr> entityAnnotation = findAnnotation(javaType, type.getAnnotations(), ENTITY);
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

    Optional<AnnotationExpr> tableAnnotation = findAnnotation(javaType, type.getAnnotations(), TABLE);
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
    JpaIdClassFact idClass = idClass(javaType, entityEvidence);

    List<JpaIdentifierFieldFact> identifierFields = new ArrayList<>(identifierFields(
        javaType,
        SOURCE_KIND_DECLARED,
        List.of(),
        embeddableSources,
        evidence));
    MappedSuperclassTraversal mappedSuperclassTraversal = mappedSuperclassTraversal(
        javaType,
        mappedSuperclasses);
    identifierFields.addAll(mappedSuperclassTraversal.identifierFields());
    evidence.addAll(mappedSuperclassTraversal.evidence());
    List<JpaEntityFieldFact> fields = fields(
        javaType,
        SOURCE_KIND_DECLARED,
        embeddableSources,
        evidence);
    List<JpaRelationshipFact> relationships = relationships(
        javaType.sourcePath(),
        javaType.className(),
        javaType,
        evidence);
    List<String> evidenceIds = entityEvidence.stream()
        .map(JpaEntityEvidence::id)
        .toList();

    entities.add(new JpaEntityFact(
        "entity:" + javaType.className(),
        javaType.className(),
        tableName,
        idClass,
        fields.stream().sorted(FIELD_ORDER).toList(),
        identifierFields.stream().sorted(IDENTIFIER_FIELD_ORDER).toList(),
        relationships.stream().sorted(RELATIONSHIP_ORDER).toList(),
        evidenceIds));
    evidence.addAll(entityEvidence);
  }

  private JpaIdClassFact idClass(
      JavaTypeSource javaType,
      List<JpaEntityEvidence> entityEvidence) {
    Optional<AnnotationExpr> idClassAnnotation = findAnnotation(
        javaType,
        javaType.type().getAnnotations(),
        ID_CLASS);
    if (idClassAnnotation.isEmpty()) {
      return null;
    }

    JpaEntityEvidence idClassEvidence = annotationEvidence(
        javaType.sourcePath(),
        javaType.className(),
        idClassAnnotation.orElseThrow(),
        javaType.sourceLines(),
        null);
    entityEvidence.add(idClassEvidence);
    return new JpaIdClassFact(
        annotationValue(idClassAnnotation.orElseThrow())
            .flatMap(this::classLiteralType)
            .orElse(null),
        STATUS_NOT_ANALYZED,
        STATUS_NOT_ANALYZED,
        List.of(idClassEvidence.id()));
  }

  private List<JpaIdentifierFieldFact> identifierFields(
      JavaTypeSource javaType,
      String sourceKind,
      List<String> additionalEvidenceIds,
      Map<String, EmbeddableSource> embeddableSources,
      List<JpaEntityEvidence> evidence) {
    List<JpaIdentifierFieldFact> identifierFields = new ArrayList<>();

    for (FieldDeclaration field : javaType.type().getFields()) {
      Optional<AnnotationExpr> idAnnotation = findAnnotation(javaType, field.getAnnotations(), ID);
      Optional<AnnotationExpr> embeddedIdAnnotation = findAnnotation(
          javaType,
          field.getAnnotations(),
          EMBEDDED_ID);
      if (idAnnotation.isEmpty() && embeddedIdAnnotation.isEmpty()) {
        continue;
      }

      for (VariableDeclarator variable : field.getVariables()) {
        JpaEmbeddedFact embeddedId = embeddedIdAnnotation
            .map(annotation -> embeddedFact(
                javaType,
                annotation,
                variable,
                embeddableSources,
                evidence))
            .orElse(null);
        JpaEntityEvidence idEvidence = null;
        if (idAnnotation.isPresent()) {
          idEvidence = annotationEvidence(
              javaType.sourcePath(),
              javaType.className(),
              idAnnotation.orElseThrow(),
              javaType.sourceLines(),
              "field:" + variable.getNameAsString());
          evidence.add(idEvidence);
        }
        Optional<AnnotationExpr> generatedValueAnnotation = findAnnotation(
            javaType,
            field.getAnnotations(),
            GENERATED_VALUE);
        JpaGeneratedValueFact generatedValue = idAnnotation.isPresent()
            ? generatedValueAnnotation
            .map(annotation -> generatedValueFact(
                javaType,
                annotation,
                variable.getNameAsString(),
                evidence))
            .orElse(null)
            : null;
        List<String> evidenceIds = new ArrayList<>();
        if (idEvidence != null) {
          evidenceIds.add(idEvidence.id());
        }
        if (embeddedId != null) {
          evidenceIds.addAll(embeddedId.evidenceIds());
        }
        if (generatedValue != null) {
          evidenceIds.addAll(generatedValue.evidenceIds());
        }
        evidenceIds.addAll(additionalEvidenceIds);
        identifierFields.add(new JpaIdentifierFieldFact(
            variable.getNameAsString(),
            variable.getType().asString(),
            javaType.className(),
            sourceKind,
            embeddedId != null ? IDENTIFIER_KIND_EMBEDDED_ID : IDENTIFIER_KIND_SIMPLE_ID,
            generatedValue,
            evidenceIds));
      }
    }

    return identifierFields;
  }

  private List<JpaEntityFieldFact> fields(
      JavaTypeSource javaType,
      String sourceKind,
      Map<String, EmbeddableSource> embeddableSources,
      List<JpaEntityEvidence> evidence) {
    List<JpaEntityFieldFact> fields = new ArrayList<>();

    for (FieldDeclaration field : javaType.type().getFields()) {
      Optional<AnnotationExpr> columnAnnotation = findAnnotation(javaType, field.getAnnotations(), COLUMN);
      Optional<AnnotationExpr> enumeratedAnnotation = findAnnotation(javaType, field.getAnnotations(), ENUMERATED);
      Optional<AnnotationExpr> generatedValueAnnotation = findAnnotation(
          javaType,
          field.getAnnotations(),
          GENERATED_VALUE);
      Optional<AnnotationExpr> versionAnnotation = findAnnotation(javaType, field.getAnnotations(), VERSION);
      Optional<AnnotationExpr> embeddedAnnotation = findAnnotation(javaType, field.getAnnotations(), EMBEDDED);
      Optional<AnnotationExpr> embeddedIdAnnotation = findAnnotation(javaType, field.getAnnotations(), EMBEDDED_ID);
      if (columnAnnotation.isEmpty()
          && enumeratedAnnotation.isEmpty()
          && generatedValueAnnotation.isEmpty()
          && versionAnnotation.isEmpty()
          && embeddedAnnotation.isEmpty()
          && embeddedIdAnnotation.isEmpty()) {
        continue;
      }

      boolean identifier = findAnnotation(javaType, field.getAnnotations(), ID).isPresent();
      boolean embeddedId = embeddedIdAnnotation.isPresent();
      for (VariableDeclarator variable : field.getVariables()) {
        String fieldName = variable.getNameAsString();
        List<String> annotations = fieldAnnotations(
            columnAnnotation,
            enumeratedAnnotation,
            generatedValueAnnotation,
            versionAnnotation,
            embeddedAnnotation,
            embeddedIdAnnotation);
        JpaColumnFact column = columnAnnotation
            .map(annotation -> columnFact(javaType, annotation, fieldName, evidence))
            .orElse(null);
        JpaEnumeratedFact enumerated = enumeratedAnnotation
            .map(annotation -> enumeratedFact(javaType, annotation, fieldName, evidence))
            .orElse(null);
        JpaGeneratedValueFact generatedValue = generatedValueAnnotation
            .map(annotation -> generatedValueFact(javaType, annotation, fieldName, evidence))
            .orElse(null);
        JpaVersionFact version = versionAnnotation
            .map(annotation -> versionFact(javaType, annotation, fieldName, evidence))
            .orElse(null);
        JpaEmbeddedFact embedded = embeddedAnnotation
            .or(() -> embeddedIdAnnotation)
            .map(annotation -> embeddedFact(
                javaType,
                annotation,
                variable,
                embeddableSources,
                evidence))
            .orElse(null);
        List<String> evidenceIds = new ArrayList<>();
        if (column != null) {
          evidenceIds.addAll(column.evidenceIds());
        }
        if (enumerated != null) {
          evidenceIds.addAll(enumerated.evidenceIds());
        }
        if (generatedValue != null) {
          evidenceIds.addAll(generatedValue.evidenceIds());
        }
        if (version != null) {
          evidenceIds.addAll(version.evidenceIds());
        }
        if (embedded != null) {
          evidenceIds.addAll(embedded.evidenceIds());
        }
        fields.add(new JpaEntityFieldFact(
            fieldName,
            variable.getType().asString(),
            javaType.className(),
            sourceKind,
            persistenceRole(identifier, embeddedId, embeddedAnnotation.isPresent(), version != null),
            annotations,
            column,
            enumerated,
            generatedValue,
            version,
            embedded,
            evidenceIds));
      }
    }

    return fields;
  }

  private List<String> fieldAnnotations(
      Optional<AnnotationExpr> columnAnnotation,
      Optional<AnnotationExpr> enumeratedAnnotation,
      Optional<AnnotationExpr> generatedValueAnnotation,
      Optional<AnnotationExpr> versionAnnotation,
      Optional<AnnotationExpr> embeddedAnnotation,
      Optional<AnnotationExpr> embeddedIdAnnotation) {
    List<String> annotations = new ArrayList<>();
    if (columnAnnotation.isPresent()) {
      annotations.add("@Column");
    }
    if (enumeratedAnnotation.isPresent()) {
      annotations.add("@Enumerated");
    }
    if (generatedValueAnnotation.isPresent()) {
      annotations.add("@GeneratedValue");
    }
    if (versionAnnotation.isPresent()) {
      annotations.add("@Version");
    }
    if (embeddedAnnotation.isPresent()) {
      annotations.add("@Embedded");
    }
    if (embeddedIdAnnotation.isPresent()) {
      annotations.add("@EmbeddedId");
    }
    return annotations.stream()
        .sorted(Comparator.comparingInt(FIELD_ANNOTATION_ORDER::indexOf))
        .toList();
  }

  private String persistenceRole(
      boolean identifier,
      boolean embeddedId,
      boolean embedded,
      boolean version) {
    if (embeddedId) {
      return PERSISTENCE_ROLE_EMBEDDED_ID;
    }
    if (version) {
      return PERSISTENCE_ROLE_VERSION;
    }
    if (identifier) {
      return PERSISTENCE_ROLE_SIMPLE_ID;
    }
    if (embedded) {
      return PERSISTENCE_ROLE_EMBEDDED;
    }
    return PERSISTENCE_ROLE_BASIC;
  }

  private JpaColumnFact columnFact(
      JavaTypeSource javaType,
      AnnotationExpr annotation,
      String fieldName,
      List<JpaEntityEvidence> evidence) {
    JpaEntityEvidence columnEvidence = fieldAnnotationEvidence(javaType, annotation, fieldName);
    evidence.add(columnEvidence);
    return new JpaColumnFact(
        annotationNamedValue(annotation, "name").flatMap(this::literalStringValue).orElse(null),
        annotationNamedValue(annotation, "nullable").flatMap(this::literalBooleanValue).orElse(null),
        annotationNamedValue(annotation, "unique").flatMap(this::literalBooleanValue).orElse(null),
        annotationNamedValue(annotation, "length").flatMap(this::literalIntegerValue).orElse(null),
        annotationNamedValue(annotation, "precision").flatMap(this::literalIntegerValue).orElse(null),
        annotationNamedValue(annotation, "scale").flatMap(this::literalIntegerValue).orElse(null),
        annotationNamedValue(annotation, "insertable").flatMap(this::literalBooleanValue).orElse(null),
        annotationNamedValue(annotation, "updatable").flatMap(this::literalBooleanValue).orElse(null),
        List.of(columnEvidence.id()));
  }

  private JpaEnumeratedFact enumeratedFact(
      JavaTypeSource javaType,
      AnnotationExpr annotation,
      String fieldName,
      List<JpaEntityEvidence> evidence) {
    JpaEntityEvidence enumeratedEvidence = fieldAnnotationEvidence(javaType, annotation, fieldName);
    evidence.add(enumeratedEvidence);
    return new JpaEnumeratedFact(
        annotationValue(annotation)
            .flatMap(value -> supportedJpaEnumValue(javaType, value, SUPPORTED_ENUM_TYPE_ORIGINS))
            .orElse(null),
        List.of(enumeratedEvidence.id()));
  }

  private JpaGeneratedValueFact generatedValueFact(
      JavaTypeSource javaType,
      AnnotationExpr annotation,
      String fieldName,
      List<JpaEntityEvidence> evidence) {
    JpaEntityEvidence generatedValueEvidence = fieldAnnotationEvidence(javaType, annotation, fieldName);
    evidence.add(generatedValueEvidence);
    return new JpaGeneratedValueFact(
        annotationNamedValue(annotation, "strategy")
            .flatMap(value -> supportedJpaEnumValue(javaType, value, SUPPORTED_GENERATION_TYPE_ORIGINS))
            .orElse(null),
        annotationNamedValue(annotation, "generator").flatMap(this::literalStringValue).orElse(null),
        List.of(generatedValueEvidence.id()));
  }

  private JpaVersionFact versionFact(
      JavaTypeSource javaType,
      AnnotationExpr annotation,
      String fieldName,
      List<JpaEntityEvidence> evidence) {
    JpaEntityEvidence versionEvidence = fieldAnnotationEvidence(javaType, annotation, fieldName);
    evidence.add(versionEvidence);
    return new JpaVersionFact(List.of(versionEvidence.id()));
  }

  private JpaEmbeddedFact embeddedFact(
      JavaTypeSource javaType,
      AnnotationExpr annotation,
      VariableDeclarator variable,
      Map<String, EmbeddableSource> embeddableSources,
      List<JpaEntityEvidence> evidence) {
    String fieldName = variable.getNameAsString();
    JpaEntityEvidence embeddedEvidence = fieldAnnotationEvidence(javaType, annotation, fieldName);
    evidence.add(embeddedEvidence);
    Optional<String> targetClassName = uniqueEmbeddableTarget(javaType, variable, embeddableSources);
    List<String> evidenceIds = new ArrayList<>();
    evidenceIds.add(embeddedEvidence.id());
    targetClassName
        .map(embeddableSources::get)
        .map(EmbeddableSource::embeddableEvidence)
        .map(JpaEntityEvidence::id)
        .ifPresent(evidenceIds::add);

    return new JpaEmbeddedFact(
        annotationSymbol(annotation),
        variable.getType().asString(),
        targetClassName.isPresent() ? EMBEDDED_TARGET_RESOLUTION_SOURCE_VISIBLE : TARGET_RESOLUTION,
        targetClassName.orElse(null),
        targetClassName.isPresent() ? SUPPORT_TYPE_INFERRED : null,
        targetClassName.isPresent() ? MEDIUM_CONFIDENCE : null,
        targetClassName.isPresent() ? null : EMBEDDED_UNCERTAINTY,
        evidenceIds);
  }

  private Optional<String> uniqueEmbeddableTarget(
      JavaTypeSource javaType,
      VariableDeclarator variable,
      Map<String, EmbeddableSource> embeddableSources) {
    if (!variable.getType().isClassOrInterfaceType()) {
      return Optional.empty();
    }

    List<String> matches = typeCandidates(javaType, variable.getType().asClassOrInterfaceType()).stream()
        .filter(embeddableSources::containsKey)
        .distinct()
        .toList();
    if (matches.size() != 1) {
      return Optional.empty();
    }
    return Optional.of(matches.get(0));
  }

  private JpaEntityEvidence fieldAnnotationEvidence(
      JavaTypeSource javaType,
      AnnotationExpr annotation,
      String fieldName) {
    return annotationEvidence(
        javaType.sourcePath(),
        javaType.className(),
        annotation,
        javaType.sourceLines(),
        "field:" + fieldName);
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
      List<MappedSuperclassSource> matches = typeCandidates(currentContext, currentExtendedType).stream()
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

  private List<String> typeCandidates(
      JavaTypeSource javaType,
      ClassOrInterfaceType sourceType) {
    LinkedHashSet<String> candidates = new LinkedHashSet<>();
    String nameWithScope = sourceType.getNameWithScope();
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
      JavaTypeSource javaType,
      List<JpaEntityEvidence> evidence) {
    List<JpaRelationshipFact> relationships = new ArrayList<>();

    for (FieldDeclaration field : javaType.type().getFields()) {
      List<AnnotationExpr> relationshipAnnotations = relationshipAnnotations(
          javaType,
          field.getAnnotations());
      if (relationshipAnnotations.isEmpty()) {
        continue;
      }
      Optional<AnnotationExpr> joinColumnAnnotation = findAnnotation(
          javaType,
          field.getAnnotations(),
          JOIN_COLUMN);
      Optional<AnnotationExpr> joinTableAnnotation = findAnnotation(
          javaType,
          field.getAnnotations(),
          JOIN_TABLE);

      for (AnnotationExpr relationshipAnnotation : relationshipAnnotations) {
        String annotationSymbol = annotationSymbol(relationshipAnnotation);
        Optional<Expression> mappedByValue = annotationNamedValue(relationshipAnnotation, "mappedBy");
        String mappedBy = mappedByValue
            .flatMap(this::literalStringValue)
            .orElse(null);
        Boolean optional = annotationNamedValue(relationshipAnnotation, "optional")
            .flatMap(this::literalBooleanValue)
            .orElse(null);
        String fetch = annotationNamedValue(relationshipAnnotation, "fetch")
            .flatMap(value -> supportedJpaEnumValue(javaType, value, SUPPORTED_FETCH_TYPE_ORIGINS))
            .orElse(null);
        List<String> cascade = annotationNamedValue(relationshipAnnotation, "cascade")
            .map(value -> cascadeValues(javaType, value))
            .orElse(List.of());
        Boolean orphanRemoval = annotationNamedValue(relationshipAnnotation, "orphanRemoval")
            .flatMap(this::literalBooleanValue)
            .orElse(null);
        for (VariableDeclarator variable : field.getVariables()) {
          JpaEntityEvidence relationshipEvidence = annotationEvidence(
              sourcePath,
              className,
              relationshipAnnotation,
              javaType.sourceLines(),
              "field:" + variable.getNameAsString());
          evidence.add(relationshipEvidence);
          List<JpaJoinColumnFact> joinColumns = joinColumnAnnotation
              .map(annotation -> List.of(joinColumnFact(
                  javaType,
                  annotation,
                  variable.getNameAsString(),
                  evidence)))
              .orElse(List.of());
          JpaJoinTableFact joinTable = joinTableAnnotation
              .map(annotation -> joinTableFact(
                  javaType,
                  annotation,
                  variable.getNameAsString(),
                  evidence))
              .orElse(null);
          List<String> evidenceIds = new ArrayList<>();
          evidenceIds.add(relationshipEvidence.id());
          joinColumns.stream()
              .flatMap(joinColumn -> joinColumn.evidenceIds().stream())
              .forEach(evidenceIds::add);
          if (joinTable != null) {
            evidenceIds.addAll(joinTable.evidenceIds());
          }
          relationships.add(new JpaRelationshipFact(
              variable.getNameAsString(),
              annotationSymbol,
              cardinality(annotationSymbol),
              variable.getType().asString(),
              relationshipTarget(variable),
              mappedBy,
              ownershipSignal(mappedBy, mappedByValue.isPresent(), !joinColumns.isEmpty() || joinTable != null),
              optional,
              fetch,
              cascade,
              orphanRemoval,
              joinColumns,
              joinTable,
              evidenceIds));
        }
      }
    }

    return relationships;
  }

  private JpaRelationshipTargetFact relationshipTarget(VariableDeclarator variable) {
    return new JpaRelationshipTargetFact(
        variable.getType().asString(),
        TARGET_RESOLUTION,
        null,
        null,
        null,
        null,
        null,
        UNCERTAINTY,
        List.of());
  }

  private String cardinality(String annotationSymbol) {
    return switch (annotationSymbol) {
      case "@ManyToOne" -> "many_to_one";
      case "@OneToMany" -> "one_to_many";
      case "@OneToOne" -> "one_to_one";
      case "@ManyToMany" -> "many_to_many";
      default -> STATUS_NOT_ANALYZED;
    };
  }

  private String ownershipSignal(
      String mappedBy,
      boolean mappedByAttributePresent,
      boolean joinMetadataPresent) {
    if (mappedBy != null) {
      return OWNERSHIP_MAPPED_BY_PRESENT;
    }
    if (mappedByAttributePresent) {
      return STATUS_NOT_ANALYZED;
    }
    if (joinMetadataPresent) {
      return OWNERSHIP_JOIN_METADATA_PRESENT;
    }
    return OWNERSHIP_MAPPED_BY_ABSENT;
  }

  private List<String> cascadeValues(JavaTypeSource javaType, Expression expression) {
    List<String> values = new ArrayList<>();
    if (expression.isArrayInitializerExpr()) {
      expression.asArrayInitializerExpr().getValues().stream()
          .map(value -> supportedJpaEnumValue(javaType, value, SUPPORTED_CASCADE_TYPE_ORIGINS))
          .flatMap(Optional::stream)
          .forEach(value -> addDistinct(values, value));
      return values;
    }

    supportedJpaEnumValue(javaType, expression, SUPPORTED_CASCADE_TYPE_ORIGINS)
        .ifPresent(value -> addDistinct(values, value));
    return values;
  }

  private void addDistinct(List<String> values, String value) {
    if (!values.contains(value)) {
      values.add(value);
    }
  }

  private JpaJoinColumnFact joinColumnFact(
      JavaTypeSource javaType,
      AnnotationExpr annotation,
      String fieldName,
      List<JpaEntityEvidence> evidence) {
    JpaEntityEvidence joinColumnEvidence = fieldAnnotationEvidence(javaType, annotation, fieldName);
    evidence.add(joinColumnEvidence);
    return joinColumnFact(annotation, List.of(joinColumnEvidence.id()));
  }

  private JpaJoinColumnFact joinColumnFact(
      AnnotationExpr annotation,
      List<String> evidenceIds) {
    return new JpaJoinColumnFact(
        annotationNamedValue(annotation, "name").flatMap(this::literalStringValue).orElse(null),
        annotationNamedValue(annotation, "referencedColumnName").flatMap(this::literalStringValue).orElse(null),
        annotationNamedValue(annotation, "nullable").flatMap(this::literalBooleanValue).orElse(null),
        annotationNamedValue(annotation, "unique").flatMap(this::literalBooleanValue).orElse(null),
        annotationNamedValue(annotation, "insertable").flatMap(this::literalBooleanValue).orElse(null),
        annotationNamedValue(annotation, "updatable").flatMap(this::literalBooleanValue).orElse(null),
        evidenceIds);
  }

  private JpaJoinTableFact joinTableFact(
      JavaTypeSource javaType,
      AnnotationExpr annotation,
      String fieldName,
      List<JpaEntityEvidence> evidence) {
    JpaEntityEvidence joinTableEvidence = fieldAnnotationEvidence(javaType, annotation, fieldName);
    evidence.add(joinTableEvidence);
    List<String> evidenceIds = List.of(joinTableEvidence.id());
    return new JpaJoinTableFact(
        annotationNamedValue(annotation, "name").flatMap(this::literalStringValue).orElse(null),
        annotationNamedValue(annotation, "schema").flatMap(this::literalStringValue).orElse(null),
        annotationNamedValue(annotation, "catalog").flatMap(this::literalStringValue).orElse(null),
        joinTableJoinColumns(javaType, annotation, "joinColumns", evidenceIds),
        joinTableJoinColumns(javaType, annotation, "inverseJoinColumns", evidenceIds),
        evidenceIds);
  }

  private List<JpaJoinColumnFact> joinTableJoinColumns(
      JavaTypeSource javaType,
      AnnotationExpr joinTableAnnotation,
      String attributeName,
      List<String> evidenceIds) {
    return annotationNamedValue(joinTableAnnotation, attributeName)
        .map(value -> joinColumnValues(javaType, value, evidenceIds))
        .orElse(List.of());
  }

  private List<JpaJoinColumnFact> joinColumnValues(
      JavaTypeSource javaType,
      Expression expression,
      List<String> evidenceIds) {
    if (expression.isAnnotationExpr()) {
      return joinColumnValue(javaType, expression.asAnnotationExpr(), evidenceIds)
          .map(List::of)
          .orElse(List.of());
    }
    if (!expression.isArrayInitializerExpr()) {
      return List.of();
    }

    return expression.asArrayInitializerExpr().getValues().stream()
        .filter(Expression::isAnnotationExpr)
        .map(Expression::asAnnotationExpr)
        .map(annotation -> joinColumnValue(javaType, annotation, evidenceIds))
        .flatMap(Optional::stream)
        .toList();
  }

  private Optional<JpaJoinColumnFact> joinColumnValue(
      JavaTypeSource javaType,
      AnnotationExpr annotation,
      List<String> evidenceIds) {
    return supportedJpaAnnotationName(javaType, annotation)
        .filter(JOIN_COLUMN::equals)
        .map(ignored -> joinColumnFact(annotation, evidenceIds));
  }

  private List<AnnotationExpr> relationshipAnnotations(
      JavaTypeSource javaType,
      List<AnnotationExpr> annotations) {
    return annotations.stream()
        .filter(annotation -> supportedJpaAnnotationName(javaType, annotation)
            .map(name -> RELATIONSHIP_ANNOTATIONS.contains("@" + name))
            .orElse(false))
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

  private Optional<AnnotationExpr> findAnnotation(
      JavaTypeSource javaType,
      List<AnnotationExpr> annotations,
      String simpleName) {
    return annotations.stream()
        .filter(annotation -> supportedJpaAnnotationName(javaType, annotation)
            .filter(simpleName::equals)
            .isPresent())
        .findFirst();
  }

  private Optional<String> supportedJpaAnnotationName(
      JavaTypeSource javaType,
      AnnotationExpr annotation) {
    return supportedJpaTypeSimpleName(
        javaType,
        annotation.getNameAsString(),
        SUPPORTED_JPA_ANNOTATION_ORIGINS);
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

  private Optional<Expression> annotationValue(AnnotationExpr annotation) {
    if (annotation.isSingleMemberAnnotationExpr()) {
      return Optional.of(annotation.asSingleMemberAnnotationExpr().getMemberValue());
    }
    return annotationNamedValue(annotation, "value");
  }

  private Optional<Expression> annotationNamedValue(AnnotationExpr annotation, String name) {
    if (!annotation.isNormalAnnotationExpr()) {
      return Optional.empty();
    }

    return annotation.asNormalAnnotationExpr().getPairs().stream()
        .filter(pair -> name.equals(pair.getNameAsString()))
        .findFirst()
        .map(pair -> pair.getValue());
  }

  private Optional<String> literalStringValue(Expression expression) {
    if (expression.isStringLiteralExpr()) {
      return Optional.of(expression.asStringLiteralExpr().asString());
    }
    return Optional.empty();
  }

  private Optional<String> classLiteralType(Expression expression) {
    if (expression.isClassExpr()) {
      return Optional.of(expression.asClassExpr().getType().asString());
    }
    return Optional.empty();
  }

  private Optional<Boolean> literalBooleanValue(Expression expression) {
    if (expression.isBooleanLiteralExpr()) {
      return Optional.of(expression.asBooleanLiteralExpr().getValue());
    }
    return Optional.empty();
  }

  private Optional<Integer> literalIntegerValue(Expression expression) {
    if (!expression.isIntegerLiteralExpr()) {
      return Optional.empty();
    }

    try {
      return Optional.of(Integer.parseInt(expression.asIntegerLiteralExpr().getValue().replace("_", "")));
    } catch (NumberFormatException exception) {
      return Optional.empty();
    }
  }

  private Optional<String> supportedJpaEnumValue(
      JavaTypeSource javaType,
      Expression expression,
      Map<String, Set<String>> supportedOrigins) {
    if (!expression.isFieldAccessExpr()) {
      return Optional.empty();
    }

    String scope = expression.asFieldAccessExpr().getScope().toString();
    String value = expression.asFieldAccessExpr().getNameAsString();
    return supportedJpaTypeSimpleName(
            javaType,
            scope,
            supportedOrigins)
        .map(simpleName -> simpleName + "." + value);
  }

  private Optional<String> supportedJpaTypeSimpleName(
      JavaTypeSource javaType,
      String referenceName,
      Map<String, Set<String>> supportedOrigins) {
    Optional<String> exactOrSingleImport = JavaSourceOrigins.supportedTypeSimpleName(
        referenceName,
        javaType.importsBySimpleName(),
        supportedOrigins,
        javaType.sourceDeclaredTypeNames());
    if (exactOrSingleImport.isPresent()) {
      return exactOrSingleImport;
    }

    if (referenceName.contains(".")) {
      return Optional.empty();
    }

    String simpleName = JavaSourceOrigins.simpleName(referenceName);
    Set<String> supportedQualifiedNames = supportedOrigins.get(simpleName);
    if (supportedQualifiedNames == null) {
      return Optional.empty();
    }
    if (javaType.importsBySimpleName().containsKey(simpleName)) {
      return Optional.empty();
    }
    if (sourceDeclaresSamePackageType(javaType, simpleName)) {
      return Optional.empty();
    }

    List<String> wildcardCandidates = javaType.wildcardImportPackages().stream()
        .filter(SUPPORTED_JPA_WILDCARD_IMPORT_PACKAGES::contains)
        .map(packageName -> packageName + "." + simpleName)
        .filter(supportedQualifiedNames::contains)
        .filter(qualifiedName -> !javaType.sourceDeclaredTypeNames().contains(qualifiedName))
        .distinct()
        .toList();
    if (wildcardCandidates.size() == 1) {
      return Optional.of(simpleName);
    }
    return Optional.empty();
  }

  private boolean sourceDeclaresSamePackageType(JavaTypeSource javaType, String simpleName) {
    String qualifiedName = javaType.packageName().isBlank()
        ? simpleName
        : javaType.packageName() + "." + simpleName;
    return javaType.sourceDeclaredTypeNames().contains(qualifiedName);
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
    return JavaSourceOrigins.simpleAnnotationName(annotation);
  }

  private record JavaTypeSource(
      CompilationUnit compilationUnit,
      String packageName,
      String sourcePath,
      List<String> sourceLines,
      ClassOrInterfaceDeclaration type,
      String className,
      Map<String, String> importsBySimpleName,
      Set<String> wildcardImportPackages,
      Set<String> sourceDeclaredTypeNames) {
    private JavaTypeSource {
      sourceLines = List.copyOf(sourceLines);
      importsBySimpleName = Map.copyOf(importsBySimpleName);
      wildcardImportPackages = Set.copyOf(wildcardImportPackages);
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

  private record EmbeddableSource(
      JavaTypeSource javaType,
      JpaEntityEvidence embeddableEvidence) {
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
