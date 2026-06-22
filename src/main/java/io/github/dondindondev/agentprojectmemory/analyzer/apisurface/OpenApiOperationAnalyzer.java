package io.github.dondindondev.agentprojectmemory.analyzer.apisurface;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.dondindondev.agentprojectmemory.OutputRedactor;
import io.github.dondindondev.agentprojectmemory.analyzer.EvidenceExcerpts;
import io.github.dondindondev.agentprojectmemory.analyzer.ScanPathContainment;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

public final class OpenApiOperationAnalyzer {
  private static final String ANALYSIS_STATUS_ANALYZED = "analyzed";
  private static final String ANALYSIS_STATUS_NOT_DETECTED = "not_detected";
  private static final String API_SPEC_SOURCE_TYPE = "api_spec";
  private static final String HIGH_CONFIDENCE = "high";
  private static final String API_SURFACE_CATEGORY = "openapi_declared_operation";
  private static final String IMPLEMENTATION_STATUS_NOT_ANALYZED = "not_analyzed";
  private static final String WARNING_CATEGORY_HIDDEN_HTTP_SURFACE = "hidden_http_surface";
  private static final String WARNING_SIGNAL_PARSE_ERROR = "openapi_spec_parse_error";
  private static final String WARNING_SIGNAL_UNSUPPORTED = "openapi_spec_unsupported";
  private static final String WARNING_SIGNAL_DUPLICATE_OPERATION = "openapi_spec_duplicate_operation";
  private static final String WARNING_SIGNAL_OPERATION_COUNT_CAP =
      "openapi_operation_count_cap_reached";
  private static final int MAX_SPEC_BYTES = 1024 * 1024;
  private static final int MAX_NESTING_DEPTH = 80;
  private static final int MAX_OPERATION_ID_LENGTH = 160;
  private static final int MAX_TAG_LENGTH = 120;
  private static final int MAX_TAGS = 8;
  private static final int MAX_JSON_STRING_LENGTH = 64 * 1024;
  private static final int MAX_OPERATION_FACTS = 4096;
  private static final Set<String> SUPPORTED_HTTP_METHODS = Set.of(
      "get",
      "put",
      "post",
      "delete",
      "options",
      "head",
      "patch",
      "trace");
  private static final Comparator<OpenApiOperationFact> OPERATION_ORDER = Comparator
      .comparingInt(OpenApiOperationFact::moduleOrder)
      .thenComparing(OpenApiOperationFact::specPath)
      .thenComparing(OpenApiOperationFact::path)
      .thenComparing(OpenApiOperationFact::httpMethod)
      .thenComparing(
          OpenApiOperationFact::operationId,
          Comparator.nullsLast(Comparator.naturalOrder()))
      .thenComparing(OpenApiOperationFact::id);
  private static final Comparator<OpenApiSpecWarningFact> WARNING_ORDER = Comparator
      .comparing(OpenApiSpecWarningFact::category)
      .thenComparing(OpenApiSpecWarningFact::signal)
      .thenComparingInt(OpenApiSpecWarningFact::moduleOrder)
      .thenComparing(OpenApiSpecWarningFact::sourcePath)
      .thenComparing(OpenApiSpecWarningFact::id);

  private final ObjectMapper jsonMapper;
  private final int maxOperationFacts;

  public OpenApiOperationAnalyzer() {
    this(MAX_OPERATION_FACTS);
  }

  OpenApiOperationAnalyzer(int maxOperationFacts) {
    if (maxOperationFacts < 0) {
      throw new IllegalArgumentException("maxOperationFacts must not be negative.");
    }
    JsonFactory jsonFactory = JsonFactory.builder()
        .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
        .streamReadConstraints(StreamReadConstraints.builder()
            .maxNestingDepth(MAX_NESTING_DEPTH)
            .maxStringLength(MAX_JSON_STRING_LENGTH)
            .build())
        .build();
    this.jsonMapper = new ObjectMapper(jsonFactory);
    this.maxOperationFacts = maxOperationFacts;
  }

