# Agent Guide

Generated deterministically from `project-map.json` and `evidence-index.jsonl`. The guide generator does not re-analyze source files.

## Read This First

- Open `artifact-set.json` before this guide and respect its artifact authority labels.
- Use this guide as deterministic orientation only. It is not evidence and does not re-analyze source files.
- For large or unknown outputs, prefer `query <path> agent-context`, targeted query commands, focused `project-map.json` selection, exact `evidence-index.jsonl` lookup, and source readback instead of reading every row.
- Size note: this guide is `small-guide` (about `24 KiB`, `249` rendered lines); known generator inputs are `project-map.json` `18 KiB` and `evidence-index.jsonl` `4 KiB`.

## Trust And Verification Legend

Trust and verification legend:
- Use `evidence-index.jsonl` as the authoritative source-backed evidence ledger; verify important claims against its exact records and the repository source locations they cite.
- Generated project facts: `project-map.json` facts; verify important use through their evidence IDs.
- Deterministic presentation: this guide, `endpoints.md`, and query stdout help with orientation; they are not evidence.
- Navigation, provenance, or execution metadata: `artifact-set.json`, `project-graph.json`, `source-registry.json`, profiles, LLM/provider AI output, cache, workspace, adapter output, release metadata, security reports, and downstream-agent output are non-evidence unless a later public contract explicitly changes that.
- Before code changes, review findings, public/security/release wording, or architecture decisions, resolve exact evidence IDs and read the cited source.

## Practical Inspection Order For Coding Agents

1. Start with detected build, module, and layout facts in `pom.xml`.
2. For HTTP behavior, inspect detected endpoint and hidden-surface warning evidence in `src/main/java/com/example/web/OrderController.java`.
3. For Spring application surface changes, inspect Spring application surface and component evidence in `src/main/java/com/example/web/OrderController.java` and avoid assuming runtime repository registration, entity ownership, injection graphs, transaction behavior, scheduler registration, event delivery, or messaging topology.
4. For tests, inspect detected test files and tested-subject relation/status evidence (no evidence paths recorded); do not treat inferred or statused subjects as coverage proof.
5. For quality and change-risk planning, inspect quality signal evidence in `src/main/java/com/example/web/OrderController.java` and treat `no_obvious_test`, warning-oriented, and uncertain statuses as planning hints only, not coverage, runtime, correctness, vulnerability, or business-priority claims.
6. For local documentation context, inspect accepted document evidence and reconciliation hints in `README.md`, `src/main/java/com/example/web/OrderController.java` and treat document paths, heading refs, chunk refs, and reconciliation rows as navigation aids only; prefer code-backed facts for implementation truth.

## Project Memory Overview

- Build/layout: build system `maven`, modules `1`, source roots `1`, test roots `0`.
- Source-backed fact surfaces: endpoints `2`, direct Spring components `1`, Spring application surface rows `0`, entities `0`, embeddables `0`, tests `0`.
- Planning/navigation surfaces: warnings `0`, quality/change-risk hints `2`, local documents `1`, document reconciliation hints `3`.
- Evidence records: `12` records in `evidence-index.jsonl`; this overview is presentation only.
- Size band: `small-guide`; large detailed sections should be selected by task and verified through exact evidence IDs.

## Known Uncertainty Snapshot

- Warnings: `0` warning rows; warning evidence and messages stay in the detailed limits section.
- Inferred or statused rows: `2` rows; keep `inferred`, `ambiguous`, `not_detected`, `unsupported`, and similar labels attached to any use.
- Explicit uncertainty labels: `5` values; preserve those caveats with the cited evidence.
- Not analyzed/out-of-scope status markers: `6`; runtime behavior, generated-source contents, test execution/coverage, source/spec agreement, connectors, and LLM summaries remain outside source-backed evidence unless a later contract says otherwise.

## Not Represented In This Scan

- No represented rows for: `domain/data model facts`, `test classes`, `generated-source root metadata`. This means the current deterministic scan emitted no rows for those surfaces; it does not prove the runtime behavior is absent outside the supported analyzer scope.

## Detected Project Layout

- Build system: Detected `maven`
- Root build file: Detected `pom.xml`
  - Evidence: `pom.xml:1` (`ev:pom.xml:1-1:build_file:pom.xml`)
- Source roots: Detected `src/main/java`
  - Evidence: recorded in `project-map.json`; no separate source-root evidence IDs are emitted.
