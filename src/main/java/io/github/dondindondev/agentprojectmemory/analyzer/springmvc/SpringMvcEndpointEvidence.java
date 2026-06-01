package io.github.dondindondev.agentprojectmemory.analyzer.springmvc;

record SpringMvcEndpointEvidence(
    String id,
    String sourceType,
    String sourcePath,
    String className,
    String methodName,
    String symbolName,
    Integer lineStart,
    Integer lineEnd,
    String excerpt,
    String confidence) {
  String annotationSymbol() {
    return symbolName;
  }
}
