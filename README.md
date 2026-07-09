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

The v3.0.0 release keeps that boundary and adds defense-in-depth checks
around set-level artifact metadata, OpenAPI operation IDs, connector source URLs, local
structured source identities, query-rendered path/ID text, and workspace sample
evidence IDs. These checks protect generated-output and provenance boundaries; they are
not vulnerability scanning, complete secret discovery, security correctness proof, or a
guarantee that every sensitive value in a repository will be detected.

The v3.3.0 release keeps the same security boundary while adding package,
workspace-map, and adapter full-output regression coverage for the current generated
artifact and provenance contracts. It does not add security scanning, security proof,
release automation, network behavior, provider AI, or source upload.

The v3.4.0 release keeps the same security boundary while improving deterministic
query verification wording. It does not change generated artifact schemas, evidence
semantics, adapter behavior, query grammar, distribution channels, network behavior,
provider AI, or source upload.

See [SECURITY.md](SECURITY.md) for vulnerability reporting and
[docs/development/THREAT_MODEL.md](docs/development/THREAT_MODEL.md) for the public
product threat model and security limitations.

## Requirements

- Java 21.
- Apache Maven 3.x.

## Download

Published release artifacts are available on the
[GitHub Releases page](https://github.com/donDinDonDev/agent-project-memory/releases).

The latest published release is `v3.4.0`. Its release artifact is
`agent-project-memory-3.4.0.jar`; release assets use `SHA256SUMS` for optional checksum
verification.

```sh
shasum -a 256 -c SHA256SUMS
java -jar agent-project-memory-3.4.0.jar scan /path/to/java-spring-project
```

For published releases, the supported installation path is the executable jar from
GitHub Releases: download the jar, optionally verify `SHA256SUMS`, and run it with
`java -jar`. The public release line keeps this release-jar path as the minimum planned
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
target/agent-project-memory-3.4.0.jar
```

Local build examples below use the current v3.4.0 release version.

Release artifact and checksum verification expectations are documented in
[docs/development/RELEASE_PROCESS.md](docs/development/RELEASE_PROCESS.md).

## Quick Start

After `mvn package`, run a scan with the packaged CLI jar:

```sh
java -jar target/agent-project-memory-3.4.0.jar scan /path/to/java-spring-project
```

For a small fake input included in this repository, run:

```sh
java -jar target/agent-project-memory-3.4.0.jar scan examples/quickstart-demo
```

A supported scan writes `.project-memory/` inside the scanned repository. For a first
read, open these surfaces in this order:

1. `.project-memory/artifact-set.json` for the generated artifact inventory and
   evidence-authority labels.
2. `.project-memory/agent-guide.md` for deterministic human/agent orientation when the
   file is small enough for the task.
3. `.project-memory/endpoints.md` for API-surface review tasks.
4. `.project-memory/project-map.json` and `.project-memory/project-graph.json` only
   when structured fact or relation lookup is needed.
5. `.project-memory/evidence-index.jsonl` when an important generated fact needs
   source-backed verification.

For compact coding-agent handoff, give the agent `artifact-set.json` first and ask it to
respect the authority labels. For small outputs, add `agent-guide.md` and any
task-relevant Markdown such as `endpoints.md`. For larger outputs, prefer the read-only
`query <path> agent-context` command as the first compact context view, then use
targeted query commands for exact evidence, fact, symbol, relation, or impact lookup.
This recipe is a conservative reading order, not evidence of user outcomes.

Trust legend for generated output:

- Trust as source-backed evidence: records in `.project-memory/evidence-index.jsonl`
  and the repository source locations they reference.
- Treat as structured facts that must resolve to evidence for important decisions:
  `.project-memory/project-map.json`.
- Treat as navigation or presentation, not evidence: `artifact-set.json`,
  `project-graph.json`, `endpoints.md`, `agent-guide.md`, `query` output,
  `agent-context` output, `impact` output, profile Markdown, AI presentation artifacts,
  cache metadata, workspace output, adapter provenance, release metadata, and
  downstream coding-agent output.
- Verify important claims against `evidence-index.jsonl` and source files before using
  generated output to guide code changes.

The packaged CLI also supports help and version commands without scanning:

```sh
java -jar target/agent-project-memory-3.4.0.jar --help
java -jar target/agent-project-memory-3.4.0.jar help
java -jar target/agent-project-memory-3.4.0.jar scan --help
java -jar target/agent-project-memory-3.4.0.jar --version
java -jar target/agent-project-memory-3.4.0.jar version
```

Current builds also support opt-in local policy profile selection:

```sh
java -jar target/agent-project-memory-3.4.0.jar scan /path/to/java-spring-project --policy-profile guarded-local
java -jar target/agent-project-memory-3.4.0.jar scan /path/to/java-spring-project --policy-profile docs-focused
java -jar target/agent-project-memory-3.4.0.jar scan /path/to/java-spring-project --policy-profile adapter-local
```

Supported policy profiles are `guarded-local`, `docs-focused`, and `adapter-local`.
The same profile may also be selected with root-local config key
`policy_profile: <name>`. A scan without a policy profile keeps the compatibility
baseline and does not emit default policy metadata. When a policy profile is selected,
the scan records bounded execution metadata under `project-map.json` `scan.policy_profile`;
that metadata is not evidence and must not be treated as a security or compliance claim.
Selected profiles fail closed on unsafe combinations such as AI presentation with policy
profiles, adapter enablement outside `adapter-local`, document path-rule expansion under
`guarded-local`, and reserved generated-source or symlink modes.

Current builds also support opt-in agent profile artifact selection:

```sh
java -jar target/agent-project-memory-3.4.0.jar scan /path/to/java-spring-project --agent-profile codex
java -jar target/agent-project-memory-3.4.0.jar scan /path/to/java-spring-project --agent-profile all
```

Supported profile selectors are `codex`, `claude`, `cursor`, `generic`, and `all`.
`--agent-profile` may be repeated, and duplicate selectors are idempotent. Profile
selection is optional; a scan without `--agent-profile` keeps the default output set.
Profile selection writes a generated-profile manifest and selected deterministic profile
Markdown files under `.project-memory/agent-profiles/`. Profile Markdown is
generated only from existing structured project facts and existing evidence references;
it does not add project facts or evidence records.

Current builds also support explicitly enabled mock/no-network AI presentation
artifacts:

```sh
java -jar target/agent-project-memory-3.4.0.jar scan /path/to/java-spring-project --ai-presentation mock_no_network
```

Default scans do not create AI presentation artifacts. When enabled, the mock/no-network
slice writes `.project-memory/ai-presentations/manifest.json` and
`.project-memory/ai-presentations/brief.md` from the already generated
`project-map.json`, `evidence-index.jsonl`, and `project-graph.json` artifacts. These
files are non-authoritative presentation only: they do not add project facts, evidence
records, connector truth, security findings, runtime claims, source/spec agreement
claims, documentation-freshness claims, release evidence, or code-change authority. The
mock provider performs no network access, credential lookup, telemetry, source upload,
prompt logging, prompt transcript serialization, embeddings, vector search, repository
chat, or real provider calls. When combined with `--incremental`, the current AI
presentation slice runs a full scan and skips incremental cache metadata refresh.

Current builds also support opt-in incremental scan mode:

```sh
java -jar target/agent-project-memory-3.4.0.jar scan /path/to/java-spring-project --incremental
```

`--incremental` reuses the existing generated output set only after validating cache
schema, tool version, selected CLI options, selected config, selected agent profiles,
input fingerprints, and current generated output fingerprints. The first incremental
scan for a repository state is a cache miss: it runs the normal full analysis path and
refreshes metadata-only cache files under `.project-memory/cache/v1/` after successful
output generation. Missing, stale, unsafe, corrupted, or mismatched cache state fails
closed to normal full analysis. Scans without `--incremental` ignore persistent cache
state and do not read, write, delete, or trust cache files. For untrusted repositories
or pre-existing generated outputs, run a normal scan without `--incremental` first so
the output set is regenerated from current source inputs.

Current builds also include read-only query commands over existing
no-adapter generated artifacts:

```sh
java -jar target/agent-project-memory-3.4.0.jar query /path/to/java-spring-project list modules
java -jar target/agent-project-memory-3.4.0.jar query /path/to/java-spring-project list endpoints
java -jar target/agent-project-memory-3.4.0.jar query /path/to/java-spring-project list api-operations
java -jar target/agent-project-memory-3.4.0.jar query /path/to/java-spring-project list entities
java -jar target/agent-project-memory-3.4.0.jar query /path/to/java-spring-project list tests
java -jar target/agent-project-memory-3.4.0.jar query /path/to/java-spring-project explain evidence <evidence-id>
java -jar target/agent-project-memory-3.4.0.jar query /path/to/java-spring-project find fact <term>
java -jar target/agent-project-memory-3.4.0.jar query /path/to/java-spring-project find symbol <term>
java -jar target/agent-project-memory-3.4.0.jar query /path/to/java-spring-project relations <id>
java -jar target/agent-project-memory-3.4.0.jar query /path/to/java-spring-project relations <id> --direction incoming
java -jar target/agent-project-memory-3.4.0.jar query /path/to/java-spring-project agent-context
java -jar target/agent-project-memory-3.4.0.jar query /path/to/java-spring-project impact --files src/main/java/com/example/Foo.java
```

`query <path> ...` accepts either a repository directory containing
`.project-memory/` or the `.project-memory/` directory itself. These commands read the
existing `project-map.json` and `evidence-index.jsonl` artifacts, and read
`artifact-set.json` for set-level validation when the manifest is present. They print
deterministic human text and do not run scans, create `.project-memory/`, refresh
cache/profile artifacts, read repository source files, or write repository files.
Current query support remains focused on no-adapter `project-map.json`
`schema_version: "1.0"` artifact sets; adapter-enabled `schema_version: "2.0"` outputs
and `source-registry.json` are not query input sources in this slice. Legacy
no-manifest no-adapter artifact sets keep the earlier behavior: non-graph query
commands do not require or parse `project-graph.json`, and a missing or malformed graph
artifact is ignored unless the command needs graph-backed lookup. When
`artifact-set.json` is present, the reader validates the coherent generated set before
rendering query output and fails closed on unsupported manifest schema markers, mixed
manifest/file state, mixed artifact schemas, or stale optional artifacts such as a
`source-registry.json` that the manifest does not include. The migration action for
mixed or unsupported artifact sets is to regenerate the complete `.project-memory/`
output set from source and from the same explicitly configured local adapter exports,
not to edit or copy individual generated files in place. Source-visible endpoint rows
and spec-backed declared API operation rows stay separate; entity rows and embeddable
rows stay separate. Query artifact loading fails closed when evidence paths are unsafe,
project-map evidence references do not resolve to `evidence-index.jsonl`, or required
artifact files are unsafe local files.
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
is not included in the current build line.

`impact --files <changed-file> [...]` renders conservative direct mapping and one-hop
projection for explicit repository-relative changed-file paths over existing no-adapter
`project-map.json`, `evidence-index.jsonl`, and `project-graph.json` artifacts. It
reports direct matches to existing evidence paths, generated fact source references,
generated fact IDs, or graph nodes; one-hop `graph_neighbor` rows; tied
`relation_status` rows; low-confidence `planning_hint` rows from existing
`quality.change_risk_signals`; explicit `not_represented` rows for valid files with no
accepted artifact reference; and bounded diagnostics such as duplicate or capped
inputs. It does not parse raw diffs, inspect Git state, require changed files to exist,
read source files, refresh scans, create or mutate generated artifacts, write an impact
report, traverse graph neighbors beyond one hop, use adapter/source-registry context,
or claim complete runtime, call-graph, vulnerability, business-priority, or production
impact.

`agent-context` renders a bounded read-only context view for agent/editor consumption
over existing no-adapter `project-map.json` schema `1.0` and `evidence-index.jsonl`
artifacts, with optional valid `project-graph.json` navigation metadata when present.
The command writes deterministic stdout only; it does not create generated artifacts,
open referenced source files to expand evidence, parse generated Markdown, profiles,
AI presentation, cache metadata, adapter context, connector records, or
`source-registry.json` as fact inputs, and it does not add MCP/server/API/editor/plugin,
network, credential, telemetry, source-upload, repository-chat, semantic-search, or
automatic code-modification behavior. Agent-context output is navigation and
presentation only, not project evidence.

Current builds also include workspace map aggregation over explicitly configured local
member roots:

```sh
java -jar target/agent-project-memory-3.4.0.jar workspace scan /path/to/workspace/agent-project-memory-workspace.yml
```

The accepted workspace config shape is an explicit local YAML file:

```yaml
version: 1
members:
  - repo_id: orders
    root: services/orders
```

`workspace scan <config>` treats the config file directory as the workspace root,
requires each member to declare one unique logical `repo_id`, and accepts only
workspace-relative member roots such as `services/orders`. The command validates
config grammar and root safety, then writes the workspace-root
`.project-memory/workspace-map.json` from existing per-repo `.project-memory/`
artifacts. It does not run child repository scans, does not refresh or mutate member
`.project-memory/` directories, does not copy full member evidence records, does not
emit cross-repo relations, and does not add workspace query or workspace
`agent-context` behavior. Members with missing or invalid per-repo artifacts remain
listed with bounded workspace diagnostics. Member evidence navigation uses composite
`repo_id` plus existing per-repo `evidence_id` sample references.

CLI exit codes are stable for automation:

- `0`: success, help, or version.
- `1`: unexpected internal error.
- `2`: usage error, such as an unknown command, unknown flag, or malformed command.
- `3`: scan/query input or artifact error, such as a missing scan path, missing query
  path, missing directory, unsafe output path, missing query artifact, malformed
  query artifact, or invalid graph artifact for relation lookup.
- `4`: invalid scan or workspace config.
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
<path>/.project-memory/artifact-set.json
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

`artifact-set.json` is the deterministic set-level manifest for generated
single-repo scan output. It inventories the required base artifacts plus optional
adapter, profile, AI presentation, cache, and workspace-related surfaces without
making those surfaces evidence. It is contract/provenance metadata only, keeps
`artifact_root` relative as `.project-memory`, and does not serialize local absolute
paths, command transcripts, credentials, tokens, raw source bodies, or environment
values. Its artifact inventory marks only `evidence-index.jsonl` as authoritative
source-backed evidence; every other listed surface remains non-evidence. The initial
manifest uses `artifact_set_schema_version: "1.0"` and does not bump
`project-map.json` to `schema_version: "3.0"`.

`project-map.json` is the minimal stable machine-readable project map. No-adapter
current development output uses `schema_version: "1.0"` and includes redacted scan
metadata for safe root-local config selection, detected root `pom.xml` build metadata
when present,
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
- The v2.0 local structured import release keeps no-adapter scans v1-compatible:
  `project-map.json` remains on `schema_version: "1.0"` and
  `.project-memory/source-registry.json` is not emitted. When the local structured
  import adapter is explicitly enabled and an import file is accepted, the scan emits
  `.project-memory/source-registry.json` and uses `project-map.json`
  `schema_version: "2.0"` for adapter context. Treat that as a v2 artifact set:
  regenerate the base artifacts and source registry together, and do not mix
  `project-map.json`, `project-graph.json`, `evidence-index.jsonl`,
  `source-registry.json`, or generated Markdown from different scans.
- The v2.1 release also supports disabled-by-default GitHub/GitLab local
  export imports through `adapters.git_hosting_import`. Accepted records use
  `.project-memory/source-registry.json` schema `1.1` for Git hosting provenance and
  the existing `project-map.json` `schema_version: "2.0"` adapter context. The import
  remains local JSON only: no API calls, network access, credentials, adapter-aware
  query behavior, raw issue/PR/MR bodies, comments, review notes, configured import
  paths, or local absolute paths are serialized by default.
- The v2.2 release also supports disabled-by-default Jira/YouTrack/Confluence
  local export imports through `adapters.connector_import`.
  Accepted records use `.project-memory/source-registry.json` schema `1.2` for
  connector provenance and the existing `project-map.json` `schema_version: "2.0"`
  adapter context. The import remains local JSON only: no API calls, network access,
  credentials, adapter-aware query behavior, raw issue/page/article bodies, comments,
  rendered HTML, attachment details, configured import paths, or local absolute paths
  are serialized by default.
- The v3.0.0 release adds
  `.project-memory/artifact-set.json` with `artifact_set_schema_version: "1.0"` as a
  set-level generated-output manifest. No-adapter `project-map.json` remains on
  `schema_version: "1.0"`, adapter-enabled `project-map.json` remains on
  `schema_version: "2.0"`, and `evidence-index.jsonl` remains the source-backed
  evidence artifact. Current query, `agent-context`, and impact loading validate the
  manifest when it is present, accept only coherent no-adapter `schema_version: "1.0"`
  query input sets, and fail closed on unsupported or mixed artifact-set state.
- The v3.3.0 release keeps those generated artifact schemas and evidence semantics
  unchanged while strengthening package-phase, workspace-map, and local structured
  import full-output regression coverage for the current artifact and provenance
  boundaries.
- The v3.4.0 release keeps generated artifact schemas and evidence semantics
  unchanged while improving deterministic query presentation: evidence lookup names
  `evidence-index.jsonl` as the source-backed authority, evidence-bearing navigation
  outputs point readers to exact evidence lookup, and `agent-context` includes the
  path-first impact query in its compact verification loop.
- Downstream consumers that are not v2-adapter-aware should keep consuming no-adapter
  `schema_version: "1.0"` outputs. Consumers that encounter
  `schema_version: "2.0"` or `source-registry.json` should explicitly handle or reject
  adapter context and provenance joins instead of treating adapter-backed rows as
  Java/Spring facts. Current query support remains focused on no-adapter v1 artifact
  sets unless later release notes and architecture docs document adapter-aware query
  behavior.
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
.project-memory/artifact-set.json
.project-memory/project-map.json
.project-memory/project-graph.json
.project-memory/evidence-index.jsonl
.project-memory/endpoints.md
.project-memory/agent-guide.md
```

These files are meant to give humans and coding agents a compact, evidence-backed map
of the project plus a bounded relation graph: detected build layout, Spring MVC
endpoints, generated-source/codegen metadata, important components, structural graph
navigation, and references back to the source files that support each fact.

## Public Documentation Map

Start here:

- Contributor and coding-agent route: for implementation work, read
  [AGENTS.md](AGENTS.md) first, then this README and the task-relevant product,
  roadmap, output, and evidence docs below.
- Latest release:
  [docs/product/V3_4_RELEASE_NOTES.md](docs/product/V3_4_RELEASE_NOTES.md).
- Release history: [CHANGELOG.md](CHANGELOG.md). Detailed historical release notes live
  under `docs/product/`, and public evaluation summaries are linked from the relevant
  release notes.
- Narrow public evaluation summary:
  [v3.4 evidence-handoff and repeatability observations](docs/development/evaluations/v3.4-evidence-handoff-repeatability_SUMMARY.md).
- Generated-output example:
  [examples/stage3-project-map/README.md](examples/stage3-project-map/README.md).
- Runnable quickstart demo input:
  [examples/quickstart-demo/README.md](examples/quickstart-demo/README.md).
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
- Contributing, release process, and security:
  [CONTRIBUTING.md](CONTRIBUTING.md),
  [docs/development/RELEASE_PROCESS.md](docs/development/RELEASE_PROCESS.md), and
  [SECURITY.md](SECURITY.md).

## What This Is Not

`agent-project-memory` is not:

- a generic AI documentation generator,
- a repository chatbot,
- a RAG system,
- a SaaS product,
- a hosted codebase wiki,
- a tool that treats LLM output as the source of truth,
- an automatic code modification system.

AI is optional presentation, grouping, or summarization only; the core project memory
must come from deterministic analysis, explicit output contracts, and evidence
references. Any AI output must be labeled as non-evidence, must not create project facts
or security findings, and must not require source upload, network access, provider
credentials, repository chat, generic RAG, or automatic code modification by default.
The current product line includes only explicitly enabled mock/no-network AI
presentation plumbing and no real AI provider integration.

## Project Status

The latest published release is `v3.4.0`. The `v3.4.0` tag, GitHub Release,
executable jar, and `SHA256SUMS` assets are published. The supported public
distribution remains the GitHub Release executable jar plus `SHA256SUMS`; signing,
SBOM publication, package-manager channels, native images, container images, release
automation, and automatic publication are not included.

The v3.3.0 release added validation and regression hardening for package smoke,
workspace-map, and local structured import full-output coverage without changing
JSON/JSONL schema markers, evidence semantics, adapter behavior, query authority, or
the supported public distribution channel.

The v3.4.0 release improves query verification wording while keeping query output as
deterministic navigation and presentation, not evidence. It does not change generated
JSON/JSONL schema markers, evidence fields, evidence semantics, adapter behavior, query
grammar, or the supported public distribution channel.

The current implementation is a local Java 21 CLI for Java/Spring repositories. It
scans local source and configuration inputs, then writes deterministic `.project-memory/`
artifacts with explicit source-backed evidence. The current output set includes
`artifact-set.json`, `project-map.json`, `project-graph.json`, `evidence-index.jsonl`,
`endpoints.md`, and `agent-guide.md`, with optional local-only adapter, profile, AI
presentation, cache, and workspace surfaces when explicitly selected by supported
workflows.

The current compatibility baseline keeps normal no-adapter `project-map.json` output on
`schema_version: "1.0"`. Explicit adapter-enabled scans use
`schema_version: "2.0"` with `source-registry.json` provenance. The v3.0.0 release adds
the `.project-memory/artifact-set.json` manifest and manifest-present reader/query
validation without bumping `project-map.json` to `schema_version: "3.0"`, adding
adapter-aware query behavior, or changing evidence semantics.

For authoritative details, use the owner documents:

- Product scope and non-goals: [docs/product/MVP_SPEC.md](docs/product/MVP_SPEC.md) and
  [docs/product/NON_GOALS.md](docs/product/NON_GOALS.md).
- Current roadmap and release-track history:
  [docs/product/ROADMAP.md](docs/product/ROADMAP.md) and [CHANGELOG.md](CHANGELOG.md).
- Generated output semantics:
  [docs/architecture/OUTPUT_CONTRACT.md](docs/architecture/OUTPUT_CONTRACT.md).
- Evidence semantics:
  [docs/architecture/EVIDENCE_MODEL.md](docs/architecture/EVIDENCE_MODEL.md).
- Latest release scope, validation, and asset status:
  [docs/product/V3_4_RELEASE_NOTES.md](docs/product/V3_4_RELEASE_NOTES.md).

## License

Apache-2.0. See [LICENSE](LICENSE). Runtime dependency notices are summarized in
[THIRD-PARTY-NOTICES.md](THIRD-PARTY-NOTICES.md).
