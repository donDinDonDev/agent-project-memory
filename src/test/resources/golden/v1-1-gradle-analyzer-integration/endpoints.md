# Endpoints

## Source-Visible Spring MVC Endpoints

### Direct Handler Mappings

#### Module `module:.` (`.`)

- Module path: `.`
- Support status: `supported`

##### GET /orders/{id}

- Module: `module:.` (`.`)
- API surface category: `source_visible_spring_mvc_endpoint`
- Controller: `com.example.gradle.web.OrderController`
- Handler: `getOrder`
- Mapping source: `direct_handler_method` (`com.example.gradle.web.OrderController#getOrder`)
- HTTP methods: `GET`
- Request parameters: `path_variable:id` (`Long`)
- Request body: none detected
- Response: `OrderDto`
- Evidence: `ev:src/main/java/com/example/gradle/web/OrderController.java:9-9:com.example.gradle.web.OrderController:@RestController`, `ev:src/main/java/com/example/gradle/web/OrderController.java:10-10:com.example.gradle.web.OrderController:@RequestMapping`, `ev:src/main/java/com/example/gradle/web/OrderController.java:19-19:com.example.gradle.web.OrderController#getOrder:@GetMapping`, `ev:src/main/java/com/example/gradle/web/OrderController.java:20-20:com.example.gradle.web.OrderController#getOrder:@PathVariable:parameter:0:id`

### Source-Visible Interface-Declared Mappings

No source-visible interface-declared mappings detected.

## Declared OpenAPI Operations

### Module `module:.` (`.`)

#### Declared `GET /orders/{id}`

- API surface category: `openapi_declared_operation`
- Module: `module:.` (`.`)
- Spec path: `src/main/resources/openapi.yml`
- HTTP method: `GET`
- Declared path: `/orders/{id}`
- Operation ID: `getOrderDocumented`
- Tags: `Orders`
- Implementation status: `not_analyzed`
- Evidence: `ev:src/main/resources/openapi.yml:7-7:api_spec:operation%3Aget%3A/orders/{id}`

## Generated And Hidden API Warnings

### Generated-Source API Signals

No generated-source API warning signals recorded.

### Repository REST Warnings

No repository-rest warnings recorded.

### Hidden HTTP Warnings

No hidden HTTP warnings recorded.
