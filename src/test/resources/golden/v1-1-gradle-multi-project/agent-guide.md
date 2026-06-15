# Agent Guide

Generated deterministically from `project-map.json` and `evidence-index.jsonl`. The guide generator does not re-analyze source files.

## Detected Project Layout

- Build system: Detected `gradle`
- Root build file: Detected `settings.gradle.kts`
  - Evidence: `settings.gradle.kts:1` (`ev:settings.gradle.kts:1-1:build_file:gradle:settings`), `build.gradle.kts:1` (`ev:build.gradle.kts:1-1:build_file:gradle:build`)
- Root build files: Detected `gradle:settings:settings.gradle.kts`, `gradle:root_project_build:build.gradle.kts`
  - Root build-file evidence: `settings.gradle.kts:1` (`ev:settings.gradle.kts:1-1:build_file:gradle:settings`), `build.gradle.kts:1` (`ev:build.gradle.kts:1-1:build_file:gradle:build`)
- Source roots: Detected `app/src/main/java`, `src/main/java`
  - Evidence: recorded in `project-map.json`; no separate source-root evidence IDs are emitted.
- Test roots: Detected `app/src/test/java`
  - Evidence: recorded in `project-map.json`; no separate test-root evidence IDs are emitted.
- Modules analysis status: `analyzed`
- Module: Detected `module:.` (path: `.`)
  - POM path: Not analyzed; no POM path was recorded for this module.
  - Support status: `supported`
  - Declaration kind: `scan_root`
  - Declared path: `.`
  - Build systems: `gradle`
  - Gradle project path: `:`
  - Source roots: Detected `src/main/java`
  - Source roots evidence: recorded in `project-map.json`; no separate production root evidence IDs are emitted.
  - Test roots: Not analyzed; no supported test roots were recorded for this module.
  - Declaration evidence: none recorded.
  - POM evidence: none recorded.
  - Gradle build-file evidence: `settings.gradle.kts:1` (`ev:settings.gradle.kts:1-1:build_file:gradle:settings`), `build.gradle.kts:1` (`ev:build.gradle.kts:1-1:build_file:gradle:build`)
- Module: Detected `module:app` (path: `app`)
  - POM path: Not analyzed; no POM path was recorded for this module.
  - Support status: `supported`
  - Declaration kind: `gradle_settings_include`
  - Declared path: `app`
  - Build systems: `gradle`
  - Gradle project path: `:app`
  - Source roots: Detected `app/src/main/java`
  - Source roots evidence: recorded in `project-map.json`; no separate production root evidence IDs are emitted.
  - Test roots: Detected `app/src/test/java`
  - Test roots evidence: recorded in `project-map.json`; no separate test root evidence IDs are emitted.
  - Declaration evidence: `settings.gradle.kts:2` (`ev:settings.gradle.kts:2-2:build_file:gradle:include:decl:000001`)
  - POM evidence: none recorded.
  - Gradle build-file evidence: `settings.gradle.kts:2` (`ev:settings.gradle.kts:2-2:build_file:gradle:include:decl:000001`), `app/build.gradle.kts:1` (`ev:app/build.gradle.kts:1-1:build_file:gradle:build`)
- Module: Detected `module:libs/empty` (path: `libs/empty`)
  - POM path: Not analyzed; no POM path was recorded for this module.
  - Support status: `unsupported`
  - Declaration kind: `gradle_settings_include`
  - Declared path: `libs/empty`
  - Build systems: `gradle`
  - Gradle project path: `:libs:empty`
  - Source roots: Not analyzed; no supported production roots were recorded for this module.
  - Test roots: Not analyzed; no supported test roots were recorded for this module.
  - Declaration evidence: `settings.gradle.kts:2` (`ev:settings.gradle.kts:2-2:build_file:gradle:include:decl:000004`)
  - POM evidence: none recorded.
  - Gradle build-file evidence: `settings.gradle.kts:2` (`ev:settings.gradle.kts:2-2:build_file:gradle:include:decl:000004`), `libs/empty/build.gradle.kts:1` (`ev:libs/empty/build.gradle.kts:1-1:build_file:gradle:build`)
