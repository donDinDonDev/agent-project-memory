package io.github.dondindondev.agentprojectmemory.analyzer.maven;

import io.github.dondindondev.agentprojectmemory.analyzer.EvidenceExcerpts;
import io.github.dondindondev.agentprojectmemory.analyzer.ScanPathContainment;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.DefaultHandler2;

public final class MavenPluginAnalyzer {
  private static final String ANALYSIS_STATUS_ANALYZED = "analyzed";
  private static final String ANALYSIS_STATUS_NOT_DETECTED = "not_detected";
  private static final String BUILD_FILE_SOURCE_TYPE = "build_file";
  private static final String HIGH_CONFIDENCE = "high";
  private static final String DIRECT_PLUGIN = "direct_plugin";
  private static final String PLUGIN_MANAGEMENT = "plugin_management";
  private static final String OPENAPI_SWAGGER_CODEGEN = "openapi_swagger_codegen";
  private static final String SOURCE_GENERATOR_PLUGIN = "source_generator_plugin";
  private static final String ANNOTATION_PROCESSOR = "annotation_processor";
  private static final String INPUT_SPEC_CONFIG_PRESENT = "input_spec_config_present";
  private static final String GENERATED_SOURCES_CONFIG_PRESENT = "generated_sources_config_present";
  private static final String ANNOTATION_PROCESSOR_PATHS_PRESENT = "annotation_processor_paths_present";
  private static final String ADD_SOURCE_GOAL_PRESENT = "add_source_goal_present";
  private static final Pattern PROPERTY_REFERENCE = Pattern.compile("\\$\\{[^}]+}");
  private static final Set<String> OPENAPI_SWAGGER_CODEGEN_ARTIFACT_IDS = Set.of(
      "openapi-generator-maven-plugin",
      "swagger-codegen-maven-plugin");
  private static final Set<String> SOURCE_GENERATOR_PLUGIN_ARTIFACT_IDS = Set.of(
      "avro-maven-plugin",
      "cxf-codegen-plugin",
      "jaxb2-maven-plugin",
      "jaxws-maven-plugin",
      "jsonschema2pojo-maven-plugin",
      "protobuf-maven-plugin");
  private static final Set<String> ANNOTATION_PROCESSOR_PLUGIN_ARTIFACT_IDS = Set.of(
      "apt-maven-plugin",
      "maven-processor-plugin");
  private static final Set<String> BUILD_HELPER_PLUGIN_ARTIFACT_IDS = Set.of(
      "build-helper-maven-plugin");
  private static final Comparator<MavenPluginDeclaration> PLUGIN_ORDER = Comparator
      .comparing(
          (MavenPluginDeclaration plugin) -> nullLast(plugin.groupId().value()),
          Comparator.nullsLast(String::compareTo))
      .thenComparing(
          plugin -> nullLast(plugin.artifactId().value()),
          Comparator.nullsLast(String::compareTo))
      .thenComparingInt(MavenPluginDeclaration::declarationOrdinal)
      .thenComparing(MavenPluginDeclaration::id);
  private static final Comparator<MavenPluginSignal> SIGNAL_ORDER = Comparator
      .comparing(MavenPluginSignal::signal)
      .thenComparing(signal -> String.join(",", signal.evidenceIds()));
  private static final Comparator<MavenPluginEvidence> EVIDENCE_ORDER = Comparator
      .comparing(MavenPluginEvidence::sourcePath)
      .thenComparing(evidence -> evidence.lineStart() == null ? Integer.MAX_VALUE : evidence.lineStart())
      .thenComparing(evidence -> evidence.lineEnd() == null ? Integer.MAX_VALUE : evidence.lineEnd())
      .thenComparing(evidence -> nullSafe(evidence.className()))
      .thenComparing(evidence -> nullSafe(evidence.methodName()))
      .thenComparing(MavenPluginEvidence::symbolName)
      .thenComparing(MavenPluginEvidence::id);

  public MavenPluginAnalysis analyze(Path repositoryRoot, List<MavenModuleItem> modules)
      throws IOException {
    Objects.requireNonNull(repositoryRoot, "repositoryRoot");
    Objects.requireNonNull(modules, "modules");

    Path normalizedRepositoryRoot = repositoryRoot.toAbsolutePath().normalize();
    Path canonicalRepositoryRoot = ScanPathContainment.canonicalRoot(normalizedRepositoryRoot);
    Map<String, MavenPluginEvidence> evidence = new LinkedHashMap<>();
    List<MavenModulePlugins> modulePlugins = new ArrayList<>();

    for (MavenModuleItem module : modules) {
      if (module.pomPath() == null || module.pomPath().isBlank()) {
        modulePlugins.add(notDetectedPlugins(module.moduleId()));
        continue;
      }

      Path pom = normalizedRepositoryRoot.resolve(module.pomPath()).normalize();
      if (!ScanPathContainment.isRegularFileUnderRootNoFollow(canonicalRepositoryRoot, pom)) {
        modulePlugins.add(notDetectedPlugins(module.moduleId()));
        continue;
      }

      ParsedPomPlugins parsedPom = parsePomPlugins(
          pom,
          repositoryRelativePath(normalizedRepositoryRoot, pom));
      modulePlugins.add(modulePlugins(module.moduleId(), parsedPom));
      parsedPom.evidence().forEach(record -> evidence.putIfAbsent(record.id(), record));
    }

    return new MavenPluginAnalysis(
        modulePlugins,
        evidence.values().stream()
            .sorted(EVIDENCE_ORDER)
            .toList());
  }

