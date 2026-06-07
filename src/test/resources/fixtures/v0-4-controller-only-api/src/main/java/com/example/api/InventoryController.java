package com.example.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class InventoryController {
  @PostMapping("/inventory")
  InventoryDto createInventory(@RequestBody CreateInventoryRequest request) {
    return new InventoryDto();
  }

  @GetMapping("/inventory/{id}")
  InventoryDto getInventory(@RequestParam(name = "includeDetails") String includeDetails) {
    return new InventoryDto();
  }

  static class CreateInventoryRequest {
  }

  static class InventoryDto {
  }
}
