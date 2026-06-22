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

public final class ConnectorImportAdapter {
  public static final String FORMAT = "agent-project-memory.connector_export.v1";
  public static final String SOURCE_TYPE_JIRA_ISSUE = "jira_issue";
  public static final String SOURCE_TYPE_YOUTRACK_ISSUE = "youtrack_issue";
  public static final String SOURCE_TYPE_YOUTRACK_ARTICLE = "youtrack_article";
  public static final String SOURCE_TYPE_CONFLUENCE_PAGE = "confluence_page";
  public static final int MAX_IMPORT_BYTES = 256 * 1024;
  static final int MAX_RECORDS = 64;
  private static final int MAX_JSON_NESTING_DEPTH = 16;
  private static final int MAX_JSON_STRING_LENGTH = 64 * 1024;
  private static final int MAX_TITLE_LENGTH = 200;
  private static final int MAX_BODY_LENGTH = 32 * 1024;
  private static final int MAX_HOST_LENGTH = 253;
  private static final int MAX_IDENTITY_PART_LENGTH = 100;
  private static final int MAX_RECORD_STATE_LENGTH = 40;
  private static final int MAX_TIMESTAMP_LENGTH = 64;
  private static final Pattern HOST_SEGMENT = Pattern.compile("[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?");
  private static final Pattern IDENTITY_PART = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._~-]{0,99}");
  private static final Pattern RECORD_STATE = Pattern.compile("[a-z0-9][a-z0-9_-]{0,39}");
  private static final Pattern SAFE_URL_PATH = Pattern.compile("[A-Za-z0-9._~/%-]+");
  private static final Set<String> ROOT_FIELDS = Set.of("format", "records");
  private static final Set<String> RECORD_FIELDS = Set.of(
      "provider",
      "host",
      "record_type",
      "project_key",
      "space_key",
      "collection_key",
      "issue_key",
      "issue_id",
      "article_id",
      "page_id",
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
      new AdapterIdentity(AdapterLocalImport.CONNECTOR_IMPORT_ADAPTER, "2.2.0");
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
    if (!AdapterLocalImport.CONNECTOR_IMPORT_ADAPTER.equals(localImport.adapterName())) {
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
          "Connector import record cap reached; additional records were omitted."));
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
          "Connector import record rejected because it is not an object."));
      return;
    }
    if (!hasOnlyFields(record, RECORD_FIELDS)) {
      diagnostics.add(diagnostic(
          "unsupported_record_shape_rejected",
          ordinal,
          "Connector import record rejected because its field set is unsupported."));
      return;
    }
    if (!validScalarFields(record)) {
      diagnostics.add(diagnostic(
          "malformed_record_rejected",
          ordinal,
          "Connector import record rejected because a field has an unsupported value."));
      return;
    }

    String status = text(record.path("status"));
    if (!"current".equals(status)) {
      diagnostics.add(diagnostic(statusSignal(status), ordinal, statusMessage(status)));
      return;
    }

    ConnectorRecordIdentity identity = sourceIdentity(record);
    if (identity.ambiguous()) {
      diagnostics.add(diagnostic(
          "ambiguous_record_rejected",
          ordinal,
          "Connector import record rejected because its identity fields are ambiguous."));
      return;
    }
    if (identity.authorityConfusing()) {
      diagnostics.add(diagnostic(
          "authority_confusing_record_rejected",
          ordinal,
          "Connector import record rejected because provider and identity fields conflict."));
      return;
    }
    if (!identity.accepted()) {
      diagnostics.add(diagnostic(
          identity.unsupportedSourceType()
              ? "unsupported_source_type_rejected"
              : "provenance_missing_record_rejected",
          ordinal,
          identity.unsupportedSourceType()
              ? "Connector import record rejected because its source type is unsupported."
              : "Connector import record rejected because stable source identity is missing or unsafe."));
      return;
    }

    String body = text(record.path("body"));
    if (body.length() > MAX_BODY_LENGTH) {
      diagnostics.add(diagnostic(
          "record_body_too_large_rejected",
          ordinal,
          "Connector import record rejected because normalized body exceeds the record limit."));
      return;
    }

    String recordState = optionalRecordState(record.path("record_state"));
    if (record.hasNonNull("record_state") && recordState == null) {
      diagnostics.add(diagnostic(
          "malformed_record_rejected",
          ordinal,
          "Connector import record rejected because record state metadata is malformed."));
      return;
    }

    String sourceUrl = optionalSourceUrl(record.path("source_url"), identity);
    if (record.hasNonNull("source_url") && sourceUrl == null) {
      diagnostics.add(diagnostic(
          "unsafe_source_url_rejected",
          ordinal,
          "Connector import record rejected because source URL metadata is unsafe."));
      return;
    }

    String exportedAt = optionalTimestamp(record.path("exported_at"));
    String recordUpdatedAt = optionalTimestamp(record.path("record_updated_at"));
    if ((record.hasNonNull("exported_at") && exportedAt == null)
        || (record.hasNonNull("record_updated_at") && recordUpdatedAt == null)) {
      diagnostics.add(diagnostic(
          "malformed_record_rejected",
          ordinal,
          "Connector import record rejected because timestamp metadata is malformed."));
      return;
    }

    String duplicateKey = identity.sourceType() + "\0" + identity.sourceIdentity();
    if (!acceptedKeys.add(duplicateKey)) {
      diagnostics.add(diagnostic(
          "duplicate_source_identity_rejected",
          ordinal,
          "Connector import record rejected because source identity is duplicated."));
      return;
    }

    String title = boundedTitle(text(record.path("title")));
    String contentHash = StableAdapterIds.contentHash(
        FORMAT,
        identity.sourceType(),
        identity.sourceIdentity(),
        title == null ? "" : title,
        body,
        identity.recordId() == null ? "" : identity.recordId(),
        recordState == null ? "" : recordState,
        sourceUrl == null ? "" : sourceUrl,
        exportedAt == null ? "" : exportedAt,
        recordUpdatedAt == null ? "" : recordUpdatedAt);
    ConnectorMetadata connector = new ConnectorMetadata(
        identity.provider(),
        identity.host(),
        identity.sourceFamily(),
        identity.containerType(),
        identity.containerKey(),
        identity.recordType(),
        identity.recordKey(),
        identity.recordId(),
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
            "connector_import",
            identity.provider(),
            "repository_relative_file",
            "provenance_backed_external_context",
            "not_code_evidence",
            "network_disabled"),
        connector);
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

  private ConnectorRecordIdentity sourceIdentity(JsonNode record) {
    String provider = normalizedToken(text(record.path("provider")));
    String recordType = normalizedToken(text(record.path("record_type")));
    String host = normalizedHost(text(record.path("host")));
    if (provider.isBlank() || recordType.isBlank()) {
      return ConnectorRecordIdentity.rejected();
    }
    if (!("jira".equals(provider) || "youtrack".equals(provider) || "confluence".equals(provider))) {
      return ConnectorRecordIdentity.unsupported();
    }
    SourceType sourceType = sourceType(provider, recordType);
    if (sourceType == null) {
      return ConnectorRecordIdentity.unsupported();
    }
    if (host == null) {
      return ConnectorRecordIdentity.rejected();
    }
    if (authorityConfusing(provider, recordType, record)) {
      return ConnectorRecordIdentity.authorityConfusingRecord();
    }
    return switch (sourceType.sourceType()) {
      case SOURCE_TYPE_JIRA_ISSUE -> jiraIssueIdentity(provider, host, sourceType, record);
      case SOURCE_TYPE_YOUTRACK_ISSUE -> youTrackIssueIdentity(provider, host, sourceType, record);
      case SOURCE_TYPE_YOUTRACK_ARTICLE -> youTrackArticleIdentity(provider, host, sourceType, record);
      case SOURCE_TYPE_CONFLUENCE_PAGE -> confluencePageIdentity(provider, host, sourceType, record);
      default -> ConnectorRecordIdentity.unsupported();
    };
  }

  private ConnectorRecordIdentity jiraIssueIdentity(
      String provider,
      String host,
      SourceType sourceType,
      JsonNode record) {
    String projectKey = normalizedIdentityPart(text(record.path("project_key")));
    String issueKey = normalizedIdentityPart(text(record.path("issue_key")));
    String issueId = optionalIdentityPart(record.path("issue_id"), true);
    if (projectKey == null || issueKey == null) {
      return ConnectorRecordIdentity.rejected();
    }
    return acceptedIdentity(
        provider,
        host,
        sourceType,
        "project",
        projectKey,
        issueKey,
        issueId);
  }

  private ConnectorRecordIdentity youTrackIssueIdentity(
      String provider,
      String host,
      SourceType sourceType,
      JsonNode record) {
    String projectKey = normalizedIdentityPart(text(record.path("project_key")));
    String issueKey = optionalIdentityPart(record.path("issue_key"), false);
    String issueId = optionalIdentityPart(record.path("issue_id"), true);
    String recordKey = issueKey == null ? issueId : issueKey;
    String recordId = issueKey == null ? null : issueId;
    if (projectKey == null || recordKey == null) {
      return ConnectorRecordIdentity.rejected();
    }
    return acceptedIdentity(
        provider,
        host,
        sourceType,
        "project",
        projectKey,
        recordKey,
        recordId);
  }

  private ConnectorRecordIdentity youTrackArticleIdentity(
      String provider,
      String host,
      SourceType sourceType,
      JsonNode record) {
    boolean hasProjectKey = present(record, "project_key");
    boolean hasCollectionKey = present(record, "collection_key");
    if (hasProjectKey && hasCollectionKey) {
      return ConnectorRecordIdentity.ambiguousRecord();
    }
    String containerType = hasCollectionKey ? "collection" : "project";
    String containerKey = normalizedIdentityPart(text(record.path(
        hasCollectionKey ? "collection_key" : "project_key")));
    String articleId = optionalIdentityPart(record.path("article_id"), true);
    if (containerKey == null || articleId == null) {
      return ConnectorRecordIdentity.rejected();
    }
    return acceptedIdentity(
        provider,
        host,
        sourceType,
        containerType,
        containerKey,
        articleId,
        null);
  }

  private ConnectorRecordIdentity confluencePageIdentity(
      String provider,
      String host,
      SourceType sourceType,
      JsonNode record) {
    String spaceKey = normalizedIdentityPart(text(record.path("space_key")));
    String pageId = optionalIdentityPart(record.path("page_id"), true);
    if (spaceKey == null || pageId == null) {
      return ConnectorRecordIdentity.rejected();
    }
    return acceptedIdentity(
        provider,
        host,
        sourceType,
        "space",
        spaceKey,
        pageId,
        null);
  }

  private ConnectorRecordIdentity acceptedIdentity(
      String provider,
      String host,
      SourceType sourceType,
      String containerType,
      String containerKey,
      String recordKey,
      String recordId) {
    String sourceIdentity = "connector/%s/%s/%s/%s/%s/%s".formatted(
        provider,
        host,
        containerType,
        containerKey,
        sourceType.identityRecordType(),
        recordKey);
    return ConnectorRecordIdentity.accepted(
        provider,
        host,
        sourceType.sourceFamily(),
        containerType,
        containerKey,
        sourceType.identityRecordType(),
        recordKey,
        recordId,
        sourceType.sourceType(),
        sourceIdentity);
  }

  private SourceType sourceType(String provider, String recordType) {
    if ("jira".equals(provider) && "issue".equals(recordType)) {
      return new SourceType(SOURCE_TYPE_JIRA_ISSUE, "issue_tracker", "issue");
    }
    if ("youtrack".equals(provider) && "issue".equals(recordType)) {
      return new SourceType(SOURCE_TYPE_YOUTRACK_ISSUE, "issue_tracker", "issue");
    }
    if ("youtrack".equals(provider) && "article".equals(recordType)) {
      return new SourceType(SOURCE_TYPE_YOUTRACK_ARTICLE, "wiki", "article");
    }
    if ("confluence".equals(provider) && "page".equals(recordType)) {
      return new SourceType(SOURCE_TYPE_CONFLUENCE_PAGE, "wiki", "page");
    }
    return null;
  }

  private boolean authorityConfusing(String provider, String recordType, JsonNode record) {
    if ("jira".equals(provider)) {
      return present(record, "space_key")
          || present(record, "collection_key")
          || present(record, "article_id")
          || present(record, "page_id");
    }
    if ("youtrack".equals(provider) && "issue".equals(recordType)) {
      return present(record, "space_key")
          || present(record, "collection_key")
          || present(record, "article_id")
          || present(record, "page_id");
    }
    if ("youtrack".equals(provider) && "article".equals(recordType)) {
      return present(record, "space_key")
          || present(record, "issue_key")
          || present(record, "issue_id")
          || present(record, "page_id");
    }
    if ("confluence".equals(provider)) {
      return present(record, "project_key")
          || present(record, "collection_key")
          || present(record, "issue_key")
          || present(record, "issue_id")
          || present(record, "article_id");
    }
    return false;
  }

  private boolean present(JsonNode record, String fieldName) {
    JsonNode field = record.path(fieldName);
    return !field.isMissingNode() && !field.isNull();
  }

  private boolean validScalarFields(JsonNode record) {
    if (!textualFieldsIfPresent(
        record,
        "provider",
        "host",
        "record_type",
        "project_key",
        "space_key",
        "collection_key",
        "issue_key",
        "title",
        "body",
        "status",
        "record_state",
        "source_url",
        "exported_at",
        "record_updated_at")) {
      return false;
    }
    return textOrIntegerFieldsIfPresent(record, "issue_id", "article_id", "page_id");
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

  private String normalizedIdentityPart(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    String trimmed = value.trim();
    if (trimmed.length() > MAX_IDENTITY_PART_LENGTH
        || sensitiveSegment(trimmed)
        || !IDENTITY_PART.matcher(trimmed).matches()) {
      return null;
    }
    return trimmed;
  }

  private String optionalIdentityPart(JsonNode node, boolean allowInteger) {
    if (node == null || node.isMissingNode() || node.isNull()) {
      return null;
    }
    String value;
    if (node.isTextual()) {
      value = node.asText().trim();
    } else if (allowInteger && node.isIntegralNumber()) {
      value = node.asText();
    } else {
      return null;
    }
    return normalizedIdentityPart(value);
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

  private String optionalSourceUrl(JsonNode node, ConnectorRecordIdentity identity) {
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
          || rawPath.contains("%")
          || rawPath.toLowerCase(Locale.ROOT).contains("%2f")
          || rawPath.toLowerCase(Locale.ROOT).contains("%2e")
          || !SAFE_URL_PATH.matcher(rawPath).matches()
          || !safeUrlPathSegments(rawPath)) {
        return null;
      }
      if (!rawPath.equals(expectedSourceUrlPath(identity))) {
        return null;
      }
      return uri.toASCIIString();
    } catch (URISyntaxException exception) {
      return null;
    }
  }

  private String expectedSourceUrlPath(ConnectorRecordIdentity identity) {
    return switch (identity.sourceType()) {
      case SOURCE_TYPE_JIRA_ISSUE -> "/browse/" + identity.recordKey();
      case SOURCE_TYPE_YOUTRACK_ISSUE -> "/issue/" + identity.recordKey();
      case SOURCE_TYPE_YOUTRACK_ARTICLE -> "/articles/" + identity.recordKey();
      case SOURCE_TYPE_CONFLUENCE_PAGE -> "/spaces/" + identity.containerKey() + "/pages/"
          + identity.recordKey();
      default -> "";
    };
  }

  private boolean safeUrlPathSegments(String rawPath) {
    String[] segments = rawPath.split("/", -1);
    for (String segment : segments) {
      if (segment.isBlank()) {
        continue;
      }
      if (".".equals(segment) || "..".equals(segment) || sensitiveSegment(segment)) {
        return false;
      }
    }
    return true;
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

  private boolean textOrIntegerFieldsIfPresent(JsonNode node, String... fieldNames) {
    for (String fieldName : fieldNames) {
      JsonNode field = node.path(fieldName);
      if (!field.isMissingNode()
          && !field.isNull()
          && !field.isTextual()
          && !field.isIntegralNumber()) {
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
      case "stale" -> "Connector import record rejected because it is marked stale.";
      case "partial" -> "Connector import record rejected because it is marked partial.";
      case "ambiguous" -> "Connector import record rejected because it is marked ambiguous.";
      default -> "Connector import record rejected because status is unsupported.";
    };
  }

  private AdapterDiagnostic diagnostic(String signal, Integer ordinal, String message) {
    String scope = ordinal == null ? "file" : "record:%06d".formatted(ordinal);
    return new AdapterDiagnostic(
        "adapter-diagnostic:connector-import:" + scope + ":" + signal,
        "warning",
        "connector_import",
        signal,
        message,
        ordinal);
  }

  private record AcceptedRecord(SourceDocument document, SourceProvenance provenance) {
  }

  private record SourceType(String sourceType, String sourceFamily, String identityRecordType) {
  }

  private record ConnectorRecordIdentity(
      boolean accepted,
      boolean ambiguous,
      boolean authorityConfusing,
      boolean unsupportedSourceType,
      String provider,
      String host,
      String sourceFamily,
      String containerType,
      String containerKey,
      String recordType,
      String recordKey,
      String recordId,
      String sourceType,
      String sourceIdentity) {
    static ConnectorRecordIdentity accepted(
        String provider,
        String host,
        String sourceFamily,
        String containerType,
        String containerKey,
        String recordType,
        String recordKey,
        String recordId,
        String sourceType,
        String sourceIdentity) {
      return new ConnectorRecordIdentity(
          true,
          false,
          false,
          false,
          provider,
          host,
          sourceFamily,
          containerType,
          containerKey,
          recordType,
          recordKey,
          recordId,
          sourceType,
          sourceIdentity);
    }

    static ConnectorRecordIdentity rejected() {
      return new ConnectorRecordIdentity(
          false, false, false, false, null, null, null, null, null, null, null, null, null, null);
    }

    static ConnectorRecordIdentity ambiguousRecord() {
      return new ConnectorRecordIdentity(
          false, true, false, false, null, null, null, null, null, null, null, null, null, null);
    }

    static ConnectorRecordIdentity authorityConfusingRecord() {
      return new ConnectorRecordIdentity(
          false, false, true, false, null, null, null, null, null, null, null, null, null, null);
    }

    static ConnectorRecordIdentity unsupported() {
      return new ConnectorRecordIdentity(
          false, false, false, true, null, null, null, null, null, null, null, null, null, null);
    }
  }
}
