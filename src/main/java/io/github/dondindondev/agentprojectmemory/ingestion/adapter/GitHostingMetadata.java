package io.github.dondindondev.agentprojectmemory.ingestion.adapter;

public record GitHostingMetadata(
    String provider,
    String host,
    String namespace,
    String recordType,
    String recordNumber,
    String recordState,
    String sourceUrl,
    String exportedAt,
    String recordUpdatedAt) {
  public GitHostingMetadata {
    provider = StableAdapterIds.requiredText(provider, "git hosting provider");
    host = StableAdapterIds.requiredText(host, "git hosting host");
    namespace = StableAdapterIds.requiredText(namespace, "git hosting namespace");
    recordType = StableAdapterIds.requiredText(recordType, "git hosting record type");
    recordNumber = StableAdapterIds.requiredText(recordNumber, "git hosting record number");
    recordState = StableAdapterIds.optionalDisplayText(recordState);
    sourceUrl = StableAdapterIds.optionalDisplayText(sourceUrl);
    exportedAt = StableAdapterIds.optionalDisplayText(exportedAt);
    recordUpdatedAt = StableAdapterIds.optionalDisplayText(recordUpdatedAt);
  }
}
