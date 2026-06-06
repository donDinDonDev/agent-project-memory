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

final class V03BuildConfigRegressionPackTest {
  private static final ObjectMapper JSON = new ObjectMapper();
  private static final Pattern GUIDE_EVIDENCE_ID = Pattern.compile("`(ev:[^`]+)`");

  private static final List<String> SENSITIVE_FIXTURE_NEEDLES = List.of(
      "FAKE_STAGE3_CONFIG_SECRET",
      "FAKE_V03_ALPHA_DB_PASSWORD",
      "FAKE_V03_ALPHA_ENV_TOKEN",
      "FAKE_V03_ALPHA_CI_SECRET",
      "FAKE_V03_ALPHA_LOGBACK_SECRET",
      "FAKE_V03_ALPHA_TEST_PASSWORD",
      "FAKE_V03_ZETA_API_KEY",
      "FAKE_V03_ZETA_LOG4J_PASSWORD",
      "alpha-processor",
      "internal-api.yml",
      "target/generated-sources/zeta-private");

  @TempDir
  private Path tempDir;

  private final SpringMvcEndpointOutputGenerator generator = new SpringMvcEndpointOutputGenerator();

  @Test
  void singleModuleV03RegressionGoldenOutputsRemainStable() throws Exception {
    GeneratedOutput output = generateFromFixture("stage3-project-map");

    assertAll(
        () -> assertEquals(expected("stage3-project-map", "project-map.json"), output.projectMap()),
        () -> assertEquals(expected("stage3-project-map", "evidence-index.jsonl"), output.evidenceIndex()),
        () -> assertEquals(expected("stage3-project-map", "agent-guide.md"), output.agentGuide()),
        () -> assertEvidenceIdsResolve(output),
        () -> assertSensitiveFixtureNeedlesDoNotAppear(output));
  }

  @Test
  void multiModuleV03RegressionGoldenOutputsRemainStable() throws Exception {
    GeneratedOutput output = generateFromFixture("v0-3-build-config-regression");

    assertAll(
        () -> assertEquals(
            expected("v0-3-build-config-regression", "project-map.json"),
            output.projectMap()),
        () -> assertEquals(
            expected("v0-3-build-config-regression", "evidence-index.jsonl"),
            output.evidenceIndex()),
        () -> assertEquals(
            expected("v0-3-build-config-regression", "agent-guide.md"),
            output.agentGuide()),
        () -> assertEvidenceIdsResolve(output),
        () -> assertRelativeEvidencePaths(output.evidenceIndex()),
        () -> assertSensitiveFixtureNeedlesDoNotAppear(output));
  }

