package io.github.dondindondev.agentprojectmemory.generator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

public final class AgentGuideGenerator {
  private static final ObjectMapper JSON = new ObjectMapper();
  private static final int MAX_INLINE_EVIDENCE_REFERENCES = 5;
  private static final int MAX_INLINE_INSPECTION_PATHS = 5;

  public String generate(String projectMapJson, String evidenceIndexJsonl) throws IOException {
    Objects.requireNonNull(projectMapJson, "projectMapJson");
    Objects.requireNonNull(evidenceIndexJsonl, "evidenceIndexJsonl");

    JsonNode projectMap = JSON.readTree(projectMapJson);
    Map<String, EvidenceRecord> evidenceById = evidenceById(evidenceIndexJsonl);

    StringBuilder markdown = new StringBuilder();
    markdown.append("# Agent Guide\n\n");
    markdown.append("Generated deterministically from `project-map.json` and ")
        .append("`evidence-index.jsonl`. The guide generator does not re-analyze source files.\n\n");

    appendProjectLayout(markdown, projectMap.path("project"), evidenceById);
    appendEndpoints(markdown, projectMap.path("endpoints"), evidenceById);
    appendComponents(markdown, projectMap.path("components"), evidenceById);
    appendEntities(markdown, projectMap.path("entities"), evidenceById);
    appendTests(markdown, projectMap.path("tests"), evidenceById);
    appendKnownLimits(markdown, projectMap, evidenceById);
    appendInspectionOrder(markdown, projectMap, evidenceById);

    return markdown.toString();
  }

  private void appendProjectLayout(
      StringBuilder markdown,
      JsonNode project,
      Map<String, EvidenceRecord> evidenceById) {
    markdown.append("## Detected Project Layout\n\n");

    JsonNode build = project.path("build");
    String buildSystem = text(build, "system");
    if ("not_detected".equals(buildSystem) || buildSystem.isBlank()) {
      markdown.append("- Build system: Not analyzed; no supported build system was detected.\n");
    } else {
      markdown.append("- Build system: Detected ").append(code(buildSystem)).append("\n");
    }

    String rootBuildFile = nullableText(build, "root_build_file");
    if (rootBuildFile == null) {
      markdown.append("- Root build file: Not analyzed; no root build file was recorded.\n");
    } else {
      markdown.append("- Root build file: Detected ").append(code(rootBuildFile)).append("\n");
    }
    appendEvidenceLine(markdown, build.path("evidence_ids"), evidenceById);

    List<String> sourceRoots = stringValues(project.path("source_roots"));
    if (sourceRoots.isEmpty()) {
      markdown.append("- Source roots: Not analyzed; no supported production source roots were recorded.\n");
    } else {
      markdown.append("- Source roots: Detected ").append(codeList(sourceRoots)).append("\n");
      markdown.append("  - Evidence: recorded in `project-map.json`; ")
          .append("no separate source-root evidence IDs are emitted.\n");
    }

    List<String> testRoots = stringValues(project.path("test_roots"));
    if (testRoots.isEmpty()) {
      markdown.append("- Test roots: Not analyzed; no supported test roots were recorded.\n");
    } else {
      markdown.append("- Test roots: Detected ").append(codeList(testRoots)).append("\n");
      markdown.append("  - Evidence: recorded in `project-map.json`; ")
          .append("no separate test-root evidence IDs are emitted.\n");
    }

    markdown.append("\n");
  }

