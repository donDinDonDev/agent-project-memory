package io.github.dondindondev.agentprojectmemory.analyzer.springmvc;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

final class V05SpringApplicationSurfaceRegressionPackTest {
  private static final ObjectMapper JSON = new ObjectMapper();
  private static final Pattern MARKDOWN_EVIDENCE_ID = Pattern.compile("`(ev:[^`]+)`");

  @TempDir
  private Path tempDir;

  private final SpringMvcEndpointOutputGenerator generator = new SpringMvcEndpointOutputGenerator();

  @Test
  void v05ConfigurationSurfaceGoldenOutputsRemainStable() throws Exception {
    GeneratedOutput output = generateFromFixture("v0-5-spring-configuration-surface");

    assertAll(
        () -> assertEquals(expected("project-map.json"), output.projectMap()),
        () -> assertEquals(expected("evidence-index.jsonl"), output.evidenceIndex()),
        () -> assertEquals(expected("endpoints.md"), output.endpoints()),
        () -> assertEquals(expected("agent-guide.md"), output.agentGuide()),
        () -> assertEvidenceAndReferenceIdsResolve(output));
  }

  @Test
  void configurationSurfaceKeepsFactsSourceVisibleAndRuntimeStatusesNotAnalyzed()
      throws Exception {
    GeneratedOutput output = generateFromFixture("v0-5-spring-configuration-surface");
    JsonNode root = JSON.readTree(output.projectMap());
    JsonNode configuration = root.path("spring_application_surface").path("configuration");
    JsonNode configurationClasses = configuration.path("configuration_classes").path("items");
    JsonNode configurationProperties = configuration.path("configuration_properties").path("items");
    JsonNode beanMethods = configuration.path("bean_methods").path("items");

    assertAll(
        () -> assertEquals("analyzed", root.path("spring_application_surface")
            .path("analysis_status").asText()),
        () -> assertEquals("analyzed", configuration.path("configuration_classes")
            .path("analysis_status").asText()),
        () -> assertEquals("analyzed", configuration.path("configuration_properties")
            .path("analysis_status").asText()),
        () -> assertEquals("analyzed", configuration.path("bean_methods")
            .path("analysis_status").asText()),
        () -> assertEquals(
            List.of("com.example.config.InventoryConfiguration"),
            jsonTextValues(configurationClasses, "class_name")),
        () -> assertEquals(
            List.of(
                "com.example.config.CatalogProperties",
                "com.example.config.InventoryProperties"),
            jsonTextValues(configurationProperties, "class_name")),
        () -> assertEquals(
            List.of(
                "com.example.config.InventoryConfiguration#inventoryClient",
                "com.example.config.InventoryConfiguration#inventoryClock",
                "com.example.config.SecondaryBeanFactory#secondaryBean"),
            beanMethodLabels(beanMethods)),
        () -> assertTrue(allTextValuesEqual(configurationClasses, "support_type", "extracted")),
        () -> assertTrue(allTextValuesEqual(configurationProperties, "support_type", "extracted")),
        () -> assertTrue(allTextValuesEqual(beanMethods, "support_type", "extracted")),
        () -> assertTrue(allTextValuesEqual(configurationProperties, "binding_status", "not_analyzed")),
        () -> assertTrue(allTextValuesEqual(beanMethods, "bean_name_status", "not_analyzed")),
        () -> assertFalse(output.projectMap().contains("UnresolvedConfiguration")),
        () -> assertFalse(output.projectMap().contains("UnresolvedProperties")),
        () -> assertFalse(output.projectMap().contains("unresolvedBean")),
        () -> assertFalse(output.projectMap().contains("\"prefix\"")),
        () -> assertTrue(output.agentGuide().contains("they do not prove runtime bean graphs")));
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
      if (line.isBlank()) {
        continue;
      }
      ids.add(JSON.readTree(line).path("id").asText());
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

  private List<String> jsonTextValues(JsonNode array, String fieldName) {
    return java.util.stream.StreamSupport.stream(array.spliterator(), false)
        .map(item -> item.path(fieldName).asText())
        .toList();
  }

  private List<String> beanMethodLabels(JsonNode array) {
    return java.util.stream.StreamSupport.stream(array.spliterator(), false)
        .map(item -> item.path("class_name").asText() + "#" + item.path("method_name").asText())
        .toList();
  }

  private boolean allTextValuesEqual(JsonNode array, String fieldName, String expected) {
    return java.util.stream.StreamSupport.stream(array.spliterator(), false)
        .allMatch(item -> expected.equals(item.path(fieldName).asText()));
  }

  private String expected(String fileName) throws Exception {
    return Files.readString(goldenRoot().resolve(fileName));
  }

  private Path goldenRoot() throws Exception {
    return Path.of(Objects.requireNonNull(
        getClass().getResource("/golden/v0-5-spring-configuration-surface")).toURI());
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
