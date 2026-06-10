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
- Source-visible Maven metadata: group_id `com.example` (value_kind: `literal`), artifact_id `v0-7-guide-quality-regression` (value_kind: `literal`), version `1.0.0` (value_kind: `literal`), packaging `value:not_declared` (value_kind: `not_declared`).
  - Evidence: `pom.xml:3` (`ev:pom.xml:3-3:build_file:maven:project:groupId`), `pom.xml:4` (`ev:pom.xml:4-4:build_file:maven:project:artifactId`), `pom.xml:5` (`ev:pom.xml:5-5:build_file:maven:project:version`)
- Source-visible direct dependencies: Detected none.
- Source-visible dependency-management declarations: Detected none.
- Source-visible direct plugins: Detected none.
- Source-visible plugin-management declarations: Detected none.
- Resource roots: Not analyzed; status `not_detected`.
- Config files: Not analyzed; status `not_detected`.
- Spring Boot application signals: Detected none.
- Module warnings: Detected 2 warning signals for this module: `spring_security:security_configuration_annotation`, `spring_security:security_filter_chain_bean`. See `Known Uncertainty And Limits` for warning evidence and messages.

## API Surface Interpretation

- API surface analysis status: `analyzed`
- Source-visible Spring MVC endpoint facts are code-backed local source observations from `endpoints[]`; they do not prove complete runtime handler mappings.
- Source-visible interface-declared endpoint facts are code-backed only when the interface mapping and unique concrete binding are both source-visible.
- Declared OpenAPI operations are spec-backed contract facts with `implementation_status: "not_analyzed"`; they are not implemented endpoint facts.
- Generated-source API signals, repository-rest warnings, and hidden HTTP warnings are inspection hints, not endpoint or operation facts.
- LLM output, generated Markdown, release notes, and chat text are never evidence for API surface facts or relations.
- Source-visible direct Spring MVC endpoint IDs: status `analyzed`; detected 1 ID `endpoint:com.example.web.OrderController#getOrder`.
- Source-visible interface-declared Spring MVC endpoint IDs: status `analyzed`; detected none.
- OpenAPI/Swagger spec files: status `analyzed`; detected none.
- OpenAPI/Swagger operations: status `not_detected`; detected none.
- Generated-source API warning IDs: status `analyzed`; detected none.
- Repository-rest warning IDs: status `analyzed`; detected none.
- Hidden HTTP warning IDs: status `analyzed`; detected none.

## Spring Application Surface

- Spring application surface analysis status: `analyzed`
- Repository stereotype entries are direct `@Repository` annotation observations; they do not prove runtime bean registration or entity ownership.
- Spring Data repository interface entries are inferred source-visible extension signals; repository/entity relation rows, when present, are inferred generic links. They do not prove runtime repositories, query method behavior, database access, or runtime repository/entity verification.
- Configuration classes, configuration-properties types, and `@Bean` methods are source-visible Spring configuration signals; they do not prove runtime bean graphs, binding success, config values, bean scopes, lifecycle, proxy behavior, or dependency graphs.
- Transaction, scheduled, event listener, and messaging listener entries are source-visible operational change-surface signals; they do not prove runtime transaction behavior, scheduler registration, event delivery, message destinations, or broker topology.
- Spring Security configuration warnings are inspection hints and change-risk signals; they do not prove security policy, endpoint protection, authentication behavior, authorization behavior, vulnerability, or correctness.
- Subsection statuses: repositories `analyzed`, configuration classes `analyzed`, configuration properties `analyzed`, bean methods `analyzed`, transaction boundaries `analyzed`, scheduled methods `analyzed`, event listeners `analyzed`, messaging listeners `analyzed`, security warnings `analyzed`.

### Module `module:.` (path: `.`)

- Extracted facts: detected 4 source-visible facts.
  - `spring_configuration_class`: `com.example.config.AppConfig` (support_type: `extracted`, configuration_signal: `direct_configuration_class`).
    - Source: `src/main/java/com/example/config/AppConfig.java`
    - Evidence: `src/main/java/com/example/config/AppConfig.java:6` (`ev:src/main/java/com/example/config/AppConfig.java:6-6:com.example.config.AppConfig:@Configuration`)
  - `spring_bean_method`: `com.example.config.AppConfig#orderBean` (support_type: `extracted`, bean_signal: `direct_bean_method`).
    - Source: `src/main/java/com/example/config/AppConfig.java`
    - Evidence: `src/main/java/com/example/config/AppConfig.java:8` (`ev:src/main/java/com/example/config/AppConfig.java:8-8:com.example.config.AppConfig#orderBean:@Bean`)
  - `spring_bean_method`: `com.example.security.SecurityConfig#appSecurity` (support_type: `extracted`, bean_signal: `direct_bean_method`).
    - Source: `src/main/java/com/example/security/SecurityConfig.java`
    - Evidence: `src/main/java/com/example/security/SecurityConfig.java:9` (`ev:src/main/java/com/example/security/SecurityConfig.java:9-9:com.example.security.SecurityConfig#appSecurity:@Bean`)
  - `spring_transaction_boundary`: `com.example.service.OrderService#settleOrder` (support_type: `extracted`, transaction_signal: `direct_transactional_method`, target_kind: `method`, annotation_symbol: `@Transactional`).
    - Source: `src/main/java/com/example/service/OrderService.java`
    - Evidence: `src/main/java/com/example/service/OrderService.java:8` (`ev:src/main/java/com/example/service/OrderService.java:8-8:com.example.service.OrderService#settleOrder:@Transactional`)
