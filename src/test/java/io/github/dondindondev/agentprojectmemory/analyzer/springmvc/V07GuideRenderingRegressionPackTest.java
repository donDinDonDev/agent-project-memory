package io.github.dondindondev.agentprojectmemory.analyzer.springmvc;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class V07GuideRenderingRegressionPackTest {
  private static final ObjectMapper JSON = new ObjectMapper();
  private static final Pattern MARKDOWN_EVIDENCE_ID = Pattern.compile("`(ev:[^`]+)`");

  @TempDir
  private Path tempDir;

  private final SpringMvcEndpointOutputGenerator generator = new SpringMvcEndpointOutputGenerator();

  @Test
  void v07GuideQualityGoldenOutputsRemainStableAndCautious() throws Exception {
    GeneratedOutput output = generateFromFixture("v0-7-guide-quality-regression");
    JsonNode root = JSON.readTree(output.projectMap());
    JsonNode tests = root.path("tests").path("items");
    JsonNode testGapSignals = root.path("quality").path("test_gap_signals").path("items");
    JsonNode changeRiskSignals = root.path("quality").path("change_risk_signals").path("items");

    assertAll(
        () -> assertEquals(
            expected("v0-7-guide-quality-regression", "project-map.json"),
            output.projectMap()),
        () -> assertEquals(
            expected("v0-7-guide-quality-regression", "evidence-index.jsonl"),
            output.evidenceIndex()),
        () -> assertEquals(
            expected("v0-7-guide-quality-regression", "endpoints.md"),
            output.endpoints()),
        () -> assertEquals(
            expected("v0-7-guide-quality-regression", "agent-guide.md"),
            output.agentGuide()),
        () -> assertEvidenceAndReferenceIdsResolve(output),
        () -> assertEquals(4, tests.size()),
        () -> assertTrue(output.agentGuide().contains(
            "- Inferred tested subject: `com.example.web.OrderController`")),
        () -> assertTrue(output.agentGuide().contains(
            "- Ambiguous tested subject candidate: `com.example.alpha.DuplicateService`")),
        () -> assertTrue(output.agentGuide().contains(
            "- Tested-subject status: `not_detected`")),
        () -> assertTrue(output.agentGuide().contains(
            "- Tested-subject status: `unsupported`")),
        () -> assertFalse(hasSignalWithSubject(
            testGapSignals,
            "endpoint_without_obvious_test",
            "com.example.web.OrderController#getOrder")),
        () -> assertTrue(hasSignalWithSubject(
            testGapSignals,
            "repository_without_obvious_test",
            "com.example.repositories.MissingOrderRepository")),
        () -> assertTrue(hasSignalWithSubject(
            testGapSignals,
            "entity_without_obvious_test",
            "com.example.domain.Order")),
        () -> assertIterableEquals(
            List.of(
                "entity_without_obvious_test",
                "entity_without_obvious_test",
                "repository_without_obvious_test"),
            signalNames(testGapSignals)),
        () -> assertIterableEquals(
            List.of(
                "jpa_relationship_change_surface",
                "repository_entity_relation_uncertain",
                "spring_bean_method_change_surface",
                "spring_bean_method_change_surface",
                "spring_configuration_change_surface",
                "spring_security_warning_change_surface",
                "spring_security_warning_change_surface",
                "spring_service_change_surface",
                "transaction_boundary_change_surface"),
            signalNames(changeRiskSignals)),
        () -> assertTrue(output.agentGuide().contains("## Quality And Change-Risk Signals")),
        () -> assertTrue(output.agentGuide().contains(
            "No coverage, execution, assertion, CI, or runtime relation is claimed")),
        () -> assertTrue(output.agentGuide().contains(
            "No production impact, vulnerability, correctness, runtime behavior, or business priority is claimed")),
        () -> assertTrue(output.agentGuide().contains(
            "planning hints only, not coverage, runtime, correctness, vulnerability, or business-priority claims")),
        () -> assertFalse(output.agentGuide().contains("No tests exist")),
        () -> assertFalse(output.agentGuide().contains("coverage proof.")),
        () -> assertFalse(output.agentGuide().contains("vulnerability detected")));
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

  private boolean hasSignalWithSubject(JsonNode items, String signalName, String subjectName) {
    if (!items.isArray()) {
      return false;
    }
    for (JsonNode item : items) {
      if (signalName.equals(item.path("signal").asText())
          && subjectName.equals(item.path("subject_name").asText())) {
        return true;
      }
    }
    return false;
  }

  private List<String> signalNames(JsonNode items) {
    List<String> names = new ArrayList<>();
    if (items.isArray()) {
      items.forEach(item -> names.add(item.path("signal").asText()));
    }
    return names;
  }

  private String expected(String fixtureName, String fileName) throws Exception {
    return Files.readString(goldenRoot(fixtureName).resolve(fileName));
  }

  private Path goldenRoot(String fixtureName) throws Exception {
    return Path.of(Objects.requireNonNull(
        getClass().getResource("/golden/" + fixtureName)).toURI());
  }

  private void copyDirectory(Path source, Path target) throws Exception {
    try (var paths = Files.walk(source)) {
      for (Path path : paths.toList()) {
        Path relative = source.relativize(path);
        Path destination = target.resolve(relative);
        if (Files.isDirectory(path)) {
          Files.createDirectories(destination);
        } else {
          Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING);
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