  private void appendEndpoints(
      StringBuilder markdown,
      JsonNode endpoints,
      Map<String, EvidenceRecord> evidenceById) {
    markdown.append("## Detected Spring MVC Endpoints\n\n");

    if (!endpoints.isArray() || endpoints.isEmpty()) {
      markdown.append("- Detected: no Spring MVC endpoints recorded in `project-map.json`.\n\n");
      return;
    }

    for (JsonNode endpoint : endpoints) {
      markdown.append("### ").append(code(endpointLabel(endpoint))).append("\n\n");
      markdown.append("- Controller: Detected ")
          .append(code(text(endpoint, "controller_class")))
          .append("\n");
      markdown.append("- Handler: Detected ")
          .append(code(text(endpoint, "handler_method")))
          .append("\n");
      appendMappingSourceLine(markdown, endpoint.path("mapping_source"));
      appendHttpMethodLine(markdown, endpoint);
      markdown.append("- Paths: Detected ")
          .append(codeList(stringValues(endpoint.path("paths"))))
          .append("\n");
      appendRequestParametersLine(markdown, endpoint.path("request_parameters"));
      appendNullableDetectedLine(markdown, "Request body", nullableText(endpoint, "request_body_type"));
      appendNullableDetectedLine(markdown, "Response", nullableText(endpoint, "response_type"));
      appendEvidenceLine(markdown, endpoint.path("evidence_ids"), evidenceById);
      markdown.append("\n");
    }
  }

  private void appendComponents(
      StringBuilder markdown,
      JsonNode components,
      Map<String, EvidenceRecord> evidenceById) {
    markdown.append("## Detected Spring Components\n\n");
    markdown.append("- Analysis status: ").append(code(text(components, "analysis_status"))).append("\n");

    JsonNode items = components.path("items");
    if (!items.isArray() || items.isEmpty()) {
      markdown.append("- Detected: no direct Spring stereotype components recorded.\n\n");
      return;
    }

    markdown.append("\n");
    for (JsonNode component : items) {
      markdown.append("### ").append(code(text(component, "class_name"))).append("\n\n");
      markdown.append("- Stereotypes: Detected ")
          .append(codeList(stringValues(component.path("stereotypes"))))
          .append("\n");
      appendEvidenceLine(markdown, component.path("evidence_ids"), evidenceById);
      markdown.append("\n");
    }
  }

  private void appendEntities(
      StringBuilder markdown,
      JsonNode entities,
      Map<String, EvidenceRecord> evidenceById) {
    markdown.append("## Detected JPA Entities\n\n");
    markdown.append("- Analysis status: ").append(code(text(entities, "analysis_status"))).append("\n");

    JsonNode items = entities.path("items");
    if (!items.isArray() || items.isEmpty()) {
      markdown.append("- Detected: no direct JPA entities recorded.\n\n");
      return;
    }

    markdown.append("\n");
    for (JsonNode entity : items) {
      markdown.append("### ").append(code(text(entity, "class_name"))).append("\n\n");
      String tableName = nullableText(entity, "table_name");
      List<String> tableEvidenceIds = evidenceIdsWithSymbol(
          entity.path("evidence_ids"),
          evidenceById,
          "@Table");
      List<String> entityEvidenceIds = evidenceIdsWithoutSymbols(
          entity.path("evidence_ids"),
          evidenceById,
          Set.of("@Table"));
      markdown.append("- Entity: Detected ")
          .append(code(text(entity, "class_name")))
          .append("\n");
      appendEvidenceLine(markdown, entityEvidenceIds, evidenceById);
      appendNullableDetectedLine(markdown, "Table", tableName);
      if (tableName != null && !tableName.isBlank()) {
        appendEvidenceLine(markdown, tableEvidenceIds, evidenceById);
      }
      appendIdentifierFields(markdown, entity.path("identifier_fields"), evidenceById);
      appendRelationships(markdown, entity.path("relationships"), evidenceById);
      markdown.append("\n");
    }
  }

  private void appendIdentifierFields(
      StringBuilder markdown,
      JsonNode identifierFields,
      Map<String, EvidenceRecord> evidenceById) {
    if (!identifierFields.isArray() || identifierFields.isEmpty()) {
      markdown.append("- Identifier fields: Detected none.\n");
      return;
    }

    for (JsonNode field : identifierFields) {
      markdown.append("- Identifier field: Detected ")
          .append(code(text(field, "field_name")))
          .append(" (")
          .append(code(text(field, "java_type")))
          .append(")");
      String declaringClass = nullableText(field, "declaring_class");
      String sourceKind = nullableText(field, "source_kind");
      if (declaringClass != null && !declaringClass.isBlank()) {
        markdown.append(" declared by ").append(code(declaringClass));
      }
      if (sourceKind != null && !sourceKind.isBlank()) {
        markdown.append(" with source_kind ").append(code(sourceKind));
      }
      markdown.append("\n");
      appendEvidenceLine(markdown, field.path("evidence_ids"), evidenceById);
    }
  }

