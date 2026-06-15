# v1.2.0 Release Notes

Release date: 2026-06-15

`agent-project-memory` v1.2.0 makes generated-source and code generation handling more
explicit through metadata-only output while preserving the default behavior that
generated source contents are not read.

The release keeps the local-first deterministic analyzer boundary: no source upload, no
generator execution, no Maven or Gradle task execution, no dependency or plugin
resolution, no connectors, and no LLM calls in the core analyzer.

## Highlights

- Added a top-level `generated_sources` object to `project-map.json` with explicit
  policy metadata for disabled generated-source content scanning.
- Added bounded generated-root inventory for recognized Maven and Gradle generated
  source root families.
- Marked generated roots with `content_status: "not_scanned"` and
  `source_origin: "metadata_only"` so generated-source path observations stay distinct
  from scanned human-authored source roots.
- Linked generator and codegen warning references to generated-source metadata without
  creating endpoint, component, Spring/JPA, test, or API implementation facts from
  generated files.
- Kept `features.generated_sources: false` as a valid reserved config value and
  `features.generated_sources: true` as invalid config.
- Added diagnostics for unsafe generated-source root candidates and bounded candidate
  caps.
- Added generated-source/codegen orientation to `agent-guide.md`.

## Output Compatibility

v1.2.0 remains a `schema_version: "1.0"` compatibility expansion. The generated files
are unchanged:

- `.project-memory/project-map.json`
- `.project-memory/evidence-index.jsonl`
- `.project-memory/endpoints.md`
- `.project-memory/agent-guide.md`

The new generated-source metadata is additive. Existing Maven, Gradle, Spring MVC,
Spring application surface, JPA/domain, tests, documents, API-spec, warning, diagnostic,
and evidence semantics are preserved.

Generated-source root inventory uses existing evidence semantics:

- `path_signal` evidence for generated-root path observations.
- `build_file` evidence for source-visible generator or codegen declarations.

Generated-source file contents do not create scanned-content evidence in this release.

## Security Notes

The generated-source boundary remains conservative:

- Generated source contents are not read.
- Generated files do not create endpoint, component, Spring/JPA, test, or API
  implementation facts.
- Maven, Gradle, generators, wrappers, daemons, tasks, plugins, and dependency
  resolution are not executed.
- Symlinks are not followed.
- Raw config values, raw user path patterns, local absolute paths, generated output
  contents, and arbitrary plugin configuration bodies are not serialized into outputs.
- Invalid generated-source content-scan config fails before outputs are created.

## Validation

The v1.2.0 local release-prep validation passed:

- `mvn test`: 353 tests, 0 failures, 0 errors, 0 skipped.
- `mvn package`: 353 tests, 0 failures, 0 errors, 0 skipped, plus packaged CLI scan,
  help, and version validation.
- Separate packaged CLI smoke covered a generated-source fixture with
  `target/agent-project-memory-1.2.0.jar`, proving generated roots are reported as
  metadata-only, generated Java content is not emitted, and no generated endpoint facts
  are created.
- Packaged CLI invalid-config smoke confirmed `features.generated_sources: true` exits
  with code `4` before creating `.project-memory/`.
- Packaged jar metadata inspection confirmed the CLI manifest entrypoint, filtered
  version resource, and Maven artifact metadata for `1.2.0`.
- `SHA256SUMS` was generated with the release asset filename only and verified
  successfully.
- `git diff --check`: passed.
- Public release-document marker audit passed.
- Risk-based release review for the final release-prep diff completed with no
  release-blocking findings remaining.

## Not Included

v1.2.0 does not add:

- Generated-source content scanning, whether default or opt-in.
- Generator execution.
- Maven lifecycle execution or Gradle task execution.
- Dependency, plugin, task, repository, generated-source graph, or effective build model
  reconstruction.
- Runtime API freshness checks.
- Generated client or server API reconstruction.
- Automatic OpenAPI/source implementation matching.
- Custom Gradle `sourceSets` support.
- Connectors, network access, telemetry, SaaS, web UI, repository chat, generic RAG, LLM
  calls in the core analyzer, or automatic code modification.

## Expected Release Assets

- `agent-project-memory-1.2.0.jar`
- `SHA256SUMS`
