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
- a local Markdown document,
- another explicit source reference.

## Evidence Types

Initial evidence types:

- `code_symbol`: a class, method, field, enum, interface, or other Java symbol.
- `annotation`: an annotation on a class, method, field, parameter, or configuration element.
- `config_file`: a configuration file such as `application.yml`, `application.properties`, or XML configuration.
- `build_file`: a build file such as `pom.xml`.
- `test_file`: a test source file or test resource.
- `document`: a local project document such as Markdown.

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

## Fact Categories

### Extracted Facts

Extracted facts are directly observed in source files or documents.

Examples:

- A class annotated with `@RestController`.
- A method annotated with `@GetMapping("/orders/{id}")`.
- A source-visible Java interface method annotated with `@GetMapping("/orders/{id}")`
  when it is uniquely bound to a concrete controller handler under supported
  production source roots.
- A class annotated with direct `@Entity`.
- A field annotated with direct `@Id` or `@ManyToOne`.
- A field annotated with direct `@Id` on a directly source-visible
  `@MappedSuperclass` when that field is attached to an entity as a bounded
  mapped-superclass identifier fact.
- A Maven project with a root `pom.xml`.

Extracted facts should use strong evidence references and high confidence.

### Stage 6.1 Emitted Evidence

The Stage 6.1 implementation emits these evidence records:

- `build_file` for a root `pom.xml` when present. The evidence path is `pom.xml`,
  `symbol_name` is `pom.xml`, `class_name` and `method_name` are `null`, and confidence is
  `high`.
- `annotation` for extracted Spring MVC annotations that support endpoint facts, including
  controller stereotype annotations `@Controller` and `@RestController`, class-level
  `@RequestMapping`, method-level mapping annotations, `@PathVariable`, `@RequestParam`,
  and `@RequestBody`.
- `annotation` for direct supported Spring component stereotype annotations on Java class
  declarations under supported production source roots. Stage 5.1 supports `@Component`,
  `@Service`, `@Repository`, `@Controller`, `@RestController`, and `@Configuration`.
  Component stereotype evidence uses `class_name` for the annotated type, `method_name`
  as `null`, `symbol_name` as the annotation symbol, the annotation line range, the
  annotation excerpt, and `high` confidence. When the same `@Controller` or
  `@RestController` annotation supports both endpoint and component facts, both facts
  reference the same evidence ID and `evidence-index.jsonl` emits a single record.
- `annotation` for direct JPA annotations under supported production source roots.
  Stage 5.1 supports class-level `@Entity`, class-level `@Table`, field-level `@Id`,
  and field-level relationship annotations `@ManyToOne`, `@OneToMany`, `@OneToOne`, and
  `@ManyToMany`. JPA annotation evidence uses `class_name` for the annotated type,
  `method_name` as `null`, `symbol_name` as the annotation symbol, the annotation line
  range, the annotation excerpt, and `high` confidence. Field-level JPA annotation
  evidence IDs include a `field:<field_name>` discriminator while preserving the global
  evidence field set.
- Test inventory evidence described in the Stage 6.1 Test Evidence section below.

Stage 6.1 does not emit evidence records for Maven modules, connectors, generated
guidance, coverage data, test execution results, behavioral assertion analysis, or LLM
output.

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
OpenAPI YAML operations, Maven-generated APIs, runtime proxy behavior, or complete
Spring handler mapping reconstruction.

### JPA Mapped-Superclass Identifier Evidence

Mapped-superclass identifier facts reuse the existing `annotation` evidence shape. No
new global evidence fields are introduced.

When `project-map.json` emits an `identifier_fields` item with `source_kind` set to
`"mapped_superclass"`, its evidence IDs should include:

- annotation evidence for the field-level `@Id` declaration on the declaring class.
- annotation evidence for the direct class-level `@MappedSuperclass` declaration on the
  same declaring class.

This evidence supports only a direct, source-visible mapped-superclass identifier fact.
It does not prove full ORM inheritance reconstruction, multi-level hierarchy traversal,
classpath resolution, property-access mapping, generated-value behavior, column mapping,
schema generation, or runtime persistence behavior.

### Inferred Relations

Inferred relations are derived from multiple extracted facts or conventions.

Examples:

- A test class named `OrderControllerTest` likely covers `OrderController`.
- A service injected into a controller is likely involved in handling that controller's endpoints.

Inferred relations must be marked as inferred and must preserve the evidence that led to the relation.
Stage 6.1 tests inventory uses only naming-convention inferred relations for
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

Stage 5.1 JPA relationship facts are extracted directly from field-level annotations.
They preserve the declared Java field type in `java_type`, but they do not claim a
resolved fully qualified target class. Every relationship fact therefore includes
`target_resolution: "declared_type_only"` and
`uncertainty: "target_type_not_resolved"`.

This is an extracted annotation fact with an explicitly uncertain target, not a full ORM
mapping. The analyzer does not interpret `mappedBy`, `@JoinColumn`, cascade, fetch,
collection element types, runtime proxies, persistence provider behavior, or database
schema semantics in this stage.

### Stage 6.1 Test Evidence

The Stage 6.1 tests inventory emits only these additional evidence records:

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
  qualified annotation names, and direct Spring test annotations.

Test evidence uses the same stable evidence field set as the rest of
`evidence-index.jsonl`; no new global evidence fields are introduced in Stage 6.1.

### Stage 6.1 Tested-Subject Relations

Stage 6.1 infers likely tested subjects only from test class naming conventions. The
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
