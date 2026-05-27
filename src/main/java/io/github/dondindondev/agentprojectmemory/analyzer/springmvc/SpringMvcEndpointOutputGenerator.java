package io.github.dondindondev.agentprojectmemory.analyzer.springmvc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

public final class SpringMvcEndpointOutputGenerator {
  private static final String MAIN_SOURCE_ROOT = "src/main/java";
  private static final String TEST_SOURCE_ROOT = "src/test/java";
  private static final String ROOT_BUILD_FILE = "pom.xml";
  private static final String PROJECT_MAP_FILE_NAME = "project-map.json";
  private static final String ENDPOINTS_FILE_NAME = "endpoints.md";
  private static final String EVIDENCE_INDEX_FILE_NAME = "evidence-index.jsonl";
  private static final String ANNOTATION_SOURCE_TYPE = "annotation";
  private static final String BUILD_FILE_SOURCE_TYPE = "build_file";
  private static final String HIGH_CONFIDENCE = "high";
  private static final Comparator<EvidenceRecord> EVIDENCE_ORDER = Comparator
      .comparing(EvidenceRecord::path)
      .thenComparing(record -> record.lineStart() == null ? Integer.MAX_VALUE : record.lineStart())
      .thenComparing(record -> record.lineEnd() == null ? Integer.MAX_VALUE : record.lineEnd())
      .thenComparing(record -> nullSafe(record.className()))
      .thenComparing(record -> nullSafe(record.methodName()))
      .thenComparing(EvidenceRecord::symbolName)
      .thenComparing(EvidenceRecord::id);
  private static final Comparator<SpringMvcEndpointFact> ENDPOINT_ORDER = Comparator
      .comparing(SpringMvcEndpointOutputGenerator::firstPath)
      .thenComparing(endpoint -> String.join(",", endpoint.httpMethods()))
      .thenComparing(endpoint -> endpoint.httpMethodSemantics().name())
      .thenComparing(SpringMvcEndpointFact::controllerClass)
      .thenComparing(SpringMvcEndpointFact::handlerMethod);

  private final SpringMvcEndpointAnalyzer analyzer;

  public SpringMvcEndpointOutputGenerator() {
    this(new SpringMvcEndpointAnalyzer());
  }

  SpringMvcEndpointOutputGenerator(SpringMvcEndpointAnalyzer analyzer) {
    this.analyzer = Objects.requireNonNull(analyzer, "analyzer");
  }

  public Result generate(Path repositoryRoot, Path outputDirectory) throws IOException {
    Objects.requireNonNull(repositoryRoot, "repositoryRoot");
    Objects.requireNonNull(outputDirectory, "outputDirectory");

    Path normalizedRepositoryRoot = repositoryRoot.toAbsolutePath().normalize();
    Path sourceRoot = normalizedRepositoryRoot.resolve(MAIN_SOURCE_ROOT);
    if (!Files.isDirectory(sourceRoot)) {
      return new Result(false, 0, 0);
    }

    ProjectLayout layout = detectLayout(normalizedRepositoryRoot);
    SpringMvcEndpointAnalysis analysis = analyzer.analyze(
        normalizedRepositoryRoot,
        List.of(sourceRoot));
    List<EvidenceRecord> evidenceRecords = evidenceRecords(layout, analysis.evidence());

    Files.writeString(
        outputDirectory.resolve(ENDPOINTS_FILE_NAME),
        endpointsMarkdown(analysis),
        StandardCharsets.UTF_8);
    Files.writeString(
        outputDirectory.resolve(EVIDENCE_INDEX_FILE_NAME),
        evidenceIndexJsonl(evidenceRecords),
        StandardCharsets.UTF_8);
    Files.writeString(
        outputDirectory.resolve(PROJECT_MAP_FILE_NAME),
        projectMapJson(layout, analysis),
        StandardCharsets.UTF_8);

    return new Result(true, analysis.endpoints().size(), evidenceRecords.size());
  }