  private ParsedPomPlugins parsePomPlugins(Path pom, String sourcePath) throws IOException {
    byte[] pomBytes = ScanPathContainment.readRegularFileBytesNoFollowStable(pom, Integer.MAX_VALUE);
    List<String> sourceLines = utf8Lines(pomBytes);
    PluginElementHandler handler = new PluginElementHandler();
    SAXParser parser;
    try {
      parser = secureSaxParserFactory().newSAXParser();
    } catch (ParserConfigurationException | SAXException exception) {
      throw new IOException("Unable to configure secure XML parser for " + pom, exception);
    }

    try (InputStream input = new ByteArrayInputStream(pomBytes)) {
      parser.parse(input, handler);
    } catch (SAXException exception) {
      throw malformedPomException(sourcePath, exception);
    }

    List<EvidenceCandidate> candidates = new ArrayList<>();
    for (PluginDeclarationElement declaration : handler.declarations()) {
      candidates.add(evidenceCandidate(
          declaration,
          evidenceBaseId(sourcePath, declaration),
          declaration.symbolName(),
          declaration.lineStart(),
          declaration.lineEnd(),
          "Maven " + declaration.kind().contractName() + " declaration detected.",
          "decl:" + ordinalText(declaration.ordinal())));
      for (PluginValueElement value : declaration.values()) {
        candidates.add(evidenceCandidate(
            value,
            evidenceBaseId(sourcePath, declaration, value),
            value.symbolName(declaration.kind(), declaration.ordinal()),
            value.lineStart(),
            value.lineEnd(),
            evidenceExcerpt(sourceLines, value),
            "decl:" + ordinalText(declaration.ordinal())
                + ":value:" + ordinalText(value.fieldOrdinal())));
      }
      for (PluginExecutionElement execution : declaration.executions()) {
        candidates.add(evidenceCandidate(
            execution,
            evidenceBaseId(sourcePath, declaration, execution),
            execution.symbolName(declaration.kind(), declaration.ordinal()),
            execution.lineStart(),
            execution.lineEnd(),
            "Maven plugin execution declaration detected.",
            "decl:" + ordinalText(declaration.ordinal())
                + ":execution:" + ordinalText(execution.ordinal())));
        for (ExecutionValueElement value : execution.values()) {
          candidates.add(evidenceCandidate(
              value,
              evidenceBaseId(sourcePath, declaration, execution, value),
              value.symbolName(declaration.kind(), declaration.ordinal(), execution.ordinal()),
              value.lineStart(),
              value.lineEnd(),
              evidenceExcerpt(sourceLines, value),
              "decl:" + ordinalText(declaration.ordinal())
                  + ":execution:" + ordinalText(execution.ordinal())
                  + ":value:" + ordinalText(value.fieldOrdinal())));
        }
      }
      for (ConfigurationSignalElement signal : declaration.configurationSignals()) {
        candidates.add(evidenceCandidate(
            signal,
            evidenceBaseId(sourcePath, declaration, signal),
            signal.symbolName(declaration.kind(), declaration.ordinal()),
            signal.lineStart(),
            signal.lineEnd(),
            "Maven plugin configuration signal detected: " + signal.elementName(),
            "decl:" + ordinalText(declaration.ordinal())
                + ":signal:" + ordinalText(signal.signalOrdinal())));
      }
    }

    Map<String, Integer> baseIdCounts = new LinkedHashMap<>();
    for (EvidenceCandidate candidate : candidates) {
      baseIdCounts.merge(candidate.evidenceBaseId(), 1, Integer::sum);
    }

    Map<PluginDeclarationElement, MavenPluginEvidence> declarationEvidence = new LinkedHashMap<>();
    Map<PluginValueElement, MavenPluginEvidence> valueEvidence = new LinkedHashMap<>();
    Map<PluginExecutionElement, MavenPluginEvidence> executionEvidence = new LinkedHashMap<>();
    Map<ExecutionValueElement, MavenPluginEvidence> executionValueEvidence = new LinkedHashMap<>();
    Map<ConfigurationSignalElement, MavenPluginEvidence> configurationSignalEvidence =
        new LinkedHashMap<>();
    List<MavenPluginEvidence> evidence = new ArrayList<>();
    for (EvidenceCandidate candidate : candidates) {
      String evidenceId = candidate.evidenceBaseId();
      if (baseIdCounts.getOrDefault(evidenceId, 0) > 1) {
        evidenceId += ":" + candidate.collisionDiscriminator();
      }

      MavenPluginEvidence evidenceRecord = new MavenPluginEvidence(
          evidenceId,
          BUILD_FILE_SOURCE_TYPE,
          sourcePath,
          null,
          null,
          candidate.symbolName(),
          candidate.lineStart(),
          candidate.lineEnd(),
          EvidenceExcerpts.bounded(candidate.excerpt()),
          HIGH_CONFIDENCE);
      evidence.add(evidenceRecord);

      Object key = candidate.key();
      if (key instanceof PluginDeclarationElement declaration) {
        declarationEvidence.put(declaration, evidenceRecord);
      } else if (key instanceof PluginValueElement value) {
        valueEvidence.put(value, evidenceRecord);
      } else if (key instanceof PluginExecutionElement execution) {
        executionEvidence.put(execution, evidenceRecord);
      } else if (key instanceof ExecutionValueElement value) {
        executionValueEvidence.put(value, evidenceRecord);
      } else if (key instanceof ConfigurationSignalElement signal) {
        configurationSignalEvidence.put(signal, evidenceRecord);
      }
    }

    List<ObservedPomPlugin> observed = new ArrayList<>();
    for (PluginDeclarationElement declaration : handler.declarations()) {
      List<ObservedPluginValue> values = declaration.values().stream()
          .map(value -> new ObservedPluginValue(
              value.field(),
              value.rawText(),
              value.hasNestedElement(),
              valueEvidence.get(value)))
          .toList();
      List<ObservedPluginExecution> executions = declaration.executions().stream()
          .map(execution -> observedExecution(
              execution,
              executionEvidence,
              executionValueEvidence))
          .toList();
      List<ObservedConfigurationSignal> configurationSignals = declaration.configurationSignals().stream()
          .map(signal -> new ObservedConfigurationSignal(
              signal.signal(),
              signal.elementName(),
              configurationSignalEvidence.get(signal)))
          .toList();
      observed.add(new ObservedPomPlugin(
          new ObservedPluginDeclaration(
              declaration.kind(),
              declaration.ordinal(),
              declarationEvidence.get(declaration)),
          values,
          executions,
          configurationSignals));
    }

    return new ParsedPomPlugins(observed, evidence);
  }

