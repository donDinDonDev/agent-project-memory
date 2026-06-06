package io.github.dondindondev.agentprojectmemory.analyzer.maven;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class MavenModuleDiscoveryAnalyzerTest {
  @TempDir
  private Path tempDir;

  private final MavenModuleDiscoveryAnalyzer analyzer = new MavenModuleDiscoveryAnalyzer();

  @Test
  void singleModuleRootPomDiscoversScanRootModule() throws Exception {
    Path repositoryRoot = repository("single-module");
    writePom(repositoryRoot.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);
    Files.createDirectories(repositoryRoot.resolve("src/main/java"));
    Files.createDirectories(repositoryRoot.resolve("src/test/java"));

    MavenModuleDiscoveryAnalysis analysis = analyzer.analyze(repositoryRoot);
    MavenModuleItem rootModule = analysis.items().get(0);

    assertAll(
        () -> assertEquals("analyzed", analysis.analysisStatus()),
        () -> assertEquals(1, analysis.items().size()),
        () -> assertEquals("module:.", rootModule.moduleId()),
        () -> assertEquals(".", rootModule.modulePath()),
        () -> assertEquals("pom.xml", rootModule.pomPath()),
        () -> assertEquals(List.of("src/main/java"), rootModule.sourceRoots()),
        () -> assertEquals(List.of("src/test/java"), rootModule.testRoots()),
        () -> assertEquals("supported", rootModule.supportStatus()),
        () -> assertEquals("scan_root", rootModule.declarationKind()),
        () -> assertEquals(".", rootModule.declaredPath()),
        () -> assertEquals(List.of(), rootModule.declarationEvidenceIds()),
        () -> assertEquals(List.of("ev:pom.xml:1-1:build_file:pom.xml"), rootModule.pomEvidenceIds()),
        () -> assertEquals(List.of(), analysis.warnings()),
        () -> assertEvidenceIdsResolve(analysis));
  }

  @Test
  void malformedRootPomWithModuleSignalFailsInsteadOfSilentSingleModuleDiscovery()
      throws Exception {
    Path repositoryRoot = repository("malformed-root-pom");
    writePom(repositoryRoot.resolve("pom.xml"), """
        <project>
          <modules>
            <module>services/hidden</module>
          </modules>
          <broken>
        </project>
        """);
    Files.createDirectories(repositoryRoot.resolve("src/main/java"));
    writePom(repositoryRoot.resolve("services/hidden/pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);
    Files.createDirectories(repositoryRoot.resolve("services/hidden/src/main/java"));

    IOException exception = assertThrows(
        IOException.class,
        () -> analyzer.analyze(repositoryRoot));

    assertAll(
        () -> assertTrue(exception.getMessage()
            .contains("Could not parse Maven module declarations in pom.xml")),
        () -> assertTrue(exception.getMessage().contains("malformed XML")),
        () -> assertTrue(exception.getMessage().contains("line")),
        () -> assertFalse(exception.getMessage().contains("services/hidden")));
  }

  @Test
  void validRootModulesDiscoverChildPomsAndRootsInModuleOrder() throws Exception {
    Path repositoryRoot = repository("valid-modules");
    writePom(repositoryRoot.resolve("pom.xml"), """
        <project>
          <modules>
            <module>services/zeta/</module>
            <module>services/orders</module>
          </modules>
        </project>
        """);
    Files.createDirectories(repositoryRoot.resolve("src/main/java"));
    writePom(repositoryRoot.resolve("services/zeta/pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);
    Files.createDirectories(repositoryRoot.resolve("services/zeta/src/test/java"));
    writePom(repositoryRoot.resolve("services/orders/pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);
    Files.createDirectories(repositoryRoot.resolve("services/orders/src/main/java"));

    MavenModuleDiscoveryAnalysis analysis = analyzer.analyze(repositoryRoot);
    List<String> moduleIds = analysis.items().stream()
        .map(MavenModuleItem::moduleId)
        .toList();
    MavenModuleItem orders = item(analysis, "module:services/orders");
    MavenModuleItem zeta = item(analysis, "module:services/zeta");

    assertAll(
        () -> assertEquals(List.of("module:.", "module:services/orders", "module:services/zeta"), moduleIds),
        () -> assertEquals("services/orders", orders.modulePath()),
        () -> assertEquals("services/orders/pom.xml", orders.pomPath()),
        () -> assertEquals(List.of("services/orders/src/main/java"), orders.sourceRoots()),
        () -> assertEquals(List.of(), orders.testRoots()),
        () -> assertEquals("supported", orders.supportStatus()),
        () -> assertEquals("root_modules_entry", orders.declarationKind()),
        () -> assertEquals("services/orders", orders.declaredPath()),
        () -> assertEquals("services/zeta", zeta.modulePath()),
        () -> assertEquals("services/zeta", zeta.declaredPath()),
        () -> assertEquals("services/zeta/pom.xml", zeta.pomPath()),
        () -> assertEquals(List.of(), zeta.sourceRoots()),
        () -> assertEquals(List.of("services/zeta/src/test/java"), zeta.testRoots()),
        () -> assertEquals(List.of(), analysis.warnings()));
  }

  @Test
  void resourceOnlyModuleIsSupportedForBuildConfigDiscovery() throws Exception {
    Path repositoryRoot = repository("resource-only-module");
    writePom(repositoryRoot.resolve("pom.xml"), """
        <project>
          <modules>
            <module>services/config</module>
          </modules>
        </project>
        """);
    writePom(repositoryRoot.resolve("services/config/pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);
    Files.createDirectories(repositoryRoot.resolve("services/config/src/main/resources"));

    MavenModuleDiscoveryAnalysis analysis = analyzer.analyze(repositoryRoot);
    MavenModuleItem config = item(analysis, "module:services/config");

    assertAll(
        () -> assertEquals("supported", config.supportStatus()),
        () -> assertEquals(List.of(), config.sourceRoots()),
        () -> assertEquals(List.of(), config.testRoots()),
        () -> assertEquals(List.of(), analysis.warnings()),
        () -> assertEvidenceIdsResolve(analysis));
  }

  @Test
  void missingChildPomCreatesModuleItemAndWarning() throws Exception {
    Path repositoryRoot = repository("missing-child-pom");
    writePom(repositoryRoot.resolve("pom.xml"), """
        <project>
          <modules>
            <module>services/missing</module>
          </modules>
        </project>
        """);

    MavenModuleDiscoveryAnalysis analysis = analyzer.analyze(repositoryRoot);
    MavenModuleItem missing = item(analysis, "module:services/missing");
    MavenModuleWarning warning = warning(analysis, "missing_child_pom");

    assertAll(
        () -> assertEquals("services/missing", missing.modulePath()),
        () -> assertNull(missing.pomPath()),
        () -> assertEquals(List.of(), missing.sourceRoots()),
        () -> assertEquals(List.of(), missing.testRoots()),
        () -> assertEquals("missing_child_pom", missing.supportStatus()),
        () -> assertEquals("root_modules_entry", missing.declarationKind()),
        () -> assertEquals("services/missing", missing.declaredPath()),
        () -> assertEquals(List.of(), missing.pomEvidenceIds()),
        () -> assertEquals("warning:maven_module:missing_child_pom:services/missing", warning.id()),
        () -> assertEquals("maven_module", warning.category()),
        () -> assertEquals("module:services/missing", warning.moduleId()),
        () -> assertEquals("pom.xml", warning.sourcePath()),
        () -> assertTrue(warning.message().contains("does not have a child pom.xml")),
        () -> assertEquals(missing.declarationEvidenceIds(), warning.evidenceIds()),
        () -> assertEvidenceIdsResolve(analysis));
  }

  @Test
  void childPomSymlinkEscapingScanRootIsRejectedWithoutOutsidePomEvidence() throws Exception {
    Path repositoryRoot = repository("child-pom-symlink-escape");
    Path outsidePom = tempDir.resolve("outside-pom.xml");
    Files.writeString(outsidePom, """
        <!-- OUTSIDE_POM_SECRET_LINE -->
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);
    writePom(repositoryRoot.resolve("pom.xml"), """
        <project>
          <modules>
            <module>services/orders</module>
          </modules>
        </project>
        """);
    Files.createDirectories(repositoryRoot.resolve("services/orders"));
    createSymbolicLink(repositoryRoot.resolve("services/orders/pom.xml"), outsidePom);

    MavenModuleDiscoveryAnalysis analysis = analyzer.analyze(repositoryRoot);

    assertAll(
        () -> assertEquals(List.of(), analysis.items()),
        () -> assertEquals(
            List.of("invalid_module_path"),
            analysis.warnings().stream().map(MavenModuleWarning::signal).toList()),
        () -> assertTrue(analysis.warnings().stream().allMatch(warning -> warning.moduleId() == null)),
        () -> assertFalse(analysis.evidence().stream()
            .anyMatch(evidence -> "services/orders/pom.xml".equals(evidence.sourcePath()))),
        () -> assertFalse(analysis.evidence().stream()
            .anyMatch(evidence -> evidence.excerpt().contains("OUTSIDE_POM_SECRET_LINE"))),
        () -> assertEvidenceIdsResolve(analysis));
  }

  @Test
  void sourceAndTestRootSymlinksEscapingScanRootAreIgnored() throws Exception {
    Path repositoryRoot = repository("source-test-root-symlink-escape");
    Path outsideMain = tempDir.resolve("outside-main");
    Path outsideTest = tempDir.resolve("outside-test");
    Files.createDirectories(outsideMain);
    Files.createDirectories(outsideTest);
    writePom(repositoryRoot.resolve("pom.xml"), """
        <project>
          <modules>
            <module>services/orders</module>
          </modules>
        </project>
        """);
    writePom(repositoryRoot.resolve("services/orders/pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);
    Files.createDirectories(repositoryRoot.resolve("services/orders/src/main"));
    Files.createDirectories(repositoryRoot.resolve("services/orders/src/test"));
    createSymbolicLink(repositoryRoot.resolve("services/orders/src/main/java"), outsideMain);
    createSymbolicLink(repositoryRoot.resolve("services/orders/src/test/java"), outsideTest);

    MavenModuleDiscoveryAnalysis analysis = analyzer.analyze(repositoryRoot);
    MavenModuleItem orders = item(analysis, "module:services/orders");
    MavenModuleWarning warning = warning(analysis, "unsupported_module");

    assertAll(
        () -> assertEquals("unsupported", orders.supportStatus()),
        () -> assertEquals(List.of(), orders.sourceRoots()),
        () -> assertEquals(List.of(), orders.testRoots()),
        () -> assertEquals(
            List.of("unsupported_module"),
            analysis.warnings().stream().map(MavenModuleWarning::signal).toList()),
        () -> assertEquals("module:services/orders", warning.moduleId()),
        () -> assertEvidenceIdsResolve(analysis));
  }

  @Test
  void invalidModuleDeclarationsCreateWarningsWithoutModuleItems() throws Exception {
    Path repositoryRoot = repository("invalid-modules");
    writePom(repositoryRoot.resolve("pom.xml"), """
        <project>
          <modules>
            <module>   </module>
            <module>/absolute</module>
            <module>../escape</module>
            <module>services/./orders</module>
            <module>services\\orders</module>
          </modules>
        </project>
        """);

    MavenModuleDiscoveryAnalysis analysis = analyzer.analyze(repositoryRoot);
    List<MavenModuleWarning> warnings = analysis.warnings();

    assertAll(
        () -> assertEquals(List.of(), analysis.items()),
        () -> assertEquals(5, warnings.size()),
        () -> assertTrue(warnings.stream().allMatch(warning -> warning.signal().equals("invalid_module_path"))),
        () -> assertTrue(warnings.stream().allMatch(warning -> warning.moduleId() == null)),
        () -> assertEquals(
            List.of(
                "warning:maven_module:invalid_module_path:decl:000001",
                "warning:maven_module:invalid_module_path:decl:000002",
                "warning:maven_module:invalid_module_path:decl:000003",
                "warning:maven_module:invalid_module_path:decl:000004",
                "warning:maven_module:invalid_module_path:decl:000005"),
            warnings.stream().map(MavenModuleWarning::id).toList()),
        () -> assertEvidenceIdsResolve(analysis));
  }

  @Test
  void duplicateModuleDeclarationsCreateOneItemAndDuplicateWarningPerLaterDeclaration()
      throws Exception {
    Path repositoryRoot = repository("duplicate-modules");
    writePom(repositoryRoot.resolve("pom.xml"), """
        <project>
          <modules><module>services/orders</module><module>services/orders</module></modules>
        </project>
        """);
    writePom(repositoryRoot.resolve("services/orders/pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);
    Files.createDirectories(repositoryRoot.resolve("services/orders/src/main/java"));

    MavenModuleDiscoveryAnalysis analysis = analyzer.analyze(repositoryRoot);
    MavenModuleItem orders = item(analysis, "module:services/orders");
    MavenModuleWarning warning = warning(analysis, "duplicate_module_path");

    assertAll(
        () -> assertEquals(1, analysis.items().size()),
        () -> assertEquals("supported", orders.supportStatus()),
        () -> assertEquals(
            List.of("ev:pom.xml:2-2:build_file:module:services/orders:decl:000001"),
            orders.declarationEvidenceIds()),
        () -> assertEquals(
            "warning:maven_module:duplicate_module_path:services/orders:decl:000002",
            warning.id()),
        () -> assertEquals("module:services/orders", warning.moduleId()),
        () -> assertEquals(
            List.of("ev:pom.xml:2-2:build_file:module:services/orders:decl:000002"),
            warning.evidenceIds()),
        () -> assertEvidenceIdsResolve(analysis));
  }

  @Test
  void nestedModuleDeclarationsCreateWarningsWithoutRecursiveDiscovery() throws Exception {
    Path repositoryRoot = repository("nested-modules");
    writePom(repositoryRoot.resolve("pom.xml"), """
        <project>
          <modules>
            <module>services/parent</module>
          </modules>
        </project>
        """);
    writePom(repositoryRoot.resolve("services/parent/pom.xml"), """
        <project>
          <modules>
            <module>nested-child</module>
          </modules>
        </project>
        """);
    Files.createDirectories(repositoryRoot.resolve("services/parent/src/main/java"));
    writePom(repositoryRoot.resolve("services/parent/nested-child/pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);
    Files.createDirectories(repositoryRoot.resolve("services/parent/nested-child/src/main/java"));

    MavenModuleDiscoveryAnalysis analysis = analyzer.analyze(repositoryRoot);
    MavenModuleWarning warning = warning(analysis, "nested_module_declaration");

    assertAll(
        () -> assertEquals(
            List.of("module:services/parent"),
            analysis.items().stream().map(MavenModuleItem::moduleId).toList()),
        () -> assertFalse(analysis.items().stream()
            .anyMatch(item -> item.moduleId().equals("module:services/parent/nested-child"))),
        () -> assertEquals(
            "warning:maven_module:nested_module_declaration:services/parent",
            warning.id()),
        () -> assertEquals("module:services/parent", warning.moduleId()),
        () -> assertEquals("services/parent/pom.xml", warning.sourcePath()),
        () -> assertEquals(2, warning.evidenceIds().size()),
        () -> assertTrue(warning.evidenceIds().stream()
            .anyMatch(id -> id.contains("services/parent/pom.xml:1-1:build_file:pom.xml"))),
        () -> assertTrue(warning.evidenceIds().stream()
            .anyMatch(id -> id.contains("build_file:module:services/parent/nested-child"))),
        () -> assertEvidenceIdsResolve(analysis));
  }

  @Test
  void unsupportedChildPomCreatesUnsupportedItemAndWarning() throws Exception {
    Path repositoryRoot = repository("unsupported-module");
    writePom(repositoryRoot.resolve("pom.xml"), """
        <project>
          <modules>
            <module>libraries/shared</module>
          </modules>
        </project>
        """);
    writePom(repositoryRoot.resolve("libraries/shared/pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);

    MavenModuleDiscoveryAnalysis analysis = analyzer.analyze(repositoryRoot);
    MavenModuleItem shared = item(analysis, "module:libraries/shared");
    MavenModuleWarning warning = warning(analysis, "unsupported_module");

    assertAll(
        () -> assertEquals("unsupported", shared.supportStatus()),
        () -> assertEquals(List.of(), shared.sourceRoots()),
        () -> assertEquals(List.of(), shared.testRoots()),
        () -> assertEquals("libraries/shared/pom.xml", warning.sourcePath()),
        () -> assertEquals("module:libraries/shared", warning.moduleId()),
        () -> assertTrue(warning.message().contains("no supported Java source, test, or resource roots")),
        () -> assertEquals(
            new ArrayList<>(List.of(shared.declarationEvidenceIds().get(0), shared.pomEvidenceIds().get(0))),
            warning.evidenceIds()),
        () -> assertEvidenceIdsResolve(analysis));
  }

  @Test
  void unsupportedChildPomWithNestedModulesCreatesOnlyUnsupportedWarning() throws Exception {
    Path repositoryRoot = repository("unsupported-nested-module");
    writePom(repositoryRoot.resolve("pom.xml"), """
        <project>
          <modules>
            <module>libraries/shared</module>
          </modules>
        </project>
        """);
    writePom(repositoryRoot.resolve("libraries/shared/pom.xml"), """
        <project>
          <modules>
            <module>nested-child</module>
          </modules>
        </project>
        """);
    writePom(repositoryRoot.resolve("libraries/shared/nested-child/pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
        </project>
        """);
    Files.createDirectories(repositoryRoot.resolve("libraries/shared/nested-child/src/main/java"));

    MavenModuleDiscoveryAnalysis analysis = analyzer.analyze(repositoryRoot);
    MavenModuleItem shared = item(analysis, "module:libraries/shared");
    MavenModuleWarning warning = warning(analysis, "unsupported_module");

    assertAll(
        () -> assertEquals("unsupported", shared.supportStatus()),
        () -> assertEquals(List.of(), shared.sourceRoots()),
        () -> assertEquals(List.of(), shared.testRoots()),
        () -> assertEquals(
            List.of("unsupported_module"),
            analysis.warnings().stream().map(MavenModuleWarning::signal).toList()),
        () -> assertFalse(analysis.warnings().stream()
            .anyMatch(moduleWarning -> moduleWarning.signal().equals("nested_module_declaration"))),
        () -> assertEquals(
            "warning:maven_module:unsupported_module:libraries/shared",
            warning.id()),
        () -> assertEvidenceIdsResolve(analysis));
  }

  private Path repository(String name) throws Exception {
    Path repositoryRoot = tempDir.resolve(name);
    Files.createDirectories(repositoryRoot);
    return repositoryRoot;
  }

  private void writePom(Path pom, String xml) throws Exception {
    Files.createDirectories(pom.getParent());
    Files.writeString(pom, xml);
  }

  private void createSymbolicLink(Path link, Path target) throws Exception {
    try {
      Files.createSymbolicLink(link, target);
    } catch (UnsupportedOperationException | IOException | SecurityException exception) {
      assumeTrue(false, "symbolic links are unavailable: " + exception.getMessage());
    }
  }

  private MavenModuleItem item(MavenModuleDiscoveryAnalysis analysis, String moduleId) {
    return analysis.items().stream()
        .filter(item -> item.moduleId().equals(moduleId))
        .findFirst()
        .orElseThrow();
  }

  private MavenModuleWarning warning(MavenModuleDiscoveryAnalysis analysis, String signal) {
    return analysis.warnings().stream()
        .filter(warning -> warning.signal().equals(signal))
        .findFirst()
        .orElseThrow();
  }

  private void assertEvidenceIdsResolve(MavenModuleDiscoveryAnalysis analysis) {
    Set<String> evidenceIds = analysis.evidence().stream()
        .map(MavenModuleDiscoveryEvidence::id)
        .collect(Collectors.toSet());
    List<String> referencedEvidenceIds = new ArrayList<>();
    for (MavenModuleItem item : analysis.items()) {
      referencedEvidenceIds.addAll(item.declarationEvidenceIds());
      referencedEvidenceIds.addAll(item.pomEvidenceIds());
    }
    for (MavenModuleWarning warning : analysis.warnings()) {
      referencedEvidenceIds.addAll(warning.evidenceIds());
    }

    assertTrue(evidenceIds.containsAll(referencedEvidenceIds));
    assertTrue(analysis.evidence().stream()
        .allMatch(evidence -> !Path.of(evidence.sourcePath()).isAbsolute()
            && !evidence.sourcePath().startsWith("./")));
  }
}
