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

## Generated Artifacts And Counts

| Artifact | Status |
| --- | --- |
| `.project-memory/project-map.json` | Generated in the 2026-05-30 retest. |
| `.project-memory/evidence-index.jsonl` | Generated in the 2026-05-30 retest. |
| `.project-memory/endpoints.md` | Generated in the 2026-05-30 retest. |
| `.project-memory/agent-guide.md` | Generated in the 2026-05-30 retest. |

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
| `spring-petclinic@3c06fbfc1e42eb40802e0d0ca989bc9226755804` | `TBD` | `TBD` | `TBD` | `TBD` | `TBD` | `TBD` | Parser-fix retest generated all four contract artifacts. Content-level scorecard review was not repeated in this bounded task. |

## Scorecard: Endpoints

- Expected observations: Spring MVC controller routes supported by the current v0.1
  analyzer should be emitted in `project-map.json` and `endpoints.md` with evidence IDs.
- Actual observations: The 2026-05-30 parser-fix retest generated 17 endpoint facts and
  `endpoints.md`.
- False positives if found: Not evaluated in this bounded parser-fix retest.
- False negatives if found: Not evaluated in this bounded parser-fix retest.
- Evidence quality notes: Endpoint evidence was generated, but evidence quality was not
  manually rescored in this bounded parser-fix retest.
- Output contract issues if found: No output generation blocker remained. Field-shape
  review was not repeated in this bounded parser-fix retest.

## Scorecard: Components

- Expected observations: Direct class-level Spring stereotypes should be emitted under
  `components.items` with direct annotation evidence.
- Actual observations: The 2026-05-30 parser-fix retest generated 9 component facts.
- False positives if found: Not evaluated in this bounded parser-fix retest.
- False negatives if found: Not evaluated in this bounded parser-fix retest.
- Evidence quality notes: Component evidence was generated, but evidence quality was not
  manually rescored in this bounded parser-fix retest.
- Output contract issues if found: No output generation blocker remained. Field-shape
  review was not repeated in this bounded parser-fix retest.

## Scorecard: Entities

- Expected observations: Direct `@Entity`, direct `@Table(name = "...")`, field-level
  `@Id`, and supported direct relationship annotations should be emitted under
  `entities.items` with uncertainty preserved for unresolved relationship targets.
- Actual observations: The 2026-05-30 parser-fix retest generated 6 entity facts.
- False positives if found: Not evaluated in this bounded parser-fix retest.
- False negatives if found: Not evaluated in this bounded parser-fix retest.
- Evidence quality notes: Entity evidence was generated, but evidence quality was not
  manually rescored in this bounded parser-fix retest.
- Output contract issues if found: No output generation blocker remained. Field-shape
  review was not repeated in this bounded parser-fix retest.

## Scorecard: Tests

- Expected observations: Standard Maven `src/test/java` classes should be emitted under
  `tests.items`, with directly visible framework signals and naming-convention
  `tested_subjects` marked as inferred where applicable.
- Actual observations: The 2026-05-30 parser-fix retest generated 22 test facts.
- False positives if found: Not evaluated in this bounded parser-fix retest.
- False negatives if found: Not evaluated in this bounded parser-fix retest.
- Evidence quality notes: Test evidence was generated, but evidence quality was not
  manually rescored in this bounded parser-fix retest.
- Output contract issues if found: No output generation blocker remained. Field-shape
  review was not repeated in this bounded parser-fix retest.

## Scorecard: Evidence Quality

- Expected observations: Evidence IDs should resolve to repository-relative paths, line
  ranges, symbols, excerpts, and confidence labels in `evidence-index.jsonl`.
- Actual observations: The 2026-05-30 parser-fix retest generated
  `evidence-index.jsonl` with 302 evidence records.
- False positives if found: Not evaluated in this bounded parser-fix retest.
- False negatives if found: Not evaluated in this bounded parser-fix retest.
- Evidence quality notes: Evidence records were generated, but line-level quality was not
  manually rescored in this bounded parser-fix retest.
- Output contract issues if found: No evidence output generation blocker remained.
  Field-shape review was not repeated in this bounded parser-fix retest.

## Scorecard: `agent-guide.md`

- Expected observations: `agent-guide.md` should be generated deterministically from
  `project-map.json` and `evidence-index.jsonl`, using cautious wording and known-limit
  sections.
- Actual observations: The 2026-05-30 parser-fix retest generated `agent-guide.md`.
- False positives if found: Not evaluated in this bounded parser-fix retest.
- False negatives if found: Not evaluated in this bounded parser-fix retest.
- Evidence quality notes: Guide evidence references were generated, but guide quality was
  not manually rescored in this bounded parser-fix retest.
- Output contract issues if found: No guide output generation blocker remained.
  Field-shape/content review was not repeated in this bounded parser-fix retest.

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

## Network And Leakage Note

- Source was cloned only under `/private/tmp/agent-project-memory-eval/`.
- No source was uploaded to external analysis services.
- No LLM calls were used for analysis or fact extraction.
- Third-party source and generated outputs were not committed.
- Generated third-party contract outputs were produced only under
  `/private/tmp/agent-project-memory-eval/spring-petclinic/.project-memory/` during the
  parser-fix retest and were not committed.
