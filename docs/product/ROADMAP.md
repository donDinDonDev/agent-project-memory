# Roadmap

## Current Status

Stages 0 through 8 are closed for the v0.1 release-candidate implementation. Stage 9 is
post-v0.1 future work only and is not started.

## Stage 0: Foundation Docs (Closed)

Create the initial repository documentation, product boundaries, architecture notes, evidence model, and output contract.

Exit criteria:

- `README.md` explains the local-first Java/Spring purpose and non-goals.
- `docs/product/MVP_SPEC.md` defines v0.1 scope, out-of-scope items, inputs, outputs, and acceptance criteria.
- `docs/architecture/OUTPUT_CONTRACT.md` defines the first `.project-memory/` files and contract rules.
- `docs/architecture/EVIDENCE_MODEL.md` defines evidence references, evidence types, fact categories, and evidence discipline.
- `docs/development/CODEX_WORKFLOW.md` defines the Codex-driven development operating model.
- `docs/development/PROMPT_PLAYBOOK.md` provides reusable prompts for planning, execution, review, fixes, goal mode, stage close, and checkpointing.
- `AGENTS.md` points agents to the canonical docs needed before implementation work.
- No production implementation code, Maven project skeleton, or runtime dependencies have been added.

## Stage 1: Maven CLI Skeleton (Closed)

Create the Java 21 Maven project structure and a minimal CLI entrypoint. The CLI should accept a local path and prepare the output directory, but it should avoid implementing analyzers beyond what is needed to prove the command shape.

Exit criteria:

- The repository has a minimal Maven build configured for Java 21.
- The CLI exposes the intended command shape for scanning a local path.
- The CLI validates basic input path existence and creates or prepares `.project-memory/`.
- The implementation does not perform Spring, Java symbol, or evidence analysis beyond command-shape scaffolding.
- Focused tests cover CLI argument handling and output directory preparation.
- Documentation reflects how to build, test, and run the skeleton locally.

## Stage 2: Spring MVC Endpoints Analyzer (Closed)

Use JavaParser to extract Spring MVC controller classes and endpoint methods. Capture annotations, paths, HTTP methods, Java symbols, and line ranges.

Exit criteria:

- The analyzer detects controller classes annotated with `@Controller` and `@RestController`.
- The analyzer detects endpoint methods using common Spring MVC mapping annotations, including `@RequestMapping`, `@GetMapping`, `@PostMapping`, `@PutMapping`, `@PatchMapping`, and `@DeleteMapping`.
- Extracted endpoint facts include controller class, handler method, HTTP methods, paths, relevant request metadata when available, response type when available, and evidence IDs.
- Evidence records point to concrete source paths, symbols or annotations, line ranges when available, excerpts, and confidence.
- `endpoints.md` is generated from deterministic endpoint facts and includes evidence references.
- Fixture tests cover representative controllers, class-level mappings, method-level mappings, and unsupported or ambiguous cases.
- The analyzer does not use LLM calls or external services.

## Stage 3: Evidence/Project-Map Stabilization (Closed)

Stabilize `project-map.json` and `evidence-index.jsonl` around explicit contracts. Add fixture tests that compare generated JSON with expected results.

Exit criteria:

- `project-map.json` has a stable v0.1 schema for project metadata, Maven layout, source roots, endpoints, components when available, and evidence ID references.
- `evidence-index.jsonl` has stable evidence entries aligned with `docs/architecture/EVIDENCE_MODEL.md`.
- Generated JSON output is deterministic for the same fixture input.
- Golden fixture tests compare generated project-map and evidence-index output with expected files.
- Contract changes are reflected in `docs/architecture/OUTPUT_CONTRACT.md` and `docs/architecture/EVIDENCE_MODEL.md`.
- Unknown, inferred, or uncertain facts are represented explicitly instead of being presented as unsupported facts.

## Stage 4: Spring Components Analyzer (Closed)

Extract a basic inventory of Spring components such as controllers, services, repositories, configuration classes, and components. Attach evidence to every component fact.

Exit criteria:

- The analyzer detects common Spring stereotypes, including `@Component`, `@Service`, `@Repository`, `@Controller`, `@RestController`, and `@Configuration`.
- Component facts include stable IDs, fully qualified class names when resolvable, stereotypes, and evidence IDs.
- Evidence records point to the class or annotation source location.
- `project-map.json` includes component inventory without breaking the output contract.
- Fixture tests cover each supported stereotype and classes without supported stereotypes.
- The analyzer avoids reconstructing full Spring runtime behavior.

## Stage 5: JPA Entities Analyzer (Closed)

Detect JPA entities and basic relationships from annotations such as `@Entity`, `@Table`, `@Id`, `@ManyToOne`, and `@OneToMany`.

Exit criteria:

- The analyzer detects classes annotated with `@Entity`.
- Entity facts include class name, table name when declared, identifier fields when detected, and evidence IDs.
- Basic relationship annotations such as `@ManyToOne`, `@OneToMany`, `@OneToOne`, and `@ManyToMany` are captured as extracted facts when directly present.
- Relationship facts preserve evidence and mark uncertainty when target resolution is incomplete.
- Fixture tests cover simple entities, table names, ID fields, and basic relationships.
- The analyzer does not attempt full ORM runtime reconstruction.

## Stage 6: Tests Inventory (Closed)

Detect test classes and likely tested subjects using naming conventions, imports, annotations, and references. Mark uncertain relations explicitly.

Exit criteria:

- The analyzer detects test roots and test classes in standard Maven layouts.
- Test inventory includes class names, test framework signals when detectable, source paths, and evidence IDs.
- Likely tested-subject relations are marked as inferred unless directly supported by references.
- Uncertain relations include lower confidence or explicit uncertainty rather than being presented as facts.
- Fixture tests cover naming-convention matches, annotation-based test classes, and ambiguous cases.
- The output does not claim full test coverage analysis.

## Stage 7: Agent Guide Generator (Closed)

Generate `agent-guide.md` from the deterministic project map and evidence index. The guide should help coding agents orient themselves without inventing unsupported architecture.

Exit criteria:

- `agent-guide.md` is generated from `project-map.json` and `evidence-index.jsonl`.
- The guide summarizes detected build system, source roots, endpoints, components, entities, and tests when available.
- Every important claim is either evidence-backed, explicitly inferred, or explicitly uncertain.
- The guide includes practical orientation for coding agents without becoming a repository chatbot or generic RAG output.
- Fixture tests or golden-output checks cover guide generation from representative project-map inputs.
- The guide does not invent architecture that is not present in deterministic facts.

## Stage 8: Evaluation On Real Spring Projects (Closed)

Run the tool on real open-source Spring projects and measure whether generated facts are accurate, useful, and evidence-backed.

Exit criteria:

- A small set of real open-source Spring projects is selected with documented versions or commit references.
- The scan runs locally without sending source code to external services.
- Evaluation records endpoint, component, entity, test inventory, evidence quality, and false-positive or false-negative observations.
- Findings are summarized in `docs/development/EVALUATION_PLAN.md` or a linked evaluation report.
- Bugs or contract gaps discovered during evaluation are converted into bounded follow-up tasks.
- v0.1 scope remains Java/Spring-first and local-first.

## Stage 9: Future Connectors/Imports (Post-v0.1 Future, Not Started)

Add future input adapters for systems such as YouTrack, Jira, Confluence, GitHub, and
GitLab. These adapters should produce normalized source documents and remain separate
from the core analyzer. This stage is outside v0.1 and must not begin as part of v0.1
release-readiness work.

Exit criteria:

- Connector/import requirements are documented as post-v0.1 work and remain outside the deterministic core analyzer.
- Normalized imported-source document shapes are defined before implementation.
- Adapters preserve provenance so imported facts can be tied back to source systems.
- The core analyzer can run without connector dependencies or network access.
- Any optional AI layer remains separate from deterministic analysis and is not treated as evidence.
- Product and architecture docs are updated before any connector implementation begins.
