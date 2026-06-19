package io.github.dondindondev.agentprojectmemory.ai;

import java.util.Optional;

public enum AiPresentationProviderMode {
  MOCK_NO_NETWORK("mock_no_network");

  private final String cliValue;

  AiPresentationProviderMode(String cliValue) {
    this.cliValue = cliValue;
  }

  public String cliValue() {
    return cliValue;
  }

  public static Optional<AiPresentationProviderMode> fromCliValue(String value) {
    if (value == null) {
      return Optional.empty();
    }
    for (AiPresentationProviderMode mode : values()) {
      if (mode.cliValue.equals(value)) {
        return Optional.of(mode);
      }
    }
    return Optional.empty();
  }
}
