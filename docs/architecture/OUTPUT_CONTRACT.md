# Output Contract

This document defines the first `.project-memory/` output structure.

Any output field addition, removal, rename, or semantic change requires updating this file. Tests should be updated at the same time once implementation begins.

## Directory Structure

The first scan output is:

```text
.project-memory/
  project-map.json
  evidence-index.jsonl
  endpoints.md
  agent-guide.md
```

## `project-map.json`

`project-map.json` is the machine-readable project memory file. It contains detected project metadata, build layout, Spring endpoints, component inventory, and references to evidence entries.

Rough endpoint fact example:

```json
{
  "schema_version": "0.1",
  "project": {
    "root": ".",
    "build_system": "maven",
    "source_roots": ["src/main/java"],
    "test_roots": ["src/test/java"]
  },
  "endpoints": [
    {
      "id": "endpoint:com.example.orders.OrderController#getOrder",
      "controller_class": "com.example.orders.OrderController",
      "method_name": "getOrder",
      "http_methods": ["GET"],
      "paths": ["/orders/{id}"],
      "request_parameters": [
        {
          "name": "id",
          "source": "path_variable",
          "java_type": "java.lang.Long"
        }
      ],
      "request_body_type": null,
      "response_type": "com.example.orders.OrderDto",
      "evidence_ids": [
        "ev:src/main/java/com/example/orders/OrderController.java:12-24"
      ]
    }
  ],
  "components": [
    {
      "id": "component:com.example.orders.OrderService",
      "class_name": "com.example.orders.OrderService",
      "stereotypes": ["Service"],
      "evidence_ids": [
        "ev:src/main/java/com/example/orders/OrderService.java:8-8"
      ]
    }
  ]
}
```

The exact schema will be stabilized during implementation, but every important fact must have evidence IDs.

## `evidence-index.jsonl`

`evidence-index.jsonl` is newline-delimited JSON. Each line is one evidence record.

Rough evidence entry example:

```json
{"id":"ev:src/main/java/com/example/orders/OrderController.java:12-24","source_type":"annotation","path":"src/main/java/com/example/orders/OrderController.java","class_name":"com.example.orders.OrderController","method_name":"getOrder","symbol_name":"@GetMapping","line_start":20,"line_end":20,"excerpt":"@GetMapping(\"/orders/{id}\")","confidence":"high"}
```

Evidence entries should follow `docs/architecture/EVIDENCE_MODEL.md`.

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

Example shape:

```md
# Endpoints

## GET /orders/{id}

- Controller: `com.example.orders.OrderController`
- Method: `getOrder`
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

