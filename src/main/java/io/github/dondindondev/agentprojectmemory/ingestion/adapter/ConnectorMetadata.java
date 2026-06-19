package io.github.dondindondev.agentprojectmemory.ingestion.adapter;

public record ConnectorMetadata(
    String provider,
    String host,
    String sourceFamily,
    String containerType,
    String containerKey,
    String recordType,
    String recordKey,
    String recordId,
    String recordState,
    String sourceUrl,
    String exportedAt,
    String recordUpdatedAt) {
  public ConnectorMetadata {
    provider = StableAdapterIds.requiredText(provider, "connector provider");
    host = StableAdapterIds.requiredText(host, "connector host");
    sourceFamily = StableAdapterIds.requiredText(sourceFamily, "connector source family");
    containerType = StableAdapterIds.requiredText(containerType, "connector container type");
    containerKey = StableAdapterIds.requiredText(containerKey, "connector container key");
    recordType = StableAdapterIds.requiredText(recordType, "connector record type");
    recordKey = StableAdapterIds.requiredText(recordKey, "connector record key");
    recordId = StableAdapterIds.optionalDisplayText(recordId);
    recordState = StableAdapterIds.optionalDisplayText(recordState);
    sourceUrl = StableAdapterIds.optionalDisplayText(sourceUrl);
    exportedAt = StableAdapterIds.optionalDisplayText(exportedAt);
    recordUpdatedAt = StableAdapterIds.optionalDisplayText(recordUpdatedAt);
  }
}
