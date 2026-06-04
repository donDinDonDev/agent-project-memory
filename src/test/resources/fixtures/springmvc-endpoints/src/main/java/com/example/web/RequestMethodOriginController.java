package com.example.web;

import static com.example.web.RequestMethodOriginController.LocalRequestMethod.POST;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class RequestMethodOriginController {
  @RequestMapping(path = "/local-request-method", method = LocalRequestMethod.GET)
  String localRequestMethod() {
    return "local";
  }

  @RequestMapping(path = "/static-request-method", method = POST)
  String staticImportedRequestMethod() {
    return "static";
  }

  enum LocalRequestMethod {
    GET,
    POST
  }
}