  private void appendRelationships(
      StringBuilder markdown,
      JsonNode relationships,
      Map<String, EvidenceRecord> evidenceById) {
    if (!relationships.isArray() || relationships.isEmpty()) {
      markdown.append("- Relationships: Detected none.\n");
      return;
    }

    for (JsonNode relationship : relationships) {
      markdown.append("- Relationship: Uncertain target for ")
          .append(code(text(relationship, "field_name")))
          .append(" ")
          .append(code(text(relationship, "annotation")))
          .append(" declared type ")
          .append(code(text(relationship, "java_type")))
          .append("\n");
      markdown.append("  - target_resolution: ")
          .append(code(text(relationship, "target_resolution")))
          .append("\n");
      markdown.append("  - uncertainty: ")
          .append(code(text(relationship, "uncertainty")))
          .append("\n");
      appendEvidenceLine(markdown, relationship.path("evidence_ids"), evidenceById);
    }
  }

  private void appendTests(
      StringBuilder markdown,
      JsonNode tests,
      Map<String, EvidenceRecord> evidenceById) {
    markdown.append("## Detected Tests\n\n");
    String analysisStatus = text(tests, "analysis_status");
    markdown.append("- Analysis status: ").append(code(analysisStatus)).append("\n");

    JsonNode items = tests.path("items");
    if (!items.isArray() || items.isEmpty()) {
      if ("not_detected".equals(analysisStatus)) {
        markdown.append("- Not analyzed: no supported test root was detected.\n\n");
      } else {
        markdown.append("- Detected: no test classes recorded.\n\n");
      }
      return;
    }

    markdown.append("\n");
    for (JsonNode test : items) {
      markdown.append("### ").append(code(text(test, "class_name"))).append("\n\n");
      markdown.append("- Test class: Detected ")
          .append(code(text(test, "class_name")))
          .append("\n");
      appendEvidenceLine(markdown, test.path("evidence_ids"), evidenceById);
      markdown.append("- Source: Detected ").append(code(text(test, "source_path"))).append("\n");
      appendFrameworkSignals(markdown, test.path("framework_signals"), evidenceById);
      appendTestedSubjects(markdown, test.path("tested_subjects"), evidenceById);
      markdown.append("\n");
    }
  }

  private void appendFrameworkSignals(
      StringBuilder markdown,
      JsonNode frameworkSignals,
      Map<String, EvidenceRecord> evidenceById) {
    if (!frameworkSignals.isArray() || frameworkSignals.isEmpty()) {
      markdown.append("- Framework signals: Detected none.\n");
      return;
    }

    for (JsonNode signal : frameworkSignals) {
      markdown.append("- Framework signal: Detected ")
          .append(code(text(signal, "name")))
          .append("\n");
      appendEvidenceLine(markdown, signal.path("evidence_ids"), evidenceById);
    }
  }

  private void appendTestedSubjects(
      StringBuilder markdown,
      JsonNode testedSubjects,
      Map<String, EvidenceRecord> evidenceById) {
    if (!testedSubjects.isArray() || testedSubjects.isEmpty()) {
      markdown.append("- Inferred tested subjects: none recorded.\n");
      return;
    }

    for (JsonNode subject : testedSubjects) {
      markdown.append("- Inferred tested subject: ")
          .append(code(text(subject, "class_name")))
          .append(" (support_type: ")
          .append(code(text(subject, "support_type")))
          .append(", confidence: ")
          .append(code(text(subject, "confidence")));
      String uncertainty = nullableText(subject, "uncertainty");
      if (uncertainty != null) {
        markdown.append(", uncertainty: ").append(code(uncertainty));
      }
      markdown.append(")\n");
      appendEvidenceLine(markdown, subject.path("evidence_ids"), evidenceById);
    }
  }

