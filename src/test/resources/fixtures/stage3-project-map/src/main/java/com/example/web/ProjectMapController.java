package com.example.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
class ProjectMapController {
  @GetMapping("/items/{id}")
  ItemResponse getItem(
      @PathVariable Long id,
      @RequestParam("expand") String expand) {
    return new ItemResponse();
  }

  @PostMapping("/items")
  ItemResponse createItem(@RequestBody CreateItemRequest request) {
    return new ItemResponse();
  }
}

class CreateItemRequest {
}

class ItemResponse {
}
