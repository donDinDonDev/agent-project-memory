package io.github.dondindondev.agentprojectmemory.ai;

public interface AiPresentationProvider {
  AiPresentationProviderMode providerMode();

  String renderBrief(AiPresentationInput input);
}
