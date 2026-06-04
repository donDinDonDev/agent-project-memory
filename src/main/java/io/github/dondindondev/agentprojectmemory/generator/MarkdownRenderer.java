package io.github.dondindondev.agentprojectmemory.generator;

import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;

public final class MarkdownRenderer {
  private MarkdownRenderer() {
  }

  public static String text(String value) {
    String normalized = normalizeInline(value);
    StringBuilder escaped = new StringBuilder();
    for (int index = 0; index < normalized.length();) {
      int codePoint = normalized.codePointAt(index);
      if (isInlineMarkdownPunctuation(codePoint)) {
        escaped.append('\\');
      }
      escaped.appendCodePoint(codePoint);
      index += Character.charCount(codePoint);
    }
    return escaped.toString();
  }

  public static String inlineCode(String value) {
    String normalized = normalizeInline(value);
    String delimiter = "`".repeat(longestBacktickRun(normalized) + 1);
    if (normalized.startsWith("`") || normalized.endsWith("`")) {
      return delimiter + " " + normalized + " " + delimiter;
    }
    return delimiter + normalized + delimiter;
  }

  public static String inlineCodeList(List<String> values, String emptyLabel) {
    if (values.isEmpty()) {
      return emptyLabel;
    }

    StringJoiner joiner = new StringJoiner(", ");
    for (String value : values) {
      joiner.add(inlineCode(value));
    }
    return joiner.toString();
  }

  private static String normalizeInline(String value) {
    if (value == null || value.isEmpty()) {
      return "";
    }

    StringBuilder normalized = new StringBuilder();
    for (int index = 0; index < value.length();) {
      int codePoint = value.codePointAt(index);
      appendNormalizedCodePoint(normalized, codePoint);
      index += Character.charCount(codePoint);
    }
    return normalized.toString();
  }

  private static void appendNormalizedCodePoint(StringBuilder normalized, int codePoint) {
    switch (codePoint) {
      case '\n' -> normalized.append("\\n");
      case '\r' -> normalized.append("\\r");
      case '\t' -> normalized.append("\\t");
      default -> {
        if (Character.isISOControl(codePoint) || isBidirectionalControl(codePoint)) {
          normalized.append(String.format(Locale.ROOT, "\\u%04X", codePoint));
        } else {
          normalized.appendCodePoint(codePoint);
        }
      }
    }
  }

  private static boolean isInlineMarkdownPunctuation(int codePoint) {
    return switch (codePoint) {
      case '`', '*', '_', '[', ']', '<', '>', '#', '+', '!', '|' -> true;
      default -> false;
    };
  }

  private static boolean isBidirectionalControl(int codePoint) {
    return (codePoint >= 0x202A && codePoint <= 0x202E)
        || (codePoint >= 0x2066 && codePoint <= 0x2069);
  }

  private static int longestBacktickRun(String value) {
    int longest = 0;
    int current = 0;
    for (int index = 0; index < value.length(); index++) {
      if (value.charAt(index) == '`') {
        current++;
        longest = Math.max(longest, current);
      } else {
        current = 0;
      }
    }
    return longest;
  }
}
