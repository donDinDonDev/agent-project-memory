package io.github.dondindondev.agentprojectmemory.analyzer.warnings;

import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import io.github.dondindondev.agentprojectmemory.analyzer.JavaSourceParser;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

public final class AnalysisWarningAnalyzer {
  private static final String CATEGORY_HIDDEN_HTTP_SURFACE = "hidden_http_surface";
  private static final String SIGNAL_OPENAPI_SPEC_FILE = "openapi_spec_file";
  private static final String SIGNAL_MAVEN_CODEGEN_PLUGIN = "maven_openapi_swagger_codegen_plugin";
  private static final String SIGNAL_REPOSITORY_REST_RESOURCE = "repository_rest_resource";
  private static final String HIGH_CONFIDENCE = "high";
  private static final String CONFIG_FILE_SOURCE_TYPE = "config_file";
  private static final String BUILD_FILE_SOURCE_TYPE = "build_file";
  private static final String ANNOTATION_SOURCE_TYPE = "annotation";
  private static final String REPOSITORY_REST_RESOURCE = "RepositoryRestResource";
  private static final String ROOT_BUILD_FILE = "pom.xml";
  private static final Set<String> OPENAPI_SPEC_FILENAMES = Set.of(
      "openapi.yml",
      "openapi.yaml",
      "swagger.yml",
      "swagger.yaml");
  private static final Set<String> MAVEN_CODEGEN_PLUGIN_ARTIFACT_IDS = Set.of(
      "openapi-generator-maven-plugin",
      "swagger-codegen-maven-plugin");
  private static final Comparator<AnalysisWarningFact> WARNING_ORDER = Comparator
      .comparing(AnalysisWarningFact::category)
      .thenComparing(AnalysisWarningFact::signal)
      .thenComparing(AnalysisWarningFact::sourcePath)
      .thenComparing(AnalysisWarningFact::id);

  public AnalysisWarningAnalysis analyze(Path repositoryRoot, List<Path> sourceRoots) throws IOException {
    Objects.requireNonNull(repositoryRoot, "repositoryRoot");
    Objects.requireNonNull(sourceRoots, "sourceRoots");

    Path normalizedRepositoryRoot = repositoryRoot.toAbsolutePath().normalize();
    List<AnalysisWarningFact> warnings = new ArrayList<>();
    List<AnalysisWarningEvidence> evidence = new ArrayList<>();

    analyzeOpenApiSpecFiles(normalizedRepositoryRoot, warnings, evidence);
    analyzeMavenPluginSignals(normalizedRepositoryRoot, warnings, evidence);
    analyzeRepositoryRestResources(normalizedRepositoryRoot, sourceRoots, warnings, evidence);

    return new AnalysisWarningAnalysis(
        warnings.stream().sorted(WARNING_ORDER).toList(),
        evidence);
  }

  private void analyzeOpenApiSpecFiles(
      Path repositoryRoot,
      List<AnalysisWarningFact> warnings,
      List<AnalysisWarningEvidence> evidence) throws IOException {
    for (Path specFile : repositoryFiles(repositoryRoot)) {
      String fileName = specFile.getFileName().toString().toLowerCase(Locale.ROOT);
      if (!OPENAPI_SPEC_FILENAMES.contains(fileName)) {
        continue;
      }

      String sourcePath = repositoryRelativePath(repositoryRoot, specFile);
      AnalysisWarningEvidence specEvidence = new AnalysisWarningEvidence(
          "ev:" + sourcePath + ":unknown:config_file:" + specFile.getFileName(),
          CONFIG_FILE_SOURCE_TYPE,
          sourcePath,
          null,
          null,
          specFile.getFileName().toString(),
          null,
          null,
          "filename detected: " + specFile.getFileName(),
          HIGH_CONFIDENCE);
      evidence.add(specEvidence);
      warnings.add(new AnalysisWarningFact(
          warningId(SIGNAL_OPENAPI_SPEC_FILE, sourcePath),
          CATEGORY_HIDDEN_HTTP_SURFACE,
          SIGNAL_OPENAPI_SPEC_FILE,
          "OpenAPI/Swagger spec file detected by filename only; v0.1 does not parse specs or reconstruct generated APIs.",
          sourcePath,
          List.of(specEvidence.id())));
    }
  }

