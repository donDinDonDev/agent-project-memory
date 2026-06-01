package io.github.dondindondev.agentprojectmemory.analyzer.springmvc;

import java.util.List;

record SpringMvcEndpointMappingSource(
    String kind,
    String declaringType,
    String declaringMethod,
    String binding,
    String uncertainty,
    List<String> evidenceIds) {
  SpringMvcEndpointMappingSource {
    evidenceIds = List.copyOf(evidenceIds);
  }
}
