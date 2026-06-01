package io.github.dondindondev.agentprojectmemory.analyzer.jpa;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.Test;

final class JpaEntityAnalyzerTest {
  private final JpaEntityAnalyzer analyzer = new JpaEntityAnalyzer();

  @Test
  void simpleEntityIsDetected() throws Exception {
    JpaEntityAnalysis analysis = analyzeFixture();

    JpaEntityFact customer = entity(analysis, "Customer");

    assertAll(
        () -> assertEquals("entity:com.example.domain.Customer", customer.id()),
        () -> assertEquals("com.example.domain.Customer", customer.className()),
        () -> assertNull(customer.tableName()),
        () -> assertTrue(customer.evidenceIds().stream()
            .anyMatch(evidenceId -> evidence(analysis, evidenceId).annotationSymbol().equals("@Entity"))));
  }

  @Test
  void modernInstanceofPatternInEntitySourceIsParsed() throws Exception {
    JpaEntityAnalysis analysis = analyzeModernJavaFixture();

    JpaEntityFact entity = analysis.entities().stream()
        .filter(candidate -> candidate.className().equals("com.example.modern.ModernEntity"))
        .findFirst()
        .orElseThrow();

    assertAll(
        () -> assertNull(entity.tableName()),
        () -> assertEquals(List.of("id"), entity.identifierFields().stream()
            .map(JpaIdentifierFieldFact::fieldName)
            .toList()),
        () -> assertEquals(List.of("declared"), entity.identifierFields().stream()
            .map(JpaIdentifierFieldFact::sourceKind)
            .toList()));
  }

  @Test
  void tableNameIsExtractedFromDirectTableName() throws Exception {
    JpaEntityAnalysis analysis = analyzeFixture();

    JpaEntityFact order = entity(analysis, "Order");

    assertAll(
        () -> assertEquals("orders", order.tableName()),
        () -> assertTrue(order.evidenceIds().stream()
            .anyMatch(evidenceId -> evidence(analysis, evidenceId).annotationSymbol().equals("@Table"))));
  }

  @Test
  void fieldLevelIdIsDetected() throws Exception {
    JpaEntityAnalysis analysis = analyzeFixture();

    JpaIdentifierFieldFact id = identifierField(entity(analysis, "Order"), "id");
    JpaEntityEvidence idEvidence = evidence(analysis, id.evidenceIds().get(0));

    assertAll(
        () -> assertEquals("Long", id.javaType()),
        () -> assertEquals("com.example.domain.Order", id.declaringClass()),
        () -> assertEquals("declared", id.sourceKind()),
        () -> assertEquals("@Id", idEvidence.annotationSymbol()),
        () -> assertEquals("com.example.domain.Order", idEvidence.className()),
        () -> assertNull(idEvidence.methodName()),
        () -> assertTrue(idEvidence.id().endsWith(":@Id:field:id")));
  }

  @Test
  void mappedSuperclassIdentifierIsAttachedToDirectEntitySubclass() throws Exception {
    JpaEntityAnalysis analysis = analyzeMappedSuperclassFixture();

    JpaIdentifierFieldFact id = identifierField(entity(analysis, "Owner"), "id");
    List<JpaEntityEvidence> identifierEvidence = id.evidenceIds().stream()
        .map(evidenceId -> evidence(analysis, evidenceId))
        .toList();

    assertAll(
        () -> assertEquals("Long", id.javaType()),
        () -> assertEquals("com.example.domain.BaseEntity", id.declaringClass()),
        () -> assertEquals("mapped_superclass", id.sourceKind()),
        () -> assertEquals(2, id.evidenceIds().size()),
        () -> assertTrue(identifierEvidence.stream()
            .anyMatch(evidence -> evidence.annotationSymbol().equals("@Id")
                && evidence.className().equals("com.example.domain.BaseEntity")
                && evidence.sourcePath().endsWith(
                    "src/main/java/com/example/domain/MappedSuperclassEntities.java")
                && evidence.id().endsWith(
                    ":com.example.domain.BaseEntity:@Id:field:id"))),
        () -> assertTrue(identifierEvidence.stream()
            .anyMatch(evidence -> evidence.annotationSymbol().equals("@MappedSuperclass")
                && evidence.className().equals("com.example.domain.BaseEntity"))));
  }

