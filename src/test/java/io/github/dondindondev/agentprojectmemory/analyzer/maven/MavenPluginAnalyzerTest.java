package io.github.dondindondev.agentprojectmemory.analyzer.maven;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class MavenPluginAnalyzerTest {
  @TempDir
  private Path tempDir;

  private final MavenPluginAnalyzer analyzer = new MavenPluginAnalyzer();

  @Test
  void directPluginsAreExtractedWithBoundedExecutionsAndSignals() throws Exception {
    Path repositoryRoot = repository("direct-plugins");
    writePom(repositoryRoot.resolve("pom.xml"), """
        <project>
          <build>
            <plugins>
              <plugin>
                <groupId>org.openapitools</groupId>
                <artifactId>openapi-generator-maven-plugin</artifactId>
                <version>${openapi.generator.version}</version>
                <executions>
                  <execution>
                    <id>generate-api</id>
                    <phase>generate-sources</phase>
                    <goals>
                      <goal>generate</goal>
                    </goals>
                    <configuration>
                      <generatedSourcesDirectory>target/generated-sources/openapi</generatedSourcesDirectory>
                    </configuration>
                  </execution>
                </executions>
                <configuration>
                  <inputSpec>src/main/resources/private-api.yml</inputSpec>
                </configuration>
              </plugin>
            </plugins>
          </build>
        </project>
        """);

    MavenPluginAnalysis analysis = analyzer.analyze(repositoryRoot, List.of(rootModule()));
    MavenModulePlugins modulePlugins = modulePlugins(analysis, "module:.");
    MavenPluginDeclaration plugin = modulePlugins.plugins().get(0);
    MavenPluginExecution execution = plugin.executions().get(0);

    assertAll(
        () -> assertEquals("analyzed", modulePlugins.analysisStatus()),
        () -> assertEquals(1, modulePlugins.plugins().size()),
        () -> assertEquals(0, modulePlugins.pluginManagement().size()),
        () -> assertEquals("direct_plugin", plugin.declarationKind()),
        () -> assertEquals(1, plugin.declarationOrdinal()),
        () -> assertValue(plugin.groupId(), "org.openapitools", "literal"),
        () -> assertValue(plugin.artifactId(), "openapi-generator-maven-plugin", "literal"),
        () -> assertValue(plugin.version(), "${openapi.generator.version}", "property_reference"),
        () -> assertEquals("generate-api", execution.executionId()),
        () -> assertValue(execution.phase(), "generate-sources", "literal"),
        () -> assertEquals(1, execution.goals().size()),
        () -> assertValue(execution.goals().get(0), "generate", "literal"),
        () -> assertSignal(plugin.configurationSignals(), "input_spec_config_present"),
        () -> assertSignal(plugin.configurationSignals(), "generated_sources_config_present"),
        () -> assertSignal(plugin.generatorSignals(), "openapi_swagger_codegen"),
        () -> assertTrue(analysis.evidence().stream()
            .anyMatch(evidence -> evidence.symbolName().equals("maven:plugin:000001:artifactId"))),
        () -> assertTrue(analysis.evidence().stream()
            .filter(evidence -> evidence.symbolName().contains(":configuration:"))
            .allMatch(evidence -> !evidence.excerpt().contains("private-api.yml")
                && !evidence.excerpt().contains("target/generated-sources/openapi"))),
        () -> assertEvidenceIdsResolve(analysis));
  }

  @Test
  void pluginManagementDeclarationsStaySeparateFromDirectPlugins() throws Exception {
    Path repositoryRoot = repository("plugin-management-boundary");
    writePom(repositoryRoot.resolve("pom.xml"), """
        <project>
          <build>
            <pluginManagement>
              <plugins>
                <plugin>
                  <groupId>org.apache.maven.plugins</groupId>
                  <artifactId>maven-compiler-plugin</artifactId>
                  <configuration>
                    <annotationProcessorPaths>
                      <path>
                        <groupId>com.example</groupId>
                        <artifactId>processor</artifactId>
                      </path>
                    </annotationProcessorPaths>
                  </configuration>
                </plugin>
              </plugins>
            </pluginManagement>
            <plugins>
              <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
              </plugin>
            </plugins>
          </build>
        </project>
        """);

    MavenPluginAnalysis analysis = analyzer.analyze(repositoryRoot, List.of(rootModule()));
    MavenModulePlugins modulePlugins = modulePlugins(analysis, "module:.");
    MavenPluginDeclaration directPlugin = modulePlugins.plugins().get(0);
    MavenPluginDeclaration managedPlugin = modulePlugins.pluginManagement().get(0);

    assertAll(
        () -> assertEquals(1, modulePlugins.plugins().size()),
        () -> assertEquals(1, modulePlugins.pluginManagement().size()),
        () -> assertEquals("maven-surefire-plugin", directPlugin.artifactId().value()),
        () -> assertEquals("direct_plugin", directPlugin.declarationKind()),
        () -> assertEquals("maven-compiler-plugin", managedPlugin.artifactId().value()),
        () -> assertEquals("plugin_management", managedPlugin.declarationKind()),
        () -> assertSignal(managedPlugin.configurationSignals(), "annotation_processor_paths_present"),
        () -> assertSignal(managedPlugin.generatorSignals(), "annotation_processor"),
        () -> assertTrue(analysis.evidence().stream()
            .noneMatch(evidence -> evidence.excerpt().contains("processor"))),
        () -> assertEvidenceIdsResolve(analysis));
  }

  @Test
  void generatorAndBuildHelperSignalsAreConservativeAndEvidenceBacked() throws Exception {
    Path repositoryRoot = repository("generator-signals");
    writePom(repositoryRoot.resolve("pom.xml"), """
        <project>
          <build>
            <plugins>
              <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>jaxb2-maven-plugin</artifactId>
              </plugin>
              <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>build-helper-maven-plugin</artifactId>
                <executions>
                  <execution>
                    <goals>
                      <goal>add-source</goal>
                    </goals>
                  </execution>
                </executions>
              </plugin>
            </plugins>
          </build>
        </project>
        """);

    MavenPluginAnalysis analysis = analyzer.analyze(repositoryRoot, List.of(rootModule()));
    MavenModulePlugins modulePlugins = modulePlugins(analysis, "module:.");
    MavenPluginDeclaration buildHelperPlugin = modulePlugins.plugins().stream()
        .filter(plugin -> "build-helper-maven-plugin".equals(plugin.artifactId().value()))
        .findFirst()
        .orElseThrow();
    MavenPluginDeclaration jaxbPlugin = modulePlugins.plugins().stream()
        .filter(plugin -> "jaxb2-maven-plugin".equals(plugin.artifactId().value()))
        .findFirst()
        .orElseThrow();

    assertAll(
        () -> assertSignal(jaxbPlugin.generatorSignals(), "source_generator_plugin"),
        () -> assertSignal(buildHelperPlugin.configurationSignals(), "add_source_goal_present"),
        () -> assertFalse(buildHelperPlugin.generatorSignals().stream()
            .anyMatch(signal -> signal.signal().equals("source_generator_plugin"))),
        () -> assertEvidenceIdsResolve(analysis));
  }

  @Test
  void pluginsAreSortedDeterministicallyWithinEachSection() throws Exception {
    Path repositoryRoot = repository("plugin-sort");
    writePom(repositoryRoot.resolve("pom.xml"), """
        <project>
          <build>
            <plugins>
              <plugin>
                <groupId>com.zeta</groupId>
                <artifactId>b-plugin</artifactId>
              </plugin>
              <plugin>
                <groupId>com.alpha</groupId>
                <artifactId>c-plugin</artifactId>
              </plugin>
              <plugin>
                <groupId>com.alpha</groupId>
                <artifactId>a-plugin</artifactId>
              </plugin>
            </plugins>
          </build>
        </project>
        """);

    MavenPluginAnalysis analysis = analyzer.analyze(repositoryRoot, List.of(rootModule()));

    List<String> artifactIds = modulePlugins(analysis, "module:.").plugins().stream()
        .map(plugin -> plugin.artifactId().value())
        .toList();
    List<Integer> declarationOrdinals = modulePlugins(analysis, "module:.").plugins().stream()
        .map(MavenPluginDeclaration::declarationOrdinal)
        .toList();

    assertAll(
        () -> assertEquals(List.of("a-plugin", "c-plugin", "b-plugin"), artifactIds),
        () -> assertEquals(List.of(3, 2, 1), declarationOrdinals),
        () -> assertEvidenceIdsResolve(analysis));
  }

  @Test
  void missingPomProducesNotDetectedPluginSections() throws Exception {
    Path repositoryRoot = repository("missing-pom");

    MavenPluginAnalysis analysis = analyzer.analyze(
        repositoryRoot,
        List.of(new MavenModuleItem(
            "module:missing",
            "missing",
            null,
            List.of(),
            List.of(),
            "missing_child_pom",
            "root_modules_entry",
            "missing",
            List.of(),
            List.of())));
    MavenModulePlugins modulePlugins = modulePlugins(analysis, "module:missing");

    assertAll(
        () -> assertEquals("not_detected", modulePlugins.analysisStatus()),
        () -> assertEquals(0, modulePlugins.plugins().size()),
        () -> assertEquals(0, modulePlugins.pluginManagement().size()),
        () -> assertEquals(0, analysis.evidence().size()));
  }

  @Test
  void pomSymlinkIsNotParsedForPluginSignals() throws Exception {
    Path repositoryRoot = repository("pom-symlink");
    writePom(repositoryRoot.resolve("shared/pom.xml"), """
        <project>
          <build>
            <plugins>
              <plugin>
                <artifactId>openapi-generator-maven-plugin</artifactId>
              </plugin>
            </plugins>
          </build>
        </project>
        """);
    createSymbolicLink(repositoryRoot.resolve("pom.xml"), repositoryRoot.resolve("shared/pom.xml"));

    MavenPluginAnalysis analysis = analyzer.analyze(repositoryRoot, List.of(rootModule()));
    MavenModulePlugins modulePlugins = modulePlugins(analysis, "module:.");

    assertAll(
        () -> assertEquals("not_detected", modulePlugins.analysisStatus()),
        () -> assertEquals(List.of(), modulePlugins.plugins()),
        () -> assertEquals(List.of(), modulePlugins.pluginManagement()),
        () -> assertEquals(List.of(), analysis.evidence()));
  }

  @Test
  void xmlDoctypeIsRejectedBeforeEntityExpansion() throws Exception {
    Path repositoryRoot = repository("doctype-rejected");
    writePom(repositoryRoot.resolve("pom.xml"), """
        <!DOCTYPE project [
          <!ENTITY xxe SYSTEM "file:///etc/passwd">
        ]>
        <project>
          <build>
            <plugins>
              <plugin>
                <artifactId>&xxe;</artifactId>
              </plugin>
            </plugins>
          </build>
        </project>
        """);

    IOException exception = assertThrows(
        IOException.class,
        () -> analyzer.analyze(repositoryRoot, List.of(rootModule())));

    assertAll(
        () -> assertTrue(exception.getMessage().contains("Could not parse Maven plugins in pom.xml")),
        () -> assertTrue(exception.getMessage().contains("malformed XML")));
  }

  private Path repository(String name) throws Exception {
    Path repositoryRoot = tempDir.resolve(name);
    Files.createDirectories(repositoryRoot);
    return repositoryRoot;
  }

  private void writePom(Path pom, String xml) throws Exception {
    Files.createDirectories(pom.getParent());
    Files.writeString(pom, xml);
  }

  private void createSymbolicLink(Path link, Path target) throws Exception {
    try {
      Files.createSymbolicLink(link, target);
    } catch (UnsupportedOperationException | IOException | SecurityException exception) {
      assumeTrue(false, "symbolic links are unavailable: " + exception.getMessage());
    }
  }

  private MavenModuleItem rootModule() {
    return new MavenModuleItem(
        "module:.",
        ".",
        "pom.xml",
        List.of(),
        List.of(),
        "unsupported",
        "scan_root",
        ".",
        List.of(),
        List.of("ev:pom.xml:1-1:build_file:pom.xml"));
  }

  private MavenModulePlugins modulePlugins(MavenPluginAnalysis analysis, String moduleId) {
    return analysis.modules().stream()
        .filter(plugins -> plugins.moduleId().equals(moduleId))
        .findFirst()
        .orElseThrow();
  }

  private void assertValue(MavenMetadataValue value, String expectedValue, String expectedKind) {
    assertEquals(expectedValue, value.value());
    assertEquals(expectedKind, value.valueKind());
  }

  private void assertSignal(List<MavenPluginSignal> signals, String expectedSignal) {
    MavenPluginSignal signal = signals.stream()
        .filter(candidate -> candidate.signal().equals(expectedSignal))
        .findFirst()
        .orElseThrow();
    assertFalse(signal.evidenceIds().isEmpty());
  }

  private void assertEvidenceIdsResolve(MavenPluginAnalysis analysis) {
    Set<String> evidenceIds = analysis.evidence().stream()
        .map(MavenPluginEvidence::id)
        .collect(Collectors.toSet());
    List<String> referencedEvidenceIds = new ArrayList<>();
    for (MavenModulePlugins plugins : analysis.modules()) {
      plugins.plugins().forEach(plugin -> addReferencedEvidenceIds(referencedEvidenceIds, plugin));
      plugins.pluginManagement().forEach(plugin -> addReferencedEvidenceIds(referencedEvidenceIds, plugin));
    }

    assertTrue(evidenceIds.containsAll(referencedEvidenceIds));
    assertTrue(analysis.evidence().stream()
        .allMatch(evidence -> !Path.of(evidence.sourcePath()).isAbsolute()
            && !evidence.sourcePath().startsWith("./")));
  }

  private void addReferencedEvidenceIds(
      List<String> referencedEvidenceIds,
      MavenPluginDeclaration plugin) {
    referencedEvidenceIds.addAll(plugin.groupId().evidenceIds());
    referencedEvidenceIds.addAll(plugin.artifactId().evidenceIds());
    referencedEvidenceIds.addAll(plugin.version().evidenceIds());
    plugin.executions().forEach(execution -> {
      referencedEvidenceIds.addAll(execution.phase().evidenceIds());
      execution.goals().forEach(goal -> referencedEvidenceIds.addAll(goal.evidenceIds()));
      referencedEvidenceIds.addAll(execution.evidenceIds());
    });
    plugin.configurationSignals().forEach(signal ->
        referencedEvidenceIds.addAll(signal.evidenceIds()));
    plugin.generatorSignals().forEach(signal ->
        referencedEvidenceIds.addAll(signal.evidenceIds()));
    referencedEvidenceIds.addAll(plugin.evidenceIds());
  }
}
