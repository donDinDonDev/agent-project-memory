# Agent Guide

Generated deterministically from `project-map.json` and `evidence-index.jsonl`. The guide generator does not re-analyze source files.

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

## Known Uncertainty And Limits

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

## Practical Inspection Order For Coding Agents

1. Start with detected build and layout facts in `pom.xml`.
2. For HTTP behavior, inspect detected endpoint and hidden-surface warning evidence (no evidence paths recorded).
3. For Spring application surface changes, inspect Spring application surface and component evidence in `src/main/java/com/example/components/ComponentOne.java`, `src/main/java/com/example/components/ComponentTwo.java`, `src/main/java/com/example/components/ComponentThree.java`, `src/main/java/com/example/components/ComponentFour.java`, `src/main/java/com/example/components/ComponentFive.java`, ... and 2 more evidence paths in `evidence-index.jsonl` and avoid assuming runtime repository registration, entity ownership, injection graphs, transaction behavior, scheduler registration, event delivery, or messaging topology.
4. For tests, inspect detected test files and tested-subject relation/status evidence in `src/test/java/com/example/web/LargeControllerTest.java`, `src/main/java/com/example/web/LargeController.java`; do not treat inferred or statused subjects as coverage proof.
