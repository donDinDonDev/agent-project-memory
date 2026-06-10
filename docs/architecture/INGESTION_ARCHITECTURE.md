# Ingestion Architecture

The current product focuses on local repository Java/Spring source files, Maven build
files, standard Maven source/test/resource roots, bounded local OpenAPI/Swagger spec
inputs, conservative default-scope local Markdown discovery, and deterministic
generated project-memory output. The current v0.8 implementation emits local Markdown
document inventory only; heading extraction, chunk extraction, document evidence,
code-doc reconciliation, and local documentation guide rendering remain later layers.

External connectors are future input adapters. They should not be part of the MVP core analyzer, and they should not be required to generate `.project-memory/` from a Java/Spring repository.

## SourceDocument

Future ingestors should normalize external and local documents into a `SourceDocument`
abstraction.

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

## v0.8 Local Markdown Ingestion Boundary

The v0.8 local document ingestor is a deterministic local Markdown ingestor, not a
generic document search system and not an AI documentation layer. The current
implementation covers default discovery and inventory only.

Default discovery should be conservative:

- root `README.md` or `README.markdown`;
- README files directly under supported Maven module roots;
- root `docs/**/*.md`;
- root `adr/**/*.md` or `adrs/**/*.md`.

Default discovery should exclude:

- `.project-memory/` generated outputs;
- `.git/` and hidden path segments;
- build, generated, dependency, and cache directories such as `target/`, `build/`,
  `out/`, `dist/`, `node_modules/`, and generated-source-like paths;
- maintainer-only, private/internal, and secret-like paths such as directories named
  `maintainer`, `internal`, `private`, or `secrets`;
- symlinked Markdown files and symlinked directories.

All emitted document paths must be normalized repository-relative paths. The ingestor
must not emit absolute paths, paths that start with `./`, paths that escape the scanned
repository root, or paths reached only by following symlinks.

Runbooks, local notes, broad repository-wide `*.md` discovery, hidden/private docs, and
generated documentation should remain outside the default scope unless a future explicit
include/exclude contract makes them safe and reviewable.

The current v0.8 ingestor normalizes accepted Markdown files into document inventory
only. Later layers may add heading references, bounded chunk references, and `document`
evidence records. The ingestor must not store full document bodies, perform semantic
summarization, run embeddings, build a vector index, call an LLM, fetch external links,
or read external documentation sources.

Code-doc reconciliation should be implemented only as conservative uncertain signals
derived from bounded deterministic token rules. Document-backed observations must remain
separate from code-backed facts. When code and documents disagree, generated memory
should prefer deterministic code facts and label document-side observations as
document-backed signals.

## v0.1 Ingestion Scope

v0.1 supports:

- Local repository files.
- Root Maven build file detection for `pom.xml`.
- Java source files under supported production source roots such as `src/main/java`.
- Java test files under supported test roots such as `src/test/java`.

v0.1 does not support:

- Local Markdown or document ingestion.
- YouTrack, Jira, Confluence, GitHub, or GitLab imports.
- PDF parsing.
- Web crawling.
- SaaS synchronization.
- Background indexing services.

## Relationship To Evidence

Future document ingestors may provide evidence, but document evidence must be identified
as `document` evidence and kept separate from code evidence. The current local Markdown
inventory slice does not emit document evidence records.

When code and documents disagree, generated memory should prefer deterministic code facts and mark document-only claims as document-backed, not code-backed.

The planned v0.8 document evidence layer follows that rule by emitting document evidence
for file, heading, chunk, and bounded mention observations only. Document evidence
should not become evidence for Java symbols, Spring annotations, build metadata, config
values, tests, OpenAPI implementation, runtime behavior, or source/document agreement.
