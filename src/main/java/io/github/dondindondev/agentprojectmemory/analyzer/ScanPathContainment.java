package io.github.dondindondev.agentprojectmemory.analyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

public final class ScanPathContainment {
  private ScanPathContainment() {
  }

  public static Path canonicalRoot(Path repositoryRoot) throws IOException {
    Objects.requireNonNull(repositoryRoot, "repositoryRoot");
    return repositoryRoot.toRealPath();
  }

  public static boolean isDirectoryUnderRoot(Path canonicalRepositoryRoot, Path path) {
    return realPathUnderRoot(canonicalRepositoryRoot, path)
        .filter(Files::isDirectory)
        .isPresent();
  }

  public static boolean isRegularFileUnderRoot(Path canonicalRepositoryRoot, Path path) {
    return realPathUnderRoot(canonicalRepositoryRoot, path)
        .filter(Files::isRegularFile)
        .isPresent();
  }

  public static Optional<Path> realPathUnderRoot(Path canonicalRepositoryRoot, Path path) {
    Objects.requireNonNull(canonicalRepositoryRoot, "canonicalRepositoryRoot");
    Objects.requireNonNull(path, "path");
    try {
      Path realPath = path.toRealPath();
      if (realPath.startsWith(canonicalRepositoryRoot)) {
        return Optional.of(realPath);
      }
      return Optional.empty();
    } catch (IOException | SecurityException exception) {
      return Optional.empty();
    }
  }
}
