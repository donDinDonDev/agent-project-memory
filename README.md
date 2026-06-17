# agent-project-memory

`agent-project-memory` is a local-first CLI/devtool for generating evidence-backed
project memory for Java/Spring codebases.

The goal is to help developers and AI coding agents understand a legacy Java/Spring
project before changing it. The tool scans local Java source, standard Maven and bounded
static Gradle Java/Spring layouts, standard Maven or Gradle test roots, and bounded
local API-surface inputs, extracts deterministic facts, attaches evidence references,
and writes Markdown/JSON artifacts that can be reviewed, versioned, and reused.

The current product focus is intentionally narrow:

- Java/Spring codebases first.
- Local repository analysis first.
- Maven projects first, with bounded static Gradle Java/Spring layout support.
- Deterministic source analysis as the source of truth.
- Optional AI assistance later, outside the core analyzer.

The supported product line is a local-first CLI. Source code must not be sent to
external services by default.

## Security And Sensitive Data

`agent-project-memory` treats repository contents as local, untrusted input and keeps
generated project memory local by default. It is not a SaaS scanner, vulnerability
scanner, general-purpose secret scanner, or secret inventory tool.

The current output contracts avoid serializing known sensitive surfaces such as raw
config values, document bodies, generated-source contents, command logs, local absolute
paths, credentials, tokens, and secret-looking values. The v1.7.0 release adds
bounded deterministic redaction for obvious secret-looking values that may
otherwise appear in generated excerpts or rendered query output, while explicitly
preserving evidence IDs, normalized repository-relative paths, symbols, line ranges,
confidence, uncertainty, and claim categories.

See [SECURITY.md](SECURITY.md) for vulnerability reporting and
[docs/development/THREAT_MODEL.md](docs/development/THREAT_MODEL.md) for the public
product threat model and security limitations.

## Requirements

- Java 21.
- Apache Maven 3.x.

## Download

