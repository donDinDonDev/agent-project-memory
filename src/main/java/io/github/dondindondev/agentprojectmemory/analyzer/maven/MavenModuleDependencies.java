package io.github.dondindondev.agentprojectmemory.analyzer.maven;

import java.util.List;

public record MavenModuleDependencies(
    String moduleId,
    String analysisStatus,
    List<MavenDependencyDeclaration> dependencies,
    List<MavenDependencyDeclaration> dependencyManagement) {
  public MavenModuleDependencies {
    dependencies = List.copyOf(dependencies);
    dependencyManagement = List.copyOf(dependencyManagement);
  }
}
