# Agent Guide

Generated deterministically from `project-map.json` and `evidence-index.jsonl`. The guide generator does not re-analyze source files.

## Detected Project Layout

- Build system: Detected `maven`
- Root build file: Detected `pom.xml`
  - Evidence: `pom.xml:1` (`ev:pom.xml:1-1:build_file:pom.xml`)
- Source roots: Detected `services/alpha/src/main/java`, `services/zeta/src/main/java`
  - Evidence: recorded in `project-map.json`; no separate source-root evidence IDs are emitted.
- Test roots: Detected `services/alpha/src/test/java`, `services/zeta/src/test/java`
  - Evidence: recorded in `project-map.json`; no separate test-root evidence IDs are emitted.
- Modules analysis status: `analyzed`
- Module: Detected `module:libraries/common` (path: `libraries/common`)
  - POM path: Detected `libraries/common/pom.xml`
  - Support status: `unsupported`
  - Declaration kind: `root_modules_entry`
  - Declared path: `libraries/common`
  - Source roots: Not analyzed; no supported production roots were recorded for this module.
  - Test roots: Not analyzed; no supported test roots were recorded for this module.
  - Declaration evidence: `pom.xml:14` (`ev:pom.xml:14-14:build_file:module:libraries/common`)
  - POM evidence: `libraries/common/pom.xml:1` (`ev:libraries/common/pom.xml:1-1:build_file:pom.xml`)
- Module: Detected `module:services/alpha` (path: `services/alpha`)
  - POM path: Detected `services/alpha/pom.xml`
  - Support status: `supported`
  - Declaration kind: `root_modules_entry`
  - Declared path: `services/alpha`
  - Source roots: Detected `services/alpha/src/main/java`
  - Source roots evidence: recorded in `project-map.json`; no separate production root evidence IDs are emitted.
  - Test roots: Detected `services/alpha/src/test/java`
  - Test roots evidence: recorded in `project-map.json`; no separate test root evidence IDs are emitted.
  - Declaration evidence: `pom.xml:15` (`ev:pom.xml:15-15:build_file:module:services/alpha`)
  - POM evidence: `services/alpha/pom.xml:1` (`ev:services/alpha/pom.xml:1-1:build_file:pom.xml`)
- Module: Detected `module:services/zeta` (path: `services/zeta`)
  - POM path: Detected `services/zeta/pom.xml`
  - Support status: `supported`
  - Declaration kind: `root_modules_entry`
  - Declared path: `services/zeta`
  - Source roots: Detected `services/zeta/src/main/java`
  - Source roots evidence: recorded in `project-map.json`; no separate production root evidence IDs are emitted.
  - Test roots: Detected `services/zeta/src/test/java`
  - Test roots evidence: recorded in `project-map.json`; no separate test root evidence IDs are emitted.
  - Declaration evidence: `pom.xml:13` (`ev:pom.xml:13-13:build_file:module:services/zeta`)
  - POM evidence: `services/zeta/pom.xml:1` (`ev:services/zeta/pom.xml:1-1:build_file:pom.xml`)

## Build And Configuration Orientation

### Module `module:libraries/common` (path: `libraries/common`)

- Build/config analysis status: `analyzed`
- Source-visible Maven metadata: group_id `value:not_declared` (value_kind: `not_declared`), artifact_id `common-library` (value_kind: `literal`), version `value:not_declared` (value_kind: `not_declared`), packaging `jar` (value_kind: `literal`).
  - Evidence: `libraries/common/pom.xml:14` (`ev:libraries/common/pom.xml:14-14:build_file:maven:project:artifactId`), `libraries/common/pom.xml:15` (`ev:libraries/common/pom.xml:15-15:build_file:maven:project:packaging`), `libraries/common/pom.xml:8` (`ev:libraries/common/pom.xml:8-8:build_file:maven:parent:groupId`), `libraries/common/pom.xml:9` (`ev:libraries/common/pom.xml:9-9:build_file:maven:parent:artifactId`), `libraries/common/pom.xml:10` (`ev:libraries/common/pom.xml:10-10:build_file:maven:parent:version`), ... and 1 more evidence references in `evidence-index.jsonl`
- Source-visible Maven parent: group_id `com.example.regression` (value_kind: `literal`), artifact_id `v03-build-config-parent` (value_kind: `literal`), version `${revision}` (value_kind: `property_reference`), relative_path `../../pom.xml` (value_kind: `literal`).
  - Evidence: `libraries/common/pom.xml:8` (`ev:libraries/common/pom.xml:8-8:build_file:maven:parent:groupId`), `libraries/common/pom.xml:9` (`ev:libraries/common/pom.xml:9-9:build_file:maven:parent:artifactId`), `libraries/common/pom.xml:10` (`ev:libraries/common/pom.xml:10-10:build_file:maven:parent:version`), `libraries/common/pom.xml:11` (`ev:libraries/common/pom.xml:11-11:build_file:maven:parent:relativePath`)
