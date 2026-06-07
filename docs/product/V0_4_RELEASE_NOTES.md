# v0.4.0 Release Notes

Release date: 2026-06-07

`agent-project-memory` v0.4.0 adds a deterministic API surface taxonomy for local
Java/Spring Maven repositories. It keeps source-visible Spring MVC endpoints,
spec-declared OpenAPI operations, generated-source API signals, repository-rest warnings,
and hidden HTTP warnings separate, while preserving the local-first analyzer boundary
and the same four generated `.project-memory/` files.

## Highlights

- Local OpenAPI/Swagger spec-file discovery for common `openapi.*` and `swagger.*`
  filenames as declared API inputs.
- `api_surface.openapi.spec_files` facts with repository-relative paths, optional module
  ownership, spec format/kind, bounded version observations, and `api_spec` evidence.
- Minimal bounded OpenAPI/Swagger YAML/JSON operation extraction under
  `api_surface.openapi.operations.items[]`.
- Spec-backed operation facts with declared path, HTTP method, bounded `operationId`,
  bounded tags, `implementation_status: "not_analyzed"`, and operation `api_spec`
  evidence.
- Generated-source root path warning signals for common local `target/generated-*`
  roots with `path_signal` evidence, without reading generated source contents.
- `endpoints.md` API surface sections that keep source-visible endpoints, declared
  OpenAPI operations, generated-source API signals, repository-rest warnings, and hidden
  HTTP warnings separate.
- `agent-guide.md` API surface interpretation generated only from structured facts and
  evidence.

## Output Compatibility

v0.4.0 moves normal generated `project-map.json` output to `schema_version: "0.4"`.
The v0.4 contract builds on the v0.3 module-aware build/configuration boundary:

- The same output files remain under `.project-memory/`.
- Source-visible Spring MVC endpoint facts remain in top-level `endpoints[]`.
- Endpoint facts include `api_surface_category` so direct handler mappings and
  source-visible interface-declared mappings can be distinguished.
- OpenAPI/Swagger spec files live under `api_surface.openapi.spec_files`.
- OpenAPI/Swagger operations live under `api_surface.openapi.operations.items[]`, not
  `endpoints[]`.
- Generated-source API signals, repository-rest warnings, and hidden HTTP warnings
  remain warning references and do not become endpoint or operation facts.
- Evidence records keep the existing `evidence-index.jsonl` field set. v0.4 adds
  `api_spec` evidence for local spec facts and operation facts, and `path_signal`
  evidence for generated-source path warnings.

The generated files remain:

```text
.project-memory/project-map.json
.project-memory/evidence-index.jsonl
.project-memory/endpoints.md
.project-memory/agent-guide.md
```

## Validation

This release-prep pass ran and passed:

- `mvn test`: 202 tests, 0 failures, 0 errors, 0 skipped.
- `mvn package`: 202 tests, 0 failures, 0 errors, 0 skipped.
- Packaged CLI smoke from the Maven build: generated `project-map.json`,
  `endpoints.md`, `evidence-index.jsonl`, and `agent-guide.md`.
- `git diff --check`: passed.
- `git diff --stat`: run for the release-prep diff.

Earlier v0.4 release-track gates supporting this release:

- v0.4 real-project evaluation on pinned Java/Spring Maven projects
- v0.4 review/security diff audit

The v0.4 implementation-range risk-based implementation-range security assessment reported no findings:

The final release-prep diff uses a manual low-risk documentation assessment because it changes only
release documentation, changelog, README/status wording, and the Maven project version;
the v0.4 review and security audit already covered the implementation range with a risk-based security assessment.

## Security Notes

v0.4.0 keeps the deterministic local analyzer boundary:

- no Maven execution during scan;
- no network fetching or external `$ref` resolution;
- no connector, SaaS, web UI, repository chat, generic RAG, or LLM call in the core
  analyzer;
- no default generated-source content scanning;
- no runtime handler mapping, implementation coverage, generated client SDK, or
  source/spec agreement claims.

OpenAPI/Swagger parsing is bounded and local-only. Invalid, unsupported, oversized, or
duplicate operation inputs degrade to warnings instead of endpoint or operation claims.
Generated-source path signals prove only normalized local path presence and do not read
or summarize generated source contents.

## Not Included

- Full OpenAPI validation.
- External `$ref` fetching or remote schema retrieval.
- Maven generation or generated-source content scanning.
- Generated API reconstruction.
- Endpoint creation from OpenAPI operations, generated-source signals, build warnings, or
  repository-rest warnings.
- Runtime Spring handler mapping reconstruction.
- Runtime API implementation coverage or source/spec agreement claims.
- Client SDK reconstruction.
- Full Java symbol solving.
- Gradle support.
- Maven profile resolution, effective POM reconstruction, or dependency graph
  reconstruction.
- Runtime Spring bean graph, component scanning, deployment, or security-policy claims.
- Test execution, coverage, CI, mutation testing, or call graph analysis.
- Local Markdown/document ingestion.
- Connectors for YouTrack, Jira, Confluence, GitHub, or GitLab.
- LLM calls in the core analyzer.
- SaaS, web UI, repository chat, generic RAG, or automatic code modification.

## Known Follow-Ups

The v0.4 evaluation and review record bounded future work that is not required for this
release:

- Reduce duplicated legacy generator warning presentation when both generated-source and
  hidden HTTP plugin warnings describe the same Maven code-generation plugin.
- Consider future plugin `inputSpec` path discovery for OpenAPI specs that do not use the
  current common filenames.
- Consider a future endpoint/spec relation model that preserves separate code evidence,
  spec evidence, confidence, and uncertainty.
- Consider a future explicit generated-source scan mode. It must be non-default and must
  have separate output/evidence contract design.

## Publication Status

The `v0.4.0` tag and GitHub release are published:

- Release: <https://github.com/donDinDonDev/agent-project-memory/releases/tag/v0.4.0>
- Assets: `agent-project-memory-0.4.0.jar` and `SHA256SUMS`.
- Jar SHA-256:
  `5a7944704727a2e6b28eeffa04027a3dbc47c3da3ec8141890243716ee479992`.

Post-publish verification confirmed that the remote tag points to the intended release
commit, the release is not a draft or prerelease, the expected assets are attached, and
the published checksum verifies the downloaded jar.
