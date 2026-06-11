package io.github.dondindondev.agentprojectmemory.analyzer.documents;

record DocumentAnalysisLimits(
    int maxDocuments,
    long maxTotalDocumentBytes,
    int maxHeadings,
    int maxChunks,
    int maxReconciliationMentions,
    int maxReconciliationSignals) {
  private static final DocumentAnalysisLimits DEFAULTS = new DocumentAnalysisLimits(
      256,
      16L * 1024L * 1024L,
      4_096,
      4_096,
      2_048,
      2_048);

  static DocumentAnalysisLimits defaults() {
    return DEFAULTS;
  }

  DocumentAnalysisLimits {
    if (maxDocuments < 0
        || maxTotalDocumentBytes < 0
        || maxHeadings < 0
        || maxChunks < 0
        || maxReconciliationMentions < 0
        || maxReconciliationSignals < 0) {
      throw new IllegalArgumentException("Document analysis limits must not be negative.");
    }
  }
}
