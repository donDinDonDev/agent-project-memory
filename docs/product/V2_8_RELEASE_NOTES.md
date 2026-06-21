# v2.8.0 Release Notes

Release date: 2026-06-21

Release status: release candidate. The `v2.8.0` tag, GitHub Release, executable jar,
and `SHA256SUMS` assets are not published yet.

`agent-project-memory` v2.8.0 improves distribution and release artifact integrity
without turning release preparation into publication automation. The supported public
distribution baseline remains the executable GitHub Release jar plus `SHA256SUMS`.

## Highlights

- Added a local artifact-integrity dry-run helper for packaged release candidates.
- Validates the expected candidate jar filename from Maven coordinates.
- Validates packaged CLI `--version` output.
- Validates jar manifest `Main-Class`.
- Validates embedded Maven `pom.properties` group ID, artifact ID, and version.
- Creates a clean local dry-run asset directory with exactly the jar and
  `SHA256SUMS`.
- Verifies that `SHA256SUMS` contains a filename-only jar entry and passes local
  checksum verification.
- Keeps signing, SBOM publication, package-manager channels, native images, container
  images, dependency workflow automation, CI release workflow changes, and release
  automation parked.

## Artifact Integrity Dry-Run

After building the candidate jar:

```sh
mvn package
bash scripts/release-artifact-integrity-dry-run.sh
```

The helper creates or refreshes only:

```text
target/release-artifact-dry-run/
```

The dry-run asset directory contains:

```text
agent-project-memory-2.8.0.jar
SHA256SUMS
```

The checksum can be verified from inside that directory:

```sh
shasum -a 256 -c SHA256SUMS
```

The dry-run helper is validation only. It does not publish, upload, attach assets, sign
files, generate an SBOM, create or move tags, create or edit releases, deploy packages,
use credentials, or contact remote services.

## Output And Evidence Compatibility

Normal no-adapter generated artifacts remain on their existing stable schemas:

- `project-map.json` remains on `schema_version: "1.0"`;
- `project-graph.json` remains on `graph_schema_version: "1.0"`;
- `evidence-index.jsonl` records are unchanged.

v2.8.0 does not add, remove, rename, or reinterpret generated project-memory fields. It
does not create release metadata artifacts under `.project-memory/` and does not change
evidence semantics.

Release notes, GitHub Release bodies, checksums, CI logs, dependency update reports,
security review summaries, package metadata, and helper output remain non-evidence for
generated Java/Spring project facts.

## Security Notes

The v2.8.0 distribution hardening boundary remains local-first and manual:

- The artifact-integrity helper treats project root, target directory, asset directory,
  jar metadata, and checksum contents as local inputs that must be validated before the
  dry-run asset directory is refreshed.
- The helper rejects unsafe asset directory placement and keeps generated dry-run
  assets under the repository `target/` directory.
- The helper validates filename-only checksum contents so local absolute paths,
  temporary paths, and parent directories are not written into `SHA256SUMS`.
- No network access, remote service calls, remote configuration, update checks,
  telemetry, source upload, credential lookup, credential storage, package registry
  access, signing authority, or release publication authority is included.
- Release preparation remains separate from publication. Tags, GitHub Releases, asset
  uploads, checksum publication, signing, SBOM publication, package-manager
  publication, native image publication, container image publication, and release
  automation require separate explicit maintainer approval and are not part of this
  release candidate.

Release-level distribution and supply-chain review for the accepted v2.8 scope
completed before release prep, with no release-blocking findings remaining.

## Validation

The v2.8.0 release-candidate validation passed:

- Focused artifact-integrity dry-run helper tests: 6 tests, 0 failures, 0 errors,
  0 skipped.
- Full local Maven test suite: 572 tests, 0 failures, 0 errors, 0 skipped.
- Maven package build: 572 tests, 0 failures, 0 errors, 0 skipped, including the
  packaged CLI smoke bound to the Maven package lifecycle.
- Packaged CLI `--version` reported `agent-project-memory 2.8.0`.
- Packaged CLI scan smoke passed.
- Packaged jar metadata inspection confirmed the CLI manifest entrypoint, filtered
  version resource, and Maven artifact metadata for `2.8.0`.
- The local artifact-integrity dry-run generated a clean dry-run asset directory with
  exactly `agent-project-memory-2.8.0.jar` and `SHA256SUMS`.
- `SHA256SUMS` used the release asset filename only and verified successfully for
  `agent-project-memory-2.8.0.jar`.
- Whitespace checks, release-notes trailing-whitespace checks, and public release
  wording review passed.

## Not Included

v2.8.0 does not add:

- Signing, signatures, signing keys, key custody policy, signing verification
  instructions, or signature release assets.
- SBOM generation, SBOM publication, SBOM release assets, dependency freshness claims,
  license compliance claims, or vulnerability absence claims.
- Package-manager channels, first-party installed-command distribution, JBang catalogs,
  Homebrew taps, Maven Central publication, SDKMAN/asdf plugins, native images, or
  container images.
- CI release workflow changes, dependency workflow automation, release automation,
  automatic tag creation, automatic GitHub Release creation, artifact upload,
  checksum publication, or package publication.
- Network access, remote service calls, provider API calls, source upload, provider
  SDKs, real provider calls, credentials, credential lookup, credential storage,
  credential validation, OAuth, API keys, tokens, cookies, or authorization header
  handling.
- Repository chat, generic RAG, semantic search, embeddings, vector stores, MCP server,
  local server, socket listener, daemon, public API service, editor plugin, plugin
  runtime, plugin marketplace, web UI, or SaaS.
- Analyzer behavior changes, generated output schema changes, evidence semantic
  changes, adapter behavior changes, workspace behavior changes, policy profile
  behavior changes, change-impact behavior changes, release publication, or automatic
  code modification.

## Release Assets

The intended `v2.8.0` release assets are:

- `agent-project-memory-2.8.0.jar`
- `SHA256SUMS`

These assets are not published yet. Publication requires the separate release
publication workflow after release-prep review.