- Inferred signals: detected 1 source-visible signal.
  - `spring_data_repository_interface_signal`: `com.example.repositories.MissingOrderRepository` extends `org.springframework.data.jpa.repository.JpaRepository` (support_type: `inferred`, repository_signal: `spring_data_repository_interface_extension`).
    - Source: `src/main/java/com/example/repositories/MissingOrderRepository.java`
    - Evidence: `src/main/java/com/example/repositories/MissingOrderRepository.java:6-7` (`ev:src/main/java/com/example/repositories/MissingOrderRepository.java:6-7:com.example.repositories.MissingOrderRepository:com.example.repositories.MissingOrderRepository`), `src/main/java/com/example/repositories/MissingOrderRepository.java:6` (`ev:src/main/java/com/example/repositories/MissingOrderRepository.java:6-6:com.example.repositories.MissingOrderRepository:extends:org.springframework.data.jpa.repository.JpaRepository`)
- Uncertain/not-analyzed statuses: detected 3 explicit statuses.
  - `com.example.repositories.MissingOrderRepository`: `entity_relation_status` is `not_detected`; no runtime repository/entity relation is claimed.
    - Evidence: `src/main/java/com/example/repositories/MissingOrderRepository.java:6-7` (`ev:src/main/java/com/example/repositories/MissingOrderRepository.java:6-7:com.example.repositories.MissingOrderRepository:com.example.repositories.MissingOrderRepository`), `src/main/java/com/example/repositories/MissingOrderRepository.java:6` (`ev:src/main/java/com/example/repositories/MissingOrderRepository.java:6-6:com.example.repositories.MissingOrderRepository:extends:org.springframework.data.jpa.repository.JpaRepository`)
  - `com.example.config.AppConfig#orderBean`: `bean_name_status` is `not_analyzed`; no effective runtime bean name is claimed.
    - Evidence: `src/main/java/com/example/config/AppConfig.java:8` (`ev:src/main/java/com/example/config/AppConfig.java:8-8:com.example.config.AppConfig#orderBean:@Bean`)
  - `com.example.security.SecurityConfig#appSecurity`: `bean_name_status` is `not_analyzed`; no effective runtime bean name is claimed.
    - Evidence: `src/main/java/com/example/security/SecurityConfig.java:9` (`ev:src/main/java/com/example/security/SecurityConfig.java:9-9:com.example.security.SecurityConfig#appSecurity:@Bean`)
- Warnings: referenced 2 inspection hint/change-risk warnings.
  - Warning `spring_security`: inspection hint `security_configuration_annotation` (warning_id: `warning:spring_security:security_configuration_annotation:com.example.security.SecurityConfig:annotation:enable_web_security:decl:000001`) at `src/main/java/com/example/security/SecurityConfig.java`. Spring Security configuration annotation detected as a source-visible inspection hint and change-risk signal; the analyzer does not evaluate security policy, endpoint protection, authentication, authorization, filter-chain order, vulnerability, or correctness.
    - Evidence: `src/main/java/com/example/security/SecurityConfig.java:7` (`ev:src/main/java/com/example/security/SecurityConfig.java:7-7:com.example.security.SecurityConfig:@EnableWebSecurity`)
  - Warning `spring_security`: inspection hint `security_filter_chain_bean` (warning_id: `warning:spring_security:security_filter_chain_bean:com.example.security.SecurityConfig#appSecurity:decl:000001`) at `src/main/java/com/example/security/SecurityConfig.java`. SecurityFilterChain @Bean method detected as a source-visible Spring Security configuration inspection hint and change-risk signal; the analyzer does not evaluate security policy, endpoint protection, authentication, authorization, filter-chain order, vulnerability, or correctness.
    - Evidence: `src/main/java/com/example/security/SecurityConfig.java:9` (`ev:src/main/java/com/example/security/SecurityConfig.java:9-9:com.example.security.SecurityConfig#appSecurity:@Bean`), `src/main/java/com/example/security/SecurityConfig.java:10` (`ev:src/main/java/com/example/security/SecurityConfig.java:10-10:com.example.security.SecurityConfig#appSecurity:return:SecurityFilterChain`)

## Detected Spring MVC Endpoints

### `GET /orders/{id}`

- Module: Detected `module:.` (path: `.`)
- Controller: Detected `com.example.web.OrderController`
- Handler: Detected `getOrder`
- Mapping source: Detected `direct_handler_method` from `com.example.web.OrderController#getOrder` with binding `direct`
- HTTP methods: Detected `GET`
- Paths: Detected `/orders/{id}`
- Request parameters: Detected none.
- Request body: Detected none.
- Response: Detected `String`
  - Evidence: `src/main/java/com/example/web/OrderController.java:6` (`ev:src/main/java/com/example/web/OrderController.java:6-6:com.example.web.OrderController:@RestController`), `src/main/java/com/example/web/OrderController.java:8` (`ev:src/main/java/com/example/web/OrderController.java:8-8:com.example.web.OrderController#getOrder:@GetMapping`)

