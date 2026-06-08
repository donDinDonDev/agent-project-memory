package io.github.dondindondev.agentprojectmemory.analyzer.springapp;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
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

public final class SpringBehaviorAnalyzer {
  public static final String SURFACE_CATEGORY_TRANSACTION_BOUNDARY =
      "spring_transaction_boundary";
  public static final String SURFACE_CATEGORY_SCHEDULED_METHOD = "spring_scheduled_method";
  public static final String SURFACE_CATEGORY_EVENT_LISTENER = "spring_event_listener";
  public static final String SURFACE_CATEGORY_MESSAGING_LISTENER = "messaging_listener_signal";
  public static final String SUPPORT_TYPE_EXTRACTED = "extracted";
  public static final String DIRECT_TRANSACTIONAL_TYPE = "direct_transactional_type";
  public static final String DIRECT_TRANSACTIONAL_METHOD = "direct_transactional_method";
  public static final String DIRECT_SCHEDULED_METHOD = "direct_scheduled_method";
  public static final String DIRECT_EVENT_LISTENER_METHOD = "direct_event_listener_method";
  public static final String DIRECT_KAFKA_LISTENER_ANNOTATION =
      "direct_kafka_listener_annotation";
  public static final String DIRECT_RABBIT_LISTENER_ANNOTATION =
      "direct_rabbit_listener_annotation";
  public static final String TARGET_KIND_TYPE = "type";
  public static final String TARGET_KIND_METHOD = "method";
  public static final String LISTENER_FRAMEWORK_KAFKA = "kafka";
  public static final String LISTENER_FRAMEWORK_RABBIT = "rabbit";

  private static final String HIGH_CONFIDENCE = "high";
  private static final String ANNOTATION_SOURCE_TYPE = "annotation";
  private static final String TRANSACTIONAL = "Transactional";
  private static final String SCHEDULED = "Scheduled";
  private static final String EVENT_LISTENER = "EventListener";
  private static final String KAFKA_LISTENER = "KafkaListener";
  private static final String KAFKA_LISTENERS = "KafkaListeners";
  private static final String RABBIT_LISTENER = "RabbitListener";
  private static final String RABBIT_LISTENERS = "RabbitListeners";
  private static final Map<String, Set<String>> SUPPORTED_ANNOTATION_ORIGINS =
      Map.ofEntries(
          Map.entry(
              TRANSACTIONAL,
              Set.of("org.springframework.transaction.annotation.Transactional")),
          Map.entry(
              SCHEDULED,
              Set.of("org.springframework.scheduling.annotation.Scheduled")),
          Map.entry(
              EVENT_LISTENER,
              Set.of("org.springframework.context.event.EventListener")),
          Map.entry(
              KAFKA_LISTENER,
              Set.of("org.springframework.kafka.annotation.KafkaListener")),
          Map.entry(
              KAFKA_LISTENERS,
              Set.of("org.springframework.kafka.annotation.KafkaListeners")),
          Map.entry(
              RABBIT_LISTENER,
              Set.of("org.springframework.amqp.rabbit.annotation.RabbitListener")),
          Map.entry(
              RABBIT_LISTENERS,
              Set.of("org.springframework.amqp.rabbit.annotation.RabbitListeners")));
  private static final Comparator<SpringTransactionBoundaryFact> TRANSACTION_BOUNDARY_ORDER =
      Comparator
          .comparing(SpringTransactionBoundaryFact::sourcePath)
          .thenComparing(SpringTransactionBoundaryFact::className)
          .thenComparing(SpringBehaviorAnalyzer::nullableMethodName)
          .thenComparing(SpringTransactionBoundaryFact::targetKind)
          .thenComparing(SpringTransactionBoundaryFact::idDiscriminator);
  private static final Comparator<SpringScheduledMethodFact> SCHEDULED_METHOD_ORDER =
      Comparator
          .comparing(SpringScheduledMethodFact::sourcePath)
          .thenComparing(SpringScheduledMethodFact::className)
          .thenComparing(SpringScheduledMethodFact::methodName)
          .thenComparing(SpringScheduledMethodFact::idDiscriminator);
  private static final Comparator<SpringEventListenerFact> EVENT_LISTENER_ORDER =
      Comparator
          .comparing(SpringEventListenerFact::sourcePath)
          .thenComparing(SpringEventListenerFact::className)
          .thenComparing(SpringEventListenerFact::methodName)
          .thenComparing(SpringEventListenerFact::idDiscriminator);
  private static final Comparator<SpringMessagingListenerFact> MESSAGING_LISTENER_ORDER =
      Comparator
          .comparing(SpringMessagingListenerFact::sourcePath)
          .thenComparing(SpringMessagingListenerFact::className)
          .thenComparing(SpringBehaviorAnalyzer::nullableMethodName)
          .thenComparing(SpringMessagingListenerFact::targetKind)
          .thenComparing(SpringMessagingListenerFact::listenerFramework)
          .thenComparing(SpringMessagingListenerFact::annotationSymbol)
          .thenComparing(SpringMessagingListenerFact::idDiscriminator);

