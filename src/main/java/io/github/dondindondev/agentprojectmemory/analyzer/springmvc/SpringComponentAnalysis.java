package io.github.dondindondev.agentprojectmemory.analyzer.springmvc;

import java.util.List;

record SpringComponentAnalysis(
    List<SpringComponentFact> components,
    List<SpringComponentEvidence> evidence) {
  SpringComponentAnalysis {
    components = List.copyOf(components);
    evidence = List.copyOf(evidence);
  }
}
