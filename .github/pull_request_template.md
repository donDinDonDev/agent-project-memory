## Summary

-

## Validation

-

## Checklist

- [ ] Tests were run, or skipped with a clear reason.
- [ ] `mvn test` passes when relevant.
- [ ] `mvn package` passes when relevant.
- [ ] Documentation is synced for behavior, analyzer, CLI, architecture, or workflow
      changes.
- [ ] Output contract changes are reflected in `docs/architecture/OUTPUT_CONTRACT.md`.
- [ ] Evidence model changes are reflected in `docs/architecture/EVIDENCE_MODEL.md`.
- [ ] Generated facts remain deterministic, evidence-backed, and explicit about inferred
      or uncertain relations.
- [ ] The change stays within the documented release-track scope and does not start
      future connector, AI, OpenAPI, local-doc ingestion, or other later-track work
      casually.
- [ ] No external service calls are added to the core analyzer or default CLI behavior.
- [ ] No connectors, LLM calls, SaaS, web UI, repository chat, generic RAG, automatic code
      modification, or multi-language analysis are added casually.
