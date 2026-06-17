# v1.8.0 Release Notes

Release date: 2026-06-17

`agent-project-memory` v1.8.0 improves the public adoption surface for first-time users
and contributors while preserving the local-first analyzer boundary and the existing
output and evidence contracts.

The release is documentation and onboarding polish: no analyzer behavior changes, no new
CLI commands or flags, no generated output schema changes, no evidence semantic changes,
no package-manager publication, and no release automation.

## Highlights

- Added a public generated-output example snapshot under
  `examples/stage3-project-map/` so readers can inspect the base `.project-memory/`
  output set without first running the CLI.
- Documented how to regenerate and compare that example from the packaged jar using the
  deterministic fake `stage3-project-map` fixture/golden pair.
- Linked the example from the README public documentation map.
- Expanded `CONTRIBUTING.md` with bounded contribution paths for docs-only,
  examples/templates, fixtures/tests, analyzer behavior, output/evidence contract, and
  release-prep changes.
- Refined the bug report, feature request, and PR templates so reports and reviews
  capture version, command, generated-file, evidence-ID, contract-impact, validation,
  and sensitive-data context.

## Output Compatibility

v1.8.0 remains a `schema_version: "1.0"` compatibility release for generated
project-memory artifacts. It does not add, remove, rename, or reinterpret generated
output fields, evidence fields, evidence types, `project-map.json` schema markers, or
`project-graph.json` graph schema markers.

The base generated files remain:

```text
.project-memory/project-map.json
.project-memory/project-graph.json
.project-memory/evidence-index.jsonl
.project-memory/endpoints.md
.project-memory/agent-guide.md
```

The checked-in example snapshot is a documentation aid:

- It is not project evidence.
- It is not a second schema contract.
- It is not a stable Markdown parser contract.
- Authoritative generated-output semantics remain in
  [OUTPUT_CONTRACT.md](../architecture/OUTPUT_CONTRACT.md).
- Authoritative evidence semantics remain in
  [EVIDENCE_MODEL.md](../architecture/EVIDENCE_MODEL.md).

## Contributor And Reporting Notes

The contributor path remains conservative and scope-bounded:

- Docs-only, examples/template, fixture/golden/test, analyzer behavior,
  output/evidence contract, and release-prep changes are described as separate
  contribution paths with different validation expectations.
- Good-first contribution guidance points toward small docs, example, template, fixture,
  and focused-test changes without depending on remote labels, milestones, or project
  boards.
- Issue templates ask for enough generated-output and evidence context to reproduce
  reports without asking users to post secrets, private repository data, or vulnerability
  details in public issues.
- The PR template keeps output/evidence contract synchronization, example verification,
  changelog needs, public-doc safety, and future-scope boundaries visible during review.

## Validation

The v1.8.0 local release-prep validation passed:

- `mvn test`: 448 tests, 0 failures, 0 errors, 0 skipped.
- `mvn package`: 448 tests, 0 failures, 0 errors, 0 skipped, including the packaged
  CLI smoke bound to the Maven package lifecycle.
- Additional packaged CLI smoke covered representative scan, query, help, and version
  behavior with `target/agent-project-memory-1.8.0.jar`.
- The public `stage3-project-map` example snapshot was compared against packaged CLI
  output regenerated from `src/test/resources/fixtures/stage3-project-map`.
- `git diff --check`: passed.
- Public release-document marker audit passed.

## Not Included

v1.8.0 does not add:

- Analyzer features, output field changes, evidence field changes, evidence semantic
  changes, stable JSON query output, new CLI commands, or new CLI flags.
- Release automation, package-manager publication, installed-command distribution,
  signing, native images, container images, or additional distribution channels.
- Connectors, adapter APIs, plugin platforms, MCP/server surfaces, source upload,
  external service calls, telemetry, SaaS, web UI, repository chat, generic RAG,
  embeddings, LLM calls in the core analyzer or query layer, or automatic code
  modification.
- Generated-source content scanning, default symlink following, Maven or Gradle
  execution during scans, dependency resolution, runtime Spring reconstruction,
  vulnerability scanning, or security correctness claims.
- Real secrets, credentials, private repository data, local machine paths, raw command
  transcripts, or maintainer-only workflow details in public examples, templates, docs,
  release notes, or release metadata.

## Release Assets

- `agent-project-memory-1.8.0.jar`
- `SHA256SUMS`
