# Roadmap

## Current Status

The latest published release is `v2.8.0`, with executable jar and `SHA256SUMS` assets.
Normal no-adapter generated `project-map.json` files use
`schema_version: "1.0"` as the stable-line marker. The v1.5.0 lightweight relation
graph expansion is additive, the v1.6.0 query
expansion adds deterministic read-only lookup commands over existing generated artifacts
without changing generated artifact schemas or evidence semantics, the v1.7.0 release
adds deterministic redaction hardening for selected generated and rendered strings
without adding evidence fields or schema markers, and the v1.8.0 release adds public
examples and contributor onboarding polish without changing analyzer behavior, generated
artifact schemas, or evidence semantics. The v1.9.0 release adds public v2 architecture
preparation for planned adapter, connector provenance, optional AI presentation,
plugin/API security, and v1-to-v2 migration boundaries without implementing adapters,
connectors, network/auth behavior, plugin loading, AI providers, new CLI commands or
flags, generated artifact schema changes, evidence semantic changes, or analyzer
behavior changes.
The v2.0 release includes a disabled-by-default local structured import reference
adapter. No-adapter scans remain on the current base artifact set and
`project-map.json` `schema_version: "1.0"`; explicitly adapter-enabled scans emit
`source-registry.json` and `project-map.json` `schema_version: "2.0"` adapter context
for provenance-backed external/document context only. The v2.1 release adds
disabled-by-default local GitHub/GitLab export import through
`adapters.git_hosting_import`, with Git hosting provenance in `source-registry.json`
schema `1.1` and no adapter-backed query support. Current query support remains focused
on no-adapter v1 artifact sets unless a later release explicitly documents
adapter-aware query behavior.
The v2.2 release adds disabled-by-default Jira/YouTrack/Confluence local export import
through `adapters.connector_import`, with connector provenance in `source-registry.json`
schema `1.2` and the same no-adapter query baseline.
The v2.3 release adds explicitly enabled mock/no-network AI presentation artifacts
under `.project-memory/ai-presentations/` through
`scan --ai-presentation mock_no_network`, while keeping default scans free of AI
presentation artifacts and keeping real provider integration deferred.
The v2.4.0 release adds a CLI-only
`query <path> agent-context` surface that renders deterministic stdout over existing
no-adapter generated artifacts and optional valid graph navigation metadata without
creating generated artifacts, reading source files, adding adapter-aware query,
starting a server/API/editor/plugin runtime, using network or credentials, or creating
automatic code-modification authority.
The v2.5.0 release adds explicit
`workspace scan <config>` support for local workspace YAML configs,
workspace-relative member roots, required unique logical `repo_id` values, unsafe
root rejection, and workspace-root `.project-memory/workspace-map.json` aggregation
from existing member artifacts. It does not run child repository scans, mutate member
`.project-memory/` directories, add cross-repo relations, or add workspace query
behavior.
The v2.6.0 release adds a first change-impact workflow boundary as a
read-only single-repo query over existing no-adapter generated artifacts. It includes
the `query <path> impact --files ...` direct mapping and conservative one-hop
projection implementation for explicit repository-relative changed-file input, required
`project-map.json`, `evidence-index.jsonl`, and `project-graph.json` artifacts,
deterministic text output, direct matches, graph neighbors, relation-status rows,
low-confidence planning hints, explicit `not_represented` rows, and bounded
diagnostics. Generated impact reports, raw diff ingestion, workspace impact,
adapter-aware impact, runtime tracing, call graph, source/spec scoring, documentation
freshness scoring, vulnerability claim, business-priority claim, and automatic code
modification remain out of scope for the first boundary.
The v2.8.0 release adds local distribution and supply-chain hardening around candidate
jar integrity checks. It keeps the published jar plus `SHA256SUMS` as the
supported distribution baseline while adding a no-secret local artifact-integrity
dry-run for candidate jar filename, CLI version output, manifest entrypoint, Maven
artifact metadata, exact dry-run release asset list, and filename-only checksum
verification. Signing, SBOM publication, package-manager channels, native images,
container images, CI release workflow changes, dependency workflow automation, and
release automation remain parked until separately approved, implemented, validated, and
documented.
The v2.9.0 release candidate completes the public v3 preparation design: frozen v3.0
scope, planned schema/API migration, evidence/provenance migration boundary, and release
readiness direction. It does not implement v3 schema markers, migration behavior,
runtime surfaces, provider AI, live connectors, release automation, or new distribution
channels.

The v1.x stable-line compatibility policy treats `project-map.json` and
`evidence-index.jsonl` as the stable machine-readable surface. `endpoints.md` and
`agent-guide.md` remain
deterministic human-readable outputs with stable evidence visibility and cautious fact
boundaries, while exact Markdown presentation may evolve. Future breaking changes,
deprecations, and required migration steps must be documented in architecture docs, the
changelog, and release notes.

The current release line includes module-aware Maven analysis, build/config
orientation, bounded static Gradle Java/Spring layout support, source-visible Spring
MVC and application-surface signals, declared OpenAPI operations, bounded JPA/domain
metadata, source-visible test and quality planning signals, default-scope local Markdown
document inventory, generated-source/codegen metadata-only inventory, opt-in
deterministic agent profile artifacts for supported coding-agent consumption, opt-in
incremental cache metadata and whole-output-set reuse, a bounded lightweight relation
graph artifact, read-only text query commands over existing generated artifacts,
redacted scan metadata, safe root-local YAML config support, stable CLI help/version
behavior, deterministic output redaction for obvious secret-looking values, and a
documented release-jar verification path. The v2.0 release also includes a bounded
local structured import adapter that is disabled by default, local-only, and
provenance-backed. The v2.1 release also includes a bounded local
GitHub/GitLab export import adapter that is disabled by default, local-only, and
provenance-backed. The v2.2 release also includes a bounded local
Jira/YouTrack/Confluence export import adapter that is disabled by default, local-only,
and provenance-backed. The v2.3 release also includes explicitly enabled
mock/no-network AI presentation artifacts that are non-authoritative,
non-evidence, and separate from the base generated artifact set. The v2.4.0 release
also includes a CLI-only `agent-context` query view for read-only
agent/editor consumption over existing no-adapter generated artifacts. The v2.5.0
release also includes explicit workspace map aggregation over configured
local member roots from existing per-repo artifacts. The current
public adoption surface also includes a checked-in generated-output example snapshot
and contributor/reporting templates that point readers back to the output and evidence
contracts.

Earlier v0.x release notes remain available for historical scope, compatibility, and
validation details. Future work is organized by release tracks instead of extending the
original v0.1 baseline. Networked connector work remains future work after the initial
local structured import boundary. The v1.6 expansion is the local query/read-only
explorer, scoped as an
additive v1.0-compatible CLI and output-contract expansion over existing generated
artifacts rather than a `project-map.json` schema marker migration.

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
- Real-project evaluation on pinned open-source Spring projects.

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
- The release evidence-excerpt decision is resolved: bounded source annotation evidence
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
- Release documentation and version alignment. (complete)

Release readiness notes:

- Real-project v0.6 evaluation is complete on pinned Spring PetClinic, Spring PetClinic
  REST, Spring PetClinic Microservices, and Spring Cloud OpenFeign targets.
- Follow-ups required for the v0.6 release are complete: safe JPA wildcard import
  support and quieter no-domain guide rendering.
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
- Release validation passed with `mvn test`, `mvn package`, packaged CLI smoke, and
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
- Release readiness and publication. (complete)

Release readiness notes:

- Real-project v0.8 evaluation is complete on pinned Spring PetClinic, Spring PetClinic
  REST, and Spring PetClinic Microservices targets with local Markdown documentation.
- Deterministic packaged CLI validation, structural evidence/document validation, and a
  read-only security/contract audit completed with no reportable findings or
  release-blocking follow-up.
- Release notes exist in [V0_8_RELEASE_NOTES.md](V0_8_RELEASE_NOTES.md).
- Release validation passed with `mvn test`, `mvn package`, packaged CLI smoke, and
  `git diff --check`.
- The `v0.8.0` tag and GitHub release are published with the packaged jar and checksum
  assets.

## v0.9.0: CLI, Config, Performance, And Distribution Readiness

Product outcome: make the local CLI predictable, safely configurable, and easier to
evaluate before the v1.0 stabilization track, without weakening the local-first
deterministic analyzer boundary.

Published v0.9 implementation status:

- The config parser and safe-defaults slice is implemented for root-local
  `agent-project-memory.yml` discovery and optional explicit
  `scan <path> --config <repo-relative-yaml>` selection.
- The current YAML schema is intentionally small: required `version: 1`, optional
  `features.local_markdown`, reserved `features.generated_sources: false` and
  `features.follow_symlinks: false`, and optional `documents.include` /
  `documents.exclude` rules for local Markdown discovery only.
- Published v0.9 generated `project-map.json` output uses `schema_version: "0.9"` with
  redacted `scan` metadata. The selected tool config is not evidence and does not create
  `evidence-index.jsonl` records.
- CLI help/version commands, bounded command validation, stable exit codes, concise scan
  summaries, and packaged help/version validation are implemented for the current
  v0.9 CLI slice.
- A bounded performance baseline and release artifact/checksum verification discipline
  are complete for the current v0.9 track.
- Installation options research is complete for the current v0.9 track: the minimal
  planned v1.0 path remains a GitHub Release executable jar with optional `SHA256SUMS`
  verification.
- Broader packaged CLI/config/performance validation is complete for the current v0.9
  release. It covered default packaged scans, safe config include/exclude
  behavior, disabled local Markdown behavior, invalid config exit codes, help/version
  behavior, deterministic output stability, and bounded local performance observations.
- Local Markdown discovery, structure extraction, reconciliation mention observation,
  and reconciliation output now have deterministic aggregate caps with bounded
  non-fatal `scan.diagnostics` warnings when caps are reached.
- Release security review follow-up is complete for the current v0.9 track,
  including bounded hardening for path-rule matching, local Markdown aggregate limits,
  OpenAPI/warning traversal, generated-source warning POM reads, and stable no-follow
  spec/POM/root build-file reads.
- The `v0.9.0` tag and GitHub release are published with the packaged jar and checksum
  assets after Maven version alignment, release notes, README/changelog/roadmap status,
  packaged CLI validation, and local plus downloaded jar/checksum verification.

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
- Local Markdown discovery and reconciliation output must remain aggregate-capped so
  document count, accepted Markdown bytes, heading references, chunk references,
  document-side mention observations, reconciliation rows, and document evidence output
  cannot grow without bound.
- User include/exclude rules may refine local Markdown discovery only within the
  repository-root boundary. Built-in safety exclusions remain non-overridable in the
  initial v0.9 design.
- Feature toggles for local Markdown discovery and reserved later optional scan modes.
  Generated-source scanning and symlink following remain disabled by default; enabling
  generated-source scanning or symlink following must require a later explicit mode,
  implementation, tests, output/evidence contract update, and security review.
- CLI help, version, command validation, exit codes, and diagnostics that are stable
  enough for automation and do not print config values, secrets, source excerpts, raw
  document bodies, or generated output contents. Successful scans may report bounded
  non-fatal diagnostics such as local Markdown aggregate cap warnings.
- A v0.9 `project-map.json` scan summary that records redacted effective config,
  feature, path-policy, and diagnostic metadata. It records config source and
  counts/statuses, not raw config values or user-provided include/exclude patterns.
