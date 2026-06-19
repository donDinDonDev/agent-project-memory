# v2.4.0 Release Notes

Release date: 2026-06-19

Release status: release candidate. The `v2.4.0` tag, GitHub Release, executable jar,
and `SHA256SUMS` assets are not published yet.

`agent-project-memory` v2.4.0 adds a CLI-only `agent-context` query view for
read-only agent and editor-adjacent consumption while preserving deterministic project
memory, evidence, graph, adapter, profile, AI presentation, and no-write boundaries.

## Highlights

- Added `query <path> agent-context`.
- Renders deterministic stdout from existing no-adapter `project-map.json`
  `schema_version: "1.0"` and `evidence-index.jsonl` artifacts.
- Uses valid `project-graph.json` only as optional navigation metadata when present.
- Keeps the command read-only: no scans, generated artifacts, cache refresh, profile
  generation, AI presentation generation, source reads, or repository writes.
- Keeps agent-facing output as navigation and presentation only, not project evidence
  or code-change authority.
- Defers MCP, server, public API, editor plugin, network, credentials, telemetry,
  source upload, semantic search, repository chat, and automatic code modification.

## Output And Evidence Compatibility

Default scans and generated artifact schemas remain unchanged. No-adapter
`project-map.json` stays on `schema_version: "1.0"`, and adapter-enabled artifact sets
remain outside current query support.

The new command reads the same local artifact root policy as existing query commands:
`<path>` may name either a repository directory containing `.project-memory/` or the
`.project-memory/` directory itself.

Required inputs are:

```text
.project-memory/project-map.json
.project-memory/evidence-index.jsonl
```

Optional graph navigation input is:

```text
.project-memory/project-graph.json
```

The command does not parse generated Markdown bodies, profile Markdown, AI presentation
Markdown, cache metadata, adapter context, connector records, or `source-registry.json`
as fact inputs. It may reference existing evidence IDs so users or agents can call
`query <path> explain evidence <evidence-id>`, but it does not create, repair,
strengthen, reinterpret, or replace evidence.

Agent-context output is not a stable machine-readable parser interface in v2.4.0.
Stable JSON query output remains future work.

## Security Notes

The v2.4.0 agent-context boundary remains local-first, deterministic, and read-only:

- No repository source files are opened to expand evidence.
- No generated artifacts, cache metadata, profile artifacts, AI presentation artifacts,
  source files, repository docs, root instruction files, scan config, or external state
  are created or mutated.
- No MCP server, socket listener, daemon, public API service, editor plugin, plugin
  runtime, network access, remote service call, remote config, update check, telemetry,
  credential lookup, credential storage, source upload, raw prompt transcript
  serialization, real provider call, repository chat, generic RAG, semantic search,
  embeddings, vector store, or automatic code modification is included.
- `source-registry.json`, adapter context rows, connector records, profile artifacts,
  cache metadata, generated Markdown, AI presentation, graph derivation metadata, query
  output, downstream agent output, prompts, and LLM output remain non-evidence.
- AI presentation artifacts, when present, may be mentioned only as optional
  non-authoritative/non-evidence presentation and are not fact inputs.

Release-track security review for the implemented CLI-only boundary completed before
release prep, with no release-blocking findings remaining for v2.4.0.

## Validation

The v2.4.0 release-prep validation passed:

- Focused query and artifact-reader tests for the implemented `agent-context` surface.
- `mvn test`: 521 tests, 0 failures, 0 errors, 0 skipped.
- `mvn package`: 521 tests, 0 failures, 0 errors, 0 skipped, including the packaged
  CLI smoke bound to the Maven package lifecycle.
- Packaged CLI `--version` reported `agent-project-memory 2.4.0`.
- Packaged CLI scan smoke generated the base artifact set and kept `project-map.json`
  on `schema_version: "1.0"`.
- Packaged CLI `query <path> agent-context` smoke read the generated artifact set and
  rendered deterministic agent-context output.
- Packaged jar metadata inspection confirmed the CLI manifest entrypoint and Maven
  artifact metadata for `2.4.0`.
- In a clean local dry-run asset directory, `SHA256SUMS` was generated with the release
  asset filename only and verified successfully for
  `agent-project-memory-2.4.0.jar`.
- `git diff --check`: passed.
- Public release wording review passed.

## Not Included

v2.4.0 does not add:

- MCP server, local server, socket listener, daemon, public API service, editor plugin,
  plugin runtime, plugin marketplace, tool marketplace, hosted index, SaaS, web UI, or
  repository chat.
- Network access, remote service calls, remote configuration, update checks, telemetry,
  source upload, provider SDKs, real provider calls, credentials, credential lookup,
  credential storage, credential validation, OAuth, API keys, tokens, cookies, or
  authorization header handling.
- Adapter-aware query over `source-registry.json`, connector records, external data as
  Java/Spring source truth, connector truth, security findings, runtime claims,
  source/spec agreement claims, documentation-freshness claims, release evidence, or
  automatic code-modification input.
- Repository source readback during query, generated artifact mutation, scan config
  edits, root instruction file edits, branch/commit/tag/release automation,
  pull-request or issue writes, package-manager publication, installed-command
  distribution, signing, native images, container images, or release automation.
- Stable JSON query output, semantic search, embeddings, vector stores, generic RAG,
  workspace memory, change-impact workflow, full call graph, runtime dependency graph,
  runtime Spring graph, runtime handler mapping, coverage or CI claims, vulnerability
  claims, correctness claims, or business-priority claims.

## Release Assets

The expected `v2.4.0` GitHub Release assets are:

- `agent-project-memory-2.4.0.jar`
- `SHA256SUMS`