  private ProjectLayout detectLayout(Path repositoryRoot) throws IOException {
    Optional<EvidenceRecord> buildFileEvidence = buildFileEvidence(repositoryRoot);
    BuildMetadata build = buildFileEvidence
        .map(evidence -> new BuildMetadata("maven", ROOT_BUILD_FILE, List.of(evidence.id())))
        .orElseGet(() -> new BuildMetadata("not_detected", null, List.of()));

    List<String> sourceRoots = detectedRoots(repositoryRoot, List.of(MAIN_SOURCE_ROOT));
    List<String> testRoots = detectedRoots(repositoryRoot, List.of(TEST_SOURCE_ROOT));

    return new ProjectLayout(build, sourceRoots, testRoots, buildFileEvidence);
  }

  private Optional<EvidenceRecord> buildFileEvidence(Path repositoryRoot) throws IOException {
    Path buildFile = repositoryRoot.resolve(ROOT_BUILD_FILE);
    if (!Files.isRegularFile(buildFile)) {
      return Optional.empty();
    }

    List<String> lines = Files.readAllLines(buildFile, StandardCharsets.UTF_8);
    Integer line = lines.isEmpty() ? null : 1;
    String lineRange = line == null ? "unknown" : line + "-" + line;
    String excerpt = lines.isEmpty() ? "" : lines.get(0).trim();
    return Optional.of(new EvidenceRecord(
        "ev:" + ROOT_BUILD_FILE + ":" + lineRange + ":build_file:" + ROOT_BUILD_FILE,
        BUILD_FILE_SOURCE_TYPE,
        ROOT_BUILD_FILE,
        null,
        null,
        ROOT_BUILD_FILE,
        line,
        line,
        excerpt,
        HIGH_CONFIDENCE));
  }

  private List<String> detectedRoots(Path repositoryRoot, List<String> candidates) {
    return candidates.stream()
        .filter(candidate -> Files.isDirectory(repositoryRoot.resolve(candidate)))
        .sorted()
        .toList();
  }

  private String endpointsMarkdown(SpringMvcEndpointAnalysis analysis) {
    StringBuilder markdown = new StringBuilder();
    markdown.append("# Endpoints\n\n");

    if (analysis.endpoints().isEmpty()) {
      markdown.append("No Spring MVC endpoints detected under `")
          .append(MAIN_SOURCE_ROOT)
          .append("`.\n");
      return markdown.toString();
    }

    for (EndpointRow row : endpointRows(analysis.endpoints())) {
      SpringMvcEndpointFact endpoint = row.endpoint();
      markdown.append("## ")
          .append(row.methodLabel())
          .append(" ")
          .append(row.path())
          .append("\n\n");
      markdown.append("- Controller: ").append(code(endpoint.controllerClass())).append("\n");
      markdown.append("- Handler: ").append(code(endpoint.handlerMethod())).append("\n");
      markdown.append("- HTTP methods: ").append(httpMethods(endpoint)).append("\n");
      markdown.append("- Request parameters: ")
          .append(requestParameters(endpoint.requestParameters()))
          .append("\n");
      markdown.append("- Request body: ")
          .append(nullableCode(endpoint.requestBodyType()))
          .append("\n");
      markdown.append("- Response: ")
          .append(nullableCode(endpoint.declaredResponseType()))
          .append("\n");
      markdown.append("- Evidence: ").append(codeList(endpoint.evidenceIds())).append("\n\n");
    }

    return markdown.toString();
  }

  private String projectMapJson(ProjectLayout layout, SpringMvcEndpointAnalysis analysis) {
    StringBuilder json = new StringBuilder();
    json.append("{\n");
    appendIndentedStringField(json, 1, "schema_version", "0.1", true);
    json.append("  \"project\": {\n");
    appendIndentedStringField(json, 2, "root", ".", true);
    json.append("    \"build\": {\n");
    appendIndentedStringField(json, 3, "system", layout.build().system(), true);
    appendIndentedNullableStringField(
        json,
        3,
        "root_build_file",
        layout.build().rootBuildFile(),
        true);
    appendIndentedStringArrayField(json, 3, "evidence_ids", layout.build().evidenceIds(), false);
    json.append("    },\n");
    appendIndentedStringArrayField(json, 2, "source_roots", layout.sourceRoots(), true);
    appendIndentedStringArrayField(json, 2, "test_roots", layout.testRoots(), false);
    json.append("  },\n");
    json.append("  \"endpoints\": [");
    List<SpringMvcEndpointFact> endpoints = sortedEndpoints(analysis.endpoints());
    if (endpoints.isEmpty()) {
      json.append("],\n");
    } else {
      json.append("\n");
      for (int index = 0; index < endpoints.size(); index++) {
        appendEndpoint(json, endpoints.get(index), index < endpoints.size() - 1);
      }
      json.append("  ],\n");
    }
    json.append("  \"components\": {\n");
    appendIndentedStringField(json, 2, "analysis_status", "not_analyzed", true);
    json.append("    \"items\": []\n");
    json.append("  }\n");
    json.append("}\n");
    return json.toString();
  }

