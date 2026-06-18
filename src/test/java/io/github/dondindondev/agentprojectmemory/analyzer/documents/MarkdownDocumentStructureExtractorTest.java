package io.github.dondindondev.agentprojectmemory.analyzer.documents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import io.github.dondindondev.agentprojectmemory.analyzer.ScanPathContainment;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class MarkdownDocumentStructureExtractorTest {
  @TempDir
  private Path tempDir;

  private final MarkdownDocumentStructureExtractor extractor =
      new MarkdownDocumentStructureExtractor();

  @Test
  void extractsMarkdownStructureThroughStableNoFollowRead() throws Exception {
    Path markdown = tempDir.resolve("README.md");
    Files.writeString(markdown, "# Root\nBody\n## Child\n");

    DocumentStructure structure = extractor.extract(markdown, "README.md", 10, 10, 128);

    assertEquals(
        List.of("Root", "Child"),
        structure.headings().stream().map(DocumentHeadingFact::title).toList());
  }

  @Test
  void rejectsSymlinkedMarkdownFileWithoutReadingTargetContent() throws Exception {
    Path outside = tempDir.resolve("outside.md");
    Path link = tempDir.resolve("README.md");
    Files.writeString(outside, "# FAKE_SYMLINKED_MARKDOWN_SECRET\n");
    createSymbolicLink(link, outside);

    IOException exception = assertThrows(
        IOException.class,
        () -> extractor.extract(link, "README.md", 10, 10, 1024));

    assertFalse(exception.getMessage().contains("FAKE_SYMLINKED_MARKDOWN_SECRET"));
    assertFalse(exception.getMessage().contains(outside.toString()));
  }

  @Test
  void rejectsHardlinkedMarkdownFileWithoutReadingLinkedContent() throws Exception {
    Path outside = tempDir.resolve("outside-hardlinked.md");
    Path link = tempDir.resolve("README.md");
    Files.writeString(outside, "# FAKE_HARDLINKED_MARKDOWN_SECRET\n");
    createHardLink(link, outside);

    IOException exception = assertThrows(
        IOException.class,
        () -> extractor.extract(link, "README.md", 10, 10, 1024));

    assertFalse(exception.getMessage().contains("FAKE_HARDLINKED_MARKDOWN_SECRET"));
    assertFalse(exception.getMessage().contains(outside.toString()));
  }

  @Test
  void enforcesStableReadByteLimitBeforeExtractingStructure() throws Exception {
    Path markdown = tempDir.resolve("README.md");
    Files.writeString(markdown, "# Root\n");

    assertThrows(
        ScanPathContainment.FileSizeLimitExceededException.class,
        () -> extractor.extract(markdown, "README.md", 10, 10, 4));
  }

  private void createSymbolicLink(Path link, Path target) throws Exception {
    try {
      Files.createSymbolicLink(link, target);
    } catch (UnsupportedOperationException | IOException | SecurityException exception) {
      assumeTrue(false, "symbolic links are unavailable: " + exception.getMessage());
    }
  }

  private void createHardLink(Path link, Path existing) throws Exception {
    try {
      Files.createLink(link, existing);
    } catch (UnsupportedOperationException | IOException | SecurityException exception) {
      assumeTrue(false, "hard links are unavailable: " + exception.getMessage());
    }
  }
}