  private ObservedPluginExecution observedExecution(
      PluginExecutionElement execution,
      Map<PluginExecutionElement, MavenPluginEvidence> executionEvidence,
      Map<ExecutionValueElement, MavenPluginEvidence> executionValueEvidence) {
    List<ObservedExecutionValue> values = execution.values().stream()
        .map(value -> new ObservedExecutionValue(
            value.field(),
            value.rawText(),
            value.hasNestedElement(),
            executionValueEvidence.get(value)))
        .toList();
    return new ObservedPluginExecution(
        execution.ordinal(),
        executionEvidence.get(execution),
        values);
  }

  private MavenModulePlugins modulePlugins(String moduleId, ParsedPomPlugins parsedPom) {
    List<MavenPluginDeclaration> directPlugins = new ArrayList<>();
    List<MavenPluginDeclaration> managedPlugins = new ArrayList<>();
    for (ObservedPomPlugin plugin : parsedPom.plugins()) {
      MavenPluginDeclaration declaration = pluginDeclaration(moduleId, plugin);
      if (plugin.declaration().kind() == PluginDeclarationKind.DIRECT) {
        directPlugins.add(declaration);
      } else {
        managedPlugins.add(declaration);
      }
    }

    return new MavenModulePlugins(
        moduleId,
        ANALYSIS_STATUS_ANALYZED,
        directPlugins.stream().sorted(PLUGIN_ORDER).toList(),
        managedPlugins.stream().sorted(PLUGIN_ORDER).toList());
  }

  private MavenPluginDeclaration pluginDeclaration(String moduleId, ObservedPomPlugin plugin) {
    Map<PluginField, List<ObservedPluginValue>> byField = new EnumMap<>(PluginField.class);
    for (ObservedPluginValue value : plugin.values()) {
      byField.computeIfAbsent(value.field(), ignored -> new ArrayList<>()).add(value);
    }

    MavenMetadataValue groupId = value(byField.getOrDefault(PluginField.GROUP_ID, List.of()));
    MavenMetadataValue artifactId = value(byField.getOrDefault(PluginField.ARTIFACT_ID, List.of()));
    MavenMetadataValue version = value(byField.getOrDefault(PluginField.VERSION, List.of()));
    List<MavenPluginExecution> executions = plugin.executions().stream()
        .map(this::pluginExecution)
        .toList();
    List<MavenPluginSignal> configurationSignals =
        configurationSignals(plugin, artifactId).stream().sorted(SIGNAL_ORDER).toList();
    List<MavenPluginSignal> generatorSignals =
        generatorSignals(plugin, artifactId, configurationSignals).stream().sorted(SIGNAL_ORDER).toList();
    String declarationKind = plugin.declaration().kind() == PluginDeclarationKind.DIRECT
        ? DIRECT_PLUGIN
        : PLUGIN_MANAGEMENT;

    return new MavenPluginDeclaration(
        pluginId(
            moduleId,
            plugin.declaration().kind(),
            plugin.declaration().ordinal(),
            groupId,
            artifactId),
        declarationKind,
        plugin.declaration().ordinal(),
        groupId,
        artifactId,
        version,
        executions,
        configurationSignals,
        generatorSignals,
        List.of(plugin.declaration().evidence().id()));
  }

  private MavenPluginExecution pluginExecution(ObservedPluginExecution execution) {
    List<ObservedExecutionValue> ids = values(execution, ExecutionField.ID);
    List<ObservedExecutionValue> phases = values(execution, ExecutionField.PHASE);
    List<ObservedExecutionValue> goals = values(execution, ExecutionField.GOAL);
    List<String> evidenceIds = new ArrayList<>();
    evidenceIds.add(execution.evidence().id());
    ids.stream().map(value -> value.evidence().id()).forEach(evidenceIds::add);

    return new MavenPluginExecution(
        directTextOrNull(ids),
        value(phases),
        goals.stream().map(this::value).toList(),
        evidenceIds);
  }

  private List<ObservedExecutionValue> values(
      ObservedPluginExecution execution,
      ExecutionField field) {
    return execution.values().stream()
        .filter(value -> value.field() == field)
        .toList();
  }

  private String directTextOrNull(List<ObservedExecutionValue> values) {
    if (values.size() != 1) {
      return null;
    }
    ObservedExecutionValue value = values.get(0);
    String text = value.rawText().trim();
    if (value.hasNestedElement() || text.isEmpty()) {
      return null;
    }
    return text;
  }

  private MavenMetadataValue value(List<? extends ObservedMavenValue> values) {
    if (values.isEmpty()) {
      return MavenMetadataValue.notDeclared();
    }

    List<String> evidenceIds = values.stream()
        .map(value -> value.evidence().id())
        .toList();
    if (values.size() != 1) {
      return MavenMetadataValue.unsupported(evidenceIds);
    }

    return value(values.get(0));
  }

  private MavenMetadataValue value(ObservedMavenValue value) {
    List<String> evidenceIds = List.of(value.evidence().id());
    String text = value.rawText().trim();
    if (value.hasNestedElement() || text.isEmpty()) {
      return MavenMetadataValue.unsupported(evidenceIds);
    }
    return new MavenMetadataValue(text, valueKind(text), evidenceIds);
  }

