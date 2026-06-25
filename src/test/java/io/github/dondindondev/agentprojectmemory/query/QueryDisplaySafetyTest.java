package io.github.dondindondev.agentprojectmemory.query;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.dondindondev.agentprojectmemory.OutputRedactor;
import org.junit.jupiter.api.Test;

final class QueryDisplaySafetyTest {
  @Test
  void sanitizeLocatorPreservesBoundedPasswdDeclTokenWithoutWeakeningOtherRedaction() {
    String locator = "ev:README.md:132-132:document:mention:/passwd:decl:000038";

    assertEquals(locator, QueryDisplaySafety.sanitizeLocator(locator));
    assertEquals(OutputRedactor.REDACTION_MARKER, QueryDisplaySafety.sanitize(locator));
    assertEquals(
        OutputRedactor.REDACTION_MARKER,
        QueryDisplaySafety.sanitizeLocator("ev:/etc/passwd:decl:000038"));
    assertEquals(
        OutputRedactor.REDACTION_MARKER,
        QueryDisplaySafety.sanitizeLocator(locator + " /Users/example/.ssh/id_rsa"));
    assertEquals(
        "password=" + OutputRedactor.REDACTION_MARKER,
        QueryDisplaySafety.sanitizeLocator("password=/passwd:decl:000038"));
  }
}
