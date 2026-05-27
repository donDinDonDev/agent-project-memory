package io.github.dondindondev.agentprojectmemory.analyzer.springmvc;

record SpringMvcEndpointEvidence(
    String id,
    String sourcePath,
    String className,
    String methodName,
    String annotationSymbol,
    Integer lineStart,
    Integer lineEnd,
    String excerpt,
    String confidence) {
}
