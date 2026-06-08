package com.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class SourceDeclaredConfiguration {
  @Bean
  String sourceDeclaredBean() {
    return "ignored";
  }
}

@ConfigurationProperties(prefix = "spoofed")
class SourceDeclaredProperties {
}
