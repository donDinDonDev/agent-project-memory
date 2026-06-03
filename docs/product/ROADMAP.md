# Roadmap

## Current Status

`v0.1.0` is the first public local-first Java/Spring CLI release slice. Roadmap Stages 0
through 8 are closed as the historical v0.1 baseline.

Future work is organized by release tracks instead of extending the original v0.1 stage
list. Connector/import work remains post-v0.1 future work and is not started.

For strategic context, see
[POST_V0_1_STRATEGY.md](POST_V0_1_STRATEGY.md). For the next planned release track, see
the public v0.2 roadmap and release notes.

## Roadmap Principles

- Keep the core analyzer deterministic and local-first.
- Keep Java/Spring as the first-class analyzer focus through v1.0.
- Do not upload source code by default.
- Do not treat LLM-generated text as evidence.
- Keep extracted facts, inferred relations, uncertain signals, and document-backed claims
  distinct.
- Update `docs/architecture/OUTPUT_CONTRACT.md` for output field additions, removals,
  renames, or semantic changes.
- Update `docs/architecture/EVIDENCE_MODEL.md` for evidence shape or semantic changes.
- Evaluate meaningful analyzer expansions on pinned real projects before release.
- Keep connectors as optional adapters around the deterministic core.
- Keep optional AI layers separate from the core analyzer and non-authoritative.

## v0.1.0 Historical Baseline (Closed)

The v0.1 implementation includes:

- Java 21 Maven CLI with `scan <path>`.
- Root `pom.xml` Maven detection.
- Standard single-module Maven production and test roots.
- Spring MVC endpoint extraction from source-visible direct controller mappings.
- Source-visible interface-declared Spring MVC mappings when uniquely bindable to
  concrete handlers.
- Deterministic hidden HTTP surface warnings for OpenAPI/Swagger filenames, root Maven
  generator plugins, and direct `@RepositoryRestResource`.
- Direct Spring stereotype component inventory.
- Direct JPA entity inventory with conservative mapped-superclass identifier support.
- Minimal deterministic tests inventory with naming-convention tested-subject
  inferences.
- `project-map.json`, `evidence-index.jsonl`, `endpoints.md`, and `agent-guide.md`.
- Stage 8 evaluation on pinned open-source Spring projects.

The detailed v0.1 scope and limitations are documented in
[V0_1_RELEASE_NOTES.md](V0_1_RELEASE_NOTES.md) and [MVP_SPEC.md](MVP_SPEC.md).

## v0.2.0: Module-Aware Maven Support (Next)

Product outcome: make project memory module-aware for real Maven Java/Spring
repositories while preserving deterministic analysis and evidence discipline.

Expected scope:

- Root aggregator `pom.xml` detection.
- `<modules>` parsing.
- Module path resolution.
- Child `pom.xml` detection.
- Per-module source and test roots.
- Existing analyzers run per supported module.
- Generated facts can be traced to module identity.
- `project-map.json` gains module inventory after contract design.
- `endpoints.md` and `agent-guide.md` include module orientation after generator updates.
- Multi-module fixtures and golden outputs.
- Real-project multi-module evaluation.

Non-goals:

- Gradle.
- Maven profile resolution.
- Effective POM reconstruction.
- Dependency graph reconstruction.
- Running Maven during scan.
- Generated source scanning by default.
- OpenAPI parsing.
- Full symbol solving.
- Runtime Spring model reconstruction.

Planned goal sequence:

1. Design module-aware output contract.
2. Implement Maven module discovery.
3. Run existing analyzers module-aware.
4. Update Markdown generators.
5. Add multi-module fixtures and golden checks.
6. Evaluate on pinned real Maven multi-module projects.
7. Prepare v0.2 release materials.

## v0.3.0: Build And Configuration Model

Expected direction:

- Per-module Maven metadata where source-visible.
- Packaging, parent, dependency, and plugin summaries.
- Spring Boot application signals.
- Resource root and application config discovery.
- Build warnings for generated-source and OpenAPI/Swagger plugin signals.

Non-goals include effective POM reconstruction, dependency repository resolution, Maven
execution, runtime config resolution, and secret extraction.

## v0.4.0: Declared And Generated API Surface

