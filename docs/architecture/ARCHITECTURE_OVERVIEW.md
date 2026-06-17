# Architecture Overview

`agent-project-memory` is organized around a deterministic local analysis pipeline.

The core analyzer must not depend on external APIs. It should be able to scan a local Java/Spring repository and produce project memory artifacts without network access and without sending source code anywhere.

## Conceptual Components

### CLI

Accepts user commands and local repository paths. Published binary usage currently runs
the executable release jar:

```sh
java -jar agent-project-memory-X.Y.Z.jar scan .
```

A future installed `agent-project-memory scan .` command may wrap the same CLI after a
separate distribution-channel release documents it. The CLI should coordinate scanning
and artifact generation. It should not contain analyzer logic directly.

### Repository Scanner

Walks the local repository, applies ignore rules, identifies candidate project files, and provides normalized file references to analyzers.

### Build Detector

Detects supported local build structure and build metadata needed by analyzers. The
current implementation detects a root `pom.xml` when present, root-declared Maven child
modules from the root `<modules>` section, child `pom.xml` files for supported modules,
standard Maven source, test, and resource roots such as `src/main/java`,
`src/test/java`, and `src/main/resources`, accepted Gradle root build files, simple
static Gradle settings includes, supported Gradle project build files, and standard
Gradle Java/test/resource roots. It also extracts direct source-visible Maven metadata
from module POMs for `groupId`, `artifactId`, `version`, `packaging`, and parent
coordinates, plus direct source-visible dependency/dependency-management declarations
and plugin/plugin-management declarations.

The current implementation does not resolve Maven profiles, recursively discover nested
Maven modules, reconstruct effective POMs, fill missing metadata from Maven defaults or
parent inheritance, resolve dependencies or plugins, run Maven, execute Gradle, evaluate
Gradle build scripts, use the Gradle Tooling API, reconstruct effective Gradle models,
emit custom Gradle `sourceSets`, scan generated source roots by default, or analyze
Kotlin source.

### Build And Configuration Analyzer

The v0.3 build and configuration analyzer emits source-visible module-owned Maven
metadata, direct Maven dependency inventory, separate dependency-management
declarations, direct Maven plugin inventory, separate plugin-management declarations,
conservative plugin-derived generated-source warnings, standard resource-root
discovery, path-only supported application/logging config-file inventory, direct
source-visible Spring Boot application signals, and a complete `build_config` shell.
The implemented scope is direct local POM, resource, config-file, and source annotation
observations:

- direct Maven metadata, dependency declarations, dependency-management declarations,
  plugin declarations, plugin-management declarations, and bounded generator signals;
- bounded Gradle build-file and static include observations, standard Gradle roots, and
  `sourceSets` not-analyzed status without Gradle execution or effective model claims;
- standard resource roots and supported Spring application or logging config filenames;
- direct `@SpringBootApplication` application class and source-visible `main` method
  signals;
- generated-source and generator-plugin warnings that remain separate from endpoint,
  generated API, and generated source facts.

This analyzer must not execute Maven, reconstruct effective POMs, activate profiles,
resolve remote dependencies, interpret config values, extract secrets, scan generated
sources by default, parse OpenAPI specs, or turn build/config warnings into application
facts.

### Java/Spring Analyzer

Uses JavaParser first to inspect Java source files. The current analyzer family
extracts source-visible Spring MVC endpoint facts, deterministic hidden HTTP surface
warnings, direct Spring stereotype component facts, Spring application-surface signals,
direct JPA/domain facts, and bounded test inventory facts from supported Maven or Gradle
standard roots.

The v0.1 endpoint contract includes Spring MVC mappings declared on Java interface
methods only when those interfaces are visible under supported production source roots
such as `src/main/java` and can be uniquely bound to concrete controller handler
methods. This is still source-visible analysis: the analyzer must not run Maven
generation, scan `target/generated-sources` by default, parse OpenAPI YAML, reconstruct
generated APIs, or claim complete Spring runtime handler mapping behavior. Ambiguous or
non-unique interface bindings are skipped instead of emitted as uncertain endpoints.

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

