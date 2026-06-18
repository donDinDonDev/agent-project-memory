package io.github.dondindondev.agentprojectmemory;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.dondindondev.agentprojectmemory.analyzer.springmvc.SpringMvcEndpointOutputGenerator;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class AgentProjectMemoryCliTest {
  private static final ObjectMapper JSON = new ObjectMapper();

  @TempDir
  private Path tempDir;

  @Test
  void missingArgsReturnsNonZeroAndUsage() {
    CliResult result = runCli();

    assertAll(
        () -> assertEquals(2, result.exitCode()),
        () -> assertTrue(result.stderr().contains("Usage error: Missing command.")),
        () -> assertTrue(result.stderr().contains(AgentProjectMemoryCli.USAGE)));
  }

  @Test
  void unknownCommandReturnsNonZeroAndUsage() {
    CliResult result = runCli("status");

    assertAll(
        () -> assertEquals(2, result.exitCode()),
        () -> assertTrue(result.stderr().contains("Unknown command.")),
        () -> assertFalse(result.stderr().contains("status")),
        () -> assertTrue(result.stderr().contains(AgentProjectMemoryCli.USAGE)));
  }

  @Test
  void helpCommandsPrintHelpWithoutScanning() {
    for (String[] args : List.of(new String[] {"--help"}, new String[] {"help"})) {
      CliResult result = runCli(args);

      assertAll(
          () -> assertEquals(0, result.exitCode()),
          () -> assertTrue(result.stdout().contains("agent-project-memory - local evidence-backed")),
          () -> assertTrue(result.stdout().contains("Usage:")),
          () -> assertTrue(result.stdout().contains(
              "agent-project-memory scan <path> [--config <path>] [--agent-profile <profile>]")),
          () -> assertTrue(result.stderr().isEmpty()),
          () -> assertFalse(Files.exists(tempDir.resolve(".project-memory"))));
    }
  }

  @Test
  void scanHelpPrintsScanHelpWithoutScanning() {
    CliResult result = runCli("scan", "--help");

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertTrue(result.stdout().contains(AgentProjectMemoryCli.USAGE)),
        () -> assertTrue(result.stdout().contains("--config <path>")),
        () -> assertTrue(result.stdout().contains("--agent-profile <profile>")),
        () -> assertTrue(result.stdout().contains("--incremental")),
        () -> assertTrue(result.stderr().isEmpty()),
        () -> assertFalse(Files.exists(tempDir.resolve(".project-memory"))));
  }

  @Test
  void versionCommandsPrintVersionWithoutScanning() {
    for (String[] args : List.of(new String[] {"--version"}, new String[] {"version"})) {
      CliResult result = runCli(args);

      assertAll(
          () -> assertEquals(0, result.exitCode()),
          () -> assertTrue(result.stdout().startsWith("agent-project-memory ")),
          () -> assertFalse(result.stdout().contains("${")),
          () -> assertTrue(result.stderr().isEmpty()),
          () -> assertFalse(Files.exists(tempDir.resolve(".project-memory"))));
    }
  }

  @Test
  void scanWithoutPathReturnsScanInputExitCode() {
    CliResult result = runCli("scan");

    assertAll(
        () -> assertEquals(3, result.exitCode()),
        () -> assertTrue(result.stderr().contains("Scan input error: Missing scan path.")));
  }

  @Test
  void scanMissingPathReturnsScanInputExitCode() {
    Path missingPath = tempDir.resolve("missing-project");

    CliResult result = runCli("scan", missingPath.toString());

    assertAll(
        () -> assertEquals(3, result.exitCode()),
        () -> assertTrue(result.stderr().contains("Scan path does not exist")),
        () -> assertFalse(result.stderr().contains(missingPath.toString())));
  }

  @Test
  void scanFilePathReturnsScanInputExitCode() throws Exception {
    Path filePath = tempDir.resolve("project.txt");
    Files.writeString(filePath, "not a directory");

    CliResult result = runCli("scan", filePath.toString());

    assertAll(
        () -> assertEquals(3, result.exitCode()),
        () -> assertTrue(result.stderr().contains("Scan path is not a directory")),
        () -> assertFalse(result.stderr().contains(filePath.toString())));
  }

  @Test
  void scanUnknownFlagReturnsUsageExitCode() {
    CliResult result = runCli("scan", tempDir.toString(), "--unknown");

    assertAll(
        () -> assertEquals(2, result.exitCode()),
        () -> assertTrue(result.stderr().contains("Unexpected extra arguments.")),
        () -> assertTrue(result.stderr().contains(AgentProjectMemoryCli.USAGE)));
  }

  @Test
  void scanDuplicateConfigFlagReturnsUsageExitCode() {
    CliResult result = runCli(
        "scan",
        tempDir.toString(),
        "--config",
        "agent-project-memory.yml",
        "--config",
        "other.yml");

    assertAll(
        () -> assertEquals(2, result.exitCode()),
        () -> assertTrue(result.stderr().contains("Duplicate --config flag.")),
        () -> assertTrue(result.stderr().contains(AgentProjectMemoryCli.USAGE)));
  }

  @Test
  void scanFlagBeforePathReturnsUsageExitCodeWithoutScanning() {
    for (String[] args : List.of(new String[] {"scan", "--unknown"}, new String[] {"scan", "--version"})) {
      CliResult result = runCli(args);

      assertAll(
          () -> assertEquals(2, result.exitCode()),
          () -> assertTrue(result.stderr().contains("Usage error: Unknown flag.")),
          () -> assertTrue(result.stderr().contains(AgentProjectMemoryCli.USAGE)),
          () -> assertFalse(Files.exists(tempDir.resolve(".project-memory"))));
    }
  }

  @Test
  void scanConfigFlagBeforePathReturnsUsageExitCodeWithoutScanning() {
    CliResult result = runCli("scan", "--config", "agent-project-memory.yml");

    assertAll(
        () -> assertEquals(2, result.exitCode()),
        () -> assertTrue(result.stderr().contains("Usage error: Missing scan path.")),
        () -> assertTrue(result.stderr().contains(AgentProjectMemoryCli.USAGE)),
        () -> assertFalse(Files.exists(tempDir.resolve(".project-memory"))));
  }

  @Test
  void scanExistingDirectoryCreatesProjectMemoryDirectory() {
    CliResult result = runCli("scan", tempDir.toString());

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertTrue(Files.isDirectory(tempDir.resolve(".project-memory"))),
        () -> assertTrue(result.stdout().contains("Prepared .project-memory.")),
        () -> assertTrue(result.stdout().contains("Diagnostics: none.")),
        () -> assertFalse(result.stdout().contains(tempDir.toString())));
  }

  @Test
  void repeatedScanPreservesExistingFilesInsideProjectMemory() throws Exception {
    Path outputDirectory = tempDir.resolve(".project-memory");
    Path existingFile = outputDirectory.resolve("keep.txt");

    assertEquals(0, runCli("scan", tempDir.toString()).exitCode());
    Files.writeString(existingFile, "existing content");

    CliResult secondResult = runCli("scan", tempDir.toString());

    assertAll(
        () -> assertEquals(0, secondResult.exitCode()),
        () -> assertTrue(Files.exists(existingFile)),
        () -> assertEquals("existing content", Files.readString(existingFile)));
  }

  @Test
  void scanDoesNotCreateContractOutputFiles() {
    CliResult result = runCli("scan", tempDir.toString());
    Path outputDirectory = tempDir.resolve(".project-memory");

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertFalse(Files.exists(outputDirectory.resolve("project-map.json"))),
        () -> assertFalse(Files.exists(outputDirectory.resolve("project-graph.json"))),
        () -> assertFalse(Files.exists(outputDirectory.resolve("evidence-index.jsonl"))),
        () -> assertFalse(Files.exists(outputDirectory.resolve("endpoints.md"))),
        () -> assertFalse(Files.exists(outputDirectory.resolve("agent-guide.md"))),
        () -> assertFalse(Files.exists(outputDirectory.resolve("source-registry.json"))),
        () -> assertFalse(Files.exists(outputDirectory.resolve("agent-profiles"))));
  }

  @Test
  void scanRootPomOnlyGeneratesMetadataOnlyContractOutputFiles() throws Exception {
    Files.writeString(tempDir.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);

    CliResult result = runCli("scan", tempDir.toString());
    Path outputDirectory = tempDir.resolve(".project-memory");
    String projectMap = Files.readString(outputDirectory.resolve("project-map.json"));
    String projectGraph = Files.readString(outputDirectory.resolve("project-graph.json"));
    String evidenceIndex = Files.readString(outputDirectory.resolve("evidence-index.jsonl"));

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertTrue(Files.isDirectory(outputDirectory)),
        () -> assertTrue(result.stdout().contains("Generated project-map.json")),
        () -> assertTrue(result.stdout().contains("Diagnostics: none.")),
        () -> assertFalse(result.stdout().contains(tempDir.toString())),
        () -> assertTrue(Files.exists(outputDirectory.resolve("project-map.json"))),
        () -> assertTrue(Files.exists(outputDirectory.resolve("project-graph.json"))),
        () -> assertTrue(Files.exists(outputDirectory.resolve("evidence-index.jsonl"))),
        () -> assertTrue(Files.exists(outputDirectory.resolve("endpoints.md"))),
        () -> assertTrue(Files.exists(outputDirectory.resolve("agent-guide.md"))),
        () -> assertFalse(Files.exists(outputDirectory.resolve("source-registry.json"))),
        () -> assertFalse(Files.exists(outputDirectory.resolve("agent-profiles"))),
        () -> assertTrue(projectMap.contains("\"schema_version\": \"1.0\"")),
        () -> assertTrue(projectMap.contains("\"scan\": {")),
        () -> assertTrue(projectMap.contains("\"source\": \"defaults_only\"")),
        () -> assertTrue(projectMap.contains("\"module_id\": \"module:.\"")),
        () -> assertTrue(projectMap.contains("\"build_config\": {")),
        () -> assertTrue(projectMap.contains("\"metadata\": {\n"
            + "                \"analysis_status\": \"analyzed\"")),
        () -> assertTrue(projectMap.contains("\"artifact_id\": {\n"
            + "                  \"value\": null,\n"
            + "                  \"value_kind\": \"not_declared\"")),
        () -> assertTrue(evidenceIndex.contains("\"symbol_name\":\"pom.xml\"")));
  }

  @Test
  void scanWithSingleAgentProfileWritesManifestAndSelectedDeterministicContent() throws Exception {
    Files.writeString(tempDir.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);

    CliResult result = runCli("scan", tempDir.toString(), "--agent-profile", "codex");
    Path profileDirectory = tempDir.resolve(".project-memory/agent-profiles");
    String manifest = Files.readString(profileDirectory.resolve("manifest.json"));
    String codexProfile = Files.readString(profileDirectory.resolve("codex.md"));

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertTrue(result.stdout().contains("Generated agent profile artifacts: 1.")),
        () -> assertTrue(Files.exists(tempDir.resolve(".project-memory/project-map.json"))),
        () -> assertTrue(Files.exists(tempDir.resolve(".project-memory/project-graph.json"))),
        () -> assertTrue(Files.exists(profileDirectory.resolve("manifest.json"))),
        () -> assertTrue(Files.exists(profileDirectory.resolve("codex.md"))),
        () -> assertFalse(Files.exists(profileDirectory.resolve("claude.md"))),
        () -> assertFalse(Files.exists(profileDirectory.resolve("cursor.md"))),
        () -> assertFalse(Files.exists(profileDirectory.resolve("generic.md"))),
        () -> assertTrue(manifest.contains("\"manifest_version\": \"1.0\"")),
        () -> assertTrue(manifest.contains("\"project_map_schema_version\": \"1.0\"")),
        () -> assertTrue(manifest.contains("\"name\": \"codex\"")),
        () -> assertTrue(manifest.contains("\"artifact_path\": \"agent-profiles/codex.md\"")),
        () -> assertTrue(manifest.contains(
            "\"evidence_policy\": \"references_existing_evidence_only\"")),
        () -> assertTrue(codexProfile.contains("# Codex Agent Profile")),
        () -> assertTrue(codexProfile.contains("## Project Snapshot")),
        () -> assertTrue(codexProfile.contains("## Evidence-Visible Fact Pointers")),
        () -> assertTrue(codexProfile.contains("does not add evidence records")));
  }

  @Test
  void scanAllAndDuplicateAgentProfileSelectorsAreIdempotentAndCanonical() throws Exception {
    Files.writeString(tempDir.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);

    CliResult result = runCli(
        "scan",
        tempDir.toString(),
        "--agent-profile",
        "generic",
        "--agent-profile",
        "codex",
        "--agent-profile",
        "codex",
        "--agent-profile",
        "all");
    Path profileDirectory = tempDir.resolve(".project-memory/agent-profiles");
    String manifest = Files.readString(profileDirectory.resolve("manifest.json"));

    int codex = manifest.indexOf("\"name\": \"codex\"");
    int claude = manifest.indexOf("\"name\": \"claude\"");
    int cursor = manifest.indexOf("\"name\": \"cursor\"");
    int generic = manifest.indexOf("\"name\": \"generic\"");

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertTrue(result.stdout().contains("Generated agent profile artifacts: 4.")),
        () -> assertTrue(Files.exists(profileDirectory.resolve("codex.md"))),
        () -> assertTrue(Files.exists(profileDirectory.resolve("claude.md"))),
        () -> assertTrue(Files.exists(profileDirectory.resolve("cursor.md"))),
        () -> assertTrue(Files.exists(profileDirectory.resolve("generic.md"))),
        () -> assertEquals(1, countOccurrences(manifest, "\"name\": \"codex\"")),
        () -> assertEquals(1, countOccurrences(manifest, "\"name\": \"claude\"")),
        () -> assertEquals(1, countOccurrences(manifest, "\"name\": \"cursor\"")),
        () -> assertEquals(1, countOccurrences(manifest, "\"name\": \"generic\"")),
        () -> assertTrue(codex >= 0),
        () -> assertTrue(codex < claude),
        () -> assertTrue(claude < cursor),
        () -> assertTrue(cursor < generic));
  }

  @Test
  void scanUnsupportedAgentProfileReturnsUsageExitCodeWithoutScanning() {
    CliResult result = runCli("scan", tempDir.toString(), "--agent-profile", "unknown");

    assertAll(
        () -> assertEquals(2, result.exitCode()),
        () -> assertTrue(result.stderr().contains("Unsupported --agent-profile value.")),
        () -> assertFalse(result.stderr().contains("unknown")),
        () -> assertTrue(result.stderr().contains(AgentProjectMemoryCli.USAGE)),
        () -> assertFalse(Files.exists(tempDir.resolve(".project-memory"))));
  }

  @Test
  void scanAgentProfileFlagBeforePathReturnsUsageExitCodeWithoutScanning() {
    CliResult result = runCli("scan", "--agent-profile", "codex");

    assertAll(
        () -> assertEquals(2, result.exitCode()),
        () -> assertTrue(result.stderr().contains("Usage error: Missing scan path.")),
        () -> assertTrue(result.stderr().contains(AgentProjectMemoryCli.USAGE)),
        () -> assertFalse(Files.exists(tempDir.resolve(".project-memory"))));
  }

  @Test
  void scanWithAgentProfileDoesNotCreateOrphanArtifactsForUnsupportedDirectory() {
    CliResult result = runCli("scan", tempDir.toString(), "--agent-profile", "codex");
    Path outputDirectory = tempDir.resolve(".project-memory");

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertTrue(Files.isDirectory(outputDirectory)),
        () -> assertTrue(result.stdout().contains("No project memory output generated.")),
        () -> assertFalse(Files.exists(outputDirectory.resolve("agent-profiles"))));
  }

  @Test
  void scanWithIncrementalDoesNotCreateCacheForUnsupportedDirectory() {
    CliResult result = runCli("scan", tempDir.toString(), "--incremental");
    Path cacheDirectory = tempDir.resolve(".project-memory/cache");

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertTrue(result.stdout().contains("No project memory output generated.")),
        () -> assertFalse(result.stdout().contains("Updated incremental cache metadata.")),
        () -> assertFalse(Files.exists(cacheDirectory)));
  }

  @Test
  void scanIncrementalWritesCacheMetadataSchemaAndFingerprints() throws Exception {
    Files.writeString(tempDir.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);
    Files.createDirectories(tempDir.resolve("src/main/java/com/example"));
    Files.writeString(tempDir.resolve("src/main/java/com/example/Sample.java"), """
        package com.example;

        class Sample {}
        """);
    Files.writeString(tempDir.resolve("README.md"), "# Public Notes\n");
    Files.createDirectories(
        tempDir.resolve("target/generated-sources/openapi/src/main/java/com/example"));
    Files.writeString(
        tempDir.resolve("target/generated-sources/openapi/src/main/java/com/example/GeneratedApi.java"),
        """
        package com.example;
        // FAKE_GENERATED_CACHE_SECRET
        class GeneratedApi {}
        """);

    CliResult result = runCli(
        "scan",
        tempDir.toString(),
        "--agent-profile",
        "codex",
        "--incremental");
    Path outputDirectory = tempDir.resolve(".project-memory");
    Path cacheDirectory = outputDirectory.resolve("cache/v1");
    JsonNode manifest = JSON.readTree(Files.readString(cacheDirectory.resolve("manifest.json")));
    List<JsonNode> inputs = jsonLines(cacheDirectory.resolve("inputs.jsonl"));
    List<JsonNode> outputs = jsonLines(cacheDirectory.resolve("outputs.jsonl"));
    String inputsJsonl = Files.readString(cacheDirectory.resolve("inputs.jsonl"));
    String projectMap = Files.readString(outputDirectory.resolve("project-map.json"));
    String evidenceIndex = Files.readString(outputDirectory.resolve("evidence-index.jsonl"));

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertTrue(result.stdout().contains("Updated incremental cache metadata.")),
        () -> assertEquals("1.0", manifest.path("cache_schema_version").asText()),
        () -> assertEquals("1.0", manifest.path("project_map_schema_version").asText()),
        () -> assertEquals("incremental_scan_metadata", manifest.path("cache_kind").asText()),
        () -> assertEquals("whole_output_set", manifest.path("reuse_granularity").asText()),
        () -> assertEquals("sha256", manifest.path("fingerprint_algorithm").asText()),
        () -> assertEquals("cache/v1/inputs.jsonl", manifest.path("input_fingerprints_path").asText()),
        () -> assertEquals("cache/v1/outputs.jsonl", manifest.path("output_fingerprints_path").asText()),
        () -> assertTrue(manifest.path("tool_version").asText().length() > 0),
        () -> assertTrue(manifest.path("option_fingerprint").asText().startsWith("sha256:")),
        () -> assertEquals("not_detected", manifest.path("config_fingerprint").path("status").asText()),
        () -> assertTrue(manifest.path("config_fingerprint").path("path").isNull()),
        () -> assertTrue(manifest.path("config_fingerprint").path("sha256").isNull()),
        () -> assertEquals("codex", manifest.path("selected_profiles").get(0).asText()),
        () -> assertEquals("cache_is_not_evidence", manifest.path("evidence_policy").asText()),
        () -> assertFalse(manifest.path("raw_values_serialized").asBoolean()),
        () -> assertCacheInput(inputs, "maven_pom", "pom.xml", true),
        () -> assertCacheInput(inputs, "java_source", "src/main/java/com/example/Sample.java", true),
        () -> assertCacheInput(inputs, "local_markdown_document", "README.md", true),
        () -> assertCacheInput(inputs, "generated_source_root_path", "target/generated-sources", false),
        () -> assertCacheInput(inputs, "generated_source_root_path", "target/generated-sources/openapi", false),
        () -> assertCacheOutput(outputs, "project_map", "project-map.json"),
        () -> assertCacheOutput(outputs, "project_graph", "project-graph.json"),
        () -> assertCacheOutput(outputs, "evidence_index", "evidence-index.jsonl"),
        () -> assertCacheOutput(outputs, "endpoints_markdown", "endpoints.md"),
        () -> assertCacheOutput(outputs, "agent_guide_markdown", "agent-guide.md"),
        () -> assertCacheOutput(outputs, "agent_profile_manifest", "agent-profiles/manifest.json"),
        () -> assertCacheOutput(outputs, "agent_profile_markdown", "agent-profiles/codex.md"),
        () -> assertFalse(inputsJsonl.contains("GeneratedApi.java")),
        () -> assertFalse(inputsJsonl.contains("FAKE_GENERATED_CACHE_SECRET")),
        () -> assertFalse(projectMap.contains("\"cache\"")),
        () -> assertFalse(projectMap.contains("incremental")),
        () -> assertFalse(evidenceIndex.contains("cache/v1")),
        () -> assertFalse(evidenceIndex.contains("incremental")));
  }

  @Test
  void scanIncrementalUsesValidatedCacheHitWithoutRegeneratingOutputs() throws Exception {
    Files.writeString(tempDir.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);

    CliResult warmup = runCli("scan", tempDir.toString(), "--incremental");
    Path outputDirectory = tempDir.resolve(".project-memory");
    String projectMap = Files.readString(outputDirectory.resolve("project-map.json"));
    String projectGraph = Files.readString(outputDirectory.resolve("project-graph.json"));
    String evidenceIndex = Files.readString(outputDirectory.resolve("evidence-index.jsonl"));
    String endpoints = Files.readString(outputDirectory.resolve("endpoints.md"));
    String agentGuide = Files.readString(outputDirectory.resolve("agent-guide.md"));

    CliResult hit = runCli("scan", tempDir.toString(), "--incremental");

    assertAll(
        () -> assertEquals(0, warmup.exitCode()),
        () -> assertFullIncrementalRefresh(warmup),
        () -> assertEquals(0, hit.exitCode()),
        () -> assertIncrementalHit(hit),
        () -> assertEquals(projectMap, Files.readString(outputDirectory.resolve("project-map.json"))),
        () -> assertEquals(projectGraph, Files.readString(outputDirectory.resolve("project-graph.json"))),
        () -> assertEquals(evidenceIndex, Files.readString(outputDirectory.resolve("evidence-index.jsonl"))),
        () -> assertEquals(endpoints, Files.readString(outputDirectory.resolve("endpoints.md"))),
        () -> assertEquals(agentGuide, Files.readString(outputDirectory.resolve("agent-guide.md"))));
  }

  @Test
  void scanIncrementalFallsBackWhenProjectGraphOutputFingerprintMismatches()
      throws Exception {
    Files.writeString(tempDir.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);

    CliResult warmup = runCli("scan", tempDir.toString(), "--incremental");
    Path outputDirectory = tempDir.resolve(".project-memory");
    Path projectGraphPath = outputDirectory.resolve("project-graph.json");
    String projectGraph = Files.readString(projectGraphPath);
    Files.writeString(projectGraphPath, "{\"tampered\":true}\n");

    CliResult result = runCli("scan", tempDir.toString(), "--incremental");

    assertAll(
        () -> assertEquals(0, warmup.exitCode()),
        () -> assertFullIncrementalRefresh(warmup),
        () -> assertFullIncrementalRefresh(result),
        () -> assertEquals(projectGraph, Files.readString(projectGraphPath)));
  }

  @Test
  void scanIncrementalFingerprintsEmptyStandardRootDirectoriesAcrossSupportedModules()
      throws Exception {
    Files.writeString(tempDir.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
          <modules>
            <module>service</module>
          </modules>
        </project>
        """);
    Files.createDirectories(tempDir.resolve("service"));
    Files.writeString(tempDir.resolve("service/pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);
    Files.writeString(tempDir.resolve("settings.gradle"), "include 'client'\n");
    Files.writeString(tempDir.resolve("build.gradle"), "plugins { id 'java' }\n");
    Files.createDirectories(tempDir.resolve("client"));
    Files.writeString(tempDir.resolve("client/build.gradle"), "plugins { id 'java' }\n");
    Files.createDirectories(tempDir.resolve("src/main/java"));
    Files.createDirectories(tempDir.resolve("src/test/java"));
    Files.createDirectories(tempDir.resolve("src/main/resources"));
    Files.createDirectories(tempDir.resolve("src/test/resources"));
    Files.createDirectories(tempDir.resolve("service/src/main/java"));
    Files.createDirectories(tempDir.resolve("client/src/test/resources"));

    CliResult result = runCli("scan", tempDir.toString(), "--incremental");
    List<JsonNode> inputs = jsonLines(tempDir.resolve(".project-memory/cache/v1/inputs.jsonl"));

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertCacheInput(inputs, "java_source_root_path", "src/main/java", false),
        () -> assertCacheInput(inputs, "java_test_root_path", "src/test/java", false),
        () -> assertCacheInput(inputs, "resource_root_path", "src/main/resources", false),
        () -> assertCacheInput(inputs, "resource_root_path", "src/test/resources", false),
        () -> assertCacheInput(inputs, "java_source_root_path", "service/src/main/java", false),
        () -> assertCacheInput(inputs, "resource_root_path", "client/src/test/resources", false));
  }

  @Test
  void scanIncrementalFallsBackAndRefreshesForAddedEditedDeletedAndRenamedInputs()
      throws Exception {
    Files.writeString(tempDir.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);
    Path javaFile = tempDir.resolve("src/main/java/com/example/Sample.java");
    Files.createDirectories(javaFile.getParent());
    Files.writeString(javaFile, "package com.example;\nclass Sample {}\n");

    assertFullIncrementalRefresh(runCli("scan", tempDir.toString(), "--incremental"));
    assertIncrementalHit(runCli("scan", tempDir.toString(), "--incremental"));

    Files.writeString(javaFile, "package com.example;\nclass Sample { String changed; }\n");
    assertFullIncrementalRefresh(runCli("scan", tempDir.toString(), "--incremental"));
    assertIncrementalHit(runCli("scan", tempDir.toString(), "--incremental"));

    Path readme = tempDir.resolve("README.md");
    Files.writeString(readme, "# Public Notes\n");
    assertFullIncrementalRefresh(runCli("scan", tempDir.toString(), "--incremental"));
    assertIncrementalHit(runCli("scan", tempDir.toString(), "--incremental"));

    Files.delete(readme);
    assertFullIncrementalRefresh(runCli("scan", tempDir.toString(), "--incremental"));
    assertIncrementalHit(runCli("scan", tempDir.toString(), "--incremental"));

    Path renamedJavaFile = tempDir.resolve("src/main/java/com/example/Renamed.java");
    Files.move(javaFile, renamedJavaFile);
    assertFullIncrementalRefresh(runCli("scan", tempDir.toString(), "--incremental"));
    assertIncrementalHit(runCli("scan", tempDir.toString(), "--incremental"));
  }

  @Test
  void scanIncrementalInputFingerprintsChangeWhenEmptyStandardRootIsDeletedAndAdded()
      throws Exception {
    Files.writeString(tempDir.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);
    Path javaRoot = tempDir.resolve("src/main/java");
    Files.createDirectories(javaRoot);
    Files.createDirectories(tempDir.resolve("src/test/resources"));

    assertEquals(0, runCli("scan", tempDir.toString(), "--incremental").exitCode());
    Path inputsPath = tempDir.resolve(".project-memory/cache/v1/inputs.jsonl");
    String firstInputs = Files.readString(inputsPath);
    assertTrue(hasCacheInput(
        jsonLines(inputsPath),
        "java_source_root_path",
        "src/main/java"));

    Files.delete(javaRoot);
    assertEquals(0, runCli("scan", tempDir.toString(), "--incremental").exitCode());
    String deletedInputs = Files.readString(inputsPath);
    assertFalse(hasCacheInput(
        jsonLines(inputsPath),
        "java_source_root_path",
        "src/main/java"));

    Files.createDirectories(javaRoot);
    assertEquals(0, runCli("scan", tempDir.toString(), "--incremental").exitCode());
    String addedInputs = Files.readString(inputsPath);

    assertAll(
        () -> assertNotEquals(firstInputs, deletedInputs),
        () -> assertNotEquals(deletedInputs, addedInputs),
        () -> assertTrue(hasCacheInput(
            jsonLines(inputsPath),
            "java_source_root_path",
            "src/main/java")));
  }

  @Test
  void scanWithoutIncrementalIgnoresExistingCacheStateAndPreservesFullOutputs()
      throws Exception {
    Files.writeString(tempDir.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);
    assertEquals(0, runCli("scan", tempDir.toString()).exitCode());
    Path outputDirectory = tempDir.resolve(".project-memory");
    String projectMap = Files.readString(outputDirectory.resolve("project-map.json"));
    String projectGraph = Files.readString(outputDirectory.resolve("project-graph.json"));
    String evidenceIndex = Files.readString(outputDirectory.resolve("evidence-index.jsonl"));
    String endpoints = Files.readString(outputDirectory.resolve("endpoints.md"));
    String agentGuide = Files.readString(outputDirectory.resolve("agent-guide.md"));
    Path cacheDirectory = outputDirectory.resolve("cache/v1");
    Files.createDirectories(cacheDirectory);
    Files.writeString(cacheDirectory.resolve("manifest.json"), "{not-json");

    CliResult result = runCli("scan", tempDir.toString());

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertFalse(result.stdout().contains("cache metadata")),
        () -> assertEquals("{not-json", Files.readString(cacheDirectory.resolve("manifest.json"))),
        () -> assertFalse(Files.exists(cacheDirectory.resolve("inputs.jsonl"))),
        () -> assertFalse(Files.exists(cacheDirectory.resolve("outputs.jsonl"))),
        () -> assertEquals(projectMap, Files.readString(outputDirectory.resolve("project-map.json"))),
        () -> assertEquals(projectGraph, Files.readString(outputDirectory.resolve("project-graph.json"))),
        () -> assertEquals(evidenceIndex, Files.readString(outputDirectory.resolve("evidence-index.jsonl"))),
        () -> assertEquals(endpoints, Files.readString(outputDirectory.resolve("endpoints.md"))),
        () -> assertEquals(agentGuide, Files.readString(outputDirectory.resolve("agent-guide.md"))));
  }

  @Test
  void scanIncrementalOverwritesCorruptRegularCacheMetadataAfterMiss()
      throws Exception {
    Files.writeString(tempDir.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);
    Path cacheDirectory = tempDir.resolve(".project-memory/cache/v1");
    Files.createDirectories(cacheDirectory);
    Files.writeString(cacheDirectory.resolve("manifest.json"), "{not-json");
    Files.writeString(cacheDirectory.resolve("inputs.jsonl"), "not-json\n");
    Files.writeString(cacheDirectory.resolve("outputs.jsonl"), "not-json\n");

    CliResult result = runCli("scan", tempDir.toString(), "--incremental");
    JsonNode manifest = JSON.readTree(Files.readString(cacheDirectory.resolve("manifest.json")));

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertTrue(result.stdout().contains("Updated incremental cache metadata.")),
        () -> assertEquals("1.0", manifest.path("cache_schema_version").asText()),
        () -> assertFalse(Files.readString(cacheDirectory.resolve("inputs.jsonl")).contains("not-json")),
        () -> assertFalse(Files.readString(cacheDirectory.resolve("outputs.jsonl")).contains("not-json")));
  }

  @Test
  void scanIncrementalFallsBackWhenCacheSchemaMismatches() throws Exception {
    Files.writeString(tempDir.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);
    assertFullIncrementalRefresh(runCli("scan", tempDir.toString(), "--incremental"));
    Path manifestPath = tempDir.resolve(".project-memory/cache/v1/manifest.json");
    Files.writeString(
        manifestPath,
        Files.readString(manifestPath).replace(
            "\"cache_schema_version\": \"1.0\"",
            "\"cache_schema_version\": \"0.9\""));

    CliResult result = runCli("scan", tempDir.toString(), "--incremental");
    JsonNode manifest = JSON.readTree(Files.readString(manifestPath));

    assertAll(
        () -> assertFullIncrementalRefresh(result),
        () -> assertEquals("1.0", manifest.path("cache_schema_version").asText()));
  }

  @Test
  void scanIncrementalFingerprintsChangeWhenInputBytesChange() throws Exception {
    Files.writeString(tempDir.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);
    Path javaFile = tempDir.resolve("src/main/java/com/example/Sample.java");
    Files.createDirectories(javaFile.getParent());
    Files.writeString(javaFile, "package com.example;\nclass Sample {}\n");

    assertEquals(0, runCli("scan", tempDir.toString(), "--incremental").exitCode());
    Path inputsPath = tempDir.resolve(".project-memory/cache/v1/inputs.jsonl");
    String firstHash = cacheInput(jsonLines(inputsPath), "java_source", "src/main/java/com/example/Sample.java")
        .path("content_sha256")
        .asText();

    Files.writeString(javaFile, "package com.example;\nclass Sample { String changed; }\n");
    assertEquals(0, runCli("scan", tempDir.toString(), "--incremental").exitCode());
    String secondHash = cacheInput(jsonLines(inputsPath), "java_source", "src/main/java/com/example/Sample.java")
        .path("content_sha256")
        .asText();

    assertNotEquals(firstHash, secondHash);
  }

  @Test
  void scanIncrementalRecordsConfigOptionAndProfileMatchingMetadata() throws Exception {
    Files.writeString(tempDir.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);
    assertEquals(0, runCli("scan", tempDir.toString(), "--incremental").exitCode());
    Path manifestPath = tempDir.resolve(".project-memory/cache/v1/manifest.json");
    JsonNode defaultManifest = JSON.readTree(Files.readString(manifestPath));
    Files.createDirectories(tempDir.resolve("config"));
    Path config = tempDir.resolve("config/custom.yml");
    Files.writeString(config, """
        version: 1
        features:
          local_markdown: false
        """);

    CliResult result = runCli(
        "scan",
        tempDir.toString(),
        "--incremental",
        "--config",
        "config/custom.yml",
        "--agent-profile",
        "codex");
    JsonNode selectedManifest = JSON.readTree(Files.readString(manifestPath));
    String firstConfigHash = selectedManifest.path("config_fingerprint").path("sha256").asText();

    Files.writeString(config, """
        version: 1
        features:
          local_markdown: true
        """);
    assertEquals(
        0,
        runCli(
            "scan",
            tempDir.toString(),
            "--incremental",
            "--config",
            "config/custom.yml",
            "--agent-profile",
            "codex").exitCode());
    JsonNode changedConfigManifest = JSON.readTree(Files.readString(manifestPath));

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertNotEquals(
            defaultManifest.path("option_fingerprint").asText(),
            selectedManifest.path("option_fingerprint").asText()),
        () -> assertTrue(defaultManifest.path("selected_profiles").isEmpty()),
        () -> assertEquals("codex", selectedManifest.path("selected_profiles").get(0).asText()),
        () -> assertEquals("explicit", selectedManifest.path("config_fingerprint").path("status").asText()),
        () -> assertEquals("config/custom.yml", selectedManifest.path("config_fingerprint").path("path").asText()),
        () -> assertTrue(firstConfigHash.startsWith("sha256:")),
        () -> assertNotEquals(
            firstConfigHash,
            changedConfigManifest.path("config_fingerprint").path("sha256").asText()),
        () -> assertCacheOutput(
            jsonLines(tempDir.resolve(".project-memory/cache/v1/outputs.jsonl")),
            "agent_profile_markdown",
            "agent-profiles/codex.md"));
  }

  @Test
  void scanIncrementalFallsBackOnConfigAndProfileMismatchThenHitsSelectedProfileArtifacts()
      throws Exception {
    Files.writeString(tempDir.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);
    Files.createDirectories(tempDir.resolve("config"));
    Files.writeString(tempDir.resolve("config/custom.yml"), """
        version: 1
        features:
          local_markdown: false
        """);

    assertFullIncrementalRefresh(runCli("scan", tempDir.toString(), "--incremental"));

    CliResult profileMiss = runCli(
        "scan",
        tempDir.toString(),
        "--incremental",
        "--agent-profile",
        "codex");
    assertFullIncrementalRefresh(profileMiss);
    Path profile = tempDir.resolve(".project-memory/agent-profiles/codex.md");
    String profileContent = Files.readString(profile);

    CliResult profileHit = runCli(
        "scan",
        tempDir.toString(),
        "--incremental",
        "--agent-profile",
        "codex");
    assertAll(
        () -> assertIncrementalHit(profileHit),
        () -> assertEquals(profileContent, Files.readString(profile)));

    CliResult configMiss = runCli(
        "scan",
        tempDir.toString(),
        "--incremental",
        "--config",
        "config/custom.yml",
        "--agent-profile",
        "codex");
    assertFullIncrementalRefresh(configMiss);

    CliResult configHit = runCli(
        "scan",
        tempDir.toString(),
        "--incremental",
        "--config",
        "config/custom.yml",
        "--agent-profile",
        "codex");
    assertIncrementalHit(configHit);
  }

  @Test
  void scanIncrementalFallsBackWhenSelectedOutputDigestMismatches() throws Exception {
    Files.writeString(tempDir.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);

    assertFullIncrementalRefresh(runCli("scan", tempDir.toString(), "--incremental"));
    Path agentGuide = tempDir.resolve(".project-memory/agent-guide.md");
    Files.writeString(agentGuide, "tampered generated output");

    CliResult result = runCli("scan", tempDir.toString(), "--incremental");

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertFullIncrementalRefresh(result),
        () -> assertFalse(Files.readString(agentGuide).contains("tampered generated output")));
  }

  @Test
  void scanIncrementalFallsBackWhenSelectedProfileOutputDigestMismatches() throws Exception {
    Files.writeString(tempDir.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);

    assertFullIncrementalRefresh(runCli(
        "scan",
        tempDir.toString(),
        "--incremental",
        "--agent-profile",
        "codex"));
    Path profile = tempDir.resolve(".project-memory/agent-profiles/codex.md");
    Files.writeString(profile, "tampered profile output");

    CliResult result = runCli(
        "scan",
        tempDir.toString(),
        "--incremental",
        "--agent-profile",
        "codex");

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertFullIncrementalRefresh(result),
        () -> assertFalse(Files.readString(profile).contains("tampered profile output")));
  }

  @Test
  void scanIncrementalFallsBackWhenGeneratedSourceChildBecomesUnsafe() throws Exception {
    Files.writeString(tempDir.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);
    Path generatedSourceRoot = tempDir.resolve("target/generated-sources");
    Files.createDirectories(generatedSourceRoot);
    assertFullIncrementalRefresh(runCli("scan", tempDir.toString(), "--incremental"));
    assertIncrementalHit(runCli("scan", tempDir.toString(), "--incremental"));

    Path outsideGeneratedSource = tempDir.resolve("outside-generated-source");
    Files.createDirectories(outsideGeneratedSource);
    createSymbolicLink(generatedSourceRoot.resolve("openapi"), outsideGeneratedSource);

    CliResult result = runCli("scan", tempDir.toString(), "--incremental");
    List<JsonNode> inputs = jsonLines(tempDir.resolve(".project-memory/cache/v1/inputs.jsonl"));

    assertAll(
        () -> assertFullIncrementalRefresh(result),
        () -> assertCacheInput(
            inputs,
            "generated_source_root_unsafe_path",
            "target/generated-sources/openapi",
            false));
  }

  @Test
  void scanIncrementalMatchesFullScanWhenSymlinkedResourceConfigIsRemoved()
      throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Files.createDirectories(repositoryRoot);
    Files.writeString(repositoryRoot.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);
    writeSymlinkedResourceConfig(repositoryRoot, repositoryRoot.resolve("shared/application.yml"));
    Path symlinkedConfig = repositoryRoot.resolve("src/main/resources/application.yml");

    assertFullIncrementalRefresh(runCli("scan", repositoryRoot.toString(), "--incremental"));
    Path outputDirectory = repositoryRoot.resolve(".project-memory");
    Path cacheDirectory = outputDirectory.resolve("cache/v1");
    String cacheMetadata = Files.readString(cacheDirectory.resolve("manifest.json"))
        + Files.readString(cacheDirectory.resolve("inputs.jsonl"))
        + Files.readString(cacheDirectory.resolve("outputs.jsonl"));

    Files.delete(symlinkedConfig);

    CliResult incrementalAfterRemoval = runCli("scan", repositoryRoot.toString(), "--incremental");
    String incrementalProjectMap = Files.readString(outputDirectory.resolve("project-map.json"));
    String incrementalEvidenceIndex = Files.readString(outputDirectory.resolve("evidence-index.jsonl"));
    String incrementalEndpoints = Files.readString(outputDirectory.resolve("endpoints.md"));
    String incrementalAgentGuide = Files.readString(outputDirectory.resolve("agent-guide.md"));

    CliResult fullAfterRemoval = runCli("scan", repositoryRoot.toString());

    assertAll(
        () -> assertEquals(0, incrementalAfterRemoval.exitCode()),
        () -> assertEquals(0, fullAfterRemoval.exitCode()),
        () -> assertFalse(hasCacheInput(
            jsonLines(cacheDirectory.resolve("inputs.jsonl")),
            "resource_config_file",
            "src/main/resources/application.yml")),
        () -> assertFalse(cacheMetadata.contains("FAKE_SYMLINK_RESOURCE_CONFIG_SECRET")),
        () -> assertFalse(cacheMetadata.contains("api-token")),
        () -> assertFalse(incrementalProjectMap.contains("src/main/resources/application.yml")),
        () -> assertEquals(incrementalProjectMap, Files.readString(outputDirectory.resolve("project-map.json"))),
        () -> assertEquals(incrementalEvidenceIndex, Files.readString(outputDirectory.resolve("evidence-index.jsonl"))),
        () -> assertEquals(incrementalEndpoints, Files.readString(outputDirectory.resolve("endpoints.md"))),
        () -> assertEquals(incrementalAgentGuide, Files.readString(outputDirectory.resolve("agent-guide.md"))));
  }

  @Test
  void scanIncrementalMatchesFullScanWhenSymlinkedResourceConfigTargetBecomesUnsafe()
      throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Files.createDirectories(repositoryRoot);
    Files.writeString(repositoryRoot.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);
    writeSymlinkedResourceConfig(repositoryRoot, repositoryRoot.resolve("shared/application.yml"));
    Path symlinkedConfig = repositoryRoot.resolve("src/main/resources/application.yml");
    Path outsideConfig = tempDir.resolve("outside-resource-config.yml");
    Files.writeString(outsideConfig, """
        api-token: FAKE_OUTSIDE_SYMLINK_RESOURCE_CONFIG_SECRET
        """);

    assertFullIncrementalRefresh(runCli("scan", repositoryRoot.toString(), "--incremental"));
    Files.delete(symlinkedConfig);
    createSymbolicLink(symlinkedConfig, outsideConfig);

    CliResult incrementalAfterTargetChange = runCli("scan", repositoryRoot.toString(), "--incremental");
    Path outputDirectory = repositoryRoot.resolve(".project-memory");
    String incrementalProjectMap = Files.readString(outputDirectory.resolve("project-map.json"));
    String incrementalEvidenceIndex = Files.readString(outputDirectory.resolve("evidence-index.jsonl"));
    String incrementalEndpoints = Files.readString(outputDirectory.resolve("endpoints.md"));
    String incrementalAgentGuide = Files.readString(outputDirectory.resolve("agent-guide.md"));

    CliResult fullAfterTargetChange = runCli("scan", repositoryRoot.toString());
    String cacheMetadata = Files.readString(outputDirectory.resolve("cache/v1/manifest.json"))
        + Files.readString(outputDirectory.resolve("cache/v1/inputs.jsonl"))
        + Files.readString(outputDirectory.resolve("cache/v1/outputs.jsonl"));

    assertAll(
        () -> assertEquals(0, incrementalAfterTargetChange.exitCode()),
        () -> assertEquals(0, fullAfterTargetChange.exitCode()),
        () -> assertFalse(incrementalProjectMap.contains("src/main/resources/application.yml")),
        () -> assertFalse(cacheMetadata.contains("FAKE_OUTSIDE_SYMLINK_RESOURCE_CONFIG_SECRET")),
        () -> assertFalse(cacheMetadata.contains(outsideConfig.toString())),
        () -> assertEquals(incrementalProjectMap, Files.readString(outputDirectory.resolve("project-map.json"))),
        () -> assertEquals(incrementalEvidenceIndex, Files.readString(outputDirectory.resolve("evidence-index.jsonl"))),
        () -> assertEquals(incrementalEndpoints, Files.readString(outputDirectory.resolve("endpoints.md"))),
        () -> assertEquals(incrementalAgentGuide, Files.readString(outputDirectory.resolve("agent-guide.md"))));
  }

  @Test
  void scanIncrementalMatchesFullScanWhenSymlinkedResourceRootParentSegmentIsRemoved()
      throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Files.createDirectories(repositoryRoot);
    Files.writeString(repositoryRoot.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);
    writeResourceConfigBehindSymlinkedMainParent(repositoryRoot);
    Path symlinkedParent = repositoryRoot.resolve("src/main");

    assertFullIncrementalRefresh(runCli("scan", repositoryRoot.toString(), "--incremental"));
    Path outputDirectory = repositoryRoot.resolve(".project-memory");
    Path cacheDirectory = outputDirectory.resolve("cache/v1");
    String cacheMetadata = Files.readString(cacheDirectory.resolve("manifest.json"))
        + Files.readString(cacheDirectory.resolve("inputs.jsonl"))
        + Files.readString(cacheDirectory.resolve("outputs.jsonl"));

    Files.delete(symlinkedParent);

    CliResult incrementalAfterRemoval = runCli("scan", repositoryRoot.toString(), "--incremental");
    String incrementalProjectMap = Files.readString(outputDirectory.resolve("project-map.json"));
    String incrementalEvidenceIndex = Files.readString(outputDirectory.resolve("evidence-index.jsonl"));
    String incrementalEndpoints = Files.readString(outputDirectory.resolve("endpoints.md"));
    String incrementalAgentGuide = Files.readString(outputDirectory.resolve("agent-guide.md"));

    CliResult fullAfterRemoval = runCli("scan", repositoryRoot.toString());

    assertAll(
        () -> assertEquals(0, incrementalAfterRemoval.exitCode()),
        () -> assertEquals(0, fullAfterRemoval.exitCode()),
        () -> assertFalse(hasCacheInput(
            jsonLines(cacheDirectory.resolve("inputs.jsonl")),
            "resource_root_path",
            "src/main/resources")),
        () -> assertFalse(hasCacheInput(
            jsonLines(cacheDirectory.resolve("inputs.jsonl")),
            "resource_config_file",
            "src/main/resources/application.yml")),
        () -> assertFalse(cacheMetadata.contains("FAKE_SYMLINK_RESOURCE_CONFIG_SECRET")),
        () -> assertFalse(cacheMetadata.contains("api-token")),
        () -> assertFalse(incrementalProjectMap.contains("src/main/resources/application.yml")),
        () -> assertEquals(incrementalProjectMap, Files.readString(outputDirectory.resolve("project-map.json"))),
        () -> assertEquals(incrementalEvidenceIndex, Files.readString(outputDirectory.resolve("evidence-index.jsonl"))),
        () -> assertEquals(incrementalEndpoints, Files.readString(outputDirectory.resolve("endpoints.md"))),
        () -> assertEquals(incrementalAgentGuide, Files.readString(outputDirectory.resolve("agent-guide.md"))));
  }

  @Test
  void scanIncrementalSkipsCacheRefreshWhenCacheDirectoryIsSymlink() throws Exception {
    Files.writeString(tempDir.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);
    Path outputDirectory = tempDir.resolve(".project-memory");
    Files.createDirectories(outputDirectory);
    Path outsideCache = tempDir.resolve("outside-cache");
    Files.createDirectories(outsideCache);
    createSymbolicLink(outputDirectory.resolve("cache"), outsideCache);

    CliResult result = runCli("scan", tempDir.toString(), "--incremental");

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertFalse(result.stdout().contains("Reused incremental cache output set.")),
        () -> assertTrue(result.stdout().contains("Skipped incremental cache metadata refresh.")),
        () -> assertFalse(Files.exists(outsideCache.resolve("v1/manifest.json"))),
        () -> assertFalse(Files.exists(outsideCache.resolve("v1/inputs.jsonl"))),
        () -> assertFalse(Files.exists(outsideCache.resolve("v1/outputs.jsonl"))),
        () -> assertTrue(Files.exists(outputDirectory.resolve("project-map.json"))));
  }

  @Test
  void scanIncrementalSkipsCacheRefreshWhenCacheTargetIsHardLink() throws Exception {
    Files.writeString(tempDir.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);
    Path cacheDirectory = tempDir.resolve(".project-memory/cache/v1");
    Files.createDirectories(cacheDirectory);
    Path outsideManifest = tempDir.resolve("outside-manifest.json");
    Files.writeString(outsideManifest, "outside content");
    createHardLink(cacheDirectory.resolve("manifest.json"), outsideManifest);

    CliResult result = runCli("scan", tempDir.toString(), "--incremental");

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertFalse(result.stdout().contains("Reused incremental cache output set.")),
        () -> assertTrue(result.stdout().contains("Skipped incremental cache metadata refresh.")),
        () -> assertEquals("outside content", Files.readString(outsideManifest)),
        () -> assertEquals("outside content", Files.readString(cacheDirectory.resolve("manifest.json"))),
        () -> assertFalse(Files.exists(cacheDirectory.resolve("inputs.jsonl"))),
        () -> assertFalse(Files.exists(cacheDirectory.resolve("outputs.jsonl"))));
  }

  @Test
  void scanIncrementalPreservesByteEqualBaseOutputsFromFullScan() throws Exception {
    Path projectPath = tempDir.resolve("fixture-project");
    copyDirectory(fixtureRoot(), projectPath);

    assertEquals(0, runCli("scan", projectPath.toString()).exitCode());
    Path outputDirectory = projectPath.resolve(".project-memory");
    String projectMap = Files.readString(outputDirectory.resolve("project-map.json"));
    String evidenceIndex = Files.readString(outputDirectory.resolve("evidence-index.jsonl"));
    String endpoints = Files.readString(outputDirectory.resolve("endpoints.md"));
    String agentGuide = Files.readString(outputDirectory.resolve("agent-guide.md"));

    CliResult result = runCli("scan", projectPath.toString(), "--incremental");

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertEquals(projectMap, Files.readString(outputDirectory.resolve("project-map.json"))),
        () -> assertEquals(evidenceIndex, Files.readString(outputDirectory.resolve("evidence-index.jsonl"))),
        () -> assertEquals(endpoints, Files.readString(outputDirectory.resolve("endpoints.md"))),
        () -> assertEquals(agentGuide, Files.readString(outputDirectory.resolve("agent-guide.md"))),
        () -> assertTrue(Files.exists(outputDirectory.resolve("cache/v1/manifest.json"))));
  }

  @Test
  void scanIncrementalAllProfilesKeepsFakeSecretLikeValuesOutOfG003Surfaces()
      throws Exception {
    List<String> fakeNeedles = List.of(
        "FAKE_V170_CONFIG_COMMENT_SECRET",
        "FAKE_V170_MAVEN_ARTIFACT_SECRET",
        "FAKE_V170_MARKDOWN_TITLE_SECRET",
        "FAKE_V170_MARKDOWN_BODY_SECRET",
        "FAKE_V170_RESOURCE_CONFIG_SECRET",
        "FAKE_V170_GENERATED_SOURCE_BODY_SECRET",
        "FAKE_V170_OPENAPI_TAG_SECRET",
        "FAKE_V170_JAVA_CLASS_SECRET",
        "FAKE_V170_JAVA_HEADER_SECRET");
    writeFile(tempDir.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
          <groupId>com.example</groupId>
          <artifactId>password=FAKE_V170_MAVEN_ARTIFACT_SECRET</artifactId>
          <version>1.0.0</version>
        </project>
        """);
    writeFile(tempDir.resolve("agent-project-memory.yml"), """
        version: 1
        features:
          local_markdown: true
        documents:
          include:
            - docs/*.md
        # password=FAKE_V170_CONFIG_COMMENT_SECRET
        """);
    writeFile(tempDir.resolve("docs/public.md"), """
        # Authorization: Bearer FAKE_V170_MARKDOWN_TITLE_SECRET

        This local Markdown body mentions password=FAKE_V170_MARKDOWN_BODY_SECRET.
        """);
    writeFile(tempDir.resolve("src/main/resources/application.yml"), """
        api-token: FAKE_V170_RESOURCE_CONFIG_SECRET
        """);
    writeFile(tempDir.resolve("target/generated-sources/openapi/GeneratedSecret.java"), """
        package com.example.generated;
        class GeneratedSecret {
          static final String TOKEN = "FAKE_V170_GENERATED_SOURCE_BODY_SECRET";
        }
        """);
    writeFile(tempDir.resolve("src/main/resources/openapi.yml"), """
        openapi: 3.0.0
        info:
          title: G003 fixture
          version: 1.0.0
        paths:
          /v170-safe:
            get:
              operationId: v170Safe
              tags:
                - password=FAKE_V170_OPENAPI_TAG_SECRET
              responses:
                '200':
                  description: ok
        """);
    writeFile(tempDir.resolve("src/main/java/com/example/SecretController.java"), """
        package com.example;

        import org.springframework.web.bind.annotation.GetMapping;
        import org.springframework.web.bind.annotation.RestController;

        @RestController("clientSecret=FAKE_V170_JAVA_CLASS_SECRET")
        class SecretController {
          @GetMapping(value = "/v170-safe", headers = "Authorization: Bearer FAKE_V170_JAVA_HEADER_SECRET")
          String v170Safe() {
            return "ok";
          }
        }
        """);

    CliResult scan = runCli(
        "scan",
        tempDir.toString(),
        "--incremental",
        "--agent-profile",
        "all");
    Path outputDirectory = tempDir.resolve(".project-memory");
    List<Path> surfaceFiles = List.of(
        outputDirectory.resolve("project-map.json"),
        outputDirectory.resolve("project-graph.json"),
        outputDirectory.resolve("evidence-index.jsonl"),
        outputDirectory.resolve("endpoints.md"),
        outputDirectory.resolve("agent-guide.md"),
        outputDirectory.resolve("agent-profiles/manifest.json"),
        outputDirectory.resolve("agent-profiles/codex.md"),
        outputDirectory.resolve("agent-profiles/claude.md"),
        outputDirectory.resolve("agent-profiles/cursor.md"),
        outputDirectory.resolve("agent-profiles/generic.md"),
        outputDirectory.resolve("cache/v1/manifest.json"),
        outputDirectory.resolve("cache/v1/inputs.jsonl"),
        outputDirectory.resolve("cache/v1/outputs.jsonl"));
    String generatedSurfaces = readAll(surfaceFiles);
    List<JsonNode> cacheInputs = jsonLines(outputDirectory.resolve("cache/v1/inputs.jsonl"));
    List<JsonNode> cacheOutputs = jsonLines(outputDirectory.resolve("cache/v1/outputs.jsonl"));
    String getMappingEvidenceId = evidenceId(
        outputDirectory.resolve("evidence-index.jsonl"),
        "src/main/java/com/example/SecretController.java",
        "@GetMapping");
    CliResult queryEndpoints = runCli("query", tempDir.toString(), "list", "endpoints");
    CliResult queryApiOperations = runCli("query", tempDir.toString(), "list", "api-operations");
    CliResult queryEvidence = runCli(
        "query",
        tempDir.toString(),
        "explain",
        "evidence",
        getMappingEvidenceId);
    String querySurfaces = String.join(
        "\n",
        queryEndpoints.stdout(),
        queryEndpoints.stderr(),
        queryApiOperations.stdout(),
        queryApiOperations.stderr(),
        queryEvidence.stdout(),
        queryEvidence.stderr());
    CliResult cliError = runCli(
        "scan",
        tempDir.toString(),
        "--config",
        "config/password=FAKE_V170_CLI_ERROR_SECRET.txt");

    assertAll(
        () -> assertEquals(0, scan.exitCode()),
        () -> assertTrue(scan.stderr().isEmpty()),
        () -> assertTrue(scan.stdout().contains("Generated project-map.json")),
        () -> assertTrue(scan.stdout().contains("Generated project-graph.json")),
        () -> assertTrue(scan.stdout().contains("Generated agent profile artifacts: 4.")),
        () -> assertTrue(scan.stdout().contains("Updated incremental cache metadata.")),
        () -> assertTrue(generatedSurfaces.contains(OutputRedactor.REDACTION_MARKER)),
        () -> assertFalse(generatedSurfaces.contains(tempDir.toString())),
        () -> assertFalse((scan.stdout() + scan.stderr()).contains(tempDir.toString())),
        () -> assertFalse((scan.stdout() + scan.stderr()).contains("\tat ")),
        () -> assertEquals(0, queryEndpoints.exitCode()),
        () -> assertEquals(0, queryApiOperations.exitCode()),
        () -> assertEquals(0, queryEvidence.exitCode()),
        () -> assertTrue(querySurfaces.contains(OutputRedactor.REDACTION_MARKER)),
        () -> assertFalse(querySurfaces.contains(tempDir.toString())),
        () -> assertFalse(querySurfaces.contains("\tat ")),
        () -> assertEquals(4, cliError.exitCode()),
        () -> assertTrue(cliError.stdout().isEmpty()),
        () -> assertFalse(cliError.stderr().contains("FAKE_V170_CLI_ERROR_SECRET")),
        () -> assertFalse(cliError.stderr().contains(tempDir.toString())),
        () -> assertFalse(cliError.stderr().contains("\tat ")));
    assertFakeNeedlesAbsent("scan stdout/stderr", scan.stdout() + scan.stderr(), fakeNeedles);
    assertFakeNeedlesAbsent("generated artifacts/profile/cache", generatedSurfaces, fakeNeedles);
    assertFakeNeedlesAbsent("query stdout/stderr", querySurfaces, fakeNeedles);
    assertCacheInput(cacheInputs, "scan_config", "agent-project-memory.yml", true);
    assertCacheInput(cacheInputs, "maven_pom", "pom.xml", true);
    assertCacheInput(cacheInputs, "local_markdown_document", "docs/public.md", true);
    assertCacheInput(cacheInputs, "resource_config_file", "src/main/resources/application.yml", true);
    assertCacheInput(cacheInputs, "openapi_spec", "src/main/resources/openapi.yml", true);
    assertCacheInput(cacheInputs, "generated_source_root_path", "target/generated-sources", false);
    assertCacheInput(
        cacheInputs,
        "generated_source_root_path",
        "target/generated-sources/openapi",
        false);
    assertCacheOutput(cacheOutputs, "project_map", "project-map.json");
    assertCacheOutput(cacheOutputs, "project_graph", "project-graph.json");
    assertCacheOutput(cacheOutputs, "evidence_index", "evidence-index.jsonl");
    assertCacheOutput(cacheOutputs, "endpoints_markdown", "endpoints.md");
    assertCacheOutput(cacheOutputs, "agent_guide_markdown", "agent-guide.md");
    assertCacheOutput(cacheOutputs, "agent_profile_manifest", "agent-profiles/manifest.json");
    assertCacheOutput(cacheOutputs, "agent_profile_markdown", "agent-profiles/codex.md");
    assertCacheOutput(cacheOutputs, "agent_profile_markdown", "agent-profiles/claude.md");
    assertCacheOutput(cacheOutputs, "agent_profile_markdown", "agent-profiles/cursor.md");
    assertCacheOutput(cacheOutputs, "agent_profile_markdown", "agent-profiles/generic.md");
  }

  @Test
  void scanSummarizesReportedDiagnostics() throws Exception {
    CliResult result = runCliWithGenerator(
        (repositoryRoot, outputDirectory, scanConfiguration, agentProfiles) ->
            new SpringMvcEndpointOutputGenerator.Result(true, 0, 0, 0, 0, 0, 0, 2),
        "scan",
        tempDir.toString());

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertTrue(result.stdout().contains("Diagnostics: 2 item(s).")),
        () -> assertFalse(result.stdout().contains(tempDir.toString())));
  }

  @Test
  void scanMavenStyleSourceRootGeneratesProjectMapEndpointsEvidenceIndexAndAgentGuide()
      throws Exception {
    Path projectPath = tempDir.resolve("fixture-project");
    copyDirectory(fixtureRoot(), projectPath);

    CliResult result = runCli("scan", projectPath.toString());
    Path outputDirectory = projectPath.resolve(".project-memory");
    String projectMap = Files.readString(outputDirectory.resolve("project-map.json"));
    String endpoints = Files.readString(outputDirectory.resolve("endpoints.md"));
    String evidenceIndex = Files.readString(outputDirectory.resolve("evidence-index.jsonl"));
    String agentGuide = Files.readString(outputDirectory.resolve("agent-guide.md"));

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertTrue(result.stdout().contains("Generated project-map.json")),
        () -> assertTrue(result.stdout().contains("Generated endpoints.md")),
        () -> assertTrue(result.stdout().contains("Generated evidence-index.jsonl")),
        () -> assertTrue(result.stdout().contains("Generated agent-guide.md")),
        () -> assertTrue(result.stdout().contains("Diagnostics: none.")),
        () -> assertFalse(result.stdout().contains(projectPath.toString())),
        () -> assertTrue(projectMap.contains("\"schema_version\": \"1.0\"")),
        () -> assertTrue(projectMap.contains("\"spring_application_surface\": {")),
        () -> assertTrue(projectMap.contains("\"modules\": {")),
        () -> assertTrue(projectMap.contains("\"api_surface\": {")),
        () -> assertTrue(projectMap.contains("\"module_id\": \"module:.\"")),
        () -> assertTrue(projectMap.contains("\"source_roots\": [")),
        () -> assertTrue(projectMap.contains("\"src/main/java\"")),
        () -> assertTrue(projectMap.contains("\"endpoints\": [")),
        () -> assertTrue(projectMap.contains("\"mapping_source\": {")),
        () -> assertTrue(projectMap.contains("\"entities\": {")),
        () -> assertTrue(projectMap.contains("\"tests\": {")),
        () -> assertTrue(projectMap.contains("\"analysis_status\": \"not_detected\"")),
        () -> assertTrue(projectMap.contains("\"controller_class\": \"com.example.web.SimpleRestController\"")),
        () -> assertTrue(projectMap.contains("\"kind\": \"source_visible_interface_method\"")),
        () -> assertTrue(projectMap.contains("\"paths\": [\n        \"/interface/orders/{id}\"")),
        () -> assertTrue(endpoints.contains("# Endpoints")),
        () -> assertTrue(endpoints.contains("## GET /health")),
        () -> assertTrue(endpoints.contains("## GET /interface/orders/{id}")),
        () -> assertTrue(endpoints.contains("- Controller: `com.example.web.SimpleRestController`")),
        () -> assertTrue(endpoints.contains("- Handler: `health`")),
        () -> assertTrue(endpoints.contains("- Mapping source: `direct_handler_method`")),
        () -> assertTrue(endpoints.contains("- Response: `String`")),
        () -> assertTrue(endpoints.contains("- Evidence: `ev:")),
        () -> assertTrue(evidenceIndex.contains("\"source_type\":\"annotation\"")),
        () -> assertTrue(evidenceIndex.contains("\"source_type\":\"code_symbol\"")),
        () -> assertTrue(evidenceIndex.contains(
            "\"path\":\"src/main/java/com/example/web/SimpleRestController.java\"")),
        () -> assertTrue(evidenceIndex.contains("\"symbol_name\":\"@GetMapping\"")),
        () -> assertTrue(agentGuide.contains("# Agent Guide")),
        () -> assertTrue(agentGuide.contains("## Detected Project Layout")),
        () -> assertTrue(agentGuide.contains("## Detected Spring MVC Endpoints")),
        () -> assertTrue(agentGuide.contains("Generated deterministically from `project-map.json`")));
  }

  @Test
  void repeatedScanRewritesExistingGeneratedOutputFile() throws Exception {
    Path projectPath = tempDir.resolve("fixture-project");
    copyDirectory(fixtureRoot(), projectPath);

    assertEquals(0, runCli("scan", projectPath.toString()).exitCode());
    Path projectMap = projectPath.resolve(".project-memory/project-map.json");
    Files.writeString(projectMap, "stale generated content");

    CliResult result = runCli("scan", projectPath.toString());
    String rewrittenProjectMap = Files.readString(projectMap);

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertTrue(result.stdout().contains("Generated project-map.json")),
        () -> assertTrue(rewrittenProjectMap.contains("\"schema_version\": \"1.0\"")),
        () -> assertFalse(rewrittenProjectMap.contains("stale generated content")));
  }

  @Test
  void scanDiscoversRootConfigAndDisablesLocalMarkdownWithoutDocumentEvidence()
      throws Exception {
    Files.writeString(tempDir.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);
    Files.writeString(tempDir.resolve("README.md"), "# Should not be discovered\n");
    Files.writeString(tempDir.resolve("agent-project-memory.yml"), """
        version: 1
        features:
          local_markdown: false
        documents:
          include:
            - docs/*.md
          exclude:
            - docs/private/**
        """);

    CliResult result = runCli("scan", tempDir.toString());
    Path outputDirectory = tempDir.resolve(".project-memory");
    String projectMap = Files.readString(outputDirectory.resolve("project-map.json"));
    String evidenceIndex = Files.readString(outputDirectory.resolve("evidence-index.jsonl"));

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertTrue(projectMap.contains("\"schema_version\": \"1.0\"")),
        () -> assertTrue(projectMap.contains("\"source\": \"config_file\"")),
        () -> assertTrue(projectMap.contains("\"config_file_path\": \"agent-project-memory.yml\"")),
        () -> assertTrue(projectMap.contains("\"config_file_status\": \"applied\"")),
        () -> assertTrue(projectMap.contains("\"local_markdown\": {\n"
            + "        \"enabled\": false,\n"
            + "        \"source\": \"config_file\"")),
        () -> assertTrue(projectMap.contains("\"user_includes_applied\": false")),
        () -> assertTrue(projectMap.contains("\"user_include_count\": 0")),
        () -> assertTrue(projectMap.contains("\"user_excludes_applied\": false")),
        () -> assertTrue(projectMap.contains("\"user_exclude_count\": 0")),
        () -> assertTrue(projectMap.contains("\"documents\": {\n"
            + "    \"analysis_status\": \"not_analyzed\"")),
        () -> assertTrue(projectMap.contains("\"reconciliation\": {\n"
            + "      \"analysis_status\": \"not_analyzed\"")),
        () -> assertFalse(projectMap.contains("Should not be discovered")),
        () -> assertFalse(evidenceIndex.contains("\"source_type\":\"document\"")));
  }

  @Test
  void scanAppliesExplicitConfigIncludeExcludeWithoutSerializingPatterns()
      throws Exception {
    Files.writeString(tempDir.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);
    Files.createDirectories(tempDir.resolve("config"));
    Files.createDirectories(tempDir.resolve("docs"));
    Files.createDirectories(tempDir.resolve("notes"));
    Files.writeString(tempDir.resolve("docs/public.md"), "# Public\n");
    Files.writeString(tempDir.resolve("docs/secret-token-plan.md"), "# FAKE_SECRET_TOKEN_MARKDOWN\n");
    Files.writeString(tempDir.resolve("notes/visible.md"), "# Visible\n");
    Files.writeString(tempDir.resolve("config/custom.yml"), """
        version: 1
        features:
          local_markdown: true
        documents:
          include:
            - notes/*.md
          exclude:
            - docs/secret-token-*.md
        """);

    CliResult result = runCli("scan", tempDir.toString(), "--config", "config/custom.yml");
    Path outputDirectory = tempDir.resolve(".project-memory");
    String projectMap = Files.readString(outputDirectory.resolve("project-map.json"));
    String evidenceIndex = Files.readString(outputDirectory.resolve("evidence-index.jsonl"));
    String agentGuide = Files.readString(outputDirectory.resolve("agent-guide.md"));

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertTrue(projectMap.contains("\"config_file_path\": \"config/custom.yml\"")),
        () -> assertTrue(projectMap.contains("\"config_file_status\": \"explicit\"")),
        () -> assertTrue(projectMap.contains("\"user_includes_applied\": true")),
        () -> assertTrue(projectMap.contains("\"user_include_count\": 1")),
        () -> assertTrue(projectMap.contains("\"user_excludes_applied\": true")),
        () -> assertTrue(projectMap.contains("\"user_exclude_count\": 1")),
        () -> assertTrue(projectMap.contains("\"path\": \"docs/public.md\"")),
        () -> assertTrue(projectMap.contains("\"path\": \"notes/visible.md\"")),
        () -> assertTrue(projectMap.contains("\"discovery_source\": \"explicit_include\"")),
        () -> assertFalse(projectMap.contains("notes/*.md")),
        () -> assertFalse(projectMap.contains("secret-token")),
        () -> assertFalse(projectMap.contains("FAKE_SECRET_TOKEN_MARKDOWN")),
        () -> assertFalse(evidenceIndex.contains("notes/*.md")),
        () -> assertFalse(evidenceIndex.contains("secret-token")),
        () -> assertFalse(evidenceIndex.contains("FAKE_SECRET_TOKEN_MARKDOWN")),
        () -> assertFalse(agentGuide.contains("notes/*.md")),
        () -> assertFalse(agentGuide.contains("secret-token")),
        () -> assertFalse(agentGuide.contains("FAKE_SECRET_TOKEN_MARKDOWN")));
  }

  @Test
  void scanRejectsInvalidConfigBeforeCreatingOutputDirectory() throws Exception {
    Files.writeString(tempDir.resolve("agent-project-memory.yml"), """
        version: 1
        features:
          generated_sources: true
        """);

    CliResult result = runCli("scan", tempDir.toString());

    assertAll(
        () -> assertEquals(4, result.exitCode()),
        () -> assertTrue(result.stderr().contains("reserved scan modes cannot be enabled")),
        () -> assertFalse(result.stderr().contains("true")),
        () -> assertFalse(Files.exists(tempDir.resolve(".project-memory"))));
  }

  @Test
  void scanRejectsUnsafeAdapterConfigBeforeCreatingOutputDirectoryWithoutLeakingValues()
      throws Exception {
    Path rawAbsoluteImportPath = tempDir.resolve("exports/token=FAKE_ADAPTER_CLI_SECRET.json");
    Files.writeString(tempDir.resolve("agent-project-memory.yml"), """
        version: 1
        adapters:
          local_structured_import:
            enabled: true
            path: %s
        """.formatted(rawAbsoluteImportPath));

    CliResult result = runCli("scan", tempDir.toString());

    assertAll(
        () -> assertEquals(4, result.exitCode()),
        () -> assertTrue(result.stderr().contains("adapter import path must be repository-relative")),
        () -> assertFalse(result.stderr().contains(rawAbsoluteImportPath.toString())),
        () -> assertFalse(result.stderr().contains("FAKE_ADAPTER_CLI_SECRET")),
        () -> assertFalse(result.stderr().contains(tempDir.toString())),
        () -> assertFalse(Files.exists(tempDir.resolve(".project-memory"))));
  }

  @Test
  void scanReadsLocalStructuredImportIntoSourceRegistryWithoutPromotingFactsOrRawBodies()
      throws Exception {
    Files.writeString(tempDir.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);
    Files.createDirectories(tempDir.resolve("exports"));
    Files.writeString(tempDir.resolve("exports/issues.json"), """
        {
          "format": "agent-project-memory.local_structured_import.v1",
          "records": [
            {
              "source_type": "local_export",
              "source_identity": "issues/PM-201",
              "title": "Imported issue",
              "body": "FAKE_ADAPTER_EXPORT_SECRET raw connector body",
              "status": "current"
            }
          ]
        }
        """);
    Files.writeString(tempDir.resolve("agent-project-memory.yml"), """
        version: 1
        adapters:
          local_structured_import:
            enabled: true
            path: exports/issues.json
        """);

    CliResult result = runCli("scan", tempDir.toString());
    Path outputDirectory = tempDir.resolve(".project-memory");
    JsonNode projectMap = JSON.readTree(Files.readString(outputDirectory.resolve("project-map.json")));
    JsonNode sourceRegistry = JSON.readTree(Files.readString(outputDirectory.resolve("source-registry.json")));
    JsonNode projectGraph = JSON.readTree(Files.readString(outputDirectory.resolve("project-graph.json")));
    String evidenceIndex = Files.readString(outputDirectory.resolve("evidence-index.jsonl"));
    String agentGuide = Files.readString(outputDirectory.resolve("agent-guide.md"));
    String sourceRegistryJson = Files.readString(outputDirectory.resolve("source-registry.json"));
    String generatedSurfaces = String.join(
        "\n",
        projectMap.toString(),
        sourceRegistryJson,
        evidenceIndex,
        agentGuide);
    JsonNode adapterItem = projectMap.path("adapter_context").path("items").get(0);

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertTrue(result.stdout().contains("Generated source-registry.json with 1 source document(s)")),
        () -> assertEquals("2.0", projectMap.path("schema_version").asText()),
        () -> assertEquals("2.0", projectGraph.path("project_map_schema_version").asText()),
        () -> assertTrue(projectMap.path("scan").path("features").path("adapters").path("enabled").asBoolean()),
        () -> assertEquals(
            "local_import_read",
            projectMap.path("scan").path("features").path("adapters").path("status").asText()),
        () -> assertEquals(
            "provenance_backed_external_context",
            projectMap.path("adapter_context").path("context_kind").asText()),
        () -> assertEquals("source-registry.json", projectMap.path("adapter_context").path("source_registry").asText()),
        () -> assertEquals(1, projectMap.path("adapter_context").path("items").size()),
        () -> assertEquals("external_document_context", adapterItem.path("context_kind").asText()),
        () -> assertEquals("provenance_only", adapterItem.path("support_type").asText()),
        () -> assertEquals("low", adapterItem.path("confidence").asText()),
        () -> assertTrue(adapterItem.has("source_document_ids")),
        () -> assertTrue(adapterItem.has("provenance_ids")),
        () -> assertFalse(adapterItem.has("evidence_ids")),
        () -> assertEquals(0, projectMap.path("endpoints").size()),
        () -> assertEquals(0, projectMap.path("components").path("items").size()),
        () -> assertEquals(0, projectMap.path("entities").path("items").size()),
        () -> assertEquals(0, projectMap.path("tests").path("items").size()),
        () -> assertEquals("1.0", sourceRegistry.path("source_registry_schema_version").asText()),
        () -> assertEquals(1, sourceRegistry.path("adapter_runs").size()),
        () -> assertEquals(1, sourceRegistry.path("source_documents").size()),
        () -> assertEquals(1, sourceRegistry.path("provenance").size()),
        () -> assertEquals("local_export", sourceRegistry.path("source_documents").get(0).path("source_type").asText()),
        () -> assertEquals("issues/PM-201", sourceRegistry.path("source_documents").get(0)
            .path("source_identity").asText()),
        () -> assertEquals("issues/PM-201", adapterItem.path("source_identity").asText()),
        () -> assertEquals(
            "not_serialized",
            sourceRegistry.path("source_documents").get(0).path("content_status").asText()),
        () -> assertTrue(sourceRegistry.path("provenance").get(0).path("trust_boundary_labels").toString()
            .contains("not_code_evidence")),
        () -> assertEquals("disabled", sourceRegistry.path("adapter_runs").get(0).path("network_access").asText()),
        () -> assertEquals("disabled", sourceRegistry.path("provenance").get(0).path("network_access").asText()),
        () -> assertFalse(evidenceIndex.contains("local_export")),
        () -> assertFalse(evidenceIndex.contains("source-provenance")),
        () -> assertFalse(generatedSurfaces.contains("exports/issues.json")),
        () -> assertFalse(generatedSurfaces.contains("FAKE_ADAPTER_EXPORT_SECRET")),
        () -> assertFalse(generatedSurfaces.contains("raw connector body")),
        () -> assertFalse((result.stdout() + result.stderr()).contains("exports/issues.json")),
        () -> assertFalse((result.stdout() + result.stderr()).contains("FAKE_ADAPTER_EXPORT_SECRET")));
  }

  @Test
  void scanRejectsSensitiveLocalStructuredImportSourceIdentitiesWithoutOutputLeakage()
      throws Exception {
    Files.writeString(tempDir.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);
    Files.createDirectories(tempDir.resolve("exports"));
    Files.writeString(tempDir.resolve("exports/issues.json"), """
        {
          "format": "agent-project-memory.local_structured_import.v1",
          "records": [
            {
              "source_type": "local_export",
              "source_identity": "C:/Users/alice/.ssh/id_rsa",
              "title": "Rejected drive path",
              "body": "FAKE_REJECTED_DRIVE_BODY",
              "status": "current"
            },
            {
              "source_type": "local_export",
              "source_identity": "file:/Users/alice/.ssh/id_rsa",
              "title": "Rejected file path",
              "body": "FAKE_REJECTED_FILE_BODY",
              "status": "current"
            },
            {
              "source_type": "local_export",
              "source_identity": "token/FAKE_REJECTED_TOKEN",
              "title": "Rejected token path",
              "body": "FAKE_REJECTED_TOKEN_BODY",
              "status": "current"
            },
            {
              "source_type": "local_export",
              "source_identity": "services/orders/issues/PM-401",
              "title": "Accepted issue",
              "body": "Accepted imported body",
              "status": "current"
            }
          ]
        }
        """);
    Files.writeString(tempDir.resolve("agent-project-memory.yml"), """
        version: 1
        adapters:
          local_structured_import:
            enabled: true
            path: exports/issues.json
        """);

    CliResult result = runCli("scan", tempDir.toString());
    Path outputDirectory = tempDir.resolve(".project-memory");
    JsonNode projectMap = JSON.readTree(Files.readString(outputDirectory.resolve("project-map.json")));
    JsonNode sourceRegistry = JSON.readTree(Files.readString(outputDirectory.resolve("source-registry.json")));
    String generatedSurfaces = String.join(
        "\n",
        Files.readString(outputDirectory.resolve("project-map.json")),
        Files.readString(outputDirectory.resolve("source-registry.json")),
        Files.readString(outputDirectory.resolve("project-graph.json")),
        Files.readString(outputDirectory.resolve("evidence-index.jsonl")),
        Files.readString(outputDirectory.resolve("endpoints.md")),
        Files.readString(outputDirectory.resolve("agent-guide.md")),
        result.stdout(),
        result.stderr());
    JsonNode adapterItem = projectMap.path("adapter_context").path("items").get(0);

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertTrue(result.stdout()
            .contains("Generated source-registry.json with 1 source document(s) and 3 adapter diagnostic(s).")),
        () -> assertEquals("2.0", projectMap.path("schema_version").asText()),
        () -> assertEquals(
            "local_import_read_with_partial_rejections",
            projectMap.path("scan").path("features").path("adapters").path("status").asText()),
        () -> assertEquals(3, projectMap.path("adapter_context").path("diagnostic_count").asInt()),
        () -> assertEquals(1, projectMap.path("adapter_context").path("items").size()),
        () -> assertEquals("services/orders/issues/PM-401", adapterItem.path("source_identity").asText()),
        () -> assertFalse(adapterItem.has("evidence_ids")),
        () -> assertEquals(1, sourceRegistry.path("source_documents").size()),
        () -> assertEquals(1, sourceRegistry.path("provenance").size()),
        () -> assertEquals(3, sourceRegistry.path("diagnostics").path("items").size()),
        () -> assertEquals(
            1,
            sourceRegistry.path("adapter_runs").get(0).path("accepted_count").asInt()),
        () -> assertEquals(
            3,
            sourceRegistry.path("adapter_runs").get(0).path("rejected_count").asInt()),
        () -> assertEquals(
            3,
            sourceRegistry.path("adapter_runs").get(0).path("diagnostic_count").asInt()),
        () -> assertFalse(generatedSurfaces.contains("C:/Users/alice")),
        () -> assertFalse(generatedSurfaces.contains("file:/Users/alice")),
        () -> assertFalse(generatedSurfaces.contains("token/FAKE_REJECTED_TOKEN")),
        () -> assertFalse(generatedSurfaces.contains("FAKE_REJECTED_DRIVE_BODY")),
        () -> assertFalse(generatedSurfaces.contains("FAKE_REJECTED_FILE_BODY")),
        () -> assertFalse(generatedSurfaces.contains("FAKE_REJECTED_TOKEN_BODY")),
        () -> assertFalse(generatedSurfaces.contains("Rejected drive path")),
        () -> assertFalse(generatedSurfaces.contains("Rejected file path")),
        () -> assertFalse(generatedSurfaces.contains("Rejected token path")),
        () -> assertTrue(generatedSurfaces.contains("services/orders/issues/PM-401")));
  }

  @Test
  void scanLocalStructuredImportWithAgentProfileManifestUsesAdapterSchemaVersion()
      throws Exception {
    Files.writeString(tempDir.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);
    Files.createDirectories(tempDir.resolve("exports"));
    Files.writeString(tempDir.resolve("exports/issues.json"), """
        {
          "format": "agent-project-memory.local_structured_import.v1",
          "records": [
            {
              "source_type": "local_export",
              "source_identity": "issues/PM-202",
              "title": "Imported issue",
              "body": "Imported body",
              "status": "current"
            }
          ]
        }
        """);
    Files.writeString(tempDir.resolve("agent-project-memory.yml"), """
        version: 1
        adapters:
          local_structured_import:
            enabled: true
            path: exports/issues.json
        """);

    CliResult result = runCli(
        "scan",
        tempDir.toString(),
        "--agent-profile",
        "codex");
    Path outputDirectory = tempDir.resolve(".project-memory");
    JsonNode projectMap = JSON.readTree(Files.readString(outputDirectory.resolve("project-map.json")));
    JsonNode manifest = JSON.readTree(
        Files.readString(outputDirectory.resolve("agent-profiles/manifest.json")));

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertEquals("2.0", projectMap.path("schema_version").asText()),
        () -> assertEquals("2.0", manifest.path("project_map_schema_version").asText()),
        () -> assertTrue(Files.exists(outputDirectory.resolve("source-registry.json"))),
        () -> assertTrue(Files.exists(outputDirectory.resolve("agent-profiles/codex.md"))));
  }

  @Test
  void scanIncrementalWithLocalStructuredImportSkipsCacheMetadataContainingImportPath()
      throws Exception {
    Files.writeString(tempDir.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);
    Files.createDirectories(tempDir.resolve("exports"));
    Files.writeString(tempDir.resolve("exports/issues.json"), """
        {
          "format": "agent-project-memory.local_structured_import.v1",
          "records": [
            {
              "source_type": "local_export",
              "source_identity": "issues/PM-201",
              "title": "Imported issue",
              "body": "Imported body",
              "status": "current"
            }
          ]
        }
        """);
    Files.writeString(tempDir.resolve("agent-project-memory.yml"), """
        version: 1
        adapters:
          local_structured_import:
            enabled: true
            path: exports/issues.json
        """);

    CliResult result = runCli("scan", tempDir.toString(), "--incremental");
    Path outputDirectory = tempDir.resolve(".project-memory");

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertTrue(result.stdout().contains("Generated source-registry.json with 1 source document(s)")),
        () -> assertTrue(result.stdout().contains("Skipped incremental cache metadata refresh.")),
        () -> assertFalse(result.stdout().contains("Updated incremental cache metadata.")),
        () -> assertFalse(result.stdout().contains("Reused incremental cache output set.")),
        () -> assertTrue(Files.exists(outputDirectory.resolve("source-registry.json"))),
        () -> assertFalse(Files.exists(outputDirectory.resolve("cache/v1/manifest.json"))),
        () -> assertFalse(Files.exists(outputDirectory.resolve("cache/v1/inputs.jsonl"))),
        () -> assertFalse(Files.exists(outputDirectory.resolve("cache/v1/outputs.jsonl"))));
  }

  @Test
  void scanMalformedJavaReturnsBoundedDiagnosticAndGeneratedContractOutputFiles()
      throws Exception {
    Path javaFile = tempDir.resolve("src/main/java/com/example/BrokenController.java");
    Files.createDirectories(javaFile.getParent());
    Files.writeString(javaFile, """
        package com.example;

        public class BrokenController {
          public void broken( {
          }
        }
        """);

    CliResult result = runCli("scan", tempDir.toString());
    Path outputDirectory = tempDir.resolve(".project-memory");
    String projectMap = Files.readString(outputDirectory.resolve("project-map.json"));

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertTrue(result.stderr().isEmpty()),
        () -> assertTrue(projectMap.contains("\"code\": \"java_source_parse_error\"")),
        () -> assertTrue(projectMap.contains(
            "\"path\": \"src/main/java/com/example/BrokenController.java\"")),
        () -> assertTrue(projectMap.contains("\"category\": \"java_source\"")),
        () -> assertTrue(projectMap.contains("\"endpoints\": []")),
        () -> assertFalse(result.stderr().contains(tempDir.toString())),
        () -> assertFalse(result.stderr().contains("ParseProblemException")),
        () -> assertFalse(result.stderr().contains("com.github.javaparser")),
        () -> assertFalse(result.stderr().contains("\tat ")),
        () -> assertTrue(Files.exists(outputDirectory.resolve("project-map.json"))),
        () -> assertTrue(Files.exists(outputDirectory.resolve("project-graph.json"))),
        () -> assertTrue(Files.exists(outputDirectory.resolve("evidence-index.jsonl"))),
        () -> assertTrue(Files.exists(outputDirectory.resolve("endpoints.md"))),
        () -> assertTrue(Files.exists(outputDirectory.resolve("agent-guide.md"))));
  }

  @Test
  void scanSymlinkedJavaReturnsBoundedDiagnosticAndGeneratedContractOutputFiles()
      throws Exception {
    Path target = tempDir.resolve("linked-target/RealSource.java");
    Files.createDirectories(target.getParent());
    Files.writeString(target, "package com.example;\nclass RealSource {}\n");
    Path javaLink = tempDir.resolve("src/main/java/com/example/LinkedSource.java");
    Files.createDirectories(javaLink.getParent());
    createSymbolicLink(javaLink, target);

    CliResult result = runCli("scan", tempDir.toString());
    Path outputDirectory = tempDir.resolve(".project-memory");
    String projectMap = Files.readString(outputDirectory.resolve("project-map.json"));
    String evidenceIndex = Files.readString(outputDirectory.resolve("evidence-index.jsonl"));

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertTrue(result.stderr().isEmpty()),
        () -> assertTrue(projectMap.contains("\"code\": \"java_source_file_read_skipped\"")),
        () -> assertTrue(projectMap.contains(
            "\"path\": \"src/main/java/com/example/LinkedSource.java\"")),
        () -> assertTrue(projectMap.contains("\"category\": \"java_source\"")),
        () -> assertFalse(evidenceIndex.contains("LinkedSource.java")),
        () -> assertFalse(result.stderr().contains(tempDir.toString())),
        () -> assertFalse(result.stderr().contains("\tat ")),
        () -> assertTrue(Files.exists(outputDirectory.resolve("project-map.json"))),
        () -> assertTrue(Files.exists(outputDirectory.resolve("project-graph.json"))),
        () -> assertTrue(Files.exists(outputDirectory.resolve("evidence-index.jsonl"))),
        () -> assertTrue(Files.exists(outputDirectory.resolve("endpoints.md"))),
        () -> assertTrue(Files.exists(outputDirectory.resolve("agent-guide.md"))));
  }

  @Test
  void scanRejectsProjectMemorySymlinkAndDoesNotWriteOutsideScanRoot() throws Exception {
    Path projectPath = tempDir.resolve("fixture-project");
    copyDirectory(fixtureRoot(), projectPath);
    Path outsideOutputDirectory = tempDir.resolve("outside-output");
    Files.createDirectories(outsideOutputDirectory);
    createSymbolicLink(projectPath.resolve(".project-memory"), outsideOutputDirectory);

    CliResult result = runCli("scan", projectPath.toString());

    assertAll(
        () -> assertEquals(3, result.exitCode()),
        () -> assertTrue(result.stderr().contains("Output path must not be a symbolic link")),
        () -> assertFalse(result.stderr().contains(projectPath.toString())),
        () -> assertFalse(Files.exists(outsideOutputDirectory.resolve("project-map.json"))),
        () -> assertFalse(Files.exists(outsideOutputDirectory.resolve("project-graph.json"))),
        () -> assertFalse(Files.exists(outsideOutputDirectory.resolve("evidence-index.jsonl"))),
        () -> assertFalse(Files.exists(outsideOutputDirectory.resolve("endpoints.md"))),
        () -> assertFalse(Files.exists(outsideOutputDirectory.resolve("agent-guide.md"))));
  }

  @Test
  void scanRejectsGeneratedOutputFileSymlinkAndDoesNotWriteOutsideScanRoot() throws Exception {
    Path projectPath = tempDir.resolve("fixture-project");
    copyDirectory(fixtureRoot(), projectPath);
    Path outputDirectory = projectPath.resolve(".project-memory");
    Files.createDirectories(outputDirectory);
    Path outsideOutputFile = tempDir.resolve("outside-endpoints.md");
    Files.writeString(outsideOutputFile, "outside content");
    createSymbolicLink(outputDirectory.resolve("endpoints.md"), outsideOutputFile);

    CliResult result = runCli("scan", projectPath.toString());

    assertAll(
        () -> assertEquals(3, result.exitCode()),
        () -> assertTrue(result.stderr().contains("Output file must not be a symbolic link")),
        () -> assertFalse(result.stderr().contains(projectPath.toString())),
        () -> assertEquals("outside content", Files.readString(outsideOutputFile)),
        () -> assertFalse(Files.exists(outputDirectory.resolve("project-map.json"))),
        () -> assertFalse(Files.exists(outputDirectory.resolve("project-graph.json"))),
        () -> assertFalse(Files.exists(outputDirectory.resolve("evidence-index.jsonl"))),
        () -> assertFalse(Files.exists(outputDirectory.resolve("agent-guide.md"))));
  }

  @Test
  void scanRejectsGeneratedOutputFileSymlinkThroughSymlinkedRootWithoutCanonicalPath()
      throws Exception {
    Path realProjectPath = tempDir.resolve("real-fixture-project");
    copyDirectory(fixtureRoot(), realProjectPath);
    Path symlinkedProjectPath = tempDir.resolve("scan-root-link");
    createSymbolicLink(symlinkedProjectPath, realProjectPath);
    Path outputDirectory = realProjectPath.resolve(".project-memory");
    Files.createDirectories(outputDirectory);
    Path outsideOutputFile = tempDir.resolve("outside-endpoints.md");
    Files.writeString(outsideOutputFile, "outside content");
    createSymbolicLink(outputDirectory.resolve("endpoints.md"), outsideOutputFile);
    Path canonicalRealProjectPath = realProjectPath.toRealPath();

    CliResult result = runCli("scan", symlinkedProjectPath.toString());

    assertAll(
        () -> assertEquals(3, result.exitCode()),
        () -> assertTrue(result.stderr().contains("Output file must not be a symbolic link")),
        () -> assertFalse(result.stderr().contains(symlinkedProjectPath.toString())),
        () -> assertFalse(result.stderr().contains(realProjectPath.toString())),
        () -> assertFalse(result.stderr().contains(canonicalRealProjectPath.toString())),
        () -> assertEquals("outside content", Files.readString(outsideOutputFile)),
        () -> assertFalse(Files.exists(outputDirectory.resolve("project-map.json"))),
        () -> assertFalse(Files.exists(outputDirectory.resolve("project-graph.json"))),
        () -> assertFalse(Files.exists(outputDirectory.resolve("evidence-index.jsonl"))),
        () -> assertFalse(Files.exists(outputDirectory.resolve("agent-guide.md"))));
  }

  @Test
  void scanRejectsGeneratedOutputFileHardLinkAndDoesNotWriteOutsideAlias() throws Exception {
    Path projectPath = tempDir.resolve("fixture-project");
    copyDirectory(fixtureRoot(), projectPath);
    Path outputDirectory = projectPath.resolve(".project-memory");
    Files.createDirectories(outputDirectory);
    Path outsideOutputFile = tempDir.resolve("outside-project-map.json");
    Files.writeString(outsideOutputFile, "outside content");
    createHardLink(outputDirectory.resolve("project-map.json"), outsideOutputFile);

    CliResult result = runCli("scan", projectPath.toString());

    assertAll(
        () -> assertEquals(3, result.exitCode()),
        () -> assertTrue(result.stderr().contains("Output file must not have multiple hard links")),
        () -> assertFalse(result.stderr().contains(projectPath.toString())),
        () -> assertEquals("outside content", Files.readString(outsideOutputFile)),
        () -> assertEquals(
            "outside content",
            Files.readString(outputDirectory.resolve("project-map.json"))),
        () -> assertFalse(Files.exists(outputDirectory.resolve("project-graph.json"))),
        () -> assertFalse(Files.exists(outputDirectory.resolve("endpoints.md"))),
        () -> assertFalse(Files.exists(outputDirectory.resolve("evidence-index.jsonl"))),
        () -> assertFalse(Files.exists(outputDirectory.resolve("agent-guide.md"))));
  }

  @Test
  void scanRejectsAgentProfileDirectorySymlinkAndDoesNotWriteOutsideScanRoot()
      throws Exception {
    Path projectPath = tempDir.resolve("fixture-project");
    copyDirectory(fixtureRoot(), projectPath);
    Path outputDirectory = projectPath.resolve(".project-memory");
    Files.createDirectories(outputDirectory);
    Path outsideProfileDirectory = tempDir.resolve("outside-profiles");
    Files.createDirectories(outsideProfileDirectory);
    createSymbolicLink(outputDirectory.resolve("agent-profiles"), outsideProfileDirectory);

    CliResult result = runCli(
        "scan",
        projectPath.toString(),
        "--agent-profile",
        "codex");

    assertAll(
        () -> assertEquals(3, result.exitCode()),
        () -> assertTrue(result.stderr().contains("Output directory must not be a symbolic link")),
        () -> assertFalse(result.stderr().contains(projectPath.toString())),
        () -> assertFalse(Files.exists(outsideProfileDirectory.resolve("manifest.json"))),
        () -> assertFalse(Files.exists(outsideProfileDirectory.resolve("codex.md"))),
        () -> assertFalse(Files.exists(outputDirectory.resolve("project-map.json"))),
        () -> assertFalse(Files.exists(outputDirectory.resolve("project-graph.json"))),
        () -> assertFalse(Files.exists(outputDirectory.resolve("endpoints.md"))),
        () -> assertFalse(Files.exists(outputDirectory.resolve("evidence-index.jsonl"))),
        () -> assertFalse(Files.exists(outputDirectory.resolve("agent-guide.md"))));
  }

  @Test
  void scanRejectsAgentProfileFileSymlinkAndDoesNotWriteOutsideScanRoot() throws Exception {
    Path projectPath = tempDir.resolve("fixture-project");
    copyDirectory(fixtureRoot(), projectPath);
    Path outputDirectory = projectPath.resolve(".project-memory");
    Path profileDirectory = outputDirectory.resolve("agent-profiles");
    Files.createDirectories(profileDirectory);
    Path outsideProfileFile = tempDir.resolve("outside-codex.md");
    Files.writeString(outsideProfileFile, "outside content");
    createSymbolicLink(profileDirectory.resolve("codex.md"), outsideProfileFile);

    CliResult result = runCli(
        "scan",
        projectPath.toString(),
        "--agent-profile",
        "codex");

    assertAll(
        () -> assertEquals(3, result.exitCode()),
        () -> assertTrue(result.stderr().contains("Output file must not be a symbolic link")),
        () -> assertFalse(result.stderr().contains(projectPath.toString())),
        () -> assertEquals("outside content", Files.readString(outsideProfileFile)),
        () -> assertFalse(Files.exists(outputDirectory.resolve("project-map.json"))),
        () -> assertFalse(Files.exists(outputDirectory.resolve("project-graph.json"))),
        () -> assertFalse(Files.exists(outputDirectory.resolve("endpoints.md"))),
        () -> assertFalse(Files.exists(outputDirectory.resolve("evidence-index.jsonl"))),
        () -> assertFalse(Files.exists(outputDirectory.resolve("agent-guide.md"))),
        () -> assertFalse(Files.exists(profileDirectory.resolve("manifest.json"))));
  }

  @Test
  void scanReturnsNonZeroWhenProjectMemoryPathIsAFile() throws Exception {
    Path conflictingOutputPath = tempDir.resolve(".project-memory");
    Files.writeString(conflictingOutputPath, "not a directory");

    CliResult result = runCli("scan", tempDir.toString());

    assertAll(
        () -> assertEquals(3, result.exitCode()),
        () -> assertTrue(result.stderr().contains("Output path exists and is not a directory")));
  }

  @Test
  void unexpectedRuntimeErrorReturnsInternalExitCodeWithoutDetails() throws Exception {
    Files.writeString(tempDir.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);

    CliResult result = runCliWithGenerator(
        (repositoryRoot, outputDirectory, scanConfiguration, agentProfiles) -> {
          throw new IllegalStateException("INTERNAL_SECRET_DETAIL");
        },
        "scan",
        tempDir.toString());

    assertAll(
        () -> assertEquals(1, result.exitCode()),
        () -> assertTrue(result.stderr().contains("Unexpected internal error.")),
        () -> assertFalse(result.stderr().contains("INTERNAL_SECRET_DETAIL")),
        () -> assertFalse(result.stderr().contains("\tat ")));
  }

  private CliResult runCli(String... args) {
    return runCliWithGenerator(new SpringMvcEndpointOutputGenerator()::generate, args);
  }

  private CliResult runCliWithGenerator(
      AgentProjectMemoryCli.ProjectMemoryOutputGenerator outputGenerator,
      String... args) {
    StringWriter stdout = new StringWriter();
    StringWriter stderr = new StringWriter();
    int exitCode = new AgentProjectMemoryCli(
        new PrintWriter(stdout, true),
        new PrintWriter(stderr, true),
        outputGenerator)
        .run(args);

    return new CliResult(exitCode, stdout.toString(), stderr.toString());
  }

  private List<JsonNode> jsonLines(Path path) throws Exception {
    List<JsonNode> records = new ArrayList<>();
    for (String line : Files.readString(path).lines().toList()) {
      if (!line.isBlank()) {
        records.add(JSON.readTree(line));
      }
    }
    return records;
  }

  private String evidenceId(Path evidenceIndex, String sourcePath, String symbolName)
      throws Exception {
    return jsonLines(evidenceIndex).stream()
        .filter(record -> sourcePath.equals(record.path("path").asText()))
        .filter(record -> symbolName.equals(record.path("symbol_name").asText()))
        .map(record -> record.path("id").asText())
        .findFirst()
        .orElseThrow(() -> new AssertionError(
            "Missing evidence record for " + sourcePath + " " + symbolName));
  }

  private String readAll(List<Path> files) throws Exception {
    StringBuilder joined = new StringBuilder();
    for (Path file : files) {
      joined.append("\n--- ").append(file.getFileName()).append(" ---\n");
      joined.append(Files.readString(file));
    }
    return joined.toString();
  }

  private void assertFakeNeedlesAbsent(String surface, String output, List<String> fakeNeedles) {
    for (String needle : fakeNeedles) {
      int index = output.indexOf(needle);
      String context = index < 0
          ? ""
          : output.substring(Math.max(0, index - 120), Math.min(output.length(), index + 120));
      assertEquals(
          -1,
          index,
          surface + " must not include fake sensitive value " + needle + "; context: " + context);
    }
  }

  private void assertIncrementalHit(CliResult result) {
    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertTrue(result.stdout().contains("Reused incremental cache output set.")),
        () -> assertFalse(result.stdout().contains("Generated project-map.json")),
        () -> assertFalse(result.stdout().contains("Updated incremental cache metadata.")),
        () -> assertTrue(result.stdout().contains("Diagnostics:")));
  }

  private void assertFullIncrementalRefresh(CliResult result) {
    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertFalse(result.stdout().contains("Reused incremental cache output set.")),
        () -> assertTrue(result.stdout().contains("Generated project-map.json")),
        () -> assertTrue(result.stdout().contains("Updated incremental cache metadata.")),
        () -> assertTrue(result.stdout().contains("Diagnostics:")));
  }

  private void assertCacheInput(
      List<JsonNode> inputs,
      String inputKind,
      String path,
      boolean contentFingerprintExpected) {
    JsonNode input = cacheInput(inputs, inputKind, path);
    assertEquals("1.0", input.path("cache_schema_version").asText());
    if (contentFingerprintExpected) {
      assertTrue(input.path("content_sha256").asText().startsWith("sha256:"));
      assertTrue(input.path("size_bytes").asLong() >= 0);
    } else {
      assertTrue(input.path("content_sha256").isNull());
      assertTrue(input.path("size_bytes").isNull());
    }
  }

  private JsonNode cacheInput(List<JsonNode> inputs, String inputKind, String path) {
    return inputs.stream()
        .filter(input -> inputKind.equals(input.path("input_kind").asText()))
        .filter(input -> path.equals(input.path("path").asText()))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Missing cache input " + inputKind + " " + path));
  }

  private boolean hasCacheInput(List<JsonNode> inputs, String inputKind, String path) {
    return inputs.stream()
        .filter(input -> inputKind.equals(input.path("input_kind").asText()))
        .anyMatch(input -> path.equals(input.path("path").asText()));
  }

  private void assertCacheOutput(List<JsonNode> outputs, String outputKind, String path) {
    JsonNode output = outputs.stream()
        .filter(record -> outputKind.equals(record.path("output_kind").asText()))
        .filter(record -> path.equals(record.path("path").asText()))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Missing cache output " + outputKind + " " + path));
    assertEquals("1.0", output.path("cache_schema_version").asText());
    assertTrue(output.path("content_sha256").asText().startsWith("sha256:"));
    assertTrue(output.path("size_bytes").asLong() >= 0);
  }

  private Path fixtureRoot() throws Exception {
    return Path.of(Objects.requireNonNull(
        getClass().getResource("/fixtures/springmvc-endpoints")).toURI());
  }

  private void copyDirectory(Path source, Path target) throws Exception {
    try (var paths = Files.walk(source)) {
      for (Path sourcePath : paths.toList()) {
        Path targetPath = target.resolve(source.relativize(sourcePath));
        if (Files.isDirectory(sourcePath)) {
          Files.createDirectories(targetPath);
        } else {
          Files.createDirectories(targetPath.getParent());
          Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
      }
    }
  }

  private void writeSymlinkedResourceConfig(Path repositoryRoot, Path target) throws Exception {
    Files.createDirectories(target.getParent());
    Files.writeString(target, """
        api-token: FAKE_SYMLINK_RESOURCE_CONFIG_SECRET
        """);
    Files.createDirectories(repositoryRoot.resolve("src/main/resources"));
    createSymbolicLink(repositoryRoot.resolve("src/main/resources/application.yml"), target);
  }

  private void writeResourceConfigBehindSymlinkedMainParent(Path repositoryRoot) throws Exception {
    Files.createDirectories(repositoryRoot.resolve("src/test/java"));
    Path target = repositoryRoot.resolve("real-main/resources/application.yml");
    Files.createDirectories(target.getParent());
    Files.writeString(target, """
        api-token: FAKE_SYMLINK_RESOURCE_CONFIG_SECRET
        """);
    createSymbolicLink(repositoryRoot.resolve("src/main"), repositoryRoot.resolve("real-main"));
  }

  private void writeFile(Path path, String content) throws Exception {
    Files.createDirectories(path.getParent());
    Files.writeString(path, content);
  }

  private void createSymbolicLink(Path link, Path target) throws Exception {
    try {
      Files.createSymbolicLink(link, target);
    } catch (IOException | SecurityException | UnsupportedOperationException ex) {
      assumeTrue(false, "Symbolic links are unavailable: " + ex.getMessage());
    }
  }

  private void createHardLink(Path link, Path target) throws Exception {
    try {
      Files.createLink(link, target);
    } catch (IOException | SecurityException | UnsupportedOperationException ex) {
      assumeTrue(false, "Hard links are unavailable: " + ex.getMessage());
    }
  }

  private int countOccurrences(String value, String needle) {
    int count = 0;
    int index = value.indexOf(needle);
    while (index >= 0) {
      count++;
      index = value.indexOf(needle, index + needle.length());
    }
    return count;
  }

  private record CliResult(int exitCode, String stdout, String stderr) {
  }
}
