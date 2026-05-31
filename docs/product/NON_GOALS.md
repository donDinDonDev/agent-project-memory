# Non-Goals

This document protects the v0.1 scope. Items listed here may be useful later, but they are not part of the first MVP.

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
- Gradle support.
- Kotlin support for Spring projects.
- Deeper Spring Boot auto-configuration analysis.
- Full JPA/ORM relationship mapping and runtime persistence semantics beyond direct
  annotation extraction.
- Test coverage inference.
- Change-impact analysis.
- IDE integration.
- Hosted collaboration features.

None of these should be implemented before v0.1 has stable local scanning, evidence-backed outputs, and tests against fixture Spring projects.
