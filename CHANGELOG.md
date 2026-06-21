# Changelog

All notable changes to this project will be documented in this file.

The format follows the spirit of [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and versioning follows SemVer intent. During the `0.x` line, output contracts may still
evolve, but schema and evidence changes should be explicit in release notes and
architecture documents.

## [Unreleased]

## [2.9.0] - 2026-06-21

Release status: release candidate. The `v2.9.0` tag, GitHub Release, executable jar,
and `SHA256SUMS` assets are not published yet.

### Added

- Documented the planned v3 schema/API migration strategy, including artifact-set
  regeneration, fail-closed compatibility behavior, deprecation guidance, compatibility
  test categories, and the future evidence/provenance boundary without implementing v3
  behavior.
- Documented the planned v2.9 preparation boundary and frozen v3.0 scope direction,
  including included platform scope, excluded/deferred surfaces, implementation
  prerequisites, breaking-change categories, and the distinction between planned v3
  design and current shipped behavior.

### Changed

- Aligned the Maven project version, README local build examples, roadmap status,
  v2.9 release notes, and public release wording for release-candidate review.

### Security

- Kept v3 security review and release-readiness boundaries as planned future work.
  v2.9.0 does not change shipped parser, path, filesystem, output, evidence, network,
  authentication, provider, plugin/runtime, dependency, release artifact, or
  publication behavior.

### Not Included

- v3 schema markers, serializers, readers, migration code, compatibility behavior,
  generated output changes, evidence semantic changes, server/API/MCP/editor/plugin
  runtime, provider AI, live network connectors, credentials, source upload, signing,
  SBOM publication, package-manager channels, native images, container images, release
  automation, artifact upload, release publication, tag creation or movement, or
  automatic code modification.

## [2.8.0] - 2026-06-21

Release status: published. The `v2.8.0` tag, GitHub Release, executable jar, and
`SHA256SUMS` assets are published.

### Added

- Documented the v2.8 distribution and supply-chain hardening boundary,
  keeping the GitHub Release executable jar plus `SHA256SUMS` as the supported
  distribution baseline while defining future no-secret checksum/metadata validation,
  release approval gates, credential boundaries, CI/dependency workflow constraints, and
  parked signing, SBOM, package-manager, native image, container, and release automation
  work.
- Added a local artifact-integrity dry-run helper for packaged release candidates. It
  validates the candidate jar filename, CLI version output, manifest entrypoint, Maven
  artifact metadata, exact local dry-run release asset list, and filename-only
  `SHA256SUMS` contents without publishing, uploading, signing, creating releases,
  moving tags, requiring secrets, or contacting remote services.

### Changed

- Aligned the Maven project version, README local build examples, roadmap status,
  v2.8 release notes, and public release wording for publication.
- Synchronized release-process wording around the v2.8 distribution hardening baseline
  while preserving manual maintainer authority for all publication actions.

### Fixed

- Synchronized `SECURITY.md` to identify `v2.8.0` as the latest published supported
  public release line for vulnerability reports.

### Security

- Completed release-level distribution and supply-chain review for the accepted v2.8
  scope, with no release-blocking findings remaining before release prep.
- Kept the artifact-integrity dry-run local-only: it validates jar filename, CLI
  version output, manifest entrypoint, Maven artifact metadata, exact local dry-run
  asset list, and filename-only `SHA256SUMS` contents without network access,
  credentials, uploads, signing, tag mutation, release mutation, or package publication.

### Not Included

- Signing, SBOM generation or publication, package-manager channels, first-party
  installed-command distribution, native images, container images, release automation,
  automatic artifact upload, automatic release publication, automatic tag creation or
  movement, dependency workflow automation, CI release workflow changes, credentials,
  network release actions,
  SaaS, web UI, repository chat, generic RAG, provider AI, plugin/runtime expansion,
  or automatic code modification.

## [2.7.0] - 2026-06-20

Release status: published. The `v2.7.0` tag, GitHub Release, executable jar, and
`SHA256SUMS` assets are published.

### Added

- Documented the v2.7 policy profile boundary, selecting local
  configuration presets and guardrails with explicit selector precedence,
  fail-closed unsafe combinations, additive scan metadata, and a non-evidence
  evidence decision while parking hosted policy management, network defaults,
  credentials, compliance/security certification claims, generated-source content
  scanning, and automatic code modification.
- Implemented the v2.7 policy profile selector/config foundation for
  `scan <path> --policy-profile <name>` and root-local
  `policy_profile: <name>` with accepted names, duplicate/unknown selector
  failures, and fail-closed config-plus-CLI mismatch validation while preserving
  no-profile scan compatibility.
- Implemented the accepted policy profile behavior matrix for `guarded-local`,
  `docs-focused`, and `adapter-local`, including fail-closed unsafe-combination
  validation, bounded `scan.policy_profile` execution metadata for explicit
  selections only, no-profile output compatibility, and tests that keep policy
  metadata outside evidence semantics.

### Changed

- Aligned the Maven project version, README local build examples, roadmap status,
  v2.7 release notes, output/evidence wording, and public release wording for
  publication.

### Security

- Completed release-level security review for the accepted v2.7 policy profile scope,
  with no release-blocking findings remaining.
- Hardened release output safety around non-standard authorization header
  redaction, bounded Java/OpenAPI analysis, generated/build/vendor-like OpenAPI spec
  exclusions, query artifact evidence path validation, project-map evidence-reference
  validation, and evidence-backed impact path matching.

### Not Included

- Hosted policy management; remote policy registry; remote configuration; user-home
  policy discovery; organization policy crawling; background sync; update checks;
  telemetry; network calls; provider API calls; source upload; credential lookup,
  storage, validation, or handling; real provider AI; embeddings; vector stores;
  repository chat; generic RAG; web UI; server/API/editor/plugin runtime;
  generated-source content scanning; symlink following; security certification;
  compliance mode; vulnerability scanning; secret inventory; package-manager
  publication; signing; native images; container images; release automation; or
  automatic code modification.

## [2.6.0] - 2026-06-20

Release status: published. The `v2.6.0` tag, GitHub Release, executable jar, and
`SHA256SUMS` assets are published.

### Added

- Documented the planned v2.6 change-impact workflow boundary, selecting a read-only
  single-repo `query <path> impact --files ...` shape over existing no-adapter
  generated artifacts, one-hop graph navigation, confidence-labeled text output, and
  explicit parking for generated impact reports, raw diff input, workspace impact,
  adapter-aware impact, source/spec scoring, documentation freshness scoring,
  vulnerability claims, business-priority claims, and automatic code modification.
- Implemented the first `query <path> impact --files ...` direct mapping foundation
  over existing no-adapter `project-map.json`, `evidence-index.jsonl`, and
  `project-graph.json` artifacts, with explicit repository-relative changed-file input
  validation, direct matches to existing evidence/source/fact/graph references,
  `not_represented` rows, bounded diagnostics, no source readback, no scan refresh, no
  generated artifact mutation, no impact report, and no Git inspection.
- Implemented conservative one-hop impact projection for `query <path> impact --files
  ...`, rendering graph neighbors, relation-status rows, and low-confidence
  quality/change-risk planning hints from existing `project-map.json` and
  `project-graph.json` records without adding stable JSON output, generated impact
  reports, raw diff handling, workspace impact, adapter-aware impact, source readback,
  scoring claims, vulnerability claims, business-priority claims, or code-change
  authority.

### Changed

- Aligned the Maven project version, README local build examples, roadmap status, v2.6
  release notes, and public release wording for publication.

### Security

- Completed release-level security review for the accepted v2.6 impact query scope,
  with no release-blocking findings remaining.

### Not Included

- Top-level `impact` command; `--from-git-diff`; raw diff parsing; Git working-tree
  inspection; branch or commit comparison; rename detection; stable JSON output;
  generated Markdown; generated impact reports; workspace impact; cross-repo impact;
  adapter-aware impact; source/spec scoring; documentation freshness scoring; runtime
  tracing; call graphs; vulnerability claims; business-priority claims; package-manager
  publication; signing; native images; container images; release automation; or
  automatic code modification.

## [2.5.0] - 2026-06-20

Release status: published. The `v2.5.0` tag, GitHub Release, executable jar, and
`SHA256SUMS` assets are published.

### Added

- Documented the planned v2.5 workspace design boundary, selecting an explicit
  `workspace scan <config>` shape, required logical repo identity, separate
  `workspace-map.json` placement, composite `repo_id` plus `evidence_id` workspace
  evidence references, and parking cross-repo relation emission and workspace query
  behavior behind later explicit implementation gates.
- Implemented the first validation-only `workspace scan <config>` foundation for local
  workspace YAML configs, required unique `repo_id` values, workspace-relative member
  roots, bounded invalid-config diagnostics, unsafe root rejection, and no child scans
  or generated-output writes.
- Implemented workspace map aggregation for `workspace scan <config>`, writing
  workspace-root `.project-memory/workspace-map.json` from existing member
  `.project-memory/` artifacts with explicit `repo_id`, member artifact schema
  summaries, bounded diagnostics for missing or invalid member artifacts, empty
  cross-repo relations, and composite `repo_id` plus existing per-repo `evidence_id`
  sample references without mutating child repositories or single-repo artifacts.

### Changed

- Aligned the Maven project version, README local build examples, roadmap status, v2.5
  release notes, and public release wording for publication.

### Security

- Completed release-level security review for the accepted v2.5 workspace scope, with
  no release-blocking findings remaining.

### Not Included

- Cross-repo relation emission; workspace query; workspace `agent-context`; workspace
  graph output; adapter-aware workspace context; child repository scan refresh or
  mutation; remote discovery; network access; credentials; SaaS; repository chat;
  generic RAG; semantic search; package-manager publication; signing; native images;
  container images; release automation; or automatic code modification.

## [2.4.0] - 2026-06-19

Release status: published. The `v2.4.0` tag, GitHub Release, executable jar, and
`SHA256SUMS` assets are published.

### Added

- Documented the planned v2.4 read-only agent consumption boundary, selecting a
  CLI-only `query`-based agent context surface as the first implementation slice and
  deferring MCP, server, public API, editor plugin, network, credential, telemetry,
  source-upload, and automatic code-modification behavior behind later explicit design
  and security review.
- Implemented `query <path> agent-context` as a deterministic stdout-only read-only
  agent context view over existing no-adapter `project-map.json` schema `1.0` and
  `evidence-index.jsonl` artifacts, with optional valid `project-graph.json` navigation
  metadata, no generated artifacts, no source readback, no adapter-aware query, no
  network or credentials, and focused no-write/content-safety coverage.

### Changed

- Aligned the Maven project version, README local build examples, roadmap status, v2.4
  release notes, and public release wording for release-prep review.

### Security

- Completed release-track security review for the CLI-only `agent-context` boundary
  with no release-blocking findings remaining.

### Not Included

- MCP/server/API/editor/plugin/runtime behavior; network access; remote service calls;
  credentials; credential lookup or storage; telemetry; source upload; raw prompt
  transcript serialization; adapter-aware query; semantic search; embeddings; vector
  stores; generic RAG; repository chat; real AI provider calls; package-manager
  publication; installed-command distribution; signing; native images; container
  images; release automation; or automatic code modification.

## [2.3.0] - 2026-06-19

Release status: published. The `v2.3.0` tag, GitHub Release, executable jar, and
`SHA256SUMS` assets are published.

### Added

- Finalized the planned v2.3 optional AI presentation boundary, including the separate
  `.project-memory/ai-presentations/` artifact surface, non-evidence metadata labels,
  mock/no-network first provider mode, forbidden inputs and authoritative outputs,
  prompt/content-injection controls, privacy defaults, and release blockers before
  implementation.
- Implemented explicitly enabled mock/no-network AI presentation artifacts through
  `scan --ai-presentation mock_no_network`, writing
  `.project-memory/ai-presentations/manifest.json` and
  `.project-memory/ai-presentations/brief.md` from deterministic generated artifacts
  only, with non-authoritative/non-evidence labels, no default AI artifacts, no real
  provider, no network access, no credentials, no source upload, no prompt transcript
  serialization, and focused safety/golden coverage.

### Changed

- Aligned the Maven project version, README local build examples, roadmap status, v2.3
  release notes, and public release wording for release-prep review.

### Not Included

- Real AI provider integration; provider SDKs; network access; provider credentials,
  credential lookup, credential storage, telemetry, source upload, prompt logging, raw
  prompt transcript serialization, embeddings, vector search, repository chat, generic
  RAG, plugin loading, API/server behavior, SaaS, web UI, package-manager publication,
  installed-command distribution, signing, native images, container images, release
  automation, or automatic code modification.

## [2.2.0] - 2026-06-19

Release status: published. The `v2.2.0` tag, GitHub Release, and release assets are
published.

### Added

- Implemented disabled-by-default Jira/YouTrack/Confluence local export import for
  explicitly configured repository-relative JSON files using
  `format: "agent-project-memory.connector_export.v1"`, including source types
  `jira_issue`, `youtrack_issue`, `youtrack_article`, and `confluence_page`,
  `source-registry.json` schema `1.2` connector provenance metadata, existing
  `project-map.json` adapter context output, bounded diagnostics, fake-only fixtures,
  and regression coverage for no-adapter compatibility and raw body/path/credential
  non-serialization.
- Documented the v2.2 Jira/YouTrack/Confluence local export connector boundary,
  including supported source types, source identity rules, source-registry provenance,
  adapter-context placement, credential/network defaults, and evidence/provenance
  separation for the local import implementation boundary.

### Changed

- Aligned the Maven project version, README local build examples, roadmap status,
  security-policy wording, v2.2 release notes, and public release wording for
  release-prep review.
- Updated post-release documentation status to mark `v2.2.0` as published with release
  assets.

### Not Included

- Live Jira, YouTrack, or Confluence API fetching; connector credentials, credential
  lookup, OAuth, PAT, app password, API key, cookie, credential storage, retry/backoff,
  rate-limit handling, pagination, background sync, remote cache, source upload,
  adapter-aware query support, AI behavior, plugin loading, SaaS, web UI, repository
  chat, generic RAG, package-manager publication, installed-command distribution,
  signing, native images, container images, release automation, or automatic code
  modification.

## [2.1.0] - 2026-06-18

Release status: published. The `v2.1.0` tag, GitHub Release, and release assets are
published.

### Added

- Implemented disabled-by-default GitHub/GitLab local export import for explicitly
  configured repository-relative JSON files using
  `format: "agent-project-memory.git_hosting_export.v1"`, including Git hosting source
  types, stable provider/host/namespace/record identity normalization,
  `source-registry.json` schema `1.1` provenance metadata, existing
  `project-map.json` adapter context output, bounded diagnostics, fake-only fixtures,
  and regression coverage for no-adapter compatibility and raw text/path redaction.

### Changed

- Aligned the Maven project version, README local build examples, roadmap status,
  security-policy wording, v2.1 release notes, and public adapter contract wording for
  release-prep review.
- Updated post-release documentation status to mark `v2.1.0` as published with release
  assets.

### Not Included

- Live GitHub or GitLab API fetching; connector credentials, credential lookup, OAuth,
  PAT, GitHub App, GitLab token, credential storage, retry/backoff, rate-limit,
  background sync, remote cache, source upload, adapter-aware query support, AI
  behavior, plugin loading, SaaS, web UI, repository chat, generic RAG, package-manager
  publication, installed-command distribution, signing, native images, container
  images, release automation, or automatic code modification.

## [2.0.0] - 2026-06-18

### Added

- Finalized the planned initial v2.0 adapter design boundary in architecture docs,
  including the no-adapter runtime invariant, source registry placement, provenance
  versus evidence separation, explicit v2 schema-marker strategy for adapter-backed
  output, disabled-by-default local import configuration, and conservative
  implementation order.
- Added the initial adapter-domain contract foundation for deterministic source-document
  IDs and required provenance validation without adding adapter readers, generated
  adapter output, source registry emission, evidence types, network/auth, plugin, or AI
  behavior.
- Added the disabled-by-default adapter configuration safety gate for a future local
  structured import adapter, including repository-relative regular-file path
  validation, network-off defaults, redacted scan metadata, and focused unsafe-path
  tests without adding an adapter reader, parser, source registry writer, or
  adapter-backed project facts.
- Implemented the local structured import reference adapter for explicitly configured
  repository-relative export files, including bounded JSON parsing, deterministic
  source-document/provenance IDs, `source-registry.json`, `project-map.json`
  `schema_version: "2.0"` adapter context, malformed/partial/stale diagnostics,
  no raw body/path serialization, and regression/golden coverage that keeps adapter
  records out of Java/Spring facts and `evidence-index.jsonl`.
- Documented v1-to-v2 migration and compatibility expectations for adapter-enabled
  output, including no-adapter `schema_version: "1.0"` compatibility,
  `source-registry.json` handling, full artifact-set regeneration, downstream consumer
  behavior, current query limits, and evidence/provenance separation.

### Security

- Hardened trusted local input handling so scan config, Java/source, Maven/Gradle build,
  resource config, OpenAPI/spec, Markdown/document, adapter import, and query artifact
  readers reject multi-link regular files before treating repository-relative content as
  source-owned input.

### Changed

- Aligned the Maven project version, README local build examples, roadmap status,
  security-policy wording, and v2.0 release notes for release-prep review.

### Not Included

- Networked GitHub, GitLab, Jira, YouTrack, Confluence, or other API connectors;
  connector credentials or credential storage; source upload; telemetry; plugin
  loading; MCP/server surfaces; AI provider behavior; embeddings; generic RAG;
  repository chat; SaaS; web UI; adapter-aware query support; package-manager
  publication; installed-command distribution; signing; native images; container
  images; release automation; or automatic code modification.

## [1.9.0] - 2026-06-17

### Added

- Documented the planned v2 adapter platform design boundary, including optional adapter
  responsibilities, core/adapters separation, normalized source-document and connector
  provenance requirements, network-off defaults, and draft output/evidence migration
  questions.
- Documented the planned optional v2 AI presentation boundary, including allowed
  deterministic-memory inputs, forbidden authoritative outputs, non-evidence labeling,
  provider/privacy/network/source-upload defaults, and adapter-provenance interaction
  without adding AI provider implementation.
- Extended the planned v2 threat model for adapters, connectors, optional AI, plugin/API
  surfaces, external data, credential handling, network defaults, provenance boundaries,
  and future security review gates without adding implementation behavior.
- Added v1.9.0 release notes covering v2 architecture preparation, compatibility,
  validation, not-included scope, and expected release assets.

### Changed

- Aligned public release status, roadmap wording, the README documentation map, and
  security-policy supported-version wording for the `v1.9.0` release.
- Aligned the Maven project version and README local build examples for the `v1.9.0`
  release.
- Updated post-release documentation status to mark `v1.9.0` as published with release
  jar and checksum assets.

### Not Included

- Production adapter APIs, connector implementations, network or authentication
  behavior, plugin platforms, MCP/server surfaces, AI provider code, embeddings, generic
  RAG, repository chat, SaaS, web UI, generated output schema changes, evidence
  semantic changes, analyzer behavior changes, new CLI commands or flags, package
  publication, release automation, or automatic code modification.

## [1.8.0] - 2026-06-17

### Added

- Added a public `stage3-project-map` generated-output example snapshot with
  regeneration instructions and README discoverability.
- Added v1.8.0 release notes covering adoption polish, output compatibility,
  validation, not-included scope, and expected release assets.

### Changed

- Expanded the public contributor guide and issue/PR templates with bounded
  contribution paths, good-first scope guidance, and clearer output/evidence context
  prompts.
- Aligned the Maven project version, README local build examples, roadmap status,
  public examples regeneration command, and release notes for the `v1.8.0` release
  materials.
- Updated post-release documentation status to mark `v1.8.0` as published with release
  jar and checksum assets.

### Not Included

- Analyzer features, CLI commands or flags, generated output schema changes, evidence
  semantic changes, release automation, package-manager publication, signing, native
  images, container images, connectors, SaaS, web UI, repository chat, generic RAG, LLM
  calls in the core analyzer or query layer, or automatic code modification.

## [1.7.0] - 2026-06-17

### Added

- Implemented the initial v1.7 deterministic redaction primitive for obvious
  secret-looking output values, including evidence excerpt hardening, selected
  generated JSON/Markdown rendering, selected profile rendering, query render-time
  hardening for existing artifacts, and bounded CLI error-message hardening.
- Documented the v1.7 security and secrets-safety design boundary, including explicit
  non-guarantee language and the decision to preserve the existing evidence field
  shape.
- Added a public security threat model and refreshed the root security policy to
  describe the local-first trust boundary, vulnerability reporting scope, and redaction
  hardening without positioning the tool as a vulnerability scanner or
  general-purpose secret scanner.
- Added v1.7.0 release notes covering redaction compatibility, security boundaries,
  validation, not-included scope, and expected release assets.

### Changed

- Aligned the Maven project version, README local build examples, roadmap status,
  output contract status wording, public threat model wording, and release notes for
  the `v1.7.0` release materials.
- Updated post-release documentation status to mark `v1.7.0` as published with release
  jar and checksum assets.
- Updated post-release documentation status to mark `v1.6.0` as published with release
  jar and checksum assets.

### Fixed

- Prevented obvious secret-looking local Markdown heading values from leaking through
  generated document heading IDs, heading anchors, chunk heading links, graph source
  references, and heading evidence symbol keys by deriving those keys from the
  redaction-safe heading text.
- Hardened JSON-style quoted credential-key redaction so generated excerpts and
  rendered output mask the full value without leaving suffixes after the redaction
  marker.
- Sanitized bounded CLI error text for symlinked scan-root output-safety failures so
  canonical local absolute paths are not exposed.
- Redacted legacy graph `source_ref.id` rendering in graph-backed query output while
  preserving exact lookup semantics and leaving artifact files unchanged.

### Security

- Kept v1.7 redaction local, deterministic, and bounded to selected generated or
  rendered strings. Redaction remains output hardening, not complete secret discovery,
  a credential classifier, a secret inventory, vulnerability evidence, or security
  correctness proof.
- Completed the v1.7 surface-safety, path-safety, and release-prep review sequence with
  no release-blocking findings remaining.

### Not Included

- Complete secret detection, secret inventory, credential validation, vulnerability
  scanning, security correctness claims, external secret scanners, network access,
  telemetry, connectors, SaaS, web UI, repository chat, generic RAG, embeddings, LLM
  calls in the core analyzer or query layer, generated-source content scanning, default
  symlink following, package-manager publication, signing, native images, container
  images, release automation, or automatic code modification.

## [1.6.0] - 2026-06-16

### Added

- Documented the planned v1.6 local read-only query CLI contract, including `query`
  command grammar, generated-artifact input policy, list/explain/find/relations
  behavior, implemented text output, future JSON output boundaries, exit behavior, and
  implementation validation expectations.
- Implemented the first read-only `query` foundation for artifact root resolution,
  bounded loading and validation of `project-map.json`, `evidence-index.jsonl`, and
  optional `project-graph.json`, plus minimal command help and skeleton validation
  behavior. Relations rendering and graph lookup UX are now implemented in the
  dedicated relation lookup slice; JSON result envelopes remain future work.
- Implemented deterministic text output for the basic read-only list commands:
  `query <path> list modules`, `list endpoints`, `list api-operations`,
  `list entities`, and `list tests`. The commands render existing generated facts
  without scanning source or writing artifacts, keep source-visible endpoints separate
  from spec-backed declared operations, keep entities separate from embeddables, and
  preserve useful module IDs, fact IDs, relation/status labels, confidence,
  uncertainty, and evidence ID references.
- Implemented deterministic text output for exact read-only lookup commands:
  `query <path> explain evidence <id>`, `find fact <term>`, and `find symbol <term>`.
  The commands read existing generated artifacts without scanning or writing, resolve
  evidence IDs exactly from `evidence-index.jsonl`, perform case-sensitive fact and
  symbol lookup only over generated artifact fields, preserve navigation references as
  non-evidence metadata, and return no-result lookups without falling back to source
  reads or fuzzy search.
- Implemented deterministic one-hop text output for read-only graph relation lookup:
  `query <path> relations <id>`. The command requires a valid `project-graph.json`,
  accepts a graph node ID or generated fact ID mapped through node `source_ref`,
  supports the existing `--direction incoming|outgoing|both` behavior with `both` as
  default, preserves graph edge fields and `relation_statuses[]` separately, and keeps
  graph derivation as non-evidence navigation metadata.
- Added v1.6.0 release notes covering query command compatibility, validation, security
  boundaries, not-included scope, and expected release assets.

### Changed

- Aligned the Maven project version, README local build examples, roadmap status, output
  contract status wording, and release notes for the `v1.6.0` release materials.

### Fixed

- Fixed non-graph query commands so a present malformed optional `project-graph.json`
  does not block list commands, evidence explanation, symbol lookup, or non-graph fact
  lookup when `project-map.json` and `evidence-index.jsonl` are valid. Relation lookup
  and graph ID-shaped fact lookup still require a valid graph artifact when they use
  graph-backed results.

### Security

- Completed query UX evaluation and risk-based security review for the v1.6 query path,
  artifact reader, JSON/JSONL loading, and deterministic stdout behavior with no
  release-blocking findings remaining.
- Kept query local-only, read-only, and artifact-backed: query commands do not scan
  source, create or mutate `.project-memory/`, refresh cache/profile artifacts, write
  repository files, call LLMs, use connectors, or treat query output, diagnostics, graph
  derivation metadata, generated Markdown, profile artifacts, or cache metadata as
  evidence.

### Not Included

- Stable JSON query output, chat or natural-language query, substring/fuzzy/regex/
  semantic/vector search, impact analysis, full call graphs, runtime dependency graphs,
  runtime Spring graphs, source/spec agreement scoring, documentation freshness scoring,
  coverage or CI claims, connectors, network access, SaaS, web UI, repository chat,
  generic RAG, LLM calls in the core analyzer, or automatic code modification.

## [1.5.0] - 2026-06-16

### Added

- Implemented the v1.5 lightweight relation graph foundation: supported scans now write
  `.project-memory/project-graph.json` with `graph_schema_version: "1.0"`,
  deterministic graph nodes, direct/structural `owns` and `declares` edges,
  derivation-only structural edge metadata, bounded graph warnings, and existing
  `evidence_ids` references without duplicating evidence records as graph nodes.
- Added conservative inferred and statused graph relation material from existing
  tested-subject rows, Spring Data repository/entity rows, and document reconciliation
  hints, preserving relation status, relation attributes, confidence, uncertainty, and
  existing evidence references while keeping unsupported or no-target rows out of
  graph edges.
- Included `project-graph.json` in incremental cache output fingerprints with
  `output_kind: "project_graph"` so cache hits verify the graph digest and size before
  reusing a generated output set.
- Added v1.5.0 release notes covering graph output compatibility, validation, security
  boundaries, not-included scope, and expected release assets.

### Changed

- Documented the v1.5 lightweight relation graph contract boundary: a separate
  `.project-memory/project-graph.json` artifact with its own graph schema marker,
  deterministic node and edge IDs, explicit evidence or derivation basis, bounded
  confidence and uncertainty semantics, size/noise limits, and no `project-map.json`
  schema migration.
- Aligned the Maven project version, README local build examples, roadmap status, and
  release notes for the `v1.5.0` release materials.
- Updated post-release documentation status to mark `v1.5.0` as published with release
  jar and checksum assets.
- Updated post-release documentation status to mark `v1.4.0` as published with release
  jar and checksum assets.

### Security

- Completed graph evaluation and risk-based security review for the v1.5 graph output
  surface with no release-blocking findings remaining.
- Kept graph output local-only and evidence-bounded: graph nodes and edges reference
  existing evidence IDs or non-evidence derivation metadata, do not duplicate evidence
  records as graph nodes, do not read generated-source contents, and do not claim call
  reachability, runtime dependencies, source/spec agreement, documentation freshness,
  coverage, vulnerabilities, or correctness.

### Not Included

- Query/read-only explorer commands, impact analysis commands, runtime call graphs,
  dependency graphs, Spring bean/autowiring graphs, source/spec agreement scoring,
  documentation freshness scoring, generated-source content scanning, connectors,
  network access, SaaS, web UI, repository chat, generic RAG, LLM calls in the core
  analyzer, or automatic code modification.

## [1.4.0] - 2026-06-15

### Added

- Implemented the first v1.4 incremental cache metadata foundation: opt-in
  `scan <path> --incremental` runs the normal full analysis path, preserves the normal
  generated output set, and writes repository-local cache metadata under
  `.project-memory/cache/v1/{manifest.json,inputs.jsonl,outputs.jsonl}` after
  successful output generation. This foundation records schema/version metadata,
  selected profile/config/option matching metadata, input fingerprints, and generated
  output fingerprints, including path-only standard source, test, and resource root
  directory-presence fingerprints, without enabling cache-hit reuse.
- Added validated whole-output-set cache hits for `scan <path> --incremental`: unchanged
  repository states can reuse the existing generated output set only when cache schema,
  tool version, selected CLI options, selected config, selected agent profiles, input
  fingerprints, and current output fingerprints all match.
- Added v1.4.0 release notes covering incremental cache compatibility, validation,
  security boundaries, not-included scope, and expected release assets.
- Added a v1.4 incremental cache evaluation summary for packaged CLI full-scan,
  cache-miss, cache-hit, invalidation, no-profile, and selected-profile validation.

### Changed

- Documented the planned v1.4 incremental cache design boundary: optional
  cache-assisted incremental scans, fixed repository-local cache metadata paths,
  fail-closed invalidation, full-scan output parity, cache-sensitive-data limits, and
  the evidence decision that cache state is execution metadata rather than project
  evidence.
- Aligned the Maven project version, README local build examples, roadmap status, and
  release notes for the `v1.4.0` release materials.
- Confirmed that `schema_version: "1.0"` remains the stable-line marker for v1.4;
  incremental cache files are additive execution metadata outside `project-map.json`
  and `evidence-index.jsonl`.
- Updated post-release documentation status to mark `v1.3.0` as published with release
  jar and checksum assets.

### Security

- Kept the v1.4 cache path local-only, bounded, and fail-closed: normal scans without
  `--incremental` ignore cache state, cache metadata stays under
  `.project-memory/cache/v1/`, symlinked or multi-link cache targets are not trusted or
  overwritten, generated-source roots remain path-only metadata, output digest
  mismatches and unsafe generated-source child path changes fall back to full analysis,
  and cache files remain non-evidence execution metadata.
- Fixed an incremental cache/resource-config parity edge case by keeping standard
  resource roots and supported resource config files reached through symlinked path
  segments out of full resource-config discovery. This preserves the documented
  no-symlink policy and prevents validated cache hits from retaining stale config-file
  facts after symlink removal or retargeting.

## [1.3.0] - 2026-06-15

### Added

- Documented the v1.3 deterministic agent output profile contract, including
  supported `codex`, `claude`, `cursor`, and `generic` profiles, opt-in profile
  generation, profile artifact names under `.project-memory/agent-profiles/`,
  no default repository-file modification, profile validation expectations, and the
  evidence decision that profile artifacts reference existing evidence without creating
  new evidence records.
- Implemented opt-in deterministic agent profile artifacts with repeatable
  `scan <path> --agent-profile <profile>` selection for `codex`, `claude`, `cursor`,
  `generic`, and `all`, idempotent duplicate selectors, `agent-profiles/manifest.json`,
  selected profile-specific Markdown content generated from existing structured facts
  and evidence references, unchanged no-profile default output, and no
  `project-map.json` or `evidence-index.jsonl` semantic changes.
- Added v1.3.0 release notes covering agent profile compatibility, validation, security
  boundaries, not-included scope, and expected release assets.

### Changed

- Aligned the Maven project version, README local build examples, roadmap status, and
  release notes for the `v1.3.0` release materials.
- Clarified that `schema_version: "1.0"` remains the stable-line marker for v1.3;
  agent profile artifacts are additive generated presentations, while existing
  project-map, evidence-index, endpoint, guide, generated-source, and evidence
  semantics are preserved.

### Security

- Completed risk-based release review for the agent profile presentation boundary with
  no release-blocking findings remaining.
- Kept profile generation local-only and deterministic: profile artifacts do not call
  LLMs, external services, connectors, editors, local agent runtimes, build tools, or
  generated-source scanners; do not create evidence records; and do not modify root
  repository instruction/config files.

### Not Included

- LLM calls, AI-generated summaries, source upload, connectors, network access,
  telemetry, editor integrations, MCP/server surfaces, plugin platforms, repository
  chat, generic RAG, automatic repository-file modification, generated-source content
  scanning, build execution, package-manager publication, SaaS, web UI, or automatic
  code modification.

## [1.2.0] - 2026-06-15

### Added

- Implemented the v1.2 generated-source/codegen metadata-only slice: top-level
  `generated_sources` policy metadata, bounded generated-root inventory with
  `content_status: "not_scanned"`, generator/codegen warning references, unsafe-path
  and cap diagnostics, guide wording, and golden coverage, while keeping generated
  source content scanning disabled and `features.generated_sources: true` invalid.
- Added v1.2.0 release notes covering generated-source/codegen compatibility,
  validation, security boundaries, not-included scope, and expected release assets.

### Changed

- Aligned the Maven project version, README local build examples, roadmap status, and
  release notes for the `v1.2.0` release materials.
- Clarified that `schema_version: "1.0"` remains the stable-line marker for v1.2;
  generated-source/codegen metadata is additive, while existing Maven, Gradle,
  source-visible output, and evidence semantics are preserved.
- Updated post-release documentation status to mark `v1.2.0` as published with release
  jar and checksum assets.

### Security

- Completed risk-based release review for the generated-source/codegen metadata-only
  boundary with no release-blocking findings remaining.
- Kept generated-source handling local-only and metadata-only: the analyzer does not
  scan generated source contents, execute generators, run Maven or Gradle tasks, resolve
  plugins or dependencies, follow symlinks, or serialize generated output contents.

### Not Included

- Generated-source content scanning, whether default or opt-in, generator execution,
  Maven lifecycle execution, Gradle task execution, plugin/dependency/task/repository
  resolution, generated-source graph reconstruction, runtime API freshness checks,
  generated client or server API reconstruction, automatic OpenAPI/source matching,
  custom Gradle `sourceSets` support, connectors, network access, telemetry,
  package-manager publication, SaaS, web UI, repository chat, generic RAG, LLM calls in
  the core analyzer, or automatic code modification.

## [1.1.0] - 2026-06-14

### Added

- Added bounded static Gradle Java/Spring layout support as an
  additive `schema_version: "1.0"` compatibility expansion, including supported Gradle
  root build files, project build files, standard Java/test/resource roots, simple
  static settings include parsing, mixed Maven/Gradle detection, Gradle warning and
  diagnostic taxonomy, Gradle `build_file` evidence semantics, guide wording,
  validation expectations, and deferred custom `sourceSets` support.
- Added Gradle module inventory fields for Gradle and mixed builds, including
  `project.build.root_build_files[]`, module `build_systems`, `gradle_project_path`,
  and module-owned `build_config.gradle` build-file orientation.
- Added focused Gradle fixtures and golden output coverage for single-project Gradle
  layouts, simple multi-project Gradle layouts, static `settings.gradle.kts` includes,
  unsupported dynamic includes, duplicate project paths, missing project directories,
  unsupported Gradle modules, and Gradle analyzer integration over existing Java/Spring
  analyzers.
- Added a v1.1 Gradle evaluation summary for packaged CLI scans over pinned Gradle
  Java/Spring targets and a selected Maven regression target.
- Added v1.1.0 release notes covering Gradle compatibility, validation, security
  boundaries, not-included scope, and expected release assets.

### Changed

- Aligned the Maven project version, README local build examples, roadmap status,
  release notes, output/evidence contract wording, and public architecture overview for
  the `v1.1.0` release materials.
- Clarified that `schema_version: "1.0"` remains the stable-line marker for v1.1;
  Gradle and mixed Maven/Gradle fields are additive, while existing Maven output and
  evidence semantics are preserved.
- Updated post-release documentation status to mark `v1.1.0` as published with release
  jar and checksum assets.

### Fixed

- Updated post-release documentation status to mark `v1.0.0` as published with release
  assets and checksums.

### Security

- Completed risk-based review for the accumulated Gradle parser, path, evidence, and
  output changes with no release-blocking findings remaining.
- Kept Gradle support local-only and static: the analyzer does not execute Gradle,
  invoke wrappers, use the Gradle Tooling API, evaluate build scripts, resolve plugins
  or dependencies, fetch remote metadata, follow symlinks, or serialize raw build-script
  bodies, dependency blocks, plugin configuration, credentials, tokens, local absolute
  paths, or generated output contents.

### Not Included

- Gradle execution, dynamic buildscript evaluation, Gradle Tooling API usage, wrapper or
  daemon execution, plugin/dependency/task/repository resolution, effective Gradle model
  reconstruction, custom `sourceSets` root emission, `projectDir` remapping,
  `includeBuild`, `includeFlat`, Kotlin source analysis, generated-source scanning by
  default, connectors, network access, telemetry, package-manager publication, SaaS,
  web UI, repository chat, generic RAG, LLM calls in the core analyzer, or automatic
  code modification.

## [1.0.0] - 2026-06-12

### Added

- Added a v1.0 evaluation corpus summary for packaged CLI scans over pinned
  representative Java/Spring Maven targets, covering deterministic output stability,
  `schema_version: "1.0"`, evidence reference integrity, normalized paths, module and
  warning references, document/code separation, and configured local Markdown volume.
- Added v1.0.0 release notes covering compatibility, migration, validation, security
  boundaries, not-included scope, and expected release assets.

### Changed

- Updated normal generated `project-map.json` output to `schema_version: "1.0"` as a
  schema marker and compatibility-policy migration, preserving the current v0.9 output
  shape, scan metadata, and evidence semantics without adding analyzer capability or
  changing `evidence-index.jsonl` behavior.
- Documented conservative v1.0 compatibility and migration expectations for
  `project-map.json`, `evidence-index.jsonl`, `endpoints.md`, and `agent-guide.md`,
  including the v0.9-to-v1.0 schema marker migration, JSON/JSONL field and semantic
  stability, Markdown presentation expectations, and future breaking-change,
  deprecation, and migration-note requirements.
- Aligned the Maven project version, README local build examples, roadmap status, and
  release notes for the `v1.0.0` release materials.

### Security

- Bounded Maven POM/root build-file ingestion at 1 MiB for module discovery, Maven
  metadata, dependency, plugin, and root build-file evidence paths; oversized POM inputs
  are skipped with deterministic `scan.diagnostics` warnings instead of being fully
  materialized.
- Bounded Java source discovery, parsing, source-line loading, and aggregate Java source
  workload for Spring MVC, component, JPA, Spring application surface, warning, test
  inventory, and Spring Boot application analyzers; oversized, symlinked/unreadable, or
  pathological Java files are skipped with deterministic `scan.diagnostics` warnings.
- Bounded pre-materialization candidate retention for resource config file discovery,
  OpenAPI/Swagger spec discovery, and local Markdown discovery so large path sets are
  truncated deterministically before fact or evidence materialization; local Markdown
  truncation reuses the existing document count cap diagnostic.

### Not Included

- Connectors, network access, telemetry, package-manager publication, SaaS, web UI,
  repository chat, generic RAG, LLM calls in the core analyzer, generated-source
  scanning by default, or automatic code modification.

## [0.9.0] - 2026-06-11

### Added

- Added the planned v0.9 CLI/config contract design boundary, including root-local
  config discovery, config precedence, repository-relative include/exclude semantics,
  safe local Markdown defaults, reserved optional scan-mode toggles, stable help/version
  and exit-code behavior, bounded scan diagnostics, redacted scan metadata output, and
  the no-tool-config-evidence decision.
- Added the first v0.9 config parser and safe-defaults implementation slice, including
  root-local `agent-project-memory.yml` discovery, optional explicit
  `scan <path> --config <repo-relative-yaml>` selection, required `version: 1` YAML
  schema validation, safe local Markdown defaults, local Markdown-only user
  include/exclude refinement, non-overridable built-in safety exclusions, reserved
  generated-source and symlink-following mode rejection, and redacted `scan` metadata
  without raw config values or raw user path patterns.
- Added v0.9 CLI help/version behavior, including top-level help, `help`, `scan --help`,
  top-level version, `version`, stable exit codes, bounded command validation, concise
  scan summaries, and packaged CLI validation for help/version commands.
- Added public release artifact discipline for packaged CLI verification and
  `SHA256SUMS` verification, while keeping publication authority manual.
- Added v0.9 installation-options guidance that keeps the v1.0 minimum installation path
  on GitHub Release executable jars with `SHA256SUMS`, while parking shell wrappers,
  JBang catalogs, Homebrew taps, Maven Central publication, SDKMAN/asdf plugins, native
  images, and container images as future distribution channels.
- Added a v0.9 packaged CLI/config/performance validation summary covering default scan
  behavior, safe config include/exclude behavior, disabled local Markdown behavior,
  invalid config exit codes, help/version behavior, deterministic output stability, and
  bounded local performance observations.

### Changed

- Updated generated `project-map.json` output to `schema_version: "0.9"` for the
  config parser and safe-defaults slice, adding top-level redacted `scan` metadata while
  preserving existing evidence semantics and keeping the tool config file out of
  `evidence-index.jsonl`.
- Split CLI failure modes into documented exit codes for usage, scan input, invalid
  config, output/write, and unexpected internal errors while keeping stderr bounded and
  stack-trace-free by default.
- Extended `scan.diagnostics` and the concise CLI diagnostic summary to report bounded
  non-fatal warnings when aggregate local Markdown caps are reached.
- Aligned release README usage, roadmap status, release notes, output contract
  wording, changelog, and Maven project version for `v0.9.0`.

### Fixed

- Updated post-release documentation status to mark `v0.9.0` as published with release
  assets and checksums.

### Security

- Added deterministic aggregate caps for local Markdown discovery and reconciliation:
  accepted document count, accepted Markdown bytes, heading references, chunk references,
  reconciliation mention observations, and emitted reconciliation rows are now bounded,
  with skipped document-backed output represented by diagnostics rather than evidence.
- Completed release security review follow-up with bounded hardening for
  recursive scan config path-rule matching, local Markdown aggregate limits,
  OpenAPI/warning traversal, generated-source warning POM reads, and stable no-follow
  spec/POM/root build-file reads.

### Not Included

- Connectors, network access, telemetry, update checks, plugin loading, package-manager
  publication, global machine config, generated-source scanning by default, symlink
  following, SaaS, web UI, repository chat, generic RAG, LLM calls in the core analyzer,
  or automatic code modification.

## [0.8.0] - 2026-06-11

### Added

- Added the planned v0.8 local Markdown/document ingestion contract design boundary,
  including conservative default discovery scope, path and symlink rules, document
  inventory shape, heading and chunk references, `document` evidence semantics,
  reconciliation signal taxonomy, guide-rendering expectations, validation boundaries,
  and explicit non-goals for external docs, generic RAG, LLM-core, and document claims
  overriding code facts.
- Added the first v0.8 local Markdown discovery implementation slice, with deterministic
  default-scope document inventory, repository-relative in-root path normalization,
  default exclusions for hidden/private/generated/dependency/output/maintainer-like
  paths, and a default no-symlink-following policy.
- Added deterministic v0.8 ATX Markdown structure extraction for accepted default-scope
  local Markdown documents, including bounded heading references, bounded chunk
  references, stable path-scoped IDs, line ranges, nearest owning heading links, and
  `content_status: "not_serialized"` without document body serialization.
- Added v0.8 local Markdown `document` evidence records for accepted file, heading, and
  chunk observations, with resolving `documents.items[].evidence_ids`,
  `documents.items[].headings[].evidence_ids`, and
  `documents.items[].chunks[].evidence_ids` while keeping excerpts bounded to path,
  normalized heading, or chunk-boundary observations.
- Added conservative v0.8 `documents.reconciliation` signals for bounded local Markdown
  endpoint-like path mentions, module-like references, and source-backed API/module
  facts without an obvious accepted-document mention. Reconciliation rows are emitted
  only as low-confidence uncertain inspection hints, with resolving document-side or
  source-backed evidence, and without stale-document, completeness, coverage, or
  documentation-quality claims.
- Added compact v0.8 local project documentation rendering to `agent-guide.md` from
  structured `documents` facts and resolving evidence only. The guide renders document
  inventory, module ownership, bounded heading/chunk navigation references, discovery
  policy facts, and `documents.reconciliation` rows as uncertain inspection hints
  without document prose summaries, chunk bodies, stale-document truth claims,
  completeness claims, documentation-quality scoring, or document-backed facts
  overriding code-backed facts.
- Added a v0.8 real-project evaluation summary for pinned Java/Spring Maven targets,
  covering local Markdown discovery, heading/chunk navigation references, `document`
  evidence, reconciliation hint behavior, guide size/noise, determinism, evidence
  integrity, document/code separation, and the sensitive-content boundary.

### Changed

- Updated generated `project-map.json` output to `schema_version: "0.8"` for the
  local Markdown discovery and structure slice, including top-level `documents`
  discovery policy metadata, document inventory, ATX heading references, and bounded
  chunk references with resolving document evidence and conservative
  `documents.reconciliation` hints plus compact local-document guide rendering from
  structured document facts and evidence only.
- Aligned release README usage, roadmap status, release notes, changelog, output
  contract wording, and Maven project version for `v0.8.0`.

### Fixed

- Updated post-release documentation status to mark `v0.7.0` as published with release
  assets and checksums.

### Security

- Completed the v0.8 real-project evaluation and read-only security/contract audit with
  no reportable findings or release-blocking follow-up.

### Not Included

- PDF, Word, external documentation, connectors, network access, embeddings, vector
  stores, generic RAG, repository chat, AI-generated document summaries, or LLM calls in
  the core analyzer.
- Document body serialization, document prose summaries, stale-document truth claims,
  documentation completeness or coverage claims, documentation-quality scoring, or
  document claims promoted to source-backed code facts.
- SaaS, web UI, or automatic code modification.

## [0.7.0] - 2026-06-10

### Added

- Added the v0.7 tests, quality, and change-risk contract design boundary,
  including source-visible test class and method inventory, Spring test slice signals,
  conservative tested-subject relation statuses, inferred/uncertain test-gap and
  change-risk planning hints, evidence semantics, and explicit non-goals for coverage,
  assertion, CI, runtime, and call-graph claims.
- Added the first v0.7 tests inventory implementation slice, with stable test fact IDs,
  module-owned source-visible test facts, bounded JUnit Jupiter/JUnit 4 test method
  inventory for directly visible supported annotations, direct JUnit/Spring Test
  framework signal classifications, and evidence-backed `agent-guide.md` rendering.
- Added source-visible Spring test slice and mock annotation signals to the v0.7 tests
  inventory, including direct `@SpringBootTest`, `@WebMvcTest`, `@DataJpaTest`,
  `@ContextConfiguration`, `@MockBean`, and `@SpyBean` observations with evidence-backed
  `project-map.json` and `agent-guide.md` output.
- Added conservative tested-subject relation/status rows for supported naming
  conventions, exact production imports, direct test field types, and direct Spring test
  slice class literals, with explicit inferred, not-detected, ambiguous, and unsupported
  statuses.
- Added conservative v0.7 quality planning hints under the top-level `quality` object,
  including `no_obvious_test` test-gap signals for selected endpoint, repository, and
  entity surfaces, plus warning-oriented or uncertain change-risk signals for existing
  Spring application, security, repository/entity, and JPA relationship facts.
- Added a v0.7 real-project evaluation summary for pinned Java/Spring Maven targets,
  covering source-visible test inventory accuracy, tested-subject inference quality,
  quality planning-hint noise, determinism, and evidence/reference consistency.

### Changed

- Updated generated `project-map.json` output to `schema_version: "0.7"` for the bounded
  tests inventory refinement, including `tests.items[].id`,
  `framework_signals[].signal_kind`, and `tests.items[].methods[]` while continuing to
  avoid coverage, CI, assertion, runtime Spring context, and test execution claims.
- Expanded generated v0.7 `tests.items[]` output with `spring_test_slices[]` and
  `mock_signals[]` while keeping Spring test slice annotations as source-visible signals
  only and continuing to avoid runtime Spring test context reconstruction, Mockito
  behavior claims, coverage, CI, assertion, and test execution claims.
- Expanded generated v0.7 `tests.items[].tested_subjects[]` rows with
  `relation_status`, `relation_type`, nullable target/candidate fields, confidence, and
  uncertainty while continuing to avoid coverage, CI, assertion, runtime execution, full
  call graph, and complete subject-mapping claims.
- Expanded generated v0.7 `project-map.json` and `agent-guide.md` output with
  evidence-backed quality planning hints while continuing to avoid coverage, test
  execution, assertion, CI, runtime, correctness, vulnerability, production-impact,
  business-priority, and complete subject-mapping claims.
- Aligned release README usage, roadmap status, release notes, changelog, and Maven
  project version for `v0.7.0`.

### Fixed

- Updated post-release documentation status to mark `v0.6.0` as published with release
  assets and checksums.

### Security

- Completed the v0.7 real-project evaluation and read-only security/contract audit with
  no release-blocking or bounded-fix findings.
- Kept v0.7 tests, tested-subject, and quality/change-risk output local-first,
  evidence-backed, source-visible/inferred/uncertain where appropriate, and without
  coverage, runtime execution, CI status, assertion, correctness, vulnerability, or
  production-impact claims.

### Not Included

- Coverage analysis, mutation testing, behavioral assertion understanding, CI result
  claims, runtime test execution, runtime Spring test context reconstruction, runtime
  repository or database verification, full call graph reconstruction, or complete
  tested-subject mapping.
- SaaS, connectors, repository chat, generic RAG, web UI, LLM calls in the core
  analyzer, or automatic code modification.

## [0.6.0] - 2026-06-09

### Added

- Added the planned v0.6 JPA/domain contract design boundary, including source-visible
  entity field metadata, relationship metadata, embedded and identifier support,
  inferred repository/entity relation semantics, guide-rendering expectations, evidence
  semantics, and explicit runtime ORM/database non-goals.
- Added the first v0.6 JPA/domain implementation slice for direct source-visible entity
  field annotation extraction, including field-level `@Column`, `@Enumerated`,
  `@GeneratedValue`, and `@Version` metadata with evidence-backed output and conservative
  getter/property-access skips.
- Added the v0.6 embedded and identifier model implementation slice for direct
  source-visible `@Embeddable` classes, direct field-level `@Embedded` and
  `@EmbeddedId` signals, and direct class-level `@IdClass` composite-id signals with
  explicit partial/not-analyzed semantics and evidence-backed output.
- Added the v0.6 relationship metadata deepening slice for direct field-level
  relationship cardinality, direct string-literal `mappedBy`, bounded direct
  `@JoinColumn` and `@JoinTable` metadata, and direct source-visible relationship
  `optional`, `fetch`, `cascade`, and `orphanRemoval` attributes without ORM runtime,
  database, ownership-correctness, foreign-key, join-table-existence, fetch-behavior, or
  cascade-behavior claims.
- Added the v0.6 repository/entity inferred relation slice for source-visible Spring
  Data repository interface generic types, linking to an emitted entity fact only when
  the target is unique and representing missing, ambiguous, raw, wildcard, nested, or
  otherwise unsupported generic shapes with explicit relation statuses.

### Changed

- Updated generated `project-map.json` output to `schema_version: "0.6"` for the bounded
  JPA field annotation slice, with `entities.items[].fields[]`, `identifier_kind`, and
  nullable `generated_value` identifier metadata while keeping missing annotation
  attributes distinct from runtime JPA defaults.
- Expanded generated `project-map.json` and `agent-guide.md` JPA/domain output with
  `entities.embeddables`, nullable `entity.id_class`, nullable `field.embedded`, and
  `identifier_kind: "embedded_id"` while keeping embedded target links conservative and
  composite-id semantics not analyzed.
- Expanded generated `project-map.json` relationship entries with `cardinality`, a
  nested uncertain `target` object, `mapped_by`, `ownership_signal`, direct relationship
  attribute fields, `join_columns`, and nullable `join_table`; relationship targets
  remain declared-type-only and uncertain in this slice.
- Expanded generated `spring_application_surface.repositories.items[]` Spring Data
  interface signal entries with `entity_relation_status` values and nullable
  `entity_relation` objects for conservative inferred repository/entity generic links,
  and updated `agent-guide.md` to render those links as inferred source-visible
  relations rather than runtime repository or database facts.
- Refined v0.6 `agent-guide.md` domain/data rendering to state source-visible JPA and
  Spring Data generic boundaries only when domain facts are present, and to show inferred
  repository/entity relation status and uncertainty without turning JSON `null`
  uncertainty into a source-attribute placeholder.
- Quieted no-domain `agent-guide.md` rendering by omitting the empty
  `Detected JPA Entities` section and the persistence inspection-order step when no
  entity, embeddable, relationship, or inferred repository/entity relation facts are
  present.
- Reduced the public documentation surface to product, contract, release, evaluation
  summary, and review-risk summary documents.
- Aligned release README usage, roadmap status, release notes, changelog, and Maven
  project version for `v0.6.0`.

### Fixed

- Added safe v0.6 JPA/domain origin support for explicit `jakarta.persistence.*` and
  `javax.persistence.*` wildcard imports for the existing supported JPA annotation and
  enum-attribute slice, while preserving source-declared and same-package fake
  annotation protections.
- Updated post-release documentation status to mark `v0.5.0` as published with release
  assets and checksums.

### Security

- Completed v0.6 risk-based implementation and follow-up review gates with no
  release-blocking findings.
- Kept v0.6 JPA/domain output local-first and evidence-backed, without database
  introspection, runtime Hibernate metadata, DDL reconstruction, JPQL semantic parsing,
  migration interpretation, runtime repository/entity verification, or provider-default
  claims.

### Not Included

- Getter/property-access JPA annotation extraction.
- Full composite-key semantic reconstruction.
- Relationship target entity linking beyond declared unresolved targets.
- Database schema reconstruction, database introspection, runtime Hibernate/JPA metadata,
  DDL reconstruction, JPQL semantic parsing, or migration interpretation.
- Runtime Spring Data repository registration, query-method behavior, database access,
  or repository/entity verification.
- Additional real-project corpus coverage for embeddables, embedded IDs, id-class
  signals, enumerated fields, and version fields.
- SaaS, connectors, repository chat, generic RAG, web UI, LLM calls in the core
  analyzer, or automatic code modification.

## [0.5.0] - 2026-06-08

### Added

- Added the public v0.5 deeper Spring application surface design boundary, including
  taxonomy, output contract direction, evidence semantics, non-goals, future-work
  boundaries, and risk-review expectations.
- Added the v0.5 repository signal analyzer, including direct
  source-visible `@Repository` repository stereotype facts and inferred source-visible
  Spring Data repository interface extension signals.
- Added the v0.5 configuration surface analyzer, including
  direct source-visible `@Configuration` class facts, direct source-visible
  `@ConfigurationProperties` type facts with `binding_status: "not_analyzed"`, and
  direct source-visible `@Bean` method facts with `bean_name_status: "not_analyzed"`.
- Added the v0.5 behavior and messaging signal analyzer,
  including direct source-visible `@Transactional` type and method facts, direct
  source-visible `@Scheduled` method facts, direct source-visible `@EventListener`
  method facts, and direct source-visible Kafka/Rabbit listener annotation signals
  without runtime transaction, scheduling, event delivery, or messaging topology claims.
- Added the v0.5 Spring Security configuration warning analyzer,
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
- Added concise module-grouped Spring Application Surface guidance in `agent-guide.md`
  that separates extracted facts, inferred signals, explicit not-analyzed statuses, and
  warnings while keeping direct repository annotation observations, inferred Spring Data
  interface signals, repository-to-entity non-analysis, configuration, bean,
  configuration-properties, transaction, scheduled, event listener, messaging listener,
  and Spring Security warning semantics distinct without runtime bean graph, binding,
  behavior, topology, or security policy claims.
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

### Security

- Completed the v0.5 implementation-range review and risk-based security assessment
  with no release-blocking findings.
- Kept Spring Security configuration output as warning/change-risk signals only, without
  endpoint protection, authentication, authorization, filter-chain ordering,
  vulnerability, or correctness claims.
- Resolved the release evidence-excerpt question: bounded source annotation
  evidence excerpts for `@ConfigurationProperties` and inherited test annotations remain
  acceptable for v0.5 because they are bounded, source-local evidence excerpts and do
  not serialize structured `prefix`/`value` fields, config file contents, environment
  values, decrypted values, or secret-looking values in `project-map.json`.

### Not Included

- Runtime Spring bean graph reconstruction.
- Autowiring graph reconstruction.
- Runtime conditional evaluation or Spring Boot auto-configuration reconstruction.
- Runtime configuration binding, active profile, or property value extraction.
- Repository query-method semantic analysis or repository-to-entity relation claims.
- Transaction propagation, scheduler registration, event delivery, or messaging topology
  reconstruction.
- Spring Security policy, endpoint protection, authentication, authorization, filter
  chain ordering, vulnerability, or correctness analysis.
- SaaS, connectors, repository chat, generic RAG, web UI, LLM calls in the core
  analyzer, or automatic code modification.

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

- Completed the v0.4 implementation-range review and risk-based security assessment
  with no release-blocking findings.
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

- Added public v0.3 build/configuration contract documentation.
- Added v0.3 module-owned source-visible Maven metadata extraction for direct
  module `groupId`, `artifactId`, `version`, `packaging`, and parent coordinates.
- Added v0.3 module-owned source-visible Maven dependency inventory for direct
  dependencies and separate dependency-management declarations.
- Added v0.3 module-owned source-visible Maven plugin inventory for direct
  plugins, separate plugin-management declarations, bounded direct execution/configuration
  signals, and conservative generated-source warnings.
- Added v0.3 module-owned standard resource-root discovery and path-only
  application/logging config-file inventory with config-file evidence that does not
  include config contents.
- Added v0.3 module-owned direct source-visible Spring Boot application signal
  extraction for `@SpringBootApplication` classes and bounded source-visible `main`
  method signals.

### Changed

- Documented `schema_version: "0.3"` build/configuration output and evidence
  contract decisions for source-visible Maven, resource, config-file, Spring Boot, and
  generated-source warning signals.
- Updated public output to `schema_version: "0.3"` with a complete `build_config` shell;
  v0.3 build/config subsections use explicit `analysis_status` values and do not claim
  effective, resolved, runtime, or generated behavior.
- Changed Maven `dependencies` and `dependency_management` build/config subsections from
  placeholders to analyzed source-visible inventories.
- Changed Maven `plugins` and `plugin_management` build/config subsections from
  placeholders to analyzed source-visible inventories.
- Changed `resources` and `config_files` build/config subsections from placeholders to
  analyzed path inventories when standard resource roots are present.
- Changed `spring_boot_applications` from a placeholder to analyzed
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
- Completed the final v0.3 release security baseline after the bounded-excerpt fix with
  no release-blocking findings.

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
- Added public v0.2 module-aware Maven documentation.
- Documented v0.2 module-aware output and evidence contract decisions.
- Added public `schema_version: "0.2"` project-map output with `project.modules`,
  compatibility root summaries, direct `module_id` on module-owned facts, and
  Maven module warnings.
- Added module-aware execution for the existing endpoint, component, JPA entity, hidden
  HTTP surface warning, and tests inventory analyzers.
- Added module-aware `endpoints.md` grouping and `agent-guide.md` orientation based on
  `project.modules` and fact-level `module_id` values.
- Added a real-project v0.2 evaluation summary for pinned Maven multi-module
  Java/Spring projects.
- Added v0.2 release notes.
- Adopted a public changelog.
- Added public release process and versioning discipline documentation.

### Changed

- Reorganized the public roadmap from historical v0.1 stages into post-v0.1 release
  tracks.
- Clarified v0.2 module-aware schema atomicity, warning analysis status, and
  Maven module warning ID/sort semantics.
- Moved normal generated `project-map.json` output from the v0.1 single-module contract
  to the atomic v0.2 module-aware JSON boundary.
- Clarified release authority and changelog expectations in contributor documentation.
- Clarified public agent boundaries, issue/PR scope wording, development versioning, and
  development-slice versus release commit expectations.
- Aligned the Maven project version and README artifact references with the intended
  `0.2.0` release artifact.

### Security

- Added v0.2 risk-based review requirements for the implementation range and
  release-candidate security baseline.
- Fixed unsafe `.project-memory` symlink handling so scan output directories and
  generated output file targets must stay under the canonical scan root.
- Fixed hardlinked generated output targets so scans cannot overwrite outside aliases
  through pre-existing multi-link `.project-memory` files.
- Completed the final v0.2 release-candidate security baseline fixes, with no
  remaining release-blocking findings.
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
- Added Stage 8 evaluation summaries for pinned open-source Spring projects.

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
