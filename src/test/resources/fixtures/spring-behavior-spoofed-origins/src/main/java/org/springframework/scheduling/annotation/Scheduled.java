package org.springframework.scheduling.annotation;

public @interface Scheduled {
  String cron() default "";
}
