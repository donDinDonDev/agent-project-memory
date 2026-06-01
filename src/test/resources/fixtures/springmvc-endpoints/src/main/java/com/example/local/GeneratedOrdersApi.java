package com.example.local;

import org.springframework.web.bind.annotation.GetMapping;

interface GeneratedOrdersApi {
  @GetMapping("/wrong-generated-fallback")
  String generatedOperation();
}
