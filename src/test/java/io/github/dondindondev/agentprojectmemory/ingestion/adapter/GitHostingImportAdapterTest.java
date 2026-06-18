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
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class GitHostingImportAdapterTest {
  private static final ObjectMapper JSON = new ObjectMapper();

  @TempDir
  private Path tempDir;

  private final GitHostingImportAdapter adapter = new GitHostingImportAdapter();
  private final SourceRegistryJsonSerializer serializer = new SourceRegistryJsonSerializer();

  @Test
  void acceptsGitHubAndGitLabRecordsWithDeterministicProvenanceAndNoRawTextSerialization()
      throws Exception {
    Path importFile = writeImportFile("""
        {
          "format": "agent-project-memory.git_hosting_export.v1",
          "records": [
            {
              "provider": "github",
              "host": "GitHub.COM",
              "namespace": "Owner/Repo",
              "record_type": "issue",
              "number": 123,
              "title": "api_token: FAKE_TITLE_TOKEN",
              "body": "FAKE_GITHUB_BODY_SECRET token=SHOULD_NOT_RENDER",
              "status": "current",
              "record_state": "open",
              "source_url": "https://github.com/owner/repo/issues/123",
              "exported_at": "2026-06-18T00:00:00Z",
              "record_updated_at": "2026-06-17T12:30:00+03:00"
            },
            {
              "provider": "gitlab",
              "host": "gitlab.example.test",
              "namespace": "Group/Subgroup/Project",
              "record_type": "merge_request",
              "iid": "88",
              "title": "Fake merge request",
              "body": "FAKE_GITLAB_BODY_SECRET",
              "status": "current",
              "record_state": "merged",
              "source_url": "https://gitlab.example.test/group/subgroup/project/-/merge_requests/88",
              "exported_at": "2026-06-18T01:00:00Z",
              "record_updated_at": "2026-06-18T02:00:00Z"
            }
          ]
        }
        """);

    AdapterIngestionResult result = adapter.read(
        tempDir,
        AdapterLocalImport.gitHostingImport(tempDir.relativize(importFile).toString()));
    String registryJson = serializer.serialize(result);
    JsonNode registry = JSON.readTree(registryJson);
    JsonNode githubProvenance = registry.path("provenance").get(0);
    JsonNode gitlabProvenance = registry.path("provenance").get(1);

    assertAll(
        () -> assertTrue(result.enabled()),
        () -> assertEquals(2, result.sourceDocuments().size()),
        () -> assertEquals(2, result.provenance().size()),
        () -> assertEquals(0, result.diagnostics().size()),
        () -> assertEquals(
            "git-hosting/github/github.com/owner/repo/issue/123",
            result.sourceDocuments().get(0).sourceIdentity()),
        () -> assertEquals(
            "git-hosting/gitlab/gitlab.example.test/group/subgroup/project/merge_request/88",
            result.sourceDocuments().get(1).sourceIdentity()),
        () -> assertEquals("1.1", registry.path("source_registry_schema_version").asText()),
        () -> assertEquals(
            AdapterLocalImport.GIT_HOSTING_IMPORT_ADAPTER,
            registry.path("adapter_runs").get(0).path("adapter").path("name").asText()),
        () -> assertEquals("disabled", registry.path("adapter_runs").get(0).path("network_access").asText()),
        () -> assertEquals("github_issue", githubProvenance.path("source_type").asText()),
        () -> assertEquals("github", githubProvenance.path("git_hosting").path("provider").asText()),
        () -> assertEquals("github.com", githubProvenance.path("git_hosting").path("host").asText()),
        () -> assertEquals("owner/repo", githubProvenance.path("git_hosting").path("namespace").asText()),
        () -> assertEquals("issue", githubProvenance.path("git_hosting").path("record_type").asText()),
        () -> assertEquals("123", githubProvenance.path("git_hosting").path("record_number").asText()),
        () -> assertEquals("open", githubProvenance.path("git_hosting").path("record_state").asText()),
        () -> assertEquals(
            "2026-06-17T09:30:00Z",
            githubProvenance.path("git_hosting").path("record_updated_at").asText()),
        () -> assertEquals(
            "gitlab_merge_request",
            gitlabProvenance.path("source_type").asText()),
        () -> assertTrue(githubProvenance.path("trust_boundary_labels").toString()
            .contains("not_code_evidence")),
        () -> assertFalse(registryJson.contains("FAKE_GITHUB_BODY_SECRET")),
        () -> assertFalse(registryJson.contains("FAKE_GITLAB_BODY_SECRET")),
        () -> assertFalse(registryJson.contains("SHOULD_NOT_RENDER")),
        () -> assertFalse(registryJson.contains("FAKE_TITLE_TOKEN")),
        () -> assertTrue(registryJson.contains("[REDACTED_SECRET_LIKE_VALUE]")),
        () -> assertFalse(registryJson.contains("exports/git-hosting.json")));
  }

  @Test
  void validatesSourceUrlAgainstExactProviderPathShape() throws Exception {
    Path importFile = writeImportFile("""
        {
          "format": "agent-project-memory.git_hosting_export.v1",
          "records": [
            {
              "provider": "github",
              "host": "github.com",
              "namespace": "owner/repo",
              "record_type": "issue",
              "number": 101,
              "title": "Valid GitHub issue URL",
              "status": "current",
              "source_url": "https://github.com/owner/repo/issues/101"
            },
            {
              "provider": "github",
              "host": "github.com",
              "namespace": "owner/repo",
              "record_type": "pull_request",
              "number": 102,
              "title": "Valid GitHub pull URL",
              "status": "current",
              "source_url": "https://github.com/owner/repo/pull/102"
            },
            {
              "provider": "gitlab",
              "host": "gitlab.example.test",
              "namespace": "group/subgroup/project",
              "record_type": "issue",
              "iid": 201,
              "title": "Valid GitLab issue URL",
              "status": "current",
              "source_url": "https://gitlab.example.test/group/subgroup/project/-/issues/201"
            },
            {
              "provider": "gitlab",
              "host": "gitlab.example.test",
              "namespace": "group/subgroup/project",
              "record_type": "merge_request",
              "iid": 202,
              "title": "Valid GitLab merge request URL",
              "status": "current",
              "source_url": "https://gitlab.example.test/group/subgroup/project/-/merge_requests/202"
            },
            {
              "provider": "github",
              "host": "github.com",
              "namespace": "owner/repo",
              "record_type": "issue",
              "number": 301,
              "title": "Namespace collision",
              "status": "current",
              "source_url": "https://github.com/evilowner/repo/issues/301"
            },
            {
              "provider": "github",
              "host": "github.com",
              "namespace": "owner/repo",
              "record_type": "pull_request",
              "number": 302,
              "title": "Wrong GitHub record type",
              "status": "current",
              "source_url": "https://github.com/owner/repo/issues/302"
            },
            {
              "provider": "github",
              "host": "github.com",
              "namespace": "owner/repo",
              "record_type": "issue",
              "number": 303,
              "title": "Wrong GitHub record number",
              "status": "current",
              "source_url": "https://github.com/owner/repo/issues/999"
            },
            {
              "provider": "gitlab",
              "host": "gitlab.example.test",
              "namespace": "group/subgroup/project",
              "record_type": "merge_request",
              "iid": 402,
              "title": "Wrong GitLab record type",
              "status": "current",
              "source_url": "https://gitlab.example.test/group/subgroup/project/-/issues/402"
            },
            {
              "provider": "gitlab",
              "host": "gitlab.example.test",
              "namespace": "group/subgroup/project",
              "record_type": "issue",
              "iid": 403,
              "title": "Wrong GitLab IID",
              "status": "current",
              "source_url": "https://gitlab.example.test/group/subgroup/project/-/issues/404"
            }
          ]
        }
        """);

    AdapterIngestionResult result = adapter.read(
        tempDir,
        AdapterLocalImport.gitHostingImport(tempDir.relativize(importFile).toString()));
    String registryJson = serializer.serialize(result);

    assertAll(
        () -> assertEquals(4, result.sourceDocuments().size()),
        () -> assertEquals(5, result.diagnostics().size()),
        () -> assertEquals(5, result.rejectedCount()),
        () -> assertEquals(
            5,
            result.diagnostics().stream()
                .filter(diagnostic -> "unsafe_source_url_rejected".equals(diagnostic.signal()))
                .count()),
        () -> assertTrue(registryJson.contains("https://github.com/owner/repo/issues/101")),
        () -> assertTrue(registryJson.contains("https://github.com/owner/repo/pull/102")),
        () -> assertTrue(registryJson.contains(
            "https://gitlab.example.test/group/subgroup/project/-/issues/201")),
        () -> assertTrue(registryJson.contains(
            "https://gitlab.example.test/group/subgroup/project/-/merge_requests/202")),
        () -> assertFalse(registryJson.contains("evilowner/repo")),
        () -> assertFalse(registryJson.contains("issues/302")),
        () -> assertFalse(registryJson.contains("issues/999")),
        () -> assertFalse(registryJson.contains("issues/402")),
        () -> assertFalse(registryJson.contains("issues/404")));
  }

  @Test
  void rejectsMalformedPartialStaleDuplicateUnsupportedOversizedAmbiguousAndUnsafeRecords()
      throws Exception {
    String oversizedBody = "x".repeat(32 * 1024 + 1);
    Path importFile = writeImportFile("""
        {
          "format": "agent-project-memory.git_hosting_export.v1",
          "records": [
            {
              "provider": "github",
              "host": "github.com",
              "namespace": "owner/repo",
              "record_type": "pull_request",
              "number": 7,
              "title": "Accepted pull request",
              "body": "Accepted body",
              "status": "current"
            },
            {
              "provider": "github",
              "host": "github.com",
              "namespace": "owner/repo",
              "record_type": "issue",
              "number": 8,
              "title": "Partial",
              "status": "partial"
            },
            {
              "provider": "github",
              "host": "github.com",
              "namespace": "owner/repo",
              "record_type": "issue",
              "number": 9,
              "title": "Stale",
              "status": "stale"
            },
            {
              "provider": "bitbucket",
              "host": "bitbucket.example.test",
              "namespace": "owner/repo",
              "record_type": "issue",
              "number": 10,
              "title": "Unsupported provider",
              "status": "current"
            },
            {
              "provider": "gitlab",
              "host": "gitlab.example.test",
              "namespace": "group/project",
              "record_type": "pull_request",
              "iid": 11,
              "title": "Unsupported record type",
              "status": "current"
            },
            {
              "provider": "github",
              "host": "file:/Users/alice",
              "namespace": "owner/repo",
              "record_type": "issue",
              "number": 12,
              "title": "Unsafe host",
              "status": "current"
            },
            {
              "provider": "github",
              "host": "github.com",
              "namespace": "owner/repo",
              "record_type": "pull_request",
              "number": 7,
              "title": "Duplicate",
              "status": "current"
            },
            {
              "provider": "github",
              "host": "github.com",
              "namespace": "owner/repo",
              "record_type": "issue",
              "number": 13,
              "iid": 13,
              "title": "Ambiguous",
              "status": "current"
            },
            {
              "provider": "github",
              "host": "github.com",
              "namespace": "owner/repo",
              "record_type": "issue",
              "number": 14,
              "title": "Unsafe URL",
              "status": "current",
              "source_url": "https://token:SHOULD_NOT_RENDER@github.com/owner/repo/issues/14?token=FAKE"
            },
            {
              "provider": "github",
              "host": "github.com",
              "namespace": "owner/repo",
              "record_type": "issue",
              "number": 15,
              "title": "Oversized",
              "body": %s,
              "status": "current"
            },
            {
              "provider": "github",
              "host": "github.com",
              "namespace": "owner/repo",
              "record_type": "issue",
              "number": 16,
              "title": "Unsupported shape",
              "status": "current",
              "raw": "FAKE_RAW_OBJECT_SECRET"
            },
            "not an object"
          ]
        }
        """.formatted(JSON.writeValueAsString(oversizedBody)));

    AdapterIngestionResult result = adapter.read(
        tempDir,
        AdapterLocalImport.gitHostingImport(tempDir.relativize(importFile).toString()));
    String registryJson = serializer.serialize(result);

    assertAll(
        () -> assertEquals(1, result.sourceDocuments().size()),
        () -> assertEquals(11, result.diagnostics().size()),
        () -> assertEquals(11, result.rejectedCount()),
        () -> assertTrue(hasDiagnostic(result, "partial_record_rejected")),
        () -> assertTrue(hasDiagnostic(result, "stale_record_rejected")),
        () -> assertTrue(hasDiagnostic(result, "unsupported_source_type_rejected")),
        () -> assertTrue(hasDiagnostic(result, "provenance_missing_record_rejected")),
        () -> assertTrue(hasDiagnostic(result, "duplicate_source_identity_rejected")),
        () -> assertTrue(hasDiagnostic(result, "ambiguous_record_rejected")),
        () -> assertTrue(hasDiagnostic(result, "unsafe_source_url_rejected")),
        () -> assertTrue(hasDiagnostic(result, "record_body_too_large_rejected")),
        () -> assertTrue(hasDiagnostic(result, "unsupported_record_shape_rejected")),
        () -> assertTrue(hasDiagnostic(result, "malformed_record_rejected")),
        () -> assertFalse(registryJson.contains("FAKE_RAW_OBJECT_SECRET")),
        () -> assertFalse(registryJson.contains("SHOULD_NOT_RENDER")),
        () -> assertFalse(registryJson.contains("file:/Users/alice")),
        () -> assertTrue(registryJson.contains("git-hosting/github/github.com/owner/repo/pull_request/7")));
  }

  @Test
  void capsRecordProcessingAndReportsBoundedDiagnostic() throws Exception {
    String records = IntStream.rangeClosed(1, GitHostingImportAdapter.MAX_RECORDS + 1)
        .mapToObj(index -> """
            {
              "provider": "github",
              "host": "github.com",
              "namespace": "owner/repo",
              "record_type": "issue",
              "number": %d,
              "title": "Issue %03d",
              "status": "current"
            }
            """.formatted(index, index))
        .collect(Collectors.joining(",\n"));
    Path importFile = writeImportFile("""
        {
          "format": "agent-project-memory.git_hosting_export.v1",
          "records": [
        %s
          ]
        }
        """.formatted(records));

    AdapterIngestionResult result = adapter.read(
        tempDir,
        AdapterLocalImport.gitHostingImport(tempDir.relativize(importFile).toString()));

    assertAll(
        () -> assertEquals(GitHostingImportAdapter.MAX_RECORDS, result.sourceDocuments().size()),
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
            AdapterLocalImport.gitHostingImport(tempDir.relativize(importFile).toString())));

    assertAll(
        () -> assertEquals("Adapter import file format is unsupported.", exception.getMessage()),
        () -> assertFalse(exception.getMessage().contains("FAKE_RAW_IMPORT_SECRET")),
        () -> assertFalse(exception.getMessage().contains(importFile.toString())));
  }

  @Test
  void rejectsDirectImportPathEscapeEvenWhenConfigurationLoaderIsBypassed() throws Exception {
    Path outsideImport = tempDir.resolveSibling(tempDir.getFileName() + "-outside-import.json");
    Files.writeString(outsideImport, """
        {
          "format": "agent-project-memory.git_hosting_export.v1",
          "records": [
            {
              "provider": "github",
              "host": "github.com",
              "namespace": "owner/repo",
              "record_type": "issue",
              "number": 999,
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
            AdapterLocalImport.gitHostingImport("../" + outsideImport.getFileName())));

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
          "format": "agent-project-memory.git_hosting_export.v1",
          "records": [
            {
              "provider": "github",
              "host": "github.com",
              "namespace": "owner/repo",
              "record_type": "issue",
              "number": 999,
              "title": "Hardlinked import",
              "body": "FAKE_HARDLINKED_IMPORT_BODY_SECRET",
              "status": "current"
            }
          ]
        }
        """);
    Path importFile = tempDir.resolve("exports/git-hosting.json");
    Files.createDirectories(importFile.getParent());
    createHardLink(importFile, outsideImport);

    IOException exception = assertThrows(
        IOException.class,
        () -> adapter.read(tempDir, AdapterLocalImport.gitHostingImport("exports/git-hosting.json")));

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
    Path importFile = exports.resolve("git-hosting.json");
    Files.writeString(importFile, content);
    return importFile;
  }

  private void createHardLink(Path link, Path existing) throws Exception {
    try {
      Files.createLink(link, existing);
    } catch (UnsupportedOperationException | IOException | SecurityException exception) {
      assumeTrue(false, "hard links are unavailable: " + exception.getMessage());
    }
  }
}
