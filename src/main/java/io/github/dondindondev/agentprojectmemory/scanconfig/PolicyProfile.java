package io.github.dondindondev.agentprojectmemory.scanconfig;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum PolicyProfile {
  GUARDED_LOCAL(
      "guarded-local",
      List.of("built_in_local_markdown"),
      List.of(
          "adapters",
          "ai_presentation",
          "local_markdown_path_rules",
          "generated_source_content",
          "symlink_following")),
  DOCS_FOCUSED(
      "docs-focused",
      List.of("built_in_local_markdown", "local_markdown_path_rules"),
      List.of(
          "adapters",
          "ai_presentation",
          "generated_source_content",
          "symlink_following")),
  ADAPTER_LOCAL(
      "adapter-local",
      List.of(
          "built_in_local_markdown",
          "local_markdown_path_rules",
          "local_import_adapters"),
      List.of(
          "ai_presentation",
          "generated_source_content",
          "symlink_following"));

  private final String selector;
  private final List<String> allowedOptionalSurfaces;
  private final List<String> rejectedOptionalSurfaces;

  PolicyProfile(
      String selector,
      List<String> allowedOptionalSurfaces,
      List<String> rejectedOptionalSurfaces) {
    this.selector = selector;
    this.allowedOptionalSurfaces = List.copyOf(allowedOptionalSurfaces);
    this.rejectedOptionalSurfaces = List.copyOf(rejectedOptionalSurfaces);
  }

  public String selector() {
    return selector;
  }

  public List<String> allowedOptionalSurfaces() {
    return allowedOptionalSurfaces;
  }

  public List<String> rejectedOptionalSurfaces() {
    return rejectedOptionalSurfaces;
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
