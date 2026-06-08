# Changelog

All notable changes to this project will be documented in this file.

The format follows the spirit of [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and versioning follows SemVer intent. During the `0.x` line, output contracts may still
evolve, but schema and evidence changes should be explicit in release notes and
architecture documents.

## [Unreleased]

### Added

- Added the public v0.5 deeper Spring application surface design boundary, including
  planned taxonomy, output contract direction, evidence semantics, non-goals, follow-up
  goal sequence, and security gate expectations.
- Added the staged v0.5 repository signal analyzer for the repository signal analyzer slice, including direct
  source-visible `@Repository` repository stereotype facts and inferred source-visible
  Spring Data repository interface extension signals.
- Added the staged v0.5 configuration surface analyzer for the configuration, bean, and configuration-properties analyzer slice, including
  direct source-visible `@Configuration` class facts, direct source-visible
  `@ConfigurationProperties` type facts with `binding_status: "not_analyzed"`, and
  direct source-visible `@Bean` method facts with `bean_name_status: "not_analyzed"`.
- Added the staged v0.5 behavior and messaging signal analyzer for the transaction, scheduled, event, and messaging signal slice,
  including direct source-visible `@Transactional` type and method facts, direct
  source-visible `@Scheduled` method facts, direct source-visible `@EventListener`
  method facts, and direct source-visible Kafka/Rabbit listener annotation signals
  without runtime transaction, scheduling, event delivery, or messaging topology claims.
- Added the staged v0.5 Spring Security configuration warning analyzer for the Spring Security configuration warning slice,
  including source-visible supported Spring Security configuration annotation warnings
  and source-visible `SecurityFilterChain` `@Bean` method warnings without endpoint
  protection, authentication, authorization, filter-chain order, vulnerability, or
  correctness claims.
- Added `schema_version: "0.5"` output with top-level
  `spring_application_surface.repositories` repository signal items,
  `spring_application_surface.configuration` configuration class,
  configuration-properties, and bean method items,
  `spring_application_surface.behavior` transaction, scheduled, and event listener
  items, `spring_application_surface.messaging.listener_signals` items, plus
  `spring_application_surface.security.configuration_warnings.warning_ids` references
  to `spring_security` warning items when bounded source-visible security configuration
  signals are detected.
- Added Spring Application Surface guidance in `agent-guide.md` that keeps direct
  repository annotation observations, inferred Spring Data interface signals, and
  repository-to-entity non-analysis distinct, while rendering configuration, bean, and
  configuration-properties facts as source-visible signals rather than runtime bean
  graph or binding claims, and rendering transaction, scheduled, event listener, and
  messaging listener facts as operational change-surface signals rather than runtime
  behavior or topology claims, and rendering Spring Security configuration warnings as
  inspection hints and change-risk signals rather than security policy analysis.
- Added focused repository analyzer fixtures and golden coverage for direct
  `@Repository`, supported Spring Data base interface extensions, spoofed framework
  origins, and output/evidence resolution.
- Added focused configuration analyzer fixtures and golden coverage for direct
  `@Configuration`, direct `@Bean`, direct `@ConfigurationProperties`, wildcard-only
  imports, spoofed framework origins, and output/evidence resolution.
- Added focused behavior/messaging analyzer fixtures and golden coverage for direct
  `@Transactional`, `@Scheduled`, `@EventListener`, Kafka/Rabbit listener annotations,
  wildcard-only imports, spoofed framework origins, output/evidence resolution, and
  destination-like messaging annotation values staying out of generated outputs.
- Added focused Spring Security configuration warning fixtures and golden coverage for
  supported security annotations, modern `SecurityFilterChain` `@Bean` methods,
  wildcard-only imports, spoofed framework origins, output/evidence resolution, and
  conservative warning wording.

### Fixed

- Updated post-release documentation status to mark `v0.4.0` as published with release
  assets and checksums.

## [0.4.0] - 2026-06-07

### Added

- Added the first v0.4 API surface implementation slice: deterministic local
  OpenAPI/Swagger spec file discovery for common `openapi.*` and `swagger.*` filenames
  as declared API inputs.
- Added `api_surface.openapi.spec_files` facts with normalized repository-relative
  paths, optional `module_id` ownership for specs under supported modules, format,
  spec kind, bounded version observations, and `api_spec` evidence.
- Added minimal local OpenAPI/Swagger YAML/JSON operation extraction under
  `api_surface.openapi.operations.items[]` for declared path, HTTP method, bounded
  `operationId`, bounded tags, `implementation_status: "not_analyzed"`, and operation
  `api_spec` evidence.
- Added bounded warnings for invalid, oversized, unsupported, or duplicate local
  OpenAPI/Swagger operation parser inputs without creating endpoint facts.
- Added generated-source root path warning signals for common local
  `target/generated-*` roots with `path_signal` evidence, without reading generated
  source contents or creating endpoint/API facts.
- Added the `API Surface Interpretation` section to `agent-guide.md` from structured
  `project-map.json` facts and resolving evidence.

### Changed

- Updated public output to `schema_version: "0.4"` with endpoint
  `api_surface_category` values and a top-level `api_surface` section.
- Changed OpenAPI operations from an explicit parser placeholder to analyzed declared
  operation facts when supported local specs are present.
- Changed `api_surface.generated_source_api_signals.warning_ids` to reference
  generated-source path warnings and OpenAPI generator output configuration warnings
  when they are backed by warning evidence.
- Changed `endpoints.md` to render source-visible Spring MVC endpoints, declared
  OpenAPI operations, generated-source API signals, repository-rest warnings, and hidden
  HTTP warnings in separate sections.
- Expanded `agent-guide.md` API surface guidance to explain source-visible,
  declared/spec-backed, generated signal, warning, and not-analyzed confidence
  categories without implementation-coverage claims.

### Security

- Completed the v0.4 review/security diff audit over the implementation range with no
  reportable Codex Security findings and no required CS-* fix goals.
- Kept OpenAPI/Swagger parsing bounded and local-only: no network fetching, no external
  `$ref` resolution, invalid specs degrade to warnings, and generated-source paths
  remain warning-only signals without reading generated source contents by default.

### Not Included

- Full OpenAPI validation.
- External `$ref` fetching or network access.
- Maven generation, generated-source content scanning, or generated API reconstruction.
- Runtime API, Spring handler mapping, client SDK, or implementation-coverage claims
  from spec files.

## [0.3.0] - 2026-06-06

### Added

- Added public v0.3 build/configuration planning documentation.
- Added staged v0.3 module-owned source-visible Maven metadata extraction for direct
  module `groupId`, `artifactId`, `version`, `packaging`, and parent coordinates.
- Added staged v0.3 module-owned source-visible Maven dependency inventory for direct
  dependencies and separate dependency-management declarations.
- Added staged v0.3 module-owned source-visible Maven plugin inventory for direct
  plugins, separate plugin-management declarations, bounded direct execution/configuration
  signals, and conservative generated-source warnings.
- Added staged v0.3 module-owned standard resource-root discovery and path-only
  application/logging config-file inventory with config-file evidence that does not
  include config contents.
- Added staged v0.3 module-owned direct source-visible Spring Boot application signal
  extraction for `@SpringBootApplication` classes and bounded source-visible `main`
  method signals.

### Changed

- Documented planned `schema_version: "0.3"` build/configuration output and evidence
  contract decisions for source-visible Maven, resource, config-file, Spring Boot, and
  generated-source warning signals.
- Updated public output to `schema_version: "0.3"` with a complete `build_config` shell;
  v0.3 build/config subsections use explicit `analysis_status` values and do not claim
  effective, resolved, runtime, or generated behavior.
- Changed Maven `dependencies` and `dependency_management` build/config subsections from
  staged placeholders to analyzed source-visible inventories.
- Changed Maven `plugins` and `plugin_management` build/config subsections from staged
  placeholders to analyzed source-visible inventories.
- Changed `resources` and `config_files` build/config subsections from staged
  placeholders to analyzed path inventories when standard resource roots are present.
- Changed `spring_boot_applications` from a staged placeholder to analyzed
  source-visible application signal inventory when supported production source roots are
  present.
- Added build/configuration orientation to `agent-guide.md` from structured
  `project-map.json` facts for Maven metadata, dependencies, dependency management,
  plugins, plugin management, resource roots, config file paths, Spring Boot application
  signals, and module warnings.

### Security

- Bounded generated evidence excerpts across Java annotation/code-symbol evidence,
  warning/test evidence, Maven module discovery evidence, and the evidence-index JSONL
  sink so hostile repository source cannot inflate `.project-memory` outputs through
  oversized source excerpts.
- Completed the final v0.3 Codex Security release baseline after the bounded-excerpt
  fix with no reportable findings.

### Not Included

- Gradle support.
- Maven execution.
- Effective POM reconstruction.
- Parent inheritance resolution into effective coordinates.
- Maven profile activation.
- Remote dependency or plugin resolution.
- Transitive dependency graph reconstruction.
- Runtime Spring configuration resolution.
- Config property key/value inventory or secret extraction.
- Default generated-source scanning.
- OpenAPI YAML/JSON parsing or generated API reconstruction.
- Endpoint creation from build, config, OpenAPI, or generated-source warning signals.
- Connectors for YouTrack, Jira, Confluence, GitHub, or GitLab.
- LLM calls in the core analyzer.
- SaaS, web UI, repository chat, generic RAG, or automatic code modification.

## [0.2.0] - 2026-06-05

### Added

- Added public post-v0.1 strategy documentation.
- Added public v0.2 module-aware Maven planning documentation.
- Documented planned v0.2 module-aware output and evidence contract decisions.
- Added public `schema_version: "0.2"` project-map output with `project.modules`,
  compatibility root summaries, direct `module_id` on module-owned facts, and
  Maven module warnings.
- Added module-aware execution for the existing endpoint, component, JPA entity, hidden
  HTTP surface warning, and tests inventory analyzers.
- Added module-aware `endpoints.md` grouping and `agent-guide.md` orientation based on
  `project.modules` and fact-level `module_id` values.
- Added a real-project v0.2 evaluation report for pinned Maven multi-module
  Java/Spring projects.
- Added v0.2 release notes.
- Adopted a public changelog.
- Added public release process and versioning discipline documentation.

### Changed

- Reorganized the public roadmap from historical v0.1 stages into post-v0.1 release
  tracks.
- Clarified planned v0.2 module-aware schema atomicity, warning analysis status, and
  Maven module warning ID/sort semantics.
- Moved normal generated `project-map.json` output from the v0.1 single-module contract
  to the atomic v0.2 module-aware JSON boundary.
- Clarified release authority and changelog expectations in contributor documentation.
- Clarified public agent boundaries, issue/PR scope wording, development versioning, and
  checkpoint-vs-release commit expectations.
- Aligned the Maven project version and README artifact references with the intended
  `0.2.0` release artifact.

### Security

- Added v0.2 Codex Security gate requirements for the implementation range and
  repository-wide release-candidate scan.
- Fixed unsafe `.project-memory` symlink handling so scan output directories and
  generated output file targets must stay under the canonical scan root.
- Fixed hardlinked generated output targets so scans cannot overwrite outside aliases
  through pre-existing multi-link `.project-memory` files.
- Checkpointed the final v0.2 Codex Security discovery baseline fixes:
  `CS-APM-RC-006` in `53a4fab`, `CS-APM-RC-007` through `CS-APM-RC-009` plus
  `CS-APM-RC-011` in `3156238`, and `CS-APM-RC-010` plus `CS-APM-RC-012` in
  `6b49306`.
- Fixed malformed root `pom.xml` handling so Maven module discovery fails with a
  bounded scan error instead of treating parse failure as an empty module inventory.
- Hardened annotation-origin checks so source-declared fake framework annotations and
  bare/static-imported `RequestMethod` constants do not create trusted Spring MVC,
  component, JPA, hidden HTTP surface, or Spring Test inventory facts.
- Escaped Unicode line and paragraph separators in generated JSON/JSONL strings, and
  stopped `agent-guide.md` evidence classification from inferring evidence type from
  unresolved evidence ID substrings.
- Recorded that no security blockers remain from the final v0.2 discovery baseline and
  that no additional open-ended repository-wide security scan is required before
  `v0.2.0`.

### Not Included

- Gradle support.
- Maven profile resolution.
- Effective POM reconstruction.
- Dependency graph reconstruction.
- Maven execution or code generation during scan.
- Default scanning of generated source roots.
- OpenAPI YAML/JSON parsing or generated API reconstruction.
- Full Java symbol solving.
- Runtime Spring handler mapping, bean graph, or component scanning reconstruction.
- Full ORM runtime behavior.
- Test execution, coverage, CI, or call graph analysis.
- Local Markdown/document ingestion.
- Connectors for YouTrack, Jira, Confluence, GitHub, or GitLab.
- LLM calls in the core analyzer.
- SaaS, web UI, repository chat, generic RAG, or automatic code modification.

## [0.1.0] - 2026-06-03

### Added

- Added the first public local-first Java/Spring CLI release slice.
- Added Java 21 Maven CLI support for `scan <path>`.
- Added root `pom.xml` Maven detection.
- Added standard single-module Maven production and test root detection.
- Added Spring MVC endpoint extraction for source-visible controller mappings.
- Added source-visible interface-declared Spring MVC mappings when uniquely bindable to
  concrete handlers.
- Added deterministic hidden HTTP surface warnings for OpenAPI/Swagger filenames, root
  Maven generator plugins, and direct `@RepositoryRestResource`.
- Added direct Spring stereotype component inventory.
- Added direct JPA entity inventory with conservative mapped-superclass identifier
  support.
- Added minimal deterministic tests inventory with naming-convention tested-subject
  inferences.
- Added `.project-memory/project-map.json`.
- Added `.project-memory/evidence-index.jsonl`.
- Added `.project-memory/endpoints.md`.
- Added `.project-memory/agent-guide.md`.
- Added Stage 8 evaluation reports for pinned open-source Spring projects.

### Not Included

- Full Maven module parsing.
- Gradle or Kotlin support.
- OpenAPI YAML/JSON parsing or generated API reconstruction.
- Maven execution or code generation during scan.
- Default scanning of generated source roots.
- Full Spring runtime reconstruction.
- Full ORM runtime reconstruction.
- Test execution, coverage, CI, or call graph analysis.
- Local Markdown/document ingestion.
- Connectors for YouTrack, Jira, Confluence, GitHub, or GitLab.
- LLM calls in the core analyzer.
- SaaS, web UI, repository chat, generic RAG, or automatic code modification.