- No tool-config evidence records in `evidence-index.jsonl`; the config summary is scan
  metadata, not project evidence. Existing `config_file` evidence remains limited to
  project application/logging config file presence facts.
- Package-manager distribution channels remain future work unless explicitly implemented
  and documented.

Non-goals include connector configuration, network or credential handling, global
machine config, telemetry, update checks, plugin loading, package publication,
generated-source scanning by default, broad filesystem traversal outside the scanned
repository root, config value extraction, secret output, source upload, SaaS, web UI,
repository chat, generic RAG, LLM calls in the core analyzer, and automatic code
modification.

Installation options research outcome:

| Option | v1.0 recommendation | Rationale |
| --- | --- | --- |
| GitHub Release executable jar plus `SHA256SUMS` | Minimal supported path. | Keeps the Java 21 requirement explicit, supports optional checksum verification, and avoids new registries, signing keys, package indexes, or update automation. |
| First-party shell wrapper | Defer. | Improves command UX but adds install, uninstall, PATH, permission, platform, checksum, and upgrade behavior that should be designed and tested as a separate distribution channel. |
| JBang catalog or app install path | Park for v1.x. | Useful for Java CLI discovery after artifact coordinates or stable jar URLs are mature, but it adds a JBang prerequisite and catalog maintenance surface. |
| Homebrew tap | Park for v1.x. | Good developer UX on macOS/Linux after adoption grows, but it requires formula/tap maintenance, per-release checksum updates, and a tested package-manager workflow. |
| Maven Central publication | Park for v1.x/v2.x infrastructure work. | Helpful for Java ecosystem consumption and possible JBang coordinates, but it is not a minimal CLI install path and requires namespace, metadata, validation, signing, and immutable publication discipline. |
| SDKMAN/asdf plugins, native images, or container images | Park until demand is clear. | Each channel adds channel-specific accounts, plugin or image maintenance, platform testing, and release-process complexity beyond the current Java 21 jar. |

Until one of those parked channels is implemented and documented in a release note,
public install instructions should keep the release jar as the supported path and should
not present package-manager commands as available.

Release readiness notes:

- Packaged CLI/config/performance validation is complete for default scans, safe
  config include/exclude behavior, disabled local Markdown behavior, invalid config
  exit codes, help/version behavior, deterministic output stability, and bounded local
  performance observations.
- Release security review follow-up is complete with no remaining
  release-blocking finding recorded for v0.9.0.
- Release notes exist in [V0_9_RELEASE_NOTES.md](V0_9_RELEASE_NOTES.md).
- Release validation passed with `mvn test`, `mvn package`, packaged CLI validation, checksum
  verification, and `git diff --check`.
- The `v0.9.0` tag and GitHub release are published with the packaged jar and checksum
  assets.

## v1.0.0: Stable Java/Spring Local-First Project Memory

v1.0 should stabilize the public Java/Spring Maven project-memory product. It is a
contract and reliability milestone, not just a feature milestone.

Current development status:

- Normal generated `project-map.json` output uses `schema_version: "1.0"` as a schema
  marker and compatibility-policy migration over the v0.9 output shape.
- The v1.0 schema marker preserves current v0.9 evidence semantics. It does not add
  analyzer capability, change evidence fields, change evidence types, or redesign
  Markdown compatibility by itself.
- v1.0 compatibility expectations are conservative: JSON and JSONL field semantics are
  the stable machine-readable contract, while Markdown outputs are deterministic
  evidence-visible presentations rather than stable parser APIs.
- Expanded v1.0 evaluation corpus validation is complete on pinned representative
  Java/Spring Maven targets covering single-module, multi-module, OpenAPI/spec,
  JPA-heavy, test-heavy, docs/config-heavy, and moderate-size shapes. The public summary
  is [v1.0 Evaluation Corpus Summary](../development/evaluations/v1.0-evaluation-corpus_SUMMARY.md).
- Release hardening is complete for bounded Maven POM/root build-file ingestion, Java
  source discovery and parsing workload, and pre-materialization candidate retention for
  resource config, OpenAPI/Swagger spec, and local Markdown discovery.
- The `v1.0.0` tag and GitHub release are published with the packaged jar and checksum
  assets. Release notes are available in
  [V1_0_RELEASE_NOTES.md](V1_0_RELEASE_NOTES.md).

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

### v1.1.0: Gradle Java/Spring Support (Published)

Product outcome: add static/source-visible Gradle Java/Spring layout support while
preserving the stable Maven v1.0 behavior and evidence semantics.

Released contract boundary:

- Keep normal generated `project-map.json` output on `schema_version: "1.0"` as an
  additive compatibility expansion. Gradle support does not require a
  `schema_version: "1.1"` migration when fields remain additive and the existing Maven
  semantics are preserved.
- Support root `settings.gradle`, `settings.gradle.kts`, `build.gradle`, and
  `build.gradle.kts` as bounded local build-file inputs.
- Support Gradle single-project layouts and simple multi-project layouts declared by
  static string-literal `include` declarations in root settings files, including the
  same bounded literal subset in `settings.gradle.kts`.
- Detect standard Gradle Java/Spring roots only:
  `src/main/java`, `src/test/java`, `src/main/resources`, and `src/test/resources`.
- Represent Gradle project/module identity through normalized repository-relative
  module paths and Gradle project paths without using Gradle execution, project names,
  plugin output, dependency coordinates, or task output as identity.
- Represent mixed Maven/Gradle checkouts as a supported detected state without
  reconstructing an effective unified build model. Module records are de-duplicated by
  normalized module path, and Java/Spring facts are emitted once per supported source
  root.
- Reuse `build_file` evidence for accepted Gradle build files and static settings
  include declarations, with bounded excerpts and repository-relative paths.
- Keep custom Gradle `sourceSets`, custom `projectDir` mappings, `includeBuild`,
  `includeFlat`, variables, loops, conditionals, function indirection, convention
  plugins, plugin/dependency/task resolution, generated-source graphs, and Kotlin source
  analysis out of the v1.1 analyzer scope.
- Degrade unsupported or unsafe Gradle inputs to bounded warnings or scan diagnostics
  rather than dynamic claims.

Completed validation:

- Focused Gradle fixtures for single-project, multi-project, simple Kotlin settings
  include declarations, unsupported dynamic includes, visible but not-analyzed
  `sourceSets`, missing project directories, duplicate project paths, and mixed
  Maven/Gradle roots. (complete)
- Golden output coverage for Gradle output shape, warning IDs, diagnostics, evidence
  reference integrity, deterministic sorting, and guide wording. (complete)
- Maven regression coverage proving pure Maven output and evidence behavior remain
  stable. (complete)
- Packaged CLI evaluation on pinned Gradle Java/Spring projects before release, plus
  selected Maven regression scans. (complete; public summary:
  [v1.1 Gradle Java/Spring Evaluation Summary](../development/evaluations/v1.1-gradle-java-spring_SUMMARY.md))

Release status:

- Release notes exist in [V1_1_RELEASE_NOTES.md](V1_1_RELEASE_NOTES.md).
- Release validation passed with `mvn test`, `mvn package`, Maven and Gradle packaged
  CLI smoke, checksum dry-run, whitespace checks, public marker audit, and final
  risk-based security verification.
- No release-blocking security finding or bounded security-fix goal remains open for
  `v1.1.0`.
- The `v1.1.0` tag and GitHub release are published with the packaged jar and checksum
  assets.

Non-goals:

- Gradle execution.
- Dynamic buildscript evaluation.
- Gradle Tooling API, wrapper execution, daemon/task execution, plugin resolution,
  dependency resolution, repository access, lockfile interpretation, or effective Gradle
  model reconstruction.
- Kotlin source analysis or broad Kotlin DSL semantic parsing.
- Custom source-set root emission in v1.1.
- Connectors, network/auth, SaaS, web UI, repository chat, generic RAG, LLM calls in the
  core analyzer, or automatic code modification.

### v1.2.0: Generated Sources And Codegen Maturity (Published)

Product outcome: make generated-source and code generation handling more explicit,
safer, and more useful while preserving the default behavior that generated source
contents are not read.

Contract decision:

- v1.2 stays warning/config/metadata-only.
- No default generated-source content scanning is introduced.
- No non-default generated-source content scan mode is introduced in v1.2.
- The existing `features.generated_sources` config key remains a reserved disabled
  content-scan toggle. `false` is valid, and attempts to enable generated-source
  content scanning remain invalid config until a later explicit contract changes that.
- Generated-source root presence, generator declarations, annotation-processor signals,
  and build-helper add-source signals remain metadata, inventory, warning, or guide
  orientation signals. They must not create endpoint, API operation, Spring/JPA/test, or
  generated source content facts.
- Generated-source root inventory and diagnostics are additive `schema_version: "1.0"`
  compatibility expansion fields. Existing Maven and Gradle source-visible facts,
  warning IDs, evidence semantics, and disabled-mode output meanings are preserved.
- Existing evidence types are sufficient for the v1.2 metadata-only boundary:
  `path_signal` for generated-root path observations and `build_file` for generator
  declarations. No generated source content evidence is emitted in v1.2.

Origin and claim-separation policy:

- Existing source-visible Java facts remain human-source facts backed by source-root
  Java evidence.
- OpenAPI/Swagger operations remain spec-backed declared operation facts.
- Local Markdown facts and reconciliation rows remain document-backed orientation or
  uncertain inspection hints.
- Inferred and uncertain rows continue to use their existing support, confidence,
  status, and uncertainty fields.
- Generated-source root inventory is metadata-only unless a later explicit content scan
  contract is designed. It must be visibly distinct from human-authored source roots and
  must use `content_status: "not_scanned"` or equivalent cautious wording.
- A future generated-source content scan, if ever added, must be a separate
  non-default mode with explicit path policy, caps, fact-level generated-source labels,
  evidence semantics, fixtures, goldens, evaluation, and risk-based security review. It
  must not be inferred from `features.generated_sources: true` alone.

Release readiness notes:

- Public release notes are available in
  [V1_2_RELEASE_NOTES.md](V1_2_RELEASE_NOTES.md).
- Focused tests and goldens cover generated-root inventory, disabled config behavior,
  diagnostics, warning references, deterministic sorting, and guide wording.
- Maven and Gradle regression coverage proves disabled-mode source-visible output and
  evidence semantics are preserved.
- Packaged CLI release-prep validation passed for generated-source metadata behavior,
  help/version metadata, artifact metadata, and checksum verification.
- Risk-based release review completed with no release-blocking findings remaining.
- The `v1.2.0` tag and GitHub release are published with the packaged jar and checksum
  assets.

Non-goals:

- Generator execution.
- Generated-source content scanning, whether default or opt-in.
- Dependency, plugin, task, Gradle Tooling API, Maven lifecycle, generated-source graph,
  or effective build model reconstruction.
- Runtime API freshness, runtime Spring handler mapping, generated client SDK
  reconstruction, or automatic OpenAPI/source implementation matching.
- Custom Gradle `sourceSets` support unless a separate Gradle follow-up explicitly
  designs it.
- Connectors, network/auth, SaaS, web UI, repository chat, generic RAG, LLM calls in the
  core analyzer, or automatic code modification.

Validation expectations:

- Focused tests and goldens for generated-root inventory, disabled config behavior,
  diagnostics, warning references, deterministic sorting, and guide wording.
- Maven and Gradle regression coverage proving disabled-mode source-visible output and
  evidence semantics are preserved.
- Packaged CLI evaluation on representative generated-source/codegen projects before
  release, plus selected Maven and Gradle regression scans.