  private void appendKnownLimits(
      StringBuilder markdown,
      JsonNode projectMap,
      Map<String, EvidenceRecord> evidenceById) {
    markdown.append("## Known Uncertainty And Limits\n\n");
    appendWarnings(markdown, projectMap.path("warnings"), evidenceById);
    markdown.append("- Not analyzed: Spring runtime behavior such as component scanning, dependency ")
        .append("injection graphs, bean lifecycle, scopes, and conditional configuration is not ")
        .append("represented by `components.items`.\n");
    markdown.append("- Uncertain: JPA relationship targets preserve `target_resolution: ")
        .append("declared_type_only` and `uncertainty: target_type_not_resolved`; no symbol ")
        .append("solving or ORM runtime behavior is claimed.\n");
    markdown.append("- Not analyzed: JPA mapped-superclass identifier support is limited to ")
        .append("conservative source-visible mapped-superclass chains; unresolved, ambiguous, ")
        .append("cyclic, or non-source-visible branches are skipped.\n");
    markdown.append("- Inferred: tested-subject relations use naming conventions only. Test execution, ")
        .append("coverage, assertion behavior, call graphs, and complete subject mapping are not ")
        .append("analyzed.\n");
    if (projectMap.path("project").path("modules").isObject()) {
      markdown.append("- Not analyzed: connectors, LLM summaries, repository chat, generic RAG, ")
          .append("Gradle/Kotlin support, Maven profiles, effective POM reconstruction, ")
          .append("dependency graphs, and recursive nested Maven modules are outside this guide.\n");
    } else {
      markdown.append("- Not analyzed: connectors, LLM summaries, repository chat, generic RAG, ")
          .append("Gradle/Kotlin support, and multi-module Maven parsing are outside this guide.\n");
    }
    markdown.append("- Not analyzed: generated sources, OpenAPI YAML, generated API reconstruction, ")
        .append("classpath-only interfaces, and ambiguous interface endpoint bindings are outside ")
        .append("the source-visible interface endpoint support.\n");

    if (projectMap.path("endpoints").isEmpty()) {
      markdown.append("- Uncertain: no endpoint facts were recorded, so HTTP entry points may be absent ")
          .append("or outside the currently supported analyzer scope.\n");
    }
    if (projectMap.path("entities").path("items").isEmpty()) {
      markdown.append("- Uncertain: no entity facts were recorded, so persistence mappings may be absent ")
          .append("or outside the currently supported analyzer scope.\n");
    }
    if ("not_detected".equals(text(projectMap.path("tests"), "analysis_status"))) {
      markdown.append("- Not analyzed: supported Maven test roots were not detected.\n");
    }

    markdown.append("\n");
  }

  private void appendInspectionOrder(
      StringBuilder markdown,
      JsonNode projectMap,
      Map<String, EvidenceRecord> evidenceById) {
    markdown.append("## Practical Inspection Order For Coding Agents\n\n");
    markdown.append("1. Start with detected build and layout facts");
    List<String> buildPaths = evidencePaths(projectMap.path("project").path("build"), evidenceById);
    appendPathHint(markdown, buildPaths);
    markdown.append(".\n");
    markdown.append("2. For HTTP behavior, inspect detected endpoint and hidden-surface warning evidence");
    LinkedHashSet<String> httpPaths = new LinkedHashSet<>();
    httpPaths.addAll(evidencePaths(projectMap.path("endpoints"), evidenceById));
    httpPaths.addAll(evidencePaths(projectMap.path("warnings").path("items"), evidenceById));
    appendPathHint(markdown, List.copyOf(httpPaths));
    markdown.append(".\n");
    markdown.append("3. For Spring wiring changes, inspect detected component evidence");
    appendPathHint(markdown, evidencePaths(projectMap.path("components").path("items"), evidenceById));
    markdown.append(" and avoid assuming runtime injection graphs.\n");
    markdown.append("4. For persistence changes, inspect detected entity evidence");
    appendPathHint(markdown, evidencePaths(projectMap.path("entities").path("items"), evidenceById));
    markdown.append(" and treat relationship targets as declared-type-only.\n");
    markdown.append("5. For tests, inspect detected test files and inferred tested-subject evidence");
    appendPathHint(markdown, evidencePaths(projectMap.path("tests").path("items"), evidenceById));
    markdown.append("; do not treat inferred subjects as coverage proof.\n");
  }

