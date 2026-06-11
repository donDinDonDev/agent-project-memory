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

public final class MavenMetadataAnalyzer {
  private static final String ANALYSIS_STATUS_ANALYZED = "analyzed";
  private static final String ANALYSIS_STATUS_NOT_DETECTED = "not_detected";
  private static final String BUILD_FILE_SOURCE_TYPE = "build_file";
  private static final String HIGH_CONFIDENCE = "high";
  private static final Pattern PROPERTY_REFERENCE = Pattern.compile("\\$\\{[^}]+}");
  private static final Comparator<MavenMetadataEvidence> EVIDENCE_ORDER = Comparator
      .comparing(MavenMetadataEvidence::sourcePath)
      .thenComparing(evidence -> evidence.lineStart() == null ? Integer.MAX_VALUE : evidence.lineStart())
      .thenComparing(evidence -> evidence.lineEnd() == null ? Integer.MAX_VALUE : evidence.lineEnd())
      .thenComparing(evidence -> nullSafe(evidence.className()))
      .thenComparing(evidence -> nullSafe(evidence.methodName()))
      .thenComparing(MavenMetadataEvidence::symbolName)
      .thenComparing(MavenMetadataEvidence::id);

  public MavenMetadataAnalysis analyze(Path repositoryRoot, List<MavenModuleItem> modules)
      throws IOException {
    Objects.requireNonNull(repositoryRoot, "repositoryRoot");
    Objects.requireNonNull(modules, "modules");

    Path normalizedRepositoryRoot = repositoryRoot.toAbsolutePath().normalize();
    Path canonicalRepositoryRoot = ScanPathContainment.canonicalRoot(normalizedRepositoryRoot);
    Map<String, MavenMetadataEvidence> evidence = new LinkedHashMap<>();
    List<MavenModuleMetadata> moduleMetadata = new ArrayList<>();

    for (MavenModuleItem module : modules) {
      if (module.pomPath() == null || module.pomPath().isBlank()) {
        moduleMetadata.add(notDetectedMetadata(module.moduleId()));
        continue;
      }

      Path pom = normalizedRepositoryRoot.resolve(module.pomPath()).normalize();
      if (!ScanPathContainment.isRegularFileUnderRootNoFollow(canonicalRepositoryRoot, pom)) {
        moduleMetadata.add(notDetectedMetadata(module.moduleId()));
        continue;
      }

      ParsedPomMetadata parsedPom = parsePomMetadata(
          pom,
          repositoryRelativePath(normalizedRepositoryRoot, pom));
      moduleMetadata.add(moduleMetadata(module.moduleId(), parsedPom));
      parsedPom.evidence().forEach(record -> evidence.putIfAbsent(record.id(), record));
    }

    return new MavenMetadataAnalysis(
        moduleMetadata,
        evidence.values().stream()
            .sorted(EVIDENCE_ORDER)
            .toList());
  }

  private ParsedPomMetadata parsePomMetadata(Path pom, String sourcePath) throws IOException {
    byte[] pomBytes = ScanPathContainment.readRegularFileBytesNoFollowStable(pom, Integer.MAX_VALUE);
    List<String> sourceLines = utf8Lines(pomBytes);
    MetadataElementHandler handler = new MetadataElementHandler();
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

    List<PreliminaryObservedElement> preliminary = handler.elements().stream()
        .map(element -> new PreliminaryObservedElement(
            element,
            evidenceBaseId(sourcePath, element),
            evidenceExcerpt(sourceLines, element)))
        .toList();
    Map<String, Integer> baseIdCounts = new LinkedHashMap<>();
    for (PreliminaryObservedElement element : preliminary) {
      baseIdCounts.merge(element.evidenceBaseId(), 1, Integer::sum);
    }

    List<ObservedMetadataElement> observed = new ArrayList<>();
    List<MavenMetadataEvidence> evidence = new ArrayList<>();
    for (PreliminaryObservedElement element : preliminary) {
      String evidenceId = element.evidenceBaseId();
      if (baseIdCounts.getOrDefault(evidenceId, 0) > 1) {
        evidenceId += ":decl:" + ordinalText(element.element().fieldOrdinal());
      }
      MetadataElement metadataElement = element.element();
      MavenMetadataEvidence evidenceRecord = new MavenMetadataEvidence(
          evidenceId,
          BUILD_FILE_SOURCE_TYPE,
          sourcePath,
          null,
          null,
          metadataElement.field().symbolName(),
          metadataElement.lineStart(),
          metadataElement.lineEnd(),
          element.excerpt(),
          HIGH_CONFIDENCE);
      observed.add(new ObservedMetadataElement(
          metadataElement.field(),
          metadataElement.rawText(),
          metadataElement.hasNestedElement(),
          evidenceRecord));
      evidence.add(evidenceRecord);
    }

    return new ParsedPomMetadata(handler.parentPresent(), observed, evidence);
  }