## Detected Spring Components

- Analysis status: `analyzed`

### `com.example.config.AppConfig`

- Module: Detected `module:.` (path: `.`)
- Stereotypes: Detected `@Configuration`
  - Evidence: `src/main/java/com/example/config/AppConfig.java:6` (`ev:src/main/java/com/example/config/AppConfig.java:6-6:com.example.config.AppConfig:@Configuration`)

### `com.example.service.OrderService`

- Module: Detected `module:.` (path: `.`)
- Stereotypes: Detected `@Service`
  - Evidence: `src/main/java/com/example/service/OrderService.java:6` (`ev:src/main/java/com/example/service/OrderService.java:6-6:com.example.service.OrderService:@Service`)

### `com.example.web.OrderController`

- Module: Detected `module:.` (path: `.`)
- Stereotypes: Detected `@RestController`
  - Evidence: `src/main/java/com/example/web/OrderController.java:6` (`ev:src/main/java/com/example/web/OrderController.java:6-6:com.example.web.OrderController:@RestController`)

## Detected JPA Entities

- Analysis status: `analyzed`
- Domain/data facts are source-visible JPA annotations and Spring Data generic signals only; no database schema, runtime Hibernate metadata, migration interpretation, or provider defaults are claimed.
- Extracted entity, field, identifier, embeddable, and relationship facts stay separate from inferred repository/entity links, uncertain relationship targets, and explicit not-analyzed composite-id/runtime boundaries.

### `com.example.domain.Customer`

- Module: Detected `module:.` (path: `.`)
- Entity: Detected `com.example.domain.Customer`
  - Evidence: `src/main/java/com/example/domain/Order.java:7` (`ev:src/main/java/com/example/domain/Order.java:7-7:com.example.domain.Customer:@Entity`)
- Table: Detected none.
- Field metadata: Detected none.
- Identifier field: Detected `id` (`Long`) declared by `com.example.domain.Customer` with source_kind `declared` identifier_kind `simple_id`
  - Evidence: `src/main/java/com/example/domain/Order.java:9` (`ev:src/main/java/com/example/domain/Order.java:9-9:com.example.domain.Customer:@Id:field:id`)
- Relationships: Detected none.

### `com.example.domain.Order`

- Module: Detected `module:.` (path: `.`)
- Entity: Detected `com.example.domain.Order`
  - Evidence: `src/main/java/com/example/domain/Order.java:13` (`ev:src/main/java/com/example/domain/Order.java:13-13:com.example.domain.Order:@Entity`)
- Table: Detected none.
- Field metadata: Detected none.
- Identifier field: Detected `id` (`Long`) declared by `com.example.domain.Order` with source_kind `declared` identifier_kind `simple_id`
  - Evidence: `src/main/java/com/example/domain/Order.java:15` (`ev:src/main/java/com/example/domain/Order.java:15-15:com.example.domain.Order:@Id:field:id`)
- Relationship: Uncertain target for `customer` `@ManyToOne` cardinality `many_to_one` declared type `Customer`
  - target_resolution: `declared_type_only`
  - uncertainty: `target_type_not_resolved`
  - Relationship attributes: Source-visible `ownership_signal=mapped_by_absent`
  - Evidence: `src/main/java/com/example/domain/Order.java:18` (`ev:src/main/java/com/example/domain/Order.java:18-18:com.example.domain.Order:@ManyToOne:field:customer`)

### Embeddables

- Analysis status: `analyzed`
- Detected: no direct `@Embeddable` classes recorded.

## Detected Tests

- Analysis status: `analyzed`

### `com.example.alpha.DuplicateServiceTest`

- Module: Detected `module:.` (path: `.`)
- Test class: Detected `com.example.alpha.DuplicateServiceTest`
  - Evidence: `src/test/java/com/example/alpha/DuplicateServiceTest.java:3` (`ev:src/test/java/com/example/alpha/DuplicateServiceTest.java:3-3:com.example.alpha.DuplicateServiceTest:test_file`)
- Source: Detected `src/test/java/com/example/alpha/DuplicateServiceTest.java`
- Framework signals: Detected none.
- Test methods: Detected none with supported direct JUnit test annotations.
- Ambiguous tested subject candidate: `com.example.alpha.DuplicateService` in target module `module:.` (path: `.`) (relation_status: `ambiguous`, relation_type: `naming_convention`, support_type: `inferred`, confidence: `low`, uncertainty: `ambiguous_subject_name`); no tested-subject coverage or runtime execution relation is claimed.
  - Evidence: `src/test/java/com/example/alpha/DuplicateServiceTest.java:3` (`ev:src/test/java/com/example/alpha/DuplicateServiceTest.java:3-3:com.example.alpha.DuplicateServiceTest:test_file`), `src/main/java/com/example/alpha/DuplicateService.java:3` (`ev:src/main/java/com/example/alpha/DuplicateService.java:3-3:com.example.alpha.DuplicateService:code_symbol`)
