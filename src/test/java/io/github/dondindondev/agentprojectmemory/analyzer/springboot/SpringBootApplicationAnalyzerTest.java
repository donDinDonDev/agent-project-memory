package io.github.dondindondev.agentprojectmemory.analyzer.springboot;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenModuleItem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class SpringBootApplicationAnalyzerTest {
  @TempDir
  private Path tempDir;

  private final SpringBootApplicationAnalyzer analyzer = new SpringBootApplicationAnalyzer();

  @Test
  void detectsDirectSpringBootApplicationSignalsWithBoundedMainMethod() throws Exception {
    writeFile(tempDir.resolve("src/main/java/com/example/ImportedApplication.java"), """
        package com.example;

        import org.springframework.boot.autoconfigure.SpringBootApplication;

        @SpringBootApplication
        class ImportedApplication {
          public static void main(String[] args) {
          }
        }
        """);
    writeFile(tempDir.resolve("src/main/java/com/example/FullyQualifiedApplication.java"), """
        package com.example;

        @org.springframework.boot.autoconfigure.SpringBootApplication
        class FullyQualifiedApplication {
        }
        """);

    SpringBootApplicationAnalysis analysis = analyzer.analyze(tempDir, List.of(rootModule()));

    ModuleSpringBootApplications module = analysis.modules().get(0);
    SpringBootApplicationFact fqcnApplication = module.applications().get(0);
    SpringBootApplicationFact importedApplication = module.applications().get(1);

    assertAll(
        () -> assertEquals("analyzed", module.analysisStatus()),
        () -> assertEquals(2, module.applications().size()),
        () -> assertEquals(
            "com.example.FullyQualifiedApplication",
            fqcnApplication.className()),
        () -> assertEquals(
            "spring_boot_application_annotation_only",
            fqcnApplication.applicationSignal()),
        () -> assertFalse(fqcnApplication.mainMethodPresent()),
        () -> assertEquals(
            "com.example.ImportedApplication",
            importedApplication.className()),
        () -> assertEquals(
            "spring_boot_application_with_main_method",
            importedApplication.applicationSignal()),
        () -> assertTrue(importedApplication.mainMethodPresent()),
        () -> assertEquals(2, importedApplication.evidenceIds().size()),
        () -> assertTrue(analysis.evidence().stream()
            .anyMatch(evidence -> "annotation".equals(evidence.sourceType())
                && "@SpringBootApplication".equals(evidence.symbolName()))),
        () -> assertTrue(analysis.evidence().stream()
            .anyMatch(evidence -> "code_symbol".equals(evidence.sourceType())
                && "main".equals(evidence.methodName()))));
  }

  @Test
  void mainMethodSignalRequiresStaticVoidStringArray() throws Exception {
    writeFile(tempDir.resolve("src/main/java/com/example/NotExecutableApplication.java"), """
        package com.example;

        import org.springframework.boot.autoconfigure.SpringBootApplication;

        @SpringBootApplication
        class NotExecutableApplication {
          void main(String[] args) {
          }

          static int main(String[] args) {
            return 0;
          }

          static void main(String arg) {
          }
        }
        """);

    SpringBootApplicationAnalysis analysis = analyzer.analyze(tempDir, List.of(rootModule()));

    SpringBootApplicationFact application = analysis.modules().get(0).applications().get(0);

    assertAll(
        () -> assertEquals(
            "spring_boot_application_annotation_only",
            application.applicationSignal()),
        () -> assertFalse(application.mainMethodPresent()),
        () -> assertTrue(application.mainMethodEvidenceIds().isEmpty()),
        () -> assertEquals(1, application.evidenceIds().size()));
  }

  @Test
  void mainMethodSignalAcceptsStaticVoidStringVarargs() throws Exception {
    writeFile(tempDir.resolve("src/main/java/com/example/VarargsApplication.java"), """
        package com.example;

        import org.springframework.boot.autoconfigure.SpringBootApplication;

        @SpringBootApplication
        class VarargsApplication {
          public static void main(String... args) {
          }
        }
        """);

    SpringBootApplicationAnalysis analysis = analyzer.analyze(tempDir, List.of(rootModule()));

    SpringBootApplicationFact application = analysis.modules().get(0).applications().get(0);

    assertAll(
        () -> assertEquals("com.example.VarargsApplication", application.className()),
        () -> assertEquals(
            "spring_boot_application_with_main_method",
            application.applicationSignal()),
        () -> assertTrue(application.mainMethodPresent()),
        () -> assertEquals(1, application.mainMethodEvidenceIds().size()),
        () -> assertEquals(2, application.evidenceIds().size()),
        () -> assertTrue(analysis.evidence().stream()
            .anyMatch(evidence -> "code_symbol".equals(evidence.sourceType())
                && "main".equals(evidence.methodName()))));
  }

  @Test
  void sourceDeclaredSpringBootApplicationAnnotationDoesNotEmitFacts() throws Exception {
    writeFile(
        tempDir.resolve(
            "src/main/java/org/springframework/boot/autoconfigure/SpringBootApplication.java"),
        """
            package org.springframework.boot.autoconfigure;

            public @interface SpringBootApplication {
            }
            """);
    writeFile(tempDir.resolve("src/main/java/com/example/SpoofedApplication.java"), """
        package com.example;

        @org.springframework.boot.autoconfigure.SpringBootApplication
        class SpoofedApplication {
          public static void main(String[] args) {
          }
        }
        """);

    SpringBootApplicationAnalysis analysis = analyzer.analyze(tempDir, List.of(rootModule()));

    assertAll(
        () -> assertEquals("analyzed", analysis.modules().get(0).analysisStatus()),
        () -> assertTrue(analysis.modules().get(0).applications().isEmpty()),
        () -> assertTrue(analysis.evidence().isEmpty()));
  }

  private MavenModuleItem rootModule() {
    return new MavenModuleItem(
        "module:.",
        ".",
        null,
        List.of("src/main/java"),
        List.of(),
        "supported",
        "scan_root",
        ".",
        List.of(),
        List.of());
  }

  private void writeFile(Path path, String content) throws Exception {
    Files.createDirectories(path.getParent());
    Files.writeString(path, content);
  }
}
