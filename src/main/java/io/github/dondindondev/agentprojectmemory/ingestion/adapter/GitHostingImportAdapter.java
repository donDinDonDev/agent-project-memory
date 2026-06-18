package io.github.dondindondev.agentprojectmemory.ingestion.adapter;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.dondindondev.agentprojectmemory.OutputRedactor;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public final class GitHostingImportAdapter {
  public static final String FORMAT = "agent-project-memory.git_hosting_export.v1";
  public static final String SOURCE_TYPE_GITHUB_ISSUE = "github_issue";
  public static final String SOURCE_TYPE_GITHUB_PULL_REQUEST = "github_pull_request";
  public static final String SOURCE_TYPE_GITLAB_ISSUE = "gitlab_issue";
  public static final String SOURCE_TYPE_GITLAB_MERGE_REQUEST = "gitlab_merge_request";
  public static final int MAX_IMPORT_BYTES = 256 * 1024;
  static final int MAX_RECORDS = 64;
  private static final int MAX_JSON_NESTING_DEPTH = 16;
  private static final int MAX_JSON_STRING_LENGTH = 64 * 1024;
  private static final int MAX_TITLE_LENGTH = 200;
  private static final int MAX_BODY_LENGTH = 32 * 1024;
  private static final int MAX_HOST_LENGTH = 253;
  private static final int MAX_NAMESPACE_LENGTH = 200;
  private static final int MAX_RECORD_STATE_LENGTH = 40;
  private static final int MAX_TIMESTAMP_LENGTH = 64;
  private static final Pattern HOST_SEGMENT = Pattern.compile("[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?");
  private static final Pattern NAMESPACE_SEGMENT = Pattern.compile("[a-z0-9][a-z0-9._-]{0,99}");
  private static final Pattern POSITIVE_INTEGER = Pattern.compile("[1-9][0-9]{0,18}");
  private static final Pattern RECORD_STATE = Pattern.compile("[a-z0-9][a-z0-9_-]{0,39}");
  private static final Pattern SAFE_URL_PATH = Pattern.compile("[A-Za-z0-9._~/%-]+");
  private static final Set<String> ROOT_FIELDS = Set.of("format", "records");
  private static final Set<String> RECORD_FIELDS = Set.of(
      "provider",
      "host",
      "namespace",
      "record_type",
      "number",
      "iid",
      "title",
      "body",
      "status",
      "record_state",
      "source_url",
      "exported_at",
      "record_updated_at");
  private static final Set<String> SENSITIVE_SEGMENTS = Set.of(
      "accesskey",
      "accesstoken",
      "apikey",
      "apitoken",
      "authorization",
      "bearer",
      "clientsecret",
      "cookie",
      "credential",
      "credentials",
      "oauthtoken",
      "password",
      "passwd",
      "privatekey",
      "refreshtoken",
      "secret",
      "secrets",
      "sessionid",
      "sshkey",
      "token",
      "tokens");
  private static final AdapterIdentity IDENTITY =
      new AdapterIdentity(AdapterLocalImport.GIT_HOSTING_IMPORT_ADAPTER, "2.1.0");
  private static final Comparator<AcceptedRecord> ACCEPTED_RECORD_ORDER = Comparator
      .comparing((AcceptedRecord record) -> record.document().sourceType())
      .thenComparing(record -> record.document().sourceIdentity())
      .thenComparing(record -> record.document().id());
  private static final ObjectMapper JSON = new ObjectMapper(JsonFactory.builder()
      .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
      .streamReadConstraints(StreamReadConstraints.builder()
          .maxNestingDepth(MAX_JSON_NESTING_DEPTH)
          .maxStringLength(MAX_JSON_STRING_LENGTH)
          .build())
      .build());

  public AdapterIngestionResult read(Path repositoryRoot, AdapterLocalImport localImport)
      throws IOException {
    Objects.requireNonNull(repositoryRoot, "repositoryRoot");
    Objects.requireNonNull(localImport, "localImport");
    if (!AdapterLocalImport.GIT_HOSTING_IMPORT_ADAPTER.equals(localImport.adapterName())) {
      throw new IOException("Adapter import selection is unsupported.");
    }
    if (localImport.importMode() != AdapterImportMode.LOCAL_EXPORT) {
      throw new IOException("Adapter import mode is unsupported.");
    }

    byte[] bytes = AdapterImportFileReader.readImportBytes(
        repositoryRoot,
        localImport.path(),
        MAX_IMPORT_BYTES);
    String inputContentHash = StableAdapterIds.contentHash(new String(bytes, StandardCharsets.UTF_8));
    JsonNode root = parseImportRoot(bytes);
    requireRootShape(root);

    List<JsonNode> records = records(root.path("records"));
    List<AdapterDiagnostic> diagnostics = new ArrayList<>();
    List<AcceptedRecord> acceptedRecords = new ArrayList<>();
    Set<String> acceptedKeys = new HashSet<>();

    int processCount = Math.min(records.size(), MAX_RECORDS);
    if (records.size() > MAX_RECORDS) {
      diagnostics.add(diagnostic(
          "record_count_cap_reached",
          null,
          "Git hosting import record cap reached; additional records were omitted."));
    }

    for (int index = 0; index < processCount; index++) {
      acceptRecord(records.get(index), index + 1, acceptedRecords, acceptedKeys, diagnostics);
    }

    List<SourceDocument> sourceDocuments = acceptedRecords.stream()
        .sorted(ACCEPTED_RECORD_ORDER)
        .map(AcceptedRecord::document)
        .toList();
    List<SourceProvenance> provenance = acceptedRecords.stream()
        .sorted(ACCEPTED_RECORD_ORDER)
        .map(AcceptedRecord::provenance)
        .toList();
    int rejectedCount = records.size() - sourceDocuments.size();
    AdapterRun adapterRun = AdapterRun.completed(
        IDENTITY,
        AdapterImportMode.LOCAL_EXPORT,
        inputContentHash,
        sourceDocuments.size(),
        rejectedCount,
        diagnostics.size());
    return AdapterIngestionResult.enabled(adapterRun, sourceDocuments, provenance, diagnostics);
  }

  private JsonNode parseImportRoot(byte[] bytes) throws IOException {
    try {
      JsonNode root = JSON.readTree(bytes);
      if (root == null || !root.isObject()) {
        throw new IOException("Adapter import file must contain a JSON object.");
      }
      return root;
    } catch (JsonProcessingException exception) {
      throw new IOException("Adapter import file could not be parsed as JSON.");
    }
  }

  private void requireRootShape(JsonNode root) throws IOException {
    if (!hasOnlyFields(root, ROOT_FIELDS)) {
      throw new IOException("Adapter import file contains unsupported top-level fields.");
    }
    if (!FORMAT.equals(text(root.path("format")))) {
      throw new IOException("Adapter import file format is unsupported.");
    }
    if (!root.path("records").isArray()) {
      throw new IOException("Adapter import file must contain a records array.");
    }
  }

  private List<JsonNode> records(JsonNode recordsNode) {
    List<JsonNode> records = new ArrayList<>();
    recordsNode.forEach(records::add);
    return records;
  }

  private void acceptRecord(
      JsonNode record,
      int ordinal,
      List<AcceptedRecord> acceptedRecords,
      Set<String> acceptedKeys,
      List<AdapterDiagnostic> diagnostics) {
    if (!record.isObject()) {
      diagnostics.add(diagnostic(
          "malformed_record_rejected",
          ordinal,
          "Git hosting import record rejected because it is not an object."));
      return;
    }
    if (!hasOnlyFields(record, RECORD_FIELDS)) {
      diagnostics.add(diagnostic(
          "unsupported_record_shape_rejected",
          ordinal,
          "Git hosting import record rejected because its field set is unsupported."));
      return;
    }
    if (!textualFieldsIfPresent(
        record,
        "provider",
        "host",
        "namespace",
        "record_type",
        "title",
        "body",
        "status",
        "record_state",
        "source_url",
        "exported_at",
        "record_updated_at")) {
      diagnostics.add(diagnostic(
          "malformed_record_rejected",
          ordinal,
          "Git hosting import record rejected because a string field has an unsupported value."));
      return;
    }

    String status = text(record.path("status"));
    if (!"current".equals(status)) {
      diagnostics.add(diagnostic(statusSignal(status), ordinal, statusMessage(status)));
      return;
    }

    GitHostingRecordIdentity identity = sourceIdentity(record);
    if (identity.ambiguous()) {
      diagnostics.add(diagnostic(
          "ambiguous_record_rejected",
          ordinal,
          "Git hosting import record rejected because its identity fields are ambiguous."));
      return;
    }
    if (!identity.accepted()) {
      diagnostics.add(diagnostic(
          identity.unsupportedSourceType()
              ? "unsupported_source_type_rejected"
              : "provenance_missing_record_rejected",
          ordinal,
          identity.unsupportedSourceType()
              ? "Git hosting import record rejected because its source type is unsupported."
              : "Git hosting import record rejected because stable source identity is missing or unsafe."));
      return;
    }

    String body = text(record.path("body"));
    if (body.length() > MAX_BODY_LENGTH) {
      diagnostics.add(diagnostic(
          "record_body_too_large_rejected",
          ordinal,
          "Git hosting import record rejected because normalized body exceeds the record limit."));
      return;
    }

    String recordState = optionalRecordState(record.path("record_state"));
    if (record.hasNonNull("record_state") && recordState == null) {
      diagnostics.add(diagnostic(
          "malformed_record_rejected",
          ordinal,
          "Git hosting import record rejected because record state metadata is malformed."));
      return;
    }

    String sourceUrl = optionalSourceUrl(record.path("source_url"), identity);
    if (record.hasNonNull("source_url") && sourceUrl == null) {
      diagnostics.add(diagnostic(
          "unsafe_source_url_rejected",
          ordinal,
          "Git hosting import record rejected because source URL metadata is unsafe."));
      return;
    }

    String exportedAt = optionalTimestamp(record.path("exported_at"));
    String recordUpdatedAt = optionalTimestamp(record.path("record_updated_at"));
    if ((record.hasNonNull("exported_at") && exportedAt == null)
        || (record.hasNonNull("record_updated_at") && recordUpdatedAt == null)) {
      diagnostics.add(diagnostic(
          "malformed_record_rejected",
          ordinal,
          "Git hosting import record rejected because timestamp metadata is malformed."));
      return;
    }

    String duplicateKey = identity.sourceType() + "\0" + identity.sourceIdentity();
    if (!acceptedKeys.add(duplicateKey)) {
      diagnostics.add(diagnostic(
          "duplicate_source_identity_rejected",
          ordinal,
          "Git hosting import record rejected because source identity is duplicated."));
      return;
    }

    String title = boundedTitle(text(record.path("title")));
    String contentHash = StableAdapterIds.contentHash(
        FORMAT,
        identity.sourceType(),
        identity.sourceIdentity(),
        title == null ? "" : title,
        body,
        recordState == null ? "" : recordState,
        sourceUrl == null ? "" : sourceUrl,
        exportedAt == null ? "" : exportedAt,
        recordUpdatedAt == null ? "" : recordUpdatedAt);
    GitHostingMetadata gitHosting = new GitHostingMetadata(
        identity.provider(),
        identity.host(),
        identity.namespace(),
        identity.recordType(),
        identity.recordNumber(),
        recordState,
        sourceUrl,
        exportedAt,
        recordUpdatedAt);
    SourceProvenance provenance = SourceProvenance.accepted(
        IDENTITY,
        AdapterImportMode.LOCAL_EXPORT,
        identity.sourceType(),
        identity.sourceIdentity(),
        contentHash,
        "repository_relative_file",
        "disabled",
        List.of(
            "git_hosting_import",
            identity.provider(),
            "repository_relative_file",
            "provenance_backed_external_context",
            "not_code_evidence",
            "network_disabled"),
        gitHosting);
    SourceDocument document = SourceDocument.accepted(
        IDENTITY,
        AdapterImportMode.LOCAL_EXPORT,
        identity.sourceType(),
        identity.sourceIdentity(),
        title,
        contentHash,
        provenance.id());
    acceptedRecords.add(new AcceptedRecord(document, provenance));
  }

  private GitHostingRecordIdentity sourceIdentity(JsonNode record) {
    String provider = normalizedToken(text(record.path("provider")));
    String recordType = normalizedToken(text(record.path("record_type")));
    String host = normalizedHost(text(record.path("host")));
    String namespace = normalizedNamespace(text(record.path("namespace")));
    boolean hasNumber = record.has("number") && !record.path("number").isNull();
    boolean hasIid = record.has("iid") && !record.path("iid").isNull();
    if (hasNumber && hasIid) {
      return GitHostingRecordIdentity.ambiguousRecord();
    }
    if (provider.isBlank() || recordType.isBlank()) {
      return GitHostingRecordIdentity.rejected();
    }
    if (!("github".equals(provider) || "gitlab".equals(provider))) {
      return GitHostingRecordIdentity.unsupported();
    }
    SourceType sourceType = sourceType(provider, recordType);
    if (sourceType == null) {
      return GitHostingRecordIdentity.unsupported();
    }
    String recordNumber = "github".equals(provider)
        ? normalizedPositiveInteger(record.path("number"))
        : normalizedPositiveInteger(record.path("iid"));
    boolean wrongNumberField = "github".equals(provider) ? hasIid : hasNumber;
    if (wrongNumberField) {
      return GitHostingRecordIdentity.ambiguousRecord();
    }
    if (host == null || namespace == null || recordNumber == null) {
      return GitHostingRecordIdentity.rejected();
    }
    if ("github".equals(provider) && namespace.split("/", -1).length != 2) {
      return GitHostingRecordIdentity.rejected();
    }
    String sourceIdentity = "git-hosting/%s/%s/%s/%s/%s".formatted(
        provider,
        host,
        namespace,
        sourceType.identityRecordType(),
        recordNumber);
    return GitHostingRecordIdentity.accepted(
        provider,
        host,
        namespace,
        sourceType.identityRecordType(),
        recordNumber,
        sourceType.sourceType(),
        sourceIdentity);
  }

  private SourceType sourceType(String provider, String recordType) {
    if ("github".equals(provider) && "issue".equals(recordType)) {
      return new SourceType(SOURCE_TYPE_GITHUB_ISSUE, "issue");
    }
    if ("github".equals(provider) && "pull_request".equals(recordType)) {
      return new SourceType(SOURCE_TYPE_GITHUB_PULL_REQUEST, "pull_request");
    }
    if ("gitlab".equals(provider) && "issue".equals(recordType)) {
      return new SourceType(SOURCE_TYPE_GITLAB_ISSUE, "issue");
    }
    if ("gitlab".equals(provider) && "merge_request".equals(recordType)) {
      return new SourceType(SOURCE_TYPE_GITLAB_MERGE_REQUEST, "merge_request");
    }
    return null;
  }

  private String normalizedToken(String value) {
    if (value == null || value.isBlank()) {
      return "";
    }
    return value.trim().toLowerCase(Locale.ROOT);
  }

  private String normalizedHost(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    String normalized = value.trim().toLowerCase(Locale.ROOT);
    if (normalized.length() > MAX_HOST_LENGTH
        || normalized.startsWith(".")
        || normalized.endsWith(".")
        || normalized.contains("..")
        || normalized.contains("/")
        || normalized.contains("\\")
        || normalized.contains(":")
        || normalized.contains("@")) {
      return null;
    }
    String[] segments = normalized.split("\\.", -1);
    for (String segment : segments) {
      if (!HOST_SEGMENT.matcher(segment).matches()) {
        return null;
      }
    }
    return normalized;
  }

  private String normalizedNamespace(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    String normalized = value.trim().toLowerCase(Locale.ROOT);
    if (normalized.length() > MAX_NAMESPACE_LENGTH
        || normalized.startsWith("/")
        || normalized.startsWith("./")
        || normalized.contains("\\")
        || normalized.contains("://")) {
      return null;
    }
    String[] segments = normalized.split("/", -1);
    if (segments.length < 2) {
      return null;
    }
    for (String segment : segments) {
      if (".".equals(segment)
          || "..".equals(segment)
          || sensitiveSegment(segment)
          || !NAMESPACE_SEGMENT.matcher(segment).matches()) {
        return null;
      }
    }
    return normalized;
  }

  private boolean sensitiveSegment(String value) {
    String lowerCase = value.toLowerCase(Locale.ROOT);
    int delimiter = firstSensitiveKeyDelimiter(lowerCase);
    String keyCandidate = delimiter >= 0 ? lowerCase.substring(0, delimiter) : lowerCase;
    String normalizedKey = keyCandidate.replace("-", "").replace("_", "");
    return SENSITIVE_SEGMENTS.contains(normalizedKey);
  }

  private int firstSensitiveKeyDelimiter(String value) {
    int colon = value.indexOf(':');
    int equals = value.indexOf('=');
    if (colon < 0) {
      return equals;
    }
    if (equals < 0) {
      return colon;
    }
    return Math.min(colon, equals);
  }

  private String normalizedPositiveInteger(JsonNode node) {
    if (node == null || node.isMissingNode() || node.isNull()) {
      return null;
    }
    String value;
    if (node.isIntegralNumber()) {
      value = node.asText();
    } else if (node.isTextual()) {
      value = node.asText().trim();
    } else {
      return null;
    }
    if (!POSITIVE_INTEGER.matcher(value).matches()) {
      return null;
    }
    try {
      return Long.toString(Long.parseLong(value));
    } catch (NumberFormatException exception) {
      return null;
    }
  }

  private String optionalRecordState(JsonNode node) {
    String value = text(node);
    if (value.isBlank()) {
      return null;
    }
    String normalized = value.toLowerCase(Locale.ROOT);
    if (normalized.length() > MAX_RECORD_STATE_LENGTH || !RECORD_STATE.matcher(normalized).matches()) {
      return null;
    }
    return normalized;
  }

  private String optionalSourceUrl(JsonNode node, GitHostingRecordIdentity identity) {
    String value = text(node);
    if (value.isBlank()) {
      return null;
    }
    try {
      URI uri = new URI(value.trim());
      if (!"https".equalsIgnoreCase(uri.getScheme())
          || uri.getUserInfo() != null
          || uri.getRawQuery() != null
          || uri.getRawFragment() != null
          || uri.getPort() >= 0
          || uri.getHost() == null
          || !identity.host().equals(uri.getHost().toLowerCase(Locale.ROOT))) {
        return null;
      }
      String rawPath = uri.getRawPath();
      if (rawPath == null
          || rawPath.isBlank()
          || rawPath.contains("\\")
          || rawPath.contains("//")
          || rawPath.toLowerCase(Locale.ROOT).contains("%2f")
          || rawPath.toLowerCase(Locale.ROOT).contains("%2e")
          || !SAFE_URL_PATH.matcher(rawPath).matches()) {
        return null;
      }
      if (!rawPath.toLowerCase(Locale.ROOT).equals(expectedSourceUrlPath(identity))) {
        return null;
      }
      return uri.toASCIIString();
    } catch (URISyntaxException exception) {
      return null;
    }
  }

  private String expectedSourceUrlPath(GitHostingRecordIdentity identity) {
    String recordPath = switch (identity.provider() + "/" + identity.recordType()) {
      case "github/issue" -> "issues/" + identity.recordNumber();
      case "github/pull_request" -> "pull/" + identity.recordNumber();
      case "gitlab/issue" -> "-/issues/" + identity.recordNumber();
      case "gitlab/merge_request" -> "-/merge_requests/" + identity.recordNumber();
      default -> "";
    };
    return "/" + identity.namespace() + "/" + recordPath;
  }

  private String optionalTimestamp(JsonNode node) {
    String value = text(node);
    if (value.isBlank()) {
      return null;
    }
    if (value.length() > MAX_TIMESTAMP_LENGTH) {
      return null;
    }
    try {
      return OffsetDateTime.parse(value).toInstant().toString();
    } catch (DateTimeParseException exception) {
      return null;
    }
  }

  private boolean hasOnlyFields(JsonNode node, Set<String> allowedFields) {
    Iterator<String> fields = node.fieldNames();
    while (fields.hasNext()) {
      if (!allowedFields.contains(fields.next())) {
        return false;
      }
    }
    return true;
  }

  private boolean textualFieldsIfPresent(JsonNode node, String... fieldNames) {
    for (String fieldName : fieldNames) {
      JsonNode field = node.path(fieldName);
      if (!field.isMissingNode() && !field.isNull() && !field.isTextual()) {
        return false;
      }
    }
    return true;
  }

  private String text(JsonNode node) {
    if (node == null || !node.isTextual()) {
      return "";
    }
    return node.asText().trim();
  }

  private String boundedTitle(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    String redacted = OutputRedactor.redact(value.trim());
    if (redacted.length() <= MAX_TITLE_LENGTH) {
      return redacted;
    }
    return redacted.substring(0, MAX_TITLE_LENGTH);
  }

  private String statusSignal(String status) {
    return switch (status) {
      case "stale" -> "stale_record_rejected";
      case "partial" -> "partial_record_rejected";
      case "ambiguous" -> "ambiguous_record_rejected";
      default -> "unsupported_status_rejected";
    };
  }

  private String statusMessage(String status) {
    return switch (status) {
      case "stale" -> "Git hosting import record rejected because it is marked stale.";
      case "partial" -> "Git hosting import record rejected because it is marked partial.";
      case "ambiguous" -> "Git hosting import record rejected because it is marked ambiguous.";
      default -> "Git hosting import record rejected because status is unsupported.";
    };
  }

  private AdapterDiagnostic diagnostic(String signal, Integer ordinal, String message) {
    String scope = ordinal == null ? "file" : "record:%06d".formatted(ordinal);
    return new AdapterDiagnostic(
        "adapter-diagnostic:git-hosting-import:" + scope + ":" + signal,
        "warning",
        "git_hosting_import",
        signal,
        message,
        ordinal);
  }

  private record AcceptedRecord(SourceDocument document, SourceProvenance provenance) {
  }

  private record SourceType(String sourceType, String identityRecordType) {
  }

  private record GitHostingRecordIdentity(
      boolean accepted,
      boolean ambiguous,
      boolean unsupportedSourceType,
      String provider,
      String host,
      String namespace,
      String recordType,
      String recordNumber,
      String sourceType,
      String sourceIdentity) {
    static GitHostingRecordIdentity accepted(
        String provider,
        String host,
        String namespace,
        String recordType,
        String recordNumber,
        String sourceType,
        String sourceIdentity) {
      return new GitHostingRecordIdentity(
          true,
          false,
          false,
          provider,
          host,
          namespace,
          recordType,
          recordNumber,
          sourceType,
          sourceIdentity);
    }

    static GitHostingRecordIdentity rejected() {
      return new GitHostingRecordIdentity(false, false, false, null, null, null, null, null, null, null);
    }

    static GitHostingRecordIdentity ambiguousRecord() {
      return new GitHostingRecordIdentity(false, true, false, null, null, null, null, null, null, null);
    }

    static GitHostingRecordIdentity unsupported() {
      return new GitHostingRecordIdentity(false, false, true, null, null, null, null, null, null, null);
    }
  }
}
