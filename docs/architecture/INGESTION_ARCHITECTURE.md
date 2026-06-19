# Ingestion Architecture

The current product focuses on local repository Java/Spring source files, Maven build
files, bounded static Gradle build files, standard Maven or Gradle source/test/resource
roots, bounded local OpenAPI/Swagger spec inputs, conservative default-scope local
Markdown discovery, and deterministic generated project-memory output. The current v1.x
line emits local Markdown document inventory, deterministic ATX heading references,
bounded chunk references, resolving document evidence, conservative code-doc
reconciliation signals, and compact local documentation guide rendering from structured
document facts and evidence only.

The v2 line adds disabled-by-default local import adapters for explicitly configured
repository-relative export files. v2.0 ships a local structured import reference
adapter, and v2.1 adds a Git hosting local JSON export import adapter. The v2.2
connector boundary keeps Jira, YouTrack, and Confluence local export import ahead of any
networked API mode. Git hosting, issue-tracker, and wiki API/network connectors remain
future input adapters. They should not be part of the MVP core analyzer, and they should
not be required to generate `.project-memory/` from a Java/Spring repository.

## v2 Adapter Boundary

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
  connector configuration values in generated output;
- reject, cap, or mark uncertain stale, malformed, partial, contradictory, or
  provenance-missing records before they reach generated memory.

Adapters must not:

- create code-backed Java/Spring facts;
- bypass evidence or provenance requirements;
- mutate repository source files;
- become required for normal `scan <path>` behavior;
- add network, auth, AI, or plugin dependencies to the core analyzer;
- make external text, connector summaries, query output, or AI output authoritative;
- load plugin code or expose an API/server trust boundary without a separate permission
  and security design.

The v2.0 lifecycle is:

1. No adapter is selected unless configuration explicitly enables one. A scan with no
   adapter configuration follows the current Java/Spring pipeline and does not create
   adapter artifacts, adapter diagnostics, network activity, credentials, plugin
   loading, AI provider use, or source upload.
2. Adapter configuration is validated before any adapter reads input. The initial v2.0
   local import boundary accepts only configured repository-relative regular files
   under the scanned repository root, rejects escaping paths, absolute paths, generated
   output paths, directories, and symlinked inputs, and does not accept credentials.
3. The adapter validates import mode, source kind, size and record-count limits, and
   required source identity before normalization. Unsafe, missing, or malformed
   configured inputs fail closed before adapter-backed output is emitted. Malformed,
   oversized, partial, contradictory, stale, or provenance-missing records inside an
   otherwise accepted input are rejected, capped, or emitted only as bounded
   warning/status material.
4. The adapter normalizes accepted records into `SourceDocument` records and separate
   provenance records. Normalized bodies are in-memory analyzer input only and are not
   serialized by default.
5. Deterministic ingestion consumes adapter output only through documented
   document/spec/metadata boundaries. Adapter-backed material can become
   provenance-backed external/document context, metadata-only rows, warnings, or
   uncertain inspection hints, but not code-backed Java/Spring facts.
6. Serialization writes adapter-backed records only through the output and provenance
   strategy documented in `OUTPUT_CONTRACT.md` and the evidence strategy documented in
   `EVIDENCE_MODEL.md`.

## SourceDocument

External ingestors and any broader local document modes should normalize inputs into a
`SourceDocument` abstraction. For v2 design, `SourceDocument` is an adapter-domain
contract object for explicit adapters, not a replacement for code-backed Java/Spring
facts.

The initial contract foundation validates deterministic source-document identity and
required provenance references for accepted adapter records. The current local
structured import adapter adds disabled-by-default selection, repository-relative local
import path validation, bounded JSON parsing, source-document normalization,
`.project-memory/source-registry.json` emission, and `project-map.json` adapter context
for accepted records. No-adapter scan behavior remains unchanged, and the query layer
continues to support the no-adapter `schema_version: "1.0"` artifact contract.

Fields stable enough for the v2.0 design are:

- `id`: deterministic source-document identifier derived from adapter identity, import
  mode, source type, and a normalized logical source identity such as a source-system
  record ID, source namespace plus record ID, source URL key, or repository-relative
  local import record key. Import timestamp, local absolute path, and content hash must
  not be the primary identifier when a stable logical source identity exists.
