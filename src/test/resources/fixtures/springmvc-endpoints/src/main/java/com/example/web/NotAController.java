package com.example.web;

import org.springframework.web.bind.annotation.GetMapping;

class NotAController {
  @GetMapping("/ignored")
  String ignored() {
    return "ignored";
  }
}