- Ambiguous tested subject candidate: `com.example.beta.DuplicateService` in target module `module:.` (path: `.`) (relation_status: `ambiguous`, relation_type: `naming_convention`, support_type: `inferred`, confidence: `low`, uncertainty: `ambiguous_subject_name`); no tested-subject coverage or runtime execution relation is claimed.
  - Evidence: `src/test/java/com/example/alpha/DuplicateServiceTest.java:3` (`ev:src/test/java/com/example/alpha/DuplicateServiceTest.java:3-3:com.example.alpha.DuplicateServiceTest:test_file`), `src/main/java/com/example/beta/DuplicateService.java:3` (`ev:src/main/java/com/example/beta/DuplicateService.java:3-3:com.example.beta.DuplicateService:code_symbol`)

### `com.example.web.MissingControllerTest`

- Module: Detected `module:.` (path: `.`)
- Test class: Detected `com.example.web.MissingControllerTest`
  - Evidence: `src/test/java/com/example/web/MissingControllerTest.java:3` (`ev:src/test/java/com/example/web/MissingControllerTest.java:3-3:com.example.web.MissingControllerTest:test_file`)
- Source: Detected `src/test/java/com/example/web/MissingControllerTest.java`
- Framework signals: Detected none.
- Test methods: Detected none with supported direct JUnit test annotations.
- Tested-subject status: `not_detected` (relation_status: `not_detected`, relation_type: `naming_convention`, support_type: `null`, confidence: `low`, candidate_reference: `MissingController`, uncertainty: `no_matching_production_class`); no tested-subject coverage or runtime execution relation is claimed.
  - Evidence: `src/test/java/com/example/web/MissingControllerTest.java:3` (`ev:src/test/java/com/example/web/MissingControllerTest.java:3-3:com.example.web.MissingControllerTest:test_file`)

### `com.example.web.OrderControllerTest`

- Module: Detected `module:.` (path: `.`)
- Test class: Detected `com.example.web.OrderControllerTest`
  - Evidence: `src/test/java/com/example/web/OrderControllerTest.java:9` (`ev:src/test/java/com/example/web/OrderControllerTest.java:9-9:com.example.web.OrderControllerTest:test_file`)
- Source: Detected `src/test/java/com/example/web/OrderControllerTest.java`
- Framework signal: Detected `JUnit Jupiter` (signal_kind: `framework`)
  - Evidence: `src/test/java/com/example/web/OrderControllerTest.java:13` (`ev:src/test/java/com/example/web/OrderControllerTest.java:13-13:com.example.web.OrderControllerTest#returnsOrder:@Test`), `src/test/java/com/example/web/OrderControllerTest.java:4` (`ev:src/test/java/com/example/web/OrderControllerTest.java:4-4:com.example.web.OrderControllerTest:import:org.junit.jupiter.api.Test`)
- Framework signal: Detected `Spring Test` (signal_kind: `framework`)
  - Evidence: `src/test/java/com/example/web/OrderControllerTest.java:5` (`ev:src/test/java/com/example/web/OrderControllerTest.java:5-5:com.example.web.OrderControllerTest:import:org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest`), `src/test/java/com/example/web/OrderControllerTest.java:6` (`ev:src/test/java/com/example/web/OrderControllerTest.java:6-6:com.example.web.OrderControllerTest:import:org.springframework.boot.test.mock.mockito.MockBean`), `src/test/java/com/example/web/OrderControllerTest.java:8` (`ev:src/test/java/com/example/web/OrderControllerTest.java:8-8:com.example.web.OrderControllerTest:@WebMvcTest`)
- Spring test slice signal: Detected `@WebMvcTest` (slice_kind: `web_mvc_test`, signal_kind: `spring_test_slice`)
  - Evidence: `src/test/java/com/example/web/OrderControllerTest.java:8` (`ev:src/test/java/com/example/web/OrderControllerTest.java:8-8:com.example.web.OrderControllerTest:@WebMvcTest`)
- Mock annotation signal: Detected `@MockBean` on `field` `orderService` (mock_signal: `spring_boot_mockbean_annotation`, signal_kind: `mock_annotation`)
  - Evidence: `src/test/java/com/example/web/OrderControllerTest.java:10` (`ev:src/test/java/com/example/web/OrderControllerTest.java:10-10:com.example.web.OrderControllerTest:field:orderService:@MockBean`)
- Test method: Detected `returnsOrder` annotated with `@Test` (method_kind: `test`)
  - Evidence: `src/test/java/com/example/web/OrderControllerTest.java:13` (`ev:src/test/java/com/example/web/OrderControllerTest.java:13-13:com.example.web.OrderControllerTest#returnsOrder:@Test`)
- Inferred tested subject: `com.example.web.OrderController` in target module `module:.` (path: `.`) (relation_status: `inferred`, relation_type: `naming_convention`, support_type: `inferred`, confidence: `medium`).
  - Evidence: `src/test/java/com/example/web/OrderControllerTest.java:9` (`ev:src/test/java/com/example/web/OrderControllerTest.java:9-9:com.example.web.OrderControllerTest:test_file`), `src/main/java/com/example/web/OrderController.java:7` (`ev:src/main/java/com/example/web/OrderController.java:7-7:com.example.web.OrderController:code_symbol`)
