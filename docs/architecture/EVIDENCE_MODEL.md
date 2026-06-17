# Evidence Model

Evidence is the basis for trust in `agent-project-memory`.

Every important generated fact should point back to one or more evidence references. A fact without evidence is either incomplete, inferred, uncertain, or out of scope for the deterministic project map.

## Evidence Reference

An evidence reference identifies the source that supports a generated fact.

Evidence may point to:

- a Java class,
- a Java method,
- an annotation,
- a Maven build file,
- a Gradle build file when v1.1 Gradle support is in scope,
- a Spring configuration file,
- a test file,
- a local Markdown document from local document ingestion,
- another explicit source reference.

## Evidence Types

Evidence types defined by the model:

- `code_symbol`: a class, method, field, enum, interface, or other Java symbol.
- `annotation`: an annotation on a class, method, field, parameter, or configuration element.
- `config_file`: a configuration file such as `application.yml`,
  `application.properties`, XML configuration, or legacy bounded filename-only
  OpenAPI/Swagger spec presence warning evidence.
- `build_file`: a build file such as `pom.xml`, or Gradle build files such as
  `settings.gradle`, `settings.gradle.kts`, `build.gradle`, or `build.gradle.kts`
  under the v1.1 Gradle support contract.
- `test_file`: a test source file or test resource.
- `api_spec`: a local OpenAPI/Swagger specification file, bounded version/kind
  observation, extracted operation evidence, or bounded operation parser status
  evidence. This evidence type was introduced by the v0.4 API surface implementation
  and is still emitted for local spec file facts, operation facts, and invalid or
  unsupported spec parser warnings.
- `path_signal`: a repository-relative file or directory path presence signal. This
  evidence type is emitted for v0.4 generated-source path warnings and is also available
  for other path-only signals that need evidence beyond a source file line.
- `document`: a local project document such as Markdown. This evidence type is emitted
  by the v0.8 local Markdown/document ingestion contract for accepted file, heading, and
  chunk observations, plus bounded document-side mention observations used only by
  uncertain reconciliation signals.

No adapter-specific evidence type is emitted in the current v1.x product line.

## Evidence Fields

Evidence entries use these fields:

- `id`: stable evidence identifier within one scan output.
- `source_type`: evidence type.
- `path`: repository-relative path to the source file.
- `class_name`: fully qualified class name when applicable.
- `method_name`: method name when applicable.
- `symbol_name`: symbol, annotation, property, file section, or document heading when applicable.
- `line_start`: first line supporting the evidence when known.
- `line_end`: last line supporting the evidence when known.
- `excerpt`: short source excerpt or normalized snippet.
- `confidence`: confidence label or score for the evidence reference.

Additional fields may be added later only when `OUTPUT_CONTRACT.md` and this document are updated.

The current implementation emits this field set in `evidence-index.jsonl`. Fields that are not
applicable to a source type are emitted as JSON `null`, not omitted. For example,
`build_file` evidence for `pom.xml` has `class_name` and `method_name` set to `null`.
When a line range is unavailable, `line_start` and `line_end` are `null`; when a repeated
value has no entries in `project-map.json`, it is emitted as an empty array.

Evidence paths must be normalized repository-relative paths. They must not be absolute,
must not start with `./`, must use slash separators in output, and must not escape the
scanned repository root. The root build file path is `pom.xml`; the scan root module path
may be represented as `"."` in `project-map.json`, but evidence paths do not use `"."` as
a file path.

## Planned Adapter And Connector Provenance

Future v2 adapters may introduce adapter-backed provenance, but the initial v2.0
evidence strategy is to keep adapter provenance outside `evidence-index.jsonl` in the
separate source registry documented by `OUTPUT_CONTRACT.md`. The initial v2.0 adapter
platform does not add an adapter-specific evidence type and does not reuse code evidence
categories for external records. Connector issues, pages, comments, tickets, exported
records, and API responses are not Java classes, annotations, build files, tests, or
repository config files.

The initial adapter-domain contract foundation preserves this evidence boundary. It
validates source-document and provenance identifiers for future accepted adapter
records, but it does not emit adapter evidence records, add adapter-specific evidence
fields, or treat adapter provenance as project evidence.

The v2.0 design uses a source-document envelope with provenance instead of embedding
connector metadata in free-form excerpts. Provenance should identify:

- adapter name and version;
- source type and source-system record ID where applicable;
- import mode, such as local export import or explicitly enabled API import;
- source URL or namespace when applicable;
- content hash;
- export, fetch, or import timestamp when known;
- whether the record came from a local repository file, an out-of-repository local
  export, or a remote API response;
- trust-boundary labels needed to keep external records distinct from repository source.

`document` evidence remains reserved for accepted local project documents handled by
the local Markdown/document ingestion contract. Adapter-backed records from local
structured exports or future connectors must reference `source_document_ids` and
`provenance_ids` from the source registry instead of `evidence_ids`, unless a later
contract explicitly introduces an adapter/external evidence type. This keeps
adapter-backed records provenance-backed external/document context rather than
authoritative evidence.

Adapter provenance can help users inspect where external context came from, but it does
not prove that an external service is current, reachable, complete, authoritative, or
aligned with repository source. A source-document ID or provenance ID is a deterministic
generated-output join key, not evidence for Java/Spring facts.

Any future change that treats adapter-backed records as evidence, reuses `document`
evidence for connector records, adds an adapter-specific evidence type, or adds
adapter-specific fields to `evidence-index.jsonl` requires synchronized updates to this
document, `OUTPUT_CONTRACT.md`, tests or goldens where applicable, the changelog,
release notes, and security review before implementation.

Adapter-backed evidence and provenance must not:

- create code-backed facts for Java/Spring endpoints, components, entities,
  repositories, tests, build metadata, or config semantics;
- imply that an external service is current, reachable, complete, or authoritative;
- store credentials, tokens, cookies, authorization headers, raw request/response logs,
  local absolute paths, or raw connector configuration values;
- treat connector summaries, query output, generated Markdown, cache metadata, graph
  derivation metadata, profile output, chat output, or LLM output as evidence.

## Planned Optional AI Presentation Evidence Decision

Future optional AI presentation output is not evidence. It must not add evidence types,
evidence fields, evidence records, confidence labels, evidence IDs, source references,
security findings, vulnerability proof, connector truth, runtime claims, source/spec
agreement claims, or code-modification authority.

AI presentation may reference existing evidence IDs, source-artifact names, graph IDs,
or future adapter provenance IDs to help humans navigate deterministic memory. Those
references are citations to existing deterministic material, not new evidence created
by the AI layer. An AI summary that cites an evidence ID must not strengthen, weaken,
replace, suppress, reinterpret, or fabricate the underlying fact or evidence record.

Allowed AI inputs for any future presentation surface are limited to deterministic
generated facts, existing evidence references, already serialized bounded excerpts,
and bounded non-evidence metadata such as graph derivation, cache/profile/query
metadata, and future adapter provenance accepted by deterministic adapter contracts.
AI must not read repository source files, generated-source contents, raw local document
bodies, connector credentials, raw connector request/response logs, or remote API
responses directly as evidence.

AI output must preserve the existing claim categories:

- code-backed source-visible facts remain code-backed facts;
- spec-backed declared operations remain spec-backed declared operations;
- local Markdown and future adapter-backed records remain document-backed or
  provenance-backed material;
- graph derivation, cache metadata, profile Markdown, query output, diagnostics, and AI
  output remain non-evidence;
- inferred relations, uncertain signals, warnings, and not-analyzed areas keep their
  confidence, uncertainty, status, and support labels.

Any future change that treats AI output, prompts, embeddings, vector-store entries,
provider responses, chat transcripts, generated Markdown, or AI presentation artifacts
as evidence must update this document and `OUTPUT_CONTRACT.md` before implementation.
Such a change would also need focused tests or goldens where applicable, a changelog
entry, release notes, and a separate security review. The preferred boundary is that AI
output remains permanently non-evidence.

## Fact Categories

### Extracted Facts

Extracted facts are directly observed in source files or documents.

In the current implementation, extracted facts come from root and child Maven build
files, supported Java production source roots, supported Java test roots, and
default-scope local Markdown document paths, ATX heading references, and bounded chunk
references, with resolving `document` evidence for accepted file, heading, chunk, and
bounded reconciliation mention observations.

Examples:

- A class annotated with `@RestController`.
- An interface annotated with a direct supported Spring stereotype such as `@Repository`.
- A method annotated with `@GetMapping("/orders/{id}")`.
- A source-visible Java interface method annotated with `@GetMapping("/orders/{id}")`
  when it is uniquely bound to a concrete controller handler under supported
  production source roots.
- A class annotated with direct `@Entity`.
- A field annotated with direct `@Id` or `@ManyToOne`.
- A field annotated with direct `@Id` on a source-visible `@MappedSuperclass` reached
  from an entity through a conservative mapped-superclass chain when that field is
  attached to the entity as a bounded mapped-superclass identifier fact.
- A Maven project with a root `pom.xml`.
- Filename-only presence of `openapi.yml`, `openapi.yaml`, `openapi.json`,
  `swagger.yml`, `swagger.yaml`, or `swagger.json` as a local OpenAPI/Swagger spec-file
  fact and as a legacy hidden HTTP surface warning signal.
