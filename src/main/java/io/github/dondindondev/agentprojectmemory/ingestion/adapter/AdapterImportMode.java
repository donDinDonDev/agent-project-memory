package io.github.dondindondev.agentprojectmemory.ingestion.adapter;

public enum AdapterImportMode {
  LOCAL_EXPORT("local_export");

  private final String contractValue;

  AdapterImportMode(String contractValue) {
    this.contractValue = contractValue;
  }

  public String contractValue() {
    return contractValue;
  }
}
