# Agent Guide

Generated deterministically from `project-map.json` and `evidence-index.jsonl`. The guide generator does not re-analyze source files.

## Detected Project Layout

- Build system: Detected `maven`
- Root build file: Detected `pom.xml`
  - Evidence: `pom.xml:1` (`ev:pom.xml:1-1:build_file:pom.xml`)
- Source roots: Detected `services/billing/src/main/java`, `services/orders/src/main/java`
  - Evidence: recorded in `project-map.json`; no separate source-root evidence IDs are emitted.
- Test roots: Detected `services/billing/src/test/java`, `services/orders/src/test/java`
  - Evidence: recorded in `project-map.json`; no separate test-root evidence IDs are emitted.
- Modules analysis status: `analyzed`
- Module: Detected `module:libraries/shared` (path: `libraries/shared`)
  - POM path: Detected `libraries/shared/pom.xml`
  - Support status: `unsupported`
  - Declaration kind: `root_modules_entry`
  - Declared path: `libraries/shared`
  - Source roots: Not analyzed; no supported production roots were recorded for this module.
  - Test roots: Not analyzed; no supported test roots were recorded for this module.
  - Declaration evidence: `pom.xml:6` (`ev:pom.xml:6-6:build_file:module:libraries/shared`)
  - POM evidence: `libraries/shared/pom.xml:1` (`ev:libraries/shared/pom.xml:1-1:build_file:pom.xml`)
- Module: Detected `module:services/billing` (path: `services/billing`)
  - POM path: Detected `services/billing/pom.xml`
  - Support status: `supported`
  - Declaration kind: `root_modules_entry`
  - Declared path: `services/billing`
  - Source roots: Detected `services/billing/src/main/java`
  - Source roots evidence: recorded in `project-map.json`; no separate production root evidence IDs are emitted.
  - Test roots: Detected `services/billing/src/test/java`
  - Test roots evidence: recorded in `project-map.json`; no separate test root evidence IDs are emitted.
  - Declaration evidence: `pom.xml:4` (`ev:pom.xml:4-4:build_file:module:services/billing`)
  - POM evidence: `services/billing/pom.xml:1` (`ev:services/billing/pom.xml:1-1:build_file:pom.xml`)
- Module: Detected `module:services/missing` (path: `services/missing`)
  - POM path: Not analyzed; no POM path was recorded for this module.
  - Support status: `missing_child_pom`
  - Declaration kind: `root_modules_entry`
  - Declared path: `services/missing`
  - Source roots: Not analyzed; no supported production roots were recorded for this module.
  - Test roots: Not analyzed; no supported test roots were recorded for this module.
  - Declaration evidence: `pom.xml:5` (`ev:pom.xml:5-5:build_file:module:services/missing`)
  - POM evidence: none recorded.
- Module: Detected `module:services/orders` (path: `services/orders`)
  - POM path: Detected `services/orders/pom.xml`
  - Support status: `supported`
  - Declaration kind: `root_modules_entry`
  - Declared path: `services/orders`
  - Source roots: Detected `services/orders/src/main/java`
  - Source roots evidence: recorded in `project-map.json`; no separate production root evidence IDs are emitted.
  - Test roots: Detected `services/orders/src/test/java`
  - Test roots evidence: recorded in `project-map.json`; no separate test root evidence IDs are emitted.
  - Declaration evidence: `pom.xml:3` (`ev:pom.xml:3-3:build_file:module:services/orders`)
  - POM evidence: `services/orders/pom.xml:1` (`ev:services/orders/pom.xml:1-1:build_file:pom.xml`)

## Build And Configuration Orientation

### Module `module:libraries/shared` (path: `libraries/shared`)

- Build/config analysis status: `analyzed`
- Source-visible Maven metadata: group_id `value:not_declared` (value_kind: `not_declared`), artifact_id `value:not_declared` (value_kind: `not_declared`), version `value:not_declared` (value_kind: `not_declared`), packaging `value:not_declared` (value_kind: `not_declared`).
  - Evidence: none recorded.
- Source-visible direct dependencies: Detected none.
- Source-visible dependency-management declarations: Detected none.
- Source-visible direct plugins: Detected none.
- Source-visible plugin-management declarations: Detected none.
- Resource roots: Not analyzed; status `not_detected`.
- Config files: Not analyzed; status `not_detected`.
- Spring Boot application signals: Not analyzed; status `not_detected`.
- Module warnings: Detected 1 warning signal for this module: `maven_module:unsupported_module`. See `Known Uncertainty And Limits` for warning evidence and messages.

