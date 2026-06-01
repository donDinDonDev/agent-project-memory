package io.github.dondindondev.agentprojectmemory.analyzer.springmvc;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class SpringMvcEndpointOutputGeneratorTest {
  private static final Pattern EVIDENCE_ID_ARRAY = Pattern.compile(
      "\"evidence_ids\": \\[(.*?)]",
      Pattern.DOTALL);
  private static final Pattern JSON_STRING = Pattern.compile("\"((?:\\\\.|[^\"\\\\])*)\"");
  private static final Pattern EVIDENCE_INDEX_ID = Pattern.compile("^\\{\"id\":\"([^\"]+)\"");

  @TempDir
  private Path tempDir;

  private final SpringMvcEndpointOutputGenerator generator = new SpringMvcEndpointOutputGenerator();

  @Test
  void generatedProjectMapEvidenceIndexAndAgentGuideMatchGoldenFiles() throws Exception {
    Path projectPath = tempDir.resolve("stage3-project-map");
    Path outputDirectory = projectPath.resolve(".project-memory");
    copyDirectory(fixtureRoot(), projectPath);
    Files.createDirectories(outputDirectory);

    SpringMvcEndpointOutputGenerator.Result result = generator.generate(
        projectPath,
        outputDirectory);

    assertAll(
        () -> assertTrue(result.generated()),
        () -> assertEquals(
            expected("project-map.json"),
            Files.readString(outputDirectory.resolve("project-map.json"))),
        () -> assertEquals(
            expected("evidence-index.jsonl"),
            Files.readString(outputDirectory.resolve("evidence-index.jsonl"))),
        () -> assertEquals(
            expected("agent-guide.md"),
            Files.readString(outputDirectory.resolve("agent-guide.md"))));
  }

  @Test
  void projectMapEvidenceIdsResolveToEvidenceIndexRecords() throws Exception {
    Path projectPath = tempDir.resolve("stage3-project-map");
    Path outputDirectory = projectPath.resolve(".project-memory");
    copyDirectory(fixtureRoot(), projectPath);
    Files.createDirectories(outputDirectory);

    generator.generate(projectPath, outputDirectory);

    String projectMap = Files.readString(outputDirectory.resolve("project-map.json"));
    String evidenceIndex = Files.readString(outputDirectory.resolve("evidence-index.jsonl"));
    Set<String> projectMapEvidenceIds = projectMapEvidenceIds(projectMap);
    Set<String> evidenceIndexIds = evidenceIndexIds(evidenceIndex);

    assertAll(
        () -> assertEquals(27, projectMapEvidenceIds.size()),
        () -> assertTrue(
            evidenceIndexIds.containsAll(projectMapEvidenceIds),
            "Every project-map evidence_ids entry must exist in evidence-index.jsonl"));
  }

  @Test
  void fullScanOutputWritesAgentGuide() throws Exception {
    Path projectPath = tempDir.resolve("stage3-project-map");
    Path outputDirectory = projectPath.resolve(".project-memory");
    copyDirectory(fixtureRoot(), projectPath);
    Files.createDirectories(outputDirectory);

    SpringMvcEndpointOutputGenerator.Result result = generator.generate(
        projectPath,
        outputDirectory);

    assertAll(
        () -> assertTrue(result.generated()),
        () -> assertTrue(Files.exists(outputDirectory.resolve("agent-guide.md"))),
        () -> assertTrue(Files.readString(outputDirectory.resolve("agent-guide.md"))
            .contains("# Agent Guide")));
  }

  @Test
  void projectMapIncludesAnalyzedComponentInventoryWithoutDroppingEndpoints() throws Exception {
    Path projectPath = tempDir.resolve("stage3-project-map");
    Path outputDirectory = projectPath.resolve(".project-memory");
    copyDirectory(fixtureRoot(), projectPath);
    Files.createDirectories(outputDirectory);

    generator.generate(projectPath, outputDirectory);

    String projectMap = Files.readString(outputDirectory.resolve("project-map.json"));

    assertAll(
        () -> assertTrue(projectMap.contains("\"analysis_status\": \"analyzed\"")),
        () -> assertTrue(projectMap.contains(
            "\"id\": \"component:com.example.components.AppConfiguration\"")),
        () -> assertTrue(projectMap.contains(
            "\"id\": \"component:com.example.components.InventoryComponent\"")),
        () -> assertTrue(projectMap.contains(
            "\"id\": \"component:com.example.components.InventoryRepository\"")),
        () -> assertTrue(projectMap.contains(
            "\"id\": \"component:com.example.components.InventoryService\"")),
        () -> assertTrue(projectMap.contains(
            "\"id\": \"component:com.example.web.ProjectMapController\"")),
        () -> assertTrue(projectMap.indexOf(
            "\"class_name\": \"com.example.components.AppConfiguration\"")
            < projectMap.indexOf("\"class_name\": \"com.example.components.InventoryComponent\"")),
        () -> assertTrue(projectMap.indexOf(
            "\"class_name\": \"com.example.components.InventoryComponent\"")
            < projectMap.indexOf("\"class_name\": \"com.example.components.InventoryRepository\"")),
        () -> assertTrue(projectMap.indexOf(
            "\"class_name\": \"com.example.components.InventoryRepository\"")
            < projectMap.indexOf("\"class_name\": \"com.example.components.InventoryService\"")),
        () -> assertTrue(projectMap.indexOf(
            "\"class_name\": \"com.example.components.InventoryService\"")
            < projectMap.indexOf("\"class_name\": \"com.example.web.ProjectMapController\"")),
        () -> assertTrue(projectMap.contains(
            "\"id\": \"endpoint:com.example.web.ProjectMapController#getItem\"")),
        () -> assertTrue(projectMap.contains(
            "\"id\": \"endpoint:com.example.web.ProjectMapController#createItem\"")));
  }

  @Test
  void componentRestControllerEvidenceReusesEndpointAnnotationEvidenceId() throws Exception {
    Path projectPath = tempDir.resolve("stage3-project-map");
    Path outputDirectory = projectPath.resolve(".project-memory");
    copyDirectory(fixtureRoot(), projectPath);
    Files.createDirectories(outputDirectory);

    generator.generate(projectPath, outputDirectory);

    String evidenceIndex = Files.readString(outputDirectory.resolve("evidence-index.jsonl"));
    String restControllerEvidenceId =
        "ev:src/main/java/com/example/web/ProjectMapController.java:11-11:"
            + "com.example.web.ProjectMapController:@RestController";
    long evidenceLineCount = evidenceIndex.lines()
        .filter(line -> line.contains("\"id\":\"" + restControllerEvidenceId + "\""))
        .count();

    assertEquals(1, evidenceLineCount);
  }

  @Test
  void projectMapIncludesAnalyzedEntityInventoryWithRelationshipUncertainty() throws Exception {
    Path projectPath = tempDir.resolve("stage3-project-map");
    Path outputDirectory = projectPath.resolve(".project-memory");
    copyDirectory(fixtureRoot(), projectPath);
    Files.createDirectories(outputDirectory);

    generator.generate(projectPath, outputDirectory);

    String projectMap = Files.readString(outputDirectory.resolve("project-map.json"));

    assertAll(
        () -> assertTrue(projectMap.contains("\"entities\": {")),
        () -> assertTrue(projectMap.contains(
            "\"id\": \"entity:com.example.domain.ProjectCustomer\"")),
        () -> assertTrue(projectMap.contains(
            "\"id\": \"entity:com.example.domain.ProjectOrder\"")),
        () -> assertTrue(projectMap.contains(
            "\"id\": \"entity:com.example.domain.ProjectVisit\"")),
        () -> assertTrue(projectMap.contains("\"table_name\": \"orders\"")),
        () -> assertTrue(projectMap.contains("\"field_name\": \"id\"")),
        () -> assertTrue(projectMap.contains(
            "\"declaring_class\": \"com.example.domain.ProjectBaseEntity\"")),
        () -> assertTrue(projectMap.contains("\"source_kind\": \"mapped_superclass\"")),
        () -> assertTrue(projectMap.contains("\"source_kind\": \"declared\"")),
        () -> assertTrue(projectMap.contains("\"annotation\": \"@ManyToOne\"")),
        () -> assertTrue(projectMap.contains("\"java_type\": \"ProjectCustomer\"")),
        () -> assertTrue(projectMap.contains("\"annotation\": \"@OneToMany\"")),
        () -> assertTrue(projectMap.contains("\"java_type\": \"List<ProjectOrderLine>\"")),
        () -> assertTrue(projectMap.contains("\"annotation\": \"@OneToOne\"")),
        () -> assertTrue(projectMap.contains("\"annotation\": \"@ManyToMany\"")),
        () -> assertTrue(projectMap.contains("\"target_resolution\": \"declared_type_only\"")),
        () -> assertTrue(projectMap.contains("\"uncertainty\": \"target_type_not_resolved\"")),
        () -> assertTrue(projectMap.indexOf(
            "\"class_name\": \"com.example.domain.ProjectCustomer\"")
            < projectMap.indexOf("\"class_name\": \"com.example.domain.ProjectOrder\"")));
  }

  @Test
  void projectMapIncludesAnalyzedTestsInventoryWithResolvedEvidence() throws Exception {
    Path projectPath = tempDir.resolve("stage3-project-map");
    Path outputDirectory = projectPath.resolve(".project-memory");
    copyDirectory(fixtureRoot(), projectPath);
    Files.createDirectories(outputDirectory);

    generator.generate(projectPath, outputDirectory);

    String projectMap = Files.readString(outputDirectory.resolve("project-map.json"));
    String evidenceIndex = Files.readString(outputDirectory.resolve("evidence-index.jsonl"));

    assertAll(
        () -> assertTrue(projectMap.contains("\"tests\": {")),
        () -> assertTrue(projectMap.contains("\"class_name\": \"com.example.web.ProjectMapControllerTest\"")),
        () -> assertTrue(projectMap.contains("\"source_path\": "
            + "\"src/test/java/com/example/web/ProjectMapControllerTest.java\"")),
        () -> assertTrue(projectMap.contains("\"framework_signals\": []")),
        () -> assertTrue(projectMap.contains(
            "\"class_name\": \"com.example.web.ProjectMapController\"")),
        () -> assertTrue(projectMap.contains("\"support_type\": \"inferred\"")),
        () -> assertTrue(projectMap.contains("\"confidence\": \"medium\"")),
        () -> assertTrue(projectMap.contains("\"uncertainty\": null")),
        () -> assertTrue(evidenceIndex.contains("\"source_type\":\"test_file\"")),
        () -> assertTrue(evidenceIndex.contains("\"source_type\":\"code_symbol\"")),
        () -> assertTrue(evidenceIndex.contains(
            "\"path\":\"src/test/java/com/example/web/ProjectMapControllerTest.java\"")));
  }

  @Test
  void hiddenHttpSurfaceWarningsAreGeneratedWithoutInventingEndpoints() throws Exception {
    Path projectPath = tempDir.resolve("hidden-http-warnings");
    Path outputDirectory = projectPath.resolve(".project-memory");
    copyDirectory(hiddenWarningFixtureRoot(), projectPath);
    Files.createDirectories(outputDirectory);

    SpringMvcEndpointOutputGenerator.Result result = generator.generate(
        projectPath,
        outputDirectory);

    String projectMap = Files.readString(outputDirectory.resolve("project-map.json"));
    String evidenceIndex = Files.readString(outputDirectory.resolve("evidence-index.jsonl"));
    String agentGuide = Files.readString(outputDirectory.resolve("agent-guide.md"));
    Set<String> projectMapEvidenceIds = projectMapEvidenceIds(projectMap);
    Set<String> evidenceIndexIds = evidenceIndexIds(evidenceIndex);

    assertAll(
        () -> assertTrue(result.generated()),
        () -> assertEquals(0, result.endpointCount()),
        () -> assertTrue(projectMap.contains("\"endpoints\": [],")),
        () -> assertTrue(projectMap.contains("\"warnings\": {")),
        () -> assertTrue(projectMap.contains("\"category\": \"hidden_http_surface\"")),
        () -> assertTrue(projectMap.contains("\"signal\": \"openapi_spec_file\"")),
        () -> assertTrue(projectMap.contains("\"source_path\": \"src/main/resources/openapi.yml\"")),
        () -> assertTrue(projectMap.contains("\"signal\": \"repository_rest_resource\"")),
        () -> assertTrue(projectMap.contains("\"signal\": \"maven_openapi_swagger_codegen_plugin\"")),
        () -> assertTrue(evidenceIndex.contains("\"source_type\":\"config_file\"")),
        () -> assertTrue(evidenceIndex.contains("\"symbol_name\":\"openapi.yml\"")),
        () -> assertTrue(evidenceIndex.contains("\"symbol_name\":\"@RepositoryRestResource\"")),
        () -> assertTrue(agentGuide.contains("Warning: `hidden_http_surface` signal `openapi_spec_file`")),
        () -> assertTrue(agentGuide.contains(
            "Warning: `hidden_http_surface` signal `repository_rest_resource`")),
        () -> assertTrue(
            evidenceIndexIds.containsAll(projectMapEvidenceIds),
            "Warning evidence_ids must resolve in evidence-index.jsonl"));
  }

  private Set<String> projectMapEvidenceIds(String projectMap) {
    Set<String> ids = new HashSet<>();
    var arrayMatcher = EVIDENCE_ID_ARRAY.matcher(projectMap);
    while (arrayMatcher.find()) {
      var stringMatcher = JSON_STRING.matcher(arrayMatcher.group(1));
      while (stringMatcher.find()) {
        ids.add(stringMatcher.group(1));
      }
    }
    return ids;
  }

  private Set<String> evidenceIndexIds(String evidenceIndex) {
    Set<String> ids = new HashSet<>();
    for (String line : evidenceIndex.lines().toList()) {
      var matcher = EVIDENCE_INDEX_ID.matcher(line);
      if (matcher.find()) {
        ids.add(matcher.group(1));
      }
    }
    return ids;
  }

  private String expected(String fileName) throws Exception {
    return Files.readString(goldenRoot().resolve(fileName));
  }

  private Path fixtureRoot() throws Exception {
    return Path.of(Objects.requireNonNull(
        getClass().getResource("/fixtures/stage3-project-map")).toURI());
  }

  private Path goldenRoot() throws Exception {
    return Path.of(Objects.requireNonNull(
        getClass().getResource("/golden/stage3-project-map")).toURI());
  }

  private Path hiddenWarningFixtureRoot() throws Exception {
    return Path.of(Objects.requireNonNull(
        getClass().getResource("/fixtures/hidden-http-warnings")).toURI());
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
}
