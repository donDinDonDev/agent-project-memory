# Agent Guide

Generated deterministically from `project-map.json` and `evidence-index.jsonl`. The guide generator does not re-analyze source files.

## Read This First

- Open `artifact-set.json` before this guide and respect its artifact authority labels.
- Use this guide as deterministic orientation only. It is not evidence and does not re-analyze source files.
- For large or unknown outputs, prefer `query <path> agent-context`, targeted query commands, focused `project-map.json` selection, exact `evidence-index.jsonl` lookup, and source readback instead of reading every row.
- Size note: this guide is `small-guide` (about `53 KiB`, `476` rendered lines); known generator inputs are `project-map.json` `68 KiB` and `evidence-index.jsonl` `29 KiB`.

## Trust And Verification Legend

Trust and verification legend:
- Use `evidence-index.jsonl` as the authoritative source-backed evidence ledger; verify important claims against its exact records and the repository source locations they cite.
- Generated project facts: `project-map.json` facts; verify important use through their evidence IDs.
- Deterministic presentation: this guide, `endpoints.md`, and query stdout help with orientation; they are not evidence.
- Navigation, provenance, or execution metadata: `artifact-set.json`, `project-graph.json`, `source-registry.json`, profiles, LLM/provider AI output, cache, workspace, adapter output, release metadata, security reports, and downstream-agent output are non-evidence unless a later public contract explicitly changes that.
- Before code changes, review findings, public/security/release wording, or architecture decisions, resolve exact evidence IDs and read the cited source.

## Practical Inspection Order For Coding Agents

1. Start with detected build, module, and layout facts in `pom.xml`, `src/main/resources/application.yml`, `src/main/java/com/example/Stage3Application.java`.
2. For HTTP behavior, inspect detected endpoint and hidden-surface warning evidence in `src/main/java/com/example/web/ProjectMapController.java`.
3. For Spring application surface changes, inspect Spring application surface and component evidence in `src/main/java/com/example/components/InventoryComponents.java`, `src/main/java/com/example/repositories/ProjectOrderRepository.java`, `src/main/java/com/example/domain/ProjectEntities.java`, `src/main/java/com/example/web/ProjectMapController.java` and avoid assuming runtime repository registration, entity ownership, injection graphs, transaction behavior, scheduler registration, event delivery, or messaging topology.
4. For persistence changes, inspect detected entity evidence in `src/main/java/com/example/domain/ProjectEntities.java` and treat field metadata as source-visible annotations only, not runtime schema, provider defaults, or complete access-strategy reconstruction; relationship targets remain declared-type-only.
5. For tests, inspect detected test files and tested-subject relation/status evidence in `src/test/java/com/example/web/ProjectMapControllerTest.java`, `src/main/java/com/example/web/ProjectMapController.java`; do not treat inferred or statused subjects as coverage proof.
6. For quality and change-risk planning, inspect quality signal evidence in `src/main/java/com/example/domain/ProjectEntities.java`, `src/main/java/com/example/components/InventoryComponents.java`, `src/main/java/com/example/repositories/ProjectOrderRepository.java` and treat `no_obvious_test`, warning-oriented, and uncertain statuses as planning hints only, not coverage, runtime, correctness, vulnerability, or business-priority claims.

## Project Memory Overview

- Build/layout: build system `maven`, modules `1`, source roots `1`, test roots `1`.
- Source-backed fact surfaces: endpoints `2`, direct Spring components `5`, Spring application surface rows `3`, entities `5`, embeddables `2`, tests `1`.
- Planning/navigation surfaces: warnings `0`, quality/change-risk hints `13`, local documents `0`, document reconciliation hints `0`.
- Evidence records: `77` records in `evidence-index.jsonl`; this overview is presentation only.
- Size band: `small-guide`; large detailed sections should be selected by task and verified through exact evidence IDs.

## Known Uncertainty Snapshot

- Warnings: `0` warning rows; warning evidence and messages stay in the detailed limits section.
- Inferred or statused rows: `16` rows; keep `inferred`, `ambiguous`, `not_detected`, `unsupported`, and similar labels attached to any use.
- Explicit uncertainty labels: `18` values; preserve those caveats with the cited evidence.
- Not analyzed/out-of-scope status markers: `4`; runtime behavior, generated-source contents, test execution/coverage, source/spec agreement, connectors, and LLM summaries remain outside source-backed evidence unless a later contract says otherwise.

## Not Represented In This Scan

- No represented rows for: `local project documentation`, `generated-source root metadata`. This means the current deterministic scan emitted no rows for those surfaces; it does not prove the runtime behavior is absent outside the supported analyzer scope.

## Detected Project Layout

- Build system: Detected `maven`
- Root build file: Detected `pom.xml`
  - Evidence: `pom.xml:1` (`ev:pom.xml:1-1:build_file:pom.xml`)
- Source roots: Detected `src/main/java`
  - Evidence: recorded in `project-map.json`; no separate source-root evidence IDs are emitted.
- Test roots: Detected `src/test/java`
  - Evidence: recorded in `project-map.json`; no separate test-root evidence IDs are emitted.
- Modules analysis status: `analyzed`
- Module: Detected `module:.` (path: `.`)
  - POM path: Detected `pom.xml`
  - Support status: `supported`
  - Declaration kind: `scan_root`
  - Declared path: `.`
  - Source roots: Detected `src/main/java`
  - Source roots evidence: recorded in `project-map.json`; no separate production root evidence IDs are emitted.
  - Test roots: Detected `src/test/java`
  - Test roots evidence: recorded in `project-map.json`; no separate test root evidence IDs are emitted.
  - Declaration evidence: none recorded.
  - POM evidence: `pom.xml:1` (`ev:pom.xml:1-1:build_file:pom.xml`)

## Build And Configuration Orientation

### Module `module:.` (path: `.`)

