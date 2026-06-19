package io.github.dondindondev.agentprojectmemory.workspace;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class WorkspaceMapGeneratorTest {
  private static final ObjectMapper JSON = new ObjectMapper();

  @TempDir
  private Path tempDir;

  private final WorkspaceConfigurationLoader loader = new WorkspaceConfigurationLoader();
  private final WorkspaceMapGenerator generator = new WorkspaceMapGenerator();

  @Test
  void writesMultiRepoWorkspaceMapFromExistingArtifactsAndCompositeEvidenceReferences()
      throws Exception {
    Path workspaceRoot = workspace("multi-repo");
    Path orders = workspaceRoot.resolve("repos/orders");
    Path billing = workspaceRoot.resolve("repos/billing");
    writeMemberArtifacts(
        orders,
        "1.0",
        List.of("ev:orders:pom", "ev:orders:controller"),
        true,
        null);
    writeMemberArtifacts(
        billing,
        "2.0",
        List.of("ev:billing:pom"),
        true,
        "1.2");
    Path config = writeWorkspaceConfig(workspaceRoot, """
        version: 1
        members:
          - repo_id: orders
            root: repos/orders
          - repo_id: billing
            root: repos/billing
        """);
    String ordersProjectMap = Files.readString(orders.resolve(".project-memory/project-map.json"));
    String billingEvidenceIndex = Files.readString(billing.resolve(".project-memory/evidence-index.jsonl"));

    WorkspaceMapGenerator.Result result = generator.generate(loader.load(config.toString()));

    JsonNode workspaceMap = readWorkspaceMap(workspaceRoot);
    JsonNode firstMember = workspaceMap.path("members").get(0);
    JsonNode secondMember = workspaceMap.path("members").get(1);
    Map<String, Path> memberRoots = Map.of("orders", orders, "billing", billing);

    assertAll(
        () -> assertEquals(2, result.memberCount()),
        () -> assertEquals(0, result.diagnosticCount()),
        () -> assertEquals("1.0", workspaceMap.path("workspace_schema_version").asText()),
        () -> assertEquals("config_directory", workspaceMap.path("workspace").path("root_kind").asText()),
        () -> assertEquals(
            "agent-project-memory-workspace.yml",
            workspaceMap.path("workspace").path("config_source").path("path").asText()),
        () -> assertEquals(
            "not_serialized",
            workspaceMap.path("workspace").path("config_source").path("content_status").asText()),
        () -> assertEquals(2, workspaceMap.path("workspace").path("member_count").asInt()),
        () -> assertEquals("orders", firstMember.path("repo_id").asText()),
        () -> assertEquals("repos/orders", firstMember.path("root_path").asText()),
        () -> assertEquals("repos/orders/.project-memory", firstMember.path("artifact_root").asText()),
        () -> assertEquals("present", firstMember.path("artifact_status").asText()),
        () -> assertEquals("1.0", firstMember.path("project_map_schema_version").asText()),
        () -> assertEquals("1.0", firstMember.path("graph_schema_version").asText()),
        () -> assertTrue(firstMember.path("source_registry_schema_version").isNull()),
        () -> assertEquals(2, firstMember.path("evidence_record_count").asInt()),
        () -> assertEquals("orders", firstMember.path("sample_evidence_references").get(0)
            .path("repo_id").asText()),
        () -> assertEquals("ev:orders:pom", firstMember.path("sample_evidence_references").get(0)
            .path("evidence_id").asText()),
        () -> assertEquals("evidence-index.jsonl", firstMember.path("sample_evidence_references").get(0)
            .path("artifact").asText()),
        () -> assertEquals("billing", secondMember.path("repo_id").asText()),
        () -> assertEquals("2.0", secondMember.path("project_map_schema_version").asText()),
        () -> assertEquals("1.0", secondMember.path("graph_schema_version").asText()),
        () -> assertEquals("1.2", secondMember.path("source_registry_schema_version").asText()),
        () -> assertEquals(1, secondMember.path("evidence_record_count").asInt()),
        () -> assertEquals("not_analyzed", workspaceMap.path("relations").path("analysis_status").asText()),
        () -> assertEquals(0, workspaceMap.path("relations").path("items").size()),
        () -> assertEquals(0, workspaceMap.path("diagnostics").size()),
        () -> assertEquals(
            ordersProjectMap,
            Files.readString(orders.resolve(".project-memory/project-map.json"))),
        () -> assertEquals(
            billingEvidenceIndex,
            Files.readString(billing.resolve(".project-memory/evidence-index.jsonl"))),
        () -> assertFalse(Files.readString(orders.resolve(".project-memory/evidence-index.jsonl"))
            .contains("\"repo_id\"")),
        () -> assertFalse(workspaceMap.toString().contains(workspaceRoot.toString())));
    assertCompositeReferencesResolve(workspaceMap, memberRoots);
  }

  @Test
  void writesMonorepoServiceMemberRootsWithExplicitRepoIds() throws Exception {
    Path workspaceRoot = workspace("monorepo");
    Path ordersService = workspaceRoot.resolve("platform/services/orders");
    Path sharedLibrary = workspaceRoot.resolve("platform/libs/shared");
    writeMemberArtifacts(ordersService, "1.0", List.of("ev:orders:pom"), false, null);
    writeMemberArtifacts(sharedLibrary, "1.0", List.of("ev:shared:pom"), false, null);
    Path config = writeWorkspaceConfig(workspaceRoot, """
        version: 1
        members:
          - repo_id: orders-service
            root: platform/services/orders
          - repo_id: shared-lib
            root: platform/libs/shared
        """);

    generator.generate(loader.load(config.toString()));

    JsonNode workspaceMap = readWorkspaceMap(workspaceRoot);

    assertAll(
        () -> assertEquals("orders-service", workspaceMap.path("members").get(0)
            .path("repo_id").asText()),
        () -> assertEquals("platform/services/orders", workspaceMap.path("members").get(0)
            .path("root_path").asText()),
        () -> assertEquals("shared-lib", workspaceMap.path("members").get(1)
            .path("repo_id").asText()),
        () -> assertEquals("platform/libs/shared", workspaceMap.path("members").get(1)
            .path("root_path").asText()),
        () -> assertEquals("present", workspaceMap.path("members").get(0)
            .path("artifact_status").asText()),
        () -> assertEquals("present", workspaceMap.path("members").get(1)
            .path("artifact_status").asText()));
  }

  @Test
  void keepsCrossRepoRelationsParkedWhenMemberArtifactsExposeRelationLikeSignals()
      throws Exception {
    Path workspaceRoot = workspace("parked-relations");
    Path orders = workspaceRoot.resolve("repos/orders");
    Path billing = workspaceRoot.resolve("repos/billing");
    writeMemberArtifacts(
        orders,
        "1.0",
        List.of(
            "ev:src/main/java/com/example/orders/OrderClient.java:12-12:com.example.orders.OrderClient:OrderClient",
            "ev:src/main/java/com/example/shared/CheckoutEvent.java:8-8:com.example.shared.CheckoutEvent:CheckoutEvent"),
        true,
        null);
    writeMemberArtifacts(
        billing,
        "1.0",
        List.of(
            "ev:src/main/java/com/example/billing/OrderController.java:20-20:com.example.billing.OrderController#get:@GetMapping",
            "ev:src/main/java/com/example/shared/CheckoutEvent.java:8-8:com.example.shared.CheckoutEvent:CheckoutEvent"),
        true,
        null);
    Path config = writeWorkspaceConfig(workspaceRoot, """
        version: 1
        members:
          - repo_id: orders
            root: repos/orders
          - repo_id: billing
            root: repos/billing
        """);

    generator.generate(loader.load(config.toString()));

    JsonNode workspaceMap = readWorkspaceMap(workspaceRoot);
    Map<String, Path> memberRoots = Map.of("orders", orders, "billing", billing);
    String output = workspaceMap.toString();

    assertAll(
        () -> assertEquals("not_analyzed", workspaceMap.path("relations").path("analysis_status").asText()),
        () -> assertEquals(0, workspaceMap.path("relations").path("items").size()),
        () -> assertEquals(0, workspaceMap.path("diagnostics").size()),
        () -> assertFalse(output.contains("cross_repo")),
        () -> assertFalse(output.contains("depends_on")),
        () -> assertFalse(output.contains("calls")),
        () -> assertFalse(output.contains("data_flow")));
    assertCompositeReferencesResolve(workspaceMap, memberRoots);
  }

  @Test
  void recordsMissingAndInvalidMemberArtifactsAsBoundedDiagnostics() throws Exception {
    Path workspaceRoot = workspace("diagnostics");
    Path missing = workspaceRoot.resolve("services/missing-artifacts");
    Path invalid = workspaceRoot.resolve("services/invalid-artifacts");
    Files.createDirectories(missing);
    Files.createDirectories(invalid.resolve(".project-memory"));
    Files.writeString(invalid.resolve(".project-memory/project-map.json"), "{not-json");
    Files.writeString(invalid.resolve(".project-memory/evidence-index.jsonl"), evidenceRecord("ev:invalid"));
    Path config = writeWorkspaceConfig(workspaceRoot, """
        version: 1
        members:
          - repo_id: missing
            root: services/missing-artifacts
          - repo_id: invalid
            root: services/invalid-artifacts
        """);

    WorkspaceMapGenerator.Result result = generator.generate(loader.load(config.toString()));

    JsonNode workspaceMap = readWorkspaceMap(workspaceRoot);
    String output = workspaceMap.toString();

    assertAll(
        () -> assertEquals(2, result.diagnosticCount()),
        () -> assertEquals("missing", workspaceMap.path("members").get(0)
            .path("artifact_status").asText()),
        () -> assertEquals("invalid", workspaceMap.path("members").get(1)
            .path("artifact_status").asText()),
        () -> assertEquals(2, workspaceMap.path("diagnostics").size()),
        () -> assertEquals("workspace-diagnostic:missing:member-artifacts-missing",
            workspaceMap.path("diagnostics").get(0).path("id").asText()),
        () -> assertEquals("workspace-diagnostic:invalid:project-map-invalid",
            workspaceMap.path("diagnostics").get(1).path("id").asText()),
        () -> assertFalse(output.contains(workspaceRoot.toString())),
        () -> assertFalse(output.contains("{not-json")));
  }

  @Test
  void rejectsSymlinkedWorkspaceOutputDirectoryWithoutWritingOutsideRoot() throws Exception {
    Path workspaceRoot = workspace("symlinked-output");
    Path service = workspaceRoot.resolve("service");
    writeMemberArtifacts(service, "1.0", List.of("ev:service:pom"), false, null);
    Path outsideOutput = tempDir.resolve("outside-output");
    Files.createDirectories(outsideOutput);
    assumeTrue(createSymbolicLink(workspaceRoot.resolve(".project-memory"), outsideOutput));
    Path config = writeWorkspaceConfig(workspaceRoot, """
        version: 1
        members:
          - repo_id: service
            root: service
        """);

    IOException exception = assertThrows(
        IOException.class,
        () -> generator.generate(loader.load(config.toString())));

    assertAll(
        () -> assertTrue(exception.getMessage().contains("Output directory must not be a symbolic link")),
        () -> assertFalse(Files.exists(outsideOutput.resolve("workspace-map.json"))));
  }

  @Test
  void rejectsHardlinkedWorkspaceMapTargetWithoutWritingOutsideAlias() throws Exception {
    Path workspaceRoot = workspace("hardlinked-output");
    Path service = workspaceRoot.resolve("service");
    writeMemberArtifacts(service, "1.0", List.of("ev:service:pom"), false, null);
    Files.createDirectories(workspaceRoot.resolve(".project-memory"));
    Path outsideWorkspaceMap = tempDir.resolve("outside-workspace-map.json");
    Files.writeString(outsideWorkspaceMap, "outside content");
    assumeTrue(createHardLink(
        workspaceRoot.resolve(".project-memory/workspace-map.json"),
        outsideWorkspaceMap));
    Path config = writeWorkspaceConfig(workspaceRoot, """
        version: 1
        members:
          - repo_id: service
            root: service
        """);

    IOException exception = assertThrows(
        IOException.class,
        () -> generator.generate(loader.load(config.toString())));

    assertAll(
        () -> assertTrue(exception.getMessage().contains("Output file must not have multiple hard links")),
        () -> assertEquals("outside content", Files.readString(outsideWorkspaceMap)),
        () -> assertEquals(
            "outside content",
            Files.readString(workspaceRoot.resolve(".project-memory/workspace-map.json"))));
  }

  private Path workspace(String name) throws IOException {
    Path workspaceRoot = tempDir.resolve(name);
    Files.createDirectories(workspaceRoot);
    return workspaceRoot;
  }

  private Path writeWorkspaceConfig(Path workspaceRoot, String content) throws IOException {
    Path config = workspaceRoot.resolve("agent-project-memory-workspace.yml");
    Files.writeString(config, content);
    return config;
  }

  private JsonNode readWorkspaceMap(Path workspaceRoot) throws IOException {
    return JSON.readTree(Files.readString(workspaceRoot.resolve(".project-memory/workspace-map.json")));
  }

  private void writeMemberArtifacts(
      Path memberRoot,
      String projectMapSchemaVersion,
      List<String> evidenceIds,
      boolean graph,
      String sourceRegistrySchemaVersion) throws IOException {
    Path artifactRoot = memberRoot.resolve(".project-memory");
    Files.createDirectories(artifactRoot);
    Files.writeString(
        artifactRoot.resolve("project-map.json"),
        "{\"schema_version\":\"" + projectMapSchemaVersion + "\",\"project\":{\"modules\":{\"items\":[]}}}\n");
    StringBuilder evidence = new StringBuilder();
    for (String evidenceId : evidenceIds) {
      evidence.append(evidenceRecord(evidenceId));
    }
    Files.writeString(artifactRoot.resolve("evidence-index.jsonl"), evidence.toString());
    if (graph) {
      Files.writeString(
          artifactRoot.resolve("project-graph.json"),
          graph(projectMapSchemaVersion, evidenceIds.get(0)));
    }
    if (sourceRegistrySchemaVersion != null) {
      Files.writeString(
          artifactRoot.resolve("source-registry.json"),
          sourceRegistry(sourceRegistrySchemaVersion));
    }
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

  private String graph(String projectMapSchemaVersion, String evidenceId) {
    return """
        {
          "graph_schema_version":"1.0",
          "project_map_schema_version":"%s",
          "nodes":[
            {"id":"node:module:root","evidence_ids":["%s"]},
            {"id":"node:relation-like:shared-order-api","evidence_ids":["%s"]}
          ],
          "edges":[
            {
              "id":"edge:relation-like:shared-order-api",
              "source":"node:module:root",
              "target":"node:relation-like:shared-order-api",
              "relation_type":"declares",
              "evidence_ids":["%s"]
            }
          ],
          "relation_statuses":[],
          "warnings":[]
        }
        """.formatted(projectMapSchemaVersion, evidenceId, evidenceId, evidenceId);
  }

  private String sourceRegistry(String sourceRegistrySchemaVersion) {
    return """
        {
          "source_registry_schema_version":"%s",
          "adapter_runs":[],
          "source_documents":[],
          "provenance":[],
          "diagnostics":{"analysis_status":"analyzed","items":[]}
        }
        """.formatted(sourceRegistrySchemaVersion);
  }

  private void assertCompositeReferencesResolve(JsonNode workspaceMap, Map<String, Path> memberRoots)
      throws Exception {
    Map<String, List<String>> evidenceByRepo = new HashMap<>();
    for (Map.Entry<String, Path> entry : memberRoots.entrySet()) {
      evidenceByRepo.put(entry.getKey(), evidenceIds(entry.getValue()));
    }

    for (JsonNode member : workspaceMap.path("members")) {
      for (JsonNode reference : member.path("sample_evidence_references")) {
        String repoId = reference.path("repo_id").asText();
        String evidenceId = reference.path("evidence_id").asText();
        assertEquals("evidence-index.jsonl", reference.path("artifact").asText());
        assertTrue(
            evidenceByRepo.getOrDefault(repoId, List.of()).contains(evidenceId),
            "Composite workspace reference must resolve to the member evidence index");
      }
    }
  }

  private boolean createSymbolicLink(Path link, Path target) throws IOException {
    try {
      Files.createSymbolicLink(link, target);
      return true;
    } catch (UnsupportedOperationException | SecurityException exception) {
      return false;
    }
  }

  private boolean createHardLink(Path link, Path existing) throws IOException {
    try {
      Files.createLink(link, existing);
      return true;
    } catch (UnsupportedOperationException | SecurityException exception) {
      return false;
    }
  }

  private List<String> evidenceIds(Path memberRoot) throws IOException {
    List<String> ids = new java.util.ArrayList<>();
    for (String line : Files.readString(memberRoot.resolve(".project-memory/evidence-index.jsonl"))
        .lines()
        .toList()) {
      ids.add(JSON.readTree(line).path("id").asText());
    }
    return ids;
  }
}