- Source-visible direct dependencies: Detected 1 direct dependency declarations.
  - Dependency: `com.example.common:common-api` declaration_kind `direct_dependency`, version `${common.api.version}` (value_kind: `property_reference`), scope `value:not_declared` (value_kind: `not_declared`), optional `value:not_declared` (value_kind: `not_declared`).
  - Evidence: `libraries/common/pom.xml:18-22` (`ev:libraries/common/pom.xml:18-22:build_file:maven:dependency:000001`)
- Source-visible dependency-management declarations: Detected none.
- Source-visible direct plugins: Detected none.
- Source-visible plugin-management declarations: Detected none.
- Resource roots: Not analyzed; status `not_detected`.
- Config files: Not analyzed; status `not_detected`.
- Spring Boot application signals: Not analyzed; status `not_detected`.
- Module warnings: Detected 1 warning signal for this module: `maven_module:unsupported_module`. See `Known Uncertainty And Limits` for warning evidence and messages.

### Module `module:services/alpha` (path: `services/alpha`)

- Build/config analysis status: `analyzed`
- Source-visible Maven metadata: group_id `value:not_declared` (value_kind: `not_declared`), artifact_id `alpha-service` (value_kind: `literal`), version `value:not_declared` (value_kind: `not_declared`), packaging `value:not_declared` (value_kind: `not_declared`).
  - Evidence: `services/alpha/pom.xml:14` (`ev:services/alpha/pom.xml:14-14:build_file:maven:project:artifactId`), `services/alpha/pom.xml:8` (`ev:services/alpha/pom.xml:8-8:build_file:maven:parent:groupId`), `services/alpha/pom.xml:9` (`ev:services/alpha/pom.xml:9-9:build_file:maven:parent:artifactId`), `services/alpha/pom.xml:10` (`ev:services/alpha/pom.xml:10-10:build_file:maven:parent:version`), `services/alpha/pom.xml:11` (`ev:services/alpha/pom.xml:11-11:build_file:maven:parent:relativePath`)
- Source-visible Maven parent: group_id `com.example.regression` (value_kind: `literal`), artifact_id `v03-build-config-parent` (value_kind: `literal`), version `${revision}` (value_kind: `property_reference`), relative_path `../../pom.xml` (value_kind: `literal`).
  - Evidence: `services/alpha/pom.xml:8` (`ev:services/alpha/pom.xml:8-8:build_file:maven:parent:groupId`), `services/alpha/pom.xml:9` (`ev:services/alpha/pom.xml:9-9:build_file:maven:parent:artifactId`), `services/alpha/pom.xml:10` (`ev:services/alpha/pom.xml:10-10:build_file:maven:parent:version`), `services/alpha/pom.xml:11` (`ev:services/alpha/pom.xml:11-11:build_file:maven:parent:relativePath`)
- Source-visible direct dependencies: Detected 3 direct dependency declarations.
  - Dependency: `com.alpha:alpha-core` declaration_kind `direct_dependency`, version `1.2-${revision}` (value_kind: `expression`), scope `value:not_declared` (value_kind: `not_declared`), optional `value:not_declared` (value_kind: `not_declared`).
  - Evidence: `services/alpha/pom.xml:40-46` (`ev:services/alpha/pom.xml:40-46:build_file:maven:dependency:000003`)
  - Dependency: `com.zeta:zeta-helper` declaration_kind `direct_dependency`, version `value:not_declared` (value_kind: `not_declared`), scope `test` (value_kind: `literal`), optional `false` (value_kind: `literal`).
  - Evidence: `services/alpha/pom.xml:29-34` (`ev:services/alpha/pom.xml:29-34:build_file:maven:dependency:000001`)
  - Dependency: `org.springframework.boot:spring-boot-starter-web` declaration_kind `direct_dependency`, version `${spring.boot.version}` (value_kind: `property_reference`), scope `value:not_declared` (value_kind: `not_declared`), optional `value:not_declared` (value_kind: `not_declared`).
  - Evidence: `services/alpha/pom.xml:35-39` (`ev:services/alpha/pom.xml:35-39:build_file:maven:dependency:000002`)
- Source-visible dependency-management declarations: Detected 1 dependency-management declarations; these are management declarations, not active resolved dependencies.
  - Dependency: `com.example.alpha:alpha-managed-bom` declaration_kind `dependency_management`, version `${alpha.managed.version}` (value_kind: `property_reference`), scope `import` (value_kind: `literal`), optional `value:not_declared` (value_kind: `not_declared`).
  - Evidence: `services/alpha/pom.xml:18-24` (`ev:services/alpha/pom.xml:18-24:build_file:maven:dependency_management:000001`)
