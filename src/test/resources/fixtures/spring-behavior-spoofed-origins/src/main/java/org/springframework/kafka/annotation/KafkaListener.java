package org.springframework.kafka.annotation;

public @interface KafkaListener {
  String[] topics() default {};
}
