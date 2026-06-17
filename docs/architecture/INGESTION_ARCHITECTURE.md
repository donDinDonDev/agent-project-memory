# Ingestion Architecture

The current product focuses on local repository Java/Spring source files, Maven build
files, bounded static Gradle build files, standard Maven or Gradle source/test/resource
roots, bounded local OpenAPI/Swagger spec inputs, conservative default-scope local
Markdown discovery, and deterministic generated project-memory output. The current v1.x
line emits local Markdown document inventory, deterministic ATX heading references,
bounded chunk references, resolving document evidence, conservative code-doc
reconciliation signals, and compact local documentation guide rendering from structured
document facts and evidence only.

External connectors are future input adapters. They should not be part of the MVP core
analyzer, and they should not be required to generate `.project-memory/` from a
Java/Spring repository.

## Planned v2 Adapter Boundary

The v2 adapter platform is planned as an optional ingestion layer around the
deterministic core, not as a replacement for the local Java/Spring analyzer. The core
scanner, analyzers, graph builder, evidence index builder, query layer, and generators
must continue to run without adapters, connector configuration, network access,
credential handling, plugin loading, AI providers, or source upload.

Adapters should have these responsibilities:

- read one explicitly configured input source;
- validate that the requested import mode is allowed;
- normalize accepted records into source documents plus provenance metadata;
- classify records as document-backed, metadata-only, spec-backed, or warning/status
  material before they reach generated memory;
- preserve source identity, timestamps, content hashes, source URLs or source IDs where
  applicable, and the trust boundary that produced the record;
- avoid exposing credentials, tokens, local absolute paths, raw command logs, or raw
  connector configuration values in generated output.

Adapters must not:

- create code-backed Java/Spring facts;
- bypass evidence or provenance requirements;
- mutate repository source files;
- become required for normal `scan <path>` behavior;
- add network, auth, AI, or plugin dependencies to the core analyzer;
- make external text, connector summaries, query output, or AI output authoritative.

The smallest planned lifecycle is:

1. Adapter configuration is selected explicitly and defaults to no adapters.
2. The adapter validates the source boundary and import mode before reading input.
3. Local import adapters read local export files only; future API adapters must require
   explicit network enablement.
4. The adapter emits normalized documents and separate provenance metadata.
5. The core ingests adapter output only through documented document/spec/metadata
   boundaries and keeps adapter-backed observations distinct from code-backed facts.

## SourceDocument

Future external ingestors and any broader local document modes should normalize inputs
into a `SourceDocument` abstraction. For v2 design, `SourceDocument` should be treated
as a planned boundary object rather than a stable public API.

Fields stable enough for the v2 design are:

- `id`: stable document identifier.
- `sourceType`: source category, such as `local_markdown`, `local_export`,
  `youtrack_issue`, `jira_issue`, `confluence_page`, `github_issue`, or
  `gitlab_issue`.
- `title`: document title.
- `contentHash`: hash of the normalized content.
- `contentStatus`: whether normalized content is available only in memory, bounded,
  not serialized, or unavailable.
- `provenance`: connector/source metadata described below.

Fields that remain draft until v2 output and evidence contracts are updated are:

- `body` or `normalizedBody`: normalized text body used by an analyzer. Full bodies
  should not be serialized by default.
- `localPath`: repository-relative path when the input is inside the scanned repository,
  or a redacted/non-output filesystem reference when the source is outside it.
- `url`: source URL when applicable.
- `sourceId`: external issue/page/record identifier when applicable.
- `createdAt`: creation timestamp when known.
- `updatedAt`: update timestamp when known.
- `exportedAt` or `fetchedAt`: timestamp for the import source snapshot.
- `tags`: source labels, project keys, or other classification tags.
- `adapterName` and `adapterVersion`: identity of the adapter that normalized the
  record.

`SourceDocument` identity should be stable within one import snapshot, but future v2
contracts must still decide whether generated output uses adapter-assigned IDs,
content-addressed IDs, source-system IDs, or a combination. External IDs and URLs are
provenance, not proof that the external service is currently reachable or authoritative.

## Connector Provenance

