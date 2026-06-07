# Agent Guide

Generated deterministically from `project-map.json` and `evidence-index.jsonl`. The guide generator does not re-analyze source files.

## Detected Project Layout

- Build system: Detected `maven`
- Root build file: Detected `pom.xml`
  - Evidence: `pom.xml:1` (`ev:pom.xml:1-1:build_file:pom.xml`)
- Source roots: Detected `src/main/java`
  - Evidence: recorded in `project-map.json`; no separate source-root evidence IDs are emitted.
- Test roots: Detected `src/test/java`
  - Evidence: recorded in `project-map.json`; no separate test-root evidence IDs are emitted.
- Modules analysis status: `analyzed`
- Module: Detected `module:.` (path: `.`)
  - POM path: Detected `pom.xml`
  - Support status: `supported`
  - Declaration kind: `scan_root`
  - Declared path: `.`
  - Source roots: Detected `src/main/java`
  - Source roots evidence: recorded in `project-map.json`; no separate production root evidence IDs are emitted.
  - Test roots: Detected `src/test/java`
  - Test roots evidence: recorded in `project-map.json`; no separate test root evidence IDs are emitted.
  - Declaration evidence: none recorded.
  - POM evidence: `pom.xml:1` (`ev:pom.xml:1-1:build_file:pom.xml`)

## Build And Configuration Orientation

### Module `module:.` (path: `.`)

- Build/config analysis status: `analyzed`
- Source-visible Maven metadata: group_id `com.example` (value_kind: `literal`), artifact_id `stage3-project-map` (value_kind: `literal`), version `1.0.0` (value_kind: `literal`), packaging `value:not_declared` (value_kind: `not_declared`).
  - Evidence: `pom.xml:7` (`ev:pom.xml:7-7:build_file:maven:project:groupId`), `pom.xml:8` (`ev:pom.xml:8-8:build_file:maven:project:artifactId`), `pom.xml:9` (`ev:pom.xml:9-9:build_file:maven:project:version`)
- Source-visible direct dependencies: Detected 2 direct dependency declarations.
  - Dependency: `com.example:inventory-client` declaration_kind `direct_dependency`, version `value:not_declared` (value_kind: `not_declared`), scope `test` (value_kind: `literal`), optional `true` (value_kind: `literal`).
  - Evidence: `pom.xml:29-34` (`ev:pom.xml:29-34:build_file:maven:dependency:000002`)
  - Dependency: `org.springframework.boot:spring-boot-starter-web` declaration_kind `direct_dependency`, version `${spring.boot.version}` (value_kind: `property_reference`), scope `value:not_declared` (value_kind: `not_declared`), optional `value:not_declared` (value_kind: `not_declared`).
  - Evidence: `pom.xml:24-28` (`ev:pom.xml:24-28:build_file:maven:dependency:000001`)
- Source-visible dependency-management declarations: Detected 1 dependency-management declarations; these are management declarations, not active resolved dependencies.
  - Dependency: `org.springframework.boot:spring-boot-dependencies` declaration_kind `dependency_management`, version `${spring.boot.version}` (value_kind: `property_reference`), scope `import` (value_kind: `literal`), optional `value:not_declared` (value_kind: `not_declared`).
  - Evidence: `pom.xml:13-19` (`ev:pom.xml:13-19:build_file:maven:dependency_management:000001`)
- Source-visible direct plugins: Detected 1 direct plugin declarations.
  - Plugin: `org.springframework.boot:spring-boot-maven-plugin` declaration_kind `direct_plugin`, version `${spring.boot.version}` (value_kind: `property_reference`).
    - Direct execution declarations: none recorded.
    - Configuration signals: none recorded
    - Generator signals: none recorded
  - Evidence: `pom.xml:48-52` (`ev:pom.xml:48-52:build_file:maven:plugin:000001`)
- Source-visible plugin-management declarations: Detected 1 plugin-management declarations; these are management declarations, not active plugin executions.
  - Plugin: `org.apache.maven.plugins:maven-surefire-plugin` declaration_kind `plugin_management`, version `3.2.5` (value_kind: `literal`).
    - Direct execution declarations: none recorded.
    - Configuration signals: none recorded
    - Generator signals: none recorded
  - Evidence: `pom.xml:40-44` (`ev:pom.xml:40-44:build_file:maven:plugin_management:000001`)
