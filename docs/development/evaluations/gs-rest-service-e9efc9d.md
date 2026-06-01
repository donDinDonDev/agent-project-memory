# Stage 8 Pilot Evaluation: Spring Guide REST Service `e9efc9d`

Evaluation date: 2026-06-01

## Scope

- Project URL: `https://github.com/spring-guides/gs-rest-service.git`
- Exact commit: `e9efc9dfa0abe8cf8e15cf0e71830b5125322cae`
- Local-only workspace path: `/private/tmp/agent-project-memory-eval/gs-rest-service`
- Scanned Maven project path: `/private/tmp/agent-project-memory-eval/gs-rest-service/complete`
- Repository worktree state before editing: clean; `git status --short` produced no output.
- Evaluation result: scan completed and generated all v0.1 contract artifacts.

This project is the small Stage 8 baseline for a source-visible Maven Spring MVC sample.
The scan intentionally targets `complete/`, which contains the runnable Maven sample
with `pom.xml`, `src/main/java`, and `src/test/java`.

## Commands Run

```sh
mvn package
git clone https://github.com/spring-guides/gs-rest-service.git /private/tmp/agent-project-memory-eval/gs-rest-service
git -C /private/tmp/agent-project-memory-eval/gs-rest-service checkout e9efc9dfa0abe8cf8e15cf0e71830b5125322cae
git -C /private/tmp/agent-project-memory-eval/gs-rest-service rev-parse HEAD
git -C /private/tmp/agent-project-memory-eval/gs-rest-service status --short
find /private/tmp/agent-project-memory-eval/gs-rest-service -maxdepth 2 -name pom.xml -print
find /private/tmp/agent-project-memory-eval/gs-rest-service -maxdepth 2 -type d -name src -print
java -jar target/agent-project-memory-0.1.0.jar scan /private/tmp/agent-project-memory-eval/gs-rest-service/complete
find /private/tmp/agent-project-memory-eval/gs-rest-service/complete/.project-memory -maxdepth 1 -type f -print
jq '{endpoints: (.endpoints | length), components: (.components.items | length), entities: (.entities.items | length), tests: (.tests.items | length)}' /private/tmp/agent-project-memory-eval/gs-rest-service/complete/.project-memory/project-map.json
wc -l /private/tmp/agent-project-memory-eval/gs-rest-service/complete/.project-memory/evidence-index.jsonl
jq -r '.endpoints[] | [.controller_class, .handler_method, (.http_methods|join(",")), .http_method_semantics, (.paths|join(",")), (.request_parameters|map(.name + ":" + .source + ":" + .java_type)|join(";")), (.request_body_type // "null"), (.response_type // "null"), .mapping_source.kind] | @tsv' /private/tmp/agent-project-memory-eval/gs-rest-service/complete/.project-memory/project-map.json
jq -r '.components.items[] | [.class_name, (.stereotypes|join(","))] | @tsv' /private/tmp/agent-project-memory-eval/gs-rest-service/complete/.project-memory/project-map.json
jq -r '.tests.items[] | [.class_name, .source_path, (.framework_signals|map(.name)|join(",")), (.tested_subjects|map(.class_name + "(" + .support_type + ":" + .confidence + (if .uncertainty then ":" + .uncertainty else "" end) + ")")|join(";"))] | @tsv' /private/tmp/agent-project-memory-eval/gs-rest-service/complete/.project-memory/project-map.json
jq -n --slurpfile pm /private/tmp/agent-project-memory-eval/gs-rest-service/complete/.project-memory/project-map.json --slurpfile ev /private/tmp/agent-project-memory-eval/gs-rest-service/complete/.project-memory/evidence-index.jsonl '($ev | map(.id)) as $ids | ([$pm[0] | .. | objects | .evidence_ids? // empty | .[]] | unique) as $refs | {referenced_evidence_ids: ($refs | length), indexed_evidence_records: ($ids | length), missing_references: ($refs | map(select(($ids | index(.)) | not)))}'
sed -n '1,220p' /private/tmp/agent-project-memory-eval/gs-rest-service/complete/.project-memory/endpoints.md
sed -n '1,280p' /private/tmp/agent-project-memory-eval/gs-rest-service/complete/.project-memory/agent-guide.md
rg -n "@(RestController|Controller|RequestMapping|GetMapping|PostMapping|PutMapping|PatchMapping|DeleteMapping|RequestParam|RequestBody|PathVariable)" /private/tmp/agent-project-memory-eval/gs-rest-service/complete/src/main/java
rg -n "@(Component|Service|Repository|Configuration|RestController|Controller|SpringBootApplication)" /private/tmp/agent-project-memory-eval/gs-rest-service/complete/src/main/java
rg -n "@(Entity|Table|Id|MappedSuperclass|ManyToOne|OneToMany|OneToOne|ManyToMany)" /private/tmp/agent-project-memory-eval/gs-rest-service/complete/src/main/java
find /private/tmp/agent-project-memory-eval/gs-rest-service/complete/src/test/java -type f -name '*.java' -print
rg -n "class .*\\b(Test|Tests|IT)\\b|@(SpringBootTest|DataJpaTest|WebMvcTest|ContextConfiguration|Test|Nested|RunWith)" /private/tmp/agent-project-memory-eval/gs-rest-service/complete/src/test/java
jq -r '.source_type' /private/tmp/agent-project-memory-eval/gs-rest-service/complete/.project-memory/evidence-index.jsonl | sort | uniq -c
```