  public OpenApiOperationAnalysis analyze(
      Path repositoryRoot,
      List<OpenApiSpecFileFact> specFiles) throws IOException {
    Objects.requireNonNull(repositoryRoot, "repositoryRoot");
    Objects.requireNonNull(specFiles, "specFiles");

    if (specFiles.isEmpty()) {
      return new OpenApiOperationAnalysis(
          ANALYSIS_STATUS_NOT_DETECTED,
          List.of(),
          List.of(),
          List.of());
    }

    Path normalizedRepositoryRoot = repositoryRoot.toAbsolutePath().normalize();
    Path canonicalRepositoryRoot = ScanPathContainment.canonicalRoot(normalizedRepositoryRoot);
    List<OpenApiOperationFact> operations = new ArrayList<>();
    List<ApiSpecEvidence> evidence = new ArrayList<>();
    List<OpenApiSpecWarningFact> warnings = new ArrayList<>();
    boolean operationCapReached = false;

    for (OpenApiSpecFileFact specFile : specFiles) {
      if (operations.size() >= maxOperationFacts) {
        if (!operationCapReached) {
          addWarning(
              specFile,
              WARNING_SIGNAL_OPERATION_COUNT_CAP,
              "Additional OpenAPI/Swagger operations were skipped because the analyzer reached "
                  + "the bounded operation fact limit.",
              new LinkedHashSet<>(),
              evidence,
              warnings);
          operationCapReached = true;
        }
        break;
      }
      parseSpec(
          normalizedRepositoryRoot,
          canonicalRepositoryRoot,
          specFile,
          operations,
          evidence,
          warnings);
    }

    return new OpenApiOperationAnalysis(
        ANALYSIS_STATUS_ANALYZED,
        operations.stream().sorted(OPERATION_ORDER).toList(),
        evidence,
        warnings.stream().sorted(WARNING_ORDER).toList());
  }

  private void parseSpec(
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      OpenApiSpecFileFact specFile,
      List<OpenApiOperationFact> operations,
      List<ApiSpecEvidence> evidence,
      List<OpenApiSpecWarningFact> warnings) {
    Set<String> emittedWarningSignals = new LinkedHashSet<>();
    Path localSpecPath = repositoryRoot.resolve(specFile.specPath()).normalize();
    if (!ScanPathContainment.isRegularFileUnderRootNoFollow(canonicalRepositoryRoot, localSpecPath)) {
      addWarning(
          specFile,
          WARNING_SIGNAL_UNSUPPORTED,
          "OpenAPI/Swagger spec file is no longer a regular local file under the scan root; "
              + "no operation facts were emitted for this spec.",
          emittedWarningSignals,
          evidence,
          warnings);
      return;
    }

    String content;
    try {
      content = readBoundedSpecContent(localSpecPath);
    } catch (OversizedSpecException exception) {
      addWarning(
          specFile,
          WARNING_SIGNAL_UNSUPPORTED,
          "OpenAPI/Swagger spec file exceeds the bounded parser input size; no operation facts "
              + "were emitted for this spec.",
          emittedWarningSignals,
          evidence,
          warnings);
      return;
    } catch (IOException exception) {
      addWarning(
          specFile,
          WARNING_SIGNAL_PARSE_ERROR,
          "OpenAPI/Swagger spec file could not be read safely; no operation facts were emitted "
              + "for this spec.",
          emittedWarningSignals,
          evidence,
          warnings);
      return;
    }

    List<String> sourceLines = List.of(content.split("\\R", -1));
    List<OperationCandidate> candidates;
    try {
      candidates = switch (specFile.format()) {
        case "json" -> jsonCandidates(specFile, content, sourceLines);
        case "yaml" -> yamlCandidates(specFile, content, sourceLines);
        default -> throw new UnsupportedSpecException();
      };
    } catch (UnsupportedSpecException exception) {
      addWarning(
          specFile,
          WARNING_SIGNAL_UNSUPPORTED,
          "OpenAPI/Swagger spec file is unsupported by the minimal operation parser; no "
              + "operation facts were emitted for this spec.",
          emittedWarningSignals,
          evidence,
          warnings);
      return;
    } catch (IOException | RuntimeException exception) {
      addWarning(
          specFile,
          WARNING_SIGNAL_PARSE_ERROR,
          "OpenAPI/Swagger spec file could not be parsed safely; no operation facts were emitted "
              + "for this spec.",
          emittedWarningSignals,
          evidence,
          warnings);
      return;
    }

    Map<String, List<OperationCandidate>> candidatesByKey = new LinkedHashMap<>();
    for (OperationCandidate candidate : candidates) {
      candidatesByKey.computeIfAbsent(candidate.operationKey(), ignored -> new ArrayList<>())
          .add(candidate);
    }

    for (List<OperationCandidate> groupedCandidates : candidatesByKey.values()) {
      if (groupedCandidates.size() > 1) {
        addWarning(
            specFile,
            WARNING_SIGNAL_DUPLICATE_OPERATION,
            "OpenAPI/Swagger spec declares duplicate operations for the same normalized spec "
                + "path, HTTP method, and operation path; colliding operation facts were not "
                + "emitted.",
            emittedWarningSignals,
            evidence,
            warnings);
        continue;
      }
      if (operations.size() >= maxOperationFacts) {
        addWarning(
            specFile,
            WARNING_SIGNAL_OPERATION_COUNT_CAP,
            "Additional OpenAPI/Swagger operations from this spec were skipped because the "
                + "analyzer reached the bounded operation fact limit.",
            emittedWarningSignals,
            evidence,
            warnings);
        break;
      }
      addOperation(groupedCandidates.get(0), operations, evidence);
    }
  }