- A root `pom.xml` Maven plugin declaration under `<build><plugins><plugin>` or
  `<build><pluginManagement><plugins><plugin>` with an exact artifact ID of
  `openapi-generator-maven-plugin` or `swagger-codegen-maven-plugin` as a hidden HTTP
  surface warning signal.
- A direct source-visible `@RepositoryRestResource` annotation as a hidden HTTP surface
  warning signal.
- Planned v0.3 source-visible Maven metadata, dependency, plugin, and generator signals
  extracted from local `pom.xml` files.
- Planned v0.3 resource-root and configuration-file presence facts, where configuration
  evidence is path-oriented and does not include configuration values.
- Planned v0.3 direct source-visible `@SpringBootApplication` application signals.
- Current v0.4 local OpenAPI/Swagger spec file facts and operation facts, where
  operation facts are spec-backed declared API facts rather than code-backed endpoint
  facts.
- Current v0.4 generated-source path signals, where path presence supports warnings and
  does not imply generated source contents.
- Current v0.5 direct source-visible `@Repository`, `@Configuration`,
  `@ConfigurationProperties`, `@Bean`, `@Transactional`, `@Scheduled`,
  `@EventListener`, and common Kafka/Rabbit listener annotation Spring application
  surface facts, plus Spring Security configuration warnings, where these observations
  support extracted facts or warning signals without runtime reconstruction.
- v0.6 source-visible JPA/domain annotations. The current implementation emits direct
  field-level `@Column`, `@Enumerated`, `@GeneratedValue`, and `@Version` evidence,
  direct class-level `@Embeddable` and `@IdClass` evidence, and direct field-level
  `@Embedded`, `@EmbeddedId`, `@ManyToOne`, `@OneToMany`, `@OneToOne`, `@ManyToMany`,
  `@JoinColumn`, and `@JoinTable` evidence where these observations support extracted
  source facts without database schema or runtime ORM claims.
- v0.7 source-visible test structure, including emitted test class declarations,
  supported directly visible test method annotations, direct JUnit/Spring Test framework
  signal observations, direct Spring test slice annotation facts, and conservative mock
  annotation signals. None of these observations prove test execution, coverage,
  assertion behavior, CI behavior, runtime Spring context behavior, Mockito behavior, or
  slice correctness.
- Current v0.8 local Markdown document inventory, deterministic ATX heading references,
  bounded chunk references, conservative document reconciliation signals, and resolving
  `document` evidence from the documented default discovery scope. These document facts
  and signals support document orientation and uncertain inspection only; they do not
  prove code structure, runtime behavior, API implementation, test coverage,
  configuration semantics, documentation completeness, stale documentation, or
  source/document agreement. Current output does not serialize document bodies.
- v1.1 Gradle build layout observations extract accepted Gradle root and project
  build-file presence, simple static settings include
  declarations, default Java source/test/resource root paths, and unsupported Gradle
  layout warnings. These observations support local build-layout orientation only; they
  do not prove Gradle execution, effective Gradle models, dependency resolution, plugin
  resolution, task graphs, generated-source graphs, Kotlin source facts, or runtime
  Spring behavior.

Extracted facts should use strong evidence references and high confidence.

Spring endpoint and component annotation evidence is emitted only when source-visible
syntax supports a Spring origin: a fully qualified annotation name in the supported
Spring package, or a simple annotation name with an explicit single-type import for the
supported Spring annotation, and only when that exact framework type is not declared by
scanned source. Unresolved simple-name annotations, wildcard-import-only annotations,
same-package/local fake annotations, source-declared fake framework annotations,
generated-source-only annotations, and classpath-only annotations are not
high-confidence Spring evidence and are skipped.

Direct JPA annotation evidence is emitted only when source-visible syntax supports a
supported `jakarta.persistence.*` or `javax.persistence.*` origin through an exact fully
qualified annotation name, explicit single-type import, or explicit non-static
`jakarta.persistence.*` or `javax.persistence.*` wildcard import for the existing
supported JPA annotation set, and that exact framework type is not declared by scanned
source. Wildcard JPA evidence is skipped when a conflicting explicit import,
same-package/local simple-name declaration, source-declared fake framework type,
unresolved simple name, unsupported wildcard import, generated-source-only signal, or
classpath-only signal prevents a high-confidence source-visible JPA origin.

Hidden HTTP surface `@RepositoryRestResource` annotation evidence and Spring Test
annotation/import evidence follow the same external-origin rule for their supported
Spring framework types. Source-declared fake framework FQCNs, unresolved simple names,
wildcard-only imports, and static-import-only references do not produce high-confidence
warning or `Spring Test` framework-signal evidence.

### v0.1 Emitted Evidence

The v0.1 implementation emits these evidence records:

- `build_file` for a root `pom.xml` when present. The evidence path is `pom.xml`,
  `symbol_name` is `pom.xml`, `class_name` and `method_name` are `null`, and confidence is
  `high`.
- `annotation` for extracted Spring MVC annotations that support endpoint facts, including
  controller stereotype annotations `@Controller` and `@RestController`, class-level
  `@RequestMapping`, method-level mapping annotations, `@PathVariable`, `@RequestParam`,
  and `@RequestBody`.
- `annotation` for direct supported Spring component stereotype annotations on Java class
  or interface declarations under supported production source roots. The v0.1
  implementation supports `@Component`, `@Service`, `@Repository`, `@Controller`,
  `@RestController`, and `@Configuration`.
  Component stereotype evidence uses `class_name` for the annotated type, `method_name`
  as `null`, `symbol_name` as the annotation symbol, the annotation line range, the
  annotation excerpt, and `high` confidence. When the same `@Controller` or
  `@RestController` annotation supports both endpoint and component facts, both facts
  reference the same evidence ID and `evidence-index.jsonl` emits a single record.
- `annotation` for direct v0.5 `@Repository` Spring application surface facts. When the
  same source-visible `@Repository` annotation also supports a component fact, both
  facts reference the same evidence ID and `evidence-index.jsonl` emits a single record.
- `code_symbol` for current v0.5 inferred Spring Data repository interface extension
  signals. The repository slice emits evidence for the source-visible interface
  declaration and for each supported direct
  `extends:<fully-qualified-spring-data-base-type>` observation that led to the signal.
- `annotation` for direct JPA annotations under supported production source roots.
  The v0.1 implementation supports class-level `@Entity`, class-level `@Table`,
  field-level `@Id`, and field-level relationship annotations `@ManyToOne`,
  `@OneToMany`, `@OneToOne`, and `@ManyToMany`. JPA annotation evidence uses
  `class_name` for the annotated type,
  `method_name` as `null`, `symbol_name` as the annotation symbol, the annotation line
  range, the annotation excerpt, and `high` confidence. Field-level JPA annotation
  evidence IDs include a `field:<field_name>` discriminator while preserving the global
  evidence field set.
- Test inventory evidence described in the v0.1 Test Evidence section below.
- Hidden HTTP surface warning evidence described in the v0.1 Warning Evidence section
  below.

The current v0.8 local Markdown discovery, structure, and reconciliation analyzers emit
`document` evidence records only for accepted local Markdown file, heading, chunk, and
bounded reconciliation mention observations. They do not emit `document` evidence for
connectors, generated guidance, coverage data, test execution results, behavioral
assertion analysis, or LLM output.

### v0.2 Maven Module Evidence

v0.2 module discovery reuses the existing evidence field set and the existing
`build_file` evidence type. No new global evidence fields are introduced for
module-aware Maven discovery.

Root `<modules>` declaration evidence:

- Each root `<module>` entry that affects module inventory or a Maven module warning
  should have `source_type: "build_file"` and `path: "pom.xml"`.
- A module declaration ordinal is the one-based document-order index of a root
  `<modules><module>` declaration. When evidence IDs need a collision discriminator,
  they should use the same zero-padded `decl:000001` style ordinal used by module warning
  IDs.
- `symbol_name` should be `module:<normalized_module_path>` when the module declaration
  normalizes to a valid repository-relative path.
- Invalid module declarations that cannot produce a valid module path should still use
  `build_file` evidence for the root POM line. Their `symbol_name` may use a bounded
  deterministic discriminator such as `module:<invalid>:decl:<ordinal>` and their
  excerpt should preserve a short normalized observation of the declaration text.
- `line_start` and `line_end` should point to the `<module>` declaration line when
  known. If an XML declaration spans lines and the exact range is available, the range
  should cover the declaration element.
- `excerpt` should be a short source excerpt or normalized snippet from the `<module>`
  declaration. It must not summarize the child module contents.
- Duplicate declarations should each keep evidence for the declaration that produced the
  warning. Duplicate warnings are emitted per duplicate declaration after the first
  declaration for a normalized module path, not once per normalized path. Evidence IDs
  should include the declaration ordinal when path, line range, and normalized module path
  would otherwise collide.

Child POM evidence:

- Each detected child POM used for module inventory should have `source_type:
  "build_file"` and a repository-relative `path` such as `services/orders/pom.xml`.
- `symbol_name` should be `pom.xml`, `class_name` and `method_name` should be `null`,
  and confidence should be `high`.
- Child POM evidence proves only that the POM file was present as a local build file. It
  does not prove effective POM contents, parent inheritance, dependency graphs, Maven
  profile activation, generated sources, or runtime behavior.

