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

## Current Usage

The current CLI exposes the intended command shape:

```sh
java -jar target/agent-project-memory-0.1.0-SNAPSHOT.jar scan /path/to/java-spring-project
```

`scan <path>` validates that the path exists and is a directory, then creates or reuses:

```text
<path>/.project-memory/
```

Existing unrelated contents inside `.project-memory/` are preserved. Generated files are
rewritten deterministically when a supported source root exists.

When the scanned path has a Maven-style Java source root at `src/main/java`, the current
implementation analyzes Spring MVC controllers and direct Spring stereotype components,
then writes:

```text
<path>/.project-memory/project-map.json
<path>/.project-memory/endpoints.md
<path>/.project-memory/evidence-index.jsonl
```

`project-map.json` is the minimal stable machine-readable project map for the currently
supported single-module scan. It includes detected root `pom.xml` build metadata when
present, standard Maven source roots, Spring MVC endpoint facts, direct component
inventory, and evidence ID references. `endpoints.md` is a deterministic endpoint
inventory. `evidence-index.jsonl` contains source-backed evidence records referenced by
generated facts.

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
Stage 3.1 stabilizes minimal `project-map.json` and `evidence-index.jsonl` output for
the currently supported single-module Maven/Spring MVC scan.
Stage 4.1 adds a deterministic direct Spring stereotype component inventory to
`project-map.json`.

Current Stage 4.1 limitations:

- Maven detection is limited to root `pom.xml`; full Maven module parsing is not implemented.
- Component inventory is limited to direct class-level `@Component`, `@Service`,
  `@Repository`, `@Controller`, `@RestController`, and `@Configuration` annotations under
  `src/main/java`.
- Component analysis does not model Spring component scanning semantics, bean lifecycle,
  bean names, scopes, conditional configuration, dependency injection, or autowiring graphs.
- `agent-guide.md` is not created yet.
- `evidence-index.jsonl` currently contains root `pom.xml` `build_file` evidence when present
  plus Spring MVC endpoint and component stereotype annotation evidence.
- The CLI uses only Java standard library argument handling.
