package io.github.dondindondev.agentprojectmemory.scanconfig;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public final class ScanConfigPathPattern {
  private static final Pattern DRIVE_LETTER = Pattern.compile("^[A-Za-z]:.*");
  private static final Pattern URL_LIKE_SCHEME = Pattern.compile("^[A-Za-z][A-Za-z0-9+.-]*://.*");
  private static final String INVALID_GLOB_CHARS = "?[]{}()|";

  private final String pattern;
  private final List<SegmentMatcher> segments;

  private ScanConfigPathPattern(String pattern) {
    this.pattern = pattern;
    this.segments = List.of(pattern.split("/", -1)).stream()
        .map(SegmentMatcher::new)
        .toList();
  }

  public static ScanConfigPathPattern parse(String pattern, String field, boolean includeRule)
      throws InvalidScanConfigException {
    if (pattern == null || pattern.isBlank()) {
      throw new InvalidScanConfigException("Invalid config: " + field + " must be a non-empty path rule.");
    }
    if (pattern.startsWith("/")
        || pattern.startsWith("./")
        || pattern.contains("\\")
        || DRIVE_LETTER.matcher(pattern).matches()
        || URL_LIKE_SCHEME.matcher(pattern).matches()) {
      throw new InvalidScanConfigException("Invalid config: " + field + " must be repository-relative.");
    }
    for (int index = 0; index < INVALID_GLOB_CHARS.length(); index++) {
      if (pattern.indexOf(INVALID_GLOB_CHARS.charAt(index)) >= 0) {
        throw new InvalidScanConfigException("Invalid config: " + field + " uses unsupported pattern syntax.");
      }
    }

    List<String> segments = List.of(pattern.split("/", -1));
    for (String segment : segments) {
      if (segment.isEmpty() || ".".equals(segment) || "..".equals(segment)) {
        throw new InvalidScanConfigException("Invalid config: " + field + " contains an unsafe path segment.");
      }
      if (segment.contains("**") && !"**".equals(segment)) {
        throw new InvalidScanConfigException("Invalid config: " + field + " uses unsupported recursive syntax.");
      }
    }
    if (includeRule && !(pattern.endsWith(".md") || pattern.endsWith(".markdown"))) {
      throw new InvalidScanConfigException(
          "Invalid config: " + field + " include rules must target Markdown files.");
    }
    return new ScanConfigPathPattern(pattern);
  }

  public boolean matches(String repositoryRelativePath) {
    Objects.requireNonNull(repositoryRelativePath, "repositoryRelativePath");
    if (repositoryRelativePath.isBlank()
        || repositoryRelativePath.startsWith("/")
        || repositoryRelativePath.contains("\\")) {
      return false;
    }
    String[] pathSegments = repositoryRelativePath.split("/", -1);
    boolean[] nextMatches = new boolean[pathSegments.length + 1];
    nextMatches[pathSegments.length] = true;
    for (int patternIndex = segments.size() - 1; patternIndex >= 0; patternIndex--) {
      SegmentMatcher segment = segments.get(patternIndex);
      boolean[] currentMatches = new boolean[pathSegments.length + 1];
      if (segment.recursive()) {
        currentMatches[pathSegments.length] = nextMatches[pathSegments.length];
        for (int pathIndex = pathSegments.length - 1; pathIndex >= 0; pathIndex--) {
          currentMatches[pathIndex] = nextMatches[pathIndex] || currentMatches[pathIndex + 1];
        }
      } else {
        for (int pathIndex = pathSegments.length - 1; pathIndex >= 0; pathIndex--) {
          currentMatches[pathIndex] = segment.matches(pathSegments[pathIndex]) && nextMatches[pathIndex + 1];
        }
      }
      nextMatches = currentMatches;
    }
    return nextMatches[0];
  }

  String pattern() {
    return pattern;
  }

  private static Pattern segmentPattern(String patternSegment) {
    StringBuilder regex = new StringBuilder("^");
    for (int index = 0; index < patternSegment.length(); index++) {
      char character = patternSegment.charAt(index);
      if (character == '*') {
        regex.append("[^/]*");
      } else {
        regex.append(Pattern.quote(String.valueOf(character)));
      }
    }
    regex.append('$');
    return Pattern.compile(regex.toString());
  }

  private static final class SegmentMatcher {
    private final String literal;
    private final Pattern pattern;
    private final boolean recursive;

    private SegmentMatcher(String segment) {
      this.recursive = "**".equals(segment);
      this.literal = !recursive && !segment.contains("*") ? segment : null;
      this.pattern = !recursive && literal == null ? segmentPattern(segment) : null;
    }

    private boolean recursive() {
      return recursive;
    }

    private boolean matches(String pathSegment) {
      if (recursive) {
        return true;
      }
      if (literal != null) {
        return literal.equals(pathSegment);
      }
      return pattern.matcher(pathSegment).matches();
    }
  }
}
