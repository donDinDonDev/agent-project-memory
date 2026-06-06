package io.github.dondindondev.agentprojectmemory.analyzer.springboot;

import java.util.List;

public record SpringBootApplicationFact(
    String id,
    String className,
    String sourcePath,
    String applicationSignal,
    boolean mainMethodPresent,
    List<String> mainMethodEvidenceIds,
    List<String> evidenceIds) {
  public SpringBootApplicationFact {
    mainMethodEvidenceIds = List.copyOf(mainMethodEvidenceIds);
    evidenceIds = List.copyOf(evidenceIds);
  }
}
