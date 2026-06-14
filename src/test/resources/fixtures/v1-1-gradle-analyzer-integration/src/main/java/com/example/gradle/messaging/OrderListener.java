package com.example.gradle.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderListener {

  @KafkaListener(topics = "orders")
  void handleOrderMessage(String payload) {}
}
