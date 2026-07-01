# v3.2.0 Release Notes

Release date: 2026-07-01

Release status: release candidate. The `v3.2.0` tag, GitHub Release, executable jar,
and `SHA256SUMS` assets are not published yet.

`agent-project-memory` v3.2.0 is a generated presentation and agent handoff maturity
release. It improves deterministic `agent-guide.md`, selected agent profile Markdown,
and `agent-context` wording while keeping JSON/JSONL schemas, evidence semantics,
adapter behavior, query authority, and distribution channels unchanged.

## Highlights

- Front-loads `agent-guide.md` with a compact first-pass reading path, trust legend,
  practical inspection order, project-memory overview, large-artifact notice when
  applicable, uncertainty snapshot, and absent-surface summary before detailed
  inventories.
- Labels generated Markdown, query output, `agent-context`, profiles, AI presentation,
  cache, workspace, adapter, release, and downstream-agent surfaces as non-evidence
  presentation, navigation, provenance, or execution metadata unless a later public
  contract changes that boundary.
- Adds deterministic large-section summaries and presentation-only row caps for large
  `Detected Tests`, `Quality And Change-Risk Signals`, `Spring Application Surface`,
  and `Domain And Data Model` guide sections.
- Keeps displayed rows evidence-visible where evidence exists and points omitted-row
  notices back to complete generated facts in `project-map.json`.
- Keeps `evidence-index.jsonl` as the authoritative source-backed evidence artifact.

## Output And Evidence Compatibility

v3.2.0 does not change generated JSON or JSONL schema markers:

- no-adapter `project-map.json` remains on `schema_version: "1.0"`;
- adapter-enabled `project-map.json` remains on `schema_version: "2.0"` with
  `source-registry.json` when an explicitly enabled adapter accepts input;
- `artifact-set.json` remains on `artifact_set_schema_version: "1.0"`;
- `project-graph.json`, `workspace-map.json`, cache, profile, and AI presentation
  surfaces keep their current documented schema markers and semantics;
- `evidence-index.jsonl` keeps its existing field set and remains the source-backed
  evidence artifact.

The compatibility impact is limited to deterministic human-readable presentation
surfaces. Consumers that parse generated Markdown as an undocumented machine interface
should update that parsing or use the documented JSON/JSONL artifacts instead.

Recommended migration action:

- regenerate `.project-memory/` outputs with the v3.2.0 build;
- use `artifact-set.json` first for generated artifact inventory and evidence-authority
  labels;
- use `project-map.json` and `evidence-index.jsonl` for complete fact and evidence
  lookup when a guide row is summarized or omitted for readability;
- continue treating generated Markdown, query output, profile output, and
  `agent-context` output as deterministic presentation rather than evidence.

## Security Notes

v3.2.0 keeps the local-first security boundary:

- no source upload by default;
- no network connector defaults;
- no credential lookup or storage;
- no real provider AI;
- no server/API/MCP/editor/plugin runtime;
- no repository chat, generic RAG, or automatic code modification;
- no signing, SBOM publication, package-manager channel, native image, container image,
  release automation, artifact upload, tag creation, or GitHub Release publication.

The generated presentation changes do not make the tool a vulnerability scanner,
complete secret detector, secret inventory, security correctness engine,
dependency-freshness proof, or security certification.

Release preparation remains separate from publication. Tags, GitHub Releases, asset
uploads, checksum publication, signing, SBOM publication, package-manager publication,
native image publication, container image publication, and release automation require
separate explicit maintainer approval and are not part of this release candidate.

## Validation

The v3.2.0 release-candidate validation passed:

- Full local Maven test suite: 603 tests, 0 failures, 0 errors, 0 skipped.
- Maven package build: 603 tests, 0 failures, 0 errors, 0 skipped, including the
  packaged CLI smoke bound to the Maven package lifecycle.
- Packaged CLI smoke generated `project-map.json`, `artifact-set.json`,
  `project-graph.json`, `evidence-index.jsonl`, `endpoints.md`, and `agent-guide.md`
  for the fixture project.
- Packaged CLI `--version` reported `agent-project-memory 3.2.0`.
- Packaged jar metadata inspection confirmed the CLI manifest entrypoint and Maven
  artifact metadata for `3.2.0`.
- The local artifact-integrity dry-run generated a clean dry-run asset directory with
  exactly `agent-project-memory-3.2.0.jar` and `SHA256SUMS`.
- `SHA256SUMS` used the release asset filename only and verified successfully for
  `agent-project-memory-3.2.0.jar`.
- Whitespace checks and public text marker checks passed for the release-prep diff.

## Not Included

v3.2.0 does not add:

- `project-map.json` `schema_version: "3.0"`, v3-only project-map serialization, or
  v3-only query behavior.
- Evidence field changes, evidence type changes, adapter-backed evidence, or generated
  Markdown/query/profile/agent-context authority as evidence.
- Adapter-aware query support, live network connectors, connector credentials,
  credential lookup/storage, background sync, remote cache, provider discovery,
  pagination/retry/rate-limit behavior, or remote freshness claims.
- Provider AI, real AI provider modes, source upload, embeddings, vector search,
  repository chat, generic RAG, SaaS, or automatic code modification.
- Server/API/MCP/editor/plugin runtime, plugin loading, plugin marketplace, socket
  listener, daemon, public API service, or hosted service mode.
- Signing, signatures, signing keys, signing verification instructions, SBOM generation
  or publication, package-manager channels, first-party installed-command distribution,
  JBang catalogs, Homebrew taps, Maven Central publication, SDKMAN/asdf plugins, native
  images, container images, dependency workflow automation, CI release workflow changes,
  release automation, automatic tag creation, automatic GitHub Release creation,
  artifact upload, checksum publication, or package publication.

## Release Assets

The intended `v3.2.0` release assets are:

- `agent-project-memory-3.2.0.jar`
- `SHA256SUMS`

These assets are not published yet. Publication requires the separate release
publication workflow after release-prep review.
