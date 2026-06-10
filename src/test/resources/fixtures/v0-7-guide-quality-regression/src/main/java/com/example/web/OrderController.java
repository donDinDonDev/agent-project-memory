package com.example.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class OrderController {
  @GetMapping("/orders/{id}")
  String getOrder() {
    return "ok";
  }
}
