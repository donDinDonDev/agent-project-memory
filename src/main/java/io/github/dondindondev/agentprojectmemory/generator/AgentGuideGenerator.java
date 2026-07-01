package io.github.dondindondev.agentprojectmemory.generator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.dondindondev.agentprojectmemory.OutputRedactor;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.ToIntFunction;

public final class AgentGuideGenerator {
  private static final ObjectMapper JSON = new ObjectMapper();
  private static final int MAX_INLINE_INSPECTION_PATHS = 5;
  private static final int MAX_INLINE_BUILD_CONFIG_ITEMS = 5;
  private static final int MAX_INLINE_DOCUMENT_REFS = 3;
  private static final int KIB = 1024;
  private static final int MIB = 1024 * KIB;
  private static final int MEDIUM_GUIDE_BYTES = 100 * KIB;
  private static final int MEDIUM_GUIDE_LINES = 1_000;
  private static final int LARGE_GUIDE_BYTES = 250 * KIB;
  private static final int LARGE_GUIDE_LINES = 1_500;
  private static final int HUGE_GUIDE_BYTES = MIB;
  private static final int HUGE_GUIDE_LINES = 5_000;
  private static final int LARGE_MACHINE_ARTIFACT_BYTES = MIB;
  private static final int HUGE_KNOWN_OUTPUT_BYTES = 5 * MIB;
  private static final int LARGE_DETAILED_SECTION_LINES = 500;
  private static final int LARGE_DETAILED_SECTION_BYTES = 100 * KIB;
  private static final int VERY_LARGE_DETAILED_SECTION_LINES = 1_500;
  private static final int VERY_LARGE_DETAILED_SECTION_BYTES = 250 * KIB;
  private static final int LARGE_DETAILED_SECTION_ITEM_COUNT = 50;
  private static final int MAX_DETAILED_TEST_CLASSES = 50;
  private static final int MAX_LARGE_TEST_METHODS_PER_CLASS = 10;
  private static final int MAX_LARGE_TEST_FRAMEWORK_SIGNALS_PER_CLASS = 5;
  private static final int MAX_LARGE_TEST_SLICES_PER_CLASS = 5;
  private static final int MAX_LARGE_TEST_MOCK_SIGNALS_PER_CLASS = 5;
  private static final int MAX_LARGE_TESTED_SUBJECTS_PER_CLASS = 5;
  private static final int MAX_LARGE_QUALITY_TEST_GAP_SIGNALS = 50;
  private static final int MAX_LARGE_QUALITY_CHANGE_RISK_SIGNALS = 50;
  private static final int MAX_LARGE_SPRING_MODULE_SUMMARY_ROWS = 25;
  private static final int MAX_LARGE_SPRING_DETAILED_MODULES = 20;
  private static final int MAX_LARGE_DOMAIN_ENTITIES = 50;
  private static final int MAX_LARGE_DOMAIN_EMBEDDABLES = 25;
  private static final int MAX_LARGE_DOMAIN_FIELDS_PER_TYPE = 8;
  private static final int MAX_LARGE_DOMAIN_IDENTIFIERS_PER_ENTITY = 5;
  private static final int MAX_LARGE_DOMAIN_RELATIONSHIPS_PER_ENTITY = 8;
  private static final int MAX_LARGE_DOMAIN_JOIN_COLUMNS_PER_RELATIONSHIP = 5;

  public String generate(String projectMapJson, String evidenceIndexJsonl) throws IOException {
    Objects.requireNonNull(projectMapJson, "projectMapJson");
    Objects.requireNonNull(evidenceIndexJsonl, "evidenceIndexJsonl");

    JsonNode projectMap = JSON.readTree(projectMapJson);
    Map<String, EvidenceRecord> evidenceById = evidenceById(evidenceIndexJsonl);
    List<ModuleInfo> modules = moduleInfos(projectMap.path("project").path("modules"));
    Map<String, ModuleInfo> moduleById = moduleById(modules);

    String header = "# Agent Guide\n\n"
        + "Generated deterministically from `project-map.json` and "
        + "`evidence-index.jsonl`. The guide generator does not re-analyze source files.\n\n";

    StringBuilder detailedSections = new StringBuilder();
    appendProjectLayout(detailedSections, projectMap.path("project"), modules, evidenceById);
    appendBuildAndConfiguration(detailedSections, projectMap, moduleById, evidenceById);
    appendApiSurfaceInterpretation(
        detailedSections,
        projectMap.path("api_surface"),
        moduleById,
        evidenceById);
    appendEndpoints(detailedSections, projectMap.path("endpoints"), moduleById, evidenceById);
    appendSpringApplicationSurface(
        detailedSections,
        projectMap.path("spring_application_surface"),
        projectMap.path("warnings"),
        moduleById,
        evidenceById);
    appendComponents(detailedSections, projectMap.path("components"), moduleById, evidenceById);
    appendEntities(detailedSections, projectMap, moduleById, evidenceById);
    appendTests(detailedSections, projectMap.path("tests"), moduleById, evidenceById);
    appendQuality(detailedSections, projectMap.path("quality"), moduleById, evidenceById);
    appendLocalProjectDocumentation(
        detailedSections,
        projectMap.path("documents"),
        moduleById,
        evidenceById);
    appendGeneratedSourceAndCodegenOrientation(
        detailedSections,
        projectMap.path("generated_sources"),
        moduleById,
        evidenceById);
    appendOptionalSurfaceOrientation(detailedSections);
    appendDetailedKnownLimits(detailedSections, projectMap, moduleById, evidenceById);

    return guideWithFrontLoadedOrientation(
        header,
        detailedSections.toString(),
        projectMapJson,
        evidenceIndexJsonl,
        projectMap,
        evidenceById);
  }

  private String guideWithFrontLoadedOrientation(
      String header,
      String detailedSections,
      String projectMapJson,
      String evidenceIndexJsonl,
      JsonNode projectMap,
      Map<String, EvidenceRecord> evidenceById) {
    SizeSnapshot snapshot = sizeSnapshot(projectMapJson, evidenceIndexJsonl, header + detailedSections);
    String orientation = "";
    for (int attempt = 0; attempt < 3; attempt++) {
      orientation = frontLoadedOrientation(projectMap, evidenceById, snapshot);
      SizeSnapshot nextSnapshot = sizeSnapshot(
          projectMapJson,
          evidenceIndexJsonl,
          header + orientation + detailedSections);
      if (nextSnapshot.equals(snapshot)) {
        return normalizeMarkdownDocument(header + orientation + detailedSections);
      }
      snapshot = nextSnapshot;
    }
    orientation = frontLoadedOrientation(projectMap, evidenceById, snapshot);
    return normalizeMarkdownDocument(header + orientation + detailedSections);
  }

  private String normalizeMarkdownDocument(String markdown) {
    int end = markdown.length();
    while (end > 0 && markdown.charAt(end - 1) == '\n') {
      end--;
    }
    return markdown.substring(0, end) + "\n";
  }

  private String frontLoadedOrientation(
      JsonNode projectMap,
      Map<String, EvidenceRecord> evidenceById,
      SizeSnapshot snapshot) {
    StringBuilder markdown = new StringBuilder();
    appendReadThisFirst(markdown, snapshot);
    appendTrustAndVerificationLegend(markdown);
    appendInspectionOrder(markdown, projectMap, evidenceById);
    appendProjectMemoryOverview(markdown, projectMap, evidenceById.size(), snapshot);
    if (snapshot.requiresLargeArtifactNotice()) {
      appendLargeArtifactNotice(markdown, snapshot);
    }
    appendKnownUncertaintySnapshot(markdown, projectMap);
    appendNotRepresentedInThisScan(markdown, projectMap);
    return markdown.toString();
  }

  private void appendReadThisFirst(StringBuilder markdown, SizeSnapshot snapshot) {
    markdown.append("## Read This First\n\n");
    markdown.append("- Open `artifact-set.json` before this guide and respect its artifact authority labels.\n");
    markdown.append("- Use this guide as deterministic orientation only. It is not evidence and does not re-analyze source files.\n");
    markdown.append("- For large or unknown outputs, prefer `query <path> agent-context`, targeted query commands, focused `project-map.json` selection, exact `evidence-index.jsonl` lookup, and source readback instead of reading every row.\n");
    if (snapshot.requiresLargeArtifactNotice()) {
      markdown.append("- Large artifact notice: this guide or known generator inputs cross a large threshold; read this top block first and select task-relevant detailed sections.\n");
    }
    markdown.append("- Size note: this guide is ")
        .append(code(snapshot.guideBand()))
        .append(" (about ")
        .append(code(formatBytes(snapshot.guideBytes())))
        .append(", ")
        .append(code(Long.toString(snapshot.guideLines())))
        .append(" rendered lines); known generator inputs are `project-map.json` ")
        .append(code(formatBytes(snapshot.projectMapBytes())))
        .append(" and `evidence-index.jsonl` ")
        .append(code(formatBytes(snapshot.evidenceIndexBytes())))
        .append(".\n\n");
  }

  private void appendTrustAndVerificationLegend(StringBuilder markdown) {
    markdown.append("## Trust And Verification Legend\n\n");
    markdown.append("Trust and verification legend:\n");
    markdown.append("- Use `evidence-index.jsonl` as the authoritative source-backed evidence ledger; verify important claims against its exact records and the repository source locations they cite.\n");
    markdown.append("- Generated project facts: `project-map.json` facts; verify important use through their evidence IDs.\n");
    markdown.append("- Deterministic presentation: this guide, `endpoints.md`, and query stdout help with orientation; they are not evidence.\n");
    markdown.append("- Navigation, provenance, or execution metadata: `artifact-set.json`, `project-graph.json`, `source-registry.json`, profiles, LLM/provider AI output, cache, workspace, adapter output, release metadata, security reports, and downstream-agent output are non-evidence unless a later public contract explicitly changes that.\n");
    markdown.append("- Before code changes, review findings, public/security/release wording, or architecture decisions, resolve exact evidence IDs and read the cited source.\n\n");
  }

  private void appendProjectMemoryOverview(
      StringBuilder markdown,
      JsonNode projectMap,
      int evidenceRecordCount,
      SizeSnapshot snapshot) {
    int moduleCount = arraySize(projectMap.path("project").path("modules").path("items"));
    int sourceRootCount = arraySize(projectMap.path("project").path("source_roots"));
    int testRootCount = arraySize(projectMap.path("project").path("test_roots"));
    int endpointCount = arraySize(projectMap.path("endpoints"));
    int componentCount = arraySize(projectMap.path("components").path("items"));
    int springSurfaceCount = springSurfaceFactCount(projectMap.path("spring_application_surface"));
    int entityCount = arraySize(projectMap.path("entities").path("items"));
    int embeddableCount = arraySize(projectMap.path("entities").path("embeddables").path("items"));
    int testCount = arraySize(projectMap.path("tests").path("items"));
    int qualitySignalCount = qualitySignalCount(projectMap.path("quality"));
    int documentCount = arraySize(projectMap.path("documents").path("items"));
    int reconciliationCount = arraySize(
        projectMap.path("documents").path("reconciliation").path("items"));
    int warningCount = arraySize(projectMap.path("warnings").path("items"));

    markdown.append("## Project Memory Overview\n\n");
    markdown.append("- Build/layout: build system ")
        .append(code(text(projectMap.path("project").path("build"), "system")))
        .append(", modules ")
        .append(code(Integer.toString(moduleCount)))
        .append(", source roots ")
        .append(code(Integer.toString(sourceRootCount)))
        .append(", test roots ")
        .append(code(Integer.toString(testRootCount)))
        .append(".\n");
    markdown.append("- Source-backed fact surfaces: endpoints ")
        .append(code(Integer.toString(endpointCount)))
        .append(", direct Spring components ")
        .append(code(Integer.toString(componentCount)))
        .append(", Spring application surface rows ")
        .append(code(Integer.toString(springSurfaceCount)))
        .append(", entities ")
        .append(code(Integer.toString(entityCount)))
        .append(", embeddables ")
        .append(code(Integer.toString(embeddableCount)))
        .append(", tests ")
        .append(code(Integer.toString(testCount)))
        .append(".\n");
    markdown.append("- Planning/navigation surfaces: warnings ")
        .append(code(Integer.toString(warningCount)))
        .append(", quality/change-risk hints ")
        .append(code(Integer.toString(qualitySignalCount)))
        .append(", local documents ")
        .append(code(Integer.toString(documentCount)))
        .append(", document reconciliation hints ")
        .append(code(Integer.toString(reconciliationCount)))
        .append(".\n");
    markdown.append("- Evidence records: ")
        .append(code(Integer.toString(evidenceRecordCount)))
        .append(" records in `evidence-index.jsonl`; this overview is presentation only.\n");
    markdown.append("- Size band: ")
        .append(code(snapshot.guideBand()))
        .append("; large detailed sections should be selected by task and verified through exact evidence IDs.\n\n");
  }

  private void appendLargeArtifactNotice(StringBuilder markdown, SizeSnapshot snapshot) {
    markdown.append("## Large Artifact Notice\n\n");
    markdown.append("Large artifact notice: this guide is deterministic presentation, not evidence. For first-pass orientation, read this top block first. For large detailed sections, prefer targeted query, focused JSON selection, exact evidence lookup, or source readback instead of reading every row.\n");
    markdown.append("- Guide band: ")
        .append(code(snapshot.guideBand()))
        .append(" (about ")
        .append(code(formatBytes(snapshot.guideBytes())))
        .append(", ")
        .append(code(Long.toString(snapshot.guideLines())))
        .append(" rendered lines).\n");
    List<String> largeArtifacts = new ArrayList<>();
    if (snapshot.projectMapBytes() > LARGE_MACHINE_ARTIFACT_BYTES) {
      largeArtifacts.add("project-map.json " + formatBytes(snapshot.projectMapBytes()));
    }
    if (snapshot.evidenceIndexBytes() > LARGE_MACHINE_ARTIFACT_BYTES) {
      largeArtifacts.add("evidence-index.jsonl " + formatBytes(snapshot.evidenceIndexBytes()));
    }
    if (largeArtifacts.isEmpty()) {
      markdown.append("- Large machine artifacts visible to this renderer: none among `project-map.json` and `evidence-index.jsonl`.\n");
    } else {
      markdown.append("- Large machine artifacts visible to this renderer: ")
          .append(codeList(largeArtifacts))
          .append(". Use `artifact-set.json` for the complete generated artifact inventory.\n");
    }
    if (snapshot.knownOutputBytes() > HUGE_KNOWN_OUTPUT_BYTES) {
      markdown.append("- Known renderer inputs plus this guide exceed ")
          .append(code("5 MiB"))
          .append("; make a task-specific artifact reading plan before opening full machine artifacts.\n");
    }
    markdown.append("\n");
  }

  private void appendKnownUncertaintySnapshot(StringBuilder markdown, JsonNode projectMap) {
    int warningCount = arraySize(projectMap.path("warnings").path("items"));
    int inferredOrStatusedCount = inferredOrStatusedRowCount(projectMap);
    int uncertaintyLabelCount = fieldValueCount(projectMap, "uncertainty");
    int notAnalyzedStatusCount = statusValueCount(projectMap, "analysis_status", Set.of(
        "not_analyzed",
        "not_detected",
        "unsupported"));

    markdown.append("## Known Uncertainty Snapshot\n\n");
    markdown.append("- Warnings: ")
        .append(code(Integer.toString(warningCount)))
        .append(" warning rows; warning evidence and messages stay in the detailed limits section.\n");
    markdown.append("- Inferred or statused rows: ")
        .append(code(Integer.toString(inferredOrStatusedCount)))
        .append(" rows; keep `inferred`, `ambiguous`, `not_detected`, `unsupported`, and similar labels attached to any use.\n");
    markdown.append("- Explicit uncertainty labels: ")
        .append(code(Integer.toString(uncertaintyLabelCount)))
        .append(" values; preserve those caveats with the cited evidence.\n");
    markdown.append("- Not analyzed/out-of-scope status markers: ")
        .append(code(Integer.toString(notAnalyzedStatusCount)))
        .append("; runtime behavior, generated-source contents, test execution/coverage, source/spec agreement, connectors, and LLM summaries remain outside source-backed evidence unless a later contract says otherwise.\n\n");
  }

  private void appendNotRepresentedInThisScan(StringBuilder markdown, JsonNode projectMap) {
    List<String> absent = new ArrayList<>();
    if (arraySize(projectMap.path("endpoints")) == 0) {
      absent.add("Spring MVC endpoints");
    }
    if (arraySize(projectMap.path("components").path("items")) == 0) {
      absent.add("direct Spring components");
    }
    if (!hasDomainGuideContent(projectMap)) {
      absent.add("domain/data model facts");
    }
    if (arraySize(projectMap.path("tests").path("items")) == 0) {
      absent.add("test classes");
    }
    if (!hasQualitySignals(projectMap.path("quality"))) {
      absent.add("quality/change-risk planning hints");
    }
    if (!hasLocalDocumentationGuideContent(projectMap.path("documents"))) {
      absent.add("local project documentation");
    }
    if (arraySize(projectMap.path("generated_sources").path("roots").path("items")) == 0) {
      absent.add("generated-source root metadata");
    }
    if (absent.isEmpty()) {
      return;
    }

    markdown.append("## Not Represented In This Scan\n\n");
    markdown.append("- No represented rows for: ")
        .append(codeList(absent))
        .append(". This means the current deterministic scan emitted no rows for those surfaces; it does not prove the runtime behavior is absent outside the supported analyzer scope.\n\n");
  }

  private void appendOptionalSurfaceOrientation(StringBuilder markdown) {
    markdown.append("## Optional Surface Orientation\n\n");
    markdown.append("- Use `artifact-set.json` to confirm whether adapter provenance, agent profiles, AI presentation, cache metadata, or workspace output belong to the generated artifact set.\n");
    markdown.append("- Treat optional surfaces as provenance, navigation, execution metadata, or presentation. They are not `evidence-index.jsonl` evidence and must not create Java/Spring project facts.\n\n");
  }

  private SizeSnapshot sizeSnapshot(
      String projectMapJson,
      String evidenceIndexJsonl,
      String guideMarkdown) {
    long projectMapBytes = utf8Bytes(projectMapJson);
    long evidenceIndexBytes = utf8Bytes(evidenceIndexJsonl);
    long guideBytes = utf8Bytes(guideMarkdown);
    long guideLines = guideMarkdown.isEmpty() ? 0 : guideMarkdown.lines().count();
    String guideBand = guideBand(guideBytes, guideLines);
    return new SizeSnapshot(
        projectMapBytes,
        evidenceIndexBytes,
        guideBytes,
        guideLines,
        projectMapBytes + evidenceIndexBytes + guideBytes,
        guideBand);
  }

  private String guideBand(long guideBytes, long guideLines) {
    if (guideBytes > HUGE_GUIDE_BYTES || guideLines > HUGE_GUIDE_LINES) {
      return "huge-guide";
    }
    if (guideBytes > LARGE_GUIDE_BYTES || guideLines > LARGE_GUIDE_LINES) {
      return "large-guide";
    }
    if (guideBytes > MEDIUM_GUIDE_BYTES || guideLines > MEDIUM_GUIDE_LINES) {
      return "medium-guide";
    }
    return "small-guide";
  }

  private long utf8Bytes(String value) {
    return value.getBytes(StandardCharsets.UTF_8).length;
  }

  private boolean isLargeDetailedSection(String renderedSection, int primaryDetailRows) {
    long lineCount = renderedSection.isEmpty() ? 0 : renderedSection.lines().count();
    long byteCount = utf8Bytes(renderedSection);
    return primaryDetailRows > LARGE_DETAILED_SECTION_ITEM_COUNT
        || lineCount > LARGE_DETAILED_SECTION_LINES
        || byteCount > LARGE_DETAILED_SECTION_BYTES
        || lineCount > VERY_LARGE_DETAILED_SECTION_LINES
        || byteCount > VERY_LARGE_DETAILED_SECTION_BYTES;
  }

  private void appendLargeSectionSummaryBoundary(StringBuilder markdown) {
    markdown.append("- Large section: use this summary first. Detailed rows are deterministic ")
        .append("presentation only; full generated facts remain in `project-map.json`, and ")
        .append("source-backed evidence remains in `evidence-index.jsonl`.\n");
  }

  private void appendLargeSectionEvidenceVisibility(StringBuilder markdown) {
    markdown.append("- Evidence visibility: displayed rows keep evidence references where available; ")
        .append("omitted rows remain inspectable by resolving their `evidence_ids` from ")
        .append("`project-map.json` into `evidence-index.jsonl`.\n");
  }

  private void appendOmittedRows(
      StringBuilder markdown,
      String indent,
      int omittedCount,
      String rowDescription,
      String projectMapPointer) {
    if (omittedCount <= 0) {
      return;
    }
    markdown.append(indent)
        .append("- ... and ")
        .append(omittedCount)
        .append(" more ")
        .append(MarkdownRenderer.text(rowDescription))
        .append(" in ")
        .append(code("project-map.json"));
    if (projectMapPointer != null && !projectMapPointer.isBlank()) {
      markdown.append(" (").append(code(projectMapPointer)).append(")");
    }
    markdown.append(".\n");
  }