  private void appendWarnings(
      StringBuilder markdown,
      JsonNode warnings,
      Map<String, EvidenceRecord> evidenceById) {
    JsonNode items = warnings.path("items");
    if (!items.isArray() || items.isEmpty()) {
      return;
    }

    for (JsonNode warning : items) {
      markdown.append("- Warning: ")
          .append(code(text(warning, "category")))
          .append(" signal ")
          .append(code(text(warning, "signal")))
          .append(" at ")
          .append(code(text(warning, "source_path")))
          .append(". ")
          .append(text(warning, "message"))
          .append("\n");
      appendEvidenceLine(markdown, warning.path("evidence_ids"), evidenceById);
    }
  }

  private void appendHttpMethodLine(StringBuilder markdown, JsonNode endpoint) {
    String semantics = text(endpoint, "http_method_semantics");
    List<String> methods = stringValues(endpoint.path("http_methods"));
    if ("declared".equals(semantics) && !methods.isEmpty()) {
      markdown.append("- HTTP methods: Detected ").append(codeList(methods)).append("\n");
      return;
    }
    if ("not_declared".equals(semantics)) {
      markdown.append("- HTTP methods: Detected not declared in source.\n");
      return;
    }
    if ("unsupported".equals(semantics)) {
      markdown.append("- HTTP methods: Uncertain; unsupported source expression.\n");
      return;
    }
    markdown.append("- HTTP methods: Not analyzed; no supported method value was recorded.\n");
  }

  private void appendMappingSourceLine(StringBuilder markdown, JsonNode mappingSource) {
    if (!mappingSource.isObject() || mappingSource.isEmpty()) {
      markdown.append("- Mapping source: Not analyzed; no mapping source was recorded.\n");
      return;
    }

    String kind = text(mappingSource, "kind");
    String declaringType = text(mappingSource, "declaring_type");
    String declaringMethod = text(mappingSource, "declaring_method");
    String binding = text(mappingSource, "binding");
    markdown.append("- Mapping source: Detected ")
        .append(code(kind))
        .append(" from ")
        .append(code(declaringType + "#" + declaringMethod));
    if (!binding.isBlank()) {
      markdown.append(" with binding ").append(code(binding));
    }
    String uncertainty = nullableText(mappingSource, "uncertainty");
    if (uncertainty != null) {
      markdown.append(" and uncertainty ").append(code(uncertainty));
    }
    markdown.append("\n");
  }

  private void appendRequestParametersLine(StringBuilder markdown, JsonNode requestParameters) {
    if (!requestParameters.isArray() || requestParameters.isEmpty()) {
      markdown.append("- Request parameters: Detected none.\n");
      return;
    }

    List<String> parameters = new ArrayList<>();
    for (JsonNode parameter : requestParameters) {
      parameters.add(text(parameter, "source")
          + ":"
          + text(parameter, "name")
          + " ("
          + text(parameter, "java_type")
          + ")");
    }
    markdown.append("- Request parameters: Detected ").append(codeList(parameters)).append("\n");
  }

  private void appendNullableDetectedLine(StringBuilder markdown, String label, String value) {
    markdown.append("- ").append(label).append(": ");
    if (value == null || value.isBlank()) {
      markdown.append("Detected none.\n");
      return;
    }
    markdown.append("Detected ").append(code(value)).append("\n");
  }

