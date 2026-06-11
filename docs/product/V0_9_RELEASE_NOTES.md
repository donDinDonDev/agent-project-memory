# v0.9.0 Release Notes

Release date: 2026-06-11

`agent-project-memory` v0.9.0 makes the local CLI more predictable, safely configurable,
and ready for repeatable local release artifact verification before the v1.0
stabilization track. The release keeps the deterministic local analyzer boundary and
does not add connectors, network behavior, SaaS, web UI, repository chat, generic RAG,
or LLM calls in the core analyzer.

## Highlights

- Root-local `agent-project-memory.yml` scan config discovery, plus optional explicit
  `scan <path> --config <repo-relative-yaml>` selection.
- A small safe YAML config schema with required `version: 1`, optional
  `features.local_markdown`, reserved disabled `features.generated_sources` and
  `features.follow_symlinks`, and local Markdown-only `documents.include` /
  `documents.exclude` rules.
- Redacted top-level `scan` metadata in `project-map.json` for effective config source,
  feature state, path-policy counts/statuses, and bounded non-fatal diagnostics.
- Stable CLI help/version command forms, documented exit codes, bounded command
  validation, concise scan summaries, and packaged help/version validation.
- Deterministic aggregate caps for local Markdown document count, accepted Markdown
  bytes, heading references, chunk references, reconciliation mention observations, and
  reconciliation rows.
- Release artifact verification using the executable shaded jar plus `SHA256SUMS`.
- Public installation guidance that keeps GitHub Release executable jars with optional
  checksum verification as the minimal supported path through v1.0.

## Output Compatibility

v0.9.0 moves normal generated `project-map.json` output to `schema_version: "0.9"`.
The v0.9 contract builds on the v0.8 local Markdown/document contract, v0.7
tests/quality contract, v0.6 JPA/domain contract, v0.5 Spring application surface
contract, v0.4 API surface contract, and v0.3 module-aware build/config contract:

- The same four output files remain under `.project-memory/`.
- A new top-level `scan` object records redacted effective scan metadata. It may record
  selected config path when safe, feature enablement source, path-policy counts, and
  bounded non-fatal diagnostics.
- The selected tool config is execution metadata, not project evidence. It does not
  create `evidence-index.jsonl` records and does not reuse `config_file` evidence.
- Generated outputs do not serialize raw config values, raw include/exclude patterns,
  config file contents, YAML nodes, environment variables, credentials, tokens,
  secret-looking values, source excerpts, document bodies, stack traces, local absolute
  paths, or generated output contents.
- User include/exclude rules refine local Markdown discovery only. They cannot override
  built-in safety exclusions and do not change Java/Maven/API/test analyzer scope.
- Local Markdown caps are represented as bounded non-fatal `scan.diagnostics` warnings
  when reached. Diagnostics are metadata, not evidence.

The generated files remain:

```text
.project-memory/project-map.json
.project-memory/evidence-index.jsonl
.project-memory/endpoints.md
.project-memory/agent-guide.md
```

`docs/architecture/OUTPUT_CONTRACT.md` and
`docs/architecture/EVIDENCE_MODEL.md` describe the generated v0.9 output shape,
redacted scan metadata, config path policy, diagnostic boundaries, and no-tool-config
evidence decision.

## Validation

The v0.9 release validation passed:

- `mvn test`: 314 tests, 0 failures, 0 errors, 0 skipped.
- `mvn package`: 314 tests, 0 failures, 0 errors, 0 skipped, plus packaged CLI
  validation.
- Packaged CLI help/version and representative scan behavior succeeded with the
  `agent-project-memory-0.9.0.jar` artifact.
- The packaged scan generated `project-map.json`, `endpoints.md`,
  `evidence-index.jsonl`, and `agent-guide.md`.
- `SHA256SUMS` was generated with release asset filenames only and verified
  successfully.
- `git diff --check`: passed.
- Public release-document review passed.

Earlier v0.9 release-track checks supporting this release:

- focused CLI, config, path-policy, document-discovery, output, and packaged CLI tests
  across the implementation slices;
- packaged CLI/config/performance validation covering default scans, safe config
  include/exclude behavior, disabled local Markdown behavior, invalid config exit codes,
  help/version behavior, deterministic output stability, and bounded local performance
  observations;
- release security review follow-up with bounded hardening for path-rule
  matching, local Markdown aggregate limits, OpenAPI/warning traversal, generated-source
  warning POM reads, and stable no-follow spec/POM/root build-file reads.

Public evaluation summary:
[docs/development/evaluations/v0.9-cli-config-performance_SUMMARY.md][v0.9-eval].

[v0.9-eval]: ../development/evaluations/v0.9-cli-config-performance_SUMMARY.md

## Security Notes

v0.9.0 keeps the deterministic local analyzer boundary:

- no source upload or external service dependency in the core analyzer;
- no connector configuration, network-loaded config, credentials, telemetry, update
  checks, plugin loading, package publication, global machine config, or user-home
  config discovery;
- no generated-source scanning by default and no symlink following by default;
- no raw config values, raw include/exclude patterns, config file contents, secret-like
  values, document bodies, source excerpts, stack traces, local absolute paths, or
  generated output contents in generated project memory;
- no tool-config evidence records;
- no package-manager publishing, signing, credentials, upload automation, or remote
  release-state change in artifact verification;
- no SaaS, web UI, repository chat, generic RAG, LLM calls in the core analyzer, or
  automatic code modification.

Config parsing is bounded and local-only. The selected config must be one regular YAML
file under the scan root and must not be loaded through symlinks. Built-in safety
exclusions continue to win over all user includes in v0.9.

## Not Included

- GitHub/GitLab/Jira/YouTrack/Confluence connectors, network access, external
  documentation fetching, telemetry, update checks, or credentials.
- Global config files, user-home config files, environment-variable config discovery,
  network-loaded config, generated output directory config discovery, or config merging.
- Generated-source scanning by default, symlink following, broad filesystem traversal
  outside the scanned repository root, or user path rules that affect Java/Maven/API/test
  analyzer scope.
- Shell wrappers, JBang catalogs, Homebrew taps, Maven Central publication, SDKMAN/asdf
  plugins, native images, container images, signing, or release upload automation.
- SaaS, web UI, repository chat, generic RAG, LLM calls in the core analyzer, or
  automatic code modification.

## Known Follow-Ups

The v0.9 validation and release review record bounded future work that is not
required for this release:

- Design a shared source parse model before attempting broader analyzer parse reuse.
- Recheck performance on larger representative projects before making stronger
  performance claims.
- Keep package-manager distribution channels parked until a separate channel-specific
  design covers installation, update, checksum, signing, and release-process behavior.

## Publication Status

v0.9.0 is published on GitHub Releases with executable jar and `SHA256SUMS` assets.

Published release assets:

- `agent-project-memory-0.9.0.jar`
- `SHA256SUMS`