  public SpringBehaviorAnalysis analyze(Path repositoryRoot, List<Path> sourceRoots)
      throws IOException {
    Objects.requireNonNull(repositoryRoot, "repositoryRoot");
    Objects.requireNonNull(sourceRoots, "sourceRoots");

    Path normalizedRepositoryRoot = repositoryRoot.toAbsolutePath().normalize();
    Path canonicalRepositoryRoot = ScanPathContainment.canonicalRoot(normalizedRepositoryRoot);
    List<BehaviorSourceFile> sourceFiles = new ArrayList<>();
    Set<String> sourceDeclaredTypeNames = new LinkedHashSet<>();

    for (Path sourceRoot : sourceRoots) {
      Path normalizedSourceRoot = normalizeSourceRoot(normalizedRepositoryRoot, sourceRoot);
      if (!ScanPathContainment.isDirectoryUnderRoot(canonicalRepositoryRoot, normalizedSourceRoot)) {
        continue;
      }
      for (Path javaFile : javaFiles(canonicalRepositoryRoot, normalizedSourceRoot)) {
        BehaviorSourceFile sourceFile = sourceFile(normalizedRepositoryRoot, javaFile);
        sourceFiles.add(sourceFile);
        sourceDeclaredTypeNames.addAll(sourceFile.declaredTypeNames());
      }
    }

    List<SpringTransactionBoundaryFact> transactionBoundaries = new ArrayList<>();
    List<SpringScheduledMethodFact> scheduledMethods = new ArrayList<>();
    List<SpringEventListenerFact> eventListeners = new ArrayList<>();
    List<SpringMessagingListenerFact> messagingListenerSignals = new ArrayList<>();
    Map<String, SpringBehaviorEvidence> evidence = new java.util.LinkedHashMap<>();
    for (BehaviorSourceFile sourceFile : sourceFiles) {
      analyzeJavaFile(
          sourceFile,
          sourceDeclaredTypeNames,
          transactionBoundaries,
          scheduledMethods,
          eventListeners,
          messagingListenerSignals,
          evidence);
    }

    return new SpringBehaviorAnalysis(
        transactionBoundaries.stream().sorted(TRANSACTION_BOUNDARY_ORDER).toList(),
        scheduledMethods.stream().sorted(SCHEDULED_METHOD_ORDER).toList(),
        eventListeners.stream().sorted(EVENT_LISTENER_ORDER).toList(),
        messagingListenerSignals.stream().sorted(MESSAGING_LISTENER_ORDER).toList(),
        List.copyOf(evidence.values()));
  }

  private void analyzeJavaFile(
      BehaviorSourceFile sourceFile,
      Set<String> sourceDeclaredTypeNames,
      List<SpringTransactionBoundaryFact> transactionBoundaries,
      List<SpringScheduledMethodFact> scheduledMethods,
      List<SpringEventListenerFact> eventListeners,
      List<SpringMessagingListenerFact> messagingListenerSignals,
      Map<String, SpringBehaviorEvidence> evidence) {
    for (ClassOrInterfaceDeclaration type
        : sourceFile.compilationUnit().findAll(ClassOrInterfaceDeclaration.class)) {
      String className = qualifiedClassName(sourceFile.packageName(), type);
      transactionalAnnotation(
              type.getAnnotations(),
              sourceFile.importsBySimpleName(),
              sourceDeclaredTypeNames)
          .ifPresent(annotation -> addTransactionalTypeFact(
              sourceFile,
              className,
              annotation,
              transactionBoundaries,
              evidence));
      addMessagingTypeFacts(
          sourceFile,
          className,
          type.getAnnotations(),
          sourceDeclaredTypeNames,
          messagingListenerSignals,
          evidence);
      addMethodFacts(
          sourceFile,
          type,
          className,
          sourceDeclaredTypeNames,
          transactionBoundaries,
          scheduledMethods,
          eventListeners,
          messagingListenerSignals,
          evidence);
    }
  }

