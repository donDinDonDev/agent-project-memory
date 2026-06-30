# Agent Guide

Generated deterministically from `project-map.json` and `evidence-index.jsonl`. The guide generator does not re-analyze source files.

## Read This First

- Open `artifact-set.json` before this guide and respect its artifact authority labels.
- Use this guide as deterministic orientation only. It is not evidence and does not re-analyze source files.
- For large or unknown outputs, prefer `query <path> agent-context`, targeted query commands, focused `project-map.json` selection, exact `evidence-index.jsonl` lookup, and source readback instead of reading every row.
- Size note: this guide is `small-guide` (about `60 KiB`, `464` rendered lines); known generator inputs are `project-map.json` `61 KiB` and `evidence-index.jsonl` `21 KiB`.

## Trust And Verification Legend

Trust and verification legend:
- Use `evidence-index.jsonl` as the authoritative source-backed evidence ledger; verify important claims against its exact records and the repository source locations they cite.
- Generated project facts: `project-map.json` facts; verify important use through their evidence IDs.
- Deterministic presentation: this guide, `endpoints.md`, and query stdout help with orientation; they are not evidence.
- Navigation, provenance, or execution metadata: `artifact-set.json`, `project-graph.json`, `source-registry.json`, profiles, LLM/provider AI output, cache, workspace, adapter output, release metadata, security reports, and downstream-agent output are non-evidence unless a later public contract explicitly changes that.
- Before code changes, review findings, public/security/release wording, or architecture decisions, resolve exact evidence IDs and read the cited source.

## Practical Inspection Order For Coding Agents

1. Start with detected build, module, and layout facts in `settings.gradle.kts`, `build.gradle.kts`, `src/main/resources/application.yml`, `src/main/java/com/example/gradle/GradleApplication.java`.
2. For HTTP behavior, inspect detected endpoint and hidden-surface warning evidence in `src/main/java/com/example/gradle/web/OrderController.java`, `src/main/resources/openapi.yml`.
3. For Spring application surface changes, inspect Spring application surface and component evidence in `src/main/java/com/example/gradle/repository/OrderRepository.java`, `src/main/java/com/example/gradle/domain/Order.java`, `src/main/java/com/example/gradle/config/GradleConfiguration.java`, `src/main/java/com/example/gradle/security/SecurityConfig.java`, `src/main/java/com/example/gradle/service/OrderService.java`, ... and 2 more evidence paths in `evidence-index.jsonl` and avoid assuming runtime repository registration, entity ownership, injection graphs, transaction behavior, scheduler registration, event delivery, or messaging topology.
4. For persistence changes, inspect detected entity evidence in `src/main/java/com/example/gradle/domain/Customer.java`, `src/main/java/com/example/gradle/domain/Order.java` and treat field metadata as source-visible annotations only, not runtime schema, provider defaults, or complete access-strategy reconstruction; relationship targets remain declared-type-only.
5. For tests, inspect detected test files and tested-subject relation/status evidence in `src/test/java/com/example/gradle/web/OrderControllerTest.java`, `src/main/java/com/example/gradle/web/OrderController.java`, `src/main/java/com/example/gradle/service/OrderService.java`; do not treat inferred or statused subjects as coverage proof, and do not treat Spring test slice or mock annotations as execution or runtime behavior proof.
6. For quality and change-risk planning, inspect quality signal evidence in `src/main/java/com/example/gradle/domain/Customer.java`, `src/main/java/com/example/gradle/domain/Order.java`, `src/main/java/com/example/gradle/repository/OrderRepository.java`, `src/main/java/com/example/gradle/service/OrderService.java`, `src/main/java/com/example/gradle/messaging/OrderListener.java`, ... and 2 more evidence paths in `evidence-index.jsonl` and treat `no_obvious_test`, warning-oriented, and uncertain statuses as planning hints only, not coverage, runtime, correctness, vulnerability, or business-priority claims.
7. For local documentation context, inspect accepted document evidence and reconciliation hints in `README.md` and treat document paths, heading refs, chunk refs, and reconciliation rows as navigation aids only; prefer code-backed facts for implementation truth.

## Project Memory Overview

- Build/layout: build system `gradle`, modules `1`, source roots `1`, test roots `1`.
- Source-backed fact surfaces: endpoints `1`, direct Spring components `6`, Spring application surface rows `13`, entities `2`, embeddables `0`, tests `1`.
- Planning/navigation surfaces: warnings `3`, quality/change-risk hints `17`, local documents `1`, document reconciliation hints `0`.
- Evidence records: `52` records in `evidence-index.jsonl`; this overview is presentation only.
- Size band: `small-guide`; large detailed sections should be selected by task and verified through exact evidence IDs.

## Known Uncertainty Snapshot

- Warnings: `3` warning rows; warning evidence and messages stay in the detailed limits section.
- Inferred or statused rows: `23` rows; keep `inferred`, `ambiguous`, `not_detected`, `unsupported`, and similar labels attached to any use.
- Explicit uncertainty labels: `18` values; preserve those caveats with the cited evidence.
- Not analyzed/out-of-scope status markers: `7`; runtime behavior, generated-source contents, test execution/coverage, source/spec agreement, connectors, and LLM summaries remain outside source-backed evidence unless a later contract says otherwise.

## Not Represented In This Scan

- No represented rows for: `generated-source root metadata`. This means the current deterministic scan emitted no rows for those surfaces; it does not prove the runtime behavior is absent outside the supported analyzer scope.

## Detected Project Layout

- Build system: Detected `gradle`
- Root build file: Detected `settings.gradle.kts`
  - Evidence: `settings.gradle.kts:1` (`ev:settings.gradle.kts:1-1:build_file:gradle:settings`), `build.gradle.kts:1` (`ev:build.gradle.kts:1-1:build_file:gradle:build`)
- Root build files: Detected `gradle:settings:settings.gradle.kts`, `gradle:root_project_build:build.gradle.kts`
  - Root build-file evidence: `settings.gradle.kts:1` (`ev:settings.gradle.kts:1-1:build_file:gradle:settings`), `build.gradle.kts:1` (`ev:build.gradle.kts:1-1:build_file:gradle:build`)
- Source roots: Detected `src/main/java`
  - Evidence: recorded in `project-map.json`; no separate source-root evidence IDs are emitted.
- Test roots: Detected `src/test/java`
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
  - Test roots: Detected `src/test/java`
  - Test roots evidence: recorded in `project-map.json`; no separate test root evidence IDs are emitted.
  - Declaration evidence: none recorded.
  - POM evidence: none recorded.
  - Gradle build-file evidence: `settings.gradle.kts:1` (`ev:settings.gradle.kts:1-1:build_file:gradle:settings`), `build.gradle.kts:1` (`ev:build.gradle.kts:1-1:build_file:gradle:build`)

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
  - Resource root: `main` `src/main/resources`
    - Evidence: recorded in `project-map.json`; no separate resource-root evidence IDs are emitted.
