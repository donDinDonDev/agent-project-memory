# v2.6.0 Release Notes

Release date: 2026-06-20

Release status: release candidate. The `v2.6.0` tag, GitHub Release, executable jar,
and `SHA256SUMS` assets are not published yet.

`agent-project-memory` v2.6.0 adds conservative read-only change-impact hints for
explicit changed-file inputs while preserving deterministic artifacts, evidence-backed
facts, local-only operation, and no repository mutation from query commands.

## Highlights

- Added `query <path> impact --files <changed-file> [...]`.
- Uses explicit repository-relative changed-file paths, not Git inspection or raw diff
  parsing.
- Reads existing no-adapter `project-map.json`, `evidence-index.jsonl`, and
  `project-graph.json` artifacts.
- Renders direct matches, one-hop graph neighbors, tied relation-status rows,
  low-confidence planning hints, explicit `not_represented` rows, and bounded
  diagnostics.
- Keeps the impact output as deterministic text on stdout.
- Does not generate impact reports, mutate generated artifacts, refresh scans, read
  source bodies, inspect Git state, call the network, look up credentials, or modify
  code.

## Impact Query

The accepted command shape is:

```sh
java -jar target/agent-project-memory-2.6.0.jar query /path/to/java-spring-project impact --files src/main/java/com/example/Foo.java
```

`impact --files` accepts one or more explicit changed-file values. Values must be
repository-relative paths. Absolute paths, escaping segments, generated-output paths,
URL-like strings, glob or regex syntax, option-like values, raw diff fragments, blank
values, and multiline values are rejected before artifact loading.

The command reports:

- `direct_match` rows for existing artifact references to the changed file;
- `graph_neighbor` rows for one-hop graph orientation from directly matched graph
  nodes;
- `relation_status` rows tied to directly matched graph nodes;
- `planning_hint` rows from existing low-confidence quality/change-risk signals;
- `not_represented` rows when no accepted artifact directly references a valid
  changed-file input;
- `diagnostic` rows for duplicates, caps, or other bounded non-fatal conditions.

The output is navigation and presentation only. It is not evidence, a complete impact
analysis, a vulnerability claim, a correctness claim, a runtime trace, or a business
priority claim.

## Output And Evidence Compatibility

Normal no-adapter generated artifacts remain on their existing stable schemas:

- `project-map.json` remains on `schema_version: "1.0"`;
- `project-graph.json` remains on `graph_schema_version: "1.0"`;
- `evidence-index.jsonl` records are unchanged.

The impact query does not add, remove, rename, or reinterpret generated output fields.
It reads existing artifacts and renders a bounded query result only.

Graph derivation metadata, query output, diagnostics, planning hints, and impact rows
remain non-evidence. Evidence-backed facts remain grounded in existing evidence records.

## Security Notes

The v2.6.0 impact boundary remains local-first and read-only:

- No network access, remote repository discovery, provider API calls, source upload,
  telemetry, update checks, credential lookup, credential storage, server mode, public
  API, editor plugin, repository chat, generic RAG, semantic search, embeddings, vector
  store, or automatic code modification is included.
- Query paths, generated artifact roots, required artifact files, graph references,
  evidence references, and changed-file arguments are treated as untrusted local input
  until validation passes.
- Query output must not expose local absolute paths, raw source bodies, raw document
  bodies, raw generated-source contents, raw command logs, stack traces, credentials,
  tokens, authorization headers, prompts, or maintainer notes.
- Impact output remains navigation and presentation only; it does not create evidence
  or convert graph derivation into source-backed facts.

Release-level security review for the accepted impact query scope completed before
release prep, with no release-blocking findings remaining for v2.6.0.

## Validation

The v2.6.0 release-candidate validation passed:

- Focused query CLI tests: 41 tests, 0 failures, 0 errors, 0 skipped.
- Focused artifact reader tests: 12 tests, 0 failures, 0 errors, 0 skipped.
- Focused output redaction tests: 7 tests, 0 failures, 0 errors, 0 skipped.
- Full local Maven test suite: 546 tests, 0 failures, 0 errors, 0 skipped.
- Maven package build: 546 tests, 0 failures, 0 errors, 0 skipped, including the
  packaged CLI smoke bound to the Maven package lifecycle.
- Packaged CLI `--version` reported `agent-project-memory 2.6.0`.
- Packaged CLI scan, `list modules`, `agent-context`, impact query, and workspace scan
  smoke passed.
- Packaged jar metadata inspection confirmed the CLI manifest entrypoint, filtered
  version resource, and Maven artifact metadata for `2.6.0`.
- In a clean local dry-run asset directory, `SHA256SUMS` was generated with the release
  asset filename only and verified successfully for
  `agent-project-memory-2.6.0.jar`.
- Whitespace checks and public release wording review passed.

## Not Included

v2.6.0 does not add:

- A top-level `impact` command, `--from-git-diff`, raw diff parsing, Git working-tree
  inspection, branch or commit comparison, rename detection, stable JSON impact output,
  generated Markdown, generated impact reports, workspace impact, cross-repo impact,
  adapter-aware impact, source/spec scoring, documentation-freshness scoring, runtime
  tracing, call graphs, data-flow graphs, vulnerability claims, correctness claims,
  production-impact claims, or business-priority claims.
- Remote repository clone, fetch, pull, provider API scan, organization crawler,
  background sync, remote cache, remote index, hosted index, SaaS, web UI, repository
  chat, generic RAG, semantic search, embeddings, vector stores, MCP server, local
  server, socket listener, daemon, public API service, editor plugin, plugin runtime, or
  plugin marketplace.
- Network access, remote service calls, remote configuration, update checks, telemetry,
  source upload, provider SDKs, real provider calls, credentials, credential lookup,
  credential storage, credential validation, OAuth, API keys, tokens, cookies, or
  authorization header handling.
- Branch, commit, tag, release, pull-request, issue, package-manager publication,
  installed-command distribution, signing, native image, container image, artifact
  upload, release publication, release automation, or automatic code modification.

## Release Assets

The intended `v2.6.0` release assets are:

- `agent-project-memory-2.6.0.jar`
- `SHA256SUMS`

These assets are not published yet. Publication requires the separate release
publication workflow after release-prep review.