- Build/config analysis status: `analyzed`
- Source-visible Maven metadata: group_id `com.example` (value_kind: `literal`), artifact_id `stage3-project-map` (value_kind: `literal`), version `1.0.0` (value_kind: `literal`), packaging `value:not_declared` (value_kind: `not_declared`).
  - Evidence: `pom.xml:7` (`ev:pom.xml:7-7:build_file:maven:project:groupId`), `pom.xml:8` (`ev:pom.xml:8-8:build_file:maven:project:artifactId`), `pom.xml:9` (`ev:pom.xml:9-9:build_file:maven:project:version`)
- Source-visible direct dependencies: Detected 2 direct dependency declarations.
  - Dependency: `com.example:inventory-client` declaration_kind `direct_dependency`, version `value:not_declared` (value_kind: `not_declared`), scope `test` (value_kind: `literal`), optional `true` (value_kind: `literal`).
  - Evidence: `pom.xml:29-34` (`ev:pom.xml:29-34:build_file:maven:dependency:000002`)
  - Dependency: `org.springframework.boot:spring-boot-starter-web` declaration_kind `direct_dependency`, version `${spring.boot.version}` (value_kind: `property_reference`), scope `value:not_declared` (value_kind: `not_declared`), optional `value:not_declared` (value_kind: `not_declared`).
  - Evidence: `pom.xml:24-28` (`ev:pom.xml:24-28:build_file:maven:dependency:000001`)
- Source-visible dependency-management declarations: Detected 1 dependency-management declarations; these are management declarations, not active resolved dependencies.
  - Dependency: `org.springframework.boot:spring-boot-dependencies` declaration_kind `dependency_management`, version `${spring.boot.version}` (value_kind: `property_reference`), scope `import` (value_kind: `literal`), optional `value:not_declared` (value_kind: `not_declared`).
  - Evidence: `pom.xml:13-19` (`ev:pom.xml:13-19:build_file:maven:dependency_management:000001`)
- Source-visible direct plugins: Detected 1 direct plugin declarations.
  - Plugin: `org.springframework.boot:spring-boot-maven-plugin` declaration_kind `direct_plugin`, version `${spring.boot.version}` (value_kind: `property_reference`).
    - Direct execution declarations: none recorded.
    - Configuration signals: none recorded
    - Generator signals: none recorded
  - Evidence: `pom.xml:48-52` (`ev:pom.xml:48-52:build_file:maven:plugin:000001`)
- Source-visible plugin-management declarations: Detected 1 plugin-management declarations; these are management declarations, not active plugin executions.
  - Plugin: `org.apache.maven.plugins:maven-surefire-plugin` declaration_kind `plugin_management`, version `3.2.5` (value_kind: `literal`).
    - Direct execution declarations: none recorded.
    - Configuration signals: none recorded
    - Generator signals: none recorded
  - Evidence: `pom.xml:40-44` (`ev:pom.xml:40-44:build_file:maven:plugin_management:000001`)
- Resource roots: Detected 1 standard resource root.
  - Resource root: `main` `src/main/resources`
    - Evidence: recorded in `project-map.json`; no separate resource-root evidence IDs are emitted.
- Config files: Detected 1 path-only supported config file; config contents, keys, and values are not rendered.
  - Config file: `src/main/resources/application.yml` kind `spring_application`, format `yaml`.
  - Evidence: `src/main/resources/application.yml` (`ev:src/main/resources/application.yml:unknown:config_file:application.yml`)
- Spring Boot application signals: Detected 1 direct `@SpringBootApplication` class signal.
  - Spring Boot application: Detected `com.example.Stage3Application` at `src/main/java/com/example/Stage3Application.java` with signal `spring_boot_application_with_main_method`.
    - Main method: Detected source-visible `main` method.
  - Evidence: `src/main/java/com/example/Stage3Application.java:5` (`ev:src/main/java/com/example/Stage3Application.java:5-5:com.example.Stage3Application:@SpringBootApplication`), `src/main/java/com/example/Stage3Application.java:7` (`ev:src/main/java/com/example/Stage3Application.java:7-7:com.example.Stage3Application#main:code_symbol`)
- Module warnings: Detected none.

## API Surface Interpretation

- API surface analysis status: `analyzed`
- Source-visible Spring MVC endpoint facts are code-backed local source observations from `endpoints[]`; they do not prove complete runtime handler mappings.
- Source-visible interface-declared endpoint facts are code-backed only when the interface mapping and unique concrete binding are both source-visible.
- Declared OpenAPI operations are spec-backed contract facts with `implementation_status: "not_analyzed"`; they are not implemented endpoint facts.
- Generated-source API signals, repository-rest warnings, and hidden HTTP warnings are inspection hints, not endpoint or operation facts.
- LLM output, generated Markdown, release notes, and chat text are never evidence for API surface facts or relations.
- Source-visible direct Spring MVC endpoint IDs: status `analyzed`; detected 2 IDs `endpoint:com.example.web.ProjectMapController#createItem`, `endpoint:com.example.web.ProjectMapController#getItem`.
- Source-visible interface-declared Spring MVC endpoint IDs: status `analyzed`; detected none.
- OpenAPI/Swagger spec files: status `analyzed`; detected none.
- OpenAPI/Swagger operations: status `not_detected`; detected none.
- Generated-source API warning IDs: status `analyzed`; detected none.
- Repository-rest warning IDs: status `analyzed`; detected none.
- Hidden HTTP warning IDs: status `analyzed`; detected none.

## Detected Spring MVC Endpoints

- Endpoint summary: detected 2 source-visible Spring MVC endpoint facts.

### `POST /api/items`

