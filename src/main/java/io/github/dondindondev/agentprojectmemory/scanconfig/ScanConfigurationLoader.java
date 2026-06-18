package io.github.dondindondev.agentprojectmemory.scanconfig;

import io.github.dondindondev.agentprojectmemory.analyzer.ScanPathContainment;
import io.github.dondindondev.agentprojectmemory.ingestion.adapter.AdapterConfiguration;
import io.github.dondindondev.agentprojectmemory.ingestion.adapter.AdapterLocalImport;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;

public final class ScanConfigurationLoader {
  public static final String DEFAULT_CONFIG_FILE_NAME = "agent-project-memory.yml";
  private static final int MAX_CONFIG_BYTES = 64 * 1024;
  private static final int MAX_NESTING_DEPTH = 12;
  private static final int MAX_RULES = 100;
  private static final java.util.regex.Pattern DRIVE_LETTER =
      java.util.regex.Pattern.compile("^[A-Za-z]:.*");
  private static final java.util.regex.Pattern URL_LIKE_SCHEME =
      java.util.regex.Pattern.compile("^[A-Za-z][A-Za-z0-9+.-]*://.*");
  private static final Set<String> ROOT_KEYS = Set.of(
      "version",
      "features",
      "documents",
      "adapters");
  private static final Set<String> FEATURE_KEYS = Set.of(
      "local_markdown",
      "generated_sources",
      "follow_symlinks");
  private static final Set<String> DOCUMENT_KEYS = Set.of("include", "exclude");
  private static final Set<String> ADAPTER_KEYS = Set.of("local_structured_import");
  private static final Set<String> LOCAL_STRUCTURED_IMPORT_KEYS = Set.of("enabled", "path");

  public ScanConfiguration load(
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      String explicitConfigPath) throws InvalidScanConfigException {
    Objects.requireNonNull(repositoryRoot, "repositoryRoot");
    Objects.requireNonNull(canonicalRepositoryRoot, "canonicalRepositoryRoot");
    Path normalizedRepositoryRoot = repositoryRoot.toAbsolutePath().normalize();
    Path normalizedCanonicalRepositoryRoot = canonicalRepositoryRoot.toAbsolutePath().normalize();

    Path selectedConfig = selectedConfig(
        normalizedRepositoryRoot,
        normalizedCanonicalRepositoryRoot,
        explicitConfigPath);
    if (selectedConfig == null) {
      return ScanConfiguration.defaultsOnly();
    }

    String sourcePath = repositoryRelativePath(normalizedRepositoryRoot, selectedConfig);
    ParsedConfig parsedConfig = parsedConfig(normalizedRepositoryRoot, selectedConfig);
    return new ScanConfiguration(
        "config_file",
        sourcePath,
        explicitConfigPath == null ? "applied" : "explicit",
        false,
        false,
        parsedConfig.localMarkdownEnabled(),
        parsedConfig.localMarkdownConfigured() ? "config_file" : "default",
        parsedConfig.documentIncludes(),
        parsedConfig.documentExcludes(),
        parsedConfig.adapterConfiguration());
  }

  private Path selectedConfig(
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      String explicitConfigPath) throws InvalidScanConfigException {
    if (explicitConfigPath != null) {
      return explicitConfig(repositoryRoot, canonicalRepositoryRoot, explicitConfigPath);
    }

    Path defaultConfig = repositoryRoot.resolve(DEFAULT_CONFIG_FILE_NAME).toAbsolutePath().normalize();
    if (Files.notExists(defaultConfig, LinkOption.NOFOLLOW_LINKS)) {
      return null;
    }
    return requireRegularYamlConfig(repositoryRoot, canonicalRepositoryRoot, defaultConfig, false);
  }

