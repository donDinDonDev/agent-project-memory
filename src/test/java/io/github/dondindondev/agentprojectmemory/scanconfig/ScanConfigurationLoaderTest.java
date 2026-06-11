package io.github.dondindondev.agentprojectmemory.scanconfig;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import io.github.dondindondev.agentprojectmemory.analyzer.ScanPathContainment;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class ScanConfigurationLoaderTest {
  @TempDir
  private Path tempDir;

  private final ScanConfigurationLoader loader = new ScanConfigurationLoader();

  @Test
  void returnsSafeDefaultsWhenRootConfigIsAbsent() throws Exception {
    Path repositoryRoot = repository("defaults");

    ScanConfiguration configuration = loader.load(
        repositoryRoot,
        ScanPathContainment.canonicalRoot(repositoryRoot),
        null);

    assertAll(
        () -> assertEquals("defaults_only", configuration.configSource()),
        () -> assertEquals("not_detected", configuration.configFileStatus()),
        () -> assertTrue(configuration.localMarkdownEnabled()),
        () -> assertEquals("default", configuration.localMarkdownSource()),
        () -> assertEquals(List.of(), configuration.documentIncludes()),
        () -> assertEquals(List.of(), configuration.documentExcludes()));
  }

  @Test
  void parsesApprovedRootLocalYamlSchema() throws Exception {
    Path repositoryRoot = repository("valid");
    writeConfig(repositoryRoot, """
        version: 1
        features:
          local_markdown: false
          generated_sources: false
          follow_symlinks: false
        documents:
          include:
            - notes/**/*.md
          exclude:
            - docs/archive/**
        """);

    ScanConfiguration configuration = loader.load(
        repositoryRoot,
        ScanPathContainment.canonicalRoot(repositoryRoot),
        null);

    assertAll(
        () -> assertEquals("config_file", configuration.configSource()),
        () -> assertEquals("agent-project-memory.yml", configuration.configFilePath()),
        () -> assertEquals("applied", configuration.configFileStatus()),
        () -> assertFalse(configuration.localMarkdownEnabled()),
        () -> assertEquals("config_file", configuration.localMarkdownSource()),
        () -> assertTrue(configuration.documentIncludes().get(0).matches("notes/a/b/guide.md")),
        () -> assertTrue(configuration.documentExcludes().get(0).matches("docs/archive/old.md")));
  }

  @Test
  void loadsExplicitRepositoryRelativeYamlInsteadOfDefaultDiscovery() throws Exception {
    Path repositoryRoot = repository("explicit");
    writeConfig(repositoryRoot, """
        version: 1
        features:
          local_markdown: false
        """);
    Path configDirectory = repositoryRoot.resolve("config");
    Files.createDirectories(configDirectory);
    Files.writeString(configDirectory.resolve("scan.yaml"), """
        version: 1
        features:
          local_markdown: true
        """);

    ScanConfiguration configuration = loader.load(
        repositoryRoot,
        ScanPathContainment.canonicalRoot(repositoryRoot),
        "config/scan.yaml");

    assertAll(
        () -> assertEquals("config_file", configuration.configSource()),
        () -> assertEquals("config/scan.yaml", configuration.configFilePath()),
        () -> assertEquals("explicit", configuration.configFileStatus()),
        () -> assertTrue(configuration.localMarkdownEnabled()),
        () -> assertEquals("config_file", configuration.localMarkdownSource()));
  }

  @Test
  void rejectsUnsafeConfigPathsAndPathRules() throws Exception {
    Path repositoryRoot = repository("unsafe");
    Files.createDirectories(repositoryRoot.resolve("config"));
    Files.writeString(repositoryRoot.resolve("config/scan.yml"), "version: 1\n");

    assertAll(
        () -> assertInvalid(repositoryRoot, "../outside.yml", "unsafe segment"),
        () -> assertInvalid(repositoryRoot, "/tmp/scan.yml", "repository-relative"),
        () -> assertInvalid(repositoryRoot, "C:/scan.yml", "repository-relative"),
        () -> assertInvalid(repositoryRoot, ".project-memory/scan.yml", "generated output"),
        () -> assertInvalid(repositoryRoot, "config/scan.txt", "YAML file"));

    Files.writeString(repositoryRoot.resolve("agent-project-memory.yml"), """
        version: 1
        documents:
          include:
            - ../outside.md
        """);

    assertInvalid(repositoryRoot, null, "unsafe path segment");
  }

  @Test
  void rejectsReservedModeEnablementAndUnknownKeysWithoutEchoingValues() throws Exception {
    Path repositoryRoot = repository("reserved");
    writeConfig(repositoryRoot, """
        version: 1
        features:
          generated_sources: true
        """);

    InvalidScanConfigException reserved = assertThrows(
        InvalidScanConfigException.class,
        () -> loader.load(repositoryRoot, ScanPathContainment.canonicalRoot(repositoryRoot), null));

    Files.writeString(repositoryRoot.resolve("agent-project-memory.yml"), """
        version: 1
        api_token: FAKE_SECRET_TOKEN_VALUE
        """);

    InvalidScanConfigException unknown = assertThrows(
        InvalidScanConfigException.class,
        () -> loader.load(repositoryRoot, ScanPathContainment.canonicalRoot(repositoryRoot), null));

    assertAll(
        () -> assertTrue(reserved.getMessage().contains("reserved scan modes cannot be enabled")),
        () -> assertFalse(unknown.getMessage().contains("api_token")),
        () -> assertFalse(unknown.getMessage().contains("FAKE_SECRET_TOKEN_VALUE")));
  }

  @Test
  void rejectsRootConfigSymlink() throws Exception {
    Path repositoryRoot = repository("symlink");
    Path outsideConfig = tempDir.resolve("outside-config.yml");
    Files.writeString(outsideConfig, "version: 1\n");
    try {
      Files.createSymbolicLink(repositoryRoot.resolve("agent-project-memory.yml"), outsideConfig);
    } catch (UnsupportedOperationException | IOException | SecurityException exception) {
      assumeTrue(false, "symbolic links are unavailable: " + exception.getMessage());
    }

    assertInvalid(repositoryRoot, null, "must not be a symbolic link");
  }

  private void assertInvalid(Path repositoryRoot, String explicitConfigPath, String expectedMessage)
      throws Exception {
    InvalidScanConfigException exception = assertThrows(
        InvalidScanConfigException.class,
        () -> loader.load(
            repositoryRoot,
            ScanPathContainment.canonicalRoot(repositoryRoot),
            explicitConfigPath));
    assertTrue(exception.getMessage().contains(expectedMessage));
  }

  private Path repository(String name) throws Exception {
    Path repositoryRoot = tempDir.resolve(name);
    Files.createDirectories(repositoryRoot);
    return repositoryRoot;
  }

  private void writeConfig(Path repositoryRoot, String content) throws Exception {
    Files.writeString(repositoryRoot.resolve("agent-project-memory.yml"), content);
  }
}
