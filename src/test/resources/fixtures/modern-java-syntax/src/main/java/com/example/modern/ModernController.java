package com.example.modern;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class ModernController {
  @GetMapping("/modern")
  String modern(Object value) {
    if (value instanceof String text) {
      return text.strip();
    }
    return "";
  }
}
