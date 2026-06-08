package io.github.dondindondev.agentprojectmemory.analyzer.springapp;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

final class SpringBehaviorAnalyzerTest {
  private final SpringBehaviorAnalyzer analyzer = new SpringBehaviorAnalyzer();

  @Test
  void detectsDirectTransactionalScheduledEventAndMessagingAnnotations()
      throws Exception {
    SpringBehaviorAnalysis analysis = analyzeFixture("spring-behavior-signals");

    SpringTransactionBoundaryFact typeBoundary = transactionBoundary(
        analysis,
        "com.example.behavior.BehaviorSignals",
        null);
    SpringTransactionBoundaryFact methodBoundary = transactionBoundary(
        analysis,
        "com.example.behavior.BehaviorSignals",
        "saveOrder");
    SpringTransactionBoundaryFact fqcnMethodBoundary = transactionBoundary(
        analysis,
        "com.example.behavior.BehaviorSignals",
        "fullyQualifiedTransaction");
    SpringScheduledMethodFact scheduledMethod = scheduledMethod(
        analysis,
        "com.example.behavior.BehaviorSignals",
        "refreshProjection");
    SpringEventListenerFact eventListener = eventListener(
        analysis,
        "com.example.behavior.BehaviorSignals",
        "onInventoryChanged");
    SpringMessagingListenerFact kafkaTypeListener = messagingListener(
        analysis,
        "com.example.behavior.BehaviorSignals",
        null,
        "@KafkaListener");
    SpringMessagingListenerFact rabbitTypeListener = messagingListener(
        analysis,
        "com.example.behavior.TypeLevelRabbitListener",
        null,
        "@RabbitListener");
    SpringMessagingListenerFact kafkaMethodListener = messagingListener(
        analysis,
        "com.example.behavior.BehaviorSignals",
        "onKafkaOrder",
        "@KafkaListener");
    SpringMessagingListenerFact rabbitMethodListener = messagingListener(
        analysis,
        "com.example.behavior.BehaviorSignals",
        "onRabbitOrder",
        "@RabbitListener");
    SpringMessagingListenerFact kafkaContainerListener = messagingListener(
        analysis,
        "com.example.behavior.BehaviorSignals",
        "onKafkaBatch",
        "@KafkaListeners");
    SpringMessagingListenerFact rabbitContainerListener = messagingListener(
        analysis,
        "com.example.behavior.BehaviorSignals",
        "onRabbitBatch",
        "@RabbitListeners");

    assertAll(
        () -> assertEquals("spring_transaction_boundary", typeBoundary.surfaceCategory()),
        () -> assertEquals("extracted", typeBoundary.supportType()),
        () -> assertEquals("type", typeBoundary.targetKind()),
        () -> assertEquals("@Transactional", typeBoundary.annotationSymbol()),
        () -> assertEquals("direct_transactional_type", typeBoundary.transactionSignal()),
        () -> assertEquals("method", methodBoundary.targetKind()),
        () -> assertEquals("direct_transactional_method", methodBoundary.transactionSignal()),
        () -> assertEquals("direct_transactional_method", fqcnMethodBoundary.transactionSignal()),
        () -> assertEquals("spring_scheduled_method", scheduledMethod.surfaceCategory()),
        () -> assertEquals("direct_scheduled_method", scheduledMethod.scheduledSignal()),
        () -> assertEquals("@Scheduled", scheduledMethod.annotationSymbol()),
        () -> assertEquals("spring_event_listener", eventListener.surfaceCategory()),
        () -> assertEquals("direct_event_listener_method", eventListener.eventListenerSignal()),
        () -> assertEquals("@EventListener", eventListener.annotationSymbol()),
        () -> assertEquals("type", kafkaTypeListener.targetKind()),
        () -> assertEquals("kafka", kafkaTypeListener.listenerFramework()),
        () -> assertEquals("direct_kafka_listener_annotation", kafkaTypeListener.listenerSignal()),
        () -> assertEquals("type", rabbitTypeListener.targetKind()),
        () -> assertEquals("rabbit", rabbitTypeListener.listenerFramework()),
        () -> assertEquals("direct_rabbit_listener_annotation", rabbitTypeListener.listenerSignal()),
        () -> assertEquals("method", kafkaMethodListener.targetKind()),
        () -> assertEquals("method", rabbitMethodListener.targetKind()),
        () -> assertEquals("@KafkaListeners", kafkaContainerListener.annotationSymbol()),
        () -> assertEquals("@RabbitListeners", rabbitContainerListener.annotationSymbol()));
  }

