# v3.3.0 Release Notes

Release date: 2026-07-02

Release status: published. The `v3.3.0` tag, GitHub Release, executable jar, and
`SHA256SUMS` assets are published.

`agent-project-memory` v3.3.0 is a validation and regression hardening release. It
adds focused package, workspace, and adapter-output regression coverage for the current
v3 generated artifact boundary while keeping product behavior, JSON/JSONL schema
markers, evidence semantics, adapter behavior, query authority, and distribution
channels unchanged.

## Highlights

- Adds package-phase smoke validation that requires generated
  `.project-memory/artifact-set.json` and verifies selected manifest and
  evidence-boundary fields.
- Adds static golden coverage for current `.project-memory/workspace-map.json`
  aggregation output, including workspace schema marker, logical repository identity,
  workspace-relative artifact paths, artifact schema summaries, sample evidence
  references, relation status, and diagnostics.
- Adds full-output golden coverage for the safe local structured import fixture,
  covering `project-map.json`, `project-graph.json`, `evidence-index.jsonl`,
  `endpoints.md`, and `agent-guide.md`.
- Confirms adapter context remains provenance-only external context with
  `source_document_ids` and `provenance_ids`, and does not promote adapter records to
  `evidence_ids`.

## Output And Evidence Compatibility

v3.3.0 does not change generated JSON or JSONL schema markers:

- no-adapter `project-map.json` remains on `schema_version: "1.0"`;
- adapter-enabled `project-map.json` remains on `schema_version: "2.0"` with
  `source-registry.json` when an explicitly enabled adapter accepts input;
- `artifact-set.json` remains on `artifact_set_schema_version: "1.0"`;
- `workspace-map.json` keeps its current schema marker and aggregation semantics;
- `evidence-index.jsonl` keeps its existing field set and remains the source-backed
  evidence artifact.

The compatibility impact is limited to stronger regression coverage around existing
outputs. Existing generated-output consumers do not need a migration for v3.3.0 beyond
the normal recommendation to regenerate `.project-memory/` outputs with the current
release.

## Security Notes

v3.3.0 keeps the local-first security boundary:

- no source upload by default;
- no network connector defaults;
- no credential lookup or storage;
- no real provider AI;
- no server/API/MCP/editor/plugin runtime;
- no repository chat, generic RAG, or automatic code modification;
- no signing, SBOM publication, package-manager channel, native image, container image,
  release automation, or automatic publication.

The validation hardening confirms existing non-evidence boundaries for generated
artifact metadata, workspace aggregation, adapter provenance, generated Markdown, graph
metadata, release metadata, and downstream-agent output. It does not make the tool a
vulnerability scanner, complete secret detector, secret inventory, security correctness
engine, dependency-freshness proof, or security certification.

Signing, SBOM publication, package-manager publication, native image publication,
container image publication, and release automation remain separate future work and are
not part of this release.

## Validation

The v3.3.0 release validation passed:

- Full local Maven test suite: 603 tests, 0 failures, 0 errors, 0 skipped.
- Maven package build: 603 tests, 0 failures, 0 errors, 0 skipped, including the
  packaged CLI smoke bound to the Maven package lifecycle.
- Packaged CLI smoke generated `project-map.json`, `artifact-set.json`,
  `project-graph.json`, `evidence-index.jsonl`, `endpoints.md`, and `agent-guide.md`
  for the fixture project.
- Package smoke validates generated `artifact-set.json` presence and selected
  manifest/evidence-boundary fields.
- Workspace-map golden coverage verifies the current workspace aggregation output
  contract.
- Local structured import full-output golden coverage verifies the current adapter
  provenance boundary without adapter-backed `evidence_ids`.
- Packaged CLI `--version` reported `agent-project-memory 3.3.0`.
- Packaged jar metadata inspection confirmed the CLI manifest entrypoint and Maven
  artifact metadata for `3.3.0`.
- The local artifact-integrity dry-run generated a clean dry-run asset directory with
  exactly `agent-project-memory-3.3.0.jar` and `SHA256SUMS`.
- `SHA256SUMS` used the release asset filename only and verified successfully for
  `agent-project-memory-3.3.0.jar`.
- Whitespace checks and public text marker checks passed for the release-prep diff.

## Not Included

v3.3.0 does not add:

- Product behavior changes, CLI command changes, CLI flag changes, generated artifact
  additions or removals, JSON/JSONL field changes, evidence field changes, evidence type
  changes, or schema marker changes.
- Adapter-backed evidence, adapter-aware query behavior, live network connectors,
  connector credentials, credential lookup/storage, background sync, remote cache,
  provider discovery, pagination/retry/rate-limit behavior, or remote freshness claims.
- Provider AI, real AI provider modes, source upload, embeddings, vector search,
  repository chat, generic RAG, SaaS, or automatic code modification.
- Server/API/MCP/editor/plugin runtime, plugin loading, plugin marketplace, socket
  listener, daemon, public API service, or hosted service mode.
- Signing, signatures, signing keys, signing verification instructions, SBOM generation
  or publication, package-manager channels, first-party installed-command distribution,
  JBang catalogs, Homebrew taps, Maven Central publication, SDKMAN/asdf plugins, native
  images, container images, dependency workflow automation, CI release workflow changes,
  release automation, automatic tag creation, automatic GitHub Release creation,
  automatic artifact upload, automatic checksum publication, or package publication.

## Release Assets

The published `v3.3.0` release assets are:

- `agent-project-memory-3.3.0.jar`
- `SHA256SUMS`

These assets are published with the `v3.3.0` GitHub Release.
