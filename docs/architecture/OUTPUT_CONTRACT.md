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
module roots, supported root source, test, or resource roots, supported config files, or
Maven module warnings are detected. Unsupported directories still only get a prepared
`.project-memory/` directory and do not get contract output files.

The v0.1 interface-mapping endpoint contract keeps endpoint extraction limited to
source-visible Java inputs under supported production source roots, while adding
uniquely bound interface-declared Spring MVC mappings to the v0.1 endpoint semantics. It
does not add Maven generation during scans, default `target/generated-sources` scanning,
full OpenAPI validation, generated API reconstruction, or Spring runtime handler
mapping reconstruction.

## `project-map.json`

`project-map.json` is the machine-readable project memory file. The current public
contract is the v0.5 Spring application surface repository and configuration slices
layered on top of the v0.4 API surface slice and the v0.3 module-aware Maven
metadata, dependency, and plugin inventory contract.
The v0.1 single-module shape below is kept as historical compatibility context for
fields that later contracts preserve.

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

Endpoint mapping-source rules for the v0.1 interface-mapping decision:

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
  run Maven generation, scan `target/generated-sources` by default, derive endpoint
  facts from OpenAPI operations, reconstruct generated APIs, resolve classpath-only
  interfaces, infer runtime proxies, or interpret unsupported Spring mapping conditions.
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
    such as `openapi.yml`, `openapi.yaml`, `openapi.json`, `swagger.yml`,
    `swagger.yaml`, or `swagger.json`. The legacy warning is by filename only and does
    not parse the file content.
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
  OpenAPI operations, run Maven generation, scan `target/generated-sources` by default, or
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
  mapping, embedded IDs, generated-value runtime semantics, join-column analysis,
  repository analysis, schema generation, or runtime ORM behavior. The bounded v0.6
  direct field-level `@Column` metadata slice is described in the v0.6 section below.
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
  the root has supported production, test, or resource roots, plus valid unique child
  module paths declared by the root `<modules>` section.
- For compatibility with pre-v0.2 local source-root scans, when no root `pom.xml` is
  present but supported root source, test, or resource roots are detected,
  `project.modules` uses `analysis_status: "not_detected"` and emits a scan-root module
  with `module_id: "module:."`, `module_path: "."`, `pom_path: null`, empty POM
  evidence, and the detected root source or test roots.
- `source_roots` and `test_roots` inside a module item contain repository-relative roots
  under that module. They are empty arrays when no supported root of that kind is
  detected.
- `support_status` is one of:
  - `"supported"`: at least one supported production, test, or resource root is detected
    for the module.
  - `"missing_child_pom"`: the root declaration normalized to a valid repository-relative
    module path, but `<module_path>/pom.xml` is missing.
  - `"unsupported"`: a valid child POM is present, but the module has no supported
    Java production, test, or resource roots for the current analyzer slice.
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
    production, test, or resource roots are detected for the current analyzer slice. It
    emits at most one warning per normalized module path and uses an ID shaped as
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

### Current v0.3 Build And Configuration Contract

This section defines the v0.3 build/configuration JSON contract. The current
implementation emits source-visible Maven metadata, source-visible Maven dependency
inventory, source-visible Maven plugin inventory, standard resource-root inventory,
path-only supported application/logging config-file inventory, direct source-visible
Spring Boot application signals, and the complete `build_config` section shell.

The v0.3 contract uses:

- `schema_version: "0.3"` only for an atomic public output state that keeps the v0.2
  module-aware contract and adds the complete v0.3 build/config section shape for every
  emitted module item.
- The same four output files under `.project-memory/`.
- A module-owned `build_config` object inside each `project.modules.items[]` entry.
- Existing evidence fields and evidence types. Maven observations reuse `build_file`;
  configuration-file observations reuse `config_file`; resource-root inventory entries
  use empty evidence IDs in the v0.3 contract; Spring Boot application signals reuse
  `annotation` and `code_symbol`.

Schema and compatibility rules:

- `schema_version: "0.3"` builds on the v0.2 boundary. Normal public v0.3 output must
  still include `project.modules` and direct `module_id` fields on emitted module-owned
  endpoint, warning, component, entity, and test facts.
- There is no valid public partial `schema_version: "0.3"` state where only Maven
  metadata, only dependencies, only plugins, only config files, or only Spring Boot
  application signals are emitted while other required v0.3 `build_config` subsection
  shells are absent.
- `analysis_status: "not_analyzed"` is valid only for explicit not-analyzed subsection
  shells. It means the subsection made no absence claim. Once a subsection analyzer
  exists, it must use that subsection's normal `"analyzed"`/`"not_detected"` rules
  instead.
- v0.3 single-module scans keep the existing output files and preserve v0.2
  single-module compatibility for root-module fact IDs, top-level `project.source_roots`,
  and top-level `project.test_roots`.
- Root-level `project.build` remains a scan-level compatibility summary for build system
  detection. It must not become an effective Maven model.

The v0.3 module item shape extends the v0.2 item shape like this:

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

- `build_config.analysis_status` is `"analyzed"` when at least one v0.3
  build/config analyzer runs for the module. It is `"not_detected"` when the module has
  no supported POM, source, resource, or config input for v0.3 build/config analysis.
- Maven subsection `analysis_status` values are `"analyzed"` when a module POM is
  present and parsed for the relevant subsection. They are `"not_detected"` when the
  module has no POM available to that analyzer.
- In the current v0.3 Maven analysis,
  `maven.metadata.analysis_status`, `dependencies.analysis_status`, and
  `dependency_management.analysis_status`, plus `plugins.analysis_status` and
  `plugin_management.analysis_status` are `"analyzed"` when a module POM is present and
  parsed for the relevant direct POM observations.
- Resource and config subsection `analysis_status` values are `"analyzed"` when standard
  resource roots are present and the relevant analyzer runs, even when the resulting
  config item list is empty. They are `"not_detected"` when no supported resource input
  root exists.
- Spring Boot application subsection `analysis_status` values are `"analyzed"` when
  supported production source roots are present and the analyzer runs, even when no
  direct `@SpringBootApplication` class signal is detected. They are `"not_detected"`
  when no supported production source root exists for that module.

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
- Resource-root entries are path inventory facts. In the v0.3 contract they use empty
  `evidence_ids`, matching the current source-root and test-root summary pattern,
  because the existing evidence model has no directory evidence type.
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

v0.3 warning rules:

- Generated-source and generator warnings are emitted in `warnings.items`.
- Generated-source warning items use `category: "generated_source"`.
- Plugin-derived generated-source warning IDs use the shape
  `warning:generated_source:<signal>:module:<module_path>:<declaration_kind>:decl:<ordinal>`.
  `<declaration_kind>` is the emitted plugin `declaration_kind` value and distinguishes
  direct plugin declarations from `pluginManagement` declarations when their declaration
  ordinals would otherwise collide.
- POM-derived plugin, annotation-processor, and generated-source configuration warnings
  include the module path, declaration kind, and bounded declaration ordinal in the
  warning ID.
  Repository-path generated-source root warnings include the detected normalized
  generated-source root path.
- Warnings include direct `module_id` when the signal belongs to a valid module.
- Current plugin-derived generated-source warning signals include:
  - `"maven_generator_plugin"`;
  - `"maven_openapi_swagger_codegen_plugin"`;
  - `"maven_annotation_processor"`;
  - `"maven_generated_source_config"`;
  - `"maven_build_helper_add_source"`.
- Current generated-source path warnings include:
  - `"generated_source_root_path_detected"`.
- Path-derived generated-source warning IDs use
  `warning:generated_source:generated_source_root_path_detected:path:<generated_source_path_key>`
  for the scan-root module and
  `warning:generated_source:generated_source_root_path_detected:module:<module_path>:path:<generated_source_path_key>`
  for child modules. `<generated_source_path_key>` uses the same percent-encoded
  repository-relative path key rules as v0.4 spec paths.
- OpenAPI/Swagger plugin declarations may also continue to emit the existing
  `hidden_http_surface` warning signal where appropriate. The warning remains a warning
  and must not create endpoint or API facts.
