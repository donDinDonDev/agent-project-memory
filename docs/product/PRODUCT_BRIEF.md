# Product Brief

## Problem

Java/Spring backends often survive for years longer than the original team context. A developer joining the project has to reconstruct how the system works by reading controllers, services, configuration, tests, build files, and scattered notes. AI coding agents face the same problem, but with an added risk: if the project context is not explicit and evidence-backed, the agent may infer architecture that is not actually present in the code.

Current options do not solve this cleanly. Generic documentation generators tend to produce broad summaries without verifiable references. Repo chat systems can answer questions, but they often hide how the answer was derived. RAG setups can retrieve snippets, but retrieval alone is not a stable project map. Manual onboarding docs are useful, but they decay.

`agent-project-memory` exists to produce a local, deterministic memory layer that developers and agents can inspect before making changes.

## Target Users

- Individual Java/Spring backend developers maintaining or exploring unfamiliar codebases.
- AI-agent power users who want better context before asking an agent to modify code.
- Tech leads onboarding developers into legacy Spring projects.

## Positioning

`agent-project-memory` is a deterministic project-memory compiler for Java/Spring codebases.

It scans a local repository, extracts verifiable facts, builds a project map and evidence index, and writes stable Markdown/JSON artifacts. Those artifacts are designed to be useful both to humans and to AI coding agents.

The product should feel closer to a compiler or static analyzer than to a chatbot. It should prefer a small set of correct, evidence-backed facts over broad generated prose.

## Why This Is Not AI Docs, RAG, Or Repo Chat

AI documentation tools usually optimize for readable summaries. `agent-project-memory` optimizes for evidence-backed project memory.

RAG systems retrieve relevant chunks at question time. `agent-project-memory` produces explicit artifacts ahead of time: a project map, evidence index, endpoint summary, and agent guide.

Repo chat lets users ask questions interactively. `agent-project-memory` does not require a chat interface and does not make an LLM the source of truth.

LLMs may later help with wording, grouping, or optional summaries, but v0.1 core facts
come from deterministic analysis of local build/source/test inputs and explicit output
contracts. Local project document ingestion is future-only.

## North Star

Generate evidence-based project memory for Java/Spring codebases so AI coding agents can understand a project before changing it.
