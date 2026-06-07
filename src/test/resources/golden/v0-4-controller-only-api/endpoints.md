# Endpoints

## Source-Visible Spring MVC Endpoints

### Direct Handler Mappings

#### Module `module:.` (`.`)

- Module path: `.`
- Support status: `supported`

##### POST /inventory

- Module: `module:.` (`.`)
- API surface category: `source_visible_spring_mvc_endpoint`
- Controller: `com.example.api.InventoryController`
- Handler: `createInventory`
- Mapping source: `direct_handler_method` (`com.example.api.InventoryController#createInventory`)
- HTTP methods: `POST`
- Request parameters: none detected
- Request body: `CreateInventoryRequest`
- Response: `InventoryDto`
- Evidence: `ev:src/main/java/com/example/api/InventoryController.java:9-9:com.example.api.InventoryController:@RestController`, `ev:src/main/java/com/example/api/InventoryController.java:11-11:com.example.api.InventoryController#createInventory:@PostMapping`, `ev:src/main/java/com/example/api/InventoryController.java:12-12:com.example.api.InventoryController#createInventory:@RequestBody:parameter:0:request`

##### GET /inventory/{id}

- Module: `module:.` (`.`)
- API surface category: `source_visible_spring_mvc_endpoint`
- Controller: `com.example.api.InventoryController`
- Handler: `getInventory`
- Mapping source: `direct_handler_method` (`com.example.api.InventoryController#getInventory`)
- HTTP methods: `GET`
- Request parameters: `request_param:includeDetails` (`String`)
- Request body: none detected
- Response: `InventoryDto`
- Evidence: `ev:src/main/java/com/example/api/InventoryController.java:9-9:com.example.api.InventoryController:@RestController`, `ev:src/main/java/com/example/api/InventoryController.java:16-16:com.example.api.InventoryController#getInventory:@GetMapping`, `ev:src/main/java/com/example/api/InventoryController.java:17-17:com.example.api.InventoryController#getInventory:@RequestParam:parameter:0:includeDetails`

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