  private List<JsonNode> jsonArrayItems(JsonNode values) {
    if (!values.isArray() || values.isEmpty()) {
      return List.of();
    }
    List<JsonNode> items = new ArrayList<>();
    for (JsonNode value : values) {
      items.add(value);
    }
    return List.copyOf(items);
  }

  private <T> List<T> prioritizedRows(List<T> rows, ToIntFunction<T> priorityFunction) {
    List<PrioritizedRow<T>> prioritized = new ArrayList<>();
    for (int index = 0; index < rows.size(); index++) {
      T row = rows.get(index);
      prioritized.add(new PrioritizedRow<>(row, priorityFunction.applyAsInt(row), index));
    }
    prioritized.sort(Comparator
        .comparingInt(PrioritizedRow<T>::priority)
        .thenComparingInt(PrioritizedRow<T>::originalIndex));
    List<T> ordered = new ArrayList<>();
    for (PrioritizedRow<T> row : prioritized) {
      ordered.add(row.value());
    }
    return List.copyOf(ordered);
  }

  private String formatBytes(long bytes) {
    long kib = (bytes + KIB - 1) / KIB;
    return kib + " KiB";
  }

  private int arraySize(JsonNode node) {
    return node.isArray() ? node.size() : 0;
  }

  private int springSurfaceFactCount(JsonNode springApplicationSurface) {
    if (!springApplicationSurface.isObject()) {
      return 0;
    }
    return arraySize(springApplicationSurface.path("repositories").path("items"))
        + arraySize(
            springApplicationSurface.path("configuration").path("configuration_classes").path("items"))
        + arraySize(
            springApplicationSurface.path("configuration").path("configuration_properties").path("items"))
        + arraySize(
            springApplicationSurface.path("configuration").path("bean_methods").path("items"))
        + arraySize(
            springApplicationSurface.path("behavior").path("transaction_boundaries").path("items"))
        + arraySize(
            springApplicationSurface.path("behavior").path("scheduled_methods").path("items"))
        + arraySize(
            springApplicationSurface.path("behavior").path("event_listeners").path("items"))
        + arraySize(
            springApplicationSurface.path("messaging").path("listener_signals").path("items"))
        + arraySize(
            springApplicationSurface.path("security").path("configuration_warnings").path("warning_ids"));
  }

  private int qualitySignalCount(JsonNode quality) {
    if (!quality.isObject()) {
      return 0;
    }
    return arraySize(quality.path("test_gap_signals").path("items"))
        + arraySize(quality.path("change_risk_signals").path("items"));
  }

  private int testPrimaryDetailRowCount(JsonNode tests) {
    int count = 0;
    for (JsonNode test : items(tests)) {
      count++;
      count += arraySize(test.path("framework_signals"));
      count += arraySize(test.path("spring_test_slices"));
      count += arraySize(test.path("mock_signals"));
      count += arraySize(test.path("methods"));
      count += arraySize(test.path("tested_subjects"));
    }
    return count;
  }

  private int largeTestPriority(JsonNode test) {
    if (hasArrayEntries(test.path("spring_test_slices"))) {
      return 0;
    }
    if (hasArrayEntries(test.path("mock_signals"))) {
      return 1;
    }
    for (JsonNode subject : jsonArrayItems(test.path("tested_subjects"))) {
      if ("inferred".equals(textOrFallback(subject, "relation_status", "inferred"))) {
        return 2;
      }
    }
    for (JsonNode subject : jsonArrayItems(test.path("tested_subjects"))) {
      String status = textOrFallback(subject, "relation_status", "inferred");
      if ("ambiguous".equals(status)
          || "unsupported".equals(status)
          || "unresolved".equals(status)
          || "not_detected".equals(status)
          || "not_analyzed".equals(status)) {
        return 3;
      }
    }
    return 4;
  }

  private int largeQualitySignalPriority(JsonNode signal) {
    String status = text(signal, "status");
    String uncertainty = nullableText(signal, "uncertainty");
    if (status.contains("warning")
        || status.contains("uncertain")
        || (uncertainty != null && !uncertainty.isBlank() && !"null".equals(uncertainty))) {
      return 0;
    }
    String subjectKind = text(signal, "subject_kind");
    String signalName = text(signal, "signal");
    if (subjectKind.contains("endpoint")
        || subjectKind.contains("spring")
        || subjectKind.contains("jpa")
        || subjectKind.contains("entity")
        || subjectKind.contains("repository")
        || signalName.contains("security")) {
      return 1;
    }
    String basis = text(signal, "inference_basis") + " " + text(signal, "risk_basis");
    if (basis.contains("tested_subject") || basis.contains("inferred")) {
      return 2;
    }
    return 3;
  }

  private void appendLargeQualitySummary(
      StringBuilder markdown,
      JsonNode quality,
      Map<String, ModuleInfo> moduleById) {
    List<JsonNode> testGapSignals = items(quality.path("test_gap_signals"));
    List<JsonNode> changeRiskSignals = items(quality.path("change_risk_signals"));
    List<JsonNode> combined = new ArrayList<>();
    combined.addAll(testGapSignals);
    combined.addAll(changeRiskSignals);

    appendLargeSectionSummaryBoundary(markdown);
    markdown.append("- Quality summary: test-gap hints ")
        .append(code(Integer.toString(testGapSignals.size())))
        .append(", change-risk hints ")
        .append(code(Integer.toString(changeRiskSignals.size())))
        .append("; signals ")
        .append(countSummary(countByField(combined, "signal"), moduleById))
        .append("; subject_kind ")
        .append(countSummary(countByField(combined, "subject_kind"), moduleById))
        .append(".\n");
    markdown.append("- Quality status summary: status ")
        .append(countSummary(countByField(combined, "status"), moduleById))
        .append("; confidence ")
        .append(countSummary(countByField(combined, "confidence"), moduleById))
        .append("; uncertainty ")
        .append(countSummary(countByField(combined, "uncertainty"), moduleById))
        .append("; modules ")
        .append(countSummary(countByModule(combined, moduleById), moduleById))
        .append(".\n");
    markdown.append("- Quality detail cap: showing ")
        .append(Math.min(testGapSignals.size(), MAX_LARGE_QUALITY_TEST_GAP_SIGNALS))
        .append(" of ")
        .append(testGapSignals.size())
        .append(" test-gap hints and ")
        .append(Math.min(changeRiskSignals.size(), MAX_LARGE_QUALITY_CHANGE_RISK_SIGNALS))
        .append(" of ")
        .append(changeRiskSignals.size())
        .append(" change-risk hints. These rows are conservative planning hints, not ")
        .append("coverage, execution, assertion, CI, runtime, correctness, vulnerability, ")
        .append("production impact, or business-priority evidence.\n");
    appendLargeSectionEvidenceVisibility(markdown);
    markdown.append("\n");
  }

  private Map<String, Integer> seededModuleCountMap(Map<String, ModuleInfo> moduleById) {
    Map<String, Integer> counts = new LinkedHashMap<>();
    for (String moduleId : moduleById.keySet()) {
      counts.put(moduleId, 0);
    }
    return counts;
  }

  private Map<String, Integer> countByModule(
      List<JsonNode> rows,
      Map<String, ModuleInfo> moduleById) {
    Map<String, Integer> counts = seededModuleCountMap(moduleById);
    for (JsonNode row : rows) {
      incrementCount(counts, moduleCountKey(nullableText(row, "module_id")));
    }
    return counts;
  }

  private Map<String, Integer> countByField(List<JsonNode> rows, String fieldName) {
    Map<String, Integer> counts = new LinkedHashMap<>();
    for (JsonNode row : rows) {
      incrementCount(counts, textOrFallback(row, fieldName, "not_recorded"));
    }
    return counts;
  }

  private void incrementCount(Map<String, Integer> counts, String key) {
    counts.put(key, counts.getOrDefault(key, 0) + 1);
  }

  private String moduleCountKey(String moduleId) {
    if (moduleId == null || moduleId.isBlank()) {
      return "module not recorded";
    }
    return moduleId;
  }

  private String textOrFallback(JsonNode node, String fieldName, String fallback) {
    String value = nullableText(node, fieldName);
    if (value == null || value.isBlank()) {
      return fallback;
    }
    return value;
  }

  private String countSummary(Map<String, Integer> counts, Map<String, ModuleInfo> moduleById) {
    List<String> values = new ArrayList<>();
    for (Map.Entry<String, Integer> entry : counts.entrySet()) {
      if (entry.getValue() <= 0) {
        continue;
      }
      String label = entry.getKey();
      if (moduleById.containsKey(label)) {
        ModuleInfo module = moduleById.get(label);
        label = label + " path " + module.modulePath();
      }
      values.add(label + "=" + entry.getValue());
    }
    if (values.isEmpty()) {
      return code("none");
    }
    return codeList(values);
  }

  private int visibleNestedCount(JsonNode values, boolean largeMode, int cap) {
    if (!values.isArray()) {
      return 0;
    }
    if (!largeMode) {
      return values.size();
    }
    return Math.min(values.size(), cap);
  }

  private List<SpringSurfaceModuleGroup> springSurfaceVisibleGroups(
      Map<String, SpringSurfaceModuleGroup> groups) {
    return groups.values().stream()
        .filter(SpringSurfaceModuleGroup::hasContent)
        .toList();
  }

  private int springSurfacePrimaryDetailRowCount(Map<String, SpringSurfaceModuleGroup> groups) {
    int count = 0;
    for (SpringSurfaceModuleGroup group : groups.values()) {
      count += group.extractedFacts().size();
      count += group.inferredSignals().size();
      count += group.inferredRelations().size();
      count += group.uncertainStatuses().size();
      count += group.warningFacts().size();
    }
    return count;
  }

  private void appendLargeSpringSurfaceSummary(
      StringBuilder markdown,
      List<SpringSurfaceModuleGroup> groups,
      Map<String, ModuleInfo> moduleById) {
    appendLargeSectionSummaryBoundary(markdown);
    int extractedFacts = 0;
    int inferredSignals = 0;
    int inferredRelations = 0;
    int uncertainStatuses = 0;
    int warnings = 0;
    for (SpringSurfaceModuleGroup group : groups) {
      extractedFacts += group.extractedFacts().size();
      inferredSignals += group.inferredSignals().size();
      inferredRelations += group.inferredRelations().size();
      uncertainStatuses += group.uncertainStatuses().size();
      warnings += group.warningFacts().size();
    }
    int detailedCount = Math.min(groups.size(), MAX_LARGE_SPRING_DETAILED_MODULES);
    markdown.append("- Spring surface summary: modules with rows ")
        .append(code(Integer.toString(groups.size())))
        .append(", extracted facts ")
        .append(code(Integer.toString(extractedFacts)))
        .append(", inferred repository interface signals ")
        .append(code(Integer.toString(inferredSignals)))
        .append(", inferred repository/entity relations ")
        .append(code(Integer.toString(inferredRelations)))
        .append(", uncertain/not-analyzed statuses ")
        .append(code(Integer.toString(uncertainStatuses)))
        .append(", warnings ")
        .append(code(Integer.toString(warnings)))
        .append(".\n");
    markdown.append("- Spring surface detail cap: showing detailed rows for ")
        .append(detailedCount)
        .append(" of ")
        .append(groups.size())
        .append(" modules. The cap is Markdown presentation only; omitted module rows ")
        .append("remain in `project-map.json`, and displayed evidence references resolve ")
        .append("through `evidence-index.jsonl`.\n");
    appendLargeSectionEvidenceVisibility(markdown);
    markdown.append("- Module/category summary rows:\n");
    int summaryCount = Math.min(groups.size(), MAX_LARGE_SPRING_MODULE_SUMMARY_ROWS);
    for (int index = 0; index < summaryCount; index++) {
      SpringSurfaceModuleGroup group = groups.get(index);
      markdown.append("  - ")
          .append(code(springSurfaceModuleSummaryLabel(group.moduleId(), moduleById)))
          .append(": extracted ")
          .append(code(Integer.toString(group.extractedFacts().size())))
          .append(", inferred signals ")
          .append(code(Integer.toString(group.inferredSignals().size())))
          .append(", inferred relations ")
          .append(code(Integer.toString(group.inferredRelations().size())))
          .append(", uncertain/not-analyzed ")
          .append(code(Integer.toString(group.uncertainStatuses().size())))
          .append(", warnings ")
          .append(code(Integer.toString(group.warningFacts().size())))
          .append(".\n");
    }
    appendOmittedRows(
        markdown,
        "  ",
        groups.size() - summaryCount,
        "module summary rows",
        "spring_application_surface");
  }

  private String springSurfaceModuleSummaryLabel(
      String moduleId,
      Map<String, ModuleInfo> moduleById) {
    if (moduleId == null || moduleId.isBlank()) {
      return "module not recorded";
    }
    ModuleInfo module = moduleById.get(moduleId);
    if (module == null) {
      return moduleId + " (module path not recorded)";
    }
    return moduleId + " (path: " + module.modulePath() + ")";
  }

  private int largeSpringModulePriority(SpringSurfaceModuleGroup group) {
    if (!group.warningFacts().isEmpty()) {
      return 0;
    }
    if (!group.uncertainStatuses().isEmpty()) {
      return 1;
    }
    if (!group.inferredRelations().isEmpty()) {
      return 2;
    }
    for (JsonNode fact : group.extractedFacts()) {
      String category = text(fact, "surface_category");
      if (category.contains("transaction")
          || category.contains("scheduled")
          || category.contains("event_listener")
          || category.contains("messaging_listener")) {
        return 3;
      }
    }
    return 4;
  }

  private int domainPrimaryDetailRowCount(JsonNode projectMap) {
    int count = 0;
    JsonNode entities = projectMap.path("entities");
    for (JsonNode entity : items(entities)) {
      count++;
      count += arraySize(entity.path("fields"));
      count += arraySize(entity.path("identifier_fields"));
      for (JsonNode relationship : jsonArrayItems(entity.path("relationships"))) {
        count++;
        count += arraySize(relationship.path("join_columns"));
        count += arraySize(relationship.path("join_table").path("join_columns"));
        count += arraySize(relationship.path("join_table").path("inverse_join_columns"));
      }
    }
    for (JsonNode embeddable : items(entities.path("embeddables"))) {
      count++;
      count += arraySize(embeddable.path("fields"));
    }
    return count;
  }

  private Set<String> inferredRepositoryEntityTargetClasses(JsonNode projectMap) {
    Set<String> targetClasses = new LinkedHashSet<>();
    for (JsonNode repository : items(
        projectMap.path("spring_application_surface").path("repositories"))) {
      if (!"inferred".equals(text(repository, "entity_relation_status"))) {
        continue;
      }
      String targetClassName = nullableText(repository.path("entity_relation"), "target_class_name");
      if (targetClassName != null && !targetClassName.isBlank()) {
        targetClasses.add(targetClassName);
      }
    }
    return targetClasses;
  }

  private int largeDomainEntityPriority(JsonNode entity, Set<String> relationTargets) {
    for (JsonNode relationship : jsonArrayItems(entity.path("relationships"))) {
      JsonNode target = relationship.path("target");
      String targetResolution = text(target, "target_resolution");
      String uncertainty = nullableText(target, "uncertainty");
      if ("declared_type_only".equals(targetResolution)
          || (uncertainty != null && !uncertainty.isBlank())) {
        return 0;
      }
    }
    if (entity.path("id_class").isObject() || hasEmbeddedIdentifierSignal(entity)) {
      return 1;
    }
    for (JsonNode identifier : jsonArrayItems(entity.path("identifier_fields"))) {
      if ("mapped_superclass".equals(text(identifier, "source_kind"))) {
        return 2;
      }
    }
    if (relationTargets.contains(text(entity, "class_name"))) {
      return 3;
    }
    return 4;
  }

  private boolean hasEmbeddedIdentifierSignal(JsonNode entity) {
    for (JsonNode field : jsonArrayItems(entity.path("fields"))) {
      JsonNode embedded = field.path("embedded");
      if (embedded.isObject()
          && ("@EmbeddedId".equals(text(embedded, "annotation"))
              || "embedded_id".equals(text(field, "persistence_role")))) {
        return true;
      }
    }
    return false;
  }

  private void appendLargeDomainSummary(
      StringBuilder markdown,
      List<JsonNode> entities,
      List<JsonNode> embeddables,
      Map<String, ModuleInfo> moduleById) {
    appendLargeSectionSummaryBoundary(markdown);
    Map<String, Integer> moduleCounts = seededModuleCountMap(moduleById);
    int fieldRows = 0;
    int identifierRows = 0;
    int relationshipRows = 0;
    int uncertainRelationshipTargets = 0;
    int embeddedSignals = 0;
    int embeddedIdSignals = 0;
    int idClassSignals = 0;
    for (JsonNode entity : entities) {
      incrementCount(moduleCounts, moduleCountKey(nullableText(entity, "module_id")));
      fieldRows += arraySize(entity.path("fields"));
      identifierRows += arraySize(entity.path("identifier_fields"));
      relationshipRows += arraySize(entity.path("relationships"));
      if (entity.path("id_class").isObject()) {
        idClassSignals++;
      }
      for (JsonNode field : jsonArrayItems(entity.path("fields"))) {
        JsonNode embedded = field.path("embedded");
        if (embedded.isObject()) {
          embeddedSignals++;
          if ("@EmbeddedId".equals(text(embedded, "annotation"))
              || "embedded_id".equals(text(field, "persistence_role"))) {
            embeddedIdSignals++;
          }
        }
      }
      for (JsonNode relationship : jsonArrayItems(entity.path("relationships"))) {
        JsonNode target = relationship.path("target");
        String uncertainty = nullableText(target, "uncertainty");
        if ("declared_type_only".equals(text(target, "target_resolution"))
            || (uncertainty != null && !uncertainty.isBlank())) {
          uncertainRelationshipTargets++;
        }
      }
    }
    for (JsonNode embeddable : embeddables) {
      incrementCount(moduleCounts, moduleCountKey(nullableText(embeddable, "module_id")));
      fieldRows += arraySize(embeddable.path("fields"));
    }
    markdown.append("- Domain/JPA summary: modules ")
        .append(countSummary(moduleCounts, moduleById))
        .append("; entity facts ")
        .append(code(Integer.toString(entities.size())))
        .append("; embeddable facts ")
        .append(code(Integer.toString(embeddables.size())))
        .append("; field metadata rows ")
        .append(code(Integer.toString(fieldRows)))
        .append("; identifier rows ")
        .append(code(Integer.toString(identifierRows)))
        .append("; relationship rows ")
        .append(code(Integer.toString(relationshipRows)))
        .append("; uncertain relationship targets ")
        .append(code(Integer.toString(uncertainRelationshipTargets)))
        .append("; embedded signals ")
        .append(code(Integer.toString(embeddedSignals)))
        .append("; embedded-id signals ")
        .append(code(Integer.toString(embeddedIdSignals)))
        .append("; id-class signals ")
        .append(code(Integer.toString(idClassSignals)))
        .append(".\n");
  }

  private int inferredOrStatusedRowCount(JsonNode projectMap) {
    int count = 0;
    for (JsonNode repository : items(
        projectMap.path("spring_application_surface").path("repositories"))) {
      if ("inferred".equals(text(repository, "support_type"))) {
        count++;
      }
      if (!text(repository, "entity_relation_status").isBlank()) {
        count++;
      }
    }
    for (JsonNode test : items(projectMap.path("tests"))) {
      count += arraySize(test.path("tested_subjects"));
    }
    return count + qualitySignalCount(projectMap.path("quality"));
  }

  private int fieldValueCount(JsonNode node, String fieldName) {
    if (node.isObject()) {
      int count = 0;
      var fields = node.fields();
      while (fields.hasNext()) {
        Map.Entry<String, JsonNode> field = fields.next();
        if (fieldName.equals(field.getKey())) {
          JsonNode value = field.getValue();
          if (!value.isMissingNode() && !value.isNull() && !value.asText().isBlank()) {
            count++;
          }
        } else {
          count += fieldValueCount(field.getValue(), fieldName);
        }
      }
      return count;
    }
    if (node.isArray()) {
      int count = 0;
      for (JsonNode item : node) {
        count += fieldValueCount(item, fieldName);
      }
      return count;
    }
    return 0;
  }

  private int statusValueCount(JsonNode node, String fieldName, Set<String> statuses) {
    if (node.isObject()) {
      int count = 0;
      var fields = node.fields();
      while (fields.hasNext()) {
        Map.Entry<String, JsonNode> field = fields.next();
        if (fieldName.equals(field.getKey()) && statuses.contains(field.getValue().asText())) {
          count++;
        } else {
          count += statusValueCount(field.getValue(), fieldName, statuses);
        }
      }
      return count;
    }
    if (node.isArray()) {
      int count = 0;
      for (JsonNode item : node) {
        count += statusValueCount(item, fieldName, statuses);
      }
      return count;
    }
    return 0;
  }

  private void appendProjectLayout(
      StringBuilder markdown,
      JsonNode project,
      List<ModuleInfo> modules,
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
    appendRootBuildFiles(markdown, build.path("root_build_files"), evidenceById);

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

    appendModuleLayout(markdown, project.path("modules"), modules, evidenceById);
    markdown.append("\n");
  }

