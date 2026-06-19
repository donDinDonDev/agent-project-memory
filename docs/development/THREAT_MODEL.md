# Security Threat Model

This document describes the public security boundary for `agent-project-memory`.
It is a product threat model, not an implementation checklist or a vulnerability
guarantee.

## Product Boundary

`agent-project-memory` is a local-first CLI/devtool for deterministic Java/Spring
project-memory generation. The core analyzer reads local repository files, extracts
bounded source-visible facts, attaches evidence references, and writes local
`.project-memory/` artifacts.

The core product is not:

- a SaaS scanner;
- a hosted project-memory store;
- a vulnerability scanner;
- a complete secret detector;
- a secret inventory or credential classifier;
- a repository chatbot, generic RAG system, or LLM-backed analyzer;
- a connector, network, auth, telemetry, editor, server, or plugin platform.

## Assets To Protect

The primary security assets are:

- source files and local project documents;
- build files, resource paths, and local API specs;
- generated `.project-memory/` artifacts;
- normalized repository-relative evidence references;
- the user's local filesystem outside the scanned repository;
- terminal output and error messages;
- credentials, tokens, private keys, config values, and other sensitive values that may
  exist in repository input.

Generated evidence must stay useful even when sensitive-looking values are masked.
Evidence IDs, normalized repository-relative paths, symbols, line ranges, confidence,
uncertainty, and claim categories should remain navigable.

## Trust Boundaries

Repository files are untrusted input. A hostile repository may contain oversized files,
malformed XML/YAML/JSON/Markdown/Java, symlinks, hardlinks, confusing path names,
secret-looking literals, fake framework types, and generated outputs from other tools.

CLI arguments and root-local scan configuration are untrusted input. They must be
validated before they affect file discovery, output generation, or query behavior.

Generated artifacts are trusted only as local files produced under the documented
contract. The read-only query layer still treats artifact files as untrusted input and
must validate schema markers, required fields, IDs, graph references, evidence
references, and path safety before rendering results.

Planned workspace config and workspace artifacts add a multiple-root local trust
boundary. The workspace config, configured member roots, per-repo generated artifacts,
member `repo_id` values, and workspace diagnostics must be treated as untrusted input
until path containment, link safety, duplicate identity, nested-root, schema, and
evidence-reference checks pass.

The current core product has no network trust boundary because it does not fetch remote
resources, call external APIs, upload source, load remote config, or invoke LLMs.

The v2 local structured import adapter introduces a separate adapter trust boundary.
Adapter inputs, exported connector records, remote API responses, connector
configuration, and adapter-generated provenance must be treated as untrusted input.

The local structured import adapter adds a local-export trust boundary. Export files can
be stale, malformed, oversized, intentionally confusing, or authored by a different
system than the scanned repository. They are validated before normalization, tied to
explicit import provenance, and kept separate from repository source facts.

Future API connector modes would add remote service, network, authentication, and
credential trust boundaries. Remote API responses, pagination state, rate-limit state,
timestamps, deleted or edited records, partial exports, and service-specific IDs must be
treated as untrusted connector input, not as proof that a service is current, reachable,
complete, or authoritative.

Future plugin or public API surfaces would add an extension trust boundary. Plugin code,
plugin manifests, adapter packages, external schemas, API requests, API responses, and
agent integration inputs must remain outside the current core product until their load
path, permissions, provenance, filesystem access, network access, credential access, and
output authority are designed and reviewed.

## Security Properties

The intended security properties are:

- local-only operation by default;
- no source upload, telemetry, connector auth, or external API calls in the core
  analyzer or query layer;
- deterministic output from bounded local inputs;
- repository-root containment for scan inputs and generated outputs;
- normalized repository-relative paths in generated artifacts;
- no local absolute paths in generated artifacts or successful query output;
- no default symlink following for documents, generated-source metadata, query artifact
  roots, or required query artifact files;
- trusted local input files are accepted as source-owned only when they are regular
  files with a verifiable single-link identity; multi-link regular files and files
  whose link count cannot be verified fail closed before parsing;
- no generated-source content scanning by default;
- bounded parsing and bounded evidence excerpts;
- no config value extraction or environment-variable interpolation;
- no raw source bodies, document bodies, generated-source contents, command logs, stack
  traces, credentials, tokens, or secret-looking values in generated metadata or query
  output;