  private void appendEndpoint(
      StringBuilder json,
      SpringMvcEndpointFact endpoint,
      boolean trailingComma) {
    json.append("    {\n");
    appendIndentedStringField(json, 3, "id", endpointId(endpoint), true);
    appendIndentedStringField(json, 3, "controller_class", endpoint.controllerClass(), true);
    appendIndentedStringField(json, 3, "handler_method", endpoint.handlerMethod(), true);
    appendIndentedStringArrayField(json, 3, "http_methods", endpoint.httpMethods(), true);
    appendIndentedStringField(
        json,
        3,
        "http_method_semantics",
        endpoint.httpMethodSemantics().name().toLowerCase(Locale.ROOT),
        true);
    appendIndentedStringArrayField(json, 3, "paths", endpoint.paths(), true);
    appendRequestParameters(json, endpoint.requestParameters());
    appendIndentedNullableStringField(
        json,
        3,
        "request_body_type",
        endpoint.requestBodyType(),
        true);
    appendIndentedNullableStringField(
        json,
        3,
        "response_type",
        endpoint.declaredResponseType(),
        true);
    appendIndentedStringArrayField(json, 3, "evidence_ids", endpoint.evidenceIds(), false);
    json.append("    }");
    if (trailingComma) {
      json.append(",");
    }
    json.append("\n");
  }

  private void appendRequestParameters(
      StringBuilder json,
      List<SpringMvcRequestParameterFact> requestParameters) {
    json.append("      \"request_parameters\": [");
    if (requestParameters.isEmpty()) {
      json.append("],\n");
      return;
    }

    json.append("\n");
    for (int index = 0; index < requestParameters.size(); index++) {
      SpringMvcRequestParameterFact parameter = requestParameters.get(index);
      json.append("        {\n");
      appendIndentedStringField(json, 5, "name", parameter.name(), true);
      appendIndentedStringField(json, 5, "source", parameter.source(), true);
      appendIndentedStringField(json, 5, "java_type", parameter.javaType(), true);
      appendIndentedStringArrayField(json, 5, "evidence_ids", parameter.evidenceIds(), false);
      json.append("        }");
      if (index < requestParameters.size() - 1) {
        json.append(",");
      }
      json.append("\n");
    }
    json.append("      ],\n");
  }

  private String endpointId(SpringMvcEndpointFact endpoint) {
    return "endpoint:" + endpoint.controllerClass() + "#" + endpoint.handlerMethod();
  }

  private List<EndpointRow> endpointRows(List<SpringMvcEndpointFact> endpoints) {
    List<EndpointRow> rows = new ArrayList<>();
    for (SpringMvcEndpointFact endpoint : endpoints) {
      List<String> methodLabels = endpointMethodLabels(endpoint);
      for (String path : endpoint.paths()) {
        for (String methodLabel : methodLabels) {
          rows.add(new EndpointRow(methodLabel, path, endpoint));
        }
      }
    }

    return rows.stream()
        .sorted(Comparator.comparing(EndpointRow::path)
            .thenComparing(EndpointRow::methodLabel)
            .thenComparing(row -> row.endpoint().controllerClass())
            .thenComparing(row -> row.endpoint().handlerMethod()))
        .toList();
  }

  private List<String> endpointMethodLabels(SpringMvcEndpointFact endpoint) {
    if (endpoint.httpMethodSemantics() == SpringMvcHttpMethodSemantics.DECLARED
        && !endpoint.httpMethods().isEmpty()) {
      return endpoint.httpMethods();
    }

    return switch (endpoint.httpMethodSemantics()) {
      case NOT_DECLARED -> List.of("METHOD NOT DECLARED");
      case UNSUPPORTED -> List.of("METHOD UNSUPPORTED");
      case DECLARED -> List.of("METHOD NOT DETECTED");
    };
  }

