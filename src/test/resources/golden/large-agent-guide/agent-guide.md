# Agent Guide

Generated deterministically from `project-map.json` and `evidence-index.jsonl`. The guide generator does not re-analyze source files.

## Read This First

- Open `artifact-set.json` before this guide and respect its artifact authority labels.
- Use this guide as deterministic orientation only. It is not evidence and does not re-analyze source files.
- For large or unknown outputs, prefer `query <path> agent-context`, targeted query commands, focused `project-map.json` selection, exact `evidence-index.jsonl` lookup, and source readback instead of reading every row.
- Size note: this guide is `small-guide` (about `16 KiB`, `157` rendered lines); known generator inputs are `project-map.json` `8 KiB` and `evidence-index.jsonl` `7 KiB`.

## Trust And Verification Legend

Trust and verification legend:
- Use `evidence-index.jsonl` as the authoritative source-backed evidence ledger; verify important claims against its exact records and the repository source locations they cite.
- Generated project facts: `project-map.json` facts; verify important use through their evidence IDs.
- Deterministic presentation: this guide, `endpoints.md`, and query stdout help with orientation; they are not evidence.
- Navigation, provenance, or execution metadata: `artifact-set.json`, `project-graph.json`, `source-registry.json`, profiles, LLM/provider AI output, cache, workspace, adapter output, release metadata, security reports, and downstream-agent output are non-evidence unless a later public contract explicitly changes that.
- Before code changes, review findings, public/security/release wording, or architecture decisions, resolve exact evidence IDs and read the cited source.

## Practical Inspection Order For Coding Agents

1. Start with detected build and layout facts in `pom.xml`.
2. For HTTP behavior, inspect detected endpoint and hidden-surface warning evidence (no evidence paths recorded).
3. For Spring application surface changes, inspect Spring application surface and component evidence in `src/main/java/com/example/components/ComponentOne.java`, `src/main/java/com/example/components/ComponentTwo.java`, `src/main/java/com/example/components/ComponentThree.java`, `src/main/java/com/example/components/ComponentFour.java`, `src/main/java/com/example/components/ComponentFive.java`, ... and 2 more evidence paths in `evidence-index.jsonl` and avoid assuming runtime repository registration, entity ownership, injection graphs, transaction behavior, scheduler registration, event delivery, or messaging topology.
4. For tests, inspect detected test files and tested-subject relation/status evidence in `src/test/java/com/example/web/LargeControllerTest.java`, `src/main/java/com/example/web/LargeController.java`; do not treat inferred or statused subjects as coverage proof.

## Project Memory Overview

- Build/layout: build system `maven`, modules `0`, source roots `1`, test roots `1`.
- Source-backed fact surfaces: endpoints `0`, direct Spring components `7`, Spring application surface rows `0`, entities `0`, embeddables `0`, tests `1`.
- Planning/navigation surfaces: warnings `0`, quality/change-risk hints `0`, local documents `0`, document reconciliation hints `0`.
- Evidence records: `18` records in `evidence-index.jsonl`; this overview is presentation only.
- Size band: `small-guide`; large detailed sections should be selected by task and verified through exact evidence IDs.

## Known Uncertainty Snapshot

- Warnings: `0` warning rows; warning evidence and messages stay in the detailed limits section.
- Inferred or statused rows: `1` rows; keep `inferred`, `ambiguous`, `not_detected`, `unsupported`, and similar labels attached to any use.
- Explicit uncertainty labels: `0` values; preserve those caveats with the cited evidence.
- Not analyzed/out-of-scope status markers: `0`; runtime behavior, generated-source contents, test execution/coverage, source/spec agreement, connectors, and LLM summaries remain outside source-backed evidence unless a later contract says otherwise.

## Not Represented In This Scan

- No represented rows for: `Spring MVC endpoints`, `domain/data model facts`, `quality/change-risk planning hints`, `local project documentation`, `generated-source root metadata`. This means the current deterministic scan emitted no rows for those surfaces; it does not prove the runtime behavior is absent outside the supported analyzer scope.

## Detected Project Layout

- Build system: Detected `maven`
- Root build file: Detected `pom.xml`
  - Evidence: `pom.xml:1` (`ev:pom.xml:1-1:build_file:pom.xml`)
- Source roots: Detected `src/main/java`
  - Evidence: recorded in `project-map.json`; no separate source-root evidence IDs are emitted.