  private String readBoundedSpecContent(Path localSpecPath) throws IOException {
    byte[] bytes;
    try {
      bytes = ScanPathContainment.readRegularFileBytesNoFollowStable(localSpecPath, MAX_SPEC_BYTES);
    } catch (ScanPathContainment.FileSizeLimitExceededException exception) {
      throw new OversizedSpecException();
    }
    return new String(bytes, StandardCharsets.UTF_8);
  }

  private List<OperationCandidate> jsonCandidates(
      OpenApiSpecFileFact specFile,
      String content,
      List<String> sourceLines) throws IOException {
    JsonNode root = jsonMapper.readTree(content);
    if (!root.isObject()) {
      throw new UnsupportedSpecException();
    }

    JsonNode paths = root.path("paths");
    if (paths.isMissingNode() || paths.isNull()) {
      return List.of();
    }
    if (!paths.isObject()) {
      throw new UnsupportedSpecException();
    }

    List<OperationCandidate> candidates = new ArrayList<>();
    paths.fields().forEachRemaining(pathEntry -> {
      String operationPath = pathEntry.getKey();
      JsonNode pathItem = pathEntry.getValue();
      if (!operationPath.startsWith("/") || !pathItem.isObject()) {
        return;
      }
      pathItem.fields().forEachRemaining(operationEntry -> {
        String methodKey = operationEntry.getKey().toLowerCase(Locale.ROOT);
        if (!SUPPORTED_HTTP_METHODS.contains(methodKey)) {
          return;
        }
        JsonNode operation = operationEntry.getValue();
        if (!operation.isObject()) {
          throw new UnsupportedSpecException();
        }
        candidates.add(operationCandidate(
            specFile,
            operationPath,
            methodKey,
            operationId(operation.path("operationId")),
            tags(operation.path("tags")),
            jsonOperationLine(sourceLines, operationPath, methodKey)));
      });
    });
    return candidates;
  }