  private void appendModuleLayout(
      StringBuilder markdown,
      JsonNode modulesNode,
      List<ModuleInfo> modules,
      Map<String, EvidenceRecord> evidenceById) {
    if (!modulesNode.isObject()) {
      return;
    }

    markdown.append("- Modules analysis status: ")
        .append(code(text(modulesNode, "analysis_status")))
        .append("\n");
    if (modules.isEmpty()) {
      markdown.append("- Modules: Not analyzed; no module inventory items were recorded.\n");
      return;
    }

    for (ModuleInfo module : modules) {
      markdown.append("- Module: Detected ")
          .append(moduleLabel(module.moduleId(), module))
          .append("\n");
      markdown.append("  - POM path: ");
      if (module.pomPath() == null || module.pomPath().isBlank()) {
        markdown.append("Not analyzed; no POM path was recorded for this module.\n");
      } else {
        markdown.append("Detected ").append(code(module.pomPath())).append("\n");
      }
      markdown.append("  - Support status: ").append(code(module.supportStatus())).append("\n");
      markdown.append("  - Declaration kind: ").append(code(module.declarationKind())).append("\n");
      markdown.append("  - Declared path: ").append(code(module.declaredPath())).append("\n");
      if (!module.buildSystems().isEmpty()) {
        markdown.append("  - Build systems: ").append(codeList(module.buildSystems())).append("\n");
      }
      if (module.gradleProjectPath() != null && !module.gradleProjectPath().isBlank()) {
        markdown.append("  - Gradle project path: ")
            .append(code(module.gradleProjectPath()))
            .append("\n");
      }
      appendModuleRootsLine(markdown, "Source roots", module.sourceRoots(), "production");
      appendModuleRootsLine(markdown, "Test roots", module.testRoots(), "test");
      appendEvidenceLine(
          markdown,
          module.declarationEvidenceIds(),
          evidenceById,
          "Declaration evidence");
      appendEvidenceLine(markdown, module.pomEvidenceIds(), evidenceById, "POM evidence");
      if (!module.gradleBuildFileEvidenceIds().isEmpty()) {
        appendEvidenceLine(
            markdown,
            module.gradleBuildFileEvidenceIds(),
            evidenceById,
            "Gradle build-file evidence");
      }
    }
  }

  private void appendRootBuildFiles(
      StringBuilder markdown,
      JsonNode rootBuildFiles,
      Map<String, EvidenceRecord> evidenceById) {
    if (!rootBuildFiles.isArray() || rootBuildFiles.isEmpty()) {
      return;
    }

    List<String> labels = new ArrayList<>();
    List<String> evidenceIds = new ArrayList<>();
    for (JsonNode buildFile : rootBuildFiles) {
      labels.add(text(buildFile, "build_system")
          + ":"
          + text(buildFile, "role")
          + ":"
          + text(buildFile, "path"));
      evidenceIds.addAll(stringValues(buildFile.path("evidence_ids")));
    }
    markdown.append("- Root build files: Detected ")
        .append(codeList(labels))
        .append("\n");
    appendEvidenceLine(markdown, evidenceIds, evidenceById, "Root build-file evidence");
  }

  private void appendModuleRootsLine(
      StringBuilder markdown,
      String label,
      List<String> roots,
      String rootKind) {
    markdown.append("  - ").append(label).append(": ");
    if (roots.isEmpty()) {
      markdown.append("Not analyzed; no supported ")
          .append(rootKind)
          .append(" roots were recorded for this module.\n");
      return;
    }
    markdown.append("Detected ").append(codeList(roots)).append("\n");
    markdown.append("  - ")
        .append(label)
        .append(" evidence: recorded in `project-map.json`; no separate ")
        .append(rootKind)
        .append(" root evidence IDs are emitted.\n");
  }

  private void appendModuleLine(
      StringBuilder markdown,
      JsonNode fact,
      Map<String, ModuleInfo> moduleById) {
    String moduleId = nullableText(fact, "module_id");
    if ((moduleId == null || moduleId.isBlank()) && moduleById.isEmpty()) {
      return;
    }
    markdown.append("- Module: ");
    if (moduleId == null || moduleId.isBlank()) {
      markdown.append("Not analyzed; no module identity was recorded.\n");
      return;
    }
    markdown.append("Detected ")
        .append(moduleLabel(moduleId, moduleById))
        .append("\n");
  }

  private void appendBuildAndConfiguration(
      StringBuilder markdown,
      JsonNode projectMap,
      Map<String, ModuleInfo> moduleById,
      Map<String, EvidenceRecord> evidenceById) {
    markdown.append("## Build And Configuration Orientation\n\n");
    JsonNode moduleItems = projectMap.path("project").path("modules").path("items");
    if (!moduleItems.isArray() || moduleItems.isEmpty()) {
      markdown.append("- Not analyzed: no module build/config facts were recorded.\n\n");
      return;
    }

    for (JsonNode module : moduleItems) {
      String moduleId = text(module, "module_id");
      markdown.append("### Module ")
          .append(moduleLabel(moduleId, moduleById))
          .append("\n\n");
      JsonNode buildConfig = module.path("build_config");
      markdown.append("- Build/config analysis status: ")
          .append(code(text(buildConfig, "analysis_status")))
          .append("\n");
      appendMavenOrientation(markdown, buildConfig.path("maven"), evidenceById);
      appendGradleOrientation(markdown, buildConfig.path("gradle"), evidenceById);
      appendResourceOrientation(markdown, buildConfig.path("resources"));
      appendConfigFileOrientation(markdown, buildConfig.path("config_files"), evidenceById);
      appendSpringBootApplicationOrientation(
          markdown,
          buildConfig.path("spring_boot_applications"),
          evidenceById);
      appendBuildConfigWarningSummary(markdown, projectMap.path("warnings"), moduleId);
      markdown.append("\n");
    }
  }

  private void appendMavenOrientation(
      StringBuilder markdown,
      JsonNode maven,
      Map<String, EvidenceRecord> evidenceById) {
    if (!maven.isObject()) {
      markdown.append("- Maven build facts: Not analyzed; no Maven build_config section recorded.\n");
      return;
    }

    appendMavenMetadataOrientation(markdown, maven.path("metadata"), evidenceById);
    appendMavenDependencyOrientation(
        markdown,
        "Source-visible direct dependencies",
        "direct dependency declarations",
        maven.path("dependencies"),
        false,
        evidenceById);
    appendMavenDependencyOrientation(
        markdown,
        "Source-visible dependency-management declarations",
        "dependency-management declarations",
        maven.path("dependency_management"),
        true,
        evidenceById);
    appendMavenPluginOrientation(
        markdown,
        "Source-visible direct plugins",
        "direct plugin declarations",
        maven.path("plugins"),
        false,
        evidenceById);
    appendMavenPluginOrientation(
        markdown,
        "Source-visible plugin-management declarations",
        "plugin-management declarations",
        maven.path("plugin_management"),
        true,
        evidenceById);
  }

  private void appendMavenMetadataOrientation(
      StringBuilder markdown,
      JsonNode metadata,
      Map<String, EvidenceRecord> evidenceById) {
    String analysisStatus = text(metadata, "analysis_status");
    if (!"analyzed".equals(analysisStatus)) {
      markdown.append("- Source-visible Maven metadata: Not analyzed; status ")
          .append(code(analysisStatus))
          .append(".\n");
      return;
    }

    markdown.append("- Source-visible Maven metadata: group_id ")
        .append(mavenValueLabel(metadata.path("group_id")))
        .append(", artifact_id ")
        .append(mavenValueLabel(metadata.path("artifact_id")))
        .append(", version ")
        .append(mavenValueLabel(metadata.path("version")))
        .append(", packaging ")
        .append(mavenValueLabel(metadata.path("packaging")))
        .append(".\n");
    appendEvidenceLine(markdown, evidenceIdsInSubtree(metadata), evidenceById);

    JsonNode parent = metadata.path("parent");
    if ("analyzed".equals(text(parent, "analysis_status"))) {
      markdown.append("- Source-visible Maven parent: group_id ")
          .append(mavenValueLabel(parent.path("group_id")))
          .append(", artifact_id ")
          .append(mavenValueLabel(parent.path("artifact_id")))
          .append(", version ")
          .append(mavenValueLabel(parent.path("version")))
          .append(", relative_path ")
          .append(mavenValueLabel(parent.path("relative_path")))
          .append(".\n");
      appendEvidenceLine(markdown, evidenceIdsInSubtree(parent), evidenceById);
    }
  }

  private void appendMavenDependencyOrientation(
      StringBuilder markdown,
      String label,
      String itemDescription,
      JsonNode section,
      boolean managementDeclarations,
      Map<String, EvidenceRecord> evidenceById) {
    JsonNode items = section.path("items");
    String analysisStatus = text(section, "analysis_status");
    if (!"analyzed".equals(analysisStatus)) {
      markdown.append("- ")
          .append(label)
          .append(": Not analyzed; status ")
          .append(code(analysisStatus))
          .append(".\n");
      return;
    }
    if (!items.isArray() || items.isEmpty()) {
      markdown.append("- ")
          .append(label)
          .append(": Detected none.\n");
      return;
    }

    markdown.append("- ")
        .append(label)
        .append(": Detected ")
        .append(items.size())
        .append(" ")
        .append(itemDescription);
    if (managementDeclarations) {
      markdown.append("; these are management declarations, not active resolved dependencies");
    }
    markdown.append(".\n");
    int visibleCount = Math.min(items.size(), MAX_INLINE_BUILD_CONFIG_ITEMS);
    for (int index = 0; index < visibleCount; index++) {
      JsonNode dependency = items.get(index);
      markdown.append("  - Dependency: ")
          .append(code(mavenCoordinateLabel(dependency)))
          .append(" declaration_kind ")
          .append(code(text(dependency, "declaration_kind")))
          .append(", version ")
          .append(mavenValueLabel(dependency.path("version")))
          .append(", scope ")
          .append(mavenValueLabel(dependency.path("scope")))
          .append(", optional ")
          .append(mavenValueLabel(dependency.path("optional")))
          .append(".\n");
      appendEvidenceLine(markdown, dependency.path("evidence_ids"), evidenceById);
    }
    appendOmittedBuildConfigItems(markdown, items.size() - visibleCount, itemDescription);
  }

  private void appendMavenPluginOrientation(
      StringBuilder markdown,
      String label,
      String itemDescription,
      JsonNode section,
      boolean managementDeclarations,
      Map<String, EvidenceRecord> evidenceById) {
    JsonNode items = section.path("items");
    String analysisStatus = text(section, "analysis_status");
    if (!"analyzed".equals(analysisStatus)) {
      markdown.append("- ")
          .append(label)
          .append(": Not analyzed; status ")
          .append(code(analysisStatus))
          .append(".\n");
      return;
    }
    if (!items.isArray() || items.isEmpty()) {
      markdown.append("- ")
          .append(label)
          .append(": Detected none.\n");
      return;
    }

    markdown.append("- ")
        .append(label)
        .append(": Detected ")
        .append(items.size())
        .append(" ")
        .append(itemDescription);
    if (managementDeclarations) {
      markdown.append("; these are management declarations, not active plugin executions");
    }
    markdown.append(".\n");
    int visibleCount = Math.min(items.size(), MAX_INLINE_BUILD_CONFIG_ITEMS);
    for (int index = 0; index < visibleCount; index++) {
      JsonNode plugin = items.get(index);
      markdown.append("  - Plugin: ")
          .append(code(mavenCoordinateLabel(plugin)))
          .append(" declaration_kind ")
          .append(code(text(plugin, "declaration_kind")))
          .append(", version ")
          .append(mavenValueLabel(plugin.path("version")))
          .append(".\n");
      appendMavenPluginExecutions(markdown, plugin.path("executions"));
      appendMavenPluginSignals(markdown, "Configuration signals", plugin.path("configuration_signals"));
      appendMavenPluginSignals(markdown, "Generator signals", plugin.path("generator_signals"));
      appendEvidenceLine(markdown, plugin.path("evidence_ids"), evidenceById);
    }
    appendOmittedBuildConfigItems(markdown, items.size() - visibleCount, itemDescription);
  }

  private void appendMavenPluginExecutions(StringBuilder markdown, JsonNode executions) {
    if (!executions.isArray() || executions.isEmpty()) {
      markdown.append("    - Direct execution declarations: none recorded.\n");
      return;
    }

    List<String> labels = new ArrayList<>();
    for (JsonNode execution : executions) {
      labels.add("id="
          + nullDisplay(nullableText(execution, "execution_id"))
          + ", phase="
          + mavenRawValueLabel(execution.path("phase"), "phase")
          + ", goals="
          + rawMavenValues(execution.path("goals")));
    }
    markdown.append("    - Direct execution declarations: ")
        .append(cappedCodeList(labels, MAX_INLINE_BUILD_CONFIG_ITEMS, "execution declarations"))
        .append("\n");
  }

  private void appendMavenPluginSignals(
      StringBuilder markdown,
      String label,
      JsonNode signals) {
    List<String> signalNames = new ArrayList<>();
    if (signals.isArray()) {
      for (JsonNode signal : signals) {
        signalNames.add(text(signal, "signal"));
      }
    }
    markdown.append("    - ")
        .append(label)
        .append(": ")
        .append(signalNames.isEmpty()
            ? "none recorded"
            : cappedCodeList(signalNames, MAX_INLINE_BUILD_CONFIG_ITEMS, "signals"))
        .append("\n");
  }

  private void appendGradleOrientation(
      StringBuilder markdown,
      JsonNode gradle,
      Map<String, EvidenceRecord> evidenceById) {
    if (!gradle.isObject()) {
      return;
    }

    JsonNode buildFiles = gradle.path("build_files");
    String analysisStatus = text(gradle, "analysis_status");
    if (!"analyzed".equals(analysisStatus)) {
      markdown.append("- Source-visible Gradle build files: Not analyzed; status ")
          .append(code(analysisStatus))
          .append(".\n");
      return;
    }

    List<String> evidenceIds = new ArrayList<>();
    int buildFileCount = buildFiles.isArray() ? buildFiles.size() : 0;
    markdown.append("- Source-visible Gradle build files: Detected ")
        .append(buildFileCount)
        .append(" local build-file input")
        .append(buildFileCount == 1 ? "" : "s")
        .append(" for Gradle project path ")
        .append(code(text(gradle, "project_path")))
        .append(".\n");
    if (buildFiles.isArray()) {
      int visibleCount = Math.min(buildFiles.size(), MAX_INLINE_BUILD_CONFIG_ITEMS);
      for (int index = 0; index < visibleCount; index++) {
        JsonNode buildFile = buildFiles.get(index);
        markdown.append("  - Gradle build file: ")
            .append(code(text(buildFile, "path")))
            .append(" role ")
            .append(code(text(buildFile, "role")))
            .append(", language ")
            .append(code(text(buildFile, "language")))
            .append(".\n");
        evidenceIds.addAll(stringValues(buildFile.path("evidence_ids")));
      }
      appendOmittedBuildConfigItems(
          markdown,
          buildFiles.size() - visibleCount,
          "Gradle build files");
    }
    appendEvidenceLine(markdown, evidenceIds, evidenceById);

    JsonNode sourceSets = gradle.path("source_sets");
    markdown.append("- Static Gradle sourceSets: Not analyzed; status ")
        .append(code(text(sourceSets, "analysis_status")))
        .append(". Standard Java and resource roots are represented by module roots and resources.\n");
  }

  private void appendResourceOrientation(StringBuilder markdown, JsonNode resources) {
    JsonNode items = resources.path("items");
    String analysisStatus = text(resources, "analysis_status");
    if (!"analyzed".equals(analysisStatus)) {
      markdown.append("- Resource roots: Not analyzed; status ")
          .append(code(analysisStatus))
          .append(".\n");
      return;
    }
    if (!items.isArray() || items.isEmpty()) {
      markdown.append("- Resource roots: Detected none.\n");
      return;
    }

    markdown.append("- Resource roots: Detected ")
        .append(items.size())
        .append(" standard resource root")
        .append(items.size() == 1 ? "" : "s")
        .append(".\n");
    int visibleCount = Math.min(items.size(), MAX_INLINE_BUILD_CONFIG_ITEMS);
    for (int index = 0; index < visibleCount; index++) {
      JsonNode resource = items.get(index);
      markdown.append("  - Resource root: ")
          .append(code(text(resource, "scope")))
          .append(" ")
          .append(code(text(resource, "path")))
          .append("\n");
      markdown.append("    - Evidence: recorded in `project-map.json`; no separate resource-root evidence IDs are emitted.\n");
    }
    appendOmittedBuildConfigItems(markdown, items.size() - visibleCount, "resource roots");
  }

  private void appendConfigFileOrientation(
      StringBuilder markdown,
      JsonNode configFiles,
      Map<String, EvidenceRecord> evidenceById) {
    JsonNode items = configFiles.path("items");
    String analysisStatus = text(configFiles, "analysis_status");
    if (!"analyzed".equals(analysisStatus)) {
      markdown.append("- Config files: Not analyzed; status ")
          .append(code(analysisStatus))
          .append(".\n");
      return;
    }
    if (!items.isArray() || items.isEmpty()) {
      markdown.append("- Config files: Detected none.\n");
      return;
    }

    markdown.append("- Config files: Detected ")
        .append(items.size())
        .append(" path-only supported config file")
        .append(items.size() == 1 ? "" : "s")
        .append("; config contents, keys, and values are not rendered.\n");
    int visibleCount = Math.min(items.size(), MAX_INLINE_BUILD_CONFIG_ITEMS);
    for (int index = 0; index < visibleCount; index++) {
      JsonNode configFile = items.get(index);
      markdown.append("  - Config file: ")
          .append(code(text(configFile, "path")))
          .append(" kind ")
          .append(code(text(configFile, "config_kind")))
          .append(", format ")
          .append(code(text(configFile, "format")));
      String profileName = nullableText(configFile, "profile_name");
      if (profileName != null && !profileName.isBlank()) {
        markdown.append(", filename-derived profile ")
            .append(code(profileName))
            .append(" from ")
            .append(code(text(configFile, "profile_source")));
      }
      markdown.append(".\n");
      appendEvidenceLine(markdown, configFile.path("evidence_ids"), evidenceById);
    }
    appendOmittedBuildConfigItems(markdown, items.size() - visibleCount, "config files");
  }

  private void appendSpringBootApplicationOrientation(
      StringBuilder markdown,
      JsonNode springBootApplications,
      Map<String, EvidenceRecord> evidenceById) {
    JsonNode items = springBootApplications.path("items");
    String analysisStatus = text(springBootApplications, "analysis_status");
    if (!"analyzed".equals(analysisStatus)) {
      markdown.append("- Spring Boot application signals: Not analyzed; status ")
          .append(code(analysisStatus))
          .append(".\n");
      return;
    }
    if (!items.isArray() || items.isEmpty()) {
      markdown.append("- Spring Boot application signals: Detected none.\n");
      return;
    }

    markdown.append("- Spring Boot application signals: Detected ")
        .append(items.size())
        .append(" direct `@SpringBootApplication` class signal")
        .append(items.size() == 1 ? "" : "s")
        .append(".\n");
    int visibleCount = Math.min(items.size(), MAX_INLINE_BUILD_CONFIG_ITEMS);
    for (int index = 0; index < visibleCount; index++) {
      JsonNode application = items.get(index);
      markdown.append("  - Spring Boot application: Detected ")
          .append(code(text(application, "class_name")))
          .append(" at ")
          .append(code(text(application, "source_path")))
          .append(" with signal ")
          .append(code(text(application, "application_signal")))
          .append(".\n");
      JsonNode mainMethod = application.path("main_method");
      markdown.append("    - Main method: ")
          .append(mainMethod.path("present").asBoolean(false)
              ? "Detected source-visible `main` method."
              : "Detected none on the annotated class.")
          .append("\n");
      appendEvidenceLine(markdown, application.path("evidence_ids"), evidenceById);
    }
    appendOmittedBuildConfigItems(markdown, items.size() - visibleCount, "Spring Boot application signals");
  }

