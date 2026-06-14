# v1.1.0 Release Notes

Release date: 2026-06-14

`agent-project-memory` v1.1.0 adds bounded static Gradle Java/Spring layout support
while preserving the stable v1.0 Maven behavior and evidence semantics. The release
keeps the core analyzer local-first and deterministic; it does not execute Gradle,
evaluate build scripts, resolve dependencies, add connectors, upload source, or use LLM
calls in the core analyzer.

## Highlights

- Gradle-only and mixed Maven/Gradle scans can now participate in the same
  `.project-memory/` output set while generated `project-map.json` stays on
  `schema_version: "1.0"`.
- The scanner accepts local root `settings.gradle`, `settings.gradle.kts`,
  `build.gradle`, and `build.gradle.kts` files, plus project `build.gradle` and
  `build.gradle.kts` files under supported Gradle project directories.
- Simple static string-literal Gradle settings includes are detected for multi-project
  layouts, including the same bounded subset in `settings.gradle.kts`.
- Standard Gradle Java/Spring roots are detected:
  `src/main/java`, `src/test/java`, `src/main/resources`, and `src/test/resources`.
- Gradle `build_file` evidence records identify accepted Gradle build files and static
  include declarations with bounded excerpts and repository-relative paths.
- Gradle warnings and scan diagnostics record unsupported dynamic includes, duplicate
  project paths, missing project directories, unsupported modules, visible but
  not-analyzed `sourceSets`, unsupported `projectDir` remapping, and skipped Gradle
  build-file reads without turning them into runtime Gradle claims.
- Existing Spring MVC, component, Spring application surface, JPA/domain, API surface,
  document, tests, quality, and guide rendering paths run over supported Gradle standard
  roots without changing their source-visible fact semantics.

## Output Compatibility

The generated files remain:

```text
.project-memory/project-map.json
.project-memory/evidence-index.jsonl
.project-memory/endpoints.md
.project-memory/agent-guide.md
```

The v1.1 Gradle expansion is additive:

- `project-map.json` remains on `schema_version: "1.0"`.
- Pure Maven output and evidence semantics are preserved.
- Gradle and mixed Maven/Gradle scans may add `project.build.root_build_files[]`,
  module `build_systems`, `gradle_project_path`, and
  `project.modules.items[].build_config.gradle`.
- Gradle-only modules keep Maven compatibility fields such as `pom_path` and
  `pom_evidence_ids`, but those fields remain `null` or empty and are not reinterpreted
  as Gradle build-file references.
- Existing Maven fields, Maven warning IDs, Maven evidence ID conventions, JSON/JSONL
  nullability, and evidence semantics are unchanged.

Consumers that already accept the v1.0 output marker should continue to treat
`schema_version: "1.0"` as the stable-line machine-readable surface and ignore unknown
additive fields when practical. Regenerate all four `.project-memory/` files together so
JSON facts, evidence IDs, and Markdown evidence references stay aligned.

## Validation

The v1.1.0 local release-prep validation passed:

- `mvn test`: 350 tests, 0 failures, 0 errors, 0 skipped.
- `mvn package`: 350 tests, 0 failures, 0 errors, 0 skipped, plus packaged CLI scan,
  help, and version validation.
- Separate packaged CLI smoke covered Maven and Gradle fixture scans with
  `target/agent-project-memory-1.1.0.jar`.
- The packaged scans generated `project-map.json`, `endpoints.md`,
  `evidence-index.jsonl`, and `agent-guide.md` with `schema_version: "1.0"`.
- Packaged jar metadata inspection confirmed the CLI manifest entrypoint and filtered
  version resource for `1.1.0`.
- `SHA256SUMS` was generated with the release asset filename only and verified
  successfully.
- `git diff --check`: passed.
- Public release-document marker audit passed.
- Risk-based security review for the Gradle parser, path, evidence, and output changes
  completed with no release-blocking findings remaining.

Public evaluation summary:
[docs/development/evaluations/v1.1-gradle-java-spring_SUMMARY.md][v1.1-eval].

[v1.1-eval]: ../development/evaluations/v1.1-gradle-java-spring_SUMMARY.md

## Security Notes

v1.1.0 keeps the deterministic local analyzer boundary:

- no source upload or external service dependency in the core analyzer;
- no connector configuration, network-loaded config, credentials, telemetry, update
  checks, plugin loading, package publication, global machine config, or user-home
  config discovery;
- no Gradle execution, wrapper invocation, Gradle Tooling API use, daemon or task
  execution, dynamic buildscript evaluation, dependency resolution, plugin resolution,
  repository access, lockfile interpretation, or effective Gradle model reconstruction;
- no generated-source scanning by default and no symlink following by default;
- no raw build-script bodies, dependency blocks, plugin configuration, config values,
  raw include/exclude patterns, secret-looking values, document bodies, stack traces,
  local absolute paths, or generated output contents in generated project memory;
- no tool-config evidence records;
- no package-manager publishing, signing, credentials, upload automation, or remote
  release-state change in artifact verification;
- no SaaS, web UI, repository chat, generic RAG, LLM calls in the core analyzer, or
  automatic code modification.

Gradle support is intentionally static and source-visible. Unsupported or unsafe Gradle
inputs degrade to bounded warnings or scan diagnostics rather than dynamic claims.

## Not Included

- Gradle execution, Gradle wrapper invocation, Gradle Tooling API use, daemon or task
  execution, dependency resolution, plugin resolution, repository access, lockfile
  interpretation, effective Gradle model reconstruction, or dynamic buildscript
  evaluation.
- Custom `sourceSets` root emission, custom `projectDir` mapping, `includeBuild`,
  `includeFlat`, generated-source graphs, broad Kotlin DSL semantic parsing, or Kotlin
  source analysis.
- New Maven dependency/plugin behavior, Maven profile resolution, effective POM
  reconstruction, recursive nested Maven module discovery, or Maven execution.
- GitHub/GitLab/Jira/YouTrack/Confluence connectors, network access, external
  documentation fetching, telemetry, update checks, credentials, package-manager
  installation channels, native images, container images, signing, or release upload
  automation.
- SaaS, web UI, repository chat, generic RAG, LLM calls in the core analyzer, or
  automatic code modification.

## Known Follow-Ups

The v1.1 validation records bounded future work that is not required for this release:

- Keep custom Gradle `sourceSets`, `projectDir` remapping, included builds, dependency
  graphs, plugin graphs, and task graphs parked until a separate static contract and
  security review justify them.
- Add future pinned evaluation targets with larger or more diverse Gradle multi-project
  layouts before making stronger Gradle corpus claims.
- Continue treating package-manager distribution channels as separate release tracks
  that need channel-specific installation, update, checksum, signing, and release
  process design.

## Release Assets

- `agent-project-memory-1.1.0.jar`
- `SHA256SUMS`
