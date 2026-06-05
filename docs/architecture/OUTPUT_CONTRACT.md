# Output Contract

This document defines the first `.project-memory/` output structure.

Any output field addition, removal, rename, or semantic change requires updating this file. Tests should be updated at the same time once implementation begins.

## Directory Structure

The current scan output uses the same four files introduced in v0.1:

```text
.project-memory/
  project-map.json
  evidence-index.jsonl
  endpoints.md
  agent-guide.md
```

In the current implementation, `scan <path>` writes all four files when supported Maven
module roots, supported root source or test roots, or Maven module warnings are
detected. Unsupported directories still only get a prepared `.project-memory/`
directory and do not get contract output files.

`EVAL-8-004` decision B keeps endpoint extraction limited to source-visible Java inputs
under supported production source roots, while adding uniquely bound interface-declared
Spring MVC mappings to the v0.1 endpoint semantics. It does not add Maven generation
during scans, default `target/generated-sources` scanning, OpenAPI YAML parsing,
generated API reconstruction, or Spring runtime handler mapping reconstruction.

## `project-map.json`

`project-map.json` is the machine-readable project memory file. The current public
contract is the staged v0.3 module-aware Maven metadata slice. The v0.1 single-module
shape below is kept as historical compatibility context for fields that later contracts
preserve.

The v0.1 baseline wrote this top-level object:

```json
{
  "schema_version": "0.1",
  "project": {
    "root": ".",
    "build": {
      "system": "maven",
      "root_build_file": "pom.xml",
      "evidence_ids": [
        "ev:pom.xml:1-1:build_file:pom.xml"
      ]
    },
    "source_roots": ["src/main/java"],
    "test_roots": ["src/test/java"]
  },
  "endpoints": [
    {
      "id": "endpoint:com.example.orders.OrderController#getOrder",
      "controller_class": "com.example.orders.OrderController",
      "handler_method": "getOrder",
      "http_methods": ["GET"],
      "http_method_semantics": "declared",
      "paths": ["/orders/{id}"],
      "request_parameters": [
        {
          "name": "id",
          "source": "path_variable",
          "java_type": "java.lang.Long",
          "evidence_ids": [
            "ev:src/main/java/com/example/orders/OrderController.java:21-21:com.example.orders.OrderController#getOrder:@PathVariable:parameter:0:id"
          ]
        }
      ],
      "request_body_type": null,
      "response_type": "com.example.orders.OrderDto",
      "mapping_source": {
        "kind": "direct_handler_method",
        "declaring_type": "com.example.orders.OrderController",
        "declaring_method": "getOrder",
        "binding": "direct",
        "uncertainty": null,
        "evidence_ids": [
          "ev:src/main/java/com/example/orders/OrderController.java:20-20:com.example.orders.OrderController#getOrder:@GetMapping"
        ]
      },
      "evidence_ids": [
        "ev:src/main/java/com/example/orders/OrderController.java:18-18:com.example.orders.OrderController:@RestController",
        "ev:src/main/java/com/example/orders/OrderController.java:20-20:com.example.orders.OrderController#getOrder:@GetMapping",
        "ev:src/main/java/com/example/orders/OrderController.java:21-21:com.example.orders.OrderController#getOrder:@PathVariable:parameter:0:id"
      ]
    }
  ],
  "warnings": {
    "analysis_status": "analyzed",
    "items": [
      {
        "id": "warning:hidden_http_surface:openapi_spec_file:src/main/resources/openapi.yml",
        "category": "hidden_http_surface",
        "signal": "openapi_spec_file",
        "message": "OpenAPI/Swagger spec file detected by filename only; v0.1 does not parse specs or reconstruct generated APIs.",
        "source_path": "src/main/resources/openapi.yml",
        "evidence_ids": [
          "ev:src/main/resources/openapi.yml:unknown:config_file:openapi.yml"
        ]
      }
    ]
  },
  "components": {
    "analysis_status": "analyzed",
    "items": [
      {
        "id": "component:com.example.orders.OrderService",
        "class_name": "com.example.orders.OrderService",
        "stereotypes": ["@Service"],
        "evidence_ids": [
          "ev:src/main/java/com/example/orders/OrderService.java:12-12:com.example.orders.OrderService:@Service"
        ]
      }
    ]
  },
  "entities": {
    "analysis_status": "analyzed",
    "items": [
      {
        "id": "entity:com.example.orders.Order",
        "class_name": "com.example.orders.Order",
        "table_name": "orders",
        "identifier_fields": [
          {
            "field_name": "id",
            "java_type": "Long",
            "declaring_class": "com.example.orders.Order",
            "source_kind": "declared",
            "evidence_ids": [
              "ev:src/main/java/com/example/orders/Order.java:16-16:com.example.orders.Order:@Id:field:id"
            ]
          }
        ],
        "relationships": [
          {
            "field_name": "customer",
            "annotation": "@ManyToOne",
            "java_type": "Customer",
            "target_resolution": "declared_type_only",
            "uncertainty": "target_type_not_resolved",
            "evidence_ids": [
              "ev:src/main/java/com/example/orders/Order.java:19-19:com.example.orders.Order:@ManyToOne:field:customer"
            ]
          }
        ],
        "evidence_ids": [
          "ev:src/main/java/com/example/orders/Order.java:12-12:com.example.orders.Order:@Entity",
          "ev:src/main/java/com/example/orders/Order.java:13-13:com.example.orders.Order:@Table"
        ]
      }
    ]
  },
  "tests": {
    "analysis_status": "analyzed",
    "items": [
      {
        "class_name": "com.example.orders.OrderControllerTest",
        "source_path": "src/test/java/com/example/orders/OrderControllerTest.java",
        "framework_signals": [
          {
            "name": "JUnit Jupiter",
            "evidence_ids": [
              "ev:src/test/java/com/example/orders/OrderControllerTest.java:3-3:com.example.orders.OrderControllerTest:import:org.junit.jupiter.api.Test",
              "ev:src/test/java/com/example/orders/OrderControllerTest.java:7-7:com.example.orders.OrderControllerTest#returnsOrder:@Test"
            ]
          }
        ],
        "tested_subjects": [
          {
            "class_name": "com.example.orders.OrderController",
            "support_type": "inferred",
            "confidence": "medium",
            "uncertainty": null,
            "evidence_ids": [
              "ev:src/test/java/com/example/orders/OrderControllerTest.java:5-5:com.example.orders.OrderControllerTest:test_file",
              "ev:src/main/java/com/example/orders/OrderController.java:18-18:com.example.orders.OrderController:code_symbol"
            ]
          }
        ],
        "evidence_ids": [
          "ev:src/test/java/com/example/orders/OrderControllerTest.java:5-5:com.example.orders.OrderControllerTest:test_file"
        ]
      }
    ]
  }
}
```

Field rules:

- `schema_version` is the string `"0.1"` for the v0.1 output contract slice.
- `project.root` is `"."` because the output is relative to the scanned repository root.
- `project.build.system` is `"maven"` when a root `pom.xml` file is detected and
  `"not_detected"` otherwise.
- `project.build.root_build_file` is `"pom.xml"` when detected and `null` otherwise.
- `project.build.evidence_ids` references `build_file` evidence for the root `pom.xml`
  when present and is an empty array otherwise.
- `project.source_roots` contains detected standard production source roots. The v0.1
  implementation supports `src/main/java`.
- `project.test_roots` contains detected standard test source roots. The v0.1
  implementation supports `src/test/java`.
- `endpoints` is sorted deterministically by first path, HTTP methods, method semantics,
  controller class, and handler method.
- `endpoint.id` is `endpoint:<controller_class>#<handler_method>` in this slice.
- `endpoint.controller_class` is always the concrete controller class that owns or
  implements the emitted handler, even when the mapping annotations are declared on a
  source-visible interface method.
- `endpoint.handler_method` is always the concrete handler method name. Interface-only
  declarations with no uniquely bindable concrete handler are not emitted as endpoints
  in this v0.1 slice.
- `http_methods` contains directly extracted methods when available. It is an empty array
  when the source did not declare a method or used an unsupported expression.
- `http_method_semantics` is one of `"declared"`, `"not_declared"`, or `"unsupported"`.
- `request_parameters` is an empty array when no supported request parameter annotations
  are detected.
- `request_body_type` is a Java type string when a supported `@RequestBody` parameter is
  detected and `null` otherwise.
- `response_type` is the declared Java return type when available.
- Endpoint and request-parameter `evidence_ids` must resolve to records in
  `evidence-index.jsonl`.

Endpoint mapping-source rules for `EVAL-8-004` decision B:

- Endpoint facts for this analyzer slice include a `mapping_source` object.
- `mapping_source.kind` is one of:
  - `"direct_handler_method"`: the Spring MVC method-level mapping annotation is declared
    directly on the concrete `controller_class` `handler_method`.
  - `"source_visible_interface_method"`: the Spring MVC method-level mapping annotation
    is declared on a Java interface method under a supported production source root such
    as `src/main/java`, and that interface method is uniquely bound to the concrete
    `controller_class` `handler_method`.
- `mapping_source.declaring_type` is the fully qualified class or interface that declares
  the method-level mapping annotation used for this endpoint fact.
- `mapping_source.declaring_method` is the method name on `declaring_type` that declares
  the method-level mapping annotation.
- `mapping_source.binding` is `"direct"` for direct handler method mappings and
  `"unique_implemented_interface_method"` for source-visible interface method mappings.
- `mapping_source.uncertainty` is `null` for emitted endpoint facts in this decision
  slice. Ambiguous interface bindings are skipped rather than emitted with an uncertain
  endpoint claim.
- `mapping_source.evidence_ids` references the evidence that supports where the mapping
  annotation was read from and, for interface mappings, the evidence that supports the
  unique source-visible binding. These IDs must resolve to records in
  `evidence-index.jsonl`.
- For a direct handler method mapping, `mapping_source.evidence_ids` should include the
  concrete method-level mapping annotation evidence and any directly used class-level
  controller mapping annotation evidence.
- For a source-visible interface method mapping, `mapping_source.evidence_ids` should
  include interface method mapping annotation evidence, relevant source-visible
  class-level mapping annotation evidence, and `code_symbol` evidence for the concrete
  handler/interface binding.
- Interface mapping support does not claim complete Spring runtime behavior. It does not
  run Maven generation, scan `target/generated-sources` by default, parse OpenAPI YAML,
  reconstruct generated APIs, resolve classpath-only interfaces, infer runtime proxies,
  or interpret unsupported Spring mapping conditions.
- Spring MVC endpoint annotations are trusted only when source-visible syntax supports a
  Spring origin: a fully qualified annotation name in the supported Spring package, or a
  simple annotation name with an explicit single-type import for the supported Spring
  annotation, and only when that exact framework type is not declared by scanned source.
  Unresolved simple-name annotations, wildcard-import-only annotations,
  same-package/local fake annotations, source-declared fake framework annotations,
  generated-source-only annotations, and classpath-only annotations are skipped rather
  than emitted as endpoint facts.
- `@RequestMapping(method = ...)` values are extracted only from supported Spring
  `RequestMethod` references visible as the exact fully qualified enum type or through
  an explicit single-type import. Bare enum constants, static-imported constants, local
  `RequestMethod` types, wildcard-import-only references, and source-declared fake
  `org.springframework.web.bind.annotation.RequestMethod` types produce
  `http_method_semantics: "unsupported"` rather than declared HTTP methods.
- Source-visible interface binding is established only from Java-visible source syntax:
  fully qualified implemented interface names, explicit single-type imports, or
  same-package interface names. Wildcard imports are not resolved in this v0.1 slice and
  are skipped rather than matched through a repository-wide simple-name fallback.
- If more than one source-visible interface method could bind to the same concrete
  handler, if the binding cannot be established from supported source roots, or if the
  interface is only present in generated or classpath sources outside the scan inputs,
  the interface-derived endpoint is skipped. If the concrete handler also has a direct
  handler method mapping, the direct endpoint may still be emitted with
  `mapping_source.kind: "direct_handler_method"`.
- `warnings.analysis_status` is `"analyzed"` when the supported `src/main/java` source
  root exists and the hidden HTTP surface warning analyzer runs.
- `warnings.items` contains deterministic warning signals that may indicate HTTP
  surfaces intentionally not expanded into endpoint facts. Warning items are sorted by
  `category`, `signal`, `source_path`, and `id`.
- `warning.id` is a stable string beginning with
  `warning:hidden_http_surface:<signal>:` in this slice.
- `warning.category` is `"hidden_http_surface"` for the current warning set.
- `warning.signal` is one of:
  - `"openapi_spec_file"`: a repository file has a supported OpenAPI/Swagger filename
    such as `openapi.yml`, `openapi.yaml`, `swagger.yml`, or `swagger.yaml`. Detection is
    by filename only and does not parse the file content.
  - `"maven_openapi_swagger_codegen_plugin"`: the root `pom.xml` contains a deterministic
    OpenAPI/Swagger Maven plugin declaration under `<build><plugins><plugin>` or
    `<build><pluginManagement><plugins><plugin>` with exact artifact ID
    `openapi-generator-maven-plugin` or `swagger-codegen-maven-plugin`. Comments,
    dependencies, properties, and arbitrary text do not produce this signal. Duplicate
    declarations of the same plugin artifact ID in one `pom.xml` emit one warning.
  - `"repository_rest_resource"`: a source-visible Java type under a supported
    production source root has a direct `@RepositoryRestResource` annotation whose
    origin is visible as `org.springframework.data.rest.core.annotation.RepositoryRestResource`
    through an exact fully qualified annotation name or explicit single-type import, and
    that exact framework type is not declared by scanned source.
- `warning.message` is a concise deterministic explanation of the limitation. It must
  not summarize the referenced source file or turn the signal into endpoint facts.
- `warning.source_path` is the repository-relative source path that produced the signal.
- `warning.evidence_ids` references the evidence that supports the warning and must
  resolve to records in `evidence-index.jsonl`.
- Warning signals do not create entries in `endpoints`; the analyzer must not parse
  OpenAPI YAML, run Maven generation, scan `target/generated-sources` by default, or
  reconstruct generated APIs from warning signals.

Example direct mapping source:

```json
{
  "kind": "direct_handler_method",
  "declaring_type": "com.example.orders.OrderController",
  "declaring_method": "getOrder",
  "binding": "direct",
  "uncertainty": null,
  "evidence_ids": [
    "ev:src/main/java/com/example/orders/OrderController.java:20-20:com.example.orders.OrderController#getOrder:@GetMapping"
  ]
}
```

Example source-visible interface mapping source:

```json
{
  "kind": "source_visible_interface_method",
  "declaring_type": "com.example.orders.OrdersApi",
  "declaring_method": "getOrder",
  "binding": "unique_implemented_interface_method",
  "uncertainty": null,
  "evidence_ids": [
    "ev:src/main/java/com/example/orders/OrdersApi.java:18-18:com.example.orders.OrdersApi#getOrder:@GetMapping",
    "ev:src/main/java/com/example/orders/OrderController.java:16-16:com.example.orders.OrderController:code_symbol"
  ]
}
```
- `components.analysis_status` is `"analyzed"` when the supported `src/main/java` source
  root exists and the direct component analyzer runs.
- `components.items` contains direct Spring stereotype component facts sorted
  deterministically by `class_name` and `id`.
- `component.id` is `component:<class_name>`.
- `component.class_name` is the fully qualified Java source type name when resolvable
  from the source file package and class or interface declaration. The field name remains
  `class_name` for v0.1 compatibility even when the component is an annotated interface.
- `component.stereotypes` contains directly present supported class-level annotation
  symbols with `@` on source-visible Java classes or interfaces. The v0.1 implementation
  supports `@Component`, `@Service`, `@Repository`, `@Controller`, `@RestController`, and
  `@Configuration`. It does not infer repository components from `extends JpaRepository`
  unless a supported stereotype annotation is directly present.
- Direct Spring component stereotypes are trusted only when source-visible syntax
  supports a Spring origin: a fully qualified annotation name in the supported Spring
  package, or a simple annotation name with an explicit single-type import for the
  supported Spring annotation, and only when that exact framework type is not declared
  by scanned source. Unresolved simple-name stereotypes, wildcard-import-only
  stereotypes, same-package/local fake stereotypes, source-declared fake framework
  stereotypes, generated-source-only stereotypes, and classpath-only stereotypes are
  skipped rather than emitted as component facts.
- `component.evidence_ids` references annotation evidence for the direct stereotype
  annotations and must resolve to records in `evidence-index.jsonl`.
- `entities.analysis_status` is `"analyzed"` when the supported `src/main/java` source
  root exists and the direct JPA entity analyzer runs.
- `entities.items` contains direct JPA entity facts sorted deterministically by
  `class_name` and `id`.
- `entity.id` is `entity:<class_name>`.
- `entity.class_name` is the fully qualified Java class name when resolvable from the
  source file package and class declaration.
- `entity.table_name` is the literal string from direct class-level
  `@Table(name = "...")` when present and deterministically extractable, otherwise
  `null`.
