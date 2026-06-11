package io.github.dondindondev.agentprojectmemory.analyzer.documents;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenModuleItem;
import io.github.dondindondev.agentprojectmemory.scanconfig.ScanConfigPathPattern;
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
  void discoversDefaultScopeMarkdownWithDeterministicStructure() throws Exception {
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
    DocumentHeadingFact rootHeading = documents.get(1).headings().get(0);
    DocumentChunkFact rootChunk = documents.get(1).chunks().get(0);
    DocumentEvidence rootFileEvidence = evidence(
        analysis,
        "ev:README.md:unknown:document:file:README.md");
    DocumentEvidence rootHeadingEvidence = evidence(
        analysis,
        "ev:README.md:1-1:document:heading:Root:decl:000001");
    DocumentEvidence rootChunkEvidence = evidence(
        analysis,
        "ev:README.md:1-1:document:chunk:000001");

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
        () -> assertEquals("Root", documents.get(1).title()),
        () -> assertEquals("first_heading", documents.get(1).titleSource()),
        () -> assertEquals("local_markdown", documents.get(1).documentKind()),
        () -> assertEquals("markdown", documents.get(1).format()),
        () -> assertEquals("document_heading:README.md:heading:Root:occ:000001", rootHeading.id()),
        () -> assertEquals(1, rootHeading.level()),
        () -> assertEquals("Root", rootHeading.title()),
        () -> assertEquals("root", rootHeading.anchor()),
        () -> assertEquals(1, rootHeading.lineStart()),
        () -> assertEquals(1, rootHeading.lineEnd()),
        () -> assertEquals("document_chunk:README.md:chunk:000001", rootChunk.id()),
        () -> assertEquals(rootHeading.id(), rootChunk.headingId()),
        () -> assertEquals(1, rootChunk.lineStart()),
        () -> assertEquals(1, rootChunk.lineEnd()),
        () -> assertEquals("not_serialized", rootChunk.contentStatus()),
        () -> assertEquals(List.of(rootFileEvidence.id()), documents.get(1).evidenceIds()),
        () -> assertEquals(List.of(rootHeadingEvidence.id()), rootHeading.evidenceIds()),
        () -> assertEquals(List.of(rootChunkEvidence.id()), rootChunk.evidenceIds()),
        () -> assertEquals(18, analysis.evidence().size()),
        () -> assertEquals("document", rootFileEvidence.sourceType()),
        () -> assertEquals("README.md", rootFileEvidence.sourcePath()),
        () -> assertEquals("file:README.md", rootFileEvidence.symbolName()),
        () -> assertNull(rootFileEvidence.lineStart()),
        () -> assertNull(rootFileEvidence.lineEnd()),
        () -> assertEquals("markdown file detected: README.md", rootFileEvidence.excerpt()),
        () -> assertEquals("high", rootFileEvidence.confidence()),
        () -> assertEquals("heading:Root", rootHeadingEvidence.symbolName()),
        () -> assertEquals("# Root", rootHeadingEvidence.excerpt()),
        () -> assertEquals("chunk:000001", rootChunkEvidence.symbolName()),
        () -> assertEquals("chunk lines 1-1; heading: Root", rootChunkEvidence.excerpt()));
  }

  @Test
  void extractsNestedDuplicateHeadingsAndNearestOwningChunks() throws Exception {
    DocumentFileFact document = readmeDocument(
        "nested-duplicate",
        """
            Intro
            # Root
            Root body
            ## Child
            Child body
            ### Child
            ```
            # Not a heading in a fence
            ```
            # Root
            Tail
            """);

    List<DocumentHeadingFact> headings = document.headings();
    List<DocumentChunkFact> chunks = document.chunks();

    assertAll(
        () -> assertEquals("Root", document.title()),
        () -> assertEquals("first_heading", document.titleSource()),
        () -> assertEquals(4, headings.size()),
        () -> assertEquals(List.of(1, 2, 3, 1), headings.stream().map(DocumentHeadingFact::level).toList()),
        () -> assertEquals(List.of("Root", "Child", "Child", "Root"),
            headings.stream().map(DocumentHeadingFact::title).toList()),
        () -> assertEquals(List.of("root", "child", "child-2", "root-2"),
            headings.stream().map(DocumentHeadingFact::anchor).toList()),
        () -> assertEquals(4L, headings.stream().map(DocumentHeadingFact::id).distinct().count()),
        () -> assertEquals(5, chunks.size()),
        () -> assertNull(chunks.get(0).headingId()),
        () -> assertEquals(1, chunks.get(0).lineStart()),
        () -> assertEquals(1, chunks.get(0).lineEnd()),
        () -> assertEquals(headings.get(0).id(), chunks.get(1).headingId()),
        () -> assertEquals(2, chunks.get(1).lineStart()),
        () -> assertEquals(3, chunks.get(1).lineEnd()),
        () -> assertEquals(headings.get(2).id(), chunks.get(3).headingId()),
        () -> assertEquals(6, chunks.get(3).lineStart()),
        () -> assertEquals(9, chunks.get(3).lineEnd()),
        () -> assertEquals(headings.get(3).id(), chunks.get(4).headingId()),
        () -> assertEquals(10, chunks.get(4).lineStart()),
        () -> assertEquals(11, chunks.get(4).lineEnd()));
  }

  @Test
  void ignoresMalformedAtxHeadingLikeLines() throws Exception {
    DocumentFileFact document = readmeDocument(
        "malformed-headings",
        """
            #NoSpace
            ####### Too deep
                # Indented code
            ### Valid ###
            """);

    assertAll(
        () -> assertEquals(1, document.headings().size()),
        () -> assertEquals(3, document.headings().get(0).level()),
        () -> assertEquals("Valid", document.headings().get(0).title()),
        () -> assertEquals(4, document.headings().get(0).lineStart()));
  }

  @Test
  void keepsEmptyMarkdownDocumentBounded() throws Exception {
    DocumentFileFact document = readmeDocument("empty-markdown", "");

    assertAll(
        () -> assertEquals("README", document.title()),
        () -> assertEquals("filename", document.titleSource()),
        () -> assertEquals(List.of(), document.headings()),
        () -> assertEquals(List.of(), document.chunks()));
  }

  @Test
  void usesFirstNonBlankHeadingAsDocumentTitle() throws Exception {
    DocumentFileFact document = readmeDocument(
        "blank-heading-before-title",
        """
            #
            ## Visible title
            """);

    assertAll(
        () -> assertEquals("Visible title", document.title()),
        () -> assertEquals("first_heading", document.titleSource()),
        () -> assertEquals(2, document.headings().size()),
        () -> assertEquals("", document.headings().get(0).title()),
        () -> assertNull(document.headings().get(0).anchor()));
  }

  @Test
  void boundsLongHeadingTitlesAndAnchors() throws Exception {
    String longHeading = "A".repeat(160);
    DocumentFileFact document = readmeDocument("long-heading", "# " + longHeading + "\n");
    DocumentHeadingFact heading = document.headings().get(0);

    assertAll(
        () -> assertEquals("A".repeat(120) + "...", heading.title()),
        () -> assertEquals(123, heading.title().length()),
        () -> assertEquals("a".repeat(120), heading.anchor()),
        () -> assertTrue(heading.id().startsWith("document_heading:README.md:heading:")));
  }

  @Test
  void createsNoHeadingFallbackChunksWithoutSerializingContent() throws Exception {
    Path repositoryRoot = repository("no-heading");
    writeFile(
        repositoryRoot.resolve("README.md"),
        """
            Intro
            Body
            """);
    DocumentDiscoveryAnalysis analysis = analyzer.analyze(repositoryRoot, List.of());
    DocumentFileFact document = analysis.documents().get(0);

    assertAll(
        () -> assertEquals(List.of(), document.headings()),
        () -> assertEquals(1, document.chunks().size()),
        () -> assertNull(document.chunks().get(0).headingId()),
        () -> assertEquals(1, document.chunks().get(0).lineStart()),
        () -> assertEquals(2, document.chunks().get(0).lineEnd()),
        () -> assertEquals("not_serialized", document.chunks().get(0).contentStatus()),
        () -> assertEquals(
            List.of("ev:README.md:1-2:document:chunk:000001"),
            document.chunks().get(0).evidenceIds()),
        () -> assertEquals(
            "chunk lines 1-2; heading: none",
            evidence(analysis, "ev:README.md:1-2:document:chunk:000001").excerpt()),
        () -> assertFalse(analysis.evidence().stream()
            .map(DocumentEvidence::excerpt)
            .anyMatch(excerpt -> excerpt.contains("Intro") || excerpt.contains("Body"))));
  }

  @Test
  void splitsLongChunksByDeterministicLineBounds() throws Exception {
    StringBuilder content = new StringBuilder();
    for (int index = 1; index <= 85; index++) {
      content.append("Line ").append(index).append('\n');
    }

    DocumentFileFact document = readmeDocument("long-chunk", content.toString());

    assertAll(
        () -> assertEquals(2, document.chunks().size()),
        () -> assertEquals(1, document.chunks().get(0).lineStart()),
        () -> assertEquals(80, document.chunks().get(0).lineEnd()),
        () -> assertEquals(81, document.chunks().get(1).lineStart()),
        () -> assertEquals(85, document.chunks().get(1).lineEnd()),
        () -> assertEquals("document_chunk:README.md:chunk:000001", document.chunks().get(0).id()),
        () -> assertEquals("document_chunk:README.md:chunk:000002", document.chunks().get(1).id()));
  }

  @Test
  void normalizesControlCharactersInHeadingTitles() throws Exception {
    String bidirectionalOverride = "\u202E";
    DocumentFileFact document = readmeDocument(
        "control-heading",
        "# Safe\u0000" + bidirectionalOverride + "\tTitle\n");
    DocumentHeadingFact heading = document.headings().get(0);

    assertAll(
        () -> assertEquals("Safe\\u0000\\u202E Title", heading.title()),
        () -> assertEquals("safe-u0000-u202e-title", heading.anchor()),
        () -> assertFalse(heading.title().contains("\u0000")),
        () -> assertFalse(heading.title().contains(bidirectionalOverride)));
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

  @Test
  void appliesUserIncludesAndExcludesWithoutOverridingSafetyExclusions() throws Exception {
    Path repositoryRoot = repository("user-rules");
    writeFile(repositoryRoot.resolve("docs/public.md"), "# Public\n");
    writeFile(repositoryRoot.resolve("notes/guide.md"), "# Guide\n");
    writeFile(repositoryRoot.resolve("notes/archive/old.md"), "# Old\n");
    writeFile(repositoryRoot.resolve("notes/manual.markdown"), "# Manual\n");
    writeFile(repositoryRoot.resolve("docs/private/secret.md"), "# FAKE_PRIVATE_MARKDOWN_SECRET\n");
    writeFile(repositoryRoot.resolve(".hidden/secret.md"), "# FAKE_HIDDEN_MARKDOWN_SECRET\n");

    DocumentDiscoveryAnalysis analysis = analyzer.analyze(
        repositoryRoot,
        List.of(supportedModule("module:.", ".", "pom.xml")),
        new DocumentDiscoveryOptions(
            true,
            List.of(
                ScanConfigPathPattern.parse("notes/**/*.md", "documents.include[0]", true),
                ScanConfigPathPattern.parse("notes/*.markdown", "documents.include[1]", true),
                ScanConfigPathPattern.parse("docs/private/*.md", "documents.include[2]", true),
                ScanConfigPathPattern.parse(".hidden/*.md", "documents.include[3]", true)),
            List.of(ScanConfigPathPattern.parse("notes/archive/**", "documents.exclude[0]", false))));

    assertAll(
        () -> assertEquals(
            List.of("docs/public.md", "notes/guide.md", "notes/manual.markdown"),
            analysis.documents().stream().map(DocumentFileFact::path).toList()),
        () -> assertEquals(
            List.of("docs_tree", "explicit_include", "explicit_include"),
            analysis.documents().stream().map(DocumentFileFact::discoverySource).toList()),
        () -> assertFalse(analysis.toString().contains("FAKE_PRIVATE_MARKDOWN_SECRET")),
        () -> assertFalse(analysis.toString().contains("FAKE_HIDDEN_MARKDOWN_SECRET")),
        () -> assertFalse(analysis.documents().stream()
            .map(DocumentFileFact::path)
            .anyMatch(path -> path.contains("archive") || path.contains("private") || path.contains(".hidden"))));
  }

  @Test
  void reportsNotAnalyzedWhenLocalMarkdownIsDisabled() throws Exception {
    Path repositoryRoot = repository("disabled");
    writeFile(repositoryRoot.resolve("README.md"), "# Root\n");

    DocumentDiscoveryAnalysis analysis = analyzer.analyze(
        repositoryRoot,
        List.of(),
        new DocumentDiscoveryOptions(false, List.of(), List.of()));

    assertAll(
        () -> assertEquals("not_analyzed", analysis.analysisStatus()),
        () -> assertEquals(List.of(), analysis.documents()),
        () -> assertEquals(List.of(), analysis.evidence()),
        () -> assertEquals("default_local_markdown", analysis.discoveryPolicy().scope()));
  }

  private Path repository(String name) throws Exception {
    Path repositoryRoot = tempDir.resolve(name);
    Files.createDirectories(repositoryRoot);
    return repositoryRoot;
  }

  private DocumentFileFact readmeDocument(String repositoryName, String content) throws Exception {
    Path repositoryRoot = repository(repositoryName);
    writeFile(repositoryRoot.resolve("README.md"), content);
    DocumentDiscoveryAnalysis analysis = analyzer.analyze(repositoryRoot, List.of());
    return analysis.documents().get(0);
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

  private DocumentEvidence evidence(DocumentDiscoveryAnalysis analysis, String id) {
    return analysis.evidence().stream()
        .filter(evidence -> id.equals(evidence.id()))
        .findFirst()
        .orElseThrow();
  }
}
