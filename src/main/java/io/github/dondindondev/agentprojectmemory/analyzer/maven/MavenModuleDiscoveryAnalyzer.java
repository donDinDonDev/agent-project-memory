package io.github.dondindondev.agentprojectmemory.analyzer.maven;

import io.github.dondindondev.agentprojectmemory.analyzer.ScanPathContainment;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

public final class MavenModuleDiscoveryAnalyzer {
  private static final String ANALYSIS_STATUS_ANALYZED = "analyzed";
  private static final String ANALYSIS_STATUS_NOT_DETECTED = "not_detected";
  private static final String ROOT_BUILD_FILE = "pom.xml";
  private static final String BUILD_FILE_SOURCE_TYPE = "build_file";
  private static final String HIGH_CONFIDENCE = "high";
  private static final String MAIN_SOURCE_ROOT = "src/main/java";
  private static final String TEST_SOURCE_ROOT = "src/test/java";
  private static final String CATEGORY_MAVEN_MODULE = "maven_module";
  private static final String SIGNAL_INVALID_MODULE_PATH = "invalid_module_path";
  private static final String SIGNAL_MISSING_CHILD_POM = "missing_child_pom";
  private static final String SIGNAL_DUPLICATE_MODULE_PATH = "duplicate_module_path";
  private static final String SIGNAL_NESTED_MODULE_DECLARATION = "nested_module_declaration";
  private static final String SIGNAL_UNSUPPORTED_MODULE = "unsupported_module";
  private static final Comparator<MavenModuleItem> MODULE_ORDER = Comparator
      .comparingInt(MavenModuleDiscoveryAnalyzer::rootFirst)
      .thenComparing(MavenModuleItem::modulePath);
  private static final Comparator<MavenModuleDiscoveryEvidence> EVIDENCE_ORDER = Comparator
      .comparing(MavenModuleDiscoveryEvidence::sourcePath)
      .thenComparing(evidence -> evidence.lineStart() == null ? Integer.MAX_VALUE : evidence.lineStart())
      .thenComparing(evidence -> evidence.lineEnd() == null ? Integer.MAX_VALUE : evidence.lineEnd())
      .thenComparing(evidence -> nullSafe(evidence.className()))
      .thenComparing(evidence -> nullSafe(evidence.methodName()))
      .thenComparing(MavenModuleDiscoveryEvidence::symbolName)
      .thenComparing(MavenModuleDiscoveryEvidence::id);