- If the same OpenAPI/Swagger plugin declaration supports both a `generated_source`
  warning and an existing `hidden_http_surface` warning, each warning keeps its own
  category and ID namespace.
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
  sensitive-fixture tests, and risk-based security review.

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

### v0.4 Declared And Generated API Surface Contract

This section defines the current v0.4 API surface contract slice. The release
implementation emits the API surface shell, endpoint categories, local OpenAPI/Swagger
spec file facts, minimal spec-backed OpenAPI/Swagger operation facts, and conservative
generated-source path warnings with `path_signal` evidence while keeping
generated-source content scanning non-default.

The v0.4 contract uses:

- `schema_version: "0.4"` for the public output state that preserves the v0.3
  module-aware build/config contract and adds the API surface section shape.
- The same four output files under `.project-memory/`.
- Existing top-level `endpoints[]` as the canonical collection of code-backed
  source-visible Spring MVC endpoint facts.
- An `api_surface_category` field on each endpoint fact, with values
  `"source_visible_spring_mvc_endpoint"` or
  `"interface_declared_spring_mvc_endpoint"`.
- A top-level `api_surface` object that categorizes source-visible endpoint IDs, local
  OpenAPI/Swagger spec file facts, declared OpenAPI operations, generated-source API
  warning IDs, repository-rest warning IDs, and hidden HTTP warning IDs without turning
  every API-adjacent signal into an endpoint fact.
- Current `api_spec` evidence for local OpenAPI/Swagger spec file and operation facts.
  Current `path_signal` evidence for generated-source path warning signals.

Taxonomy rules:

- `source_visible_spring_mvc_endpoint` means a direct Spring MVC mapping on a concrete
  source-visible controller handler. It is code-backed by supported Java source evidence
  and remains in `endpoints[]`.
- `interface_declared_spring_mvc_endpoint` means a source-visible interface-declared
  Spring MVC mapping uniquely bound to a concrete source-visible controller handler. It
  is code-backed by interface mapping evidence and concrete binding evidence, remains in
  `endpoints[]`, and must stay separate from direct handler mappings.
- `openapi_declared_operation` means a path/method operation declared in a local
  OpenAPI/Swagger spec file. It is spec-backed, lives outside `endpoints[]`, and must not
  imply implementation by Spring MVC or generated code.
- `generated_source_api_signal` means a build/config/path signal that generated API
  source may exist. It is a warning/signal only unless a later explicit generated-source
  scan mode is designed and enabled.
- `repository_rest_warning` means a direct source-visible `@RepositoryRestResource`
  signal. It remains a warning and must not create repository REST endpoint facts until
  a deterministic repository-rest model is designed.
- `hidden_http_warning` means a bounded HTTP/API-adjacent signal that cannot be
  represented as a source endpoint, spec operation, generated-source API signal, or
  repository-rest warning. It remains a warning.

Planned high-level `project-map.json` shape:

```json
{
  "schema_version": "0.4",
  "endpoints": [
    {
      "id": "endpoint:module:services/orders:com.example.orders.OrderController#getOrder",
      "module_id": "module:services/orders",
      "api_surface_category": "source_visible_spring_mvc_endpoint",
      "mapping_source": {
        "kind": "direct_handler_method"
      }
    }
  ],
  "api_surface": {
    "analysis_status": "analyzed",
    "source_visible_spring_mvc_endpoints": {
      "analysis_status": "analyzed",
      "endpoint_ids": [
        "endpoint:module:services/orders:com.example.orders.OrderController#getOrder"
      ]
    },
    "interface_declared_spring_mvc_endpoints": {
      "analysis_status": "analyzed",
      "endpoint_ids": []
    },
    "openapi": {
      "spec_files": {
        "analysis_status": "analyzed",
        "items": [
          {
            "id": "openapi_spec:module:services/orders:path:services/orders/src/main/resources/openapi.yml",
            "module_id": "module:services/orders",
            "spec_path": "services/orders/src/main/resources/openapi.yml",
            "format": "yaml",
            "spec_kind": "openapi",
            "version": "3.0.3",
            "evidence_ids": [
              "ev:services/orders/src/main/resources/openapi.yml:1-1:api_spec:openapi"
            ]
          }
        ]
      },
      "operations": {
        "analysis_status": "analyzed",
        "items": [
          {
            "id": "openapi_operation:module:services/orders:spec:services/orders/src/main/resources/openapi.yml:operation:get:/orders/{id}",
            "module_id": "module:services/orders",
            "api_surface_category": "openapi_declared_operation",
            "spec_path": "services/orders/src/main/resources/openapi.yml",
            "http_method": "GET",
            "path": "/orders/{id}",
            "operation_id": "getOrder",
            "tags": [
              "Orders"
            ],
            "implementation_status": "not_analyzed",
            "evidence_ids": [
              "ev:services/orders/src/main/resources/openapi.yml:12-12:api_spec:operation%3Aget%3A/orders/{id}"
            ]
          }
        ]
      }
    },
    "generated_source_api_signals": {
      "analysis_status": "analyzed",
      "warning_ids": []
    },
    "repository_rest_warnings": {
      "analysis_status": "analyzed",
      "warning_ids": []
    },
    "hidden_http_warnings": {
      "analysis_status": "analyzed",
      "warning_ids": []
    }
  }
}
```

API surface analysis status rules:

- `api_surface.analysis_status` is `"analyzed"` when any v0.4 API surface analyzer runs.
  It is `"not_detected"` only when no supported source, spec, build, generated-source
  path, or warning input is available.
- Endpoint category subsections use endpoint IDs that must resolve to existing
  `endpoints[]` facts. They must not duplicate endpoint payloads.
- `api_surface.openapi.spec_files.analysis_status` is `"analyzed"` when spec discovery
  runs, even if no spec files are detected. It is `"not_detected"` only when no
  supported discovery input exists.
- `api_surface.openapi.operations.analysis_status` is `"analyzed"` when parser
  extraction runs, including when supported spec files contain no usable operations or
  degrade to warnings, and `"not_detected"` when no supported local spec files are
  available to parse.
- Warning-reference subsections use warning IDs that must resolve to `warnings.items`.
  They must not duplicate warning payloads or create operation/endpoint facts.

OpenAPI spec file rules:

- Spec file fact IDs use the shape `openapi_spec:<module_id-or-unscoped>:path:<spec_path_key>`.
- `module_id` is the owning supported module ID when the spec path is inside a
  supported module, and `null` when the spec is outside supported modules. Fact IDs use
  `unscoped` for `null` module ownership.
- `spec_path_key` is derived from normalized `spec_path`. It preserves case and slash
  separators, and uses uppercase UTF-8 byte percent-encoding for `%`, `:`, whitespace,
  ASCII control characters, and any other character outside the bounded readable key set
  `A-Z`, `a-z`, `0-9`, `.`, `_`, `-`, `~`, `/`, `{`, and `}`.
- `spec_path` is a normalized repository-relative path. It must not be absolute, start
  with `./`, or escape the scanned repository root.
- Spec discovery treats symlink path entries as unsupported for spec facts to avoid
  content/path ownership mismatches. A regular target file inside the repository may be
  discovered only through its own normalized repository-relative path.
- `format` is one of `"yaml"` or `"json"` for currently supported filenames.
- `spec_kind` is `"openapi"` or `"swagger"` based on bounded local spec header content
  when directly visible, with a filename fallback when no bounded version signal is
  detected.
- `version` preserves the direct source-visible OpenAPI or Swagger version string when
  deterministically available and is `null` otherwise.
- Spec file facts prove only local spec presence and bounded version/kind observations.
  They do not prove runtime APIs, OpenAPI operations, or generated code.

OpenAPI operation rules:

- Operation fact IDs use the shape
  `openapi_operation:<module_id>:spec:<spec_path_key>:operation:<http_method_key>:<operation_path_key>`.
- `operation_path_key` is derived from the declared OpenAPI/Swagger operation `path` with
  the same ID-key escaping as `spec_path_key`.
- `http_method_key` is the lowercase normalized HTTP method. It is used only for stable
  ID construction; the public `http_method` field uses the normalized display value.
