package com.example.gradle.web;

import com.example.gradle.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

  @MockBean
  private OrderService orderService;

  @Test
  void returnsOrder() {}
}
