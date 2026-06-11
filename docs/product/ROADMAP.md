# Roadmap

## Current Status

`v0.1.0` is the first public local-first Java/Spring CLI release slice. Roadmap Stages 0
through 8 are closed as the historical v0.1 baseline.

Future work is organized by release tracks instead of extending the original v0.1 stage
list. Connector/import work remains post-v0.1 future work and is not started.
The v0.5 deeper Spring application surface release is published with packaged jar and
checksum assets after implementation, guide rendering, real-project evaluation,
review, risk-based security assessment, and release-prep documentation completion.
The v0.6 JPA/domain release is published with packaged jar and checksum assets after
implementation, real-project evaluation, follow-up fixes, release-prep documentation,
and risk-based review/compliance gates. It extracts bounded source-visible entity field
annotations for direct field-level `@Column`, `@Enumerated`, `@GeneratedValue`, and
`@Version`, emits partial embedded and identifier model signals for direct
`@Embeddable`, `@Embedded`, `@EmbeddedId`, and `@IdClass`, extracts bounded
source-visible relationship metadata for direct field-level relationship cardinality,
`mappedBy`, `@JoinColumn`, `@JoinTable`, and direct relationship attributes while
keeping relationship targets declared-type-only and uncertain, infers conservative
repository/entity relations from supported source-visible Spring Data repository generic
types only when exactly one emitted entity fact matches, supports safe JPA-only wildcard
imports for the existing supported JPA annotation set, and omits noisy no-domain guide
sections.
The v0.7 tests, quality, and change-risk release is published with packaged jar and
checksum assets. Generated output uses `schema_version: "0.7"`, test facts carry stable
IDs and `module_id`, direct JUnit/Spring Test framework signals include a source-visible
`signal_kind`, and supported JUnit Jupiter/JUnit 4 test method annotations are emitted
as method inventory with evidence. Direct source-visible Spring test slice annotations
and conservative mock annotation signals are emitted under module-owned test facts
without runtime Spring context, Mockito behavior, or slice-correctness claims.
Conservative tested-subject relation/status rows are emitted from supported naming,
exact production imports, direct field types, and direct Spring test slice class
literals where deterministic. Conservative test-gap and change-risk planning hints are
emitted under the top-level `quality` object from existing deterministic facts and
inferred tested-subject relations, without coverage, execution, assertion, CI, runtime,
correctness, vulnerability, production-impact, or business-priority claims. Real-project
evaluation for the current v0.7 slices and the read-only security/contract audit are
complete with no release-blocking findings.
The v0.8 local Markdown and document ingestion release is published with packaged jar
and checksum assets. Generated output uses `schema_version: "0.8"` and adds a top-level
`documents` object with deterministic default-scope local Markdown discovery policy
metadata, document inventory, ATX heading references, bounded chunk references, resolving
`document` evidence, and conservative `documents.reconciliation` rows as low-confidence
uncertain inspection hints. `agent-guide.md` renders compact local project documentation
orientation from structured document facts and evidence only, without document body
serialization, AI summaries, stale-document truth claims, completeness claims, or
document-backed facts overriding code-backed facts. Real-project v0.8 evaluation and
read-only security/contract audit are complete with no reportable findings or
release-blocking follow-up.

For strategic context, see [POST_V0_1_STRATEGY.md](POST_V0_1_STRATEGY.md). Release
notes and architecture documents are the public source for shipped behavior, contract
semantics, and compatibility notes.

## Roadmap Principles

- Keep the core analyzer deterministic and local-first.
- Keep Java/Spring as the first-class analyzer focus through v1.0.
- Do not upload source code by default.
- Do not treat LLM-generated text as evidence.
- Keep extracted facts, inferred relations, uncertain signals, and document-backed claims
  distinct.
- Update `docs/architecture/OUTPUT_CONTRACT.md` for output field additions, removals,
  renames, or semantic changes.
- Update `docs/architecture/EVIDENCE_MODEL.md` for evidence shape or semantic changes.
- Evaluate meaningful analyzer expansions on pinned real projects before release.
- Keep connectors as optional adapters around the deterministic core.
- Keep optional AI layers separate from the core analyzer and non-authoritative.

## v0.1.0 Historical Baseline (Closed)

The v0.1 implementation includes:

- Java 21 Maven CLI with `scan <path>`.
- Root `pom.xml` Maven detection.
- Standard single-module Maven production and test roots.
- Spring MVC endpoint extraction from source-visible direct controller mappings.
- Source-visible interface-declared Spring MVC mappings when uniquely bindable to
  concrete handlers.
