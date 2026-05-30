# Stage 8 Pilot Evaluation: Spring PetClinic REST `v4.0.2`

Evaluation date: 2026-05-30

## Scope

- Project URL: `https://github.com/spring-petclinic/spring-petclinic-rest.git`
- Exact tag: `v4.0.2`
- Exact commit: `d8026bb5bcc58145b95a66a7f8e7694f0fae142f`
- Local-only workspace path: `/private/tmp/agent-project-memory-eval/spring-petclinic-rest`
- Repository worktree state before editing: clean; `git status --short` produced no output.
- Evaluation result: scan completed and generated all v0.1 contract artifacts.

This project is the Stage 8 known-limitation probe for generated/interface-only REST API
mappings. The concrete controllers implement generated OpenAPI interfaces, while the
current v0.1 scanner analyzes the standard `src/main/java` source root and direct
source-visible Spring MVC annotations only.

## Commands Run

```sh
mvn package
mkdir -p /private/tmp/agent-project-memory-eval
git clone https://github.com/spring-petclinic/spring-petclinic-rest.git /private/tmp/agent-project-memory-eval/spring-petclinic-rest
git -C /private/tmp/agent-project-memory-eval/spring-petclinic-rest checkout v4.0.2
git -C /private/tmp/agent-project-memory-eval/spring-petclinic-rest rev-parse HEAD
git -C /private/tmp/agent-project-memory-eval/spring-petclinic-rest describe --tags --exact-match HEAD
git -C /private/tmp/agent-project-memory-eval/spring-petclinic-rest status --short
git -C /private/tmp/agent-project-memory-eval/spring-petclinic-rest rev-parse v4.0.2^{commit}
java -jar target/agent-project-memory-0.1.0-SNAPSHOT.jar scan /private/tmp/agent-project-memory-eval/spring-petclinic-rest
find /private/tmp/agent-project-memory-eval/spring-petclinic-rest/.project-memory -maxdepth 1 -type f -print
jq '{endpoints: (.endpoints | length), components: (.components.items | length), entities: (.entities.items | length), tests: (.tests.items | length)}' /private/tmp/agent-project-memory-eval/spring-petclinic-rest/.project-memory/project-map.json
wc -l /private/tmp/agent-project-memory-eval/spring-petclinic-rest/.project-memory/evidence-index.jsonl
sed -n '1,220p' /private/tmp/agent-project-memory-eval/spring-petclinic-rest/.project-memory/endpoints.md
sed -n '1,260p' /private/tmp/agent-project-memory-eval/spring-petclinic-rest/.project-memory/agent-guide.md
jq -r '.components.items[].class_name' /private/tmp/agent-project-memory-eval/spring-petclinic-rest/.project-memory/project-map.json
jq -r '.entities.items[] | [.class_name, (.identifier_fields|length), (.relationships|length), (.table_name // "null")] | @tsv' /private/tmp/agent-project-memory-eval/spring-petclinic-rest/.project-memory/project-map.json
jq -r '.tests.items[] | [.class_name, .source_path, ((.framework_signals | map(.name) | join("+"))), (.tested_subjects|length)] | @tsv' /private/tmp/agent-project-memory-eval/spring-petclinic-rest/.project-memory/project-map.json
jq -r '.endpoints[] | [.controller_class, .handler_method, (.http_methods|join(",")), .http_method_semantics, (.paths|join(",")), (.request_parameters|length), (.request_body_type // "null"), (.response_type // "null")] | @tsv' /private/tmp/agent-project-memory-eval/spring-petclinic-rest/.project-memory/project-map.json
rg -n "@(RestController|Controller|RequestMapping|GetMapping|PostMapping|PutMapping|PatchMapping|DeleteMapping)" src/main/java
rg -n "@(Component|Service|Repository|Configuration|RestController|Controller)" src/main/java
rg -n "@(Entity|Table|Id|MappedSuperclass|ManyToOne|OneToMany|OneToOne|ManyToMany)" src/main/java/org/springframework/samples/petclinic/model
find src/test/java -type f -name '*.java' | sort
rg -n "class .*\\b(Test|Tests|IT)\\b|@(SpringBootTest|DataJpaTest|WebMvcTest|ContextConfiguration|Test|Nested|RunWith)" src/test/java
jq -n --slurpfile pm /private/tmp/agent-project-memory-eval/spring-petclinic-rest/.project-memory/project-map.json --slurpfile ev /private/tmp/agent-project-memory-eval/spring-petclinic-rest/.project-memory/evidence-index.jsonl '($ev | map(.id)) as $ids | ([$pm[0] | .. | objects | .evidence_ids? // empty | .[]] | unique) as $refs | {referenced_evidence_ids: ($refs | length), indexed_evidence_records: ($ids | length), missing_references: ($refs | map(select(($ids | index(.)) | not)))}'
rg -n "^  /|^    (get|post|put|delete|patch):" src/main/resources/openapi.yml
rg -c "^    (get|post|put|delete|patch):" src/main/resources/openapi.yml
rg -n "implements .*Api|class .*RestController|@RequestMapping" src/main/java/org/springframework/samples/petclinic/rest/controller
find target/generated-sources/openapi/src/main/java -type f -name '*Api.java' -maxdepth 20 -print
git diff --check
git diff --stat
git status --short
```