- Public documentation, output contract, evidence model, changelog, and release notes
  must agree before release.

### v1.3.0: Agent Output Profiles (Published)

Product outcome: add deterministic, opt-in agent-consumption profile artifacts while
preserving the local-first core analyzer and the existing project-memory facts.

Release contract decisions:

- Supported canonical profile names are `codex`, `claude`, `cursor`, and `generic`.
  The selector value `all` may request every supported profile. No other aliases are
  part of the initial v1.3 contract.
- Profile generation is opt-in. A normal scan with no profile selector keeps the
  current default generated output set and does not create profile artifacts.
- The implemented CLI surface is a repeatable
  `scan <path> --agent-profile <profile>` selector, with `--agent-profile all` as a
  convenience for the full supported set.
  Profile selection is not part of root-local YAML config in the initial v1.3 design.
- Opt-in profile artifacts are generated under `.project-memory/agent-profiles/`, with
  one Markdown file per selected profile and a small `manifest.json` inventory for
  generated-profile detection. The profile manifest is profile-output metadata, not
  project evidence.
- Existing `project-map.json`, `evidence-index.jsonl`, `endpoints.md`, and
  `agent-guide.md` behavior remains stable when profiles are not requested.
  `agent-guide.md` remains the generic deterministic orientation guide rather than
  becoming a selected profile.
- The v1.3 profile surface is an additive `schema_version: "1.0"` compatibility
  expansion. The design does not add profile-generation fields to `project-map.json`,
  does not change `evidence-index.jsonl`, and does not create new evidence records.
- The v1.3.0 implementation supports the opt-in profile selector, writes
  `agent-profiles/manifest.json`, and writes selected deterministic profile Markdown
  content generated from existing structured facts and evidence references.

Profile content boundary:

- Profiles are deterministic Markdown presentations generated from existing structured
  project facts and existing evidence references, or from the same in-memory facts used
  to write the base outputs.
- Profiles may differ by reading order, section labels, and copyable prompt or
  instruction snippets tailored to the selected agent, but they must preserve the same
  underlying fact meanings and evidence discipline.
- Profile wording must keep extracted facts, inferred relations, uncertain signals,
  document-backed hints, spec-backed declared operations, generated-source
  metadata-only observations, warnings, and not-analyzed areas distinct.
- Profile snippets are copyable guidance only. The mainline v1.3 design must not modify
  root repository instruction files such as `AGENTS.md`, `CLAUDE.md`, Cursor rule files,
  IDE settings, source files, docs, or config files.
- Profiles must not call LLMs, external services, editors, connectors, or local agent
  runtimes. They must not summarize source or document bodies, create tasks, invent
  architecture, claim runtime behavior, or treat downstream agent output as evidence.

Validation expectations:

- Focused CLI and output-path tests for profile selection, `all`, unsupported profile
  names, default no-profile behavior, and stable profile artifact names.
- Golden Markdown outputs for every supported profile plus a manifest golden.
- Regression tests proving existing default outputs remain stable when no profile is
  requested.
- Evidence-reference integrity checks for profile Markdown, including capped
  presentation of long reference lists and pointers back to `evidence-index.jsonl`.
- Repeated-output determinism checks on representative generated project-memory
  outputs, plus evaluation for concise usefulness and claim separation before release.

Implementation sequence:

- Add the opt-in profile artifact and invocation foundation while preserving the default
  no-profile scan behavior. (implemented in v1.3.0)
- Add deterministic profile content generation for the supported profile set from
  existing structured facts and evidence references. (implemented in v1.3.0)
- Evaluate profile outputs on representative generated project-memory outputs, then
  prepare release documentation only after artifact, content, compatibility, and
  validation expectations are satisfied. (complete)

Release readiness notes:

- Focused CLI, output-path, manifest, profile Markdown, default no-profile, and
  evidence-reference tests are implemented.
- Profile outputs were evaluated on representative Maven, Gradle, generated-source, and
  quality-guide fixtures with repeated digest checks, structural manifest validation,
  normalized profile paths, evidence ID resolution, and claim-boundary checks.
- Default no-profile scans preserve the existing four-file output behavior and do not
  create `agent-profiles/`.
- Public release notes are available in
  [V1_3_RELEASE_NOTES.md](V1_3_RELEASE_NOTES.md).
- Final local release-prep validation passed with `mvn test`, `mvn package`, packaged
  CLI profile/no-profile smoke, checksum dry-run, whitespace checks, public marker
  audit, and risk-based release review for the final release-prep diff.
- No release-blocking security finding or bounded security-fix goal remains open for
  `v1.3.0`.
- The `v1.3.0` tag and GitHub release are published with the packaged jar and checksum
  assets.

Non-goals:

- LLM calls, AI-generated summaries, source upload, connectors, network/auth, editor
  integrations, MCP/server surfaces, plugin platforms, repository chat, generic RAG,
  automatic repository-file modification, generated-source content scanning, or build
  execution.
- Writing or updating repository root agent instruction/config files by default.
- Adding profile-driven project facts, evidence records, runtime claims, security
  correctness claims, or source/document body summaries.

### v1.4.0: Incremental Scan And Performance (Published)

Product outcome: improve repeat scans for larger local Java/Spring repositories while
preserving full-scan correctness, deterministic outputs, local-only operation, and the
existing evidence model.

Current implementation status:

- The incremental cache implementation is published in v1.4.0. Opt-in
  `scan <path> --incremental` validates `.project-memory/cache/v1/manifest.json`,
  `inputs.jsonl`, and `outputs.jsonl`; unchanged repository states can reuse the
  existing generated output set, while missing, stale, unsafe, corrupted, or mismatched
  cache state falls back to full analysis and refreshes metadata after successful output
  generation.
- Normal scans without `--incremental` continue to ignore persistent cache state.

Planned contract boundary:

- Full scan remains the compatibility baseline. A scan without the incremental selector
  should continue to run normal full analysis and should not depend on cache state.
- Incremental behavior is explicit and opt-in through the `scan <path> --incremental`
  selector. Root-local YAML config does not enable incremental scans in the initial
  design, and there is no daemon, remote cache, or background service.
- The initial v1.4 cache is metadata-only and repository-contained under
  `.project-memory/cache/v1/`. It is not a new source of project facts.
- The initial reuse model is whole-output-set reuse for unchanged repository states.
  When cache metadata, selected CLI options, selected config, selected agent profiles,
  input fingerprints, output fingerprints, schema, or tool-version expectations do not
  match, incremental mode falls back to full analysis and refreshes cache metadata after
  successful output generation.
- Incremental output for the same repository state and selected options must byte-match
  full scan output for the same generated artifact set. The planned contract does not
  add cache-hit or timing fields to `project-map.json`.
- Cache metadata may store normalized repository-relative paths, SHA-256 fingerprints,
  byte counts, schema/tool/option/config/profile matching metadata, and generated output
  digests. It must not store source bodies, local document bodies, config contents, raw
  build-script bodies, generated-source contents, generated Markdown bodies, raw command
  logs, timing measurements, local absolute paths, credentials, tokens, or
  secret-looking values.
- Cache files, fingerprints, cache hits or misses, invalidation decisions, output
  digests, generated Markdown, diagnostics, timing observations, and LLM output are not
  evidence. Generated project facts must continue to reference source-backed records in
  `evidence-index.jsonl`.
- Corrupt, missing, stale, unsafe, schema-mismatched, option-mismatched,
  config-mismatched, profile-mismatched, tool-version-mismatched, or otherwise unclear
  cache state must fail closed to full analysis.

Non-goals:

- Partial per-module, per-analyzer, or per-source-file fact reuse in the initial v1.4
  design.
- Source content caches, document body caches, config value caches, generated-source
  content caches, raw output transcript caches, or cache files outside `.project-memory/`.
- Generated-source content scanning, generator execution, Maven lifecycle execution,
  Gradle task execution, dependency/plugin/task/repository resolution, effective build
  model reconstruction, or source/spec implementation matching.
- Connectors, network/auth, telemetry, SaaS, web UI, repository chat, generic RAG, LLM
  calls in the core analyzer, automatic code modification, package-manager
  publication, release automation, or artifact upload automation.

Validation expectations before release:

- Focused tests for cache path containment, schema/version mismatches, fingerprint
  changes, file additions, edits, deletions and renames, unsafe paths, corruption,
  config/option/profile mismatches, output digest mismatches, and unchanged-state cache
  hits.
- Regression tests proving normal full scan output remains stable when incremental mode
  is not selected.
- Full scan versus incremental scan parity checks for base outputs and selected agent
  profile artifacts.
- Cache content inspection proving cache metadata stays within the documented
  sensitive-data boundary.
- Packaged CLI evaluation on representative fixtures and larger local targets before
  release, plus risk-based review for cache, path, filesystem, config, output, and
  evidence-boundary behavior.

Release readiness notes:

- Public release notes are available in
  [V1_4_RELEASE_NOTES.md](V1_4_RELEASE_NOTES.md).
- A public incremental cache evaluation summary is available in
  [../development/evaluations/v1.4-incremental-cache_SUMMARY.md](../development/evaluations/v1.4-incremental-cache_SUMMARY.md).
- Focused tests cover cache path containment, symlink and hardlink cache paths, cache
  schema and corruption handling, fingerprint changes, file additions, edits,
  deletions and renames, config/option/profile mismatches, output digest mismatches,
  normal no-incremental stability, and unchanged-state cache hits.
- Final local release-prep validation passed with `mvn test`, `mvn package`, packaged
  CLI full/incremental/profile smoke, checksum dry-run in a clean local asset
  directory, whitespace checks, public marker audit, and risk-based release-prep review
  for the final release-prep diff.
- No release-blocking security finding or bounded security-fix goal remains open for
  `v1.4.0`.
- The `v1.4.0` tag and GitHub release are published with the packaged jar and checksum
  assets.

### v1.5.0: Lightweight Relation Graph (Published)

Product outcome: add a bounded deterministic graph artifact that helps humans and
coding agents navigate relationships between existing project-memory facts without
turning the graph into a full architecture, runtime dependency, or impact model.

Published v1.5 implementation status:

- Supported scans emit `.project-memory/project-graph.json` as a separate machine-
  readable graph artifact with `graph_schema_version: "1.0"`.
- The foundation graph contains deterministic nodes and direct/structural `owns` and
  `declares` edges derived from existing structured facts, with derivation metadata for
  structural edges and existing `evidence_ids` on evidence-backed nodes.
- The current graph also carries conservative inferred repository/entity and
  tested-subject edges only when existing relation rows have concrete graph targets,
  while unsupported, ambiguous, not-detected, not-analyzed, uncertain, or no-target
  rows stay in `relation_statuses[]`.
- Incremental cache output fingerprints include `project-graph.json` as
  `output_kind: "project_graph"`.
- Document reconciliation graph material remains low-confidence uncertain inspection
  metadata; rows without both document and source fact nodes remain status records, not
  edges.

Contract decision:

- Emit a separate `.project-memory/project-graph.json` artifact. The graph is not a
  top-level `project-map.json` section.
- Keep `project-map.json` on `schema_version: "1.0"` for the initial graph expansion.
  The graph artifact has its own `graph_schema_version: "1.0"` marker.
- Generate the graph only from already extracted deterministic facts, existing
  relation/status rows, existing evidence IDs, and existing document reconciliation
  hints. Do not add new analyzer families merely to populate graph edges.