  @Test
  void ignoresWildcardOnlyAndUnresolvedSimpleAnnotations() throws Exception {
    SpringBehaviorAnalysis analysis = analyzeFixture("spring-behavior-signals");

    assertAll(
        () -> assertFalse(hasTransactionBoundary(
            analysis,
            "com.example.behavior.WildcardOnlyBehaviorSignals",
            null)),
        () -> assertFalse(hasTransactionBoundary(
            analysis,
            "com.example.behavior.UnresolvedBehaviorSignals",
            null)),
        () -> assertFalse(hasScheduledMethod(
            analysis,
            "com.example.behavior.WildcardOnlyBehaviorSignals",
            "scheduled")),
        () -> assertFalse(hasScheduledMethod(
            analysis,
            "com.example.behavior.UnresolvedBehaviorSignals",
            "scheduled")),
        () -> assertFalse(hasEventListener(
            analysis,
            "com.example.behavior.WildcardOnlyBehaviorSignals",
            "event")),
        () -> assertFalse(hasEventListener(
            analysis,
            "com.example.behavior.UnresolvedBehaviorSignals",
            "event")),
        () -> assertFalse(hasMessagingListener(
            analysis,
            "com.example.behavior.WildcardOnlyBehaviorSignals",
            "kafka",
            "@KafkaListener")),
        () -> assertFalse(hasMessagingListener(
            analysis,
            "com.example.behavior.UnresolvedBehaviorSignals",
            "rabbit",
            "@RabbitListener")));
  }

  @Test
  void sourceDeclaredSpringOriginsDoNotEmitBehaviorOrMessagingFacts() throws Exception {
    SpringBehaviorAnalysis analysis = analyzeFixture("spring-behavior-spoofed-origins");

    assertAll(
        () -> assertTrue(analysis.transactionBoundaries().isEmpty()),
        () -> assertTrue(analysis.scheduledMethods().isEmpty()),
        () -> assertTrue(analysis.eventListeners().isEmpty()),
        () -> assertTrue(analysis.messagingListenerSignals().isEmpty()),
        () -> assertTrue(analysis.evidence().isEmpty()));
  }

  @Test
  void evidenceIdsResolveToAnnotationEvidenceWithoutAnnotationAttributeExcerpts()
      throws Exception {
    SpringBehaviorAnalysis analysis = analyzeFixture("spring-behavior-signals");

    for (SpringTransactionBoundaryFact boundary : analysis.transactionBoundaries()) {
      assertSingleAnnotationEvidence(
          analysis,
          boundary.evidenceIds(),
          boundary.className(),
          boundary.methodName(),
          boundary.annotationSymbol());
    }
    for (SpringScheduledMethodFact scheduledMethod : analysis.scheduledMethods()) {
      assertSingleAnnotationEvidence(
          analysis,
          scheduledMethod.evidenceIds(),
          scheduledMethod.className(),
          scheduledMethod.methodName(),
          scheduledMethod.annotationSymbol());
    }
    for (SpringEventListenerFact eventListener : analysis.eventListeners()) {
      assertSingleAnnotationEvidence(
          analysis,
          eventListener.evidenceIds(),
          eventListener.className(),
          eventListener.methodName(),
          eventListener.annotationSymbol());
    }
    for (SpringMessagingListenerFact listener : analysis.messagingListenerSignals()) {
      assertSingleAnnotationEvidence(
          analysis,
          listener.evidenceIds(),
          listener.className(),
          listener.methodName(),
          listener.annotationSymbol());
    }

    assertAll(
        () -> assertTrue(analysis.evidence().stream()
            .allMatch(evidence -> evidence.excerpt().equals(evidence.symbolName()))),
        () -> assertTrue(analysis.evidence().stream()
            .noneMatch(evidence -> evidence.excerpt().contains("orders"))),
        () -> assertTrue(analysis.evidence().stream()
            .noneMatch(evidence -> evidence.excerpt().contains("audit"))),
        () -> assertTrue(analysis.evidence().stream()
            .noneMatch(evidence -> evidence.excerpt().contains("cron"))));
  }

  @Test
  void behaviorAndMessagingFactsAreSortedDeterministically() throws Exception {
    SpringBehaviorAnalysis analysis = analyzeFixture("spring-behavior-signals");

    assertAll(
        () -> assertEquals(
            List.of(
                "com.example.behavior.BehaviorSignals#<type>",
                "com.example.behavior.BehaviorSignals#fullyQualifiedTransaction",
                "com.example.behavior.BehaviorSignals#saveOrder",
                "com.example.behavior.TypeLevelRabbitListener#handle"),
            analysis.transactionBoundaries().stream()
                .map(boundary -> boundary.className()
                    + "#"
                    + (boundary.methodName() == null ? "<type>" : boundary.methodName()))
                .toList()),
        () -> assertEquals(
            List.of("com.example.behavior.BehaviorSignals#refreshProjection"),
            analysis.scheduledMethods().stream()
                .map(method -> method.className() + "#" + method.methodName())
                .toList()),
        () -> assertEquals(
            List.of("com.example.behavior.BehaviorSignals#onInventoryChanged"),
            analysis.eventListeners().stream()
                .map(listener -> listener.className() + "#" + listener.methodName())
                .toList()),
        () -> assertEquals(
            List.of(
                "com.example.behavior.BehaviorSignals#<type>:@KafkaListener",
                "com.example.behavior.BehaviorSignals#onKafkaBatch:@KafkaListeners",
                "com.example.behavior.BehaviorSignals#onKafkaOrder:@KafkaListener",
                "com.example.behavior.BehaviorSignals#onRabbitBatch:@RabbitListeners",
                "com.example.behavior.BehaviorSignals#onRabbitOrder:@RabbitListener",
                "com.example.behavior.TypeLevelRabbitListener#<type>:@RabbitListener"),
            analysis.messagingListenerSignals().stream()
                .map(listener -> listener.className()
                    + "#"
                    + (listener.methodName() == null ? "<type>" : listener.methodName())
                    + ":"
                    + listener.annotationSymbol())
                .toList()));
  }