  private void addTransactionalTypeFact(
      BehaviorSourceFile sourceFile,
      String className,
      AnnotationExpr annotation,
      List<SpringTransactionBoundaryFact> transactionBoundaries,
      Map<String, SpringBehaviorEvidence> evidence) {
    String annotationSymbol = annotationSymbol(TRANSACTIONAL);
    SpringBehaviorEvidence annotationEvidence = annotationEvidence(
        sourceFile.sourcePath(),
        className,
        null,
        annotationSymbol,
        annotation);
    evidence.putIfAbsent(annotationEvidence.id(), annotationEvidence);

    transactionBoundaries.add(new SpringTransactionBoundaryFact(
        SURFACE_CATEGORY_TRANSACTION_BOUNDARY,
        SUPPORT_TYPE_EXTRACTED,
        className,
        null,
        sourceFile.sourcePath(),
        TARGET_KIND_TYPE,
        annotationSymbol,
        DIRECT_TRANSACTIONAL_TYPE,
        TARGET_KIND_TYPE,
        List.of(annotationEvidence.id())));
  }

  private void addMessagingTypeFacts(
      BehaviorSourceFile sourceFile,
      String className,
      List<AnnotationExpr> annotations,
      Set<String> sourceDeclaredTypeNames,
      List<SpringMessagingListenerFact> messagingListenerSignals,
      Map<String, SpringBehaviorEvidence> evidence) {
    int listenerIndex = 0;
    for (AnnotationExpr annotation : annotations) {
      Optional<String> simpleName = supportedMessagingAnnotation(
          annotation,
          sourceFile.importsBySimpleName(),
          sourceDeclaredTypeNames);
      if (simpleName.isEmpty()) {
        continue;
      }
      listenerIndex++;
      String annotationSymbol = annotationSymbol(simpleName.orElseThrow());
      SpringBehaviorEvidence annotationEvidence = annotationEvidence(
          sourceFile.sourcePath(),
          className,
          null,
          annotationSymbol,
          annotation);
      evidence.putIfAbsent(annotationEvidence.id(), annotationEvidence);
      messagingListenerSignals.add(new SpringMessagingListenerFact(
          SURFACE_CATEGORY_MESSAGING_LISTENER,
          SUPPORT_TYPE_EXTRACTED,
          className,
          null,
          sourceFile.sourcePath(),
          TARGET_KIND_TYPE,
          annotationSymbol,
          listenerFramework(simpleName.orElseThrow()),
          listenerSignal(simpleName.orElseThrow()),
          annotationDiscriminator(simpleName.orElseThrow()) + ":decl:" + ordinal(listenerIndex),
          List.of(annotationEvidence.id())));
    }
  }

  private void addMethodFacts(
      BehaviorSourceFile sourceFile,
      ClassOrInterfaceDeclaration type,
      String className,
      Set<String> sourceDeclaredTypeNames,
      List<SpringTransactionBoundaryFact> transactionBoundaries,
      List<SpringScheduledMethodFact> scheduledMethods,
      List<SpringEventListenerFact> eventListeners,
      List<SpringMessagingListenerFact> messagingListenerSignals,
      Map<String, SpringBehaviorEvidence> evidence) {
    int transactionalMethodIndex = 0;
    int scheduledMethodIndex = 0;
    int eventListenerIndex = 0;
    int messagingListenerIndex = 0;
    for (MethodDeclaration method : type.getMethods()) {
      Optional<AnnotationExpr> transactionalAnnotation = transactionalAnnotation(
          method.getAnnotations(),
          sourceFile.importsBySimpleName(),
          sourceDeclaredTypeNames);
      if (transactionalAnnotation.isPresent()) {
        transactionalMethodIndex++;
        addTransactionalMethodFact(
            sourceFile,
            className,
            method,
            transactionalAnnotation.orElseThrow(),
            transactionalMethodIndex,
            transactionBoundaries,
            evidence);
      }

      Optional<AnnotationExpr> scheduledAnnotation = scheduledAnnotation(
          method.getAnnotations(),
          sourceFile.importsBySimpleName(),
          sourceDeclaredTypeNames);
      if (scheduledAnnotation.isPresent()) {
        scheduledMethodIndex++;
        addScheduledMethodFact(
            sourceFile,
            className,
            method,
            scheduledAnnotation.orElseThrow(),
            scheduledMethodIndex,
            scheduledMethods,
            evidence);
      }

      Optional<AnnotationExpr> eventListenerAnnotation = eventListenerAnnotation(
          method.getAnnotations(),
          sourceFile.importsBySimpleName(),
          sourceDeclaredTypeNames);
      if (eventListenerAnnotation.isPresent()) {
        eventListenerIndex++;
        addEventListenerFact(
            sourceFile,
            className,
            method,
            eventListenerAnnotation.orElseThrow(),
            eventListenerIndex,
            eventListeners,
            evidence);
      }

      for (AnnotationExpr annotation : method.getAnnotations()) {
        Optional<String> simpleName = supportedMessagingAnnotation(
            annotation,
            sourceFile.importsBySimpleName(),
            sourceDeclaredTypeNames);
        if (simpleName.isEmpty()) {
          continue;
        }
        messagingListenerIndex++;
        addMessagingMethodFact(
            sourceFile,
            className,
            method,
            annotation,
            simpleName.orElseThrow(),
            messagingListenerIndex,
            messagingListenerSignals,
            evidence);
      }
    }
  }

