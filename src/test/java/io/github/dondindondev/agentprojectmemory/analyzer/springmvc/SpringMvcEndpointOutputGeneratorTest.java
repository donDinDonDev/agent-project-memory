package io.github.dondindondev.agentprojectmemory.analyzer.springmvc;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.dondindondev.agentprojectmemory.analyzer.EvidenceExcerpts;
import io.github.dondindondev.agentprojectmemory.analyzer.JavaSourceParser;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenPomInput;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class SpringMvcEndpointOutputGeneratorTest {
  private static final ObjectMapper JSON = new ObjectMapper();
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
        () -> assertEquals(77, projectMapEvidenceIds.size()),
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
  void projectMapRendersDocumentHeadingsChunksAndResolvingDocumentEvidence() throws Exception {
    Path projectPath = tempDir.resolve("document-inventory");
    Path outputDirectory = projectPath.resolve(".project-memory");
    Files.createDirectories(outputDirectory);

    writeFile(projectPath.resolve("README.md"), """
        Intro
        # Root docs
        Body text that must not be serialized
        ## API
        More body text that must not be serialized
        """);
    writeFile(projectPath.resolve("docs/guide.md"), "# Guide\n");
    writeFile(projectPath.resolve("docs/private/secret.md"), "# FAKE_PRIVATE_MARKDOWN_SECRET\n");

    SpringMvcEndpointOutputGenerator.Result result = generator.generate(
        projectPath,
        outputDirectory);

    String projectMap = Files.readString(outputDirectory.resolve("project-map.json"));
    String evidenceIndex = Files.readString(outputDirectory.resolve("evidence-index.jsonl"));
    String agentGuide = Files.readString(outputDirectory.resolve("agent-guide.md"));
    Set<String> projectMapEvidenceIds = projectMapEvidenceIds(projectMap);
    Set<String> evidenceIndexIds = evidenceIndexIds(evidenceIndex);
    JsonNode documents = JSON.readTree(projectMap).path("documents");
    JsonNode reconciliation = documents.path("reconciliation");
    JsonNode items = documents.path("items");
    JsonNode readme = items.get(0);
    JsonNode readmeHeadings = readme.path("headings");
    JsonNode readmeChunks = readme.path("chunks");

    assertAll(
        () -> assertTrue(result.generated()),
        () -> assertEquals(2, result.documentCount()),
        () -> assertEquals("1.0", JSON.readTree(projectMap).path("schema_version").asText()),
        () -> assertEquals("analyzed", documents.path("analysis_status").asText()),
        () -> assertEquals("not_detected", reconciliation.path("analysis_status").asText()),
        () -> assertEquals(0, reconciliation.path("items").size()),
        () -> assertEquals("default_local_markdown", documents.path("discovery").path("scope").asText()),
        () -> assertEquals("repository_relative_in_root",
            documents.path("discovery").path("path_policy").asText()),
        () -> assertEquals("skip_symlinks",
            documents.path("discovery").path("symlink_policy").asText()),
        () -> assertEquals(List.of("README.md", "docs/guide.md"), jsonTextValues(items, "path")),
        () -> assertEquals(List.of("root_readme", "docs_tree"), jsonTextValues(items, "discovery_source")),
        () -> assertTrue(readme.path("module_id").isNull()),
        () -> assertEquals("Root docs", readme.path("title").asText()),
        () -> assertEquals("first_heading", readme.path("title_source").asText()),
        () -> assertEquals(2, readmeHeadings.size()),
        () -> assertEquals(
            List.of("ev:README.md:unknown:document:file:README.md"),
            stringValues(readme.path("evidence_ids"))),
        () -> assertEquals(
            "document_heading:README.md:heading:Root%20docs:occ:000001",
            readmeHeadings.get(0).path("id").asText()),
        () -> assertEquals(1, readmeHeadings.get(0).path("level").asInt()),
        () -> assertEquals("Root docs", readmeHeadings.get(0).path("title").asText()),
        () -> assertEquals("root-docs", readmeHeadings.get(0).path("anchor").asText()),
        () -> assertEquals(2, readmeHeadings.get(0).path("line_start").asInt()),
        () -> assertEquals(2, readmeHeadings.get(0).path("line_end").asInt()),
        () -> assertEquals(
            List.of("ev:README.md:2-2:document:heading:Root%20docs:decl:000001"),
            stringValues(readmeHeadings.get(0).path("evidence_ids"))),
        () -> assertEquals(3, readmeChunks.size()),
        () -> assertEquals("document_chunk:README.md:chunk:000001",
            readmeChunks.get(0).path("id").asText()),
        () -> assertTrue(readmeChunks.get(0).path("heading_id").isNull()),
        () -> assertEquals(1, readmeChunks.get(0).path("line_start").asInt()),
        () -> assertEquals(1, readmeChunks.get(0).path("line_end").asInt()),
        () -> assertEquals(
            readmeHeadings.get(0).path("id").asText(),
            readmeChunks.get(1).path("heading_id").asText()),
        () -> assertEquals("not_serialized", readmeChunks.get(1).path("content_status").asText()),
        () -> assertEquals(
            List.of("ev:README.md:2-3:document:chunk:000002"),
            stringValues(readmeChunks.get(1).path("evidence_ids"))),
        () -> assertTrue(
            evidenceIndexIds.containsAll(projectMapEvidenceIds),
            "Every project-map evidence_ids entry must exist in evidence-index.jsonl"),
        () -> assertEquals(9, countOccurrences(evidenceIndex, "\"source_type\":\"document\"")),
        () -> assertTrue(evidenceIndex.contains(
            "\"excerpt\":\"markdown file detected: README.md\"")),
        () -> assertTrue(evidenceIndex.contains(
            "\"excerpt\":\"# Root docs\"")),
        () -> assertTrue(evidenceIndex.contains(
            "\"excerpt\":\"chunk lines 2-3; heading: Root docs\"")),
        () -> assertFalse(projectMap.contains("Body text that must not be serialized")),
        () -> assertFalse(projectMap.contains("More body text that must not be serialized")),
        () -> assertFalse(projectMap.contains("FAKE_PRIVATE_MARKDOWN_SECRET")),
        () -> assertFalse(evidenceIndex.contains("Body text that must not be serialized")),
        () -> assertFalse(evidenceIndex.contains("More body text that must not be serialized")),
        () -> assertFalse(evidenceIndex.contains("FAKE_PRIVATE_MARKDOWN_SECRET")),
        () -> assertTrue(agentGuide.contains("## Local Project Documentation")),
        () -> assertTrue(agentGuide.contains(
            "- Document inventory: detected 2 accepted default-scope Markdown documents.")),
        () -> assertTrue(agentGuide.contains(
            "  - Document: `README.md` (module: `repository-level`, discovery_source: "
                + "`root_readme`, title_source: `first_heading`, headings: `2`, chunks: `3`).")),
        () -> assertTrue(agentGuide.contains(
            "      - Chunk ref: `document_chunk:README.md:chunk:000002` heading_id "
                + "`document_heading:README.md:heading:Root%20docs:occ:000001`, "
                + "lines `2-3`, content_status `not_serialized`, evidence `README.md:2-3` "
                + "(`ev:README.md:2-3:document:chunk:000002`).")),
        () -> assertTrue(agentGuide.contains(
            "- Reconciliation hints: status `not_detected`; detected none.")),
        () -> assertFalse(agentGuide.contains("Body text that must not be serialized")),
        () -> assertFalse(agentGuide.contains("More body text that must not be serialized")),
        () -> assertFalse(agentGuide.contains("FAKE_PRIVATE_MARKDOWN_SECRET")));
  }

  @Test
  void projectMapRendersDocumentCapDiagnosticsAndBoundsOutput() throws Exception {
    Path projectPath = tempDir.resolve("document-cap-diagnostics");
    Path outputDirectory = projectPath.resolve(".project-memory");
    Files.createDirectories(outputDirectory);

    for (int index = 1; index <= 257; index++) {
      String ordinal = String.format(Locale.ROOT, "%03d", index);
      writeFile(projectPath.resolve("docs/doc-" + ordinal + ".md"), "# Doc " + ordinal + "\n");
    }

    SpringMvcEndpointOutputGenerator.Result result = generator.generate(
        projectPath,
        outputDirectory);

    String projectMap = Files.readString(outputDirectory.resolve("project-map.json"));
    String evidenceIndex = Files.readString(outputDirectory.resolve("evidence-index.jsonl"));
    JsonNode root = JSON.readTree(projectMap);
    JsonNode diagnostics = root.path("scan").path("diagnostics").path("items");
    JsonNode documents = root.path("documents").path("items");
    List<String> documentPaths = jsonTextValues(documents, "path");
    Set<String> projectMapEvidenceIds = projectMapEvidenceIds(projectMap);
    Set<String> evidenceIndexIds = evidenceIndexIds(evidenceIndex);

    assertAll(
        () -> assertTrue(result.generated()),
        () -> assertEquals(256, result.documentCount()),
        () -> assertEquals(1, result.diagnosticCount()),
        () -> assertEquals(256, documents.size()),
        () -> assertTrue(documentPaths.contains("docs/doc-256.md")),
        () -> assertFalse(documentPaths.contains("docs/doc-257.md")),
        () -> assertEquals(1, diagnostics.size()),
        () -> assertEquals(
            "local_markdown_document_count_cap_reached",
            diagnostics.get(0).path("code").asText()),
        () -> assertEquals("warning", diagnostics.get(0).path("severity").asText()),
        () -> assertEquals("documents", diagnostics.get(0).path("category").asText()),
        () -> assertEquals(256, diagnostics.get(0).path("count").asInt()),
        () -> assertTrue(diagnostics.get(0).path("path").isNull()),
        () -> assertTrue(
            evidenceIndexIds.containsAll(projectMapEvidenceIds),
            "Capped project-map evidence_ids must resolve in evidence-index.jsonl"));
  }

  @Test
  void projectMapRendersOversizedMavenPomDiagnosticAndSkipsBuildEvidence() throws Exception {
    Path projectPath = tempDir.resolve("oversized-maven-pom-diagnostics");
    Path outputDirectory = projectPath.resolve(".project-memory");
    Files.createDirectories(outputDirectory);
    writeOversizedPom(projectPath.resolve("pom.xml"));

    SpringMvcEndpointOutputGenerator.Result result = generator.generate(
        projectPath,
        outputDirectory);

    String projectMap = Files.readString(outputDirectory.resolve("project-map.json"));
    String evidenceIndex = Files.readString(outputDirectory.resolve("evidence-index.jsonl"));
    JsonNode root = JSON.readTree(projectMap);
    JsonNode diagnostics = root.path("scan").path("diagnostics").path("items");
    JsonNode build = root.path("project").path("build");
    Set<String> projectMapEvidenceIds = projectMapEvidenceIds(projectMap);
    Set<String> evidenceIndexIds = evidenceIndexIds(evidenceIndex);

    assertAll(
        () -> assertTrue(result.generated()),
        () -> assertEquals(1, result.diagnosticCount()),
        () -> assertEquals(1, diagnostics.size()),
        () -> assertEquals(
            MavenPomInput.DIAGNOSTIC_CODE_POM_BYTES_CAP_EXCEEDED,
            diagnostics.get(0).path("code").asText()),
        () -> assertEquals("warning", diagnostics.get(0).path("severity").asText()),
        () -> assertEquals("maven", diagnostics.get(0).path("category").asText()),
        () -> assertEquals("pom.xml", diagnostics.get(0).path("path").asText()),
        () -> assertEquals(MavenPomInput.MAX_POM_BYTES, diagnostics.get(0).path("count").asInt()),
        () -> assertEquals("not_detected", build.path("system").asText()),
        () -> assertTrue(build.path("root_build_file").isNull()),
        () -> assertEquals(0, build.path("evidence_ids").size()),
        () -> assertFalse(evidenceIndex.contains("ev:pom.xml:1-1:build_file:pom.xml")),
        () -> assertTrue(evidenceIndexIds.containsAll(projectMapEvidenceIds)));
  }

  @Test
  void projectMapRendersJavaSourceDiagnosticsAndSkipsSpringJpaAndTestFacts() throws Exception {
    Path projectPath = tempDir.resolve("java-source-diagnostics");
    Path outputDirectory = projectPath.resolve(".project-memory");
    Files.createDirectories(outputDirectory);
    writeOversizedJava(projectPath.resolve("src/main/java/com/example/HugeController.java"));
    writeLineBombJava(projectPath.resolve("src/main/java/com/example/LineBombService.java"));
    writeFile(projectPath.resolve("src/main/java/com/example/BrokenEntity.java"), """
        package com.example;

        public class BrokenEntity {
          void broken( {
          }
        }
        """);
    writeFile(projectPath.resolve("src/main/java/com/example/AcceptedSignals.java"), """
        package com.example;

        @org.springframework.web.bind.annotation.RestController
        class AcceptedController {
          @org.springframework.web.bind.annotation.GetMapping("/accepted")
          String accepted() {
            return "ok";
          }
        }

        @org.springframework.stereotype.Service
        class AcceptedService {
        }

        @jakarta.persistence.Entity
        class AcceptedEntity {
          @jakarta.persistence.Id
          Long id;
        }

        @org.springframework.context.annotation.Configuration
        class AcceptedConfiguration {
          @org.springframework.context.annotation.Bean
          Object acceptedBean() {
            return new Object();
          }
        }

        @org.springframework.transaction.annotation.Transactional
        class AcceptedTransactionalService {
        }

        @org.springframework.data.rest.core.annotation.RepositoryRestResource
        interface AcceptedRestRepository
            extends org.springframework.data.repository.Repository<AcceptedEntity, Long> {
        }
        """);
    writeFile(projectPath.resolve("src/test/java/com/example/AcceptedSignalsTest.java"), """
        package com.example;

        class AcceptedSignalsTest {
          @org.junit.jupiter.api.Test
          void accepted() {
          }
        }
        """);

    SpringMvcEndpointOutputGenerator.Result result = generator.generate(
        projectPath,
        outputDirectory);

    String projectMap = Files.readString(outputDirectory.resolve("project-map.json"));
    String evidenceIndex = Files.readString(outputDirectory.resolve("evidence-index.jsonl"));
    JsonNode root = JSON.readTree(projectMap);
    JsonNode diagnostics = root.path("scan").path("diagnostics").path("items");
    JsonNode testItems = root.path("tests").path("items");
    List<String> diagnosticCodes = jsonTextValues(diagnostics, "code");
    List<String> diagnosticPaths = jsonTextValues(diagnostics, "path");
    Set<String> projectMapEvidenceIds = projectMapEvidenceIds(projectMap);
    Set<String> evidenceIndexIds = evidenceIndexIds(evidenceIndex);

    assertAll(
        () -> assertTrue(result.generated()),
        () -> assertEquals(3, result.diagnosticCount()),
        () -> assertEquals(3, diagnostics.size()),
        () -> assertTrue(diagnosticCodes.contains(
            JavaSourceParser.DIAGNOSTIC_CODE_JAVA_SOURCE_FILE_BYTES_CAP_EXCEEDED)),
        () -> assertTrue(diagnosticCodes.contains(
            JavaSourceParser.DIAGNOSTIC_CODE_JAVA_SOURCE_FILE_LINES_CAP_EXCEEDED)),
        () -> assertTrue(diagnosticCodes.contains(
            JavaSourceParser.DIAGNOSTIC_CODE_JAVA_SOURCE_PARSE_ERROR)),
        () -> assertTrue(diagnosticPaths.contains("src/main/java/com/example/HugeController.java")),
        () -> assertTrue(diagnosticPaths.contains("src/main/java/com/example/LineBombService.java")),
        () -> assertTrue(diagnosticPaths.contains("src/main/java/com/example/BrokenEntity.java")),
        () -> assertEquals(0, root.path("endpoints").size()),
        () -> assertEquals(0, root.path("components").path("items").size()),
        () -> assertEquals(0, root.path("entities").path("items").size()),
        () -> assertEquals(0, root.path("entities").path("embeddables").path("items").size()),
        () -> assertEquals(0, root.path("spring_application_surface").path("repositories")
            .path("items").size()),
        () -> assertEquals(0, root.path("spring_application_surface").path("configuration")
            .path("classes").path("items").size()),
        () -> assertEquals(0, root.path("spring_application_surface").path("behavior")
            .path("transaction_boundaries").path("items").size()),
        () -> assertEquals(0, root.path("warnings").path("items").size()),
        () -> assertEquals(1, testItems.size()),
        () -> assertEquals("com.example.AcceptedSignalsTest",
            testItems.get(0).path("class_name").asText()),
        () -> assertEquals(0, testItems.get(0).path("framework_signals").size()),
        () -> assertEquals(0, testItems.get(0).path("methods").size()),
        () -> assertFalse(projectMap.contains("AcceptedController")),
        () -> assertFalse(projectMap.contains("AcceptedService")),
        () -> assertFalse(projectMap.contains("AcceptedEntity")),
        () -> assertFalse(projectMap.contains("AcceptedRestRepository")),
        () -> assertTrue(evidenceIndexIds.containsAll(projectMapEvidenceIds)));
  }

  @Test
  void documentStructureProjectMapMatchesFocusedGolden() throws Exception {
    Path projectPath = tempDir.resolve("document-structure-golden");
    Path outputDirectory = projectPath.resolve(".project-memory");
    Files.createDirectories(outputDirectory);

    writeFile(projectPath.resolve("README.md"), """
        Intro
        # Root docs
        Body text that must not be serialized
        ## API
        More body text that must not be serialized
        """);
    writeFile(projectPath.resolve("docs/guide.md"), "# Guide\n");

    SpringMvcEndpointOutputGenerator.Result result = generator.generate(
        projectPath,
        outputDirectory);

    assertAll(
        () -> assertTrue(result.generated()),
        () -> assertEquals(2, result.documentCount()),
        () -> assertEquals(
            expected("v0-8-document-structure", "project-map.json"),
            Files.readString(outputDirectory.resolve("project-map.json"))),
        () -> assertEquals(
            expected("v0-8-document-structure", "evidence-index.jsonl"),
            Files.readString(outputDirectory.resolve("evidence-index.jsonl"))),
        () -> assertEquals(
            expected("v0-8-document-structure", "agent-guide.md"),
            Files.readString(outputDirectory.resolve("agent-guide.md"))));
  }

  @Test
  void projectMapRendersDocumentReconciliationSignalsWithResolvingEvidence() throws Exception {
    Path projectPath = tempDir.resolve("document-reconciliation-golden");
    Path outputDirectory = projectPath.resolve(".project-memory");
    Files.createDirectories(outputDirectory);

    writeFile(projectPath.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
          <groupId>com.example</groupId>
          <artifactId>document-reconciliation-golden</artifactId>
          <version>1.0.0</version>
        </project>
        """);
    writeFile(projectPath.resolve("README.md"), """
        # API docs
        The `/orders` endpoint is documented.
        The `/ghost` path is only a bounded document mention.
        The billing-service module is mentioned without a source backed module fact.
        """);
    writeFile(projectPath.resolve("src/main/java/com/example/web/OrderController.java"), """
        package com.example.web;

        @org.springframework.web.bind.annotation.RestController
        class OrderController {
          @org.springframework.web.bind.annotation.GetMapping("/orders")
          String orders() {
            return "ok";
          }

          @org.springframework.web.bind.annotation.GetMapping("/internal")
          String internal() {
            return "ok";
          }
        }
        """);

    SpringMvcEndpointOutputGenerator.Result result = generator.generate(
        projectPath,
        outputDirectory);

    String projectMap = Files.readString(outputDirectory.resolve("project-map.json"));
    String evidenceIndex = Files.readString(outputDirectory.resolve("evidence-index.jsonl"));
    String agentGuide = Files.readString(outputDirectory.resolve("agent-guide.md"));
    JsonNode reconciliation = JSON.readTree(projectMap).path("documents").path("reconciliation");
    JsonNode signals = reconciliation.path("items");
    JsonNode documentOnlyEndpoint = signalWithSubject(
        signals,
        "document_only_endpoint_mention",
        "/ghost");
    JsonNode documentOnlyModule = signalWithSubject(
        signals,
        "document_only_module_reference",
        "billing-service");
    JsonNode sourceWithoutDocument = signalWithSubject(
        signals,
        "source_api_without_document_mention",
        "/internal");
    Set<String> projectMapEvidenceIds = projectMapEvidenceIds(projectMap);
    Set<String> evidenceIndexIds = evidenceIndexIds(evidenceIndex);

    assertAll(
        () -> assertTrue(result.generated()),
        () -> assertEquals(2, result.endpointCount()),
        () -> assertEquals(1, result.documentCount()),
        () -> assertEquals(
            expected("v0-8-document-reconciliation", "project-map.json"),
            projectMap),
        () -> assertEquals(
            expected("v0-8-document-reconciliation", "evidence-index.jsonl"),
            evidenceIndex),
        () -> assertEquals("analyzed", reconciliation.path("analysis_status").asText()),
        () -> assertEquals(3, signals.size()),
        () -> assertEquals(
            List.of(
                "document_only_endpoint_mention",
                "document_only_module_reference",
                "source_api_without_document_mention"),
            jsonTextValues(signals, "signal")),
        () -> assertEquals("uncertain_signal", documentOnlyEndpoint.path("status").asText()),
        () -> assertEquals("low", documentOnlyEndpoint.path("confidence").asText()),
        () -> assertEquals("bounded_endpoint_like_path_token",
            documentOnlyEndpoint.path("match_basis").asText()),
        () -> assertEquals("document:README.md", documentOnlyEndpoint.path("document_id").asText()),
        () -> assertEquals("README.md", documentOnlyEndpoint.path("document_path").asText()),
        () -> assertEquals("document_chunk:README.md:chunk:000001",
            documentOnlyEndpoint.path("document_chunk_id").asText()),
        () -> assertEquals("bounded_module_name_token",
            documentOnlyModule.path("match_basis").asText()),
        () -> assertEquals("spring_mvc_endpoint",
            sourceWithoutDocument.path("source_fact_kind").asText()),
        () -> assertEquals("endpoint:com.example.web.OrderController#internal",
            sourceWithoutDocument.path("source_fact_id").asText()),
        () -> assertTrue(sourceWithoutDocument.path("document_id").isNull()),
        () -> assertFalse(stringValues(sourceWithoutDocument.path("evidence_ids")).isEmpty()),
        () -> assertFalse(hasSignalWithSubject(
            signals,
            "document_only_endpoint_mention",
            "/orders")),
        () -> assertTrue(
            evidenceIndexIds.containsAll(projectMapEvidenceIds),
            "Every project-map evidence_ids entry must exist in evidence-index.jsonl"),
        () -> assertTrue(evidenceIndex.contains("\"symbol_name\":\"mention:/ghost\"")),
        () -> assertTrue(evidenceIndex.contains("\"excerpt\":\"mention token: /ghost\"")),
        () -> assertFalse(projectMap.contains("only a bounded document mention")),
        () -> assertFalse(evidenceIndex.contains("only a bounded document mention")),
        () -> assertEquals(
            expected("v0-8-document-reconciliation", "agent-guide.md"),
            agentGuide),
        () -> assertTrue(agentGuide.contains("## Local Project Documentation")),
        () -> assertTrue(agentGuide.contains(
            "- Reconciliation hints: status `analyzed`; detected 3 low-confidence uncertain inspection hints.")),
        () -> assertTrue(agentGuide.contains("`document_only_endpoint_mention`")),
        () -> assertTrue(agentGuide.contains("`source_api_without_document_mention`")),
        () -> assertFalse(agentGuide.contains("only a bounded document mention")),
        () -> assertFalse(agentGuide.contains("is stale")));
  }

  @Test
  void springBootApplicationsAreModuleOwnedAndEvidenceBacked() throws Exception {
    Path projectPath = tempDir.resolve("stage3-project-map");
    Path outputDirectory = projectPath.resolve(".project-memory");
    copyDirectory(fixtureRoot(), projectPath);
    Files.createDirectories(outputDirectory);

    generator.generate(projectPath, outputDirectory);

    String projectMap = Files.readString(outputDirectory.resolve("project-map.json"));
    String evidenceIndex = Files.readString(outputDirectory.resolve("evidence-index.jsonl"));
    String agentGuide = Files.readString(outputDirectory.resolve("agent-guide.md"));
    Set<String> projectMapEvidenceIds = projectMapEvidenceIds(projectMap);
    Set<String> evidenceIndexIds = evidenceIndexIds(evidenceIndex);
    JsonNode applications = moduleNode(projectMap, "module:.")
        .path("build_config")
        .path("spring_boot_applications");
    JsonNode application = applications.path("items").get(0);

    assertAll(
        () -> assertEquals("analyzed", applications.path("analysis_status").asText()),
        () -> assertEquals(1, applications.path("items").size()),
        () -> assertEquals("com.example.Stage3Application", application.path("class_name").asText()),
        () -> assertEquals(
            "src/main/java/com/example/Stage3Application.java",
            application.path("source_path").asText()),
        () -> assertEquals(
            "spring_boot_application_with_main_method",
            application.path("application_signal").asText()),
        () -> assertTrue(application.path("main_method").path("present").asBoolean()),
        () -> assertTrue(evidenceIndex.contains("\"source_type\":\"annotation\"")),
        () -> assertTrue(evidenceIndex.contains("\"symbol_name\":\"@SpringBootApplication\"")),
        () -> assertTrue(evidenceIndex.contains("\"source_type\":\"code_symbol\"")),
        () -> assertTrue(evidenceIndex.contains("\"method_name\":\"main\"")),
        () -> assertTrue(
            evidenceIndexIds.containsAll(projectMapEvidenceIds),
            "Spring Boot application evidence_ids must resolve in evidence-index.jsonl"),
        () -> assertTrue(agentGuide.contains("Spring Boot application: Detected "
            + "`com.example.Stage3Application`")),
        () -> assertSensitiveConfigValuesDoNotAppear(projectMap, evidenceIndex, agentGuide));
  }

  @Test
  void projectMapRendersSpringTestSliceAndMockSignalsWithResolvingEvidence() throws Exception {
    Path projectPath = tempDir.resolve("spring-test-slice-signals");
    Path outputDirectory = projectPath.resolve(".project-memory");
    Files.createDirectories(outputDirectory);

    writeFile(projectPath.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
          <groupId>com.example</groupId>
          <artifactId>spring-test-slice-signals</artifactId>
          <version>1.0.0</version>
        </project>
        """);
    writeFile(projectPath.resolve("src/main/java/com/example/web/OrderController.java"), """
        package com.example.web;

        class OrderController {
        }
        """);
    writeFile(projectPath.resolve("src/test/java/com/example/web/OrderControllerSlice.java"), """
        package com.example.web;

        import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
        import org.springframework.boot.test.mock.mockito.MockBean;
        import org.springframework.boot.test.mock.mockito.SpyBean;

        @WebMvcTest(OrderController.class)
        @SpyBean(OrderController.class)
        class OrderControllerSlice {
          @MockBean
          OrderController orderController;
        }
        """);

    generator.generate(projectPath, outputDirectory);

    String projectMap = Files.readString(outputDirectory.resolve("project-map.json"));
    String evidenceIndex = Files.readString(outputDirectory.resolve("evidence-index.jsonl"));
    String agentGuide = Files.readString(outputDirectory.resolve("agent-guide.md"));
    JsonNode tests = JSON.readTree(projectMap).path("tests").path("items");
    JsonNode test = objectWithText(tests, "class_name", "com.example.web.OrderControllerSlice");
    JsonNode slice = test.path("spring_test_slices").get(0);
    JsonNode fieldMockSignal = objectWithText(test.path("mock_signals"), "target_name", "orderController");
    JsonNode typeMockSignal = objectWithText(
        test.path("mock_signals"),
        "target_name",
        "com.example.web.OrderControllerSlice");
    JsonNode fieldSubject = objectWithText(test.path("tested_subjects"), "relation_type", "test_field_type");
    JsonNode sliceSubject = objectWithText(
        test.path("tested_subjects"),
        "relation_type",
        "spring_test_slice_class_literal");
    Set<String> projectMapEvidenceIds = projectMapEvidenceIds(projectMap);
    Set<String> evidenceIndexIds = evidenceIndexIds(evidenceIndex);

    assertAll(
        () -> assertEquals(1, test.path("spring_test_slices").size()),
        () -> assertEquals("@WebMvcTest", slice.path("annotation").asText()),
        () -> assertEquals("web_mvc_test", slice.path("slice_kind").asText()),
        () -> assertEquals("spring_test_slice", slice.path("signal_kind").asText()),
        () -> assertEquals("@MockBean", fieldMockSignal.path("annotation").asText()),
        () -> assertEquals("spring_boot_mockbean_annotation", fieldMockSignal.path("mock_signal").asText()),
        () -> assertEquals("field", fieldMockSignal.path("target_kind").asText()),
        () -> assertEquals("@SpyBean", typeMockSignal.path("annotation").asText()),
        () -> assertEquals("spring_boot_spybean_annotation", typeMockSignal.path("mock_signal").asText()),
        () -> assertEquals("type", typeMockSignal.path("target_kind").asText()),
        () -> assertEquals("inferred", fieldSubject.path("relation_status").asText()),
        () -> assertEquals("com.example.web.OrderController", fieldSubject.path("class_name").asText()),
        () -> assertEquals("module:.", fieldSubject.path("target_module_id").asText()),
        () -> assertTrue(fieldSubject.path("candidate_reference").isNull()),
        () -> assertEquals("inferred", sliceSubject.path("relation_status").asText()),
        () -> assertEquals("com.example.web.OrderController", sliceSubject.path("class_name").asText()),
        () -> assertEquals("module:.", sliceSubject.path("target_module_id").asText()),
        () -> assertTrue(
            evidenceIndexIds.containsAll(projectMapEvidenceIds),
            "Every project-map evidence_ids entry must exist in evidence-index.jsonl"),
        () -> assertTrue(agentGuide.contains(
            "- Spring test slice signal: Detected `@WebMvcTest` "
                + "(slice_kind: `web_mvc_test`, signal_kind: `spring_test_slice`)")),
        () -> assertTrue(agentGuide.contains(
            "- Mock annotation signal: Detected `@MockBean` on `field` `orderController` "
                + "(mock_signal: `spring_boot_mockbean_annotation`, signal_kind: `mock_annotation`)")),
        () -> assertTrue(agentGuide.contains(
            "- Inferred tested subject: `com.example.web.OrderController` in target module "
                + "`module:.` (path: `.`) (relation_status: `inferred`, relation_type: "
                + "`spring_test_slice_class_literal`, support_type: `inferred`, confidence: `medium`).")),
        () -> assertTrue(agentGuide.contains(
            "do not treat Spring test slice or mock annotations as execution or runtime behavior proof")));
  }

  @Test
  void projectMapRendersConservativeQualitySignalsWithoutCoverageClaims() throws Exception {
    Path projectPath = tempDir.resolve("quality-signals");
    Path outputDirectory = projectPath.resolve(".project-memory");
    Files.createDirectories(outputDirectory);

    writeFile(projectPath.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
          <groupId>com.example</groupId>
          <artifactId>quality-signals</artifactId>
          <version>1.0.0</version>
        </project>
        """);
    writeFile(projectPath.resolve("src/main/java/com/example/web/OrderController.java"), """
        package com.example.web;

        import org.springframework.web.bind.annotation.GetMapping;
        import org.springframework.web.bind.annotation.RestController;

        @RestController
        class OrderController {
          @GetMapping("/orders")
          String listOrders() {
            return "ok";
          }
        }
        """);
    writeFile(projectPath.resolve("src/main/java/com/example/domain/Order.java"), """
        package com.example.domain;

        import jakarta.persistence.Entity;
        import jakarta.persistence.Id;
        import jakarta.persistence.ManyToOne;

        @Entity
        class Customer {
          @Id
          Long id;
        }

        @Entity
        class Order {
          @Id
          Long id;

          @ManyToOne
          Customer customer;
        }
        """);
    writeFile(projectPath.resolve("src/main/java/com/example/repositories/OrderRepository.java"), """
        package com.example.repositories;

        import com.example.domain.Order;
        import org.springframework.data.jpa.repository.JpaRepository;

        interface OrderRepository extends JpaRepository<Order, Long> {
        }
        """);
    writeFile(projectPath.resolve("src/main/java/com/example/service/OrderService.java"), """
        package com.example.service;

        import org.springframework.kafka.annotation.KafkaListener;
        import org.springframework.scheduling.annotation.Scheduled;
        import org.springframework.stereotype.Service;
        import org.springframework.transaction.annotation.Transactional;

        @Service
        class OrderService {
          @Transactional
          void settleOrder() {
          }

          @Scheduled(fixedDelayString = "${orders.delay}")
          void refreshOrders() {
          }

          @KafkaListener(topics = "orders")
          void onOrder(String payload) {
          }
        }
        """);
    writeFile(projectPath.resolve("src/main/java/com/example/config/AppConfig.java"), """
        package com.example.config;

        import org.springframework.context.annotation.Bean;
        import org.springframework.context.annotation.Configuration;

        @Configuration
        class AppConfig {
          @Bean
          Object orderBean() {
            return new Object();
          }
        }
        """);
    writeFile(projectPath.resolve("src/main/java/com/example/security/SecurityConfig.java"), """
        package com.example.security;

        import org.springframework.context.annotation.Bean;
        import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
        import org.springframework.security.web.SecurityFilterChain;

        @EnableWebSecurity
        class SecurityConfig {
          @Bean
          SecurityFilterChain appSecurity() {
            return null;
          }
        }
        """);
    writeFile(projectPath.resolve("src/test/java/com/example/web/OrderControllerTest.java"), """
        package com.example.web;

        class OrderControllerTest {
        }
        """);

    generator.generate(projectPath, outputDirectory);

    String projectMap = Files.readString(outputDirectory.resolve("project-map.json"));
    String evidenceIndex = Files.readString(outputDirectory.resolve("evidence-index.jsonl"));
    String agentGuide = Files.readString(outputDirectory.resolve("agent-guide.md"));
    JsonNode root = JSON.readTree(projectMap);
    JsonNode quality = root.path("quality");
    JsonNode testGapItems = quality.path("test_gap_signals").path("items");
    JsonNode changeRiskItems = quality.path("change_risk_signals").path("items");
    JsonNode repositoryGap = signalWithSubject(
        testGapItems,
        "repository_without_obvious_test",
        "com.example.repositories.OrderRepository");
    JsonNode entityGap = signalWithSubject(
        testGapItems,
        "entity_without_obvious_test",
        "com.example.domain.Order");
    Set<String> projectMapEvidenceIds = projectMapEvidenceIds(projectMap);
    Set<String> evidenceIndexIds = evidenceIndexIds(evidenceIndex);

    assertAll(
        () -> assertEquals("analyzed", quality.path("analysis_status").asText()),
        () -> assertFalse(hasSignalWithSubject(
            testGapItems,
            "endpoint_without_obvious_test",
            "com.example.web.OrderController#listOrders")),
        () -> assertEquals("no_obvious_test", repositoryGap.path("status").asText()),
        () -> assertEquals(
            "no_inferred_tested_subject_relation_for_subject_class",
            repositoryGap.path("inference_basis").asText()),
        () -> assertEquals(
            "bounded_test_inventory_supported_relations_only",
            repositoryGap.path("uncertainty").asText()),
        () -> assertEquals("no_obvious_test", entityGap.path("status").asText()),
        () -> assertTrue(hasSignalWithSubject(
            changeRiskItems,
            "spring_service_change_surface",
            "com.example.service.OrderService")),
        () -> assertTrue(hasSignalWithSubject(
            changeRiskItems,
            "spring_configuration_change_surface",
            "com.example.config.AppConfig")),
        () -> assertTrue(hasSignalWithSubject(
            changeRiskItems,
            "spring_bean_method_change_surface",
            "com.example.config.AppConfig#orderBean")),
        () -> assertTrue(hasSignalWithSubject(
            changeRiskItems,
            "transaction_boundary_change_surface",
            "com.example.service.OrderService#settleOrder")),
        () -> assertTrue(hasSignalWithSubject(
            changeRiskItems,
            "scheduled_method_change_surface",
            "com.example.service.OrderService#refreshOrders")),
        () -> assertTrue(hasSignalWithSubject(
            changeRiskItems,
            "messaging_listener_change_surface",
            "com.example.service.OrderService#onOrder")),
        () -> assertTrue(hasSignalWithSubject(
            changeRiskItems,
            "jpa_relationship_change_surface",
            "com.example.domain.Order#customer")),
        () -> assertTrue(hasSignal(
            changeRiskItems,
            "spring_security_warning_change_surface")),
        () -> assertTrue(
            evidenceIndexIds.containsAll(projectMapEvidenceIds),
            "Quality signal evidence_ids must resolve in evidence-index.jsonl"),
        () -> assertTrue(agentGuide.contains("## Quality And Change-Risk Signals")),
        () -> assertTrue(agentGuide.contains(
            "No coverage, execution, assertion, CI, or runtime relation is claimed")),
        () -> assertTrue(agentGuide.contains(
            "No production impact, vulnerability, correctness, runtime behavior, or business priority is claimed")));
  }

  @Test
  void generatedEvidenceIndexBoundsOversizedEvidenceExcerpts() throws Exception {
    Path projectPath = tempDir.resolve("bounded-excerpts");
    Path outputDirectory = projectPath.resolve(".project-memory");
    Files.createDirectories(outputDirectory);
    String payload = "A".repeat(1_200);

    writeFile(projectPath.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
          <groupId>com.example</groupId>
          <artifactId>bounded-excerpts</artifactId>
          <version>1.0.0</version>
          <modules>
            <module>../%s</module>
          </modules>
          <build>
            <plugins>
              <plugin>
                <groupId>org.openapitools</groupId>
                <artifactId>openapi-generator-maven-plugin</artifactId><!--%s-->
              </plugin>
            </plugins>
          </build>
        </project>
        """.formatted(payload, payload));
    writeFile(projectPath.resolve("src/main/java/com/example/OversizedController.java"), """
        package com.example;

        import org.springframework.web.bind.annotation.GetMapping;
        import org.springframework.web.bind.annotation.RestController;

        @RestController("%s")
        class OversizedController {
          @GetMapping("/bounded")
          String bounded() {
            return "ok";
          }
        }
        """.formatted(payload));
    writeFile(projectPath.resolve("src/main/java/com/example/OversizedRepository.java"), """
        package com.example;

        import org.springframework.data.rest.core.annotation.RepositoryRestResource;

        @RepositoryRestResource(path = "%s")
        interface OversizedRepository {
        }
        """.formatted(payload));
    writeFile(projectPath.resolve("src/test/java/com/example/OversizedControllerTest.java"), """
        package com.example;

        import org.junit.jupiter.api.Test;

        class OversizedControllerTest {
          @Test(value = "%s")
          void bounded() {
          }
        }
        """.formatted(payload));

    SpringMvcEndpointOutputGenerator.Result result = generator.generate(
        projectPath,
        outputDirectory);

    String evidenceIndex = Files.readString(outputDirectory.resolve("evidence-index.jsonl"));
    String projectMap = Files.readString(outputDirectory.resolve("project-map.json"));
    List<JsonNode> evidenceRecords = evidenceRecords(evidenceIndex);
    int maxExcerptLength = EvidenceExcerpts.MAX_EXCERPT_LENGTH + 3;
    int maxEvidenceLineLength = evidenceIndex.lines()
        .mapToInt(String::length)
        .max()
        .orElse(0);
    JsonNode restControllerEvidence = evidenceRecord(
        evidenceRecords,
        "src/main/java/com/example/OversizedController.java",
        "@RestController");
    JsonNode repositoryRestEvidence = evidenceRecord(
        evidenceRecords,
        "src/main/java/com/example/OversizedRepository.java",
        "@RepositoryRestResource");
    JsonNode testAnnotationEvidence = evidenceRecord(
        evidenceRecords,
        "src/test/java/com/example/OversizedControllerTest.java",
        "@Test");
    JsonNode mavenModuleEvidence = evidenceRecords.stream()
        .filter(record -> "pom.xml".equals(record.path("path").asText()))
        .filter(record -> record.path("symbol_name").asText().startsWith("module:<invalid>:decl:"))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Missing invalid Maven module declaration evidence"));
    JsonNode mavenWarningEvidence = evidenceRecord(
        evidenceRecords,
        "pom.xml",
        "openapi-generator-maven-plugin");

    assertAll(
        () -> assertTrue(result.generated()),
        () -> assertFalse(evidenceIndex.contains(payload)),
        () -> assertFalse(projectMap.contains(payload)),
        () -> assertTrue(
            evidenceRecords.stream()
                .allMatch(record -> record.path("excerpt").asText().length() <= maxExcerptLength),
            "Every generated evidence excerpt must stay bounded"),
        () -> assertTrue(maxEvidenceLineLength < 1_000),
        () -> assertBoundedExcerpt(restControllerEvidence, "@RestController(\""),
        () -> assertBoundedExcerpt(repositoryRestEvidence, "@RepositoryRestResource(path = \""),
        () -> assertBoundedExcerpt(testAnnotationEvidence, "@Test(value = \""),
        () -> assertBoundedExcerpt(mavenModuleEvidence, "<module>../"),
        () -> assertBoundedExcerpt(mavenWarningEvidence, "<artifactId>openapi-generator-maven-plugin</artifactId>"));
  }

  @Test
  void projectMapIncludesAnalyzedComponentInventoryWithoutDroppingEndpoints() throws Exception {
    Path projectPath = tempDir.resolve("stage3-project-map");
    Path outputDirectory = projectPath.resolve(".project-memory");
    copyDirectory(fixtureRoot(), projectPath);
    Files.createDirectories(outputDirectory);

    generator.generate(projectPath, outputDirectory);

    String projectMap = Files.readString(outputDirectory.resolve("project-map.json"));
    JsonNode projectMapJson = JSON.readTree(projectMap);
    JsonNode repositories = projectMapJson
        .path("spring_application_surface")
        .path("repositories");
    JsonNode repositoryItems = repositories.path("items");
    JsonNode componentItems = projectMapJson.path("components").path("items");

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
        () -> assertFalse(projectMap.contains(
            "\"id\": \"component:com.example.repositories.ProjectOrderRepository\"")),
        () -> assertEquals("analyzed", repositories.path("analysis_status").asText()),
        () -> assertEquals(2, repositoryItems.size()),
        () -> assertEquals(
            "spring_repository_stereotype:module:.:com.example.components.InventoryRepository",
            repositoryItems.get(0).path("id").asText()),
        () -> assertEquals("spring_repository_stereotype",
            repositoryItems.get(0).path("surface_category").asText()),
        () -> assertEquals("extracted", repositoryItems.get(0).path("support_type").asText()),
        () -> assertEquals(
            "spring_data_repository_interface_signal:module:.:com.example.repositories.ProjectOrderRepository",
            repositoryItems.get(1).path("id").asText()),
        () -> assertEquals("spring_data_repository_interface_signal",
            repositoryItems.get(1).path("surface_category").asText()),
        () -> assertEquals("inferred", repositoryItems.get(1).path("support_type").asText()),
        () -> assertEquals("inferred",
            repositoryItems.get(1).path("entity_relation_status").asText()),
        () -> assertEquals(
            "repository_entity_generic",
            repositoryItems.get(1).path("entity_relation").path("relation_type").asText()),
        () -> assertEquals(
            "entity:com.example.domain.ProjectOrder",
            repositoryItems.get(1).path("entity_relation").path("target_entity_id").asText()),
        () -> assertEquals(
            "module:.",
            repositoryItems.get(1).path("entity_relation").path("target_module_id").asText()),
        () -> assertEquals(
            "com.example.domain.ProjectOrder",
            repositoryItems.get(1).path("entity_relation").path("target_class_name").asText()),
        () -> assertEquals(
            "com.example.domain.ProjectOrder",
            repositoryItems.get(1).path("entity_relation").path("generic_type").asText()),
        () -> assertEquals(
            "inferred",
            repositoryItems.get(1).path("entity_relation").path("support_type").asText()),
        () -> assertTrue(repositoryItems.get(1).path("entity_relation")
            .path("evidence_ids").toString().contains("@Entity")),
        () -> assertEquals(
            List.of("org.springframework.data.jpa.repository.JpaRepository"),
            stringValues(repositoryItems.get(1).path("extends_types"))),
        () -> assertEquals(
            List.of(
                "com.example.components.AppConfiguration",
                "com.example.components.InventoryComponent",
                "com.example.components.InventoryRepository",
                "com.example.components.InventoryService",
                "com.example.web.ProjectMapController"),
            jsonTextValues(componentItems, "class_name")),
        () -> assertTrue(projectMap.contains(
            "\"id\": \"endpoint:com.example.web.ProjectMapController#getItem\"")),
        () -> assertTrue(projectMap.contains(
            "\"id\": \"endpoint:com.example.web.ProjectMapController#createItem\"")));
  }

  @Test
  void projectMapInfersRepositoryEntityRelationsConservatively() throws Exception {
    Path fixture = Path.of(Objects.requireNonNull(
        getClass().getResource("/fixtures/v0-6-repository-entity-relations")).toURI());
    Path projectPath = tempDir.resolve("v0-6-repository-entity-relations");
    Path outputDirectory = projectPath.resolve(".project-memory");
    copyDirectory(fixture, projectPath);
    Files.createDirectories(outputDirectory);

    generator.generate(projectPath, outputDirectory);

    JsonNode repositoryItems = JSON.readTree(Files.readString(outputDirectory.resolve("project-map.json")))
        .path("spring_application_surface")
        .path("repositories")
        .path("items");
    JsonNode uniqueRepository = objectWithText(
        repositoryItems,
        "class_name",
        "com.example.repositories.UniqueOrderRepository");
    JsonNode fqcnRepository = objectWithText(
        repositoryItems,
        "class_name",
        "com.example.repositories.FqcnUniqueOrderRepository");
    JsonNode missingRepository = objectWithText(
        repositoryItems,
        "class_name",
        "com.example.repositories.MissingOrderRepository");
    JsonNode ambiguousRepository = objectWithText(
        repositoryItems,
        "class_name",
        "com.example.repositories.AmbiguousSharedOrderRepository");
    JsonNode nestedRepository = objectWithText(
        repositoryItems,
        "class_name",
        "com.example.repositories.NestedGenericOrderRepository");
    JsonNode wildcardRepository = objectWithText(
        repositoryItems,
        "class_name",
        "com.example.repositories.WildcardGenericOrderRepository");
    JsonNode rawRepository = objectWithText(
        repositoryItems,
        "class_name",
        "com.example.repositories.RawOrderRepository");

    assertAll(
        () -> assertEquals("inferred", uniqueRepository.path("entity_relation_status").asText()),
        () -> assertEquals(
            "entity:module:domain-a:com.example.unique.UniqueOrder",
            uniqueRepository.path("entity_relation").path("target_entity_id").asText()),
        () -> assertEquals(
            "module:domain-a",
            uniqueRepository.path("entity_relation").path("target_module_id").asText()),
        () -> assertEquals(
            "com.example.unique.UniqueOrder",
            uniqueRepository.path("entity_relation").path("generic_type").asText()),
        () -> assertTrue(uniqueRepository.path("entity_relation")
            .path("evidence_ids").toString().contains("@Entity")),
        () -> assertEquals("inferred", fqcnRepository.path("entity_relation_status").asText()),
        () -> assertEquals(
            "entity:module:domain-a:com.example.unique.UniqueOrder",
            fqcnRepository.path("entity_relation").path("target_entity_id").asText()),
        () -> assertEquals(
            "com.example.unique.UniqueOrder",
            fqcnRepository.path("entity_relation").path("generic_type").asText()),
        () -> assertEquals("not_detected", missingRepository.path("entity_relation_status").asText()),
        () -> assertTrue(missingRepository.path("entity_relation").isNull()),
        () -> assertEquals("ambiguous", ambiguousRepository.path("entity_relation_status").asText()),
        () -> assertTrue(ambiguousRepository.path("entity_relation").isNull()),
        () -> assertEquals("unsupported", nestedRepository.path("entity_relation_status").asText()),
        () -> assertTrue(nestedRepository.path("entity_relation").isNull()),
        () -> assertEquals("unsupported", wildcardRepository.path("entity_relation_status").asText()),
        () -> assertTrue(wildcardRepository.path("entity_relation").isNull()),
        () -> assertEquals("unsupported", rawRepository.path("entity_relation_status").asText()),
        () -> assertTrue(rawRepository.path("entity_relation").isNull()));
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
    JsonNode projectMapJson = JSON.readTree(projectMap);
    JsonNode orderEntity = objectWithText(
        projectMapJson.path("entities").path("items"),
        "class_name",
        "com.example.domain.ProjectOrder");
    JsonNode shipmentEntity = objectWithText(
        projectMapJson.path("entities").path("items"),
        "class_name",
        "com.example.domain.ProjectShipment");
    JsonNode legacyOrderEntity = objectWithText(
        projectMapJson.path("entities").path("items"),
        "class_name",
        "com.example.domain.ProjectLegacyOrder");
    JsonNode fields = orderEntity.path("fields");
    JsonNode statusField = objectWithText(fields, "field_name", "status");
    JsonNode idIdentifier = objectWithText(orderEntity.path("identifier_fields"), "field_name", "id");
    JsonNode shipmentDestination = objectWithText(shipmentEntity.path("fields"), "field_name", "destination");
    JsonNode shipmentExternalAddress = objectWithText(
        shipmentEntity.path("fields"),
        "field_name",
        "externalAddress");
    JsonNode shipmentIdIdentifier = objectWithText(shipmentEntity.path("identifier_fields"), "field_name", "id");
    JsonNode embeddables = projectMapJson.path("entities").path("embeddables").path("items");
    JsonNode projectAddress = objectWithText(embeddables, "class_name", "com.example.domain.ProjectAddress");
    JsonNode customerRelationship = objectWithText(orderEntity.path("relationships"), "field_name", "customer");
    JsonNode linesRelationship = objectWithText(orderEntity.path("relationships"), "field_name", "lines");
    JsonNode tagsRelationship = objectWithText(orderEntity.path("relationships"), "field_name", "tags");

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
        () -> assertEquals(3, fields.size()),
        () -> assertEquals(
            List.of("@Column", "@Enumerated"),
            stringValues(statusField.path("annotations"))),
        () -> assertEquals("basic", statusField.path("persistence_role").asText()),
        () -> assertEquals("status", statusField.path("column").path("name").asText()),
        () -> assertEquals(false, statusField.path("column").path("nullable").asBoolean()),
        () -> assertEquals(32, statusField.path("column").path("length").asInt()),
        () -> assertTrue(statusField.path("column").path("unique").isNull()),
        () -> assertEquals("EnumType.STRING", statusField.path("enumerated").path("value").asText()),
        () -> assertEquals("simple_id", idIdentifier.path("identifier_kind").asText()),
        () -> assertEquals(
            "GenerationType.IDENTITY",
            idIdentifier.path("generated_value").path("strategy").asText()),
        () -> assertTrue(idIdentifier.path("generated_value").path("generator").isNull()),
        () -> assertTrue(orderEntity.path("id_class").isNull()),
        () -> assertEquals("ProjectLegacyOrderKey", legacyOrderEntity.path("id_class").path("type_name").asText()),
        () -> assertEquals(
            "not_analyzed",
            legacyOrderEntity.path("id_class").path("field_matching_status").asText()),
        () -> assertEquals(
            "not_analyzed",
            legacyOrderEntity.path("id_class").path("semantic_reconstruction_status").asText()),
        () -> assertEquals("embedded_id", shipmentIdIdentifier.path("identifier_kind").asText()),
        () -> assertTrue(shipmentIdIdentifier.path("generated_value").isNull()),
        () -> assertEquals(
            "@Embedded",
            shipmentDestination.path("embedded").path("annotation").asText()),
        () -> assertEquals(
            "source_visible_embeddable",
            shipmentDestination.path("embedded").path("target_resolution").asText()),
        () -> assertEquals(
            "embeddable:com.example.domain.ProjectAddress",
            shipmentDestination.path("embedded").path("target_embeddable_id").asText()),
        () -> assertEquals(
            "module:.",
            shipmentDestination.path("embedded").path("target_module_id").asText()),
        () -> assertTrue(shipmentDestination.path("embedded").path("uncertainty").isNull()),
        () -> assertEquals(
            "declared_type_only",
            shipmentExternalAddress.path("embedded").path("target_resolution").asText()),
        () -> assertEquals(
            "embeddable_target_not_resolved",
            shipmentExternalAddress.path("embedded").path("uncertainty").asText()),
        () -> assertEquals("analyzed", projectMapJson.path("entities").path("embeddables")
            .path("analysis_status").asText()),
        () -> assertEquals("embeddable:com.example.domain.ProjectAddress", projectAddress.path("id").asText()),
        () -> assertEquals(1, projectAddress.path("fields").size()),
        () -> assertTrue(projectAddress.path("evidence_ids").toString().contains("@Embeddable")),
        () -> assertTrue(projectMap.contains("\"annotation\": \"@ManyToOne\"")),
        () -> assertTrue(projectMap.contains("\"java_type\": \"ProjectCustomer\"")),
        () -> assertTrue(projectMap.contains("\"annotation\": \"@OneToMany\"")),
        () -> assertTrue(projectMap.contains("\"java_type\": \"List<ProjectOrderLine>\"")),
        () -> assertTrue(projectMap.contains("\"annotation\": \"@OneToOne\"")),
        () -> assertTrue(projectMap.contains("\"annotation\": \"@ManyToMany\"")),
        () -> assertEquals("many_to_one", customerRelationship.path("cardinality").asText()),
        () -> assertEquals(
            "declared_type_only",
            customerRelationship.path("target").path("target_resolution").asText()),
        () -> assertEquals(
            "target_type_not_resolved",
            customerRelationship.path("target").path("uncertainty").asText()),
        () -> assertEquals("join_metadata_present", customerRelationship.path("ownership_signal").asText()),
        () -> assertFalse(customerRelationship.path("optional").asBoolean()),
        () -> assertEquals("FetchType.LAZY", customerRelationship.path("fetch").asText()),
        () -> assertEquals(
            List.of("CascadeType.PERSIST", "CascadeType.MERGE"),
            stringValues(customerRelationship.path("cascade"))),
        () -> assertEquals(
            "customer_id",
            customerRelationship.path("join_columns").get(0).path("name").asText()),
        () -> assertEquals(
            "id",
            customerRelationship.path("join_columns").get(0).path("referenced_column_name").asText()),
        () -> assertEquals("order", linesRelationship.path("mapped_by").asText()),
        () -> assertEquals("mapped_by_present", linesRelationship.path("ownership_signal").asText()),
        () -> assertEquals(true, linesRelationship.path("orphan_removal").asBoolean()),
        () -> assertEquals("order_tags", tagsRelationship.path("join_table").path("name").asText()),
        () -> assertEquals(
            "order_id",
            tagsRelationship.path("join_table").path("join_columns").get(0).path("name").asText()),
        () -> assertEquals(
            "tag_id",
            tagsRelationship.path("join_table").path("inverse_join_columns").get(0).path("name").asText()),
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
        () -> assertTrue(evidenceIndex.contains("\"source_type\":\"api_spec\"")),
        () -> assertTrue(evidenceIndex.contains("\"symbol_name\":\"openapi.yml\"")),
        () -> assertTrue(projectMap.contains("\"api_surface\": {")),
        () -> assertTrue(projectMap.contains("\"spec_path\": \"src/main/resources/openapi.yml\"")),
        () -> assertTrue(projectMap.contains("\"operations\": {\n        \"analysis_status\": \"analyzed\"")),
        () -> assertFalse(projectMap.contains("\"openapi_operation:")),
        () -> assertTrue(evidenceIndex.contains("\"symbol_name\":\"@RepositoryRestResource\"")),
        () -> assertTrue(agentGuide.contains("Warning: `hidden_http_surface` signal `openapi_spec_file`")),
        () -> assertTrue(agentGuide.contains(
            "OpenAPI/Swagger spec files: status `analyzed`; detected 1 local spec file")),
        () -> assertTrue(agentGuide.contains(
            "Warning: `hidden_http_surface` signal `repository_rest_resource`")),
        () -> assertTrue(
            evidenceIndexIds.containsAll(projectMapEvidenceIds),
            "Warning evidence_ids must resolve in evidence-index.jsonl"));
  }

  @Test
  void apiSurfaceSpecDiscoveryFactsAndOperationsAreSerializedSeparatelyFromEndpoints() throws Exception {
    Path projectPath = tempDir.resolve("api-surface-spec-project");
    Path outputDirectory = projectPath.resolve(".project-memory");
    writeFile(projectPath.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);
    writeFile(projectPath.resolve("src/main/resources/openapi.yml"), """
        openapi: 3.0.3
        paths:
          /orders:
            get:
              operationId: listOrders
        """);
    Files.createDirectories(outputDirectory);

    SpringMvcEndpointOutputGenerator.Result result = generator.generate(projectPath, outputDirectory);

    String projectMap = Files.readString(outputDirectory.resolve("project-map.json"));
    String evidenceIndex = Files.readString(outputDirectory.resolve("evidence-index.jsonl"));
    String endpoints = Files.readString(outputDirectory.resolve("endpoints.md"));
    JsonNode apiSurface = JSON.readTree(projectMap).path("api_surface");
    JsonNode specFiles = apiSurface.path("openapi").path("spec_files");
    JsonNode specFile = specFiles.path("items").get(0);
    JsonNode operations = apiSurface.path("openapi").path("operations");
    Set<String> projectMapEvidenceIds = projectMapEvidenceIds(projectMap);
    Set<String> evidenceIndexIds = evidenceIndexIds(evidenceIndex);

    assertAll(
        () -> assertTrue(result.generated()),
        () -> assertEquals(0, result.endpointCount()),
        () -> assertTrue(projectMap.contains("\"schema_version\": \"1.0\"")),
        () -> assertEquals("analyzed", apiSurface.path("analysis_status").asText()),
        () -> assertEquals("analyzed", specFiles.path("analysis_status").asText()),
        () -> assertEquals(1, specFiles.path("items").size()),
        () -> assertEquals("module:.", specFile.path("module_id").asText()),
        () -> assertEquals("src/main/resources/openapi.yml", specFile.path("spec_path").asText()),
        () -> assertEquals("yaml", specFile.path("format").asText()),
        () -> assertEquals("openapi", specFile.path("spec_kind").asText()),
        () -> assertEquals("3.0.3", specFile.path("version").asText()),
        () -> assertEquals("analyzed", operations.path("analysis_status").asText()),
        () -> assertEquals(1, operations.path("items").size()),
        () -> assertEquals(0, JSON.readTree(projectMap).path("endpoints").size()),
        () -> assertEquals("openapi_declared_operation",
            operations.path("items").get(0).path("api_surface_category").asText()),
        () -> assertEquals("GET", operations.path("items").get(0).path("http_method").asText()),
        () -> assertEquals("/orders", operations.path("items").get(0).path("path").asText()),
        () -> assertEquals("listOrders",
            operations.path("items").get(0).path("operation_id").asText()),
        () -> assertEquals("not_analyzed",
            operations.path("items").get(0).path("implementation_status").asText()),
        () -> assertTrue(evidenceIndex.contains("\"source_type\":\"api_spec\"")),
        () -> assertTrue(evidenceIndex.contains("\"symbol_name\":\"openapi\"")),
        () -> assertTrue(projectMap.contains("openapi_operation:")),
        () -> assertTrue(evidenceIndex.contains("\"symbol_name\":\"operation:get:/orders\"")),
        () -> assertTrue(endpoints.contains("## Declared OpenAPI Operations")),
        () -> assertTrue(endpoints.contains("#### Declared `GET /orders`")),
        () -> assertTrue(endpoints.contains("- Implementation status: `not_analyzed`")),
        () -> assertFalse(endpoints.contains("Implemented")),
        () -> assertTrue(evidenceIndexIds.containsAll(projectMapEvidenceIds)));
  }

  @Test
  void invalidOpenApiSpecProducesWarningWithoutEndpointOrOperationFacts() throws Exception {
    Path projectPath = tempDir.resolve("invalid-openapi-project");
    Path outputDirectory = projectPath.resolve(".project-memory");
    writeFile(projectPath.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);
    writeFile(projectPath.resolve("src/main/resources/openapi.yml"), """
        openapi: [3.0.3
        paths:
          /orders:
            get: {}
        """);
    Files.createDirectories(outputDirectory);

    SpringMvcEndpointOutputGenerator.Result result = generator.generate(projectPath, outputDirectory);

    String projectMap = Files.readString(outputDirectory.resolve("project-map.json"));
    String evidenceIndex = Files.readString(outputDirectory.resolve("evidence-index.jsonl"));
    JsonNode root = JSON.readTree(projectMap);
    JsonNode operations = root.path("api_surface").path("openapi").path("operations");
    JsonNode warnings = root.path("warnings").path("items");
    JsonNode hiddenHttpWarningIds = root.path("api_surface").path("hidden_http_warnings").path("warning_ids");
    Set<String> projectMapEvidenceIds = projectMapEvidenceIds(projectMap);
    Set<String> evidenceIndexIds = evidenceIndexIds(evidenceIndex);

    assertAll(
        () -> assertTrue(result.generated()),
        () -> assertEquals(0, result.endpointCount()),
        () -> assertEquals(0, root.path("endpoints").size()),
        () -> assertEquals("analyzed", operations.path("analysis_status").asText()),
        () -> assertEquals(0, operations.path("items").size()),
        () -> assertTrue(warnings.toString().contains("src/main/resources/openapi.yml")),
        () -> assertTrue(projectMap.contains("\"signal\": \"openapi_spec_parse_error\"")),
        () -> assertTrue(hiddenHttpWarningIds.toString().contains("openapi_spec_parse_error")),
        () -> assertTrue(evidenceIndex.contains("\"source_type\":\"api_spec\"")),
        () -> assertTrue(evidenceIndex.contains("\"symbol_name\":\"operation_parse_status:openapi_spec_parse_error\"")),
        () -> assertFalse(projectMap.contains("openapi_operation:")),
        () -> assertTrue(evidenceIndexIds.containsAll(projectMapEvidenceIds)));
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
        """
            openapi: 3.0.0
            paths:
              "/safe\\n## Forged Operation\\n  - Evidence: `ev:operation`":
                get:
                  operationId: "operation\\n## Forged Operation ID"
                  tags:
                    - "tag\\n## Forged Tag"
            """);
    writeFile(
        projectPath.resolve("docs/guide\n## Forged Doc.md"),
        """
            # Safe docs
            The `/doc-only` path is only a bounded document mention.
            """);
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
        () -> assertTrue(endpoints.contains("Forged Operation")),
        () -> assertTrue(agentGuide.contains("Fake Guide")),
        () -> assertTrue(agentGuide.contains("Forged Doc")),
        () -> assertFalse(hasLineStartingWith(endpoints, "## Forged")),
        () -> assertFalse(hasLineStartingWith(endpoints, "- Evidence: `ev:forged`")),
        () -> assertFalse(hasLineStartingWith(endpoints, "- Evidence: `ev:operation`")),
        () -> assertFalse(hasLineStartingWith(endpoints, "- Evidence: `ev:param`")),
        () -> assertFalse(hasLineStartingWith(endpoints, "  - Evidence: `ev:forged`")),
        () -> assertFalse(hasLineStartingWith(endpoints, "  - Evidence: `ev:operation`")),
        () -> assertFalse(hasLineStartingWith(endpoints, "  - Evidence: `ev:param`")),
        () -> assertFalse(hasLineStartingWith(agentGuide, "## Forged")),
        () -> assertFalse(hasLineStartingWith(agentGuide, "## Fake Guide")),
        () -> assertFalse(hasLineStartingWith(agentGuide, "## Forged Doc")),
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
        """
            openapi: 3.0.0
            paths:
              /orders/health:
                get:
                  operationId: declaredOrdersHealth
                  tags:
                    - Orders
            """);
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
        () -> assertTrue(projectMap.contains("\"schema_version\": \"1.0\"")),
        () -> assertTrue(projectMap.contains("\"api_surface_category\": \"source_visible_spring_mvc_endpoint\"")),
        () -> assertTrue(projectMap.contains("\"source_visible_spring_mvc_endpoints\": {")),
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
            "\"id\": \"openapi_operation:module:services/orders:spec:services/orders/src/main/resources/openapi.yml:operation:get:/orders/health\"")),
        () -> assertTrue(projectMap.contains("\"implementation_status\": \"not_analyzed\"")),
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
        () -> assertTrue(endpoints.contains("## Declared OpenAPI Operations")),
        () -> assertTrue(endpoints.contains("#### Declared `GET /orders/health`")),
        () -> assertFalse(endpoints.contains("Implemented")),
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
        () -> assertTrue(projectMap.contains("\"schema_version\": \"1.0\"")),
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
  void mavenMetadataIsAttachedToCorrectModulesAndEvidenceBacked() throws Exception {
    Path projectPath = tempDir.resolve("maven-metadata-project");
    Path outputDirectory = projectPath.resolve(".project-memory");
    writeFile(projectPath.resolve("pom.xml"), """
        <project>
          <modules>
            <module>services/zeta</module>
            <module>services/alpha</module>
          </modules>
        </project>
        """);
    writeFile(projectPath.resolve("services/zeta/pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
          <groupId>com.example</groupId>
          <artifactId>zeta-service</artifactId>
          <version>1.0-${revision}</version>
          <packaging>jar</packaging>
        </project>
        """);
    writeFile(projectPath.resolve("services/alpha/pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
          <parent>
            <groupId>com.example.parent</groupId>
            <artifactId>example-parent</artifactId>
            <version>${revision}</version>
          </parent>
          <artifactId>alpha-service</artifactId>
        </project>
        """);
    Files.createDirectories(outputDirectory);

    SpringMvcEndpointOutputGenerator.Result result = generator.generate(projectPath, outputDirectory);

    String projectMap = Files.readString(outputDirectory.resolve("project-map.json"));
    String evidenceIndex = Files.readString(outputDirectory.resolve("evidence-index.jsonl"));
    Set<String> projectMapEvidenceIds = projectMapEvidenceIds(projectMap);
    Set<String> evidenceIndexIds = evidenceIndexIds(evidenceIndex);
    JsonNode alphaMetadata = moduleNode(projectMap, "module:services/alpha")
        .path("build_config")
        .path("maven")
        .path("metadata");
    JsonNode zetaMetadata = moduleNode(projectMap, "module:services/zeta")
        .path("build_config")
        .path("maven")
        .path("metadata");
    JsonNode alphaBuildConfig = moduleNode(projectMap, "module:services/alpha").path("build_config");

    assertAll(
        () -> assertTrue(result.generated()),
        () -> assertTrue(projectMap.contains("\"schema_version\": \"1.0\"")),
        () -> assertTrue(projectMap.indexOf("\"module_id\": \"module:services/alpha\"")
            < projectMap.indexOf("\"module_id\": \"module:services/zeta\"")),
        () -> assertEquals("analyzed", alphaBuildConfig.path("analysis_status").asText()),
        () -> assertEquals("alpha-service", alphaMetadata.path("artifact_id").path("value").asText()),
        () -> assertTrue(alphaMetadata.path("group_id").path("value").isNull()),
        () -> assertEquals("not_declared", alphaMetadata.path("group_id").path("value_kind").asText()),
        () -> assertEquals("analyzed", alphaMetadata.path("parent").path("analysis_status").asText()),
        () -> assertEquals(
            "${revision}",
            alphaMetadata.path("parent").path("version").path("value").asText()),
        () -> assertEquals(
            "property_reference",
            alphaMetadata.path("parent").path("version").path("value_kind").asText()),
        () -> assertEquals("zeta-service", zetaMetadata.path("artifact_id").path("value").asText()),
        () -> assertEquals("1.0-${revision}", zetaMetadata.path("version").path("value").asText()),
        () -> assertEquals("expression", zetaMetadata.path("version").path("value_kind").asText()),
        () -> assertEquals("jar", zetaMetadata.path("packaging").path("value").asText()),
        () -> assertEquals(
            "analyzed",
            alphaBuildConfig.path("maven").path("dependencies").path("analysis_status").asText()),
        () -> assertTrue(evidenceIndex.contains("\"symbol_name\":\"maven:project:artifactId\"")),
        () -> assertTrue(evidenceIndex.contains("\"symbol_name\":\"maven:parent:version\"")),
        () -> assertTrue(
            evidenceIndexIds.containsAll(projectMapEvidenceIds),
            "Maven metadata evidence_ids must resolve in evidence-index.jsonl"));
  }

  @Test
  void mavenDependenciesAreAttachedToCorrectModulesAndEvidenceBacked() throws Exception {
    Path projectPath = tempDir.resolve("maven-dependencies-project");
    Path outputDirectory = projectPath.resolve(".project-memory");
    writeFile(projectPath.resolve("pom.xml"), """
        <project>
          <modules>
            <module>services/orders</module>
            <module>services/billing</module>
          </modules>
        </project>
        """);
    writeFile(projectPath.resolve("services/orders/pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
          <artifactId>orders-service</artifactId>
          <dependencyManagement>
            <dependencies>
              <dependency>
                <groupId>com.example</groupId>
                <artifactId>managed-orders-api</artifactId>
                <version>${managed.orders.version}</version>
              </dependency>
            </dependencies>
          </dependencyManagement>
          <dependencies>
            <dependency>
              <groupId>org.springframework.boot</groupId>
              <artifactId>spring-boot-starter-web</artifactId>
              <version>${spring.boot.version}</version>
            </dependency>
            <dependency>
              <groupId>com.example</groupId>
              <artifactId>orders-client</artifactId>
              <scope>test</scope>
              <optional>true</optional>
            </dependency>
          </dependencies>
        </project>
        """);
    writeFile(projectPath.resolve("services/billing/pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
          <artifactId>billing-service</artifactId>
        </project>
        """);
    Files.createDirectories(outputDirectory);

    generator.generate(projectPath, outputDirectory);

    String projectMap = Files.readString(outputDirectory.resolve("project-map.json"));
    String evidenceIndex = Files.readString(outputDirectory.resolve("evidence-index.jsonl"));
    Set<String> projectMapEvidenceIds = projectMapEvidenceIds(projectMap);
    Set<String> evidenceIndexIds = evidenceIndexIds(evidenceIndex);
    JsonNode ordersMaven = moduleNode(projectMap, "module:services/orders")
        .path("build_config")
        .path("maven");
    JsonNode billingMaven = moduleNode(projectMap, "module:services/billing")
        .path("build_config")
        .path("maven");
    JsonNode firstOrdersDependency = ordersMaven.path("dependencies").path("items").get(0);
    JsonNode secondOrdersDependency = ordersMaven.path("dependencies").path("items").get(1);
    JsonNode managedOrdersDependency = ordersMaven.path("dependency_management").path("items").get(0);

    assertAll(
        () -> assertEquals("analyzed", ordersMaven.path("dependencies").path("analysis_status").asText()),
        () -> assertEquals(2, ordersMaven.path("dependencies").path("items").size()),
        () -> assertEquals(1, ordersMaven.path("dependency_management").path("items").size()),
        () -> assertEquals("orders-client", firstOrdersDependency.path("artifact_id").path("value").asText()),
        () -> assertEquals("direct_dependency", firstOrdersDependency.path("declaration_kind").asText()),
        () -> assertEquals(2, firstOrdersDependency.path("declaration_ordinal").asInt()),
        () -> assertEquals("test", firstOrdersDependency.path("scope").path("value").asText()),
        () -> assertEquals("true", firstOrdersDependency.path("optional").path("value").asText()),
        () -> assertEquals(
            "spring-boot-starter-web",
            secondOrdersDependency.path("artifact_id").path("value").asText()),
        () -> assertEquals(
            "${spring.boot.version}",
            secondOrdersDependency.path("version").path("value").asText()),
        () -> assertEquals(
            "property_reference",
            secondOrdersDependency.path("version").path("value_kind").asText()),
        () -> assertEquals(
            "managed-orders-api",
            managedOrdersDependency.path("artifact_id").path("value").asText()),
        () -> assertEquals(
            "dependency_management",
            managedOrdersDependency.path("declaration_kind").asText()),
        () -> assertEquals(0, billingMaven.path("dependencies").path("items").size()),
        () -> assertEquals(
            "analyzed",
            billingMaven.path("dependencies").path("analysis_status").asText()),
        () -> assertTrue(evidenceIndex.contains("\"symbol_name\":\"maven:dependency:000001:groupId\"")),
        () -> assertTrue(evidenceIndex.contains(
            "\"symbol_name\":\"maven:dependency_management:000001:version\"")),
        () -> assertTrue(
            evidenceIndexIds.containsAll(projectMapEvidenceIds),
            "Maven dependency evidence_ids must resolve in evidence-index.jsonl"));
  }

  @Test
  void mavenPluginsAndGeneratedSourceWarningsAreModuleOwnedAndEvidenceBacked() throws Exception {
    Path projectPath = tempDir.resolve("maven-plugins-project");
    Path outputDirectory = projectPath.resolve(".project-memory");
    writeFile(projectPath.resolve("pom.xml"), """
        <project>
          <modules>
            <module>services/orders</module>
            <module>services/billing</module>
          </modules>
        </project>
        """);
    writeFile(projectPath.resolve("services/orders/pom.xml"), """
        <project>
          <build>
            <pluginManagement>
              <plugins>
                <plugin>
                  <groupId>org.apache.maven.plugins</groupId>
                  <artifactId>maven-compiler-plugin</artifactId>
                  <configuration>
                    <annotationProcessorPaths>
                      <path>
                        <groupId>com.example</groupId>
                        <artifactId>processor</artifactId>
                      </path>
                    </annotationProcessorPaths>
                  </configuration>
                </plugin>
              </plugins>
            </pluginManagement>
            <plugins>
              <plugin>
                <groupId>org.openapitools</groupId>
                <artifactId>openapi-generator-maven-plugin</artifactId>
                <version>${openapi.generator.version}</version>
                <executions>
                  <execution>
                    <id>generate-api</id>
                    <phase>generate-sources</phase>
                    <goals>
                      <goal>generate</goal>
                    </goals>
                    <configuration>
                      <generatedSourcesDirectory>target/generated-sources/private</generatedSourcesDirectory>
                    </configuration>
                  </execution>
                </executions>
                <configuration>
                  <inputSpec>src/main/resources/private-api.yml</inputSpec>
                </configuration>
              </plugin>
              <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>build-helper-maven-plugin</artifactId>
                <executions>
                  <execution>
                    <goals>
                      <goal>add-source</goal>
                    </goals>
                  </execution>
                </executions>
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
    Files.createDirectories(projectPath.resolve("services/orders/src/main/java"));
    Files.createDirectories(projectPath.resolve("services/billing/src/main/java"));
    Files.createDirectories(outputDirectory);

    SpringMvcEndpointOutputGenerator.Result result = generator.generate(projectPath, outputDirectory);

    String projectMap = Files.readString(outputDirectory.resolve("project-map.json"));
    String evidenceIndex = Files.readString(outputDirectory.resolve("evidence-index.jsonl"));
    Set<String> projectMapEvidenceIds = projectMapEvidenceIds(projectMap);
    Set<String> evidenceIndexIds = evidenceIndexIds(evidenceIndex);
    JsonNode ordersMaven = moduleNode(projectMap, "module:services/orders")
        .path("build_config")
        .path("maven");
    JsonNode billingMaven = moduleNode(projectMap, "module:services/billing")
        .path("build_config")
        .path("maven");
    JsonNode firstOrdersPlugin = ordersMaven.path("plugins").path("items").get(0);
    JsonNode secondOrdersPlugin = ordersMaven.path("plugins").path("items").get(1);
    JsonNode managedOrdersPlugin = ordersMaven.path("plugin_management").path("items").get(0);
    JsonNode generatedApiWarningIds = JSON.readTree(projectMap)
        .path("api_surface")
        .path("generated_source_api_signals")
        .path("warning_ids");

    assertAll(
        () -> assertTrue(result.generated()),
        () -> assertEquals(0, result.endpointCount()),
        () -> assertTrue(projectMap.contains("\"endpoints\": [],")),
        () -> assertEquals("analyzed", ordersMaven.path("plugins").path("analysis_status").asText()),
        () -> assertEquals(2, ordersMaven.path("plugins").path("items").size()),
        () -> assertEquals(1, ordersMaven.path("plugin_management").path("items").size()),
        () -> assertEquals(
            "build-helper-maven-plugin",
            firstOrdersPlugin.path("artifact_id").path("value").asText()),
        () -> assertEquals("add_source_goal_present",
            firstOrdersPlugin.path("configuration_signals").get(0).path("signal").asText()),
        () -> assertEquals(
            "openapi-generator-maven-plugin",
            secondOrdersPlugin.path("artifact_id").path("value").asText()),
        () -> assertEquals(
            "${openapi.generator.version}",
            secondOrdersPlugin.path("version").path("value").asText()),
        () -> assertEquals(
            "property_reference",
            secondOrdersPlugin.path("version").path("value_kind").asText()),
        () -> assertEquals("generate-api",
            secondOrdersPlugin.path("executions").get(0).path("execution_id").asText()),
        () -> assertEquals(
            "generate-sources",
            secondOrdersPlugin.path("executions").get(0).path("phase").path("value").asText()),
        () -> assertEquals(
            "openapi_swagger_codegen",
            secondOrdersPlugin.path("generator_signals").get(0).path("signal").asText()),
        () -> assertEquals(
            "maven-compiler-plugin",
            managedOrdersPlugin.path("artifact_id").path("value").asText()),
        () -> assertEquals(
            "plugin_management",
            managedOrdersPlugin.path("declaration_kind").asText()),
        () -> assertEquals(
            "annotation_processor",
            managedOrdersPlugin.path("generator_signals").get(0).path("signal").asText()),
        () -> assertEquals(0, billingMaven.path("plugins").path("items").size()),
        () -> assertEquals(
            "analyzed",
            billingMaven.path("plugins").path("analysis_status").asText()),
        () -> assertTrue(projectMap.contains("\"category\": \"generated_source\"")),
        () -> assertTrue(projectMap.contains("\"signal\": \"maven_openapi_swagger_codegen_plugin\"")),
        () -> assertTrue(projectMap.contains("\"signal\": \"maven_annotation_processor\"")),
        () -> assertTrue(projectMap.contains("\"signal\": \"maven_generated_source_config\"")),
        () -> assertTrue(projectMap.contains("\"signal\": \"maven_build_helper_add_source\"")),
        () -> assertTrue(projectMap.contains(
            "\"id\": \"warning:generated_source:maven_openapi_swagger_codegen_plugin:module:services/orders:direct_plugin:decl:000001\"")),
        () -> assertTrue(projectMap.contains(
            "\"id\": \"warning:generated_source:maven_annotation_processor:module:services/orders:plugin_management:decl:000001\"")),
        () -> assertEquals(
            List.of(
            "warning:generated_source:maven_generated_source_config:module:services/orders:direct_plugin:decl:000001",
                "warning:generated_source:maven_openapi_swagger_codegen_plugin:module:services/orders:direct_plugin:decl:000001",
                "warning:hidden_http_surface:maven_openapi_swagger_codegen_plugin:module:services/orders:services/orders/pom.xml:openapi-generator-maven-plugin"),
            stringValues(generatedApiWarningIds)),
        () -> assertFalse(stringValues(generatedApiWarningIds).stream()
            .anyMatch(id -> id.contains("maven_annotation_processor"))),
        () -> assertFalse(projectMap.contains("private-api.yml")),
        () -> assertFalse(projectMap.contains("target/generated-sources/private")),
        () -> assertFalse(evidenceIndex.contains("private-api.yml")),
        () -> assertFalse(evidenceIndex.contains("target/generated-sources/private")),
        () -> assertTrue(evidenceIndex.contains("\"symbol_name\":\"maven:plugin:000001:artifactId\"")),
        () -> assertTrue(evidenceIndex.contains(
            "\"symbol_name\":\"maven:plugin_management:000001:configuration:annotationProcessorPaths\"")),
        () -> assertTrue(
            evidenceIndexIds.containsAll(projectMapEvidenceIds),
            "Maven plugin evidence_ids must resolve in evidence-index.jsonl"));
  }

  @Test
  void generatedSourceRootPathWarningsAreApiSurfaceSignalsWithoutContentReads()
      throws Exception {
    Path projectPath = tempDir.resolve("generated-source-path-project");
    Path outputDirectory = projectPath.resolve(".project-memory");
    writeFile(projectPath.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);
    Files.createDirectories(projectPath.resolve("src/main/java"));
    writeFile(
        projectPath.resolve(
            "target/generated-sources/openapi/src/main/java/com/example/GeneratedApiController.java"),
        """
            package com.example;
            // FAKE_GENERATED_API_SECRET
            @org.springframework.web.bind.annotation.RestController
            class GeneratedApiController {}
            """);
    Files.createDirectories(outputDirectory);

    SpringMvcEndpointOutputGenerator.Result result = generator.generate(projectPath, outputDirectory);

    String projectMap = Files.readString(outputDirectory.resolve("project-map.json"));
    String evidenceIndex = Files.readString(outputDirectory.resolve("evidence-index.jsonl"));
    JsonNode root = JSON.readTree(projectMap);
    JsonNode warnings = root.path("warnings").path("items");
    JsonNode generatedApiWarningIds = root.path("api_surface")
        .path("generated_source_api_signals")
        .path("warning_ids");
    Set<String> projectMapEvidenceIds = projectMapEvidenceIds(projectMap);
    Set<String> evidenceIndexIds = evidenceIndexIds(evidenceIndex);

    assertAll(
        () -> assertTrue(result.generated()),
        () -> assertEquals(0, result.endpointCount()),
        () -> assertEquals(0, root.path("endpoints").size()),
        () -> assertEquals(0, root.path("api_surface").path("openapi").path("operations").path("items").size()),
        () -> assertEquals(
            List.of(
                "target/generated-sources",
                "target/generated-sources/openapi"),
            jsonTextValues(warnings, "source_path")),
        () -> assertEquals(
            List.of(
                "warning:generated_source:generated_source_root_path_detected:path:target/generated-sources",
                "warning:generated_source:generated_source_root_path_detected:path:target/generated-sources/openapi"),
            stringValues(generatedApiWarningIds)),
        () -> assertTrue(projectMap.contains("\"category\": \"generated_source\"")),
        () -> assertTrue(projectMap.contains("\"signal\": \"generated_source_root_path_detected\"")),
        () -> assertTrue(evidenceIndex.contains("\"source_type\":\"path_signal\"")),
        () -> assertTrue(evidenceIndex.contains(
            "\"symbol_name\":\"generated_source_root_path_detected\"")),
        () -> assertFalse(projectMap.contains("GeneratedApiController")),
        () -> assertFalse(evidenceIndex.contains("GeneratedApiController")),
        () -> assertFalse(projectMap.contains("FAKE_GENERATED_API_SECRET")),
        () -> assertFalse(evidenceIndex.contains("FAKE_GENERATED_API_SECRET")),
        () -> assertTrue(evidenceIndexIds.containsAll(projectMapEvidenceIds)));
  }

  @Test
  void resourceAndConfigDiscoveryIsModuleOwnedPathOnlyAndEvidenceBacked() throws Exception {
    Path projectPath = tempDir.resolve("resource-config-project");
    Path outputDirectory = projectPath.resolve(".project-memory");
    writeFile(projectPath.resolve("pom.xml"), """
        <project>
          <modules>
            <module>services/config</module>
            <module>services/empty</module>
          </modules>
        </project>
        """);
    writeFile(projectPath.resolve("services/config/pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);
    writeFile(projectPath.resolve("services/config/src/main/resources/application.yaml"), """
        datasource:
          password: FAKE_CONFIG_DB_PASSWORD
        api-token: ${FAKE_CONFIG_ENV_TOKEN}
        """);
    writeFile(projectPath.resolve("services/config/src/main/resources/application-prod.yml"), """
        secret: FAKE_CONFIG_PROFILE_SECRET
        """);
    writeFile(projectPath.resolve("services/config/src/main/resources/logback.xml"), """
        <configuration password="FAKE_CONFIG_LOGBACK_SECRET"/>
        """);
    writeFile(projectPath.resolve("services/config/src/test/resources/application-test.properties"), """
        password=FAKE_CONFIG_TEST_SECRET
        """);
    writeFile(projectPath.resolve("services/config/src/test/resources/log4j2-spring.xml"), """
        <configuration password="FAKE_CONFIG_LOG4J_SECRET"/>
        """);
    writeFile(projectPath.resolve("services/empty/pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);
    Files.createDirectories(outputDirectory);

    SpringMvcEndpointOutputGenerator.Result result = generator.generate(projectPath, outputDirectory);

    String projectMap = Files.readString(outputDirectory.resolve("project-map.json"));
    String evidenceIndex = Files.readString(outputDirectory.resolve("evidence-index.jsonl"));
    String endpoints = Files.readString(outputDirectory.resolve("endpoints.md"));
    String agentGuide = Files.readString(outputDirectory.resolve("agent-guide.md"));
    Set<String> projectMapEvidenceIds = projectMapEvidenceIds(projectMap);
    Set<String> evidenceIndexIds = evidenceIndexIds(evidenceIndex);
    JsonNode configModule = moduleNode(projectMap, "module:services/config");
    JsonNode emptyModule = moduleNode(projectMap, "module:services/empty");
    JsonNode resourceItems = configModule.path("build_config").path("resources").path("items");
    JsonNode configItems = configModule.path("build_config").path("config_files").path("items");

    assertAll(
        () -> assertTrue(result.generated()),
        () -> assertEquals("supported", configModule.path("support_status").asText()),
        () -> assertEquals("unsupported", emptyModule.path("support_status").asText()),
        () -> assertEquals(
            "analyzed",
            configModule.path("build_config").path("resources").path("analysis_status").asText()),
        () -> assertEquals(
            "analyzed",
            configModule.path("build_config").path("config_files").path("analysis_status").asText()),
        () -> assertEquals(
            "not_detected",
            emptyModule.path("build_config").path("resources").path("analysis_status").asText()),
        () -> assertEquals(
            "not_detected",
            emptyModule.path("build_config").path("config_files").path("analysis_status").asText()),
        () -> assertEquals(2, resourceItems.size()),
        () -> assertEquals("main", resourceItems.get(0).path("scope").asText()),
        () -> assertEquals("services/config/src/main/resources", resourceItems.get(0).path("path").asText()),
        () -> assertEquals("test", resourceItems.get(1).path("scope").asText()),
        () -> assertEquals("services/config/src/test/resources", resourceItems.get(1).path("path").asText()),
        () -> assertEquals(5, configItems.size()),
        () -> assertEquals(
            List.of(
                "services/config/src/main/resources/logback.xml",
                "services/config/src/main/resources/application-prod.yml",
                "services/config/src/main/resources/application.yaml",
                "services/config/src/test/resources/log4j2-spring.xml",
                "services/config/src/test/resources/application-test.properties"),
            jsonPathValues(configItems)),
        () -> assertEquals("logging_config", configItems.get(0).path("config_kind").asText()),
        () -> assertEquals("xml", configItems.get(0).path("format").asText()),
        () -> assertEquals("spring_application", configItems.get(1).path("config_kind").asText()),
        () -> assertEquals("yaml", configItems.get(1).path("format").asText()),
        () -> assertEquals("prod", configItems.get(1).path("profile_name").asText()),
        () -> assertEquals("filename_only", configItems.get(1).path("profile_source").asText()),
        () -> assertEquals("properties", configItems.get(4).path("format").asText()),
        () -> assertEquals("test", configItems.get(4).path("profile_name").asText()),
        () -> assertTrue(evidenceIndex.contains(
            "\"id\":\"ev:services/config/src/main/resources/application-prod.yml:unknown:config_file:application-prod.yml\"")),
        () -> assertTrue(evidenceIndex.contains(
            "\"excerpt\":\"config file detected: application-prod.yml\"")),
        () -> assertTrue(
            evidenceIndexIds.containsAll(projectMapEvidenceIds),
            "Config file evidence_ids must resolve in evidence-index.jsonl"),
        () -> assertSensitiveConfigValuesDoNotAppear(projectMap, evidenceIndex, endpoints, agentGuide));
  }

  @Test
  void pomOnlyAggregatorProjectGeneratesSourceVisibleMetadataOutput() throws Exception {
    Path projectPath = tempDir.resolve("pom-only-aggregator-project");
    Path outputDirectory = projectPath.resolve(".project-memory");
    writeFile(projectPath.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
          <groupId>com.example</groupId>
          <artifactId>platform</artifactId>
          <version>1.0.0</version>
          <packaging>pom</packaging>
        </project>
        """);
    Files.createDirectories(outputDirectory);

    SpringMvcEndpointOutputGenerator.Result result = generator.generate(projectPath, outputDirectory);

    String projectMap = Files.readString(outputDirectory.resolve("project-map.json"));
    String evidenceIndex = Files.readString(outputDirectory.resolve("evidence-index.jsonl"));
    JsonNode metadata = moduleNode(projectMap, "module:.")
        .path("build_config")
        .path("maven")
        .path("metadata");
    JsonNode apiSurface = JSON.readTree(projectMap).path("api_surface");

    assertAll(
        () -> assertTrue(result.generated()),
        () -> assertEquals(0, result.endpointCount()),
        () -> assertEquals(0, result.componentCount()),
        () -> assertEquals(0, result.entityCount()),
        () -> assertEquals(0, result.testCount()),
        () -> assertTrue(projectMap.contains("\"schema_version\": \"1.0\"")),
        () -> assertTrue(projectMap.contains("\"module_id\": \"module:.\"")),
        () -> assertTrue(projectMap.contains("\"support_status\": \"unsupported\"")),
        () -> assertTrue(projectMap.contains("\"build_config\": {")),
        () -> assertEquals("platform", metadata.path("artifact_id").path("value").asText()),
        () -> assertEquals("pom", metadata.path("packaging").path("value").asText()),
        () -> assertEquals("analyzed", apiSurface.path("analysis_status").asText()),
        () -> assertEquals("not_detected",
            apiSurface.path("openapi").path("operations").path("analysis_status").asText()),
        () -> assertTrue(projectMap.contains("\"endpoints\": [],")),
        () -> assertTrue(evidenceIndex.contains("\"symbol_name\":\"maven:project:packaging\"")));
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

  private List<JsonNode> evidenceRecords(String evidenceIndex) throws Exception {
    java.util.ArrayList<JsonNode> records = new java.util.ArrayList<>();
    for (String line : evidenceIndex.lines().toList()) {
      if (!line.isBlank()) {
        records.add(JSON.readTree(line));
      }
    }
    return List.copyOf(records);
  }

  private JsonNode evidenceRecord(
      List<JsonNode> evidenceRecords,
      String sourcePath,
      String symbolName) {
    return evidenceRecords.stream()
        .filter(record -> sourcePath.equals(record.path("path").asText()))
        .filter(record -> symbolName.equals(record.path("symbol_name").asText()))
        .findFirst()
        .orElseThrow(() -> new AssertionError(
            "Missing evidence record for " + sourcePath + " and " + symbolName));
  }

  private void assertBoundedExcerpt(JsonNode evidenceRecord, String expectedPrefix) {
    String excerpt = evidenceRecord.path("excerpt").asText();
    assertAll(
        () -> assertTrue(excerpt.startsWith(expectedPrefix)),
        () -> assertTrue(excerpt.endsWith("...")),
        () -> assertTrue(excerpt.length() <= EvidenceExcerpts.MAX_EXCERPT_LENGTH + 3));
  }

  private List<String> jsonPathValues(JsonNode items) {
    return jsonTextValues(items, "path");
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

  private JsonNode signalWithSubject(JsonNode items, String signal, String subjectName) {
    if (!items.isArray()) {
      throw new AssertionError("Expected array of signal objects");
    }
    for (JsonNode item : items) {
      if (signal.equals(item.path("signal").asText())
          && subjectName.equals(item.path("subject_name").asText())) {
        return item;
      }
    }
    throw new AssertionError("Missing signal " + signal + " for " + subjectName);
  }

  private boolean hasSignalWithSubject(JsonNode items, String signal, String subjectName) {
    if (!items.isArray()) {
      return false;
    }
    for (JsonNode item : items) {
      if (signal.equals(item.path("signal").asText())
          && subjectName.equals(item.path("subject_name").asText())) {
        return true;
      }
    }
    return false;
  }

  private boolean hasSignal(JsonNode items, String signal) {
    if (!items.isArray()) {
      return false;
    }
    for (JsonNode item : items) {
      if (signal.equals(item.path("signal").asText())) {
        return true;
      }
    }
    return false;
  }

  private List<String> jsonTextValues(JsonNode items, String fieldName) {
    return items.findValues(fieldName).stream()
        .map(JsonNode::asText)
        .toList();
  }

  private List<String> stringValues(JsonNode items) {
    java.util.ArrayList<String> values = new java.util.ArrayList<>();
    items.forEach(item -> values.add(item.asText()));
    return List.copyOf(values);
  }

  private void assertSensitiveConfigValuesDoNotAppear(String... generatedOutputs) {
    String joinedOutput = String.join("\n", generatedOutputs);
    assertAll(
        () -> assertFalse(joinedOutput.contains("FAKE_CONFIG_DB_PASSWORD")),
        () -> assertFalse(joinedOutput.contains("FAKE_CONFIG_ENV_TOKEN")),
        () -> assertFalse(joinedOutput.contains("FAKE_CONFIG_PROFILE_SECRET")),
        () -> assertFalse(joinedOutput.contains("FAKE_CONFIG_LOGBACK_SECRET")),
        () -> assertFalse(joinedOutput.contains("FAKE_CONFIG_TEST_SECRET")),
        () -> assertFalse(joinedOutput.contains("FAKE_CONFIG_LOG4J_SECRET")),
        () -> assertFalse(joinedOutput.contains("FAKE_STAGE3_CONFIG_SECRET")));
  }

  private JsonNode moduleNode(String projectMap, String moduleId) throws Exception {
    JsonNode modules = JSON.readTree(projectMap).path("project").path("modules").path("items");
    for (JsonNode module : modules) {
      if (moduleId.equals(module.path("module_id").asText())) {
        return module;
      }
    }
    throw new AssertionError("Missing module block for " + moduleId);
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

  private void writeOversizedPom(Path path) throws Exception {
    writeFile(path, "<project>\n<!-- " + "x".repeat(MavenPomInput.MAX_POM_BYTES) + " -->\n</project>\n");
  }

  private void writeOversizedJava(Path path) throws Exception {
    writeFile(path, "package com.example;\n// " + "x".repeat(JavaSourceParser.MAX_JAVA_SOURCE_BYTES));
  }

  private void writeLineBombJava(Path path) throws Exception {
    writeFile(
        path,
        "package com.example;\n"
            + "// line\n".repeat(JavaSourceParser.MAX_JAVA_SOURCE_LINES)
            + "class LineBombService {}\n");
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
