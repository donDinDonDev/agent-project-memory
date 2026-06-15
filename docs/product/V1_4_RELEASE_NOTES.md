# v1.4.0 Release Notes

Release date: 2026-06-15

`agent-project-memory` v1.4.0 adds opt-in incremental scan cache support for unchanged
repository states while preserving full-scan correctness, deterministic outputs, and
the existing evidence model.

The release keeps the local-first deterministic analyzer boundary: no source upload, no
remote cache, no daemon, no connectors, no network access, no build execution, and no
LLM calls in the core analyzer.

## Highlights

- Added explicit `scan <path> --incremental` selection.
- Added repository-local cache metadata under `.project-memory/cache/v1/`.
- Added cache warm-up behavior: a first incremental scan for a repository state runs
  normal full analysis and writes cache metadata after successful output generation.
- Added validated whole-output-set cache hits for unchanged repository states.
- Kept normal scans without `--incremental` independent from persistent cache state.
- Kept cache metadata as execution metadata, not project evidence.
- Preserved byte-for-byte generated output parity between full scans and validated
  incremental cache hits for the same repository state and selected options.

## Output Compatibility

v1.4.0 remains a `schema_version: "1.0"` compatibility expansion. The base generated
files remain unchanged:

```text
.project-memory/project-map.json
.project-memory/evidence-index.jsonl
.project-memory/endpoints.md
.project-memory/agent-guide.md
```

When `--incremental` is selected and the scan writes the base output files, the scan
also writes cache metadata:

```text
.project-memory/cache/v1/manifest.json
.project-memory/cache/v1/inputs.jsonl
.project-memory/cache/v1/outputs.jsonl
```

Incremental cache support is additive:

- `project-map.json` does not gain cache-hit, cache-miss, timing, output-digest, or
  incremental-reuse fields.
- `evidence-index.jsonl` is unchanged.
- Cache metadata does not create evidence records.
- Profile artifacts remain governed by the v1.3 profile contract.
- Downstream consumers should continue to use `project-map.json` and
  `evidence-index.jsonl` as the stable machine-readable project-memory surface.

## Security Notes

The incremental cache boundary remains conservative:

- Cache behavior is opt-in through `--incremental`.
- Cache files are fixed under `.project-memory/cache/v1/`.
- Cache validation fails closed to normal full analysis when cache state is missing,
  stale, unsafe, corrupted, schema-mismatched, option-mismatched, config-mismatched,
  profile-mismatched, tool-version-mismatched, or otherwise unclear.
- Cache files and cache parent directories are not followed through symlinks, and
  symlinked or multi-link cache targets are not trusted or overwritten.
- Generated-source roots remain path-only metadata; generated-source contents are not
  read or fingerprinted.
- Cache metadata stores only bounded repository-relative paths, safe
  `.project-memory`-relative output/cache paths, SHA-256 hashes, byte counts,
  schema/tool metadata, selected profile names, and redacted option/config matching
  metadata.
- Cache metadata must not store source bodies, local document bodies, config contents,
  raw build-script bodies, generated-source contents, generated Markdown bodies, raw
  command logs, stack traces, local absolute paths, credentials, tokens, timing
  measurements, or secret-looking values.

## Validation

The v1.4.0 local release-prep validation passed:

- `mvn test`: 387 tests, 0 failures, 0 errors, 0 skipped.
- `mvn package`: 387 tests, 0 failures, 0 errors, 0 skipped, plus packaged CLI smoke.
- Packaged CLI smoke covered full scan, incremental cache warm-up, validated cache hit,
  stale cache fallback after an input change, no-profile behavior, and selected profile
  behavior with `target/agent-project-memory-1.4.0.jar`.
- Cache content inspection confirmed cache metadata stays within the documented
  metadata-only boundary.
- Packaged jar metadata inspection confirmed the CLI manifest entrypoint, filtered
  version resource, and Maven artifact metadata for `1.4.0`.
- In a clean local dry-run asset directory, `SHA256SUMS` was generated with the
  release asset filename only and verified successfully.
- `git diff --check`: passed.
- Public release-document marker audit passed.
- Risk-based release review for the final release-prep diff completed with no
  release-blocking findings remaining.

See the public v1.4 incremental cache evaluation summary for additional packaged CLI
validation detail:
[docs/development/evaluations/v1.4-incremental-cache_SUMMARY.md](../development/evaluations/v1.4-incremental-cache_SUMMARY.md).

## Not Included

v1.4.0 does not add:

- Partial per-module, per-analyzer, per-source-file, or per-section fact reuse.
- Source content caches, document body caches, config value caches, generated-source
  content caches, raw output transcript caches, or cache files outside
  `.project-memory/`.
- Cache status, output digest, or timing fields in `project-map.json`.
- Timing measurements in generated outputs or cache metadata.
- Generated-source content scanning.
- Generator execution.
- Maven lifecycle execution or Gradle task execution.
- Dependency, plugin, task, repository, generated-source graph, or effective build model
  reconstruction.
- Connectors, network access, telemetry, daemon/background service, remote cache, SaaS,
  web UI, repository chat, generic RAG, LLM calls in the core analyzer, or automatic
  code modification.
- Package-manager publication, signing, native images, container images, release
  automation, or artifact upload automation.

## Release Assets

- `agent-project-memory-1.4.0.jar`
- `SHA256SUMS`
