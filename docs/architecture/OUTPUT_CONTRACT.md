# Output Contract

This document defines the first `.project-memory/` output structure.

Any output field addition, removal, rename, or semantic change requires updating this file. Tests should be updated at the same time once implementation begins.

## Directory Structure

The v0.1 target scan output is:

```text
.project-memory/
  project-map.json
  evidence-index.jsonl
  endpoints.md
  agent-guide.md
```

During the incremental Stage 4.1 implementation, `scan <path>` writes
`project-map.json`, `endpoints.md`, and `evidence-index.jsonl` when a Maven-style
`src/main/java` source root exists. `agent-guide.md` is stabilized in a later roadmap
stage and is not emitted by this slice.

## `project-map.json`

`project-map.json` is the machine-readable project memory file. In Stage 4.1 it contains
the minimal stable v0.1 slice for the currently supported local single-module
Maven-style Spring MVC endpoint and direct Spring component scan.

Stage 4.1 writes this top-level object:

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
      "evidence_ids": [
        "ev:src/main/java/com/example/orders/OrderController.java:18-18:com.example.orders.OrderController:@RestController",
        "ev:src/main/java/com/example/orders/OrderController.java:20-20:com.example.orders.OrderController#getOrder:@GetMapping",
        "ev:src/main/java/com/example/orders/OrderController.java:21-21:com.example.orders.OrderController#getOrder:@PathVariable:parameter:0:id"
      ]
    }
  ],
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
  }
}
```

Field rules:

- `schema_version` is the string `"0.1"` for this output contract slice.
- `project.root` is `"."` because the output is relative to the scanned repository root.
- `project.build.system` is `"maven"` when a root `pom.xml` file is detected and
  `"not_detected"` otherwise.
- `project.build.root_build_file` is `"pom.xml"` when detected and `null` otherwise.
- `project.build.evidence_ids` references `build_file` evidence for the root `pom.xml`
  when present and is an empty array otherwise.
- `project.source_roots` contains detected standard production source roots. Stage 4.1
  supports `src/main/java`.
- `project.test_roots` contains detected standard test source roots. Stage 4.1 supports
  `src/test/java`.
- `endpoints` is sorted deterministically by first path, HTTP methods, method semantics,
  controller class, and handler method.
- `endpoint.id` is `endpoint:<controller_class>#<handler_method>` in this slice.
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
- `components.analysis_status` is `"analyzed"` when the supported `src/main/java` source
  root exists and the direct component analyzer runs.
- `components.items` contains direct Spring stereotype component facts sorted
  deterministically by `class_name` and `id`.
- `component.id` is `component:<class_name>`.
- `component.class_name` is the fully qualified Java class name when resolvable from the
  source file package and class declaration.
- `component.stereotypes` contains directly present supported class-level annotation
  symbols with `@`. Stage 4.1 supports `@Component`, `@Service`, `@Repository`,
  `@Controller`, `@RestController`, and `@Configuration`.
- `component.evidence_ids` references annotation evidence for the direct stereotype
  annotations and must resolve to records in `evidence-index.jsonl`.

## `evidence-index.jsonl`

`evidence-index.jsonl` is newline-delimited JSON. Each line is one evidence record.
Stage 4.1 emits a stable field order:

```json
{"id":"ev:src/main/java/com/example/orders/OrderController.java:18-18:com.example.orders.OrderController:@RestController","source_type":"annotation","path":"src/main/java/com/example/orders/OrderController.java","class_name":"com.example.orders.OrderController","method_name":null,"symbol_name":"@RestController","line_start":18,"line_end":18,"excerpt":"@RestController","confidence":"high"}
{"id":"ev:src/main/java/com/example/orders/OrderController.java:20-20:com.example.orders.OrderController#getOrder:@GetMapping","source_type":"annotation","path":"src/main/java/com/example/orders/OrderController.java","class_name":"com.example.orders.OrderController","method_name":"getOrder","symbol_name":"@GetMapping","line_start":20,"line_end":20,"excerpt":"@GetMapping(\"/orders/{id}\")","confidence":"high"}
```

Evidence entries should follow `docs/architecture/EVIDENCE_MODEL.md`.

Stage 4.1 emits:

- `build_file` evidence for root `pom.xml` when present.
- `annotation` evidence for extracted Spring MVC controller, endpoint, request parameter,
  and request body annotations.
- `annotation` evidence for direct supported Spring component stereotype annotations on
  Java class declarations. `@Controller` and `@RestController` evidence IDs use the same
  annotation ID convention as endpoint evidence so the same source annotation is not
  duplicated in `evidence-index.jsonl`.

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
- Request body type when detected.
- Response type when detected.
- Evidence reference.

If a `@RequestMapping` endpoint does not declare an HTTP method, the Markdown output
must say that the method was not declared instead of inventing one. If a method
expression is present but unsupported by deterministic source extraction, the output
must mark it as unsupported.

Example shape:

```md
# Endpoints

## GET /orders/{id}

- Controller: `com.example.orders.OrderController`
- Handler: `getOrder`
- Response: `com.example.orders.OrderDto`
- Evidence: `src/main/java/com/example/orders/OrderController.java:20`
```

## `agent-guide.md`

`agent-guide.md` is a concise orientation file for AI coding agents and developers.

It should be generated from `project-map.json` and `evidence-index.jsonl`, not from unsupported guesses.

It may include:

- Detected build system and source roots.
- Important Spring entry points.
- Endpoint inventory summary.
- Component inventory summary.
- Known uncertainty.
- Suggested files to inspect before changing common areas.

It must not claim architecture that is not backed by evidence or explicitly marked as inference.

## Contract Rules

- Output changes require updating this file.
- Evidence field changes require updating `docs/architecture/EVIDENCE_MODEL.md`.
- Generated facts must reference evidence IDs where possible.
- Markdown outputs should remain readable without hiding evidence.
- JSON outputs should remain stable enough for tests and downstream tools.
