package io.github.dondindondev.agentprojectmemory.scanconfig;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import io.github.dondindondev.agentprojectmemory.analyzer.ScanPathContainment;
import io.github.dondindondev.agentprojectmemory.ingestion.adapter.AdapterImportMode;
import io.github.dondindondev.agentprojectmemory.ingestion.adapter.AdapterLocalImport;
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
        () -> assertEquals(List.of(), configuration.documentExcludes()),
        () -> assertFalse(configuration.adapterConfiguration().enabled()),
        () -> assertFalse(configuration.adapterConfiguration().networkEnabled()),
        () -> assertEquals(List.of(), configuration.adapterConfiguration().localImports()));
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
  void parsesExplicitOptInAdapterLocalImportWithoutReadingInput() throws Exception {
    Path repositoryRoot = repository("adapter-valid");
    Files.createDirectories(repositoryRoot.resolve("exports"));
    Files.writeString(repositoryRoot.resolve("exports/issues.json"), """
        {"token":"FAKE_ADAPTER_EXPORT_CONTENT"}
        """);
    writeConfig(repositoryRoot, """
        version: 1
        adapters:
          local_structured_import:
            enabled: true
            path: exports/issues.json
        """);

    ScanConfiguration configuration = loader.load(
        repositoryRoot,
        ScanPathContainment.canonicalRoot(repositoryRoot),
        null);
    AdapterLocalImport localImport = configuration.adapterConfiguration().localImports().get(0);

    assertAll(
        () -> assertTrue(configuration.adapterConfiguration().enabled()),
        () -> assertFalse(configuration.adapterConfiguration().networkEnabled()),
        () -> assertEquals(1, configuration.adapterConfiguration().localImports().size()),
        () -> assertEquals(
            AdapterLocalImport.LOCAL_STRUCTURED_IMPORT_ADAPTER,
            localImport.adapterName()),
        () -> assertEquals(AdapterImportMode.LOCAL_EXPORT, localImport.importMode()),
        () -> assertEquals("exports/issues.json", localImport.path()));
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
  void rejectsMalformedAdapterConfigWithoutEchoingCredentialsOrValues() throws Exception {
    Path repositoryRoot = repository("adapter-malformed");

    writeConfig(repositoryRoot, """
        version: 1
        adapters: true
        """);
    assertInvalid(repositoryRoot, null, "adapters must be a mapping");

    writeConfig(repositoryRoot, """
        version: 1
        adapters:
          local_structured_import:
            path: exports/issues.json
        """);
    assertInvalid(repositoryRoot, null, "disabled adapter config must not declare an import path");

    writeConfig(repositoryRoot, """
        version: 1
        adapters:
          local_structured_import:
            enabled: true
        """);
    assertInvalid(repositoryRoot, null, "adapter import path is required");

    writeConfig(repositoryRoot, """
        version: 1
        adapters:
          local_structured_import:
            enabled: true
            path: 123
        """);
    assertInvalid(repositoryRoot, null, "adapter import path must be a string");

    writeConfig(repositoryRoot, """
        version: 1
        adapters:
          local_structured_import:
            enabled: true
            path: exports/issues.json
            network: true
        """);
    InvalidScanConfigException network = assertThrows(
        InvalidScanConfigException.class,
        () -> loader.load(repositoryRoot, ScanPathContainment.canonicalRoot(repositoryRoot), null));

    writeConfig(repositoryRoot, """
        version: 1
        adapters:
          local_structured_import:
            enabled: true
            path: exports/issues.json
            api_token: FAKE_ADAPTER_CONFIG_TOKEN
        """);
    InvalidScanConfigException credential = assertThrows(
        InvalidScanConfigException.class,
        () -> loader.load(repositoryRoot, ScanPathContainment.canonicalRoot(repositoryRoot), null));

    assertAll(
        () -> assertTrue(network.getMessage().contains("unsupported key")),
        () -> assertFalse(network.getMessage().contains("network")),
        () -> assertFalse(network.getMessage().contains("true")),
        () -> assertTrue(credential.getMessage().contains("unsupported key")),
        () -> assertFalse(credential.getMessage().contains("api_token")),
        () -> assertFalse(credential.getMessage().contains("FAKE_ADAPTER_CONFIG_TOKEN")));
  }

  @Test
  void rejectsUnsafeAdapterImportPathsAndFilesystemTargets() throws Exception {
    Path repositoryRoot = repository("adapter-unsafe");
    Files.createDirectories(repositoryRoot.resolve("exports"));
    Files.writeString(repositoryRoot.resolve("exports/issues.json"), "{}\n");
    Files.createDirectories(repositoryRoot.resolve(".project-memory"));
    Files.createDirectories(repositoryRoot.resolve("exports/directory.json"));

    assertInvalidAdapterImportPath(repositoryRoot, "../outside.json", "unsafe path segment");
    assertInvalidAdapterImportPath(repositoryRoot, "/tmp/adapter-export.json", "repository-relative");
    assertInvalidAdapterImportPath(repositoryRoot, "C:/adapter-export.json", "repository-relative");
    assertInvalidAdapterImportPath(repositoryRoot, ".project-memory/source.json", "generated output");
    assertInvalidAdapterImportPath(repositoryRoot, "exports/missing.json", "adapter import file was not found");
    assertInvalidAdapterImportPath(repositoryRoot, "exports/directory.json", "regular file");
  }

  @Test
  void rejectsAdapterImportSymlinkAndSymlinkedPathSegments() throws Exception {
    Path repositoryRoot = repository("adapter-symlink");
    Files.createDirectories(repositoryRoot.resolve("exports"));
    Path outsideDirectory = tempDir.resolve("outside-adapter-export");
    Files.createDirectories(outsideDirectory);
    Path outsideFile = outsideDirectory.resolve("issues.json");
    Files.writeString(outsideFile, "{}\n");
    try {
      Files.createSymbolicLink(repositoryRoot.resolve("exports/link.json"), outsideFile);
      Files.createSymbolicLink(repositoryRoot.resolve("exports/link-dir"), outsideDirectory);
    } catch (UnsupportedOperationException | IOException | SecurityException exception) {
      assumeTrue(false, "symbolic links are unavailable: " + exception.getMessage());
    }

    assertInvalidAdapterImportPath(repositoryRoot, "exports/link.json", "symbolic link");
    assertInvalidAdapterImportPath(repositoryRoot, "exports/link-dir/issues.json", "symbolic link");
  }

  @Test
  void rejectsHardlinkedRootConfigAndAdapterImportFileWithoutLeakingTargets() throws Exception {
    Path repositoryRoot = repository("hardlinks");
    Path outsideConfig = tempDir.resolve("outside-hardlinked-config.yml");
    Files.writeString(outsideConfig, "version: 1\nsecret: FAKE_HARDLINKED_CONFIG_SECRET\n");
    createHardLink(repositoryRoot.resolve("agent-project-memory.yml"), outsideConfig);

    InvalidScanConfigException configException = assertThrows(
        InvalidScanConfigException.class,
        () -> loader.load(repositoryRoot, ScanPathContainment.canonicalRoot(repositoryRoot), null));

    Path adapterRepositoryRoot = repository("adapter-hardlink");
    Files.createDirectories(adapterRepositoryRoot.resolve("exports"));
    writeConfig(adapterRepositoryRoot, """
        version: 1
        adapters:
          local_structured_import:
            enabled: true
            path: exports/issues.json
        """);
    Path outsideImport = tempDir.resolve("outside-hardlinked-import.json");
    Files.writeString(outsideImport, "{\"format\":\"FAKE_HARDLINKED_IMPORT_SECRET\"}\n");
    createHardLink(adapterRepositoryRoot.resolve("exports/issues.json"), outsideImport);

    InvalidScanConfigException adapterException = assertThrows(
        InvalidScanConfigException.class,
        () -> loader.load(
            adapterRepositoryRoot,
            ScanPathContainment.canonicalRoot(adapterRepositoryRoot),
            null));

    assertAll(
        () -> assertTrue(configException.getMessage().contains("regular YAML file")),
        () -> assertFalse(configException.getMessage().contains(outsideConfig.toString())),
        () -> assertFalse(configException.getMessage().contains("FAKE_HARDLINKED_CONFIG_SECRET")),
        () -> assertTrue(adapterException.getMessage().contains("trusted regular file")),
        () -> assertFalse(adapterException.getMessage().contains(outsideImport.toString())),
        () -> assertFalse(adapterException.getMessage().contains("FAKE_HARDLINKED_IMPORT_SECRET")));
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

  private void assertInvalidAdapterImportPath(
      Path repositoryRoot,
      String importPath,
      String expectedMessage) throws Exception {
    writeConfig(repositoryRoot, """
        version: 1
        adapters:
          local_structured_import:
            enabled: true
            path: %s
        """.formatted(importPath));
    assertInvalid(repositoryRoot, null, expectedMessage);
  }

  private Path repository(String name) throws Exception {
    Path repositoryRoot = tempDir.resolve(name);
    Files.createDirectories(repositoryRoot);
    return repositoryRoot;
  }

  private void writeConfig(Path repositoryRoot, String content) throws Exception {
    Files.writeString(repositoryRoot.resolve("agent-project-memory.yml"), content);
  }

  private void createHardLink(Path link, Path existing) throws Exception {
    try {
      Files.createLink(link, existing);
    } catch (UnsupportedOperationException | IOException | SecurityException exception) {
      assumeTrue(false, "hard links are unavailable: " + exception.getMessage());
    }
  }
}