  public MavenModuleDiscoveryAnalysis analyze(Path repositoryRoot) throws IOException {
    Objects.requireNonNull(repositoryRoot, "repositoryRoot");

    Path normalizedRepositoryRoot = repositoryRoot.toAbsolutePath().normalize();
    Path canonicalRepositoryRoot = ScanPathContainment.canonicalRoot(normalizedRepositoryRoot);
    Path rootPom = normalizedRepositoryRoot.resolve(ROOT_BUILD_FILE);
    if (!ScanPathContainment.isRegularFileUnderRoot(canonicalRepositoryRoot, rootPom)) {
      return new MavenModuleDiscoveryAnalysis(
          ANALYSIS_STATUS_NOT_DETECTED,
          List.of(),
          List.of(),
          List.of());
    }

    Map<String, MavenModuleDiscoveryEvidence> evidence = new LinkedHashMap<>();
    List<MavenModuleItem> moduleItems = new ArrayList<>();
    List<MavenModuleWarning> warnings = new ArrayList<>();

    MavenModuleDiscoveryEvidence rootPomEvidence = pomEvidence(
        normalizedRepositoryRoot,
        rootPom,
        ROOT_BUILD_FILE);
    addEvidence(evidence, rootPomEvidence);

    List<String> rootSourceRoots = detectedRoots(
        normalizedRepositoryRoot,
        canonicalRepositoryRoot,
        ".");
    List<String> rootTestRoots = detectedTestRoots(
        normalizedRepositoryRoot,
        canonicalRepositoryRoot,
        ".");
    List<ObservedModuleDeclaration> declarations = observedModuleDeclarations(
        normalizedRepositoryRoot,
        normalizedRepositoryRoot,
        rootPom,
        ROOT_BUILD_FILE);
    declarations.forEach(declaration -> addEvidence(evidence, declaration.evidence()));

    Map<String, ObservedModuleDeclaration> uniqueDeclarations = new LinkedHashMap<>();
    for (ObservedModuleDeclaration declaration : declarations) {
      if (declaration.normalizedPath().isEmpty()) {
        warnings.add(invalidModulePathWarning(declaration));
        continue;
      }

      String modulePath = declaration.normalizedPath().orElseThrow();
      if (uniqueDeclarations.containsKey(modulePath)) {
        warnings.add(duplicateModulePathWarning(modulePath, declaration));
        continue;
      }
      uniqueDeclarations.put(modulePath, declaration);
    }

    boolean rootHasSupportedRoots = !rootSourceRoots.isEmpty() || !rootTestRoots.isEmpty();
    boolean singleModule = declarations.isEmpty();
    if (singleModule || rootHasSupportedRoots) {
      moduleItems.add(new MavenModuleItem(
          "module:.",
          ".",
          ROOT_BUILD_FILE,
          rootSourceRoots,
          rootTestRoots,
          rootHasSupportedRoots ? "supported" : "unsupported",
          "scan_root",
          ".",
          List.of(),
          List.of(rootPomEvidence.id())));
    }

    for (ObservedModuleDeclaration declaration : uniqueDeclarations.values()) {
      String modulePath = declaration.normalizedPath().orElseThrow();
      Path childModuleDirectory = normalizedRepositoryRoot.resolve(modulePath).normalize();
      Path childPom = childModuleDirectory.resolve(ROOT_BUILD_FILE).normalize();
      String childPomPath = repositoryRelativePath(normalizedRepositoryRoot, childPom);
      List<String> declarationEvidenceIds = List.of(declaration.evidence().id());

      if (existsButEscapesRoot(canonicalRepositoryRoot, childModuleDirectory)
          || existsButEscapesRoot(canonicalRepositoryRoot, childPom)) {
        warnings.add(invalidModulePathWarning(declaration));
        continue;
      }

      if (!ScanPathContainment.isRegularFileUnderRoot(canonicalRepositoryRoot, childPom)) {
        moduleItems.add(new MavenModuleItem(
            moduleId(modulePath),
            modulePath,
            null,
            List.of(),
            List.of(),
            "missing_child_pom",
            "root_modules_entry",
            modulePath,
            declarationEvidenceIds,
            List.of()));
        warnings.add(missingChildPomWarning(modulePath, declaration));
        continue;
      }

      MavenModuleDiscoveryEvidence childPomEvidence = pomEvidence(
          normalizedRepositoryRoot,
          childPom,
          childPomPath);
      addEvidence(evidence, childPomEvidence);

      List<String> sourceRoots = detectedRoots(
          normalizedRepositoryRoot,
          canonicalRepositoryRoot,
          modulePath);
      List<String> testRoots = detectedTestRoots(
          normalizedRepositoryRoot,
          canonicalRepositoryRoot,
          modulePath);
      boolean supported = !sourceRoots.isEmpty() || !testRoots.isEmpty();

      moduleItems.add(new MavenModuleItem(
          moduleId(modulePath),
          modulePath,
          childPomPath,
          sourceRoots,
          testRoots,
          supported ? "supported" : "unsupported",
          "root_modules_entry",
          modulePath,
          declarationEvidenceIds,
          List.of(childPomEvidence.id())));

      if (supported) {
        List<ObservedModuleDeclaration> nestedDeclarations = observedModuleDeclarations(
            normalizedRepositoryRoot,
            childPom.getParent(),
            childPom,
            childPomPath);
        if (!nestedDeclarations.isEmpty()) {
          ObservedModuleDeclaration nestedDeclaration = nestedDeclarations.get(0);
          addEvidence(evidence, nestedDeclaration.evidence());
          warnings.add(nestedModuleDeclarationWarning(
              modulePath,
              childPomPath,
              childPomEvidence,
              nestedDeclaration));
        }
      }

      if (!supported) {
        warnings.add(unsupportedModuleWarning(modulePath, childPomPath, declaration, childPomEvidence));
      }
    }

    List<MavenModuleItem> sortedItems = moduleItems.stream()
        .sorted(MODULE_ORDER)
        .toList();
    Map<String, Integer> moduleOrder = moduleOrder(sortedItems);

    return new MavenModuleDiscoveryAnalysis(
        ANALYSIS_STATUS_ANALYZED,
        sortedItems,
        warnings.stream()
            .sorted(warningOrder(moduleOrder))
            .toList(),
        evidence.values().stream()
            .sorted(EVIDENCE_ORDER)
            .toList());
  }

