# Evaluation Plan

The goal of evaluation is to prove that `agent-project-memory` is not a toy and that it produces useful, evidence-backed project memory for Java/Spring codebases.

## Evaluation Strategy

### Fixture Spring Projects

Run the tool on small fixture Spring projects with known expected outputs.

Fixtures should cover:

- Simple Maven project layout.
- Multi-module Maven layout later.
- `@RestController` and `@Controller`.
- Class-level and method-level request mappings.
- Request path variables and request parameters.
- Request body DTOs.
- Response DTOs.
- Services and repositories.
- Controller tests.

Generated facts should be compared with expected JSON and Markdown outputs.

### Evidence Coverage

Evaluate whether endpoints and components have evidence references.

For each important generated fact, verify:

- An evidence ID exists.
- The evidence entry points to the right file.
- The line range is accurate when available.
- The excerpt supports the fact.
- Inferred or uncertain relations are labeled correctly.

### Agent Usefulness

Test whether Codex or another coding agent can answer project questions using only the generated `.project-memory/` files and the repository.

The agent should be able to cite evidence-backed files and avoid unsupported architecture claims.

### Agent-Guide Impact

Compare task execution with and without `agent-guide.md`.

Evaluation should check whether the generated guide:

- reduces time spent locating entry points,
- improves first-file selection,
- helps avoid unrelated edits,
- makes uncertainty visible,
- encourages evidence-based reasoning.

## Golden Questions

Use these questions against fixture and real Spring projects:

- Where is endpoint X declared?
- Which controller handles path Y?
- Which DTO is used by endpoint Z?
- Which tests cover controller A?
- Which components are likely involved in flow B?

## Real Project Evaluation

After fixture tests are stable, run the tool on real open-source Spring projects.

For each project, record:

- repository name and commit,
- scan command,
- generated file list,
- number of endpoints detected,
- number of components detected,
- percentage of important facts with evidence,
- known misses,
- false positives,
- output contract issues,
- examples where generated memory helped answer a golden question.

Real project evaluation should happen before adding future connectors or optional LLM summarization.