## Run Results

`mvn package` completed successfully. The packaged build ran 81 tests with 0 failures
and 0 errors, then completed the packaged CLI smoke test.

The target checkout was pinned successfully:

```text
e9efc9dfa0abe8cf8e15cf0e71830b5125322cae
```

The target checkout was clean before the scan. After the scan, the external checkout
showed only the expected untracked generated output:

```text
?? complete/.project-memory/
```

The scan command exited with code 0 and produced:

```text
Generated project-map.json with 1 endpoint facts and 1 component facts and 0 entity facts and 1 test facts.
Generated endpoints.md with 1 endpoint facts.
Generated evidence-index.jsonl with 12 evidence records.
Generated agent-guide.md.
```

The generated outputs stayed under
`/private/tmp/agent-project-memory-eval/gs-rest-service/complete/.project-memory/` and
were not copied into this repository.

## Network And Leakage

- Network access was used only to clone the public target repository.
- Source was cloned only under `/private/tmp/agent-project-memory-eval/`.
- No external analysis services were used.
- No LLM calls were used for source analysis, fact extraction, or evidence generation.
- Third-party source and generated `.project-memory/` outputs were not committed.
- The analyzer run itself used the local checkout and local packaged CLI only.

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
| Components | 1 |
| Entities | 0 |
| Tests | 1 |
| Evidence records | 12 |
| Referenced evidence IDs | 12 |
| Missing evidence references | 0 |

Evidence records by type:

| Evidence type | Count |
| --- | --- |
| `annotation` | 6 |
| `build_file` | 1 |
| `code_symbol` | 4 |
| `test_file` | 1 |

## Scorecard Summary

| Project/ref | Endpoints | Components | Entities | Tests | Evidence quality | `agent-guide.md` | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `gs-rest-service@e9efc9dfa0abe8cf8e15cf0e71830b5125322cae` (`complete/`) | `2` | `2` | `N/A` | `2` | `2` | `2` | The small source-visible Maven sample generated all expected v0.1 artifacts. Bounded source searches matched the emitted endpoint, direct supported component, absent entity inventory, and one test class. No meaningful issues were found. |

## Scorecard: Endpoints

- Expected observations from bounded manual inspection: Supported Spring MVC controller
  and method-level mapping annotations under `complete/src/main/java` should be emitted
  with evidence. A bounded source search found one direct `@RestController`,
  `GreetingController`, with one direct `@GetMapping("/greeting")` handler and one
  `@RequestParam` parameter.
- Actual observations from generated artifacts: `project-map.json` and `endpoints.md`
  contain one endpoint: `GET /greeting` for
  `com.example.restservice.GreetingController#greeting`, with request parameter
  `name` from `@RequestParam`, no request body, response type `Greeting`, and
  `mapping_source.kind: "direct_handler_method"`.
- False positives if found: None found in the bounded inspection.
- False negatives if found: None found for supported direct Spring MVC annotations in
  the bounded inspection. Exact completeness beyond the documented v0.1 annotation
  forms was not established.
- Evidence quality notes: Endpoint evidence IDs resolve to repository-relative paths,
  annotation symbols, line ranges, excerpts, and high confidence records.
- Output contract issues if found: None found.

## Scorecard: Components

- Expected observations from bounded manual inspection: Direct class-level supported
  Spring stereotypes should be emitted under `components.items`.
- Actual observations from generated artifacts: `components.items` emits
  `com.example.restservice.GreetingController` with `@RestController`, matching the
  bounded search for supported direct stereotypes. `RestServiceApplication` has
  `@SpringBootApplication`, which is outside the current direct stereotype contract.
- False positives if found: None found.
- False negatives if found: None found for supported direct stereotypes.
- Evidence quality notes: Component evidence resolves to the direct `@RestController`
  annotation record with a repository-relative path, line range, excerpt, and high
  confidence.
- Output contract issues if found: None found.

## Scorecard: Entities

- Expected observations from bounded manual inspection: This sample is not expected to
  contain JPA entities. If direct JPA annotations were present, they should be emitted
  with evidence and relationship uncertainty preserved.
- Actual observations from generated artifacts: `entities.items` is empty with
  `analysis_status: "analyzed"`. A bounded source search found no direct `@Entity`,
  `@Table`, `@Id`, `@MappedSuperclass`, or supported relationship annotations under
  `complete/src/main/java`.