- Resource roots: Detected 1 standard resource root.
  - Resource root: `main` `src/main/resources`
    - Evidence: recorded in `project-map.json`; no separate resource-root evidence IDs are emitted.
- Config files: Detected 1 path-only supported config file; config contents, keys, and values are not rendered.
  - Config file: `src/main/resources/application.yml` kind `spring_application`, format `yaml`.
  - Evidence: `src/main/resources/application.yml` (`ev:src/main/resources/application.yml:unknown:config_file:application.yml`)
- Spring Boot application signals: Detected 1 direct `@SpringBootApplication` class signal.
  - Spring Boot application: Detected `com.example.Stage3Application` at `src/main/java/com/example/Stage3Application.java` with signal `spring_boot_application_with_main_method`.
    - Main method: Detected source-visible `main` method.
  - Evidence: `src/main/java/com/example/Stage3Application.java:5` (`ev:src/main/java/com/example/Stage3Application.java:5-5:com.example.Stage3Application:@SpringBootApplication`), `src/main/java/com/example/Stage3Application.java:7` (`ev:src/main/java/com/example/Stage3Application.java:7-7:com.example.Stage3Application#main:code_symbol`)
- Module warnings: Detected none.

## API Surface Interpretation

- API surface analysis status: `analyzed`
- Code-backed direct Spring MVC endpoint IDs: status `analyzed`; detected 2 IDs `endpoint:com.example.web.ProjectMapController#createItem`, `endpoint:com.example.web.ProjectMapController#getItem`.
- Code-backed source-visible interface-declared endpoint IDs: status `analyzed`; detected none.
- OpenAPI/Swagger spec files: status `analyzed`; detected none.
- OpenAPI/Swagger operations: status `not_detected`; no operation facts are emitted until the dedicated operation parser runs.
- Generated-source API warning IDs: status `analyzed`; detected none.
- Repository-rest warning IDs: status `analyzed`; detected none.
- Hidden HTTP warning IDs: status `analyzed`; detected none.

## Detected Spring MVC Endpoints

### `POST /api/items`

- Module: Detected `module:.` (path: `.`)
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

- Module: Detected `module:.` (path: `.`)
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

- Module: Detected `module:.` (path: `.`)
- Stereotypes: Detected `@Configuration`
  - Evidence: `src/main/java/com/example/components/InventoryComponents.java:20` (`ev:src/main/java/com/example/components/InventoryComponents.java:20-20:com.example.components.AppConfiguration:@Configuration`)

### `com.example.components.InventoryComponent`

- Module: Detected `module:.` (path: `.`)
- Stereotypes: Detected `@Component`
  - Evidence: `src/main/java/com/example/components/InventoryComponents.java:8` (`ev:src/main/java/com/example/components/InventoryComponents.java:8-8:com.example.components.InventoryComponent:@Component`)

### `com.example.components.InventoryRepository`

- Module: Detected `module:.` (path: `.`)
- Stereotypes: Detected `@Repository`
  - Evidence: `src/main/java/com/example/components/InventoryComponents.java:16` (`ev:src/main/java/com/example/components/InventoryComponents.java:16-16:com.example.components.InventoryRepository:@Repository`)

### `com.example.components.InventoryService`

- Module: Detected `module:.` (path: `.`)
- Stereotypes: Detected `@Service`
  - Evidence: `src/main/java/com/example/components/InventoryComponents.java:12` (`ev:src/main/java/com/example/components/InventoryComponents.java:12-12:com.example.components.InventoryService:@Service`)

### `com.example.web.ProjectMapController`

- Module: Detected `module:.` (path: `.`)
- Stereotypes: Detected `@RestController`
  - Evidence: `src/main/java/com/example/web/ProjectMapController.java:11` (`ev:src/main/java/com/example/web/ProjectMapController.java:11-11:com.example.web.ProjectMapController:@RestController`)

## Detected JPA Entities

- Analysis status: `analyzed`

### `com.example.domain.ProjectCustomer`

