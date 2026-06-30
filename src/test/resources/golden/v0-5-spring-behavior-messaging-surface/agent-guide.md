# Agent Guide

Generated deterministically from `project-map.json` and `evidence-index.jsonl`. The guide generator does not re-analyze source files.

## Read This First

- Open `artifact-set.json` before this guide and respect its artifact authority labels.
- Use this guide as deterministic orientation only. It is not evidence and does not re-analyze source files.
- For large or unknown outputs, prefer `query <path> agent-context`, targeted query commands, focused `project-map.json` selection, exact `evidence-index.jsonl` lookup, and source readback instead of reading every row.
- Size note: this guide is `small-guide` (about `25 KiB`, `233` rendered lines); known generator inputs are `project-map.json` `23 KiB` and `evidence-index.jsonl` `5 KiB`.

## Trust And Verification Legend

Trust and verification legend:
- Use `evidence-index.jsonl` as the authoritative source-backed evidence ledger; verify important claims against its exact records and the repository source locations they cite.
- Generated project facts: `project-map.json` facts; verify important use through their evidence IDs.
- Deterministic presentation: this guide, `endpoints.md`, and query stdout help with orientation; they are not evidence.
- Navigation, provenance, or execution metadata: `artifact-set.json`, `project-graph.json`, `source-registry.json`, profiles, LLM/provider AI output, cache, workspace, adapter output, release metadata, security reports, and downstream-agent output are non-evidence unless a later public contract explicitly changes that.
- Before code changes, review findings, public/security/release wording, or architecture decisions, resolve exact evidence IDs and read the cited source.

## Practical Inspection Order For Coding Agents

1. Start with detected build, module, and layout facts in `pom.xml`.
2. For HTTP behavior, inspect detected endpoint and hidden-surface warning evidence (no evidence paths recorded).
3. For Spring application surface changes, inspect Spring application surface and component evidence in `src/main/java/com/example/behavior/BehaviorMessagingSurface.java` and avoid assuming runtime repository registration, entity ownership, injection graphs, transaction behavior, scheduler registration, event delivery, or messaging topology.
4. For tests, inspect detected test files and tested-subject relation/status evidence (no evidence paths recorded); do not treat inferred or statused subjects as coverage proof.
5. For quality and change-risk planning, inspect quality signal evidence in `src/main/java/com/example/behavior/BehaviorMessagingSurface.java` and treat `no_obvious_test`, warning-oriented, and uncertain statuses as planning hints only, not coverage, runtime, correctness, vulnerability, or business-priority claims.

## Project Memory Overview

- Build/layout: build system `maven`, modules `1`, source roots `1`, test roots `0`.
- Source-backed fact surfaces: endpoints `0`, direct Spring components `0`, Spring application surface rows `7`, entities `0`, embeddables `0`, tests `0`.
- Planning/navigation surfaces: warnings `0`, quality/change-risk hints `7`, local documents `0`, document reconciliation hints `0`.
- Evidence records: `11` records in `evidence-index.jsonl`; this overview is presentation only.
- Size band: `small-guide`; large detailed sections should be selected by task and verified through exact evidence IDs.

## Known Uncertainty Snapshot

- Warnings: `0` warning rows; warning evidence and messages stay in the detailed limits section.
- Inferred or statused rows: `7` rows; keep `inferred`, `ambiguous`, `not_detected`, `unsupported`, and similar labels attached to any use.
- Explicit uncertainty labels: `7` values; preserve those caveats with the cited evidence.
- Not analyzed/out-of-scope status markers: `8`; runtime behavior, generated-source contents, test execution/coverage, source/spec agreement, connectors, and LLM summaries remain outside source-backed evidence unless a later contract says otherwise.

## Not Represented In This Scan