- Deterministic hidden HTTP surface warnings for OpenAPI/Swagger filenames, root Maven
  generator plugins, and direct `@RepositoryRestResource`.
- Direct Spring stereotype component inventory.
- Direct JPA entity inventory with conservative mapped-superclass identifier support.
- Minimal deterministic tests inventory with naming-convention tested-subject
  inferences.
- `project-map.json`, `evidence-index.jsonl`, `endpoints.md`, and `agent-guide.md`.
- Stage 8 evaluation on pinned open-source Spring projects.

The detailed v0.1 scope and limitations are documented in
[V0_1_RELEASE_NOTES.md](V0_1_RELEASE_NOTES.md) and [MVP_SPEC.md](MVP_SPEC.md).

## v0.2.0: Module-Aware Maven Support (Published)

Product outcome: make project memory module-aware for real Maven Java/Spring
repositories while preserving deterministic analysis and evidence discipline.

Shipped scope:

- Root aggregator `pom.xml` detection.
- `<modules>` parsing.
- Module path resolution.
- Child `pom.xml` detection.
- Per-module source and test roots.
- Existing analyzers run per supported module.
- Generated facts can be traced to module identity.
- `project-map.json` gains module inventory after contract design.
- `endpoints.md` and `agent-guide.md` include module orientation after generator updates.
- Multi-module fixtures and golden outputs.
- Real-project multi-module evaluation.

The v0.2 module-aware JSON output and evidence contract is documented in
`docs/architecture/OUTPUT_CONTRACT.md` and `docs/architecture/EVIDENCE_MODEL.md`.
Markdown module grouping, real-project evaluation, release-readiness materials, and
final-baseline security finding fixes are complete. No security blockers remain from the
final v0.2 discovery baseline. The `v0.2.0` tag and GitHub release are published.

Non-goals:

- Gradle.
- Maven profile resolution.
- Effective POM reconstruction.
- Dependency graph reconstruction.
- Running Maven during scan.
- Generated source scanning by default.
- OpenAPI parsing.
- Full symbol solving.
- Runtime Spring model reconstruction.

## v0.3.0: Build And Configuration Model (Published)

Product outcome: add a deterministic module-aware build and configuration orientation
layer on top of the v0.2 Maven module inventory.

Release-ready scope:

- Per-module Maven metadata where source-visible.
- Packaging, parent, dependency, and plugin summaries.
- Spring Boot application signals.
- Resource root and application config discovery.
- Build warnings for generated-source and OpenAPI/Swagger plugin signals.

The v0.3 implementation includes source-visible per-module Maven metadata, direct
dependency/dependency-management inventory, direct plugin/plugin-management inventory,
bounded direct plugin execution and configuration signals, conservative generated-source
plugin warnings, standard resource-root discovery, path-only supported
application/logging config-file inventory, direct source-visible Spring Boot application
signals, and build/config orientation in `agent-guide.md`.

Release readiness notes:

- Real-project v0.3 evaluation is complete on pinned Maven Java/Spring projects.
- Review and risk-based security assessment are complete.
- Final post-fix release security baseline is clean with no reportable findings.
- The `v0.3.0` tag and GitHub release are published with the packaged jar and checksum
  assets.

Non-goals include effective POM reconstruction, dependency repository resolution, Maven
execution, runtime config resolution, and secret extraction.

The v0.3 contract uses source-visible Maven, resource, config, and Spring Boot
signals only. Config discovery is path-oriented and must not store configuration values.
Generated-source and generator plugin signals remain warnings until future explicit scan
modes are designed.

## v0.4.0: Declared And Generated API Surface (Published)

Product outcome: represent API surfaces beyond source-visible Spring MVC controllers
while keeping source-visible endpoints, spec-declared operations, generated API signals,
repository-rest warnings, and hidden HTTP warnings separate.

Release-ready scope:

- Define a stable API surface taxonomy.
- Keep direct source-visible Spring MVC endpoints and source-visible interface-declared
  Spring MVC endpoints as code-backed endpoint facts with separate categories.
- Discover local OpenAPI/Swagger files as declared API inputs.
- Parse minimal OpenAPI/Swagger operations as spec-backed declared API operation facts.
- Keep OpenAPI operations separate from implemented endpoint facts unless separate code
  evidence supports an explicit relation.
- Represent generated-source API signals as warnings unless an explicit future
  generated-source scan mode is designed and enabled.
- Record common generated-source root path presence as path-only warnings without
  reading generated source contents.