The JPA entity analyzer records directly present class-level `@Entity`, class-level
`@Table(name = "...")`, field-level `@Id`, bounded direct field metadata, embedded and
identifier-model signals, and field-level relationship annotations `@ManyToOne`,
`@OneToMany`, `@OneToOne`, and `@ManyToMany`. Relationship facts preserve the declared
field type, direct source-visible metadata such as `mappedBy`, `@JoinColumn`,
`@JoinTable`, and selected relationship attributes, and explicitly mark target
resolution as uncertain because no Java symbol solving or ORM runtime reconstruction is
performed. It also attaches field-level `@Id` facts declared on a conservative
source-visible superclass chain where each traversed superclass is annotated with direct
`@MappedSuperclass`. This traversal resolves only fully qualified names, explicit
single-type imports, and same-package references; unresolved, ambiguous, cyclic, or
non-source-visible hierarchy branches are skipped. It does not solve classpaths or claim
ORM runtime behavior.

The tests inventory analyzer records test-like Java class declarations under supported
standard Maven or Gradle `src/test/java` roots, directly visible test framework signals
from imports and annotations, and conservative tested-subject relation/status rows
inferred or statused from supported naming, exact production imports, direct field
types, and direct Spring test slice class literals against production classes under
`src/main/java`. Helper,
support, or configuration declarations without clear test naming and without direct
test-class marker annotations are omitted. Import evidence is attached only to top-level
emitted test classes; nested emitted test classes use their own class or method
annotation evidence. Tested-subject rows are explicitly marked with relation status,
confidence, and uncertainty. Duplicate production class simple-name matches are emitted
with low confidence and explicit uncertainty. The analyzer does not perform coverage
analysis, test execution analysis, behavioral assertion analysis, call graph
construction, symbol solving, custom Gradle/Kotlin test-root discovery, or complete
subject mapping.

Future deeper analyzers may be added, but they must preserve deterministic evidence-backed behavior.

### Project Graph Builder

Builds a structured project map from extracted facts. The graph should describe known project elements and relationships, while distinguishing direct facts from inferred relations.

### Evidence Index Builder

Creates stable evidence records for important facts. The current implementation emits
evidence for build files, code symbols, annotations, supported config-file paths, local
API specs, generated-source path signals, test files, and accepted local Markdown
document observations.

### Memory Generator

Writes machine-readable memory artifacts such as `project-map.json` and `evidence-index.jsonl`.

### Agent Files Generator

Writes human-readable and agent-oriented Markdown artifacts such as `endpoints.md` and `agent-guide.md`.

`endpoints.md` is generated directly from deterministic endpoint facts. `agent-guide.md`
is generated from `project-map.json` and `evidence-index.jsonl`, or from the same
structured in-memory facts that are serialized to those files. The guide generator does
not re-analyze source files, call LLMs, call external services, or read documents outside
the structured local-document facts emitted by the analyzer.

These files must not invent architecture beyond the extracted facts, documented
inferences, and explicitly labeled uncertainty.

### Local Docs And Future Issue Ingestors

The current local Markdown ingestor handles a conservative default scope and keeps
document facts separate from code-backed facts. Future external ingestors may import
materials from systems such as YouTrack, Jira, Confluence, GitHub, and GitLab.

Connectors are input adapters. They should normalize external records into source
documents and should not become part of the core Java/Spring analyzer.

### Planned Adapter Layer

The planned v2 adapter layer is an optional input boundary before document/spec/metadata
analysis. It is not implemented in the current v1.x product line.

The adapter layer should:

- keep the Java/Spring core runnable with no adapter configuration;
- default to no network access and no source upload;
- start from explicit local import modes before any future API connector mode;
- produce normalized source documents plus structured provenance;
- label adapter-backed observations as document-backed, spec-backed, metadata-only, or
  warning/status material before they reach generated memory;
- keep adapter provenance available for review without exposing credentials or local
  absolute paths;