### Module `module:services/billing` (path: `services/billing`)

- Build/config analysis status: `analyzed`
- Source-visible Maven metadata: group_id `value:not_declared` (value_kind: `not_declared`), artifact_id `value:not_declared` (value_kind: `not_declared`), version `value:not_declared` (value_kind: `not_declared`), packaging `value:not_declared` (value_kind: `not_declared`).
  - Evidence: none recorded.
- Source-visible direct dependencies: Detected none.
- Source-visible dependency-management declarations: Detected none.
- Source-visible direct plugins: Detected none.
- Source-visible plugin-management declarations: Detected none.
- Resource roots: Not analyzed; status `not_detected`.
- Config files: Not analyzed; status `not_detected`.
- Spring Boot application signals: Detected none.
- Module warnings: Detected 1 warning signal for this module: `hidden_http_surface:repository_rest_resource`. See `Known Uncertainty And Limits` for warning evidence and messages.

### Module `module:services/missing` (path: `services/missing`)

- Build/config analysis status: `not_detected`
- Source-visible Maven metadata: Not analyzed; status `not_detected`.
- Source-visible direct dependencies: Not analyzed; status `not_detected`.
- Source-visible dependency-management declarations: Not analyzed; status `not_detected`.
- Source-visible direct plugins: Not analyzed; status `not_detected`.
- Source-visible plugin-management declarations: Not analyzed; status `not_detected`.
- Resource roots: Not analyzed; status `not_detected`.
- Config files: Not analyzed; status `not_detected`.
- Spring Boot application signals: Not analyzed; status `not_detected`.
- Module warnings: Detected 1 warning signal for this module: `maven_module:missing_child_pom`. See `Known Uncertainty And Limits` for warning evidence and messages.

### Module `module:services/orders` (path: `services/orders`)

- Build/config analysis status: `analyzed`
- Source-visible Maven metadata: group_id `value:not_declared` (value_kind: `not_declared`), artifact_id `value:not_declared` (value_kind: `not_declared`), version `value:not_declared` (value_kind: `not_declared`), packaging `value:not_declared` (value_kind: `not_declared`).
  - Evidence: none recorded.
- Source-visible direct dependencies: Detected none.
- Source-visible dependency-management declarations: Detected none.
- Source-visible direct plugins: Detected 1 direct plugin declarations.
  - Plugin: `group_id:not_declared:openapi-generator-maven-plugin` declaration_kind `direct_plugin`, version `value:not_declared` (value_kind: `not_declared`).
    - Direct execution declarations: none recorded.
    - Configuration signals: none recorded
    - Generator signals: `openapi_swagger_codegen`
  - Evidence: `services/orders/pom.xml:4-6` (`ev:services/orders/pom.xml:4-6:build_file:maven:plugin:000001`)
- Source-visible plugin-management declarations: Detected none.
- Resource roots: Detected 1 standard resource root.
  - Resource root: `main` `services/orders/src/main/resources`
    - Evidence: recorded in `project-map.json`; no separate resource-root evidence IDs are emitted.
- Config files: Detected none.
- Spring Boot application signals: Detected none.
- Module warnings: Detected 4 warning signals for this module: `generated_source:maven_openapi_swagger_codegen_plugin`, `hidden_http_surface:maven_openapi_swagger_codegen_plugin`, `hidden_http_surface:openapi_spec_file`, `hidden_http_surface:repository_rest_resource`. See `Known Uncertainty And Limits` for warning evidence and messages.

## API Surface Interpretation

- API surface analysis status: `analyzed`
- Source-visible Spring MVC endpoint facts are code-backed local source observations from `endpoints[]`; they do not prove complete runtime handler mappings.
- Source-visible interface-declared endpoint facts are code-backed only when the interface mapping and unique concrete binding are both source-visible.
- Declared OpenAPI operations are spec-backed contract facts with `implementation_status: "not_analyzed"`; they are not implemented endpoint facts.
- Generated-source API signals, repository-rest warnings, and hidden HTTP warnings are inspection hints, not endpoint or operation facts.
- LLM output, generated Markdown, release notes, and chat text are never evidence for API surface facts or relations.
- Source-visible direct Spring MVC endpoint IDs: status `analyzed`; detected 2 IDs `endpoint:module:services/billing:com.example.shared.SharedController#health`, `endpoint:module:services/orders:com.example.shared.SharedController#health`.
- Source-visible interface-declared Spring MVC endpoint IDs: status `analyzed`; detected none.
- OpenAPI/Swagger spec files: status `analyzed`; detected 1 local spec file as declared API inputs.
  - Spec file: `services/orders/src/main/resources/openapi.yml` kind `openapi`, format `yaml`, version `3.0.0`.
