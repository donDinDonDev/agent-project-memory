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

final class MavenMetadataAnalyzerTest {
  @TempDir
  private Path tempDir;

  private final MavenMetadataAnalyzer analyzer = new MavenMetadataAnalyzer();

  @Test
  void directMetadataAndParentCoordinatesAreExtractedWithEvidence() throws Exception {
    Path repositoryRoot = repository("direct-metadata");
    writePom(repositoryRoot.resolve("pom.xml"), """
        <project xmlns="http://maven.apache.org/POM/4.0.0">
          <modelVersion>4.0.0</modelVersion>
          <parent>
            <groupId>com.example.parent</groupId>
            <artifactId>example-parent</artifactId>
            <version>1.0.0</version>
            <relativePath>../pom.xml</relativePath>
          </parent>
          <groupId>com.example</groupId>
          <artifactId>orders-service</artifactId>
          <version>${revision}</version>
          <packaging>war</packaging>
        </project>
        """);

    MavenMetadataAnalysis analysis = analyzer.analyze(
        repositoryRoot,
        List.of(rootModule()));
    MavenModuleMetadata metadata = metadata(analysis, "module:.");

    assertAll(
        () -> assertEquals("analyzed", metadata.analysisStatus()),
        () -> assertValue(metadata.groupId(), "com.example", "literal"),
        () -> assertValue(metadata.artifactId(), "orders-service", "literal"),
        () -> assertValue(metadata.version(), "${revision}", "property_reference"),
        () -> assertValue(metadata.packaging(), "war", "literal"),
        () -> assertEquals("analyzed", metadata.parent().analysisStatus()),
        () -> assertValue(metadata.parent().groupId(), "com.example.parent", "literal"),
        () -> assertValue(metadata.parent().artifactId(), "example-parent", "literal"),
        () -> assertValue(metadata.parent().version(), "1.0.0", "literal"),
        () -> assertValue(metadata.parent().relativePath(), "../pom.xml", "literal"),
        () -> assertTrue(analysis.evidence().stream()
            .anyMatch(evidence -> evidence.symbolName().equals("maven:project:artifactId")
                && evidence.sourcePath().equals("pom.xml"))),
        () -> assertEvidenceIdsResolve(analysis));
  }

  @Test
  void missingPartialAndUnsupportedMetadataIsExplicit() throws Exception {
    Path repositoryRoot = repository("partial-metadata");
    writePom(repositoryRoot.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
          <parent>
            <groupId>com.example.parent</groupId>
          </parent>
          <groupId>   </groupId>
          <artifactId>partial-service</artifactId>
          <version>1.0-${revision}</version>
          <packaging><type>jar</type></packaging>
        </project>
        """);

    MavenMetadataAnalysis analysis = analyzer.analyze(
        repositoryRoot,
        List.of(rootModule()));
    MavenModuleMetadata metadata = metadata(analysis, "module:.");

    assertAll(
        () -> assertValue(metadata.groupId(), null, "unsupported"),
        () -> assertValue(metadata.artifactId(), "partial-service", "literal"),
        () -> assertValue(metadata.version(), "1.0-${revision}", "expression"),
        () -> assertValue(metadata.packaging(), null, "unsupported"),
        () -> assertEquals("analyzed", metadata.parent().analysisStatus()),
        () -> assertValue(metadata.parent().groupId(), "com.example.parent", "literal"),
        () -> assertValue(metadata.parent().artifactId(), null, "not_declared"),
        () -> assertValue(metadata.parent().version(), null, "not_declared"),
        () -> assertValue(metadata.parent().relativePath(), null, "not_declared"),
        () -> assertFalse(metadata.groupId().evidenceIds().isEmpty()),
        () -> assertFalse(metadata.packaging().evidenceIds().isEmpty()),
        () -> assertEvidenceIdsResolve(analysis));
  }

  @Test
  void parentOnlyCoordinatesDoNotFillModuleCoordinates() throws Exception {
    Path repositoryRoot = repository("parent-only-metadata");
    writePom(repositoryRoot.resolve("pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
          <parent>
            <groupId>com.example.parent</groupId>
            <artifactId>example-parent</artifactId>
            <version>2.0.0</version>
          </parent>
          <artifactId>child-module</artifactId>
        </project>
        """);

    MavenMetadataAnalysis analysis = analyzer.analyze(
        repositoryRoot,
        List.of(rootModule()));
    MavenModuleMetadata metadata = metadata(analysis, "module:.");

    assertAll(
        () -> assertValue(metadata.groupId(), null, "not_declared"),
        () -> assertValue(metadata.artifactId(), "child-module", "literal"),
        () -> assertValue(metadata.version(), null, "not_declared"),
        () -> assertValue(metadata.packaging(), null, "not_declared"),
        () -> assertValue(metadata.parent().groupId(), "com.example.parent", "literal"),
        () -> assertValue(metadata.parent().artifactId(), "example-parent", "literal"),
        () -> assertValue(metadata.parent().version(), "2.0.0", "literal"),
        () -> assertEvidenceIdsResolve(analysis));
  }