- Config files: Detected 1 path-only supported config file; config contents, keys, and values are not rendered.
  - Config file: `src/main/resources/application.yml` kind `spring_application`, format `yaml`.
  - Evidence: `src/main/resources/application.yml` (`ev:src/main/resources/application.yml:unknown:config_file:application.yml`)
- Spring Boot application signals: Detected 1 direct `@SpringBootApplication` class signal.
  - Spring Boot application: Detected `com.example.gradle.GradleApplication` at `src/main/java/com/example/gradle/GradleApplication.java` with signal `spring_boot_application_with_main_method`.
    - Main method: Detected source-visible `main` method.
  - Evidence: `src/main/java/com/example/gradle/GradleApplication.java:6` (`ev:src/main/java/com/example/gradle/GradleApplication.java:6-6:com.example.gradle.GradleApplication:@SpringBootApplication`), `src/main/java/com/example/gradle/GradleApplication.java:9` (`ev:src/main/java/com/example/gradle/GradleApplication.java:9-9:com.example.gradle.GradleApplication#main:code_symbol`)
- Module warnings: Detected 3 warning signals for this module: `hidden_http_surface:openapi_spec_file`, `spring_security:security_configuration_annotation`, `spring_security:security_filter_chain_bean`. See `Detailed Known Uncertainty And Limits` for warning evidence and messages.

## API Surface Interpretation

- API surface analysis status: `analyzed`
- Source-visible Spring MVC endpoint facts are code-backed local source observations from `endpoints[]`; they do not prove complete runtime handler mappings.
- Source-visible interface-declared endpoint facts are code-backed only when the interface mapping and unique concrete binding are both source-visible.
- Declared OpenAPI operations are spec-backed contract facts with `implementation_status: "not_analyzed"`; they are not implemented endpoint facts.
- Generated-source API signals, repository-rest warnings, and hidden HTTP warnings are inspection hints, not endpoint or operation facts.
- LLM output, generated Markdown, release notes, and chat text are never evidence for API surface facts or relations.
- Source-visible direct Spring MVC endpoint IDs: status `analyzed`; detected 1 ID `endpoint:com.example.gradle.web.OrderController#getOrder`.
- Source-visible interface-declared Spring MVC endpoint IDs: status `analyzed`; detected none.
- OpenAPI/Swagger spec files: status `analyzed`; detected 1 local spec file as declared API inputs.
  - Spec file: `src/main/resources/openapi.yml` kind `openapi`, format `yaml`, version `3.0.3`.
- Module: Detected `module:.` (path: `.`)
  - Evidence: `src/main/resources/openapi.yml:1` (`ev:src/main/resources/openapi.yml:1-1:api_spec:openapi`)
- OpenAPI/Swagger operations: status `analyzed`; detected 1 spec-backed declared operation.
  - Declared operation: `GET /orders/{id}` from `src/main/resources/openapi.yml`, operationId `getOrderDocumented`, tags `Orders`, implementation_status `not_analyzed`.
- Module: Detected `module:.` (path: `.`)
  - Evidence: `src/main/resources/openapi.yml:7` (`ev:src/main/resources/openapi.yml:7-7:api_spec:operation%3Aget%3A/orders/{id}`)
- Generated-source API warning IDs: status `analyzed`; detected none.
- Repository-rest warning IDs: status `analyzed`; detected none.
- Hidden HTTP warning IDs: status `analyzed`; detected none.

## Detected Spring MVC Endpoints

- Endpoint summary: detected 1 source-visible Spring MVC endpoint fact.

### `GET /orders/{id}`

- Module: Detected `module:.` (path: `.`)
- Controller: Detected `com.example.gradle.web.OrderController`
- Handler: Detected `getOrder`
- Mapping source: Detected `direct_handler_method` from `com.example.gradle.web.OrderController#getOrder` with binding `direct`
- HTTP methods: Detected `GET`
- Paths: Detected `/orders/{id}`
- Request parameters: Detected `path_variable:id (Long)`
- Request body: Detected none.
- Response: Detected `OrderDto`
  - Evidence: `src/main/java/com/example/gradle/web/OrderController.java:9` (`ev:src/main/java/com/example/gradle/web/OrderController.java:9-9:com.example.gradle.web.OrderController:@RestController`), `src/main/java/com/example/gradle/web/OrderController.java:10` (`ev:src/main/java/com/example/gradle/web/OrderController.java:10-10:com.example.gradle.web.OrderController:@RequestMapping`), `src/main/java/com/example/gradle/web/OrderController.java:19` (`ev:src/main/java/com/example/gradle/web/OrderController.java:19-19:com.example.gradle.web.OrderController#getOrder:@GetMapping`), `src/main/java/com/example/gradle/web/OrderController.java:20` (`ev:src/main/java/com/example/gradle/web/OrderController.java:20-20:com.example.gradle.web.OrderController#getOrder:@PathVariable:parameter:0:id`)

## Spring Application Surface

- Spring application surface analysis status: `analyzed`
- Repository stereotype entries are direct `@Repository` annotation observations; they do not prove runtime bean registration or entity ownership.
- Spring Data repository interface entries are inferred source-visible extension signals; repository/entity relation rows, when present, are inferred generic links. They do not prove runtime repositories, query method behavior, database access, or runtime repository/entity verification.
- Configuration classes, configuration-properties types, and `@Bean` methods are source-visible Spring configuration signals; they do not prove runtime bean graphs, binding success, config values, bean scopes, lifecycle, proxy behavior, or dependency graphs.
- Transaction, scheduled, event listener, and messaging listener entries are source-visible operational change-surface signals; they do not prove runtime transaction behavior, scheduler registration, event delivery, message destinations, or broker topology.
- Spring Security configuration warnings are inspection hints and change-risk signals; they do not prove security policy, endpoint protection, authentication behavior, authorization behavior, vulnerability, or correctness.
- Subsection statuses: repositories `analyzed`, configuration classes `analyzed`, configuration properties `analyzed`, bean methods `analyzed`, transaction boundaries `analyzed`, scheduled methods `analyzed`, event listeners `analyzed`, messaging listeners `analyzed`, security warnings `analyzed`.

### Module `module:.` (path: `.`)