- Keep repository-rest warnings separate from unknown hidden HTTP warnings.
- Update `endpoints.md` and `agent-guide.md` to render API surface confidence without
  conflating source, spec, generated, and warning categories.

Non-goals include runtime handler mapping reconstruction, full OpenAPI validation,
external `$ref` fetching, Maven generation, default generated-source scanning,
generated source analysis without an explicit mode, client SDK reconstruction, and any
SaaS, connector, web UI, repository chat, generic RAG, LLM-core, or automatic
code-modification scope.

Implementation work must receive risk-based review when it changes OpenAPI/YAML/JSON
parsing, file discovery, generated-source path handling, source-derived output
rendering, or evidence semantics. A broader release-candidate security review is
expected if parser, filesystem, generated-source, or output changes accumulate.

Release readiness notes:

- Real-project v0.4 evaluation is complete on pinned Java/Spring Maven projects with
  local OpenAPI specs and generated API signals.
- Review and risk-based security assessment are complete with no release-blocking
  findings.
- The `v0.4.0` tag and GitHub release are published with the packaged jar and checksum
  assets.

## v0.5.0: Deeper Spring Application Surface (Published)

Product outcome: make source-visible Spring application change surfaces visible beyond
controllers/components while preserving extracted, inferred, uncertain, and warning
semantics.

Expected scope:

- Repository interface signals. (implemented for the current repository slice)
- `@ConfigurationProperties`. (implemented for the current configuration slice)
- `@Configuration` classes. (implemented for the current configuration slice)
- `@Bean` methods. (implemented for the current configuration slice)
- `@Transactional`, `@Scheduled`, and listener annotations. (implemented for the current
  behavior/messaging slice)
- Security configuration warnings where source-visible. (implemented for the current
  security-warning slice)
- Module-grouped Spring application surface guidance in `agent-guide.md`. (implemented
  for the current guide-rendering slice)

Planned taxonomy:

- direct `@Repository` repository stereotype facts;
- inferred Spring Data repository interface extension signals;
- direct `@Configuration`, `@ConfigurationProperties`, and `@Bean` facts;
- direct transaction, scheduled, event listener, and messaging listener annotation
  signals;
- Spring Security configuration warnings.

Non-goals include runtime bean graph reconstruction, autowiring graph reconstruction,
runtime conditional evaluation, auto-configuration reconstruction, repository-to-entity
relation claims, transaction runtime semantics, scheduling runtime behavior, messaging
topology reconstruction, security policy claims, and endpoint protection claims.

Release readiness notes:

- Real-project v0.5 evaluation is complete on five pinned Java/Spring targets.
- Review and risk-based security assessment are complete with no release-blocking
  findings.
- The release-prep evidence-excerpt decision is resolved: bounded source annotation evidence
  excerpts for `@ConfigurationProperties` and inherited test annotations remain
  acceptable for v0.5; no pre-release symbol-only evidence fix is required.
- The `v0.5.0` tag and GitHub release are published with the packaged jar and checksum
  assets.

## v0.6.0: JPA And Domain Model Deepening (Published)

Product outcome: make source-visible JPA/domain facts more useful for legacy change
planning while preserving extracted, inferred, uncertain, and warning semantics.

Release contract boundary:

- Additional source-visible JPA annotations including `@Column`, `@JoinColumn`,
  `@JoinTable`, `@Embedded`, `@Embeddable`, `@EmbeddedId`, `@IdClass`, `@Enumerated`,
  `@GeneratedValue`, and `@Version`.
- Existing source-visible `@Table(name = "...")` compatibility output remains; structured
  `table_metadata` for name/schema/catalog is still future work and is not emitted in
  the current v0.6 implementation.
- Entity field metadata for supported direct JPA annotations, without claiming complete
  persistent-property inventory or runtime access strategy.
- Embedded and composite identifier signals with explicit partial-support boundaries.
- Relationship metadata for cardinality, `mappedBy`, source-visible join metadata, and
  directly visible annotation attributes while keeping target resolution
  declared-type-only and uncertain.
- Repository-to-entity inferred relations from deterministic source-visible Spring Data
  repository generic types when a unique emitted entity fact supports the link.
- Domain/data guidance in `agent-guide.md` that separates extracted facts, inferred
  links, uncertain targets, and not-analyzed/runtime boundaries.

Non-goals include generated schema reconstruction, database introspection, runtime
Hibernate metadata, DDL reconstruction, JPQL semantic parsing, migration tool
interpretation, and complete ORM model guarantees.

