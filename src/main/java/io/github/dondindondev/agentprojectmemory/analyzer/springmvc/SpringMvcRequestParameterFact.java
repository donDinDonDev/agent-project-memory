package io.github.dondindondev.agentprojectmemory.analyzer.springmvc;

import java.util.List;

record SpringMvcRequestParameterFact(
    String name,
    String source,
    String javaType,
    List<String> evidenceIds) {
  SpringMvcRequestParameterFact {
    evidenceIds = List.copyOf(evidenceIds);
  }
}
