package io.github.dondindondev.agentprojectmemory.analyzer.springmvc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public final class SpringMvcEndpointOutputGenerator {
  private static final String MAIN_SOURCE_ROOT = "src/main/java";
  private static final String ENDPOINTS_FILE_NAME = "endpoints.md";
  private static final String EVIDENCE_INDEX_FILE_NAME = "evidence-index.jsonl";
  private static final String ANNOTATION_SOURCE_TYPE = "annotation";

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

    SpringMvcEndpointAnalysis analysis = analyzer.analyze(
        normalizedRepositoryRoot,
        List.of(sourceRoot));

    Files.writeString(
        outputDirectory.resolve(ENDPOINTS_FILE_NAME),
        endpointsMarkdown(analysis),
        StandardCharsets.UTF_8);
    Files.writeString(
        outputDirectory.resolve(EVIDENCE_INDEX_FILE_NAME),
        evidenceIndexJsonl(analysis.evidence()),
        StandardCharsets.UTF_8);

    return new Result(true, analysis.endpoints().size(), analysis.evidence().size());
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
      markdown.append("- Request body: ").append(nullableCode(endpoint.requestBodyType())).append("\n");
      markdown.append("- Response: ").append(nullableCode(endpoint.declaredResponseType())).append("\n");
      markdown.append("- Evidence: ").append(codeList(endpoint.evidenceIds())).append("\n\n");
    }

    return markdown.toString();
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

  private String evidenceIndexJsonl(List<SpringMvcEndpointEvidence> evidenceRecords) {
    StringBuilder jsonl = new StringBuilder();
    for (SpringMvcEndpointEvidence evidence : evidenceRecords) {
      jsonl.append("{");
      appendStringField(jsonl, "id", evidence.id());
      appendStringField(jsonl, "source_type", ANNOTATION_SOURCE_TYPE);
      appendStringField(jsonl, "path", evidence.sourcePath());
      appendStringField(jsonl, "class_name", evidence.className());
      appendNullableStringField(jsonl, "method_name", evidence.methodName());
      appendStringField(jsonl, "symbol_name", evidence.annotationSymbol());
      appendNullableIntegerField(jsonl, "line_start", evidence.lineStart());
      appendNullableIntegerField(jsonl, "line_end", evidence.lineEnd());
      appendStringField(jsonl, "excerpt", evidence.excerpt());
      appendStringField(jsonl, "confidence", evidence.confidence());
      jsonl.append("}\n");
    }
    return jsonl.toString();
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

  public record Result(boolean generated, int endpointCount, int evidenceCount) {
  }

  private record EndpointRow(String methodLabel, String path, SpringMvcEndpointFact endpoint) {
  }
}
