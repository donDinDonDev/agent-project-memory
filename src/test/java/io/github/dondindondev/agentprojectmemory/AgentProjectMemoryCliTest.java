package io.github.dondindondev.agentprojectmemory;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class AgentProjectMemoryCliTest {
  @TempDir
  private Path tempDir;

  @Test
  void missingArgsReturnsNonZeroAndUsage() {
    CliResult result = runCli();

    assertAll(
        () -> assertNotEquals(0, result.exitCode()),
        () -> assertTrue(result.stderr().contains("Missing command.")),
        () -> assertTrue(result.stderr().contains(AgentProjectMemoryCli.USAGE)));
  }

  @Test
  void unknownCommandReturnsNonZeroAndUsage() {
    CliResult result = runCli("status");

    assertAll(
        () -> assertNotEquals(0, result.exitCode()),
        () -> assertTrue(result.stderr().contains("Unknown command: status")),
        () -> assertTrue(result.stderr().contains(AgentProjectMemoryCli.USAGE)));
  }

  @Test
  void scanMissingPathReturnsNonZero() {
    Path missingPath = tempDir.resolve("missing-project");

    CliResult result = runCli("scan", missingPath.toString());

    assertAll(
        () -> assertNotEquals(0, result.exitCode()),
        () -> assertTrue(result.stderr().contains("Path does not exist")));
  }

  @Test
  void scanFilePathReturnsNonZero() throws Exception {
    Path filePath = tempDir.resolve("project.txt");
    Files.writeString(filePath, "not a directory");

    CliResult result = runCli("scan", filePath.toString());

    assertAll(
        () -> assertNotEquals(0, result.exitCode()),
        () -> assertTrue(result.stderr().contains("Path is not a directory")));
  }

  @Test
  void scanExistingDirectoryCreatesProjectMemoryDirectory() {
    CliResult result = runCli("scan", tempDir.toString());

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertTrue(Files.isDirectory(tempDir.resolve(".project-memory"))));
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
        () -> assertFalse(Files.exists(outputDirectory.resolve("agent-guide.md"))));
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
        () -> assertTrue(Files.exists(outputDirectory.resolve("project-map.json"))),
        () -> assertTrue(Files.exists(outputDirectory.resolve("evidence-index.jsonl"))),
        () -> assertTrue(Files.exists(outputDirectory.resolve("endpoints.md"))),
        () -> assertTrue(Files.exists(outputDirectory.resolve("agent-guide.md"))),
        () -> assertTrue(projectMap.contains("\"schema_version\": \"0.9\"")),
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
        () -> assertTrue(projectMap.contains("\"schema_version\": \"0.9\"")),
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
        () -> assertTrue(rewrittenProjectMap.contains("\"schema_version\": \"0.9\"")),
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
        () -> assertTrue(projectMap.contains("\"schema_version\": \"0.9\"")),
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
  void scanMalformedJavaReturnsBoundedErrorWithoutGeneratedContractOutputFiles()
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

    assertAll(
        () -> assertNotEquals(0, result.exitCode()),
        () -> assertTrue(
            result.stderr().contains(
                "Could not generate project memory output: Could not parse Java source:")),
        () -> assertTrue(result.stderr().contains("BrokenController.java")),
        () -> assertTrue(result.stderr().contains("1 parse problem")),
        () -> assertTrue(result.stderr().contains("first problem at line")),
        () -> assertFalse(result.stderr().contains("ParseProblemException")),
        () -> assertFalse(result.stderr().contains("com.github.javaparser")),
        () -> assertFalse(result.stderr().contains("\tat ")),
        () -> assertFalse(Files.exists(outputDirectory.resolve("project-map.json"))),
        () -> assertFalse(Files.exists(outputDirectory.resolve("evidence-index.jsonl"))),
        () -> assertFalse(Files.exists(outputDirectory.resolve("endpoints.md"))),
        () -> assertFalse(Files.exists(outputDirectory.resolve("agent-guide.md"))));
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
        () -> assertNotEquals(0, result.exitCode()),
        () -> assertTrue(result.stderr().contains("Output path must not be a symbolic link")),
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
        () -> assertNotEquals(0, result.exitCode()),
        () -> assertTrue(result.stderr().contains("Output file must not be a symbolic link")),
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
        () -> assertNotEquals(0, result.exitCode()),
        () -> assertTrue(result.stderr().contains("Output file must not have multiple hard links")),
        () -> assertEquals("outside content", Files.readString(outsideOutputFile)),
        () -> assertEquals(
            "outside content",
            Files.readString(outputDirectory.resolve("project-map.json"))),
        () -> assertFalse(Files.exists(outputDirectory.resolve("endpoints.md"))),
        () -> assertFalse(Files.exists(outputDirectory.resolve("evidence-index.jsonl"))),
        () -> assertFalse(Files.exists(outputDirectory.resolve("agent-guide.md"))));
  }

  @Test
  void scanReturnsNonZeroWhenProjectMemoryPathIsAFile() throws Exception {
    Path conflictingOutputPath = tempDir.resolve(".project-memory");
    Files.writeString(conflictingOutputPath, "not a directory");

    CliResult result = runCli("scan", tempDir.toString());

    assertAll(
        () -> assertNotEquals(0, result.exitCode()),
        () -> assertTrue(result.stderr().contains("Output path exists and is not a directory")));
  }

  private CliResult runCli(String... args) {
    StringWriter stdout = new StringWriter();
    StringWriter stderr = new StringWriter();
    int exitCode = new AgentProjectMemoryCli(
        new PrintWriter(stdout, true),
        new PrintWriter(stderr, true))
        .run(args);

    return new CliResult(exitCode, stdout.toString(), stderr.toString());
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

  private record CliResult(int exitCode, String stdout, String stderr) {
  }
}
