package io.github.dondindondev.agentprojectmemory.scanconfig;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

final class ScanConfigPathPatternTest {
  @Test
  void matchesExistingValidPatternSemantics() throws Exception {
    ScanConfigPathPattern recursiveMarkdown =
        ScanConfigPathPattern.parse("notes/**/*.md", "documents.include[0]", true);
    ScanConfigPathPattern treeExclude =
        ScanConfigPathPattern.parse("docs/archive/**", "documents.exclude[0]", false);
    ScanConfigPathPattern singleSegment =
        ScanConfigPathPattern.parse("docs/*.md", "documents.include[1]", true);
    ScanConfigPathPattern rootMarkdown =
        ScanConfigPathPattern.parse("README.markdown", "documents.include[2]", true);

    assertAll(
        () -> assertTrue(recursiveMarkdown.matches("notes/guide.md")),
        () -> assertTrue(recursiveMarkdown.matches("notes/a/b/guide.md")),
        () -> assertFalse(recursiveMarkdown.matches("notes/a/b/guide.txt")),
        () -> assertTrue(treeExclude.matches("docs/archive")),
        () -> assertTrue(treeExclude.matches("docs/archive/old.md")),
        () -> assertTrue(singleSegment.matches("docs/guide.md")),
        () -> assertFalse(singleSegment.matches("docs/nested/guide.md")),
        () -> assertTrue(rootMarkdown.matches("README.markdown")),
        () -> assertFalse(rootMarkdown.matches("README.md")));
  }

  @Test
  void matchesMultipleRecursiveSegments() throws Exception {
    ScanConfigPathPattern pattern =
        ScanConfigPathPattern.parse("**/api/**/v1/**/*.md", "documents.include[0]", true);

    assertAll(
        () -> assertTrue(pattern.matches("api/v1/guide.md")),
        () -> assertTrue(pattern.matches("docs/api/reference/v1/guide.md")),
        () -> assertTrue(pattern.matches("a/api/b/c/v1/d/e/guide.md")),
        () -> assertFalse(pattern.matches("docs/api/reference/v2/guide.md")),
        () -> assertFalse(pattern.matches("docs/reference/v1/guide.md")));
  }

  @Test
  void boundsDeepFalseMatchesWithManyRecursiveSegments() throws Exception {
    ScanConfigPathPattern pattern = ScanConfigPathPattern.parse(
        "**/a/**/a/**/a/**/a/**/a/**/a/**/a/**/a/**/z.md",
        "documents.include[0]",
        true);
    List<String> segments = new ArrayList<>();
    for (int index = 0; index < 32; index++) {
      segments.add("a");
    }
    segments.add("not-z.md");
    String deepFalsePath = String.join("/", segments);

    assertTimeout(
        Duration.ofSeconds(1),
        () -> assertFalse(pattern.matches(deepFalsePath)));
  }

  @Test
  void falseMatchesRemainControlledForRepeatedChecks() throws Exception {
    ScanConfigPathPattern pattern = ScanConfigPathPattern.parse(
        "**/a/**/b/**/c/**/d/**/target.md",
        "documents.include[0]",
        true);
    String deepFalsePath = "a/".repeat(40) + "miss.md";

    assertTimeout(Duration.ofSeconds(1), () -> {
      for (int index = 0; index < 1_000; index++) {
        assertFalse(pattern.matches(deepFalsePath));
      }
    });
  }
}
