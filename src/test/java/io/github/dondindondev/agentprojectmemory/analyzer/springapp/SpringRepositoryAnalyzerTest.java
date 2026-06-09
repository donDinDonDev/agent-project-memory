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

final class SpringRepositoryAnalyzerTest {
  private final SpringRepositoryAnalyzer analyzer = new SpringRepositoryAnalyzer();

  @Test
  void detectsDirectRepositoryStereotypesAndSpringDataInterfaceSignalsSeparately()
      throws Exception {
    SpringRepositoryAnalysis analysis = analyzeFixture("spring-repository-signals");

    SpringRepositoryFact directClass = repository(
        analysis,
        "com.example.repositories.DirectOrderRepository",
        SpringRepositoryAnalyzer.SURFACE_CATEGORY_REPOSITORY_STEREOTYPE);
    SpringRepositoryFact inferredInterface = repository(
        analysis,
        "com.example.repositories.OrderRepository",
        SpringRepositoryAnalyzer.SURFACE_CATEGORY_SPRING_DATA_INTERFACE);
    SpringRepositoryFact bothDirect = repository(
        analysis,
        "com.example.repositories.AnnotatedSpringDataRepository",
        SpringRepositoryAnalyzer.SURFACE_CATEGORY_REPOSITORY_STEREOTYPE);
    SpringRepositoryFact bothInferred = repository(
        analysis,
        "com.example.repositories.AnnotatedSpringDataRepository",
        SpringRepositoryAnalyzer.SURFACE_CATEGORY_SPRING_DATA_INTERFACE);

    assertAll(
        () -> assertEquals("extracted", directClass.supportType()),
        () -> assertEquals("direct_repository_stereotype", directClass.repositorySignal()),
        () -> assertEquals(List.of(), directClass.extendsTypes()),
        () -> assertNull(directClass.entityRelationStatus()),
        () -> assertEquals("inferred", inferredInterface.supportType()),
        () -> assertEquals(
            "spring_data_repository_interface_extension",
            inferredInterface.repositorySignal()),
        () -> assertEquals(
            List.of("org.springframework.data.jpa.repository.JpaRepository"),
            inferredInterface.extendsTypes()),
        () -> assertEquals(1, inferredInterface.entityGenericTypes().size()),
        () -> assertEquals(
            "ProjectOrder",
            inferredInterface.entityGenericTypes().get(0).sourceTypeName()),
        () -> assertEquals(
            "com.example.repositories.ProjectOrder",
            inferredInterface.entityGenericTypes().get(0).qualifiedTypeName()),
        () -> assertEquals(
            SpringRepositoryAnalyzer.ENTITY_GENERIC_SUPPORTED,
            inferredInterface.entityGenericTypes().get(0).supportStatus()),
        () -> assertEquals("not_analyzed", inferredInterface.entityRelationStatus()),
        () -> assertNull(inferredInterface.entityRelation()),
        () -> assertEquals("extracted", bothDirect.supportType()),
        () -> assertEquals("inferred", bothInferred.supportType()),
        () -> assertEquals(
            List.of("org.springframework.data.repository.CrudRepository"),
            bothInferred.extendsTypes()));
  }

  @Test
  void detectsSupportedSpringDataFamiliesWithExplicitImportsOrFullyQualifiedNames()
      throws Exception {
    SpringRepositoryAnalysis analysis = analyzeFixture("spring-repository-signals");

    assertAll(
        () -> assertEquals(
            List.of("org.springframework.data.repository.Repository"),
            repository(
                analysis,
                "com.example.repositories.CoreRepositorySignal",
                SpringRepositoryAnalyzer.SURFACE_CATEGORY_SPRING_DATA_INTERFACE)
                .extendsTypes()),
        () -> assertEquals(
            List.of("org.springframework.data.repository.PagingAndSortingRepository"),
            repository(
                analysis,
                "com.example.repositories.PagedOrderRepository",
                SpringRepositoryAnalyzer.SURFACE_CATEGORY_SPRING_DATA_INTERFACE)
                .extendsTypes()),
        () -> assertEquals(
            List.of("org.springframework.data.mongodb.repository.MongoRepository"),
            repository(
                analysis,
                "com.example.repositories.MongoOrderRepository",
                SpringRepositoryAnalyzer.SURFACE_CATEGORY_SPRING_DATA_INTERFACE)
                .extendsTypes()),
        () -> assertEquals(
            List.of("org.springframework.data.repository.CrudRepository"),
            repository(
                analysis,
                "com.example.repositories.FullyQualifiedCrudRepository",
                SpringRepositoryAnalyzer.SURFACE_CATEGORY_SPRING_DATA_INTERFACE)
                .extendsTypes()));
  }

