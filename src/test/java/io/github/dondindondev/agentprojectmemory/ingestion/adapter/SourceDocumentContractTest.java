package io.github.dondindondev.agentprojectmemory.ingestion.adapter;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

final class SourceDocumentContractTest {
  private static final AdapterIdentity ADAPTER =
      new AdapterIdentity("local-structured-import", "2.0.0-test");

  @Test
  void sourceDocumentIdIsStableAcrossContentChanges() {
    SourceProvenance firstProvenance = provenance("content-hash-a");
    SourceProvenance secondProvenance = provenance("content-hash-b");

    SourceDocument first = document(firstProvenance);
    SourceDocument second = document(secondProvenance);

    assertAll(
        () -> assertEquals(first.id(), second.id()),
        () -> assertNotEquals(first.contentHash(), second.contentHash()),
        () -> assertNotEquals(first.provenanceId(), second.provenanceId()),
        () -> assertEquals("not_serialized", first.contentStatus().contractValue()));
  }

  @Test
  void sourceDocumentIdKeepsSourceTypeAndIdentityBoundaries() {
    String separator = "\u001f";
    SourceDocument first = SourceDocument.accepted(
        ADAPTER,
        AdapterImportMode.LOCAL_EXPORT,
        "local" + separator + "export",
        "issues/PM-123",
        "PM-123",
        "content-hash-a",
        "source-provenance:sha256:test");
    SourceDocument second = SourceDocument.accepted(
        ADAPTER,
        AdapterImportMode.LOCAL_EXPORT,
        "local",
        "export" + separator + "issues/PM-123",
        "PM-123",
        "content-hash-a",
        "source-provenance:sha256:test");

    assertNotEquals(first.id(), second.id());
  }

  @Test
  void sourceDocumentAndProvenanceIdsKeepAdapterNameAndVersionBoundaries() {
    AdapterIdentity firstAdapter = new AdapterIdentity("local@structured", "import");
    AdapterIdentity secondAdapter = new AdapterIdentity("local", "structured@import");

    SourceDocument firstDocument = SourceDocument.accepted(
        firstAdapter,
        AdapterImportMode.LOCAL_EXPORT,
        "local_export",
        "issues/PM-123",
        "PM-123",
        "content-hash-a",
        "source-provenance:sha256:test");
    SourceDocument secondDocument = SourceDocument.accepted(
        secondAdapter,
        AdapterImportMode.LOCAL_EXPORT,
        "local_export",
        "issues/PM-123",
        "PM-123",
        "content-hash-a",
        "source-provenance:sha256:test");
    SourceProvenance firstProvenance = SourceProvenance.accepted(
        firstAdapter,
        AdapterImportMode.LOCAL_EXPORT,
        "local_export",
        "issues/PM-123",
        "content-hash-a",
        "repository_relative_file",
        "not_applicable",
        List.of("local_export"));
    SourceProvenance secondProvenance = SourceProvenance.accepted(
        secondAdapter,
        AdapterImportMode.LOCAL_EXPORT,
        "local_export",
        "issues/PM-123",
        "content-hash-a",
        "repository_relative_file",
        "not_applicable",
        List.of("local_export"));

    assertAll(
        () -> assertNotEquals(firstDocument.id(), secondDocument.id()),
        () -> assertNotEquals(firstProvenance.id(), secondProvenance.id()));
  }

  @Test
  void acceptedSourceDocumentRequiresProvenanceId() {
    IllegalArgumentException thrown = assertThrows(
        IllegalArgumentException.class,
        () -> SourceDocument.accepted(
            ADAPTER,
            AdapterImportMode.LOCAL_EXPORT,
            "local_export",
            "issues/PM-123",
            "PM-123",
            "content-hash-a",
            " "));

    assertEquals("provenance id is required", thrown.getMessage());
  }

  @Test
  void acceptedSourceDocumentRequiresStableSourceIdentity() {
    IllegalArgumentException thrown = assertThrows(
        IllegalArgumentException.class,
        () -> SourceDocument.accepted(
            ADAPTER,
            AdapterImportMode.LOCAL_EXPORT,
            "local_export",
            "",
            "Untitled",
            "content-hash-a",
            "source-provenance:sha256:test"));

    assertEquals("source identity is required", thrown.getMessage());
  }

  @Test
  void sourceProvenanceRequiresRequiredBoundaryMetadata() {
    assertAll(
        () -> assertThrows(
            IllegalArgumentException.class,
            () -> SourceProvenance.accepted(
                ADAPTER,
                AdapterImportMode.LOCAL_EXPORT,
                "local_export",
                "issues/PM-123",
                " ",
                "repository_relative_file",
                "not_applicable",
                List.of("local_export"))),
        () -> assertThrows(
            IllegalArgumentException.class,
            () -> SourceProvenance.accepted(
                ADAPTER,
                AdapterImportMode.LOCAL_EXPORT,
                "local_export",
                "issues/PM-123",
                "content-hash-a",
                "repository_relative_file",
                "not_applicable",
                List.of())));
  }

  @Test
  void sourceProvenanceRejectsAmbiguousBoundaryMetadata() {
    assertAll(
        () -> assertThrows(
            IllegalArgumentException.class,
            () -> SourceProvenance.accepted(
                ADAPTER,
                AdapterImportMode.LOCAL_EXPORT,
                "local_export",
                "issues/PM-123",
                "content-hash-a",
                "remote_or_local",
                "not_applicable",
                List.of("local_export"))),
        () -> assertThrows(
            IllegalArgumentException.class,
            () -> SourceProvenance.accepted(
                ADAPTER,
                AdapterImportMode.LOCAL_EXPORT,
                "local_export",
                "issues/PM-123",
                "content-hash-a",
                "repository_relative_file",
                "maybe",
                List.of("local_export"))));
  }

  private SourceDocument document(SourceProvenance provenance) {
    return SourceDocument.accepted(
        ADAPTER,
        AdapterImportMode.LOCAL_EXPORT,
        provenance.sourceType(),
        provenance.sourceIdentity(),
        "PM-123",
        provenance.contentHash(),
        provenance.id());
  }

  private SourceProvenance provenance(String contentHash) {
    return SourceProvenance.accepted(
        ADAPTER,
        AdapterImportMode.LOCAL_EXPORT,
        "local_export",
        "issues/PM-123",
        contentHash,
        "repository_relative_file",
        "not_applicable",
        List.of("local_export", "adapter_provenance"));
  }
}
