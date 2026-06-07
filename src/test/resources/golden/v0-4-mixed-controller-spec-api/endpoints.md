# Endpoints

## Source-Visible Spring MVC Endpoints

### Direct Handler Mappings

#### Module `module:.` (`.`)

- Module path: `.`
- Support status: `supported`

##### GET /orders/{id}

- Module: `module:.` (`.`)
- API surface category: `source_visible_spring_mvc_endpoint`
- Controller: `com.example.mixed.OrderController`
- Handler: `getOrder`
- Mapping source: `direct_handler_method` (`com.example.mixed.OrderController#getOrder`)
- HTTP methods: `GET`
- Request parameters: none detected
- Request body: none detected
- Response: `OrderDto`
- Evidence: `ev:src/main/java/com/example/mixed/OrderController.java:6-6:com.example.mixed.OrderController:@RestController`, `ev:src/main/java/com/example/mixed/OrderController.java:8-8:com.example.mixed.OrderController#getOrder:@GetMapping`

### Source-Visible Interface-Declared Mappings

No source-visible interface-declared mappings detected.

## Declared OpenAPI Operations

### Module `module:.` (`.`)

#### Declared `POST /orders`

- API surface category: `openapi_declared_operation`
- Module: `module:.` (`.`)
- Spec path: `src/main/resources/openapi.yml`
- HTTP method: `POST`
- Declared path: `/orders`
- Operation ID: `createOrder`
- Tags: `Orders`
- Implementation status: `not_analyzed`
- Evidence: `ev:src/main/resources/openapi.yml:9-9:api_spec:operation%3Apost%3A/orders`

#### Declared `GET /orders/{id}`

- API surface category: `openapi_declared_operation`
- Module: `module:.` (`.`)
- Spec path: `src/main/resources/openapi.yml`
- HTTP method: `GET`
- Declared path: `/orders/{id}`
- Operation ID: `getOrder`
- Tags: `Orders`
- Implementation status: `not_analyzed`
- Evidence: `ev:src/main/resources/openapi.yml:4-4:api_spec:operation%3Aget%3A/orders/{id}`

## Generated And Hidden API Warnings

### Generated-Source API Signals

No generated-source API warning signals recorded.

### Repository REST Warnings

No repository-rest warnings recorded.

### Hidden HTTP Warnings

No hidden HTTP warnings recorded.