- Extracted facts: detected 10 source-visible facts.
  - `spring_repository_stereotype`: `com.example.gradle.repository.OrderRepository` (support_type: `extracted`, repository_signal: `direct_repository_stereotype`).
    - Source: `src/main/java/com/example/gradle/repository/OrderRepository.java`
    - Evidence: `src/main/java/com/example/gradle/repository/OrderRepository.java:7` (`ev:src/main/java/com/example/gradle/repository/OrderRepository.java:7-7:com.example.gradle.repository.OrderRepository:@Repository`)
  - `spring_configuration_class`: `com.example.gradle.config.GradleConfiguration` (support_type: `extracted`, configuration_signal: `direct_configuration_class`).
    - Source: `src/main/java/com/example/gradle/config/GradleConfiguration.java`
    - Evidence: `src/main/java/com/example/gradle/config/GradleConfiguration.java:9` (`ev:src/main/java/com/example/gradle/config/GradleConfiguration.java:9-9:com.example.gradle.config.GradleConfiguration:@Configuration`)
  - `spring_configuration_class`: `com.example.gradle.security.SecurityConfig` (support_type: `extracted`, configuration_signal: `direct_configuration_class`).
    - Source: `src/main/java/com/example/gradle/security/SecurityConfig.java`
    - Evidence: `src/main/java/com/example/gradle/security/SecurityConfig.java:9` (`ev:src/main/java/com/example/gradle/security/SecurityConfig.java:9-9:com.example.gradle.security.SecurityConfig:@Configuration`)
  - `spring_configuration_properties_type`: `com.example.gradle.config.GradleConfiguration.OrderProperties` (support_type: `extracted`, configuration_properties_signal: `direct_configuration_properties_type`).
    - Source: `src/main/java/com/example/gradle/config/GradleConfiguration.java`
    - Evidence: `src/main/java/com/example/gradle/config/GradleConfiguration.java:20` (`ev:src/main/java/com/example/gradle/config/GradleConfiguration.java:20-20:com.example.gradle.config.GradleConfiguration.OrderProperties:@ConfigurationProperties`)
  - `spring_bean_method`: `com.example.gradle.config.GradleConfiguration#orderClock` (support_type: `extracted`, bean_signal: `direct_bean_method`).
    - Source: `src/main/java/com/example/gradle/config/GradleConfiguration.java`
    - Evidence: `src/main/java/com/example/gradle/config/GradleConfiguration.java:12` (`ev:src/main/java/com/example/gradle/config/GradleConfiguration.java:12-12:com.example.gradle.config.GradleConfiguration#orderClock:@Bean`)
  - ... and 5 more Spring application surface extracted facts in `project-map.json`.
- Inferred signals: detected 1 source-visible signal.
  - `spring_data_repository_interface_signal`: `com.example.gradle.repository.OrderRepository` extends `org.springframework.data.jpa.repository.JpaRepository` (support_type: `inferred`, repository_signal: `spring_data_repository_interface_extension`).
    - Source: `src/main/java/com/example/gradle/repository/OrderRepository.java`
    - Evidence: `src/main/java/com/example/gradle/repository/OrderRepository.java:7-8` (`ev:src/main/java/com/example/gradle/repository/OrderRepository.java:7-8:com.example.gradle.repository.OrderRepository:com.example.gradle.repository.OrderRepository`), `src/main/java/com/example/gradle/repository/OrderRepository.java:8` (`ev:src/main/java/com/example/gradle/repository/OrderRepository.java:8-8:com.example.gradle.repository.OrderRepository:extends:org.springframework.data.jpa.repository.JpaRepository`)
- Inferred repository/entity relations: detected 1 source-visible Spring Data generic relation.
  - `com.example.gradle.repository.OrderRepository` -> `com.example.gradle.domain.Order` (entity_relation_status: `inferred`, relation_type: `repository_entity_generic`, support_type: `inferred`, generic_type: `com.example.gradle.domain.Order`, confidence: `medium`, uncertainty: `null`).
    - Evidence: `src/main/java/com/example/gradle/repository/OrderRepository.java:8` (`ev:src/main/java/com/example/gradle/repository/OrderRepository.java:8-8:com.example.gradle.repository.OrderRepository:extends:org.springframework.data.jpa.repository.JpaRepository`), `src/main/java/com/example/gradle/domain/Order.java:12` (`ev:src/main/java/com/example/gradle/domain/Order.java:12-12:com.example.gradle.domain.Order:@Entity`), `src/main/java/com/example/gradle/domain/Order.java:13` (`ev:src/main/java/com/example/gradle/domain/Order.java:13-13:com.example.gradle.domain.Order:@Table`)
- Uncertain/not-analyzed statuses: detected 3 explicit statuses.
  - `com.example.gradle.config.GradleConfiguration.OrderProperties`: `binding_status` is `not_analyzed`; no runtime binding success or config values are claimed.
    - Evidence: `src/main/java/com/example/gradle/config/GradleConfiguration.java:20` (`ev:src/main/java/com/example/gradle/config/GradleConfiguration.java:20-20:com.example.gradle.config.GradleConfiguration.OrderProperties:@ConfigurationProperties`)
  - `com.example.gradle.config.GradleConfiguration#orderClock`: `bean_name_status` is `not_analyzed`; no effective runtime bean name is claimed.
    - Evidence: `src/main/java/com/example/gradle/config/GradleConfiguration.java:12` (`ev:src/main/java/com/example/gradle/config/GradleConfiguration.java:12-12:com.example.gradle.config.GradleConfiguration#orderClock:@Bean`)
  - `com.example.gradle.security.SecurityConfig#filterChain`: `bean_name_status` is `not_analyzed`; no effective runtime bean name is claimed.
    - Evidence: `src/main/java/com/example/gradle/security/SecurityConfig.java:13` (`ev:src/main/java/com/example/gradle/security/SecurityConfig.java:13-13:com.example.gradle.security.SecurityConfig#filterChain:@Bean`)
- Warnings: referenced 2 inspection hint/change-risk warnings.
  - Warning `spring_security`: inspection hint `security_configuration_annotation` (warning_id: `warning:spring_security:security_configuration_annotation:com.example.gradle.security.SecurityConfig:annotation:enable_web_security:decl:000001`) at `src/main/java/com/example/gradle/security/SecurityConfig.java`. Spring Security configuration annotation detected as a source-visible inspection hint and change-risk signal; the analyzer does not evaluate security policy, endpoint protection, authentication, authorization, filter-chain order, vulnerability, or correctness.
    - Evidence: `src/main/java/com/example/gradle/security/SecurityConfig.java:10` (`ev:src/main/java/com/example/gradle/security/SecurityConfig.java:10-10:com.example.gradle.security.SecurityConfig:@EnableWebSecurity`)
  - Warning `spring_security`: inspection hint `security_filter_chain_bean` (warning_id: `warning:spring_security:security_filter_chain_bean:com.example.gradle.security.SecurityConfig#filterChain:decl:000001`) at `src/main/java/com/example/gradle/security/SecurityConfig.java`. SecurityFilterChain @Bean method detected as a source-visible Spring Security configuration inspection hint and change-risk signal; the analyzer does not evaluate security policy, endpoint protection, authentication, authorization, filter-chain order, vulnerability, or correctness.
    - Evidence: `src/main/java/com/example/gradle/security/SecurityConfig.java:13` (`ev:src/main/java/com/example/gradle/security/SecurityConfig.java:13-13:com.example.gradle.security.SecurityConfig#filterChain:@Bean`), `src/main/java/com/example/gradle/security/SecurityConfig.java:14` (`ev:src/main/java/com/example/gradle/security/SecurityConfig.java:14-14:com.example.gradle.security.SecurityConfig#filterChain:return:SecurityFilterChain`)

