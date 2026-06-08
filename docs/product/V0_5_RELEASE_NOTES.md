# v0.5.0 Release Notes

Release date: 2026-06-08

`agent-project-memory` v0.5.0 adds a deterministic Spring application surface layer for
local Java/Spring Maven repositories. It keeps repositories, configuration, bean
methods, behavior annotations, messaging listener annotations, and Spring Security
configuration warnings visible without claiming runtime Spring behavior, security
policy, messaging topology, or repository/entity ownership.

These release materials are prepared for maintainer review. The `v0.5.0` tag, GitHub
release, jar asset, and checksum publication have not been created yet.

## Highlights

- Direct source-visible `@Repository` facts as extracted repository stereotype
  observations.
- Inferred source-visible Spring Data repository interface extension signals with
  `entity_relation_status: "not_analyzed"`.
- Direct source-visible `@Configuration`, `@ConfigurationProperties`, and `@Bean` facts
  with explicit `binding_status: "not_analyzed"` and
  `bean_name_status: "not_analyzed"` where applicable.
- Direct source-visible `@Transactional`, `@Scheduled`, and `@EventListener`
  operational change-surface signals.
- Direct source-visible Spring Kafka and Rabbit listener annotation signals without
  serializing topics, queues, exchanges, routing keys, group IDs, broker topology, or
  delivery semantics.
- Spring Security configuration warnings for supported security annotations and trusted
  `SecurityFilterChain` `@Bean` methods without policy, endpoint protection,
  authentication, authorization, filter-chain order, vulnerability, or correctness
  claims.
- Module-grouped `agent-guide.md` Spring Application Surface guidance that separates
  extracted facts, inferred signals, explicit not-analyzed statuses, and warnings.

## Output Compatibility

v0.5.0 moves normal generated `project-map.json` output to `schema_version: "0.5"`.
The v0.5 contract builds on the v0.4 API surface and v0.3 module-aware build/config
boundaries:

- The same output files remain under `.project-memory/`.
- Existing `components`, `entities`, `tests`, `endpoints`, `warnings`, and
  `api_surface` sections remain distinct.
- A top-level `spring_application_surface` object groups repository, configuration,
  behavior, messaging, and security-warning references.
- Direct `@Repository` and `@Configuration` observations may appear both as existing
  component facts and as Spring application surface facts. This is two contract views
  over the same source observation, not evidence of multiple runtime beans.
- Spring Security configuration warnings continue to live in `warnings.items` and are
  referenced from
  `spring_application_surface.security.configuration_warnings.warning_ids`.
- Evidence records keep the existing `evidence-index.jsonl` field set. No new global
  evidence fields are introduced for v0.5.

The generated files remain:

```text
.project-memory/project-map.json
.project-memory/evidence-index.jsonl
.project-memory/endpoints.md
.project-memory/agent-guide.md
```

`docs/architecture/OUTPUT_CONTRACT.md` and
`docs/architecture/EVIDENCE_MODEL.md` already describe the generated v0.5 output shape,
warning references, messaging destination-value exclusion, Spring Security warning
semantics, and evidence boundaries. No contract or evidence-model change was required
for this release-prep pass.

## Validation

This release-prep pass ran and passed:

- `git status --short --branch --untracked-files=all`: expected release-prep working
  tree on `main`.
- `mvn test`: 230 tests, 0 failures, 0 errors, 0 skipped.
- `mvn package`: 230 tests, 0 failures, 0 errors, 0 skipped.
- Packaged CLI smoke from the Maven build: generated `project-map.json`,
  `endpoints.md`, `evidence-index.jsonl`, and `agent-guide.md`.
- Packaged jar/version inspection: `target/agent-project-memory-0.5.0.jar` exists, and
  embedded Maven metadata reports `version=0.5.0`.
- `git diff --check`: passed.
- `git diff --stat`: run for the release-prep diff.

Earlier v0.5 release-track gates supporting this release:

- v0.5 real-project evaluation on five pinned Java/Spring targets.
- v0.5 review/security diff audit over the implementation range.

The v0.5 implementation-range risk-based implementation-range security assessment reported no findings:

The final release-prep diff uses a manual low-risk documentation assessment because it changes only
release documentation, changelog, README/status wording, the Maven project version, and
local maintainer state; the v0.5 review and risk assessment already covered the implementation range with a clean
risk-based security assessment.

## Security Notes

v0.5.0 keeps the deterministic local analyzer boundary:

- no Maven execution during scan;
- no network, connector, SaaS, web UI, repository chat, generic RAG, or LLM call in the
  core analyzer;
- no config file value extraction or runtime binding claims;
- no repository-to-entity relation claims;
- no runtime transaction, scheduler, event delivery, messaging topology, or broker
  behavior claims;
- no Spring Security policy, endpoint protection, authentication, authorization,
  vulnerability, or correctness claims.

the release-prep evidence-excerpt decision is resolved for this release prep: bounded source annotation evidence
excerpts for `@ConfigurationProperties` and inherited test annotations remain
acceptable for v0.5. They are bounded source-local evidence excerpts, not structured
`prefix`/`value` output fields or configuration value extraction. The v0.5 evaluation
found no secret-looking values in the checked generated artifacts, and the the v0.5 review and risk assessment
risk-based security assessment did not classify this as a reportable security finding. A future
evidence-hardening follow-up may still evaluate symbol-only excerpts, but it is not
required before v0.5.0 release.

## Not Included

- Runtime Spring bean graph reconstruction.
- Autowiring graph reconstruction.
- Runtime conditional evaluation, profile activation, or auto-configuration
  reconstruction.
- Runtime configuration binding, property value extraction, config key/value inventory,
  validation results, or secret extraction.
- Repository query-method semantic analysis.
- Repository-to-entity relation claims.
- Transaction propagation, isolation, rollback, effective transaction-manager, proxy, or
  call-graph analysis.
- Runtime scheduler registration, frequency correctness, lock behavior, or cluster
  behavior.
- Event publication path, listener ordering, transaction phase, or runtime delivery
  analysis.
- Messaging destination, topology, queue/topic/exchange existence, consumer group,
  binding, delivery, or deployment configuration analysis.
- Spring Security policy, endpoint protection, authentication, authorization,
  filter-chain order, vulnerability, or correctness analysis.
- Full Java symbol solving.
- Gradle support.
- Maven profile resolution, effective POM reconstruction, or dependency graph
  reconstruction.
- Local Markdown/document ingestion.
- Connectors for YouTrack, Jira, Confluence, GitHub, or GitLab.
- LLM calls in the core analyzer.
- SaaS, web UI, repository chat, generic RAG, or automatic code modification.

## Known Follow-Ups

The v0.5 evaluation and review record bounded future work that is not required for this
release:

- Consider a future evidence-hardening follow-up for symbol-only annotation evidence
  excerpts if the maintainer wants stricter evidence excerpt policy consistency.
- Add a future real-project evaluation target or focused fixture for direct
  `@EventListener` coverage.
- Add a future real-project evaluation target or focused fixture for direct
  `@RabbitListener` coverage.
- Consider documenting source-root compatibility behavior for non-Maven Spring Boot
  guide inputs if that remains a supported local scan mode.

## Maintainer Review Notes

This document prepares release materials for maintainer review. Tag creation, pushing,
publishing, GitHub release creation, artifact upload, checksum generation, and checksum
publication remain separate maintainer-approved actions.

Recommended manual next steps:

1. Review the release-prep diff for scope, version, README, roadmap, changelog, output
   contract, and evidence contract alignment.
2. Confirm local validation results from the release-prep summary.
3. Create the `v0.5.0` tag only after approval.
4. Draft the GitHub release from these notes only after the tag is approved.
5. Attach `target/agent-project-memory-0.5.0.jar` and checksum files if publishing
   binary assets.
6. Verify published release notes and download instructions after publication.