  private List<MavenPluginSignal> configurationSignals(
      ObservedPomPlugin plugin,
      MavenMetadataValue artifactId) {
    Map<String, LinkedHashSet<String>> evidenceIdsBySignal = new LinkedHashMap<>();
    for (ObservedConfigurationSignal signal : plugin.configurationSignals()) {
      addSignalEvidence(evidenceIdsBySignal, signal.signal(), signal.evidence().id());
    }

    if (BUILD_HELPER_PLUGIN_ARTIFACT_IDS.contains(artifactId.value())) {
      plugin.executions().stream()
          .flatMap(execution -> values(execution, ExecutionField.GOAL).stream())
          .filter(goal -> "add-source".equals(goal.rawText().trim()) && !goal.hasNestedElement())
          .map(goal -> goal.evidence().id())
          .forEach(evidenceId -> addSignalEvidence(
              evidenceIdsBySignal,
              ADD_SOURCE_GOAL_PRESENT,
              evidenceId));
    }

    return signalRecords(evidenceIdsBySignal);
  }

  private List<MavenPluginSignal> generatorSignals(
      ObservedPomPlugin plugin,
      MavenMetadataValue artifactId,
      List<MavenPluginSignal> configurationSignals) {
    Map<String, LinkedHashSet<String>> evidenceIdsBySignal = new LinkedHashMap<>();
    if (OPENAPI_SWAGGER_CODEGEN_ARTIFACT_IDS.contains(artifactId.value())) {
      artifactId.evidenceIds().forEach(evidenceId ->
          addSignalEvidence(evidenceIdsBySignal, OPENAPI_SWAGGER_CODEGEN, evidenceId));
    } else if (SOURCE_GENERATOR_PLUGIN_ARTIFACT_IDS.contains(artifactId.value())) {
      artifactId.evidenceIds().forEach(evidenceId ->
          addSignalEvidence(evidenceIdsBySignal, SOURCE_GENERATOR_PLUGIN, evidenceId));
    }

    if (ANNOTATION_PROCESSOR_PLUGIN_ARTIFACT_IDS.contains(artifactId.value())) {
      artifactId.evidenceIds().forEach(evidenceId ->
          addSignalEvidence(evidenceIdsBySignal, ANNOTATION_PROCESSOR, evidenceId));
    }

    configurationSignals.stream()
        .filter(signal -> ANNOTATION_PROCESSOR_PATHS_PRESENT.equals(signal.signal()))
        .flatMap(signal -> signal.evidenceIds().stream())
        .forEach(evidenceId -> addSignalEvidence(
            evidenceIdsBySignal,
            ANNOTATION_PROCESSOR,
            evidenceId));

    return signalRecords(evidenceIdsBySignal);
  }

  private void addSignalEvidence(
      Map<String, LinkedHashSet<String>> evidenceIdsBySignal,
      String signal,
      String evidenceId) {
    evidenceIdsBySignal
        .computeIfAbsent(signal, ignored -> new LinkedHashSet<>())
        .add(evidenceId);
  }

  private List<MavenPluginSignal> signalRecords(
      Map<String, LinkedHashSet<String>> evidenceIdsBySignal) {
    return evidenceIdsBySignal.entrySet().stream()
        .map(entry -> new MavenPluginSignal(entry.getKey(), List.copyOf(entry.getValue())))
        .toList();
  }

  private MavenModulePlugins notDetectedPlugins(String moduleId) {
    return new MavenModulePlugins(
        moduleId,
        ANALYSIS_STATUS_NOT_DETECTED,
        List.of(),
        List.of());
  }

  private String pluginId(
      String moduleId,
      PluginDeclarationKind kind,
      int ordinal,
      MavenMetadataValue groupId,
      MavenMetadataValue artifactId) {
    String kindSegment = kind == PluginDeclarationKind.DIRECT ? "direct" : PLUGIN_MANAGEMENT;
    return "maven_plugin:" + moduleId + ":" + kindSegment + ":"
        + idComponent(groupId, "group_id")
        + ":"
        + idComponent(artifactId, "artifact_id")
        + ":decl:"
        + ordinalText(ordinal);
  }

  private String idComponent(MavenMetadataValue value, String fieldName) {
    if (value.value() == null || value.value().isBlank()) {
      return fieldName + ":" + value.valueKind();
    }
    return value.value().trim()
        .replace('\n', ' ')
        .replace('\r', ' ')
        .replace('\t', ' ');
  }

  private String valueKind(String text) {
    if (PROPERTY_REFERENCE.matcher(text).matches()) {
      return "property_reference";
    }
    if (text.contains("${")) {
      return "expression";
    }
    return "literal";
  }

  private IOException malformedPomException(String sourcePath, SAXException exception) {
    String location = "";
    if (exception instanceof SAXParseException parseException) {
      int line = parseException.getLineNumber();
      int column = parseException.getColumnNumber();
      if (line > 0 && column > 0) {
        location = " at line " + line + ", column " + column;
      } else if (line > 0) {
        location = " at line " + line;
      }
    }
    return new IOException(
        "Could not parse Maven plugins in "
            + sourcePath
            + ": malformed XML"
            + location
            + ".",
        exception);
  }

  private SAXParserFactory secureSaxParserFactory() throws ParserConfigurationException, SAXException {
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setXIncludeAware(false);
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
    factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    return factory;
  }

  private EvidenceCandidate evidenceCandidate(
      Object key,
      String evidenceBaseId,
      String symbolName,
      Integer lineStart,
      Integer lineEnd,
      String excerpt,
      String collisionDiscriminator) {
    return new EvidenceCandidate(
        key,
        evidenceBaseId,
        symbolName,
        lineStart,
        lineEnd,
        excerpt,
        collisionDiscriminator);
  }

  private String evidenceBaseId(String sourcePath, PluginDeclarationElement declaration) {
    return "ev:" + sourcePath + ":" + lineRange(declaration.lineStart(), declaration.lineEnd())
        + ":build_file:" + declaration.symbolName();
  }

  private String evidenceBaseId(
      String sourcePath,
      PluginDeclarationElement declaration,
      PluginValueElement value) {
    return "ev:" + sourcePath + ":" + lineRange(value.lineStart(), value.lineEnd())
        + ":build_file:" + value.symbolName(declaration.kind(), declaration.ordinal());
  }

