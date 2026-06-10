package io.github.dondindondev.agentprojectmemory.analyzer.documents;

import java.util.List;

public record DocumentDiscoveryPolicy(
    String scope,
    String pathPolicy,
    String symlinkPolicy,
    List<String> includedPatterns,
    List<String> excludedPatterns) {
  public DocumentDiscoveryPolicy {
    includedPatterns = List.copyOf(includedPatterns);
    excludedPatterns = List.copyOf(excludedPatterns);
  }
}
