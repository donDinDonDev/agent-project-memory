# Agent Guide

Generated deterministically from `project-map.json` and `evidence-index.jsonl`. The guide generator does not re-analyze source files.

## Read This First

- Open `artifact-set.json` before this guide and respect its artifact authority labels.
- Use this guide as deterministic orientation only. It is not evidence and does not re-analyze source files.
- For large or unknown outputs, prefer `query <path> agent-context`, targeted query commands, focused `project-map.json` selection, exact `evidence-index.jsonl` lookup, and source readback instead of reading every row.
- Size note: this guide is `small-guide` (about `50 KiB`, `380` rendered lines); known generator inputs are `project-map.json` `47 KiB` and `evidence-index.jsonl` `14 KiB`.

## Trust And Verification Legend

Trust and verification legend:
- Use `evidence-index.jsonl` as the authoritative source-backed evidence ledger; verify important claims against its exact records and the repository source locations they cite.
- Generated project facts: `project-map.json` facts; verify important use through their evidence IDs.
- Deterministic presentation: this guide, `endpoints.md`, and query stdout help with orientation; they are not evidence.
- Navigation, provenance, or execution metadata: `artifact-set.json`, `project-graph.json`, `source-registry.json`, profiles, LLM/provider AI output, cache, workspace, adapter output, release metadata, security reports, and downstream-agent output are non-evidence unless a later public contract explicitly changes that.
- Before code changes, review findings, public/security/release wording, or architecture decisions, resolve exact evidence IDs and read the cited source.

## Practical Inspection Order For Coding Agents

1. Start with detected build, module, and layout facts in `pom.xml`, `domain-a/pom.xml`, `domain-b/pom.xml`, `repositories/pom.xml`.
2. For HTTP behavior, inspect detected endpoint and hidden-surface warning evidence (no evidence paths recorded).
3. For Spring application surface changes, inspect Spring application surface and component evidence in `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java`, `domain-a/src/main/java/com/example/unique/UniqueOrder.java` and avoid assuming runtime repository registration, entity ownership, injection graphs, transaction behavior, scheduler registration, event delivery, or messaging topology.
4. For persistence changes, inspect detected entity evidence in `domain-a/src/main/java/com/example/domain/SharedOrder.java`, `domain-a/src/main/java/com/example/unique/UniqueOrder.java`, `domain-b/src/main/java/com/example/domain/SharedOrder.java` and treat field metadata as source-visible annotations only, not runtime schema, provider defaults, or complete access-strategy reconstruction; relationship targets remain declared-type-only.
5. For tests, inspect detected test files and tested-subject relation/status evidence (no evidence paths recorded); do not treat inferred or statused subjects as coverage proof.
6. For quality and change-risk planning, inspect quality signal evidence in `domain-a/src/main/java/com/example/domain/SharedOrder.java`, `domain-a/src/main/java/com/example/unique/UniqueOrder.java`, `domain-b/src/main/java/com/example/domain/SharedOrder.java`, `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java` and treat `no_obvious_test`, warning-oriented, and uncertain statuses as planning hints only, not coverage, runtime, correctness, vulnerability, or business-priority claims.

## Project Memory Overview

- Build/layout: build system `maven`, modules `3`, source roots `3`, test roots `0`.
- Source-backed fact surfaces: endpoints `0`, direct Spring components `0`, Spring application surface rows `7`, entities `3`, embeddables `0`, tests `0`.
- Planning/navigation surfaces: warnings `0`, quality/change-risk hints `15`, local documents `0`, document reconciliation hints `0`.
- Evidence records: `30` records in `evidence-index.jsonl`; this overview is presentation only.
- Size band: `small-guide`; large detailed sections should be selected by task and verified through exact evidence IDs.

## Known Uncertainty Snapshot

- Warnings: `0` warning rows; warning evidence and messages stay in the detailed limits section.
- Inferred or statused rows: `29` rows; keep `inferred`, `ambiguous`, `not_detected`, `unsupported`, and similar labels attached to any use.
- Explicit uncertainty labels: `15` values; preserve those caveats with the cited evidence.
- Not analyzed/out-of-scope status markers: `13`; runtime behavior, generated-source contents, test execution/coverage, source/spec agreement, connectors, and LLM summaries remain outside source-backed evidence unless a later contract says otherwise.

## Not Represented In This Scan

