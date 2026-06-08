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
- Source-visible Maven metadata: group_id `com.example` (value_kind: `literal`), artifact_id `v04-generated-source-warning` (value_kind: `literal`), version `1.0.0` (value_kind: `literal`), packaging `value:not_declared` (value_kind: `not_declared`).
  - Evidence: `pom.xml:3` (`ev:pom.xml:3-3:build_file:maven:project:groupId`), `pom.xml:4` (`ev:pom.xml:4-4:build_file:maven:project:artifactId`), `pom.xml:5` (`ev:pom.xml:5-5:build_file:maven:project:version`)
- Source-visible direct dependencies: Detected none.
- Source-visible dependency-management declarations: Detected none.
- Source-visible direct plugins: Detected none.
- Source-visible plugin-management declarations: Detected none.
- Resource roots: Not analyzed; status `not_detected`.
- Config files: Not analyzed; status `not_detected`.
- Spring Boot application signals: Detected none.
- Module warnings: Detected 2 warning signals for this module: `generated_source:generated_source_root_path_detected`, `generated_source:generated_source_root_path_detected`. See `Known Uncertainty And Limits` for warning evidence and messages.

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
- Generated-source API warning IDs: status `analyzed`; referenced 2 warning IDs `warning:generated_source:generated_source_root_path_detected:path:target/generated-sources`, `warning:generated_source:generated_source_root_path_detected:path:target/generated-sources/openapi`.
- Repository-rest warning IDs: status `analyzed`; detected none.
- Hidden HTTP warning IDs: status `analyzed`; detected none.

## Spring Application Surface

- Spring application surface analysis status: `analyzed`
- Repository stereotype entries are direct `@Repository` annotation observations; they do not prove runtime bean registration or entity ownership.
- Spring Data repository interface entries are inferred source-visible extension signals; they do not prove runtime repositories, query method behavior, database access, or repository-to-entity relations.
- Repository signals: status `analyzed`; detected none.
- Configuration classes: status `not_analyzed`; not analyzed in the current v0.5 implementation slice because configuration class analyzer has not run.
- Configuration properties: status `not_analyzed`; not analyzed in the current v0.5 implementation slice because configuration-properties analyzer has not run.
- Bean methods: status `not_analyzed`; not analyzed in the current v0.5 implementation slice because bean method analyzer has not run.
- Transaction boundaries: status `not_analyzed`; not analyzed in the current v0.5 implementation slice because transaction analyzer has not run.
- Scheduled methods: status `not_analyzed`; not analyzed in the current v0.5 implementation slice because scheduled method analyzer has not run.
- Event listeners: status `not_analyzed`; not analyzed in the current v0.5 implementation slice because event listener analyzer has not run.
- Messaging listener signals: status `not_analyzed`; not analyzed in the current v0.5 implementation slice because messaging listener analyzer has not run.
- Spring Security configuration warnings: status `not_analyzed`; not analyzed in the current v0.5 implementation slice because security configuration warning analysis has not run.

## Detected Spring MVC Endpoints

- Detected: no Spring MVC endpoints recorded in `project-map.json`.

## Detected Spring Components

- Analysis status: `analyzed`
- Detected: no direct Spring stereotype components recorded.

## Detected JPA Entities

- Analysis status: `analyzed`
- Detected: no direct JPA entities recorded.

## Detected Tests

- Analysis status: `not_detected`
- Not analyzed: no supported test root was detected.

## Known Uncertainty And Limits

- Warning: `generated_source` signal `generated_source_root_path_detected` for module `module:.` (path: `.`) at `target/generated-sources`. Generated-source root path detected; the analyzer records the path signal only and does not read generated source contents or create endpoint/API facts.
  - Evidence: `target/generated-sources` (`ev:target/generated-sources:unknown:path_signal:generated_source_root_path_detected`)
- Warning: `generated_source` signal `generated_source_root_path_detected` for module `module:.` (path: `.`) at `target/generated-sources/openapi`. Generated-source root path detected; the analyzer records the path signal only and does not read generated source contents or create endpoint/API facts.
  - Evidence: `target/generated-sources/openapi` (`ev:target/generated-sources/openapi:unknown:path_signal:generated_source_root_path_detected`)
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
- Not analyzed: v0.5 configuration, bean, transaction, scheduled, event, messaging, and security surface categories remain outside the current repository-signal implementation slice unless their subsection status says `analyzed`.
- Uncertain: no endpoint facts were recorded, so HTTP entry points may be absent or outside the currently supported analyzer scope.
- Uncertain: no entity facts were recorded, so persistence mappings may be absent or outside the currently supported analyzer scope.
- Not analyzed: supported Maven test roots were not detected.

## Practical Inspection Order For Coding Agents

1. Start with detected build, module, and layout facts in `pom.xml`.
2. For HTTP behavior, inspect detected endpoint and hidden-surface warning evidence (no evidence paths recorded).
3. For Spring application surface changes, inspect repository surface and component evidence (no evidence paths recorded) and avoid assuming runtime repository registration, entity ownership, or injection graphs.
4. For persistence changes, inspect detected entity evidence (no evidence paths recorded) and treat relationship targets as declared-type-only.
5. For tests, inspect detected test files and inferred tested-subject evidence (no evidence paths recorded); do not treat inferred subjects as coverage proof.
