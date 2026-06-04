package io.github.dondindondev.agentprojectmemory.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import org.junit.jupiter.api.Test;

final class AgentGuideGeneratorTest {
  private final AgentGuideGenerator generator = new AgentGuideGenerator();

  @Test
  void generatedGuideFromGoldenProjectMapAndEvidenceIndexMatchesGoldenFile() throws Exception {
    Path goldenRoot = goldenRoot("stage3-project-map");

    String guide = generator.generate(
        Files.readString(goldenRoot.resolve("project-map.json")),
        Files.readString(goldenRoot.resolve("evidence-index.jsonl")));

    assertEquals(Files.readString(goldenRoot.resolve("agent-guide.md")), guide);
    assertEvidenceIsAttachedToDetectedClaims(guide);
  }

  @Test
  void generatedGuideCapsLargeEvidenceListsAndInspectionPaths() throws Exception {
    Path goldenRoot = goldenRoot("large-agent-guide");

    String guide = generator.generate(
        Files.readString(goldenRoot.resolve("project-map.json")),
        Files.readString(goldenRoot.resolve("evidence-index.jsonl")));

    assertEquals(Files.readString(goldenRoot.resolve("agent-guide.md")), guide);
    assertTrue(guide.contains(
        "... and 3 more evidence references in `evidence-index.jsonl`"));
    assertTrue(guide.contains(
        "... and 2 more evidence paths in `evidence-index.jsonl`"));
    assertTrue(guide.contains("""
        - Inferred tested subject: `com.example.web.LargeController` (support_type: `inferred`, confidence: `medium`)
          - Evidence: `src/test/java/com/example/web/LargeControllerTest.java:3` (`ev:src/test/java/com/example/web/LargeControllerTest.java:3-3:com.example.web.LargeControllerTest:test_file`), `src/main/java/com/example/web/LargeController.java:8` (`ev:src/main/java/com/example/web/LargeController.java:8-8:com.example.web.LargeController:code_symbol`)
        """));
  }

  @Test
  void evidenceClassificationUsesResolvedSymbolNameOnly() throws Exception {
    String misleadingEntityEvidenceId =
        "ev:src/main/java/com/example/:@Table/Entity.java:1-1:com.example.TableNameSpoof:@Entity";
    String unresolvedEvidenceId =
        "ev:src/main/java/com/example/Entity.java:2-2:source-controlled:@Table";
    String projectMap = """
        {
          "project": {
            "build": {
              "system": "maven",
              "root_build_file": "pom.xml",
              "evidence_ids": []
            },
            "source_roots": [],
            "test_roots": []
          },
          "endpoints": [],
          "warnings": {
            "analysis_status": "not_detected",
            "items": []
          },
          "components": {
            "analysis_status": "not_detected",
            "items": []
          },
          "entities": {
            "analysis_status": "analyzed",
            "items": [
              {
                "id": "entity:com.example.TableNameSpoof",
                "class_name": "com.example.TableNameSpoof",
                "table_name": "orders",
                "identifier_fields": [],
                "relationships": [],
                "evidence_ids": [
                  "%s",
                  "%s"
                ]
              }
            ]
          },
          "tests": {
            "analysis_status": "not_detected",
            "items": []
          }
        }
        """.formatted(misleadingEntityEvidenceId, unresolvedEvidenceId);
    String evidenceIndex = """
        {"id":"%s","source_type":"annotation","path":"src/main/java/com/example/:@Table/Entity.java","class_name":"com.example.TableNameSpoof","method_name":null,"symbol_name":"@Entity","line_start":1,"line_end":1,"excerpt":"@Entity","confidence":"high"}
        """.formatted(misleadingEntityEvidenceId);

    String guide = generator.generate(projectMap, evidenceIndex);

    assertTrue(guide.contains("""
        - Entity: Detected `com.example.TableNameSpoof`
          - Evidence: `src/main/java/com/example/:@Table/Entity.java:1` (`ev:src/main/java/com/example/:@Table/Entity.java:1-1:com.example.TableNameSpoof:@Entity`), `ev:src/main/java/com/example/Entity.java:2-2:source-controlled:@Table` (unresolved evidence record)
        - Table: Detected `orders`
          - Evidence: none recorded.
        """));
  }

