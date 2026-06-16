package io.github.dondindondev.agentprojectmemory.analyzer.documents;

import io.github.dondindondev.agentprojectmemory.OutputRedactor;
import io.github.dondindondev.agentprojectmemory.analyzer.BoundedCandidateSet;
import io.github.dondindondev.agentprojectmemory.analyzer.ScanDiagnostic;
import io.github.dondindondev.agentprojectmemory.analyzer.ScanPathContainment;
import io.github.dondindondev.agentprojectmemory.analyzer.build.BuildModule;
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
import java.util.OptionalLong;
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
  private static final String DIAGNOSTIC_SEVERITY_WARNING = "warning";
  private static final String DIAGNOSTIC_CATEGORY_DOCUMENTS = "documents";
  private static final String DIAGNOSTIC_DOCUMENT_COUNT_CAP =
      "local_markdown_document_count_cap_reached";
  private static final String DIAGNOSTIC_DOCUMENT_BYTES_CAP =
      "local_markdown_document_bytes_cap_reached";
  private static final String DIAGNOSTIC_HEADING_COUNT_CAP =
      "local_markdown_heading_count_cap_reached";
  private static final String DIAGNOSTIC_CHUNK_COUNT_CAP =
      "local_markdown_chunk_count_cap_reached";
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
  private static final Comparator<CandidateDocument> CANDIDATE_ORDER = Comparator
      .comparingInt(CandidateDocument::moduleOrder)
      .thenComparing(CandidateDocument::sourcePath);
  private final MarkdownDocumentStructureExtractor structureExtractor =
      new MarkdownDocumentStructureExtractor();
  private final DocumentAnalysisLimits limits;

  public DocumentDiscoveryAnalyzer() {
    this(DocumentAnalysisLimits.defaults());
  }

  DocumentDiscoveryAnalyzer(DocumentAnalysisLimits limits) {
    this.limits = Objects.requireNonNull(limits, "limits");
  }

  public DocumentDiscoveryAnalysis analyze(
      Path repositoryRoot,
      List<? extends BuildModule> modules) throws IOException {
    return analyze(repositoryRoot, modules, DocumentDiscoveryOptions.defaults());
  }

  public DocumentDiscoveryAnalysis analyze(
      Path repositoryRoot,
      List<? extends BuildModule> modules,
      DocumentDiscoveryOptions options) throws IOException {
    Objects.requireNonNull(repositoryRoot, "repositoryRoot");
    Objects.requireNonNull(modules, "modules");
    Objects.requireNonNull(options, "options");

    if (!options.localMarkdownEnabled()) {
      return DocumentDiscoveryAnalysis.notAnalyzed(DEFAULT_POLICY);
    }

    Path normalizedRepositoryRoot = repositoryRoot.toAbsolutePath().normalize();
    Path canonicalRepositoryRoot = ScanPathContainment.canonicalRoot(normalizedRepositoryRoot);
    if (!safeDirectory(normalizedRepositoryRoot, normalizedRepositoryRoot, canonicalRepositoryRoot)) {
      return new DocumentDiscoveryAnalysis(
          ANALYSIS_NOT_DETECTED,
          DEFAULT_POLICY,
          List.of(),
          List.of(),
          List.of());
    }

    List<SupportedModuleRoot> supportedModuleRoots = supportedModuleRoots(
        normalizedRepositoryRoot,
        canonicalRepositoryRoot,
        modules);
    BoundedCandidateSet<CandidateDocument> candidates = new BoundedCandidateSet<>(
        limits.maxDocuments(),
        CANDIDATE_ORDER,
        CandidateDocument::sourcePath);

    addReadmeCandidate(
        candidates,
        normalizedRepositoryRoot,
        canonicalRepositoryRoot,
        normalizedRepositoryRoot.resolve("README.md"),
        null,
        UNSCOPED_DOCUMENT_ORDER,
        ROOT_README,
        options);
    addReadmeCandidate(
        candidates,
        normalizedRepositoryRoot,
        canonicalRepositoryRoot,
        normalizedRepositoryRoot.resolve("README.markdown"),
        null,
        UNSCOPED_DOCUMENT_ORDER,
        ROOT_README,
        options);

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
          MODULE_README,
          options);
      addReadmeCandidate(
          candidates,
          normalizedRepositoryRoot,
          canonicalRepositoryRoot,
          moduleRoot.normalizedPath().resolve("README.markdown"),
          moduleRoot.moduleId(),
          moduleRoot.moduleOrder(),
          MODULE_README,
          options);
    }

    addMarkdownTreeCandidates(
        candidates,
        normalizedRepositoryRoot,
        canonicalRepositoryRoot,
        normalizedRepositoryRoot.resolve("docs"),
        DOCS_TREE,
        supportedModuleRoots,
        options);
    addMarkdownTreeCandidates(
        candidates,
        normalizedRepositoryRoot,
        canonicalRepositoryRoot,
        normalizedRepositoryRoot.resolve("adr"),
        ADR_TREE,
        supportedModuleRoots,
        options);
    addMarkdownTreeCandidates(
        candidates,
        normalizedRepositoryRoot,
        canonicalRepositoryRoot,
        normalizedRepositoryRoot.resolve("adrs"),
        ADR_TREE,
        supportedModuleRoots,
        options);
    addExplicitIncludeCandidates(
        candidates,
        normalizedRepositoryRoot,
        canonicalRepositoryRoot,
        supportedModuleRoots,
        options);

    List<ScanDiagnostic> diagnostics = new ArrayList<>();
    List<AnalyzedDocument> analyzedDocuments = new ArrayList<>();
    boolean documentCountCapReported = false;
    boolean documentBytesCapReported = false;
    boolean headingCountCapReported = false;
    boolean chunkCountCapReported = false;
    long acceptedDocumentBytes = 0;
    int acceptedHeadings = 0;
    int acceptedChunks = 0;

    for (CandidateDocument candidate : candidates.sorted()) {
      if (analyzedDocuments.size() >= limits.maxDocuments()) {
        if (!documentCountCapReported) {
          diagnostics.add(documentCountCapDiagnostic());
          documentCountCapReported = true;
        }
        continue;
      }

      OptionalLong candidateSize = documentSize(candidate);
      long remainingBytes = limits.maxTotalDocumentBytes() - acceptedDocumentBytes;
      if (candidateSize.isEmpty() || candidateSize.getAsLong() > remainingBytes) {
        if (!documentBytesCapReported) {
          diagnostics.add(documentBytesCapDiagnostic());
          documentBytesCapReported = true;
        }
        continue;
      }
      long documentBytes = candidateSize.getAsLong();

      DocumentStructure structure = documentStructure(
          candidate,
          remainingCount(limits.maxHeadings(), acceptedHeadings),
          remainingCount(limits.maxChunks(), acceptedChunks));
      if (structure.headingCapReached() && !headingCountCapReported) {
        diagnostics.add(headingCountCapDiagnostic());
        headingCountCapReported = true;
      }
      if (structure.chunkCapReached() && !chunkCountCapReported) {
        diagnostics.add(chunkCountCapDiagnostic());
        chunkCountCapReported = true;
      }

      AnalyzedDocument analyzedDocument = analyzedDocument(candidate, structure);
      analyzedDocuments.add(analyzedDocument);
      acceptedDocumentBytes += documentBytes;
      acceptedHeadings += analyzedDocument.document().headings().size();
      acceptedChunks += analyzedDocument.document().chunks().size();
    }
    if (candidates.capReached() && !documentCountCapReported) {
      diagnostics.add(documentCountCapDiagnostic());
    }

    analyzedDocuments = analyzedDocuments.stream()
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
        evidence,
        diagnostics);
  }

  private void addExplicitIncludeCandidates(
      BoundedCandidateSet<CandidateDocument> candidates,
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      List<SupportedModuleRoot> supportedModuleRoots,
      DocumentDiscoveryOptions options) throws IOException {
    if (options.includes().isEmpty()) {
      return;
    }

    Files.walkFileTree(
        repositoryRoot,
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
            acceptedUserMarkdownFile(repositoryRoot, canonicalRepositoryRoot, file)
                .filter(path -> options.includes().stream().anyMatch(pattern -> pattern.matches(path)))
                .ifPresent(path -> {
                  Path normalizedFile = file.toAbsolutePath().normalize();
                  Optional<SupportedModuleRoot> moduleRoot = owningModule(
                      normalizedFile,
                      supportedModuleRoots);
                  addCandidate(
                      candidates,
                      new CandidateDocument(
                          normalizedFile,
                          path,
                          moduleRoot.map(SupportedModuleRoot::moduleId).orElse(null),
                          moduleRoot.map(SupportedModuleRoot::moduleOrder).orElse(UNSCOPED_DOCUMENT_ORDER),
                          "explicit_include"),
                      options);
                });
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult visitFileFailed(Path file, IOException exception) {
            return FileVisitResult.CONTINUE;
          }
        });
  }

  private void addReadmeCandidate(
      BoundedCandidateSet<CandidateDocument> candidates,
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      Path file,
      String moduleId,
      int moduleOrder,
      String discoverySource,
      DocumentDiscoveryOptions options) {
    Optional<String> sourcePath = acceptedMarkdownFile(
        repositoryRoot,
        canonicalRepositoryRoot,
        file,
        true);
    sourcePath.ifPresent(path -> addCandidate(
        candidates,
        new CandidateDocument(file.toAbsolutePath().normalize(), path, moduleId, moduleOrder, discoverySource),
        options));
  }

  private void addMarkdownTreeCandidates(
      BoundedCandidateSet<CandidateDocument> candidates,
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      Path treeRoot,
      String discoverySource,
      List<SupportedModuleRoot> supportedModuleRoots,
      DocumentDiscoveryOptions options) throws IOException {
    if (!safeDirectory(repositoryRoot, treeRoot, canonicalRepositoryRoot)
        || isExcluded(repositoryRoot, treeRoot)) {
      return;
    }

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
                .ifPresent(path -> {
                  Path normalizedFile = file.toAbsolutePath().normalize();
                  Optional<SupportedModuleRoot> moduleRoot = owningModule(
                      normalizedFile,
                      supportedModuleRoots);
                  addCandidate(
                      candidates,
                      new CandidateDocument(
                          normalizedFile,
                          path,
                          moduleRoot.map(SupportedModuleRoot::moduleId).orElse(null),
                          moduleRoot.map(SupportedModuleRoot::moduleOrder).orElse(UNSCOPED_DOCUMENT_ORDER),
                          discoverySource),
                      options);
                });
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult visitFileFailed(Path file, IOException exception) {
            return FileVisitResult.CONTINUE;
          }
        });
  }

  private void addCandidate(
      BoundedCandidateSet<CandidateDocument> candidates,
      CandidateDocument candidate,
      DocumentDiscoveryOptions options) {
    if (!isUserExcluded(candidate.sourcePath(), options)) {
      candidates.add(candidate);
    }
  }

  private boolean isUserExcluded(String path, DocumentDiscoveryOptions options) {
    return options.excludes().stream().anyMatch(pattern -> pattern.matches(path));
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

  private Optional<String> acceptedUserMarkdownFile(
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      Path file) {
    Path normalizedFile = file.toAbsolutePath().normalize();
    String fileName = normalizedFile.getFileName().toString();
    if (!(fileName.endsWith(".md") || fileName.endsWith(".markdown"))) {
      return Optional.empty();
    }
    return acceptedMarkdownFile(repositoryRoot, canonicalRepositoryRoot, file, false)
        .or(() -> {
          if (fileName.endsWith(".markdown")
              && safeRegularDocumentFile(repositoryRoot, canonicalRepositoryRoot, normalizedFile)) {
            return Optional.of(repositoryRelativePath(repositoryRoot, normalizedFile));
          }
          return Optional.empty();
        });
  }

  private boolean safeRegularDocumentFile(
      Path repositoryRoot,
      Path canonicalRepositoryRoot,
      Path normalizedFile) {
    return normalizedFile.startsWith(repositoryRoot)
        && !Files.isSymbolicLink(normalizedFile)
        && Files.isRegularFile(normalizedFile, LinkOption.NOFOLLOW_LINKS)
        && !hasSymbolicLinkSegment(repositoryRoot, normalizedFile)
        && ScanPathContainment.realPathUnderRoot(canonicalRepositoryRoot, normalizedFile).isPresent()
        && !isExcluded(repositoryRoot, normalizedFile);
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
      List<? extends BuildModule> modules) {
    List<SupportedModuleRoot> roots = new ArrayList<>();
    for (int index = 0; index < modules.size(); index++) {
      BuildModule module = modules.get(index);
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

  private AnalyzedDocument analyzedDocument(CandidateDocument candidate, DocumentStructure structure) {
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
      String titleKey = safeHeadingKey(heading.title());
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
      headingTitleById.put(heading.id(), safeHeadingKey(heading.title()));
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

  private String safeHeadingKey(String title) {
    String redacted = OutputRedactor.redact(title);
    return redacted.isBlank() ? "untitled" : redacted;
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

  private DocumentStructure documentStructure(
      CandidateDocument candidate,
      int remainingHeadings,
      int remainingChunks) {
    try {
      return structureExtractor.extract(
          candidate.normalizedPath(),
          candidate.sourcePath(),
          remainingHeadings,
          remainingChunks);
    } catch (IOException exception) {
      return DocumentStructure.empty();
    }
  }

  private OptionalLong documentSize(CandidateDocument candidate) {
    try {
      return OptionalLong.of(Files.readAttributes(
          candidate.normalizedPath(),
          BasicFileAttributes.class,
          LinkOption.NOFOLLOW_LINKS).size());
    } catch (IOException exception) {
      return OptionalLong.empty();
    }
  }

  private int remainingCount(int max, int accepted) {
    return Math.max(0, max - accepted);
  }

  private ScanDiagnostic documentCountCapDiagnostic() {
    return diagnostic(
        DIAGNOSTIC_DOCUMENT_COUNT_CAP,
        "Local Markdown document discovery reached the aggregate document count cap; "
            + "remaining document candidates were skipped.",
        limits.maxDocuments());
  }

  private ScanDiagnostic documentBytesCapDiagnostic() {
    return diagnostic(
        DIAGNOSTIC_DOCUMENT_BYTES_CAP,
        "Local Markdown document discovery reached the aggregate document byte cap; "
            + "remaining document candidates were skipped.",
        boundedCount(limits.maxTotalDocumentBytes()));
  }

  private ScanDiagnostic headingCountCapDiagnostic() {
    return diagnostic(
        DIAGNOSTIC_HEADING_COUNT_CAP,
        "Local Markdown structure extraction reached the aggregate heading cap; "
            + "additional headings were omitted.",
        limits.maxHeadings());
  }

  private ScanDiagnostic chunkCountCapDiagnostic() {
    return diagnostic(
        DIAGNOSTIC_CHUNK_COUNT_CAP,
        "Local Markdown structure extraction reached the aggregate chunk cap; "
            + "additional chunks were omitted.",
        limits.maxChunks());
  }

  private ScanDiagnostic diagnostic(String code, String message, int count) {
    return new ScanDiagnostic(
        "scan_diagnostic:documents:" + code,
        DIAGNOSTIC_SEVERITY_WARNING,
        code,
        DIAGNOSTIC_CATEGORY_DOCUMENTS,
        message,
        null,
        count);
  }

  private int boundedCount(long value) {
    return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
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
