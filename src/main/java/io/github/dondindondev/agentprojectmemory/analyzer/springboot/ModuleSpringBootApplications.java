package io.github.dondindondev.agentprojectmemory.analyzer.springboot;

import java.util.List;

public record ModuleSpringBootApplications(
    String moduleId,
    String analysisStatus,
    List<SpringBootApplicationFact> applications) {
  public ModuleSpringBootApplications {
    applications = List.copyOf(applications);
  }
}
