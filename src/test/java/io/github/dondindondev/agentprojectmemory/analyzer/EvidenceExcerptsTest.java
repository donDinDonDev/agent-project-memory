package io.github.dondindondev.agentprojectmemory.analyzer;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class EvidenceExcerptsTest {
  @Test
  void boundedRedactsBeforeApplyingExcerptLimit() {
    String excerpt = "@GetMapping(headers = \"Authorization: Bearer FAKE_V170_EXCERPT_SECRET\") "
        + "A".repeat(1_000);

    String redacted = EvidenceExcerpts.bounded(excerpt);

    assertAll(
        () -> assertTrue(redacted.contains("[REDACTED_SECRET_LIKE_VALUE]")),
        () -> assertFalse(redacted.contains("FAKE_V170_EXCERPT_SECRET")),
        () -> assertTrue(redacted.length() <= EvidenceExcerpts.MAX_EXCERPT_LENGTH + 3),
        () -> assertTrue(redacted.startsWith("@GetMapping(headers = \"Authorization: Bearer ")));
  }
}
