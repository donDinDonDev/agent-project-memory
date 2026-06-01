package com.example.other;

import org.springframework.web.bind.annotation.GetMapping;

public interface CrossPackageOrdersApi {
  @GetMapping("/cross-package/orders")
  String crossPackageOrder();
}
