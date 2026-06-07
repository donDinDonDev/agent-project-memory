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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

final class V04ApiSurfaceRegressionPackTest {
  private static final ObjectMapper JSON = new ObjectMapper();
  private static final Pattern MARKDOWN_EVIDENCE_ID = Pattern.compile("`(ev:[^`]+)`");
  private static final List<String> GENERATED_SOURCE_NEEDLES = List.of(
      "GeneratedApiController",
      "FAKE_V04_GENERATED_SOURCE_SECRET");

  @TempDir
  private Path tempDir;

  private final SpringMvcEndpointOutputGenerator generator = new SpringMvcEndpointOutputGenerator();

  @ParameterizedTest
  @ValueSource(strings = {
      "v0-4-controller-only-api",
      "v0-4-openapi-only-api",
      "v0-4-mixed-controller-spec-api",
      "v0-4-generated-source-warning",
      "v0-4-invalid-spec"
  })
  void v04RegressionGoldenOutputsRemainStable(String fixtureName) throws Exception {
    GeneratedOutput output = generateFromFixture(fixtureName);

    assertAll(
        () -> assertEquals(expected(fixtureName, "project-map.json"), output.projectMap()),
        () -> assertEquals(expected(fixtureName, "evidence-index.jsonl"), output.evidenceIndex()),
        () -> assertEquals(expected(fixtureName, "endpoints.md"), output.endpoints()),
        () -> assertEquals(expected(fixtureName, "agent-guide.md"), output.agentGuide()),
        () -> assertEvidenceAndReferenceIdsResolve(output),
        () -> assertRelativeEvidencePaths(output.evidenceIndex()));
  }

  @Test
  void controllerOnlyRegressionKeepsEndpointsCodeBackedWithoutDeclaredApiFacts()
      throws Exception {
    GeneratedOutput output = generateFromFixture("v0-4-controller-only-api");
    JsonNode root = JSON.readTree(output.projectMap());
    JsonNode apiSurface = root.path("api_surface");

    assertAll(
        () -> assertEquals(2, root.path("endpoints").size()),
        () -> assertEquals(
            List.of(
                "endpoint:com.example.api.InventoryController#createInventory",
                "endpoint:com.example.api.InventoryController#getInventory"),
            jsonTextValues(root.path("endpoints"), "id")),
        () -> assertEquals(
            jsonTextValues(root.path("endpoints"), "id"),
            stringValues(apiSurface.path("source_visible_spring_mvc_endpoints").path("endpoint_ids"))),
        () -> assertEquals(0,
            apiSurface.path("interface_declared_spring_mvc_endpoints").path("endpoint_ids").size()),
        () -> assertEquals(0, apiSurface.path("openapi").path("spec_files").path("items").size()),
        () -> assertEquals(0, apiSurface.path("openapi").path("operations").path("items").size()),
        () -> assertEquals(0, root.path("warnings").path("items").size()),
        () -> assertFalse(output.endpoints().contains("Declared OpenAPI Operations\n\n####")),
        () -> assertFalse(output.agentGuide().contains("OpenAPI operation: Detected implemented")));
  }

  @Test
  void openApiOnlyRegressionPreservesDeclaredOperationOrderingOutsideEndpoints()
      throws Exception {
    GeneratedOutput output = generateFromFixture("v0-4-openapi-only-api");
    JsonNode root = JSON.readTree(output.projectMap());
    JsonNode operations = root.path("api_surface").path("openapi").path("operations").path("items");

    assertAll(
        () -> assertEquals(0, root.path("endpoints").size()),
        () -> assertEquals(
            List.of("GET /alpha", "PATCH /alpha", "POST /zeta"),
            operationDisplayValues(operations)),
        () -> assertEquals(
            List.of("getAlpha", "patchAlpha", "postZeta"),
            jsonTextValues(operations, "operation_id")),
        () -> assertTrue(output.endpoints().contains("#### Declared `GET /alpha`")),
        () -> assertTrue(output.endpoints().indexOf("#### Declared `GET /alpha`")
            < output.endpoints().indexOf("#### Declared `PATCH /alpha`")),
        () -> assertTrue(output.endpoints().indexOf("#### Declared `PATCH /alpha`")
            < output.endpoints().indexOf("#### Declared `POST /zeta`")),
        () -> assertFalse(output.endpoints().contains("## Direct Handler Mappings\n\n###")),
        () -> assertFalse(output.projectMap().contains("\"api_surface_category\": \"source_visible_spring_mvc_endpoint\"")));
  }