- Source-visible direct plugins: Detected 2 direct plugin declarations.
  - Plugin: `org.codehaus.mojo:build-helper-maven-plugin` declaration_kind `direct_plugin`, version `value:not_declared` (value_kind: `not_declared`).
    - Direct execution declarations: `id=not_declared, phase=phase:not_declared, goals=add-source`
    - Configuration signals: `add_source_goal_present`
    - Generator signals: none recorded
  - Evidence: `services/alpha/pom.xml:68-78` (`ev:services/alpha/pom.xml:68-78:build_file:maven:plugin:000001`)
  - Plugin: `org.springframework.boot:spring-boot-maven-plugin` declaration_kind `direct_plugin`, version `${spring.boot.version}` (value_kind: `property_reference`).
    - Direct execution declarations: none recorded.
    - Configuration signals: none recorded
    - Generator signals: none recorded
  - Evidence: `services/alpha/pom.xml:79-83` (`ev:services/alpha/pom.xml:79-83:build_file:maven:plugin:000002`)
- Source-visible plugin-management declarations: Detected 1 plugin-management declarations; these are management declarations, not active plugin executions.
  - Plugin: `org.apache.maven.plugins:maven-compiler-plugin` declaration_kind `plugin_management`, version `3.12.1` (value_kind: `literal`).
    - Direct execution declarations: none recorded.
    - Configuration signals: `annotation_processor_paths_present`
    - Generator signals: `annotation_processor`
  - Evidence: `services/alpha/pom.xml:52-64` (`ev:services/alpha/pom.xml:52-64:build_file:maven:plugin_management:000001`)
- Resource roots: Detected 2 standard resource roots.
  - Resource root: `main` `services/alpha/src/main/resources`
    - Evidence: recorded in `project-map.json`; no separate resource-root evidence IDs are emitted.
  - Resource root: `test` `services/alpha/src/test/resources`
    - Evidence: recorded in `project-map.json`; no separate resource-root evidence IDs are emitted.
- Config files: Detected 4 path-only supported config files; config contents, keys, and values are not rendered.
  - Config file: `services/alpha/src/main/resources/logback-spring.xml` kind `logging_config`, format `xml`.
  - Evidence: `services/alpha/src/main/resources/logback-spring.xml` (`ev:services/alpha/src/main/resources/logback-spring.xml:unknown:config_file:logback-spring.xml`)
  - Config file: `services/alpha/src/main/resources/application-ci.properties` kind `spring_application`, format `properties`, filename-derived profile `ci` from `filename_only`.
  - Evidence: `services/alpha/src/main/resources/application-ci.properties` (`ev:services/alpha/src/main/resources/application-ci.properties:unknown:config_file:application-ci.properties`)
  - Config file: `services/alpha/src/main/resources/application.yml` kind `spring_application`, format `yaml`.
  - Evidence: `services/alpha/src/main/resources/application.yml` (`ev:services/alpha/src/main/resources/application.yml:unknown:config_file:application.yml`)
  - Config file: `services/alpha/src/test/resources/application-test.yaml` kind `spring_application`, format `yaml`, filename-derived profile `test` from `filename_only`.
  - Evidence: `services/alpha/src/test/resources/application-test.yaml` (`ev:services/alpha/src/test/resources/application-test.yaml:unknown:config_file:application-test.yaml`)
- Spring Boot application signals: Detected 1 direct `@SpringBootApplication` class signal.
  - Spring Boot application: Detected `com.example.alpha.AlphaApplication` at `services/alpha/src/main/java/com/example/alpha/AlphaApplication.java` with signal `spring_boot_application_with_main_method`.
    - Main method: Detected source-visible `main` method.
  - Evidence: `services/alpha/src/main/java/com/example/alpha/AlphaApplication.java:3` (`ev:services/alpha/src/main/java/com/example/alpha/AlphaApplication.java:3-3:com.example.alpha.AlphaApplication:@SpringBootApplication`), `services/alpha/src/main/java/com/example/alpha/AlphaApplication.java:5` (`ev:services/alpha/src/main/java/com/example/alpha/AlphaApplication.java:5-5:com.example.alpha.AlphaApplication#main:code_symbol`)
- Module warnings: Detected 2 warning signals for this module: `generated_source:maven_annotation_processor`, `generated_source:maven_build_helper_add_source`. See `Known Uncertainty And Limits` for warning evidence and messages.

### Module `module:services/zeta` (path: `services/zeta`)

- Build/config analysis status: `analyzed`
- Source-visible Maven metadata: group_id `com.example.regression` (value_kind: `literal`), artifact_id `zeta-service` (value_kind: `literal`), version `1.0-${revision}` (value_kind: `expression`), packaging `war` (value_kind: `literal`).
  - Evidence: `services/zeta/pom.xml:7` (`ev:services/zeta/pom.xml:7-7:build_file:maven:project:groupId`), `services/zeta/pom.xml:8` (`ev:services/zeta/pom.xml:8-8:build_file:maven:project:artifactId`), `services/zeta/pom.xml:9` (`ev:services/zeta/pom.xml:9-9:build_file:maven:project:version`), `services/zeta/pom.xml:10` (`ev:services/zeta/pom.xml:10-10:build_file:maven:project:packaging`)