- Keep evidence records in `evidence-index.jsonl`. Graph nodes and edges reference
  existing `evidence_ids`; evidence records are not duplicated as graph nodes in the
  initial contract.
- Use explicit derivation metadata for structural edges that are derived from current
  project-map fields rather than directly supported by a dedicated evidence record.
- Keep extracted facts, inferred relations, uncertain signals, document-backed hints,
  spec-backed declared operations, generated-source metadata-only observations,
  warnings, and not-analyzed areas distinct in graph node and edge metadata.
- Treat graph output as an additive navigation/index artifact over deterministic facts,
  not as a stronger authority than `project-map.json` and `evidence-index.jsonl`.

Full v1.5 graph scope:

- Node families: modules, packages derived from emitted source-visible types,
  source-visible types already represented by existing facts, Spring MVC endpoints,
  declared OpenAPI operations, JPA entities and embeddables, Spring repository signals,
  emitted tests, local Markdown documents/headings/chunks, generated-source metadata
  rows, warnings, and selected status/not-analyzed markers when they can be represented
  without implying a missing target relation.
- Edge families: structural ownership edges, declaration edges, existing
  repository/entity and tested-subject relation edges, document reconciliation
  uncertain-reference edges, and fact-to-evidence references through `evidence_ids`
  rather than evidence-node duplication.
- Status-only relation rows that lack a concrete graph target remain status records,
  not inferred graph edges.

Non-goals:

- No full call graph, runtime dependency graph, runtime Spring bean/autowiring graph,
  runtime handler mapping, complete type solving, source/spec agreement scoring,
  documentation freshness scoring, coverage/CI/assertion proof, behavior-impact
  guarantee, query/impact command, generated-source content scanning, connectors,
  optional AI, SaaS, web UI, repository chat, generic RAG, or automatic code
  modification.

Release readiness notes:

- Public release notes are available in
  [V1_5_RELEASE_NOTES.md](V1_5_RELEASE_NOTES.md).
- Focused tests and goldens cover graph schema, deterministic IDs, sorting, node/edge
  taxonomy, cap behavior, evidence ID resolution, derivation metadata, existing output
  stability, and incremental cache interaction.
- Representative packaged CLI validation covered graph size/noise, duplicate IDs,
  dangling edges, unresolved evidence references, deterministic repeated digests, and
  sensitive-data boundaries.
- Final local release-prep validation passed with `mvn test`, `mvn package`, packaged
  CLI graph/profile/incremental smoke, checksum dry-run, whitespace checks, public
  marker audit, and risk-based release-prep review for the final release-prep diff.
- No release-blocking security finding or bounded security-fix goal remains open for
  `v1.5.0`.
- The `v1.5.0` tag and GitHub release are published with the packaged jar and checksum
  assets.

### v1.6.0: Local Query And Read-Only Explorer (Published)

The `v1.6.0` tag and GitHub Release are published with the packaged jar and checksum
assets. This section describes the shipped v1.6 query implementation.

Product outcome: add deterministic read-only CLI lookup commands over existing
`.project-memory/` artifacts so humans and coding agents can inspect generated facts,
evidence, and graph relations without running a chat, RAG, source-scan, or write flow.

Published command boundary:

- Add a top-level `query` command. The executable-jar form is
  `java -jar agent-project-memory-1.6.0.jar query <path> ...`; the installed-command
  form remains future distribution work until a release note documents it.
- Require exactly one local directory path after `query`. If the path names a
  `.project-memory` directory, query commands read artifacts from that directory. For
  any other directory, query commands read artifacts from its direct `.project-memory/`
  child.
- Read only `project-map.json` and `evidence-index.jsonl` for the base query layer,
  plus `project-graph.json` for relation lookup and optional graph-ID lookup when a
  valid graph artifact exists. Generated Markdown, cache metadata, profile artifacts,
  diagnostics, graph derivation metadata, and query output are not evidence.
- Support list commands for modules, source-visible Spring MVC endpoints, spec-backed
  declared API operations, JPA entities/embeddables, and emitted tests. Source-visible
  endpoints and spec-backed operations stay separate.
- Support `explain evidence <id>` by exact evidence ID from `evidence-index.jsonl`.
- Support exact `find fact <term>` and `find symbol <term>` lookup over generated
  artifact fields. v1.6 lookup is case-sensitive and does not include substring,
  fuzzy, regex, semantic, natural-language, or embedding search.
- Support one-hop graph relation lookup for a generated fact ID or graph node ID when a
  valid `project-graph.json` artifact exists. Relation lookup must preserve graph
  edges, relation-status rows, claim categories, support type, confidence, uncertainty,
  evidence IDs, and non-evidence derivation metadata without turning the graph into
  impact analysis.
- Use deterministic human text output. Stable JSON query output and a `--format` option
  are not included in v1.6.0 and remain future work. Error output
  remains bounded and deterministic on stderr.

Release non-goals:

- No source scanning, scan refresh, `.project-memory/` creation, cache refresh, profile
  generation, repository writes, or code modification during query.
- No chat interface, natural-language query, embeddings, vector store, generic RAG,
  LLM calls in the core analyzer or query layer, connectors, network/auth, telemetry,
  SaaS, web UI, editor integration, or agent server surface.
- No full call graph, runtime dependency graph, runtime Spring graph, runtime handler
  mapping, source/spec agreement scoring, documentation freshness scoring, coverage,
  CI status, assertion proof, vulnerability, correctness, production-impact, or
  business-priority claim.

Release readiness notes:

- Public release notes are available in
  [V1_6_RELEASE_NOTES.md](V1_6_RELEASE_NOTES.md).
- Focused tests cover query path handling, missing and malformed artifacts, optional
  graph behavior, list commands, evidence explanation, exact fact and symbol lookup,
  graph relation lookup, no-result behavior, stdout/stderr separation, and no-write
  behavior.
- Representative packaged CLI validation covered query behavior over Maven, Gradle,
  local-document, generated-source metadata, tests/quality, and graph-rich artifact
  sets, plus deterministic repeated query output checks and sensitive-output
  boundaries.
- Final local release-prep validation passed with `mvn test`, `mvn package`, packaged
  CLI scan/query smoke, checksum dry-run, whitespace checks, public marker audit, and
  risk-based release-prep review for the final release-prep diff.
- No release-blocking security finding or bounded security-fix goal remains open for
  `v1.6.0`.
- The `v1.6.0` tag and GitHub Release are published with the packaged jar and checksum
  assets.

### v1.7.0: Security And Secrets Safety Maturity (Published)

Product outcome: harden the accumulated local scan, generated-output, and read-only
query surfaces against accidental sensitive-data exposure and path-safety regressions
while preserving deterministic evidence-backed project memory.

Release-candidate boundary:

- Defines deterministic redaction for obvious secret-looking values that may otherwise
  appear in selected generated excerpts, generated Markdown, selected agent profile
  Markdown, graph labels or attributes, cache/scan diagnostics, CLI stdout/stderr, or
  query-rendered text.
- Uses `[REDACTED_SECRET_LIKE_VALUE]` as the plain-text redaction marker.
- Treats redaction as output hardening, not as complete secret detection, a secret
  inventory, vulnerability scanning, or security correctness proof.
- Applies redaction at generation time for newly produced artifacts and at query render
  time for existing artifacts. Query render-time redaction must not rewrite or repair
  artifact files.
- Preserves evidence usefulness after redaction: evidence IDs, normalized
  repository-relative paths, symbols, line ranges, confidence, uncertainty, relation
  statuses, and claim categories should remain navigable.
- Keeps redaction markers inside existing excerpt or rendered-output strings. The
  initial v1.7 design does not add evidence fields, evidence types, a new
  `project-map.json` schema marker, or a new `graph_schema_version` by itself.
- Audits path and symlink behavior across scan roots, output writes, root-local config,
  local Markdown discovery, generated-source metadata, cache metadata, graph output,
  agent profile output, query artifact reads, CLI stdout, and CLI stderr.
- Keeps public security documentation in the root `SECURITY.md` and the public threat
  model in [../development/THREAT_MODEL.md](../development/THREAT_MODEL.md).

Implemented scope:

- The shared bounded redaction/excerpt primitive is applied to
  evidence excerpts and selected generated-output serialization and rendering points
  that can carry selected source or artifact text.
- Fake-only security regression fixtures and output checks cover generated artifacts,
  generated Markdown, profile output, graph output, cache metadata, query output, and
  terminal output.
- The path/symlink audit over existing surface behavior is complete, with bounded fixes
  for JSON-style credential strings, symlinked-root CLI error path sanitization, and
  legacy graph source-reference render-time redaction.
- Release-track validation and release-prep review are complete with no
  release-blocking findings remaining.

Release readiness notes:

- Public release notes are available in
  [V1_7_RELEASE_NOTES.md](V1_7_RELEASE_NOTES.md).
- Focused tests cover redaction primitives, evidence excerpt handling, selected
  generated JSON and Markdown surfaces, profile output, graph output, cache metadata,
  query render-time redaction, bounded CLI error text, and fake-sensitive surface
  regressions.
- Final local release-prep validation passed with `mvn test`, `mvn package`, packaged
  CLI scan/query smoke over representative and fake-sensitive fixtures, checksum
  dry-run, whitespace checks, public marker audit, and risk-based release-prep review.
- No release-blocking finding remains open for `v1.7.0`.
- The `v1.7.0` tag and GitHub Release are published with the packaged jar and checksum
  assets.

Non-goals:

- No complete secret detector, secret inventory, credential classifier, vulnerability
  scanner, or security correctness claim.
- No external secret scanners, network access, telemetry, source upload, connector
  credentials, repository chat, generic RAG, embeddings, LLM calls in the core analyzer
  or query layer, SaaS, web UI, editor integration, server surface, plugin platform, or
  automatic code modification.
- No generated-source content scanning, default symlink following, environment-variable
  interpolation, config value extraction, Maven or Gradle execution, dependency
  resolution, runtime Spring reconstruction, stable JSON query output, release
  automation, package-manager publication, signing, native images, or container images.
- No real secrets in fixtures, tests, docs, examples, release materials, or public
  vulnerability reports.

### v1.8.0: Public Adoption Polish (Published)

Product outcome: reduce first-run and first-contribution friction for public users
without changing the deterministic analyzer, generated output schemas, evidence
semantics, installation channel, release process, or release authority.

Release-candidate boundary:

- Adds a small checked-in generated-output example snapshot for readers who want to
  inspect the base `.project-memory/` output set before running the CLI.
- Keeps public examples as documentation aids only. Examples are not evidence sources,
  not a second output schema contract, and not a stable Markdown parser contract.
- Uses the existing fake `stage3-project-map` fixture/golden pair as the deterministic
  source for the public example snapshot.
- Improves contributor path guidance for docs-only, examples/templates, fixtures/tests,
  analyzer behavior, output/evidence contract, and release-prep changes.
- Refines issue and PR templates so reports and reviews capture version, command,
  generated-file, evidence-ID, contract-impact, and sensitive-data context.
- Preserves the executable jar plus optional `SHA256SUMS` as the supported public
  release path. Installed commands and package-manager channels remain future work until
  a release note explicitly documents them.

Implemented scope:

- Added `examples/stage3-project-map/README.md` and committed the base generated output
  snapshot under `examples/stage3-project-map/.project-memory/`.
- Linked the example from the README public documentation map and documented the
  packaged-jar regenerate-and-compare workflow.
- Expanded `CONTRIBUTING.md` with a bounded contribution-path guide and good-first
  scope guidance.
