package io.github.dondindondev.agentprojectmemory.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.dondindondev.agentprojectmemory.OutputRedactor;
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
    assertTrue(guide.contains("## Detected JPA Entities"));
    assertTrue(guide.contains(
        "For persistence changes, inspect detected entity evidence in "
            + "`src/main/java/com/example/domain/ProjectEntities.java`"));
    assertEvidenceIsAttachedToDetectedClaims(guide);
  }

  @Test
  void redactsLegacyOpenApiOperationIdWhenRenderingGuide() throws Exception {
    String guide = generator.generate("""
        {
          "schema_version": "1.0",
          "project": {
            "build": {
              "system": "maven",
              "root_build_file": "pom.xml",
              "evidence_ids": []
            },
            "source_roots": [],
            "test_roots": [],
            "modules": {
              "analysis_status": "analyzed",
              "items": []
            }
          },
          "api_surface": {
            "openapi": {
              "spec_files": {
                "analysis_status": "analyzed",
                "items": []
              },
              "operations": {
                "analysis_status": "analyzed",
                "items": [
                  {
                    "id": "api-operation:get:/tokens",
                    "module_id": "module:.",
                    "spec_path": "src/main/resources/openapi.yml",
                    "http_method": "GET",
                    "path": "/tokens",
                    "operation_id": "password=FAKE_V300_GUIDE_OPERATION_ID_SECRET",
                    "tags": ["Tokens"],
                    "implementation_status": "not_analyzed",
                    "evidence_ids": []
                  }
                ]
              }
            }
          }
        }
        """, "");

    assertTrue(guide.contains("operationId `password=" + OutputRedactor.REDACTION_MARKER + "`"));
    assertFalse(guide.contains("FAKE_V300_GUIDE_OPERATION_ID_SECRET"));
  }

  @Test
  void noDomainGuideSkipsJpaSectionAndPersistenceInspectionHint() throws Exception {
    String projectMap = """
        {
          "schema_version": "0.8",
          "project": {
            "build": {
              "system": "maven",
              "root_build_file": "pom.xml",
              "evidence_ids": ["ev:pom.xml:1-1:build_file:pom.xml"]
            },
            "source_roots": ["src/main/java"],
            "test_roots": [],
            "modules": {
              "analysis_status": "analyzed",
              "items": [
                {
                  "module_id": "module:.",
                  "module_path": ".",
                  "pom_path": "pom.xml",
                  "support_status": "supported",
                  "declaration_kind": "scan_root",
                  "declared_path": ".",
                  "source_roots": ["src/main/java"],
                  "test_roots": [],
                  "declaration_evidence_ids": [],
                  "pom_evidence_ids": ["ev:pom.xml:1-1:build_file:pom.xml"]
                }
              ]
            }
          },
          "endpoints": [],
          "warnings": {
            "analysis_status": "analyzed",
            "items": []
          },
          "components": {
            "analysis_status": "analyzed",
            "items": []
          },
          "entities": {
            "analysis_status": "analyzed",
            "items": [],
            "embeddables": {
              "analysis_status": "analyzed",
              "items": []
            }
          },
          "tests": {
            "analysis_status": "not_detected",
            "items": []
          },
          "spring_application_surface": {
            "analysis_status": "analyzed",
            "repositories": {
              "analysis_status": "analyzed",
              "items": [
                {
                  "id": "spring_data_repository_interface_signal:com.example.NoDomainRepository",
                  "module_id": "module:.",
                  "surface_category": "spring_data_repository_interface_signal",
                  "support_type": "inferred",
                  "class_name": "com.example.NoDomainRepository",
                  "source_path": "src/main/java/com/example/NoDomainRepository.java",
                  "repository_signal": "spring_data_repository_interface_extension",
                  "extends_types": [
                    "org.springframework.data.jpa.repository.JpaRepository"
                  ],
                  "entity_relation_status": "not_detected",
                  "entity_relation": null,
                  "evidence_ids": ["ev:repo"]
                }
              ]
            },
            "configuration": {
              "configuration_classes": {
                "analysis_status": "analyzed",
                "items": []
              },
              "configuration_properties": {
                "analysis_status": "analyzed",
                "items": []
              },
              "bean_methods": {
                "analysis_status": "analyzed",
                "items": []
              }
            },
            "behavior": {
              "transaction_boundaries": {
                "analysis_status": "analyzed",
                "items": []
              },
              "scheduled_methods": {
                "analysis_status": "analyzed",
                "items": []
              },
              "event_listeners": {
                "analysis_status": "analyzed",
                "items": []
              }
            },
            "messaging": {
              "listener_signals": {
                "analysis_status": "analyzed",
                "items": []
              }
            },
            "security": {
              "configuration_warnings": {
                "analysis_status": "analyzed",
                "warning_ids": []
              }
            }
          }
        }
        """;
    String evidenceIndex = """
        {"id":"ev:pom.xml:1-1:build_file:pom.xml","source_type":"build_file","path":"pom.xml","class_name":null,"method_name":null,"symbol_name":"pom.xml","line_start":1,"line_end":1,"excerpt":"<project>","confidence":"high"}
        {"id":"ev:repo","source_type":"code_symbol","path":"src/main/java/com/example/NoDomainRepository.java","class_name":"com.example.NoDomainRepository","method_name":null,"symbol_name":"extends:org.springframework.data.jpa.repository.JpaRepository","line_start":6,"line_end":6,"excerpt":"extends JpaRepository","confidence":"high"}
        """;

    String guide = generator.generate(projectMap, evidenceIndex);

    assertFalse(guide.contains("## Detected JPA Entities"));
    assertFalse(guide.contains("For persistence changes, inspect detected entity evidence"));
    assertFalse(guide.contains("inspect detected entity evidence (no evidence paths recorded)"));
    assertTrue(guide.contains(
        "`com.example.NoDomainRepository`: `entity_relation_status` is `not_detected`"));
  }

  @Test
  void localDocumentationGuideRendersInventoryRefsEvidenceAndUncertainHints() throws Exception {
    String projectMap = """
        {
          "schema_version": "0.8",
          "project": {
            "build": {
              "system": "maven",
              "root_build_file": "pom.xml",
              "evidence_ids": []
            },
            "source_roots": [],
            "test_roots": [],
            "modules": {
              "analysis_status": "analyzed",
              "items": [
                {
                  "module_id": "module:services/orders",
                  "module_path": "services/orders",
                  "pom_path": "services/orders/pom.xml",
                  "support_status": "supported",
                  "declaration_kind": "root_declared_module",
                  "declared_path": "services/orders",
                  "source_roots": [],
                  "test_roots": [],
                  "declaration_evidence_ids": [],
                  "pom_evidence_ids": []
                }
              ]
            }
          },
          "endpoints": [],
          "warnings": {
            "analysis_status": "analyzed",
            "items": []
          },
          "components": {
            "analysis_status": "analyzed",
            "items": []
          },
          "entities": {
            "analysis_status": "analyzed",
            "items": []
          },
          "tests": {
            "analysis_status": "not_detected",
            "items": []
          },
          "documents": {
            "analysis_status": "analyzed",
            "discovery": {
              "scope": "default_local_markdown",
              "path_policy": "repository_relative_in_root",
              "symlink_policy": "skip_symlinks",
              "included_patterns": ["README.md", "docs/**/*.md"],
              "excluded_patterns": [".git/**", ".project-memory/**"]
            },
            "items": [
              {
                "id": "document:README.md",
                "document_kind": "local_markdown",
                "format": "markdown",
                "module_id": null,
                "path": "README.md",
                "title": "Architecture Notes",
                "title_source": "first_heading",
                "discovery_source": "root_readme",
                "headings": [
                  {
                    "id": "document_heading:README.md:heading:Architecture%20Notes:occ:000001",
                    "level": 1,
                    "title": "Architecture Notes",
                    "anchor": "architecture-notes",
                    "line_start": 1,
                    "line_end": 1,
                    "evidence_ids": ["ev:README.md:1-1:document:heading:Architecture%20Notes:decl:000001"]
                  }
                ],
                "chunks": [
                  {
                    "id": "document_chunk:README.md:chunk:000001",
                    "heading_id": "document_heading:README.md:heading:Architecture%20Notes:occ:000001",
                    "line_start": 1,
                    "line_end": 6,
                    "content_status": "not_serialized",
                    "evidence_ids": ["ev:README.md:1-6:document:chunk:000001"]
                  }
                ],
                "evidence_ids": ["ev:README.md:unknown:document:file:README.md"]
              },
              {
                "id": "document:services/orders/README.md",
                "document_kind": "local_markdown",
                "format": "markdown",
                "module_id": "module:services/orders",
                "path": "services/orders/README.md",
                "title": "Orders Module",
                "title_source": "first_heading",
                "discovery_source": "module_readme",
                "headings": [],
                "chunks": [],
                "evidence_ids": ["ev:services/orders/README.md:unknown:document:file:services/orders/README.md"]
              }
            ],
            "reconciliation": {
              "analysis_status": "analyzed",
              "items": [
                {
                  "id": "document_reconciliation:document_only_endpoint_mention:README.md:/ghost:decl:000001",
                  "module_id": null,
                  "signal": "document_only_endpoint_mention",
                  "status": "uncertain_signal",
                  "document_id": "document:README.md",
                  "document_path": "README.md",
                  "document_chunk_id": "document_chunk:README.md:chunk:000001",
                  "source_fact_kind": null,
                  "source_fact_id": null,
                  "subject_kind": "endpoint_like_path",
                  "subject_name": "/ghost",
                  "match_basis": "bounded_endpoint_like_path_token",
                  "confidence": "low",
                  "uncertainty": "document_mention_not_matched_to_source_backed_api_fact",
                  "evidence_ids": ["ev:README.md:5-5:document:mention:/ghost:decl:000001"]
                },
                {
                  "id": "document_reconciliation:source_api_without_document_mention:endpoint%3Acom.example.OrderController%23internal",
                  "module_id": "module:services/orders",
                  "signal": "source_api_without_document_mention",
                  "status": "uncertain_signal",
                  "document_id": null,
                  "document_path": null,
                  "document_chunk_id": null,
                  "source_fact_kind": "spring_mvc_endpoint",
                  "source_fact_id": "endpoint:com.example.OrderController#internal",
                  "subject_kind": "api_path",
                  "subject_name": "/internal",
                  "match_basis": "bounded_source_api_path_token",
                  "confidence": "low",
                  "uncertainty": "source_api_fact_not_matched_to_default_scope_document",
                  "evidence_ids": ["ev:src/main/java/com/example/OrderController.java:8-8:com.example.OrderController#internal:@GetMapping"]
                }
              ]
            }
          }
        }
        """;
    String evidenceIndex = """
        {"id":"ev:README.md:unknown:document:file:README.md","source_type":"document","path":"README.md","class_name":null,"method_name":null,"symbol_name":"file:README.md","line_start":null,"line_end":null,"excerpt":"markdown file detected: README.md","confidence":"high"}
        {"id":"ev:README.md:1-1:document:heading:Architecture%20Notes:decl:000001","source_type":"document","path":"README.md","class_name":null,"method_name":null,"symbol_name":"heading:Architecture Notes","line_start":1,"line_end":1,"excerpt":"# Architecture Notes","confidence":"high"}
        {"id":"ev:README.md:1-6:document:chunk:000001","source_type":"document","path":"README.md","class_name":null,"method_name":null,"symbol_name":"chunk:000001","line_start":1,"line_end":6,"excerpt":"chunk lines 1-6; heading: Architecture Notes","confidence":"high"}
        {"id":"ev:services/orders/README.md:unknown:document:file:services/orders/README.md","source_type":"document","path":"services/orders/README.md","class_name":null,"method_name":null,"symbol_name":"file:services/orders/README.md","line_start":null,"line_end":null,"excerpt":"markdown file detected: services/orders/README.md","confidence":"high"}
        {"id":"ev:README.md:5-5:document:mention:/ghost:decl:000001","source_type":"document","path":"README.md","class_name":null,"method_name":null,"symbol_name":"mention:/ghost","line_start":5,"line_end":5,"excerpt":"mention token: /ghost","confidence":"low"}
        {"id":"ev:src/main/java/com/example/OrderController.java:8-8:com.example.OrderController#internal:@GetMapping","source_type":"annotation","path":"src/main/java/com/example/OrderController.java","class_name":"com.example.OrderController","method_name":"internal","symbol_name":"@GetMapping","line_start":8,"line_end":8,"excerpt":"@GetMapping(\\"/internal\\")","confidence":"high"}
        """;

    String guide = generator.generate(projectMap, evidenceIndex);

    assertTrue(guide.contains("## Local Project Documentation"));
    assertTrue(guide.contains(
        "- Discovery policy: scope `default_local_markdown`, path_policy "
            + "`repository_relative_in_root`, symlink_policy `skip_symlinks`, "
            + "included_patterns `2`, excluded_patterns `2`."));
    assertTrue(guide.contains(
        "- Document inventory: detected 2 accepted default-scope Markdown documents."));
    assertTrue(guide.contains(
        "  - Document: `README.md` (module: `repository-level`, discovery_source: "
            + "`root_readme`, title_source: `first_heading`, headings: `1`, chunks: `1`)."));
    assertTrue(guide.contains(
        "  - Document: `services/orders/README.md` (module: `module:services/orders` "
            + "(path: `services/orders`), discovery_source: `module_readme`, "
            + "title_source: `first_heading`, headings: `0`, chunks: `0`)."));
    assertTrue(guide.contains(
        "      - Heading ref: `document_heading:README.md:heading:Architecture%20Notes:occ:000001` "
            + "level `1`, lines `1`, anchor `architecture-notes`, evidence `README.md:1` "
            + "(`ev:README.md:1-1:document:heading:Architecture%20Notes:decl:000001`)."));
    assertTrue(guide.contains(
        "      - Chunk ref: `document_chunk:README.md:chunk:000001` heading_id "
            + "`document_heading:README.md:heading:Architecture%20Notes:occ:000001`, "
            + "lines `1-6`, content_status `not_serialized`, evidence `README.md:1-6` "
            + "(`ev:README.md:1-6:document:chunk:000001`)."));
    assertTrue(guide.contains(
        "- Reconciliation hints: status `analyzed`; detected 2 low-confidence uncertain inspection hints."));
    assertTrue(guide.contains(
        "- Reconciliation hint: `document_only_endpoint_mention` for `endpoint_like_path` `/ghost` "
            + "(status: `uncertain_signal`, confidence: `low`, uncertainty: "
            + "`document_mention_not_matched_to_source_backed_api_fact`, match_basis: "
            + "`bounded_endpoint_like_path_token`)."));
    assertTrue(guide.contains(
        "    - Source fact: `spring_mvc_endpoint`, `endpoint:com.example.OrderController#internal`"));
    assertTrue(guide.contains(
        "Document-backed signals do not override code-backed facts."));
    assertTrue(guide.contains(
        "prefer code-backed facts for implementation truth."));
    assertFalse(guide.contains("documented prose body"));
    assertFalse(guide.contains("is stale"));
    assertTrue(guide.indexOf("## Local Project Documentation")
        < guide.indexOf("## Known Uncertainty And Limits"));
  }

  @Test
  void localDocumentationGuideRendersNoReconciliationCaseWithoutClaims() throws Exception {
    String projectMap = """
        {
          "schema_version": "0.8",
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
            "analysis_status": "analyzed",
            "items": []
          },
          "components": {
            "analysis_status": "analyzed",
            "items": []
          },
          "entities": {
            "analysis_status": "analyzed",
            "items": []
          },
          "tests": {
            "analysis_status": "not_detected",
            "items": []
          },
          "documents": {
            "analysis_status": "analyzed",
            "discovery": {
              "scope": "default_local_markdown",
              "path_policy": "repository_relative_in_root",
              "symlink_policy": "skip_symlinks",
              "included_patterns": [],
              "excluded_patterns": []
            },
            "items": [
              {
                "id": "document:README.md",
                "document_kind": "local_markdown",
                "format": "markdown",
                "module_id": null,
                "path": "README.md",
                "title": "README",
                "title_source": "filename",
                "discovery_source": "root_readme",
                "headings": [],
                "chunks": [],
                "evidence_ids": ["ev:README.md:unknown:document:file:README.md"]
              }
            ],
            "reconciliation": {
              "analysis_status": "not_detected",
              "items": []
            }
          }
        }
        """;
    String evidenceIndex = """
        {"id":"ev:README.md:unknown:document:file:README.md","source_type":"document","path":"README.md","class_name":null,"method_name":null,"symbol_name":"file:README.md","line_start":null,"line_end":null,"excerpt":"markdown file detected: README.md","confidence":"high"}
        """;

    String guide = generator.generate(projectMap, evidenceIndex);

    assertTrue(guide.contains("## Local Project Documentation"));
    assertTrue(guide.contains("- Reconciliation hints: status `not_detected`; detected none."));
    assertFalse(guide.contains("is stale"));
    assertFalse(guide.contains("complete documentation"));
  }

  @Test
  void localDocumentationGuideStaysQuietWhenNoDocumentsOrReconciliationExist() throws Exception {
    String projectMap = """
        {
          "schema_version": "0.8",
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
            "analysis_status": "analyzed",
            "items": []
          },
          "components": {
            "analysis_status": "analyzed",
            "items": []
          },
          "entities": {
            "analysis_status": "analyzed",
            "items": []
          },
          "tests": {
            "analysis_status": "not_detected",
            "items": []
          },
          "documents": {
            "analysis_status": "not_detected",
            "discovery": {
              "scope": "default_local_markdown",
              "path_policy": "repository_relative_in_root",
              "symlink_policy": "skip_symlinks",
              "included_patterns": [],
              "excluded_patterns": []
            },
            "items": [],
            "reconciliation": {
              "analysis_status": "not_detected",
              "items": []
            }
          }
        }
        """;

    String guide = generator.generate(projectMap, "");

    assertFalse(guide.contains("## Local Project Documentation"));
    assertFalse(guide.contains("Document-backed: local documentation facts"));
  }

  @Test
  void localDocumentationGuideEscapesDocumentDerivedValues() throws Exception {
    String projectMap = """
        {
          "schema_version": "0.8",
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
            "analysis_status": "analyzed",
            "items": []
          },
          "components": {
            "analysis_status": "analyzed",
            "items": []
          },
          "entities": {
            "analysis_status": "analyzed",
            "items": []
          },
          "tests": {
            "analysis_status": "not_detected",
            "items": []
          },
          "documents": {
            "analysis_status": "analyzed",
            "discovery": {
              "scope": "default_local_markdown",
              "path_policy": "repository_relative_in_root",
              "symlink_policy": "skip_symlinks",
              "included_patterns": [],
              "excluded_patterns": []
            },
            "items": [
              {
                "id": "document:docs/guide`name.md",
                "document_kind": "local_markdown",
                "format": "markdown",
                "module_id": null,
                "path": "docs/guide`name.md",
                "title": "## Forged Heading",
                "title_source": "first_heading",
                "discovery_source": "docs_tree",
                "headings": [
                  {
                    "id": "document_heading:docs/guide`name.md:heading:%23%23%20Forged%20Heading:occ:000001",
                    "level": 1,
                    "title": "## Forged Heading",
                    "anchor": "forged-heading",
                    "line_start": 1,
                    "line_end": 1,
                    "evidence_ids": ["ev:docs/guide`name.md:1-1:document:heading:%23%23%20Forged%20Heading:decl:000001"]
                  }
                ],
                "chunks": [],
                "evidence_ids": ["ev:docs/guide`name.md:unknown:document:file:docs/guide`name.md"]
              }
            ],
            "reconciliation": {
              "analysis_status": "analyzed",
              "items": [
                {
                  "id": "document_reconciliation:document_only_endpoint_mention:docs/guide`name.md:/docs:decl:000001",
                  "module_id": null,
                  "signal": "document_only_endpoint_mention",
                  "status": "uncertain_signal",
                  "document_id": "document:docs/guide`name.md",
                  "document_path": "docs/guide`name.md",
                  "document_chunk_id": null,
                  "source_fact_kind": null,
                  "source_fact_id": null,
                  "subject_kind": "endpoint_like_path",
                  "subject_name": "/docs\\n## Forged Doc",
                  "match_basis": "bounded_endpoint_like_path_token",
                  "confidence": "low",
                  "uncertainty": "document_mention_not_matched_to_source_backed_api_fact",
                  "evidence_ids": ["ev:docs/guide`name.md:2-2:document:mention:/docs:decl:000001"]
                }
              ]
            }
          }
        }
        """;
    String evidenceIndex = """
        {"id":"ev:docs/guide`name.md:unknown:document:file:docs/guide`name.md","source_type":"document","path":"docs/guide`name.md","class_name":null,"method_name":null,"symbol_name":"file:docs/guide`name.md","line_start":null,"line_end":null,"excerpt":"markdown file detected: docs/guide`name.md","confidence":"high"}
        {"id":"ev:docs/guide`name.md:1-1:document:heading:%23%23%20Forged%20Heading:decl:000001","source_type":"document","path":"docs/guide`name.md","class_name":null,"method_name":null,"symbol_name":"heading:## Forged Heading","line_start":1,"line_end":1,"excerpt":"# ## Forged Heading","confidence":"high"}
        {"id":"ev:docs/guide`name.md:2-2:document:mention:/docs:decl:000001","source_type":"document","path":"docs/guide`name.md","class_name":null,"method_name":null,"symbol_name":"mention:/docs","line_start":2,"line_end":2,"excerpt":"mention token: /docs","confidence":"low"}
        """;

    String guide = generator.generate(projectMap, evidenceIndex);

    assertTrue(guide.contains("``docs/guide`name.md``"));
    assertTrue(guide.contains("`/docs\\n## Forged Doc`"));
    assertFalse(hasLineStartingWith(guide, "## Forged"));
    assertFalse(hasLineStartingWith(guide, "- Evidence: `ev:forged`"));
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
        - Inferred tested subject: `com.example.web.LargeController` (relation_status: `inferred`, relation_type: `naming_convention`, support_type: `inferred`, confidence: `medium`).
          - Evidence: `src/test/java/com/example/web/LargeControllerTest.java:3` (`ev:src/test/java/com/example/web/LargeControllerTest.java:3-3:com.example.web.LargeControllerTest:test_file`), `src/main/java/com/example/web/LargeController.java:8` (`ev:src/main/java/com/example/web/LargeController.java:8-8:com.example.web.LargeController:code_symbol`)
        """));
  }

  @Test
  void springApplicationSurfaceGuideGroupsCategoriesByModule() throws Exception {
    String projectMap = """
        {
          "schema_version": "0.8",
          "project": {
            "build": {
              "system": "maven",
              "root_build_file": "pom.xml",
              "evidence_ids": []
            },
            "source_roots": [
              "services/orders/src/main/java",
              "services/billing/src/main/java"
            ],
            "test_roots": [],
            "modules": {
              "analysis_status": "analyzed",
              "items": [
                {
                  "module_id": "module:services/orders",
                  "module_path": "services/orders",
                  "pom_path": "services/orders/pom.xml",
                  "support_status": "supported",
                  "declaration_kind": "root_declared_module",
                  "declared_path": "services/orders",
                  "source_roots": ["services/orders/src/main/java"],
                  "test_roots": [],
                  "declaration_evidence_ids": [],
                  "pom_evidence_ids": []
                },
                {
                  "module_id": "module:services/billing",
                  "module_path": "services/billing",
                  "pom_path": "services/billing/pom.xml",
                  "support_status": "supported",
                  "declaration_kind": "root_declared_module",
                  "declared_path": "services/billing",
                  "source_roots": ["services/billing/src/main/java"],
                  "test_roots": [],
                  "declaration_evidence_ids": [],
                  "pom_evidence_ids": []
                }
              ]
            }
          },
          "endpoints": [],
          "warnings": {
            "analysis_status": "analyzed",
            "items": [
              {
                "id": "warning:spring_security:security_configuration_annotation:module:services/billing:com.example.billing.BillingSecurity:annotation:enable_method_security:decl:000001",
                "module_id": "module:services/billing",
                "category": "spring_security",
                "signal": "security_configuration_annotation",
                "message": "Spring Security configuration annotation detected as a source-visible inspection hint and change-risk signal; the analyzer does not evaluate security policy, endpoint protection, authentication, authorization, filter-chain order, vulnerability, or correctness.",
                "source_path": "services/billing/src/main/java/com/example/billing/BillingSecurity.java",
                "evidence_ids": [
                  "ev:services/billing/src/main/java/com/example/billing/BillingSecurity.java:7-7:com.example.billing.BillingSecurity:@EnableMethodSecurity"
                ]
              }
            ]
          },
          "components": {
            "analysis_status": "analyzed",
            "items": []
          },
          "entities": {
            "analysis_status": "analyzed",
            "items": []
          },
          "tests": {
            "analysis_status": "not_detected",
            "items": []
          },
          "spring_application_surface": {
            "analysis_status": "analyzed",
            "repositories": {
              "analysis_status": "analyzed",
              "items": [
                {
                  "id": "spring_repository_stereotype:module:services/orders:com.example.orders.OrderRepositoryAdapter",
                  "module_id": "module:services/orders",
                  "surface_category": "spring_repository_stereotype",
                  "support_type": "extracted",
                  "class_name": "com.example.orders.OrderRepositoryAdapter",
                  "source_path": "services/orders/src/main/java/com/example/orders/OrderRepositoryAdapter.java",
                  "repository_signal": "direct_repository_stereotype",
                  "evidence_ids": [
                    "ev:services/orders/src/main/java/com/example/orders/OrderRepositoryAdapter.java:8-8:com.example.orders.OrderRepositoryAdapter:@Repository"
                  ]
                },
                {
                  "id": "spring_data_repository_interface_signal:module:services/billing:com.example.billing.BillingRepository",
                  "module_id": "module:services/billing",
                  "surface_category": "spring_data_repository_interface_signal",
                  "support_type": "inferred",
                  "class_name": "com.example.billing.BillingRepository",
                  "source_path": "services/billing/src/main/java/com/example/billing/BillingRepository.java",
                  "repository_signal": "spring_data_repository_interface_extension",
                  "extends_types": [
                    "org.springframework.data.repository.CrudRepository"
                  ],
                  "entity_relation_status": "not_analyzed",
                  "evidence_ids": [
                    "ev:services/billing/src/main/java/com/example/billing/BillingRepository.java:6-6:com.example.billing.BillingRepository:com.example.billing.BillingRepository",
                    "ev:services/billing/src/main/java/com/example/billing/BillingRepository.java:6-6:com.example.billing.BillingRepository:extends:org.springframework.data.repository.CrudRepository"
                  ]
                }
              ]
            },
            "configuration": {
              "configuration_classes": {
                "analysis_status": "analyzed",
                "items": [
                  {
                    "id": "spring_configuration_class:module:services/orders:com.example.orders.OrderConfiguration",
                    "module_id": "module:services/orders",
                    "surface_category": "spring_configuration_class",
                    "support_type": "extracted",
                    "class_name": "com.example.orders.OrderConfiguration",
                    "source_path": "services/orders/src/main/java/com/example/orders/OrderConfiguration.java",
                    "configuration_signal": "direct_configuration_class",
                    "evidence_ids": [
                      "ev:services/orders/src/main/java/com/example/orders/OrderConfiguration.java:5-5:com.example.orders.OrderConfiguration:@Configuration"
                    ]
                  }
                ]
              },
              "configuration_properties": {
                "analysis_status": "analyzed",
                "items": [
                  {
                    "id": "spring_configuration_properties_type:module:services/orders:com.example.orders.OrderProperties",
                    "module_id": "module:services/orders",
                    "surface_category": "spring_configuration_properties_type",
                    "support_type": "extracted",
                    "class_name": "com.example.orders.OrderProperties",
                    "source_path": "services/orders/src/main/java/com/example/orders/OrderProperties.java",
                    "configuration_properties_signal": "direct_configuration_properties_type",
                    "binding_status": "not_analyzed",
                    "evidence_ids": [
                      "ev:services/orders/src/main/java/com/example/orders/OrderProperties.java:4-4:com.example.orders.OrderProperties:@ConfigurationProperties"
                    ]
                  }
                ]
              },
              "bean_methods": {
                "analysis_status": "analyzed",
                "items": [
                  {
                    "id": "spring_bean_method:module:services/orders:com.example.orders.OrderConfiguration#orderClock:decl:000001",
                    "module_id": "module:services/orders",
                    "surface_category": "spring_bean_method",
                    "support_type": "extracted",
                    "class_name": "com.example.orders.OrderConfiguration",
                    "method_name": "orderClock",
                    "source_path": "services/orders/src/main/java/com/example/orders/OrderConfiguration.java",
                    "bean_signal": "direct_bean_method",
                    "bean_name_status": "not_analyzed",
                    "evidence_ids": [
                      "ev:services/orders/src/main/java/com/example/orders/OrderConfiguration.java:8-8:com.example.orders.OrderConfiguration#orderClock:@Bean"
                    ]
                  }
                ]
              }
            },
            "behavior": {
              "transaction_boundaries": {
                "analysis_status": "analyzed",
                "items": [
                  {
                    "id": "spring_transaction_boundary:module:services/billing:com.example.billing.BillingService:type",
                    "module_id": "module:services/billing",
                    "surface_category": "spring_transaction_boundary",
                    "support_type": "extracted",
                    "class_name": "com.example.billing.BillingService",
                    "method_name": null,
                    "target_kind": "type",
                    "source_path": "services/billing/src/main/java/com/example/billing/BillingService.java",
                    "annotation_symbol": "@Transactional",
                    "transaction_signal": "direct_transactional_type",
                    "evidence_ids": [
                      "ev:services/billing/src/main/java/com/example/billing/BillingService.java:9-9:com.example.billing.BillingService:@Transactional"
                    ]
                  }
                ]
              },
              "scheduled_methods": {
                "analysis_status": "analyzed",
                "items": []
              },
              "event_listeners": {
                "analysis_status": "analyzed",
                "items": [
                  {
                    "id": "spring_event_listener:module:services/billing:com.example.billing.BillingEvents#onPaid:decl:000001",
                    "module_id": "module:services/billing",
                    "surface_category": "spring_event_listener",
                    "support_type": "extracted",
                    "class_name": "com.example.billing.BillingEvents",
                    "method_name": "onPaid",
                    "target_kind": "method",
                    "source_path": "services/billing/src/main/java/com/example/billing/BillingEvents.java",
                    "annotation_symbol": "@EventListener",
                    "event_listener_signal": "direct_event_listener_method",
                    "evidence_ids": [
                      "ev:services/billing/src/main/java/com/example/billing/BillingEvents.java:11-11:com.example.billing.BillingEvents#onPaid:@EventListener"
                    ]
                  }
                ]
              }
            },
            "messaging": {
              "listener_signals": {
                "analysis_status": "analyzed",
                "items": [
                  {
                    "id": "messaging_listener_signal:module:services/billing:com.example.billing.BillingEvents:#onKafkaEvent:annotation:@KafkaListener:decl:000001",
                    "module_id": "module:services/billing",
                    "surface_category": "messaging_listener_signal",
                    "support_type": "extracted",
                    "class_name": "com.example.billing.BillingEvents",
                    "method_name": "onKafkaEvent",
                    "target_kind": "method",
                    "source_path": "services/billing/src/main/java/com/example/billing/BillingEvents.java",
                    "annotation_symbol": "@KafkaListener",
                    "listener_framework": "kafka",
                    "listener_signal": "direct_kafka_listener_annotation",
                    "evidence_ids": [
                      "ev:services/billing/src/main/java/com/example/billing/BillingEvents.java:15-15:com.example.billing.BillingEvents#onKafkaEvent:@KafkaListener"
                    ]
                  }
                ]
              }
            },
            "security": {
              "configuration_warnings": {
                "analysis_status": "analyzed",
                "warning_ids": [
                  "warning:spring_security:security_configuration_annotation:module:services/billing:com.example.billing.BillingSecurity:annotation:enable_method_security:decl:000001"
                ]
              }
            }
          }
        }
        """;
    String evidenceIndex = """
        {"id":"ev:services/orders/src/main/java/com/example/orders/OrderRepositoryAdapter.java:8-8:com.example.orders.OrderRepositoryAdapter:@Repository","source_type":"annotation","path":"services/orders/src/main/java/com/example/orders/OrderRepositoryAdapter.java","class_name":"com.example.orders.OrderRepositoryAdapter","method_name":null,"symbol_name":"@Repository","line_start":8,"line_end":8,"excerpt":"@Repository","confidence":"high"}
        {"id":"ev:services/billing/src/main/java/com/example/billing/BillingRepository.java:6-6:com.example.billing.BillingRepository:com.example.billing.BillingRepository","source_type":"code_symbol","path":"services/billing/src/main/java/com/example/billing/BillingRepository.java","class_name":"com.example.billing.BillingRepository","method_name":null,"symbol_name":"com.example.billing.BillingRepository","line_start":6,"line_end":6,"excerpt":"interface BillingRepository","confidence":"high"}
        {"id":"ev:services/billing/src/main/java/com/example/billing/BillingRepository.java:6-6:com.example.billing.BillingRepository:extends:org.springframework.data.repository.CrudRepository","source_type":"code_symbol","path":"services/billing/src/main/java/com/example/billing/BillingRepository.java","class_name":"com.example.billing.BillingRepository","method_name":null,"symbol_name":"extends:org.springframework.data.repository.CrudRepository","line_start":6,"line_end":6,"excerpt":"extends CrudRepository","confidence":"high"}
        {"id":"ev:services/orders/src/main/java/com/example/orders/OrderConfiguration.java:5-5:com.example.orders.OrderConfiguration:@Configuration","source_type":"annotation","path":"services/orders/src/main/java/com/example/orders/OrderConfiguration.java","class_name":"com.example.orders.OrderConfiguration","method_name":null,"symbol_name":"@Configuration","line_start":5,"line_end":5,"excerpt":"@Configuration","confidence":"high"}
        {"id":"ev:services/orders/src/main/java/com/example/orders/OrderProperties.java:4-4:com.example.orders.OrderProperties:@ConfigurationProperties","source_type":"annotation","path":"services/orders/src/main/java/com/example/orders/OrderProperties.java","class_name":"com.example.orders.OrderProperties","method_name":null,"symbol_name":"@ConfigurationProperties","line_start":4,"line_end":4,"excerpt":"@ConfigurationProperties","confidence":"high"}
        {"id":"ev:services/orders/src/main/java/com/example/orders/OrderConfiguration.java:8-8:com.example.orders.OrderConfiguration#orderClock:@Bean","source_type":"annotation","path":"services/orders/src/main/java/com/example/orders/OrderConfiguration.java","class_name":"com.example.orders.OrderConfiguration","method_name":"orderClock","symbol_name":"@Bean","line_start":8,"line_end":8,"excerpt":"@Bean","confidence":"high"}
        {"id":"ev:services/billing/src/main/java/com/example/billing/BillingService.java:9-9:com.example.billing.BillingService:@Transactional","source_type":"annotation","path":"services/billing/src/main/java/com/example/billing/BillingService.java","class_name":"com.example.billing.BillingService","method_name":null,"symbol_name":"@Transactional","line_start":9,"line_end":9,"excerpt":"@Transactional","confidence":"high"}
        {"id":"ev:services/billing/src/main/java/com/example/billing/BillingEvents.java:11-11:com.example.billing.BillingEvents#onPaid:@EventListener","source_type":"annotation","path":"services/billing/src/main/java/com/example/billing/BillingEvents.java","class_name":"com.example.billing.BillingEvents","method_name":"onPaid","symbol_name":"@EventListener","line_start":11,"line_end":11,"excerpt":"@EventListener","confidence":"high"}
        {"id":"ev:services/billing/src/main/java/com/example/billing/BillingEvents.java:15-15:com.example.billing.BillingEvents#onKafkaEvent:@KafkaListener","source_type":"annotation","path":"services/billing/src/main/java/com/example/billing/BillingEvents.java","class_name":"com.example.billing.BillingEvents","method_name":"onKafkaEvent","symbol_name":"@KafkaListener","line_start":15,"line_end":15,"excerpt":"@KafkaListener","confidence":"high"}
        {"id":"ev:services/billing/src/main/java/com/example/billing/BillingSecurity.java:7-7:com.example.billing.BillingSecurity:@EnableMethodSecurity","source_type":"annotation","path":"services/billing/src/main/java/com/example/billing/BillingSecurity.java","class_name":"com.example.billing.BillingSecurity","method_name":null,"symbol_name":"@EnableMethodSecurity","line_start":7,"line_end":7,"excerpt":"@EnableMethodSecurity","confidence":"high"}
        """;

    String guide = generator.generate(projectMap, evidenceIndex);

    assertEquals(
        Files.readString(goldenRoot("spring-application-surface-guide")
            .resolve("spring-application-surface.md")),
        markdownSection(
            guide,
            "## Spring Application Surface",
            "## Detected Spring MVC Endpoints"));
  }

  @Test
  void testGuideRendersSpringSliceAndMockSignalsWithoutRuntimeClaims() throws Exception {
    String projectMap = """
        {
          "schema_version": "0.8",
          "project": {
            "build": {
              "system": "maven",
              "root_build_file": "pom.xml",
              "evidence_ids": []
            },
            "source_roots": [],
            "test_roots": ["src/test/java"]
          },
          "endpoints": [],
          "warnings": {
            "analysis_status": "analyzed",
            "items": []
          },
          "components": {
            "analysis_status": "analyzed",
            "items": []
          },
          "entities": {
            "analysis_status": "analyzed",
            "items": []
          },
          "tests": {
            "analysis_status": "analyzed",
            "items": [
              {
                "id": "test:com.example.web.OrderControllerSlice",
                "module_id": "module:.",
                "class_name": "com.example.web.OrderControllerSlice",
                "source_path": "src/test/java/com/example/web/OrderControllerSlice.java",
                "framework_signals": [],
                "spring_test_slices": [
                  {
                    "annotation": "@WebMvcTest",
                    "slice_kind": "web_mvc_test",
                    "signal_kind": "spring_test_slice",
                    "evidence_ids": ["ev:webmvc"]
                  }
                ],
                "mock_signals": [
                  {
                    "annotation": "@MockBean",
                    "mock_signal": "spring_boot_mockbean_annotation",
                    "signal_kind": "mock_annotation",
                    "target_kind": "field",
                    "target_name": "orderService",
                    "evidence_ids": ["ev:mockbean"]
                  }
                ],
                "methods": [],
                "tested_subjects": [],
                "evidence_ids": ["ev:test-file"]
              }
            ]
          }
        }
        """;
    String evidenceIndex = """
        {"id":"ev:test-file","source_type":"test_file","path":"src/test/java/com/example/web/OrderControllerSlice.java","class_name":"com.example.web.OrderControllerSlice","method_name":null,"symbol_name":"com.example.web.OrderControllerSlice","line_start":6,"line_end":6,"excerpt":"class OrderControllerSlice","confidence":"high"}
        {"id":"ev:webmvc","source_type":"annotation","path":"src/test/java/com/example/web/OrderControllerSlice.java","class_name":"com.example.web.OrderControllerSlice","method_name":null,"symbol_name":"@WebMvcTest","line_start":5,"line_end":5,"excerpt":"@WebMvcTest(OrderController.class)","confidence":"high"}
        {"id":"ev:mockbean","source_type":"annotation","path":"src/test/java/com/example/web/OrderControllerSlice.java","class_name":"com.example.web.OrderControllerSlice","method_name":null,"symbol_name":"@MockBean","line_start":7,"line_end":7,"excerpt":"@MockBean","confidence":"high"}
        """;

    String guide = generator.generate(projectMap, evidenceIndex);

    assertTrue(guide.contains(
        "- Spring test slice signal: Detected `@WebMvcTest` "
            + "(slice_kind: `web_mvc_test`, signal_kind: `spring_test_slice`)"));
    assertTrue(guide.contains(
        "- Mock annotation signal: Detected `@MockBean` on `field` `orderService` "
            + "(mock_signal: `spring_boot_mockbean_annotation`, signal_kind: `mock_annotation`)"));
    assertTrue(guide.contains(
        "Runtime Spring context behavior, bean graph contents, MockMvc setup, database access, "
            + "Mockito behavior, and slice correctness are not claimed."));
    assertTrue(guide.contains(
        "do not treat Spring test slice or mock annotations as execution or runtime behavior proof"));
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
          - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:52` (`ev:src/main/java/com/example/domain/ProjectEntities.java:52-52:com.example.domain.ProjectOrder:@Entity`)
        - Table: Detected `orders`
          - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:53` (`ev:src/main/java/com/example/domain/ProjectEntities.java:53-53:com.example.domain.ProjectOrder:@Table`)
        """));
    assertTrue(guide.contains("""
        - Identifier field: Detected `id` (`Long`) declared by `com.example.domain.ProjectBaseEntity` with source_kind `mapped_superclass` identifier_kind `simple_id`
          - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:30` (`ev:src/main/java/com/example/domain/ProjectEntities.java:30-30:com.example.domain.ProjectBaseEntity:@Id:field:id`), `src/main/java/com/example/domain/ProjectEntities.java:28` (`ev:src/main/java/com/example/domain/ProjectEntities.java:28-28:com.example.domain.ProjectBaseEntity:@MappedSuperclass`)
        """));
    assertFalse(guide.contains("""
        - Table: Detected `orders`
          - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:52` (`ev:src/main/java/com/example/domain/ProjectEntities.java:52-52:com.example.domain.ProjectOrder:@Entity`)
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

  private String markdownSection(String markdown, String startHeading, String endHeading) {
    int start = markdown.indexOf(startHeading);
    int end = markdown.indexOf(endHeading, start);
    assertTrue(start >= 0, "start heading must exist");
    assertTrue(end > start, "end heading must exist after start heading");
    String section = markdown.substring(start, end);
    if (section.endsWith("\n\n")) {
      return section.substring(0, section.length() - 1);
    }
    return section;
  }

  private boolean hasLineStartingWith(String markdown, String prefix) {
    return markdown.lines().anyMatch(line -> line.startsWith(prefix));
  }

  private Path goldenRoot(String fixtureName) throws Exception {
    return Path.of(Objects.requireNonNull(
        getClass().getResource("/golden/" + fixtureName)).toURI());
  }
}
