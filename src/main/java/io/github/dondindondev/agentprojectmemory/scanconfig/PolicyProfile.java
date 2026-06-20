package io.github.dondindondev.agentprojectmemory.scanconfig;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum PolicyProfile {
  GUARDED_LOCAL("guarded-local"),
  DOCS_FOCUSED("docs-focused"),
  ADAPTER_LOCAL("adapter-local");

  private final String selector;

  PolicyProfile(String selector) {
    this.selector = selector;
  }

  public String selector() {
    return selector;
  }

  public static Optional<PolicyProfile> fromSelector(String selector) {
    if (selector == null) {
      return Optional.empty();
    }
    return Arrays.stream(values())
        .filter(profile -> profile.selector.equals(selector))
        .findFirst();
  }

  public static List<String> selectors() {
    return Arrays.stream(values())
        .map(PolicyProfile::selector)
        .toList();
  }
}
