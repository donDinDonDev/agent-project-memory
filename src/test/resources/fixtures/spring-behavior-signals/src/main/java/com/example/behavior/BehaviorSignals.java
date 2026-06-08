package com.example.behavior;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.RabbitListeners;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.KafkaListeners;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@KafkaListener(topics = "type-orders")
class BehaviorSignals {
  @Transactional
  void saveOrder() {
  }

  @org.springframework.transaction.annotation.Transactional
  void fullyQualifiedTransaction() {
  }

  @Scheduled(cron = "0 * * * * *")
  void refreshProjection() {
  }

  @EventListener
  void onInventoryChanged(InventoryChanged event) {
  }

  @KafkaListener(topics = "orders")
  void onKafkaOrder(String payload) {
  }

  @RabbitListener(queues = "orders")
  void onRabbitOrder(String payload) {
  }

  @KafkaListeners({
      @KafkaListener(topics = "audit")
  })
  void onKafkaBatch(String payload) {
  }

  @RabbitListeners({
      @RabbitListener(queues = "audit")
  })
  void onRabbitBatch(String payload) {
  }
}

@RabbitListener(queues = "type-orders")
class TypeLevelRabbitListener {
  @Transactional
  void handle() {
  }
}

record InventoryChanged(String id) {
}
