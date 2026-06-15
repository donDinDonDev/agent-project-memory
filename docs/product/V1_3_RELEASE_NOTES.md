# v1.3.0 Release Notes

Release date: 2026-06-15

`agent-project-memory` v1.3.0 adds deterministic, opt-in agent output profile
artifacts for coding-agent consumption while preserving the local-first analyzer and
the existing project-memory fact model.

The release keeps the core analyzer deterministic: no source upload, no connectors, no
editor integration, no repository-file modification, and no LLM calls in the core
analyzer.

## Highlights

- Added repeatable `scan <path> --agent-profile <profile>` selection for supported
  profiles.
- Supported canonical profile selectors are `codex`, `claude`, `cursor`, and
  `generic`; `all` selects every supported profile.
- Kept duplicate profile selectors idempotent so each canonical profile is generated at
  most once.
- Added `.project-memory/agent-profiles/manifest.json` for generated-profile detection.
- Added selected profile Markdown files under `.project-memory/agent-profiles/`.
- Generated profile Markdown from existing structured project facts and existing
  evidence references only.
- Preserved default scan behavior: scans without `--agent-profile` do not create
  profile artifacts.

## Output Compatibility

v1.3.0 remains a `schema_version: "1.0"` compatibility expansion. The base generated
files remain unchanged:

```text
.project-memory/project-map.json
.project-memory/evidence-index.jsonl
.project-memory/endpoints.md
.project-memory/agent-guide.md
```

When a supported profile is selected and the base output files are written, the scan
also writes:

```text
.project-memory/agent-profiles/manifest.json
.project-memory/agent-profiles/<selected-profile>.md
```

Profile generation is additive:

- `project-map.json` does not gain profile-generation fields.
- `evidence-index.jsonl` is unchanged.
- Profile artifacts do not create evidence records.
- `agent-guide.md` remains the generic deterministic orientation guide.
- Downstream automation should use `manifest.json` and documented profile filenames for
  profile detection rather than parsing profile Markdown as a stable machine-readable
  API.

## Security Notes

The profile boundary remains conservative:

- Profile generation is opt-in.
- Profile artifacts are generated presentations, not evidence.
- Profile files reference existing evidence IDs and source locations only.
- Profile generation does not call LLMs, external services, connectors, editors, local
  agent runtimes, build tools, or generated-source scanners.
- Profile generation does not modify root repository instruction/config files such as
  `AGENTS.md`, `CLAUDE.md`, Cursor rules, IDE settings, source files, docs, or config
  files.
- Profile outputs must not serialize source bodies, local document bodies, config
  contents, arbitrary build-script bodies, generated-source contents, raw command
  transcripts, stack traces, local absolute paths, credentials, tokens, or
  secret-looking values.

## Validation

The v1.3.0 local release-prep validation passed:

- `mvn test`: 363 tests, 0 failures, 0 errors, 0 skipped.
- `mvn package`: 363 tests, 0 failures, 0 errors, 0 skipped, plus packaged CLI scan,
  help, and version validation.
- Separate packaged CLI profile smoke covered `--agent-profile all` with
  `target/agent-project-memory-1.3.0.jar`, proving the profile manifest and all
  supported profile Markdown files are generated under `.project-memory/agent-profiles/`.
- Separate packaged CLI no-profile smoke confirmed that scans without `--agent-profile`
  keep the default output set and do not create `agent-profiles/`.
- Packaged jar metadata inspection confirmed the CLI manifest entrypoint, filtered
  version resource, and Maven artifact metadata for `1.3.0`.
- `SHA256SUMS` was generated with the release asset filename only and verified
  successfully.
- `git diff --check`: passed.
- Public release-document marker audit passed.
- Risk-based release review for the final release-prep diff completed with no
  release-blocking findings remaining.

## Not Included

v1.3.0 does not add:

- LLM calls or AI-generated summaries.
- Source upload, external service calls, connectors, network access, telemetry, editor
  integrations, MCP/server surfaces, or plugin platforms.
- Repository chat, generic RAG, embeddings, or vector stores.
- Automatic modification of `AGENTS.md`, `CLAUDE.md`, Cursor files, IDE settings,
  source files, docs, config files, or other repository files.
- Generated-source content scanning, generator execution, Maven lifecycle execution, or
  Gradle task execution.
- Package-manager publication, signing, native images, container images, release
  automation, or artifact upload automation.
- SaaS, web UI, or automatic code modification.

## Release Assets

- `agent-project-memory-1.3.0.jar`
- `SHA256SUMS`
