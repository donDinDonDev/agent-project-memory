# v2.9.0 Release Notes

Release date: 2026-06-21

Release status: release candidate. The `v2.9.0` tag, GitHub Release, executable jar,
and `SHA256SUMS` assets are not published yet.

`agent-project-memory` v2.9.0 is a planning/design release for v3 preparation. It
freezes the public v3.0 direction, documents the planned v3 schema/API migration and
evidence/provenance boundary, and keeps v3 implementation work for a later release.

## Highlights

- Freezes v3.0 as a stable local-first, evidence-backed project memory platform and
  migration release.
- Documents included v3.0 scope, excluded and deferred surfaces, implementation
  prerequisites, and breaking-change categories.
- Documents the planned v3 schema/API migration strategy as future behavior: coherent
  artifact sets, regeneration-first migration, fail-closed handling for unsupported or
  mixed artifacts, and compatibility-test categories.
- Documents the planned v3 evidence/provenance boundary: source-backed evidence remains
  authoritative for project facts, while adapter provenance, query output, generated
  Markdown, graph derivation, cache/profile/AI output, release metadata, security
  reports, downstream agent output, chat output, and LLM output remain non-evidence
  unless a later contract explicitly changes that boundary before implementation.
- Keeps current v1/v2 generated artifact behavior unchanged.

## Output And Evidence Compatibility

v2.9.0 does not add, remove, rename, or reinterpret generated project-memory fields.
It does not implement v3 serializers, readers, query behavior, migration code,
artifact-set manifests, or schema markers.

Current generated artifact behavior remains unchanged:

- no-adapter `project-map.json` remains on `schema_version: "1.0"`;
- adapter-enabled `project-map.json` remains on `schema_version: "2.0"`;
- `source-registry.json` remains the adapter provenance artifact when adapters are
  explicitly enabled;
- `project-graph.json`, `workspace-map.json`, cache, profile, and AI presentation
  surfaces keep their current documented markers and semantics;
- `evidence-index.jsonl` evidence semantics are unchanged.

The planned v3 migration action for future users is regeneration from source and from
explicitly configured local adapter exports, not in-place mutation of an existing
`.project-memory/` directory. Future v3 implementation work must update the architecture
docs, tests, changelog, and release notes together before any v3 behavior ships.

## Security Notes

v2.9.0 does not change production security behavior. It does not change parser, path,
filesystem, output rendering, evidence serialization, dependency, release artifact,
checksum, network, authentication, provider, plugin/runtime, CI, or publication
behavior.

The release keeps current local-first defaults:

- no source upload by default;
- no network connector defaults;
- no credential lookup or storage;
- no real provider AI;
- no server/API/MCP/editor/plugin runtime;
- no signing, SBOM publication, package-manager channel, native image, container image,
  release automation, artifact upload, tag creation, or GitHub Release publication.

Release preparation remains separate from publication. Tags, GitHub Releases, asset
uploads, checksum publication, signing, SBOM publication, package-manager publication,
native image publication, container image publication, and release automation require
separate explicit maintainer approval and are not part of this release candidate.

## Validation

The v2.9.0 release-candidate validation passed:

- Full local Maven test suite: 572 tests, 0 failures, 0 errors, 0 skipped.
- Maven package build: 572 tests, 0 failures, 0 errors, 0 skipped, including the
  packaged CLI smoke bound to the Maven package lifecycle.
- Packaged CLI `--version` reported `agent-project-memory 2.9.0`.
- Packaged jar metadata inspection confirmed the CLI manifest entrypoint, filtered
  version resource, and Maven artifact metadata for `2.9.0`.
- The local artifact-integrity dry-run generated a clean dry-run asset directory with
  exactly `agent-project-memory-2.9.0.jar` and `SHA256SUMS`.
- `SHA256SUMS` used the release asset filename only and verified successfully for
  `agent-project-memory-2.9.0.jar`.
- Whitespace checks, release-notes trailing-whitespace checks, candidate GitHub Release
  body trailing-whitespace checks, and public marker audits passed.

## Not Included

v2.9.0 does not add:

- v3 schema markers, v3 serializers, v3 readers, v3 query behavior, v3 compatibility
  behavior, v3 migration code, v3 artifact-set manifests, v3 generated outputs, or v3
  tests/goldens.
- Server/API/MCP/editor/plugin runtime, plugin loading, plugin marketplace, socket
  listener, daemon, public API service, or hosted service mode.
- Provider AI, live network connectors, connector credentials, credential lookup,
  background sync, remote cache, remote discovery, telemetry, source upload,
  embeddings, vector search, repository chat, generic RAG, SaaS, or automatic code
  modification.
- Signing, signatures, signing keys, signing verification instructions, SBOM generation
  or publication, package-manager channels, first-party installed-command distribution,
  JBang catalogs, Homebrew taps, Maven Central publication, SDKMAN/asdf plugins, native
  images, container images, dependency workflow automation, CI release workflow changes,
  release automation, automatic tag creation, automatic GitHub Release creation,
  artifact upload, checksum publication, or package publication.
- Analyzer behavior changes, generated output schema changes, evidence semantic
  changes, adapter behavior changes, workspace behavior changes, policy profile
  behavior changes, change-impact behavior changes, release publication, or automatic
  code modification.

## Release Assets

The intended `v2.9.0` release assets are:

- `agent-project-memory-2.9.0.jar`
- `SHA256SUMS`

These assets are not published yet. Publication requires the separate release
publication workflow after release-prep review.
