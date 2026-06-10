package com.example.security;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
class SecurityConfig {
  @Bean
  SecurityFilterChain appSecurity() {
    return null;
  }
}