- Valid OpenAPI/Swagger input must not produce more than one operation fact for the same
  spec path, HTTP method, and operation path. If duplicate declarations cannot be
  represented without an ID collision, the analyzer must degrade the duplicate condition
  to a warning instead of emitting colliding operation facts.
- Operation facts use `api_surface_category: "openapi_declared_operation"`.
- `http_method` is the normalized HTTP method declared under a spec path item.
- `path` is the declared OpenAPI/Swagger path template, not a Spring MVC path.
- `operation_id` is the direct `operationId` value when present and bounded, otherwise
  `null`.
- `tags` contains bounded direct tag strings when present and is an empty array when no
  tags are present or deterministically usable. The initial implementation preserves up
  to eight direct string tags of up to 120 characters each.
- Operation `operation_id` values longer than the bounded analyzer limit are emitted as
  `null` rather than serializing unbounded source-derived strings.
- `implementation_status` is `"not_analyzed"` in the initial v0.4 operation extraction
  contract. A spec operation must not be treated as implemented merely because a similar
  Spring MVC endpoint exists.
- Future endpoint/spec matching must use a separate relation that preserves both spec
  evidence and code evidence and labels support type, confidence, and uncertainty.
- Invalid or unsupported specs should degrade to warnings rather than crashing a scan or
  producing partial operation claims.
- Operation facts must not follow external `$ref` values, perform network access, fetch
  remote schemas, run code generation, or reconstruct client SDKs.
- Invalid, unsupported, oversized, or duplicate operation parser inputs may emit
  `hidden_http_surface` warnings such as `openapi_spec_parse_error`,
  `openapi_spec_unsupported`, or `openapi_spec_duplicate_operation` with bounded
  `api_spec` parse/status evidence.

Generated-source API signal rules:

- Generated-source API signals remain warnings. They may be referenced by
  `api_surface.generated_source_api_signals.warning_ids`.
- Build/config-derived generator signals use `build_file` evidence and warning
  categories. Path-derived generated-source root signals use `path_signal` evidence.
- API-surface generated-source warning references may include OpenAPI/Swagger generator
  plugin warnings, matching OpenAPI generator output configuration warnings, and
  generated-source root path warnings. They must not include generic annotation-processor
  warnings unless a future contract defines them as API-related.
- A generated-source path warning proves only normalized local path presence. It does
  not prove generated Java types, generated operations, generated endpoint handlers, or
  runtime behavior.
- The default scan must not read generated source roots such as `target/generated-sources`.
  Any future generated-source scan mode must be explicit, documented, non-default, and
  introduced with a separate output/evidence contract update.

Planned warning separation rules:

- `repository_rest_warning` must be separate from generic `hidden_http_warning` because
  it is a direct Spring Data REST annotation signal.
- `hidden_http_warning` is reserved for unknown, unsupported, invalid, or otherwise
  non-modeled HTTP/API-adjacent signals.
- Warning messages must use detected-signal wording and must not summarize generated
  source contents, OpenAPI schemas, examples, arbitrary descriptions, runtime build
  behavior, or effective Maven execution.

Deterministic sorting rules:

- Endpoint category ID lists follow existing endpoint sort order.
- Spec files are sorted by module order, `spec_path`, `spec_kind`, `format`, and `id`.
- OpenAPI operations are sorted by module order, `spec_path`, `path`, `http_method`,
  `operation_id`, and `id`, with `null` values sorting after strings.
- API surface warning ID lists follow the existing warning sort order for their
  referenced warning items.

### Current v0.5 Spring Application Surface Contract

This section defines the v0.5 Spring application surface contract. The current
implementation emits repository signals, configuration/bean/configuration-properties
signals, transaction/scheduled/event/messaging signals, and Spring Security
configuration warnings. Later work must not change the meaning of the repository,
configuration, behavior, messaging, or security-warning slices without updating this
contract and the evidence model where applicable.

The v0.5 contract uses:

- `schema_version: "0.5"` for an atomic public output state that preserves the v0.4
  module-aware build/config and API surface contract and adds the Spring application
  surface section shape.
- The same four output files under `.project-memory/`.
- A top-level `spring_application_surface` object that groups deeper Spring surface
  facts and warning references without changing the meaning of existing `components`,
  `entities`, `tests`, `endpoints`, `warnings`, or `api_surface` sections.
- Existing evidence fields and evidence types. Direct annotation-backed facts reuse
  `annotation` evidence; source-visible Java structural observations reuse
  `code_symbol` evidence; security warnings reuse `annotation` and `code_symbol`
  evidence.
- Direct `@Repository` and `@Configuration` observations may appear both as existing
  component stereotype facts and as category-specific Spring application surface items.
  This is two contract views over the same source observation, not evidence of multiple
  runtime beans or component registrations.
- In the current implementation, `repositories.analysis_status`,
  `configuration.*.analysis_status`, `behavior.*.analysis_status`, and
  `messaging.listener_signals.analysis_status` are `"analyzed"` when supported
  production source roots exist and their analyzers run. The
  `security.configuration_warnings.analysis_status` value is also `"analyzed"` when
  supported production source roots exist and the security warning analyzer runs.

Current high-level `project-map.json` shape:

```json
{
  "schema_version": "0.5",
  "spring_application_surface": {
    "analysis_status": "analyzed",
    "repositories": {
      "analysis_status": "analyzed",
      "items": [
        {
          "id": "spring_repository_stereotype:module:services/orders:com.example.orders.DirectOrderRepository",
          "module_id": "module:services/orders",
          "surface_category": "spring_repository_stereotype",
          "support_type": "extracted",
          "class_name": "com.example.orders.DirectOrderRepository",
          "source_path": "services/orders/src/main/java/com/example/orders/DirectOrderRepository.java",
          "repository_signal": "direct_repository_stereotype",
          "evidence_ids": [
            "ev:services/orders/src/main/java/com/example/orders/DirectOrderRepository.java:8-8:com.example.orders.DirectOrderRepository:@Repository"
          ]
        },
        {
          "id": "spring_data_repository_interface_signal:module:services/orders:com.example.orders.OrderRepository",
          "module_id": "module:services/orders",
          "surface_category": "spring_data_repository_interface_signal",
          "support_type": "inferred",
          "class_name": "com.example.orders.OrderRepository",
          "source_path": "services/orders/src/main/java/com/example/orders/OrderRepository.java",
          "repository_signal": "spring_data_repository_interface_extension",
          "extends_types": [
            "org.springframework.data.jpa.repository.JpaRepository"
          ],
          "entity_relation_status": "not_analyzed",
          "evidence_ids": [
            "ev:services/orders/src/main/java/com/example/orders/OrderRepository.java:8-8:com.example.orders.OrderRepository:com.example.orders.OrderRepository",
            "ev:services/orders/src/main/java/com/example/orders/OrderRepository.java:8-8:com.example.orders.OrderRepository:extends:org.springframework.data.jpa.repository.JpaRepository"
          ]
        }
      ]
    },
    "configuration": {
      "configuration_classes": {
        "analysis_status": "analyzed",
        "items": [
          {
            "id": "spring_configuration_class:module:services/orders:com.example.orders.OrderConfiguration",
            "module_id": "module:services/orders",
            "surface_category": "spring_configuration_class",
            "support_type": "extracted",
            "class_name": "com.example.orders.OrderConfiguration",
            "source_path": "services/orders/src/main/java/com/example/orders/OrderConfiguration.java",
            "configuration_signal": "direct_configuration_class",
            "evidence_ids": [
              "ev:services/orders/src/main/java/com/example/orders/OrderConfiguration.java:8-8:com.example.orders.OrderConfiguration:@Configuration"
            ]
          }
        ]
      },
      "configuration_properties": {
        "analysis_status": "analyzed",
        "items": [
          {
            "id": "spring_configuration_properties_type:module:services/orders:com.example.orders.OrderProperties",
            "module_id": "module:services/orders",
            "surface_category": "spring_configuration_properties_type",
            "support_type": "extracted",
            "class_name": "com.example.orders.OrderProperties",
            "source_path": "services/orders/src/main/java/com/example/orders/OrderProperties.java",
            "configuration_properties_signal": "direct_configuration_properties_type",
            "binding_status": "not_analyzed",
            "evidence_ids": [
              "ev:services/orders/src/main/java/com/example/orders/OrderProperties.java:8-8:com.example.orders.OrderProperties:@ConfigurationProperties"
            ]
          }
        ]
      },
      "bean_methods": {
        "analysis_status": "analyzed",
        "items": [
          {
            "id": "spring_bean_method:module:services/orders:com.example.orders.OrderConfiguration#orderClock:decl:000001",
            "module_id": "module:services/orders",
            "surface_category": "spring_bean_method",
            "support_type": "extracted",
            "class_name": "com.example.orders.OrderConfiguration",
            "method_name": "orderClock",
            "source_path": "services/orders/src/main/java/com/example/orders/OrderConfiguration.java",
            "bean_signal": "direct_bean_method",
            "bean_name_status": "not_analyzed",
            "evidence_ids": [
              "ev:services/orders/src/main/java/com/example/orders/OrderConfiguration.java:10-10:com.example.orders.OrderConfiguration#orderClock:@Bean"
            ]
          }
        ]
      }
    },
    "behavior": {
      "transaction_boundaries": {
        "analysis_status": "analyzed",
        "items": []
      },
      "scheduled_methods": {
        "analysis_status": "analyzed",
        "items": []
      },
      "event_listeners": {
        "analysis_status": "analyzed",
        "items": []
      }
    },
    "messaging": {
      "listener_signals": {
        "analysis_status": "analyzed",
        "items": []
      }
    },
    "security": {
      "configuration_warnings": {
        "analysis_status": "analyzed",
        "warning_ids": []
      }
    }
  }
}
```

