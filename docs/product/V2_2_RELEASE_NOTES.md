# v2.2.0 Release Notes

Release date: 2026-06-19

Release status: release candidate. The `v2.2.0` tag, GitHub Release, and release
assets are not published until the manual release step occurs.

`agent-project-memory` v2.2.0 adds disabled-by-default local Jira/YouTrack/Confluence
export import while preserving no-adapter Java/Spring analysis and query compatibility
as the default path.

## Highlights

- Added local JSON export import for Jira issues, YouTrack issues, YouTrack articles,
  and Confluence pages through `adapters.connector_import`.
- Uses the normalized local export format
  `agent-project-memory.connector_export.v1` rather than raw Jira, YouTrack, or
  Confluence API responses.
- Emits accepted connector records as provenance-backed external/document context
  through `.project-memory/source-registry.json` and top-level `project-map.json`
  `adapter_context`.
- Uses `source-registry.json` schema `1.2` for connector provider provenance while
  keeping `project-map.json` adapter-backed output on `schema_version: "2.0"`.
- Keeps no-adapter scans on `project-map.json` `schema_version: "1.0"` and removes
  stale `source-registry.json` when a previous adapter-enabled output set is regenerated
  without an adapter.

## Output And Evidence Compatibility

No-adapter scans remain the compatibility baseline. They keep the current base artifact
set and `project-map.json` `schema_version: "1.0"`, and they do not emit
`.project-memory/source-registry.json`.

Adapter-enabled scans are v2 artifact sets:

```text
.project-memory/project-map.json
.project-memory/project-graph.json
.project-memory/evidence-index.jsonl
.project-memory/source-registry.json
.project-memory/endpoints.md
.project-memory/agent-guide.md
```

When `adapters.connector_import` is explicitly enabled and the configured local export
file is accepted, `source-registry.json` records normalized source documents and
provider provenance for connector records. `project-map.json` keeps the existing
adapter-context shape with `schema_version: "2.0"`.

Connector records reference `source_document_ids` and `provenance_ids`; they do not
carry `evidence_ids`, do not add `evidence-index.jsonl` records, and must not be
promoted to Java/Spring facts, graph facts, quality signals, security findings,
source/spec agreement claims, documentation freshness claims, runtime claims, or
automatic code-modification input.

Current query commands remain focused on no-adapter `schema_version: "1.0"` artifact
sets. They do not read `source-registry.json`, look up source-document IDs, join
connector provenance, or provide adapter-context lookup in v2.2.0.

## Security Notes

The v2.2.0 connector import boundary remains local-first and closed by default:

- Adapters are disabled unless explicitly configured.
- The connector import adapter accepts only one configured repository-relative regular
  JSON file under the scan root.
- Network access remains disabled; no live Jira, YouTrack, or Confluence API fetching
  is included.
- Credentials, tokens, cookies, authorization headers, raw connector configuration
  values, raw request/response logs, raw issue/page/article bodies, descriptions,
  comments, rendered HTML, rich text, attachment names, attachment bodies, configured
  import paths, local absolute paths, and source upload are not serialized by default.
- Accepted connector records are provenance-backed external/document context only; they
  are not Java/Spring code facts and not project evidence.
- Connector text remains stale-prone external context and is not proof that Jira,
  YouTrack, Confluence, or any self-hosted service is current, complete, authoritative,
  reachable, or aligned with repository source.

Release-level security review for the implemented local import boundary completed
before release prep, with no release-blocking findings remaining for v2.2.0.

## Validation

The v2.2.0 release-candidate validation passed:

- `mvn test`: 508 tests, 0 failures, 0 errors, 0 skipped.
- `mvn package`: 508 tests, 0 failures, 0 errors, 0 skipped, including the packaged
  CLI smoke bound to the Maven package lifecycle.
- Packaged CLI `--version` reported `agent-project-memory 2.2.0`.
- Packaged jar metadata inspection confirmed the CLI manifest entrypoint and Maven
  artifact metadata for `2.2.0`.
- Separate packaged CLI no-adapter scan smoke generated the base artifact set, kept
  `project-map.json` on `schema_version: "1.0"`, and did not emit
  `source-registry.json`.
- Separate packaged CLI no-adapter query smoke read the generated artifact set with
  `query <path> list modules`.
- Separate packaged CLI adapter-enabled Jira/YouTrack/Confluence local import smoke
  generated `source-registry.json` with 4 source documents and 8 adapter diagnostics,
  wrote `source_registry_schema_version: "1.2"`, and wrote `project-map.json`
  `schema_version: "2.0"` adapter context.
- Separate packaged CLI output-safety smoke confirmed the connector fixture's raw
  issue/article/page body markers, configured import path, and local absolute workspace
  path were not serialized in generated project-memory output.
- Separate packaged CLI no-adapter regeneration smoke removed a stale
  `source-registry.json` from a previous adapter-enabled scan, kept unrelated output
  files, and returned `project-map.json` to `schema_version: "1.0"` without
  `adapter_context`.
- In a clean local dry-run asset directory, `SHA256SUMS` was generated with the release
  asset filename only and verified successfully for
  `agent-project-memory-2.2.0.jar`.
- `git diff --check`: passed.
- Public release wording review passed.

## Not Included

v2.2.0 does not add:

- Live Jira, YouTrack, or Confluence API fetching.
- Connector credentials, credential lookup, OAuth, PAT, app password, API key, cookie,
  credential storage, credential validation, retry/backoff, rate-limit handling,
  pagination, background sync, remote cache, provider discovery, remote configuration,
  update checks, or external API calls.
- Source upload, telemetry, plugin loading, MCP/server implementation, public API
  services, editor integrations, SaaS, hosted indexes, hosted scanners, or web UI.
- Adapter-aware query support, stable JSON query output, natural-language query
  behavior, or query writes.
- External issue, article, page, comment, attachment, label, workflow, author, space,
  project, sprint, version, or link data as Java/Spring source truth, evidence,
  security findings, source/spec agreement claims, documentation freshness claims,
  runtime claims, or automatic code-modification input.
- AI provider code, prompts that upload source by default, embeddings, vector stores,
  generic RAG, repository chat, AI-generated project facts, AI-generated evidence, or
  LLM calls in the core analyzer, adapter path, or query layer.
- Package-manager publication, installed-command distribution, signing, native images,
  container images, release automation, or automatic code modification.

## Release Assets

When published, the `v2.2.0` GitHub Release is expected to include the standard release
assets:

- `agent-project-memory-2.2.0.jar`
- `SHA256SUMS`
