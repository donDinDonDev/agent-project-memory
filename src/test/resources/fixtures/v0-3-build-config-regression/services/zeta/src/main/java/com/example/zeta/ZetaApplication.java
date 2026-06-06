package com.example.zeta;

@org.springframework.boot.autoconfigure.SpringBootApplication
class ZetaApplication {
}

@org.springframework.web.bind.annotation.RestController
@org.springframework.web.bind.annotation.RequestMapping("/zeta")
class ZetaController {
  @org.springframework.web.bind.annotation.PostMapping("/items")
  ZetaItem create(@org.springframework.web.bind.annotation.RequestBody ZetaItem item) {
    return item;
  }
}

record ZetaItem(String id) {
}
