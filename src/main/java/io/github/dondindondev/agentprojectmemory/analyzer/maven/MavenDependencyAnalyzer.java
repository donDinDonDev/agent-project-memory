package io.github.dondindondev.agentprojectmemory.analyzer.maven;

import io.github.dondindondev.agentprojectmemory.analyzer.ScanPathContainment;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

public final class MavenDependencyAnalyzer {
  private static final String ANALYSIS_STATUS_ANALYZED = "analyzed";
  private static final String ANALYSIS_STATUS_NOT_DETECTED = "not_detected";
  private static final String BUILD_FILE_SOURCE_TYPE = "build_file";
  private static final String HIGH_CONFIDENCE = "high";
  private static final String DIRECT_DEPENDENCY = "direct_dependency";
  private static final String DEPENDENCY_MANAGEMENT = "dependency_management";
  private static final int MAX_EXCERPT_LENGTH = 240;
  private static final Pattern PROPERTY_REFERENCE = Pattern.compile("\\$\\{[^}]+}");
  private static final Comparator<MavenDependencyDeclaration> DEPENDENCY_ORDER = Comparator
      .comparing(
          (MavenDependencyDeclaration dependency) -> nullLast(dependency.groupId().value()),
          Comparator.nullsLast(String::compareTo))
      .thenComparing(
          dependency -> nullLast(dependency.artifactId().value()),
          Comparator.nullsLast(String::compareTo))
      .thenComparing(
          dependency -> nullLast(dependency.type().value()),
          Comparator.nullsLast(String::compareTo))
      .thenComparing(
          dependency -> nullLast(dependency.classifier().value()),
          Comparator.nullsLast(String::compareTo))
      .thenComparing(
          dependency -> nullLast(dependency.scope().value()),
          Comparator.nullsLast(String::compareTo))
      .thenComparingInt(MavenDependencyDeclaration::declarationOrdinal)
      .thenComparing(MavenDependencyDeclaration::id);
  private static final Comparator<MavenDependencyEvidence> EVIDENCE_ORDER = Comparator
      .comparing(MavenDependencyEvidence::sourcePath)
      .thenComparing(evidence -> evidence.lineStart() == null ? Integer.MAX_VALUE : evidence.lineStart())
      .thenComparing(evidence -> evidence.lineEnd() == null ? Integer.MAX_VALUE : evidence.lineEnd())
      .thenComparing(evidence -> nullSafe(evidence.className()))
      .thenComparing(evidence -> nullSafe(evidence.methodName()))
      .thenComparing(MavenDependencyEvidence::symbolName)
      .thenComparing(MavenDependencyEvidence::id);

  public MavenDependencyAnalysis analyze(Path repositoryRoot, List<MavenModuleItem> modules)
      throws IOException {
    Objects.requireNonNull(repositoryRoot, "repositoryRoot");
    Objects.requireNonNull(modules, "modules");

    Path normalizedRepositoryRoot = repositoryRoot.toAbsolutePath().normalize();
    Path canonicalRepositoryRoot = ScanPathContainment.canonicalRoot(normalizedRepositoryRoot);
    Map<String, MavenDependencyEvidence> evidence = new LinkedHashMap<>();
    List<MavenModuleDependencies> moduleDependencies = new ArrayList<>();

    for (MavenModuleItem module : modules) {
      if (module.pomPath() == null || module.pomPath().isBlank()) {
        moduleDependencies.add(notDetectedDependencies(module.moduleId()));
        continue;
      }

      Path pom = normalizedRepositoryRoot.resolve(module.pomPath()).normalize();
      if (!ScanPathContainment.isRegularFileUnderRoot(canonicalRepositoryRoot, pom)) {
        moduleDependencies.add(notDetectedDependencies(module.moduleId()));
        continue;
      }

      ParsedPomDependencies parsedPom = parsePomDependencies(
          pom,
          repositoryRelativePath(normalizedRepositoryRoot, pom));
      moduleDependencies.add(moduleDependencies(module.moduleId(), parsedPom));
      parsedPom.evidence().forEach(record -> evidence.putIfAbsent(record.id(), record));
    }

    return new MavenDependencyAnalysis(
        moduleDependencies,
        evidence.values().stream()
            .sorted(EVIDENCE_ORDER)
            .toList());
  }

