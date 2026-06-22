# v3.0.0 Release Notes

Release date: 2026-06-22

Release status: release candidate. The `v3.0.0` tag, GitHub Release, executable jar,
and `SHA256SUMS` assets are not published yet.

`agent-project-memory` v3.0.0 is the first implementation release for the v3 platform
foundation. It adds a generated artifact-set manifest, validates manifest-present query
input as a coherent generated set, preserves source-backed evidence authority, and
keeps the product local-first and deterministic.

## Highlights

- Adds `.project-memory/artifact-set.json` as a set-level generated-output manifest for
  single-repo scans.
- Inventories required base artifacts plus optional adapter, profile, AI presentation,
  cache, and workspace-related surfaces without changing existing per-artifact schema
  markers.
- Adds bounded reader/query compatibility checks for manifest-present artifact sets,
  including fail-closed rejection for unsupported or mixed generated output state.
- Marks `evidence-index.jsonl` as the only authoritative source-backed evidence
  artifact in the manifest inventory and keeps manifest, project-map, graph, generated
  Markdown, adapter, profile, AI, cache, workspace, query, release, and downstream
  agent surfaces as non-evidence.
- Preserves regeneration-first migration guidance for unsupported or mixed generated
  output sets.
- Adds bounded output/provenance and CI hardening for selected display, provenance, and
  release-readiness surfaces.

## Output And Evidence Compatibility

v3.0.0 adds the artifact-set manifest and manifest-present validation behavior, but it
does not bump `project-map.json` to `schema_version: "3.0"`.

Current generated artifact behavior:

- no-adapter `project-map.json` remains on `schema_version: "1.0"`;
- adapter-enabled `project-map.json` remains on `schema_version: "2.0"` with
  `source-registry.json` when an explicitly enabled adapter accepts input;
- `artifact-set.json` uses `artifact_set_schema_version: "1.0"` for the initial
  manifest foundation;
- `project-graph.json`, `workspace-map.json`, cache, profile, and AI presentation
  surfaces keep their current documented schema markers and semantics;
- `evidence-index.jsonl` remains the source-backed evidence artifact and does not gain
  new evidence fields or adapter-specific evidence records.

Current query, `agent-context`, and impact loading continue to support coherent
no-adapter `schema_version: "1.0"` artifact sets. Legacy no-manifest no-adapter query
input remains supported. When `artifact-set.json` is present, readers validate the
manifest and fail closed on unsupported manifest schema markers, mixed artifact schemas,
required-file mismatches, stale optional artifacts, unsafe manifest paths, workspace
mix-ins, or evidence-authority label mismatches before rendering query output.

Adapter-enabled artifact sets remain unsupported query input in this release. The
migration action for unsupported or mixed generated output is to regenerate the complete
`.project-memory/` output set from source and from the same explicitly configured local
adapter exports, not to edit or copy generated files in place.

## Security Notes

v3.0.0 keeps the local-first security boundary:

- no source upload by default;
- no network connector defaults;
- no credential lookup or storage;
- no real provider AI;
- no server/API/MCP/editor/plugin runtime;
- no repository chat, generic RAG, or automatic code modification;
- no signing, SBOM publication, package-manager channel, native image, container image,
  release automation, artifact upload, tag creation, or GitHub Release publication.

The release candidate includes bounded hardening for selected local output and
provenance surfaces:

- secret-like OpenAPI `operationId` values are redacted in generated and rendered
  output while ordinary operation IDs remain stable;
- connector `source_url` provenance is bound to the accepted provider-specific record
  identity before serialization;
- local structured import `source_identity` values reject sensitive token-shaped
  fragments before adapter provenance is emitted;
- query-rendered path and ID text is sanitized so unsafe path-shaped or sensitive values
  do not become navigation output;
- workspace sample evidence IDs are validated before serialization;
- the current CI workflow pins third-party actions by commit.

These hardening changes do not make the tool a vulnerability scanner, complete secret
detector, secret inventory, security correctness engine, dependency-freshness proof, or
security certification.

Release preparation remains separate from publication. Tags, GitHub Releases, asset
uploads, checksum publication, signing, SBOM publication, package-manager publication,
native image publication, container image publication, and release automation require
separate explicit maintainer approval and are not part of this release candidate.

## Validation

The v3.0.0 release-candidate validation passed:

- Full local Maven test suite: 594 tests, 0 failures, 0 errors, 0 skipped.
- Maven package build: 594 tests, 0 failures, 0 errors, 0 skipped, including the
  packaged CLI smoke bound to the Maven package lifecycle.
- Packaged CLI `--version` and `version` reported `agent-project-memory 3.0.0`.
- Packaged CLI `--help` and `scan --help` printed the expected command help.
- Packaged scan smoke generated `project-map.json`, `artifact-set.json`,
  `project-graph.json`, `evidence-index.jsonl`, `endpoints.md`, and `agent-guide.md`
  for the fixture project with no diagnostics.
- Packaged query smokes covered `list modules`, `list endpoints`, `agent-context`, and
  `impact --files` over the manifest-present no-adapter output set.
- Packaged jar metadata inspection confirmed the CLI manifest entrypoint and Maven
  artifact metadata for `3.0.0`.
- The local artifact-integrity dry-run generated a clean dry-run asset directory with
  exactly `agent-project-memory-3.0.0.jar` and `SHA256SUMS`.
- `SHA256SUMS` used the release asset filename only and verified successfully for
  `agent-project-memory-3.0.0.jar`.
- Targeted release-prep security verification found no reportable findings.

## Not Included

v3.0.0 does not add:

- `project-map.json` `schema_version: "3.0"`, v3-only project-map serialization, or
  v3-only query behavior.
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

The intended `v3.0.0` release assets are:

- `agent-project-memory-3.0.0.jar`
- `SHA256SUMS`

These assets are not published yet. Publication requires the separate release
publication workflow after release-prep review.
