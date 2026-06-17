package io.github.dondindondev.agentprojectmemory.scanconfig;

import io.github.dondindondev.agentprojectmemory.ingestion.adapter.AdapterConfiguration;
import java.util.List;

public record ScanConfiguration(
    String configSource,
    String configFilePath,
    String configFileStatus,
    boolean cliOverridesApplied,
    boolean rawValuesSerialized,
    boolean localMarkdownEnabled,
    String localMarkdownSource,
    List<ScanConfigPathPattern> documentIncludes,
    List<ScanConfigPathPattern> documentExcludes,
    AdapterConfiguration adapterConfiguration) {
  public static ScanConfiguration defaultsOnly() {
    return new ScanConfiguration(
        "defaults_only",
        null,
        "not_detected",
        false,
        false,
        true,
        "default",
        List.of(),
        List.of(),
        AdapterConfiguration.disabled());
  }

  public ScanConfiguration(
      String configSource,
      String configFilePath,
      String configFileStatus,
      boolean cliOverridesApplied,
      boolean rawValuesSerialized,
      boolean localMarkdownEnabled,
      String localMarkdownSource,
      List<ScanConfigPathPattern> documentIncludes,
      List<ScanConfigPathPattern> documentExcludes) {
    this(
        configSource,
        configFilePath,
        configFileStatus,
        cliOverridesApplied,
        rawValuesSerialized,
        localMarkdownEnabled,
        localMarkdownSource,
        documentIncludes,
        documentExcludes,
        AdapterConfiguration.disabled());
  }

  public ScanConfiguration {
    documentIncludes = List.copyOf(documentIncludes);
    documentExcludes = List.copyOf(documentExcludes);
    adapterConfiguration = adapterConfiguration == null
        ? AdapterConfiguration.disabled()
        : adapterConfiguration;
  }

  public boolean userIncludesApplied() {
    return !documentIncludes.isEmpty();
  }

  public boolean userExcludesApplied() {
    return !documentExcludes.isEmpty();
  }
}