- Module: Detected `module:.` (path: `.`)
- Controller: Detected `com.example.web.ProjectMapController`
- Handler: Detected `createItem`
- Mapping source: Detected `direct_handler_method` from `com.example.web.ProjectMapController#createItem` with binding `direct`
- HTTP methods: Detected `POST`
- Paths: Detected `/api/items`
- Request parameters: Detected none.
- Request body: Detected `CreateItemRequest`
- Response: Detected `ItemResponse`
  - Evidence: `src/main/java/com/example/web/ProjectMapController.java:11` (`ev:src/main/java/com/example/web/ProjectMapController.java:11-11:com.example.web.ProjectMapController:@RestController`), `src/main/java/com/example/web/ProjectMapController.java:12` (`ev:src/main/java/com/example/web/ProjectMapController.java:12-12:com.example.web.ProjectMapController:@RequestMapping`), `src/main/java/com/example/web/ProjectMapController.java:21` (`ev:src/main/java/com/example/web/ProjectMapController.java:21-21:com.example.web.ProjectMapController#createItem:@PostMapping`), `src/main/java/com/example/web/ProjectMapController.java:22` (`ev:src/main/java/com/example/web/ProjectMapController.java:22-22:com.example.web.ProjectMapController#createItem:@RequestBody:parameter:0:request`)

### `GET /api/items/{id}`

- Module: Detected `module:.` (path: `.`)
- Controller: Detected `com.example.web.ProjectMapController`
- Handler: Detected `getItem`
- Mapping source: Detected `direct_handler_method` from `com.example.web.ProjectMapController#getItem` with binding `direct`
- HTTP methods: Detected `GET`
- Paths: Detected `/api/items/{id}`
- Request parameters: Detected `path_variable:id (Long)`, `request_param:expand (String)`
- Request body: Detected none.
- Response: Detected `ItemResponse`
  - Evidence: `src/main/java/com/example/web/ProjectMapController.java:11` (`ev:src/main/java/com/example/web/ProjectMapController.java:11-11:com.example.web.ProjectMapController:@RestController`), `src/main/java/com/example/web/ProjectMapController.java:12` (`ev:src/main/java/com/example/web/ProjectMapController.java:12-12:com.example.web.ProjectMapController:@RequestMapping`), `src/main/java/com/example/web/ProjectMapController.java:14` (`ev:src/main/java/com/example/web/ProjectMapController.java:14-14:com.example.web.ProjectMapController#getItem:@GetMapping`), `src/main/java/com/example/web/ProjectMapController.java:16` (`ev:src/main/java/com/example/web/ProjectMapController.java:16-16:com.example.web.ProjectMapController#getItem:@PathVariable:parameter:0:id`), `src/main/java/com/example/web/ProjectMapController.java:17` (`ev:src/main/java/com/example/web/ProjectMapController.java:17-17:com.example.web.ProjectMapController#getItem:@RequestParam:parameter:1:expand`)

## Spring Application Surface

- Spring application surface analysis status: `analyzed`
- Repository stereotype entries are direct `@Repository` annotation observations; they do not prove runtime bean registration or entity ownership.
- Spring Data repository interface entries are inferred source-visible extension signals; repository/entity relation rows, when present, are inferred generic links. They do not prove runtime repositories, query method behavior, database access, or runtime repository/entity verification.
- Configuration classes, configuration-properties types, and `@Bean` methods are source-visible Spring configuration signals; they do not prove runtime bean graphs, binding success, config values, bean scopes, lifecycle, proxy behavior, or dependency graphs.
- Transaction, scheduled, event listener, and messaging listener entries are source-visible operational change-surface signals; they do not prove runtime transaction behavior, scheduler registration, event delivery, message destinations, or broker topology.
- Spring Security configuration warnings are inspection hints and change-risk signals; they do not prove security policy, endpoint protection, authentication behavior, authorization behavior, vulnerability, or correctness.
- Subsection statuses: repositories `analyzed`, configuration classes `analyzed`, configuration properties `analyzed`, bean methods `analyzed`, transaction boundaries `analyzed`, scheduled methods `analyzed`, event listeners `analyzed`, messaging listeners `analyzed`, security warnings `analyzed`.

### Module `module:.` (path: `.`)

- Extracted facts: detected 2 source-visible facts.
  - `spring_repository_stereotype`: `com.example.components.InventoryRepository` (support_type: `extracted`, repository_signal: `direct_repository_stereotype`).
    - Source: `src/main/java/com/example/components/InventoryComponents.java`
    - Evidence: `src/main/java/com/example/components/InventoryComponents.java:16` (`ev:src/main/java/com/example/components/InventoryComponents.java:16-16:com.example.components.InventoryRepository:@Repository`)
  - `spring_configuration_class`: `com.example.components.AppConfiguration` (support_type: `extracted`, configuration_signal: `direct_configuration_class`).
    - Source: `src/main/java/com/example/components/InventoryComponents.java`
    - Evidence: `src/main/java/com/example/components/InventoryComponents.java:20` (`ev:src/main/java/com/example/components/InventoryComponents.java:20-20:com.example.components.AppConfiguration:@Configuration`)
- Inferred signals: detected 1 source-visible signal.
  - `spring_data_repository_interface_signal`: `com.example.repositories.ProjectOrderRepository` extends `org.springframework.data.jpa.repository.JpaRepository` (support_type: `inferred`, repository_signal: `spring_data_repository_interface_extension`).
    - Source: `src/main/java/com/example/repositories/ProjectOrderRepository.java`
    - Evidence: `src/main/java/com/example/repositories/ProjectOrderRepository.java:6-7` (`ev:src/main/java/com/example/repositories/ProjectOrderRepository.java:6-7:com.example.repositories.ProjectOrderRepository:com.example.repositories.ProjectOrderRepository`), `src/main/java/com/example/repositories/ProjectOrderRepository.java:6` (`ev:src/main/java/com/example/repositories/ProjectOrderRepository.java:6-6:com.example.repositories.ProjectOrderRepository:extends:org.springframework.data.jpa.repository.JpaRepository`)