  private void appendEvidenceLine(
      StringBuilder markdown,
      JsonNode evidenceIds,
      Map<String, EvidenceRecord> evidenceById) {
    appendEvidenceLine(markdown, stringValues(evidenceIds), evidenceById);
  }

  private void appendEvidenceLine(
      StringBuilder markdown,
      List<String> ids,
      Map<String, EvidenceRecord> evidenceById) {
    markdown.append("  - Evidence: ");
    if (ids.isEmpty()) {
      markdown.append("none recorded.\n");
      return;
    }

    int visibleCount = Math.min(ids.size(), MAX_INLINE_EVIDENCE_REFERENCES);
    StringJoiner joiner = new StringJoiner(", ");
    for (int i = 0; i < visibleCount; i++) {
      joiner.add(evidenceReference(ids.get(i), evidenceById));
    }
    markdown.append(joiner);
    appendOmittedEvidenceSuffix(markdown, ids.size() - visibleCount);
    markdown.append("\n");
  }

  private String endpointLabel(JsonNode endpoint) {
    List<String> paths = stringValues(endpoint.path("paths"));
    String path = paths.isEmpty() ? "path not detected" : paths.get(0);
    String semantics = text(endpoint, "http_method_semantics");
    List<String> methods = stringValues(endpoint.path("http_methods"));
    String method;
    if ("declared".equals(semantics) && !methods.isEmpty()) {
      method = methods.get(0);
    } else if ("not_declared".equals(semantics)) {
      method = "METHOD NOT DECLARED";
    } else if ("unsupported".equals(semantics)) {
      method = "METHOD UNSUPPORTED";
    } else {
      method = "METHOD NOT DETECTED";
    }
    return method + " " + path;
  }

  private Map<String, EvidenceRecord> evidenceById(String evidenceIndexJsonl) throws IOException {
    Map<String, EvidenceRecord> evidenceById = new LinkedHashMap<>();
    for (String line : evidenceIndexJsonl.split("\\R")) {
      if (line.isBlank()) {
        continue;
      }
      JsonNode evidence = JSON.readTree(line);
      EvidenceRecord record = new EvidenceRecord(
          text(evidence, "id"),
          nullableText(evidence, "path"),
          nullableInteger(evidence, "line_start"),
          nullableInteger(evidence, "line_end"),
          nullableText(evidence, "symbol_name"));
      evidenceById.put(record.id(), record);
    }
    return evidenceById;
  }

  private List<String> evidenceIdsWithSymbol(
      JsonNode evidenceIds,
      Map<String, EvidenceRecord> evidenceById,
      String symbolName) {
    List<String> matching = new ArrayList<>();
    for (String id : stringValues(evidenceIds)) {
      if (evidenceMatchesSymbol(id, evidenceById, symbolName)) {
        matching.add(id);
      }
    }
    return matching;
  }

  private List<String> evidenceIdsWithoutSymbols(
      JsonNode evidenceIds,
      Map<String, EvidenceRecord> evidenceById,
      Set<String> excludedSymbols) {
    List<String> matching = new ArrayList<>();
    for (String id : stringValues(evidenceIds)) {
      boolean excluded = false;
      for (String symbolName : excludedSymbols) {
        if (evidenceMatchesSymbol(id, evidenceById, symbolName)) {
          excluded = true;
          break;
        }
      }
      if (!excluded) {
        matching.add(id);
      }
    }
    return matching;
  }

  private boolean evidenceMatchesSymbol(
      String id,
      Map<String, EvidenceRecord> evidenceById,
      String symbolName) {
    EvidenceRecord evidence = evidenceById.get(id);
    if (evidence != null && symbolName.equals(evidence.symbolName())) {
      return true;
    }
    return id.contains(":" + symbolName);
  }

