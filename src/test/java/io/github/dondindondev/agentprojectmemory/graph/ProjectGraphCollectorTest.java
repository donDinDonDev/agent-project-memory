package io.github.dondindondev.agentprojectmemory.graph;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

final class ProjectGraphCollectorTest {
  @Test
  void buildsDeterministicSortedGraphAndDropsDanglingEdges() {
    ProjectGraphCollector collector = new ProjectGraphCollector();
    collector.addNode(node("node:type:root:com.example.OrderController", "type"));
    collector.addNode(node("node:module:root", "module"));
    collector.addEdge(edge(
        "edge:owns:node:module:root:node:type:root:com.example.OrderController",
        "node:module:root",
        "node:type:root:com.example.OrderController"));
    collector.addEdge(edge(
        "edge:owns:node:module:root:node:type:missing",
        "node:module:root",
        "node:type:missing"));

    ProjectGraph graph = collector.build();

    assertAll(
        () -> assertEquals(List.of("node:module:root", "node:type:root:com.example.OrderController"),
            graph.nodes().stream().map(GraphNode::id).toList()),
        () -> assertEquals(
            List.of("edge:owns:node:module:root:node:type:root:com.example.OrderController"),
            graph.edges().stream().map(GraphEdge::id).toList()),
        () -> assertEquals(List.of(), graph.warnings()));
  }

  @Test
  void emitsCapWarningsAndPreservesPriorityBeforeFinalSort() {
    ProjectGraphCollector collector = new ProjectGraphCollector(new ProjectGraphLimits(2, 0, 0));
    collector.addNode(node("node:type:root:com.example.OrderController", "type"));
    collector.addNode(node("node:module:root", "module"));
    collector.addNode(node("node:package:root:com.example", "package"));
    collector.addEdge(edge(
        "edge:owns:node:module:root:node:type:root:com.example.OrderController",
        "node:module:root",
        "node:type:root:com.example.OrderController"));

    ProjectGraph graph = collector.build();

    assertAll(
        () -> assertEquals(List.of("node:module:root", "node:type:root:com.example.OrderController"),
            graph.nodes().stream().map(GraphNode::id).toList()),
        () -> assertEquals(List.of(), graph.edges()),
        () -> assertEquals(
            List.of("graph-warning:cap:edges", "graph-warning:cap:nodes"),
            graph.warnings().stream().map(GraphWarning::id).toList()));
  }

  @Test
  void nodeCapsPreferDirectFactNodesBeforeStructuralPackageNodes() {
    ProjectGraphCollector collector = new ProjectGraphCollector(new ProjectGraphLimits(3, 10, 0));
    collector.addNode(node("node:type:root:com.example.OrderController", "type"));
    collector.addNode(node("node:module:root", "module"));
    collector.addNode(node("node:package:root:com.example", "package"));
    collector.addNode(node("node:endpoint:endpoint%3Acom.example.OrderController%23get", "endpoint"));

    ProjectGraph graph = collector.build();

    assertEquals(
        List.of(
            "node:endpoint:endpoint%3Acom.example.OrderController%23get",
            "node:module:root",
            "node:type:root:com.example.OrderController"),
        graph.nodes().stream().map(GraphNode::id).toList());
  }

  @Test
  void graphIdKeysPreserveReadablePathCharactersAndEscapeSeparators() {
    assertEquals(
        "openapi_operation%3Amodule%3A.%3A/orders/{id}%E2%80%A8",
        ProjectGraphIds.key("openapi_operation:module:.:/orders/{id}\u2028"));
  }

  private GraphNode node(String id, String kind) {
    return new GraphNode(
        id,
        kind,
        id,
        "structural",
        "module:.",
        new GraphSourceRef("project-map.json", "test.items", id),
        List.of());
  }

  private GraphEdge edge(String id, String sourceId, String targetId) {
    return new GraphEdge(
        id,
        "owns",
        sourceId,
        targetId,
        "structural",
        "derived",
        "project_map_derivation",
        "high",
        null,
        Map.of(),
        new GraphDerivation(
            "project_map_field",
            "project-map.json",
            "test.items",
            List.of("id")),
        List.of());
  }
}
