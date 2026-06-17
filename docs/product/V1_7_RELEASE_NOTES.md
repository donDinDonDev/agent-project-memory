# v1.7.0 Release Notes

Release date: 2026-06-17

`agent-project-memory` v1.7.0 hardens generated and rendered output against accidental
exposure of obvious secret-looking values while preserving the local-first analyzer
boundary and the existing evidence model.

The release keeps redaction as deterministic output hardening: no repository-wide
secret scan, no vulnerability scanner, no credential validation, no secret inventory,
no connectors, and no LLM calls in the core analyzer or query layer.

## Highlights

- Added a shared deterministic redaction primitive for selected generated and rendered
  strings that would otherwise include obvious credential-like key/value, header/value,
  or private-key material.
- Uses the plain marker `[REDACTED_SECRET_LIKE_VALUE]` inside existing string fields or
  rendered text.
- Applies redaction at generation time for selected evidence excerpts, generated JSON,
  generated Markdown, profile Markdown, graph output, cache or scan diagnostics, and
  bounded CLI error text.
- Applies query render-time redaction for selected existing artifact text without
  rewriting, repairing, or mutating artifact files.
- Preserves evidence IDs, normalized repository-relative paths, symbols, line ranges,
  fact IDs, confidence, uncertainty, relation statuses, and claim categories so
  generated facts remain navigable after redaction.
- Adds regression coverage for generated artifacts, generated Markdown, profile output,
  graph output, cache metadata, query output, scan stdout/stderr, and bounded CLI error
  text using fake sentinel values only.

## Output Compatibility

v1.7.0 remains a `schema_version: "1.0"` compatibility expansion for generated
project-memory artifacts. The release does not add new evidence fields, evidence types,
`project-map.json` schema markers, or `project-graph.json` graph schema markers.

The base generated files remain:

```text
.project-memory/project-map.json
.project-memory/project-graph.json
.project-memory/evidence-index.jsonl
.project-memory/endpoints.md
.project-memory/agent-guide.md
```

Redaction behavior is additive output hardening:

- The marker is stored or rendered inside existing selected string fields, most often
  evidence `excerpt` values or generated/query Markdown text.
- Evidence excerpt construction redacts before final excerpt bounding and output
  escaping.
- Local Markdown heading-derived IDs, anchors, graph source references, and heading
  evidence symbol keys derive from redaction-safe heading text when the source heading
  contains an obvious secret-looking value.
- Query redaction is presentation-only and does not mutate `project-map.json`,
  `project-graph.json`, `evidence-index.jsonl`, generated Markdown, profile artifacts,
  or cache metadata.
- Query output, generated Markdown, profile output, graph derivation metadata, cache
  metadata, diagnostics, and release notes are not project evidence.

## Security Notes

The v1.7.0 security boundary remains conservative:

- Redaction targets selected output strings only. It does not scan the whole repository
  for secrets, perform entropy-only detection, detect every unlabeled or split secret,
  classify credentials, validate credentials against providers, or prove that no
  secrets exist.
- The absence of a redaction marker is not evidence that an input contains no secrets.
- The presence of a redaction marker is not vulnerability evidence, security
  correctness evidence, credential validity evidence, or a secret inventory row.
- The path/symlink audit covered scan roots, generated output files, root-local config,
  local Markdown discovery, generated-source metadata, cache metadata, graph output,
  profile output, query artifact reads, and CLI stdout/stderr. Bounded fixes stayed
  within the documented local-first, no-symlink-following, metadata-only, and read-only
  query boundaries.
- Generated-source contents remain unscanned by default. Config values, environment
  variables, local machine state, remote services, connector credentials, and generated
  source bodies remain outside the v1.7 redaction scan scope.

## Validation

The v1.7.0 local release-prep validation passed:

- `mvn test`: 448 tests, 0 failures, 0 errors, 0 skipped.
- `mvn package`: 448 tests, 0 failures, 0 errors, 0 skipped, plus packaged CLI smoke.
- Packaged CLI smoke covered help, version, representative scan output, all selected
  profile outputs, incremental cache metadata, read-only query output, and fake-sensitive
  redaction behavior with `target/agent-project-memory-1.7.0.jar`.
- Packaged query smoke confirmed selected legacy unredacted evidence excerpt text is
  redacted at render time without mutating `project-map.json`, `project-graph.json`, or
  `evidence-index.jsonl`.
- Packaged jar metadata inspection confirmed the CLI manifest entrypoint, filtered
  version resource, and Maven artifact metadata for `1.7.0`.
- In a clean local dry-run asset directory, `SHA256SUMS` was generated with the release
  asset filename only and verified successfully.
- `git diff --check`: passed.
- Public release-document marker audit passed.
- Risk-based release-prep review completed with no release-blocking findings remaining.

## Not Included

v1.7.0 does not add:

- Complete secret detection, secret inventory, credential classification, credential
  validation, vulnerability scanning, security correctness claims, or proof that a
  repository contains no secrets.
- External secret scanners, remote APIs, network access, telemetry, source upload,
  connector credentials, SaaS, web UI, editor integration, server surfaces, plugin
  platforms, repository chat, generic RAG, embeddings, or LLM calls in the core analyzer
  or query layer.
- Generated-source content scanning, default symlink following, environment-variable
  interpolation, config value extraction, Maven or Gradle execution, dependency
  resolution, runtime Spring reconstruction, stable JSON query output, release
  automation, package-manager publication, signing, native images, container images, or
  automatic code modification.
- Real secrets in fixtures, tests, docs, examples, release materials, or public
  vulnerability reports.

## Release Assets

- `agent-project-memory-1.7.0.jar`
- `SHA256SUMS`
