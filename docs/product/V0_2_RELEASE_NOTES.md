# v0.2 Release Notes

These notes summarize the planned v0.2 public release surface for
`agent-project-memory`. Detailed evaluation records remain in
[docs/development/evaluations/](../development/evaluations/).

## Release Status

v0.2 is the module-aware Maven release track for the local-first Java/Spring CLI.

These notes are prepared as release-readiness material. The release is not tagged,
published, or uploaded by these notes. The release version and README artifact
references are aligned to `0.2.0`, but creating the release tag, publishing GitHub
release assets, and uploading artifacts still require explicit maintainer approval.
The required repository-wide Codex Security release-candidate gate has completed for
the current v0.2 chain: the second scan at HEAD `451a75a` is the final discovery
baseline, `CS-APM-RC-006` through `CS-APM-RC-012` are fixed and checkpointed in
`53a4fab`, `3156238`, and `6b49306`, and no security blockers remain from that final
baseline. Do not run another open-ended repository-wide security scan before `v0.2.0`;
use targeted verification only for known findings or release-readiness checks.

## Scope

v0.2 turns the v0.1 single-module Maven scanner into module-aware project memory while
preserving deterministic analysis and evidence discipline.

Included in v0.2:

- Root aggregator `pom.xml` detection.
- Root `<modules>` parsing.
- Module path normalization and containment checks.
- Child `pom.xml` detection.
- Per-module production and test root detection for standard Maven roots.
- Module inventory in `project-map.json`.
- Public `schema_version: "0.2"` output with direct `module_id` fields on module-owned
  endpoint, warning, component, entity, and test facts.
- Module-aware execution for the existing Spring MVC endpoint, Spring component, JPA
  entity, hidden HTTP surface warning, and tests inventory analyzers.
- Maven module warnings for invalid, duplicate, missing-child-POM, nested, and
  unsupported module declarations.
- Module-aware `endpoints.md` grouping.
- Module-aware `agent-guide.md` orientation generated from structured facts and
  evidence only.
- Multi-module fixtures and golden checks.
- Real-project evaluation on pinned Maven multi-module Java/Spring projects.

The core analyzer remains deterministic and local-first. It does not call LLMs or
external services, and source code is not sent anywhere by default.

## Outputs

For a supported scan target, v0.2 continues to write:

```text
.project-memory/project-map.json
.project-memory/evidence-index.jsonl
.project-memory/endpoints.md
.project-memory/agent-guide.md
```

- `project-map.json` uses `schema_version: "0.2"` and includes module inventory plus
  fact-level module identity.
- `evidence-index.jsonl` keeps the existing evidence field set; Maven module discovery
  reuses `build_file` evidence.
- `endpoints.md` groups endpoint facts by module in deterministic module order.
- `agent-guide.md` summarizes module inventory and labels module-owned facts without
  inventing unsupported architecture.

The output shape is governed by
[docs/architecture/OUTPUT_CONTRACT.md](../architecture/OUTPUT_CONTRACT.md); evidence
semantics are governed by
[docs/architecture/EVIDENCE_MODEL.md](../architecture/EVIDENCE_MODEL.md).

## Compatibility Notes

- v0.2 changes the public JSON schema version from `"0.1"` to `"0.2"`.
- v0.2 single-module scans keep the same output files and preserve the v0.1 top-level
  `project.source_roots`, `project.test_roots`, and root-module fact ID shapes while
  adding `project.modules` and `module_id` fields.
- v0.2 multi-module scans keep `project.source_roots` and `project.test_roots` as sorted
  compatibility summaries; authoritative per-module roots live under
  `project.modules.items`.
- There is no valid inventory-only `schema_version: "0.2"` state. Normal public output
  must include both `project.modules` and direct `module_id` fields on every emitted
  module-owned endpoint, warning, component, entity, and test fact.

## Evaluation Summary

the v0.2 real-project evaluation evaluated the packaged CLI on two pinned public Maven multi-module
Java/Spring projects:

| Project | Ref | Result |
| --- | --- | --- |
| Spring PetClinic Microservices | `305a1f13e4f961001d4e6cb50a9db51dc3fc5967` | Produced deterministic module inventory, module-owned endpoint/component/entity/test facts, module-aware Markdown output, and resolving evidence references. |
| Spring Cloud OpenFeign | `d9f528bdc3f2ad6abebf4846258592d9786f42c7` | Produced supported-vs-unsupported module inventory, unsupported-module warnings, module-owned component/test facts, and resolving evidence references. |

Detailed report:

- [docs/development/evaluations/v0.2-module-aware-maven-real-projects.md](../development/evaluations/v0.2-module-aware-maven-real-projects.md)

No confirmed analyzer, output-contract, evidence-model, or Markdown generator defect was
found in that evaluation slice.

## Known Limitations

v0.2 intentionally does not implement:

- Gradle support.
- Maven profile resolution.
- Effective POM reconstruction.
- Dependency graph reconstruction.
- Maven execution during scan.
- Code generation during scan.
- Default scanning of generated source roots such as `target/generated-sources`.
- OpenAPI YAML/JSON parsing or generated API reconstruction.
- Full Java symbol solving.
- Runtime Spring handler mapping, bean graph, or component scanning reconstruction.
- Full ORM runtime behavior.
- Test execution, coverage, assertion analysis, CI analysis, call graphs, or complete
  tested-subject mapping.
- Local Markdown/document ingestion.
- Connectors for YouTrack, Jira, Confluence, GitHub, or GitLab.
- LLM calls in the core analyzer.
- SaaS, web UI, repository chat, generic RAG, or automatic code modification.

When a relation is inferred or uncertain, v0.2 preserves that status in generated
outputs instead of presenting it as a direct fact.

## Validation

The v0.2 release-readiness pass is expected to run:

```sh
git status --short
git diff --check
mvn test
mvn package
git diff --stat
```

The active release track's repository-wide Codex Security release-candidate scan
requirement has completed. The second scan at HEAD `451a75a` is the final v0.2
discovery baseline, its `CS-APM-RC-006` through `CS-APM-RC-012` findings are fixed and
checkpointed, and no additional open-ended repository-wide security scan is required
before `v0.2.0`.

Binary release artifact names should match the maintainer-approved release version in
`pom.xml` before final tagging or publishing.