- Updated the bug report, feature request, and PR templates in place without adding
  remote GitHub issue, label, milestone, project-board, CI, script, dependency, or
  publication workflow changes.

Release readiness notes:

- Public release notes are available in
  [V1_8_RELEASE_NOTES.md](V1_8_RELEASE_NOTES.md).
- Example snapshot verification compares the committed base example files with packaged
  CLI output regenerated from the deterministic `stage3-project-map` fixture.
- Final local release-prep validation passed with `mvn test`, `mvn package`, packaged
  CLI scan/query/help/version smoke, example snapshot comparison, whitespace checks, and
  public marker audit.
- No analyzer, CLI, generated-output, evidence, release-process, CI, dependency,
  package-distribution, or security-policy change is included in v1.8.0.
- The `v1.8.0` tag and GitHub Release are published with the packaged jar and checksum
  assets.

Non-goals:

- No analyzer feature expansion, output field addition/removal/rename, schema marker
  change, evidence field/type/semantic change, stable JSON query output, new CLI command,
  new CLI flag, or generated-source content scanning.
- No package-manager publication, installed-command distribution, signing, native image,
  container image, release automation, or additional distribution channel.
- No connectors, adapter APIs, plugin platform, MCP/server surface, source upload,
  external service calls, telemetry, SaaS, web UI, repository chat, generic RAG,
  embeddings, LLM calls in the core analyzer or query layer, or automatic code
  modification.
- No real secrets, credentials, private repository data, local machine paths, raw
  command transcripts, or maintainer-only workflow details in public examples,
  templates, docs, release notes, or release metadata.

Possible later tracks:

- v2 architecture preparation.

## v1.9.0: v2 Architecture Preparation (Published)

Expected outcome:

- Public architecture docs describe the planned v2 adapter boundary before v2
  implementation starts.
- Adapter responsibilities and forbidden responsibilities are explicit.
- Source/document normalization and connector provenance are defined as design
  boundaries, not shipped behavior.
- Optional AI presentation is defined as a non-authoritative boundary over deterministic
  memory, not as a source of facts or evidence.
- The planned v2 threat model covers external data, credentials, network/auth defaults,
  plugin/API surfaces, and provenance boundaries before implementation starts.
- Network access, source upload, connector credentials, plugin loading, and AI providers
  remain absent from the current core and disabled by default in future designs.
- Draft output and evidence migration questions are recorded without changing current
  `schema_version: "1.0"` behavior.

Design scope:

- Optional adapter layer around the deterministic core.
- Normalized source document model and connector provenance model.
- Local export import as the first safe adapter implementation candidate for a later
  v2 release.
- Future evidence/provenance requirements for adapter-backed records.
- Optional AI presentation inputs, forbidden outputs, non-evidence labeling, provider
  privacy defaults, and adapter-provenance interaction.
- Planned threat-model controls for stale or malicious exported records, prompt/content
  injection, credential handling, network enablement, plugin/API permissions, and future
  implementation or release-level security review gates.
- v1-to-v2 compatibility questions for generated-output consumers.

Non-goals:

- No production adapter API, connector implementation, network/auth behavior, plugin
  loading, AI provider code, source upload, embeddings/vector/RAG implementation, new
  CLI command or flag, generated artifact schema change, evidence field/type change,
  release publication, package publication, SaaS, web UI, repository chat as the core
  product, generic RAG, or automatic code modification.

## v2.0.0: Extensible Ingestion And Adapter Platform (Published)

Expected outcome:

- Introduce the first production adapter-domain boundary while keeping the
  deterministic Java/Spring analyzer independent from adapters.
- Preserve no-adapter scan compatibility with the current base artifact set and
  `project-map.json` `schema_version: "1.0"`.
- Add a disabled-by-default local structured import adapter for explicitly configured
  repository-relative export files.
- Emit `.project-memory/source-registry.json` and `project-map.json`
  `schema_version: "2.0"` adapter context only when that local import adapter is
  explicitly enabled and accepted.
- Keep adapter-backed records provenance-backed external/document context, not
  Java/Spring source facts and not evidence records.
- Keep current query commands focused on no-adapter v1 artifact sets.

Shipped scope:

- Adapter contract foundation with deterministic source-document IDs and required
  provenance validation.
- Safe adapter config and path validation with adapters disabled by default and network
  access disabled.
- Local structured import parsing, bounded diagnostics, source registry output, and
  adapter context output.
- v1-to-v2 migration and downstream consumer compatibility documentation.
- Release-level security review and targeted follow-up hardening completed before
  release prep.

Non-goals:

- No networked GitHub, GitLab, Jira, YouTrack, Confluence, or other API connectors.
- No connector credentials, credential lookup/storage/validation, source upload,
  telemetry, plugin loading, MCP/server surfaces, AI provider behavior, embeddings,
  generic RAG, repository chat, SaaS, web UI, adapter-aware query support, package
  publication, installed-command distribution, signing, native images, container
  images, release automation, or automatic code modification.

## v2.1.0: GitHub/GitLab Local Export Imports (Published)

Expected outcome:

- Add disabled-by-default local JSON export import for GitHub and GitLab issue,
  pull-request, and merge-request records.
- Use one normalized local export format for Git hosting records rather than raw
  GitHub or GitLab API responses.
- Preserve no-adapter scan and query compatibility with the v1 artifact set.
- Emit Git hosting records only as provenance-backed external/document context through
  the existing v2 source registry and `project-map.json` adapter context strategy.
- Keep Git hosting records out of `evidence-index.jsonl` and out of Java/Spring code
  facts.
- Keep network/API mode, credentials, background sync, remote cache, source upload,
  adapter-aware query support, and AI behavior out of the local import baseline.

Published scope:

- Supported local source types are `github_issue`, `github_pull_request`,
  `gitlab_issue`, and `gitlab_merge_request`.
- Source identity is a stable logical key derived from provider, host, namespace, record
  type, and issue/PR/MR number or IID. It is not a local path, raw URL, timestamp,
  title, author, branch name, or content hash.
- Generated output may serialize bounded redacted display titles, content hashes,
  provenance join keys, sanitized source URLs when safe, snapshot timestamps, and
  provider metadata. It must not serialize raw issue/PR/MR bodies, comments, review
  notes, raw provider export objects, raw request/response logs, configured import
  paths, local absolute paths, credentials, tokens, cookies, authorization headers, or
  raw config values by default.
- GitHub/GitLab text remains stale-prone external context. It is not proof that the
  remote service is current, complete, authoritative, reachable, or aligned with
  repository source.

Non-goals:

- No live GitHub/GitLab API fetching in the local import baseline.
- No connector credentials, credential lookup, OAuth, PAT, GitHub App, GitLab token,
  credential storage, credential validation, retry/backoff, rate-limit handling,
  background sync, or remote cache behavior.
- No adapter-backed query commands unless a later release explicitly documents
  adapter-aware query behavior.
- No external issue, pull-request, merge-request, comment, review, label, milestone,
  branch, pipeline, or discussion text as Java/Spring source truth, evidence, security
  findings, source/spec agreement claims, documentation freshness claims, or automatic
  code-modification input.

## v2.2.0: Jira/YouTrack/Confluence Local Export Scope (Published)

Published scope:

- Add disabled-by-default local JSON export import for selected Jira issue, YouTrack
  issue/article, and Confluence page records.
- Use one normalized local export format for connector records rather than raw Jira,
  YouTrack, or Confluence API responses.
- Preserve no-adapter scan and query compatibility with the v1 artifact set.
- Emit connector records only as provenance-backed external/document context through
  the existing v2 source registry and `project-map.json` adapter context strategy.
- Keep connector records out of `evidence-index.jsonl` and out of Java/Spring code
  facts.
- Keep live API mode, credentials, provider discovery, background sync, remote cache,
  source upload, adapter-aware query support, and AI behavior out of the local import
  baseline.

Implemented local-export scope:

- Supported local source types are `jira_issue`, `youtrack_issue`,
  `youtrack_article`, and `confluence_page`.
- Source identity is a stable logical key derived from provider, host, source type,
  project or space container, and stable issue/article/page key or ID. It is not a
  local path, raw URL, timestamp, title, author, workflow state, label, attachment name,
  page title, or content hash.
- Generated output may serialize bounded redacted display titles, content hashes,
  provenance join keys, sanitized source URLs when safe, snapshot timestamps, and
  provider metadata. It must not serialize raw issue/page/article bodies, descriptions,
  comments, rendered HTML, rich text, attachment names, attachment bodies, raw provider
  export objects, raw request/response logs, configured import paths, local absolute
  paths, credentials, tokens, cookies, authorization headers, or raw config values by
  default.
- Jira/YouTrack/Confluence text remains stale-prone external context. It is not proof
  that the remote service is current, complete, authoritative, reachable, or aligned
  with repository source.

Non-goals:

- No live Jira, YouTrack, or Confluence API fetching in the local import baseline.
- No connector credentials, credential lookup, OAuth, PAT, app password, API key,
  cookie, credential storage, credential validation, retry/backoff, rate-limit handling,
  pagination, background sync, remote cache, or provider discovery behavior.
- No adapter-backed query commands unless a later release explicitly documents
  adapter-aware query behavior.
- No external issue, article, page, comment, attachment, label, workflow, author, space,
  project, sprint, version, or link data as Java/Spring source truth, evidence, security
  findings, source/spec agreement claims, documentation freshness claims, runtime claims,
  or automatic code-modification input.

## v2.3.0: Optional AI Presentation Layer (Published)

Release outcome:

- Add an explicitly enabled AI-assisted presentation layer over existing deterministic
  generated memory without allowing AI to create authoritative facts.
- Keep the deterministic core analyzer, no-adapter scan path, adapter normalization,
  graph generation, evidence index generation, cache behavior, profile generation, and
  current query behavior independent from AI.
- Use a separate optional `.project-memory/ai-presentations/` artifact surface for the
  first implementation, rather than extending selected agent profiles, deterministic
  query output, `project-map.json`, `source-registry.json`, or `evidence-index.jsonl`.
- Label AI output as non-authoritative and non-evidence in both visible wording and
  machine-readable metadata.

First surface:

- No AI presentation artifacts are emitted by default.
- The first implementation uses only a mock/no-network provider mode through
  `scan --ai-presentation mock_no_network`.
- The artifact directory is:

```text
.project-memory/ai-presentations/
  manifest.json
  brief.md
```

The AI presentation manifest is generated-output metadata only. It may identify source
artifacts, provider mode, authority, evidence policy, network/source-upload status, and
generated presentation files. It is not project evidence, not a source registry, not a
project-map section, and not proof that the AI presentation is correct.

Allowed input boundary:

- Generated structured facts from documented artifacts.
- Existing evidence IDs and already serialized bounded evidence excerpts.
- Graph navigation and derivation metadata labeled as non-evidence.
- Bounded source-artifact, profile, cache, and status metadata labeled as non-evidence.
- Adapter source-document and provenance IDs only after deterministic adapter contracts
  have accepted those records.

Forbidden input and output boundary:

- No raw repository source files, generated-source contents, raw local document bodies,
  generated Markdown bodies, raw connector exports, raw adapter input files, raw
  connector request/response logs, remote API responses, credentials, tokens, cookies,
  authorization headers, local absolute paths, or raw prompt transcripts as AI
  presentation inputs.
- No AI output in `project-map.json`, `evidence-index.jsonl`,
  `source-registry.json`, `project-graph.json`, deterministic profile artifacts, cache
  metadata, source files, repository docs, root instruction files, or scan config.