  private ParsedPomDependencies parsePomDependencies(Path pom, String sourcePath) throws IOException {
    List<String> sourceLines = Files.readAllLines(pom, StandardCharsets.UTF_8);
    DependencyElementHandler handler = new DependencyElementHandler();
    SAXParser parser;
    try {
      parser = secureSaxParserFactory().newSAXParser();
    } catch (ParserConfigurationException | SAXException exception) {
      throw new IOException("Unable to configure secure XML parser for " + pom, exception);
    }

    try (InputStream input = Files.newInputStream(pom)) {
      parser.parse(input, handler);
    } catch (SAXException exception) {
      throw malformedPomException(sourcePath, exception);
    }

    List<PreliminaryDependencyEvidence> preliminary = new ArrayList<>();
    for (DependencyDeclarationElement declaration : handler.declarations()) {
      preliminary.add(new PreliminaryDependencyEvidence(
          declaration,
          null,
          evidenceBaseId(sourcePath, declaration),
          evidenceExcerpt(sourceLines, declaration)));
      for (DependencyValueElement value : declaration.values()) {
        preliminary.add(new PreliminaryDependencyEvidence(
            declaration,
            value,
            evidenceBaseId(sourcePath, declaration, value),
            evidenceExcerpt(sourceLines, value)));
      }
    }

    Map<String, Integer> baseIdCounts = new LinkedHashMap<>();
    for (PreliminaryDependencyEvidence evidence : preliminary) {
      baseIdCounts.merge(evidence.evidenceBaseId(), 1, Integer::sum);
    }

    Map<DependencyDeclarationElement, List<ObservedDependencyValue>> valuesByDeclaration =
        new LinkedHashMap<>();
    List<ObservedDependencyDeclaration> declarations = new ArrayList<>();
    List<MavenDependencyEvidence> evidence = new ArrayList<>();
    for (PreliminaryDependencyEvidence preliminaryEvidence : preliminary) {
      String evidenceId = preliminaryEvidence.evidenceBaseId();
      if (baseIdCounts.getOrDefault(evidenceId, 0) > 1) {
        int collisionOrdinal = preliminaryEvidence.value() == null
            ? preliminaryEvidence.declaration().ordinal()
            : preliminaryEvidence.value().fieldOrdinal();
        evidenceId += ":decl:" + ordinalText(preliminaryEvidence.declaration().ordinal())
            + ":value:"
            + ordinalText(collisionOrdinal);
      }

      String symbolName = preliminaryEvidence.value() == null
          ? preliminaryEvidence.declaration().symbolName()
          : preliminaryEvidence.value().symbolName(
              preliminaryEvidence.declaration().kind(),
              preliminaryEvidence.declaration().ordinal());
      Integer lineStart = preliminaryEvidence.value() == null
          ? preliminaryEvidence.declaration().lineStart()
          : preliminaryEvidence.value().lineStart();
      Integer lineEnd = preliminaryEvidence.value() == null
          ? preliminaryEvidence.declaration().lineEnd()
          : preliminaryEvidence.value().lineEnd();
      MavenDependencyEvidence evidenceRecord = new MavenDependencyEvidence(
          evidenceId,
          BUILD_FILE_SOURCE_TYPE,
          sourcePath,
          null,
          null,
          symbolName,
          lineStart,
          lineEnd,
          preliminaryEvidence.excerpt(),
          HIGH_CONFIDENCE);
      evidence.add(evidenceRecord);

      if (preliminaryEvidence.value() == null) {
        declarations.add(new ObservedDependencyDeclaration(
            preliminaryEvidence.declaration().kind(),
            preliminaryEvidence.declaration().ordinal(),
            evidenceRecord));
      } else {
        DependencyValueElement value = preliminaryEvidence.value();
        valuesByDeclaration
            .computeIfAbsent(preliminaryEvidence.declaration(), ignored -> new ArrayList<>())
            .add(new ObservedDependencyValue(
                value.field(),
                value.rawText(),
                value.hasNestedElement(),
                evidenceRecord));
      }
    }

    List<ObservedPomDependency> observed = new ArrayList<>();
    for (ObservedDependencyDeclaration declaration : declarations) {
      DependencyDeclarationElement declarationElement = handler.declarations().stream()
          .filter(element -> element.kind() == declaration.kind()
              && element.ordinal() == declaration.ordinal())
          .findFirst()
          .orElseThrow();
      observed.add(new ObservedPomDependency(
          declaration,
          valuesByDeclaration.getOrDefault(declarationElement, List.of())));
    }

    return new ParsedPomDependencies(observed, evidence);
  }

