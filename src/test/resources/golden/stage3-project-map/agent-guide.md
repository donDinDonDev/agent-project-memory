# Agent Guide

Generated deterministically from `project-map.json` and `evidence-index.jsonl`. The guide generator does not re-analyze source files.

## Detected Project Layout

- Build system: Detected `maven`
- Root build file: Detected `pom.xml`
  - Evidence: `pom.xml:1` (`ev:pom.xml:1-1:build_file:pom.xml`)
- Source roots: Detected `src/main/java`
  - Evidence: recorded in `project-map.json`; no separate source-root evidence IDs are emitted in v0.1.
- Test roots: Detected `src/test/java`
  - Evidence: recorded in `project-map.json`; no separate test-root evidence IDs are emitted in v0.1.

## Detected Spring MVC Endpoints

### `POST /api/items`

- Controller: Detected `com.example.web.ProjectMapController`
- Handler: Detected `createItem`
- Mapping source: Detected `direct_handler_method` from `com.example.web.ProjectMapController#createItem` with binding `direct`
- HTTP methods: Detected `POST`
- Paths: Detected `/api/items`
- Request parameters: Detected none.
- Request body: Detected `CreateItemRequest`
- Response: Detected `ItemResponse`
  - Evidence: `src/main/java/com/example/web/ProjectMapController.java:11` (`ev:src/main/java/com/example/web/ProjectMapController.java:11-11:com.example.web.ProjectMapController:@RestController`), `src/main/java/com/example/web/ProjectMapController.java:12` (`ev:src/main/java/com/example/web/ProjectMapController.java:12-12:com.example.web.ProjectMapController:@RequestMapping`), `src/main/java/com/example/web/ProjectMapController.java:21` (`ev:src/main/java/com/example/web/ProjectMapController.java:21-21:com.example.web.ProjectMapController#createItem:@PostMapping`), `src/main/java/com/example/web/ProjectMapController.java:22` (`ev:src/main/java/com/example/web/ProjectMapController.java:22-22:com.example.web.ProjectMapController#createItem:@RequestBody:parameter:0:request`)

### `GET /api/items/{id}`

- Controller: Detected `com.example.web.ProjectMapController`
- Handler: Detected `getItem`
- Mapping source: Detected `direct_handler_method` from `com.example.web.ProjectMapController#getItem` with binding `direct`
- HTTP methods: Detected `GET`
- Paths: Detected `/api/items/{id}`
- Request parameters: Detected `path_variable:id (Long)`, `request_param:expand (String)`
- Request body: Detected none.
- Response: Detected `ItemResponse`
  - Evidence: `src/main/java/com/example/web/ProjectMapController.java:11` (`ev:src/main/java/com/example/web/ProjectMapController.java:11-11:com.example.web.ProjectMapController:@RestController`), `src/main/java/com/example/web/ProjectMapController.java:12` (`ev:src/main/java/com/example/web/ProjectMapController.java:12-12:com.example.web.ProjectMapController:@RequestMapping`), `src/main/java/com/example/web/ProjectMapController.java:14` (`ev:src/main/java/com/example/web/ProjectMapController.java:14-14:com.example.web.ProjectMapController#getItem:@GetMapping`), `src/main/java/com/example/web/ProjectMapController.java:16` (`ev:src/main/java/com/example/web/ProjectMapController.java:16-16:com.example.web.ProjectMapController#getItem:@PathVariable:parameter:0:id`), `src/main/java/com/example/web/ProjectMapController.java:17` (`ev:src/main/java/com/example/web/ProjectMapController.java:17-17:com.example.web.ProjectMapController#getItem:@RequestParam:parameter:1:expand`)

## Detected Spring Components

- Analysis status: `analyzed`

### `com.example.components.AppConfiguration`

- Stereotypes: Detected `@Configuration`
  - Evidence: `src/main/java/com/example/components/InventoryComponents.java:20` (`ev:src/main/java/com/example/components/InventoryComponents.java:20-20:com.example.components.AppConfiguration:@Configuration`)