- `sourceType`: source category, such as `local_export`, `youtrack_issue`,
  `jira_issue`, `confluence_page`, `github_issue`, `github_pull_request`,
  `gitlab_issue`, or `gitlab_merge_request`. Existing default-scope local Markdown
  remains owned by the current local document ingestor rather than the v2 adapter layer.
- `sourceIdentity`: normalized source-system identity used to derive `id`. Accepted
  adapter records must have this identity; records without one are rejected or kept as
  bounded warning/status material.
- `title`: bounded, redacted display title when known.
- `contentHash`: hash of the normalized content used by the adapter.
- `contentStatus`: default `"not_serialized"` for generated artifacts. Future values
  such as bounded excerpts require an explicit output contract update before use.
- `provenanceId`: stable reference to a separate provenance record in the source
  registry.

Fields that are analyzer-internal or postponed beyond the initial v2.0 boundary are:

- `body` or `normalizedBody`: normalized text body used in memory by an analyzer. Full
  bodies are not serialized by the local structured import adapter.
- `localPath`: allowed only as a repository-relative configured import path for the
  initial v2.0 local import mode. Out-of-repository local export paths are postponed
  until a later path-safety design; local absolute paths must not be serialized.
- `url`: provenance metadata when applicable, not source truth and not reachability
  proof.
- `sourceId`: provenance metadata when applicable; the normalized
  `sourceIdentity` remains the generated-output join key.
- `createdAt`, `updatedAt`, `exportedAt`, `fetchedAt`, `tags`, `adapterName`, and
  `adapterVersion`: provenance or bounded metadata owned by the source registry rather
  than free-form document text.

`SourceDocument` IDs should be stable across regenerated outputs for the same adapter,
import mode, source type, and logical source identity. If a record's content changes,
`contentHash` changes while `id` remains stable. If a record lacks a stable logical
source identity, it must not be accepted as a normal adapter-backed record.

## Connector Provenance

Connector provenance should be emitted in a separate source registry rather than hidden
inside free-form document text or evidence excerpts. At minimum, v2.0 provenance should
identify:

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

For the initial v2.0 implementation, provenance is required for every accepted
adapter-backed record and is referenced by `provenanceId`. Missing or ambiguous
provenance blocks normal record acceptance. API import provenance remains future work;
the v2.0 reference mode is local structured import with network marked as
not applicable or disabled.

## v2.1 Git Hosting Local Export Import

The v2.1 Git hosting adapter starts with a normalized local JSON export format, not raw
GitHub or GitLab API responses and not live API fetching. The supported format is:

```text
agent-project-memory.git_hosting_export.v1
```

The import file is one explicitly configured repository-relative JSON file under the
scan root. It is untrusted local input and must pass the same regular-file,
single-link, no-follow, size, record-count, duplicate, and bounded parsing gates used by
the v2 adapter boundary before any record is normalized.

The format is provider-normalized rather than API-shape-normalized. Records must carry
the provider and stable logical identity fields needed to derive a `SourceDocument`
identity:

- `provider`: `github` or `gitlab`.
- `host`: normalized host, such as `github.com`, `gitlab.com`, or a bounded safe
  self-hosted domain. It is provenance metadata, not reachability proof.
- `namespace`: GitHub repository namespace such as `owner/repo`, or GitLab project path
  such as `group/subgroup/project`.
- `record_type`: `issue`, `pull_request`, or `merge_request`.
- `number` for GitHub issues and pull requests, or `iid` for GitLab issues and merge
  requests.
- `status`: `current` for accepted records; stale, partial, unsupported, ambiguous, or
  missing-status records are rejected or represented only as bounded diagnostics.
- `exported_at`: import snapshot timestamp when known and safe to parse.

The accepted source types are:

- `github_issue`
- `github_pull_request`
- `gitlab_issue`
- `gitlab_merge_request`

