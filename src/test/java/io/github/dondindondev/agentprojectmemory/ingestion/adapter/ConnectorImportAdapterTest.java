package io.github.dondindondev.agentprojectmemory.ingestion.adapter;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class ConnectorImportAdapterTest {
  private static final ObjectMapper JSON = new ObjectMapper();

  @TempDir
  private Path tempDir;

  private final ConnectorImportAdapter adapter = new ConnectorImportAdapter();

  @Test
  void acceptsSupportedConnectorRecordsAndRejectsUnsafeOrUnsupportedRecords()
      throws Exception {
    Files.createDirectories(tempDir.resolve("exports"));
    Files.writeString(tempDir.resolve("exports/connectors.json"), """
        {
          "format": "agent-project-memory.connector_export.v1",
          "records": [
            {
              "provider": "jira",
              "host": "jira.example.com",
              "record_type": "issue",
              "project_key": "PROJ",
              "issue_key": "PROJ-123",
              "issue_id": "10001",
              "title": "Fake Jira issue",
              "body": "FAKE_JIRA_BODY_SECRET raw Jira issue body",
              "status": "current",
              "record_state": "open",
              "source_url": "https://jira.example.com/browse/PROJ-123",
              "exported_at": "2026-06-19T00:00:00Z",
              "record_updated_at": "2026-06-18T00:00:00Z"
            },
            {
              "provider": "youtrack",
              "host": "youtrack.example.com",
              "record_type": "issue",
              "project_key": "PROJ",
              "issue_key": "PROJ-456",
              "title": "Fake YouTrack issue",
              "body": "FAKE_YOUTRACK_ISSUE_BODY_SECRET",
              "status": "current"
            },
            {
              "provider": "youtrack",
              "host": "youtrack.example.com",
              "record_type": "article",
              "project_key": "PROJ",
              "article_id": "ABC123",
              "title": "Fake YouTrack article",
              "body": "FAKE_YOUTRACK_ARTICLE_BODY_SECRET",
              "status": "current"
            },
            {
              "provider": "confluence",
              "host": "confluence.example.com",
              "record_type": "page",
              "space_key": "ENG",
              "page_id": 123456,
              "title": "Fake Confluence page",
              "body": "FAKE_CONFLUENCE_PAGE_BODY_SECRET",
              "status": "current"
            },
            {
              "provider": "jira",
              "host": "jira.example.com",
              "record_type": "issue",
              "project_key": "PROJ",
              "issue_key": "PROJ-124",
              "body": "FAKE_STALE_BODY_SECRET",
              "status": "stale"
            },
            {
              "provider": "jira",
              "host": "jira.example.com",
              "record_type": "issue",
              "project_key": "PROJ",
              "issue_key": "PROJ-125",
              "body": "FAKE_PARTIAL_BODY_SECRET",
              "status": "partial"
            },
            {
              "provider": "trello",
              "host": "trello.example.com",
              "record_type": "card",
              "body": "FAKE_UNSUPPORTED_BODY_SECRET",
              "status": "current"
            },
            {
              "provider": "jira",
              "host": "jira.example.com",
              "record_type": "issue",
              "project_key": "PROJ",
              "issue_key": "PROJ-123",
              "body": "FAKE_DUPLICATE_BODY_SECRET",
              "status": "current"
            },
            {
              "provider": "jira",
              "host": "jira.example.com",
              "record_type": "issue",
              "project_key": "PROJ",
              "body": "FAKE_MISSING_IDENTITY_BODY_SECRET",
              "status": "current"
            },
            {
              "provider": "youtrack",
              "host": "youtrack.example.com",
              "record_type": "article",
              "project_key": "PROJ",
              "collection_key": "KB",
              "article_id": "ABC124",
              "body": "FAKE_AMBIGUOUS_BODY_SECRET",
              "status": "current"
            },
            {
              "provider": "confluence",
              "host": "confluence.example.com",
              "record_type": "page",
              "project_key": "PROJ",
              "space_key": "ENG",
              "page_id": "123457",
              "body": "FAKE_AUTHORITY_BODY_SECRET",
              "status": "current"
            },
            {
              "provider": "jira",
              "host": "jira.example.com",
              "record_type": "issue",
              "project_key": "PROJ",
              "issue_key": "PROJ-127",
              "body": "FAKE_ENCODED_URL_BODY_SECRET",
              "status": "current",
              "source_url": "https://jira.example.com/browse/%%74oken%%3DFAKE_URL_SECRET"
            },
            {
              "provider": "jira",
              "host": "jira.example.com",
              "record_type": "issue",
              "project_key": "PROJ",
              "issue_key": "PROJ-126",
              "body": "%s",
              "status": "current"
            },
            "not an object"
          ]
        }
        """.formatted("A".repeat(33 * 1024)));

    AdapterIngestionResult result = adapter.read(
        tempDir,
        AdapterLocalImport.connectorImport("exports/connectors.json"));
    String sourceRegistryJson = new SourceRegistryJsonSerializer().serialize(result);
    JsonNode sourceRegistry = JSON.readTree(sourceRegistryJson);
    List<String> sourceTypes = result.sourceDocuments().stream()
        .map(SourceDocument::sourceType)
        .toList();
    List<String> signals = result.diagnostics().stream()
        .map(AdapterDiagnostic::signal)
        .toList();

    assertAll(
        () -> assertTrue(result.enabled()),
        () -> assertEquals(4, result.acceptedCount()),
        () -> assertEquals(10, result.rejectedCount()),
        () -> assertEquals(10, result.diagnostics().size()),
        () -> assertIterableEquals(
            List.of("confluence_page", "jira_issue", "youtrack_article", "youtrack_issue"),
            sourceTypes),
        () -> assertIterableEquals(
            List.of(
                "stale_record_rejected",
                "partial_record_rejected",
                "unsupported_source_type_rejected",
                "duplicate_source_identity_rejected",
                "provenance_missing_record_rejected",
                "ambiguous_record_rejected",
                "authority_confusing_record_rejected",
                "unsafe_source_url_rejected",
                "record_body_too_large_rejected",
                "malformed_record_rejected"),
            signals),
        () -> assertEquals(
            "1.2",
            sourceRegistry.path("source_registry_schema_version").asText()),
        () -> assertEquals(
            AdapterLocalImport.CONNECTOR_IMPORT_ADAPTER,
            sourceRegistry.path("adapter_runs").get(0).path("adapter").path("name").asText()),
        () -> assertEquals(
            "connector",
            sourceRegistry.path("provenance").get(0).path("source_identity").asText()
                .split("/", -1)[0]),
        () -> assertTrue(sourceRegistryJson.contains("\"connector\": {")),
        () -> assertFalse(sourceRegistryJson.contains("FAKE_JIRA_BODY_SECRET")),
        () -> assertFalse(sourceRegistryJson.contains("raw Jira issue body")),
        () -> assertFalse(sourceRegistryJson.contains("FAKE_DUPLICATE_BODY_SECRET")),
        () -> assertFalse(sourceRegistryJson.contains("FAKE_AUTHORITY_BODY_SECRET")),
        () -> assertFalse(sourceRegistryJson.contains("FAKE_ENCODED_URL_BODY_SECRET")),
        () -> assertFalse(sourceRegistryJson.contains("FAKE_URL_SECRET")));
  }

  @Test
  void allRejectedConnectorImportStillUsesConnectorSourceRegistrySchema() throws Exception {
    Files.createDirectories(tempDir.resolve("exports"));
    Files.writeString(tempDir.resolve("exports/connectors.json"), """
        {
          "format": "agent-project-memory.connector_export.v1",
          "records": [
            {
              "provider": "jira",
              "host": "jira.example.com",
              "record_type": "issue",
              "project_key": "PROJ",
              "body": "FAKE_ALL_REJECTED_CONNECTOR_BODY_SECRET",
              "status": "current"
            }
          ]
        }
        """);

    AdapterIngestionResult result = adapter.read(
        tempDir,
        AdapterLocalImport.connectorImport("exports/connectors.json"));
    JsonNode sourceRegistry = JSON.readTree(new SourceRegistryJsonSerializer().serialize(result));

    assertAll(
        () -> assertEquals(0, result.acceptedCount()),
        () -> assertEquals(1, result.rejectedCount()),
        () -> assertEquals(
            "1.2",
            sourceRegistry.path("source_registry_schema_version").asText()),
        () -> assertEquals(0, sourceRegistry.path("source_documents").size()),
        () -> assertEquals(0, sourceRegistry.path("provenance").size()),
        () -> assertEquals(
            AdapterLocalImport.CONNECTOR_IMPORT_ADAPTER,
            sourceRegistry.path("adapter_runs").get(0).path("adapter").path("name").asText()),
        () -> assertFalse(sourceRegistry.toString().contains("FAKE_ALL_REJECTED_CONNECTOR_BODY_SECRET")));
  }
}