  private Path explicitConfig(
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      String explicitConfigPath) throws InvalidScanConfigException {
    Path rawPath;
    try {
      rawPath = Path.of(explicitConfigPath);
    } catch (InvalidPathException exception) {
      throw new InvalidScanConfigException("Invalid config: explicit config path is invalid.");
    }
    if (explicitConfigPath.isBlank()
        || rawPath.isAbsolute()
        || explicitConfigPath.startsWith("./")
        || explicitConfigPath.contains("\\")
        || DRIVE_LETTER.matcher(explicitConfigPath).matches()
        || URL_LIKE_SCHEME.matcher(explicitConfigPath).matches()) {
      throw new InvalidScanConfigException("Invalid config: explicit config path must be repository-relative.");
    }
    List<String> segments = List.of(explicitConfigPath.split("/", -1));
    for (String segment : segments) {
      if (segment.isEmpty() || ".".equals(segment) || "..".equals(segment)) {
        throw new InvalidScanConfigException("Invalid config: explicit config path contains an unsafe segment.");
      }
    }
    if (segments.get(0).equals(".project-memory")) {
      throw new InvalidScanConfigException("Invalid config: explicit config path points to generated output.");
    }
    if (!(explicitConfigPath.endsWith(".yml") || explicitConfigPath.endsWith(".yaml"))) {
      throw new InvalidScanConfigException("Invalid config: explicit config path must point to a YAML file.");
    }

    Path config = repositoryRoot.resolve(explicitConfigPath).toAbsolutePath().normalize();
    return requireRegularYamlConfig(repositoryRoot, canonicalRepositoryRoot, config, true);
  }

  private Path requireRegularYamlConfig(
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      Path config,
      boolean explicit) throws InvalidScanConfigException {
    if (!config.startsWith(repositoryRoot)) {
      throw new InvalidScanConfigException("Invalid config: config file must stay under scan root.");
    }
    if (Files.notExists(config, LinkOption.NOFOLLOW_LINKS)) {
      throw new InvalidScanConfigException(explicit
          ? "Invalid config: explicit config file was not found."
          : "Invalid config: default config file was not found.");
    }
    if (Files.isSymbolicLink(config) || hasSymbolicLinkSegment(repositoryRoot, config)) {
      throw new InvalidScanConfigException("Invalid config: config file must not be a symbolic link.");
    }
    if (!ScanPathContainment.isRegularFileUnderRootNoFollow(canonicalRepositoryRoot, config)) {
      throw new InvalidScanConfigException("Invalid config: config file must be a regular YAML file.");
    }
    try {
      BasicFileAttributes attributes = Files.readAttributes(
          config,
          BasicFileAttributes.class,
          LinkOption.NOFOLLOW_LINKS);
      if (attributes.size() > MAX_CONFIG_BYTES) {
        throw new InvalidScanConfigException("Invalid config: config file is too large.");
      }
    } catch (IOException exception) {
      throw new InvalidScanConfigException("Invalid config: config file metadata could not be read.");
    }
    return config;
  }

  private boolean hasSymbolicLinkSegment(Path repositoryRoot, Path path) {
    Path normalizedPath = path.toAbsolutePath().normalize();
    if (!normalizedPath.startsWith(repositoryRoot)) {
      return true;
    }
    Path current = repositoryRoot;
    for (Path part : repositoryRoot.relativize(normalizedPath)) {
      current = current.resolve(part);
      if (Files.isSymbolicLink(current)) {
        return true;
      }
    }
    return false;
  }