  @Test
  void mixedRegressionKeepsControllerEndpointAndMatchingSpecOperationSeparate()
      throws Exception {
    GeneratedOutput output = generateFromFixture("v0-4-mixed-controller-spec-api");
    JsonNode root = JSON.readTree(output.projectMap());
    JsonNode operations = root.path("api_surface").path("openapi").path("operations").path("items");

    assertAll(
        () -> assertEquals(1, root.path("endpoints").size()),
        () -> assertEquals(2, operations.size()),
        () -> assertEquals(
            "endpoint:com.example.mixed.OrderController#getOrder",
            root.path("endpoints").get(0).path("id").asText()),
        () -> assertEquals(
            List.of("POST /orders", "GET /orders/{id}"),
            operationDisplayValues(operations)),
        () -> assertEquals("not_analyzed",
            operations.get(0).path("implementation_status").asText()),
        () -> assertTrue(output.endpoints().contains("### GET /orders/{id}")),
        () -> assertTrue(output.endpoints().contains("#### Declared `GET /orders/{id}`")),
        () -> assertFalse(output.projectMap().contains("source_spec_match")),
        () -> assertFalse(output.agentGuide().contains("Source/spec agreement: Detected")),
        () -> assertFalse(output.agentGuide().contains("implementation coverage")));
  }

  @Test
  void generatedSourceRegressionEmitsWarningsWithoutScanningGeneratedContents()
      throws Exception {
    GeneratedOutput output = generateFromFixture("v0-4-generated-source-warning");
    JsonNode root = JSON.readTree(output.projectMap());
    JsonNode generatedWarningIds = root.path("api_surface")
        .path("generated_source_api_signals")
        .path("warning_ids");

    assertAll(
        () -> assertEquals(0, root.path("endpoints").size()),
        () -> assertEquals(0,
            root.path("api_surface").path("openapi").path("operations").path("items").size()),
        () -> assertEquals(
            List.of(
                "target/generated-sources",
                "target/generated-sources/openapi"),
            jsonTextValues(root.path("warnings").path("items"), "source_path")),
        () -> assertEquals(
            List.of(
                "warning:generated_source:generated_source_root_path_detected:path:target/generated-sources",
                "warning:generated_source:generated_source_root_path_detected:path:target/generated-sources/openapi"),
            stringValues(generatedWarningIds)),
        () -> assertTrue(output.evidenceIndex().contains("\"source_type\":\"path_signal\"")),
        () -> assertGeneratedSourceNeedlesDoNotAppear(output));
  }