  private List<OperationCandidate> yamlCandidates(
      OpenApiSpecFileFact specFile,
      String content,
      List<String> sourceLines) {
    LoaderOptions loaderOptions = new LoaderOptions();
    loaderOptions.setAllowDuplicateKeys(false);
    loaderOptions.setMaxAliasesForCollections(0);
    loaderOptions.setNestingDepthLimit(MAX_NESTING_DEPTH);
    loaderOptions.setCodePointLimit(MAX_SPEC_BYTES);
    Yaml yaml = new Yaml(new SafeConstructor(loaderOptions));
    List<Object> documents = new ArrayList<>();
    for (Object document : yaml.loadAll(content)) {
      if (document != null) {
        documents.add(document);
      }
      if (documents.size() > 1) {
        throw new UnsupportedSpecException();
      }
    }
    if (documents.isEmpty()) {
      return List.of();
    }
    Object root = documents.get(0);
    if (!(root instanceof Map<?, ?> rootMap)) {
      throw new UnsupportedSpecException();
    }
    Object pathsObject = rootMap.get("paths");
    if (pathsObject == null) {
      return List.of();
    }
    if (!(pathsObject instanceof Map<?, ?> pathsMap)) {
      throw new UnsupportedSpecException();
    }

    List<OperationCandidate> candidates = new ArrayList<>();
    for (Map.Entry<?, ?> pathEntry : pathsMap.entrySet()) {
      if (!(pathEntry.getKey() instanceof String operationPath)
          || !operationPath.startsWith("/")) {
        continue;
      }
      if (!(pathEntry.getValue() instanceof Map<?, ?> pathItem)) {
        throw new UnsupportedSpecException();
      }
      for (Map.Entry<?, ?> operationEntry : pathItem.entrySet()) {
        if (!(operationEntry.getKey() instanceof String method)) {
          continue;
        }
        String methodKey = method.toLowerCase(Locale.ROOT);
        if (!SUPPORTED_HTTP_METHODS.contains(methodKey)) {
          continue;
        }
        if (!(operationEntry.getValue() instanceof Map<?, ?> operation)) {
          throw new UnsupportedSpecException();
        }
        candidates.add(operationCandidate(
            specFile,
            operationPath,
            methodKey,
            operationId(operation.get("operationId")),
            tags(operation.get("tags")),
            yamlOperationLine(sourceLines, operationPath, methodKey)));
      }
    }
    return candidates;
  }

  private OperationCandidate operationCandidate(
      OpenApiSpecFileFact specFile,
      String operationPath,
      String methodKey,
      String operationId,
      List<String> tags,
      Integer lineNumber) {
    return new OperationCandidate(
        specFile,
        operationPath,
        methodKey,
        methodKey.toUpperCase(Locale.ROOT),
        operationId,
        tags,
        lineNumber,
        lineNumber);
  }

  private String operationId(JsonNode operationId) {
    if (!operationId.isTextual()) {
      return null;
    }
    return boundedOperationId(operationId.asText());
  }

  private String operationId(Object operationId) {
    if (!(operationId instanceof String value)) {
      return null;
    }
    return boundedOperationId(value);
  }

  private String boundedOperationId(String value) {
    if (value.isBlank() || value.length() > MAX_OPERATION_ID_LENGTH) {
      return null;
    }
    return OutputRedactor.redactField("operation_id", value);
  }

  private List<String> tags(JsonNode tags) {
    if (!tags.isArray()) {
      return List.of();
    }
    List<String> values = new ArrayList<>();
    for (JsonNode tag : tags) {
      if (!tag.isTextual()) {
        continue;
      }
      addBoundedTag(values, tag.asText());
    }
    return List.copyOf(values);
  }

  private List<String> tags(Object tags) {
    if (!(tags instanceof List<?> tagList)) {
      return List.of();
    }
    List<String> values = new ArrayList<>();
    for (Object tag : tagList) {
      if (!(tag instanceof String value)) {
        continue;
      }
      addBoundedTag(values, value);
    }
    return List.copyOf(values);
  }

  private void addBoundedTag(List<String> values, String value) {
    if (values.size() >= MAX_TAGS || value.isBlank() || value.length() > MAX_TAG_LENGTH) {
      return;
    }
    values.add(value);
  }

