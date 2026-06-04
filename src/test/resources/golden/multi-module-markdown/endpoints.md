# Endpoints

## Module `module:services/billing` (`services/billing`)

- Module path: `services/billing`
- Support status: `supported`

### GET /billing/health

- Module: `module:services/billing` (`services/billing`)
- Controller: `com.example.shared.SharedController`
- Handler: `health`
- Mapping source: `direct_handler_method` (`com.example.shared.SharedController#health`)
- HTTP methods: `GET`
- Request parameters: none detected
- Request body: none detected
- Response: `String`
- Evidence: `ev:services/billing/src/main/java/com/example/shared/SharedController.java:3-3:com.example.shared.SharedController:@RestController`, `ev:services/billing/src/main/java/com/example/shared/SharedController.java:4-4:com.example.shared.SharedController:@RequestMapping`, `ev:services/billing/src/main/java/com/example/shared/SharedController.java:6-6:com.example.shared.SharedController#health:@GetMapping`

## Module `module:services/orders` (`services/orders`)

- Module path: `services/orders`
- Support status: `supported`

### GET /orders/health

- Module: `module:services/orders` (`services/orders`)
- Controller: `com.example.shared.SharedController`
- Handler: `health`
- Mapping source: `direct_handler_method` (`com.example.shared.SharedController#health`)
- HTTP methods: `GET`
- Request parameters: none detected
- Request body: none detected
- Response: `String`
- Evidence: `ev:services/orders/src/main/java/com/example/shared/SharedController.java:3-3:com.example.shared.SharedController:@RestController`, `ev:services/orders/src/main/java/com/example/shared/SharedController.java:4-4:com.example.shared.SharedController:@RequestMapping`, `ev:services/orders/src/main/java/com/example/shared/SharedController.java:6-6:com.example.shared.SharedController#health:@GetMapping`
