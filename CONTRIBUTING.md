# Contributing

Thanks for helping improve `agent-project-memory`.

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

## Pull Request Expectations

- Keep the PR scope focused and reviewable.
- Do not drift beyond the documented v0.1 scope.
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

## Evidence-First Analyzer Discipline

- Use deterministic inputs and explicit output contracts.
- Back important generated facts with evidence IDs.
- Treat inferred relations as inferred and preserve the evidence that led to them.
- Record uncertainty instead of presenting uncertain signals as facts.
- Do not treat LLM-generated text as evidence.
- Do not send source code to external services by default.