- Module: Detected `module:libs/missing` (path: `libs/missing`)
  - POM path: Not analyzed; no POM path was recorded for this module.
  - Support status: `missing_project_directory`
  - Declaration kind: `gradle_settings_include`
  - Declared path: `libs/missing`
  - Build systems: `gradle`
  - Gradle project path: `:libs:missing`
  - Source roots: Not analyzed; no supported production roots were recorded for this module.
  - Test roots: Not analyzed; no supported test roots were recorded for this module.
  - Declaration evidence: `settings.gradle.kts:2` (`ev:settings.gradle.kts:2-2:build_file:gradle:include:decl:000003`)
  - POM evidence: none recorded.
  - Gradle build-file evidence: `settings.gradle.kts:2` (`ev:settings.gradle.kts:2-2:build_file:gradle:include:decl:000003`)
- Module: Detected `module:services/orders` (path: `services/orders`)
  - POM path: Not analyzed; no POM path was recorded for this module.
  - Support status: `supported`
  - Declaration kind: `gradle_settings_include`
  - Declared path: `services/orders`
  - Build systems: `gradle`
  - Gradle project path: `:services:orders`
  - Source roots: Not analyzed; no supported production roots were recorded for this module.
  - Test roots: Not analyzed; no supported test roots were recorded for this module.
  - Declaration evidence: `settings.gradle.kts:2` (`ev:settings.gradle.kts:2-2:build_file:gradle:include:decl:000002`)
  - POM evidence: none recorded.
  - Gradle build-file evidence: `settings.gradle.kts:2` (`ev:settings.gradle.kts:2-2:build_file:gradle:include:decl:000002`), `services/orders/build.gradle:1` (`ev:services/orders/build.gradle:1-1:build_file:gradle:build`)

## Build And Configuration Orientation

### Module `module:.` (path: `.`)

- Build/config analysis status: `analyzed`
- Source-visible Maven metadata: Not analyzed; status `not_detected`.
- Source-visible direct dependencies: Not analyzed; status `not_detected`.
- Source-visible dependency-management declarations: Not analyzed; status `not_detected`.
- Source-visible direct plugins: Not analyzed; status `not_detected`.
- Source-visible plugin-management declarations: Not analyzed; status `not_detected`.
- Source-visible Gradle build files: Detected 2 local build-file inputs for Gradle project path `:`.
  - Gradle build file: `settings.gradle.kts` role `settings`, language `kotlin_dsl`.
  - Gradle build file: `build.gradle.kts` role `project_build`, language `kotlin_dsl`.
  - Evidence: `settings.gradle.kts:1` (`ev:settings.gradle.kts:1-1:build_file:gradle:settings`), `build.gradle.kts:1` (`ev:build.gradle.kts:1-1:build_file:gradle:build`)
- Static Gradle sourceSets: Not analyzed; status `not_analyzed`. Standard Java and resource roots are represented by module roots and resources.
- Resource roots: Detected 1 standard resource root.
  - Resource root: `test` `src/test/resources`
    - Evidence: recorded in `project-map.json`; no separate resource-root evidence IDs are emitted.
- Config files: Detected none.
- Spring Boot application signals: Detected none.
- Module warnings: Detected none.

### Module `module:app` (path: `app`)

- Build/config analysis status: `analyzed`
- Source-visible Maven metadata: Not analyzed; status `not_detected`.
- Source-visible direct dependencies: Not analyzed; status `not_detected`.
- Source-visible dependency-management declarations: Not analyzed; status `not_detected`.
- Source-visible direct plugins: Not analyzed; status `not_detected`.
- Source-visible plugin-management declarations: Not analyzed; status `not_detected`.
- Source-visible Gradle build files: Detected 2 local build-file inputs for Gradle project path `:app`.
  - Gradle build file: `settings.gradle.kts` role `settings`, language `kotlin_dsl`.
  - Gradle build file: `app/build.gradle.kts` role `project_build`, language `kotlin_dsl`.
  - Evidence: `settings.gradle.kts:2` (`ev:settings.gradle.kts:2-2:build_file:gradle:include:decl:000001`), `app/build.gradle.kts:1` (`ev:app/build.gradle.kts:1-1:build_file:gradle:build`)
