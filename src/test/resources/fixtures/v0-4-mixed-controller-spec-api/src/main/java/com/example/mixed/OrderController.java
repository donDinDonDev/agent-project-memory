package com.example.mixed;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class OrderController {
  @GetMapping("/orders/{id}")
  OrderDto getOrder() {
    return new OrderDto();
  }

  static class OrderDto {
  }
}
