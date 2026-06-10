# Endpoints

## Source-Visible Spring MVC Endpoints

### Direct Handler Mappings

#### Module `module:.` (`.`)

- Module path: `.`
- Support status: `supported`

##### GET /orders/{id}

- Module: `module:.` (`.`)
- API surface category: `source_visible_spring_mvc_endpoint`
- Controller: `com.example.web.OrderController`
- Handler: `getOrder`
- Mapping source: `direct_handler_method` (`com.example.web.OrderController#getOrder`)
- HTTP methods: `GET`
- Request parameters: none detected
- Request body: none detected
- Response: `String`
- Evidence: `ev:src/main/java/com/example/web/OrderController.java:6-6:com.example.web.OrderController:@RestController`, `ev:src/main/java/com/example/web/OrderController.java:8-8:com.example.web.OrderController#getOrder:@GetMapping`

### Source-Visible Interface-Declared Mappings

No source-visible interface-declared mappings detected.

## Declared OpenAPI Operations

No spec-backed declared OpenAPI operations recorded.

## Generated And Hidden API Warnings

### Generated-Source API Signals

No generated-source API warning signals recorded.

### Repository REST Warnings

No repository-rest warnings recorded.

### Hidden HTTP Warnings

No hidden HTTP warnings recorded.
