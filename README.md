# agent-project-memory

`agent-project-memory` is a local-first CLI/devtool for generating evidence-backed project memory for Java/Spring codebases.

The goal is to help developers and AI coding agents understand a legacy Java/Spring project before changing it. The tool scans local Java source, standard Maven layout, and standard Maven test roots, extracts deterministic facts, attaches evidence references, and writes Markdown/JSON artifacts that can be reviewed, versioned, and reused.

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

`mvn package` produces an executable shaded jar with dependencies and a CLI manifest at:

```text
target/agent-project-memory-0.1.0-SNAPSHOT.jar
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
implementation analyzes Spring MVC controllers and source-visible interface-declared
Spring MVC mappings that can be uniquely bound to concrete handlers, direct Spring
stereotype components, direct JPA entity annotations with direct source-visible
mapped-superclass identifier fields, and standard Maven test-root classes with
conservative helper filtering, then writes:

```text
<path>/.project-memory/project-map.json
<path>/.project-memory/endpoints.md
<path>/.project-memory/evidence-index.jsonl
<path>/.project-memory/agent-guide.md
```

`project-map.json` is the minimal stable machine-readable project map for the currently
supported single-module scan. It includes detected root `pom.xml` build metadata when
present, standard Maven source roots, Spring MVC endpoint facts, direct component
inventory, direct JPA entity facts, a minimal tests inventory, and evidence ID references.
`endpoints.md` is a deterministic endpoint inventory. `evidence-index.jsonl` contains
source-backed evidence records referenced by generated facts. `agent-guide.md` is a
deterministic orientation guide generated only from the structured project-map facts and
evidence index.

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

The current implementation is the v0.1 release-candidate slice after Stage 8 evaluation.
Roadmap Stages 0 through 8 are closed for v0.1. Stage 9 is post-v0.1 future work and is
not started.

The v0.1 implementation includes a Java 21 Maven CLI, JavaParser-backed Spring MVC
endpoint extraction, source-visible interface mapping support when uniquely bindable,
stable `project-map.json` and `evidence-index.jsonl` outputs, deterministic direct
Spring component and JPA entity inventories, a minimal deterministic tests inventory,
deterministic `endpoints.md`, and deterministic `agent-guide.md` generation from the
structured facts and evidence index.

Current v0.1 limitations:

- Maven detection is limited to root `pom.xml`; full Maven module parsing is not implemented.
- Component inventory is limited to direct class-level `@Component`, `@Service`,
  `@Repository`, `@Controller`, `@RestController`, and `@Configuration` annotations under
  `src/main/java`.
- Component analysis does not model Spring component scanning semantics, bean lifecycle,
  bean names, scopes, conditional configuration, dependency injection, or autowiring graphs.
- Entity analysis is limited to direct class-level `@Entity`, direct class-level
  `@Table(name = "...")`, field-level `@Id` declared on the entity class or on an
  immediate source-visible `@MappedSuperclass`, and field-level `@ManyToOne`,
  `@OneToMany`, `@OneToOne`, and `@ManyToMany` annotations under `src/main/java`.
- Entity analysis does not implement getter/property-access mapping, embedded IDs,
  generated values, column or join-column details, repository analysis, schema
  generation, transactional semantics, symbol solving, multi-level inheritance, or
  ORM runtime behavior.
- Relationship facts preserve the declared field type only and explicitly mark target
  type resolution as uncertain.
- Tests inventory is limited to test-like Java classes under standard single-module
  Maven `src/test/java`; helper/support/configuration declarations without clear test
  naming or direct test-class marker annotations are omitted.
- Test framework signals are limited to directly visible imports and annotations for
  JUnit Jupiter, JUnit 4, and Spring Test signals. Import evidence is attached only to
  top-level emitted test classes; nested emitted test classes use their own class or
  method annotation evidence.
- Tested-subject relations are inferred only from test class naming conventions against
  production classes under `src/main/java`; ambiguous simple-name matches are marked with
  low confidence and explicit uncertainty.
- Tests inventory does not claim code coverage, test execution results, behavioral
  assertion analysis, call graph resolution, symbol solving, or complete subject mapping.
- `agent-guide.md` is generated from existing deterministic output facts only. It does not
  ingest local documentation, summarize source files, infer architecture layers, or add
  claims beyond extracted facts, explicit inferences, and known uncertainty labels.
- Local Markdown/document ingestion is not implemented in v0.1.
- `evidence-index.jsonl` currently contains root `pom.xml` `build_file` evidence when present
  plus Spring MVC endpoint, component stereotype, JPA annotation, and tests inventory evidence.
- The CLI uses only Java standard library argument handling.