- Module: Detected `module:services/orders` (path: `services/orders`)
  - Evidence: `services/orders/src/main/resources/openapi.yml:1` (`ev:services/orders/src/main/resources/openapi.yml:1-1:api_spec:openapi`)
- OpenAPI/Swagger operations: status `analyzed`; detected 1 spec-backed declared operation.
  - Declared operation: `GET /orders/health` from `services/orders/src/main/resources/openapi.yml`, operationId `declaredOrdersHealth`, tags `Orders`, implementation_status `not_analyzed`.
- Module: Detected `module:services/orders` (path: `services/orders`)
  - Evidence: `services/orders/src/main/resources/openapi.yml:4` (`ev:services/orders/src/main/resources/openapi.yml:4-4:api_spec:operation%3Aget%3A/orders/health`)
- Generated-source API warning IDs: status `analyzed`; referenced 2 warning IDs `warning:generated_source:maven_openapi_swagger_codegen_plugin:module:services/orders:direct_plugin:decl:000001`, `warning:hidden_http_surface:maven_openapi_swagger_codegen_plugin:module:services/orders:services/orders/pom.xml:openapi-generator-maven-plugin`.
- Repository-rest warning IDs: status `analyzed`; referenced 2 warning IDs `warning:hidden_http_surface:repository_rest_resource:module:services/billing:com.example.shared.SharedRepository`, `warning:hidden_http_surface:repository_rest_resource:module:services/orders:com.example.shared.SharedRepository`.
- Hidden HTTP warning IDs: status `analyzed`; detected none.

## Spring Application Surface

- Spring application surface analysis status: `analyzed`
- Repository stereotype entries are direct `@Repository` annotation observations; they do not prove runtime bean registration or entity ownership.
- Spring Data repository interface entries are inferred source-visible extension signals; they do not prove runtime repositories, query method behavior, database access, or repository-to-entity relations.
- Configuration classes, configuration-properties types, and `@Bean` methods are source-visible Spring configuration signals; they do not prove runtime bean graphs, binding success, config values, bean scopes, lifecycle, proxy behavior, or dependency graphs.
- Repository signals: status `analyzed`; detected none.
- Configuration classes: status `analyzed`; detected none.
- Configuration properties: status `analyzed`; detected none.
- Bean methods: status `analyzed`; detected none.
- Transaction boundaries: status `not_analyzed`; not analyzed in the current v0.5 implementation slice because transaction analyzer has not run.
- Scheduled methods: status `not_analyzed`; not analyzed in the current v0.5 implementation slice because scheduled method analyzer has not run.
- Event listeners: status `not_analyzed`; not analyzed in the current v0.5 implementation slice because event listener analyzer has not run.
- Messaging listener signals: status `not_analyzed`; not analyzed in the current v0.5 implementation slice because messaging listener analyzer has not run.
- Spring Security configuration warnings: status `not_analyzed`; not analyzed in the current v0.5 implementation slice because security configuration warning analysis has not run.

## Detected Spring MVC Endpoints

### `GET /billing/health`

- Module: Detected `module:services/billing` (path: `services/billing`)
- Controller: Detected `com.example.shared.SharedController`
- Handler: Detected `health`
- Mapping source: Detected `direct_handler_method` from `com.example.shared.SharedController#health` with binding `direct`
- HTTP methods: Detected `GET`
- Paths: Detected `/billing/health`
- Request parameters: Detected none.
- Request body: Detected none.
- Response: Detected `String`
  - Evidence: `services/billing/src/main/java/com/example/shared/SharedController.java:3` (`ev:services/billing/src/main/java/com/example/shared/SharedController.java:3-3:com.example.shared.SharedController:@RestController`), `services/billing/src/main/java/com/example/shared/SharedController.java:4` (`ev:services/billing/src/main/java/com/example/shared/SharedController.java:4-4:com.example.shared.SharedController:@RequestMapping`), `services/billing/src/main/java/com/example/shared/SharedController.java:6` (`ev:services/billing/src/main/java/com/example/shared/SharedController.java:6-6:com.example.shared.SharedController#health:@GetMapping`)

