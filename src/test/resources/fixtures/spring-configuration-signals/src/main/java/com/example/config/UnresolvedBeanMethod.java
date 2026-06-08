package com.example.config;

class UnresolvedBeanMethod {
  @Bean
  String unresolvedBean() {
    return "ignored";
  }
}
