package io.github.dondindondev.agentprojectmemory.analyzer.springmvc;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.dondindondev.agentprojectmemory.scanconfig.ScanConfigPathPattern;
import io.github.dondindondev.agentprojectmemory.scanconfig.ScanConfiguration;
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

final class V10EvidenceFreezeRegressionGateTest {
  private static final ObjectMapper JSON = new ObjectMapper();
  private static final Pattern MARKDOWN_EVIDENCE_ID = Pattern.compile("`(ev:[^`]+)`");
  private static final Pattern DRIVE_LETTER_PATH = Pattern.compile("^[A-Za-z]:.*");
  private static final Set<String> ALLOWED_SOURCE_TYPES = Set.of(
      "annotation",
      "api_spec",
      "build_file",
      "code_symbol",
      "config_file",
      "document",
      "path_signal",
      "test_file");
  private static final List<String> REPRESENTATIVE_FIXTURES = List.of(
      "stage3-project-map",
      "v0-3-build-config-regression",
      "v0-4-mixed-controller-spec-api",
      "v0-4-generated-source-warning",
      "v0-7-guide-quality-regression");
  private static final List<String> SENSITIVE_EXCERPT_NEEDLES = List.of(
      "FAKE_FREEZE_TOOL_CONFIG_SECRET",
      "FAKE_FREEZE_CONFIG_PASSWORD",
      "FAKE_FREEZE_DOCUMENT_BODY",
      "FAKE_FREEZE_STACK_TRACE_VALUE",
      "java.lang.IllegalStateException",
      "/home/example/agent-project-memory",
      "Generated output contents:");

  @TempDir
  private Path tempDir;

  private final SpringMvcEndpointOutputGenerator generator = new SpringMvcEndpointOutputGenerator();

  @Test
  void generatedEvidenceKeepsClosedSourceTypesResolvingIdsAndNormalizedPaths()
      throws Exception {
    Set<String> observedSourceTypes = new LinkedHashSet<>();
    List<GeneratedOutput> outputs = new ArrayList<>();
    for (String fixtureName : REPRESENTATIVE_FIXTURES) {
      outputs.add(generateFromFixture(fixtureName));
    }
    outputs.add(generateFreezeBoundaryOutput());

    for (GeneratedOutput output : outputs) {
      List<JsonNode> evidenceRecords = evidenceRecords(output.evidenceIndex());
      observedSourceTypes.addAll(sourceTypes(evidenceRecords));
      assertEvidenceFreezeInvariants(output, evidenceRecords);
    }

    assertEquals(
        ALLOWED_SOURCE_TYPES,
        observedSourceTypes,
        "v1.0 evidence freeze should keep the current closed evidence source_type set");
  }

  @Test
  void scanMetadataToolConfigDocumentsAndReconciliationRemainOutsideEvidenceSemantics()
      throws Exception {
    GeneratedOutput output = generateFreezeBoundaryOutput();
    JsonNode projectMap = JSON.readTree(output.projectMap());
    List<JsonNode> evidenceRecords = evidenceRecords(output.evidenceIndex());
    String joinedOutput = String.join(
        "\n",
        output.projectMap(),
        output.evidenceIndex(),
        output.endpoints(),
        output.agentGuide());

    JsonNode scan = projectMap.path("scan");
    JsonNode documents = projectMap.path("documents");
    JsonNode reconciliationItems = documents.path("reconciliation").path("items");

    assertAll(
        () -> assertEquals("1.0", projectMap.path("schema_version").asText()),
        () -> assertEquals("config_file", scan.path("config").path("source").asText()),
        () -> assertEquals(
            "agent-project-memory.yml",
            scan.path("config").path("config_file_path").asText()),
        () -> assertFalse(hasEvidenceIds(scan), "scan config/diagnostics metadata is not evidence"),
        () -> assertFalse(hasToolConfigEvidence(evidenceRecords)),
        () -> assertFalse(hasScanMetadataEvidence(evidenceRecords)),
        () -> assertDocumentChunksNotSerialized(documents),
        () -> assertReconciliationSignalsRemainLowConfidenceUncertain(reconciliationItems),
        () -> assertTrue(
            hasSignalWithSubject(
                reconciliationItems,
                "document_only_endpoint_mention",
                "/document-only")),
        () -> assertTrue(
            hasSignalWithSubject(
                reconciliationItems,
                "source_api_without_document_mention",
                "/code-only")),
        () -> assertFalse(joinedOutput.contains(tempDir.toString())),
        () -> assertFalse(joinedOutput.contains("FAKE_FREEZE_TOOL_CONFIG_SECRET")),
        () -> assertFalse(joinedOutput.contains("FAKE_FREEZE_CONFIG_PASSWORD")),
        () -> assertFalse(joinedOutput.contains("FAKE_FREEZE_DOCUMENT_BODY")),
        () -> assertFalse(joinedOutput.contains("FAKE_FREEZE_STACK_TRACE_VALUE")),
        () -> assertFalse(joinedOutput.contains("docs/*.md")),
        () -> assertFalse(joinedOutput.contains("secret-*.md")));
  }

