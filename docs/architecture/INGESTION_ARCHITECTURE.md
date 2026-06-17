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

The planned v2.0 lifecycle is:

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

Future external ingestors and any broader local document modes should normalize inputs
into a `SourceDocument` abstraction. For v2 design, `SourceDocument` should be treated
as an adapter-domain contract object for future explicit adapters, not as an enabled
reader, parser, or generated-output integration point.

The initial contract foundation validates deterministic source-document identity and
required provenance references for accepted adapter records. The current adapter config
safety layer adds disabled-by-default selection and repository-relative local import
path validation before any adapter reader or parser exists. It still does not read local
import contents, normalize records, emit `.project-memory/source-registry.json`, add
adapter-backed `project-map.json` fact sections, or change no-adapter scan/query
behavior.

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
  bodies must not be serialized by default.
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

## External Data Risk Controls

Local export files and future remote API responses must be treated as untrusted records.
They can be stale, malicious, partially exported, deleted after export, edited between
fetches, inconsistent with repository source, or intentionally shaped to confuse
Markdown rendering, optional AI prompts, provenance joins, or future API responses.

Future adapter contracts should require deterministic validation before normalization:
bounded file sizes and record counts, explicit source kind, explicit import mode,
content hash, import or fetch timestamp when known, and provenance labels that keep
local repository files, out-of-repository local exports, and remote API responses
separate. Missing or ambiguous provenance should block the record or keep it as an
uncertain warning/status row, not a fact.

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
