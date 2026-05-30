# Stage 8 Pilot Evaluation: Spring PetClinic `3c06fbf`

Evaluation date: 2026-05-30

## Scope

- Project URL: `https://github.com/spring-projects/spring-petclinic.git`
- Exact commit: `3c06fbfc1e42eb40802e0d0ca989bc9226755804`
- Local-only workspace path: `/private/tmp/agent-project-memory-eval/spring-petclinic`
- Initial evaluation result: blocked before contract artifact generation.
- EVAL-8-001 retest result: addressed; the scan reached contract artifact generation on
  2026-05-30 after configuring JavaParser for modern Java syntax.
- Repository worktree state before editing: clean; `git status --short` produced no output.

## Commands Run

```sh
mvn package
mkdir -p /private/tmp/agent-project-memory-eval
git clone https://github.com/spring-projects/spring-petclinic.git /private/tmp/agent-project-memory-eval/spring-petclinic
git -C /private/tmp/agent-project-memory-eval/spring-petclinic checkout 3c06fbfc1e42eb40802e0d0ca989bc9226755804
git -C /private/tmp/agent-project-memory-eval/spring-petclinic rev-parse HEAD
java -jar target/agent-project-memory-0.1.0-SNAPSHOT.jar scan /private/tmp/agent-project-memory-eval/spring-petclinic
find /private/tmp/agent-project-memory-eval/spring-petclinic/.project-memory -maxdepth 1 -type f -print
find /private/tmp/agent-project-memory-eval/spring-petclinic/.project-memory -maxdepth 1 -print
git -C /private/tmp/agent-project-memory-eval/spring-petclinic status --short
```

Retest after `EVAL-8-001` fix on 2026-05-30:

```sh
mvn package
git -C /private/tmp/agent-project-memory-eval/spring-petclinic rev-parse HEAD
git -C /private/tmp/agent-project-memory-eval/spring-petclinic status --short
java -jar target/agent-project-memory-0.1.0-SNAPSHOT.jar scan /private/tmp/agent-project-memory-eval/spring-petclinic
ls /private/tmp/agent-project-memory-eval/spring-petclinic/.project-memory/project-map.json
ls /private/tmp/agent-project-memory-eval/spring-petclinic/.project-memory/evidence-index.jsonl
ls /private/tmp/agent-project-memory-eval/spring-petclinic/.project-memory/endpoints.md
ls /private/tmp/agent-project-memory-eval/spring-petclinic/.project-memory/agent-guide.md
jq -r '[["endpoints", (.endpoints | length)], ["components", (.components.items | length)], ["entities", (.entities.items | length)], ["tests", (.tests.items | length)]][] | @tsv' /private/tmp/agent-project-memory-eval/spring-petclinic/.project-memory/project-map.json
wc -l /private/tmp/agent-project-memory-eval/spring-petclinic/.project-memory/evidence-index.jsonl
```

Content-level scorecard pass after the parser fix on 2026-05-30 15:53:40 CST:

```sh
mvn package
ls -ld /private/tmp/agent-project-memory-eval/spring-petclinic
git -C /private/tmp/agent-project-memory-eval/spring-petclinic rev-parse HEAD
git -C /private/tmp/agent-project-memory-eval/spring-petclinic status --short
java -jar target/agent-project-memory-0.1.0-SNAPSHOT.jar scan /private/tmp/agent-project-memory-eval/spring-petclinic
jq -r '.endpoints[] | [(.http_methods | join(",")), (.paths | join(",")), .controller_class, .handler_method] | @tsv' /private/tmp/agent-project-memory-eval/spring-petclinic/.project-memory/project-map.json
jq -r '.components.items[] | [.class_name, (.stereotypes | join(","))] | @tsv' /private/tmp/agent-project-memory-eval/spring-petclinic/.project-memory/project-map.json
jq -r '.entities.items[] | [.class_name, (.table_name // "null"), (.identifier_fields | map(.field_name + ":" + .java_type) | join(",")), (.relationships | map(.annotation + " " + .field_name + ":" + .java_type + " " + .target_resolution + " " + .uncertainty) | join("; "))] | @tsv' /private/tmp/agent-project-memory-eval/spring-petclinic/.project-memory/project-map.json
jq -r '.tests.items[] | [.class_name, .source_path, (.framework_signals | map(.name) | join(",")), (.tested_subjects | map(.class_name + "(" + .support_type + ":" + .confidence + (if .uncertainty then ":" + .uncertainty else "" end) + ")") | join(";"))] | @tsv' /private/tmp/agent-project-memory-eval/spring-petclinic/.project-memory/project-map.json
rg -n "@(Controller|RestController|RequestMapping|GetMapping|PostMapping|PutMapping|PatchMapping|DeleteMapping)" src/main/java
rg -n "@(Component|Service|Repository|Controller|RestController|Configuration)" src/main/java
rg -n "@(Entity|Table|Id|ManyToOne|OneToMany|OneToOne|ManyToMany)" src/main/java
find src/test/java -name '*.java' -print
jq -n --slurpfile pm /private/tmp/agent-project-memory-eval/spring-petclinic/.project-memory/project-map.json --slurpfile ev /private/tmp/agent-project-memory-eval/spring-petclinic/.project-memory/evidence-index.jsonl '($ev | map(.id)) as $ids | ([$pm[0] | .. | objects | .evidence_ids? // empty | .[]] | unique) as $refs | {referenced_evidence_ids: ($refs | length), indexed_evidence_records: ($ids | length), missing_references: ($refs | map(select(($ids | index(.)) | not)))}'
sed -n '1,760p' /private/tmp/agent-project-memory-eval/spring-petclinic/.project-memory/agent-guide.md
```

## Run Results

`mvn package` completed successfully. The packaged build ran 62 tests with 0 failures and
0 errors.

The target checkout was pinned successfully:

```text
3c06fbfc1e42eb40802e0d0ca989bc9226755804
```

The scan command created `/private/tmp/agent-project-memory-eval/spring-petclinic/.project-memory`
but exited with code 1 before writing contract artifacts. The failure was:

```text
com.github.javaparser.ParseProblemException: Use of patterns with instanceof is not supported.
Pay attention that this feature is supported starting from 'JAVA_14' language level.
```

The exception occurred while `TestInventoryAnalyzer` was parsing test sources. No analyzer
code or output contracts were changed in this evaluation slice.

The 2026-05-30 parser-fix retest used the same pinned checkout:

```text
3c06fbfc1e42eb40802e0d0ca989bc9226755804
```

The target checkout was clean before the retest scan. `mvn package` completed
successfully; the packaged build ran 66 tests with 0 failures and 0 errors. The scan
command exited with code 0 and produced:

```text
Generated project-map.json with 17 endpoint facts and 9 component facts and 6 entity facts and 22 test facts.
Generated endpoints.md with 17 endpoint facts.
Generated evidence-index.jsonl with 302 evidence records.
Generated agent-guide.md.
```

The content-level scorecard pass reused the same pinned checkout. The target checkout
showed only untracked generated `.project-memory/` output before the pass, which is
expected for the external evaluation workspace. `mvn package` completed successfully
again with 66 tests, 0 failures, and 0 errors. The scan command exited with code 0 and
regenerated the same artifact counts: 17 endpoints, 9 components, 6 entities, 22 tests,
and 302 evidence records.

## Generated Artifacts And Counts

| Artifact | Status |
| --- | --- |
| `.project-memory/project-map.json` | Generated in the 2026-05-30 retest and regenerated in the content-level scorecard pass. |
| `.project-memory/evidence-index.jsonl` | Generated in the 2026-05-30 retest and regenerated in the content-level scorecard pass. |
| `.project-memory/endpoints.md` | Generated in the 2026-05-30 retest and regenerated in the content-level scorecard pass. |
| `.project-memory/agent-guide.md` | Generated in the 2026-05-30 retest and regenerated in the content-level scorecard pass. |

