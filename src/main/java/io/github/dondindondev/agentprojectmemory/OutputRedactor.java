package io.github.dondindondev.agentprojectmemory;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class OutputRedactor {
  public static final String REDACTION_MARKER = "[REDACTED_SECRET_LIKE_VALUE]";

  private static final String CREDENTIAL_KEY_PATTERN =
      "[A-Za-z0-9_.-]*(?:password|passwd|pwd|token|secret|credential|private[-_]?key|"
          + "api[-_]?key|apikey|client[-_]?secret|access[-_]?key|authorization|auth)"
          + "[A-Za-z0-9_.-]*";
  private static final Pattern PRIVATE_KEY_BLOCK = Pattern.compile(
      "(?is)-----BEGIN [A-Z0-9 ]*PRIVATE KEY-----.*?(?:-----END [A-Z0-9 ]*PRIVATE KEY-----|$)");
  private static final Pattern XML_KEY_VALUE = Pattern.compile(
      "(?is)(<\\s*" + CREDENTIAL_KEY_PATTERN + "\\b[^>]*>)(.*?)(</\\s*[^>]+>)");
  private static final Pattern AUTHORIZATION_CREDENTIAL = Pattern.compile(
      "(?i)(\\b(?:authorization\\s*[:=]\\s*)?(?:bearer|basic)\\s+)"
          + "(?!\\[REDACTED_SECRET_LIKE_VALUE\\])([^\\s,;)}\\]<>\"'`]+)");
  private static final Pattern QUOTED_KEY_VALUE = Pattern.compile(
      "(?i)(\\b" + CREDENTIAL_KEY_PATTERN + "\\b\\s*[:=]\\s*)([\"'`])([^\"'`\\r\\n]*)(\\2)");
  private static final Pattern BARE_KEY_VALUE = Pattern.compile(
      "(?i)(\\b" + CREDENTIAL_KEY_PATTERN + "\\b\\s*[:=]\\s*)"
          + "(?!(?:bearer|basic)\\b|\\[REDACTED_SECRET_LIKE_VALUE\\])"
          + "([^\\s,;)}\\]<>\"'`]+)");

  private OutputRedactor() {
  }

  public static String redact(String value) {
    if (value == null || value.isEmpty()) {
      return value;
    }

    String redacted = PRIVATE_KEY_BLOCK.matcher(value).replaceAll(REDACTION_MARKER);
    redacted = replaceXmlKeyValues(redacted);
    redacted = replaceAuthorizationCredentials(redacted);
    redacted = replaceQuotedKeyValues(redacted);
    return replaceBareKeyValues(redacted);
  }

  public static String redactField(String fieldName, String value) {
    if (value == null) {
      return null;
    }
    if (isCredentialKey(fieldName)) {
      return REDACTION_MARKER;
    }
    return shouldRedactFreeTextField(fieldName) ? redact(value) : value;
  }

  public static String redactMapValue(String key, String value) {
    if (value == null) {
      return null;
    }
    return isCredentialKey(key) ? REDACTION_MARKER : redact(value);
  }

  public static boolean shouldRedactFreeTextField(String fieldName) {
    if (fieldName == null) {
      return false;
    }
    String normalized = normalizeKey(fieldName);
    return switch (normalized) {
      case "excerpt",
          "message",
          "title",
          "label",
          "display-name",
          "value",
          "reason",
          "tags" -> true;
      default -> false;
    };
  }

  public static boolean isCredentialKey(String key) {
    if (key == null || key.isBlank()) {
      return false;
    }
    String normalized = normalizeKey(key);
    return normalized.contains("password")
        || normalized.contains("passwd")
        || normalized.contains("pwd")
        || normalized.contains("token")
        || normalized.contains("secret")
        || normalized.contains("credential")
        || normalized.contains("private-key")
        || normalized.contains("apikey")
        || normalized.contains("api-key")
        || normalized.contains("client-secret")
        || normalized.contains("access-key")
        || normalized.contains("authorization")
        || normalized.equals("auth")
        || normalized.endsWith("-auth")
        || normalized.contains("-auth-");
  }

  private static String replaceXmlKeyValues(String value) {
    Matcher matcher = XML_KEY_VALUE.matcher(value);
    StringBuffer redacted = new StringBuffer();
    while (matcher.find()) {
      matcher.appendReplacement(
          redacted,
          Matcher.quoteReplacement(matcher.group(1) + REDACTION_MARKER + matcher.group(3)));
    }
    matcher.appendTail(redacted);
    return redacted.toString();
  }

  private static String replaceAuthorizationCredentials(String value) {
    Matcher matcher = AUTHORIZATION_CREDENTIAL.matcher(value);
    StringBuffer redacted = new StringBuffer();
    while (matcher.find()) {
      matcher.appendReplacement(
          redacted,
          Matcher.quoteReplacement(matcher.group(1) + REDACTION_MARKER));
    }
    matcher.appendTail(redacted);
    return redacted.toString();
  }

  private static String replaceQuotedKeyValues(String value) {
    Matcher matcher = QUOTED_KEY_VALUE.matcher(value);
    StringBuffer redacted = new StringBuffer();
    while (matcher.find()) {
      matcher.appendReplacement(
          redacted,
          Matcher.quoteReplacement(
              matcher.group(1) + matcher.group(2) + REDACTION_MARKER + matcher.group(4)));
    }
    matcher.appendTail(redacted);
    return redacted.toString();
  }

  private static String replaceBareKeyValues(String value) {
    Matcher matcher = BARE_KEY_VALUE.matcher(value);
    StringBuffer redacted = new StringBuffer();
    while (matcher.find()) {
      if (isDocumentMentionToken(matcher.start(1), value)) {
        matcher.appendReplacement(redacted, Matcher.quoteReplacement(matcher.group(0)));
        continue;
      }
      matcher.appendReplacement(
          redacted,
          Matcher.quoteReplacement(matcher.group(1) + REDACTION_MARKER));
    }
    matcher.appendTail(redacted);
    return redacted.toString();
  }

  private static boolean isDocumentMentionToken(int keyStart, String value) {
    String prefix = "mention ";
    return keyStart >= prefix.length()
        && value.regionMatches(true, keyStart - prefix.length(), prefix, 0, prefix.length());
  }

  private static String normalizeKey(String key) {
    return key.toLowerCase(Locale.ROOT)
        .replace('_', '-')
        .replace('.', '-')
        .replace(' ', '-');
  }
}
