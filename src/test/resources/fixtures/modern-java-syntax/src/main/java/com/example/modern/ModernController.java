package com.example.modern;

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
