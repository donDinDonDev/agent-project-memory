package io.github.dondindondev.agentprojectmemory.analyzer.springmvc;

import java.util.List;

record SpringComponentFact(
    String id,
    String className,
    List<String> stereotypes,
    List<String> evidenceIds) {
  SpringComponentFact {
    stereotypes = List.copyOf(stereotypes);
    evidenceIds = List.copyOf(evidenceIds);
  }
}