  private void addTransactionalMethodFact(
      BehaviorSourceFile sourceFile,
      String className,
      MethodDeclaration method,
      AnnotationExpr annotation,
      int declarationIndex,
      List<SpringTransactionBoundaryFact> transactionBoundaries,
      Map<String, SpringBehaviorEvidence> evidence) {
    String annotationSymbol = annotationSymbol(TRANSACTIONAL);
    SpringBehaviorEvidence annotationEvidence = annotationEvidence(
        sourceFile.sourcePath(),
        className,
        method.getNameAsString(),
        annotationSymbol,
        annotation);
    evidence.putIfAbsent(annotationEvidence.id(), annotationEvidence);

    transactionBoundaries.add(new SpringTransactionBoundaryFact(
        SURFACE_CATEGORY_TRANSACTION_BOUNDARY,
        SUPPORT_TYPE_EXTRACTED,
        className,
        method.getNameAsString(),
        sourceFile.sourcePath(),
        TARGET_KIND_METHOD,
        annotationSymbol,
        DIRECT_TRANSACTIONAL_METHOD,
        "decl:" + ordinal(declarationIndex),
        List.of(annotationEvidence.id())));
  }

  private void addScheduledMethodFact(
      BehaviorSourceFile sourceFile,
      String className,
      MethodDeclaration method,
      AnnotationExpr annotation,
      int declarationIndex,
      List<SpringScheduledMethodFact> scheduledMethods,
      Map<String, SpringBehaviorEvidence> evidence) {
    String annotationSymbol = annotationSymbol(SCHEDULED);
    SpringBehaviorEvidence annotationEvidence = annotationEvidence(
        sourceFile.sourcePath(),
        className,
        method.getNameAsString(),
        annotationSymbol,
        annotation);
    evidence.putIfAbsent(annotationEvidence.id(), annotationEvidence);

    scheduledMethods.add(new SpringScheduledMethodFact(
        SURFACE_CATEGORY_SCHEDULED_METHOD,
        SUPPORT_TYPE_EXTRACTED,
        className,
        method.getNameAsString(),
        sourceFile.sourcePath(),
        TARGET_KIND_METHOD,
        annotationSymbol,
        DIRECT_SCHEDULED_METHOD,
        "decl:" + ordinal(declarationIndex),
        List.of(annotationEvidence.id())));
  }

  private void addEventListenerFact(
      BehaviorSourceFile sourceFile,
      String className,
      MethodDeclaration method,
      AnnotationExpr annotation,
      int declarationIndex,
      List<SpringEventListenerFact> eventListeners,
      Map<String, SpringBehaviorEvidence> evidence) {
    String annotationSymbol = annotationSymbol(EVENT_LISTENER);
    SpringBehaviorEvidence annotationEvidence = annotationEvidence(
        sourceFile.sourcePath(),
        className,
        method.getNameAsString(),
        annotationSymbol,
        annotation);
    evidence.putIfAbsent(annotationEvidence.id(), annotationEvidence);

    eventListeners.add(new SpringEventListenerFact(
        SURFACE_CATEGORY_EVENT_LISTENER,
        SUPPORT_TYPE_EXTRACTED,
        className,
        method.getNameAsString(),
        sourceFile.sourcePath(),
        TARGET_KIND_METHOD,
        annotationSymbol,
        DIRECT_EVENT_LISTENER_METHOD,
        "decl:" + ordinal(declarationIndex),
        List.of(annotationEvidence.id())));
  }