| Count | Value |
| --- | --- |
| Endpoints | 17 |
| Components | 9 |
| Entities | 6 |
| Tests | 22 |
| Evidence records | 302 |

## Scorecard Summary

| Project/ref | Endpoints | Components | Entities | Tests | Evidence quality | `agent-guide.md` | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `spring-petclinic@3c06fbfc1e42eb40802e0d0ca989bc9226755804` | `2` | `2` | `1` | `1` | `1` | `1` | Content-level pass after parser fix: direct endpoint and component extraction matched bounded source searches. Entity, test, evidence, and guide usefulness is limited by inherited mapped-superclass IDs being absent and by noisy helper/nested class handling in the test inventory. |

## Scorecard: Endpoints

- Expected observations: Spring MVC controller routes supported by the current v0.1
  analyzer should be emitted in `project-map.json` and `endpoints.md` with evidence IDs.
  A bounded source search found supported controller and mapping annotations in
  `WelcomeController`, `CrashController`, `OwnerController`, `PetController`,
  `VisitController`, and `VetController`.
- Actual observations: The regenerated artifacts contain 17 endpoint facts. The emitted
  routes match the bounded source search, including `GET /`, `GET /oups`, owner create,
  find, update, and show routes, PetController class-level `/owners/{ownerId}` combined
  with pet method mappings, visit create routes, and both vet routes.
- False positives if found: None found in the bounded inspection.
- False negatives if found: None found in the bounded inspection. Exact completeness
  beyond supported Spring MVC annotations was not established.
- Evidence quality notes: Endpoint evidence IDs resolve to repository-relative paths,
  line ranges, annotation symbols, excerpts, and high confidence records. Sample checks
  included `OwnerController#showOwner` at
  `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java:166`.
- Output contract issues if found: None found.

## Scorecard: Components

- Expected observations: Direct class-level Spring stereotypes should be emitted under
  `components.items` with direct annotation evidence.
- Actual observations: A bounded source search found 9 direct supported stereotypes:
  six `@Controller` classes, two `@Configuration` classes, and one `@Component`
  formatter. `components.items` emits the same 9 classes with the expected stereotypes.
- False positives if found: None found in the bounded inspection.
- False negatives if found: None found in the bounded inspection. Runtime beans created
  through configuration methods or component scanning semantics were not evaluated
  because they are out of v0.1 scope.
- Evidence quality notes: Component evidence IDs resolve to annotation records with
  repository-relative paths, line ranges, excerpts, and high confidence. Sample checks
  included `CacheConfiguration` at
  `src/main/java/org/springframework/samples/petclinic/system/CacheConfiguration.java:31`.
- Output contract issues if found: None found.

## Scorecard: Entities

- Expected observations: Direct `@Entity`, direct `@Table(name = "...")`, field-level
  `@Id`, and supported direct relationship annotations should be emitted under
  `entities.items` with uncertainty preserved for unresolved relationship targets.
- Actual observations: A bounded source search found 6 direct `@Entity` classes with
  direct `@Table(name = "...")` annotations and 4 supported direct relationship
  annotations. `entities.items` emits all 6 entities, their table names, and the 4
  relationship facts with `target_resolution: "declared_type_only"` and
  `uncertainty: "target_type_not_resolved"`.
- False positives if found: None found for direct entity, table, or relationship facts.
- False negatives if found: No false negative was confirmed for fields declared directly
  on the emitted entity classes. A practical gap was observed: all emitted
  `identifier_fields` arrays are empty because the model inherits `id` from
  `org.springframework.samples.petclinic.model.BaseEntity`, a `@MappedSuperclass` with a
  direct field-level `@Id`. The current contract does not clearly say whether inherited
  mapped-superclass identifiers should be represented.
- Evidence quality notes: Direct entity/table/relationship evidence resolves with precise
  annotation lines and high confidence. There is no evidence for the inherited `@Id`
  because that fact is not emitted.
