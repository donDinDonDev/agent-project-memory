package io.github.dondindondev.agentprojectmemory.query;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.dondindondev.agentprojectmemory.OutputRedactor;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class ProjectMemoryListRenderer {
  private static final int MAX_TEXT_CHARS = 4096;

  public String render(ProjectMemoryArtifacts artifacts, String subject) {
    return switch (subject) {
      case "modules" -> renderModules(artifacts);
      case "endpoints" -> renderEndpoints(artifacts);
      case "api-operations" -> renderApiOperations(artifacts);
      case "entities" -> renderEntities(artifacts);
      case "tests" -> renderTests(artifacts);
      default -> throw new IllegalArgumentException("Unsupported list subject.");
    };
  }

  private String renderModules(ProjectMemoryArtifacts artifacts) {
    List<JsonNode> modules = arrayAt(artifacts.projectMap(), "/project/modules/items");
    List<String> lines = header(artifacts, "modules", modules.size());
    if (modules.isEmpty()) {
      lines.add("No modules found.");
      return finish(lines);
    }

    for (int index = 0; index < modules.size(); index++) {
      JsonNode module = modules.get(index);
      lines.add((index + 1) + ". " + firstPresent(module, "module_id", "module_path"));
      field(lines, "   ", "module_path", module.path("module_path"));
      lines.add("   build_systems: " + buildSystems(module));
      field(lines, "   ", "support_status", module.path("support_status"));
      field(lines, "   ", "pom_path", module.path("pom_path"));
      lines.add("   source_roots: " + arrayText(module.path("source_roots")));
      lines.add("   test_roots: " + arrayText(module.path("test_roots")));
      field(lines, "   ", "declaration_kind", module.path("declaration_kind"));
      field(lines, "   ", "declared_path", module.path("declared_path"));
      lines.add("   evidence_ids: " + evidenceIds(module, "declaration_evidence_ids", "pom_evidence_ids"));
    }
    return finish(lines);
  }

  private String renderEndpoints(ProjectMemoryArtifacts artifacts) {
    List<JsonNode> endpoints = arrayAt(artifacts.projectMap(), "/endpoints");
    List<String> lines = header(artifacts, "endpoints", endpoints.size());
    if (endpoints.isEmpty()) {
      lines.add("No endpoints found.");
      return finish(lines);
    }

    for (int index = 0; index < endpoints.size(); index++) {
      JsonNode endpoint = endpoints.get(index);
      lines.add((index + 1) + ". " + firstPresent(endpoint, "id", "handler_method"));
      field(lines, "   ", "kind", endpoint.path("api_surface_category"));
      field(lines, "   ", "module_id", endpoint.path("module_id"));
      field(lines, "   ", "controller_class", endpoint.path("controller_class"));
      field(lines, "   ", "handler_method", endpoint.path("handler_method"));
      lines.add("   http_methods: " + arrayText(endpoint.path("http_methods")));
      field(lines, "   ", "http_method_semantics", endpoint.path("http_method_semantics"));
      lines.add("   paths: " + arrayText(endpoint.path("paths")));
      field(lines, "   ", "request_body_type", endpoint.path("request_body_type"));
      field(lines, "   ", "response_type", endpoint.path("response_type"));
      JsonNode mappingSource = endpoint.path("mapping_source");
      lines.add(
          "   mapping_source: kind="
              + text(mappingSource.path("kind"))
              + " binding="
              + text(mappingSource.path("binding"))
              + " uncertainty="
              + text(mappingSource.path("uncertainty"))
              + " evidence_ids="
              + evidenceIds(mappingSource, "evidence_ids"));
      lines.add("   evidence_ids: " + evidenceIds(endpoint, "evidence_ids"));
    }
    return finish(lines);
  }

  private String renderApiOperations(ProjectMemoryArtifacts artifacts) {
    List<JsonNode> operations = arrayAt(
        artifacts.projectMap(),
        "/api_surface/openapi/operations/items");
    List<String> lines = header(artifacts, "api-operations", operations.size());
    if (operations.isEmpty()) {
      lines.add("No api-operations found.");
      return finish(lines);
    }

    for (int index = 0; index < operations.size(); index++) {
      JsonNode operation = operations.get(index);
      lines.add((index + 1) + ". " + firstPresent(operation, "id", "path"));
      lines.add("   kind: spec-backed declared API operation");
      field(lines, "   ", "module_id", operation.path("module_id"));
      field(lines, "   ", "api_surface_category", operation.path("api_surface_category"));
      field(lines, "   ", "spec_path", operation.path("spec_path"));
      field(lines, "   ", "http_method", operation.path("http_method"));
      field(lines, "   ", "path", operation.path("path"));
      field(lines, "   ", "operation_id", operation.path("operation_id"));
      lines.add("   tags: " + arrayText(operation.path("tags"), "tags"));
      field(lines, "   ", "implementation_status", operation.path("implementation_status"));
      lines.add("   evidence_ids: " + evidenceIds(operation, "evidence_ids"));
    }
    return finish(lines);
  }

  private String renderEntities(ProjectMemoryArtifacts artifacts) {
    List<JsonNode> entities = arrayAt(artifacts.projectMap(), "/entities/items");
    List<JsonNode> embeddables = arrayAt(artifacts.projectMap(), "/entities/embeddables/items");
    List<JsonNode> repositoryRelations = repositoryRelationRows(artifacts.projectMap());
    int resultCount = entities.size() + embeddables.size() + repositoryRelations.size();
    List<String> lines = header(artifacts, "entities", resultCount);
    lines.add("Entities: " + entities.size());
    lines.add("Embeddables: " + embeddables.size());
    lines.add("Repository/entity relation rows: " + repositoryRelations.size());
    if (resultCount == 0) {
      lines.add("No entities found.");
      return finish(lines);
    }
    lines.add("");

    if (!entities.isEmpty()) {
      lines.add("Entity rows");
      for (int index = 0; index < entities.size(); index++) {
        appendEntity(lines, index + 1, entities.get(index));
      }
    }

    if (!embeddables.isEmpty()) {
      if (!entities.isEmpty()) {
        lines.add("");
      }
      lines.add("Embeddable rows");
      for (int index = 0; index < embeddables.size(); index++) {
        appendEmbeddable(lines, index + 1, embeddables.get(index));
      }
    }

    if (!repositoryRelations.isEmpty()) {
      if (!entities.isEmpty() || !embeddables.isEmpty()) {
        lines.add("");
      }
      lines.add("Repository/entity relation rows");
      for (int index = 0; index < repositoryRelations.size(); index++) {
        appendRepositoryRelation(lines, index + 1, repositoryRelations.get(index));
      }
    }

    return finish(lines);
  }

  private void appendEntity(List<String> lines, int number, JsonNode entity) {
    lines.add(number + ". " + firstPresent(entity, "id", "class_name"));
    lines.add("   kind: jpa_entity");
    field(lines, "   ", "module_id", entity.path("module_id"));
    field(lines, "   ", "class_name", entity.path("class_name"));
    field(lines, "   ", "table_name", entity.path("table_name"));
    lines.add("   id_class: " + idClassText(entity.path("id_class")));
    lines.add("   identifier_fields: " + identifierFields(entity.path("identifier_fields")));
    lines.add("   fields: " + sizeOfArray(entity.path("fields")));
    appendEmbeddedTargets(lines, entity.path("fields"));
    appendRelationships(lines, entity.path("relationships"));
    lines.add("   evidence_ids: " + evidenceIds(entity, "evidence_ids"));
  }

  private void appendEmbeddable(List<String> lines, int number, JsonNode embeddable) {
    lines.add(number + ". " + firstPresent(embeddable, "id", "class_name"));
    lines.add("   kind: jpa_embeddable");
    field(lines, "   ", "module_id", embeddable.path("module_id"));
    field(lines, "   ", "class_name", embeddable.path("class_name"));
    lines.add("   fields: " + sizeOfArray(embeddable.path("fields")));
    lines.add("   evidence_ids: " + evidenceIds(embeddable, "evidence_ids"));
  }

  private void appendRepositoryRelation(List<String> lines, int number, JsonNode repository) {
    lines.add(number + ". " + firstPresent(repository, "id", "class_name"));
    field(lines, "   ", "module_id", repository.path("module_id"));
    field(lines, "   ", "repository_class", repository.path("class_name"));
    field(lines, "   ", "entity_relation_status", repository.path("entity_relation_status"));
    JsonNode relation = repository.path("entity_relation");
    if (relation.isObject()) {
      field(lines, "   ", "relation_type", relation.path("relation_type"));
      field(lines, "   ", "target_entity_id", relation.path("target_entity_id"));
      field(lines, "   ", "target_module_id", relation.path("target_module_id"));
      field(lines, "   ", "target_class_name", relation.path("target_class_name"));
      field(lines, "   ", "generic_type", relation.path("generic_type"));
      field(lines, "   ", "support_type", relation.path("support_type"));
      field(lines, "   ", "confidence", relation.path("confidence"));
      field(lines, "   ", "uncertainty", relation.path("uncertainty"));
      lines.add("   relation_evidence_ids: " + evidenceIds(relation, "evidence_ids"));
    } else {
      lines.add("   entity_relation: null");
    }
    lines.add("   evidence_ids: " + evidenceIds(repository, "evidence_ids"));
  }

  private String renderTests(ProjectMemoryArtifacts artifacts) {
    List<JsonNode> tests = arrayAt(artifacts.projectMap(), "/tests/items");
    List<String> lines = header(artifacts, "tests", tests.size());
    if (tests.isEmpty()) {
      lines.add("No tests found.");
      return finish(lines);
    }

    for (int index = 0; index < tests.size(); index++) {
      JsonNode test = tests.get(index);
      lines.add((index + 1) + ". " + firstPresent(test, "id", "class_name"));
      field(lines, "   ", "module_id", test.path("module_id"));
      field(lines, "   ", "class_name", test.path("class_name"));
      appendTestMethods(lines, test.path("methods"));
      appendFrameworkSignals(lines, test.path("framework_signals"));
      appendSpringTestSlices(lines, test.path("spring_test_slices"));
      appendMockSignals(lines, test.path("mock_signals"));
      appendTestedSubjects(lines, test.path("tested_subjects"));
      lines.add("   evidence_ids: " + evidenceIds(test, "evidence_ids"));
    }
    return finish(lines);
  }

  private void appendTestMethods(List<String> lines, JsonNode methods) {
    if (sizeOfArray(methods) == 0) {
      lines.add("   methods: none");
      return;
    }
    lines.add("   methods:");
    for (JsonNode method : methods) {
      lines.add(
          "     - method_name="
              + text(method.path("method_name"))
              + " test_annotation="
              + text(method.path("test_annotation"))
              + " method_kind="
              + text(method.path("method_kind"))
              + " display_name="
              + text(method.path("display_name"), "display_name")
              + " evidence_ids="
              + evidenceIds(method, "evidence_ids"));
    }
  }

  private void appendFrameworkSignals(List<String> lines, JsonNode signals) {
    if (sizeOfArray(signals) == 0) {
      lines.add("   framework_signals: none");
      return;
    }
    lines.add("   framework_signals:");
    for (JsonNode signal : signals) {
      lines.add(
          "     - name="
              + text(signal.path("name"))
              + " signal_kind="
              + text(signal.path("signal_kind"))
              + " evidence_ids="
              + evidenceIds(signal, "evidence_ids"));
    }
  }

  private void appendSpringTestSlices(List<String> lines, JsonNode slices) {
    if (sizeOfArray(slices) == 0) {
      lines.add("   spring_test_slices: none");
      return;
    }
    lines.add("   spring_test_slices:");
    for (JsonNode slice : slices) {
      lines.add(
          "     - annotation="
              + text(slice.path("annotation"))
              + " slice_kind="
              + text(slice.path("slice_kind"))
              + " signal_kind="
              + text(slice.path("signal_kind"))
              + " evidence_ids="
              + evidenceIds(slice, "evidence_ids"));
    }
  }

  private void appendMockSignals(List<String> lines, JsonNode mocks) {
    if (sizeOfArray(mocks) == 0) {
      lines.add("   mock_signals: none");
      return;
    }
    lines.add("   mock_signals:");
    for (JsonNode mock : mocks) {
      lines.add(
          "     - annotation="
              + text(mock.path("annotation"))
              + " mock_signal="
              + text(mock.path("mock_signal"))
              + " signal_kind="
              + text(mock.path("signal_kind"))
              + " target_kind="
              + text(mock.path("target_kind"))
              + " target_name="
              + text(mock.path("target_name"))
              + " evidence_ids="
              + evidenceIds(mock, "evidence_ids"));
    }
  }

  private void appendTestedSubjects(List<String> lines, JsonNode subjects) {
    if (sizeOfArray(subjects) == 0) {
      lines.add("   tested_subjects: none");
      return;
    }
    lines.add("   tested_subjects:");
    for (JsonNode subject : subjects) {
      lines.add(
          "     - relation_status="
              + text(subject.path("relation_status"))
              + " relation_type="
              + text(subject.path("relation_type"))
              + " class_name="
              + text(subject.path("class_name"))
              + " target_module_id="
              + text(subject.path("target_module_id"))
              + " candidate_reference="
              + text(subject.path("candidate_reference"))
              + " support_type="
              + text(subject.path("support_type"))
              + " confidence="
              + text(subject.path("confidence"))
              + " uncertainty="
              + text(subject.path("uncertainty"))
              + " evidence_ids="
              + evidenceIds(subject, "evidence_ids"));
    }
  }

  private void appendEmbeddedTargets(List<String> lines, JsonNode fields) {
    List<JsonNode> embeddedTargets = new ArrayList<>();
    for (JsonNode field : iterableArray(fields)) {
      JsonNode embedded = field.path("embedded");
      if (embedded.isObject()) {
        embeddedTargets.add(field);
      }
    }
    if (embeddedTargets.isEmpty()) {
      lines.add("   embedded_targets: none");
      return;
    }
    lines.add("   embedded_targets:");
    for (JsonNode field : embeddedTargets) {
      JsonNode embedded = field.path("embedded");
      lines.add(
          "     - field="
              + text(field.path("field_name"))
              + " annotation="
              + text(embedded.path("annotation"))
              + " java_type="
              + text(embedded.path("java_type"))
              + " target_resolution="
              + text(embedded.path("target_resolution"))
              + " target_embeddable_id="
              + text(embedded.path("target_embeddable_id"))
              + " target_module_id="
              + text(embedded.path("target_module_id"))
              + " support_type="
              + text(embedded.path("support_type"))
              + " confidence="
              + text(embedded.path("confidence"))
              + " uncertainty="
              + text(embedded.path("uncertainty"))
              + " evidence_ids="
              + evidenceIds(embedded, "evidence_ids"));
    }
  }

  private void appendRelationships(List<String> lines, JsonNode relationships) {
    if (sizeOfArray(relationships) == 0) {
      lines.add("   relationships: none");
      return;
    }
    lines.add("   relationships:");
    for (JsonNode relationship : relationships) {
      JsonNode target = relationship.path("target");
      lines.add(
          "     - field="
              + text(relationship.path("field_name"))
              + " annotation="
              + text(relationship.path("annotation"))
              + " cardinality="
              + text(relationship.path("cardinality"))
              + " java_type="
              + text(relationship.path("java_type"))
              + " target_resolution="
              + text(target.isObject() ? target.path("target_resolution") : relationship.path("target_resolution"))
              + " target_entity_id="
              + text(target.path("target_entity_id"))
              + " target_module_id="
              + text(target.path("target_module_id"))
              + " support_type="
              + text(target.path("support_type"))
              + " confidence="
              + text(target.path("confidence"))
              + " uncertainty="
              + text(target.isObject() ? target.path("uncertainty") : relationship.path("uncertainty"))
              + " ownership_signal="
              + text(relationship.path("ownership_signal"))
              + " evidence_ids="
              + evidenceIds(relationship, "evidence_ids"));
    }
  }

  private List<JsonNode> repositoryRelationRows(JsonNode projectMap) {
    List<JsonNode> rows = new ArrayList<>();
    for (JsonNode repository : arrayAt(projectMap, "/spring_application_surface/repositories/items")) {
      if (repository.has("entity_relation_status")) {
        rows.add(repository);
      }
    }
    return rows;
  }

  private String idClassText(JsonNode idClass) {
    if (!idClass.isObject()) {
      return "null";
    }
    return "type_name="
        + text(idClass.path("type_name"))
        + " field_matching_status="
        + text(idClass.path("field_matching_status"))
        + " semantic_reconstruction_status="
        + text(idClass.path("semantic_reconstruction_status"))
        + " evidence_ids="
        + evidenceIds(idClass, "evidence_ids");
  }

  private String identifierFields(JsonNode fields) {
    List<String> values = new ArrayList<>();
    for (JsonNode field : iterableArray(fields)) {
      values.add(
          text(field.path("field_name"))
              + "("
              + text(field.path("identifier_kind"))
              + ":"
              + text(field.path("java_type"))
              + ":"
              + text(field.path("source_kind"))
              + ")");
    }
    return values.isEmpty() ? "none" : String.join(", ", values);
  }

  private List<String> header(ProjectMemoryArtifacts artifacts, String subject, int resultCount) {
    List<String> lines = new ArrayList<>();
    lines.add("Query: list " + subject);
    lines.add(
        "Source artifacts: project-map.json schema_version="
            + safe(artifacts.projectMapSchemaVersion())
            + ", evidence-index.jsonl records="
            + artifacts.evidenceRecords().size());
    lines.add("Results: " + resultCount);
    if (resultCount > 0) {
      lines.add("");
    }
    return lines;
  }

  private String finish(List<String> lines) {
    return String.join("\n", lines) + "\n";
  }

  private List<JsonNode> arrayAt(JsonNode root, String pointer) {
    return iterableArray(root.at(pointer));
  }

  private List<JsonNode> iterableArray(JsonNode node) {
    if (node == null || !node.isArray()) {
      return List.of();
    }
    List<JsonNode> values = new ArrayList<>();
    for (JsonNode item : node) {
      values.add(item);
    }
    return values;
  }

  private int sizeOfArray(JsonNode node) {
    return node != null && node.isArray() ? node.size() : 0;
  }

  private void field(List<String> lines, String indent, String name, JsonNode value) {
    lines.add(indent + name + ": " + text(value, name));
  }

  private String firstPresent(JsonNode node, String firstField, String secondField) {
    String first = text(node.path(firstField));
    if (!"null".equals(first)) {
      return first;
    }
    return text(node.path(secondField));
  }

  private String buildSystems(JsonNode module) {
    Set<String> systems = new LinkedHashSet<>();
    if (hasText(module.path("pom_path"))
        || "analyzed".equals(rawText(module.at("/build_config/maven/metadata/analysis_status")))) {
      systems.add("maven");
    }
    if ("analyzed".equals(rawText(module.at("/build_config/gradle/analysis_status")))) {
      systems.add("gradle");
    }
    return systems.isEmpty() ? "none" : String.join(", ", systems);
  }

  private String arrayText(JsonNode node) {
    return arrayText(node, null);
  }

  private String arrayText(JsonNode node, String fieldName) {
    List<String> values = new ArrayList<>();
    for (JsonNode value : iterableArray(node)) {
      values.add(fieldName == null ? text(value) : text(value, fieldName));
    }
    return values.isEmpty() ? "none" : String.join(", ", values);
  }

  private String evidenceIds(JsonNode node, String... fieldNames) {
    List<String> values = new ArrayList<>();
    for (String fieldName : fieldNames) {
      JsonNode field = node.path(fieldName);
      if (field.isArray()) {
        for (JsonNode evidenceId : field) {
          values.add(text(evidenceId));
        }
      }
    }
    return values.isEmpty() ? "none" : String.join(", ", values);
  }

  private boolean hasText(JsonNode node) {
    return node != null && node.isTextual() && !node.asText().isBlank();
  }

  private String rawText(JsonNode node) {
    return hasText(node) ? node.asText() : null;
  }

  private String text(JsonNode node) {
    if (node == null || node.isMissingNode() || node.isNull()) {
      return "null";
    }
    if (node.isTextual()) {
      return safe(node.asText());
    }
    if (node.isNumber() || node.isBoolean()) {
      return node.asText();
    }
    return safe(node.toString());
  }

  private String text(JsonNode node, String fieldName) {
    if (node == null || node.isMissingNode() || node.isNull()) {
      return "null";
    }
    if (node.isTextual()) {
      return safe(node.asText(), shouldRedactField(fieldName));
    }
    if (node.isNumber() || node.isBoolean()) {
      return node.asText();
    }
    return safe(node.toString(), shouldRedactField(fieldName));
  }

  private String safe(String value) {
    return safe(value, false);
  }

  private String safe(String value, boolean redact) {
    String rendered = QueryDisplaySafety.sanitize(value);
    String bounded = rendered.length() <= MAX_TEXT_CHARS
        ? rendered
        : rendered.substring(0, MAX_TEXT_CHARS) + "...[truncated]";
    StringBuilder result = new StringBuilder(bounded.length());
    for (int index = 0; index < bounded.length(); index++) {
      char ch = bounded.charAt(index);
      if (ch == '\n') {
        result.append("\\n");
      } else if (ch == '\r') {
        result.append("\\r");
      } else if (ch == '\t') {
        result.append("\\t");
      } else if (Character.isISOControl(ch)) {
        result.append(String.format("\\u%04x", (int) ch));
      } else {
        result.append(ch);
      }
    }
    return result.toString();
  }

  private boolean shouldRedactField(String fieldName) {
    return OutputRedactor.shouldRedactFreeTextField(fieldName)
        || OutputRedactor.isCredentialKey(fieldName);
  }
}