Spring application surface taxonomy rules:

- `spring_repository_stereotype` means a direct source-visible `@Repository` annotation
  on a Java class or interface. It is an extracted fact and must not imply Spring Data
  repository behavior, query semantics, runtime bean registration, or entity ownership.
- `spring_data_repository_interface_signal` means a source-visible Java interface
  appears to extend a supported Spring Data repository base type. The current
  implementation supports `org.springframework.data.repository.Repository`,
  `org.springframework.data.repository.CrudRepository`,
  `org.springframework.data.repository.PagingAndSortingRepository`,
  `org.springframework.data.jpa.repository.JpaRepository`, and
  `org.springframework.data.mongodb.repository.MongoRepository` when visible through a
  fully qualified name or explicit single-type import. It is an inferred signal and
  must not imply runtime repository registration, resolved generic entity type, query
  method behavior, database access, or repository-to-entity relation.
- `spring_configuration_class` means a direct source-visible `@Configuration`
  annotation. It must not imply conditional activation, bean graph, component scan
  result, or auto-configuration behavior.
- `spring_configuration_properties_type` means a direct source-visible
  `@ConfigurationProperties` annotation. Optional bounded `prefix` or `value` fields, if
  implemented, are annotation literals only and must not imply runtime binding,
  environment values, active profiles, or config file values.
- `spring_bean_method` means a direct source-visible `@Bean` method. It must not imply
  an instantiated runtime bean, effective bean name, scope, lifecycle, proxy behavior, or
  dependency graph.
- `spring_transaction_boundary` means a direct source-visible `@Transactional`
  annotation on a class or method. It must not imply runtime proxy application,
  effective transaction manager, propagation semantics, isolation semantics, rollback
  behavior, or call graph effects.
- `spring_scheduled_method` means a direct source-visible `@Scheduled` method. It must
  not imply scheduler enablement, runtime registration, cron correctness, execution
  frequency, lock behavior, or cluster behavior.
- `spring_event_listener` means a direct source-visible Spring event listener annotation
  such as `@EventListener`. It must not imply event publication paths, listener ordering,
  transaction phase behavior, runtime event delivery, or call graph behavior.
- `messaging_listener_signal` means a direct source-visible messaging listener
  annotation for common Kafka and Rabbit listener annotations. It must
  not imply runtime broker topology, queue/topic existence, exchange bindings, consumer
  group membership, delivery semantics, or deployment configuration.
- `spring_security_configuration_warning` means a bounded source-visible Spring Security
  configuration signal. It lives in `warnings.items` and is referenced by
  `spring_application_surface.security.configuration_warnings.warning_ids`; it must not
  imply endpoint protection state, authentication provider behavior, authorization
  rules, filter-chain ordering, vulnerability, or security correctness.

Spring application surface field rules:

- `spring_application_surface.analysis_status` is `"analyzed"` when any v0.5 Spring
  surface analyzer runs. It is `"not_detected"` only when no supported production source
  input is available.
- Subsection `analysis_status` values are `"analyzed"` when their analyzer runs, even
  when their item or warning-reference collections are empty. They are `"not_detected"`
  only when no supported input exists for that subsection.
- In the v0.5 implementation state, repository, configuration, behavior,
  messaging, and security configuration warning subsections emit `"analyzed"` when
  supported production source roots exist and their analyzers run.
- `surface_category` uses one of the v0.5 taxonomy values. Warning-reference
  containers do not duplicate warning payloads or use `surface_category`.
- Item `support_type` is `"extracted"` for direct source-visible facts and `"inferred"`
  for source-visible signals derived from structure or conventions.
- All module-owned Spring surface items include direct `module_id` fields.
- Current repository item IDs are stable:
  `spring_repository_stereotype:<module_id>:<class_name>` for direct `@Repository`
  observations and
  `spring_data_repository_interface_signal:<module_id>:<class_name>` for inferred
  Spring Data repository interface extension signals.
- Current configuration item IDs are stable:
  `spring_configuration_class:<module_id>:<class_name>` for direct `@Configuration`
  observations,
  `spring_configuration_properties_type:<module_id>:<class_name>` for direct
  `@ConfigurationProperties` observations, and
  `spring_bean_method:<module_id>:<class_name>#<method_name>:decl:<ordinal>` for direct
  `@Bean` method observations. The zero-padded declaration ordinal disambiguates
  source-visible `@Bean` method facts and is not a bean name, dependency relation, or
  runtime identity claim.
- Current behavior and messaging item IDs are stable:
  `spring_transaction_boundary:<module_id>:<class_name>:type` for direct type-level
  `@Transactional` observations,
  `spring_transaction_boundary:<module_id>:<class_name>#<method_name>:decl:<ordinal>`
  for direct method-level `@Transactional` observations,
  `spring_scheduled_method:<module_id>:<class_name>#<method_name>:decl:<ordinal>` for
  direct `@Scheduled` method observations,
  `spring_event_listener:<module_id>:<class_name>#<method_name>:decl:<ordinal>` for
  direct `@EventListener` method observations, and
  `messaging_listener_signal:<module_id>:<class_name>[:#<method_name>]:annotation:<annotation_name>:decl:<ordinal>`
  for direct Kafka/Rabbit listener annotation observations. The zero-padded declaration
  ordinal disambiguates source-visible declarations and is not a runtime listener,
  scheduler, transaction, destination, or broker identity claim.
- Source paths are normalized repository-relative paths and must not be absolute, start
  with `./`, or escape the scanned repository root.
- `extends_types` preserves bounded source-visible Spring Data base type observations
  only. It must not imply classpath resolution, entity relation, or runtime repository
  creation.
- `entity_relation_status: "not_analyzed"` is required for v0.5 Spring Data repository
  interface signals where a reader might otherwise assume repository-to-entity mapping.
- `configuration_signal` is `"direct_configuration_class"` for current direct
  `@Configuration` facts.
- `configuration_properties_signal` is `"direct_configuration_properties_type"` for
  current direct `@ConfigurationProperties` facts.
- `binding_status: "not_analyzed"` is required for current configuration-properties
  facts. The current implementation does not emit `prefix` or `value` fields and does
  not extract configuration file values, active profiles, environment values, validation
  state, or runtime binding success.
- `bean_signal` is `"direct_bean_method"` for current direct `@Bean` method facts.
- `bean_name_status` is `"not_analyzed"` for current bean method facts. Future bounded
  source-visible `@Bean` name extraction would require a separate design; emitted names
  would remain annotation literals, not runtime bean names.
