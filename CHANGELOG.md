# Changelog

All notable changes to this project will be documented in this file.

The format follows the spirit of [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and versioning follows SemVer intent. During the `0.x` line, output contracts may still
evolve, but schema and evidence changes should be explicit in release notes and
architecture documents.

## [Unreleased]

### Added

- Added public v0.3 build/configuration planning documentation.
- Added staged v0.3 module-owned source-visible Maven metadata extraction for direct
  module `groupId`, `artifactId`, `version`, `packaging`, and parent coordinates.

### Changed

- Documented planned `schema_version: "0.3"` build/configuration output and evidence
  contract decisions for source-visible Maven, resource, config-file, Spring Boot, and
  generated-source warning signals.
- Updated public output to `schema_version: "0.3"` with a complete `build_config` shell;
  future v0.3 build/config subsections that are not implemented yet use
  `analysis_status: "not_analyzed"` without claiming empty inventories.

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