- Inferred repository/entity relations: detected 1 source-visible Spring Data generic relation.
  - `com.example.repositories.ProjectOrderRepository` -> `com.example.domain.ProjectOrder` (entity_relation_status: `inferred`, relation_type: `repository_entity_generic`, support_type: `inferred`, generic_type: `com.example.domain.ProjectOrder`, confidence: `medium`, uncertainty: `null`).
    - Evidence: `src/main/java/com/example/repositories/ProjectOrderRepository.java:6` (`ev:src/main/java/com/example/repositories/ProjectOrderRepository.java:6-6:com.example.repositories.ProjectOrderRepository:extends:org.springframework.data.jpa.repository.JpaRepository`), `src/main/java/com/example/domain/ProjectEntities.java:52` (`ev:src/main/java/com/example/domain/ProjectEntities.java:52-52:com.example.domain.ProjectOrder:@Entity`), `src/main/java/com/example/domain/ProjectEntities.java:53` (`ev:src/main/java/com/example/domain/ProjectEntities.java:53-53:com.example.domain.ProjectOrder:@Table`)
- Uncertain/not-analyzed statuses: detected none.
- Warnings: detected none.

## Detected Spring Components

- Analysis status: `analyzed`
- Component summary: detected 5 direct Spring stereotype components.

### `com.example.components.AppConfiguration`

- Module: Detected `module:.` (path: `.`)
- Stereotypes: Detected `@Configuration`
  - Evidence: `src/main/java/com/example/components/InventoryComponents.java:20` (`ev:src/main/java/com/example/components/InventoryComponents.java:20-20:com.example.components.AppConfiguration:@Configuration`)

### `com.example.components.InventoryComponent`

- Module: Detected `module:.` (path: `.`)
- Stereotypes: Detected `@Component`
  - Evidence: `src/main/java/com/example/components/InventoryComponents.java:8` (`ev:src/main/java/com/example/components/InventoryComponents.java:8-8:com.example.components.InventoryComponent:@Component`)

### `com.example.components.InventoryRepository`

- Module: Detected `module:.` (path: `.`)
- Stereotypes: Detected `@Repository`
  - Evidence: `src/main/java/com/example/components/InventoryComponents.java:16` (`ev:src/main/java/com/example/components/InventoryComponents.java:16-16:com.example.components.InventoryRepository:@Repository`)

### `com.example.components.InventoryService`

- Module: Detected `module:.` (path: `.`)
- Stereotypes: Detected `@Service`
  - Evidence: `src/main/java/com/example/components/InventoryComponents.java:12` (`ev:src/main/java/com/example/components/InventoryComponents.java:12-12:com.example.components.InventoryService:@Service`)

### `com.example.web.ProjectMapController`

- Module: Detected `module:.` (path: `.`)
- Stereotypes: Detected `@RestController`
  - Evidence: `src/main/java/com/example/web/ProjectMapController.java:11` (`ev:src/main/java/com/example/web/ProjectMapController.java:11-11:com.example.web.ProjectMapController:@RestController`)

## Domain And Data Model

- Analysis status: `analyzed`
- Domain summary: detected 5 JPA entity facts and 2 embeddable facts.
- Domain/data facts are source-visible JPA annotations and Spring Data generic signals only; no database schema, runtime Hibernate metadata, migration interpretation, or provider defaults are claimed.
- Extracted entity, field, identifier, embeddable, and relationship facts stay separate from inferred repository/entity links, uncertain relationship targets, and explicit not-analyzed composite-id/runtime boundaries.

### `com.example.domain.ProjectCustomer`

- Module: Detected `module:.` (path: `.`)
- Entity: Detected `com.example.domain.ProjectCustomer`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:34` (`ev:src/main/java/com/example/domain/ProjectEntities.java:34-34:com.example.domain.ProjectCustomer:@Entity`)
- Table: Detected none.
- Field metadata: Detected none.
- Identifier field: Detected `id` (`Long`) declared by `com.example.domain.ProjectCustomer` with source_kind `declared` identifier_kind `simple_id`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:36` (`ev:src/main/java/com/example/domain/ProjectEntities.java:36-36:com.example.domain.ProjectCustomer:@Id:field:id`)
- Relationships: Detected none.

### `com.example.domain.ProjectLegacyOrder`

- Module: Detected `module:.` (path: `.`)
- Entity: Detected `com.example.domain.ProjectLegacyOrder`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:107` (`ev:src/main/java/com/example/domain/ProjectEntities.java:107-107:com.example.domain.ProjectLegacyOrder:@Entity`)
- Table: Detected none.
- IdClass signal: Source-visible type `ProjectLegacyOrderKey` with field_matching_status `not_analyzed` and semantic_reconstruction_status `not_analyzed`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:108` (`ev:src/main/java/com/example/domain/ProjectEntities.java:108-108:com.example.domain.ProjectLegacyOrder:@IdClass`)
- Field metadata: Detected none.
- Identifier field: Detected `orderNumber` (`Long`) declared by `com.example.domain.ProjectLegacyOrder` with source_kind `declared` identifier_kind `simple_id`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:113` (`ev:src/main/java/com/example/domain/ProjectEntities.java:113-113:com.example.domain.ProjectLegacyOrder:@Id:field:orderNumber`)
- Identifier field: Detected `tenantId` (`String`) declared by `com.example.domain.ProjectLegacyOrder` with source_kind `declared` identifier_kind `simple_id`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:110` (`ev:src/main/java/com/example/domain/ProjectEntities.java:110-110:com.example.domain.ProjectLegacyOrder:@Id:field:tenantId`)
- Relationships: Detected none.

### `com.example.domain.ProjectOrder`

