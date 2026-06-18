package io.github.dondindondev.agentprojectmemory.query;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class ProjectMemoryArtifactReaderTest {
  private final ProjectMemoryArtifactReader reader = new ProjectMemoryArtifactReader();

  @TempDir
  private Path tempDir;

  @Test
  void resolvesRepositoryRootAndDirectArtifactRoot() throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Path artifactRoot = repositoryRoot.resolve(".project-memory");
    writeBaseArtifacts(artifactRoot);

    ProjectMemoryArtifacts fromRepository = reader.load(
        repositoryRoot,
        ProjectMemoryArtifactReader.GraphRequirement.OPTIONAL);
    ProjectMemoryArtifacts fromArtifactRoot = reader.load(
        artifactRoot,
        ProjectMemoryArtifactReader.GraphRequirement.OPTIONAL);

    assertAll(
        () -> assertEquals(artifactRoot, fromRepository.artifactRoot()),
        () -> assertEquals(artifactRoot, fromArtifactRoot.artifactRoot()),
        () -> assertEquals("1.0", fromRepository.projectMapSchemaVersion()),
        () -> assertEquals(1, fromRepository.evidenceRecords().size()),
        () -> assertEquals(1, fromRepository.evidenceById().size()));
  }

  @Test
  void acceptsMissingOptionalGraphAndLoadsPresentOptionalGraph() throws Exception {
    Path artifactRoot = tempDir.resolve("repo/.project-memory");
    writeBaseArtifacts(artifactRoot);

    ProjectMemoryArtifacts withoutGraph = reader.load(
        artifactRoot.getParent(),
        ProjectMemoryArtifactReader.GraphRequirement.OPTIONAL);
    writeValidGraph(artifactRoot);
    ProjectMemoryArtifacts withGraph = reader.load(
        artifactRoot.getParent(),
        ProjectMemoryArtifactReader.GraphRequirement.OPTIONAL);

    assertAll(
        () -> assertTrue(!withoutGraph.hasProjectGraph()),
        () -> assertTrue(withGraph.hasProjectGraph()),
        () -> assertEquals("1.0", withGraph.projectGraphSchemaVersion()));
  }

  @Test
  void doesNotReadGraphWhenGraphIsNotRequested() throws Exception {
    Path artifactRoot = tempDir.resolve("repo/.project-memory");
    writeBaseArtifacts(artifactRoot);
    Files.writeString(artifactRoot.resolve("project-graph.json"), "{not-json");

    ProjectMemoryArtifacts artifacts = reader.load(
        artifactRoot.getParent(),
        ProjectMemoryArtifactReader.GraphRequirement.NONE);

    assertAll(
        () -> assertTrue(!artifacts.hasProjectGraph()),
        () -> assertEquals("1.0", artifacts.projectMapSchemaVersion()),
        () -> assertEquals(1, artifacts.evidenceRecords().size()));
  }

  @Test
  void requiresGraphWhenRequested() throws Exception {
    Path artifactRoot = tempDir.resolve("repo/.project-memory");
    writeBaseArtifacts(artifactRoot);

    QueryArtifactException exception = assertThrows(
        QueryArtifactException.class,
        () -> reader.load(
            artifactRoot.getParent(),
            ProjectMemoryArtifactReader.GraphRequirement.REQUIRED));

    assertEquals("Missing project-graph.json.", exception.getMessage());
  }

  @Test
  void rejectsMissingPathNonDirectoryAndMissingArtifactRoot() throws Exception {
    Path filePath = tempDir.resolve("file.txt");
    Files.writeString(filePath, "not a directory");

    assertAll(
        () -> assertArtifactError(
            tempDir.resolve("missing"),
            "Query path does not exist."),
        () -> assertArtifactError(
            filePath,
            "Query path is not a directory."),
        () -> assertArtifactError(
            tempDir,
            "Missing .project-memory artifact root."));
  }

  @Test
  void rejectsMissingRequiredArtifacts() throws Exception {
    Path artifactRoot = tempDir.resolve("repo/.project-memory");
    Files.createDirectories(artifactRoot);

    assertArtifactError(artifactRoot.getParent(), "Missing project-map.json.");

    Files.writeString(artifactRoot.resolve("project-map.json"), minimalProjectMap());

    assertArtifactError(artifactRoot.getParent(), "Missing evidence-index.jsonl.");
  }

  @Test
  void rejectsMalformedJsonAndJsonl() throws Exception {
    Path artifactRoot = tempDir.resolve("repo/.project-memory");
    writeBaseArtifacts(artifactRoot);
    Files.writeString(artifactRoot.resolve("project-map.json"), "{not-json");

    assertArtifactError(artifactRoot.getParent(), "Malformed project-map.json.");

    Files.writeString(artifactRoot.resolve("project-map.json"), minimalProjectMap());
    Files.writeString(artifactRoot.resolve("evidence-index.jsonl"), "not-json\n");

    assertArtifactError(artifactRoot.getParent(), "Malformed evidence-index.jsonl.");
  }

  @Test
  void rejectsUnsupportedSchemaMarkers() throws Exception {
    Path artifactRoot = tempDir.resolve("repo/.project-memory");
    writeBaseArtifacts(artifactRoot);
    Files.writeString(
        artifactRoot.resolve("project-map.json"),
        minimalProjectMap().replace("\"1.0\"", "\"0.9\""));

    assertArtifactError(
        artifactRoot.getParent(),
        "Unsupported project-map.json schema_version.");

    Files.writeString(artifactRoot.resolve("project-map.json"), minimalProjectMap());
    writeValidGraph(artifactRoot);
    Files.writeString(
        artifactRoot.resolve("project-graph.json"),
        validGraph().replace("\"graph_schema_version\":\"1.0\"", "\"graph_schema_version\":\"0.9\""));

    assertArtifactError(
        artifactRoot.getParent(),
        "Unsupported project-graph.json graph_schema_version.");
  }

  @Test
  void rejectsDuplicateEvidenceIds() throws Exception {
    Path artifactRoot = tempDir.resolve("repo/.project-memory");
    writeBaseArtifacts(artifactRoot);
    Files.writeString(
        artifactRoot.resolve("evidence-index.jsonl"),
        evidenceRecord("ev:one") + evidenceRecord("ev:one"));

    assertArtifactError(
        artifactRoot.getParent(),
        "Duplicate evidence id in evidence-index.jsonl.");
  }

  @Test
  void rejectsInvalidGraphReferencesAndEvidenceReferences() throws Exception {
    Path artifactRoot = tempDir.resolve("repo/.project-memory");
    writeBaseArtifacts(artifactRoot);
    Files.writeString(artifactRoot.resolve("project-graph.json"), """
        {
          "graph_schema_version": "1.0",
          "project_map_schema_version": "1.0",
          "nodes": [{"id": "node:module:root", "evidence_ids": []}],
          "edges": [{"id": "edge:bad", "source_id": "node:module:root", "target_id": "node:missing"}],
          "relation_statuses": [],
          "warnings": []
        }
        """);

    assertArtifactError(
        artifactRoot.getParent(),
        "Invalid project-graph.json graph reference.");

    Files.writeString(artifactRoot.resolve("project-graph.json"), """
        {
          "graph_schema_version": "1.0",
          "project_map_schema_version": "1.0",
          "nodes": [{"id": "node:module:root", "evidence_ids": ["ev:missing"]}],
          "edges": [],
          "relation_statuses": [],
          "warnings": []
        }
        """);

    assertArtifactError(
        artifactRoot.getParent(),
        "Invalid project-graph.json evidence reference.");
  }

  @Test
  void rejectsSymlinkedArtifactRootAndFiles() throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Files.createDirectories(repositoryRoot);
    Path outsideArtifactRoot = tempDir.resolve("outside-artifacts");
    writeBaseArtifacts(outsideArtifactRoot);
    assumeTrue(createSymbolicLink(repositoryRoot.resolve(".project-memory"), outsideArtifactRoot));

    assertArtifactError(
        repositoryRoot,
        "Query artifact root must not be a symbolic link.");

    Path secondRepositoryRoot = tempDir.resolve("repo2");
    Path artifactRoot = secondRepositoryRoot.resolve(".project-memory");
    writeBaseArtifacts(artifactRoot);
    Path outsideProjectMap = tempDir.resolve("outside-project-map.json");
    Files.writeString(outsideProjectMap, minimalProjectMap());
    Files.delete(artifactRoot.resolve("project-map.json"));
    assumeTrue(createSymbolicLink(artifactRoot.resolve("project-map.json"), outsideProjectMap));

    assertArtifactError(
        secondRepositoryRoot,
        "project-map.json must not be a symbolic link.");
  }

  @Test
  void rejectsHardlinkedArtifactFilesBeforeParsing() throws Exception {
    Path repositoryRoot = tempDir.resolve("repo");
    Path artifactRoot = repositoryRoot.resolve(".project-memory");
    writeBaseArtifacts(artifactRoot);
    Path outsideProjectMap = tempDir.resolve("outside-hardlinked-project-map.json");
    Files.writeString(outsideProjectMap, "{\"schema_version\":\"FAKE_HARDLINKED_QUERY_SECRET\"}\n");
    Files.delete(artifactRoot.resolve("project-map.json"));
    assumeTrue(createHardLink(artifactRoot.resolve("project-map.json"), outsideProjectMap));

    QueryArtifactException exception = assertThrows(
        QueryArtifactException.class,
        () -> reader.load(repositoryRoot, ProjectMemoryArtifactReader.GraphRequirement.OPTIONAL));

    assertAll(
        () -> assertEquals("project-map.json is not a regular file.", exception.getMessage()),
        () -> assertFalse(exception.getMessage().contains(outsideProjectMap.toString())),
        () -> assertFalse(exception.getMessage().contains("FAKE_HARDLINKED_QUERY_SECRET")));
  }

  private void assertArtifactError(Path path, String message) {
    QueryArtifactException exception = assertThrows(
        QueryArtifactException.class,
        () -> reader.load(path, ProjectMemoryArtifactReader.GraphRequirement.OPTIONAL));
    assertEquals(message, exception.getMessage());
  }

  private void writeBaseArtifacts(Path artifactRoot) throws IOException {
    Files.createDirectories(artifactRoot);
    Files.writeString(artifactRoot.resolve("project-map.json"), minimalProjectMap());
    Files.writeString(artifactRoot.resolve("evidence-index.jsonl"), evidenceRecord("ev:one"));
  }

  private void writeValidGraph(Path artifactRoot) throws IOException {
    Files.writeString(artifactRoot.resolve("project-graph.json"), validGraph());
  }

  private String minimalProjectMap() {
    return "{\"schema_version\":\"1.0\",\"project\":{\"modules\":{\"items\":[]}}}\n";
  }

  private String validGraph() {
    return """
        {
          "graph_schema_version":"1.0",
          "project_map_schema_version":"1.0",
          "nodes":[{"id":"node:module:root","evidence_ids":[]}],
          "edges":[],
          "relation_statuses":[],
          "warnings":[]
        }
        """;
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
}
