# v1.9.0 Release Notes

Release date: 2026-06-17

`agent-project-memory` v1.9.0 prepares the public v2 architecture boundary while
preserving the current local-first v1.x analyzer and generated-output contracts.

This is an architecture-preparation documentation release: no production adapter APIs,
connector implementations, network or authentication behavior, plugin loading,
MCP/server surfaces, AI provider code, new CLI commands or flags, generated output
schema changes, evidence semantic changes, analyzer behavior changes, package-manager
publication, or release automation are included.

## Highlights

- Documented the planned v2 adapter platform boundary, including optional adapter
  responsibilities, forbidden responsibilities, core/adapters separation, local export
  import as the first safe implementation candidate, network-off defaults, and
  no-source-upload defaults.
- Documented the planned normalized source-document and connector provenance model,
  including source identity, import mode, timestamps or hashes when known, and
  trust-boundary labels without treating connector records as code-backed facts.
- Documented draft v2 output and evidence migration questions without changing current
  `project-map.json` or `evidence-index.jsonl` behavior.
- Documented the planned optional AI presentation boundary over deterministic generated
  memory, including allowed inputs, forbidden authoritative outputs, non-evidence
  labeling, provider/privacy/network/source-upload defaults, and adapter-provenance
  interaction.
- Extended the public threat model for planned v2 local import adapters, future API
  connector modes, optional AI, plugin/API surfaces, external data risks, credential
  handling, network defaults, provenance boundaries, and future security review gates.

## Output And Evidence Compatibility

v1.9.0 remains a `schema_version: "1.0"` compatibility release for generated
project-memory artifacts. It does not add, remove, rename, or reinterpret generated
output fields, evidence fields, evidence types, `project-map.json` schema markers, or
`project-graph.json` graph schema markers.

The base generated files remain:

```text
.project-memory/project-map.json
.project-memory/project-graph.json
.project-memory/evidence-index.jsonl
.project-memory/endpoints.md
.project-memory/agent-guide.md
```

The v2 material added in this release is design and migration preparation only:

- The current v1.x implementation does not emit adapter packages, connector output,
  adapter provenance sections, adapter evidence records, connector credentials, network
  metadata, AI provider metadata, prompts, embeddings, vector indexes, chat transcripts,
  plugin metadata, MCP/server metadata, or AI-generated project facts.
- Future adapter-backed records must remain distinguishable from code-backed facts,
  inferred relations, uncertain signals, document-backed observations, graph metadata,
  cache metadata, profile Markdown, query output, generated Markdown, and AI output.
- Future optional AI output, if introduced later, must be labeled as non-evidence and
  must not create project facts, evidence records, connector truth, security findings,
  vulnerability proof, runtime claims, source/spec agreement claims, repository-file
  edits, or code changes.

## Security Notes

The v1.9.0 security boundary remains conservative:

- The current core product still has no network trust boundary because it does not fetch
  remote resources, call external APIs, upload source, load remote configuration, or
  invoke LLMs.
- Future adapters, connectors, optional AI, plugin/API surfaces, telemetry, update
  checks, and remote configuration remain off by default unless a later release designs,
  implements, tests, reviews, and documents explicit enablement.
- Future credential lookup, storage, rotation, validation, redaction, and error
  reporting must be designed before authenticated connector, provider, plugin, or API
  modes are implemented.
- External records, connector content, plugin manifests, API requests, API responses,
  prompt inputs, and repository text must be treated as untrusted input and must not
  bypass provenance, evidence, redaction, credential, permission, or network defaults.
- Generated evidence remains a local source reference, not a security proof. Generated
  Markdown, query output, graph derivation metadata, cache metadata, profile Markdown,
  release notes, chat output, and any future AI output are not project evidence.

## Validation

The v1.9.0 local release-prep validation passed:

- `mvn test`: 448 tests, 0 failures, 0 errors, 0 skipped.
- `mvn package`: 448 tests, 0 failures, 0 errors, 0 skipped, including the packaged
  CLI smoke bound to the Maven package lifecycle.
- Packaged CLI `--version` reported `agent-project-memory 1.9.0`.
- Packaged jar metadata inspection confirmed the CLI manifest entrypoint and Maven
  artifact metadata for `1.9.0`.
- In a clean local dry-run asset directory, `SHA256SUMS` was generated with the release
  asset filename only and verified successfully for
  `agent-project-memory-1.9.0.jar`.
- `git diff --check`: passed.
- Release-notes whitespace check: passed.
- Public release-document marker audit passed.

The v1.9 architecture-prep work changed documentation/design text and Maven version
metadata only. No production code, tests, fixtures, goldens, generated artifacts,
dependencies, scripts, CLI behavior, or packaged runtime behavior changed.

## Not Included

v1.9.0 does not add:

- Production adapter APIs, local import adapter implementation, GitHub/GitLab/Jira/
  YouTrack/Confluence connectors, connector output, adapter provenance artifacts, or
  adapter evidence records.
- Network access, HTTP clients, external API calls, source upload, telemetry, background
  sync, remote configuration, update checks, authentication flows, connector
  credentials, credential storage, credential validation, or secret inventories.
- AI provider code, prompts that upload source by default, embeddings, vector stores,
  generic RAG, repository chat, AI-generated project facts, AI-generated evidence, or
  LLM calls in the core analyzer or query layer.
- Plugin loading, plugin marketplaces, MCP/server implementation, public API services,
  editor integrations, SaaS, hosted indexes, hosted scanners, or web UI.
- Analyzer behavior changes, new parser families, generated-source content scanning,
  default symlink following, Maven or Gradle execution during scans, dependency
  resolution, runtime Spring reconstruction, runtime ORM reconstruction, runtime graph
  construction, source/spec agreement scoring, documentation freshness scoring, new CLI
  commands or flags, stable JSON query output, generated output schema changes, evidence
  field/type changes, or evidence semantic changes.
- Package-manager publication, installed-command distribution, signing, native images,
  container images, release automation, or automatic code modification.

## Release Assets

The `v1.9.0` GitHub Release publishes the standard release assets:

- `agent-project-memory-1.9.0.jar`
- `SHA256SUMS`
