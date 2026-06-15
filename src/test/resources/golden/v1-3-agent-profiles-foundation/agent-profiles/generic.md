# Generic Agent Profile

Generated deterministically from `project-map.json` and `evidence-index.jsonl` for the `generic` profile. The profile generator does not re-analyze source files.

## Profile Operating Notes

- Intended use: Tool-neutral repository orientation for deterministic project memory consumers.
- First-pass reading order: `project-map.json`, `evidence-index.jsonl`, `agent-guide.md`, `endpoints.md`
- Change posture: Consume machine-readable facts first and use Markdown only as a cautious presentation layer.
- Evidence posture: Resolve every important claim through existing evidence IDs; do not create new facts from this profile.
- Selector: `generic`; artifact path: `agent-profiles/generic.md`

## Source Artifacts

- `project-map.json`: machine-readable source for extracted facts, inferred/statused relations, uncertain signals, document-backed hints, spec-backed operations, generated-source metadata, warnings, and not-analyzed status fields.
- `evidence-index.jsonl`: existing evidence records referenced by `*_evidence_ids`; this profile does not add evidence records.
- `agent-guide.md`: generic deterministic guide generated from the same source artifacts; this profile narrows the presentation for `generic`.
- `endpoints.md`: endpoint-oriented Markdown derived from the same structured endpoint and OpenAPI facts.

## Project Snapshot

- Schema version: `1.0`
- Build system: `maven`; root build file: `pom.xml`.
  - Build evidence: `pom.xml:1` (`ev:pom.xml:1-1:build_file:pom.xml`)
- Layout counts: modules `1` (status `analyzed`); source roots `1`; test roots `1`.
- API counts: endpoint facts `2`; source-visible endpoint IDs `2`; interface endpoint IDs `0`; OpenAPI operations `0` (status `not_detected`).
- Spring surface counts: components `5`; repository signals `2`; inferred repository/entity links `1`; configuration classes `1`; bean methods `0`; behavior/messaging signals `0`.
- Domain/test counts: JPA entities `5`; embeddables `2`; test classes `1` (status `analyzed`); test-gap hints `7`; change-risk hints `6`.
- Generated-source metadata: status `analyzed`; content scan `disabled`; content status `not_scanned`; roots `0`.
- Documents/warnings: documents `0` (status `not_detected`); reconciliation hints `0` (status `not_detected`); warnings `0` (status `analyzed`).

## Evidence-Visible Fact Pointers