### `com.example.components.InventoryComponent`

- Stereotypes: Detected `@Component`
  - Evidence: `src/main/java/com/example/components/InventoryComponents.java:8` (`ev:src/main/java/com/example/components/InventoryComponents.java:8-8:com.example.components.InventoryComponent:@Component`)

### `com.example.components.InventoryRepository`

- Stereotypes: Detected `@Repository`
  - Evidence: `src/main/java/com/example/components/InventoryComponents.java:16` (`ev:src/main/java/com/example/components/InventoryComponents.java:16-16:com.example.components.InventoryRepository:@Repository`)

### `com.example.components.InventoryService`

- Stereotypes: Detected `@Service`
  - Evidence: `src/main/java/com/example/components/InventoryComponents.java:12` (`ev:src/main/java/com/example/components/InventoryComponents.java:12-12:com.example.components.InventoryService:@Service`)

### `com.example.web.ProjectMapController`

- Stereotypes: Detected `@RestController`
  - Evidence: `src/main/java/com/example/web/ProjectMapController.java:11` (`ev:src/main/java/com/example/web/ProjectMapController.java:11-11:com.example.web.ProjectMapController:@RestController`)

## Detected JPA Entities

- Analysis status: `analyzed`

### `com.example.domain.ProjectCustomer`

- Entity: Detected `com.example.domain.ProjectCustomer`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:20` (`ev:src/main/java/com/example/domain/ProjectEntities.java:20-20:com.example.domain.ProjectCustomer:@Entity`)
- Table: Detected none.
- Identifier field: Detected `id` (`Long`) declared by `com.example.domain.ProjectCustomer` with source_kind `declared`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:22` (`ev:src/main/java/com/example/domain/ProjectEntities.java:22-22:com.example.domain.ProjectCustomer:@Id:field:id`)
- Relationships: Detected none.

### `com.example.domain.ProjectOrder`

- Entity: Detected `com.example.domain.ProjectOrder`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:26` (`ev:src/main/java/com/example/domain/ProjectEntities.java:26-26:com.example.domain.ProjectOrder:@Entity`)
- Table: Detected `orders`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:27` (`ev:src/main/java/com/example/domain/ProjectEntities.java:27-27:com.example.domain.ProjectOrder:@Table`)
- Identifier field: Detected `id` (`Long`) declared by `com.example.domain.ProjectOrder` with source_kind `declared`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:29` (`ev:src/main/java/com/example/domain/ProjectEntities.java:29-29:com.example.domain.ProjectOrder:@Id:field:id`)
- Relationship: Uncertain target for `customer` `@ManyToOne` declared type `ProjectCustomer`
  - target_resolution: `declared_type_only`
  - uncertainty: `target_type_not_resolved`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:32` (`ev:src/main/java/com/example/domain/ProjectEntities.java:32-32:com.example.domain.ProjectOrder:@ManyToOne:field:customer`)
- Relationship: Uncertain target for `invoice` `@OneToOne` declared type `ProjectInvoice`
  - target_resolution: `declared_type_only`
  - uncertainty: `target_type_not_resolved`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:38` (`ev:src/main/java/com/example/domain/ProjectEntities.java:38-38:com.example.domain.ProjectOrder:@OneToOne:field:invoice`)
- Relationship: Uncertain target for `lines` `@OneToMany` declared type `List<ProjectOrderLine>`
  - target_resolution: `declared_type_only`
  - uncertainty: `target_type_not_resolved`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:35` (`ev:src/main/java/com/example/domain/ProjectEntities.java:35-35:com.example.domain.ProjectOrder:@OneToMany:field:lines`)
