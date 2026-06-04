package com.example.web;

@org.springframework.web.bind.annotation.RestController
class SourceDeclaredFqcnController {
  @org.springframework.web.bind.annotation.GetMapping("/spoofed")
  String spoofed() {
    return "spoofed";
  }
}