- Inferred tested subject: `com.example.web.OrderController` in target module `module:.` (path: `.`) (relation_status: `inferred`, relation_type: `spring_test_slice_class_literal`, support_type: `inferred`, confidence: `medium`).
  - Evidence: `src/test/java/com/example/web/OrderControllerTest.java:8` (`ev:src/test/java/com/example/web/OrderControllerTest.java:8-8:com.example.web.OrderControllerTest:@WebMvcTest`), `src/main/java/com/example/web/OrderController.java:7` (`ev:src/main/java/com/example/web/OrderController.java:7-7:com.example.web.OrderController:code_symbol`)
- Inferred tested subject: `com.example.service.OrderService` in target module `module:.` (path: `.`) (relation_status: `inferred`, relation_type: `test_field_type`, support_type: `inferred`, confidence: `medium`).
  - Evidence: `src/test/java/com/example/web/OrderControllerTest.java:11` (`ev:src/test/java/com/example/web/OrderControllerTest.java:11-11:com.example.web.OrderControllerTest:field:orderService:type:OrderService`), `src/main/java/com/example/service/OrderService.java:7` (`ev:src/main/java/com/example/service/OrderService.java:7-7:com.example.service.OrderService:code_symbol`)
- Inferred tested subject: `com.example.service.OrderService` in target module `module:.` (path: `.`) (relation_status: `inferred`, relation_type: `test_import`, support_type: `inferred`, confidence: `medium`).
  - Evidence: `src/test/java/com/example/web/OrderControllerTest.java:3` (`ev:src/test/java/com/example/web/OrderControllerTest.java:3-3:com.example.web.OrderControllerTest:import:com.example.service.OrderService`), `src/main/java/com/example/service/OrderService.java:7` (`ev:src/main/java/com/example/service/OrderService.java:7-7:com.example.service.OrderService:code_symbol`)

### `com.example.web.UnsupportedFieldTypeTest`

- Module: Detected `module:.` (path: `.`)
- Test class: Detected `com.example.web.UnsupportedFieldTypeTest`
  - Evidence: `src/test/java/com/example/web/UnsupportedFieldTypeTest.java:5` (`ev:src/test/java/com/example/web/UnsupportedFieldTypeTest.java:5-5:com.example.web.UnsupportedFieldTypeTest:test_file`)
- Source: Detected `src/test/java/com/example/web/UnsupportedFieldTypeTest.java`
- Framework signals: Detected none.
- Test methods: Detected none with supported direct JUnit test annotations.
- Tested-subject status: `not_detected` (relation_status: `not_detected`, relation_type: `naming_convention`, support_type: `null`, confidence: `low`, candidate_reference: `UnsupportedFieldType`, uncertainty: `no_matching_production_class`); no tested-subject coverage or runtime execution relation is claimed.
  - Evidence: `src/test/java/com/example/web/UnsupportedFieldTypeTest.java:5` (`ev:src/test/java/com/example/web/UnsupportedFieldTypeTest.java:5-5:com.example.web.UnsupportedFieldTypeTest:test_file`)
- Tested-subject status: `unsupported` (relation_status: `unsupported`, relation_type: `test_field_type`, support_type: `null`, confidence: `low`, candidate_reference: `List<OrderController>`, uncertainty: `unsupported_subject_reference`); no tested-subject coverage or runtime execution relation is claimed.
  - Evidence: `src/test/java/com/example/web/UnsupportedFieldTypeTest.java:6` (`ev:src/test/java/com/example/web/UnsupportedFieldTypeTest.java:6-6:com.example.web.UnsupportedFieldTypeTest:field:controllers:type:List<OrderController>`)

## Quality And Change-Risk Signals

- Quality analysis status: `analyzed`
- Test-gap signals are absence-sensitive planning hints from the bounded test inventory and inferred tested-subject relations. They do not prove coverage gaps, execution behavior, assertion behavior, CI status, or complete subject mapping.
- Change-risk signals are warning-oriented or uncertain planning hints from existing deterministic facts. They do not prove production impact, vulnerability, business priority, correctness, runtime behavior, or test priority.

### Test-Gap Signals

- Analysis status: `analyzed`
- Test-gap signal: `entity_without_obvious_test` for `jpa_entity` `com.example.domain.Customer` (status: `no_obvious_test`, inference_basis: `no_inferred_tested_subject_relation_for_subject_class`, confidence: `low`, uncertainty: `bounded_test_inventory_supported_relations_only`). No coverage, execution, assertion, CI, or runtime relation is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `entity:com.example.domain.Customer`
  - Subject source hint: class `com.example.domain.Customer`, member `not recorded`
  - Evidence: `src/main/java/com/example/domain/Order.java:7` (`ev:src/main/java/com/example/domain/Order.java:7-7:com.example.domain.Customer:@Entity`)
- Test-gap signal: `entity_without_obvious_test` for `jpa_entity` `com.example.domain.Order` (status: `no_obvious_test`, inference_basis: `no_inferred_tested_subject_relation_for_subject_class`, confidence: `low`, uncertainty: `bounded_test_inventory_supported_relations_only`). No coverage, execution, assertion, CI, or runtime relation is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `entity:com.example.domain.Order`
  - Subject source hint: class `com.example.domain.Order`, member `not recorded`
  - Evidence: `src/main/java/com/example/domain/Order.java:13` (`ev:src/main/java/com/example/domain/Order.java:13-13:com.example.domain.Order:@Entity`)