  private List<String> detectedRoots(
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      String modulePath) {
    return detectedModuleRoots(
        repositoryRoot,
        canonicalRepositoryRoot,
        modulePath,
        MAIN_SOURCE_ROOT);
  }

  private List<String> detectedTestRoots(
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      String modulePath) {
    return detectedModuleRoots(
        repositoryRoot,
        canonicalRepositoryRoot,
        modulePath,
        TEST_SOURCE_ROOT);
  }

  private List<String> detectedModuleRoots(
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      String modulePath,
      String rootName) {
    String root = ".".equals(modulePath) ? rootName : modulePath + "/" + rootName;
    if (!ScanPathContainment.isDirectoryUnderRoot(
        canonicalRepositoryRoot,
        repositoryRoot.resolve(root))) {
      return List.of();
    }
    return List.of(root);
  }

  private boolean existsButEscapesRoot(Path canonicalRepositoryRoot, Path path) {
    return Files.exists(path)
        && ScanPathContainment.realPathUnderRoot(canonicalRepositoryRoot, path).isEmpty();
  }

  private MavenModuleDiscoveryEvidence pomEvidence(
      Path repositoryRoot,
      Path pom,
      String sourcePath) throws IOException {
    List<String> lines = Files.readAllLines(pom, StandardCharsets.UTF_8);
    Integer line = lines.isEmpty() ? null : 1;
    String lineRange = line == null ? "unknown" : line + "-" + line;
    String excerpt = lines.isEmpty() ? "" : lines.get(0).trim();
    return new MavenModuleDiscoveryEvidence(
        "ev:" + sourcePath + ":" + lineRange + ":build_file:" + ROOT_BUILD_FILE,
        BUILD_FILE_SOURCE_TYPE,
        repositoryRelativePath(repositoryRoot, pom),
        null,
        null,
        ROOT_BUILD_FILE,
        line,
        line,
        excerpt,
        HIGH_CONFIDENCE);
  }

