package com.example.components;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

@Component
class PlainComponent {
}

@Service
class OrderService {
}

@Repository
class OrderRepository {
}

@Controller
class PageController {
}

@RestController
class ApiController {
}

@Configuration
class AppConfiguration {
}

@Deprecated
class PlainJavaClass {
}
