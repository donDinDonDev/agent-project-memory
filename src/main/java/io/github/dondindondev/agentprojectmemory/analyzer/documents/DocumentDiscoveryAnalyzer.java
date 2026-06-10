package io.github.dondindondev.agentprojectmemory.analyzer.documents;

import io.github.dondindondev.agentprojectmemory.analyzer.ScanPathContainment;
import io.github.dondindondev.agentprojectmemory.analyzer.maven.MavenModuleItem;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class DocumentDiscoveryAnalyzer {
  public static final DocumentDiscoveryPolicy DEFAULT_POLICY = new DocumentDiscoveryPolicy(
      "default_local_markdown",
      "repository_relative_in_root",
      "skip_symlinks",
      List.of(
          "README.md",
          "README.markdown",
          "<module>/README.md",
          "<module>/README.markdown",
          "docs/**/*.md",
          "adr/**/*.md",
          "adrs/**/*.md"),
      List.of(
          ".git/**",
          ".project-memory/**",
          "**/.*/**",
          "target/**",
          "build/**",
          "out/**",
          "dist/**",
          "node_modules/**",
          "**/generated/**",
          "**/maintainer/**",
          "docs/internal/**",
          "docs/private/**",
          "**/secrets/**"));
  private static final String ANALYSIS_ANALYZED = "analyzed";
  private static final String ANALYSIS_NOT_DETECTED = "not_detected";
  private static final String MODULE_SUPPORTED = "supported";
  private static final String DOCUMENT_KIND_LOCAL_MARKDOWN = "local_markdown";
  private static final String FORMAT_MARKDOWN = "markdown";
  private static final String DOCUMENT_SOURCE_TYPE = "document";
  private static final String HIGH_CONFIDENCE = "high";
  private static final String TITLE_SOURCE_FILENAME = "filename";
  private static final String ROOT_README = "root_readme";
  private static final String MODULE_README = "module_readme";
  private static final String DOCS_TREE = "docs_tree";
  private static final String ADR_TREE = "adr_tree";
  private static final int UNSCOPED_DOCUMENT_ORDER = Integer.MAX_VALUE;
  private static final Set<String> EXCLUDED_SEGMENTS = Set.of(
      ".git",
      ".project-memory",
      "target",
      "build",
      "out",
      "dist",
      "node_modules",
      "maintainer",
      "internal",
      "private",
      "secrets");
  private static final Comparator<DocumentFileFact> DOCUMENT_ORDER = Comparator
      .comparingInt(DocumentFileFact::moduleOrder)
      .thenComparing(DocumentFileFact::path)
      .thenComparing(DocumentFileFact::id);
  private final MarkdownDocumentStructureExtractor structureExtractor =
      new MarkdownDocumentStructureExtractor();

  public DocumentDiscoveryAnalysis analyze(
      Path repositoryRoot,
      List<MavenModuleItem> modules) throws IOException {
    Objects.requireNonNull(repositoryRoot, "repositoryRoot");
    Objects.requireNonNull(modules, "modules");

    Path normalizedRepositoryRoot = repositoryRoot.toAbsolutePath().normalize();
    Path canonicalRepositoryRoot = ScanPathContainment.canonicalRoot(normalizedRepositoryRoot);
    if (!safeDirectory(normalizedRepositoryRoot, normalizedRepositoryRoot, canonicalRepositoryRoot)) {
      return new DocumentDiscoveryAnalysis(
          ANALYSIS_NOT_DETECTED,
          DEFAULT_POLICY,
          List.of(),
          List.of());
    }

    List<SupportedModuleRoot> supportedModuleRoots = supportedModuleRoots(
        normalizedRepositoryRoot,
        canonicalRepositoryRoot,
        modules);
    Map<String, CandidateDocument> candidates = new LinkedHashMap<>();

    addReadmeCandidate(
        candidates,
        normalizedRepositoryRoot,
        canonicalRepositoryRoot,
        normalizedRepositoryRoot.resolve("README.md"),
        null,
        UNSCOPED_DOCUMENT_ORDER,
        ROOT_README);
    addReadmeCandidate(
        candidates,
        normalizedRepositoryRoot,
        canonicalRepositoryRoot,
        normalizedRepositoryRoot.resolve("README.markdown"),
        null,
        UNSCOPED_DOCUMENT_ORDER,
        ROOT_README);

    for (SupportedModuleRoot moduleRoot : supportedModuleRoots) {
      if (".".equals(moduleRoot.modulePath())) {
        continue;
      }
      addReadmeCandidate(
          candidates,
          normalizedRepositoryRoot,
          canonicalRepositoryRoot,
          moduleRoot.normalizedPath().resolve("README.md"),
          moduleRoot.moduleId(),
          moduleRoot.moduleOrder(),
          MODULE_README);
      addReadmeCandidate(
          candidates,
          normalizedRepositoryRoot,
          canonicalRepositoryRoot,
          moduleRoot.normalizedPath().resolve("README.markdown"),
          moduleRoot.moduleId(),
          moduleRoot.moduleOrder(),
          MODULE_README);
    }

    addMarkdownTreeCandidates(
        candidates,
        normalizedRepositoryRoot,
        canonicalRepositoryRoot,
        normalizedRepositoryRoot.resolve("docs"),
        DOCS_TREE,
        supportedModuleRoots);
    addMarkdownTreeCandidates(
        candidates,
        normalizedRepositoryRoot,
        canonicalRepositoryRoot,
        normalizedRepositoryRoot.resolve("adr"),
        ADR_TREE,
        supportedModuleRoots);
    addMarkdownTreeCandidates(
        candidates,
        normalizedRepositoryRoot,
        canonicalRepositoryRoot,
        normalizedRepositoryRoot.resolve("adrs"),
        ADR_TREE,
        supportedModuleRoots);

    List<AnalyzedDocument> analyzedDocuments = candidates.values().stream()
        .map(this::analyzedDocument)
        .sorted(Comparator.comparing(AnalyzedDocument::document, DOCUMENT_ORDER))
        .toList();
    List<DocumentFileFact> documents = analyzedDocuments.stream()
        .map(AnalyzedDocument::document)
        .toList();
    List<DocumentEvidence> evidence = analyzedDocuments.stream()
        .flatMap(document -> document.evidence().stream())
        .toList();
    return new DocumentDiscoveryAnalysis(
        documents.isEmpty() ? ANALYSIS_NOT_DETECTED : ANALYSIS_ANALYZED,
        DEFAULT_POLICY,
        documents,
        evidence);
  }

  private void addReadmeCandidate(
      Map<String, CandidateDocument> candidates,
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      Path file,
      String moduleId,
      int moduleOrder,
      String discoverySource) {
    Optional<String> sourcePath = acceptedMarkdownFile(
        repositoryRoot,
        canonicalRepositoryRoot,
        file,
        true);
    sourcePath.ifPresent(path -> candidates.putIfAbsent(
        path,
        new CandidateDocument(file.toAbsolutePath().normalize(), path, moduleId, moduleOrder, discoverySource)));
  }

  private void addMarkdownTreeCandidates(
      Map<String, CandidateDocument> candidates,
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      Path treeRoot,
      String discoverySource,
      List<SupportedModuleRoot> supportedModuleRoots) throws IOException {
    if (!safeDirectory(repositoryRoot, treeRoot, canonicalRepositoryRoot)
        || isExcluded(repositoryRoot, treeRoot)) {
      return;
    }

    List<Path> files = new ArrayList<>();
    Files.walkFileTree(
        treeRoot,
        new SimpleFileVisitor<>() {
          @Override
          public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            if (!safeDirectory(repositoryRoot, dir, canonicalRepositoryRoot)
                || isExcluded(repositoryRoot, dir)) {
              return FileVisitResult.SKIP_SUBTREE;
            }
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            acceptedMarkdownFile(repositoryRoot, canonicalRepositoryRoot, file, false)
                .ifPresent(ignored -> files.add(file.toAbsolutePath().normalize()));
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult visitFileFailed(Path file, IOException exception) {
            return FileVisitResult.CONTINUE;
          }
        });

    files.stream()
        .sorted(Comparator.comparing(file -> repositoryRelativePath(repositoryRoot, file)))
        .forEach(file -> {
          String path = repositoryRelativePath(repositoryRoot, file);
          Optional<SupportedModuleRoot> moduleRoot = owningModule(file, supportedModuleRoots);
          candidates.putIfAbsent(
              path,
              new CandidateDocument(
                  file,
                  path,
                  moduleRoot.map(SupportedModuleRoot::moduleId).orElse(null),
                  moduleRoot.map(SupportedModuleRoot::moduleOrder).orElse(UNSCOPED_DOCUMENT_ORDER),
                  discoverySource));
        });
  }

  private Optional<String> acceptedMarkdownFile(
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      Path file,
      boolean readmeOnly) {
    Path normalizedFile = file.toAbsolutePath().normalize();
    if (!normalizedFile.startsWith(repositoryRoot)) {
      return Optional.empty();
    }
    if (Files.isSymbolicLink(normalizedFile)
        || !Files.isRegularFile(normalizedFile, LinkOption.NOFOLLOW_LINKS)) {
      return Optional.empty();
    }
    if (hasSymbolicLinkSegment(repositoryRoot, normalizedFile)) {
      return Optional.empty();
    }
    if (ScanPathContainment.realPathUnderRoot(canonicalRepositoryRoot, normalizedFile).isEmpty()) {
      return Optional.empty();
    }
    if (isExcluded(repositoryRoot, normalizedFile)) {
      return Optional.empty();
    }

    String fileName = normalizedFile.getFileName().toString();
    if (readmeOnly && !("README.md".equals(fileName) || "README.markdown".equals(fileName))) {
      return Optional.empty();
    }
    if (!readmeOnly && !fileName.endsWith(".md")) {
      return Optional.empty();
    }
    return Optional.of(repositoryRelativePath(repositoryRoot, normalizedFile));
  }

  private boolean safeDirectory(Path repositoryRoot, Path directory, Path canonicalRepositoryRoot) {
    Path normalizedDirectory = directory.toAbsolutePath().normalize();
    return normalizedDirectory.startsWith(repositoryRoot)
        && !Files.isSymbolicLink(normalizedDirectory)
        && !hasSymbolicLinkSegment(repositoryRoot, normalizedDirectory)
        && Files.isDirectory(normalizedDirectory, LinkOption.NOFOLLOW_LINKS)
        && ScanPathContainment.realPathUnderRoot(canonicalRepositoryRoot, normalizedDirectory).isPresent();
  }

  private boolean hasSymbolicLinkSegment(Path repositoryRoot, Path path) {
    Path normalizedPath = path.toAbsolutePath().normalize();
    if (!normalizedPath.startsWith(repositoryRoot)) {
      return true;
    }
    Path current = repositoryRoot;
    for (Path part : repositoryRoot.relativize(normalizedPath)) {
      current = current.resolve(part);
      if (Files.isSymbolicLink(current)) {
        return true;
      }
    }
    return false;
  }

  private boolean isExcluded(Path repositoryRoot, Path path) {
    Path normalizedPath = path.toAbsolutePath().normalize();
    if (!normalizedPath.startsWith(repositoryRoot)) {
      return true;
    }
    Path relativePath = repositoryRoot.relativize(normalizedPath);
    for (Path part : relativePath) {
      String segment = part.toString();
      String lowerSegment = segment.toLowerCase(Locale.ROOT);
      if (segment.startsWith(".")
          || EXCLUDED_SEGMENTS.contains(lowerSegment)
          || isGeneratedLike(lowerSegment)) {
        return true;
      }
    }
    return false;
  }

  private boolean isGeneratedLike(String segment) {
    return "generated".equals(segment)
        || segment.startsWith("generated-")
        || segment.endsWith("-generated")
        || segment.startsWith("generated_")
        || segment.endsWith("_generated");
  }

  private List<SupportedModuleRoot> supportedModuleRoots(
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      List<MavenModuleItem> modules) {
    List<SupportedModuleRoot> roots = new ArrayList<>();
    for (int index = 0; index < modules.size(); index++) {
      MavenModuleItem module = modules.get(index);
      if (!MODULE_SUPPORTED.equals(module.supportStatus())) {
        continue;
      }
      Path moduleRoot = ".".equals(module.modulePath())
          ? repositoryRoot
          : repositoryRoot.resolve(module.modulePath()).normalize();
      if (!safeDirectory(repositoryRoot, moduleRoot, canonicalRepositoryRoot)
          || isExcluded(repositoryRoot, moduleRoot)) {
        continue;
      }
      roots.add(new SupportedModuleRoot(
          module.moduleId(),
          module.modulePath(),
          index,
          moduleRoot.toAbsolutePath().normalize()));
    }
    return roots.stream()
        .sorted(Comparator
            .comparingInt((SupportedModuleRoot root) -> root.modulePath().length()).reversed()
            .thenComparingInt(SupportedModuleRoot::moduleOrder))
        .toList();
  }

  private Optional<SupportedModuleRoot> owningModule(
      Path file,
      List<SupportedModuleRoot> supportedModuleRoots) {
    Path normalizedFile = file.toAbsolutePath().normalize();
    for (SupportedModuleRoot moduleRoot : supportedModuleRoots) {
      if (!".".equals(moduleRoot.modulePath())
          && normalizedFile.startsWith(moduleRoot.normalizedPath())) {
        return Optional.of(moduleRoot);
      }
    }
    return Optional.empty();
  }

  private AnalyzedDocument analyzedDocument(CandidateDocument candidate) {
    DocumentStructure structure = documentStructure(candidate);
    List<DocumentEvidence> evidence = new ArrayList<>();
    DocumentEvidence fileEvidence = fileEvidence(candidate);
    evidence.add(fileEvidence);
    List<DocumentHeadingFact> headings = headingsWithEvidence(
        candidate.sourcePath(),
        structure.headings(),
        evidence);
    List<DocumentChunkFact> chunks = chunksWithEvidence(
        candidate.sourcePath(),
        structure.chunks(),
        headings,
        evidence);

    String title = titleFromFilename(candidate.normalizedPath().getFileName().toString());
    String titleSource = TITLE_SOURCE_FILENAME;
    Optional<DocumentHeadingFact> firstNonBlankHeading = headings.stream()
        .filter(heading -> !heading.title().isBlank())
        .findFirst();
    if (firstNonBlankHeading.isPresent()) {
      title = firstNonBlankHeading.get().title();
      titleSource = "first_heading";
    }
    DocumentFileFact document = new DocumentFileFact(
        "document:" + idKey(candidate.sourcePath()),
        DOCUMENT_KIND_LOCAL_MARKDOWN,
        FORMAT_MARKDOWN,
        candidate.moduleId(),
        candidate.moduleOrder(),
        candidate.sourcePath(),
        title,
        titleSource,
        candidate.discoverySource(),
        headings,
        chunks,
        List.of(fileEvidence.id()));
    return new AnalyzedDocument(document, evidence);
  }

  private DocumentEvidence fileEvidence(CandidateDocument candidate) {
    String fileName = candidate.normalizedPath().getFileName().toString();
    return new DocumentEvidence(
        "ev:"
            + idKey(candidate.sourcePath())
            + ":unknown:document:file:"
            + idKey(fileName),
        DOCUMENT_SOURCE_TYPE,
        candidate.sourcePath(),
        null,
        null,
        "file:" + fileName,
        null,
        null,
        "markdown file detected: " + candidate.sourcePath(),
        HIGH_CONFIDENCE);
  }

  private List<DocumentHeadingFact> headingsWithEvidence(
      String sourcePath,
      List<DocumentHeadingFact> headings,
      List<DocumentEvidence> evidence) {
    List<DocumentHeadingFact> updatedHeadings = new ArrayList<>();
    for (int index = 0; index < headings.size(); index++) {
      DocumentHeadingFact heading = headings.get(index);
      String ordinal = zeroPadded(index + 1);
      String titleKey = heading.title().isBlank() ? "untitled" : heading.title();
      DocumentEvidence headingEvidence = new DocumentEvidence(
          "ev:"
              + idKey(sourcePath)
              + ":"
              + lineRangeKey(heading.lineStart(), heading.lineEnd())
              + ":document:heading:"
              + idKey(titleKey)
              + ":decl:"
              + ordinal,
          DOCUMENT_SOURCE_TYPE,
          sourcePath,
          null,
          null,
          "heading:" + titleKey,
          heading.lineStart(),
          heading.lineEnd(),
          normalizedHeadingLine(heading),
          HIGH_CONFIDENCE);
      evidence.add(headingEvidence);
      updatedHeadings.add(new DocumentHeadingFact(
          heading.id(),
          heading.level(),
          heading.title(),
          heading.anchor(),
          heading.lineStart(),
          heading.lineEnd(),
          List.of(headingEvidence.id())));
    }
    return updatedHeadings;
  }

  private List<DocumentChunkFact> chunksWithEvidence(
      String sourcePath,
      List<DocumentChunkFact> chunks,
      List<DocumentHeadingFact> headings,
      List<DocumentEvidence> evidence) {
    Map<String, String> headingTitleById = new LinkedHashMap<>();
    for (DocumentHeadingFact heading : headings) {
      headingTitleById.put(heading.id(), heading.title().isBlank() ? "untitled" : heading.title());
    }

    List<DocumentChunkFact> updatedChunks = new ArrayList<>();
    for (int index = 0; index < chunks.size(); index++) {
      DocumentChunkFact chunk = chunks.get(index);
      String ordinal = zeroPadded(index + 1);
      DocumentEvidence chunkEvidence = new DocumentEvidence(
          "ev:"
              + idKey(sourcePath)
              + ":"
              + lineRangeKey(chunk.lineStart(), chunk.lineEnd())
              + ":document:chunk:"
              + ordinal,
          DOCUMENT_SOURCE_TYPE,
          sourcePath,
          null,
          null,
          "chunk:" + ordinal,
          chunk.lineStart(),
          chunk.lineEnd(),
          chunkExcerpt(chunk, headingTitleById),
          HIGH_CONFIDENCE);
      evidence.add(chunkEvidence);
      updatedChunks.add(new DocumentChunkFact(
          chunk.id(),
          chunk.headingId(),
          chunk.lineStart(),
          chunk.lineEnd(),
          chunk.contentStatus(),
          List.of(chunkEvidence.id())));
    }
    return updatedChunks;
  }

  private String normalizedHeadingLine(DocumentHeadingFact heading) {
    String marker = "#".repeat(heading.level());
    if (heading.title().isBlank()) {
      return marker;
    }
    return marker + " " + heading.title();
  }

  private String chunkExcerpt(DocumentChunkFact chunk, Map<String, String> headingTitleById) {
    String owningHeading = chunk.headingId() == null
        ? "none"
        : headingTitleById.getOrDefault(chunk.headingId(), chunk.headingId());
    return "chunk lines "
        + chunk.lineStart()
        + "-"
        + chunk.lineEnd()
        + "; heading: "
        + owningHeading;
  }

  private String lineRangeKey(Integer lineStart, Integer lineEnd) {
    if (lineStart == null || lineEnd == null) {
      return "unknown";
    }
    return lineStart + "-" + lineEnd;
  }

  private String zeroPadded(int value) {
    return String.format(Locale.ROOT, "%06d", value);
  }

  private DocumentStructure documentStructure(CandidateDocument candidate) {
    try {
      return structureExtractor.extract(candidate.normalizedPath(), candidate.sourcePath());
    } catch (IOException exception) {
      return DocumentStructure.empty();
    }
  }

  private String titleFromFilename(String fileName) {
    if (fileName.endsWith(".markdown")) {
      return fileName.substring(0, fileName.length() - ".markdown".length());
    }
    if (fileName.endsWith(".md")) {
      return fileName.substring(0, fileName.length() - ".md".length());
    }
    return fileName;
  }

  private String repositoryRelativePath(Path repositoryRoot, Path file) {
    return repositoryRoot.relativize(file.toAbsolutePath().normalize()).toString().replace('\\', '/');
  }

  static String idKey(String value) {
    byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
    StringBuilder key = new StringBuilder();
    for (byte rawByte : bytes) {
      int unsignedByte = rawByte & 0xFF;
      char character = (char) unsignedByte;
      if (isAllowedKeyCharacter(character)) {
        key.append(character);
      } else {
        key.append('%')
            .append(String.format(Locale.ROOT, "%02X", unsignedByte));
      }
    }
    return key.toString();
  }

  private static boolean isAllowedKeyCharacter(char character) {
    return (character >= 'A' && character <= 'Z')
        || (character >= 'a' && character <= 'z')
        || (character >= '0' && character <= '9')
        || character == '.'
        || character == '_'
        || character == '-'
        || character == '~'
        || character == '/'
        || character == '{'
        || character == '}';
  }

  private record CandidateDocument(
      Path normalizedPath,
      String sourcePath,
      String moduleId,
      int moduleOrder,
      String discoverySource) {
  }

  private record AnalyzedDocument(
      DocumentFileFact document,
      List<DocumentEvidence> evidence) {
  }

  private record SupportedModuleRoot(
      String moduleId,
      String modulePath,
      int moduleOrder,
      Path normalizedPath) {
  }
}