  private void assertEvidenceFreezeInvariants(
      GeneratedOutput output,
      List<JsonNode> evidenceRecords) throws Exception {
    Set<String> evidenceIds = evidenceIds(evidenceRecords);
    Set<String> projectMapEvidenceIds = referencedEvidenceIds(JSON.readTree(output.projectMap()));
    Set<String> markdownEvidenceIds = markdownEvidenceIds(output.endpoints());
    markdownEvidenceIds.addAll(markdownEvidenceIds(output.agentGuide()));

    assertAll(
        () -> assertTrue(
            evidenceIds.containsAll(projectMapEvidenceIds),
            "Every generated project-map evidence_ids entry must resolve in evidence-index.jsonl"),
        () -> assertTrue(
            evidenceIds.containsAll(markdownEvidenceIds),
            "Every generated Markdown evidence reference must resolve in evidence-index.jsonl"),
        () -> assertSourceTypesAreAllowed(evidenceRecords),
        () -> assertEvidencePathsAreRepositoryRelative(evidenceRecords),
        () -> assertEvidenceExcerptsRemainBoundedAndRedacted(evidenceRecords),
        () -> assertFalse(hasToolConfigEvidence(evidenceRecords)),
        () -> assertFalse(hasScanMetadataEvidence(evidenceRecords)));
  }

  private void assertSourceTypesAreAllowed(List<JsonNode> evidenceRecords) {
    for (JsonNode record : evidenceRecords) {
      String sourceType = record.path("source_type").asText();
      assertTrue(
          ALLOWED_SOURCE_TYPES.contains(sourceType),
          "Unexpected v1.0 evidence source_type: " + sourceType);
    }
  }

  private void assertEvidencePathsAreRepositoryRelative(List<JsonNode> evidenceRecords) {
    for (JsonNode record : evidenceRecords) {
      String path = record.path("path").asText();
      assertFalse(path.isBlank(), "Evidence path must not be blank: " + record.path("id").asText());
      assertFalse(Path.of(path).isAbsolute(), "Evidence path must be repository-relative: " + path);
      assertFalse(DRIVE_LETTER_PATH.matcher(path).matches(), "Evidence path must not be absolute: " + path);
      assertFalse(path.startsWith("./"), "Evidence path must not start with ./: " + path);
      assertFalse(path.startsWith("../"), "Evidence path must not escape the repository root: " + path);
      assertFalse(path.contains("\\"), "Evidence path must use slash separators: " + path);
      assertFalse(".".equals(path), "Evidence path must not use scan-root sentinel: " + path);
      for (String segment : path.split("/")) {
        assertFalse(segment.isEmpty(), "Evidence path must not contain empty segments: " + path);
        assertFalse(".".equals(segment), "Evidence path must not contain . segments: " + path);
        assertFalse("..".equals(segment), "Evidence path must not contain .. segments: " + path);
      }
    }
  }

  private void assertEvidenceExcerptsRemainBoundedAndRedacted(List<JsonNode> evidenceRecords) {
    for (JsonNode record : evidenceRecords) {
      String excerpt = record.path("excerpt").asText("");
      assertTrue(excerpt.length() <= 280, "Evidence excerpt must stay bounded: " + record.path("id").asText());
      for (String needle : SENSITIVE_EXCERPT_NEEDLES) {
        assertFalse(
            excerpt.contains(needle),
            "Evidence excerpt must not serialize sensitive/raw content needle: " + needle);
      }
    }
  }