- Source-visible direct dependencies: Detected 2 direct dependency declarations.
  - Dependency: `com.zeta:zeta-runtime` declaration_kind `direct_dependency`, version `${zeta.runtime.version}` (value_kind: `property_reference`), scope `value:not_declared` (value_kind: `not_declared`), optional `value:not_declared` (value_kind: `not_declared`).
  - Evidence: `services/zeta/pom.xml:29-34` (`ev:services/zeta/pom.xml:29-34:build_file:maven:dependency:000002`)
  - Dependency: `org.springframework.boot:spring-boot-starter` declaration_kind `direct_dependency`, version `value:not_declared` (value_kind: `not_declared`), scope `value:not_declared` (value_kind: `not_declared`), optional `value:not_declared` (value_kind: `not_declared`).
  - Evidence: `services/zeta/pom.xml:25-28` (`ev:services/zeta/pom.xml:25-28:build_file:maven:dependency:000001`)
- Source-visible dependency-management declarations: Detected 1 dependency-management declarations; these are management declarations, not active resolved dependencies.
  - Dependency: `com.example.zeta:zeta-managed-bom` declaration_kind `dependency_management`, version `${zeta.managed.version}` (value_kind: `property_reference`), scope `import` (value_kind: `literal`), optional `value:not_declared` (value_kind: `not_declared`).
  - Evidence: `services/zeta/pom.xml:14-20` (`ev:services/zeta/pom.xml:14-20:build_file:maven:dependency_management:000001`)
- Source-visible direct plugins: Detected 2 direct plugin declarations.
  - Plugin: `org.codehaus.mojo:jaxb2-maven-plugin` declaration_kind `direct_plugin`, version `value:not_declared` (value_kind: `not_declared`).
    - Direct execution declarations: none recorded.
    - Configuration signals: none recorded
    - Generator signals: `source_generator_plugin`
  - Evidence: `services/zeta/pom.xml:39-42` (`ev:services/zeta/pom.xml:39-42:build_file:maven:plugin:000001`)
  - Plugin: `org.openapitools:openapi-generator-maven-plugin` declaration_kind `direct_plugin`, version `${openapi.generator.version}` (value_kind: `property_reference`).
    - Direct execution declarations: `id=generate-zeta-api, phase=generate-sources, goals=generate`
    - Configuration signals: `generated_sources_config_present`, `input_spec_config_present`
    - Generator signals: `openapi_swagger_codegen`
  - Evidence: `services/zeta/pom.xml:43-62` (`ev:services/zeta/pom.xml:43-62:build_file:maven:plugin:000002`)
- Source-visible plugin-management declarations: Detected none.
- Resource roots: Detected 1 standard resource root.
  - Resource root: `main` `services/zeta/src/main/resources`
    - Evidence: recorded in `project-map.json`; no separate resource-root evidence IDs are emitted.
- Config files: Detected 2 path-only supported config files; config contents, keys, and values are not rendered.
  - Config file: `services/zeta/src/main/resources/log4j2.xml` kind `logging_config`, format `xml`.
  - Evidence: `services/zeta/src/main/resources/log4j2.xml` (`ev:services/zeta/src/main/resources/log4j2.xml:unknown:config_file:log4j2.xml`)
  - Config file: `services/zeta/src/main/resources/application.yaml` kind `spring_application`, format `yaml`.
  - Evidence: `services/zeta/src/main/resources/application.yaml` (`ev:services/zeta/src/main/resources/application.yaml:unknown:config_file:application.yaml`)
- Spring Boot application signals: Detected 1 direct `@SpringBootApplication` class signal.
  - Spring Boot application: Detected `com.example.zeta.ZetaApplication` at `services/zeta/src/main/java/com/example/zeta/ZetaApplication.java` with signal `spring_boot_application_annotation_only`.
    - Main method: Detected none on the annotated class.
  - Evidence: `services/zeta/src/main/java/com/example/zeta/ZetaApplication.java:3` (`ev:services/zeta/src/main/java/com/example/zeta/ZetaApplication.java:3-3:com.example.zeta.ZetaApplication:@SpringBootApplication`)
- Module warnings: Detected 5 warning signals for this module: `generated_source:maven_generated_source_config`, `generated_source:maven_generator_plugin`, `generated_source:maven_openapi_swagger_codegen_plugin`, `hidden_http_surface:maven_openapi_swagger_codegen_plugin`, `hidden_http_surface:openapi_spec_file`. See `Known Uncertainty And Limits` for warning evidence and messages.

## API Surface Interpretation

