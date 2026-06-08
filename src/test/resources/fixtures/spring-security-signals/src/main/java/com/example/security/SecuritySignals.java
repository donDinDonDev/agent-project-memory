package com.example.security;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
class WebSecuritySignals {
  @Bean
  SecurityFilterChain apiSecurity() {
    return null;
  }

  @org.springframework.context.annotation.Bean
  org.springframework.security.web.SecurityFilterChain actuatorSecurity() {
    return null;
  }

  @Bean
  String notSecurityFilterChain() {
    return "not-security";
  }
}

@EnableMethodSecurity
class MethodSecuritySignals {
}

@org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
class WebFluxSecuritySignals {
}

@org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
class ReactiveMethodSecuritySignals {
}

@org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
class LegacyMethodSecuritySignals {
}