- Output contract issues if found: No JSON field-shape issue found. The mapped-superclass
  identifier behavior is a contract/product gap candidate rather than an observed schema
  violation.

## Scorecard: Tests

- Expected observations: Standard Maven `src/test/java` classes should be emitted under
  `tests.items`, with directly visible framework signals and naming-convention
  `tested_subjects` marked as inferred where applicable.
- Actual observations: A bounded file listing found 17 Java files under `src/test/java`.
  The generated inventory emits 22 class declarations, including nested classes and
  top-level helper/configuration classes such as `MysqlTestApplication` and
  `EntityUtils`. Eight naming-convention `tested_subjects` relations were emitted with
  `support_type: "inferred"` and `confidence: "medium"`.
- False positives if found: No strict contract-level false positive was confirmed if the
  intended contract is "all Java class declarations under supported test roots." A
  usability/contract gap was found: helper and nested configuration classes are rendered
  as "Test class" entries, and nested classes can inherit file-level framework-signal
  evidence from imports that are not specific to that nested class.
- False negatives if found: None found in the bounded inspection. Exact completeness was
  not established beyond comparing the generated 22 class declarations with the bounded
  test-root file/class inspection.
- Evidence quality notes: Test class evidence generally resolves to class declaration
  lines. Framework-signal evidence resolves, but signal attribution is noisy for nested
  helper/configuration classes because source-file imports are associated with each class
  declaration in the file.
- Output contract issues if found: No field-shape issue found. The distinction between
  "test-root class declaration" and "test class" needs a bounded follow-up.

## Scorecard: Evidence Quality

- Expected observations: Evidence IDs should resolve to repository-relative paths, line
  ranges, symbols, excerpts, and confidence labels in `evidence-index.jsonl`.
- Actual observations: The regenerated `evidence-index.jsonl` contains 302 records. A
  recursive reference check over `project-map.json` found 302 unique referenced evidence
  IDs and 0 missing references.
- False positives if found: No unresolved or fabricated evidence record was found in the
  bounded inspection. The test framework-signal records for nested helper/configuration
  classes are weak/noisy because file-level imports are reused as class-level signals.
- False negatives if found: No evidence exists for inherited mapped-superclass identifier
  facts because those facts are not emitted.
- Evidence quality notes: Endpoint, component, entity, and direct test class evidence is
  precise enough for v0.1. Relationship uncertainty is preserved. Test framework-signal
  evidence is noisier than the other categories when nested/helper classes are involved.
- Output contract issues if found: No evidence schema or reference-resolution issue was
  found. The weak nested-class signal attribution is a semantic gap candidate.

## Scorecard: `agent-guide.md`

- Expected observations: `agent-guide.md` should be generated deterministically from
  `project-map.json` and `evidence-index.jsonl`, using cautious wording and known-limit
  sections.
- Actual observations: The guide contains the required Stage 7.1 sections, resolves
  evidence references inline, uses cautious `Detected`, `Inferred`, and `Uncertain`
  wording, and lists known limits for Spring runtime behavior, ORM behavior, test
  execution/coverage, connectors, LLM summaries, RAG, Gradle/Kotlin, and multi-module
  Maven parsing.
- False positives if found: No unsupported architecture claim was found in the bounded
  inspection. The guide does render helper/nested test-root classes as "Test class"
  entries because that wording comes from the current structured test inventory.
- False negatives if found: The guide does not orient a persistence change toward
  `BaseEntity`, `NamedEntity`, or `Person`, where inherited mapped-superclass identifier
  and common fields live. This follows from the missing structured entity facts rather
  than a separate guide-only source analysis failure.
- Evidence quality notes: Guide references are readable and evidence-backed, but the
  test section and practical inspection order become noisy because every emitted test
  inventory item and inferred subject contributes paths.
- Output contract issues if found: No section-order or generated-file contract issue was
  found. Usefulness is limited by the entity and test inventory gaps noted above.

## Observations

### OBS-8-001: Scan aborts on Java 14+ `instanceof` pattern in tests