  private SpringBehaviorAnalysis analyzeFixture(String fixtureName) throws Exception {
    Path fixtureRoot = Path.of(Objects.requireNonNull(
        getClass().getResource("/fixtures/" + fixtureName)).toURI());
    return analyzer.analyze(fixtureRoot, List.of(fixtureRoot.resolve("src/main/java")));
  }

  private SpringTransactionBoundaryFact transactionBoundary(
      SpringBehaviorAnalysis analysis,
      String className,
      String methodName) {
    return analysis.transactionBoundaries().stream()
        .filter(candidate -> candidate.className().equals(className))
        .filter(candidate -> Objects.equals(candidate.methodName(), methodName))
        .findFirst()
        .orElseThrow();
  }

  private boolean hasTransactionBoundary(
      SpringBehaviorAnalysis analysis,
      String className,
      String methodName) {
    return analysis.transactionBoundaries().stream()
        .anyMatch(candidate -> candidate.className().equals(className)
            && Objects.equals(candidate.methodName(), methodName));
  }

  private SpringScheduledMethodFact scheduledMethod(
      SpringBehaviorAnalysis analysis,
      String className,
      String methodName) {
    return analysis.scheduledMethods().stream()
        .filter(candidate -> candidate.className().equals(className))
        .filter(candidate -> candidate.methodName().equals(methodName))
        .findFirst()
        .orElseThrow();
  }

  private boolean hasScheduledMethod(
      SpringBehaviorAnalysis analysis,
      String className,
      String methodName) {
    return analysis.scheduledMethods().stream()
        .anyMatch(candidate -> candidate.className().equals(className)
            && candidate.methodName().equals(methodName));
  }

  private SpringEventListenerFact eventListener(
      SpringBehaviorAnalysis analysis,
      String className,
      String methodName) {
    return analysis.eventListeners().stream()
        .filter(candidate -> candidate.className().equals(className))
        .filter(candidate -> candidate.methodName().equals(methodName))
        .findFirst()
        .orElseThrow();
  }

  private boolean hasEventListener(
      SpringBehaviorAnalysis analysis,
      String className,
      String methodName) {
    return analysis.eventListeners().stream()
        .anyMatch(candidate -> candidate.className().equals(className)
            && candidate.methodName().equals(methodName));
  }

  private SpringMessagingListenerFact messagingListener(
      SpringBehaviorAnalysis analysis,
      String className,
      String methodName,
      String annotationSymbol) {
    return analysis.messagingListenerSignals().stream()
        .filter(candidate -> candidate.className().equals(className))
        .filter(candidate -> Objects.equals(candidate.methodName(), methodName))
        .filter(candidate -> candidate.annotationSymbol().equals(annotationSymbol))
        .findFirst()
        .orElseThrow();
  }

  private boolean hasMessagingListener(
      SpringBehaviorAnalysis analysis,
      String className,
      String methodName,
      String annotationSymbol) {
    return analysis.messagingListenerSignals().stream()
        .anyMatch(candidate -> candidate.className().equals(className)
            && Objects.equals(candidate.methodName(), methodName)
            && candidate.annotationSymbol().equals(annotationSymbol));
  }

  private void assertSingleAnnotationEvidence(
      SpringBehaviorAnalysis analysis,
      List<String> evidenceIds,
      String className,
      String methodName,
      String annotationSymbol) {
    assertEquals(1, evidenceIds.size());
    SpringBehaviorEvidence evidence = evidence(analysis, evidenceIds.get(0));
    assertAll(
        () -> assertEquals("annotation", evidence.sourceType()),
        () -> assertEquals(className, evidence.className()),
        () -> assertEquals(methodName, evidence.methodName()),
        () -> assertEquals(annotationSymbol, evidence.symbolName()),
        () -> assertEquals(annotationSymbol, evidence.excerpt()),
        () -> assertTrue(evidence.sourcePath().startsWith("src/main/java/")),
        () -> assertNotNull(evidence.lineStart()),
        () -> assertNotNull(evidence.lineEnd()),
        () -> assertEquals("high", evidence.confidence()));
    if (methodName == null) {
      assertNull(evidence.methodName());
    }
  }

  private SpringBehaviorEvidence evidence(
      SpringBehaviorAnalysis analysis,
      String evidenceId) {
    return analysis.evidence().stream()
        .filter(candidate -> candidate.id().equals(evidenceId))
        .findFirst()
        .orElseThrow();
  }
}
