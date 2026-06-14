package io.github.dondindondev.agentprojectmemory.analyzer.springmvc;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.dondindondev.agentprojectmemory.analyzer.gradle.GradleBuildFileInput;
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

final class V11GradleRootDetectionRegressionTest {
  private static final ObjectMapper JSON = new ObjectMapper();
  private static final Pattern EVIDENCE_ID_ARRAY = Pattern.compile(
      "\"evidence_ids\": \\[(.*?)]",
      Pattern.DOTALL);
  private static final Pattern JSON_STRING = Pattern.compile("\"((?:\\\\.|[^\"\\\\])*)\"");
  private static final Pattern EVIDENCE_INDEX_ID = Pattern.compile("^\\{\"id\":\"([^\"]+)\"");
  private static final String FIXTURE_NAME = "v1-1-gradle-single-project";
  private static final String MULTI_PROJECT_FIXTURE_NAME = "v1-1-gradle-multi-project";
  private static final String ANALYZER_INTEGRATION_FIXTURE_NAME = "v1-1-gradle-analyzer-integration";

  @TempDir
  private Path tempDir;

  private final SpringMvcEndpointOutputGenerator generator = new SpringMvcEndpointOutputGenerator();

  @Test
  void singleProjectGradleRootDetectionMatchesGoldenOutput() throws Exception {
    GeneratedOutput output = generateFromFixture(FIXTURE_NAME);
    JsonNode projectMap = JSON.readTree(output.projectMap());
    JsonNode build = projectMap.path("project").path("build");
    JsonNode module = projectMap.path("project").path("modules").path("items").get(0);
    JsonNode gradle = module.path("build_config").path("gradle");

    assertAll(
        () -> assertEquals(expected(FIXTURE_NAME, "project-map.json"), output.projectMap()),
        () -> assertEquals(expected(FIXTURE_NAME, "evidence-index.jsonl"), output.evidenceIndex()),
        () -> assertEquals(expected(FIXTURE_NAME, "endpoints.md"), output.endpoints()),
        () -> assertEquals(expected(FIXTURE_NAME, "agent-guide.md"), output.agentGuide()),
        () -> assertEquals("1.0", projectMap.path("schema_version").asText()),
        () -> assertEquals("gradle", build.path("system").asText()),
        () -> assertEquals("settings.gradle.kts", build.path("root_build_file").asText()),
        () -> assertEquals(
            List.of("settings.gradle.kts", "build.gradle.kts"),
            textValues(build.path("root_build_files"), "path")),
        () -> assertEquals("module:.", module.path("module_id").asText()),
        () -> assertTrue(module.path("pom_path").isNull()),
        () -> assertEquals(List.of("gradle"), stringValues(module.path("build_systems"))),
        () -> assertEquals(":", module.path("gradle_project_path").asText()),
        () -> assertEquals(List.of("src/main/java"), stringValues(module.path("source_roots"))),
        () -> assertEquals(List.of("src/test/java"), stringValues(module.path("test_roots"))),
        () -> assertEquals("analyzed", gradle.path("analysis_status").asText()),
        () -> assertEquals("not_analyzed", gradle.path("source_sets").path("analysis_status").asText()),
        () -> assertEquals(
            List.of("settings.gradle.kts", "build.gradle.kts"),
            textValues(gradle.path("build_files"), "path")),
        () -> assertEvidenceIdsResolve(output));
  }

  @Test
  void multiProjectGradleSettingsIncludesMatchGoldenOutput() throws Exception {
    GeneratedOutput output = generateFromFixture(MULTI_PROJECT_FIXTURE_NAME);
    JsonNode projectMap = JSON.readTree(output.projectMap());
    JsonNode modules = projectMap.path("project").path("modules").path("items");
    JsonNode warnings = projectMap.path("warnings").path("items");
    JsonNode app = moduleNode(projectMap, "module:app");
    JsonNode missing = moduleNode(projectMap, "module:libs/missing");
    JsonNode empty = moduleNode(projectMap, "module:libs/empty");
    JsonNode orders = moduleNode(projectMap, "module:services/orders");

    assertAll(
        () -> assertEquals(expected(MULTI_PROJECT_FIXTURE_NAME, "project-map.json"), output.projectMap()),
        () -> assertEquals(expected(MULTI_PROJECT_FIXTURE_NAME, "evidence-index.jsonl"), output.evidenceIndex()),
        () -> assertEquals(expected(MULTI_PROJECT_FIXTURE_NAME, "endpoints.md"), output.endpoints()),
        () -> assertEquals(expected(MULTI_PROJECT_FIXTURE_NAME, "agent-guide.md"), output.agentGuide()),
        () -> assertEquals("1.0", projectMap.path("schema_version").asText()),
        () -> assertEquals("gradle", projectMap.path("project").path("build").path("system").asText()),
        () -> assertEquals(
            List.of("module:.", "module:app", "module:libs/empty", "module:libs/missing", "module:services/orders"),
            textValues(modules, "module_id")),
        () -> assertEquals(List.of("app/src/main/java", "src/main/java"),
            stringValues(projectMap.path("project").path("source_roots"))),
        () -> assertEquals("gradle_settings_include", app.path("declaration_kind").asText()),
        () -> assertEquals(":app", app.path("gradle_project_path").asText()),
        () -> assertEquals(
            List.of("settings.gradle.kts", "app/build.gradle.kts"),
            textValues(app.path("build_config").path("gradle").path("build_files"), "path")),
        () -> assertEquals("missing_project_directory", missing.path("support_status").asText()),
        () -> assertEquals("unsupported", empty.path("support_status").asText()),
        () -> assertEquals(":services:orders", orders.path("gradle_project_path").asText()),
        () -> assertEquals(
            List.of(
                "duplicate_project_path",
                "missing_project_directory",
                "unsupported_dynamic_include",
                "unsupported_module",
                "unsupported_project_dir_mapping"),
            textValues(warnings, "signal")),
        () -> assertEvidenceIdsResolve(output));
  }

