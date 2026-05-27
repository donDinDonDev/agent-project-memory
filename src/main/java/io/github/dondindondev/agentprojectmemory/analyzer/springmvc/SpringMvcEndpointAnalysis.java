package io.github.dondindondev.agentprojectmemory.analyzer.springmvc;

import java.util.List;

record SpringMvcEndpointAnalysis(
    List<SpringMvcEndpointFact> endpoints,
    List<SpringMvcEndpointEvidence> evidence) {
  SpringMvcEndpointAnalysis {
    endpoints = List.copyOf(endpoints);
    evidence = List.copyOf(evidence);
  }
}
