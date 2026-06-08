package com.example.security;

import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.web.*;

@EnableWebSecurity
class WildcardOnlySecuritySignals {
  @Bean
  SecurityFilterChain wildcardOnlySecurity() {
    return null;
  }
}
