package com.example.fake;

@interface RestController {
}

@interface GetMapping {
  String value() default "";
}

@RestController
class FakeSpringAnnotationsController {
  @GetMapping("/fake")
  String fake() {
    return "fake";
  }
}