  private void appendGeneratedSourceAndCodegenOrientation(
      StringBuilder markdown,
      JsonNode generatedSources,
      Map<String, ModuleInfo> moduleById,
      Map<String, EvidenceRecord> evidenceById) {
    if (!generatedSources.isObject()) {
      return;
    }

    markdown.append("## Generated Source And Codegen Orientation\n\n");
    markdown.append("- Generated-source metadata status: ")
        .append(code(text(generatedSources, "analysis_status")))
        .append(".\n");
    JsonNode policy = generatedSources.path("policy");
    markdown.append("- Policy: content scan ")
        .append(code(text(policy, "content_scan")))
        .append(", default ")
        .append(code(String.valueOf(policy.path("content_scan_default").asBoolean(false))))
        .append(", configurable ")
        .append(code(String.valueOf(policy.path("content_scan_configurable").asBoolean(false))))
        .append(", content_status ")
        .append(code(text(policy, "content_status")))
        .append(".\n");
    markdown.append("- Generated-source roots are metadata only; they are not production `source_roots`, test roots, endpoint facts, API operation facts, or generated API facts.\n");

    JsonNode roots = generatedSources.path("roots");
    JsonNode rootItems = roots.path("items");
    markdown.append("- Generated-source roots: status ")
        .append(code(text(roots, "analysis_status")));
    if (!rootItems.isArray() || rootItems.isEmpty()) {
      markdown.append("; detected none.\n");
    } else {
      markdown.append("; detected ")
          .append(rootItems.size())
          .append(" metadata-only root")
          .append(rootItems.size() == 1 ? "" : "s")
          .append(".\n");
      int visibleCount = Math.min(rootItems.size(), MAX_INLINE_BUILD_CONFIG_ITEMS);
      for (int index = 0; index < visibleCount; index++) {
        JsonNode root = rootItems.get(index);
        markdown.append("  - Root: ")
            .append(code(text(root, "path")))
            .append(" kind ")
            .append(code(text(root, "root_kind")))
            .append(", scope ")
            .append(code(text(root, "scope")))
            .append(", origin ")
            .append(code(text(root, "source_origin")))
            .append(", content_status ")
            .append(code(text(root, "content_status")))
            .append(".\n");
        appendModuleLine(markdown, root, moduleById);
        List<String> relatedWarningIds = stringValues(root.path("related_warning_ids"));
        if (relatedWarningIds.isEmpty()) {
          markdown.append("    - Related warnings: none.\n");
        } else {
          markdown.append("    - Related warnings: ")
              .append(cappedCodeList(relatedWarningIds, MAX_INLINE_BUILD_CONFIG_ITEMS, "warning IDs"))
              .append(".\n");
        }
        appendEvidenceLine(markdown, root.path("evidence_ids"), evidenceById);
      }
      appendOmittedBuildConfigItems(
          markdown,
          rootItems.size() - visibleCount,
          "generated-source root metadata items");
    }

    JsonNode generatorSignals = generatedSources.path("generator_signals");
    markdown.append("- Generator/codegen signals: status ")
        .append(code(text(generatorSignals, "analysis_status")))
        .append("; warning IDs ")
        .append(cappedCodeList(
            stringValues(generatorSignals.path("warning_ids")),
            MAX_INLINE_BUILD_CONFIG_ITEMS,
            "warning IDs"))
        .append("; Maven plugin IDs ")
        .append(cappedCodeList(
            stringValues(generatorSignals.path("maven_plugin_ids")),
            MAX_INLINE_BUILD_CONFIG_ITEMS,
            "Maven plugin IDs"))
        .append(".\n\n");
  }

  private void appendApiSurfaceInterpretation(
      StringBuilder markdown,
      JsonNode apiSurface,
      Map<String, ModuleInfo> moduleById,
      Map<String, EvidenceRecord> evidenceById) {
    if (!apiSurface.isObject()) {
      return;
    }

    markdown.append("## API Surface Interpretation\n\n");
    markdown.append("- API surface analysis status: ")
        .append(code(text(apiSurface, "analysis_status")))
        .append("\n");
    markdown.append("- Source-visible Spring MVC endpoint facts are code-backed local source observations from `endpoints[]`; they do not prove complete runtime handler mappings.\n");
    markdown.append("- Source-visible interface-declared endpoint facts are code-backed only when the interface mapping and unique concrete binding are both source-visible.\n");
    markdown.append("- Declared OpenAPI operations are spec-backed contract facts with `implementation_status: \"not_analyzed\"`; they are not implemented endpoint facts.\n");
    markdown.append("- Generated-source API signals, repository-rest warnings, and hidden HTTP warnings are inspection hints, not endpoint or operation facts.\n");
    markdown.append("- LLM output, generated Markdown, release notes, and chat text are never evidence for API surface facts or relations.\n");
    appendEndpointIdSummary(
        markdown,
        "Source-visible direct Spring MVC endpoint IDs",
        apiSurface.path("source_visible_spring_mvc_endpoints"));
    appendEndpointIdSummary(
        markdown,
        "Source-visible interface-declared Spring MVC endpoint IDs",
        apiSurface.path("interface_declared_spring_mvc_endpoints"));
    appendOpenApiSpecSummary(markdown, apiSurface.path("openapi"), moduleById, evidenceById);
    appendApiSurfaceWarningSummary(
        markdown,
        "Generated-source API warning IDs",
        apiSurface.path("generated_source_api_signals"));
    appendApiSurfaceWarningSummary(
        markdown,
        "Repository-rest warning IDs",
        apiSurface.path("repository_rest_warnings"));
    appendApiSurfaceWarningSummary(
        markdown,
        "Hidden HTTP warning IDs",
        apiSurface.path("hidden_http_warnings"));
    markdown.append("\n");
  }

  private void appendEndpointIdSummary(
      StringBuilder markdown,
      String label,
      JsonNode section) {
    List<String> endpointIds = stringValues(section.path("endpoint_ids"));
    markdown.append("- ")
        .append(label)
        .append(": status ")
        .append(code(text(section, "analysis_status")));
    if (endpointIds.isEmpty()) {
      markdown.append("; detected none.\n");
      return;
    }
    markdown.append("; detected ")
        .append(endpointIds.size())
        .append(" ID")
        .append(endpointIds.size() == 1 ? "" : "s")
        .append(" ")
        .append(cappedCodeList(endpointIds, MAX_INLINE_BUILD_CONFIG_ITEMS, "endpoint IDs"))
        .append(".\n");
  }

  private void appendOpenApiSpecSummary(
      StringBuilder markdown,
      JsonNode openapi,
      Map<String, ModuleInfo> moduleById,
      Map<String, EvidenceRecord> evidenceById) {
    JsonNode specFiles = openapi.path("spec_files");
    JsonNode specItems = specFiles.path("items");
    markdown.append("- OpenAPI/Swagger spec files: status ")
        .append(code(text(specFiles, "analysis_status")));
    if (!specItems.isArray() || specItems.isEmpty()) {
      markdown.append("; detected none.\n");
    } else {
      markdown.append("; detected ")
          .append(specItems.size())
          .append(" local spec file")
          .append(specItems.size() == 1 ? "" : "s")
          .append(" as declared API inputs.\n");
      int visibleCount = Math.min(specItems.size(), MAX_INLINE_BUILD_CONFIG_ITEMS);
      for (int index = 0; index < visibleCount; index++) {
        JsonNode spec = specItems.get(index);
        markdown.append("  - Spec file: ")
            .append(code(text(spec, "spec_path")))
            .append(" kind ")
            .append(code(text(spec, "spec_kind")))
            .append(", format ")
            .append(code(text(spec, "format")))
            .append(", version ")
            .append(code(nullDisplay(nullableText(spec, "version"))))
            .append(".\n");
        appendModuleLine(markdown, spec, moduleById);
        appendEvidenceLine(markdown, spec.path("evidence_ids"), evidenceById);
      }
      appendOmittedBuildConfigItems(markdown, specItems.size() - visibleCount, "OpenAPI/Swagger spec files");
    }

    JsonNode operations = openapi.path("operations");
    JsonNode operationItems = operations.path("items");
    markdown.append("- OpenAPI/Swagger operations: status ")
        .append(code(text(operations, "analysis_status")));
    if (!operationItems.isArray() || operationItems.isEmpty()) {
      if ("analyzed".equals(text(operations, "analysis_status"))) {
        markdown.append("; detected no declared operation facts.\n");
      } else if ("not_analyzed".equals(text(operations, "analysis_status"))) {
        markdown.append("; no operation facts are emitted until the dedicated operation parser runs.\n");
      } else {
        markdown.append("; detected none.\n");
      }
      return;
    }

    markdown.append("; detected ")
        .append(operationItems.size())
        .append(" spec-backed declared operation")
        .append(operationItems.size() == 1 ? "" : "s")
        .append(".\n");
    int visibleCount = Math.min(operationItems.size(), MAX_INLINE_BUILD_CONFIG_ITEMS);
    for (int index = 0; index < visibleCount; index++) {
      JsonNode operation = operationItems.get(index);
      markdown.append("  - Declared operation: ")
          .append(code(text(operation, "http_method") + " " + text(operation, "path")))
          .append(" from ")
          .append(code(text(operation, "spec_path")))
          .append(", operationId ")
          .append(code(nullDisplay(redactedNullableText(operation, "operation_id"))))
          .append(", tags ")
          .append(codeList(stringValues(operation.path("tags"))))
          .append(", implementation_status ")
          .append(code(text(operation, "implementation_status")))
          .append(".\n");
      appendModuleLine(markdown, operation, moduleById);
      appendEvidenceLine(markdown, operation.path("evidence_ids"), evidenceById);
    }
    appendOmittedBuildConfigItems(
        markdown,
        operationItems.size() - visibleCount,
        "OpenAPI/Swagger operation facts");
  }

  private void appendApiSurfaceWarningSummary(
      StringBuilder markdown,
      String label,
      JsonNode section) {
    List<String> warningIds = stringValues(section.path("warning_ids"));
    markdown.append("- ")
        .append(label)
        .append(": status ")
        .append(code(text(section, "analysis_status")));
    if (warningIds.isEmpty()) {
      markdown.append("; detected none.\n");
      return;
    }
    markdown.append("; referenced ")
        .append(warningIds.size())
        .append(" warning ID")
        .append(warningIds.size() == 1 ? "" : "s")
        .append(" ")
        .append(cappedCodeList(warningIds, MAX_INLINE_BUILD_CONFIG_ITEMS, "warning IDs"))
        .append(".\n");
  }

  private void appendSpringApplicationSurface(
      StringBuilder markdown,
      JsonNode springApplicationSurface,
      JsonNode warnings,
      Map<String, ModuleInfo> moduleById,
      Map<String, EvidenceRecord> evidenceById) {
    if (!springApplicationSurface.isObject()) {
      return;
    }

    Map<String, SpringSurfaceModuleGroup> groups = springSurfaceModuleGroups(
        springApplicationSurface,
        warnings,
        moduleById);
    String normalSection = renderSpringApplicationSurfaceSection(
        springApplicationSurface,
        groups,
        moduleById,
        evidenceById,
        false);
    if (!isLargeDetailedSection(normalSection, springSurfacePrimaryDetailRowCount(groups))) {
      markdown.append(normalSection);
      return;
    }
    markdown.append(renderSpringApplicationSurfaceSection(
        springApplicationSurface,
        groups,
        moduleById,
        evidenceById,
        true));
  }

  private String renderSpringApplicationSurfaceSection(
      JsonNode springApplicationSurface,
      Map<String, SpringSurfaceModuleGroup> groups,
      Map<String, ModuleInfo> moduleById,
      Map<String, EvidenceRecord> evidenceById,
      boolean largeMode) {
    StringBuilder markdown = new StringBuilder();
    markdown.append("## Spring Application Surface\n\n");
    markdown.append("- Spring application surface analysis status: ")
        .append(code(text(springApplicationSurface, "analysis_status")))
        .append("\n");
    markdown.append("- Repository stereotype entries are direct `@Repository` annotation observations; they do not prove runtime bean registration or entity ownership.\n");
    markdown.append("- Spring Data repository interface entries are inferred source-visible extension signals; repository/entity relation rows, when present, are inferred generic links. They do not prove runtime repositories, query method behavior, database access, or runtime repository/entity verification.\n");
    markdown.append("- Configuration classes, configuration-properties types, and `@Bean` methods are source-visible Spring configuration signals; they do not prove runtime bean graphs, binding success, config values, bean scopes, lifecycle, proxy behavior, or dependency graphs.\n");
    markdown.append("- Transaction, scheduled, event listener, and messaging listener entries are source-visible operational change-surface signals; they do not prove runtime transaction behavior, scheduler registration, event delivery, message destinations, or broker topology.\n");
    markdown.append("- Spring Security configuration warnings are inspection hints and change-risk signals; they do not prove security policy, endpoint protection, authentication behavior, authorization behavior, vulnerability, or correctness.\n");
    appendSpringSurfaceStatusSummary(markdown, springApplicationSurface);
    List<SpringSurfaceModuleGroup> visibleGroups = springSurfaceVisibleGroups(groups);
    if (largeMode && !visibleGroups.isEmpty()) {
      appendLargeSpringSurfaceSummary(markdown, visibleGroups, moduleById);
    }
    appendSpringSurfaceModuleGroups(markdown, visibleGroups, moduleById, evidenceById, largeMode);
    markdown.append("\n");
    return markdown.toString();
  }

  private void appendSpringSurfaceStatusSummary(
      StringBuilder markdown,
      JsonNode springApplicationSurface) {
    List<String> statuses = List.of(
        springSurfaceStatusLabel("repositories", springApplicationSurface.path("repositories")),
        springSurfaceStatusLabel(
            "configuration classes",
            springApplicationSurface.path("configuration").path("configuration_classes")),
        springSurfaceStatusLabel(
            "configuration properties",
            springApplicationSurface.path("configuration").path("configuration_properties")),
        springSurfaceStatusLabel(
            "bean methods",
            springApplicationSurface.path("configuration").path("bean_methods")),
        springSurfaceStatusLabel(
            "transaction boundaries",
            springApplicationSurface.path("behavior").path("transaction_boundaries")),
        springSurfaceStatusLabel(
            "scheduled methods",
            springApplicationSurface.path("behavior").path("scheduled_methods")),
        springSurfaceStatusLabel(
            "event listeners",
            springApplicationSurface.path("behavior").path("event_listeners")),
        springSurfaceStatusLabel(
            "messaging listeners",
            springApplicationSurface.path("messaging").path("listener_signals")),
        springSurfaceStatusLabel(
            "security warnings",
            springApplicationSurface.path("security").path("configuration_warnings")));
    markdown.append("- Subsection statuses: ")
        .append(String.join(", ", statuses))
        .append(".\n");
  }

  private String springSurfaceStatusLabel(String label, JsonNode section) {
    return label + " " + code(text(section, "analysis_status"));
  }

  private void appendSpringSurfaceModuleGroups(
      StringBuilder markdown,
      List<SpringSurfaceModuleGroup> visibleGroups,
      Map<String, ModuleInfo> moduleById,
      Map<String, EvidenceRecord> evidenceById,
      boolean largeMode) {
    if (visibleGroups.isEmpty()) {
      markdown.append("- Spring application surface facts: detected none for supported modules.\n");
      return;
    }

    List<SpringSurfaceModuleGroup> detailedGroups = visibleGroups;
    if (largeMode) {
      detailedGroups = prioritizedRows(visibleGroups, this::largeSpringModulePriority);
      detailedGroups = detailedGroups.subList(
          0,
          Math.min(detailedGroups.size(), MAX_LARGE_SPRING_DETAILED_MODULES));
    }

    for (SpringSurfaceModuleGroup group : detailedGroups) {
      markdown.append("\n### ")
          .append(springSurfaceModuleHeading(group.moduleId(), moduleById))
          .append("\n\n");
      appendSpringExtractedFacts(markdown, group.extractedFacts(), evidenceById);
      appendSpringInferredSignals(markdown, group.inferredSignals(), evidenceById);
      appendSpringInferredRelations(markdown, group.inferredRelations(), evidenceById);
      appendSpringUncertainStatuses(markdown, group.uncertainStatuses(), evidenceById);
      appendSpringWarningFacts(markdown, group.warningFacts(), evidenceById);
    }
    appendOmittedRows(
        markdown,
        "  ",
        visibleGroups.size() - detailedGroups.size(),
        "Spring application surface module groups",
        "spring_application_surface");
  }

  private Map<String, SpringSurfaceModuleGroup> springSurfaceModuleGroups(
      JsonNode springApplicationSurface,
      JsonNode warnings,
      Map<String, ModuleInfo> moduleById) {
    Map<String, SpringSurfaceModuleGroup> groups = new LinkedHashMap<>();
    for (String moduleId : moduleById.keySet()) {
      groups.put(moduleId, new SpringSurfaceModuleGroup(moduleId));
    }

    for (JsonNode repository : items(springApplicationSurface.path("repositories"))) {
      if ("inferred".equals(text(repository, "support_type"))) {
        groupFor(groups, nullableText(repository, "module_id")).inferredSignals().add(repository);
      } else {
        groupFor(groups, nullableText(repository, "module_id")).extractedFacts().add(repository);
      }
      String entityRelationStatus = nullableText(repository, "entity_relation_status");
      if (entityRelationStatus != null && !entityRelationStatus.isBlank()) {
        if ("inferred".equals(entityRelationStatus)
            && repository.path("entity_relation").isObject()) {
          groupFor(groups, nullableText(repository, "module_id")).inferredRelations().add(repository);
        } else {
          groupFor(groups, nullableText(repository, "module_id"))
              .uncertainStatuses()
              .add(new SpringSurfaceUncertainStatus(
                  springSurfaceFactTarget(repository),
                  "entity_relation_status",
                  entityRelationStatus,
                  "no runtime repository/entity relation is claimed",
                  stringValues(repository.path("evidence_ids"))));
        }
      }
    }

    addExtractedFacts(
        groups,
        items(springApplicationSurface.path("configuration").path("configuration_classes")));
    for (JsonNode properties : items(
        springApplicationSurface.path("configuration").path("configuration_properties"))) {
      groupFor(groups, nullableText(properties, "module_id")).extractedFacts().add(properties);
      String bindingStatus = nullableText(properties, "binding_status");
      if (bindingStatus != null && !bindingStatus.isBlank()) {
        groupFor(groups, nullableText(properties, "module_id"))
            .uncertainStatuses()
            .add(new SpringSurfaceUncertainStatus(
                springSurfaceFactTarget(properties),
                "binding_status",
                bindingStatus,
                "no runtime binding success or config values are claimed",
                stringValues(properties.path("evidence_ids"))));
      }
    }
    for (JsonNode beanMethod : items(
        springApplicationSurface.path("configuration").path("bean_methods"))) {
      groupFor(groups, nullableText(beanMethod, "module_id")).extractedFacts().add(beanMethod);
      String beanNameStatus = nullableText(beanMethod, "bean_name_status");
      if (beanNameStatus != null && !beanNameStatus.isBlank()) {
        groupFor(groups, nullableText(beanMethod, "module_id"))
            .uncertainStatuses()
            .add(new SpringSurfaceUncertainStatus(
                springSurfaceFactTarget(beanMethod),
                "bean_name_status",
                beanNameStatus,
                "no effective runtime bean name is claimed",
                stringValues(beanMethod.path("evidence_ids"))));
      }
    }

    addExtractedFacts(
        groups,
        items(springApplicationSurface.path("behavior").path("transaction_boundaries")));
    addExtractedFacts(
        groups,
        items(springApplicationSurface.path("behavior").path("scheduled_methods")));
    addExtractedFacts(
        groups,
        items(springApplicationSurface.path("behavior").path("event_listeners")));
    addExtractedFacts(
        groups,
        items(springApplicationSurface.path("messaging").path("listener_signals")));

    Map<String, JsonNode> warningById = warningById(warnings);
    for (String warningId : stringValues(
        springApplicationSurface.path("security").path("configuration_warnings").path("warning_ids"))) {
      JsonNode warning = warningById.get(warningId);
      String moduleId = warning == null ? null : nullableText(warning, "module_id");
      groupFor(groups, moduleId).warningFacts().add(new SpringSurfaceWarningFact(warningId, warning));
    }

    return groups;
  }

  private void addExtractedFacts(
      Map<String, SpringSurfaceModuleGroup> groups,
      List<JsonNode> facts) {
    for (JsonNode fact : facts) {
      groupFor(groups, nullableText(fact, "module_id")).extractedFacts().add(fact);
    }
  }

  private SpringSurfaceModuleGroup groupFor(
      Map<String, SpringSurfaceModuleGroup> groups,
      String moduleId) {
    String key = moduleId == null ? "" : moduleId;
    SpringSurfaceModuleGroup group = groups.get(key);
    if (group == null) {
      group = new SpringSurfaceModuleGroup(key);
      groups.put(key, group);
    }
    return group;
  }

  private List<JsonNode> items(JsonNode section) {
    JsonNode items = section.path("items");
    if (!items.isArray() || items.isEmpty()) {
      return List.of();
    }
    List<JsonNode> values = new ArrayList<>();
    for (JsonNode item : items) {
      values.add(item);
    }
    return List.copyOf(values);
  }

  private String springSurfaceModuleHeading(
      String moduleId,
      Map<String, ModuleInfo> moduleById) {
    if (moduleId == null || moduleId.isBlank()) {
      return "Module not recorded";
    }
    return "Module " + moduleLabel(moduleId, moduleById);
  }

  private void appendSpringExtractedFacts(
      StringBuilder markdown,
      List<JsonNode> facts,
      Map<String, EvidenceRecord> evidenceById) {
    markdown.append("- Extracted facts: ");
    if (facts.isEmpty()) {
      markdown.append("detected none.\n");
      return;
    }
    markdown.append("detected ")
        .append(facts.size())
        .append(" source-visible fact")
        .append(facts.size() == 1 ? "" : "s")
        .append(".\n");

    int visibleCount = Math.min(facts.size(), MAX_INLINE_BUILD_CONFIG_ITEMS);
    for (int index = 0; index < visibleCount; index++) {
      JsonNode fact = facts.get(index);
      markdown.append("  - ")
          .append(code(text(fact, "surface_category")))
          .append(": ")
          .append(springSurfaceFactDescription(fact))
          .append("\n");
      appendSpringSurfaceSourceLine(markdown, fact);
      appendNestedEvidenceLine(markdown, fact.path("evidence_ids"), evidenceById);
    }
    appendOmittedBuildConfigItems(markdown, facts.size() - visibleCount, "Spring application surface extracted facts");
  }

