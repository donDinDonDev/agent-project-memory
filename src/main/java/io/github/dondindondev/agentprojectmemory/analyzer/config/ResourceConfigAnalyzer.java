package io.github.dondindondev.agentprojectmemory.analyzer.config;

import io.github.dondindondev.agentprojectmemory.analyzer.ScanPathContainment;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenModuleItem;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public final class ResourceConfigAnalyzer {
  private static final String ANALYSIS_STATUS_ANALYZED = "analyzed";
  private static final String ANALYSIS_STATUS_NOT_DETECTED = "not_detected";
  private static final String MODULE_SUPPORTED = "supported";
  private static final String CONFIG_FILE_SOURCE_TYPE = "config_file";
  private static final String HIGH_CONFIDENCE = "high";
  private static final String MAIN_SCOPE = "main";
  private static final String TEST_SCOPE = "test";
  private static final String SPRING_APPLICATION_CONFIG_KIND = "spring_application";
  private static final String LOGGING_CONFIG_KIND = "logging_config";
  private static final String FILENAME_ONLY_PROFILE_SOURCE = "filename_only";
  private static final List<ResourceRootCandidate> RESOURCE_ROOTS = List.of(
      new ResourceRootCandidate(MAIN_SCOPE, "src/main/resources"),
      new ResourceRootCandidate(TEST_SCOPE, "src/test/resources"));
  private static final Set<String> LOGGING_CONFIG_FILENAMES = Set.of(
      "logback.xml",
      "logback-spring.xml",
      "log4j2.xml",
      "log4j2-spring.xml");
  private static final Comparator<ResourceRootFact> RESOURCE_ROOT_ORDER = Comparator
      .comparing(ResourceRootFact::scope)
      .thenComparing(ResourceRootFact::path)
      .thenComparing(ResourceRootFact::id);
  private static final Comparator<ConfigFileFact> CONFIG_FILE_ORDER = Comparator
      .comparing(ConfigFileFact::resourceScope)
      .thenComparing(ConfigFileFact::configKind)
      .thenComparing(ConfigFileFact::path)
      .thenComparing(ConfigFileFact::id);
  private static final Comparator<ResourceConfigEvidence> EVIDENCE_ORDER = Comparator
      .comparing(ResourceConfigEvidence::sourcePath)
      .thenComparing(evidence -> evidence.lineStart() == null ? Integer.MAX_VALUE : evidence.lineStart())
      .thenComparing(evidence -> evidence.lineEnd() == null ? Integer.MAX_VALUE : evidence.lineEnd())
      .thenComparing(evidence -> nullSafe(evidence.className()))
      .thenComparing(evidence -> nullSafe(evidence.methodName()))
      .thenComparing(ResourceConfigEvidence::symbolName)
      .thenComparing(ResourceConfigEvidence::id);

  public ResourceConfigAnalysis analyze(Path repositoryRoot, List<MavenModuleItem> modules)
      throws IOException {
    Objects.requireNonNull(repositoryRoot, "repositoryRoot");
    Objects.requireNonNull(modules, "modules");

    Path normalizedRepositoryRoot = repositoryRoot.toAbsolutePath().normalize();
    Path canonicalRepositoryRoot = ScanPathContainment.canonicalRoot(normalizedRepositoryRoot);
    Map<String, ResourceConfigEvidence> evidence = new LinkedHashMap<>();
    List<ModuleResourceConfig> moduleConfigs = new ArrayList<>();

    for (MavenModuleItem module : modules) {
      if (!MODULE_SUPPORTED.equals(module.supportStatus())) {
        moduleConfigs.add(notDetected(module.moduleId()));
        continue;
      }

      List<ResourceRootFact> resourceRoots = detectedResourceRoots(
          normalizedRepositoryRoot,
          canonicalRepositoryRoot,
          module);
      List<ConfigFileFact> configFiles = new ArrayList<>();
      for (ResourceRootFact resourceRoot : resourceRoots) {
        configFiles.addAll(detectedConfigFiles(
            normalizedRepositoryRoot,
            canonicalRepositoryRoot,
            module.moduleId(),
            resourceRoot,
            evidence));
      }

      String analysisStatus = resourceRoots.isEmpty()
          ? ANALYSIS_STATUS_NOT_DETECTED
          : ANALYSIS_STATUS_ANALYZED;
      moduleConfigs.add(new ModuleResourceConfig(
          module.moduleId(),
          analysisStatus,
          analysisStatus,
          resourceRoots.stream().sorted(RESOURCE_ROOT_ORDER).toList(),
          configFiles.stream().sorted(CONFIG_FILE_ORDER).toList()));
    }

    return new ResourceConfigAnalysis(
        moduleConfigs,
        evidence.values().stream()
            .sorted(EVIDENCE_ORDER)
            .toList());
  }

  private ModuleResourceConfig notDetected(String moduleId) {
    return new ModuleResourceConfig(
        moduleId,
        ANALYSIS_STATUS_NOT_DETECTED,
        ANALYSIS_STATUS_NOT_DETECTED,
        List.of(),
        List.of());
  }

  private List<ResourceRootFact> detectedResourceRoots(
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      MavenModuleItem module) {
    List<ResourceRootFact> resourceRoots = new ArrayList<>();
    for (ResourceRootCandidate candidate : RESOURCE_ROOTS) {
      String path = ".".equals(module.modulePath())
          ? candidate.path()
          : module.modulePath() + "/" + candidate.path();
      Path resourceRoot = repositoryRoot.resolve(path).normalize();
      if (!ScanPathContainment.isDirectoryUnderRoot(canonicalRepositoryRoot, resourceRoot)) {
        continue;
      }

      resourceRoots.add(new ResourceRootFact(
          "resource_root:" + module.moduleId() + ":" + candidate.scope() + ":" + path,
          candidate.scope(),
          path,
          List.of()));
    }
    return resourceRoots;
  }

  private List<ConfigFileFact> detectedConfigFiles(
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      String moduleId,
      ResourceRootFact resourceRoot,
      Map<String, ResourceConfigEvidence> evidence) throws IOException {
    Path resourceRootPath = repositoryRoot.resolve(resourceRoot.path()).normalize();
    List<ConfigFileFact> configFiles = new ArrayList<>();
    try (Stream<Path> paths = Files.walk(resourceRootPath)) {
      for (Path candidate : paths.toList()) {
        if (!ScanPathContainment.isRegularFileUnderRoot(canonicalRepositoryRoot, candidate)) {
          continue;
        }

        String filename = candidate.getFileName().toString();
        Optional<ConfigDescriptor> descriptor = configDescriptor(filename);
        if (descriptor.isEmpty()) {
          continue;
        }

        String sourcePath = repositoryRelativePath(repositoryRoot, candidate);
        ResourceConfigEvidence configEvidence = new ResourceConfigEvidence(
            "ev:" + sourcePath + ":unknown:config_file:" + filename,
            CONFIG_FILE_SOURCE_TYPE,
            sourcePath,
            null,
            null,
            filename,
            null,
            null,
            "config file detected: " + filename,
            HIGH_CONFIDENCE);
        evidence.putIfAbsent(configEvidence.id(), configEvidence);
        ConfigDescriptor configDescriptor = descriptor.orElseThrow();
        configFiles.add(new ConfigFileFact(
            "config_file:" + moduleId + ":" + configDescriptor.kind() + ":" + sourcePath,
            sourcePath,
            resourceRoot.scope(),
            configDescriptor.kind(),
            configDescriptor.format(),
            configDescriptor.profileName(),
            configDescriptor.profileName() == null ? null : FILENAME_ONLY_PROFILE_SOURCE,
            List.of(configEvidence.id())));
      }
    }
    return configFiles;
  }

  private Optional<ConfigDescriptor> configDescriptor(String filename) {
    Optional<ConfigDescriptor> applicationConfig = applicationConfigDescriptor(filename);
    if (applicationConfig.isPresent()) {
      return applicationConfig;
    }

    if (LOGGING_CONFIG_FILENAMES.contains(filename)) {
      return Optional.of(new ConfigDescriptor(
          LOGGING_CONFIG_KIND,
          "xml",
          null));
    }

    return Optional.empty();
  }

  private Optional<ConfigDescriptor> applicationConfigDescriptor(String filename) {
    for (String extension : List.of(".properties", ".yml", ".yaml")) {
      if (filename.equals("application" + extension)) {
        return Optional.of(new ConfigDescriptor(
            SPRING_APPLICATION_CONFIG_KIND,
            formatForExtension(extension),
            null));
      }

      String prefix = "application-";
      if (filename.startsWith(prefix) && filename.endsWith(extension)) {
        String profileName = filename.substring(prefix.length(), filename.length() - extension.length());
        if (!profileName.isBlank()) {
          return Optional.of(new ConfigDescriptor(
              SPRING_APPLICATION_CONFIG_KIND,
              formatForExtension(extension),
              profileName));
        }
      }
    }
    return Optional.empty();
  }

  private String formatForExtension(String extension) {
    return switch (extension.toLowerCase(Locale.ROOT)) {
      case ".properties" -> "properties";
      case ".yml", ".yaml" -> "yaml";
      case ".xml" -> "xml";
      default -> "unknown";
    };
  }

  private String repositoryRelativePath(Path repositoryRoot, Path path) {
    return repositoryRoot.relativize(path.normalize()).toString().replace('\\', '/');
  }

  private static String nullSafe(String value) {
    return value == null ? "" : value;
  }

  private record ResourceRootCandidate(String scope, String path) {
  }

  private record ConfigDescriptor(String kind, String format, String profileName) {
  }
}
