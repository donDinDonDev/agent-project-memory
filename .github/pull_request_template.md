## Summary

-

## Change Type

- [ ] Docs-only
- [ ] Examples or templates
- [ ] Fixtures, goldens, or tests
- [ ] Analyzer or CLI behavior
- [ ] Output or evidence contract
- [ ] Release prep

## Validation

-

## Output And Evidence Impact

- Affected generated files or query output:
- Relevant evidence IDs, fixtures, or goldens:
- [ ] No generated output fields, evidence shape, or evidence semantics changed.
- [ ] Output contract changes, if any, are reflected in
      `docs/architecture/OUTPUT_CONTRACT.md`.
- [ ] Evidence model changes, if any, are reflected in
      `docs/architecture/EVIDENCE_MODEL.md`.

## Checklist

- [ ] Tests were run, or skipped with a clear reason.
- [ ] `mvn test` passes when relevant.
- [ ] `mvn package` passes when relevant.
- [ ] Example snapshots were regenerated or compared when examples changed.
- [ ] Documentation is synced for behavior, analyzer, CLI, architecture, or workflow
      changes.
- [ ] `CHANGELOG.md` is updated when the change is user-visible or changes contributor
      workflow.
- [ ] Generated facts remain deterministic, evidence-backed, and explicit about inferred
      or uncertain relations.
- [ ] Public docs, examples, and templates do not include secrets, private repository
      data, local machine paths, or vulnerability details.
- [ ] The change stays within the documented project scope and does not start future
      connector, AI, stable query JSON, package distribution, release automation, or
      other later-track work casually.
- [ ] No external service calls are added to the core analyzer or default CLI behavior.
- [ ] No connectors, LLM calls, SaaS, web UI, repository chat, generic RAG, automatic code
      modification, package-manager publication, or multi-language analysis are added
      casually.
