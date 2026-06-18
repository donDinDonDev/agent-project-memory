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
  void redactsWrappedAuthorizationCredentialsWhilePreservingDelimiters() {
    String redacted = OutputRedactor.redact(
        "Authorization: Bearer \"FAKE_V200_QUOTED_BEARER\" "
            + "authorization=Basic 'FAKE_V200_QUOTED_BASIC' "
            + "Bearer `FAKE_V200_BACKTICK_BEARER` "
            + "Basic <FAKE_V200_ANGLE_BASIC> "
            + "Authorization: Bearer \\\"FAKE_V200_ESCAPED_QUOTED_BEARER\\\" "
            + "Authorization: Bearer FAKE_V200_BARE_BACKSLASH\\TAIL");

    assertAll(
        () -> assertTrue(redacted.contains(
            "Authorization: Bearer \"[REDACTED_SECRET_LIKE_VALUE]\"")),
        () -> assertTrue(redacted.contains(
            "authorization=Basic '[REDACTED_SECRET_LIKE_VALUE]'")),
        () -> assertTrue(redacted.contains(
            "Bearer `[REDACTED_SECRET_LIKE_VALUE]`")),
        () -> assertTrue(redacted.contains(
            "Basic <[REDACTED_SECRET_LIKE_VALUE]>")),
        () -> assertTrue(redacted.contains(
            "Authorization: Bearer \\\"[REDACTED_SECRET_LIKE_VALUE]\\\"")),
        () -> assertTrue(redacted.contains(
            "Authorization: Bearer [REDACTED_SECRET_LIKE_VALUE]")),
        () -> assertFalse(redacted.contains("FAKE_V200_QUOTED_BEARER")),
        () -> assertFalse(redacted.contains("FAKE_V200_QUOTED_BASIC")),
        () -> assertFalse(redacted.contains("FAKE_V200_BACKTICK_BEARER")),
        () -> assertFalse(redacted.contains("FAKE_V200_ANGLE_BASIC")),
        () -> assertFalse(redacted.contains("FAKE_V200_ESCAPED_QUOTED_BEARER")),
        () -> assertFalse(redacted.contains("FAKE_V200_BARE_BACKSLASH")),
        () -> assertFalse(redacted.contains("TAIL")));
  }

  @Test
  void redactsJsonStyleQuotedCredentialKeysWhilePreservingNonCredentialValues() {
    String redacted = OutputRedactor.redact(
        "{\"password\":\"FAKE_V170_JSON_PASSWORD\","
            + "\"client_secret\" : \"FAKE_V170_JSON_CLIENT_SECRET\","
            + "\"display_name\":\"visible\"}");

    assertAll(
        () -> assertTrue(redacted.contains(
            "\"password\":\"[REDACTED_SECRET_LIKE_VALUE]\"")),
        () -> assertTrue(redacted.contains(
            "\"client_secret\" : \"[REDACTED_SECRET_LIKE_VALUE]\"")),
        () -> assertTrue(redacted.contains("\"display_name\":\"visible\"")),
        () -> assertFalse(redacted.contains("FAKE_V170_JSON_PASSWORD")),
        () -> assertFalse(redacted.contains("FAKE_V170_JSON_CLIENT_SECRET")));
  }

  @Test
  void redactsJsonStyleQuotedCredentialValuesWithEscapedQuotes() {
    String redacted = OutputRedactor.redact(
        "{\"password\":\"FAKE_V170_JSON_PREFIX\\\"FAKE_V170_JSON_SUFFIX\"} "
            + "{\\\"password\\\":\\\"FAKE_V170_SOURCE_PREFIX\\\\\\\"FAKE_V170_SOURCE_SUFFIX\\\"}");

    assertAll(
        () -> assertTrue(redacted.contains(
            "\"password\":\"[REDACTED_SECRET_LIKE_VALUE]\"")),
        () -> assertTrue(redacted.contains(
            "\\\"password\\\":\\\"[REDACTED_SECRET_LIKE_VALUE]\\\"")),
        () -> assertFalse(redacted.contains("FAKE_V170_JSON_PREFIX")),
        () -> assertFalse(redacted.contains("FAKE_V170_JSON_SUFFIX")),
        () -> assertFalse(redacted.contains("FAKE_V170_SOURCE_PREFIX")),
        () -> assertFalse(redacted.contains("FAKE_V170_SOURCE_SUFFIX")));
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