- Module: Detected `module:.` (path: `.`)
- Entity: Detected `com.example.domain.ProjectCustomer`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:20` (`ev:src/main/java/com/example/domain/ProjectEntities.java:20-20:com.example.domain.ProjectCustomer:@Entity`)
- Table: Detected none.
- Identifier field: Detected `id` (`Long`) declared by `com.example.domain.ProjectCustomer` with source_kind `declared`
  - Evidence: `src/main/java/com/example/domain/ProjectEntities.java:22` (`ev:src/main/java/com/example/domain/ProjectEntities.java:22-22:com.example.domain.ProjectCustomer:@Id:field:id`)
- Relationships: Detected none.

### `com.example.domain.ProjectOrder`

- Module: Detected `module:.` (path: `.`)
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

- Module: Detected `module:.` (path: `.`)
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

- Module: Detected `module:.` (path: `.`)
- Test class: Detected `com.example.web.ProjectMapControllerTest`
  - Evidence: `src/test/java/com/example/web/ProjectMapControllerTest.java:3` (`ev:src/test/java/com/example/web/ProjectMapControllerTest.java:3-3:com.example.web.ProjectMapControllerTest:test_file`)
- Source: Detected `src/test/java/com/example/web/ProjectMapControllerTest.java`
- Framework signals: Detected none.
- Inferred tested subject: `com.example.web.ProjectMapController` in target module `module:.` (path: `.`) (support_type: `inferred`, confidence: `medium`)
  - Evidence: `src/test/java/com/example/web/ProjectMapControllerTest.java:3` (`ev:src/test/java/com/example/web/ProjectMapControllerTest.java:3-3:com.example.web.ProjectMapControllerTest:test_file`), `src/main/java/com/example/web/ProjectMapController.java:13` (`ev:src/main/java/com/example/web/ProjectMapController.java:13-13:com.example.web.ProjectMapController:code_symbol`)

## Known Uncertainty And Limits

- Not analyzed: Spring runtime behavior such as component scanning, dependency injection graphs, bean lifecycle, scopes, and conditional configuration is not represented by `components.items`.
- Uncertain: JPA relationship targets preserve `target_resolution: declared_type_only` and `uncertainty: target_type_not_resolved`; no symbol solving or ORM runtime behavior is claimed.
- Not analyzed: JPA mapped-superclass identifier support is limited to conservative source-visible mapped-superclass chains; unresolved, ambiguous, cyclic, or non-source-visible branches are skipped.
- Inferred: tested-subject relations use naming conventions only. Test execution, coverage, assertion behavior, call graphs, and complete subject mapping are not analyzed.
- Not analyzed: connectors, LLM summaries, repository chat, generic RAG, Gradle/Kotlin support, Maven profiles, effective POM reconstruction, dependency graphs, and recursive nested Maven modules are outside this guide.
- Not analyzed: generated sources, OpenAPI operations, generated API reconstruction, classpath-only interfaces, and ambiguous interface endpoint bindings are outside the source-visible interface endpoint support.
- Not analyzed: v0.3 build/config facts are direct local source observations only. Maven execution, effective POM reconstruction, profile activation, remote dependency resolution, config value interpretation, secret extraction, and default generated-source scanning are not performed.
- Not analyzed: Spring Boot application signals do not prove executable packaging, active profiles, runtime auto-configuration, bean graphs, component scanning results, deployment behavior, or actual process entrypoint behavior.

## Practical Inspection Order For Coding Agents

1. Start with detected build, module, and layout facts in `pom.xml`, `src/main/resources/application.yml`, `src/main/java/com/example/Stage3Application.java`.
2. For HTTP behavior, inspect detected endpoint and hidden-surface warning evidence in `src/main/java/com/example/web/ProjectMapController.java`.
3. For Spring wiring changes, inspect detected component evidence in `src/main/java/com/example/components/InventoryComponents.java`, `src/main/java/com/example/web/ProjectMapController.java` and avoid assuming runtime injection graphs.
4. For persistence changes, inspect detected entity evidence in `src/main/java/com/example/domain/ProjectEntities.java` and treat relationship targets as declared-type-only.
5. For tests, inspect detected test files and inferred tested-subject evidence in `src/test/java/com/example/web/ProjectMapControllerTest.java`, `src/main/java/com/example/web/ProjectMapController.java`; do not treat inferred subjects as coverage proof.
