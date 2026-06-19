package io.github.dondindondev.agentprojectmemory.ai;

import java.util.Objects;
import java.util.Optional;

public record AiPresentationOptions(AiPresentationProviderMode providerMode) {
  public static AiPresentationOptions disabled() {
    return new AiPresentationOptions(null);
  }

  public static AiPresentationOptions enabled(AiPresentationProviderMode providerMode) {
    return new AiPresentationOptions(Objects.requireNonNull(providerMode, "providerMode"));
  }

  public static Optional<AiPresentationOptions> fromCliValue(String value) {
    return AiPresentationProviderMode.fromCliValue(value).map(AiPresentationOptions::enabled);
  }

  public boolean enabled() {
    return providerMode != null;
  }

  public String providerModeValue() {
    return enabled() ? providerMode.cliValue() : "none";
  }
}
