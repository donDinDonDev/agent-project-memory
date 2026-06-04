# Changelog

All notable changes to this project will be documented in this file.

The format follows the spirit of [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and versioning follows SemVer intent. During the `0.x` line, output contracts may still
evolve, but schema and evidence changes should be explicit in release notes and
architecture documents.

## [Unreleased]

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

### Security

- Fixed unsafe `.project-memory` symlink handling so scan output directories and
  generated output file targets must stay under the canonical scan root.
- Fixed hardlinked generated output targets so scans cannot overwrite outside aliases
  through pre-existing multi-link `.project-memory` files.

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
