package com.example.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class RequestMetadataController {
  @GetMapping("/orders/{id}")
  OrderResponse getOrder(
      @PathVariable Long id,
      @RequestParam(name = "status") String status,
      @RequestParam("page") int page) {
    return new OrderResponse();
  }

  @PostMapping("/orders")
  OrderResponse createOrder(@RequestBody CreateOrderRequest request) {
    return new OrderResponse();
  }

  @GetMapping("/orders/search")
  OrderResponse unsupportedRequestParamName(@RequestParam(ApiFields.STATUS) String status) {
    return new OrderResponse();
  }
}

class CreateOrderRequest {
}

class OrderResponse {
}

final class ApiFields {
  static final String STATUS = "status";

  private ApiFields() {
  }
}
