package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class AppConfig {
  @Bean
  Object orderBean() {
    return new Object();
  }
}