- No AI-created project facts, evidence records, connector truth, security findings,
  vulnerability proof, runtime claims, source/spec agreement claims, coverage/CI/
  assertion claims, business-priority claims, documentation-freshness claims, release
  evidence, repository edits, or automatic code-modification input.

Provider and security direction:

- Provider mode is absent unless AI presentation is explicitly enabled.
- `mock_no_network` is the only first implementation mode.
- Real provider modes remain parked until a separate design defines explicit
  enablement, contacted service, request minimization, credential policy, network
  behavior, telemetry defaults, prompt transcript policy, retention/training-use wording
  limits, diagnostics, and release review requirements.
- Prompt and content-injection controls must treat repository text, local document text,
  evidence excerpts, adapter-backed records, connector text, generated Markdown, and
  user-provided labels as untrusted content rather than executable instructions.

Non-goals:

- No LLM calls in the core analyzer, adapter path, graph builder, evidence index
  builder, cache layer, profile generator, or current query layer.
- No real provider integration, provider SDK, network access, credentials, telemetry,
  source upload, prompt logging, embeddings, vector search, generic RAG, repository
  chat, SaaS, web UI, plugin loading, public API/server behavior, editor integration, or
  automatic code modification in the first implementation slice.

## v2.4.0: Agent Integrations, MCP, And Editor Consumption (Published)

Release outcome:

- Expose generated project memory to agents and editor-adjacent workflows through a
  read-only consumption surface.
- Preserve the deterministic core analyzer, evidence index, graph, adapter provenance,
  profile output, AI presentation boundary, and current no-write product model.
- Keep agent-facing output as navigation and presentation only, not project evidence,
  connector truth, security findings, runtime claims, release evidence, or code-change
  authority.

Implemented first slice:

- The first implementation path is a CLI-only `query`-based agent context surface over
  existing generated artifacts.
- The implemented surface reads the same bounded local artifact root policy as the
  current query layer: a repository directory with `.project-memory/` or the
  `.project-memory/` directory itself.
- The implemented surface uses existing no-adapter `project-map.json`
  `schema_version: "1.0"`, `evidence-index.jsonl`, and graph navigation metadata when
  available. Adapter-aware query over `source-registry.json` or connector records
  remains a separate later contract.
- The output is deterministic stdout intended for agent/editor
  consumption. It may contain reading order, artifact provenance, supported query
  commands, bounded fact orientation, and existing evidence IDs, but it must not create
  evidence records or open referenced source files to expand excerpts.

Security and product boundary:

- No repository source readback in the first slice.
- No repository writes, generated artifact mutation, scan config edits, root instruction
  file edits, commits, branches, tags, releases, pull requests, issues, external writes,
  or automatic code modification.
- No MCP server, local server, public API service, socket listener, daemon, background
  process, editor plugin, plugin runtime, network access, remote service call,
  credential lookup, credential storage, telemetry, source upload, raw prompt transcript
  serialization, repository chat, generic RAG, embeddings, vector store, semantic
  search, real AI provider call, or agent-driven provider call in the first slice.
- AI presentation artifacts, when present, may be mentioned only as
  non-authoritative, non-evidence presentation. They must not be consumed as facts,
  evidence, connector truth, security findings, runtime claims, documentation-freshness
  claims, or code-change authority.
- MCP, server, public API, editor plugin, network, auth, credential, telemetry,
  source-upload, and automatic code-modification behavior are deferred until a later
  explicit design and higher-risk release security review approve their transport,
  permissions, path scope, logging, and failure behavior.

Implementation readiness expectations:

- Focused tests for query grammar, artifact path handling, schema rejection, no-write
  behavior, deterministic output, and content-safety boundaries.
- Regression coverage proving the new surface does not create or mutate
  `.project-memory/`, cache metadata, profile artifacts, AI presentation artifacts,
  source files, repository docs, root instruction files, scan config, or external state.
- Public docs, output contract, evidence model, threat model, changelog, and release
  notes must stay synchronized when the implementation lands.

Release readiness notes:

- Public release notes are available in
  [V2_4_RELEASE_NOTES.md](V2_4_RELEASE_NOTES.md).
- Release-prep validation covers focused query tests, full local tests, packaged CLI
  scan and `agent-context` smoke, jar metadata inspection, checksum dry run, whitespace
  checks, and public marker audit.
- The `v2.4.0` tag and GitHub Release are published with the packaged jar and checksum
  assets.

## v2.5.0: Workspace, Monorepo, And Cross-Repo Memory (Published)

Product outcome: add local workspace-level project memory across multiple explicitly
configured local repository or service roots while preserving deterministic single-repo
analysis, explicit output contracts, evidence-backed facts, and no-default-network or
no-write authority.

Accepted design boundary:

- The first workspace invocation shape is an explicit CLI workflow:
  `agent-project-memory workspace scan <config>`. The packaged-jar invocation uses the
  same arguments after `java -jar agent-project-memory-X.Y.Z.jar`.
- The workspace config is an explicit local YAML file argument. The config file
  directory is the workspace root for the first implementation boundary. No default
  workspace config discovery, user-home config, environment-variable config, remote
  config, or parent-directory crawler is included.
- Workspace members are configured with workspace-relative root paths and unique logical
  `repo_id` values. The first boundary does not accept absolute member paths, `..`
  escapes, symlinked roots, multi-link or link-count-unverifiable roots, generated-output
  paths, duplicate `repo_id` values, or ambiguous nested roots.
- Workspace output is a separate workspace-root artifact:
  `.project-memory/workspace-map.json`. Existing single-repo `.project-memory/`
  artifacts remain the compatibility baseline and are not renamed or migrated by the
  workspace design.
- `workspace-map.json` uses a new workspace schema marker and may summarize member
  identity, member path, accepted per-repo artifact schema versions, diagnostics, and
  bounded sample composite evidence references. It must not serialize local absolute
  paths, raw source bodies, raw document bodies, command logs, credentials, tokens,
  local maintainer notes, or remote provider data.
- Single-repo `scan <path>`, query commands, no-adapter `project-map.json`
  `schema_version: "1.0"`, adapter-enabled `schema_version: "2.0"` sets, graph output,
  evidence index output, cache metadata, agent profiles, and AI presentation artifacts
  remain unchanged unless a later implementation goal explicitly documents otherwise.
- Workspace evidence references are composite references from workspace output to
  per-repo artifacts: `repo_id` plus an existing per-repo `evidence_id`. Normal
  `evidence-index.jsonl` records stay repository-relative and do not gain a `repo_id`
  field in the first workspace boundary.
- Cross-repo relation emission is parked for the first implementation slice. Any later
  cross-repo relation must be deterministic, conservative, and supported by evidence
  from every participating repo or remain absent/uncertain. Name similarity, package
  similarity, generated Markdown, query output, graph derivation, adapter records, AI
  output, prompts, issue text, maintainer notes, or chat output must not create
  workspace relation evidence.
- Workspace query, workspace `agent-context`, adapter-aware workspace context,
  workspace graph output, change-impact workflows, and child-repo scan refresh/mutation
  are not part of the first workspace slice unless a later bounded goal updates the
  public contracts, tests, and security review scope.

Release scope:

- The workspace config and root-safety foundation parses only the accepted config
  shape, validates root identity and path policy, emits bounded diagnostics, and proves
  no unintended child scans or child repository writes occur.
- Workspace map aggregation prefers existing per-repo artifacts and writes only the
  workspace-root `workspace-map.json`; running or refreshing child repo scans remains a
  separate explicit write-scope decision.
- v2.5 ships as workspace aggregation only; no cross-repo relation family was accepted
  before release prep.
- The `v2.5.0` tag and GitHub Release are published with the packaged jar and checksum
  assets.

Non-goals:

- No remote clone, fetch, pull, provider API scan, organization crawler, background
  sync, remote cache, SaaS index, source upload, network access, credentials,
  telemetry, MCP/server/API/editor/plugin runtime, repository chat, generic RAG,
  semantic search, embeddings, vector store, automatic code modification, speculative
  service dependency graph, runtime dependency graph, call graph, source/spec agreement
  scoring, documentation-freshness scoring, vulnerability finding, correctness claim,
  release automation, package-manager publication, signing, native images, or container
  images.

Validation expectations before release:

- Focused tests for workspace config grammar, root containment, missing roots, duplicate
  roots, duplicate `repo_id` values, nested roots, symlinked roots, multi-link roots,
  generated-output paths, diagnostics, and no-unintended-scan/no-unintended-write
  behavior.
- Workspace aggregation tests over multi-repo and monorepo fixtures, including
  workspace evidence-reference resolution through `repo_id` plus per-repo evidence IDs.
- Compatibility tests proving existing single-repo scan and query behavior remains
  unchanged unless explicitly documented.
- Public docs, output contract, evidence model, threat model, changelog, and release
  notes must agree before release.
- Risk-based security review is required for implementation changes that touch
  config parsing, multiple-root path handling, generated output, evidence reference
  rendering, workspace diagnostics, query behavior, or child-repo write behavior.

## v2.6.0: Change-Impact Workflows (Published)

Product outcome: provide conservative local change-impact hints from explicit changed
files while preserving deterministic artifacts, evidence-backed facts, local-only
operation, and no code-change authority.

Accepted first design boundary:

- The first implementation path is a read-only query-layer expansion:
  `agent-project-memory query <path> impact --files <changed-file> [...]`. The
  packaged-jar invocation uses the same arguments after
  `java -jar agent-project-memory-X.Y.Z.jar`.
- `<path>` uses the existing query artifact-root policy: a repository directory with
  `.project-memory/` or the `.project-memory/` directory itself. The command reads
  existing artifacts only and must not run scans, refresh artifacts, create generated
  outputs, or write repository files.
- Changed-file inputs are explicit repository-relative paths. The first boundary does
  not include local absolute changed-file paths, escaping paths, generated-output paths,
  glob expansion, raw diff text, standard-input diff parsing, or `--from-git-diff`.
- The first source artifact set is the no-adapter single-repo set:
  `project-map.json` with `schema_version: "1.0"`, `evidence-index.jsonl`, and
  `project-graph.json` with `graph_schema_version: "1.0"`. Adapter-enabled artifact
  sets, `source-registry.json`, `workspace-map.json`, generated Markdown, cache
  metadata, agent profiles, AI presentation artifacts, query output, and downstream
  agent output are not impact fact inputs.
- Direct matches come only from existing evidence paths, source references, generated
  fact IDs, or graph nodes already present in generated artifacts. If a changed file is
  not represented in generated memory, the output reports that status instead of
  inferring hidden impact.
- Graph navigation is one hop from direct matches only. It may surface existing graph
  edges and relation-status rows as orientation, but it must not perform transitive
  traversal, reachability analysis, call-graph traversal, dependency traversal, or
  runtime analysis.
- Output is deterministic text to stdout for the first slice. Stable JSON output,
  generated Markdown, and `.project-memory/impact-report.json` remain parked until a
  later explicit output contract accepts them.
- Impact rows must keep direct matches, graph neighbors, relation/status rows, quality
  planning hints, uncertain rows, not-represented inputs, and diagnostics visibly
  separate. Confidence labels describe support for the hint, not certainty of complete
  downstream impact.
- Workspace impact is parked for the first v2.6 boundary. The existing
  `workspace-map.json` artifact remains workspace aggregation metadata, not a query or
  impact input source.

Non-goals:

- No guaranteed complete impact analysis, runtime tracing, runtime service graph,
  runtime dependency graph, runtime Spring bean graph, call graph, data-flow graph,
  complete dependency graph, source/spec agreement scoring, documentation-freshness
  scoring, test coverage scoring, CI-status claim, assertion analysis, correctness
  claim, vulnerability finding, production-impact claim, business-priority claim, raw
  source readback, generated-source content scanning, network access, credentials,
  repository chat, generic RAG, semantic search, embeddings, vector store, generated
  impact artifact, workspace impact, cross-repo impact, adapter-aware impact, release
  automation, package publication, or automatic code modification.

Validation expectations before release:

- Focused query parser and changed-file path tests, including missing, duplicate,
  escaping, absolute, generated-output, malformed, and unrepresented path inputs.
- Artifact-loading tests for missing or invalid `project-map.json`,
  `evidence-index.jsonl`, and `project-graph.json`, including unsupported schema
  markers and unresolved graph/evidence references.
- Deterministic output tests for direct matches, one-hop graph neighbors,
  relation-status rows, quality planning hints, caps, diagnostics, no-result-like
  not-represented inputs, and repeated runs.
- No-write tests proving impact queries do not scan source, create or mutate
  `.project-memory/`, refresh cache metadata, write reports, edit source files, or
  change repository docs/configuration.
- Public docs, output contract, evidence model, threat model, changelog, and release
  notes must agree before release.
- Risk-based security review is required for implementation changes that touch query
  grammar, changed-file path handling, artifact reading, graph traversal, output
  rendering, diagnostics, evidence-reference rendering, or generated-output write
  boundaries.

Release scope:

- v2.6 ships the read-only single-repo `query <path> impact --files ...` boundary with
  direct mapping, one-hop graph projection, relation-status rows, low-confidence
  planning hints, explicit `not_represented` rows, bounded diagnostics, and deterministic
  text output only.
- The `v2.6.0` tag and GitHub Release are published with the packaged jar and checksum
  assets.

## v2.7.0: Policy And Configuration Profiles (Published)

Product outcome: add explicit local policy profiles that make scan configuration
presets and guardrails easier to select, review, and audit while preserving
deterministic local analysis, evidence-backed facts, no-default-network behavior, and
the existing agent output profile surface.

Release boundary:

- A policy profile is a local scan configuration preset and guardrail. It is not an
  agent output profile, security certification, compliance mode, vulnerability scanner,
  secret inventory, hosted policy, enterprise policy enforcement system, or complete
  safety proof.
- Existing `scan <path> --agent-profile <profile>` selectors keep their v1.3 meaning:
  deterministic generated presentations under `.project-memory/agent-profiles/`.
  Policy profiles use a separate selector and must not reuse the `codex`, `claude`,
  `cursor`, `generic`, or `all` agent-profile names.
- The selector shape is a single optional
  `scan <path> --policy-profile <name>` CLI flag plus a single optional root-local scan
  config key, `policy_profile: <name>`. Unknown names fail closed before output
  generation. Repeated CLI selectors are usage errors.
- The initial profile names are `guarded-local`, `docs-focused`, and
  `adapter-local`. `strict`, `no-network`, `enterprise-local`, `oss`, `docs-heavy`,
  and `generated-source-enabled` remain parked names because they are ambiguous,
  overstate guarantees, or imply behavior that is not safe in the first boundary.
- A normal scan with no policy profile remains the compatibility baseline and should
  keep the existing default output set and local-first behavior. Explicit selected
  profile metadata is additive, and no-profile scans do not emit default policy
  metadata.
- Effective policy calculation is built-in defaults first, then the selected policy
  profile, then explicit root-local config keys, then explicit CLI flags. Explicit
  config or CLI values may be stricter than the selected profile, but they must not
  weaken profile guardrails.
- If `policy_profile` in config and `--policy-profile` on the CLI are both present,
  they must name the same profile. A mismatch fails closed rather than letting a CLI
  invocation silently weaken a repository-local policy choice.
- Policy profiles never silently enable adapters, AI presentation, generated-source
  content scanning, symlink following, network access, credentials, telemetry,
  source upload, hosted policy management, server/API/editor/plugin runtime,
  repository chat, generic RAG, or automatic code modification.
- `guarded-local` rejects optional expanding surfaces such as adapters, AI
  presentation, generated-source content scanning, symlink following, and local
  document include/exclude expansion. It may still use the existing built-in
  default-scope local Markdown policy.
- `docs-focused` keeps the run local-only and no-network while allowing validated
  Markdown-only document include/exclude refinement under the existing path policy.
  It rejects adapters, AI presentation, generated-source content scanning, symlink
  following, credentials, and source upload.
- `adapter-local` allows explicitly configured existing local import adapters under the
  current repository-relative local-file validation rules. It does not enable any
  adapter by itself and still rejects network access, credentials, AI presentation,
  generated-source content scanning, symlink following, and source upload.
- `features.generated_sources: true` and `features.follow_symlinks: true` remain invalid
  config in v2.7. A generated-source content profile stays parked until a separate
  design defines path policy, traversal caps, fact labels, evidence semantics, tests,
  evaluation, and security review.
- Redaction posture must not be weakened by a profile. Profile metadata, diagnostics,
  and conflict messages must not serialize raw config values, user include/exclude
  patterns, adapter import paths, source bodies, document bodies, generated-source
  contents, local absolute paths, command logs, credentials, tokens, or secret-looking
  values.

Output and evidence stance:

- Selected policy metadata, when emitted, belongs under `project-map.json` `scan`
  metadata as redacted execution metadata. It should record the selected profile name,
  selector source, profile version, local/network/source-upload/credential posture,
  allowed optional surfaces, fail-closed conflict policy, and non-evidence authority.
- Policy metadata is an additive compatibility expansion and should not require a
  `project-map.json` schema marker migration by itself. Adapter-enabled scans still use
  the existing adapter schema-marker rules when adapter context is emitted.
- Policy profile selection does not create `evidence-index.jsonl` records, add evidence
  fields or evidence types, reuse `config_file` evidence for the tool config, or change
  the meaning of existing evidence IDs.
- Downstream consumers that do not understand policy metadata may ignore
  `scan.policy_profile` and continue using generated facts and evidence references.
  Consumers must not treat policy metadata as proof of compliance, complete safety, or
  security correctness.

Validation expectations before release:

- Focused CLI/config tests for accepted names, unsupported names, duplicate selectors,
  config selection, CLI selection, matching config-plus-CLI selection, and mismatched
  config-plus-CLI failure.
- Profile matrix tests for `guarded-local`, `docs-focused`, `adapter-local`, and
  no-profile baseline behavior.
- Unsafe-combination tests for adapters, AI presentation, reserved generated-source and
  symlink modes, local document expansion, redaction-sensitive diagnostics, and
  no-network/no-credential/no-source-upload defaults.
- Golden/output tests if selected profile metadata changes generated outputs, plus
  regression tests proving no-profile scans remain stable if that compatibility decision
  is retained.
- Evidence tests proving no policy profile creates evidence records, evidence fields,
  evidence types, or tool-config evidence.
- Public docs, output contract, evidence model, threat model, changelog, and release
  notes must agree before release.
- Risk-based security review is required for implementation changes that touch CLI or
  config parsing, precedence, filesystem/path handling, adapter allowance,
  generated-output rendering, evidence serialization, redaction, diagnostics, network or
  credential posture, or output write boundaries.

Stop conditions for implementation:

- A profile name or public wording implies security certification, compliance,
  enterprise enforcement, vulnerability scanning, secret inventory, production
  correctness, or complete safety.
- Defaults stop being local-first, no-network, no-source-upload, deterministic, and
  compatible for no-profile scans.
- Precedence, conflict behavior, metadata authority, or evidence semantics cannot be
  made explicit and fail-closed.
- Profile behavior would require hosted policy management, remote configuration,
  user-home policy discovery, organization crawling, background sync, telemetry, update
  checks, credentials, network calls, provider AI, plugin loading, server/API/editor
  runtime, repository chat, generic RAG, generated-source content scanning, symlink
  following, source upload, release automation, package publication, or automatic code
  modification.

Release scope:

- v2.7 ships explicit local policy profiles through
  `scan <path> --policy-profile <name>` and root-local `policy_profile: <name>`,
  with `guarded-local`, `docs-focused`, and `adapter-local` as the accepted profile
  names.
- v2.7 emits bounded `scan.policy_profile` execution metadata only when a policy
  profile is explicitly selected, preserves no-profile output compatibility, and keeps
  policy metadata outside `evidence-index.jsonl`.
- The `v2.7.0` tag and GitHub Release are published with the packaged jar and checksum
  assets.

## v2.8.0: Distribution And Supply-Chain Hardening (Published)

Product outcome: improve release artifact integrity, dependency workflow review, and
release approval clarity while preserving manual maintainer authority over every
publication action.

Release boundary:

- The supported public distribution baseline remains the executable jar attached to a
  GitHub Release plus `SHA256SUMS`.
- Checksum hardening is implemented as a local artifact-integrity dry-run helper. It
  validates candidate jar filename, CLI version output, manifest entrypoint, Maven
  artifact metadata, exact local dry-run release asset list, and filename-only
  `SHA256SUMS` contents, but it must not publish, upload, sign, create releases, move
  tags, or require secrets.
- The current dependency workflow baseline is Dependabot coverage for Maven and GitHub
  Actions updates plus human review. Dependency/security workflow automation changes are
  release-sensitive and require risk-based review before release.
- CI release validation remains parked for this release. If implemented in a
  later release, it must run with read-only repository permissions and no secrets. It
  must not attach assets, upload artifacts, create or move tags, publish releases, sign
  files, deploy packages, or mutate remote state.
- Signing remains parked. A later design must define signing model, key custody,
  verification instructions, release asset status, maintainer approval, and security
  review before any signing implementation or public signing claim.
- SBOM generation and publication remain parked. A later design must define the SBOM
  tool, dependency-resolution boundary, generated-file status, release asset status,
  validation, wording limits, and review gate before any SBOM implementation or public
  asset claim.
- Package-manager channels, a first-party installed command, JBang catalogs, Homebrew
  taps, Maven Central publication, SDKMAN/asdf plugins, native images, and container
  images remain unavailable until a specific channel is implemented, tested, documented,
  and published in release notes.
- Release automation remains parked. Release preparation may produce checklists,
  validation output, and reviewed release materials, but publication remains a separate
  explicit maintainer action.
- Release credentials, signing keys, package-registry tokens, account details,
  passphrases, secret names, and private publication state must not be stored in
  repository files, generated artifacts, fixtures, scripts, public docs, release notes,
  or logs intended for public review.

Output and evidence stance:

- v2.8 distribution hardening is expected to have no `.project-memory/` output contract
  impact and no evidence model impact.
- Release notes, GitHub Release bodies, checksums, signatures, SBOMs, CI logs,
  dependency update reports, security review summaries, package metadata, and LLM output
  remain non-evidence for generated Java/Spring project facts.
- Any future proposal to emit a generated release metadata artifact, treat SBOMs or
  signatures as project evidence, or add generated output fields must update
  `OUTPUT_CONTRACT.md`, `EVIDENCE_MODEL.md`, tests or goldens where applicable, the
  changelog, release notes, and security review scope before implementation.

Validation completed for release:

- Public docs, threat model, release process, changelog, README install wording, and
  release notes must agree on the supported distribution channel and parked surfaces.