- No represented rows for: `Spring MVC endpoints`, `direct Spring components`, `domain/data model facts`, `test classes`, `local project documentation`, `generated-source root metadata`. This means the current deterministic scan emitted no rows for those surfaces; it does not prove the runtime behavior is absent outside the supported analyzer scope.

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
- Source-visible Maven metadata: group_id `com.example` (value_kind: `literal`), artifact_id `v0-5-spring-behavior-messaging-surface` (value_kind: `literal`), version `1.0.0` (value_kind: `literal`), packaging `value:not_declared` (value_kind: `not_declared`).
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
- Source-visible direct Spring MVC endpoint IDs: status `analyzed`; detected none.
- Source-visible interface-declared Spring MVC endpoint IDs: status `analyzed`; detected none.
- OpenAPI/Swagger spec files: status `analyzed`; detected none.
- OpenAPI/Swagger operations: status `not_detected`; detected none.
- Generated-source API warning IDs: status `analyzed`; detected none.
- Repository-rest warning IDs: status `analyzed`; detected none.
- Hidden HTTP warning IDs: status `analyzed`; detected none.

## Detected Spring MVC Endpoints

- Detected: no Spring MVC endpoints recorded in `project-map.json`.

## Spring Application Surface

- Spring application surface analysis status: `analyzed`
- Repository stereotype entries are direct `@Repository` annotation observations; they do not prove runtime bean registration or entity ownership.
- Spring Data repository interface entries are inferred source-visible extension signals; repository/entity relation rows, when present, are inferred generic links. They do not prove runtime repositories, query method behavior, database access, or runtime repository/entity verification.
- Configuration classes, configuration-properties types, and `@Bean` methods are source-visible Spring configuration signals; they do not prove runtime bean graphs, binding success, config values, bean scopes, lifecycle, proxy behavior, or dependency graphs.
- Transaction, scheduled, event listener, and messaging listener entries are source-visible operational change-surface signals; they do not prove runtime transaction behavior, scheduler registration, event delivery, message destinations, or broker topology.
- Spring Security configuration warnings are inspection hints and change-risk signals; they do not prove security policy, endpoint protection, authentication behavior, authorization behavior, vulnerability, or correctness.
- Subsection statuses: repositories `analyzed`, configuration classes `analyzed`, configuration properties `analyzed`, bean methods `analyzed`, transaction boundaries `analyzed`, scheduled methods `analyzed`, event listeners `analyzed`, messaging listeners `analyzed`, security warnings `analyzed`.

### Module `module:.` (path: `.`)

- Extracted facts: detected 7 source-visible facts.
  - `spring_transaction_boundary`: `com.example.behavior.BehaviorMessagingSurface` (support_type: `extracted`, transaction_signal: `direct_transactional_type`, target_kind: `type`, annotation_symbol: `@Transactional`).
    - Source: `src/main/java/com/example/behavior/BehaviorMessagingSurface.java`
    - Evidence: `src/main/java/com/example/behavior/BehaviorMessagingSurface.java:9` (`ev:src/main/java/com/example/behavior/BehaviorMessagingSurface.java:9-9:com.example.behavior.BehaviorMessagingSurface:@Transactional`)
  - `spring_transaction_boundary`: `com.example.behavior.BehaviorMessagingSurface#settleInvoice` (support_type: `extracted`, transaction_signal: `direct_transactional_method`, target_kind: `method`, annotation_symbol: `@Transactional`).
    - Source: `src/main/java/com/example/behavior/BehaviorMessagingSurface.java`
    - Evidence: `src/main/java/com/example/behavior/BehaviorMessagingSurface.java:12` (`ev:src/main/java/com/example/behavior/BehaviorMessagingSurface.java:12-12:com.example.behavior.BehaviorMessagingSurface#settleInvoice:@Transactional`)
  - `spring_scheduled_method`: `com.example.behavior.BehaviorMessagingSurface#refreshInvoices` (support_type: `extracted`, scheduled_signal: `direct_scheduled_method`, target_kind: `method`, annotation_symbol: `@Scheduled`).
    - Source: `src/main/java/com/example/behavior/BehaviorMessagingSurface.java`
    - Evidence: `src/main/java/com/example/behavior/BehaviorMessagingSurface.java:16` (`ev:src/main/java/com/example/behavior/BehaviorMessagingSurface.java:16-16:com.example.behavior.BehaviorMessagingSurface#refreshInvoices:@Scheduled`)
  - `spring_event_listener`: `com.example.behavior.BehaviorMessagingSurface#onInvoicePaid` (support_type: `extracted`, event_listener_signal: `direct_event_listener_method`, target_kind: `method`, annotation_symbol: `@EventListener`).
    - Source: `src/main/java/com/example/behavior/BehaviorMessagingSurface.java`
    - Evidence: `src/main/java/com/example/behavior/BehaviorMessagingSurface.java:20` (`ev:src/main/java/com/example/behavior/BehaviorMessagingSurface.java:20-20:com.example.behavior.BehaviorMessagingSurface#onInvoicePaid:@EventListener`)
  - `messaging_listener_signal`: `com.example.behavior.BehaviorMessagingSurface` (support_type: `extracted`, listener_signal: `direct_rabbit_listener_annotation`, target_kind: `type`, annotation_symbol: `@RabbitListener`, listener_framework: `rabbit`).
    - Source: `src/main/java/com/example/behavior/BehaviorMessagingSurface.java`
    - Evidence: `src/main/java/com/example/behavior/BehaviorMessagingSurface.java:10` (`ev:src/main/java/com/example/behavior/BehaviorMessagingSurface.java:10-10:com.example.behavior.BehaviorMessagingSurface:@RabbitListener`)
  - ... and 2 more Spring application surface extracted facts in `project-map.json`.
