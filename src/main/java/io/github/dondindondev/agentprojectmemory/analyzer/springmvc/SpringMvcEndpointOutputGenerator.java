package io.github.dondindondev.agentprojectmemory.analyzer.springmvc;

import io.github.dondindondev.agentprojectmemory.analyzer.ScanPathContainment;
import io.github.dondindondev.agentprojectmemory.analyzer.jpa.JpaEntityAnalysis;
import io.github.dondindondev.agentprojectmemory.analyzer.jpa.JpaEntityAnalyzer;
import io.github.dondindondev.agentprojectmemory.analyzer.jpa.JpaEntityEvidence;
import io.github.dondindondev.agentprojectmemory.analyzer.jpa.JpaEntityFact;
import io.github.dondindondev.agentprojectmemory.analyzer.jpa.JpaIdentifierFieldFact;
import io.github.dondindondev.agentprojectmemory.analyzer.jpa.JpaRelationshipFact;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenDependencyAnalysis;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenDependencyAnalyzer;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenDependencyDeclaration;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenDependencyEvidence;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenModuleDiscoveryAnalysis;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenModuleDiscoveryAnalyzer;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenModuleDiscoveryEvidence;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenModuleDependencies;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenModuleItem;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenModuleMetadata;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenModuleWarning;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenMetadataAnalysis;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenMetadataAnalyzer;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenMetadataEvidence;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenMetadataParent;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenMetadataValue;
import io.github.dondindondev.agentprojectmemory.analyzer.tests.TestClassFact;
import io.github.dondindondev.agentprojectmemory.analyzer.tests.TestFrameworkSignalFact;
import io.github.dondindondev.agentprojectmemory.analyzer.tests.TestInventoryAnalysis;
import io.github.dondindondev.agentprojectmemory.analyzer.tests.TestInventoryAnalyzer;
import io.github.dondindondev.agentprojectmemory.analyzer.tests.TestInventoryEvidence;
import io.github.dondindondev.agentprojectmemory.analyzer.tests.TestedSubjectFact;
import io.github.dondindondev.agentprojectmemory.analyzer.warnings.AnalysisWarningAnalysis;
import io.github.dondindondev.agentprojectmemory.analyzer.warnings.AnalysisWarningAnalyzer;
import io.github.dondindondev.agentprojectmemory.analyzer.warnings.AnalysisWarningEvidence;
import io.github.dondindondev.agentprojectmemory.analyzer.warnings.AnalysisWarningFact;
import io.github.dondindondev.agentprojectmemory.generator.AgentGuideGenerator;
import io.github.dondindondev.agentprojectmemory.generator.MarkdownRenderer;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
  private static final String SCHEMA_VERSION = "0.3";
  private static final String ANALYSIS_ANALYZED = "analyzed";
  private static final String ANALYSIS_NOT_ANALYZED = "not_analyzed";
  private static final String ANALYSIS_NOT_DETECTED = "not_detected";
  private static final String MODULE_ANALYSIS_NOT_DETECTED = "not_detected";
  private static final String MODULE_SUPPORTED = "supported";
  private static final String ROOT_MODULE_ID = "module:.";
  private static final String PROJECT_MAP_FILE_NAME = "project-map.json";
  private static final String ENDPOINTS_FILE_NAME = "endpoints.md";
  private static final String EVIDENCE_INDEX_FILE_NAME = "evidence-index.jsonl";
  private static final String AGENT_GUIDE_FILE_NAME = "agent-guide.md";
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
  private static final Comparator<ModuleScopedEndpointFact> ENDPOINT_ORDER = Comparator
      .comparingInt(ModuleScopedEndpointFact::moduleOrder)
      .thenComparing(endpoint -> firstPath(endpoint.fact()))
      .thenComparing(endpoint -> String.join(",", endpoint.fact().httpMethods()))
      .thenComparing(endpoint -> endpoint.fact().httpMethodSemantics().name())
      .thenComparing(endpoint -> endpoint.fact().controllerClass())
      .thenComparing(endpoint -> endpoint.fact().handlerMethod());
  private static final Comparator<ModuleScopedComponentFact> COMPONENT_ORDER = Comparator
      .comparingInt(ModuleScopedComponentFact::moduleOrder)
      .thenComparing(component -> component.fact().className())
      .thenComparing(component -> componentId(component.moduleId(), component.fact()));
  private static final Comparator<ModuleScopedEntityFact> ENTITY_ORDER = Comparator
      .comparingInt(ModuleScopedEntityFact::moduleOrder)
      .thenComparing(entity -> entity.fact().className())
      .thenComparing(entity -> entityId(entity.moduleId(), entity.fact()));
  private static final Comparator<JpaIdentifierFieldFact> IDENTIFIER_FIELD_ORDER = Comparator
      .comparing(JpaIdentifierFieldFact::sourceKind)
      .thenComparing(JpaIdentifierFieldFact::declaringClass)
      .thenComparing(JpaIdentifierFieldFact::fieldName)
      .thenComparing(JpaIdentifierFieldFact::javaType);
  private static final Comparator<JpaRelationshipFact> RELATIONSHIP_ORDER = Comparator
      .comparing(JpaRelationshipFact::fieldName)
      .thenComparing(JpaRelationshipFact::annotation)
      .thenComparing(JpaRelationshipFact::javaType);
  private static final Comparator<ModuleScopedTestFact> TEST_CLASS_ORDER = Comparator
      .comparingInt(ModuleScopedTestFact::moduleOrder)
      .thenComparing(test -> test.fact().className())
      .thenComparing(test -> test.fact().sourcePath());
  private static final Comparator<TestFrameworkSignalFact> TEST_FRAMEWORK_SIGNAL_ORDER = Comparator
      .comparing(TestFrameworkSignalFact::name);
  private static final Comparator<TestedSubjectFact> TESTED_SUBJECT_ORDER = Comparator
      .comparing(TestedSubjectFact::className)
      .thenComparing(TestedSubjectFact::supportType)
      .thenComparing(TestedSubjectFact::confidence)
      .thenComparing(subject -> nullSafe(subject.uncertainty()));
  private static final Comparator<ModuleScopedWarningFact> WARNING_ORDER = Comparator
      .comparing(ModuleScopedWarningFact::category)
      .thenComparing(ModuleScopedWarningFact::signal)
      .thenComparingInt(ModuleScopedWarningFact::moduleOrder)
      .thenComparing(ModuleScopedWarningFact::sourcePath)
      .thenComparing(ModuleScopedWarningFact::id);

  private final SpringMvcEndpointAnalyzer analyzer;
  private final SpringComponentAnalyzer componentAnalyzer;
  private final JpaEntityAnalyzer entityAnalyzer;
  private final TestInventoryAnalyzer testInventoryAnalyzer;
  private final AnalysisWarningAnalyzer warningAnalyzer;
  private final MavenModuleDiscoveryAnalyzer moduleDiscoveryAnalyzer;
  private final MavenMetadataAnalyzer mavenMetadataAnalyzer;
  private final MavenDependencyAnalyzer mavenDependencyAnalyzer;
  private final AgentGuideGenerator agentGuideGenerator;

  public SpringMvcEndpointOutputGenerator() {
    this(
        new SpringMvcEndpointAnalyzer(),
        new SpringComponentAnalyzer(),
        new JpaEntityAnalyzer(),
        new TestInventoryAnalyzer(),
        new AnalysisWarningAnalyzer(),
        new MavenModuleDiscoveryAnalyzer(),
        new MavenMetadataAnalyzer(),
        new MavenDependencyAnalyzer(),
        new AgentGuideGenerator());
  }

  SpringMvcEndpointOutputGenerator(SpringMvcEndpointAnalyzer analyzer) {
    this(
        analyzer,
        new SpringComponentAnalyzer(),
        new JpaEntityAnalyzer(),
        new TestInventoryAnalyzer(),
        new AnalysisWarningAnalyzer(),
        new MavenModuleDiscoveryAnalyzer(),
        new MavenMetadataAnalyzer(),
        new MavenDependencyAnalyzer(),
        new AgentGuideGenerator());
  }

  SpringMvcEndpointOutputGenerator(
      SpringMvcEndpointAnalyzer analyzer,
      SpringComponentAnalyzer componentAnalyzer) {
    this(
        analyzer,
        componentAnalyzer,
        new JpaEntityAnalyzer(),
        new TestInventoryAnalyzer(),
        new AnalysisWarningAnalyzer(),
        new MavenModuleDiscoveryAnalyzer(),
        new MavenMetadataAnalyzer(),
        new MavenDependencyAnalyzer(),
        new AgentGuideGenerator());
  }

  SpringMvcEndpointOutputGenerator(
      SpringMvcEndpointAnalyzer analyzer,
      SpringComponentAnalyzer componentAnalyzer,
      JpaEntityAnalyzer entityAnalyzer) {
    this(
        analyzer,
        componentAnalyzer,
        entityAnalyzer,
        new TestInventoryAnalyzer(),
        new AnalysisWarningAnalyzer(),
        new MavenModuleDiscoveryAnalyzer(),
        new MavenMetadataAnalyzer(),
        new MavenDependencyAnalyzer(),
        new AgentGuideGenerator());
  }

  SpringMvcEndpointOutputGenerator(
      SpringMvcEndpointAnalyzer analyzer,
      SpringComponentAnalyzer componentAnalyzer,
      JpaEntityAnalyzer entityAnalyzer,
      TestInventoryAnalyzer testInventoryAnalyzer) {
    this(
        analyzer,
        componentAnalyzer,
        entityAnalyzer,
        testInventoryAnalyzer,
        new AnalysisWarningAnalyzer(),
        new MavenModuleDiscoveryAnalyzer(),
        new MavenMetadataAnalyzer(),
        new MavenDependencyAnalyzer(),
        new AgentGuideGenerator());
  }

  SpringMvcEndpointOutputGenerator(
      SpringMvcEndpointAnalyzer analyzer,
      SpringComponentAnalyzer componentAnalyzer,
      JpaEntityAnalyzer entityAnalyzer,
      TestInventoryAnalyzer testInventoryAnalyzer,
      AnalysisWarningAnalyzer warningAnalyzer,
      MavenModuleDiscoveryAnalyzer moduleDiscoveryAnalyzer,
      MavenMetadataAnalyzer mavenMetadataAnalyzer,
      MavenDependencyAnalyzer mavenDependencyAnalyzer,
      AgentGuideGenerator agentGuideGenerator) {
    this.analyzer = Objects.requireNonNull(analyzer, "analyzer");
    this.componentAnalyzer = Objects.requireNonNull(componentAnalyzer, "componentAnalyzer");
    this.entityAnalyzer = Objects.requireNonNull(entityAnalyzer, "entityAnalyzer");
    this.testInventoryAnalyzer = Objects.requireNonNull(
        testInventoryAnalyzer,
        "testInventoryAnalyzer");
    this.warningAnalyzer = Objects.requireNonNull(warningAnalyzer, "warningAnalyzer");
    this.moduleDiscoveryAnalyzer = Objects.requireNonNull(
        moduleDiscoveryAnalyzer,
        "moduleDiscoveryAnalyzer");
    this.mavenMetadataAnalyzer = Objects.requireNonNull(
        mavenMetadataAnalyzer,
        "mavenMetadataAnalyzer");
    this.mavenDependencyAnalyzer = Objects.requireNonNull(
        mavenDependencyAnalyzer,
        "mavenDependencyAnalyzer");
    this.agentGuideGenerator = Objects.requireNonNull(agentGuideGenerator, "agentGuideGenerator");
  }

  public Result generate(Path repositoryRoot, Path outputDirectory) throws IOException {
    Objects.requireNonNull(repositoryRoot, "repositoryRoot");
    Objects.requireNonNull(outputDirectory, "outputDirectory");

    Path normalizedRepositoryRoot = repositoryRoot.toAbsolutePath().normalize();
    Path canonicalRepositoryRoot = ScanPathContainment.canonicalRoot(normalizedRepositoryRoot);
    MavenModuleDiscoveryAnalysis moduleDiscoveryAnalysis = moduleDiscoveryAnalyzer.analyze(
        normalizedRepositoryRoot);
    ProjectLayout layout = detectLayout(
        normalizedRepositoryRoot,
        canonicalRepositoryRoot,
        moduleDiscoveryAnalysis);
    MavenMetadataAnalysis metadataAnalysis = mavenMetadataAnalyzer.analyze(
        normalizedRepositoryRoot,
        layout.modules().items());
    MavenDependencyAnalysis dependencyAnalysis = mavenDependencyAnalyzer.analyze(
        normalizedRepositoryRoot,
        layout.modules().items());
    if (!shouldGenerate(layout, moduleDiscoveryAnalysis, metadataAnalysis, dependencyAnalysis)) {
      return new Result(false, 0, 0, 0, 0, 0);
    }

    ModuleAwareScan scan = analyzeModules(
        normalizedRepositoryRoot,
        canonicalRepositoryRoot,
        layout.modules(),
        moduleDiscoveryAnalysis.warnings());
    List<EvidenceRecord> evidenceRecords = evidenceRecords(
        layout,
        moduleDiscoveryAnalysis.evidence(),
        metadataAnalysis.evidence(),
        dependencyAnalysis.evidence(),
        scan.endpointEvidence(),
        scan.componentEvidence(),
        scan.entityEvidence(),
        scan.testEvidence(),
        scan.warningEvidence());
    String evidenceIndexJsonl = evidenceIndexJsonl(evidenceRecords);
    String projectMapJson = projectMapJson(
        layout,
        scan,
        metadataAnalysis,
        dependencyAnalysis);

    writeGeneratedFiles(
        canonicalRepositoryRoot,
        outputDirectory,
        List.of(
            new GeneratedOutputFile(
                ENDPOINTS_FILE_NAME,
                endpointsMarkdown(layout.modules(), scan.endpoints())),
            new GeneratedOutputFile(
                EVIDENCE_INDEX_FILE_NAME,
                evidenceIndexJsonl),
            new GeneratedOutputFile(
                PROJECT_MAP_FILE_NAME,
                projectMapJson),
            new GeneratedOutputFile(
                AGENT_GUIDE_FILE_NAME,
                agentGuideGenerator.generate(projectMapJson, evidenceIndexJsonl))));

    return new Result(
        true,
        scan.endpoints().size(),
        scan.components().size(),
        scan.entities().size(),
        scan.tests().size(),
        evidenceRecords.size());
  }

  private boolean shouldGenerate(
      ProjectLayout layout,
      MavenModuleDiscoveryAnalysis moduleDiscoveryAnalysis,
      MavenMetadataAnalysis metadataAnalysis,
      MavenDependencyAnalysis dependencyAnalysis) {
    return !layout.sourceRoots().isEmpty()
        || !layout.testRoots().isEmpty()
        || !moduleDiscoveryAnalysis.warnings().isEmpty()
        || metadataAnalysis.modules().stream()
            .anyMatch(metadata -> ANALYSIS_ANALYZED.equals(metadata.analysisStatus()))
        || dependencyAnalysis.modules().stream()
            .anyMatch(dependencies -> ANALYSIS_ANALYZED.equals(dependencies.analysisStatus()));
  }

  private ProjectLayout detectLayout(
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      MavenModuleDiscoveryAnalysis moduleDiscoveryAnalysis) throws IOException {
    Optional<EvidenceRecord> buildFileEvidence = buildFileEvidence(
        repositoryRoot,
        canonicalRepositoryRoot);
    BuildMetadata build = buildFileEvidence
        .map(evidence -> new BuildMetadata("maven", ROOT_BUILD_FILE, List.of(evidence.id())))
        .orElseGet(() -> new BuildMetadata("not_detected", null, List.of()));

    ProjectModules modules = projectModules(
        repositoryRoot,
        canonicalRepositoryRoot,
        moduleDiscoveryAnalysis);
    List<String> sourceRoots = modules.items().stream()
        .flatMap(module -> module.sourceRoots().stream())
        .sorted()
        .toList();
    List<String> testRoots = modules.items().stream()
        .flatMap(module -> module.testRoots().stream())
        .sorted()
        .toList();

    return new ProjectLayout(build, sourceRoots, testRoots, modules, buildFileEvidence);
  }

  private ProjectModules projectModules(
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      MavenModuleDiscoveryAnalysis moduleDiscoveryAnalysis) {
    if (!MODULE_ANALYSIS_NOT_DETECTED.equals(moduleDiscoveryAnalysis.analysisStatus())) {
      return new ProjectModules(moduleDiscoveryAnalysis.analysisStatus(), moduleDiscoveryAnalysis.items());
    }

    List<String> sourceRoots = detectedRoots(
        repositoryRoot,
        canonicalRepositoryRoot,
        List.of(MAIN_SOURCE_ROOT));
    List<String> testRoots = detectedRoots(
        repositoryRoot,
        canonicalRepositoryRoot,
        List.of(TEST_SOURCE_ROOT));
    if (sourceRoots.isEmpty() && testRoots.isEmpty()) {
      return new ProjectModules(MODULE_ANALYSIS_NOT_DETECTED, List.of());
    }

    return new ProjectModules(
        MODULE_ANALYSIS_NOT_DETECTED,
        List.of(new MavenModuleItem(
            ROOT_MODULE_ID,
            ".",
            null,
            sourceRoots,
            testRoots,
            MODULE_SUPPORTED,
            "scan_root",
            ".",
            List.of(),
            List.of())));
  }

  private ModuleAwareScan analyzeModules(
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      ProjectModules modules,
      List<MavenModuleWarning> moduleWarnings) throws IOException {
    List<ModuleScopedEndpointFact> endpoints = new ArrayList<>();
    List<ModuleScopedComponentFact> components = new ArrayList<>();
    List<ModuleScopedEntityFact> entities = new ArrayList<>();
    List<ModuleScopedTestFact> tests = new ArrayList<>();
    List<ModuleScopedWarningFact> warnings = new ArrayList<>();
    List<SpringMvcEndpointEvidence> endpointEvidence = new ArrayList<>();
    List<SpringComponentEvidence> componentEvidence = new ArrayList<>();
    List<JpaEntityEvidence> entityEvidence = new ArrayList<>();
    List<TestInventoryEvidence> testEvidence = new ArrayList<>();
    List<AnalysisWarningEvidence> warningEvidence = new ArrayList<>();
    Map<String, Integer> moduleOrder = moduleOrder(modules.items());
    boolean warningAnalyzerRan = !moduleWarnings.isEmpty();
    boolean componentAnalyzerRan = false;
    boolean entityAnalyzerRan = false;
    boolean testAnalyzerRan = false;

    for (MavenModuleWarning warning : moduleWarnings) {
      warnings.add(new ModuleScopedWarningFact(
          warning.id(),
          warning.category(),
          warning.signal(),
          warning.moduleId(),
          moduleOrder.getOrDefault(warning.moduleId(), Integer.MAX_VALUE),
          warning.message(),
          warning.sourcePath(),
          warning.evidenceIds()));
    }

    List<String> childModulePaths = modules.items().stream()
        .map(MavenModuleItem::modulePath)
        .filter(modulePath -> !".".equals(modulePath))
        .toList();

    for (MavenModuleItem module : modules.items()) {
      if (!MODULE_SUPPORTED.equals(module.supportStatus())) {
        continue;
      }

      int order = moduleOrder.getOrDefault(module.moduleId(), Integer.MAX_VALUE);
      List<Path> sourceRoots = module.sourceRoots().stream()
          .map(repositoryRoot::resolve)
          .filter(sourceRoot -> ScanPathContainment.isDirectoryUnderRoot(
              canonicalRepositoryRoot,
              sourceRoot))
          .toList();
      List<Path> testRoots = module.testRoots().stream()
          .map(repositoryRoot::resolve)
          .filter(testRoot -> ScanPathContainment.isDirectoryUnderRoot(
              canonicalRepositoryRoot,
              testRoot))
          .toList();

      if (!sourceRoots.isEmpty()) {
        SpringMvcEndpointAnalysis endpointAnalysis = analyzer.analyze(repositoryRoot, sourceRoots);
        endpointAnalysis.endpoints().forEach(endpoint ->
            endpoints.add(new ModuleScopedEndpointFact(module.moduleId(), order, endpoint)));
        endpointEvidence.addAll(endpointAnalysis.evidence());

        SpringComponentAnalysis componentAnalysis = componentAnalyzer.analyze(repositoryRoot, sourceRoots);
        componentAnalysis.components().forEach(component ->
            components.add(new ModuleScopedComponentFact(module.moduleId(), order, component)));
        componentEvidence.addAll(componentAnalysis.evidence());
        componentAnalyzerRan = true;

        JpaEntityAnalysis entityAnalysis = entityAnalyzer.analyze(repositoryRoot, sourceRoots);
        entityAnalysis.entities().forEach(entity ->
            entities.add(new ModuleScopedEntityFact(module.moduleId(), order, entity)));
        entityEvidence.addAll(entityAnalysis.evidence());
        entityAnalyzerRan = true;
      }

      if (!testRoots.isEmpty()) {
        TestInventoryAnalysis testAnalysis = testInventoryAnalyzer.analyze(
            repositoryRoot,
            sourceRoots,
            testRoots);
        if (!MODULE_ANALYSIS_NOT_DETECTED.equals(testAnalysis.analysisStatus())) {
          testAnalyzerRan = true;
        }
        testAnalysis.tests().forEach(test ->
            tests.add(new ModuleScopedTestFact(module.moduleId(), order, test)));
        testEvidence.addAll(testAnalysis.evidence());
      }

      List<String> excludedModulePaths = ".".equals(module.modulePath()) ? childModulePaths : List.of();
      AnalysisWarningAnalysis warningAnalysis = warningAnalyzer.analyzeModule(
          repositoryRoot,
          module.modulePath(),
          sourceRoots,
          excludedModulePaths);
      warningAnalyzerRan = true;
      warningAnalysis.warnings().forEach(warning ->
          warnings.add(new ModuleScopedWarningFact(
              warning.id(),
              warning.category(),
              warning.signal(),
              module.moduleId(),
              order,
              warning.message(),
              warning.sourcePath(),
              warning.evidenceIds())));
      warningEvidence.addAll(warningAnalysis.evidence());
    }

    return new ModuleAwareScan(
        endpoints.stream().sorted(ENDPOINT_ORDER).toList(),
        warnings.stream().sorted(WARNING_ORDER).toList(),
        components.stream().sorted(COMPONENT_ORDER).toList(),
        entities.stream().sorted(ENTITY_ORDER).toList(),
        tests.stream().sorted(TEST_CLASS_ORDER).toList(),
        warningAnalyzerRan ? ANALYSIS_ANALYZED : MODULE_ANALYSIS_NOT_DETECTED,
        componentAnalyzerRan ? ANALYSIS_ANALYZED : MODULE_ANALYSIS_NOT_DETECTED,
        entityAnalyzerRan ? ANALYSIS_ANALYZED : MODULE_ANALYSIS_NOT_DETECTED,
        testAnalyzerRan ? ANALYSIS_ANALYZED : MODULE_ANALYSIS_NOT_DETECTED,
        endpointEvidence,
        componentEvidence,
        entityEvidence,
        testEvidence,
        warningEvidence);
  }

  private Map<String, Integer> moduleOrder(List<MavenModuleItem> modules) {
    Map<String, Integer> order = new LinkedHashMap<>();
    for (int index = 0; index < modules.size(); index++) {
      order.put(modules.get(index).moduleId(), index);
    }
    return order;
  }

  private Optional<EvidenceRecord> buildFileEvidence(
      Path repositoryRoot,
      Path canonicalRepositoryRoot) throws IOException {
    Path buildFile = repositoryRoot.resolve(ROOT_BUILD_FILE);
    if (!ScanPathContainment.isRegularFileUnderRoot(canonicalRepositoryRoot, buildFile)) {
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

  private List<String> detectedRoots(
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      List<String> candidates) {
    return candidates.stream()
        .filter(candidate -> ScanPathContainment.isDirectoryUnderRoot(
            canonicalRepositoryRoot,
            repositoryRoot.resolve(candidate)))
        .sorted()
        .toList();
  }

  private String endpointsMarkdown(
      ProjectModules modules,
      List<ModuleScopedEndpointFact> endpoints) {
    StringBuilder markdown = new StringBuilder();
    markdown.append("# Endpoints\n\n");

    if (endpoints.isEmpty()) {
      markdown.append("No Spring MVC endpoints detected in supported module source roots.\n");
      return markdown.toString();
    }

    Map<String, MavenModuleItem> moduleById = moduleById(modules.items());
    String currentModuleId = null;
    for (EndpointRow row : endpointRows(endpoints)) {
      if (!row.moduleId().equals(currentModuleId)) {
        currentModuleId = row.moduleId();
        appendEndpointModuleHeading(markdown, currentModuleId, moduleById.get(currentModuleId));
      }
      SpringMvcEndpointFact endpoint = row.endpoint();
      markdown.append("### ")
          .append(MarkdownRenderer.text(row.methodLabel() + " " + row.path()))
          .append("\n\n");
      markdown.append("- Module: ")
          .append(moduleLabel(row.moduleId(), moduleById.get(row.moduleId())))
          .append("\n");
      markdown.append("- Controller: ").append(code(endpoint.controllerClass())).append("\n");
      markdown.append("- Handler: ").append(code(endpoint.handlerMethod())).append("\n");
      markdown.append("- Mapping source: ")
          .append(mappingSourceLabel(endpoint.mappingSource()))
          .append("\n");
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

    return withoutTrailingBlankLine(markdown);
  }

  private String withoutTrailingBlankLine(StringBuilder markdown) {
    if (markdown.length() >= 2
        && markdown.charAt(markdown.length() - 1) == '\n'
        && markdown.charAt(markdown.length() - 2) == '\n') {
      markdown.deleteCharAt(markdown.length() - 1);
    }
    return markdown.toString();
  }

  private Map<String, MavenModuleItem> moduleById(List<MavenModuleItem> modules) {
    Map<String, MavenModuleItem> moduleById = new LinkedHashMap<>();
    for (MavenModuleItem module : modules) {
      moduleById.put(module.moduleId(), module);
    }
    return moduleById;
  }

  private void appendEndpointModuleHeading(
      StringBuilder markdown,
      String moduleId,
      MavenModuleItem module) {
    markdown.append("## Module ")
        .append(moduleLabel(moduleId, module))
        .append("\n\n");
    if (module == null) {
      markdown.append("- Module metadata: not recorded in `project.modules.items`.\n\n");
      return;
    }
    markdown.append("- Module path: ").append(code(module.modulePath())).append("\n");
    markdown.append("- Support status: ").append(code(module.supportStatus())).append("\n\n");
  }

  private String projectMapJson(
      ProjectLayout layout,
      ModuleAwareScan scan,
      MavenMetadataAnalysis metadataAnalysis,
      MavenDependencyAnalysis dependencyAnalysis) {
    StringBuilder json = new StringBuilder();
    json.append("{\n");
    appendIndentedStringField(json, 1, "schema_version", SCHEMA_VERSION, true);
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
    appendIndentedStringArrayField(json, 2, "test_roots", layout.testRoots(), true);
    appendModules(
        json,
        layout.modules(),
        metadataByModuleId(metadataAnalysis),
        dependenciesByModuleId(dependencyAnalysis));
    json.append("  },\n");
    json.append("  \"endpoints\": [");
    if (scan.endpoints().isEmpty()) {
      json.append("],\n");
    } else {
      json.append("\n");
      for (int index = 0; index < scan.endpoints().size(); index++) {
        appendEndpoint(json, scan.endpoints().get(index), index < scan.endpoints().size() - 1);
      }
      json.append("  ],\n");
    }
    json.append("  \"warnings\": {\n");
    appendIndentedStringField(json, 2, "analysis_status", scan.warningAnalysisStatus(), true);
    appendWarnings(json, scan.warnings());
    json.append("  },\n");
    json.append("  \"components\": {\n");
    appendIndentedStringField(json, 2, "analysis_status", scan.componentAnalysisStatus(), true);
    appendComponents(json, scan.components());
    json.append("  },\n");
    json.append("  \"entities\": {\n");
    appendIndentedStringField(json, 2, "analysis_status", scan.entityAnalysisStatus(), true);
    appendEntities(json, scan.entities());
    json.append("  },\n");
    json.append("  \"tests\": {\n");
    appendIndentedStringField(json, 2, "analysis_status", scan.testAnalysisStatus(), true);
    appendTests(json, scan.tests());
    json.append("  }\n");
    json.append("}\n");
    return json.toString();
  }

  private Map<String, MavenModuleMetadata> metadataByModuleId(MavenMetadataAnalysis metadataAnalysis) {
    Map<String, MavenModuleMetadata> metadataByModuleId = new LinkedHashMap<>();
    for (MavenModuleMetadata metadata : metadataAnalysis.modules()) {
      metadataByModuleId.put(metadata.moduleId(), metadata);
    }
    return metadataByModuleId;
  }

  private Map<String, MavenModuleDependencies> dependenciesByModuleId(
      MavenDependencyAnalysis dependencyAnalysis) {
    Map<String, MavenModuleDependencies> dependenciesByModuleId = new LinkedHashMap<>();
    for (MavenModuleDependencies dependencies : dependencyAnalysis.modules()) {
      dependenciesByModuleId.put(dependencies.moduleId(), dependencies);
    }
    return dependenciesByModuleId;
  }

  private void appendModules(
      StringBuilder json,
      ProjectModules modules,
      Map<String, MavenModuleMetadata> metadataByModuleId,
      Map<String, MavenModuleDependencies> dependenciesByModuleId) {
    json.append("    \"modules\": {\n");
    appendIndentedStringField(json, 3, "analysis_status", modules.analysisStatus(), true);
    json.append("      \"items\": [");
    if (modules.items().isEmpty()) {
      json.append("]\n");
      json.append("    }\n");
      return;
    }

    json.append("\n");
    for (int index = 0; index < modules.items().size(); index++) {
      MavenModuleItem module = modules.items().get(index);
      json.append("        {\n");
      appendIndentedStringField(json, 5, "module_id", module.moduleId(), true);
      appendIndentedStringField(json, 5, "module_path", module.modulePath(), true);
      appendIndentedNullableStringField(json, 5, "pom_path", module.pomPath(), true);
      appendIndentedStringArrayField(json, 5, "source_roots", module.sourceRoots(), true);
      appendIndentedStringArrayField(json, 5, "test_roots", module.testRoots(), true);
      appendIndentedStringField(json, 5, "support_status", module.supportStatus(), true);
      appendIndentedStringField(json, 5, "declaration_kind", module.declarationKind(), true);
      appendIndentedStringField(json, 5, "declared_path", module.declaredPath(), true);
      appendIndentedStringArrayField(
          json,
          5,
          "declaration_evidence_ids",
          module.declarationEvidenceIds(),
          true);
      appendIndentedStringArrayField(json, 5, "pom_evidence_ids", module.pomEvidenceIds(), true);
      appendBuildConfig(
          json,
          metadataForModule(module, metadataByModuleId),
          dependenciesForModule(module, dependenciesByModuleId),
          false);
      json.append("        }");
      if (index < modules.items().size() - 1) {
        json.append(",");
      }
      json.append("\n");
    }
    json.append("      ]\n");
    json.append("    }\n");
  }

  private MavenModuleMetadata metadataForModule(
      MavenModuleItem module,
      Map<String, MavenModuleMetadata> metadataByModuleId) {
    MavenModuleMetadata metadata = metadataByModuleId.get(module.moduleId());
    if (metadata != null) {
      return metadata;
    }
    return notDetectedMetadata(module.moduleId());
  }

  private MavenModuleMetadata notDetectedMetadata(String moduleId) {
    MavenMetadataParent parent = new MavenMetadataParent(
        ANALYSIS_NOT_DETECTED,
        MavenMetadataValue.notDeclared(),
        MavenMetadataValue.notDeclared(),
        MavenMetadataValue.notDeclared(),
        MavenMetadataValue.notDeclared());
    return new MavenModuleMetadata(
        moduleId,
        ANALYSIS_NOT_DETECTED,
        MavenMetadataValue.notDeclared(),
        MavenMetadataValue.notDeclared(),
        MavenMetadataValue.notDeclared(),
        MavenMetadataValue.notDeclared(),
        parent);
  }

  private MavenModuleDependencies dependenciesForModule(
      MavenModuleItem module,
      Map<String, MavenModuleDependencies> dependenciesByModuleId) {
    MavenModuleDependencies dependencies = dependenciesByModuleId.get(module.moduleId());
    if (dependencies != null) {
      return dependencies;
    }
    return notDetectedDependencies(module.moduleId());
  }

  private MavenModuleDependencies notDetectedDependencies(String moduleId) {
    return new MavenModuleDependencies(
        moduleId,
        ANALYSIS_NOT_DETECTED,
        List.of(),
        List.of());
  }

  private void appendBuildConfig(
      StringBuilder json,
      MavenModuleMetadata metadata,
      MavenModuleDependencies dependencies,
      boolean trailingComma) {
    indent(json, 5);
    json.append("\"build_config\": {\n");
    appendIndentedStringField(json, 6, "analysis_status", metadata.analysisStatus(), true);
    appendMavenBuildConfig(json, metadata, dependencies);
    appendEmptyItemsSection(json, 6, "resources", ANALYSIS_NOT_ANALYZED, true);
    appendEmptyItemsSection(json, 6, "config_files", ANALYSIS_NOT_ANALYZED, true);
    appendEmptyItemsSection(json, 6, "spring_boot_applications", ANALYSIS_NOT_ANALYZED, false);
    indent(json, 5);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendMavenBuildConfig(
      StringBuilder json,
      MavenModuleMetadata metadata,
      MavenModuleDependencies dependencies) {
    indent(json, 6);
    json.append("\"maven\": {\n");
    appendMavenMetadata(json, metadata);
    appendMavenDependencySection(
        json,
        7,
        "dependencies",
        dependencies.analysisStatus(),
        dependencies.dependencies(),
        true);
    appendMavenDependencySection(
        json,
        7,
        "dependency_management",
        dependencies.analysisStatus(),
        dependencies.dependencyManagement(),
        true);
    appendEmptyItemsSection(json, 7, "plugins", ANALYSIS_NOT_ANALYZED, true);
    appendEmptyItemsSection(json, 7, "plugin_management", ANALYSIS_NOT_ANALYZED, false);
    indent(json, 6);
    json.append("},\n");
  }

  private void appendMavenMetadata(StringBuilder json, MavenModuleMetadata metadata) {
    indent(json, 7);
    json.append("\"metadata\": {\n");
    appendIndentedStringField(json, 8, "analysis_status", metadata.analysisStatus(), true);
    appendMavenValue(json, 8, "group_id", metadata.groupId(), true);
    appendMavenValue(json, 8, "artifact_id", metadata.artifactId(), true);
    appendMavenValue(json, 8, "version", metadata.version(), true);
    appendMavenValue(json, 8, "packaging", metadata.packaging(), true);
    appendMavenParent(json, metadata.parent(), false);
    indent(json, 7);
    json.append("},\n");
  }

  private void appendMavenParent(
      StringBuilder json,
      MavenMetadataParent parent,
      boolean trailingComma) {
    indent(json, 8);
    json.append("\"parent\": {\n");
    appendIndentedStringField(json, 9, "analysis_status", parent.analysisStatus(), true);
    appendMavenValue(json, 9, "group_id", parent.groupId(), true);
    appendMavenValue(json, 9, "artifact_id", parent.artifactId(), true);
    appendMavenValue(json, 9, "version", parent.version(), true);
    appendMavenValue(json, 9, "relative_path", parent.relativePath(), false);
    indent(json, 8);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendMavenValue(
      StringBuilder json,
      int indentLevel,
      String fieldName,
      MavenMetadataValue value,
      boolean trailingComma) {
    indent(json, indentLevel);
    json.append(jsonString(fieldName)).append(": {\n");
    appendIndentedNullableStringField(json, indentLevel + 1, "value", value.value(), true);
    appendIndentedStringField(json, indentLevel + 1, "value_kind", value.valueKind(), true);
    appendIndentedStringArrayField(json, indentLevel + 1, "evidence_ids", value.evidenceIds(), false);
    indent(json, indentLevel);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendMavenDependencySection(
      StringBuilder json,
      int indentLevel,
      String fieldName,
      String analysisStatus,
      List<MavenDependencyDeclaration> dependencies,
      boolean trailingComma) {
    indent(json, indentLevel);
    json.append(jsonString(fieldName)).append(": {\n");
    appendIndentedStringField(json, indentLevel + 1, "analysis_status", analysisStatus, true);
    indent(json, indentLevel + 1);
    json.append("\"items\": [");
    if (dependencies.isEmpty()) {
      json.append("]\n");
      indent(json, indentLevel);
      json.append("}");
      appendLineEnding(json, trailingComma);
      return;
    }

    json.append("\n");
    for (int index = 0; index < dependencies.size(); index++) {
      appendMavenDependency(
          json,
          indentLevel + 2,
          dependencies.get(index),
          index < dependencies.size() - 1);
    }
    indent(json, indentLevel + 1);
    json.append("]\n");
    indent(json, indentLevel);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendMavenDependency(
      StringBuilder json,
      int indentLevel,
      MavenDependencyDeclaration dependency,
      boolean trailingComma) {
    indent(json, indentLevel);
    json.append("{\n");
    appendIndentedStringField(json, indentLevel + 1, "id", dependency.id(), true);
    appendIndentedStringField(
        json,
        indentLevel + 1,
        "declaration_kind",
        dependency.declarationKind(),
        true);
    appendIndentedIntegerField(
        json,
        indentLevel + 1,
        "declaration_ordinal",
        dependency.declarationOrdinal(),
        true);
    appendMavenValue(json, indentLevel + 1, "group_id", dependency.groupId(), true);
    appendMavenValue(json, indentLevel + 1, "artifact_id", dependency.artifactId(), true);
    appendMavenValue(json, indentLevel + 1, "version", dependency.version(), true);
    appendMavenValue(json, indentLevel + 1, "scope", dependency.scope(), true);
    appendMavenValue(json, indentLevel + 1, "optional", dependency.optional(), true);
    appendMavenValue(json, indentLevel + 1, "type", dependency.type(), true);
    appendMavenValue(json, indentLevel + 1, "classifier", dependency.classifier(), true);
    appendIndentedStringArrayField(
        json,
        indentLevel + 1,
        "evidence_ids",
        dependency.evidenceIds(),
        false);
    indent(json, indentLevel);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendEmptyItemsSection(
      StringBuilder json,
      int indentLevel,
      String fieldName,
      String analysisStatus,
      boolean trailingComma) {
    indent(json, indentLevel);
    json.append(jsonString(fieldName)).append(": {\n");
    appendIndentedStringField(json, indentLevel + 1, "analysis_status", analysisStatus, true);
    indent(json, indentLevel + 1);
    json.append("\"items\": []\n");
    indent(json, indentLevel);
    json.append("}");
    appendLineEnding(json, trailingComma);
  }

  private void appendEndpoint(
      StringBuilder json,
      ModuleScopedEndpointFact scopedEndpoint,
      boolean trailingComma) {
    SpringMvcEndpointFact endpoint = scopedEndpoint.fact();
    json.append("    {\n");
    appendIndentedStringField(json, 3, "id", endpointId(scopedEndpoint.moduleId(), endpoint), true);
    appendIndentedStringField(json, 3, "module_id", scopedEndpoint.moduleId(), true);
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
    appendMappingSource(json, endpoint.mappingSource());
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

  private void appendMappingSource(
      StringBuilder json,
      SpringMvcEndpointMappingSource mappingSource) {
    json.append("      \"mapping_source\": {\n");
    appendIndentedStringField(json, 4, "kind", mappingSource.kind(), true);
    appendIndentedStringField(json, 4, "declaring_type", mappingSource.declaringType(), true);
    appendIndentedStringField(json, 4, "declaring_method", mappingSource.declaringMethod(), true);
    appendIndentedStringField(json, 4, "binding", mappingSource.binding(), true);
    appendIndentedNullableStringField(json, 4, "uncertainty", mappingSource.uncertainty(), true);
    appendIndentedStringArrayField(json, 4, "evidence_ids", mappingSource.evidenceIds(), false);
    json.append("      },\n");
  }

  private void appendComponents(StringBuilder json, List<ModuleScopedComponentFact> components) {
    json.append("    \"items\": [");
    if (components.isEmpty()) {
      json.append("]\n");
      return;
    }

    json.append("\n");
    for (int index = 0; index < components.size(); index++) {
      appendComponent(json, components.get(index), index < components.size() - 1);
    }
    json.append("    ]\n");
  }

  private void appendComponent(
      StringBuilder json,
      ModuleScopedComponentFact scopedComponent,
      boolean trailingComma) {
    SpringComponentFact component = scopedComponent.fact();
    json.append("      {\n");
    appendIndentedStringField(json, 4, "id", componentId(scopedComponent.moduleId(), component), true);
    appendIndentedStringField(json, 4, "module_id", scopedComponent.moduleId(), true);
    appendIndentedStringField(json, 4, "class_name", component.className(), true);
    appendIndentedStringArrayField(json, 4, "stereotypes", component.stereotypes(), true);
    appendIndentedStringArrayField(json, 4, "evidence_ids", component.evidenceIds(), false);
    json.append("      }");
    if (trailingComma) {
      json.append(",");
    }
    json.append("\n");
  }

  private void appendWarnings(StringBuilder json, List<ModuleScopedWarningFact> warnings) {
    json.append("    \"items\": [");
    if (warnings.isEmpty()) {
      json.append("]\n");
      return;
    }

    json.append("\n");
    for (int index = 0; index < warnings.size(); index++) {
      appendWarning(json, warnings.get(index), index < warnings.size() - 1);
    }
    json.append("    ]\n");
  }

  private void appendWarning(
      StringBuilder json,
      ModuleScopedWarningFact warning,
      boolean trailingComma) {
    json.append("      {\n");
    appendIndentedStringField(json, 4, "id", warning.id(), true);
    appendIndentedStringField(json, 4, "category", warning.category(), true);
    appendIndentedStringField(json, 4, "signal", warning.signal(), true);
    appendIndentedNullableStringField(json, 4, "module_id", warning.moduleId(), true);
    appendIndentedStringField(json, 4, "message", warning.message(), true);
    appendIndentedStringField(json, 4, "source_path", warning.sourcePath(), true);
    appendIndentedStringArrayField(json, 4, "evidence_ids", warning.evidenceIds(), false);
    json.append("      }");
    if (trailingComma) {
      json.append(",");
    }
    json.append("\n");
  }

  private void appendEntities(StringBuilder json, List<ModuleScopedEntityFact> entities) {
    json.append("    \"items\": [");
    if (entities.isEmpty()) {
      json.append("]\n");
      return;
    }

    json.append("\n");
    for (int index = 0; index < entities.size(); index++) {
      appendEntity(json, entities.get(index), index < entities.size() - 1);
    }
    json.append("    ]\n");
  }

  private void appendEntity(
      StringBuilder json,
      ModuleScopedEntityFact scopedEntity,
      boolean trailingComma) {
    JpaEntityFact entity = scopedEntity.fact();
    json.append("      {\n");
    appendIndentedStringField(json, 4, "id", entityId(scopedEntity.moduleId(), entity), true);
    appendIndentedStringField(json, 4, "module_id", scopedEntity.moduleId(), true);
    appendIndentedStringField(json, 4, "class_name", entity.className(), true);
    appendIndentedNullableStringField(json, 4, "table_name", entity.tableName(), true);
    appendIdentifierFields(json, entity.identifierFields());
    appendRelationships(json, entity.relationships());
    appendIndentedStringArrayField(json, 4, "evidence_ids", entity.evidenceIds(), false);
    json.append("      }");
    if (trailingComma) {
      json.append(",");
    }
    json.append("\n");
  }

  private void appendIdentifierFields(
      StringBuilder json,
      List<JpaIdentifierFieldFact> identifierFields) {
    json.append("        \"identifier_fields\": [");
    List<JpaIdentifierFieldFact> sortedIdentifierFields = identifierFields.stream()
        .sorted(IDENTIFIER_FIELD_ORDER)
        .toList();
    if (sortedIdentifierFields.isEmpty()) {
      json.append("],\n");
      return;
    }

    json.append("\n");
    for (int index = 0; index < sortedIdentifierFields.size(); index++) {
      JpaIdentifierFieldFact identifierField = sortedIdentifierFields.get(index);
      json.append("          {\n");
      appendIndentedStringField(json, 6, "field_name", identifierField.fieldName(), true);
      appendIndentedStringField(json, 6, "java_type", identifierField.javaType(), true);
      appendIndentedStringField(json, 6, "declaring_class", identifierField.declaringClass(), true);
      appendIndentedStringField(json, 6, "source_kind", identifierField.sourceKind(), true);
      appendIndentedStringArrayField(json, 6, "evidence_ids", identifierField.evidenceIds(), false);
      json.append("          }");
      if (index < sortedIdentifierFields.size() - 1) {
        json.append(",");
      }
      json.append("\n");
    }
    json.append("        ],\n");
  }

  private void appendRelationships(
      StringBuilder json,
      List<JpaRelationshipFact> relationships) {
    json.append("        \"relationships\": [");
    List<JpaRelationshipFact> sortedRelationships = relationships.stream()
        .sorted(RELATIONSHIP_ORDER)
        .toList();
    if (sortedRelationships.isEmpty()) {
      json.append("],\n");
      return;
    }

    json.append("\n");
    for (int index = 0; index < sortedRelationships.size(); index++) {
      JpaRelationshipFact relationship = sortedRelationships.get(index);
      json.append("          {\n");
      appendIndentedStringField(json, 6, "field_name", relationship.fieldName(), true);
      appendIndentedStringField(json, 6, "annotation", relationship.annotation(), true);
      appendIndentedStringField(json, 6, "java_type", relationship.javaType(), true);
      appendIndentedStringField(json, 6, "target_resolution", relationship.targetResolution(), true);
      appendIndentedStringField(json, 6, "uncertainty", relationship.uncertainty(), true);
      appendIndentedStringArrayField(json, 6, "evidence_ids", relationship.evidenceIds(), false);
      json.append("          }");
      if (index < sortedRelationships.size() - 1) {
        json.append(",");
      }
      json.append("\n");
    }
    json.append("        ],\n");
  }

  private void appendTests(StringBuilder json, List<ModuleScopedTestFact> tests) {
    json.append("    \"items\": [");
    if (tests.isEmpty()) {
      json.append("]\n");
      return;
    }

    json.append("\n");
    for (int index = 0; index < tests.size(); index++) {
      appendTest(json, tests.get(index), index < tests.size() - 1);
    }
    json.append("    ]\n");
  }

  private void appendTest(
      StringBuilder json,
      ModuleScopedTestFact scopedTest,
      boolean trailingComma) {
    TestClassFact test = scopedTest.fact();
    json.append("      {\n");
    appendIndentedStringField(json, 4, "module_id", scopedTest.moduleId(), true);
    appendIndentedStringField(json, 4, "class_name", test.className(), true);
    appendIndentedStringField(json, 4, "source_path", test.sourcePath(), true);
    appendFrameworkSignals(json, test.frameworkSignals());
    appendTestedSubjects(json, scopedTest.moduleId(), test.testedSubjects());
    appendIndentedStringArrayField(json, 4, "evidence_ids", test.evidenceIds(), false);
    json.append("      }");
    if (trailingComma) {
      json.append(",");
    }
    json.append("\n");
  }

  private void appendFrameworkSignals(
      StringBuilder json,
      List<TestFrameworkSignalFact> frameworkSignals) {
    json.append("        \"framework_signals\": [");
    List<TestFrameworkSignalFact> sortedSignals = frameworkSignals.stream()
        .sorted(TEST_FRAMEWORK_SIGNAL_ORDER)
        .toList();
    if (sortedSignals.isEmpty()) {
      json.append("],\n");
      return;
    }

    json.append("\n");
    for (int index = 0; index < sortedSignals.size(); index++) {
      TestFrameworkSignalFact signal = sortedSignals.get(index);
      json.append("          {\n");
      appendIndentedStringField(json, 6, "name", signal.name(), true);
      appendIndentedStringArrayField(json, 6, "evidence_ids", signal.evidenceIds(), false);
      json.append("          }");
      if (index < sortedSignals.size() - 1) {
        json.append(",");
      }
      json.append("\n");
    }
    json.append("        ],\n");
  }

  private void appendTestedSubjects(
      StringBuilder json,
      String targetModuleId,
      List<TestedSubjectFact> testedSubjects) {
    json.append("        \"tested_subjects\": [");
    List<TestedSubjectFact> sortedSubjects = testedSubjects.stream()
        .sorted(TESTED_SUBJECT_ORDER)
        .toList();
    if (sortedSubjects.isEmpty()) {
      json.append("],\n");
      return;
    }

    json.append("\n");
    for (int index = 0; index < sortedSubjects.size(); index++) {
      TestedSubjectFact subject = sortedSubjects.get(index);
      json.append("          {\n");
      appendIndentedStringField(json, 6, "class_name", subject.className(), true);
      appendIndentedStringField(json, 6, "target_module_id", targetModuleId, true);
      appendIndentedStringField(json, 6, "support_type", subject.supportType(), true);
      appendIndentedStringField(json, 6, "confidence", subject.confidence(), true);
      appendIndentedNullableStringField(json, 6, "uncertainty", subject.uncertainty(), true);
      appendIndentedStringArrayField(json, 6, "evidence_ids", subject.evidenceIds(), false);
      json.append("          }");
      if (index < sortedSubjects.size() - 1) {
        json.append(",");
      }
      json.append("\n");
    }
    json.append("        ],\n");
  }

  private static String endpointId(String moduleId, SpringMvcEndpointFact endpoint) {
    if (ROOT_MODULE_ID.equals(moduleId)) {
      return "endpoint:" + endpoint.controllerClass() + "#" + endpoint.handlerMethod();
    }
    return "endpoint:" + moduleId + ":" + endpoint.controllerClass() + "#" + endpoint.handlerMethod();
  }

  private static String componentId(String moduleId, SpringComponentFact component) {
    if (ROOT_MODULE_ID.equals(moduleId)) {
      return component.id();
    }
    return "component:" + moduleId + ":" + component.className();
  }

  private static String entityId(String moduleId, JpaEntityFact entity) {
    if (ROOT_MODULE_ID.equals(moduleId)) {
      return entity.id();
    }
    return "entity:" + moduleId + ":" + entity.className();
  }

  private List<EndpointRow> endpointRows(List<ModuleScopedEndpointFact> endpoints) {
    List<EndpointRow> rows = new ArrayList<>();
    for (ModuleScopedEndpointFact scopedEndpoint : endpoints) {
      SpringMvcEndpointFact endpoint = scopedEndpoint.fact();
      List<String> methodLabels = endpointMethodLabels(endpoint);
      for (String path : endpoint.paths()) {
        for (String methodLabel : methodLabels) {
          rows.add(new EndpointRow(
              scopedEndpoint.moduleId(),
              scopedEndpoint.moduleOrder(),
              methodLabel,
              path,
              endpoint));
        }
      }
    }

    return rows.stream()
        .sorted(Comparator.comparingInt(EndpointRow::moduleOrder)
            .thenComparing(EndpointRow::path)
            .thenComparing(EndpointRow::methodLabel)
            .thenComparing(row -> row.endpoint().controllerClass())
            .thenComparing(row -> row.endpoint().handlerMethod()))
        .toList();
  }

  private String moduleLabel(String moduleId, MavenModuleItem module) {
    if (module == null) {
      return code(moduleId) + " (module path not recorded)";
    }
    return code(moduleId) + " (" + code(module.modulePath()) + ")";
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

  private String mappingSourceLabel(SpringMvcEndpointMappingSource mappingSource) {
    return code(mappingSource.kind())
        + " ("
        + code(mappingSource.declaringType() + "#" + mappingSource.declaringMethod())
        + ")";
  }

  private List<EvidenceRecord> evidenceRecords(
      ProjectLayout layout,
      List<MavenModuleDiscoveryEvidence> moduleEvidenceRecords,
      List<MavenMetadataEvidence> metadataEvidenceRecords,
      List<MavenDependencyEvidence> dependencyEvidenceRecords,
      List<SpringMvcEndpointEvidence> endpointEvidenceRecords,
      List<SpringComponentEvidence> componentEvidenceRecords,
      List<JpaEntityEvidence> entityEvidenceRecords,
      List<TestInventoryEvidence> testEvidenceRecords,
      List<AnalysisWarningEvidence> warningEvidenceRecords) {
    Map<String, EvidenceRecord> uniqueRecords = new LinkedHashMap<>();
    layout.buildFileEvidence().ifPresent(evidence -> uniqueRecords.put(evidence.id(), evidence));
    moduleEvidenceRecords.stream()
        .map(this::evidenceRecord)
        .forEach(evidence -> uniqueRecords.putIfAbsent(evidence.id(), evidence));
    metadataEvidenceRecords.stream()
        .map(this::evidenceRecord)
        .forEach(evidence -> uniqueRecords.putIfAbsent(evidence.id(), evidence));
    dependencyEvidenceRecords.stream()
        .map(this::evidenceRecord)
        .forEach(evidence -> uniqueRecords.putIfAbsent(evidence.id(), evidence));
    endpointEvidenceRecords.stream()
        .map(this::evidenceRecord)
        .forEach(evidence -> uniqueRecords.putIfAbsent(evidence.id(), evidence));
    componentEvidenceRecords.stream()
        .map(this::evidenceRecord)
        .forEach(evidence -> uniqueRecords.putIfAbsent(evidence.id(), evidence));
    entityEvidenceRecords.stream()
        .map(this::evidenceRecord)
        .forEach(evidence -> uniqueRecords.putIfAbsent(evidence.id(), evidence));
    testEvidenceRecords.stream()
        .map(this::evidenceRecord)
        .forEach(evidence -> uniqueRecords.putIfAbsent(evidence.id(), evidence));
    warningEvidenceRecords.stream()
        .map(this::evidenceRecord)
        .forEach(evidence -> uniqueRecords.putIfAbsent(evidence.id(), evidence));

    return uniqueRecords.values().stream()
        .sorted(EVIDENCE_ORDER)
        .toList();
  }

  private EvidenceRecord evidenceRecord(SpringMvcEndpointEvidence evidence) {
    return new EvidenceRecord(
        evidence.id(),
        evidence.sourceType(),
        evidence.sourcePath(),
        evidence.className(),
        evidence.methodName(),
        evidence.symbolName(),
        evidence.lineStart(),
        evidence.lineEnd(),
        evidence.excerpt(),
        evidence.confidence());
  }

  private EvidenceRecord evidenceRecord(MavenModuleDiscoveryEvidence evidence) {
    return new EvidenceRecord(
        evidence.id(),
        evidence.sourceType(),
        evidence.sourcePath(),
        evidence.className(),
        evidence.methodName(),
        evidence.symbolName(),
        evidence.lineStart(),
        evidence.lineEnd(),
        evidence.excerpt(),
        evidence.confidence());
  }

  private EvidenceRecord evidenceRecord(MavenMetadataEvidence evidence) {
    return new EvidenceRecord(
        evidence.id(),
        evidence.sourceType(),
        evidence.sourcePath(),
        evidence.className(),
        evidence.methodName(),
        evidence.symbolName(),
        evidence.lineStart(),
        evidence.lineEnd(),
        evidence.excerpt(),
        evidence.confidence());
  }

  private EvidenceRecord evidenceRecord(MavenDependencyEvidence evidence) {
    return new EvidenceRecord(
        evidence.id(),
        evidence.sourceType(),
        evidence.sourcePath(),
        evidence.className(),
        evidence.methodName(),
        evidence.symbolName(),
        evidence.lineStart(),
        evidence.lineEnd(),
        evidence.excerpt(),
        evidence.confidence());
  }

  private EvidenceRecord evidenceRecord(SpringComponentEvidence evidence) {
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

  private EvidenceRecord evidenceRecord(JpaEntityEvidence evidence) {
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

  private EvidenceRecord evidenceRecord(TestInventoryEvidence evidence) {
    return new EvidenceRecord(
        evidence.id(),
        evidence.sourceType(),
        evidence.sourcePath(),
        evidence.className(),
        evidence.methodName(),
        evidence.symbolName(),
        evidence.lineStart(),
        evidence.lineEnd(),
        evidence.excerpt(),
        evidence.confidence());
  }

  private EvidenceRecord evidenceRecord(AnalysisWarningEvidence evidence) {
    return new EvidenceRecord(
        evidence.id(),
        evidence.sourceType(),
        evidence.sourcePath(),
        evidence.className(),
        evidence.methodName(),
        evidence.symbolName(),
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

  private void appendIndentedIntegerField(
      StringBuilder json,
      int indentLevel,
      String name,
      int value,
      boolean trailingComma) {
    indent(json, indentLevel);
    json.append(jsonString(name)).append(": ").append(value);
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
          if (character == 0x2028) {
            escaped.append("\\u2028");
          } else if (character == 0x2029) {
            escaped.append("\\u2029");
          } else if (character < 0x20) {
            escaped.append(String.format(Locale.ROOT, "\\u%04x", (int) character));
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
    return MarkdownRenderer.inlineCodeList(values, "none detected");
  }

  private String code(String value) {
    return MarkdownRenderer.inlineCode(value);
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

  private void writeGeneratedFiles(
      Path canonicalRepositoryRoot,
      Path outputDirectory,
      List<GeneratedOutputFile> files) throws IOException {
    for (GeneratedOutputFile file : files) {
      validateGeneratedOutputTarget(
          canonicalRepositoryRoot,
          outputDirectory.resolve(file.fileName()));
    }

    for (GeneratedOutputFile file : files) {
      writeGeneratedFile(
          canonicalRepositoryRoot,
          outputDirectory,
          file.fileName(),
          file.content());
    }
  }

  private void writeGeneratedFile(
      Path canonicalRepositoryRoot,
      Path outputDirectory,
      String fileName,
      String content) throws IOException {
    Path target = outputDirectory.resolve(fileName);
    validateGeneratedOutputTarget(canonicalRepositoryRoot, target);

    Path tempFile = Files.createTempFile(outputDirectory, "." + fileName + ".", ".tmp");
    boolean moved = false;
    try {
      if (!isRegularFileUnderRoot(canonicalRepositoryRoot, tempFile)) {
        throw new IOException(
            "Temporary output file is not a regular file under scan root: " + tempFile);
      }

      Files.writeString(tempFile, content, StandardCharsets.UTF_8);
      validateGeneratedOutputTarget(canonicalRepositoryRoot, target);
      moveGeneratedFile(tempFile, target);
      moved = true;

      if (!isRegularFileUnderRoot(canonicalRepositoryRoot, target)) {
        throw new IOException(
            "Output file target is not a regular file under scan root: " + target);
      }
    } finally {
      if (!moved) {
        Files.deleteIfExists(tempFile);
      }
    }
  }

  private void validateGeneratedOutputTarget(Path canonicalRepositoryRoot, Path target)
      throws IOException {
    if (Files.isSymbolicLink(target)) {
      throw new IOException("Output file must not be a symbolic link: " + target);
    }

    if (!Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
      return;
    }

    if (!isRegularFileUnderRoot(canonicalRepositoryRoot, target)) {
      throw new IOException(
          "Output file target is not a regular file under scan root: " + target);
    }

    Long linkCount = hardLinkCount(target);
    if (linkCount != null && linkCount > 1) {
      throw new IOException("Output file must not have multiple hard links: " + target);
    }
  }

  private void moveGeneratedFile(Path tempFile, Path target) throws IOException {
    try {
      Files.move(
          tempFile,
          target,
          StandardCopyOption.ATOMIC_MOVE,
          StandardCopyOption.REPLACE_EXISTING);
    } catch (AtomicMoveNotSupportedException ex) {
      Files.move(tempFile, target, StandardCopyOption.REPLACE_EXISTING);
    }
  }

  private Long hardLinkCount(Path target) throws IOException {
    try {
      Object value = Files.getAttribute(target, "unix:nlink", LinkOption.NOFOLLOW_LINKS);
      if (value instanceof Number number) {
        return number.longValue();
      }
      return null;
    } catch (IllegalArgumentException | UnsupportedOperationException ex) {
      return null;
    }
  }

  private boolean isRegularFileUnderRoot(Path canonicalRepositoryRoot, Path target) {
    return ScanPathContainment.realPathUnderRoot(canonicalRepositoryRoot, target)
        .filter(Files::isRegularFile)
        .isPresent();
  }

  public record Result(
      boolean generated,
      int endpointCount,
      int componentCount,
      int entityCount,
      int testCount,
      int evidenceCount) {
  }

  private record GeneratedOutputFile(String fileName, String content) {
  }

  private record EndpointRow(
      String moduleId,
      int moduleOrder,
      String methodLabel,
      String path,
      SpringMvcEndpointFact endpoint) {
  }

  private record ProjectLayout(
      BuildMetadata build,
      List<String> sourceRoots,
      List<String> testRoots,
      ProjectModules modules,
      Optional<EvidenceRecord> buildFileEvidence) {
    private ProjectLayout {
      sourceRoots = List.copyOf(sourceRoots);
      testRoots = List.copyOf(testRoots);
      modules = Objects.requireNonNull(modules, "modules");
    }
  }

  private record ProjectModules(String analysisStatus, List<MavenModuleItem> items) {
    private ProjectModules {
      items = List.copyOf(items);
    }
  }

  private record BuildMetadata(String system, String rootBuildFile, List<String> evidenceIds) {
    private BuildMetadata {
      evidenceIds = List.copyOf(evidenceIds);
    }
  }

  private record ModuleAwareScan(
      List<ModuleScopedEndpointFact> endpoints,
      List<ModuleScopedWarningFact> warnings,
      List<ModuleScopedComponentFact> components,
      List<ModuleScopedEntityFact> entities,
      List<ModuleScopedTestFact> tests,
      String warningAnalysisStatus,
      String componentAnalysisStatus,
      String entityAnalysisStatus,
      String testAnalysisStatus,
      List<SpringMvcEndpointEvidence> endpointEvidence,
      List<SpringComponentEvidence> componentEvidence,
      List<JpaEntityEvidence> entityEvidence,
      List<TestInventoryEvidence> testEvidence,
      List<AnalysisWarningEvidence> warningEvidence) {
    private ModuleAwareScan {
      endpoints = List.copyOf(endpoints);
      warnings = List.copyOf(warnings);
      components = List.copyOf(components);
      entities = List.copyOf(entities);
      tests = List.copyOf(tests);
      endpointEvidence = List.copyOf(endpointEvidence);
      componentEvidence = List.copyOf(componentEvidence);
      entityEvidence = List.copyOf(entityEvidence);
      testEvidence = List.copyOf(testEvidence);
      warningEvidence = List.copyOf(warningEvidence);
    }
  }

  private record ModuleScopedEndpointFact(
      String moduleId,
      int moduleOrder,
      SpringMvcEndpointFact fact) {
  }

  private record ModuleScopedComponentFact(
      String moduleId,
      int moduleOrder,
      SpringComponentFact fact) {
  }

  private record ModuleScopedEntityFact(
      String moduleId,
      int moduleOrder,
      JpaEntityFact fact) {
  }

  private record ModuleScopedTestFact(
      String moduleId,
      int moduleOrder,
      TestClassFact fact) {
  }

  private record ModuleScopedWarningFact(
      String id,
      String category,
      String signal,
      String moduleId,
      int moduleOrder,
      String message,
      String sourcePath,
      List<String> evidenceIds) {
    private ModuleScopedWarningFact {
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