- No represented rows for: `Spring MVC endpoints`, `direct Spring components`, `test classes`, `local project documentation`, `generated-source root metadata`. This means the current deterministic scan emitted no rows for those surfaces; it does not prove the runtime behavior is absent outside the supported analyzer scope.

## Detected Project Layout

- Build system: Detected `maven`
- Root build file: Detected `pom.xml`
  - Evidence: `pom.xml:1` (`ev:pom.xml:1-1:build_file:pom.xml`)
- Source roots: Detected `domain-a/src/main/java`, `domain-b/src/main/java`, `repositories/src/main/java`
  - Evidence: recorded in `project-map.json`; no separate source-root evidence IDs are emitted.
- Test roots: Not analyzed; no supported test roots were recorded.
- Modules analysis status: `analyzed`
- Module: Detected `module:domain-a` (path: `domain-a`)
  - POM path: Detected `domain-a/pom.xml`
  - Support status: `supported`
  - Declaration kind: `root_modules_entry`
  - Declared path: `domain-a`
  - Source roots: Detected `domain-a/src/main/java`
  - Source roots evidence: recorded in `project-map.json`; no separate production root evidence IDs are emitted.
  - Test roots: Not analyzed; no supported test roots were recorded for this module.
  - Declaration evidence: `pom.xml:13` (`ev:pom.xml:13-13:build_file:module:domain-a`)
  - POM evidence: `domain-a/pom.xml:1` (`ev:domain-a/pom.xml:1-1:build_file:pom.xml`)
- Module: Detected `module:domain-b` (path: `domain-b`)
  - POM path: Detected `domain-b/pom.xml`
  - Support status: `supported`
  - Declaration kind: `root_modules_entry`
  - Declared path: `domain-b`
  - Source roots: Detected `domain-b/src/main/java`
  - Source roots evidence: recorded in `project-map.json`; no separate production root evidence IDs are emitted.
  - Test roots: Not analyzed; no supported test roots were recorded for this module.
  - Declaration evidence: `pom.xml:14` (`ev:pom.xml:14-14:build_file:module:domain-b`)
  - POM evidence: `domain-b/pom.xml:1` (`ev:domain-b/pom.xml:1-1:build_file:pom.xml`)
- Module: Detected `module:repositories` (path: `repositories`)
  - POM path: Detected `repositories/pom.xml`
  - Support status: `supported`
  - Declaration kind: `root_modules_entry`
  - Declared path: `repositories`
  - Source roots: Detected `repositories/src/main/java`
  - Source roots evidence: recorded in `project-map.json`; no separate production root evidence IDs are emitted.
  - Test roots: Not analyzed; no supported test roots were recorded for this module.
  - Declaration evidence: `pom.xml:15` (`ev:pom.xml:15-15:build_file:module:repositories`)
  - POM evidence: `repositories/pom.xml:1` (`ev:repositories/pom.xml:1-1:build_file:pom.xml`)

## Build And Configuration Orientation

### Module `module:domain-a` (path: `domain-a`)

- Build/config analysis status: `analyzed`
- Source-visible Maven metadata: group_id `value:not_declared` (value_kind: `not_declared`), artifact_id `domain-a` (value_kind: `literal`), version `value:not_declared` (value_kind: `not_declared`), packaging `value:not_declared` (value_kind: `not_declared`).
  - Evidence: `domain-a/pom.xml:7` (`ev:domain-a/pom.xml:7-7:build_file:maven:project:artifactId`)
- Source-visible direct dependencies: Detected none.
- Source-visible dependency-management declarations: Detected none.
- Source-visible direct plugins: Detected none.
- Source-visible plugin-management declarations: Detected none.
- Resource roots: Not analyzed; status `not_detected`.
- Config files: Not analyzed; status `not_detected`.
- Spring Boot application signals: Detected none.
- Module warnings: Detected none.

### Module `module:domain-b` (path: `domain-b`)

- Build/config analysis status: `analyzed`
- Source-visible Maven metadata: group_id `value:not_declared` (value_kind: `not_declared`), artifact_id `domain-b` (value_kind: `literal`), version `value:not_declared` (value_kind: `not_declared`), packaging `value:not_declared` (value_kind: `not_declared`).
  - Evidence: `domain-b/pom.xml:7` (`ev:domain-b/pom.xml:7-7:build_file:maven:project:artifactId`)
