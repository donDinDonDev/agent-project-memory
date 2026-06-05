# Architecture Overview

`agent-project-memory` is organized around a deterministic local analysis pipeline.

The core analyzer must not depend on external APIs. It should be able to scan a local Java/Spring repository and produce project memory artifacts without network access and without sending source code anywhere.

## Conceptual Components

### CLI

Accepts user commands and local repository paths. The intended first command is:

```sh
agent-project-memory scan .
```

The CLI should coordinate scanning and artifact generation. It should not contain analyzer logic directly.

### Repository Scanner

Walks the local repository, applies ignore rules, identifies candidate project files, and provides normalized file references to analyzers.

### Build Detector

Detects Maven project structure and build metadata needed by analyzers. The current
implementation detects a root `pom.xml` when present, root-declared Maven child modules
from the root `<modules>` section, child `pom.xml` files for supported modules, and
standard Maven source and test roots such as `src/main/java` and `src/test/java`. It
also extracts direct source-visible Maven metadata from module POMs for `groupId`,
`artifactId`, `version`, `packaging`, and parent coordinates.

The current implementation does not resolve Maven profiles, recursively discover nested
modules, reconstruct effective POMs, fill missing metadata from Maven defaults or parent
inheritance, resolve dependencies, run Maven, scan generated source roots by default, or
discover Gradle projects.

### Build And Configuration Analyzer

The v0.3 build and configuration analyzer is being implemented in bounded slices. The
current slice emits source-visible module-owned Maven metadata and a complete
`build_config` shell. Future subsections that are not implemented yet use
`analysis_status: "not_analyzed"` and do not claim empty inventories. The full planned
scope is direct local POM, resource, config-file, and source annotation observations:

- direct Maven metadata, dependency declarations, dependency-management declarations,
  plugin declarations, plugin-management declarations, and bounded generator signals;
- standard resource roots and supported Spring application or logging config filenames;
- direct `@SpringBootApplication` application class and source-visible `main` method
  signals;
- generated-source and generator-plugin warnings that remain separate from endpoint,
  generated API, and generated source facts.

This planned analyzer must not execute Maven, reconstruct effective POMs, activate
profiles, resolve remote dependencies, interpret config values, extract secrets, scan
generated sources by default, parse OpenAPI specs, or turn build/config warnings into
application facts.

### Java/Spring Analyzer

Uses JavaParser first to inspect Java source files. The current implementation extracts
Spring MVC endpoint facts, deterministic hidden HTTP surface warnings, direct Spring
stereotype component facts, and direct JPA entity facts from supported production source
roots.

For `EVAL-8-004` decision B, the v0.1 endpoint contract includes Spring MVC mappings
declared on Java interface methods only when those interfaces are visible under
supported production source roots such as `src/main/java` and can be uniquely bound to
concrete controller handler methods. This is still source-visible analysis: the analyzer
must not run Maven generation, scan `target/generated-sources` by default, parse OpenAPI
YAML, reconstruct generated APIs, or claim complete Spring runtime handler mapping
behavior. Ambiguous or non-unique interface bindings are skipped instead of emitted as
uncertain endpoints.

The hidden HTTP surface warning analyzer records bounded signals such as
OpenAPI/Swagger spec filename presence, root `pom.xml` OpenAPI/Swagger Maven plugin
declarations under `<build><plugins>` or `<build><pluginManagement><plugins>`, and
direct source-visible `@RepositoryRestResource`. These warnings do not become endpoint
facts, and they do not parse OpenAPI YAML, run Maven generation, scan generated sources
by default, or reconstruct generated APIs.

The basic component analyzer records only directly present source-type-level stereotypes
on Java classes or interfaces, such as `@Component`, `@Service`, `@Repository`,
`@Controller`, `@RestController`, and `@Configuration`. It does not infer repository
components from `extends JpaRepository`, or reconstruct Spring component scanning, bean
lifecycle, bean names, scopes, autowiring, conditional configuration, or other runtime
behavior.

The basic JPA entity analyzer records only directly present class-level `@Entity`,
class-level `@Table(name = "...")`, field-level `@Id`, and field-level relationship
annotations `@ManyToOne`, `@OneToMany`, `@OneToOne`, and `@ManyToMany`. Relationship
facts preserve the declared field type and explicitly mark target resolution as
uncertain because no Java symbol solving or ORM runtime reconstruction is performed.
It also attaches field-level `@Id` facts declared on a conservative source-visible
superclass chain where each traversed superclass is annotated with direct
`@MappedSuperclass`. This traversal resolves only fully qualified names, explicit
single-type imports, and same-package references; unresolved, ambiguous, cyclic, or
non-source-visible hierarchy branches are skipped. It does not solve classpaths or claim
ORM runtime behavior.

The tests inventory analyzer records test-like Java class declarations under standard
Maven `src/test/java` roots, directly visible test framework signals from imports and
annotations, and likely tested-subject relations inferred only from class naming
conventions against production classes under `src/main/java`. Helper, support, or
configuration declarations without clear test naming and without direct test-class
marker annotations are omitted. Import evidence is attached only to top-level emitted
test classes; nested emitted test classes use their own class or method annotation
evidence. Naming-convention relations are explicitly marked as inferred. Duplicate
production class simple-name matches are emitted with low confidence and explicit
uncertainty. The analyzer does not perform coverage analysis, test execution analysis,
behavioral assertion analysis, call graph construction, symbol solving, Gradle/Kotlin
test-root discovery, or complete subject mapping.

Future deeper analyzers may be added, but they must preserve deterministic evidence-backed behavior.

### Project Graph Builder

Builds a structured project map from extracted facts. The graph should describe known project elements and relationships, while distinguishing direct facts from inferred relations.

### Evidence Index Builder

Creates stable evidence records for important facts. The current v0.1 implementation
emits evidence for build files, code symbols, annotations, and test files. Future
ingestors may add document evidence.

### Memory Generator

Writes machine-readable memory artifacts such as `project-map.json` and `evidence-index.jsonl`.

### Agent Files Generator

Writes human-readable and agent-oriented Markdown artifacts such as `endpoints.md` and `agent-guide.md`.

`endpoints.md` is generated directly from deterministic endpoint facts. `agent-guide.md`
is generated from `project-map.json` and `evidence-index.jsonl`, or from the same
structured in-memory facts that are serialized to those files. The guide generator does
not re-analyze source files, call LLMs, call external services, or ingest local
documentation.

These files must not invent architecture beyond the extracted facts, documented
inferences, and explicitly labeled uncertainty.

### Future Docs/Issues Ingestors

Future ingestors may import local Markdown docs or external materials from systems such as YouTrack, Jira, Confluence, GitHub, and GitLab.

Connectors are input adapters. They should normalize external records into source documents and should not become part of the core Java/Spring analyzer.

### Optional LLM Layer

An optional LLM layer may be considered later for presentation, grouping, or summarization. It must not be required for core analysis, and it must not be treated as the source of truth.

Any LLM-generated output must be derived from deterministic facts and evidence references.

## Pipeline

The intended pipeline is:

```text
local repository
  -> repository scanner
  -> build detector
  -> Java/Spring analyzers
  -> project graph builder
  -> evidence index builder
  -> memory and agent file generators
  -> .project-memory/
```

## Architectural Constraints

- The core analyzer runs locally.
- The core analyzer does not call external APIs.
- Source code is not sent anywhere by default.
- Facts must be traceable to evidence.
- Output contracts must be explicit and documented.
- Optional future integrations must be adapters around the core, not dependencies inside it.
