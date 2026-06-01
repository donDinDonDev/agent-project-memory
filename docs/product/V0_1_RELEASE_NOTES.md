# v0.1 Release Notes

These notes summarize the v0.1 public release surface for `agent-project-memory`.
Detailed evaluation records remain in [docs/development/evaluations/](../development/evaluations/).

## Release Status

v0.1 is the first public local-first CLI/devtool milestone for generating
evidence-backed project memory for Java/Spring codebases.

The implementation has completed roadmap Stages 0 through 8 for v0.1. Future
connector/import work remains post-v0.1 and has not started.

## Scope

v0.1 scans a local Maven-style Java/Spring repository and writes deterministic
project-memory artifacts under `.project-memory/`.

Included in v0.1:

- Java 21 Maven CLI with `scan <path>`.
- Root `pom.xml` Maven detection.
- Standard single-module Maven production and test roots: `src/main/java` and
  `src/test/java`.
- Spring MVC endpoint extraction from direct controller mappings.
- Source-visible interface-declared Spring MVC endpoint mappings when the interface is
  under a supported production source root and uniquely binds to a concrete handler.
- Deterministic hidden HTTP surface warnings for OpenAPI/Swagger filenames, root
  `pom.xml` OpenAPI/Swagger generator plugin declarations, and direct
  `@RepositoryRestResource`.
- Direct Spring stereotype component inventory.
- Direct JPA entity inventory with conservative mapped-superclass identifier support.
- Minimal deterministic tests inventory with naming-convention tested-subject
  inferences.
- Evidence-backed Markdown and JSON output files.

The core analyzer is deterministic and local-first. It does not call LLMs or external
services, and source code is not sent anywhere by default.

## Outputs

For a supported scan target with `src/main/java`, v0.1 writes:

```text
.project-memory/project-map.json
.project-memory/evidence-index.jsonl
.project-memory/endpoints.md
.project-memory/agent-guide.md
```

- `project-map.json` is the stable v0.1 machine-readable project map.
- `evidence-index.jsonl` contains source-backed evidence records referenced by generated
  facts.
- `endpoints.md` is a deterministic endpoint inventory.
- `agent-guide.md` is a deterministic orientation guide generated from the structured
  facts and evidence index.

The output shape is governed by
[docs/architecture/OUTPUT_CONTRACT.md](../architecture/OUTPUT_CONTRACT.md); evidence
semantics are governed by
[docs/architecture/EVIDENCE_MODEL.md](../architecture/EVIDENCE_MODEL.md).

## Evaluation Summary

Stage 8 evaluated v0.1 against three pinned open-source Spring projects. The reports are
supporting detail rather than the primary user path:

| Project | Ref | Result |
| --- | --- | --- |
| Spring PetClinic | `3c06fbfc1e42eb40802e0d0ca989bc9226755804` | Passed the bounded v0.1 scorecard after parser, mapped-superclass, and test-inventory follow-ups. |
| Spring PetClinic REST | `v4.0.2` / `d8026bb5bcc58145b95a66a7f8e7694f0fae142f` | Direct-source facts were accurate and evidence-backed; generated/OpenAPI API operations remained a documented limitation. |
| Spring Guide REST Service | `e9efc9dfa0abe8cf8e15cf0e71830b5125322cae` (`complete/`) | Passed the bounded v0.1 scorecard for a small source-visible Maven Spring MVC sample. |

Detailed reports:

- [docs/development/evaluations/spring-petclinic-3c06fbf.md](../development/evaluations/spring-petclinic-3c06fbf.md)
- [docs/development/evaluations/spring-petclinic-rest-v4.0.2.md](../development/evaluations/spring-petclinic-rest-v4.0.2.md)
- [docs/development/evaluations/gs-rest-service-e9efc9d.md](../development/evaluations/gs-rest-service-e9efc9d.md)

## Known Limitations

v0.1 intentionally does not implement:

- Full Maven module parsing.
- Gradle or Kotlin support.
- Multi-language analysis beyond the documented Java/Spring focus.
- Spring runtime handler mapping, component scanning, bean lifecycle, autowiring graphs,
  or runtime configuration behavior.
- Full ORM runtime behavior, property-access mapping, embedded IDs, generated-value
  semantics, schema generation, or repository analysis.
- OpenAPI YAML parsing, generated API reconstruction, Maven generation during scans, or
  default scanning of `target/generated-sources`.
- Test execution, coverage, assertion analysis, CI analysis, call graphs, or complete
  tested-subject mapping.
- Local Markdown/document ingestion.
- YouTrack, Jira, Confluence, GitHub, or GitLab connectors.
- SaaS, web UI, repository chat, generic RAG, or automatic code modification.
- LLM calls in the core analyzer.

When a relation is inferred or uncertain, v0.1 preserves that status in generated
outputs instead of presenting it as a direct fact.

## Validation

The v0.1 release-readiness documentation pass validates working-tree scope, public-doc
marker hygiene, Markdown whitespace, the full test suite, and package generation with
`git status --short`, `rg`, `git diff --check`, `mvn test`, `mvn package`, and
`git diff --stat`.

`mvn package` produces the executable shaded CLI jar at:

```text
target/agent-project-memory-0.1.0-SNAPSHOT.jar
```
