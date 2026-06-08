package com.example.behavior;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RabbitListener(queues = "billing-type")
class BehaviorMessagingSurface {
  @Transactional
  void settleInvoice() {
  }

  @Scheduled(fixedDelayString = "${billing.delay}")
  void refreshInvoices() {
  }

  @EventListener
  void onInvoicePaid(InvoicePaid event) {
  }

  @KafkaListener(topics = "billing-events", groupId = "billing-workers")
  void onKafkaEvent(String payload) {
  }

  @RabbitListener(queues = "billing.retry")
  void onRabbitRetry(String payload) {
  }
}

record InvoicePaid(String id) {
}
