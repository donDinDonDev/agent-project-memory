package com.example.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
class ClassLevelMappingController {
  @GetMapping("/orders")
  ListResponse orders() {
    return new ListResponse();
  }
}

class ListResponse {
}
