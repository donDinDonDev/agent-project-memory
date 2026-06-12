package io.github.dondindondev.agentprojectmemory.analyzer.config;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenModuleItem;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class ResourceConfigAnalyzerTest {
  @TempDir
  private Path tempDir;

  private final ResourceConfigAnalyzer analyzer = new ResourceConfigAnalyzer();

  @Test
  void detectsResourceRootsAndSupportedConfigFilenamesWithoutReadingValues() throws Exception {
    Path repositoryRoot = repository("resource-config");
    writeFile(repositoryRoot.resolve("src/main/resources/application.yml"), """
        spring:
          datasource:
            password: FAKE_MAIN_DB_PASSWORD
        external-token: ${FAKE_ENV_TOKEN}
        """);
    writeFile(repositoryRoot.resolve("src/main/resources/application-prod.properties"), """
        api.secret=FAKE_PROD_API_SECRET
        """);
    writeFile(repositoryRoot.resolve("src/main/resources/logback-spring.xml"), """
        <configuration>
          <property name="password" value="FAKE_LOGBACK_SECRET"/>
        </configuration>
        """);
    writeFile(repositoryRoot.resolve("src/main/resources/bootstrap.yml"), """
        ignored.secret=FAKE_BOOTSTRAP_SECRET
        """);
    writeFile(repositoryRoot.resolve("src/test/resources/application-test.yaml"), """
        test:
          password: FAKE_TEST_CONFIG_SECRET
        """);
    writeFile(repositoryRoot.resolve("src/test/resources/log4j2.xml"), """
        <configuration password="FAKE_LOG4J_SECRET"/>
        """);

    ResourceConfigAnalysis analysis = analyzer.analyze(repositoryRoot, List.of(rootModule()));
    ModuleResourceConfig module = moduleConfig(analysis, "module:.");

    List<String> resourceRootPaths = module.resourceRoots().stream()
        .map(ResourceRootFact::path)
        .toList();
    List<String> configPaths = module.configFiles().stream()
        .map(ConfigFileFact::path)
        .toList();
    ConfigFileFact prodConfig = configFile(module, "src/main/resources/application-prod.properties");
    ConfigFileFact testConfig = configFile(module, "src/test/resources/application-test.yaml");
    ConfigFileFact loggingConfig = configFile(module, "src/main/resources/logback-spring.xml");

    assertAll(
        () -> assertEquals("analyzed", module.resourceAnalysisStatus()),
        () -> assertEquals("analyzed", module.configFileAnalysisStatus()),
        () -> assertEquals(
            List.of("src/main/resources", "src/test/resources"),
            resourceRootPaths),
        () -> assertEquals(
            List.of(
                "src/main/resources/logback-spring.xml",
                "src/main/resources/application-prod.properties",
                "src/main/resources/application.yml",
                "src/test/resources/log4j2.xml",
                "src/test/resources/application-test.yaml"),
            configPaths),
        () -> assertEquals("spring_application", prodConfig.configKind()),
        () -> assertEquals("properties", prodConfig.format()),
        () -> assertEquals("prod", prodConfig.profileName()),
        () -> assertEquals("filename_only", prodConfig.profileSource()),
        () -> assertEquals("yaml", testConfig.format()),
        () -> assertEquals("test", testConfig.profileName()),
        () -> assertEquals("logging_config", loggingConfig.configKind()),
        () -> assertEquals("xml", loggingConfig.format()),
        () -> assertTrue(analysis.evidence().stream()
            .anyMatch(evidence -> evidence.id()
                .equals("ev:src/main/resources/application-prod.properties:unknown:config_file:application-prod.properties"))),
        () -> assertTrue(analysis.evidence().stream()
            .allMatch(evidence -> evidence.excerpt().startsWith("config file detected: "))),
        () -> assertEvidenceIdsResolve(analysis),
        () -> assertSensitiveFixtureValuesDoNotAppear(analysis));
  }

  @Test
  void resourceRootSymlinkEscapingRepositoryRootIsIgnored() throws Exception {
    Path repositoryRoot = repository("resource-symlink-escape");
    Path outsideResourceRoot = tempDir.resolve("outside-resources");
    writeFile(outsideResourceRoot.resolve("application.yml"), """
        password: FAKE_OUTSIDE_RESOURCE_SECRET
        """);
    Files.createDirectories(repositoryRoot.resolve("src/main"));
    createSymbolicLink(repositoryRoot.resolve("src/main/resources"), outsideResourceRoot);

    ResourceConfigAnalysis analysis = analyzer.analyze(repositoryRoot, List.of(rootModule()));
    ModuleResourceConfig module = moduleConfig(analysis, "module:.");

    assertAll(
        () -> assertEquals("not_detected", module.resourceAnalysisStatus()),
        () -> assertEquals("not_detected", module.configFileAnalysisStatus()),
        () -> assertEquals(List.of(), module.resourceRoots()),
        () -> assertEquals(List.of(), module.configFiles()),
        () -> assertEquals(List.of(), analysis.evidence()),
        () -> assertSensitiveFixtureValuesDoNotAppear(analysis));
  }

  @Test
  void boundsConfigFileCandidatesBeforeFactMaterialization() throws Exception {
    Path repositoryRoot = repository("resource-config-candidate-cap");
    writeFile(repositoryRoot.resolve("src/main/resources/a/application.yml"), "name: a\n");
    writeFile(repositoryRoot.resolve("src/main/resources/b/application.yml"), "name: b\n");
    writeFile(repositoryRoot.resolve("src/main/resources/z/application.yml"), """
        password: FAKE_SKIPPED_RESOURCE_CONFIG_SECRET
        """);
    ResourceConfigAnalyzer cappedAnalyzer = new ResourceConfigAnalyzer(2);

    ResourceConfigAnalysis analysis = cappedAnalyzer.analyze(repositoryRoot, List.of(rootModule()));
    ModuleResourceConfig module = moduleConfig(analysis, "module:.");

    assertAll(
        () -> assertEquals(
            List.of(
                "src/main/resources/a/application.yml",
                "src/main/resources/b/application.yml"),
            module.configFiles().stream().map(ConfigFileFact::path).toList()),
        () -> assertEquals(2, analysis.evidence().size()),
        () -> assertEvidenceIdsResolve(analysis),
        () -> assertFalse(analysis.toString().contains("src/main/resources/z/application.yml")),
        () -> assertFalse(analysis.toString().contains("FAKE_SKIPPED_RESOURCE_CONFIG_SECRET")));
  }

  private Path repository(String name) throws Exception {
    Path repositoryRoot = tempDir.resolve(name);
    Files.createDirectories(repositoryRoot);
    return repositoryRoot;
  }

  private void writeFile(Path path, String content) throws Exception {
    Files.createDirectories(path.getParent());
    Files.writeString(path, content);
  }

  private MavenModuleItem rootModule() {
    return new MavenModuleItem(
        "module:.",
        ".",
        "pom.xml",
        List.of(),
        List.of(),
        "supported",
        "scan_root",
        ".",
        List.of(),
        List.of("ev:pom.xml:1-1:build_file:pom.xml"));
  }

  private ModuleResourceConfig moduleConfig(ResourceConfigAnalysis analysis, String moduleId) {
    return analysis.modules().stream()
        .filter(module -> module.moduleId().equals(moduleId))
        .findFirst()
        .orElseThrow();
  }

  private ConfigFileFact configFile(ModuleResourceConfig module, String path) {
    return module.configFiles().stream()
        .filter(configFile -> configFile.path().equals(path))
        .findFirst()
        .orElseThrow();
  }

  private void assertEvidenceIdsResolve(ResourceConfigAnalysis analysis) {
    Set<String> evidenceIds = analysis.evidence().stream()
        .map(ResourceConfigEvidence::id)
        .collect(Collectors.toSet());
    List<String> referencedEvidenceIds = new ArrayList<>();
    for (ModuleResourceConfig module : analysis.modules()) {
      module.resourceRoots().forEach(resourceRoot ->
          referencedEvidenceIds.addAll(resourceRoot.evidenceIds()));
      module.configFiles().forEach(configFile ->
          referencedEvidenceIds.addAll(configFile.evidenceIds()));
    }

    assertTrue(evidenceIds.containsAll(referencedEvidenceIds));
    assertTrue(analysis.evidence().stream()
        .allMatch(evidence -> !Path.of(evidence.sourcePath()).isAbsolute()
            && !evidence.sourcePath().startsWith("./")));
  }

  private void assertSensitiveFixtureValuesDoNotAppear(ResourceConfigAnalysis analysis) {
    String serializedFacts = analysis.toString();
    assertAll(
        () -> assertFalse(serializedFacts.contains("FAKE_MAIN_DB_PASSWORD")),
        () -> assertFalse(serializedFacts.contains("FAKE_ENV_TOKEN")),
        () -> assertFalse(serializedFacts.contains("FAKE_PROD_API_SECRET")),
        () -> assertFalse(serializedFacts.contains("FAKE_LOGBACK_SECRET")),
        () -> assertFalse(serializedFacts.contains("FAKE_BOOTSTRAP_SECRET")),
        () -> assertFalse(serializedFacts.contains("FAKE_TEST_CONFIG_SECRET")),
        () -> assertFalse(serializedFacts.contains("FAKE_LOG4J_SECRET")),
        () -> assertFalse(serializedFacts.contains("FAKE_OUTSIDE_RESOURCE_SECRET")));
  }

  private void createSymbolicLink(Path link, Path target) throws Exception {
    try {
      Files.createSymbolicLink(link, target);
    } catch (UnsupportedOperationException | IOException | SecurityException exception) {
      assumeTrue(false, "symbolic links are unavailable: " + exception.getMessage());
    }
  }
}
