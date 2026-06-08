package com.example.behavior;

import org.springframework.kafka.annotation.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.transaction.annotation.*;

@Transactional
class IgnoredBehaviorMessagingSurface {
  @Scheduled(fixedDelayString = "${ignored.delay}")
  void refresh() {
  }

  @KafkaListener(topics = "ignored-events")
  void listen(String payload) {
  }
}
