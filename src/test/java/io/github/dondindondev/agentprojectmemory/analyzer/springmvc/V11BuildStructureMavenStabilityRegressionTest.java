package io.github.dondindondev.agentprojectmemory.analyzer.springmvc;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class V11BuildStructureMavenStabilityRegressionTest {
  private static final ObjectMapper JSON = new ObjectMapper();
  private static final String FIXTURE_NAME = "v0-3-build-config-regression";

  @TempDir
  private Path tempDir;

  private final SpringMvcEndpointOutputGenerator generator = new SpringMvcEndpointOutputGenerator();

  @Test
  void buildStructureRefactorPreservesMavenGoldenOutputsAndModuleSemantics() throws Exception {
    GeneratedOutput output = generateFromFixture();
    JsonNode projectMap = JSON.readTree(output.projectMap());
    JsonNode modules = projectMap.path("project").path("modules").path("items");
    JsonNode warnings = projectMap.path("warnings").path("items");
    JsonNode alpha = moduleNode(projectMap, "module:services/alpha");

    assertAll(
        () -> assertEquals(expected("project-map.json"), output.projectMap()),
        () -> assertEquals(expected("evidence-index.jsonl"), output.evidenceIndex()),
        () -> assertEquals(expected("agent-guide.md"), output.agentGuide()),
        () -> assertEquals(
            List.of(
                "module:libraries/common",
                "module:services/alpha",
                "module:services/zeta"),
            textValues(modules, "module_id")),
        () -> assertEquals(
            List.of(
                "libraries/common/pom.xml",
                "services/alpha/pom.xml",
                "services/zeta/pom.xml"),
            textValues(modules, "pom_path")),
        () -> assertEquals(
            List.of(
                "root_modules_entry",
                "root_modules_entry",
                "root_modules_entry"),
            textValues(modules, "declaration_kind")),
        () -> assertEquals("analyzed", alpha.path("build_config").path("analysis_status").asText()),
        () -> assertTrue(alpha.path("build_config").has("maven")),
        () -> assertEquals(
            List.of(
                "generated_source:maven_annotation_processor",
                "generated_source:maven_build_helper_add_source",
                "generated_source:maven_generated_source_config",
                "generated_source:maven_generator_plugin",
                "generated_source:maven_openapi_swagger_codegen_plugin",
                "hidden_http_surface:maven_openapi_swagger_codegen_plugin",
                "hidden_http_surface:openapi_spec_file",
                "maven_module:unsupported_module"),
            warningSignals(warnings)));
  }

  private GeneratedOutput generateFromFixture() throws Exception {
    Path projectPath = tempDir.resolve(FIXTURE_NAME);
    Path outputDirectory = projectPath.resolve(".project-memory");
    copyDirectory(fixtureRoot(), projectPath);
    Files.createDirectories(outputDirectory);

    SpringMvcEndpointOutputGenerator.Result result = generator.generate(projectPath, outputDirectory);

    assertTrue(result.generated());
    return new GeneratedOutput(
        Files.readString(outputDirectory.resolve("project-map.json")),
        Files.readString(outputDirectory.resolve("evidence-index.jsonl")),
        Files.readString(outputDirectory.resolve("agent-guide.md")));
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

  private List<String> textValues(JsonNode items, String fieldName) {
    List<String> values = new ArrayList<>();
    items.forEach(item -> values.add(item.path(fieldName).asText()));
    return values;
  }

  private List<String> warningSignals(JsonNode warnings) {
    List<String> signals = new ArrayList<>();
    warnings.forEach(warning -> signals.add(
        warning.path("category").asText() + ":" + warning.path("signal").asText()));
    return signals;
  }

  private String expected(String fileName) throws Exception {
    return Files.readString(goldenRoot().resolve(fileName));
  }

  private Path fixtureRoot() throws Exception {
    return Path.of(Objects.requireNonNull(
        getClass().getResource("/fixtures/" + FIXTURE_NAME)).toURI());
  }

  private Path goldenRoot() throws Exception {
    return Path.of(Objects.requireNonNull(
        getClass().getResource("/golden/" + FIXTURE_NAME)).toURI());
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