- evidence-backed facts remain distinct from inferred relations, uncertain signals,
  document-backed hints, graph derivation metadata, cache metadata, profile output, and
  query output.

Workspace security defaults:

- workspace behavior is explicitly configured and local-only;
- the first workspace config is an explicit local YAML file argument, with its parent
  directory treated as the workspace root;
- member roots are configured through normalized workspace-relative paths and required
  logical `repo_id` values;
- absolute member paths, escaping paths, symlinked roots, ambiguous nested roots,
  duplicate roots, duplicate `repo_id` values, generated-output roots, missing roots,
  and trusted multi-link or link-count-unverifiable workspace input fail closed before
  workspace output is trusted;
- the current workspace foundation validates config and member roots only and does not
  write workspace output;
- planned workspace output is written only under the workspace root
  `.project-memory/workspace-map.json` once aggregation is implemented, unless a later
  public contract changes the placement;
- normal single-repo artifacts remain unchanged, and child repository scans or child
  `.project-memory/` mutation require a separate explicit write-scope decision;
- workspace evidence references use configured `repo_id` plus existing per-repo
  evidence IDs instead of serializing local absolute paths or copying per-repo evidence
  records into the workspace artifact;
- cross-repo relation emission is parked for the first implementation slice and later
  relation families must require evidence from every participating repo or stay
  absent/uncertain;
- workspace output, diagnostics, query output, graph derivation, adapter provenance, AI
  presentation, generated Markdown, prompts, downstream agent output, and maintainer
  notes remain non-evidence.

v2 adapter security defaults:

- adapters are disabled unless explicitly configured;
- local export import is the preferred first adapter mode;
- the initial local import mode accepts only explicitly configured repository-relative
  regular files under the scanned repository root and rejects absolute paths, escaping
  paths, generated-output paths, directories, symlinked inputs, multi-link regular
  files, unverifiable link counts, and missing inputs before adapter-backed output is
  emitted;
- the current implementation reads at most 256 KiB from the configured local import
  file, parses only the documented local structured import JSON format, processes at
  most 64 records, accepts only `local_export` records with `status: "current"`, and
  rejects stale, partial, malformed, duplicate, unsupported, oversized, or
  provenance-missing records as bounded diagnostics;
- the GitHub/GitLab local export importer keeps the same local-file trust boundary: it
  accepts only an explicitly configured repository-relative JSON export, parses a
  provider-normalized format rather than raw API responses, supports only bounded issue,
  pull-request, and merge-request source types, emits Git hosting provenance through
  `source-registry.json`, and rejects records whose provider, host, namespace,
  number/IID, status, source identity, or provenance cannot be validated safely;
- the Jira/YouTrack/Confluence local export importer keeps the same local-file
  trust boundary: it accepts only an explicitly configured repository-relative JSON
  export, parses a provider-normalized format rather than raw API responses, supports
  only bounded issue, article, and page source types, emits connector provenance through
  `source-registry.json`, and rejects records whose provider, host, project or space,
  record key or ID, status, source identity, or provenance cannot be validated safely;
- adapter-enabled incremental scans skip persistent cache metadata refresh in this
  slice so configured import paths and raw adapter config values are not serialized into
  cache manifests;
- network access remains off by default and must be explicitly enabled for any future
  API connector mode;
- source upload is not a default behavior;
- credential storage, credential lookup, credential echoing, committed connector
  credentials, and raw connector request/response logs are out of scope until a
  separate credential boundary is designed;
- adapter output must preserve source provenance and must not turn external records into
  code-backed facts or authoritative evidence;
- the core analyzer and query layer must not gain network, auth, provider, or plugin
  dependencies from adapter support.

v2 external-data risk controls:

- imported records must carry source identity, import mode, content hash, import or
  fetch timestamp when known, and trust-boundary labels;
- Git hosting source identity must be a logical provider/host/namespace/record key, not
  a local path, raw URL, title, author, branch name, timestamp, or content hash;
- Jira, YouTrack, and Confluence source identity must be a logical
  provider/host/container/record key, not a local path, raw URL, title, author, workflow
  state, label, page title, timestamp, attachment name, or content hash;
- accepted adapter-backed records must reference source-document and provenance IDs
  from the source registry; provenance must not be hidden only in free-form document
  text or evidence excerpts;