- Test roots: Not analyzed; no supported test roots were recorded.
- Modules analysis status: `analyzed`
- Module: Detected `module:.` (path: `.`)
  - POM path: Detected `pom.xml`
  - Support status: `supported`
  - Declaration kind: `scan_root`
  - Declared path: `.`
  - Source roots: Detected `src/main/java`
  - Source roots evidence: recorded in `project-map.json`; no separate production root evidence IDs are emitted.
  - Test roots: Not analyzed; no supported test roots were recorded for this module.
  - Declaration evidence: none recorded.
  - POM evidence: `pom.xml:1` (`ev:pom.xml:1-1:build_file:pom.xml`)

## Build And Configuration Orientation

### Module `module:.` (path: `.`)

- Build/config analysis status: `analyzed`
- Source-visible Maven metadata: group_id `com.example` (value_kind: `literal`), artifact_id `document-reconciliation-golden` (value_kind: `literal`), version `1.0.0` (value_kind: `literal`), packaging `value:not_declared` (value_kind: `not_declared`).
  - Evidence: `pom.xml:3` (`ev:pom.xml:3-3:build_file:maven:project:groupId`), `pom.xml:4` (`ev:pom.xml:4-4:build_file:maven:project:artifactId`), `pom.xml:5` (`ev:pom.xml:5-5:build_file:maven:project:version`)
- Source-visible direct dependencies: Detected none.
- Source-visible dependency-management declarations: Detected none.
- Source-visible direct plugins: Detected none.
- Source-visible plugin-management declarations: Detected none.
- Resource roots: Not analyzed; status `not_detected`.
- Config files: Not analyzed; status `not_detected`.
- Spring Boot application signals: Detected none.
- Module warnings: Detected none.

## API Surface Interpretation

- API surface analysis status: `analyzed`
- Source-visible Spring MVC endpoint facts are code-backed local source observations from `endpoints[]`; they do not prove complete runtime handler mappings.
- Source-visible interface-declared endpoint facts are code-backed only when the interface mapping and unique concrete binding are both source-visible.
- Declared OpenAPI operations are spec-backed contract facts with `implementation_status: "not_analyzed"`; they are not implemented endpoint facts.
- Generated-source API signals, repository-rest warnings, and hidden HTTP warnings are inspection hints, not endpoint or operation facts.
- LLM output, generated Markdown, release notes, and chat text are never evidence for API surface facts or relations.
- Source-visible direct Spring MVC endpoint IDs: status `analyzed`; detected 2 IDs `endpoint:com.example.web.OrderController#internal`, `endpoint:com.example.web.OrderController#orders`.
- Source-visible interface-declared Spring MVC endpoint IDs: status `analyzed`; detected none.
- OpenAPI/Swagger spec files: status `analyzed`; detected none.
- OpenAPI/Swagger operations: status `not_detected`; detected none.
- Generated-source API warning IDs: status `analyzed`; detected none.
- Repository-rest warning IDs: status `analyzed`; detected none.
- Hidden HTTP warning IDs: status `analyzed`; detected none.

## Detected Spring MVC Endpoints

- Endpoint summary: detected 2 source-visible Spring MVC endpoint facts.

### `GET /internal`

- Module: Detected `module:.` (path: `.`)
- Controller: Detected `com.example.web.OrderController`
- Handler: Detected `internal`
- Mapping source: Detected `direct_handler_method` from `com.example.web.OrderController#internal` with binding `direct`
- HTTP methods: Detected `GET`
- Paths: Detected `/internal`
- Request parameters: Detected none.
- Request body: Detected none.
- Response: Detected `String`
  - Evidence: `src/main/java/com/example/web/OrderController.java:3` (`ev:src/main/java/com/example/web/OrderController.java:3-3:com.example.web.OrderController:@RestController`), `src/main/java/com/example/web/OrderController.java:10` (`ev:src/main/java/com/example/web/OrderController.java:10-10:com.example.web.OrderController#internal:@GetMapping`)

### `GET /orders`

- Module: Detected `module:.` (path: `.`)
- Controller: Detected `com.example.web.OrderController`
- Handler: Detected `orders`
- Mapping source: Detected `direct_handler_method` from `com.example.web.OrderController#orders` with binding `direct`
- HTTP methods: Detected `GET`
- Paths: Detected `/orders`
- Request parameters: Detected none.
- Request body: Detected none.
- Response: Detected `String`
  - Evidence: `src/main/java/com/example/web/OrderController.java:3` (`ev:src/main/java/com/example/web/OrderController.java:3-3:com.example.web.OrderController:@RestController`), `src/main/java/com/example/web/OrderController.java:5` (`ev:src/main/java/com/example/web/OrderController.java:5-5:com.example.web.OrderController#orders:@GetMapping`)