- Transaction facts use `transaction_signal` values `"direct_transactional_type"` and
  `"direct_transactional_method"`, include `target_kind` (`"type"` or `"method"`),
  include `annotation_symbol: "@Transactional"`, and never emit propagation, isolation,
  rollback, transaction-manager, proxy, or call-graph fields.
- Scheduled method facts use `scheduled_signal: "direct_scheduled_method"`, include
  `target_kind: "method"`, include `annotation_symbol: "@Scheduled"`, and never emit
  cron, fixed-rate, fixed-delay, scheduler, lock, registration, frequency, cluster, or
  runtime execution fields.
- Event listener facts use `event_listener_signal: "direct_event_listener_method"`,
  include `target_kind: "method"`, include `annotation_symbol: "@EventListener"`, and
  never emit event publication paths, listener ordering, transaction phase, delivery, or
  call-graph fields.
- Messaging listener facts support direct source-visible Spring Kafka
  `@KafkaListener`/`@KafkaListeners` and Spring AMQP Rabbit
  `@RabbitListener`/`@RabbitListeners` annotations. They include `target_kind`,
  `annotation_symbol`, `listener_framework` (`"kafka"` or `"rabbit"`), and
  `listener_signal` (`"direct_kafka_listener_annotation"` or
  `"direct_rabbit_listener_annotation"`). They do not emit topic, queue, exchange,
  routing-key, group-id, broker, binding, consumer-group, delivery, or deployment fields.

Current security warning rules:

- Spring Security configuration warnings use `category: "spring_security"`.
- Warning `signal` values include `"security_configuration_annotation"` and
  `"security_filter_chain_bean"` when directly source-visible.
- Current supported security configuration annotations are
  `org.springframework.security.config.annotation.web.configuration.EnableWebSecurity`,
  `org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity`,
  `org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity`,
  `org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity`,
  and
  `org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity`
  when visible through a fully qualified name or explicit single-type import.
- Current `SecurityFilterChain` `@Bean` warnings require both a trusted
  `org.springframework.context.annotation.Bean` annotation and a return type visible as
  `org.springframework.security.web.SecurityFilterChain` through a fully qualified name
  or explicit single-type import.
- Security warning IDs are stable. Scan-root warnings use
  `warning:spring_security:<signal>:<target>`; child-module warnings use
  `warning:spring_security:<signal>:module:<module_path>:<target>`. Current targets are
  `<class_name>:annotation:<annotation_discriminator>:decl:<ordinal>` for supported
  security annotations and `<class_name>#<method_name>:decl:<ordinal>` for
  `SecurityFilterChain` `@Bean` methods. The zero-padded ordinal disambiguates
  source-visible matching declarations and is not a runtime security-chain identity.
- Warning messages must use detected-signal wording. They must not claim endpoint
  protection, authentication behavior, authorization behavior, runtime filter order,
  vulnerability, or policy correctness.

Deterministic sorting rules:

- Spring surface item lists sort by module order, source path, class name, method name
  when present, `surface_category`, and `id`.
- Security warning ID lists follow the existing warning sort order for their referenced
  warning items.

### v0.6 JPA And Domain Contract

This section defines the v0.6 JPA/domain output contract. The current V060-G002
implementation moves normal generated output to `schema_version: "0.6"` and implements
the bounded entity field annotation slice for direct field-level `@Column`,
`@Enumerated`, `@GeneratedValue`, and `@Version`. Later v0.6 goals may fill the planned
table metadata, embedded/composite identifier, relationship metadata, and
repository/entity relation portions described below.

The v0.6 contract uses:

- `schema_version: "0.6"` for an atomic public output state that preserves the v0.5
  module-aware build/config, API surface, and Spring application surface contracts while
  deepening the existing JPA/domain model.
- The same four output files under `.project-memory/`.
- The existing top-level `entities` object as the owner of source-visible JPA entity,
  embeddable, field, identifier, and relationship facts. v0.6 does not add a database
  schema file or a runtime ORM model file.
- Existing evidence fields and evidence types. Direct JPA annotations reuse
  `annotation` evidence; source-visible Java type declarations and generic type
  observations reuse `code_symbol` evidence.
- Repository/entity links remain attached to v0.5
  `spring_application_surface.repositories.items[]` because they refine inferred Spring
  Data repository interface signals. They are inferred relations, not extracted entity
  facts.

Current V060-G002 implementation state:

- `schema_version` is `"0.6"`.
- `entities.items[]` continues to emit existing entity, table compatibility,
  identifier, and relationship fields.
- Each entity object emits `fields`, sorted deterministically, as a possibly empty
  array.
- `fields[]` currently contains only direct field-level `@Column`, `@Enumerated`,
  `@GeneratedValue`, and `@Version` metadata declared on the entity class. Getter or
  property-access annotations are not emitted in this slice.
- `identifier_fields[]` now emits `identifier_kind: "simple_id"` for supported simple
  `@Id` facts and a nullable `generated_value` object when a direct field-level
  `@GeneratedValue` annotation is present on that identifier field.
- Missing annotation attributes remain `null`; generated output must not fill JPA
  runtime defaults.
- `table_metadata`, `id_class`, `entities.embeddables`, field `embedded`, relationship
  metadata deepening, and repository/entity relation fields are planned for later v0.6
  goals and are not emitted by V060-G002.

Full-track planned `project-map.json` excerpt. Unchanged v0.5 fields are omitted from
some objects for focus, but remain required by their existing contracts when those
objects are emitted. Fields explicitly listed as planned later are not current
V060-G002 output until their implementation goals land:

```json
{
  "schema_version": "0.6",
  "entities": {
    "analysis_status": "analyzed",
    "items": [
      {
        "id": "entity:module:services/orders:com.example.orders.Order",
        "module_id": "module:services/orders",
        "class_name": "com.example.orders.Order",
        "source_path": "services/orders/src/main/java/com/example/orders/Order.java",
        "table_name": "orders",
        "table_metadata": {
          "name": "orders",
          "schema": "sales",
          "catalog": null,
          "evidence_ids": [
            "ev:services/orders/src/main/java/com/example/orders/Order.java:12-12:com.example.orders.Order:@Table"
          ]
        },
        "id_class": null,
        "fields": [
          {
            "field_name": "status",
            "java_type": "OrderStatus",
            "declaring_class": "com.example.orders.Order",
            "source_kind": "declared",
            "persistence_role": "basic",
            "annotations": ["@Column", "@Enumerated"],
            "column": {
              "name": "status",
              "nullable": false,
              "unique": null,
              "length": null,
              "precision": null,
              "scale": null,
              "insertable": null,
              "updatable": null,
              "evidence_ids": [
                "ev:services/orders/src/main/java/com/example/orders/Order.java:20-20:com.example.orders.Order:@Column:field:status"
              ]
            },
            "enumerated": {
              "value": "EnumType.STRING",
              "evidence_ids": [
                "ev:services/orders/src/main/java/com/example/orders/Order.java:21-21:com.example.orders.Order:@Enumerated:field:status"
              ]
            },
            "generated_value": null,
            "version": null,
            "embedded": null,
            "evidence_ids": [
              "ev:services/orders/src/main/java/com/example/orders/Order.java:20-20:com.example.orders.Order:@Column:field:status",
              "ev:services/orders/src/main/java/com/example/orders/Order.java:21-21:com.example.orders.Order:@Enumerated:field:status"
            ]
          }
        ],
        "identifier_fields": [
          {
            "field_name": "id",
            "java_type": "Long",
            "declaring_class": "com.example.orders.Order",
            "source_kind": "declared",
            "identifier_kind": "simple_id",
            "generated_value": {
              "strategy": "GenerationType.IDENTITY",
              "generator": null,
              "evidence_ids": [
                "ev:services/orders/src/main/java/com/example/orders/Order.java:16-16:com.example.orders.Order:@GeneratedValue:field:id"
              ]
            },
            "evidence_ids": [
              "ev:services/orders/src/main/java/com/example/orders/Order.java:15-15:com.example.orders.Order:@Id:field:id",
              "ev:services/orders/src/main/java/com/example/orders/Order.java:16-16:com.example.orders.Order:@GeneratedValue:field:id"
            ]
          }
        ],
        "relationships": [
          {
            "field_name": "customer",
            "annotation": "@ManyToOne",
            "cardinality": "many_to_one",
            "java_type": "Customer",
            "target": {
              "declared_type": "Customer",
              "target_resolution": "declared_type_only",
              "target_entity_id": null,
              "target_module_id": null,
              "target_class_name": null,
              "support_type": null,
              "confidence": null,
              "uncertainty": "target_type_not_resolved",
              "evidence_ids": []
            },
            "mapped_by": null,
            "ownership_signal": "mapped_by_absent",
            "optional": false,
            "fetch": "FetchType.LAZY",
            "cascade": [],
            "orphan_removal": null,
            "join_columns": [
              {
                "name": "customer_id",
                "referenced_column_name": null,
                "nullable": false,
                "unique": null,
                "insertable": null,
                "updatable": null,
                "evidence_ids": [
                  "ev:services/orders/src/main/java/com/example/orders/Order.java:30-30:com.example.orders.Order:@JoinColumn:field:customer"
                ]
              }
            ],
            "join_table": null,
            "evidence_ids": [
              "ev:services/orders/src/main/java/com/example/orders/Order.java:29-29:com.example.orders.Order:@ManyToOne:field:customer",
              "ev:services/orders/src/main/java/com/example/orders/Order.java:30-30:com.example.orders.Order:@JoinColumn:field:customer"
            ]
          }
        ],
        "evidence_ids": [
          "ev:services/orders/src/main/java/com/example/orders/Order.java:11-11:com.example.orders.Order:@Entity",
          "ev:services/orders/src/main/java/com/example/orders/Order.java:12-12:com.example.orders.Order:@Table"
        ]
      }
    ],
    "embeddables": {
      "analysis_status": "analyzed",
      "items": [
        {
          "id": "embeddable:module:services/orders:com.example.orders.OrderId",
          "module_id": "module:services/orders",
          "class_name": "com.example.orders.OrderId",
          "source_path": "services/orders/src/main/java/com/example/orders/OrderId.java",
          "fields": [],
          "evidence_ids": [
            "ev:services/orders/src/main/java/com/example/orders/OrderId.java:8-8:com.example.orders.OrderId:@Embeddable"
          ]
        }
      ]
    }
  },
  "spring_application_surface": {
    "repositories": {
      "items": [
        {
          "id": "spring_data_repository_interface_signal:module:services/orders:com.example.orders.OrderRepository",
          "entity_relation_status": "inferred",
          "entity_relation": {
            "support_type": "inferred",
            "relation_type": "repository_entity_generic",
            "target_entity_id": "entity:module:services/orders:com.example.orders.Order",
            "target_module_id": "module:services/orders",
            "target_class_name": "com.example.orders.Order",
            "generic_type": "com.example.orders.Order",
            "confidence": "medium",
            "uncertainty": null,
            "evidence_ids": [
              "ev:services/orders/src/main/java/com/example/orders/OrderRepository.java:8-8:com.example.orders.OrderRepository:extends:org.springframework.data.jpa.repository.JpaRepository",
              "ev:services/orders/src/main/java/com/example/orders/Order.java:11-11:com.example.orders.Order:@Entity"
            ]
          }
        }
      ]
    }
  }
}
```

v0.6 entity and embeddable rules:

- `entities.analysis_status` remains `"analyzed"` when supported production source roots
  exist and the JPA/domain analyzer runs. `entities.embeddables.analysis_status` follows
  the same rule for `@Embeddable` detection.
- `entities.items[]` continues to contain direct source-visible `@Entity` facts.
  `entities.embeddables.items[]` contains direct source-visible `@Embeddable` facts and
  must not be described as entity/table facts.
- Existing entity IDs remain stable for root-module compatibility. Child module entity
  IDs include `module_id` as in the existing v0.2 module-aware fact-ID rule.
- `entity.table_name` remains the compatibility string for direct
  `@Table(name = "...")`. `entity.table_metadata` is a planned structured
  source-visible `@Table` view with optional `name`, `schema`, and `catalog` values when
  directly extractable. It must not imply that a table exists in any database.
- `entity.fields[]` contains source-visible field metadata for supported direct JPA
  annotations. It is not a complete persistent-property inventory. Fields with no
  supported JPA annotation do not have to be emitted.
- Current field metadata supports direct field-level `@Column`, `@Enumerated`,
  `@GeneratedValue`, and `@Version` on entity classes. Planned later field metadata may
  add `@Embedded`, `@EmbeddedId`, relationship annotations, `@JoinColumn`, and
  `@JoinTable`. Getter/property-access support is a separate bounded implementation
  choice; if added, it must preserve a distinct member kind and evidence for the
  annotated method without pretending it was a field declaration.
- `field.persistence_role` is a source-visible classification such as `"basic"`,
  `"simple_id"`, `"embedded"`, `"embedded_id"`, `"version"`, or `"relationship"`. It is
  not a runtime access strategy or schema role claim.
- `field.annotations` lists only supported direct JPA annotation symbols detected on
  that field. It must not include classpath-only, wildcard-only, unresolved, generated,
  or source-declared fake annotations.
- `field.column` records only source-visible direct `@Column` literal attributes chosen
  by the implementation, such as `name`, `nullable`, `unique`, `length`, `precision`,
  `scale`, `insertable`, and `updatable`. Missing attributes remain `null`; the analyzer
  must not fill JPA defaults.
- `field.enumerated` records only direct `@Enumerated` source-visible enum/literal
  values, such as `EnumType.STRING`; it must not infer enum storage when the annotation
  is absent or unsupported.
- `field.generated_value` records only direct `@GeneratedValue` source-visible
  `strategy` and `generator` literals. It must not claim generated identifier behavior,
  sequence/table existence, database identity behavior, or provider defaults.
- `field.version` records direct `@Version` presence and evidence only. It must not
  claim optimistic-locking correctness or runtime version behavior.
- `field.embedded` is planned to record direct `@Embedded` or `@EmbeddedId` presence and the declared
  Java type. A source-visible `@Embeddable` target may be linked only when the type can
  be matched deterministically to a unique emitted embeddable fact; otherwise it remains
  declared-type-only with explicit uncertainty.
- `entity.identifier_fields[]` keeps existing simple `@Id` support and may add
  `identifier_kind` values such as `"simple_id"`, `"embedded_id"`, and
  `"id_class_field"`. Mapped-superclass identifiers keep `source_kind:
  "mapped_superclass"` and the existing conservative hierarchy boundary.
- `entity.id_class` records direct class-level `@IdClass` source-visible type literals
  when present. It is a composite-id signal only. It must not reconstruct field matching,
  equality semantics, serializability, generated keys, provider behavior, or database
  primary keys.
- `entities.embeddables.items[].fields[]` uses the same supported field metadata shape
  as entity fields where applicable. Embeddable field facts must not imply that an
  embeddable is used by any entity unless a separate `@Embedded` or `@EmbeddedId` fact
  supports that relation.

Planned v0.6 relationship rules:

- `entity.relationships[]` remains the relationship fact list for direct field-level
  `@ManyToOne`, `@OneToMany`, `@OneToOne`, and `@ManyToMany` annotations.
- `relationship.cardinality` is derived only from the direct relationship annotation:
  `"many_to_one"`, `"one_to_many"`, `"one_to_one"`, or `"many_to_many"`.
- `relationship.java_type` preserves the declared Java field type string. It is not a
  database type, table name, or guaranteed entity target.
- `relationship.target.target_resolution` is `"declared_type_only"` when only the
  declared field type is preserved, `"source_visible_entity"` only when a unique
  emitted entity fact is deterministically matched, and `"ambiguous"` when source-visible
  candidates cannot be reduced to one target. Target links are inferred relation
  support, not extracted annotation facts.
- `relationship.target.support_type` is `"inferred"` only when
  `target_resolution: "source_visible_entity"`; otherwise it is `null`.
- `relationship.target.uncertainty` must preserve uncertainty values such as
  `"target_type_not_resolved"`, `"ambiguous_target_type"`, or
  `"unsupported_collection_type"` when a target link cannot be made conservatively.
