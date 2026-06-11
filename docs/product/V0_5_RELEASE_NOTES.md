# v0.5.0 Release Notes

Release date: 2026-06-08

`agent-project-memory` v0.5.0 adds a deterministic Spring application surface layer for
local Java/Spring Maven repositories. It keeps repositories, configuration, bean
methods, behavior annotations, messaging listener annotations, and Spring Security
configuration warnings visible without claiming runtime Spring behavior, security
policy, messaging topology, or repository/entity ownership.

The `v0.5.0` tag and GitHub release are published with
`agent-project-memory-0.5.0.jar` and `SHA256SUMS` assets.

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
for the final release documentation update.

## Validation

The v0.5 validation record covers the full test suite, package build, packaged CLI
smoke test, jar metadata inspection, Markdown whitespace checks, documentation/version
consistency review, and publication verification. The published `v0.5.0` release
includes the expected jar and checksum assets, downloaded assets verify against
`SHA256SUMS`, and embedded Maven metadata reports `version=0.5.0`.

Additional v0.5 validation supporting this release:

- v0.5 real-project evaluation on five pinned Java/Spring targets.
- v0.5 review and security assessment over the changed implementation surface.

Public evaluation summary:
[docs/development/evaluations/v0.5-spring-application-surface-real-projects_SUMMARY.md](../development/evaluations/v0.5-spring-application-surface-real-projects_SUMMARY.md).

The v0.5 security assessment reported no release-blocking findings.

The final documentation and version-state assessment was narrow because only release
documentation, changelog, README/status wording, the Maven project version, and release
state changed after the implementation review.

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

The evidence-excerpt decision is resolved for this release: bounded source annotation
evidence excerpts for `@ConfigurationProperties` and inherited test annotations remain
acceptable for v0.5. They are bounded source-local evidence excerpts, not structured
`prefix`/`value` output fields or configuration value extraction. The v0.5 evaluation
found no secret-looking values in the checked generated artifacts, and the
security assessment did not classify this as a reportable finding. A future
evidence-hardening follow-up may still evaluate symbol-only excerpts, but it was not
required for v0.5.0.

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

## Publication

The release is published at:
<https://github.com/donDinDonDev/agent-project-memory/releases/tag/v0.5.0>

Published assets:

- `agent-project-memory-0.5.0.jar`
- `SHA256SUMS`
