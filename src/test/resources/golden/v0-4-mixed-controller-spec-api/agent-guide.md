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
- Source-visible Maven metadata: group_id `com.example` (value_kind: `literal`), artifact_id `v04-mixed-controller-spec-api` (value_kind: `literal`), version `1.0.0` (value_kind: `literal`), packaging `value:not_declared` (value_kind: `not_declared`).
  - Evidence: `pom.xml:3` (`ev:pom.xml:3-3:build_file:maven:project:groupId`), `pom.xml:4` (`ev:pom.xml:4-4:build_file:maven:project:artifactId`), `pom.xml:5` (`ev:pom.xml:5-5:build_file:maven:project:version`)
- Source-visible direct dependencies: Detected none.
- Source-visible dependency-management declarations: Detected none.
- Source-visible direct plugins: Detected none.
- Source-visible plugin-management declarations: Detected none.
- Resource roots: Detected 1 standard resource root.
  - Resource root: `main` `src/main/resources`
    - Evidence: recorded in `project-map.json`; no separate resource-root evidence IDs are emitted.
- Config files: Detected none.
- Spring Boot application signals: Detected none.
- Module warnings: Detected 1 warning signal for this module: `hidden_http_surface:openapi_spec_file`. See `Known Uncertainty And Limits` for warning evidence and messages.

## API Surface Interpretation

- API surface analysis status: `analyzed`
- Source-visible Spring MVC endpoint facts are code-backed local source observations from `endpoints[]`; they do not prove complete runtime handler mappings.
- Source-visible interface-declared endpoint facts are code-backed only when the interface mapping and unique concrete binding are both source-visible.
- Declared OpenAPI operations are spec-backed contract facts with `implementation_status: "not_analyzed"`; they are not implemented endpoint facts.
- Generated-source API signals, repository-rest warnings, and hidden HTTP warnings are inspection hints, not endpoint or operation facts.
- LLM output, generated Markdown, release notes, and chat text are never evidence for API surface facts or relations.
- Source-visible direct Spring MVC endpoint IDs: status `analyzed`; detected 1 ID `endpoint:com.example.mixed.OrderController#getOrder`.
- Source-visible interface-declared Spring MVC endpoint IDs: status `analyzed`; detected none.
- OpenAPI/Swagger spec files: status `analyzed`; detected 1 local spec file as declared API inputs.
  - Spec file: `src/main/resources/openapi.yml` kind `openapi`, format `yaml`, version `3.0.3`.
- Module: Detected `module:.` (path: `.`)
  - Evidence: `src/main/resources/openapi.yml:1` (`ev:src/main/resources/openapi.yml:1-1:api_spec:openapi`)
- OpenAPI/Swagger operations: status `analyzed`; detected 2 spec-backed declared operations.
  - Declared operation: `POST /orders` from `src/main/resources/openapi.yml`, operationId `createOrder`, tags `Orders`, implementation_status `not_analyzed`.
- Module: Detected `module:.` (path: `.`)
  - Evidence: `src/main/resources/openapi.yml:9` (`ev:src/main/resources/openapi.yml:9-9:api_spec:operation%3Apost%3A/orders`)
  - Declared operation: `GET /orders/{id}` from `src/main/resources/openapi.yml`, operationId `getOrder`, tags `Orders`, implementation_status `not_analyzed`.
- Module: Detected `module:.` (path: `.`)
  - Evidence: `src/main/resources/openapi.yml:4` (`ev:src/main/resources/openapi.yml:4-4:api_spec:operation%3Aget%3A/orders/{id}`)
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

### `GET /orders/{id}`

- Module: Detected `module:.` (path: `.`)
- Controller: Detected `com.example.mixed.OrderController`
- Handler: Detected `getOrder`
- Mapping source: Detected `direct_handler_method` from `com.example.mixed.OrderController#getOrder` with binding `direct`
- HTTP methods: Detected `GET`
- Paths: Detected `/orders/{id}`
- Request parameters: Detected none.
- Request body: Detected none.
- Response: Detected `OrderDto`
  - Evidence: `src/main/java/com/example/mixed/OrderController.java:6` (`ev:src/main/java/com/example/mixed/OrderController.java:6-6:com.example.mixed.OrderController:@RestController`), `src/main/java/com/example/mixed/OrderController.java:8` (`ev:src/main/java/com/example/mixed/OrderController.java:8-8:com.example.mixed.OrderController#getOrder:@GetMapping`)

## Detected Spring Components

- Analysis status: `analyzed`

### `com.example.mixed.OrderController`

- Module: Detected `module:.` (path: `.`)
- Stereotypes: Detected `@RestController`
  - Evidence: `src/main/java/com/example/mixed/OrderController.java:6` (`ev:src/main/java/com/example/mixed/OrderController.java:6-6:com.example.mixed.OrderController:@RestController`)

