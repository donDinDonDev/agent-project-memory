package io.github.dondindondev.agentprojectmemory.workspace;

import io.github.dondindondev.agentprojectmemory.analyzer.ScanPathContainment;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;

public final class WorkspaceConfigurationLoader {
  private static final int MAX_CONFIG_BYTES = 64 * 1024;
  private static final int MAX_MEMBERS = 128;
  private static final int MAX_NESTING_DEPTH = 8;
  private static final int MAX_REPO_ID_LENGTH = 64;
  private static final int MAX_ROOT_PATH_LENGTH = 512;
  private static final Set<String> ROOT_KEYS = Set.of("version", "members");
  private static final Set<String> MEMBER_KEYS = Set.of("repo_id", "root");
  private static final Pattern REPO_ID =
      Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]{0," + (MAX_REPO_ID_LENGTH - 1) + "}");
  private static final Pattern DRIVE_LETTER = Pattern.compile("^[A-Za-z]:.*");
  private static final Pattern URL_LIKE_SCHEME =
      Pattern.compile("^[A-Za-z][A-Za-z0-9+.-]*://.*");

  private final DirectoryLinkCountReader directoryLinkCountReader;

  public WorkspaceConfigurationLoader() {
    this((root) -> Files.getAttribute(root, "unix:nlink", LinkOption.NOFOLLOW_LINKS));
  }

  WorkspaceConfigurationLoader(DirectoryLinkCountReader directoryLinkCountReader) {
    this.directoryLinkCountReader = Objects.requireNonNull(
        directoryLinkCountReader,
        "directoryLinkCountReader");
  }

  public WorkspaceConfiguration load(String rawConfigPath)
      throws InvalidWorkspaceConfigException {
    if (rawConfigPath == null || rawConfigPath.isBlank()) {
      throw new InvalidWorkspaceConfigException(
          "Invalid workspace config: config path must not be blank.");
    }

    Path config = configPath(rawConfigPath);
    requireConfigPathSafe(config);
    Path workspaceRoot = config.getParent();
    if (workspaceRoot == null) {
      throw new InvalidWorkspaceConfigException(
          "Invalid workspace config: config file must have a parent directory.");
    }
    requireWorkspaceRootSafe(workspaceRoot);
    Path canonicalWorkspaceRoot = canonicalWorkspaceRoot(workspaceRoot);
    requireConfigFileSafe(canonicalWorkspaceRoot, config);

    Object root = parseConfig(config);
    if (!(root instanceof Map<?, ?> rootMap)) {
      throw new InvalidWorkspaceConfigException(
          "Invalid workspace config: config must contain a mapping.");
    }
    rejectUnknownKeys(rootMap, ROOT_KEYS, "root");
    requireVersion(rootMap.get("version"));

    List<WorkspaceMember> members = members(
        rootMap.get("members"),
        workspaceRoot,
        canonicalWorkspaceRoot);
    return new WorkspaceConfiguration(
        workspaceRoot,
        canonicalWorkspaceRoot,
        workspaceRelativePath(workspaceRoot, config),
        members);
  }

  private Path configPath(String rawConfigPath) throws InvalidWorkspaceConfigException {
    try {
      return Path.of(rawConfigPath).toAbsolutePath().normalize();
    } catch (InvalidPathException exception) {
      throw new InvalidWorkspaceConfigException(
          "Invalid workspace config: config path is invalid.");
    }
  }

  private void requireConfigPathSafe(Path config) throws InvalidWorkspaceConfigException {
    if (!(config.toString().endsWith(".yml") || config.toString().endsWith(".yaml"))) {
      throw new InvalidWorkspaceConfigException(
          "Invalid workspace config: config path must point to a YAML file.");
    }
    if (containsSegment(config, ".project-memory")) {
      throw new InvalidWorkspaceConfigException(
          "Invalid workspace config: config path must not point to generated output.");
    }
  }

  private void requireWorkspaceRootSafe(Path workspaceRoot) throws InvalidWorkspaceConfigException {
    if (Files.notExists(workspaceRoot, LinkOption.NOFOLLOW_LINKS)) {
      throw new InvalidWorkspaceConfigException(
          "Invalid workspace config: workspace root was not found.");
    }
    if (!Files.isDirectory(workspaceRoot, LinkOption.NOFOLLOW_LINKS)) {
      throw new InvalidWorkspaceConfigException(
          "Invalid workspace config: workspace root must be a directory.");
    }
    if (Files.isSymbolicLink(workspaceRoot)) {
      throw new InvalidWorkspaceConfigException(
          "Invalid workspace config: workspace root must not be a symbolic link.");
    }
  }

  private Path canonicalWorkspaceRoot(Path workspaceRoot) throws InvalidWorkspaceConfigException {
    try {
      return ScanPathContainment.canonicalRoot(workspaceRoot);
    } catch (IOException exception) {
      throw new InvalidWorkspaceConfigException(
          "Invalid workspace config: workspace root could not be resolved.");
    }
  }

  private void requireConfigFileSafe(Path canonicalWorkspaceRoot, Path config)
      throws InvalidWorkspaceConfigException {
    if (Files.notExists(config, LinkOption.NOFOLLOW_LINKS)) {
      throw new InvalidWorkspaceConfigException(
          "Invalid workspace config: config file was not found.");
    }
    if (Files.isSymbolicLink(config)) {
      throw new InvalidWorkspaceConfigException(
          "Invalid workspace config: config file must not be a symbolic link.");
    }
    if (!ScanPathContainment.isRegularFileUnderRootNoFollow(canonicalWorkspaceRoot, config)) {
      throw new InvalidWorkspaceConfigException(
          "Invalid workspace config: config file must be a trusted regular YAML file.");
    }
    try {
      BasicFileAttributes attributes = Files.readAttributes(
          config,
          BasicFileAttributes.class,
          LinkOption.NOFOLLOW_LINKS);
      if (attributes.size() > MAX_CONFIG_BYTES) {
        throw new InvalidWorkspaceConfigException(
            "Invalid workspace config: config file is too large.");
      }
    } catch (IOException exception) {
      throw new InvalidWorkspaceConfigException(
          "Invalid workspace config: config file metadata could not be read.");
    }
  }

  private Object parseConfig(Path config) throws InvalidWorkspaceConfigException {
    try {
      String content = ScanPathContainment.readRegularFileStringNoFollowStable(
          config,
          StandardCharsets.UTF_8,
          MAX_CONFIG_BYTES);
      LoaderOptions loaderOptions = new LoaderOptions();
      loaderOptions.setAllowDuplicateKeys(false);
      loaderOptions.setMaxAliasesForCollections(0);
      loaderOptions.setNestingDepthLimit(MAX_NESTING_DEPTH);
      loaderOptions.setCodePointLimit(MAX_CONFIG_BYTES);
      Yaml yaml = new Yaml(new SafeConstructor(loaderOptions));
      List<Object> documents = new ArrayList<>();
      for (Object document : yaml.loadAll(content)) {
        if (document != null) {
          documents.add(document);
        }
        if (documents.size() > 1) {
          throw new InvalidWorkspaceConfigException(
              "Invalid workspace config: config must contain one YAML document.");
        }
      }
      if (documents.isEmpty()) {
        throw new InvalidWorkspaceConfigException(
            "Invalid workspace config: config must contain a mapping.");
      }
      return documents.get(0);
    } catch (ScanPathContainment.FileSizeLimitExceededException exception) {
      throw new InvalidWorkspaceConfigException(
          "Invalid workspace config: config file is too large.");
    } catch (YAMLException exception) {
      throw new InvalidWorkspaceConfigException(
          "Invalid workspace config: YAML could not be parsed.");
    } catch (IOException | SecurityException exception) {
      throw new InvalidWorkspaceConfigException(
          "Invalid workspace config: config file could not be read.");
    }
  }

  private void rejectUnknownKeys(Map<?, ?> map, Set<String> allowedKeys, String location)
      throws InvalidWorkspaceConfigException {
    for (Object key : map.keySet()) {
      if (!(key instanceof String stringKey) || !allowedKeys.contains(stringKey)) {
        throw new InvalidWorkspaceConfigException(
            "Invalid workspace config: unsupported key at " + location + ".");
      }
    }
  }

  private void requireVersion(Object value) throws InvalidWorkspaceConfigException {
    if (!(value instanceof Integer version) || version != 1) {
      throw new InvalidWorkspaceConfigException(
          "Invalid workspace config: version must be 1.");
    }
  }

  private List<WorkspaceMember> members(
      Object value,
      Path workspaceRoot,
      Path canonicalWorkspaceRoot) throws InvalidWorkspaceConfigException {
    if (!(value instanceof List<?> items)) {
      throw new InvalidWorkspaceConfigException(
          "Invalid workspace config: members must be a non-empty list.");
    }
    if (items.isEmpty()) {
      throw new InvalidWorkspaceConfigException(
          "Invalid workspace config: members must be a non-empty list.");
    }
    if (items.size() > MAX_MEMBERS) {
      throw new InvalidWorkspaceConfigException(
          "Invalid workspace config: too many workspace members.");
    }

    List<ValidatedMember> validatedMembers = new ArrayList<>();
    Set<String> repoIds = new HashSet<>();
    Set<Path> roots = new HashSet<>();
    for (Object item : items) {
      if (!(item instanceof Map<?, ?> memberMap)) {
        throw new InvalidWorkspaceConfigException(
            "Invalid workspace config: member entries must be mappings.");
      }
      rejectUnknownKeys(memberMap, MEMBER_KEYS, "members");
      String repoId = repoId(memberMap.get("repo_id"));
      String rootPath = rootPath(memberMap.get("root"));
      if (!repoIds.add(repoId)) {
        throw new InvalidWorkspaceConfigException(
            "Invalid workspace config: duplicate repo_id values are not allowed.");
      }

      Path memberRoot = memberRoot(workspaceRoot, canonicalWorkspaceRoot, rootPath);
      if (!roots.add(memberRoot)) {
        throw new InvalidWorkspaceConfigException(
            "Invalid workspace config: duplicate member roots are not allowed.");
      }
      validatedMembers.add(new ValidatedMember(repoId, rootPath, memberRoot));
    }
    rejectNestedRoots(validatedMembers);
    return validatedMembers.stream()
        .map(member -> new WorkspaceMember(member.repoId(), member.rootPath(), member.canonicalRoot()))
        .toList();
  }

  private String repoId(Object value) throws InvalidWorkspaceConfigException {
    if (!(value instanceof String repoId) || repoId.isBlank()) {
      throw new InvalidWorkspaceConfigException(
          "Invalid workspace config: member repo_id is required.");
    }
    if (repoId.length() > MAX_REPO_ID_LENGTH || !REPO_ID.matcher(repoId).matches()) {
      throw new InvalidWorkspaceConfigException(
          "Invalid workspace config: member repo_id must be a safe logical identifier.");
    }
    return repoId;
  }

  private String rootPath(Object value) throws InvalidWorkspaceConfigException {
    if (!(value instanceof String rootPath) || rootPath.isBlank()) {
      throw new InvalidWorkspaceConfigException(
          "Invalid workspace config: member root is required.");
    }
    Path rawPath;
    try {
      rawPath = Path.of(rootPath);
    } catch (InvalidPathException exception) {
      throw new InvalidWorkspaceConfigException(
          "Invalid workspace config: member root path is invalid.");
    }
    if (rootPath.length() > MAX_ROOT_PATH_LENGTH
        || rawPath.isAbsolute()
        || rootPath.startsWith("./")
        || rootPath.contains("\\")
        || DRIVE_LETTER.matcher(rootPath).matches()
        || URL_LIKE_SCHEME.matcher(rootPath).matches()) {
      throw new InvalidWorkspaceConfigException(
          "Invalid workspace config: member roots must be workspace-relative paths.");
    }

    List<String> segments = List.of(rootPath.split("/", -1));
    for (String segment : segments) {
      if (segment.isEmpty() || ".".equals(segment) || "..".equals(segment)) {
        throw new InvalidWorkspaceConfigException(
            "Invalid workspace config: member root contains an unsafe path segment.");
      }
      if (".project-memory".equals(segment)) {
        throw new InvalidWorkspaceConfigException(
            "Invalid workspace config: member root points to generated output.");
      }
    }
    return String.join("/", segments);
  }

  private Path memberRoot(
      Path workspaceRoot,
      Path canonicalWorkspaceRoot,
      String rootPath) throws InvalidWorkspaceConfigException {
    Path root = workspaceRoot.resolve(rootPath).toAbsolutePath().normalize();
    if (!root.startsWith(workspaceRoot)) {
      throw new InvalidWorkspaceConfigException(
          "Invalid workspace config: member root must stay under workspace root.");
    }
    if (Files.notExists(root, LinkOption.NOFOLLOW_LINKS)) {
      throw new InvalidWorkspaceConfigException(
          "Invalid workspace config: member root was not found.");
    }
    if (Files.isSymbolicLink(root) || hasSymbolicLinkSegment(workspaceRoot, root)) {
      throw new InvalidWorkspaceConfigException(
          "Invalid workspace config: member root must not be a symbolic link.");
    }
    if (!Files.isDirectory(root, LinkOption.NOFOLLOW_LINKS)) {
      throw new InvalidWorkspaceConfigException(
          "Invalid workspace config: member root must be a directory.");
    }
    DirectorySnapshot before = directorySnapshot(root);
    Path realRoot;
    try {
      realRoot = root.toRealPath();
    } catch (IOException exception) {
      throw new InvalidWorkspaceConfigException(
          "Invalid workspace config: member root could not be resolved.");
    }
    DirectorySnapshot after = directorySnapshot(root);
    if (!before.matches(after)) {
      throw new InvalidWorkspaceConfigException(
          "Invalid workspace config: member root identity changed during validation.");
    }
    if (!realRoot.startsWith(canonicalWorkspaceRoot)) {
      throw new InvalidWorkspaceConfigException(
          "Invalid workspace config: member root must stay under workspace root.");
    }
    return realRoot;
  }

  private DirectorySnapshot directorySnapshot(Path root) throws InvalidWorkspaceConfigException {
    try {
      BasicFileAttributes attributes = Files.readAttributes(
          root,
          BasicFileAttributes.class,
          LinkOption.NOFOLLOW_LINKS);
      if (!attributes.isDirectory()) {
        throw new InvalidWorkspaceConfigException(
            "Invalid workspace config: member root must be a directory.");
      }
      if (attributes.fileKey() == null) {
        throw new InvalidWorkspaceConfigException(
            "Invalid workspace config: member root identity could not be verified.");
      }
      return new DirectorySnapshot(
          attributes.fileKey(),
          attributes.lastModifiedTime().toMillis(),
          directoryLinkCount(root));
    } catch (IOException | SecurityException exception) {
      throw new InvalidWorkspaceConfigException(
          "Invalid workspace config: member root metadata could not be read.");
    }
  }

  private long directoryLinkCount(Path root) throws InvalidWorkspaceConfigException {
    Object value;
    try {
      value = directoryLinkCountReader.read(root);
    } catch (IOException | UnsupportedOperationException | IllegalArgumentException
        | SecurityException exception) {
      throw new InvalidWorkspaceConfigException(
          "Invalid workspace config: member root link count could not be verified.");
    }
    if (!(value instanceof Number number) || number.longValue() < 1L) {
      throw new InvalidWorkspaceConfigException(
          "Invalid workspace config: member root link count could not be verified.");
    }
    return number.longValue();
  }

  private void rejectNestedRoots(List<ValidatedMember> members)
      throws InvalidWorkspaceConfigException {
    for (int left = 0; left < members.size(); left++) {
      Path leftRoot = members.get(left).canonicalRoot();
      for (int right = left + 1; right < members.size(); right++) {
        Path rightRoot = members.get(right).canonicalRoot();
        if (leftRoot.startsWith(rightRoot) || rightRoot.startsWith(leftRoot)) {
          throw new InvalidWorkspaceConfigException(
              "Invalid workspace config: ambiguous nested member roots are not allowed.");
        }
      }
    }
  }

  private boolean hasSymbolicLinkSegment(Path root, Path path) {
    Path current = root;
    for (Path part : root.relativize(path.toAbsolutePath().normalize())) {
      current = current.resolve(part);
      if (Files.isSymbolicLink(current)) {
        return true;
      }
    }
    return false;
  }

  private boolean containsSegment(Path path, String segment) {
    for (Path part : path) {
      if (segment.equals(part.toString())) {
        return true;
      }
    }
    return false;
  }

  private String workspaceRelativePath(Path workspaceRoot, Path path) {
    return workspaceRoot.relativize(path.toAbsolutePath().normalize()).toString().replace('\\', '/');
  }

  private record ValidatedMember(
      String repoId,
      String rootPath,
      Path canonicalRoot) {
  }

  private record DirectorySnapshot(
      Object fileKey,
      long lastModifiedMillis,
      long linkCount) {
    private boolean matches(DirectorySnapshot other) {
      return Objects.equals(fileKey, other.fileKey)
          && lastModifiedMillis == other.lastModifiedMillis
          && linkCount == other.linkCount;
    }
  }

  @FunctionalInterface
  interface DirectoryLinkCountReader {
    Object read(Path root) throws IOException;
  }
}
