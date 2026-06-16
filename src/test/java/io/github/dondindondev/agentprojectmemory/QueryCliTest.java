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
        () -> assertTrue(queryHelp.stdout().contains("relations <id>")),
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
  void listEndpointsAndApiOperationsStaySeparate() throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Path artifactRoot = repositoryRoot.resolve(".project-memory");
    writeArtifacts(artifactRoot, richProjectMap());

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
    writeArtifacts(artifactRoot, richProjectMap());

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
    writeArtifacts(artifactRoot, richProjectMap());

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
  void queryRelationsValidatesGraphWhenPresent() throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Path artifactRoot = repositoryRoot.resolve(".project-memory");
    writeBaseArtifacts(artifactRoot);
    writeValidGraph(artifactRoot);

    CliResult result = runCli("query", repositoryRoot.toString(), "relations", "node:module:root");

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertTrue(result.stdout().contains("Loaded project-graph.json graph_schema_version 1.0.")),
        () -> assertTrue(result.stdout().contains("query relations is not implemented")),
        () -> assertFalse(result.stdout().contains("node:module:root")),
        () -> assertTrue(result.stderr().isEmpty()));
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
    Files.createDirectories(artifactRoot);
    Files.writeString(artifactRoot.resolve("project-map.json"), projectMap);
    Files.writeString(
        artifactRoot.resolve("evidence-index.jsonl"),
        evidenceRecord("ev:pom.xml:1-1:build_file:pom.xml"));
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