## Detected Tests

- Analysis status: `not_detected`
- Not analyzed: no supported test root was detected.

## Known Uncertainty And Limits

- Warning: `hidden_http_surface` signal `openapi_spec_file` for module `module:.` (path: `.`) at `src/main/resources/openapi.yml`. OpenAPI/Swagger spec file detected by filename; declared operations, when supported, are reported separately under api\_surface.openapi.operations, and this warning does not reconstruct generated APIs.
  - Evidence: `src/main/resources/openapi.yml` (`ev:src/main/resources/openapi.yml:unknown:config_file:openapi.yml`)
- Not analyzed: Spring runtime behavior such as component scanning, dependency injection graphs, bean lifecycle, scopes, and conditional configuration is not represented by `components.items`.
- Uncertain: JPA relationship targets preserve `target_resolution: declared_type_only` and `uncertainty: target_type_not_resolved`; no symbol solving or ORM runtime behavior is claimed.
- Source-visible: JPA relationship metadata such as `mappedBy`, `@JoinColumn`, `@JoinTable`, `optional`, `fetch`, `cascade`, and `orphanRemoval` is reported only when direct annotation attributes are supported; foreign keys, join tables, ownership correctness, fetch behavior, cascade behavior, and database constraints are not claimed.
- Not analyzed: JPA mapped-superclass identifier support is limited to conservative source-visible mapped-superclass chains; unresolved, ambiguous, cyclic, or non-source-visible branches are skipped.
- Partial: JPA embedded and composite identifier support is limited to direct source-visible `@Embeddable`, `@Embedded`, `@EmbeddedId`, and `@IdClass` signals. Embedded targets are linked only when a unique local `@Embeddable` can be matched; `@IdClass` field matching and composite-key semantics are not analyzed.
- Inferred: tested-subject relations use naming conventions only. Test execution, coverage, assertion behavior, call graphs, and complete subject mapping are not analyzed.
- Not analyzed: connectors, LLM summaries, repository chat, generic RAG, Gradle/Kotlin support, Maven profiles, effective POM reconstruction, dependency graphs, and recursive nested Maven modules are outside this guide.
- Not analyzed: generated sources, generated API reconstruction, classpath-only interfaces, and ambiguous interface endpoint bindings are outside the source-visible interface endpoint support.
- Not analyzed: OpenAPI operation facts are spec-backed declared operations only; runtime implementation matching, source/spec agreement, generated source contents, and client SDK reconstruction are not claimed.
- Not analyzed: v0.3 build/config facts are direct local source observations only. Maven execution, effective POM reconstruction, profile activation, remote dependency resolution, config value interpretation, secret extraction, and default generated-source scanning are not performed.
- Not analyzed: Spring Boot application signals do not prove executable packaging, active profiles, runtime auto-configuration, bean graphs, component scanning results, deployment behavior, or actual process entrypoint behavior.
- Not analyzed: Spring Data repository interface signals do not prove runtime repository registration, query method behavior, database access, or runtime repository/entity verification. Repository/entity links, when present, are bounded inferred Spring Data generic relations with explicit `entity_relation_status` values.
- Not analyzed: JPA field metadata is limited to supported direct field-level source-visible annotations. It is not a complete persistent-property inventory, does not support getter/property access in this slice, and does not fill missing annotation attributes from JPA provider defaults.
- Not analyzed: v0.5 transaction, scheduling, event listener, and messaging listener facts are annotation-presence change-surface signals only. Transaction propagation, scheduler registration, event delivery, message destinations, broker topology, consumer groups, and delivery semantics are not claimed.
- Not analyzed: Security policy, endpoint protection state, authentication behavior, authorization behavior, filter-chain ordering, vulnerabilities, and correctness are not claimed. v0.5 Spring Security configuration warnings are bounded source-visible inspection hints only.
- Uncertain: no entity facts were recorded, so persistence mappings may be absent or outside the currently supported analyzer scope.
- Not analyzed: supported Maven test roots were not detected.

## Practical Inspection Order For Coding Agents

1. Start with detected build, module, and layout facts in `pom.xml`.
2. For HTTP behavior, inspect detected endpoint and hidden-surface warning evidence in `src/main/java/com/example/mixed/OrderController.java`, `src/main/resources/openapi.yml`.
3. For Spring application surface changes, inspect Spring application surface and component evidence in `src/main/java/com/example/mixed/OrderController.java` and avoid assuming runtime repository registration, entity ownership, injection graphs, transaction behavior, scheduler registration, event delivery, or messaging topology.
4. For tests, inspect detected test files and inferred tested-subject evidence (no evidence paths recorded); do not treat inferred subjects as coverage proof.
