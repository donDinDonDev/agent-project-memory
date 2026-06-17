package io.github.dondindondev.agentprojectmemory.ingestion.adapter;

import java.util.Objects;

public record AdapterLocalImport(
    String adapterName,
    AdapterImportMode importMode,
    String path) {
  public static final String LOCAL_STRUCTURED_IMPORT_ADAPTER = "local-structured-import";

  public AdapterLocalImport {
    adapterName = StableAdapterIds.requiredText(adapterName, "adapter name");
    importMode = Objects.requireNonNull(importMode, "import mode");
    path = StableAdapterIds.requiredText(path, "adapter import path");
  }

  public static AdapterLocalImport localStructuredImport(String path) {
    return new AdapterLocalImport(
        LOCAL_STRUCTURED_IMPORT_ADAPTER,
        AdapterImportMode.LOCAL_EXPORT,
        path);
  }
}