- API surface analysis status: `analyzed`
- Source-visible Spring MVC endpoint facts are code-backed local source observations from `endpoints[]`; they do not prove complete runtime handler mappings.
- Source-visible interface-declared endpoint facts are code-backed only when the interface mapping and unique concrete binding are both source-visible.
- Declared OpenAPI operations are spec-backed contract facts with `implementation_status: "not_analyzed"`; they are not implemented endpoint facts.
- Generated-source API signals, repository-rest warnings, and hidden HTTP warnings are inspection hints, not endpoint or operation facts.
- LLM output, generated Markdown, release notes, and chat text are never evidence for API surface facts or relations.
- Source-visible direct Spring MVC endpoint IDs: status `analyzed`; detected 2 IDs `endpoint:module:services/alpha:com.example.alpha.AlphaController#status`, `endpoint:module:services/zeta:com.example.zeta.ZetaController#create`.
- Source-visible interface-declared Spring MVC endpoint IDs: status `analyzed`; detected none.
- OpenAPI/Swagger spec files: status `analyzed`; detected 1 local spec file as declared API inputs.
  - Spec file: `services/zeta/src/main/resources/openapi.yml` kind `openapi`, format `yaml`, version `3.0.0`.
- Module: Detected `module:services/zeta` (path: `services/zeta`)
  - Evidence: `services/zeta/src/main/resources/openapi.yml:1` (`ev:services/zeta/src/main/resources/openapi.yml:1-1:api_spec:openapi`)
- OpenAPI/Swagger operations: status `analyzed`; detected no declared operation facts.
- Generated-source API warning IDs: status `analyzed`; referenced 3 warning IDs `warning:generated_source:maven_generated_source_config:module:services/zeta:direct_plugin:decl:000002`, `warning:generated_source:maven_openapi_swagger_codegen_plugin:module:services/zeta:direct_plugin:decl:000002`, `warning:hidden_http_surface:maven_openapi_swagger_codegen_plugin:module:services/zeta:services/zeta/pom.xml:openapi-generator-maven-plugin`.
- Repository-rest warning IDs: status `analyzed`; detected none.
- Hidden HTTP warning IDs: status `analyzed`; detected none.

## Spring Application Surface

- Spring application surface analysis status: `analyzed`
- Repository stereotype entries are direct `@Repository` annotation observations; they do not prove runtime bean registration or entity ownership.
- Spring Data repository interface entries are inferred source-visible extension signals; they do not prove runtime repositories, query method behavior, database access, or repository-to-entity relations.
- Configuration classes, configuration-properties types, and `@Bean` methods are source-visible Spring configuration signals; they do not prove runtime bean graphs, binding success, config values, bean scopes, lifecycle, proxy behavior, or dependency graphs.
- Transaction, scheduled, event listener, and messaging listener entries are source-visible operational change-surface signals; they do not prove runtime transaction behavior, scheduler registration, event delivery, message destinations, or broker topology.
- Spring Security configuration warnings are inspection hints and change-risk signals; they do not prove security policy, endpoint protection, authentication behavior, authorization behavior, vulnerability, or correctness.
- Subsection statuses: repositories `analyzed`, configuration classes `analyzed`, configuration properties `analyzed`, bean methods `analyzed`, transaction boundaries `analyzed`, scheduled methods `analyzed`, event listeners `analyzed`, messaging listeners `analyzed`, security warnings `analyzed`.
- Spring application surface facts: detected none for supported modules.

## Detected Spring MVC Endpoints

### `GET /alpha/status`

- Module: Detected `module:services/alpha` (path: `services/alpha`)
- Controller: Detected `com.example.alpha.AlphaController`
- Handler: Detected `status`
- Mapping source: Detected `direct_handler_method` from `com.example.alpha.AlphaController#status` with binding `direct`
- HTTP methods: Detected `GET`
- Paths: Detected `/alpha/status`
- Request parameters: Detected `request_param:detail (String)`
- Request body: Detected none.
- Response: Detected `AlphaStatus`
  - Evidence: `services/alpha/src/main/java/com/example/alpha/AlphaApplication.java:9` (`ev:services/alpha/src/main/java/com/example/alpha/AlphaApplication.java:9-9:com.example.alpha.AlphaController:@RestController`), `services/alpha/src/main/java/com/example/alpha/AlphaApplication.java:10` (`ev:services/alpha/src/main/java/com/example/alpha/AlphaApplication.java:10-10:com.example.alpha.AlphaController:@RequestMapping`), `services/alpha/src/main/java/com/example/alpha/AlphaApplication.java:12` (`ev:services/alpha/src/main/java/com/example/alpha/AlphaApplication.java:12-12:com.example.alpha.AlphaController#status:@GetMapping`), `services/alpha/src/main/java/com/example/alpha/AlphaApplication.java:13` (`ev:services/alpha/src/main/java/com/example/alpha/AlphaApplication.java:13-13:com.example.alpha.AlphaController#status:@RequestParam:parameter:0:detail`)

### `POST /zeta/items`