- Inferred signals: detected none.
- Uncertain/not-analyzed statuses: detected none.
- Warnings: detected none.

## Detected Spring Components

- Analysis status: `analyzed`
- Detected: no direct Spring stereotype components recorded.

## Detected Tests

- Analysis status: `not_detected`
- Not analyzed: no supported test root was detected.

## Quality And Change-Risk Signals

- Quality analysis status: `analyzed`
- Test-gap signals are absence-sensitive planning hints from the bounded test inventory and inferred tested-subject relations. They do not prove coverage gaps, execution behavior, assertion behavior, CI status, or complete subject mapping.
- Change-risk signals are warning-oriented or uncertain planning hints from existing deterministic facts. They do not prove production impact, vulnerability, business priority, correctness, runtime behavior, or test priority.

### Test-Gap Signals

- Analysis status: `not_detected`
- Test-gap signals: none recorded.

### Change-Risk Signals

- Analysis status: `analyzed`
- Change-risk signal: `event_listener_change_surface` for `spring_event_listener` `com.example.behavior.BehaviorMessagingSurface#onInvoicePaid` (status: `planning_hint`, risk_basis: `source_visible_event_listener`, confidence: `low`, uncertainty: `source_visible_change_surface_only`). No production impact, vulnerability, correctness, runtime behavior, or business priority is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `spring_event_listener:module:.:com.example.behavior.BehaviorMessagingSurface#onInvoicePaid:decl:000001`
  - Subject source hint: class `com.example.behavior.BehaviorMessagingSurface`, member `onInvoicePaid`
  - Evidence: `src/main/java/com/example/behavior/BehaviorMessagingSurface.java:20` (`ev:src/main/java/com/example/behavior/BehaviorMessagingSurface.java:20-20:com.example.behavior.BehaviorMessagingSurface#onInvoicePaid:@EventListener`)
- Change-risk signal: `messaging_listener_change_surface` for `spring_messaging_listener` `com.example.behavior.BehaviorMessagingSurface` (status: `planning_hint`, risk_basis: `source_visible_messaging_listener`, confidence: `low`, uncertainty: `source_visible_change_surface_only`). No production impact, vulnerability, correctness, runtime behavior, or business priority is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `messaging_listener_signal:module:.:com.example.behavior.BehaviorMessagingSurface:annotation:rabbit_listener:decl:000001`
  - Subject source hint: class `com.example.behavior.BehaviorMessagingSurface`, member `not recorded`
  - Evidence: `src/main/java/com/example/behavior/BehaviorMessagingSurface.java:10` (`ev:src/main/java/com/example/behavior/BehaviorMessagingSurface.java:10-10:com.example.behavior.BehaviorMessagingSurface:@RabbitListener`)
- Change-risk signal: `messaging_listener_change_surface` for `spring_messaging_listener` `com.example.behavior.BehaviorMessagingSurface#onKafkaEvent` (status: `planning_hint`, risk_basis: `source_visible_messaging_listener`, confidence: `low`, uncertainty: `source_visible_change_surface_only`). No production impact, vulnerability, correctness, runtime behavior, or business priority is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `messaging_listener_signal:module:.:com.example.behavior.BehaviorMessagingSurface#onKafkaEvent:annotation:kafka_listener:decl:000001`
  - Subject source hint: class `com.example.behavior.BehaviorMessagingSurface`, member `onKafkaEvent`
  - Evidence: `src/main/java/com/example/behavior/BehaviorMessagingSurface.java:24` (`ev:src/main/java/com/example/behavior/BehaviorMessagingSurface.java:24-24:com.example.behavior.BehaviorMessagingSurface#onKafkaEvent:@KafkaListener`)
