# v3.4.0 Release Notes

Release date: 2026-07-02

Release status: prepared for release review. The `v3.4.0` tag, GitHub Release,
executable jar, and `SHA256SUMS` assets are not published by this checkpoint.

`agent-project-memory` v3.4.0 improves query verification wording for the existing
read-only query surface. It makes the path from compact navigation output to canonical
evidence records clearer while keeping generated artifact schemas, evidence semantics,
adapter behavior, query grammar, and distribution channels unchanged.

## Highlights

- `query <path> explain evidence <id>` now states that
  `evidence-index.jsonl` is the authoritative source-backed evidence artifact.
- Evidence lookup output explicitly says query stdout is deterministic presentation of
  the evidence record, not a source-file readback.
- Evidence-bearing `list`, `find`, and `relations` outputs now include a compact
  verification hint that routes readers to `explain evidence <id>` and source readback
  for important claims.
- `query <path> agent-context` now lists `impact --files <changed-file> [...]` as a
  supported follow-up query and includes a compact verification loop:
  navigate with query output, resolve evidence IDs, then read back cited source
  locations for important claims.

## Output And Evidence Compatibility

v3.4.0 does not change generated JSON or JSONL schema markers:

- no-adapter `project-map.json` remains on `schema_version: "1.0"`;
- adapter-enabled `project-map.json` remains on `schema_version: "2.0"` with
  `source-registry.json` when an explicitly enabled adapter accepts input;
- `artifact-set.json` remains on `artifact_set_schema_version: "1.0"`;
- `project-graph.json` keeps its current graph schema marker and navigation semantics;
- `workspace-map.json` keeps its current schema marker and aggregation semantics;
- `evidence-index.jsonl` keeps its existing field set and remains the source-backed
  evidence artifact.

The compatibility impact is limited to deterministic human-readable query presentation.
Consumers that parse query stdout as an undocumented machine interface should use the
documented JSON/JSONL generated artifacts instead. Existing generated-output consumers
do not need a migration for v3.4.0 beyond regenerating `.project-memory/` outputs with
the current release when desired.

## Security Notes

v3.4.0 keeps the local-first security boundary:

- no source upload by default;
- no network connector defaults;
- no credential lookup or storage;
- no real provider AI;
- no server/API/MCP/editor/plugin runtime;
- no repository chat, generic RAG, or automatic code modification;
- no signing, SBOM publication, package-manager channel, native image, container image,
  release automation, or automatic publication.

The query wording changes reinforce the existing evidence boundary: query stdout,
`agent-context`, impact output, generated Markdown, graph metadata, adapter provenance,
release metadata, and downstream-agent output remain deterministic presentation or
navigation surfaces, not evidence. The release does not make the tool a vulnerability
scanner, complete secret detector, secret inventory, security correctness engine,
dependency-freshness proof, or security certification.

Signing, SBOM publication, package-manager publication, native image publication,
container image publication, and release automation remain separate future work and are
not part of this release.

## Validation

The v3.4.0 release-prep validation passed:

- Full local Maven test suite: 603 tests, 0 failures, 0 errors, 0 skipped.
- Maven package build: 603 tests, 0 failures, 0 errors, 0 skipped, including the
  packaged CLI smoke bound to the Maven package lifecycle.
- Packaged CLI smoke generated `project-map.json`, `artifact-set.json`,
  `project-graph.json`, `evidence-index.jsonl`, `endpoints.md`, and `agent-guide.md`
  for the fixture project.
- Packaged CLI `--version` reported `agent-project-memory 3.4.0`.
- Packaged jar metadata inspection confirmed the CLI manifest entrypoint and Maven
  artifact metadata for `3.4.0`.
- The local artifact-integrity dry-run generated a clean dry-run asset directory with
  exactly `agent-project-memory-3.4.0.jar` and `SHA256SUMS`.
- `SHA256SUMS` used the release asset filename only and verified successfully for
  `agent-project-memory-3.4.0.jar`.
- Whitespace checks and public text marker checks passed for the release-prep diff.

## Not Included

v3.4.0 does not add:

- New query commands, query flags, stable JSON query output, source readback from query,
  generated artifact mutation, adapter-aware query behavior, or workspace query
  behavior.
- Generated artifact additions or removals, JSON/JSONL field changes, evidence field
  changes, evidence type changes, evidence semantic changes, or schema marker changes.
- Adapter-backed evidence, live network connectors, connector credentials, credential
  lookup/storage, background sync, remote cache, provider discovery, pagination/retry/
  rate-limit behavior, or remote freshness claims.
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

Expected v3.4.0 release assets are:

- `agent-project-memory-3.4.0.jar`
- `SHA256SUMS`

These assets are not published by this checkpoint.