- Direct JPA annotations are trusted only when source-visible syntax supports a
  supported JPA origin: a fully qualified `jakarta.persistence.*` or `javax.persistence.*`
  annotation name, or a simple annotation name with an explicit single-type import for a
  supported JPA annotation, and only when that exact framework type is not declared by
  scanned source. Unresolved simple-name annotations, wildcard-import-only annotations,
  same-package/local fake annotations, source-declared fake framework annotations,
  generated-source-only annotations, and classpath-only annotations are skipped rather
  than emitted as entity, identifier, table, mapped-superclass, or relationship facts.
- `entity.identifier_fields` contains field-level `@Id` facts declared directly on the
  entity class or declared on a conservative source-visible superclass chain where each
  traversed superclass is present under supported production source roots and has a
  direct class-level `@MappedSuperclass` annotation. Identifier fields are sorted
  deterministically by `source_kind`, `declaring_class`, `field_name`, and `java_type`.
- Mapped-superclass support resolves superclass references only through fully qualified
  names, explicit single-type imports, or the same package. Unresolved, ambiguous,
  cyclic, wildcard-import-only, classpath-only, generated-source-only, or otherwise
  non-source-visible hierarchy branches are skipped. This does not imply full ORM
  inheritance reconstruction, classpath solving, `@Inheritance` handling, property-access
  mapping, embedded IDs, generated-value semantics, column or join-column analysis,
  repository analysis, schema generation, or runtime ORM behavior.
- `identifier_field.field_name` is the declared Java field name.
- `identifier_field.java_type` is the declared Java field type string.
- `identifier_field.declaring_class` is the fully qualified Java class that declares the
  identifier field.
- `identifier_field.source_kind` is one of:
  - `"declared"`: the field is declared directly on the entity class.
  - `"mapped_superclass"`: the field is declared on a directly source-visible class
    annotated with `@MappedSuperclass`.
- `identifier_field.evidence_ids` references field-level `@Id` annotation evidence and
  must resolve to records in `evidence-index.jsonl`. For identifier fields with
  `source_kind` set to `"mapped_superclass"`, it must also reference class-level
  `@MappedSuperclass` annotation evidence for `declaring_class`.
- `entity.relationships` contains field-level direct JPA relationship annotation facts
  sorted by `field_name`, `annotation`, and `java_type`. The v0.1 implementation
  supports `@ManyToOne`, `@OneToMany`, `@OneToOne`, and `@ManyToMany`.
- `relationship.field_name` is the declared Java field name.
- `relationship.annotation` is the direct relationship annotation symbol with `@`.
- `relationship.java_type` is the declared Java field type string. It is not a resolved
  target class.
- `relationship.target_resolution` is `"declared_type_only"` in v0.1.
- `relationship.uncertainty` is `"target_type_not_resolved"` in v0.1.
- `relationship.evidence_ids` references field-level relationship annotation evidence
  and must resolve to records in `evidence-index.jsonl`.
- `entity.evidence_ids` references class-level direct `@Entity` evidence and direct
  `@Table` evidence when present. These IDs must resolve to records in
  `evidence-index.jsonl`.
- `tests.analysis_status` is `"analyzed"` when the supported `src/test/java` source root
  exists and the tests inventory analyzer runs. It is `"not_detected"` when no supported
  test root is present in the current single-module scan.
- `tests.items` contains Java class declarations under supported test roots that look
  like test classes, sorted deterministically by `class_name` and `source_path`.
  Interfaces are not emitted. A declaration is emitted when it has a supported test
  suffix such as `Test`, `Tests`, or `IT`, or when it has directly visible test-class
  marker annotations on the class or its methods, such as JUnit `@Test`, JUnit
  `@Nested`, JUnit 4 `@RunWith`, or Spring test context annotations such as
  `@SpringBootTest`, `@WebMvcTest`, `@DataJpaTest`, and `@ContextConfiguration`.
  Helper, support, or configuration declarations without clear test naming and without
  direct test-class marker annotations are omitted, including nested helper/configuration
  declarations inside otherwise valid test files.
- `test.class_name` is the fully qualified Java class name when resolvable from the
  source file package and class declaration.
- `test.source_path` is the repository-relative Java source path.
- `test.framework_signals` contains only directly visible framework signals from imports
  or annotations in the test source file for emitted test classes. The v0.1 implementation
  emits signal names `"JUnit Jupiter"`, `"JUnit 4"`, and `"Spring Test"` when detectable.
  It is empty when no supported direct signal is visible. Source-file-level import
  evidence is attached only to top-level emitted test classes; nested emitted test
  classes use their own class or method annotation evidence so imports are not repeated
  as nested-class signals.
- Spring Test signals are trusted only when the annotation origin is visible as a
  supported `org.springframework.test.*` or `org.springframework.boot.test.*` type
  through an exact fully qualified annotation name or explicit single-type import, and
  that exact framework type is not declared by scanned source. Unresolved simple-name
  annotations, wildcard-import-only annotations, same-package/local fake annotations,
  source-declared fake framework annotations, and static-import-only references do not
  emit `Spring Test` framework signals.
- `framework_signal.name` is the detected framework family name.
- `framework_signal.evidence_ids` references direct import or annotation evidence and
  must resolve to records in `evidence-index.jsonl`.
- `test.tested_subjects` contains only naming-convention relations inferred by stripping
  supported test suffixes such as `Test`, `Tests`, or `IT` and matching the resulting
  simple name against production classes under `src/main/java`. It is empty when no
  production class match is found.
- `tested_subject.class_name` is a candidate production class name.
- `tested_subject.support_type` is `"inferred"` for v0.1 naming-convention
  relations.
- `tested_subject.confidence` is `"medium"` for a single naming-convention production
  class match and `"low"` for duplicate or ambiguous production class matches.
- `tested_subject.uncertainty` is `null` for a single naming-convention match and
  `"ambiguous_subject_name"` when multiple production classes share the candidate simple
  name.
- `tested_subject.evidence_ids` references the test class evidence and candidate
  production class evidence that led to the inferred relation. These IDs must resolve to
  records in `evidence-index.jsonl`.
- `test.evidence_ids` references direct test class evidence and must resolve to records
  in `evidence-index.jsonl`.
- The tests inventory does not claim code coverage, test execution results, direct
  behavioral assertion analysis, call graph resolution, or complete subject mapping.

### v0.2 Module-Aware Maven Contract

This section defines the current public v0.2 module-aware Maven JSON contract.

The v0.2 module-aware contract uses:

- `schema_version: "0.2"` only for an atomic public output state that includes both
  `project.modules` and direct `module_id` fields on every emitted module-owned
  endpoint, warning, component, entity, and test fact.
- The same four output files under `.project-memory/`.
- Existing v0.1 fact arrays, with direct `module_id` fields added to module-owned facts.
- Existing evidence fields; Maven module discovery reuses `build_file` evidence.

The v0.2 `project-map.json` project shape is:

```json
{
  "schema_version": "0.2",
  "project": {
    "root": ".",
    "build": {
      "system": "maven",
      "root_build_file": "pom.xml",
      "evidence_ids": [
        "ev:pom.xml:1-1:build_file:pom.xml"
      ]
    },
    "source_roots": [
      "src/main/java",
      "services/orders/src/main/java"
    ],
    "test_roots": [
      "src/test/java",
      "services/orders/src/test/java"
    ],
    "modules": {
      "analysis_status": "analyzed",
      "items": [
        {
          "module_id": "module:.",
          "module_path": ".",
          "pom_path": "pom.xml",
          "source_roots": ["src/main/java"],
          "test_roots": ["src/test/java"],
          "support_status": "supported",
          "declaration_kind": "scan_root",
          "declared_path": ".",
          "declaration_evidence_ids": [],
          "pom_evidence_ids": [
            "ev:pom.xml:1-1:build_file:pom.xml"
          ]
        },
        {
          "module_id": "module:services/orders",
          "module_path": "services/orders",
          "pom_path": "services/orders/pom.xml",
          "source_roots": ["services/orders/src/main/java"],
          "test_roots": ["services/orders/src/test/java"],
          "support_status": "supported",
          "declaration_kind": "root_modules_entry",
          "declared_path": "services/orders",
          "declaration_evidence_ids": [
            "ev:pom.xml:14-14:build_file:module:services/orders"
          ],
          "pom_evidence_ids": [
            "ev:services/orders/pom.xml:1-1:build_file:pom.xml"
          ]
        }
      ]
    }
  }
}
```

Module identity rules:

- `module_id` is stable within a repository because it is derived from the normalized
  repository-relative module path, not from Maven coordinates, artifact IDs, display
  names, parent POMs, or effective POM data.