### `GET /orders/health`

- Module: Detected `module:services/orders` (path: `services/orders`)
- Controller: Detected `com.example.shared.SharedController`
- Handler: Detected `health`
- Mapping source: Detected `direct_handler_method` from `com.example.shared.SharedController#health` with binding `direct`
- HTTP methods: Detected `GET`
- Paths: Detected `/orders/health`
- Request parameters: Detected none.
- Request body: Detected none.
- Response: Detected `String`
  - Evidence: `services/orders/src/main/java/com/example/shared/SharedController.java:3` (`ev:services/orders/src/main/java/com/example/shared/SharedController.java:3-3:com.example.shared.SharedController:@RestController`), `services/orders/src/main/java/com/example/shared/SharedController.java:4` (`ev:services/orders/src/main/java/com/example/shared/SharedController.java:4-4:com.example.shared.SharedController:@RequestMapping`), `services/orders/src/main/java/com/example/shared/SharedController.java:6` (`ev:services/orders/src/main/java/com/example/shared/SharedController.java:6-6:com.example.shared.SharedController#health:@GetMapping`)

## Detected Spring Components

- Analysis status: `analyzed`

### `com.example.shared.SharedController`

- Module: Detected `module:services/billing` (path: `services/billing`)
- Stereotypes: Detected `@RestController`
  - Evidence: `services/billing/src/main/java/com/example/shared/SharedController.java:3` (`ev:services/billing/src/main/java/com/example/shared/SharedController.java:3-3:com.example.shared.SharedController:@RestController`)

### `com.example.shared.SharedService`

- Module: Detected `module:services/billing` (path: `services/billing`)
- Stereotypes: Detected `@Service`
  - Evidence: `services/billing/src/main/java/com/example/shared/SharedController.java:12` (`ev:services/billing/src/main/java/com/example/shared/SharedController.java:12-12:com.example.shared.SharedService:@Service`)

### `com.example.shared.SharedController`

- Module: Detected `module:services/orders` (path: `services/orders`)
- Stereotypes: Detected `@RestController`
  - Evidence: `services/orders/src/main/java/com/example/shared/SharedController.java:3` (`ev:services/orders/src/main/java/com/example/shared/SharedController.java:3-3:com.example.shared.SharedController:@RestController`)

### `com.example.shared.SharedService`

- Module: Detected `module:services/orders` (path: `services/orders`)
- Stereotypes: Detected `@Service`
  - Evidence: `services/orders/src/main/java/com/example/shared/SharedController.java:12` (`ev:services/orders/src/main/java/com/example/shared/SharedController.java:12-12:com.example.shared.SharedService:@Service`)

## Detected JPA Entities

- Analysis status: `analyzed`

### `com.example.shared.SharedEntity`

- Module: Detected `module:services/billing` (path: `services/billing`)
- Entity: Detected `com.example.shared.SharedEntity`
  - Evidence: `services/billing/src/main/java/com/example/shared/SharedController.java:16` (`ev:services/billing/src/main/java/com/example/shared/SharedController.java:16-16:com.example.shared.SharedEntity:@Entity`)
- Table: Detected none.
- Identifier field: Detected `id` (`Long`) declared by `com.example.shared.SharedEntity` with source_kind `declared`
  - Evidence: `services/billing/src/main/java/com/example/shared/SharedController.java:18` (`ev:services/billing/src/main/java/com/example/shared/SharedController.java:18-18:com.example.shared.SharedEntity:@Id:field:id`)
- Relationships: Detected none.

### `com.example.shared.SharedEntity`

- Module: Detected `module:services/orders` (path: `services/orders`)
- Entity: Detected `com.example.shared.SharedEntity`
  - Evidence: `services/orders/src/main/java/com/example/shared/SharedController.java:16` (`ev:services/orders/src/main/java/com/example/shared/SharedController.java:16-16:com.example.shared.SharedEntity:@Entity`)
