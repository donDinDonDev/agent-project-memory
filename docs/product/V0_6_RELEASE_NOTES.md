# v0.6.0 Release Notes

Release date: 2026-06-09

`agent-project-memory` v0.6.0 deepens the deterministic JPA/domain model for local
Java/Spring Maven repositories. It keeps source-visible JPA annotations, embedded and
identifier signals, relationship metadata, and inferred Spring Data repository/entity
generic links visible without claiming runtime ORM behavior, database schema truth,
relationship target resolution, repository runtime registration, or provider defaults.

## Highlights

- Direct source-visible entity field metadata for field-level `@Column`, `@Enumerated`,
  `@GeneratedValue`, and `@Version` annotations.
- Direct source-visible `@Embeddable` facts, field-level `@Embedded` and `@EmbeddedId`
  signals, and class-level `@IdClass` composite-id signals with explicit partial and
  not-analyzed semantics.
- Bounded direct relationship metadata for relationship cardinality, string-literal
  `mappedBy`, direct `@JoinColumn` and `@JoinTable` metadata, and directly visible
  `optional`, `fetch`, `cascade`, and `orphanRemoval` attributes.
- Conservative inferred Spring Data repository/entity relations when a supported
  source-visible repository generic type matches exactly one emitted entity fact.
- Safe JPA-only wildcard import support for explicit `jakarta.persistence.*` and
  `javax.persistence.*` imports for the existing supported JPA annotation set.
- Quieter no-domain `agent-guide.md` rendering that omits empty JPA/domain sections and
  no-evidence persistence inspection hints when no domain facts are present.
- Module-grouped domain/data guidance that separates extracted facts, inferred links,
  uncertain targets, explicit not-analyzed statuses, and warnings.

## Output Compatibility

v0.6.0 moves normal generated `project-map.json` output to `schema_version: "0.6"`.
The v0.6 contract builds on the v0.5 Spring application surface, v0.4 API surface, and
v0.3 module-aware build/config contracts:

- The same output files remain under `.project-memory/`.
- Existing top-level `entities.items[]` remains the owner of source-visible JPA entity
  facts.
- Entity facts may include `fields[]`, richer `identifier_fields[]`,
  nullable `id_class`, and enriched `relationships[]` entries.
- `entities.embeddables` contains direct source-visible `@Embeddable` facts; embeddables
  are not emitted as entity/table facts.
- Relationship targets remain declared-type-only and uncertain in this release.
- Spring Data repository interface signal entries under
  `spring_application_surface.repositories.items[]` include
  `entity_relation_status` and nullable `entity_relation`.
- Evidence records keep the existing `evidence-index.jsonl` field set. No new global
  evidence fields or database evidence types are introduced for v0.6.

The generated files remain:

```text
.project-memory/project-map.json
.project-memory/evidence-index.jsonl
.project-memory/endpoints.md
.project-memory/agent-guide.md
```

`docs/architecture/OUTPUT_CONTRACT.md` and
`docs/architecture/EVIDENCE_MODEL.md` describe the generated v0.6 output shape,
repository/entity relation statuses, relationship uncertainty semantics, safe JPA
wildcard-origin rule, no-domain guide rendering behavior, and evidence boundaries.

## Validation

This release-prep pass ran and passed the required local release checks:

- `mvn test`: 244 tests, 0 failures, 0 errors, 0 skipped.
- `mvn package`: 244 tests, 0 failures, 0 errors, 0 skipped, plus packaged CLI smoke.
- Packaged jar metadata inspection for `target/agent-project-memory-0.6.0.jar`:
  embedded Maven metadata reports `version=0.6.0`, and the manifest includes
  `Main-Class: io.github.dondindondev.agentprojectmemory.Main`.
- `git diff --check`: passed.
- `git diff --stat`: run for the release-prep diff.
- release notes, changelog, README, roadmap, output contract, and evidence model
  consistency review

Earlier v0.6 release-track checks supporting this release:

- focused analyzer, output, guide, fixture, and golden checks across the v0.6
  implementation slices;
- v0.6 real-project evaluation on pinned Spring PetClinic, Spring PetClinic REST, Spring
  PetClinic Microservices, and Spring Cloud OpenFeign targets;
- risk-based implementation and follow-up review gates.

Public evaluation summary:
[docs/development/evaluations/v0.6-jpa-domain-real-projects_SUMMARY.md][v0.6-eval].

[v0.6-eval]: ../development/evaluations/v0.6-jpa-domain-real-projects_SUMMARY.md

The v0.6 implementation and follow-up risk checks reported no release-blocking findings.

## Security Notes

v0.6.0 keeps the deterministic local analyzer boundary:

- no Maven execution during scan;
- no database connection or database introspection;
- no runtime Hibernate/JPA metadata analysis;
- no generated schema, DDL reconstruction, JPQL semantic parsing, or migration
  interpretation;
- no runtime repository/entity verification, query-method behavior, or database access
  claims;
- no connector, SaaS, web UI, repository chat, generic RAG, or LLM call in the core
  analyzer;
- no automatic code modification.

Direct JPA annotations are trusted only when source-visible syntax supports a supported
`jakarta.persistence.*` or `javax.persistence.*` origin through a fully qualified
annotation name, explicit single-type import, or the bounded explicit wildcard-import
rule. Conflicting explicit imports, same-package/local fake annotations,
source-declared fake framework types, unsupported wildcard imports, generated-source-only
signals, and classpath-only signals remain out of scope.

## Not Included

- Getter/property-access JPA annotation extraction.
- Complete persistent-property inventory.
- Full composite-key semantic reconstruction.
- Relationship target entity linking beyond declared unresolved targets.
- Database schema reconstruction, database introspection, runtime Hibernate/JPA metadata,
  DDL reconstruction, JPQL semantic parsing, or migration interpretation.
- Runtime Spring Data repository registration, query-method behavior, database access,
  transaction behavior, dependency graph reachability, or repository/entity
  verification.
- Provider-default, fetch-behavior, cascade-behavior, foreign-key, join-table,
  constraint, optimistic-locking correctness, or ORM ownership correctness claims.
- Full Java symbol solving.
- Gradle support.
- Maven profile resolution, effective POM reconstruction, or dependency graph
  reconstruction.
- Local Markdown/document ingestion.
- Connectors for YouTrack, Jira, Confluence, GitHub, or GitLab.
- LLM calls in the core analyzer.
- SaaS, web UI, repository chat, generic RAG, or automatic code modification.

## Known Follow-Ups

The v0.6 evaluation and follow-up work record one future coverage item that is not
required for this release:

- Add a future real-project or focused real-world evaluation target that exercises
  `@Embeddable`, `@EmbeddedId`, `@IdClass`, `@Enumerated`, and `@Version`.

## Publication

The `v0.6.0` GitHub release is published at
https://github.com/donDinDonDev/agent-project-memory/releases/tag/v0.6.0 with packaged
jar and checksum assets.

Published assets:

- `agent-project-memory-0.6.0.jar`
- `SHA256SUMS`

Published jar checksum:

```text
ab0cb5b1c1bbf660e53864ffb4aad7eae97989e1b985ec98d9d1f74b72f3f89b  agent-project-memory-0.6.0.jar
```
