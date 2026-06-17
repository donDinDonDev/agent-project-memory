package io.github.dondindondev.agentprojectmemory.ingestion.adapter;

public record AdapterDiagnostic(
    String id,
    String severity,
    String category,
    String signal,
    String message,
    Integer recordOrdinal) {
  public AdapterDiagnostic {
    id = StableAdapterIds.requiredText(id, "adapter diagnostic id");
    severity = StableAdapterIds.requiredKnownText(
        severity,
        "adapter diagnostic severity",
        java.util.List.of("info", "warning", "error"));
    category = StableAdapterIds.requiredText(category, "adapter diagnostic category");
    signal = StableAdapterIds.requiredText(signal, "adapter diagnostic signal");
    message = StableAdapterIds.requiredText(message, "adapter diagnostic message");
    if (recordOrdinal != null && recordOrdinal < 1) {
      throw new IllegalArgumentException("adapter diagnostic record ordinal must be positive");
    }
  }
}