  private void assertDocumentChunksNotSerialized(JsonNode documents) {
    JsonNode items = documents.path("items");
    if (!items.isArray()) {
      return;
    }
    for (JsonNode document : items) {
      JsonNode chunks = document.path("chunks");
      if (chunks.isArray()) {
        for (JsonNode chunk : chunks) {
          assertEquals("not_serialized", chunk.path("content_status").asText());
          assertFalse(chunk.has("content"), "Document chunks must not serialize content");
          assertFalse(chunk.has("body"), "Document chunks must not serialize bodies");
          assertFalse(chunk.has("text"), "Document chunks must not serialize text");
        }
      }
    }
  }

  private void assertReconciliationSignalsRemainLowConfidenceUncertain(JsonNode reconciliationItems) {
    assertTrue(reconciliationItems.isArray(), "documents.reconciliation.items must be an array");
    assertFalse(reconciliationItems.isEmpty(), "Freeze boundary fixture should emit reconciliation hints");
    for (JsonNode item : reconciliationItems) {
      assertEquals("uncertain_signal", item.path("status").asText());
      assertEquals("low", item.path("confidence").asText());
    }
  }

  private boolean hasToolConfigEvidence(List<JsonNode> evidenceRecords) {
    return evidenceRecords.stream()
        .anyMatch(record -> containsText(record, "agent-project-memory.yml")
            || "tool_config".equals(record.path("source_type").asText())
            || "scan_config".equals(record.path("source_type").asText()));
  }

  private boolean hasScanMetadataEvidence(List<JsonNode> evidenceRecords) {
    return evidenceRecords.stream()
        .anyMatch(record -> Set.of("scan", "diagnostic", "diagnostics", "scan_diagnostic")
            .contains(record.path("source_type").asText()));
  }

  private boolean containsText(JsonNode record, String needle) {
    return record.path("id").asText("").contains(needle)
        || record.path("path").asText("").contains(needle)
        || record.path("symbol_name").asText("").contains(needle)
        || record.path("excerpt").asText("").contains(needle);
  }

