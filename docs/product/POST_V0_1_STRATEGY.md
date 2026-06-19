# Post-v0.1 Strategy

This document describes the public post-v0.1 direction for `agent-project-memory`.
It is a strategy guide, not a release commitment. Concrete behavior changes remain
governed by `docs/product/ROADMAP.md`, `docs/architecture/OUTPUT_CONTRACT.md`,
`docs/architecture/EVIDENCE_MODEL.md`, tests, and release notes.

## Strategic Thesis

`agent-project-memory` should grow as a deterministic local project-memory engine for
Java/Spring codebases and AI coding agents.

The project should not become a generic AI documentation generator, repository chatbot,
hosted RAG system, or SaaS scanner. Its core value is evidence-backed memory:

- source-visible facts are extracted deterministically;
- important facts reference evidence;
- inferred relations are labeled as inferred;
- uncertain signals are labeled as uncertain;
- document-backed claims remain distinct from code-backed facts;
- optional AI can explain or present facts later, but cannot become evidence.

## Product Invariants

- The core analyzer runs locally.
- Source code is not uploaded by default.
- The core analyzer does not require external APIs.
- LLM-generated text is never authoritative evidence.
- Optional AI output is presentation only: it may explain, group, or summarize
  deterministic memory, but it must not create project facts, evidence records,
  connector truth, security findings, vulnerability proof, or code changes.
- Output contracts are explicit and versioned.
- Evidence semantics are explicit and documented.
- New analyzer behavior has focused tests.
- Meaningful analyzer expansions are evaluated on pinned real projects.
- Future connectors are adapters around the core, not dependencies inside it.

## Development Order

Future work should build from the inside out:

1. Project structure truth.
2. Source-visible application facts.
3. Declared and generated API surface.
4. Local project documents.
5. Tests, quality, and change-risk intelligence.
6. CLI, config, performance, and distribution maturity.
7. Optional adapters and connectors.
8. Optional AI presentation and agent integrations.
9. Platform/plugin ecosystem.

This order keeps external integrations and AI presentation dependent on a strong
deterministic project model, not the other way around.

## Public And Local Planning

Public documents should stay concise and useful for users and contributors:

- `README.md` explains the product and current usage.
- `docs/product/MVP_SPEC.md` defines the v0.1 baseline.
- `docs/product/ROADMAP.md` describes release tracks and future direction.
- release notes summarize shipped public behavior and compatibility notes.
- public evaluation summaries record concise validation outcomes for pinned projects.
- `docs/architecture/OUTPUT_CONTRACT.md` and `docs/architecture/EVIDENCE_MODEL.md`
  govern generated output and evidence semantics.

Maintainer-only planning can be more detailed and may live in ignored local files that
are not part of the public documentation set.

## v0.x Direction

The v0.x line should harden the local Java/Spring analyzer before declaring a stable
public contract:

- v0.2: module-aware Maven support.
- v0.3: build and configuration model.
- v0.4: declared and generated API surface taxonomy.
- v0.5: deeper Spring application surface.
- v0.6: deeper JPA/domain model.
- v0.7: tests, quality, and change-risk map.
- v0.8: local Markdown/document ingestion.
- v0.9: CLI/config/performance/distribution readiness.

The exact sequence may change when evaluation finds higher-priority correctness or
contract work.

## v1.0 Direction

v1.0 should mean that Java/Spring Maven project memory is stable enough for public use:

- output compatibility rules are clear;
- evidence semantics are stable;
- generated facts are deterministic and evidence-backed;
- evaluation coverage is broad enough to trust common Java/Spring shapes;
- CLI UX and installation are acceptable;
- release, changelog, and review discipline are in place.

## v2.x And v3.x Direction

v2.x may introduce adapter APIs, connector provenance, local imports, optional AI
presentation, and read-only agent integrations. These layers must remain optional and
non-authoritative.

The initial v2 adapter platform should preserve no-adapter scans as the compatibility
baseline: the core analyzer, query layer, and normal generated artifact set continue to
work without adapter configuration, network access, credentials, plugin loading,
provider use, or source upload. Adapter-enabled output should keep normalized source
documents and provenance in a separate source registry, use explicit v2 schema markers
when adapter-backed `project-map.json` sections are added, and keep adapter-backed
records as provenance-backed external/document context rather than code-backed facts or
project evidence. Downstream consumers should treat adapter-enabled output as a v2
artifact set, regenerate it as one set, and keep using no-adapter v1 artifacts for query
workflows until adapter-aware query behavior is separately documented.

The safest implementation order is adapter contract foundation first, then
disabled-by-default configuration and local path safety, then a local structured import
reference adapter. Networked connectors, credential handling, plugin loading,
provider-backed AI, public API/server modes, and broad local document imports require
separate later design and review.

