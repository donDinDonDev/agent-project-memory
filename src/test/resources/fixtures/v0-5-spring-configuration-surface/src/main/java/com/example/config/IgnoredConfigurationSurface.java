package com.example.config;

@Configuration
class UnresolvedConfiguration {
}

@ConfigurationProperties(prefix = "ignored")
class UnresolvedProperties {
}

class UnresolvedFactory {
  @Bean
  String unresolvedBean() {
    return "ignored";
  }
}
