package com.example.behavior;

import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.context.event.*;
import org.springframework.kafka.annotation.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.transaction.annotation.*;

@Transactional
class WildcardOnlyBehaviorSignals {
  @Scheduled(cron = "0 * * * * *")
  void scheduled() {
  }

  @EventListener
  void event() {
  }

  @KafkaListener(topics = "orders")
  void kafka() {
  }

  @RabbitListener(queues = "orders")
  void rabbit() {
  }
}
