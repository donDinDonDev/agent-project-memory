# Endpoints

## Source-Visible Spring MVC Endpoints

### Direct Handler Mappings

#### Module `module:services/billing` (`services/billing`)

- Module path: `services/billing`
- Support status: `supported`

##### GET /billing/health

- Module: `module:services/billing` (`services/billing`)
- API surface category: `source_visible_spring_mvc_endpoint`
- Controller: `com.example.shared.SharedController`
- Handler: `health`
- Mapping source: `direct_handler_method` (`com.example.shared.SharedController#health`)
- HTTP methods: `GET`
- Request parameters: none detected
- Request body: none detected
- Response: `String`
- Evidence: `ev:services/billing/src/main/java/com/example/shared/SharedController.java:3-3:com.example.shared.SharedController:@RestController`, `ev:services/billing/src/main/java/com/example/shared/SharedController.java:4-4:com.example.shared.SharedController:@RequestMapping`, `ev:services/billing/src/main/java/com/example/shared/SharedController.java:6-6:com.example.shared.SharedController#health:@GetMapping`

#### Module `module:services/orders` (`services/orders`)

- Module path: `services/orders`
- Support status: `supported`

##### GET /orders/health

- Module: `module:services/orders` (`services/orders`)
- API surface category: `source_visible_spring_mvc_endpoint`
- Controller: `com.example.shared.SharedController`
- Handler: `health`
- Mapping source: `direct_handler_method` (`com.example.shared.SharedController#health`)
- HTTP methods: `GET`
- Request parameters: none detected
- Request body: none detected
- Response: `String`
- Evidence: `ev:services/orders/src/main/java/com/example/shared/SharedController.java:3-3:com.example.shared.SharedController:@RestController`, `ev:services/orders/src/main/java/com/example/shared/SharedController.java:4-4:com.example.shared.SharedController:@RequestMapping`, `ev:services/orders/src/main/java/com/example/shared/SharedController.java:6-6:com.example.shared.SharedController#health:@GetMapping`

### Source-Visible Interface-Declared Mappings

No source-visible interface-declared mappings detected.

## Declared OpenAPI Operations

### Module `module:services/orders` (`services/orders`)

#### Declared `GET /orders/health`

- API surface category: `openapi_declared_operation`
- Module: `module:services/orders` (`services/orders`)
- Spec path: `services/orders/src/main/resources/openapi.yml`
- HTTP method: `GET`
- Declared path: `/orders/health`
- Operation ID: `declaredOrdersHealth`
- Tags: `Orders`
- Implementation status: `not_analyzed`
- Evidence: `ev:services/orders/src/main/resources/openapi.yml:4-4:api_spec:operation%3Aget%3A/orders/health`

## Generated And Hidden API Warnings

### Generated-Source API Signals

#### Warning `generated_source:maven_openapi_swagger_codegen_plugin`

- Module: `module:services/orders` (`services/orders`)
- Warning category: `generated_source`
- Signal: `maven_openapi_swagger_codegen_plugin`
- Source path: `services/orders/pom.xml`
- Message: Maven OpenAPI/Swagger code generation plugin declaration detected; the analyzer does not run code generation, scan generated sources by default, or create endpoint/API facts from this build signal.
- Evidence: `ev:services/orders/pom.xml:5-5:build_file:maven:plugin:000001:artifactId`

#### Warning `hidden_http_surface:maven_openapi_swagger_codegen_plugin`

- Module: `module:services/orders` (`services/orders`)
- Warning category: `hidden_http_surface`
- Signal: `maven_openapi_swagger_codegen_plugin`
- Source path: `services/orders/pom.xml`
- Message: Maven OpenAPI/Swagger code generation plugin signal detected; the analyzer does not run generation or scan generated sources by default.
- Evidence: `ev:services/orders/pom.xml:5-5:build_file:openapi-generator-maven-plugin`

### Repository REST Warnings

#### Warning `hidden_http_surface:repository_rest_resource`

- Module: `module:services/billing` (`services/billing`)
- Warning category: `hidden_http_surface`
- Signal: `repository_rest_resource`
- Source path: `services/billing/src/main/java/com/example/shared/SharedController.java`
- Message: Direct @RepositoryRestResource detected; the analyzer warns about possible Spring Data REST HTTP surface but does not expand endpoints.
- Evidence: `ev:services/billing/src/main/java/com/example/shared/SharedController.java:22-22:com.example.shared.SharedRepository:@RepositoryRestResource`

#### Warning `hidden_http_surface:repository_rest_resource`

- Module: `module:services/orders` (`services/orders`)
- Warning category: `hidden_http_surface`
- Signal: `repository_rest_resource`
- Source path: `services/orders/src/main/java/com/example/shared/SharedController.java`
- Message: Direct @RepositoryRestResource detected; the analyzer warns about possible Spring Data REST HTTP surface but does not expand endpoints.
- Evidence: `ev:services/orders/src/main/java/com/example/shared/SharedController.java:22-22:com.example.shared.SharedRepository:@RepositoryRestResource`

### Hidden HTTP Warnings

No hidden HTTP warnings recorded.