- The scan root module is `module:.` with `module_path: "."`.
- A child module is `module:<module_path>`, where `<module_path>` is a normalized
  slash-separated repository-relative path with no leading `./`, no trailing slash, no
  absolute path prefix, and no `.` or `..` path segments.
- `module_path` is the normalized repository-relative directory path for the module. It
  is `"."` only for the scan root.
- `pom_path` is the repository-relative POM path for valid modules with a detected POM,
  or `null` when a valid root declaration is missing its child POM.
- Maven profile resolution, effective POM reconstruction, parent inheritance, dependency
  graph reconstruction, and Maven execution are not part of module identity.

Single-module compatibility rules:

- v0.2 single-module scans use `schema_version: "0.2"` and include one module item for
  the scan root with `module_id: "module:."`.
- There is no valid inventory-only `schema_version: "0.2"` state. Normal public
  scan output must not emit `project.modules` under `schema_version: "0.2"` while any
  emitted module-owned endpoint, warning, component, entity, or test fact lacks
  `module_id`.
- v0.2 single-module scans keep the existing output files and preserve v0.1 top-level
  `project.source_roots`, `project.test_roots`, and root-module fact ID shapes.
- v0.2 multi-module scans keep `project.source_roots` and `project.test_roots` as
  compatibility summaries containing all supported repository-relative roots sorted
  deterministically. Per-module roots in `project.modules.items` are authoritative.

Module inventory rules:

- `project.modules.analysis_status` is `"analyzed"` when module discovery runs. It may
  be `"not_detected"` only when no Maven build input is available for module discovery.
- `project.modules.items` contains the scan root when the scan is single-module or when
  the root has supported production or test roots, plus valid unique child module paths
  declared by the root `<modules>` section.
- For compatibility with pre-v0.2 local source-root scans, when no root `pom.xml` is
  present but supported root source or test roots are detected, `project.modules` uses
  `analysis_status: "not_detected"` and emits a scan-root module with `module_id:
  "module:."`, `module_path: "."`, `pom_path: null`, empty POM evidence, and the
  detected root source or test roots.
- `source_roots` and `test_roots` inside a module item contain repository-relative roots
  under that module. They are empty arrays when no supported root of that kind is
  detected.
- `support_status` is one of:
  - `"supported"`: at least one supported production or test root is detected for the
    module.
  - `"missing_child_pom"`: the root declaration normalized to a valid repository-relative
    module path, but `<module_path>/pom.xml` is missing.
  - `"unsupported"`: a valid child POM is present, but the module has no supported
    Java production or test roots for the v0.2 analyzer slice.
- `declaration_kind` is `"scan_root"` for the root module and `"root_modules_entry"` for
  modules declared in root `<modules>`.
- `declared_path` preserves the deterministic normalized declaration used to derive
  `module_path`; it is `"."` for the scan root.
- `declaration_evidence_ids` references root `<module>` declaration evidence for child
  modules and is an empty array for the scan root.
- `pom_evidence_ids` references `build_file` evidence for the detected root or child
  `pom.xml`. It is an empty array for a valid declaration whose child POM is missing.

Fact-level module identity rules:

- Endpoint facts, component facts, entity facts, test facts, and warning items include a
  direct `module_id` field in v0.2.
- Request parameters, endpoint `mapping_source`, entity identifier fields, entity
  relationships, and test framework signals inherit the `module_id` of their parent fact
  and do not repeat it.
- `tested_subjects` relations include `target_module_id` for the matched production
  class. The initial v0.2 naming-convention inference is same-module only, so
  `target_module_id` is expected to match the parent test fact `module_id`.
- Entity relationships continue to use `target_resolution: "declared_type_only"` and
  `uncertainty: "target_type_not_resolved"`; v0.2 module identity does not imply target
  entity resolution.
- Root-module fact IDs keep the v0.1 ID shape for single-module compatibility. Child
  module facts include the module identity in their stable IDs to avoid collisions with
  facts from other modules.

Example v0.2 endpoint fact:

```json
{
  "id": "endpoint:module:services/orders:com.example.orders.OrderController#getOrder",
  "module_id": "module:services/orders",
  "controller_class": "com.example.orders.OrderController",
  "handler_method": "getOrder",
  "http_methods": ["GET"],
  "http_method_semantics": "declared",
  "paths": ["/orders/{id}"],
  "request_parameters": [],
  "request_body_type": null,
  "response_type": "com.example.orders.OrderDto",
  "mapping_source": {
    "kind": "direct_handler_method",
    "declaring_type": "com.example.orders.OrderController",
    "declaring_method": "getOrder",
    "binding": "direct",
    "uncertainty": null,
    "evidence_ids": [
      "ev:services/orders/src/main/java/com/example/orders/OrderController.java:20-20:com.example.orders.OrderController#getOrder:@GetMapping"
    ]
  },
  "evidence_ids": [
    "ev:services/orders/src/main/java/com/example/orders/OrderController.java:18-18:com.example.orders.OrderController:@RestController",
    "ev:services/orders/src/main/java/com/example/orders/OrderController.java:20-20:com.example.orders.OrderController#getOrder:@GetMapping"
  ]
}
```

Module warning rules:

- In v0.2 output, `warnings.analysis_status` is `"analyzed"` when at least one
  warning-producing analyzer runs, including Maven module discovery or hidden HTTP
  surface analysis. It is `"not_detected"` only when no warning-producing analyzer has
  supported input.
- If Maven module discovery runs and produces `maven_module` warnings but hidden HTTP
  surface analysis does not run because no supported Java production source root exists,
  `warnings.analysis_status` is still `"analyzed"` and `warnings.items` contains only the
  module warnings that were actually produced.
- v0.2 Maven module discovery warnings are emitted in `warnings.items` with
  `category: "maven_module"`.
- Warning items include direct `module_id` when a valid module path exists. `module_id`
  is `null` for invalid declarations that cannot produce a valid module identity.
- `source_path` is the repository-relative path that produced the warning, usually
  `pom.xml` for root declarations or `<module_path>/pom.xml` for nested declarations.
- Warning IDs begin with `warning:maven_module:<signal>:` and use only normalized
  repository-relative module paths or deterministic declaration ordinals as
  discriminators.
- A module declaration ordinal is the one-based document-order index of a root
  `<modules><module>` declaration. Ordinals are rendered in warning IDs as zero-padded
  `decl:000001` style suffixes so invalid or duplicate declarations on the same line
  cannot collide.
- Supported module warning signals are:
  - `"invalid_module_path"`: the `<module>` text is empty, absolute, contains unsupported
    `.` or `..` segments, or resolves outside the scanned repository root. It emits one
    warning per invalid declaration, uses `module_id: null`, does not create a module
    inventory item, and uses an ID shaped as
    `warning:maven_module:invalid_module_path:decl:<ordinal>`.
  - `"missing_child_pom"`: a valid root module path does not contain
    `<module_path>/pom.xml`. It emits at most one warning per normalized module path and
    uses an ID shaped as `warning:maven_module:missing_child_pom:<module_path>`.
  - `"duplicate_module_path"`: more than one root `<module>` declaration resolves to the
    same normalized module path. The first valid declaration may be processed once; later
    duplicates each emit a warning and are ignored as duplicate module items. The warning
    ID includes both the normalized module path and duplicate declaration ordinal, shaped
    as `warning:maven_module:duplicate_module_path:<module_path>:decl:<ordinal>`.
  - `"nested_module_declaration"`: a supported child module POM declares its own
    `<modules>` section. v0.2 records the warning but does not recursively discover
    nested modules. It emits at most one warning per supported child module path and uses
    an ID shaped as `warning:maven_module:nested_module_declaration:<module_path>`.
  - `"unsupported_module"`: a valid child POM is present, but no supported Java
    production or test roots are detected for the v0.2 analyzer slice. It emits at most
    one warning per normalized module path and uses an ID shaped as
    `warning:maven_module:unsupported_module:<module_path>`.
- Invalid declarations do not create module inventory items. Duplicate declarations do
  not create duplicate module inventory items. Valid missing or unsupported modules may
  appear in `project.modules.items` with the corresponding `support_status`.
- Module warnings do not create endpoint, component, entity, or test facts.

Example v0.2 module warning:

```json
{
  "id": "warning:maven_module:missing_child_pom:services/missing",
  "category": "maven_module",
  "signal": "missing_child_pom",
  "module_id": "module:services/missing",
  "message": "Maven module declared in root pom.xml does not have a child pom.xml; v0.2 does not analyze this module.",
  "source_path": "pom.xml",
  "evidence_ids": [
    "ev:pom.xml:18-18:build_file:module:services/missing"
  ]
}
```

