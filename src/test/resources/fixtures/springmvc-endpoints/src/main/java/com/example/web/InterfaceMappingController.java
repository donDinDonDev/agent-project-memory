package com.example.web;

import com.example.api.ImportedOrdersApi;
import com.example.generated.GeneratedOrdersApi;
import com.example.wildcard.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/interface")
interface InterfaceOrdersApi {
  @GetMapping("/orders/{id}")
  InterfaceOrderResponse getInterfaceOrder(
      @PathVariable("id") Long orderId,
      @RequestParam("expand") String expand);
}

@RestController
class InterfaceBackedController implements InterfaceOrdersApi {
  @Override
  public InterfaceOrderResponse getInterfaceOrder(Long orderId, String expand) {
    return new InterfaceOrderResponse();
  }
}

@RequestMapping("/interface-class")
interface InterfaceClassLevelApi {
  @PostMapping("/orders")
  InterfaceOrderResponse createFromInterface(@RequestBody InterfaceOrderRequest request);
}

@RestController
@RequestMapping("/api")
class InterfaceClassLevelController implements InterfaceClassLevelApi {
  @Override
  public InterfaceOrderResponse createFromInterface(InterfaceOrderRequest request) {
    return new InterfaceOrderResponse();
  }
}

interface DirectDuplicateApi {
  @GetMapping("/interface/direct")
  String duplicateDirect();
}

@RestController
class DirectDuplicateController implements DirectDuplicateApi {
  @Override
  @GetMapping("/controller/direct")
  public String duplicateDirect() {
    return "direct";
  }
}

interface AmbiguousFirstApi {
  @GetMapping("/ambiguous/first/{id}")
  String ambiguous(Long id);
}

interface AmbiguousSecondApi {
  @GetMapping("/ambiguous/second/{id}")
  String ambiguous(Long id);
}

@RestController
class AmbiguousInterfaceController implements AmbiguousFirstApi, AmbiguousSecondApi {
  @Override
  public String ambiguous(Long id) {
    return "ambiguous";
  }
}

@RestController
class GeneratedApiController implements GeneratedOrdersApi {
  @Override
  public String generatedOperation() {
    return "generated";
  }
}

@RestController
class ImportedApiController implements ImportedOrdersApi {
  @Override
  public String importedOrder() {
    return "imported";
  }
}

@RestController
class CrossPackageNoImportController implements CrossPackageOrdersApi {
  @Override
  public String crossPackageOrder() {
    return "cross-package";
  }
}

@RestController
class WildcardImportedController implements WildcardOrdersApi {
  @Override
  public String wildcardOrder() {
    return "wildcard";
  }
}

class InterfaceOrderRequest {
}

class InterfaceOrderResponse {
}