Module evidence supports only deterministic Maven module discovery from source-visible
root and child POM files. It does not require running Maven, resolving profiles,
reconstructing effective POMs, resolving dependencies, scanning generated sources by
default, or discovering Gradle projects.

v0.2 module warnings use this same evidence:

- `invalid_module_path`, `missing_child_pom`, and `duplicate_module_path` warnings should
  reference root `<module>` declaration evidence.
- `invalid_module_path` warnings should reference the evidence for the specific invalid,
  empty, or blank declaration and should not reuse evidence from another invalid
  declaration.
- `duplicate_module_path` warnings should reference the duplicate declaration evidence
  that was ignored, not only the first declaration for that normalized module path.
- `nested_module_declaration` warnings should reference child POM `build_file` evidence
  and, when available, evidence for the nested `<module>` declaration in that child POM.
- `unsupported_module` warnings should reference child POM evidence and any root
  declaration evidence that led to the module candidate.

### Current v0.3 Build And Configuration Evidence

v0.3 build/configuration analysis should reuse the existing evidence field set and the
existing evidence types. No new global evidence fields are introduced for the v0.3
build/configuration contract.

Maven metadata evidence:

- Direct source-visible Maven metadata such as `groupId`, `artifactId`, `version`,
  `packaging`, and parent coordinates should use `source_type: "build_file"`.
- Evidence paths should point to the module POM, such as `pom.xml` for the scan root or
  `services/orders/pom.xml` for a child module.
- `symbol_name` should identify the bounded Maven element or section, such as
  `maven:project:artifactId`, `maven:project:packaging`, or `maven:parent:version`.
- `line_start` and `line_end` should point to the XML element line or element range when
  known.
- `excerpt` may preserve a short normalized XML element observation for source-visible
  Maven metadata. This proves only the direct POM text. It does not prove Maven defaults,
  parent inheritance, profile activation, property resolution, effective POM values, or
  runtime build behavior.
- The current v0.3 implementation emits this metadata evidence where a module POM
  is present, plus dependency evidence for direct `<dependencies><dependency>`
  declarations, separate `<dependencyManagement><dependencies><dependency>` management
  declarations, plugin evidence for direct `<build><plugins><plugin>` declarations,
  separate `<build><pluginManagement><plugins><plugin>` management declarations,
  path-oriented `config_file` evidence for supported application/logging config-file
  presence, and `annotation` plus `code_symbol` evidence for direct source-visible
  Spring Boot application signals where supported production source roots are present.

Dependency evidence:

- Direct `<dependencies><dependency>` declarations should use `build_file` evidence for
  the dependency declaration and, when available, for directly declared coordinate,
  scope, optional, type, and classifier elements.
- Direct `<dependencyManagement><dependencies><dependency>` declarations should also use
  `build_file` evidence, but the emitted facts must remain management declarations, not
  active resolved dependencies.
- Dependency value evidence `symbol_name` values include the one-based declaration
  ordinal, such as `maven:dependency:000001:artifactId` or
  `maven:dependency_management:000001:version`.
- Evidence for dependency values supports only direct source-visible XML text. It does
  not prove resolved versions, inherited values, transitive dependencies, conflict
  mediation, active profiles, repository availability, or effective dependency graphs.
- Property references such as `${revision}` should remain source-visible
  `property_reference` values. v0.3 evidence does not resolve project properties.

Plugin and generator signal evidence:

- Direct `<build><plugins><plugin>` and
  `<build><pluginManagement><plugins><plugin>` declarations should use `build_file`
  evidence for plugin declarations and directly visible coordinates.
- Plugin execution IDs, phases, and goals may use `build_file` evidence when directly
  visible in the module POM. This evidence does not prove Maven lifecycle execution,
  inherited executions, default goals, or resolved plugin behavior.
- Bounded plugin configuration and generator signals should use `build_file` evidence
  with a `symbol_name` that identifies the element or signal, such as
  `maven:plugin:configuration:inputSpec`.
- Evidence excerpts for plugin configuration signals should use bounded normalized
  snippets that identify the signal. They must not include arbitrary nested plugin
  configuration values.
- OpenAPI/Swagger, annotation processor, and generated-source plugin evidence supports
  warnings only. It does not prove generated source contents, generated API operations,
  endpoint facts, or runtime behavior.
- The current v0.3 plugin analyzer emits plugin declaration and execution evidence
  excerpts as bounded declaration observations, not full `<plugin>` or `<execution>`
  source blocks, so arbitrary `<configuration>` values are not serialized through those
  evidence records.

Resource and config discovery evidence:

- Resource roots should be recorded in `project-map.json` as path inventory entries with
  empty evidence IDs in the v0.3 contract, following the existing
  source-root and test-root summary pattern. The existing evidence model does not define
  a directory evidence type.
- Spring application and logging configuration file presence should use `config_file`
  evidence with the repository-relative file path, nullable `class_name` and
  `method_name`, nullable line fields unless line ranges are known without reading file
  contents, and `confidence: "high"`.
- Config discovery evidence must be path-oriented. `excerpt` should be a bounded
  filename/path observation such as `config file detected: application.yml`.
- v0.3 config evidence must not include configuration file contents, property keys,
  property values, YAML node content, XML element content, environment placeholders,
  decrypted secrets, or config excerpts.
- Filename-derived profile names for files such as `application-prod.yml` are evidence
  for filename shape only. They do not prove active Spring profiles, runtime precedence,
  or effective configuration.

Spring Boot application evidence:

- Direct `@SpringBootApplication` application signals should use `annotation` evidence
  under supported production source roots when the annotation origin is visible as the
  supported Spring Boot annotation type and that exact type is not declared by scanned
  source.
- A source-visible Java `main` method used to strengthen the application signal should
  use `code_symbol` evidence for that method.
- This evidence supports only a direct source-visible application signal. It does not
  prove executable jar packaging, active profiles, auto-configuration behavior,
  component scanning result, deployment behavior, or actual process entrypoint behavior.

### v0.4 API Surface Evidence

v0.4 API surface analysis preserves the existing evidence field set while adding
`api_spec` evidence for local spec-file discovery, extracted operations, and bounded
operation parser warnings, plus `path_signal` evidence for generated-source path
warnings.

Spec-backed evidence:

- Local OpenAPI/Swagger spec file facts, extracted operation facts, and bounded
  operation parser status warnings use `source_type: "api_spec"`.
- `api_spec.path` must be the normalized repository-relative spec path. It must not be
  absolute, start with `./`, or escape the scanned repository root.
- Symlink path entries are not emitted as `api_spec` facts. A regular target file inside
  the repository can be emitted only through its own normalized repository-relative path.
- `api_spec` evidence IDs use
  `ev:<spec_path_key>:<line_range_key>:api_spec:<api_spec_symbol_key>`.
- `spec_path_key` and `api_spec_symbol_key` preserve case and slash separators, and
  use uppercase UTF-8 byte percent-encoding for `%`, `:`, whitespace, ASCII control
  characters, and any other character outside the bounded readable key set `A-Z`, `a-z`,
  `0-9`, `.`, `_`, `-`, `~`, `/`, `{`, and `}`.
- `line_range_key` should be `<line_start>-<line_end>` when both parser line values are
  stable and `unknown` when stable line mapping is unavailable.
- If parser output would otherwise create two `api_spec` evidence IDs with the same path,
  line range, and symbol key, the implementation must add a deterministic
  `decl:<zero-padded-ordinal>` suffix or degrade the duplicate condition to a warning.
- For spec-file presence or version evidence, `symbol_name` identifies the bounded spec
  observation, currently `openapi` or `swagger`.
- For operation evidence, `symbol_name` identifies the operation location, such as
  `operation:get:/orders/{id}`.
- For current spec-file evidence, `line_start` and `line_end` point to the directly
  visible OpenAPI or Swagger version signal when it is found in the bounded header
  window; otherwise both fields are `null` and the evidence still identifies the spec
  path and filename-derived spec kind. Operation evidence should use the most precise
  bounded parser line location available, and may use `null` line fields when stable line
  mapping is unavailable.
- `excerpt` must be bounded. For operation evidence, it should contain only a compact
  normalized operation observation, such as method, path, and bounded `operationId`.
  It must not serialize full schemas, examples, arbitrary descriptions, request/response
  payloads, or large YAML/JSON blocks.
- Current `api_spec` evidence supports local declared API input facts and
  `openapi_declared_operation` facts. Neither form proves Spring MVC implementation,
  generated source contents, runtime routing, service ownership, or source/spec
  agreement.
- Invalid or unsupported spec warnings should keep bounded evidence for the local spec
  path and parse/status observation without copying large file contents.

Generated-source path signal evidence:

- Generated-source root path warnings use `source_type: "path_signal"` in the v0.4
  generated-source signal contract. The v1.2 metadata-only generated-source root
  inventory reuses the same evidence type for bounded root path observations.
- `path_signal` evidence IDs for generated-source roots use
  `ev:<generated_source_path_key>:unknown:path_signal:generated_source_root_path_detected`.
  `<generated_source_path_key>` uses the same percent-encoded repository-relative path
  key rules as v0.4 spec paths.
- `path_signal.path` must be a normalized repository-relative path to the file or
  directory that produced the signal.
- `symbol_name` should identify the path signal, such as
  `generated_source_root_path_detected`.