## Spring Application Surface

- Spring application surface analysis status: `analyzed`
- Repository stereotype entries are direct `@Repository` annotation observations; they do not prove runtime bean registration or entity ownership.
- Spring Data repository interface entries are inferred source-visible extension signals; repository/entity relation rows, when present, are inferred generic links. They do not prove runtime repositories, query method behavior, database access, or runtime repository/entity verification.
- Configuration classes, configuration-properties types, and `@Bean` methods are source-visible Spring configuration signals; they do not prove runtime bean graphs, binding success, config values, bean scopes, lifecycle, proxy behavior, or dependency graphs.
- Transaction, scheduled, event listener, and messaging listener entries are source-visible operational change-surface signals; they do not prove runtime transaction behavior, scheduler registration, event delivery, message destinations, or broker topology.
- Spring Security configuration warnings are inspection hints and change-risk signals; they do not prove security policy, endpoint protection, authentication behavior, authorization behavior, vulnerability, or correctness.
- Subsection statuses: repositories `analyzed`, configuration classes `analyzed`, configuration properties `analyzed`, bean methods `analyzed`, transaction boundaries `analyzed`, scheduled methods `analyzed`, event listeners `analyzed`, messaging listeners `analyzed`, security warnings `analyzed`.
- Spring application surface facts: detected none for supported modules.

## Detected Spring Components

- Analysis status: `analyzed`
- Component summary: detected 1 direct Spring stereotype component.

### `com.example.web.OrderController`

- Module: Detected `module:.` (path: `.`)
- Stereotypes: Detected `@RestController`
  - Evidence: `src/main/java/com/example/web/OrderController.java:3` (`ev:src/main/java/com/example/web/OrderController.java:3-3:com.example.web.OrderController:@RestController`)

## Detected Tests

- Analysis status: `not_detected`
- Not analyzed: no supported test root was detected.

## Quality And Change-Risk Signals

- Quality analysis status: `analyzed`
- Test-gap signals are absence-sensitive planning hints from the bounded test inventory and inferred tested-subject relations. They do not prove coverage gaps, execution behavior, assertion behavior, CI status, or complete subject mapping.
- Change-risk signals are warning-oriented or uncertain planning hints from existing deterministic facts. They do not prove production impact, vulnerability, business priority, correctness, runtime behavior, or test priority.

### Test-Gap Signals

- Analysis status: `analyzed`
- Test-gap signal: `endpoint_without_obvious_test` for `spring_mvc_endpoint` `com.example.web.OrderController#internal` (status: `no_obvious_test`, inference_basis: `bounded_test_inventory_not_available`, confidence: `low`, uncertainty: `supported_test_roots_not_detected`). No coverage, execution, assertion, CI, or runtime relation is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `endpoint:com.example.web.OrderController#internal`
  - Subject source hint: class `com.example.web.OrderController`, member `internal`
  - Evidence: `src/main/java/com/example/web/OrderController.java:3` (`ev:src/main/java/com/example/web/OrderController.java:3-3:com.example.web.OrderController:@RestController`), `src/main/java/com/example/web/OrderController.java:10` (`ev:src/main/java/com/example/web/OrderController.java:10-10:com.example.web.OrderController#internal:@GetMapping`)
- Test-gap signal: `endpoint_without_obvious_test` for `spring_mvc_endpoint` `com.example.web.OrderController#orders` (status: `no_obvious_test`, inference_basis: `bounded_test_inventory_not_available`, confidence: `low`, uncertainty: `supported_test_roots_not_detected`). No coverage, execution, assertion, CI, or runtime relation is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `endpoint:com.example.web.OrderController#orders`
  - Subject source hint: class `com.example.web.OrderController`, member `orders`
  - Evidence: `src/main/java/com/example/web/OrderController.java:3` (`ev:src/main/java/com/example/web/OrderController.java:3-3:com.example.web.OrderController:@RestController`), `src/main/java/com/example/web/OrderController.java:5` (`ev:src/main/java/com/example/web/OrderController.java:5-5:com.example.web.OrderController#orders:@GetMapping`)

### Change-Risk Signals

- Analysis status: `not_detected`
- Change-risk signals: none recorded.


## Local Project Documentation