- Source-visible direct dependencies: Detected none.
- Source-visible dependency-management declarations: Detected none.
- Source-visible direct plugins: Detected none.
- Source-visible plugin-management declarations: Detected none.
- Resource roots: Not analyzed; status `not_detected`.
- Config files: Not analyzed; status `not_detected`.
- Spring Boot application signals: Detected none.
- Module warnings: Detected none.

### Module `module:repositories` (path: `repositories`)

- Build/config analysis status: `analyzed`
- Source-visible Maven metadata: group_id `value:not_declared` (value_kind: `not_declared`), artifact_id `repositories` (value_kind: `literal`), version `value:not_declared` (value_kind: `not_declared`), packaging `value:not_declared` (value_kind: `not_declared`).
  - Evidence: `repositories/pom.xml:7` (`ev:repositories/pom.xml:7-7:build_file:maven:project:artifactId`)
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

### Module `module:repositories` (path: `repositories`)

- Extracted facts: detected none.
- Inferred signals: detected 7 source-visible signals.
  - `spring_data_repository_interface_signal`: `com.example.repositories.AmbiguousSharedOrderRepository` extends `org.springframework.data.jpa.repository.JpaRepository` (support_type: `inferred`, repository_signal: `spring_data_repository_interface_extension`).
    - Source: `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java`
    - Evidence: `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:18-19` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:18-19:com.example.repositories.AmbiguousSharedOrderRepository:com.example.repositories.AmbiguousSharedOrderRepository`), `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:18` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:18-18:com.example.repositories.AmbiguousSharedOrderRepository:extends:org.springframework.data.jpa.repository.JpaRepository`)
  - `spring_data_repository_interface_signal`: `com.example.repositories.FqcnUniqueOrderRepository` extends `org.springframework.data.jpa.repository.JpaRepository` (support_type: `inferred`, repository_signal: `spring_data_repository_interface_extension`).
    - Source: `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java`
    - Evidence: `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:12-13` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:12-13:com.example.repositories.FqcnUniqueOrderRepository:com.example.repositories.FqcnUniqueOrderRepository`), `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:12` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:12-12:com.example.repositories.FqcnUniqueOrderRepository:extends:org.springframework.data.jpa.repository.JpaRepository`)
  - `spring_data_repository_interface_signal`: `com.example.repositories.MissingOrderRepository` extends `org.springframework.data.jpa.repository.JpaRepository` (support_type: `inferred`, repository_signal: `spring_data_repository_interface_extension`).
    - Source: `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java`
    - Evidence: `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:15-16` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:15-16:com.example.repositories.MissingOrderRepository:com.example.repositories.MissingOrderRepository`), `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:15` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:15-15:com.example.repositories.MissingOrderRepository:extends:org.springframework.data.jpa.repository.JpaRepository`)
  - `spring_data_repository_interface_signal`: `com.example.repositories.NestedGenericOrderRepository` extends `org.springframework.data.jpa.repository.JpaRepository` (support_type: `inferred`, repository_signal: `spring_data_repository_interface_extension`).
    - Source: `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java`
    - Evidence: `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:21-22` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:21-22:com.example.repositories.NestedGenericOrderRepository:com.example.repositories.NestedGenericOrderRepository`), `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:21` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:21-21:com.example.repositories.NestedGenericOrderRepository:extends:org.springframework.data.jpa.repository.JpaRepository`)
  - `spring_data_repository_interface_signal`: `com.example.repositories.RawOrderRepository` extends `org.springframework.data.jpa.repository.JpaRepository` (support_type: `inferred`, repository_signal: `spring_data_repository_interface_extension`).
    - Source: `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java`
    - Evidence: `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:27-28` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:27-28:com.example.repositories.RawOrderRepository:com.example.repositories.RawOrderRepository`), `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:27` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:27-27:com.example.repositories.RawOrderRepository:extends:org.springframework.data.jpa.repository.JpaRepository`)
  - ... and 2 more Spring application surface inferred signals in `project-map.json`.
