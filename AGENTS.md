# Agent Instructions

These rules apply to Codex and other AI coding agents working in this repository.

## Required Reading

Before any implementation task, read these public files:

- `README.md`
- `docs/product/MVP_SPEC.md`
- `docs/product/ROADMAP.md`
- `docs/architecture/OUTPUT_CONTRACT.md`
- `docs/architecture/EVIDENCE_MODEL.md`

Use those documents as the source of product scope, stage sequencing, output shape, and
evidence requirements.

If `AGENTS.local.md` exists, read it after this file for local maintainer workflow
instructions. `AGENTS.local.md` is intentionally not tracked.

## Working Rules

- Keep every change small, reviewable, and tied to the requested task.
- Stop after the requested task is complete.
- Do not continue into adjacent implementation, cleanup, refactoring, or product design work unless explicitly asked.
- Prefer the repository's documented scope over assumptions from similar tools.
- Update the relevant docs when behavior, architecture, or output contracts change.
- Do not expand scope without updating `docs/product/MVP_SPEC.md` and the relevant architecture documents.

## Documentation Synchronization

- Before changing product behavior, analyzer behavior, output files, evidence semantics, CLI workflow, or development workflow, identify the canonical owner document and update all affected docs in the same logical change. Do not leave chat-only knowledge as the synchronization mechanism.

## User-Facing Language

- Communicate with the user in Russian by default, even when the user's prompt is in English.
- Keep code identifiers, file paths, commands, API fields, JSON keys, error messages, source excerpts, and official technical terms in their original language.
- Keep durable repository documentation in English by default for open-source readability, unless the user explicitly asks otherwise or the edited document already establishes another language.

## Analyzer Rules

- Every analyzer must have focused tests.
- Each analyzer should use deterministic inputs and explicit output contracts.
- Important generated facts must be evidence-backed.
- Evidence must point to source locations or documents using file paths, symbols, annotations, line ranges, excerpts, or other explicit references defined in `docs/architecture/EVIDENCE_MODEL.md`.
- Do not treat LLM-generated text as evidence.
- If a relation is inferred rather than directly extracted, mark it as inferred and preserve the evidence that led to it.
- If a signal is uncertain, record uncertainty instead of presenting it as a fact.

## MVP Boundaries

Do not implement these in the v0.1 MVP:

- YouTrack, Jira, Confluence, GitHub, or GitLab connectors.
- LLM calls in the core analyzer.
- SaaS features.
- Web UI.
- Repository chat.
- Generic RAG.
- Automatic code modification.
- Multi-language analysis outside the documented Java/Spring focus.

Future adapters and optional AI layers must remain separate from the deterministic core analyzer.

## Output Contract Discipline

- Generated files under `.project-memory/` must match `docs/architecture/OUTPUT_CONTRACT.md`.
- Any output field addition, removal, rename, or semantic change requires updating `docs/architecture/OUTPUT_CONTRACT.md`.
- Evidence shape changes require updating `docs/architecture/EVIDENCE_MODEL.md`.
- Documentation examples should stay aligned with tests once implementation begins.