- stale, partial, deleted, edited, or source-system-disconnected records must remain
  provenance-backed observations rather than current-state claims;
- malicious exported records and connector content must not inject instructions into
  generated Markdown, query output, optional AI prompts, plugin manifests, or future API
  responses;
- external text must not override source-visible repository facts, evidence records,
  confidence labels, uncertainty labels, or provenance labels;
- connector summaries, AI summaries, query output, graph metadata, cache metadata,
  profile Markdown, release notes, and chat output remain non-evidence;
- adapter-backed observations must be rejected or marked uncertain when provenance is
  missing, ambiguous, contradictory, or outside the configured import boundary.
- Git hosting titles may be serialized only as bounded redacted display metadata; raw
  issue bodies, pull-request or merge-request descriptions, comments, review notes,
  labels, authors, branch names, commit metadata, pipeline/status payloads, raw export
  objects, configured import paths, and raw request/response logs must not be serialized
  by default.
- Jira, YouTrack, and Confluence titles or summaries may be serialized only as bounded
  redacted display metadata; raw issue/page/article bodies, descriptions, comments,
  rendered HTML, rich text, attachment names, attachment bodies, labels, authors,
  workflow history, raw export objects, configured import paths, and raw request/response
  logs must not be serialized by default.

Planned v2 credential and network defaults:

- network access is disabled by default for adapters, connectors, optional AI, plugin
  surfaces, public API surfaces, telemetry, update checks, and remote configuration;
- future networked modes must require explicit user configuration and must identify
  which source, provider, or service is contacted;
- credentials must not be accepted from committed repository files, generated artifacts,
  local export bundles, plugin manifests, prompts, or connector records by default;
- credential lookup, storage, rotation, validation, redaction, and error reporting must
  be designed before any future authenticated connector, provider, plugin, or API mode
  is implemented;
- credentials, tokens, cookies, authorization headers, raw request/response logs, local
  absolute paths, and raw connector configuration values must not be serialized into
  provenance, evidence, generated artifacts, query output, AI prompts, or public logs by
  default.
- normalized adapter bodies are in-memory input only by default; generated artifacts may
  serialize bounded redacted display metadata and hashes, but must not serialize full
  connector exports, raw source/document bodies, prompt transcripts, or local absolute
  paths by default.

Optional AI security defaults:

- AI presentation is disabled unless explicitly enabled;
- no real AI provider is configured by default;
- the first AI presentation surface is a separate optional generated artifact
  directory under `.project-memory/ai-presentations/`, not a `project-map.json`,
  `evidence-index.jsonl`, source registry, profile, cache, or query mutation;
- network access, provider credentials, telemetry, and source upload remain off by
  default;
- the core analyzer, query layer, and adapter normalization path must not require AI;
- AI inputs must be minimized to deterministic generated memory, existing evidence
  references, bounded already-serialized excerpts, and accepted provenance metadata;
- raw repository source files, raw local document bodies, generated-source contents,
  connector exports, connector credentials, raw connector request/response logs, local
  absolute paths, and raw prompt transcripts must not be uploaded or serialized by
  default;
- provider configuration, credential lookup, retention/privacy claims, network behavior,
  and real-provider prompt-input policy must be designed and reviewed before any future
  provider mode is implemented;
- AI output must be labeled as non-evidence and must not create project facts, evidence
  records, connector truth, security findings, vulnerability proof, runtime claims, or
  code modifications.

AI provider mode taxonomy:

- absent or `none`: no AI presentation mode is enabled, no provider is configured, and
  no AI presentation artifacts are emitted;
- `mock_no_network`: the current test/local presentation provider exercises prompt and
  output plumbing without network access, credentials, telemetry, source upload,
  provider SDKs, or provider privacy claims;
- real provider modes: parked until a separate design defines explicit enablement,
  contacted service, request minimization, credential lookup, timeout/retry behavior,
  telemetry defaults, prompt transcript policy, retention/training-use wording limits,
  diagnostics, and release review requirements.

Provider mode must not be inferred from repository files, generated artifacts, adapter
records, prompts, environment variables, credentials, plugin manifests, or a provider
SDK on the classpath. Any future provider-backed mode must fail closed when provider
configuration, credential source, network scope, source-upload behavior, prompt
transcript policy, or retention/privacy wording is undefined.

