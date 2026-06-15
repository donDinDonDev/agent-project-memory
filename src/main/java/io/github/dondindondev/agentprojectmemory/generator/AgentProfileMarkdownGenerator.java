package io.github.dondindondev.agentprojectmemory.generator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.dondindondev.agentprojectmemory.profiles.AgentOutputProfile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class AgentProfileMarkdownGenerator {
  private static final ObjectMapper JSON = new ObjectMapper();
  private static final int MAX_INLINE_PATHS = 5;

  public String generate(
      AgentOutputProfile profile,
      String projectMapJson,
      String evidenceIndexJsonl) throws IOException {
    Objects.requireNonNull(profile, "profile");
    Objects.requireNonNull(projectMapJson, "projectMapJson");
    Objects.requireNonNull(evidenceIndexJsonl, "evidenceIndexJsonl");

    JsonNode projectMap = JSON.readTree(projectMapJson);
    Map<String, EvidenceRecord> evidenceById =
        EvidenceReferenceRenderer.evidenceById(evidenceIndexJsonl);
    ProfileCopy profileCopy = profileCopy(profile);

    StringBuilder markdown = new StringBuilder();
    markdown.append("# ")
        .append(MarkdownRenderer.text(profile.displayName()))
        .append(" Agent Profile\n\n");
    markdown.append("Generated deterministically from `project-map.json` and ")
        .append("`evidence-index.jsonl` for the ")
        .append(code(profile.selector()))
        .append(" profile. The profile generator does not re-analyze source files.\n\n");

    appendProfileOperatingNotes(markdown, profile, profileCopy);
    appendSourceArtifacts(markdown, profile);
    appendProjectSnapshot(markdown, projectMap, evidenceById);
    appendEvidencePointers(markdown, projectMap, evidenceById);
    appendFactBoundaryMap(markdown);
    appendProfileChecklist(markdown, profileCopy);
    return markdown.toString();
  }

  private void appendProfileOperatingNotes(
      StringBuilder markdown,
      AgentOutputProfile profile,
      ProfileCopy profileCopy) {
    markdown.append("## Profile Operating Notes\n\n");
    markdown.append("- Intended use: ")
        .append(MarkdownRenderer.text(profileCopy.intendedUse()))
        .append("\n");
    markdown.append("- First-pass reading order: ")
        .append(codeList(profileCopy.readingOrder()))
        .append("\n");
    markdown.append("- Change posture: ")
        .append(MarkdownRenderer.text(profileCopy.changePosture()))
        .append("\n");
    markdown.append("- Evidence posture: ")
        .append(MarkdownRenderer.text(profileCopy.evidencePosture()))
        .append("\n");
    markdown.append("- Selector: ")
        .append(code(profile.selector()))
        .append("; artifact path: ")
        .append(code(profile.artifactPath()))
        .append("\n\n");
  }

  private void appendSourceArtifacts(StringBuilder markdown, AgentOutputProfile profile) {
    markdown.append("## Source Artifacts\n\n");
    markdown.append("- `project-map.json`: machine-readable source for extracted facts, ")
        .append("inferred/statused relations, uncertain signals, document-backed hints, ")
        .append("spec-backed operations, generated-source metadata, warnings, and ")
        .append("not-analyzed status fields.\n");
    markdown.append("- `evidence-index.jsonl`: existing evidence records referenced by ")
        .append("`*_evidence_ids`; this profile does not add evidence records.\n");
    markdown.append("- `agent-guide.md`: generic deterministic guide generated from the same ")
        .append("source artifacts; this profile narrows the presentation for ")
        .append(code(profile.selector()))
        .append(".\n");
    markdown.append("- `endpoints.md`: endpoint-oriented Markdown derived from the same ")
        .append("structured endpoint and OpenAPI facts.\n\n");
  }

  private void appendProjectSnapshot(
      StringBuilder markdown,
      JsonNode projectMap,
      Map<String, EvidenceRecord> evidenceById) {
    JsonNode project = projectMap.path("project");
    JsonNode build = project.path("build");
    JsonNode modules = project.path("modules");
    JsonNode apiSurface = projectMap.path("api_surface");
    JsonNode openApiOperations = apiSurface.path("openapi").path("operations");
    JsonNode springSurface = projectMap.path("spring_application_surface");
    JsonNode configuration = springSurface.path("configuration");
    JsonNode behavior = springSurface.path("behavior");
    JsonNode generatedSources = projectMap.path("generated_sources");
    JsonNode documents = projectMap.path("documents");
    JsonNode quality = projectMap.path("quality");
    JsonNode warnings = projectMap.path("warnings");

    markdown.append("## Project Snapshot\n\n");
    markdown.append("- Schema version: ")
        .append(code(textOrNotRecorded(projectMap, "schema_version")))
        .append("\n");
    markdown.append("- Build system: ")
        .append(code(textOrNotRecorded(build, "system")))
        .append("; root build file: ")
        .append(code(nullableDisplay(nullableText(build, "root_build_file"))))
        .append(".\n");
    appendEvidenceLine(
        markdown,
        "Build evidence",
        EvidenceReferenceRenderer.stringValues(build.path("evidence_ids")),
        evidenceById);
    markdown.append("- Layout counts: modules ")
        .append(code(Integer.toString(arraySize(modules.path("items")))))
        .append(" (status ")
        .append(code(textOrNotRecorded(modules, "analysis_status")))
        .append("); source roots ")
        .append(code(Integer.toString(arraySize(project.path("source_roots")))))
        .append("; test roots ")
        .append(code(Integer.toString(arraySize(project.path("test_roots")))))
        .append(".\n");
    markdown.append("- API counts: endpoint facts ")
        .append(code(Integer.toString(arraySize(projectMap.path("endpoints")))))
        .append("; source-visible endpoint IDs ")
        .append(code(Integer.toString(arraySize(apiSurface
            .path("source_visible_spring_mvc_endpoints")
            .path("endpoint_ids")))))
        .append("; interface endpoint IDs ")
        .append(code(Integer.toString(arraySize(apiSurface
            .path("interface_declared_spring_mvc_endpoints")
            .path("endpoint_ids")))))
        .append("; OpenAPI operations ")
        .append(code(Integer.toString(arraySize(openApiOperations.path("items")))))
        .append(" (status ")
        .append(code(textOrNotRecorded(openApiOperations, "analysis_status")))
        .append(").\n");
    markdown.append("- Spring surface counts: components ")
        .append(code(Integer.toString(arraySize(projectMap.path("components").path("items")))))
        .append("; repository signals ")
        .append(code(Integer.toString(arraySize(springSurface.path("repositories").path("items")))))
        .append("; inferred repository/entity links ")
        .append(code(Integer.toString(inferredRepositoryRelationCount(
            springSurface.path("repositories").path("items")))))
        .append("; configuration classes ")
        .append(code(Integer.toString(arraySize(
            configuration.path("configuration_classes").path("items")))))
        .append("; bean methods ")
        .append(code(Integer.toString(arraySize(configuration.path("bean_methods").path("items")))))
        .append("; behavior/messaging signals ")
        .append(code(Integer.toString(behaviorSignalCount(behavior, springSurface))))
        .append(".\n");
    markdown.append("- Domain/test counts: JPA entities ")
        .append(code(Integer.toString(arraySize(projectMap.path("entities").path("items")))))
        .append("; embeddables ")
        .append(code(Integer.toString(arraySize(
            projectMap.path("entities").path("embeddables").path("items")))))
        .append("; test classes ")
        .append(code(Integer.toString(arraySize(projectMap.path("tests").path("items")))))
        .append(" (status ")
        .append(code(textOrNotRecorded(projectMap.path("tests"), "analysis_status")))
        .append("); test-gap hints ")
        .append(code(Integer.toString(arraySize(
            quality.path("test_gap_signals").path("items")))))
        .append("; change-risk hints ")
        .append(code(Integer.toString(arraySize(
            quality.path("change_risk_signals").path("items")))))
        .append(".\n");
    appendGeneratedSourceSnapshot(markdown, generatedSources);
    markdown.append("- Documents/warnings: documents ")
        .append(code(Integer.toString(arraySize(documents.path("items")))))
        .append(" (status ")
        .append(code(textOrNotRecorded(documents, "analysis_status")))
        .append("); reconciliation hints ")
        .append(code(Integer.toString(arraySize(
            documents.path("reconciliation").path("items")))))
        .append(" (status ")
        .append(code(textOrNotRecorded(documents.path("reconciliation"), "analysis_status")))
        .append("); warnings ")
        .append(code(Integer.toString(arraySize(warnings.path("items")))))
        .append(" (status ")
        .append(code(textOrNotRecorded(warnings, "analysis_status")))
        .append(").\n\n");
  }

  private void appendGeneratedSourceSnapshot(
      StringBuilder markdown,
      JsonNode generatedSources) {
    JsonNode policy = generatedSources.path("policy");
    markdown.append("- Generated-source metadata: status ")
        .append(code(textOrNotRecorded(generatedSources, "analysis_status")))
        .append("; content scan ")
        .append(code(textOrNotRecorded(policy, "content_scan")))
        .append("; content status ")
        .append(code(textOrNotRecorded(policy, "content_status")))
        .append("; roots ")
        .append(code(Integer.toString(arraySize(
            generatedSources.path("roots").path("items")))))
        .append(".\n");
  }

  private void appendEvidencePointers(
      StringBuilder markdown,
      JsonNode projectMap,
      Map<String, EvidenceRecord> evidenceById) {
    markdown.append("## Evidence-Visible Fact Pointers\n\n");
    appendEvidencePointer(markdown, "Build/layout facts", evidenceById, projectMap.path("project"));
    appendEvidencePointer(
        markdown,
        "API facts and spec-backed operations",
        evidenceById,
        projectMap.path("endpoints"),
        projectMap.path("api_surface"));
    appendEvidencePointer(
        markdown,
        "Spring application-surface facts",
        evidenceById,
        projectMap.path("spring_application_surface"));
    appendEvidencePointer(
        markdown,
        "Domain and persistence facts",
        evidenceById,
        projectMap.path("entities"));
    appendEvidencePointer(
        markdown,
        "Tests and quality planning hints",
        evidenceById,
        projectMap.path("tests"),
        projectMap.path("quality"));
    appendEvidencePointer(
        markdown,
        "Document-backed hints",
        evidenceById,
        projectMap.path("documents"));
    appendEvidencePointer(
        markdown,
        "Generated-source metadata and warnings",
        evidenceById,
        projectMap.path("generated_sources"),
        projectMap.path("warnings"));
    appendEvidencePaths(markdown, projectMap, evidenceById);
    markdown.append("\n");
  }

  private void appendEvidencePointer(
      StringBuilder markdown,
      String label,
      Map<String, EvidenceRecord> evidenceById,
      JsonNode... nodes) {
    LinkedHashSet<String> ids = new LinkedHashSet<>();
    for (JsonNode node : nodes) {
      ids.addAll(EvidenceReferenceRenderer.evidenceIdsInSubtree(node));
    }
    markdown.append("- ")
        .append(MarkdownRenderer.text(label))
        .append(": ")
        .append(EvidenceReferenceRenderer.evidenceReferenceList(
            List.copyOf(ids),
            evidenceById))
        .append(".\n");
  }

  private void appendEvidencePaths(
      StringBuilder markdown,
      JsonNode projectMap,
      Map<String, EvidenceRecord> evidenceById) {
    List<String> paths = EvidenceReferenceRenderer.evidencePaths(projectMap, evidenceById);
    markdown.append("- First evidence paths: ")
        .append(cappedCodeList(paths, MAX_INLINE_PATHS, "evidence paths"))
        .append(".\n");
  }

  private void appendFactBoundaryMap(StringBuilder markdown) {
    markdown.append("## Fact Boundary Map\n\n");
    markdown.append("- Extracted facts: use source-visible facts from `project-map.json` ")
        .append("sections such as `endpoints[]`, `components.items`, `entities.items`, ")
        .append("build/config sections, and Spring Boot application signals.\n");
    markdown.append("- Inferred relations/signals: keep `support_type`, ")
        .append("`entity_relation_status`, tested-subject relation statuses, ")
        .append("confidence, and uncertainty fields attached to the relation.\n");
    markdown.append("- Uncertain signals: treat statuses such as `ambiguous`, ")
        .append("`unsupported`, `not_detected`, `no_obvious_test`, and ")
        .append("`uncertain_planning_hint` as bounded planning signals, not proof.\n");
    markdown.append("- Document-backed hints: use `documents.items` and ")
        .append("`documents.reconciliation.items` as default-scope Markdown navigation ")
        .append("or uncertain hints only; document bodies are not serialized here.\n");
    markdown.append("- Spec-backed operations: OpenAPI operation rows are declared contract ")
        .append("facts with `implementation_status: \"not_analyzed\"`, not implemented ")
        .append("endpoint facts.\n");
    markdown.append("- Generated-source metadata: generated-source roots and codegen/API ")
        .append("signals are metadata-only observations with `content_status: ")
        .append("\"not_scanned\"`; generated source contents are not scanned.\n");
    markdown.append("- Warnings: warning rows are inspection hints and change-surface inputs; ")
        .append("do not upgrade them into extracted behavior facts.\n");
    markdown.append("- Not-analyzed areas: preserve explicit `analysis_status`, ")
        .append("`implementation_status`, `content_status`, confidence, and ")
        .append("uncertainty boundaries when using this profile.\n\n");
  }

  private void appendProfileChecklist(StringBuilder markdown, ProfileCopy profileCopy) {
    markdown.append("## Profile Checklist\n\n");
    for (String item : profileCopy.checklist()) {
      markdown.append("- ").append(item).append("\n");
    }
  }

  private void appendEvidenceLine(
      StringBuilder markdown,
      String label,
      List<String> evidenceIds,
      Map<String, EvidenceRecord> evidenceById) {
    markdown.append("  - ")
        .append(MarkdownRenderer.text(label))
        .append(": ")
        .append(EvidenceReferenceRenderer.evidenceReferenceList(evidenceIds, evidenceById))
        .append("\n");
  }

  private int inferredRepositoryRelationCount(JsonNode repositories) {
    if (!repositories.isArray()) {
      return 0;
    }
    int count = 0;
    for (JsonNode repository : repositories) {
      if ("inferred".equals(text(repository, "entity_relation_status"))) {
        count++;
      }
    }
    return count;
  }

  private int behaviorSignalCount(JsonNode behavior, JsonNode springSurface) {
    return arraySize(behavior.path("transaction_boundaries").path("items"))
        + arraySize(behavior.path("scheduled_methods").path("items"))
        + arraySize(behavior.path("event_listeners").path("items"))
        + arraySize(springSurface.path("messaging").path("listener_signals").path("items"));
  }

  private int arraySize(JsonNode node) {
    if (!node.isArray()) {
      return 0;
    }
    return node.size();
  }

  private String cappedCodeList(
      List<String> values,
      int maxVisible,
      String omittedDescription) {
    if (values.isEmpty()) {
      return "none recorded";
    }
    if (values.size() <= maxVisible) {
      return codeList(values);
    }

    List<String> visibleValues = values.subList(0, maxVisible);
    return codeList(visibleValues)
        + ", ... and "
        + (values.size() - maxVisible)
        + " more "
        + MarkdownRenderer.text(omittedDescription)
        + " in "
        + code("evidence-index.jsonl");
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

  private String textOrNotRecorded(JsonNode node, String fieldName) {
    String value = text(node, fieldName);
    if (value.isBlank()) {
      return "not_recorded";
    }
    return value;
  }

  private String nullableDisplay(String value) {
    if (value == null || value.isBlank()) {
      return "not_recorded";
    }
    return value;
  }

  private String codeList(List<String> values) {
    return MarkdownRenderer.inlineCodeList(values, "none recorded");
  }

  private String code(String value) {
    return MarkdownRenderer.inlineCode(value);
  }

  private ProfileCopy profileCopy(AgentOutputProfile profile) {
    return switch (profile) {
      case CODEX -> new ProfileCopy(
          "Local coding-agent implementation, review, and verification planning.",
          List.of("agent-guide.md", "project-map.json", "evidence-index.jsonl", "endpoints.md"),
          "Inspect cited evidence before edits; keep changes tied to existing facts "
              + "and explicit uncertainty.",
          "Prefer file/line evidence references over narrative claims; do not treat "
              + "this Markdown as new evidence.",
          List.of(
              "Start from the affected section in `agent-guide.md`, then verify the "
                  + "same fact IDs in `project-map.json` and `evidence-index.jsonl`.",
              "Before changing code, open the first cited source path for each "
                  + "affected fact category.",
              "When planning tests, keep `no_obvious_test` and "
                  + "`uncertain_planning_hint` as planning hints, not coverage proof."));
      case CLAUDE -> new ProfileCopy(
          "Long-context repository explanation, careful review, and handoff notes.",
          List.of("agent-guide.md", "project-map.json", "endpoints.md", "evidence-index.jsonl"),
          "Keep explanations bounded by extracted facts, inferred/statused rows, "
              + "and cited evidence IDs.",
          "Quote or cite evidence locations when making claims; preserve uncertainty "
              + "instead of smoothing it into prose.",
          List.of(
              "Summarize by section only after checking the corresponding evidence "
                  + "pointer in this profile.",
              "Keep document-backed hints separate from code-backed facts in any "
                  + "handoff or review.",
              "Call out not-analyzed areas explicitly instead of filling them from "
                  + "general framework knowledge."));
      case CURSOR -> new ProfileCopy(
          "Editor-grounded navigation from generated facts to source files.",
          List.of("project-map.json", "endpoints.md", "agent-guide.md", "evidence-index.jsonl"),
          "Use evidence paths as navigation targets; verify source before relying on "
              + "a generated presentation.",
          "Treat profile text as navigation glue over existing evidence records, not "
              + "as a source of truth.",
          List.of(
              "Use `First evidence paths` to jump into source files, then cross-check "
                  + "the associated evidence ID.",
              "For endpoint edits, compare `endpoints.md` with the endpoint rows in "
                  + "`project-map.json`.",
              "Do not scan generated-source contents unless a future analyzer "
                  + "explicitly records them as source facts."));
      case GENERIC -> new ProfileCopy(
          "Tool-neutral repository orientation for deterministic project memory consumers.",
          List.of("project-map.json", "evidence-index.jsonl", "agent-guide.md", "endpoints.md"),
          "Consume machine-readable facts first and use Markdown only as a cautious "
              + "presentation layer.",
          "Resolve every important claim through existing evidence IDs; do not create "
              + "new facts from this profile.",
          List.of(
              "Prefer `project-map.json` for automation and `agent-guide.md` for "
                  + "human orientation.",
              "Preserve extracted, inferred, uncertain, warning, document-backed, "
                  + "spec-backed, generated-source, and not-analyzed categories.",
              "When a category has no evidence pointers, treat it as absent or "
                  + "not recorded in the current analyzer scope."));
    };
  }

  private record ProfileCopy(
      String intendedUse,
      List<String> readingOrder,
      String changePosture,
      String evidencePosture,
      List<String> checklist) {
    private ProfileCopy {
      readingOrder = List.copyOf(readingOrder);
      checklist = List.copyOf(checklist);
    }
  }
}