- Build/layout facts: `pom.xml:1` (`ev:pom.xml:1-1:build_file:pom.xml`), `pom.xml:7` (`ev:pom.xml:7-7:build_file:maven:project:groupId`), `pom.xml:8` (`ev:pom.xml:8-8:build_file:maven:project:artifactId`), `pom.xml:9` (`ev:pom.xml:9-9:build_file:maven:project:version`), `pom.xml:30` (`ev:pom.xml:30-30:build_file:maven:dependency:000002:groupId`), ... and 25 more evidence references in `evidence-index.jsonl`.
- API facts and spec-backed operations: `src/main/java/com/example/web/ProjectMapController.java:12` (`ev:src/main/java/com/example/web/ProjectMapController.java:12-12:com.example.web.ProjectMapController:@RequestMapping`), `src/main/java/com/example/web/ProjectMapController.java:21` (`ev:src/main/java/com/example/web/ProjectMapController.java:21-21:com.example.web.ProjectMapController#createItem:@PostMapping`), `src/main/java/com/example/web/ProjectMapController.java:11` (`ev:src/main/java/com/example/web/ProjectMapController.java:11-11:com.example.web.ProjectMapController:@RestController`), `src/main/java/com/example/web/ProjectMapController.java:22` (`ev:src/main/java/com/example/web/ProjectMapController.java:22-22:com.example.web.ProjectMapController#createItem:@RequestBody:parameter:0:request`), `src/main/java/com/example/web/ProjectMapController.java:16` (`ev:src/main/java/com/example/web/ProjectMapController.java:16-16:com.example.web.ProjectMapController#getItem:@PathVariable:parameter:0:id`), ... and 2 more evidence references in `evidence-index.jsonl`.
- Spring application-surface facts: `src/main/java/com/example/components/InventoryComponents.java:16` (`ev:src/main/java/com/example/components/InventoryComponents.java:16-16:com.example.components.InventoryRepository:@Repository`), `src/main/java/com/example/repositories/ProjectOrderRepository.java:6` (`ev:src/main/java/com/example/repositories/ProjectOrderRepository.java:6-6:com.example.repositories.ProjectOrderRepository:extends:org.springframework.data.jpa.repository.JpaRepository`), `src/main/java/com/example/domain/ProjectEntities.java:52` (`ev:src/main/java/com/example/domain/ProjectEntities.java:52-52:com.example.domain.ProjectOrder:@Entity`), `src/main/java/com/example/domain/ProjectEntities.java:53` (`ev:src/main/java/com/example/domain/ProjectEntities.java:53-53:com.example.domain.ProjectOrder:@Table`), `src/main/java/com/example/repositories/ProjectOrderRepository.java:6-7` (`ev:src/main/java/com/example/repositories/ProjectOrderRepository.java:6-7:com.example.repositories.ProjectOrderRepository:com.example.repositories.ProjectOrderRepository`), ... and 1 more evidence references in `evidence-index.jsonl`.
- Domain and persistence facts: `src/main/java/com/example/domain/ProjectEntities.java:36` (`ev:src/main/java/com/example/domain/ProjectEntities.java:36-36:com.example.domain.ProjectCustomer:@Id:field:id`), `src/main/java/com/example/domain/ProjectEntities.java:34` (`ev:src/main/java/com/example/domain/ProjectEntities.java:34-34:com.example.domain.ProjectCustomer:@Entity`), `src/main/java/com/example/domain/ProjectEntities.java:108` (`ev:src/main/java/com/example/domain/ProjectEntities.java:108-108:com.example.domain.ProjectLegacyOrder:@IdClass`), `src/main/java/com/example/domain/ProjectEntities.java:113` (`ev:src/main/java/com/example/domain/ProjectEntities.java:113-113:com.example.domain.ProjectLegacyOrder:@Id:field:orderNumber`), `src/main/java/com/example/domain/ProjectEntities.java:110` (`ev:src/main/java/com/example/domain/ProjectEntities.java:110-110:com.example.domain.ProjectLegacyOrder:@Id:field:tenantId`), ... and 27 more evidence references in `evidence-index.jsonl`.
- Tests and quality planning hints: `src/test/java/com/example/web/ProjectMapControllerTest.java:3` (`ev:src/test/java/com/example/web/ProjectMapControllerTest.java:3-3:com.example.web.ProjectMapControllerTest:test_file`), `src/main/java/com/example/web/ProjectMapController.java:13` (`ev:src/main/java/com/example/web/ProjectMapController.java:13-13:com.example.web.ProjectMapController:code_symbol`), `src/main/java/com/example/domain/ProjectEntities.java:34` (`ev:src/main/java/com/example/domain/ProjectEntities.java:34-34:com.example.domain.ProjectCustomer:@Entity`), `src/main/java/com/example/domain/ProjectEntities.java:107` (`ev:src/main/java/com/example/domain/ProjectEntities.java:107-107:com.example.domain.ProjectLegacyOrder:@Entity`), `src/main/java/com/example/domain/ProjectEntities.java:108` (`ev:src/main/java/com/example/domain/ProjectEntities.java:108-108:com.example.domain.ProjectLegacyOrder:@IdClass`), ... and 17 more evidence references in `evidence-index.jsonl`.
- Document-backed hints: none recorded.
- Generated-source metadata and warnings: none recorded.
- First evidence paths: `pom.xml`, `src/main/resources/application.yml`, `src/main/java/com/example/Stage3Application.java`, `src/main/java/com/example/web/ProjectMapController.java`, `src/main/java/com/example/components/InventoryComponents.java`, ... and 3 more evidence paths in `evidence-index.jsonl`.

## Fact Boundary Map

- Extracted facts: use source-visible facts from `project-map.json` sections such as `endpoints[]`, `components.items`, `entities.items`, build/config sections, and Spring Boot application signals.
- Inferred relations/signals: keep `support_type`, `entity_relation_status`, tested-subject relation statuses, confidence, and uncertainty fields attached to the relation.
- Uncertain signals: treat statuses such as `ambiguous`, `unsupported`, `not_detected`, `no_obvious_test`, and `uncertain_planning_hint` as bounded planning signals, not proof.
- Document-backed hints: use `documents.items` and `documents.reconciliation.items` as default-scope Markdown navigation or uncertain hints only; document bodies are not serialized here.
- Spec-backed operations: OpenAPI operation rows are declared contract facts with `implementation_status: "not_analyzed"`, not implemented endpoint facts.
- Generated-source metadata: generated-source roots and codegen/API signals are metadata-only observations with `content_status: "not_scanned"`; generated source contents are not scanned.
- Warnings: warning rows are inspection hints and change-surface inputs; do not upgrade them into extracted behavior facts.
- Not-analyzed areas: preserve explicit `analysis_status`, `implementation_status`, `content_status`, confidence, and uncertainty boundaries when using this profile.

## Profile Checklist

- Prefer `project-map.json` for automation and `agent-guide.md` for human orientation.
- Preserve extracted, inferred, uncertain, warning, document-backed, spec-backed, generated-source, and not-analyzed categories.
- When a category has no evidence pointers, treat it as absent or not recorded in the current analyzer scope.