AI surfaces introduce prompt and content-injection risk even when they read only
generated memory. Repository text, local documents, adapter-backed records, evidence
excerpts, and connector content must be treated as untrusted content, not executable
instructions. An AI layer must not obey repository-provided instructions to change
files, reveal credentials, fetch network resources, alter evidence, override
provenance, or mark AI output as authoritative.

Prompt assembly, if implemented, must keep untrusted content and control instructions
separate. Evidence excerpts, document titles, connector titles, adapter display
metadata, source-derived paths, generated Markdown, and user labels may be quoted or
summarized only within the documented input scope. They must not be allowed to request
additional file reads, network calls, provider configuration, credential lookup,
artifact rewrites, evidence changes, provenance changes, plugin loading, repository
chat behavior, generic RAG behavior, or automatic code modification. Raw prompt
transcripts must not be serialized by default; bounded prompt template identifiers or
mode labels may be recorded only as generated-output metadata when the output contract
allows them.

Planned plugin and API surface gates:

- no plugin code loading, plugin marketplace, extension runtime, server mode, or public
  API service is part of the current core product;
- a future plugin or API design must define a default-deny permission model before code
  exists, including filesystem scope, network scope, credential scope, generated-output
  write scope, and whether plugins may emit adapter-backed records;
- plugins must not bypass adapter validation, evidence requirements, provenance labels,
  redaction boundaries, path containment, or no-source-upload defaults;
- plugin manifests, API requests, API responses, connector records, prompts, and local
  repository text must be treated as untrusted input and must not become executable
  instructions;
- any future plugin or API output must be labeled by source and authority, and must not
  create Java/Spring facts, evidence records, connector truth, security findings,
  vulnerability proof, runtime claims, or repository-file changes unless a later public
  contract explicitly designs that behavior.

Read-only agent consumption defaults:

- agent consumption starts from a CLI-only query surface over existing generated
  project-memory artifacts, not from a server, socket listener, daemon, editor plugin,
  or plugin runtime;
- the first implemented agent context surface reads only documented generated artifacts from the
  approved artifact root and must not read repository source files, generated-source
  contents, raw local document bodies, raw connector exports, raw adapter input files,
  raw prompt transcripts, environment values, credentials, or local absolute paths to
  expand its output;
- agent-facing output is deterministic navigation and presentation metadata only. It may
  reference existing evidence IDs and artifact names, but it must not create evidence
  records, strengthen evidence, alter provenance, create connector truth, create
  security findings, claim runtime behavior, or provide code-change authority;
- all agent-facing labels, prompts, editor labels, generated Markdown, AI presentation,
  adapter records, connector text, and future API or MCP requests are untrusted content
  and must not become executable instructions that request file writes, network calls,
  credential lookup, evidence changes, provenance changes, artifact rewrites, or
  repository edits;
- MCP, server, public API, editor plugin, network, authentication, credentials,
  telemetry, source upload, raw prompt transcript serialization, and automatic code
  modification remain out of the first slice and require a separate design and
  release-level security review before implementation.

Workspace memory defaults:

- workspace memory starts from explicit local config and generated artifacts, not remote
  repository discovery, organization crawling, network shares, provider APIs, SaaS
  indexing, repository chat, generic RAG, semantic search, embeddings, or vector stores;
- workspace output must not serialize local absolute paths, raw config contents, raw
  source bodies, raw local document bodies, raw connector exports, raw adapter input
  files, command logs, credentials, tokens, cookies, authorization headers, prompts, or
  maintainer notes;
- workspace query, workspace agent context, workspace graph output, adapter-aware
  workspace context, child-repo scan refresh, and cross-repo relation emission remain
  separate later design decisions unless a bounded public contract accepts them;
- workspace relations must not be inferred from name similarity, package similarity,
  generated Markdown, query output, graph derivation, adapter records, AI output,
  prompts, connector text, issue text, release notes, chat output, or maintainer notes.

Future v2 security review expectations:

- design-only documentation changes can be reviewed with a lightweight documentation
  security assessment when they do not change implementation behavior;
- implementation diffs that touch adapters, parsers, import validation, filesystem/path
  handling, output rendering, evidence serialization, redaction, credentials, network
  access, plugin loading, provider calls, API/server surfaces, dependencies, scripts, or
  command execution need a focused implementation security review before release;