## Detected Spring Components

- Analysis status: `analyzed`
- Component summary: detected 6 direct Spring stereotype components.

### `com.example.gradle.config.GradleConfiguration`

- Module: Detected `module:.` (path: `.`)
- Stereotypes: Detected `@Configuration`
  - Evidence: `src/main/java/com/example/gradle/config/GradleConfiguration.java:9` (`ev:src/main/java/com/example/gradle/config/GradleConfiguration.java:9-9:com.example.gradle.config.GradleConfiguration:@Configuration`)

### `com.example.gradle.messaging.OrderListener`

- Module: Detected `module:.` (path: `.`)
- Stereotypes: Detected `@Component`
  - Evidence: `src/main/java/com/example/gradle/messaging/OrderListener.java:6` (`ev:src/main/java/com/example/gradle/messaging/OrderListener.java:6-6:com.example.gradle.messaging.OrderListener:@Component`)

### `com.example.gradle.repository.OrderRepository`

- Module: Detected `module:.` (path: `.`)
- Stereotypes: Detected `@Repository`
  - Evidence: `src/main/java/com/example/gradle/repository/OrderRepository.java:7` (`ev:src/main/java/com/example/gradle/repository/OrderRepository.java:7-7:com.example.gradle.repository.OrderRepository:@Repository`)

### `com.example.gradle.security.SecurityConfig`

- Module: Detected `module:.` (path: `.`)
- Stereotypes: Detected `@Configuration`
  - Evidence: `src/main/java/com/example/gradle/security/SecurityConfig.java:9` (`ev:src/main/java/com/example/gradle/security/SecurityConfig.java:9-9:com.example.gradle.security.SecurityConfig:@Configuration`)

### `com.example.gradle.service.OrderService`

- Module: Detected `module:.` (path: `.`)
- Stereotypes: Detected `@Service`
  - Evidence: `src/main/java/com/example/gradle/service/OrderService.java:8` (`ev:src/main/java/com/example/gradle/service/OrderService.java:8-8:com.example.gradle.service.OrderService:@Service`)

### `com.example.gradle.web.OrderController`

- Module: Detected `module:.` (path: `.`)
- Stereotypes: Detected `@RestController`
  - Evidence: `src/main/java/com/example/gradle/web/OrderController.java:9` (`ev:src/main/java/com/example/gradle/web/OrderController.java:9-9:com.example.gradle.web.OrderController:@RestController`)

## Domain And Data Model

- Analysis status: `analyzed`
- Domain summary: detected 2 JPA entity facts and 0 embeddable facts.
- Domain/data facts are source-visible JPA annotations and Spring Data generic signals only; no database schema, runtime Hibernate metadata, migration interpretation, or provider defaults are claimed.
- Extracted entity, field, identifier, embeddable, and relationship facts stay separate from inferred repository/entity links, uncertain relationship targets, and explicit not-analyzed composite-id/runtime boundaries.

### `com.example.gradle.domain.Customer`

- Module: Detected `module:.` (path: `.`)
- Entity: Detected `com.example.gradle.domain.Customer`
  - Evidence: `src/main/java/com/example/gradle/domain/Customer.java:7` (`ev:src/main/java/com/example/gradle/domain/Customer.java:7-7:com.example.gradle.domain.Customer:@Entity`)
- Table: Detected `customers`
  - Evidence: `src/main/java/com/example/gradle/domain/Customer.java:8` (`ev:src/main/java/com/example/gradle/domain/Customer.java:8-8:com.example.gradle.domain.Customer:@Table`)
- Field metadata: Detected none.
- Identifier field: Detected `id` (`Long`) declared by `com.example.gradle.domain.Customer` with source_kind `declared` identifier_kind `simple_id`
  - Evidence: `src/main/java/com/example/gradle/domain/Customer.java:11` (`ev:src/main/java/com/example/gradle/domain/Customer.java:11-11:com.example.gradle.domain.Customer:@Id:field:id`)
- Relationships: Detected none.

### `com.example.gradle.domain.Order`

- Module: Detected `module:.` (path: `.`)
- Entity: Detected `com.example.gradle.domain.Order`
  - Evidence: `src/main/java/com/example/gradle/domain/Order.java:12` (`ev:src/main/java/com/example/gradle/domain/Order.java:12-12:com.example.gradle.domain.Order:@Entity`)
- Table: Detected `orders`
  - Evidence: `src/main/java/com/example/gradle/domain/Order.java:13` (`ev:src/main/java/com/example/gradle/domain/Order.java:13-13:com.example.gradle.domain.Order:@Table`)
- Field metadata: Source-visible `externalId` (`String`) role `basic` annotations `@Column`
  - Column attributes: Source-visible `name=external_id`, `nullable=false`
  - Evidence: `src/main/java/com/example/gradle/domain/Order.java:20` (`ev:src/main/java/com/example/gradle/domain/Order.java:20-20:com.example.gradle.domain.Order:@Column:field:externalId`)
- Field metadata: Source-visible `id` (`Long`) role `simple_id` annotations `@GeneratedValue`
  - Generated value attributes: Source-visible none recorded
  - Evidence: `src/main/java/com/example/gradle/domain/Order.java:17` (`ev:src/main/java/com/example/gradle/domain/Order.java:17-17:com.example.gradle.domain.Order:@GeneratedValue:field:id`)
- Identifier field: Detected `id` (`Long`) declared by `com.example.gradle.domain.Order` with source_kind `declared` identifier_kind `simple_id`
  - Generated value attributes: Source-visible none recorded
  - Evidence: `src/main/java/com/example/gradle/domain/Order.java:16` (`ev:src/main/java/com/example/gradle/domain/Order.java:16-16:com.example.gradle.domain.Order:@Id:field:id`), `src/main/java/com/example/gradle/domain/Order.java:17` (`ev:src/main/java/com/example/gradle/domain/Order.java:17-17:com.example.gradle.domain.Order:@GeneratedValue:field:id`)
- Relationship: Uncertain target for `customer` `@ManyToOne` cardinality `many_to_one` declared type `Customer`
  - target_resolution: `declared_type_only`
  - uncertainty: `target_type_not_resolved`
  - Relationship attributes: Source-visible `ownership_signal=join_metadata_present`, `optional=false`, `fetch=FetchType.LAZY`
  - Join column: Source-visible `name=customer_id`
  - Evidence: `src/main/java/com/example/gradle/domain/Order.java:23` (`ev:src/main/java/com/example/gradle/domain/Order.java:23-23:com.example.gradle.domain.Order:@ManyToOne:field:customer`), `src/main/java/com/example/gradle/domain/Order.java:24` (`ev:src/main/java/com/example/gradle/domain/Order.java:24-24:com.example.gradle.domain.Order:@JoinColumn:field:customer`)

### Embeddables

- Analysis status: `analyzed`
- Detected: no direct `@Embeddable` classes recorded.

