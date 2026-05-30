package com.example.web;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;

class NestedAndHelperTests {
  @Test
  void topLevelTest() {
  }

  @Nested
  class ValidationCases {
    @Test
    void rejectsInvalidInput() {
    }
  }

  @TestConfiguration
  static class NestedTestConfiguration {
  }
}