  private String evidenceBaseId(
      String sourcePath,
      PluginDeclarationElement declaration,
      PluginExecutionElement execution) {
    return "ev:" + sourcePath + ":" + lineRange(execution.lineStart(), execution.lineEnd())
        + ":build_file:" + execution.symbolName(declaration.kind(), declaration.ordinal());
  }

  private String evidenceBaseId(
      String sourcePath,
      PluginDeclarationElement declaration,
      PluginExecutionElement execution,
      ExecutionValueElement value) {
    return "ev:" + sourcePath + ":" + lineRange(value.lineStart(), value.lineEnd())
        + ":build_file:" + value.symbolName(
            declaration.kind(),
            declaration.ordinal(),
            execution.ordinal());
  }

  private String evidenceBaseId(
      String sourcePath,
      PluginDeclarationElement declaration,
      ConfigurationSignalElement signal) {
    return "ev:" + sourcePath + ":" + lineRange(signal.lineStart(), signal.lineEnd())
        + ":build_file:" + signal.symbolName(declaration.kind(), declaration.ordinal());
  }

  private String evidenceExcerpt(List<String> sourceLines, PluginValueElement value) {
    Integer lineStart = value.lineStart();
    Integer lineEnd = value.lineEnd();
    String excerpt;
    if (lineStart != null
        && lineEnd != null
        && lineStart >= 1
        && lineEnd >= lineStart
        && lineEnd <= sourceLines.size()) {
      excerpt = EvidenceExcerpts.sourceLines(sourceLines, lineStart, lineEnd);
    } else {
      excerpt = "<" + value.field().elementName() + ">"
          + value.rawText().trim()
          + "</"
          + value.field().elementName()
          + ">";
    }
    return EvidenceExcerpts.bounded(excerpt);
  }

  private String evidenceExcerpt(List<String> sourceLines, ExecutionValueElement value) {
    Integer lineStart = value.lineStart();
    Integer lineEnd = value.lineEnd();
    String elementName = value.field().elementName();
    String excerpt;
    if (lineStart != null
        && lineEnd != null
        && lineStart >= 1
        && lineEnd >= lineStart
        && lineEnd <= sourceLines.size()) {
      excerpt = EvidenceExcerpts.sourceLines(sourceLines, lineStart, lineEnd);
    } else {
      excerpt = "<" + elementName + ">"
          + value.rawText().trim()
          + "</"
          + elementName
          + ">";
    }
    return EvidenceExcerpts.bounded(excerpt);
  }

  private String lineRange(Integer lineStart, Integer lineEnd) {
    if (lineStart == null || lineEnd == null) {
      return "unknown";
    }
    return lineStart + "-" + lineEnd;
  }

  private String repositoryRelativePath(Path repositoryRoot, Path path) {
    Path relativePath = repositoryRoot.relativize(path.toAbsolutePath().normalize());
    return relativePath.toString().replace(path.getFileSystem().getSeparator(), "/");
  }

  private List<String> utf8Lines(byte[] bytes) {
    return new String(bytes, StandardCharsets.UTF_8).lines().toList();
  }

  private static String ordinalText(int ordinal) {
    return String.format("%06d", ordinal);
  }

  private static String nullLast(String value) {
    return value;
  }

  private static String nullSafe(String value) {
    return value == null ? "" : value;
  }

  private interface ObservedMavenValue {
    String rawText();

    boolean hasNestedElement();

    MavenPluginEvidence evidence();
  }

  private enum PluginDeclarationKind {
    DIRECT("maven:plugin", "direct plugin"),
    MANAGEMENT("maven:plugin_management", "plugin-management");

    private final String symbolPrefix;
    private final String contractName;

    PluginDeclarationKind(String symbolPrefix, String contractName) {
      this.symbolPrefix = symbolPrefix;
      this.contractName = contractName;
    }

    private String symbolPrefix() {
      return symbolPrefix;
    }

    private String contractName() {
      return contractName;
    }
  }

  private enum PluginField {
    GROUP_ID("groupId"),
    ARTIFACT_ID("artifactId"),
    VERSION("version");

    private final String elementName;

    PluginField(String elementName) {
      this.elementName = elementName;
    }

    private String elementName() {
      return elementName;
    }
  }

  private enum ExecutionField {
    ID("id"),
    PHASE("phase"),
    GOAL("goal");

    private final String elementName;

    ExecutionField(String elementName) {
      this.elementName = elementName;
    }

    private String elementName() {
      return elementName;
    }
  }

  private enum ConfigurationSignal {
    INPUT_SPEC(INPUT_SPEC_CONFIG_PRESENT, Set.of(
        "inputSpec",
        "inputSpecRootDirectory",
        "inputSpecRootDirectories")),
    GENERATED_SOURCES(GENERATED_SOURCES_CONFIG_PRESENT, Set.of(
        "generatedSourceDirectory",
        "generatedSourceRoot",
        "generatedSources",
        "generatedSourcesDirectory",
        "sourceRoot")),
    ANNOTATION_PROCESSOR_PATHS(ANNOTATION_PROCESSOR_PATHS_PRESENT, Set.of(
        "annotationProcessorPaths"));

    private final String signalName;
    private final Set<String> elementNames;

    ConfigurationSignal(String signalName, Set<String> elementNames) {
      this.signalName = signalName;
      this.elementNames = Set.copyOf(elementNames);
    }

    private String signalName() {
      return signalName;
    }

    private static ConfigurationSignal forElementName(String elementName) {
      for (ConfigurationSignal signal : values()) {
        if (signal.elementNames.contains(elementName)) {
          return signal;
        }
      }
      return null;
    }
  }

  private record PluginValueElement(
      PluginField field,
      int fieldOrdinal,
      String rawText,
      Integer lineStart,
      Integer lineEnd,
      boolean hasNestedElement) {
    private String symbolName(PluginDeclarationKind kind, int ordinal) {
      return kind.symbolPrefix() + ":" + ordinalText(ordinal) + ":" + field.elementName();
    }
  }