- Inferred repository/entity relations: detected 2 source-visible Spring Data generic relations.
  - `com.example.repositories.FqcnUniqueOrderRepository` -> `com.example.unique.UniqueOrder` (entity_relation_status: `inferred`, relation_type: `repository_entity_generic`, support_type: `inferred`, generic_type: `com.example.unique.UniqueOrder`, confidence: `medium`, uncertainty: `null`).
    - Evidence: `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:12` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:12-12:com.example.repositories.FqcnUniqueOrderRepository:extends:org.springframework.data.jpa.repository.JpaRepository`), `domain-a/src/main/java/com/example/unique/UniqueOrder.java:6` (`ev:domain-a/src/main/java/com/example/unique/UniqueOrder.java:6-6:com.example.unique.UniqueOrder:@Entity`)
  - `com.example.repositories.UniqueOrderRepository` -> `com.example.unique.UniqueOrder` (entity_relation_status: `inferred`, relation_type: `repository_entity_generic`, support_type: `inferred`, generic_type: `com.example.unique.UniqueOrder`, confidence: `medium`, uncertainty: `null`).
    - Evidence: `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:9` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:9-9:com.example.repositories.UniqueOrderRepository:extends:org.springframework.data.jpa.repository.JpaRepository`), `domain-a/src/main/java/com/example/unique/UniqueOrder.java:6` (`ev:domain-a/src/main/java/com/example/unique/UniqueOrder.java:6-6:com.example.unique.UniqueOrder:@Entity`)
- Uncertain/not-analyzed statuses: detected 5 explicit statuses.
  - `com.example.repositories.AmbiguousSharedOrderRepository`: `entity_relation_status` is `ambiguous`; no runtime repository/entity relation is claimed.
    - Evidence: `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:18-19` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:18-19:com.example.repositories.AmbiguousSharedOrderRepository:com.example.repositories.AmbiguousSharedOrderRepository`), `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:18` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:18-18:com.example.repositories.AmbiguousSharedOrderRepository:extends:org.springframework.data.jpa.repository.JpaRepository`)
  - `com.example.repositories.MissingOrderRepository`: `entity_relation_status` is `not_detected`; no runtime repository/entity relation is claimed.
    - Evidence: `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:15-16` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:15-16:com.example.repositories.MissingOrderRepository:com.example.repositories.MissingOrderRepository`), `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:15` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:15-15:com.example.repositories.MissingOrderRepository:extends:org.springframework.data.jpa.repository.JpaRepository`)
  - `com.example.repositories.NestedGenericOrderRepository`: `entity_relation_status` is `unsupported`; no runtime repository/entity relation is claimed.
    - Evidence: `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:21-22` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:21-22:com.example.repositories.NestedGenericOrderRepository:com.example.repositories.NestedGenericOrderRepository`), `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:21` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:21-21:com.example.repositories.NestedGenericOrderRepository:extends:org.springframework.data.jpa.repository.JpaRepository`)
  - `com.example.repositories.RawOrderRepository`: `entity_relation_status` is `unsupported`; no runtime repository/entity relation is claimed.
    - Evidence: `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:27-28` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:27-28:com.example.repositories.RawOrderRepository:com.example.repositories.RawOrderRepository`), `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:27` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:27-27:com.example.repositories.RawOrderRepository:extends:org.springframework.data.jpa.repository.JpaRepository`)
  - `com.example.repositories.WildcardGenericOrderRepository`: `entity_relation_status` is `unsupported`; no runtime repository/entity relation is claimed.
    - Evidence: `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:24-25` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:24-25:com.example.repositories.WildcardGenericOrderRepository:com.example.repositories.WildcardGenericOrderRepository`), `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:24` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:24-24:com.example.repositories.WildcardGenericOrderRepository:extends:org.springframework.data.jpa.repository.JpaRepository`)
- Warnings: detected none.

## Detected Spring Components

- Analysis status: `analyzed`
- Detected: no direct Spring stereotype components recorded.

## Domain And Data Model

- Analysis status: `analyzed`
- Domain summary: detected 3 JPA entity facts and 0 embeddable facts.
- Domain/data facts are source-visible JPA annotations and Spring Data generic signals only; no database schema, runtime Hibernate metadata, migration interpretation, or provider defaults are claimed.
- Extracted entity, field, identifier, embeddable, and relationship facts stay separate from inferred repository/entity links, uncertain relationship targets, and explicit not-analyzed composite-id/runtime boundaries.

### `com.example.domain.SharedOrder`

- Module: Detected `module:domain-a` (path: `domain-a`)
- Entity: Detected `com.example.domain.SharedOrder`
  - Evidence: `domain-a/src/main/java/com/example/domain/SharedOrder.java:6` (`ev:domain-a/src/main/java/com/example/domain/SharedOrder.java:6-6:com.example.domain.SharedOrder:@Entity`)
