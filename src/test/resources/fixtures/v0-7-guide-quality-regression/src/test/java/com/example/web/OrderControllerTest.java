package com.example.web;

import com.example.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(OrderController.class)
class OrderControllerTest {
  @MockBean
  OrderService orderService;

  @Test
  void returnsOrder() {
  }
}
