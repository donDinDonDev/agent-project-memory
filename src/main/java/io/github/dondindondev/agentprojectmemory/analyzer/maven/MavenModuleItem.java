package io.github.dondindondev.agentprojectmemory.analyzer.maven;

import io.github.dondindondev.agentprojectmemory.analyzer.build.BuildModule;
import java.util.List;

public record MavenModuleItem(
    String moduleId,
    String modulePath,
    String pomPath,
    List<String> sourceRoots,
    List<String> testRoots,
    String supportStatus,
    String declarationKind,
    String declaredPath,
    List<String> declarationEvidenceIds,
    List<String> pomEvidenceIds) implements BuildModule {
  public MavenModuleItem {
    sourceRoots = List.copyOf(sourceRoots);
    testRoots = List.copyOf(testRoots);
    declarationEvidenceIds = List.copyOf(declarationEvidenceIds);
    pomEvidenceIds = List.copyOf(pomEvidenceIds);
  }
}
