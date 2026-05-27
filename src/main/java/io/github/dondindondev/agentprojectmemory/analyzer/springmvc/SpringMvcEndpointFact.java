package io.github.dondindondev.agentprojectmemory.analyzer.springmvc;

import java.util.List;

record SpringMvcEndpointFact(
    String controllerClass,
    String handlerMethod,
    String httpMethod,
    List<String> paths,
    String declaredResponseType,
    List<String> evidenceIds) {
  SpringMvcEndpointFact {
    paths = List.copyOf(paths);
    evidenceIds = List.copyOf(evidenceIds);
  }
}
