package io.github.dondindondev.agentprojectmemory.analyzer.documents;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class DocumentReconciliationAnalyzer {
  private static final String ANALYSIS_ANALYZED = "analyzed";
  private static final String ANALYSIS_NOT_DETECTED = "not_detected";
  private static final String DOCUMENT_SOURCE_TYPE = "document";
  private static final String LOW_CONFIDENCE = "low";
  private static final String STATUS_UNCERTAIN_SIGNAL = "uncertain_signal";
  private static final String SIGNAL_DOCUMENT_ONLY_ENDPOINT_MENTION =
      "document_only_endpoint_mention";
  private static final String SIGNAL_SOURCE_API_WITHOUT_DOCUMENT_MENTION =
      "source_api_without_document_mention";
  private static final String SIGNAL_DOCUMENT_ONLY_MODULE_REFERENCE =
      "document_only_module_reference";
  private static final String SIGNAL_MODULE_WITHOUT_DOCUMENT_MENTION =
      "module_without_document_mention";
  private static final String SOURCE_FACT_KIND_MAVEN_MODULE = "maven_module";
  private static final String SUBJECT_KIND_ENDPOINT_LIKE_PATH = "endpoint_like_path";
  private static final String SUBJECT_KIND_API_PATH = "api_path";
  private static final String SUBJECT_KIND_MODULE_REFERENCE = "module_reference";
  private static final String SUBJECT_KIND_MAVEN_MODULE = "maven_module";
  private static final String MATCH_BASIS_ENDPOINT_LIKE_PATH_TOKEN =
      "bounded_endpoint_like_path_token";
  private static final String MATCH_BASIS_SOURCE_API_PATH_TOKEN =
      "bounded_source_api_path_token";
  private static final String MATCH_BASIS_MODULE_PATH_TOKEN =
      "bounded_module_path_token";
  private static final String MATCH_BASIS_MODULE_NAME_TOKEN =
      "bounded_module_name_token";
  private static final String UNCERTAINTY_DOCUMENT_ENDPOINT_NOT_MATCHED =
      "document_mention_not_matched_to_source_backed_api_fact";
  private static final String UNCERTAINTY_SOURCE_API_NOT_MATCHED =
      "source_api_fact_not_matched_to_default_scope_document";
  private static final String UNCERTAINTY_DOCUMENT_MODULE_NOT_MATCHED =
      "document_module_reference_not_matched_to_module_fact";
  private static final String UNCERTAINTY_MODULE_NOT_MATCHED =
      "module_fact_not_matched_to_default_scope_document";
  private static final int MAX_TOKEN_LENGTH = 120;
  private static final int MAX_MODULE_NAME_LENGTH = 80;
  private static final int MAX_SCANNED_LINE_LENGTH = 4_096;
  private static final int UNOWNED_MODULE_ORDER = Integer.MAX_VALUE;
  private static final Set<String> FILE_LIKE_EXTENSIONS = Set.of(
      ".md",
      ".markdown",
      ".java",
      ".kt",
      ".xml",
      ".json",
      ".yaml",
      ".yml",
      ".properties",
      ".txt",
      ".html");
  private static final Set<String> FILESYSTEM_ENDPOINT_PREFIXES = Set.of(
      "/Users/",
      "/private/",
      "/tmp/",
      "/var/",
      "/home/",
      "/etc/",
      "/usr/",
      "/opt/");
  private static final Set<String> MODULE_PATH_FIRST_SEGMENT_EXCLUSIONS = Set.of(
      "docs",
      "doc",
      "adr",
      "adrs",
      "src",
      "target",
      "build",
      "out",
      "dist",
      "node_modules",
      ".project-memory",
      ".git");
  private static final Set<String> MODULE_CONTEXT_WORDS = Set.of(
      "module",
      "modules",
      "service",
      "services",
      "artifact",
      "artifacts");
  private static final Comparator<DocumentReconciliationSignal> SIGNAL_ORDER = Comparator
      .comparing(DocumentReconciliationSignal::signal)
      .thenComparingInt(DocumentReconciliationSignal::moduleOrder)
      .thenComparing(DocumentReconciliationSignal::subjectKind)
      .thenComparing(DocumentReconciliationSignal::subjectName)
      .thenComparing(signal -> nullSafe(signal.documentPath()))
      .thenComparing(DocumentReconciliationSignal::id);

  public DocumentReconciliationAnalysis analyze(
      Path repositoryRoot,
      DocumentDiscoveryAnalysis documentDiscoveryAnalysis,
      List<DocumentSourceApiFact> sourceApiFacts,
      List<DocumentSourceModuleFact> sourceModuleFacts) {
    Objects.requireNonNull(repositoryRoot, "repositoryRoot");
    Objects.requireNonNull(documentDiscoveryAnalysis, "documentDiscoveryAnalysis");
    Objects.requireNonNull(sourceApiFacts, "sourceApiFacts");
    Objects.requireNonNull(sourceModuleFacts, "sourceModuleFacts");

    if (documentDiscoveryAnalysis.documents().isEmpty()) {
      return notDetected();
    }

    List<DocumentSourceApiFact> apiFacts = List.copyOf(sourceApiFacts);
    List<DocumentSourceModuleFact> moduleFacts = childModuleFacts(sourceModuleFacts);
    Map<String, List<DocumentSourceApiFact>> sourceApisByPath = sourceApisByPath(apiFacts);
    Map<String, List<DocumentSourceModuleFact>> sourceModulesByToken = sourceModulesByToken(moduleFacts);
    Set<String> sourceModuleNames = sourceModuleNames(moduleFacts);
    List<DocumentMention> mentions = documentMentions(
        repositoryRoot,
        documentDiscoveryAnalysis.documents(),
        sourceModuleNames);

    if (apiFacts.isEmpty() && moduleFacts.isEmpty() && mentions.isEmpty()) {
      return notDetected();
    }

    List<DocumentReconciliationSignal> signals = new ArrayList<>();
    List<DocumentEvidence> evidence = new ArrayList<>();
    LinkedHashSet<String> matchedApiIds = new LinkedHashSet<>();
    LinkedHashSet<String> matchedModuleIds = new LinkedHashSet<>();

    for (DocumentMention mention : mentions) {
      if (MentionKind.ENDPOINT.equals(mention.kind())) {
        List<DocumentSourceApiFact> matchedApis = sourceApisByPath.get(mention.normalizedToken());
        if (matchedApis == null || matchedApis.isEmpty()) {
          DocumentEvidence mentionEvidence = mentionEvidence(mention);
          evidence.add(mentionEvidence);
          signals.add(documentOnlySignal(
              SIGNAL_DOCUMENT_ONLY_ENDPOINT_MENTION,
              SUBJECT_KIND_ENDPOINT_LIKE_PATH,
              UNCERTAINTY_DOCUMENT_ENDPOINT_NOT_MATCHED,
              mention,
              mentionEvidence));
        } else {
          matchedApis.stream()
              .map(DocumentSourceApiFact::id)
              .forEach(matchedApiIds::add);
        }
      } else {
        List<DocumentSourceModuleFact> matchedModules = sourceModulesByToken.get(mention.normalizedToken());
        if (matchedModules == null || matchedModules.isEmpty()) {
          DocumentEvidence mentionEvidence = mentionEvidence(mention);
          evidence.add(mentionEvidence);
          signals.add(documentOnlySignal(
              SIGNAL_DOCUMENT_ONLY_MODULE_REFERENCE,
              SUBJECT_KIND_MODULE_REFERENCE,
              UNCERTAINTY_DOCUMENT_MODULE_NOT_MATCHED,
              mention,
              mentionEvidence));
        } else {
          matchedModules.stream()
              .map(DocumentSourceModuleFact::id)
              .forEach(matchedModuleIds::add);
        }
      }
    }

    for (DocumentSourceApiFact sourceApi : apiFacts) {
      if (!matchedApiIds.contains(sourceApi.id())) {
        signals.add(sourceOnlyApiSignal(sourceApi));
      }
    }
    for (DocumentSourceModuleFact sourceModule : moduleFacts) {
      if (!matchedModuleIds.contains(sourceModule.id())) {
        signals.add(sourceOnlyModuleSignal(sourceModule));
      }
    }

    return new DocumentReconciliationAnalysis(
        ANALYSIS_ANALYZED,
        signals.stream().sorted(SIGNAL_ORDER).toList(),
        evidence);
  }

  private DocumentReconciliationAnalysis notDetected() {
    return new DocumentReconciliationAnalysis(ANALYSIS_NOT_DETECTED, List.of(), List.of());
  }

  private List<DocumentSourceModuleFact> childModuleFacts(List<DocumentSourceModuleFact> sourceModuleFacts) {
    return sourceModuleFacts.stream()
        .filter(module -> module.modulePath() != null)
        .filter(module -> !".".equals(module.modulePath()))
        .toList();
  }

  private Map<String, List<DocumentSourceApiFact>> sourceApisByPath(
      List<DocumentSourceApiFact> sourceApiFacts) {
    Map<String, List<DocumentSourceApiFact>> sourceApisByPath = new LinkedHashMap<>();
    for (DocumentSourceApiFact sourceApi : sourceApiFacts) {
      for (String pathToken : sourceApi.pathTokens()) {
        normalizeEndpointToken(pathToken).ifPresent(normalized ->
            sourceApisByPath.computeIfAbsent(normalized, ignored -> new ArrayList<>()).add(sourceApi));
      }
    }
    return sourceApisByPath;
  }

  private Map<String, List<DocumentSourceModuleFact>> sourceModulesByToken(
      List<DocumentSourceModuleFact> sourceModuleFacts) {
    Map<String, List<DocumentSourceModuleFact>> sourceModulesByToken = new LinkedHashMap<>();
    for (DocumentSourceModuleFact sourceModule : sourceModuleFacts) {
      for (String token : sourceModuleTokens(sourceModule)) {
        sourceModulesByToken.computeIfAbsent(token, ignored -> new ArrayList<>()).add(sourceModule);
      }
    }
    return sourceModulesByToken;
  }

  private Set<String> sourceModuleNames(List<DocumentSourceModuleFact> sourceModuleFacts) {
    LinkedHashSet<String> names = new LinkedHashSet<>();
    for (DocumentSourceModuleFact sourceModule : sourceModuleFacts) {
      moduleName(sourceModule.modulePath()).ifPresent(names::add);
    }
    return names;
  }

  private List<String> sourceModuleTokens(DocumentSourceModuleFact sourceModule) {
    LinkedHashSet<String> tokens = new LinkedHashSet<>();
    normalizeModulePathToken(sourceModule.modulePath()).ifPresent(tokens::add);
    moduleName(sourceModule.modulePath()).ifPresent(tokens::add);
    return List.copyOf(tokens);
  }

  private Optional<String> moduleName(String modulePath) {
    if (modulePath == null || modulePath.isBlank() || ".".equals(modulePath)) {
      return Optional.empty();
    }
    String normalizedPath = modulePath.replace('\\', '/');
    int lastSlash = normalizedPath.lastIndexOf('/');
    String candidate = lastSlash >= 0 ? normalizedPath.substring(lastSlash + 1) : normalizedPath;
    return normalizeModuleNameToken(candidate);
  }

  private List<DocumentMention> documentMentions(
      Path repositoryRoot,
      List<DocumentFileFact> documents,
      Set<String> sourceModuleNames) {
    Path normalizedRepositoryRoot = repositoryRoot.toAbsolutePath().normalize();
    List<DocumentMention> mentions = new ArrayList<>();
    for (DocumentFileFact document : documents) {
      Path documentPath = normalizedRepositoryRoot.resolve(document.path()).normalize();
      if (!documentPath.startsWith(normalizedRepositoryRoot)
          || !Files.isRegularFile(documentPath, LinkOption.NOFOLLOW_LINKS)) {
        continue;
      }
      try (BufferedReader reader = Files.newBufferedReader(documentPath, StandardCharsets.UTF_8)) {
        Map<MentionKey, DocumentMention> uniqueDocumentMentions = new LinkedHashMap<>();
        String line;
        int lineNumber = 0;
        int ordinal = 0;
        while ((line = reader.readLine()) != null) {
          lineNumber++;
          String boundedLine = boundedLine(line);
          for (String token : endpointTokens(boundedLine)) {
            Optional<String> normalizedToken = normalizeEndpointToken(token);
            if (normalizedToken.isPresent()) {
              ordinal = addMention(
                  uniqueDocumentMentions,
                  document,
                  MentionKind.ENDPOINT,
                  token,
                  normalizedToken.get(),
                  MATCH_BASIS_ENDPOINT_LIKE_PATH_TOKEN,
                  lineNumber,
                  ordinal);
            }
          }
          for (ModuleToken moduleToken : moduleTokens(boundedLine, sourceModuleNames)) {
            ordinal = addMention(
                uniqueDocumentMentions,
                document,
                MentionKind.MODULE,
                moduleToken.token(),
                moduleToken.normalizedToken(),
                moduleToken.matchBasis(),
                lineNumber,
                ordinal);
          }
        }
        mentions.addAll(uniqueDocumentMentions.values());
      } catch (IOException exception) {
        // Reconciliation is an optional hint layer; unreadable documents keep inventory output intact.
      }
    }
    return mentions;
  }

  private int addMention(
      Map<MentionKey, DocumentMention> mentions,
      DocumentFileFact document,
      MentionKind kind,
      String token,
      String normalizedToken,
      String matchBasis,
      int lineNumber,
      int ordinal) {
    MentionKey key = new MentionKey(kind, normalizedToken);
    if (mentions.containsKey(key)) {
      return ordinal;
    }
    int nextOrdinal = ordinal + 1;
    mentions.put(
        key,
        new DocumentMention(
            document,
            kind,
            token,
            normalizedToken,
            matchBasis,
            lineNumber,
            chunkId(document, lineNumber),
            nextOrdinal));
    return nextOrdinal;
  }

  private List<String> endpointTokens(String line) {
    List<String> tokens = new ArrayList<>();
    int index = 0;
    while (index < line.length()) {
      int slash = line.indexOf('/', index);
      if (slash < 0) {
        break;
      }
      if (slash > 0 && isModulePathCharacter(line.charAt(slash - 1))) {
        index = slash + 1;
        continue;
      }
      int end = slash + 1;
      while (end < line.length() && isEndpointTokenCharacter(line.charAt(end))) {
        end++;
      }
      if (end > slash + 1) {
        String token = line.substring(slash, end);
        if (normalizeEndpointToken(token).isPresent()) {
          tokens.add(token);
        }
      }
      index = Math.max(end, slash + 1);
    }
    return tokens;
  }

  private List<ModuleToken> moduleTokens(String line, Set<String> sourceModuleNames) {
    List<ModuleToken> tokens = new ArrayList<>();
    tokens.addAll(modulePathTokens(line));
    tokens.addAll(contextualModuleNameTokens(line, sourceModuleNames));
    return tokens;
  }

  private List<ModuleToken> modulePathTokens(String line) {
    List<ModuleToken> tokens = new ArrayList<>();
    int index = 0;
    while (index < line.length()) {
      if (!isModulePathStart(line.charAt(index))) {
        index++;
        continue;
      }
      int end = index + 1;
      while (end < line.length() && isModulePathCharacter(line.charAt(end))) {
        end++;
      }
      String token = line.substring(index, end);
      normalizeModulePathToken(token)
          .ifPresent(normalized -> tokens.add(new ModuleToken(token, normalized, MATCH_BASIS_MODULE_PATH_TOKEN)));
      index = end;
    }
    return tokens;
  }

  private List<ModuleToken> contextualModuleNameTokens(String line, Set<String> sourceModuleNames) {
    List<ModuleToken> tokens = new ArrayList<>();
    List<WordSpan> words = wordSpans(line);
    for (int index = 0; index < words.size(); index++) {
      WordSpan word = words.get(index);
      String lowerWord = word.value().toLowerCase(Locale.ROOT);
      if (MODULE_CONTEXT_WORDS.contains(lowerWord)) {
        if (index + 1 < words.size()) {
          moduleNameToken(words.get(index + 1).value(), sourceModuleNames)
              .ifPresent(token -> tokens.add(token));
        }
        if (index > 0) {
          moduleNameToken(words.get(index - 1).value(), sourceModuleNames)
              .ifPresent(token -> tokens.add(token));
        }
      }
    }
    return tokens;
  }

  private Optional<ModuleToken> moduleNameToken(String value, Set<String> sourceModuleNames) {
    Optional<String> normalizedToken = normalizeModuleNameToken(value);
    if (normalizedToken.isEmpty()) {
      return Optional.empty();
    }
    String normalized = normalizedToken.get();
    if (!value.contains("-") && !value.contains("_") && !sourceModuleNames.contains(normalized)) {
      return Optional.empty();
    }
    return Optional.of(new ModuleToken(value, normalized, MATCH_BASIS_MODULE_NAME_TOKEN));
  }

  private List<WordSpan> wordSpans(String line) {
    List<WordSpan> words = new ArrayList<>();
    int index = 0;
    while (index < line.length()) {
      if (!isModuleNameCharacter(line.charAt(index))) {
        index++;
        continue;
      }
      int end = index + 1;
      while (end < line.length() && isModuleNameCharacter(line.charAt(end))) {
        end++;
      }
      words.add(new WordSpan(line.substring(index, end)));
      index = end;
    }
    return words;
  }

  private Optional<String> normalizeEndpointToken(String token) {
    if (token == null || token.length() < 2 || token.length() > MAX_TOKEN_LENGTH) {
      return Optional.empty();
    }
    if (!token.startsWith("/") || token.startsWith("//") || token.contains("..")) {
      return Optional.empty();
    }
    for (String prefix : FILESYSTEM_ENDPOINT_PREFIXES) {
      if (token.startsWith(prefix)) {
        return Optional.empty();
      }
    }
    String normalized = stripTrailingSlash(token);
    if (normalized.length() < 2 || !containsAsciiLetter(normalized)) {
      return Optional.empty();
    }
    if (isFileLikeToken(normalized)) {
      return Optional.empty();
    }
    return Optional.of(normalized);
  }

  private Optional<String> normalizeModulePathToken(String token) {
    if (token == null || token.length() < 3 || token.length() > MAX_TOKEN_LENGTH) {
      return Optional.empty();
    }
    String normalized = stripTrailingPunctuation(token.replace('\\', '/'));
    if (normalized.startsWith("/") || normalized.startsWith("//") || !normalized.contains("/")) {
      return Optional.empty();
    }
    if (normalized.contains("..") || !containsAsciiLetter(normalized) || isFileLikeToken(normalized)) {
      return Optional.empty();
    }
    String[] segments = normalized.split("/");
    if (segments.length < 2 || segments[0].isBlank()) {
      return Optional.empty();
    }
    if (MODULE_PATH_FIRST_SEGMENT_EXCLUSIONS.contains(segments[0].toLowerCase(Locale.ROOT))) {
      return Optional.empty();
    }
    for (String segment : segments) {
      if (segment.isBlank() || !isModulePathSegment(segment)) {
        return Optional.empty();
      }
    }
    return Optional.of(normalized.toLowerCase(Locale.ROOT));
  }

  private Optional<String> normalizeModuleNameToken(String token) {
    if (token == null || token.length() < 3 || token.length() > MAX_MODULE_NAME_LENGTH) {
      return Optional.empty();
    }
    String normalized = stripTrailingPunctuation(token);
    if (normalized.contains("/") || normalized.contains("..") || isFileLikeToken(normalized)) {
      return Optional.empty();
    }
    if (!containsAsciiLetter(normalized) || !isModulePathSegment(normalized)) {
      return Optional.empty();
    }
    return Optional.of(normalized.toLowerCase(Locale.ROOT));
  }

  private String stripTrailingSlash(String token) {
    String normalized = token;
    while (normalized.length() > 1 && normalized.endsWith("/")) {
      normalized = normalized.substring(0, normalized.length() - 1);
    }
    return normalized;
  }

  private String stripTrailingPunctuation(String token) {
    String normalized = token;
    while (!normalized.isEmpty()) {
      char last = normalized.charAt(normalized.length() - 1);
      if (last == '.' || last == ',' || last == ';' || last == ':') {
        normalized = normalized.substring(0, normalized.length() - 1);
      } else {
        break;
      }
    }
    return normalized;
  }

  private boolean isFileLikeToken(String token) {
    String lower = token.toLowerCase(Locale.ROOT);
    for (String extension : FILE_LIKE_EXTENSIONS) {
      if (lower.endsWith(extension)) {
        return true;
      }
    }
    return false;
  }

  private boolean isModulePathSegment(String segment) {
    for (int index = 0; index < segment.length(); index++) {
      char character = segment.charAt(index);
      if (!isModulePathCharacter(character) || character == '/') {
        return false;
      }
    }
    return true;
  }

  private boolean isEndpointTokenCharacter(char character) {
    return isAsciiLetterOrDigit(character)
        || character == '/'
        || character == '_'
        || character == '-'
        || character == '.'
        || character == '~'
        || character == '{'
        || character == '}';
  }

  private boolean isModulePathStart(char character) {
    return isAsciiLetterOrDigit(character) || character == '_' || character == '-' || character == '.';
  }

  private boolean isModulePathCharacter(char character) {
    return isAsciiLetterOrDigit(character)
        || character == '/'
        || character == '_'
        || character == '-'
        || character == '.';
  }

  private boolean isModuleNameCharacter(char character) {
    return isAsciiLetterOrDigit(character) || character == '_' || character == '-';
  }

  private boolean isAsciiLetterOrDigit(char character) {
    return (character >= 'A' && character <= 'Z')
        || (character >= 'a' && character <= 'z')
        || (character >= '0' && character <= '9');
  }

  private boolean containsAsciiLetter(String value) {
    for (int index = 0; index < value.length(); index++) {
      char character = value.charAt(index);
      if ((character >= 'A' && character <= 'Z') || (character >= 'a' && character <= 'z')) {
        return true;
      }
    }
    return false;
  }

  private String boundedLine(String line) {
    if (line.length() <= MAX_SCANNED_LINE_LENGTH) {
      return line;
    }
    return line.substring(0, MAX_SCANNED_LINE_LENGTH);
  }

  private String chunkId(DocumentFileFact document, int lineNumber) {
    for (DocumentChunkFact chunk : document.chunks()) {
      if (lineNumber >= chunk.lineStart() && lineNumber <= chunk.lineEnd()) {
        return chunk.id();
      }
    }
    return null;
  }

  private DocumentEvidence mentionEvidence(DocumentMention mention) {
    return new DocumentEvidence(
        "ev:"
            + DocumentDiscoveryAnalyzer.idKey(mention.document().path())
            + ":"
            + mention.lineNumber()
            + "-"
            + mention.lineNumber()
            + ":document:mention:"
            + DocumentDiscoveryAnalyzer.idKey(mention.token())
            + ":decl:"
            + zeroPadded(mention.ordinal()),
        DOCUMENT_SOURCE_TYPE,
        mention.document().path(),
        null,
        null,
        "mention:" + mention.token(),
        mention.lineNumber(),
        mention.lineNumber(),
        "mention token: " + mention.token(),
        LOW_CONFIDENCE);
  }

  private DocumentReconciliationSignal documentOnlySignal(
      String signal,
      String subjectKind,
      String uncertainty,
      DocumentMention mention,
      DocumentEvidence mentionEvidence) {
    return new DocumentReconciliationSignal(
        "document_reconciliation:"
            + signal
            + ":"
            + DocumentDiscoveryAnalyzer.idKey(mention.document().path())
            + ":"
            + DocumentDiscoveryAnalyzer.idKey(mention.token())
            + ":decl:"
            + zeroPadded(mention.ordinal()),
        mention.document().moduleOrder(),
        mention.document().moduleId(),
        signal,
        STATUS_UNCERTAIN_SIGNAL,
        mention.document().id(),
        mention.document().path(),
        mention.chunkId(),
        null,
        null,
        subjectKind,
        mention.token(),
        mention.matchBasis(),
        LOW_CONFIDENCE,
        uncertainty,
        List.of(mentionEvidence.id()));
  }

  private DocumentReconciliationSignal sourceOnlyApiSignal(DocumentSourceApiFact sourceApi) {
    return new DocumentReconciliationSignal(
        "document_reconciliation:"
            + SIGNAL_SOURCE_API_WITHOUT_DOCUMENT_MENTION
            + ":"
            + DocumentDiscoveryAnalyzer.idKey(sourceApi.id()),
        sourceApi.moduleOrder(),
        sourceApi.moduleId(),
        SIGNAL_SOURCE_API_WITHOUT_DOCUMENT_MENTION,
        STATUS_UNCERTAIN_SIGNAL,
        null,
        null,
        null,
        sourceApi.sourceFactKind(),
        sourceApi.id(),
        SUBJECT_KIND_API_PATH,
        sourceApi.subjectName(),
        MATCH_BASIS_SOURCE_API_PATH_TOKEN,
        LOW_CONFIDENCE,
        UNCERTAINTY_SOURCE_API_NOT_MATCHED,
        sourceApi.evidenceIds());
  }

  private DocumentReconciliationSignal sourceOnlyModuleSignal(DocumentSourceModuleFact sourceModule) {
    return new DocumentReconciliationSignal(
        "document_reconciliation:"
            + SIGNAL_MODULE_WITHOUT_DOCUMENT_MENTION
            + ":"
            + DocumentDiscoveryAnalyzer.idKey(sourceModule.id()),
        sourceModule.moduleOrder(),
        sourceModule.moduleId(),
        SIGNAL_MODULE_WITHOUT_DOCUMENT_MENTION,
        STATUS_UNCERTAIN_SIGNAL,
        null,
        null,
        null,
        SOURCE_FACT_KIND_MAVEN_MODULE,
        sourceModule.id(),
        SUBJECT_KIND_MAVEN_MODULE,
        sourceModule.modulePath(),
        MATCH_BASIS_MODULE_PATH_TOKEN,
        LOW_CONFIDENCE,
        UNCERTAINTY_MODULE_NOT_MATCHED,
        sourceModule.evidenceIds());
  }

  private String zeroPadded(int value) {
    return String.format(Locale.ROOT, "%06d", value);
  }

  private static String nullSafe(String value) {
    return value == null ? "" : value;
  }

  private enum MentionKind {
    ENDPOINT,
    MODULE
  }

  private record MentionKey(MentionKind kind, String normalizedToken) {
  }

  private record DocumentMention(
      DocumentFileFact document,
      MentionKind kind,
      String token,
      String normalizedToken,
      String matchBasis,
      int lineNumber,
      String chunkId,
      int ordinal) {
  }

  private record ModuleToken(String token, String normalizedToken, String matchBasis) {
  }

  private record WordSpan(String value) {
  }
}