- Relationship: Uncertain target for `tags` `@ManyToMany` declared type `Set<ProjectTag>`
  - target_resolution: `declared_type_only`
  - uncertainty: `target_type_not_resolved`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:41` (`ev:src/main/java/com/example/domain/ProjectEntities.java:41-41:com.example.domain.ProjectOrder:@ManyToMany:field:tags`)

### `com.example.domain.ProjectVisit`

- Entity: Detected `com.example.domain.ProjectVisit`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:45` (`ev:src/main/java/com/example/domain/ProjectEntities.java:45-45:com.example.domain.ProjectVisit:@Entity`)
- Table: Detected `visits`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:46` (`ev:src/main/java/com/example/domain/ProjectEntities.java:46-46:com.example.domain.ProjectVisit:@Table`)
- Identifier field: Detected `id` (`Long`) declared by `com.example.domain.ProjectBaseEntity` with source_kind `mapped_superclass`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:16` (`ev:src/main/java/com/example/domain/ProjectEntities.java:16-16:com.example.domain.ProjectBaseEntity:@Id:field:id`), `src/main/java/com/example/domain/ProjectEntities.java:14` (`ev:src/main/java/com/example/domain/ProjectEntities.java:14-14:com.example.domain.ProjectBaseEntity:@MappedSuperclass`)
- Relationships: Detected none.

## Detected Tests

- Analysis status: `analyzed`

### `com.example.web.ProjectMapControllerTest`

- Test class: Detected `com.example.web.ProjectMapControllerTest`
  - Evidence: `src/test/java/com/example/web/ProjectMapControllerTest.java:3` (`ev:src/test/java/com/example/web/ProjectMapControllerTest.java:3-3:com.example.web.ProjectMapControllerTest:test_file`)
- Source: Detected `src/test/java/com/example/web/ProjectMapControllerTest.java`
- Framework signals: Detected none.
- Inferred tested subject: `com.example.web.ProjectMapController` (support_type: `inferred`, confidence: `medium`)
  - Evidence: `src/test/java/com/example/web/ProjectMapControllerTest.java:3` (`ev:src/test/java/com/example/web/ProjectMapControllerTest.java:3-3:com.example.web.ProjectMapControllerTest:test_file`), `src/main/java/com/example/web/ProjectMapController.java:13` (`ev:src/main/java/com/example/web/ProjectMapController.java:13-13:com.example.web.ProjectMapController:code_symbol`)

## Known Uncertainty And Limits

- Not analyzed: Spring runtime behavior such as component scanning, dependency injection graphs, bean lifecycle, scopes, and conditional configuration is not represented by `components.items`.
- Uncertain: JPA relationship targets preserve `target_resolution: declared_type_only` and `uncertainty: target_type_not_resolved`; no symbol solving or ORM runtime behavior is claimed.
- Not analyzed: JPA mapped-superclass identifier support is limited to immediate source-visible superclasses; multi-level inheritance is not walked.
- Inferred: tested-subject relations use naming conventions only. Test execution, coverage, assertion behavior, call graphs, and complete subject mapping are not analyzed.
- Not analyzed: connectors, LLM summaries, repository chat, generic RAG, Gradle/Kotlin support, and multi-module Maven parsing are outside this guide.
- Not analyzed: generated sources, OpenAPI YAML, generated API reconstruction, classpath-only interfaces, and ambiguous interface endpoint bindings are outside the source-visible interface endpoint support.

## Practical Inspection Order For Coding Agents

1. Start with detected build and layout facts in `pom.xml`.
2. For HTTP behavior, inspect detected endpoint evidence in `src/main/java/com/example/web/ProjectMapController.java`.
3. For Spring wiring changes, inspect detected component evidence in `src/main/java/com/example/components/InventoryComponents.java`, `src/main/java/com/example/web/ProjectMapController.java` and avoid assuming runtime injection graphs.
4. For persistence changes, inspect detected entity evidence in `src/main/java/com/example/domain/ProjectEntities.java` and treat relationship targets as declared-type-only.
5. For tests, inspect detected test files and inferred tested-subject evidence in `src/test/java/com/example/web/ProjectMapControllerTest.java`, `src/main/java/com/example/web/ProjectMapController.java`; do not treat inferred subjects as coverage proof.