- Change-risk signal: `messaging_listener_change_surface` for `spring_messaging_listener` `com.example.behavior.BehaviorMessagingSurface#onRabbitRetry` (status: `planning_hint`, risk_basis: `source_visible_messaging_listener`, confidence: `low`, uncertainty: `source_visible_change_surface_only`). No production impact, vulnerability, correctness, runtime behavior, or business priority is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `messaging_listener_signal:module:.:com.example.behavior.BehaviorMessagingSurface#onRabbitRetry:annotation:rabbit_listener:decl:000002`
  - Subject source hint: class `com.example.behavior.BehaviorMessagingSurface`, member `onRabbitRetry`
  - Evidence: `src/main/java/com/example/behavior/BehaviorMessagingSurface.java:28` (`ev:src/main/java/com/example/behavior/BehaviorMessagingSurface.java:28-28:com.example.behavior.BehaviorMessagingSurface#onRabbitRetry:@RabbitListener`)
- Change-risk signal: `scheduled_method_change_surface` for `spring_scheduled_method` `com.example.behavior.BehaviorMessagingSurface#refreshInvoices` (status: `planning_hint`, risk_basis: `source_visible_scheduled_method`, confidence: `low`, uncertainty: `source_visible_change_surface_only`). No production impact, vulnerability, correctness, runtime behavior, or business priority is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `spring_scheduled_method:module:.:com.example.behavior.BehaviorMessagingSurface#refreshInvoices:decl:000001`
  - Subject source hint: class `com.example.behavior.BehaviorMessagingSurface`, member `refreshInvoices`
  - Evidence: `src/main/java/com/example/behavior/BehaviorMessagingSurface.java:16` (`ev:src/main/java/com/example/behavior/BehaviorMessagingSurface.java:16-16:com.example.behavior.BehaviorMessagingSurface#refreshInvoices:@Scheduled`)
- Change-risk signal: `transaction_boundary_change_surface` for `spring_transaction_boundary` `com.example.behavior.BehaviorMessagingSurface` (status: `planning_hint`, risk_basis: `source_visible_transaction_boundary`, confidence: `low`, uncertainty: `source_visible_change_surface_only`). No production impact, vulnerability, correctness, runtime behavior, or business priority is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `spring_transaction_boundary:module:.:com.example.behavior.BehaviorMessagingSurface:type`
  - Subject source hint: class `com.example.behavior.BehaviorMessagingSurface`, member `not recorded`
  - Evidence: `src/main/java/com/example/behavior/BehaviorMessagingSurface.java:9` (`ev:src/main/java/com/example/behavior/BehaviorMessagingSurface.java:9-9:com.example.behavior.BehaviorMessagingSurface:@Transactional`)
- Change-risk signal: `transaction_boundary_change_surface` for `spring_transaction_boundary` `com.example.behavior.BehaviorMessagingSurface#settleInvoice` (status: `planning_hint`, risk_basis: `source_visible_transaction_boundary`, confidence: `low`, uncertainty: `source_visible_change_surface_only`). No production impact, vulnerability, correctness, runtime behavior, or business priority is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `spring_transaction_boundary:module:.:com.example.behavior.BehaviorMessagingSurface#settleInvoice:decl:000001`
  - Subject source hint: class `com.example.behavior.BehaviorMessagingSurface`, member `settleInvoice`
  - Evidence: `src/main/java/com/example/behavior/BehaviorMessagingSurface.java:12` (`ev:src/main/java/com/example/behavior/BehaviorMessagingSurface.java:12-12:com.example.behavior.BehaviorMessagingSurface#settleInvoice:@Transactional`)


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
- Uncertain: no endpoint facts were recorded, so HTTP entry points may be absent or outside the currently supported analyzer scope.
- Uncertain: no entity facts were recorded, so persistence mappings may be absent or outside the currently supported analyzer scope.
- Not analyzed: supported Maven test roots were not detected.
