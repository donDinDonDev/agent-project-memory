# MVP Spec

## Version

v0.1 is the first implementation milestone for `agent-project-memory`.

The MVP is a local-first Java 21 CLI/devtool that scans a Maven-based Java/Spring repository and writes evidence-backed project memory files under `.project-memory/`.

## In Scope

v0.1 includes:

- Local repository scan.
- Maven project detection.
- Java source root detection.
- Spring MVC endpoint extraction.
- Basic Spring component inventory.
- Basic direct JPA entity extraction.
- Minimal deterministic tests inventory.
- Evidence index.
- `project-map.json`.
- `endpoints.md`.
- `agent-guide.md`.
- Sample fixture tests.

## Expected Inputs

The first supported input is a local repository path, eventually invoked as:

```sh
agent-project-memory scan .
```

The repository may include:

- Maven build files such as `pom.xml`.
- Java source roots such as `src/main/java`.
- Spring MVC controllers.
- Spring components and services.
- JPA entities with direct annotations.
- Local Markdown documentation.
- Tests under standard Maven test roots.

## Expected Outputs

The scan writes:

```text
.project-memory/project-map.json
.project-memory/evidence-index.jsonl
.project-memory/endpoints.md
.project-memory/agent-guide.md
```

Output shape is governed by `docs/architecture/OUTPUT_CONTRACT.md`.

Evidence shape is governed by `docs/architecture/EVIDENCE_MODEL.md`.

## Out Of Scope

v0.1 does not include:

- SaaS.
- Web UI.
- Chat.
- LLM calls in the core analyzer.
- YouTrack, Jira, Confluence, GitHub, or GitLab connectors.
- Generic RAG.
- Multi-language analysis.
- PDF parsing.
- Automatic code modification.
- Complete Spring runtime behavior reconstruction.
- Full ORM runtime behavior reconstruction.
- Full dependency graph analysis.
- Full test coverage analysis.
- Test execution analysis, CI integration, mutation testing, or runtime test behavior analysis.
- Call graph or symbol-solving based test-subject resolution.

## Acceptance Criteria

v0.1 is acceptable when:

- The tool can scan a local Maven Java/Spring fixture project.
- Maven project detection identifies at least root `pom.xml` and Maven module boundaries when present.
- Java source root detection identifies standard Maven source and test roots.
- Spring MVC endpoint extraction detects common controller annotations and request mapping annotations.
- Basic Spring component inventory detects common stereotypes such as `@Component`, `@Service`, `@Repository`, `@Controller`, and `@RestController`.
- Basic direct JPA entity extraction detects direct `@Entity`, direct `@Table(name = "...")`,
  field-level `@Id` declared on the entity class or on an immediate source-visible
  `@MappedSuperclass`, and direct field-level relationship annotations while marking
  unresolved relationship targets as uncertain.
- Minimal tests inventory detects test-like Java classes under standard single-module
  Maven `src/test/java`, while omitting helper/support/configuration declarations
  without clear test naming or direct test-class marker annotations.
- Minimal tests inventory records test class names, repository-relative source paths,
  directly visible framework signals when supported, and evidence IDs.
- Likely tested-subject relations are emitted only as conservative inferred relations,
  based on naming conventions and evidence for both the test class and candidate
  production class.
- Minimal tests inventory does not claim full test coverage, test execution results,
  behavioral assertion analysis, CI results, call graph resolution, symbol solving, or
  complete tested-subject mapping.
- `evidence-index.jsonl` contains evidence entries for important generated facts.
- `project-map.json` references evidence IDs for extracted facts.
- `endpoints.md` lists detected endpoints with evidence references.
- `agent-guide.md` summarizes how a coding agent should approach the project based on extracted facts, without inventing unsupported architecture.
- Fixture tests compare generated facts with expected output.
- No source code is sent to external services by default.
- The core analyzer does not depend on external APIs.