- Project/ref: `spring-petclinic@3c06fbfc1e42eb40802e0d0ca989bc9226755804`
- Observed artifact: CLI scan; expected `.project-memory/*` contract outputs were not
  generated.
- Expected: The Stage 8 pilot should produce all four v0.1 files for a Maven-style
  Java/Spring project with `src/main/java`.
- Actual: The scan exited with code 1 after preparing `.project-memory/`, before writing
  `project-map.json`, `evidence-index.jsonl`, `endpoints.md`, or `agent-guide.md`.
- False positive: None; no facts were emitted.
- False negative: No content-level false negative was confirmed. The full output set is
  unavailable because the run aborted.
- Evidence quality: No evidence records were generated.
- Output contract issue: The output files promised for supported source roots were not
  produced, but no output field-shape or evidence schema gap was identified.
- Notes: The failure message says JavaParser needs a language level that supports Java
  14+ pattern matching for `instanceof`.
- Retest: Addressed on 2026-05-30. The same pinned checkout scanned successfully after
  JavaParser was configured for modern Java syntax, and all four contract artifacts were
  generated.

### OBS-8-002: Direct endpoint and component facts match bounded source searches

- Project/ref: `spring-petclinic@3c06fbfc1e42eb40802e0d0ca989bc9226755804`
- Observed artifact: `.project-memory/project-map.json` `endpoints` and
  `components.items`; `.project-memory/endpoints.md`; `.project-memory/agent-guide.md`.
- Expected: Supported Spring MVC mapping annotations and direct class-level Spring
  stereotypes under `src/main/java` should be emitted with evidence references.
- Actual: Targeted source searches found 17 supported endpoint mappings and 9 direct
  supported stereotypes. The generated artifacts emitted the same counts and the same
  bounded set of controller/component classes.
- False positive: None found in the bounded inspection.
- False negative: None found in the bounded inspection. Exact completeness beyond
  supported annotation forms was not established.
- Evidence quality: Evidence references resolve to precise repository-relative source
  paths, annotation symbols, line ranges, excerpts, and high confidence records.
- Output contract issue: None found.
- Notes: This observation is bounded to direct supported annotations and does not claim
  Spring runtime route or bean graph reconstruction.

### OBS-8-003: Inherited mapped-superclass identifier is not represented

- Project/ref: `spring-petclinic@3c06fbfc1e42eb40802e0d0ca989bc9226755804`
- Observed artifact: `.project-memory/project-map.json` `entities.items[*].identifier_fields`
  and `agent-guide.md` `Detected JPA Entities`.
- Expected: Direct entity, table, identifier, and supported relationship facts should be
  emitted when in scope. If inherited mapped-superclass identifiers are out of scope, the
  contract should make that limitation clear for real projects using JPA inheritance.
- Actual: The 6 direct `@Entity` classes and 4 direct relationship annotations were
  emitted, but every `identifier_fields` array is empty. The target model inherits `id`
  from `org.springframework.samples.petclinic.model.BaseEntity`, a `@MappedSuperclass`
  containing a direct field-level `@Id`.
- False positive: None found.
- False negative: Practical false negative for project orientation: entity identifiers
  are absent from generated facts and the guide. Strict supported-scope classification is
  ambiguous because `OUTPUT_CONTRACT.md` does not say whether inherited
  `@MappedSuperclass` identifiers should be attached to entity facts.
- Evidence quality: Direct entity/table/relationship evidence is precise. No evidence is
  emitted for the inherited identifier because the fact is absent.
- Output contract issue: Possible contract gap around inherited mapped-superclass
  identifier semantics; no JSON schema violation was found.
- Notes: The bounded inspection did not evaluate full ORM inheritance, property access,
  generated values, or schema behavior.

### OBS-8-004: Test-root helper and nested classes make the tests inventory noisy

- Project/ref: `spring-petclinic@3c06fbfc1e42eb40802e0d0ca989bc9226755804`
- Observed artifact: `.project-memory/project-map.json` `tests.items`,
  `.project-memory/evidence-index.jsonl` test framework-signal evidence, and
  `.project-memory/agent-guide.md` `Detected Tests`.