- Test-gap signal: `repository_without_obvious_test` for `spring_repository` `com.example.repositories.MissingOrderRepository` (status: `no_obvious_test`, inference_basis: `no_inferred_tested_subject_relation_for_subject_class`, confidence: `low`, uncertainty: `bounded_test_inventory_supported_relations_only`). No coverage, execution, assertion, CI, or runtime relation is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `spring_data_repository_interface_signal:module:.:com.example.repositories.MissingOrderRepository`
  - Subject source hint: class `com.example.repositories.MissingOrderRepository`, member `not recorded`
  - Evidence: `src/main/java/com/example/repositories/MissingOrderRepository.java:6-7` (`ev:src/main/java/com/example/repositories/MissingOrderRepository.java:6-7:com.example.repositories.MissingOrderRepository:com.example.repositories.MissingOrderRepository`), `src/main/java/com/example/repositories/MissingOrderRepository.java:6` (`ev:src/main/java/com/example/repositories/MissingOrderRepository.java:6-6:com.example.repositories.MissingOrderRepository:extends:org.springframework.data.jpa.repository.JpaRepository`)

### Change-Risk Signals

- Analysis status: `analyzed`
- Change-risk signal: `jpa_relationship_change_surface` for `jpa_relationship` `com.example.domain.Order#customer` (status: `uncertain_planning_hint`, risk_basis: `source_visible_jpa_relationship_metadata`, confidence: `low`, uncertainty: `relationship_target_declared_type_only`). No production impact, vulnerability, correctness, runtime behavior, or business priority is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `entity:com.example.domain.Order#relationship:customer`
  - Subject source hint: class `com.example.domain.Order`, member `customer`
  - Evidence: `src/main/java/com/example/domain/Order.java:18` (`ev:src/main/java/com/example/domain/Order.java:18-18:com.example.domain.Order:@ManyToOne:field:customer`)
- Change-risk signal: `repository_entity_relation_uncertain` for `spring_data_repository` `com.example.repositories.MissingOrderRepository` (status: `uncertain_planning_hint`, risk_basis: `repository_entity_relation_status_not_detected`, confidence: `low`, uncertainty: `bounded_repository_entity_relation_rules_only`). No production impact, vulnerability, correctness, runtime behavior, or business priority is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `spring_data_repository_interface_signal:module:.:com.example.repositories.MissingOrderRepository`
  - Subject source hint: class `com.example.repositories.MissingOrderRepository`, member `not recorded`
  - Evidence: `src/main/java/com/example/repositories/MissingOrderRepository.java:6-7` (`ev:src/main/java/com/example/repositories/MissingOrderRepository.java:6-7:com.example.repositories.MissingOrderRepository:com.example.repositories.MissingOrderRepository`), `src/main/java/com/example/repositories/MissingOrderRepository.java:6` (`ev:src/main/java/com/example/repositories/MissingOrderRepository.java:6-6:com.example.repositories.MissingOrderRepository:extends:org.springframework.data.jpa.repository.JpaRepository`)
- Change-risk signal: `spring_bean_method_change_surface` for `spring_bean_method` `com.example.config.AppConfig#orderBean` (status: `planning_hint`, risk_basis: `source_visible_bean_method`, confidence: `low`, uncertainty: `source_visible_change_surface_only`). No production impact, vulnerability, correctness, runtime behavior, or business priority is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `spring_bean_method:module:.:com.example.config.AppConfig#orderBean:decl:000001`
  - Subject source hint: class `com.example.config.AppConfig`, member `orderBean`
  - Evidence: `src/main/java/com/example/config/AppConfig.java:8` (`ev:src/main/java/com/example/config/AppConfig.java:8-8:com.example.config.AppConfig#orderBean:@Bean`)
- Change-risk signal: `spring_bean_method_change_surface` for `spring_bean_method` `com.example.security.SecurityConfig#appSecurity` (status: `planning_hint`, risk_basis: `source_visible_bean_method`, confidence: `low`, uncertainty: `source_visible_change_surface_only`). No production impact, vulnerability, correctness, runtime behavior, or business priority is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `spring_bean_method:module:.:com.example.security.SecurityConfig#appSecurity:decl:000001`
  - Subject source hint: class `com.example.security.SecurityConfig`, member `appSecurity`
  - Evidence: `src/main/java/com/example/security/SecurityConfig.java:9` (`ev:src/main/java/com/example/security/SecurityConfig.java:9-9:com.example.security.SecurityConfig#appSecurity:@Bean`)
- Change-risk signal: `spring_configuration_change_surface` for `spring_configuration_class` `com.example.config.AppConfig` (status: `planning_hint`, risk_basis: `source_visible_spring_configuration`, confidence: `low`, uncertainty: `source_visible_change_surface_only`). No production impact, vulnerability, correctness, runtime behavior, or business priority is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `spring_configuration_class:module:.:com.example.config.AppConfig`
  - Subject source hint: class `com.example.config.AppConfig`, member `not recorded`
  - Evidence: `src/main/java/com/example/config/AppConfig.java:6` (`ev:src/main/java/com/example/config/AppConfig.java:6-6:com.example.config.AppConfig:@Configuration`)
