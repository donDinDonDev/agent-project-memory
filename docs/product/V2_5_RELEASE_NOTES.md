# v2.5.0 Release Notes

Release date: 2026-06-20

Release status: published. The `v2.5.0` tag, GitHub Release, executable jar, and
`SHA256SUMS` assets are published.

`agent-project-memory` v2.5.0 adds explicit local workspace map aggregation across
configured local repository or service roots while preserving deterministic
single-repo analysis, evidence-backed facts, local-only operation, and no child
repository mutation.

## Highlights

- Added `workspace scan <config>`.
- Uses an explicit local YAML config file with required unique logical `repo_id`
  values and workspace-relative member roots.
- Writes a separate workspace-root `.project-memory/workspace-map.json` artifact from
  existing member `.project-memory/` artifacts.
- Keeps normal single-repo scan and query artifacts unchanged.
- Uses composite workspace evidence references with `repo_id` plus existing per-repo
  `evidence_id` sample references.
- Keeps cross-repo relation emission parked with `relations.analysis_status:
  "not_analyzed"` and an empty `relations.items[]` list.
- Defers workspace query, workspace `agent-context`, workspace graph output,
  adapter-aware workspace context, child repository scan refresh, remote discovery,
  network access, credentials, repository chat, generic RAG, semantic search, and
  automatic code modification.

## Workspace Config

The accepted workspace config shape is:

```yaml
version: 1
members:
  - repo_id: orders
    root: services/orders
```

`workspace scan <config>` treats the config file directory as the workspace root. Member
roots must be workspace-relative paths under that root. Absolute member paths, `./`
prefixes, `..` segments, backslash paths, URL-like paths, generated-output roots,
missing roots, duplicate roots, duplicate `repo_id` values, symlinked roots,
link-count-unverifiable roots, and ambiguous nested roots fail closed before workspace
output is trusted.

The command does not run child repository scans and does not refresh or mutate member
`.project-memory/` directories.

## Output And Evidence Compatibility

The new workspace artifact is:

```text
.project-memory/workspace-map.json
```

It is written under the workspace root, not under child member roots. The artifact uses
`workspace_schema_version: "1.0"` and summarizes member identity, member root paths,
accepted member artifact schema versions, bounded diagnostics, and sample composite
evidence references.

Normal single-repo generated artifacts remain unchanged:

- no-adapter `project-map.json` remains on `schema_version: "1.0"`;
- adapter-enabled `project-map.json` remains on `schema_version: "2.0"`;
- normal member `evidence-index.jsonl` records do not gain a `repo_id` field;
- full member evidence records are not copied into the workspace artifact;
- `workspace-map.json` is not a query input source in this release.

Composite workspace evidence references are navigation keys into member artifacts. They
combine the configured `repo_id` with an existing per-repo `evidence_id`.

## Security Notes

The v2.5.0 workspace boundary remains local-first and explicitly configured:

- No remote repository discovery, organization crawling, clone, fetch, pull, network
  access, remote service call, remote configuration, update check, telemetry, source
  upload, credential lookup, credential storage, provider SDK, server, public API,
  editor plugin, plugin runtime, repository chat, generic RAG, semantic search,
  embeddings, vector store, or automatic code modification is included.
- Workspace config and member roots are treated as untrusted local input until config
  grammar, root containment, link safety, duplicate identity, nested-root, missing-root,
  and generated-output-root checks pass.
- Workspace output must not serialize local absolute paths, raw config contents, raw
  source bodies, raw local document bodies, raw connector exports, command logs,
  credentials, tokens, authorization headers, prompts, or maintainer notes.
- Cross-repo relation emission remains parked for this release. Relation-like member
  evidence IDs or graph metadata do not create workspace relation rows.
- Adapter records, source registry provenance, query output, graph derivation metadata,
  AI presentation, generated Markdown, release notes, and downstream agent output remain
  non-evidence for workspace facts and relations.

Release-level security review for the accepted workspace scope completed before release
prep, with no release-blocking findings remaining for v2.5.0.

## Validation

The v2.5.0 release validation passed:

- Focused workspace config, root-safety, workspace map, parked-relation, and CLI tests:
  96 tests, 0 failures, 0 errors, 0 skipped.
- Full local Maven test suite: 539 tests, 0 failures, 0 errors, 0 skipped.
- Maven package build: 539 tests, 0 failures, 0 errors, 0 skipped, including the
  packaged CLI smoke bound to the Maven package lifecycle.
- Packaged CLI `--version` reported `agent-project-memory 2.5.0`.
- Packaged CLI scan and query smoke over the existing single-repo surface.
- Packaged CLI `workspace scan <config>` smoke over a local multi-member workspace.
- Packaged jar metadata inspection confirmed the CLI manifest entrypoint and Maven
  artifact metadata for `2.5.0`.
- In a clean local dry-run asset directory, `SHA256SUMS` was generated with the release
  asset filename only and verified successfully for
  `agent-project-memory-2.5.0.jar` with SHA-256
  `53611851b3d1a022c5cf14933d91e36b19f2f5402e4981a78e089913be6a7230`.
- Whitespace checks passed.
- Public release wording review passed.

## Not Included

v2.5.0 does not add:

- Cross-repo relation emission, workspace query, workspace `agent-context`, workspace
  graph output, adapter-aware workspace context, child repository scan refresh, child
  repository generated-output mutation, workspace change-impact workflow, source/spec
  agreement scoring, documentation-freshness scoring, runtime service graph, runtime
  dependency graph, call graph, data-flow graph, vulnerability claims, correctness
  claims, or business-priority claims.
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

The `v2.5.0` GitHub Release assets are:

- `agent-project-memory-2.5.0.jar`
- `SHA256SUMS`

These assets are published with this GitHub Release.
