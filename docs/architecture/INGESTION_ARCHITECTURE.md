# Ingestion Architecture

The v0.1 product focuses on local repository files and local Markdown docs.

External connectors are future input adapters. They should not be part of the MVP core analyzer, and they should not be required to generate `.project-memory/` from a Java/Spring repository.

## SourceDocument

Future ingestors should normalize external and local documents into a `SourceDocument` abstraction.

Proposed fields:

- `id`: stable document identifier.
- `sourceType`: source category, such as `local_markdown`, `youtrack_issue`, `jira_issue`, `confluence_page`, `github_issue`, or `gitlab_issue`.
- `title`: document title.
- `body`: normalized text body.
- `localPath`: repository-relative or filesystem path when applicable.
- `url`: source URL when applicable.
- `createdAt`: creation timestamp when known.
- `updatedAt`: update timestamp when known.
- `contentHash`: hash of the normalized content.
- `tags`: source labels, project keys, or other classification tags.

## Connector Role

Future connectors for YouTrack, Jira, Confluence, GitHub, and GitLab should produce `SourceDocument` records.

They should not:

- own the project memory schema,
- bypass evidence requirements,
- make LLM output authoritative,
- become required for local repository scanning,
- add network dependencies to the core Java/Spring analyzer.

## v0.1 Ingestion Scope

v0.1 supports:

- Local repository files.
- Maven build files.
- Java source files.
- Java test files.
- Local Markdown docs when present.

v0.1 does not support:

- YouTrack, Jira, Confluence, GitHub, or GitLab imports.
- PDF parsing.
- Web crawling.
- SaaS synchronization.
- Background indexing services.

## Relationship To Evidence

Documents can provide evidence, but document evidence must be identified as `document` evidence and kept separate from code evidence.

When code and documents disagree, generated memory should prefer deterministic code facts and mark document-only claims as document-backed, not code-backed.