- Module: Detected `module:.` (path: `.`)
- Entity: Detected `com.example.domain.ProjectOrder`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:52` (`ev:src/main/java/com/example/domain/ProjectEntities.java:52-52:com.example.domain.ProjectOrder:@Entity`)
- Table: Detected `orders`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:53` (`ev:src/main/java/com/example/domain/ProjectEntities.java:53-53:com.example.domain.ProjectOrder:@Table`)
- Field metadata: Source-visible `id` (`Long`) role `simple_id` annotations `@GeneratedValue`
  - Generated value attributes: Source-visible `strategy=GenerationType.IDENTITY`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:56` (`ev:src/main/java/com/example/domain/ProjectEntities.java:56-56:com.example.domain.ProjectOrder:@GeneratedValue:field:id`)
- Field metadata: Source-visible `status` (`ProjectOrderStatus`) role `basic` annotations `@Column`, `@Enumerated`
  - Column attributes: Source-visible `name=status`, `nullable=false`, `length=32`
  - Enumerated value: Source-visible `EnumType.STRING`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:59` (`ev:src/main/java/com/example/domain/ProjectEntities.java:59-59:com.example.domain.ProjectOrder:@Column:field:status`), `src/main/java/com/example/domain/ProjectEntities.java:60` (`ev:src/main/java/com/example/domain/ProjectEntities.java:60-60:com.example.domain.ProjectOrder:@Enumerated:field:status`)
- Field metadata: Source-visible `version` (`long`) role `version` annotations `@Version`
  - Version: Source-visible `@Version` presence.
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:63` (`ev:src/main/java/com/example/domain/ProjectEntities.java:63-63:com.example.domain.ProjectOrder:@Version:field:version`)
- Identifier field: Detected `id` (`Long`) declared by `com.example.domain.ProjectOrder` with source_kind `declared` identifier_kind `simple_id`
  - Generated value attributes: Source-visible `strategy=GenerationType.IDENTITY`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:55` (`ev:src/main/java/com/example/domain/ProjectEntities.java:55-55:com.example.domain.ProjectOrder:@Id:field:id`), `src/main/java/com/example/domain/ProjectEntities.java:56` (`ev:src/main/java/com/example/domain/ProjectEntities.java:56-56:com.example.domain.ProjectOrder:@GeneratedValue:field:id`)
- Relationship: Uncertain target for `customer` `@ManyToOne` cardinality `many_to_one` declared type `ProjectCustomer`
  - target_resolution: `declared_type_only`
  - uncertainty: `target_type_not_resolved`
  - Relationship attributes: Source-visible `ownership_signal=join_metadata_present`, `optional=false`, `fetch=FetchType.LAZY`, `cascade=[CascadeType.PERSIST, CascadeType.MERGE]`
  - Join column: Source-visible `name=customer_id`, `referenced_column_name=id`, `nullable=false`, `unique=true`, `insertable=false`, `updatable=true`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:66` (`ev:src/main/java/com/example/domain/ProjectEntities.java:66-66:com.example.domain.ProjectOrder:@ManyToOne:field:customer`), `src/main/java/com/example/domain/ProjectEntities.java:67-73` (`ev:src/main/java/com/example/domain/ProjectEntities.java:67-73:com.example.domain.ProjectOrder:@JoinColumn:field:customer`)
- Relationship: Uncertain target for `invoice` `@OneToOne` cardinality `one_to_one` declared type `ProjectInvoice`
  - target_resolution: `declared_type_only`
  - uncertainty: `target_type_not_resolved`
  - Relationship attributes: Source-visible `ownership_signal=join_metadata_present`, `optional=false`, `fetch=FetchType.LAZY`, `orphan_removal=true`
  - Join column: Source-visible `name=invoice_id`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:79` (`ev:src/main/java/com/example/domain/ProjectEntities.java:79-79:com.example.domain.ProjectOrder:@OneToOne:field:invoice`), `src/main/java/com/example/domain/ProjectEntities.java:80` (`ev:src/main/java/com/example/domain/ProjectEntities.java:80-80:com.example.domain.ProjectOrder:@JoinColumn:field:invoice`)
- Relationship: Uncertain target for `lines` `@OneToMany` cardinality `one_to_many` declared type `List<ProjectOrderLine>`
  - target_resolution: `declared_type_only`
  - uncertainty: `target_type_not_resolved`
  - Relationship attributes: Source-visible `mapped_by=order`, `ownership_signal=mapped_by_present`, `cascade=[CascadeType.ALL]`, `orphan_removal=true`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:76` (`ev:src/main/java/com/example/domain/ProjectEntities.java:76-76:com.example.domain.ProjectOrder:@OneToMany:field:lines`)
- Relationship: Uncertain target for `tags` `@ManyToMany` cardinality `many_to_many` declared type `Set<ProjectTag>`
  - target_resolution: `declared_type_only`
  - uncertainty: `target_type_not_resolved`
  - Relationship attributes: Source-visible `ownership_signal=join_metadata_present`, `fetch=FetchType.LAZY`
  - Join table: Source-visible `name=order_tags`, `schema=sales`, `catalog=crm`
    - join_columns: Source-visible `name=order_id`, `referenced_column_name=id`
    - inverse_join_columns: Source-visible `name=tag_id`, `referenced_column_name=id`, `nullable=false`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:83` (`ev:src/main/java/com/example/domain/ProjectEntities.java:83-83:com.example.domain.ProjectOrder:@ManyToMany:field:tags`), `src/main/java/com/example/domain/ProjectEntities.java:84-91` (`ev:src/main/java/com/example/domain/ProjectEntities.java:84-91:com.example.domain.ProjectOrder:@JoinTable:field:tags`)

### `com.example.domain.ProjectShipment`