  private void analyzeMavenPluginSignals(
      Path repositoryRoot,
      List<AnalysisWarningFact> warnings,
      List<AnalysisWarningEvidence> evidence) throws IOException {
    Path pom = repositoryRoot.resolve(ROOT_BUILD_FILE);
    if (!Files.isRegularFile(pom)) {
      return;
    }

    List<String> lines = Files.readAllLines(pom, StandardCharsets.UTF_8);
    Set<String> emittedArtifactIds = new LinkedHashSet<>();
    for (MavenPluginArtifactIdSignal signal : mavenPluginArtifactIdSignals(pom)) {
      if (!emittedArtifactIds.add(signal.artifactId())) {
        continue;
      }

      Integer lineNumber = signal.lineNumber();
      String lineRange = lineNumber == null ? "unknown" : lineNumber + "-" + lineNumber;
      AnalysisWarningEvidence pluginEvidence = new AnalysisWarningEvidence(
          "ev:" + ROOT_BUILD_FILE + ":" + lineRange
              + ":build_file:" + signal.artifactId(),
          BUILD_FILE_SOURCE_TYPE,
          ROOT_BUILD_FILE,
          null,
          null,
          signal.artifactId(),
          lineNumber,
          lineNumber,
          sourceLineExcerpt(lines, lineNumber, signal.artifactId()),
          HIGH_CONFIDENCE);
      evidence.add(pluginEvidence);
      warnings.add(new AnalysisWarningFact(
          warningId(SIGNAL_MAVEN_CODEGEN_PLUGIN, ROOT_BUILD_FILE + ":" + signal.artifactId()),
          CATEGORY_HIDDEN_HTTP_SURFACE,
          SIGNAL_MAVEN_CODEGEN_PLUGIN,
          "Maven OpenAPI/Swagger code generation plugin signal detected; v0.1 does not run generation or scan generated sources by default.",
          ROOT_BUILD_FILE,
          List.of(pluginEvidence.id())));
    }
  }