- `line_start` and `line_end` should be `null` for directory/path presence evidence.
- `excerpt` should be a bounded path observation such as
  `generated source root detected: target/generated-sources/openapi`.
- `path_signal` evidence supports generated-source warning facts and v1.2
  metadata-only generated-source root inventory items only. It does not prove generated
  Java types, generated OpenAPI operations, generated endpoints, or runtime behavior.
- The default analyzer must not read generated source contents from generated-source
  roots. Any future generated-source scan mode must be explicit, non-default, and
  introduced with a separate output/evidence contract update.

API surface relation evidence:

- Source-visible Spring MVC endpoint facts remain code-backed by `annotation` and
  `code_symbol` evidence.
- OpenAPI operation facts are spec-backed by `api_spec` evidence.
- If a future relation connects a spec operation to a source-visible endpoint, the
  relation must preserve both spec evidence and code evidence and must label
  `support_type`, `confidence`, and `uncertainty`.
- Similar paths, operation names, controller names, or tags are not enough to convert a
  spec operation into an endpoint fact.
- LLM-generated text, generated Markdown guidance, release notes, and chat output are
  never evidence for API surface facts or relations.

### Current Staged v0.5 Spring Application Surface Evidence

v0.5 Spring application surface analysis preserves the existing evidence field set and
reuses existing evidence types. No new global evidence fields are added by the current
repository, configuration, behavior, and messaging signal slices.

Annotation-backed Spring surface evidence:

- Direct source-visible Spring annotations should use `source_type: "annotation"` when
  the annotation origin is visible as a supported external Spring framework type and
  that exact type is not declared by scanned source.
- Current annotation-backed facts include direct `@Repository`, direct
  `@Configuration`, direct `@ConfigurationProperties`, direct `@Bean`, direct
  `@Transactional`, direct `@Scheduled`, direct `@EventListener`, and common
  Kafka/Rabbit listener annotations. Current annotation-backed warnings include
  supported Spring Security configuration annotations and the `@Bean` annotation on
  source-visible `SecurityFilterChain` bean methods.
- Annotation evidence supports source-visible facts or warnings only. It does not prove
  runtime bean registration, autowiring, conditional activation, profile state,
  auto-configuration, transaction proxying, scheduler registration, event delivery,
  messaging topology, or security policy.

Spring Data repository interface signal evidence:

- Spring Data repository interface extension is an inferred signal, not a direct
  annotation fact. If the same source-visible interface also has a direct supported
  `@Repository` annotation, the direct annotation fact and inferred extension signal
  remain separate observations backed by their own evidence.
- The current signal preserves `code_symbol` evidence for the source-visible interface
  declaration and the source-visible extends/base-type observation that led to the
  inference. Supported Spring Data base types are
  `org.springframework.data.repository.Repository`,
  `org.springframework.data.repository.CrudRepository`,
  `org.springframework.data.repository.PagingAndSortingRepository`,
  `org.springframework.data.jpa.repository.JpaRepository`, and
  `org.springframework.data.mongodb.repository.MongoRepository` when visible through a
  fully qualified name or explicit single-type import.
- This repository-signal evidence does not by itself prove runtime repository
  registration, resolved generic entity type, query method behavior, database access, or
  repository-to-entity relation. In v0.6 it may also support a separate inferred
  repository/entity relation only when paired with a supported source-visible generic
  observation and exactly one emitted entity fact.

Configuration and bean evidence:

- Direct `@Configuration` class facts use `annotation` evidence. When the same
  source-visible `@Configuration` annotation also supports a component fact, both facts
  reference the same evidence ID and `evidence-index.jsonl` emits a single record.
- Direct `@ConfigurationProperties` facts use `annotation` evidence. The current
  implementation does not emit bounded source-visible `prefix` or `value` fields; any
  future addition of those annotation literals must be explicitly designed and tested.
- Configuration-properties evidence must not include configuration file contents,
  property keys, property values, YAML node content, XML element content, environment
  values, decrypted values, active profile state, runtime binding success, or
  secret-looking values.
- Direct `@Bean` method facts use `annotation` evidence for the annotation. This
  evidence does not prove an instantiated runtime bean, effective bean name, scope,
  lifecycle, proxy behavior, method return type as a runtime bean type, method
  parameters as dependencies, or dependency graph.

Behavior, messaging, and security evidence:

- `@Transactional`, `@Scheduled`, `@EventListener`, and messaging listener annotations
  support operational change-surface signals only.
- Current behavior/messaging evidence excerpts for these facts record the annotation
  symbol only, such as `@Transactional`, `@Scheduled`, `@EventListener`,
  `@KafkaListener`, or `@RabbitListener`. They do not serialize annotation attributes.
- Messaging listener evidence may prove annotation presence but must not prove runtime
  topic, queue, exchange, broker, binding, consumer group, delivery, or deployment
  behavior.
- Messaging listener evidence must not serialize destination-like annotation values such
  as topics, queues, exchanges, routing keys, or group IDs into generated outputs.
- Spring Security configuration evidence supports warnings only. It must not prove
  endpoint protection state, authentication behavior, authorization behavior, filter
  chain ordering, vulnerability, or security correctness.
- Security warning evidence may combine `annotation` evidence with `code_symbol`
  evidence, for example when a source-visible `@Bean` method returns
  `SecurityFilterChain`.
- LLM-generated text, generated Markdown guidance, release notes, and chat output are
  never evidence for Spring application surface facts, warnings, or relations.

### Current v0.6 JPA And Domain Evidence

The v0.6 JPA/domain model preserves the existing evidence field set and reuses existing
evidence types. No new global evidence fields or database evidence types are introduced
by the current JPA/domain contract.

Direct JPA annotation evidence:

- Direct source-visible JPA annotations continue to use `source_type: "annotation"`
  only when source-visible syntax supports a supported `jakarta.persistence.*` or
  `javax.persistence.*` origin through an exact fully qualified annotation name,
  explicit single-type import, or explicit non-static `jakarta.persistence.*` or
  `javax.persistence.*` wildcard import for the existing supported JPA annotation set,
  and that exact framework type is not declared by scanned source. Wildcard support
  remains per exact JPA type and is skipped for conflicting explicit imports,
  same-package/local simple-name declarations, unsupported wildcard imports,
  source-declared fake framework types, generated-source-only signals, and
  classpath-only signals.
- Current annotation-backed facts include class-level `@Entity`, `@Table`,
  `@Embeddable`, and `@IdClass`, plus field-level `@Id`, `@Column`, `@Enumerated`,
  `@GeneratedValue`, `@Version`, `@Embedded`, `@EmbeddedId`, `@ManyToOne`,
  `@OneToMany`, `@OneToOne`, `@ManyToMany`, `@JoinColumn`, and `@JoinTable`.
- Field-level JPA annotation evidence keeps using the existing field discriminator in
  evidence IDs, such as `:field:<field_name>`, while preserving the global evidence
  field set.
- If a future bounded implementation supports getter/property-access annotations, that
  evidence must identify the annotated method through `method_name` and a stable
  property or method discriminator. It must not be merged with field declaration
  evidence.
- JPA annotation evidence supports source-visible facts only. It does not prove runtime
  access strategy, schema generation, table or column existence, foreign keys, indexes,
  constraints, generated identifier behavior, optimistic-locking correctness, cascade
  behavior, fetch behavior, proxy behavior, or provider defaults.

Source-visible annotation attributes:

- v0.6 field, table, identifier, relationship, embedded, and join metadata may record
  bounded direct annotation attributes chosen by the output contract, such as
  `@Table` name/schema/catalog, `@Column` name/nullable/unique/length/precision/scale,
  `@Enumerated` value, `@GeneratedValue` strategy/generator, `mappedBy`, directly
  visible relationship attributes, and bounded `@JoinColumn` or `@JoinTable` names.
- Missing annotation attributes are not evidence for JPA defaults. Unsupported
  expressions, classpath-only values, provider defaults, and runtime metadata must not
  be converted into extracted values.
- Evidence excerpts for JPA annotations must remain bounded source excerpts. They should
  identify the annotation or a short normalized observation and must not serialize
  arbitrary DDL, migration content, generated schema, or database inspection output.

Embedded and identifier evidence:

- Direct `@Embeddable` class facts use class-level `annotation` evidence.
- Direct `@Embedded` and `@EmbeddedId` facts use field-level `annotation` evidence. The
  current implementation may pair that field-level evidence with the target
  `@Embeddable` class-level annotation evidence when the declared field type can be
  matched deterministically to a unique local embeddable in the same supported module.
  This supports only a source-visible target signal; unresolved or classpath-only
  targets remain `declared_type_only` with explicit uncertainty.
- Direct `@IdClass` facts use class-level `annotation` evidence and may preserve the
  source-visible type literal. This evidence supports only a composite-id signal; it
  does not prove field matching, equality semantics, serializability, generated keys,
  provider behavior, or database primary-key shape.
- Existing mapped-superclass identifier evidence remains conservative and continues to
  require field-level `@Id` evidence plus class-level `@MappedSuperclass` evidence on
  the declaring class.

Relationship evidence:

- Direct relationship annotations and relationship metadata annotations support
  extracted source-visible relationship facts.