- If artifact-integrity validation is implemented, focused validation should prove
  filename-only checksum entries, jar/version/manifest metadata checks, no local
  absolute paths, no credential use, and no remote state mutation.
- CI release workflow changes are not included in this release.
- Release prep ran full tests, packaging checks, packaged CLI smoke, local checksum
  dry-run, public-surface review, and downloaded asset verification after explicit
  publication approval.
- Risk-based security review is required for implementation changes that touch release
  artifacts, checksums, dependencies, GitHub Actions workflows, security configuration,
  signing, SBOMs, package channels, credentials, scripts, or release automation.

Stop conditions for implementation:

- A proposed helper, workflow, or documented path can publish, upload, sign, create or
  move tags, create releases, deploy packages, or mutate remote state without a separate
  explicit maintainer approval.
- Ordinary validation requires credentials, signing keys, tokens, package-registry
  accounts, private endpoints, or account-specific publication state.
- Public docs present a package channel, signature, SBOM, native image, container image,
  or installed command as available before that channel or asset is implemented, tested,
  documented, and released.
- Checksums, logs, docs, release notes, or generated output would include local absolute
  paths, temporary paths, raw command transcripts, credentials, tokens, secret names, or
  private publication state.
- Distribution work would change output or evidence semantics without synchronized
  architecture docs, tests, changelog, release notes, and security review.

Release scope:

- v2.8 ships a local artifact-integrity dry-run helper and public release-process
  hardening while remaining a distribution/process hardening release, not an analyzer
  capability expansion.
- The `v2.8.0` tag and GitHub Release are published with the packaged jar and checksum
  assets.

## v2.9.0: v3 Preparation (Release Candidate)

Product outcome: freeze the v3.0 product, contract, evidence/provenance, migration, and
security-review boundaries before v3 implementation starts.

Release-candidate boundary:

- v2.9 is a planning, design, review, and release-prep track. It must not implement v3
  output schemas, serializers, parsers, readers, migration code, server/API/MCP
  runtimes, editor or plugin runtimes, provider AI, network connector behavior, release
  automation, or distribution channels.
- Public v2.9 docs may describe planned v3.0 behavior only as future/draft scope. They
  must not present v3 schemas, APIs, migration behavior, security gates, or platform
  capabilities as current shipped behavior.
- The accepted v3.0 scope must name the included platform boundary, excluded surfaces,
  implementation prerequisites, and breaking-change categories before v3 code changes
  start.
- v2.9 may prepare a v3 schema/API migration plan, evidence/provenance review,
  security-review plan, deprecation notes, compatibility-test plan, and release-readiness
  path. Those plans remain prerequisites for v3.0; they do not change current generated
  artifacts by themselves.
- The current published baseline remains `v2.8.0` until `v2.9.0` is published.
  The `v2.9.0` release candidate remains a planning/design release and does not ship
  v3 implementation behavior.

Design constraints:

- No SaaS-first positioning, default source upload, LLM calls in the core analyzer, LLM
  output as evidence, repository chat as the core product, generic RAG as the core
  product, or automatic code modification as product authority.
- No network connector defaults, credentials, background sync, remote cache, provider AI,
  server/API/MCP/editor/plugin runtime, plugin code loading, signing, SBOM publication,
  package-manager channels, native images, container images, or release automation unless
  a later release explicitly designs, implements, tests, documents, and reviews that
  surface.

## v2.x: Extensible Platform, Adapters, And Optional AI

Expected direction:

- Stable input adapter API.
- Normalized source document model.
- Connector provenance model.
- Local import format before network-backed connector modes.
- GitHub/GitLab/Jira/YouTrack/Confluence adapters when explicitly enabled.
- Optional AI presentation layer that reads deterministic memory and never creates
  authoritative evidence.
- Read-only agent integrations such as MCP or agent prompt bundles.
- Workspace and change-impact workflows.
- Local policy/configuration profiles that make safe scan presets easier to select and
  review without becoming security certifications or hosted policy management.
- Distribution and supply-chain hardening that improves release artifact validation
  while preserving explicit maintainer approval for publication, signing, package
  channels, and release automation.

The core analyzer must continue to run without adapters, network access, credentials,
plugin loading, source upload, or AI. Adapter-backed records must remain backed by
provenance and distinct from code-backed facts. Local export files, remote API
responses, plugin manifests, API requests, API responses, and AI prompt inputs must be
treated as untrusted content that cannot bypass provenance, evidence, redaction,
credential, or network defaults.

The initial v2.0 adapter boundary keeps no-adapter scans compatible with the current
v1.x generated artifact set and `project-map.json` `schema_version: "1.0"`.
Adapter-enabled output uses a separate source registry for normalized source documents
and provenance, and uses `project-map.json` `schema_version: "2.0"` for adapter-backed
context sections. Adapter provenance is navigation and review metadata, not code
evidence and not proof that an external service is current or authoritative.

Migration and release-prep notes for the initial v2 adapter boundary:

- No-adapter scans are the compatibility baseline for existing v1 consumers. They do
  not emit `source-registry.json` and keep the current base artifact set.
- Adapter-enabled scans should be treated as v2 artifact sets. Regenerate
  `project-map.json`, `project-graph.json`, `evidence-index.jsonl`,
  `source-registry.json`, and generated Markdown together when adapter input changes,
  and do not mix artifacts from different scans.
- Downstream consumers that do not implement the v2 adapter contract should continue
  using no-adapter `schema_version: "1.0"` outputs or explicitly reject v2 adapter
  sections. The current query layer remains focused on no-adapter v1 artifacts.

The first implementation path is landing in conservative order: adapter contract
foundation, disabled-by-default configuration and local path safety, then a local
structured import reference adapter and output integration. Networked connector modes,
credential handling, provider-backed AI, plugin loading, and public API/server surfaces
remain later work requiring separate design, tests, and security review.

Any optional AI layer must be explicitly enabled, must treat deterministic memory and
adapter provenance as inputs rather than authority it can rewrite, and must label its
output as non-evidence. The first AI presentation surface is a separate optional
`.project-memory/ai-presentations/` artifact set, currently enabled only through
`scan --ai-presentation mock_no_network`, so existing project-map, evidence,
source-registry, profile, cache, and query consumers can ignore it. AI presentation must
not create `project-map.json` facts, `evidence-index.jsonl` records, connector truth,
security findings, vulnerability proof, runtime claims, source/spec agreement claims,
documentation-freshness claims, release evidence, repository-file edits, or code
changes. Provider use, credentials, network access, telemetry, prompt transcript
serialization, retention/privacy claims, and source upload remain off by default unless
a later release explicitly designs and implements them.

## v3.0.0: Agent-Native Project Memory Platform

Frozen product goal:

v3.0 is planned as a stable local-first, evidence-backed project memory platform for
developers and AI coding agents. Its center is the deterministic Java/Spring analyzer
and generated local artifact contract. Optional layers may improve ingestion,
presentation, workspace orientation, impact inspection, and agent consumption, but they
must remain explicit, local-first, non-authoritative, and separable from the core
analyzer.

Included v3.0 scope:

- Stable v3 generated artifact contract and compatibility policy for the mature platform
  line, with explicit migration guidance from the existing no-adapter v1-compatible
  artifact set and v2 adapter-enabled artifact set.
- Stable evidence/provenance categories that preserve source-backed evidence as the
  authority for project facts while keeping adapter provenance, AI output, query output,
  graph derivation, cache metadata, profile output, release metadata, and generated
  Markdown as non-evidence unless a later contract explicitly changes that boundary.
- Mature deterministic Java/Spring analyzer reference behavior, including current
  module/build/config/API/application-surface/domain/test/document/graph/query/profile,
  workspace, impact, policy-profile, adapter-import, AI-presentation, and distribution
  boundaries that have already been accepted in v1.x and v2.x.
- Stable local adapter and extension contract boundaries for explicitly configured local
  inputs and provenance-backed context. v3.0 may stabilize adapter-facing interfaces and
  manifest/schema expectations, but plugin code loading or runtime extension execution
  is excluded from the frozen v3.0 scope unless a later release separately approves it.
- Read-only agent workflows over generated artifacts, staying local and artifact-backed.
  CLI query and deterministic agent-context output are in scope; server, socket, daemon,
  editor plugin, MCP runtime, or public API service behavior is not included in the
  frozen v3.0 scope.
- Workspace and change-impact maturity for existing local artifact-backed workflows,
  including clearer compatibility and regeneration guidance. Remote discovery,
  organization crawling, adapter-aware workspace inference, generated impact reports,
  and automatic code modification remain separate later work.
- Security, migration, evaluation, documentation, and release-readiness gates strong
  enough to justify a major version. v3 implementation must not start until the v2.9
  preparation plans are reviewed and accepted.

Excluded or deferred from v3.0:

- Mandatory SaaS, hosted project-memory storage, default source upload, network access by
  default, telemetry by default, or source-upload claims.
- LLM calls in the core analyzer, LLM output as evidence, AI-created project facts,
  connector truth, vulnerability proof, runtime claims, or code-change authority.
- Provider-backed AI as required behavior. Optional AI remains presentation-only unless a
  later design separately accepts real provider behavior, request minimization,
  credential handling, prompt/content-injection controls, diagnostics, retention wording,
  and review requirements.
- Live network connectors, connector credentials, credential lookup/storage, background
  sync, remote cache, provider discovery, pagination/retry/rate-limit behavior, and
  remote freshness claims.
- Repository chat as the core product, generic RAG as the core product, embeddings,
  vector stores, semantic search as authority, and automatic repository modification.
- Server/API/MCP/editor/plugin runtime, plugin code loading, marketplace behavior, socket
  listeners, daemons, and public service modes.
- Signing, SBOM publication, package-manager channels, installed-command distribution,
  native images, container images, release automation, or automatic publication.

Breaking-change categories that may be considered for v3.0:

- Generated artifact schema markers, required files, field names, nesting, nullability,
  repeated-value conventions, and documented field semantics.
- Evidence field shape, evidence type taxonomy, evidence ID stability expectations,
  confidence/uncertainty semantics, excerpt boundaries, and evidence-resolution rules.
- Adapter/source-registry/provenance join keys, source-document identity rules, accepted
  provenance metadata, adapter-context placement, and regeneration requirements.
- Query and agent-consumption supported schema markers, validation failures, exit codes,
  command grammar, output wording where documented as contractual, and rejection behavior
  for unsupported artifact sets.
- Workspace-map, graph, impact, profile, policy-profile, cache, and AI-presentation
  schema markers and metadata placement when those surfaces are included in v3
  compatibility.
- CLI/config selection semantics, default compatibility behavior, and migration or
  deprecation warnings.
- Release and distribution compatibility expectations such as supported artifact naming,
  checksum validation wording, and documented installation channel support when those
  surfaces are intentionally changed.

Breaking changes are not accepted merely because v3 is a major version. Each breaking
change must name the old behavior, new behavior, affected consumers, migration action,
tests or evaluation coverage, release-note impact, and security-review impact before
implementation.

Current shipped behavior boundary:

- Current no-adapter scans remain on `project-map.json` `schema_version: "1.0"`.
- Current adapter-enabled scans use `project-map.json` `schema_version: "2.0"` plus the
  optional `source-registry.json` provenance artifact.
- Current query support remains focused on no-adapter v1-compatible artifact sets unless
  a later release explicitly documents adapter-aware or v3-aware query behavior.
- This section describes planned v3.0 scope, not behavior already available in the latest
  published release.
