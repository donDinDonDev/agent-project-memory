package io.github.dondindondev.agentprojectmemory.ingestion.adapter;

public enum SourceDocumentContentStatus {
  NOT_SERIALIZED("not_serialized");

  private final String contractValue;

  SourceDocumentContentStatus(String contractValue) {
    this.contractValue = contractValue;
  }

  public String contractValue() {
    return contractValue;
  }
}
