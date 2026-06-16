# v1.6.0 Release Notes

Release date: 2026-06-16

`agent-project-memory` v1.6.0 adds deterministic read-only query commands over existing
`.project-memory/` artifacts while preserving the local-first analyzer boundary and the
existing evidence model.

The release keeps query as an artifact-backed lookup layer: no source scanning, no
repository writes, no chat or natural-language query, no embeddings, no connectors, and
no LLM calls in the core analyzer or query layer.

## Highlights

- Added a top-level `query <path> ...` command that reads existing generated artifacts
  from either a repository directory or a direct `.project-memory/` directory.
- Added deterministic text list commands for generated modules, source-visible Spring
  MVC endpoints, spec-backed declared API operations, JPA entities and embeddables, and
  emitted tests.
- Added exact evidence lookup with `query <path> explain evidence <id>`.
- Added exact, case-sensitive fact and symbol lookup with `find fact <term>` and
  `find symbol <term>`.
- Added one-hop graph relation lookup with `relations <id>` and
  `--direction incoming|outgoing|both`, preserving graph edges and
  `relation_statuses[]` as separate navigation material.
- Kept non-graph query commands independent of optional graph output: a missing or
  malformed `project-graph.json` is ignored unless a graph-backed lookup is requested.

## Output Compatibility

v1.6.0 remains a `schema_version: "1.0"` compatibility expansion for generated
project-memory artifacts. Query commands consume existing artifacts and do not add,
remove, rename, or reinterpret generated output fields.

The base generated files remain:

```text
.project-memory/project-map.json
.project-memory/project-graph.json
.project-memory/evidence-index.jsonl
.project-memory/endpoints.md
.project-memory/agent-guide.md
```

Query behavior is additive:

- `project-map.json` is unchanged.
- `project-graph.json` is unchanged.
- `evidence-index.jsonl` is unchanged.
- Query output is not evidence.
- Generated Markdown, profile artifacts, cache metadata, diagnostics, and graph
  derivation metadata are not query evidence sources.
- Stable JSON query output is not included in v1.6.0; current query output is
  deterministic human text.

## Security Notes

The query boundary remains conservative:

- Query commands do not run scans, create `.project-memory/`, refresh cache metadata,
  generate profile artifacts, read repository source files, or write repository files.
- Query input is limited to existing generated artifact files under the resolved
  artifact root.
- Query commands reject missing, malformed, unsupported, unsafe, or internally
  inconsistent required artifacts with bounded deterministic errors.
- Graph relation lookup requires a valid `project-graph.json`; non-graph commands ignore
  missing or malformed optional graph output.
- Query output must not serialize source bodies, local document bodies, config
  contents, raw build-script bodies, generated-source contents, generated Markdown
  bodies, raw command logs, stack traces, local absolute paths, credentials, tokens, or
  secret-looking values.
- Query output must not imply call reachability, dependency reachability, runtime Spring
  wiring, runtime routing, source/spec agreement, documentation freshness, coverage, CI
  status, assertion behavior, vulnerability, correctness, production impact, business
  priority, or complete ownership claims.

## Validation

The v1.6.0 local release-prep validation passed:

- `mvn test`: 430 tests, 0 failures, 0 errors, 0 skipped.
- `mvn package`: 430 tests, 0 failures, 0 errors, 0 skipped, plus packaged CLI smoke.
- Packaged CLI smoke covered help, version, base scan output, read-only list commands,
  evidence explanation, exact fact lookup, exact symbol lookup, graph relation lookup,
  and direct `.project-memory/` query input with `target/agent-project-memory-1.6.0.jar`.
- Packaged query smoke confirmed non-graph commands ignore a malformed optional
  `project-graph.json`, while relation lookup still requires a valid graph artifact.
- Packaged jar metadata inspection confirmed the CLI manifest entrypoint, filtered
  version resource, and Maven artifact metadata for `1.6.0`.
- In a clean local dry-run asset directory, `SHA256SUMS` was generated with the release
  asset filename only and verified successfully.
- `git diff --check`: passed.
- Public release-document marker audit passed.
- Risk-based release review for the final release-prep diff completed with no
  release-blocking findings remaining.

## Not Included

v1.6.0 does not add:

- Stable JSON query output or a `--format` option.
- Chat, natural-language query, embeddings, vector stores, semantic search, generic RAG,
  or LLM calls in the core analyzer or query layer.
- Substring, fuzzy, regex, glob, semantic, or embedding search.
- Source scanning, scan refresh, `.project-memory/` creation, cache refresh, profile
  generation, repository writes, or code modification during query.
- Full call graphs, runtime dependency graphs, runtime Spring graphs, runtime handler
  mapping, runtime repository/database verification, or impact-analysis guarantees.
- Source/spec agreement scoring, documentation freshness scoring, documentation
  correctness claims, test coverage, CI status, assertion proof, vulnerability,
  correctness, production-impact, or business-priority claims.
- Connectors, network access, telemetry, SaaS, web UI, repository chat, package-manager
  publication, signing, native images, container images, or release automation.

## Release Assets

- `agent-project-memory-1.6.0.jar`
- `SHA256SUMS`
