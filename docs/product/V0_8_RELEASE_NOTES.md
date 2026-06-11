# v0.8.0 Release Notes

Release date: 2026-06-11

`agent-project-memory` v0.8.0 adds conservative local Markdown/project-document
ingestion for local Java/Spring Maven repositories. It keeps document-backed context
separate from code-backed facts, inferred relations, uncertain signals, and warnings
without claiming that documents are authoritative over source code.

## Highlights

- Deterministic default-scope local Markdown discovery for root README files, supported
  module README files, and bounded `docs/**/*.md`, `adr/**/*.md`, and `adrs/**/*.md`
  trees.
- Built-in exclusions for hidden, generated, dependency, output, private/internal,
  maintainer-like, and secret-like paths, plus repository-relative path containment and
  a default no-symlink-following policy.
- A top-level `documents` object in `project-map.json` with discovery policy metadata,
  document inventory, optional module ownership, first-heading or filename-derived
  titles, ATX heading references, and bounded chunk references.
- Resolving `document` evidence records for accepted local Markdown file, heading,
  chunk, and bounded document-side reconciliation mention observations.
- Conservative `documents.reconciliation` rows for bounded document-only endpoint-like
  mentions, document-only module references, source-backed API facts without an obvious
  default-scope document mention, and module facts without an obvious default-scope
  document mention.
- Compact `agent-guide.md` local project documentation rendering from structured
  `documents` facts and resolving evidence only.
- Real-project evaluation and security/contract review for the accumulated v0.8 local
  document ingestion boundary.

## Output Compatibility

v0.8.0 moves normal generated `project-map.json` output to `schema_version: "0.8"`.
The v0.8 contract builds on the v0.7 tests/quality contract, v0.6 JPA/domain contract,
v0.5 Spring application surface contract, v0.4 API surface contract, and v0.3
module-aware build/config contract:

- The same output files remain under `.project-memory/`.
- A new top-level `documents` object owns local Markdown document inventory, applied
  discovery policy metadata, heading and chunk navigation references, and
  document/code reconciliation hints.
- `documents.items[]` contains document metadata and navigation references only. It is
  not a code fact, API fact, module fact, test fact, configuration fact, or runtime
  behavior claim.
- `documents.reconciliation.items[]` contains low-confidence uncertain inspection hints
  only. Reconciliation rows do not prove staleness, completeness, documentation quality,
  implementation, source/document agreement, correctness, vulnerability, or business
  priority.
- Evidence records keep the existing `evidence-index.jsonl` field set. The v0.8 layer
  emits the `document` evidence type for accepted local Markdown file, heading, chunk,
  and bounded mention observations.
- `agent-guide.md` may include a `Local Project Documentation` section generated from
  structured `documents` facts and resolving evidence. It does not summarize document
  prose, print chunk bodies, or promote document-only mentions to code-backed facts.

The generated files remain:

```text
.project-memory/project-map.json
.project-memory/evidence-index.jsonl
.project-memory/endpoints.md
.project-memory/agent-guide.md
```

`docs/architecture/OUTPUT_CONTRACT.md` and
`docs/architecture/EVIDENCE_MODEL.md` describe the generated v0.8 output shape,
document discovery policy, heading/chunk semantics, reconciliation uncertainty,
`document` evidence boundaries, and non-goals.

## Validation

This release-prep pass ran and passed the required local release checks:

- `mvn test`: 278 tests, 0 failures, 0 errors, 0 skipped.
- `mvn package`: 278 tests, 0 failures, 0 errors, 0 skipped, plus packaged CLI smoke.
- Packaged jar metadata inspection for `target/agent-project-memory-0.8.0.jar`:
  embedded Maven metadata reports `version=0.8.0`, and the manifest includes
  `Main-Class: io.github.dondindondev.agentprojectmemory.Main`.
- Separate packaged CLI smoke with `target/agent-project-memory-0.8.0.jar`: generated
  `project-map.json`, `endpoints.md`, `evidence-index.jsonl`, and `agent-guide.md`.
- `git diff --check`: passed.
- `git diff --stat`: run for the release-prep diff.
- release notes, changelog, README, roadmap, output contract, and evidence model
  consistency review.

Earlier v0.8 release-track checks supporting this release:

- focused analyzer, output, guide, fixture, and golden checks across the v0.8
  implementation slices;
- local docs guide rendering and regression-pack finalization;
- v0.8 real-project evaluation on pinned Spring PetClinic, Spring PetClinic REST, and
  Spring PetClinic Microservices targets;
- read-only security/contract audit confirming no reportable findings or
  release-blocking follow-up.

Public evaluation summary:
[docs/development/evaluations/v0.8-local-markdown-document-ingestion_SUMMARY.md][v0.8-eval].

[v0.8-eval]: ../development/evaluations/v0.8-local-markdown-document-ingestion_SUMMARY.md

## Security Notes

v0.8.0 keeps the deterministic local analyzer boundary:

- no source upload or external service dependency in the core analyzer;
- no network access, external documentation fetching, connectors, embeddings, vector
  stores, repository chat, generic RAG, or LLM call in the core analyzer;
- no PDF, Word, Confluence, Jira, GitHub, GitLab, YouTrack, or other external document
  ingestion;
- no document body serialization, document prose summaries, AI-generated document
  summaries, or broad filesystem traversal outside the scanned repository root;
- no document-backed facts promoted to code-backed facts;
- no stale-document proof, completeness proof, documentation-quality scoring,
  source/document agreement proof, coverage claim, runtime behavior claim, correctness
  claim, vulnerability claim, or business-priority claim;
- no automatic code modification.

Default document discovery is conservative: generated output, hidden paths, dependency
directories, private/internal paths, maintainer-like paths, secret-like path segments,
and `.project-memory/` outputs are excluded by default. Symlinked Markdown files and
symlinked directories are skipped by default.

## Not Included

- PDF, Word, external docs, Confluence/Jira/GitHub/GitLab/YouTrack connectors, network
  access, embeddings, vector stores, generic RAG, repository chat, SaaS, web UI, or LLM
  calls in the core analyzer.
- Runtime application behavior claims, Spring MVC routing proof from documents, OpenAPI
  implementation proof from documents, Maven behavior proof from documents,
  configuration value extraction from documents, database or ORM behavior proof, test
  coverage claims, CI claims, assertion analysis, or source/document agreement claims.
- Document body serialization, chunk body printing, document prose summaries,
  documentation-quality scoring, stale-document truth claims, or completeness claims.
- User-configurable include/exclude files or flags beyond the current documented
  default-scope policy.
- Gradle support, full Java symbol solving, Maven profile resolution, effective POM
  reconstruction, dependency graph reconstruction, or generated-source scanning by
  default.

## Known Follow-Ups

The v0.8 evaluation and audit record bounded future work that is not required for this
release:

- Add explicit future coverage for README filename casing across filesystems before
  making stronger cross-platform casing claims.
- Add future pinned evaluation targets that exercise `docs/**/*.md`, ADR-style
  directories, multiple module README files, and denser heading trees.
- Continue monitoring guide size and reconciliation hint density on
  documentation-heavy targets before adding new document-signal categories.

## Publication Status

The v0.8.0 release candidate materials are prepared for maintainer approval. No tag,
GitHub release, artifact upload, or checksum publication is performed by this
release-prep change.

Expected release assets when published:

- `agent-project-memory-0.8.0.jar`
- `SHA256SUMS`
