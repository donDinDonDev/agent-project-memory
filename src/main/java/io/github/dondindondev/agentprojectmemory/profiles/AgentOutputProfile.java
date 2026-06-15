package io.github.dondindondev.agentprojectmemory.profiles;

import java.util.List;
import java.util.Optional;

public enum AgentOutputProfile {
  CODEX("codex", "Codex", "agent-profiles/codex.md"),
  CLAUDE("claude", "Claude", "agent-profiles/claude.md"),
  CURSOR("cursor", "Cursor", "agent-profiles/cursor.md"),
  GENERIC("generic", "Generic", "agent-profiles/generic.md");

  private static final List<AgentOutputProfile> CANONICAL_ORDER = List.of(values());

  private final String selector;
  private final String displayName;
  private final String artifactPath;

  AgentOutputProfile(String selector, String displayName, String artifactPath) {
    this.selector = selector;
    this.displayName = displayName;
    this.artifactPath = artifactPath;
  }

  public String selector() {
    return selector;
  }

  public String displayName() {
    return displayName;
  }

  public String artifactPath() {
    return artifactPath;
  }

  public static List<AgentOutputProfile> canonicalOrder() {
    return CANONICAL_ORDER;
  }

  public static Optional<AgentOutputProfile> fromSelector(String selector) {
    return CANONICAL_ORDER.stream()
        .filter(profile -> profile.selector.equals(selector))
        .findFirst();
  }
}