  private void addOperation(
      OperationCandidate candidate,
      List<OpenApiOperationFact> operations,
      List<ApiSpecEvidence> evidence) {
    String symbolName = "operation:" + candidate.methodKey() + ":" + candidate.path();
    String evidenceId = evidenceId(
        candidate.specFile().specPath(),
        candidate.lineStart(),
        candidate.lineEnd(),
        symbolName);
    ApiSpecEvidence operationEvidence = new ApiSpecEvidence(
        evidenceId,
        API_SPEC_SOURCE_TYPE,
        candidate.specFile().specPath(),
        null,
        null,
        symbolName,
        candidate.lineStart(),
        candidate.lineEnd(),
        operationExcerpt(candidate),
        HIGH_CONFIDENCE);
    evidence.add(operationEvidence);
    operations.add(new OpenApiOperationFact(
        operationId(candidate.specFile().moduleId(), candidate.specFile().specPath(), candidate.methodKey(), candidate.path()),
        candidate.specFile().moduleId(),
        candidate.specFile().moduleOrder(),
        API_SURFACE_CATEGORY,
        candidate.specFile().specPath(),
        candidate.displayMethod(),
        candidate.path(),
        candidate.operationId(),
        candidate.tags(),
        IMPLEMENTATION_STATUS_NOT_ANALYZED,
        List.of(operationEvidence.id())));
  }

  private String operationExcerpt(OperationCandidate candidate) {
    StringBuilder excerpt = new StringBuilder();
    excerpt.append("operation ")
        .append(candidate.methodKey())
        .append(" ")
        .append(candidate.path());
    if (candidate.operationId() != null) {
      excerpt.append("; operationId ").append(candidate.operationId());
    }
    if (!candidate.tags().isEmpty()) {
      excerpt.append("; tags ").append(String.join(",", candidate.tags()));
    }
    return EvidenceExcerpts.bounded(excerpt.toString());
  }

  private void addWarning(
      OpenApiSpecFileFact specFile,
      String signal,
      String message,
      Set<String> emittedWarningSignals,
      List<ApiSpecEvidence> evidence,
      List<OpenApiSpecWarningFact> warnings) {
    if (!emittedWarningSignals.add(signal)) {
      return;
    }
    String symbolName = "operation_parse_status:" + signal;
    ApiSpecEvidence warningEvidence = new ApiSpecEvidence(
        evidenceId(specFile.specPath(), null, null, symbolName),
        API_SPEC_SOURCE_TYPE,
        specFile.specPath(),
        null,
        null,
        symbolName,
        null,
        null,
        EvidenceExcerpts.bounded("operation parser warning: " + signal),
        HIGH_CONFIDENCE);
    evidence.add(warningEvidence);
    warnings.add(new OpenApiSpecWarningFact(
        warningId(signal, specFile.specPath()),
        WARNING_CATEGORY_HIDDEN_HTTP_SURFACE,
        signal,
        specFile.moduleId(),
        specFile.moduleOrder(),
        message,
        specFile.specPath(),
        List.of(warningEvidence.id())));
  }

  private String operationId(String moduleId, String specPath, String methodKey, String path) {
    String scope = moduleId == null || moduleId.isBlank() ? "unscoped" : moduleId;
    return "openapi_operation:"
        + scope
        + ":spec:"
        + OpenApiSpecDiscoveryAnalyzer.idKey(specPath)
        + ":operation:"
        + methodKey
        + ":"
        + OpenApiSpecDiscoveryAnalyzer.idKey(path);
  }

  private String evidenceId(
      String specPath,
      Integer lineStart,
      Integer lineEnd,
      String symbolName) {
    String lineRange = lineStart == null || lineEnd == null ? "unknown" : lineStart + "-" + lineEnd;
    return "ev:"
        + OpenApiSpecDiscoveryAnalyzer.idKey(specPath)
        + ":"
        + lineRange
        + ":api_spec:"
        + OpenApiSpecDiscoveryAnalyzer.idKey(symbolName);
  }

  private String warningId(String signal, String specPath) {
    return "warning:"
        + WARNING_CATEGORY_HIDDEN_HTTP_SURFACE
        + ":"
        + signal
        + ":"
        + OpenApiSpecDiscoveryAnalyzer.idKey(specPath);
  }

