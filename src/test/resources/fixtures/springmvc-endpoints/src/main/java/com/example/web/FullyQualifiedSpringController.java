package com.example.web;

@org.springframework.web.bind.annotation.RestController
class FullyQualifiedSpringController {
  @org.springframework.web.bind.annotation.GetMapping("/fully-qualified")
  String fullyQualified() {
    return "ok";
  }
}
