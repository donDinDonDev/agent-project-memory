package com.example.behavior;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@KafkaListener(topics = "orders")
@RabbitListener(queues = "orders")
class SpoofedBehaviorSignals {
  @Transactional
  void transactional() {
  }

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