- Static Gradle sourceSets: Not analyzed; status `not_analyzed`. Standard Java and resource roots are represented by module roots and resources.
- Resource roots: Not analyzed; status `not_detected`.
- Config files: Not analyzed; status `not_detected`.
- Spring Boot application signals: Detected none.
- Module warnings: Detected 1 warning signal for this module: `gradle_module:duplicate_project_path`. See `Known Uncertainty And Limits` for warning evidence and messages.

### Module `module:libs/empty` (path: `libs/empty`)

- Build/config analysis status: `analyzed`
- Source-visible Maven metadata: Not analyzed; status `not_detected`.
- Source-visible direct dependencies: Not analyzed; status `not_detected`.
- Source-visible dependency-management declarations: Not analyzed; status `not_detected`.
- Source-visible direct plugins: Not analyzed; status `not_detected`.
- Source-visible plugin-management declarations: Not analyzed; status `not_detected`.
- Source-visible Gradle build files: Detected 2 local build-file inputs for Gradle project path `:libs:empty`.
  - Gradle build file: `settings.gradle.kts` role `settings`, language `kotlin_dsl`.
  - Gradle build file: `libs/empty/build.gradle.kts` role `project_build`, language `kotlin_dsl`.
  - Evidence: `settings.gradle.kts:2` (`ev:settings.gradle.kts:2-2:build_file:gradle:include:decl:000004`), `libs/empty/build.gradle.kts:1` (`ev:libs/empty/build.gradle.kts:1-1:build_file:gradle:build`)
- Static Gradle sourceSets: Not analyzed; status `not_analyzed`. Standard Java and resource roots are represented by module roots and resources.
- Resource roots: Not analyzed; status `not_detected`.
- Config files: Not analyzed; status `not_detected`.
- Spring Boot application signals: Not analyzed; status `not_detected`.
- Module warnings: Detected 1 warning signal for this module: `gradle_module:unsupported_module`. See `Known Uncertainty And Limits` for warning evidence and messages.

### Module `module:libs/missing` (path: `libs/missing`)

- Build/config analysis status: `analyzed`
- Source-visible Maven metadata: Not analyzed; status `not_detected`.
- Source-visible direct dependencies: Not analyzed; status `not_detected`.
- Source-visible dependency-management declarations: Not analyzed; status `not_detected`.
- Source-visible direct plugins: Not analyzed; status `not_detected`.
- Source-visible plugin-management declarations: Not analyzed; status `not_detected`.
- Source-visible Gradle build files: Detected 1 local build-file input for Gradle project path `:libs:missing`.
  - Gradle build file: `settings.gradle.kts` role `settings`, language `kotlin_dsl`.
  - Evidence: `settings.gradle.kts:2` (`ev:settings.gradle.kts:2-2:build_file:gradle:include:decl:000003`)
- Static Gradle sourceSets: Not analyzed; status `not_analyzed`. Standard Java and resource roots are represented by module roots and resources.
- Resource roots: Not analyzed; status `not_detected`.
- Config files: Not analyzed; status `not_detected`.
- Spring Boot application signals: Not analyzed; status `not_detected`.
- Module warnings: Detected 1 warning signal for this module: `gradle_module:missing_project_directory`. See `Known Uncertainty And Limits` for warning evidence and messages.

### Module `module:services/orders` (path: `services/orders`)

- Build/config analysis status: `analyzed`
- Source-visible Maven metadata: Not analyzed; status `not_detected`.
- Source-visible direct dependencies: Not analyzed; status `not_detected`.
- Source-visible dependency-management declarations: Not analyzed; status `not_detected`.
- Source-visible direct plugins: Not analyzed; status `not_detected`.
- Source-visible plugin-management declarations: Not analyzed; status `not_detected`.
- Source-visible Gradle build files: Detected 2 local build-file inputs for Gradle project path `:services:orders`.
  - Gradle build file: `settings.gradle.kts` role `settings`, language `kotlin_dsl`.
  - Gradle build file: `services/orders/build.gradle` role `project_build`, language `groovy_dsl`.
  - Evidence: `settings.gradle.kts:2` (`ev:settings.gradle.kts:2-2:build_file:gradle:include:decl:000002`), `services/orders/build.gradle:1` (`ev:services/orders/build.gradle:1-1:build_file:gradle:build`)