- A future relationship target link to an emitted entity fact would be inferred from
  source-visible type observations and must be labeled as inferred. It must preserve
  evidence for the relationship annotation and the target entity evidence that led to
  the link. The current v0.6 implementation does not emit relationship target
  links.
- Ambiguous, unresolved, unsupported collection, wildcard, generated-source-only, or
  classpath-only relationship targets must remain uncertain rather than being emitted as
  resolved entity links.
- `mappedBy`, `optional`, `fetch`, `cascade`, `orphanRemoval`, `@JoinColumn`, and
  `@JoinTable` evidence supports source-visible orientation only. It does not prove ORM
  owning side correctness, foreign keys, join tables, database constraints, runtime
  cascade behavior, lazy/eager loading behavior, or provider-specific mapping semantics.

Repository/entity inferred relation evidence:

- A v0.6 repository/entity relation is an inferred relation attached to an inferred
  Spring Data repository interface signal when a supported source-visible generic entity
  type can be matched to exactly one emitted entity fact.
- The relation must preserve repository-side `code_symbol` evidence for the interface
  and supported Spring Data base-type/generic observation, plus target entity evidence
  such as the direct `@Entity` annotation evidence or a source-visible entity class
  `code_symbol` evidence record if the implementation emits one.
- This evidence does not prove runtime Spring Data repository registration, query method
  behavior, JPQL semantics, database access, transaction behavior, dependency graph
  reachability, complete type solving, or cross-module dependency correctness.
- Ambiguous, unsupported, raw, wildcard, nested, unresolved, classpath-only, and missing
  entity generic shapes must use explicit relation status or uncertainty instead of an
  inferred relation object.

Runtime database and ORM non-evidence:

- Database introspection output, runtime Hibernate metadata, generated DDL, migration
  tool interpretation, JPQL semantic parsing, runtime application behavior, and LLM
  output are not evidence for v0.6 JPA/domain facts or relations.
- Local migration files or SQL files may become future document or file facts only after
  an explicit contract update. They must not silently become database schema evidence in
  v0.6.

### Spring MVC Interface Mapping Evidence

Source-visible interface-declared endpoint facts reuse the existing evidence types. No
new global evidence fields are introduced for the v0.1 interface-mapping decision.

When an endpoint mapping is declared on a Java interface method, the emitted fact should
use:

- `annotation` evidence for the Spring MVC mapping annotations on the interface method,
  plus any source-visible class-level mapping annotations that are deterministically used
  to form the endpoint path or HTTP method semantics.
- `code_symbol` evidence for the source-visible interface method and the concrete
  controller handler or class declaration needed to support the unique binding.
- existing controller stereotype `annotation` evidence when the concrete handler class
  is emitted as the endpoint `controller_class`.

This evidence supports only a deterministic source-visible interface binding. It does
not prove generated-source mappings that are absent from supported source roots,
OpenAPI operations, Maven-generated APIs, runtime proxy behavior, or complete
Spring handler mapping reconstruction.

### JPA Mapped-Superclass Identifier Evidence

Mapped-superclass identifier facts reuse the existing `annotation` evidence shape. No
new global evidence fields are introduced.

When `project-map.json` emits an `identifier_fields` item with `source_kind` set to
`"mapped_superclass"`, its evidence IDs should include:

- annotation evidence for the field-level `@Id` declaration on the declaring class.
- annotation evidence for the direct class-level `@MappedSuperclass` declaration on the
  same declaring class.

This evidence supports only a conservative source-visible mapped-superclass identifier
fact. Superclass traversal is limited to fully qualified names, explicit single-type
imports, and same-package references under supported production source roots.
Unresolved, ambiguous, cyclic, wildcard-import-only, classpath-only, generated-source-only,
or otherwise non-source-visible hierarchy branches are skipped. This does not prove full
ORM inheritance reconstruction, classpath resolution, property-access mapping,
generated-value behavior, column mapping, schema generation, or runtime persistence
behavior.

### v0.1 Warning Evidence

The v0.1 hidden HTTP surface warning analyzer emits evidence only for bounded,
deterministic signals. Warning evidence supports `warnings.items`; it does not create
endpoint facts.

The emitted warning evidence records are:

- `config_file` for OpenAPI/Swagger spec file presence by filename only. The evidence
  path is the repository-relative spec path, `symbol_name` is the filename, `class_name`
  and `method_name` are `null`, `line_start` and `line_end` are `null`, and the excerpt is
  a bounded filename observation such as `filename detected: openapi.yml`. The analyzer
  does not parse OpenAPI/Swagger YAML content.
- `build_file` for deterministic OpenAPI/Swagger Maven code generation plugin
  declarations in the root `pom.xml`, under `<build><plugins><plugin>` or
  `<build><pluginManagement><plugins><plugin>`, with exact artifact IDs such as
  `openapi-generator-maven-plugin` or `swagger-codegen-maven-plugin`. The matching
  artifactId line is used as the evidence excerpt.
- `annotation` for direct source-visible `@RepositoryRestResource` annotations under
  supported production source roots when the annotation origin is visible as the
  supported Spring Data REST annotation type and that exact type is not declared by
  scanned source.

This evidence does not prove runtime Spring Data REST endpoints, generated OpenAPI
interfaces, generated source contents, or complete HTTP API coverage. It only proves that
the warning signal was visible in the scanned local sources.

### Inferred Relations

Inferred relations are derived from multiple extracted facts or conventions.

Examples:

- A test class named `OrderControllerTest` likely covers `OrderController`.
- A service injected into a controller is likely involved in handling that controller's endpoints.
- A source-visible interface extending a supported Spring Data repository base type is a
  likely repository signal, not a direct runtime repository fact.

Inferred relations must be marked as inferred and must preserve the evidence that led to the relation.
The v0.1 tests inventory used only naming-convention inferred relations for
`tested_subjects`; it did not use call graphs, assertions, runtime execution, or
coverage data. Current v0.7 tested-subject rows may also use bounded source-visible
imports, field types, and Spring test slice class literals when the test-side
observation and production-side source fact can both be evidenced. Such rows remain
tested-subject orientation hints or explicit statuses, not coverage or runtime execution
evidence.

### Uncertain Signals

Uncertain signals are observations that may be useful but should not be presented as facts.

Examples:

- A naming convention suggests a flow boundary, but no direct call or injection evidence was found.
- A Markdown document mentions a module name that does not clearly map to source code.
- A direct JPA relationship annotation declares a field type, but the analyzer has not
  performed symbol solving to resolve the relationship target class.

Uncertain signals should be labeled with lower confidence and should not be used as the sole basis for important generated claims.

### JPA Relationship Uncertainty

JPA relationship facts are extracted directly from field-level annotations. They
preserve the declared Java field type in `java_type`, but they do not claim a resolved
fully qualified target class. In current v0.6 output, every relationship target
therefore includes `target.target_resolution: "declared_type_only"` and
`target.uncertainty: "target_type_not_resolved"` until a future relationship
target-link implementation supports otherwise.

This is an extracted annotation fact with an explicitly uncertain target, not a full ORM
mapping. Current v0.6 output may preserve direct source-visible `mappedBy`,
`@JoinColumn`, `@JoinTable`, `optional`, `fetch`, `cascade`, and `orphanRemoval`
attributes, but those observations are metadata only. They do not prove relationship
target resolution, collection element types, ORM ownership correctness, foreign keys,
join tables, database constraints, runtime cascade behavior, lazy/eager loading
behavior, runtime proxies, persistence provider behavior, or database schema semantics.

### v0.1 Test Evidence

The v0.1 tests inventory emits only these additional evidence records:

- `test_file` for emitted test-like Java class declarations under supported Maven test
  roots. The evidence path points to `src/test/java/...`, `class_name` is the detected
  test class, `method_name` is `null`, `symbol_name` is the fully qualified class name,
  and confidence is `high`. Helper, support, or configuration declarations without clear
  test naming and without direct test-class marker annotations are not emitted as test
  classes.
- `code_symbol` for production class declarations under `src/main/java` when they are
  referenced by an inferred `tested_subjects` relation. This evidence supports the
  candidate production class side of the naming convention; it is not coverage evidence.
- `code_symbol` for directly visible imports that indicate supported test framework
  signals, such as JUnit Jupiter, JUnit 4, or Spring Test imports. Import evidence is
  attached only to top-level emitted test classes; nested emitted test classes use their
  own class or method annotation evidence so file-level imports are not repeated as
  nested-class signals.
- `annotation` for directly visible annotations that indicate supported test framework
  signals, such as JUnit `@Test` annotations when resolvable from imports or fully
  qualified annotation names, and direct Spring test annotations when resolvable to a
  supported external Spring Test origin that is not declared by scanned source.

Test evidence uses the same stable evidence field set as the rest of
`evidence-index.jsonl`; no new global evidence fields are introduced in v0.1.

### v0.1 Tested-Subject Relations

v0.1 infers likely tested subjects only from test class naming conventions. The
analyzer strips supported suffixes such as `Test`, `Tests`, or `IT` from the test class
simple name and matches the result against production class simple names under
`src/main/java`.

Every emitted tested-subject relation includes:

- `support_type: "inferred"`.
- `confidence: "medium"` when exactly one production class has the candidate simple name.
- `confidence: "low"` and `uncertainty: "ambiguous_subject_name"` when multiple
  production classes share the candidate simple name.