Connector provenance should be emitted as a separate source envelope rather than hidden
inside free-form document text. At minimum, future v2 provenance should identify:

- source kind and adapter identity;
- import mode, such as local export import or explicitly enabled API import;
- source-system record ID, URL, project key, repository, or namespace when applicable;
- content hash and import snapshot timestamp;
- whether content came from a local file, local export bundle, or remote API response;
- whether network access was disabled, explicitly enabled, or not applicable;
- trust-boundary notes needed to keep external records separate from repository source.

Credential names, credential values, authorization headers, tokens, cookies, local
machine paths, and raw connector request/response logs must not be serialized as
provenance.

## Optional AI And Adapter Provenance

Optional AI presentation is not an ingestion adapter. It must not normalize connector
records, create `SourceDocument` objects, fetch remote systems, read repository source
directly, or decide whether external records become project memory. That work belongs
to deterministic adapters and analyzers with documented provenance and evidence
contracts.

If future AI presentation reads adapter-backed records, it may use only the normalized
documents, structured facts, existing evidence references, and provenance metadata that
the deterministic pipeline has already accepted. It must preserve the distinction
between code-backed facts, local Markdown document observations, spec-backed declared
operations, adapter-backed records, metadata-only rows, warnings, inferred relations,
uncertain signals, and not-analyzed areas.

AI output must not become connector truth. A summary of a Jira issue, GitHub pull
request, local export bundle, or Confluence page is a generated presentation over the
accepted record and its provenance, not proof that the external service is current,
reachable, complete, authoritative, or aligned with repository source. AI-generated
grouping or summarization must be labeled as non-evidence and must not create facts,
evidence records, security findings, vulnerability proof, source/spec agreement claims,
or repository-file changes.

Provider, privacy, network, credential, telemetry, and source-upload defaults remain
closed:

- no provider is configured by default;
- no network access is enabled by default;
- no source code, local document body, generated-source content, connector export,
  evidence excerpt, credential, token, cookie, or local absolute path is uploaded by
  default;
- future remote provider use must require explicit enablement, prompt-input
  minimization, documented privacy implications, and separate implementation/security
  review.

## Connector Role

Future connectors for YouTrack, Jira, Confluence, GitHub, and GitLab should produce
`SourceDocument` records plus provenance metadata. The first safe v2 implementation
candidate should be a local import adapter over user-provided export files, because it
can exercise normalization and provenance without adding network or credential behavior
to the product.

They should not:

- own the project memory schema,
- bypass evidence requirements,
- make LLM output authoritative,
- become required for local repository scanning,
- add network dependencies to the core Java/Spring analyzer,
- upload source code or generated project memory by default,
- treat connector metadata as code evidence.

## v0.8 Local Markdown Ingestion Boundary

The v0.8 local document ingestor is a deterministic local Markdown ingestor, not a
generic document search system and not an AI documentation layer. The current
implementation covers default discovery, inventory, ATX headings, bounded chunks,
document evidence, and conservative reconciliation signals.

Default discovery should be conservative:

- root `README.md` or `README.markdown`;
- README files directly under supported Maven or Gradle module roots;
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

The current v0.8 ingestor normalizes accepted Markdown files into document inventory,
deterministic ATX heading references, and bounded chunk references with
`content_status: "not_serialized"`, emits bounded `document` evidence, and emits
conservative `documents.reconciliation` rows as low-confidence uncertain inspection
hints. The ingestor must not store full document bodies, perform semantic
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

Document ingestors may provide evidence, but document evidence must be identified as
`document` evidence and kept separate from code evidence. The current local Markdown
discovery, structure, and reconciliation slice emits document evidence records for
accepted file, heading, chunk, and bounded mention observations only.

When code and documents disagree, generated memory should prefer deterministic code facts and mark document-only claims as document-backed, not code-backed.

The current v0.8 document evidence layer follows that rule by emitting document evidence
for file, heading, chunk, and bounded reconciliation mention observations only.
Document evidence should not become evidence for Java symbols, Spring annotations, build
metadata, config values, tests, OpenAPI implementation, runtime behavior, stale-document
claims, completeness claims, or source/document agreement.
