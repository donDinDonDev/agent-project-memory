package io.github.dondindondev.agentprojectmemory.analyzer.springmvc;

import java.util.List;

record SpringMvcEndpointFact(
    String controllerClass,
    String handlerMethod,
    List<String> httpMethods,
    SpringMvcHttpMethodSemantics httpMethodSemantics,
    List<String> paths,
    List<SpringMvcRequestParameterFact> requestParameters,
    String requestBodyType,
    List<String> requestBodyEvidenceIds,
    String declaredResponseType,
    SpringMvcEndpointMappingSource mappingSource,
    List<String> evidenceIds) {
  SpringMvcEndpointFact {
    httpMethods = List.copyOf(httpMethods);
    paths = List.copyOf(paths);
    requestParameters = List.copyOf(requestParameters);
    requestBodyEvidenceIds = List.copyOf(requestBodyEvidenceIds);
    evidenceIds = List.copyOf(evidenceIds);
  }
}

enum SpringMvcHttpMethodSemantics {
  DECLARED,
  NOT_DECLARED,
  UNSUPPORTED
}
