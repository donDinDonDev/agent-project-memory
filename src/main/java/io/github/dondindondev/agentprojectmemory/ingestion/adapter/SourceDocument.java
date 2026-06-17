package io.github.dondindondev.agentprojectmemory.ingestion.adapter;

import java.util.Objects;

public record SourceDocument(
    String id,
    String sourceType,
    String sourceIdentity,
    String title,
    String contentHash,
    SourceDocumentContentStatus contentStatus,
    String provenanceId) {
  public SourceDocument {
    id = StableAdapterIds.requiredText(id, "source document id");
    sourceType = StableAdapterIds.requiredText(sourceType, "source type");
    sourceIdentity = StableAdapterIds.requiredText(sourceIdentity, "source identity");
    title = StableAdapterIds.optionalDisplayText(title);
    contentHash = StableAdapterIds.requiredText(contentHash, "content hash");
    contentStatus = Objects.requireNonNull(contentStatus, "content status");
    provenanceId = StableAdapterIds.requiredText(provenanceId, "provenance id");
  }

  public static SourceDocument accepted(
      AdapterIdentity adapterIdentity,
      AdapterImportMode importMode,
      String sourceType,
      String sourceIdentity,
      String title,
      String contentHash,
      String provenanceId) {
    String id = StableAdapterIds.sourceDocumentId(
        adapterIdentity,
        importMode,
        sourceType,
        sourceIdentity);
    return new SourceDocument(
        id,
        sourceType,
        sourceIdentity,
        title,
        contentHash,
        SourceDocumentContentStatus.NOT_SERIALIZED,
        provenanceId);
  }
}
