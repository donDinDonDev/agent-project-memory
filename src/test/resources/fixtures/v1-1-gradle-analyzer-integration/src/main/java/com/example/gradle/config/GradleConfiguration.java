package com.example.gradle.config;

import java.time.Clock;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
public class GradleConfiguration {

  @Bean
  Clock orderClock() {
    return Clock.systemUTC();
  }

  @Scheduled(fixedDelay = 30000)
  void refreshOrderCache() {}

  @ConfigurationProperties(prefix = "orders")
  public record OrderProperties(String region) {}
}