- Evidence IDs for the test class declaration and the candidate production class
  declaration.

These relations are orientation hints. They do not claim complete subject mapping, test
execution, code coverage, assertion behavior, or runtime verification.

### v0.7 Tests Inventory Evidence

The current v0.7 tests inventory refinement and Spring test slice/mock signal support
preserve the existing evidence field set. No new global evidence fields or evidence
types are introduced by this slice.

Test class and method evidence:

- Emitted test class declarations continue to use `source_type: "test_file"` for the
  class-level inventory fact. The evidence path points to the supported Maven test root,
  `class_name` is the detected test class, `method_name` is `null`, `symbol_name` is the
  fully qualified class name, and confidence is `high`.
- Emitted test method facts use directly visible `annotation` evidence for the supported
  test method annotation. `method_name` identifies the test method when applicable.
- Current supported test method annotations include JUnit Jupiter `@Test`,
  `@ParameterizedTest`, `@RepeatedTest`, `@TestFactory`, and `@TestTemplate`, plus JUnit
  4 `@Test`, only when source-visible syntax supports a trusted external origin through
  a fully qualified annotation name or explicit single-type import.
- Lifecycle, setup, teardown, helper, support, and configuration methods are not test
  method evidence in the current v0.7 tests inventory refinement unless a later bounded contract
  explicitly adds separate setup/teardown signal semantics.
- Test method evidence proves only source-visible test structure. It does not prove that
  a test ran, passed, failed, asserted behavior, covered a subject, or executes in CI.

Direct framework signal evidence:

- Emitted framework signals use direct source-visible import or annotation observations
  for JUnit Jupiter, JUnit 4, and supported Spring Test annotation origins. Import
  observations use `source_type: "code_symbol"` and annotation observations use
  `source_type: "annotation"`.
- Import observations are recorded only for explicit non-static single-type imports.
  Wildcard imports, static imports, and source-declared types that spoof supported
  framework fully qualified names are not trusted as framework signal evidence in this
  slice.
- Framework signal evidence proves only a source-visible framework classification. It
  does not prove runtime test engine execution, CI behavior, assertion behavior, runtime
  Spring context startup, bean graph contents, active profiles, MockMvc setup, database
  access, repository behavior, or slice correctness.

Spring test slice and mock annotation evidence:

- Supported Spring test slice annotations use `source_type: "annotation"` only when the
  annotation origin is visible as a supported `org.springframework.test.*` or
  `org.springframework.boot.test.*` type and that exact framework type is not declared
  by scanned source.
- Current supported Spring test slice annotations are `@SpringBootTest`,
  `@WebMvcTest`, `@DataJpaTest`, and `@ContextConfiguration`. Additional Spring test
  annotations may be added only with focused tests and a synchronized output/evidence
  contract update if semantics change.
- Supported mock annotation signals use `source_type: "annotation"` only when the
  annotation origin is visible as supported `org.springframework.boot.test.mock.mockito`
  `@MockBean` or `@SpyBean` and that exact framework type is not declared by scanned
  source.
- Bounded class-literal observations from supported Spring test slice annotations may be
  preserved in annotation evidence excerpts or referenced by a separate `code_symbol`
  observation when needed, but excerpts must remain short and must not serialize
  arbitrary annotation payloads, property values, profiles, environment values, or test
  configuration contents.
- Mock annotation target type declarations, mock reset settings, names, answers, and
  other attributes are not parsed into structured output in this slice. Such source text
  may appear only as bounded evidence excerpts.
- Spring test slice and mock annotation evidence proves only direct source-visible
  annotation presence. It does not prove runtime Spring context startup, bean graph
  contents, active profiles, MockMvc setup, database access, Spring bean override
  behavior, Mockito behavior, or slice correctness.
- Unresolved simple-name annotations, wildcard-import-only annotations,
  same-package/local fake annotations, source-declared fake framework annotations,
  generated-source-only annotations, classpath-only annotations, and static-import-only
  references do not produce high-confidence Spring test slice or mock annotation
  evidence.

Current tested-subject relation evidence:

- Naming-convention tested-subject relations continue to preserve evidence for the test
  class declaration and the candidate production class declaration.
- Import-based tested-subject relations preserve `code_symbol` evidence for the
  explicit non-static single-type production import and the matched production class
  declaration.
- Field-type tested-subject relations preserve `code_symbol` evidence for the direct
  test field type observation and the matched production class declaration.
- Spring test slice class-literal tested-subject relations preserve evidence for the
  test-side slice annotation and the production-side source fact that matched the class
  literal.
- Ambiguous, unsupported, unresolved, generated-source-only, classpath-only, or
  otherwise non-source-visible target shapes use explicit relation statuses or
  uncertainty instead of being presented as unique inferred relations.
- Status-only tested-subject rows such as no matching production class, unsupported
  field type, or no supported subject signal preserve the source-side evidence that led
  to the status. They do not fabricate absence evidence for missing source facts.
- Tested-subject relation evidence does not prove coverage, assertion behavior, runtime
  calls, dependency injection, request routing, repository behavior, database access, or
  CI execution.

Current quality and change-risk evidence:

- Test-gap and change-risk signals are inferred or uncertain planning hints derived from
  deterministic facts and inferred tested-subject relations. They are emitted under the
  top-level `quality` object and reuse evidence IDs from the underlying source-visible
  facts that produced the hint, such as endpoint annotations, Spring application surface
  annotations, JPA annotations, warning signals, repository/entity relation facts, and
  tested-subject relation evidence. They do not introduce a new evidence type.
- Absence-sensitive hints, such as an `endpoint_without_obvious_test`,
  `repository_without_obvious_test`, or `entity_without_obvious_test` signal, do not
  have direct absence evidence. Their evidence supports the subject fact being
  considered; the signal uncertainty states that it is limited to the bounded generated
  test inventory and supported relation rules.
- Quality and change-risk evidence must not include test execution logs, CI provider
  output, coverage reports, mutation testing reports, runtime application traces,
  database inspection output, or LLM-generated summaries unless a future contract adds a
  separate non-core evidence source. Such external or runtime material is not evidence
  for current deterministic quality or change-risk facts.
- A change-risk hint must not be treated as vulnerability evidence, production impact
  evidence, business priority evidence, or correctness evidence.
- Quality and change-risk signals must preserve cautious wording such as
  `no_obvious_test`, `planning_hint`, `uncertain_planning_hint`, or
  `warning_oriented_planning_hint`. They do not prove coverage, assertion behavior,
  test execution, CI status, runtime behavior, call graph reachability, production
  impact, vulnerability, business priority, correctness, or complete subject mapping.

### v0.8 Document Evidence

The v0.8 local Markdown/document evidence layer uses the existing evidence field set and
the `document` evidence type. It does not add global evidence fields. The current
implementation emits file, heading, chunk, and bounded mention evidence for accepted
default-scope local Markdown documents, subject to the aggregate local Markdown caps
documented in `OUTPUT_CONTRACT.md`.

Document evidence scope:

- `document` evidence is emitted only for local Markdown files accepted by the
  documented default discovery policy or by a future explicit include policy that
  preserves repository-root containment.
- Evidence paths must be normalized repository-relative paths. They must not be
  absolute, must not start with `./`, must use slash separators, and must not escape the
  scanned repository root after normalization.
- Default document discovery does not follow symlinked Markdown files or symlinked
  directories. A symlink path is not document evidence unless a future explicit,
  non-default contract defines safe symlink handling.
- `.project-memory/`, generated output paths, hidden paths, dependency directories,
  build outputs, maintainer-only paths, private/internal paths, and secret-like path
  segments are not default document evidence sources.
- The current aggregate caps bound emitted `document` evidence by accepted document
  count, accepted document bytes, heading references, chunk references, document-side
  reconciliation mention observations, and reconciliation output rows. Omitted
  documents, headings, chunks, or mentions do not create placeholder evidence records.
- Scan diagnostics for aggregate cap conditions are execution metadata under
  `project-map.json` `scan.diagnostics`. They are not evidence and their diagnostic IDs
  must not appear in `evidence_ids`.

Document evidence IDs:

- Document evidence IDs use
  `ev:<document_path_key>:<line_range_key>:document:<document_symbol_key>`.
- `<document_path_key>` uses the same percent-encoded repository-relative path key rules
  as path-backed API-spec evidence.
- `<line_range_key>` is `<line_start>-<line_end>` when stable line mapping is available
  and `unknown` when it is not.
- `<document_symbol_key>` identifies the bounded document observation, such as
  `file:<filename>`, `heading:<normalized-heading>`, `chunk:<zero-padded-ordinal>`, or
  `mention:<bounded-token-key>`. For heading observations, `<normalized-heading>` is
  the redaction-safe normalized heading key when the heading contains an obvious
  secret-looking value covered by the v1.7 redaction policy.
- Current heading evidence IDs include a deterministic `decl:<zero-padded-ordinal>`
  discriminator based on heading document order to keep IDs path-scoped and
  collision-safe.
- If duplicate headings, chunks, or mention observations would otherwise collide, the
  evidence ID must add a deterministic `decl:<zero-padded-ordinal>` suffix.

Document evidence field semantics:

- `source_type` is `"document"`.
- `path` is the normalized repository-relative Markdown path.
- `class_name` and `method_name` are `null`.
- `symbol_name` identifies the bounded file, heading, chunk, or mention observation. It
  must not contain full paragraphs or generated summaries.