  @Test
  void multiModuleV03RegressionPackPreservesOrderingAndBuildConfigBoundaries()
      throws Exception {
    GeneratedOutput output = generateFromFixture("v0-3-build-config-regression");
    JsonNode projectMap = JSON.readTree(output.projectMap());
    JsonNode modules = projectMap.path("project").path("modules").path("items");
    JsonNode common = moduleNode(projectMap, "module:libraries/common");
    JsonNode alpha = moduleNode(projectMap, "module:services/alpha");
    JsonNode zeta = moduleNode(projectMap, "module:services/zeta");
    JsonNode alphaMaven = alpha.path("build_config").path("maven");
    JsonNode zetaMaven = zeta.path("build_config").path("maven");

    assertAll(
        () -> assertEquals(
            List.of(
                "module:libraries/common",
                "module:services/alpha",
                "module:services/zeta"),
            values(modules, "module_id")),
        () -> assertEquals(
            List.of("services/alpha/src/main/java", "services/zeta/src/main/java"),
            values(projectMap.path("project").path("source_roots"))),
        () -> assertEquals("unsupported", common.path("support_status").asText()),
        () -> assertEquals(
            "common-library",
            common.path("build_config")
                .path("maven")
                .path("metadata")
                .path("artifact_id")
                .path("value")
                .asText()),
        () -> assertEquals(
            List.of("alpha-core", "zeta-helper", "spring-boot-starter-web"),
            mavenValueList(alphaMaven.path("dependencies").path("items"), "artifact_id")),
        () -> assertEquals(
            List.of(3, 1, 2),
            intValues(alphaMaven.path("dependencies").path("items"), "declaration_ordinal")),
        () -> assertEquals(
            "dependency_management",
            alphaMaven.path("dependency_management")
                .path("items")
                .get(0)
                .path("declaration_kind")
                .asText()),
        () -> assertEquals(
            "plugin_management",
            alphaMaven.path("plugin_management")
                .path("items")
                .get(0)
                .path("declaration_kind")
                .asText()),
        () -> assertEquals(
            List.of("build-helper-maven-plugin", "spring-boot-maven-plugin"),
            mavenValueList(alphaMaven.path("plugins").path("items"), "artifact_id")),
        () -> assertEquals(
            List.of("jaxb2-maven-plugin", "openapi-generator-maven-plugin"),
            mavenValueList(zetaMaven.path("plugins").path("items"), "artifact_id")),
        () -> assertEquals(
            List.of(
                "services/alpha/src/main/resources/logback-spring.xml",
                "services/alpha/src/main/resources/application-ci.properties",
                "services/alpha/src/main/resources/application.yml",
                "services/alpha/src/test/resources/application-test.yaml"),
            values(alpha.path("build_config").path("config_files").path("items"), "path")),
        () -> assertEquals(
            "spring_boot_application_with_main_method",
            alpha.path("build_config")
                .path("spring_boot_applications")
                .path("items")
                .get(0)
                .path("application_signal")
                .asText()),
        () -> assertEquals(
            "spring_boot_application_annotation_only",
            zeta.path("build_config")
                .path("spring_boot_applications")
                .path("items")
                .get(0)
                .path("application_signal")
                .asText()),
        () -> assertWarningSignals(
            projectMap,
            List.of(
                "generated_source:maven_annotation_processor",
                "generated_source:maven_build_helper_add_source",
                "generated_source:maven_generated_source_config",
                "generated_source:maven_generator_plugin",
                "generated_source:maven_openapi_swagger_codegen_plugin",
                "hidden_http_surface:maven_openapi_swagger_codegen_plugin",
                "hidden_http_surface:openapi_spec_file",
                "maven_module:unsupported_module")),
        () -> assertTrue(output.agentGuide()
            .contains("dependency-management declarations; these are management declarations")),
        () -> assertTrue(output.agentGuide()
            .contains("plugin-management declarations; these are management declarations")),
        () -> assertTrue(output.agentGuide()
            .contains("config contents, keys, and values are not rendered")),
        () -> assertFalse(output.projectMap().contains("generated API operation")),
        () -> assertFalse(output.agentGuide().contains("Detected generated API")));
  }

  @Test
  void multiModuleV03RegressionPackGenerationIsDeterministicAcrossRuns()
      throws Exception {
    GeneratedOutput first = generateFromFixture("v0-3-build-config-regression");
    GeneratedOutput second = generateFromFixture("v0-3-build-config-regression");

    assertAll(
        () -> assertEquals(first.projectMap(), second.projectMap()),
        () -> assertEquals(first.evidenceIndex(), second.evidenceIndex()),
        () -> assertEquals(first.agentGuide(), second.agentGuide()));
  }

  private GeneratedOutput generateFromFixture(String fixtureName) throws Exception {
    Path projectPath = tempDir.resolve(fixtureName + "-" + System.nanoTime());
    Path outputDirectory = projectPath.resolve(".project-memory");
    copyDirectory(fixtureRoot(fixtureName), projectPath);
    Files.createDirectories(outputDirectory);

    SpringMvcEndpointOutputGenerator.Result result = generator.generate(projectPath, outputDirectory);

    assertTrue(result.generated());
    return new GeneratedOutput(
        Files.readString(outputDirectory.resolve("project-map.json")),
        Files.readString(outputDirectory.resolve("evidence-index.jsonl")),
        Files.readString(outputDirectory.resolve("agent-guide.md")));
  }

  private void assertEvidenceIdsResolve(GeneratedOutput output) throws Exception {
    Set<String> evidenceIndexIds = evidenceIndexIds(output.evidenceIndex());
    Set<String> projectMapEvidenceIds = projectMapEvidenceIds(output.projectMap());
    Set<String> guideEvidenceIds = guideEvidenceIds(output.agentGuide());

    assertTrue(
        evidenceIndexIds.containsAll(projectMapEvidenceIds),
        "Every project-map evidence_ids entry must exist in evidence-index.jsonl");
    assertTrue(
        evidenceIndexIds.containsAll(guideEvidenceIds),
        "Every agent-guide evidence reference must exist in evidence-index.jsonl");
  }