Implementation sequence:

- Contract design for the planned v0.6 output and evidence semantics. (complete)
- Entity field annotation extraction for direct field-level `@Column`, `@Enumerated`,
  `@GeneratedValue`, and `@Version`. (implemented)
- Embedded and identifier model support. (implemented)
- Relationship metadata deepening. (implemented)
- Repository-to-entity inferred relation support. (implemented)
- Guide rendering, fixtures, goldens, and real-project evaluation. (complete)
- Safe JPA wildcard import support follow-up for explicit `jakarta.persistence.*` and
  `javax.persistence.*`. (implemented)
- Quieter no-domain guide rendering follow-up. (implemented)
- Release-prep documentation and version alignment. (complete in this release-prep
  state)

Release readiness notes:

- Real-project v0.6 evaluation is complete on pinned Spring PetClinic, Spring PetClinic
  REST, Spring PetClinic Microservices, and Spring Cloud OpenFeign targets.
- Follow-ups from that evaluation that were required for this release-prep state are
  complete: safe JPA wildcard import support and quieter no-domain guide rendering.
- Additional real-project corpus coverage for embeddables, embedded IDs, id-class
  signals, enumerated fields, and version fields was tracked separately from the release
  and is not a release blocker.
- Release notes exist in [V0_6_RELEASE_NOTES.md](V0_6_RELEASE_NOTES.md).
- The `v0.6.0` tag and GitHub release are published with the packaged jar and checksum
  assets.

## v0.7.0: Tests, Quality, And Change-Risk Map

Product outcome: make source-visible test structure and conservative change-planning
signals more useful while preserving the difference between extracted test facts,
inferred tested-subject relations, uncertain test gaps, and warning/change-risk hints.

Planned contract boundary:

- Better test class inventory under supported standard Maven test roots, preserving the
  existing helper/support/configuration filtering boundary. (implemented for the current
  inventory refinement slice)
- Bounded test method inventory only when a method has directly visible supported JUnit
  test annotations. Method inventory is source structure, not assertion understanding.
  (implemented for supported JUnit Jupiter and JUnit 4 method annotations in the current
  inventory refinement slice)
- Directly visible framework and annotation signals for JUnit Jupiter, JUnit 4, and
  Spring Test where origin can be trusted from source-visible imports or fully qualified
  annotation names. (implemented as direct framework signal classification in the
  current inventory refinement slice)
- Spring test slice signals such as `@SpringBootTest`, `@WebMvcTest`, and
  `@DataJpaTest`, recorded as source-visible annotations and not as runtime Spring
  context behavior. (implemented for direct class-level `@SpringBootTest`,
  `@WebMvcTest`, `@DataJpaTest`, and `@ContextConfiguration` in the current slice)
- Conservative mock annotation signals such as direct source-visible `@MockBean` and
  `@SpyBean` on emitted test classes, recorded as annotation-presence signals and not
  as runtime Spring override, Mockito, bean graph, or slice-correctness behavior.
  (implemented for the current slice)
- Conservative tested-subject inference from supported naming conventions and bounded
  source-visible Spring test slice class literals when they can be matched to emitted
  production facts, plus bounded exact production imports and direct field types.
  (implemented for the current relation/status slice)
- Explicit tested-subject relation statuses such as inferred, not detected, ambiguous,
  unsupported, and not analyzed. (implemented for the current relation/status slice;
  `not_analyzed` remains a reserved compatibility value)
- Test-gap signals emitted only as inferred or uncertain planning hints when no supported
  tested-subject relation is inferred for selected source-visible change surfaces.
  (implemented for the current planning-hint slice)
- Change-risk signals emitted only as warning-oriented or uncertain planning hints from
  existing deterministic facts such as endpoint, Spring application surface, security
  warning, messaging, transaction/scheduled, repository/entity, or JPA relationship
  surfaces. (implemented for the current planning-hint slice)

Non-goals include coverage claims, mutation testing, behavioral assertion understanding,
CI result claims, runtime test execution, runtime Spring context reconstruction, runtime
repository or database verification, and full call graph reconstruction.

Implementation sequence:

- Contract design for the planned v0.7 output and evidence semantics. (complete)
- Test class, annotation, and method inventory refinement. (implemented for the current
  slice)
- Spring test slice and mock annotation signal extraction. (implemented for the current
  slice)
- Conservative tested-subject relation status support. (implemented for the current
  slice)
- Test-gap and change-risk planning hints. (implemented for the current slice)
- Guide rendering, fixtures, and goldens for the current test inventory refinement
  and quality/change-risk planning-hint slices. (implemented for the current slices)