  @Test
  void ignoresUnsupportedNonInterfaceAndWildcardOnlySpringDataReferences()
      throws Exception {
    SpringRepositoryAnalysis analysis = analyzeFixture("spring-repository-signals");

    assertAll(
        () -> assertFalse(hasRepository(
            analysis,
            "com.example.repositories.NotARepository",
            SpringRepositoryAnalyzer.SURFACE_CATEGORY_SPRING_DATA_INTERFACE)),
        () -> assertFalse(hasRepository(
            analysis,
            "com.example.repositories.WildcardOnlyRepository",
            SpringRepositoryAnalyzer.SURFACE_CATEGORY_SPRING_DATA_INTERFACE)),
        () -> assertFalse(hasRepository(
            analysis,
            "com.example.repositories.LocalBaseRepository",
            SpringRepositoryAnalyzer.SURFACE_CATEGORY_SPRING_DATA_INTERFACE)));
  }

  @Test
  void sourceDeclaredSpringOriginsDoNotEmitRepositoryFacts() throws Exception {
    SpringRepositoryAnalysis analysis = analyzeFixture("spring-repository-spoofed-origins");

    assertAll(
        () -> assertTrue(analysis.repositories().isEmpty()),
        () -> assertTrue(analysis.evidence().isEmpty()));
  }

  @Test
  void evidenceIdsResolveToAnnotationAndCodeSymbolEvidence() throws Exception {
    SpringRepositoryAnalysis analysis = analyzeFixture("spring-repository-signals");

    for (SpringRepositoryFact repository : analysis.repositories()) {
      assertFalse(repository.evidenceIds().isEmpty());
      for (String evidenceId : repository.evidenceIds()) {
        SpringRepositoryEvidence evidence = evidence(analysis, evidenceId);
        assertAll(
            () -> assertEquals(repository.className(), evidence.className()),
            () -> assertNull(evidence.methodName()),
            () -> assertTrue(evidence.sourcePath().startsWith("src/main/java/")),
            () -> assertNotNull(evidence.lineStart()),
            () -> assertNotNull(evidence.lineEnd()),
            () -> assertEquals("high", evidence.confidence()));
      }
    }

    SpringRepositoryFact inferred = repository(
        analysis,
        "com.example.repositories.OrderRepository",
        SpringRepositoryAnalyzer.SURFACE_CATEGORY_SPRING_DATA_INTERFACE);

    assertAll(
        () -> assertTrue(inferred.evidenceIds().stream()
            .map(evidenceId -> evidence(analysis, evidenceId))
            .anyMatch(evidence -> "code_symbol".equals(evidence.sourceType())
                && evidence.symbolName().equals("com.example.repositories.OrderRepository"))),
        () -> assertTrue(inferred.evidenceIds().stream()
            .map(evidenceId -> evidence(analysis, evidenceId))
            .anyMatch(evidence -> "code_symbol".equals(evidence.sourceType())
                && evidence.symbolName().equals(
                    "extends:org.springframework.data.jpa.repository.JpaRepository"))));
  }

  @Test
  void repositoryFactsAreSortedBySourcePathClassAndCategory() throws Exception {
    SpringRepositoryAnalysis analysis = analyzeFixture("spring-repository-signals");

    assertEquals(
        List.of(
            "com.example.repositories.AnnotatedSpringDataRepository:spring_data_repository_interface_signal",
            "com.example.repositories.AnnotatedSpringDataRepository:spring_repository_stereotype",
            "com.example.repositories.CoreRepositorySignal:spring_data_repository_interface_signal",
            "com.example.repositories.DirectOrderRepository:spring_repository_stereotype",
            "com.example.repositories.FullyQualifiedCrudRepository:spring_data_repository_interface_signal",
            "com.example.repositories.MongoOrderRepository:spring_data_repository_interface_signal",
            "com.example.repositories.OrderRepository:spring_data_repository_interface_signal",
            "com.example.repositories.PagedOrderRepository:spring_data_repository_interface_signal"),
        analysis.repositories().stream()
            .map(repository -> repository.className() + ":" + repository.surfaceCategory())
            .toList());
  }

  private SpringRepositoryAnalysis analyzeFixture(String fixtureName) throws Exception {
    Path fixtureRoot = Path.of(Objects.requireNonNull(
        getClass().getResource("/fixtures/" + fixtureName)).toURI());
    return analyzer.analyze(fixtureRoot, List.of(fixtureRoot.resolve("src/main/java")));
  }

  private SpringRepositoryFact repository(
      SpringRepositoryAnalysis analysis,
      String className,
      String surfaceCategory) {
    return analysis.repositories().stream()
        .filter(candidate -> candidate.className().equals(className))
        .filter(candidate -> candidate.surfaceCategory().equals(surfaceCategory))
        .findFirst()
        .orElseThrow();
  }

  private boolean hasRepository(
      SpringRepositoryAnalysis analysis,
      String className,
      String surfaceCategory) {
    return analysis.repositories().stream()
        .anyMatch(candidate -> candidate.className().equals(className)
            && candidate.surfaceCategory().equals(surfaceCategory));
  }

  private SpringRepositoryEvidence evidence(SpringRepositoryAnalysis analysis, String evidenceId) {
    return analysis.evidence().stream()
        .filter(candidate -> candidate.id().equals(evidenceId))
        .findFirst()
        .orElseThrow();
  }
}