Deterministic sorting rules:

- Module inventory items are sorted with `module:.` first, followed by lexicographic
  `module_path` order.
- Top-level `project.source_roots` and `project.test_roots` are sorted
  repository-relative path strings.
- Module-aware endpoints are sorted by module order first, then by the existing v0.1
  endpoint sort keys: first path, HTTP methods, method semantics, controller class, and
  handler method.
- Module-aware component, entity, and test items are sorted by module order first, then
  by their existing v0.1 sort keys.
- Module-aware warning items are sorted by `category`, `signal`, module order,
  `source_path`, and `id`.
- For warning items with `module_id: null`, module order uses the declaration ordinal
  after all concrete module IDs for the same `category` and `signal`; the final `id`
  sort key keeps multiple invalid declarations deterministic.
- Duplicate declaration warnings sort with their normalized module path through
  `module_id`, then by `id`, whose ordinal suffix preserves declaration-specific order
  and prevents ID collisions.
- Evidence entries keep the existing sort order by path, line range, class, method,
  symbol, and ID.

### Planned v0.3 Build And Configuration Contract

This section defines the v0.3 build/configuration JSON contract. The current staged
implementation emits source-visible Maven metadata and the complete `build_config`
section shell. Future v0.3 subsections that are not implemented yet use
`analysis_status: "not_analyzed"` and empty `items` arrays so they do not claim absence
of dependencies, plugins, resources, config files, or Spring Boot application signals.

The planned v0.3 contract uses:

- `schema_version: "0.3"` only for an atomic public output state that keeps the v0.2
  module-aware contract and adds the complete v0.3 build/config section shape for every
  emitted module item.
- The same four output files under `.project-memory/`.
- A module-owned `build_config` object inside each `project.modules.items[]` entry.
- Existing evidence fields and evidence types. Maven observations reuse `build_file`;
  configuration-file observations reuse `config_file`; resource-root inventory entries
  use empty evidence IDs in the planned initial v0.3 contract; Spring Boot application
  signals reuse `annotation` and `code_symbol`.

Schema and compatibility rules:

- `schema_version: "0.3"` builds on the v0.2 boundary. Normal public v0.3 output must
  still include `project.modules` and direct `module_id` fields on emitted module-owned
  endpoint, warning, component, entity, and test facts.
- There is no valid public partial `schema_version: "0.3"` state where only Maven
  metadata, only dependencies, only plugins, only config files, or only Spring Boot
  application signals are emitted while other required v0.3 `build_config` subsection
  shells are absent.
- Implementation checkpoints before the complete public v0.3 boundary should either keep
  unfinished data internal/test-scoped or emit the complete designed section shape with
  explicit `analysis_status` values.
- `analysis_status: "not_analyzed"` is valid only for staged v0.3 subsection shells whose
  analyzers have not been implemented yet. It means the subsection made no absence claim.
  Once a subsection analyzer exists, it must use that subsection's normal
  `"analyzed"`/`"not_detected"` rules instead.
- v0.3 single-module scans keep the existing output files and preserve v0.2
  single-module compatibility for root-module fact IDs, top-level `project.source_roots`,
  and top-level `project.test_roots`.
- Root-level `project.build` remains a scan-level compatibility summary for build system
  detection. It must not become an effective Maven model.

The planned v0.3 module item shape extends the v0.2 item shape like this:

```json
{
  "module_id": "module:services/orders",
  "module_path": "services/orders",
  "pom_path": "services/orders/pom.xml",
  "source_roots": ["services/orders/src/main/java"],
  "test_roots": ["services/orders/src/test/java"],
  "support_status": "supported",
  "declaration_kind": "root_modules_entry",
  "declared_path": "services/orders",
  "declaration_evidence_ids": [
    "ev:pom.xml:14-14:build_file:module:services/orders"
  ],
  "pom_evidence_ids": [
    "ev:services/orders/pom.xml:1-1:build_file:pom.xml"
  ],
  "build_config": {
    "analysis_status": "analyzed",
    "maven": {
      "metadata": {
        "analysis_status": "analyzed",
        "group_id": {
          "value": "com.example",
          "value_kind": "literal",
          "evidence_ids": [
            "ev:services/orders/pom.xml:5-5:build_file:maven:project:groupId"
          ]
        },
        "artifact_id": {
          "value": "orders-service",
          "value_kind": "literal",
          "evidence_ids": [
            "ev:services/orders/pom.xml:6-6:build_file:maven:project:artifactId"
          ]
        },
        "version": {
          "value": "${revision}",
          "value_kind": "property_reference",
          "evidence_ids": [
            "ev:services/orders/pom.xml:7-7:build_file:maven:project:version"
          ]
        },
        "packaging": {
          "value": null,
          "value_kind": "not_declared",
          "evidence_ids": []
        },
        "parent": {
          "analysis_status": "analyzed",
          "group_id": {
            "value": "com.example",
            "value_kind": "literal",
            "evidence_ids": [
              "ev:services/orders/pom.xml:10-10:build_file:maven:parent:groupId"
            ]
          },
          "artifact_id": {
            "value": "example-parent",
            "value_kind": "literal",
            "evidence_ids": [
              "ev:services/orders/pom.xml:11-11:build_file:maven:parent:artifactId"
            ]
          },
          "version": {
            "value": "1.0.0",
            "value_kind": "literal",
            "evidence_ids": [
              "ev:services/orders/pom.xml:12-12:build_file:maven:parent:version"
            ]
          },
          "relative_path": {
            "value": "../pom.xml",
            "value_kind": "literal",
            "evidence_ids": [
              "ev:services/orders/pom.xml:13-13:build_file:maven:parent:relativePath"
            ]
          }
        }
      },
      "dependencies": {
        "analysis_status": "analyzed",
        "items": [
          {
            "id": "maven_dependency:module:services/orders:direct:org.springframework.boot:spring-boot-starter-web:decl:000001",
            "declaration_kind": "direct_dependency",
            "declaration_ordinal": 1,
            "group_id": {
              "value": "org.springframework.boot",
              "value_kind": "literal",
              "evidence_ids": [
                "ev:services/orders/pom.xml:24-24:build_file:maven:dependency:000001:groupId"
              ]
            },
            "artifact_id": {
              "value": "spring-boot-starter-web",
              "value_kind": "literal",
              "evidence_ids": [
                "ev:services/orders/pom.xml:25-25:build_file:maven:dependency:000001:artifactId"
              ]
            },
            "version": {
              "value": null,
              "value_kind": "not_declared",
              "evidence_ids": []
            },
            "scope": {
              "value": null,
              "value_kind": "not_declared",
              "evidence_ids": []
            },
            "optional": {
              "value": null,
              "value_kind": "not_declared",
              "evidence_ids": []
            },
            "type": {
              "value": null,
              "value_kind": "not_declared",
              "evidence_ids": []
            },
            "classifier": {
              "value": null,
              "value_kind": "not_declared",
              "evidence_ids": []
            },
            "evidence_ids": [
              "ev:services/orders/pom.xml:23-27:build_file:maven:dependency:000001"
            ]
          }
        ]
      },
      "dependency_management": {
        "analysis_status": "analyzed",
        "items": []
      },
      "plugins": {
        "analysis_status": "analyzed",
        "items": [
          {
            "id": "maven_plugin:module:services/orders:direct:org.openapitools:openapi-generator-maven-plugin:decl:000001",
            "declaration_kind": "direct_plugin",
            "declaration_ordinal": 1,
            "group_id": {
              "value": "org.openapitools",
              "value_kind": "literal",
              "evidence_ids": [
                "ev:services/orders/pom.xml:45-45:build_file:maven:plugin:000001:groupId"
              ]
            },
            "artifact_id": {
              "value": "openapi-generator-maven-plugin",
              "value_kind": "literal",
              "evidence_ids": [
                "ev:services/orders/pom.xml:46-46:build_file:maven:plugin:000001:artifactId"
              ]
            },
            "version": {
              "value": "${openapi.generator.version}",
              "value_kind": "property_reference",
              "evidence_ids": [
                "ev:services/orders/pom.xml:47-47:build_file:maven:plugin:000001:version"
              ]
            },
            "executions": [
              {
                "execution_id": "generate-api",
                "phase": {
                  "value": "generate-sources",
                  "value_kind": "literal",
                  "evidence_ids": [
                    "ev:services/orders/pom.xml:53-53:build_file:maven:plugin:000001:execution:000001:phase"
                  ]
                },
                "goals": [
                  {
                    "value": "generate",
                    "value_kind": "literal",
                    "evidence_ids": [
                      "ev:services/orders/pom.xml:56-56:build_file:maven:plugin:000001:execution:000001:goal:generate"
                    ]
                  }
                ],
                "evidence_ids": [
                  "ev:services/orders/pom.xml:50-58:build_file:maven:plugin:000001:execution:000001"
                ]
              }
            ],
            "configuration_signals": [
              {
                "signal": "input_spec_config_present",
                "evidence_ids": [
                  "ev:services/orders/pom.xml:61-61:build_file:maven:plugin:000001:configuration:inputSpec"
                ]
              }
            ],
            "generator_signals": [
              {
                "signal": "openapi_swagger_codegen",
                "evidence_ids": [
                  "ev:services/orders/pom.xml:46-46:build_file:maven:plugin:000001:artifactId"
                ]
              }
            ],
            "evidence_ids": [
              "ev:services/orders/pom.xml:44-63:build_file:maven:plugin:000001"
            ]
          }
        ]
      },
      "plugin_management": {
        "analysis_status": "analyzed",
        "items": []
      }
    },
    "resources": {
      "analysis_status": "analyzed",
      "items": [
        {
          "id": "resource_root:module:services/orders:main:services/orders/src/main/resources",
          "scope": "main",
          "path": "services/orders/src/main/resources",
          "evidence_ids": []
        }
      ]
    },
    "config_files": {
      "analysis_status": "analyzed",
      "items": [
        {
          "id": "config_file:module:services/orders:spring_application:services/orders/src/main/resources/application-prod.yml",
          "path": "services/orders/src/main/resources/application-prod.yml",
          "resource_scope": "main",
          "config_kind": "spring_application",
          "format": "yaml",
          "profile_name": "prod",
          "profile_source": "filename_only",
          "evidence_ids": [
            "ev:services/orders/src/main/resources/application-prod.yml:unknown:config_file:application-prod.yml"
          ]
        }
      ]
    },
    "spring_boot_applications": {
      "analysis_status": "analyzed",
      "items": [
        {
          "id": "spring_boot_application:module:services/orders:com.example.orders.OrdersApplication",
          "class_name": "com.example.orders.OrdersApplication",
          "source_path": "services/orders/src/main/java/com/example/orders/OrdersApplication.java",
          "application_signal": "spring_boot_application_with_main_method",
          "main_method": {
            "present": true,
            "evidence_ids": [
              "ev:services/orders/src/main/java/com/example/orders/OrdersApplication.java:12-12:com.example.orders.OrdersApplication#main:code_symbol"
            ]
          },
          "evidence_ids": [
            "ev:services/orders/src/main/java/com/example/orders/OrdersApplication.java:8-8:com.example.orders.OrdersApplication:@SpringBootApplication",
            "ev:services/orders/src/main/java/com/example/orders/OrdersApplication.java:12-12:com.example.orders.OrdersApplication#main:code_symbol"
          ]
        }
      ]
    }
  }
}
```