- Module: Detected `module:services/zeta` (path: `services/zeta`)
- Controller: Detected `com.example.zeta.ZetaController`
- Handler: Detected `create`
- Mapping source: Detected `direct_handler_method` from `com.example.zeta.ZetaController#create` with binding `direct`
- HTTP methods: Detected `POST`
- Paths: Detected `/zeta/items`
- Request parameters: Detected none.
- Request body: Detected `ZetaItem`
- Response: Detected `ZetaItem`
  - Evidence: `services/zeta/src/main/java/com/example/zeta/ZetaApplication.java:7` (`ev:services/zeta/src/main/java/com/example/zeta/ZetaApplication.java:7-7:com.example.zeta.ZetaController:@RestController`), `services/zeta/src/main/java/com/example/zeta/ZetaApplication.java:8` (`ev:services/zeta/src/main/java/com/example/zeta/ZetaApplication.java:8-8:com.example.zeta.ZetaController:@RequestMapping`), `services/zeta/src/main/java/com/example/zeta/ZetaApplication.java:10` (`ev:services/zeta/src/main/java/com/example/zeta/ZetaApplication.java:10-10:com.example.zeta.ZetaController#create:@PostMapping`), `services/zeta/src/main/java/com/example/zeta/ZetaApplication.java:11` (`ev:services/zeta/src/main/java/com/example/zeta/ZetaApplication.java:11-11:com.example.zeta.ZetaController#create:@RequestBody:parameter:0:item`)

## Detected Spring Components

- Analysis status: `analyzed`

### `com.example.alpha.AlphaController`

- Module: Detected `module:services/alpha` (path: `services/alpha`)
- Stereotypes: Detected `@RestController`
  - Evidence: `services/alpha/src/main/java/com/example/alpha/AlphaApplication.java:9` (`ev:services/alpha/src/main/java/com/example/alpha/AlphaApplication.java:9-9:com.example.alpha.AlphaController:@RestController`)

### `com.example.zeta.ZetaController`

- Module: Detected `module:services/zeta` (path: `services/zeta`)
- Stereotypes: Detected `@RestController`
  - Evidence: `services/zeta/src/main/java/com/example/zeta/ZetaApplication.java:7` (`ev:services/zeta/src/main/java/com/example/zeta/ZetaApplication.java:7-7:com.example.zeta.ZetaController:@RestController`)

## Detected JPA Entities

- Analysis status: `analyzed`
- Detected: no direct JPA entities recorded.

### Embeddables

- Analysis status: `analyzed`
- Detected: no direct `@Embeddable` classes recorded.

## Detected Tests

- Analysis status: `analyzed`

### `com.example.alpha.AlphaControllerTest`

- Module: Detected `module:services/alpha` (path: `services/alpha`)
- Test class: Detected `com.example.alpha.AlphaControllerTest`
  - Evidence: `services/alpha/src/test/java/com/example/alpha/AlphaControllerTest.java:5` (`ev:services/alpha/src/test/java/com/example/alpha/AlphaControllerTest.java:5-5:com.example.alpha.AlphaControllerTest:test_file`)
- Source: Detected `services/alpha/src/test/java/com/example/alpha/AlphaControllerTest.java`
- Framework signal: Detected `JUnit Jupiter`
  - Evidence: `services/alpha/src/test/java/com/example/alpha/AlphaControllerTest.java:3` (`ev:services/alpha/src/test/java/com/example/alpha/AlphaControllerTest.java:3-3:com.example.alpha.AlphaControllerTest:import:org.junit.jupiter.api.Test`), `services/alpha/src/test/java/com/example/alpha/AlphaControllerTest.java:6` (`ev:services/alpha/src/test/java/com/example/alpha/AlphaControllerTest.java:6-6:com.example.alpha.AlphaControllerTest#status:@Test`)
- Inferred tested subject: `com.example.alpha.AlphaController` in target module `module:services/alpha` (path: `services/alpha`) (support_type: `inferred`, confidence: `medium`)
  - Evidence: `services/alpha/src/test/java/com/example/alpha/AlphaControllerTest.java:5` (`ev:services/alpha/src/test/java/com/example/alpha/AlphaControllerTest.java:5-5:com.example.alpha.AlphaControllerTest:test_file`), `services/alpha/src/main/java/com/example/alpha/AlphaApplication.java:11` (`ev:services/alpha/src/main/java/com/example/alpha/AlphaApplication.java:11-11:com.example.alpha.AlphaController:code_symbol`)

### `com.example.zeta.ZetaControllerTest`

- Module: Detected `module:services/zeta` (path: `services/zeta`)
- Test class: Detected `com.example.zeta.ZetaControllerTest`
  - Evidence: `services/zeta/src/test/java/com/example/zeta/ZetaControllerTest.java:5` (`ev:services/zeta/src/test/java/com/example/zeta/ZetaControllerTest.java:5-5:com.example.zeta.ZetaControllerTest:test_file`)
