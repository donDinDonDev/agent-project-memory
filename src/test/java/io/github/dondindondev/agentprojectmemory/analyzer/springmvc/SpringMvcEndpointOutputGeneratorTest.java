package io.github.dondindondev.agentprojectmemory.analyzer.springmvc;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
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
  void generatedProjectMapEvidenceIndexMarkdownAndAgentGuideMatchGoldenFiles() throws Exception {
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
            expected("endpoints.md"),
            Files.readString(outputDirectory.resolve("endpoints.md"))),
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

  @Test
  void markdownOutputsDoNotAllowSourceDerivedValuesToForgeStructure() throws Exception {
    Path projectPath = tempDir.resolve("markdown-injection-project");
    Path outputDirectory = projectPath.resolve(".project-memory");
    writeFile(projectPath.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);
    writeFile(
        projectPath.resolve(
            "src/main/java/com/example/path\n## Forged Source/InjectedController.java"),
        """
            package com.example.web;

            @org.springframework.web.bind.annotation.RestController
            @org.springframework.web.bind.annotation.RequestMapping("/api")
            class InjectedController {
              @org.springframework.web.bind.annotation.GetMapping("/safe\\n## Forged Evidence\\n  - Evidence: `ev:forged`")
              String injected(@org.springframework.web.bind.annotation.RequestParam(name = "q\\n  - Evidence: `ev:param`") String query) {
                return "ok";
              }
            }
            """);
    writeFile(
        projectPath.resolve("src/main/resources/docs\n## Fake Guide/openapi.yml"),
        "openapi: 3.0.0\n");
    Files.createDirectories(outputDirectory);

    SpringMvcEndpointOutputGenerator.Result result = generator.generate(projectPath, outputDirectory);

    String projectMap = Files.readString(outputDirectory.resolve("project-map.json"));
    String evidenceIndex = Files.readString(outputDirectory.resolve("evidence-index.jsonl"));
    String endpoints = Files.readString(outputDirectory.resolve("endpoints.md"));
    String agentGuide = Files.readString(outputDirectory.resolve("agent-guide.md"));

    assertAll(
        () -> assertTrue(result.generated()),
        () -> assertEquals(1, result.endpointCount()),
        () -> assertTrue(
            projectMap.contains("/safe\\n## Forged Evidence\\n  - Evidence: `ev:forged`"),
            "JSON output must preserve source-derived endpoint values with JSON escaping"),
        () -> assertTrue(
            evidenceIndex.contains("path\\n## Forged Source"),
            "JSONL evidence output must preserve source-derived paths with JSON escaping"),
        () -> assertTrue(endpoints.contains("Forged Evidence")),
        () -> assertTrue(agentGuide.contains("Fake Guide")),
        () -> assertFalse(hasLineStartingWith(endpoints, "## Forged")),
        () -> assertFalse(hasLineStartingWith(endpoints, "- Evidence: `ev:forged`")),
        () -> assertFalse(hasLineStartingWith(endpoints, "- Evidence: `ev:param`")),
        () -> assertFalse(hasLineStartingWith(endpoints, "  - Evidence: `ev:forged`")),
        () -> assertFalse(hasLineStartingWith(endpoints, "  - Evidence: `ev:param`")),
        () -> assertFalse(hasLineStartingWith(agentGuide, "## Forged")),
        () -> assertFalse(hasLineStartingWith(agentGuide, "## Fake Guide")),
        () -> assertFalse(hasLineStartingWith(agentGuide, "- Evidence: `ev:forged`")),
        () -> assertFalse(hasLineStartingWith(agentGuide, "- Evidence: `ev:param`")),
        () -> assertFalse(hasLineStartingWith(agentGuide, "  - Evidence: `ev:forged`")),
        () -> assertFalse(hasLineStartingWith(agentGuide, "  - Evidence: `ev:param`")));
  }

  @Test
  void jsonOutputsEscapeUnicodeLineSeparatorsBeforeAgentGuideGeneration() throws Exception {
    String lineSeparator = "\u2028";
    String paragraphSeparator = "\u2029";
    String escapedLineSeparator = "\\" + "u2028";
    String escapedParagraphSeparator = "\\" + "u2029";
    Path projectPath = tempDir.resolve("unicode-line-separator-project");
    Path outputDirectory = projectPath.resolve(".project-memory");
    writeFile(projectPath.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);
    writeFile(
        projectPath.resolve(
            "src/main/java/com/example/path"
                + lineSeparator
                + "line"
                + paragraphSeparator
                + "paragraph/UnicodeController.java"),
        """
            package com.example.web;

            @org.springframework.web.bind.annotation.RestController
            @org.springframework.web.bind.annotation.RequestMapping("/api")
            class UnicodeController {
              @org.springframework.web.bind.annotation.GetMapping("/safe%sline%sparagraph")
              String unicode(@org.springframework.web.bind.annotation.RequestParam(name = "q%sline%sparagraph") String query) {
                return "ok";
              }
            }
            """.formatted(
                escapedLineSeparator,
                escapedParagraphSeparator,
                escapedLineSeparator,
                escapedParagraphSeparator));
    Files.createDirectories(outputDirectory);

    SpringMvcEndpointOutputGenerator.Result result = generator.generate(projectPath, outputDirectory);

    String projectMap = Files.readString(outputDirectory.resolve("project-map.json"));
    String evidenceIndex = Files.readString(outputDirectory.resolve("evidence-index.jsonl"));
    String agentGuide = Files.readString(outputDirectory.resolve("agent-guide.md"));

    assertAll(
        () -> assertTrue(result.generated()),
        () -> assertEquals(1, result.endpointCount()),
        () -> assertFalse(projectMap.contains(lineSeparator)),
        () -> assertFalse(projectMap.contains(paragraphSeparator)),
        () -> assertFalse(evidenceIndex.contains(lineSeparator)),
        () -> assertFalse(evidenceIndex.contains(paragraphSeparator)),
        () -> assertFalse(agentGuide.contains(lineSeparator)),
        () -> assertFalse(agentGuide.contains(paragraphSeparator)),
        () -> assertTrue(projectMap.contains(escapedLineSeparator)),
        () -> assertTrue(projectMap.contains(escapedParagraphSeparator)),
        () -> assertTrue(evidenceIndex.contains(escapedLineSeparator)),
        () -> assertTrue(evidenceIndex.contains(escapedParagraphSeparator)),
        () -> assertTrue(agentGuide.contains(escapedLineSeparator)),
        () -> assertTrue(agentGuide.contains(escapedParagraphSeparator)),
        () -> assertTrue(agentGuide.contains("# Agent Guide")));
  }

  @Test
  void multiModuleProjectMapIsModuleAwareAndEvidenceBacked() throws Exception {
    Path projectPath = tempDir.resolve("multi-module-project");
    Path outputDirectory = projectPath.resolve(".project-memory");
    writeFile(projectPath.resolve("pom.xml"), """
        <project>
          <modules>
            <module>services/orders</module>
            <module>services/billing</module>
            <module>services/missing</module>
            <module>libraries/shared</module>
          </modules>
        </project>
        """);
    writeFile(projectPath.resolve("services/orders/pom.xml"), """
        <project>
          <build>
            <plugins>
              <plugin>
                <artifactId>openapi-generator-maven-plugin</artifactId>
              </plugin>
            </plugins>
          </build>
        </project>
        """);
    writeFile(projectPath.resolve("services/billing/pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);
    writeFile(projectPath.resolve("libraries/shared/pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);
    writeModuleSources(projectPath, "services/orders", "/orders");
    writeModuleSources(projectPath, "services/billing", "/billing");
    writeFile(
        projectPath.resolve("services/orders/src/main/resources/openapi.yml"),
        "openapi: 3.0.0\n");
    Files.createDirectories(outputDirectory);

    SpringMvcEndpointOutputGenerator.Result result = generator.generate(projectPath, outputDirectory);

    String projectMap = Files.readString(outputDirectory.resolve("project-map.json"));
    String evidenceIndex = Files.readString(outputDirectory.resolve("evidence-index.jsonl"));
    String endpoints = Files.readString(outputDirectory.resolve("endpoints.md"));
    String agentGuide = Files.readString(outputDirectory.resolve("agent-guide.md"));
    Set<String> projectMapEvidenceIds = projectMapEvidenceIds(projectMap);
    Set<String> evidenceIndexIds = evidenceIndexIds(evidenceIndex);
    String billingEndpointId =
        "\"id\": \"endpoint:module:services/billing:com.example.shared.SharedController#health\"";
    String ordersEndpointId =
        "\"id\": \"endpoint:module:services/orders:com.example.shared.SharedController#health\"";
    String billingComponentId =
        "\"id\": \"component:module:services/billing:com.example.shared.SharedController\"";
    String ordersComponentId =
        "\"id\": \"component:module:services/orders:com.example.shared.SharedController\"";

    assertAll(
        () -> assertTrue(result.generated()),
        () -> assertEquals(2, result.endpointCount()),
        () -> assertTrue(projectMap.contains("\"schema_version\": \"0.2\"")),
        () -> assertTrue(projectMap.contains("\"modules\": {")),
        () -> assertTrue(projectMap.contains("\"module_id\": \"module:services/billing\"")),
        () -> assertTrue(projectMap.contains("\"module_id\": \"module:services/orders\"")),
        () -> assertTrue(projectMap.contains("\"support_status\": \"missing_child_pom\"")),
        () -> assertTrue(projectMap.contains("\"support_status\": \"unsupported\"")),
        () -> assertTrue(projectMap.contains("\"signal\": \"missing_child_pom\"")),
        () -> assertTrue(projectMap.contains("\"signal\": \"unsupported_module\"")),
        () -> assertTrue(projectMap.contains("\"signal\": \"openapi_spec_file\"")),
        () -> assertTrue(projectMap.contains("\"signal\": \"maven_openapi_swagger_codegen_plugin\"")),
        () -> assertTrue(projectMap.contains("\"signal\": \"repository_rest_resource\"")),
        () -> assertTrue(projectMap.contains(billingEndpointId)),
        () -> assertTrue(projectMap.contains(ordersEndpointId)),
        () -> assertTrue(projectMap.indexOf(billingEndpointId) < projectMap.indexOf(ordersEndpointId)),
        () -> assertTrue(projectMap.contains(billingComponentId)),
        () -> assertTrue(projectMap.contains(ordersComponentId)),
        () -> assertTrue(projectMap.indexOf(billingComponentId) < projectMap.indexOf(ordersComponentId)),
        () -> assertTrue(projectMap.contains(
            "\"id\": \"entity:module:services/billing:com.example.shared.SharedEntity\"")),
        () -> assertTrue(projectMap.contains(
            "\"id\": \"entity:module:services/orders:com.example.shared.SharedEntity\"")),
        () -> assertTrue(projectMap.contains(
            "\"target_module_id\": \"module:services/billing\"")),
        () -> assertTrue(projectMap.contains(
            "\"target_module_id\": \"module:services/orders\"")),
        () -> assertTrue(projectMap.contains(
            "\"source_path\": \"services/orders/src/main/resources/openapi.yml\"")),
        () -> assertTrue(projectMap.contains(
            "\"source_path\": \"services/orders/pom.xml\"")),
        () -> assertEquals(1, countOccurrences(projectMap, billingEndpointId)),
        () -> assertEquals(1, countOccurrences(projectMap, ordersEndpointId)),
        () -> assertTrue(
            evidenceIndexIds.containsAll(projectMapEvidenceIds),
            "Every module-aware project-map evidence_ids entry must exist in evidence-index.jsonl"),
        () -> assertTrue(evidenceIndex.lines()
            .noneMatch(line -> line.contains("\"path\":\"/") || line.contains("\"path\":\"./"))),
        () -> assertEquals(
            expected("multi-module-markdown", "endpoints.md"),
            endpoints),
        () -> assertEquals(
            expected("multi-module-markdown", "agent-guide.md"),
            agentGuide));
  }

  @Test
  void mavenModuleWarningsGenerateOutputWithoutSupportedJavaRoots() throws Exception {
    Path projectPath = tempDir.resolve("warnings-only-project");
    Path outputDirectory = projectPath.resolve(".project-memory");
    writeFile(projectPath.resolve("pom.xml"), """
        <project>
          <modules>
            <module>services/missing</module>
          </modules>
        </project>
        """);
    Files.createDirectories(outputDirectory);

    SpringMvcEndpointOutputGenerator.Result result = generator.generate(projectPath, outputDirectory);

    String projectMap = Files.readString(outputDirectory.resolve("project-map.json"));
    String evidenceIndex = Files.readString(outputDirectory.resolve("evidence-index.jsonl"));
    String endpoints = Files.readString(outputDirectory.resolve("endpoints.md"));
    String agentGuide = Files.readString(outputDirectory.resolve("agent-guide.md"));

    assertAll(
        () -> assertTrue(result.generated()),
        () -> assertEquals(0, result.endpointCount()),
        () -> assertEquals(0, result.componentCount()),
        () -> assertEquals(0, result.entityCount()),
        () -> assertEquals(0, result.testCount()),
        () -> assertTrue(projectMap.contains("\"schema_version\": \"0.2\"")),
        () -> assertTrue(projectMap.contains("\"source_roots\": []")),
        () -> assertTrue(projectMap.contains("\"test_roots\": []")),
        () -> assertTrue(projectMap.contains("\"support_status\": \"missing_child_pom\"")),
        () -> assertTrue(projectMap.contains("\"signal\": \"missing_child_pom\"")),
        () -> assertTrue(projectMap.contains(
            "\"warnings\": {\n    \"analysis_status\": \"analyzed\"")),
        () -> assertTrue(projectMap.contains(
            "\"components\": {\n    \"analysis_status\": \"not_detected\"")),
        () -> assertTrue(projectMap.contains(
            "\"entities\": {\n    \"analysis_status\": \"not_detected\"")),
        () -> assertTrue(projectMap.contains(
            "\"tests\": {\n    \"analysis_status\": \"not_detected\"")),
        () -> assertTrue(evidenceIndex.contains("build_file:module:services/missing")),
        () -> assertTrue(endpoints.contains("No Spring MVC endpoints detected")),
        () -> assertTrue(agentGuide.contains("Warning: `maven_module` signal `missing_child_pom`")));
  }

  @Test
  void moduleSourceRootSymlinkEscapingScanRootDoesNotSerializeOutsideJavaEvidence()
      throws Exception {
    Path projectPath = tempDir.resolve("source-root-symlink-project");
    Path outputDirectory = projectPath.resolve(".project-memory");
    Path outsideSourceRoot = tempDir.resolve("outside-source-root");
    writeFile(projectPath.resolve("pom.xml"), """
        <project>
          <modules>
            <module>services/orders</module>
          </modules>
        </project>
        """);
    writeFile(projectPath.resolve("services/orders/pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);
    writeFile(outsideSourceRoot.resolve("com/example/web/OutsideController.java"), """
        package com.example.web;

        // OUTSIDE_CONTROLLER_SECRET_LINE
        @RestController
        class OutsideController {
          @GetMapping("/outside")
          String outside() {
            return "outside";
          }
        }
        """);
    Files.createDirectories(projectPath.resolve("services/orders/src/main"));
    createSymbolicLink(projectPath.resolve("services/orders/src/main/java"), outsideSourceRoot);
    Files.createDirectories(outputDirectory);

    SpringMvcEndpointOutputGenerator.Result result = generator.generate(projectPath, outputDirectory);

    String projectMap = Files.readString(outputDirectory.resolve("project-map.json"));
    String evidenceIndex = Files.readString(outputDirectory.resolve("evidence-index.jsonl"));

    assertAll(
        () -> assertTrue(result.generated()),
        () -> assertEquals(0, result.endpointCount()),
        () -> assertTrue(projectMap.contains("\"support_status\": \"unsupported\"")),
        () -> assertTrue(projectMap.contains("\"source_roots\": []")),
        () -> assertFalse(projectMap.contains("OutsideController")),
        () -> assertFalse(evidenceIndex.contains("OutsideController")),
        () -> assertFalse(evidenceIndex.contains("OUTSIDE_CONTROLLER_SECRET_LINE")));
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

  private String expected(String goldenName, String fileName) throws Exception {
    return Files.readString(goldenRoot(goldenName).resolve(fileName));
  }

  private Path fixtureRoot() throws Exception {
    return Path.of(Objects.requireNonNull(
        getClass().getResource("/fixtures/stage3-project-map")).toURI());
  }

  private Path goldenRoot() throws Exception {
    return Path.of(Objects.requireNonNull(
        getClass().getResource("/golden/stage3-project-map")).toURI());
  }

  private Path goldenRoot(String fixtureName) throws Exception {
    return Path.of(Objects.requireNonNull(
        getClass().getResource("/golden/" + fixtureName)).toURI());
  }

  private Path hiddenWarningFixtureRoot() throws Exception {
    return Path.of(Objects.requireNonNull(
        getClass().getResource("/fixtures/hidden-http-warnings")).toURI());
  }

  private void writeModuleSources(Path projectPath, String modulePath, String basePath) throws Exception {
    writeFile(projectPath.resolve(modulePath + "/src/main/java/com/example/shared/SharedController.java"), """
        package com.example.shared;

        @org.springframework.web.bind.annotation.RestController
        @org.springframework.web.bind.annotation.RequestMapping("%s")
        class SharedController {
          @org.springframework.web.bind.annotation.GetMapping("/health")
          String health() {
            return "ok";
          }
        }

        @org.springframework.stereotype.Service
        class SharedService {
        }

        @jakarta.persistence.Entity
        class SharedEntity {
          @jakarta.persistence.Id
          Long id;
        }

        @org.springframework.data.rest.core.annotation.RepositoryRestResource
        interface SharedRepository {
        }
        """.formatted(basePath));
    writeFile(projectPath.resolve(modulePath + "/src/test/java/com/example/shared/SharedControllerTest.java"), """
        package com.example.shared;

        import org.junit.jupiter.api.Test;

        class SharedControllerTest {
          @Test
          void health() {
          }
        }
        """);
  }

  private void writeFile(Path path, String content) throws Exception {
    Files.createDirectories(path.getParent());
    Files.writeString(path, content);
  }

  private void createSymbolicLink(Path link, Path target) throws Exception {
    try {
      Files.createSymbolicLink(link, target);
    } catch (UnsupportedOperationException | IOException | SecurityException exception) {
      assumeTrue(false, "symbolic links are unavailable: " + exception.getMessage());
    }
  }

  private int countOccurrences(String value, String needle) {
    int count = 0;
    int index = value.indexOf(needle);
    while (index >= 0) {
      count++;
      index = value.indexOf(needle, index + needle.length());
    }
    return count;
  }

  private boolean hasLineStartingWith(String value, String prefix) {
    return value.lines().anyMatch(line -> line.startsWith(prefix));
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