  @Test
  void mappedSuperclassIdentifierIsAttachedThroughSourceVisibleSuperclassChain() throws Exception {
    JpaEntityAnalysis analysis = analyzeMappedSuperclassFixture();

    JpaIdentifierFieldFact id = identifierField(entity(analysis, "NamedOwner"), "id");
    List<JpaEntityEvidence> identifierEvidence = id.evidenceIds().stream()
        .map(evidenceId -> evidence(analysis, evidenceId))
        .toList();

    assertAll(
        () -> assertEquals("Long", id.javaType()),
        () -> assertEquals("com.example.domain.BaseEntity", id.declaringClass()),
        () -> assertEquals("mapped_superclass", id.sourceKind()),
        () -> assertTrue(identifierEvidence.stream()
            .anyMatch(evidence -> evidence.annotationSymbol().equals("@Id")
                && evidence.className().equals("com.example.domain.BaseEntity"))),
        () -> assertTrue(identifierEvidence.stream()
            .anyMatch(evidence -> evidence.annotationSymbol().equals("@MappedSuperclass")
                && evidence.className().equals("com.example.domain.BaseEntity"))));
  }

  @Test
  void unresolvedMappedSuperclassChainDoesNotFabricateIdentifier() throws Exception {
    JpaEntityFact brokenOwner = entity(analyzeMappedSuperclassFixture(), "BrokenOwner");

    assertTrue(brokenOwner.identifierFields().isEmpty());
  }

  @Test
  void nonMappedSuperclassDoesNotContributeIdentifier() throws Exception {
    JpaEntityFact plainOwner = entity(analyzeMappedSuperclassFixture(), "PlainOwner");

    assertTrue(plainOwner.identifierFields().isEmpty());
  }

  @Test
  void unresolvedSuperclassDoesNotFabricateIdentifier() throws Exception {
    JpaEntityFact missingOwner = entity(analyzeMappedSuperclassFixture(), "MissingOwner");

    assertTrue(missingOwner.identifierFields().isEmpty());
  }

  @Test
  void manyToOneRelationshipIsDetectedWithUnresolvedTargetType() throws Exception {
    JpaRelationshipFact relationship = relationship(entity(analyzeFixture(), "Order"), "customer");

    assertRelationship(relationship, "@ManyToOne", "Customer");
  }

  @Test
  void oneToManyRelationshipIsDetectedWithUnresolvedTargetType() throws Exception {
    JpaRelationshipFact relationship = relationship(entity(analyzeFixture(), "Order"), "lines");

    assertRelationship(relationship, "@OneToMany", "List<OrderLine>");
  }

  @Test
  void oneToOneRelationshipIsDetectedWithUnresolvedTargetType() throws Exception {
    JpaRelationshipFact relationship = relationship(entity(analyzeFixture(), "Order"), "invoice");

    assertRelationship(relationship, "@OneToOne", "Invoice");
  }

  @Test
  void manyToManyRelationshipIsDetectedWithUnresolvedTargetType() throws Exception {
    JpaRelationshipFact relationship = relationship(entity(analyzeFixture(), "Order"), "tags");

    assertRelationship(relationship, "@ManyToMany", "Set<Tag>");
  }

  @Test
  void nonEntityClassIsIgnored() throws Exception {
    JpaEntityAnalysis analysis = analyzeFixture();

    assertFalse(analysis.entities().stream()
        .anyMatch(entity -> entity.className().equals("com.example.domain.NotAnEntity")));
  }

  @Test
  void entitiesAndNestedFactsAreSortedDeterministically() throws Exception {
    JpaEntityAnalysis analysis = analyzeFixture();

    assertAll(
        () -> assertEquals(
            List.of("com.example.domain.Customer", "com.example.domain.Order"),
            analysis.entities().stream().map(JpaEntityFact::className).toList()),
        () -> assertEquals(
            List.of("customer", "invoice", "lines", "tags"),
            entity(analysis, "Order").relationships().stream()
                .map(JpaRelationshipFact::fieldName)
                .toList()));
  }

