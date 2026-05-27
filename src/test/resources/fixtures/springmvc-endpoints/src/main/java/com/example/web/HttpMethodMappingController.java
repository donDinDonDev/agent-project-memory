package com.example.web;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/coverage")
class HttpMethodMappingController {
  @RequestMapping(path = "/request")
  String requestMappingWithoutMethod() {
    return "request";
  }

  @RequestMapping(method = RequestMethod.GET)
  String requestMappingGet() {
    return "request-get";
  }

  @RequestMapping(path = "/request-both", method = {RequestMethod.GET, RequestMethod.POST})
  String requestMappingMultipleMethods() {
    return "request-both";
  }

  @GetMapping(path = "/get-path")
  String getMappingPathAlias() {
    return "get";
  }

  @PostMapping(value = "/post-value")
  String postMappingValueAlias() {
    return "post";
  }

  @PutMapping(path = "/put-path")
  String putMappingPathAlias() {
    return "put";
  }

  @PatchMapping("/patch")
  String patchMapping() {
    return "patch";
  }

  @DeleteMapping(value = {"/delete", "/remove"})
  String deleteMappingArray() {
    return "delete";
  }

  @RequestMapping(path = MappingConstants.API_PREFIX)
  String nonLiteralRequestMappingPath() {
    return "non-literal";
  }

  @CustomComposedMapping("/custom")
  String customComposedMapping() {
    return "custom";
  }
}

@GetMapping
@interface CustomComposedMapping {
  String value();
}