- Table: Detected none.
- Field metadata: Detected none.
- Identifier field: Detected `id` (`Long`) declared by `com.example.domain.SharedOrder` with source_kind `declared` identifier_kind `simple_id`
  - Evidence: `domain-a/src/main/java/com/example/domain/SharedOrder.java:8` (`ev:domain-a/src/main/java/com/example/domain/SharedOrder.java:8-8:com.example.domain.SharedOrder:@Id:field:id`)
- Relationships: Detected none.

### `com.example.unique.UniqueOrder`

- Module: Detected `module:domain-a` (path: `domain-a`)
- Entity: Detected `com.example.unique.UniqueOrder`
  - Evidence: `domain-a/src/main/java/com/example/unique/UniqueOrder.java:6` (`ev:domain-a/src/main/java/com/example/unique/UniqueOrder.java:6-6:com.example.unique.UniqueOrder:@Entity`)
- Table: Detected none.
- Field metadata: Detected none.
- Identifier field: Detected `id` (`Long`) declared by `com.example.unique.UniqueOrder` with source_kind `declared` identifier_kind `simple_id`
  - Evidence: `domain-a/src/main/java/com/example/unique/UniqueOrder.java:8` (`ev:domain-a/src/main/java/com/example/unique/UniqueOrder.java:8-8:com.example.unique.UniqueOrder:@Id:field:id`)
- Relationships: Detected none.

### `com.example.domain.SharedOrder`

- Module: Detected `module:domain-b` (path: `domain-b`)
- Entity: Detected `com.example.domain.SharedOrder`
  - Evidence: `domain-b/src/main/java/com/example/domain/SharedOrder.java:6` (`ev:domain-b/src/main/java/com/example/domain/SharedOrder.java:6-6:com.example.domain.SharedOrder:@Entity`)
- Table: Detected none.
- Field metadata: Detected none.
- Identifier field: Detected `id` (`Long`) declared by `com.example.domain.SharedOrder` with source_kind `declared` identifier_kind `simple_id`
  - Evidence: `domain-b/src/main/java/com/example/domain/SharedOrder.java:8` (`ev:domain-b/src/main/java/com/example/domain/SharedOrder.java:8-8:com.example.domain.SharedOrder:@Id:field:id`)
- Relationships: Detected none.

### Embeddables

- Analysis status: `analyzed`
- Detected: no direct `@Embeddable` classes recorded.

## Detected Tests

- Analysis status: `not_detected`
- Not analyzed: no supported test root was detected.

## Quality And Change-Risk Signals

- Quality analysis status: `analyzed`
- Test-gap signals are absence-sensitive planning hints from the bounded test inventory and inferred tested-subject relations. They do not prove coverage gaps, execution behavior, assertion behavior, CI status, or complete subject mapping.
- Change-risk signals are warning-oriented or uncertain planning hints from existing deterministic facts. They do not prove production impact, vulnerability, business priority, correctness, runtime behavior, or test priority.

### Test-Gap Signals

- Analysis status: `analyzed`
- Test-gap signal: `entity_without_obvious_test` for `jpa_entity` `com.example.domain.SharedOrder` (status: `no_obvious_test`, inference_basis: `bounded_test_inventory_not_available`, confidence: `low`, uncertainty: `supported_test_roots_not_detected`). No coverage, execution, assertion, CI, or runtime relation is claimed.
  - Module: `module:domain-a` (path: `domain-a`)
  - Subject ID: `entity:module:domain-a:com.example.domain.SharedOrder`
  - Subject source hint: class `com.example.domain.SharedOrder`, member `not recorded`
  - Evidence: `domain-a/src/main/java/com/example/domain/SharedOrder.java:6` (`ev:domain-a/src/main/java/com/example/domain/SharedOrder.java:6-6:com.example.domain.SharedOrder:@Entity`)