- Module: Detected `module:.` (path: `.`)
- Entity: Detected `com.example.domain.ProjectShipment`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:95` (`ev:src/main/java/com/example/domain/ProjectEntities.java:95-95:com.example.domain.ProjectShipment:@Entity`)
- Table: Detected none.
- Field metadata: Source-visible `destination` (`ProjectAddress`) role `embedded` annotations `@Embedded`
  - Embedded signal: `@Embedded` declared type `ProjectAddress` target_resolution `source_visible_embeddable` target_class `com.example.domain.ProjectAddress`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:100` (`ev:src/main/java/com/example/domain/ProjectEntities.java:100-100:com.example.domain.ProjectShipment:@Embedded:field:destination`), `src/main/java/com/example/domain/ProjectEntities.java:40` (`ev:src/main/java/com/example/domain/ProjectEntities.java:40-40:com.example.domain.ProjectAddress:@Embeddable`)
- Field metadata: Source-visible `externalAddress` (`ExternalProjectAddress`) role `embedded` annotations `@Embedded`
  - Embedded signal: `@Embedded` declared type `ExternalProjectAddress` target_resolution `declared_type_only` uncertainty `embeddable_target_not_resolved`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:103` (`ev:src/main/java/com/example/domain/ProjectEntities.java:103-103:com.example.domain.ProjectShipment:@Embedded:field:externalAddress`)
- Field metadata: Source-visible `id` (`ProjectShipmentId`) role `embedded_id` annotations `@EmbeddedId`
  - Embedded signal: `@EmbeddedId` declared type `ProjectShipmentId` target_resolution `source_visible_embeddable` target_class `com.example.domain.ProjectShipmentId`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:97` (`ev:src/main/java/com/example/domain/ProjectEntities.java:97-97:com.example.domain.ProjectShipment:@EmbeddedId:field:id`), `src/main/java/com/example/domain/ProjectEntities.java:46` (`ev:src/main/java/com/example/domain/ProjectEntities.java:46-46:com.example.domain.ProjectShipmentId:@Embeddable`)
- Identifier field: Detected `id` (`ProjectShipmentId`) declared by `com.example.domain.ProjectShipment` with source_kind `declared` identifier_kind `embedded_id`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:97` (`ev:src/main/java/com/example/domain/ProjectEntities.java:97-97:com.example.domain.ProjectShipment:@EmbeddedId:field:id`), `src/main/java/com/example/domain/ProjectEntities.java:46` (`ev:src/main/java/com/example/domain/ProjectEntities.java:46-46:com.example.domain.ProjectShipmentId:@Embeddable`)
- Relationships: Detected none.

### `com.example.domain.ProjectVisit`

- Module: Detected `module:.` (path: `.`)
- Entity: Detected `com.example.domain.ProjectVisit`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:122` (`ev:src/main/java/com/example/domain/ProjectEntities.java:122-122:com.example.domain.ProjectVisit:@Entity`)
- Table: Detected `visits`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:123` (`ev:src/main/java/com/example/domain/ProjectEntities.java:123-123:com.example.domain.ProjectVisit:@Table`)
- Field metadata: Detected none.
- Identifier field: Detected `id` (`Long`) declared by `com.example.domain.ProjectBaseEntity` with source_kind `mapped_superclass` identifier_kind `simple_id`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:30` (`ev:src/main/java/com/example/domain/ProjectEntities.java:30-30:com.example.domain.ProjectBaseEntity:@Id:field:id`), `src/main/java/com/example/domain/ProjectEntities.java:28` (`ev:src/main/java/com/example/domain/ProjectEntities.java:28-28:com.example.domain.ProjectBaseEntity:@MappedSuperclass`)
- Relationships: Detected none.

### Embeddables

- Analysis status: `analyzed`
- Embeddable: Detected `com.example.domain.ProjectAddress`
- Module: Detected `module:.` (path: `.`)
  - Source: Detected `src/main/java/com/example/domain/ProjectEntities.java`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:40` (`ev:src/main/java/com/example/domain/ProjectEntities.java:40-40:com.example.domain.ProjectAddress:@Embeddable`)
- Field metadata: Source-visible `postalCode` (`String`) role `basic` annotations `@Column`
  - Column attributes: Source-visible `name=postal_code`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:42` (`ev:src/main/java/com/example/domain/ProjectEntities.java:42-42:com.example.domain.ProjectAddress:@Column:field:postalCode`)
- Embeddable: Detected `com.example.domain.ProjectShipmentId`
- Module: Detected `module:.` (path: `.`)
  - Source: Detected `src/main/java/com/example/domain/ProjectEntities.java`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:46` (`ev:src/main/java/com/example/domain/ProjectEntities.java:46-46:com.example.domain.ProjectShipmentId:@Embeddable`)
- Field metadata: Source-visible `trackingNumber` (`String`) role `basic` annotations `@Column`
  - Column attributes: Source-visible `name=tracking_number`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:48` (`ev:src/main/java/com/example/domain/ProjectEntities.java:48-48:com.example.domain.ProjectShipmentId:@Column:field:trackingNumber`)

## Detected Tests

- Analysis status: `analyzed`
- Test inventory summary: detected 1 test class, 0 framework signals, 0 Spring test slice signals, 0 mock signals, 0 supported JUnit methods, and 1 tested-subject relation/status row.

### `com.example.web.ProjectMapControllerTest`

- Module: Detected `module:.` (path: `.`)
- Test class: Detected `com.example.web.ProjectMapControllerTest`
  - Evidence: `src/test/java/com/example/web/ProjectMapControllerTest.java:3` (`ev:src/test/java/com/example/web/ProjectMapControllerTest.java:3-3:com.example.web.ProjectMapControllerTest:test_file`)
