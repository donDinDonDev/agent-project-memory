package com.example.wildcard;

import org.springframework.web.bind.annotation.*;

@RestController
class WildcardAnnotationController {
  @GetMapping("/wildcard-annotation")
  String wildcardAnnotation() {
    return "ignored";
  }
}