  @Test
  void gradleAnalyzerIntegrationMatchesGoldenOutput() throws Exception {
    GeneratedOutput output = generateFromFixture(ANALYZER_INTEGRATION_FIXTURE_NAME);
    JsonNode projectMap = JSON.readTree(output.projectMap());
    JsonNode module = projectMap.path("project").path("modules").path("items").get(0);

    assertAll(
        () -> assertEquals(expected(ANALYZER_INTEGRATION_FIXTURE_NAME, "project-map.json"), output.projectMap()),
        () -> assertEquals(expected(ANALYZER_INTEGRATION_FIXTURE_NAME, "evidence-index.jsonl"), output.evidenceIndex()),
        () -> assertEquals(expected(ANALYZER_INTEGRATION_FIXTURE_NAME, "endpoints.md"), output.endpoints()),
        () -> assertEquals(expected(ANALYZER_INTEGRATION_FIXTURE_NAME, "agent-guide.md"), output.agentGuide()),
        () -> assertEquals("gradle", projectMap.path("project").path("build").path("system").asText()),
        () -> assertEquals(1, projectMap.path("endpoints").size()),
        () -> assertEquals(
            1,
            projectMap.path("api_surface").path("openapi").path("operations").path("items").size()),
        () -> assertFalse(
            module.path("build_config").path("config_files").path("items").isEmpty(),
            "Gradle resource roots should feed config/resource analysis"),
        () -> assertFalse(
            module.path("build_config").path("spring_boot_applications").path("items").isEmpty(),
            "Gradle source roots should feed Spring Boot application analysis"),
        () -> assertFalse(projectMap.path("components").path("items").isEmpty()),
        () -> assertFalse(projectMap.path("entities").path("items").isEmpty()),
        () -> assertFalse(projectMap.path("spring_application_surface").path("repositories").path("items").isEmpty()),
        () -> assertFalse(projectMap.path("tests").path("items").isEmpty()),
        () -> assertFalse(projectMap.path("documents").path("items").isEmpty()),
        () -> assertFalse(projectMap.path("quality").path("test_gap_signals").path("items").isEmpty()),
        () -> assertFalse(projectMap.path("quality").path("change_risk_signals").path("items").isEmpty()),
        () -> assertTrue(output.projectMap().contains("\"class_name\": \"com.example.gradle.GradleApplication\"")),
        () -> assertTrue(output.projectMap().contains("\"class_name\": \"com.example.gradle.service.OrderService\"")),
        () -> assertTrue(output.projectMap().contains("\"class_name\": \"com.example.gradle.domain.Order\"")),
        () -> assertTrue(output.projectMap().contains("\"class_name\": \"com.example.gradle.repository.OrderRepository\"")),
        () -> assertTrue(output.projectMap().contains("\"class_name\": \"com.example.gradle.config.GradleConfiguration\"")),
        () -> assertTrue(output.projectMap().contains("\"class_name\": \"com.example.gradle.web.OrderControllerTest\"")),
        () -> assertTrue(output.agentGuide().contains("Source-visible Gradle build files")),
        () -> assertTrue(output.agentGuide().contains("## Quality And Change-Risk Signals")),
        () -> assertTrue(output.endpoints().contains("GET /orders/{id}")),
        () -> assertEvidenceIdsResolve(output));
  }

