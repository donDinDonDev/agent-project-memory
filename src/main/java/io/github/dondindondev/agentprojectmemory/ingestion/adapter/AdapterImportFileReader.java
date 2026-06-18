package io.github.dondindondev.agentprojectmemory.ingestion.adapter;

import io.github.dondindondev.agentprojectmemory.analyzer.ScanPathContainment;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

final class AdapterImportFileReader {
  private AdapterImportFileReader() {
  }

  static byte[] readImportBytes(Path repositoryRoot, String importPath, int maxBytes)
      throws IOException {
    Path normalizedRoot = repositoryRoot.toAbsolutePath().normalize();
    Path resolvedImportPath = normalizedRoot.resolve(importPath).toAbsolutePath().normalize();
    try {
      Path canonicalRoot = ScanPathContainment.canonicalRoot(normalizedRoot);
      if (!resolvedImportPath.startsWith(normalizedRoot)
          || hasSymbolicLinkSegment(normalizedRoot, resolvedImportPath)
          || !ScanPathContainment.isRegularFileUnderRootNoFollow(canonicalRoot, resolvedImportPath)) {
        throw new IOException("Adapter import file could not be read.");
      }
      return ScanPathContainment.readRegularFileBytesNoFollowStable(resolvedImportPath, maxBytes);
    } catch (ScanPathContainment.FileSizeLimitExceededException exception) {
      throw new IOException("Adapter import file exceeds maximum supported size.");
    } catch (IOException | SecurityException exception) {
      throw new IOException("Adapter import file could not be read.");
    }
  }

  private static boolean hasSymbolicLinkSegment(Path repositoryRoot, Path path) {
    if (!path.startsWith(repositoryRoot)) {
      return true;
    }
    Path current = repositoryRoot;
    for (Path part : repositoryRoot.relativize(path)) {
      current = current.resolve(part);
      if (Files.isSymbolicLink(current)) {
        return true;
      }
    }
    return false;
  }
}
