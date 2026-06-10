package io.github.dondindondev.agentprojectmemory.analyzer.documents;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenModuleItem;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class DocumentDiscoveryAnalyzerTest {
  @TempDir
  private Path tempDir;

  private final DocumentDiscoveryAnalyzer analyzer = new DocumentDiscoveryAnalyzer();

  @Test
  void discoversDefaultScopeMarkdownWithDeterministicInventoryOnly() throws Exception {
    Path repositoryRoot = repository("default-scope");
    writeFile(repositoryRoot.resolve("README.md"), "# Root\n");
    writeFile(repositoryRoot.resolve("services/orders/README.markdown"), "# Orders\n");
    writeFile(repositoryRoot.resolve("docs/guide.md"), "# Guide\n");
    writeFile(repositoryRoot.resolve("docs/nested/usage.md"), "# Usage\n");
    writeFile(repositoryRoot.resolve("adr/0001-records.md"), "# ADR\n");
    writeFile(repositoryRoot.resolve("adrs/0002-more.md"), "# ADR\n");

    DocumentDiscoveryAnalysis analysis = analyzer.analyze(
        repositoryRoot,
        List.of(
            supportedModule("module:services/orders", "services/orders", "services/orders/pom.xml"),
            supportedModule("module:.", ".", "pom.xml")));

    List<DocumentFileFact> documents = analysis.documents();

    assertAll(
        () -> assertEquals("analyzed", analysis.analysisStatus()),
        () -> assertEquals(
            List.of(
                "services/orders/README.markdown",
                "README.md",
                "adr/0001-records.md",
                "adrs/0002-more.md",
                "docs/guide.md",
                "docs/nested/usage.md"),
            documents.stream().map(DocumentFileFact::path).toList()),
        () -> assertEquals("module:services/orders", documents.get(0).moduleId()),
        () -> assertNull(documents.get(1).moduleId()),
        () -> assertEquals("module_readme", documents.get(0).discoverySource()),
        () -> assertEquals("root_readme", documents.get(1).discoverySource()),
        () -> assertEquals("adr_tree", documents.get(2).discoverySource()),
        () -> assertEquals("docs_tree", documents.get(4).discoverySource()),
        () -> assertEquals("README", documents.get(1).title()),
        () -> assertEquals("filename", documents.get(1).titleSource()),
        () -> assertEquals("local_markdown", documents.get(1).documentKind()),
        () -> assertEquals("markdown", documents.get(1).format()),
        () -> assertEquals(List.of(), documents.get(1).headings()),
        () -> assertEquals(List.of(), documents.get(1).chunks()),
        () -> assertEquals(List.of(), documents.get(1).evidenceIds()));
  }

  @Test
  void excludesHiddenPrivateGeneratedDependencyOutputAndMaintainerPathsByDefault()
      throws Exception {
    Path repositoryRoot = repository("excluded");
    writeFile(repositoryRoot.resolve("docs/public.md"), "# Public\n");
    writeFile(repositoryRoot.resolve(".project-memory/agent-guide.md"), "# Generated\n");
    writeFile(repositoryRoot.resolve(".hidden/README.md"), "# Hidden\n");
    writeFile(repositoryRoot.resolve("docs/.hidden.md"), "# Hidden file\n");
    writeFile(repositoryRoot.resolve("docs/internal/notes.md"), "# Internal\n");
    writeFile(repositoryRoot.resolve("docs/private/notes.md"), "# Private\n");
    writeFile(repositoryRoot.resolve("docs/maintainer/notes.md"), "# Maintainer\n");
    writeFile(repositoryRoot.resolve("docs/secrets/notes.md"), "# Secrets\n");
    writeFile(repositoryRoot.resolve("docs/generated/guide.md"), "# Generated\n");
    writeFile(repositoryRoot.resolve("docs/generated-docs/guide.md"), "# Generated docs\n");
    writeFile(repositoryRoot.resolve("docs/target/guide.md"), "# Target\n");
    writeFile(repositoryRoot.resolve("docs/build/guide.md"), "# Build\n");
    writeFile(repositoryRoot.resolve("docs/out/guide.md"), "# Out\n");
    writeFile(repositoryRoot.resolve("docs/dist/guide.md"), "# Dist\n");
    writeFile(repositoryRoot.resolve("docs/node_modules/pkg/readme.md"), "# Dependency\n");

    DocumentDiscoveryAnalysis analysis = analyzer.analyze(
        repositoryRoot,
        List.of(supportedModule("module:.", ".", "pom.xml")));

    assertEquals(
        List.of("docs/public.md"),
        analysis.documents().stream().map(DocumentFileFact::path).toList());
  }

  @Test
  void skipsSymlinkedMarkdownFilesAndSymlinkedDirectoriesByDefault() throws Exception {
    Path repositoryRoot = repository("symlinks");
    Path outsideRoot = tempDir.resolve("outside-docs");
    writeFile(repositoryRoot.resolve("docs/visible.md"), "# Visible\n");
    writeFile(repositoryRoot.resolve("shared/inside.md"), "# Shared\n");
    writeFile(repositoryRoot.resolve("real-services/orders/README.md"), "# Orders\n");
    writeFile(outsideRoot.resolve("secret.md"), "# FAKE_OUTSIDE_MARKDOWN_SECRET\n");
    Files.createDirectories(repositoryRoot.resolve("docs"));
    Files.createDirectories(repositoryRoot.resolve("services/orders"));
    createSymbolicLink(
        repositoryRoot.resolve("docs/link-inside.md"),
        repositoryRoot.resolve("shared/inside.md"));
    createSymbolicLink(
        repositoryRoot.resolve("docs/linked-outside"),
        outsideRoot);
    createSymbolicLink(
        repositoryRoot.resolve("linked-services"),
        repositoryRoot.resolve("real-services"));
    createSymbolicLink(
        repositoryRoot.resolve("services/orders/README.md"),
        repositoryRoot.resolve("shared/inside.md"));

    DocumentDiscoveryAnalysis analysis = analyzer.analyze(
        repositoryRoot,
        List.of(
            supportedModule("module:services/orders", "services/orders", "services/orders/pom.xml"),
            supportedModule(
                "module:linked-services/orders",
                "linked-services/orders",
                "linked-services/orders/pom.xml")));

    assertAll(
        () -> assertEquals(
            List.of("docs/visible.md"),
            analysis.documents().stream().map(DocumentFileFact::path).toList()),
        () -> assertFalse(analysis.toString().contains("FAKE_OUTSIDE_MARKDOWN_SECRET")));
  }

  @Test
  void normalizesRepositoryRelativePathsAndPercentEncodesDocumentIds() throws Exception {
    Path repositoryRoot = repository("normalized");
    writeFile(repositoryRoot.resolve("docs/api specs:public/guide.md"), "# Guide\n");

    DocumentDiscoveryAnalysis analysis = analyzer.analyze(repositoryRoot, List.of());
    DocumentFileFact document = analysis.documents().get(0);

    assertAll(
        () -> assertEquals("docs/api specs:public/guide.md", document.path()),
        () -> assertEquals("document:docs/api%20specs%3Apublic/guide.md", document.id()),
        () -> assertFalse(Path.of(document.path()).isAbsolute()),
        () -> assertFalse(document.path().startsWith("./")),
        () -> assertFalse(document.path().contains("..")));
  }

  @Test
  void rejectsModuleReadmeCandidatesThatEscapeRepositoryRoot() throws Exception {
    Path repositoryRoot = repository("boundary");
    Path outsideRoot = tempDir.resolve("outside-module");
    writeFile(outsideRoot.resolve("README.md"), "# Outside\n");
    writeFile(repositoryRoot.resolve("docs/inside.md"), "# Inside\n");

    DocumentDiscoveryAnalysis analysis = analyzer.analyze(
        repositoryRoot,
        List.of(supportedModule("module:../outside-module", "../outside-module", "../outside-module/pom.xml")));

    assertEquals(
        List.of("docs/inside.md"),
        analysis.documents().stream().map(DocumentFileFact::path).toList());
  }

  @Test
  void reportsNotDetectedWhenNoDefaultScopeMarkdownExists() throws Exception {
    Path repositoryRoot = repository("empty");
    writeFile(repositoryRoot.resolve("notes/runbook.md"), "# Not default scope\n");

    DocumentDiscoveryAnalysis analysis = analyzer.analyze(repositoryRoot, List.of());

    assertAll(
        () -> assertEquals("not_detected", analysis.analysisStatus()),
        () -> assertEquals(List.of(), analysis.documents()),
        () -> assertEquals("default_local_markdown", analysis.discoveryPolicy().scope()),
        () -> assertEquals("skip_symlinks", analysis.discoveryPolicy().symlinkPolicy()));
  }

  private Path repository(String name) throws Exception {
    Path repositoryRoot = tempDir.resolve(name);
    Files.createDirectories(repositoryRoot);
    return repositoryRoot;
  }

  private void writeFile(Path path, String content) throws Exception {
    Files.createDirectories(path.getParent());
    Files.writeString(path, content);
  }

  private MavenModuleItem supportedModule(String moduleId, String modulePath, String pomPath) {
    return new MavenModuleItem(
        moduleId,
        modulePath,
        pomPath,
        List.of(),
        List.of(),
        "supported",
        "scan_root",
        modulePath,
        List.of(),
        List.of());
  }

  private void createSymbolicLink(Path link, Path target) throws Exception {
    try {
      Files.createSymbolicLink(link, target);
    } catch (UnsupportedOperationException | IOException | SecurityException exception) {
      assumeTrue(false, "symbolic links are unavailable: " + exception.getMessage());
    }
  }
}
