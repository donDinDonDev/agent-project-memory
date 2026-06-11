# v0.7.0 Release Notes

Release date: 2026-06-10

`agent-project-memory` v0.7.0 deepens the deterministic tests inventory and adds a
conservative quality/change-risk planning map for local Java/Spring Maven repositories.
It keeps source-visible test structure, inferred tested-subject relations, uncertain
test-gap hints, and warning-oriented change-risk hints separate without claiming
coverage, test execution, CI status, assertion behavior, runtime Spring context
behavior, correctness, vulnerability, production impact, or complete subject mapping.

## Highlights

- Stable module-owned test facts with `tests.items[].id` and direct `module_id` fields.
- Bounded test method inventory for supported directly visible JUnit Jupiter and JUnit 4
  test method annotations.
- Direct source-visible JUnit Jupiter, JUnit 4, and Spring Test framework signal
  classifications with `framework_signals[].signal_kind`.
- Direct source-visible Spring test slice signals for `@SpringBootTest`, `@WebMvcTest`,
  `@DataJpaTest`, and `@ContextConfiguration`.
- Conservative source-visible mock annotation signals for `@MockBean` and `@SpyBean` on
  emitted test classes.
- Conservative tested-subject relation/status rows from supported naming conventions,
  exact production imports, direct field types, and direct Spring test slice class
  literals where deterministic.
- A top-level `quality` object with conservative `no_obvious_test` test-gap hints and
  warning-oriented or uncertain change-risk planning hints from existing deterministic
  facts.
- `agent-guide.md` rendering and regression coverage that preserve source-visible,
  inferred, uncertain, warning, and not-analyzed semantics.

## Output Compatibility

v0.7.0 moves normal generated `project-map.json` output to `schema_version: "0.7"`.
The v0.7 contract builds on the v0.6 JPA/domain, v0.5 Spring application surface, v0.4
API surface, and v0.3 module-aware build/config contracts:

- The same output files remain under `.project-memory/`.
- The existing top-level `tests` object remains the owner of source-visible test class,
  method, direct framework signal, Spring test slice, mock annotation signal, and
  tested-subject relation/status facts.
- `tests.items[]` may include `methods[]`, `spring_test_slices[]`, `mock_signals[]`, and
  expanded `tested_subjects[]` relation/status rows.
- A new top-level `quality` object owns conservative test-gap and change-risk planning
  hints.
- Evidence records keep the existing `evidence-index.jsonl` field set. No new global
  evidence fields or evidence types are introduced for v0.7.
- Quality signals reuse evidence from the underlying source-visible subject facts; they
  do not fabricate absence evidence.

The generated files remain:

```text
.project-memory/project-map.json
.project-memory/evidence-index.jsonl
.project-memory/endpoints.md
.project-memory/agent-guide.md
```

`docs/architecture/OUTPUT_CONTRACT.md` and
`docs/architecture/EVIDENCE_MODEL.md` describe the generated v0.7 output shape,
tested-subject relation statuses, quality/change-risk planning-hint semantics, evidence
boundaries, and non-goals.

## Validation

The v0.7 validation record passed:

- `mvn test`: 254 tests, 0 failures, 0 errors, 0 skipped.
- `mvn package`: 254 tests, 0 failures, 0 errors, 0 skipped, plus packaged CLI smoke.
- Packaged jar metadata inspection for `target/agent-project-memory-0.7.0.jar`:
  embedded Maven metadata reports `version=0.7.0`, and the manifest includes
  `Main-Class: io.github.dondindondev.agentprojectmemory.Main`.
- Separate packaged CLI smoke with `target/agent-project-memory-0.7.0.jar`: generated
  `project-map.json`, `endpoints.md`, `evidence-index.jsonl`, and `agent-guide.md`.
- `git diff --check`: passed.
- Documentation/version consistency review for release documentation and version state.
- release notes, changelog, README, roadmap, output contract, and evidence model
  consistency review.

Additional v0.7 validation supporting this release:

- focused analyzer, output, guide, fixture, and golden checks across the v0.7
  implementation slices;
- v0.7 guide rendering and regression-pack finalization;
- v0.7 real-project evaluation on pinned Spring PetClinic, Spring PetClinic REST, Spring
  PetClinic Microservices, and Spring Cloud OpenFeign targets;
- read-only security/contract audit confirming no release-blocking or bounded-fix
  findings.

Public evaluation summary:
[docs/development/evaluations/v0.7-tests-quality-real-projects_SUMMARY.md][v0.7-eval].

[v0.7-eval]: ../development/evaluations/v0.7-tests-quality-real-projects_SUMMARY.md

## Security Notes

v0.7.0 keeps the deterministic local analyzer boundary:

- no source upload or external service dependency in the core analyzer;
- no Maven execution during scan;
- no test execution, CI provider, coverage report, mutation testing report, runtime
  trace, database inspection, or LLM-generated summary as evidence for deterministic
  quality or change-risk facts;
- no coverage, assertion, runtime execution, CI status, correctness, vulnerability,
  production-impact, business-priority, or complete subject-mapping claims;
- no runtime Spring test context reconstruction, bean graph reconstruction, MockMvc
  setup proof, Mockito behavior proof, database access verification, or repository
  runtime verification;
- no connector, SaaS, web UI, repository chat, generic RAG, or LLM call in the core
  analyzer;
- no automatic code modification.

Test framework, Spring test slice, and mock annotation signals are trusted only when
source-visible syntax supports a supported external framework origin and the same
framework type is not declared by scanned source. Ambiguous, unsupported, unresolved,
generated-source-only, classpath-only, or absent tested-subject signals use explicit
statuses and uncertainty instead of inferred coverage or runtime claims.

## Not Included

- Coverage analysis, mutation testing, behavioral assertion understanding, CI result
  claims, runtime test execution, runtime Spring context reconstruction, runtime
  repository or database verification, or full call graph reconstruction.
- Complete tested-subject mapping or repository-interface tested-subject matching beyond
  the current bounded relation rules.
- Generic field-type tested-subject inference beyond the current unsupported status
  boundary.
- Runtime Spring test slice correctness, MockMvc setup, bean override, Mockito behavior,
  active profile, database access, or repository behavior analysis.
- Production-impact, vulnerability, correctness, or business-priority scoring.
- Full Java symbol solving.
- Gradle support.
- Maven profile resolution, effective POM reconstruction, or dependency graph
  reconstruction.
- Local Markdown/document ingestion.
- Connectors for YouTrack, Jira, Confluence, GitHub, or GitLab.
- LLM calls in the core analyzer.
- SaaS, web UI, repository chat, generic RAG, or automatic code modification.

## Known Follow-Ups

The v0.7 evaluation and audit record bounded future work that is not required for this
release:

- Consider repository-interface tested-subject matching for exact test imports or direct
  field types that reference emitted source-visible repository interface signals.
- Consider a bounded generic field-type status refinement for simple collection element
  type candidates.
- Consider overloaded method display disambiguation in Markdown or summary output.
- Add a future real-project evaluation target that emits real `@MockBean` or `@SpyBean`
  mock annotation signals.

## Publication Status

The v0.7.0 release is published at:

[https://github.com/donDinDonDev/agent-project-memory/releases/tag/v0.7.0](https://github.com/donDinDonDev/agent-project-memory/releases/tag/v0.7.0)

Published release assets:

- `agent-project-memory-0.7.0.jar`
- `SHA256SUMS`