  private void addMessagingMethodFact(
      BehaviorSourceFile sourceFile,
      String className,
      MethodDeclaration method,
      AnnotationExpr annotation,
      String simpleName,
      int declarationIndex,
      List<SpringMessagingListenerFact> messagingListenerSignals,
      Map<String, SpringBehaviorEvidence> evidence) {
    String annotationSymbol = annotationSymbol(simpleName);
    SpringBehaviorEvidence annotationEvidence = annotationEvidence(
        sourceFile.sourcePath(),
        className,
        method.getNameAsString(),
        annotationSymbol,
        annotation);
    evidence.putIfAbsent(annotationEvidence.id(), annotationEvidence);

    messagingListenerSignals.add(new SpringMessagingListenerFact(
        SURFACE_CATEGORY_MESSAGING_LISTENER,
        SUPPORT_TYPE_EXTRACTED,
        className,
        method.getNameAsString(),
        sourceFile.sourcePath(),
        TARGET_KIND_METHOD,
        annotationSymbol,
        listenerFramework(simpleName),
        listenerSignal(simpleName),
        annotationDiscriminator(simpleName) + ":decl:" + ordinal(declarationIndex),
        List.of(annotationEvidence.id())));
  }

  private Optional<AnnotationExpr> transactionalAnnotation(
      List<AnnotationExpr> annotations,
      Map<String, String> importsBySimpleName,
      Set<String> sourceDeclaredTypeNames) {
    return annotation(annotations, importsBySimpleName, sourceDeclaredTypeNames, TRANSACTIONAL);
  }

  private Optional<AnnotationExpr> scheduledAnnotation(
      List<AnnotationExpr> annotations,
      Map<String, String> importsBySimpleName,
      Set<String> sourceDeclaredTypeNames) {
    return annotation(annotations, importsBySimpleName, sourceDeclaredTypeNames, SCHEDULED);
  }

  private Optional<AnnotationExpr> eventListenerAnnotation(
      List<AnnotationExpr> annotations,
      Map<String, String> importsBySimpleName,
      Set<String> sourceDeclaredTypeNames) {
    return annotation(annotations, importsBySimpleName, sourceDeclaredTypeNames, EVENT_LISTENER);
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

  private Optional<String> supportedMessagingAnnotation(
      AnnotationExpr annotation,
      Map<String, String> importsBySimpleName,
      Set<String> sourceDeclaredTypeNames) {
    Optional<String> simpleName = JavaSourceOrigins.supportedAnnotationSimpleName(
        annotation,
        importsBySimpleName,
        SUPPORTED_ANNOTATION_ORIGINS,
        sourceDeclaredTypeNames);
    return simpleName.filter(candidate -> candidate.startsWith("Kafka")
        || candidate.startsWith("Rabbit"));
  }

  private BehaviorSourceFile sourceFile(Path repositoryRoot, Path javaFile)
      throws IOException {
    CompilationUnit compilationUnit = JavaSourceParser.parse(javaFile);
    String packageName = compilationUnit.getPackageDeclaration()
        .map(packageDeclaration -> packageDeclaration.getName().asString())
        .orElse("");
    return new BehaviorSourceFile(
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

  private SpringBehaviorEvidence annotationEvidence(
      String sourcePath,
      String className,
      String methodName,
      String annotationSymbol,
      AnnotationExpr annotation) {
    Integer lineStart = annotation.getRange().map(range -> range.begin.line).orElse(null);
    Integer lineEnd = annotation.getRange().map(range -> range.end.line).orElse(null);
    return new SpringBehaviorEvidence(
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

  private String qualifiedClassName(String packageName, ClassOrInterfaceDeclaration type) {
    return type.getFullyQualifiedName()
        .orElseGet(() -> packageName.isBlank()
            ? type.getNameAsString()
            : packageName + "." + type.getNameAsString());
  }

  private static String annotationSymbol(String simpleName) {
    return "@" + simpleName;
  }

  private static String listenerFramework(String simpleName) {
    return simpleName.startsWith("Kafka") ? LISTENER_FRAMEWORK_KAFKA : LISTENER_FRAMEWORK_RABBIT;
  }

  private static String listenerSignal(String simpleName) {
    return simpleName.startsWith("Kafka")
        ? DIRECT_KAFKA_LISTENER_ANNOTATION
        : DIRECT_RABBIT_LISTENER_ANNOTATION;
  }

  private static String annotationDiscriminator(String simpleName) {
    StringBuilder discriminator = new StringBuilder("annotation:");
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

  private static String nullableMethodName(SpringTransactionBoundaryFact fact) {
    return fact.methodName() == null ? "" : fact.methodName();
  }

  private static String nullableMethodName(SpringMessagingListenerFact fact) {
    return fact.methodName() == null ? "" : fact.methodName();
  }

  private record BehaviorSourceFile(
      CompilationUnit compilationUnit,
      String packageName,
      String sourcePath,
      Map<String, String> importsBySimpleName,
      Set<String> declaredTypeNames) {
    private BehaviorSourceFile {
      importsBySimpleName = Map.copyOf(importsBySimpleName);
      declaredTypeNames = Set.copyOf(declaredTypeNames);
    }
  }
}
