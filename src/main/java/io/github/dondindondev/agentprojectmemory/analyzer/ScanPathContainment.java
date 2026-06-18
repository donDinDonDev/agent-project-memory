package io.github.dondindondev.agentprojectmemory.analyzer;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.Optional;
import java.util.List;

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

  public static boolean isRegularFileUnderRootNoFollow(Path canonicalRepositoryRoot, Path path) {
    Objects.requireNonNull(canonicalRepositoryRoot, "canonicalRepositoryRoot");
    Objects.requireNonNull(path, "path");
    return isTrustedRegularFileNoFollow(path)
        && isRegularFileUnderRoot(canonicalRepositoryRoot, path);
  }

  public static boolean isTrustedRegularFileNoFollow(Path path) {
    Objects.requireNonNull(path, "path");
    try {
      regularFileSnapshot(path);
      return true;
    } catch (IOException | SecurityException exception) {
      return false;
    }
  }

  public static byte[] readRegularFileBytesNoFollowStable(Path path, int maxBytes)
      throws IOException {
    Objects.requireNonNull(path, "path");
    if (maxBytes < 0) {
      throw new IllegalArgumentException("maxBytes must be non-negative");
    }

    RegularFileSnapshot before = regularFileSnapshot(path);
    int initialCapacity = (int) Math.min(Math.min(before.size(), (long) maxBytes), 8192L);
    ByteArrayOutputStream content = new ByteArrayOutputStream(Math.max(0, initialCapacity));
    byte[] buffer = new byte[8192];
    try (InputStream input = Files.newInputStream(path, LinkOption.NOFOLLOW_LINKS)) {
      int read;
      while ((read = input.read(buffer)) >= 0) {
        long nextSize = (long) content.size() + read;
        if (nextSize > maxBytes) {
          throw new FileSizeLimitExceededException(path, maxBytes);
        }
        content.write(buffer, 0, read);
      }
    }

    RegularFileSnapshot after = regularFileSnapshot(path);
    if (!before.matches(after)) {
      throw new IOException("File changed while reading: " + path);
    }
    return content.toByteArray();
  }

  public static byte[] readRegularFilePrefixNoFollowStable(Path path, int maxBytes)
      throws IOException {
    Objects.requireNonNull(path, "path");
    if (maxBytes < 0) {
      throw new IllegalArgumentException("maxBytes must be non-negative");
    }

    RegularFileSnapshot before = regularFileSnapshot(path);
    ByteArrayOutputStream content = new ByteArrayOutputStream(Math.min(maxBytes, 8192));
    byte[] buffer = new byte[8192];
    try (InputStream input = Files.newInputStream(path, LinkOption.NOFOLLOW_LINKS)) {
      while (content.size() < maxBytes) {
        int maxRead = Math.min(buffer.length, maxBytes - content.size());
        int read = input.read(buffer, 0, maxRead);
        if (read < 0) {
          break;
        }
        content.write(buffer, 0, read);
      }
    }

    RegularFileSnapshot after = regularFileSnapshot(path);
    if (!before.matches(after)) {
      throw new IOException("File changed while reading: " + path);
    }
    return content.toByteArray();
  }

  public static List<String> readRegularFileLinesNoFollowStable(
      Path path,
      Charset charset,
      int maxBytes) throws IOException {
    String content = new String(readRegularFileBytesNoFollowStable(path, maxBytes), charset);
    return content.lines().toList();
  }

  public static String readRegularFileStringNoFollowStable(
      Path path,
      Charset charset,
      int maxBytes) throws IOException {
    Objects.requireNonNull(charset, "charset");
    return charset.newDecoder()
        .decode(ByteBuffer.wrap(readRegularFileBytesNoFollowStable(path, maxBytes)))
        .toString();
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

  private static RegularFileSnapshot regularFileSnapshot(Path path) throws IOException {
    BasicFileAttributes attributes = Files.readAttributes(
        path,
        BasicFileAttributes.class,
        LinkOption.NOFOLLOW_LINKS);
    if (!attributes.isRegularFile()) {
      throw new NoSuchFileException(path.toString());
    }
    return new RegularFileSnapshot(
        attributes.fileKey(),
        attributes.size(),
        attributes.lastModifiedTime().toMillis(),
        trustedSingleLinkCount(path));
  }

  private static long trustedSingleLinkCount(Path path) throws IOException {
    Object value;
    try {
      value = Files.getAttribute(path, "unix:nlink", LinkOption.NOFOLLOW_LINKS);
    } catch (UnsupportedOperationException | IllegalArgumentException exception) {
      throw new UnsafeRegularFileException("Regular file link count is not available.");
    } catch (SecurityException exception) {
      throw new UnsafeRegularFileException("Regular file link count could not be verified.");
    }
    if (!(value instanceof Number number)) {
      throw new UnsafeRegularFileException("Regular file link count is not available.");
    }
    long linkCount = number.longValue();
    if (linkCount != 1L) {
      throw new UnsafeRegularFileException("Regular file must not have multiple hard links.");
    }
    return linkCount;
  }

  public static final class FileSizeLimitExceededException extends IOException {
    public FileSizeLimitExceededException(Path path, int maxBytes) {
      super(path + " exceeds " + maxBytes + " bytes");
    }
  }

  public static final class UnsafeRegularFileException extends IOException {
    public UnsafeRegularFileException(String message) {
      super(message);
    }
  }

  private record RegularFileSnapshot(
      Object fileKey,
      long size,
      long lastModifiedMillis,
      long linkCount) {
    private boolean matches(RegularFileSnapshot other) {
      if (fileKey != null && other.fileKey != null && !Objects.equals(fileKey, other.fileKey)) {
        return false;
      }
      return size == other.size
          && lastModifiedMillis == other.lastModifiedMillis
          && linkCount == other.linkCount;
    }
  }
}
