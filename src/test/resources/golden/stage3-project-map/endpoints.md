# Endpoints

## Module `module:.` (`.`)

- Module path: `.`
- Support status: `supported`

### POST /api/items

- Module: `module:.` (`.`)
- Controller: `com.example.web.ProjectMapController`
- Handler: `createItem`
- Mapping source: `direct_handler_method` (`com.example.web.ProjectMapController#createItem`)
- HTTP methods: `POST`
- Request parameters: none detected
- Request body: `CreateItemRequest`
- Response: `ItemResponse`
- Evidence: `ev:src/main/java/com/example/web/ProjectMapController.java:11-11:com.example.web.ProjectMapController:@RestController`, `ev:src/main/java/com/example/web/ProjectMapController.java:12-12:com.example.web.ProjectMapController:@RequestMapping`, `ev:src/main/java/com/example/web/ProjectMapController.java:21-21:com.example.web.ProjectMapController#createItem:@PostMapping`, `ev:src/main/java/com/example/web/ProjectMapController.java:22-22:com.example.web.ProjectMapController#createItem:@RequestBody:parameter:0:request`

### GET /api/items/{id}

- Module: `module:.` (`.`)
- Controller: `com.example.web.ProjectMapController`
- Handler: `getItem`
- Mapping source: `direct_handler_method` (`com.example.web.ProjectMapController#getItem`)
- HTTP methods: `GET`
- Request parameters: `path_variable:id` (`Long`), `request_param:expand` (`String`)
- Request body: none detected
- Response: `ItemResponse`
- Evidence: `ev:src/main/java/com/example/web/ProjectMapController.java:11-11:com.example.web.ProjectMapController:@RestController`, `ev:src/main/java/com/example/web/ProjectMapController.java:12-12:com.example.web.ProjectMapController:@RequestMapping`, `ev:src/main/java/com/example/web/ProjectMapController.java:14-14:com.example.web.ProjectMapController#getItem:@GetMapping`, `ev:src/main/java/com/example/web/ProjectMapController.java:16-16:com.example.web.ProjectMapController#getItem:@PathVariable:parameter:0:id`, `ev:src/main/java/com/example/web/ProjectMapController.java:17-17:com.example.web.ProjectMapController#getItem:@RequestParam:parameter:1:expand`
