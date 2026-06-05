package io.github.dondindondev.agentprojectmemory.analyzer.maven;

import java.util.List;

public record MavenModulePlugins(
    String moduleId,
    String analysisStatus,
    List<MavenPluginDeclaration> plugins,
    List<MavenPluginDeclaration> pluginManagement) {
  public MavenModulePlugins {
    plugins = List.copyOf(plugins);
    pluginManagement = List.copyOf(pluginManagement);
  }
}