  private String httpMethods(SpringMvcEndpointFact endpoint) {
    if (endpoint.httpMethodSemantics() == SpringMvcHttpMethodSemantics.DECLARED
        && !endpoint.httpMethods().isEmpty()) {
      return codeList(endpoint.httpMethods());
    }

    return switch (endpoint.httpMethodSemantics()) {
      case NOT_DECLARED -> "not declared in source";
      case UNSUPPORTED -> "unsupported source expression";
      case DECLARED -> "not detected";
    };
  }

  private String requestParameters(List<SpringMvcRequestParameterFact> requestParameters) {
    if (requestParameters.isEmpty()) {
      return "none detected";
    }

    StringJoiner joiner = new StringJoiner(", ");
    for (SpringMvcRequestParameterFact parameter : requestParameters) {
      joiner.add(code(parameter.source() + ":" + parameter.name())
          + " ("
          + code(parameter.javaType())
          + ")");
    }
    return joiner.toString();
  }

  private List<SpringMvcEndpointFact> sortedEndpoints(List<SpringMvcEndpointFact> endpoints) {
    return endpoints.stream()
        .sorted(ENDPOINT_ORDER)
        .toList();
  }

  private List<EvidenceRecord> evidenceRecords(
      ProjectLayout layout,
      List<SpringMvcEndpointEvidence> endpointEvidenceRecords) {
    Map<String, EvidenceRecord> uniqueRecords = new LinkedHashMap<>();
    layout.buildFileEvidence().ifPresent(evidence -> uniqueRecords.put(evidence.id(), evidence));
    endpointEvidenceRecords.stream()
        .map(this::evidenceRecord)
        .forEach(evidence -> uniqueRecords.putIfAbsent(evidence.id(), evidence));

    return uniqueRecords.values().stream()
        .sorted(EVIDENCE_ORDER)
        .toList();
  }

  private EvidenceRecord evidenceRecord(SpringMvcEndpointEvidence evidence) {
    return new EvidenceRecord(
        evidence.id(),
        ANNOTATION_SOURCE_TYPE,
        evidence.sourcePath(),
        evidence.className(),
        evidence.methodName(),
        evidence.annotationSymbol(),
        evidence.lineStart(),
        evidence.lineEnd(),
        evidence.excerpt(),
        evidence.confidence());
  }

  private String evidenceIndexJsonl(List<EvidenceRecord> evidenceRecords) {
    StringBuilder jsonl = new StringBuilder();
    for (EvidenceRecord evidence : evidenceRecords) {
      jsonl.append("{");
      appendStringField(jsonl, "id", evidence.id());
      appendStringField(jsonl, "source_type", evidence.sourceType());
      appendStringField(jsonl, "path", evidence.path());
      appendNullableStringField(jsonl, "class_name", evidence.className());
      appendNullableStringField(jsonl, "method_name", evidence.methodName());
      appendStringField(jsonl, "symbol_name", evidence.symbolName());
      appendNullableIntegerField(jsonl, "line_start", evidence.lineStart());
      appendNullableIntegerField(jsonl, "line_end", evidence.lineEnd());
      appendStringField(jsonl, "excerpt", evidence.excerpt());
      appendStringField(jsonl, "confidence", evidence.confidence());
      jsonl.append("}\n");
    }
    return jsonl.toString();
  }

  private void appendIndentedStringField(
      StringBuilder json,
      int indentLevel,
      String name,
      String value,
      boolean trailingComma) {
    indent(json, indentLevel);
    json.append(jsonString(name)).append(": ").append(jsonString(value));
    appendLineEnding(json, trailingComma);
  }

  private void appendIndentedNullableStringField(
      StringBuilder json,
      int indentLevel,
      String name,
      String value,
      boolean trailingComma) {
    indent(json, indentLevel);
    json.append(jsonString(name)).append(": ");
    if (value == null) {
      json.append("null");
    } else {
      json.append(jsonString(value));
    }
    appendLineEnding(json, trailingComma);
  }

