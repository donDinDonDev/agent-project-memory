package com.example.web;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

@SpringBootTest
class SpringSlice {
  @MockBean
  ProjectMapController projectMapController;
}

@WebMvcTest(ProjectMapController.class)
class WebControllerSlice {
  @MockBean
  ProjectMapController controller;

  @SpyBean
  ProjectMapController spyController;
}

@DataJpaTest
class DataRepositorySlice {
}