Build/config analysis status rules:

- `build_config.analysis_status` is `"analyzed"` when at least one planned v0.3
  build/config analyzer runs for the module. It is `"not_detected"` when the module has
  no supported POM, source, resource, or config input for v0.3 build/config analysis.
- Maven subsection `analysis_status` values are `"analyzed"` when a module POM is
  present and parsed for the relevant subsection. They are `"not_detected"` when the
  module has no POM available to that analyzer.
- In the current staged v0.3 metadata slice, `maven.metadata.analysis_status` is
  `"analyzed"` when a module POM is present and parsed for direct metadata, while
  `dependencies`, `dependency_management`, `plugins`, and `plugin_management` use
  `"not_analyzed"` with empty `items` arrays until their bounded analyzers are
  implemented. Those empty arrays must not be read as a dependency or plugin inventory
  result.
- Resource, config, and Spring Boot subsection `analysis_status` values are `"analyzed"`
  when the relevant analyzer runs for supported module roots, even when the resulting
  item list is empty. They are `"not_detected"` only when no supported input root exists.
- In the current staged v0.3 metadata slice, `resources`, `config_files`, and
  `spring_boot_applications` use `"not_analyzed"` with empty `items` arrays until their
  bounded analyzers are implemented.

Maven value rules:

- Maven scalar values use the object shape `{ "value": ..., "value_kind": ...,
  "evidence_ids": [...] }`.
- `value_kind` is one of:
  - `"literal"`: directly declared literal XML text.
  - `"property_reference"`: directly declared `${...}` style property reference.
  - `"expression"`: directly declared text containing non-literal Maven expressions.
  - `"not_declared"`: the XML element is absent.
  - `"unsupported"`: the XML element is present but cannot be represented
    deterministically.
- Missing `groupId`, `version`, or `packaging` values must not be filled from parent
  inheritance, Maven defaults, dependency management, active profiles, or effective POM
  behavior.
- Parent coordinates are recorded only under `metadata.parent`. Recording parent
  coordinates does not resolve the module's effective coordinates.
- `metadata.parent.analysis_status` is `"analyzed"` when a direct `<parent>` element is
  present in the module POM and `"not_detected"` when no direct `<parent>` element is
  present. Parent value fields use the same Maven scalar value object as module metadata.

Dependency inventory rules:

- `dependencies.items` contains only direct `<dependencies><dependency>` declarations in
  the module POM.
- `dependency_management.items` contains only direct
  `<dependencyManagement><dependencies><dependency>` declarations in the module POM.
- `declaration_kind` is `"direct_dependency"` for active direct declarations and
  `"dependency_management"` for management declarations.
- Dependency management declarations must not be rendered as active dependencies.
- Dependency facts preserve source-visible coordinate, scope, optional, type, and
  classifier text. They do not claim resolved versions, transitive dependencies,
  inherited scopes, profile activation, conflict mediation, repository availability, or
  effective dependency graphs.
- Property references remain source-visible `property_reference` values. v0.3 does not
  resolve project properties.

Plugin inventory and generator signal rules:

- `plugins.items` contains only direct `<build><plugins><plugin>` declarations in the
  module POM.
- `plugin_management.items` contains only direct
  `<build><pluginManagement><plugins><plugin>` declarations in the module POM.
- `declaration_kind` is `"direct_plugin"` for direct plugin declarations and
  `"plugin_management"` for plugin-management declarations.
- Plugin-management declarations must not be rendered as active execution behavior.
- Plugin facts may include direct source-visible execution IDs, phases, and goals.
  They must not reconstruct Maven lifecycle bindings, default goals, inherited
  executions, resolved plugin versions, or full plugin execution behavior.
- `configuration_signals` records only bounded signal names and evidence IDs. It must not
  store arbitrary plugin configuration values.
- Planned bounded configuration signals include
  `"input_spec_config_present"`, `"generated_sources_config_present"`,
  `"annotation_processor_paths_present"`, and `"add_source_goal_present"`.
- `generator_signals` records conservative plugin-level signals such as
  `"openapi_swagger_codegen"`, `"source_generator_plugin"`, and
  `"annotation_processor"`.
- Generator and OpenAPI/Swagger plugin signals do not create endpoint facts, API
  operation facts, generated source facts, or generated API reconstruction.

Resource and config discovery rules:

- Resource roots are repository-relative paths under supported modules.
- `resource.scope` is `"main"` for `src/main/resources` and `"test"` for
  `src/test/resources`.
- Resource-root entries are path inventory facts. In the planned initial v0.3 contract
  they use empty `evidence_ids`, matching the current source-root and test-root summary
  pattern, because the existing evidence model has no directory evidence type.
- Config file facts record file paths and filename-derived metadata only.
- `config_kind` is one of:
  - `"spring_application"` for `application.properties`, `application.yml`,
    `application.yaml`, and supported `application-*` profile filenames.
  - `"logging_config"` for supported logging configuration filenames.
- `format` is one of `"properties"`, `"yaml"`, `"xml"`, or `"unknown"`.
- `profile_name` is the filename-derived profile segment for profile-specific Spring
  application files, or `null` for default application files and non-profile config
  files.
- `profile_source` is `"filename_only"` when `profile_name` is present and `null`
  otherwise.
- Profile names do not imply profile activation, runtime precedence, environment
  selection, or effective Spring configuration.
- Config discovery must not parse or store property keys, property values, YAML node
  content, XML element content, environment placeholders, decrypted secrets, or config
  excerpts.

Spring Boot application signal rules:

- `spring_boot_applications.items` contains direct source-visible
  `@SpringBootApplication` class signals under supported production source roots.