- Source: Detected `services/zeta/src/test/java/com/example/zeta/ZetaControllerTest.java`
- Framework signal: Detected `JUnit Jupiter`
  - Evidence: `services/zeta/src/test/java/com/example/zeta/ZetaControllerTest.java:3` (`ev:services/zeta/src/test/java/com/example/zeta/ZetaControllerTest.java:3-3:com.example.zeta.ZetaControllerTest:import:org.junit.jupiter.api.Test`), `services/zeta/src/test/java/com/example/zeta/ZetaControllerTest.java:6` (`ev:services/zeta/src/test/java/com/example/zeta/ZetaControllerTest.java:6-6:com.example.zeta.ZetaControllerTest#create:@Test`)
- Inferred tested subject: `com.example.zeta.ZetaController` in target module `module:services/zeta` (path: `services/zeta`) (support_type: `inferred`, confidence: `medium`)
  - Evidence: `services/zeta/src/test/java/com/example/zeta/ZetaControllerTest.java:5` (`ev:services/zeta/src/test/java/com/example/zeta/ZetaControllerTest.java:5-5:com.example.zeta.ZetaControllerTest:test_file`), `services/zeta/src/main/java/com/example/zeta/ZetaApplication.java:9` (`ev:services/zeta/src/main/java/com/example/zeta/ZetaApplication.java:9-9:com.example.zeta.ZetaController:code_symbol`)

## Known Uncertainty And Limits

- Warning: `generated_source` signal `maven_annotation_processor` for module `module:services/alpha` (path: `services/alpha`) at `services/alpha/pom.xml`. Maven annotation processor signal detected; the analyzer does not inspect generated sources or infer generated APIs from processors.
  - Evidence: `services/alpha/pom.xml:57-62` (`ev:services/alpha/pom.xml:57-62:build_file:maven:plugin_management:000001:configuration:annotationProcessorPaths`)
- Warning: `generated_source` signal `maven_build_helper_add_source` for module `module:services/alpha` (path: `services/alpha`) at `services/alpha/pom.xml`. Maven build-helper add-source goal detected; the analyzer does not scan added or generated sources by default.
  - Evidence: `services/alpha/pom.xml:74` (`ev:services/alpha/pom.xml:74-74:build_file:maven:plugin:000001:execution:000001:goal:000001`)
- Warning: `generated_source` signal `maven_generated_source_config` for module `module:services/zeta` (path: `services/zeta`) at `services/zeta/pom.xml`. Maven generated-source configuration signal detected; the analyzer records the bounded build signal only and does not inspect configured generated output.
  - Evidence: `services/zeta/pom.xml:55` (`ev:services/zeta/pom.xml:55-55:build_file:maven:plugin:000002:execution:000001:configuration:generatedSourcesDirectory`)
- Warning: `generated_source` signal `maven_generator_plugin` for module `module:services/zeta` (path: `services/zeta`) at `services/zeta/pom.xml`. Maven source generator plugin declaration detected; the analyzer records the source-visible build signal only and does not scan generated sources by default.
  - Evidence: `services/zeta/pom.xml:41` (`ev:services/zeta/pom.xml:41-41:build_file:maven:plugin:000001:artifactId`)
- Warning: `generated_source` signal `maven_openapi_swagger_codegen_plugin` for module `module:services/zeta` (path: `services/zeta`) at `services/zeta/pom.xml`. Maven OpenAPI/Swagger code generation plugin declaration detected; the analyzer does not run code generation, scan generated sources by default, or create endpoint/API facts from this build signal.
  - Evidence: `services/zeta/pom.xml:45` (`ev:services/zeta/pom.xml:45-45:build_file:maven:plugin:000002:artifactId`)
- Warning: `hidden_http_surface` signal `maven_openapi_swagger_codegen_plugin` for module `module:services/zeta` (path: `services/zeta`) at `services/zeta/pom.xml`. Maven OpenAPI/Swagger code generation plugin signal detected; the analyzer does not run generation or scan generated sources by default.
  - Evidence: `services/zeta/pom.xml:45` (`ev:services/zeta/pom.xml:45-45:build_file:openapi-generator-maven-plugin`)
- Warning: `hidden_http_surface` signal `openapi_spec_file` for module `module:services/zeta` (path: `services/zeta`) at `services/zeta/src/main/resources/openapi.yml`. OpenAPI/Swagger spec file detected by filename; declared operations, when supported, are reported separately under api\_surface.openapi.operations, and this warning does not reconstruct generated APIs.
  - Evidence: `services/zeta/src/main/resources/openapi.yml` (`ev:services/zeta/src/main/resources/openapi.yml:unknown:config_file:openapi.yml`)
- Warning: `maven_module` signal `unsupported_module` for module `module:libraries/common` (path: `libraries/common`) at `libraries/common/pom.xml`. Maven module has a child pom.xml but no supported Java source, test, or resource roots; the analyzer does not inspect this module.
  - Evidence: `pom.xml:14` (`ev:pom.xml:14-14:build_file:module:libraries/common`), `libraries/common/pom.xml:1` (`ev:libraries/common/pom.xml:1-1:build_file:pom.xml`)