## Detected Tests

- Analysis status: `analyzed`
- Test inventory summary: detected 1 test class, 2 framework signals, 1 Spring test slice signal, 1 mock signal, 1 supported JUnit method, and 4 tested-subject relation/status rows.

### `com.example.gradle.web.OrderControllerTest`

- Module: Detected `module:.` (path: `.`)
- Test class: Detected `com.example.gradle.web.OrderControllerTest`
  - Evidence: `src/test/java/com/example/gradle/web/OrderControllerTest.java:9` (`ev:src/test/java/com/example/gradle/web/OrderControllerTest.java:9-9:com.example.gradle.web.OrderControllerTest:test_file`)
- Source: Detected `src/test/java/com/example/gradle/web/OrderControllerTest.java`
- Framework signal: Detected `JUnit Jupiter` (signal_kind: `framework`)
  - Evidence: `src/test/java/com/example/gradle/web/OrderControllerTest.java:14` (`ev:src/test/java/com/example/gradle/web/OrderControllerTest.java:14-14:com.example.gradle.web.OrderControllerTest#returnsOrder:@Test`), `src/test/java/com/example/gradle/web/OrderControllerTest.java:4` (`ev:src/test/java/com/example/gradle/web/OrderControllerTest.java:4-4:com.example.gradle.web.OrderControllerTest:import:org.junit.jupiter.api.Test`)
- Framework signal: Detected `Spring Test` (signal_kind: `framework`)
  - Evidence: `src/test/java/com/example/gradle/web/OrderControllerTest.java:5` (`ev:src/test/java/com/example/gradle/web/OrderControllerTest.java:5-5:com.example.gradle.web.OrderControllerTest:import:org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest`), `src/test/java/com/example/gradle/web/OrderControllerTest.java:6` (`ev:src/test/java/com/example/gradle/web/OrderControllerTest.java:6-6:com.example.gradle.web.OrderControllerTest:import:org.springframework.boot.test.mock.mockito.MockBean`), `src/test/java/com/example/gradle/web/OrderControllerTest.java:8` (`ev:src/test/java/com/example/gradle/web/OrderControllerTest.java:8-8:com.example.gradle.web.OrderControllerTest:@WebMvcTest`)
- Spring test slice signal: Detected `@WebMvcTest` (slice_kind: `web_mvc_test`, signal_kind: `spring_test_slice`)
  - Evidence: `src/test/java/com/example/gradle/web/OrderControllerTest.java:8` (`ev:src/test/java/com/example/gradle/web/OrderControllerTest.java:8-8:com.example.gradle.web.OrderControllerTest:@WebMvcTest`)
- Mock annotation signal: Detected `@MockBean` on `field` `orderService` (mock_signal: `spring_boot_mockbean_annotation`, signal_kind: `mock_annotation`)
  - Evidence: `src/test/java/com/example/gradle/web/OrderControllerTest.java:11` (`ev:src/test/java/com/example/gradle/web/OrderControllerTest.java:11-11:com.example.gradle.web.OrderControllerTest:field:orderService:@MockBean`)
- Test method: Detected `returnsOrder` annotated with `@Test` (method_kind: `test`)
  - Evidence: `src/test/java/com/example/gradle/web/OrderControllerTest.java:14` (`ev:src/test/java/com/example/gradle/web/OrderControllerTest.java:14-14:com.example.gradle.web.OrderControllerTest#returnsOrder:@Test`)
- Inferred tested subject: `com.example.gradle.web.OrderController` in target module `module:.` (path: `.`) (relation_status: `inferred`, relation_type: `naming_convention`, support_type: `inferred`, confidence: `medium`).
  - Evidence: `src/test/java/com/example/gradle/web/OrderControllerTest.java:9` (`ev:src/test/java/com/example/gradle/web/OrderControllerTest.java:9-9:com.example.gradle.web.OrderControllerTest:test_file`), `src/main/java/com/example/gradle/web/OrderController.java:11` (`ev:src/main/java/com/example/gradle/web/OrderController.java:11-11:com.example.gradle.web.OrderController:code_symbol`)
- Inferred tested subject: `com.example.gradle.web.OrderController` in target module `module:.` (path: `.`) (relation_status: `inferred`, relation_type: `spring_test_slice_class_literal`, support_type: `inferred`, confidence: `medium`).
  - Evidence: `src/test/java/com/example/gradle/web/OrderControllerTest.java:8` (`ev:src/test/java/com/example/gradle/web/OrderControllerTest.java:8-8:com.example.gradle.web.OrderControllerTest:@WebMvcTest`), `src/main/java/com/example/gradle/web/OrderController.java:11` (`ev:src/main/java/com/example/gradle/web/OrderController.java:11-11:com.example.gradle.web.OrderController:code_symbol`)
- Inferred tested subject: `com.example.gradle.service.OrderService` in target module `module:.` (path: `.`) (relation_status: `inferred`, relation_type: `test_field_type`, support_type: `inferred`, confidence: `medium`).
  - Evidence: `src/test/java/com/example/gradle/web/OrderControllerTest.java:12` (`ev:src/test/java/com/example/gradle/web/OrderControllerTest.java:12-12:com.example.gradle.web.OrderControllerTest:field:orderService:type:OrderService`), `src/main/java/com/example/gradle/service/OrderService.java:9` (`ev:src/main/java/com/example/gradle/service/OrderService.java:9-9:com.example.gradle.service.OrderService:code_symbol`)
- Inferred tested subject: `com.example.gradle.service.OrderService` in target module `module:.` (path: `.`) (relation_status: `inferred`, relation_type: `test_import`, support_type: `inferred`, confidence: `medium`).
  - Evidence: `src/test/java/com/example/gradle/web/OrderControllerTest.java:3` (`ev:src/test/java/com/example/gradle/web/OrderControllerTest.java:3-3:com.example.gradle.web.OrderControllerTest:import:com.example.gradle.service.OrderService`), `src/main/java/com/example/gradle/service/OrderService.java:9` (`ev:src/main/java/com/example/gradle/service/OrderService.java:9-9:com.example.gradle.service.OrderService:code_symbol`)

## Quality And Change-Risk Signals

- Quality analysis status: `analyzed`
- Test-gap signals are absence-sensitive planning hints from the bounded test inventory and inferred tested-subject relations. They do not prove coverage gaps, execution behavior, assertion behavior, CI status, or complete subject mapping.
- Change-risk signals are warning-oriented or uncertain planning hints from existing deterministic facts. They do not prove production impact, vulnerability, business priority, correctness, runtime behavior, or test priority.

### Test-Gap Signals