  private MavenModuleMetadata moduleMetadata(String moduleId, ParsedPomMetadata parsedPom) {
    Map<MetadataField, List<ObservedMetadataElement>> byField = new EnumMap<>(MetadataField.class);
    for (ObservedMetadataElement element : parsedPom.elements()) {
      byField.computeIfAbsent(element.field(), ignored -> new ArrayList<>()).add(element);
    }

    MavenMetadataParent parent = new MavenMetadataParent(
        parsedPom.parentPresent() ? ANALYSIS_STATUS_ANALYZED : ANALYSIS_STATUS_NOT_DETECTED,
        value(byField.getOrDefault(MetadataField.PARENT_GROUP_ID, List.of())),
        value(byField.getOrDefault(MetadataField.PARENT_ARTIFACT_ID, List.of())),
        value(byField.getOrDefault(MetadataField.PARENT_VERSION, List.of())),
        value(byField.getOrDefault(MetadataField.PARENT_RELATIVE_PATH, List.of())));

    return new MavenModuleMetadata(
        moduleId,
        ANALYSIS_STATUS_ANALYZED,
        value(byField.getOrDefault(MetadataField.PROJECT_GROUP_ID, List.of())),
        value(byField.getOrDefault(MetadataField.PROJECT_ARTIFACT_ID, List.of())),
        value(byField.getOrDefault(MetadataField.PROJECT_VERSION, List.of())),
        value(byField.getOrDefault(MetadataField.PROJECT_PACKAGING, List.of())),
        parent);
  }

  private MavenMetadataValue value(List<ObservedMetadataElement> elements) {
    if (elements.isEmpty()) {
      return MavenMetadataValue.notDeclared();
    }

    List<String> evidenceIds = elements.stream()
        .map(element -> element.evidence().id())
        .toList();
    if (elements.size() != 1) {
      return MavenMetadataValue.unsupported(evidenceIds);
    }

    ObservedMetadataElement element = elements.get(0);
    String text = element.rawText().trim();
    if (element.hasNestedElement() || text.isEmpty()) {
      return MavenMetadataValue.unsupported(evidenceIds);
    }

    return new MavenMetadataValue(text, valueKind(text), evidenceIds);
  }