  private void appendSpringInferredSignals(
      StringBuilder markdown,
      List<JsonNode> signals,
      Map<String, EvidenceRecord> evidenceById) {
    markdown.append("- Inferred signals: ");
    if (signals.isEmpty()) {
      markdown.append("detected none.\n");
      return;
    }
    markdown.append("detected ")
        .append(signals.size())
        .append(" source-visible signal")
        .append(signals.size() == 1 ? "" : "s")
        .append(".\n");

    int visibleCount = Math.min(signals.size(), MAX_INLINE_BUILD_CONFIG_ITEMS);
    for (int index = 0; index < visibleCount; index++) {
      JsonNode signal = signals.get(index);
      markdown.append("  - ")
          .append(code(text(signal, "surface_category")))
          .append(": ")
          .append(code(text(signal, "class_name")))
          .append(" extends ")
          .append(codeList(stringValues(signal.path("extends_types"))))
          .append(" (support_type: ")
          .append(code(text(signal, "support_type")))
          .append(", repository_signal: ")
          .append(code(text(signal, "repository_signal")))
          .append(").\n");
      appendSpringSurfaceSourceLine(markdown, signal);
      appendNestedEvidenceLine(markdown, signal.path("evidence_ids"), evidenceById);
    }
    appendOmittedBuildConfigItems(markdown, signals.size() - visibleCount, "Spring application surface inferred signals");
  }

  private void appendSpringInferredRelations(
      StringBuilder markdown,
      List<JsonNode> repositories,
      Map<String, EvidenceRecord> evidenceById) {
    if (repositories.isEmpty()) {
      return;
    }
    markdown.append("- Inferred repository/entity relations: detected ")
        .append(repositories.size())
        .append(" source-visible Spring Data generic relation")
        .append(repositories.size() == 1 ? "" : "s")
        .append(".\n");

    int visibleCount = Math.min(repositories.size(), MAX_INLINE_BUILD_CONFIG_ITEMS);
    for (int index = 0; index < visibleCount; index++) {
      JsonNode repository = repositories.get(index);
      JsonNode relation = repository.path("entity_relation");
      String uncertainty = nullableText(relation, "uncertainty");
      markdown.append("  - ")
          .append(code(text(repository, "class_name")))
          .append(" -> ")
          .append(code(text(relation, "target_class_name")))
          .append(" (entity_relation_status: ")
          .append(code(text(repository, "entity_relation_status")))
          .append(", relation_type: ")
          .append(code(text(relation, "relation_type")))
          .append(", support_type: ")
          .append(code(text(relation, "support_type")))
          .append(", generic_type: ")
          .append(code(text(relation, "generic_type")))
          .append(", confidence: ")
          .append(code(text(relation, "confidence")))
          .append(", uncertainty: ")
          .append(code(uncertainty == null ? "null" : uncertainty))
          .append(").\n");
      appendNestedEvidenceLine(markdown, relation.path("evidence_ids"), evidenceById);
    }
    appendOmittedBuildConfigItems(
        markdown,
        repositories.size() - visibleCount,
        "Spring application surface inferred repository/entity relations");
  }

  private void appendSpringUncertainStatuses(
      StringBuilder markdown,
      List<SpringSurfaceUncertainStatus> statuses,
      Map<String, EvidenceRecord> evidenceById) {
    markdown.append("- Uncertain/not-analyzed statuses: ");
    if (statuses.isEmpty()) {
      markdown.append("detected none.\n");
      return;
    }
    markdown.append("detected ")
        .append(statuses.size())
        .append(" explicit status")
        .append(statuses.size() == 1 ? "" : "es")
        .append(".\n");

    int visibleCount = Math.min(statuses.size(), MAX_INLINE_BUILD_CONFIG_ITEMS);
    for (int index = 0; index < visibleCount; index++) {
      SpringSurfaceUncertainStatus status = statuses.get(index);
      markdown.append("  - ")
          .append(code(status.target()))
          .append(": ")
          .append(code(status.fieldName()))
          .append(" is ")
          .append(code(status.status()))
          .append("; ")
          .append(MarkdownRenderer.text(status.reason()))
          .append(".\n");
      appendNestedEvidenceLine(markdown, status.evidenceIds(), evidenceById);
    }
    appendOmittedBuildConfigItems(markdown, statuses.size() - visibleCount, "Spring application surface not-analyzed statuses");
  }

  private void appendSpringWarningFacts(
      StringBuilder markdown,
      List<SpringSurfaceWarningFact> warningFacts,
      Map<String, EvidenceRecord> evidenceById) {
    markdown.append("- Warnings: ");
    if (warningFacts.isEmpty()) {
      markdown.append("detected none.\n");
      return;
    }
    markdown.append("referenced ")
        .append(warningFacts.size())
        .append(" inspection hint/change-risk warning")
        .append(warningFacts.size() == 1 ? "" : "s")
        .append(".\n");

    int visibleCount = Math.min(warningFacts.size(), MAX_INLINE_BUILD_CONFIG_ITEMS);
    for (int index = 0; index < visibleCount; index++) {
      SpringSurfaceWarningFact warningFact = warningFacts.get(index);
      JsonNode warning = warningFact.warning();
      if (warning == null) {
        markdown.append("  - Spring Security warning: Referenced warning ID ")
            .append(code(warningFact.warningId()))
            .append(" was not found in `warnings.items`.\n");
        continue;
      }
      markdown.append("  - Warning ")
          .append(code(text(warning, "category")))
          .append(": inspection hint ")
          .append(code(text(warning, "signal")))
          .append(" (warning_id: ")
          .append(code(warningFact.warningId()))
          .append(")")
          .append(" at ")
          .append(code(text(warning, "source_path")))
          .append(". ")
          .append(MarkdownRenderer.text(text(warning, "message")))
          .append("\n");
      appendNestedEvidenceLine(markdown, warning.path("evidence_ids"), evidenceById);
    }
    appendOmittedBuildConfigItems(markdown, warningFacts.size() - visibleCount, "Spring application surface warnings");
  }

  private void appendSpringSurfaceSourceLine(StringBuilder markdown, JsonNode fact) {
    markdown.append("    - Source: ")
        .append(code(text(fact, "source_path")))
        .append("\n");
  }

  private String springSurfaceFactDescription(JsonNode fact) {
    String supportType = text(fact, "support_type");
    String target = springSurfaceFactTarget(fact);
    StringBuilder description = new StringBuilder();
    description.append(code(target))
        .append(" (support_type: ")
        .append(code(supportType));
    appendPresentFactField(description, fact, "repository_signal");
    appendPresentFactField(description, fact, "configuration_signal");
    appendPresentFactField(description, fact, "configuration_properties_signal");
    appendPresentFactField(description, fact, "bean_signal");
    appendPresentFactField(description, fact, "transaction_signal");
    appendPresentFactField(description, fact, "scheduled_signal");
    appendPresentFactField(description, fact, "event_listener_signal");
    appendPresentFactField(description, fact, "listener_signal");
    appendPresentFactField(description, fact, "target_kind");
    appendPresentFactField(description, fact, "annotation_symbol");
    appendPresentFactField(description, fact, "listener_framework");
    description.append(").");
    return description.toString();
  }

  private void appendPresentFactField(
      StringBuilder description,
      JsonNode fact,
      String fieldName) {
    String value = nullableText(fact, fieldName);
    if (value == null || value.isBlank()) {
      return;
    }
    description.append(", ")
        .append(fieldName)
        .append(": ")
        .append(code(value));
  }

  private String springSurfaceFactTarget(JsonNode fact) {
    String methodName = nullableText(fact, "method_name");
    if (methodName == null || methodName.isBlank()) {
      return text(fact, "class_name");
    }
    return text(fact, "class_name") + "#" + methodName;
  }

  private Map<String, JsonNode> warningById(JsonNode warnings) {
    JsonNode items = warnings.path("items");
    if (!items.isArray() || items.isEmpty()) {
      return Map.of();
    }

    Map<String, JsonNode> warningById = new LinkedHashMap<>();
    for (JsonNode warning : items) {
      warningById.put(text(warning, "id"), warning);
    }
    return warningById;
  }

  private void appendBuildConfigWarningSummary(
      StringBuilder markdown,
      JsonNode warnings,
      String moduleId) {
    JsonNode items = warnings.path("items");
    if (!items.isArray() || items.isEmpty()) {
      markdown.append("- Module warnings: Detected none.\n");
      return;
    }

    List<String> warningSignals = new ArrayList<>();
    for (JsonNode warning : items) {
      if (!moduleId.equals(nullableText(warning, "module_id"))) {
        continue;
      }
      warningSignals.add(text(warning, "category") + ":" + text(warning, "signal"));
    }
    if (warningSignals.isEmpty()) {
      markdown.append("- Module warnings: Detected none.\n");
      return;
    }
    markdown.append("- Module warnings: Detected ")
        .append(warningSignals.size())
        .append(" warning signal")
        .append(warningSignals.size() == 1 ? "" : "s")
        .append(" for this module: ")
        .append(cappedCodeList(warningSignals, MAX_INLINE_BUILD_CONFIG_ITEMS, "warning signals"))
        .append(". See `Detailed Known Uncertainty And Limits` for warning evidence and messages.\n");
  }