  private record ExecutionValueElement(
      ExecutionField field,
      int fieldOrdinal,
      String rawText,
      Integer lineStart,
      Integer lineEnd,
      boolean hasNestedElement) {
    private String symbolName(PluginDeclarationKind kind, int pluginOrdinal, int executionOrdinal) {
      return kind.symbolPrefix()
          + ":"
          + ordinalText(pluginOrdinal)
          + ":execution:"
          + ordinalText(executionOrdinal)
          + ":"
          + field.elementName()
          + ":"
          + ordinalText(fieldOrdinal);
    }
  }

  private record ConfigurationSignalElement(
      String signal,
      String elementName,
      int signalOrdinal,
      Integer lineStart,
      Integer lineEnd,
      boolean executionScoped,
      int executionOrdinal) {
    private String symbolName(PluginDeclarationKind kind, int pluginOrdinal) {
      if (executionScoped) {
        return kind.symbolPrefix()
            + ":"
            + ordinalText(pluginOrdinal)
            + ":execution:"
            + ordinalText(executionOrdinal)
            + ":configuration:"
            + elementName;
      }
      return kind.symbolPrefix()
          + ":"
          + ordinalText(pluginOrdinal)
          + ":configuration:"
          + elementName;
    }
  }

  private record PluginExecutionElement(
      int ordinal,
      Integer lineStart,
      Integer lineEnd,
      List<ExecutionValueElement> values) {
    private PluginExecutionElement {
      values = List.copyOf(values);
    }

    private String symbolName(PluginDeclarationKind kind, int pluginOrdinal) {
      return kind.symbolPrefix()
          + ":"
          + ordinalText(pluginOrdinal)
          + ":execution:"
          + ordinalText(ordinal);
    }
  }

  private record PluginDeclarationElement(
      PluginDeclarationKind kind,
      int ordinal,
      Integer lineStart,
      Integer lineEnd,
      List<PluginValueElement> values,
      List<PluginExecutionElement> executions,
      List<ConfigurationSignalElement> configurationSignals) {
    private PluginDeclarationElement {
      values = List.copyOf(values);
      executions = List.copyOf(executions);
      configurationSignals = List.copyOf(configurationSignals);
    }

    private String symbolName() {
      return kind.symbolPrefix() + ":" + ordinalText(ordinal);
    }
  }

  private record EvidenceCandidate(
      Object key,
      String evidenceBaseId,
      String symbolName,
      Integer lineStart,
      Integer lineEnd,
      String excerpt,
      String collisionDiscriminator) {
  }

  private record ObservedPluginDeclaration(
      PluginDeclarationKind kind,
      int ordinal,
      MavenPluginEvidence evidence) {
  }

  private record ObservedPluginValue(
      PluginField field,
      String rawText,
      boolean hasNestedElement,
      MavenPluginEvidence evidence) implements ObservedMavenValue {
  }

  private record ObservedExecutionValue(
      ExecutionField field,
      String rawText,
      boolean hasNestedElement,
      MavenPluginEvidence evidence) implements ObservedMavenValue {
  }

  private record ObservedPluginExecution(
      int ordinal,
      MavenPluginEvidence evidence,
      List<ObservedExecutionValue> values) {
    private ObservedPluginExecution {
      values = List.copyOf(values);
    }
  }

  private record ObservedConfigurationSignal(
      String signal,
      String elementName,
      MavenPluginEvidence evidence) {
  }

  private record ObservedPomPlugin(
      ObservedPluginDeclaration declaration,
      List<ObservedPluginValue> values,
      List<ObservedPluginExecution> executions,
      List<ObservedConfigurationSignal> configurationSignals) {
    private ObservedPomPlugin {
      values = List.copyOf(values);
      executions = List.copyOf(executions);
      configurationSignals = List.copyOf(configurationSignals);
    }
  }

  private record ParsedPomPlugins(
      List<ObservedPomPlugin> plugins,
      List<MavenPluginEvidence> evidence) {
    private ParsedPomPlugins {
      plugins = List.copyOf(plugins);
      evidence = List.copyOf(evidence);
    }
  }

  private static final class PluginElementHandler extends DefaultHandler2 {
    private final List<String> elementStack = new ArrayList<>();
    private final List<MutablePluginDeclaration> declarations = new ArrayList<>();
    private final Map<PluginDeclarationKind, Integer> declarationOrdinals =
        new EnumMap<>(PluginDeclarationKind.class);
    private final StringBuilder elementText = new StringBuilder();
    private Locator locator;
    private MutablePluginDeclaration currentDeclaration;
    private MutablePluginExecution currentExecution;
    private MutableConfigurationSignal currentConfigurationSignal;
    private boolean readingValue;
    private boolean nestedElement;
    private PluginField currentPluginField;
    private ExecutionField currentExecutionField;
    private Integer valueLineStart;

    @Override
    public void setDocumentLocator(Locator locator) {
      this.locator = locator;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
      if (readingValue) {
        nestedElement = true;
      }

      elementStack.add(elementName(localName, qName));
      PluginDeclarationKind declarationKind = pluginDeclarationKindForCurrentPath();
      if (declarationKind != null) {
        int ordinal = declarationOrdinals.merge(declarationKind, 1, Integer::sum);
        currentDeclaration = new MutablePluginDeclaration(
            declarationKind,
            ordinal,
            currentLineNumber());
        return;
      }

      if (currentDeclaration == null) {
        return;
      }

      if (isExecutionStartPath()) {
        currentExecution = new MutablePluginExecution(
            currentDeclaration.nextExecutionOrdinal(),
            currentLineNumber());
        return;
      }

      ConfigurationSignal configurationSignal = configurationSignalForCurrentPath();
      if (currentConfigurationSignal == null && configurationSignal != null) {
        currentConfigurationSignal = new MutableConfigurationSignal(
            configurationSignal.signalName(),
            elementStack.get(elementStack.size() - 1),
            currentDeclaration.nextConfigurationSignalOrdinal(),
            currentLineNumber(),
            currentExecution != null,
            currentExecution == null ? 0 : currentExecution.ordinal());
      }

      PluginField pluginField = pluginFieldForCurrentPath();
      if (currentExecution == null && pluginField != null) {
        startReadingPluginValue(pluginField);
        return;
      }

      ExecutionField executionField = executionFieldForCurrentPath();
      if (currentExecution != null && executionField != null) {
        startReadingExecutionValue(executionField);
      }
    }

