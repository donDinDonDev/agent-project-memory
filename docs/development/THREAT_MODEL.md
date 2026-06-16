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

The current core product has no network trust boundary because it does not fetch remote
resources, call external APIs, upload source, load remote config, or invoke LLMs.

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
- no generated-source content scanning by default;
- bounded parsing and bounded evidence excerpts;
- no config value extraction or environment-variable interpolation;
- no raw source bodies, document bodies, generated-source contents, command logs, stack
  traces, credentials, tokens, or secret-looking values in generated metadata or query
  output;
- evidence-backed facts remain distinct from inferred relations, uncertain signals,
  document-backed hints, graph derivation metadata, cache metadata, profile output, and
  query output.

## Planned Redaction Boundary

The planned v1.7 security track defines deterministic redaction as an output safety
boundary, not as a repository-wide secret scan.

The planned marker is:

```text
[REDACTED_SECRET_LIKE_VALUE]
```

The marker is plain generated text inside existing excerpt or rendered-output strings.
It does not require new evidence fields, a new evidence type, or a schema migration by
itself.

In scope for the planned redaction policy:

- obvious key/value or assignment-like output strings where the key name indicates a
  credential, token, password, private key, API key, client secret, or authorization
  value;
- common authorization header forms such as bearer or basic credentials;
- obvious private-key material markers when they appear in a selected output excerpt;
- generated excerpts, generated Markdown, selected profile Markdown, graph labels or
  attributes, cache/scan diagnostics, CLI stdout/stderr, and query-rendered text when
  those surfaces would otherwise print a secret-looking value.

Out of scope for the planned redaction policy:

- complete secret discovery;
- entropy-only detection;
- unlabeled or split secrets;
- proof that a repository contains no secrets;
- vulnerability findings or security correctness claims;
- path, symbol, ID, line range, confidence, uncertainty, or claim-category redaction
  when redaction would destroy evidence navigation;
- scanning generated-source contents, config values, environment variables, local
  machine state, remote services, or connector credentials.

The planned implementation should apply redaction at generation time for new artifacts
and at query render time for existing artifacts. Query render-time redaction is a
defense-in-depth presentation boundary; it must not rewrite or repair artifact files.

## Path And Symlink Audit Matrix

The planned v1.7 hardening work should audit these surfaces against the documented
target policy:

| Surface | Target policy |
| --- | --- |
| Scan root | Resolve one local repository directory and keep generated output contained under it. |
| Output directory and generated files | Keep `.project-memory/` under the scan root; reject unsafe symlink or hardlink overwrite paths. |
| Root-local scan config | Accept only one bounded YAML file under the scan root; reject absolute, escaping, generated-output, or symlinked config paths. |
| Java, Maven, Gradle, resource, and API-spec inputs | Read only documented local input classes through bounded parser policies and normalized repository-relative paths. |
| Local Markdown documents | Use default safety exclusions, user rules only for local Markdown, and no symlink following. |
| Generated-source metadata | Record path-presence metadata only; do not read generated-source contents by default. |
| Cache metadata | Keep cache files under `.project-memory/cache/v1/`; fail closed on unsafe, stale, corrupt, or inconsistent cache state. |
| Graph output | Generate navigation metadata only from existing facts, evidence IDs, and derivation metadata. |
| Agent profile output | Render selected deterministic Markdown from existing facts and evidence references only. |
| Query artifact root and files | Read only required direct child artifacts; reject symlinked artifact roots or required artifact files; never write during query. |
| CLI stdout and stderr | Keep messages deterministic and bounded; do not print stack traces, local absolute paths, raw command text, or secret-looking values. |

Findings from that audit should become bounded fixes. The audit must not expand the
product into generated-source content scanning, symlink-following modes, connector
auth, external scanning, or broad filesystem traversal without a separate public
contract update.

## Evidence And Output Limitations

Generated evidence is a local source reference, not a security proof. Evidence excerpts
may be redacted in future versions, and the marker should be read as "a secret-looking
value was masked in this selected output string", not as proof that the original value
was a valid credential or that all similar values were found.

Generated Markdown, agent profile Markdown, graph derivation metadata, cache metadata,
diagnostics, query output, release notes, chat output, and LLM output are not project
evidence.

## Reporting

Report vulnerabilities privately according to [SECURITY.md](../../SECURITY.md).
Do not include real secrets in public reports, fixtures, docs, examples, or reproduction
artifacts.
