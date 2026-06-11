package io.github.dondindondev.agentprojectmemory.scanconfig;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public final class ScanConfigPathPattern {
  private static final Pattern DRIVE_LETTER = Pattern.compile("^[A-Za-z]:.*");
  private static final Pattern URL_LIKE_SCHEME = Pattern.compile("^[A-Za-z][A-Za-z0-9+.-]*://.*");
  private static final String INVALID_GLOB_CHARS = "?[]{}()|";

  private final String pattern;
  private final List<String> segments;

  private ScanConfigPathPattern(String pattern) {
    this.pattern = pattern;
    this.segments = List.of(pattern.split("/", -1));
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
    return matches(0, 0, List.of(repositoryRelativePath.split("/", -1)));
  }

  String pattern() {
    return pattern;
  }

  private boolean matches(int patternIndex, int pathIndex, List<String> pathSegments) {
    if (patternIndex == segments.size()) {
      return pathIndex == pathSegments.size();
    }

    String segment = segments.get(patternIndex);
    if ("**".equals(segment)) {
      for (int nextPathIndex = pathIndex; nextPathIndex <= pathSegments.size(); nextPathIndex++) {
        if (matches(patternIndex + 1, nextPathIndex, pathSegments)) {
          return true;
        }
      }
      return false;
    }

    return pathIndex < pathSegments.size()
        && segmentMatches(segment, pathSegments.get(pathIndex))
        && matches(patternIndex + 1, pathIndex + 1, pathSegments);
  }

  private boolean segmentMatches(String patternSegment, String pathSegment) {
    StringBuilder regex = new StringBuilder();
    for (int index = 0; index < patternSegment.length(); index++) {
      char character = patternSegment.charAt(index);
      if (character == '*') {
        regex.append("[^/]*");
      } else {
        regex.append(Pattern.quote(String.valueOf(character)));
      }
    }
    return pathSegment.matches(regex.toString());
  }
}