- major or high-risk release gates that introduce networked connectors, authentication,
  credential handling, plugin execution, provider-backed AI, public API/server surfaces,
  or broad ingestion changes need a broader release-level security review before tag or
  publication;
- review is blocked when credential handling is undefined, network defaults are not
  explicitly off, plugin authority is unclear, or external data can bypass evidence and
  provenance requirements.

## Redaction Boundary

The v1.7.0 release defines deterministic redaction as an output safety
boundary, not as a repository-wide secret scan.

The marker is:

```text
[REDACTED_SECRET_LIKE_VALUE]
```

The marker is plain generated text inside existing excerpt or rendered-output strings.
It does not require new evidence fields, a new evidence type, or a schema migration by
itself.

In scope for the redaction policy:

- obvious key/value or assignment-like output strings where the key name indicates a
  credential, token, password, private key, API key, client secret, or authorization
  value;
- common authorization header forms such as bearer or basic credentials;
- obvious private-key material markers when they appear in a selected output excerpt;
- generated excerpts, generated Markdown, selected profile Markdown, graph labels or
  attributes, cache/scan diagnostics, CLI stdout/stderr, and query-rendered text when
  those surfaces would otherwise print a secret-looking value.

Out of scope for the redaction policy:

- complete secret discovery;
- entropy-only detection;
- unlabeled or split secrets;
- proof that a repository contains no secrets;
- vulnerability findings or security correctness claims;
- path, symbol, ID, line range, confidence, uncertainty, or claim-category redaction
  when redaction would destroy evidence navigation;
- scanning generated-source contents, config values, environment variables, local
  machine state, remote services, or connector credentials.

The implementation applies redaction at generation time for selected new generated
artifacts and at query render time for selected existing artifacts. Query render-time
redaction is a defense-in-depth presentation boundary; it must not rewrite or repair
artifact files.

## Path And Symlink Audit Matrix

The v1.7 hardening work audits these surfaces against the documented target policy:

| Surface | Target policy |
| --- | --- |
| Scan root | Resolve one local repository directory and keep generated output contained under it. |
| Output directory and generated files | Keep `.project-memory/` under the scan root; reject unsafe symlink or hardlink overwrite paths. |
| Root-local scan config | Accept only one bounded YAML file under the scan root; reject absolute, escaping, generated-output, symlinked, multi-link, or link-count-unverifiable config paths. |
| Java, Maven, Gradle, resource, and API-spec inputs | Read only documented local input classes through bounded parser policies, normalized repository-relative paths, no symlink following, and verified single-link regular-file checks. |
| Local Markdown documents | Use default safety exclusions, user rules only for local Markdown, no symlink following, and verified single-link regular-file checks. |
| Generated-source metadata | Record path-presence metadata only; do not read generated-source contents by default. |
| Cache metadata | Keep cache files under `.project-memory/cache/v1/`; fail closed on unsafe, stale, corrupt, or inconsistent cache state. |
| Graph output | Generate navigation metadata only from existing facts, evidence IDs, and derivation metadata. |
| Agent profile output | Render selected deterministic Markdown from existing facts and evidence references only. |
| Query artifact root and files | Read only required direct child artifacts; reject symlinked artifact roots and symlinked, multi-link, or link-count-unverifiable required artifact files; never write during query. |
| CLI stdout and stderr | Keep messages deterministic and bounded; do not print stack traces, local absolute paths, raw command text, or secret-looking values. |

Findings from that audit become bounded fixes. The audit must not expand the
product into generated-source content scanning, symlink-following modes, connector
auth, external scanning, or broad filesystem traversal without a separate public
contract update.

## Evidence And Output Limitations

Generated evidence is a local source reference, not a security proof. Evidence excerpts
may be redacted in v1.7.0 and later versions, and the marker should be read as
"a secret-looking value was masked in this selected output string", not as proof that
the original value was a valid credential or that all similar values were found.

Generated Markdown, agent profile Markdown, graph derivation metadata, cache metadata,
diagnostics, query output, release notes, chat output, and LLM output are not project
evidence.

## Reporting

Report vulnerabilities privately according to [SECURITY.md](../../SECURITY.md).
Do not include real secrets in public reports, fixtures, docs, examples, or reproduction
artifacts.