## Run Results

`mvn package` completed successfully. The packaged build ran 70 tests with 0 failures
and 0 errors.

The target checkout was pinned successfully:

```text
v4.0.2
d8026bb5bcc58145b95a66a7f8e7694f0fae142f
```

The target checkout was clean before the scan. The scan command exited with code 0 and
produced:

```text
Generated project-map.json with 1 endpoint facts and 28 component facts and 8 entity facts and 19 test facts.
Generated endpoints.md with 1 endpoint facts.
Generated evidence-index.jsonl with 342 evidence records.
Generated agent-guide.md.
```

The target project now has generated local `.project-memory/` files in its external
workspace. Those third-party generated outputs were not copied into this repository.

## Network And Leakage

- Network access was used only to clone the public target repository and verify the pinned
  tag/commit.
- Source was cloned only under `/private/tmp/agent-project-memory-eval/`.
- No source was uploaded to external analysis services.
- No LLM calls were used for analysis or fact extraction.
- Third-party source and generated `.project-memory/` outputs were not committed.

## Generated Artifacts And Counts

| Artifact | Status |
| --- | --- |
| `.project-memory/project-map.json` | Generated under the external target workspace. |
| `.project-memory/evidence-index.jsonl` | Generated under the external target workspace. |
| `.project-memory/endpoints.md` | Generated under the external target workspace. |
| `.project-memory/agent-guide.md` | Generated under the external target workspace. |

| Count | Value |
| --- | --- |
| Endpoints | 1 |
| Components | 28 |
| Entities | 8 |
| Tests | 19 |
| Evidence records | 342 |
| Referenced evidence IDs | 342 |
| Missing evidence references | 0 |

## Scorecard Summary

| Project/ref | Endpoints | Components | Entities | Tests | Evidence quality | `agent-guide.md` | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `spring-petclinic-rest@v4.0.2` / `d8026bb5bcc58145b95a66a7f8e7694f0fae142f` | `1` | `2` | `2` | `2` | `2` | `1` | Direct-source extraction is accurate and evidence-backed, but the main REST API surface is generated/interface-only and intentionally missed by current v0.1. The guide remains useful for extracted facts but is noisy for large test evidence and does not explicitly warn about the generated-interface endpoint gap. |

## Scorecard: Endpoints

- Expected observations from bounded manual inspection: The current v0.1 analyzer should
  detect direct Spring MVC mappings visible under `src/main/java`. A bounded source
  search found one concrete handler method mapping in `RootRestController`: class-level
  `@RequestMapping("/")` and method-level `@RequestMapping(value = "/")`. The seven
  domain REST controllers are direct `@RestController` classes with class-level
  `@RequestMapping("api")` or `@RequestMapping("/api")`, but their operation mappings are
  on generated OpenAPI API interfaces, not concrete controller methods in `src/main/java`.
