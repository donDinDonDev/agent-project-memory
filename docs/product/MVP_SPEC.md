# MVP Spec

## Version

v0.1 is the first release-candidate implementation milestone for
`agent-project-memory`.

The MVP is a local-first Java 21 CLI/devtool that scans a Maven-based Java/Spring
repository and writes evidence-backed project memory files under `.project-memory/`.
The current build is packaged with Maven as an executable shaded jar produced by
`mvn package`.

## In Scope

v0.1 includes:

- Local repository scan.
- Root `pom.xml` Maven project detection.
- Standard single-module Maven source and test root detection.
- Spring MVC endpoint extraction.
- Source-visible interface-declared Spring MVC endpoint mappings under supported
  production source roots when they can be uniquely bound to concrete handlers.
- Basic Spring component inventory.
- Deterministic hidden HTTP surface warnings without endpoint expansion.
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

- A root Maven build file at `pom.xml`.
- Standard single-module Maven source roots such as `src/main/java`.
- Spring MVC controllers.
- Java interfaces under supported production source roots when they declare Spring MVC
  mappings implemented by concrete controllers.
- Spring components and services, including directly annotated interfaces.
- JPA entities with direct annotations.
- Optional OpenAPI/Swagger spec files, Maven code generation plugin declarations under
  root `pom.xml` build plugin sections, and direct `@RepositoryRestResource`
  annotations that should be warned about but not expanded into endpoint facts.
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
- Local Markdown or document ingestion in the current implementation.
- Running Maven or other code generation during a scan.
- Default scanning of generated source roots such as `target/generated-sources`.
- OpenAPI YAML parsing or generated API reconstruction.
- Complete Spring runtime behavior reconstruction.
- Full ORM runtime behavior reconstruction.
- Full dependency graph analysis.
- Full test coverage analysis.
- Test execution analysis, CI integration, mutation testing, or runtime test behavior analysis.
- Call graph or symbol-solving based test-subject resolution.

## Acceptance Criteria

v0.1 is acceptable when:

- The tool can scan a local Maven Java/Spring fixture project.
- Maven project detection identifies a root `pom.xml` when present.
- Java source root detection identifies standard single-module Maven source and test
  roots such as `src/main/java` and `src/test/java` when present.
- Spring MVC endpoint extraction detects common controller annotations and request
  mapping annotations declared directly on handler methods, and detects
  source-visible interface-declared mappings only when the interface is under a
  supported production source root such as `src/main/java` and the mapping can be
  uniquely bound to a concrete controller handler.
- Basic Spring component inventory detects common direct stereotypes such as
  `@Component`, `@Service`, `@Repository`, `@Controller`, and `@RestController` on
  source-visible Java classes or interfaces, without inferring repository components
  from `extends JpaRepository`.
- Hidden HTTP surface warnings detect bounded deterministic signals such as
  OpenAPI/Swagger spec filename presence, root `pom.xml` OpenAPI/Swagger Maven plugin
  declarations under build plugin sections, and direct source-visible
  `@RepositoryRestResource`, while not adding endpoint facts from those warnings.
- Basic direct JPA entity extraction detects direct `@Entity`, direct `@Table(name = "...")`,
  field-level `@Id` declared on the entity class or on a conservative source-visible
  `@MappedSuperclass` chain, and direct field-level relationship annotations while
  marking unresolved relationship targets as uncertain.
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
- Full Maven module parsing, generated-source/OpenAPI reconstruction, and Local
  Markdown ingestion remain outside the current v0.1 implementation.
