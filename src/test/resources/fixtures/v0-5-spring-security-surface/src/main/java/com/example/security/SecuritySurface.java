package com.example.security;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@EnableMethodSecurity
class SecuritySurface {
  @Bean
  SecurityFilterChain applicationSecurity() {
    return null;
  }

  @org.springframework.context.annotation.Bean
  org.springframework.security.web.SecurityFilterChain managementSecurity() {
    return null;
  }
}
