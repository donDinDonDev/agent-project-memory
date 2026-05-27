# agent-project-memory

`agent-project-memory` is a local-first CLI/devtool for generating evidence-backed project memory for Java/Spring codebases.

The goal is to help developers and AI coding agents understand a legacy Java/Spring project before changing it. The tool will scan local source code and project materials, extract deterministic facts, attach evidence references, and write Markdown/JSON artifacts that can be reviewed, versioned, and reused.

The current product focus is intentionally narrow:

- Java/Spring codebases first.
- Local repository analysis first.
- Maven projects first.
- Deterministic source analysis as the source of truth.
- Optional AI assistance later, outside the core analyzer.

The first version is intended to be a local-first CLI. Source code must not be sent to external services by default.

## Requirements

- Java 21.
- Apache Maven 3.x.

## Build And Test

Run the test suite:

```sh
mvn test
```

Build the packaged CLI jar:

```sh
mvn package
```

## Stage 2 Usage

The current CLI exposes the intended command shape:

```sh
java -jar target/agent-project-memory-0.1.0-SNAPSHOT.jar scan /path/to/java-spring-project
```

`scan <path>` validates that the path exists and is a directory, then creates or reuses:

```text
<path>/.project-memory/
```

Existing unrelated contents inside `.project-memory/` are preserved. Generated Stage 2
files are rewritten deterministically when a supported source root exists.

When the scanned path has a Maven-style Java source root at `src/main/java`, the Stage 2
implementation also analyzes Spring MVC controllers and writes:

```text
<path>/.project-memory/endpoints.md
<path>/.project-memory/evidence-index.jsonl
```

`endpoints.md` is a deterministic endpoint inventory. `evidence-index.jsonl` contains
source-backed annotation evidence referenced by the endpoint inventory.

## Intended Usage

Future installed command:

```sh
agent-project-memory scan .
```

Intended output:

```text
.project-memory/project-map.json
.project-memory/evidence-index.jsonl
.project-memory/endpoints.md
.project-memory/agent-guide.md
```

These files are meant to give humans and coding agents a compact, evidence-backed map of the project: detected build layout, Spring MVC endpoints, important components, and references back to the source files that prove each fact.

## What This Is Not

`agent-project-memory` is not:

- a generic AI documentation generator,
- a repository chatbot,
- a RAG system,
- a SaaS product,
- a DeepWiki clone,
- a tool that treats LLM output as the source of truth,
- an automatic code modification system.

AI may become an optional presentation or summarization layer later, but the core project memory must come from deterministic analysis, explicit output contracts, and evidence references.

## Project Status

Stage 1 is implemented as a minimal Java 21 Maven CLI skeleton.
Stage 2 implements a JavaParser-backed Spring MVC endpoint analyzer and wires it into
`scan <path>` for Maven-style `src/main/java` source roots.

Current Stage 2 limitations:

- No Maven project detection is implemented yet.
- `project-map.json` and `agent-guide.md` are not created yet.
- `evidence-index.jsonl` currently contains Spring MVC endpoint annotation evidence only.
- The CLI uses only Java standard library argument handling.
