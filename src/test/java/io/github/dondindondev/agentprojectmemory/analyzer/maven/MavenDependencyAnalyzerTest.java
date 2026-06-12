package io.github.dondindondev.agentprojectmemory.analyzer.maven;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.dondindondev.agentprojectmemory.analyzer.ScanDiagnostic;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class MavenDependencyAnalyzerTest {
  @TempDir
  private Path tempDir;

  private final MavenDependencyAnalyzer analyzer = new MavenDependencyAnalyzer();

  @Test
  void directDependenciesAreExtractedWithSourceVisibleValuesAndEvidence() throws Exception {
    Path repositoryRoot = repository("direct-dependencies");
    writePom(repositoryRoot.resolve("pom.xml"), """
        <project>
          <dependencies>
            <dependency>
              <groupId>org.springframework.boot</groupId>
              <artifactId>spring-boot-starter-web</artifactId>
              <version>${spring.boot.version}</version>
              <scope>compile</scope>
              <optional>true</optional>
              <type>jar</type>
              <classifier>tests</classifier>
            </dependency>
          </dependencies>
        </project>
        """);

    MavenDependencyAnalysis analysis = analyzer.analyze(repositoryRoot, List.of(rootModule()));
    MavenModuleDependencies moduleDependencies = moduleDependencies(analysis, "module:.");
    MavenDependencyDeclaration dependency = moduleDependencies.dependencies().get(0);

    assertAll(
        () -> assertEquals("analyzed", moduleDependencies.analysisStatus()),
        () -> assertEquals(1, moduleDependencies.dependencies().size()),
        () -> assertEquals(0, moduleDependencies.dependencyManagement().size()),
        () -> assertEquals("direct_dependency", dependency.declarationKind()),
        () -> assertEquals(1, dependency.declarationOrdinal()),
        () -> assertValue(dependency.groupId(), "org.springframework.boot", "literal"),
        () -> assertValue(dependency.artifactId(), "spring-boot-starter-web", "literal"),
        () -> assertValue(dependency.version(), "${spring.boot.version}", "property_reference"),
        () -> assertValue(dependency.scope(), "compile", "literal"),
        () -> assertValue(dependency.optional(), "true", "literal"),
        () -> assertValue(dependency.type(), "jar", "literal"),
        () -> assertValue(dependency.classifier(), "tests", "literal"),
        () -> assertTrue(dependency.id().startsWith(
            "maven_dependency:module:.:direct:org.springframework.boot:spring-boot-starter-web")),
        () -> assertTrue(analysis.evidence().stream()
            .anyMatch(evidence -> evidence.symbolName().equals("maven:dependency:000001:groupId")
                && evidence.sourcePath().equals("pom.xml"))),
        () -> assertEvidenceIdsResolve(analysis));
  }

  @Test
  void missingAndUnsupportedDependencyFieldsAreExplicit() throws Exception {
    Path repositoryRoot = repository("missing-dependency-fields");
    writePom(repositoryRoot.resolve("pom.xml"), """
        <project>
          <dependencies>
            <dependency>
              <groupId>   </groupId>
              <artifactId>partial-dependency</artifactId>
              <version>1.0-${revision}</version>
              <scope><name>test</name></scope>
            </dependency>
          </dependencies>
        </project>
        """);

    MavenDependencyAnalysis analysis = analyzer.analyze(repositoryRoot, List.of(rootModule()));
    MavenDependencyDeclaration dependency = moduleDependencies(analysis, "module:.")
        .dependencies()
        .get(0);

    assertAll(
        () -> assertValue(dependency.groupId(), null, "unsupported"),
        () -> assertValue(dependency.artifactId(), "partial-dependency", "literal"),
        () -> assertValue(dependency.version(), "1.0-${revision}", "expression"),
        () -> assertValue(dependency.scope(), null, "unsupported"),
        () -> assertValue(dependency.optional(), null, "not_declared"),
        () -> assertValue(dependency.type(), null, "not_declared"),
        () -> assertValue(dependency.classifier(), null, "not_declared"),
        () -> assertFalse(dependency.groupId().evidenceIds().isEmpty()),
        () -> assertFalse(dependency.scope().evidenceIds().isEmpty()),
        () -> assertEvidenceIdsResolve(analysis));
  }

  @Test
  void dependencyManagementDeclarationsStaySeparateFromDirectDependencies() throws Exception {
    Path repositoryRoot = repository("dependency-management-boundary");
    writePom(repositoryRoot.resolve("pom.xml"), """
        <project>
          <dependencyManagement>
            <dependencies>
              <dependency>
                <groupId>com.example</groupId>
                <artifactId>managed-library</artifactId>
                <version>${managed.version}</version>
              </dependency>
            </dependencies>
          </dependencyManagement>
          <dependencies>
            <dependency>
              <groupId>com.example</groupId>
              <artifactId>active-library</artifactId>
            </dependency>
          </dependencies>
          <build>
            <plugins>
              <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-fake-plugin</artifactId>
                <dependencies>
                  <dependency>
                    <groupId>com.example</groupId>
                    <artifactId>plugin-helper</artifactId>
                  </dependency>
                </dependencies>
              </plugin>
            </plugins>
          </build>
        </project>
        """);

    MavenDependencyAnalysis analysis = analyzer.analyze(repositoryRoot, List.of(rootModule()));
    MavenModuleDependencies moduleDependencies = moduleDependencies(analysis, "module:.");
    MavenDependencyDeclaration directDependency = moduleDependencies.dependencies().get(0);
    MavenDependencyDeclaration managedDependency = moduleDependencies.dependencyManagement().get(0);

    assertAll(
        () -> assertEquals(1, moduleDependencies.dependencies().size()),
        () -> assertEquals(1, moduleDependencies.dependencyManagement().size()),
        () -> assertEquals("active-library", directDependency.artifactId().value()),
        () -> assertEquals("direct_dependency", directDependency.declarationKind()),
        () -> assertEquals("managed-library", managedDependency.artifactId().value()),
        () -> assertEquals("dependency_management", managedDependency.declarationKind()),
        () -> assertEquals("${managed.version}", managedDependency.version().value()),
        () -> assertTrue(analysis.evidence().stream()
            .noneMatch(evidence -> evidence.excerpt().contains("plugin-helper"))),
        () -> assertEvidenceIdsResolve(analysis));
  }

  @Test
  void dependenciesAreSortedDeterministicallyWithinEachSection() throws Exception {
    Path repositoryRoot = repository("dependency-sort");
    writePom(repositoryRoot.resolve("pom.xml"), """
        <project>
          <dependencies>
            <dependency>
              <groupId>com.zeta</groupId>
              <artifactId>b-library</artifactId>
              <scope>test</scope>
            </dependency>
            <dependency>
              <groupId>com.alpha</groupId>
              <artifactId>c-library</artifactId>
            </dependency>
            <dependency>
              <groupId>com.alpha</groupId>
              <artifactId>a-library</artifactId>
              <type>test-jar</type>
            </dependency>
          </dependencies>
        </project>
        """);

    MavenDependencyAnalysis analysis = analyzer.analyze(repositoryRoot, List.of(rootModule()));

    List<String> artifactIds = moduleDependencies(analysis, "module:.").dependencies().stream()
        .map(dependency -> dependency.artifactId().value())
        .toList();
    List<Integer> declarationOrdinals = moduleDependencies(analysis, "module:.").dependencies().stream()
        .map(MavenDependencyDeclaration::declarationOrdinal)
        .toList();

    assertAll(
        () -> assertEquals(List.of("a-library", "c-library", "b-library"), artifactIds),
        () -> assertEquals(List.of(3, 2, 1), declarationOrdinals),
        () -> assertEvidenceIdsResolve(analysis));
  }

  @Test
  void propertyPlaceholdersArePreservedWithoutResolution() throws Exception {
    Path repositoryRoot = repository("dependency-placeholders");
    writePom(repositoryRoot.resolve("pom.xml"), """
        <project>
          <dependencies>
            <dependency>
              <groupId>${dependency.group}</groupId>
              <artifactId>placeholder-library</artifactId>
              <version>${dependency.version}</version>
              <scope>${dependency.scope}</scope>
              <optional>${dependency.optional}</optional>
            </dependency>
          </dependencies>
        </project>
        """);

    MavenDependencyAnalysis analysis = analyzer.analyze(repositoryRoot, List.of(rootModule()));
    MavenDependencyDeclaration dependency = moduleDependencies(analysis, "module:.")
        .dependencies()
        .get(0);

    assertAll(
        () -> assertValue(dependency.groupId(), "${dependency.group}", "property_reference"),
        () -> assertValue(dependency.version(), "${dependency.version}", "property_reference"),
        () -> assertValue(dependency.scope(), "${dependency.scope}", "property_reference"),
        () -> assertValue(dependency.optional(), "${dependency.optional}", "property_reference"),
        () -> assertEvidenceIdsResolve(analysis));
  }

  @Test
  void missingPomProducesNotDetectedDependencySections() throws Exception {
    Path repositoryRoot = repository("missing-pom");

    MavenDependencyAnalysis analysis = analyzer.analyze(
        repositoryRoot,
        List.of(new MavenModuleItem(
            "module:missing",
            "missing",
            null,
            List.of(),
            List.of(),
            "missing_child_pom",
            "root_modules_entry",
            "missing",
            List.of(),
            List.of())));
    MavenModuleDependencies moduleDependencies = moduleDependencies(analysis, "module:missing");

    assertAll(
        () -> assertEquals("not_detected", moduleDependencies.analysisStatus()),
        () -> assertEquals(0, moduleDependencies.dependencies().size()),
        () -> assertEquals(0, moduleDependencies.dependencyManagement().size()),
        () -> assertEquals(0, analysis.evidence().size()));
  }

  @Test
  void xmlDoctypeIsRejectedBeforeEntityExpansion() throws Exception {
    Path repositoryRoot = repository("doctype-rejected");
    writePom(repositoryRoot.resolve("pom.xml"), """
        <!DOCTYPE project [
          <!ENTITY xxe SYSTEM "file:///etc/passwd">
        ]>
        <project>
          <dependencies>
            <dependency>
              <groupId>&xxe;</groupId>
              <artifactId>unsafe</artifactId>
            </dependency>
          </dependencies>
        </project>
        """);

    IOException exception = assertThrows(
        IOException.class,
        () -> analyzer.analyze(repositoryRoot, List.of(rootModule())));

    assertAll(
        () -> assertTrue(exception.getMessage().contains("Could not parse Maven dependencies in pom.xml")),
        () -> assertTrue(exception.getMessage().contains("malformed XML")));
  }

  @Test
  void oversizedPomIsSkippedWithDiagnostic() throws Exception {
    Path repositoryRoot = repository("oversized-dependency-pom");
    writeOversizedPom(repositoryRoot.resolve("pom.xml"));

    MavenDependencyAnalysis analysis = analyzer.analyze(repositoryRoot, List.of(rootModule()));
    MavenModuleDependencies dependencies = moduleDependencies(analysis, "module:.");

    assertAll(
        () -> assertEquals("not_detected", dependencies.analysisStatus()),
        () -> assertEquals(List.of(), dependencies.dependencies()),
        () -> assertEquals(List.of(), dependencies.dependencyManagement()),
        () -> assertEquals(List.of(), analysis.evidence()),
        () -> assertEquals(1, analysis.diagnostics().size()),
        () -> assertPomSizeDiagnostic(analysis.diagnostics().get(0), "pom.xml"));
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

  private void writeOversizedPom(Path pom) throws Exception {
    writePom(pom, "<project>\n<!-- " + "x".repeat(MavenPomInput.MAX_POM_BYTES) + " -->\n</project>\n");
  }

  private MavenModuleItem rootModule() {
    return new MavenModuleItem(
        "module:.",
        ".",
        "pom.xml",
        List.of(),
        List.of(),
        "unsupported",
        "scan_root",
        ".",
        List.of(),
        List.of("ev:pom.xml:1-1:build_file:pom.xml"));
  }

  private MavenModuleDependencies moduleDependencies(MavenDependencyAnalysis analysis, String moduleId) {
    return analysis.modules().stream()
        .filter(dependencies -> dependencies.moduleId().equals(moduleId))
        .findFirst()
        .orElseThrow();
  }

  private void assertValue(MavenMetadataValue value, String expectedValue, String expectedKind) {
    assertEquals(expectedValue, value.value());
    assertEquals(expectedKind, value.valueKind());
  }

  private void assertPomSizeDiagnostic(ScanDiagnostic diagnostic, String sourcePath) {
    assertAll(
        () -> assertEquals(
            MavenPomInput.DIAGNOSTIC_CODE_POM_BYTES_CAP_EXCEEDED,
            diagnostic.code()),
        () -> assertEquals("warning", diagnostic.severity()),
        () -> assertEquals("maven", diagnostic.category()),
        () -> assertEquals(sourcePath, diagnostic.path()),
        () -> assertEquals(MavenPomInput.MAX_POM_BYTES, diagnostic.count()));
  }

  private void assertEvidenceIdsResolve(MavenDependencyAnalysis analysis) {
    Set<String> evidenceIds = analysis.evidence().stream()
        .map(MavenDependencyEvidence::id)
        .collect(Collectors.toSet());
    List<String> referencedEvidenceIds = new ArrayList<>();
    for (MavenModuleDependencies dependencies : analysis.modules()) {
      dependencies.dependencies().forEach(dependency ->
          addReferencedEvidenceIds(referencedEvidenceIds, dependency));
      dependencies.dependencyManagement().forEach(dependency ->
          addReferencedEvidenceIds(referencedEvidenceIds, dependency));
    }

    assertTrue(evidenceIds.containsAll(referencedEvidenceIds));
    assertTrue(analysis.evidence().stream()
        .allMatch(evidence -> !Path.of(evidence.sourcePath()).isAbsolute()
            && !evidence.sourcePath().startsWith("./")));
  }

  private void addReferencedEvidenceIds(
      List<String> referencedEvidenceIds,
      MavenDependencyDeclaration dependency) {
    referencedEvidenceIds.addAll(dependency.groupId().evidenceIds());
    referencedEvidenceIds.addAll(dependency.artifactId().evidenceIds());
    referencedEvidenceIds.addAll(dependency.version().evidenceIds());
    referencedEvidenceIds.addAll(dependency.scope().evidenceIds());
    referencedEvidenceIds.addAll(dependency.optional().evidenceIds());
    referencedEvidenceIds.addAll(dependency.type().evidenceIds());
    referencedEvidenceIds.addAll(dependency.classifier().evidenceIds());
    referencedEvidenceIds.addAll(dependency.evidenceIds());
  }
}
