package io.github.dondindondev.agentprojectmemory.ingestion.adapter;

import java.util.List;
import java.util.Objects;

public record SourceProvenance(
    String id,
    AdapterIdentity adapterIdentity,
    AdapterImportMode importMode,
    String sourceType,
    String sourceIdentity,
    String contentHash,
    String sourceLocationKind,
    String networkAccess,
    List<String> trustBoundaryLabels,
    GitHostingMetadata gitHosting,
    ConnectorMetadata connector) {
  private static final List<String> SOURCE_LOCATION_KINDS = List.of(
      "repository_relative_file",
      "local_export_bundle",
      "remote_api_response");
  private static final List<String> NETWORK_ACCESS_VALUES = List.of(
      "disabled",
      "explicitly_enabled",
      "not_applicable");

  public SourceProvenance {
    id = StableAdapterIds.requiredText(id, "provenance id");
    adapterIdentity = Objects.requireNonNull(adapterIdentity, "adapter identity");
    importMode = Objects.requireNonNull(importMode, "import mode");
    sourceType = StableAdapterIds.requiredText(sourceType, "source type");
    sourceIdentity = StableAdapterIds.requiredText(sourceIdentity, "source identity");
    contentHash = StableAdapterIds.requiredText(contentHash, "content hash");
    sourceLocationKind = StableAdapterIds.requiredKnownText(
        sourceLocationKind,
        "source location kind",
        SOURCE_LOCATION_KINDS);
    networkAccess = StableAdapterIds.requiredKnownText(
        networkAccess,
        "network access",
        NETWORK_ACCESS_VALUES);
    trustBoundaryLabels = StableAdapterIds.requiredTextList(
        trustBoundaryLabels,
        "trust boundary labels");
  }

  public static SourceProvenance accepted(
      AdapterIdentity adapterIdentity,
      AdapterImportMode importMode,
      String sourceType,
      String sourceIdentity,
      String contentHash,
      String sourceLocationKind,
      String networkAccess,
      List<String> trustBoundaryLabels) {
    return accepted(
        adapterIdentity,
        importMode,
        sourceType,
        sourceIdentity,
        contentHash,
        sourceLocationKind,
        networkAccess,
        trustBoundaryLabels,
        null,
        null);
  }

  public static SourceProvenance accepted(
      AdapterIdentity adapterIdentity,
      AdapterImportMode importMode,
      String sourceType,
      String sourceIdentity,
      String contentHash,
      String sourceLocationKind,
      String networkAccess,
      List<String> trustBoundaryLabels,
      GitHostingMetadata gitHosting) {
    return accepted(
        adapterIdentity,
        importMode,
        sourceType,
        sourceIdentity,
        contentHash,
        sourceLocationKind,
        networkAccess,
        trustBoundaryLabels,
        gitHosting,
        null);
  }

  public static SourceProvenance accepted(
      AdapterIdentity adapterIdentity,
      AdapterImportMode importMode,
      String sourceType,
      String sourceIdentity,
      String contentHash,
      String sourceLocationKind,
      String networkAccess,
      List<String> trustBoundaryLabels,
      ConnectorMetadata connector) {
    return accepted(
        adapterIdentity,
        importMode,
        sourceType,
        sourceIdentity,
        contentHash,
        sourceLocationKind,
        networkAccess,
        trustBoundaryLabels,
        null,
        connector);
  }

  public static SourceProvenance accepted(
      AdapterIdentity adapterIdentity,
      AdapterImportMode importMode,
      String sourceType,
      String sourceIdentity,
      String contentHash,
      String sourceLocationKind,
      String networkAccess,
      List<String> trustBoundaryLabels,
      GitHostingMetadata gitHosting,
      ConnectorMetadata connector) {
    String id = StableAdapterIds.provenanceId(
        adapterIdentity,
        importMode,
        sourceType,
        sourceIdentity,
        contentHash);
    return new SourceProvenance(
        id,
        adapterIdentity,
        importMode,
        sourceType,
        sourceIdentity,
        contentHash,
        sourceLocationKind,
        networkAccess,
        trustBoundaryLabels,
        gitHosting,
        connector);
  }
}