- `relationship.mapped_by` records only directly visible `mappedBy` string literals.
  Unsupported expressions or absent attributes must not be converted to runtime defaults.
- `relationship.ownership_signal` is a source-visible orientation signal, not a runtime
  ORM ownership guarantee. Allowed planned values include `"mapped_by_present"`,
  `"mapped_by_absent"`, `"join_metadata_present"`, and `"not_analyzed"`.
- `relationship.optional`, `fetch`, `cascade`, and `orphan_removal` record only directly
  visible annotation attributes chosen by the implementation. Missing values remain
  `null` or empty arrays and must not be filled from JPA defaults.
- `relationship.join_columns[]` records bounded source-visible `@JoinColumn` metadata
  such as `name`, `referenced_column_name`, `nullable`, `unique`, `insertable`, and
  `updatable`. It must not reconstruct foreign keys, indexes, constraints, or database
  columns.
- `relationship.join_table` records bounded source-visible `@JoinTable` metadata such
  as `name`, `schema`, `catalog`, `join_columns`, and `inverse_join_columns` when
  directly extractable. It must not reconstruct join tables or migration state.

Planned v0.6 repository/entity relation rules:

- v0.5 `spring_data_repository_interface_signal` items continue to be inferred Spring
  Data interface signals. v0.6 may replace `entity_relation_status: "not_analyzed"` with
  a conservative relation status only when the repository/entity relation analyzer runs.
- Planned `entity_relation_status` values are:
  - `"inferred"`: a supported source-visible Spring Data repository generic type can be
    matched to exactly one emitted entity fact.
  - `"not_detected"`: the analyzer ran but did not find a supported source-visible
    entity generic or matching entity fact.
  - `"ambiguous"`: the analyzer found multiple possible source-visible entity matches.
  - `"unsupported"`: the generic shape is source-visible but outside the supported
    bounded parser, such as nested, wildcard, raw, or unresolved generic forms.
  - `"not_analyzed"`: compatibility value used only when this relation analyzer did not
    run for the item.
- `entity_relation` is non-null only when `entity_relation_status` is `"inferred"`.
  It must include `support_type: "inferred"`, `relation_type:
  "repository_entity_generic"`, target entity identity, the source-visible generic type
  string, confidence, uncertainty, and evidence IDs for both repository-side generic
  evidence and target entity evidence.
- All unchanged v0.5 repository item fields remain required when v0.6 adds
  `entity_relation_status` and `entity_relation` relation fields.
- Inferred repository/entity relations must not be emitted for direct `@Repository`
  stereotype facts unless they also have a separate supported Spring Data repository
  interface signal.
- Repository/entity relations must not use runtime Spring Data registration, query
  method parsing, JPQL semantics, database access, dependency graphs, classpath solving,
  Hibernate metadata, or migration files as evidence.

Planned v0.6 deterministic sorting rules:

- Entity fields sort by module order, declaring class, source kind, field name, Java
  type, persistence role, and ID/evidence discriminator where needed.
- Identifier fields keep the existing deterministic order and then sort by
  `identifier_kind`.
- Relationships sort by module order, declaring class, field name, cardinality,
  annotation, Java type, and ID/evidence discriminator where needed.
- Embeddables sort by module order, class name, source path, and ID.
- Repository/entity relation status does not change repository item sort order.

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
- `annotation` evidence for direct `@Repository` Spring application surface repository
  stereotype facts. When the same source annotation also supports a component fact, the
  evidence ID resolves to the same `evidence-index.jsonl` record.
- `code_symbol` evidence for inferred Spring Data repository interface extension
  signals. The current repository slice emits one evidence record for the source-visible
  interface declaration and one `extends:<fully-qualified-spring-data-base-type>`
  evidence record for each supported directly visible Spring Data base type that led to
  the signal.
- `annotation` evidence for direct `@Configuration`, `@ConfigurationProperties`, and
  `@Bean` Spring application surface facts. When the same source-visible
  `@Configuration` annotation also supports a component fact, both facts reference the
  same evidence ID and `evidence-index.jsonl` emits a single record. Current
  configuration-properties facts do not emit `prefix` or `value` fields, and current
  bean method facts do not emit bean names, scopes, lifecycle, return type, parameter, or
  dependency graph facts.
- `annotation` evidence for direct `@Transactional`, `@Scheduled`, `@EventListener`,
  and Kafka/Rabbit listener annotation Spring application surface facts. Current
  behavior/messaging evidence excerpts record annotation symbols only for these facts
  and do not serialize destination-like messaging annotation values such as topics,
  queues, exchanges, routing keys, or group IDs.
- `annotation` evidence for direct JPA annotations that support entity facts, including
  class-level `@Entity`, class-level `@Table`, field-level `@Id`, and field-level
  `@Column`, `@Enumerated`, `@GeneratedValue`, `@Version`, and relationship annotations
  `@ManyToOne`, `@OneToMany`, `@OneToOne`, and `@ManyToMany`.
  Field-level evidence IDs include a `field:<field_name>` discriminator because the
  current evidence record field set does not add a separate field-name property.
- `test_file` evidence for emitted test-like Java class declarations under supported
  test roots.
- `code_symbol` evidence for production class declarations that are referenced by
  inferred `tested_subjects` relations.
- `code_symbol` evidence for directly visible test framework imports attached to
  top-level emitted test classes.
- `annotation` evidence for directly visible test framework annotations.
- `api_spec` evidence for local OpenAPI/Swagger spec file facts and extracted operation
  facts. The evidence path is the normalized repository-relative spec path,
  `symbol_name` is the bounded spec observation such as `openapi` or `swagger`, an
  operation symbol such as `operation:get:/orders/{id}`, or a bounded parser status
  symbol for invalid/unsupported specs. Line fields point to the directly visible
  version signal or operation line when available and are `null` when stable line mapping
  is unavailable. Excerpts are bounded observations rather than full spec content.
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
- The current v0.3 Maven metadata analyzer also reuses `build_file` evidence for
  direct source-visible module metadata and parent coordinate elements. Metadata evidence
  uses `symbol_name` values such as `maven:project:artifactId` or
  `maven:parent:version`, points to the module POM path, and supports only the direct POM
  text. It does not prove Maven defaults, inherited coordinates, profile activation, or
  effective POM values.
- The current v0.3 dependency analyzer reuses `build_file` evidence for direct
  source-visible dependency declarations, dependency-management declarations, and their
  directly declared coordinate, `scope`, `optional`, `type`, and `classifier` elements.
  Dependency evidence uses `symbol_name` values such as
  `maven:dependency:000001:artifactId` or
  `maven:dependency_management:000001:version`, points to the module POM path, and
  supports only direct POM text. It does not prove resolved versions, inherited or
  managed values, transitive dependencies, active profiles, repository availability, or
  effective dependency graphs.
- The current v0.3 plugin analyzer reuses `build_file` evidence for direct
  source-visible plugin declarations, plugin-management declarations, directly declared
  plugin coordinates, direct execution IDs/phases/goals, bounded configuration signal
  elements, and plugin-derived generator signals. Plugin declaration and execution
  evidence excerpts identify the bounded declaration only; bounded configuration signal
  evidence excerpts identify the signal element name and must not include arbitrary
  plugin configuration values. Plugin evidence does not prove resolved plugin versions,
  Maven lifecycle execution, inherited executions, generated source contents, OpenAPI
  operations, endpoint facts, active profiles, repository availability, or effective POM
  behavior.
- v0.4 API surface evidence adds `api_spec` evidence for local OpenAPI/Swagger spec
  files and extracted operation facts, plus `path_signal` evidence for generated-source
  path signals. Current `api_spec` evidence supports local declared API input and
  operation facts only; it does not prove source-visible endpoint implementation,
  generated source contents, or runtime behavior. `path_signal` evidence supports
  path-presence warnings only; it will not prove generated source contents or generated
  API operations. `api_spec`
  evidence IDs use
  `ev:<spec_path_key>:<line_range_key>:api_spec:<api_spec_symbol_key>`, where
  `line_range_key` is `<line_start>-<line_end>` when stable and `unknown` otherwise, and
  `api_spec_symbol_key` uses the same percent-encoded ID-key rules as v0.4 spec and
  operation fact IDs. Duplicate evidence ID collisions must be resolved with a
  deterministic `decl:<zero-padded-ordinal>` suffix or degraded to warnings.

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