- Test-gap signal: `entity_without_obvious_test` for `jpa_entity` `com.example.unique.UniqueOrder` (status: `no_obvious_test`, inference_basis: `bounded_test_inventory_not_available`, confidence: `low`, uncertainty: `supported_test_roots_not_detected`). No coverage, execution, assertion, CI, or runtime relation is claimed.
  - Module: `module:domain-a` (path: `domain-a`)
  - Subject ID: `entity:module:domain-a:com.example.unique.UniqueOrder`
  - Subject source hint: class `com.example.unique.UniqueOrder`, member `not recorded`
  - Evidence: `domain-a/src/main/java/com/example/unique/UniqueOrder.java:6` (`ev:domain-a/src/main/java/com/example/unique/UniqueOrder.java:6-6:com.example.unique.UniqueOrder:@Entity`)
- Test-gap signal: `entity_without_obvious_test` for `jpa_entity` `com.example.domain.SharedOrder` (status: `no_obvious_test`, inference_basis: `bounded_test_inventory_not_available`, confidence: `low`, uncertainty: `supported_test_roots_not_detected`). No coverage, execution, assertion, CI, or runtime relation is claimed.
  - Module: `module:domain-b` (path: `domain-b`)
  - Subject ID: `entity:module:domain-b:com.example.domain.SharedOrder`
  - Subject source hint: class `com.example.domain.SharedOrder`, member `not recorded`
  - Evidence: `domain-b/src/main/java/com/example/domain/SharedOrder.java:6` (`ev:domain-b/src/main/java/com/example/domain/SharedOrder.java:6-6:com.example.domain.SharedOrder:@Entity`)
- Test-gap signal: `repository_without_obvious_test` for `spring_repository` `com.example.repositories.AmbiguousSharedOrderRepository` (status: `no_obvious_test`, inference_basis: `bounded_test_inventory_not_available`, confidence: `low`, uncertainty: `supported_test_roots_not_detected`). No coverage, execution, assertion, CI, or runtime relation is claimed.
  - Module: `module:repositories` (path: `repositories`)
  - Subject ID: `spring_data_repository_interface_signal:module:repositories:com.example.repositories.AmbiguousSharedOrderRepository`
  - Subject source hint: class `com.example.repositories.AmbiguousSharedOrderRepository`, member `not recorded`
  - Evidence: `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:18-19` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:18-19:com.example.repositories.AmbiguousSharedOrderRepository:com.example.repositories.AmbiguousSharedOrderRepository`), `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:18` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:18-18:com.example.repositories.AmbiguousSharedOrderRepository:extends:org.springframework.data.jpa.repository.JpaRepository`)
- Test-gap signal: `repository_without_obvious_test` for `spring_repository` `com.example.repositories.FqcnUniqueOrderRepository` (status: `no_obvious_test`, inference_basis: `bounded_test_inventory_not_available`, confidence: `low`, uncertainty: `supported_test_roots_not_detected`). No coverage, execution, assertion, CI, or runtime relation is claimed.
  - Module: `module:repositories` (path: `repositories`)
  - Subject ID: `spring_data_repository_interface_signal:module:repositories:com.example.repositories.FqcnUniqueOrderRepository`
  - Subject source hint: class `com.example.repositories.FqcnUniqueOrderRepository`, member `not recorded`
  - Evidence: `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:12-13` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:12-13:com.example.repositories.FqcnUniqueOrderRepository:com.example.repositories.FqcnUniqueOrderRepository`), `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:12` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:12-12:com.example.repositories.FqcnUniqueOrderRepository:extends:org.springframework.data.jpa.repository.JpaRepository`)
- Test-gap signal: `repository_without_obvious_test` for `spring_repository` `com.example.repositories.MissingOrderRepository` (status: `no_obvious_test`, inference_basis: `bounded_test_inventory_not_available`, confidence: `low`, uncertainty: `supported_test_roots_not_detected`). No coverage, execution, assertion, CI, or runtime relation is claimed.
  - Module: `module:repositories` (path: `repositories`)
  - Subject ID: `spring_data_repository_interface_signal:module:repositories:com.example.repositories.MissingOrderRepository`
  - Subject source hint: class `com.example.repositories.MissingOrderRepository`, member `not recorded`
  - Evidence: `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:15-16` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:15-16:com.example.repositories.MissingOrderRepository:com.example.repositories.MissingOrderRepository`), `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:15` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:15-15:com.example.repositories.MissingOrderRepository:extends:org.springframework.data.jpa.repository.JpaRepository`)