  @Test
  void invalidSpecRegressionCreatesHiddenWarningWithoutOperationFacts()
      throws Exception {
    GeneratedOutput output = generateFromFixture("v0-4-invalid-spec");
    JsonNode root = JSON.readTree(output.projectMap());
    JsonNode operations = root.path("api_surface").path("openapi").path("operations");
    JsonNode hiddenWarningIds = root.path("api_surface").path("hidden_http_warnings").path("warning_ids");

    assertAll(
        () -> assertEquals(0, root.path("endpoints").size()),
        () -> assertEquals("analyzed", operations.path("analysis_status").asText()),
        () -> assertEquals(0, operations.path("items").size()),
        () -> assertTrue(jsonTextValues(root.path("warnings").path("items"), "signal")
            .contains("openapi_spec_parse_error")),
        () -> assertTrue(stringValues(hiddenWarningIds).stream()
            .anyMatch(id -> id.contains("openapi_spec_parse_error"))),
        () -> assertTrue(output.evidenceIndex().contains("\"source_type\":\"api_spec\"")),
        () -> assertTrue(output.evidenceIndex()
            .contains("\"symbol_name\":\"operation_parse_status:openapi_spec_parse_error\"")),
        () -> assertFalse(output.projectMap().contains("openapi_operation:")),
        () -> assertFalse(output.endpoints().contains("#### Declared `")));
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
        Files.readString(outputDirectory.resolve("endpoints.md")),
        Files.readString(outputDirectory.resolve("agent-guide.md")));
  }

  private void assertEvidenceAndReferenceIdsResolve(GeneratedOutput output) throws Exception {
    Set<String> evidenceIndexIds = evidenceIndexIds(output.evidenceIndex());
    Set<String> projectMapEvidenceIds = projectMapEvidenceIds(output.projectMap());
    Set<String> markdownEvidenceIds = markdownEvidenceIds(output.endpoints());
    markdownEvidenceIds.addAll(markdownEvidenceIds(output.agentGuide()));

    assertTrue(
        evidenceIndexIds.containsAll(projectMapEvidenceIds),
        "Every project-map evidence_ids entry must exist in evidence-index.jsonl");
    assertTrue(
        evidenceIndexIds.containsAll(markdownEvidenceIds),
        "Every Markdown evidence reference must exist in evidence-index.jsonl");
    assertEndpointIdsResolve(output.projectMap());
    assertWarningIdsResolve(output.projectMap());
  }

  private Set<String> projectMapEvidenceIds(String projectMap) throws Exception {
    Set<String> ids = new LinkedHashSet<>();
    collectIds(JSON.readTree(projectMap), "evidence_ids", ids);
    return ids;
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

  private Set<String> markdownEvidenceIds(String markdown) {
    Set<String> ids = new LinkedHashSet<>();
    var matcher = MARKDOWN_EVIDENCE_ID.matcher(markdown);
    while (matcher.find()) {
      ids.add(matcher.group(1));
    }
    return ids;
  }

  private void assertEndpointIdsResolve(String projectMap) throws Exception {
    JsonNode root = JSON.readTree(projectMap);
    Set<String> endpointIds = new LinkedHashSet<>(jsonTextValues(root.path("endpoints"), "id"));
    Set<String> referencedEndpointIds = new LinkedHashSet<>();
    collectIds(root.path("api_surface"), "endpoint_ids", referencedEndpointIds);

    assertTrue(
        endpointIds.containsAll(referencedEndpointIds),
        "Every api_surface endpoint_ids entry must resolve to endpoints[]");
  }

  private void assertWarningIdsResolve(String projectMap) throws Exception {
    JsonNode root = JSON.readTree(projectMap);
    Set<String> warningIds = new LinkedHashSet<>(jsonTextValues(root.path("warnings").path("items"), "id"));
    Set<String> referencedWarningIds = new LinkedHashSet<>();
    collectIds(root.path("api_surface"), "warning_ids", referencedWarningIds);

    assertTrue(
        warningIds.containsAll(referencedWarningIds),
        "Every api_surface warning_ids entry must resolve to warnings.items");
  }

  private void collectIds(JsonNode node, String fieldName, Set<String> ids) {
    if (node.isObject()) {
      node.fields().forEachRemaining(field -> {
        if (fieldName.equals(field.getKey()) && field.getValue().isArray()) {
          field.getValue().forEach(id -> ids.add(id.asText()));
        }
        collectIds(field.getValue(), fieldName, ids);
      });
      return;
    }
    if (node.isArray()) {
      node.forEach(child -> collectIds(child, fieldName, ids));
    }
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

  private void assertGeneratedSourceNeedlesDoNotAppear(GeneratedOutput output) {
    String joinedOutput = String.join(
        "\n",
        output.projectMap(),
        output.evidenceIndex(),
        output.endpoints(),
        output.agentGuide());
    assertAll(GENERATED_SOURCE_NEEDLES.stream()
        .map(needle -> () -> assertFalse(
            joinedOutput.contains(needle),
            "Generated outputs must not contain generated-source content needle: " + needle)));
  }

  private List<String> operationDisplayValues(JsonNode operations) {
    java.util.ArrayList<String> values = new java.util.ArrayList<>();
    operations.forEach(operation -> values.add(
        operation.path("http_method").asText() + " " + operation.path("path").asText()));
    return values;
  }

  private List<String> jsonTextValues(JsonNode items, String fieldName) {
    return items.findValues(fieldName).stream()
        .map(JsonNode::asText)
        .toList();
  }

  private List<String> stringValues(JsonNode items) {
    java.util.ArrayList<String> values = new java.util.ArrayList<>();
    items.forEach(item -> values.add(item.asText()));
    return values;
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
      String endpoints,
      String agentGuide) {
  }
}