- Source: Detected `src/test/java/com/example/web/ProjectMapControllerTest.java`
- Framework signals: Detected none.
- Test methods: Detected none with supported direct JUnit test annotations.
- Inferred tested subject: `com.example.web.ProjectMapController` in target module `module:.` (path: `.`) (relation_status: `inferred`, relation_type: `naming_convention`, support_type: `inferred`, confidence: `medium`).
  - Evidence: `src/test/java/com/example/web/ProjectMapControllerTest.java:3` (`ev:src/test/java/com/example/web/ProjectMapControllerTest.java:3-3:com.example.web.ProjectMapControllerTest:test_file`), `src/main/java/com/example/web/ProjectMapController.java:13` (`ev:src/main/java/com/example/web/ProjectMapController.java:13-13:com.example.web.ProjectMapController:code_symbol`)

## Quality And Change-Risk Signals

- Quality analysis status: `analyzed`
- Test-gap signals are absence-sensitive planning hints from the bounded test inventory and inferred tested-subject relations. They do not prove coverage gaps, execution behavior, assertion behavior, CI status, or complete subject mapping.
- Change-risk signals are warning-oriented or uncertain planning hints from existing deterministic facts. They do not prove production impact, vulnerability, business priority, correctness, runtime behavior, or test priority.

### Test-Gap Signals

- Analysis status: `analyzed`
- Test-gap signal: `entity_without_obvious_test` for `jpa_entity` `com.example.domain.ProjectCustomer` (status: `no_obvious_test`, inference_basis: `no_inferred_tested_subject_relation_for_subject_class`, confidence: `low`, uncertainty: `bounded_test_inventory_supported_relations_only`). No coverage, execution, assertion, CI, or runtime relation is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `entity:com.example.domain.ProjectCustomer`
  - Subject source hint: class `com.example.domain.ProjectCustomer`, member `not recorded`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:34` (`ev:src/main/java/com/example/domain/ProjectEntities.java:34-34:com.example.domain.ProjectCustomer:@Entity`)
- Test-gap signal: `entity_without_obvious_test` for `jpa_entity` `com.example.domain.ProjectLegacyOrder` (status: `no_obvious_test`, inference_basis: `no_inferred_tested_subject_relation_for_subject_class`, confidence: `low`, uncertainty: `bounded_test_inventory_supported_relations_only`). No coverage, execution, assertion, CI, or runtime relation is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `entity:com.example.domain.ProjectLegacyOrder`
  - Subject source hint: class `com.example.domain.ProjectLegacyOrder`, member `not recorded`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:107` (`ev:src/main/java/com/example/domain/ProjectEntities.java:107-107:com.example.domain.ProjectLegacyOrder:@Entity`), `src/main/java/com/example/domain/ProjectEntities.java:108` (`ev:src/main/java/com/example/domain/ProjectEntities.java:108-108:com.example.domain.ProjectLegacyOrder:@IdClass`)
- Test-gap signal: `entity_without_obvious_test` for `jpa_entity` `com.example.domain.ProjectOrder` (status: `no_obvious_test`, inference_basis: `no_inferred_tested_subject_relation_for_subject_class`, confidence: `low`, uncertainty: `bounded_test_inventory_supported_relations_only`). No coverage, execution, assertion, CI, or runtime relation is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `entity:com.example.domain.ProjectOrder`
  - Subject source hint: class `com.example.domain.ProjectOrder`, member `not recorded`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:52` (`ev:src/main/java/com/example/domain/ProjectEntities.java:52-52:com.example.domain.ProjectOrder:@Entity`), `src/main/java/com/example/domain/ProjectEntities.java:53` (`ev:src/main/java/com/example/domain/ProjectEntities.java:53-53:com.example.domain.ProjectOrder:@Table`)
- Test-gap signal: `entity_without_obvious_test` for `jpa_entity` `com.example.domain.ProjectShipment` (status: `no_obvious_test`, inference_basis: `no_inferred_tested_subject_relation_for_subject_class`, confidence: `low`, uncertainty: `bounded_test_inventory_supported_relations_only`). No coverage, execution, assertion, CI, or runtime relation is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `entity:com.example.domain.ProjectShipment`
  - Subject source hint: class `com.example.domain.ProjectShipment`, member `not recorded`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:95` (`ev:src/main/java/com/example/domain/ProjectEntities.java:95-95:com.example.domain.ProjectShipment:@Entity`)