- `application_signal` is one of:
  - `"spring_boot_application_annotation_only"` when the annotation is present but no
    supported source-visible `main` method is detected on that class.
  - `"spring_boot_application_with_main_method"` when the annotation and a supported
    source-visible `main` method are both detected on that class.
- These facts do not claim executable jar packaging, active profiles, runtime
  auto-configuration, bean graph, component scanning result, deployment behavior, or
  actual process entrypoint behavior.

Planned v0.3 warning rules:

- Generated-source and generator warnings are emitted in `warnings.items`.
- Planned generated-source warning items use `category: "generated_source"`.
- Generated-source warning IDs begin with `warning:generated_source:<signal>:` and use
  only normalized repository-relative module paths, source paths, or zero-padded
  `decl:<ordinal>` declaration/signal ordinals as discriminators.
- POM-derived plugin, annotation-processor, and generated-source configuration warnings
  should include the module path plus a bounded declaration or signal ordinal in the
  warning ID. Repository-path generated-source root warnings should include the detected
  normalized generated-source root path.
- Warnings include direct `module_id` when the signal belongs to a valid module.
- Planned generated-source warning signals include:
  - `"maven_generator_plugin"`;
  - `"maven_openapi_swagger_codegen_plugin"`;
  - `"maven_annotation_processor"`;
  - `"maven_generated_source_config"`;
  - `"maven_build_helper_add_source"`;
  - `"generated_source_root_path_detected"`.
- OpenAPI/Swagger plugin declarations may also continue to emit the existing
  `hidden_http_surface` warning signal where appropriate. The warning remains a warning
  and must not create endpoint or API facts.
- If the same OpenAPI/Swagger plugin declaration supports both a planned
  `generated_source` warning and an existing `hidden_http_surface` warning, each warning
  keeps its own category and ID namespace.
- Warning messages must use detected-signal wording. They must not summarize generated
  source contents, generated API operations, runtime build behavior, or effective Maven
  execution.

Sensitive config handling rules:

- `project-map.json`, `evidence-index.jsonl`, `endpoints.md`, and `agent-guide.md` must
  not include config file contents, property keys, property values, YAML node content,
  XML element content, decrypted values, or secret-looking values from config files.
- `config_file` evidence excerpts for v0.3 config discovery must be bounded path or
  filename observations such as `config file detected: application.yml`.
- Any future proposal to store config keys, selected safe values, or source excerpts from
  config files requires an explicit contract update, evidence model update if needed,
  sensitive-fixture tests, and a security gate.

Deterministic sorting rules:

- Build/config module sections follow the existing v0.2 module order.
- Dependency and dependency-management items are sorted by `group_id.value`,
  `artifact_id.value`, `type.value`, `classifier.value`, `scope.value`,
  `declaration_ordinal`, and `id`, with `null` values sorting after strings.
- Plugin and plugin-management items are sorted by `group_id.value`,
  `artifact_id.value`, `declaration_ordinal`, and `id`.
- Resource roots are sorted by `scope`, `path`, and `id`.
- Config files are sorted by `resource_scope`, `config_kind`, `path`, and `id`.
- Spring Boot application signals are sorted by `class_name`, `source_path`, and `id`.
- Generated-source warnings follow the existing warning sort order by `category`,
  `signal`, module order, `source_path`, and `id`.

## `evidence-index.jsonl`

`evidence-index.jsonl` is newline-delimited JSON. Each line is one evidence record.
The implementation emits a stable field order:

```json
{"id":"ev:src/main/java/com/example/orders/OrderController.java:18-18:com.example.orders.OrderController:@RestController","source_type":"annotation","path":"src/main/java/com/example/orders/OrderController.java","class_name":"com.example.orders.OrderController","method_name":null,"symbol_name":"@RestController","line_start":18,"line_end":18,"excerpt":"@RestController","confidence":"high"}
{"id":"ev:src/main/java/com/example/orders/OrderController.java:20-20:com.example.orders.OrderController#getOrder:@GetMapping","source_type":"annotation","path":"src/main/java/com/example/orders/OrderController.java","class_name":"com.example.orders.OrderController","method_name":"getOrder","symbol_name":"@GetMapping","line_start":20,"line_end":20,"excerpt":"@GetMapping(\"/orders/{id}\")","confidence":"high"}
```

Evidence entries should follow `docs/architecture/EVIDENCE_MODEL.md`.

The current implementation emits:

- `build_file` evidence for root `pom.xml` when present.
- `annotation` evidence for extracted Spring MVC controller, endpoint, request parameter,
  and request body annotations.
- Source-visible interface-declared endpoint mappings reuse existing `annotation`
  evidence for interface mapping annotations and existing `code_symbol` evidence for
  interface and concrete handler symbols needed to prove the unique binding. No new
  evidence fields are required.
- `annotation` evidence for direct supported Spring component stereotype annotations on
  Java class or interface declarations. `@Controller` and `@RestController` evidence IDs
  use the same annotation ID convention as endpoint evidence so the same source
  annotation is not duplicated in `evidence-index.jsonl`.
- `annotation` evidence for direct JPA annotations that support entity facts, including
  class-level `@Entity`, class-level `@Table`, field-level `@Id`, and field-level
  relationship annotations `@ManyToOne`, `@OneToMany`, `@OneToOne`, and `@ManyToMany`.
  Field-level evidence IDs include a `field:<field_name>` discriminator because the
  current evidence record field set does not add a separate field-name property.
- `test_file` evidence for emitted test-like Java class declarations under supported
  test roots.
- `code_symbol` evidence for production class declarations that are referenced by
  inferred `tested_subjects` relations.
- `code_symbol` evidence for directly visible test framework imports attached to
  top-level emitted test classes.
- `annotation` evidence for directly visible test framework annotations.
- Warning evidence for hidden HTTP surface signals:
  - `config_file` evidence for OpenAPI/Swagger spec filename presence, with the spec
    path as `path`, the filename as `symbol_name`, nullable line fields, and a bounded
    excerpt such as `filename detected: openapi.yml`. The scanner does not parse the
    file content.
  - `build_file` evidence for deterministic OpenAPI/Swagger Maven code generation
    plugin declarations in the root `pom.xml`, with the plugin artifact ID as
    `symbol_name` and the matching artifactId line as the excerpt.
  - `annotation` evidence for direct source-visible `@RepositoryRestResource`
    annotations.

Mapped-superclass identifier facts do not add new evidence fields. When an identifier
field uses `source_kind` set to `"mapped_superclass"`, it uses the existing `annotation`
evidence shape for the field-level `@Id` declaration and the class-level
`@MappedSuperclass` declaration on `declaring_class`, including when that declaring class
is reached through a conservative source-visible mapped-superclass chain.

v0.2 Maven module discovery also does not add new evidence fields. Root
`<modules>` entries and child POM files reuse `build_file` evidence:

- Root `<module>` declaration evidence uses `path: "pom.xml"`, `source_type:
  "build_file"`, and a `symbol_name` derived from the normalized module path such as
  `module:services/orders` when the declaration is valid.
- Child POM evidence uses the repository-relative child POM path such as
  `services/orders/pom.xml`, `source_type: "build_file"`, and `symbol_name: "pom.xml"`.
- Module discovery evidence supports only deterministic local POM observations. It does
  not prove effective POM contents, Maven profile activation, dependency graphs, generated
  sources, or runtime Spring behavior.
- Module discovery evidence paths must remain normalized repository-relative paths and
  must not be absolute, start with `./`, or escape the scanned repository root.
- The current staged v0.3 Maven metadata analyzer also reuses `build_file` evidence for
  direct source-visible module metadata and parent coordinate elements. Metadata evidence
  uses `symbol_name` values such as `maven:project:artifactId` or
  `maven:parent:version`, points to the module POM path, and supports only the direct POM
  text. It does not prove Maven defaults, inherited coordinates, profile activation, or
  effective POM values.

Evidence entries are sorted deterministically by path, line range, class, method, symbol,
and ID. Nullable fields are emitted as JSON `null`; absent repeated values are emitted as
empty arrays in `project-map.json`.

## `endpoints.md`

`endpoints.md` is a human-readable endpoint inventory generated from deterministic endpoint facts.

It should include:

- HTTP method.
- Path.
- Controller class.
- Handler method.
- Mapping source when available.
- Request body type when detected.
- Response type when detected.
- Evidence reference.

If a `@RequestMapping` endpoint does not declare an HTTP method, the Markdown output
must say that the method was not declared instead of inventing one. If a method
expression is present but unsupported by deterministic source extraction, the output
must mark it as unsupported.

