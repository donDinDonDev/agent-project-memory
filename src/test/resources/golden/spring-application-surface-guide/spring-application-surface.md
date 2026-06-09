## Spring Application Surface

- Spring application surface analysis status: `analyzed`
- Repository stereotype entries are direct `@Repository` annotation observations; they do not prove runtime bean registration or entity ownership.
- Spring Data repository interface entries are inferred source-visible extension signals; repository/entity relation rows, when present, are inferred generic links. They do not prove runtime repositories, query method behavior, database access, or runtime repository/entity verification.
- Configuration classes, configuration-properties types, and `@Bean` methods are source-visible Spring configuration signals; they do not prove runtime bean graphs, binding success, config values, bean scopes, lifecycle, proxy behavior, or dependency graphs.
- Transaction, scheduled, event listener, and messaging listener entries are source-visible operational change-surface signals; they do not prove runtime transaction behavior, scheduler registration, event delivery, message destinations, or broker topology.
- Spring Security configuration warnings are inspection hints and change-risk signals; they do not prove security policy, endpoint protection, authentication behavior, authorization behavior, vulnerability, or correctness.
- Subsection statuses: repositories `analyzed`, configuration classes `analyzed`, configuration properties `analyzed`, bean methods `analyzed`, transaction boundaries `analyzed`, scheduled methods `analyzed`, event listeners `analyzed`, messaging listeners `analyzed`, security warnings `analyzed`.

### Module `module:services/orders` (path: `services/orders`)

- Extracted facts: detected 4 source-visible facts.
  - `spring_repository_stereotype`: `com.example.orders.OrderRepositoryAdapter` (support_type: `extracted`, repository_signal: `direct_repository_stereotype`).
    - Source: `services/orders/src/main/java/com/example/orders/OrderRepositoryAdapter.java`
    - Evidence: `services/orders/src/main/java/com/example/orders/OrderRepositoryAdapter.java:8` (`ev:services/orders/src/main/java/com/example/orders/OrderRepositoryAdapter.java:8-8:com.example.orders.OrderRepositoryAdapter:@Repository`)
  - `spring_configuration_class`: `com.example.orders.OrderConfiguration` (support_type: `extracted`, configuration_signal: `direct_configuration_class`).
    - Source: `services/orders/src/main/java/com/example/orders/OrderConfiguration.java`
    - Evidence: `services/orders/src/main/java/com/example/orders/OrderConfiguration.java:5` (`ev:services/orders/src/main/java/com/example/orders/OrderConfiguration.java:5-5:com.example.orders.OrderConfiguration:@Configuration`)
  - `spring_configuration_properties_type`: `com.example.orders.OrderProperties` (support_type: `extracted`, configuration_properties_signal: `direct_configuration_properties_type`).
    - Source: `services/orders/src/main/java/com/example/orders/OrderProperties.java`
    - Evidence: `services/orders/src/main/java/com/example/orders/OrderProperties.java:4` (`ev:services/orders/src/main/java/com/example/orders/OrderProperties.java:4-4:com.example.orders.OrderProperties:@ConfigurationProperties`)
  - `spring_bean_method`: `com.example.orders.OrderConfiguration#orderClock` (support_type: `extracted`, bean_signal: `direct_bean_method`).
    - Source: `services/orders/src/main/java/com/example/orders/OrderConfiguration.java`
    - Evidence: `services/orders/src/main/java/com/example/orders/OrderConfiguration.java:8` (`ev:services/orders/src/main/java/com/example/orders/OrderConfiguration.java:8-8:com.example.orders.OrderConfiguration#orderClock:@Bean`)
- Inferred signals: detected none.
- Uncertain/not-analyzed statuses: detected 2 explicit statuses.
  - `com.example.orders.OrderProperties`: `binding_status` is `not_analyzed`; no runtime binding success or config values are claimed.
    - Evidence: `services/orders/src/main/java/com/example/orders/OrderProperties.java:4` (`ev:services/orders/src/main/java/com/example/orders/OrderProperties.java:4-4:com.example.orders.OrderProperties:@ConfigurationProperties`)
  - `com.example.orders.OrderConfiguration#orderClock`: `bean_name_status` is `not_analyzed`; no effective runtime bean name is claimed.
    - Evidence: `services/orders/src/main/java/com/example/orders/OrderConfiguration.java:8` (`ev:services/orders/src/main/java/com/example/orders/OrderConfiguration.java:8-8:com.example.orders.OrderConfiguration#orderClock:@Bean`)