- Static Gradle sourceSets: Not analyzed; status `not_analyzed`. Standard Java and resource roots are represented by module roots and resources.
- Resource roots: Detected 2 standard resource roots.
  - Resource root: `main` `services/orders/src/main/resources`
    - Evidence: recorded in `project-map.json`; no separate resource-root evidence IDs are emitted.
  - Resource root: `test` `services/orders/src/test/resources`
    - Evidence: recorded in `project-map.json`; no separate resource-root evidence IDs are emitted.
- Config files: Detected none.
- Spring Boot application signals: Not analyzed; status `not_detected`.
- Module warnings: Detected none.

## Generated Source And Codegen Orientation

- Generated-source metadata status: `analyzed`.
- Policy: content scan `disabled`, default `false`, configurable `false`, content_status `not_scanned`.
- Generated-source roots are metadata only; they are not production `source_roots`, test roots, endpoint facts, API operation facts, or generated API facts.
- Generated-source roots: status `analyzed`; detected none.
- Generator/codegen signals: status `analyzed`; warning IDs none recorded; Maven plugin IDs none recorded.

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

## Spring Application Surface

- Spring application surface analysis status: `analyzed`
- Repository stereotype entries are direct `@Repository` annotation observations; they do not prove runtime bean registration or entity ownership.
- Spring Data repository interface entries are inferred source-visible extension signals; repository/entity relation rows, when present, are inferred generic links. They do not prove runtime repositories, query method behavior, database access, or runtime repository/entity verification.
- Configuration classes, configuration-properties types, and `@Bean` methods are source-visible Spring configuration signals; they do not prove runtime bean graphs, binding success, config values, bean scopes, lifecycle, proxy behavior, or dependency graphs.
- Transaction, scheduled, event listener, and messaging listener entries are source-visible operational change-surface signals; they do not prove runtime transaction behavior, scheduler registration, event delivery, message destinations, or broker topology.
- Spring Security configuration warnings are inspection hints and change-risk signals; they do not prove security policy, endpoint protection, authentication behavior, authorization behavior, vulnerability, or correctness.
- Subsection statuses: repositories `analyzed`, configuration classes `analyzed`, configuration properties `analyzed`, bean methods `analyzed`, transaction boundaries `analyzed`, scheduled methods `analyzed`, event listeners `analyzed`, messaging listeners `analyzed`, security warnings `analyzed`.
- Spring application surface facts: detected none for supported modules.

## Detected Spring MVC Endpoints

- Detected: no Spring MVC endpoints recorded in `project-map.json`.

## Detected Spring Components

- Analysis status: `analyzed`
- Detected: no direct Spring stereotype components recorded.

## Detected Tests

- Analysis status: `analyzed`
- Detected: no test classes recorded.

## Known Uncertainty And Limits

- Warning: `gradle_module` signal `duplicate_project_path` for module `module:app` (path: `app`) at `settings.gradle.kts`. Duplicate Gradle settings include ignored; v1.1 analyzes each normalized Gradle project path once.
  - Evidence: `settings.gradle.kts:2` (`ev:settings.gradle.kts:2-2:build_file:gradle:include:decl:000005`)
- Warning: `gradle_module` signal `missing_project_directory` for module `module:libs/missing` (path: `libs/missing`) at `settings.gradle.kts`. Gradle settings include uses the default project directory mapping, but the project directory is absent; this module is not inspected.
  - Evidence: `settings.gradle.kts:2` (`ev:settings.gradle.kts:2-2:build_file:gradle:include:decl:000003`)
