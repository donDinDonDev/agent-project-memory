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
  private static final Pattern CREDENTIAL_KEY = Pattern.compile("(?i)" + CREDENTIAL_KEY_PATTERN);
  private static final Pattern PRIVATE_KEY_BLOCK = Pattern.compile(
      "(?is)-----BEGIN [A-Z0-9 ]*PRIVATE KEY-----.*?(?:-----END [A-Z0-9 ]*PRIVATE KEY-----|$)");
  private static final Pattern XML_KEY_VALUE = Pattern.compile(
      "(?is)(<\\s*" + CREDENTIAL_KEY_PATTERN + "\\b[^>]*>)(.*?)(</\\s*[^>]+>)");
  private static final Pattern AUTHORIZATION_CREDENTIAL_PREFIX = Pattern.compile(
      "(?i)\\b(?:authorization\\s*[:=]\\s*)?(?:bearer|basic)\\s+");
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
    redacted = replaceJsonStyleQuotedKeyValues(redacted);
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
    Matcher matcher = AUTHORIZATION_CREDENTIAL_PREFIX.matcher(value);
    StringBuilder redacted = new StringBuilder(value.length());
    int copiedUntil = 0;
    int searchFrom = 0;
    while (matcher.find(searchFrom)) {
      AuthorizationCredential credential = authorizationCredentialAt(value, matcher.end());
      if (credential == null) {
        searchFrom = matcher.end();
        continue;
      }
      redacted.append(value, copiedUntil, matcher.end());
      redacted.append(credential.opening());
      redacted.append(REDACTION_MARKER);
      redacted.append(credential.closing());
      copiedUntil = credential.end();
      searchFrom = credential.end();
    }
    redacted.append(value, copiedUntil, value.length());
    return redacted.toString();
  }

  private static AuthorizationCredential authorizationCredentialAt(String value, int start) {
    if (startsWithRedactionMarker(value, start)) {
      return null;
    }
    AuthorizationWrapper wrapper = authorizationWrapperAt(value, start);
    if (wrapper != null) {
      if (startsWithRedactionMarker(value, wrapper.contentStart())) {
        return null;
      }
      int closingStart = findAuthorizationWrapperClosing(value, wrapper);
      if (closingStart >= wrapper.contentStart()) {
        String closing = value.substring(closingStart, closingStart + wrapper.closingLength());
        return new AuthorizationCredential(
            value.substring(start, wrapper.contentStart()),
            closing,
            closingStart + wrapper.closingLength());
      }
      int unclosedEnd = authorizationCredentialEnd(value, wrapper.contentStart());
      if (unclosedEnd > wrapper.contentStart()) {
        return new AuthorizationCredential(
            value.substring(start, wrapper.contentStart()),
            "",
            unclosedEnd);
      }
      return null;
    }

    int end = authorizationCredentialEnd(value, start);
    if (end <= start) {
      return null;
    }
    return new AuthorizationCredential("", "", end);
  }

  private static AuthorizationWrapper authorizationWrapperAt(String value, int start) {
    if (start >= value.length()) {
      return null;
    }
    char current = value.charAt(start);
    if (current == '<') {
      return new AuthorizationWrapper(start + 1, '>', false);
    }
    QuoteBoundary quoteBoundary = quoteBoundaryAt(value, start);
    if (quoteBoundary != null) {
      return new AuthorizationWrapper(
          quoteBoundary.end(),
          quoteBoundary.quote(),
          quoteBoundary.escaped());
    }
    return null;
  }

  private static int findAuthorizationWrapperClosing(String value, AuthorizationWrapper wrapper) {
    if (wrapper.closing() == '>') {
      for (int index = wrapper.contentStart(); index < value.length(); index++) {
        char current = value.charAt(index);
        if (current == '\r' || current == '\n') {
          return -1;
        }
        if (current == '>') {
          return index;
        }
      }
      return -1;
    }
    QuoteBoundary openingBoundary = new QuoteBoundary(
        wrapper.contentStart() - wrapper.openingLength(),
        wrapper.contentStart(),
        wrapper.closing(),
        wrapper.escaped());
    QuoteBoundary closingBoundary = findClosingQuoteBoundary(
        value,
        wrapper.contentStart(),
        openingBoundary);
    return closingBoundary == null ? -1 : closingBoundary.start();
  }

  private static int authorizationCredentialEnd(String value, int start) {
    int index = start;
    while (index < value.length() && !isAuthorizationCredentialDelimiter(value.charAt(index))) {
      index++;
    }
    return index;
  }

  private static boolean isAuthorizationCredentialDelimiter(char value) {
    return Character.isWhitespace(value)
        || value == ','
        || value == ';'
        || value == ')'
        || value == '}'
        || value == ']'
        || value == '<'
        || value == '>'
        || value == '"'
        || value == '\''
        || value == '`';
  }

  private static boolean startsWithRedactionMarker(String value, int start) {
    return start >= 0 && value.startsWith(REDACTION_MARKER, start);
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

  private static String replaceJsonStyleQuotedKeyValues(String value) {
    StringBuilder redacted = new StringBuilder(value.length());
    int copiedUntil = 0;
    int searchFrom = 0;
    while (searchFrom < value.length()) {
      QuoteBoundary keyOpen = findNextQuoteBoundary(value, searchFrom);
      if (keyOpen == null) {
        break;
      }
      QuoteBoundary keyClose = findClosingQuoteBoundary(value, keyOpen.end(), keyOpen);
      if (keyClose == null) {
        searchFrom = nextLineStart(value, keyOpen.end());
        continue;
      }
      String key = value.substring(keyOpen.end(), keyClose.start());
      if (!CREDENTIAL_KEY.matcher(key).matches()) {
        searchFrom = keyOpen.end();
        continue;
      }
      int delimiterIndex = skipWhitespace(value, keyClose.end());
      if (delimiterIndex >= value.length()
          || (value.charAt(delimiterIndex) != ':' && value.charAt(delimiterIndex) != '=')) {
        searchFrom = keyClose.end();
        continue;
      }
      int valueOpenIndex = skipWhitespace(value, delimiterIndex + 1);
      QuoteBoundary valueOpen = quoteBoundaryAt(value, valueOpenIndex);
      if (valueOpen == null) {
        searchFrom = delimiterIndex + 1;
        continue;
      }
      QuoteBoundary valueClose = findClosingQuoteBoundary(value, valueOpen.end(), valueOpen);
      if (valueClose == null) {
        searchFrom = nextLineStart(value, valueOpen.end());
        continue;
      }
      redacted.append(value, copiedUntil, valueOpen.end());
      redacted.append(REDACTION_MARKER);
      copiedUntil = valueClose.start();
      searchFrom = valueClose.end();
    }
    redacted.append(value, copiedUntil, value.length());
    return redacted.toString();
  }

  private static QuoteBoundary findNextQuoteBoundary(String value, int startIndex) {
    for (int index = Math.max(0, startIndex); index < value.length(); index++) {
      QuoteBoundary boundary = quoteBoundaryAt(value, index);
      if (boundary != null) {
        return boundary;
      }
    }
    return null;
  }

  private static QuoteBoundary findClosingQuoteBoundary(
      String value,
      int startIndex,
      QuoteBoundary openingBoundary) {
    for (int index = startIndex; index < value.length(); index++) {
      char current = value.charAt(index);
      if (current == '\r' || current == '\n') {
        return null;
      }
      QuoteBoundary boundary = quoteBoundaryAt(value, index);
      if (boundary != null
          && boundary.quote() == openingBoundary.quote()
          && boundary.escaped() == openingBoundary.escaped()) {
        return boundary;
      }
    }
    return null;
  }

  private static QuoteBoundary quoteBoundaryAt(String value, int index) {
    if (index < 0 || index >= value.length()) {
      return null;
    }
    char current = value.charAt(index);
    if (current == '\\' && index + 1 < value.length() && isQuote(value.charAt(index + 1))
        && countPrecedingBackslashes(value, index) == 0) {
      return new QuoteBoundary(index, index + 2, value.charAt(index + 1), true);
    }
    if (isQuote(current) && countPrecedingBackslashes(value, index) % 2 == 0) {
      return new QuoteBoundary(index, index + 1, current, false);
    }
    return null;
  }

  private static int countPrecedingBackslashes(String value, int index) {
    int count = 0;
    for (int previous = index - 1; previous >= 0 && value.charAt(previous) == '\\'; previous--) {
      count++;
    }
    return count;
  }

  private static int skipWhitespace(String value, int startIndex) {
    int index = startIndex;
    while (index < value.length() && Character.isWhitespace(value.charAt(index))) {
      index++;
    }
    return index;
  }

  private static int nextLineStart(String value, int startIndex) {
    for (int index = startIndex; index < value.length(); index++) {
      char current = value.charAt(index);
      if (current == '\r' || current == '\n') {
        return index + 1;
      }
    }
    return value.length();
  }

  private static boolean isQuote(char value) {
    return value == '"' || value == '\'' || value == '`';
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

  private record QuoteBoundary(int start, int end, char quote, boolean escaped) {
  }

  private record AuthorizationWrapper(int contentStart, char closing, boolean escaped) {
    int openingLength() {
      return escaped ? 2 : 1;
    }

    int closingLength() {
      return escaped ? 2 : 1;
    }
  }

  private record AuthorizationCredential(String opening, String closing, int end) {
  }
}
