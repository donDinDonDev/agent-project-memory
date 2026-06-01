package com.example.api;

import org.springframework.web.bind.annotation.GetMapping;

public interface ImportedOrdersApi {
  @GetMapping("/imported/orders")
  String importedOrder();
}