  private MavenModuleDependencies moduleDependencies(
      String moduleId,
      ParsedPomDependencies parsedPom) {
    List<MavenDependencyDeclaration> directDependencies = new ArrayList<>();
    List<MavenDependencyDeclaration> managedDependencies = new ArrayList<>();
    for (ObservedPomDependency dependency : parsedPom.dependencies()) {
      MavenDependencyDeclaration declaration = dependencyDeclaration(moduleId, dependency);
      if (dependency.declaration().kind() == DependencyDeclarationKind.DIRECT) {
        directDependencies.add(declaration);
      } else {
        managedDependencies.add(declaration);
      }
    }

    return new MavenModuleDependencies(
        moduleId,
        ANALYSIS_STATUS_ANALYZED,
        directDependencies.stream().sorted(DEPENDENCY_ORDER).toList(),
        managedDependencies.stream().sorted(DEPENDENCY_ORDER).toList());
  }

  private MavenDependencyDeclaration dependencyDeclaration(
      String moduleId,
      ObservedPomDependency dependency) {
    Map<DependencyField, List<ObservedDependencyValue>> byField =
        new EnumMap<>(DependencyField.class);
    for (ObservedDependencyValue value : dependency.values()) {
      byField.computeIfAbsent(value.field(), ignored -> new ArrayList<>()).add(value);
    }

    MavenMetadataValue groupId = value(byField.getOrDefault(DependencyField.GROUP_ID, List.of()));
    MavenMetadataValue artifactId = value(byField.getOrDefault(DependencyField.ARTIFACT_ID, List.of()));
    MavenMetadataValue version = value(byField.getOrDefault(DependencyField.VERSION, List.of()));
    MavenMetadataValue scope = value(byField.getOrDefault(DependencyField.SCOPE, List.of()));
    MavenMetadataValue optional = value(byField.getOrDefault(DependencyField.OPTIONAL, List.of()));
    MavenMetadataValue type = value(byField.getOrDefault(DependencyField.TYPE, List.of()));
    MavenMetadataValue classifier = value(byField.getOrDefault(DependencyField.CLASSIFIER, List.of()));
    String declarationKind = dependency.declaration().kind() == DependencyDeclarationKind.DIRECT
        ? DIRECT_DEPENDENCY
        : DEPENDENCY_MANAGEMENT;

    return new MavenDependencyDeclaration(
        dependencyId(
            moduleId,
            dependency.declaration().kind(),
            dependency.declaration().ordinal(),
            groupId,
            artifactId),
        declarationKind,
        dependency.declaration().ordinal(),
        groupId,
        artifactId,
        version,
        scope,
        optional,
        type,
        classifier,
        List.of(dependency.declaration().evidence().id()));
  }

  private MavenMetadataValue value(List<ObservedDependencyValue> values) {
    if (values.isEmpty()) {
      return MavenMetadataValue.notDeclared();
    }

    List<String> evidenceIds = values.stream()
        .map(value -> value.evidence().id())
        .toList();
    if (values.size() != 1) {
      return MavenMetadataValue.unsupported(evidenceIds);
    }

    ObservedDependencyValue value = values.get(0);
    String text = value.rawText().trim();
    if (value.hasNestedElement() || text.isEmpty()) {
      return MavenMetadataValue.unsupported(evidenceIds);
    }

    return new MavenMetadataValue(text, valueKind(text), evidenceIds);
  }

  private MavenModuleDependencies notDetectedDependencies(String moduleId) {
    return new MavenModuleDependencies(
        moduleId,
        ANALYSIS_STATUS_NOT_DETECTED,
        List.of(),
        List.of());
  }

