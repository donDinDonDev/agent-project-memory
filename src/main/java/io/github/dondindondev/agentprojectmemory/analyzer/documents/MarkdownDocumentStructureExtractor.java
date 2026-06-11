package io.github.dondindondev.agentprojectmemory.analyzer.documents;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class MarkdownDocumentStructureExtractor {
  private static final int MAX_HEADING_TITLE_LENGTH = 120;
  private static final int MAX_ANCHOR_BASE_LENGTH = 120;
  private static final int MAX_CHUNK_LINES = 80;
  private static final int MAX_CHUNK_UTF8_BYTES = 12 * 1024;
  private static final String TRUNCATED_SUFFIX = "...";
  private static final String CONTENT_STATUS_NOT_SERIALIZED = "not_serialized";

  DocumentStructure extract(Path markdownFile, String sourcePath) throws IOException {
    return extract(markdownFile, sourcePath, Integer.MAX_VALUE, Integer.MAX_VALUE);
  }

  DocumentStructure extract(
      Path markdownFile,
      String sourcePath,
      int maxHeadings,
      int maxChunks) throws IOException {
    List<DocumentHeadingFact> headings = new ArrayList<>();
    List<DocumentChunkFact> chunks = new ArrayList<>();
    Map<String, Integer> titleOccurrences = new LinkedHashMap<>();
    Map<String, Integer> anchorOccurrences = new LinkedHashMap<>();
    ExtractionBudget budget = new ExtractionBudget(maxHeadings, maxChunks);
    ChunkState chunk = new ChunkState(sourcePath, budget);
    FenceState fence = FenceState.none();

    try (BufferedReader reader = Files.newBufferedReader(markdownFile, StandardCharsets.UTF_8)) {
      String line;
      int lineNumber = 0;
      while ((line = reader.readLine()) != null) {
        lineNumber++;
        HeadingCandidate heading = fence.inFence() ? null : atxHeading(line);
        if (heading != null) {
          chunk.closeBefore(lineNumber, chunks);
          DocumentHeadingFact headingFact = null;
          if (budget.canAddHeading(headings)) {
            headingFact = headingFact(
                sourcePath,
                heading,
                lineNumber,
                titleOccurrences,
                anchorOccurrences);
            headings.add(headingFact);
          } else {
            budget.markHeadingCapReached();
          }
          chunk.start(lineNumber, headingFact == null ? null : headingFact.id());
        } else if (!chunk.started()) {
          chunk.start(lineNumber, null);
        }
        chunk.addLine(lineNumber, line, chunks);
        fence = fence.next(line);
      }
    }

    chunk.close(chunks);
    return new DocumentStructure(
        headings,
        chunks,
        budget.headingCapReached(),
        budget.chunkCapReached());
  }

  private DocumentHeadingFact headingFact(
      String sourcePath,
      HeadingCandidate heading,
      int lineNumber,
      Map<String, Integer> titleOccurrences,
      Map<String, Integer> anchorOccurrences) {
    String title = boundedTitle(normalizedInlineText(heading.title()));
    String titleKey = title.isBlank() ? "untitled" : title;
    int titleOccurrence = titleOccurrences.merge(titleKey, 1, Integer::sum);
    String id = "document_heading:"
        + DocumentDiscoveryAnalyzer.idKey(sourcePath)
        + ":heading:"
        + DocumentDiscoveryAnalyzer.idKey(titleKey)
        + ":occ:"
        + zeroPadded(titleOccurrence);

    String anchorBase = anchorBase(title);
    String anchor = null;
    if (anchorBase != null) {
      int anchorOccurrence = anchorOccurrences.merge(anchorBase, 1, Integer::sum);
      anchor = anchorOccurrence == 1 ? anchorBase : anchorBase + "-" + anchorOccurrence;
    }

    return new DocumentHeadingFact(
        id,
        heading.level(),
        title,
        anchor,
        lineNumber,
        lineNumber,
        List.of());
  }

  private HeadingCandidate atxHeading(String line) {
    int index = 0;
    int spaces = 0;
    while (index < line.length() && line.charAt(index) == ' ') {
      spaces++;
      index++;
    }
    if (spaces > 3 || index >= line.length()) {
      return null;
    }

    int level = 0;
    while (index < line.length() && line.charAt(index) == '#') {
      level++;
      index++;
    }
    if (level < 1 || level > 6) {
      return null;
    }
    if (index < line.length() && line.charAt(index) != ' ' && line.charAt(index) != '\t') {
      return null;
    }

    String title = index >= line.length() ? "" : line.substring(index + 1);
    title = stripClosingSequence(title.strip());
    return new HeadingCandidate(level, title);
  }

  private String stripClosingSequence(String title) {
    if (title.isEmpty()) {
      return title;
    }

    int end = title.length() - 1;
    while (end >= 0 && (title.charAt(end) == ' ' || title.charAt(end) == '\t')) {
      end--;
    }
    int hashEnd = end;
    while (end >= 0 && title.charAt(end) == '#') {
      end--;
    }
    if (hashEnd == end) {
      return title;
    }
    if (end < 0) {
      return "";
    }
    if (title.charAt(end) == ' ' || title.charAt(end) == '\t') {
      return title.substring(0, end).strip();
    }
    return title;
  }

  private String normalizedInlineText(String value) {
    StringBuilder normalized = new StringBuilder();
    boolean pendingSpace = false;
    for (int index = 0; index < value.length();) {
      int codePoint = value.codePointAt(index);
      if (Character.isWhitespace(codePoint)) {
        pendingSpace = normalized.length() > 0;
      } else {
        if (pendingSpace) {
          normalized.append(' ');
          pendingSpace = false;
        }
        appendNormalizedCodePoint(normalized, codePoint);
      }
      index += Character.charCount(codePoint);
    }
    return normalized.toString();
  }

  private void appendNormalizedCodePoint(StringBuilder normalized, int codePoint) {
    if (Character.isISOControl(codePoint) || isBidirectionalControl(codePoint)) {
      normalized.append(String.format(Locale.ROOT, "\\u%04X", codePoint));
      return;
    }
    normalized.appendCodePoint(codePoint);
  }

  private boolean isBidirectionalControl(int codePoint) {
    return (codePoint >= 0x202A && codePoint <= 0x202E)
        || (codePoint >= 0x2066 && codePoint <= 0x2069);
  }

  private String boundedTitle(String title) {
    if (title.length() <= MAX_HEADING_TITLE_LENGTH) {
      return title;
    }
    int end = MAX_HEADING_TITLE_LENGTH;
    if (Character.isHighSurrogate(title.charAt(end - 1))) {
      end--;
    }
    return title.substring(0, end) + TRUNCATED_SUFFIX;
  }

  private String anchorBase(String title) {
    if (title.isBlank()) {
      return null;
    }

    String lower = title.toLowerCase(Locale.ROOT);
    StringBuilder anchor = new StringBuilder();
    boolean pendingSeparator = false;
    for (int index = 0; index < lower.length();) {
      int codePoint = lower.codePointAt(index);
      if (Character.isLetterOrDigit(codePoint)) {
        if (pendingSeparator && anchor.length() > 0) {
          anchor.append('-');
        }
        anchor.appendCodePoint(codePoint);
        pendingSeparator = false;
      } else if (anchor.length() > 0) {
        pendingSeparator = true;
      }
      index += Character.charCount(codePoint);
      if (anchor.length() >= MAX_ANCHOR_BASE_LENGTH) {
        break;
      }
    }

    int end = anchor.length();
    while (end > 0 && anchor.charAt(end - 1) == '-') {
      end--;
    }
    if (end == 0) {
      return null;
    }
    return anchor.substring(0, end);
  }

  private String chunkId(String sourcePath, int chunkOrdinal) {
    return "document_chunk:"
        + DocumentDiscoveryAnalyzer.idKey(sourcePath)
        + ":chunk:"
        + zeroPadded(chunkOrdinal);
  }

  private String zeroPadded(int value) {
    return String.format(Locale.ROOT, "%06d", value);
  }

  private static final class ExtractionBudget {
    private final int maxHeadings;
    private final int maxChunks;
    private boolean headingCapReached;
    private boolean chunkCapReached;

    private ExtractionBudget(int maxHeadings, int maxChunks) {
      this.maxHeadings = Math.max(0, maxHeadings);
      this.maxChunks = Math.max(0, maxChunks);
    }

    private boolean canAddHeading(List<DocumentHeadingFact> headings) {
      return headings.size() < maxHeadings;
    }

    private boolean canAddChunk(List<DocumentChunkFact> chunks) {
      return chunks.size() < maxChunks;
    }

    private void markHeadingCapReached() {
      headingCapReached = true;
    }

    private void markChunkCapReached() {
      chunkCapReached = true;
    }

    private boolean headingCapReached() {
      return headingCapReached;
    }

    private boolean chunkCapReached() {
      return chunkCapReached;
    }
  }

  private final class ChunkState {
    private final String sourcePath;
    private final ExtractionBudget budget;
    private int startLine;
    private int endLine;
    private String headingId;
    private int lineCount;
    private int utf8Bytes;
    private int nextOrdinal = 1;

    private ChunkState(String sourcePath, ExtractionBudget budget) {
      this.sourcePath = sourcePath;
      this.budget = budget;
    }

    private boolean started() {
      return startLine > 0;
    }

    private void start(int lineNumber, String currentHeadingId) {
      startLine = lineNumber;
      endLine = lineNumber - 1;
      headingId = currentHeadingId;
      lineCount = 0;
      utf8Bytes = 0;
    }

    private void addLine(int lineNumber, String line, List<DocumentChunkFact> chunks) {
      int lineBytes = line.getBytes(StandardCharsets.UTF_8).length + 1;
      if (lineCount > 0
          && (lineCount + 1 > MAX_CHUNK_LINES
              || utf8Bytes + lineBytes > MAX_CHUNK_UTF8_BYTES)) {
        close(chunks);
        start(lineNumber, headingId);
      }
      endLine = lineNumber;
      lineCount++;
      utf8Bytes += lineBytes;
    }

    private void closeBefore(int lineNumber, List<DocumentChunkFact> chunks) {
      if (started() && endLine >= startLine && endLine < lineNumber) {
        close(chunks);
      }
    }

    private void close(List<DocumentChunkFact> chunks) {
      if (!started() || endLine < startLine) {
        return;
      }
      if (!budget.canAddChunk(chunks)) {
        budget.markChunkCapReached();
        startLine = 0;
        endLine = 0;
        headingId = null;
        lineCount = 0;
        utf8Bytes = 0;
        return;
      }
      chunks.add(new DocumentChunkFact(
          chunkId(sourcePath, nextOrdinal),
          headingId,
          startLine,
          endLine,
          CONTENT_STATUS_NOT_SERIALIZED,
          List.of()));
      nextOrdinal++;
      startLine = 0;
      endLine = 0;
      headingId = null;
      lineCount = 0;
      utf8Bytes = 0;
    }
  }

  private record HeadingCandidate(int level, String title) {
  }

  private record FenceState(boolean inFence, char marker, int length) {
    private static FenceState none() {
      return new FenceState(false, '\0', 0);
    }

    private FenceState next(String line) {
      FenceMarker current = fenceMarker(line);
      if (current == null) {
        return this;
      }
      if (!inFence) {
        return new FenceState(true, current.marker(), current.length());
      }
      if (current.marker() == marker && current.length() >= length && current.closing()) {
        return none();
      }
      return this;
    }

    private FenceMarker fenceMarker(String line) {
      int index = 0;
      int spaces = 0;
      while (index < line.length() && line.charAt(index) == ' ') {
        spaces++;
        index++;
      }
      if (spaces > 3 || index >= line.length()) {
        return null;
      }

      char candidate = line.charAt(index);
      if (candidate != '`' && candidate != '~') {
        return null;
      }
      int markerLength = 0;
      while (index < line.length() && line.charAt(index) == candidate) {
        markerLength++;
        index++;
      }
      if (markerLength < 3) {
        return null;
      }

      boolean closing = true;
      for (int rest = index; rest < line.length(); rest++) {
        char value = line.charAt(rest);
        if (value != ' ' && value != '\t') {
          closing = false;
          break;
        }
      }
      return new FenceMarker(candidate, markerLength, closing);
    }
  }

  private record FenceMarker(char marker, int length, boolean closing) {
  }
}
