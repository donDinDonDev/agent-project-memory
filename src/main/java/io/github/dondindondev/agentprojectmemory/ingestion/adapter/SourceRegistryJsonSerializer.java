package io.github.dondindondev.agentprojectmemory.ingestion.adapter;

import io.github.dondindondev.agentprojectmemory.OutputRedactor;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class SourceRegistryJsonSerializer {
  public static final String SOURCE_REGISTRY_SCHEMA_VERSION = "1.0";
  public static final String GIT_HOSTING_SOURCE_REGISTRY_SCHEMA_VERSION = "1.1";

  public String serialize(AdapterIngestionResult result) {
    Objects.requireNonNull(result, "result");
    if (!result.enabled()) {
      throw new IllegalArgumentException("source registry requires enabled adapter ingestion");
    }
    StringBuilder json = new StringBuilder();
    json.append("{\n");
    appendStringField(json, 1, "source_registry_schema_version", schemaVersion(result), true);
    appendAdapterRuns(json, result.adapterRuns(), true);
    appendSourceDocuments(json, result.sourceDocuments(), true);
    appendProvenance(json, result.provenance(), true);
    appendDiagnostics(json, result.diagnostics(), false);
    json.append("}\n");
    return json.toString();
  }

  private void appendAdapterRuns(StringBuilder json, List<AdapterRun> runs, boolean trailingComma) {
    indent(json, 1);
    json.append("\"adapter_runs\": [");
    if (runs.isEmpty()) {
      json.append("]");
      appendLineEnding(json, trailingComma);
      return;
    }
    json.append("\n");
    for (int index = 0; index < runs.size(); index++) {
      AdapterRun run = runs.get(index);
      indent(json, 2);
      json.append("{\n");
      appendStringField(json, 3, "id", run.id(), true);
      appendAdapterIdentity(json, run.adapterIdentity(), true);
      appendStringField(json, 3, "import_mode", run.importMode().contractValue(), true);
      appendStringField(json, 3, "source_location_kind", run.sourceLocationKind(), true);
      appendStringField(json, 3, "network_access", run.networkAccess(), true);
      appendStringField(json, 3, "input_content_hash", run.inputContentHash(), true);
      appendStringField(json, 3, "content_status", run.contentStatus(), true);
      appendIntegerField(json, 3, "accepted_count", run.acceptedCount(), true);
      appendIntegerField(json, 3, "rejected_count", run.rejectedCount(), true);
      appendIntegerField(json, 3, "diagnostic_count", run.diagnosticCount(), false);
      indent(json, 2);
      json.append("}");
      appendLineEnding(json, index < runs.size() - 1);
    }
    indent(json, 1);
    json.append("]");
    appendLineEnding(json, trailingComma);
  }

  private void appendSourceDocuments(
      StringBuilder json,
      List<SourceDocument> documents,
      boolean trailingComma) {
    indent(json, 1);
    json.append("\"source_documents\": [");
    if (documents.isEmpty()) {
      json.append("]");
      appendLineEnding(json, trailingComma);
      return;
    }
    json.append("\n");
    for (int index = 0; index < documents.size(); index++) {
      SourceDocument document = documents.get(index);
      indent(json, 2);
      json.append("{\n");
      appendStringField(json, 3, "id", document.id(), true);
      appendStringField(json, 3, "source_type", document.sourceType(), true);
      appendStringField(json, 3, "source_identity", document.sourceIdentity(), true);
      appendNullableStringField(json, 3, "title", document.title(), true);
      appendStringField(json, 3, "content_hash", document.contentHash(), true);
      appendStringField(json, 3, "content_status", document.contentStatus().contractValue(), true);
      appendStringField(json, 3, "provenance_id", document.provenanceId(), false);
      indent(json, 2);
      json.append("}");
      appendLineEnding(json, index < documents.size() - 1);
    }
    indent(json, 1);
    json.append("]");
    appendLineEnding(json, trailingComma);
  }

  private void appendProvenance(
      StringBuilder json,
      List<SourceProvenance> provenanceRecords,
      boolean trailingComma) {
    indent(json, 1);
    json.append("\"provenance\": [");
    if (provenanceRecords.isEmpty()) {
      json.append("]");
      appendLineEnding(json, trailingComma);
      return;
    }
    json.append("\n");
    for (int index = 0; index < provenanceRecords.size(); index++) {
      SourceProvenance provenance = provenanceRecords.get(index);
      indent(json, 2);
      json.append("{\n");
      appendStringField(json, 3, "id", provenance.id(), true);
      appendAdapterIdentity(json, provenance.adapterIdentity(), true);
      appendStringField(json, 3, "import_mode", provenance.importMode().contractValue(), true);
      appendStringField(json, 3, "source_type", provenance.sourceType(), true);
      appendStringField(json, 3, "source_identity", provenance.sourceIdentity(), true);
      appendStringField(json, 3, "content_hash", provenance.contentHash(), true);
      appendStringField(json, 3, "source_location_kind", provenance.sourceLocationKind(), true);
      appendStringField(json, 3, "network_access", provenance.networkAccess(), true);
      if (provenance.gitHosting() != null) {
        appendGitHostingMetadata(json, provenance.gitHosting(), true);
      }
      appendStringArrayField(json, 3, "trust_boundary_labels", provenance.trustBoundaryLabels(), false);
      indent(json, 2);
      json.append("}");
      appendLineEnding(json, index < provenanceRecords.size() - 1);
    }
    indent(json, 1);
    json.append("]");
    appendLineEnding(json, trailingComma);
  }

  private void appendDiagnostics(
      StringBuilder json,
      List<AdapterDiagnostic> diagnostics,
      boolean trailingComma) {
    indent(json, 1);
    json.append("\"diagnostics\": {\n");
    appendStringField(json, 2, "analysis_status", "analyzed", true);
    indent(json, 2);
    json.append("\"items\": [");
    if (diagnostics.isEmpty()) {
      json.append("]\n");
      indent(json, 1);
      json.append("}");
      appendLineEnding(json, trailingComma);
      return;
    }
    json.append("\n");
    for (int index = 0; index < diagnostics.size(); index++) {
      AdapterDiagnostic diagnostic = diagnostics.get(index);
      indent(json, 3);
      json.append("{\n");
      appendStringField(json, 4, "id", diagnostic.id(), true);
      appendStringField(json, 4, "severity", diagnostic.severity(), true);
      appendStringField(json, 4, "category", diagnostic.category(), true);
      appendStringField(json, 4, "signal", diagnostic.signal(), true);
      appendStringField(json, 4, "message", diagnostic.message(), true);
      appendNullableIntegerField(json, 4, "record_ordinal", diagnostic.recordOrdinal(), false);
      indent(json, 3);
      json.append("}");
      appendLineEnding(json, index < diagnostics.size() - 1);
    }
    indent(json, 2);
    json.append("]\n");
    indent(json, 1);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendAdapterIdentity(
      StringBuilder json,
      AdapterIdentity adapterIdentity,
      boolean trailingComma) {
    indent(json, 3);
    json.append("\"adapter\": {\n");
    appendStringField(json, 4, "name", adapterIdentity.name(), true);
    appendStringField(json, 4, "version", adapterIdentity.version(), false);
    indent(json, 3);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private String schemaVersion(AdapterIngestionResult result) {
    return result.provenance().stream().anyMatch(provenance -> provenance.gitHosting() != null)
        ? GIT_HOSTING_SOURCE_REGISTRY_SCHEMA_VERSION
        : SOURCE_REGISTRY_SCHEMA_VERSION;
  }

  private void appendGitHostingMetadata(
      StringBuilder json,
      GitHostingMetadata metadata,
      boolean trailingComma) {
    indent(json, 3);
    json.append("\"git_hosting\": {\n");
    appendStringField(json, 4, "provider", metadata.provider(), true);
    appendStringField(json, 4, "host", metadata.host(), true);
    appendStringField(json, 4, "namespace", metadata.namespace(), true);
    appendStringField(json, 4, "record_type", metadata.recordType(), true);
    List<MetadataField> optionalFields = new ArrayList<>();
    if (metadata.recordState() != null) {
      optionalFields.add(new MetadataField("record_state", metadata.recordState()));
    }
    if (metadata.sourceUrl() != null) {
      optionalFields.add(new MetadataField("source_url", metadata.sourceUrl()));
    }
    if (metadata.exportedAt() != null) {
      optionalFields.add(new MetadataField("exported_at", metadata.exportedAt()));
    }
    if (metadata.recordUpdatedAt() != null) {
      optionalFields.add(new MetadataField("record_updated_at", metadata.recordUpdatedAt()));
    }
    appendStringField(json, 4, "record_number", metadata.recordNumber(), !optionalFields.isEmpty());
    for (int index = 0; index < optionalFields.size(); index++) {
      MetadataField field = optionalFields.get(index);
      appendStringField(json, 4, field.name(), field.value(), index < optionalFields.size() - 1);
    }
    indent(json, 3);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendStringArrayField(
      StringBuilder json,
      int level,
      String fieldName,
      List<String> values,
      boolean trailingComma) {
    indent(json, level);
    json.append('"').append(fieldName).append("\": [");
    for (int index = 0; index < values.size(); index++) {
      if (index > 0) {
        json.append(", ");
      }
      json.append('"').append(jsonString(values.get(index))).append('"');
    }
    json.append("]");
    appendLineEnding(json, trailingComma);
  }

  private void appendStringField(
      StringBuilder json,
      int level,
      String fieldName,
      String value,
      boolean trailingComma) {
    indent(json, level);
    json.append('"').append(fieldName).append("\": \"")
        .append(jsonString(OutputRedactor.redact(value)))
        .append('"');
    appendLineEnding(json, trailingComma);
  }

  private void appendNullableStringField(
      StringBuilder json,
      int level,
      String fieldName,
      String value,
      boolean trailingComma) {
    indent(json, level);
    json.append('"').append(fieldName).append("\": ");
    if (value == null) {
      json.append("null");
    } else {
      json.append('"').append(jsonString(OutputRedactor.redact(value))).append('"');
    }
    appendLineEnding(json, trailingComma);
  }

  private void appendIntegerField(
      StringBuilder json,
      int level,
      String fieldName,
      int value,
      boolean trailingComma) {
    indent(json, level);
    json.append('"').append(fieldName).append("\": ").append(value);
    appendLineEnding(json, trailingComma);
  }

  private void appendNullableIntegerField(
      StringBuilder json,
      int level,
      String fieldName,
      Integer value,
      boolean trailingComma) {
    indent(json, level);
    json.append('"').append(fieldName).append("\": ");
    if (value == null) {
      json.append("null");
    } else {
      json.append(value);
    }
    appendLineEnding(json, trailingComma);
  }

  private void appendLineEnding(StringBuilder json, boolean trailingComma) {
    if (trailingComma) {
      json.append(",");
    }
    json.append("\n");
  }

  private void indent(StringBuilder json, int level) {
    json.append("  ".repeat(level));
  }

  private String jsonString(String value) {
    StringBuilder escaped = new StringBuilder();
    for (int index = 0; index < value.length(); index++) {
      char current = value.charAt(index);
      switch (current) {
        case '"' -> escaped.append("\\\"");
        case '\\' -> escaped.append("\\\\");
        case '\b' -> escaped.append("\\b");
        case '\f' -> escaped.append("\\f");
        case '\n' -> escaped.append("\\n");
        case '\r' -> escaped.append("\\r");
        case '\t' -> escaped.append("\\t");
        default -> {
          if (current < 0x20) {
            escaped.append(String.format("\\u%04x", (int) current));
          } else {
            escaped.append(current);
          }
        }
      }
    }
    return escaped.toString();
  }

  private record MetadataField(String name, String value) {
  }
}
