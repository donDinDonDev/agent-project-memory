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
- a Spring configuration file,
- a test file,
- a local Markdown document from future document ingestion,
- another explicit source reference.

## Evidence Types

Evidence types defined by the model:

- `code_symbol`: a class, method, field, enum, interface, or other Java symbol.
- `annotation`: an annotation on a class, method, field, parameter, or configuration element.
- `config_file`: a configuration file such as `application.yml`,
  `application.properties`, XML configuration, or legacy bounded filename-only
  OpenAPI/Swagger spec presence warning evidence.
- `build_file`: a build file such as `pom.xml`.
- `test_file`: a test source file or test resource.
- `api_spec`: a local OpenAPI/Swagger specification file, bounded version/kind
  observation, extracted operation evidence, or bounded operation parser status
  evidence. This evidence type was introduced by the v0.4 API surface implementation
  and is still emitted for local spec file facts, operation facts, and invalid or
  unsupported spec parser warnings.
- `path_signal`: a repository-relative file or directory path presence signal. This
  evidence type is emitted for v0.4 generated-source path warnings and is also available
  for other path-only signals that need evidence beyond a source file line.
- `document`: a local project document such as Markdown. This evidence type is reserved
  for future document ingestion and is not emitted by the current implementation.

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

## Fact Categories

### Extracted Facts

Extracted facts are directly observed in source files or documents.

In the current implementation, extracted facts come from root and child Maven build
files, supported Java production source roots, and supported Java test roots. Local
Markdown or document ingestion is future work.

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
- Current v0.5 direct source-visible `@Repository` Spring application surface facts, and
  planned future v0.5 direct source-visible Spring application surface annotations such
  as `@Configuration`, `@ConfigurationProperties`, `@Bean`, `@Transactional`,
  `@Scheduled`, `@EventListener`, common messaging listener annotations, and Spring
  Security configuration annotations, where these observations support extracted facts
  or warning signals without runtime reconstruction.

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
qualified annotation name or explicit single-type import, and that exact framework type
is not declared by scanned source. The same source-declared-fake, unresolved, wildcard,
generated-source-only, and classpath-only cases are skipped rather than emitted as
high-confidence JPA evidence.

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

The analyzer does not emit evidence records for local Markdown/documents, connectors,
generated guidance, coverage data, test execution results, behavioral assertion
analysis, or LLM output.

### v0.2 Maven Module Evidence

v0.2 module discovery reuses the existing evidence field set and the existing
`build_file` evidence type. No new global evidence fields are planned for module-aware
Maven discovery.

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

### Planned v0.3 Build And Configuration Evidence

v0.3 build/configuration analysis should reuse the existing evidence field set and the
existing evidence types. No new global evidence fields are planned for the v0.3
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
- The current staged v0.3 implementation emits this metadata evidence where a module POM
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
- The current staged v0.3 plugin analyzer emits plugin declaration and execution evidence
  excerpts as bounded declaration observations, not full `<plugin>` or `<execution>`
  source blocks, so arbitrary `<configuration>` values are not serialized through those
  evidence records.

Resource and config discovery evidence:

- Resource roots should be recorded in `project-map.json` as path inventory entries with
  empty evidence IDs in the planned initial v0.3 contract, following the existing
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

- Generated-source root path warnings use `source_type: "path_signal"` in the current
  v0.4 generated-source signal contract.
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
- `path_signal` evidence supports warning facts only. It does not prove generated Java
  types, generated OpenAPI operations, generated endpoints, or runtime behavior.
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
repository signal slice.

Annotation-backed Spring surface evidence:

- Direct source-visible Spring annotations should use `source_type: "annotation"` when
  the annotation origin is visible as a supported external Spring framework type and
  that exact type is not declared by scanned source.
- Current annotation-backed facts include direct `@Repository`. Planned future
  annotation-backed facts include direct `@Configuration`, `@ConfigurationProperties`,
  `@Bean`, `@Transactional`, `@Scheduled`, `@EventListener`, common Kafka/Rabbit listener
  annotations, and supported Spring Security configuration annotations.
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
- This evidence does not prove runtime repository registration, resolved generic entity
  type, query method behavior, database access, or repository-to-entity relation.

Configuration and bean evidence:

- `@ConfigurationProperties` evidence may support a bounded source-visible annotation
  `prefix` or `value` observation if an implementation explicitly designs and tests that
  field.
- Configuration-properties evidence must not include configuration file contents,
  property keys, property values, YAML node content, XML element content, environment
  values, decrypted values, or secret-looking values.
- `@Bean` method evidence may use `annotation` evidence for the annotation and
  `code_symbol` evidence for the method when source-visible method context is needed.
  This evidence does not prove an instantiated runtime bean, effective bean name, scope,
  lifecycle, proxy behavior, or dependency graph.

Behavior, messaging, and security evidence:

- `@Transactional`, `@Scheduled`, `@EventListener`, and messaging listener annotations
  support operational change-surface signals only.
- Messaging listener evidence may prove annotation presence but must not prove runtime
  topic, queue, exchange, broker, binding, consumer group, delivery, or deployment
  behavior.
- Spring Security configuration evidence supports warnings only. It must not prove
  endpoint protection state, authentication behavior, authorization behavior, filter
  chain ordering, vulnerability, or security correctness.
- Security warning evidence may combine `annotation` evidence with `code_symbol`
  evidence, for example when a source-visible `@Bean` method returns
  `SecurityFilterChain`.
- LLM-generated text, generated Markdown guidance, release notes, and chat output are
  never evidence for Spring application surface facts, warnings, or relations.

### Spring MVC Interface Mapping Evidence

Source-visible interface-declared endpoint facts reuse the existing evidence types. No
new global evidence fields are introduced for `EVAL-8-004` decision B.

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
The v0.1 tests inventory uses only naming-convention inferred relations for
`tested_subjects`; it does not use call graphs, assertions, runtime execution, or coverage
data.

### Uncertain Signals

Uncertain signals are observations that may be useful but should not be presented as facts.

Examples:

- A naming convention suggests a flow boundary, but no direct call or injection evidence was found.
- A Markdown document mentions a module name that does not clearly map to source code.
- A direct JPA relationship annotation declares a field type, but the analyzer has not
  performed symbol solving to resolve the relationship target class.

Uncertain signals should be labeled with lower confidence and should not be used as the sole basis for important generated claims.

### JPA Relationship Uncertainty

v0.1 JPA relationship facts are extracted directly from field-level annotations.
They preserve the declared Java field type in `java_type`, but they do not claim a
resolved fully qualified target class. Every relationship fact therefore includes
`target_resolution: "declared_type_only"` and
`uncertainty: "target_type_not_resolved"`.

This is an extracted annotation fact with an explicitly uncertain target, not a full ORM
mapping. The analyzer does not interpret `mappedBy`, `@JoinColumn`, cascade, fetch,
collection element types, runtime proxies, persistence provider behavior, or database
schema semantics in this stage.

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

## Evidence Discipline

- Do not fabricate evidence.
- Do not cite a file without a specific symbol, line range, excerpt, or documented reason when a more precise reference is available.
- Do not treat LLM output as evidence.
- Prefer exact source references over broad summaries.
- When line numbers are unavailable, make that explicit in the evidence entry and use the most precise available symbol reference.
