package io.github.dondindondev.agentprojectmemory.scanconfig;

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
    List<ScanConfigPathPattern> documentExcludes) {
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
        List.of());
  }

  public ScanConfiguration {
    documentIncludes = List.copyOf(documentIncludes);
    documentExcludes = List.copyOf(documentExcludes);
  }

  public boolean userIncludesApplied() {
    return !documentIncludes.isEmpty();
  }

  public boolean userExcludesApplied() {
    return !documentExcludes.isEmpty();
  }
}