For interface-declared mappings, `endpoints.md` should say that the mapping source is a
source-visible interface method and name the interface method when `mapping_source` is
available. It must not describe generated OpenAPI operations, generated `*Api`
interfaces, or runtime handler mappings unless the source-visible Java interface is
present under supported production source roots and represented in `project-map.json`
with resolving evidence.

Example shape:

```md
# Endpoints

## GET /orders/{id}

- Controller: `com.example.orders.OrderController`
- Handler: `getOrder`
- Mapping source: `direct_handler_method`
- Response: `com.example.orders.OrderDto`
- Evidence: `src/main/java/com/example/orders/OrderController.java:20`
```

Current v0.2 `endpoints.md` behavior:

- Endpoint sections should be grouped by module in deterministic module order.
- The single-module root group may be omitted or rendered as the scan root when there is
  only `module:.`, but endpoint content must still resolve from module-aware
  `project-map.json` facts.
- Multi-module endpoint entries should include the module identity or module path near
  the endpoint heading or metadata.
- Module grouping must not claim architectural layers, service ownership, bounded
  contexts, or runtime routing behavior beyond the module identity recorded in
  `project-map.json`.

## `agent-guide.md`

`agent-guide.md` is a concise orientation file for AI coding agents and developers.

It is generated from `project-map.json` and `evidence-index.jsonl`, or from the same
structured in-memory facts that are serialized to those files. The guide generator must
not walk source files, call LLMs, call external services, ingest local documentation, or
invent architecture not represented by deterministic facts.

The minimal stable v0.1 section order is:

```md
# Agent Guide

Generated deterministically from `project-map.json` and `evidence-index.jsonl`.

## Detected Project Layout
## Detected Spring MVC Endpoints
## Detected Spring Components
## Detected JPA Entities
## Detected Tests
## Known Uncertainty And Limits
## Practical Inspection Order For Coding Agents
```

Content rules:

- The project layout section reports the detected build system, root build file, source
  roots, and test roots from `project-map.json`.
- Evidence-backed entries render readable evidence references by resolving
  `evidence_ids` through `evidence-index.jsonl`. References should include a source
  location such as `path:line` or `path:start-end` plus the evidence ID.
- Long Markdown evidence-reference lists are presentation-capped to keep the guide
  concise. When a list is capped, the guide must keep the first evidence references
  inline and add a suffix such as `... and N more evidence references in
  evidence-index.jsonl`. This does not remove or alter complete evidence records in
  `evidence-index.jsonl` or evidence IDs in `project-map.json`.
- Facts without dedicated evidence IDs, such as current source-root and test-root lists,
  must say that they are recorded in `project-map.json` and that no separate evidence ID
  is emitted by the current implementation.
- Endpoint entries use cautious `Detected` wording and include controller class, handler
  method, HTTP method status, paths, request parameters, request body, response type, and
  evidence references. When `mapping_source` is available, endpoint entries should state
  whether the mapping came from a direct handler method or from a uniquely bound
  source-visible interface method, without claiming complete runtime handler mapping
  behavior.
- Hidden HTTP surface warnings, when present, are rendered in the known-limits section
  with `Warning` wording, the warning category, signal, source path, deterministic
  message, and resolving evidence references. They must not be rendered as detected
  endpoint facts.
- Component entries use `Detected` wording and include direct stereotype annotations and
  evidence references. They must not claim Spring runtime wiring, component scanning,
  lifecycle, scopes, bean names, or dependency graphs.
- Entity entries use `Detected` wording for direct entity, table, and identifier facts.
  Identifier entries should include `declaring_class` and `source_kind` when that context
  matters. Mapped-superclass identifiers must be described as direct source-visible
  mapped-superclass facts, not as full ORM inheritance reconstruction. Relationship
  entries must preserve `target_resolution: declared_type_only` and `uncertainty:
  target_type_not_resolved`, and should present relationship targets as `Uncertain`, not
  resolved entity links.
- Test entries use `Detected` wording for test classes and directly visible framework
  signals. `tested_subjects` entries must use `Inferred` wording and show
  `support_type`, `confidence`, and `uncertainty` when present.
- The known-limits section must explicitly call out `Not analyzed`, `Inferred`, and
  `Uncertain` areas, including Spring runtime behavior, ORM runtime behavior, test
  execution/coverage/assertion behavior, call graphs, complete subject mapping,
  connectors, LLM summaries, repository chat, generic RAG, Gradle/Kotlin support, Maven
  profiles, effective POM reconstruction, dependency graphs, and recursive nested Maven
  modules. It should also call out that generated sources, OpenAPI YAML, generated API
  reconstruction, classpath-only interfaces, and ambiguous interface endpoint bindings
  are not analyzed for `EVAL-8-004` decision B, and that mapped-superclass identifier
  traversal skips unresolved, ambiguous, cyclic, and non-source-visible branches.
- The practical inspection order may suggest evidence paths from generated facts, but it
  must not introduce unsupported architecture, modules, domain flows, service layers, or
  source summaries. Long inline evidence path lists should be capped with a suffix that
  points readers back to `evidence-index.jsonl` for the complete source-backed evidence.

Current v0.2 `agent-guide.md` behavior:

- The detected project layout section should summarize `project.modules.items` in
  deterministic module order, including `module_id`, `module_path`, `pom_path`,
  `support_status`, source roots, test roots, and resolving evidence where available.
- Endpoint, component, entity, test, and warning sections should group or label facts by
  module using the module identity from `project-map.json`.
- Module warnings should appear in the known-limits section with `Warning` wording and
  resolving evidence references. They must not be rendered as application facts.
- The practical inspection order may use module paths as navigation hints, but it must
  not infer dependency direction, runtime Spring boundaries, ownership, generated API
  contents, or cross-module architecture unless future deterministic facts explicitly
  support those claims.

Current staged v0.3 metadata behavior:

- `project-map.json` includes module-owned Maven metadata under
  `project.modules.items[].build_config.maven.metadata`.
- `agent-guide.md` keeps the current v0.2 guide sections until the bounded v0.3 guide
  rendering goal adds a dedicated build/configuration orientation section.
- The current guide must not render `not_analyzed` dependency, plugin, resource, config,
  or Spring Boot application subsection shells as detected facts.

Planned v0.3 `agent-guide.md` behavior:

- The guide should add a `Build And Configuration Orientation` section generated from
  planned v0.3 `build_config` facts.
- Maven metadata, dependency, and plugin sections must use `Source-visible` wording and
  must not claim effective POM coordinates, inherited values, transitive dependencies,
  resolved plugin versions, lifecycle execution, profile activation, or remote
  repository availability.
- Dependency-management and plugin-management declarations must be labeled as management
  declarations, not as active dependencies or active plugin executions.
- Resource and config summaries may list detected resource roots and config file paths,
  config kind, format, and filename-derived profile name. They must not print config
  file contents, property keys, property values, YAML node content, XML element content,
  environment placeholders, decrypted secrets, or config excerpts.
- Spring Boot application entries must use `Detected` wording for direct
  `@SpringBootApplication` and source-visible `main` method signals. They must not claim
  executable packaging, active profiles, runtime auto-configuration, bean graph,
  component scanning result, deployment behavior, or actual process entrypoint behavior.
- Generated-source, OpenAPI/Swagger, annotation-processor, and generator plugin signals
  should appear as warnings or known limits. They must not be rendered as detected
  endpoints, generated APIs, implemented API operations, or generated source contents.
- The known-limits section should explicitly state that v0.3 build/config facts are
  direct local source observations only, and that Maven execution, effective POM
  reconstruction, profile activation, remote dependency resolution, config value
  interpretation, secret extraction, and default generated-source scanning are not
  performed.

Markdown rendering safety:

- Markdown generators must render source-derived values through Markdown-safe presentation
  helpers before writing `endpoints.md` or `agent-guide.md`.
- Source-derived inline text, inline code, module labels and paths, endpoint paths,
  request parameter labels, warning paths and messages, evidence references, and evidence
  locations must not be able to introduce new Markdown headings, list items, evidence
  lines, tables, links, or HTML when the source value contains newlines, control
  characters, backticks, or Markdown punctuation.
- Markdown presentation may normalize control characters and line breaks into visible
  escaped sequences such as `\n`, `\r`, `\t`, or `\u001B`. This does not change the
  corresponding `project-map.json` or `evidence-index.jsonl` values, which keep their
  JSON/JSONL escaping and semantics.

## Contract Rules

- Output changes require updating this file.
- Evidence field changes require updating `docs/architecture/EVIDENCE_MODEL.md`.
- Generated facts must reference evidence IDs where possible.
- Markdown outputs should remain readable without hiding evidence.
- JSON outputs should remain stable enough for tests and downstream tools.
