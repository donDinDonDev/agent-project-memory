# v1.5.0 Release Notes

Release date: 2026-06-16

`agent-project-memory` v1.5.0 adds a lightweight deterministic relation graph artifact
over existing project-memory facts while preserving the local-first analyzer boundary
and the existing evidence model.

The release keeps graph output as navigation metadata: no full call graph, no runtime
dependency graph, no runtime Spring graph, no source/spec agreement proof, no document
freshness scoring, no connectors, and no LLM calls in the core analyzer.

## Highlights

- Added `.project-memory/project-graph.json` as a separate machine-readable graph
  artifact with `graph_schema_version: "1.0"`.
- Added deterministic graph nodes for existing project-memory facts such as modules,
  packages, source-visible types, endpoints, declared OpenAPI operations, JPA entities
  and embeddables, repository signals, tests, local Markdown document structure,
  generated-source metadata rows, and warnings.
- Added direct and structural `owns` and `declares` edges with explicit non-evidence
  derivation metadata when an edge is derived from current `project-map.json` fields.
- Added conservative inferred `repository_entity` and `tested_subject` graph edges only
  when existing relation rows resolve to concrete graph targets.
- Preserved unsupported, ambiguous, not-detected, not-analyzed, uncertain, and no-target
  relation rows under `relation_statuses[]` instead of promoting them to graph edges.
- Included `project-graph.json` in incremental cache output fingerprints as
  `output_kind: "project_graph"` so validated cache hits verify the graph digest and
  size before reusing a generated output set.

## Output Compatibility

v1.5.0 remains a `schema_version: "1.0"` compatibility expansion for
`project-map.json`. The graph is a separate artifact with its own graph schema marker:

```text
.project-memory/project-map.json
.project-memory/project-graph.json
.project-memory/evidence-index.jsonl
.project-memory/endpoints.md
.project-memory/agent-guide.md
```

Graph output is additive:

- `project-map.json` does not gain a top-level graph section.
- `evidence-index.jsonl` is unchanged.
- Graph nodes and edges reference existing `evidence_ids` when the underlying fact or
  relation already has evidence.
- Evidence records are not duplicated as graph nodes.
- Structural graph edges use `derivation` metadata instead of fabricated evidence.
- `agent-guide.md` and selected agent profile Markdown are not required to render graph
  content in the initial graph contract.
- Normal scans without `--incremental` continue to ignore persistent cache state.

## Security Notes

The graph boundary remains conservative:

- Graph output is generated only from existing deterministic facts, existing
  relation/status rows, existing evidence IDs, and existing document reconciliation
  hints.
- Graph warnings are construction diagnostics for `project-graph.json`; they are not
  project evidence, security findings, vulnerabilities, or runtime claims.
- Graph derivation metadata is a reproducibility explanation, not evidence.
- Document reconciliation graph material remains low-confidence uncertain inspection
  metadata and does not prove stale documentation, missing documentation,
  source/document agreement, implementation truth, or code ownership.
- Generated-source graph material remains metadata-only and does not read
  generated-source contents.
- Graph output must not serialize source bodies, local document bodies, config
  contents, raw build-script bodies, generated-source contents, generated Markdown
  bodies, raw command logs, stack traces, local absolute paths, credentials, tokens, or
  secret-looking values.

## Validation

The v1.5.0 local release-prep validation passed:

- `mvn test`: 395 tests, 0 failures, 0 errors, 0 skipped.
- `mvn package`: 395 tests, 0 failures, 0 errors, 0 skipped, plus packaged CLI smoke.
- Packaged CLI smoke covered base scan output, graph artifact generation, selected
  profile output, incremental warm-up/cache-hit/invalidation behavior with graph output
  in the selected output set, help, and version behavior with
  `target/agent-project-memory-1.5.0.jar`.
- Graph structural checks covered duplicate node and edge IDs, dangling edge
  references, unresolved graph evidence references, structural derivation metadata, and
  deterministic repeated digests for release-candidate artifacts.
- Packaged jar metadata inspection confirmed the CLI manifest entrypoint, filtered
  version resource, and Maven artifact metadata for `1.5.0`.
- `SHA256SUMS` was generated with the release asset filename only and verified
  successfully before publication; downloaded release assets also verified successfully
  after publication.
- `git diff --check`: passed.
- Public release-document marker audit passed.
- Risk-based release review for the final release-prep diff completed with no
  release-blocking findings remaining.

## Not Included

v1.5.0 does not add:

- Full call graphs.
- Runtime dependency graphs.
- Runtime Spring bean graphs, autowiring graphs, component scanning reconstruction, or
  handler mapping reconstruction.
- Complete type solving, classpath solving, build execution, Maven effective model
  reconstruction, Gradle task execution, or dependency/plugin resolution.
- Source/spec agreement scoring or implementation-coverage claims.
- Documentation freshness scoring, documentation correctness claims, or document truth
  upgrades over code-backed facts.
- Test coverage, CI, assertion, mutation-testing, test execution, or runtime
  tested-subject proof.
- Query/read-only explorer commands or impact analysis commands.
- Generated-source content scanning.
- Connectors, network access, telemetry, SaaS, web UI, repository chat, generic RAG,
  LLM calls in the core analyzer, or automatic code modification.
- Package-manager publication, signing, native images, container images, or release
  automation.

## Release Assets

- `agent-project-memory-1.5.0.jar`
- `SHA256SUMS`