After the v2 adapter foundation exists, the next GitHub/GitLab import step should stay
local export first. A normalized local JSON export for Git hosting issue,
pull-request, and merge-request records can exercise provider-specific provenance
without making the core analyzer depend on Git hosting services. These records should
remain source-registry provenance and `adapter_context` rows only; they should not enter
`evidence-index.jsonl`, become Java/Spring facts, drive normal query behavior, or imply
that remote issue/PR/MR text is current source truth. Live API mode, credentials,
background sync, remote cache, and adapter-aware query support remain separate later
design decisions.

After the Git hosting local export boundary, Jira, YouTrack, and Confluence connectors
should follow the same local export first pattern. A normalized local JSON export can
carry issue, article, and page records as provenance-backed external/document context
without making the core analyzer depend on project-management or wiki services. These
records should reuse the source-registry and `adapter_context` placement, remain out of
`evidence-index.jsonl`, and avoid raw body, comment, attachment, credential, configured
path, and local absolute path serialization by default. Live API mode, credentials,
provider discovery, pagination, retry/rate-limit behavior, background sync, remote
cache, adapter-aware query support, source/spec agreement scoring, documentation
freshness scoring, and connector-driven change-impact behavior remain separate later
design decisions.

Optional AI in v2.x is a presentation boundary, not an analysis boundary. Its allowed
inputs are deterministic generated memory, existing evidence references, and bounded
non-evidence metadata such as graph derivation, query/source-artifact metadata, profile
metadata, cache status summaries, and adapter provenance after those surfaces are
designed. It must preserve labels for code-backed facts, spec-backed declared
operations, document-backed observations, adapter-backed records, inferred relations,
uncertain signals, warnings, and not-analyzed areas.

The first AI presentation surface is a separate optional
`.project-memory/ai-presentations/` artifact set, not a profile extension, query mode,
or project-map section. A normal scan keeps working without AI and does not emit AI
artifacts unless `scan --ai-presentation mock_no_network` is explicitly selected.
Downstream consumers that do not understand AI presentation should be able to ignore
that optional directory.

AI output must be labeled as non-evidence whenever it is emitted, with both visible
human-readable wording and machine-readable metadata. It must not write
`project-map.json`, `evidence-index.jsonl`, `source-registry.json`, source files,
repository docs, root instruction files, configuration files, cache metadata, profile
artifacts, or deterministic query outputs; it must not create facts, evidence records,
connector truth, security findings, vulnerability proof, runtime claims, source/spec
agreement claims, documentation-freshness claims, release evidence, or automatic code
modifications. Provider use, network access, credentials, telemetry, retention/privacy
claims, prompt transcript serialization, and source upload must stay disabled by default
and require a later explicit design, tests, review, and documentation before
implementation.

The only accepted first provider mode is a mock/no-network mode for local contract and
test coverage. Real provider modes remain parked until a separate design defines
explicit enablement, contacted service, request minimization, credential policy,
network behavior, prompt/content-injection controls, diagnostics, retention wording, and
release review requirements.

Read-only agent consumption should start from the existing local CLI and deterministic
generated artifacts rather than from a server. The first v2.4 integration boundary is a
CLI-only `query` surface that can render a bounded agent-context view from existing
project-memory artifacts, existing evidence references, and existing graph navigation
metadata. It must not read repository source files to expand evidence, mutate
generated artifacts, write repository files, open a socket, call a provider, use
credentials, upload source, serialize raw prompt transcripts, or treat agent-facing
output as evidence. MCP, server, public API, editor plugin, network, authentication,
credential, telemetry, source-upload, and automatic code-modification behavior remain
parked until a later explicit design defines the transport, permission model,
filesystem scope, credential stance, logging/telemetry stance, and release review
requirements.

Workspace memory should start as an explicit local workspace artifact over configured
local repository or service roots, not as remote discovery, organization crawling,
repository chat, semantic search, or change-impact analysis. The planned first v2.5
boundary is an explicit `workspace scan <config>` workflow that treats the config file
directory as the workspace root, requires unique logical `repo_id` values for each
member, writes a separate workspace-root `.project-memory/workspace-map.json`, and
references per-repo evidence through composite `repo_id` plus existing `evidence_id`
keys without changing normal single-repo artifacts. Cross-repo relation emission,
workspace query, adapter-aware workspace context, and child-repo scan mutation remain
separate later implementation decisions unless a bounded v2.5 goal explicitly accepts
and documents them.

v3.0 is the long-term platform target: a local-first evidence-backed project memory
platform with deterministic analyzers, stable adapter/plugin APIs, optional AI
presentation, and agent-native workflows.

## Explicit Non-Goals

The post-v0.1 roadmap should continue to avoid:

- source upload by default;
- LLM calls in the core analyzer;
- treating LLM output as evidence;
- treating AI output as project facts, connector truth, security findings,
  vulnerability proof, or code modifications;
- SaaS-first positioning;
- repository chat as the core product;
- generic RAG as the core product;
- automatic code modification as the core product;
- broad multi-language expansion before the Java/Spring core is stable.
