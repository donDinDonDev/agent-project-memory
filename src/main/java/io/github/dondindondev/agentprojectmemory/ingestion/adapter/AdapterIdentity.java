package io.github.dondindondev.agentprojectmemory.ingestion.adapter;

public record AdapterIdentity(String name, String version) {
  public AdapterIdentity {
    name = StableAdapterIds.requiredText(name, "adapter name");
    version = StableAdapterIds.requiredText(version, "adapter version");
  }
}