Published release artifacts are available on the
[GitHub Releases page](https://github.com/donDinDonDev/agent-project-memory/releases).

The latest published release is `v1.9.0`. Its release artifact is
`agent-project-memory-1.9.0.jar`; release assets use `SHA256SUMS` for optional checksum
verification.

```sh
shasum -a 256 -c SHA256SUMS
java -jar agent-project-memory-1.9.0.jar scan /path/to/java-spring-project
```

For published releases, the supported installation path is the executable jar from
GitHub Releases: download the jar, optionally verify `SHA256SUMS`, and run it with
`java -jar`. The v1.x stable line keeps this release-jar path as the minimum planned
distribution path. Package-manager installs and a first-party installed
`agent-project-memory` command are future distribution work and should not be assumed
available until a release note documents them.

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
target/agent-project-memory-1.9.0.jar
```

Release artifact and checksum verification expectations are documented in
[docs/development/RELEASE_PROCESS.md](docs/development/RELEASE_PROCESS.md).

## Quick Start

After `mvn package`, run a scan with the packaged CLI jar:

```sh
java -jar target/agent-project-memory-1.9.0.jar scan /path/to/java-spring-project
```

The packaged CLI also supports help and version commands without scanning:

```sh
java -jar target/agent-project-memory-1.9.0.jar --help
java -jar target/agent-project-memory-1.9.0.jar help
java -jar target/agent-project-memory-1.9.0.jar scan --help
java -jar target/agent-project-memory-1.9.0.jar --version
java -jar target/agent-project-memory-1.9.0.jar version
```

Current v1.x builds also support opt-in agent profile artifact selection:

```sh
java -jar target/agent-project-memory-1.9.0.jar scan /path/to/java-spring-project --agent-profile codex
java -jar target/agent-project-memory-1.9.0.jar scan /path/to/java-spring-project --agent-profile all
```

Supported profile selectors are `codex`, `claude`, `cursor`, `generic`, and `all`.
`--agent-profile` may be repeated, and duplicate selectors are idempotent. Profile
selection is optional; a scan without `--agent-profile` keeps the default output set.
Profile selection writes a generated-profile manifest and selected deterministic profile
Markdown files under `.project-memory/agent-profiles/`. Profile Markdown is
generated only from existing structured project facts and existing evidence references;
it does not add project facts or evidence records.

v1.4 and later release builds also support opt-in incremental scan mode:

```sh
java -jar target/agent-project-memory-1.9.0.jar scan /path/to/java-spring-project --incremental
```

`--incremental` reuses the existing generated output set only after validating cache
schema, tool version, selected CLI options, selected config, selected agent profiles,
input fingerprints, and current generated output fingerprints. The first incremental
scan for a repository state is a cache miss: it runs the normal full analysis path and
refreshes metadata-only cache files under `.project-memory/cache/v1/` after successful
output generation. Missing, stale, unsafe, corrupted, or mismatched cache state fails
closed to normal full analysis. Scans without `--incremental` ignore persistent cache
state and do not read, write, delete, or trust cache files.

v1.6 and later release builds also include read-only query commands over existing
generated artifacts:

```sh
java -jar target/agent-project-memory-1.9.0.jar query /path/to/java-spring-project list modules
java -jar target/agent-project-memory-1.9.0.jar query /path/to/java-spring-project list endpoints
java -jar target/agent-project-memory-1.9.0.jar query /path/to/java-spring-project list api-operations
java -jar target/agent-project-memory-1.9.0.jar query /path/to/java-spring-project list entities
java -jar target/agent-project-memory-1.9.0.jar query /path/to/java-spring-project list tests
java -jar target/agent-project-memory-1.9.0.jar query /path/to/java-spring-project explain evidence <evidence-id>
java -jar target/agent-project-memory-1.9.0.jar query /path/to/java-spring-project find fact <term>
java -jar target/agent-project-memory-1.9.0.jar query /path/to/java-spring-project find symbol <term>
java -jar target/agent-project-memory-1.9.0.jar query /path/to/java-spring-project relations <id>
java -jar target/agent-project-memory-1.9.0.jar query /path/to/java-spring-project relations <id> --direction incoming
```

`query <path> ...` accepts either a repository directory containing
`.project-memory/` or the `.project-memory/` directory itself. These commands read the
existing `project-map.json` and `evidence-index.jsonl` artifacts, print deterministic
human text, and do not run scans, create `.project-memory/`, refresh cache/profile
artifacts, read repository source files, or write repository files. Non-graph query
commands do not require or parse `project-graph.json`; a missing or malformed graph
artifact is ignored unless the command needs graph-backed lookup. Source-visible
endpoint rows and spec-backed declared API operation rows stay separate; entity rows
and embeddable rows stay separate.
`explain evidence <id>` resolves exact evidence IDs from `evidence-index.jsonl`.
`find fact <term>` and `find symbol <term>` are exact and case-sensitive; they do not
perform substring, fuzzy, regex, semantic, natural-language, or embedding search.
`find fact <term>` uses `project-graph.json` only for graph ID-shaped terms such as
`node:`, `edge:`, `relation-status:`, and `graph-warning:` when graph output is present
and valid. `relations <id>` requires a valid `project-graph.json`, accepts either a
graph node ID or a generated fact ID that maps through node `source_ref`, and renders
only one-hop incoming, outgoing, or default `both` graph neighbors while keeping graph
edges separate from `relation_statuses[]`. Graph `source_ref` and `derivation` fields
are navigation metadata, not evidence. Stable JSON query output remains future work and
is not included in v1.8.0.

CLI exit codes are stable for automation:

- `0`: success, help, or version.
- `1`: unexpected internal error.
- `2`: usage error, such as an unknown command, unknown flag, or malformed command.
- `3`: scan/query input or artifact error, such as a missing scan path, missing query
  path, missing directory, unsafe output path, missing query artifact, malformed
  query artifact, or invalid graph artifact for relation lookup.
- `4`: invalid scan config.
- `5`: output generation or write error.
- `6`: query no-result, such as an absent evidence ID, absent exact fact/symbol match,
  or absent relation subject ID.

Normal scan stdout is concise and deterministic: it reports `.project-memory`
preparation, generated file names with stable fact counts when outputs are written, and
a bounded diagnostics summary.

`scan <path>` validates that the path exists and is a directory, then creates or reuses:

```text
<path>/.project-memory/
```

Existing unrelated contents inside `.project-memory/` are preserved. Generated files are
rewritten deterministically when supported Maven module roots, source-visible Maven
metadata from module POMs, supported Gradle build files or static settings includes,
supported root source, test, or resource roots, supported config files, local
OpenAPI/Swagger spec files, safe default-scope local Markdown documents, Spring
repository signals, Spring configuration surface signals, Spring behavior or messaging
listener signals, or Maven/Gradle module warnings are detected.

When the scanned path has a root `pom.xml`, the current implementation discovers the
scan root and root-declared Maven child modules, then runs the Spring MVC endpoint,
Spring component, Spring repository signal, Spring configuration surface, Spring
behavior/messaging signal, Spring Security configuration warning, JPA entity, hidden
HTTP surface warning, and tests inventory analyzers per supported module. For
Gradle inputs, the current implementation discovers accepted root Gradle build files,
simple static `settings.gradle` or `settings.gradle.kts` include declarations, and
standard Gradle Java/source/test/resource roots without executing Gradle or resolving an
effective Gradle model. For
compatibility with earlier local source-root scans, a
repository without a root `pom.xml` but with supported root source, test, or resource roots is
represented as the scan-root module with module discovery marked `not_detected`.

The analyzer extracts Spring MVC controllers and source-visible interface-declared
Spring MVC mappings that can be uniquely bound to concrete handlers, direct Spring
stereotype components on classes and interfaces, deterministic hidden HTTP surface
warnings, direct JPA entity annotations with conservative source-visible
mapped-superclass identifier fields, standard Maven or Gradle test-root classes with
conservative helper filtering, direct source-visible Maven metadata from module POMs,
direct source-visible Maven dependency and plugin declarations from module POMs, and
bounded Gradle build layout observations, plus
path-only standard resource-root and supported application/logging config-file
inventory. It also emits direct source-visible `@Repository` repository surface facts
and inferred source-visible Spring Data repository interface extension signals in a
separate Spring application surface section, with conservative inferred repository/entity
links when a supported Spring Data generic type matches exactly one emitted entity fact,
plus direct source-visible `@Configuration`
class, `@ConfigurationProperties` type, and `@Bean` method signals without runtime bean
graph or binding claims, direct source-visible `@Transactional`, `@Scheduled`, and
`@EventListener` signals, and common source-visible Kafka/Rabbit listener annotation
signals without runtime transaction, scheduler, event delivery, message topology, or
broker behavior claims, plus source-visible Spring Security configuration warnings for
supported security annotations and `SecurityFilterChain` `@Bean` methods without
security policy, endpoint protection, authentication, authorization, filter-chain
ordering, vulnerability, or correctness claims. It also discovers common local
OpenAPI/Swagger spec filenames as declared API
inputs, extracts minimal spec-backed declared OpenAPI/Swagger operations, discovers
generated-source/codegen root metadata without reading generated source contents, and
discovers safe default-scope local Markdown document inventory with deterministic ATX heading
references and bounded chunk references, with resolving document evidence for file,
heading, chunk, and bounded reconciliation mention observations, plus conservative
`documents.reconciliation` inspection hints for document-only endpoint-like path
mentions, document-only module references, and source-backed API/module facts with no
obvious default-scope document mention, plus compact local-document guide rendering from
structured document inventory, bounded heading/chunk references, and uncertain
reconciliation hints. Supported scans also emit a bounded deterministic lightweight
relation graph over existing facts, with direct/structural nodes and `owns`/`declares`
edges, conservative inferred repository/entity and tested-subject relation edges, and
status-only or uncertain relation rows kept separate from edges, then write:

```text
<path>/.project-memory/project-map.json
<path>/.project-memory/project-graph.json
<path>/.project-memory/endpoints.md
<path>/.project-memory/evidence-index.jsonl
<path>/.project-memory/agent-guide.md
```

When `--agent-profile` is selected and the scan writes the base output files, the scan
also writes:

```text
<path>/.project-memory/agent-profiles/manifest.json
<path>/.project-memory/agent-profiles/<selected-profile>.md
```

Only selected profile Markdown files are written. Unsupported directories that only
prepare `.project-memory/` do not create orphan profile artifacts.

When `--incremental` is selected and the scan writes the base output files, the scan
also writes cache metadata:

```text
<path>/.project-memory/cache/v1/manifest.json
<path>/.project-memory/cache/v1/inputs.jsonl
<path>/.project-memory/cache/v1/outputs.jsonl
```

These cache files are execution metadata, not evidence. They store bounded
repository-relative paths, SHA-256 fingerprints, byte counts, schema/tool metadata,
selected profile names, and redacted option/config matching metadata only. They do not
add fields to `project-map.json`, do not create `evidence-index.jsonl` records, and do
not store source bodies, local document bodies, config contents, generated-source
contents, generated Markdown bodies, local absolute paths, command logs, timing
measurements, credentials, tokens, or secret-looking values.

`project-map.json` is the minimal stable machine-readable project map. Current
development output uses `schema_version: "1.0"` and includes redacted scan metadata for
safe root-local config selection, detected root `pom.xml` build metadata when present,
accepted Gradle build-file summary fields when present, Maven or Gradle module
inventory,
module-owned source-visible Maven metadata under
`project.modules.items[].build_config.maven.metadata`, module-owned source-visible Maven
dependency inventory under `project.modules.items[].build_config.maven.dependencies` and
`dependency_management`, module-owned source-visible Maven plugin inventory under
`project.modules.items[].build_config.maven.plugins` and `plugin_management`,
module-owned bounded Gradle build-file orientation under
`project.modules.items[].build_config.gradle`, compatibility source and test root
summaries, module-owned standard resource-root inventory under
`project.modules.items[].build_config.resources`, module-owned path-only supported
application/logging config-file inventory under
`project.modules.items[].build_config.config_files`, module-owned direct source-visible
Spring Boot application signals under
`project.modules.items[].build_config.spring_boot_applications`, API surface categories
for source-visible endpoint facts, local OpenAPI/Swagger spec file facts under
`api_surface.openapi.spec_files`, minimal declared operation facts under
`api_surface.openapi.operations`, top-level generated-source/codegen policy and
metadata-only root inventory under `generated_sources`, direct `module_id`
fields on module-owned facts, Spring MVC endpoint facts, hidden HTTP surface,
generated-source, and Maven module warnings that are not expanded into endpoint/API
facts, direct component inventory, direct JPA entity facts with bounded source-visible
field metadata, partial embedded/identifier signals, relationship metadata, and
repository/entity relation statuses for the current JPA/domain slice, a bounded
source-visible tests inventory with stable test IDs, module ownership, direct framework
signal classifications, supported JUnit test method annotations, direct Spring test
slice annotations, conservative mock annotation signals, and conservative
tested-subject relation/status rows, a top-level `quality` object with conservative
test-gap and change-risk planning hints,
the staged `spring_application_surface.repositories` repository signal inventory,
the staged `spring_application_surface.configuration` configuration class,
configuration-properties, and bean method inventories,
`spring_application_surface.behavior` transaction, scheduled, and event listener
inventories, `spring_application_surface.messaging.listener_signals` inventories, and
`spring_application_surface.security.configuration_warnings` warning-ID references, a
top-level `scan` object with redacted config, feature, path-policy, and diagnostic
metadata, and a top-level `documents` object with deterministic default-scope local
Markdown discovery policy metadata, document inventory, ATX heading references, and
bounded chunk references, conservative low-confidence reconciliation hints, and evidence
ID references. The current Spring
application surface implementation emits
repository, configuration-surface, behavior, and messaging facts, plus Spring Security
configuration warning references when bounded source-visible signals are detected. The
current v0.6 JPA/domain implementation emits field metadata for direct field-level
`@Column`, `@Enumerated`, `@GeneratedValue`, and `@Version` annotations, direct
`@Embeddable` facts, direct field-level `@Embedded`/`@EmbeddedId` signals, and direct
class-level `@IdClass` composite-id signals, direct source-visible relationship
metadata, conservative Spring Data repository/entity inferred relations, and quiet
no-domain guide rendering without runtime schema, access-strategy, generated-identifier,
optimistic-locking, composite-key, relationship target-resolution, repository runtime,
or provider-default claims.
`endpoints.md` is a deterministic API surface Markdown inventory that keeps
source-visible Spring MVC endpoints, declared OpenAPI operations, generated-source API
signals, repository-rest warnings, and hidden HTTP warnings in separate sections.
`evidence-index.jsonl` contains source-backed evidence records referenced by generated
facts. `agent-guide.md` is a deterministic orientation guide generated only from the
structured project-map facts and evidence index.

Compatibility and migration notes:

- The v0.9-to-v1.0 output migration is limited to the normal generated
  `project-map.json` marker moving from `schema_version: "0.9"` to `"1.0"`. The current
  JSON shape and evidence semantics are preserved.
- The v1.1 Gradle expansion keeps `schema_version: "1.0"` and adds Gradle and mixed
  Maven/Gradle fields only where Gradle inputs are detected. Existing Maven fields and
  evidence semantics are preserved.
- The v1.2 generated-source/codegen expansion keeps `schema_version: "1.0"` and adds
  top-level `generated_sources` policy and root metadata with
  `content_status: "not_scanned"`. Generated-source content scanning remains
  unavailable, and `features.generated_sources: true` remains invalid config.
- The v1.4 incremental cache mode keeps `schema_version: "1.0"` and adds only opt-in
  `.project-memory/cache/v1/` execution metadata. Validated cache hits reuse the
  existing generated output set without adding cache fields to `project-map.json`, and
  normal scans without `--incremental` preserve full-scan behavior.
- The v1.5 graph expansion keeps `project-map.json` on `schema_version: "1.0"` and
  adds `.project-memory/project-graph.json` with `graph_schema_version: "1.0"` as a
  separate navigation artifact over existing facts and evidence references.
- The v1.6 query expansion keeps generated artifact schemas unchanged. Query commands
  read existing `project-map.json`, `project-graph.json` when graph lookup is needed,
  and `evidence-index.jsonl` artifacts without scanning source, writing repository
  files, or treating query output as evidence.
- Consumers that accept only known schema markers should add `"1.0"` for the preserved
  v0.9 shape. Regenerate the base `.project-memory/` files together so JSON facts,
  graph nodes, evidence IDs, and Markdown evidence references stay aligned.
- `project-map.json`, `project-graph.json`, and `evidence-index.jsonl` are the stable
  machine-readable outputs. `endpoints.md` and `agent-guide.md` are deterministic human-readable
  presentations; their filenames, cautious categories, and evidence visibility are
  documented expectations, while exact Markdown wording or layout may evolve.
- Future breaking changes, deprecations, and required migration steps are documented in
  the architecture contract, changelog, and release notes for the release that changes
  behavior.

## Future Installed Usage

The current supported path is the release jar documented above. The command forms below
describe the intended installed-command UX for a future distribution channel; no
first-party shell wrapper, Homebrew formula, JBang catalog, Maven Central CLI
installation, SDKMAN/asdf plugin, native image, or container image is currently
published.

Future installed command:

```sh
agent-project-memory scan .
```

Future installed help and version command forms:

```sh
agent-project-memory --help
agent-project-memory help
agent-project-memory scan --help
agent-project-memory --version
agent-project-memory version
```

The same output files:

```text
.project-memory/project-map.json
.project-memory/project-graph.json
.project-memory/evidence-index.jsonl
.project-memory/endpoints.md
.project-memory/agent-guide.md
```

These files are meant to give humans and coding agents a compact, evidence-backed map
of the project plus a bounded relation graph: detected build layout, Spring MVC
endpoints, generated-source/codegen metadata, important components, structural graph
navigation, and references back to the source files that prove each fact.

## Public Documentation Map

Start here:

- Generated-output example:
  [examples/stage3-project-map/README.md](examples/stage3-project-map/README.md).
- v1.9 release summary:
  [docs/product/V1_9_RELEASE_NOTES.md](docs/product/V1_9_RELEASE_NOTES.md).
- v1.8 release summary:
  [docs/product/V1_8_RELEASE_NOTES.md](docs/product/V1_8_RELEASE_NOTES.md).
- v1.7 release summary:
  [docs/product/V1_7_RELEASE_NOTES.md](docs/product/V1_7_RELEASE_NOTES.md).
- v1.6 release summary:
  [docs/product/V1_6_RELEASE_NOTES.md](docs/product/V1_6_RELEASE_NOTES.md).
- v1.5 release summary:
  [docs/product/V1_5_RELEASE_NOTES.md](docs/product/V1_5_RELEASE_NOTES.md).
- v1.4 release summary:
  [docs/product/V1_4_RELEASE_NOTES.md](docs/product/V1_4_RELEASE_NOTES.md).
- v1.3 release summary:
  [docs/product/V1_3_RELEASE_NOTES.md](docs/product/V1_3_RELEASE_NOTES.md).
- v1.2 release summary: [docs/product/V1_2_RELEASE_NOTES.md](docs/product/V1_2_RELEASE_NOTES.md).
- v1.1 release summary: [docs/product/V1_1_RELEASE_NOTES.md](docs/product/V1_1_RELEASE_NOTES.md).
- v1.0 release summary: [docs/product/V1_0_RELEASE_NOTES.md](docs/product/V1_0_RELEASE_NOTES.md).
- v0.9 release summary: [docs/product/V0_9_RELEASE_NOTES.md](docs/product/V0_9_RELEASE_NOTES.md).
- v0.8 release summary: [docs/product/V0_8_RELEASE_NOTES.md](docs/product/V0_8_RELEASE_NOTES.md).
- v0.7 release summary: [docs/product/V0_7_RELEASE_NOTES.md](docs/product/V0_7_RELEASE_NOTES.md).
- v0.6 release summary: [docs/product/V0_6_RELEASE_NOTES.md](docs/product/V0_6_RELEASE_NOTES.md).
- v0.5 release summary: [docs/product/V0_5_RELEASE_NOTES.md](docs/product/V0_5_RELEASE_NOTES.md).
- v0.4 release summary: [docs/product/V0_4_RELEASE_NOTES.md](docs/product/V0_4_RELEASE_NOTES.md).
- v0.3 release summary: [docs/product/V0_3_RELEASE_NOTES.md](docs/product/V0_3_RELEASE_NOTES.md).
- v0.2 release summary: [docs/product/V0_2_RELEASE_NOTES.md](docs/product/V0_2_RELEASE_NOTES.md).
- v0.1 release summary: [docs/product/V0_1_RELEASE_NOTES.md](docs/product/V0_1_RELEASE_NOTES.md).
- v1.0 evaluation corpus summary:
  [docs/development/evaluations/v1.0-evaluation-corpus_SUMMARY.md](docs/development/evaluations/v1.0-evaluation-corpus_SUMMARY.md).
- v1.1 Gradle evaluation summary:
  [docs/development/evaluations/v1.1-gradle-java-spring_SUMMARY.md](docs/development/evaluations/v1.1-gradle-java-spring_SUMMARY.md).
- v1.4 incremental cache evaluation summary:
  [docs/development/evaluations/v1.4-incremental-cache_SUMMARY.md](docs/development/evaluations/v1.4-incremental-cache_SUMMARY.md).
- Product scope and boundaries: [docs/product/MVP_SPEC.md](docs/product/MVP_SPEC.md) and
  [docs/product/NON_GOALS.md](docs/product/NON_GOALS.md).
- Product direction and release tracks:
  [docs/product/POST_V0_1_STRATEGY.md](docs/product/POST_V0_1_STRATEGY.md) and
  [docs/product/ROADMAP.md](docs/product/ROADMAP.md).
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

Public evaluation summaries are linked from the release notes as supporting detail.

## What This Is Not

`agent-project-memory` is not:

- a generic AI documentation generator,
- a repository chatbot,
- a RAG system,
- a SaaS product,
- a hosted codebase wiki,
- a tool that treats LLM output as the source of truth,
- an automatic code modification system.

AI may become an optional presentation, grouping, or summarization layer later, but the
core project memory must come from deterministic analysis, explicit output contracts,
and evidence references. Any future AI output must be labeled as non-evidence, must not
create project facts or security findings, and must not require source upload, network
access, provider credentials, repository chat, generic RAG, or automatic code
modification by default. The current v1.x product line includes no AI provider
integration.

## Project Status

The latest published release is `v1.9.0`. It ships an executable jar and `SHA256SUMS`
asset. Local builds produce `target/agent-project-memory-1.9.0.jar`. Normal generated
`project-map.json` files use `schema_version: "1.0"` as a stable-line marker. The v1.5
lightweight relation graph expansion is additive, the v1.6 read-only query expansion
adds deterministic artifact-backed lookup commands without changing generated
project-memory schemas or evidence semantics, the v1.7 release adds deterministic
redaction hardening for selected generated and rendered strings without adding evidence
fields or schema markers, and the v1.8 release adds public examples and contributor
onboarding polish without changing analyzer behavior, generated artifact schemas, or
evidence semantics. The v1.9 release adds public v2 architecture preparation for
planned adapters, connector provenance, optional AI presentation, plugin/API security,
and v1-to-v2 migration boundaries without changing analyzer behavior, CLI commands or
flags, generated artifact schemas, evidence semantics, or packaged runtime behavior.
Unreleased v2 development includes a disabled-by-default local structured import
reference adapter for explicitly configured repository-relative export files. Adapter
enabled scans emit `source-registry.json` and `project-map.json`
`schema_version: "2.0"` adapter context as provenance-backed external/document context
only.

The current Java/Spring line includes module-aware Maven analysis, build/config
orientation, bounded static Gradle Java/Spring layout support, source-visible Spring
MVC and application-surface signals, declared OpenAPI operations, bounded JPA/domain
metadata, source-visible test and quality planning signals, default-scope local Markdown
document inventory, opt-in deterministic agent profile artifacts, opt-in incremental
cache metadata under `.project-memory/cache/v1/`, a bounded lightweight relation graph
artifact under `.project-memory/project-graph.json`, read-only text query commands over
existing generated artifacts, deterministic output redaction for obvious
secret-looking values, redacted scan metadata, safe root-local YAML config support,
stable CLI help/version behavior, and documented release-jar verification.

Earlier v0.x release notes remain available for historical scope, compatibility, and
validation details. Network connector work remains a later optional adapter track and is
not started.

The current implementation includes a Java 21 Maven-built CLI, root-declared Maven
module discovery, bounded static Gradle root and multi-project discovery,
JavaParser-backed Spring MVC endpoint extraction, source-visible interface mapping
support when uniquely bindable, stable `project-map.json` and `evidence-index.jsonl`
outputs, deterministic module-owned source-visible Maven metadata, dependency, and
plugin extraction, deterministic bounded Gradle build-file and standard-root
orientation, deterministic direct Spring component and JPA entity inventories,
deterministic path-only resource-root and supported config-file
discovery, deterministic hidden HTTP surface, generated-source, and Maven module
warnings, deterministic local OpenAPI/Swagger spec file discovery as declared API
inputs, minimal deterministic OpenAPI/Swagger operation extraction as spec-backed
declared operation facts, deterministic generated-source/codegen metadata-only
inventory under `generated_sources`, a minimal deterministic tests inventory, deterministic
repository signal extraction for direct `@Repository` and supported Spring Data
repository interface extensions, deterministic configuration surface extraction for
direct `@Configuration`, direct `@Bean`, and direct `@ConfigurationProperties`
observations, deterministic behavior and messaging signal extraction for direct
`@Transactional`, `@Scheduled`, `@EventListener`, and common Kafka/Rabbit listener
annotations, deterministic Spring Security configuration warning extraction for
supported security annotations and `SecurityFilterChain` `@Bean` methods, deterministic
direct source-visible JPA field annotation extraction for `@Column`, `@Enumerated`,
`@GeneratedValue`, and `@Version`, deterministic partial embedded and identifier model
signals for direct `@Embeddable`, `@Embedded`, `@EmbeddedId`, and `@IdClass`,
deterministic direct source-visible relationship metadata extraction for relationship
cardinality, direct `mappedBy`, bounded `@JoinColumn` and `@JoinTable` metadata, and
direct relationship `optional`, `fetch`, `cascade`, and `orphanRemoval` attributes,
deterministic bounded tests inventory refinement for direct JUnit Jupiter/JUnit 4 test
method annotations and direct JUnit/Spring Test framework signals where source origin is
trusted, direct Spring test slice annotation extraction for `@SpringBootTest`,
`@WebMvcTest`, `@DataJpaTest`, and `@ContextConfiguration`, conservative
source-visible `@MockBean` and `@SpyBean` annotation signals on emitted test classes,
conservative tested-subject relation/status rows from supported naming, exact
production imports, direct field types, and direct Spring test slice class literals,
conservative test-gap and change-risk planning hints from existing deterministic facts
and inferred tested-subject relations, deterministic default-scope local Markdown
document discovery, inventory, ATX heading references, and bounded chunk references with
safe path exclusions, aggregate caps, and no symlink following, conservative local
Markdown/code reconciliation hints kept under `documents.reconciliation`, deterministic
root-local `agent-project-memory.yml` config discovery with optional explicit
`scan <path> --config <repo-relative-yaml>` selection, safe config defaults, local
Markdown-only user include/exclude refinement, non-overridable built-in document safety
exclusions, reserved generated-source and symlink-following modes rejected when enabled,
redacted `scan` metadata and bounded diagnostics that avoid raw config values and raw
user path patterns, deterministic lightweight relation graph generation in
`project-graph.json` from existing facts, evidence IDs, and derivation metadata,
`endpoints.md`, and deterministic `agent-guide.md` generation from the
structured facts and evidence index, including module-grouped Spring application
surface guidance, bounded JPA field metadata, embedded/id, relationship metadata
guidance, source-visible test method/framework/slice/mock/tested-subject guidance, and
quality/change-risk planning guidance
that keeps extracted facts, inferred signals, relation statuses, uncertain targets,
not-analyzed statuses, and warnings separate.

Current limitations:

- Maven module support is limited to the scan root and modules declared directly under
  the root `pom.xml` `<modules>` section. It does not resolve Maven profiles, recursively
  discover nested modules, reconstruct effective POMs, build dependency graphs, or run
  Maven.
- Maven metadata extraction is limited to direct source-visible module POM text for
  `groupId`, `artifactId`, `version`, `packaging`, and parent coordinates. It preserves
  property references and expressions as source-visible values and does not fill missing
  coordinates from Maven defaults, parent inheritance, profiles, or effective POM data.
- Maven dependency inventory is limited to direct source-visible module POM
  `<dependencies><dependency>` declarations and separate direct
  `<dependencyManagement><dependencies><dependency>` management declarations. It
  preserves direct `groupId`, `artifactId`, `version`, `scope`, `optional`, `type`, and
  `classifier` text when present, preserves property references and expressions as
  source-visible values, and does not resolve parent, managed, profile, effective, or
  transitive dependency behavior.
- Maven plugin inventory is limited to direct source-visible module POM
  `<build><plugins><plugin>` declarations and separate direct
  `<build><pluginManagement><plugins><plugin>` management declarations. It preserves
  direct plugin coordinates, bounded direct execution IDs, phases, goals, and conservative
  configuration/generator signal names without storing arbitrary plugin configuration
  values. It does not resolve plugin versions, reconstruct lifecycle bindings, inherit
  executions, execute plugins, scan generated sources by default, parse OpenAPI
  operations, or create generated API/endpoint facts from plugin signals.
- Gradle support is limited to accepted root `settings.gradle`, `settings.gradle.kts`,
  `build.gradle`, and `build.gradle.kts` files, project `build.gradle` or
  `build.gradle.kts` files under supported Gradle project directories, simple static
  string-literal settings includes, and standard Java/test/resource roots. It does not
  execute Gradle, invoke the wrapper, use the Gradle Tooling API, evaluate build
  scripts, resolve plugins, dependencies, repositories, tasks, effective models, custom
  `sourceSets`, `projectDir` remapping, included builds, or Kotlin source structure.
- Resource-root discovery is limited to standard `src/main/resources` and
  `src/test/resources` roots under supported modules. Config-file discovery is limited
  to supported Spring `application.properties`, `application.yml`, `application.yaml`,
  profile-specific `application-*` variants, and supported logging configuration
  filenames. It records paths and filename-derived metadata only; it does not parse or
  output config keys, values, YAML nodes, XML elements, environment placeholders,
  decrypted values, profile activation, or runtime configuration precedence.
- Spring Boot application build/config signals are limited to direct source-visible
  `@SpringBootApplication` annotations under supported production source roots and a
  bounded source-visible `static void main(String[] args)` or varargs `main` method
  signal on the annotated class. They do not prove executable jar packaging, active
  profiles, runtime auto-configuration, component scanning results, bean graphs,
  deployment behavior, or actual process entrypoint behavior.
- Component inventory is limited to direct source-type-level `@Component`, `@Service`,
  `@Repository`, `@Controller`, `@RestController`, and `@Configuration` annotations on
  Java classes or interfaces under `src/main/java`. It does not infer repositories from
  `extends JpaRepository` without a direct supported stereotype. Inferred Spring Data
  repository interface extension signals live separately under
  `spring_application_surface.repositories`, not in `components.items`.
- Component analysis does not model Spring component scanning semantics, bean lifecycle,
  bean names, scopes, conditional configuration, dependency injection, or autowiring graphs.
- Spring application surface repository analysis is limited to repository signals:
  direct source-visible `@Repository` observations and inferred source-visible Java
  interfaces that directly extend a supported Spring Data repository base type visible
  through a fully qualified name or explicit single-type import. Supported base types
  are `org.springframework.data.repository.Repository`,
  `org.springframework.data.repository.CrudRepository`,
  `org.springframework.data.repository.PagingAndSortingRepository`,
  `org.springframework.data.jpa.repository.JpaRepository`, and
  `org.springframework.data.mongodb.repository.MongoRepository`. Repository/entity
  relations are inferred only when a supported source-visible repository generic type
  can be matched to exactly one emitted entity fact; missing, ambiguous, raw, wildcard,
  nested, or otherwise unsupported generic shapes use explicit relation statuses and do
  not emit relation objects. It does not perform dependency type solving, wildcard-import
  fallback, runtime Spring Data reconstruction, query method parsing, database access
  analysis, dependency graph analysis, or runtime repository/entity verification.
- Spring application surface configuration analysis is limited to direct source-visible
  `@Configuration` classes, direct source-visible `@ConfigurationProperties` types, and
  direct source-visible `@Bean` methods visible through a fully qualified name or
  explicit single-type import. It does not extract configuration file values, emit
  `@ConfigurationProperties` `prefix` or `value` annotation values, prove binding
  success, infer active profiles, reconstruct runtime bean graphs, infer effective bean
  names, or model scopes, lifecycle, proxies, autowiring, or dependency graphs.
- Spring application surface behavior analysis is limited to direct source-visible
  Spring `@Transactional` annotations on Java types and methods, direct source-visible
  Spring `@Scheduled` methods, and direct source-visible Spring `@EventListener`
  methods visible through a fully qualified name or explicit single-type import. It
  does not interpret transaction propagation, effective transaction managers, rollback
  behavior, scheduler enablement, runtime registration, frequency correctness, cluster
  behavior, event publication paths, listener ordering, transaction phases, event
  delivery, or call graph effects.
- Spring application surface messaging analysis is limited to direct source-visible
  Spring Kafka `@KafkaListener`/`@KafkaListeners` and Spring AMQP Rabbit
  `@RabbitListener`/`@RabbitListeners` annotations on Java types and methods visible
  through a fully qualified name or explicit single-type import. It records annotation
  presence and framework family only; it does not serialize topic, queue, exchange,
  routing-key, or group-id annotation values, verify broker topology, infer consumer
  groups, bindings, delivery semantics, or deployment configuration.
- Spring application surface security analysis is limited to source-visible Spring
  Security configuration warnings for supported direct security annotations and
  `SecurityFilterChain` `@Bean` methods visible through a fully qualified name or
  explicit single-type import. It records warning/change-risk signals only; it does not
  analyze security policy, endpoint protection state, authentication behavior,
  authorization behavior, runtime filter-chain ordering, vulnerabilities, or security
  correctness.
- Entity analysis is limited to direct class-level `@Entity`, direct class-level
  `@Table(name = "...")`, field-level `@Id` declared on the entity class or on a
  conservative source-visible `@MappedSuperclass` chain, field-level `@Column`,
  `@Enumerated`, `@GeneratedValue`, and `@Version` annotations on direct entity fields,
  direct `@Embeddable` classes, direct field-level `@Embedded` and `@EmbeddedId`
  signals, direct class-level `@IdClass` signals, and field-level `@ManyToOne`,
  `@OneToMany`, `@OneToOne`, and `@ManyToMany` annotations under `src/main/java`.
- Entity field metadata is limited to supported direct field-level annotations on the
  entity class. It records only bounded source-visible annotation attributes for
  `@Column`, `@Enumerated`, and `@GeneratedValue`, plus direct `@Version` presence, and
  does not fill runtime JPA defaults.
- Entity relationship metadata is limited to supported direct field-level relationship
  annotations and direct source-visible annotation attributes. It records cardinality
  from the relationship annotation, direct string-literal `mappedBy`, bounded direct
  `@JoinColumn` and `@JoinTable` metadata, and direct `optional`, `fetch`, `cascade`,
  and `orphanRemoval` values when supported. Missing attributes remain `null` or empty
  arrays, relationship targets remain declared-type-only and uncertain, and the output
  does not claim ORM ownership correctness, foreign keys, join tables, database
  constraints, fetch behavior, cascade behavior, provider defaults, or runtime ORM
  behavior.
- Entity embedded/id support is partial: it records direct `@Embeddable`,
  `@Embedded`, `@EmbeddedId`, and `@IdClass` source-visible signals, links embedded
  targets only when a unique local embeddable can be matched deterministically, and
  marks unresolved embedded targets and `@IdClass` semantic reconstruction explicitly.
- Entity analysis does not implement getter/property-access mapping, full composite-key
  semantic reconstruction, schema generation, transactional semantics, symbol solving,
  relationship target resolution, runtime repository/entity verification, or ORM runtime
  behavior.
- API surface spec discovery is limited to common local filenames such as
  `openapi.yml`, `openapi.yaml`, `openapi.json`, `swagger.yml`, `swagger.yaml`, and
  `swagger.json`. It records normalized repository-relative paths, format, spec kind,
  bounded version signals when directly visible near the file header, module ownership
  when the file is under a supported module, and `api_spec` evidence. Minimal operation
  extraction reads bounded local YAML/JSON specs and records only declared path, HTTP
  method, bounded `operationId`, bounded tags, `implementation_status: "not_analyzed"`,
  and operation `api_spec` evidence. It does not validate the full spec, follow `$ref`,
  fetch external schemas, claim implementation, treat symlink entries as spec files, or
  scan generated-source roots. Invalid or unsupported specs degrade to warnings rather
  than endpoint facts.
- Generated-source/codegen metadata remains warning, inventory, and guide orientation
  only. Generated roots are reported with `content_status: "not_scanned"` and
  `source_origin: "metadata_only"`; generated-source contents are not read, do not
  produce scanned-content evidence, and do not create endpoint, component, Spring/JPA,
  test, or API implementation facts. `features.generated_sources: false` remains a
  valid reserved config value, while `features.generated_sources: true` remains invalid
  config.
- Hidden HTTP surface and generated-source warnings are limited to OpenAPI/Swagger spec filename presence,
  supported module `pom.xml` OpenAPI/Swagger Maven plugin declarations under
  `<build><plugins>` or `<build><pluginManagement><plugins>`, bounded Maven generator,
  annotation-processor, generated-source configuration, and build-helper add-source
  signals, common local generated-source root path presence such as
  `target/generated-sources`, and direct `@RepositoryRestResource`. Generated-source path
  warnings record the normalized path only and do not read generated source contents.
  These warnings do not create endpoint facts, parse OpenAPI schemas, run Maven
  generation, scan `target/generated-sources` by default, or reconstruct generated APIs.
- Relationship facts preserve the declared field type and direct source-visible
  relationship metadata only; target type resolution is explicitly marked uncertain.
- Tests inventory is limited to test-like Java classes under supported standard Maven or
  Gradle `src/test/java` roots; helper/support/configuration declarations without clear test
  naming or direct test-class marker annotations are omitted. Test method inventory is
  limited to directly declared methods with supported directly visible JUnit Jupiter or
  JUnit 4 test annotations resolved from a fully qualified annotation name or explicit
  single-type import. Lifecycle, setup, teardown, helper, support, and configuration
  methods are not counted as test methods.
- Test framework signals are limited to directly visible imports and annotations for
  JUnit Jupiter, JUnit 4, and Spring Test signals where the source origin is trusted.
  Import evidence is attached only to top-level emitted test classes; nested emitted
  test classes use their own class or method annotation evidence. These signals do not
  prove test engine execution, CI behavior, assertion behavior, runtime Spring context
  startup, or coverage.
- Spring test slice and mock annotation signals are limited to directly visible
  supported annotations on emitted test classes. Direct slice annotation class literals
  may contribute only conservative tested-subject relation/status rows. Slice/mock
  signals do not reconstruct runtime Spring test contexts, prove MockMvc setup, bean
  graph contents, database access, Mockito behavior, or slice correctness.
- Tested-subject rows are conservative inferred/statused hints from supported naming
  conventions, exact production-class imports, direct test field types, and direct
  Spring test slice class literals against production classes in the same supported
  module. Ambiguous, missing, unsupported, or absent signals are represented with
  explicit relation statuses, low confidence, and uncertainty instead of coverage or
  runtime claims.
- Tests inventory does not claim code coverage, test execution results, behavioral
  assertion analysis, call graph resolution, symbol solving, or complete subject mapping.
- Quality test-gap and change-risk signals are conservative planning hints derived from
  existing deterministic facts and inferred tested-subject relations. They do not claim
  coverage, test execution, assertion behavior, CI results, runtime behavior, production
  impact, vulnerability, correctness, business priority, or complete subject mapping.
- `agent-guide.md` is generated from existing deterministic output facts only. It may
  render compact local Markdown document inventory, bounded heading/chunk navigation
  references, and uncertain reconciliation hints from structured `documents` facts, but
  it does not render document bodies, summarize source files or local docs, infer
  architecture layers, or add claims beyond extracted facts, explicit inferences, and
  known uncertainty labels.
- Local Markdown/document ingestion is limited to conservative default-scope document
  inventory, deterministic ATX heading references, and bounded chunk references with
  `content_status: "not_serialized"` and resolving `document` evidence for file,
  heading, chunk, and bounded reconciliation mention observations. Reconciliation rows
  are low-confidence uncertain inspection hints only; they do not prove stale
  documentation, missing documentation, coverage, completeness, correctness, or source
  and document agreement. The current implementation caps emitted local Markdown output
  at 256 accepted documents, 16 MiB of aggregate accepted Markdown bytes, 4096 heading
  references, 4096 chunk references, 2048 reconciliation mention observations, and 2048
  reconciliation rows per scan; cap hits are reported as bounded non-fatal
  `scan.diagnostics` items and do not create evidence records. The implementation
  renders local-document guide sections only from structured document facts and
  evidence; it does not read
  hidden/private/generated/dependency/maintainer paths, follow symlinks, or summarize or
  serialize document bodies.
- Agent profile artifacts are opt-in generated presentations only. They reference
  existing project facts and evidence, do not add `project-map.json` fields, do not
  create evidence records, do not replace `agent-guide.md`, and do not modify root
  repository instruction/config files such as `AGENTS.md`, `CLAUDE.md`, Cursor rules,
  IDE settings, source files, docs, or config files.
- Root-local scan configuration is limited to the safe YAML schema introduced in v0.9:
  `version: 1`, optional `features.local_markdown`, reserved
  `features.generated_sources: false` and `features.follow_symlinks: false`, optional
  `documents.include`/`documents.exclude` path rules, and disabled-by-default adapter
  config for local structured import. When explicitly enabled, the adapter reads one
  validated repository-relative regular-file import path under the scan root, parses the
  bounded local structured import JSON format, emits `source-registry.json`, and adds
  `project-map.json` adapter context as provenance-backed external/document context. It
  does not enable network access, accept credentials, serialize raw bodies or configured
  import paths, create `evidence-index.jsonl` records, or create Java/Spring project
  facts. User include/exclude rules apply only to local Markdown discovery through
  normalized repository-relative paths, cannot override built-in safety exclusions, and
  are summarized only through redacted counts and statuses in `scan` metadata.
- `evidence-index.jsonl` currently contains root and child `pom.xml` `build_file`
  evidence when present, accepted Gradle build-file and static include `build_file`
  evidence when present, bounded source-visible Maven metadata, dependency, plugin, and
  module declaration `build_file` evidence, path-oriented `config_file` evidence,
  bounded Spring MVC endpoint, warning, component stereotype, JPA annotation, Spring
  Boot application, Spring repository stereotype and interface signal, local
  OpenAPI/Swagger `api_spec`, generated-source path `path_signal`, tests inventory
  evidence, and local Markdown `document` evidence for accepted file, heading, chunk,
  and bounded reconciliation mention observations.
- The CLI uses only Java standard library argument handling.

For the concise v0.1 scope, evaluation summary, limitations, and validation surface, see
[docs/product/V0_1_RELEASE_NOTES.md](docs/product/V0_1_RELEASE_NOTES.md).

## License

Apache-2.0. See [LICENSE](LICENSE). Runtime dependency notices are summarized in
[THIRD-PARTY-NOTICES.md](THIRD-PARTY-NOTICES.md).
