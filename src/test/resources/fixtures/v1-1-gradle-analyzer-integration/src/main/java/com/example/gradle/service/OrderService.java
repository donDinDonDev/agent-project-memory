package com.example.gradle.service;

import com.example.gradle.web.OrderDto;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

  @Transactional(readOnly = true)
  public OrderDto load(Long id) {
    return new OrderDto(id, "ORD-" + id);
  }

  @EventListener
  void onOrderEvent(OrderImportedEvent event) {}

  public record OrderImportedEvent(Long orderId) {}
}