- Analysis status: `analyzed`
- Test-gap signal: `entity_without_obvious_test` for `jpa_entity` `com.example.gradle.domain.Customer` (status: `no_obvious_test`, inference_basis: `no_inferred_tested_subject_relation_for_subject_class`, confidence: `low`, uncertainty: `bounded_test_inventory_supported_relations_only`). No coverage, execution, assertion, CI, or runtime relation is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `entity:com.example.gradle.domain.Customer`
  - Subject source hint: class `com.example.gradle.domain.Customer`, member `not recorded`
  - Evidence: `src/main/java/com/example/gradle/domain/Customer.java:7` (`ev:src/main/java/com/example/gradle/domain/Customer.java:7-7:com.example.gradle.domain.Customer:@Entity`), `src/main/java/com/example/gradle/domain/Customer.java:8` (`ev:src/main/java/com/example/gradle/domain/Customer.java:8-8:com.example.gradle.domain.Customer:@Table`)
- Test-gap signal: `entity_without_obvious_test` for `jpa_entity` `com.example.gradle.domain.Order` (status: `no_obvious_test`, inference_basis: `no_inferred_tested_subject_relation_for_subject_class`, confidence: `low`, uncertainty: `bounded_test_inventory_supported_relations_only`). No coverage, execution, assertion, CI, or runtime relation is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `entity:com.example.gradle.domain.Order`
  - Subject source hint: class `com.example.gradle.domain.Order`, member `not recorded`
  - Evidence: `src/main/java/com/example/gradle/domain/Order.java:12` (`ev:src/main/java/com/example/gradle/domain/Order.java:12-12:com.example.gradle.domain.Order:@Entity`), `src/main/java/com/example/gradle/domain/Order.java:13` (`ev:src/main/java/com/example/gradle/domain/Order.java:13-13:com.example.gradle.domain.Order:@Table`)
- Test-gap signal: `repository_without_obvious_test` for `spring_repository` `com.example.gradle.repository.OrderRepository` (status: `no_obvious_test`, inference_basis: `no_inferred_tested_subject_relation_for_subject_class`, confidence: `low`, uncertainty: `bounded_test_inventory_supported_relations_only`). No coverage, execution, assertion, CI, or runtime relation is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `spring_data_repository_interface_signal:module:.:com.example.gradle.repository.OrderRepository`
  - Subject source hint: class `com.example.gradle.repository.OrderRepository`, member `not recorded`
  - Evidence: `src/main/java/com/example/gradle/repository/OrderRepository.java:7-8` (`ev:src/main/java/com/example/gradle/repository/OrderRepository.java:7-8:com.example.gradle.repository.OrderRepository:com.example.gradle.repository.OrderRepository`), `src/main/java/com/example/gradle/repository/OrderRepository.java:8` (`ev:src/main/java/com/example/gradle/repository/OrderRepository.java:8-8:com.example.gradle.repository.OrderRepository:extends:org.springframework.data.jpa.repository.JpaRepository`)
- Test-gap signal: `repository_without_obvious_test` for `spring_repository` `com.example.gradle.repository.OrderRepository` (status: `no_obvious_test`, inference_basis: `no_inferred_tested_subject_relation_for_subject_class`, confidence: `low`, uncertainty: `bounded_test_inventory_supported_relations_only`). No coverage, execution, assertion, CI, or runtime relation is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `spring_repository_stereotype:module:.:com.example.gradle.repository.OrderRepository`
  - Subject source hint: class `com.example.gradle.repository.OrderRepository`, member `not recorded`
  - Evidence: `src/main/java/com/example/gradle/repository/OrderRepository.java:7` (`ev:src/main/java/com/example/gradle/repository/OrderRepository.java:7-7:com.example.gradle.repository.OrderRepository:@Repository`)

### Change-Risk Signals

- Analysis status: `analyzed`
- Change-risk signal: `event_listener_change_surface` for `spring_event_listener` `com.example.gradle.service.OrderService#onOrderEvent` (status: `planning_hint`, risk_basis: `source_visible_event_listener`, confidence: `low`, uncertainty: `source_visible_change_surface_only`). No production impact, vulnerability, correctness, runtime behavior, or business priority is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `spring_event_listener:module:.:com.example.gradle.service.OrderService#onOrderEvent:decl:000001`
  - Subject source hint: class `com.example.gradle.service.OrderService`, member `onOrderEvent`
  - Evidence: `src/main/java/com/example/gradle/service/OrderService.java:16` (`ev:src/main/java/com/example/gradle/service/OrderService.java:16-16:com.example.gradle.service.OrderService#onOrderEvent:@EventListener`)
- Change-risk signal: `jpa_relationship_change_surface` for `jpa_relationship` `com.example.gradle.domain.Order#customer` (status: `uncertain_planning_hint`, risk_basis: `source_visible_jpa_relationship_metadata`, confidence: `low`, uncertainty: `relationship_target_declared_type_only`). No production impact, vulnerability, correctness, runtime behavior, or business priority is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `entity:com.example.gradle.domain.Order#relationship:customer`
  - Subject source hint: class `com.example.gradle.domain.Order`, member `customer`
  - Evidence: `src/main/java/com/example/gradle/domain/Order.java:23` (`ev:src/main/java/com/example/gradle/domain/Order.java:23-23:com.example.gradle.domain.Order:@ManyToOne:field:customer`), `src/main/java/com/example/gradle/domain/Order.java:24` (`ev:src/main/java/com/example/gradle/domain/Order.java:24-24:com.example.gradle.domain.Order:@JoinColumn:field:customer`)
- Change-risk signal: `messaging_listener_change_surface` for `spring_messaging_listener` `com.example.gradle.messaging.OrderListener#handleOrderMessage` (status: `planning_hint`, risk_basis: `source_visible_messaging_listener`, confidence: `low`, uncertainty: `source_visible_change_surface_only`). No production impact, vulnerability, correctness, runtime behavior, or business priority is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `messaging_listener_signal:module:.:com.example.gradle.messaging.OrderListener#handleOrderMessage:annotation:kafka_listener:decl:000001`
  - Subject source hint: class `com.example.gradle.messaging.OrderListener`, member `handleOrderMessage`
  - Evidence: `src/main/java/com/example/gradle/messaging/OrderListener.java:9` (`ev:src/main/java/com/example/gradle/messaging/OrderListener.java:9-9:com.example.gradle.messaging.OrderListener#handleOrderMessage:@KafkaListener`)
- Change-risk signal: `scheduled_method_change_surface` for `spring_scheduled_method` `com.example.gradle.config.GradleConfiguration#refreshOrderCache` (status: `planning_hint`, risk_basis: `source_visible_scheduled_method`, confidence: `low`, uncertainty: `source_visible_change_surface_only`). No production impact, vulnerability, correctness, runtime behavior, or business priority is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `spring_scheduled_method:module:.:com.example.gradle.config.GradleConfiguration#refreshOrderCache:decl:000001`
  - Subject source hint: class `com.example.gradle.config.GradleConfiguration`, member `refreshOrderCache`
  - Evidence: `src/main/java/com/example/gradle/config/GradleConfiguration.java:17` (`ev:src/main/java/com/example/gradle/config/GradleConfiguration.java:17-17:com.example.gradle.config.GradleConfiguration#refreshOrderCache:@Scheduled`)
