package io.github.dondindondev.agentprojectmemory.analyzer;

import com.github.javaparser.Range;
import com.github.javaparser.ast.Node;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class EvidenceExcerpts {
  public static final int MAX_EXCERPT_LENGTH = 240;

  private static final String TRUNCATED_SUFFIX = "...";

  private EvidenceExcerpts() {
  }

  public static String bounded(String excerpt) {
    Objects.requireNonNull(excerpt, "excerpt");
    if (excerpt.length() <= MAX_EXCERPT_LENGTH) {
      return excerpt;
    }

    int end = MAX_EXCERPT_LENGTH;
    if (Character.isHighSurrogate(excerpt.charAt(end - 1))) {
      end--;
    }
    return excerpt.substring(0, end) + TRUNCATED_SUFFIX;
  }

  public static String sourceRange(Node node, List<String> sourceLines) {
    Objects.requireNonNull(node, "node");
    Objects.requireNonNull(sourceLines, "sourceLines");

    Optional<Range> range = node.getRange();
    if (range.isEmpty()) {
      return bounded(node.toString());
    }

    int start = range.orElseThrow().begin.line;
    int end = range.orElseThrow().end.line;
    if (start < 1 || end < start || end > sourceLines.size()) {
      return bounded(node.toString());
    }

    return sourceLines(sourceLines, start, end);
  }

  public static String sourceLines(List<String> sourceLines, int lineStart, int lineEnd) {
    Objects.requireNonNull(sourceLines, "sourceLines");

    StringBuilder excerpt = new StringBuilder();
    boolean sawContent = false;
    boolean truncated = false;
    scan:
    for (int index = lineStart - 1; index < lineEnd; index++) {
      String value = index > lineStart - 1
          ? "\n" + sourceLines.get(index)
          : sourceLines.get(index);
      for (int offset = 0; offset < value.length(); offset++) {
        char current = value.charAt(offset);
        if (!sawContent && current <= ' ') {
          continue;
        }

        sawContent = true;
        if (excerpt.length() >= MAX_EXCERPT_LENGTH + 1) {
          truncated = true;
          break scan;
        }
        excerpt.append(current);
      }
    }

    String normalized = truncated ? excerpt.toString() : excerpt.toString().trim();
    return bounded(normalized);
  }

  public static String singleLine(Node node, List<String> sourceLines, Integer preferredLine) {
    Objects.requireNonNull(node, "node");
    Objects.requireNonNull(sourceLines, "sourceLines");

    if (preferredLine != null && preferredLine >= 1 && preferredLine <= sourceLines.size()) {
      return bounded(sourceLines.get(preferredLine - 1).trim());
    }

    Optional<Range> range = node.getRange();
    if (range.isEmpty()) {
      return bounded(node.toString());
    }

    int line = range.orElseThrow().begin.line;
    if (line < 1 || line > sourceLines.size()) {
      return bounded(node.toString());
    }

    return bounded(sourceLines.get(line - 1).trim());
  }
}
