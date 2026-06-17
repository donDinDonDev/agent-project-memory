package io.github.dondindondev.agentprojectmemory.ingestion.adapter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;

final class StableAdapterIds {
  private StableAdapterIds() {
  }

  static String sourceDocumentId(
      AdapterIdentity adapterIdentity,
      AdapterImportMode importMode,
      String sourceType,
      String sourceIdentity) {
    return "source-document:sha256:" + sha256(
        Objects.requireNonNull(adapterIdentity, "adapter identity").name(),
        adapterIdentity.version(),
        Objects.requireNonNull(importMode, "import mode").contractValue(),
        requiredText(sourceType, "source type"),
        requiredText(sourceIdentity, "source identity"));
  }

  static String provenanceId(
      AdapterIdentity adapterIdentity,
      AdapterImportMode importMode,
      String sourceType,
      String sourceIdentity,
      String contentHash) {
    return "source-provenance:sha256:" + sha256(
        Objects.requireNonNull(adapterIdentity, "adapter identity").name(),
        adapterIdentity.version(),
        Objects.requireNonNull(importMode, "import mode").contractValue(),
        requiredText(sourceType, "source type"),
        requiredText(sourceIdentity, "source identity"),
        requiredText(contentHash, "content hash"));
  }

  static String requiredText(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " is required");
    }
    return value.trim();
  }

  static String requiredKnownText(String value, String fieldName, List<String> allowedValues) {
    String normalized = requiredText(value, fieldName);
    if (!allowedValues.contains(normalized)) {
      throw new IllegalArgumentException(fieldName + " must be one of " + allowedValues);
    }
    return normalized;
  }

  static String optionalDisplayText(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }

  static List<String> requiredTextList(List<String> values, String fieldName) {
    if (values == null || values.isEmpty()) {
      throw new IllegalArgumentException(fieldName + " are required");
    }
    List<String> normalized = values.stream()
        .map(value -> requiredText(value, fieldName + " item"))
        .toList();
    if (normalized.isEmpty()) {
      throw new IllegalArgumentException(fieldName + " are required");
    }
    return List.copyOf(normalized);
  }

  private static String sha256(String... parts) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      for (String part : parts) {
        byte[] encoded = part.getBytes(StandardCharsets.UTF_8);
        digest.update(Integer.toString(encoded.length).getBytes(StandardCharsets.UTF_8));
        digest.update((byte) ':');
        digest.update(encoded);
      }
      return HexFormat.of().formatHex(digest.digest());
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException("SHA-256 is unavailable", ex);
    }
  }
}
