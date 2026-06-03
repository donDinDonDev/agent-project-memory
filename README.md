# agent-project-memory

`agent-project-memory` is a local-first CLI/devtool for generating evidence-backed
project memory for Java/Spring codebases.

The goal is to help developers and AI coding agents understand a legacy Java/Spring
project before changing it. The tool scans local Java source, standard Maven layout, and
standard Maven test roots, extracts deterministic facts, attaches evidence references,
and writes Markdown/JSON artifacts that can be reviewed, versioned, and reused.

The current product focus is intentionally narrow:

- Java/Spring codebases first.
- Local repository analysis first.
- Maven projects first.
- Deterministic source analysis as the source of truth.
- Optional AI assistance later, outside the core analyzer.

The first version is a local-first CLI. Source code must not be sent to external
services by default.

## Requirements

- Java 21.
- Apache Maven 3.x.

## Download

Release artifacts are expected on the
[GitHub Releases page](https://github.com/donDinDonDev/agent-project-memory/releases)
after the `v0.1.0` tag and release are created.

Download `agent-project-memory-0.1.0.jar`. If `SHA256SUMS` is published with the
release, you can optionally verify the jar checksum before running it.

```sh
java -jar agent-project-memory-0.1.0.jar scan /path/to/java-spring-project
```

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
target/agent-project-memory-0.1.0.jar
```

## Quick Start

After `mvn package`, run a scan with the packaged CLI jar:

```sh
java -jar target/agent-project-memory-0.1.0.jar scan /path/to/java-spring-project
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
stereotype components on classes and interfaces, deterministic hidden HTTP surface
warnings, direct JPA entity annotations with conservative source-visible
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
present, standard Maven source roots, Spring MVC endpoint facts, hidden HTTP surface
warnings that are not expanded into endpoint facts, direct component inventory, direct
JPA entity facts, a minimal tests inventory, and evidence ID references.
`endpoints.md` is a deterministic endpoint inventory. `evidence-index.jsonl` contains
source-backed evidence records referenced by generated facts. `agent-guide.md` is a
deterministic orientation guide generated only from the structured project-map facts and
evidence index.

## Future Installed Usage

Future installed command:

```sh
agent-project-memory scan .
```

The same output files:

```text
.project-memory/project-map.json
.project-memory/evidence-index.jsonl
.project-memory/endpoints.md
.project-memory/agent-guide.md
```

These files are meant to give humans and coding agents a compact, evidence-backed map of the project: detected build layout, Spring MVC endpoints, important components, and references back to the source files that prove each fact.

## Public Documentation Map

Start here:

- v0.1 release summary: [docs/product/V0_1_RELEASE_NOTES.md](docs/product/V0_1_RELEASE_NOTES.md).
- Product scope and boundaries: [docs/product/MVP_SPEC.md](docs/product/MVP_SPEC.md) and
  [docs/product/NON_GOALS.md](docs/product/NON_GOALS.md).
- Post-v0.1 direction:
  [docs/product/POST_V0_1_STRATEGY.md](docs/product/POST_V0_1_STRATEGY.md) and
  the public v0.2 roadmap and release notes.
- Output and evidence contracts:
  [docs/architecture/OUTPUT_CONTRACT.md](docs/architecture/OUTPUT_CONTRACT.md) and
  [docs/architecture/EVIDENCE_MODEL.md](docs/architecture/EVIDENCE_MODEL.md).
- Architecture overview:
  [docs/architecture/ARCHITECTURE_OVERVIEW.md](docs/architecture/ARCHITECTURE_OVERVIEW.md) and
  [docs/architecture/INGESTION_ARCHITECTURE.md](docs/architecture/INGESTION_ARCHITECTURE.md).
- Roadmap: [docs/product/ROADMAP.md](docs/product/ROADMAP.md).
- Changelog: [CHANGELOG.md](CHANGELOG.md).
- Contributing, release process, and security:
  [CONTRIBUTING.md](CONTRIBUTING.md),
  [docs/development/RELEASE_PROCESS.md](docs/development/RELEASE_PROCESS.md), and
  [SECURITY.md](SECURITY.md).

Stage 8 evaluation reports are linked from the v0.1 release notes as supporting detail.

## What This Is Not

`agent-project-memory` is not:

- a generic AI documentation generator,
- a repository chatbot,
- a RAG system,
- a SaaS product,
- a hosted codebase wiki,
- a tool that treats LLM output as the source of truth,
- an automatic code modification system.

AI may become an optional presentation or summarization layer later, but the core project
memory must come from deterministic analysis, explicit output contracts, and evidence
references.

## Project Status

The current implementation is the v0.1 public release slice after Stage 8 evaluation.
Roadmap Stages 0 through 8 are closed for v0.1. Future connector/import work is
post-v0.1 and is not started.

The v0.1 implementation includes a Java 21 Maven CLI, JavaParser-backed Spring MVC
endpoint extraction, source-visible interface mapping support when uniquely bindable,
stable `project-map.json` and `evidence-index.jsonl` outputs, deterministic direct
Spring component and JPA entity inventories, deterministic hidden HTTP surface warnings,
a minimal deterministic tests inventory, deterministic `endpoints.md`, and deterministic
`agent-guide.md` generation from the structured facts and evidence index.

Current v0.1 limitations:

- Maven detection is limited to root `pom.xml`; full Maven module parsing is not implemented.
- Component inventory is limited to direct source-type-level `@Component`, `@Service`,
  `@Repository`, `@Controller`, `@RestController`, and `@Configuration` annotations on
  Java classes or interfaces under `src/main/java`. It does not infer repositories from
  `extends JpaRepository` without a direct supported stereotype.
- Component analysis does not model Spring component scanning semantics, bean lifecycle,
  bean names, scopes, conditional configuration, dependency injection, or autowiring graphs.
- Entity analysis is limited to direct class-level `@Entity`, direct class-level
  `@Table(name = "...")`, field-level `@Id` declared on the entity class or on a
  conservative source-visible `@MappedSuperclass` chain, and field-level `@ManyToOne`,
  `@OneToMany`, `@OneToOne`, and `@ManyToMany` annotations under `src/main/java`.
- Entity analysis does not implement getter/property-access mapping, embedded IDs,
  generated values, column or join-column details, repository analysis, schema
  generation, transactional semantics, symbol solving, or ORM runtime behavior.
- Hidden HTTP surface warnings are limited to OpenAPI/Swagger spec filename presence,
  root `pom.xml` OpenAPI/Swagger Maven plugin declarations under `<build><plugins>` or
  `<build><pluginManagement><plugins>`, and direct `@RepositoryRestResource`. They do
  not create endpoint facts, parse OpenAPI YAML, run
  Maven generation, scan `target/generated-sources` by default, or reconstruct generated
  APIs.
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
- `evidence-index.jsonl` currently contains root `pom.xml` `build_file` evidence when
  present plus Spring MVC endpoint, warning, component stereotype, JPA annotation, and
  tests inventory evidence.
- The CLI uses only Java standard library argument handling.

For the concise v0.1 scope, evaluation summary, limitations, and validation surface, see
[docs/product/V0_1_RELEASE_NOTES.md](docs/product/V0_1_RELEASE_NOTES.md).

## License

Apache-2.0. See [LICENSE](LICENSE). Runtime dependency notices are summarized in
[THIRD-PARTY-NOTICES.md](THIRD-PARTY-NOTICES.md).
