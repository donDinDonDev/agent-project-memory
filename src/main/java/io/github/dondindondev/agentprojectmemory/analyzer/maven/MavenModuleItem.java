package io.github.dondindondev.agentprojectmemory.analyzer.maven;

import io.github.dondindondev.agentprojectmemory.analyzer.build.BuildModule;
import io.github.dondindondev.agentprojectmemory.analyzer.gradle.GradleBuildFileItem;
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
    List<String> pomEvidenceIds,
    List<String> buildSystems,
    String gradleProjectPath,
    List<GradleBuildFileItem> gradleBuildFiles) implements BuildModule {
  public MavenModuleItem(
      String moduleId,
      String modulePath,
      String pomPath,
      List<String> sourceRoots,
      List<String> testRoots,
      String supportStatus,
      String declarationKind,
      String declaredPath,
      List<String> declarationEvidenceIds,
      List<String> pomEvidenceIds) {
    this(
        moduleId,
        modulePath,
        pomPath,
        sourceRoots,
        testRoots,
        supportStatus,
        declarationKind,
        declaredPath,
        declarationEvidenceIds,
        pomEvidenceIds,
        List.of(),
        null,
        List.of());
  }

  public MavenModuleItem {
    sourceRoots = List.copyOf(sourceRoots);
    testRoots = List.copyOf(testRoots);
    declarationEvidenceIds = List.copyOf(declarationEvidenceIds);
    pomEvidenceIds = List.copyOf(pomEvidenceIds);
    buildSystems = List.copyOf(buildSystems);
    gradleBuildFiles = List.copyOf(gradleBuildFiles);
  }
}