  private ParsedConfig parsedConfig(Path repositoryRoot, Path config) throws InvalidScanConfigException {
    Object root;
    try {
      String content = Files.readString(config, StandardCharsets.UTF_8);
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
          throw new InvalidScanConfigException("Invalid config: config must contain one YAML document.");
        }
      }
      if (documents.isEmpty()) {
        throw new InvalidScanConfigException("Invalid config: config must contain a mapping.");
      }
      root = documents.get(0);
    } catch (YAMLException exception) {
      throw new InvalidScanConfigException("Invalid config: YAML could not be parsed.");
    } catch (IOException exception) {
      throw new InvalidScanConfigException("Invalid config: config file could not be read.");
    }

    if (!(root instanceof Map<?, ?> rootMap)) {
      throw new InvalidScanConfigException("Invalid config: config must contain a mapping.");
    }
    rejectUnknownKeys(rootMap, ROOT_KEYS, "root");
    requireVersion(rootMap.get("version"));

    FeatureConfig featureConfig = featureConfig(rootMap.get("features"));
    DocumentRules documentRules = documentRules(rootMap.get("documents"));
    AdapterConfiguration adapterConfiguration = adapterConfiguration(
        rootMap.get("adapters"),
        repositoryRoot);
    return new ParsedConfig(
        featureConfig.localMarkdownEnabled(),
        featureConfig.localMarkdownConfigured(),
        documentRules.includes(),
        documentRules.excludes(),
        adapterConfiguration);
  }

  private void rejectUnknownKeys(Map<?, ?> map, Set<String> allowedKeys, String location)
      throws InvalidScanConfigException {
    for (Object key : map.keySet()) {
      if (!(key instanceof String stringKey) || !allowedKeys.contains(stringKey)) {
        throw new InvalidScanConfigException("Invalid config: unsupported key at " + location + ".");
      }
    }
  }

  private void requireVersion(Object value) throws InvalidScanConfigException {
    if (!(value instanceof Integer version) || version != 1) {
      throw new InvalidScanConfigException("Invalid config: version must be 1.");
    }
  }

  private FeatureConfig featureConfig(Object value) throws InvalidScanConfigException {
    if (value == null) {
      return new FeatureConfig(true, false);
    }
    if (!(value instanceof Map<?, ?> features)) {
      throw new InvalidScanConfigException("Invalid config: features must be a mapping.");
    }
    rejectUnknownKeys(features, FEATURE_KEYS, "features");
    boolean localMarkdownEnabled = true;
    boolean localMarkdownConfigured = false;
    for (Map.Entry<?, ?> entry : features.entrySet()) {
      String key = (String) entry.getKey();
      Object featureValue = entry.getValue();
      if (!(featureValue instanceof Boolean booleanValue)) {
        throw new InvalidScanConfigException("Invalid config: feature values must be boolean.");
      }
      if ("local_markdown".equals(key)) {
        localMarkdownEnabled = booleanValue;
        localMarkdownConfigured = true;
      } else if (booleanValue) {
        throw new InvalidScanConfigException("Invalid config: reserved scan modes cannot be enabled.");
      }
    }
    return new FeatureConfig(localMarkdownEnabled, localMarkdownConfigured);
  }

  private DocumentRules documentRules(Object value) throws InvalidScanConfigException {
    if (value == null) {
      return new DocumentRules(List.of(), List.of());
    }
    if (!(value instanceof Map<?, ?> documents)) {
      throw new InvalidScanConfigException("Invalid config: documents must be a mapping.");
    }
    rejectUnknownKeys(documents, DOCUMENT_KEYS, "documents");
    return new DocumentRules(
        pathRules(documents.get("include"), "documents.include", true),
        pathRules(documents.get("exclude"), "documents.exclude", false));
  }

  private AdapterConfiguration adapterConfiguration(Object value, Path repositoryRoot)
      throws InvalidScanConfigException {
    if (value == null) {
      return AdapterConfiguration.disabled();
    }
    if (!(value instanceof Map<?, ?> adapters)) {
      throw new InvalidScanConfigException("Invalid config: adapters must be a mapping.");
    }
    rejectUnknownKeys(adapters, ADAPTER_KEYS, "adapters");

    Object localImportValue = adapters.get("local_structured_import");
    if (localImportValue == null) {
      return AdapterConfiguration.disabled();
    }
    if (!(localImportValue instanceof Map<?, ?> localImport)) {
      throw new InvalidScanConfigException(
          "Invalid config: adapters.local_structured_import must be a mapping.");
    }
    rejectUnknownKeys(
        localImport,
        LOCAL_STRUCTURED_IMPORT_KEYS,
        "adapters.local_structured_import");

    Object pathValue = localImport.get("path");
    Object enabledValue = localImport.get("enabled");
    if (enabledValue == null) {
      if (pathValue != null) {
        throw new InvalidScanConfigException(
            "Invalid config: disabled adapter config must not declare an import path.");
      }
      return AdapterConfiguration.disabled();
    }
    if (!(enabledValue instanceof Boolean enabled)) {
      throw new InvalidScanConfigException(
          "Invalid config: adapters.local_structured_import.enabled must be boolean.");
    }

    if (!enabled) {
      if (pathValue != null) {
        throw new InvalidScanConfigException(
            "Invalid config: disabled adapter config must not declare an import path.");
      }
      return AdapterConfiguration.disabled();
    }
    if (pathValue == null) {
      throw new InvalidScanConfigException("Invalid config: adapter import path is required.");
    }
    if (!(pathValue instanceof String importPath)) {
      throw new InvalidScanConfigException("Invalid config: adapter import path must be a string.");
    }

    return AdapterConfiguration.enabledLocalImport(
        AdapterLocalImport.localStructuredImport(validAdapterLocalImportPath(repositoryRoot, importPath)));
  }

  private String validAdapterLocalImportPath(Path repositoryRoot, String importPath)
      throws InvalidScanConfigException {
    Path rawPath;
    try {
      rawPath = Path.of(importPath);
    } catch (InvalidPathException exception) {
      throw new InvalidScanConfigException("Invalid config: adapter import path is invalid.");
    }
    if (importPath.isBlank()
        || rawPath.isAbsolute()
        || importPath.startsWith("./")
        || importPath.contains("\\")
        || DRIVE_LETTER.matcher(importPath).matches()
        || URL_LIKE_SCHEME.matcher(importPath).matches()) {
      throw new InvalidScanConfigException(
          "Invalid config: adapter import path must be repository-relative.");
    }

    List<String> segments = List.of(importPath.split("/", -1));
    for (String segment : segments) {
      if (segment.isEmpty() || ".".equals(segment) || "..".equals(segment)) {
        throw new InvalidScanConfigException(
            "Invalid config: adapter import path contains an unsafe path segment.");
      }
      if (".project-memory".equals(segment)) {
        throw new InvalidScanConfigException(
            "Invalid config: adapter import path points to generated output.");
      }
    }

    Path importFile = repositoryRoot.resolve(importPath).toAbsolutePath().normalize();
    if (!importFile.startsWith(repositoryRoot)) {
      throw new InvalidScanConfigException(
          "Invalid config: adapter import path must stay under scan root.");
    }
    if (Files.notExists(importFile, LinkOption.NOFOLLOW_LINKS)) {
      throw new InvalidScanConfigException("Invalid config: adapter import file was not found.");
    }
    if (Files.isSymbolicLink(importFile) || hasSymbolicLinkSegment(repositoryRoot, importFile)) {
      throw new InvalidScanConfigException(
          "Invalid config: adapter import file must not be a symbolic link.");
    }
    Path canonicalRepositoryRoot;
    try {
      canonicalRepositoryRoot = ScanPathContainment.canonicalRoot(repositoryRoot);
    } catch (IOException exception) {
      throw new InvalidScanConfigException("Invalid config: scan root could not be resolved.");
    }
    if (!ScanPathContainment.isRegularFileUnderRootNoFollow(canonicalRepositoryRoot, importFile)) {
      throw new InvalidScanConfigException(
          "Invalid config: adapter import path must be a trusted regular file under scan root.");
    }
    return repositoryRelativePath(repositoryRoot, importFile);
  }

  private List<ScanConfigPathPattern> pathRules(Object value, String field, boolean includeRule)
      throws InvalidScanConfigException {
    if (value == null) {
      return List.of();
    }
    if (!(value instanceof List<?> items)) {
      throw new InvalidScanConfigException("Invalid config: " + field + " must be a list.");
    }
    if (items.size() > MAX_RULES) {
      throw new InvalidScanConfigException("Invalid config: too many path rules.");
    }
    List<ScanConfigPathPattern> rules = new ArrayList<>();
    for (int index = 0; index < items.size(); index++) {
      Object item = items.get(index);
      if (!(item instanceof String pattern)) {
        throw new InvalidScanConfigException(
            "Invalid config: " + field + "[" + index + "] must be a string.");
      }
      rules.add(ScanConfigPathPattern.parse(pattern, field + "[" + index + "]", includeRule));
    }
    return rules;
  }

  private String repositoryRelativePath(Path repositoryRoot, Path file) {
    return repositoryRoot.relativize(file.toAbsolutePath().normalize()).toString().replace('\\', '/');
  }

  private record FeatureConfig(boolean localMarkdownEnabled, boolean localMarkdownConfigured) {
  }

  private record DocumentRules(
      List<ScanConfigPathPattern> includes,
      List<ScanConfigPathPattern> excludes) {
  }

  private record ParsedConfig(
      boolean localMarkdownEnabled,
      boolean localMarkdownConfigured,
      List<ScanConfigPathPattern> documentIncludes,
      List<ScanConfigPathPattern> documentExcludes,
      AdapterConfiguration adapterConfiguration) {
  }
}
