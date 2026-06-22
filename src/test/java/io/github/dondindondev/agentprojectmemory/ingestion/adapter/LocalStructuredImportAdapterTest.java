package io.github.dondindondev.agentprojectmemory.ingestion.adapter;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class LocalStructuredImportAdapterTest {
  private static final ObjectMapper JSON = new ObjectMapper();

  @TempDir
  private Path tempDir;

  private final LocalStructuredImportAdapter adapter = new LocalStructuredImportAdapter();
  private final SourceRegistryJsonSerializer serializer = new SourceRegistryJsonSerializer();

  @Test
  void acceptsCurrentLocalExportRecordsWithDeterministicProvenanceAndNoBodySerialization()
      throws Exception {
    Path importFile = writeImportFile("""
        {
          "format": "agent-project-memory.local_structured_import.v1",
          "records": [
            {
              "source_type": "local_export",
              "source_identity": "issues/PM-200",
              "title": "Second record",
              "body": "FAKE_BODY_SECRET token=SHOULD_NOT_RENDER",
              "status": "current"
            },
            {
              "source_type": "local_export",
              "source_identity": "issues/PM-100",
              "title": "api_token: FAKE_TITLE_TOKEN",
              "body": "First imported body",
              "status": "current"
            }
          ]
        }
        """);

    AdapterIngestionResult result = adapter.read(
        tempDir,
        AdapterLocalImport.localStructuredImport(tempDir.relativize(importFile).toString()));
    String registryJson = serializer.serialize(result);
    JsonNode registry = JSON.readTree(registryJson);

    assertAll(
        () -> assertTrue(result.enabled()),
        () -> assertEquals(2, result.sourceDocuments().size()),
        () -> assertEquals(2, result.provenance().size()),
        () -> assertEquals(0, result.diagnostics().size()),
        () -> assertEquals("issues/PM-100", result.sourceDocuments().get(0).sourceIdentity()),
        () -> assertEquals("issues/PM-200", result.sourceDocuments().get(1).sourceIdentity()),
        () -> assertEquals(
            result.sourceDocuments().get(0).provenanceId(),
            result.provenance().get(0).id()),
        () -> assertEquals(
            "not_serialized",
            result.sourceDocuments().get(0).contentStatus().contractValue()),
        () -> assertTrue(
            result.provenance().get(0).trustBoundaryLabels().contains("not_code_evidence")),
        () -> assertEquals(
            "1.0",
            registry.path("source_registry_schema_version").asText()),
        () -> assertEquals(
            "disabled",
            registry.path("adapter_runs").get(0).path("network_access").asText()),
        () -> assertEquals(
            "repository_relative_file",
            registry.path("provenance").get(0).path("source_location_kind").asText()),
        () -> assertFalse(registryJson.contains("FAKE_BODY_SECRET")),
        () -> assertFalse(registryJson.contains("SHOULD_NOT_RENDER")),
        () -> assertFalse(registryJson.contains("FAKE_TITLE_TOKEN")),
        () -> assertTrue(registryJson.contains("[REDACTED_SECRET_LIKE_VALUE]")),
        () -> assertFalse(registryJson.contains("exports/issues.json")));
  }

  @Test
  void rejectsMalformedPartialStaleDuplicateAndUnsupportedRecordsAsDiagnostics()
      throws Exception {
    Path importFile = writeImportFile("""
        {
          "format": "agent-project-memory.local_structured_import.v1",
          "records": [
            {
              "source_type": "local_export",
              "source_identity": "issues/PM-100",
              "title": "Accepted",
              "body": "Accepted body",
              "status": "current"
            },
            {
              "source_type": "local_export",
              "source_identity": "issues/PM-101",
              "title": "Partial",
              "status": "current"
            },
            {
              "source_type": "local_export",
              "source_identity": "issues/PM-102",
              "title": "Stale",
              "body": "Stale body",
              "status": "stale"
            },
            {
              "source_type": "remote_issue",
              "source_identity": "issues/PM-103",
              "title": "Unsupported",
              "body": "Unsupported source",
              "status": "current"
            },
            {
              "source_type": "local_export",
              "source_identity": "../unsafe",
              "title": "Unsafe identity",
              "body": "Unsafe identity body",
              "status": "current"
            },
            {
              "source_type": "local_export",
              "source_identity": "issues/PM-100",
              "title": "Duplicate",
              "body": "Duplicate body",
              "status": "current"
            },
            "not an object"
          ]
        }
        """);

    AdapterIngestionResult result = adapter.read(
        tempDir,
        AdapterLocalImport.localStructuredImport(tempDir.relativize(importFile).toString()));

    assertAll(
        () -> assertEquals(1, result.sourceDocuments().size()),
        () -> assertEquals(6, result.diagnostics().size()),
        () -> assertEquals(6, result.rejectedCount()),
        () -> assertTrue(hasDiagnostic(result, "partial_record_rejected")),
        () -> assertTrue(hasDiagnostic(result, "stale_record_rejected")),
        () -> assertTrue(hasDiagnostic(result, "unsupported_source_type_rejected")),
        () -> assertTrue(hasDiagnostic(result, "provenance_missing_record_rejected")),
        () -> assertTrue(hasDiagnostic(result, "duplicate_source_identity_rejected")),
        () -> assertTrue(hasDiagnostic(result, "malformed_record_rejected")));
  }

  @Test
  void rejectsSensitiveAndPathLikeSourceIdentitiesBeforeSerialization() throws Exception {
    List<String> unsafeIdentities = List.of(
        "C:/Users/alice/.ssh/id_rsa",
        "D:Users/alice/.ssh/id_rsa",
        "\\\\fileserver\\share\\token.txt",
        "//fileserver/share/token.txt",
        "file:/Users/alice/.ssh/id_rsa",
        "file:///Users/alice/.ssh/id_rsa",
        "~/secrets/api_key",
        "/Users/alice/.ssh/id_rsa",
        "Users/alice/.ssh/id_rsa",
        "token/SHOULD_NOT_RENDER_TOKEN",
        "api_key/SHOULD_NOT_RENDER_API_KEY",
        "client-secret/SHOULD_NOT_RENDER_CLIENT_SECRET",
        "authorization/Bearer SHOULD_NOT_RENDER_AUTH",
        "projects/api_token/SHOULD_NOT_RENDER_NESTED_TOKEN",
        "api_token:SHOULD_NOT_RENDER_KEY_VALUE");
    String records = unsafeIdentities.stream()
        .map(identity -> localExportRecord(
            identity,
            "Rejected " + identity,
            "Rejected body SHOULD_NOT_RENDER_REJECTED_BODY"))
        .collect(Collectors.joining(",\n"));
    Path importFile = writeImportFile("""
        {
          "format": "agent-project-memory.local_structured_import.v1",
          "records": [
        %s,
        %s
          ]
        }
        """.formatted(
            records,
            localExportRecord(
                "services/orders/issues/PM-300",
                "Accepted issue-like identity",
                "Accepted body")));

    AdapterIngestionResult result = adapter.read(
        tempDir,
        AdapterLocalImport.localStructuredImport(tempDir.relativize(importFile).toString()));
    String registryJson = serializer.serialize(result);

    assertAll(
        () -> assertEquals(1, result.sourceDocuments().size()),
        () -> assertEquals("services/orders/issues/PM-300", result.sourceDocuments().get(0).sourceIdentity()),
        () -> assertEquals(unsafeIdentities.size(), result.diagnostics().size()),
        () -> assertEquals(unsafeIdentities.size(), result.rejectedCount()),
        () -> assertTrue(result.diagnostics().stream()
            .allMatch(diagnostic -> "provenance_missing_record_rejected".equals(diagnostic.signal()))),
        () -> assertFalse(registryJson.contains("SHOULD_NOT_RENDER")),
        () -> assertFalse(registryJson.contains("C:/Users/alice")),
        () -> assertFalse(registryJson.contains("fileserver")),
        () -> assertFalse(registryJson.contains("file:/Users")),
        () -> assertFalse(registryJson.contains("~/secrets")),
        () -> assertFalse(registryJson.contains("/Users/alice")),
        () -> assertTrue(registryJson.contains("services/orders/issues/PM-300")));
  }

  @Test
  void rejectsDelimitedSensitiveSourceIdentityFragmentsBeforeSerialization()
      throws Exception {
    List<String> unsafeIdentities = List.of(
        "token#SHOULD_NOT_RENDER_TOKEN",
        "api_key=SHOULD_NOT_RENDER_EQUALS_API_KEY",
        "api_key#SHOULD_NOT_RENDER_API_KEY",
        "api-key#SHOULD_NOT_RENDER_HYPHEN_API_KEY",
        "client-secret#SHOULD_NOT_RENDER_CLIENT_SECRET",
        "issues/PM-302=api_key:SHOULD_NOT_RENDER_EMBEDDED_EQUALS_API_KEY",
        "issues/PM-302#api_key:SHOULD_NOT_RENDER_EMBEDDED_API_KEY");
    String records = unsafeIdentities.stream()
        .map(identity -> localExportRecord(
            identity,
            "Rejected " + identity,
            "Rejected body SHOULD_NOT_RENDER_REJECTED_BODY"))
        .collect(Collectors.joining(",\n"));
    Path importFile = writeImportFile("""
        {
          "format": "agent-project-memory.local_structured_import.v1",
          "records": [
        %s,
        %s
          ]
        }
        """.formatted(
            records,
            localExportRecord(
                "services/orders/issues/PM-301",
                "Accepted issue-like identity",
                "Accepted body")));

    AdapterIngestionResult result = adapter.read(
        tempDir,
        AdapterLocalImport.localStructuredImport(tempDir.relativize(importFile).toString()));
    String registryJson = serializer.serialize(result);

    assertAll(
        () -> assertEquals(1, result.sourceDocuments().size()),
        () -> assertEquals("services/orders/issues/PM-301", result.sourceDocuments().get(0).sourceIdentity()),
        () -> assertEquals(unsafeIdentities.size(), result.diagnostics().size()),
        () -> assertEquals(unsafeIdentities.size(), result.rejectedCount()),
        () -> assertTrue(result.diagnostics().stream()
            .allMatch(diagnostic -> "provenance_missing_record_rejected".equals(diagnostic.signal()))),
        () -> assertFalse(registryJson.contains("SHOULD_NOT_RENDER")),
        () -> assertFalse(registryJson.contains("token#")),
        () -> assertFalse(registryJson.contains("api_key=")),
        () -> assertFalse(registryJson.contains("api_key#")),
        () -> assertFalse(registryJson.contains("api-key#")),
        () -> assertFalse(registryJson.contains("client-secret#")),
        () -> assertFalse(registryJson.contains("PM-302=api_key")),
        () -> assertFalse(registryJson.contains("PM-302#api_key")),
        () -> assertTrue(registryJson.contains("services/orders/issues/PM-301")));
  }

  @Test
  void capsRecordProcessingAndReportsBoundedDiagnostic() throws Exception {
    String records = IntStream.rangeClosed(1, LocalStructuredImportAdapter.MAX_RECORDS + 1)
        .mapToObj(index -> """
            {
              "source_type": "local_export",
              "source_identity": "issues/PM-%03d",
              "title": "Issue %03d",
              "body": "Body %03d",
              "status": "current"
            }
            """.formatted(index, index, index))
        .collect(Collectors.joining(",\n"));
    Path importFile = writeImportFile("""
        {
          "format": "agent-project-memory.local_structured_import.v1",
          "records": [
        %s
          ]
        }
        """.formatted(records));

    AdapterIngestionResult result = adapter.read(
        tempDir,
        AdapterLocalImport.localStructuredImport(tempDir.relativize(importFile).toString()));

    assertAll(
        () -> assertEquals(LocalStructuredImportAdapter.MAX_RECORDS, result.sourceDocuments().size()),
        () -> assertEquals(1, result.diagnostics().size()),
        () -> assertEquals(1, result.rejectedCount()),
        () -> assertTrue(hasDiagnostic(result, "record_count_cap_reached")));
  }

  @Test
  void failsClosedForMalformedTopLevelImportFileWithoutEchoingRawContent() throws Exception {
    Path importFile = writeImportFile("""
        {"format":"wrong","records":[{"body":"FAKE_RAW_IMPORT_SECRET"}]}
        """);

    IOException exception = assertThrows(
        IOException.class,
        () -> adapter.read(
            tempDir,
            AdapterLocalImport.localStructuredImport(tempDir.relativize(importFile).toString())));

    assertAll(
        () -> assertEquals("Adapter import file format is unsupported.", exception.getMessage()),
        () -> assertFalse(exception.getMessage().contains("FAKE_RAW_IMPORT_SECRET")),
        () -> assertFalse(exception.getMessage().contains(importFile.toString())));
  }

  @Test
  void failsClosedWhenImportFileExceedsBoundWithoutEchoingPath() throws Exception {
    Path importFile = writeImportFile("x".repeat(LocalStructuredImportAdapter.MAX_IMPORT_BYTES + 1));

    IOException exception = assertThrows(
        IOException.class,
        () -> adapter.read(
            tempDir,
            AdapterLocalImport.localStructuredImport(tempDir.relativize(importFile).toString())));

    assertAll(
        () -> assertEquals(
            "Adapter import file exceeds maximum supported size.",
            exception.getMessage()),
        () -> assertFalse(exception.getMessage().contains(importFile.toString())),
        () -> assertFalse(exception.getMessage().contains("exports/issues.json")));
  }

  @Test
  void rejectsDirectImportPathEscapeEvenWhenConfigurationLoaderIsBypassed() throws Exception {
    Path outsideImport = tempDir.resolveSibling(tempDir.getFileName() + "-outside-import.json");
    Files.writeString(outsideImport, """
        {
          "format": "agent-project-memory.local_structured_import.v1",
          "records": [
            {
              "source_type": "local_export",
              "source_identity": "issues/PM-999",
              "title": "Outside import",
              "body": "FAKE_OUTSIDE_IMPORT_SECRET",
              "status": "current"
            }
          ]
        }
        """);

    IOException exception = assertThrows(
        IOException.class,
        () -> adapter.read(
            tempDir,
            AdapterLocalImport.localStructuredImport("../" + outsideImport.getFileName())));

    assertAll(
        () -> assertEquals("Adapter import file could not be read.", exception.getMessage()),
        () -> assertFalse(exception.getMessage().contains(outsideImport.toString())),
        () -> assertFalse(exception.getMessage().contains("FAKE_OUTSIDE_IMPORT_SECRET")));
  }

  @Test
  void rejectsDirectHardlinkedImportFileWithoutParsingLinkedContent() throws Exception {
    Path outsideImport = tempDir.resolve("outside-hardlinked-import.json");
    Files.writeString(outsideImport, """
        {
          "format": "agent-project-memory.local_structured_import.v1",
          "records": [
            {
              "source_type": "local_export",
              "source_identity": "issues/PM-999",
              "title": "Hardlinked import",
              "body": "FAKE_HARDLINKED_IMPORT_BODY_SECRET",
              "status": "current"
            }
          ]
        }
        """);
    Path importFile = tempDir.resolve("exports/issues.json");
    Files.createDirectories(importFile.getParent());
    createHardLink(importFile, outsideImport);

    IOException exception = assertThrows(
        IOException.class,
        () -> adapter.read(tempDir, AdapterLocalImport.localStructuredImport("exports/issues.json")));

    assertAll(
        () -> assertEquals("Adapter import file could not be read.", exception.getMessage()),
        () -> assertFalse(exception.getMessage().contains(outsideImport.toString())),
        () -> assertFalse(exception.getMessage().contains("FAKE_HARDLINKED_IMPORT_BODY_SECRET")));
  }

  private boolean hasDiagnostic(AdapterIngestionResult result, String signal) {
    return result.diagnostics().stream().anyMatch(diagnostic -> signal.equals(diagnostic.signal()));
  }

  private Path writeImportFile(String content) throws Exception {
    Path exports = tempDir.resolve("exports");
    Files.createDirectories(exports);
    Path importFile = exports.resolve("issues.json");
    Files.writeString(importFile, content);
    return importFile;
  }

  private String localExportRecord(String sourceIdentity, String title, String body) {
    try {
      return """
            {
              "source_type": "local_export",
              "source_identity": %s,
              "title": %s,
              "body": %s,
              "status": "current"
            }
            """.formatted(
          JSON.writeValueAsString(sourceIdentity),
          JSON.writeValueAsString(title),
          JSON.writeValueAsString(body));
    } catch (IOException exception) {
      throw new IllegalStateException(exception);
    }
  }

  private void createHardLink(Path link, Path existing) throws Exception {
    try {
      Files.createLink(link, existing);
    } catch (UnsupportedOperationException | IOException | SecurityException exception) {
      assumeTrue(false, "hard links are unavailable: " + exception.getMessage());
    }
  }
}
