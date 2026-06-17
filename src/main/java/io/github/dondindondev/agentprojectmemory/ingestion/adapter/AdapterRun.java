package io.github.dondindondev.agentprojectmemory.ingestion.adapter;

public record AdapterRun(
    String id,
    AdapterIdentity adapterIdentity,
    AdapterImportMode importMode,
    String sourceLocationKind,
    String networkAccess,
    String inputContentHash,
    String contentStatus,
    int acceptedCount,
    int rejectedCount,
    int diagnosticCount) {
  public AdapterRun {
    id = StableAdapterIds.requiredText(id, "adapter run id");
    adapterIdentity = java.util.Objects.requireNonNull(adapterIdentity, "adapter identity");
    importMode = java.util.Objects.requireNonNull(importMode, "import mode");
    sourceLocationKind = StableAdapterIds.requiredKnownText(
        sourceLocationKind,
        "source location kind",
        java.util.List.of("repository_relative_file"));
    networkAccess = StableAdapterIds.requiredKnownText(
        networkAccess,
        "network access",
        java.util.List.of("disabled"));
    inputContentHash = StableAdapterIds.requiredText(inputContentHash, "input content hash");
    contentStatus = StableAdapterIds.requiredKnownText(
        contentStatus,
        "content status",
        java.util.List.of(SourceDocumentContentStatus.NOT_SERIALIZED.contractValue()));
    if (acceptedCount < 0 || rejectedCount < 0 || diagnosticCount < 0) {
      throw new IllegalArgumentException("adapter run counts must be non-negative");
    }
  }

  public static AdapterRun completed(
      AdapterIdentity adapterIdentity,
      AdapterImportMode importMode,
      String inputContentHash,
      int acceptedCount,
      int rejectedCount,
      int diagnosticCount) {
    return new AdapterRun(
        StableAdapterIds.adapterRunId(adapterIdentity, importMode, inputContentHash),
        adapterIdentity,
        importMode,
        "repository_relative_file",
        "disabled",
        inputContentHash,
        SourceDocumentContentStatus.NOT_SERIALIZED.contractValue(),
        acceptedCount,
        rejectedCount,
        diagnosticCount);
  }
}