- Actual observations from generated artifacts: `project-map.json` and `endpoints.md`
  contain one endpoint, `METHOD NOT DECLARED /`, for
  `RootRestController#redirectToSwagger`. The OpenAPI spec contains 35 HTTP operations,
  and the concrete controllers implement `OwnersApi`, `PetsApi`, `PettypesApi`,
  `SpecialtiesApi`, `UsersApi`, `VetsApi`, and `VisitsApi`; those interface mappings are
  not emitted.
- False positives if found: None found for the direct emitted endpoint.
- False negatives if found: The main REST API operations are missing from a user-facing
  API perspective. Under the current v0.1 contract this is a known limitation rather than
  a direct-source extraction bug, because generated source roots and OpenAPI specs are
  outside the scanner's current supported source roots.
- Evidence quality notes: The emitted endpoint evidence resolves to repository-relative
  annotation records for `RootRestController.java:33`, `RootRestController.java:35`, and
  `RootRestController.java:41` with high confidence.
- Output contract issues if found: None for the emitted endpoint shape. The evaluation
  exposes a future scope/contract decision if generated-source or interface-inherited
  endpoint mappings become part of v0.1+.

## Scorecard: Components

- Expected observations from bounded manual inspection: Direct class-level supported
  stereotypes `@Component`, `@Service`, `@Repository`, `@Controller`, `@RestController`,
  and `@Configuration` should be emitted with direct annotation evidence.
- Actual observations from generated artifacts: `components.items` emits 28 classes: 8
  REST controllers, 14 JDBC/JPA repository implementations, 3 configuration classes, 2
  service implementations, and `Roles` as a component. This matches the bounded source
  search for supported direct stereotypes.
- False positives if found: None found in the bounded inspection.
- False negatives if found: None found for supported direct stereotypes. `@ControllerAdvice`,
  `@SpringBootApplication`, MapStruct `@Mapper`, Spring Data repository interfaces, and
  runtime/generated beans are not emitted, which is outside the documented direct
  stereotype contract.
- Evidence quality notes: Component evidence IDs resolve to direct annotation records
  with repository-relative paths, line ranges, excerpts, and high confidence.
- Output contract issues if found: None found.

## Scorecard: Entities

- Expected observations from bounded manual inspection: Direct `@Entity`, direct
  `@Table(name = "...")`, field-level direct relationship annotations, and entity or
  immediate source-visible mapped-superclass `@Id` fields should be emitted. Relationship
  targets should remain declared-type-only with explicit uncertainty.
- Actual observations from generated artifacts: `entities.items` emits 8 entities:
  `Owner`, `Pet`, `PetType`, `Role`, `Specialty`, `User`, `Vet`, and `Visit`. Table names
  and 8 relationship facts are emitted. Identifier fields are emitted for `User.username`
  and for `Role.id` and `Visit.id` inherited from immediate mapped superclass
  `BaseEntity`.
- False positives if found: None found for direct entity, table, identifier, or
  relationship facts.
- False negatives if found: None within the current direct-only contract. Several
  entities have empty `identifier_fields` because their IDs are inherited through
  multi-level mapped-superclass chains such as `Owner -> Person -> BaseEntity`,
  `PetType -> NamedEntity -> BaseEntity`, and `Vet -> Person -> BaseEntity`. The generated
  guide explicitly lists multi-level mapped-superclass walking as not analyzed.
- Evidence quality notes: Entity evidence resolves to annotation records. Relationship
  facts consistently preserve `target_resolution: "declared_type_only"` and
  `uncertainty: "target_type_not_resolved"`.
- Output contract issues if found: None found.

## Scorecard: Tests

- Expected observations from bounded manual inspection: Test-like Java classes under
  `src/test/java` with supported suffixes or direct test markers should be emitted.
  Helper/configuration declarations without clear test naming or direct test-class
  markers should be omitted. Tested subjects should be naming-convention inferences only.
