package io.github.dondindondev.agentprojectmemory.workspace;

import java.nio.file.Path;
import java.util.Objects;

public record WorkspaceMember(
    String repoId,
    String rootPath,
    Path canonicalRoot) {
  public WorkspaceMember {
    canonicalRoot = Objects.requireNonNull(canonicalRoot, "canonicalRoot");
  }
}