The primary `sourceIdentity` is derived from provider, host, namespace, record type, and
record number or IID, for example
`git-hosting/github/github.com/owner/repo/issue/123`. Local file paths, absolute paths,
raw URLs, local export filenames, timestamps, titles, content hashes, branch names, and
author names must not be primary identities. Records without a stable safe identity are
not accepted as normal adapter-backed records.

Titles may be serialized only as bounded redacted display metadata. Bodies, comments,
review notes, descriptions, labels, author names, branch names, commit metadata,
pipeline/status payloads, and raw provider export objects are normalized only as
untrusted adapter input. The first Git hosting import boundary may use them only for
content hashing, bounded counts, or diagnostics. Any later in-memory analysis over raw
Git hosting text requires a separate contract before implementation, and raw text must
not be serialized by default. Comments and reviews are part of the parent
issue/pull-request/merge-request source document in the first slice; they do not become
separate source-document types unless a later contract explicitly adds that model.

Generated output placement remains the v2 adapter placement:

- accepted records are emitted through `.project-memory/source-registry.json`;
- `project-map.json` uses the existing top-level `adapter_context` shape with
  `schema_version: "2.0"`;
- `adapter_context.items[]` reference `source_document_ids` and `provenance_ids`;
- no Git hosting record carries `evidence_ids`;
- no Git hosting record becomes a Java/Spring endpoint, component, repository, entity,
  build, config, test, document-evidence, quality, graph, query, or security fact.

Git hosting provenance adds provider-specific metadata inside the source registry,
such as provider, host, namespace, record type, record number or IID, sanitized source
URL when safe, exported timestamp, record updated timestamp when known, stale/current
snapshot status, and trust-boundary labels. These fields are generated provenance and
review metadata only. They do not prove that GitHub or GitLab is reachable, current,
complete, authoritative, or aligned with repository source.

The v2.1 config shape uses a disabled-by-default adapter key:

```yaml
adapters:
  git_hosting_import:
    enabled: true
    path: exports/git-hosting.json
```

The `path` value is a repository-relative local JSON export path. The config must not
accept credentials, environment-variable interpolation, token names, token values,
remote URLs, global/user-home config, generated-output paths, or API/network options.
Live API mode, credential lookup, OAuth, PATs, GitHub App auth, GitLab tokens,
rate-limit state, retries, background sync, remote cache, and out-of-repository export
paths remain separate later design work.

## v2.2 Jira, YouTrack, And Confluence Local Export Import

The v2.2 connector boundary starts with one normalized local JSON export format,
not raw Jira, YouTrack, or Confluence API responses and not live API fetching. The
format name is:

```text
agent-project-memory.connector_export.v1
```

The import file remains one explicitly configured repository-relative JSON file under
the scan root. It is untrusted local input and must pass the same regular-file,
single-link, no-follow, size, record-count, duplicate, and bounded parsing gates used by
the existing v2 adapter boundary before any record is normalized.

The first local export slice supports these adapter source types:

- `jira_issue`
- `youtrack_issue`
- `youtrack_article`
- `confluence_page`

The shared export format should be provider-normalized. A record must carry the
provider and stable logical identity fields needed to derive a `SourceDocument`
identity:

- `provider`: `jira`, `youtrack`, or `confluence`.
- `host`: normalized host for the local export's source system. It is provenance
  metadata, not reachability proof.
- `record_type`: `issue`, `article`, or `page`.
- `status`: `current` for accepted records; stale, partial, unsupported, ambiguous, or
  missing-status records are rejected or represented only as bounded diagnostics.
- `exported_at`: import snapshot timestamp when known and safe to parse.

Provider-specific identity fields should be required by source type:

- `jira_issue`: Jira project key and issue key, with provider issue ID kept as
  provenance metadata when present.
- `youtrack_issue`: YouTrack project key or project ID plus readable issue ID or stable
  issue ID.
- `youtrack_article`: YouTrack project, article collection, or scope key plus stable
  article ID.
- `confluence_page`: Confluence space key plus stable page ID.

The primary `sourceIdentity` is derived from provider, host, source type, container key,
and stable record key or ID, for example:

```text
connector/jira/jira.example.com/project/PROJ/issue/PROJ-123
connector/youtrack/youtrack.example.com/project/PROJ/issue/PROJ-123
connector/youtrack/youtrack.example.com/project/PROJ/article/ABC123
connector/confluence/confluence.example.com/space/ENG/page/123456
```

Local file paths, absolute paths, raw URLs, local export filenames, timestamps, titles,
author names, mutable workflow states, labels, content hashes, and page titles must not
be primary identities. Records without a stable safe identity are not accepted as normal
adapter-backed records.

Titles or summaries may be serialized only as bounded redacted display metadata.
Issue/page bodies, descriptions, comments, rich text, rendered HTML, attachment names,
attachment bodies, labels, author names, workflow history, raw provider export objects,
and raw request/response logs are normalized only as untrusted adapter input. The first
local export boundary may use accepted body or comment text only for content hashing,
bounded counts, or diagnostics. Raw text, rich text, comments, and attachment details
must not be serialized by default. Comments stay part of the parent issue/article/page
source document in the first slice; they do not become separate source-document types
unless a later contract explicitly adds that model. Attachments are outside the first
slice except for bounded diagnostics or aggregate counts that do not expose names,
paths, URLs, or content.

Generated output placement remains the v2 adapter placement:

- accepted records are emitted through `.project-memory/source-registry.json`;
- `project-map.json` uses the existing top-level `adapter_context` shape with
  `schema_version: "2.0"`;
- `adapter_context.items[]` reference `source_document_ids` and `provenance_ids`;
- no Jira, YouTrack, or Confluence record carries `evidence_ids`;
- no connector record becomes a Java/Spring endpoint, component, repository, entity,
  build, config, test, document-evidence, quality, graph, query, source/spec agreement,
  documentation freshness, runtime, security, or automatic code-modification fact.

Connector provenance adds provider-specific metadata inside the source registry, such
as provider, host, source family, container type and key, record type, record key or ID,
sanitized source URL when safe, exported timestamp, record updated timestamp when known,
snapshot status, and trust-boundary labels. These fields are generated provenance and
review metadata only. They do not prove that Jira, YouTrack, or Confluence is reachable,
current, complete, authoritative, or aligned with repository source.

The v2.2 config shape uses a disabled-by-default adapter key:

```yaml
adapters:
  connector_import:
    enabled: true
    path: exports/connectors.json
```

The `path` value is a repository-relative local JSON export path. The config must not
accept credentials, environment-variable interpolation, token names, token values,
remote URLs, global/user-home config, generated-output paths, API/network options,
background sync settings, retry/rate-limit settings, pagination settings, remote cache
settings, or provider discovery options. Live API mode, credential lookup, OAuth, PATs,
app passwords, API keys, cookies, rate-limit state, retries, background sync, remote
cache, and out-of-repository export paths remain separate later design work.

## External Data Risk Controls

Local export files and future remote API responses must be treated as untrusted records.
They can be stale, malicious, partially exported, deleted after export, edited between
fetches, inconsistent with repository source, or intentionally shaped to confuse
Markdown rendering, optional AI prompts, provenance joins, or future API responses.

Adapter contracts require deterministic validation before normalization: bounded file
sizes and record counts, explicit source kind, explicit import mode, content hash, and
provenance labels that keep local repository files, out-of-repository local exports, and
remote API responses separate. The current local structured import adapter reads at most
256 KiB, processes at most 64 records, accepts only the
`agent-project-memory.local_structured_import.v1` format, accepts only
`source_type: "local_export"` records with `status: "current"`, rejects stale, partial,
malformed, duplicate, unsupported, oversized, or provenance-missing records as bounded
diagnostics, and fails closed for unsupported top-level import files. Missing or
ambiguous provenance blocks the record or keeps it as a warning/status row, not a fact.

External content must not become executable instruction text. Adapter records, issue
comments, page bodies, titles, labels, exported Markdown, and connector metadata must
not be allowed to override evidence records, confidence, uncertainty, redaction,
network settings, credential handling, plugin permissions, or generated-output
authority.

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
`SourceDocument` records plus provenance metadata. The first v2 implementation is a
local structured import adapter over user-provided repository-relative export files; it
exercises normalization and provenance without adding network or credential behavior to
the product.

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
