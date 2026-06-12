# v1.0.0 Release Notes

Release date: 2026-06-12

`agent-project-memory` v1.0.0 stabilizes the local-first Java/Spring Maven project-memory
product line. The release is a contract, compatibility, validation, and hardening
milestone over the v0.9 analyzer shape; it does not add connectors, network behavior,
SaaS, web UI, repository chat, generic RAG, generated-source scanning by default, or LLM
calls in the core analyzer.

## Highlights

- Normal generated `project-map.json` output now uses `schema_version: "1.0"` as the
  stable-line compatibility marker.
- The v1.0 marker preserves the current v0.9 JSON shape, redacted scan metadata, and
  evidence semantics. It does not add analyzer capability or change
  `evidence-index.jsonl` behavior.
- Conservative compatibility and migration expectations are documented for
  `project-map.json`, `evidence-index.jsonl`, `endpoints.md`, and `agent-guide.md`.
- Expanded packaged CLI evaluation covered pinned representative Java/Spring Maven
  targets across single-module, multi-module, OpenAPI/spec, JPA-heavy, test-heavy,
  docs/config-heavy, and moderate-size project shapes.
- Release hardening bounded Maven POM/root build-file reads, Java source discovery and
  parsing workload, and pre-materialization candidate retention for resource config,
  OpenAPI/Swagger spec, and local Markdown discovery.
- The supported distribution path remains the executable GitHub Release jar with
  optional `SHA256SUMS` verification. Package-manager channels remain future work.

## Output Compatibility

The generated files remain:

```text
.project-memory/project-map.json
.project-memory/evidence-index.jsonl
.project-memory/endpoints.md
.project-memory/agent-guide.md
```

The v0.9-to-v1.0 migration is intentionally narrow:

- `project-map.json` moves from `schema_version: "0.9"` to `"1.0"`.
- The current JSON shape and evidence semantics are preserved.
- Consumers that allowlist known schema markers should add `"1.0"` for the preserved
  v0.9 shape.
- Regenerate all four `.project-memory/` files together so JSON facts, evidence IDs, and
  Markdown evidence references stay aligned.

`project-map.json` and `evidence-index.jsonl` are the stable machine-readable surfaces.
`endpoints.md` and `agent-guide.md` remain deterministic, evidence-visible, cautious
human-readable outputs; exact Markdown presentation is not a parser API unless the
architecture contract explicitly says otherwise.

## Validation

The v1.0.0 local release-prep validation passed:

- `mvn test`: 338 tests, 0 failures, 0 errors, 0 skipped.
- `mvn package`: 338 tests, 0 failures, 0 errors, 0 skipped, plus packaged CLI scan,
  help, and version validation.
- Separate packaged CLI smoke covered `--help`, `help`, `scan --help`, `--version`,
  `version`, and `scan` with `target/agent-project-memory-1.0.0.jar`.
- The packaged scan generated `project-map.json`, `endpoints.md`,
  `evidence-index.jsonl`, and `agent-guide.md` with `schema_version: "1.0"`.
- Packaged jar metadata inspection confirmed the CLI manifest entrypoint and filtered
  version resource for `1.0.0`.
- `SHA256SUMS` was generated with the release asset filename only and verified
  successfully.
- `git diff --check`: passed.
- Public release-document marker audit passed.
- Risk-based release security review and bounded hardening completed with no
  release-blocking findings remaining.

Public evaluation summary:
[docs/development/evaluations/v1.0-evaluation-corpus_SUMMARY.md][v1.0-eval].

[v1.0-eval]: ../development/evaluations/v1.0-evaluation-corpus_SUMMARY.md

## Security Notes

v1.0.0 keeps the deterministic local analyzer boundary:

- no source upload or external service dependency in the core analyzer;
- no connector configuration, network-loaded config, credentials, telemetry, update
  checks, plugin loading, package publication, global machine config, or user-home
  config discovery;
- no generated-source scanning by default and no symlink following by default;
- no raw config values, raw include/exclude patterns, config file contents,
  secret-looking values, document bodies, stack traces, local absolute paths, or
  generated output contents in generated project memory;
- no tool-config evidence records;
- no package-manager publishing, signing, credentials, upload automation, or remote
  release-state change in artifact verification;
- no SaaS, web UI, repository chat, generic RAG, LLM calls in the core analyzer, or
  automatic code modification.

The release hardening keeps oversized or pathological local inputs bounded and
diagnostic-driven. Oversized POM and Java inputs are skipped with deterministic
`scan.diagnostics` warnings, and large candidate path sets are truncated before fact or
evidence materialization.

## Not Included

- GitHub/GitLab/Jira/YouTrack/Confluence connectors, network access, external
  documentation fetching, telemetry, update checks, or credentials.
- Package-manager installation channels such as shell wrappers, JBang catalogs,
  Homebrew taps, Maven Central publication, SDKMAN/asdf plugins, native images,
  container images, signing, or release upload automation.
- Generated-source scanning by default, symlink following, broad filesystem traversal
  outside the scanned repository root, or Java/Maven/API/test scope changes from local
  Markdown include/exclude rules.
- SaaS, web UI, repository chat, generic RAG, LLM calls in the core analyzer, or
  automatic code modification.

## Known Follow-Ups

The v1.0 validation records bounded future work that is not required for this release:

- Continue monitoring `agent-guide.md` size on larger JPA-heavy and test-heavy projects
  before changing guide presentation density.
- Add future pinned evaluation targets with denser default-scope Markdown and broader
  OpenAPI/spec shapes before making stronger corpus claims.
- Keep package-manager distribution channels parked until separate channel-specific
  designs cover installation, update, checksum, signing, and release-process behavior.

## Expected Release Assets

- `agent-project-memory-1.0.0.jar`
- `SHA256SUMS`
