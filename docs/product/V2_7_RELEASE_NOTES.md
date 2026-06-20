# v2.7.0 Release Notes

Release date: 2026-06-20

Release status: published. The `v2.7.0` tag, GitHub Release, executable jar, and
`SHA256SUMS` assets are published.

`agent-project-memory` v2.7.0 adds explicit local policy profiles for scan
configuration presets and guardrails while preserving deterministic local analysis,
evidence-backed facts, no-default-network behavior, and the existing agent output
profile surface.

## Highlights

- Added `scan <path> --policy-profile <name>`.
- Added root-local scan config selection through `policy_profile: <name>`.
- Supports `guarded-local`, `docs-focused`, and `adapter-local`.
- Keeps `--policy-profile` separate from existing `--agent-profile` output
  presentation selectors.
- Fails closed for unknown profile names, duplicated selectors, mismatched config and
  CLI selectors, and unsafe combinations.
- Emits bounded `project-map.json` `scan.policy_profile` execution metadata only when a
  policy profile is explicitly selected.
- Keeps no-profile scans on the compatibility baseline without default policy metadata.
- Does not create evidence records, evidence fields, evidence types, tool-config
  evidence, security findings, compliance claims, or vulnerability claims.

## Policy Profiles

The accepted command shape is:

```sh
java -jar target/agent-project-memory-2.7.0.jar scan /path/to/java-spring-project --policy-profile guarded-local
```

The same profile may be selected from the root-local `agent-project-memory.yml`:

```yaml
version: 1
policy_profile: docs-focused
```

Supported profiles:

- `guarded-local` keeps the scan on the narrow built-in local-input path and rejects
  adapters, AI presentation, user document include/exclude expansion, generated-source
  content scanning, symlink following, network access, credentials, telemetry, and
  source upload.
- `docs-focused` keeps the run local-only and no-network while allowing validated
  Markdown-only document include/exclude refinement under the existing document path
  policy.
- `adapter-local` allows one explicitly configured existing local import adapter under
  the current repository-relative local-file validation rules. It does not enable an
  adapter by itself.

Policy profiles are local configuration presets and guardrails. They are not security
certifications, compliance modes, vulnerability scanners, secret inventories, hosted
policy management, enterprise policy enforcement, or complete safety proofs.

## Output And Evidence Compatibility

Normal no-adapter generated artifacts remain on their existing stable schemas:

- `project-map.json` remains on `schema_version: "1.0"`;
- `project-graph.json` remains on `graph_schema_version: "1.0"`;
- `evidence-index.jsonl` records are unchanged.

When a policy profile is explicitly selected, `project-map.json` includes bounded
execution metadata under `scan.policy_profile`. This metadata records enum-like
selection and posture fields such as selected profile, selection source, profile
version, disabled network/source-upload/credential posture, and allowed or rejected
optional surfaces. It does not include raw config values, include/exclude patterns,
adapter import paths, local absolute paths, source bodies, document bodies,
generated-source contents, command logs, credentials, tokens, prompts, or
secret-looking values.

Policy profile metadata is execution metadata, not project evidence. It is not copied
to `evidence-index.jsonl`, is not referenced from `evidence_ids`, and must not be
treated as proof of compliance, complete safety, security correctness, credential
absence, vulnerability absence, runtime behavior, source/spec agreement, documentation
freshness, production impact, or business priority.

## Security Notes

The v2.7.0 policy profile boundary remains local-first and fail-closed:

- No network access, remote policy loading, remote repository discovery, provider API
  calls, source upload, telemetry, update checks, credential lookup, credential storage,
  server mode, public API, editor plugin, repository chat, generic RAG, semantic search,
  embeddings, vector store, hosted policy management, or automatic code modification is
  included.
- Policy profile names, config files, adapter configuration, document path rules, and
  generated-output roots are treated as untrusted local input until validation passes.
- Profile metadata and diagnostics must not expose local absolute paths, raw config
  values, raw document path rules, raw source bodies, raw document bodies, raw
  generated-source contents, raw command logs, stack traces, credentials, tokens,
  authorization headers, prompts, or maintainer notes.
- Release-candidate hardening expands authorization-value redaction beyond
  `Bearer`/`Basic`, bounds Java and OpenAPI fact collection, excludes generated,
  build, dependency, private, and hidden OpenAPI spec locations from spec evidence, and
  makes query artifact loading fail closed for unsafe evidence paths or unresolved
  project-map evidence references.
- `query <path> impact --files ...` now treats generated fact path-like fields as
  direct matches only when those paths are backed by the fact or graph node's resolved
  evidence paths.
- `features.generated_sources: true` and `features.follow_symlinks: true` remain
  reserved invalid configuration in v2.7.

Release-level security review for the accepted policy profile scope completed as part
of release validation, with no release-blocking findings remaining for v2.7.0.

## Validation

The v2.7.0 release validation passed:

- Focused policy profile CLI/config/output/evidence tests: 112 tests, 0 failures,
  0 errors, 0 skipped.
- Full local Maven test suite: 566 tests, 0 failures, 0 errors, 0 skipped.
- Maven package build: 566 tests, 0 failures, 0 errors, 0 skipped, including the
  packaged CLI smoke bound to the Maven package lifecycle.
- Focused release hardening tests cover authorization redaction, query
  artifact validation, impact matching, Java source file caps, OpenAPI spec discovery
  exclusions, and OpenAPI operation caps.
- Packaged CLI `--version` reported `agent-project-memory 2.7.0`.
- Packaged CLI scan smoke passed for no-profile, `guarded-local`, `docs-focused`, and
  `adapter-local` selections.
- Packaged CLI metadata checks confirmed no-profile scans omit `scan.policy_profile`,
  explicit profile scans emit bounded `scan.policy_profile`, and profile selections do
  not create `evidence-index.jsonl` policy records.
- Packaged jar metadata inspection confirmed the CLI manifest entrypoint, filtered
  version resource, and Maven artifact metadata for `2.7.0`.
- In a clean local dry-run asset directory, `SHA256SUMS` was generated with the release
  asset filename only and verified successfully for
  `agent-project-memory-2.7.0.jar`.
- Whitespace checks and public release wording review passed.

## Not Included

v2.7.0 does not add:

- Hosted policy management, remote policy registry, remote configuration, user-home
  policy discovery, organization policy crawling, background sync, update checks,
  telemetry, or hosted index.
- Network access, remote service calls, provider API calls, source upload, provider
  SDKs, real provider calls, credentials, credential lookup, credential storage,
  credential validation, OAuth, API keys, tokens, cookies, or authorization header
  handling.
- Security certification, compliance mode, vulnerability scanning, secret inventory,
  complete safety proof, enterprise policy enforcement, production-correctness claims,
  source/spec scoring, documentation freshness scoring, runtime claims, production
  impact, or business-priority claims.
- Generated-source content scanning, symlink following, raw source-body policy output,
  stable JSON policy reports, generated Markdown policy reports, or policy evidence
  records.
- Repository chat, generic RAG, semantic search, embeddings, vector stores, MCP server,
  local server, socket listener, daemon, public API service, editor plugin, plugin
  runtime, plugin marketplace, web UI, or SaaS.
- Branch, commit, tag, release, pull-request, issue, package-manager publication,
  installed-command distribution, signing, native image, container image, artifact
  upload, release publication, release automation, or automatic code modification.

## Release Assets

The intended `v2.7.0` release assets are:

- `agent-project-memory-2.7.0.jar`
- `SHA256SUMS`

These assets are published with the GitHub Release.
