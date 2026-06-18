package io.github.dondindondev.agentprojectmemory.ingestion.adapter;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.dondindondev.agentprojectmemory.OutputRedactor;
import io.github.dondindondev.agentprojectmemory.analyzer.ScanPathContainment;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public final class LocalStructuredImportAdapter {
  public static final String FORMAT = "agent-project-memory.local_structured_import.v1";
  public static final String SOURCE_TYPE_LOCAL_EXPORT = "local_export";
  public static final int MAX_IMPORT_BYTES = 256 * 1024;
  static final int MAX_RECORDS = 64;
  private static final int MAX_JSON_NESTING_DEPTH = 16;
  private static final int MAX_JSON_STRING_LENGTH = 64 * 1024;
  private static final int MAX_TITLE_LENGTH = 200;
  private static final int MAX_BODY_LENGTH = 32 * 1024;
  private static final int MAX_SOURCE_IDENTITY_LENGTH = 200;
  private static final Pattern DRIVE_LETTER_PATH = Pattern.compile("^[A-Za-z]:.*");
  private static final Set<String> LOCAL_PATH_ROOT_SEGMENTS = Set.of(
      "applications",
      "bin",
      "dev",
      "etc",
      "home",
      "library",
      "media",
      "mnt",
      "opt",
      "private",
      "sbin",
      "system",
      "tmp",
      "users",
      "usr",
      "var",
      "volumes",
      "windows");
  private static final Set<String> SENSITIVE_SOURCE_IDENTITY_SEGMENTS = Set.of(
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
      new AdapterIdentity(AdapterLocalImport.LOCAL_STRUCTURED_IMPORT_ADAPTER, "2.0.0");
  private static final Set<String> ROOT_FIELDS = Set.of("format", "records");
  private static final Set<String> RECORD_FIELDS = Set.of(
      "source_type",
      "source_identity",
      "title",
      "body",
      "status");
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
    if (!AdapterLocalImport.LOCAL_STRUCTURED_IMPORT_ADAPTER.equals(localImport.adapterName())) {
      throw new IOException("Adapter import selection is unsupported.");
    }
    if (localImport.importMode() != AdapterImportMode.LOCAL_EXPORT) {
      throw new IOException("Adapter import mode is unsupported.");
    }

    byte[] bytes = readImportBytes(repositoryRoot, localImport);
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
          "Local structured import record cap reached; additional records were omitted."));
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

  private byte[] readImportBytes(Path repositoryRoot, AdapterLocalImport localImport)
      throws IOException {
    Path normalizedRoot = repositoryRoot.toAbsolutePath().normalize();
    Path importPath = normalizedRoot.resolve(localImport.path()).toAbsolutePath().normalize();
    try {
      Path canonicalRoot = ScanPathContainment.canonicalRoot(normalizedRoot);
      if (!importPath.startsWith(normalizedRoot)
          || hasSymbolicLinkSegment(normalizedRoot, importPath)
          || !ScanPathContainment.isRegularFileUnderRootNoFollow(canonicalRoot, importPath)) {
        throw new IOException("Adapter import file could not be read.");
      }
      return ScanPathContainment.readRegularFileBytesNoFollowStable(importPath, MAX_IMPORT_BYTES);
    } catch (ScanPathContainment.FileSizeLimitExceededException exception) {
      throw new IOException("Adapter import file exceeds maximum supported size.");
    } catch (IOException | SecurityException exception) {
      throw new IOException("Adapter import file could not be read.");
    }
  }

  private boolean hasSymbolicLinkSegment(Path repositoryRoot, Path path) {
    if (!path.startsWith(repositoryRoot)) {
      return true;
    }
    Path current = repositoryRoot;
    for (Path part : repositoryRoot.relativize(path)) {
      current = current.resolve(part);
      if (Files.isSymbolicLink(current)) {
        return true;
      }
    }
    return false;
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
          "Local structured import record rejected because it is not an object."));
      return;
    }
    if (!hasOnlyFields(record, RECORD_FIELDS)) {
      diagnostics.add(diagnostic(
          "unsupported_record_shape_rejected",
          ordinal,
          "Local structured import record rejected because its field set is unsupported."));
      return;
    }

    String status = text(record.path("status"));
    if (!"current".equals(status)) {
      diagnostics.add(diagnostic(statusSignal(status), ordinal, statusMessage(status)));
      return;
    }

    String sourceType = text(record.path("source_type"));
    if (!SOURCE_TYPE_LOCAL_EXPORT.equals(sourceType)) {
      diagnostics.add(diagnostic(
          "unsupported_source_type_rejected",
          ordinal,
          "Local structured import record rejected because its source type is unsupported."));
      return;
    }

    String sourceIdentity = text(record.path("source_identity"));
    if (!validSourceIdentity(sourceIdentity)) {
      diagnostics.add(diagnostic(
          "provenance_missing_record_rejected",
          ordinal,
          "Local structured import record rejected because stable source identity is missing or unsafe."));
      return;
    }

    String body = text(record.path("body"));
    if (body.isBlank()) {
      diagnostics.add(diagnostic(
          "partial_record_rejected",
          ordinal,
          "Local structured import record rejected because normalized body is missing."));
      return;
    }
    if (body.length() > MAX_BODY_LENGTH) {
      diagnostics.add(diagnostic(
          "record_body_too_large_rejected",
          ordinal,
          "Local structured import record rejected because normalized body exceeds the record limit."));
      return;
    }

    String duplicateKey = sourceType + "\0" + sourceIdentity;
    if (!acceptedKeys.add(duplicateKey)) {
      diagnostics.add(diagnostic(
          "duplicate_source_identity_rejected",
          ordinal,
          "Local structured import record rejected because source identity is duplicated."));
      return;
    }

    String title = boundedTitle(text(record.path("title")));
    String contentHash = StableAdapterIds.contentHash(sourceType, sourceIdentity, title == null ? "" : title, body);
    SourceProvenance provenance = SourceProvenance.accepted(
        IDENTITY,
        AdapterImportMode.LOCAL_EXPORT,
        sourceType,
        sourceIdentity,
        contentHash,
        "repository_relative_file",
        "disabled",
        List.of(
            "local_structured_import",
            "repository_relative_file",
            "provenance_backed_external_context",
            "not_code_evidence"));
    SourceDocument document = SourceDocument.accepted(
        IDENTITY,
        AdapterImportMode.LOCAL_EXPORT,
        sourceType,
        sourceIdentity,
        title,
        contentHash,
        provenance.id());
    acceptedRecords.add(new AcceptedRecord(document, provenance));
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

  private String text(JsonNode node) {
    if (node == null || !node.isTextual()) {
      return "";
    }
    return node.asText().trim();
  }

  private static boolean validSourceIdentity(String value) {
    if (value == null || value.isBlank()) {
      return false;
    }
    String trimmed = value.trim();
    String lowerCase = trimmed.toLowerCase(Locale.ROOT);
    if (trimmed.length() > MAX_SOURCE_IDENTITY_LENGTH
        || trimmed.startsWith("/")
        || trimmed.startsWith("./")
        || trimmed.startsWith("~")
        || trimmed.contains("\\")
        || trimmed.contains("://")
        || trimmed.startsWith("//")
        || lowerCase.startsWith("file:")
        || DRIVE_LETTER_PATH.matcher(trimmed).matches()) {
      return false;
    }

    String[] segments = trimmed.split("/", -1);
    if (segments.length == 0 || localPathRootSegment(segments[0])) {
      return false;
    }
    for (String segment : segments) {
      if (segment.isBlank() || ".".equals(segment) || "..".equals(segment)) {
        return false;
      }
      if (sensitiveSourceIdentitySegment(segment)) {
        return false;
      }
    }
    return trimmed.chars().allMatch(character ->
        (character >= 'A' && character <= 'Z')
            || (character >= 'a' && character <= 'z')
            || (character >= '0' && character <= '9')
            || character == '.'
            || character == '_'
            || character == '-'
            || character == '~'
            || character == '/'
            || character == ':'
            || character == '#');
  }

  private static boolean localPathRootSegment(String segment) {
    return LOCAL_PATH_ROOT_SEGMENTS.contains(segment.toLowerCase(Locale.ROOT));
  }

  private static boolean sensitiveSourceIdentitySegment(String segment) {
    String lowerCase = segment.toLowerCase(Locale.ROOT);
    int delimiter = firstSensitiveKeyDelimiter(lowerCase);
    String keyCandidate = delimiter >= 0 ? lowerCase.substring(0, delimiter) : lowerCase;
    String normalizedKey = keyCandidate.replace("-", "").replace("_", "");
    return SENSITIVE_SOURCE_IDENTITY_SEGMENTS.contains(normalizedKey);
  }

  private static int firstSensitiveKeyDelimiter(String value) {
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
      default -> "unsupported_status_rejected";
    };
  }

  private String statusMessage(String status) {
    return switch (status) {
      case "stale" -> "Local structured import record rejected because it is marked stale.";
      case "partial" -> "Local structured import record rejected because it is marked partial.";
      default -> "Local structured import record rejected because status is unsupported.";
    };
  }

  private AdapterDiagnostic diagnostic(String signal, Integer ordinal, String message) {
    String scope = ordinal == null ? "file" : "record:%06d".formatted(ordinal);
    return new AdapterDiagnostic(
        "adapter-diagnostic:local-structured-import:" + scope + ":" + signal,
        "warning",
        "local_structured_import",
        signal,
        message,
        ordinal);
  }

  private record AcceptedRecord(SourceDocument document, SourceProvenance provenance) {
  }
}
