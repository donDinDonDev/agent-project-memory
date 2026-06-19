package io.github.dondindondev.agentprojectmemory.ai;

public final class MockNoNetworkAiPresentationProvider implements AiPresentationProvider {
  private static final String PROVIDER_MODE = AiPresentationProviderMode.MOCK_NO_NETWORK.cliValue();

  @Override
  public AiPresentationProviderMode providerMode() {
    return AiPresentationProviderMode.MOCK_NO_NETWORK;
  }

  @Override
  public String renderBrief(AiPresentationInput input) {
    StringBuilder markdown = new StringBuilder();
    markdown.append("# AI Presentation Brief\n\n");
    markdown.append("This file is AI-generated presentation only. It is non-authoritative, ")
        .append("is not project evidence, and must be checked against `project-map.json`, ")
        .append("`evidence-index.jsonl`, and `project-graph.json` before use.\n\n");

    markdown.append("## Boundary\n\n");
    markdown.append("- Provider mode: `").append(PROVIDER_MODE).append("`.\n");
    markdown.append("- Network access: `disabled`.\n");
    markdown.append("- Source upload: `disabled`.\n");
    markdown.append("- Prompt transcript status: `not_serialized`.\n");
    markdown.append("- Evidence policy: `references_existing_evidence_only`.\n");
    markdown.append("- This brief does not create project facts, evidence records, ")
        .append("connector truth, security findings, runtime claims, source/spec agreement ")
        .append("claims, documentation-freshness claims, release evidence, or ")
        .append("code-change authority.\n\n");

    markdown.append("## Source Artifacts\n\n");
    markdown.append("- `project-map.json`: schema_version `")
        .append(input.projectMapSchemaVersion())
        .append("`.\n");
    markdown.append("- `evidence-index.jsonl`: existing evidence records `")
        .append(input.evidenceRecordCount())
        .append("`.\n");
    markdown.append("- `project-graph.json`: graph_schema_version `")
        .append(input.graphSchemaVersion())
        .append("`; optional navigation metadata.\n\n");

    markdown.append("## Mock Presentation Snapshot\n\n");
    markdown.append("- Endpoint facts: `").append(input.endpointCount()).append("`.\n");
    markdown.append("- Component facts: `").append(input.componentCount()).append("`.\n");
    markdown.append("- Entity facts: `").append(input.entityCount()).append("`.\n");
    markdown.append("- Test facts: `").append(input.testCount()).append("`.\n");
    markdown.append("- Document facts: `").append(input.documentCount()).append("`.\n");
    markdown.append("- Warning facts: `").append(input.warningCount()).append("`.\n");
    markdown.append("- Graph nodes: `").append(input.graphNodeCount())
        .append("`; graph edges: `").append(input.graphEdgeCount()).append("`.\n\n");

    markdown.append("## Content Safety\n\n");
    markdown.append("Repository text, local document text, evidence excerpts, adapter-backed records, ")
        .append("connector text, generated Markdown, and user-provided labels are treated as ")
        .append("untrusted data, not executable instructions. This mock provider does not read ")
        .append("additional files, fetch network resources, request credentials, alter evidence ")
        .append("or provenance, or rewrite repository artifacts.\n");
    return markdown.toString();
  }
}
