package com.example.modern;

import org.junit.jupiter.api.Test;

class ModernControllerTest {
  @Test
  void acceptsInstanceofPattern() {
    Object value = "ok";
    if (value instanceof String text) {
      text.length();
    }
  }
}
