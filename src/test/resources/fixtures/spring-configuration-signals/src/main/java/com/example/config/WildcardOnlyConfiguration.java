package com.example.config;

import org.springframework.boot.context.properties.*;
import org.springframework.context.annotation.*;

@Configuration
class WildcardOnlyConfiguration {
  @Bean
  String wildcardOnlyBean() {
    return "ignored";
  }
}

@ConfigurationProperties(prefix = "wildcard")
class WildcardOnlyProperties {
}