- Actual observations from generated artifacts: The target project has 20 Java files
  under `src/test/java`; `tests.items` emits 19 test classes and omits
  `ApplicationTestConfig`, which is a helper configuration. Controller tests infer seven
  tested subjects with `support_type: "inferred"` and `confidence: "medium"`. Service
  variant tests and abstract base tests do not infer subjects when their naming
  convention does not match a production class simple name exactly.
- False positives if found: None found under the current contract. Abstract base test
  classes are emitted because they have test-like names and direct test methods.
- False negatives if found: None within the current naming-convention contract. Runtime
  inheritance of test methods, behavioral coverage, and service-interface subject
  inference are intentionally not analyzed.
- Evidence quality notes: Test evidence resolves to `test_file`, framework import, and
  annotation records with high confidence. Inferred tested-subject relations include both
  test class evidence and production class evidence.
- Output contract issues if found: None found.

## Scorecard: Evidence Quality

- Expected observations from bounded manual inspection: Every generated evidence ID
  referenced by `project-map.json` should resolve in `evidence-index.jsonl`, with
  repository-relative paths, line ranges, excerpts, and confidence labels.
- Actual observations from generated artifacts: A recursive evidence reference check
  found 342 unique referenced evidence IDs, 342 indexed evidence records, and 0 missing
  references. No evidence records had null path, line range, or excerpt in this scan.
  Evidence records by type were: 218 `annotation`, 104 `code_symbol`, 19 `test_file`, and
  1 `build_file`.
- False positives if found: None found.
- False negatives if found: None found for emitted facts. No evidence is emitted for
  generated OpenAPI interfaces because those sources were absent from the scanned source
  root and are outside the current analyzer scope.
- Evidence quality notes: Evidence is precise and resolves for emitted facts. The high
  number of test annotation evidence records is accurate but contributes to guide
  verbosity.
- Output contract issues if found: None found in JSON/JSONL field shape or evidence ID
  resolution.

## Scorecard: `agent-guide.md`

- Expected observations from bounded manual inspection: The guide should orient an agent
  using deterministic facts, expose uncertainty, and avoid unsupported architecture
  claims.
- Actual observations from generated artifacts: The guide correctly reports the Maven
  layout, one detected endpoint, 28 direct components, 8 entities, 19 tests, relationship
  uncertainty, immediate mapped-superclass limits, and naming-convention-only test
  subject inference. It does not invent unsupported architecture.
- False positives if found: None found in extracted factual claims.
- False negatives if found: The guide does not explicitly alert the reader that seven
  `@RestController` components have no emitted operation-level endpoints because the API
  mappings are generated/interface-only. It also cannot point to generated API interfaces
  because they are absent from the current scan inputs.
- Evidence quality notes: Evidence references resolve, but large test classes cause very
  long framework-signal evidence lists. The practical inspection order also becomes
  lengthy because it renders many component, entity, and test paths inline.
- Output contract issues if found: No strict field-shape issue. There is a usefulness
  tension with the guide contract's "concise orientation" wording for larger projects.

## Observations

### OBS-8-004: Generated/interface-only REST mappings are not emitted

- Project/ref: `spring-petclinic-rest@v4.0.2`
  (`d8026bb5bcc58145b95a66a7f8e7694f0fae142f`)
- Observed artifact: `.project-memory/project-map.json` `endpoints`; `.project-memory/endpoints.md`.
- Expected: Direct Spring MVC mappings under `src/main/java` should be emitted. For a
  complete runtime REST API map, the generated API interfaces or OpenAPI spec would also
  need to be analyzed.
- Actual: Only `RootRestController#redirectToSwagger` is emitted. The OpenAPI spec
  defines 35 operations, and seven concrete REST controllers implement generated `*Api`
  interfaces.
- False positive: None.
- False negative: Main API operations are absent from the generated endpoint inventory
  from a user-facing API perspective.
- Evidence quality: The emitted root endpoint evidence is precise. Missing generated API
  mappings have no evidence because generated source roots and OpenAPI specs are outside
  current v0.1 scan inputs.