- Warnings: detected none.

### Module `module:services/billing` (path: `services/billing`)

- Extracted facts: detected 3 source-visible facts.
  - `spring_transaction_boundary`: `com.example.billing.BillingService` (support_type: `extracted`, transaction_signal: `direct_transactional_type`, target_kind: `type`, annotation_symbol: `@Transactional`).
    - Source: `services/billing/src/main/java/com/example/billing/BillingService.java`
    - Evidence: `services/billing/src/main/java/com/example/billing/BillingService.java:9` (`ev:services/billing/src/main/java/com/example/billing/BillingService.java:9-9:com.example.billing.BillingService:@Transactional`)
  - `spring_event_listener`: `com.example.billing.BillingEvents#onPaid` (support_type: `extracted`, event_listener_signal: `direct_event_listener_method`, target_kind: `method`, annotation_symbol: `@EventListener`).
    - Source: `services/billing/src/main/java/com/example/billing/BillingEvents.java`
    - Evidence: `services/billing/src/main/java/com/example/billing/BillingEvents.java:11` (`ev:services/billing/src/main/java/com/example/billing/BillingEvents.java:11-11:com.example.billing.BillingEvents#onPaid:@EventListener`)
  - `messaging_listener_signal`: `com.example.billing.BillingEvents#onKafkaEvent` (support_type: `extracted`, listener_signal: `direct_kafka_listener_annotation`, target_kind: `method`, annotation_symbol: `@KafkaListener`, listener_framework: `kafka`).
    - Source: `services/billing/src/main/java/com/example/billing/BillingEvents.java`
    - Evidence: `services/billing/src/main/java/com/example/billing/BillingEvents.java:15` (`ev:services/billing/src/main/java/com/example/billing/BillingEvents.java:15-15:com.example.billing.BillingEvents#onKafkaEvent:@KafkaListener`)
- Inferred signals: detected 1 source-visible signal.
  - `spring_data_repository_interface_signal`: `com.example.billing.BillingRepository` extends `org.springframework.data.repository.CrudRepository` (support_type: `inferred`, repository_signal: `spring_data_repository_interface_extension`).
    - Source: `services/billing/src/main/java/com/example/billing/BillingRepository.java`
    - Evidence: `services/billing/src/main/java/com/example/billing/BillingRepository.java:6` (`ev:services/billing/src/main/java/com/example/billing/BillingRepository.java:6-6:com.example.billing.BillingRepository:com.example.billing.BillingRepository`), `services/billing/src/main/java/com/example/billing/BillingRepository.java:6` (`ev:services/billing/src/main/java/com/example/billing/BillingRepository.java:6-6:com.example.billing.BillingRepository:extends:org.springframework.data.repository.CrudRepository`)
- Uncertain/not-analyzed statuses: detected 1 explicit status.
  - `com.example.billing.BillingRepository`: `entity_relation_status` is `not_analyzed`; no runtime repository/entity relation is claimed.
    - Evidence: `services/billing/src/main/java/com/example/billing/BillingRepository.java:6` (`ev:services/billing/src/main/java/com/example/billing/BillingRepository.java:6-6:com.example.billing.BillingRepository:com.example.billing.BillingRepository`), `services/billing/src/main/java/com/example/billing/BillingRepository.java:6` (`ev:services/billing/src/main/java/com/example/billing/BillingRepository.java:6-6:com.example.billing.BillingRepository:extends:org.springframework.data.repository.CrudRepository`)
- Warnings: referenced 1 inspection hint/change-risk warning.
  - Warning `spring_security`: inspection hint `security_configuration_annotation` (warning_id: `warning:spring_security:security_configuration_annotation:module:services/billing:com.example.billing.BillingSecurity:annotation:enable_method_security:decl:000001`) at `services/billing/src/main/java/com/example/billing/BillingSecurity.java`. Spring Security configuration annotation detected as a source-visible inspection hint and change-risk signal; the analyzer does not evaluate security policy, endpoint protection, authentication, authorization, filter-chain order, vulnerability, or correctness.
    - Evidence: `services/billing/src/main/java/com/example/billing/BillingSecurity.java:7` (`ev:services/billing/src/main/java/com/example/billing/BillingSecurity.java:7-7:com.example.billing.BillingSecurity:@EnableMethodSecurity`)
