package com.example.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class PathVariantsController {
  static final String CONSTANT_PATH = "/constant";

  @GetMapping(CONSTANT_PATH)
  String constantPath() {
    return "constant";
  }

  @GetMapping({"/alpha", "/beta"})
  String multipleMethodPaths() {
    return "multiple";
  }
}

@RestController
@RequestMapping({"/api", "/internal"})
class MultipleClassLevelPathsController {
  @GetMapping({"/orders", "/purchases"})
  String orders() {
    return "orders";
  }
}

@RestController
@RequestMapping("/")
class RootClassLevelMappingController {
  @GetMapping("/orders")
  String orders() {
    return "orders";
  }
}

@RestController
@RequestMapping(MappingConstants.API_PREFIX)
class NonLiteralClassLevelPathController {
  @GetMapping("/orders")
  String orders() {
    return "orders";
  }
}

final class MappingConstants {
  static final String API_PREFIX = "/api";

  private MappingConstants() {
  }
}
