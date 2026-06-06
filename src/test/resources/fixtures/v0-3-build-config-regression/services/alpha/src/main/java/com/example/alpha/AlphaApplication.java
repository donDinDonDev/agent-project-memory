package com.example.alpha;

@org.springframework.boot.autoconfigure.SpringBootApplication
class AlphaApplication {
  public static void main(String[] args) {
  }
}

@org.springframework.web.bind.annotation.RestController
@org.springframework.web.bind.annotation.RequestMapping("/alpha")
class AlphaController {
  @org.springframework.web.bind.annotation.GetMapping("/status")
  AlphaStatus status(@org.springframework.web.bind.annotation.RequestParam("detail") String detail) {
    return new AlphaStatus(detail);
  }
}

record AlphaStatus(String value) {
}