  private boolean hasEvidenceIds(JsonNode node) {
    if (node.isObject()) {
      var fields = node.fields();
      while (fields.hasNext()) {
        var field = fields.next();
        if (field.getKey().endsWith("evidence_ids") && field.getValue().isArray()
            && !field.getValue().isEmpty()) {
          return true;
        }
        if (hasEvidenceIds(field.getValue())) {
          return true;
        }
      }
      return false;
    }
    if (node.isArray()) {
      for (JsonNode child : node) {
        if (hasEvidenceIds(child)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean hasSignalWithSubject(JsonNode items, String signal, String subjectName) {
    for (JsonNode item : items) {
      if (signal.equals(item.path("signal").asText())
          && subjectName.equals(item.path("subject_name").asText())) {
        return true;
      }
    }
    return false;
  }

  private Set<String> sourceTypes(List<JsonNode> evidenceRecords) {
    Set<String> sourceTypes = new LinkedHashSet<>();
    evidenceRecords.forEach(record -> sourceTypes.add(record.path("source_type").asText()));
    return sourceTypes;
  }

  private Set<String> evidenceIds(List<JsonNode> evidenceRecords) {
    Set<String> ids = new LinkedHashSet<>();
    evidenceRecords.forEach(record -> ids.add(record.path("id").asText()));
    return ids;
  }

  private List<JsonNode> evidenceRecords(String evidenceIndex) throws Exception {
    List<JsonNode> records = new ArrayList<>();
    for (String line : evidenceIndex.split("\\R")) {
      if (!line.isBlank()) {
        records.add(JSON.readTree(line));
      }
    }
    return records;
  }

  private Set<String> referencedEvidenceIds(JsonNode node) {
    Set<String> ids = new LinkedHashSet<>();
    collectEvidenceIds(node, ids);
    return ids;
  }

  private void collectEvidenceIds(JsonNode node, Set<String> ids) {
    if (node.isObject()) {
      node.fields().forEachRemaining(entry -> {
        if (entry.getKey().endsWith("evidence_ids") && entry.getValue().isArray()) {
          entry.getValue().forEach(id -> ids.add(id.asText()));
        }
        collectEvidenceIds(entry.getValue(), ids);
      });
    } else if (node.isArray()) {
      node.forEach(child -> collectEvidenceIds(child, ids));
    }
  }

  private Set<String> markdownEvidenceIds(String markdown) {
    Set<String> ids = new LinkedHashSet<>();
    var matcher = MARKDOWN_EVIDENCE_ID.matcher(markdown);
    while (matcher.find()) {
      ids.add(matcher.group(1));
    }
    return ids;
  }

  private GeneratedOutput generateFromFixture(String fixtureName) throws Exception {
    Path projectPath = tempDir.resolve(fixtureName + "-" + System.nanoTime());
    Path outputDirectory = projectPath.resolve(".project-memory");
    copyDirectory(fixtureRoot(fixtureName), projectPath);
    Files.createDirectories(outputDirectory);

    SpringMvcEndpointOutputGenerator.Result result = generator.generate(projectPath, outputDirectory);

    assertTrue(result.generated());
    return readOutput(outputDirectory);
  }

  private GeneratedOutput generateFreezeBoundaryOutput() throws Exception {
    Path projectPath = tempDir.resolve("v10-evidence-freeze-" + System.nanoTime());
    Path outputDirectory = projectPath.resolve(".project-memory");
    Files.createDirectories(outputDirectory);

    writeFile(projectPath.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
          <groupId>com.example</groupId>
          <artifactId>v10-evidence-freeze</artifactId>
          <version>1.0.0</version>
        </project>
        """);
    writeFile(projectPath.resolve("agent-project-memory.yml"), """
        version: 1
        features:
          local_markdown: true
        documents:
          include:
            - docs/*.md
          exclude:
            - docs/secret-*.md
        raw_value_that_must_not_appear: FAKE_FREEZE_TOOL_CONFIG_SECRET
        """);
    writeFile(projectPath.resolve("src/main/resources/application.yml"), """
        spring:
          datasource:
            password: FAKE_FREEZE_CONFIG_PASSWORD
        """);
    writeFile(projectPath.resolve("README.md"), """
        # Evidence freeze docs
        The `/document-only` path is only a bounded document mention.
        The orders-service module is mentioned without a source-backed module fact.
        FAKE_FREEZE_DOCUMENT_BODY should stay outside generated project memory.
        java.lang.IllegalStateException: FAKE_FREEZE_STACK_TRACE_VALUE
        /home/example/agent-project-memory/private/path
        Generated output contents: {"schema_version":"forged"}
        """);
    writeFile(projectPath.resolve("src/main/java/com/example/web/FreezeController.java"), """
        package com.example.web;

        @org.springframework.web.bind.annotation.RestController
        class FreezeController {
          @org.springframework.web.bind.annotation.GetMapping("/code-only")
          String codeOnly() {
            return "ok";
          }
        }
        """);

    ScanConfiguration scanConfiguration = new ScanConfiguration(
        "config_file",
        "agent-project-memory.yml",
        "applied",
        false,
        false,
        true,
        "config_file",
        List.of(ScanConfigPathPattern.parse("docs/*.md", "documents.include[0]", true)),
        List.of(ScanConfigPathPattern.parse("docs/secret-*.md", "documents.exclude[0]", false)));

    SpringMvcEndpointOutputGenerator.Result result = generator.generate(
        projectPath,
        outputDirectory,
        scanConfiguration);

    assertTrue(result.generated());
    return readOutput(outputDirectory);
  }

  private GeneratedOutput readOutput(Path outputDirectory) throws Exception {
    return new GeneratedOutput(
        Files.readString(outputDirectory.resolve("project-map.json")),
        Files.readString(outputDirectory.resolve("evidence-index.jsonl")),
        Files.readString(outputDirectory.resolve("endpoints.md")),
        Files.readString(outputDirectory.resolve("agent-guide.md")));
  }

  private Path fixtureRoot(String fixtureName) throws Exception {
    return Path.of(Objects.requireNonNull(
        getClass().getResource("/fixtures/" + fixtureName)).toURI());
  }

  private void writeFile(Path path, String content) throws Exception {
    Files.createDirectories(path.getParent());
    Files.writeString(path, content);
  }

  private void copyDirectory(Path source, Path target) throws Exception {
    try (var paths = Files.walk(source)) {
      for (Path sourcePath : paths.toList()) {
        Path targetPath = target.resolve(source.relativize(sourcePath).toString());
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
