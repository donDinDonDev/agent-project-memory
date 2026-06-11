package io.github.dondindondev.agentprojectmemory.analyzer.documents;

import io.github.dondindondev.agentprojectmemory.scanconfig.ScanConfigPathPattern;
import java.util.List;

public record DocumentDiscoveryOptions(
    boolean localMarkdownEnabled,
    List<ScanConfigPathPattern> includes,
    List<ScanConfigPathPattern> excludes) {
  public static DocumentDiscoveryOptions defaults() {
    return new DocumentDiscoveryOptions(true, List.of(), List.of());
  }

  public DocumentDiscoveryOptions {
    includes = List.copyOf(includes);
    excludes = List.copyOf(excludes);
  }
}