- Test roots: Detected `src/test/java`
  - Evidence: recorded in `project-map.json`; no separate test-root evidence IDs are emitted.

## Build And Configuration Orientation

- Not analyzed: no module build/config facts were recorded.

## Detected Spring MVC Endpoints

- Detected: no Spring MVC endpoints recorded in `project-map.json`.

## Detected Spring Components

- Analysis status: `analyzed`
- Component summary: detected 7 direct Spring stereotype components.

### `com.example.components.ComponentOne`

- Stereotypes: Detected `@Service`
  - Evidence: `src/main/java/com/example/components/ComponentOne.java:8` (`ev:src/main/java/com/example/components/ComponentOne.java:8-8:com.example.components.ComponentOne:@Service`)

### `com.example.components.ComponentTwo`

- Stereotypes: Detected `@Service`
  - Evidence: `src/main/java/com/example/components/ComponentTwo.java:8` (`ev:src/main/java/com/example/components/ComponentTwo.java:8-8:com.example.components.ComponentTwo:@Service`)

### `com.example.components.ComponentThree`

- Stereotypes: Detected `@Service`
  - Evidence: `src/main/java/com/example/components/ComponentThree.java:8` (`ev:src/main/java/com/example/components/ComponentThree.java:8-8:com.example.components.ComponentThree:@Service`)

### `com.example.components.ComponentFour`

- Stereotypes: Detected `@Service`
  - Evidence: `src/main/java/com/example/components/ComponentFour.java:8` (`ev:src/main/java/com/example/components/ComponentFour.java:8-8:com.example.components.ComponentFour:@Service`)

### `com.example.components.ComponentFive`

- Stereotypes: Detected `@Service`
  - Evidence: `src/main/java/com/example/components/ComponentFive.java:8` (`ev:src/main/java/com/example/components/ComponentFive.java:8-8:com.example.components.ComponentFive:@Service`)

### `com.example.components.ComponentSix`

- Stereotypes: Detected `@Service`
  - Evidence: `src/main/java/com/example/components/ComponentSix.java:8` (`ev:src/main/java/com/example/components/ComponentSix.java:8-8:com.example.components.ComponentSix:@Service`)

### `com.example.components.ComponentSeven`

- Stereotypes: Detected `@Service`
  - Evidence: `src/main/java/com/example/components/ComponentSeven.java:8` (`ev:src/main/java/com/example/components/ComponentSeven.java:8-8:com.example.components.ComponentSeven:@Service`)

## Detected Tests

- Analysis status: `analyzed`
- Test inventory summary: detected 1 test class, 1 framework signal, 0 Spring test slice signals, 0 mock signals, 7 supported JUnit methods, and 1 tested-subject relation/status row.

### `com.example.web.LargeControllerTest`

- Test class: Detected `com.example.web.LargeControllerTest`
  - Evidence: `src/test/java/com/example/web/LargeControllerTest.java:3` (`ev:src/test/java/com/example/web/LargeControllerTest.java:3-3:com.example.web.LargeControllerTest:test_file`)
- Source: Detected `src/test/java/com/example/web/LargeControllerTest.java`
- Framework signal: Detected `JUnit Jupiter` (signal_kind: `framework`)
  - Evidence: `src/test/java/com/example/web/LargeControllerTest.java:5` (`ev:src/test/java/com/example/web/LargeControllerTest.java:5-5:com.example.web.LargeControllerTest:import:org.junit.jupiter.api.Test`), `src/test/java/com/example/web/LargeControllerTest.java:10` (`ev:src/test/java/com/example/web/LargeControllerTest.java:10-10:com.example.web.LargeControllerTest#testOne:@Test`), `src/test/java/com/example/web/LargeControllerTest.java:13` (`ev:src/test/java/com/example/web/LargeControllerTest.java:13-13:com.example.web.LargeControllerTest#testTwo:@Test`), `src/test/java/com/example/web/LargeControllerTest.java:16` (`ev:src/test/java/com/example/web/LargeControllerTest.java:16-16:com.example.web.LargeControllerTest#testThree:@Test`), `src/test/java/com/example/web/LargeControllerTest.java:19` (`ev:src/test/java/com/example/web/LargeControllerTest.java:19-19:com.example.web.LargeControllerTest#testFour:@Test`), ... and 3 more evidence references in `evidence-index.jsonl`