  private MavenModuleMetadata notDetectedMetadata(String moduleId) {
    MavenMetadataParent parent = new MavenMetadataParent(
        ANALYSIS_STATUS_NOT_DETECTED,
        MavenMetadataValue.notDeclared(),
        MavenMetadataValue.notDeclared(),
        MavenMetadataValue.notDeclared(),
        MavenMetadataValue.notDeclared());
    return new MavenModuleMetadata(
        moduleId,
        ANALYSIS_STATUS_NOT_DETECTED,
        MavenMetadataValue.notDeclared(),
        MavenMetadataValue.notDeclared(),
        MavenMetadataValue.notDeclared(),
        MavenMetadataValue.notDeclared(),
        parent);
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
        "Could not parse Maven metadata in "
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

  private String evidenceBaseId(String sourcePath, MetadataElement element) {
    return "ev:" + sourcePath + ":" + lineRange(element.lineStart(), element.lineEnd())
        + ":build_file:" + element.field().symbolName();
  }

  private String evidenceExcerpt(List<String> sourceLines, MetadataElement element) {
    Integer lineStart = element.lineStart();
    Integer lineEnd = element.lineEnd();
    String excerpt;
    if (lineStart != null
        && lineEnd != null
        && lineStart >= 1
        && lineEnd >= lineStart
        && lineEnd <= sourceLines.size()) {
      excerpt = EvidenceExcerpts.sourceLines(sourceLines, lineStart, lineEnd);
    } else {
      excerpt = "<" + element.field().elementName() + ">"
          + element.rawText().trim()
          + "</"
          + element.field().elementName()
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

  private static String nullSafe(String value) {
    return value == null ? "" : value;
  }

  private enum MetadataField {
    PROJECT_GROUP_ID("groupId", "maven:project:groupId"),
    PROJECT_ARTIFACT_ID("artifactId", "maven:project:artifactId"),
    PROJECT_VERSION("version", "maven:project:version"),
    PROJECT_PACKAGING("packaging", "maven:project:packaging"),
    PARENT_GROUP_ID("groupId", "maven:parent:groupId"),
    PARENT_ARTIFACT_ID("artifactId", "maven:parent:artifactId"),
    PARENT_VERSION("version", "maven:parent:version"),
    PARENT_RELATIVE_PATH("relativePath", "maven:parent:relativePath");

    private final String elementName;
    private final String symbolName;

    MetadataField(String elementName, String symbolName) {
      this.elementName = elementName;
      this.symbolName = symbolName;
    }

    private String elementName() {
      return elementName;
    }

    private String symbolName() {
      return symbolName;
    }
  }

  private record MetadataElement(
      MetadataField field,
      int fieldOrdinal,
      String rawText,
      Integer lineStart,
      Integer lineEnd,
      boolean hasNestedElement) {
  }

  private record PreliminaryObservedElement(
      MetadataElement element,
      String evidenceBaseId,
      String excerpt) {
  }

  private record ObservedMetadataElement(
      MetadataField field,
      String rawText,
      boolean hasNestedElement,
      MavenMetadataEvidence evidence) {
  }

  private record ParsedPomMetadata(
      boolean parentPresent,
      List<ObservedMetadataElement> elements,
      List<MavenMetadataEvidence> evidence) {
    private ParsedPomMetadata {
      elements = List.copyOf(elements);
      evidence = List.copyOf(evidence);
    }
  }

  private static final class MetadataElementHandler extends DefaultHandler2 {
    private final List<String> elementStack = new ArrayList<>();
    private final List<MetadataElement> elements = new ArrayList<>();
    private final Map<MetadataField, Integer> fieldOrdinals = new EnumMap<>(MetadataField.class);
    private final StringBuilder elementText = new StringBuilder();
    private Locator locator;
    private boolean parentPresent;
    private boolean readingElement;
    private boolean nestedElement;
    private MetadataField currentField;
    private Integer elementLineStart;

    @Override
    public void setDocumentLocator(Locator locator) {
      this.locator = locator;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
      if (readingElement) {
        nestedElement = true;
      }

      elementStack.add(elementName(localName, qName));
      if (hasPath("project", "parent")) {
        parentPresent = true;
      }

      MetadataField field = metadataFieldForCurrentPath();
      if (field != null) {
        readingElement = true;
        nestedElement = false;
        currentField = field;
        elementLineStart = currentLineNumber();
        elementText.setLength(0);
      }
    }

    @Override
    public void characters(char[] characters, int start, int length) {
      if (readingElement) {
        elementText.append(characters, start, length);
      }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
      if (readingElement && metadataFieldForCurrentPath() == currentField) {
        int ordinal = fieldOrdinals.merge(currentField, 1, Integer::sum);
        elements.add(new MetadataElement(
            currentField,
            ordinal,
            elementText.toString(),
            elementLineStart,
            currentLineNumber(),
            nestedElement));
        readingElement = false;
        nestedElement = false;
        currentField = null;
        elementLineStart = null;
        elementText.setLength(0);
      }

      if (!elementStack.isEmpty()) {
        elementStack.remove(elementStack.size() - 1);
      }
    }

    private boolean parentPresent() {
      return parentPresent;
    }

    private List<MetadataElement> elements() {
      return List.copyOf(elements);
    }

    private MetadataField metadataFieldForCurrentPath() {
      if (hasPath("project", "groupId")) {
        return MetadataField.PROJECT_GROUP_ID;
      }
      if (hasPath("project", "artifactId")) {
        return MetadataField.PROJECT_ARTIFACT_ID;
      }
      if (hasPath("project", "version")) {
        return MetadataField.PROJECT_VERSION;
      }
      if (hasPath("project", "packaging")) {
        return MetadataField.PROJECT_PACKAGING;
      }
      if (hasPath("project", "parent", "groupId")) {
        return MetadataField.PARENT_GROUP_ID;
      }
      if (hasPath("project", "parent", "artifactId")) {
        return MetadataField.PARENT_ARTIFACT_ID;
      }
      if (hasPath("project", "parent", "version")) {
        return MetadataField.PARENT_VERSION;
      }
      if (hasPath("project", "parent", "relativePath")) {
        return MetadataField.PARENT_RELATIVE_PATH;
      }
      return null;
    }

    private boolean hasPath(String... path) {
      if (elementStack.size() != path.length) {
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
}
