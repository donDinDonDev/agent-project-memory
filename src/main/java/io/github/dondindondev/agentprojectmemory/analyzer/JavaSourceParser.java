package io.github.dondindondev.agentprojectmemory.analyzer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class JavaSourceParser {
  public static final int MAX_JAVA_SOURCE_BYTES = 1024 * 1024;
  public static final int MAX_JAVA_SOURCE_LINES = 20_000;
  public static final int MAX_JAVA_SOURCE_FILES = 4096;
  public static final int MAX_AGGREGATE_JAVA_SOURCE_BYTES = 64 * 1024 * 1024;
  public static final int MAX_AGGREGATE_JAVA_SOURCE_LINES = 500_000;
  public static final int MAX_JAVA_AST_NODES = 100_000;
  public static final String DIAGNOSTIC_CODE_JAVA_SOURCE_FILE_BYTES_CAP_EXCEEDED =
      "java_source_file_bytes_cap_exceeded";
  public static final String DIAGNOSTIC_CODE_JAVA_SOURCE_FILE_LINES_CAP_EXCEEDED =
      "java_source_file_lines_cap_exceeded";
  public static final String DIAGNOSTIC_CODE_JAVA_SOURCE_FILE_READ_SKIPPED =
      "java_source_file_read_skipped";
  public static final String DIAGNOSTIC_CODE_JAVA_SOURCE_FILE_COUNT_CAP_REACHED =
      "java_source_file_count_cap_reached";
  public static final String DIAGNOSTIC_CODE_JAVA_SOURCE_AGGREGATE_BYTES_CAP_REACHED =
      "java_source_aggregate_bytes_cap_reached";
  public static final String DIAGNOSTIC_CODE_JAVA_SOURCE_AGGREGATE_LINES_CAP_REACHED =
      "java_source_aggregate_lines_cap_reached";
  public static final String DIAGNOSTIC_CODE_JAVA_SOURCE_AST_NODE_CAP_EXCEEDED =
      "java_source_ast_node_cap_exceeded";
  public static final String DIAGNOSTIC_CODE_JAVA_SOURCE_PARSE_ERROR =
      "java_source_parse_error";
  private static final String DIAGNOSTIC_SEVERITY_WARNING = "warning";
  private static final String DIAGNOSTIC_CATEGORY_JAVA_SOURCE = "java_source";
  private static final ParserConfiguration PARSER_CONFIGURATION = new ParserConfiguration()
      .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);
  private static final ThreadLocal<JavaParser> PARSER = ThreadLocal.withInitial(
      () -> new JavaParser(PARSER_CONFIGURATION));
  private static final ThreadLocal<ScanContext> ACTIVE_CONTEXT = new ThreadLocal<>();

  private JavaSourceParser() {
  }

  public static ScanContext newScanContext(Path repositoryRoot) {
    return new ScanContext(repositoryRoot);
  }

  public static <T> T withScanContext(ScanContext context, IoSupplier<T> supplier)
      throws IOException {
    Objects.requireNonNull(context, "context");
    Objects.requireNonNull(supplier, "supplier");

    ScanContext previous = ACTIVE_CONTEXT.get();
    ACTIVE_CONTEXT.set(context);
    try {
      return supplier.get();
    } finally {
      if (previous == null) {
        ACTIVE_CONTEXT.remove();
      } else {
        ACTIVE_CONTEXT.set(previous);
      }
    }
  }

  public static boolean hasSkippedSources() {
    ScanContext context = ACTIVE_CONTEXT.get();
    return context != null && context.hasSkippedSources();
  }

  public static List<Path> javaFiles(
      Path canonicalRepositoryRoot,
      Path sourceRoot) throws IOException {
    Objects.requireNonNull(canonicalRepositoryRoot, "canonicalRepositoryRoot");
    Objects.requireNonNull(sourceRoot, "sourceRoot");

    ScanContext context = ACTIVE_CONTEXT.get();
    if (context != null) {
      return context.javaFiles(canonicalRepositoryRoot, sourceRoot);
    }
    return new ScanContext(canonicalRepositoryRoot).javaFiles(canonicalRepositoryRoot, sourceRoot);
  }

  public static CompilationUnit parse(Path javaFile) throws IOException {
    Objects.requireNonNull(javaFile, "javaFile");

    JavaSourceState source = source(javaFile);
    if (source.skipped) {
      return new CompilationUnit();
    }
    if (source.compilationUnit != null) {
      return source.compilationUnit;
    }

    ParseResult<CompilationUnit> result;
    try {
      result = PARSER.get().parse(source.content);
    } catch (RuntimeException | StackOverflowError exception) {
      source.markSkipped(parseErrorDiagnostic(source.sourcePath));
      return source.compilationUnit;
    }
    Optional<CompilationUnit> compilationUnit = result.getResult();
    if (!result.isSuccessful() || compilationUnit.isEmpty()) {
      source.markSkipped(parseErrorDiagnostic(source.sourcePath));
      return source.compilationUnit;
    }
    CompilationUnit parsed = compilationUnit.orElseThrow();
    try {
      ensureAstNodeBudget(parsed);
    } catch (AstNodeLimitExceededException | StackOverflowError exception) {
      source.markSkipped(astNodeCapDiagnostic(source.sourcePath));
      return source.compilationUnit;
    }
    source.compilationUnit = parsed;
    return source.compilationUnit;
  }

  public static List<String> sourceLines(Path javaFile) throws IOException {
    Objects.requireNonNull(javaFile, "javaFile");
    JavaSourceState source = source(javaFile);
    if (source.skipped) {
      return List.of();
    }
    return source.sourceLines;
  }

  private static JavaSourceState source(Path javaFile) throws IOException {
    ScanContext context = ACTIVE_CONTEXT.get();
    if (context != null) {
      return context.source(javaFile);
    }
    Path normalizedJavaFile = javaFile.toAbsolutePath().normalize();
    Path parent = normalizedJavaFile.getParent();
    return readSource(javaFile, new ScanContext(parent == null ? normalizedJavaFile : parent));
  }

  private static JavaSourceState readSource(Path javaFile, ScanContext context)
      throws IOException {
    String sourcePath = context.sourcePath(javaFile);
    if (!context.allowSourceFile(javaFile)) {
      return JavaSourceState.skipped(sourcePath);
    }

    byte[] bytes;
    try {
      bytes = ScanPathContainment.readRegularFileBytesNoFollowStable(
          javaFile,
          MAX_JAVA_SOURCE_BYTES);
    } catch (ScanPathContainment.FileSizeLimitExceededException exception) {
      context.addDiagnostic(fileBytesCapDiagnostic(sourcePath));
      return JavaSourceState.skipped(sourcePath);
    } catch (IOException | SecurityException exception) {
      context.addDiagnostic(fileReadSkippedDiagnostic(sourcePath));
      return JavaSourceState.skipped(sourcePath);
    }

    String content = new String(bytes, StandardCharsets.UTF_8);
    List<String> sourceLines = content.lines().toList();
    if (sourceLines.size() > MAX_JAVA_SOURCE_LINES) {
      context.addDiagnostic(fileLinesCapDiagnostic(sourcePath));
      return JavaSourceState.skipped(sourcePath);
    }
    if (!context.reserveSourceBytes(bytes.length)) {
      return JavaSourceState.skipped(sourcePath);
    }
    if (!context.reserveSourceLines(sourceLines.size())) {
      return JavaSourceState.skipped(sourcePath);
    }
    return JavaSourceState.accepted(sourcePath, content, sourceLines);
  }

  private static ScanDiagnostic fileBytesCapDiagnostic(String sourcePath) {
    return new ScanDiagnostic(
        "scan_diagnostic:java_source:" + DIAGNOSTIC_CODE_JAVA_SOURCE_FILE_BYTES_CAP_EXCEEDED
            + ":" + diagnosticPathKey(sourcePath),
        DIAGNOSTIC_SEVERITY_WARNING,
        DIAGNOSTIC_CODE_JAVA_SOURCE_FILE_BYTES_CAP_EXCEEDED,
        DIAGNOSTIC_CATEGORY_JAVA_SOURCE,
        "Java source file skipped because it exceeds the 1048576 byte analyzer limit.",
        sourcePath,
        MAX_JAVA_SOURCE_BYTES);
  }

  private static ScanDiagnostic fileLinesCapDiagnostic(String sourcePath) {
    return new ScanDiagnostic(
        "scan_diagnostic:java_source:" + DIAGNOSTIC_CODE_JAVA_SOURCE_FILE_LINES_CAP_EXCEEDED
            + ":" + diagnosticPathKey(sourcePath),
        DIAGNOSTIC_SEVERITY_WARNING,
        DIAGNOSTIC_CODE_JAVA_SOURCE_FILE_LINES_CAP_EXCEEDED,
        DIAGNOSTIC_CATEGORY_JAVA_SOURCE,
        "Java source file skipped because it exceeds the 20000 line analyzer limit.",
        sourcePath,
        MAX_JAVA_SOURCE_LINES);
  }

  private static ScanDiagnostic fileReadSkippedDiagnostic(String sourcePath) {
    return new ScanDiagnostic(
        "scan_diagnostic:java_source:" + DIAGNOSTIC_CODE_JAVA_SOURCE_FILE_READ_SKIPPED
            + ":" + diagnosticPathKey(sourcePath),
        DIAGNOSTIC_SEVERITY_WARNING,
        DIAGNOSTIC_CODE_JAVA_SOURCE_FILE_READ_SKIPPED,
        DIAGNOSTIC_CATEGORY_JAVA_SOURCE,
        "Java source file skipped because it could not be read as a stable regular file "
            + "under the no-symlink source policy.",
        sourcePath,
        null);
  }

  private static ScanDiagnostic fileCountCapDiagnostic() {
    return new ScanDiagnostic(
        "scan_diagnostic:java_source:" + DIAGNOSTIC_CODE_JAVA_SOURCE_FILE_COUNT_CAP_REACHED,
        DIAGNOSTIC_SEVERITY_WARNING,
        DIAGNOSTIC_CODE_JAVA_SOURCE_FILE_COUNT_CAP_REACHED,
        DIAGNOSTIC_CATEGORY_JAVA_SOURCE,
        "Additional Java source files skipped because the analyzer reached the 4096 file limit.",
        null,
        MAX_JAVA_SOURCE_FILES);
  }

  private static ScanDiagnostic aggregateBytesCapDiagnostic() {
    return new ScanDiagnostic(
        "scan_diagnostic:java_source:" + DIAGNOSTIC_CODE_JAVA_SOURCE_AGGREGATE_BYTES_CAP_REACHED,
        DIAGNOSTIC_SEVERITY_WARNING,
        DIAGNOSTIC_CODE_JAVA_SOURCE_AGGREGATE_BYTES_CAP_REACHED,
        DIAGNOSTIC_CATEGORY_JAVA_SOURCE,
        "Additional Java source files skipped because the analyzer reached the 67108864 aggregate byte limit.",
        null,
        MAX_AGGREGATE_JAVA_SOURCE_BYTES);
  }

  private static ScanDiagnostic aggregateLinesCapDiagnostic() {
    return new ScanDiagnostic(
        "scan_diagnostic:java_source:" + DIAGNOSTIC_CODE_JAVA_SOURCE_AGGREGATE_LINES_CAP_REACHED,
        DIAGNOSTIC_SEVERITY_WARNING,
        DIAGNOSTIC_CODE_JAVA_SOURCE_AGGREGATE_LINES_CAP_REACHED,
        DIAGNOSTIC_CATEGORY_JAVA_SOURCE,
        "Additional Java source files skipped because the analyzer reached the 500000 aggregate line limit.",
        null,
        MAX_AGGREGATE_JAVA_SOURCE_LINES);
  }

  private static ScanDiagnostic astNodeCapDiagnostic(String sourcePath) {
    return new ScanDiagnostic(
        "scan_diagnostic:java_source:" + DIAGNOSTIC_CODE_JAVA_SOURCE_AST_NODE_CAP_EXCEEDED
            + ":" + diagnosticPathKey(sourcePath),
        DIAGNOSTIC_SEVERITY_WARNING,
        DIAGNOSTIC_CODE_JAVA_SOURCE_AST_NODE_CAP_EXCEEDED,
        DIAGNOSTIC_CATEGORY_JAVA_SOURCE,
        "Java source file skipped because its parsed AST exceeds the 100000 node analyzer limit.",
        sourcePath,
        MAX_JAVA_AST_NODES);
  }

  private static ScanDiagnostic parseErrorDiagnostic(String sourcePath) {
    return new ScanDiagnostic(
        "scan_diagnostic:java_source:" + DIAGNOSTIC_CODE_JAVA_SOURCE_PARSE_ERROR
            + ":" + diagnosticPathKey(sourcePath),
        DIAGNOSTIC_SEVERITY_WARNING,
        DIAGNOSTIC_CODE_JAVA_SOURCE_PARSE_ERROR,
        DIAGNOSTIC_CATEGORY_JAVA_SOURCE,
        "Java source file skipped because JavaParser could not parse the bounded source content.",
        sourcePath,
        null);
  }

  private static void ensureAstNodeBudget(CompilationUnit compilationUnit) {
    int[] count = new int[] {0};
    compilationUnit.walk(Node.class, ignored -> {
      count[0]++;
      if (count[0] > MAX_JAVA_AST_NODES) {
        throw new AstNodeLimitExceededException();
      }
    });
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

  @FunctionalInterface
  public interface IoSupplier<T> {
    T get() throws IOException;
  }

  public static final class ScanContext {
    private final Path repositoryRoot;
    private final Map<String, List<Path>> javaFilesBySourceRoot = new LinkedHashMap<>();
    private final Map<String, JavaSourceState> sourcesByPath = new LinkedHashMap<>();
    private final Map<String, ScanDiagnostic> diagnostics = new LinkedHashMap<>();
    private final Set<String> seenSourceFileKeys = new LinkedHashSet<>();
    private int aggregateBytes;
    private int aggregateLines;
    private boolean skippedSources;

    private ScanContext(Path repositoryRoot) {
      this.repositoryRoot = Objects.requireNonNull(repositoryRoot, "repositoryRoot")
          .toAbsolutePath()
          .normalize();
    }

    public List<ScanDiagnostic> diagnostics() {
      return List.copyOf(diagnostics.values());
    }

    private boolean hasSkippedSources() {
      return skippedSources;
    }

    private List<Path> javaFiles(Path canonicalRepositoryRoot, Path sourceRoot)
        throws IOException {
      String sourceRootKey = sourceRoot.toAbsolutePath().normalize().toString();
      List<Path> cached = javaFilesBySourceRoot.get(sourceRootKey);
      if (cached != null) {
        return cached;
      }

      List<JavaFileCandidate> javaFileCandidates = new ArrayList<>();
      Files.walkFileTree(sourceRoot, new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
          if (!file.getFileName().toString().endsWith(".java")) {
            return FileVisitResult.CONTINUE;
          }
          if (!isSourceCandidatePathUnderRootNoFollow(canonicalRepositoryRoot, file)) {
            return FileVisitResult.CONTINUE;
          }
          Path normalizedFile = file.toAbsolutePath().normalize();
          javaFileCandidates.add(new JavaFileCandidate(
              normalizedFile,
              Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)));
          return FileVisitResult.CONTINUE;
        }
      });
      List<JavaFileCandidate> sortedJavaFileCandidates = javaFileCandidates.stream()
          .sorted(Comparator.comparing(candidate -> candidate.path().toString()))
          .toList();
      List<Path> cappedJavaFiles = new ArrayList<>();
      for (JavaFileCandidate candidate : sortedJavaFileCandidates) {
        if (!allowSourceFile(candidate.path())) {
          break;
        }
        if (!candidate.regularFile()) {
          addDiagnostic(fileReadSkippedDiagnostic(sourcePath(candidate.path())));
          continue;
        }
        cappedJavaFiles.add(candidate.path());
      }
      List<Path> selectedJavaFiles = List.copyOf(cappedJavaFiles);
      javaFilesBySourceRoot.put(sourceRootKey, selectedJavaFiles);
      return selectedJavaFiles;
    }

    private JavaSourceState source(Path javaFile) throws IOException {
      String sourceKey = javaFile.toAbsolutePath().normalize().toString();
      JavaSourceState cached = sourcesByPath.get(sourceKey);
      if (cached != null) {
        return cached;
      }
      JavaSourceState source = readSource(javaFile, this);
      sourcesByPath.put(sourceKey, source);
      return source;
    }

    private boolean allowSourceFile(Path javaFile) {
      String sourceKey = javaFile.toAbsolutePath().normalize().toString();
      if (seenSourceFileKeys.contains(sourceKey)) {
        return true;
      }
      if (seenSourceFileKeys.size() >= MAX_JAVA_SOURCE_FILES) {
        skippedSources = true;
        addDiagnostic(fileCountCapDiagnostic());
        return false;
      }
      seenSourceFileKeys.add(sourceKey);
      return true;
    }

    private boolean reserveSourceBytes(int byteCount) {
      if ((long) aggregateBytes + byteCount > MAX_AGGREGATE_JAVA_SOURCE_BYTES) {
        skippedSources = true;
        addDiagnostic(aggregateBytesCapDiagnostic());
        return false;
      }
      aggregateBytes += byteCount;
      return true;
    }

    private boolean reserveSourceLines(int lineCount) {
      if ((long) aggregateLines + lineCount > MAX_AGGREGATE_JAVA_SOURCE_LINES) {
        skippedSources = true;
        addDiagnostic(aggregateLinesCapDiagnostic());
        return false;
      }
      aggregateLines += lineCount;
      return true;
    }

    private void addDiagnostic(ScanDiagnostic diagnostic) {
      skippedSources = true;
      diagnostics.putIfAbsent(diagnostic.id(), diagnostic);
    }

    private String sourcePath(Path javaFile) {
      Path normalizedJavaFile = javaFile.toAbsolutePath().normalize();
      try {
        return repositoryRoot.relativize(normalizedJavaFile)
            .toString()
            .replace(javaFile.getFileSystem().getSeparator(), "/");
      } catch (IllegalArgumentException exception) {
        Path fileName = normalizedJavaFile.getFileName();
        return fileName == null ? "unknown.java" : fileName.toString();
      }
    }
  }

  private static boolean isSourceCandidatePathUnderRootNoFollow(
      Path canonicalRepositoryRoot,
      Path file) {
    Path normalizedFile = file.toAbsolutePath().normalize();
    Path parent = normalizedFile.getParent();
    if (parent == null) {
      return false;
    }
    try {
      return parent.toRealPath().startsWith(canonicalRepositoryRoot);
    } catch (IOException | SecurityException exception) {
      return false;
    }
  }

  private static final class AstNodeLimitExceededException extends RuntimeException {
  }

  private record JavaFileCandidate(Path path, boolean regularFile) {
  }

  private static final class JavaSourceState {
    private final String sourcePath;
    private final String content;
    private final List<String> sourceLines;
    private boolean skipped;
    private CompilationUnit compilationUnit;

    private JavaSourceState(
        String sourcePath,
        String content,
        List<String> sourceLines,
        boolean skipped) {
      this.sourcePath = sourcePath;
      this.content = content;
      this.sourceLines = List.copyOf(sourceLines);
      this.skipped = skipped;
      this.compilationUnit = skipped ? new CompilationUnit() : null;
    }

    private static JavaSourceState accepted(
        String sourcePath,
        String content,
        List<String> sourceLines) {
      return new JavaSourceState(sourcePath, content, sourceLines, false);
    }

    private static JavaSourceState skipped(String sourcePath) {
      return new JavaSourceState(sourcePath, "", List.of(), true);
    }

    private void markSkipped(ScanDiagnostic diagnostic) {
      skipped = true;
      compilationUnit = new CompilationUnit();
      ScanContext context = ACTIVE_CONTEXT.get();
      if (context != null) {
        context.addDiagnostic(diagnostic);
      }
    }
  }
}
