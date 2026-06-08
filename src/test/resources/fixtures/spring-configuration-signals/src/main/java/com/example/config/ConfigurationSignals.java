package com.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class InventoryConfiguration {
  @Bean
  InventoryClient inventoryClient() {
    return new InventoryClient();
  }

  @org.springframework.context.annotation.Bean
  InventoryHelper inventoryHelper() {
    return new InventoryHelper();
  }
}

@ConfigurationProperties(prefix = "inventory")
class InventoryProperties {
}

@org.springframework.boot.context.properties.ConfigurationProperties("orders")
class FullyQualifiedOrderProperties {
}

class BeanOnlyFactory {
  @Bean
  String standaloneBean() {
    return "standalone";
  }
}

class InventoryClient {
}

class InventoryHelper {
}

@ConfigurationProperties(prefix = "records")
record RecordBackedProperties(String host) {
}