  private Integer yamlOperationLine(List<String> sourceLines, String path, String methodKey) {
    Integer pathsIndent = null;
    Integer pathIndent = null;
    for (int index = 0; index < sourceLines.size(); index++) {
      Optional<YamlKey> key = yamlKey(sourceLines.get(index));
      if (key.isEmpty()) {
        continue;
      }
      YamlKey yamlKey = key.orElseThrow();
      if (pathsIndent == null) {
        if ("paths".equals(yamlKey.key())) {
          pathsIndent = yamlKey.indent();
        }
        continue;
      }
      if (yamlKey.indent() <= pathsIndent) {
        pathIndent = null;
        if (!"paths".equals(yamlKey.key())) {
          pathsIndent = null;
        }
        continue;
      }
      if (yamlKey.indent() > pathsIndent && path.equals(yamlKey.key())) {
        pathIndent = yamlKey.indent();
        continue;
      }
      if (pathIndent != null && yamlKey.indent() <= pathIndent) {
        pathIndent = null;
      }
      if (pathIndent != null
          && yamlKey.indent() > pathIndent
          && methodKey.equalsIgnoreCase(yamlKey.key())) {
        return index + 1;
      }
    }
    return null;
  }

  private Optional<YamlKey> yamlKey(String line) {
    if (line.isBlank()) {
      return Optional.empty();
    }
    int indent = 0;
    while (indent < line.length() && line.charAt(indent) == ' ') {
      indent++;
    }
    String trimmed = line.substring(indent).trim();
    if (trimmed.isBlank() || trimmed.startsWith("#") || trimmed.startsWith("-")) {
      return Optional.empty();
    }
    int colon = keyColon(trimmed);
    if (colon <= 0) {
      return Optional.empty();
    }
    String key = unquote(trimmed.substring(0, colon).trim());
    if (key.isBlank()) {
      return Optional.empty();
    }
    return Optional.of(new YamlKey(indent, key));
  }

  private int keyColon(String value) {
    char quote = 0;
    boolean escaped = false;
    for (int index = 0; index < value.length(); index++) {
      char current = value.charAt(index);
      if (escaped) {
        escaped = false;
        continue;
      }
      if (current == '\\') {
        escaped = true;
        continue;
      }
      if (quote != 0) {
        if (current == quote) {
          quote = 0;
        }
        continue;
      }
      if (current == '"' || current == '\'') {
        quote = current;
        continue;
      }
      if (current == ':') {
        return index;
      }
    }
    return -1;
  }

  private String unquote(String value) {
    if (value.length() < 2) {
      return value;
    }
    char first = value.charAt(0);
    char last = value.charAt(value.length() - 1);
    if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
      return value.substring(1, value.length() - 1);
    }
    return value;
  }

  private Integer jsonOperationLine(List<String> sourceLines, String path, String methodKey) {
    String pathNeedle = "\"" + path.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    String methodNeedle = "\"" + methodKey + "\"";
    String upperMethodNeedle = "\"" + methodKey.toUpperCase(Locale.ROOT) + "\"";
    boolean sawPath = false;
    for (int index = 0; index < sourceLines.size(); index++) {
      String line = sourceLines.get(index);
      if (!sawPath && line.contains(pathNeedle)) {
        sawPath = true;
        continue;
      }
      if (sawPath && (line.contains(methodNeedle) || line.contains(upperMethodNeedle))) {
        return index + 1;
      }
    }
    return null;
  }

  private record OperationCandidate(
      OpenApiSpecFileFact specFile,
      String path,
      String methodKey,
      String displayMethod,
      String operationId,
      List<String> tags,
      Integer lineStart,
      Integer lineEnd) {
    private OperationCandidate {
      tags = List.copyOf(tags);
    }

    private String operationKey() {
      return specFile.specPath() + "\n" + methodKey + "\n" + path;
    }
  }

  private record YamlKey(int indent, String key) {
  }

  private static final class UnsupportedSpecException extends RuntimeException {
  }

  private static final class OversizedSpecException extends IOException {
  }
}