- Change-risk signal: `spring_security_warning_change_surface` for `spring_security_warning` `warning:spring_security:security_configuration_annotation:com.example.security.SecurityConfig:annotation:enable_web_security:decl:000001` (status: `warning_oriented_planning_hint`, risk_basis: `source_visible_spring_security_warning`, confidence: `low`, uncertainty: `warning_signal_only_not_vulnerability_or_correctness`). No production impact, vulnerability, correctness, runtime behavior, or business priority is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `warning:spring_security:security_configuration_annotation:com.example.security.SecurityConfig:annotation:enable_web_security:decl:000001`
  - Evidence: `src/main/java/com/example/security/SecurityConfig.java:7` (`ev:src/main/java/com/example/security/SecurityConfig.java:7-7:com.example.security.SecurityConfig:@EnableWebSecurity`)
- Change-risk signal: `spring_security_warning_change_surface` for `spring_security_warning` `warning:spring_security:security_filter_chain_bean:com.example.security.SecurityConfig#appSecurity:decl:000001` (status: `warning_oriented_planning_hint`, risk_basis: `source_visible_spring_security_warning`, confidence: `low`, uncertainty: `warning_signal_only_not_vulnerability_or_correctness`). No production impact, vulnerability, correctness, runtime behavior, or business priority is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `warning:spring_security:security_filter_chain_bean:com.example.security.SecurityConfig#appSecurity:decl:000001`
  - Evidence: `src/main/java/com/example/security/SecurityConfig.java:9` (`ev:src/main/java/com/example/security/SecurityConfig.java:9-9:com.example.security.SecurityConfig#appSecurity:@Bean`), `src/main/java/com/example/security/SecurityConfig.java:10` (`ev:src/main/java/com/example/security/SecurityConfig.java:10-10:com.example.security.SecurityConfig#appSecurity:return:SecurityFilterChain`)
- Change-risk signal: `spring_service_change_surface` for `spring_service` `com.example.service.OrderService` (status: `planning_hint`, risk_basis: `source_visible_service_stereotype`, confidence: `low`, uncertainty: `source_visible_change_surface_only`). No production impact, vulnerability, correctness, runtime behavior, or business priority is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `component:com.example.service.OrderService`
  - Subject source hint: class `com.example.service.OrderService`, member `not recorded`
  - Evidence: `src/main/java/com/example/service/OrderService.java:6` (`ev:src/main/java/com/example/service/OrderService.java:6-6:com.example.service.OrderService:@Service`)
- Change-risk signal: `transaction_boundary_change_surface` for `spring_transaction_boundary` `com.example.service.OrderService#settleOrder` (status: `planning_hint`, risk_basis: `source_visible_transaction_boundary`, confidence: `low`, uncertainty: `source_visible_change_surface_only`). No production impact, vulnerability, correctness, runtime behavior, or business priority is claimed.
  - Module: `module:.` (path: `.`)
  - Subject ID: `spring_transaction_boundary:module:.:com.example.service.OrderService#settleOrder:decl:000001`
  - Subject source hint: class `com.example.service.OrderService`, member `settleOrder`
  - Evidence: `src/main/java/com/example/service/OrderService.java:8` (`ev:src/main/java/com/example/service/OrderService.java:8-8:com.example.service.OrderService#settleOrder:@Transactional`)


## Known Uncertainty And Limits

- Warning: `spring_security` signal `security_configuration_annotation` for module `module:.` (path: `.`) at `src/main/java/com/example/security/SecurityConfig.java`. Spring Security configuration annotation detected as a source-visible inspection hint and change-risk signal; the analyzer does not evaluate security policy, endpoint protection, authentication, authorization, filter-chain order, vulnerability, or correctness.
  - Evidence: `src/main/java/com/example/security/SecurityConfig.java:7` (`ev:src/main/java/com/example/security/SecurityConfig.java:7-7:com.example.security.SecurityConfig:@EnableWebSecurity`)
- Warning: `spring_security` signal `security_filter_chain_bean` for module `module:.` (path: `.`) at `src/main/java/com/example/security/SecurityConfig.java`. SecurityFilterChain @Bean method detected as a source-visible Spring Security configuration inspection hint and change-risk signal; the analyzer does not evaluate security policy, endpoint protection, authentication, authorization, filter-chain order, vulnerability, or correctness.
  - Evidence: `src/main/java/com/example/security/SecurityConfig.java:9` (`ev:src/main/java/com/example/security/SecurityConfig.java:9-9:com.example.security.SecurityConfig#appSecurity:@Bean`), `src/main/java/com/example/security/SecurityConfig.java:10` (`ev:src/main/java/com/example/security/SecurityConfig.java:10-10:com.example.security.SecurityConfig#appSecurity:return:SecurityFilterChain`)
