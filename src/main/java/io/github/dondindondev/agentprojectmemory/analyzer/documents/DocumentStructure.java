package io.github.dondindondev.agentprojectmemory.analyzer.documents;

import java.util.List;

record DocumentStructure(
    List<DocumentHeadingFact> headings,
    List<DocumentChunkFact> chunks,
    boolean headingCapReached,
    boolean chunkCapReached) {
  DocumentStructure {
    headings = List.copyOf(headings);
    chunks = List.copyOf(chunks);
  }

  static DocumentStructure empty() {
    return new DocumentStructure(List.of(), List.of(), false, false);
  }
}
