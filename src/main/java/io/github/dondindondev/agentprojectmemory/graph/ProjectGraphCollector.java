package io.github.dondindondev.agentprojectmemory.graph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class ProjectGraphCollector {
  private static final String PROJECT_GRAPH_FILE_NAME = "project-graph.json";
  private static final Comparator<GraphNode> NODE_ORDER = Comparator
      .comparing(GraphNode::kind)
      .thenComparing(GraphNode::id);
  private static final Comparator<GraphEdge> EDGE_ORDER = Comparator
      .comparing(GraphEdge::type)
      .thenComparing(GraphEdge::sourceId)
      .thenComparing(GraphEdge::targetId)
      .thenComparing(GraphEdge::id);
  private static final Comparator<GraphRelationStatus> RELATION_STATUS_ORDER = Comparator
      .comparing(GraphRelationStatus::relationFamily)
      .thenComparing(status -> nullSafe(status.sourceId()))
      .thenComparing(status -> nullSafe(status.targetId()))
      .thenComparing(GraphRelationStatus::id);
  private static final Comparator<GraphWarning> WARNING_ORDER = Comparator
      .comparing(GraphWarning::category)
      .thenComparing(warning -> sourceRefKey(warning.sourceRef()))
      .thenComparing(GraphWarning::id);

  private final ProjectGraphLimits limits;
  private final Map<String, GraphNode> nodes = new LinkedHashMap<>();
  private final Map<String, GraphEdge> edges = new LinkedHashMap<>();
  private final Map<String, GraphRelationStatus> relationStatuses = new LinkedHashMap<>();
  private final Map<String, GraphWarning> warnings = new LinkedHashMap<>();

  public ProjectGraphCollector() {
    this(ProjectGraphLimits.DEFAULT);
  }

  public ProjectGraphCollector(ProjectGraphLimits limits) {
    this.limits = Objects.requireNonNull(limits, "limits");
  }

  public void addNode(GraphNode node) {
    Objects.requireNonNull(node, "node");
    GraphNode existing = nodes.get(node.id());
    if (existing == null) {
      nodes.put(node.id(), node);
      return;
    }
    if (canMerge(existing, node)) {
      nodes.put(node.id(), merge(existing, node));
      return;
    }
    addWarning(duplicateWarning("nodes", node.id()));
  }

  public void addEdge(GraphEdge edge) {
    Objects.requireNonNull(edge, "edge");
    GraphEdge existing = edges.get(edge.id());
    if (existing == null) {
      edges.put(edge.id(), edge);
    }
  }

  public void addRelationStatus(GraphRelationStatus status) {
    Objects.requireNonNull(status, "status");
    GraphRelationStatus existing = relationStatuses.get(status.id());
    if (existing == null) {
      relationStatuses.put(status.id(), status);
      return;
    }
    if (!existing.equals(status)) {
      addWarning(duplicateWarning("relation_statuses", status.id()));
    }
  }

  public void addWarning(GraphWarning warning) {
    Objects.requireNonNull(warning, "warning");
    warnings.putIfAbsent(warning.id(), warning);
  }

  public ProjectGraph build() {
    return build("1.0");
  }

  public ProjectGraph build(String projectMapSchemaVersion) {
    List<GraphNode> selectedNodes = selectedNodes();
    Set<String> emittedNodeIds = selectedNodes.stream()
        .map(GraphNode::id)
        .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);
    List<GraphEdge> selectedEdges = selectedEdges(emittedNodeIds);
    List<GraphRelationStatus> selectedStatuses = selectedRelationStatuses(emittedNodeIds);
    List<GraphWarning> selectedWarnings = selectedWarnings(
        selectedNodes.size(),
        selectedEdges.size(),
        selectedStatuses.size(),
        emittedNodeIds);
    return new ProjectGraph(
        "1.0",
        projectMapSchemaVersion,
        "lightweight_relation_graph",
        List.of("project-map.json", "evidence-index.jsonl"),
        limits,
        selectedNodes.stream().sorted(NODE_ORDER).toList(),
        selectedEdges.stream().sorted(EDGE_ORDER).toList(),
        selectedStatuses.stream().sorted(RELATION_STATUS_ORDER).toList(),
        selectedWarnings.stream().sorted(WARNING_ORDER).toList());
  }

  private List<GraphNode> selectedNodes() {
    return nodes.values().stream()
        .sorted(Comparator
            .comparingInt(ProjectGraphCollector::nodePriority)
            .thenComparing(NODE_ORDER))
        .limit(limits.maxNodes())
        .toList();
  }

  private List<GraphEdge> selectedEdges(Set<String> emittedNodeIds) {
    return edges.values().stream()
        .filter(edge -> emittedNodeIds.contains(edge.sourceId()))
        .filter(edge -> emittedNodeIds.contains(edge.targetId()))
        .sorted(Comparator
            .comparingInt(ProjectGraphCollector::edgePriority)
            .thenComparing(EDGE_ORDER))
        .limit(limits.maxEdges())
        .toList();
  }

  private List<GraphRelationStatus> selectedRelationStatuses(Set<String> emittedNodeIds) {
    return relationStatuses.values().stream()
        .filter(status -> status.sourceId() == null || emittedNodeIds.contains(status.sourceId()))
        .filter(status -> status.targetId() == null || emittedNodeIds.contains(status.targetId()))
        .sorted(RELATION_STATUS_ORDER)
        .limit(limits.maxRelationStatuses())
        .toList();
  }

  private List<GraphWarning> selectedWarnings(
      int selectedNodeCount,
      int selectedEdgeCount,
      int selectedRelationStatusCount,
      Set<String> emittedNodeIds) {
    Map<String, GraphWarning> selected = new LinkedHashMap<>(warnings);
    long eligibleEdgeCount = edges.values().stream()
        .filter(edge -> emittedNodeIds.contains(edge.sourceId()))
        .filter(edge -> emittedNodeIds.contains(edge.targetId()))
        .count();
    long eligibleRelationStatusCount = relationStatuses.values().stream()
        .filter(status -> status.sourceId() == null || emittedNodeIds.contains(status.sourceId()))
        .filter(status -> status.targetId() == null || emittedNodeIds.contains(status.targetId()))
        .count();
    if (nodes.size() > selectedNodeCount) {
      selected.putIfAbsent("graph-warning:cap:nodes", capWarning(
          "nodes",
          "Graph node cap reached; lower-priority graph material was omitted."));
    }
    if (eligibleEdgeCount > selectedEdgeCount) {
      selected.putIfAbsent("graph-warning:cap:edges", capWarning(
          "edges",
          "Graph edge cap reached; lower-priority graph material was omitted."));
    }
    if (eligibleRelationStatusCount > selectedRelationStatusCount) {
      selected.putIfAbsent("graph-warning:cap:relation_statuses", capWarning(
          "relation_statuses",
          "Graph relation-status cap reached; lower-priority graph material was omitted."));
    }
    return new ArrayList<>(selected.values());
  }

  private static int nodePriority(GraphNode node) {
    return switch (node.kind()) {
      case "module" -> 0;
      case "type" -> 1;
      case "endpoint", "api_operation", "entity", "embeddable", "repository", "test",
          "document", "generated_source_root", "warning", "status" -> 2;
      case "package" -> 3;
      case "document_heading", "document_chunk" -> 4;
      default -> 10;
    };
  }

  private static int edgePriority(GraphEdge edge) {
    return switch (edge.type()) {
      case "owns" -> 0;
      case "declares" -> 1;
      case "repository_entity", "tested_subject" -> 2;
      case "document_reference" -> 3;
      default -> 10;
    };
  }

  private static boolean canMerge(GraphNode left, GraphNode right) {
    return left.kind().equals(right.kind())
        && left.label().equals(right.label())
        && left.claimCategory().equals(right.claimCategory())
        && Objects.equals(left.moduleId(), right.moduleId());
  }

  private static GraphNode merge(GraphNode left, GraphNode right) {
    LinkedHashSet<String> evidenceIds = new LinkedHashSet<>(left.evidenceIds());
    evidenceIds.addAll(right.evidenceIds());
    return new GraphNode(
        left.id(),
        left.kind(),
        left.label(),
        left.claimCategory(),
        left.moduleId(),
        left.sourceRef(),
        evidenceIds.stream().sorted().toList());
  }

  private static GraphWarning capWarning(String section, String message) {
    return new GraphWarning(
        "graph-warning:cap:" + section,
        "cap_reached",
        "warning",
        message,
        null,
        GraphDerivation.withoutFields("graph_cap", PROJECT_GRAPH_FILE_NAME, section),
        List.of());
  }

  private static GraphWarning duplicateWarning(String section, String id) {
    return new GraphWarning(
        "graph-warning:duplicate_omitted:" + section + ":" + ProjectGraphIds.key(id),
        "duplicate_omitted",
        "warning",
        "Graph ID collision omitted a later " + section + " record.",
        null,
        GraphDerivation.withoutFields("graph_duplicate_omission", PROJECT_GRAPH_FILE_NAME, section),
        List.of());
  }

  private static String sourceRefKey(GraphSourceRef sourceRef) {
    if (sourceRef == null) {
      return "";
    }
    return sourceRef.artifact() + "\0" + sourceRef.section() + "\0" + nullSafe(sourceRef.id());
  }

  private static String nullSafe(String value) {
    return value == null ? "" : value;
  }
}