Current v0.4 `endpoints.md` behavior:

- The filename remains `endpoints.md`, but the content renders distinct API
  surface sections.
- Source-visible Spring MVC endpoint entries render from top-level `endpoints[]`.
- Direct handler mappings and source-visible interface-declared mappings are
  visibly separated or labeled by `api_surface_category` and `mapping_source.kind`.
- Declared OpenAPI operations render from `api_surface.openapi.operations.items[]` under
  a separate `Declared OpenAPI Operations` section.
- OpenAPI operation entries must use `Declared` or `Spec-backed` wording. They must not
  use `Detected endpoint`, `Implemented`, or other wording that implies runtime handler
  implementation.
- Generated-source API signals, repository-rest warnings, and hidden HTTP warnings
  render as warnings with category, signal, source path, and evidence references. They
  must not be rendered as endpoint or operation facts.
- A future endpoint/spec relation, if introduced, must be rendered as a separate
  evidence-backed relation and must not merge source-visible endpoint rows with
  spec-backed operation rows.

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
  modules. It should also call out that generated sources, OpenAPI operations, generated API
  reconstruction, classpath-only interfaces, and ambiguous interface endpoint bindings
  are not analyzed for the v0.1 interface-mapping decision, and that mapped-superclass identifier
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

Current v0.3 build/config behavior:

- `project-map.json` includes module-owned Maven metadata under
  `project.modules.items[].build_config.maven.metadata`.
- `project-map.json` includes module-owned source-visible Maven dependency declarations
  under `project.modules.items[].build_config.maven.dependencies` and separate
  management declarations under
  `project.modules.items[].build_config.maven.dependency_management`.
- `project-map.json` includes module-owned source-visible Maven plugin declarations
  under `project.modules.items[].build_config.maven.plugins` and separate
  plugin-management declarations under
  `project.modules.items[].build_config.maven.plugin_management`.
- `project-map.json` includes module-owned standard resource roots under
  `project.modules.items[].build_config.resources` and path-only supported
  application/logging config-file inventory under
  `project.modules.items[].build_config.config_files`.
- `project-map.json` includes module-owned direct source-visible Spring Boot application
  signals under `project.modules.items[].build_config.spring_boot_applications`.
- Plugin-derived generated-source warnings are rendered in the known-limits section as
  warning facts and must not be rendered as endpoint, API-operation, or generated-source
  facts.
- `agent-guide.md` includes a dedicated `Build And Configuration Orientation` section
  generated from structured build/config facts only. It renders Maven metadata,
  dependencies, dependency-management declarations, plugins, plugin-management
  declarations, resource roots, config file paths, Spring Boot application signals, and
  concise module warning summaries without interpreting config values or claiming
  effective/resolved/runtime/generated behavior.

Current v0.3 `agent-guide.md` behavior:

- The guide should add a `Build And Configuration Orientation` section generated from
  v0.3 `build_config` facts.
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

Current v0.4 API surface `agent-guide.md` behavior:

- The guide includes an `API Surface Interpretation` section generated from structured
  API surface facts and evidence only.
- The section distinguishes code-backed source-visible Spring MVC endpoint facts,
  code-backed source-visible interface-declared endpoint facts, spec-file declared API
  input facts, spec-backed OpenAPI operation facts, generated-source API warnings,
  repository-rest warnings, and hidden HTTP warnings.
- Source-visible Spring MVC entries may be described as detected endpoint facts only
  when they come from `endpoints[]`.
- OpenAPI/Swagger spec-file entries must be described as declared API inputs, not as
  parsed operations or implemented endpoints.
- OpenAPI operation entries must be described as declared/spec-backed operations, not
  implemented endpoints.
- Generated-source API signals must be described as warnings until explicit
  generated-source scanning is designed and enabled.
- Repository-rest and hidden HTTP warnings must be described as inspection hints, not
  detected endpoint facts.
- The guide must not claim runtime handler mappings, implementation coverage,
  source/spec agreement, service ownership, generated source contents, generated client
  SDKs, or OpenAPI/runtime agreement unless future deterministic relations explicitly
  support those claims.

Current v0.5 Spring application surface `agent-guide.md` behavior:

- The guide includes a `Spring Application Surface` section generated from structured
  `spring_application_surface` facts and resolving evidence only.
- The section is grouped by module using module identity from `project-map.json`. Inside
  each module group, extracted facts, inferred signals, explicit not-analyzed statuses,
  and warnings are rendered as separate categories.
- Repository stereotype entries should be described as direct annotation observations.
- Spring Data repository interface signals should be described as inferred
  source-visible signals. They must not be described as runtime repositories, entity
  ownership, query method behavior, or database access facts.
- Configuration classes, configuration properties, and bean methods are described as
  source-visible Spring configuration signals. They must not claim runtime bean graph,
  conditional activation, active profiles, config binding success, config values, bean
  scopes, lifecycle, proxy behavior, or dependency graphs.
- Explicit status fields such as `entity_relation_status: "not_analyzed"`,
  `binding_status: "not_analyzed"`, and `bean_name_status: "not_analyzed"` are rendered
  as not-analyzed orientation signals, not as runtime relation, binding, or bean-name
  facts.
- Transaction, scheduled, event listener, and messaging listener annotations are
  described as operational change-surface signals. They must not claim runtime
  transaction behavior, transaction propagation, scheduler registration, scheduler
  frequency, event delivery, message destinations, message topology, queue/topic
  existence, consumer groups, delivery semantics, or broker behavior.
- Spring Security configuration warnings are described as inspection hints and
  change-risk signals. Empty security warning collections under an `"analyzed"` security
  subsection mean no bounded supported source-visible security configuration warning was
  emitted; they do not prove the absence of security configuration outside the supported
  analyzer scope. Security warning guidance must not claim security policy, endpoint
  protection, authentication behavior, authorization behavior, vulnerability, or
  correctness.

Planned v0.6 JPA/domain `agent-guide.md` behavior:

- The guide may expand `Detected JPA Entities` or add a concise `Domain And Data Model`
  section generated from structured `entities` facts, repository/entity relation
  statuses, and resolving evidence only.
- Entity and embeddable entries must be grouped or labeled by module using
  `module_id`. Embeddables must be described as `@Embeddable` source-visible types, not
  as tables or standalone entities.
- Entity field metadata should use `Source-visible` or `Detected` wording for direct
  JPA annotations and must not claim complete persistent-property inventory, runtime
  access strategy, database columns, table existence, generated IDs, optimistic-locking
  correctness, or provider defaults.
- Identifier and embedded-id entries must show the explicit source-visible support
  boundary, such as simple `@Id`, mapped-superclass `@Id`, `@EmbeddedId`, or `@IdClass`
  signal. `@IdClass` must be rendered as a composite-id signal, not reconstructed
  composite-key semantics.
- Relationship entries must separate direct relationship annotation facts from inferred
  target links. Declared-type-only relationships should remain `Uncertain`; unique
  source-visible entity target links should be rendered as `Inferred`; ambiguous or
  unsupported targets should show the corresponding uncertainty/status.
- Relationship metadata such as `mappedBy`, join columns, join tables, optional, fetch,
  cascade, and orphan-removal values must be described as source-visible annotation
  attributes only. The guide must not claim ORM ownership correctness, foreign keys,
  database constraints, join-table existence, fetch behavior, cascade behavior, or
  runtime provider behavior.
- Repository/entity links should be rendered inside or near the Spring/Data/domain
  guidance as inferred Spring Data generic relations. The guide must show relation
  status values such as `inferred`, `not_detected`, `ambiguous`, `unsupported`, or
  `not_analyzed` without describing them as runtime repositories, query semantics, or
  database access facts.
- The known-limits section should explicitly state that v0.6 JPA/domain facts do not
  perform database introspection, runtime Hibernate metadata analysis, DDL
  reconstruction, JPQL semantic parsing, migration interpretation, complete ORM model
  reconstruction, or runtime repository/entity verification.

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