- Change-risk signal: `spring_bean_method_change_surface` for `spring_bean_method` `com.example.gradle.config.GradleConfiguration#orderClock` (status: `planning_hint`, risk_basis: `source_visible_bean_method`, confidence: `low`, uncertainty: `source_visible_change_surface_only`). No production impact, vulnerability, correctness, runtime behavior, or business priority is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `spring_bean_method:module:.:com.example.gradle.config.GradleConfiguration#orderClock:decl:000001`
  - Subject source hint: class `com.example.gradle.config.GradleConfiguration`, member `orderClock`
  - Evidence: `src/main/java/com/example/gradle/config/GradleConfiguration.java:12` (`ev:src/main/java/com/example/gradle/config/GradleConfiguration.java:12-12:com.example.gradle.config.GradleConfiguration#orderClock:@Bean`)
- Change-risk signal: `spring_bean_method_change_surface` for `spring_bean_method` `com.example.gradle.security.SecurityConfig#filterChain` (status: `planning_hint`, risk_basis: `source_visible_bean_method`, confidence: `low`, uncertainty: `source_visible_change_surface_only`). No production impact, vulnerability, correctness, runtime behavior, or business priority is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `spring_bean_method:module:.:com.example.gradle.security.SecurityConfig#filterChain:decl:000001`
  - Subject source hint: class `com.example.gradle.security.SecurityConfig`, member `filterChain`
  - Evidence: `src/main/java/com/example/gradle/security/SecurityConfig.java:13` (`ev:src/main/java/com/example/gradle/security/SecurityConfig.java:13-13:com.example.gradle.security.SecurityConfig#filterChain:@Bean`)
- Change-risk signal: `spring_configuration_change_surface` for `spring_configuration_class` `com.example.gradle.config.GradleConfiguration` (status: `planning_hint`, risk_basis: `source_visible_spring_configuration`, confidence: `low`, uncertainty: `source_visible_change_surface_only`). No production impact, vulnerability, correctness, runtime behavior, or business priority is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `spring_configuration_class:module:.:com.example.gradle.config.GradleConfiguration`
  - Subject source hint: class `com.example.gradle.config.GradleConfiguration`, member `not recorded`
  - Evidence: `src/main/java/com/example/gradle/config/GradleConfiguration.java:9` (`ev:src/main/java/com/example/gradle/config/GradleConfiguration.java:9-9:com.example.gradle.config.GradleConfiguration:@Configuration`)
- Change-risk signal: `spring_configuration_change_surface` for `spring_configuration_class` `com.example.gradle.security.SecurityConfig` (status: `planning_hint`, risk_basis: `source_visible_spring_configuration`, confidence: `low`, uncertainty: `source_visible_change_surface_only`). No production impact, vulnerability, correctness, runtime behavior, or business priority is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `spring_configuration_class:module:.:com.example.gradle.security.SecurityConfig`
  - Subject source hint: class `com.example.gradle.security.SecurityConfig`, member `not recorded`
  - Evidence: `src/main/java/com/example/gradle/security/SecurityConfig.java:9` (`ev:src/main/java/com/example/gradle/security/SecurityConfig.java:9-9:com.example.gradle.security.SecurityConfig:@Configuration`)
- Change-risk signal: `spring_configuration_properties_change_surface` for `spring_configuration_properties` `com.example.gradle.config.GradleConfiguration.OrderProperties` (status: `planning_hint`, risk_basis: `source_visible_configuration_properties`, confidence: `low`, uncertainty: `source_visible_change_surface_only`). No production impact, vulnerability, correctness, runtime behavior, or business priority is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `spring_configuration_properties_type:module:.:com.example.gradle.config.GradleConfiguration.OrderProperties`
  - Subject source hint: class `com.example.gradle.config.GradleConfiguration.OrderProperties`, member `not recorded`
  - Evidence: `src/main/java/com/example/gradle/config/GradleConfiguration.java:20` (`ev:src/main/java/com/example/gradle/config/GradleConfiguration.java:20-20:com.example.gradle.config.GradleConfiguration.OrderProperties:@ConfigurationProperties`)
- Change-risk signal: `spring_security_warning_change_surface` for `spring_security_warning` `warning:spring_security:security_configuration_annotation:com.example.gradle.security.SecurityConfig:annotation:enable_web_security:decl:000001` (status: `warning_oriented_planning_hint`, risk_basis: `source_visible_spring_security_warning`, confidence: `low`, uncertainty: `warning_signal_only_not_vulnerability_or_correctness`). No production impact, vulnerability, correctness, runtime behavior, or business priority is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `warning:spring_security:security_configuration_annotation:com.example.gradle.security.SecurityConfig:annotation:enable_web_security:decl:000001`
  - Evidence: `src/main/java/com/example/gradle/security/SecurityConfig.java:10` (`ev:src/main/java/com/example/gradle/security/SecurityConfig.java:10-10:com.example.gradle.security.SecurityConfig:@EnableWebSecurity`)
- Change-risk signal: `spring_security_warning_change_surface` for `spring_security_warning` `warning:spring_security:security_filter_chain_bean:com.example.gradle.security.SecurityConfig#filterChain:decl:000001` (status: `warning_oriented_planning_hint`, risk_basis: `source_visible_spring_security_warning`, confidence: `low`, uncertainty: `warning_signal_only_not_vulnerability_or_correctness`). No production impact, vulnerability, correctness, runtime behavior, or business priority is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `warning:spring_security:security_filter_chain_bean:com.example.gradle.security.SecurityConfig#filterChain:decl:000001`
  - Evidence: `src/main/java/com/example/gradle/security/SecurityConfig.java:13` (`ev:src/main/java/com/example/gradle/security/SecurityConfig.java:13-13:com.example.gradle.security.SecurityConfig#filterChain:@Bean`), `src/main/java/com/example/gradle/security/SecurityConfig.java:14` (`ev:src/main/java/com/example/gradle/security/SecurityConfig.java:14-14:com.example.gradle.security.SecurityConfig#filterChain:return:SecurityFilterChain`)
- Change-risk signal: `spring_service_change_surface` for `spring_service` `com.example.gradle.service.OrderService` (status: `planning_hint`, risk_basis: `source_visible_service_stereotype`, confidence: `low`, uncertainty: `source_visible_change_surface_only`). No production impact, vulnerability, correctness, runtime behavior, or business priority is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `component:com.example.gradle.service.OrderService`
  - Subject source hint: class `com.example.gradle.service.OrderService`, member `not recorded`
  - Evidence: `src/main/java/com/example/gradle/service/OrderService.java:8` (`ev:src/main/java/com/example/gradle/service/OrderService.java:8-8:com.example.gradle.service.OrderService:@Service`)