  private List<String> evidencePaths(JsonNode node, Map<String, EvidenceRecord> evidenceById) {
    LinkedHashSet<String> paths = new LinkedHashSet<>();
    collectEvidencePaths(node, evidenceById, paths);
    return List.copyOf(paths);
  }

  private void collectEvidencePaths(
      JsonNode node,
      Map<String, EvidenceRecord> evidenceById,
      LinkedHashSet<String> paths) {
    if (node == null || node.isMissingNode() || node.isNull()) {
      return;
    }
    if (node.isObject()) {
      JsonNode evidenceIds = node.path("evidence_ids");
      if (evidenceIds.isArray()) {
        for (String id : stringValues(evidenceIds)) {
          EvidenceRecord evidence = evidenceById.get(id);
          if (evidence != null && evidence.path() != null && !evidence.path().isBlank()) {
            paths.add(evidence.path());
          }
        }
      }
      node.fields().forEachRemaining(entry -> collectEvidencePaths(entry.getValue(), evidenceById, paths));
      return;
    }
    if (node.isArray()) {
      for (JsonNode child : node) {
        collectEvidencePaths(child, evidenceById, paths);
      }
    }
  }

  private void appendPathHint(StringBuilder markdown, List<String> paths) {
    if (paths.isEmpty()) {
      markdown.append(" (no evidence paths recorded)");
      return;
    }
    markdown.append(" in ").append(cappedCodeList(
        paths,
        MAX_INLINE_INSPECTION_PATHS,
        "evidence paths"));
  }

  private void appendOmittedEvidenceSuffix(StringBuilder markdown, int omittedCount) {
    if (omittedCount <= 0) {
      return;
    }
    markdown.append(", ... and ")
        .append(omittedCount)
        .append(" more evidence references in ")
        .append(code("evidence-index.jsonl"));
  }

  private String cappedCodeList(
      List<String> values,
      int maxVisible,
      String omittedDescription) {
    if (values.size() <= maxVisible) {
      return codeList(values);
    }

    List<String> visibleValues = values.subList(0, maxVisible);
    return codeList(visibleValues)
        + ", ... and "
        + (values.size() - maxVisible)
        + " more "
        + omittedDescription
        + " in "
        + code("evidence-index.jsonl");
  }

  private String evidenceReference(String id, Map<String, EvidenceRecord> evidenceById) {
    EvidenceRecord evidence = evidenceById.get(id);
    if (evidence == null) {
      return code(id) + " (unresolved evidence record)";
    }
    return code(evidence.location()) + " (" + code(id) + ")";
  }

  private String text(JsonNode node, String fieldName) {
    JsonNode value = node.path(fieldName);
    if (value.isMissingNode() || value.isNull()) {
      return "";
    }
    return value.asText();
  }

  private String nullableText(JsonNode node, String fieldName) {
    JsonNode value = node.path(fieldName);
    if (value.isMissingNode() || value.isNull()) {
      return null;
    }
    return value.asText();
  }

  private Integer nullableInteger(JsonNode node, String fieldName) {
    JsonNode value = node.path(fieldName);
    if (value.isMissingNode() || value.isNull()) {
      return null;
    }
    return value.asInt();
  }

  private List<String> stringValues(JsonNode values) {
    if (!values.isArray()) {
      return List.of();
    }

    List<String> strings = new ArrayList<>();
    for (JsonNode value : values) {
      if (!value.isNull()) {
        strings.add(value.asText());
      }
    }
    return strings;
  }

  private String codeList(List<String> values) {
    if (values.isEmpty()) {
      return "none recorded";
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

  private record EvidenceRecord(
      String id,
      String path,
      Integer lineStart,
      Integer lineEnd,
      String symbolName) {
    private String location() {
      if (path == null || path.isBlank()) {
        return "unknown-source";
      }
      if (lineStart == null) {
        return path;
      }
      if (lineEnd == null || lineEnd.equals(lineStart)) {
        return path + ":" + lineStart;
      }
      return path + ":" + lineStart + "-" + lineEnd;
    }
  }
}