- `line_start` and `line_end` point to the file observation, heading line, chunk range,
  or document-side mention line when stable. They are `null` only when a stable line
  location is unavailable. Current file-level path observations use the `unknown` line
  range key and null line fields because file presence has no stable source line.
- `confidence` is `"high"` for direct file, heading, and chunk observations from an
  accepted local Markdown file. It is `"low"` for document-side observations used only
  by uncertain reconciliation signals.
- `excerpt` must be bounded. File evidence may use a path observation such as
  `markdown file detected: README.md`. Heading evidence may use the normalized heading
  line. Chunk evidence should identify the chunk boundary or owning heading and should
  not copy the chunk body. Reconciliation mention evidence may contain the bounded
  matched token, such as an endpoint-like path or module-like token, but not the
  surrounding paragraph.

Document evidence does not support code facts:

- Document evidence supports document inventory, heading/chunk navigation, and
  document-backed reconciliation observations only.
- Document evidence must not be used as `code_symbol`, `annotation`, `build_file`,
  `config_file`, `test_file`, `api_spec`, or `path_signal` evidence.
- A document mention of an endpoint path, module name, entity name, repository name, or
  test subject does not create a source-backed project fact.
- Document evidence does not prove runtime behavior, Spring MVC routing, OpenAPI
  implementation, Maven behavior, configuration values, database schema, ORM behavior,
  test coverage, CI results, assertion behavior, security correctness, vulnerability,
  business priority, or source/document agreement.

Document reconciliation evidence:

- Document-only reconciliation signals preserve document evidence for the observed
  token and keep `confidence: "low"`.
- Source-only reconciliation signals reuse evidence from the source-backed fact being
  considered. They do not fabricate absence evidence for missing document mentions.
- Any reconciliation signal that compares a document observation with a source-backed
  fact must preserve evidence from both sides when both sides exist.
- Reconciliation evidence supports uncertain inspection hints only. It must not be
  treated as stale-document proof, completeness proof, documentation-quality scoring, or
  implementation truth.

### v0.9 Scan Config Evidence Decision

The v0.9 CLI/config contract does not add a tool-config evidence type, global evidence
fields, or `evidence-index.jsonl` records for the selected
`agent-project-memory.yml` scan config file.

The v0.9 scan config summary is execution metadata, not project evidence:

- It may record redacted effective scan policy under `project-map.json` `scan` metadata.
- It may record a normalized repository-relative selected config path when safe.
- It may record feature enablement, adapter enablement counts, path-policy status,
  counts, and bounded non-fatal diagnostic metadata.
- It must not record raw config values, raw user include/exclude patterns, config file
  contents, YAML nodes, adapter import paths, raw connector or export contents,
  environment variables, decrypted values, credentials, tokens, secret-looking values,
  source excerpts, document bodies, stack traces, local absolute paths, or generated
  output contents.

Existing `config_file` evidence remains reserved for project application/logging config
file presence and legacy filename-only OpenAPI/Swagger warning evidence. It must not be
reused for the tool's own scan config file unless a later contract explicitly changes
that evidence boundary.

Path-filter decisions are also not evidence. A user include or exclude rule may affect
which local Markdown files are discovered, but the rule itself does not prove a project
fact. Accepted local Markdown files still require normal `document` evidence, and
source-backed Java/Maven/API/test facts still require their existing source evidence.

### v1.0 Schema Marker Evidence Compatibility

The v1.0 `project-map.json` schema marker does not add evidence types, evidence fields,
evidence records, confidence labels, excerpt semantics, path semantics, or tool-config
evidence. Normal generated output with `schema_version: "1.0"` preserves the current
v0.9 evidence semantics, including the no-tool-config-evidence decision for
`agent-project-memory.yml` and the existing `document` evidence boundary for local
Markdown observations.

Any later v1.x evidence shape or semantic change must update this document,
`OUTPUT_CONTRACT.md`, focused tests or goldens, changelog entries, and release notes in
the same logical change.

### v1.1 Gradle Build File Evidence

The v1.1 Gradle support reuses the existing evidence field set and the existing
`build_file` evidence type. It does not add global evidence fields, confidence labels,
runtime evidence types, tool-config evidence, dependency evidence, task evidence, or
Kotlin source evidence.

Gradle build-file evidence scope:

- Accepted `settings.gradle`, `settings.gradle.kts`, `build.gradle`, and
  `build.gradle.kts` files should use `source_type: "build_file"` and normalized
  repository-relative `path` values.
- File-presence evidence should use `symbol_name` values such as `gradle:settings` for
  settings files and `gradle:build` for project build files. `class_name` and
  `method_name` should be `null`, and confidence should be `high`.
- Static Gradle include evidence should use `symbol_name` values shaped like
  `gradle:include:decl:<zero-padded-ordinal>` or another bounded deterministic
  declaration key. It should point to the line or line range containing the supported
  literal include declaration when stable line mapping is available.
- Evidence excerpts must be bounded source snippets. They may identify the accepted
  Gradle file or literal include declaration, but must not serialize whole build
  scripts, arbitrary plugin configuration, dependency blocks, task bodies, repository
  declarations, credentials, tokens, environment values, or generated output contents.
- If two Gradle evidence IDs would otherwise collide, the implementation must add a
  deterministic `decl:<zero-padded-ordinal>` discriminator or degrade the colliding
  observation to a warning.

Gradle evidence supports only deterministic local file and literal declaration
observations. It does not prove Gradle execution, effective project models, plugin
application, dependency resolution, repository availability, task graphs, generated
source contents, custom source-set semantics, Kotlin source structure, or runtime
Spring behavior.

Simple static `settings.gradle.kts` include declarations may be evidence only when the
supported string literals are directly visible in the settings file. Broader Kotlin DSL
semantic parsing, variables, loops, conditionals, function indirection, `includeBuild`,
and custom `projectDir` assignment semantics are out of scope for v1.1 evidence.

Static `sourceSets` declarations are not evidence for custom source roots in the v1.1
boundary. A directly visible `sourceSets` block may support a warning or
not-analyzed status when a later implementation records that limitation, but it must not
create custom source-root facts until a separate output/evidence contract explicitly
defines that behavior.

### v1.2 Generated Source And Codegen Evidence

The v1.2 generated-source/codegen policy is warning/config/metadata-only. It does not
add a generated-source content scan mode, global evidence fields, evidence types,
confidence labels, or tool-config evidence.

Generated-source metadata evidence:

- Generated-source root inventory uses existing `path_signal` evidence for bounded
  repository-relative path observations.
- Generator declarations and source-visible codegen signals use existing `build_file`
  evidence from Maven POMs or accepted Gradle build files when a directly visible build
  observation supports a warning or metadata row.
- `path_signal` evidence for a generated-source root proves only normalized local path
  presence. It does not prove generated Java types, generated endpoint handlers,
  generated OpenAPI operations, generated client SDKs, or runtime behavior.
- `build_file` evidence for generator declarations proves only directly visible build
  file text within the bounded existing evidence rules. It does not prove plugin
  execution, task execution, generated output freshness, dependency resolution, or
  generated source contents.
- The selected `agent-project-memory.yml` scan config remains execution metadata, not
  project evidence. Attempts to enable reserved generated-source content scanning do not
  create evidence because invalid config fails before output generation.

Generated-source content is not evidence in v1.2:

- The default analyzer must not read generated source contents from generated-source
  roots.
- No v1.2 evidence record may cite a line, symbol, annotation, class, method, test, or
  document body from generated-source content, because generated-source content is not
  scanned in the v1.2 boundary.
- Generated-source root inventory items use metadata-only origin/status fields in
  `project-map.json`, such as `source_origin: "metadata_only"` and
  `content_status: "not_scanned"`, rather than new evidence fields.
- Scan diagnostics for generated-source root caps or unsafe path skips are execution
  metadata under `scan.diagnostics`; diagnostic IDs must not appear in `evidence_ids`.

Future generated-source content scanning, if ever introduced, must update this document
and `OUTPUT_CONTRACT.md` before implementation. That later contract must define a
non-default mode, safe path policy, traversal and parsing caps, fact-level
generated-source labels, evidence semantics for generated files, Markdown rendering
rules, tests or goldens, evaluation, and risk-based security review. It must keep
generated-source-backed facts distinguishable from human-authored source facts,
spec-backed facts, document-backed hints, inferred relations, uncertain signals, and
metadata-only observations.

### v1.3 Agent Profile Presentation Evidence Decision

The v1.3 agent output profile layer does not add evidence types, evidence fields,
evidence records, confidence labels, excerpt semantics, path semantics, or tool-config
evidence.

Agent profile artifacts are generated presentations, not evidence:

- Profile Markdown files may reference existing evidence IDs and resolved evidence
  locations from `evidence-index.jsonl`.
- `agent-profiles/manifest.json` may identify which profile artifacts were generated,
  but the manifest is generated-output metadata only. It is not project evidence.
- Profile text, profile headings, profile-specific snippets, generated Markdown, copied
  instructions, downstream agent responses, release notes, chat output, and LLM output
  are not evidence for project facts or relations.
- Profiles must not create evidence for source-visible Java facts, OpenAPI operations,
  local Markdown documents, generated-source contents, runtime behavior, test coverage,
  security correctness, vulnerabilities, or business priority.