- Warning: `gradle_module` signal `unsupported_dynamic_include` for no module identity at `settings.gradle.kts`. Gradle settings declaration is directly visible but outside the v1.1 literal include subset; dynamic or composite-build declarations are not analyzed.
  - Evidence: `settings.gradle.kts:3` (`ev:settings.gradle.kts:3-3:build_file:gradle:include:decl:000006`)
- Warning: `gradle_module` signal `unsupported_module` for module `module:libs/empty` (path: `libs/empty`) at `libs/empty/build.gradle.kts`. Gradle project has no supported Java source, test, or resource roots; the analyzer does not inspect this module.
  - Evidence: `settings.gradle.kts:2` (`ev:settings.gradle.kts:2-2:build_file:gradle:include:decl:000004`), `libs/empty/build.gradle.kts:1` (`ev:libs/empty/build.gradle.kts:1-1:build_file:gradle:build`)
- Warning: `gradle_module` signal `unsupported_project_dir_mapping` for no module identity at `settings.gradle.kts`. Gradle projectDir remapping is directly visible but outside the v1.1 default project-directory mapping subset; remapped directories are not analyzed.
  - Evidence: `settings.gradle.kts:4` (`ev:settings.gradle.kts:4-4:build_file:gradle:projectDir:decl:000007`)
- Not scanned: Generated-source roots are metadata-only path/codegen observations with `content_status: "not_scanned"`; generated source contents, generator execution, generated API reconstruction, runtime freshness checks, dependency/task resolution, and custom Gradle generated-source graph reconstruction are not performed.
- Not analyzed: Spring runtime behavior such as component scanning, dependency injection graphs, bean lifecycle, scopes, and conditional configuration is not represented by `components.items`.
- Uncertain: JPA relationship targets preserve `target_resolution: declared_type_only` and `uncertainty: target_type_not_resolved`; no symbol solving or ORM runtime behavior is claimed.
- Source-visible: JPA relationship metadata such as `mappedBy`, `@JoinColumn`, `@JoinTable`, `optional`, `fetch`, `cascade`, and `orphanRemoval` is reported only when direct annotation attributes are supported; foreign keys, join tables, ownership correctness, fetch behavior, cascade behavior, and database constraints are not claimed.
- Not analyzed: JPA mapped-superclass identifier support is limited to conservative source-visible mapped-superclass chains; unresolved, ambiguous, cyclic, or non-source-visible branches are skipped.
- Partial: JPA embedded and composite identifier support is limited to direct source-visible `@Embeddable`, `@Embedded`, `@EmbeddedId`, and `@IdClass` signals. Embedded targets are linked only when a unique local `@Embeddable` can be matched; `@IdClass` field matching and composite-key semantics are not analyzed.
- Inferred/statused: tested-subject rows are conservative source-visible hints from supported naming, import, field-type, and Spring test slice class-literal signals. Non-inferred statuses such as `not_detected`, `ambiguous`, and `unsupported` do not claim coverage or execution. Test method inventory records source-visible JUnit annotation structure only. Test execution, CI results, coverage, assertion behavior, call graphs, and complete subject mapping are not analyzed.
- Not analyzed: connectors, LLM summaries, repository chat, generic RAG, Maven profiles, effective POM reconstruction, Gradle execution, dynamic buildscript evaluation, dependency graphs, and recursive nested Maven modules are outside this guide.
- Not analyzed: Gradle dependency resolution, plugin resolution, task graphs, custom `sourceSets`, `projectDir` remapping, included builds, and Kotlin source analysis are outside this guide.
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

## Practical Inspection Order For Coding Agents

1. Start with detected build, module, and layout facts in `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `libs/empty/build.gradle.kts`, `services/orders/build.gradle`.
2. For HTTP behavior, inspect detected endpoint and hidden-surface warning evidence (no evidence paths recorded).
3. For Spring application surface changes, inspect Spring application surface and component evidence (no evidence paths recorded) and avoid assuming runtime repository registration, entity ownership, injection graphs, transaction behavior, scheduler registration, event delivery, or messaging topology.
4. For tests, inspect detected test files and tested-subject relation/status evidence (no evidence paths recorded); do not treat inferred or statused subjects as coverage proof.
