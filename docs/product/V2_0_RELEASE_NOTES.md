# v2.0.0 Release Notes

Release date: 2026-06-18

Release status: published. The `v2.0.0` GitHub Release includes the executable jar and
`SHA256SUMS` assets listed below.

`agent-project-memory` v2.0.0 introduces the first production adapter boundary while
preserving the local-first deterministic Java/Spring analyzer as the default path.

## Highlights

- Added the adapter-domain contract foundation for stable source-document IDs and
  required provenance validation.
- Added disabled-by-default adapter configuration for a local structured import mode,
  with repository-relative regular-file path validation, network-off defaults, and
  redacted scan metadata.
- Added a local structured import reference adapter for explicitly configured local
  export JSON files.
- Added `.project-memory/source-registry.json` as the adapter provenance artifact and
  `project-map.json` `schema_version: "2.0"` adapter context when the adapter is
  explicitly enabled and input is accepted.
- Documented v1-to-v2 migration expectations for no-adapter compatibility,
  adapter-enabled artifact sets, downstream consumers, and current query limits.

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

When the local structured import adapter is explicitly enabled and accepted,
`project-map.json` uses `schema_version: "2.0"` for top-level `adapter_context`, and
`source-registry.json` stores normalized source-document and provenance records.
Adapter-backed rows reference `source_document_ids` and `provenance_ids`; they do not
carry `evidence_ids` and must not be promoted to Java/Spring facts.

The evidence model does not add adapter-specific evidence types or fields in v2.0.0.
`evidence-index.jsonl` remains the source-backed evidence index for repository facts.
`source-registry.json` is provenance metadata, not evidence and not proof that an
external source is current, complete, authoritative, or aligned with repository source.

Current query commands remain focused on no-adapter `schema_version: "1.0"` artifact
sets. They do not read `source-registry.json`, look up source-document IDs, join
provenance, or provide adapter-context lookup in v2.0.0.

## Security Notes

The v2.0.0 adapter boundary remains local-first and closed by default:

- Adapters are disabled unless explicitly configured.
- The shipped local structured import adapter accepts only one configured
  repository-relative regular file under the scan root.
- Network access remains disabled.
- Credentials, tokens, authorization headers, raw connector configuration values, raw
  request/response logs, local absolute paths, raw record bodies, and raw import paths
  are not serialized by default.
- Adapter-backed records are provenance-backed external/document context only; they are
  not Java/Spring code facts and not project evidence.
- Trusted local input handling rejects multi-link regular files before treating
  repository-relative content as source-owned input.

Release-level security review and targeted follow-up hardening completed before
publication, with no release-blocking findings remaining for v2.0.0.

## Validation

The v2.0.0 release validation passed:

- `mvn test`: 493 tests, 0 failures, 0 errors, 0 skipped.
- `mvn package`: 493 tests, 0 failures, 0 errors, 0 skipped, including the packaged
  CLI smoke bound to the Maven package lifecycle.
- Packaged CLI `--version` reported `agent-project-memory 2.0.0`.
- Packaged jar metadata inspection confirmed the CLI manifest entrypoint and Maven
  artifact metadata for `2.0.0`.
- Separate packaged CLI no-adapter scan smoke with
  `target/agent-project-memory-2.0.0.jar` generated the base artifact set, kept
  `project-map.json` on `schema_version: "1.0"`, and did not emit
  `source-registry.json`.
- Separate packaged CLI no-adapter query smoke read the generated artifact set with
  `query <path> list modules`.
- Separate packaged CLI adapter-enabled scan smoke generated `source-registry.json`
  with 2 source documents and 2 adapter diagnostics, and wrote `project-map.json`
  `schema_version: "2.0"` adapter context.
- In a clean local dry-run asset directory, `SHA256SUMS` was generated with the release
  asset filename only and verified successfully for
  `agent-project-memory-2.0.0.jar`.
- `git diff --check`: passed.
- Release-notes whitespace check: passed.
- Public release-document marker audit passed.

## Not Included

v2.0.0 does not add:

- Networked GitHub, GitLab, Jira, YouTrack, Confluence, or other API connectors.
- Connector credentials, credential lookup, credential storage, credential validation,
  source upload, telemetry, background sync, remote configuration, update checks, or
  external API calls.
- AI provider code, prompts that upload source by default, embeddings, vector stores,
  generic RAG, repository chat, AI-generated project facts, AI-generated evidence, or
  LLM calls in the core analyzer, adapter path, or query layer.
- Plugin loading, plugin marketplaces, MCP/server implementation, public API services,
  editor integrations, SaaS, hosted indexes, hosted scanners, or web UI.
- Adapter-aware query support, stable JSON query output, new natural-language query
  behavior, or query writes.
- Package-manager publication, installed-command distribution, signing, native images,
  container images, release automation, or automatic code modification.

## Release Assets

The `v2.0.0` GitHub Release includes the standard release assets:

- `agent-project-memory-2.0.0.jar`
- `SHA256SUMS`
