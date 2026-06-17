package io.github.dondindondev.agentprojectmemory.ingestion.adapter;

import java.util.List;

public record AdapterConfiguration(
    boolean enabled,
    boolean networkEnabled,
    List<AdapterLocalImport> localImports) {
  public AdapterConfiguration {
    if (networkEnabled) {
      throw new IllegalArgumentException("network access is disabled for adapter configuration");
    }
    localImports = List.copyOf(localImports);
    if (!enabled && !localImports.isEmpty()) {
      throw new IllegalArgumentException("adapter local imports require enabled adapters");
    }
    if (enabled && localImports.isEmpty()) {
      throw new IllegalArgumentException("enabled adapter configuration requires a local import");
    }
  }

  public static AdapterConfiguration disabled() {
    return new AdapterConfiguration(false, false, List.of());
  }

  public static AdapterConfiguration enabledLocalImport(AdapterLocalImport localImport) {
    return new AdapterConfiguration(true, false, List.of(localImport));
  }
}