- Table: Detected none.
- Identifier field: Detected `id` (`Long`) declared by `com.example.shared.SharedEntity` with source_kind `declared`
  - Evidence: `services/orders/src/main/java/com/example/shared/SharedController.java:18` (`ev:services/orders/src/main/java/com/example/shared/SharedController.java:18-18:com.example.shared.SharedEntity:@Id:field:id`)
- Relationships: Detected none.

## Detected Tests

- Analysis status: `analyzed`

### `com.example.shared.SharedControllerTest`

- Module: Detected `module:services/billing` (path: `services/billing`)
- Test class: Detected `com.example.shared.SharedControllerTest`
  - Evidence: `services/billing/src/test/java/com/example/shared/SharedControllerTest.java:5` (`ev:services/billing/src/test/java/com/example/shared/SharedControllerTest.java:5-5:com.example.shared.SharedControllerTest:test_file`)
- Source: Detected `services/billing/src/test/java/com/example/shared/SharedControllerTest.java`
- Framework signal: Detected `JUnit Jupiter`
  - Evidence: `services/billing/src/test/java/com/example/shared/SharedControllerTest.java:3` (`ev:services/billing/src/test/java/com/example/shared/SharedControllerTest.java:3-3:com.example.shared.SharedControllerTest:import:org.junit.jupiter.api.Test`), `services/billing/src/test/java/com/example/shared/SharedControllerTest.java:6` (`ev:services/billing/src/test/java/com/example/shared/SharedControllerTest.java:6-6:com.example.shared.SharedControllerTest#health:@Test`)
- Inferred tested subject: `com.example.shared.SharedController` in target module `module:services/billing` (path: `services/billing`) (support_type: `inferred`, confidence: `medium`)
  - Evidence: `services/billing/src/test/java/com/example/shared/SharedControllerTest.java:5` (`ev:services/billing/src/test/java/com/example/shared/SharedControllerTest.java:5-5:com.example.shared.SharedControllerTest:test_file`), `services/billing/src/main/java/com/example/shared/SharedController.java:5` (`ev:services/billing/src/main/java/com/example/shared/SharedController.java:5-5:com.example.shared.SharedController:code_symbol`)

### `com.example.shared.SharedControllerTest`

- Module: Detected `module:services/orders` (path: `services/orders`)
- Test class: Detected `com.example.shared.SharedControllerTest`
  - Evidence: `services/orders/src/test/java/com/example/shared/SharedControllerTest.java:5` (`ev:services/orders/src/test/java/com/example/shared/SharedControllerTest.java:5-5:com.example.shared.SharedControllerTest:test_file`)
- Source: Detected `services/orders/src/test/java/com/example/shared/SharedControllerTest.java`
- Framework signal: Detected `JUnit Jupiter`
  - Evidence: `services/orders/src/test/java/com/example/shared/SharedControllerTest.java:3` (`ev:services/orders/src/test/java/com/example/shared/SharedControllerTest.java:3-3:com.example.shared.SharedControllerTest:import:org.junit.jupiter.api.Test`), `services/orders/src/test/java/com/example/shared/SharedControllerTest.java:6` (`ev:services/orders/src/test/java/com/example/shared/SharedControllerTest.java:6-6:com.example.shared.SharedControllerTest#health:@Test`)
- Inferred tested subject: `com.example.shared.SharedController` in target module `module:services/orders` (path: `services/orders`) (support_type: `inferred`, confidence: `medium`)
  - Evidence: `services/orders/src/test/java/com/example/shared/SharedControllerTest.java:5` (`ev:services/orders/src/test/java/com/example/shared/SharedControllerTest.java:5-5:com.example.shared.SharedControllerTest:test_file`), `services/orders/src/main/java/com/example/shared/SharedController.java:5` (`ev:services/orders/src/main/java/com/example/shared/SharedController.java:5-5:com.example.shared.SharedController:code_symbol`)

## Known Uncertainty And Limits

- Warning: `generated_source` signal `maven_openapi_swagger_codegen_plugin` for module `module:services/orders` (path: `services/orders`) at `services/orders/pom.xml`. Maven OpenAPI/Swagger code generation plugin declaration detected; the analyzer does not run code generation, scan generated sources by default, or create endpoint/API facts from this build signal.
  - Evidence: `services/orders/pom.xml:5` (`ev:services/orders/pom.xml:5-5:build_file:maven:plugin:000001:artifactId`)
