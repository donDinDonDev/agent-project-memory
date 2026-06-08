# Agent Guide

Generated deterministically from `project-map.json` and `evidence-index.jsonl`. The guide generator does not re-analyze source files.

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
- Source-visible Maven metadata: group_id `com.example` (value_kind: `literal`), artifact_id `v0-5-spring-configuration-surface` (value_kind: `literal`), version `1.0.0` (value_kind: `literal`), packaging `value:not_declared` (value_kind: `not_declared`).
  - Evidence: `pom.xml:5` (`ev:pom.xml:5-5:build_file:maven:project:groupId`), `pom.xml:6` (`ev:pom.xml:6-6:build_file:maven:project:artifactId`), `pom.xml:7` (`ev:pom.xml:7-7:build_file:maven:project:version`)
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

## Spring Application Surface

- Spring application surface analysis status: `analyzed`
- Repository stereotype entries are direct `@Repository` annotation observations; they do not prove runtime bean registration or entity ownership.
- Spring Data repository interface entries are inferred source-visible extension signals; they do not prove runtime repositories, query method behavior, database access, or repository-to-entity relations.
- Configuration classes, configuration-properties types, and `@Bean` methods are source-visible Spring configuration signals; they do not prove runtime bean graphs, binding success, config values, bean scopes, lifecycle, proxy behavior, or dependency graphs.
- Transaction, scheduled, event listener, and messaging listener entries are source-visible operational change-surface signals; they do not prove runtime transaction behavior, scheduler registration, event delivery, message destinations, or broker topology.
- Repository signals: status `analyzed`; detected none.
- Configuration classes: status `analyzed`; detected 1 source-visible `@Configuration` class signal.
  - Configuration class: Detected `com.example.config.InventoryConfiguration` (surface_category: `spring_configuration_class`, support_type: `extracted`, configuration_signal: `direct_configuration_class`).
    - Source: `src/main/java/com/example/config/ConfigurationSurface.java`
    - Module: Detected `module:.` (path: `.`)
    - Evidence: `src/main/java/com/example/config/ConfigurationSurface.java:7` (`ev:src/main/java/com/example/config/ConfigurationSurface.java:7-7:com.example.config.InventoryConfiguration:@Configuration`)
- Configuration properties: status `analyzed`; detected 2 source-visible `@ConfigurationProperties` type signals.
  - Configuration properties type: Detected `com.example.config.CatalogProperties` with binding_status `not_analyzed` (surface_category: `spring_configuration_properties_type`, support_type: `extracted`, configuration_properties_signal: `direct_configuration_properties_type`).
    - Source: `src/main/java/com/example/config/ConfigurationSurface.java`
    - Module: Detected `module:.` (path: `.`)
    - Evidence: `src/main/java/com/example/config/ConfigurationSurface.java:37` (`ev:src/main/java/com/example/config/ConfigurationSurface.java:37-37:com.example.config.CatalogProperties:@ConfigurationProperties`)
  - Configuration properties type: Detected `com.example.config.InventoryProperties` with binding_status `not_analyzed` (surface_category: `spring_configuration_properties_type`, support_type: `extracted`, configuration_properties_signal: `direct_configuration_properties_type`).
    - Source: `src/main/java/com/example/config/ConfigurationSurface.java`
    - Module: Detected `module:.` (path: `.`)
    - Evidence: `src/main/java/com/example/config/ConfigurationSurface.java:20` (`ev:src/main/java/com/example/config/ConfigurationSurface.java:20-20:com.example.config.InventoryProperties:@ConfigurationProperties`)
- Bean methods: status `analyzed`; detected 3 source-visible `@Bean` method signals.
  - Bean method: Detected `com.example.config.InventoryConfiguration#inventoryClient` with bean_name_status `not_analyzed` (surface_category: `spring_bean_method`, support_type: `extracted`, bean_signal: `direct_bean_method`).
    - Source: `src/main/java/com/example/config/ConfigurationSurface.java`
    - Module: Detected `module:.` (path: `.`)
    - Evidence: `src/main/java/com/example/config/ConfigurationSurface.java:9` (`ev:src/main/java/com/example/config/ConfigurationSurface.java:9-9:com.example.config.InventoryConfiguration#inventoryClient:@Bean`)
  - Bean method: Detected `com.example.config.InventoryConfiguration#inventoryClock` with bean_name_status `not_analyzed` (surface_category: `spring_bean_method`, support_type: `extracted`, bean_signal: `direct_bean_method`).
    - Source: `src/main/java/com/example/config/ConfigurationSurface.java`
    - Module: Detected `module:.` (path: `.`)
    - Evidence: `src/main/java/com/example/config/ConfigurationSurface.java:14` (`ev:src/main/java/com/example/config/ConfigurationSurface.java:14-14:com.example.config.InventoryConfiguration#inventoryClock:@Bean`)
  - Bean method: Detected `com.example.config.SecondaryBeanFactory#secondaryBean` with bean_name_status `not_analyzed` (surface_category: `spring_bean_method`, support_type: `extracted`, bean_signal: `direct_bean_method`).
    - Source: `src/main/java/com/example/config/ConfigurationSurface.java`
    - Module: Detected `module:.` (path: `.`)
    - Evidence: `src/main/java/com/example/config/ConfigurationSurface.java:25` (`ev:src/main/java/com/example/config/ConfigurationSurface.java:25-25:com.example.config.SecondaryBeanFactory#secondaryBean:@Bean`)