  private List<ObservedModuleDeclaration> observedModuleDeclarations(
      Path repositoryRoot,
      Path declarationBaseDirectory,
      Path pom,
      String sourcePath) throws IOException {
    List<String> sourceLines = Files.readAllLines(pom, StandardCharsets.UTF_8);
    List<ModuleDeclaration> declarations = moduleDeclarations(pom);
    List<PreliminaryObservedModuleDeclaration> preliminary = declarations.stream()
        .map(declaration -> {
          NormalizedModulePath normalizedPath = normalizeModulePath(
              repositoryRoot,
              declarationBaseDirectory,
              declaration.rawText());
          return new PreliminaryObservedModuleDeclaration(
              declaration,
              normalizedPath,
              declarationEvidenceBaseId(sourcePath, declaration, normalizedPath));
        })
        .toList();
    Map<String, Integer> baseIdCounts = new LinkedHashMap<>();
    for (PreliminaryObservedModuleDeclaration declaration : preliminary) {
      baseIdCounts.merge(declaration.evidenceBaseId(), 1, Integer::sum);
    }

    List<ObservedModuleDeclaration> observedDeclarations = new ArrayList<>();
    for (PreliminaryObservedModuleDeclaration declaration : preliminary) {
      String evidenceId = declaration.evidenceBaseId();
      if (baseIdCounts.getOrDefault(evidenceId, 0) > 1) {
        evidenceId += ":decl:" + declaration.declaration().ordinalText();
      }
      String symbolName = declaration.normalizedPath().normalizedPath()
          .map(path -> "module:" + path)
          .orElse("module:<invalid>:decl:" + declaration.declaration().ordinalText());
      ModuleDeclaration moduleDeclaration = declaration.declaration();
      MavenModuleDiscoveryEvidence evidence = new MavenModuleDiscoveryEvidence(
          evidenceId,
          BUILD_FILE_SOURCE_TYPE,
          sourcePath,
          null,
          null,
          symbolName,
          moduleDeclaration.lineStart(),
          moduleDeclaration.lineEnd(),
          declarationExcerpt(sourceLines, moduleDeclaration),
          HIGH_CONFIDENCE);
      observedDeclarations.add(new ObservedModuleDeclaration(
          moduleDeclaration.ordinal(),
          moduleDeclaration.rawText(),
          declaration.normalizedPath().normalizedPath(),
          evidence));
    }
    return observedDeclarations;
  }

  private String declarationEvidenceBaseId(
      String sourcePath,
      ModuleDeclaration declaration,
      NormalizedModulePath normalizedPath) {
    String lineRange = lineRange(declaration.lineStart(), declaration.lineEnd());
    String symbolName = normalizedPath.normalizedPath()
        .map(path -> "module:" + path)
        .orElse("module:<invalid>:decl:" + declaration.ordinalText());
    return "ev:" + sourcePath + ":" + lineRange + ":build_file:" + symbolName;
  }

  private String declarationExcerpt(List<String> sourceLines, ModuleDeclaration declaration) {
    Integer lineStart = declaration.lineStart();
    Integer lineEnd = declaration.lineEnd();
    if (lineStart != null
        && lineEnd != null
        && lineStart >= 1
        && lineEnd >= lineStart
        && lineEnd <= sourceLines.size()) {
      return String.join("\n", sourceLines.subList(lineStart - 1, lineEnd)).trim();
    }
    return "<module>" + declaration.rawText().trim() + "</module>";
  }

