package io.github.dondindondev.agentprojectmemory.analyzer.springmvc;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class SpringMvcEndpointOutputGeneratorTest {
  private static final Pattern EVIDENCE_ID_ARRAY = Pattern.compile(
      "\"evidence_ids\": \\[(.*?)]",
      Pattern.DOTALL);
  private static final Pattern JSON_STRING = Pattern.compile("\"((?:\\\\.|[^\"\\\\])*)\"");
  private static final Pattern EVIDENCE_INDEX_ID = Pattern.compile("^\\{\"id\":\"([^\"]+)\"");

  @TempDir
  private Path tempDir;

  private final SpringMvcEndpointOutputGenerator generator = new SpringMvcEndpointOutputGenerator();

  @Test
  void generatedProjectMapAndEvidenceIndexMatchGoldenFiles() throws Exception {
    Path projectPath = tempDir.resolve("stage3-project-map");
    Path outputDirectory = projectPath.resolve(".project-memory");
    copyDirectory(fixtureRoot(), projectPath);
    Files.createDirectories(outputDirectory);

    SpringMvcEndpointOutputGenerator.Result result = generator.generate(
        projectPath,
        outputDirectory);

    assertAll(
        () -> assertTrue(result.generated()),
        () -> assertEquals(
            expected("project-map.json"),
            Files.readString(outputDirectory.resolve("project-map.json"))),
        () -> assertEquals(
            expected("evidence-index.jsonl"),
            Files.readString(outputDirectory.resolve("evidence-index.jsonl"))));
  }

  @Test
  void projectMapEvidenceIdsResolveToEvidenceIndexRecords() throws Exception {
    Path projectPath = tempDir.resolve("stage3-project-map");
    Path outputDirectory = projectPath.resolve(".project-memory");
    copyDirectory(fixtureRoot(), projectPath);
    Files.createDirectories(outputDirectory);

    generator.generate(projectPath, outputDirectory);

    String projectMap = Files.readString(outputDirectory.resolve("project-map.json"));
    String evidenceIndex = Files.readString(outputDirectory.resolve("evidence-index.jsonl"));
    Set<String> projectMapEvidenceIds = projectMapEvidenceIds(projectMap);
    Set<String> evidenceIndexIds = evidenceIndexIds(evidenceIndex);

    assertAll(
        () -> assertEquals(8, projectMapEvidenceIds.size()),
        () -> assertTrue(
            evidenceIndexIds.containsAll(projectMapEvidenceIds),
            "Every project-map evidence_ids entry must exist in evidence-index.jsonl"));
  }

  private Set<String> projectMapEvidenceIds(String projectMap) {
    Set<String> ids = new HashSet<>();
    var arrayMatcher = EVIDENCE_ID_ARRAY.matcher(projectMap);
    while (arrayMatcher.find()) {
      var stringMatcher = JSON_STRING.matcher(arrayMatcher.group(1));
      while (stringMatcher.find()) {
        ids.add(stringMatcher.group(1));
      }
    }
    return ids;
  }

  private Set<String> evidenceIndexIds(String evidenceIndex) {
    Set<String> ids = new HashSet<>();
    for (String line : evidenceIndex.lines().toList()) {
      var matcher = EVIDENCE_INDEX_ID.matcher(line);
      if (matcher.find()) {
        ids.add(matcher.group(1));
      }
    }
    return ids;
  }

  private String expected(String fileName) throws Exception {
    return Files.readString(goldenRoot().resolve(fileName));
  }

  private Path fixtureRoot() throws Exception {
    return Path.of(Objects.requireNonNull(
        getClass().getResource("/fixtures/stage3-project-map")).toURI());
  }

  private Path goldenRoot() throws Exception {
    return Path.of(Objects.requireNonNull(
        getClass().getResource("/golden/stage3-project-map")).toURI());
  }

  private void copyDirectory(Path source, Path target) throws Exception {
    try (var paths = Files.walk(source)) {
      for (Path sourcePath : paths.toList()) {
        Path targetPath = target.resolve(source.relativize(sourcePath));
        if (Files.isDirectory(sourcePath)) {
          Files.createDirectories(targetPath);
        } else {
          Files.createDirectories(targetPath.getParent());
          Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
      }
    }
  }
}