  private void appendIndentedStringArrayField(
      StringBuilder json,
      int indentLevel,
      String name,
      List<String> values,
      boolean trailingComma) {
    indent(json, indentLevel);
    json.append(jsonString(name)).append(": ");
    appendStringArray(json, indentLevel, values);
    appendLineEnding(json, trailingComma);
  }

  private void appendStringArray(StringBuilder json, int indentLevel, List<String> values) {
    if (values.isEmpty()) {
      json.append("[]");
      return;
    }

    json.append("[\n");
    for (int index = 0; index < values.size(); index++) {
      indent(json, indentLevel + 1);
      json.append(jsonString(values.get(index)));
      if (index < values.size() - 1) {
        json.append(",");
      }
      json.append("\n");
    }
    indent(json, indentLevel);
    json.append("]");
  }

  private void appendLineEnding(StringBuilder json, boolean trailingComma) {
    if (trailingComma) {
      json.append(",");
    }
    json.append("\n");
  }

  private void indent(StringBuilder json, int indentLevel) {
    json.append("  ".repeat(indentLevel));
  }

  private void appendStringField(StringBuilder json, String name, String value) {
    appendFieldPrefix(json, name);
    json.append(jsonString(value));
  }

  private void appendNullableStringField(StringBuilder json, String name, String value) {
    appendFieldPrefix(json, name);
    if (value == null) {
      json.append("null");
      return;
    }
    json.append(jsonString(value));
  }

  private void appendNullableIntegerField(StringBuilder json, String name, Integer value) {
    appendFieldPrefix(json, name);
    if (value == null) {
      json.append("null");
      return;
    }
    json.append(value);
  }

  private void appendFieldPrefix(StringBuilder json, String name) {
    if (json.charAt(json.length() - 1) != '{') {
      json.append(",");
    }
    json.append(jsonString(name)).append(":");
  }

  private String jsonString(String value) {
    StringBuilder escaped = new StringBuilder();
    escaped.append('"');
    for (int index = 0; index < value.length(); index++) {
      char character = value.charAt(index);
      switch (character) {
        case '"' -> escaped.append("\\\"");
        case '\\' -> escaped.append("\\\\");
        case '\b' -> escaped.append("\\b");
        case '\f' -> escaped.append("\\f");
        case '\n' -> escaped.append("\\n");
        case '\r' -> escaped.append("\\r");
        case '\t' -> escaped.append("\\t");
        default -> {
          if (character < 0x20) {
            escaped.append(String.format("\\u%04x", (int) character));
          } else {
            escaped.append(character);
          }
        }
      }
    }
    escaped.append('"');
    return escaped.toString();
  }

  private String nullableCode(String value) {
    if (value == null || value.isBlank()) {
      return "none detected";
    }
    return code(value);
  }

  private String codeList(List<String> values) {
    if (values.isEmpty()) {
      return "none detected";
    }

    StringJoiner joiner = new StringJoiner(", ");
    for (String value : values) {
      joiner.add(code(value));
    }
    return joiner.toString();
  }

  private String code(String value) {
    return "`" + value.replace("`", "\\`") + "`";
  }

  private static String firstPath(SpringMvcEndpointFact endpoint) {
    if (endpoint.paths().isEmpty()) {
      return "";
    }
    return endpoint.paths().get(0);
  }

  private static String nullSafe(String value) {
    if (value == null) {
      return "";
    }
    return value;
  }

  public record Result(boolean generated, int endpointCount, int evidenceCount) {
  }

  private record EndpointRow(String methodLabel, String path, SpringMvcEndpointFact endpoint) {
  }

  private record ProjectLayout(
      BuildMetadata build,
      List<String> sourceRoots,
      List<String> testRoots,
      Optional<EvidenceRecord> buildFileEvidence) {
    private ProjectLayout {
      sourceRoots = List.copyOf(sourceRoots);
      testRoots = List.copyOf(testRoots);
    }
  }

  private record BuildMetadata(String system, String rootBuildFile, List<String> evidenceIds) {
    private BuildMetadata {
      evidenceIds = List.copyOf(evidenceIds);
    }
  }

  private record EvidenceRecord(
      String id,
      String sourceType,
      String path,
      String className,
      String methodName,
      String symbolName,
      Integer lineStart,
      Integer lineEnd,
      String excerpt,
      String confidence) {
  }
}
