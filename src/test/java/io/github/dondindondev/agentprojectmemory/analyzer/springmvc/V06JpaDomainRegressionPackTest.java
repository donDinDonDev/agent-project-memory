package io.github.dondindondev.agentprojectmemory.analyzer.springmvc;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class V06JpaDomainRegressionPackTest {
  private static final ObjectMapper JSON = new ObjectMapper();
  private static final Pattern MARKDOWN_EVIDENCE_ID = Pattern.compile("`(ev:[^`]+)`");

  @TempDir
  private Path tempDir;

  private final SpringMvcEndpointOutputGenerator generator = new SpringMvcEndpointOutputGenerator();

  @Test
  void v06Stage3DomainGoldenOutputsRemainStable() throws Exception {
    GeneratedOutput output = generateFromFixture("stage3-project-map");

    assertAll(
        () -> assertEquals(expected("stage3-project-map", "project-map.json"), output.projectMap()),
        () -> assertEquals(expected("stage3-project-map", "evidence-index.jsonl"), output.evidenceIndex()),
        () -> assertEquals(expected("stage3-project-map", "endpoints.md"), output.endpoints()),
        () -> assertEquals(expected("stage3-project-map", "agent-guide.md"), output.agentGuide()),
        () -> assertEvidenceAndReferenceIdsResolve(output),
        () -> assertTrue(output.agentGuide().contains(
            "Domain/data facts are source-visible JPA annotations")),
        () -> assertTrue(output.agentGuide().contains(
            "Entity: Detected `com.example.domain.ProjectOrder`")),
        () -> assertTrue(output.agentGuide().contains(
            "IdClass signal: Source-visible type `ProjectLegacyOrderKey` "
                + "with field_matching_status `not_analyzed`")),
        () -> assertTrue(output.agentGuide().contains(
            "Relationship: Uncertain target for `customer` `@ManyToOne`")),
        () -> assertTrue(output.agentGuide().contains(
            "target_resolution: `declared_type_only`")),
        () -> assertTrue(output.agentGuide().contains(
            "Embedded signal: `@EmbeddedId` declared type `ProjectShipmentId`")),
        () -> assertTrue(output.agentGuide().contains(
            "Inferred repository/entity relations: detected 1 source-visible "
                + "Spring Data generic relation")),
        () -> assertTrue(output.agentGuide().contains(
            "entity_relation_status: `inferred`")),
        () -> assertTrue(output.agentGuide().contains("uncertainty: `null`")));
  }

  @Test
  void v06RepositoryEntityRelationGoldenOutputsRemainStable() throws Exception {
    GeneratedOutput output = generateFromFixture("v0-6-repository-entity-relations");
    JsonNode repositoryItems = JSON.readTree(output.projectMap())
        .path("spring_application_surface")
        .path("repositories")
        .path("items");

    assertAll(
        () -> assertEquals(
            expected("v0-6-repository-entity-relations", "project-map.json"),
            output.projectMap()),
        () -> assertEquals(
            expected("v0-6-repository-entity-relations", "evidence-index.jsonl"),
            output.evidenceIndex()),
        () -> assertEquals(
            expected("v0-6-repository-entity-relations", "endpoints.md"),
            output.endpoints()),
        () -> assertEquals(
            expected("v0-6-repository-entity-relations", "agent-guide.md"),
            output.agentGuide()),
        () -> assertEvidenceAndReferenceIdsResolve(output),
        () -> assertEquals(
            "inferred",
            objectWithText(repositoryItems, "class_name", "com.example.repositories.UniqueOrderRepository")
                .path("entity_relation_status")
                .asText()),
        () -> assertEquals(
            "inferred",
            objectWithText(repositoryItems, "class_name", "com.example.repositories.FqcnUniqueOrderRepository")
                .path("entity_relation_status")
                .asText()),
        () -> assertEquals(
            "not_detected",
            objectWithText(repositoryItems, "class_name", "com.example.repositories.MissingOrderRepository")
                .path("entity_relation_status")
                .asText()),
        () -> assertEquals(
            "ambiguous",
            objectWithText(repositoryItems, "class_name", "com.example.repositories.AmbiguousSharedOrderRepository")
                .path("entity_relation_status")
                .asText()),
        () -> assertEquals(
            "unsupported",
            objectWithText(repositoryItems, "class_name", "com.example.repositories.NestedGenericOrderRepository")
                .path("entity_relation_status")
                .asText()),
        () -> assertEquals(
            "unsupported",
            objectWithText(repositoryItems, "class_name", "com.example.repositories.WildcardGenericOrderRepository")
                .path("entity_relation_status")
                .asText()),
        () -> assertEquals(
            "unsupported",
            objectWithText(repositoryItems, "class_name", "com.example.repositories.RawOrderRepository")
                .path("entity_relation_status")
                .asText()),
        () -> assertTrue(output.agentGuide().contains(
            "Inferred repository/entity relations: detected 2 source-visible "
                + "Spring Data generic relations")),
        () -> assertTrue(output.agentGuide().contains(
            "`com.example.repositories.UniqueOrderRepository` -> `com.example.unique.UniqueOrder`")),
        () -> assertTrue(output.agentGuide().contains(
            "`com.example.repositories.MissingOrderRepository`: `entity_relation_status` "
                + "is `not_detected`")),
        () -> assertTrue(output.agentGuide().contains(
            "`com.example.repositories.AmbiguousSharedOrderRepository`: "
                + "`entity_relation_status` is `ambiguous`")),
        () -> assertTrue(output.agentGuide().contains(
            "`com.example.repositories.NestedGenericOrderRepository`: "
                + "`entity_relation_status` is `unsupported`")));
  }

  private GeneratedOutput generateFromFixture(String fixtureName) throws Exception {
    Path fixtureRoot = Path.of(Objects.requireNonNull(
        getClass().getResource("/fixtures/" + fixtureName)).toURI());
    Path projectPath = tempDir.resolve(fixtureName);
    Path outputDirectory = projectPath.resolve(".project-memory");
    copyDirectory(fixtureRoot, projectPath);
    Files.createDirectories(outputDirectory);

    generator.generate(projectPath, outputDirectory);

    return new GeneratedOutput(
        Files.readString(outputDirectory.resolve("project-map.json")),
        Files.readString(outputDirectory.resolve("evidence-index.jsonl")),
        Files.readString(outputDirectory.resolve("endpoints.md")),
        Files.readString(outputDirectory.resolve("agent-guide.md")));
  }

  private void assertEvidenceAndReferenceIdsResolve(GeneratedOutput output) throws Exception {
    Set<String> evidenceIds = evidenceIds(output.evidenceIndex());
    Set<String> referencedIds = referencedEvidenceIds(JSON.readTree(output.projectMap()));
    assertTrue(
        evidenceIds.containsAll(referencedIds),
        "Every project-map evidence_ids entry must exist in evidence-index.jsonl");

    Set<String> guideEvidenceIds = markdownEvidenceIds(output.agentGuide());
    assertTrue(
        evidenceIds.containsAll(guideEvidenceIds),
        "Every agent-guide evidence reference must exist in evidence-index.jsonl");
  }

  private Set<String> evidenceIds(String evidenceIndex) throws Exception {
    Set<String> ids = new LinkedHashSet<>();
    for (String line : evidenceIndex.split("\\R")) {
      if (!line.isBlank()) {
        ids.add(JSON.readTree(line).path("id").asText());
      }
    }
    return ids;
  }

  private Set<String> referencedEvidenceIds(JsonNode node) {
    Set<String> ids = new LinkedHashSet<>();
    collectEvidenceIds(node, ids);
    return ids;
  }

  private void collectEvidenceIds(JsonNode node, Set<String> ids) {
    if (node.isObject()) {
      node.fields().forEachRemaining(entry -> {
        if ("evidence_ids".equals(entry.getKey()) && entry.getValue().isArray()) {
          entry.getValue().forEach(id -> ids.add(id.asText()));
        } else {
          collectEvidenceIds(entry.getValue(), ids);
        }
      });
    } else if (node.isArray()) {
      node.forEach(child -> collectEvidenceIds(child, ids));
    }
  }

  private Set<String> markdownEvidenceIds(String markdown) {
    Set<String> ids = new LinkedHashSet<>();
    java.util.regex.Matcher matcher = MARKDOWN_EVIDENCE_ID.matcher(markdown);
    while (matcher.find()) {
      ids.add(matcher.group(1));
    }
    return ids;
  }

  private JsonNode objectWithText(JsonNode items, String fieldName, String value) {
    if (!items.isArray()) {
      throw new AssertionError("Expected array of objects");
    }
    for (JsonNode item : items) {
      if (value.equals(item.path(fieldName).asText())) {
        return item;
      }
    }
    throw new AssertionError("Missing object with " + fieldName + "=" + value);
  }

  private String expected(String fixtureName, String fileName) throws Exception {
    return Files.readString(goldenRoot(fixtureName).resolve(fileName));
  }

  private Path goldenRoot(String fixtureName) throws Exception {
    return Path.of(Objects.requireNonNull(
        getClass().getResource("/golden/" + fixtureName)).toURI());
  }

  private void copyDirectory(Path source, Path target) throws Exception {
    try (java.util.stream.Stream<Path> paths = Files.walk(source)) {
      for (Path sourcePath : paths.toList()) {
        Path targetPath = target.resolve(source.relativize(sourcePath).toString());
        if (Files.isDirectory(sourcePath)) {
          Files.createDirectories(targetPath);
        } else {
          Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
      }
    }
  }

  private record GeneratedOutput(
      String projectMap,
      String evidenceIndex,
      String endpoints,
      String agentGuide) {
  }
}
