# Stage 8 Pilot Evaluation: Spring PetClinic `3c06fbf`

Evaluation date: 2026-05-30

## Scope

- Project URL: `https://github.com/spring-projects/spring-petclinic.git`
- Exact commit: `3c06fbfc1e42eb40802e0d0ca989bc9226755804`
- Local-only workspace path: `/private/tmp/agent-project-memory-eval/spring-petclinic`
- Evaluation result: blocked before contract artifact generation.
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

## Generated Artifacts And Counts

| Artifact | Status |
| --- | --- |
| `.project-memory/project-map.json` | Missing; scan aborted before generation. |
| `.project-memory/evidence-index.jsonl` | Missing; scan aborted before generation. |
| `.project-memory/endpoints.md` | Missing; scan aborted before generation. |
| `.project-memory/agent-guide.md` | Missing; scan aborted before generation. |

| Count | Value |
| --- | --- |
| Endpoints | N/A; `project-map.json` was not generated. |
| Components | N/A; `project-map.json` was not generated. |
| Entities | N/A; `project-map.json` was not generated. |
| Tests | N/A; `project-map.json` was not generated. |
| Evidence records | N/A; `evidence-index.jsonl` was not generated. |

## Scorecard Summary

| Project/ref | Endpoints | Components | Entities | Tests | Evidence quality | `agent-guide.md` | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `spring-petclinic@3c06fbfc1e42eb40802e0d0ca989bc9226755804` | `0` | `0` | `0` | `0` | `0` | `0` | Blocked before contract artifacts were generated. |

## Scorecard: Endpoints

- Expected observations: Spring MVC controller routes supported by the current v0.1
  analyzer should be emitted in `project-map.json` and `endpoints.md` with evidence IDs.
- Actual observations: No endpoint artifact was generated because the scan aborted while
  parsing tests.
- False positives if found: None inspectable; no endpoint facts were emitted.
- False negatives if found: No content-level false negative was confirmed because output
  generation was blocked. The endpoint inventory is unavailable for this project/ref.
- Evidence quality notes: No endpoint evidence records were emitted.
- Output contract issues if found: No endpoint field-shape issue was inspectable. The
  expected contract files were absent due to a runtime parse blocker.

## Scorecard: Components

- Expected observations: Direct class-level Spring stereotypes should be emitted under
  `components.items` with direct annotation evidence.
- Actual observations: No component inventory was generated because the scan aborted
  before writing `project-map.json`.
- False positives if found: None inspectable; no component facts were emitted.
- False negatives if found: No content-level false negative was confirmed because output
  generation was blocked. The component inventory is unavailable for this project/ref.
- Evidence quality notes: No component evidence records were emitted.
- Output contract issues if found: No component field-shape issue was inspectable. The
  expected contract files were absent due to a runtime parse blocker.

## Scorecard: Entities

- Expected observations: Direct `@Entity`, direct `@Table(name = "...")`, field-level
  `@Id`, and supported direct relationship annotations should be emitted under
  `entities.items` with uncertainty preserved for unresolved relationship targets.
- Actual observations: No entity inventory was generated because the scan aborted before
  writing `project-map.json`.
- False positives if found: None inspectable; no entity facts were emitted.
- False negatives if found: No content-level false negative was confirmed because output
  generation was blocked. The entity inventory is unavailable for this project/ref.
- Evidence quality notes: No entity evidence records were emitted.
- Output contract issues if found: No entity field-shape issue was inspectable. The
  expected contract files were absent due to a runtime parse blocker.

## Scorecard: Tests

- Expected observations: Standard Maven `src/test/java` classes should be emitted under
  `tests.items`, with directly visible framework signals and naming-convention
  `tested_subjects` marked as inferred where applicable.
- Actual observations: Test inventory analysis started but failed on a Java language
  feature supported since Java 14: pattern matching for `instanceof`.
- False positives if found: None inspectable; no test facts were emitted.
- False negatives if found: The tests inventory is unavailable because parsing failed.
  This is a run-level blocker rather than a confirmed content-level miss.
- Evidence quality notes: No test evidence records were emitted.
- Output contract issues if found: No test field-shape issue was inspectable. The current
  implementation did not reach the contract output phase for this real project.

## Scorecard: Evidence Quality

- Expected observations: Evidence IDs should resolve to repository-relative paths, line
  ranges, symbols, excerpts, and confidence labels in `evidence-index.jsonl`.
- Actual observations: `evidence-index.jsonl` was not generated.
- False positives if found: None inspectable; no evidence records were emitted.
- False negatives if found: Evidence for all expected generated facts is unavailable due
  to the scan abort.
- Evidence quality notes: The blocker prevents evidence quality evaluation for this
  project/ref.
- Output contract issues if found: No evidence field-shape issue was inspectable. The
  missing evidence index is a generation blocker against the existing contract output
  expectation, not a proposed evidence schema change.

## Scorecard: `agent-guide.md`

- Expected observations: `agent-guide.md` should be generated deterministically from
  `project-map.json` and `evidence-index.jsonl`, using cautious wording and known-limit
  sections.
- Actual observations: `agent-guide.md` was not generated because prerequisite structured
  outputs were not generated.
- False positives if found: None inspectable; no guide content was emitted.
- False negatives if found: The guide is unavailable due to the scan abort.
- Evidence quality notes: No guide evidence references were emitted.
- Output contract issues if found: No guide content issue was inspectable. The expected
  guide file was absent due to the runtime parse blocker.

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

## Follow-up Tasks

### EVAL-8-001: Configure JavaParser language level for modern Java syntax

- Bounded task id: `EVAL-8-001`
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

## Network And Leakage Note

- Source was cloned only under `/private/tmp/agent-project-memory-eval/`.
- No source was uploaded to external analysis services.
- No LLM calls were used for analysis or fact extraction.
- Third-party source and generated outputs were not committed.
- Generated third-party contract outputs were not committed; none were produced for this
  blocked run.