- Output contract issue: None under the current source-root contract; this is a future
  scope/contract decision.
- Notes: `target/generated-sources/openapi/src/main/java` did not exist in the fresh
  target checkout because the target project was not built.

### OBS-8-005: `agent-guide.md` becomes noisy on large test evidence

- Project/ref: `spring-petclinic-rest@v4.0.2`
  (`d8026bb5bcc58145b95a66a7f8e7694f0fae142f`)
- Observed artifact: `.project-memory/agent-guide.md`.
- Expected: The guide should remain concise enough for first-pass orientation while
  preserving evidence references for important claims.
- Actual: Large controller and service tests render many individual `@Test` evidence
  references under framework signals, and the practical inspection order renders long
  inline path lists.
- False positive: None.
- False negative: None for emitted facts.
- Evidence quality: Evidence is accurate and resolving, but the Markdown presentation is
  too verbose for quick orientation.
- Output contract issue: Potential guide contract tension with "concise orientation";
  no JSON or evidence shape issue.
- Notes: The underlying `evidence-index.jsonl` should remain complete; the follow-up is
  about guide presentation.

## Follow-Up Tasks

### EVAL-8-004: Decide generated/interface endpoint handling

- Bounded task id: `EVAL-8-004`
- Project/ref: `spring-petclinic-rest@v4.0.2`
  (`d8026bb5bcc58145b95a66a7f8e7694f0fae142f`)
- Observed artifact: `.project-memory/project-map.json` `endpoints`; `.project-memory/endpoints.md`; `.project-memory/agent-guide.md`.
- Suspected cause: The endpoint analyzer scans only the standard direct source root and
  direct source-visible Spring MVC annotations. In this project, operation-level mappings
  are produced by `openapi-generator-maven-plugin` with `interfaceOnly=true` and added by
  `build-helper-maven-plugin` under `target/generated-sources/openapi/src/main/java`.
- Affected contract/doc: `docs/product/MVP_SPEC.md`,
  `docs/architecture/OUTPUT_CONTRACT.md`, and `docs/architecture/EVIDENCE_MODEL.md` if
  generated-source or interface-inherited endpoint facts become supported. If support
  remains out of scope, `docs/development/EVALUATION_PLAN.md` and the guide known-limits
  wording may be enough.
- Proposed validation: Add a focused fixture with a concrete `@RestController`
  implementing an interface whose methods carry Spring MVC mappings, plus a repeat scan
  of this project after deciding whether generated source roots are valid inputs.
- Non-goals: Do not parse OpenAPI YAML as evidence in this task, do not run Maven builds
  inside scans, do not add connectors, and do not infer runtime endpoints without source
  evidence.

### EVAL-8-005: Keep `agent-guide.md` concise for large evidence sets

- Bounded task id: `EVAL-8-005`
- Status: Addressed and retested on 2026-05-30; see "EVAL-8-005 Retest" below.
- Project/ref: `spring-petclinic-rest@v4.0.2`
  (`d8026bb5bcc58145b95a66a7f8e7694f0fae142f`)
- Observed artifact: `.project-memory/agent-guide.md` test framework signal sections and
  practical inspection order.
- Suspected cause: The guide renderer lists every framework-signal evidence reference
  and every candidate inspection path inline, which scales poorly on larger projects with
  many test methods.
- Affected contract/doc: `docs/architecture/OUTPUT_CONTRACT.md` if guide presentation
  rules are changed; no `project-map.json` or `evidence-index.jsonl` field change is
  expected.
- Proposed validation: Add or update a guide-generator fixture with many test methods and
  assert that the guide stays bounded while preserving resolving evidence references and
  complete JSON/JSONL artifacts.
- Non-goals: Do not drop evidence records from `evidence-index.jsonl`, do not perform
  test coverage analysis, and do not summarize source behavior with LLMs.

## EVAL-8-005 Retest

Retest date: 2026-05-30

Implementation scope:

- Updated only `agent-guide.md` Markdown presentation behavior in
  `AgentGuideGenerator`.
- Added a guide-generator golden fixture for many framework-signal evidence references
  and long practical inspection path lists.
- Updated `docs/architecture/OUTPUT_CONTRACT.md` to document capped guide presentation.
- Did not change analyzers, `project-map.json` schema, evidence record generation, or
  `evidence-index.jsonl` semantics.

Commands run:

```sh
mvn -Dtest=AgentGuideGeneratorTest,SpringMvcEndpointOutputGeneratorTest,AgentProjectMemoryCliTest test
mvn test
mvn package
git -C /private/tmp/agent-project-memory-eval/spring-petclinic-rest rev-parse HEAD
git -C /private/tmp/agent-project-memory-eval/spring-petclinic-rest describe --tags --exact-match HEAD
java -jar target/agent-project-memory-0.1.0-SNAPSHOT.jar scan /private/tmp/agent-project-memory-eval/spring-petclinic-rest
rg -n "more evidence references|more evidence paths" /private/tmp/agent-project-memory-eval/spring-petclinic-rest/.project-memory/agent-guide.md
wc -l /private/tmp/agent-project-memory-eval/spring-petclinic-rest/.project-memory/evidence-index.jsonl
jq -n --slurpfile pm /private/tmp/agent-project-memory-eval/spring-petclinic-rest/.project-memory/project-map.json --slurpfile ev /private/tmp/agent-project-memory-eval/spring-petclinic-rest/.project-memory/evidence-index.jsonl '($ev | map(.id)) as $ids | ([$pm[0] | .. | objects | .evidence_ids? // empty | .[]] | unique) as $refs | {referenced_evidence_ids: ($refs | length), indexed_evidence_records: ($ids | length), missing_references: ($refs | map(select(($ids | index(.)) | not)))}'
```

Results:

- The external checkout was still pinned to tag `v4.0.2` at commit
  `d8026bb5bcc58145b95a66a7f8e7694f0fae142f`.
- The scan completed and preserved the same artifact counts: 1 endpoint, 28 components,
  8 entities, 19 tests, and 342 evidence records.
- The generated guide now caps long framework-signal evidence lists with suffixes such as
  `... and 20 more evidence references in evidence-index.jsonl`.
- The practical inspection order now caps long inline path lists with suffixes such as
  `... and 23 more evidence paths in evidence-index.jsonl`.
- Complete JSON/JSONL evidence remains available: recursive evidence-reference checking
  found 342 referenced evidence IDs, 342 indexed evidence records, and no missing
  references.
- `tested_subjects` inference metadata remains visible in the guide, including
  `support_type`, `confidence`, and `uncertainty` when present.
- JPA relationship uncertainty remains visible through `target_resolution:
  declared_type_only` and `uncertainty: target_type_not_resolved`.

Retest conclusion: `EVAL-8-005` is addressed for the observed guide verbosity issue.
`EVAL-8-004` remains open and was not implemented.

## Validation

Final validation for the `EVAL-8-005` retest was run from this repository:

```sh
mvn -Dtest=AgentGuideGeneratorTest,SpringMvcEndpointOutputGeneratorTest,AgentProjectMemoryCliTest test
mvn test
mvn package
java -jar target/agent-project-memory-0.1.0-SNAPSHOT.jar scan /private/tmp/agent-project-memory-eval/spring-petclinic-rest
git diff --check
git diff --stat
git status --short
```

Results:

- Targeted Maven test selection passed with 18 tests, 0 failures, and 0 errors.
- `mvn test` passed with 71 tests, 0 failures, and 0 errors.
- `mvn package` passed with 71 tests, 0 failures, and 0 errors, then completed the
  packaged CLI smoke test.
- The Spring PetClinic REST scan rerun completed successfully and generated the same
  JSON/JSONL evidence counts recorded above.
- `git diff --check` passed with no output.
- `git diff --stat` and `git status --short` were reviewed before handoff; generated
  third-party `.project-memory/` outputs stayed in the external evaluation workspace.
