# Post-v0.1 Strategy

This document describes the public post-v0.1 direction for `agent-project-memory`.
It is a strategy guide, not a release commitment. Concrete behavior changes remain
governed by `docs/product/ROADMAP.md`, `docs/architecture/OUTPUT_CONTRACT.md`,
`docs/architecture/EVIDENCE_MODEL.md`, tests, and release notes.

## Strategic Thesis

`agent-project-memory` should grow as a deterministic local project-memory engine for
Java/Spring codebases and AI coding agents.

The project should not become a generic AI documentation generator, repository chatbot,
hosted RAG system, or SaaS scanner. Its core value is evidence-backed memory:

- source-visible facts are extracted deterministically;
- important facts reference evidence;
- inferred relations are labeled as inferred;
- uncertain signals are labeled as uncertain;
- document-backed claims remain distinct from code-backed facts;
- optional AI can explain or present facts later, but cannot become evidence.

## Product Invariants

- The core analyzer runs locally.
- Source code is not uploaded by default.
- The core analyzer does not require external APIs.
- LLM-generated text is never authoritative evidence.
- Output contracts are explicit and versioned.
- Evidence semantics are explicit and documented.
- New analyzer behavior has focused tests.
- Meaningful analyzer expansions are evaluated on pinned real projects.
- Future connectors are adapters around the core, not dependencies inside it.

## Development Order

Future work should build from the inside out:

1. Project structure truth.
2. Source-visible application facts.
3. Declared and generated API surface.
4. Local project documents.
5. Tests, quality, and change-risk intelligence.
6. CLI, config, performance, and distribution maturity.
7. Optional adapters and connectors.
8. Optional AI presentation and agent integrations.
9. Platform/plugin ecosystem.

This order keeps external integrations and AI presentation dependent on a strong
deterministic project model, not the other way around.

## Public And Local Planning

Public documents should stay concise and useful for users and contributors:

- `README.md` explains the product and current usage.
- `docs/product/MVP_SPEC.md` defines the v0.1 baseline.
- `docs/product/ROADMAP.md` describes release tracks.
- the public v0.2 roadmap and release notes describes the next planned release
  track after v0.1 and remains the v0.2 historical plan.
- the public v0.3 roadmap and release notes describes the planned v0.3 build and
  configuration model.
- the public v0.4 roadmap and release notes describes the planned v0.4 declared and
  generated API surface taxonomy.
- `docs/architecture/OUTPUT_CONTRACT.md` and `docs/architecture/EVIDENCE_MODEL.md`
  govern generated output and evidence semantics.

Maintainer-only planning can be more detailed and may live in ignored local files such
as `AGENTS.local.md` or `docs/maintainer/`.

## v0.x Direction

The v0.x line should harden the local Java/Spring analyzer before declaring a stable
public contract:

- v0.2: module-aware Maven support.
- v0.3: build and configuration model.
- v0.4: declared and generated API surface taxonomy.
- v0.5: deeper Spring application surface.
- v0.6: deeper JPA/domain model.
- v0.7: tests, quality, and change-risk map.
- v0.8: local Markdown/document ingestion.
- v0.9: CLI/config/performance/distribution readiness.

The exact sequence may change when evaluation finds higher-priority correctness or
contract work.

## v1.0 Direction

v1.0 should mean that Java/Spring Maven project memory is stable enough for public use:

- output compatibility rules are clear;
- evidence semantics are stable;
- generated facts are deterministic and evidence-backed;
- evaluation coverage is broad enough to trust common Java/Spring shapes;
- CLI UX and installation are acceptable;
- release, changelog, and review discipline are in place.

## v2.x And v3.x Direction

v2.x may introduce adapter APIs, connector provenance, local imports, optional AI
presentation, and read-only agent integrations. These layers must remain optional and
non-authoritative.

v3.0 is the long-term platform target: a local-first evidence-backed project memory
platform with deterministic analyzers, stable adapter/plugin APIs, optional AI
presentation, and agent-native workflows.

## Explicit Non-Goals

The post-v0.1 roadmap should continue to avoid:

- source upload by default;
- LLM calls in the core analyzer;
- treating LLM output as evidence;
- SaaS-first positioning;
- repository chat as the core product;
- generic RAG as the core product;
- automatic code modification as the core product;
- broad multi-language expansion before the Java/Spring core is stable.