- Test-gap signal: `repository_without_obvious_test` for `spring_repository` `com.example.repositories.NestedGenericOrderRepository` (status: `no_obvious_test`, inference_basis: `bounded_test_inventory_not_available`, confidence: `low`, uncertainty: `supported_test_roots_not_detected`). No coverage, execution, assertion, CI, or runtime relation is claimed.
  - Module: `module:repositories` (path: `repositories`)
  - Subject ID: `spring_data_repository_interface_signal:module:repositories:com.example.repositories.NestedGenericOrderRepository`
  - Subject source hint: class `com.example.repositories.NestedGenericOrderRepository`, member `not recorded`
  - Evidence: `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:21-22` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:21-22:com.example.repositories.NestedGenericOrderRepository:com.example.repositories.NestedGenericOrderRepository`), `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:21` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:21-21:com.example.repositories.NestedGenericOrderRepository:extends:org.springframework.data.jpa.repository.JpaRepository`)
- Test-gap signal: `repository_without_obvious_test` for `spring_repository` `com.example.repositories.RawOrderRepository` (status: `no_obvious_test`, inference_basis: `bounded_test_inventory_not_available`, confidence: `low`, uncertainty: `supported_test_roots_not_detected`). No coverage, execution, assertion, CI, or runtime relation is claimed.
  - Module: `module:repositories` (path: `repositories`)
  - Subject ID: `spring_data_repository_interface_signal:module:repositories:com.example.repositories.RawOrderRepository`
  - Subject source hint: class `com.example.repositories.RawOrderRepository`, member `not recorded`
  - Evidence: `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:27-28` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:27-28:com.example.repositories.RawOrderRepository:com.example.repositories.RawOrderRepository`), `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:27` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:27-27:com.example.repositories.RawOrderRepository:extends:org.springframework.data.jpa.repository.JpaRepository`)
- Test-gap signal: `repository_without_obvious_test` for `spring_repository` `com.example.repositories.UniqueOrderRepository` (status: `no_obvious_test`, inference_basis: `bounded_test_inventory_not_available`, confidence: `low`, uncertainty: `supported_test_roots_not_detected`). No coverage, execution, assertion, CI, or runtime relation is claimed.
  - Module: `module:repositories` (path: `repositories`)
  - Subject ID: `spring_data_repository_interface_signal:module:repositories:com.example.repositories.UniqueOrderRepository`
  - Subject source hint: class `com.example.repositories.UniqueOrderRepository`, member `not recorded`
  - Evidence: `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:9-10` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:9-10:com.example.repositories.UniqueOrderRepository:com.example.repositories.UniqueOrderRepository`), `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:9` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:9-9:com.example.repositories.UniqueOrderRepository:extends:org.springframework.data.jpa.repository.JpaRepository`)
- Test-gap signal: `repository_without_obvious_test` for `spring_repository` `com.example.repositories.WildcardGenericOrderRepository` (status: `no_obvious_test`, inference_basis: `bounded_test_inventory_not_available`, confidence: `low`, uncertainty: `supported_test_roots_not_detected`). No coverage, execution, assertion, CI, or runtime relation is claimed.
  - Module: `module:repositories` (path: `repositories`)
  - Subject ID: `spring_data_repository_interface_signal:module:repositories:com.example.repositories.WildcardGenericOrderRepository`
  - Subject source hint: class `com.example.repositories.WildcardGenericOrderRepository`, member `not recorded`
  - Evidence: `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:24-25` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:24-25:com.example.repositories.WildcardGenericOrderRepository:com.example.repositories.WildcardGenericOrderRepository`), `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:24` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:24-24:com.example.repositories.WildcardGenericOrderRepository:extends:org.springframework.data.jpa.repository.JpaRepository`)

### Change-Risk Signals

- Analysis status: `analyzed`
- Change-risk signal: `repository_entity_relation_uncertain` for `spring_data_repository` `com.example.repositories.AmbiguousSharedOrderRepository` (status: `uncertain_planning_hint`, risk_basis: `repository_entity_relation_status_ambiguous`, confidence: `low`, uncertainty: `bounded_repository_entity_relation_rules_only`). No production impact, vulnerability, correctness, runtime behavior, or business priority is claimed.
  - Module: `module:repositories` (path: `repositories`)
  - Subject ID: `spring_data_repository_interface_signal:module:repositories:com.example.repositories.AmbiguousSharedOrderRepository`
  - Subject source hint: class `com.example.repositories.AmbiguousSharedOrderRepository`, member `not recorded`
  - Evidence: `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:18-19` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:18-19:com.example.repositories.AmbiguousSharedOrderRepository:com.example.repositories.AmbiguousSharedOrderRepository`), `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:18` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:18-18:com.example.repositories.AmbiguousSharedOrderRepository:extends:org.springframework.data.jpa.repository.JpaRepository`)