    @Override
    public void characters(char[] characters, int start, int length) {
      if (readingValue) {
        elementText.append(characters, start, length);
      }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
      if (readingValue && currentPluginField != null
          && pluginFieldForCurrentPath() == currentPluginField) {
        currentDeclaration.addValue(new PluginValueElement(
            currentPluginField,
            currentDeclaration.nextFieldOrdinal(currentPluginField),
            elementText.toString(),
            valueLineStart,
            currentLineNumber(),
            nestedElement));
        stopReadingValue();
      } else if (readingValue && currentExecutionField != null
          && executionFieldForCurrentPath() == currentExecutionField) {
        currentExecution.addValue(new ExecutionValueElement(
            currentExecutionField,
            currentExecution.nextFieldOrdinal(currentExecutionField),
            elementText.toString(),
            valueLineStart,
            currentLineNumber(),
            nestedElement));
        stopReadingValue();
      }

      if (currentConfigurationSignal != null
          && currentConfigurationSignal.elementName().equals(currentElementName())) {
        currentConfigurationSignal.setLineEnd(currentLineNumber());
        currentDeclaration.addConfigurationSignal(currentConfigurationSignal.toElement());
        currentConfigurationSignal = null;
      }

      if (currentExecution != null && isCurrentExecutionPath()) {
        currentExecution.setLineEnd(currentLineNumber());
        currentDeclaration.addExecution(currentExecution.toElement());
        currentExecution = null;
      }

      if (currentDeclaration != null && isCurrentPluginPath(currentDeclaration.kind())) {
        currentDeclaration.setLineEnd(currentLineNumber());
        declarations.add(currentDeclaration);
        currentDeclaration = null;
      }

      if (!elementStack.isEmpty()) {
        elementStack.remove(elementStack.size() - 1);
      }
    }

    private void startReadingPluginValue(PluginField field) {
      readingValue = true;
      nestedElement = false;
      currentPluginField = field;
      currentExecutionField = null;
      valueLineStart = currentLineNumber();
      elementText.setLength(0);
    }

    private void startReadingExecutionValue(ExecutionField field) {
      readingValue = true;
      nestedElement = false;
      currentPluginField = null;
      currentExecutionField = field;
      valueLineStart = currentLineNumber();
      elementText.setLength(0);
    }

    private void stopReadingValue() {
      readingValue = false;
      nestedElement = false;
      currentPluginField = null;
      currentExecutionField = null;
      valueLineStart = null;
      elementText.setLength(0);
    }

    private List<PluginDeclarationElement> declarations() {
      return declarations.stream()
          .map(MutablePluginDeclaration::toElement)
          .toList();
    }

    private PluginDeclarationKind pluginDeclarationKindForCurrentPath() {
      if (hasPath("project", "build", "plugins", "plugin")) {
        return PluginDeclarationKind.DIRECT;
      }
      if (hasPath("project", "build", "pluginManagement", "plugins", "plugin")) {
        return PluginDeclarationKind.MANAGEMENT;
      }
      return null;
    }

    private boolean isCurrentPluginPath(PluginDeclarationKind kind) {
      if (kind == PluginDeclarationKind.DIRECT) {
        return hasPath("project", "build", "plugins", "plugin");
      }
      return hasPath("project", "build", "pluginManagement", "plugins", "plugin");
    }

    private boolean isExecutionStartPath() {
      return hasPath("project", "build", "plugins", "plugin", "executions", "execution")
          || hasPath(
              "project",
              "build",
              "pluginManagement",
              "plugins",
              "plugin",
              "executions",
              "execution");
    }

    private boolean isCurrentExecutionPath() {
      return isExecutionStartPath();
    }

    private PluginField pluginFieldForCurrentPath() {
      if (currentDeclaration == null || elementStack.isEmpty()) {
        return null;
      }

      int expectedSize = currentDeclaration.kind() == PluginDeclarationKind.DIRECT ? 5 : 6;
      if (elementStack.size() != expectedSize) {
        return null;
      }
      if (currentDeclaration.kind() == PluginDeclarationKind.DIRECT
          && !hasPrefix("project", "build", "plugins", "plugin")) {
        return null;
      }
      if (currentDeclaration.kind() == PluginDeclarationKind.MANAGEMENT
          && !hasPrefix("project", "build", "pluginManagement", "plugins", "plugin")) {
        return null;
      }

      String elementName = currentElementName();
      for (PluginField field : PluginField.values()) {
        if (field.elementName().equals(elementName)) {
          return field;
        }
      }
      return null;
    }

    private ExecutionField executionFieldForCurrentPath() {
      if (currentExecution == null || elementStack.isEmpty()) {
        return null;
      }

      if (hasPath("project", "build", "plugins", "plugin", "executions", "execution", "id")
          || hasPath(
              "project",
              "build",
              "pluginManagement",
              "plugins",
              "plugin",
              "executions",
              "execution",
              "id")) {
        return ExecutionField.ID;
      }
      if (hasPath("project", "build", "plugins", "plugin", "executions", "execution", "phase")
          || hasPath(
              "project",
              "build",
              "pluginManagement",
              "plugins",
              "plugin",
              "executions",
              "execution",
              "phase")) {
        return ExecutionField.PHASE;
      }
      if (hasPath(
              "project",
              "build",
              "plugins",
              "plugin",
              "executions",
              "execution",
              "goals",
              "goal")
          || hasPath(
              "project",
              "build",
              "pluginManagement",
              "plugins",
              "plugin",
              "executions",
              "execution",
              "goals",
              "goal")) {
        return ExecutionField.GOAL;
      }
      return null;
    }

