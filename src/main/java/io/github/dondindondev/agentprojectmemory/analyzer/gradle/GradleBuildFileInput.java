package io.github.dondindondev.agentprojectmemory.analyzer.gradle;

import io.github.dondindondev.agentprojectmemory.analyzer.ScanDiagnostic;
import io.github.dondindondev.agentprojectmemory.analyzer.ScanPathContainment;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public final class GradleBuildFileInput {
  public static final int MAX_GRADLE_BUILD_FILE_BYTES = 1024 * 1024;
  public static final String DIAGNOSTIC_CODE_GRADLE_BUILD_FILE_BYTES_CAP_EXCEEDED =
      "gradle_build_file_bytes_cap_exceeded";
  public static final String DIAGNOSTIC_CODE_GRADLE_BUILD_FILE_READ_SKIPPED =
      "gradle_build_file_read_skipped";
  private static final String DIAGNOSTIC_SEVERITY_WARNING = "warning";
  private static final String DIAGNOSTIC_CATEGORY_GRADLE = "gradle";

  private GradleBuildFileInput() {
  }

  public static List<String> readBuildFileLines(Path buildFile) throws IOException {
    return ScanPathContainment.readRegularFileLinesNoFollowStable(
        buildFile,
        StandardCharsets.UTF_8,
        MAX_GRADLE_BUILD_FILE_BYTES);
  }

  public static boolean isBuildFileSizeLimitExceeded(IOException exception) {
    return exception instanceof ScanPathContainment.FileSizeLimitExceededException;
  }

  public static void addBuildFileSizeLimitDiagnostic(
      List<ScanDiagnostic> diagnostics,
      String sourcePath) {
    addDiagnostic(diagnostics, buildFileSizeLimitDiagnostic(sourcePath));
  }

  public static void addBuildFileReadSkippedDiagnostic(
      List<ScanDiagnostic> diagnostics,
      String sourcePath) {
    addDiagnostic(diagnostics, buildFileReadSkippedDiagnostic(sourcePath));
  }

  public static ScanDiagnostic buildFileSizeLimitDiagnostic(String sourcePath) {
    return new ScanDiagnostic(
        "scan_diagnostic:gradle:" + DIAGNOSTIC_CODE_GRADLE_BUILD_FILE_BYTES_CAP_EXCEEDED
            + ":" + diagnosticPathKey(sourcePath),
        DIAGNOSTIC_SEVERITY_WARNING,
        DIAGNOSTIC_CODE_GRADLE_BUILD_FILE_BYTES_CAP_EXCEEDED,
        DIAGNOSTIC_CATEGORY_GRADLE,
        "Gradle build file skipped because it exceeds the 1048576 byte analyzer limit.",
        sourcePath,
        MAX_GRADLE_BUILD_FILE_BYTES);
  }

  public static ScanDiagnostic buildFileReadSkippedDiagnostic(String sourcePath) {
    return new ScanDiagnostic(
        "scan_diagnostic:gradle:" + DIAGNOSTIC_CODE_GRADLE_BUILD_FILE_READ_SKIPPED
            + ":" + diagnosticPathKey(sourcePath),
        DIAGNOSTIC_SEVERITY_WARNING,
        DIAGNOSTIC_CODE_GRADLE_BUILD_FILE_READ_SKIPPED,
        DIAGNOSTIC_CATEGORY_GRADLE,
        "Gradle build file skipped because it is unreadable, symlinked, escaping, or otherwise unsafe.",
        sourcePath,
        null);
  }

  private static void addDiagnostic(
      List<ScanDiagnostic> diagnostics,
      ScanDiagnostic diagnostic) {
    if (diagnostics.stream().noneMatch(existing -> existing.id().equals(diagnostic.id()))) {
      diagnostics.add(diagnostic);
    }
  }

  private static String diagnosticPathKey(String sourcePath) {
    StringBuilder key = new StringBuilder();
    byte[] bytes = sourcePath.getBytes(StandardCharsets.UTF_8);
    for (byte next : bytes) {
      int value = next & 0xff;
      if (isDiagnosticPathKeyCharacter(value)) {
        key.append((char) value);
      } else {
        key.append('%');
        key.append(String.format(Locale.ROOT, "%02X", value));
      }
    }
    return key.toString();
  }

  private static boolean isDiagnosticPathKeyCharacter(int value) {
    return value >= 'a' && value <= 'z'
        || value >= 'A' && value <= 'Z'
        || value >= '0' && value <= '9'
        || value == '.'
        || value == '_'
        || value == '-'
        || value == '~'
        || value == '/';
  }
}
