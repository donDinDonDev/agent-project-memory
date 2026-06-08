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
  InventoryClock inventoryClock() {
    return new InventoryClock();
  }
}

@ConfigurationProperties(prefix = "inventory")
class InventoryProperties {
}

class SecondaryBeanFactory {
  @Bean
  String secondaryBean() {
    return "secondary";
  }
}

class InventoryClient {
}

class InventoryClock {
}

@ConfigurationProperties(prefix = "catalog")
record CatalogProperties(String region) {
}