- Expected: Standard Maven `src/test/java` class declarations should be emitted with
  directly visible framework signals and inferred tested subjects where naming
  conventions match production classes.
- Actual: The target has 17 Java files under `src/test/java`; the generated inventory
  emits 22 class declarations, including nested classes and helper/configuration classes
  such as `MysqlTestApplication`, `EntityUtils`,
  `PostgresIntegrationTests.PropertiesLogger`, and
  `CrashControllerIntegrationTests.TestConfiguration`. Some nested/helper classes receive
  JUnit or Spring Test signal evidence from source-file imports that are not specific to
  the nested class.
- False positive: No strict contract-level false positive was confirmed if the intended
  contract is "all Java class declarations under supported test roots." The user-facing
  "Test class" wording and class-level signal attribution are noisy for helper/config
  classes.
- False negative: None found in the bounded inspection. Exact completeness was not
  established beyond matching the bounded test-root class declaration count.
- Evidence quality: Test class evidence resolves to class declaration lines. Framework
  signal evidence resolves but can be weak for nested/helper classes because it is
  source-file-level evidence repeated for each class declaration in the file.
- Output contract issue: Possible contract gap around whether `tests.items` should mean
  every test-root class declaration, only executable test classes, or class declarations
  with an explicit role.
- Notes: Naming-convention `tested_subjects` remain correctly marked as inferred and do
  not claim coverage or execution behavior.

### OBS-8-005: `agent-guide.md` is contract-complete but inherits entity and test gaps

- Project/ref: `spring-petclinic@3c06fbfc1e42eb40802e0d0ca989bc9226755804`
- Observed artifact: `.project-memory/agent-guide.md`.
- Expected: The guide should use deterministic facts from `project-map.json` and
  `evidence-index.jsonl`, expose uncertainty, and avoid unsupported architecture claims.
- Actual: The guide contains the expected Stage 7.1 sections, uses cautious
  `Detected`, `Inferred`, and `Uncertain` wording, includes evidence references, and
  calls out known limits. It is useful for first-pass orientation, but it is noisy in the
  tests section and does not direct persistence inspection toward mapped superclasses
  because those facts are absent from the structured entity inventory.
- False positive: No unsupported architecture claim was found in the bounded inspection.
  Helper/nested test-root classes are rendered as "Test class" entries because they are
  present in the structured test inventory.
- False negative: The guide misses inherited identifier orientation through `BaseEntity`,
  `NamedEntity`, and `Person`; this follows from missing structured facts rather than
  a separate guide-only analyzer pass.
- Evidence quality: Guide references resolve and remain readable for endpoints,
  components, entities, and tests. The test section is less useful because noisy test
  inventory entries contribute many evidence references.
- Output contract issue: None found for required section order or cautious wording.
- Notes: The guide correctly avoids adding unsupported source summaries or architecture
  beyond generated deterministic facts.

## Follow-up Tasks

### EVAL-8-001: Configure JavaParser language level for modern Java syntax

- Bounded task id: `EVAL-8-001`
- Status: Addressed in the 2026-05-30 parser-fix retest; the scan reached contract
  artifact generation on the same pinned `spring-petclinic` checkout.
- Project/ref: `spring-petclinic@3c06fbfc1e42eb40802e0d0ca989bc9226755804`
- Observed artifact: CLI scan failure before `.project-memory/project-map.json`,
  `.project-memory/evidence-index.jsonl`, `.project-memory/endpoints.md`, and
  `.project-memory/agent-guide.md` generation.
- Suspected cause: `TestInventoryAnalyzer` parses test sources with JavaParser's default
  language level, which rejects Java 14+ pattern matching syntax while the project and
  CLI target modern Java.
- Affected contract/doc: `docs/architecture/OUTPUT_CONTRACT.md` describes generation of
  all four files when supported source roots exist. No field-shape contract change is
  currently indicated.