- Test-gap signal: `entity_without_obvious_test` for `jpa_entity` `com.example.domain.ProjectVisit` (status: `no_obvious_test`, inference_basis: `no_inferred_tested_subject_relation_for_subject_class`, confidence: `low`, uncertainty: `bounded_test_inventory_supported_relations_only`). No coverage, execution, assertion, CI, or runtime relation is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `entity:com.example.domain.ProjectVisit`
  - Subject source hint: class `com.example.domain.ProjectVisit`, member `not recorded`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:122` (`ev:src/main/java/com/example/domain/ProjectEntities.java:122-122:com.example.domain.ProjectVisit:@Entity`), `src/main/java/com/example/domain/ProjectEntities.java:123` (`ev:src/main/java/com/example/domain/ProjectEntities.java:123-123:com.example.domain.ProjectVisit:@Table`)
- Test-gap signal: `repository_without_obvious_test` for `spring_repository` `com.example.components.InventoryRepository` (status: `no_obvious_test`, inference_basis: `no_inferred_tested_subject_relation_for_subject_class`, confidence: `low`, uncertainty: `bounded_test_inventory_supported_relations_only`). No coverage, execution, assertion, CI, or runtime relation is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `spring_repository_stereotype:module:.:com.example.components.InventoryRepository`
  - Subject source hint: class `com.example.components.InventoryRepository`, member `not recorded`
  - Evidence: `src/main/java/com/example/components/InventoryComponents.java:16` (`ev:src/main/java/com/example/components/InventoryComponents.java:16-16:com.example.components.InventoryRepository:@Repository`)
- Test-gap signal: `repository_without_obvious_test` for `spring_repository` `com.example.repositories.ProjectOrderRepository` (status: `no_obvious_test`, inference_basis: `no_inferred_tested_subject_relation_for_subject_class`, confidence: `low`, uncertainty: `bounded_test_inventory_supported_relations_only`). No coverage, execution, assertion, CI, or runtime relation is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `spring_data_repository_interface_signal:module:.:com.example.repositories.ProjectOrderRepository`
  - Subject source hint: class `com.example.repositories.ProjectOrderRepository`, member `not recorded`
  - Evidence: `src/main/java/com/example/repositories/ProjectOrderRepository.java:6-7` (`ev:src/main/java/com/example/repositories/ProjectOrderRepository.java:6-7:com.example.repositories.ProjectOrderRepository:com.example.repositories.ProjectOrderRepository`), `src/main/java/com/example/repositories/ProjectOrderRepository.java:6` (`ev:src/main/java/com/example/repositories/ProjectOrderRepository.java:6-6:com.example.repositories.ProjectOrderRepository:extends:org.springframework.data.jpa.repository.JpaRepository`)

### Change-Risk Signals

- Analysis status: `analyzed`
- Change-risk signal: `jpa_relationship_change_surface` for `jpa_relationship` `com.example.domain.ProjectOrder#customer` (status: `uncertain_planning_hint`, risk_basis: `source_visible_jpa_relationship_metadata`, confidence: `low`, uncertainty: `relationship_target_declared_type_only`). No production impact, vulnerability, correctness, runtime behavior, or business priority is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `entity:com.example.domain.ProjectOrder#relationship:customer`
  - Subject source hint: class `com.example.domain.ProjectOrder`, member `customer`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:66` (`ev:src/main/java/com/example/domain/ProjectEntities.java:66-66:com.example.domain.ProjectOrder:@ManyToOne:field:customer`), `src/main/java/com/example/domain/ProjectEntities.java:67-73` (`ev:src/main/java/com/example/domain/ProjectEntities.java:67-73:com.example.domain.ProjectOrder:@JoinColumn:field:customer`)
- Change-risk signal: `jpa_relationship_change_surface` for `jpa_relationship` `com.example.domain.ProjectOrder#invoice` (status: `uncertain_planning_hint`, risk_basis: `source_visible_jpa_relationship_metadata`, confidence: `low`, uncertainty: `relationship_target_declared_type_only`). No production impact, vulnerability, correctness, runtime behavior, or business priority is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `entity:com.example.domain.ProjectOrder#relationship:invoice`
  - Subject source hint: class `com.example.domain.ProjectOrder`, member `invoice`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:79` (`ev:src/main/java/com/example/domain/ProjectEntities.java:79-79:com.example.domain.ProjectOrder:@OneToOne:field:invoice`), `src/main/java/com/example/domain/ProjectEntities.java:80` (`ev:src/main/java/com/example/domain/ProjectEntities.java:80-80:com.example.domain.ProjectOrder:@JoinColumn:field:invoice`)
- Change-risk signal: `jpa_relationship_change_surface` for `jpa_relationship` `com.example.domain.ProjectOrder#lines` (status: `uncertain_planning_hint`, risk_basis: `source_visible_jpa_relationship_metadata`, confidence: `low`, uncertainty: `relationship_target_declared_type_only`). No production impact, vulnerability, correctness, runtime behavior, or business priority is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `entity:com.example.domain.ProjectOrder#relationship:lines`
  - Subject source hint: class `com.example.domain.ProjectOrder`, member `lines`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:76` (`ev:src/main/java/com/example/domain/ProjectEntities.java:76-76:com.example.domain.ProjectOrder:@OneToMany:field:lines`)
- Change-risk signal: `jpa_relationship_change_surface` for `jpa_relationship` `com.example.domain.ProjectOrder#tags` (status: `uncertain_planning_hint`, risk_basis: `source_visible_jpa_relationship_metadata`, confidence: `low`, uncertainty: `relationship_target_declared_type_only`). No production impact, vulnerability, correctness, runtime behavior, or business priority is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `entity:com.example.domain.ProjectOrder#relationship:tags`
  - Subject source hint: class `com.example.domain.ProjectOrder`, member `tags`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:83` (`ev:src/main/java/com/example/domain/ProjectEntities.java:83-83:com.example.domain.ProjectOrder:@ManyToMany:field:tags`), `src/main/java/com/example/domain/ProjectEntities.java:84-91` (`ev:src/main/java/com/example/domain/ProjectEntities.java:84-91:com.example.domain.ProjectOrder:@JoinTable:field:tags`)
- Change-risk signal: `spring_configuration_change_surface` for `spring_configuration_class` `com.example.components.AppConfiguration` (status: `planning_hint`, risk_basis: `source_visible_spring_configuration`, confidence: `low`, uncertainty: `source_visible_change_surface_only`). No production impact, vulnerability, correctness, runtime behavior, or business priority is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `spring_configuration_class:module:.:com.example.components.AppConfiguration`
  - Subject source hint: class `com.example.components.AppConfiguration`, member `not recorded`
  - Evidence: `src/main/java/com/example/components/InventoryComponents.java:20` (`ev:src/main/java/com/example/components/InventoryComponents.java:20-20:com.example.components.AppConfiguration:@Configuration`)
- Change-risk signal: `spring_service_change_surface` for `spring_service` `com.example.components.InventoryService` (status: `planning_hint`, risk_basis: `source_visible_service_stereotype`, confidence: `low`, uncertainty: `source_visible_change_surface_only`). No production impact, vulnerability, correctness, runtime behavior, or business priority is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `component:com.example.components.InventoryService`
  - Subject source hint: class `com.example.components.InventoryService`, member `not recorded`
  - Evidence: `src/main/java/com/example/components/InventoryComponents.java:12` (`ev:src/main/java/com/example/components/InventoryComponents.java:12-12:com.example.components.InventoryService:@Service`)


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
