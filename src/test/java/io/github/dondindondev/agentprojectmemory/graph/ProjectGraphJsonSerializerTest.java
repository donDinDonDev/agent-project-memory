package io.github.dondindondev.agentprojectmemory.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

final class ProjectGraphJsonSerializerTest {
  private final ProjectGraphJsonSerializer serializer = new ProjectGraphJsonSerializer();

  @Test
  void serializesStableSchemaFieldOrderAndNulls() {
    ProjectGraph graph = new ProjectGraph(
        "1.0",
        "1.0",
        "lightweight_relation_graph",
        List.of("project-map.json", "evidence-index.jsonl"),
        new ProjectGraphLimits(20_000, 50_000, 10_000),
        List.of(new GraphNode(
            "node:type:root:com.example.OrderController",
            "type",
            "OrderController",
            "extracted",
            "module:.",
            new GraphSourceRef("project-map.json", "components.items", "component:com.example.OrderController"),
            List.of("ev:src/main/java/com/example/OrderController.java:1-1:code_symbol:type"))),
        List.of(new GraphEdge(
            "edge:owns:node:module:root:node:type:root:com.example.OrderController",
            "owns",
            "node:module:root",
            "node:type:root:com.example.OrderController",
            "structural",
            "derived",
            "project_map_derivation",
            "high",
            null,
            new GraphDerivation(
                "project_map_field",
                "project-map.json",
                "components.items",
                List.of("module_id", "class_name")),
            List.of())),
        List.of(),
        List.of(new GraphWarning(
            "graph-warning:cap:nodes",
            "cap_reached",
            "warning",
            "Graph node cap reached; lower-priority graph material was omitted.",
            null,
            GraphDerivation.withoutFields("graph_cap", "project-graph.json", "nodes"),
            List.of())));

    assertEquals("""
        {
          "graph_schema_version": "1.0",
          "project_map_schema_version": "1.0",
          "graph_kind": "lightweight_relation_graph",
          "source_artifacts": [
            "project-map.json",
            "evidence-index.jsonl"
          ],
          "limits": {
            "max_nodes": 20000,
            "max_edges": 50000,
            "max_relation_statuses": 10000
          },
          "nodes": [
            {
              "id": "node:type:root:com.example.OrderController",
              "kind": "type",
              "label": "OrderController",
              "claim_category": "extracted",
              "module_id": "module:.",
              "source_ref": {
                "artifact": "project-map.json",
                "section": "components.items",
                "id": "component:com.example.OrderController"
              },
              "evidence_ids": [
                "ev:src/main/java/com/example/OrderController.java:1-1:code_symbol:type"
              ]
            }
          ],
          "edges": [
            {
              "id": "edge:owns:node:module:root:node:type:root:com.example.OrderController",
              "type": "owns",
              "source_id": "node:module:root",
              "target_id": "node:type:root:com.example.OrderController",
              "claim_category": "structural",
              "relation_status": "derived",
              "support_type": "project_map_derivation",
              "confidence": "high",
              "uncertainty": null,
              "derivation": {
                "kind": "project_map_field",
                "artifact": "project-map.json",
                "section": "components.items",
                "fields": [
                  "module_id",
                  "class_name"
                ]
              },
              "evidence_ids": []
            }
          ],
          "relation_statuses": [],
          "warnings": [
            {
              "id": "graph-warning:cap:nodes",
              "category": "cap_reached",
              "severity": "warning",
              "message": "Graph node cap reached; lower-priority graph material was omitted.",
              "source_ref": null,
              "derivation": {
                "kind": "graph_cap",
                "artifact": "project-graph.json",
                "section": "nodes"
              },
              "evidence_ids": []
            }
          ]
        }
        """, serializer.serialize(graph));
  }
}