  private void appendEndpoints(
      StringBuilder markdown,
      JsonNode endpoints,
      Map<String, ModuleInfo> moduleById,
      Map<String, EvidenceRecord> evidenceById) {
    markdown.append("## Detected Spring MVC Endpoints\n\n");

    if (!endpoints.isArray() || endpoints.isEmpty()) {
      markdown.append("- Detected: no Spring MVC endpoints recorded in `project-map.json`.\n\n");
      return;
    }

    markdown.append("- Endpoint summary: detected ")
        .append(endpoints.size())
        .append(" source-visible Spring MVC endpoint fact")
        .append(endpoints.size() == 1 ? "" : "s")
        .append(".\n");
    appendLargeSectionWarning(markdown, endpoints.size());
    markdown.append("\n");
    for (JsonNode endpoint : endpoints) {
      markdown.append("### ").append(code(endpointLabel(endpoint))).append("\n\n");
      appendModuleLine(markdown, endpoint, moduleById);
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
      Map<String, ModuleInfo> moduleById,
      Map<String, EvidenceRecord> evidenceById) {
    markdown.append("## Detected Spring Components\n\n");
    markdown.append("- Analysis status: ").append(code(text(components, "analysis_status"))).append("\n");

    JsonNode items = components.path("items");
    if (!items.isArray() || items.isEmpty()) {
      markdown.append("- Detected: no direct Spring stereotype components recorded.\n\n");
      return;
    }

    markdown.append("- Component summary: detected ")
        .append(items.size())
        .append(" direct Spring stereotype component")
        .append(items.size() == 1 ? "" : "s")
        .append(".\n");
    appendLargeSectionWarning(markdown, items.size());
    markdown.append("\n");
    for (JsonNode component : items) {
      markdown.append("### ").append(code(text(component, "class_name"))).append("\n\n");
      appendModuleLine(markdown, component, moduleById);
      markdown.append("- Stereotypes: Detected ")
          .append(codeList(stringValues(component.path("stereotypes"))))
          .append("\n");
      appendEvidenceLine(markdown, component.path("evidence_ids"), evidenceById);
      markdown.append("\n");
    }
  }

  private void appendEntities(
      StringBuilder markdown,
      JsonNode projectMap,
      Map<String, ModuleInfo> moduleById,
      Map<String, EvidenceRecord> evidenceById) {
    if (!hasDomainGuideContent(projectMap)) {
      return;
    }

    String normalSection = renderEntitiesSection(projectMap, moduleById, evidenceById, false);
    if (!isLargeDetailedSection(normalSection, domainPrimaryDetailRowCount(projectMap))) {
      markdown.append(normalSection);
      return;
    }
    markdown.append(renderEntitiesSection(projectMap, moduleById, evidenceById, true));
  }

  private String renderEntitiesSection(
      JsonNode projectMap,
      Map<String, ModuleInfo> moduleById,
      Map<String, EvidenceRecord> evidenceById,
      boolean largeMode) {
    StringBuilder markdown = new StringBuilder();
    JsonNode entities = projectMap.path("entities");
    markdown.append("## Domain And Data Model\n\n");
    markdown.append("- Analysis status: ").append(code(text(entities, "analysis_status"))).append("\n");
    List<JsonNode> entityItems = items(entities);
    List<JsonNode> embeddableItems = items(entities.path("embeddables"));
    markdown.append("- Domain summary: detected ")
        .append(entityItems.size())
        .append(" JPA entity fact")
        .append(entityItems.size() == 1 ? "" : "s")
        .append(" and ")
        .append(embeddableItems.size())
        .append(" embeddable fact")
        .append(embeddableItems.size() == 1 ? "" : "s")
        .append(".\n");
    boolean hasDomainData = !entityItems.isEmpty() || !embeddableItems.isEmpty();
    if (hasDomainData) {
      markdown.append("- Domain/data facts are source-visible JPA annotations and Spring Data generic ")
          .append("signals only; no database schema, runtime Hibernate metadata, migration ")
          .append("interpretation, or provider defaults are claimed.\n");
      markdown.append("- Extracted entity, field, identifier, embeddable, and relationship facts stay ")
          .append("separate from inferred repository/entity links, uncertain relationship targets, ")
          .append("and explicit not-analyzed composite-id/runtime boundaries.\n");
    }

    List<JsonNode> visibleEntities = entityItems;
    List<JsonNode> visibleEmbeddables = embeddableItems;
    if (largeMode) {
      Set<String> relationTargets = inferredRepositoryEntityTargetClasses(projectMap);
      visibleEntities = prioritizedRows(
          entityItems,
          entity -> largeDomainEntityPriority(entity, relationTargets));
      visibleEntities = visibleEntities.subList(
          0,
          Math.min(visibleEntities.size(), MAX_LARGE_DOMAIN_ENTITIES));
      visibleEmbeddables = visibleEmbeddables.subList(
          0,
          Math.min(visibleEmbeddables.size(), MAX_LARGE_DOMAIN_EMBEDDABLES));
      appendLargeDomainSummary(markdown, entityItems, embeddableItems, moduleById);
      markdown.append("- Domain detail cap: showing ")
          .append(visibleEntities.size())
          .append(" of ")
          .append(entityItems.size())
          .append(" entity rows and ")
          .append(visibleEmbeddables.size())
          .append(" of ")
          .append(embeddableItems.size())
          .append(" embeddable rows. Domain rows are source-visible JPA presentation ")
          .append("only; omitted field, identifier, relationship, and embeddable ")
          .append("details remain in `project-map.json`.\n");
      appendLargeSectionEvidenceVisibility(markdown);
      appendOmittedRows(
          markdown,
          "  ",
          entityItems.size() - visibleEntities.size(),
          "entity detail rows",
          "entities.items");
      appendOmittedRows(
          markdown,
          "  ",
          embeddableItems.size() - visibleEmbeddables.size(),
          "embeddable detail rows",
          "entities.embeddables.items");
    }

    if (entityItems.isEmpty()) {
      markdown.append("- Detected: no direct JPA entities recorded.\n\n");
    } else {
      markdown.append("\n");
      for (JsonNode entity : visibleEntities) {
        markdown.append("### ").append(code(text(entity, "class_name"))).append("\n\n");
        appendModuleLine(markdown, entity, moduleById);
        String tableName = nullableText(entity, "table_name");
        List<String> tableEvidenceIds = evidenceIdsWithSymbol(
            entity.path("evidence_ids"),
            evidenceById,
            "@Table");
        List<String> entityEvidenceIds = evidenceIdsWithoutSymbols(
            entity.path("evidence_ids"),
            evidenceById,
            Set.of("@Table", "@IdClass"));
        markdown.append("- Entity: Detected ")
            .append(code(text(entity, "class_name")))
            .append("\n");
        appendEvidenceLine(markdown, entityEvidenceIds, evidenceById);
        appendNullableDetectedLine(markdown, "Table", tableName);
        if (tableName != null && !tableName.isBlank()) {
          appendEvidenceLine(markdown, tableEvidenceIds, evidenceById);
        }
        appendIdClass(markdown, entity.path("id_class"), evidenceById);
        appendEntityFields(markdown, entity.path("fields"), evidenceById, largeMode);
        appendIdentifierFields(
            markdown,
            entity.path("identifier_fields"),
            evidenceById,
            largeMode);
        appendRelationships(markdown, entity.path("relationships"), evidenceById, largeMode);
        markdown.append("\n");
      }
    }
    appendEmbeddables(
        markdown,
        entities.path("embeddables"),
        visibleEmbeddables,
        embeddableItems.size(),
        moduleById,
        evidenceById,
        largeMode);
    return markdown.toString();
  }

  private void appendIdClass(
      StringBuilder markdown,
      JsonNode idClass,
      Map<String, EvidenceRecord> evidenceById) {
    if (!idClass.isObject()) {
      return;
    }

    markdown.append("- IdClass signal: Source-visible type ")
        .append(code(nullDisplay(nullableText(idClass, "type_name"))))
        .append(" with field_matching_status ")
        .append(code(text(idClass, "field_matching_status")))
        .append(" and semantic_reconstruction_status ")
        .append(code(text(idClass, "semantic_reconstruction_status")))
        .append("\n");
    appendEvidenceLine(markdown, idClass.path("evidence_ids"), evidenceById);
  }

  private void appendEmbeddables(
      StringBuilder markdown,
      JsonNode embeddables,
      List<JsonNode> visibleItems,
      int totalItemCount,
      Map<String, ModuleInfo> moduleById,
      Map<String, EvidenceRecord> evidenceById,
      boolean largeMode) {
    if (!embeddables.isObject()) {
      return;
    }

    markdown.append("### Embeddables\n\n");
    markdown.append("- Analysis status: ")
        .append(code(text(embeddables, "analysis_status")))
        .append("\n");
    if (totalItemCount == 0) {
      markdown.append("- Detected: no direct `@Embeddable` classes recorded.\n\n");
      return;
    }

    for (JsonNode embeddable : visibleItems) {
      markdown.append("- Embeddable: Detected ")
          .append(code(text(embeddable, "class_name")))
          .append("\n");
      appendModuleLine(markdown, embeddable, moduleById);
      markdown.append("  - Source: Detected ")
          .append(code(text(embeddable, "source_path")))
          .append("\n");
      appendEvidenceLine(markdown, embeddable.path("evidence_ids"), evidenceById);
      appendEntityFields(markdown, embeddable.path("fields"), evidenceById, largeMode);
    }
    appendOmittedRows(
        markdown,
        "  ",
        totalItemCount - visibleItems.size(),
        "embeddable rows",
        "entities.embeddables.items");
    markdown.append("\n");
  }

  private void appendEntityFields(
      StringBuilder markdown,
      JsonNode fields,
      Map<String, EvidenceRecord> evidenceById,
      boolean largeMode) {
    if (!fields.isArray() || fields.isEmpty()) {
      markdown.append("- Field metadata: Detected none.\n");
      return;
    }

    int visibleCount = visibleNestedCount(fields, largeMode, MAX_LARGE_DOMAIN_FIELDS_PER_TYPE);
    for (int index = 0; index < visibleCount; index++) {
      JsonNode field = fields.get(index);
      markdown.append("- Field metadata: Source-visible ")
          .append(code(text(field, "field_name")))
          .append(" (")
          .append(code(text(field, "java_type")))
          .append(") role ")
          .append(code(text(field, "persistence_role")))
          .append(" annotations ")
          .append(codeList(stringValues(field.path("annotations"))))
          .append("\n");
      appendColumnAttributes(markdown, field.path("column"));
      appendEnumeratedAttributes(markdown, field.path("enumerated"));
      appendGeneratedValueAttributes(markdown, field.path("generated_value"));
      appendVersionAttributes(markdown, field.path("version"));
      appendEmbeddedAttributes(markdown, field.path("embedded"));
      appendEvidenceLine(markdown, field.path("evidence_ids"), evidenceById);
    }
    appendOmittedRows(
        markdown,
        "  ",
        fields.size() - visibleCount,
        "field metadata rows for this owning type",
        "owning entity or embeddable object");
  }

  private void appendColumnAttributes(StringBuilder markdown, JsonNode column) {
    if (!column.isObject()) {
      return;
    }

    List<String> attributes = new ArrayList<>();
    addNullableAttribute(attributes, "name", nullableText(column, "name"));
    addNullableAttribute(attributes, "nullable", nullableText(column, "nullable"));
    addNullableAttribute(attributes, "unique", nullableText(column, "unique"));
    addNullableAttribute(attributes, "length", nullableText(column, "length"));
    addNullableAttribute(attributes, "precision", nullableText(column, "precision"));
    addNullableAttribute(attributes, "scale", nullableText(column, "scale"));
    addNullableAttribute(attributes, "insertable", nullableText(column, "insertable"));
    addNullableAttribute(attributes, "updatable", nullableText(column, "updatable"));
    markdown.append("  - Column attributes: Source-visible ")
        .append(codeList(attributes))
        .append("\n");
  }

  private void appendEnumeratedAttributes(StringBuilder markdown, JsonNode enumerated) {
    if (!enumerated.isObject()) {
      return;
    }

    markdown.append("  - Enumerated value: Source-visible ")
        .append(code(nullDisplay(nullableText(enumerated, "value"))))
        .append("\n");
  }

  private void appendGeneratedValueAttributes(StringBuilder markdown, JsonNode generatedValue) {
    if (!generatedValue.isObject()) {
      return;
    }

    List<String> attributes = new ArrayList<>();
    addNullableAttribute(attributes, "strategy", nullableText(generatedValue, "strategy"));
    addNullableAttribute(attributes, "generator", nullableText(generatedValue, "generator"));
    markdown.append("  - Generated value attributes: Source-visible ")
        .append(codeList(attributes))
        .append("\n");
  }

  private void appendVersionAttributes(StringBuilder markdown, JsonNode version) {
    if (!version.isObject()) {
      return;
    }

    markdown.append("  - Version: Source-visible `@Version` presence.\n");
  }

  private void appendEmbeddedAttributes(StringBuilder markdown, JsonNode embedded) {
    if (!embedded.isObject()) {
      return;
    }

    markdown.append("  - Embedded signal: ")
        .append(code(text(embedded, "annotation")))
        .append(" declared type ")
        .append(code(text(embedded, "java_type")))
        .append(" target_resolution ")
        .append(code(text(embedded, "target_resolution")));
    String targetClassName = nullableText(embedded, "target_class_name");
    if (targetClassName != null && !targetClassName.isBlank()) {
      markdown.append(" target_class ")
          .append(code(targetClassName));
    }
    String uncertainty = nullableText(embedded, "uncertainty");
    if (uncertainty != null) {
      markdown.append(" uncertainty ")
          .append(code(uncertainty));
    }
    markdown.append("\n");
  }

  private void addNullableAttribute(List<String> attributes, String name, String value) {
    if (value != null) {
      attributes.add(name + "=" + value);
    }
  }

  private void appendIdentifierFields(
      StringBuilder markdown,
      JsonNode identifierFields,
      Map<String, EvidenceRecord> evidenceById,
      boolean largeMode) {
    if (!identifierFields.isArray() || identifierFields.isEmpty()) {
      markdown.append("- Identifier fields: Detected none.\n");
      return;
    }

    int visibleCount = visibleNestedCount(
        identifierFields,
        largeMode,
        MAX_LARGE_DOMAIN_IDENTIFIERS_PER_ENTITY);
    for (int index = 0; index < visibleCount; index++) {
      JsonNode field = identifierFields.get(index);
      markdown.append("- Identifier field: Detected ")
          .append(code(text(field, "field_name")))
          .append(" (")
          .append(code(text(field, "java_type")))
          .append(")");
      String declaringClass = nullableText(field, "declaring_class");
      String sourceKind = nullableText(field, "source_kind");
      String identifierKind = nullableText(field, "identifier_kind");
      if (declaringClass != null && !declaringClass.isBlank()) {
        markdown.append(" declared by ").append(code(declaringClass));
      }
      if (sourceKind != null && !sourceKind.isBlank()) {
        markdown.append(" with source_kind ").append(code(sourceKind));
      }
      if (identifierKind != null && !identifierKind.isBlank()) {
        markdown.append(" identifier_kind ").append(code(identifierKind));
      }
      markdown.append("\n");
      JsonNode generatedValue = field.path("generated_value");
      if (generatedValue.isObject()) {
        appendGeneratedValueAttributes(markdown, generatedValue);
      }
      appendEvidenceLine(markdown, field.path("evidence_ids"), evidenceById);
    }
    appendOmittedRows(
        markdown,
        "  ",
        identifierFields.size() - visibleCount,
        "identifier rows for this owning entity",
        "owning entity object");
  }

  private void appendRelationships(
      StringBuilder markdown,
      JsonNode relationships,
      Map<String, EvidenceRecord> evidenceById,
      boolean largeMode) {
    if (!relationships.isArray() || relationships.isEmpty()) {
      markdown.append("- Relationships: Detected none.\n");
      return;
    }

    int visibleCount = visibleNestedCount(
        relationships,
        largeMode,
        MAX_LARGE_DOMAIN_RELATIONSHIPS_PER_ENTITY);
    for (int index = 0; index < visibleCount; index++) {
      JsonNode relationship = relationships.get(index);
      JsonNode target = relationship.path("target");
      String targetResolution = text(target, "target_resolution");
      if (targetResolution.isBlank()) {
        targetResolution = text(relationship, "target_resolution");
      }
      String uncertainty = nullableText(target, "uncertainty");
      if (uncertainty == null) {
        uncertainty = nullableText(relationship, "uncertainty");
      }
      markdown.append("- Relationship: Uncertain target for ")
          .append(code(text(relationship, "field_name")))
          .append(" ")
          .append(code(text(relationship, "annotation")))
          .append(" cardinality ")
          .append(code(text(relationship, "cardinality")))
          .append(" declared type ")
          .append(code(text(relationship, "java_type")))
          .append("\n");
      markdown.append("  - target_resolution: ")
          .append(code(targetResolution))
          .append("\n");
      markdown.append("  - uncertainty: ")
          .append(code(nullDisplay(uncertainty)))
          .append("\n");
      appendRelationshipAttributes(markdown, relationship);
      appendRelationshipJoinColumns(markdown, relationship.path("join_columns"), largeMode);
      appendRelationshipJoinTable(markdown, relationship.path("join_table"), largeMode);
      appendEvidenceLine(markdown, relationship.path("evidence_ids"), evidenceById);
    }
    appendOmittedRows(
        markdown,
        "  ",
        relationships.size() - visibleCount,
        "relationship rows for this owning entity",
        "owning entity object");
  }

  private void appendRelationshipAttributes(StringBuilder markdown, JsonNode relationship) {
    List<String> attributes = new ArrayList<>();
    addNullableAttribute(attributes, "mapped_by", nullableText(relationship, "mapped_by"));
    addNullableAttribute(attributes, "ownership_signal", nullableText(relationship, "ownership_signal"));
    addNullableAttribute(attributes, "optional", nullableText(relationship, "optional"));
    addNullableAttribute(attributes, "fetch", nullableText(relationship, "fetch"));
    List<String> cascade = stringValues(relationship.path("cascade"));
    if (!cascade.isEmpty()) {
      attributes.add("cascade=[" + String.join(", ", cascade) + "]");
    }
    addNullableAttribute(attributes, "orphan_removal", nullableText(relationship, "orphan_removal"));
    if (!attributes.isEmpty()) {
      markdown.append("  - Relationship attributes: Source-visible ")
          .append(codeList(attributes))
          .append("\n");
    }
  }

  private void appendRelationshipJoinColumns(
      StringBuilder markdown,
      JsonNode joinColumns,
      boolean largeMode) {
    if (!joinColumns.isArray() || joinColumns.isEmpty()) {
      return;
    }

    int visibleCount = visibleNestedCount(
        joinColumns,
        largeMode,
        MAX_LARGE_DOMAIN_JOIN_COLUMNS_PER_RELATIONSHIP);
    for (int index = 0; index < visibleCount; index++) {
      JsonNode joinColumn = joinColumns.get(index);
      markdown.append("  - Join column: Source-visible ")
          .append(codeList(joinColumnAttributes(joinColumn)))
          .append("\n");
    }
    appendOmittedRows(
        markdown,
        "  ",
        joinColumns.size() - visibleCount,
        "join columns for this relationship",
        "owning relationship object");
  }

  private void appendRelationshipJoinTable(
      StringBuilder markdown,
      JsonNode joinTable,
      boolean largeMode) {
    if (!joinTable.isObject()) {
      return;
    }

    List<String> attributes = new ArrayList<>();
    addNullableAttribute(attributes, "name", nullableText(joinTable, "name"));
    addNullableAttribute(attributes, "schema", nullableText(joinTable, "schema"));
    addNullableAttribute(attributes, "catalog", nullableText(joinTable, "catalog"));
    markdown.append("  - Join table: Source-visible ")
        .append(codeList(attributes))
        .append("\n");
    appendJoinTableColumns(markdown, "join_columns", joinTable.path("join_columns"), largeMode);
    appendJoinTableColumns(
        markdown,
        "inverse_join_columns",
        joinTable.path("inverse_join_columns"),
        largeMode);
  }

  private void appendJoinTableColumns(
      StringBuilder markdown,
      String label,
      JsonNode joinColumns,
      boolean largeMode) {
    if (!joinColumns.isArray() || joinColumns.isEmpty()) {
      return;
    }

    int visibleCount = visibleNestedCount(
        joinColumns,
        largeMode,
        MAX_LARGE_DOMAIN_JOIN_COLUMNS_PER_RELATIONSHIP);
    for (int index = 0; index < visibleCount; index++) {
      JsonNode joinColumn = joinColumns.get(index);
      markdown.append("    - ")
          .append(label)
          .append(": Source-visible ")
          .append(codeList(joinColumnAttributes(joinColumn)))
          .append("\n");
    }
    appendOmittedRows(
        markdown,
        "    ",
        joinColumns.size() - visibleCount,
        label + " rows for this relationship",
        "owning relationship object");
  }

  private List<String> joinColumnAttributes(JsonNode joinColumn) {
    List<String> attributes = new ArrayList<>();
    addNullableAttribute(attributes, "name", nullableText(joinColumn, "name"));
    addNullableAttribute(
        attributes,
        "referenced_column_name",
        nullableText(joinColumn, "referenced_column_name"));
    addNullableAttribute(attributes, "nullable", nullableText(joinColumn, "nullable"));
    addNullableAttribute(attributes, "unique", nullableText(joinColumn, "unique"));
    addNullableAttribute(attributes, "insertable", nullableText(joinColumn, "insertable"));
    addNullableAttribute(attributes, "updatable", nullableText(joinColumn, "updatable"));
    return attributes;
  }

  private void appendTests(
      StringBuilder markdown,
      JsonNode tests,
      Map<String, ModuleInfo> moduleById,
      Map<String, EvidenceRecord> evidenceById) {
    String normalSection = renderTestsSection(tests, moduleById, evidenceById, false);
    if (!isLargeDetailedSection(normalSection, testPrimaryDetailRowCount(tests))) {
      markdown.append(normalSection);
      return;
    }
    markdown.append(renderTestsSection(tests, moduleById, evidenceById, true));
  }

  private String renderTestsSection(
      JsonNode tests,
      Map<String, ModuleInfo> moduleById,
      Map<String, EvidenceRecord> evidenceById,
      boolean largeMode) {
    StringBuilder markdown = new StringBuilder();
    markdown.append("## Detected Tests\n\n");
    String analysisStatus = text(tests, "analysis_status");
    markdown.append("- Analysis status: ").append(code(analysisStatus)).append("\n");

    List<JsonNode> testItems = items(tests);
    if (testItems.isEmpty()) {
      if ("not_detected".equals(analysisStatus)) {
        markdown.append("- Not analyzed: no supported test root was detected.\n\n");
      } else {
        markdown.append("- Detected: no test classes recorded.\n\n");
      }
      return markdown.toString();
    }

    int frameworkSignalCount = 0;
    int springTestSliceCount = 0;
    int mockSignalCount = 0;
    int testMethodCount = 0;
    int testedSubjectCount = 0;
    Map<String, Integer> moduleCounts = seededModuleCountMap(moduleById);
    Map<String, Integer> relationStatusCounts = new LinkedHashMap<>();
    for (JsonNode test : testItems) {
      frameworkSignalCount += arraySize(test.path("framework_signals"));
      springTestSliceCount += arraySize(test.path("spring_test_slices"));
      mockSignalCount += arraySize(test.path("mock_signals"));
      testMethodCount += arraySize(test.path("methods"));
      List<JsonNode> testedSubjects = jsonArrayItems(test.path("tested_subjects"));
      testedSubjectCount += testedSubjects.size();
      incrementCount(moduleCounts, moduleCountKey(nullableText(test, "module_id")));
      for (JsonNode subject : testedSubjects) {
        incrementCount(
            relationStatusCounts,
            textOrFallback(subject, "relation_status", "inferred"));
      }
    }
    markdown.append("- Test inventory summary: detected ")
        .append(testItems.size())
        .append(" test class")
        .append(testItems.size() == 1 ? "" : "es")
        .append(", ")
        .append(frameworkSignalCount)
        .append(" framework signal")
        .append(frameworkSignalCount == 1 ? "" : "s")
        .append(", ")
        .append(springTestSliceCount)
        .append(" Spring test slice signal")
        .append(springTestSliceCount == 1 ? "" : "s")
        .append(", ")
        .append(mockSignalCount)
        .append(" mock signal")
        .append(mockSignalCount == 1 ? "" : "s")
        .append(", ")
        .append(testMethodCount)
        .append(" supported JUnit method")
        .append(testMethodCount == 1 ? "" : "s")
        .append(", and ")
        .append(testedSubjectCount)
        .append(" tested-subject relation/status row")
        .append(testedSubjectCount == 1 ? "" : "s")
        .append(".\n");
    List<JsonNode> visibleTests = testItems;
    if (largeMode) {
      visibleTests = prioritizedRows(testItems, this::largeTestPriority);
      visibleTests = visibleTests.subList(
          0,
          Math.min(visibleTests.size(), MAX_DETAILED_TEST_CLASSES));
      appendLargeSectionSummaryBoundary(markdown);
      markdown.append("- Test large-section summary: modules ")
          .append(countSummary(moduleCounts, moduleById))
          .append("; relation_status counts ")
          .append(countSummary(relationStatusCounts, moduleById))
          .append(".\n");
      markdown.append("- Test detail cap: showing ")
          .append(visibleTests.size())
          .append(" of ")
          .append(testItems.size())
          .append(" test classes. This is a Markdown presentation cap only; omitted ")
          .append("test classes, methods, framework signals, mock signals, and ")
          .append("tested-subject statuses remain in `project-map.json`.\n");
      appendLargeSectionEvidenceVisibility(markdown);
      appendOmittedRows(
          markdown,
          "  ",
          testItems.size() - visibleTests.size(),
          "test classes",
          "tests.items");
    }
    markdown.append("\n");
    for (JsonNode test : visibleTests) {
      markdown.append("### ").append(code(text(test, "class_name"))).append("\n\n");
      appendModuleLine(markdown, test, moduleById);
      markdown.append("- Test class: Detected ")
          .append(code(text(test, "class_name")))
          .append("\n");
      appendEvidenceLine(markdown, test.path("evidence_ids"), evidenceById);
      markdown.append("- Source: Detected ").append(code(text(test, "source_path"))).append("\n");
      appendFrameworkSignals(markdown, test.path("framework_signals"), evidenceById, largeMode);
      appendSpringTestSlices(markdown, test.path("spring_test_slices"), evidenceById, largeMode);
      appendMockSignals(markdown, test.path("mock_signals"), evidenceById, largeMode);
      appendTestMethods(markdown, test.path("methods"), evidenceById, largeMode);
      appendTestedSubjects(
          markdown,
          test.path("tested_subjects"),
          moduleById,
          evidenceById,
          largeMode);
      markdown.append("\n");
    }
    return markdown.toString();
  }

  private void appendFrameworkSignals(
      StringBuilder markdown,
      JsonNode frameworkSignals,
      Map<String, EvidenceRecord> evidenceById,
      boolean largeMode) {
    if (!frameworkSignals.isArray() || frameworkSignals.isEmpty()) {
      markdown.append("- Framework signals: Detected none.\n");
      return;
    }

    int visibleCount = visibleNestedCount(
        frameworkSignals,
        largeMode,
        MAX_LARGE_TEST_FRAMEWORK_SIGNALS_PER_CLASS);
    for (int index = 0; index < visibleCount; index++) {
      JsonNode signal = frameworkSignals.get(index);
      markdown.append("- Framework signal: Detected ")
          .append(code(text(signal, "name")))
          .append(" (signal_kind: ")
          .append(code(text(signal, "signal_kind")))
          .append(")")
          .append("\n");
      appendEvidenceLine(markdown, signal.path("evidence_ids"), evidenceById);
    }
    appendOmittedRows(
        markdown,
        "  ",
        frameworkSignals.size() - visibleCount,
        "framework signals for this test class",
        "tests.items");
  }

  private void appendSpringTestSlices(
      StringBuilder markdown,
      JsonNode springTestSlices,
      Map<String, EvidenceRecord> evidenceById,
      boolean largeMode) {
    if (!springTestSlices.isArray() || springTestSlices.isEmpty()) {
      return;
    }

    int visibleCount = visibleNestedCount(
        springTestSlices,
        largeMode,
        MAX_LARGE_TEST_SLICES_PER_CLASS);
    for (int index = 0; index < visibleCount; index++) {
      JsonNode slice = springTestSlices.get(index);
      markdown.append("- Spring test slice signal: Detected ")
          .append(code(text(slice, "annotation")))
          .append(" (slice_kind: ")
          .append(code(text(slice, "slice_kind")))
          .append(", signal_kind: ")
          .append(code(text(slice, "signal_kind")))
          .append(")")
          .append("\n");
      appendEvidenceLine(markdown, slice.path("evidence_ids"), evidenceById);
    }
    appendOmittedRows(
        markdown,
        "  ",
        springTestSlices.size() - visibleCount,
        "Spring test slice signals for this test class",
        "tests.items");
  }

  private void appendMockSignals(
      StringBuilder markdown,
      JsonNode mockSignals,
      Map<String, EvidenceRecord> evidenceById,
      boolean largeMode) {
    if (!mockSignals.isArray() || mockSignals.isEmpty()) {
      return;
    }

    int visibleCount = visibleNestedCount(
        mockSignals,
        largeMode,
        MAX_LARGE_TEST_MOCK_SIGNALS_PER_CLASS);
    for (int index = 0; index < visibleCount; index++) {
      JsonNode signal = mockSignals.get(index);
      markdown.append("- Mock annotation signal: Detected ")
          .append(code(text(signal, "annotation")))
          .append(" on ")
          .append(code(text(signal, "target_kind")))
          .append(" ")
          .append(code(text(signal, "target_name")))
          .append(" (mock_signal: ")
          .append(code(text(signal, "mock_signal")))
          .append(", signal_kind: ")
          .append(code(text(signal, "signal_kind")))
          .append(")")
          .append("\n");
      appendEvidenceLine(markdown, signal.path("evidence_ids"), evidenceById);
    }
    appendOmittedRows(
        markdown,
        "  ",
        mockSignals.size() - visibleCount,
        "mock signals for this test class",
        "tests.items");
  }

  private void appendTestMethods(
      StringBuilder markdown,
      JsonNode methods,
      Map<String, EvidenceRecord> evidenceById,
      boolean largeMode) {
    if (!methods.isArray() || methods.isEmpty()) {
      markdown.append("- Test methods: Detected none with supported direct JUnit test annotations.\n");
      return;
    }

    int visibleCount = visibleNestedCount(methods, largeMode, MAX_LARGE_TEST_METHODS_PER_CLASS);
    for (int index = 0; index < visibleCount; index++) {
      JsonNode method = methods.get(index);
      markdown.append("- Test method: Detected ")
          .append(code(text(method, "method_name")))
          .append(" annotated with ")
          .append(code(text(method, "test_annotation")))
          .append(" (method_kind: ")
          .append(code(text(method, "method_kind")));
      String displayName = nullableText(method, "display_name");
      if (displayName != null) {
        markdown.append(", display_name: ").append(code(displayName));
      }
      markdown.append(")\n");
      appendEvidenceLine(markdown, method.path("evidence_ids"), evidenceById);
    }
    appendOmittedRows(
        markdown,
        "  ",
        methods.size() - visibleCount,
        "test methods for this test class",
        "tests.items");
  }

  private void appendTestedSubjects(
      StringBuilder markdown,
      JsonNode testedSubjects,
      Map<String, ModuleInfo> moduleById,
      Map<String, EvidenceRecord> evidenceById,
      boolean largeMode) {
    if (!testedSubjects.isArray() || testedSubjects.isEmpty()) {
      markdown.append("- Inferred tested subjects: none recorded.\n");
      return;
    }

    int visibleCount = visibleNestedCount(
        testedSubjects,
        largeMode,
        MAX_LARGE_TESTED_SUBJECTS_PER_CLASS);
    for (int index = 0; index < visibleCount; index++) {
      JsonNode subject = testedSubjects.get(index);
      String relationStatus = text(subject, "relation_status");
      if (relationStatus.isBlank() && nullableText(subject, "support_type") != null) {
        relationStatus = "inferred";
      }
      String relationType = text(subject, "relation_type");
      if (relationType.isBlank()) {
        relationType = "naming_convention";
      }
      String className = nullableText(subject, "class_name");
      if ("inferred".equals(relationStatus)) {
        markdown.append("- Inferred tested subject: ")
            .append(code(className == null ? "unknown" : className));
        String targetModuleId = nullableText(subject, "target_module_id");
        if (targetModuleId != null && !targetModuleId.isBlank()) {
          markdown.append(" in target module ")
              .append(moduleLabel(targetModuleId, moduleById));
        }
      } else if ("ambiguous".equals(relationStatus) && className != null) {
        markdown.append("- Ambiguous tested subject candidate: ")
            .append(code(className));
        String targetModuleId = nullableText(subject, "target_module_id");
        if (targetModuleId != null && !targetModuleId.isBlank()) {
          markdown.append(" in target module ")
              .append(moduleLabel(targetModuleId, moduleById));
        }
      } else {
        markdown.append("- Tested-subject status: ")
            .append(code(relationStatus));
      }
      markdown.append(" (relation_status: ")
          .append(code(relationStatus))
          .append(", relation_type: ")
          .append(code(relationType))
          .append(", support_type: ")
          .append(code(nullableText(subject, "support_type") == null
              ? "null"
              : nullableText(subject, "support_type")))
          .append(", confidence: ")
          .append(code(text(subject, "confidence")));
      String candidateReference = nullableText(subject, "candidate_reference");
      if (candidateReference != null) {
        markdown.append(", candidate_reference: ").append(code(candidateReference));
      }
      String uncertainty = nullableText(subject, "uncertainty");
      if (uncertainty != null) {
        markdown.append(", uncertainty: ").append(code(uncertainty));
      }
      markdown.append(")");
      if (!"inferred".equals(relationStatus)) {
        markdown.append("; no tested-subject coverage or runtime execution relation is claimed");
      }
      markdown.append(".\n");
      appendEvidenceLine(markdown, subject.path("evidence_ids"), evidenceById);
    }
    appendOmittedRows(
        markdown,
        "  ",
        testedSubjects.size() - visibleCount,
        "tested-subject rows for this test class",
        "tests.items");
  }

  private void appendQuality(
      StringBuilder markdown,
      JsonNode quality,
      Map<String, ModuleInfo> moduleById,
      Map<String, EvidenceRecord> evidenceById) {
    if (!quality.isObject() || !hasQualitySignals(quality)) {
      return;
    }

    String normalSection = renderQualitySection(quality, moduleById, evidenceById, false);
    if (!isLargeDetailedSection(normalSection, qualitySignalCount(quality))) {
      markdown.append(normalSection);
      return;
    }
    markdown.append(renderQualitySection(quality, moduleById, evidenceById, true));
  }

  private String renderQualitySection(
      JsonNode quality,
      Map<String, ModuleInfo> moduleById,
      Map<String, EvidenceRecord> evidenceById,
      boolean largeMode) {
    StringBuilder markdown = new StringBuilder();
    markdown.append("## Quality And Change-Risk Signals\n\n");
    markdown.append("- Quality analysis status: ")
        .append(code(text(quality, "analysis_status")))
        .append("\n");
    markdown.append("- Test-gap signals are absence-sensitive planning hints from the bounded test inventory and inferred tested-subject relations. They do not prove coverage gaps, execution behavior, assertion behavior, CI status, or complete subject mapping.\n");
    markdown.append("- Change-risk signals are warning-oriented or uncertain planning hints from existing deterministic facts. They do not prove production impact, vulnerability, business priority, correctness, runtime behavior, or test priority.\n\n");

    if (largeMode) {
      appendLargeQualitySummary(markdown, quality, moduleById);
    }

    appendTestGapSignals(
        markdown,
        quality.path("test_gap_signals"),
        moduleById,
        evidenceById,
        largeMode);
    appendChangeRiskSignals(
        markdown,
        quality.path("change_risk_signals"),
        moduleById,
        evidenceById,
        largeMode);
    markdown.append("\n");
    return markdown.toString();
  }

  private void appendTestGapSignals(
      StringBuilder markdown,
      JsonNode testGapSignals,
      Map<String, ModuleInfo> moduleById,
      Map<String, EvidenceRecord> evidenceById,
      boolean largeMode) {
    markdown.append("### Test-Gap Signals\n\n");
    markdown.append("- Analysis status: ")
        .append(code(text(testGapSignals, "analysis_status")))
        .append("\n");
    List<JsonNode> signalItems = items(testGapSignals);
    if (signalItems.isEmpty()) {
      markdown.append("- Test-gap signals: none recorded.\n\n");
      return;
    }

    List<JsonNode> visibleSignals = signalItems;
    if (largeMode) {
      visibleSignals = prioritizedRows(signalItems, this::largeQualitySignalPriority);
      visibleSignals = visibleSignals.subList(
          0,
          Math.min(visibleSignals.size(), MAX_LARGE_QUALITY_TEST_GAP_SIGNALS));
    }
    for (JsonNode signal : visibleSignals) {
      markdown.append("- Test-gap signal: ")
          .append(code(text(signal, "signal")))
          .append(" for ")
          .append(code(text(signal, "subject_kind")))
          .append(" ")
          .append(code(text(signal, "subject_name")))
          .append(" (status: ")
          .append(code(text(signal, "status")))
          .append(", inference_basis: ")
          .append(code(text(signal, "inference_basis")))
          .append(", confidence: ")
          .append(code(text(signal, "confidence")))
          .append(", uncertainty: ")
          .append(code(text(signal, "uncertainty")))
          .append("). No coverage, execution, assertion, CI, or runtime relation is claimed.\n");
      appendQualitySignalDetails(markdown, signal, moduleById, evidenceById);
    }
    appendOmittedRows(
        markdown,
        "  ",
        signalItems.size() - visibleSignals.size(),
        "test-gap signal rows",
        "quality.test_gap_signals.items");
    markdown.append("\n");
  }

  private void appendChangeRiskSignals(
      StringBuilder markdown,
      JsonNode changeRiskSignals,
      Map<String, ModuleInfo> moduleById,
      Map<String, EvidenceRecord> evidenceById,
      boolean largeMode) {
    markdown.append("### Change-Risk Signals\n\n");
    markdown.append("- Analysis status: ")
        .append(code(text(changeRiskSignals, "analysis_status")))
        .append("\n");
    List<JsonNode> signalItems = items(changeRiskSignals);
    if (signalItems.isEmpty()) {
      markdown.append("- Change-risk signals: none recorded.\n\n");
      return;
    }

    List<JsonNode> visibleSignals = signalItems;
    if (largeMode) {
      visibleSignals = prioritizedRows(signalItems, this::largeQualitySignalPriority);
      visibleSignals = visibleSignals.subList(
          0,
          Math.min(visibleSignals.size(), MAX_LARGE_QUALITY_CHANGE_RISK_SIGNALS));
    }
    for (JsonNode signal : visibleSignals) {
      markdown.append("- Change-risk signal: ")
          .append(code(text(signal, "signal")))
          .append(" for ")
          .append(code(text(signal, "subject_kind")))
          .append(" ")
          .append(code(text(signal, "subject_name")))
          .append(" (status: ")
          .append(code(text(signal, "status")))
          .append(", risk_basis: ")
          .append(code(text(signal, "risk_basis")))
          .append(", confidence: ")
          .append(code(text(signal, "confidence")))
          .append(", uncertainty: ")
          .append(code(text(signal, "uncertainty")))
          .append("). No production impact, vulnerability, correctness, runtime behavior, or business priority is claimed.\n");
      appendQualitySignalDetails(markdown, signal, moduleById, evidenceById);
    }
    appendOmittedRows(
        markdown,
        "  ",
        signalItems.size() - visibleSignals.size(),
        "change-risk signal rows",
        "quality.change_risk_signals.items");
    markdown.append("\n");
  }

  private void appendQualitySignalDetails(
      StringBuilder markdown,
      JsonNode signal,
      Map<String, ModuleInfo> moduleById,
      Map<String, EvidenceRecord> evidenceById) {
    markdown.append("  - Module: ")
        .append(moduleLabel(text(signal, "module_id"), moduleById))
        .append("\n");
    markdown.append("  - Subject ID: ")
        .append(code(text(signal, "subject_id")))
        .append("\n");
    String subjectClassName = nullableText(signal, "subject_class_name");
    String subjectMemberName = nullableText(signal, "subject_member_name");
    if (subjectClassName != null || subjectMemberName != null) {
      markdown.append("  - Subject source hint: class ")
          .append(code(subjectClassName == null ? "not recorded" : subjectClassName))
          .append(", member ")
          .append(code(subjectMemberName == null ? "not recorded" : subjectMemberName))
          .append("\n");
    }
    appendEvidenceLine(markdown, signal.path("evidence_ids"), evidenceById);
  }

  private void appendLocalProjectDocumentation(
      StringBuilder markdown,
      JsonNode documents,
      Map<String, ModuleInfo> moduleById,
      Map<String, EvidenceRecord> evidenceById) {
    if (!documents.isObject() || !hasLocalDocumentationGuideContent(documents)) {
      return;
    }

    markdown.append("## Local Project Documentation\n\n");
    markdown.append("- Documents analysis status: ")
        .append(code(text(documents, "analysis_status")))
        .append("\n");
    markdown.append("- Local documentation entries are default-scope Markdown navigation facts only; document bodies, paragraphs, arbitrary lists, tables, code blocks, and prose summaries are not rendered.\n");
    markdown.append("- Reconciliation rows are uncertain inspection hints only; they do not prove stale documentation, coverage, completeness, correctness, implementation, or source/document agreement.\n");
    appendDocumentDiscoveryPolicy(markdown, documents.path("discovery"));
    appendDocumentInventory(markdown, documents.path("items"), moduleById, evidenceById);
    appendDocumentReconciliationHints(
        markdown,
        documents.path("reconciliation"),
        moduleById,
        evidenceById);
    markdown.append("\n");
  }

  private void appendDocumentDiscoveryPolicy(StringBuilder markdown, JsonNode discovery) {
    if (!discovery.isObject()) {
      markdown.append("- Discovery policy: not recorded.\n");
      return;
    }

    markdown.append("- Discovery policy: scope ")
        .append(code(text(discovery, "scope")))
        .append(", path_policy ")
        .append(code(text(discovery, "path_policy")))
        .append(", symlink_policy ")
        .append(code(text(discovery, "symlink_policy")))
        .append(", included_patterns ")
        .append(code(Integer.toString(discovery.path("included_patterns").size())))
        .append(", excluded_patterns ")
        .append(code(Integer.toString(discovery.path("excluded_patterns").size())))
        .append(".\n");
  }

  private void appendDocumentInventory(
      StringBuilder markdown,
      JsonNode documents,
      Map<String, ModuleInfo> moduleById,
      Map<String, EvidenceRecord> evidenceById) {
    if (!documents.isArray() || documents.isEmpty()) {
      markdown.append("- Document inventory: detected no default-scope local Markdown documents.\n");
      return;
    }

    markdown.append("- Document inventory: detected ")
        .append(documents.size())
        .append(" accepted default-scope Markdown document")
        .append(documents.size() == 1 ? "" : "s")
        .append(".\n");

    int visibleCount = Math.min(documents.size(), MAX_INLINE_BUILD_CONFIG_ITEMS);
    for (int index = 0; index < visibleCount; index++) {
      JsonNode document = documents.get(index);
      JsonNode headings = document.path("headings");
      JsonNode chunks = document.path("chunks");
      markdown.append("  - Document: ")
          .append(code(text(document, "path")))
          .append(" (")
          .append(documentModuleDescription(document, moduleById))
          .append(", discovery_source: ")
          .append(code(text(document, "discovery_source")))
          .append(", title_source: ")
          .append(code(text(document, "title_source")))
          .append(", headings: ")
          .append(code(Integer.toString(headings.size())))
          .append(", chunks: ")
          .append(code(Integer.toString(chunks.size())))
          .append(").\n");
      appendNestedEvidenceLine(markdown, document.path("evidence_ids"), evidenceById);
      appendDocumentHeadingRefs(markdown, headings, evidenceById);
      appendDocumentChunkRefs(markdown, chunks, evidenceById);
    }
    appendOmittedBuildConfigItems(
        markdown,
        documents.size() - visibleCount,
        "local Markdown documents");
  }

  private String documentModuleDescription(
      JsonNode document,
      Map<String, ModuleInfo> moduleById) {
    String moduleId = nullableText(document, "module_id");
    if (moduleId == null || moduleId.isBlank()) {
      return "module: " + code("repository-level");
    }
    return "module: " + moduleLabel(moduleId, moduleById);
  }

  private void appendDocumentHeadingRefs(
      StringBuilder markdown,
      JsonNode headings,
      Map<String, EvidenceRecord> evidenceById) {
    if (!headings.isArray() || headings.isEmpty()) {
      markdown.append("    - Heading refs: none recorded.\n");
      return;
    }

    markdown.append("    - Heading refs: detected ")
        .append(headings.size())
        .append(" bounded ATX heading reference")
        .append(headings.size() == 1 ? "" : "s")
        .append(".\n");
    int visibleCount = Math.min(headings.size(), MAX_INLINE_DOCUMENT_REFS);
    for (int index = 0; index < visibleCount; index++) {
      JsonNode heading = headings.get(index);
      markdown.append("      - Heading ref: ")
          .append(code(text(heading, "id")))
          .append(" level ")
          .append(code(text(heading, "level")))
          .append(", lines ")
          .append(code(lineRangeLabel(heading)))
          .append(", anchor ")
          .append(code(nullDisplay(nullableText(heading, "anchor"))))
          .append(", evidence ")
          .append(evidenceReferenceList(stringValues(heading.path("evidence_ids")), evidenceById))
          .append(".\n");
    }
    appendOmittedDocumentRefs(markdown, headings.size() - visibleCount, "heading refs");
  }

  private void appendDocumentChunkRefs(
      StringBuilder markdown,
      JsonNode chunks,
      Map<String, EvidenceRecord> evidenceById) {
    if (!chunks.isArray() || chunks.isEmpty()) {
      markdown.append("    - Chunk refs: none recorded.\n");
      return;
    }

    markdown.append("    - Chunk refs: detected ")
        .append(chunks.size())
        .append(" bounded chunk reference")
        .append(chunks.size() == 1 ? "" : "s")
        .append("; chunk bodies are not serialized.\n");
    int visibleCount = Math.min(chunks.size(), MAX_INLINE_DOCUMENT_REFS);
    for (int index = 0; index < visibleCount; index++) {
      JsonNode chunk = chunks.get(index);
      markdown.append("      - Chunk ref: ")
          .append(code(text(chunk, "id")))
          .append(" heading_id ")
          .append(code(nullDisplay(nullableText(chunk, "heading_id"))))
          .append(", lines ")
          .append(code(lineRangeLabel(chunk)))
          .append(", content_status ")
          .append(code(text(chunk, "content_status")))
          .append(", evidence ")
          .append(evidenceReferenceList(stringValues(chunk.path("evidence_ids")), evidenceById))
          .append(".\n");
    }
    appendOmittedDocumentRefs(markdown, chunks.size() - visibleCount, "chunk refs");
  }

  private void appendDocumentReconciliationHints(
      StringBuilder markdown,
      JsonNode reconciliation,
      Map<String, ModuleInfo> moduleById,
      Map<String, EvidenceRecord> evidenceById) {
    if (!reconciliation.isObject()) {
      markdown.append("- Reconciliation hints: not analyzed; no reconciliation section recorded.\n");
      return;
    }

    JsonNode items = reconciliation.path("items");
    markdown.append("- Reconciliation hints: status ")
        .append(code(text(reconciliation, "analysis_status")));
    if (!items.isArray() || items.isEmpty()) {
      markdown.append("; detected none.\n");
      return;
    }

    markdown.append("; detected ")
        .append(items.size())
        .append(" low-confidence uncertain inspection hint")
        .append(items.size() == 1 ? "" : "s")
        .append(".\n");
    int visibleCount = Math.min(items.size(), MAX_INLINE_BUILD_CONFIG_ITEMS);
    for (int index = 0; index < visibleCount; index++) {
      JsonNode hint = items.get(index);
      markdown.append("  - Reconciliation hint: ")
          .append(code(text(hint, "signal")))
          .append(" for ")
          .append(code(text(hint, "subject_kind")))
          .append(" ")
          .append(code(text(hint, "subject_name")))
          .append(" (status: ")
          .append(code(text(hint, "status")))
          .append(", confidence: ")
          .append(code(text(hint, "confidence")))
          .append(", uncertainty: ")
          .append(code(text(hint, "uncertainty")))
          .append(", match_basis: ")
          .append(code(text(hint, "match_basis")))
          .append(").\n");
      appendDocumentHintModuleLine(markdown, hint, moduleById);
      appendDocumentHintReferenceLine(
          markdown,
          "Document",
          nullableText(hint, "document_id"),
          nullableText(hint, "document_path"),
          nullableText(hint, "document_chunk_id"));
      appendDocumentHintReferenceLine(
          markdown,
          "Source fact",
          nullableText(hint, "source_fact_kind"),
          nullableText(hint, "source_fact_id"),
          null);
      appendNestedEvidenceLine(markdown, hint.path("evidence_ids"), evidenceById);
    }
    appendOmittedBuildConfigItems(
        markdown,
        items.size() - visibleCount,
        "document reconciliation hints");
  }

  private void appendDocumentHintModuleLine(
      StringBuilder markdown,
      JsonNode hint,
      Map<String, ModuleInfo> moduleById) {
    String moduleId = nullableText(hint, "module_id");
    markdown.append("    - Module: ");
    if (moduleId == null || moduleId.isBlank()) {
      markdown.append(code("repository-level-or-not-recorded")).append("\n");
      return;
    }
    markdown.append(moduleLabel(moduleId, moduleById)).append("\n");
  }

  private void appendDocumentHintReferenceLine(
      StringBuilder markdown,
      String label,
      String primary,
      String secondary,
      String tertiary) {
    markdown.append("    - ").append(label).append(": ");
    if ((primary == null || primary.isBlank()) && (secondary == null || secondary.isBlank())) {
      markdown.append("none recorded for this hint.\n");
      return;
    }

    List<String> values = new ArrayList<>();
    if (primary != null && !primary.isBlank()) {
      values.add(primary);
    }
    if (secondary != null && !secondary.isBlank()) {
      values.add(secondary);
    }
    if (tertiary != null && !tertiary.isBlank()) {
      values.add("chunk=" + tertiary);
    }
    markdown.append(codeList(values)).append("\n");
  }

  private String lineRangeLabel(JsonNode node) {
    String lineStart = nullableText(node, "line_start");
    String lineEnd = nullableText(node, "line_end");
    if (lineStart == null || lineStart.isBlank()) {
      return "unknown";
    }
    if (lineEnd == null || lineEnd.isBlank() || lineEnd.equals(lineStart)) {
      return lineStart;
    }
    return lineStart + "-" + lineEnd;
  }

  private void appendOmittedDocumentRefs(
      StringBuilder markdown,
      int omittedCount,
      String itemDescription) {
    if (omittedCount <= 0) {
      return;
    }
    markdown.append("      - ... and ")
        .append(omittedCount)
        .append(" more ")
        .append(MarkdownRenderer.text(itemDescription))
        .append(" in ")
        .append(code("project-map.json"))
        .append(".\n");
  }

  private void appendDetailedKnownLimits(
      StringBuilder markdown,
      JsonNode projectMap,
      Map<String, ModuleInfo> moduleById,
      Map<String, EvidenceRecord> evidenceById) {
    markdown.append("## Detailed Known Uncertainty And Limits\n\n");
    appendWarnings(markdown, projectMap.path("warnings"), moduleById, evidenceById);
    markdown.append("- Not scanned: Generated-source roots are metadata-only path/codegen observations with `content_status: \"not_scanned\"`; generated source contents, generator execution, generated API reconstruction, runtime freshness checks, dependency/task resolution, and custom Gradle generated-source graph reconstruction are not performed.\n");
    markdown.append("- Not analyzed: Spring runtime behavior such as component scanning, dependency ")
        .append("injection graphs, bean lifecycle, scopes, and conditional configuration is not ")
        .append("represented by `components.items`.\n");
    markdown.append("- Uncertain: JPA relationship targets preserve `target_resolution: ")
        .append("declared_type_only` and `uncertainty: target_type_not_resolved`; no symbol ")
        .append("solving or ORM runtime behavior is claimed.\n");
    markdown.append("- Source-visible: JPA relationship metadata such as `mappedBy`, ")
        .append("`@JoinColumn`, `@JoinTable`, `optional`, `fetch`, `cascade`, and ")
        .append("`orphanRemoval` is reported only when direct annotation attributes are ")
        .append("supported; foreign keys, join tables, ownership correctness, fetch behavior, ")
        .append("cascade behavior, and database constraints are not claimed.\n");
    markdown.append("- Not analyzed: JPA mapped-superclass identifier support is limited to ")
        .append("conservative source-visible mapped-superclass chains; unresolved, ambiguous, ")
        .append("cyclic, or non-source-visible branches are skipped.\n");
    markdown.append("- Partial: JPA embedded and composite identifier support is limited to direct ")
        .append("source-visible `@Embeddable`, `@Embedded`, `@EmbeddedId`, and `@IdClass` ")
        .append("signals. Embedded targets are linked only when a unique local `@Embeddable` ")
        .append("can be matched; `@IdClass` field matching and composite-key semantics are ")
        .append("not analyzed.\n");
    markdown.append("- Inferred/statused: tested-subject rows are conservative source-visible hints ")
        .append("from supported naming, import, field-type, and Spring test slice class-literal ")
        .append("signals. Non-inferred statuses such as `not_detected`, `ambiguous`, and ")
        .append("`unsupported` do not claim coverage or execution. Test method inventory records ")
        .append("source-visible JUnit annotation structure only. Test execution, CI results, ")
        .append("coverage, assertion behavior, call graphs, and complete subject mapping are not ")
        .append("analyzed.\n");
    if (hasQualitySignals(projectMap.path("quality"))) {
      markdown.append("- Planning hints: quality test-gap and change-risk signals are conservative ")
          .append("derived hints from existing deterministic facts and inferred tested-subject ")
          .append("relations. They do not claim coverage, test execution, assertion behavior, ")
          .append("runtime behavior, production impact, vulnerability, correctness, business ")
          .append("priority, or complete subject mapping.\n");
    }
    if (hasTestSliceOrMockSignals(projectMap)) {
      markdown.append("- Source-visible: Spring test slice signals and mock annotation signals record ")
          .append("direct annotation structure only. Runtime Spring context behavior, bean graph ")
          .append("contents, MockMvc setup, database access, Mockito behavior, and slice ")
          .append("correctness are not claimed.\n");
    }
    String buildSystem = text(projectMap.path("project").path("build"), "system");
    boolean gradleDetected = "gradle".equals(buildSystem) || "mixed".equals(buildSystem);
    if (projectMap.path("project").path("modules").isObject() && gradleDetected) {
      markdown.append("- Not analyzed: connectors, LLM summaries, repository chat, generic RAG, ")
          .append("Maven profiles, effective POM reconstruction, Gradle execution, dynamic ")
          .append("buildscript evaluation, dependency graphs, and recursive nested Maven modules ")
          .append("are outside this guide.\n");
      markdown.append("- Not analyzed: Gradle dependency resolution, plugin resolution, task graphs, ")
          .append("custom `sourceSets`, `projectDir` remapping, included builds, and Kotlin source ")
          .append("analysis are outside this guide.\n");
    } else if (projectMap.path("project").path("modules").isObject()) {
      markdown.append("- Not analyzed: connectors, LLM summaries, repository chat, generic RAG, ")
          .append("Gradle/Kotlin support, Maven profiles, effective POM reconstruction, ")
          .append("dependency graphs, and recursive nested Maven modules are outside this guide.\n");
    } else {
      markdown.append("- Not analyzed: connectors, LLM summaries, repository chat, generic RAG, ")
          .append("Gradle/Kotlin support, and multi-module Maven parsing are outside this guide.\n");
    }
    markdown.append("- Not analyzed: generated sources, generated API reconstruction, ")
        .append("classpath-only interfaces, and ambiguous interface endpoint bindings are outside ")
        .append("the source-visible interface endpoint support.\n");
    markdown.append("- Not analyzed: OpenAPI operation facts are spec-backed declared operations only; ")
        .append("runtime implementation matching, source/spec agreement, generated source contents, ")
        .append("and client SDK reconstruction are not claimed.\n");
    markdown.append("- Not analyzed: v0.3 build/config facts are direct local source observations ")
        .append("only. Maven execution, effective POM reconstruction, profile activation, remote ")
        .append("dependency resolution, config value interpretation, secret extraction, and default ")
        .append("generated-source scanning are not performed.\n");
    markdown.append("- Not analyzed: Spring Boot application signals do not prove executable packaging, ")
        .append("active profiles, runtime auto-configuration, bean graphs, component scanning ")
        .append("results, deployment behavior, or actual process entrypoint behavior.\n");
    markdown.append("- Not analyzed: Spring Data repository interface signals do not prove runtime ")
        .append("repository registration, query method behavior, database access, or runtime ")
        .append("repository/entity verification. Repository/entity links, when present, are ")
        .append("bounded inferred Spring Data generic relations with explicit ")
        .append("`entity_relation_status` values.\n");
    markdown.append("- Not analyzed: JPA field metadata is limited to supported direct ")
        .append("field-level source-visible annotations. It is not a complete persistent-property ")
        .append("inventory, does not support getter/property access in this slice, and does not ")
        .append("fill missing annotation attributes from JPA provider defaults.\n");
    if (projectMap.path("spring_application_surface").isObject()) {
      markdown.append("- Not analyzed: v0.5 transaction, scheduling, event listener, and ")
          .append("messaging listener facts are annotation-presence change-surface signals only. ")
          .append("Transaction propagation, scheduler registration, event delivery, message ")
          .append("destinations, broker topology, consumer groups, and delivery semantics are ")
          .append("not claimed.\n");
      markdown.append("- Not analyzed: Security policy, endpoint protection state, ")
          .append("authentication behavior, authorization behavior, filter-chain ordering, ")
          .append("vulnerabilities, and correctness are not claimed. v0.5 Spring Security ")
          .append("configuration warnings are bounded source-visible inspection hints only.\n");
    }
    if (hasLocalDocumentationGuideContent(projectMap.path("documents"))) {
      markdown.append("- Document-backed: local documentation facts come from default-scope ")
          .append("Markdown inventory, heading/chunk navigation references, and uncertain ")
          .append("reconciliation hints only. Hidden, private, generated, dependency, ")
          .append("maintainer, and `.project-memory/` paths are excluded by default; symlinks ")
          .append("are not followed by default; external docs, PDFs, Word documents, ")
          .append("connectors, generic RAG, repository chat, and LLM summaries are outside the ")
          .append("core analyzer. Document-backed signals do not override code-backed facts.\n");
    }

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
    boolean moduleAware = projectMap.path("project").path("modules").isObject();
    int step = 1;
    if (moduleAware) {
      markdown.append(step++).append(". Start with detected build, module, and layout facts");
    } else {
      markdown.append(step++).append(". Start with detected build and layout facts");
    }
    LinkedHashSet<String> layoutPaths = new LinkedHashSet<>();
    layoutPaths.addAll(evidencePaths(projectMap.path("project").path("build"), evidenceById));
    layoutPaths.addAll(evidencePaths(projectMap.path("project").path("modules"), evidenceById));
    layoutPaths.addAll(evidencePaths(projectMap.path("generated_sources"), evidenceById));
    appendPathHint(markdown, List.copyOf(layoutPaths));
    markdown.append(".\n");
    markdown.append(step++)
        .append(". For HTTP behavior, inspect detected endpoint and hidden-surface warning evidence");
    LinkedHashSet<String> httpPaths = new LinkedHashSet<>();
    httpPaths.addAll(evidencePaths(projectMap.path("endpoints"), evidenceById));
    httpPaths.addAll(evidencePaths(projectMap.path("api_surface"), evidenceById));
    httpPaths.addAll(hiddenHttpWarningEvidencePaths(projectMap.path("warnings"), evidenceById));
    appendPathHint(markdown, List.copyOf(httpPaths));
    markdown.append(".\n");
    markdown.append(step++)
        .append(". For Spring application surface changes, inspect Spring application surface and component evidence");
    LinkedHashSet<String> springPaths = new LinkedHashSet<>();
    springPaths.addAll(evidencePaths(projectMap.path("spring_application_surface"), evidenceById));
    springPaths.addAll(evidencePaths(projectMap.path("components").path("items"), evidenceById));
    appendPathHint(markdown, List.copyOf(springPaths));
    markdown.append(" and avoid assuming runtime repository registration, entity ownership, injection graphs, transaction behavior, scheduler registration, event delivery, or messaging topology.\n");
    if (hasDomainGuideContent(projectMap)) {
      markdown.append(step++)
          .append(". For persistence changes, inspect detected entity evidence");
      appendPathHint(markdown, evidencePaths(projectMap.path("entities").path("items"), evidenceById));
      markdown.append(" and treat field metadata as source-visible annotations only, not runtime schema, ")
          .append("provider defaults, or complete access-strategy reconstruction; relationship targets ")
          .append("remain declared-type-only.\n");
    }
    markdown.append(step++)
        .append(". For tests, inspect detected test files and tested-subject relation/status evidence");
    appendPathHint(markdown, evidencePaths(projectMap.path("tests").path("items"), evidenceById));
    markdown.append("; do not treat inferred or statused subjects as coverage proof");
    if (hasTestSliceOrMockSignals(projectMap)) {
      markdown.append(", and do not treat Spring test slice or mock annotations as execution or ")
          .append("runtime behavior proof");
    }
    markdown.append(".\n");
    if (hasQualitySignals(projectMap.path("quality"))) {
      markdown.append(step++)
          .append(". For quality and change-risk planning, inspect quality signal evidence");
      appendPathHint(markdown, evidencePaths(projectMap.path("quality"), evidenceById));
      markdown.append(" and treat `no_obvious_test`, warning-oriented, and uncertain statuses as ")
          .append("planning hints only, not coverage, runtime, correctness, vulnerability, or ")
          .append("business-priority claims.\n");
    }
    if (hasLocalDocumentationGuideContent(projectMap.path("documents"))) {
      markdown.append(step++)
          .append(". For local documentation context, inspect accepted document evidence and reconciliation hints");
      appendPathHint(
          markdown,
          evidencePaths(projectMap.path("documents"), evidenceById));
      markdown.append(" and treat document paths, heading refs, chunk refs, and reconciliation rows ")
          .append("as navigation aids only; prefer code-backed facts for implementation truth.\n");
    }
    markdown.append("\n");
  }

  private boolean hasLocalDocumentationGuideContent(JsonNode documents) {
    return documents.isObject()
        && (hasArrayEntries(documents.path("items"))
            || hasArrayEntries(documents.path("reconciliation").path("items")));
  }

  private boolean hasDomainGuideContent(JsonNode projectMap) {
    JsonNode entities = projectMap.path("entities");
    if (hasItems(entities) || hasItems(entities.path("embeddables"))) {
      return true;
    }
    return hasRepositoryEntityRelations(projectMap);
  }

  private boolean hasTestSliceOrMockSignals(JsonNode projectMap) {
    for (JsonNode test : items(projectMap.path("tests"))) {
      if (hasArrayEntries(test.path("spring_test_slices")) || hasArrayEntries(test.path("mock_signals"))) {
        return true;
      }
    }
    return false;
  }

  private boolean hasQualitySignals(JsonNode quality) {
    return hasArrayEntries(quality.path("test_gap_signals").path("items"))
        || hasArrayEntries(quality.path("change_risk_signals").path("items"));
  }

  private boolean hasRepositoryEntityRelations(JsonNode projectMap) {
    for (JsonNode repository : items(
        projectMap.path("spring_application_surface").path("repositories"))) {
      if ("inferred".equals(text(repository, "entity_relation_status"))
          && repository.path("entity_relation").isObject()) {
        return true;
      }
    }
    return false;
  }

  private boolean hasItems(JsonNode section) {
    JsonNode items = section.path("items");
    return items.isArray() && !items.isEmpty();
  }

  private boolean hasArrayEntries(JsonNode values) {
    return values.isArray() && !values.isEmpty();
  }

  private void appendWarnings(
      StringBuilder markdown,
      JsonNode warnings,
      Map<String, ModuleInfo> moduleById,
      Map<String, EvidenceRecord> evidenceById) {
    JsonNode items = warnings.path("items");
    if (!items.isArray() || items.isEmpty()) {
      return;
    }

    for (JsonNode warning : items) {
      markdown.append("- Warning: ")
          .append(code(text(warning, "category")))
          .append(" signal ")
          .append(code(text(warning, "signal")));
      String moduleDescription = moduleWarningLabel(warning, moduleById);
      if (!moduleDescription.isBlank()) {
        markdown.append(" for ").append(moduleDescription);
      }
      markdown.append(" at ")
          .append(code(text(warning, "source_path")))
          .append(". ")
          .append(MarkdownRenderer.text(text(warning, "message")))
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
    appendEvidenceLine(markdown, ids, evidenceById, "Evidence");
  }

  private void appendEvidenceLine(
      StringBuilder markdown,
      List<String> ids,
      Map<String, EvidenceRecord> evidenceById,
      String label) {
    markdown.append("  - ").append(label).append(": ");
    if (ids.isEmpty()) {
      markdown.append("none recorded.\n");
      return;
    }
    markdown.append(evidenceReferenceList(ids, evidenceById)).append("\n");
  }

  private void appendNestedEvidenceLine(
      StringBuilder markdown,
      JsonNode evidenceIds,
      Map<String, EvidenceRecord> evidenceById) {
    appendNestedEvidenceLine(markdown, stringValues(evidenceIds), evidenceById);
  }

  private void appendNestedEvidenceLine(
      StringBuilder markdown,
      List<String> ids,
      Map<String, EvidenceRecord> evidenceById) {
    markdown.append("    - Evidence: ");
    if (ids.isEmpty()) {
      markdown.append("none recorded.\n");
      return;
    }
    markdown.append(evidenceReferenceList(ids, evidenceById)).append("\n");
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

  private List<ModuleInfo> moduleInfos(JsonNode modulesNode) {
    JsonNode items = modulesNode.path("items");
    if (!items.isArray()) {
      return List.of();
    }

    List<ModuleInfo> modules = new ArrayList<>();
    for (JsonNode item : items) {
      modules.add(new ModuleInfo(
          text(item, "module_id"),
          text(item, "module_path"),
          nullableText(item, "pom_path"),
          text(item, "support_status"),
          text(item, "declaration_kind"),
          text(item, "declared_path"),
          stringValues(item.path("build_systems")),
          nullableText(item, "gradle_project_path"),
          stringValues(item.path("source_roots")),
          stringValues(item.path("test_roots")),
          stringValues(item.path("declaration_evidence_ids")),
          stringValues(item.path("pom_evidence_ids")),
          evidenceIdsInSubtree(item.path("build_config").path("gradle").path("build_files"))));
    }
    return List.copyOf(modules);
  }

  private Map<String, ModuleInfo> moduleById(List<ModuleInfo> modules) {
    Map<String, ModuleInfo> moduleById = new LinkedHashMap<>();
    for (ModuleInfo module : modules) {
      moduleById.put(module.moduleId(), module);
    }
    return moduleById;
  }

  private String moduleLabel(String moduleId, Map<String, ModuleInfo> moduleById) {
    return moduleLabel(moduleId, moduleById.get(moduleId));
  }

  private String moduleLabel(String moduleId, ModuleInfo module) {
    if (module == null) {
      return code(moduleId) + " (module path not recorded)";
    }
    return code(moduleId) + " (path: " + code(module.modulePath()) + ")";
  }

  private String moduleWarningLabel(JsonNode warning, Map<String, ModuleInfo> moduleById) {
    String moduleId = nullableText(warning, "module_id");
    if (moduleId == null || moduleId.isBlank()) {
      if (moduleById.isEmpty()) {
        return "";
      }
      return "no module identity";
    }
    return "module " + moduleLabel(moduleId, moduleById);
  }

  private String mavenValueLabel(JsonNode valueNode) {
    return code(mavenRawValueLabel(valueNode, "value"))
        + " (value_kind: "
        + code(text(valueNode, "value_kind"))
        + ")";
  }

  private String mavenCoordinateLabel(JsonNode declaration) {
    return mavenRawValueLabel(declaration.path("group_id"), "group_id")
        + ":"
        + mavenRawValueLabel(declaration.path("artifact_id"), "artifact_id");
  }

  private String mavenRawValueLabel(JsonNode valueNode, String fieldName) {
    String value = nullableText(valueNode, "value");
    if (value != null && !value.isBlank()) {
      return value;
    }

    String valueKind = text(valueNode, "value_kind");
    if (valueKind.isBlank()) {
      return fieldName + ":not_recorded";
    }
    return fieldName + ":" + valueKind;
  }

  private String rawMavenValues(JsonNode values) {
    if (!values.isArray() || values.isEmpty()) {
      return "none recorded";
    }

    List<String> labels = new ArrayList<>();
    for (JsonNode value : values) {
      labels.add(mavenRawValueLabel(value, "value"));
    }
    return String.join(",", labels);
  }

  private String nullDisplay(String value) {
    if (value == null || value.isBlank()) {
      return "not_declared";
    }
    return value;
  }

  private List<String> evidenceIdsInSubtree(JsonNode node) {
    return EvidenceReferenceRenderer.evidenceIdsInSubtree(node);
  }

  private void appendOmittedBuildConfigItems(
      StringBuilder markdown,
      int omittedCount,
      String itemDescription) {
    if (omittedCount <= 0) {
      return;
    }
    markdown.append("  - ... and ")
        .append(omittedCount)
        .append(" more ")
        .append(MarkdownRenderer.text(itemDescription))
        .append(" in ")
        .append(code("project-map.json"))
        .append(".\n");
  }

  private void appendLargeSectionWarning(StringBuilder markdown, int itemCount) {
    if (itemCount <= LARGE_DETAILED_SECTION_ITEM_COUNT) {
      return;
    }
    markdown.append("- Large section: use this summary first. Read detailed rows only when they are task-relevant, and verify important claims through exact evidence IDs.\n");
  }

  private Map<String, EvidenceRecord> evidenceById(String evidenceIndexJsonl) throws IOException {
    return EvidenceReferenceRenderer.evidenceById(evidenceIndexJsonl);
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
    return evidence != null && symbolName.equals(evidence.symbolName());
  }

  private List<String> evidencePaths(JsonNode node, Map<String, EvidenceRecord> evidenceById) {
    return EvidenceReferenceRenderer.evidencePaths(node, evidenceById);
  }

  private List<String> hiddenHttpWarningEvidencePaths(
      JsonNode warnings,
      Map<String, EvidenceRecord> evidenceById) {
    LinkedHashSet<String> paths = new LinkedHashSet<>();
    JsonNode items = warnings.path("items");
    if (!items.isArray()) {
      return List.of();
    }
    for (JsonNode warning : items) {
      if ("hidden_http_surface".equals(text(warning, "category"))) {
        paths.addAll(EvidenceReferenceRenderer.evidencePaths(warning, evidenceById));
      }
    }
    return List.copyOf(paths);
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

  private String evidenceReferenceList(
      List<String> ids,
      Map<String, EvidenceRecord> evidenceById) {
    return EvidenceReferenceRenderer.evidenceReferenceList(ids, evidenceById);
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

  private String redactedNullableText(JsonNode node, String fieldName) {
    return OutputRedactor.redactField(fieldName, nullableText(node, fieldName));
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
    return MarkdownRenderer.inlineCodeList(values, "none recorded");
  }

  private String code(String value) {
    return MarkdownRenderer.inlineCode(value);
  }

  private static final class SpringSurfaceModuleGroup {
    private final String moduleId;
    private final List<JsonNode> extractedFacts = new ArrayList<>();
    private final List<JsonNode> inferredSignals = new ArrayList<>();
    private final List<JsonNode> inferredRelations = new ArrayList<>();
    private final List<SpringSurfaceUncertainStatus> uncertainStatuses = new ArrayList<>();
    private final List<SpringSurfaceWarningFact> warningFacts = new ArrayList<>();

    private SpringSurfaceModuleGroup(String moduleId) {
      this.moduleId = moduleId;
    }

    private boolean hasContent() {
      return !extractedFacts.isEmpty()
          || !inferredSignals.isEmpty()
          || !inferredRelations.isEmpty()
          || !uncertainStatuses.isEmpty()
          || !warningFacts.isEmpty();
    }

    private String moduleId() {
      return moduleId;
    }

    private List<JsonNode> extractedFacts() {
      return extractedFacts;
    }

    private List<JsonNode> inferredSignals() {
      return inferredSignals;
    }

    private List<JsonNode> inferredRelations() {
      return inferredRelations;
    }

    private List<SpringSurfaceUncertainStatus> uncertainStatuses() {
      return uncertainStatuses;
    }

    private List<SpringSurfaceWarningFact> warningFacts() {
      return warningFacts;
    }
  }

  private record SpringSurfaceUncertainStatus(
      String target,
      String fieldName,
      String status,
      String reason,
      List<String> evidenceIds) {
    private SpringSurfaceUncertainStatus {
      evidenceIds = List.copyOf(evidenceIds);
    }
  }

  private record SpringSurfaceWarningFact(
      String warningId,
      JsonNode warning) {
  }

  private record SizeSnapshot(
      long projectMapBytes,
      long evidenceIndexBytes,
      long guideBytes,
      long guideLines,
      long knownOutputBytes,
      String guideBand) {
    private boolean requiresLargeArtifactNotice() {
      return "large-guide".equals(guideBand)
          || "huge-guide".equals(guideBand)
          || projectMapBytes > LARGE_MACHINE_ARTIFACT_BYTES
          || evidenceIndexBytes > LARGE_MACHINE_ARTIFACT_BYTES
          || knownOutputBytes > HUGE_KNOWN_OUTPUT_BYTES;
    }
  }

  private record PrioritizedRow<T>(
      T value,
      int priority,
      int originalIndex) {
  }

  private record ModuleInfo(
      String moduleId,
      String modulePath,
      String pomPath,
      String supportStatus,
      String declarationKind,
      String declaredPath,
      List<String> buildSystems,
      String gradleProjectPath,
      List<String> sourceRoots,
      List<String> testRoots,
      List<String> declarationEvidenceIds,
      List<String> pomEvidenceIds,
      List<String> gradleBuildFileEvidenceIds) {
    private ModuleInfo {
      buildSystems = List.copyOf(buildSystems);
      sourceRoots = List.copyOf(sourceRoots);
      testRoots = List.copyOf(testRoots);
      declarationEvidenceIds = List.copyOf(declarationEvidenceIds);
      pomEvidenceIds = List.copyOf(pomEvidenceIds);
      gradleBuildFileEvidenceIds = List.copyOf(gradleBuildFileEvidenceIds);
    }
  }
}