- Not analyzed: Spring runtime behavior such as component scanning, dependency injection graphs, bean lifecycle, scopes, and conditional configuration is not represented by `components.items`.
- Uncertain: JPA relationship targets preserve `target_resolution: declared_type_only` and `uncertainty: target_type_not_resolved`; no symbol solving or ORM runtime behavior is claimed.
- Source-visible: JPA relationship metadata such as `mappedBy`, `@JoinColumn`, `@JoinTable`, `optional`, `fetch`, `cascade`, and `orphanRemoval` is reported only when direct annotation attributes are supported; foreign keys, join tables, ownership correctness, fetch behavior, cascade behavior, and database constraints are not claimed.
- Not analyzed: JPA mapped-superclass identifier support is limited to conservative source-visible mapped-superclass chains; unresolved, ambiguous, cyclic, or non-source-visible branches are skipped.
- Partial: JPA embedded and composite identifier support is limited to direct source-visible `@Embeddable`, `@Embedded`, `@EmbeddedId`, and `@IdClass` signals. Embedded targets are linked only when a unique local `@Embeddable` can be matched; `@IdClass` field matching and composite-key semantics are not analyzed.
- Inferred/statused: tested-subject rows are conservative source-visible hints from supported naming, import, field-type, and Spring test slice class-literal signals. Non-inferred statuses such as `not_detected`, `ambiguous`, and `unsupported` do not claim coverage or execution. Test method inventory records source-visible JUnit annotation structure only. Test execution, CI results, coverage, assertion behavior, call graphs, and complete subject mapping are not analyzed.
- Planning hints: quality test-gap and change-risk signals are conservative derived hints from existing deterministic facts and inferred tested-subject relations. They do not claim coverage, test execution, assertion behavior, runtime behavior, production impact, vulnerability, correctness, business priority, or complete subject mapping.
- Source-visible: Spring test slice signals and mock annotation signals record direct annotation structure only. Runtime Spring context behavior, bean graph contents, MockMvc setup, database access, Mockito behavior, and slice correctness are not claimed.
- Not analyzed: connectors, LLM summaries, repository chat, generic RAG, Gradle/Kotlin support, Maven profiles, effective POM reconstruction, dependency graphs, and recursive nested Maven modules are outside this guide.
- Not analyzed: generated sources, generated API reconstruction, classpath-only interfaces, and ambiguous interface endpoint bindings are outside the source-visible interface endpoint support.
- Not analyzed: OpenAPI operation facts are spec-backed declared operations only; runtime implementation matching, source/spec agreement, generated source contents, and client SDK reconstruction are not claimed.
- Not analyzed: v0.3 build/config facts are direct local source observations only. Maven execution, effective POM reconstruction, profile activation, remote dependency resolution, config value interpretation, secret extraction, and default generated-source scanning are not performed.
- Not analyzed: Spring Boot application signals do not prove executable packaging, active profiles, runtime auto-configuration, bean graphs, component scanning results, deployment behavior, or actual process entrypoint behavior.
- Not analyzed: Spring Data repository interface signals do not prove runtime repository registration, query method behavior, database access, or runtime repository/entity verification. Repository/entity links, when present, are bounded inferred Spring Data generic relations with explicit `entity_relation_status` values.
- Not analyzed: JPA field metadata is limited to supported direct field-level source-visible annotations. It is not a complete persistent-property inventory, does not support getter/property access in this slice, and does not fill missing annotation attributes from JPA provider defaults.
- Not analyzed: v0.5 transaction, scheduling, event listener, and messaging listener facts are annotation-presence change-surface signals only. Transaction propagation, scheduler registration, event delivery, message destinations, broker topology, consumer groups, and delivery semantics are not claimed.
- Not analyzed: Security policy, endpoint protection state, authentication behavior, authorization behavior, filter-chain ordering, vulnerabilities, and correctness are not claimed. v0.5 Spring Security configuration warnings are bounded source-visible inspection hints only.

## Practical Inspection Order For Coding Agents

1. Start with detected build, module, and layout facts in `pom.xml`.
2. For HTTP behavior, inspect detected endpoint and hidden-surface warning evidence in `src/main/java/com/example/web/OrderController.java`.
3. For Spring application surface changes, inspect Spring application surface and component evidence in `src/main/java/com/example/repositories/MissingOrderRepository.java`, `src/main/java/com/example/config/AppConfig.java`, `src/main/java/com/example/security/SecurityConfig.java`, `src/main/java/com/example/service/OrderService.java`, `src/main/java/com/example/web/OrderController.java` and avoid assuming runtime repository registration, entity ownership, injection graphs, transaction behavior, scheduler registration, event delivery, or messaging topology.
4. For persistence changes, inspect detected entity evidence in `src/main/java/com/example/domain/Order.java` and treat field metadata as source-visible annotations only, not runtime schema, provider defaults, or complete access-strategy reconstruction; relationship targets remain declared-type-only.
5. For tests, inspect detected test files and tested-subject relation/status evidence in `src/test/java/com/example/alpha/DuplicateServiceTest.java`, `src/main/java/com/example/alpha/DuplicateService.java`, `src/main/java/com/example/beta/DuplicateService.java`, `src/test/java/com/example/web/MissingControllerTest.java`, `src/test/java/com/example/web/OrderControllerTest.java`, ... and 3 more evidence paths in `evidence-index.jsonl`; do not treat inferred or statused subjects as coverage proof, and do not treat Spring test slice or mock annotations as execution or runtime behavior proof.
6. For quality and change-risk planning, inspect quality signal evidence in `src/main/java/com/example/domain/Order.java`, `src/main/java/com/example/repositories/MissingOrderRepository.java`, `src/main/java/com/example/config/AppConfig.java`, `src/main/java/com/example/security/SecurityConfig.java`, `src/main/java/com/example/service/OrderService.java` and treat `no_obvious_test`, warning-oriented, and uncertain statuses as planning hints only, not coverage, runtime, correctness, vulnerability, or business-priority claims.