  @Test
  void evidenceJsonlParsingDoesNotSplitOnUnicodeLineSeparatorsInsideStrings()
      throws Exception {
    String lineSeparator = "\u2028";
    String paragraphSeparator = "\u2029";
    String projectMap = """
        {
          "project": {
            "build": {
              "system": "maven",
              "root_build_file": "pom.xml",
              "evidence_ids": [
                "ev:root-build"
              ]
            },
            "source_roots": [],
            "test_roots": []
          },
          "endpoints": [],
          "warnings": {
            "analysis_status": "not_detected",
            "items": []
          },
          "components": {
            "analysis_status": "not_detected",
            "items": []
          },
          "entities": {
            "analysis_status": "not_detected",
            "items": []
          },
          "tests": {
            "analysis_status": "not_detected",
            "items": []
          }
        }
        """;
    String evidenceIndex = """
        {"id":"ev:root-build","source_type":"build_file","path":"pom.xml%sline%sparagraph","class_name":null,"method_name":null,"symbol_name":"pom.xml","line_start":1,"line_end":1,"excerpt":"<project>%s</project>%s","confidence":"high"}
        """.formatted(lineSeparator, paragraphSeparator, lineSeparator, paragraphSeparator);

    String guide = generator.generate(projectMap, evidenceIndex);

    assertTrue(guide.contains(
        "`pom.xml\\u2028line\\u2029paragraph:1` (`ev:root-build`)"));
  }

  private void assertEvidenceIsAttachedToDetectedClaims(String guide) {
    assertTrue(guide.contains("""
        - Entity: Detected `com.example.domain.ProjectOrder`
          - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:26` (`ev:src/main/java/com/example/domain/ProjectEntities.java:26-26:com.example.domain.ProjectOrder:@Entity`)
        - Table: Detected `orders`
          - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:27` (`ev:src/main/java/com/example/domain/ProjectEntities.java:27-27:com.example.domain.ProjectOrder:@Table`)
        """));
    assertTrue(guide.contains("""
        - Identifier field: Detected `id` (`Long`) declared by `com.example.domain.ProjectBaseEntity` with source_kind `mapped_superclass`
          - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:16` (`ev:src/main/java/com/example/domain/ProjectEntities.java:16-16:com.example.domain.ProjectBaseEntity:@Id:field:id`), `src/main/java/com/example/domain/ProjectEntities.java:14` (`ev:src/main/java/com/example/domain/ProjectEntities.java:14-14:com.example.domain.ProjectBaseEntity:@MappedSuperclass`)
        """));
    assertFalse(guide.contains("""
        - Table: Detected `orders`
          - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:26` (`ev:src/main/java/com/example/domain/ProjectEntities.java:26-26:com.example.domain.ProjectOrder:@Entity`)
        """));
    assertTrue(guide.contains("""
        - Test class: Detected `com.example.web.ProjectMapControllerTest`
          - Evidence: `src/test/java/com/example/web/ProjectMapControllerTest.java:3` (`ev:src/test/java/com/example/web/ProjectMapControllerTest.java:3-3:com.example.web.ProjectMapControllerTest:test_file`)
        - Source: Detected `src/test/java/com/example/web/ProjectMapControllerTest.java`
        """));
    assertFalse(guide.contains("""
        - Inferred tested subject: `com.example.web.ProjectMapController` (support_type: `inferred`, confidence: `medium`)
          - Evidence: `src/test/java/com/example/web/ProjectMapControllerTest.java:3` (`ev:src/test/java/com/example/web/ProjectMapControllerTest.java:3-3:com.example.web.ProjectMapControllerTest:test_file`), `src/main/java/com/example/web/ProjectMapController.java:13` (`ev:src/main/java/com/example/web/ProjectMapController.java:13-13:com.example.web.ProjectMapController:code_symbol`)
          - Evidence: `src/test/java/com/example/web/ProjectMapControllerTest.java:3` (`ev:src/test/java/com/example/web/ProjectMapControllerTest.java:3-3:com.example.web.ProjectMapControllerTest:test_file`)
        """));
  }

  private Path goldenRoot(String fixtureName) throws Exception {
    return Path.of(Objects.requireNonNull(
        getClass().getResource("/golden/" + fixtureName)).toURI());
  }
}