- Documents analysis status: `analyzed`
- Local documentation entries are default-scope Markdown navigation facts only; document bodies, paragraphs, arbitrary lists, tables, code blocks, and prose summaries are not rendered.
- Reconciliation rows are uncertain inspection hints only; they do not prove stale documentation, coverage, completeness, correctness, implementation, or source/document agreement.
- Discovery policy: scope `default_local_markdown`, path_policy `repository_relative_in_root`, symlink_policy `skip_symlinks`, included_patterns `7`, excluded_patterns `13`.
- Document inventory: detected 1 accepted default-scope Markdown document.
  - Document: `README.md` (module: `repository-level`, discovery_source: `root_readme`, title_source: `first_heading`, headings: `1`, chunks: `1`).
    - Evidence: `README.md` (`ev:README.md:unknown:document:file:README.md`)
    - Heading refs: detected 1 bounded ATX heading reference.
      - Heading ref: `document_heading:README.md:heading:API%20docs:occ:000001` level `1`, lines `1`, anchor `api-docs`, evidence `README.md:1` (`ev:README.md:1-1:document:heading:API%20docs:decl:000001`).
    - Chunk refs: detected 1 bounded chunk reference; chunk bodies are not serialized.
      - Chunk ref: `document_chunk:README.md:chunk:000001` heading_id `document_heading:README.md:heading:API%20docs:occ:000001`, lines `1-4`, content_status `not_serialized`, evidence `README.md:1-4` (`ev:README.md:1-4:document:chunk:000001`).
- Reconciliation hints: status `analyzed`; detected 3 low-confidence uncertain inspection hints.
  - Reconciliation hint: `document_only_endpoint_mention` for `endpoint_like_path` `/ghost` (status: `uncertain_signal`, confidence: `low`, uncertainty: `document_mention_not_matched_to_source_backed_api_fact`, match_basis: `bounded_endpoint_like_path_token`).
    - Module: `repository-level-or-not-recorded`
    - Document: `document:README.md`, `README.md`, `chunk=document_chunk:README.md:chunk:000001`
    - Source fact: none recorded for this hint.
    - Evidence: `README.md:3` (`ev:README.md:3-3:document:mention:/ghost:decl:000002`)
  - Reconciliation hint: `document_only_module_reference` for `module_reference` `billing-service` (status: `uncertain_signal`, confidence: `low`, uncertainty: `document_module_reference_not_matched_to_module_fact`, match_basis: `bounded_module_name_token`).
    - Module: `repository-level-or-not-recorded`
    - Document: `document:README.md`, `README.md`, `chunk=document_chunk:README.md:chunk:000001`
    - Source fact: none recorded for this hint.
    - Evidence: `README.md:4` (`ev:README.md:4-4:document:mention:billing-service:decl:000003`)
  - Reconciliation hint: `source_api_without_document_mention` for `api_path` `/internal` (status: `uncertain_signal`, confidence: `low`, uncertainty: `source_api_fact_not_matched_to_default_scope_document`, match_basis: `bounded_source_api_path_token`).
    - Module: `module:.` (path: `.`)
    - Document: none recorded for this hint.
    - Source fact: `spring_mvc_endpoint`, `endpoint:com.example.web.OrderController#internal`
    - Evidence: `src/main/java/com/example/web/OrderController.java:3` (`ev:src/main/java/com/example/web/OrderController.java:3-3:com.example.web.OrderController:@RestController`), `src/main/java/com/example/web/OrderController.java:10` (`ev:src/main/java/com/example/web/OrderController.java:10-10:com.example.web.OrderController#internal:@GetMapping`)

## Generated Source And Codegen Orientation

- Generated-source metadata status: `analyzed`.
- Policy: content scan `disabled`, default `false`, configurable `false`, content_status `not_scanned`.
- Generated-source roots are metadata only; they are not production `source_roots`, test roots, endpoint facts, API operation facts, or generated API facts.
- Generated-source roots: status `analyzed`; detected none.
- Generator/codegen signals: status `analyzed`; warning IDs none recorded; Maven plugin IDs none recorded.

## Optional Surface Orientation

- Use `artifact-set.json` to confirm whether adapter provenance, agent profiles, AI presentation, cache metadata, or workspace output belong to the generated artifact set.
- Treat optional surfaces as provenance, navigation, execution metadata, or presentation. They are not `evidence-index.jsonl` evidence and must not create Java/Spring project facts.

## Detailed Known Uncertainty And Limits

