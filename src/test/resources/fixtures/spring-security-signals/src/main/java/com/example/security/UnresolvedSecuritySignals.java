package com.example.security;

@EnableWebSecurity
class UnresolvedSecuritySignals {
  @Bean
  SecurityFilterChain unresolvedSecurity() {
    return null;
  }
}