- Not analyzed: Spring runtime behavior such as component scanning, dependency injection graphs, bean lifecycle, scopes, and conditional configuration is not represented by `components.items`.
- Uncertain: JPA relationship targets preserve `target_resolution: declared_type_only` and `uncertainty: target_type_not_resolved`; no symbol solving or ORM runtime behavior is claimed.
- Not analyzed: JPA mapped-superclass identifier support is limited to conservative source-visible mapped-superclass chains; unresolved, ambiguous, cyclic, or non-source-visible branches are skipped.
- Partial: JPA embedded and composite identifier support is limited to direct source-visible `@Embeddable`, `@Embedded`, `@EmbeddedId`, and `@IdClass` signals. Embedded targets are linked only when a unique local `@Embeddable` can be matched; `@IdClass` field matching and composite-key semantics are not analyzed.
- Inferred: tested-subject relations use naming conventions only. Test execution, coverage, assertion behavior, call graphs, and complete subject mapping are not analyzed.
- Not analyzed: connectors, LLM summaries, repository chat, generic RAG, Gradle/Kotlin support, Maven profiles, effective POM reconstruction, dependency graphs, and recursive nested Maven modules are outside this guide.
- Not analyzed: generated sources, generated API reconstruction, classpath-only interfaces, and ambiguous interface endpoint bindings are outside the source-visible interface endpoint support.
- Not analyzed: OpenAPI operation facts are spec-backed declared operations only; runtime implementation matching, source/spec agreement, generated source contents, and client SDK reconstruction are not claimed.
- Not analyzed: v0.3 build/config facts are direct local source observations only. Maven execution, effective POM reconstruction, profile activation, remote dependency resolution, config value interpretation, secret extraction, and default generated-source scanning are not performed.
- Not analyzed: Spring Boot application signals do not prove executable packaging, active profiles, runtime auto-configuration, bean graphs, component scanning results, deployment behavior, or actual process entrypoint behavior.
- Not analyzed: Spring Data repository interface signals do not prove runtime repository registration, query method behavior, database access, or repository-to-entity relations; `entity_relation_status: not_analyzed` is preserved for those inferred signals.
- Not analyzed: JPA field metadata is limited to supported direct field-level source-visible annotations. It is not a complete persistent-property inventory, does not support getter/property access in this slice, and does not fill missing annotation attributes from JPA provider defaults.
- Not analyzed: v0.5 transaction, scheduling, event listener, and messaging listener facts are annotation-presence change-surface signals only. Transaction propagation, scheduler registration, event delivery, message destinations, broker topology, consumer groups, and delivery semantics are not claimed.
- Not analyzed: Security policy, endpoint protection state, authentication behavior, authorization behavior, filter-chain ordering, vulnerabilities, and correctness are not claimed. v0.5 Spring Security configuration warnings are bounded source-visible inspection hints only.
- Uncertain: no entity facts were recorded, so persistence mappings may be absent or outside the currently supported analyzer scope.

## Practical Inspection Order For Coding Agents

1. Start with detected build, module, and layout facts in `pom.xml`, `libraries/common/pom.xml`, `services/alpha/pom.xml`, `services/alpha/src/main/resources/logback-spring.xml`, `services/alpha/src/main/resources/application-ci.properties`, ... and 7 more evidence paths in `evidence-index.jsonl`.
2. For HTTP behavior, inspect detected endpoint and hidden-surface warning evidence in `services/alpha/src/main/java/com/example/alpha/AlphaApplication.java`, `services/zeta/src/main/java/com/example/zeta/ZetaApplication.java`, `services/zeta/src/main/resources/openapi.yml`, `services/zeta/pom.xml`.
3. For Spring application surface changes, inspect Spring application surface and component evidence in `services/alpha/src/main/java/com/example/alpha/AlphaApplication.java`, `services/zeta/src/main/java/com/example/zeta/ZetaApplication.java` and avoid assuming runtime repository registration, entity ownership, injection graphs, transaction behavior, scheduler registration, event delivery, or messaging topology.
4. For persistence changes, inspect detected entity evidence (no evidence paths recorded) and treat field metadata as source-visible annotations only, not runtime schema, provider defaults, or complete access-strategy reconstruction; relationship targets remain declared-type-only.
5. For tests, inspect detected test files and inferred tested-subject evidence in `services/alpha/src/test/java/com/example/alpha/AlphaControllerTest.java`, `services/alpha/src/main/java/com/example/alpha/AlphaApplication.java`, `services/zeta/src/test/java/com/example/zeta/ZetaControllerTest.java`, `services/zeta/src/main/java/com/example/zeta/ZetaApplication.java`; do not treat inferred subjects as coverage proof.
