# Evaluation Plan

This document is the Stage 8 runbook for evaluating `agent-project-memory` on real
open-source Spring projects. Stage 8 evaluation records observations only. Analyzer,
contract, script, and product changes must be split into later bounded tasks.

Stage 8 is closed for the v0.1 release-candidate after the linked evaluation reports.
Keep this file as the historical runbook and baseline for future pilot scans.

## Stage 8 Scope

Stage 8 is limited to:

- Java/Spring-first projects.
- Maven-first project layouts.
- Local-first scanning of checked-out source trees.
- Deterministic analyzer outputs only:
  - `.project-memory/project-map.json`
  - `.project-memory/evidence-index.jsonl`
  - `.project-memory/endpoints.md`
  - `.project-memory/agent-guide.md`

The evaluation asks whether the current v0.1 outputs are accurate, useful, and
evidence-backed for representative Spring codebases. It must not change analyzer
behavior during the evaluation slice.

## Selected Projects

Refs were verified on 2026-05-30 using GitHub REST API metadata. Re-verify refs before a
later pilot scan if reproducibility matters for the run date.

| Project | Repository URL | Ref | License note | Why selected | Expected signals |
| --- | --- | --- | --- | --- | --- |
| Spring PetClinic | `https://github.com/spring-projects/spring-petclinic` | commit `3c06fbfc1e42eb40802e0d0ca989bc9226755804` | Apache-2.0, verified from repository license metadata | Canonical Spring sample, single Maven root, Spring MVC controllers, JPA entities, and tests. Good first real-project baseline. | Endpoints: MVC controller routes. Components: direct Spring stereotypes such as controllers and services. Entities: direct JPA model classes and basic relationships. Tests: standard `src/test/java` inventory and naming-convention subjects. Evidence: annotation/build/test evidence should be high quality. Guide usefulness: should orient an agent to controllers, domain model, tests, and known uncertainty. |
| Spring PetClinic REST | `https://github.com/spring-petclinic/spring-petclinic-rest` | tag `v4.0.2`, commit `d8026bb5bcc58145b95a66a7f8e7694f0fae142f` | Apache-2.0, verified from repository license metadata | REST API variant of PetClinic with Maven, Spring MVC, JPA, security, OpenAPI-related structure, and a larger test surface. Useful as a moderate project and known-limitation probe for generated API sources. | Endpoints: REST controller mappings when declared in scanned source; generated-source or interface-only mappings may become false negatives. Components: services, controllers, configuration, repositories when directly stereotyped. Entities: direct JPA annotations and relationships. Tests: Spring/JUnit signals and naming-convention subjects. Evidence: should expose whether evidence remains precise in a larger tree. Guide usefulness: should distinguish reliable extracted facts from misses or uncertainty. |
| Spring Guide: REST Service | `https://github.com/spring-guides/gs-rest-service` | commit `e9efc9dfa0abe8cf8e15cf0e71830b5125322cae`; scan subproject `complete/` | Apache-2.0, verified from repository license metadata | Very small Maven Spring MVC project for manual review. It keeps the first evaluation set grounded with a simple endpoint and simple tests. | Endpoints: one small REST endpoint set. Components: controller/application stereotypes. Entities: expected none. Tests: simple `src/test/java` inventory. Evidence: easy manual line-level validation. Guide usefulness: should stay concise and avoid inventing architecture. |

### Refs Pending Verification

None for the Stage 8.1 selected set. If a selected project is replaced later and its ref
cannot be verified without network access, add it here with the exact command that should
verify it, such as:

```sh
git ls-remote --tags https://github.com/<owner>/<repo>.git <tag>
git ls-remote https://github.com/<owner>/<repo>.git <commit-or-branch>
```

## Local Workspace Rules

- Clone third-party projects outside this repository, for example under
  `<external-eval-dir>/`.
- Do not vendor third-party source into this repository.
- Do not copy third-party source snippets into this repository beyond short evidence
  examples needed in an observation.
- Do not commit generated `.project-memory/` outputs from third-party repositories in
  this slice.
- Keep each third-party checkout pinned to its selected ref before scanning.
- Keep all generated artifacts inside the evaluated project's own `.project-memory/`
  directory.

Suggested local layout:

```text
<external-eval-dir>/
  spring-petclinic/
  spring-petclinic-rest/
  gs-rest-service/
```

## Evaluation Procedure

1. Build the packaged CLI locally from this repository:

   ```sh
   mvn package
   ```

2. Clone or update each selected third-party project under
   `<external-eval-dir>/`, then check out the pinned ref. Example:

   ```sh
   git clone https://github.com/spring-projects/spring-petclinic.git <external-eval-dir>/spring-petclinic
   git -C <external-eval-dir>/spring-petclinic checkout 3c06fbfc1e42eb40802e0d0ca989bc9226755804
   ```

