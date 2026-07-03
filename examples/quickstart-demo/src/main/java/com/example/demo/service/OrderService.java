package com.example.demo.service;

import com.example.demo.repository.OrderRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
  private final OrderRecordRepository orders;

  public OrderService(OrderRecordRepository orders) {
    this.orders = orders;
  }

  @Transactional(readOnly = true)
  public OrderSummary findOrder(Long id) {
    return new OrderSummary(id, "Example Customer", "READY");
  }

  @Transactional
  public OrderSummary createOrder(CreateOrderRequest request) {
    orders.count();
    return new OrderSummary(1L, request.customerName(), "NEW");
  }
}
