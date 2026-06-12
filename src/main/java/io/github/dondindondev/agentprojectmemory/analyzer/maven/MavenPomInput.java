package io.github.dondindondev.agentprojectmemory.analyzer.maven;

import io.github.dondindondev.agentprojectmemory.analyzer.ScanDiagnostic;
import io.github.dondindondev.agentprojectmemory.analyzer.ScanPathContainment;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public final class MavenPomInput {
  public static final int MAX_POM_BYTES = 1024 * 1024;
  public static final String DIAGNOSTIC_CODE_POM_BYTES_CAP_EXCEEDED =
      "maven_pom_file_bytes_cap_exceeded";
  private static final String DIAGNOSTIC_SEVERITY_WARNING = "warning";
  private static final String DIAGNOSTIC_CATEGORY_MAVEN = "maven";

  private MavenPomInput() {
  }

  public static byte[] readPomBytes(Path pom) throws IOException {
    return ScanPathContainment.readRegularFileBytesNoFollowStable(pom, MAX_POM_BYTES);
  }

  public static List<String> readPomLines(Path pom) throws IOException {
    return ScanPathContainment.readRegularFileLinesNoFollowStable(
        pom,
        StandardCharsets.UTF_8,
        MAX_POM_BYTES);
  }

  public static boolean isPomSizeLimitExceeded(IOException exception) {
    return exception instanceof ScanPathContainment.FileSizeLimitExceededException;
  }

  public static void addPomSizeLimitDiagnostic(
      List<ScanDiagnostic> diagnostics,
      String sourcePath) {
    ScanDiagnostic diagnostic = pomSizeLimitDiagnostic(sourcePath);
    if (diagnostics.stream().noneMatch(existing -> existing.id().equals(diagnostic.id()))) {
      diagnostics.add(diagnostic);
    }
  }

  public static ScanDiagnostic pomSizeLimitDiagnostic(String sourcePath) {
    return new ScanDiagnostic(
        "scan_diagnostic:maven:" + DIAGNOSTIC_CODE_POM_BYTES_CAP_EXCEEDED
            + ":" + diagnosticPathKey(sourcePath),
        DIAGNOSTIC_SEVERITY_WARNING,
        DIAGNOSTIC_CODE_POM_BYTES_CAP_EXCEEDED,
        DIAGNOSTIC_CATEGORY_MAVEN,
        "Maven POM skipped because it exceeds the 1048576 byte analyzer limit.",
        sourcePath,
        MAX_POM_BYTES);
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
