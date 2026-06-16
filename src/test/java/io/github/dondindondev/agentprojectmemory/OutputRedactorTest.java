package io.github.dondindondev.agentprojectmemory;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class OutputRedactorTest {
  @Test
  void redactsBoundedCredentialLikeValuesWhilePreservingSafeContext() {
    String redacted = OutputRedactor.redact(
        "password=\"FAKE_V170_PASSWORD\" token=FAKE_V170_TOKEN "
            + "Authorization: Bearer FAKE_V170_BEARER");

    assertAll(
        () -> assertTrue(redacted.contains("password=\"[REDACTED_SECRET_LIKE_VALUE]\"")),
        () -> assertTrue(redacted.contains("token=[REDACTED_SECRET_LIKE_VALUE]")),
        () -> assertTrue(redacted.contains("Authorization: Bearer [REDACTED_SECRET_LIKE_VALUE]")),
        () -> assertFalse(redacted.contains("FAKE_V170_PASSWORD")),
        () -> assertFalse(redacted.contains("FAKE_V170_TOKEN")),
        () -> assertFalse(redacted.contains("FAKE_V170_BEARER")));
  }

  @Test
  void redactsXmlCredentialValuesAndPrivateKeyMaterial() {
    String redacted = OutputRedactor.redact(
        "<clientSecret>FAKE_V170_XML_SECRET</clientSecret>\n"
            + "-----BEGIN PRIVATE KEY-----\n"
            + "FAKE_V170_PRIVATE_KEY\n"
            + "-----END PRIVATE KEY-----");

    assertAll(
        () -> assertTrue(redacted.contains(
            "<clientSecret>[REDACTED_SECRET_LIKE_VALUE]</clientSecret>")),
        () -> assertTrue(redacted.contains("[REDACTED_SECRET_LIKE_VALUE]")),
        () -> assertFalse(redacted.contains("FAKE_V170_XML_SECRET")),
        () -> assertFalse(redacted.contains("FAKE_V170_PRIVATE_KEY")));
  }

  @Test
  void fieldRedactionKeepsNavigationFieldsStable() {
    String id = "ev:src/main/java/com/example/Secret.java:1-1:token=FAKE_V170_ID_VALUE";
    String path = "src/main/java/com/example/password=FAKE_V170_PATH_VALUE/Secret.java";

    assertAll(
        () -> assertEquals(id, OutputRedactor.redactField("id", id)),
        () -> assertEquals(path, OutputRedactor.redactField("path", path)),
        () -> assertEquals(
            OutputRedactor.REDACTION_MARKER,
            OutputRedactor.redactField("client_secret", "FAKE_V170_FIELD_SECRET")));
  }

  @Test
  void keepsDocumentMentionTokensAsNavigationText() {
    assertEquals("mention token: /ghost", OutputRedactor.redact("mention token: /ghost"));
  }
}