  private List<MavenPluginArtifactIdSignal> mavenPluginArtifactIdSignals(Path pom) throws IOException {
    MavenPluginArtifactIdHandler handler = new MavenPluginArtifactIdHandler();
    try (InputStream input = Files.newInputStream(pom)) {
      SAXParserFactory factory = secureSaxParserFactory();
      factory.newSAXParser().parse(input, handler);
    } catch (SAXException exception) {
      return List.of();
    } catch (ParserConfigurationException exception) {
      throw new IOException("Unable to configure secure XML parser for " + ROOT_BUILD_FILE, exception);
    }

    return handler.signals();
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

  private String sourceLineExcerpt(List<String> lines, Integer lineNumber, String artifactId) {
    if (lineNumber != null && lineNumber >= 1 && lineNumber <= lines.size()) {
      return lines.get(lineNumber - 1).trim();
    }
    return "<artifactId>" + artifactId + "</artifactId>";
  }

  private void analyzeRepositoryRestResources(
      Path repositoryRoot,
      List<Path> sourceRoots,
      List<AnalysisWarningFact> warnings,
      List<AnalysisWarningEvidence> evidence) throws IOException {
    for (Path sourceRoot : sourceRoots) {
      Path normalizedSourceRoot = normalizeSourceRoot(repositoryRoot, sourceRoot);
      if (!Files.isDirectory(normalizedSourceRoot)) {
        continue;
      }

      for (Path javaFile : javaFiles(normalizedSourceRoot)) {
        analyzeRepositoryRestResourceJavaFile(repositoryRoot, javaFile, warnings, evidence);
      }
    }
  }

  private void analyzeRepositoryRestResourceJavaFile(
      Path repositoryRoot,
      Path javaFile,
      List<AnalysisWarningFact> warnings,
      List<AnalysisWarningEvidence> evidence) throws IOException {
    CompilationUnit compilationUnit = JavaSourceParser.parse(javaFile);
    String packageName = compilationUnit.getPackageDeclaration()
        .map(packageDeclaration -> packageDeclaration.getName().asString())
        .orElse("");
    String sourcePath = repositoryRelativePath(repositoryRoot, javaFile);
    List<String> sourceLines = Files.readAllLines(javaFile, StandardCharsets.UTF_8);

    for (ClassOrInterfaceDeclaration type : compilationUnit.findAll(ClassOrInterfaceDeclaration.class)) {
      Optional<AnnotationExpr> annotation = type.getAnnotations().stream()
          .filter(candidate -> simpleAnnotationName(candidate).equals(REPOSITORY_REST_RESOURCE))
          .findFirst();
      if (annotation.isEmpty()) {
        continue;
      }

      String typeName = qualifiedTypeName(packageName, type);
      AnalysisWarningEvidence annotationEvidence = annotationEvidence(
          sourcePath,
          typeName,
          annotation.orElseThrow(),
          sourceLines);
      evidence.add(annotationEvidence);
      warnings.add(new AnalysisWarningFact(
          warningId(SIGNAL_REPOSITORY_REST_RESOURCE, typeName),
          CATEGORY_HIDDEN_HTTP_SURFACE,
          SIGNAL_REPOSITORY_REST_RESOURCE,
          "Direct @RepositoryRestResource detected; v0.1 warns about possible Spring Data REST HTTP surface but does not expand endpoints.",
          sourcePath,
          List.of(annotationEvidence.id())));
    }
  }

  private AnalysisWarningEvidence annotationEvidence(
      String sourcePath,
      String className,
      AnnotationExpr annotation,
      List<String> sourceLines) {
    String annotationSymbol = "@" + simpleAnnotationName(annotation);
    Integer lineStart = annotation.getRange().map(range -> range.begin.line).orElse(null);
    Integer lineEnd = annotation.getRange().map(range -> range.end.line).orElse(null);
    String lineRange = lineStart == null || lineEnd == null ? "unknown" : lineStart + "-" + lineEnd;

    return new AnalysisWarningEvidence(
        "ev:" + sourcePath + ":" + lineRange + ":" + className + ":" + annotationSymbol,
        ANNOTATION_SOURCE_TYPE,
        sourcePath,
        className,
        null,
        annotationSymbol,
        lineStart,
        lineEnd,
        excerpt(annotation, sourceLines),
        HIGH_CONFIDENCE);
  }

  private List<Path> repositoryFiles(Path repositoryRoot) throws IOException {
    try (Stream<Path> paths = Files.walk(repositoryRoot)) {
      return paths
          .filter(path -> Files.isRegularFile(path) && !isExcluded(repositoryRoot, path))
          .sorted(Comparator.comparing(path -> repositoryRelativePath(repositoryRoot, path)))
          .toList();
    }
  }

  private boolean isExcluded(Path repositoryRoot, Path path) {
    Path relativePath = repositoryRoot.relativize(path.toAbsolutePath().normalize());
    for (Path part : relativePath) {
      String name = part.toString();
      if (".git".equals(name) || ".project-memory".equals(name) || "target".equals(name)) {
        return true;
      }
    }
    return false;
  }

  private Path normalizeSourceRoot(Path repositoryRoot, Path sourceRoot) {
    Objects.requireNonNull(sourceRoot, "sourceRoot");
    if (sourceRoot.isAbsolute()) {
      return sourceRoot.toAbsolutePath().normalize();
    }
    return repositoryRoot.resolve(sourceRoot).normalize();
  }

  private List<Path> javaFiles(Path sourceRoot) throws IOException {
    try (Stream<Path> paths = Files.walk(sourceRoot)) {
      return paths
          .filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".java"))
          .sorted(Comparator.comparing(path -> path.toAbsolutePath().normalize().toString()))
          .toList();
    }
  }

  private String warningId(String signal, String sourceKey) {
    return "warning:" + CATEGORY_HIDDEN_HTTP_SURFACE + ":" + signal + ":" + sourceKey;
  }

  private String excerpt(AnnotationExpr annotation, List<String> sourceLines) {
    Optional<Range> range = annotation.getRange();
    if (range.isEmpty()) {
      return annotation.toString();
    }

    int start = range.orElseThrow().begin.line;
    int end = range.orElseThrow().end.line;
    if (start < 1 || end < start || end > sourceLines.size()) {
      return annotation.toString();
    }

    return String.join("\n", sourceLines.subList(start - 1, end)).trim();
  }

  private String repositoryRelativePath(Path repositoryRoot, Path javaFile) {
    Path relativePath = repositoryRoot.relativize(javaFile.toAbsolutePath().normalize());
    return relativePath.toString().replace(javaFile.getFileSystem().getSeparator(), "/");
  }

  private String qualifiedTypeName(String packageName, ClassOrInterfaceDeclaration type) {
    return type.getFullyQualifiedName()
        .orElseGet(() -> packageName.isBlank()
            ? type.getNameAsString()
            : packageName + "." + type.getNameAsString());
  }

  private String simpleAnnotationName(AnnotationExpr annotation) {
    String name = annotation.getNameAsString();
    int lastDot = name.lastIndexOf('.');
    if (lastDot >= 0) {
      return name.substring(lastDot + 1);
    }
    return name;
  }

  private record MavenPluginArtifactIdSignal(String artifactId, Integer lineNumber) {
  }

  private static final class MavenPluginArtifactIdHandler extends DefaultHandler2 {
    private final List<String> elementStack = new ArrayList<>();
    private final List<MavenPluginArtifactIdSignal> signals = new ArrayList<>();
    private final StringBuilder artifactIdText = new StringBuilder();
    private Locator locator;
    private boolean readingPluginArtifactId;
    private Integer artifactIdLineNumber;

    @Override
    public void setDocumentLocator(Locator locator) {
      this.locator = locator;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
      elementStack.add(elementName(localName, qName));
      if (isPluginArtifactIdPath()) {
        readingPluginArtifactId = true;
        artifactIdLineNumber = currentLineNumber();
        artifactIdText.setLength(0);
      }
    }

    @Override
    public void characters(char[] characters, int start, int length) {
      if (readingPluginArtifactId) {
        artifactIdText.append(characters, start, length);
      }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
      if (readingPluginArtifactId && isPluginArtifactIdPath()) {
        String artifactId = artifactIdText.toString().trim();
        if (MAVEN_CODEGEN_PLUGIN_ARTIFACT_IDS.contains(artifactId)) {
          signals.add(new MavenPluginArtifactIdSignal(artifactId, artifactIdLineNumber));
        }
        readingPluginArtifactId = false;
        artifactIdLineNumber = null;
        artifactIdText.setLength(0);
      }

      if (!elementStack.isEmpty()) {
        elementStack.remove(elementStack.size() - 1);
      }
    }

    private List<MavenPluginArtifactIdSignal> signals() {
      return List.copyOf(signals);
    }

    private boolean isPluginArtifactIdPath() {
      return hasPath("project", "build", "plugins", "plugin", "artifactId")
          || hasPath("project", "build", "pluginManagement", "plugins", "plugin", "artifactId");
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
