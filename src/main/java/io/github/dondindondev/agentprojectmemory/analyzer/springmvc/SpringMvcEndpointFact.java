package io.github.dondindondev.agentprojectmemory.analyzer.springmvc;

import java.util.List;

record SpringMvcEndpointFact(
    String controllerClass,
    String handlerMethod,
    List<String> httpMethods,
    SpringMvcHttpMethodSemantics httpMethodSemantics,
    List<String> paths,
    String declaredResponseType,
    List<String> evidenceIds) {
  SpringMvcEndpointFact {
    httpMethods = List.copyOf(httpMethods);
    paths = List.copyOf(paths);
    evidenceIds = List.copyOf(evidenceIds);
  }
}

enum SpringMvcHttpMethodSemantics {
  DECLARED,
  NOT_DECLARED,
  UNSUPPORTED
}