- False positives if found: None found.
- False negatives if found: None found for direct JPA annotations in the bounded
  inspection.
- Evidence quality notes: No entity evidence is expected for this project.
- Output contract issues if found: None found.

## Scorecard: Tests

- Expected observations from bounded manual inspection: Test-like Java classes under
  `complete/src/test/java` should be emitted with directly visible framework signals and
  naming-convention tested-subject inferences when applicable.
- Actual observations from generated artifacts: `tests.items` emits one test class,
  `com.example.restservice.GreetingControllerTests`, with source path
  `src/test/java/com/example/restservice/GreetingControllerTests.java`, framework
  signals `JUnit Jupiter` and `Spring Test`, and one inferred tested subject,
  `com.example.restservice.GreetingController`, with `support_type: "inferred"` and
  `confidence: "medium"`.
- False positives if found: None found.
- False negatives if found: None found in the bounded inspection.
- Evidence quality notes: Test class, import, `@SpringBootTest`, and method-level
  `@Test` evidence records resolve with precise line ranges and high confidence.
- Output contract issues if found: None found.

## Scorecard: Evidence Quality

- Expected observations from bounded manual inspection: Every generated evidence ID
  referenced by `project-map.json` should resolve in `evidence-index.jsonl`.
- Actual observations from generated artifacts: A recursive evidence-reference check
  found 12 unique referenced evidence IDs, 12 indexed evidence records, and no missing
  references. Evidence paths are repository-relative to the scanned `complete/` project.
- False positives if found: None found.
- False negatives if found: None found for emitted facts.
- Evidence quality notes: The evidence set is small and precise enough for v0.1. It
  includes `build_file`, endpoint/component annotations, production `code_symbol`
  evidence for the inferred test subject, and test evidence.
- Output contract issues if found: None found.

## Scorecard: `agent-guide.md`

- Expected observations from bounded manual inspection: The guide should use
  deterministic facts, expose known limits, and avoid unsupported architecture claims.
- Actual observations from generated artifacts: The guide contains the expected
  agent-guide.md sections, reports Maven layout, one detected endpoint, one direct component, no
  detected JPA entities, one detected test class, and the standard known-limit section.
  It uses cautious `Detected`, `Inferred`, and `Uncertain` wording and does not invent a
  broader architecture.
- False positives if found: None found.
- False negatives if found: None found for emitted facts.
- Evidence quality notes: Inline guide references resolve to the evidence index and are
  concise for this small sample.
- Output contract issues if found: None found.

## Observations

### OBS-8-006: Small source-visible REST sample matches v0.1 outputs

- Project/ref: `gs-rest-service@e9efc9dfa0abe8cf8e15cf0e71830b5125322cae`
  (`complete/`)
- Observed artifact: `.project-memory/project-map.json`,
  `.project-memory/evidence-index.jsonl`, `.project-memory/endpoints.md`, and
  `.project-memory/agent-guide.md`.
- Expected: The Stage 8 pilot should generate all four v0.1 files for the Maven-capable
  `complete/` sample, with direct Spring MVC endpoint/component facts, no invented JPA
  facts, a minimal test inventory, and resolving evidence.
- Actual: All four artifacts were generated. Counts were 1 endpoint, 1 component, 0
  entities, 1 test, and 12 evidence records. The emitted facts matched bounded source
  searches for supported annotations and test-like classes.
- False positive: None found.
- False negative: None found in the bounded inspection.
- Evidence quality: Evidence IDs resolve to repository-relative paths, line ranges,
  symbols, excerpts, and high confidence records. Recursive checking found 0 missing
  references.
- Output contract issue: None found.
- Notes: `@SpringBootApplication` on `RestServiceApplication` was observed during the
  component search, but it is outside the current supported direct stereotype list and
  is not treated as a miss for this evaluation.

## Follow-Up Tasks

No new `EVAL-8-xxx` follow-up task was opened from this evaluation. No meaningful
analyzer issue, output-contract mismatch, evidence integrity issue, or guide issue was
found in the bounded Stage 8 inspection of `gs-rest-service` at the pinned commit.

## Validation

Final validation for this report was run from this repository:

```sh
mvn package
java -jar target/agent-project-memory-0.1.0.jar scan /private/tmp/agent-project-memory-eval/gs-rest-service/complete
git diff --check
git diff --stat
git status --short
```

Results:

- `mvn package` passed with 81 tests, 0 failures, and 0 errors.
- The pinned `gs-rest-service/complete` scan completed successfully and generated all
  four v0.1 contract artifacts.
- Recursive evidence-reference checking found 12 referenced evidence IDs, 12 indexed
  evidence records, and 0 missing references.
- `git diff --check`, `git diff --stat`, and `git status --short` were reviewed before
  handoff.