Expected direction:

- Separate source-visible Spring MVC endpoints from spec-declared or generated API
  surfaces.
- Discover OpenAPI/Swagger files.
- Optionally parse minimal OpenAPI operations as contract facts after evidence design.
- Keep OpenAPI operations separate from implemented endpoint facts unless code evidence
  supports the relation.
- Keep generated-source scanning explicit, not default.

## v0.5.0: Deeper Spring Application Surface

Expected direction:

- Repository interface signals.
- `@ConfigurationProperties`.
- `@Bean` methods.
- `@Transactional`, `@Scheduled`, and listener annotations.
- Security and messaging entrypoint warnings where source-visible.

The analyzer must avoid claiming a full runtime bean graph, autowiring graph, security
policy, or messaging topology.

## v0.6.0: JPA And Domain Model Deepening

Expected direction:

- Additional source-visible JPA annotations such as `@Column`, `@JoinColumn`,
  `@Embedded`, `@Embeddable`, `@EmbeddedId`, `@IdClass`, `@Enumerated`,
  `@GeneratedValue`, and `@Version`.
- Better relationship uncertainty handling.
- Repository-to-entity inferred relations when evidence supports them.

Non-goals include generated schema reconstruction, database introspection, runtime
Hibernate metadata, and full JPQL semantics.

## v0.7.0: Tests, Quality, And Change-Risk Map

Expected direction:

- Better test class and method inventory.
- Spring test slice signals such as `@SpringBootTest`, `@WebMvcTest`, and `@DataJpaTest`.
- Conservative source-visible tested-subject inference.
- Test-gap and change-risk signals labeled as inferred or uncertain.

Non-goals include coverage claims, mutation testing, behavioral assertion understanding,
CI result claims, and full call graph reconstruction.

## v0.8.0: Local Markdown And Document Ingestion

Expected direction:

- Local Markdown discovery.
- Configurable include/exclude.
- Heading or chunk inventory.
- `document` evidence records after evidence contract design.
- Code-doc reconciliation warnings.

Documents must remain a separate evidence source. Document-only claims must not override
deterministic code facts.

## v0.9.0: CLI, Config, Performance, And Distribution Readiness

Expected direction:

- Config file design.
- Include/exclude patterns.
- Explicit enable flags for later optional scan modes.
- Better CLI help, exit codes, and diagnostics.
- Large-repository performance baseline.
- Release artifact and checksum workflow.
- Installation options research.

## v1.0.0: Stable Java/Spring Local-First Project Memory

v1.0 should stabilize the public Java/Spring Maven project-memory product. It is a
contract and reliability milestone, not just a feature milestone.

Expected readiness:

- Stable module-aware Maven support.
- Stable build/config model.
- Stable API surface taxonomy.
- Useful Spring/JPA/test inventories.
- Local Markdown ingestion either stable or clearly experimental.
- Output compatibility rules.
- Expanded evaluation corpus.
- Acceptable CLI UX and installation path.
- Changelog, release checklist, and public release discipline.

## v1.x: Stable Product Expansion

Possible tracks:

- Gradle Java/Spring support.
- Generated-source and codegen maturity.
- Agent output profiles.
- Incremental scan and performance.
- Lightweight relation graph.
- Local query/read-only explorer.
- Security and secrets safety.
- Public adoption polish.
- v2 architecture preparation.

## v2.x: Extensible Platform, Adapters, And Optional AI

Expected direction:

- Stable input adapter API.
- Normalized source document model.
- Connector provenance model.
- Local import format.
- GitHub/GitLab/Jira/YouTrack/Confluence adapters when explicitly enabled.
- Optional AI presentation layer that reads deterministic memory and never creates
  authoritative evidence.
- Read-only agent integrations such as MCP or agent prompt bundles.
- Workspace and change-impact workflows.

The core analyzer must continue to run without adapters, network access, or AI.

## v3.0.0: Agent-Native Project Memory Platform

The long-term target is a mature local-first evidence-backed project memory platform for
developers and AI coding agents, with deterministic analyzers, explicit evidence, stable
adapter/plugin APIs, optional non-authoritative AI presentation, and agent-native
read-only workflows.
