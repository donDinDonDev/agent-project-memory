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

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertTrue(result.stdout().contains("Query artifact validation succeeded.")),
        () -> assertTrue(result.stdout().contains("Loaded project-map.json schema_version 1.0.")),
        () -> assertTrue(result.stdout().contains(
            "Loaded evidence-index.jsonl with 1 evidence record(s).")),
        () -> assertTrue(result.stdout().contains("No project-graph.json loaded.")),
        () -> assertTrue(result.stdout().contains("not implemented in this foundation")),
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
        () -> assertTrue(result.stdout().contains("Loaded project-graph.json graph_schema_version 1.0.")),
        () -> assertTrue(result.stderr().isEmpty()),
        () -> assertFalse(result.stdout().contains(artifactRoot.toString())));
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

    CliResult result = runCli("query", repositoryRoot.toString(), "list", "endpoints");

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
    Files.createDirectories(artifactRoot);
    Files.writeString(
        artifactRoot.resolve("project-map.json"),
        "{\"schema_version\":\"1.0\",\"project\":{\"modules\":{\"items\":[]}}}\n");
    Files.writeString(artifactRoot.resolve("evidence-index.jsonl"), evidenceRecord("ev:one"));
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

  private record CliResult(int exitCode, String stdout, String stderr) {
  }
}