- Change-risk signal: `repository_entity_relation_uncertain` for `spring_data_repository` `com.example.repositories.MissingOrderRepository` (status: `uncertain_planning_hint`, risk_basis: `repository_entity_relation_status_not_detected`, confidence: `low`, uncertainty: `bounded_repository_entity_relation_rules_only`). No production impact, vulnerability, correctness, runtime behavior, or business priority is claimed.
  - Module: `module:repositories` (path: `repositories`)
  - Subject ID: `spring_data_repository_interface_signal:module:repositories:com.example.repositories.MissingOrderRepository`
  - Subject source hint: class `com.example.repositories.MissingOrderRepository`, member `not recorded`
  - Evidence: `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:15-16` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:15-16:com.example.repositories.MissingOrderRepository:com.example.repositories.MissingOrderRepository`), `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:15` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:15-15:com.example.repositories.MissingOrderRepository:extends:org.springframework.data.jpa.repository.JpaRepository`)
- Change-risk signal: `repository_entity_relation_uncertain` for `spring_data_repository` `com.example.repositories.NestedGenericOrderRepository` (status: `uncertain_planning_hint`, risk_basis: `repository_entity_relation_status_unsupported`, confidence: `low`, uncertainty: `bounded_repository_entity_relation_rules_only`). No production impact, vulnerability, correctness, runtime behavior, or business priority is claimed.
  - Module: `module:repositories` (path: `repositories`)
  - Subject ID: `spring_data_repository_interface_signal:module:repositories:com.example.repositories.NestedGenericOrderRepository`
  - Subject source hint: class `com.example.repositories.NestedGenericOrderRepository`, member `not recorded`
  - Evidence: `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:21-22` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:21-22:com.example.repositories.NestedGenericOrderRepository:com.example.repositories.NestedGenericOrderRepository`), `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:21` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:21-21:com.example.repositories.NestedGenericOrderRepository:extends:org.springframework.data.jpa.repository.JpaRepository`)
- Change-risk signal: `repository_entity_relation_uncertain` for `spring_data_repository` `com.example.repositories.RawOrderRepository` (status: `uncertain_planning_hint`, risk_basis: `repository_entity_relation_status_unsupported`, confidence: `low`, uncertainty: `bounded_repository_entity_relation_rules_only`). No production impact, vulnerability, correctness, runtime behavior, or business priority is claimed.
  - Module: `module:repositories` (path: `repositories`)
  - Subject ID: `spring_data_repository_interface_signal:module:repositories:com.example.repositories.RawOrderRepository`
  - Subject source hint: class `com.example.repositories.RawOrderRepository`, member `not recorded`
  - Evidence: `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:27-28` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:27-28:com.example.repositories.RawOrderRepository:com.example.repositories.RawOrderRepository`), `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:27` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:27-27:com.example.repositories.RawOrderRepository:extends:org.springframework.data.jpa.repository.JpaRepository`)
- Change-risk signal: `repository_entity_relation_uncertain` for `spring_data_repository` `com.example.repositories.WildcardGenericOrderRepository` (status: `uncertain_planning_hint`, risk_basis: `repository_entity_relation_status_unsupported`, confidence: `low`, uncertainty: `bounded_repository_entity_relation_rules_only`). No production impact, vulnerability, correctness, runtime behavior, or business priority is claimed.
  - Module: `module:repositories` (path: `repositories`)
  - Subject ID: `spring_data_repository_interface_signal:module:repositories:com.example.repositories.WildcardGenericOrderRepository`
  - Subject source hint: class `com.example.repositories.WildcardGenericOrderRepository`, member `not recorded`
  - Evidence: `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:24-25` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:24-25:com.example.repositories.WildcardGenericOrderRepository:com.example.repositories.WildcardGenericOrderRepository`), `repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:24` (`ev:repositories/src/main/java/com/example/repositories/RepositoryEntityRelations.java:24-24:com.example.repositories.WildcardGenericOrderRepository:extends:org.springframework.data.jpa.repository.JpaRepository`)


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
- Not analyzed: supported Maven test roots were not detected.
