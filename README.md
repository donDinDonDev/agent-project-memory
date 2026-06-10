# agent-project-memory

`agent-project-memory` is a local-first CLI/devtool for generating evidence-backed
project memory for Java/Spring codebases.

The goal is to help developers and AI coding agents understand a legacy Java/Spring
project before changing it. The tool scans local Java source, standard Maven layout,
standard Maven test roots, and bounded local API-surface inputs, extracts deterministic
facts, attaches evidence references, and writes Markdown/JSON artifacts that can be
reviewed, versioned, and reused.

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

Release artifacts are published on the
[GitHub Releases page](https://github.com/donDinDonDev/agent-project-memory/releases).

The `v0.7.0` release artifact is `agent-project-memory-0.7.0.jar`; release assets use
`SHA256SUMS` for optional checksum verification.

```sh
java -jar agent-project-memory-0.7.0.jar scan /path/to/java-spring-project
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
target/agent-project-memory-0.7.0.jar
```

## Quick Start

After `mvn package`, run a scan with the packaged CLI jar:

```sh
java -jar target/agent-project-memory-0.7.0.jar scan /path/to/java-spring-project
```

`scan <path>` validates that the path exists and is a directory, then creates or reuses:

```text
<path>/.project-memory/
```

Existing unrelated contents inside `.project-memory/` are preserved. Generated files are
rewritten deterministically when supported Maven module roots, source-visible Maven
metadata from module POMs, supported root source, test, or resource roots, supported
config files, local OpenAPI/Swagger spec files, safe default-scope local Markdown
documents, Spring repository signals, Spring configuration surface signals, Spring
behavior or messaging listener signals, or Maven module warnings are detected.

When the scanned path has a root `pom.xml`, the current implementation discovers the
scan root and root-declared Maven child modules, then runs the Spring MVC endpoint,
Spring component, Spring repository signal, Spring configuration surface, Spring
behavior/messaging signal, Spring Security configuration warning, JPA entity, hidden
HTTP surface warning, and tests inventory analyzers per supported module. For
compatibility with earlier local source-root scans, a
repository without a root `pom.xml` but with supported root source, test, or resource roots is
represented as the scan-root module with module discovery marked `not_detected`.

The analyzer extracts Spring MVC controllers and source-visible interface-declared
Spring MVC mappings that can be uniquely bound to concrete handlers, direct Spring
stereotype components on classes and interfaces, deterministic hidden HTTP surface
warnings, direct JPA entity annotations with conservative source-visible
mapped-superclass identifier fields, standard Maven test-root classes with conservative
helper filtering, direct source-visible Maven metadata from module POMs, and direct
source-visible Maven dependency and plugin declarations from module POMs, plus
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
inputs, extracts minimal spec-backed declared OpenAPI/Swagger operations, and discovers
safe default-scope local Markdown document inventory with deterministic ATX heading
references and bounded chunk references, with resolving document evidence for file,
heading, and chunk observations, without reconciliation or local-document guide
rendering, then writes:

```text
<path>/.project-memory/project-map.json
<path>/.project-memory/endpoints.md
<path>/.project-memory/evidence-index.jsonl
<path>/.project-memory/agent-guide.md
```

`project-map.json` is the minimal stable machine-readable project map. It currently uses
`schema_version: "0.8"` and includes detected root `pom.xml` build metadata when
present, Maven module inventory, module-owned source-visible Maven metadata under
`project.modules.items[].build_config.maven.metadata`, module-owned source-visible Maven
dependency inventory under `project.modules.items[].build_config.maven.dependencies` and
`dependency_management`, module-owned source-visible Maven plugin inventory under
`project.modules.items[].build_config.maven.plugins` and `plugin_management`, compatibility
source and test root summaries, module-owned standard resource-root inventory under
`project.modules.items[].build_config.resources`, module-owned path-only supported
application/logging config-file inventory under
`project.modules.items[].build_config.config_files`, module-owned direct source-visible
Spring Boot application signals under
`project.modules.items[].build_config.spring_boot_applications`, API surface categories
for source-visible endpoint facts, local OpenAPI/Swagger spec file facts under
`api_surface.openapi.spec_files`, minimal declared operation facts under
`api_surface.openapi.operations`, direct `module_id`
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
top-level `documents` object with deterministic default-scope local Markdown discovery
policy metadata, document inventory, ATX heading references, and bounded chunk
references, and evidence ID references. The current Spring
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

- v0.7 release summary: [docs/product/V0_7_RELEASE_NOTES.md](docs/product/V0_7_RELEASE_NOTES.md).
- v0.6 release summary: [docs/product/V0_6_RELEASE_NOTES.md](docs/product/V0_6_RELEASE_NOTES.md).
- v0.5 release summary: [docs/product/V0_5_RELEASE_NOTES.md](docs/product/V0_5_RELEASE_NOTES.md).
- v0.4 release summary: [docs/product/V0_4_RELEASE_NOTES.md](docs/product/V0_4_RELEASE_NOTES.md).
- v0.3 release summary: [docs/product/V0_3_RELEASE_NOTES.md](docs/product/V0_3_RELEASE_NOTES.md).
- v0.2 release summary: [docs/product/V0_2_RELEASE_NOTES.md](docs/product/V0_2_RELEASE_NOTES.md).
- v0.1 release summary: [docs/product/V0_1_RELEASE_NOTES.md](docs/product/V0_1_RELEASE_NOTES.md).
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

AI may become an optional presentation or summarization layer later, but the core project
memory must come from deterministic analysis, explicit output contracts, and evidence
references.

## Project Status

The v0.1 public release slice after Stage 8 evaluation is complete. The v0.2
module-aware Maven release is published with no remaining release-blocking security
findings from its final risk review. The v0.3 build/configuration release is published.
The v0.4 API surface release is published with packaged jar and checksum assets. The
v0.5 deeper Spring application surface release is published with packaged jar and
checksum assets after real-project evaluation and risk-based review completion. Future
connector/import work remains a later optional adapter track and is not started.
The v0.6 JPA/domain release is published with packaged jar and checksum assets after
implementation, real-project evaluation, follow-up fixes, release-prep documentation,
and risk-based review/compliance gates. It includes bounded v0.6 entity field annotation
metadata, embedded and identifier model signals, relationship metadata deepening,
conservative repository/entity inferred relations, safe JPA wildcard import support,
quieter no-domain guide rendering, and `schema_version: "0.6"`.
The v0.7 tests, quality, and change-risk release is published with packaged jar and
checksum assets after implementation, regression coverage, real-project evaluation,
release-prep validation, and a read-only security/contract audit. It moves normal
generated output to `schema_version: "0.7"` with bounded source-visible test method,
framework, Spring test slice, mock annotation, tested-subject relation/status, and
quality planning-hint output.

The current implementation includes a Java 21 Maven CLI, root-declared Maven module
discovery, JavaParser-backed Spring MVC endpoint extraction, source-visible interface
mapping support when uniquely bindable, stable `project-map.json` and
`evidence-index.jsonl` outputs, deterministic module-owned source-visible Maven
metadata, dependency, and plugin extraction, deterministic direct Spring component and
JPA entity inventories, deterministic path-only resource-root and supported config-file
discovery, deterministic hidden HTTP surface, generated-source, and Maven module
warnings, deterministic local OpenAPI/Swagger spec file discovery as declared API
inputs, minimal deterministic OpenAPI/Swagger operation extraction as spec-backed
declared operation facts, a minimal deterministic tests inventory, deterministic
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
safe path exclusions and no symlink following, deterministic `endpoints.md`, and
deterministic `agent-guide.md` generation from the
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
- Tests inventory is limited to test-like Java classes under supported standard Maven
  `src/test/java` roots; helper/support/configuration declarations without clear test
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
- `agent-guide.md` is generated from existing deterministic output facts only. It does not
  render local documentation content, summarize source files, infer architecture layers,
  or add claims beyond extracted facts, explicit inferences, and known uncertainty
  labels.
- Local Markdown/document ingestion is limited to conservative default-scope document
  inventory, deterministic ATX heading references, and bounded chunk references with
  `content_status: "not_serialized"` and resolving `document` evidence for file,
  heading, and chunk observations. It does not reconcile code and docs, render
  local-document guide sections, read hidden/private/generated/dependency/maintainer
  paths, follow symlinks, or summarize or serialize document bodies.
- `evidence-index.jsonl` currently contains root and child `pom.xml` `build_file`
  evidence when present, bounded source-visible Maven metadata, dependency, plugin, and
  module declaration `build_file` evidence, path-oriented `config_file` evidence,
  bounded Spring MVC endpoint, warning, component stereotype, JPA annotation, Spring
  Boot application, Spring repository stereotype and interface signal, local
  OpenAPI/Swagger `api_spec`, generated-source path `path_signal`, tests inventory
  evidence, and local Markdown `document` evidence for accepted file, heading, and chunk
  observations.
- The CLI uses only Java standard library argument handling.

For the concise v0.1 scope, evaluation summary, limitations, and validation surface, see
[docs/product/V0_1_RELEASE_NOTES.md](docs/product/V0_1_RELEASE_NOTES.md).

## License

Apache-2.0. See [LICENSE](LICENSE). Runtime dependency notices are summarized in
[THIRD-PARTY-NOTICES.md](THIRD-PARTY-NOTICES.md).