- reject or mark uncertain stale, partial, malformed, or ambiguous external records
  instead of turning them into current-state claims.

The adapter layer must not:

- create Java/Spring source-visible facts;
- feed external records into endpoint, component, entity, repository, test, build, or
  config analyzers as if they were repository source;
- make connector data, connector summaries, LLM output, graph metadata, profile
  Markdown, cache metadata, or query output authoritative evidence;
- make network/auth/provider dependencies part of the core analyzer or query layer;
- load plugin code, accept plugin-provided authority, or expose public API/server
  behavior without a separate permission, provenance, and security design.

### Optional LLM Layer

An optional LLM layer may be considered later for presentation, grouping, or
summarization. It must not be required for core analysis or query, and it must not be
treated as the source of truth.

The allowed input boundary is generated deterministic memory: structured project facts,
existing evidence IDs and bounded evidence excerpts, graph navigation metadata,
query/source-artifact metadata, profile metadata, cache status summaries, and future
adapter provenance after those adapter surfaces are explicitly designed. The layer must
not read repository source files, local documents, generated-source contents, connector
exports, remote APIs, credentials, or provider responses directly as a substitute for
the deterministic analyzer and adapter contracts.

Any LLM-generated output must be derived from deterministic facts and evidence
references, must preserve claim labels, and must be visibly labeled as non-evidence if
it is emitted. It may help a human compare, group, or summarize existing facts and
uncertain signals, but it must not create or modify `project-map.json` facts,
`evidence-index.jsonl` records, connector truth, source-backed evidence, security
findings, vulnerability proof, runtime behavior claims, source/spec agreement claims,
or repository-file/code modifications.

Provider use is also outside the current core. No provider, network access,
credentials, telemetry, or source upload may be configured or implied by default. Any
future remote provider mode must be explicitly enabled, minimize prompt inputs, avoid
serializing credentials or local absolute paths, document privacy implications, and pass
a separate implementation and security review before release.

### Planned Plugin And API Surfaces

Plugin loading, public API service modes, MCP/server surfaces, and extension runtimes
are outside the current product. Future platform work must define a default-deny
permission model before implementation, including filesystem access, network access,
credential access, generated-output writes, adapter/provenance authority, and whether an
extension can emit source documents.

Plugin manifests, API requests, API responses, connector records, and repository text
must be treated as untrusted input. They must not bypass adapter validation, path
containment, redaction, evidence requirements, provenance labels, or no-source-upload
defaults.

## Pipeline

The intended pipeline is:

```text
local repository
  -> repository scanner
  -> build detector
  -> Java/Spring analyzers
  -> local Markdown/document analyzer
  -> project graph builder
  -> evidence index builder
  -> memory and agent file generators
  -> .project-memory/
```

A future v2 adapter-enabled pipeline should remain a wrapper around that core pipeline:

```text
local repository
  -> deterministic core pipeline
optional configured adapter inputs
  -> adapter validation and normalization
  -> source documents plus provenance
  -> documented document/spec/metadata boundaries
  -> .project-memory/
```

Adapter-backed data must join generated memory only through documented output and
evidence contracts. It must remain distinguishable from code-backed Java/Spring facts
and from deterministic local Markdown document observations.

An optional future AI presentation layer, if implemented, should sit after deterministic
memory generation and should consume generated memory as a presentation input. It should
not sit before analyzers, alter adapter normalization, write evidence, or feed AI text
back into source-document ingestion as project truth.

## Architectural Constraints

- The core analyzer runs locally.
- The core analyzer does not call external APIs.
- Source code is not sent anywhere by default.
- Facts must be traceable to evidence.
- Output contracts must be explicit and documented.
- Optional future integrations must be adapters around the core, not dependencies inside it.
- Future adapter-backed observations must carry provenance and must not be promoted to
  code-backed facts or authoritative evidence.
- Future AI presentation must be optional, explicitly enabled, non-authoritative, and
  labeled as non-evidence whenever emitted.
- Provider use, network access, credentials, telemetry, and source upload must remain
  off by default.
