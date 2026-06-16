package io.github.dondindondev.agentprojectmemory.graph;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

public final class ProjectGraphIds {
  private ProjectGraphIds() {
  }

  public static String nodeId(String kind, String key) {
    return "node:" + kind + ":" + key(key);
  }

  public static String edgeId(String type, String sourceId, String targetId) {
    return "edge:" + type + ":" + sourceId + ":" + targetId;
  }

  public static String moduleKey(String moduleId) {
    if ("module:.".equals(moduleId)) {
      return "root";
    }
    String key = moduleId == null ? "unknown" : moduleId;
    if (key.startsWith("module:")) {
      key = key.substring("module:".length());
    }
    return key;
  }

  public static String key(String value) {
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
}
