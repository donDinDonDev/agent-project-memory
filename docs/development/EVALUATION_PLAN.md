# Evaluation Policy

This document defines the public evaluation policy for `agent-project-memory`.
Evaluations check whether released or release-candidate analyzer behavior is accurate,
useful, deterministic, evidence-backed, and aligned with the documented product scope.

Detailed maintainer execution notes, local command transcripts, local paths, and raw
review logs are not public product documentation. Public evaluation summaries should
record the project identity, validation scope, outcomes, limitations, and follow-ups at a
level that can be reviewed without exposing maintainer workflow mechanics.

## Scope

Public evaluations are intended for meaningful analyzer or CLI expansions, especially
when a change affects generated facts, output files, evidence semantics, CLI behavior, or
release confidence.

Evaluations should stay within the documented product boundary:

- Java/Spring-first projects.
- Maven-first project layouts.
- Local-first scanning of checked-out source trees.
- Deterministic analyzer outputs:
  - `.project-memory/project-map.json`
  - `.project-memory/evidence-index.jsonl`
  - `.project-memory/endpoints.md`
  - `.project-memory/agent-guide.md`

Evaluation work records observations. Analyzer, contract, script, release, and product
changes should be handled as separate bounded changes.

## Reproducibility Expectations

Public summaries should identify evaluated targets clearly enough for an independent
reader to understand the validation basis:

- project name;
- public repository URL;
- pinned tag or commit when useful;
- scan target when it is not the repository root;
- analyzer or release version under evaluation;
- generated schema version when relevant.

Pinned refs should be rechecked when reproducibility matters for a later evaluation.
Third-party source and generated `.project-memory/` outputs should not be vendored into
this repository as part of public evaluation documentation.

## Validation Principles

Evaluations should check the behavior that the relevant product and architecture
documents promise, without expanding the product scope during the evaluation.

Useful validation dimensions include:

- Supported facts are emitted for source-visible inputs.
- Unsupported or runtime-only behavior is not invented.
- Inferred and uncertain relations remain labeled.
- Evidence IDs resolve to repository-relative references.
- Generated paths are normalized and do not escape the scanned repository.
- Module, warning, document, and source references are internally consistent.
- Generated Markdown is useful without overclaiming.
- Determinism is checked for representative outputs when deterministic behavior is part
  of the release claim.
- Sensitive source, document, config, or local-machine details are not leaked into
  generated outputs.

The exact validation method may vary by release track. Public summaries should describe
what was validated and what passed, not expose raw command sequencing or local
maintainer mechanics.

## Public Summary Boundary

Tracked public evaluation summaries live under `docs/development/evaluations/` and use
the `*_SUMMARY.md` suffix.

Public summaries should include:

- evaluation date;
- evaluated project/ref table;
- scope and non-scope;
- generated artifact names or schema versions when relevant;
- outcome tables with stable counts when useful;
- scorecard or validation result summary;
- important observations;
- limitations and follow-ups.

Public summaries must not include:

- local machine paths;
- raw command transcripts;
- local checkout layout;
- internal task, goal, checkpoint, or decision IDs;
- maintainer-only workflow notes;
- tool-specific internal report names or local report paths;
- repeated release-action disclaimers such as commit/tag/push/upload status.

## Baseline Evaluation Targets

The original v0.1 public baseline used these pinned targets:

| Project | Repository URL | Ref | Scan target | Purpose |
| --- | --- | --- | --- | --- |
| Spring PetClinic | `https://github.com/spring-projects/spring-petclinic` | `3c06fbfc1e42eb40802e0d0ca989bc9226755804` | repository root | Canonical Spring sample with MVC controllers, JPA entities, and tests. |
| Spring PetClinic REST | `https://github.com/spring-petclinic/spring-petclinic-rest` | tag `v4.0.2`, commit `d8026bb5bcc58145b95a66a7f8e7694f0fae142f` | repository root | REST/OpenAPI-heavy Spring project used to check source-visible facts and known generated/API boundaries. |
| Spring Guide: REST Service | `https://github.com/spring-guides/gs-rest-service` | `e9efc9dfa0abe8cf8e15cf0e71830b5125322cae` | `complete/` | Small Maven Spring MVC sample for concise manual validation. |

Later release-track summaries may use different pinned targets that better exercise the
feature under validation.

## Scorecard Guidance

Public summaries may use a compact scorecard when that helps compare evaluated targets.

Recommended score meanings:

- `2`: accurate and useful for the documented release scope, with resolving evidence.
- `1`: usable, but with bounded misses, noise, or follow-up candidates.
- `0`: absent, misleading, contract-breaking, or not useful for the supported scope.
- `N/A`: the selected project did not exercise that signal.

Scorecards should be backed by observations and limitations. They should not imply
coverage, runtime correctness, security posture, or production readiness beyond the
specific evaluation scope.

## Non-Goals

Public evaluations do not include:

- analyzer changes;
- test, fixture, or golden-output changes;
- output contract or evidence model changes;
- Maven/build changes;
- generated output commits from third-party repositories;
- connector, SaaS, web UI, repository chat, generic RAG, or automatic code modification
  work;
- multi-language analysis outside the documented Java/Spring focus.
