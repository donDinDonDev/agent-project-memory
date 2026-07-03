package com.example.demo.web;

import com.example.demo.service.CreateOrderRequest;
import com.example.demo.service.OrderService;
import com.example.demo.service.OrderSummary;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
  private final OrderService service;

  public OrderController(OrderService service) {
    this.service = service;
  }

  @GetMapping("/{id}")
  public OrderSummary getOrder(@PathVariable Long id) {
    return service.findOrder(id);
  }

  @PostMapping
  public OrderSummary createOrder(@RequestBody CreateOrderRequest request) {
    return service.createOrder(request);
  }
}
