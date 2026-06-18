package io.github.dondindondev.agentprojectmemory.analyzer;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class ScanPathContainmentTest {
  @TempDir
  private Path tempDir;

  @Test
  void trustedRegularFilePolicyRejectsHardlinkedInputs() throws Exception {
    Path source = tempDir.resolve("source.txt");
    Path alias = tempDir.resolve("alias.txt");
    Files.writeString(source, "source content\n");
    createHardLink(alias, source);

    assertAll(
        () -> assertFalse(ScanPathContainment.isTrustedRegularFileNoFollow(source)),
        () -> assertFalse(ScanPathContainment.isTrustedRegularFileNoFollow(alias)),
        () -> assertFalse(ScanPathContainment.isRegularFileUnderRootNoFollow(
            ScanPathContainment.canonicalRoot(tempDir),
            source)),
        () -> assertThrows(
            ScanPathContainment.UnsafeRegularFileException.class,
            () -> ScanPathContainment.readRegularFileBytesNoFollowStable(source, 128)));
  }

  private void createHardLink(Path link, Path existing) throws Exception {
    try {
      Files.createLink(link, existing);
    } catch (UnsupportedOperationException | IOException | SecurityException exception) {
      assumeTrue(false, "hard links are unavailable: " + exception.getMessage());
    }
  }
}
