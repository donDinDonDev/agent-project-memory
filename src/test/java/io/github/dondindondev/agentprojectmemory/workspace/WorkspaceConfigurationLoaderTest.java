package io.github.dondindondev.agentprojectmemory.workspace;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class WorkspaceConfigurationLoaderTest {
  @TempDir
  private Path tempDir;

  private final WorkspaceConfigurationLoader loader = new WorkspaceConfigurationLoader();

  @Test
  void parsesExplicitWorkspaceConfigWithRequiredRepoIdsAndRelativeRoots() throws Exception {
    Path workspaceRoot = workspace("valid");
    Files.createDirectories(workspaceRoot.resolve("services/orders"));
    Files.createDirectories(workspaceRoot.resolve("services/billing"));
    Path config = writeWorkspaceConfig(workspaceRoot, """
        version: 1
        members:
          - repo_id: orders
            root: services/orders
          - repo_id: billing-api
            root: services/billing
        """);

    WorkspaceConfiguration configuration = loader.load(config.toString());

    assertAll(
        () -> assertEquals("agent-project-memory-workspace.yml", configuration.configPath()),
        () -> assertEquals(2, configuration.members().size()),
        () -> assertEquals("orders", configuration.members().get(0).repoId()),
        () -> assertEquals("services/orders", configuration.members().get(0).rootPath()),
        () -> assertEquals("billing-api", configuration.members().get(1).repoId()),
        () -> assertEquals("services/billing", configuration.members().get(1).rootPath()));
  }

  @Test
  void rejectsMalformedShapesAndUnknownKeysWithoutEchoingRawValues() throws Exception {
    Path workspaceRoot = workspace("malformed");
    Files.createDirectories(workspaceRoot.resolve("service"));

    Path config = writeWorkspaceConfig(workspaceRoot, """
        version: 1
        secret_token: FAKE_WORKSPACE_CONFIG_SECRET
        members:
          - repo_id: service
            root: service
        """);
    InvalidWorkspaceConfigException unknown = assertInvalid(config, "unsupported key");

    writeWorkspaceConfig(workspaceRoot, """
        version: 1
        members:
          - repo_id: service
            root: service
            display_name: FAKE_DISPLAY_NAME_SECRET
        """);
    InvalidWorkspaceConfigException memberUnknown = assertInvalid(config, "unsupported key");

    writeWorkspaceConfig(workspaceRoot, """
        version: 1
        version: 1
        members:
          - repo_id: service
            root: service
        """);
    InvalidWorkspaceConfigException duplicateKey = assertInvalid(config, "YAML could not be parsed");

    writeWorkspaceConfig(workspaceRoot, """
        version: 2
        members:
          - repo_id: service
            root: service
        """);
    InvalidWorkspaceConfigException version = assertInvalid(config, "version must be 1");

    writeWorkspaceConfig(workspaceRoot, """
        version: 1
        members: []
        """);
    InvalidWorkspaceConfigException emptyMembers = assertInvalid(config, "non-empty list");

    assertAll(
        () -> assertFalse(unknown.getMessage().contains("secret_token")),
        () -> assertFalse(unknown.getMessage().contains("FAKE_WORKSPACE_CONFIG_SECRET")),
        () -> assertFalse(memberUnknown.getMessage().contains("display_name")),
        () -> assertFalse(memberUnknown.getMessage().contains("FAKE_DISPLAY_NAME_SECRET")),
        () -> assertFalse(duplicateKey.getMessage().contains(workspaceRoot.toString())),
        () -> assertFalse(version.getMessage().contains(workspaceRoot.toString())),
        () -> assertFalse(emptyMembers.getMessage().contains(workspaceRoot.toString())));
  }

  @Test
  void rejectsMissingInvalidAndDuplicateRepoIds() throws Exception {
    Path workspaceRoot = workspace("repo-ids");
    Files.createDirectories(workspaceRoot.resolve("orders"));
    Files.createDirectories(workspaceRoot.resolve("billing"));
    Path config = writeWorkspaceConfig(workspaceRoot, """
        version: 1
        members:
          - root: orders
        """);
    assertInvalid(config, "repo_id is required");

    writeWorkspaceConfig(workspaceRoot, """
        version: 1
        members:
          - repo_id: https://example.invalid/orders
            root: orders
        """);
    assertInvalid(config, "safe logical identifier");

    writeWorkspaceConfig(workspaceRoot, """
        version: 1
        members:
          - repo_id: orders
            root: orders
          - repo_id: orders
            root: billing
        """);
    assertInvalid(config, "duplicate repo_id");
  }

  @Test
  void rejectsUnsafeMemberRootPaths() throws Exception {
    Path workspaceRoot = workspace("unsafe-paths");
    Files.createDirectories(workspaceRoot.resolve("service"));
    Files.createDirectories(workspaceRoot.resolve(".project-memory/member"));
    Path config = writeWorkspaceConfig(workspaceRoot, "version: 1\nmembers: []\n");

    assertInvalidRoot(workspaceRoot, config, "../outside", "unsafe path segment");
    assertInvalidRoot(workspaceRoot, config, ".", "unsafe path segment");
    assertInvalidRoot(workspaceRoot, config, "./service", "workspace-relative");
    assertInvalidRoot(workspaceRoot, config, "/tmp/service", "workspace-relative");
    assertInvalidRoot(workspaceRoot, config, "C:/service", "workspace-relative");
    assertInvalidRoot(workspaceRoot, config, "https://example.invalid/service", "workspace-relative");
    assertInvalidRoot(workspaceRoot, config, "service\\child", "workspace-relative");
    assertInvalidRoot(workspaceRoot, config, ".project-memory/member", "generated output");
  }

  @Test
  void rejectsMissingFileDuplicateAndNestedMemberRoots() throws Exception {
    Path workspaceRoot = workspace("roots");
    Files.createDirectories(workspaceRoot.resolve("services/orders"));
    Files.createDirectories(workspaceRoot.resolve("services/billing"));
    Files.writeString(workspaceRoot.resolve("not-a-directory"), "content");
    Path config = writeWorkspaceConfig(workspaceRoot, "version: 1\nmembers: []\n");

    assertInvalidRoot(workspaceRoot, config, "missing", "member root was not found");
    assertInvalidRoot(workspaceRoot, config, "not-a-directory", "must be a directory");

    writeWorkspaceConfig(workspaceRoot, """
        version: 1
        members:
          - repo_id: orders
            root: services/orders
          - repo_id: duplicate
            root: services/orders
        """);
    assertInvalid(config, "duplicate member roots");

    writeWorkspaceConfig(workspaceRoot, """
        version: 1
        members:
          - repo_id: services
            root: services
          - repo_id: orders
            root: services/orders
        """);
    assertInvalid(config, "ambiguous nested member roots");
  }

  @Test
  void rejectsSymlinkedConfigAndMemberRoots() throws Exception {
    Path workspaceRoot = workspace("symlinks");
    Files.createDirectories(workspaceRoot.resolve("real-service"));
    Path outsideConfig = tempDir.resolve("outside-workspace.yml");
    Files.writeString(outsideConfig, """
        version: 1
        members: []
        """);
    Path configLink = workspaceRoot.resolve("agent-project-memory-workspace.yml");
    try {
      Files.createSymbolicLink(configLink, outsideConfig);
      Files.createSymbolicLink(workspaceRoot.resolve("service-link"), workspaceRoot.resolve("real-service"));
    } catch (UnsupportedOperationException | IOException | SecurityException exception) {
      assumeTrue(false, "symbolic links are unavailable: " + exception.getMessage());
    }

    assertInvalid(configLink, "symbolic link");

    Files.delete(configLink);
    Path config = writeWorkspaceConfig(workspaceRoot, """
        version: 1
        members:
          - repo_id: linked
            root: service-link
        """);
    assertInvalid(config, "symbolic link");
  }

  @Test
  void rejectsHardlinkedConfigWithoutEchoingTargetOrContent() throws Exception {
    Path workspaceRoot = workspace("hardlinked-config");
    Files.createDirectories(workspaceRoot.resolve("service"));
    Path outsideConfig = tempDir.resolve("outside-hardlinked-workspace.yml");
    Files.writeString(outsideConfig, """
        version: 1
        members:
          - repo_id: service
            root: service
        secret: FAKE_HARDLINKED_WORKSPACE_SECRET
        """);
    Path config = workspaceRoot.resolve("agent-project-memory-workspace.yml");
    try {
      Files.createLink(config, outsideConfig);
    } catch (UnsupportedOperationException | IOException | SecurityException exception) {
      assumeTrue(false, "hard links are unavailable: " + exception.getMessage());
    }

    InvalidWorkspaceConfigException exception = assertInvalid(config, "trusted regular YAML file");

    assertAll(
        () -> assertFalse(exception.getMessage().contains(outsideConfig.toString())),
        () -> assertFalse(exception.getMessage().contains("FAKE_HARDLINKED_WORKSPACE_SECRET")),
        () -> assertFalse(exception.getMessage().contains(workspaceRoot.toString())));
  }

  @Test
  void rejectsMemberRootWhenLinkCountCannotBeVerified() throws Exception {
    Path workspaceRoot = workspace("unverifiable-root");
    Files.createDirectories(workspaceRoot.resolve("service"));
    Path config = writeWorkspaceConfig(workspaceRoot, """
        version: 1
        members:
          - repo_id: service
            root: service
        """);
    WorkspaceConfigurationLoader failingLoader = new WorkspaceConfigurationLoader(root -> {
      throw new UnsupportedOperationException("link count unavailable");
    });

    InvalidWorkspaceConfigException exception = assertThrows(
        InvalidWorkspaceConfigException.class,
        () -> failingLoader.load(config.toString()));

    assertAll(
        () -> assertTrue(exception.getMessage().contains("link count could not be verified")),
        () -> assertFalse(exception.getMessage().contains(workspaceRoot.toString())),
        () -> assertFalse(exception.getMessage().contains("link count unavailable")));
  }

  @Test
  void rejectsConfigInsideGeneratedOutputAndMissingConfigFiles() throws Exception {
    Path workspaceRoot = workspace("config-path");
    Files.createDirectories(workspaceRoot.resolve(".project-memory"));
    Path generatedConfig = workspaceRoot.resolve(".project-memory/workspace.yml");
    Files.writeString(generatedConfig, "version: 1\nmembers: []\n");

    assertInvalid(generatedConfig, "generated output");
    assertInvalid(workspaceRoot.resolve("missing.yml"), "config file was not found");
    assertInvalid(workspaceRoot.resolve("workspace.txt"), "YAML file");
  }

  private void assertInvalidRoot(
      Path workspaceRoot,
      Path config,
      String root,
      String expectedMessage) throws Exception {
    writeWorkspaceConfig(workspaceRoot, """
        version: 1
        members:
          - repo_id: service
            root: %s
        """.formatted(root));
    assertInvalid(config, expectedMessage);
  }

  private InvalidWorkspaceConfigException assertInvalid(Path config, String expectedMessage) {
    InvalidWorkspaceConfigException exception = assertThrows(
        InvalidWorkspaceConfigException.class,
        () -> loader.load(config.toString()));
    assertTrue(exception.getMessage().contains(expectedMessage));
    return exception;
  }

  private Path workspace(String name) throws Exception {
    Path workspaceRoot = tempDir.resolve(name);
    Files.createDirectories(workspaceRoot);
    return workspaceRoot;
  }

  private Path writeWorkspaceConfig(Path workspaceRoot, String content) throws Exception {
    Path config = workspaceRoot.resolve("agent-project-memory-workspace.yml");
    Files.writeString(config, content);
    return config;
  }
}
