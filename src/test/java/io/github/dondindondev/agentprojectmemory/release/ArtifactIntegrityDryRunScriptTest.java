package io.github.dondindondev.agentprojectmemory.release;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Stream;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class ArtifactIntegrityDryRunScriptTest {
  private static final String GROUP_ID = "io.github.dondindondev";
  private static final String ARTIFACT_ID = "agent-project-memory";
  private static final String MAIN_CLASS = "io.github.dondindondev.agentprojectmemory.Main";

  @TempDir
  private Path tempDir;

  @Test
  void dryRunCreatesFilenameOnlyChecksumAndExactAssetList() throws Exception {
    Path projectRoot = createSyntheticProject("9.9.9", "9.9.9");
    Path assetDir = projectRoot.resolve("target/release-artifact-dry-run");

    ProcessResult result = runDryRun(projectRoot, assetDir);

    List<String> assetNames = listAssetNames(assetDir);
    List<String> checksumLines = Files.readAllLines(assetDir.resolve("SHA256SUMS"));

    assertAll(
        () -> assertEquals(0, result.exitCode()),
        () -> assertTrue(result.stdout().contains("Artifact integrity dry-run passed.")),
        () -> assertTrue(result.stdout().contains("Candidate jar: agent-project-memory-9.9.9.jar")),
        () -> assertTrue(result.stdout().contains("CLI version: agent-project-memory 9.9.9")),
        () -> assertTrue(result.stdout().contains("Manifest Main-Class: " + MAIN_CLASS)),
        () -> assertTrue(result.stdout().contains(
            "Maven coordinates: io.github.dondindondev:agent-project-memory:9.9.9")),
        () -> assertTrue(result.stdout().contains(
            "Release assets: agent-project-memory-9.9.9.jar, SHA256SUMS")),
        () -> assertTrue(result.stderr().isEmpty()),
        () -> assertFalse(result.stdout().contains(projectRoot.toString())),
        () -> assertEquals(List.of("SHA256SUMS", "agent-project-memory-9.9.9.jar"), assetNames),
        () -> assertEquals(1, checksumLines.size()),
        () -> assertTrue(checksumLines.get(0).matches(
            "[0-9a-f]{64}  agent-project-memory-9\\.9\\.9\\.jar")),
        () -> assertFalse(checksumLines.get(0).contains("/")),
        () -> assertFalse(checksumLines.get(0).contains("\\")));
  }

  @Test
  void dryRunRejectsMavenArtifactMetadataMismatchBeforeCreatingAssets() throws Exception {
    Path projectRoot = createSyntheticProject("9.9.9", "0.0.1");
    Path assetDir = projectRoot.resolve("target/release-artifact-dry-run");

    ProcessResult result = runDryRun(projectRoot, assetDir);

    assertAll(
        () -> assertNotEquals(0, result.exitCode()),
        () -> assertTrue(result.stdout().isEmpty()),
        () -> assertTrue(result.stderr().contains("Maven artifact metadata version mismatch")),
        () -> assertFalse(result.stderr().contains(projectRoot.toString())),
        () -> assertFalse(Files.exists(assetDir)));
  }

  @Test
  void dryRunRejectsAssetDirectoryOutsideTarget() throws Exception {
    Path projectRoot = createSyntheticProject("9.9.9", "9.9.9");
    Path outsideTarget = tempDir.resolve("outside-assets");

    ProcessResult result = runDryRun(projectRoot, outsideTarget);

    assertAll(
        () -> assertNotEquals(0, result.exitCode()),
        () -> assertTrue(result.stdout().isEmpty()),
        () -> assertTrue(result.stderr().contains("asset directory must be under target/")),
        () -> assertFalse(Files.exists(outsideTarget)));
  }

  @Test
  void dryRunRejectsSymlinkedTargetBeforeWritingOutsideAssets() throws Exception {
    Path projectRoot = createSyntheticProject("9.9.9", "9.9.9");
    Path target = projectRoot.resolve("target");
    Path escapedTarget = Files.createDirectory(tempDir.resolve("escaped-target"));
    Path escapedAssetDir = Files.createDirectories(
        escapedTarget.resolve("release-artifact-dry-run"));
    Files.writeString(escapedAssetDir.resolve("sentinel.txt"), "keep");
    Files.move(
        target.resolve("agent-project-memory-9.9.9.jar"),
        escapedTarget.resolve("agent-project-memory-9.9.9.jar"));
    Files.delete(target);
    createSymbolicLink(target, escapedTarget);

    ProcessResult result = runDryRun(projectRoot, target.resolve("release-artifact-dry-run"));

    assertAll(
        () -> assertNotEquals(0, result.exitCode()),
        () -> assertTrue(result.stdout().isEmpty()),
        () -> assertTrue(result.stderr().contains("target directory must not be a symlink")),
        () -> assertTrue(Files.exists(escapedAssetDir.resolve("sentinel.txt"))));
  }

  @Test
  void dryRunRejectsSymlinkedAssetParentBeforeWritingOutsideAssets() throws Exception {
    Path projectRoot = createSyntheticProject("9.9.9", "9.9.9");
    Path linkedParent = projectRoot.resolve("target/linked-parent");
    Path escapedParent = Files.createDirectory(tempDir.resolve("escaped-parent"));
    Path escapedAssetDir = Files.createDirectories(
        escapedParent.resolve("release-artifact-dry-run"));
    Files.writeString(escapedAssetDir.resolve("sentinel.txt"), "keep");
    createSymbolicLink(linkedParent, escapedParent);

    ProcessResult result = runDryRun(projectRoot, linkedParent.resolve("release-artifact-dry-run"));

    assertAll(
        () -> assertNotEquals(0, result.exitCode()),
        () -> assertTrue(result.stdout().isEmpty()),
        () -> assertTrue(result.stderr().contains("asset directory must be under target/")),
        () -> assertTrue(Files.exists(escapedAssetDir.resolve("sentinel.txt"))));
  }

  @Test
  void dryRunRejectsNonDryRunAssetDirectoryNameUnderTarget() throws Exception {
    Path projectRoot = createSyntheticProject("9.9.9", "9.9.9");
    Path targetClasses = projectRoot.resolve("target/classes");
    Files.createDirectories(targetClasses);
    Files.writeString(targetClasses.resolve("existing.txt"), "do not delete");

    ProcessResult result = runDryRun(projectRoot, targetClasses);

    assertAll(
        () -> assertNotEquals(0, result.exitCode()),
        () -> assertTrue(result.stdout().isEmpty()),
        () -> assertTrue(result.stderr().contains(
            "asset directory name must be release-artifact-dry-run")),
        () -> assertTrue(Files.exists(targetClasses.resolve("existing.txt"))));
  }

  private Path createSyntheticProject(String projectVersion, String metadataVersion)
      throws Exception {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    assumeTrue(compiler != null, "JDK compiler is required for the synthetic jar test");

    Path projectRoot = Files.createDirectory(tempDir.resolve("project-" + projectVersion + "-"
        + metadataVersion.replace('.', '-')));
    Files.createDirectories(projectRoot.resolve("target"));
    Files.writeString(projectRoot.resolve("pom.xml"), """
        <?xml version="1.0" encoding="UTF-8"?>
        <project xmlns="http://maven.apache.org/POM/4.0.0">
          <modelVersion>4.0.0</modelVersion>
          <groupId>io.github.dondindondev</groupId>
          <artifactId>agent-project-memory</artifactId>
          <version>%s</version>
        </project>
        """.formatted(projectVersion));

    Path sourceRoot = Files.createDirectories(projectRoot.resolve(
        "synthetic-src/io/github/dondindondev/agentprojectmemory"));
    Path sourceFile = sourceRoot.resolve("Main.java");
    Files.writeString(sourceFile, """
        package io.github.dondindondev.agentprojectmemory;

        public final class Main {
          private Main() {
          }

          public static void main(String[] args) {
            if (args.length == 1 && "--version".equals(args[0])) {
              System.out.println("agent-project-memory %s");
              return;
            }
            System.out.println("synthetic agent-project-memory jar");
          }
        }
        """.formatted(projectVersion));

    Path classes = Files.createDirectories(projectRoot.resolve("classes"));
    ByteArrayOutputStream compilerOutput = new ByteArrayOutputStream();
    int compilerExit = compiler.run(
        null,
        compilerOutput,
        compilerOutput,
        "-d",
        classes.toString(),
        sourceFile.toString());
    assertEquals(0, compilerExit, compilerOutput.toString(StandardCharsets.UTF_8));

    Path jarPath = projectRoot.resolve("target/agent-project-memory-" + projectVersion + ".jar");
    createJar(jarPath, classes, metadataVersion);
    return projectRoot;
  }

  private static void createJar(Path jarPath, Path classes, String metadataVersion)
      throws IOException {
    Manifest manifest = new Manifest();
    Attributes attributes = manifest.getMainAttributes();
    attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
    attributes.put(Attributes.Name.MAIN_CLASS, MAIN_CLASS);

    try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(jarPath), manifest)) {
      for (Path classFile : classFiles(classes)) {
        String entryName = classes.relativize(classFile).toString().replace(File.separatorChar, '/');
        addFile(jar, entryName, classFile);
      }
      addBytes(
          jar,
          "META-INF/maven/" + GROUP_ID + "/" + ARTIFACT_ID + "/pom.properties",
          """
          groupId=%s
          artifactId=%s
          version=%s
          """.formatted(GROUP_ID, ARTIFACT_ID, metadataVersion).getBytes(StandardCharsets.UTF_8));
    }
  }

  private static List<Path> classFiles(Path classes) throws IOException {
    try (Stream<Path> paths = Files.walk(classes)) {
      return paths.filter(Files::isRegularFile).sorted().toList();
    }
  }

  private static void addFile(JarOutputStream jar, String entryName, Path source)
      throws IOException {
    jar.putNextEntry(new JarEntry(entryName));
    Files.copy(source, jar);
    jar.closeEntry();
  }

  private static void addBytes(JarOutputStream jar, String entryName, byte[] content)
      throws IOException {
    jar.putNextEntry(new JarEntry(entryName));
    jar.write(content);
    jar.closeEntry();
  }

  private static ProcessResult runDryRun(Path projectRoot, Path assetDir) throws Exception {
    Path script = Path.of("scripts/release-artifact-integrity-dry-run.sh")
        .toAbsolutePath()
        .normalize();
    assumeTrue(Files.isRegularFile(script), "dry-run script must exist");

    Process process = new ProcessBuilder(
        "bash",
        script.toString(),
        "--project-root",
        projectRoot.toString(),
        "--asset-dir",
        assetDir.toString())
        .start();
    String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
    int exitCode = process.waitFor();
    return new ProcessResult(exitCode, stdout, stderr);
  }

  private static List<String> listAssetNames(Path assetDir) throws IOException {
    try (Stream<Path> paths = Files.list(assetDir)) {
      return paths
          .map(path -> path.getFileName().toString())
          .sorted()
          .toList();
    }
  }

  private static void createSymbolicLink(Path link, Path target) throws IOException {
    try {
      Files.createSymbolicLink(link, target);
    } catch (UnsupportedOperationException | IOException exception) {
      assumeTrue(false, "symbolic links are required for this dry-run containment test");
    }
  }

  private record ProcessResult(int exitCode, String stdout, String stderr) {
  }
}
