package io.github.dondindondev.agentprojectmemory.generator;

record EvidenceRecord(
    String id,
    String path,
    Integer lineStart,
    Integer lineEnd,
    String symbolName) {
  String location() {
    if (path == null || path.isBlank()) {
      return "unknown-source";
    }
    if (lineStart == null) {
      return path;
    }
    if (lineEnd == null || lineEnd.equals(lineStart)) {
      return path + ":" + lineStart;
    }
    return path + ":" + lineStart + "-" + lineEnd;
  }
}
