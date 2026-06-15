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
        () -> assertFalse(Files.exists(outputDirectory.resolve("evidence-index.jsonl"))),
        () -> assertFalse(Files.exists(outputDirectory.resolve("endpoints.md"))),
        () -> assertFalse(Files.exists(outputDirectory.resolve("agent-guide.md"))),
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
    String evidenceIndex = Files.readString(outputDirectory.resolve("evidence-index.jsonl"));

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertTrue(Files.isDirectory(outputDirectory)),
        () -> assertTrue(result.stdout().contains("Generated project-map.json")),
        () -> assertTrue(result.stdout().contains("Diagnostics: none.")),
        () -> assertFalse(result.stdout().contains(tempDir.toString())),
        () -> assertTrue(Files.exists(outputDirectory.resolve("project-map.json"))),
        () -> assertTrue(Files.exists(outputDirectory.resolve("evidence-index.jsonl"))),
        () -> assertTrue(Files.exists(outputDirectory.resolve("endpoints.md"))),
        () -> assertTrue(Files.exists(outputDirectory.resolve("agent-guide.md"))),
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
        () -> assertEquals(evidenceIndex, Files.readString(outputDirectory.resolve("evidence-index.jsonl"))),
        () -> assertEquals(endpoints, Files.readString(outputDirectory.resolve("endpoints.md"))),
        () -> assertEquals(agentGuide, Files.readString(outputDirectory.resolve("agent-guide.md"))));
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