    private ConfigurationSignal configurationSignalForCurrentPath() {
      if (currentDeclaration == null || elementStack.isEmpty()) {
        return null;
      }
      String elementName = currentElementName();
      ConfigurationSignal signal = ConfigurationSignal.forElementName(elementName);
      if (signal == null) {
        return null;
      }
      if (currentDeclaration.kind() == PluginDeclarationKind.DIRECT) {
        if (hasPrefix("project", "build", "plugins", "plugin", "configuration")
            && elementStack.size() > 5) {
          return signal;
        }
        if (hasPrefix(
                "project",
                "build",
                "plugins",
                "plugin",
                "executions",
                "execution",
                "configuration")
            && elementStack.size() > 7) {
          return signal;
        }
      } else {
        if (hasPrefix(
                "project",
                "build",
                "pluginManagement",
                "plugins",
                "plugin",
                "configuration")
            && elementStack.size() > 6) {
          return signal;
        }
        if (hasPrefix(
                "project",
                "build",
                "pluginManagement",
                "plugins",
                "plugin",
                "executions",
                "execution",
                "configuration")
            && elementStack.size() > 8) {
          return signal;
        }
      }
      return null;
    }

    private boolean hasPath(String... path) {
      if (elementStack.size() != path.length) {
        return false;
      }
      return hasPrefix(path);
    }

    private boolean hasPrefix(String... path) {
      if (elementStack.size() < path.length) {
        return false;
      }
      for (int index = 0; index < path.length; index++) {
        if (!path[index].equals(elementStack.get(index))) {
          return false;
        }
      }
      return true;
    }

    private Integer currentLineNumber() {
      if (locator == null || locator.getLineNumber() < 1) {
        return null;
      }
      return locator.getLineNumber();
    }

    private String currentElementName() {
      if (elementStack.isEmpty()) {
        return "";
      }
      return elementStack.get(elementStack.size() - 1);
    }

    private String elementName(String localName, String qName) {
      String name = localName == null || localName.isBlank() ? qName : localName;
      int prefixEnd = name.indexOf(':');
      if (prefixEnd >= 0) {
        return name.substring(prefixEnd + 1);
      }
      return name;
    }
  }

  private static final class MutablePluginDeclaration {
    private final PluginDeclarationKind kind;
    private final int ordinal;
    private final Integer lineStart;
    private final List<PluginValueElement> values = new ArrayList<>();
    private final List<PluginExecutionElement> executions = new ArrayList<>();
    private final List<ConfigurationSignalElement> configurationSignals = new ArrayList<>();
    private final Map<PluginField, Integer> fieldOrdinals = new EnumMap<>(PluginField.class);
    private Integer lineEnd;
    private int executionOrdinal;
    private int configurationSignalOrdinal;

    private MutablePluginDeclaration(
        PluginDeclarationKind kind,
        int ordinal,
        Integer lineStart) {
      this.kind = kind;
      this.ordinal = ordinal;
      this.lineStart = lineStart;
    }

    private PluginDeclarationKind kind() {
      return kind;
    }

    private void addValue(PluginValueElement value) {
      values.add(value);
    }

    private int nextFieldOrdinal(PluginField field) {
      return fieldOrdinals.merge(field, 1, Integer::sum);
    }

    private int nextExecutionOrdinal() {
      executionOrdinal++;
      return executionOrdinal;
    }

    private int nextConfigurationSignalOrdinal() {
      configurationSignalOrdinal++;
      return configurationSignalOrdinal;
    }

    private void addExecution(PluginExecutionElement execution) {
      executions.add(execution);
    }

    private void addConfigurationSignal(ConfigurationSignalElement signal) {
      configurationSignals.add(signal);
    }

    private void setLineEnd(Integer lineEnd) {
      this.lineEnd = lineEnd;
    }

    private PluginDeclarationElement toElement() {
      return new PluginDeclarationElement(
          kind,
          ordinal,
          lineStart,
          lineEnd,
          values,
          executions,
          configurationSignals);
    }
  }

  private static final class MutablePluginExecution {
    private final int ordinal;
    private final Integer lineStart;
    private final List<ExecutionValueElement> values = new ArrayList<>();
    private final Map<ExecutionField, Integer> fieldOrdinals = new EnumMap<>(ExecutionField.class);
    private Integer lineEnd;

    private MutablePluginExecution(int ordinal, Integer lineStart) {
      this.ordinal = ordinal;
      this.lineStart = lineStart;
    }

    private int ordinal() {
      return ordinal;
    }

    private void addValue(ExecutionValueElement value) {
      values.add(value);
    }

    private int nextFieldOrdinal(ExecutionField field) {
      return fieldOrdinals.merge(field, 1, Integer::sum);
    }

    private void setLineEnd(Integer lineEnd) {
      this.lineEnd = lineEnd;
    }

    private PluginExecutionElement toElement() {
      return new PluginExecutionElement(ordinal, lineStart, lineEnd, values);
    }
  }

  private static final class MutableConfigurationSignal {
    private final String signal;
    private final String elementName;
    private final int signalOrdinal;
    private final Integer lineStart;
    private final boolean executionScoped;
    private final int executionOrdinal;
    private Integer lineEnd;

    private MutableConfigurationSignal(
        String signal,
        String elementName,
        int signalOrdinal,
        Integer lineStart,
        boolean executionScoped,
        int executionOrdinal) {
      this.signal = signal;
      this.elementName = elementName;
      this.signalOrdinal = signalOrdinal;
      this.lineStart = lineStart;
      this.executionScoped = executionScoped;
      this.executionOrdinal = executionOrdinal;
    }

    private String elementName() {
      return elementName;
    }

    private void setLineEnd(Integer lineEnd) {
      this.lineEnd = lineEnd;
    }

    private ConfigurationSignalElement toElement() {
      return new ConfigurationSignalElement(
          signal,
          elementName,
          signalOrdinal,
          lineStart,
          lineEnd,
          executionScoped,
          executionOrdinal);
    }
  }
}