- Real-project evaluation for the current slices. (complete)
- Release readiness and publication. (complete)

Release readiness notes:

- Real-project v0.7 evaluation is complete on pinned Spring PetClinic, Spring PetClinic
  REST, Spring PetClinic Microservices, and Spring Cloud OpenFeign targets.
- Review/security-contract audit is complete with no release-blocking or bounded-fix
  findings.
- Release notes exist in [V0_7_RELEASE_NOTES.md](V0_7_RELEASE_NOTES.md).
- Release-prep validation passed with `mvn test`, `mvn package`, packaged CLI smoke, and
  `git diff --check`.
- The `v0.7.0` tag and GitHub release are published with the packaged jar and checksum
  assets.

## v0.8.0: Local Markdown And Document Ingestion

Product outcome: ingest conservative local Markdown/project documentation as
document-backed context while keeping code-backed facts, inferred relations, uncertain
signals, and document-backed claims separate.

Planned contract boundary:

- Conservative local Markdown discovery under a documented default scope.
- Built-in default exclusions for hidden, generated, dependency, maintainer/private, and
  `.project-memory/` output paths, with any future user include/exclude configuration
  constrained to normalized repository-relative paths.
- Repository-root path containment and a default no-symlink-following policy for
  document discovery.
- A deterministic document inventory in `project-map.json`, including repository-relative
  document paths, Markdown format, optional module ownership, title/heading structure,
  bounded chunk references, and resolving evidence IDs.
- First-class `document` evidence records in `evidence-index.jsonl` for local Markdown
  files, headings, chunks, and document-side reconciliation observations.
- Bounded heading/chunk semantics that avoid storing full document bodies in generated
  output.
- Conservative code-doc reconciliation signals, such as document-only endpoint-like path
  mentions, document-only module references, source-backed API facts with no obvious
  document mention, and module facts with no obvious document mention.
- A local project documentation section in `agent-guide.md` generated only from
  structured document inventory, document evidence, and reconciliation signals.

Reconciliation signals are uncertain inspection hints, not truth. A document mention does
not create a code fact, and a missing document mention does not prove missing
documentation.

Non-goals include PDF/Word parsing, external docs, Confluence/Jira/GitHub/GitLab/YouTrack
connectors, network access, embeddings, vector stores, generic RAG, repository chat,
LLM calls in the core analyzer, local-document summaries generated by AI, broad
filesystem traversal outside the scanned repository root, document claims promoted to
code facts, and automatic code modification.

Implementation work that touches Markdown parsing, repository path handling,
include/exclude behavior, output rendering, evidence serialization, or document-content
scanning should receive focused tests and risk-based review before release.

Implementation sequence:

- Contract design for the planned v0.8 output and evidence semantics. (complete)
- Markdown discovery and default include/exclude rules. (implemented)
- Markdown heading and bounded chunk extraction. (implemented)
- Document evidence index records. (implemented)
- Code-doc reconciliation signals. (implemented)
- Local docs guide rendering and regression coverage. (implemented)
- Real-project evaluation and security/contract audit. (complete)
- Release readiness. (complete for release-prep; awaiting maintainer tag/publish
  approval)

Release readiness notes:

- Real-project v0.8 evaluation is complete on pinned Spring PetClinic, Spring PetClinic
  REST, and Spring PetClinic Microservices targets with local Markdown documentation.
- Repeated packaged CLI scans were deterministic, structural evidence/document
  validation passed, and a read-only security/contract audit found no reportable
  findings or release-blocking follow-up.
- Release notes exist in [V0_8_RELEASE_NOTES.md](V0_8_RELEASE_NOTES.md).
- Release-prep validation passed with `mvn test`, `mvn package`, packaged CLI smoke, and
  `git diff --check`.
- The `v0.8.0` tag and GitHub release are published with the packaged jar and checksum
  assets.

## v0.9.0: CLI, Config, Performance, And Distribution Readiness

Product outcome: make the local CLI predictable, safely configurable, and easier to
evaluate before the v1.0 stabilization track, without weakening the local-first
deterministic analyzer boundary.

Current implementation status:

- The config parser and safe-defaults slice is implemented for root-local
  `agent-project-memory.yml` discovery and optional explicit
  `scan <path> --config <repo-relative-yaml>` selection.
- The current YAML schema is intentionally small: required `version: 1`, optional
  `features.local_markdown`, reserved `features.generated_sources: false` and
  `features.follow_symlinks: false`, and optional `documents.include` /
  `documents.exclude` rules for local Markdown discovery only.