  private Set<String> projectMapEvidenceIds(String projectMap) throws Exception {
    Set<String> ids = new LinkedHashSet<>();
    collectEvidenceIds(JSON.readTree(projectMap), ids);
    return ids;
  }

  private void collectEvidenceIds(JsonNode node, Set<String> ids) {
    if (node.isObject()) {
      node.fields().forEachRemaining(field -> {
        if (field.getKey().endsWith("evidence_ids") && field.getValue().isArray()) {
          field.getValue().forEach(id -> ids.add(id.asText()));
        }
        collectEvidenceIds(field.getValue(), ids);
      });
      return;
    }
    if (node.isArray()) {
      node.forEach(child -> collectEvidenceIds(child, ids));
    }
  }

  private Set<String> evidenceIndexIds(String evidenceIndex) throws Exception {
    Set<String> ids = new LinkedHashSet<>();
    for (String line : evidenceIndex.lines().toList()) {
      if (!line.isBlank()) {
        ids.add(JSON.readTree(line).path("id").asText());
      }
    }
    return ids;
  }

  private Set<String> guideEvidenceIds(String agentGuide) {
    Set<String> ids = new LinkedHashSet<>();
    var matcher = GUIDE_EVIDENCE_ID.matcher(agentGuide);
    while (matcher.find()) {
      ids.add(matcher.group(1));
    }
    return ids;
  }

  private void assertRelativeEvidencePaths(String evidenceIndex) throws Exception {
    for (String line : evidenceIndex.lines().toList()) {
      if (!line.isBlank()) {
        String path = JSON.readTree(line).path("path").asText();
        assertFalse(Path.of(path).isAbsolute(), "Evidence path must be repository-relative: " + path);
        assertFalse(path.startsWith("./"), "Evidence path must not start with ./: " + path);
        assertFalse(path.contains("\\"), "Evidence path must use slash separators: " + path);
        for (String segment : path.split("/")) {
          assertFalse("..".equals(segment), "Evidence path must not contain .. segments: " + path);
        }
      }
    }
  }

  private void assertSensitiveFixtureNeedlesDoNotAppear(GeneratedOutput output) {
    String joinedOutput = String.join(
        "\n",
        output.projectMap(),
        output.evidenceIndex(),
        output.agentGuide());
    assertAll(SENSITIVE_FIXTURE_NEEDLES.stream()
        .map(needle -> () -> assertFalse(
            joinedOutput.contains(needle),
            "Generated outputs must not contain fixture-only sensitive needle: " + needle)));
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

  private List<String> mavenValueList(JsonNode items, String fieldName) {
    return items.findValues(fieldName).stream()
        .map(value -> value.path("value").asText())
        .toList();
  }

  private List<String> values(JsonNode items, String fieldName) {
    return items.findValues(fieldName).stream()
        .map(JsonNode::asText)
        .toList();
  }

  private List<String> values(JsonNode items) {
    java.util.ArrayList<String> values = new java.util.ArrayList<>();
    items.forEach(item -> values.add(item.asText()));
    return values;
  }

  private List<Integer> intValues(JsonNode items, String fieldName) {
    return items.findValues(fieldName).stream()
        .map(JsonNode::asInt)
        .toList();
  }

  private void assertWarningSignals(JsonNode projectMap, List<String> expectedSignals) {
    JsonNode warnings = projectMap.path("warnings").path("items");
    java.util.ArrayList<String> actualSignals = new java.util.ArrayList<>();
    warnings.forEach(warning -> actualSignals.add(
        warning.path("category").asText() + ":" + warning.path("signal").asText()));

    assertEquals(expectedSignals, actualSignals);
  }

  private String expected(String goldenName, String fileName) throws Exception {
    return Files.readString(goldenRoot(goldenName).resolve(fileName));
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

  private record GeneratedOutput(
      String projectMap,
      String evidenceIndex,
      String agentGuide) {
  }
}
