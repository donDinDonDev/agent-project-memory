package io.github.dondindondev.agentprojectmemory.analyzer;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class JavaSourceParserTest {
  @TempDir
  private Path tempDir;

  @Test
  void parseSkipsOversizedSourceBeforeJavaParserAndReportsDiagnostic() throws Exception {
    Path javaFile = tempDir.resolve("src/main/java/com/example/HugeController.java");
    writeFile(javaFile, "x".repeat(JavaSourceParser.MAX_JAVA_SOURCE_BYTES + 1));
    JavaSourceParser.ScanContext context = JavaSourceParser.newScanContext(tempDir);

    CompilationUnit compilationUnit = JavaSourceParser.withScanContext(
        context,
        () -> JavaSourceParser.parse(javaFile));
    List<String> sourceLines = JavaSourceParser.withScanContext(
        context,
        () -> JavaSourceParser.sourceLines(javaFile));

    assertAll(
        () -> assertTrue(compilationUnit.findAll(ClassOrInterfaceDeclaration.class).isEmpty()),
        () -> assertTrue(sourceLines.isEmpty()),
        () -> assertEquals(1, context.diagnostics().size()),
        () -> assertEquals(
            JavaSourceParser.DIAGNOSTIC_CODE_JAVA_SOURCE_FILE_BYTES_CAP_EXCEEDED,
            context.diagnostics().get(0).code()),
        () -> assertEquals("java_source", context.diagnostics().get(0).category()),
        () -> assertEquals("src/main/java/com/example/HugeController.java",
            context.diagnostics().get(0).path()),
        () -> assertEquals(JavaSourceParser.MAX_JAVA_SOURCE_BYTES,
            context.diagnostics().get(0).count()));
  }

  @Test
  void sourceLinesSkipsTooManyLinesAndReportsDiagnostic() throws Exception {
    Path javaFile = tempDir.resolve("src/main/java/com/example/LineBomb.java");
    writeFile(
        javaFile,
        "package com.example;\n"
            + "// line\n".repeat(JavaSourceParser.MAX_JAVA_SOURCE_LINES)
            + "class LineBomb {}\n");
    JavaSourceParser.ScanContext context = JavaSourceParser.newScanContext(tempDir);

    List<String> sourceLines = JavaSourceParser.withScanContext(
        context,
        () -> JavaSourceParser.sourceLines(javaFile));

    assertAll(
        () -> assertTrue(sourceLines.isEmpty()),
        () -> assertEquals(1, context.diagnostics().size()),
        () -> assertEquals(
            JavaSourceParser.DIAGNOSTIC_CODE_JAVA_SOURCE_FILE_LINES_CAP_EXCEEDED,
            context.diagnostics().get(0).code()),
        () -> assertEquals(JavaSourceParser.MAX_JAVA_SOURCE_LINES,
            context.diagnostics().get(0).count()));
  }

  @Test
  void parseSkipsMalformedSourceAndReportsDiagnostic() throws Exception {
    Path javaFile = tempDir.resolve("src/main/java/com/example/BrokenController.java");
    writeFile(javaFile, """
        package com.example;

        public class BrokenController {
          void broken( {
          }
        }
        """);
    JavaSourceParser.ScanContext context = JavaSourceParser.newScanContext(tempDir);

    CompilationUnit compilationUnit = JavaSourceParser.withScanContext(
        context,
        () -> JavaSourceParser.parse(javaFile));

    assertAll(
        () -> assertTrue(compilationUnit.findAll(ClassOrInterfaceDeclaration.class).isEmpty()),
        () -> assertEquals(1, context.diagnostics().size()),
        () -> assertEquals(
            JavaSourceParser.DIAGNOSTIC_CODE_JAVA_SOURCE_PARSE_ERROR,
            context.diagnostics().get(0).code()),
        () -> assertEquals("src/main/java/com/example/BrokenController.java",
            context.diagnostics().get(0).path()));
  }

  @Test
  void parseSkipsAstNodeBombAndReportsDiagnostic() throws Exception {
    Path javaFile = tempDir.resolve("src/main/java/com/example/AstBomb.java");
    StringBuilder source = new StringBuilder("package com.example;\nclass AstBomb {\n");
    for (int index = 0; index < JavaSourceParser.MAX_JAVA_SOURCE_LINES - 20; index++) {
      source.append("void m")
          .append(index)
          .append("(){ int value")
          .append(index)
          .append(" = ")
          .append(index)
          .append("; }\n");
    }
    source.append("}\n");
    writeFile(javaFile, source.toString());
    JavaSourceParser.ScanContext context = JavaSourceParser.newScanContext(tempDir);

    CompilationUnit compilationUnit = JavaSourceParser.withScanContext(
        context,
        () -> JavaSourceParser.parse(javaFile));

    assertAll(
        () -> assertTrue(compilationUnit.findAll(ClassOrInterfaceDeclaration.class).isEmpty()),
        () -> assertEquals(1, context.diagnostics().size()),
        () -> assertEquals(
            JavaSourceParser.DIAGNOSTIC_CODE_JAVA_SOURCE_AST_NODE_CAP_EXCEEDED,
            context.diagnostics().get(0).code()),
        () -> assertEquals(JavaSourceParser.MAX_JAVA_AST_NODES,
            context.diagnostics().get(0).count()));
  }

  @Test
  void sourceLinesSkipsWhenAggregateLineBudgetIsReached() throws Exception {
    JavaSourceParser.ScanContext context = JavaSourceParser.newScanContext(tempDir);
    String acceptedSource = "// line\n".repeat(JavaSourceParser.MAX_JAVA_SOURCE_LINES - 1)
        + "// line";
    for (int index = 0; index < 25; index++) {
      Path javaFile = tempDir.resolve("src/main/java/com/example/Aggregate" + index + ".java");
      writeFile(javaFile, acceptedSource);
      JavaSourceParser.withScanContext(context, () -> JavaSourceParser.sourceLines(javaFile));
    }
    Path overflow = tempDir.resolve("src/main/java/com/example/AggregateOverflow.java");
    writeFile(overflow, "// overflow");

    List<String> overflowLines = JavaSourceParser.withScanContext(
        context,
        () -> JavaSourceParser.sourceLines(overflow));

    assertAll(
        () -> assertTrue(overflowLines.isEmpty()),
        () -> assertEquals(1, context.diagnostics().size()),
        () -> assertEquals(
            JavaSourceParser.DIAGNOSTIC_CODE_JAVA_SOURCE_AGGREGATE_LINES_CAP_REACHED,
            context.diagnostics().get(0).code()),
        () -> assertEquals(JavaSourceParser.MAX_AGGREGATE_JAVA_SOURCE_LINES,
            context.diagnostics().get(0).count()));
  }

  @Test
  void sourceLinesSkipsWhenAggregateByteBudgetIsReached() throws Exception {
    JavaSourceParser.ScanContext context = JavaSourceParser.newScanContext(tempDir);
    String acceptedSource = "a".repeat(JavaSourceParser.MAX_JAVA_SOURCE_BYTES);
    int acceptedFileCount = JavaSourceParser.MAX_AGGREGATE_JAVA_SOURCE_BYTES
        / JavaSourceParser.MAX_JAVA_SOURCE_BYTES;
    for (int index = 0; index < acceptedFileCount; index++) {
      Path javaFile = tempDir.resolve(
          "src/main/java/com/example/AggregateBytes" + index + ".java");
      writeFile(javaFile, acceptedSource);
      JavaSourceParser.withScanContext(context, () -> JavaSourceParser.sourceLines(javaFile));
    }
    Path overflow = tempDir.resolve("src/main/java/com/example/AggregateBytesOverflow.java");
    writeFile(overflow, "b");

    List<String> overflowLines = JavaSourceParser.withScanContext(
        context,
        () -> JavaSourceParser.sourceLines(overflow));

    assertAll(
        () -> assertTrue(overflowLines.isEmpty()),
        () -> assertEquals(1, context.diagnostics().size()),
        () -> assertEquals(
            JavaSourceParser.DIAGNOSTIC_CODE_JAVA_SOURCE_AGGREGATE_BYTES_CAP_REACHED,
            context.diagnostics().get(0).code()),
        () -> assertEquals(JavaSourceParser.MAX_AGGREGATE_JAVA_SOURCE_BYTES,
            context.diagnostics().get(0).count()));
  }

  @Test
  void javaFilesCapsSourceFileCandidatesAfterDeterministicSortingAndReportsDiagnostic()
      throws Exception {
    Path sourceRoot = tempDir.resolve("src/main/java");
    for (int index = JavaSourceParser.MAX_JAVA_SOURCE_FILES; index >= 0; index--) {
      String className = "Source%04d".formatted(index);
      writeFile(
          sourceRoot.resolve("com/example/" + className + ".java"),
          "package com.example;\nclass " + className + " {}\n");
    }
    JavaSourceParser.ScanContext context = JavaSourceParser.newScanContext(tempDir);
    Path canonicalRepositoryRoot = ScanPathContainment.canonicalRoot(tempDir);

    List<Path> javaFiles = JavaSourceParser.withScanContext(
        context,
        () -> JavaSourceParser.javaFiles(canonicalRepositoryRoot, sourceRoot));
    List<String> fileNames = javaFiles.stream()
        .map(path -> path.getFileName().toString())
        .toList();

    assertAll(
        () -> assertEquals(JavaSourceParser.MAX_JAVA_SOURCE_FILES, javaFiles.size()),
        () -> assertEquals(fileNames.stream().sorted().toList(), fileNames),
        () -> assertEquals("Source0000.java", fileNames.get(0)),
        () -> assertEquals("Source4095.java", fileNames.get(fileNames.size() - 1)),
        () -> assertFalse(fileNames.contains("Source4096.java")),
        () -> assertEquals(1, context.diagnostics().size()),
        () -> assertEquals(
            JavaSourceParser.DIAGNOSTIC_CODE_JAVA_SOURCE_FILE_COUNT_CAP_REACHED,
            context.diagnostics().get(0).code()),
        () -> assertEquals(JavaSourceParser.MAX_JAVA_SOURCE_FILES,
            context.diagnostics().get(0).count()));
  }

  @Test
  void javaFilesSkipsSymlinkedJavaCandidatesWithBoundedDiagnostic() throws Exception {
    Path sourceRoot = tempDir.resolve("src/main/java");
    Path target = sourceRoot.resolve("com/example/RealSource.java");
    writeFile(target, "package com.example;\nclass RealSource {}\n");
    Path link = sourceRoot.resolve("com/example/LinkedSource.java");
    createSymbolicLink(link, target);
    JavaSourceParser.ScanContext context = JavaSourceParser.newScanContext(tempDir);
    Path canonicalRepositoryRoot = ScanPathContainment.canonicalRoot(tempDir);

    List<Path> javaFiles = JavaSourceParser.withScanContext(
        context,
        () -> JavaSourceParser.javaFiles(canonicalRepositoryRoot, sourceRoot));
    List<String> relativePaths = javaFiles.stream()
        .map(tempDir::relativize)
        .map(Path::toString)
        .map(path -> path.replace(tempDir.getFileSystem().getSeparator(), "/"))
        .toList();

    assertAll(
        () -> assertEquals(List.of("src/main/java/com/example/RealSource.java"), relativePaths),
        () -> assertEquals(1, context.diagnostics().size()),
        () -> assertEquals(
            JavaSourceParser.DIAGNOSTIC_CODE_JAVA_SOURCE_FILE_READ_SKIPPED,
            context.diagnostics().get(0).code()),
        () -> assertEquals("src/main/java/com/example/LinkedSource.java",
            context.diagnostics().get(0).path()),
        () -> assertEquals("java_source", context.diagnostics().get(0).category()),
        () -> assertNull(context.diagnostics().get(0).count()));
  }

  @Test
  void javaFilesReportsOutsideRootJavaSymlinkCandidateWithBoundedDiagnostic()
      throws Exception {
    Path scanRoot = tempDir.resolve("scan-root");
    Path sourceRoot = scanRoot.resolve("src/main/java");
    Path outsideTarget = tempDir.resolve("outside-root/OutsideSource.java");
    writeFile(outsideTarget, "package com.example;\nclass OutsideSource {}\n");
    Path link = sourceRoot.resolve("com/example/OutsideLink.java");
    Files.createDirectories(link.getParent());
    createSymbolicLink(link, outsideTarget);
    JavaSourceParser.ScanContext context = JavaSourceParser.newScanContext(scanRoot);
    Path canonicalRepositoryRoot = ScanPathContainment.canonicalRoot(scanRoot);

    List<Path> javaFiles = JavaSourceParser.withScanContext(
        context,
        () -> JavaSourceParser.javaFiles(canonicalRepositoryRoot, sourceRoot));

    assertAll(
        () -> assertTrue(javaFiles.isEmpty()),
        () -> assertEquals(1, context.diagnostics().size()),
        () -> assertEquals(
            JavaSourceParser.DIAGNOSTIC_CODE_JAVA_SOURCE_FILE_READ_SKIPPED,
            context.diagnostics().get(0).code()),
        () -> assertEquals("src/main/java/com/example/OutsideLink.java",
            context.diagnostics().get(0).path()),
        () -> assertEquals("java_source", context.diagnostics().get(0).category()),
        () -> assertNull(context.diagnostics().get(0).count()));
  }

  @Test
  void javaFilesReportsDanglingJavaSymlinkCandidateWithBoundedDiagnostic()
      throws Exception {
    Path scanRoot = tempDir.resolve("scan-root");
    Path sourceRoot = scanRoot.resolve("src/main/java");
    Path link = sourceRoot.resolve("com/example/DanglingLink.java");
    Files.createDirectories(link.getParent());
    createSymbolicLink(link, tempDir.resolve("missing-root/MissingSource.java"));
    JavaSourceParser.ScanContext context = JavaSourceParser.newScanContext(scanRoot);
    Path canonicalRepositoryRoot = ScanPathContainment.canonicalRoot(scanRoot);

    List<Path> javaFiles = JavaSourceParser.withScanContext(
        context,
        () -> JavaSourceParser.javaFiles(canonicalRepositoryRoot, sourceRoot));

    assertAll(
        () -> assertTrue(javaFiles.isEmpty()),
        () -> assertEquals(1, context.diagnostics().size()),
        () -> assertEquals(
            JavaSourceParser.DIAGNOSTIC_CODE_JAVA_SOURCE_FILE_READ_SKIPPED,
            context.diagnostics().get(0).code()),
        () -> assertEquals("src/main/java/com/example/DanglingLink.java",
            context.diagnostics().get(0).path()),
        () -> assertEquals("java_source", context.diagnostics().get(0).category()),
        () -> assertNull(context.diagnostics().get(0).count()));
  }

  private void writeFile(Path path, String content) throws Exception {
    Files.createDirectories(path.getParent());
    Files.writeString(path, content);
  }

  private void createSymbolicLink(Path link, Path target) throws Exception {
    try {
      Files.createSymbolicLink(link, target);
    } catch (UnsupportedOperationException | IOException | SecurityException exception) {
      assumeTrue(false, "symbolic links are unavailable: " + exception.getMessage());
    }
  }
}
