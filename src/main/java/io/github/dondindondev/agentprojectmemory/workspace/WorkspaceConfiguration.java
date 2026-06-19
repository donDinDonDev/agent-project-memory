package io.github.dondindondev.agentprojectmemory.workspace;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public record WorkspaceConfiguration(
    Path workspaceRoot,
    Path canonicalWorkspaceRoot,
    String configPath,
    List<WorkspaceMember> members) {
  public WorkspaceConfiguration {
    workspaceRoot = Objects.requireNonNull(workspaceRoot, "workspaceRoot");
    canonicalWorkspaceRoot = Objects.requireNonNull(canonicalWorkspaceRoot, "canonicalWorkspaceRoot");
    members = List.copyOf(members);
  }
}
