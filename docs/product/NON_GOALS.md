# Non-Goals

This document records the historical v0.1 non-goals and the product boundaries that
still keep the project focused on deterministic local Java/Spring memory. Items listed
here may be useful later, but they are not part of the first MVP and should become
current behavior only through an explicit roadmap, contract, tests, and release-note
change.

## Non-Goals For v0.1

- SaaS.
- Web UI.
- Chat interface.
- Generic RAG.
- Multi-language support.
- YouTrack connectors.
- Jira connectors.
- Confluence connectors.
- GitHub connectors.
- GitLab connectors.
- PDF parsing.
- LLM-generated architecture as the source of truth.
- Automatic code modification.
- Running Maven or other code generation during scans.
- Default scanning of generated source roots such as `target/generated-sources`.
- OpenAPI YAML parsing or generated API reconstruction.
- Continuous background indexing daemon.
- Cloud-hosted project memory.
- Authentication, organizations, teams, or billing.

## Later, Not Now

These may become useful after the deterministic Java/Spring core is proven:

- Optional LLM-assisted summaries generated from already extracted facts.
- YouTrack, Jira, Confluence, GitHub, and GitLab import adapters.
- Broader document ingestion.
- Gradle behavior beyond the bounded v1.1 static Java/Spring layout support, such as
  execution, dependency resolution, task graphs, custom source sets, or effective model
  reconstruction.
- Kotlin support for Spring projects.
- Deeper Spring Boot auto-configuration analysis.
- Full JPA/ORM relationship mapping and runtime persistence semantics beyond direct
  annotation extraction.
- Test coverage inference.
- Change-impact analysis.
- IDE integration.
- Hosted collaboration features.

None of these should be treated as current behavior until a scoped release track
documents the user-facing behavior, output/evidence implications, tests, limitations,
and release status.