  @Test
  void oversizedGradleRootBuildFileRendersDiagnosticAndNoBuildEvidence() throws Exception {
    Path projectPath = tempDir.resolve("oversized-gradle-build-file");
    Path outputDirectory = projectPath.resolve(".project-memory");
    Files.createDirectories(outputDirectory);
    writeOversizedGradleBuildFile(projectPath.resolve("build.gradle.kts"));

    SpringMvcEndpointOutputGenerator.Result result = generator.generate(projectPath, outputDirectory);

    String projectMap = Files.readString(outputDirectory.resolve("project-map.json"));
    String evidenceIndex = Files.readString(outputDirectory.resolve("evidence-index.jsonl"));
    JsonNode root = JSON.readTree(projectMap);
    JsonNode diagnostics = root.path("scan").path("diagnostics").path("items");
    JsonNode build = root.path("project").path("build");

    assertAll(
        () -> assertTrue(result.generated()),
        () -> assertEquals(1, result.diagnosticCount()),
        () -> assertEquals(1, diagnostics.size()),
        () -> assertEquals(
            GradleBuildFileInput.DIAGNOSTIC_CODE_GRADLE_BUILD_FILE_BYTES_CAP_EXCEEDED,
            diagnostics.get(0).path("code").asText()),
        () -> assertEquals("warning", diagnostics.get(0).path("severity").asText()),
        () -> assertEquals("gradle", diagnostics.get(0).path("category").asText()),
        () -> assertEquals("build.gradle.kts", diagnostics.get(0).path("path").asText()),
        () -> assertEquals(
            GradleBuildFileInput.MAX_GRADLE_BUILD_FILE_BYTES,
            diagnostics.get(0).path("count").asInt()),
        () -> assertEquals("not_detected", build.path("system").asText()),
        () -> assertTrue(build.path("root_build_file").isNull()),
        () -> assertEquals(0, build.path("evidence_ids").size()),
        () -> assertEquals(0, build.path("root_build_files").size()),
        () -> assertFalse(evidenceIndex.contains("build_file:gradle:build")),
        () -> assertEvidenceIdsResolve(new GeneratedOutput(projectMap, evidenceIndex, "", "")));
  }

  private GeneratedOutput generateFromFixture(String fixtureName) throws Exception {
    Path projectPath = tempDir.resolve(fixtureName);
    Path outputDirectory = projectPath.resolve(".project-memory");
    copyDirectory(fixtureRoot(fixtureName), projectPath);
    Files.createDirectories(outputDirectory);

    SpringMvcEndpointOutputGenerator.Result result = generator.generate(projectPath, outputDirectory);

    assertTrue(result.generated());
    return new GeneratedOutput(
        Files.readString(outputDirectory.resolve("project-map.json")),
        Files.readString(outputDirectory.resolve("evidence-index.jsonl")),
        Files.readString(outputDirectory.resolve("endpoints.md")),
        Files.readString(outputDirectory.resolve("agent-guide.md")));
  }

  private void assertEvidenceIdsResolve(GeneratedOutput output) throws Exception {
    Set<String> projectMapEvidenceIds = projectMapEvidenceIds(output.projectMap());
    Set<String> evidenceIndexIds = evidenceIndexIds(output.evidenceIndex());
    assertTrue(
        evidenceIndexIds.containsAll(projectMapEvidenceIds),
        "Every project-map evidence_ids entry must exist in evidence-index.jsonl");
  }

  private Set<String> projectMapEvidenceIds(String projectMap) {
    Set<String> evidenceIds = new LinkedHashSet<>();
    var matcher = EVIDENCE_ID_ARRAY.matcher(projectMap);
    while (matcher.find()) {
      var stringMatcher = JSON_STRING.matcher(matcher.group(1));
      while (stringMatcher.find()) {
        evidenceIds.add(stringMatcher.group(1));
      }
    }
    return evidenceIds;
  }

  private Set<String> evidenceIndexIds(String evidenceIndex) {
    Set<String> evidenceIds = new LinkedHashSet<>();
    for (String line : evidenceIndex.split("\\R")) {
      if (line.isBlank()) {
        continue;
      }
      var matcher = EVIDENCE_INDEX_ID.matcher(line);
      if (matcher.find()) {
        evidenceIds.add(matcher.group(1));
      }
    }
    return evidenceIds;
  }

  private List<String> textValues(JsonNode items, String fieldName) {
    List<String> values = new java.util.ArrayList<>();
    items.forEach(item -> values.add(item.path(fieldName).asText()));
    return values;
  }

  private List<String> stringValues(JsonNode array) {
    List<String> values = new java.util.ArrayList<>();
    array.forEach(value -> values.add(value.asText()));
    return values;
  }

  private JsonNode moduleNode(JsonNode projectMap, String moduleId) {
    JsonNode modules = projectMap.path("project").path("modules").path("items");
    for (JsonNode module : modules) {
      if (moduleId.equals(module.path("module_id").asText())) {
        return module;
      }
    }
    throw new AssertionError("Missing module block for " + moduleId);
  }

  private String expected(String fixtureName, String fileName) throws Exception {
    return Files.readString(goldenRoot(fixtureName).resolve(fileName));
  }

  private Path fixtureRoot(String fixtureName) throws Exception {
    return Path.of(Objects.requireNonNull(
        getClass().getResource("/fixtures/" + fixtureName)).toURI());
  }

  private Path goldenRoot(String fixtureName) throws Exception {
    return Path.of(Objects.requireNonNull(
        getClass().getResource("/golden/" + fixtureName)).toURI());
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

  private void writeOversizedGradleBuildFile(Path path) throws Exception {
    Files.createDirectories(path.getParent());
    Files.writeString(path, "// " + "x".repeat(GradleBuildFileInput.MAX_GRADLE_BUILD_FILE_BYTES) + "\n");
  }

  private record GeneratedOutput(
      String projectMap,
      String evidenceIndex,
      String endpoints,
      String agentGuide) {
  }
}