- Test method: Detected `testOne` annotated with `@Test` (method_kind: `test`)
  - Evidence: `src/test/java/com/example/web/LargeControllerTest.java:10` (`ev:src/test/java/com/example/web/LargeControllerTest.java:10-10:com.example.web.LargeControllerTest#testOne:@Test`)
- Test method: Detected `testTwo` annotated with `@Test` (method_kind: `test`)
  - Evidence: `src/test/java/com/example/web/LargeControllerTest.java:13` (`ev:src/test/java/com/example/web/LargeControllerTest.java:13-13:com.example.web.LargeControllerTest#testTwo:@Test`)
- Test method: Detected `testThree` annotated with `@Test` (method_kind: `test`)
  - Evidence: `src/test/java/com/example/web/LargeControllerTest.java:16` (`ev:src/test/java/com/example/web/LargeControllerTest.java:16-16:com.example.web.LargeControllerTest#testThree:@Test`)
- Test method: Detected `testFour` annotated with `@Test` (method_kind: `test`)
  - Evidence: `src/test/java/com/example/web/LargeControllerTest.java:19` (`ev:src/test/java/com/example/web/LargeControllerTest.java:19-19:com.example.web.LargeControllerTest#testFour:@Test`)
- Test method: Detected `testFive` annotated with `@Test` (method_kind: `test`)
  - Evidence: `src/test/java/com/example/web/LargeControllerTest.java:22` (`ev:src/test/java/com/example/web/LargeControllerTest.java:22-22:com.example.web.LargeControllerTest#testFive:@Test`)
- Test method: Detected `testSix` annotated with `@Test` (method_kind: `test`)
  - Evidence: `src/test/java/com/example/web/LargeControllerTest.java:25` (`ev:src/test/java/com/example/web/LargeControllerTest.java:25-25:com.example.web.LargeControllerTest#testSix:@Test`)
- Test method: Detected `testSeven` annotated with `@Test` (method_kind: `test`)
  - Evidence: `src/test/java/com/example/web/LargeControllerTest.java:28` (`ev:src/test/java/com/example/web/LargeControllerTest.java:28-28:com.example.web.LargeControllerTest#testSeven:@Test`)
- Inferred tested subject: `com.example.web.LargeController` (relation_status: `inferred`, relation_type: `naming_convention`, support_type: `inferred`, confidence: `medium`).
  - Evidence: `src/test/java/com/example/web/LargeControllerTest.java:3` (`ev:src/test/java/com/example/web/LargeControllerTest.java:3-3:com.example.web.LargeControllerTest:test_file`), `src/main/java/com/example/web/LargeController.java:8` (`ev:src/main/java/com/example/web/LargeController.java:8-8:com.example.web.LargeController:code_symbol`)

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
- Not analyzed: connectors, LLM summaries, repository chat, generic RAG, Gradle/Kotlin support, and multi-module Maven parsing are outside this guide.
- Not analyzed: generated sources, generated API reconstruction, classpath-only interfaces, and ambiguous interface endpoint bindings are outside the source-visible interface endpoint support.
- Not analyzed: OpenAPI operation facts are spec-backed declared operations only; runtime implementation matching, source/spec agreement, generated source contents, and client SDK reconstruction are not claimed.
- Not analyzed: v0.3 build/config facts are direct local source observations only. Maven execution, effective POM reconstruction, profile activation, remote dependency resolution, config value interpretation, secret extraction, and default generated-source scanning are not performed.
- Not analyzed: Spring Boot application signals do not prove executable packaging, active profiles, runtime auto-configuration, bean graphs, component scanning results, deployment behavior, or actual process entrypoint behavior.
- Not analyzed: Spring Data repository interface signals do not prove runtime repository registration, query method behavior, database access, or runtime repository/entity verification. Repository/entity links, when present, are bounded inferred Spring Data generic relations with explicit `entity_relation_status` values.
- Not analyzed: JPA field metadata is limited to supported direct field-level source-visible annotations. It is not a complete persistent-property inventory, does not support getter/property access in this slice, and does not fill missing annotation attributes from JPA provider defaults.
- Uncertain: no endpoint facts were recorded, so HTTP entry points may be absent or outside the currently supported analyzer scope.
- Uncertain: no entity facts were recorded, so persistence mappings may be absent or outside the currently supported analyzer scope.