- Change-risk signal: `transaction_boundary_change_surface` for `spring_transaction_boundary` `com.example.gradle.service.OrderService#load` (status: `planning_hint`, risk_basis: `source_visible_transaction_boundary`, confidence: `low`, uncertainty: `source_visible_change_surface_only`). No production impact, vulnerability, correctness, runtime behavior, or business priority is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `spring_transaction_boundary:module:.:com.example.gradle.service.OrderService#load:decl:000001`
  - Subject source hint: class `com.example.gradle.service.OrderService`, member `load`
  - Evidence: `src/main/java/com/example/gradle/service/OrderService.java:11` (`ev:src/main/java/com/example/gradle/service/OrderService.java:11-11:com.example.gradle.service.OrderService#load:@Transactional`)


## Local Project Documentation

- Documents analysis status: `analyzed`
- Local documentation entries are default-scope Markdown navigation facts only; document bodies, paragraphs, arbitrary lists, tables, code blocks, and prose summaries are not rendered.
- Reconciliation rows are uncertain inspection hints only; they do not prove stale documentation, coverage, completeness, correctness, implementation, or source/document agreement.
- Discovery policy: scope `default_local_markdown`, path_policy `repository_relative_in_root`, symlink_policy `skip_symlinks`, included_patterns `7`, excluded_patterns `13`.
- Document inventory: detected 1 accepted default-scope Markdown document.
  - Document: `README.md` (module: `repository-level`, discovery_source: `root_readme`, title_source: `first_heading`, headings: `1`, chunks: `1`).
    - Evidence: `README.md` (`ev:README.md:unknown:document:file:README.md`)
    - Heading refs: detected 1 bounded ATX heading reference.
      - Heading ref: `document_heading:README.md:heading:Gradle%20Analyzer%20Integration:occ:000001` level `1`, lines `1`, anchor `gradle-analyzer-integration`, evidence `README.md:1` (`ev:README.md:1-1:document:heading:Gradle%20Analyzer%20Integration:decl:000001`).
    - Chunk refs: detected 1 bounded chunk reference; chunk bodies are not serialized.
      - Chunk ref: `document_chunk:README.md:chunk:000001` heading_id `document_heading:README.md:heading:Gradle%20Analyzer%20Integration:occ:000001`, lines `1-5`, content_status `not_serialized`, evidence `README.md:1-5` (`ev:README.md:1-5:document:chunk:000001`).
- Reconciliation hints: status `analyzed`; detected none.

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

- Warning: `hidden_http_surface` signal `openapi_spec_file` for module `module:.` (path: `.`) at `src/main/resources/openapi.yml`. OpenAPI/Swagger spec file detected by filename; declared operations, when supported, are reported separately under api\_surface.openapi.operations, and this warning does not reconstruct generated APIs.
  - Evidence: `src/main/resources/openapi.yml` (`ev:src/main/resources/openapi.yml:unknown:config_file:openapi.yml`)
- Warning: `spring_security` signal `security_configuration_annotation` for module `module:.` (path: `.`) at `src/main/java/com/example/gradle/security/SecurityConfig.java`. Spring Security configuration annotation detected as a source-visible inspection hint and change-risk signal; the analyzer does not evaluate security policy, endpoint protection, authentication, authorization, filter-chain order, vulnerability, or correctness.
  - Evidence: `src/main/java/com/example/gradle/security/SecurityConfig.java:10` (`ev:src/main/java/com/example/gradle/security/SecurityConfig.java:10-10:com.example.gradle.security.SecurityConfig:@EnableWebSecurity`)
- Warning: `spring_security` signal `security_filter_chain_bean` for module `module:.` (path: `.`) at `src/main/java/com/example/gradle/security/SecurityConfig.java`. SecurityFilterChain @Bean method detected as a source-visible Spring Security configuration inspection hint and change-risk signal; the analyzer does not evaluate security policy, endpoint protection, authentication, authorization, filter-chain order, vulnerability, or correctness.
  - Evidence: `src/main/java/com/example/gradle/security/SecurityConfig.java:13` (`ev:src/main/java/com/example/gradle/security/SecurityConfig.java:13-13:com.example.gradle.security.SecurityConfig#filterChain:@Bean`), `src/main/java/com/example/gradle/security/SecurityConfig.java:14` (`ev:src/main/java/com/example/gradle/security/SecurityConfig.java:14-14:com.example.gradle.security.SecurityConfig#filterChain:return:SecurityFilterChain`)
- Not scanned: Generated-source roots are metadata-only path/codegen observations with `content_status: "not_scanned"`; generated source contents, generator execution, generated API reconstruction, runtime freshness checks, dependency/task resolution, and custom Gradle generated-source graph reconstruction are not performed.
- Not analyzed: Spring runtime behavior such as component scanning, dependency injection graphs, bean lifecycle, scopes, and conditional configuration is not represented by `components.items`.
- Uncertain: JPA relationship targets preserve `target_resolution: declared_type_only` and `uncertainty: target_type_not_resolved`; no symbol solving or ORM runtime behavior is claimed.
- Source-visible: JPA relationship metadata such as `mappedBy`, `@JoinColumn`, `@JoinTable`, `optional`, `fetch`, `cascade`, and `orphanRemoval` is reported only when direct annotation attributes are supported; foreign keys, join tables, ownership correctness, fetch behavior, cascade behavior, and database constraints are not claimed.
- Not analyzed: JPA mapped-superclass identifier support is limited to conservative source-visible mapped-superclass chains; unresolved, ambiguous, cyclic, or non-source-visible branches are skipped.
- Partial: JPA embedded and composite identifier support is limited to direct source-visible `@Embeddable`, `@Embedded`, `@EmbeddedId`, and `@IdClass` signals. Embedded targets are linked only when a unique local `@Embeddable` can be matched; `@IdClass` field matching and composite-key semantics are not analyzed.
- Inferred/statused: tested-subject rows are conservative source-visible hints from supported naming, import, field-type, and Spring test slice class-literal signals. Non-inferred statuses such as `not_detected`, `ambiguous`, and `unsupported` do not claim coverage or execution. Test method inventory records source-visible JUnit annotation structure only. Test execution, CI results, coverage, assertion behavior, call graphs, and complete subject mapping are not analyzed.
- Planning hints: quality test-gap and change-risk signals are conservative derived hints from existing deterministic facts and inferred tested-subject relations. They do not claim coverage, test execution, assertion behavior, runtime behavior, production impact, vulnerability, correctness, business priority, or complete subject mapping.
- Source-visible: Spring test slice signals and mock annotation signals record direct annotation structure only. Runtime Spring context behavior, bean graph contents, MockMvc setup, database access, Mockito behavior, and slice correctness are not claimed.
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
- Document-backed: local documentation facts come from default-scope Markdown inventory, heading/chunk navigation references, and uncertain reconciliation hints only. Hidden, private, generated, dependency, maintainer, and `.project-memory/` paths are excluded by default; symlinks are not followed by default; external docs, PDFs, Word documents, connectors, generic RAG, repository chat, and LLM summaries are outside the core analyzer. Document-backed signals do not override code-backed facts.