- Warning: `hidden_http_surface` signal `maven_openapi_swagger_codegen_plugin` for module `module:services/orders` (path: `services/orders`) at `services/orders/pom.xml`. Maven OpenAPI/Swagger code generation plugin signal detected; the analyzer does not run generation or scan generated sources by default.
  - Evidence: `services/orders/pom.xml:5` (`ev:services/orders/pom.xml:5-5:build_file:openapi-generator-maven-plugin`)
- Warning: `hidden_http_surface` signal `openapi_spec_file` for module `module:services/orders` (path: `services/orders`) at `services/orders/src/main/resources/openapi.yml`. OpenAPI/Swagger spec file detected by filename; declared operations, when supported, are reported separately under api\_surface.openapi.operations, and this warning does not reconstruct generated APIs.
  - Evidence: `services/orders/src/main/resources/openapi.yml` (`ev:services/orders/src/main/resources/openapi.yml:unknown:config_file:openapi.yml`)
- Warning: `hidden_http_surface` signal `repository_rest_resource` for module `module:services/billing` (path: `services/billing`) at `services/billing/src/main/java/com/example/shared/SharedController.java`. Direct @RepositoryRestResource detected; the analyzer warns about possible Spring Data REST HTTP surface but does not expand endpoints.
  - Evidence: `services/billing/src/main/java/com/example/shared/SharedController.java:22` (`ev:services/billing/src/main/java/com/example/shared/SharedController.java:22-22:com.example.shared.SharedRepository:@RepositoryRestResource`)
- Warning: `hidden_http_surface` signal `repository_rest_resource` for module `module:services/orders` (path: `services/orders`) at `services/orders/src/main/java/com/example/shared/SharedController.java`. Direct @RepositoryRestResource detected; the analyzer warns about possible Spring Data REST HTTP surface but does not expand endpoints.
  - Evidence: `services/orders/src/main/java/com/example/shared/SharedController.java:22` (`ev:services/orders/src/main/java/com/example/shared/SharedController.java:22-22:com.example.shared.SharedRepository:@RepositoryRestResource`)
- Warning: `maven_module` signal `missing_child_pom` for module `module:services/missing` (path: `services/missing`) at `pom.xml`. Maven module declared in root pom.xml does not have a child pom.xml; v0.2 does not analyze this module.
  - Evidence: `pom.xml:5` (`ev:pom.xml:5-5:build_file:module:services/missing`)
- Warning: `maven_module` signal `unsupported_module` for module `module:libraries/shared` (path: `libraries/shared`) at `libraries/shared/pom.xml`. Maven module has a child pom.xml but no supported Java source, test, or resource roots; the analyzer does not inspect this module.
  - Evidence: `pom.xml:6` (`ev:pom.xml:6-6:build_file:module:libraries/shared`), `libraries/shared/pom.xml:1` (`ev:libraries/shared/pom.xml:1-1:build_file:pom.xml`)
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
- Not analyzed: v0.5 behavior, messaging, and security surface categories remain outside the current repository/configuration implementation slices unless their subsection status says `analyzed`.

## Practical Inspection Order For Coding Agents

1. Start with detected build, module, and layout facts in `pom.xml`, `libraries/shared/pom.xml`, `services/billing/pom.xml`, `services/orders/pom.xml`.
2. For HTTP behavior, inspect detected endpoint and hidden-surface warning evidence in `services/billing/src/main/java/com/example/shared/SharedController.java`, `services/orders/src/main/java/com/example/shared/SharedController.java`, `services/orders/src/main/resources/openapi.yml`, `services/orders/pom.xml`.
3. For Spring application surface changes, inspect repository surface and component evidence in `services/billing/src/main/java/com/example/shared/SharedController.java`, `services/orders/src/main/java/com/example/shared/SharedController.java` and avoid assuming runtime repository registration, entity ownership, or injection graphs.
4. For persistence changes, inspect detected entity evidence in `services/billing/src/main/java/com/example/shared/SharedController.java`, `services/orders/src/main/java/com/example/shared/SharedController.java` and treat relationship targets as declared-type-only.
5. For tests, inspect detected test files and inferred tested-subject evidence in `services/billing/src/test/java/com/example/shared/SharedControllerTest.java`, `services/billing/src/main/java/com/example/shared/SharedController.java`, `services/orders/src/test/java/com/example/shared/SharedControllerTest.java`, `services/orders/src/main/java/com/example/shared/SharedController.java`; do not treat inferred subjects as coverage proof.
