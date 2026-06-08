package com.example.behavior;

@Transactional
class UnresolvedBehaviorSignals {
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