3. Run the packaged CLI against the local project path. Use the subproject path when the
   selected project stores the Maven app below the repository root:

   ```sh
   java -jar target/agent-project-memory-0.1.0-SNAPSHOT.jar scan <external-eval-dir>/spring-petclinic
   java -jar target/agent-project-memory-0.1.0-SNAPSHOT.jar scan <external-eval-dir>/spring-petclinic-rest
   java -jar target/agent-project-memory-0.1.0-SNAPSHOT.jar scan <external-eval-dir>/gs-rest-service/complete
   ```

4. Inspect generated local artifacts only:

   ```text
   <evaluated-project>/.project-memory/project-map.json
   <evaluated-project>/.project-memory/evidence-index.jsonl
   <evaluated-project>/.project-memory/endpoints.md
   <evaluated-project>/.project-memory/agent-guide.md
   ```

5. Record observations using the format below. Do not fix analyzer behavior during the
   evaluation run.

## Scorecard

Use `2`, `1`, `0`, or `N/A` for each category:

- `2`: accurate enough for v0.1, materially complete for the supported scope, and
  evidence-backed.
- `1`: usable, but has bounded misses, weak evidence, or unclear uncertainty labels.
- `0`: absent, misleading, contract-breaking, or not useful for the supported scope.
- `N/A`: signal is not expected in the selected project.

| Category | What to check |
| --- | --- |
| Endpoints | Supported Spring MVC controller and mapping annotations are detected without invented HTTP methods or paths. |
| Components | Direct class-level Spring stereotypes are detected and sorted deterministically. |
| Entities | Direct `@Entity`, `@Table`, `@Id`, and supported relationship annotations are detected without claiming unresolved ORM behavior. |
| Tests | Standard Maven `src/test/java` test classes, supported framework signals, and naming-convention tested subjects are represented with explicit uncertainty where needed. |
| Evidence quality | Evidence IDs resolve, paths are repository-relative, line ranges and excerpts support the facts, and inferred or uncertain relations are labeled. |
| `agent-guide.md` | The guide is useful for first-pass orientation, cites deterministic facts, exposes known uncertainty, and avoids unsupported architecture claims. |

Recommended scorecard row:

| Project/ref | Endpoints | Components | Entities | Tests | Evidence quality | `agent-guide.md` | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `<project>@<ref>` | `TBD` | `TBD` | `TBD` | `TBD` | `TBD` | `TBD` | `<summary>` |

## Observation Format

Use one observation per concrete issue or useful confirmation:

```md
### OBS-8-XXX: <short title>

- Project/ref:
- Observed artifact:
- Expected:
- Actual:
- False positive:
- False negative:
- Evidence quality:
- Output contract issue:
- Notes:
```

Field rules:

- `Expected`: what the current v0.1 contract and docs imply should appear.
- `Actual`: what appears in the generated local artifact.
- `False positive`: unsupported fact emitted by the analyzer, or `None`.
- `False negative`: supported-scope fact missing from output, or `None`.
- `Evidence quality`: whether evidence resolves to the right file, symbol, line range,
  excerpt, and confidence.
- `Output contract issue`: field-shape, naming, sorting, nullability, or semantics issue
  against `docs/architecture/OUTPUT_CONTRACT.md`, or `None`.

## Follow-up Task Format

Convert confirmed evaluation findings into bounded tasks only after the observation is
recorded:

```md
### EVAL-8-XXX: <bounded title>

- Bounded task id:
- Project/ref:
- Observed artifact:
- Suspected cause:
- Affected contract/doc:
- Proposed validation:
- Non-goals:
```

Field rules:

- `Bounded task id`: stable local identifier, for example `EVAL-8-001`.
- `Project/ref`: selected project and exact ref where the issue was observed.
- `Observed artifact`: generated file and, when possible, JSON path or Markdown section.
- `Suspected cause`: narrow analyzer, generator, evidence, or contract hypothesis.
- `Affected contract/doc`: canonical doc that may need an update, or `None` if behavior
  should change under the existing contract.
- `Proposed validation`: focused fixture, golden output, or repeat scan needed to prove
  the fix.
- `Non-goals`: adjacent cleanup or future features that must stay out of the task.

## Network And Leakage Controls

- Do not use external analysis services.
- Do not make LLM calls for source analysis, fact extraction, or evidence generation.
- Do not upload third-party source to any service.
- Do not commit third-party source.
- Do not commit generated third-party `.project-memory/` outputs in this slice.
- Network access is limited to cloning public open-source repositories and verifying
  public refs or license metadata. The analyzer run itself must be local-only.
- Evaluation notes should summarize findings and point to local paths or repository refs;
  they should not embed large third-party source excerpts.

## Non-goals

Stage 8.1 does not include:

- Analyzer changes.
- Test changes.
- Script creation.
- Pilot scans.
- Output contract changes.
- Evidence model changes.
- Maven configuration changes.
- Generated output commits.
- YouTrack, Jira, Confluence, GitHub, or GitLab connectors.
- SaaS features.
- Web UI.
- Repository chat.
- Generic RAG.
- Automatic code modification.
- Multi-language analysis outside the documented Java/Spring focus.