  private String dependencyId(
      String moduleId,
      DependencyDeclarationKind kind,
      int ordinal,
      MavenMetadataValue groupId,
      MavenMetadataValue artifactId) {
    String kindSegment = kind == DependencyDeclarationKind.DIRECT ? "direct" : DEPENDENCY_MANAGEMENT;
    return "maven_dependency:" + moduleId + ":" + kindSegment + ":"
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
        "Could not parse Maven dependencies in "
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

  private String evidenceBaseId(String sourcePath, DependencyDeclarationElement declaration) {
    return "ev:" + sourcePath + ":" + lineRange(declaration.lineStart(), declaration.lineEnd())
        + ":build_file:" + declaration.symbolName();
  }

  private String evidenceBaseId(
      String sourcePath,
      DependencyDeclarationElement declaration,
      DependencyValueElement value) {
    return "ev:" + sourcePath + ":" + lineRange(value.lineStart(), value.lineEnd())
        + ":build_file:" + value.symbolName(declaration.kind(), declaration.ordinal());
  }

  private String evidenceExcerpt(List<String> sourceLines, DependencyDeclarationElement declaration) {
    Integer lineStart = declaration.lineStart();
    Integer lineEnd = declaration.lineEnd();
    String excerpt;
    if (lineStart != null
        && lineEnd != null
        && lineStart >= 1
        && lineEnd >= lineStart
        && lineEnd <= sourceLines.size()) {
      excerpt = String.join("\n", sourceLines.subList(lineStart - 1, lineEnd)).trim();
    } else {
      excerpt = "<dependency>";
    }
    return boundedExcerpt(excerpt);
  }

  private String evidenceExcerpt(List<String> sourceLines, DependencyValueElement value) {
    Integer lineStart = value.lineStart();
    Integer lineEnd = value.lineEnd();
    String excerpt;
    if (lineStart != null
        && lineEnd != null
        && lineStart >= 1
        && lineEnd >= lineStart
        && lineEnd <= sourceLines.size()) {
      excerpt = String.join("\n", sourceLines.subList(lineStart - 1, lineEnd)).trim();
    } else {
      excerpt = "<" + value.field().elementName() + ">"
          + value.rawText().trim()
          + "</"
          + value.field().elementName()
          + ">";
    }
    return boundedExcerpt(excerpt);
  }

  private String boundedExcerpt(String excerpt) {
    if (excerpt.length() <= MAX_EXCERPT_LENGTH) {
      return excerpt;
    }
    return excerpt.substring(0, MAX_EXCERPT_LENGTH) + "...";
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

  private static String ordinalText(int ordinal) {
    return String.format("%06d", ordinal);
  }

  private static String nullLast(String value) {
    return value;
  }

  private static String nullSafe(String value) {
    return value == null ? "" : value;
  }

  private enum DependencyDeclarationKind {
    DIRECT("maven:dependency"),
    MANAGEMENT("maven:dependency_management");

    private final String symbolPrefix;

    DependencyDeclarationKind(String symbolPrefix) {
      this.symbolPrefix = symbolPrefix;
    }

    private String symbolPrefix() {
      return symbolPrefix;
    }
  }

  private enum DependencyField {
    GROUP_ID("groupId"),
    ARTIFACT_ID("artifactId"),
    VERSION("version"),
    SCOPE("scope"),
    OPTIONAL("optional"),
    TYPE("type"),
    CLASSIFIER("classifier");

    private final String elementName;

    DependencyField(String elementName) {
      this.elementName = elementName;
    }

    private String elementName() {
      return elementName;
    }
  }

  private record DependencyValueElement(
      DependencyField field,
      int fieldOrdinal,
      String rawText,
      Integer lineStart,
      Integer lineEnd,
      boolean hasNestedElement) {
    private String symbolName(DependencyDeclarationKind kind, int ordinal) {
      return kind.symbolPrefix() + ":" + ordinalText(ordinal) + ":" + field.elementName();
    }
  }

  private record DependencyDeclarationElement(
      DependencyDeclarationKind kind,
      int ordinal,
      Integer lineStart,
      Integer lineEnd,
      List<DependencyValueElement> values) {
    private DependencyDeclarationElement {
      values = List.copyOf(values);
    }

    private String symbolName() {
      return kind.symbolPrefix() + ":" + ordinalText(ordinal);
    }
  }

  private record PreliminaryDependencyEvidence(
      DependencyDeclarationElement declaration,
      DependencyValueElement value,
      String evidenceBaseId,
      String excerpt) {
  }

  private record ObservedDependencyDeclaration(
      DependencyDeclarationKind kind,
      int ordinal,
      MavenDependencyEvidence evidence) {
  }

  private record ObservedDependencyValue(
      DependencyField field,
      String rawText,
      boolean hasNestedElement,
      MavenDependencyEvidence evidence) {
  }

  private record ObservedPomDependency(
      ObservedDependencyDeclaration declaration,
      List<ObservedDependencyValue> values) {
    private ObservedPomDependency {
      values = List.copyOf(values);
    }
  }

  private record ParsedPomDependencies(
      List<ObservedPomDependency> dependencies,
      List<MavenDependencyEvidence> evidence) {
    private ParsedPomDependencies {
      dependencies = List.copyOf(dependencies);
      evidence = List.copyOf(evidence);
    }
  }

  private static final class DependencyElementHandler extends DefaultHandler2 {
    private final List<String> elementStack = new ArrayList<>();
    private final List<MutableDependencyDeclaration> declarations = new ArrayList<>();
    private final Map<DependencyDeclarationKind, Integer> declarationOrdinals =
        new EnumMap<>(DependencyDeclarationKind.class);
    private final StringBuilder elementText = new StringBuilder();
    private Locator locator;
    private MutableDependencyDeclaration currentDeclaration;
    private boolean readingValue;
    private boolean nestedElement;
    private DependencyField currentField;
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
      DependencyDeclarationKind declarationKind = dependencyDeclarationKindForCurrentPath();
      if (declarationKind != null) {
        int ordinal = declarationOrdinals.merge(declarationKind, 1, Integer::sum);
        currentDeclaration = new MutableDependencyDeclaration(
            declarationKind,
            ordinal,
            currentLineNumber());
        return;
      }

      DependencyField field = dependencyFieldForCurrentPath();
      if (currentDeclaration != null && field != null) {
        readingValue = true;
        nestedElement = false;
        currentField = field;
        valueLineStart = currentLineNumber();
        elementText.setLength(0);
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
      if (readingValue && dependencyFieldForCurrentPath() == currentField) {
        currentDeclaration.addValue(new DependencyValueElement(
            currentField,
            currentDeclaration.nextFieldOrdinal(currentField),
            elementText.toString(),
            valueLineStart,
            currentLineNumber(),
            nestedElement));
        readingValue = false;
        nestedElement = false;
        currentField = null;
        valueLineStart = null;
        elementText.setLength(0);
      }

      if (currentDeclaration != null && isCurrentDependencyPath(currentDeclaration.kind())) {
        currentDeclaration.setLineEnd(currentLineNumber());
        declarations.add(currentDeclaration);
        currentDeclaration = null;
      }

      if (!elementStack.isEmpty()) {
        elementStack.remove(elementStack.size() - 1);
      }
    }

    private List<DependencyDeclarationElement> declarations() {
      return declarations.stream()
          .map(MutableDependencyDeclaration::toElement)
          .toList();
    }

    private DependencyDeclarationKind dependencyDeclarationKindForCurrentPath() {
      if (hasPath("project", "dependencies", "dependency")) {
        return DependencyDeclarationKind.DIRECT;
      }
      if (hasPath("project", "dependencyManagement", "dependencies", "dependency")) {
        return DependencyDeclarationKind.MANAGEMENT;
      }
      return null;
    }

    private boolean isCurrentDependencyPath(DependencyDeclarationKind kind) {
      if (kind == DependencyDeclarationKind.DIRECT) {
        return hasPath("project", "dependencies", "dependency");
      }
      return hasPath("project", "dependencyManagement", "dependencies", "dependency");
    }

    private DependencyField dependencyFieldForCurrentPath() {
      if (currentDeclaration == null || elementStack.isEmpty()) {
        return null;
      }

      int expectedSize = currentDeclaration.kind() == DependencyDeclarationKind.DIRECT ? 4 : 5;
      if (elementStack.size() != expectedSize) {
        return null;
      }

      if (currentDeclaration.kind() == DependencyDeclarationKind.DIRECT
          && !hasPrefix("project", "dependencies", "dependency")) {
        return null;
      }
      if (currentDeclaration.kind() == DependencyDeclarationKind.MANAGEMENT
          && !hasPrefix("project", "dependencyManagement", "dependencies", "dependency")) {
        return null;
      }

      String elementName = elementStack.get(elementStack.size() - 1);
      for (DependencyField field : DependencyField.values()) {
        if (field.elementName().equals(elementName)) {
          return field;
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

    private String elementName(String localName, String qName) {
      String name = localName == null || localName.isBlank() ? qName : localName;
      int prefixEnd = name.indexOf(':');
      if (prefixEnd >= 0) {
        return name.substring(prefixEnd + 1);
      }
      return name;
    }
  }

  private static final class MutableDependencyDeclaration {
    private final DependencyDeclarationKind kind;
    private final int ordinal;
    private final Integer lineStart;
    private final List<DependencyValueElement> values = new ArrayList<>();
    private final Map<DependencyField, Integer> fieldOrdinals = new EnumMap<>(DependencyField.class);
    private Integer lineEnd;

    private MutableDependencyDeclaration(
        DependencyDeclarationKind kind,
        int ordinal,
        Integer lineStart) {
      this.kind = kind;
      this.ordinal = ordinal;
      this.lineStart = lineStart;
    }

    private DependencyDeclarationKind kind() {
      return kind;
    }

    private void addValue(DependencyValueElement value) {
      values.add(value);
    }

    private int nextFieldOrdinal(DependencyField field) {
      return fieldOrdinals.merge(field, 1, Integer::sum);
    }

    private void setLineEnd(Integer lineEnd) {
      this.lineEnd = lineEnd;
    }

    private DependencyDeclarationElement toElement() {
      return new DependencyDeclarationElement(kind, ordinal, lineStart, lineEnd, values);
    }
  }
}