- Transaction boundaries: status `analyzed`; detected none.
- Scheduled methods: status `analyzed`; detected none.
- Event listeners: status `analyzed`; detected none.
- Messaging listener signals: status `analyzed`; detected none.
- Spring Security configuration warnings: status `not_analyzed`; not analyzed in the current v0.5 implementation slice because security configuration warning analysis has not run.

## Detected Spring MVC Endpoints

- Detected: no Spring MVC endpoints recorded in `project-map.json`.

## Detected Spring Components

- Analysis status: `analyzed`

### `com.example.config.InventoryConfiguration`

- Module: Detected `module:.` (path: `.`)
- Stereotypes: Detected `@Configuration`
  - Evidence: `src/main/java/com/example/config/ConfigurationSurface.java:7` (`ev:src/main/java/com/example/config/ConfigurationSurface.java:7-7:com.example.config.InventoryConfiguration:@Configuration`)

## Detected JPA Entities

- Analysis status: `analyzed`
- Detected: no direct JPA entities recorded.

## Detected Tests

- Analysis status: `not_detected`
- Not analyzed: no supported test root was detected.

## Known Uncertainty And Limits

- Not analyzed: Spring runtime behavior such as component scanning, dependency injection graphs, bean lifecycle, scopes, and conditional configuration is not represented by `components.items`.
- Uncertain: JPA relationship targets preserve `target_resolution: declared_type_only` and `uncertainty: target_type_not_resolved`; no symbol solving or ORM runtime behavior is claimed.
- Not analyzed: JPA mapped-superclass identifier support is limited to conservative source-visible mapped-superclass chains; unresolved, ambiguous, cyclic, or non-source-visible branches are skipped.
- Inferred: tested-subject relations use naming conventions only. Test execution, coverage, assertion behavior, call graphs, and complete subject mapping are not analyzed.
- Not analyzed: connectors, LLM summaries, repository chat, generic RAG, Gradle/Kotlin support, Maven profiles, effective POM reconstruction, dependency graphs, and recursive nested Maven modules are outside this guide.
- Not analyzed: generated sources, generated API reconstruction, classpath-only interfaces, and ambiguous interface endpoint bindings are outside the source-visible interface endpoint support.
- Not analyzed: OpenAPI operation facts are spec-backed declared operations only; runtime implementation matching, source/spec agreement, generated source contents, and client SDK reconstruction are not claimed.
- Not analyzed: v0.3 build/config facts are direct local source observations only. Maven execution, effective POM reconstruction, profile activation, remote dependency resolution, config value interpretation, secret extraction, and default generated-source scanning are not performed.
- Not analyzed: Spring Boot application signals do not prove executable packaging, active profiles, runtime auto-configuration, bean graphs, component scanning results, deployment behavior, or actual process entrypoint behavior.
- Not analyzed: Spring Data repository interface signals do not prove runtime repository registration, query method behavior, database access, or repository-to-entity relations; `entity_relation_status: not_analyzed` is preserved for those inferred signals.
- Not analyzed: v0.5 transaction, scheduling, event listener, and messaging listener facts are annotation-presence change-surface signals only. Transaction propagation, scheduler registration, event delivery, message destinations, broker topology, consumer groups, and delivery semantics are not claimed.
- Not analyzed: v0.5 security surface categories remain outside the current implementation slices unless their subsection status says `analyzed`.
- Uncertain: no endpoint facts were recorded, so HTTP entry points may be absent or outside the currently supported analyzer scope.
- Uncertain: no entity facts were recorded, so persistence mappings may be absent or outside the currently supported analyzer scope.
- Not analyzed: supported Maven test roots were not detected.

## Practical Inspection Order For Coding Agents

1. Start with detected build, module, and layout facts in `pom.xml`.
2. For HTTP behavior, inspect detected endpoint and hidden-surface warning evidence (no evidence paths recorded).
3. For Spring application surface changes, inspect Spring application surface and component evidence in `src/main/java/com/example/config/ConfigurationSurface.java` and avoid assuming runtime repository registration, entity ownership, injection graphs, transaction behavior, scheduler registration, event delivery, or messaging topology.
4. For persistence changes, inspect detected entity evidence (no evidence paths recorded) and treat relationship targets as declared-type-only.
5. For tests, inspect detected test files and inferred tested-subject evidence (no evidence paths recorded); do not treat inferred subjects as coverage proof.