- Proposed validation: Add a focused fixture containing a `src/test/java` class with an
  `instanceof` pattern, assert that `mvn test` passes, and repeat the
  `spring-petclinic` scan at this pinned commit.
- Non-goals: Do not add connectors, LLM calls, SaaS, web UI, repository chat, generic
  RAG, automatic code modification, or multi-language analysis. Do not broaden the scan
  beyond the documented Java/Spring v0.1 scope.

### EVAL-8-002: Clarify or support inherited `@MappedSuperclass` identifier facts

- Bounded task id: `EVAL-8-002`
- Status: Contract update required before analyzer implementation.
- Project/ref: `spring-petclinic@3c06fbfc1e42eb40802e0d0ca989bc9226755804`
- Observed artifact: `.project-memory/project-map.json` `entities.items[*].identifier_fields`
  and `.project-memory/agent-guide.md` `Detected JPA Entities`.
- Suspected cause: `JpaEntityAnalyzer` records identifier fields declared directly on
  emitted `@Entity` classes but does not attach `@Id` fields inherited from
  `@MappedSuperclass` ancestors such as `BaseEntity`.
- Affected contract/doc: `docs/architecture/OUTPUT_CONTRACT.md` and
  `docs/architecture/EVIDENCE_MODEL.md` if inherited mapped-superclass identifiers are
  added or if the out-of-scope limit needs explicit documentation.
- Contract prerequisite: Before changing analyzer behavior, `identifier_fields` needs to
  distinguish direct entity declarations from direct source-visible `@MappedSuperclass`
  declarations. The minimal contract shape should add `declaring_class` and
  `source_kind`, with `source_kind` limited to `declared` and `mapped_superclass`.
- Proposed validation: Add a focused fixture with `BaseEntity` annotated
  `@MappedSuperclass`, a direct field-level `@Id`, and one or two `@Entity` subclasses.
  Assert the chosen behavior in `project-map.json`, `evidence-index.jsonl`, and
  `agent-guide.md`, then repeat the `spring-petclinic` scan at the pinned commit.
- Non-goals: Do not implement full ORM runtime behavior, property-access mapping,
  embedded IDs, generated-value semantics, schema inference, join-column analysis, or
  symbol-solving beyond the bounded inheritance decision.

### EVAL-8-003: Clarify test inventory semantics for helper and nested classes

- Bounded task id: `EVAL-8-003`
- Project/ref: `spring-petclinic@3c06fbfc1e42eb40802e0d0ca989bc9226755804`
- Observed artifact: `.project-memory/project-map.json` `tests.items`,
  `.project-memory/evidence-index.jsonl` test framework-signal records, and
  `.project-memory/agent-guide.md` `Detected Tests`.
- Suspected cause: `TestInventoryAnalyzer` emits every class declaration under
  `src/test/java`, including helper/configuration and nested classes, and attaches
  source-file-level framework imports or annotations to each class declaration in the
  file.
- Affected contract/doc: `docs/architecture/OUTPUT_CONTRACT.md` and
  `docs/architecture/EVIDENCE_MODEL.md` if `tests.items` semantics, class roles, nested
  class handling, or framework-signal attribution are changed or clarified.
- Proposed validation: Add a focused fixture containing a top-level executable test
  class, a top-level helper class, a nested `@Nested` test class, and a nested
  configuration/helper class. Assert whether each should be emitted, how it should be
  labeled in `agent-guide.md`, and which framework-signal evidence should attach to
  which class.
- Non-goals: Do not add test execution, coverage, assertion analysis, call graph
  resolution, complete subject mapping, CI integration, or runtime framework behavior.

## Network And Leakage Note

- Source was cloned only under `/private/tmp/agent-project-memory-eval/`.
- No source was uploaded to external analysis services.
- No LLM calls were used for analysis or fact extraction.
- Third-party source and generated outputs were not committed.
- Generated third-party contract outputs were produced only under
  `/private/tmp/agent-project-memory-eval/spring-petclinic/.project-memory/` during the
  parser-fix retest and were not committed.
