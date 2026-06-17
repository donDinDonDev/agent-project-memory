package io.github.dondindondev.agentprojectmemory.ingestion.adapter;

import java.util.List;

public record AdapterIngestionResult(
    boolean enabled,
    List<AdapterRun> adapterRuns,
    List<SourceDocument> sourceDocuments,
    List<SourceProvenance> provenance,
    List<AdapterDiagnostic> diagnostics) {
  public AdapterIngestionResult {
    adapterRuns = List.copyOf(adapterRuns);
    sourceDocuments = List.copyOf(sourceDocuments);
    provenance = List.copyOf(provenance);
    diagnostics = List.copyOf(diagnostics);
    if (!enabled
        && (!adapterRuns.isEmpty()
            || !sourceDocuments.isEmpty()
            || !provenance.isEmpty()
            || !diagnostics.isEmpty())) {
      throw new IllegalArgumentException("disabled adapter ingestion must not contain output");
    }
  }

  public static AdapterIngestionResult disabled() {
    return new AdapterIngestionResult(false, List.of(), List.of(), List.of(), List.of());
  }

  public static AdapterIngestionResult enabled(
      AdapterRun adapterRun,
      List<SourceDocument> sourceDocuments,
      List<SourceProvenance> provenance,
      List<AdapterDiagnostic> diagnostics) {
    return new AdapterIngestionResult(
        true,
        List.of(adapterRun),
        sourceDocuments,
        provenance,
        diagnostics);
  }

  public int acceptedCount() {
    return sourceDocuments.size();
  }

  public int rejectedCount() {
    return adapterRuns.stream().mapToInt(AdapterRun::rejectedCount).sum();
  }
}