- Generated `project-map.json` uses `schema_version: "0.9"` with redacted `scan`
  metadata. The selected tool config is not evidence and does not create
  `evidence-index.jsonl` records.
- CLI help/version, broader exit-code polish, diagnostics depth, performance evaluation,
  and distribution workflow work remain follow-up v0.9 goals.

Planned contract boundary:

- A root-local YAML scan config file named `agent-project-memory.yml`, plus optional
  explicit CLI config selection, with no global, user-home, environment-variable, or
  network-loaded config discovery. The selected config must be one regular in-root YAML
  file and must not be loaded through symlinks.
- Config precedence: built-in defaults first, then the selected scan-root config file,
  then explicit CLI flags. CLI selection of an explicit config replaces default config
  discovery rather than merging multiple config files.
- Include/exclude semantics based only on normalized repository-relative paths using
  slash separators, no absolute paths, no `./`, no `..`, no scan-root escape, and no
  symlink following by default.
- Conservative local Markdown defaults compatible with v0.8: local document discovery
  remains enabled by default, uses the existing default-scope discovery policy, applies
  the existing built-in exclusions, and does not include hidden, generated, dependency,
  private/internal, maintainer-like, secret-like, or `.project-memory/` output paths by
  default.
- User include/exclude rules may refine local Markdown discovery only within the
  repository-root boundary. Built-in safety exclusions remain non-overridable in the
  initial v0.9 design.
- Feature toggles for local Markdown discovery and reserved later optional scan modes.
  Generated-source scanning and symlink following remain disabled by default; enabling
  generated-source scanning or symlink following must require a later explicit mode,
  implementation, tests, output/evidence contract update, and security review.
- CLI help, version, command validation, exit codes, and diagnostics that are stable
  enough for automation and do not print config values, secrets, source excerpts, raw
  document bodies, or generated output contents.
- A v0.9 `project-map.json` scan summary that records redacted effective config,
  feature, path-policy, and diagnostic metadata. It records config source and
  counts/statuses, not raw config values or user-provided include/exclude patterns.
- No tool-config evidence records in `evidence-index.jsonl`; the config summary is scan
  metadata, not project evidence. Existing `config_file` evidence remains limited to
  project application/logging config file presence facts.
- Performance evaluation and distribution workflow work as follow-up goals after the
  CLI/config contract is implemented.

Non-goals include connector configuration, network or credential handling, global
machine config, telemetry, update checks, plugin loading, package publication,
generated-source scanning by default, broad filesystem traversal outside the scanned
repository root, config value extraction, secret output, source upload, SaaS, web UI,
repository chat, generic RAG, LLM calls in the core analyzer, and automatic code
modification.

## v1.0.0: Stable Java/Spring Local-First Project Memory

v1.0 should stabilize the public Java/Spring Maven project-memory product. It is a
contract and reliability milestone, not just a feature milestone.

Expected readiness:

- Stable module-aware Maven support.
- Stable build/config model.
- Stable API surface taxonomy.
- Useful Spring/JPA/test inventories.
- Local Markdown ingestion either stable or clearly experimental.
- Output compatibility rules.
- Expanded evaluation corpus.
- Acceptable CLI UX and installation path.
- Changelog, release checklist, and public release discipline.

## v1.x: Stable Product Expansion

Possible tracks:

- Gradle Java/Spring support.
- Generated-source and codegen maturity.
- Agent output profiles.
- Incremental scan and performance.
- Lightweight relation graph.
- Local query/read-only explorer.
- Security and secrets safety.
- Public adoption polish.
- v2 architecture preparation.

## v2.x: Extensible Platform, Adapters, And Optional AI

Expected direction:

- Stable input adapter API.
- Normalized source document model.
- Connector provenance model.
- Local import format.
- GitHub/GitLab/Jira/YouTrack/Confluence adapters when explicitly enabled.
- Optional AI presentation layer that reads deterministic memory and never creates
  authoritative evidence.
- Read-only agent integrations such as MCP or agent prompt bundles.
- Workspace and change-impact workflows.

The core analyzer must continue to run without adapters, network access, or AI.

## v3.0.0: Agent-Native Project Memory Platform

The long-term target is a mature local-first evidence-backed project memory platform for
developers and AI coding agents, with deterministic analyzers, explicit evidence, stable
adapter/plugin APIs, optional non-authoritative AI presentation, and agent-native
read-only workflows.