- A profile may make existing evidence easier to follow, but it must not turn
  document-backed hints, spec-backed declared operations, inferred relations, uncertain
  signals, generated-source metadata-only observations, warnings, or not-analyzed areas
  into stronger facts.

Any future profile feature that changes evidence shape or treats profile output as an
evidence source must update this document and `OUTPUT_CONTRACT.md` before
implementation.

### v1.4 Incremental Cache Evidence Decision

The v1.4 incremental cache layer does not add evidence types, evidence fields, evidence
records, confidence labels, excerpt semantics, path semantics, or tool-config evidence.
The current implementation writes opt-in cache metadata after successful full output
generation and may reuse the existing generated output set after strict whole-output-set
cache validation.

Cache state is execution metadata, not project evidence:

- Cache files under `.project-memory/cache/v1/` are not evidence sources.
- Cache manifests, input fingerprints, output fingerprints, cache hits, cache misses,
  invalidation decisions, output digests, cache corruption observations, unsafe-cache
  observations, generated Markdown, diagnostics, timing observations, downstream agent
  output, chat output, and LLM output are not evidence for project facts or relations.
- Cache metadata may help decide whether existing generated output files can be reused
  for an unchanged repository state, but it must not create, strengthen, replace,
  suppress, or weaken evidence for generated facts.
- A generated fact reused through a validated cache hit must still point to the same
  source-backed `evidence-index.jsonl` records that a full scan for the same repository
  state and selected options would emit.
- Cache metadata must not be referenced from `evidence_ids`, and diagnostic item IDs
  for cache miss, corruption, or unsafe-cache conditions must not appear in
  `evidence_ids`.

Allowed cache metadata remains non-evidence:

- Normalized repository-relative paths, `.project-memory`-relative output paths,
  SHA-256 hashes, byte counts, schema markers, tool-version matching metadata,
  canonical selected profile names, and redacted config/option matching status are
  cache validation inputs only.
- A file hash or output digest does not prove a Java symbol, annotation, endpoint,
  OpenAPI operation, document heading, test relation, configuration value, generated
  source content, runtime behavior, security correctness, vulnerability, business
  priority, or source/document agreement.
- The selected scan config remains execution metadata. Its path and hash may participate
  in cache invalidation, but the config file still does not create tool-config evidence.

Cache-sensitive-data boundaries:

- Cache metadata must not store source bodies, local document bodies, config contents,
  raw build-script bodies, generated-source contents, generated Markdown bodies, raw
  command logs, stack traces, raw include/exclude patterns, environment variables,
  decrypted values, credentials, tokens, secret-looking values, local absolute paths,
  downstream agent output, or LLM output.
- Generated-source roots remain metadata-only in the v1.4 cache boundary. Cache
  fingerprints must not read or cite files under generated-source roots unless a later
  explicit generated-source content scan contract updates this document and
  `OUTPUT_CONTRACT.md`.

Any future cache feature that treats cache entries, generated outputs, timing data,
diagnostics, or output digests as evidence must update this document and
`OUTPUT_CONTRACT.md` before implementation.

### v1.5 Relation Graph Evidence And Derivation Decision

The v1.5 relation graph does not add evidence types, evidence fields, evidence records,
confidence labels, excerpt semantics, path semantics, runtime evidence, or tool-config
evidence.

Graph state is generated navigation metadata over existing project-memory facts:

- `.project-memory/project-graph.json` is not an evidence source.
- Graph schema markers, node IDs, edge IDs, graph warnings, graph cap observations,
  graph derivation records, graph sorting, generated JSON, generated Markdown, cache
  fingerprints, downstream agent output, chat output, and LLM output are not evidence
  for project facts or relations.
- Graph nodes and edges may reference existing `evidence_ids` from `evidence-index.jsonl`
  when the underlying fact or relation already has evidence.
- Evidence records remain in `evidence-index.jsonl`; the initial graph contract does not
  duplicate evidence records as graph nodes.
- A graph node or edge must not strengthen, replace, suppress, or weaken evidence for
  an underlying generated fact. The underlying project-map fact and its evidence remain
  authoritative for the claim category already documented in this evidence model.

Structural graph edges may use derivation metadata instead of new evidence:

- Ownership and containment edges, such as module-to-fact, package-to-type, or
  document-to-heading edges, may be derived from existing `project-map.json` fields and
  graph node construction rules.
- Such derivation metadata identifies the source artifact, source section or field, and
  derivation kind. It is a reproducibility explanation for the graph edge, not an
  evidence record.
- Structural derivation must not be cited from `evidence_ids`, must not create
  `evidence-index.jsonl` records, and must not be used to prove runtime behavior,
  reachability, ownership semantics beyond current project-map fields, or source/spec
  agreement.

Inferred, uncertain, document-backed, spec-backed, warning, metadata-only, and
not-analyzed graph material keeps the same evidence boundaries as the source facts:

- Existing repository/entity and tested-subject relations keep their existing
  evidence IDs, relation statuses, support types, confidence, uncertainty, and bounded
  relation-row attributes such as relation type, candidate reference, target identity,
  and source-visible repository generic type.
- Document reconciliation graph edges remain low-confidence uncertain inspection hints.
  They do not prove stale documentation, missing documentation, source/document
  agreement, implementation truth, or code ownership.
- OpenAPI operation graph nodes remain spec-backed declared-operation facts. A graph
  connection involving a source-visible endpoint and a spec operation must not claim
  implementation agreement unless a later explicit relation contract adds separate
  evidence-backed semantics.
- Generated-source metadata graph nodes remain metadata-only and must not read or cite
  generated-source contents.
- Warning and not-analyzed graph records are orientation/status records only. They do
  not become extracted facts, inferred relations, runtime claims, coverage claims,
  security findings, vulnerabilities, or business-priority evidence.

Any future graph feature that introduces new evidence sources, emits evidence nodes as
first-class facts, changes confidence semantics, treats derivation as evidence, scans
generated-source contents, or adds runtime, connector, or AI-derived evidence must
update this document and `OUTPUT_CONTRACT.md` before implementation.

### v1.7 Redaction And Evidence Excerpt Safety

The v1.7 redaction layer does not add evidence types, evidence fields, evidence
records, confidence labels, path semantics, runtime evidence, or tool-config evidence.
It updates excerpt safety semantics for selected generated and rendered output strings.

Evidence excerpt redaction:

- Evidence `excerpt` remains a short source excerpt or normalized snippet, but
  v1.7-generated excerpts may contain the plain marker
  `[REDACTED_SECRET_LIKE_VALUE]` when a selected excerpt would otherwise include an
  obvious secret-looking value.
- The marker is a sanitized replacement inside the existing `excerpt` string. It is
  not a new evidence field, not an evidence type, not a confidence label, and not proof
  that the original value was an active credential.
- Redaction should preserve the evidence record's useful locator fields when they are
  not derived from selected free text: `id`, `source_type`, `path`, `class_name`,
  `method_name`, `symbol_name`, `line_start`, `line_end`, and `confidence`. Document
  heading evidence IDs and heading `symbol_name` values derive their heading key from
  redaction-safe heading text before percent-encoding so raw secret-looking heading
  values do not become evidence locators.
- Redaction should preserve useful safe context when possible, such as an annotation
  symbol, XML/YAML key, header name, element name, or delimiter. If preserving context
  would expose the value or make the boundary ambiguous, the selected excerpt may be
  replaced by the marker.
- Redaction should happen before final excerpt length bounding and output escaping, and
  the final emitted excerpt must remain bounded.

Secret-looking value boundary:

- The policy covers obvious credential-like key/value or header/value strings,
  obvious bearer/basic authorization values, and obvious private-key material markers
  only when such text has already been selected for generated or rendered output.
- The policy does not scan the whole repository for secrets, perform entropy-only
  detection, detect every unlabeled or split secret, validate credentials, classify
  secrets by provider, or prove that no secrets exist.
- The absence of a redaction marker is not evidence that an input contains no secrets.
- The presence of a redaction marker is not vulnerability evidence, security
  correctness evidence, credential validity evidence, or a secret inventory row.

Evidence shape decision:

- No new evidence field is required for the initial v1.7 redaction design.
- No `evidence-index.jsonl` schema migration is required by the marker alone.
- Existing facts should continue to reference evidence IDs normally after redaction.
- Graph, cache, profile, generated Markdown, diagnostics, and query output remain
  non-evidence even when they render existing evidence excerpts or redaction markers.
- Query render-time redaction over existing older artifact excerpts is a presentation
  boundary. It must not rewrite `evidence-index.jsonl` and must not create new evidence
  records.

Any future change that needs machine-readable redaction metadata, evidence-level
redaction reason fields, secret classifications, credential validation, or provider-
specific detection must update this document and `OUTPUT_CONTRACT.md` before
implementation. Such a change must still avoid presenting redaction as complete secret
detection or vulnerability proof.

## Evidence Discipline

- Do not fabricate evidence.
- Do not cite a file without a specific symbol, line range, excerpt, or documented reason when a more precise reference is available.
- Do not treat LLM output as evidence.
- Prefer exact source references over broad summaries.
- When line numbers are unavailable, make that explicit in the evidence entry and use the most precise available symbol reference.
