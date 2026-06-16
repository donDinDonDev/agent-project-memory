# Roadmap

## Current Status

The latest published release is `v1.4.0`, with executable jar and `SHA256SUMS` assets.
Normal generated `project-map.json` files use `schema_version: "1.0"` as the
stable-line marker. The v1.4 incremental cache expansion is additive: Maven, Gradle,
source-visible output, generated-source metadata, agent profile artifacts, and evidence
semantics are preserved, while opt-in `--incremental` scans may reuse an unchanged
generated output set after strict metadata validation.

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
deterministic agent profile artifacts for supported coding-agent consumption, redacted
scan metadata, safe root-local YAML config support, stable CLI help/version behavior,
and a documented release-jar verification path.

Earlier v0.x release notes remain available for historical scope, compatibility, and
validation details. Future work is organized by release tracks instead of extending the
original v0.1 baseline. Connector/import work remains post-v0.1 future work and is not
started. The published v1.3 expansion is agent output profiles, scoped as an additive
v1.0-compatible output-contract expansion rather than a schema marker migration.

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

### v1.5.0: Lightweight Relation Graph (Planned)

Product outcome: add a bounded deterministic graph artifact that helps humans and
coding agents navigate relationships between existing project-memory facts without
turning the graph into a full architecture, runtime dependency, or impact model.

Planned contract decision:

- Emit a separate `.project-memory/project-graph.json` artifact once the feature is
  implemented. The graph is not a top-level `project-map.json` section, and the design
  is not parked.
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

Planned graph scope:

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

Validation expectations before release:

- Focused tests and goldens for graph schema, deterministic IDs, sorting, node/edge
  taxonomy, cap behavior, evidence ID resolution, derivation metadata, existing output
  stability, and incremental cache interaction.
- Representative scans checking graph size/noise, duplicate IDs, dangling edges,
  unresolved evidence references, and sensitive-data boundaries.
- Risk-based review for graph output paths, generated artifact ownership, cache output
  fingerprints, evidence-reference handling, graph size limits, and JSON rendering.

Possible later tracks:

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
