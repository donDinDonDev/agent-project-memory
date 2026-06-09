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
        () -> assertEquals("simple_id", id.identifierKind()),
        () -> assertEquals("@Id", idEvidence.annotationSymbol()),
        () -> assertEquals("com.example.domain.Order", idEvidence.className()),
        () -> assertNull(idEvidence.methodName()),
        () -> assertTrue(idEvidence.id().endsWith(":@Id:field:id")));
  }

  @Test
  void commonFieldAnnotationsAreExtractedWithSourceVisibleAttributes() throws Exception {
    JpaEntityAnalysis analysis = analyzeFixture();
    JpaEntityFact order = entity(analysis, "Order");

    JpaEntityFieldFact id = field(order, "id");
    JpaEntityFieldFact status = field(order, "status");
    JpaEntityFieldFact version = field(order, "version");
    JpaIdentifierFieldFact identifier = identifierField(order, "id");

    assertAll(
        () -> assertEquals(List.of("@GeneratedValue"), id.annotations()),
        () -> assertEquals("simple_id", id.persistenceRole()),
        () -> assertNotNull(id.generatedValue()),
        () -> assertEquals("GenerationType.IDENTITY", id.generatedValue().strategy()),
        () -> assertEquals("order-id", id.generatedValue().generator()),
        () -> assertEquals("GenerationType.IDENTITY", identifier.generatedValue().strategy()),
        () -> assertEquals("order-id", identifier.generatedValue().generator()),
        () -> assertTrue(identifier.evidenceIds().stream()
            .anyMatch(evidenceId -> evidence(analysis, evidenceId).annotationSymbol().equals("@GeneratedValue"))),
        () -> assertEquals(List.of("@Column", "@Enumerated"), status.annotations()),
        () -> assertEquals("basic", status.persistenceRole()),
        () -> assertEquals("OrderStatus", status.javaType()),
        () -> assertEquals("status_code", status.column().name()),
        () -> assertEquals(false, status.column().nullable()),
        () -> assertEquals(true, status.column().unique()),
        () -> assertEquals(24, status.column().length()),
        () -> assertEquals(10, status.column().precision()),
        () -> assertEquals(2, status.column().scale()),
        () -> assertEquals(false, status.column().insertable()),
        () -> assertEquals(true, status.column().updatable()),
        () -> assertEquals("EnumType.STRING", status.enumerated().value()),
        () -> assertEquals(List.of("@Version"), version.annotations()),
        () -> assertEquals("version", version.persistenceRole()),
        () -> assertNotNull(version.version()),
        () -> assertTrue(version.evidenceIds().stream()
            .map(evidenceId -> evidence(analysis, evidenceId).annotationSymbol())
            .toList()
            .contains("@Version")));
  }

  @Test
  void getterPropertyAccessAnnotationsAreSkippedForCurrentFieldOnlySlice() throws Exception {
    JpaEntityFact propertyAccessEntity = entity(analyzeFixture(), "PropertyAccessEntity");

    assertTrue(propertyAccessEntity.fields().isEmpty());
  }

  @Test
  void embeddableClassesAreDetectedSeparatelyFromEntities() throws Exception {
    JpaEntityAnalysis analysis = analyzeFixture();

    JpaEmbeddableFact address = embeddable(analysis, "Address");
    JpaEntityFieldFact zipCode = field(address, "zipCode");

    assertAll(
        () -> assertEquals("embeddable:com.example.domain.Address", address.id()),
        () -> assertEquals("com.example.domain.Address", address.className()),
        () -> assertTrue(address.sourcePath().endsWith("src/main/java/com/example/domain/JpaEntities.java")),
        () -> assertEquals(List.of("@Column"), zipCode.annotations()),
        () -> assertEquals("zip_code", zipCode.column().name()),
        () -> assertTrue(address.evidenceIds().stream()
            .anyMatch(evidenceId -> evidence(analysis, evidenceId).annotationSymbol().equals("@Embeddable"))),
        () -> assertFalse(analysis.entities().stream()
            .anyMatch(entity -> entity.className().equals(address.className()))));
  }

  @Test
  void embeddedFieldLinksUniqueSourceVisibleEmbeddableTarget() throws Exception {
    JpaEntityAnalysis analysis = analyzeFixture();

    JpaEntityFieldFact destination = field(entity(analysis, "Shipment"), "destination");

    assertAll(
        () -> assertEquals(List.of("@Embedded"), destination.annotations()),
        () -> assertEquals("embedded", destination.persistenceRole()),
        () -> assertNotNull(destination.embedded()),
        () -> assertEquals("@Embedded", destination.embedded().annotation()),
        () -> assertEquals("Address", destination.embedded().javaType()),
        () -> assertEquals("source_visible_embeddable", destination.embedded().targetResolution()),
        () -> assertEquals("com.example.domain.Address", destination.embedded().targetClassName()),
        () -> assertEquals("inferred", destination.embedded().supportType()),
        () -> assertEquals("medium", destination.embedded().confidence()),
        () -> assertNull(destination.embedded().uncertainty()),
        () -> assertTrue(destination.embedded().evidenceIds().stream()
            .map(evidenceId -> evidence(analysis, evidenceId).annotationSymbol())
            .toList()
            .containsAll(List.of("@Embedded", "@Embeddable"))));
  }

  @Test
  void embeddedIdFieldIsEmittedAsPartialIdentifierSignal() throws Exception {
    JpaEntityAnalysis analysis = analyzeFixture();

    JpaEntityFact shipment = entity(analysis, "Shipment");
    JpaEntityFieldFact idField = field(shipment, "id");
    JpaIdentifierFieldFact identifier = identifierField(shipment, "id");

    assertAll(
        () -> assertEquals(List.of("@EmbeddedId"), idField.annotations()),
        () -> assertEquals("embedded_id", idField.persistenceRole()),
        () -> assertNotNull(idField.embedded()),
        () -> assertEquals("@EmbeddedId", idField.embedded().annotation()),
        () -> assertEquals("ShipmentId", idField.embedded().javaType()),
        () -> assertEquals("source_visible_embeddable", idField.embedded().targetResolution()),
        () -> assertEquals("com.example.domain.ShipmentId", idField.embedded().targetClassName()),
        () -> assertEquals("embedded_id", identifier.identifierKind()),
        () -> assertEquals("ShipmentId", identifier.javaType()),
        () -> assertNull(identifier.generatedValue()),
        () -> assertTrue(identifier.evidenceIds().stream()
            .map(evidenceId -> evidence(analysis, evidenceId).annotationSymbol())
            .toList()
            .contains("@EmbeddedId")));
  }

  @Test
  void unresolvedEmbeddedTargetKeepsDeclaredTypeOnlyUncertainty() throws Exception {
    JpaEntityFieldFact address = field(entity(analyzeFixture(), "ExternalProfile"), "address");

    assertAll(
        () -> assertEquals(List.of("@Embedded"), address.annotations()),
        () -> assertEquals("declared_type_only", address.embedded().targetResolution()),
        () -> assertNull(address.embedded().targetClassName()),
        () -> assertNull(address.embedded().supportType()),
        () -> assertNull(address.embedded().confidence()),
        () -> assertEquals("embeddable_target_not_resolved", address.embedded().uncertainty()));
  }

  @Test
  void idClassSignalIsDetectedWithoutCompositeKeyReconstruction() throws Exception {
    JpaEntityAnalysis analysis = analyzeFixture();
    JpaEntityFact legacyOrder = entity(analysis, "LegacyOrder");

    assertAll(
        () -> assertNotNull(legacyOrder.idClass()),
        () -> assertEquals("LegacyOrderKey", legacyOrder.idClass().typeName()),
        () -> assertEquals("not_analyzed", legacyOrder.idClass().fieldMatchingStatus()),
        () -> assertEquals("not_analyzed", legacyOrder.idClass().semanticReconstructionStatus()),
        () -> assertEquals(
            List.of("orderNumber", "tenantId"),
            legacyOrder.identifierFields().stream()
                .map(JpaIdentifierFieldFact::fieldName)
                .sorted()
                .toList()),
        () -> assertTrue(legacyOrder.idClass().evidenceIds().stream()
            .map(evidenceId -> evidence(analysis, evidenceId).annotationSymbol())
            .toList()
            .contains("@IdClass")));
  }

  @Test
  void wildcardOnlyEmbeddedAnnotationIsSkippedConservatively() throws Exception {
    Path fixtureRoot = wildcardEmbeddedFixtureRoot();
    JpaEntityAnalysis analysis = analyzer.analyze(
        fixtureRoot,
        List.of(fixtureRoot.resolve("src/main/java")));

    JpaEntityFact entity = entity(analysis, "WildcardEmbeddedEntity");

    assertAll(
        () -> assertTrue(entity.fields().isEmpty()),
        () -> assertEquals(List.of("id"), entity.identifierFields().stream()
            .map(JpaIdentifierFieldFact::fieldName)
            .toList()));
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
        () -> assertEquals("simple_id", id.identifierKind()),
        () -> assertNull(id.generatedValue()),
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
        () -> assertEquals("simple_id", id.identifierKind()),
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

    assertRelationship(relationship, "@ManyToOne", "many_to_one", "Customer");
    JpaJoinColumnFact joinColumn = relationship.joinColumns().get(0);
    assertAll(
        () -> assertEquals("join_metadata_present", relationship.ownershipSignal()),
        () -> assertEquals(false, relationship.optional()),
        () -> assertEquals("FetchType.LAZY", relationship.fetch()),
        () -> assertEquals(List.of("CascadeType.PERSIST", "CascadeType.MERGE"), relationship.cascade()),
        () -> assertNull(relationship.orphanRemoval()),
        () -> assertEquals("customer_id", joinColumn.name()),
        () -> assertEquals("id", joinColumn.referencedColumnName()),
        () -> assertEquals(false, joinColumn.nullable()),
        () -> assertEquals(true, joinColumn.unique()),
        () -> assertEquals(false, joinColumn.insertable()),
        () -> assertEquals(true, joinColumn.updatable()));
  }

  @Test
  void oneToManyRelationshipIsDetectedWithUnresolvedTargetType() throws Exception {
    JpaRelationshipFact relationship = relationship(entity(analyzeFixture(), "Order"), "lines");

    assertRelationship(relationship, "@OneToMany", "one_to_many", "List<OrderLine>");
    assertAll(
        () -> assertEquals("order", relationship.mappedBy()),
        () -> assertEquals("mapped_by_present", relationship.ownershipSignal()),
        () -> assertNull(relationship.optional()),
        () -> assertNull(relationship.fetch()),
        () -> assertEquals(List.of("CascadeType.ALL"), relationship.cascade()),
        () -> assertEquals(true, relationship.orphanRemoval()),
        () -> assertTrue(relationship.joinColumns().isEmpty()),
        () -> assertNull(relationship.joinTable()));
  }

  @Test
  void oneToOneRelationshipIsDetectedWithUnresolvedTargetType() throws Exception {
    JpaRelationshipFact relationship = relationship(entity(analyzeFixture(), "Order"), "invoice");

    assertRelationship(relationship, "@OneToOne", "one_to_one", "Invoice");
    assertAll(
        () -> assertEquals("join_metadata_present", relationship.ownershipSignal()),
        () -> assertEquals(false, relationship.optional()),
        () -> assertEquals("FetchType.LAZY", relationship.fetch()),
        () -> assertTrue(relationship.cascade().isEmpty()),
        () -> assertEquals(true, relationship.orphanRemoval()),
        () -> assertEquals("invoice_id", relationship.joinColumns().get(0).name()),
        () -> assertNull(relationship.joinColumns().get(0).referencedColumnName()));
  }

  @Test
  void manyToManyRelationshipIsDetectedWithUnresolvedTargetType() throws Exception {
    JpaRelationshipFact relationship = relationship(entity(analyzeFixture(), "Order"), "tags");

    assertRelationship(relationship, "@ManyToMany", "many_to_many", "Set<Tag>");
    JpaJoinTableFact joinTable = relationship.joinTable();
    assertAll(
        () -> assertEquals("join_metadata_present", relationship.ownershipSignal()),
        () -> assertNull(relationship.optional()),
        () -> assertEquals("FetchType.LAZY", relationship.fetch()),
        () -> assertTrue(relationship.cascade().isEmpty()),
        () -> assertNull(relationship.orphanRemoval()),
        () -> assertNotNull(joinTable),
        () -> assertEquals("order_tags", joinTable.name()),
        () -> assertEquals("sales", joinTable.schema()),
        () -> assertEquals("crm", joinTable.catalog()),
        () -> assertEquals("order_id", joinTable.joinColumns().get(0).name()),
        () -> assertEquals("id", joinTable.joinColumns().get(0).referencedColumnName()),
        () -> assertEquals("tag_id", joinTable.inverseJoinColumns().get(0).name()),
        () -> assertEquals(false, joinTable.inverseJoinColumns().get(0).nullable()));
  }

  @Test
  void nonEntityClassIsIgnored() throws Exception {
    JpaEntityAnalysis analysis = analyzeFixture();

    assertFalse(analysis.entities().stream()
        .anyMatch(entity -> entity.className().equals("com.example.domain.NotAnEntity")));
  }

  @Test
  void sourceDeclaredJpaFqcnAnnotationsDoNotEmitEntityFacts() throws Exception {
    Path fixtureRoot = spoofedOriginsFixtureRoot();
    JpaEntityAnalysis analysis = analyzer.analyze(
        fixtureRoot,
        List.of(fixtureRoot.resolve("src/main/java")));

    assertAll(
        () -> assertTrue(analysis.entities().isEmpty()),
        () -> assertTrue(analysis.embeddables().isEmpty()),
        () -> assertTrue(analysis.evidence().isEmpty()));
  }

  @Test
  void entitiesAndNestedFactsAreSortedDeterministically() throws Exception {
    JpaEntityAnalysis analysis = analyzeFixture();

    assertAll(
        () -> assertEquals(
            List.of(
                "com.example.domain.Customer",
                "com.example.domain.ExternalProfile",
                "com.example.domain.LegacyOrder",
                "com.example.domain.Order",
                "com.example.domain.PropertyAccessEntity",
                "com.example.domain.Shipment"),
            analysis.entities().stream().map(JpaEntityFact::className).toList()),
        () -> assertEquals(
            List.of(
                "com.example.domain.Address",
                "com.example.domain.ShipmentId"),
            analysis.embeddables().stream().map(JpaEmbeddableFact::className).toList()),
        () -> assertEquals(
            List.of("id", "status", "version"),
            entity(analysis, "Order").fields().stream()
                .map(JpaEntityFieldFact::fieldName)
                .toList()),
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
      if (entity.idClass() != null) {
        assertTrue(evidenceIds.containsAll(entity.idClass().evidenceIds()));
      }
      for (JpaIdentifierFieldFact identifierField : entity.identifierFields()) {
        assertTrue(evidenceIds.containsAll(identifierField.evidenceIds()));
      }
      for (JpaEntityFieldFact field : entity.fields()) {
        assertTrue(evidenceIds.containsAll(field.evidenceIds()));
        if (field.column() != null) {
          assertTrue(evidenceIds.containsAll(field.column().evidenceIds()));
        }
        if (field.enumerated() != null) {
          assertTrue(evidenceIds.containsAll(field.enumerated().evidenceIds()));
        }
        if (field.generatedValue() != null) {
          assertTrue(evidenceIds.containsAll(field.generatedValue().evidenceIds()));
        }
        if (field.version() != null) {
          assertTrue(evidenceIds.containsAll(field.version().evidenceIds()));
        }
        if (field.embedded() != null) {
          assertTrue(evidenceIds.containsAll(field.embedded().evidenceIds()));
        }
      }
      for (JpaRelationshipFact relationship : entity.relationships()) {
        assertTrue(evidenceIds.containsAll(relationship.evidenceIds()));
        for (JpaJoinColumnFact joinColumn : relationship.joinColumns()) {
          assertTrue(evidenceIds.containsAll(joinColumn.evidenceIds()));
        }
        if (relationship.joinTable() != null) {
          assertTrue(evidenceIds.containsAll(relationship.joinTable().evidenceIds()));
          for (JpaJoinColumnFact joinColumn : relationship.joinTable().joinColumns()) {
            assertTrue(evidenceIds.containsAll(joinColumn.evidenceIds()));
          }
          for (JpaJoinColumnFact joinColumn : relationship.joinTable().inverseJoinColumns()) {
            assertTrue(evidenceIds.containsAll(joinColumn.evidenceIds()));
          }
        }
      }
    }
    for (JpaEmbeddableFact embeddable : analysis.embeddables()) {
      assertTrue(evidenceIds.containsAll(embeddable.evidenceIds()));
      for (JpaEntityFieldFact field : embeddable.fields()) {
        assertTrue(evidenceIds.containsAll(field.evidenceIds()));
        if (field.column() != null) {
          assertTrue(evidenceIds.containsAll(field.column().evidenceIds()));
        }
        if (field.embedded() != null) {
          assertTrue(evidenceIds.containsAll(field.embedded().evidenceIds()));
        }
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
      String cardinality,
      String javaType) {
    assertAll(
        () -> assertEquals(annotation, relationship.annotation()),
        () -> assertEquals(cardinality, relationship.cardinality()),
        () -> assertEquals(javaType, relationship.javaType()),
        () -> assertEquals(javaType, relationship.target().declaredType()),
        () -> assertEquals("declared_type_only", relationship.target().targetResolution()),
        () -> assertNull(relationship.target().targetEntityId()),
        () -> assertNull(relationship.target().targetModuleId()),
        () -> assertNull(relationship.target().targetClassName()),
        () -> assertNull(relationship.target().supportType()),
        () -> assertNull(relationship.target().confidence()),
        () -> assertEquals("target_type_not_resolved", relationship.target().uncertainty()),
        () -> assertTrue(relationship.target().evidenceIds().isEmpty()),
        () -> assertTrue(relationship.evidenceIds().size() >= 1));
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

  private Path wildcardEmbeddedFixtureRoot() throws Exception {
    return Path.of(Objects.requireNonNull(
        getClass().getResource("/fixtures/jpa-embedded-conservative")).toURI());
  }

  private Path spoofedOriginsFixtureRoot() throws Exception {
    return Path.of(Objects.requireNonNull(
        getClass().getResource("/fixtures/jpa-spoofed-origins")).toURI());
  }

  private JpaEntityFact entity(JpaEntityAnalysis analysis, String simpleName) {
    return analysis.entities().stream()
        .filter(candidate -> candidate.className().equals("com.example.domain." + simpleName))
        .findFirst()
        .orElseThrow();
  }

  private JpaEmbeddableFact embeddable(JpaEntityAnalysis analysis, String simpleName) {
    return analysis.embeddables().stream()
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

  private JpaEntityFieldFact field(JpaEntityFact entity, String fieldName) {
    return entity.fields().stream()
        .filter(candidate -> candidate.fieldName().equals(fieldName))
        .findFirst()
        .orElseThrow();
  }

  private JpaEntityFieldFact field(JpaEmbeddableFact embeddable, String fieldName) {
    return embeddable.fields().stream()
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
