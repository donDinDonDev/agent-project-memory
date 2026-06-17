# Contributing

Thanks for helping improve `agent-project-memory`.

This project is intentionally narrow: a local-first Java/Spring CLI/devtool that
generates deterministic, evidence-backed project memory. Good contributions keep that
scope clear and make the current workflow easier to understand, verify, or maintain.

## Development Requirements

- Java 21.
- Apache Maven 3.x.

Run the test suite:

```sh
mvn test
```

Build the packaged CLI jar:

```sh
mvn package
```

## Choosing A Contribution Path

Start with the smallest path that fits the change:

| Path | Good fit | Expected checks |
| --- | --- | --- |
| Docs-only changes | README wording, public examples text, release notes, roadmap clarifications, typo fixes that do not change product meaning. | `git diff --check`; public-surface review when public docs change. |
| Examples and template changes | Checked-in example snapshots, example READMEs, issue templates, or PR template wording. | `git diff --check`; example regeneration or comparison when generated examples change. |
| Fixture, golden, and test changes | Test fixtures, expected generated outputs, or focused regression tests for existing behavior. | Focused Maven tests for the touched area; `mvn test` when output contracts or broad behavior are affected. |
| Analyzer behavior changes | Deterministic Java/Spring analysis, CLI behavior, generated output rendering, query behavior, or diagnostics. | Focused tests plus broader Maven validation appropriate to the affected surface. |
| Output or evidence contract changes | Any generated output field addition, removal, rename, or semantic change; any evidence shape or semantic change. | Contract docs, changelog, tests/goldens, and release-note planning must be updated together. |
| Release-prep changes | Version alignment, release notes, changelog entries, README release status, artifact/checksum preparation notes. | Follow [docs/development/RELEASE_PROCESS.md](docs/development/RELEASE_PROCESS.md). |

Do not use a larger path when a smaller one is enough. A docs or template contribution
should not introduce analyzer behavior, dependencies, CI changes, release-process
changes, package-distribution changes, or new generated-output semantics.

## Good First Issue Guidance

Good first contributions are small, local, and easy to verify without changing product
behavior. Suitable starting points include:

- Clarifying README or example wording while keeping current jar-based installation
  guidance accurate.
- Improving issue or PR template prompts so reports include commands, versions,
  affected generated files, and evidence IDs.
- Adding or tightening fixture-focused tests for already documented behavior.
- Correcting public docs that drift from the output contract, evidence model, or
  release process.

Avoid starting with broad analyzer expansion, new connectors, package-manager
publication, web UI, repository chat, generic RAG, automatic code modification,
security-policy changes, release automation, or output/evidence schema changes. Those
areas need explicit design and maintainer agreement before implementation.

When opening an issue, describe the smallest contribution path that seems to fit. The
project does not require a remote label, milestone, or project board to make a report
actionable.

## Pull Request Expectations

- Keep the PR scope focused and reviewable.
- Keep the change within the documented release-track scope.
- Update `CHANGELOG.md` for user-visible behavior, output contract, evidence semantics,
  release packaging, or meaningful public workflow changes.
- Sync durable documentation when analyzer behavior, output files, evidence semantics,
  CLI workflow, architecture, or development workflow changes.
- Update `docs/architecture/OUTPUT_CONTRACT.md` for output field additions, removals,
  renames, or semantic changes.
- Update `docs/architecture/EVIDENCE_MODEL.md` for evidence shape or evidence semantics
  changes.
- Avoid casual additions of connectors, LLM calls, SaaS features, web UI, repository
  chat, generic RAG, automatic code modification, or multi-language analysis.
- Keep future adapters and optional AI layers separate from the deterministic core
  analyzer.

## Release Expectations

Release preparation is governed by
[docs/development/RELEASE_PROCESS.md](docs/development/RELEASE_PROCESS.md).

The human maintainer owns release authority. Coding agents may prepare release materials
and run checks, but must not create commits, tags, GitHub releases, or publish artifacts
unless explicitly asked by the maintainer.

## Evidence-First Analyzer Discipline

- Use deterministic inputs and explicit output contracts.
- Back important generated facts with evidence IDs.
- Treat inferred relations as inferred and preserve the evidence that led to them.
- Record uncertainty instead of presenting uncertain signals as facts.
- Do not treat LLM-generated text as evidence.
- Do not send source code to external services by default.
