package io.github.dondindondev.agentprojectmemory.query;

import io.github.dondindondev.agentprojectmemory.OutputRedactor;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

final class QueryDisplaySafety {
  private static final Set<String> SENSITIVE_PATH_SEGMENTS = Set.of(
      ".aws",
      ".azure",
      ".docker",
      ".env",
      ".gcp",
      ".gnupg",
      ".kube",
      ".netrc",
      ".npmrc",
      ".pypirc",
      ".ssh",
      "authorized_keys",
      "id_dsa",
      "id_ecdsa",
      "id_ed25519",
      "id_rsa",
      "known_hosts",
      "passwd");

  private QueryDisplaySafety() {
  }

  static String sanitize(String value) {
    if (value == null || value.isEmpty()) {
      return value;
    }
    String redacted = OutputRedactor.redact(value);
    return containsUnsafePathOrIdShape(redacted) ? OutputRedactor.REDACTION_MARKER : redacted;
  }

  private static boolean containsUnsafePathOrIdShape(String value) {
    return containsUnsafePathOrIdShapeCandidate(value)
        || containsUnsafePathOrIdShapeCandidate(percentDecode(value));
  }

  private static boolean containsUnsafePathOrIdShapeCandidate(String value) {
    if (value == null || value.isEmpty()) {
      return false;
    }
    String normalized = value.replace('\\', '/');
    String lower = normalized.toLowerCase(Locale.ROOT);
    return containsUnsafeLocalPathPrefix(normalized, lower)
        || containsUnsafePathTraversal(lower)
        || containsUrlLikePath(lower)
        || containsGeneratedArtifactPath(lower)
        || containsSensitivePathSegment(normalized);
  }

  private static boolean containsUnsafeLocalPathPrefix(String value, String lower) {
    return containsDelimited(value, "/Users/")
        || containsDelimited(lower, "/home/")
        || containsDelimited(lower, "/root/")
        || containsDelimited(lower, "/private/")
        || containsDelimited(lower, "/var/folders/")
        || containsDelimited(lower, "/tmp/")
        || containsDelimited(lower, "/etc/")
        || containsDelimited(lower, "/proc/")
        || containsDelimited(lower, "/sys/")
        || containsDelimited(lower, "/dev/")
        || containsDelimited(lower, "/volumes/")
        || lower.matches(".*(^|[\\s:=,;()\\[\\]{}\"'`])[a-z]:/.*")
        || lower.startsWith("~/")
        || lower.contains("=~/")
        || lower.contains(":~/");
  }

  private static boolean containsDelimited(String value, String token) {
    int index = value.indexOf(token);
    while (index >= 0) {
      if (index == 0 || isBoundary(value.charAt(index - 1))) {
        return true;
      }
      index = value.indexOf(token, index + 1);
    }
    return false;
  }

  private static boolean isBoundary(char value) {
    return Character.isWhitespace(value)
        || value == ':'
        || value == '='
        || value == ','
        || value == ';'
        || value == '('
        || value == ')'
        || value == '['
        || value == ']'
        || value == '{'
        || value == '}'
        || value == '"'
        || value == '\''
        || value == '`';
  }

  private static boolean containsUnsafePathTraversal(String lower) {
    return lower.startsWith("../")
        || lower.contains("/../")
        || lower.endsWith("/..")
        || lower.startsWith("./")
        || lower.contains("/./")
        || lower.endsWith("/.");
  }

  private static boolean containsUrlLikePath(String lower) {
    return lower.matches(".*(^|[\\s:=,;()\\[\\]{}\"'`])[a-z][a-z0-9+.-]*://.*")
        || lower.matches(".*(^|[\\s:=,;()\\[\\]{}\"'`])file:.*");
  }

  private static boolean containsGeneratedArtifactPath(String lower) {
    return ".project-memory".equals(lower)
        || lower.startsWith(".project-memory/")
        || lower.contains("/.project-memory/")
        || lower.contains(":.project-memory/")
        || lower.contains("=.project-memory/");
  }

  private static boolean containsSensitivePathSegment(String value) {
    for (String segment : value.split("[/:=&#?\\s]+")) {
      if (SENSITIVE_PATH_SEGMENTS.contains(segment.toLowerCase(Locale.ROOT))) {
        return true;
      }
    }
    return false;
  }

  private static String percentDecode(String value) {
    StringBuilder decoded = new StringBuilder(value.length());
    for (int index = 0; index < value.length(); index++) {
      char current = value.charAt(index);
      if (current == '%' && index + 2 < value.length()) {
        int high = Character.digit(value.charAt(index + 1), 16);
        int low = Character.digit(value.charAt(index + 2), 16);
        if (high >= 0 && low >= 0) {
          decoded.append(new String(
              new byte[] {(byte) ((high << 4) + low)},
              StandardCharsets.UTF_8));
          index += 2;
          continue;
        }
      }
      decoded.append(current);
    }
    return decoded.toString();
  }
}
