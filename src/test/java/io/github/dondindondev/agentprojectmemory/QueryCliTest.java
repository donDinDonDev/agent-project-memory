package io.github.dondindondev.agentprojectmemory;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class QueryCliTest {
  @TempDir
  private Path tempDir;

  @Test
  void generalHelpMentionsQueryAndQueryHelpDoesNotCreateArtifacts() {
    CliResult generalHelp = runCli("--help");
    CliResult queryHelp = runCli("query", "--help");

    assertAll(
        () -> assertEquals(0, generalHelp.exitCode()),
        () -> assertTrue(generalHelp.stdout().contains("agent-project-memory query <path>")),
        () -> assertEquals(0, queryHelp.exitCode()),
        () -> assertTrue(queryHelp.stdout().contains("Usage: agent-project-memory query")),
        () -> assertTrue(queryHelp.stdout().contains("list modules")),
        () -> assertTrue(queryHelp.stdout().contains("list endpoints")),
        () -> assertTrue(queryHelp.stdout().contains("list api-operations")),
        () -> assertTrue(queryHelp.stdout().contains("list entities")),
        () -> assertTrue(queryHelp.stdout().contains("list tests")),
        () -> assertTrue(queryHelp.stdout().contains("explain evidence <id>")),
        () -> assertTrue(queryHelp.stdout().contains("find fact <term>")),
        () -> assertTrue(queryHelp.stdout().contains("find symbol <term>")),
        () -> assertTrue(queryHelp.stdout().contains("relations <id>")),
        () -> assertTrue(queryHelp.stdout().contains("agent-context")),
        () -> assertTrue(queryHelp.stdout().contains("impact --files <changed-file>")),
        () -> assertTrue(queryHelp.stderr().isEmpty()),
        () -> assertFalse(Files.exists(tempDir.resolve(".project-memory"))));
  }

  @Test
  void queryListModulesValidatesRepositoryRootArtifactsWithoutGraph() throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Path artifactRoot = repositoryRoot.resolve(".project-memory");
    writeBaseArtifacts(artifactRoot);

    CliResult result = runCli("query", repositoryRoot.toString(), "list", "modules");

    String expected = """
        Query: list modules
        Source artifacts: project-map.json schema_version=1.0, evidence-index.jsonl records=1
        Results: 1

        1. module:.
           module_path: .
           build_systems: maven
           support_status: supported
           pom_path: pom.xml
           source_roots: src/main/java
           test_roots: src/test/java
           declaration_kind: scan_root
           declared_path: .
           evidence_ids: ev:pom.xml:1-1:build_file:pom.xml
        """;

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertEquals(expected, result.stdout()),
        () -> assertTrue(result.stderr().isEmpty()),
        () -> assertFalse(result.stdout().contains(repositoryRoot.toString())));
  }

  @Test
  void queryListModulesAcceptsDirectArtifactRootAndValidOptionalGraph() throws Exception {
    Path artifactRoot = tempDir.resolve("repo/.project-memory");
    writeBaseArtifacts(artifactRoot);
    writeValidGraph(artifactRoot);

    CliResult result = runCli("query", artifactRoot.toString(), "list", "modules");

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertTrue(result.stdout().contains("Query: list modules")),
        () -> assertFalse(result.stdout().contains("project-graph.json")),
        () -> assertTrue(result.stderr().isEmpty()),
        () -> assertFalse(result.stdout().contains(artifactRoot.toString())));
  }

  @Test
  void nonGraphQueryCommandsIgnoreMalformedPresentGraph() throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Path artifactRoot = repositoryRoot.resolve(".project-memory");
    writeArtifacts(artifactRoot, richProjectMap(), lookupEvidenceRecords());
    writeMalformedGraph(artifactRoot);

    CliResult modules = runCli("query", repositoryRoot.toString(), "list", "modules");
    CliResult endpoints = runCli("query", repositoryRoot.toString(), "list", "endpoints");
    CliResult evidence = runCli(
        "query",
        repositoryRoot.toString(),
        "explain",
        "evidence",
        "ev:endpoint:mapping");
    CliResult symbol = runCli(
        "query",
        repositoryRoot.toString(),
        "find",
        "symbol",
        "com.example.domain.Order");
    CliResult projectMapFact = runCli(
        "query",
        repositoryRoot.toString(),
        "find",
        "fact",
        "endpoint:com.example.web.OrderController#getOrder");

    assertAll(
        () -> assertEquals(0, modules.exitCode()),
        () -> assertEquals(0, endpoints.exitCode()),
        () -> assertEquals(0, evidence.exitCode()),
        () -> assertEquals(0, symbol.exitCode()),
        () -> assertEquals(0, projectMapFact.exitCode()),
        () -> assertTrue(modules.stderr().isEmpty()),
        () -> assertTrue(endpoints.stderr().isEmpty()),
        () -> assertTrue(evidence.stderr().isEmpty()),
        () -> assertTrue(symbol.stderr().isEmpty()),
        () -> assertTrue(projectMapFact.stderr().isEmpty()),
        () -> assertTrue(modules.stdout().contains("Query: list modules")),
        () -> assertTrue(endpoints.stdout().contains("Query: list endpoints")),
        () -> assertTrue(evidence.stdout().contains("Query: explain evidence")),
        () -> assertTrue(symbol.stdout().contains("Query: find symbol")),
        () -> assertTrue(projectMapFact.stdout().contains("Query: find fact")),
        () -> assertTrue(projectMapFact.stdout().contains("Results: 1")),
        () -> assertFalse(modules.stdout().contains("project-graph.json")),
        () -> assertFalse(endpoints.stdout().contains("project-graph.json")),
        () -> assertFalse(evidence.stdout().contains("project-graph.json")),
        () -> assertFalse(symbol.stdout().contains("project-graph.json")),
        () -> assertFalse(projectMapFact.stdout().contains("project-graph.json")));
  }

  @Test
  void listEndpointsAndApiOperationsStaySeparate() throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Path artifactRoot = repositoryRoot.resolve(".project-memory");
    writeArtifacts(artifactRoot, richProjectMap(), lookupEvidenceRecords());

    CliResult endpoints = runCli("query", repositoryRoot.toString(), "list", "endpoints");
    CliResult operations = runCli("query", repositoryRoot.toString(), "list", "api-operations");

    assertAll(
        () -> assertEquals(0, endpoints.exitCode()),
        () -> assertTrue(endpoints.stdout().contains("Query: list endpoints")),
        () -> assertTrue(endpoints.stdout().contains(
            "endpoint:com.example.web.OrderController#getOrder")),
        () -> assertTrue(endpoints.stdout().contains("kind: source_visible_spring_mvc_endpoint")),
        () -> assertTrue(endpoints.stdout().contains("mapping_source: kind=direct_handler_method")),
        () -> assertFalse(endpoints.stdout().contains("openapi_operation:")),
        () -> assertEquals(0, operations.exitCode()),
        () -> assertTrue(operations.stdout().contains("Query: list api-operations")),
        () -> assertTrue(operations.stdout().contains(
            "openapi_operation:module:.:spec:src/main/resources/openapi.yml:operation:post:/orders")),
        () -> assertTrue(operations.stdout().contains("kind: spec-backed declared API operation")),
        () -> assertTrue(operations.stdout().contains("implementation_status: not_analyzed")),
        () -> assertFalse(operations.stdout().contains("endpoint:com.example.web.OrderController#getOrder")),
        () -> assertTrue(endpoints.stderr().isEmpty()),
        () -> assertTrue(operations.stderr().isEmpty()));
  }

  @Test
  void listEntitiesKeepsEntityEmbeddableAndRepositoryRelationBoundaries() throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Path artifactRoot = repositoryRoot.resolve(".project-memory");
    writeArtifacts(artifactRoot, richProjectMap(), lookupEvidenceRecords());

    CliResult result = runCli("query", repositoryRoot.toString(), "list", "entities");

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertTrue(result.stdout().contains("Results: 4")),
        () -> assertTrue(result.stdout().contains("Entities: 1")),
        () -> assertTrue(result.stdout().contains("Embeddables: 1")),
        () -> assertTrue(result.stdout().contains("Repository/entity relation rows: 2")),
        () -> assertTrue(result.stdout().contains("kind: jpa_entity")),
        () -> assertTrue(result.stdout().contains("kind: jpa_embeddable")),
        () -> assertTrue(result.stdout().contains(
            "target_resolution=source_visible_embeddable target_embeddable_id=embeddable:com.example.domain.OrderId")),
        () -> assertTrue(result.stdout().contains(
            "target_resolution=declared_type_only target_entity_id=null")),
        () -> assertTrue(result.stdout().contains("uncertainty=target_type_not_resolved")),
        () -> assertTrue(result.stdout().contains("entity_relation_status: inferred")),
        () -> assertTrue(result.stdout().contains(
            "target_entity_id: entity:com.example.domain.Order")),
        () -> assertTrue(result.stdout().contains("confidence: medium")),
        () -> assertTrue(result.stdout().contains("entity_relation_status: unsupported")),
        () -> assertTrue(result.stdout().contains("entity_relation: null")),
        () -> assertTrue(result.stderr().isEmpty()));
  }

  @Test
  void listTestsPreservesSignalsRelationsConfidenceAndUncertaintyDeterministically()
      throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Path artifactRoot = repositoryRoot.resolve(".project-memory");
    writeArtifacts(artifactRoot, richProjectMap(), lookupEvidenceRecords());

    CliResult first = runCli("query", repositoryRoot.toString(), "list", "tests");
    CliResult second = runCli("query", repositoryRoot.toString(), "list", "tests");

    assertAll(
        () -> assertEquals(0, first.exitCode()),
        () -> assertEquals(first.stdout(), second.stdout()),
        () -> assertTrue(first.stdout().contains("Query: list tests")),
        () -> assertTrue(first.stdout().contains("test:com.example.web.OrderControllerTest")),
        () -> assertTrue(first.stdout().contains("module_id: module:.")),
        () -> assertTrue(first.stdout().contains(
            "method_name=returnsOrder test_annotation=@Test method_kind=test")),
        () -> assertTrue(first.stdout().contains("name=JUnit Jupiter signal_kind=framework")),
        () -> assertTrue(first.stdout().contains(
            "annotation=@WebMvcTest slice_kind=web_mvc_test signal_kind=spring_test_slice")),
        () -> assertTrue(first.stdout().contains(
            "annotation=@MockBean mock_signal=spring_boot_mockbean_annotation")),
        () -> assertTrue(first.stdout().contains(
            "relation_status=inferred relation_type=naming_convention class_name=com.example.web.OrderController")),
        () -> assertTrue(first.stdout().contains(
            "confidence=medium uncertainty=null evidence_ids=ev:test:class, ev:controller:class")),
        () -> assertTrue(first.stdout().contains(
            "relation_status=not_detected relation_type=naming_convention class_name=null")),
        () -> assertTrue(first.stdout().contains(
            "candidate_reference=MissingController support_type=null confidence=low uncertainty=no_matching_production_class")),
        () -> assertTrue(first.stderr().isEmpty()));
  }

  @Test
  void listCommandsTreatMissingOrEmptyCategoriesAsSuccessfulEmptyResults() throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Path artifactRoot = repositoryRoot.resolve(".project-memory");
    writeArtifacts(artifactRoot, "{\"schema_version\":\"1.0\",\"project\":{\"modules\":{\"items\":[]}}}\n");

    CliResult modules = runCli("query", repositoryRoot.toString(), "list", "modules");
    CliResult endpoints = runCli("query", repositoryRoot.toString(), "list", "endpoints");
    CliResult operations = runCli("query", repositoryRoot.toString(), "list", "api-operations");
    CliResult entities = runCli("query", repositoryRoot.toString(), "list", "entities");
    CliResult tests = runCli("query", repositoryRoot.toString(), "list", "tests");

    assertAll(
        () -> assertEquals(0, modules.exitCode()),
        () -> assertTrue(modules.stdout().contains("Results: 0\nNo modules found.")),
        () -> assertEquals(0, endpoints.exitCode()),
        () -> assertTrue(endpoints.stdout().contains("Results: 0\nNo endpoints found.")),
        () -> assertEquals(0, operations.exitCode()),
        () -> assertTrue(operations.stdout().contains("Results: 0\nNo api-operations found.")),
        () -> assertEquals(0, entities.exitCode()),
        () -> assertTrue(entities.stdout().contains("Results: 0\nEntities: 0\nEmbeddables: 0")),
        () -> assertTrue(entities.stdout().contains("No entities found.")),
        () -> assertEquals(0, tests.exitCode()),
        () -> assertTrue(tests.stdout().contains("Results: 0\nNo tests found.")),
        () -> assertTrue(modules.stderr().isEmpty()),
        () -> assertTrue(endpoints.stderr().isEmpty()),
        () -> assertTrue(operations.stderr().isEmpty()),
        () -> assertTrue(entities.stderr().isEmpty()),
        () -> assertTrue(tests.stderr().isEmpty()));
  }

  @Test
  void explainEvidenceRendersExactRecordAndMissingIdReturnsNoResult() throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Path artifactRoot = repositoryRoot.resolve(".project-memory");
    writeArtifacts(artifactRoot, richProjectMap(), lookupEvidenceRecords());

    CliResult found = runCli(
        "query",
        repositoryRoot.toString(),
        "explain",
        "evidence",
        "ev:endpoint:mapping");
    CliResult missing = runCli(
        "query",
        repositoryRoot.toString(),
        "explain",
        "evidence",
        "ev:missing");

    assertAll(
        () -> assertEquals(0, found.exitCode()),
        () -> assertTrue(found.stdout().contains("Query: explain evidence")),
        () -> assertTrue(found.stdout().contains("Results: 1")),
        () -> assertTrue(found.stdout().contains("1. ev:endpoint:mapping")),
        () -> assertTrue(found.stdout().contains("source_type: annotation")),
        () -> assertTrue(found.stdout().contains("path: src/main/java/com/example/web/OrderController.java")),
        () -> assertTrue(found.stdout().contains("class_name: com.example.web.OrderController")),
        () -> assertTrue(found.stdout().contains("method_name: getOrder")),
        () -> assertTrue(found.stdout().contains("symbol_name: @GetMapping")),
        () -> assertTrue(found.stdout().contains("line_start: 12")),
        () -> assertTrue(found.stdout().contains("line_end: 12")),
        () -> assertTrue(found.stdout().contains("excerpt: @GetMapping(\"/orders/{id}\")")),
        () -> assertTrue(found.stdout().contains("confidence: high")),
        () -> assertTrue(found.stderr().isEmpty()),
        () -> assertEquals(6, missing.exitCode()),
        () -> assertTrue(missing.stdout().isEmpty()),
        () -> assertTrue(missing.stderr().contains("Query no result: No evidence record matched")),
        () -> assertFalse(missing.stderr().contains("ev:missing")));
  }

  @Test
  void findFactMatchesProjectMapAndGraphIdsWithoutRelationRendering() throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Path artifactRoot = repositoryRoot.resolve(".project-memory");
    writeArtifacts(artifactRoot, richProjectMap(), lookupEvidenceRecords());
    writeLookupGraph(artifactRoot);

    CliResult projectMapFact = runCli(
        "query",
        repositoryRoot.toString(),
        "find",
        "fact",
        "endpoint:com.example.web.OrderController#getOrder");
    CliResult graphFact = runCli(
        "query",
        repositoryRoot.toString(),
        "find",
        "fact",
        "edge:owns:node:module:root:node:endpoint:endpoint%3Acom.example.web.OrderController%23getOrder");

    assertAll(
        () -> assertEquals(0, projectMapFact.exitCode()),
        () -> assertTrue(projectMapFact.stdout().contains("Query: find fact")),
        () -> assertTrue(projectMapFact.stdout().contains("Results: 1")),
        () -> assertTrue(projectMapFact.stdout().contains("navigation: project-map.json#/endpoints/0 (not evidence)")),
        () -> assertFalse(projectMapFact.stdout().contains("navigation: project-graph.json")),
        () -> assertFalse(projectMapFact.stdout().contains("source_ref: artifact=project-map.json section=endpoints id=endpoint:com.example.web.OrderController#getOrder (not evidence)")),
        () -> assertTrue(projectMapFact.stdout().contains("api_surface_category: source_visible_spring_mvc_endpoint")),
        () -> assertTrue(projectMapFact.stdout().contains("controller_class: com.example.web.OrderController")),
        () -> assertTrue(projectMapFact.stderr().isEmpty()),
        () -> assertEquals(0, graphFact.exitCode()),
        () -> assertTrue(graphFact.stdout().contains("Query: find fact")),
        () -> assertTrue(graphFact.stdout().contains("Results: 1")),
        () -> assertTrue(graphFact.stdout().contains("navigation: project-graph.json#/edges/0 (not evidence)")),
        () -> assertTrue(graphFact.stdout().contains("type: owns")),
        () -> assertTrue(graphFact.stdout().contains("relation_status: derived")),
        () -> assertTrue(graphFact.stdout().contains("derivation: kind=project_map_field artifact=project-map.json section=endpoints (not evidence)")),
        () -> assertFalse(graphFact.stdout().contains("incoming")),
        () -> assertTrue(graphFact.stderr().isEmpty()));
  }

  @Test
  void graphBackedFindFactRejectsMalformedGraphWhenGraphLookupIsRequested() throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Path artifactRoot = repositoryRoot.resolve(".project-memory");
    writeArtifacts(artifactRoot, richProjectMap(), lookupEvidenceRecords());
    writeMalformedGraph(artifactRoot);

    CliResult result = runCli(
        "query",
        repositoryRoot.toString(),
        "find",
        "fact",
        "node:module:root");

    assertAll(
        () -> assertEquals(3, result.exitCode()),
        () -> assertTrue(result.stdout().isEmpty()),
        () -> assertTrue(result.stderr().contains("Query input error: Malformed project-graph.json.")),
        () -> assertFalse(result.stderr().contains(repositoryRoot.toString())));
  }

  @Test
  void findFactKeepsFactAndEvidenceOrSymbolCategoriesSeparate() throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Path artifactRoot = repositoryRoot.resolve(".project-memory");
    writeArtifacts(artifactRoot, richProjectMap(), lookupEvidenceRecords());

    CliResult evidenceId = runCli("query", repositoryRoot.toString(), "find", "fact", "ev:entity:order");
    CliResult operationId = runCli("query", repositoryRoot.toString(), "find", "fact", "createOrder");
    CliResult symbol = runCli("query", repositoryRoot.toString(), "find", "symbol", "createOrder");

    assertAll(
        () -> assertEquals(6, evidenceId.exitCode()),
        () -> assertTrue(evidenceId.stdout().isEmpty()),
        () -> assertTrue(evidenceId.stderr().contains("Query no result: No exact fact match found.")),
        () -> assertFalse(evidenceId.stderr().contains("ev:entity:order")),
        () -> assertEquals(6, operationId.exitCode()),
        () -> assertTrue(operationId.stdout().isEmpty()),
        () -> assertEquals(0, symbol.exitCode()),
        () -> assertTrue(symbol.stdout().contains("Query: find symbol")),
        () -> assertTrue(symbol.stdout().contains("matched_field: operation_id")),
        () -> assertTrue(symbol.stdout().contains("matched_value: createOrder")),
        () -> assertTrue(symbol.stdout().contains("api_surface_category: openapi_declared_operation")),
        () -> assertTrue(symbol.stderr().isEmpty()));
  }

  @Test
  void findSymbolIsExactCaseSensitiveAndReturnsMultipleMatches() throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Path artifactRoot = repositoryRoot.resolve(".project-memory");
    writeArtifacts(artifactRoot, richProjectMap(), lookupEvidenceRecords());

    CliResult exact = runCli(
        "query",
        repositoryRoot.toString(),
        "find",
        "symbol",
        "com.example.domain.Order");
    CliResult wrongCase = runCli(
        "query",
        repositoryRoot.toString(),
        "find",
        "symbol",
        "com.example.domain.order");
    CliResult simpleName = runCli(
        "query",
        repositoryRoot.toString(),
        "find",
        "symbol",
        "Order");

    assertAll(
        () -> assertEquals(0, exact.exitCode()),
        () -> assertTrue(exact.stdout().contains("Query: find symbol")),
        () -> assertTrue(exact.stdout().contains("Results: 4")),
        () -> assertTrue(exact.stdout().contains("navigation: project-map.json#/entities/items/0 (not evidence)")),
        () -> assertTrue(exact.stdout().contains("matched_field: class_name")),
        () -> assertTrue(exact.stdout().contains("matched_field: target_class_name")),
        () -> assertTrue(exact.stdout().contains("matched_field: generic_type")),
        () -> assertTrue(exact.stdout().contains("navigation: evidence-index.jsonl#/records/5 (not evidence)")),
        () -> assertTrue(exact.stdout().contains("source_type: code_symbol")),
        () -> assertTrue(exact.stderr().isEmpty()),
        () -> assertEquals(6, wrongCase.exitCode()),
        () -> assertTrue(wrongCase.stdout().isEmpty()),
        () -> assertEquals(0, simpleName.exitCode()),
        () -> assertTrue(simpleName.stdout().contains("matched_field: class_name.simple_name")),
        () -> assertFalse(simpleName.stdout().contains("com.example.domain.order")));
  }

  @Test
  void queryRelationsRequiresGraphArtifact() throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    writeBaseArtifacts(repositoryRoot.resolve(".project-memory"));

    CliResult result = runCli("query", repositoryRoot.toString(), "relations", "node:module:root");

    assertAll(
        () -> assertEquals(3, result.exitCode()),
        () -> assertTrue(result.stdout().isEmpty()),
        () -> assertTrue(result.stderr().contains("Query input error: Missing project-graph.json.")),
        () -> assertFalse(result.stderr().contains(repositoryRoot.toString())));
  }

  @Test
  void queryRelationsRendersDefaultBothDirectionsForNodeId() throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Path artifactRoot = repositoryRoot.resolve(".project-memory");
    writeArtifacts(artifactRoot, richProjectMap(), lookupEvidenceRecords());
    writeRelationGraph(artifactRoot);

    CliResult result = runCli(
        "query",
        repositoryRoot.toString(),
        "relations",
        "node:type:root:com.example.web.OrderController");

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertTrue(result.stdout().contains("Query: relations")),
        () -> assertTrue(result.stdout().contains("project-graph.json graph_schema_version=1.0")),
        () -> assertTrue(result.stdout().contains("Resolved by: node id")),
        () -> assertTrue(result.stdout().contains("Direction: both")),
        () -> assertTrue(result.stdout().contains("Results: 3")),
        () -> assertTrue(result.stdout().contains("Node\n   id: node:type:root:com.example.web.OrderController")),
        () -> assertTrue(result.stdout().contains("Edges: 3")),
        () -> assertTrue(result.stdout().contains("type: owns")),
        () -> assertTrue(result.stdout().contains("type: declares")),
        () -> assertTrue(result.stdout().contains("type: tested_subject")),
        () -> assertTrue(result.stdout().contains(
            "relation_attributes: relation_type=naming_convention, target_class_name=com.example.web.OrderController")),
        () -> assertTrue(result.stdout().contains(
            "derivation: kind=project_map_field artifact=project-map.json section=components.items fields=module_id, class_name (not evidence)")),
        () -> assertTrue(result.stdout().contains("Relation statuses: 0")),
        () -> assertFalse(result.stdout().contains("query relations is not implemented")),
        () -> assertFalse(result.stdout().contains(repositoryRoot.toString())),
        () -> assertTrue(result.stderr().isEmpty()));
  }

  @Test
  void queryRelationsMapsGeneratedFactIdThroughSourceRef() throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Path artifactRoot = repositoryRoot.resolve(".project-memory");
    writeArtifacts(artifactRoot, richProjectMap(), lookupEvidenceRecords());
    writeRelationGraph(artifactRoot);

    CliResult result = runCli(
        "query",
        repositoryRoot.toString(),
        "relations",
        "endpoint:com.example.web.OrderController#getOrder");

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertTrue(result.stdout().contains("Resolved by: source_ref.id")),
        () -> assertTrue(result.stdout().contains(
            "Resolved node: node:endpoint:endpoint%3Acom.example.web.OrderController%23getOrder")),
        () -> assertTrue(result.stdout().contains(
            "source_ref: artifact=project-map.json section=endpoints id=endpoint:com.example.web.OrderController#getOrder (not evidence)")),
        () -> assertTrue(result.stdout().contains("Edges: 1")),
        () -> assertTrue(result.stdout().contains("direction: incoming")),
        () -> assertTrue(result.stdout().contains("type: declares")),
        () -> assertTrue(result.stdout().contains("Relation statuses: 0")),
        () -> assertTrue(result.stderr().isEmpty()));
  }

  @Test
  void queryRelationsKeepsEdgesAndStatusesSeparate() throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Path artifactRoot = repositoryRoot.resolve(".project-memory");
    writeArtifacts(artifactRoot, richProjectMap(), lookupEvidenceRecords());
    writeRelationGraph(artifactRoot);

    CliResult result = runCli(
        "query",
        repositoryRoot.toString(),
        "relations",
        "node:test:test%3Acom.example.web.OrderControllerTest");

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertTrue(result.stdout().contains("Edges: 1")),
        () -> assertTrue(result.stdout().contains("type: tested_subject")),
        () -> assertTrue(result.stdout().contains("Relation statuses: 1")),
        () -> assertTrue(result.stdout().contains(
            "relation-status:tested_subject:node:test:test%3Acom.example.web.OrderControllerTest:no_supported_subject_signal")),
        () -> assertTrue(result.stdout().contains("relation_family: tested_subject")),
        () -> assertTrue(result.stdout().contains("target_id: null")),
        () -> assertTrue(result.stdout().contains("relation_status: not_detected")),
        () -> assertTrue(result.stdout().contains("support_type: status_only")),
        () -> assertTrue(result.stdout().contains("uncertainty: no_supported_subject_signal")),
        () -> assertTrue(result.stdout().contains("relation_attributes: relation_type=not_detected")),
        () -> assertTrue(result.stdout().contains(
            "derivation: kind=project_map_relation_status artifact=project-map.json section=tests.items[].tested_subjects fields=null (not evidence)")),
        () -> assertTrue(result.stderr().isEmpty()));
  }

  @Test
  void queryRelationsDirectionFiltersOneHopEdges() throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Path artifactRoot = repositoryRoot.resolve(".project-memory");
    writeArtifacts(artifactRoot, richProjectMap(), lookupEvidenceRecords());
    writeRelationGraph(artifactRoot);

    CliResult incoming = runCli(
        "query",
        repositoryRoot.toString(),
        "relations",
        "node:type:root:com.example.web.OrderController",
        "--direction",
        "incoming");
    CliResult outgoing = runCli(
        "query",
        repositoryRoot.toString(),
        "relations",
        "node:type:root:com.example.web.OrderController",
        "--direction",
        "outgoing");

    assertAll(
        () -> assertEquals(0, incoming.exitCode()),
        () -> assertTrue(incoming.stdout().contains("Direction: incoming")),
        () -> assertTrue(incoming.stdout().contains("Results: 2")),
        () -> assertTrue(incoming.stdout().contains("type: owns")),
        () -> assertTrue(incoming.stdout().contains("type: tested_subject")),
        () -> assertFalse(incoming.stdout().contains("type: declares")),
        () -> assertEquals(0, outgoing.exitCode()),
        () -> assertTrue(outgoing.stdout().contains("Direction: outgoing")),
        () -> assertTrue(outgoing.stdout().contains("Results: 1")),
        () -> assertTrue(outgoing.stdout().contains("type: declares")),
        () -> assertFalse(outgoing.stdout().contains("type: tested_subject")),
        () -> assertTrue(incoming.stderr().isEmpty()),
        () -> assertTrue(outgoing.stderr().isEmpty()));
  }

  @Test
  void queryRelationsMissingSubjectReturnsNoResultWithoutEchoingId() throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Path artifactRoot = repositoryRoot.resolve(".project-memory");
    writeArtifacts(artifactRoot, richProjectMap(), lookupEvidenceRecords());
    writeRelationGraph(artifactRoot);

    CliResult result = runCli(
        "query",
        repositoryRoot.toString(),
        "relations",
        "node:missing:SECRET_TOKEN");

    assertAll(
        () -> assertEquals(6, result.exitCode()),
        () -> assertTrue(result.stdout().isEmpty()),
        () -> assertTrue(result.stderr().contains("Query no result: No graph node matched")),
        () -> assertFalse(result.stderr().contains("SECRET_TOKEN")));
  }

  @Test
  void queryRelationsRejectsInvalidGraphAndInvalidDirection() throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Path artifactRoot = repositoryRoot.resolve(".project-memory");
    writeBaseArtifacts(artifactRoot);
    Files.writeString(artifactRoot.resolve("project-graph.json"), """
        {
          "graph_schema_version":"1.0",
          "project_map_schema_version":"1.0",
          "nodes":[{"id":"node:module:root","evidence_ids":[]}],
          "edges":[{"id":"edge:bad","source_id":"node:module:root","target_id":"node:missing"}],
          "relation_statuses":[],
          "warnings":[]
        }
        """);

    CliResult invalidGraph = runCli(
        "query",
        repositoryRoot.toString(),
        "relations",
        "node:module:root");
    CliResult invalidDirection = runCli(
        "query",
        repositoryRoot.toString(),
        "relations",
        "node:module:root",
        "--direction",
        "sideways");

    assertAll(
        () -> assertEquals(3, invalidGraph.exitCode()),
        () -> assertTrue(invalidGraph.stdout().isEmpty()),
        () -> assertTrue(invalidGraph.stderr().contains(
            "Query input error: Invalid project-graph.json graph reference.")),
        () -> assertEquals(2, invalidDirection.exitCode()),
        () -> assertTrue(invalidDirection.stdout().isEmpty()),
        () -> assertTrue(invalidDirection.stderr().contains("Unsupported --direction value.")),
        () -> assertTrue(invalidDirection.stderr().contains("Usage: agent-project-memory query")),
        () -> assertFalse(invalidDirection.stderr().contains(repositoryRoot.toString())));
  }

  @Test
  void queryExplainEvidenceRedactsLegacyUnredactedExcerptWithoutMutatingArtifacts()
      throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Path artifactRoot = repositoryRoot.resolve(".project-memory");
    String evidenceIndex = evidenceRecord(
        "ev:legacy:secret",
        "annotation",
        "src/main/java/com/example/SecretController.java",
        "com.example.SecretController",
        "secret",
        "@Value",
        7,
        7,
        "@Value(\"password=FAKE_V170_QUERY_EXCERPT_SECRET\")",
        "high");
    writeArtifacts(
        artifactRoot,
        "{\"schema_version\":\"1.0\",\"project\":{\"modules\":{\"items\":[]}}}\n",
        evidenceIndex);

    CliResult result = runCli(
        "query",
        repositoryRoot.toString(),
        "explain",
        "evidence",
        "ev:legacy:secret");

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertTrue(result.stdout().contains("1. ev:legacy:secret")),
        () -> assertTrue(result.stdout().contains(
            "path: src/main/java/com/example/SecretController.java")),
        () -> assertTrue(result.stdout().contains("symbol_name: @Value")),
        () -> assertTrue(result.stdout().contains(
            "excerpt: @Value(\"password=" + OutputRedactor.REDACTION_MARKER + "\")")),
        () -> assertFalse(result.stdout().contains("FAKE_V170_QUERY_EXCERPT_SECRET")),
        () -> assertTrue(result.stderr().isEmpty()),
        () -> assertTrue(Files.readString(artifactRoot.resolve("evidence-index.jsonl"))
            .contains("FAKE_V170_QUERY_EXCERPT_SECRET")));
  }

  @Test
  void queryExplainEvidenceRedactsLegacyJsonStyleQuotedCredentialExcerptWithoutMutatingArtifacts()
      throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Path artifactRoot = repositoryRoot.resolve(".project-memory");
    String evidenceIndex = evidenceRecord(
        "ev:legacy:json-secret",
        "annotation",
        "src/main/java/com/example/SecretController.java",
        "com.example.SecretController",
        "secret",
        "@GetMapping",
        7,
        7,
        "metadata {\"password\":\"FAKE_V170_QUERY_JSON_EXCERPT_SECRET\"}",
        "high");
    writeArtifacts(
        artifactRoot,
        "{\"schema_version\":\"1.0\",\"project\":{\"modules\":{\"items\":[]}}}\n",
        evidenceIndex);

    CliResult result = runCli(
        "query",
        repositoryRoot.toString(),
        "explain",
        "evidence",
        "ev:legacy:json-secret");

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertTrue(result.stdout().contains("1. ev:legacy:json-secret")),
        () -> assertTrue(result.stdout().contains(
            "path: src/main/java/com/example/SecretController.java")),
        () -> assertTrue(result.stdout().contains("symbol_name: @GetMapping")),
        () -> assertTrue(result.stdout().contains(
            "excerpt: metadata {\"password\":\"" + OutputRedactor.REDACTION_MARKER + "\"}")),
        () -> assertFalse(result.stdout().contains("FAKE_V170_QUERY_JSON_EXCERPT_SECRET")),
        () -> assertTrue(result.stderr().isEmpty()),
        () -> assertTrue(Files.readString(artifactRoot.resolve("evidence-index.jsonl"))
            .contains("FAKE_V170_QUERY_JSON_EXCERPT_SECRET")));
  }

  @Test
  void queryExplainEvidenceRedactsLegacyWrappedAuthorizationCredentialsWithoutMutatingArtifacts()
      throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Path artifactRoot = repositoryRoot.resolve(".project-memory");
    String evidenceIndex = evidenceRecord(
        "ev:legacy:wrapped-auth-secret",
        "annotation",
        "src/main/java/com/example/SecretController.java",
        "com.example.SecretController",
        "secret",
        "@GetMapping",
        7,
        7,
        "headers Authorization: Bearer \"FAKE_V200_QUERY_QUOTED_SECRET\" "
            + "Authorization: Basic <FAKE_V200_QUERY_ANGLE_SECRET> "
            + "Authorization: Bearer `FAKE_V200_QUERY_BACKTICK_SECRET`",
        "high");
    writeArtifacts(
        artifactRoot,
        "{\"schema_version\":\"1.0\",\"project\":{\"modules\":{\"items\":[]}}}\n",
        evidenceIndex);

    CliResult result = runCli(
        "query",
        repositoryRoot.toString(),
        "explain",
        "evidence",
        "ev:legacy:wrapped-auth-secret");

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertTrue(result.stdout().contains("1. ev:legacy:wrapped-auth-secret")),
        () -> assertTrue(result.stdout().contains(
            "Authorization: Bearer \"" + OutputRedactor.REDACTION_MARKER + "\"")),
        () -> assertTrue(result.stdout().contains(
            "Authorization: Basic <" + OutputRedactor.REDACTION_MARKER + ">")),
        () -> assertTrue(result.stdout().contains(
            "Authorization: Bearer `" + OutputRedactor.REDACTION_MARKER + "`")),
        () -> assertFalse(result.stdout().contains("FAKE_V200_QUERY_QUOTED_SECRET")),
        () -> assertFalse(result.stdout().contains("FAKE_V200_QUERY_ANGLE_SECRET")),
        () -> assertFalse(result.stdout().contains("FAKE_V200_QUERY_BACKTICK_SECRET")),
        () -> assertTrue(result.stderr().isEmpty()),
        () -> assertTrue(Files.readString(artifactRoot.resolve("evidence-index.jsonl"))
            .contains("FAKE_V200_QUERY_QUOTED_SECRET")),
        () -> assertTrue(Files.readString(artifactRoot.resolve("evidence-index.jsonl"))
            .contains("FAKE_V200_QUERY_ANGLE_SECRET")),
        () -> assertTrue(Files.readString(artifactRoot.resolve("evidence-index.jsonl"))
            .contains("FAKE_V200_QUERY_BACKTICK_SECRET")));
  }

  @Test
  void queryListTestsRedactsLegacyDisplayNameWhilePreservingNavigationFields()
      throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Path artifactRoot = repositoryRoot.resolve(".project-memory");
    writeArtifacts(artifactRoot, """
        {
          "schema_version": "1.0",
          "tests": {
            "items": [
              {
                "id": "test:com.example.SecretControllerTest",
                "module_id": "module:.",
                "class_name": "com.example.SecretControllerTest",
                "methods": [
                  {
                    "method_name": "usesSecretDisplayName",
                    "test_annotation": "@Test",
                    "method_kind": "test",
                    "display_name": "password=FAKE_V170_QUERY_DISPLAY_SECRET",
                    "evidence_ids": ["ev:test:secret"]
                  }
                ],
                "framework_signals": [],
                "spring_test_slices": [],
                "mock_signals": [],
                "tested_subjects": [],
                "evidence_ids": ["ev:test:secret"]
              }
            ]
          }
        }
        """, evidenceRecord(
        "ev:test:secret",
        "test_file",
        "src/test/java/com/example/SecretControllerTest.java",
        "com.example.SecretControllerTest",
        null,
        "com.example.SecretControllerTest",
        5,
        5,
        "class SecretControllerTest",
        "high"));

    CliResult result = runCli("query", repositoryRoot.toString(), "list", "tests");

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertTrue(result.stdout().contains("test:com.example.SecretControllerTest")),
        () -> assertTrue(result.stdout().contains("class_name: com.example.SecretControllerTest")),
        () -> assertTrue(result.stdout().contains("evidence_ids=ev:test:secret")),
        () -> assertTrue(result.stdout().contains(
            "display_name=password=" + OutputRedactor.REDACTION_MARKER)),
        () -> assertFalse(result.stdout().contains("FAKE_V170_QUERY_DISPLAY_SECRET")),
        () -> assertTrue(result.stderr().isEmpty()));
  }

  @Test
  void queryListApiOperationsRedactsLegacyTagsWhilePreservingNavigationFields()
      throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Path artifactRoot = repositoryRoot.resolve(".project-memory");
    writeArtifacts(artifactRoot, """
        {
          "schema_version": "1.0",
          "api_surface": {
            "openapi": {
              "operations": {
                "items": [
                  {
                    "id": "api-operation:get:/redaction",
                    "module_id": "module:.",
                    "api_surface_category": "interface_declared_spring_mvc_endpoint",
                    "spec_path": "src/main/resources/openapi.yml",
                    "http_method": "GET",
                    "path": "/redaction",
                    "operation_id": "redaction",
                    "tags": ["password=FAKE_V170_QUERY_TAG_SECRET"],
                    "implementation_status": "not_analyzed",
                    "evidence_ids": ["ev:api:secret"]
                  }
                ]
              }
            }
          }
        }
        """, evidenceRecord(
        "ev:api:secret",
        "api_spec",
        "src/main/resources/openapi.yml",
        null,
        null,
        "operation:get:/redaction",
        7,
        7,
        "operation get /redaction",
        "high"));

    CliResult result = runCli("query", repositoryRoot.toString(), "list", "api-operations");

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertTrue(result.stdout().contains("api-operation:get:/redaction")),
        () -> assertTrue(result.stdout().contains("path: /redaction")),
        () -> assertTrue(result.stdout().contains("evidence_ids: ev:api:secret")),
        () -> assertTrue(result.stdout().contains(
            "tags: password=" + OutputRedactor.REDACTION_MARKER)),
        () -> assertFalse(result.stdout().contains("FAKE_V170_QUERY_TAG_SECRET")),
        () -> assertTrue(result.stderr().isEmpty()));
  }

  @Test
  void queryRelationsRedactsLegacyGraphLabelsAndAttributes()
      throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Path artifactRoot = repositoryRoot.resolve(".project-memory");
    writeArtifacts(artifactRoot, richProjectMap(), lookupEvidenceRecords());
    Files.writeString(artifactRoot.resolve("project-graph.json"), """
        {
          "graph_schema_version": "1.0",
          "project_map_schema_version": "1.0",
          "nodes": [
            {
              "id": "node:secret",
              "kind": "type",
              "label": "Authorization: Bearer FAKE_V170_GRAPH_LABEL_SECRET",
              "claim_category": "extracted",
              "module_id": "module:.",
              "source_ref": {
                "artifact": "project-map.json",
                "section": "components.items",
                "id": "component:com.example.SecretController"
              },
              "evidence_ids": ["ev:pom.xml:1-1:build_file:pom.xml"]
            }
          ],
          "edges": [],
          "relation_statuses": [
            {
              "id": "relation-status:secret",
              "relation_family": "tested_subject",
              "source_id": "node:secret",
              "target_id": null,
              "relation_status": "not_detected",
              "support_type": "status_only",
              "confidence": "low",
              "uncertainty": "no_supported_subject_signal",
              "relation_attributes": {
                "client_secret": "FAKE_V170_GRAPH_ATTR_SECRET",
                "note": "password=FAKE_V170_GRAPH_NOTE_SECRET"
              },
              "derivation": null,
              "evidence_ids": []
            }
          ],
          "warnings": []
        }
        """);

    CliResult result = runCli("query", repositoryRoot.toString(), "relations", "node:secret");

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertTrue(result.stdout().contains("Resolved node: node:secret")),
        () -> assertTrue(result.stdout().contains(
            "label: Authorization: Bearer " + OutputRedactor.REDACTION_MARKER)),
        () -> assertTrue(result.stdout().contains(
            "relation_attributes: client_secret=" + OutputRedactor.REDACTION_MARKER)),
        () -> assertTrue(result.stdout().contains(
            "note=password=" + OutputRedactor.REDACTION_MARKER)),
        () -> assertFalse(result.stdout().contains("FAKE_V170_GRAPH_LABEL_SECRET")),
        () -> assertFalse(result.stdout().contains("FAKE_V170_GRAPH_ATTR_SECRET")),
        () -> assertFalse(result.stdout().contains("FAKE_V170_GRAPH_NOTE_SECRET")),
        () -> assertTrue(result.stderr().isEmpty()));
  }

  @Test
  void queryGraphRenderingRedactsLegacySourceRefIdWithoutChangingLookupSemantics()
      throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Path artifactRoot = repositoryRoot.resolve(".project-memory");
    writeArtifacts(artifactRoot, richProjectMap(), lookupEvidenceRecords());
    String legacySourceRefId =
        "document_heading:README.md:heading:Authorization: Bearer "
            + "FAKE_V170_GRAPH_SOURCE_REF_SECRET:occ:000001";
    String legacyGraphArtifact = """
        {
          "graph_schema_version": "1.0",
          "project_map_schema_version": "1.0",
          "nodes": [
            {
              "id": "node:secret",
              "kind": "document_heading",
              "label": "Authorization: Bearer FAKE_V170_GRAPH_LABEL_SECRET",
              "claim_category": "document_backed",
              "module_id": null,
              "source_ref": {
                "artifact": "project-map.json",
                "section": "documents.items[].headings[]",
                "id": "%s"
              },
              "evidence_ids": []
            }
          ],
          "edges": [],
          "relation_statuses": [],
          "warnings": []
        }
        """.formatted(legacySourceRefId);
    Files.writeString(artifactRoot.resolve("project-graph.json"), legacyGraphArtifact);

    CliResult relationsByNodeId = runCli(
        "query",
        repositoryRoot.toString(),
        "relations",
        "node:secret");
    CliResult relationsBySourceRefId = runCli(
        "query",
        repositoryRoot.toString(),
        "relations",
        legacySourceRefId);
    CliResult findByNodeId = runCli(
        "query",
        repositoryRoot.toString(),
        "find",
        "fact",
        "node:secret");
    String redactedSourceRefId = OutputRedactor.redact(legacySourceRefId);

    assertAll(
        () -> assertTrue(redactedSourceRefId.contains(OutputRedactor.REDACTION_MARKER)),
        () -> assertFalse(redactedSourceRefId.contains("FAKE_V170_GRAPH_SOURCE_REF_SECRET")),
        () -> assertEquals(0, relationsByNodeId.exitCode()),
        () -> assertTrue(relationsByNodeId.stdout().contains("Resolved by: node id")),
        () -> assertTrue(relationsByNodeId.stdout().contains("id=" + redactedSourceRefId)),
        () -> assertFalse(relationsByNodeId.stdout().contains("FAKE_V170_GRAPH_SOURCE_REF_SECRET")),
        () -> assertEquals(0, relationsBySourceRefId.exitCode()),
        () -> assertTrue(relationsBySourceRefId.stdout().contains("Resolved by: source_ref.id")),
        () -> assertTrue(relationsBySourceRefId.stdout().contains("Subject: " + redactedSourceRefId)),
        () -> assertTrue(relationsBySourceRefId.stdout().contains("id=" + redactedSourceRefId)),
        () -> assertFalse(relationsBySourceRefId.stdout().contains("FAKE_V170_GRAPH_SOURCE_REF_SECRET")),
        () -> assertEquals(0, findByNodeId.exitCode()),
        () -> assertTrue(findByNodeId.stdout().contains("navigation: project-graph.json#/nodes/0")),
        () -> assertTrue(findByNodeId.stdout().contains("id=" + redactedSourceRefId)),
        () -> assertFalse(findByNodeId.stdout().contains("FAKE_V170_GRAPH_SOURCE_REF_SECRET")),
        () -> assertTrue(relationsByNodeId.stderr().isEmpty()),
        () -> assertTrue(relationsBySourceRefId.stderr().isEmpty()),
        () -> assertTrue(findByNodeId.stderr().isEmpty()),
        () -> assertEquals(legacyGraphArtifact, Files.readString(artifactRoot.resolve("project-graph.json"))));
  }

  @Test
  void queryMissingPathAndNonDirectoryPathReturnQueryInputErrorsWithoutAbsolutePath()
      throws Exception {
    Path missingPath = tempDir.resolve("missing");
    Path filePath = tempDir.resolve("artifact.txt");
    Files.writeString(filePath, "not a directory");

    CliResult missing = runCli("query", missingPath.toString(), "list", "modules");
    CliResult nonDirectory = runCli("query", filePath.toString(), "list", "modules");

    assertAll(
        () -> assertEquals(3, runCli("query").exitCode()),
        () -> assertEquals(3, missing.exitCode()),
        () -> assertTrue(missing.stderr().contains("Query path does not exist.")),
        () -> assertFalse(missing.stderr().contains(missingPath.toString())),
        () -> assertEquals(3, nonDirectory.exitCode()),
        () -> assertTrue(nonDirectory.stderr().contains("Query path is not a directory.")),
        () -> assertFalse(nonDirectory.stderr().contains(filePath.toString())));
  }

  @Test
  void queryMissingArtifactRootDoesNotCreateProjectMemory() throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Files.createDirectories(repositoryRoot);

    CliResult result = runCli("query", repositoryRoot.toString(), "list", "modules");

    assertAll(
        () -> assertEquals(3, result.exitCode()),
        () -> assertTrue(result.stdout().isEmpty()),
        () -> assertTrue(result.stderr().contains("Missing .project-memory artifact root.")),
        () -> assertFalse(Files.exists(repositoryRoot.resolve(".project-memory"))));
  }

  @Test
  void queryMalformedArtifactErrorIsBoundedAndDoesNotWrite() throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Path artifactRoot = repositoryRoot.resolve(".project-memory");
    writeBaseArtifacts(artifactRoot);
    Files.writeString(artifactRoot.resolve("project-map.json"), "{not-json-with-SECRET_TOKEN}");
    Files.createDirectories(artifactRoot.resolve("cache/v1"));
    Files.writeString(artifactRoot.resolve("cache/v1/manifest.json"), "cache metadata");

    CliResult result = runCli("query", repositoryRoot.toString(), "list", "modules");

    assertAll(
        () -> assertEquals(3, result.exitCode()),
        () -> assertTrue(result.stdout().isEmpty()),
        () -> assertTrue(result.stderr().contains("Malformed project-map.json.")),
        () -> assertFalse(result.stderr().contains("SECRET_TOKEN")),
        () -> assertFalse(result.stderr().contains(repositoryRoot.toString())),
        () -> assertEquals("cache metadata", Files.readString(artifactRoot.resolve("cache/v1/manifest.json"))),
        () -> assertFalse(Files.exists(artifactRoot.resolve("agent-profiles"))));
  }

  @Test
  void queryUsageErrorsDoNotLoadOrWriteArtifacts() throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Files.createDirectories(repositoryRoot);

    CliResult result = runCli("query", repositoryRoot.toString(), "list", "components");

    assertAll(
        () -> assertEquals(2, result.exitCode()),
        () -> assertTrue(result.stdout().isEmpty()),
        () -> assertTrue(result.stderr().contains("Unsupported query list subject.")),
        () -> assertTrue(result.stderr().contains("Usage: agent-project-memory query")),
        () -> assertFalse(Files.exists(repositoryRoot.resolve(".project-memory"))));
  }

  @Test
  void queryAgentContextRendersDeterministicStdoutWithOptionalGraph() throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Path artifactRoot = repositoryRoot.resolve(".project-memory");
    writeArtifacts(artifactRoot, richProjectMap(), lookupEvidenceRecords());
    writeRelationGraph(artifactRoot);

    CliResult first = runCli("query", repositoryRoot.toString(), "agent-context");
    CliResult second = runCli("query", repositoryRoot.toString(), "agent-context");

    assertAll(
        () -> assertEquals(0, first.exitCode()),
        () -> assertEquals(first.stdout(), second.stdout()),
        () -> assertTrue(first.stdout().contains("Query: agent-context")),
        () -> assertTrue(first.stdout().contains(
            "Source artifacts: project-map.json schema_version=1.0, evidence-index.jsonl records=")),
        () -> assertTrue(first.stdout().contains(
            "Optional graph: project-graph.json graph_schema_version=1.0 (navigation metadata, not evidence)")),
        () -> assertTrue(first.stdout().contains("query <path> list modules")),
        () -> assertTrue(first.stdout().contains("query <path> explain evidence <evidence-id>")),
        () -> assertTrue(first.stdout().contains(
            "- source-visible endpoints: 1 (first: endpoint:com.example.web.OrderController#getOrder; evidence_ids: ev:endpoint:controller, ev:endpoint:mapping)")),
        () -> assertTrue(first.stdout().contains("- evidence records: ")),
        () -> assertTrue(first.stdout().contains("ev:pom.xml:1-1:build_file:pom.xml")),
        () -> assertTrue(first.stdout().contains("ev:endpoint:mapping")),
        () -> assertTrue(first.stdout().contains("- nodes: 4")),
        () -> assertTrue(first.stdout().contains("- edges: 3")),
        () -> assertTrue(first.stdout().contains(
            "- source_ref and derivation fields are navigation metadata, not evidence.")),
        () -> assertTrue(first.stdout().contains(
            "- existing evidence IDs are preserved; no evidence records or evidence IDs are created.")),
        () -> assertFalse(first.stdout().contains(repositoryRoot.toString())),
        () -> assertTrue(first.stderr().isEmpty()),
        () -> assertTrue(second.stderr().isEmpty()));
  }

  @Test
  void queryAgentContextDoesNotReadOrMutateNonInputArtifacts() throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Path artifactRoot = repositoryRoot.resolve(".project-memory");
    writeBaseArtifacts(artifactRoot);
    Files.writeString(
        artifactRoot.resolve("source-registry.json"),
        "{\"source\":\"password=FAKE_AGENT_CONTEXT_SOURCE_REGISTRY_SECRET\"}");
    Files.writeString(
        artifactRoot.resolve("agent-guide.md"),
        "Authorization: Bearer FAKE_AGENT_CONTEXT_GUIDE_SECRET");
    Files.createDirectories(artifactRoot.resolve("agent-profiles"));
    Files.writeString(
        artifactRoot.resolve("agent-profiles/generic.md"),
        "token=FAKE_AGENT_CONTEXT_PROFILE_SECRET");
    Files.createDirectories(artifactRoot.resolve("ai-presentations"));
    Files.writeString(
        artifactRoot.resolve("ai-presentations/brief.md"),
        "client_secret=FAKE_AGENT_CONTEXT_AI_SECRET");
    Files.createDirectories(artifactRoot.resolve("cache/v1"));
    Files.writeString(
        artifactRoot.resolve("cache/v1/manifest.json"),
        "api_key=FAKE_AGENT_CONTEXT_CACHE_SECRET");

    CliResult result = runCli("query", repositoryRoot.toString(), "agent-context");

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertTrue(result.stdout().contains("Optional graph: not loaded")),
        () -> assertTrue(result.stdout().contains(
            "source-registry.json, adapter context rows, connector records")),
        () -> assertTrue(result.stdout().contains(
            "AI presentation artifacts, if present, are optional non-authoritative/non-evidence")),
        () -> assertFalse(result.stdout().contains("FAKE_AGENT_CONTEXT_SOURCE_REGISTRY_SECRET")),
        () -> assertFalse(result.stdout().contains("FAKE_AGENT_CONTEXT_GUIDE_SECRET")),
        () -> assertFalse(result.stdout().contains("FAKE_AGENT_CONTEXT_PROFILE_SECRET")),
        () -> assertFalse(result.stdout().contains("FAKE_AGENT_CONTEXT_AI_SECRET")),
        () -> assertFalse(result.stdout().contains("FAKE_AGENT_CONTEXT_CACHE_SECRET")),
        () -> assertTrue(result.stderr().isEmpty()),
        () -> assertEquals(
            "{\"source\":\"password=FAKE_AGENT_CONTEXT_SOURCE_REGISTRY_SECRET\"}",
            Files.readString(artifactRoot.resolve("source-registry.json"))),
        () -> assertEquals(
            "Authorization: Bearer FAKE_AGENT_CONTEXT_GUIDE_SECRET",
            Files.readString(artifactRoot.resolve("agent-guide.md"))),
        () -> assertEquals(
            "token=FAKE_AGENT_CONTEXT_PROFILE_SECRET",
            Files.readString(artifactRoot.resolve("agent-profiles/generic.md"))),
        () -> assertEquals(
            "client_secret=FAKE_AGENT_CONTEXT_AI_SECRET",
            Files.readString(artifactRoot.resolve("ai-presentations/brief.md"))),
        () -> assertEquals(
            "api_key=FAKE_AGENT_CONTEXT_CACHE_SECRET",
            Files.readString(artifactRoot.resolve("cache/v1/manifest.json"))),
        () -> assertFalse(Files.exists(artifactRoot.resolve("project-graph.json"))));
  }

  @Test
  void queryAgentContextRejectsExtraArgsAdapterSchemaAndMalformedGraphSafely()
      throws Exception {
    Path usageRoot = tempDir.resolve("usage-repo");
    Files.createDirectories(usageRoot);

    CliResult usage = runCli("query", usageRoot.toString(), "agent-context", "--format", "json");

    Path adapterRoot = tempDir.resolve("adapter-repo/.project-memory");
    writeArtifacts(adapterRoot, """
        {
          "schema_version": "2.0",
          "adapter_context": [
            {"title": "password=FAKE_AGENT_CONTEXT_ADAPTER_SECRET"}
          ]
        }
        """);
    CliResult adapter = runCli(
        "query",
        adapterRoot.getParent().toString(),
        "agent-context");

    Path graphRoot = tempDir.resolve("graph-repo");
    Path graphArtifactRoot = graphRoot.resolve(".project-memory");
    writeBaseArtifacts(graphArtifactRoot);
    writeMalformedGraph(graphArtifactRoot);
    CliResult graph = runCli("query", graphRoot.toString(), "agent-context");

    assertAll(
        () -> assertEquals(2, usage.exitCode()),
        () -> assertTrue(usage.stdout().isEmpty()),
        () -> assertTrue(usage.stderr().contains("Malformed agent-context query command.")),
        () -> assertFalse(Files.exists(usageRoot.resolve(".project-memory"))),
        () -> assertEquals(3, adapter.exitCode()),
        () -> assertTrue(adapter.stdout().isEmpty()),
        () -> assertTrue(adapter.stderr().contains("Unsupported project-map.json schema_version.")),
        () -> assertFalse(adapter.stderr().contains("FAKE_AGENT_CONTEXT_ADAPTER_SECRET")),
        () -> assertEquals(3, graph.exitCode()),
        () -> assertTrue(graph.stdout().isEmpty()),
        () -> assertTrue(graph.stderr().contains("Malformed project-graph.json.")),
        () -> assertFalse(graph.stderr().contains("SECRET_TOKEN")),
        () -> assertFalse(graph.stderr().contains(graphRoot.toString())));
  }

  @Test
  void queryImpactRendersDirectMatchesNotRepresentedDiagnosticsAndDoesNotWrite()
      throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Path artifactRoot = repositoryRoot.resolve(".project-memory");
    writeImpactArtifacts(artifactRoot);
    Path sourceFile = repositoryRoot.resolve("src/main/java/com/example/web/OrderController.java");
    Files.createDirectories(sourceFile.getParent());
    Files.writeString(sourceFile, "password=FAKE_IMPACT_SOURCE_READBACK_SECRET");
    Files.createDirectories(artifactRoot.resolve("cache/v1"));
    Files.writeString(artifactRoot.resolve("cache/v1/manifest.json"), "cache metadata");
    String projectMapBefore = Files.readString(artifactRoot.resolve("project-map.json"));
    String evidenceBefore = Files.readString(artifactRoot.resolve("evidence-index.jsonl"));
    String graphBefore = Files.readString(artifactRoot.resolve("project-graph.json"));

    CliResult result = runCli(
        "query",
        repositoryRoot.toString(),
        "impact",
        "--files",
        "src/main/java/com/example/web/OrderController.java",
        "src/main/java/com/example/web/OrderController.java",
        "src/main/resources/openapi.yml",
        "src/main/java/com/example/Missing.java");

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertTrue(result.stderr().isEmpty()),
        () -> assertTrue(result.stdout().contains("Query: impact")),
        () -> assertTrue(result.stdout().contains(
            "Source artifacts: project-map.json schema_version=1.0, evidence-index.jsonl records=4, project-graph.json graph_schema_version=1.0")),
        () -> assertTrue(result.stdout().contains(
            "Results: direct_match=8, graph_neighbor=0, relation_status=0, planning_hint=0, not_represented=1, diagnostic=1")),
        () -> assertTrue(result.stdout().contains("1. direct_match")),
        () -> assertTrue(result.stdout().contains("match_type: evidence_path")),
        () -> assertTrue(result.stdout().contains("match_type: fact_evidence_path")),
        () -> assertTrue(result.stdout().contains("match_type: source_reference_path")),
        () -> assertTrue(result.stdout().contains("match_type: graph_node")),
        () -> assertTrue(result.stdout().contains(
            "fact_id: endpoint:com.example.web.OrderController#getOrder")),
        () -> assertTrue(result.stdout().contains(
            "node_id: node:endpoint:endpoint%3Acom.example.web.OrderController%23getOrder")),
        () -> assertTrue(result.stdout().contains("evidence_id: ev:endpoint:mapping")),
        () -> assertTrue(result.stdout().contains("matched_field: spec_path")),
        () -> assertTrue(result.stdout().contains("1. not_represented")),
        () -> assertTrue(result.stdout().contains(
            "changed_file: src/main/java/com/example/Missing.java")),
        () -> assertTrue(result.stdout().contains("code: duplicate_changed_file")),
        () -> assertTrue(result.stdout().contains(
            "No graph_neighbor rows.")),
        () -> assertTrue(result.stdout().contains("No relation_status rows.")),
        () -> assertTrue(result.stdout().contains("No planning_hint rows.")),
        () -> assertTrue(result.stdout().contains(
            "no transitive graph traversal, raw diff parsing, Git inspection, source readback")),
        () -> assertFalse(result.stdout().contains("FAKE_IMPACT_SOURCE_READBACK_SECRET")),
        () -> assertFalse(result.stdout().contains(repositoryRoot.toString())),
        () -> assertEquals(projectMapBefore, Files.readString(artifactRoot.resolve("project-map.json"))),
        () -> assertEquals(evidenceBefore, Files.readString(artifactRoot.resolve("evidence-index.jsonl"))),
        () -> assertEquals(graphBefore, Files.readString(artifactRoot.resolve("project-graph.json"))),
        () -> assertEquals("cache metadata", Files.readString(artifactRoot.resolve("cache/v1/manifest.json"))),
        () -> assertFalse(Files.exists(artifactRoot.resolve("impact-report.json"))),
        () -> assertEquals(
            "password=FAKE_IMPACT_SOURCE_READBACK_SECRET",
            Files.readString(sourceFile)));
  }

  @Test
  void queryImpactProjectsOneHopGraphNeighborsRelationStatusesAndPlanningHints()
      throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Path artifactRoot = repositoryRoot.resolve(".project-memory");
    writeImpactProjectionArtifacts(artifactRoot);
    Path sourceFile = repositoryRoot.resolve("src/main/java/com/example/web/OrderController.java");
    Files.createDirectories(sourceFile.getParent());
    Files.writeString(sourceFile, "secret=FAKE_IMPACT_PROJECTION_SOURCE_READBACK_SECRET");
    String projectMapBefore = Files.readString(artifactRoot.resolve("project-map.json"));
    String evidenceBefore = Files.readString(artifactRoot.resolve("evidence-index.jsonl"));
    String graphBefore = Files.readString(artifactRoot.resolve("project-graph.json"));

    CliResult result = runCli(
        "query",
        repositoryRoot.toString(),
        "impact",
        "--files",
        "src/main/java/com/example/web/OrderController.java");

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertTrue(result.stderr().isEmpty()),
        () -> assertTrue(result.stdout().contains(
            "Results: direct_match=5, graph_neighbor=2, relation_status=1, planning_hint=1, not_represented=0, diagnostic=0")),
        () -> assertTrue(result.stdout().contains("1. graph_neighbor")),
        () -> assertTrue(result.stdout().contains("edge_type: declares")),
        () -> assertTrue(result.stdout().contains("neighbor_node_id: node:type:com.example.web.OrderController")),
        () -> assertTrue(result.stdout().contains("confidence: low")),
        () -> assertTrue(result.stdout().contains("graph_confidence: high")),
        () -> assertTrue(result.stdout().contains("edge_type: tested_subject")),
        () -> assertTrue(result.stdout().contains("neighbor_node_id: node:test:com.example.web.OrderControllerTest")),
        () -> assertTrue(result.stdout().contains("confidence: medium")),
        () -> assertTrue(result.stdout().contains("1. relation_status")),
        () -> assertTrue(result.stdout().contains("relation_family: tested_subject")),
        () -> assertTrue(result.stdout().contains("uncertainty: no_supported_subject_signal")),
        () -> assertTrue(result.stdout().contains("1. planning_hint")),
        () -> assertTrue(result.stdout().contains("signal: spring_service_change_surface")),
        () -> assertTrue(result.stdout().contains("risk_basis: source_visible_service_stereotype")),
        () -> assertTrue(result.stdout().contains(
            "tied_node_ids: node:endpoint:endpoint%3Acom.example.web.OrderController%23getOrder")),
        () -> assertTrue(result.stdout().contains("No not_represented files.")),
        () -> assertTrue(result.stdout().contains("No diagnostics.")),
        () -> assertTrue(result.stdout().contains(
            "graph_neighbor and relation_status rows are one-hop graph orientation only")),
        () -> assertFalse(result.stdout().contains("FAKE_IMPACT_PROJECTION_SOURCE_READBACK_SECRET")),
        () -> assertFalse(result.stdout().contains(repositoryRoot.toString())),
        () -> assertEquals(projectMapBefore, Files.readString(artifactRoot.resolve("project-map.json"))),
        () -> assertEquals(evidenceBefore, Files.readString(artifactRoot.resolve("evidence-index.jsonl"))),
        () -> assertEquals(graphBefore, Files.readString(artifactRoot.resolve("project-graph.json"))),
        () -> assertEquals(
            "secret=FAKE_IMPACT_PROJECTION_SOURCE_READBACK_SECRET",
            Files.readString(sourceFile)),
        () -> assertFalse(Files.exists(artifactRoot.resolve("impact-report.json"))));
  }

  @Test
  void queryImpactRejectsUnsafeChangedFileInputsBeforeArtifactLoading() throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Files.createDirectories(repositoryRoot);
    List<String> invalidInputs = List.of(
        tempDir.resolve("repo/src/main/java/Foo.java").toString(),
        "./src/main/java/Foo.java",
        "../src/main/java/Foo.java",
        "src/../Foo.java",
        "src\\main\\java\\Foo.java",
        "https://example.com/Foo.java",
        "",
        ".project-memory/project-map.json",
        "*.java",
        "src/main/java/[A-Z]+.java",
        "^src/main/java/Foo.java$",
        "diff --git a/src/Foo.java b/src/Foo.java\nindex 123..456");

    for (String invalidInput : invalidInputs) {
      CliResult result = runCli(
          "query",
          repositoryRoot.toString(),
          "impact",
          "--files",
          invalidInput);
      assertAll(
          () -> assertEquals(2, result.exitCode(), "input=" + invalidInput),
          () -> assertTrue(result.stdout().isEmpty(), "input=" + invalidInput),
          () -> assertTrue(result.stderr().contains("Invalid changed-file path."), "input=" + invalidInput),
          () -> assertTrue(result.stderr().contains("Usage: agent-project-memory query"), "input=" + invalidInput),
          () -> assertFalse(Files.exists(repositoryRoot.resolve(".project-memory")), "input=" + invalidInput));
    }
  }

  @Test
  void queryImpactRejectsForbiddenAndUnsupportedArgumentShapesBeforeArtifactLoading()
      throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Files.createDirectories(repositoryRoot);

    CliResult topLevel = runCli("impact", "--files", "src/main/java/Foo.java");
    CliResult fromGitDiff = runCli(
        "query",
        repositoryRoot.toString(),
        "impact",
        "--from-git-diff");
    CliResult missingFiles = runCli("query", repositoryRoot.toString(), "impact");
    CliResult emptyFiles = runCli("query", repositoryRoot.toString(), "impact", "--files");
    CliResult duplicateFilesFlag = runCli(
        "query",
        repositoryRoot.toString(),
        "impact",
        "--files",
        "src/main/java/Foo.java",
        "--files",
        "src/main/java/Bar.java");

    assertAll(
        () -> assertEquals(2, topLevel.exitCode()),
        () -> assertTrue(topLevel.stderr().contains("Unknown command.")),
        () -> assertEquals(2, fromGitDiff.exitCode()),
        () -> assertTrue(fromGitDiff.stderr().contains("Malformed impact query command.")),
        () -> assertEquals(2, missingFiles.exitCode()),
        () -> assertTrue(missingFiles.stderr().contains("Malformed impact query command.")),
        () -> assertEquals(2, emptyFiles.exitCode()),
        () -> assertTrue(emptyFiles.stderr().contains("Missing changed-file value.")),
        () -> assertEquals(2, duplicateFilesFlag.exitCode()),
        () -> assertTrue(duplicateFilesFlag.stderr().contains("Duplicate --files flag.")),
        () -> assertFalse(Files.exists(repositoryRoot.resolve(".project-memory"))));
  }

  @Test
  void queryImpactFailsClosedForMissingUnsupportedAndInvalidArtifacts() throws Exception {
    Path missingGraphRoot = tempDir.resolve("missing-graph/.project-memory");
    writeBaseArtifacts(missingGraphRoot);

    Path adapterRoot = tempDir.resolve("adapter/.project-memory");
    writeArtifacts(adapterRoot, "{\"schema_version\":\"2.0\",\"adapter_context\":[]}\n");
    writeValidGraph(adapterRoot);

    Path danglingRoot = tempDir.resolve("dangling/.project-memory");
    writeArtifacts(danglingRoot, """
        {
          "schema_version": "1.0",
          "endpoints": [
            {
              "id": "endpoint:missing",
              "evidence_ids": ["ev:missing"]
            }
          ]
        }
        """);
    writeValidGraph(danglingRoot);

    Path duplicateRoot = tempDir.resolve("duplicate/.project-memory");
    writeArtifacts(duplicateRoot, """
        {
          "schema_version": "1.0",
          "endpoints": [
            {
              "id": "endpoint:duplicate",
              "evidence_ids": ["ev:pom.xml:1-1:build_file:pom.xml"]
            },
            {
              "id": "endpoint:duplicate",
              "evidence_ids": ["ev:pom.xml:1-1:build_file:pom.xml"]
            }
          ]
        }
        """);
    writeValidGraph(duplicateRoot);

    CliResult missingGraph = runCli(
        "query",
        missingGraphRoot.getParent().toString(),
        "impact",
        "--files",
        "pom.xml");
    CliResult adapter = runCli(
        "query",
        adapterRoot.getParent().toString(),
        "impact",
        "--files",
        "pom.xml");
    CliResult dangling = runCli(
        "query",
        danglingRoot.getParent().toString(),
        "impact",
        "--files",
        "pom.xml");
    CliResult duplicate = runCli(
        "query",
        duplicateRoot.getParent().toString(),
        "impact",
        "--files",
        "pom.xml");

    assertAll(
        () -> assertEquals(3, missingGraph.exitCode()),
        () -> assertTrue(missingGraph.stderr().contains("Missing project-graph.json.")),
        () -> assertEquals(3, adapter.exitCode()),
        () -> assertTrue(adapter.stderr().contains("Unsupported project-map.json schema_version.")),
        () -> assertEquals(3, dangling.exitCode()),
        () -> assertTrue(dangling.stderr().contains("Invalid project-map.json evidence reference.")),
        () -> assertEquals(3, duplicate.exitCode()),
        () -> assertTrue(duplicate.stderr().contains("Duplicate generated fact id in project-map.json.")),
        () -> assertTrue(missingGraph.stdout().isEmpty()),
        () -> assertTrue(adapter.stdout().isEmpty()),
        () -> assertTrue(dangling.stdout().isEmpty()),
        () -> assertTrue(duplicate.stdout().isEmpty()));
  }

  @Test
  void queryImpactIgnoresUnbackedProjectMapPathLikeFields() throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Path artifactRoot = repositoryRoot.resolve(".project-memory");
    writeArtifacts(
        artifactRoot,
        """
            {
              "schema_version": "1.0",
              "custom": {
                "items": [
                  {
                    "id": "fact:injected",
                    "path": "src/main/java/com/example/Injected.java",
                    "evidence_ids": ["ev:pom.xml:1-1:build_file:pom.xml"]
                  }
                ]
              }
            }
            """);
    writeValidGraph(artifactRoot);

    CliResult result = runCli(
        "query",
        repositoryRoot.toString(),
        "impact",
        "--files",
        "src/main/java/com/example/Injected.java");

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertTrue(result.stderr().isEmpty()),
        () -> assertTrue(result.stdout().contains("Results: direct_match=0")),
        () -> assertTrue(result.stdout().contains("not_represented=1")),
        () -> assertTrue(result.stdout().contains(
            "changed_file: src/main/java/com/example/Injected.java")),
        () -> assertFalse(result.stdout().contains("fact:injected")),
        () -> assertFalse(result.stdout().contains("source_reference_path")));
  }

  @Test
  void queryImpactCapsChangedFileInputsWithBoundedDiagnostic() throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Path artifactRoot = repositoryRoot.resolve(".project-memory");
    writeImpactArtifacts(artifactRoot);
    List<String> args = new ArrayList<>(List.of(
        "query",
        repositoryRoot.toString(),
        "impact",
        "--files"));
    for (int index = 0; index < 257; index++) {
      args.add("src/main/java/com/example/generated/File" + index + ".java");
    }

    CliResult result = runCli(args.toArray(String[]::new));

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertTrue(result.stderr().isEmpty()),
        () -> assertTrue(result.stdout().contains("Changed files: 256")),
        () -> assertTrue(result.stdout().contains(
            "Results: direct_match=0, graph_neighbor=0, relation_status=0, planning_hint=0, not_represented=256, diagnostic=1")),
        () -> assertTrue(result.stdout().contains("code: input_cap_reached")),
        () -> assertFalse(result.stdout().contains(repositoryRoot.toString())));
  }

  @Test
  void queryImpactCapsGraphProjectionRowsWithBoundedDiagnostic() throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Path artifactRoot = repositoryRoot.resolve(".project-memory");
    writeImpactProjectionCapArtifacts(artifactRoot, 1001);

    CliResult result = runCli(
        "query",
        repositoryRoot.toString(),
        "impact",
        "--files",
        "src/main/java/com/example/web/OrderController.java");

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertTrue(result.stderr().isEmpty()),
        () -> assertTrue(result.stdout().contains(
            "Results: direct_match=3, graph_neighbor=1000, relation_status=0, planning_hint=0, not_represented=0, diagnostic=1")),
        () -> assertTrue(result.stdout().contains("code: graph_projection_cap_reached")),
        () -> assertTrue(result.stdout().contains(
            "Only the first 1000 graph projection rows were rendered.")),
        () -> assertFalse(result.stdout().contains(repositoryRoot.toString())),
        () -> assertFalse(Files.exists(artifactRoot.resolve("impact-report.json"))));
  }

  private CliResult runCli(String... args) {
    StringWriter stdout = new StringWriter();
    StringWriter stderr = new StringWriter();
    AgentProjectMemoryCli cli = new AgentProjectMemoryCli(
        new PrintWriter(stdout),
        new PrintWriter(stderr));
    int exitCode = cli.run(args);
    return new CliResult(exitCode, stdout.toString(), stderr.toString());
  }

  private void writeBaseArtifacts(Path artifactRoot) throws IOException {
    writeArtifacts(artifactRoot, """
        {
          "schema_version": "1.0",
          "project": {
            "modules": {
              "items": [
                {
                  "module_id": "module:.",
                  "module_path": ".",
                  "pom_path": "pom.xml",
                  "source_roots": ["src/main/java"],
                  "test_roots": ["src/test/java"],
                  "support_status": "supported",
                  "declaration_kind": "scan_root",
                  "declared_path": ".",
                  "declaration_evidence_ids": [],
                  "pom_evidence_ids": ["ev:pom.xml:1-1:build_file:pom.xml"]
                }
              ]
            }
          }
        }
        """);
  }

  private void writeArtifacts(Path artifactRoot, String projectMap) throws IOException {
    writeArtifacts(artifactRoot, projectMap, evidenceRecord("ev:pom.xml:1-1:build_file:pom.xml"));
  }

  private void writeArtifacts(Path artifactRoot, String projectMap, String evidenceIndex)
      throws IOException {
    Files.createDirectories(artifactRoot);
    Files.writeString(artifactRoot.resolve("project-map.json"), projectMap);
    Files.writeString(artifactRoot.resolve("evidence-index.jsonl"), evidenceIndex);
  }

  private void writeValidGraph(Path artifactRoot) throws IOException {
    Files.writeString(artifactRoot.resolve("project-graph.json"), """
        {
          "graph_schema_version":"1.0",
          "project_map_schema_version":"1.0",
          "nodes":[{"id":"node:module:root","evidence_ids":[]}],
          "edges":[],
          "relation_statuses":[],
          "warnings":[]
        }
        """);
  }

  private void writeMalformedGraph(Path artifactRoot) throws IOException {
    Files.writeString(artifactRoot.resolve("project-graph.json"), "{not-json-with-SECRET_TOKEN}");
  }

  private void writeLookupGraph(Path artifactRoot) throws IOException {
    Files.writeString(artifactRoot.resolve("project-graph.json"), """
        {
          "graph_schema_version": "1.0",
          "project_map_schema_version": "1.0",
          "nodes": [
            {
              "id": "node:module:root",
              "kind": "module",
              "label": "root",
              "claim_category": "structural",
              "module_id": "module:.",
              "source_ref": {
                "artifact": "project-map.json",
                "section": "project.modules.items",
                "id": "module:."
              },
              "evidence_ids": []
            },
            {
              "id": "node:endpoint:endpoint%3Acom.example.web.OrderController%23getOrder",
              "kind": "endpoint",
              "label": "GET /orders/{id}",
              "claim_category": "extracted",
              "module_id": "module:.",
              "source_ref": {
                "artifact": "project-map.json",
                "section": "endpoints",
                "id": "endpoint:com.example.web.OrderController#getOrder"
              },
              "evidence_ids": ["ev:endpoint:mapping"]
            }
          ],
          "edges": [
            {
              "id": "edge:owns:node:module:root:node:endpoint:endpoint%3Acom.example.web.OrderController%23getOrder",
              "type": "owns",
              "source_id": "node:module:root",
              "target_id": "node:endpoint:endpoint%3Acom.example.web.OrderController%23getOrder",
              "claim_category": "structural",
              "relation_status": "derived",
              "support_type": "project_map_derivation",
              "confidence": "high",
              "uncertainty": null,
              "relation_attributes": {},
              "derivation": {
                "kind": "project_map_field",
                "artifact": "project-map.json",
                "section": "endpoints",
                "fields": ["module_id", "id"]
              },
              "evidence_ids": []
            }
          ],
          "relation_statuses": [],
          "warnings": []
        }
        """);
  }

  private void writeImpactArtifacts(Path artifactRoot) throws IOException {
    writeArtifacts(artifactRoot, impactProjectMap(), impactEvidenceRecords());
    Files.writeString(artifactRoot.resolve("project-graph.json"), impactGraph());
  }

  private String impactProjectMap() {
    return """
        {
          "schema_version": "1.0",
          "project": {
            "modules": {
              "items": [
                {
                  "module_id": "module:.",
                  "module_path": ".",
                  "pom_path": "pom.xml",
                  "source_roots": ["src/main/java"],
                  "test_roots": ["src/test/java"],
                  "support_status": "supported",
                  "declaration_kind": "scan_root",
                  "declared_path": ".",
                  "declaration_evidence_ids": [],
                  "pom_evidence_ids": ["ev:pom.xml:1-1:build_file:pom.xml"]
                }
              ]
            }
          },
          "endpoints": [
            {
              "id": "endpoint:com.example.web.OrderController#getOrder",
              "module_id": "module:.",
              "api_surface_category": "source_visible_spring_mvc_endpoint",
              "controller_class": "com.example.web.OrderController",
              "handler_method": "getOrder",
              "http_methods": ["GET"],
              "paths": ["/orders/{id}"],
              "mapping_source": {
                "kind": "direct_handler_method",
                "binding": "direct",
                "uncertainty": null,
                "evidence_ids": ["ev:endpoint:mapping"]
              },
              "evidence_ids": ["ev:endpoint:controller", "ev:endpoint:mapping"]
            }
          ],
          "api_surface": {
            "openapi": {
              "operations": {
                "items": [
                  {
                    "id": "openapi_operation:module:.:spec:src/main/resources/openapi.yml:operation:post:/orders",
                    "module_id": "module:.",
                    "api_surface_category": "openapi_declared_operation",
                    "spec_path": "src/main/resources/openapi.yml",
                    "http_method": "POST",
                    "path": "/orders",
                    "operation_id": "createOrder",
                    "implementation_status": "not_analyzed",
                    "evidence_ids": ["ev:api:operation"]
                  }
                ]
              }
            }
          }
        }
        """;
  }

  private String impactEvidenceRecords() {
    return evidenceRecord(
            "ev:pom.xml:1-1:build_file:pom.xml",
            "build_file",
            "pom.xml",
            null,
            null,
            "pom.xml",
            1,
            1,
            "<project>",
            "high")
        + evidenceRecord(
            "ev:endpoint:controller",
            "code_symbol",
            "src/main/java/com/example/web/OrderController.java",
            "com.example.web.OrderController",
            null,
            "com.example.web.OrderController",
            10,
            10,
            "class OrderController",
            "high")
        + evidenceRecord(
            "ev:endpoint:mapping",
            "annotation",
            "src/main/java/com/example/web/OrderController.java",
            "com.example.web.OrderController",
            "getOrder",
            "@GetMapping",
            12,
            12,
            "@GetMapping(\"/orders/{id}\")",
            "high")
        + evidenceRecord(
            "ev:api:operation",
            "api_spec",
            "src/main/resources/openapi.yml",
            null,
            null,
            "operation:post:/orders",
            6,
            12,
            "post /orders",
            "high");
  }

  private String impactGraph() {
    return """
        {
          "graph_schema_version": "1.0",
          "project_map_schema_version": "1.0",
          "nodes": [
            {
              "id": "node:endpoint:endpoint%3Acom.example.web.OrderController%23getOrder",
              "kind": "endpoint",
              "label": "GET /orders/{id}",
              "claim_category": "extracted",
              "module_id": "module:.",
              "source_ref": {
                "artifact": "project-map.json",
                "section": "endpoints",
                "id": "endpoint:com.example.web.OrderController#getOrder"
              },
              "evidence_ids": ["ev:endpoint:mapping"]
            },
            {
              "id": "node:api-operation:openapi_operation%3Amodule%3A.%3Aspec%3Asrc%2Fmain%2Fresources%2Fopenapi.yml%3Aoperation%3Apost%3A%2Forders",
              "kind": "api_operation",
              "label": "POST /orders",
              "claim_category": "spec_backed",
              "module_id": "module:.",
              "source_ref": {
                "artifact": "project-map.json",
                "section": "api_surface.openapi.operations.items",
                "id": "openapi_operation:module:.:spec:src/main/resources/openapi.yml:operation:post:/orders"
              },
              "evidence_ids": ["ev:api:operation"]
            }
          ],
          "edges": [],
          "relation_statuses": [],
          "warnings": []
        }
        """;
  }

  private void writeImpactProjectionArtifacts(Path artifactRoot) throws IOException {
    writeArtifacts(artifactRoot, impactProjectionProjectMap(), impactEvidenceRecords());
    Files.writeString(artifactRoot.resolve("project-graph.json"), impactProjectionGraph());
  }

  private String impactProjectionProjectMap() {
    return """
        {
          "schema_version": "1.0",
          "endpoints": [
            {
              "id": "endpoint:com.example.web.OrderController#getOrder",
              "module_id": "module:.",
              "api_surface_category": "source_visible_spring_mvc_endpoint",
              "controller_class": "com.example.web.OrderController",
              "handler_method": "getOrder",
              "http_methods": ["GET"],
              "paths": ["/orders/{id}"],
              "mapping_source": {
                "kind": "direct_handler_method",
                "binding": "direct",
                "uncertainty": null,
                "evidence_ids": ["ev:endpoint:mapping"]
              },
              "evidence_ids": ["ev:endpoint:controller", "ev:endpoint:mapping"]
            }
          ],
          "quality": {
            "change_risk_signals": {
              "analysis_status": "analyzed",
              "items": [
                {
                  "id": "quality:change-risk:endpoint:com.example.web.OrderController#getOrder",
                  "module_id": "module:.",
                  "signal": "spring_service_change_surface",
                  "status": "planning_hint",
                  "subject_kind": "endpoint",
                  "subject_id": "endpoint:com.example.web.OrderController#getOrder",
                  "subject_name": "GET /orders/{id}",
                  "subject_class_name": "com.example.web.OrderController",
                  "subject_member_name": "getOrder",
                  "confidence": "low",
                  "uncertainty": "source_visible_change_surface_only",
                  "risk_basis": "source_visible_service_stereotype",
                  "evidence_ids": ["ev:endpoint:mapping"]
                }
              ]
            }
          }
        }
        """;
  }

  private String impactProjectionGraph() {
    return """
        {
          "graph_schema_version": "1.0",
          "project_map_schema_version": "1.0",
          "nodes": [
            {
              "id": "node:endpoint:endpoint%3Acom.example.web.OrderController%23getOrder",
              "kind": "endpoint",
              "label": "GET /orders/{id}",
              "claim_category": "extracted",
              "module_id": "module:.",
              "source_ref": {
                "artifact": "project-map.json",
                "section": "endpoints",
                "id": "endpoint:com.example.web.OrderController#getOrder"
              },
              "evidence_ids": ["ev:endpoint:mapping"]
            },
            {
              "id": "node:type:com.example.web.OrderController",
              "kind": "type",
              "label": "OrderController",
              "claim_category": "extracted",
              "module_id": "module:.",
              "source_ref": null,
              "evidence_ids": []
            },
            {
              "id": "node:test:com.example.web.OrderControllerTest",
              "kind": "test",
              "label": "OrderControllerTest",
              "claim_category": "inferred",
              "module_id": "module:.",
              "source_ref": null,
              "evidence_ids": []
            }
          ],
          "edges": [
            {
              "id": "edge:declares:node:type:com.example.web.OrderController:node:endpoint:endpoint%3Acom.example.web.OrderController%23getOrder",
              "type": "declares",
              "source_id": "node:type:com.example.web.OrderController",
              "target_id": "node:endpoint:endpoint%3Acom.example.web.OrderController%23getOrder",
              "claim_category": "structural",
              "relation_status": "derived",
              "support_type": "project_map_derivation",
              "confidence": "high",
              "uncertainty": null,
              "relation_attributes": {},
              "derivation": {
                "kind": "project_map_field",
                "artifact": "project-map.json",
                "section": "endpoints",
                "fields": ["controller_class", "id"]
              },
              "evidence_ids": []
            },
            {
              "id": "edge:tested_subject:node:test:com.example.web.OrderControllerTest:node:endpoint:endpoint%3Acom.example.web.OrderController%23getOrder",
              "type": "tested_subject",
              "source_id": "node:test:com.example.web.OrderControllerTest",
              "target_id": "node:endpoint:endpoint%3Acom.example.web.OrderController%23getOrder",
              "claim_category": "inferred",
              "relation_status": "inferred",
              "support_type": "inferred",
              "confidence": "medium",
              "uncertainty": null,
              "relation_attributes": {
                "relation_type": "test_import",
                "subject_kind": "endpoint"
              },
              "derivation": null,
              "evidence_ids": []
            }
          ],
          "relation_statuses": [
            {
              "id": "relation-status:tested_subject:node:endpoint:endpoint%3Acom.example.web.OrderController%23getOrder:no_supported_subject_signal",
              "relation_family": "tested_subject",
              "source_id": "node:endpoint:endpoint%3Acom.example.web.OrderController%23getOrder",
              "target_id": null,
              "relation_status": "not_detected",
              "support_type": "status_only",
              "confidence": "low",
              "uncertainty": "no_supported_subject_signal",
              "relation_attributes": {
                "relation_type": "not_detected"
              },
              "derivation": {
                "kind": "project_map_relation_status",
                "artifact": "project-map.json",
                "section": "tests.items[].tested_subjects",
                "fields": []
              },
              "evidence_ids": []
            }
          ],
          "warnings": []
        }
        """;
  }

  private void writeImpactProjectionCapArtifacts(Path artifactRoot, int edgeCount)
      throws IOException {
    writeArtifacts(artifactRoot, """
        {
          "schema_version": "1.0",
          "endpoints": [
            {
              "id": "endpoint:com.example.web.OrderController#getOrder",
              "module_id": "module:.",
              "evidence_ids": ["ev:endpoint:mapping"]
            }
          ]
        }
        """, evidenceRecord(
            "ev:endpoint:mapping",
            "annotation",
            "src/main/java/com/example/web/OrderController.java",
            "com.example.web.OrderController",
            "getOrder",
            "@GetMapping",
            12,
            12,
            "@GetMapping(\"/orders/{id}\")",
            "high"));
    Files.writeString(artifactRoot.resolve("project-graph.json"), impactProjectionCapGraph(edgeCount));
  }

  private String impactProjectionCapGraph(int edgeCount) {
    StringBuilder nodes = new StringBuilder();
    nodes.append("""
            {
              "id": "node:endpoint:endpoint%3Acom.example.web.OrderController%23getOrder",
              "kind": "endpoint",
              "label": "GET /orders/{id}",
              "claim_category": "extracted",
              "module_id": "module:.",
              "source_ref": {
                "artifact": "project-map.json",
                "section": "endpoints",
                "id": "endpoint:com.example.web.OrderController#getOrder"
              },
              "evidence_ids": ["ev:endpoint:mapping"]
            }""");
    StringBuilder edges = new StringBuilder();
    for (int index = 0; index < edgeCount; index++) {
      String nodeId = "node:projection:neighbor-" + String.format("%04d", index);
      nodes.append(",\n");
      nodes.append("""
            {
              "id": "%s",
              "kind": "type",
              "label": "Neighbor%s",
              "claim_category": "structural",
              "module_id": "module:.",
              "source_ref": null,
              "evidence_ids": []
            }""".formatted(nodeId, String.format("%04d", index)));
      if (index > 0) {
        edges.append(",\n");
      }
      edges.append("""
            {
              "id": "edge:declares:%s:node:endpoint:endpoint%%3Acom.example.web.OrderController%%23getOrder",
              "type": "declares",
              "source_id": "%s",
              "target_id": "node:endpoint:endpoint%%3Acom.example.web.OrderController%%23getOrder",
              "claim_category": "structural",
              "relation_status": "derived",
              "support_type": "project_map_derivation",
              "confidence": "high",
              "uncertainty": null,
              "relation_attributes": {},
              "derivation": {
                "kind": "project_map_field",
                "artifact": "project-map.json",
                "section": "endpoints",
                "fields": ["id"]
              },
              "evidence_ids": []
            }""".formatted(nodeId, nodeId));
    }
    return """
        {
          "graph_schema_version": "1.0",
          "project_map_schema_version": "1.0",
          "nodes": [
        %s
          ],
          "edges": [
        %s
          ],
          "relation_statuses": [],
          "warnings": []
        }
        """.formatted(nodes, edges);
  }

  private void writeRelationGraph(Path artifactRoot) throws IOException {
    Files.writeString(artifactRoot.resolve("project-graph.json"), """
        {
          "graph_schema_version": "1.0",
          "project_map_schema_version": "1.0",
          "nodes": [
            {
              "id": "node:module:root",
              "kind": "module",
              "label": "root",
              "claim_category": "structural",
              "module_id": "module:.",
              "source_ref": {
                "artifact": "project-map.json",
                "section": "project.modules.items",
                "id": "module:."
              },
              "evidence_ids": []
            },
            {
              "id": "node:type:root:com.example.web.OrderController",
              "kind": "type",
              "label": "OrderController",
              "claim_category": "extracted",
              "module_id": "module:.",
              "source_ref": {
                "artifact": "project-map.json",
                "section": "components.items",
                "id": "component:com.example.web.OrderController"
              },
              "evidence_ids": ["ev:endpoint:mapping"]
            },
            {
              "id": "node:endpoint:endpoint%3Acom.example.web.OrderController%23getOrder",
              "kind": "endpoint",
              "label": "GET /orders/{id}",
              "claim_category": "extracted",
              "module_id": "module:.",
              "source_ref": {
                "artifact": "project-map.json",
                "section": "endpoints",
                "id": "endpoint:com.example.web.OrderController#getOrder"
              },
              "evidence_ids": ["ev:endpoint:mapping"]
            },
            {
              "id": "node:test:test%3Acom.example.web.OrderControllerTest",
              "kind": "test",
              "label": "OrderControllerTest",
              "claim_category": "extracted",
              "module_id": "module:.",
              "source_ref": {
                "artifact": "project-map.json",
                "section": "tests.items",
                "id": "test:com.example.web.OrderControllerTest"
              },
              "evidence_ids": ["ev:order:symbol"]
            }
          ],
          "edges": [
            {
              "id": "edge:owns:node:module:root:node:type:root:com.example.web.OrderController",
              "type": "owns",
              "source_id": "node:module:root",
              "target_id": "node:type:root:com.example.web.OrderController",
              "claim_category": "structural",
              "relation_status": "derived",
              "support_type": "project_map_derivation",
              "confidence": "high",
              "uncertainty": null,
              "relation_attributes": {},
              "derivation": {
                "kind": "project_map_field",
                "artifact": "project-map.json",
                "section": "components.items",
                "fields": ["module_id", "class_name"]
              },
              "evidence_ids": []
            },
            {
              "id": "edge:declares:node:type:root:com.example.web.OrderController:node:endpoint:endpoint%3Acom.example.web.OrderController%23getOrder",
              "type": "declares",
              "source_id": "node:type:root:com.example.web.OrderController",
              "target_id": "node:endpoint:endpoint%3Acom.example.web.OrderController%23getOrder",
              "claim_category": "structural",
              "relation_status": "derived",
              "support_type": "project_map_derivation",
              "confidence": "high",
              "uncertainty": null,
              "relation_attributes": {},
              "derivation": {
                "kind": "project_map_field",
                "artifact": "project-map.json",
                "section": "endpoints",
                "fields": ["controller_class", "handler_method"]
              },
              "evidence_ids": []
            },
            {
              "id": "edge:tested_subject:node:test:test%3Acom.example.web.OrderControllerTest:node:type:root:com.example.web.OrderController",
              "type": "tested_subject",
              "source_id": "node:test:test%3Acom.example.web.OrderControllerTest",
              "target_id": "node:type:root:com.example.web.OrderController",
              "claim_category": "inferred",
              "relation_status": "inferred",
              "support_type": "inferred",
              "confidence": "medium",
              "uncertainty": null,
              "relation_attributes": {
                "relation_type": "naming_convention",
                "target_class_name": "com.example.web.OrderController"
              },
              "derivation": null,
              "evidence_ids": ["ev:endpoint:mapping", "ev:order:symbol"]
            }
          ],
          "relation_statuses": [
            {
              "id": "relation-status:tested_subject:node:test:test%3Acom.example.web.OrderControllerTest:no_supported_subject_signal",
              "relation_family": "tested_subject",
              "source_id": "node:test:test%3Acom.example.web.OrderControllerTest",
              "target_id": null,
              "relation_status": "not_detected",
              "support_type": "status_only",
              "confidence": "low",
              "uncertainty": "no_supported_subject_signal",
              "relation_attributes": {
                "relation_type": "not_detected"
              },
              "derivation": {
                "kind": "project_map_relation_status",
                "artifact": "project-map.json",
                "section": "tests.items[].tested_subjects"
              },
              "evidence_ids": []
            }
          ],
          "warnings": []
        }
        """);
  }

  private String evidenceRecord(String id) {
    return "{"
        + "\"id\":\"" + id + "\","
        + "\"source_type\":\"build_file\","
        + "\"path\":\"pom.xml\","
        + "\"class_name\":null,"
        + "\"method_name\":null,"
        + "\"symbol_name\":\"pom.xml\","
        + "\"line_start\":1,"
        + "\"line_end\":1,"
        + "\"excerpt\":\"<project>\","
        + "\"confidence\":\"high\""
        + "}\n";
  }

  private String evidenceRecord(
      String id,
      String sourceType,
      String path,
      String className,
      String methodName,
      String symbolName,
      int lineStart,
      int lineEnd,
      String excerpt,
      String confidence) {
    return "{"
        + "\"id\":\"" + id + "\","
        + "\"source_type\":\"" + sourceType + "\","
        + "\"path\":\"" + path + "\","
        + "\"class_name\":" + nullableJsonString(className) + ","
        + "\"method_name\":" + nullableJsonString(methodName) + ","
        + "\"symbol_name\":\"" + symbolName + "\","
        + "\"line_start\":" + lineStart + ","
        + "\"line_end\":" + lineEnd + ","
        + "\"excerpt\":\"" + excerpt.replace("\"", "\\\"") + "\","
        + "\"confidence\":\"" + confidence + "\""
        + "}\n";
  }

  private String lookupEvidenceRecords() {
    return evidenceRecord(
            "ev:pom.xml:1-1:build_file:pom.xml",
            "build_file",
            "pom.xml",
            null,
            null,
            "pom.xml",
            1,
            1,
            "<project>",
            "high")
        + evidenceRecord(
            "ev:endpoint:mapping",
            "annotation",
            "src/main/java/com/example/web/OrderController.java",
            "com.example.web.OrderController",
            "getOrder",
            "@GetMapping",
            12,
            12,
            "@GetMapping(\"/orders/{id}\")",
            "high")
        + evidenceRecord(
            "ev:endpoint:controller",
            "code_symbol",
            "src/main/java/com/example/web/OrderController.java",
            "com.example.web.OrderController",
            null,
            "com.example.web.OrderController",
            10,
            10,
            "class OrderController",
            "high")
        + evidenceRecord(
            "ev:api:operation",
            "api_spec",
            "src/main/resources/openapi.yml",
            null,
            null,
            "operation:post:/orders",
            6,
            12,
            "post /orders",
            "high")
        + evidenceRecord(
            "ev:entity:order",
            "annotation",
            "src/main/java/com/example/domain/Order.java",
            "com.example.domain.Order",
            null,
            "@Entity",
            8,
            8,
            "@Entity",
            "high")
        + evidenceRecord(
            "ev:order:symbol",
            "code_symbol",
            "src/main/java/com/example/domain/Order.java",
            "com.example.domain.Order",
            null,
            "com.example.domain.Order",
            9,
            9,
            "class Order",
            "high")
        + evidenceRecord(
            "ev:entity:embedded-id",
            "annotation",
            "src/main/java/com/example/domain/Order.java",
            "com.example.domain.Order",
            null,
            "@EmbeddedId",
            11,
            11,
            "@EmbeddedId",
            "high")
        + evidenceRecord(
            "ev:embeddable:order-id",
            "annotation",
            "src/main/java/com/example/domain/OrderId.java",
            "com.example.domain.OrderId",
            null,
            "@Embeddable",
            8,
            8,
            "@Embeddable",
            "high")
        + evidenceRecord(
            "ev:entity:relationship",
            "annotation",
            "src/main/java/com/example/domain/Order.java",
            "com.example.domain.Order",
            null,
            "@ManyToOne",
            14,
            14,
            "@ManyToOne",
            "high")
        + evidenceRecord(
            "ev:repository:order",
            "code_symbol",
            "src/main/java/com/example/OrderRepository.java",
            "com.example.OrderRepository",
            null,
            "com.example.OrderRepository",
            5,
            5,
            "interface OrderRepository",
            "high")
        + evidenceRecord(
            "ev:repository:raw",
            "code_symbol",
            "src/main/java/com/example/RawRepository.java",
            "com.example.RawRepository",
            null,
            "com.example.RawRepository",
            5,
            5,
            "interface RawRepository",
            "high")
        + evidenceRecord(
            "ev:test:method",
            "annotation",
            "src/test/java/com/example/web/OrderControllerTest.java",
            "com.example.web.OrderControllerTest",
            "returnsOrder",
            "@Test",
            12,
            12,
            "@Test",
            "high")
        + evidenceRecord(
            "ev:test:framework",
            "annotation",
            "src/test/java/com/example/web/OrderControllerTest.java",
            "com.example.web.OrderControllerTest",
            null,
            "org.junit.jupiter.api.Test",
            3,
            3,
            "import org.junit.jupiter.api.Test",
            "high")
        + evidenceRecord(
            "ev:test:slice",
            "annotation",
            "src/test/java/com/example/web/OrderControllerTest.java",
            "com.example.web.OrderControllerTest",
            null,
            "@WebMvcTest",
            8,
            8,
            "@WebMvcTest(OrderController.class)",
            "high")
        + evidenceRecord(
            "ev:test:mock",
            "annotation",
            "src/test/java/com/example/web/OrderControllerTest.java",
            "com.example.web.OrderControllerTest",
            null,
            "@MockBean",
            10,
            10,
            "@MockBean OrderService orderService",
            "high")
        + evidenceRecord(
            "ev:test:class",
            "code_symbol",
            "src/test/java/com/example/web/OrderControllerTest.java",
            "com.example.web.OrderControllerTest",
            null,
            "com.example.web.OrderControllerTest",
            9,
            9,
            "class OrderControllerTest",
            "high")
        + evidenceRecord(
            "ev:controller:class",
            "code_symbol",
            "src/main/java/com/example/web/OrderController.java",
            "com.example.web.OrderController",
            null,
            "com.example.web.OrderController",
            10,
            10,
            "class OrderController",
            "high");
  }

  private String nullableJsonString(String value) {
    return value == null ? "null" : "\"" + value + "\"";
  }

  private String richProjectMap() {
    return """
        {
          "schema_version": "1.0",
          "project": {
            "modules": {
              "items": [
                {
                  "module_id": "module:.",
                  "module_path": ".",
                  "pom_path": "pom.xml",
                  "source_roots": ["src/main/java"],
                  "test_roots": ["src/test/java"],
                  "support_status": "supported",
                  "declaration_kind": "scan_root",
                  "declared_path": ".",
                  "declaration_evidence_ids": [],
                  "pom_evidence_ids": ["ev:pom.xml:1-1:build_file:pom.xml"]
                }
              ]
            }
          },
          "endpoints": [
            {
              "id": "endpoint:com.example.web.OrderController#getOrder",
              "module_id": "module:.",
              "api_surface_category": "source_visible_spring_mvc_endpoint",
              "controller_class": "com.example.web.OrderController",
              "handler_method": "getOrder",
              "http_methods": ["GET"],
              "http_method_semantics": "declared",
              "paths": ["/orders/{id}"],
              "request_body_type": null,
              "response_type": "OrderDto",
              "mapping_source": {
                "kind": "direct_handler_method",
                "binding": "direct",
                "uncertainty": null,
                "evidence_ids": ["ev:endpoint:mapping"]
              },
              "evidence_ids": ["ev:endpoint:controller", "ev:endpoint:mapping"]
            }
          ],
          "api_surface": {
            "openapi": {
              "operations": {
                "items": [
                  {
                    "id": "openapi_operation:module:.:spec:src/main/resources/openapi.yml:operation:post:/orders",
                    "module_id": "module:.",
                    "api_surface_category": "openapi_declared_operation",
                    "spec_path": "src/main/resources/openapi.yml",
                    "http_method": "POST",
                    "path": "/orders",
                    "operation_id": "createOrder",
                    "tags": ["Orders"],
                    "implementation_status": "not_analyzed",
                    "evidence_ids": ["ev:api:operation"]
                  }
                ]
              }
            }
          },
          "entities": {
            "items": [
              {
                "id": "entity:com.example.domain.Order",
                "module_id": "module:.",
                "class_name": "com.example.domain.Order",
                "table_name": "orders",
                "id_class": null,
                "fields": [
                  {
                    "field_name": "id",
                    "java_type": "OrderId",
                    "embedded": {
                      "annotation": "@EmbeddedId",
                      "java_type": "OrderId",
                      "target_resolution": "source_visible_embeddable",
                      "target_embeddable_id": "embeddable:com.example.domain.OrderId",
                      "target_module_id": "module:.",
                      "target_class_name": "com.example.domain.OrderId",
                      "support_type": "inferred",
                      "confidence": "medium",
                      "uncertainty": null,
                      "evidence_ids": ["ev:entity:embedded-id", "ev:embeddable:order-id"]
                    }
                  }
                ],
                "identifier_fields": [
                  {
                    "field_name": "id",
                    "java_type": "OrderId",
                    "source_kind": "declared",
                    "identifier_kind": "embedded_id",
                    "evidence_ids": ["ev:entity:embedded-id"]
                  }
                ],
                "relationships": [
                  {
                    "field_name": "customer",
                    "annotation": "@ManyToOne",
                    "cardinality": "many_to_one",
                    "java_type": "Customer",
                    "target": {
                      "target_resolution": "declared_type_only",
                      "target_entity_id": null,
                      "target_module_id": null,
                      "support_type": null,
                      "confidence": null,
                      "uncertainty": "target_type_not_resolved"
                    },
                    "ownership_signal": "join_metadata_present",
                    "evidence_ids": ["ev:entity:relationship"]
                  }
                ],
                "evidence_ids": ["ev:entity:order"]
              }
            ],
            "embeddables": {
              "items": [
                {
                  "id": "embeddable:com.example.domain.OrderId",
                  "module_id": "module:.",
                  "class_name": "com.example.domain.OrderId",
                  "fields": [],
                  "evidence_ids": ["ev:embeddable:order-id"]
                }
              ]
            }
          },
          "spring_application_surface": {
            "repositories": {
              "items": [
                {
                  "id": "spring_data_repository_interface_signal:com.example.OrderRepository",
                  "module_id": "module:.",
                  "class_name": "com.example.OrderRepository",
                  "entity_relation_status": "inferred",
                  "entity_relation": {
                    "relation_type": "repository_entity_generic",
                    "target_entity_id": "entity:com.example.domain.Order",
                    "target_module_id": "module:.",
                    "target_class_name": "com.example.domain.Order",
                    "generic_type": "com.example.domain.Order",
                    "support_type": "inferred",
                    "confidence": "medium",
                    "uncertainty": null,
                    "evidence_ids": ["ev:repository:order", "ev:entity:order"]
                  },
                  "evidence_ids": ["ev:repository:order"]
                },
                {
                  "id": "spring_data_repository_interface_signal:com.example.RawRepository",
                  "module_id": "module:.",
                  "class_name": "com.example.RawRepository",
                  "entity_relation_status": "unsupported",
                  "entity_relation": null,
                  "evidence_ids": ["ev:repository:raw"]
                }
              ]
            }
          },
          "tests": {
            "items": [
              {
                "id": "test:com.example.web.OrderControllerTest",
                "module_id": "module:.",
                "class_name": "com.example.web.OrderControllerTest",
                "methods": [
                  {
                    "method_name": "returnsOrder",
                    "test_annotation": "@Test",
                    "method_kind": "test",
                    "display_name": null,
                    "evidence_ids": ["ev:test:method"]
                  }
                ],
                "framework_signals": [
                  {
                    "name": "JUnit Jupiter",
                    "signal_kind": "framework",
                    "evidence_ids": ["ev:test:framework"]
                  }
                ],
                "spring_test_slices": [
                  {
                    "annotation": "@WebMvcTest",
                    "slice_kind": "web_mvc_test",
                    "signal_kind": "spring_test_slice",
                    "evidence_ids": ["ev:test:slice"]
                  }
                ],
                "mock_signals": [
                  {
                    "annotation": "@MockBean",
                    "mock_signal": "spring_boot_mockbean_annotation",
                    "signal_kind": "mock_annotation",
                    "target_kind": "field",
                    "target_name": "orderService",
                    "evidence_ids": ["ev:test:mock"]
                  }
                ],
                "tested_subjects": [
                  {
                    "relation_status": "inferred",
                    "relation_type": "naming_convention",
                    "class_name": "com.example.web.OrderController",
                    "target_module_id": "module:.",
                    "candidate_reference": null,
                    "support_type": "inferred",
                    "confidence": "medium",
                    "uncertainty": null,
                    "evidence_ids": ["ev:test:class", "ev:controller:class"]
                  },
                  {
                    "relation_status": "not_detected",
                    "relation_type": "naming_convention",
                    "class_name": null,
                    "target_module_id": null,
                    "candidate_reference": "MissingController",
                    "support_type": null,
                    "confidence": "low",
                    "uncertainty": "no_matching_production_class",
                    "evidence_ids": ["ev:test:class"]
                  }
                ],
                "evidence_ids": ["ev:test:class"]
              }
            ]
          }
        }
        """;
  }

  private record CliResult(int exitCode, String stdout, String stderr) {
  }
}
