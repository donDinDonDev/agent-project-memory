package com.example.wildcard;

import org.springframework.web.bind.annotation.GetMapping;

public interface WildcardOrdersApi {
  @GetMapping("/wildcard/orders")
  String wildcardOrder();
}