  @Test
  void aggregatorModulePomPackagingIsSourceVisibleMetadata() throws Exception {
    Path repositoryRoot = repository("aggregator-module");
    writePom(repositoryRoot.resolve("platform/pom.xml"), """
        <project>
          <modelVersion>4.0.0</modelVersion>
          <groupId>com.example</groupId>
          <artifactId>platform</artifactId>
          <version>1.0.0</version>
          <packaging>pom</packaging>
          <modules>
            <module>child</module>
          </modules>
        </project>
        """);

    MavenMetadataAnalysis analysis = analyzer.analyze(
        repositoryRoot,
        List.of(new MavenModuleItem(
            "module:platform",
            "platform",
            "platform/pom.xml",
            List.of(),
            List.of(),
            "unsupported",
            "root_modules_entry",
            "platform",
            List.of(),
            List.of("ev:platform/pom.xml:1-1:build_file:pom.xml"))));
    MavenModuleMetadata metadata = metadata(analysis, "module:platform");

    assertAll(
        () -> assertEquals("module:platform", metadata.moduleId()),
        () -> assertEquals("analyzed", metadata.analysisStatus()),
        () -> assertValue(metadata.packaging(), "pom", "literal"),
        () -> assertEvidenceIdsResolve(analysis));
  }

  @Test
  void evidenceRecordsAreSortedDeterministicallyByPath() throws Exception {
    Path repositoryRoot = repository("evidence-order");
    writePom(repositoryRoot.resolve("b/pom.xml"), """
        <project>
          <artifactId>b</artifactId>
        </project>
        """);
    writePom(repositoryRoot.resolve("a/pom.xml"), """
        <project>
          <artifactId>a</artifactId>
        </project>
        """);

    MavenMetadataAnalysis analysis = analyzer.analyze(
        repositoryRoot,
        List.of(
            childModule("module:b", "b", "b/pom.xml"),
            childModule("module:a", "a", "a/pom.xml")));

    List<String> evidencePaths = analysis.evidence().stream()
        .map(MavenMetadataEvidence::sourcePath)
        .distinct()
        .toList();

    assertEquals(List.of("a/pom.xml", "b/pom.xml"), evidencePaths);
  }

  @Test
  void xmlDoctypeIsRejectedBeforeEntityExpansion() throws Exception {
    Path repositoryRoot = repository("doctype-rejected");
    writePom(repositoryRoot.resolve("pom.xml"), """
        <!DOCTYPE project [
          <!ENTITY xxe SYSTEM "file:///etc/passwd">
        ]>
        <project>
          <groupId>&xxe;</groupId>
        </project>
        """);

    IOException exception = assertThrows(
        IOException.class,
        () -> analyzer.analyze(repositoryRoot, List.of(rootModule())));

    assertAll(
        () -> assertTrue(exception.getMessage().contains("Could not parse Maven metadata in pom.xml")),
        () -> assertTrue(exception.getMessage().contains("malformed XML")));
  }

  @Test
  void oversizedPomIsSkippedWithDiagnostic() throws Exception {
    Path repositoryRoot = repository("oversized-metadata-pom");
    writeOversizedPom(repositoryRoot.resolve("pom.xml"));

    MavenMetadataAnalysis analysis = analyzer.analyze(
        repositoryRoot,
        List.of(rootModule()));
    MavenModuleMetadata metadata = metadata(analysis, "module:.");

    assertAll(
        () -> assertEquals("not_detected", metadata.analysisStatus()),
        () -> assertEquals("not_detected", metadata.parent().analysisStatus()),
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

  private MavenModuleItem childModule(String moduleId, String modulePath, String pomPath) {
    return new MavenModuleItem(
        moduleId,
        modulePath,
        pomPath,
        List.of(),
        List.of(),
        "unsupported",
        "root_modules_entry",
        modulePath,
        List.of(),
        List.of("ev:" + pomPath + ":1-1:build_file:pom.xml"));
  }

  private MavenModuleMetadata metadata(MavenMetadataAnalysis analysis, String moduleId) {
    return analysis.modules().stream()
        .filter(metadata -> metadata.moduleId().equals(moduleId))
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

  private void assertEvidenceIdsResolve(MavenMetadataAnalysis analysis) {
    Set<String> evidenceIds = analysis.evidence().stream()
        .map(MavenMetadataEvidence::id)
        .collect(Collectors.toSet());
    List<String> referencedEvidenceIds = new ArrayList<>();
    for (MavenModuleMetadata metadata : analysis.modules()) {
      referencedEvidenceIds.addAll(metadata.groupId().evidenceIds());
      referencedEvidenceIds.addAll(metadata.artifactId().evidenceIds());
      referencedEvidenceIds.addAll(metadata.version().evidenceIds());
      referencedEvidenceIds.addAll(metadata.packaging().evidenceIds());
      referencedEvidenceIds.addAll(metadata.parent().groupId().evidenceIds());
      referencedEvidenceIds.addAll(metadata.parent().artifactId().evidenceIds());
      referencedEvidenceIds.addAll(metadata.parent().version().evidenceIds());
      referencedEvidenceIds.addAll(metadata.parent().relativePath().evidenceIds());
    }

    assertTrue(evidenceIds.containsAll(referencedEvidenceIds));
    assertTrue(analysis.evidence().stream()
        .allMatch(evidence -> !Path.of(evidence.sourcePath()).isAbsolute()
            && !evidence.sourcePath().startsWith("./")));
  }
}
