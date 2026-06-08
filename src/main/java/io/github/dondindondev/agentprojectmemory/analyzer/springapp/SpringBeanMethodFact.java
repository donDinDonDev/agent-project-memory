package io.github.dondindondev.agentprojectmemory.analyzer.springapp;

import java.util.List;

public record SpringBeanMethodFact(
    String surfaceCategory,
    String supportType,
    String className,
    String methodName,
    String sourcePath,
    String beanSignal,
    String beanNameStatus,
    String idDiscriminator,
    List<String> evidenceIds) {
  public SpringBeanMethodFact {
    evidenceIds = List.copyOf(evidenceIds);
  }
}
