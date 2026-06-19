package io.github.dondindondev.agentprojectmemory.workspace;

import java.util.List;

public record WorkspaceConfiguration(
    String configPath,
    List<WorkspaceMember> members) {
  public WorkspaceConfiguration {
    members = List.copyOf(members);
  }
}