- Not scanned: Generated-source roots are metadata-only path/codegen observations with `content_status: "not_scanned"`; generated source contents, generator execution, generated API reconstruction, runtime freshness checks, dependency/task resolution, and custom Gradle generated-source graph reconstruction are not performed.
- Not analyzed: Spring runtime behavior such as component scanning, dependency injection graphs, bean lifecycle, scopes, and conditional configuration is not represented by `components.items`.
- Uncertain: JPA relationship targets preserve `target_resolution: declared_type_only` and `uncertainty: target_type_not_resolved`; no symbol solving or ORM runtime behavior is claimed.
- Source-visible: JPA relationship metadata such as `mappedBy`, `@JoinColumn`, `@JoinTable`, `optional`, `fetch`, `cascade`, and `orphanRemoval` is reported only when direct annotation attributes are supported; foreign keys, join tables, ownership correctness, fetch behavior, cascade behavior, and database constraints are not claimed.
- Not analyzed: JPA mapped-superclass identifier support is limited to conservative source-visible mapped-superclass chains; unresolved, ambiguous, cyclic, or non-source-visible branches are skipped.
- Partial: JPA embedded and composite identifier support is limited to direct source-visible `@Embeddable`, `@Embedded`, `@EmbeddedId`, and `@IdClass` signals. Embedded targets are linked only when a unique local `@Embeddable` can be matched; `@IdClass` field matching and composite-key semantics are not analyzed.
- Inferred/statused: tested-subject rows are conservative source-visible hints from supported naming, import, field-type, and Spring test slice class-literal signals. Non-inferred statuses such as `not_detected`, `ambiguous`, and `unsupported` do not claim coverage or execution. Test method inventory records source-visible JUnit annotation structure only. Test execution, CI results, coverage, assertion behavior, call graphs, and complete subject mapping are not analyzed.
- Planning hints: quality test-gap and change-risk signals are conservative derived hints from existing deterministic facts and inferred tested-subject relations. They do not claim coverage, test execution, assertion behavior, runtime behavior, production impact, vulnerability, correctness, business priority, or complete subject mapping.
- Not analyzed: connectors, LLM summaries, repository chat, generic RAG, Gradle/Kotlin support, Maven profiles, effective POM reconstruction, dependency graphs, and recursive nested Maven modules are outside this guide.
- Not analyzed: generated sources, generated API reconstruction, classpath-only interfaces, and ambiguous interface endpoint bindings are outside the source-visible interface endpoint support.
- Not analyzed: OpenAPI operation facts are spec-backed declared operations only; runtime implementation matching, source/spec agreement, generated source contents, and client SDK reconstruction are not claimed.
- Not analyzed: v0.3 build/config facts are direct local source observations only. Maven execution, effective POM reconstruction, profile activation, remote dependency resolution, config value interpretation, secret extraction, and default generated-source scanning are not performed.
- Not analyzed: Spring Boot application signals do not prove executable packaging, active profiles, runtime auto-configuration, bean graphs, component scanning results, deployment behavior, or actual process entrypoint behavior.
- Not analyzed: Spring Data repository interface signals do not prove runtime repository registration, query method behavior, database access, or runtime repository/entity verification. Repository/entity links, when present, are bounded inferred Spring Data generic relations with explicit `entity_relation_status` values.
- Not analyzed: JPA field metadata is limited to supported direct field-level source-visible annotations. It is not a complete persistent-property inventory, does not support getter/property access in this slice, and does not fill missing annotation attributes from JPA provider defaults.
- Not analyzed: v0.5 transaction, scheduling, event listener, and messaging listener facts are annotation-presence change-surface signals only. Transaction propagation, scheduler registration, event delivery, message destinations, broker topology, consumer groups, and delivery semantics are not claimed.
- Not analyzed: Security policy, endpoint protection state, authentication behavior, authorization behavior, filter-chain ordering, vulnerabilities, and correctness are not claimed. v0.5 Spring Security configuration warnings are bounded source-visible inspection hints only.
- Document-backed: local documentation facts come from default-scope Markdown inventory, heading/chunk navigation references, and uncertain reconciliation hints only. Hidden, private, generated, dependency, maintainer, and `.project-memory/` paths are excluded by default; symlinks are not followed by default; external docs, PDFs, Word documents, connectors, generic RAG, repository chat, and LLM summaries are outside the core analyzer. Document-backed signals do not override code-backed facts.
- Uncertain: no entity facts were recorded, so persistence mappings may be absent or outside the currently supported analyzer scope.
- Not analyzed: supported Maven test roots were not detected.
