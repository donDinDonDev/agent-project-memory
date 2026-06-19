# v2.3.0 Release Notes

Release date: 2026-06-19

Release status: published. The `v2.3.0` tag, GitHub Release, executable jar, and
`SHA256SUMS` assets are published.

`agent-project-memory` v2.3.0 adds explicitly enabled mock/no-network AI presentation
artifacts while preserving deterministic project memory, evidence, query, adapter,
profile, and cache boundaries.

## Highlights

- Added the optional `scan --ai-presentation mock_no_network` mode.
- Writes a separate `.project-memory/ai-presentations/` artifact surface with
  `manifest.json` and `brief.md` only when explicitly enabled.
- Labels AI presentation artifacts as non-authoritative and non-evidence in both
  machine-readable metadata and human-readable Markdown.
- Uses only already generated deterministic artifacts as inputs for the mock/no-network
  slice: `project-map.json`, `evidence-index.jsonl`, and `project-graph.json`.
- Keeps default scans free of AI presentation artifacts.
- Keeps real provider integration deferred for a later explicit design and review
  boundary.

## Output And Evidence Compatibility

Default scans remain the compatibility baseline. They keep the existing generated
artifact set and do not emit `.project-memory/ai-presentations/`.

When `scan --ai-presentation mock_no_network` is explicitly selected, the generated
artifact set adds this optional presentation directory:

```text
.project-memory/ai-presentations/
  manifest.json
  brief.md
```

The AI presentation manifest is generated-output metadata only. It is not project
evidence, not a source registry, not a project-map section, and not proof that the
presentation is correct.

AI presentation artifacts may reference existing evidence IDs, source-artifact names,
and graph IDs for navigation. They do not create `evidence-index.jsonl` records,
evidence fields, evidence types, confidence labels, evidence IDs, source references,
Java/Spring facts, graph facts, connector truth, security findings, runtime claims,
source/spec agreement claims, documentation-freshness claims, release evidence, or
automatic code-modification authority.

Current query commands remain focused on existing deterministic generated artifacts.
They do not read AI presentation artifacts or treat AI presentation Markdown as query
truth.

## Security Notes

The v2.3.0 AI presentation boundary remains local-first, explicit, and closed by
default:

- No AI presentation artifacts are generated unless
  `scan --ai-presentation mock_no_network` is selected.
- The mock/no-network provider mode performs no network access.
- No real provider, provider SDK, provider credential, credential lookup, telemetry,
  source upload, prompt logging, prompt transcript serialization, embeddings, vector
  search, generic RAG, repository chat, plugin loading, public API/server behavior,
  SaaS, or web UI is included.
- AI presentation generation does not read raw repository source files,
  generated-source contents, raw local document bodies, generated Markdown bodies, raw
  connector exports, raw adapter input files, connector credentials, raw connector
  request/response logs, remote API responses, environment values, local absolute
  paths, or raw prompt transcripts as AI inputs.
- Authoritative artifacts such as `project-map.json`, `evidence-index.jsonl`,
  `source-registry.json`, `project-graph.json`, cache metadata, and profile artifacts
  are not mutated by AI presentation generation.
- Repository text, local document text, evidence excerpts, adapter-backed records,
  connector text, generated Markdown, and user-provided labels remain untrusted content
  for any future AI prompt boundary.

Release-track security review for the implemented mock/no-network boundary completed
before release prep, with no release-blocking findings remaining for v2.3.0.

## Validation

The v2.3.0 release validation passed:

- `mvn test`: 518 tests, 0 failures, 0 errors, 0 skipped.
- `mvn package`: 518 tests, 0 failures, 0 errors, 0 skipped, including the packaged
  CLI smoke bound to the Maven package lifecycle.
- Packaged CLI `--version` reported `agent-project-memory 2.3.0`.
- Packaged jar metadata inspection confirmed the CLI manifest entrypoint and Maven
  artifact metadata for `2.3.0`.
- Separate packaged CLI no-AI scan smoke generated the base artifact set, kept
  `project-map.json` on `schema_version: "1.0"`, and did not emit
  `.project-memory/ai-presentations/`.
- Separate packaged CLI no-AI query smoke read the generated artifact set with
  `query <path> list modules`.
- Separate packaged CLI mock/no-network AI presentation smoke generated
  `.project-memory/ai-presentations/manifest.json` and
  `.project-memory/ai-presentations/brief.md`, reported provider mode
  `mock_no_network`, and kept network access and source upload disabled.
- Separate packaged CLI AI presentation boundary smoke confirmed authoritative base
  artifacts do not contain AI presentation metadata.
- In a clean local dry-run asset directory, `SHA256SUMS` was generated with the release
  asset filename only and verified successfully for
  `agent-project-memory-2.3.0.jar`.
- `git diff --check`: passed.
- Public release wording review passed.

## Not Included

v2.3.0 does not add:

- Real AI provider integration, provider SDKs, provider selection, network access,
  provider credentials, credential lookup, credential storage, credential validation,
  telemetry, source upload, prompt logging, prompt transcript serialization, provider
  retention/privacy claims, remote cache, background sync, remote configuration, update
  checks, or external API calls.
- AI-generated project facts, evidence records, evidence fields, evidence types,
  connector truth, security findings, vulnerability proof, runtime claims,
  source/spec agreement claims, documentation-freshness claims, release evidence, query
  truth, graph facts, or automatic code-modification input.
- LLM calls in the core analyzer, adapter path, graph builder, evidence index builder,
  cache layer, profile generator, or query layer.
- Repository chat, generic RAG, embeddings, vector stores, semantic search, plugin
  loading, MCP/server implementation, public API services, editor integrations, SaaS,
  hosted indexes, hosted scanners, or web UI.
- Package-manager publication, installed-command distribution, signing, native images,
  container images, release automation, or automatic code modification.

## Release Assets

The expected `v2.3.0` GitHub Release assets are:

- `agent-project-memory-2.3.0.jar`
- `SHA256SUMS`