  private List<ModuleDeclaration> moduleDeclarations(Path pom) throws IOException {
    ModuleDeclarationHandler handler = new ModuleDeclarationHandler();
    try (InputStream input = Files.newInputStream(pom)) {
      SAXParserFactory factory = secureSaxParserFactory();
      factory.newSAXParser().parse(input, handler);
    } catch (SAXException exception) {
      return List.of();
    } catch (ParserConfigurationException exception) {
      throw new IOException("Unable to configure secure XML parser for " + pom, exception);
    }
    return handler.declarations();
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

  private NormalizedModulePath normalizeModulePath(
      Path repositoryRoot,
      Path declarationBaseDirectory,
      String rawPath) {
    String trimmedPath = rawPath.trim();
    if (trimmedPath.isEmpty()
        || trimmedPath.contains("\\")
        || trimmedPath.startsWith("/")
        || isWindowsAbsolutePath(trimmedPath)) {
      return NormalizedModulePath.invalid();
    }

    String pathWithoutTrailingSlash = stripTrailingSlash(trimmedPath);
    if (pathWithoutTrailingSlash.isEmpty()) {
      return NormalizedModulePath.invalid();
    }

    String[] segments = pathWithoutTrailingSlash.split("/", -1);
    for (String segment : segments) {
      if (segment.isBlank() || ".".equals(segment) || "..".equals(segment)) {
        return NormalizedModulePath.invalid();
      }
    }

    Path resolvedPath = declarationBaseDirectory.resolve(pathWithoutTrailingSlash).normalize();
    if (!resolvedPath.startsWith(repositoryRoot)) {
      return NormalizedModulePath.invalid();
    }

    Path relativePath = repositoryRoot.relativize(resolvedPath);
    if (relativePath.getNameCount() == 0) {
      return NormalizedModulePath.invalid();
    }
    return new NormalizedModulePath(relativePath.toString().replace(
        relativePath.getFileSystem().getSeparator(),
        "/"));
  }

  private boolean isWindowsAbsolutePath(String path) {
    return path.length() >= 3
        && Character.isLetter(path.charAt(0))
        && path.charAt(1) == ':'
        && (path.charAt(2) == '/' || path.charAt(2) == '\\');
  }

  private String stripTrailingSlash(String path) {
    String stripped = path;
    while (stripped.endsWith("/")) {
      stripped = stripped.substring(0, stripped.length() - 1);
    }
    return stripped;
  }

  private MavenModuleWarning invalidModulePathWarning(ObservedModuleDeclaration declaration) {
    return new MavenModuleWarning(
        "warning:maven_module:invalid_module_path:decl:" + ordinalText(declaration.ordinal()),
        CATEGORY_MAVEN_MODULE,
        SIGNAL_INVALID_MODULE_PATH,
        null,
        "Maven module declaration in root pom.xml is empty, absolute, escaping, or otherwise unsupported; v0.2 does not analyze this declaration.",
        ROOT_BUILD_FILE,
        List.of(declaration.evidence().id()));
  }

  private MavenModuleWarning duplicateModulePathWarning(
      String modulePath,
      ObservedModuleDeclaration declaration) {
    return new MavenModuleWarning(
        "warning:maven_module:duplicate_module_path:" + modulePath
            + ":decl:" + ordinalText(declaration.ordinal()),
        CATEGORY_MAVEN_MODULE,
        SIGNAL_DUPLICATE_MODULE_PATH,
        moduleId(modulePath),
        "Duplicate Maven module declaration in root pom.xml ignored; v0.2 analyzes each normalized module path once.",
        ROOT_BUILD_FILE,
        List.of(declaration.evidence().id()));
  }

  private MavenModuleWarning missingChildPomWarning(
      String modulePath,
      ObservedModuleDeclaration declaration) {
    return new MavenModuleWarning(
        "warning:maven_module:missing_child_pom:" + modulePath,
        CATEGORY_MAVEN_MODULE,
        SIGNAL_MISSING_CHILD_POM,
        moduleId(modulePath),
        "Maven module declared in root pom.xml does not have a child pom.xml; v0.2 does not analyze this module.",
        ROOT_BUILD_FILE,
        List.of(declaration.evidence().id()));
  }

  private MavenModuleWarning nestedModuleDeclarationWarning(
      String modulePath,
      String childPomPath,
      MavenModuleDiscoveryEvidence childPomEvidence,
      ObservedModuleDeclaration nestedDeclaration) {
    return new MavenModuleWarning(
        "warning:maven_module:nested_module_declaration:" + modulePath,
        CATEGORY_MAVEN_MODULE,
        SIGNAL_NESTED_MODULE_DECLARATION,
        moduleId(modulePath),
        "Nested Maven module declaration detected in child pom.xml; v0.2 does not recursively discover nested modules.",
        childPomPath,
        List.of(childPomEvidence.id(), nestedDeclaration.evidence().id()));
  }

  private MavenModuleWarning unsupportedModuleWarning(
      String modulePath,
      String childPomPath,
      ObservedModuleDeclaration declaration,
      MavenModuleDiscoveryEvidence childPomEvidence) {
    return new MavenModuleWarning(
        "warning:maven_module:unsupported_module:" + modulePath,
        CATEGORY_MAVEN_MODULE,
        SIGNAL_UNSUPPORTED_MODULE,
        moduleId(modulePath),
        "Maven module has a child pom.xml but no supported Java source or test roots; v0.2 does not analyze this module.",
        childPomPath,
        List.of(declaration.evidence().id(), childPomEvidence.id()));
  }

  private Comparator<MavenModuleWarning> warningOrder(Map<String, Integer> moduleOrder) {
    return Comparator
        .comparing(MavenModuleWarning::category)
        .thenComparing(MavenModuleWarning::signal)
        .thenComparing(warning -> moduleOrder.getOrDefault(warning.moduleId(), Integer.MAX_VALUE))
        .thenComparing(MavenModuleWarning::sourcePath)
        .thenComparing(MavenModuleWarning::id);
  }

  private Map<String, Integer> moduleOrder(List<MavenModuleItem> moduleItems) {
    Map<String, Integer> order = new LinkedHashMap<>();
    for (int index = 0; index < moduleItems.size(); index++) {
      order.put(moduleItems.get(index).moduleId(), index);
    }
    return order;
  }

  private void addEvidence(
      Map<String, MavenModuleDiscoveryEvidence> evidence,
      MavenModuleDiscoveryEvidence record) {
    evidence.putIfAbsent(record.id(), record);
  }

  private String moduleId(String modulePath) {
    return "module:" + modulePath;
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

  private static int rootFirst(MavenModuleItem module) {
    return ".".equals(module.modulePath()) ? 0 : 1;
  }

  private static String nullSafe(String value) {
    return value == null ? "" : value;
  }

  private String ordinalText(int ordinal) {
    return String.format("%06d", ordinal);
  }

  private record ModuleDeclaration(
      int ordinal,
      String rawText,
      Integer lineStart,
      Integer lineEnd) {
    private String ordinalText() {
      return String.format("%06d", ordinal);
    }
  }

  private record NormalizedModulePath(String value) {
    private static NormalizedModulePath invalid() {
      return new NormalizedModulePath(null);
    }

    private java.util.Optional<String> normalizedPath() {
      return java.util.Optional.ofNullable(value);
    }
  }

  private record PreliminaryObservedModuleDeclaration(
      ModuleDeclaration declaration,
      NormalizedModulePath normalizedPath,
      String evidenceBaseId) {
  }

  private record ObservedModuleDeclaration(
      int ordinal,
      String rawText,
      java.util.Optional<String> normalizedPath,
      MavenModuleDiscoveryEvidence evidence) {
  }

  private static final class ModuleDeclarationHandler extends DefaultHandler2 {
    private final List<String> elementStack = new ArrayList<>();
    private final List<ModuleDeclaration> declarations = new ArrayList<>();
    private final StringBuilder moduleText = new StringBuilder();
    private Locator locator;
    private boolean readingModule;
    private Integer moduleLineStart;

    @Override
    public void setDocumentLocator(Locator locator) {
      this.locator = locator;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
      elementStack.add(elementName(localName, qName));
      if (isTopLevelModulePath()) {
        readingModule = true;
        moduleLineStart = currentLineNumber();
        moduleText.setLength(0);
      }
    }

    @Override
    public void characters(char[] characters, int start, int length) {
      if (readingModule) {
        moduleText.append(characters, start, length);
      }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
      if (readingModule && isTopLevelModulePath()) {
        declarations.add(new ModuleDeclaration(
            declarations.size() + 1,
            moduleText.toString(),
            moduleLineStart,
            currentLineNumber()));
        readingModule = false;
        moduleLineStart = null;
        moduleText.setLength(0);
      }

      if (!elementStack.isEmpty()) {
        elementStack.remove(elementStack.size() - 1);
      }
    }

    private List<ModuleDeclaration> declarations() {
      return List.copyOf(declarations);
    }

    private boolean isTopLevelModulePath() {
      return hasPath("project", "modules", "module");
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