  @Test
  void evidenceIdsResolveInsideAnalysis() throws Exception {
    JpaEntityAnalysis analysis = analyzeFixture();
    Set<String> evidenceIds = new HashSet<>(analysis.evidence().stream()
        .map(JpaEntityEvidence::id)
        .toList());

    for (JpaEntityFact entity : analysis.entities()) {
      assertTrue(evidenceIds.containsAll(entity.evidenceIds()));
      for (JpaIdentifierFieldFact identifierField : entity.identifierFields()) {
        assertTrue(evidenceIds.containsAll(identifierField.evidenceIds()));
      }
      for (JpaRelationshipFact relationship : entity.relationships()) {
        assertTrue(evidenceIds.containsAll(relationship.evidenceIds()));
      }
    }
  }

  @Test
  void evidencePointsToAnnotationSourceLines() throws Exception {
    JpaEntityAnalysis analysis = analyzeFixture();
    JpaEntityEvidence tableEvidence = entity(analysis, "Order").evidenceIds().stream()
        .map(evidenceId -> evidence(analysis, evidenceId))
        .filter(evidence -> "@Table".equals(evidence.annotationSymbol()))
        .findFirst()
        .orElseThrow();

    assertAll(
        () -> assertTrue(tableEvidence.sourcePath()
            .endsWith("src/main/java/com/example/domain/JpaEntities.java")),
        () -> assertNotNull(tableEvidence.lineStart()),
        () -> assertNotNull(tableEvidence.lineEnd()),
        () -> assertTrue(tableEvidence.lineStart() > 0),
        () -> assertTrue(tableEvidence.lineEnd() >= tableEvidence.lineStart()),
        () -> assertTrue(tableEvidence.excerpt().contains("@Table(name = \"orders\")")),
        () -> assertEquals("high", tableEvidence.confidence()));
  }

  private void assertRelationship(
      JpaRelationshipFact relationship,
      String annotation,
      String javaType) {
    assertAll(
        () -> assertEquals(annotation, relationship.annotation()),
        () -> assertEquals(javaType, relationship.javaType()),
        () -> assertEquals("declared_type_only", relationship.targetResolution()),
        () -> assertEquals("target_type_not_resolved", relationship.uncertainty()),
        () -> assertEquals(1, relationship.evidenceIds().size()));
  }

  private JpaEntityAnalysis analyzeFixture() throws Exception {
    Path fixtureRoot = fixtureRoot();
    return analyzer.analyze(fixtureRoot, List.of(fixtureRoot.resolve("src/main/java")));
  }

  private JpaEntityAnalysis analyzeModernJavaFixture() throws Exception {
    Path fixtureRoot = modernJavaFixtureRoot();
    return analyzer.analyze(fixtureRoot, List.of(fixtureRoot.resolve("src/main/java")));
  }

  private JpaEntityAnalysis analyzeMappedSuperclassFixture() throws Exception {
    Path fixtureRoot = mappedSuperclassFixtureRoot();
    return analyzer.analyze(fixtureRoot, List.of(fixtureRoot.resolve("src/main/java")));
  }

  private Path fixtureRoot() throws Exception {
    return Path.of(Objects.requireNonNull(
        getClass().getResource("/fixtures/jpa-entities")).toURI());
  }

  private Path modernJavaFixtureRoot() throws Exception {
    return Path.of(Objects.requireNonNull(
        getClass().getResource("/fixtures/modern-java-syntax")).toURI());
  }

  private Path mappedSuperclassFixtureRoot() throws Exception {
    return Path.of(Objects.requireNonNull(
        getClass().getResource("/fixtures/jpa-mapped-superclass")).toURI());
  }

  private JpaEntityFact entity(JpaEntityAnalysis analysis, String simpleName) {
    return analysis.entities().stream()
        .filter(candidate -> candidate.className().equals("com.example.domain." + simpleName))
        .findFirst()
        .orElseThrow();
  }

  private JpaIdentifierFieldFact identifierField(JpaEntityFact entity, String fieldName) {
    return entity.identifierFields().stream()
        .filter(candidate -> candidate.fieldName().equals(fieldName))
        .findFirst()
        .orElseThrow();
  }

  private JpaRelationshipFact relationship(JpaEntityFact entity, String fieldName) {
    return entity.relationships().stream()
        .filter(candidate -> candidate.fieldName().equals(fieldName))
        .findFirst()
        .orElseThrow();
  }

  private JpaEntityEvidence evidence(JpaEntityAnalysis analysis, String evidenceId) {
    return analysis.evidence().stream()
        .filter(candidate -> candidate.id().equals(evidenceId))
        .findFirst()
        .orElseThrow();
  }
}
