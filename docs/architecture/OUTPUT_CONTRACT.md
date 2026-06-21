# Output Contract

This document defines the first `.project-memory/` output structure.

Any output field addition, removal, rename, or semantic change requires updating this file. Tests should be updated at the same time once implementation begins.

## Directory Structure

The current scan output uses the base project-memory files plus the v1.5 graph
artifact:

```text
.project-memory/
  project-map.json
  project-graph.json
  evidence-index.jsonl
  endpoints.md
  agent-guide.md
```

In the current implementation, `scan <path>` writes all base files when supported Maven
or Gradle module roots, supported root source, test, or resource roots, supported config
files, Maven/Gradle module warnings, or graph-supported facts are detected. Unsupported
directories still only get a prepared `.project-memory/` directory and do not get
contract output files.
When `--agent-profile` is selected and the scan writes the base contract files, the
current v1.3 development profile layer also writes
`.project-memory/agent-profiles/manifest.json` and selected deterministic profile
Markdown files under `.project-memory/agent-profiles/`. Profile Markdown is generated
from existing structured project facts and existing evidence references only. A scan
without `--agent-profile` does not create profile artifacts, and unsupported
directories that only prepare `.project-memory/` do not create orphan profile
artifacts.

The v2 local import adapter source registry is a separate optional artifact:

```text
.project-memory/source-registry.json
```

The current implementation emits this artifact only when an adapter such as
`adapters.local_structured_import`, `adapters.git_hosting_import`, or
`adapters.connector_import` is explicitly enabled and the configured import file is
accepted for reading. A normal scan with no adapter explicitly enabled keeps the
current base artifact set and must not create a source registry.

The initial adapter-domain contract foundation added source-document/provenance
identity validation for adapters. The current local import layer adds bounded
`scan.features.adapters` metadata for disabled-by-default selection, validated local
import counts, bounded parsing, `source-registry.json` emission, and
`project-map.json` adapter context for accepted adapter-backed records. Scans without an
explicitly enabled adapter still do not emit `source-registry.json` or adapter-backed
`project-map.json` sections.

The current v2.5 workspace implementation accepts `workspace scan <config>`, validates
the explicit local workspace config, and writes a separate workspace-root artifact:

```text
.project-memory/workspace-map.json
```

This artifact is not emitted by normal single-repo scans. It belongs to the explicit
workspace workflow, is written under the workspace root, and references existing
per-repo generated artifacts through configured logical repo identity rather than local
absolute paths. Workspace map aggregation does not run child repository scans and does
not mutate member `.project-memory/` directories. Existing per-repo `.project-memory/`
artifact names and schema markers remain unchanged by the workspace design.

The planned v2.6 change-impact boundary is a read-only query workflow over existing
single-repo generated artifacts. It does not add a generated artifact in the first
slice. A future generated impact report, if accepted, must update this file before
implementation.

The v2.7 policy profile boundary does not add a generated artifact. When a
policy profile is explicitly selected, the accepted placement for selected profile
metadata is the existing `project-map.json` top-level `scan` object. A normal scan with
no policy profile remains the compatibility baseline and does not gain default
policy-profile metadata in the v2.7 boundary.

## Planned v3 Schema/API Migration Design

This section is a design plan for a future v3 implementation. It is not current shipped
behavior. Current no-adapter scans still emit `project-map.json` with
`schema_version: "1.0"`. Current adapter-enabled scans still emit
`project-map.json` with `schema_version: "2.0"` plus the optional
`source-registry.json` contract described below. Current query, agent-context, and
impact commands still support the schema markers documented in their existing sections.

The future v3 contract should be implemented as one coherent artifact set rather than
as isolated per-file changes. A v3-capable generator, reader, or query command must not
silently mix current v1-compatible, current v2 adapter-enabled, and future v3 artifacts.
The primary migration action for existing users should be regeneration from source and
from explicitly configured local adapter exports, not in-place mutation of an existing
`.project-memory/` directory.

Planned v3 artifact-set decisions:

- `project-map.json` should receive a future major schema marker such as
  `schema_version: "3.0"` only when v3 serialization, reading, compatibility tests, and
  migration documentation are implemented together.
- The v3 implementation should add either a required artifact-set manifest or an
  equivalent documented set-level validation mechanism before accepting v3 artifacts as
  a coherent machine-readable set. That mechanism must identify the project-map schema,
  evidence model version, graph schema, optional source-registry schema, optional
  workspace schema, optional profile/AI/cache surfaces, and the tool version that wrote
  them, without relying on local absolute paths or command transcripts.
- `evidence-index.jsonl` remains the source-backed evidence artifact. If v3 changes
  evidence fields, IDs, confidence labels, uncertainty semantics, excerpts, or source
  type taxonomy, those changes must be documented in this file and in
  `EVIDENCE_MODEL.md` before implementation. A manifest-level evidence model marker is
  acceptable only as metadata; it must not make non-evidence artifacts authoritative.
- `source-registry.json` remains the home for adapter source documents, adapter runs,
  and provenance. v3 may bump the source-registry schema only with explicit join-key,
  source-document identity, provenance metadata, rejection, and regeneration tests.
  Adapter records must remain provenance-backed context unless a later evidence contract
  explicitly changes that boundary.
- `project-graph.json`, `workspace-map.json`, profile manifests, AI presentation
  manifests, and cache manifests should keep their own schema markers. Any v3 marker
  bump for those surfaces must name the affected fields, old behavior, new behavior,
  supported reader behavior, and migration action.

Planned migration and compatibility behavior:

- Existing no-adapter artifact sets using `schema_version: "1.0"` remain the current
  compatibility baseline for released v1.x/v2.x behavior. A future v3 tool should not
  reinterpret those artifacts as v3 output. Consumers that need v3 semantics should
  regenerate the full artifact set with the v3 tool.
- Existing adapter-enabled artifact sets using `project-map.json`
  `schema_version: "2.0"` and `source-registry.json` `1.0`, `1.1`, or `1.2` should be
  treated as pre-v3 adapter output. The planned v3 migration action is to rerun the scan
  with the same explicit local adapter input configuration and regenerate both
  project-map and source-registry output together.
- Query, agent-context, and impact readers should fail closed on unsupported or mixed
  artifact schema markers unless a specific legacy compatibility mode is implemented
  and tested. Text query output remains a human-readable interface unless a future JSON
  query envelope is explicitly designed and versioned.
- Workspace migration should regenerate member repositories first and then regenerate
  the workspace map from the explicit workspace config. Workspace output must continue
  to use logical repo identity and composite evidence references rather than local
  absolute paths.
- Cache migration should invalidate pre-v3 cache metadata. A v3 cache contract must
  either include the v3 artifact-set identity in its input/output fingerprints or keep
  cache metadata disabled for surfaces it cannot validate.
- Agent profile Markdown and AI presentation artifacts should be regenerated from v3
  artifacts. They remain derived, non-authoritative, and non-evidence. They must not be
  used as a migration source for project facts.

Planned deprecations for v3 consumers:

- treating generated Markdown, query text, profile output, AI presentation output,
  cache metadata, release notes, or adapter provenance as stable project-fact evidence;
- accepting unknown schema markers or mixed artifact sets by best-effort parsing;
- using `source-registry.json` rows as Java/Spring source facts or `evidence_ids`;
- relying on v1 cache metadata, profile manifests, or AI presentation manifests after a
  major artifact contract change without regeneration;
- parsing Markdown files as the stable machine API when JSON/JSONL artifacts exist.

The v3 compatibility test plan must include no-adapter regeneration, adapter-enabled
regeneration, mixed-artifact rejection, unsupported-schema rejection, evidence-reference
resolution, provenance join resolution, workspace composite-reference checks, cache
invalidation, profile/AI regeneration, and query/agent-context/impact support or
rejection behavior. These tests belong to the future implementation work, not this
design-only section.

## v2 Adapter Output Boundary

The current implementation does not emit adapter packages, network connector output,
adapter evidence records, connector credentials, network metadata, or AI provider
metadata. Normal no-adapter `project-map.json` files remain on
`schema_version: "1.0"`. Adapter-backed `project-map.json` output uses
`schema_version: "2.0"` and emits the optional source registry.

Adapter-backed output must keep these boundaries:

- adapter-backed records are optional and absent when no adapter is explicitly enabled;
- the normal Java/Spring scan path must continue without adapters, network access,
  credentials, AI, plugin loading, or source upload;
- adapter output must carry structured provenance rather than relying on free-form text;
- adapter-backed observations must stay distinct from code-backed facts, inferred
  relations, uncertain signals, spec-backed declared operations, warning/status rows,
  graph derivation metadata, cache metadata, profile Markdown, query output, and AI
  output;
- connector credentials, tokens, authorization headers, raw connector config values,
  raw request/response logs, local absolute paths, and raw source/document bodies must
  not be serialized as generated project-memory metadata.

Current v2.0 local structured import output decisions:

- The canonical placement for normalized adapter records and provenance is the separate
  `.project-memory/source-registry.json` artifact, not free-form Markdown and not
  `evidence-index.jsonl`.
- `source-registry.json` owns `source_registry_schema_version`, `adapter_runs`,
  `source_documents`, `provenance`, and bounded adapter diagnostics. It may be emitted
  only when an adapter is explicitly enabled.
- `source_documents[]` records use stable `id` values as documented in
  `INGESTION_ARCHITECTURE.md`, reference `provenance_id`, and default to
  `content_status: "not_serialized"`. They may include bounded redacted display
  metadata such as `source_type`, `source_identity`, `title`, and `content_hash`, but
  not full bodies, raw connector exports, raw request/response logs, credentials, local
  absolute paths, or raw config values.
- `provenance[]` records carry adapter/source metadata and trust-boundary labels. They
  are required for every accepted adapter-backed record and are generated-output
  metadata, not project evidence.
- Adapter-backed rows in `project-map.json` live under top-level `adapter_context`.
  Each item references `source_document_ids` and `provenance_ids` from the source
  registry, carries `context_kind: "external_document_context"`,
  `support_type: "provenance_only"`, and `confidence: "low"`, and does not carry
  `evidence_ids`. These rows remain provenance-backed external/document context. They
  must not be emitted as Java/Spring source-visible facts.
- A no-adapter scan keeps `project-map.json` on `schema_version: "1.0"` and preserves
  the current v1.x artifact set. Any scan output that adds adapter-backed
  `project-map.json` fields or sections uses a documented v2 schema marker such as
  `schema_version: "2.0"`. The separate source registry carries its own schema marker
  so downstream consumers can ignore it independently.
- Downstream consumers that do not understand adapter output can continue to consume
  no-adapter `schema_version: "1.0"` outputs. Consumers that encounter a v2 schema
  marker or `source-registry.json` must either implement the v2 adapter contract or
  ignore adapter-backed sections and provenance-aware joins explicitly.
- Generated graph nodes, query output, profile Markdown, and agent guide text do not
  gain adapter-specific behavior in this slice. The query reader continues to support
  no-adapter `schema_version: "1.0"` outputs. Such references remain navigation
  metadata and must not become evidence if introduced later.

Current `source-registry.json` shape:

```json
{
  "source_registry_schema_version": "1.0",
  "adapter_runs": [
    {
      "id": "adapter-run:sha256:<stable>",
      "adapter": {
        "name": "local-structured-import",
        "version": "2.0.0"
      },
      "import_mode": "local_export",
      "source_location_kind": "repository_relative_file",
      "network_access": "disabled",
      "input_content_hash": "sha256:<normalized-input-content>",
      "content_status": "not_serialized",
      "accepted_count": 0,
      "rejected_count": 0,
      "diagnostic_count": 0
    }
  ],
  "source_documents": [
    {
      "id": "source-document:sha256:<stable>",
      "source_type": "local_export",
      "source_identity": "source-system/record-id",
      "title": "bounded redacted title",
      "content_hash": "sha256:<normalized-record-content>",
      "content_status": "not_serialized",
      "provenance_id": "source-provenance:sha256:<stable>"
    }
  ],
  "provenance": [
    {
      "id": "source-provenance:sha256:<stable>",
      "adapter": {
        "name": "local-structured-import",
        "version": "2.0.0"
      },
      "import_mode": "local_export",
      "source_type": "local_export",
      "source_identity": "source-system/record-id",
      "content_hash": "sha256:<normalized-record-content>",
      "source_location_kind": "repository_relative_file",
      "network_access": "disabled",
      "trust_boundary_labels": [
        "local_structured_import",
        "repository_relative_file",
        "provenance_backed_external_context",
        "not_code_evidence"
      ]
    }
  ],
  "diagnostics": {
    "analysis_status": "analyzed",
    "items": [
      {
        "id": "adapter-diagnostic:local-structured-import:record:000001:<signal>",
        "severity": "warning",
        "category": "local_structured_import",
        "signal": "partial_record_rejected",
        "message": "bounded diagnostic message",
        "record_ordinal": 1
      }
    ]
  }
}
```

Current `project-map.json` adapter context shape when a local structured import adapter
is enabled:

```json
{
  "schema_version": "2.0",
  "adapter_context": {
    "analysis_status": "analyzed",
    "context_kind": "provenance_backed_external_context",
    "source_registry": "source-registry.json",
    "diagnostic_count": 0,
    "items": [
      {
        "id": "adapter_context:source-document-sha256-<stable>",
        "context_kind": "external_document_context",
        "source_type": "local_export",
        "source_identity": "source-system/record-id",
        "title": "bounded redacted title",
        "content_status": "not_serialized",
        "support_type": "provenance_only",
        "confidence": "low",
        "source_document_ids": ["source-document:sha256:<stable>"],
        "provenance_ids": ["source-provenance:sha256:<stable>"]
      }
    ]
  }
}
```

### v2.0 Adapter Migration And Consumer Compatibility

The v1-to-v2 compatibility line has two generated-output modes:

- A scan with no explicitly enabled adapter remains v1-compatible. It keeps
  `project-map.json` on `schema_version: "1.0"`, preserves the current base artifact
  set, and does not emit `.project-memory/source-registry.json`.
- A scan with an explicitly enabled local structured import adapter that accepts input
  emits `.project-memory/source-registry.json` and uses `project-map.json`
  `schema_version: "2.0"` for adapter-backed context. This is a v2 artifact set, even
  though the Java/Spring facts and `evidence-index.jsonl` evidence model remain
  separate from adapter provenance.

Regeneration expectations:

- Treat `project-map.json`, `project-graph.json`, `evidence-index.jsonl`,
  `source-registry.json`, generated Markdown, selected profile artifacts, and cache
  metadata as one generated output set for a scan. Do not mix a v2
  `source-registry.json` with an older or no-adapter `project-map.json`, and do not
  carry an old source registry forward after a no-adapter regeneration.
- Regenerate the full output set when adapter input, adapter config, repository source,
  or the tool version changes. Adapter source-document IDs are designed to remain stable
  for the same adapter/import mode/source identity, while `content_hash` changes when
  normalized content changes.
- The absence of `source-registry.json` in a successful no-adapter output set is
  expected and should not be treated as missing evidence or a partial scan.

Downstream consumer expectations:

- Consumers that support only the v1 stable line should continue to read no-adapter
  `schema_version: "1.0"` outputs and ignore or reject v2 adapter-enabled output
  explicitly.
- Consumers that read `schema_version: "2.0"` must understand top-level
  `adapter_context` and the source registry join keys before using adapter-backed rows.
  They must not promote `adapter_context` items to Java/Spring facts and must not look
  for `evidence_ids` on those items.
- `source-registry.json` is adapter provenance metadata. It is not a replacement for
  `evidence-index.jsonl`, not a new evidence index, and not proof that an external
  source is current, reachable, complete, authoritative, or aligned with repository
  source.
- The current query contract remains focused on no-adapter
  `project-map.json` `schema_version: "1.0"` artifacts. Current query commands do not
  read `source-registry.json` and do not provide adapter-context lookup, source-document
  lookup, or provenance joins. Adapter-aware query behavior requires a later contract
  update before consumers should rely on it.

Any future adapter output field addition, removal, rename, semantic change, new
generated artifact, evidence type, or schema marker change requires synchronized updates
to this contract, `EVIDENCE_MODEL.md`, focused tests or goldens where applicable, the
changelog, and release notes.

### v2.1 Git Hosting Local Export Output Boundary

The v2.1 Git hosting local import boundary reuses the existing v2 adapter artifact
placement. It does not add a new generated artifact and does not extend
`evidence-index.jsonl`.

Expected generated-output behavior:

- no-adapter scans stay on `project-map.json` `schema_version: "1.0"` and do not emit
  `.project-memory/source-registry.json`;
- explicitly enabled Git hosting import scans emit `.project-memory/source-registry.json`
  and top-level `project-map.json` `adapter_context` as a v2 adapter artifact set;
- `project-map.json` keeps the existing adapter-context shape and
  `schema_version: "2.0"`; no new project-map schema marker is needed unless the
  adapter-context item shape changes;
- Git hosting provenance uses `source_registry_schema_version: "1.1"` because provider
  metadata is added to `provenance[]`;
- current query commands remain no-adapter focused and do not read
  `source-registry.json`, source-document IDs, Git hosting provenance, or adapter
  context rows.

The Git hosting source types are:

- `github_issue`
- `github_pull_request`
- `gitlab_issue`
- `gitlab_merge_request`

Git hosting `source_identity` values are normalized logical identities, not local paths
or raw URLs. The identity is derived from provider, host, namespace, record type, and
record number or IID, for example:

```text
git-hosting/github/github.com/owner/repo/issue/123
git-hosting/github/github.com/owner/repo/pull_request/45
git-hosting/gitlab/gitlab.com/group/project/issue/77
git-hosting/gitlab/gitlab.com/group/project/merge_request/88
```

Accepted Git hosting `source_documents[]` records keep the current source-document
fields:

```json
{
  "id": "source-document:sha256:<stable>",
  "source_type": "github_issue",
  "source_identity": "git-hosting/github/github.com/owner/repo/issue/123",
  "title": "bounded redacted title",
  "content_hash": "sha256:<normalized-record-content>",
  "content_status": "not_serialized",
  "provenance_id": "source-provenance:sha256:<stable>"
}
```

Git hosting provenance extends `provenance[]` with provider metadata under
`git_hosting`. The exact implementation field order may follow the serializer, but the
contracted metadata is:

```json
{
  "id": "source-provenance:sha256:<stable>",
  "adapter": {
    "name": "git-hosting-import",
    "version": "2.1.0"
  },
  "import_mode": "local_export",
  "source_type": "github_issue",
  "source_identity": "git-hosting/github/github.com/owner/repo/issue/123",
  "content_hash": "sha256:<normalized-record-content>",
  "source_location_kind": "repository_relative_file",
  "network_access": "disabled",
  "git_hosting": {
    "provider": "github",
    "host": "github.com",
    "namespace": "owner/repo",
    "record_type": "issue",
    "record_number": "123",
    "record_state": "open",
    "source_url": "https://github.com/owner/repo/issues/123",
    "exported_at": "2026-06-18T00:00:00Z",
    "record_updated_at": "2026-06-17T00:00:00Z"
  },
  "trust_boundary_labels": [
    "git_hosting_import",
    "github",
    "repository_relative_file",
    "provenance_backed_external_context",
    "not_code_evidence",
    "network_disabled"
  ]
}
```

`git_hosting.source_url` is optional. If emitted, it must be a sanitized provider URL
without userinfo, credentials, query strings, fragments, authorization material, local
paths, or unsupported schemes. The URL is provenance metadata only and is not proof that
the remote service is reachable or current.

`git_hosting.record_state`, `exported_at`, and `record_updated_at` are snapshot metadata
from the local export. They are not current-state claims. Missing, malformed,
contradictory, stale, partial, unsupported, oversized, duplicate, ambiguous, or
provenance-missing records must be rejected, capped, or represented only as bounded
adapter diagnostics.

`project-map.json` adapter-context items keep the existing shape, with Git hosting
source types and identities:

```json
{
  "id": "adapter_context:source-document-sha256-<stable>",
  "context_kind": "external_document_context",
  "source_type": "github_issue",
  "source_identity": "git-hosting/github/github.com/owner/repo/issue/123",
  "title": "bounded redacted title",
  "content_status": "not_serialized",
  "support_type": "provenance_only",
  "confidence": "low",
  "source_document_ids": ["source-document:sha256:<stable>"],
  "provenance_ids": ["source-provenance:sha256:<stable>"]
}
```

Raw issue bodies, pull-request or merge-request descriptions, comments, review notes,
labels, authors, branch names, commit metadata, pipeline/status payloads, raw export
objects, raw request/response logs, configured import paths, local absolute paths,
credentials, tokens, cookies, authorization headers, and raw config values must not be
serialized by default. The content hash may include normalized text and metadata that
the adapter accepted, but generated artifacts must serialize only bounded redacted
display metadata, hashes, snapshot metadata, and provenance join keys.

The scan config addition is:

```yaml
adapters:
  git_hosting_import:
    enabled: true
    path: exports/git-hosting.json
```

`adapters.git_hosting_import.enabled` is optional and defaults to disabled. When it is
`true`, `path` is required and must identify one existing repository-relative regular
JSON file under the scan root with a verifiable single-link identity. The path must
follow the same safety rules as other adapter import paths: no absolute paths, no `./`,
no backslash separators, no empty, `.` or `..` segments, no `.project-memory/` target,
no directories, no symlinked path segments, no multi-link regular files, no
unverifiable link counts, and no path outside the scanned repository root. When the
adapter is disabled, `path` must be omitted.

The local export file format is
`format: "agent-project-memory.git_hosting_export.v1"`. It must not be treated as a raw
GitHub or GitLab API response. It must not accept credentials, credential names,
environment-variable interpolation, remote URLs as import locations, API enablement
flags, background sync settings, retry/rate-limit settings, or network/auth options.

### v2.2 Connector Local Export Output Boundary

The Jira, YouTrack, and Confluence local import boundary reuses the existing v2
adapter artifact placement. It does not add a new generated artifact and does not extend
`evidence-index.jsonl`.

Expected generated-output behavior:

- no-adapter scans stay on `project-map.json` `schema_version: "1.0"` and do not emit
  `.project-memory/source-registry.json`;
- explicitly enabled connector import scans emit `.project-memory/source-registry.json`
  and top-level `project-map.json` `adapter_context` as a v2 adapter artifact set;
- `project-map.json` keeps the existing adapter-context shape and
  `schema_version: "2.0"`; no new project-map schema marker is needed unless the
  adapter-context item shape changes;
- connector provenance uses `source_registry_schema_version: "1.2"` because
  provider metadata for Jira, YouTrack, and Confluence is added to `provenance[]`;
- current query commands remain no-adapter focused and do not read
  `source-registry.json`, source-document IDs, connector provenance, or adapter context
  rows.

The connector source types are:

- `jira_issue`
- `youtrack_issue`
- `youtrack_article`
- `confluence_page`

Connector `source_identity` values are normalized logical identities, not local paths or
raw URLs. The identity is derived from provider, host, source type, container key, and a
stable provider record key or ID, for example:

```text
connector/jira/jira.example.com/project/PROJ/issue/PROJ-123
connector/youtrack/youtrack.example.com/project/PROJ/issue/PROJ-123
connector/youtrack/youtrack.example.com/project/PROJ/article/ABC123
connector/confluence/confluence.example.com/space/ENG/page/123456
```

Accepted connector `source_documents[]` records keep the current source-document
fields:

```json
{
  "id": "source-document:sha256:<stable>",
  "source_type": "jira_issue",
  "source_identity": "connector/jira/jira.example.com/project/PROJ/issue/PROJ-123",
  "title": "bounded redacted title",
  "content_hash": "sha256:<normalized-record-content>",
  "content_status": "not_serialized",
  "provenance_id": "source-provenance:sha256:<stable>"
}
```

Connector provenance extends `provenance[]` with provider metadata under `connector`.
The exact implementation field order may follow the serializer, but the contracted
metadata is:

```json
{
  "id": "source-provenance:sha256:<stable>",
  "adapter": {
    "name": "connector-import",
    "version": "2.2.0"
  },
  "import_mode": "local_export",
  "source_type": "jira_issue",
  "source_identity": "connector/jira/jira.example.com/project/PROJ/issue/PROJ-123",
  "content_hash": "sha256:<normalized-record-content>",
  "source_location_kind": "repository_relative_file",
  "network_access": "disabled",
  "connector": {
    "provider": "jira",
    "host": "jira.example.com",
    "source_family": "issue_tracker",
    "container_type": "project",
    "container_key": "PROJ",
    "record_type": "issue",
    "record_key": "PROJ-123",
    "record_id": "10001",
    "record_state": "open",
    "source_url": "https://jira.example.com/browse/PROJ-123",
    "exported_at": "2026-06-19T00:00:00Z",
    "record_updated_at": "2026-06-18T00:00:00Z"
  },
  "trust_boundary_labels": [
    "connector_import",
    "jira",
    "repository_relative_file",
    "provenance_backed_external_context",
    "not_code_evidence",
    "network_disabled"
  ]
}
```

`connector.source_url` is optional. If emitted, it must be a sanitized provider URL
without userinfo, credentials, query strings, fragments, authorization material, local
paths, or unsupported schemes. The URL is provenance metadata only and is not proof that
the remote service is reachable or current.

`connector.record_state`, `exported_at`, and `record_updated_at` are snapshot metadata
from the local export. They are not current-state claims. Missing, malformed,
contradictory, stale, partial, unsupported, oversized, duplicate, ambiguous,
authority-confusing, or provenance-missing records must be rejected, capped, or
represented only as bounded adapter diagnostics.

`project-map.json` adapter-context items keep the existing shape, with connector source
types and identities:

```json
{
  "id": "adapter_context:source-document-sha256-<stable>",
  "context_kind": "external_document_context",
  "source_type": "jira_issue",
  "source_identity": "connector/jira/jira.example.com/project/PROJ/issue/PROJ-123",
  "title": "bounded redacted title",
  "content_status": "not_serialized",
  "support_type": "provenance_only",
  "confidence": "low",
  "source_document_ids": ["source-document:sha256:<stable>"],
  "provenance_ids": ["source-provenance:sha256:<stable>"]
}
```

Raw issue/page/article bodies, descriptions, comments, rendered HTML, rich text,
attachment names, attachment bodies, labels, authors, workflow history, raw provider
export objects, raw request/response logs, configured import paths, local absolute
paths, credentials, tokens, cookies, authorization headers, and raw config values must
not be serialized by default. The content hash may include normalized text and metadata
that the adapter accepted, but generated artifacts must serialize only bounded redacted
display metadata, hashes, snapshot metadata, and provenance join keys.

The scan config addition is:

```yaml
adapters:
  connector_import:
    enabled: true
    path: exports/connectors.json
```

`adapters.connector_import.enabled` is optional and defaults to disabled. When it is
`true`, `path` is required and must identify one existing repository-relative regular
JSON file under the scan root with a verifiable single-link identity. The path must
follow the same safety rules as other adapter import paths: no absolute paths, no `./`,
no backslash separators, no empty, `.` or `..` segments, no `.project-memory/` target,
no directories, no symlinked path segments, no multi-link regular files, no
unverifiable link counts, and no path outside the scanned repository root. When the
adapter is disabled, `path` must be omitted.

The local export file format is
`format: "agent-project-memory.connector_export.v1"`. It must not be treated as a raw
Jira, YouTrack, or Confluence API response. It must not accept credentials, credential
names, environment-variable interpolation, remote URLs as import locations, API
enablement flags, background sync settings, retry/rate-limit settings, pagination
settings, remote cache settings, provider discovery options, or network/auth options.

## v2 Optional AI Presentation Output Boundary

Default scans do not emit AI presentation artifacts, real AI provider metadata,
prompts, provider configuration, provider credentials, network metadata, embeddings,
vector indexes, chat transcripts, or AI-generated project facts. Normal
`project-map.json` files remain on `schema_version: "1.0"` unless a future release
explicitly documents a schema marker change.

The v2.3 AI presentation boundary chooses a separate optional generated artifact
surface rather than a profile extension or query mode. The current first implementation
creates these artifacts only when a scan is explicitly invoked with
`--ai-presentation mock_no_network`:

```text
.project-memory/
  ai-presentations/
    manifest.json
    brief.md
```

This directory is a presentation surface over deterministic generated memory, not a
stronger authority than `project-map.json`, `project-graph.json`,
`evidence-index.jsonl`, adapter provenance, deterministic profile output, cache
metadata, or deterministic query results. Consumers that do not understand AI
presentation artifacts should ignore `.project-memory/ai-presentations/` and continue
using the base artifact set.

Allowed AI presentation inputs are limited to:

- generated structured facts from documented artifacts;
- existing evidence IDs and already serialized bounded evidence excerpts;
- graph navigation metadata and derivation metadata labeled as non-evidence;
- query/source-artifact metadata labeled as non-evidence;
- deterministic profile/cache/status metadata labeled as non-evidence;
- future adapter-backed documents and provenance only after those surfaces are accepted
  by deterministic adapter contracts.

The first mock/no-network implementation slice uses only the base generated
`project-map.json`, `evidence-index.jsonl`, and `project-graph.json` artifacts.
Adapter provenance, profile metadata, cache metadata, and query-output metadata are
allowed by the boundary but may remain parked until a later implementation slice
documents and tests those joins.

Forbidden AI presentation inputs include raw repository source files, generated-source
contents, raw local document bodies, generated Markdown bodies, raw connector exports,
raw adapter input files, connector credentials, raw connector request/response logs,
remote API responses, provider credentials, environment values, local absolute paths,
and raw prompt transcripts.

AI presentation output must not:

- add, remove, rename, or reinterpret `project-map.json` facts;
- create `evidence-index.jsonl` records, evidence fields, evidence types, confidence
  labels, evidence IDs, or source references;
- create connector truth, source-of-truth claims, security findings, vulnerability
  proof, runtime behavior claims, source/spec agreement claims, coverage/CI/assertion
  claims, business-priority claims, documentation-freshness claims, release evidence,
  or code modifications;
- rewrite source files, repository docs, root instruction files, configuration files,
  generated artifacts, cache metadata, profile artifacts, adapter exports, or evidence
  records;
- make repository chat, generic RAG, embeddings, vector search, or automatic code
  modification the core product experience.

When an AI presentation artifact is emitted, it must carry visible non-evidence
labeling in both human-readable wording and machine-readable metadata. The Markdown
presentation must include an early visible statement that the file is AI-generated
presentation only, is not project evidence, and must be checked against the referenced
deterministic artifacts before use.

Current `ai-presentations/manifest.json` shape for `mock_no_network`:

```json
{
  "ai_presentation_schema_version": "1.0",
  "presentation_surface": "separate_artifact",
  "provider_mode": "mock_no_network",
  "authority": "non_authoritative_presentation",
  "evidence_policy": "references_existing_evidence_only",
  "network_access": "disabled",
  "source_upload": "disabled",
  "prompt_transcript_status": "not_serialized",
  "source_artifacts": [
    {
      "name": "project-map.json",
      "schema_version": "1.0"
    },
    {
      "name": "evidence-index.jsonl",
      "record_count": 12
    },
    {
      "name": "project-graph.json",
      "graph_schema_version": "1.0",
      "required": false
    }
  ],
  "generated_presentations": [
    {
      "name": "brief",
      "artifact_path": "ai-presentations/brief.md",
      "content_kind": "ai_markdown_presentation",
      "authority": "non_authoritative_presentation",
      "evidence_policy": "references_existing_evidence_only",
      "provider_mode": "mock_no_network"
    }
  ]
}
```

Manifest rules:

- `ai_presentation_schema_version` is `"1.0"` for the first AI presentation
  manifest and does not define a new `project-map.json` schema.
- `presentation_surface` is `"separate_artifact"` for the v2.3 surface.
- `provider_mode` values are limited to documented modes. The first implementation may
  use `"mock_no_network"` only. Real provider modes are parked until a later design
  explicitly documents provider, network, credential, telemetry, retention, and prompt
  input behavior.
- `authority` must be `"non_authoritative_presentation"`.
- `evidence_policy` must be `"references_existing_evidence_only"` unless a later
  contract documents an equivalent or stricter value. AI presentations may cite
  existing evidence IDs, graph IDs, source-artifact names, and future accepted
  provenance IDs for navigation only.
- `network_access` and `source_upload` must be `"disabled"` for the mock/no-network
  slice.
- `prompt_transcript_status` must be `"not_serialized"` unless a later provider design
  explicitly changes prompt transcript policy.
- `source_artifacts[]` names generated artifact filenames and relevant schema markers
  or bounded counts only. It must not contain local absolute paths, configured import
  paths, source bodies, document bodies, prompt text, command text, credentials, tokens,
  or environment values.
- `generated_presentations[]` lists only files owned by the AI presentation generator
  under `.project-memory/ai-presentations/`. Artifact paths must be normalized
  `.project-memory`-relative slash paths and must not be absolute, start with `./`,
  contain `.` or `..` path segments, use backslash separators, or escape the
  `ai-presentations/` directory.
- The manifest is generated-output metadata only. It is not evidence, not a source
  registry, not a project map section, and not proof that an AI output is correct.
- The current mock/no-network slice uses `project-map.json`, `evidence-index.jsonl`,
  and `project-graph.json` as inputs. It does not read raw source files, generated
  Markdown bodies, raw local document bodies, raw adapter input files, raw connector
  exports, cache metadata, profile Markdown, query output, credentials, environment
  values, local absolute paths, or prompt transcripts as AI inputs.
- When `--ai-presentation mock_no_network` is combined with `--incremental`, the
  current implementation runs a full scan and skips incremental cache metadata refresh
  rather than extending the cache contract in this slice.

Provider, privacy, network, credential, telemetry, and source-upload defaults remain
closed. No provider is configured by default, no network access is enabled by default,
and no source code, local document body, generated-source content, connector export,
credential, token, cookie, local absolute path, or raw prompt transcript may be
serialized or uploaded by default. Any future provider-backed output mode requires a
separate output contract update, focused tests or goldens where applicable, threat-model
review, changelog entry, and release notes before implementation.

Prompt and content-injection controls are part of the output boundary. Repository text,
local document text, evidence excerpts, adapter-backed records, connector text,
generated Markdown, and user-provided labels are untrusted content for any AI prompt.
They must be treated as quoted data, not executable instructions. They must not be able
to make the AI layer fetch network resources, read additional files, reveal credentials,
rewrite artifacts, alter evidence or provenance, mark AI output as authoritative, or
create repository changes.

Validation requirements before the first mock/no-network implementation release:

- focused enablement tests proving default scans do not create
  `.project-memory/ai-presentations/`;
- manifest and Markdown golden tests for non-evidence labels, authority labels,
  provider mode, source artifact metadata, and owned output paths;
- regression tests proving `project-map.json`, `project-graph.json`,
  `evidence-index.jsonl`, `endpoints.md`, `agent-guide.md`, source registry output,
  cache metadata, and profile artifacts are not mutated by AI presentation generation;
- content-safety tests proving AI presentation output and metadata do not serialize raw
  source bodies, local document bodies, generated-source contents, raw connector
  exports, raw prompt transcripts, local absolute paths, credentials, tokens, or
  command logs;
- no-network and no-credential tests for the mock/no-network provider mode;
- prompt/content-injection fixtures proving source or connector text cannot become
  AI-layer instructions or change output authority labels;
- risk-based security review before release for any implementation that changes
  enablement, artifact reading, output paths, generated output rendering, prompt-input
  assembly, provider abstraction, filesystem behavior, or evidence/provenance reference
  rendering.

Stop conditions for implementation:

- default scans create AI presentation artifacts;
- the core analyzer, adapter normalization path, graph builder, evidence index builder,
  cache layer, profile generator, or query layer requires AI provider code;
- AI output enters `project-map.json`, `evidence-index.jsonl`, `source-registry.json`,
  `project-graph.json`, cache metadata, deterministic profile artifacts, repository
  files, source files, root instruction files, or scan config;
- provider mode, prompt-input policy, output authority labels, source-upload defaults,
  network defaults, credential policy, prompt transcript policy, or content-injection
  controls are ambiguous;
- real provider/network/auth/credential/source-upload behavior enters a mock/no-network
  implementation slice.

The v0.1 interface-mapping endpoint contract keeps endpoint extraction limited to
source-visible Java inputs under supported production source roots, while adding
uniquely bound interface-declared Spring MVC mappings to the v0.1 endpoint semantics. It
does not add Maven generation during scans, default `target/generated-sources` scanning,
full OpenAPI validation, generated API reconstruction, or Spring runtime handler
mapping reconstruction.

## `project-map.json`

`project-map.json` is the machine-readable project memory file. The current implemented
public contract uses the v1.0 schema marker and compatibility policy layered on top of
the v0.9 config parser and safe-defaults slice, the v0.8 local Markdown discovery and
structure slice, the v0.7 tests inventory refinement slice, the v0.6 JPA/domain model
slice, the v0.5 Spring application surface slices, the v0.4 API surface slice, and the
v0.3 module-aware Maven metadata, dependency, and plugin inventory contract. The preserved
v0.7 contract also emits direct Spring test slice and mock annotation signals and
conservative tested-subject relation/status rows under the top-level `tests` inventory,
plus conservative test-gap and change-risk planning hints under the top-level `quality`
object.
The current no-adapter implementation emits `schema_version: "1.0"` with a top-level
`scan` owner for redacted config, feature, path-policy, and diagnostic metadata. The v1.0
marker preserves the current v0.9 output field shape and evidence semantics; v1.1 adds
bounded Gradle and mixed Maven/Gradle fields as an additive compatibility expansion. The
v1.1 expansion does not remove, rename, or reinterpret existing Maven fields and does
not change `evidence-index.jsonl` field semantics. It discovers a root-local
`agent-project-memory.yml` config file or an explicitly selected
repository-relative YAML config, validates the bounded config schema, applies safe
defaults, and applies user include/exclude rules only to local Markdown discovery while
preserving non-overridable built-in safety exclusions. It preserves the v0.8
local Markdown/document ingestion boundary with deterministic default-scope Markdown
discovery, document inventory, ATX heading references, bounded chunk references, and
resolving `document` evidence records for accepted file, heading, chunk, and bounded
reconciliation mention observations. It keeps conservative `documents.reconciliation`
uncertain inspection hints and compact local documentation orientation generated from
structured `documents` facts and resolving evidence only.
The v0.1 single-module shape below is kept as historical compatibility context for
fields that later contracts preserve.

The v0.1 baseline wrote this top-level object:

```json
{
  "schema_version": "0.1",
  "project": {
    "root": ".",
    "build": {
      "system": "maven",
      "root_build_file": "pom.xml",
      "evidence_ids": [
        "ev:pom.xml:1-1:build_file:pom.xml"
      ]
    },
    "source_roots": ["src/main/java"],
    "test_roots": ["src/test/java"]
  },
  "endpoints": [
    {
      "id": "endpoint:com.example.orders.OrderController#getOrder",
      "controller_class": "com.example.orders.OrderController",
      "handler_method": "getOrder",
      "http_methods": ["GET"],
      "http_method_semantics": "declared",
      "paths": ["/orders/{id}"],
      "request_parameters": [
        {
          "name": "id",
          "source": "path_variable",
          "java_type": "java.lang.Long",
          "evidence_ids": [
            "ev:src/main/java/com/example/orders/OrderController.java:21-21:com.example.orders.OrderController#getOrder:@PathVariable:parameter:0:id"
          ]
        }
      ],
      "request_body_type": null,
      "response_type": "com.example.orders.OrderDto",
      "mapping_source": {
        "kind": "direct_handler_method",
        "declaring_type": "com.example.orders.OrderController",
        "declaring_method": "getOrder",
        "binding": "direct",
        "uncertainty": null,
        "evidence_ids": [
          "ev:src/main/java/com/example/orders/OrderController.java:20-20:com.example.orders.OrderController#getOrder:@GetMapping"
        ]
      },
      "evidence_ids": [
        "ev:src/main/java/com/example/orders/OrderController.java:18-18:com.example.orders.OrderController:@RestController",
        "ev:src/main/java/com/example/orders/OrderController.java:20-20:com.example.orders.OrderController#getOrder:@GetMapping",
        "ev:src/main/java/com/example/orders/OrderController.java:21-21:com.example.orders.OrderController#getOrder:@PathVariable:parameter:0:id"
      ]
    }
  ],
  "warnings": {
    "analysis_status": "analyzed",
    "items": [
      {
        "id": "warning:hidden_http_surface:openapi_spec_file:src/main/resources/openapi.yml",
        "category": "hidden_http_surface",
        "signal": "openapi_spec_file",
        "message": "OpenAPI/Swagger spec file detected by filename only; v0.1 does not parse specs or reconstruct generated APIs.",
        "source_path": "src/main/resources/openapi.yml",
        "evidence_ids": [
          "ev:src/main/resources/openapi.yml:unknown:config_file:openapi.yml"
        ]
      }
    ]
  },
  "components": {
    "analysis_status": "analyzed",
    "items": [
      {
        "id": "component:com.example.orders.OrderService",
        "class_name": "com.example.orders.OrderService",
        "stereotypes": ["@Service"],
        "evidence_ids": [
          "ev:src/main/java/com/example/orders/OrderService.java:12-12:com.example.orders.OrderService:@Service"
        ]
      }
    ]
  },
  "entities": {
    "analysis_status": "analyzed",
    "items": [
      {
        "id": "entity:com.example.orders.Order",
        "class_name": "com.example.orders.Order",
        "table_name": "orders",
        "identifier_fields": [
          {
            "field_name": "id",
            "java_type": "Long",
            "declaring_class": "com.example.orders.Order",
            "source_kind": "declared",
            "evidence_ids": [
              "ev:src/main/java/com/example/orders/Order.java:16-16:com.example.orders.Order:@Id:field:id"
            ]
          }
        ],
        "relationships": [
          {
            "field_name": "customer",
            "annotation": "@ManyToOne",
            "java_type": "Customer",
            "target_resolution": "declared_type_only",
            "uncertainty": "target_type_not_resolved",
            "evidence_ids": [
              "ev:src/main/java/com/example/orders/Order.java:19-19:com.example.orders.Order:@ManyToOne:field:customer"
            ]
          }
        ],
        "evidence_ids": [
          "ev:src/main/java/com/example/orders/Order.java:12-12:com.example.orders.Order:@Entity",
          "ev:src/main/java/com/example/orders/Order.java:13-13:com.example.orders.Order:@Table"
        ]
      }
    ]
  },
  "tests": {
    "analysis_status": "analyzed",
    "items": [
      {
        "class_name": "com.example.orders.OrderControllerTest",
        "source_path": "src/test/java/com/example/orders/OrderControllerTest.java",
        "framework_signals": [
          {
            "name": "JUnit Jupiter",
            "evidence_ids": [
              "ev:src/test/java/com/example/orders/OrderControllerTest.java:3-3:com.example.orders.OrderControllerTest:import:org.junit.jupiter.api.Test",
              "ev:src/test/java/com/example/orders/OrderControllerTest.java:7-7:com.example.orders.OrderControllerTest#returnsOrder:@Test"
            ]
          }
        ],
        "tested_subjects": [
          {
            "class_name": "com.example.orders.OrderController",
            "support_type": "inferred",
            "confidence": "medium",
            "uncertainty": null,
            "evidence_ids": [
              "ev:src/test/java/com/example/orders/OrderControllerTest.java:5-5:com.example.orders.OrderControllerTest:test_file",
              "ev:src/main/java/com/example/orders/OrderController.java:18-18:com.example.orders.OrderController:code_symbol"
            ]
          }
        ],
        "evidence_ids": [
          "ev:src/test/java/com/example/orders/OrderControllerTest.java:5-5:com.example.orders.OrderControllerTest:test_file"
        ]
      }
    ]
  }
}
```

Field rules:

- `schema_version` is the string `"0.1"` for the v0.1 output contract slice.
- `project.root` is `"."` because the output is relative to the scanned repository root.
- `project.build.system` is `"maven"` when a root `pom.xml` file is detected and
  accepted within the Maven POM byte limit, and `"not_detected"` otherwise.
- `project.build.root_build_file` is `"pom.xml"` when detected and accepted, and
  `null` otherwise.
- `project.build.evidence_ids` references `build_file` evidence for the root `pom.xml`
  when present and accepted, and is an empty array otherwise.
- `project.source_roots` contains detected standard production source roots. The v0.1
  implementation supports `src/main/java`.
- `project.test_roots` contains detected standard test source roots. The v0.1
  implementation supports `src/test/java`.
- `endpoints` is sorted deterministically by first path, HTTP methods, method semantics,
  controller class, and handler method.
- `endpoint.id` is `endpoint:<controller_class>#<handler_method>` in this slice.
- `endpoint.controller_class` is always the concrete controller class that owns or
  implements the emitted handler, even when the mapping annotations are declared on a
  source-visible interface method.
- `endpoint.handler_method` is always the concrete handler method name. Interface-only
  declarations with no uniquely bindable concrete handler are not emitted as endpoints
  in this v0.1 slice.
- `http_methods` contains directly extracted methods when available. It is an empty array
  when the source did not declare a method or used an unsupported expression.
- `http_method_semantics` is one of `"declared"`, `"not_declared"`, or `"unsupported"`.
- `request_parameters` is an empty array when no supported request parameter annotations
  are detected.
- `request_body_type` is a Java type string when a supported `@RequestBody` parameter is
  detected and `null` otherwise.
- `response_type` is the declared Java return type when available.
- Endpoint and request-parameter `evidence_ids` must resolve to records in
  `evidence-index.jsonl`.

Endpoint mapping-source rules for the v0.1 interface-mapping decision:

- Endpoint facts for this analyzer slice include a `mapping_source` object.
- `mapping_source.kind` is one of:
  - `"direct_handler_method"`: the Spring MVC method-level mapping annotation is declared
    directly on the concrete `controller_class` `handler_method`.
  - `"source_visible_interface_method"`: the Spring MVC method-level mapping annotation
    is declared on a Java interface method under a supported production source root such
    as `src/main/java`, and that interface method is uniquely bound to the concrete
    `controller_class` `handler_method`.
- `mapping_source.declaring_type` is the fully qualified class or interface that declares
  the method-level mapping annotation used for this endpoint fact.
- `mapping_source.declaring_method` is the method name on `declaring_type` that declares
  the method-level mapping annotation.
- `mapping_source.binding` is `"direct"` for direct handler method mappings and
  `"unique_implemented_interface_method"` for source-visible interface method mappings.
- `mapping_source.uncertainty` is `null` for emitted endpoint facts in this decision
  slice. Ambiguous interface bindings are skipped rather than emitted with an uncertain
  endpoint claim.
- `mapping_source.evidence_ids` references the evidence that supports where the mapping
  annotation was read from and, for interface mappings, the evidence that supports the
  unique source-visible binding. These IDs must resolve to records in
  `evidence-index.jsonl`.
- For a direct handler method mapping, `mapping_source.evidence_ids` should include the
  concrete method-level mapping annotation evidence and any directly used class-level
  controller mapping annotation evidence.
- For a source-visible interface method mapping, `mapping_source.evidence_ids` should
  include interface method mapping annotation evidence, relevant source-visible
  class-level mapping annotation evidence, and `code_symbol` evidence for the concrete
  handler/interface binding.
- Interface mapping support does not claim complete Spring runtime behavior. It does not
  run Maven generation, scan `target/generated-sources` by default, derive endpoint
  facts from OpenAPI operations, reconstruct generated APIs, resolve classpath-only
  interfaces, infer runtime proxies, or interpret unsupported Spring mapping conditions.
- Spring MVC endpoint annotations are trusted only when source-visible syntax supports a
  Spring origin: a fully qualified annotation name in the supported Spring package, or a
  simple annotation name with an explicit single-type import for the supported Spring
  annotation, and only when that exact framework type is not declared by scanned source.
  Unresolved simple-name annotations, wildcard-import-only annotations,
  same-package/local fake annotations, source-declared fake framework annotations,
  generated-source-only annotations, and classpath-only annotations are skipped rather
  than emitted as endpoint facts.
- `@RequestMapping(method = ...)` values are extracted only from supported Spring
  `RequestMethod` references visible as the exact fully qualified enum type or through
  an explicit single-type import. Bare enum constants, static-imported constants, local
  `RequestMethod` types, wildcard-import-only references, and source-declared fake
  `org.springframework.web.bind.annotation.RequestMethod` types produce
  `http_method_semantics: "unsupported"` rather than declared HTTP methods.
- Source-visible interface binding is established only from Java-visible source syntax:
  fully qualified implemented interface names, explicit single-type imports, or
  same-package interface names. Wildcard imports are not resolved in this v0.1 slice and
  are skipped rather than matched through a repository-wide simple-name fallback.
- If more than one source-visible interface method could bind to the same concrete
  handler, if the binding cannot be established from supported source roots, or if the
  interface is only present in generated or classpath sources outside the scan inputs,
  the interface-derived endpoint is skipped. If the concrete handler also has a direct
  handler method mapping, the direct endpoint may still be emitted with
  `mapping_source.kind: "direct_handler_method"`.
- `warnings.analysis_status` is `"analyzed"` when the supported `src/main/java` source
  root exists and the hidden HTTP surface warning analyzer runs.
- `warnings.items` contains deterministic warning signals that may indicate HTTP
  surfaces intentionally not expanded into endpoint facts. Warning items are sorted by
  `category`, `signal`, `source_path`, and `id`.
- `warning.id` is a stable string beginning with
  `warning:hidden_http_surface:<signal>:` in this slice.
- `warning.category` is `"hidden_http_surface"` for the current warning set.
- `warning.signal` is one of:
  - `"openapi_spec_file"`: a repository file has a supported OpenAPI/Swagger filename
    such as `openapi.yml`, `openapi.yaml`, `openapi.json`, `swagger.yml`,
    `swagger.yaml`, or `swagger.json`. The legacy warning is by filename only and does
    not parse the file content.
  - `"maven_openapi_swagger_codegen_plugin"`: the root `pom.xml` contains a deterministic
    OpenAPI/Swagger Maven plugin declaration under `<build><plugins><plugin>` or
    `<build><pluginManagement><plugins><plugin>` with exact artifact ID
    `openapi-generator-maven-plugin` or `swagger-codegen-maven-plugin`. Comments,
    dependencies, properties, and arbitrary text do not produce this signal. Duplicate
    declarations of the same plugin artifact ID in one `pom.xml` emit one warning.
  - `"repository_rest_resource"`: a source-visible Java type under a supported
    production source root has a direct `@RepositoryRestResource` annotation whose
    origin is visible as `org.springframework.data.rest.core.annotation.RepositoryRestResource`
    through an exact fully qualified annotation name or explicit single-type import, and
    that exact framework type is not declared by scanned source.
- `warning.message` is a concise deterministic explanation of the limitation. It must
  not summarize the referenced source file or turn the signal into endpoint facts.
- `warning.source_path` is the repository-relative source path that produced the signal.
- `warning.evidence_ids` references the evidence that supports the warning and must
  resolve to records in `evidence-index.jsonl`.
- Warning signals do not create entries in `endpoints`; the analyzer must not parse
  OpenAPI operations, run Maven generation, scan `target/generated-sources` by default, or
  reconstruct generated APIs from warning signals.

Example direct mapping source:

```json
{
  "kind": "direct_handler_method",
  "declaring_type": "com.example.orders.OrderController",
  "declaring_method": "getOrder",
  "binding": "direct",
  "uncertainty": null,
  "evidence_ids": [
    "ev:src/main/java/com/example/orders/OrderController.java:20-20:com.example.orders.OrderController#getOrder:@GetMapping"
  ]
}
```

Example source-visible interface mapping source:

```json
{
  "kind": "source_visible_interface_method",
  "declaring_type": "com.example.orders.OrdersApi",
  "declaring_method": "getOrder",
  "binding": "unique_implemented_interface_method",
  "uncertainty": null,
  "evidence_ids": [
    "ev:src/main/java/com/example/orders/OrdersApi.java:18-18:com.example.orders.OrdersApi#getOrder:@GetMapping",
    "ev:src/main/java/com/example/orders/OrderController.java:16-16:com.example.orders.OrderController:code_symbol"
  ]
}
```
- `components.analysis_status` is `"analyzed"` when the supported `src/main/java` source
  root exists and the direct component analyzer runs.
- `components.items` contains direct Spring stereotype component facts sorted
  deterministically by `class_name` and `id`.
- `component.id` is `component:<class_name>`.
- `component.class_name` is the fully qualified Java source type name when resolvable
  from the source file package and class or interface declaration. The field name remains
  `class_name` for v0.1 compatibility even when the component is an annotated interface.
- `component.stereotypes` contains directly present supported class-level annotation
  symbols with `@` on source-visible Java classes or interfaces. The v0.1 implementation
  supports `@Component`, `@Service`, `@Repository`, `@Controller`, `@RestController`, and
  `@Configuration`. It does not infer repository components from `extends JpaRepository`
  unless a supported stereotype annotation is directly present.
- Direct Spring component stereotypes are trusted only when source-visible syntax
  supports a Spring origin: a fully qualified annotation name in the supported Spring
  package, or a simple annotation name with an explicit single-type import for the
  supported Spring annotation, and only when that exact framework type is not declared
  by scanned source. Unresolved simple-name stereotypes, wildcard-import-only
  stereotypes, same-package/local fake stereotypes, source-declared fake framework
  stereotypes, generated-source-only stereotypes, and classpath-only stereotypes are
  skipped rather than emitted as component facts.
- `component.evidence_ids` references annotation evidence for the direct stereotype
  annotations and must resolve to records in `evidence-index.jsonl`.
- `entities.analysis_status` is `"analyzed"` when the supported `src/main/java` source
  root exists and the direct JPA entity analyzer runs.
- `entities.items` contains direct JPA entity facts sorted deterministically by
  `class_name` and `id`.
- `entity.id` is `entity:<class_name>`.
- `entity.class_name` is the fully qualified Java class name when resolvable from the
  source file package and class declaration.
- `entity.table_name` is the literal string from direct class-level
  `@Table(name = "...")` when present and deterministically extractable, otherwise
  `null`.
- Direct JPA annotations are trusted only when source-visible syntax supports a
  supported JPA origin: a fully qualified `jakarta.persistence.*` or `javax.persistence.*`
  annotation name, a simple annotation name with an explicit single-type import for a
  supported JPA annotation, or a simple annotation name covered by an explicit
  non-static `jakarta.persistence.*` or `javax.persistence.*` wildcard import for the
  existing supported JPA annotation set. Wildcard trust is per exact JPA type and only
  when there is no conflicting explicit import, no same-package/local simple-name
  declaration, and that exact framework type is not declared by scanned source.
  Unresolved simple-name annotations, unsupported wildcard imports, same-package/local
  fake annotations, source-declared fake framework annotations, generated-source-only
  annotations, and classpath-only annotations are skipped rather than emitted as entity,
  identifier, table, mapped-superclass, or relationship facts.
- `entity.identifier_fields` contains field-level `@Id` facts declared directly on the
  entity class or declared on a conservative source-visible superclass chain where each
  traversed superclass is present under supported production source roots and has a
  direct class-level `@MappedSuperclass` annotation. Identifier fields are sorted
  deterministically by `source_kind`, `declaring_class`, `field_name`, and `java_type`.
- Mapped-superclass support resolves superclass references only through fully qualified
  names, explicit single-type imports, or the same package. Unresolved, ambiguous,
  cyclic, wildcard-import-only, classpath-only, generated-source-only, or otherwise
  non-source-visible hierarchy branches are skipped. This does not imply full ORM
  inheritance reconstruction, classpath solving, `@Inheritance` handling, property-access
  mapping, embedded IDs, generated-value runtime semantics, join-column analysis,
  repository analysis, schema generation, or runtime ORM behavior. The bounded v0.6
  direct field-level `@Column` metadata slice is described in the v0.6 section below.
- `identifier_field.field_name` is the declared Java field name.
- `identifier_field.java_type` is the declared Java field type string.
- `identifier_field.declaring_class` is the fully qualified Java class that declares the
  identifier field.
- `identifier_field.source_kind` is one of:
  - `"declared"`: the field is declared directly on the entity class.
  - `"mapped_superclass"`: the field is declared on a directly source-visible class
    annotated with `@MappedSuperclass`.
- `identifier_field.evidence_ids` references field-level `@Id` annotation evidence and
  must resolve to records in `evidence-index.jsonl`. For identifier fields with
  `source_kind` set to `"mapped_superclass"`, it must also reference class-level
  `@MappedSuperclass` annotation evidence for `declaring_class`.
- `entity.relationships` contains field-level direct JPA relationship annotation facts
  sorted by `field_name`, `annotation`, and `java_type`. The v0.1 implementation
  supports `@ManyToOne`, `@OneToMany`, `@OneToOne`, and `@ManyToMany`.
- `relationship.field_name` is the declared Java field name.
- `relationship.annotation` is the direct relationship annotation symbol with `@`.
- `relationship.java_type` is the declared Java field type string. It is not a resolved
  target class.
- `relationship.target_resolution` is `"declared_type_only"` in v0.1.
- `relationship.uncertainty` is `"target_type_not_resolved"` in v0.1.
- `relationship.evidence_ids` references field-level relationship annotation evidence
  and must resolve to records in `evidence-index.jsonl`.
- `entity.evidence_ids` references class-level direct `@Entity` evidence and direct
  `@Table` evidence when present. These IDs must resolve to records in
  `evidence-index.jsonl`.
- `tests.analysis_status` is `"analyzed"` when the supported `src/test/java` source root
  exists and the tests inventory analyzer runs. It is `"not_detected"` when no supported
  test root is present in the current single-module scan.
- `tests.items` contains Java class declarations under supported test roots that look
  like test classes, sorted deterministically by `class_name` and `source_path`.
  Interfaces are not emitted. A declaration is emitted when it has a supported test
  suffix such as `Test`, `Tests`, or `IT`, or when it has directly visible test-class
  marker annotations on the class or its methods, such as JUnit `@Test`, JUnit
  `@Nested`, JUnit 4 `@RunWith`, or Spring test context annotations such as
  `@SpringBootTest`, `@WebMvcTest`, `@DataJpaTest`, and `@ContextConfiguration`.
  Helper, support, or configuration declarations without clear test naming and without
  direct test-class marker annotations are omitted, including nested helper/configuration
  declarations inside otherwise valid test files.
- `test.class_name` is the fully qualified Java class name when resolvable from the
  source file package and class declaration.
- `test.source_path` is the repository-relative Java source path.
- `test.framework_signals` contains only directly visible framework signals from imports
  or annotations in the test source file for emitted test classes. The v0.1 implementation
  emits signal names `"JUnit Jupiter"`, `"JUnit 4"`, and `"Spring Test"` when detectable.
  It is empty when no supported direct signal is visible. Source-file-level import
  evidence is attached only to top-level emitted test classes; nested emitted test
  classes use their own class or method annotation evidence so imports are not repeated
  as nested-class signals.
- Spring Test signals are trusted only when the annotation origin is visible as a
  supported `org.springframework.test.*` or `org.springframework.boot.test.*` type
  through an exact fully qualified annotation name or explicit single-type import, and
  that exact framework type is not declared by scanned source. Unresolved simple-name
  annotations, wildcard-import-only annotations, same-package/local fake annotations,
  source-declared fake framework annotations, and static-import-only references do not
  emit `Spring Test` framework signals.
- `framework_signal.name` is the detected framework family name.
- `framework_signal.evidence_ids` references direct import or annotation evidence and
  must resolve to records in `evidence-index.jsonl`.
- `test.tested_subjects` contains only naming-convention relations inferred by stripping
  supported test suffixes such as `Test`, `Tests`, or `IT` and matching the resulting
  simple name against production classes under `src/main/java`. It is empty when no
  production class match is found.
- `tested_subject.class_name` is a candidate production class name.
- `tested_subject.support_type` is `"inferred"` for v0.1 naming-convention
  relations.
- `tested_subject.confidence` is `"medium"` for a single naming-convention production
  class match and `"low"` for duplicate or ambiguous production class matches.
- `tested_subject.uncertainty` is `null` for a single naming-convention match and
  `"ambiguous_subject_name"` when multiple production classes share the candidate simple
  name.
- `tested_subject.evidence_ids` references the test class evidence and candidate
  production class evidence that led to the inferred relation. These IDs must resolve to
  records in `evidence-index.jsonl`.
- `test.evidence_ids` references direct test class evidence and must resolve to records
  in `evidence-index.jsonl`.
- The tests inventory does not claim code coverage, test execution results, direct
  behavioral assertion analysis, call graph resolution, or complete subject mapping.

### v0.2 Module-Aware Maven Contract

This section defines the current public v0.2 module-aware Maven JSON contract.

The v0.2 module-aware contract uses:

- `schema_version: "0.2"` only for an atomic public output state that includes both
  `project.modules` and direct `module_id` fields on every emitted module-owned
  endpoint, warning, component, entity, and test fact.
- The same four output files under `.project-memory/`.
- Existing v0.1 fact arrays, with direct `module_id` fields added to module-owned facts.
- Existing evidence fields; Maven module discovery reuses `build_file` evidence.

The v0.2 `project-map.json` project shape is:

```json
{
  "schema_version": "0.2",
  "project": {
    "root": ".",
    "build": {
      "system": "maven",
      "root_build_file": "pom.xml",
      "evidence_ids": [
        "ev:pom.xml:1-1:build_file:pom.xml"
      ]
    },
    "source_roots": [
      "src/main/java",
      "services/orders/src/main/java"
    ],
    "test_roots": [
      "src/test/java",
      "services/orders/src/test/java"
    ],
    "modules": {
      "analysis_status": "analyzed",
      "items": [
        {
          "module_id": "module:.",
          "module_path": ".",
          "pom_path": "pom.xml",
          "source_roots": ["src/main/java"],
          "test_roots": ["src/test/java"],
          "support_status": "supported",
          "declaration_kind": "scan_root",
          "declared_path": ".",
          "declaration_evidence_ids": [],
          "pom_evidence_ids": [
            "ev:pom.xml:1-1:build_file:pom.xml"
          ]
        },
        {
          "module_id": "module:services/orders",
          "module_path": "services/orders",
          "pom_path": "services/orders/pom.xml",
          "source_roots": ["services/orders/src/main/java"],
          "test_roots": ["services/orders/src/test/java"],
          "support_status": "supported",
          "declaration_kind": "root_modules_entry",
          "declared_path": "services/orders",
          "declaration_evidence_ids": [
            "ev:pom.xml:14-14:build_file:module:services/orders"
          ],
          "pom_evidence_ids": [
            "ev:services/orders/pom.xml:1-1:build_file:pom.xml"
          ]
        }
      ]
    }
  }
}
```

Module identity rules:

- `module_id` is stable within a repository because it is derived from the normalized
  repository-relative module path, not from Maven coordinates, artifact IDs, display
  names, parent POMs, or effective POM data.
- The scan root module is `module:.` with `module_path: "."`.
- A child module is `module:<module_path>`, where `<module_path>` is a normalized
  slash-separated repository-relative path with no leading `./`, no trailing slash, no
  absolute path prefix, and no `.` or `..` path segments.
- `module_path` is the normalized repository-relative directory path for the module. It
  is `"."` only for the scan root.
- `pom_path` is the repository-relative POM path for valid modules with a detected POM,
  or `null` when a valid root declaration is missing its child POM.
- Maven profile resolution, effective POM reconstruction, parent inheritance, dependency
  graph reconstruction, and Maven execution are not part of module identity.

Single-module compatibility rules:

- v0.2 single-module scans use `schema_version: "0.2"` and include one module item for
  the scan root with `module_id: "module:."`.
- There is no valid inventory-only `schema_version: "0.2"` state. Normal public
  scan output must not emit `project.modules` under `schema_version: "0.2"` while any
  emitted module-owned endpoint, warning, component, entity, or test fact lacks
  `module_id`.
- v0.2 single-module scans keep the existing output files and preserve v0.1 top-level
  `project.source_roots`, `project.test_roots`, and root-module fact ID shapes.
- v0.2 multi-module scans keep `project.source_roots` and `project.test_roots` as
  compatibility summaries containing all supported repository-relative roots sorted
  deterministically. Per-module roots in `project.modules.items` are authoritative.

Module inventory rules:

- `project.modules.analysis_status` is `"analyzed"` when module discovery runs. It may
  be `"not_detected"` when no Maven build input is available for module discovery or
  when the root `pom.xml` is skipped because it exceeds the Maven POM byte limit.
- `project.modules.items` contains the scan root when the scan is single-module or when
  the root has supported production, test, or resource roots, plus valid unique child
  module paths declared by the root `<modules>` section.
- For compatibility with pre-v0.2 local source-root scans, when no root `pom.xml` is
  present but supported root source, test, or resource roots are detected,
  `project.modules` uses `analysis_status: "not_detected"` and emits a scan-root module
  with `module_id: "module:."`, `module_path: "."`, `pom_path: null`, empty POM
  evidence, and the detected root source or test roots.
- `source_roots` and `test_roots` inside a module item contain repository-relative roots
  under that module. They are empty arrays when no supported root of that kind is
  detected.
- `support_status` is one of:
  - `"supported"`: at least one supported production, test, or resource root is detected
    for the module.
  - `"missing_child_pom"`: the root declaration normalized to a valid repository-relative
    module path, but `<module_path>/pom.xml` is missing.
  - `"unsupported"`: a valid child POM is present, but the module has no supported
    Java production, test, or resource roots for the current analyzer slice.
- `declaration_kind` is `"scan_root"` for the root module and `"root_modules_entry"` for
  modules declared in root `<modules>`.
- `declared_path` preserves the deterministic normalized declaration used to derive
  `module_path`; it is `"."` for the scan root.
- `declaration_evidence_ids` references root `<module>` declaration evidence for child
  modules and is an empty array for the scan root.
- `pom_evidence_ids` references `build_file` evidence for the detected and accepted
  root or child `pom.xml`. It is an empty array for a valid declaration whose child POM
  is missing or whose child POM is skipped because it exceeds the Maven POM byte limit.

Fact-level module identity rules:

- Endpoint facts, component facts, entity facts, test facts, and warning items include a
  direct `module_id` field in v0.2.
- Request parameters, endpoint `mapping_source`, entity identifier fields, entity
  relationships, and test framework signals inherit the `module_id` of their parent fact
  and do not repeat it.
- `tested_subjects` relations include `target_module_id` for the matched production
  class. The initial v0.2 naming-convention inference is same-module only, so
  `target_module_id` is expected to match the parent test fact `module_id`.
- Entity relationships continue to use `target_resolution: "declared_type_only"` and
  `uncertainty: "target_type_not_resolved"`; v0.2 module identity does not imply target
  entity resolution.
- Root-module fact IDs keep the v0.1 ID shape for single-module compatibility. Child
  module facts include the module identity in their stable IDs to avoid collisions with
  facts from other modules.

Example v0.2 endpoint fact:

```json
{
  "id": "endpoint:module:services/orders:com.example.orders.OrderController#getOrder",
  "module_id": "module:services/orders",
  "controller_class": "com.example.orders.OrderController",
  "handler_method": "getOrder",
  "http_methods": ["GET"],
  "http_method_semantics": "declared",
  "paths": ["/orders/{id}"],
  "request_parameters": [],
  "request_body_type": null,
  "response_type": "com.example.orders.OrderDto",
  "mapping_source": {
    "kind": "direct_handler_method",
    "declaring_type": "com.example.orders.OrderController",
    "declaring_method": "getOrder",
    "binding": "direct",
    "uncertainty": null,
    "evidence_ids": [
      "ev:services/orders/src/main/java/com/example/orders/OrderController.java:20-20:com.example.orders.OrderController#getOrder:@GetMapping"
    ]
  },
  "evidence_ids": [
    "ev:services/orders/src/main/java/com/example/orders/OrderController.java:18-18:com.example.orders.OrderController:@RestController",
    "ev:services/orders/src/main/java/com/example/orders/OrderController.java:20-20:com.example.orders.OrderController#getOrder:@GetMapping"
  ]
}
```

Module warning rules:

- In v0.2 output, `warnings.analysis_status` is `"analyzed"` when at least one
  warning-producing analyzer runs, including Maven module discovery or hidden HTTP
  surface analysis. It is `"not_detected"` only when no warning-producing analyzer has
  supported input.
- If Maven module discovery runs and produces `maven_module` warnings but hidden HTTP
  surface analysis does not run because no supported Java production source root exists,
  `warnings.analysis_status` is still `"analyzed"` and `warnings.items` contains only the
  module warnings that were actually produced.
- v0.2 Maven module discovery warnings are emitted in `warnings.items` with
  `category: "maven_module"`.
- Warning items include direct `module_id` when a valid module path exists. `module_id`
  is `null` for invalid declarations that cannot produce a valid module identity.
- `source_path` is the repository-relative path that produced the warning, usually
  `pom.xml` for root declarations or `<module_path>/pom.xml` for nested declarations.
- Warning IDs begin with `warning:maven_module:<signal>:` and use only normalized
  repository-relative module paths or deterministic declaration ordinals as
  discriminators.
- A module declaration ordinal is the one-based document-order index of a root
  `<modules><module>` declaration. Ordinals are rendered in warning IDs as zero-padded
  `decl:000001` style suffixes so invalid or duplicate declarations on the same line
  cannot collide.
- Supported module warning signals are:
  - `"invalid_module_path"`: the `<module>` text is empty, absolute, contains unsupported
    `.` or `..` segments, or resolves outside the scanned repository root. It emits one
    warning per invalid declaration, uses `module_id: null`, does not create a module
    inventory item, and uses an ID shaped as
    `warning:maven_module:invalid_module_path:decl:<ordinal>`.
  - `"missing_child_pom"`: a valid root module path does not contain
    `<module_path>/pom.xml`. It emits at most one warning per normalized module path and
    uses an ID shaped as `warning:maven_module:missing_child_pom:<module_path>`.
  - `"duplicate_module_path"`: more than one root `<module>` declaration resolves to the
    same normalized module path. The first valid declaration may be processed once; later
    duplicates each emit a warning and are ignored as duplicate module items. The warning
    ID includes both the normalized module path and duplicate declaration ordinal, shaped
    as `warning:maven_module:duplicate_module_path:<module_path>:decl:<ordinal>`.
  - `"nested_module_declaration"`: a supported child module POM declares its own
    `<modules>` section. v0.2 records the warning but does not recursively discover
    nested modules. It emits at most one warning per supported child module path and uses
    an ID shaped as `warning:maven_module:nested_module_declaration:<module_path>`.
  - `"unsupported_module"`: a valid child POM is present, but no supported Java
    production, test, or resource roots are detected for the current analyzer slice. It
    emits at most one warning per normalized module path and uses an ID shaped as
    `warning:maven_module:unsupported_module:<module_path>`.
- Invalid declarations do not create module inventory items. Duplicate declarations do
  not create duplicate module inventory items. Valid missing or unsupported modules may
  appear in `project.modules.items` with the corresponding `support_status`.
- Module warnings do not create endpoint, component, entity, or test facts.

Example v0.2 module warning:

```json
{
  "id": "warning:maven_module:missing_child_pom:services/missing",
  "category": "maven_module",
  "signal": "missing_child_pom",
  "module_id": "module:services/missing",
  "message": "Maven module declared in root pom.xml does not have a child pom.xml; v0.2 does not analyze this module.",
  "source_path": "pom.xml",
  "evidence_ids": [
    "ev:pom.xml:18-18:build_file:module:services/missing"
  ]
}
```

Deterministic sorting rules:

- Module inventory items are sorted with `module:.` first, followed by lexicographic
  `module_path` order.
- Top-level `project.source_roots` and `project.test_roots` are sorted
  repository-relative path strings.
- Module-aware endpoints are sorted by module order first, then by the existing v0.1
  endpoint sort keys: first path, HTTP methods, method semantics, controller class, and
  handler method.
- Module-aware component, entity, and test items are sorted by module order first, then
  by their existing v0.1 sort keys.
- Module-aware warning items are sorted by `category`, `signal`, module order,
  `source_path`, and `id`.
- For warning items with `module_id: null`, module order uses the declaration ordinal
  after all concrete module IDs for the same `category` and `signal`; the final `id`
  sort key keeps multiple invalid declarations deterministic.
- Duplicate declaration warnings sort with their normalized module path through
  `module_id`, then by `id`, whose ordinal suffix preserves declaration-specific order
  and prevents ID collisions.
- Evidence entries keep the existing sort order by path, line range, class, method,
  symbol, and ID.

### Current v0.3 Build And Configuration Contract

This section defines the v0.3 build/configuration JSON contract. The current
implementation emits source-visible Maven metadata, source-visible Maven dependency
inventory, source-visible Maven plugin inventory, standard resource-root inventory,
path-only supported application/logging config-file inventory, direct source-visible
Spring Boot application signals, and the complete `build_config` section shell.

The v0.3 contract uses:

- `schema_version: "0.3"` only for an atomic public output state that keeps the v0.2
  module-aware contract and adds the complete v0.3 build/config section shape for every
  emitted module item.
- The same four output files under `.project-memory/`.
- A module-owned `build_config` object inside each `project.modules.items[]` entry.
- Existing evidence fields and evidence types. Maven observations reuse `build_file`;
  configuration-file observations reuse `config_file`; resource-root inventory entries
  use empty evidence IDs in the v0.3 contract; Spring Boot application signals reuse
  `annotation` and `code_symbol`.

Schema and compatibility rules:

- `schema_version: "0.3"` builds on the v0.2 boundary. Normal public v0.3 output must
  still include `project.modules` and direct `module_id` fields on emitted module-owned
  endpoint, warning, component, entity, and test facts.
- There is no valid public partial `schema_version: "0.3"` state where only Maven
  metadata, only dependencies, only plugins, only config files, or only Spring Boot
  application signals are emitted while other required v0.3 `build_config` subsection
  shells are absent.
- `analysis_status: "not_analyzed"` is valid only for explicit not-analyzed subsection
  shells. It means the subsection made no absence claim. Once a subsection analyzer
  exists, it must use that subsection's normal `"analyzed"`/`"not_detected"` rules
  instead.
- v0.3 single-module scans keep the existing output files and preserve v0.2
  single-module compatibility for root-module fact IDs, top-level `project.source_roots`,
  and top-level `project.test_roots`.
- Root-level `project.build` remains a scan-level compatibility summary for build system
  detection. It must not become an effective Maven model.

The v0.3 module item shape extends the v0.2 item shape like this:

```json
{
  "module_id": "module:services/orders",
  "module_path": "services/orders",
  "pom_path": "services/orders/pom.xml",
  "source_roots": ["services/orders/src/main/java"],
  "test_roots": ["services/orders/src/test/java"],
  "support_status": "supported",
  "declaration_kind": "root_modules_entry",
  "declared_path": "services/orders",
  "declaration_evidence_ids": [
    "ev:pom.xml:14-14:build_file:module:services/orders"
  ],
  "pom_evidence_ids": [
    "ev:services/orders/pom.xml:1-1:build_file:pom.xml"
  ],
  "build_config": {
    "analysis_status": "analyzed",
    "maven": {
      "metadata": {
        "analysis_status": "analyzed",
        "group_id": {
          "value": "com.example",
          "value_kind": "literal",
          "evidence_ids": [
            "ev:services/orders/pom.xml:5-5:build_file:maven:project:groupId"
          ]
        },
        "artifact_id": {
          "value": "orders-service",
          "value_kind": "literal",
          "evidence_ids": [
            "ev:services/orders/pom.xml:6-6:build_file:maven:project:artifactId"
          ]
        },
        "version": {
          "value": "${revision}",
          "value_kind": "property_reference",
          "evidence_ids": [
            "ev:services/orders/pom.xml:7-7:build_file:maven:project:version"
          ]
        },
        "packaging": {
          "value": null,
          "value_kind": "not_declared",
          "evidence_ids": []
        },
        "parent": {
          "analysis_status": "analyzed",
          "group_id": {
            "value": "com.example",
            "value_kind": "literal",
            "evidence_ids": [
              "ev:services/orders/pom.xml:10-10:build_file:maven:parent:groupId"
            ]
          },
          "artifact_id": {
            "value": "example-parent",
            "value_kind": "literal",
            "evidence_ids": [
              "ev:services/orders/pom.xml:11-11:build_file:maven:parent:artifactId"
            ]
          },
          "version": {
            "value": "1.0.0",
            "value_kind": "literal",
            "evidence_ids": [
              "ev:services/orders/pom.xml:12-12:build_file:maven:parent:version"
            ]
          },
          "relative_path": {
            "value": "../pom.xml",
            "value_kind": "literal",
            "evidence_ids": [
              "ev:services/orders/pom.xml:13-13:build_file:maven:parent:relativePath"
            ]
          }
        }
      },
      "dependencies": {
        "analysis_status": "analyzed",
        "items": [
          {
            "id": "maven_dependency:module:services/orders:direct:org.springframework.boot:spring-boot-starter-web:decl:000001",
            "declaration_kind": "direct_dependency",
            "declaration_ordinal": 1,
            "group_id": {
              "value": "org.springframework.boot",
              "value_kind": "literal",
              "evidence_ids": [
                "ev:services/orders/pom.xml:24-24:build_file:maven:dependency:000001:groupId"
              ]
            },
            "artifact_id": {
              "value": "spring-boot-starter-web",
              "value_kind": "literal",
              "evidence_ids": [
                "ev:services/orders/pom.xml:25-25:build_file:maven:dependency:000001:artifactId"
              ]
            },
            "version": {
              "value": null,
              "value_kind": "not_declared",
              "evidence_ids": []
            },
            "scope": {
              "value": null,
              "value_kind": "not_declared",
              "evidence_ids": []
            },
            "optional": {
              "value": null,
              "value_kind": "not_declared",
              "evidence_ids": []
            },
            "type": {
              "value": null,
              "value_kind": "not_declared",
              "evidence_ids": []
            },
            "classifier": {
              "value": null,
              "value_kind": "not_declared",
              "evidence_ids": []
            },
            "evidence_ids": [
              "ev:services/orders/pom.xml:23-27:build_file:maven:dependency:000001"
            ]
          }
        ]
      },
      "dependency_management": {
        "analysis_status": "analyzed",
        "items": []
      },
      "plugins": {
        "analysis_status": "analyzed",
        "items": [
          {
            "id": "maven_plugin:module:services/orders:direct:org.openapitools:openapi-generator-maven-plugin:decl:000001",
            "declaration_kind": "direct_plugin",
            "declaration_ordinal": 1,
            "group_id": {
              "value": "org.openapitools",
              "value_kind": "literal",
              "evidence_ids": [
                "ev:services/orders/pom.xml:45-45:build_file:maven:plugin:000001:groupId"
              ]
            },
            "artifact_id": {
              "value": "openapi-generator-maven-plugin",
              "value_kind": "literal",
              "evidence_ids": [
                "ev:services/orders/pom.xml:46-46:build_file:maven:plugin:000001:artifactId"
              ]
            },
            "version": {
              "value": "${openapi.generator.version}",
              "value_kind": "property_reference",
              "evidence_ids": [
                "ev:services/orders/pom.xml:47-47:build_file:maven:plugin:000001:version"
              ]
            },
            "executions": [
              {
                "execution_id": "generate-api",
                "phase": {
                  "value": "generate-sources",
                  "value_kind": "literal",
                  "evidence_ids": [
                    "ev:services/orders/pom.xml:53-53:build_file:maven:plugin:000001:execution:000001:phase"
                  ]
                },
                "goals": [
                  {
                    "value": "generate",
                    "value_kind": "literal",
                    "evidence_ids": [
                      "ev:services/orders/pom.xml:56-56:build_file:maven:plugin:000001:execution:000001:goal:generate"
                    ]
                  }
                ],
                "evidence_ids": [
                  "ev:services/orders/pom.xml:50-58:build_file:maven:plugin:000001:execution:000001"
                ]
              }
            ],
            "configuration_signals": [
              {
                "signal": "input_spec_config_present",
                "evidence_ids": [
                  "ev:services/orders/pom.xml:61-61:build_file:maven:plugin:000001:configuration:inputSpec"
                ]
              }
            ],
            "generator_signals": [
              {
                "signal": "openapi_swagger_codegen",
                "evidence_ids": [
                  "ev:services/orders/pom.xml:46-46:build_file:maven:plugin:000001:artifactId"
                ]
              }
            ],
            "evidence_ids": [
              "ev:services/orders/pom.xml:44-63:build_file:maven:plugin:000001"
            ]
          }
        ]
      },
      "plugin_management": {
        "analysis_status": "analyzed",
        "items": []
      }
    },
    "resources": {
      "analysis_status": "analyzed",
      "items": [
        {
          "id": "resource_root:module:services/orders:main:services/orders/src/main/resources",
          "scope": "main",
          "path": "services/orders/src/main/resources",
          "evidence_ids": []
        }
      ]
    },
    "config_files": {
      "analysis_status": "analyzed",
      "items": [
        {
          "id": "config_file:module:services/orders:spring_application:services/orders/src/main/resources/application-prod.yml",
          "path": "services/orders/src/main/resources/application-prod.yml",
          "resource_scope": "main",
          "config_kind": "spring_application",
          "format": "yaml",
          "profile_name": "prod",
          "profile_source": "filename_only",
          "evidence_ids": [
            "ev:services/orders/src/main/resources/application-prod.yml:unknown:config_file:application-prod.yml"
          ]
        }
      ]
    },
    "spring_boot_applications": {
      "analysis_status": "analyzed",
      "items": [
        {
          "id": "spring_boot_application:module:services/orders:com.example.orders.OrdersApplication",
          "class_name": "com.example.orders.OrdersApplication",
          "source_path": "services/orders/src/main/java/com/example/orders/OrdersApplication.java",
          "application_signal": "spring_boot_application_with_main_method",
          "main_method": {
            "present": true,
            "evidence_ids": [
              "ev:services/orders/src/main/java/com/example/orders/OrdersApplication.java:12-12:com.example.orders.OrdersApplication#main:code_symbol"
            ]
          },
          "evidence_ids": [
            "ev:services/orders/src/main/java/com/example/orders/OrdersApplication.java:8-8:com.example.orders.OrdersApplication:@SpringBootApplication",
            "ev:services/orders/src/main/java/com/example/orders/OrdersApplication.java:12-12:com.example.orders.OrdersApplication#main:code_symbol"
          ]
        }
      ]
    }
  }
}
```

Build/config analysis status rules:

- `build_config.analysis_status` is `"analyzed"` when at least one v0.3
  build/config analyzer runs for the module. It is `"not_detected"` when the module has
  no supported POM, source, resource, or config input for v0.3 build/config analysis.
- Maven subsection `analysis_status` values are `"analyzed"` when a module POM is
  present and parsed for the relevant subsection. They are `"not_detected"` when the
  module has no POM available to that analyzer.
- In the current v0.3 Maven analysis,
  `maven.metadata.analysis_status`, `dependencies.analysis_status`, and
  `dependency_management.analysis_status`, plus `plugins.analysis_status` and
  `plugin_management.analysis_status` are `"analyzed"` when a module POM is present and
  parsed for the relevant direct POM observations.
- Resource and config subsection `analysis_status` values are `"analyzed"` when standard
  resource roots are present and the relevant analyzer runs, even when the resulting
  config item list is empty. They are `"not_detected"` when no supported resource input
  root exists.
- Spring Boot application subsection `analysis_status` values are `"analyzed"` when
  supported production source roots are present and the analyzer runs, even when no
  direct `@SpringBootApplication` class signal is detected. They are `"not_detected"`
  when no supported production source root exists for that module.

Maven value rules:

- Maven scalar values use the object shape `{ "value": ..., "value_kind": ...,
  "evidence_ids": [...] }`.
- `value_kind` is one of:
  - `"literal"`: directly declared literal XML text.
  - `"property_reference"`: directly declared `${...}` style property reference.
  - `"expression"`: directly declared text containing non-literal Maven expressions.
  - `"not_declared"`: the XML element is absent.
  - `"unsupported"`: the XML element is present but cannot be represented
    deterministically.
- Missing `groupId`, `version`, or `packaging` values must not be filled from parent
  inheritance, Maven defaults, dependency management, active profiles, or effective POM
  behavior.
- Parent coordinates are recorded only under `metadata.parent`. Recording parent
  coordinates does not resolve the module's effective coordinates.
- `metadata.parent.analysis_status` is `"analyzed"` when a direct `<parent>` element is
  present in the module POM and `"not_detected"` when no direct `<parent>` element is
  present. Parent value fields use the same Maven scalar value object as module metadata.

Dependency inventory rules:

- `dependencies.items` contains only direct `<dependencies><dependency>` declarations in
  the module POM.
- `dependency_management.items` contains only direct
  `<dependencyManagement><dependencies><dependency>` declarations in the module POM.
- `declaration_kind` is `"direct_dependency"` for active direct declarations and
  `"dependency_management"` for management declarations.
- Dependency management declarations must not be rendered as active dependencies.
- Dependency facts preserve source-visible coordinate, scope, optional, type, and
  classifier text. They do not claim resolved versions, transitive dependencies,
  inherited scopes, profile activation, conflict mediation, repository availability, or
  effective dependency graphs.
- Property references remain source-visible `property_reference` values. v0.3 does not
  resolve project properties.

Plugin inventory and generator signal rules:

- `plugins.items` contains only direct `<build><plugins><plugin>` declarations in the
  module POM.
- `plugin_management.items` contains only direct
  `<build><pluginManagement><plugins><plugin>` declarations in the module POM.
- `declaration_kind` is `"direct_plugin"` for direct plugin declarations and
  `"plugin_management"` for plugin-management declarations.
- Plugin-management declarations must not be rendered as active execution behavior.
- Plugin facts may include direct source-visible execution IDs, phases, and goals.
  They must not reconstruct Maven lifecycle bindings, default goals, inherited
  executions, resolved plugin versions, or full plugin execution behavior.
- `configuration_signals` records only bounded signal names and evidence IDs. It must not
  store arbitrary plugin configuration values.
- Planned bounded configuration signals include
  `"input_spec_config_present"`, `"generated_sources_config_present"`,
  `"annotation_processor_paths_present"`, and `"add_source_goal_present"`.
- `generator_signals` records conservative plugin-level signals such as
  `"openapi_swagger_codegen"`, `"source_generator_plugin"`, and
  `"annotation_processor"`.
- Generator and OpenAPI/Swagger plugin signals do not create endpoint facts, API
  operation facts, generated source facts, or generated API reconstruction.

Resource and config discovery rules:

- Resource roots are repository-relative paths under supported modules.
- `resource.scope` is `"main"` for `src/main/resources` and `"test"` for
  `src/test/resources`.
- Resource-root entries are path inventory facts. In the v0.3 contract they use empty
  `evidence_ids`, matching the current source-root and test-root summary pattern,
  because the existing evidence model has no directory evidence type.
- Config file facts record file paths and filename-derived metadata only.
- `config_kind` is one of:
  - `"spring_application"` for `application.properties`, `application.yml`,
    `application.yaml`, and supported `application-*` profile filenames.
  - `"logging_config"` for supported logging configuration filenames.
- `format` is one of `"properties"`, `"yaml"`, `"xml"`, or `"unknown"`.
- `profile_name` is the filename-derived profile segment for profile-specific Spring
  application files, or `null` for default application files and non-profile config
  files.
- `profile_source` is `"filename_only"` when `profile_name` is present and `null`
  otherwise.
- Profile names do not imply profile activation, runtime precedence, environment
  selection, or effective Spring configuration.
- Config discovery must not parse or store property keys, property values, YAML node
  content, XML element content, environment placeholders, decrypted secrets, or config
  excerpts.

Spring Boot application signal rules:

- `spring_boot_applications.items` contains direct source-visible
  `@SpringBootApplication` class signals under supported production source roots.
- `application_signal` is one of:
  - `"spring_boot_application_annotation_only"` when the annotation is present but no
    supported source-visible `main` method is detected on that class.
  - `"spring_boot_application_with_main_method"` when the annotation and a supported
    source-visible `main` method are both detected on that class.
- These facts do not claim executable jar packaging, active profiles, runtime
  auto-configuration, bean graph, component scanning result, deployment behavior, or
  actual process entrypoint behavior.

v0.3 warning rules:

- Generated-source and generator warnings are emitted in `warnings.items`.
- Generated-source warning items use `category: "generated_source"`.
- Plugin-derived generated-source warning IDs use the shape
  `warning:generated_source:<signal>:module:<module_path>:<declaration_kind>:decl:<ordinal>`.
  `<declaration_kind>` is the emitted plugin `declaration_kind` value and distinguishes
  direct plugin declarations from `pluginManagement` declarations when their declaration
  ordinals would otherwise collide.
- POM-derived plugin, annotation-processor, and generated-source configuration warnings
  include the module path, declaration kind, and bounded declaration ordinal in the
  warning ID.
  Repository-path generated-source root warnings include the detected normalized
  generated-source root path.
- Warnings include direct `module_id` when the signal belongs to a valid module.
- Current plugin-derived generated-source warning signals include:
  - `"maven_generator_plugin"`;
  - `"maven_openapi_swagger_codegen_plugin"`;
  - `"maven_annotation_processor"`;
  - `"maven_generated_source_config"`;
  - `"maven_build_helper_add_source"`.
- Current generated-source path warnings include:
  - `"generated_source_root_path_detected"`.
- Path-derived generated-source warning IDs use
  `warning:generated_source:generated_source_root_path_detected:path:<generated_source_path_key>`
  for the scan-root module and
  `warning:generated_source:generated_source_root_path_detected:module:<module_path>:path:<generated_source_path_key>`
  for child modules. `<generated_source_path_key>` uses the same percent-encoded
  repository-relative path key rules as v0.4 spec paths.
- OpenAPI/Swagger plugin declarations may also continue to emit the existing
  `hidden_http_surface` warning signal where appropriate. The warning remains a warning
  and must not create endpoint or API facts.
- If the same OpenAPI/Swagger plugin declaration supports both a `generated_source`
  warning and an existing `hidden_http_surface` warning, each warning keeps its own
  category and ID namespace.
- Warning messages must use detected-signal wording. They must not summarize generated
  source contents, generated API operations, runtime build behavior, or effective Maven
  execution.

Sensitive config handling rules:

- `project-map.json`, `project-graph.json`, `evidence-index.jsonl`, `endpoints.md`,
  and `agent-guide.md` must not include config file contents, property keys, property
  values, YAML node content, XML element content, decrypted values, or secret-looking
  values from config files.
- `config_file` evidence excerpts for v0.3 config discovery must be bounded path or
  filename observations such as `config file detected: application.yml`.
- Config file discovery is bounded before fact or evidence materialization. The current
  implementation keeps at most 4096 supported config file candidates per resource root
  in normalized repository-relative path order. Candidates outside that bound do not
  create config facts or `config_file` evidence records.
- Any future proposal to store config keys, selected safe values, or source excerpts from
  config files requires an explicit contract update, evidence model update if needed,
  sensitive-fixture tests, and risk-based security review.

Deterministic sorting rules:

- Build/config module sections follow the existing v0.2 module order.
- Dependency and dependency-management items are sorted by `group_id.value`,
  `artifact_id.value`, `type.value`, `classifier.value`, `scope.value`,
  `declaration_ordinal`, and `id`, with `null` values sorting after strings.
- Plugin and plugin-management items are sorted by `group_id.value`,
  `artifact_id.value`, `declaration_ordinal`, and `id`.
- Resource roots are sorted by `scope`, `path`, and `id`.
- Config files are sorted by `resource_scope`, `config_kind`, `path`, and `id`.
- Spring Boot application signals are sorted by `class_name`, `source_path`, and `id`.
- Generated-source warnings follow the existing warning sort order by `category`,
  `signal`, module order, `source_path`, and `id`.

### v0.4 Declared And Generated API Surface Contract

This section defines the current v0.4 API surface contract slice. The release
implementation emits the API surface shell, endpoint categories, local OpenAPI/Swagger
spec file facts, minimal spec-backed OpenAPI/Swagger operation facts, and conservative
generated-source path warnings with `path_signal` evidence while keeping
generated-source content scanning non-default.

The v0.4 contract uses:

- `schema_version: "0.4"` for the public output state that preserves the v0.3
  module-aware build/config contract and adds the API surface section shape.
- The same four output files under `.project-memory/`.
- Existing top-level `endpoints[]` as the canonical collection of code-backed
  source-visible Spring MVC endpoint facts.
- An `api_surface_category` field on each endpoint fact, with values
  `"source_visible_spring_mvc_endpoint"` or
  `"interface_declared_spring_mvc_endpoint"`.
- A top-level `api_surface` object that categorizes source-visible endpoint IDs, local
  OpenAPI/Swagger spec file facts, declared OpenAPI operations, generated-source API
  warning IDs, repository-rest warning IDs, and hidden HTTP warning IDs without turning
  every API-adjacent signal into an endpoint fact.
- Current `api_spec` evidence for local OpenAPI/Swagger spec file and operation facts.
  Current `path_signal` evidence for generated-source path warning signals.

Taxonomy rules:

- `source_visible_spring_mvc_endpoint` means a direct Spring MVC mapping on a concrete
  source-visible controller handler. It is code-backed by supported Java source evidence
  and remains in `endpoints[]`.
- `interface_declared_spring_mvc_endpoint` means a source-visible interface-declared
  Spring MVC mapping uniquely bound to a concrete source-visible controller handler. It
  is code-backed by interface mapping evidence and concrete binding evidence, remains in
  `endpoints[]`, and must stay separate from direct handler mappings.
- `openapi_declared_operation` means a path/method operation declared in a local
  OpenAPI/Swagger spec file. It is spec-backed, lives outside `endpoints[]`, and must not
  imply implementation by Spring MVC or generated code.
- `generated_source_api_signal` means a build/config/path signal that generated API
  source may exist. It is a warning/signal only unless a later explicit generated-source
  scan mode is designed and enabled.
- `repository_rest_warning` means a direct source-visible `@RepositoryRestResource`
  signal. It remains a warning and must not create repository REST endpoint facts until
  a deterministic repository-rest model is designed.
- `hidden_http_warning` means a bounded HTTP/API-adjacent signal that cannot be
  represented as a source endpoint, spec operation, generated-source API signal, or
  repository-rest warning. It remains a warning.

Planned high-level `project-map.json` shape:

```json
{
  "schema_version": "0.4",
  "endpoints": [
    {
      "id": "endpoint:module:services/orders:com.example.orders.OrderController#getOrder",
      "module_id": "module:services/orders",
      "api_surface_category": "source_visible_spring_mvc_endpoint",
      "mapping_source": {
        "kind": "direct_handler_method"
      }
    }
  ],
  "api_surface": {
    "analysis_status": "analyzed",
    "source_visible_spring_mvc_endpoints": {
      "analysis_status": "analyzed",
      "endpoint_ids": [
        "endpoint:module:services/orders:com.example.orders.OrderController#getOrder"
      ]
    },
    "interface_declared_spring_mvc_endpoints": {
      "analysis_status": "analyzed",
      "endpoint_ids": []
    },
    "openapi": {
      "spec_files": {
        "analysis_status": "analyzed",
        "items": [
          {
            "id": "openapi_spec:module:services/orders:path:services/orders/src/main/resources/openapi.yml",
            "module_id": "module:services/orders",
            "spec_path": "services/orders/src/main/resources/openapi.yml",
            "format": "yaml",
            "spec_kind": "openapi",
            "version": "3.0.3",
            "evidence_ids": [
              "ev:services/orders/src/main/resources/openapi.yml:1-1:api_spec:openapi"
            ]
          }
        ]
      },
      "operations": {
        "analysis_status": "analyzed",
        "items": [
          {
            "id": "openapi_operation:module:services/orders:spec:services/orders/src/main/resources/openapi.yml:operation:get:/orders/{id}",
            "module_id": "module:services/orders",
            "api_surface_category": "openapi_declared_operation",
            "spec_path": "services/orders/src/main/resources/openapi.yml",
            "http_method": "GET",
            "path": "/orders/{id}",
            "operation_id": "getOrder",
            "tags": [
              "Orders"
            ],
            "implementation_status": "not_analyzed",
            "evidence_ids": [
              "ev:services/orders/src/main/resources/openapi.yml:12-12:api_spec:operation%3Aget%3A/orders/{id}"
            ]
          }
        ]
      }
    },
    "generated_source_api_signals": {
      "analysis_status": "analyzed",
      "warning_ids": []
    },
    "repository_rest_warnings": {
      "analysis_status": "analyzed",
      "warning_ids": []
    },
    "hidden_http_warnings": {
      "analysis_status": "analyzed",
      "warning_ids": []
    }
  }
}
```

API surface analysis status rules:

- `api_surface.analysis_status` is `"analyzed"` when any v0.4 API surface analyzer runs.
  It is `"not_detected"` only when no supported source, spec, build, generated-source
  path, or warning input is available.
- Endpoint category subsections use endpoint IDs that must resolve to existing
  `endpoints[]` facts. They must not duplicate endpoint payloads.
- `api_surface.openapi.spec_files.analysis_status` is `"analyzed"` when spec discovery
  runs, even if no spec files are detected. It is `"not_detected"` only when no
  supported discovery input exists.
- `api_surface.openapi.operations.analysis_status` is `"analyzed"` when parser
  extraction runs, including when supported spec files contain no usable operations or
  degrade to warnings, and `"not_detected"` when no supported local spec files are
  available to parse.
- Warning-reference subsections use warning IDs that must resolve to `warnings.items`.
  They must not duplicate warning payloads or create operation/endpoint facts.

OpenAPI spec file rules:

- Spec file fact IDs use the shape `openapi_spec:<module_id-or-unscoped>:path:<spec_path_key>`.
- `module_id` is the owning supported module ID when the spec path is inside a
  supported module, and `null` when the spec is outside supported modules. Fact IDs use
  `unscoped` for `null` module ownership.
- `spec_path_key` is derived from normalized `spec_path`. It preserves case and slash
  separators, and uses uppercase UTF-8 byte percent-encoding for `%`, `:`, whitespace,
  ASCII control characters, and any other character outside the bounded readable key set
  `A-Z`, `a-z`, `0-9`, `.`, `_`, `-`, `~`, `/`, `{`, and `}`.
- `spec_path` is a normalized repository-relative path. It must not be absolute, start
  with `./`, or escape the scanned repository root.
- Spec discovery treats symlink path entries as unsupported for spec facts to avoid
  content/path ownership mismatches. A regular target file inside the repository may be
  discovered only through its own normalized repository-relative path.
- `format` is one of `"yaml"` or `"json"` for currently supported filenames.
- `spec_kind` is `"openapi"` or `"swagger"` based on bounded local spec header content
  when directly visible, with a filename fallback when no bounded version signal is
  detected.
- `version` preserves the direct source-visible OpenAPI or Swagger version string when
  deterministically available and is `null` otherwise.
- Spec file facts prove only local spec presence and bounded version/kind observations.
  They do not prove runtime APIs, OpenAPI operations, or generated code.
- Spec file discovery is bounded before fact or evidence materialization. The current
  implementation keeps at most 4096 supported OpenAPI/Swagger spec file candidates in
  normalized repository-relative path order. Candidates outside that bound do not create
  spec file facts, operation parser input, or `api_spec` evidence records.

OpenAPI operation rules:

- Operation fact IDs use the shape
  `openapi_operation:<module_id>:spec:<spec_path_key>:operation:<http_method_key>:<operation_path_key>`.
- `operation_path_key` is derived from the declared OpenAPI/Swagger operation `path` with
  the same ID-key escaping as `spec_path_key`.
- `http_method_key` is the lowercase normalized HTTP method. It is used only for stable
  ID construction; the public `http_method` field uses the normalized display value.
- Valid OpenAPI/Swagger input must not produce more than one operation fact for the same
  spec path, HTTP method, and operation path. If duplicate declarations cannot be
  represented without an ID collision, the analyzer must degrade the duplicate condition
  to a warning instead of emitting colliding operation facts.
- Operation facts use `api_surface_category: "openapi_declared_operation"`.
- `http_method` is the normalized HTTP method declared under a spec path item.
- `path` is the declared OpenAPI/Swagger path template, not a Spring MVC path.
- `operation_id` is the direct `operationId` value when present and bounded, otherwise
  `null`.
- `tags` contains bounded direct tag strings when present and is an empty array when no
  tags are present or deterministically usable. The initial implementation preserves up
  to eight direct string tags of up to 120 characters each.
- Operation `operation_id` values longer than the bounded analyzer limit are emitted as
  `null` rather than serializing unbounded source-derived strings.
- `implementation_status` is `"not_analyzed"` in the initial v0.4 operation extraction
  contract. A spec operation must not be treated as implemented merely because a similar
  Spring MVC endpoint exists.
- Future endpoint/spec matching must use a separate relation that preserves both spec
  evidence and code evidence and labels support type, confidence, and uncertainty.
- Invalid or unsupported specs should degrade to warnings rather than crashing a scan or
  producing partial operation claims.
- Operation facts must not follow external `$ref` values, perform network access, fetch
  remote schemas, run code generation, or reconstruct client SDKs.
- Invalid, unsupported, oversized, or duplicate operation parser inputs may emit
  `hidden_http_surface` warnings such as `openapi_spec_parse_error`,
  `openapi_spec_unsupported`, or `openapi_spec_duplicate_operation` with bounded
  `api_spec` parse/status evidence.

Generated-source API signal rules:

- Generated-source API signals remain warnings. They may be referenced by
  `api_surface.generated_source_api_signals.warning_ids`.
- Build/config-derived generator signals use `build_file` evidence and warning
  categories. Path-derived generated-source root signals use `path_signal` evidence.
- API-surface generated-source warning references may include OpenAPI/Swagger generator
  plugin warnings, matching OpenAPI generator output configuration warnings, and
  generated-source root path warnings. They must not include generic annotation-processor
  warnings unless a future contract defines them as API-related.
- A generated-source path warning proves only normalized local path presence. It does
  not prove generated Java types, generated operations, generated endpoint handlers, or
  runtime behavior.
- The default scan must not read generated source roots such as `target/generated-sources`.
  Any future generated-source scan mode must be explicit, documented, non-default, and
  introduced with a separate output/evidence contract update.

Planned warning separation rules:

- `repository_rest_warning` must be separate from generic `hidden_http_warning` because
  it is a direct Spring Data REST annotation signal.
- `hidden_http_warning` is reserved for unknown, unsupported, invalid, or otherwise
  non-modeled HTTP/API-adjacent signals.
- Warning messages must use detected-signal wording and must not summarize generated
  source contents, OpenAPI schemas, examples, arbitrary descriptions, runtime build
  behavior, or effective Maven execution.

Deterministic sorting rules:

- Endpoint category ID lists follow existing endpoint sort order.
- Spec files are sorted by module order, `spec_path`, `spec_kind`, `format`, and `id`.
- OpenAPI operations are sorted by module order, `spec_path`, `path`, `http_method`,
  `operation_id`, and `id`, with `null` values sorting after strings.
- API surface warning ID lists follow the existing warning sort order for their
  referenced warning items.

### Current v0.5 Spring Application Surface Contract

This section defines the v0.5 Spring application surface contract. The current
implementation emits repository signals, configuration/bean/configuration-properties
signals, transaction/scheduled/event/messaging signals, and Spring Security
configuration warnings. Later work must not change the meaning of the repository,
configuration, behavior, messaging, or security-warning slices without updating this
contract and the evidence model where applicable.

The v0.5 contract uses:

- `schema_version: "0.5"` for an atomic public output state that preserves the v0.4
  module-aware build/config and API surface contract and adds the Spring application
  surface section shape.
- The same four output files under `.project-memory/`.
- A top-level `spring_application_surface` object that groups deeper Spring surface
  facts and warning references without changing the meaning of existing `components`,
  `entities`, `tests`, `endpoints`, `warnings`, or `api_surface` sections.
- Existing evidence fields and evidence types. Direct annotation-backed facts reuse
  `annotation` evidence; source-visible Java structural observations reuse
  `code_symbol` evidence; security warnings reuse `annotation` and `code_symbol`
  evidence.
- Direct `@Repository` and `@Configuration` observations may appear both as existing
  component stereotype facts and as category-specific Spring application surface items.
  This is two contract views over the same source observation, not evidence of multiple
  runtime beans or component registrations.
- In the current implementation, `repositories.analysis_status`,
  `configuration.*.analysis_status`, `behavior.*.analysis_status`, and
  `messaging.listener_signals.analysis_status` are `"analyzed"` when supported
  production source roots exist and their analyzers run. The
  `security.configuration_warnings.analysis_status` value is also `"analyzed"` when
  supported production source roots exist and the security warning analyzer runs.

v0.5 high-level `project-map.json` shape:

```json
{
  "schema_version": "0.5",
  "spring_application_surface": {
    "analysis_status": "analyzed",
    "repositories": {
      "analysis_status": "analyzed",
      "items": [
        {
          "id": "spring_repository_stereotype:module:services/orders:com.example.orders.DirectOrderRepository",
          "module_id": "module:services/orders",
          "surface_category": "spring_repository_stereotype",
          "support_type": "extracted",
          "class_name": "com.example.orders.DirectOrderRepository",
          "source_path": "services/orders/src/main/java/com/example/orders/DirectOrderRepository.java",
          "repository_signal": "direct_repository_stereotype",
          "evidence_ids": [
            "ev:services/orders/src/main/java/com/example/orders/DirectOrderRepository.java:8-8:com.example.orders.DirectOrderRepository:@Repository"
          ]
        },
        {
          "id": "spring_data_repository_interface_signal:module:services/orders:com.example.orders.OrderRepository",
          "module_id": "module:services/orders",
          "surface_category": "spring_data_repository_interface_signal",
          "support_type": "inferred",
          "class_name": "com.example.orders.OrderRepository",
          "source_path": "services/orders/src/main/java/com/example/orders/OrderRepository.java",
          "repository_signal": "spring_data_repository_interface_extension",
          "extends_types": [
            "org.springframework.data.jpa.repository.JpaRepository"
          ],
          "entity_relation_status": "not_analyzed",
          "evidence_ids": [
            "ev:services/orders/src/main/java/com/example/orders/OrderRepository.java:8-8:com.example.orders.OrderRepository:com.example.orders.OrderRepository",
            "ev:services/orders/src/main/java/com/example/orders/OrderRepository.java:8-8:com.example.orders.OrderRepository:extends:org.springframework.data.jpa.repository.JpaRepository"
          ]
        }
      ]
    },
    "configuration": {
      "configuration_classes": {
        "analysis_status": "analyzed",
        "items": [
          {
            "id": "spring_configuration_class:module:services/orders:com.example.orders.OrderConfiguration",
            "module_id": "module:services/orders",
            "surface_category": "spring_configuration_class",
            "support_type": "extracted",
            "class_name": "com.example.orders.OrderConfiguration",
            "source_path": "services/orders/src/main/java/com/example/orders/OrderConfiguration.java",
            "configuration_signal": "direct_configuration_class",
            "evidence_ids": [
              "ev:services/orders/src/main/java/com/example/orders/OrderConfiguration.java:8-8:com.example.orders.OrderConfiguration:@Configuration"
            ]
          }
        ]
      },
      "configuration_properties": {
        "analysis_status": "analyzed",
        "items": [
          {
            "id": "spring_configuration_properties_type:module:services/orders:com.example.orders.OrderProperties",
            "module_id": "module:services/orders",
            "surface_category": "spring_configuration_properties_type",
            "support_type": "extracted",
            "class_name": "com.example.orders.OrderProperties",
            "source_path": "services/orders/src/main/java/com/example/orders/OrderProperties.java",
            "configuration_properties_signal": "direct_configuration_properties_type",
            "binding_status": "not_analyzed",
            "evidence_ids": [
              "ev:services/orders/src/main/java/com/example/orders/OrderProperties.java:8-8:com.example.orders.OrderProperties:@ConfigurationProperties"
            ]
          }
        ]
      },
      "bean_methods": {
        "analysis_status": "analyzed",
        "items": [
          {
            "id": "spring_bean_method:module:services/orders:com.example.orders.OrderConfiguration#orderClock:decl:000001",
            "module_id": "module:services/orders",
            "surface_category": "spring_bean_method",
            "support_type": "extracted",
            "class_name": "com.example.orders.OrderConfiguration",
            "method_name": "orderClock",
            "source_path": "services/orders/src/main/java/com/example/orders/OrderConfiguration.java",
            "bean_signal": "direct_bean_method",
            "bean_name_status": "not_analyzed",
            "evidence_ids": [
              "ev:services/orders/src/main/java/com/example/orders/OrderConfiguration.java:10-10:com.example.orders.OrderConfiguration#orderClock:@Bean"
            ]
          }
        ]
      }
    },
    "behavior": {
      "transaction_boundaries": {
        "analysis_status": "analyzed",
        "items": []
      },
      "scheduled_methods": {
        "analysis_status": "analyzed",
        "items": []
      },
      "event_listeners": {
        "analysis_status": "analyzed",
        "items": []
      }
    },
    "messaging": {
      "listener_signals": {
        "analysis_status": "analyzed",
        "items": []
      }
    },
    "security": {
      "configuration_warnings": {
        "analysis_status": "analyzed",
        "warning_ids": []
      }
    }
  }
}
```

Spring application surface taxonomy rules:

- `spring_repository_stereotype` means a direct source-visible `@Repository` annotation
  on a Java class or interface. It is an extracted fact and must not imply Spring Data
  repository behavior, query semantics, runtime bean registration, or entity ownership.
- `spring_data_repository_interface_signal` means a source-visible Java interface
  appears to extend a supported Spring Data repository base type. The bounded Spring
  Data signal implementation supports `org.springframework.data.repository.Repository`,
  `org.springframework.data.repository.CrudRepository`,
  `org.springframework.data.repository.PagingAndSortingRepository`,
  `org.springframework.data.jpa.repository.JpaRepository`, and
  `org.springframework.data.mongodb.repository.MongoRepository` when visible through a
  fully qualified name or explicit single-type import. It is an inferred signal and
  must not by itself imply runtime repository registration, resolved generic entity
  type, query method behavior, database access, or repository-to-entity relation. The
  v0.6 repository/entity relation extension is a separate inferred relation object with
  its own conservative status and evidence rules.
- `spring_configuration_class` means a direct source-visible `@Configuration`
  annotation. It must not imply conditional activation, bean graph, component scan
  result, or auto-configuration behavior.
- `spring_configuration_properties_type` means a direct source-visible
  `@ConfigurationProperties` annotation. Optional bounded `prefix` or `value` fields, if
  implemented, are annotation literals only and must not imply runtime binding,
  environment values, active profiles, or config file values.
- `spring_bean_method` means a direct source-visible `@Bean` method. It must not imply
  an instantiated runtime bean, effective bean name, scope, lifecycle, proxy behavior, or
  dependency graph.
- `spring_transaction_boundary` means a direct source-visible `@Transactional`
  annotation on a class or method. It must not imply runtime proxy application,
  effective transaction manager, propagation semantics, isolation semantics, rollback
  behavior, or call graph effects.
- `spring_scheduled_method` means a direct source-visible `@Scheduled` method. It must
  not imply scheduler enablement, runtime registration, cron correctness, execution
  frequency, lock behavior, or cluster behavior.
- `spring_event_listener` means a direct source-visible Spring event listener annotation
  such as `@EventListener`. It must not imply event publication paths, listener ordering,
  transaction phase behavior, runtime event delivery, or call graph behavior.
- `messaging_listener_signal` means a direct source-visible messaging listener
  annotation for common Kafka and Rabbit listener annotations. It must
  not imply runtime broker topology, queue/topic existence, exchange bindings, consumer
  group membership, delivery semantics, or deployment configuration.
- `spring_security_configuration_warning` means a bounded source-visible Spring Security
  configuration signal. It lives in `warnings.items` and is referenced by
  `spring_application_surface.security.configuration_warnings.warning_ids`; it must not
  imply endpoint protection state, authentication provider behavior, authorization
  rules, filter-chain ordering, vulnerability, or security correctness.

Spring application surface field rules:

- `spring_application_surface.analysis_status` is `"analyzed"` when any v0.5 Spring
  surface analyzer runs. It is `"not_detected"` only when no supported production source
  input is available.
- Subsection `analysis_status` values are `"analyzed"` when their analyzer runs, even
  when their item or warning-reference collections are empty. They are `"not_detected"`
  only when no supported input exists for that subsection.
- In the v0.5 implementation state, repository, configuration, behavior,
  messaging, and security configuration warning subsections emit `"analyzed"` when
  supported production source roots exist and their analyzers run.
- `surface_category` uses one of the v0.5 taxonomy values. Warning-reference
  containers do not duplicate warning payloads or use `surface_category`.
- Item `support_type` is `"extracted"` for direct source-visible facts and `"inferred"`
  for source-visible signals derived from structure or conventions.
- All module-owned Spring surface items include direct `module_id` fields.
- Current repository item IDs are stable:
  `spring_repository_stereotype:<module_id>:<class_name>` for direct `@Repository`
  observations and
  `spring_data_repository_interface_signal:<module_id>:<class_name>` for inferred
  Spring Data repository interface extension signals.
- Current configuration item IDs are stable:
  `spring_configuration_class:<module_id>:<class_name>` for direct `@Configuration`
  observations,
  `spring_configuration_properties_type:<module_id>:<class_name>` for direct
  `@ConfigurationProperties` observations, and
  `spring_bean_method:<module_id>:<class_name>#<method_name>:decl:<ordinal>` for direct
  `@Bean` method observations. The zero-padded declaration ordinal disambiguates
  source-visible `@Bean` method facts and is not a bean name, dependency relation, or
  runtime identity claim.
- Current behavior and messaging item IDs are stable:
  `spring_transaction_boundary:<module_id>:<class_name>:type` for direct type-level
  `@Transactional` observations,
  `spring_transaction_boundary:<module_id>:<class_name>#<method_name>:decl:<ordinal>`
  for direct method-level `@Transactional` observations,
  `spring_scheduled_method:<module_id>:<class_name>#<method_name>:decl:<ordinal>` for
  direct `@Scheduled` method observations,
  `spring_event_listener:<module_id>:<class_name>#<method_name>:decl:<ordinal>` for
  direct `@EventListener` method observations, and
  `messaging_listener_signal:<module_id>:<class_name>[:#<method_name>]:annotation:<annotation_name>:decl:<ordinal>`
  for direct Kafka/Rabbit listener annotation observations. The zero-padded declaration
  ordinal disambiguates source-visible declarations and is not a runtime listener,
  scheduler, transaction, destination, or broker identity claim.
- Source paths are normalized repository-relative paths and must not be absolute, start
  with `./`, or escape the scanned repository root.
- `extends_types` preserves bounded source-visible Spring Data base type observations
  only. It must not imply classpath resolution, entity relation, or runtime repository
  creation.
- `entity_relation_status: "not_analyzed"` is required for v0.5 Spring Data repository
  interface signals where a reader might otherwise assume repository-to-entity mapping.
  v0.6 replaces this compatibility value with the bounded relation status values defined
  in the v0.6 JPA/domain contract when the repository/entity relation analyzer runs.
- `configuration_signal` is `"direct_configuration_class"` for current direct
  `@Configuration` facts.
- `configuration_properties_signal` is `"direct_configuration_properties_type"` for
  current direct `@ConfigurationProperties` facts.
- `binding_status: "not_analyzed"` is required for current configuration-properties
  facts. The current implementation does not emit `prefix` or `value` fields and does
  not extract configuration file values, active profiles, environment values, validation
  state, or runtime binding success.
- `bean_signal` is `"direct_bean_method"` for current direct `@Bean` method facts.
- `bean_name_status` is `"not_analyzed"` for current bean method facts. Future bounded
  source-visible `@Bean` name extraction would require a separate design; emitted names
  would remain annotation literals, not runtime bean names.
- Transaction facts use `transaction_signal` values `"direct_transactional_type"` and
  `"direct_transactional_method"`, include `target_kind` (`"type"` or `"method"`),
  include `annotation_symbol: "@Transactional"`, and never emit propagation, isolation,
  rollback, transaction-manager, proxy, or call-graph fields.
- Scheduled method facts use `scheduled_signal: "direct_scheduled_method"`, include
  `target_kind: "method"`, include `annotation_symbol: "@Scheduled"`, and never emit
  cron, fixed-rate, fixed-delay, scheduler, lock, registration, frequency, cluster, or
  runtime execution fields.
- Event listener facts use `event_listener_signal: "direct_event_listener_method"`,
  include `target_kind: "method"`, include `annotation_symbol: "@EventListener"`, and
  never emit event publication paths, listener ordering, transaction phase, delivery, or
  call-graph fields.
- Messaging listener facts support direct source-visible Spring Kafka
  `@KafkaListener`/`@KafkaListeners` and Spring AMQP Rabbit
  `@RabbitListener`/`@RabbitListeners` annotations. They include `target_kind`,
  `annotation_symbol`, `listener_framework` (`"kafka"` or `"rabbit"`), and
  `listener_signal` (`"direct_kafka_listener_annotation"` or
  `"direct_rabbit_listener_annotation"`). They do not emit topic, queue, exchange,
  routing-key, group-id, broker, binding, consumer-group, delivery, or deployment fields.

Current security warning rules:

- Spring Security configuration warnings use `category: "spring_security"`.
- Warning `signal` values include `"security_configuration_annotation"` and
  `"security_filter_chain_bean"` when directly source-visible.
- Current supported security configuration annotations are
  `org.springframework.security.config.annotation.web.configuration.EnableWebSecurity`,
  `org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity`,
  `org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity`,
  `org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity`,
  and
  `org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity`
  when visible through a fully qualified name or explicit single-type import.
- Current `SecurityFilterChain` `@Bean` warnings require both a trusted
  `org.springframework.context.annotation.Bean` annotation and a return type visible as
  `org.springframework.security.web.SecurityFilterChain` through a fully qualified name
  or explicit single-type import.
- Security warning IDs are stable. Scan-root warnings use
  `warning:spring_security:<signal>:<target>`; child-module warnings use
  `warning:spring_security:<signal>:module:<module_path>:<target>`. Current targets are
  `<class_name>:annotation:<annotation_discriminator>:decl:<ordinal>` for supported
  security annotations and `<class_name>#<method_name>:decl:<ordinal>` for
  `SecurityFilterChain` `@Bean` methods. The zero-padded ordinal disambiguates
  source-visible matching declarations and is not a runtime security-chain identity.
- Warning messages must use detected-signal wording. They must not claim endpoint
  protection, authentication behavior, authorization behavior, runtime filter order,
  vulnerability, or policy correctness.

Deterministic sorting rules:

- Spring surface item lists sort by module order, source path, class name, method name
  when present, `surface_category`, and `id`.
- Security warning ID lists follow the existing warning sort order for their referenced
  warning items.

### v0.6 JPA And Domain Contract

This section defines the v0.6 JPA/domain output contract. The current v0.6
implementation emits `schema_version: "0.6"`, implements the bounded entity field
annotation slice for direct field-level `@Column`, `@Enumerated`, `@GeneratedValue`,
and `@Version`, and adds bounded embedded and identifier-model signals for direct
`@Embeddable`, direct field-level `@Embedded` and `@EmbeddedId`, and direct class-level
`@IdClass`. It also deepens relationship metadata for direct field-level relationship
annotations, direct string-literal `mappedBy`, bounded direct `@JoinColumn` and
`@JoinTable` metadata, selected directly visible relationship attributes, and
conservative repository/entity inferred relations for supported source-visible Spring
Data repository generic types, safe JPA-only wildcard import support for the existing
supported JPA annotation set, and quieter no-domain guide rendering. Future goals may
fill the planned table metadata and source-visible relationship target links described
below.

The v0.6 contract uses:

- `schema_version: "0.6"` for an atomic public output state that preserves the v0.5
  module-aware build/config, API surface, and Spring application surface contracts while
  deepening the existing JPA/domain model.
- The same four output files under `.project-memory/`.
- The existing top-level `entities` object as the owner of source-visible JPA entity,
  embeddable, field, identifier, and relationship facts. v0.6 does not add a database
  schema file or a runtime ORM model file.
- Existing evidence fields and evidence types. Direct JPA annotations reuse
  `annotation` evidence; source-visible Java type declarations and generic type
  observations reuse `code_symbol` evidence.
- Repository/entity links remain attached to v0.5
  `spring_application_surface.repositories.items[]` because they refine inferred Spring
  Data repository interface signals. They are inferred relations, not extracted entity
  facts.

Current v0.6 implementation state:

- `schema_version` is `"0.6"`.
- `entities.items[]` continues to emit existing entity, table compatibility,
  identifier, and relationship fields.
- Each entity object emits `fields`, sorted deterministically, as a possibly empty
  array.
- `fields[]` currently contains direct field-level `@Column`, `@Enumerated`,
  `@GeneratedValue`, `@Version`, `@Embedded`, and `@EmbeddedId` metadata declared on the
  entity class. Getter or property-access annotations are not emitted in this slice.
- `identifier_fields[]` now emits `identifier_kind: "simple_id"` for supported simple
  `@Id` facts and a nullable `generated_value` object when a direct field-level
  `@GeneratedValue` annotation is present on that identifier field.
- `identifier_fields[]` also emits `identifier_kind: "embedded_id"` for direct
  field-level `@EmbeddedId` identifier signals. This is a partial source-visible
  composite identifier signal and does not reconstruct composite-key semantics.
- `entity.id_class` is `null` unless a direct class-level `@IdClass` annotation is
  present. When present, it records a nullable `type_name`, `field_matching_status:
  "not_analyzed"`, `semantic_reconstruction_status: "not_analyzed"`, and evidence IDs.
- `field.embedded` is `null` unless a direct field-level `@Embedded` or `@EmbeddedId`
  annotation is present. When present, it records the annotation symbol, declared Java
  type, target resolution, nullable target embeddable identity fields, nullable
  inference support fields, uncertainty, and evidence IDs.
- `entities.embeddables` is emitted with its own `analysis_status` and contains direct
  source-visible `@Embeddable` class facts. Embeddables are not emitted as entity/table
  facts.
- Missing annotation attributes remain `null`; generated output must not fill JPA
  runtime defaults.
- `relationships[]` now emits direct field-level relationship facts with `cardinality`,
  a nested `target` object, direct source-visible relationship attributes,
  `join_columns`, nullable `join_table`, conservative `ownership_signal`, and evidence
  IDs for the direct relationship and join metadata annotations.
- Relationship `target` currently preserves the declared type only with
  `target_resolution: "declared_type_only"` and `uncertainty:
  "target_type_not_resolved"`. Source-visible entity target matching is planned later
  and is not emitted by the current v0.6 implementation.
- `relationship.mapped_by` records only direct string-literal `mappedBy` values.
  Unsupported expressions are not converted to defaults.
- Direct relationship `optional`, `fetch`, `cascade`, and `orphan_removal` values are
  emitted only when directly source-visible and supported. Missing or unsupported
  attributes remain `null` or empty arrays.
- `relationship.join_columns[]` records bounded direct field-level `@JoinColumn`
  metadata. `relationship.join_table` records bounded direct field-level `@JoinTable`
  metadata and supported nested `@JoinColumn` values under `join_columns` and
  `inverse_join_columns`.
- Relationship metadata is source-visible orientation only. It must not claim ORM
  ownership correctness, foreign keys, join tables, database constraints, fetch
  behavior, cascade behavior, provider defaults, or runtime ORM behavior.
- `spring_application_surface.repositories.items[]` Spring Data interface signals now
  emit `entity_relation_status` and nullable `entity_relation`. A relation object is
  emitted only when a supported source-visible repository entity generic type can be
  matched to exactly one emitted entity fact. Missing, ambiguous, raw, wildcard, nested,
  or otherwise unsupported generic shapes use explicit status values and keep
  `entity_relation: null`.
- `table_metadata` and relationship target links are planned for future goals and are
  not emitted by the current v0.6 implementation.

Full-track `project-map.json` excerpt. Unchanged v0.5 fields are omitted from
some objects for focus, but remain required by their existing contracts when those
objects are emitted. Source-visible relationship target links shown below remain planned
later until their implementation goals land:

```json
{
  "schema_version": "0.6",
  "entities": {
    "analysis_status": "analyzed",
    "items": [
      {
        "id": "entity:module:services/orders:com.example.orders.Order",
        "module_id": "module:services/orders",
        "class_name": "com.example.orders.Order",
        "source_path": "services/orders/src/main/java/com/example/orders/Order.java",
        "table_name": "orders",
        "table_metadata": {
          "name": "orders",
          "schema": "sales",
          "catalog": null,
          "evidence_ids": [
            "ev:services/orders/src/main/java/com/example/orders/Order.java:12-12:com.example.orders.Order:@Table"
          ]
        },
        "id_class": null,
        "fields": [
          {
            "field_name": "status",
            "java_type": "OrderStatus",
            "declaring_class": "com.example.orders.Order",
            "source_kind": "declared",
            "persistence_role": "basic",
            "annotations": ["@Column", "@Enumerated"],
            "column": {
              "name": "status",
              "nullable": false,
              "unique": null,
              "length": null,
              "precision": null,
              "scale": null,
              "insertable": null,
              "updatable": null,
              "evidence_ids": [
                "ev:services/orders/src/main/java/com/example/orders/Order.java:20-20:com.example.orders.Order:@Column:field:status"
              ]
            },
            "enumerated": {
              "value": "EnumType.STRING",
              "evidence_ids": [
                "ev:services/orders/src/main/java/com/example/orders/Order.java:21-21:com.example.orders.Order:@Enumerated:field:status"
              ]
            },
            "generated_value": null,
            "version": null,
            "embedded": null,
            "evidence_ids": [
              "ev:services/orders/src/main/java/com/example/orders/Order.java:20-20:com.example.orders.Order:@Column:field:status",
              "ev:services/orders/src/main/java/com/example/orders/Order.java:21-21:com.example.orders.Order:@Enumerated:field:status"
            ]
          }
        ],
        "identifier_fields": [
          {
            "field_name": "id",
            "java_type": "Long",
            "declaring_class": "com.example.orders.Order",
            "source_kind": "declared",
            "identifier_kind": "simple_id",
            "generated_value": {
              "strategy": "GenerationType.IDENTITY",
              "generator": null,
              "evidence_ids": [
                "ev:services/orders/src/main/java/com/example/orders/Order.java:16-16:com.example.orders.Order:@GeneratedValue:field:id"
              ]
            },
            "evidence_ids": [
              "ev:services/orders/src/main/java/com/example/orders/Order.java:15-15:com.example.orders.Order:@Id:field:id",
              "ev:services/orders/src/main/java/com/example/orders/Order.java:16-16:com.example.orders.Order:@GeneratedValue:field:id"
            ]
          }
        ],
        "relationships": [
          {
            "field_name": "customer",
            "annotation": "@ManyToOne",
            "cardinality": "many_to_one",
            "java_type": "Customer",
            "target": {
              "declared_type": "Customer",
              "target_resolution": "declared_type_only",
              "target_entity_id": null,
              "target_module_id": null,
              "target_class_name": null,
              "support_type": null,
              "confidence": null,
              "uncertainty": "target_type_not_resolved",
              "evidence_ids": []
            },
            "mapped_by": null,
            "ownership_signal": "mapped_by_absent",
            "optional": false,
            "fetch": "FetchType.LAZY",
            "cascade": [],
            "orphan_removal": null,
            "join_columns": [
              {
                "name": "customer_id",
                "referenced_column_name": null,
                "nullable": false,
                "unique": null,
                "insertable": null,
                "updatable": null,
                "evidence_ids": [
                  "ev:services/orders/src/main/java/com/example/orders/Order.java:30-30:com.example.orders.Order:@JoinColumn:field:customer"
                ]
              }
            ],
            "join_table": null,
            "evidence_ids": [
              "ev:services/orders/src/main/java/com/example/orders/Order.java:29-29:com.example.orders.Order:@ManyToOne:field:customer",
              "ev:services/orders/src/main/java/com/example/orders/Order.java:30-30:com.example.orders.Order:@JoinColumn:field:customer"
            ]
          }
        ],
        "evidence_ids": [
          "ev:services/orders/src/main/java/com/example/orders/Order.java:11-11:com.example.orders.Order:@Entity",
          "ev:services/orders/src/main/java/com/example/orders/Order.java:12-12:com.example.orders.Order:@Table"
        ]
      }
    ],
    "embeddables": {
      "analysis_status": "analyzed",
      "items": [
        {
          "id": "embeddable:module:services/orders:com.example.orders.OrderId",
          "module_id": "module:services/orders",
          "class_name": "com.example.orders.OrderId",
          "source_path": "services/orders/src/main/java/com/example/orders/OrderId.java",
          "fields": [],
          "evidence_ids": [
            "ev:services/orders/src/main/java/com/example/orders/OrderId.java:8-8:com.example.orders.OrderId:@Embeddable"
          ]
        }
      ]
    }
  },
  "spring_application_surface": {
    "repositories": {
      "items": [
        {
          "id": "spring_data_repository_interface_signal:module:services/orders:com.example.orders.OrderRepository",
          "entity_relation_status": "inferred",
          "entity_relation": {
            "support_type": "inferred",
            "relation_type": "repository_entity_generic",
            "target_entity_id": "entity:module:services/orders:com.example.orders.Order",
            "target_module_id": "module:services/orders",
            "target_class_name": "com.example.orders.Order",
            "generic_type": "com.example.orders.Order",
            "confidence": "medium",
            "uncertainty": null,
            "evidence_ids": [
              "ev:services/orders/src/main/java/com/example/orders/OrderRepository.java:8-8:com.example.orders.OrderRepository:extends:org.springframework.data.jpa.repository.JpaRepository",
              "ev:services/orders/src/main/java/com/example/orders/Order.java:11-11:com.example.orders.Order:@Entity"
            ]
          }
        }
      ]
    }
  }
}
```

v0.6 entity and embeddable rules:

- `entities.analysis_status` remains `"analyzed"` when supported production source roots
  exist and the JPA/domain analyzer runs. `entities.embeddables.analysis_status` follows
  the same rule for `@Embeddable` detection.
- `entities.items[]` continues to contain direct source-visible `@Entity` facts.
  `entities.embeddables.items[]` contains direct source-visible `@Embeddable` facts and
  must not be described as entity/table facts.
- Existing entity IDs remain stable for root-module compatibility. Child module entity
  IDs include `module_id` as in the existing v0.2 module-aware fact-ID rule.
- `entity.table_name` remains the compatibility string for direct
  `@Table(name = "...")`. `entity.table_metadata` is a planned structured
  source-visible `@Table` view with optional `name`, `schema`, and `catalog` values when
  directly extractable. It must not imply that a table exists in any database.
- `entity.fields[]` contains source-visible field metadata for supported direct JPA
  annotations. It is not a complete persistent-property inventory. Fields with no
  supported JPA annotation do not have to be emitted.
- Current field metadata supports direct field-level `@Column`, `@Enumerated`,
  `@GeneratedValue`, `@Version`, `@Embedded`, and `@EmbeddedId` on entity classes.
  Planned later field metadata may add relationship annotations, `@JoinColumn`, and
  `@JoinTable`. Getter/property-access support is a separate bounded implementation
  choice; if added, it must preserve a distinct member kind and evidence for the
  annotated method without pretending it was a field declaration.
- `field.persistence_role` is a source-visible classification such as `"basic"`,
  `"simple_id"`, `"embedded"`, `"embedded_id"`, `"version"`, or `"relationship"`. It is
  not a runtime access strategy or schema role claim.
- `field.annotations` lists only supported direct JPA annotation symbols detected on
  that field. It may include annotations backed by the explicit supported JPA wildcard
  import rule above, but must not include classpath-only, unsupported wildcard-only,
  unresolved, generated, same-package/local fake, or source-declared fake annotations.
- `field.column` records only source-visible direct `@Column` literal attributes chosen
  by the implementation, such as `name`, `nullable`, `unique`, `length`, `precision`,
  `scale`, `insertable`, and `updatable`. Missing attributes remain `null`; the analyzer
  must not fill JPA defaults.
- `field.enumerated` records only direct `@Enumerated` source-visible enum/literal
  values, such as `EnumType.STRING`; it must not infer enum storage when the annotation
  is absent or unsupported.
- `field.generated_value` records only direct `@GeneratedValue` source-visible
  `strategy` and `generator` literals. It must not claim generated identifier behavior,
  sequence/table existence, database identity behavior, or provider defaults.
- `field.version` records direct `@Version` presence and evidence only. It must not
  claim optimistic-locking correctness or runtime version behavior.
- `field.embedded` records direct `@Embedded` or `@EmbeddedId` presence and the declared
  Java type. `field.embedded.annotation` is either `"@Embedded"` or `"@EmbeddedId"`.
  `field.embedded.target_resolution` is `"source_visible_embeddable"` only when the
  declared type can be matched deterministically to a unique emitted embeddable fact in
  the same supported module; otherwise it is `"declared_type_only"` with
  `uncertainty: "embeddable_target_not_resolved"`. `target_embeddable_id`,
  `target_module_id`, and `target_class_name` are nullable and are populated only for
  the unique source-visible embeddable match. `support_type: "inferred"` and
  `confidence: "medium"` are used only for that source-visible match. This target link
  is a conservative source-visible inference, not runtime ORM resolution.
- `entity.identifier_fields[]` keeps existing simple `@Id` support and may add
  `identifier_kind` values such as `"simple_id"`, `"embedded_id"`, and
  `"id_class_field"`. Mapped-superclass identifiers keep `source_kind:
  "mapped_superclass"` and the existing conservative hierarchy boundary.
- `entity.id_class` records direct class-level `@IdClass` source-visible class literals
  when present. `type_name` is nullable when the annotation value is not a direct class
  literal. `field_matching_status` and `semantic_reconstruction_status` are
  `"not_analyzed"`. It is a composite-id signal only. It must not reconstruct field
  matching, equality semantics, serializability, generated keys, provider behavior, or
  database primary keys.
- `entities.embeddables.items[].fields[]` uses the same supported field metadata shape
  as entity fields where applicable. Embeddable field facts must not imply that an
  embeddable is used by any entity unless a separate `@Embedded` or `@EmbeddedId` fact
  supports that relation.

Current v0.6 relationship rules and planned target-link extension:

- `entity.relationships[]` remains the relationship fact list for direct field-level
  `@ManyToOne`, `@OneToMany`, `@OneToOne`, and `@ManyToMany` annotations.
- `relationship.cardinality` is derived only from the direct relationship annotation:
  `"many_to_one"`, `"one_to_many"`, `"one_to_one"`, or `"many_to_many"`.
- `relationship.java_type` preserves the declared Java field type string. It is not a
  database type, table name, or guaranteed entity target.
- `relationship.target.target_resolution` is currently `"declared_type_only"` because
  the current v0.6 implementation preserves only the declared field type. Future
  relationship target-link work may use `"source_visible_entity"` only when a unique
  emitted entity fact is deterministically matched, and `"ambiguous"` when
  source-visible candidates cannot be reduced to one target. Target links are inferred
  relation support, not extracted annotation facts.
- `relationship.target.support_type` is currently `null`. Future target-link work may
  use `"inferred"` only when `target_resolution: "source_visible_entity"`.
- `relationship.target.uncertainty` must preserve uncertainty values such as
  `"target_type_not_resolved"`, `"ambiguous_target_type"`, or
  `"unsupported_collection_type"` when a target link cannot be made conservatively.
- `relationship.mapped_by` records only directly visible `mappedBy` string literals.
  Unsupported expressions or absent attributes must not be converted to runtime defaults.
- `relationship.ownership_signal` is a source-visible orientation signal, not a runtime
  ORM ownership guarantee. Allowed values include `"mapped_by_present"`,
  `"mapped_by_absent"`, `"join_metadata_present"`, and `"not_analyzed"`.
- `relationship.optional`, `fetch`, `cascade`, and `orphan_removal` record only directly
  visible annotation attributes chosen by the implementation. Missing values remain
  `null` or empty arrays and must not be filled from JPA defaults.
- `relationship.join_columns[]` records bounded source-visible `@JoinColumn` metadata
  such as `name`, `referenced_column_name`, `nullable`, `unique`, `insertable`, and
  `updatable`. It must not reconstruct foreign keys, indexes, constraints, or database
  columns.
- `relationship.join_table` records bounded source-visible `@JoinTable` metadata such
  as `name`, `schema`, `catalog`, `join_columns`, and `inverse_join_columns` when
  directly extractable. It must not reconstruct join tables or migration state.

Current v0.6 repository/entity relation rules:

- v0.5 `spring_data_repository_interface_signal` items continue to be inferred Spring
  Data interface signals. The v0.6 repository/entity relation analyzer replaces
  `entity_relation_status: "not_analyzed"` with a conservative relation status when it
  runs for those items.
- `entity_relation_status` values are:
  - `"inferred"`: a supported source-visible Spring Data repository generic type can be
    matched to exactly one emitted entity fact.
  - `"not_detected"`: the analyzer ran but did not find a supported source-visible
    entity generic or matching entity fact.
  - `"ambiguous"`: the analyzer found multiple possible source-visible entity matches.
  - `"unsupported"`: the generic shape is source-visible but outside the supported
    bounded parser, such as nested, wildcard, raw, or unresolved generic forms.
  - `"not_analyzed"`: compatibility value used only when this relation analyzer did not
    run for the item.
- `entity_relation` is non-null only when `entity_relation_status` is `"inferred"`.
  It must include `support_type: "inferred"`, `relation_type:
  "repository_entity_generic"`, target entity identity, the source-visible generic type
  string, confidence, uncertainty, and evidence IDs for both repository-side generic
  evidence and target entity evidence.
  For every non-inferred relation status, `entity_relation` is emitted as `null`.
- All unchanged v0.5 repository item fields remain required when v0.6 adds
  `entity_relation_status` and `entity_relation` relation fields.
- Inferred repository/entity relations must not be emitted for direct `@Repository`
  stereotype facts unless they also have a separate supported Spring Data repository
  interface signal.
- Repository/entity relations must not use runtime Spring Data registration, query
  method parsing, JPQL semantics, database access, dependency graphs, classpath solving,
  Hibernate metadata, or migration files as evidence.

Planned v0.6 deterministic sorting rules:

- Entity fields sort by module order, declaring class, source kind, field name, Java
  type, persistence role, and ID/evidence discriminator where needed.
- Identifier fields keep the existing deterministic order and then sort by
  `identifier_kind`.
- Relationships sort by module order, declaring class, field name, cardinality,
  annotation, Java type, and ID/evidence discriminator where needed.
- Embeddables sort by module order, class name, source path, and ID.
- Repository/entity relation status does not change repository item sort order.

### v0.7 Tests Inventory Refinement Contract

This section defines the current v0.7 tests inventory refinement, Spring test
slice/mock signal, tested-subject relation/status, and quality planning-hint output
contract. The current v0.7 implementation preserves the v0.6 module-aware build/config,
API surface, Spring application surface, and JPA/domain contracts while deepening the
top-level `tests` inventory and adding a top-level `quality` object. It does not emit
coverage, test execution, assertion, CI, runtime, correctness, vulnerability,
production-impact, business-priority, or complete subject-mapping claims.

The v0.7 tests inventory refinement contract uses:

- `schema_version: "0.7"` for an atomic public output state that preserves the v0.6
  contracts and adds bounded source-visible tests inventory refinement plus conservative
  quality/change-risk planning hints.
- The same four output files under `.project-memory/`.
- The existing top-level `tests` object as the owner of source-visible test class,
  method, direct framework signal, Spring test slice, mock annotation signal, and
  tested-subject relation/status facts.
- A top-level `quality` object as the owner of conservative test-gap and change-risk
  planning hints derived from existing deterministic facts and inferred tested-subject
  relations.
- Existing evidence fields and evidence types. Test class facts continue to use
  `test_file` evidence. Test method, framework signal, Spring test slice, and mock
  annotation signal observations reuse `annotation` and `code_symbol` evidence. Quality
  signals reuse evidence IDs from the underlying subject facts; they do not introduce a
  new evidence type.

Current `project-map.json` excerpt. Unchanged v0.6 fields are omitted for focus:

```json
{
  "schema_version": "0.7",
  "tests": {
    "analysis_status": "analyzed",
    "items": [
      {
        "id": "test:module:services/orders:com.example.orders.OrderControllerTest",
        "module_id": "module:services/orders",
        "class_name": "com.example.orders.OrderControllerTest",
        "source_path": "services/orders/src/test/java/com/example/orders/OrderControllerTest.java",
        "framework_signals": [
          {
            "name": "JUnit Jupiter",
            "signal_kind": "framework",
            "evidence_ids": [
              "ev:services/orders/src/test/java/com/example/orders/OrderControllerTest.java:3-3:com.example.orders.OrderControllerTest:import:org.junit.jupiter.api.Test"
            ]
          }
        ],
        "spring_test_slices": [
          {
            "annotation": "@WebMvcTest",
            "slice_kind": "web_mvc_test",
            "signal_kind": "spring_test_slice",
            "evidence_ids": [
              "ev:services/orders/src/test/java/com/example/orders/OrderControllerTest.java:8-8:com.example.orders.OrderControllerTest:@WebMvcTest"
            ]
          }
        ],
        "mock_signals": [
          {
            "annotation": "@MockBean",
            "mock_signal": "spring_boot_mockbean_annotation",
            "signal_kind": "mock_annotation",
            "target_kind": "field",
            "target_name": "orderService",
            "evidence_ids": [
              "ev:services/orders/src/test/java/com/example/orders/OrderControllerTest.java:12-12:com.example.orders.OrderControllerTest:field:orderService:@MockBean"
            ]
          }
        ],
        "methods": [
          {
            "method_name": "returnsOrder",
            "test_annotation": "@Test",
            "method_kind": "test",
            "display_name": null,
            "evidence_ids": [
              "ev:services/orders/src/test/java/com/example/orders/OrderControllerTest.java:14-14:com.example.orders.OrderControllerTest#returnsOrder:@Test"
            ]
          }
        ],
        "tested_subjects": [
          {
            "relation_status": "inferred",
            "relation_type": "spring_test_slice_class_literal",
            "class_name": "com.example.orders.OrderController",
            "target_module_id": "module:services/orders",
            "candidate_reference": null,
            "support_type": "inferred",
            "confidence": "medium",
            "uncertainty": null,
            "evidence_ids": [
              "ev:services/orders/src/test/java/com/example/orders/OrderControllerTest.java:9-9:com.example.orders.OrderControllerTest:test_file",
              "ev:services/orders/src/main/java/com/example/orders/OrderController.java:18-18:com.example.orders.OrderController:@RestController"
            ]
          }
        ],
        "evidence_ids": [
          "ev:services/orders/src/test/java/com/example/orders/OrderControllerTest.java:9-9:com.example.orders.OrderControllerTest:test_file"
        ]
      }
    ]
  },
  "quality": {
    "analysis_status": "analyzed",
    "test_gap_signals": {
      "analysis_status": "analyzed",
      "items": [
        {
          "id": "quality:test_gap:repository_without_obvious_test:spring_data_repository_interface_signal:module:services/orders:com.example.orders.OrderRepository",
          "module_id": "module:services/orders",
          "signal": "repository_without_obvious_test",
          "status": "no_obvious_test",
          "subject_kind": "spring_repository",
          "subject_id": "spring_data_repository_interface_signal:module:services/orders:com.example.orders.OrderRepository",
          "subject_name": "com.example.orders.OrderRepository",
          "subject_class_name": "com.example.orders.OrderRepository",
          "subject_member_name": null,
          "inference_basis": "no_inferred_tested_subject_relation_for_subject_class",
          "confidence": "low",
          "uncertainty": "bounded_test_inventory_supported_relations_only",
          "related_test_ids": [],
          "evidence_ids": [
            "ev:services/orders/src/main/java/com/example/orders/OrderRepository.java:6-6:com.example.orders.OrderRepository:extends:org.springframework.data.jpa.repository.JpaRepository"
          ]
        }
      ]
    },
    "change_risk_signals": {
      "analysis_status": "analyzed",
      "items": [
        {
          "id": "quality:change_risk:spring_service_change_surface:component:module:services/orders:com.example.orders.OrderService",
          "module_id": "module:services/orders",
          "signal": "spring_service_change_surface",
          "status": "planning_hint",
          "subject_kind": "spring_service",
          "subject_id": "component:module:services/orders:com.example.orders.OrderService",
          "subject_name": "com.example.orders.OrderService",
          "subject_class_name": "com.example.orders.OrderService",
          "subject_member_name": null,
          "risk_basis": "source_visible_service_stereotype",
          "confidence": "low",
          "uncertainty": "source_visible_change_surface_only",
          "evidence_ids": [
            "ev:services/orders/src/main/java/com/example/orders/OrderService.java:8-8:com.example.orders.OrderService:@Service"
          ]
        }
      ]
    }
  }
}
```

Current v0.7 output includes a top-level `quality` object.

Current v0.7 test inventory rules:

- `tests.analysis_status` remains `"analyzed"` when supported standard Maven test roots
  exist and the tests inventory analyzer runs. It remains `"not_detected"` when no
  supported test root is present.
- `tests.items[]` continues to contain Java class declarations under supported standard
  Maven test roots that look like test classes. Interfaces remain out of scope. Helper,
  support, and configuration declarations without clear test naming and without direct
  test-class marker annotations remain omitted.
- `test.id` is a stable fact ID. Root-module compatibility may preserve existing IDs
  where needed; child-module IDs include `module_id` following the existing v0.2
  module-aware fact-ID rule.
- `test.methods[]` contains directly declared test methods only when a method has a
  supported directly visible JUnit test method annotation. Current supported method
  annotations are JUnit Jupiter `@Test`, `@ParameterizedTest`, `@RepeatedTest`,
  `@TestFactory`, and `@TestTemplate`, plus JUnit 4 `@Test`, when the origin is trusted
  from a fully qualified annotation name or explicit single-type import.
- Lifecycle methods such as `@BeforeEach`, `@AfterEach`, `@BeforeAll`, `@AfterAll`,
  `@Before`, and `@After` may be recorded later as setup/teardown signals only after a
  separate bounded contract update. They must not be counted as test methods.
- `test.methods[].method_kind` is `"test"` for supported test method annotations in the
  current slice. It must not encode assertion behavior or execution status.
- `test.methods[].display_name` is currently `null`. Direct `@DisplayName` extraction is
  not implemented in this slice.
- `test.framework_signals[]` remains a direct source-visible signal list. Current
  framework families include `"JUnit Jupiter"`, `"JUnit 4"`, and `"Spring Test"` where
  the source-visible origin is trusted from a fully qualified annotation name or
  explicit single-type import, and where the same fully qualified name is not declared by
  scanned source. Framework signals do not prove runtime engine execution, Spring
  context behavior, CI behavior, or assertion behavior.
- `test.framework_signals[].signal_kind` is `"framework"` for the current v0.7 slice.
  It is a source-visible classification only and must not encode runtime engine
  execution, CI behavior, or assertion behavior.
- `test.spring_test_slices[]` contains direct class-level supported Spring test slice or
  context annotations on emitted test classes. Current supported annotations are
  `@SpringBootTest`, `@WebMvcTest`, `@DataJpaTest`, and `@ContextConfiguration`, when the
  origin is trusted from a fully qualified annotation name or explicit single-type
  import and the same fully qualified name is not declared by scanned source.
- `test.spring_test_slices[].annotation` is the direct annotation symbol with `@`.
- `test.spring_test_slices[].slice_kind` is one of `"spring_boot_test"`,
  `"web_mvc_test"`, `"data_jpa_test"`, or `"context_configuration"`.
- `test.spring_test_slices[].signal_kind` is `"spring_test_slice"`. It is a
  source-visible annotation classification only and must not encode runtime Spring test
  context startup, bean graph contents, active profiles, MockMvc setup, database access,
  or slice correctness.
- `test.spring_test_slices[].evidence_ids` references the direct annotation evidence and
  must resolve to records in `evidence-index.jsonl`.
- `test.mock_signals[]` contains direct supported mock-related annotations on emitted
  test classes. Current supported annotations are `@MockBean` and `@SpyBean`, when the
  origin is trusted from a fully qualified annotation name or explicit single-type
  import and the same fully qualified name is not declared by scanned source. Mock
  annotations are recorded as signals only; they are not test-class marker annotations
  by themselves.
- `test.mock_signals[].annotation` is the direct annotation symbol with `@`.
- `test.mock_signals[].mock_signal` is one of `"spring_boot_mockbean_annotation"` or
  `"spring_boot_spybean_annotation"`.
- `test.mock_signals[].signal_kind` is `"mock_annotation"`. It is a source-visible
  annotation classification only and must not encode runtime Spring bean override
  behavior, Mockito behavior, bean graph contents, database access, or slice
  correctness.
- `test.mock_signals[].target_kind` is `"type"` or `"field"` for the current slice.
- `test.mock_signals[].target_name` is the test class name for type-level annotations or
  the declared field name for field-level annotations.
- `test.mock_signals[].evidence_ids` references the direct annotation evidence and must
  resolve to records in `evidence-index.jsonl`.
- Spring test slice and mock annotation origins follow the same conservative external
  origin rule as Spring Test framework signals. Unresolved simple-name annotations,
  wildcard-import-only annotations, same-package/local fake annotations,
  source-declared fake framework annotations, generated-source-only annotations,
  classpath-only annotations, and static-import-only references are skipped rather than
  emitted as slice or mock facts.
- The current output parses bounded direct class literals from supported Spring test
  slice annotations only for conservative tested-subject relation/status rows. It does
  not emit slice annotation properties, active profiles, configuration classes, or mock
  target types as structured fields. Other source text may appear only as bounded
  evidence excerpts. Slice/mock signals do not create endpoint, entity, repository,
  bean, coverage, execution, CI, assertion, or runtime behavior facts.

Current v0.7 tested-subject relation rules:

- `test.tested_subjects[]` remains a bounded tested-subject relation/status list. It
  must never be described as coverage, execution, assertion, CI, runtime call graph, or
  complete subject mapping.
- Current emitted tested-subject rows are conservative relation/status rows from
  supported naming conventions, exact production-class imports, direct test field
  types, and bounded direct class literals inside supported Spring test slice
  annotations.
- `tested_subject.relation_status` is one of:
  - `"inferred"`: a supported source-visible test-side subject signal matched exactly
    one source-visible production class declaration.
  - `"not_detected"`: the analyzer ran but either no supported tested-subject signal was
    detected for the test class, or a supported source-visible candidate did not match a
    production class declaration.
  - `"ambiguous"`: a supported source-visible candidate matched multiple production
    class declarations.
  - `"unsupported"`: a source-visible candidate shape was present but outside the
    bounded tested-subject parser, such as a generic field type.
  - `"not_analyzed"`: reserved compatibility value for future cases where this relation
    analyzer did not run for an otherwise emitted test fact. It is not emitted by the
    current analyzer when supported test roots are analyzed.
- `tested_subject.relation_type` is one of `"naming_convention"`, `"test_import"`,
  `"test_field_type"`, `"spring_test_slice_class_literal"`, or `"not_detected"`.
- Inferred relations require evidence for the test-side source observation and the
  matched production-side source observation. `support_type` is `"inferred"`.
- `tested_subject.class_name` and `tested_subject.target_module_id` are populated only
  when a source-visible production candidate is recorded for an `"inferred"` or
  `"ambiguous"` row. They are `null` for non-candidate status-only rows such as
  `"not_detected"` and `"unsupported"`.
- `tested_subject.candidate_reference` is `null` when a production candidate class is
  recorded. It contains the bounded source-visible candidate text for status-only rows,
  such as a missing naming candidate or unsupported field type, and is `null` for the
  no-supported-signal `"not_detected"` row.
- `tested_subject.support_type` is `"inferred"` for rows that record a production
  candidate class. It is `null` for status-only rows that do not record a production
  candidate.
- `tested_subject.confidence` is `"medium"` for unique inferred target matches and
  `"low"` for ambiguous, unsupported, or not-detected status rows.
- `tested_subject.uncertainty` is `null` for unique inferred target matches. Current
  uncertainty values include `"ambiguous_subject_name"`,
  `"no_matching_production_class"`, `"unsupported_subject_reference"`, and
  `"no_supported_subject_signal"`.
- Naming-convention inference remains conservative: strip supported test suffixes such
  as `Test`, `Tests`, and `IT` from the test class simple name and match the candidate
  against production classes in the same supported module.
- Import inference is limited to explicit non-static single-type imports whose fully
  qualified name exactly matches a production class declaration in the analyzed module.
  Wildcard imports, static imports, classpath-only imports, and external framework
  imports do not create no-match rows.
- Field-type inference is limited to direct test class field declarations whose type can
  be represented as a non-generic class/interface type and matched to a production class
  through an exact fully qualified name, explicit single-type import, or same-package
  reference. Generic, array, primitive, wildcard, classpath-only, and otherwise
  unsupported shapes do not create inferred rows; supported production-like generic
  shapes are statused as `"unsupported"`.
- Spring test slice class-literal inference may link a test class to a production class
  only when a supported slice annotation contains a direct class literal and the target
  can be matched to a production class declaration. It remains an inferred
  tested-subject hint, not proof that the test executes that subject or covers any
  behavior.

Current v0.7 quality and change-risk rules:

- `quality.analysis_status` is `"analyzed"` when at least one test-gap or change-risk
  planning hint is emitted. It is `"not_detected"` when no quality planning hints are
  emitted.
- `quality.test_gap_signals.analysis_status` and
  `quality.change_risk_signals.analysis_status` are `"analyzed"` when their respective
  `items[]` arrays are non-empty and `"not_detected"` otherwise.
- `quality.test_gap_signals.items[]` contains conservative absence-sensitive planning
  hints for selected source-visible change surfaces when no inferred tested-subject
  relation matches the subject class in the same target module.
- Current test-gap signals include:
  - `"endpoint_without_obvious_test"` for Spring MVC endpoint handler facts whose
    controller class has no inferred tested-subject relation in the same module.
  - `"repository_without_obvious_test"` for Spring repository surface facts whose class
    has no inferred tested-subject relation in the same module.
  - `"entity_without_obvious_test"` for JPA entity facts whose class has no inferred
    tested-subject relation in the same module.
- Test-gap signal `status` is `"no_obvious_test"`. This status means no matching
  inferred tested-subject relation was found in the bounded generated test inventory. It
  must not be rendered or interpreted as proof that no tests exist, no behavior is
  covered, or no assertions exercise the subject.
- Test-gap signal `inference_basis` is
  `"no_inferred_tested_subject_relation_for_subject_class"` when supported test roots
  were analyzed and `"bounded_test_inventory_not_available"` when supported test roots
  were not detected.
- Test-gap signal `confidence` is `"low"`. `uncertainty` is
  `"bounded_test_inventory_supported_relations_only"` when supported test roots were
  analyzed and `"supported_test_roots_not_detected"` otherwise.
- `quality.change_risk_signals.items[]` contains warning-oriented or uncertain planning
  hints from existing deterministic facts. Current signals include:
  - `"spring_service_change_surface"` for source-visible `@Service` component facts.
  - `"spring_configuration_change_surface"` for source-visible Spring configuration
    class facts.
  - `"spring_configuration_properties_change_surface"` for source-visible
    `@ConfigurationProperties` facts.
  - `"spring_bean_method_change_surface"` for source-visible `@Bean` method facts.
  - `"transaction_boundary_change_surface"` for source-visible transaction-boundary
    facts.
  - `"scheduled_method_change_surface"` for source-visible scheduled-method facts.
  - `"event_listener_change_surface"` for source-visible event-listener facts.
  - `"messaging_listener_change_surface"` for source-visible messaging-listener facts.
  - `"repository_entity_relation_uncertain"` for Spring Data repository facts whose
    repository/entity relation status is not `"inferred"`.
  - `"jpa_relationship_change_surface"` for source-visible JPA relationship metadata.
  - `"spring_security_warning_change_surface"` for Spring Security configuration warning
    facts.
- Change-risk signal `status` is one of `"planning_hint"`,
  `"warning_oriented_planning_hint"`, or `"uncertain_planning_hint"`.
- Change-risk signal `risk_basis` records the deterministic fact family that produced
  the hint. Current values include `"source_visible_service_stereotype"`,
  `"source_visible_spring_configuration"`, `"source_visible_configuration_properties"`,
  `"source_visible_bean_method"`, `"source_visible_transaction_boundary"`,
  `"source_visible_scheduled_method"`, `"source_visible_event_listener"`,
  `"source_visible_messaging_listener"`,
  `"repository_entity_relation_status_<status>"`,
  `"source_visible_jpa_relationship_metadata"`, and
  `"source_visible_spring_security_warning"`.
- Change-risk signal `confidence` is `"low"`. Current `uncertainty` values include
  `"source_visible_change_surface_only"`, `"bounded_repository_entity_relation_rules_only"`,
  `"relationship_target_declared_type_only"`, and
  `"warning_signal_only_not_vulnerability_or_correctness"`.
- Quality signal common fields are `id`, `module_id`, `signal`, `status`,
  `subject_kind`, `subject_id`, `subject_name`, nullable `subject_class_name`, nullable
  `subject_member_name`, `confidence`, `uncertainty`, and `evidence_ids`. Test-gap
  signals also include `inference_basis` and `related_test_ids`; current
  `related_test_ids` is an empty array. Change-risk signals include `risk_basis`.
- Quality signal evidence IDs point to the underlying source-visible fact or warning
  evidence that produced the hint. Absence-sensitive test-gap signals do not fabricate
  absence evidence; their evidence supports the subject fact being considered.
- Test-gap and change-risk signals must not be presented as coverage, correctness, CI,
  runtime, call-graph, vulnerability, production impact, business priority, assertion
  analysis, or test execution truth.

Current v0.7 deterministic sorting rules:

- Test class items sort by module order, class name, and source path.
- Test methods sort by source order when line evidence is available, then by method name,
  method kind, annotation, and evidence discriminator.
- Framework signals sort by signal name and evidence discriminator.
- Spring test slices sort by `slice_kind`, annotation, and evidence discriminator.
- Mock annotation signals sort by `target_kind`, `target_name`, `mock_signal`,
  annotation, and evidence discriminator.
- Tested-subject rows sort by relation status, relation type, class name, candidate
  reference, support type, confidence, uncertainty, and evidence discriminator.
- Test-gap signals sort by module order, subject kind, subject name, and subject ID.
- Change-risk signals sort by module order, signal, subject kind, subject name, and
  subject ID.

### v0.8 Local Markdown And Document Ingestion Contract

This section defines the v0.8 public output boundary for local Markdown/project
document ingestion. The current implementation includes the discovery, inventory, ATX
heading, bounded chunk, document evidence, conservative reconciliation signal subset,
and compact local documentation guide rendering under the documented safety rules.

The v0.8 local document ingestion contract uses:

- `schema_version: "0.8"` for output that preserves the v0.7 contracts and adds the
  top-level `documents` owner. In the current implementation, `documents` contains
  deterministic default-scope local Markdown discovery policy metadata, document
  inventory, bounded ATX heading references, bounded chunk references, and conservative
  reconciliation hints.
- The same four output files under `.project-memory/`.
- A top-level `documents` object as the owner of local Markdown document inventory,
  applied discovery policy metadata, current document structure references, and
  document/code reconciliation signals.
- Existing evidence fields plus the reserved `document` evidence type. The current
  implementation emits `document` evidence records for accepted file, heading, and
  chunk observations, plus document-side mention observations used by uncertain
  reconciliation signals, within the existing evidence field set.

The current `schema_version: "0.8"` state emits document inventory, bounded
heading/chunk navigation references, and resolving `document` evidence for accepted
file, heading, chunk, and bounded reconciliation mention observations. It emits
conservative `documents.reconciliation` rows as low-confidence uncertain inspection
hints only. It also emits compact guide-rendered local documentation orientation from
structured document inventory, bounded heading/chunk references, discovery policy facts,
and reconciliation hints. It does not emit document summaries or serialized document
bodies. Future layers must update tests and contract text when they add other local
documentation outputs or semantics.

Current `project-map.json` excerpt. Unchanged v0.7 fields are omitted for focus:

```json
{
  "schema_version": "0.8",
  "documents": {
    "analysis_status": "analyzed",
    "discovery": {
      "scope": "default_local_markdown",
      "path_policy": "repository_relative_in_root",
      "symlink_policy": "skip_symlinks",
      "included_patterns": [
        "README.md",
        "README.markdown",
        "<module>/README.md",
        "<module>/README.markdown",
        "docs/**/*.md",
        "adr/**/*.md",
        "adrs/**/*.md"
      ],
      "excluded_patterns": [
        ".git/**",
        ".project-memory/**",
        "**/.*/**",
        "target/**",
        "build/**",
        "out/**",
        "dist/**",
        "node_modules/**",
        "**/generated/**",
        "**/maintainer/**",
        "docs/internal/**",
        "docs/private/**",
        "**/secrets/**"
      ]
    },
    "items": [
      {
        "id": "document:README.md",
        "document_kind": "local_markdown",
        "format": "markdown",
        "module_id": null,
        "path": "README.md",
        "title": "Root docs",
        "title_source": "first_heading",
        "discovery_source": "root_readme",
        "headings": [
          {
            "id": "document_heading:README.md:heading:Root%20docs:occ:000001",
            "level": 1,
            "title": "Root docs",
            "anchor": "root-docs",
            "line_start": 1,
            "line_end": 1,
            "evidence_ids": [
              "ev:README.md:1-1:document:heading:Root%20docs:decl:000001"
            ]
          }
        ],
        "chunks": [
          {
            "id": "document_chunk:README.md:chunk:000001",
            "heading_id": "document_heading:README.md:heading:Root%20docs:occ:000001",
            "line_start": 1,
            "line_end": 1,
            "content_status": "not_serialized",
            "evidence_ids": [
              "ev:README.md:1-1:document:chunk:000001"
            ]
          }
        ],
        "evidence_ids": [
          "ev:README.md:unknown:document:file:README.md"
        ]
      }
    ],
    "reconciliation": {
      "analysis_status": "analyzed",
      "items": [
        {
          "id": "document_reconciliation:document_only_endpoint_mention:README.md:/ghost:decl:000001",
          "module_id": null,
          "signal": "document_only_endpoint_mention",
          "status": "uncertain_signal",
          "document_id": "document:README.md",
          "document_path": "README.md",
          "document_chunk_id": "document_chunk:README.md:chunk:000001",
          "source_fact_kind": null,
          "source_fact_id": null,
          "subject_kind": "endpoint_like_path",
          "subject_name": "/ghost",
          "match_basis": "bounded_endpoint_like_path_token",
          "confidence": "low",
          "uncertainty": "document_mention_not_matched_to_source_backed_api_fact",
          "evidence_ids": [
            "ev:README.md:2-2:document:mention:/ghost:decl:000001"
          ]
        }
      ]
    }
  }
}
```

v0.8 document discovery rules:

- Default discovery is conservative and local. It includes root `README.md` or
  `README.markdown`, README files directly under supported Maven module roots, root
  `docs/**/*.md`, and root `adr/**/*.md` or `adrs/**/*.md`.
- Runbooks, local notes, private/internal docs, hidden paths, generated outputs,
  dependency directories, and maintainer-only paths are not included by default. They may
  be considered later only through an explicit include/exclude design that preserves the
  repository-root boundary and output safety rules.
- All emitted document paths are normalized repository-relative paths. They must not be
  absolute, must not start with `./`, must use slash separators, and must not escape the
  scanned repository root after normalization.
- The default symlink policy is `skip_symlinks`. The scanner must not follow symlinked
  Markdown files or symlinked directories during default document discovery. Any future
  symlink-following mode must be explicit, non-default, and separately documented.
- `.project-memory/` and generated output files are excluded so scans do not ingest their
  own generated memory. `.git/`, hidden path segments, build output directories, and
  dependency cache directories are excluded by default.
- Include/exclude policy changes must be compatibility-aware. If user configuration is
  introduced, the generated output should record the applied policy and still enforce
  repository-relative path containment.

Current v0.8 document inventory rules:

- `documents.analysis_status` is `"analyzed"` when local document discovery ran,
  `"not_detected"` when discovery ran and no default-scope document was found, and
  `"not_analyzed"` only when document discovery is intentionally disabled by a future
  explicit mode or configuration.
- `documents.discovery` records the effective high-level policy used for discovery. It is
  not evidence and does not prove that excluded files do or do not exist.
- `documents.items[]` contains document inventory facts and bounded document navigation
  references only. A document item is not a code fact, API fact, module fact, test fact,
  configuration fact, or runtime behavior claim.
- `document.id` is stable within the scan and derived from the normalized
  repository-relative path using the same percent-encoded ID-key discipline used by
  existing path-backed facts.
- `document.document_kind` is `"local_markdown"` for the v0.8 local Markdown slice.
- `document.format` is `"markdown"` for Markdown files accepted by the v0.8 default
  discovery policy.
- `document.module_id` is the owning module ID only when the document is inside a
  supported module root and can be assigned deterministically. Repository-level docs use
  `null`.
- `document.path` is the normalized repository-relative document path.
- `document.title` is derived from the first supported non-blank heading when present
  and otherwise from the filename. It is document metadata, not a semantic summary.
- `document.title_source` is `"first_heading"` when `document.title` is derived from the
  first supported non-blank heading and `"filename"` otherwise.
- `document.discovery_source` identifies the default-scope reason, such as
  `"root_readme"`, `"module_readme"`, `"docs_tree"`, `"adr_tree"`, or
  `"explicit_include"` if a future configuration contract adds explicit includes.
- `document.headings[]` and `document.chunks[]` contain bounded structure references for
  the accepted Markdown file. They must not serialize full document bodies, paragraphs,
  arbitrary lists, code blocks, tables, or generated summaries.
- `document.evidence_ids` references the file-level `document` evidence for the accepted
  Markdown file and must resolve to records in `evidence-index.jsonl`.

Current v0.8 heading and chunk rules:

- Supported headings are deterministic Markdown ATX headings with levels 1 through 6.
  Supported ATX heading lines may have up to three leading spaces, must use one to six
  `#` characters, and must end the marker at end of line or with whitespace before the
  heading text. Heading-like lines inside fenced code blocks are not emitted as
  headings. Future support for Setext headings or other Markdown constructs requires a
  contract update if output semantics change.
- Heading IDs are stable and path-scoped. Duplicate heading text is disambiguated with a
  deterministic document-order ordinal. When heading text contains an obvious
  secret-looking value covered by the v1.7 redaction policy, the heading key used in
  `heading.id`, `chunk.heading_id`, graph source references, and heading evidence IDs
  is derived from the redaction-safe heading text before ID-key percent-encoding.
- `heading.title` is the normalized heading text. It must be bounded and Markdown-safe
  when rendered. It is not a summary of the following section.
- `heading.anchor` is a deterministic local anchor when it can be computed safely. For
  redacted heading text, the anchor is computed from the same redaction-safe heading key
  used by heading IDs. If a stable anchor cannot be computed, it should be `null`
  rather than guessed.
- `heading.line_start` and `heading.line_end` point to the heading line or range when
  available. The current implementation emits integer line ranges for every emitted
  heading.
- `heading.evidence_ids` references the heading-line `document` evidence and must
  resolve to records in `evidence-index.jsonl`. Heading evidence excerpts use the
  normalized heading line only; they must not copy the following section body.
- `chunk.id` is stable and path-scoped. Chunks are ordered by document order.
- `chunk.heading_id` links the chunk to the nearest owning heading when present and is
  `null` for a no-heading fallback chunk.
- `chunk.line_start` and `chunk.line_end` bound the chunk range when available.
- `chunk.content_status` is `"not_serialized"` in the v0.8 design. The project map does
  not copy chunk text into generated JSON.
- `chunk.evidence_ids` references the chunk-boundary `document` evidence and must
  resolve to records in `evidence-index.jsonl`. Chunk evidence excerpts identify only
  the chunk line range and owning heading, or `none` when no owning heading exists; they
  must not copy chunk bodies or full paragraphs.
- Chunk sizing must be bounded by deterministic line and byte limits. If a long section
  must be split, splits use deterministic document-order ordinals and do not depend on
  semantic summarization. Empty Markdown files may have no chunks when no stable
  non-empty line range exists.

Current aggregate local Markdown caps:

- The current v0.9 implementation caps local Markdown candidate selection and emitted
  local Markdown inventory at 256 accepted documents, and caps emitted local Markdown
  inventory at 16 MiB of aggregate accepted Markdown file bytes per scan.
- Local Markdown candidate selection is bounded before document fact, document evidence,
  and structure extraction. Candidates outside the document count bound do not create
  document facts, document structure, or `document` evidence records.
- It caps emitted document structure at 4096 heading references and 4096 chunk
  references per scan. Additional headings or chunks are omitted after the cap is
  reached, and omitted structure references do not create `document` evidence records.
- It caps reconciliation document-side mention observations at 2048 per scan and caps
  emitted `documents.reconciliation.items[]` rows at 2048 per scan. Mention evidence is
  emitted only for document-side reconciliation rows that pass the output cap.
- Source-only `source_api_without_document_mention` and
  `module_without_document_mention` rows are emitted only when document candidate input
  and mention observation input were not truncated by the aggregate document count,
  document byte, or mention caps. If those inputs are truncated, the cap diagnostic is
  the conservative signal and missing-document rows are omitted rather than inferred
  from incomplete matching input.
- These caps bound document-backed `project-map.json`, `evidence-index.jsonl`, and
  local-document guide input volume. Source-only reconciliation rows reuse the existing
  source-backed evidence IDs for the source fact being considered; they do not create
  `document` evidence.
- When one of these caps is reached, the scan remains successful and records a bounded
  non-fatal diagnostic item under `scan.diagnostics.items[]`. Diagnostics are scan
  metadata, not evidence, and diagnostic IDs must not appear in `evidence_ids`.

Current v0.8 reconciliation signal taxonomy:

- Reconciliation lives under `documents.reconciliation`. It compares bounded document
  observations against existing generated source-backed facts and remains separate from
  code-backed project facts.
- `documents.reconciliation.analysis_status` is `"analyzed"` when bounded
  reconciliation rules run against at least one default-scope document. It is
  `"not_detected"` when document discovery ran but no default-scope document or no
  eligible bounded reconciliation input exists. It is `"not_analyzed"` only when
  reconciliation is intentionally disabled by a future explicit mode or configuration.
- Every reconciliation item uses `status: "uncertain_signal"` and `confidence: "low"`
  unless a later contract defines a stronger evidence-backed relation type.
- Current reconciliation items emit these fields:
  `id`, `module_id`, `signal`, `status`, `document_id`, `document_path`,
  `document_chunk_id`, `source_fact_kind`, `source_fact_id`, `subject_kind`,
  `subject_name`, `match_basis`, `confidence`, `uncertainty`, and `evidence_ids`.
- `id` is stable within the scan and derived from the signal kind plus the document
  mention or source fact identity. It is not evidence by itself.
- `module_id` is the source fact module when a source fact is present, the document owner
  module when the signal is document-only, and `null` for repository-level document-only
  signals.
- `document_id`, `document_path`, and `document_chunk_id` identify the accepted
  default-scope document observation when one exists. They are `null` for source-only
  missing-document signals.
- `source_fact_kind` and `source_fact_id` identify the deterministic source-backed fact
  when one exists, such as `"spring_mvc_endpoint"`, `"openapi_operation"`, or
  `"maven_module"`. They are `null` for document-only signals.
- `subject_kind` and `subject_name` describe the bounded comparison subject, not a
  promoted fact. Current subject kinds include `"endpoint_like_path"`, `"api_path"`,
  `"module_reference"`, and `"maven_module"`.
- `evidence_ids` must resolve in `evidence-index.jsonl`. Document-only signals reference
  document mention evidence for the observed token. Source-only signals reuse the
  source-backed fact evidence. Missing document mentions do not fabricate absence
  evidence.
- `document_only_endpoint_mention`: a default-scope document contains an endpoint-like
  path token that does not match any emitted source-visible endpoint fact or declared
  OpenAPI operation fact under the bounded matching rules.
- `source_api_without_document_mention`: an emitted source-visible endpoint fact or
  declared OpenAPI operation fact has no obvious matching mention in default-scope local
  Markdown under the bounded matching rules.
- `document_only_module_reference`: a default-scope document contains a module-like path
  or module-name token that does not match an emitted module fact under the bounded
  matching rules.
- `module_without_document_mention`: an emitted module fact has no obvious matching
  mention in default-scope local Markdown under the bounded matching rules.
- Source-only missing-document signals are emitted only when at least one default-scope
  document was accepted for the scan. A scan with no accepted local Markdown documents
  must not create one source-only reconciliation row per source fact.
- For document-only signals, `source_fact_kind` and `source_fact_id` are `null`. For
  source-only signals, `document_id` and `document_chunk_id` are `null`. Signals that
  compare both sides populate both source and document references when both sides exist.
- Reconciliation `match_basis` values identify deterministic token rules, such as
  `"bounded_endpoint_like_path_token"`, `"bounded_source_api_path_token"`,
  `"bounded_module_path_token"`, or `"bounded_module_name_token"`.
- Reconciliation `uncertainty` values should explain the boundary, such as
  `"document_mention_not_matched_to_source_backed_api_fact"`,
  `"source_api_fact_not_matched_to_default_scope_document"`,
  `"document_module_reference_not_matched_to_module_fact"`, or
  `"module_fact_not_matched_to_default_scope_document"`.
- Reconciliation signals must not be presented as stale-doc truth, completeness checks,
  coverage, implementation proof, runtime routing proof, documentation-quality scores,
  business priority, correctness, vulnerability, or production-impact claims.

Current v0.8 deterministic sorting rules:

- Document items sort by module order, document path, and ID.
- Headings sort by document order, then level, title, and ID.
- Chunks sort by document order, then ordinal and ID.
- Reconciliation signals sort by signal, module order when a source module is available,
  subject kind, subject name, document path, and ID.

### v1.0 Schema Marker And Compatibility Policy

Current normal generated `project-map.json` output uses:

```json
{
  "schema_version": "1.0"
}
```

The v1.0 schema marker is a compatibility-policy marker over the v0.9 output shape, not
a broad schema redesign. It preserves:

- the same four output files under `.project-memory/`;
- the v0.9 top-level `scan` metadata owner and redaction rules;
- the v0.8 local Markdown/document inventory, heading, chunk, reconciliation, and guide
  rendering boundaries;
- the v0.7 tests and quality planning-hint boundaries;
- the v0.6 JPA/domain, v0.5 Spring application surface, v0.4 API surface, and v0.3
  Maven/build/config output boundaries;
- the current evidence field set, evidence types, evidence ID resolution rules,
  confidence labels, excerpt boundaries, and normalized repository-relative path rules.

The marker change does not add output fields, remove output fields, rename fields,
change Markdown compatibility, change analyzer behavior, create tool-config evidence,
or change evidence semantics. Consumers that already understand the v0.9 generated
shape should treat `schema_version: "1.0"` as the same output and evidence semantics
with a stable-line marker.

Conservative v1.0 compatibility expectations:

- `.project-memory/project-map.json` is the primary machine-readable project map.
  The v1.0 compatibility line keeps the documented current field names, nesting,
  JSON null and empty-array conventions, and field semantics from the v0.9 shape
  except for the `schema_version` marker value. Later additive fields are
  compatibility expansions only when this document, tests or goldens, the changelog,
  and release notes describe them. Consumers should ignore unknown fields when
  practical. Removing fields, renaming fields, changing field meanings, or changing
  required/nullability semantics is a breaking output-contract change.
- `.project-memory/evidence-index.jsonl` remains newline-delimited JSON with the
  current evidence field set and evidence semantics documented in
  `EVIDENCE_MODEL.md`. Evidence IDs must resolve from generated facts that reference
  them and remain stable within one generated output set. The v1.0 marker does not
  change evidence types, path normalization, confidence labels, excerpt boundaries,
  or the no-tool-config-evidence decision.
- `.project-memory/endpoints.md` is a deterministic human-readable API surface
  inventory. The filename, cautious fact categories, category separation, and visible
  evidence references are part of the documented output expectation. Exact Markdown
  heading wording, list formatting, compactness, and presentation order may evolve as
  long as the generator keeps the same evidence-backed meaning and does not merge
  source-visible endpoints, declared OpenAPI operations, generated-source warnings,
  repository-rest warnings, or hidden HTTP warnings into unsupported facts.
- `.project-memory/agent-guide.md` is a deterministic human-readable orientation guide
  generated from structured project facts and `evidence-index.jsonl`. Its caution
  boundaries are stable: it must not invent architecture, summarize source or document
  bodies, call LLMs, call external services, or add unsupported runtime claims. Exact
  Markdown layout, section wording, and evidence-reference presentation may evolve for
  readability. Downstream automation should parse `project-map.json` and
  `evidence-index.jsonl` rather than treating Markdown presentation as a stable
  machine-readable API.

The v0.9-to-v1.0 migration is intentionally narrow. Normal v1.0 generation writes
`schema_version: "1.0"` instead of `"0.9"` in `project-map.json`; the current JSON
shape and evidence semantics are preserved. Consumers that gate on schema version need
to accept `"1.0"` for the same documented shape. No separate evidence-index migration is
required, but a generated output set should be treated as a set: regenerate all four
files together when moving a project from v0.9 output to v1.0 output so evidence
references, Markdown references, and JSON facts remain aligned.

Future compatibility documentation requirements:

- Breaking changes must update this document in the affected file section, update
  `EVIDENCE_MODEL.md` when evidence shape or semantics change, update focused tests or
  goldens, add a changelog entry, and include release-note migration notes that name the
  affected file, field or behavior, old behavior, new behavior, compatibility impact,
  and required consumer action.
- Deprecations must be called out in the changelog and release notes with the affected
  file, field or behavior, current support status, replacement when available, and
  removal conditions when known.
- Migration notes are required whenever users or downstream tools need to regenerate
  outputs, update schema-version allowlists, change parsers, or reinterpret generated
  facts. Markdown-only presentation changes should say whether the JSON/JSONL contract
  is unchanged.

Any later v1.x field addition, field removal, field rename, evidence shape change, or
evidence semantic change must update this document, `EVIDENCE_MODEL.md`, focused tests
or goldens, changelog entries, and release notes in the same logical change.

### v1.1 Gradle Compatibility Expansion

This section defines the public output boundary for the v1.1 Gradle Java/Spring
release. It started as a design contract and is now the released contract for the
implemented v1.1 Gradle support.

Schema and compatibility decisions:

- v1.1 Gradle support is an additive `schema_version: "1.0"` compatibility
  expansion, not a `schema_version: "1.1"` migration.
- The same four output files remain under `.project-memory/`.
- Pure Maven scans should preserve the current v1.0 output and evidence semantics.
  Maven fields such as `pom_path`, `pom_evidence_ids`, `build_config.maven`, Maven
  warning IDs, and Maven evidence ID conventions must not be reinterpreted for Gradle.
- Gradle and mixed Maven/Gradle scans may add the fields documented in this section.
  Consumers that understand the v1.0 shape should ignore unknown additive fields when
  practical.
- Removing fields, renaming fields, changing existing Maven field meanings, changing
  nullability, or changing evidence semantics remains a breaking output-contract
  change.

Supported Gradle build inputs:

- Root Gradle files: `settings.gradle`, `settings.gradle.kts`, `build.gradle`, and
  `build.gradle.kts`.
- Project Gradle build files under supported Gradle project directories:
  `build.gradle` and `build.gradle.kts`.
- Gradle files are local build-file inputs only. The scanner must not execute Gradle,
  invoke the Gradle wrapper, use the Gradle Tooling API, resolve plugins, resolve
  dependencies, evaluate build scripts, fetch remote metadata, or reconstruct an
  effective Gradle model.
- Gradle build-file reads must use the same bounded, stable, no-symlink,
  repository-contained discipline as other build-file inputs. Oversized, unreadable,
  symlinked, or otherwise unsafe Gradle build files degrade to bounded
  `scan.diagnostics` items and do not emit evidence from skipped content.

Build-system summary rules:

- `project.build.system` may be `"maven"`, `"gradle"`, `"mixed"`, or
  `"not_detected"` in the v1.1 boundary.
- `"maven"` keeps the current meaning: an accepted root `pom.xml` is the detected build
  input and no accepted root Gradle build input participates in the scan summary.
- `"gradle"` means one or more accepted root Gradle build inputs participate in the scan
  summary and no accepted root `pom.xml` participates.
- `"mixed"` means accepted Maven and Gradle build inputs both participate in the scan
  summary. Mixed output is path-de-duplicated by module path and must not emit duplicate
  application facts for the same module/source root.
- `"not_detected"` keeps the current compatibility meaning for repositories without
  accepted Maven or Gradle build inputs, while supported root source, test, or resource
  roots may still be represented as the scan-root module.
- `project.build.root_build_file` remains a compatibility summary string. For pure
  Maven scans it remains `"pom.xml"`. For pure Gradle scans it is the deterministic
  primary root Gradle file in this order when accepted: `settings.gradle`,
  `settings.gradle.kts`, `build.gradle`, `build.gradle.kts`. For mixed scans it remains
  `"pom.xml"` when the root POM is accepted; consumers should use `root_build_files`
  for the complete build-input list.
- Gradle or mixed output adds `project.build.root_build_files[]` as the
  authoritative accepted root build-file list. Each item contains `path`,
  `build_system`, `role`, `language`, and `evidence_ids`.
- In mixed output, `root_build_files[]` includes the accepted root `pom.xml` item with
  `build_system: "maven"`, `role: "root_pom"`, `language: "xml"`, and existing root POM
  `build_file` evidence. Gradle root items use `role: "settings"` for root settings
  files and `role: "root_project_build"` for root build files, with `language` set to
  `"groovy_dsl"` for `.gradle` and `"kotlin_dsl"` for `.gradle.kts`. The list is sorted
  with `pom.xml` first when present, followed by accepted root Gradle files in the
  deterministic `root_build_file` priority order.

Example Gradle build summary:

```json
{
  "system": "gradle",
  "root_build_file": "settings.gradle.kts",
  "root_build_files": [
    {
      "path": "settings.gradle.kts",
      "build_system": "gradle",
      "role": "settings",
      "language": "kotlin_dsl",
      "evidence_ids": [
        "ev:settings.gradle.kts:1-1:build_file:gradle:settings"
      ]
    },
    {
      "path": "build.gradle.kts",
      "build_system": "gradle",
      "role": "root_project_build",
      "language": "kotlin_dsl",
      "evidence_ids": [
        "ev:build.gradle.kts:1-1:build_file:gradle:build"
      ]
    }
  ],
  "evidence_ids": [
    "ev:settings.gradle.kts:1-1:build_file:gradle:settings",
    "ev:build.gradle.kts:1-1:build_file:gradle:build"
  ]
}
```

Gradle module inventory rules:

- Module identity remains path-based. `module_id` is still `module:.` for the scan root
  and `module:<module_path>` for child modules. Gradle project names, display names,
  artifact names, and plugin-derived values must not replace path-derived module IDs.
- Standard Gradle project paths map to repository-relative module paths by replacing
  colon separators with path separators: `:` maps to `.`, `:services:orders` maps to
  `services/orders`.
- `project.modules.items[]` keeps existing fields. For Gradle-only modules,
  `pom_path` is `null` and `pom_evidence_ids` is an empty array. These fields must not
  point to Gradle build files.
- Gradle or mixed module items add:
  - `build_systems`: sorted build-system labels that contributed to the module, such as
    `["gradle"]` or `["maven", "gradle"]`.
  - `gradle_project_path`: the Gradle project path such as `":"` or
    `":services:orders"`, or `null` when no Gradle project path contributed to the
    module.
- `declaration_kind` may add `"gradle_settings_include"` for child modules declared by
  a supported static Gradle settings include. The scan root may continue to use
  `"scan_root"`.
- `declaration_evidence_ids` references settings include evidence for Gradle child
  modules. It remains an empty array for the scan root unless another supported
  declaration produced the module.
- `source_roots` and `test_roots` remain repository-relative Java roots under the
  module. v1.1 Gradle support includes only standard Java roots:
  `src/main/java` and `src/test/java`.
- `build_config.resources` continues to represent standard resource roots, including
  `src/main/resources` and `src/test/resources`, under supported Gradle modules.
- A Gradle module with no supported Java production, test, or resource roots may be
  emitted with `support_status: "unsupported"` and a Gradle warning. A valid static
  include whose default project directory is missing may be emitted with
  `support_status: "missing_project_directory"` and a Gradle warning.

Gradle `build_config` rules:

- Gradle module-owned build orientation lives under
  `project.modules.items[].build_config.gradle`.
- The v1.1 `gradle` subsection is intentionally small:

```json
{
  "analysis_status": "analyzed",
  "project_path": ":services:orders",
  "build_files": [
    {
      "path": "settings.gradle.kts",
      "role": "settings",
      "language": "kotlin_dsl",
      "evidence_ids": [
        "ev:settings.gradle.kts:12-12:build_file:gradle:include:decl:000001"
      ]
    },
    {
      "path": "services/orders/build.gradle.kts",
      "role": "project_build",
      "language": "kotlin_dsl",
      "evidence_ids": [
        "ev:services/orders/build.gradle.kts:1-1:build_file:gradle:build"
      ]
    }
  ],
  "source_sets": {
    "analysis_status": "not_analyzed",
    "items": []
  }
}
```

- `language` is `"groovy_dsl"` for `.gradle` files and `"kotlin_dsl"` for `.gradle.kts`
  files.
- `build_config.gradle` must not contain dependency inventory, plugin inventory, task
  inventory, repository declarations, arbitrary build-script values, or generated-source
  roots in the v1.1 boundary.
- `source_sets.analysis_status: "not_analyzed"` records the v1.1 decision to defer
  custom static `sourceSets` support. Standard roots are still represented by
  `source_roots`, `test_roots`, and `build_config.resources`.

Static Gradle settings parsing boundary:

- v1.1 supports only simple string-literal Gradle settings includes in the root
  `settings.gradle` or `settings.gradle.kts`.
- Supported include forms are limited to direct literal project paths such as
  `include "app"`, `include ":app"`, `include("app")`, `include(":app")`, and
  comma-separated literal variants of those forms.
- Supported project paths must normalize to repository-relative paths under the scan
  root, with no absolute path, no `.` or `..` segment, no empty child segment, and no
  scan-root escape.
- The default project directory mapping is the only v1.1 mapping. Custom
  `project(":x").projectDir = file("...")`, `includeFlat`, `includeBuild`, variables,
  loops, conditionals, function indirection, convention plugins, settings plugins, and
  other dynamic settings behavior are not analyzed.
- Simple `.kts` include parsing is included for the same literal include subset.
  Broader Kotlin DSL semantic parsing and Kotlin source analysis are out of scope.

Mixed Maven/Gradle behavior:

- Mixed checkouts are supported as a detected state, not as an effective unified build
  model.
- Module records are de-duplicated by normalized `module_path`. When Maven and Gradle
  contribute the same module path, the module may record both build systems through
  additive Gradle fields, but emitted Java/Spring facts for that module must be produced
  once per supported source root.
- Maven-derived facts remain Maven-derived, Gradle-derived facts remain Gradle-derived,
  and neither build system is used as evidence for the other's build semantics.
- Mixed output must not infer cross-build dependencies, task ordering, publication
  relationships, generated-source availability, or runtime Spring behavior.

Gradle warning taxonomy:

- Gradle layout warnings live in `warnings.items` with `category: "gradle_module"`.
- Supported warning signals:
  - `"invalid_project_path"` for literal include paths that cannot normalize safely.
  - `"duplicate_project_path"` for repeated static includes that normalize to the same
    module path after the first declaration.
  - `"missing_project_directory"` for a valid static include whose default project
    directory is absent.
  - `"unsupported_module"` for a valid Gradle project directory with no supported Java
    production, test, or resource roots.
  - `"unsupported_dynamic_include"` for directly visible include declarations that are
    not in the supported literal subset.
  - `"unsupported_project_dir_mapping"` for directly visible custom project-directory
    mapping that v1.1 does not analyze.
  - `"source_sets_not_analyzed"` for directly visible `sourceSets` declarations whose
    custom roots are intentionally not emitted in v1.1.
- Warning IDs begin with `warning:gradle_module:<signal>:` and use normalized module
  paths or deterministic `decl:<zero-padded-ordinal>` discriminators.
- Mixed build-system caution warnings, when emitted, use `category: "build_system"` and
  `signal: "mixed_build_system_detected"`.
- Gradle warnings are not endpoint, component, entity, test, dependency, plugin, task,
  generated-source, or runtime facts.

Gradle diagnostics:

- Gradle build-file cap and read-skip conditions are scan diagnostics, not evidence.
- Gradle diagnostic codes include `"gradle_build_file_bytes_cap_exceeded"` and
  `"gradle_build_file_read_skipped"` with `severity: "warning"`,
  `category: "gradle"`, `path` set to the normalized repository-relative Gradle file
  path when safe, and `count` set to the applicable byte cap when relevant.
- A skipped Gradle build file emits no `build_file` evidence from skipped content and
  cannot support Gradle module, source-root, or warning facts that require that content.
- Diagnostic messages must follow the existing bounded diagnostic rules: no raw build
  script bodies, dependency blocks, plugin configuration, credentials, tokens, local
  absolute paths, stack traces, or generated output contents.

Gradle guide wording:

- `agent-guide.md` should render Gradle project layout as local build-file and
  conventional Java root observations only.
- Build/config Gradle guidance should use wording such as `Source-visible Gradle build
  files` and `Static Gradle settings include`, and must not claim effective Gradle
  configuration, plugin behavior, dependency graphs, task execution, generated-source
  availability, or Kotlin source analysis.
- Known limits should explicitly say that Gradle execution, dynamic buildscript
  evaluation, dependency resolution, plugin resolution, task graphs, custom
  `sourceSets`, `projectDir` remapping, included builds, and Kotlin source analysis are
  not analyzed in the v1.1 boundary.

v1.1 validation requirements:

- Focused fixtures for single-project Gradle layouts, simple multi-project Gradle
  layouts, simple `settings.gradle.kts` include declarations, unsupported dynamic
  includes, missing project directories, duplicate project paths, mixed Maven/Gradle
  roots, and visible `sourceSets` declarations that remain not analyzed.
- Golden output coverage for Gradle-only and mixed output shape, evidence ID
  resolution, warning IDs, diagnostics, deterministic sorting, and guide wording.
- Maven regression coverage proving pure Maven v1.0 output and evidence semantics are
  preserved unless a later explicit contract update says otherwise.
- Packaged CLI evaluation on pinned Gradle Java/Spring projects before release, plus
  selected Maven regression scans.

### v1.2 Generated Source And Codegen Maturity Contract

This section defines the public v1.2 generated-source/codegen boundary. The implemented
v1.2 slice is a warning/config/metadata-only compatibility expansion; it does not add
generated-source content scanning or generated-source facts.

The v1.2 policy decision is warning/config/metadata-only:

- Generated-source content scanning remains off by default.
- v1.2 does not introduce an opt-in generated-source content scan mode.
- `features.generated_sources` remains a reserved disabled content-scan toggle in the
  root-local YAML config. `false` is valid; attempts to set it to `true` remain invalid
  config until a later explicit generated-source content scan contract changes that.
- Generator execution, Maven lifecycle execution, Gradle task execution, dependency,
  plugin, repository, or task resolution, generated-source graph reconstruction, and
  runtime API freshness checks are out of scope.
- Generated-source roots, generator declarations, annotation-processor signals,
  build-helper add-source signals, OpenAPI declared operations, human-authored Java
  facts, inferred relations, uncertain signals, document-backed hints, and scan
  metadata must remain distinct.

Schema and compatibility decisions:

- v1.2 generated-source metadata is an additive `schema_version: "1.0"`
  compatibility expansion, not a schema marker migration.
- The same four output files remain under `.project-memory/`.
- Existing source-visible Maven and Gradle facts, warning IDs, evidence ID conventions,
  evidence field semantics, Markdown caution boundaries, and disabled-mode analyzer
  behavior must not be removed, renamed, or reinterpreted.
- Consumers that understand the v1.0/v1.1 shape should ignore unknown additive v1.2
  metadata fields when practical.
- Generated-source roots are not production `source_roots`, `test_roots`, or resource
  roots in the v1.2 boundary. They do not feed Spring, JPA, test, component, endpoint,
  OpenAPI, document, or quality analyzers.

High-level `project-map.json` shape:

```json
{
  "schema_version": "1.0",
  "generated_sources": {
    "analysis_status": "analyzed",
    "policy": {
      "content_scan": "disabled",
      "content_scan_default": false,
      "content_scan_configurable": false,
      "content_status": "not_scanned"
    },
    "roots": {
      "analysis_status": "analyzed",
      "items": [
        {
          "id": "generated_source_root:module:services/orders:path:services/orders/target/generated-sources/openapi",
          "module_id": "module:services/orders",
          "path": "services/orders/target/generated-sources/openapi",
          "root_kind": "maven_generated_sources",
          "scope": "main",
          "source_origin": "metadata_only",
          "content_status": "not_scanned",
          "detection_basis": "known_generated_root_path",
          "related_warning_ids": [
            "warning:generated_source:generated_source_root_path_detected:module:services/orders:path:services/orders/target/generated-sources/openapi"
          ],
          "evidence_ids": [
            "ev:services/orders/target/generated-sources/openapi:unknown:path_signal:generated_source_root_path_detected"
          ]
        }
      ]
    },
    "generator_signals": {
      "analysis_status": "analyzed",
      "warning_ids": [],
      "maven_plugin_ids": []
    }
  }
}
```

Origin and claim-separation labels:

- `human_source` means a fact extracted from supported human-authored Java source roots
  such as `src/main/java` or supported test roots. Existing facts keep their current
  field shape and are interpreted as human-source facts by section and evidence type;
  v1.2 does not require adding a global origin field to every existing fact.
- `generated_source` is reserved for a future generated-source content scan. v1.2
  metadata-only output must not emit endpoint, component, entity, test, Spring surface,
  quality, or relation facts with this origin.
- `spec_backed` means local OpenAPI/Swagger spec facts and declared operation facts
  backed by `api_spec` evidence.
- `document_backed` means local Markdown document inventory, structure references, and
  document-side reconciliation observations backed by `document` evidence.
- `inferred` and `uncertain` retain their existing support-type, relation-status,
  confidence, and uncertainty semantics.
- `metadata_only` means a scan/config/path/build observation that orients the reader but
  does not prove source content or runtime behavior. v1.2 generated-source root
  inventory uses this origin with `content_status: "not_scanned"`.

Generated-source root inventory rules:

- `generated_sources.analysis_status` is `"analyzed"` when generated-source metadata
  discovery runs. It is `"not_detected"` when no supported build, module, source-root,
  or generated-source path input is available.
- `generated_sources.policy` records the effective v1.2 generated-source policy. It is
  scan metadata, not evidence. It must not serialize raw config values, command output,
  local absolute paths, source excerpts, generated source contents, or generated output
  contents.
- `generated_sources.roots.items[]` contains path inventory records for bounded known
  generated-source root paths only. A root item proves path presence, not generated Java
  types or generated API operations.
- Supported `root_kind` values in the v1.2 metadata-only boundary are
  `"maven_generated_sources"`, `"maven_generated_test_sources"`,
  `"gradle_generated_sources"`, and `"gradle_generated_test_sources"`.
- `scope` is `"main"` for production-like generated-source roots and `"test"` for
  generated-test roots.
- `source_origin` is `"metadata_only"` for all v1.2 root inventory items.
- `content_status` is `"not_scanned"` for all v1.2 root inventory items.
- `detection_basis` is `"known_generated_root_path"` for current root inventory items.
  It must not preserve arbitrary plugin configuration values or raw build-script
  expressions.
- `related_warning_ids` references existing generated-source warnings when a warning is
  emitted for the same path or generator declaration. The references must resolve to
  `warnings.items`.
- Root `evidence_ids` reference `path_signal` evidence for root path observations.
  Generator declaration observations remain represented by existing warning references
  and Maven plugin IDs backed by their existing `build_file` evidence. Referenced IDs
  must resolve to their owning output collections.

Path policy:

- Emitted generated-source paths must be normalized repository-relative paths. They must
  not be absolute, start with `./`, contain `.` or `..` path segments after
  normalization, use backslash separators, or escape the scan root.
- Generated-source metadata discovery must not follow symlinked generated-root
  directories or symlinked files. A symlink path is not a generated-source root fact in
  v1.2.
- Generated-source metadata discovery must not read files under generated-source roots
  or materialize generated source contents.
- Known Maven candidate families are bounded to module-owned `target/generated-sources`
  and `target/generated-test-sources` roots and deterministic immediate child
  directories under those families.
- Known Gradle candidate families are bounded to module-owned `build/generated/sources`
  and `build/generated/source` roots and deterministic immediate child directories
  under those families.
- Arbitrary build-helper, plugin, task, or custom `sourceSets` paths are not interpreted
  as generated-source roots in v1.2 unless they also match a supported known path
  family. Directly visible declarations may still support generator warnings or
  metadata-only generator signals.
- Generated-source root candidate selection is bounded to 256 candidates before fact,
  evidence, JSON, or Markdown materialization.

Diagnostics:

- Reaching the generated-source root candidate cap emits a bounded non-fatal
  `scan.diagnostics.items[]` entry with `code:
  "generated_source_root_count_cap_reached"`, `severity: "warning"`,
  `category: "generated_sources"`, `path: null`, and `count: 256`.
- Skipping an unsafe generated-source path emits a bounded non-fatal diagnostic
  with `code: "generated_source_root_skipped_unsafe_path"`, `severity: "warning"`,
  `category: "generated_sources"`, and `path` set only when a safe normalized
  repository-relative path can be recorded.
- Normal disabled content scanning is policy, not an error. It should be represented by
  `generated_sources.policy` and cautious guide wording rather than by a warning
  diagnostic.
- Attempts to enable generated-source content scanning through reserved config remain
  invalid config errors and should fail before output generation.
- Diagnostic messages must follow the existing bounded diagnostic rules: no raw config
  values, raw path patterns, source excerpts, generated source contents, generated
  output contents, stack traces, local absolute paths, credentials, or tokens.

Markdown behavior:

- `endpoints.md` must keep source-visible Spring MVC endpoints, interface-declared
  Spring MVC endpoints, declared OpenAPI operations, generated-source API signals,
  repository-rest warnings, and hidden HTTP warnings in separate sections.
- v1.2 generated-source metadata must not be rendered as endpoint, API operation,
  implementation coverage, generated API, or runtime handler mapping facts.
- `agent-guide.md` may add a concise `Generated Source And Codegen Orientation` section
  generated only from structured `generated_sources` metadata, existing build/plugin
  facts, warnings, and resolving evidence.
- Guide wording must use `metadata only`, `warning`, `not scanned`, or equivalent
  cautious labels for generated roots and generator declarations.
- Known limits should state that generated-source content scanning, generator
  execution, generated API reconstruction, runtime freshness checks, dependency or task
  resolution, and custom Gradle generated-source graph reconstruction are not performed.

Validation requirements:

- Focused tests and goldens for generated-root inventory, config-disabled behavior,
  invalid reserved enables, warning references, diagnostics, deterministic sorting, and
  guide wording.
- Maven regression coverage proving existing source-visible output and evidence
  semantics remain stable when generated-source content scanning is disabled.
- Gradle regression coverage proving v1.1 static layout output and evidence semantics
  remain stable and that Gradle generated-source metadata does not imply task or
  generated-source graph reconstruction.
- Packaged CLI evaluation on representative generated-source/codegen projects before
  release, plus selected Maven and Gradle regression scans.
- Risk-based security review is required before release for any implementation that
  changes config handling, path discovery, filesystem traversal, diagnostics, generated
  output, evidence references, or Markdown rendering.

Stop conditions for implementation:

- Generated-source content scanning becomes default, implicit, or launchable through a
  plain boolean enable.
- Generated-source metadata cannot stay distinct from human-authored source facts,
  spec-backed operation facts, document-backed hints, inferred relations, uncertain
  signals, and scan metadata.
- Evidence semantics would allow generator declarations, path-only signals, config
  metadata, documents, specs, generated Markdown, or LLM output to masquerade as
  generated source content evidence.
- Path normalization, symlink, containment, oversized-tree, unreadable-path, candidate
  cap, evidence cap, or Markdown cap behavior is unclear.
- Disabled-mode Maven or Gradle output semantics cannot be preserved.

### v1.3 Agent Output Profiles Contract

This section defines the current v1.3 development agent profile artifact layer and
profile content boundary. The implemented layer supports deterministic, opt-in profile
selection, writes a generated-profile manifest, and writes selected profile-specific
Markdown presentations generated from existing structured facts and evidence
references.

The v1.3 policy decision is deterministic, opt-in profile presentation:

- Profiles are derived presentations over existing structured project facts and existing
  evidence references. They do not create project facts or evidence.
- Supported canonical profile names are `codex`, `claude`, `cursor`, and `generic`.
  The selector value `all` may request every supported profile. No other aliases are
  part of the initial v1.3 contract.
- Profile generation is opt-in. A normal scan with no profile selector keeps the current
  default generated output set and does not create profile artifacts.
- The implemented CLI surface is a repeatable
  `scan <path> --agent-profile <profile>` selector. `--agent-profile all` selects every
  supported profile. Unknown profile names are usage errors.
- Duplicate profile selectors are idempotent: each canonical profile is generated at
  most once and appears at most once in `generated_profiles[]`.
- Root-local YAML config does not select agent profiles in the initial v1.3 design. A
  later config-based profile selector would require a separate config and output
  contract update.
- Profiles must not call LLMs, external services, editors, connectors, local agent
  runtimes, build tools, or generated-source scanners.
- Profiles must not automatically modify root repository instruction/config files such
  as `AGENTS.md`, `CLAUDE.md`, Cursor rule files, IDE settings, source files, docs, or
  config files. Any repository-file-writing feature would require a separate explicit
  opt-in design.

Schema and compatibility decisions:

- v1.3 profile artifacts are an additive `schema_version: "1.0"` compatibility
  expansion, not a schema marker migration.
- Existing default output behavior remains the same when no profile selector is used:
  `.project-memory/project-map.json`, `.project-memory/evidence-index.jsonl`,
  `.project-memory/endpoints.md`, and `.project-memory/agent-guide.md`.
- Profile generation does not add fields to `project-map.json`, does not change
  `evidence-index.jsonl`, and does not reinterpret existing evidence IDs, evidence
  types, confidence labels, excerpt boundaries, or path rules.
- `agent-guide.md` remains the generic deterministic orientation guide. Profile
  artifacts are additional opt-in files, not a replacement mode for `agent-guide.md`.
- Consumers that understand the v1.0 through v1.2 output shape should ignore the
  optional `.project-memory/agent-profiles/` directory when they do not use profile
  outputs.

Current profile artifact layout when at least one profile is selected:

```text
.project-memory/
  project-map.json
  evidence-index.jsonl
  endpoints.md
  agent-guide.md
  agent-profiles/
    manifest.json
    codex.md
    claude.md
    cursor.md
    generic.md
```

Only selected profile Markdown files are written. For example, selecting only `codex`
writes `agent-profiles/manifest.json` and `agent-profiles/codex.md`, not the other
profile Markdown files. Selecting `all` writes all supported profile Markdown files.

Profile artifact rules:

- Profile paths are fixed `.project-memory`-relative paths under `agent-profiles/`.
  They must not be configurable to arbitrary repository paths in the initial v1.3
  design.
- Profile artifact paths must remain normalized slash-separated relative paths. They
  must not be absolute, start with `./`, contain `.` or `..` path segments, use
  backslash separators, or escape `.project-memory/agent-profiles/`.
- Profile generation should run only when the normal scan has enough supported input to
  write the base contract output files. Unsupported directories that only prepare
  `.project-memory/` should not create orphan profile files.
- Existing unrelated contents inside `.project-memory/` remain preserved. Profile
  generation owns only the documented generated profile artifact paths.
- Profile Markdown is deterministic human-readable presentation, not a stable parser
  interface. Downstream automation should use `manifest.json` and documented file names
  to detect generated profiles, not parse profile Markdown.

Current `agent-profiles/manifest.json` shape:

```json
{
  "manifest_version": "1.0",
  "project_map_schema_version": "1.0",
  "source_artifacts": [
    "project-map.json",
    "evidence-index.jsonl"
  ],
  "generated_profiles": [
    {
      "name": "codex",
      "artifact_path": "agent-profiles/codex.md",
      "content_kind": "markdown_presentation",
      "evidence_policy": "references_existing_evidence_only"
    }
  ]
}
```

Manifest rules:

- `manifest_version` is `"1.0"` for the initial profile manifest contract.
- `project_map_schema_version` records the `project-map.json` schema marker that the
  profile files were generated from. It does not define a new project-map schema.
- `source_artifacts` lists the base `.project-memory` artifacts used to generate
  profiles. The initial contract uses `project-map.json` and `evidence-index.jsonl`, or
  the same in-memory facts and evidence records used to write those files.
- `generated_profiles[]` contains one item per written profile Markdown file, sorted in
  canonical profile order: `codex`, `claude`, `cursor`, then `generic`.
- `artifact_path` is `.project-memory`-relative and must point under `agent-profiles/`.
- `content_kind` is `"markdown_presentation"` for the initial profile files.
- `evidence_policy` is `"references_existing_evidence_only"` because profile files do
  not create evidence records.
- The manifest is generated-output metadata only. It must not include source excerpts,
  document bodies, raw config values, local absolute paths, command output, credentials,
  tokens, environment values, generated-source contents, or downstream agent output.

Profile Markdown content boundary:

- Profile Markdown files include profile-specific operating notes, generated artifact
  reading order, source artifact orientation, a compact project snapshot,
  evidence-visible fact pointers, a fact-boundary map, and profile-specific checklist
  guidance.
- Common content may include a profile-specific reading order for generated
  project-memory artifacts, concise evidence-visible orientation, known limits, and
  practical inspection guidance.
- Profile-specific differences are limited to wording, heading structure, reading order,
  and copyable snippets tailored to the selected agent. They must not alter underlying
  fact meanings or evidence requirements.
- Profile snippets are copyable text only. They must not be described as automatically
  applied edits to repository instruction files.
- Profiles must keep source-visible endpoint facts, interface-declared endpoint facts,
  OpenAPI declared operations, generated-source metadata-only observations, Spring/JPA
  facts, tests inventory, quality planning hints, local-document hints, warnings,
  inferred relations, uncertain signals, and not-analyzed areas visibly distinct.
- Profiles may cap long evidence-reference lists for readability, but the cap must point
  readers back to `evidence-index.jsonl` for the complete evidence records and must not
  remove evidence IDs from the base JSON outputs.
- Profile Markdown must follow the existing Markdown-safe presentation policy for
  source-derived inline text, paths, identifiers, evidence references, and messages. The
  v1.3 design does not introduce new Markdown escaping semantics.
- Profiles must not serialize source bodies, local document bodies, config contents,
  arbitrary build-script bodies, generated-source contents, raw command transcripts,
  stack traces, local absolute paths, credentials, tokens, or secret-looking values.
- Profiles must not invent architecture layers, summarize code or documents with AI,
  create implementation tasks, claim runtime behavior, claim test coverage or CI state,
  claim security correctness or vulnerabilities, or treat generated Markdown as
  evidence.

Validation requirements:

- Focused CLI tests for no-profile default behavior, single-profile selection,
  repeated-profile selection, `all`, duplicate selectors, unsupported profile names, and
  unchanged exit-code behavior for unrelated scan errors.
- Focused output-path tests proving profile files are written only under
  `.project-memory/agent-profiles/`, cannot escape that directory, and do not overwrite
  repository root instruction/config files.
- Golden outputs for `manifest.json` and every supported profile Markdown file.
- Regression goldens proving `project-map.json`, `project-graph.json`,
  `evidence-index.jsonl`, `endpoints.md`, and `agent-guide.md` remain stable when no
  profile is requested.
- Evidence-reference integrity checks for profile Markdown, including resolving
  referenced evidence IDs and preserving claim separation.
- Repeated-output digest checks on representative profile-generation scans before
  release, plus evaluation for concise usefulness without relying on chat-only
  impressions.
- Risk-based review is required before release for any implementation that changes CLI
  selection, generated output paths, filesystem writes, Markdown rendering, evidence
  reference rendering, config behavior, or repository-file-writing surfaces.

Stop conditions for implementation:

- Profile generation requires LLM output, AI summarization, external services, editor
  integration, connectors, local agent runtime calls, network/auth, credentials, or
  telemetry.
- Profile artifacts create project facts, evidence records, generated-source content
  facts, runtime claims, implementation tasks, security claims, or source/document
  summaries outside the existing deterministic fact model.
- Profile wording cannot keep extracted, inferred, uncertain, document-backed,
  spec-backed, generated-source metadata-only, warning, and not-analyzed claims distinct.
- Default behavior would create profile artifacts or modify repository root
  instruction/config files without an explicit profile selector.
- Profile output paths, overwrite ownership, `.project-memory/` containment, symlink or
  hardlink handling, evidence-reference rendering, or Markdown-safe rendering are
  unclear.

### v1.4 Incremental Cache Contract

This section defines the v1.4 incremental cache boundary. The current implementation
supports opt-in metadata-only cache-assisted reuse with `scan <path> --incremental`:
it reuses the existing generated output set only after strict whole-output-set cache
validation, and otherwise runs the normal full analysis path and writes cache metadata
after successful output generation.

The v1.4 policy decision is optional metadata-only cache-assisted reuse:

- Full scan remains the compatibility baseline. A normal `scan <path>` without the
  incremental selector runs full analysis and does not require, read, write, delete, or
  trust cache state.
- The public selector is `scan <path> --incremental`. Root-local YAML config does not
  enable incremental scans in the initial v1.4 design, and there is no separate cache
  command, clean command, daemon, background service, remote cache, telemetry, network
  access, or connector behavior.
- The first incremental run for a repository state is a cache miss and warm-up: it runs
  normal full analysis, writes the normal generated output set, then writes cache
  metadata only after successful output generation.
- Later incremental runs may skip full analysis only when cache schema, tool version,
  selected CLI options, selected config, selected agent profiles, input fingerprints,
  and existing generated output fingerprints all match the current repository state.
- In this v2 local structured import slice, `scan <path> --incremental` with an
  explicitly enabled adapter runs the normal full analysis path and skips cache metadata
  refresh. Adapter-enabled cache reuse is postponed until cache input/output contracts
  explicitly include adapter import inputs and `source-registry.json` without
  serializing configured import paths, raw adapter config values, raw export contents,
  or raw record bodies.
- The initial v1.4 reuse granularity is the whole generated output set for an unchanged
  repository state. Partial per-module, per-analyzer, per-source-file, or per-section
  fact reuse is out of scope for the initial v1.4 contract.
- Any changed, added, deleted, renamed, unreadable, unsafe, unsupported, stale,
  corrupted, schema-mismatched, option-mismatched, config-mismatched,
  profile-mismatched, tool-version-mismatched, or otherwise unclear cache state fails
  closed to normal full analysis. A successful full analysis may refresh cache metadata.

Schema and compatibility decisions:

- v1.4 incremental cache support is an additive `schema_version: "1.0"`
  compatibility expansion, not a project-map schema marker migration.
- The same base generated files remain under `.project-memory/`:
  `project-map.json`, `evidence-index.jsonl`, `endpoints.md`, and `agent-guide.md`.
- Optional profile artifacts remain governed by the v1.3 profile contract under
  `.project-memory/agent-profiles/`.
- `project-map.json` does not gain cache-hit, cache-miss, timing, output-digest, or
  incremental-reuse fields in the v1.4 design. This keeps byte-for-byte output
  parity possible between full scan output and a validated incremental cache hit.
- `evidence-index.jsonl` field shape and evidence semantics are unchanged. Cache files,
  cache hits or misses, fingerprints, invalidation decisions, output digests, generated
  Markdown, diagnostics, timing observations, and LLM output are not evidence.
- Incremental cache metadata is implementation-owned generated metadata for
  `agent-project-memory`; downstream consumers should keep using `project-map.json` and
  `evidence-index.jsonl` as the stable machine-readable project-memory surface.

Cache artifact layout:

```text
.project-memory/
  cache/
    v1/
      manifest.json
      inputs.jsonl
      outputs.jsonl
```

Cache path and ownership rules:

- The generator owns only `.project-memory/cache/v1/manifest.json`,
  `.project-memory/cache/v1/inputs.jsonl`, and
  `.project-memory/cache/v1/outputs.jsonl` for the initial v1.4 cache contract.
- Implementations may use temporary files under `.project-memory/cache/v1/` while
  writing those three owned files, but temporary files must not become part of the
  stable cache contract and should be removed after successful replacement or failure
  cleanup.
- Cache paths are fixed. They must not be configurable to arbitrary repository paths,
  absolute paths, paths outside `.project-memory/`, or paths outside the scanned
  repository root.
- Cache paths must remain normalized slash-separated `.project-memory`-relative paths.
  They must not be absolute, start with `./`, contain `.` or `..` path segments after
  normalization, use backslash separators, or escape `.project-memory/cache/v1/`.
- Cache files and parent directories must not be followed through symlinks. Unsafe,
  symlinked, escaping, or multi-link cache targets must not be read as trusted cache
  state and must not be overwritten as cache files. Incremental mode should fall back
  to full analysis and skip cache refresh until the unsafe cache path is removed or made
  safe.
- Existing unrelated contents inside `.project-memory/` remain preserved. Unknown files
  under `.project-memory/cache/` are ignored unless a later explicit cache cleanup
  contract defines removal behavior.
- Non-incremental full scans ignore existing cache state. They do not update cache
  metadata unless a later explicit contract changes the default behavior.

`cache/v1/manifest.json` shape:

```json
{
  "cache_schema_version": "1.0",
  "project_map_schema_version": "1.0",
  "cache_kind": "incremental_scan_metadata",
  "reuse_granularity": "whole_output_set",
  "fingerprint_algorithm": "sha256",
  "input_fingerprints_path": "cache/v1/inputs.jsonl",
  "output_fingerprints_path": "cache/v1/outputs.jsonl",
  "tool_version": "1.4.0",
  "option_fingerprint": "sha256:...",
  "config_fingerprint": {
    "status": "not_detected",
    "path": null,
    "sha256": null
  },
  "selected_profiles": [],
  "evidence_policy": "cache_is_not_evidence",
  "raw_values_serialized": false
}
```

Manifest rules:

- `cache_schema_version` is `"1.0"` for the initial cache metadata contract. Missing,
  unknown, older, or newer cache schema versions are cache misses.
- `project_map_schema_version` records the `project-map.json` schema marker expected by
  the cached metadata. It does not define a new project-map schema.
- `cache_kind` is `"incremental_scan_metadata"` for the initial cache files.
- `reuse_granularity` is `"whole_output_set"` for the initial v1.4 design.
- `fingerprint_algorithm` is `"sha256"`. Content-affecting file fingerprints must be
  SHA-256 hashes over the exact file bytes read through the existing no-follow,
  bounded, repository-contained input policy.
- `input_fingerprints_path` and `output_fingerprints_path` are `.project-memory`-
  relative paths to the two cache JSONL files.
- `tool_version` records the CLI version used to write the cache metadata. A mismatch is
  a cache miss.
- `option_fingerprint` is a SHA-256 digest over a canonical representation of
  cache-relevant CLI option state. It must not serialize raw command lines, local
  absolute paths, raw config values, raw include/exclude patterns, credentials, tokens,
  or environment values.
- `config_fingerprint` records only redacted config matching metadata: selected config
  status, safe normalized repository-relative config path when one is selected, and a
  SHA-256 hash of the selected config file bytes when applicable. It must not serialize
  config contents, YAML nodes, raw path rules, environment values, decrypted values,
  credentials, tokens, or secret-looking values.
- `selected_profiles` contains the canonical selected profile names after duplicate
  selector normalization, sorted in the v1.3 canonical order. It is empty when no
  profile artifacts are selected.
- `raw_values_serialized` must be `false`.

`cache/v1/inputs.jsonl` shape:

```json
{"cache_schema_version":"1.0","path":"pom.xml","input_kind":"maven_pom","content_sha256":"sha256:...","size_bytes":123}
{"cache_schema_version":"1.0","path":"src/main/java","input_kind":"java_source_root_path","content_sha256":null,"size_bytes":null}
{"cache_schema_version":"1.0","path":"src/main/java/com/example/App.java","input_kind":"java_source","content_sha256":"sha256:...","size_bytes":4567}
{"cache_schema_version":"1.0","path":"target/generated-sources/openapi","input_kind":"generated_source_root_path","content_sha256":null,"size_bytes":null}
{"cache_schema_version":"1.0","path":"target/generated-sources/unsafe-link","input_kind":"generated_source_root_unsafe_path","content_sha256":null,"size_bytes":null}
```

Input fingerprint rules:

- `path` is a normalized repository-relative path. It must not be absolute, start with
  `./`, contain `.` or `..` path segments after normalization, use backslash
  separators, or escape the scan root.
- `input_kind` identifies the cache-relevant input family, such as `maven_pom`,
  `gradle_build_file`, `java_source_root_path`, `java_test_root_path`,
  `resource_root_path`, `java_source`, `java_test_source`, `resource_config_file`,
  `openapi_spec`, `local_markdown_document`, `scan_config`, or
  `generated_source_root_path`. Unsafe or symlinked generated-source child path
  observations may use `generated_source_root_unsafe_path`.
- Content-affecting regular-file inputs use `content_sha256` and `size_bytes`.
  Directory or path-presence observations that are already metadata-only, such as
  standard source/test/resource root presence, generated-source root path presence, and
  unsafe generated-source child path presence, use `content_sha256: null` and
  `size_bytes: null`.
- File modification times are not part of the serialized cache contract and must not be
  the sole authority for cache hits.
- The fingerprinted input set must include every source, build, spec, local Markdown,
  supported config, selected scan config, standard source/test/resource root directory
  presence, generated-source path observation, and other local input class that can
  affect the generated output set or bounded diagnostics under the selected options.
- Generated-source root fingerprints, including unsafe generated-source child path
  markers, remain path-presence metadata only. The cache contract must not read or
  fingerprint files under generated-source roots.

`cache/v1/outputs.jsonl` shape:

```json
{"cache_schema_version":"1.0","path":"project-map.json","output_kind":"project_map","content_sha256":"sha256:...","size_bytes":12345}
{"cache_schema_version":"1.0","path":"project-graph.json","output_kind":"project_graph","content_sha256":"sha256:...","size_bytes":23456}
{"cache_schema_version":"1.0","path":"agent-profiles/codex.md","output_kind":"agent_profile_markdown","content_sha256":"sha256:...","size_bytes":6789}
```

Output fingerprint rules:

- `path` is `.project-memory`-relative and must point only to artifacts generated for
  the selected scan option set.
- Base output fingerprints cover `project-map.json`, `project-graph.json`,
  `evidence-index.jsonl`, `endpoints.md`, and `agent-guide.md`.
- `source-registry.json` is not part of the v1.4 cache output fingerprint set in this
  slice because adapter-enabled scans skip cache metadata refresh.
- When agent profiles are selected, output fingerprints also cover
  `agent-profiles/manifest.json` and the selected profile Markdown files.
- Unselected profile files are not part of the selected generated output set. They
  remain governed by the existing `.project-memory/` preservation behavior and must not
  influence cache hits for no-profile scans.
- Cache hit validation must verify that every selected output file exists, is safe to
  read, and has the expected SHA-256 digest and size before skipping full analysis.

Invalidation and fallback rules:

- Cache validation must compare exact input fingerprint sets, exact selected output
  fingerprint sets, cache schema, project-map schema marker, tool version, cache
  relevant option fingerprint, selected config fingerprint, selected profile set, and
  path-policy assumptions.
- Cache validation must fail closed to full analysis on missing files, extra or missing
  fingerprint records, duplicate fingerprint keys, invalid JSON, unknown fields whose
  semantics are required for validation, hash mismatch, size mismatch, unsafe paths,
  unreadable cache files, unreadable current inputs, path containment uncertainty,
  symlink or hardlink uncertainty, candidate cap uncertainty, or any mismatch that the
  implementation cannot prove safe.
- A cache miss must not weaken source analysis, diagnostics, evidence resolution,
  Markdown rendering, profile generation, or output writing. It should run the same full
  analysis path as a non-incremental scan for the same selected options.
- A scan that fails before normal output generation must not refresh cache metadata.
- A cache hit must not rewrite generated project-memory outputs unless a later explicit
  contract defines safe rewrite behavior. It may report a bounded CLI cache-hit summary.
- Cache miss, invalidation, corruption, or unsafe-cache conditions may be reported in
  concise CLI output, but generated `project-map.json`, `project-graph.json`,
  `evidence-index.jsonl`, `endpoints.md`, `agent-guide.md`, and profile Markdown must
  not serialize cache status or timing data.

Sensitive-data policy:

- Cache metadata must not serialize source bodies, local document bodies, config
  contents, raw build-script bodies, generated-source contents, generated Markdown
  bodies, raw command logs, raw stack traces, raw include/exclude patterns, environment
  variables, decrypted values, credentials, tokens, secret-looking values, local
  absolute paths, timing measurements, downstream agent output, or LLM output.
- Repository-relative paths, byte counts, SHA-256 digests, schema markers, tool version,
  canonical profile names, redacted option/config matching status, and safe
  `.project-memory`-relative cache/output paths are the maximum planned cache metadata
  surface.

CLI behavior:

- Unknown `--incremental` combinations or malformed incremental usage should remain
  usage errors with exit code `2`.
- Invalid config remains exit code `4`; output generation or write errors remain exit
  code `5`; unexpected internal errors remain exit code `1`.
- Cache miss, cache corruption, stale cache, unsafe cache path, or output digest
  mismatch should not be fatal by itself when full analysis can proceed safely. The scan
  exits `0` if the fallback full analysis and output generation succeed.
- CLI cache summaries must be deterministic and bounded. They must not print timing
  measurements, local absolute paths, raw config values, raw command lines, source
  excerpts, document bodies, generated output contents, credentials, tokens, or
  secret-looking values.

Validation requirements:

- Focused tests for explicit incremental selection, normal no-incremental behavior,
  cache path containment, symlink and hardlink cache paths, cache schema mismatch,
  cache corruption, missing cache files, input fingerprint changes, file additions,
  edits, deletions and renames, config/option/profile mismatch, output digest mismatch,
  unsupported or unsafe paths, and unchanged-state cache hits.
- Regression tests proving non-incremental full scan output remains stable and does not
  depend on cache state.
- Full scan versus incremental scan parity checks over the same repository state and
  selected options, including selected profile artifacts where profiles are requested.
- Cache content checks proving cache files contain only the allowed metadata surface.
- Packaged CLI smoke for cache miss/warm-up, validated cache hit, stale cache fallback,
  no-profile scans, and selected profile scans.
- Risk-based review is required before release for implementation that changes cache
  files, path containment, filesystem handling, CLI/config behavior, output paths,
  output rendering, evidence references, or generated artifact ownership.

Stop conditions for implementation:

- Cache content would include source bodies, local document bodies, config contents,
  raw build-script bodies, generated-source contents, generated Markdown bodies, raw
  command logs, local absolute paths, credentials, tokens, secret-looking values, timing
  measurements, downstream agent output, or LLM output.
- Cache state could become evidence, replace source-backed evidence, suppress required
  evidence generation, or strengthen extracted, inferred, uncertain, document-backed,
  spec-backed, generated-source metadata-only, warning, or not-analyzed claims.
- Incremental output cannot be proven byte-for-byte equal to full scan output for the
  same repository state and selected options.
- Cache path ownership, containment, symlink/hardlink behavior, overwrite behavior,
  cleanup behavior, schema mismatch behavior, corruption handling, or invalidation is
  unclear.
- The implementation requires partial fact reuse, generated-source content scanning,
  build execution, network access, remote cache, daemon/background service, telemetry,
  connectors, repository chat, generic RAG, LLM calls in the core analyzer, automatic
  code modification, or release automation.

### v1.5 Lightweight Relation Graph Contract

This section defines the v1.5 lightweight relation graph boundary. Current development
builds emit the graph artifact for supported scans, including structural graph material
and conservative relation/status graph material inside the same documented boundary.

The v1.5 policy decision is a separate graph artifact:

- The graph artifact path is `.project-memory/project-graph.json`.
- The graph is not a top-level `project-map.json` section. `project-map.json` remains
  the source fact surface, and the initial graph expansion keeps
  `project-map.json` on `schema_version: "1.0"`.
- The graph artifact uses its own `graph_schema_version: "1.0"` marker.
- The graph is generated from existing deterministic facts, existing relation/status
  rows, existing evidence IDs, and existing document reconciliation hints. It must not
  add new analyzer families merely to populate graph nodes or edges.
- The graph is a navigation/index artifact over current project memory. It must not
  strengthen, reinterpret, or override the source facts in `project-map.json` or the
  evidence records in `evidence-index.jsonl`.
- The initial contract has no graph CLI selector and no root-local config selector.
  Supported scans that write the base project-memory files also write
  `project-graph.json`. Unsupported directories that only prepare
  `.project-memory/` should not create an orphan graph artifact.
- `agent-guide.md` and selected agent profile Markdown are not required to render graph
  content in the initial graph contract. The first graph surface is machine-readable
  JSON.

Graph artifact layout:

```text
.project-memory/
  project-map.json
  project-graph.json
  evidence-index.jsonl
  endpoints.md
  agent-guide.md
```

High-level `project-graph.json` shape:

```json
{
  "graph_schema_version": "1.0",
  "project_map_schema_version": "1.0",
  "graph_kind": "lightweight_relation_graph",
  "source_artifacts": [
    "project-map.json",
    "evidence-index.jsonl"
  ],
  "limits": {
    "max_nodes": 20000,
    "max_edges": 50000,
    "max_relation_statuses": 10000
  },
  "nodes": [],
  "edges": [],
  "relation_statuses": [],
  "warnings": []
}
```

Top-level rules:

- `graph_schema_version` is `"1.0"` for the initial graph artifact contract.
- `project_map_schema_version` records the `project-map.json` schema marker that the
  graph was generated from. It does not define a new project-map schema.
- `graph_kind` is `"lightweight_relation_graph"` for the initial graph artifact.
- `source_artifacts` lists the base artifacts used to generate or validate the graph.
  The initial contract uses `project-map.json` and `evidence-index.jsonl`, or the same
  in-memory facts and evidence records used to write those files.
- `nodes`, `edges`, `relation_statuses`, and `warnings` are emitted as arrays. Empty
  collections are emitted as empty arrays, not omitted.
- Graph warnings are bounded generated-output diagnostics for graph construction. They
  are not project evidence and must not appear in `evidence_ids`.

Node taxonomy:

- `module`: an emitted project module from `project.modules.items[]`.
- `package`: a Java package deterministically derived from emitted source-visible type
  facts. Package nodes are emitted only when at least one emitted type node belongs to
  the package.
- `type`: a source-visible Java class or interface already represented by existing
  endpoint, component, Spring application surface, JPA/domain, repository, or test
  facts.
- `endpoint`: an existing source-visible Spring MVC endpoint fact.
- `api_operation`: an existing spec-backed declared OpenAPI/Swagger operation fact.
- `entity`: an existing JPA entity fact.
- `embeddable`: an existing JPA embeddable fact.
- `repository`: an existing Spring repository signal fact.
- `test`: an emitted test class fact.
- `document`: an accepted local Markdown document fact.
- `document_heading`: an emitted local Markdown heading reference.
- `document_chunk`: an emitted bounded local Markdown chunk reference.
- `generated_source_root`: an existing generated-source metadata-only root row.
- `warning`: an existing warning row.
- `status`: an explicit not-analyzed or unsupported status row that can be represented
  without inventing a missing target relation.

Evidence records are not graph nodes in the initial v1.5 contract. Nodes and edges
reference existing evidence through `evidence_ids` so consumers can join to
`evidence-index.jsonl` without duplicating evidence records in the graph.

Node shape:

```json
{
  "id": "node:type:root:com.example.orders.OrderController",
  "kind": "type",
  "label": "OrderController",
  "claim_category": "extracted",
  "module_id": "root",
  "source_ref": {
    "artifact": "project-map.json",
    "section": "components.items",
    "id": "component:com.example.orders.OrderController"
  },
  "evidence_ids": []
}
```

Node rules:

- `id` is stable within a generated graph and uses `node:<kind>:<node_key>`.
- `kind` is one of the documented node kinds.
- `label` is deterministic display text derived from the source fact, such as a simple
  class name, path, HTTP method/path summary, operation ID, warning signal, or status
  label. It must not serialize source bodies, document bodies, config contents,
  generated-source contents, raw command output, local absolute paths, credentials,
  tokens, or secret-looking values.
- `claim_category` is one of `extracted`, `inferred`, `uncertain`, `document_backed`,
  `spec_backed`, `metadata_only`, `warning`, `not_analyzed`, or `structural`.
- `module_id` is the owning module ID when known and `null` otherwise.
- `source_ref` identifies the source artifact and section or fact ID used to create the
  node. It is a graph navigation reference, not evidence.
- `evidence_ids` contains existing evidence IDs from the source fact when available.
  Nodes derived only from project-map structure may have an empty evidence list.

Edge taxonomy:

- `owns`: structural ownership or containment, such as module-to-package,
  module-to-fact, package-to-type, document-to-heading, or document-to-chunk.
- `declares`: declaration relationships already visible in existing facts, such as a
  type declaring an endpoint, repository signal, entity fact, embeddable fact, or test
  class structure.
- `repository_entity`: an existing repository/entity relation or relation status from
  the current Spring Data/JPA slice.
- `tested_subject`: an existing tested-subject relation or relation status from the
  current tests inventory.
- `document_reference`: an existing document reconciliation uncertain-reference hint.
- `api_source_relation`: reserved for a future explicitly designed endpoint/spec
  relation. The initial graph contract must not emit this edge type.

The current graph emits deterministic nodes, direct/structural `owns` and `declares`
edges, conservative inferred `repository_entity` and `tested_subject` edges from
existing relation rows, and status-only relation rows for unsupported, ambiguous,
not-detected, not-analyzed, uncertain, or no-target relation rows. `document_reference`
edges are emitted only when an existing reconciliation row has both a document-side node
and a source-fact node; current no-target reconciliation rows remain in
`relation_statuses[]`. The reserved `api_source_relation` edge type remains unimplemented.

Evidence references are carried by `evidence_ids`, not by `references_evidence` edges.
The initial graph contract has no `references_evidence` edge type because evidence
records are not graph nodes.

Edge shape:

```json
{
  "id": "edge:owns:node:module:root:node:type:root:com.example.orders.OrderController",
  "type": "owns",
  "source_id": "node:module:root",
  "target_id": "node:type:root:com.example.orders.OrderController",
  "claim_category": "structural",
  "relation_status": "derived",
  "support_type": "project_map_derivation",
  "confidence": "high",
  "uncertainty": null,
  "relation_attributes": {},
  "derivation": {
    "kind": "project_map_field",
    "artifact": "project-map.json",
    "section": "components.items",
    "fields": [
      "module_id",
      "class_name"
    ]
  },
  "evidence_ids": []
}
```

Edge rules:

- `id` is stable within a generated graph and uses
  `edge:<type>:<source_id>:<target_id>` plus a deterministic qualifier when needed.
- `type` is one of the documented edge types.
- `source_id` and `target_id` must resolve to emitted node IDs. Edges with missing
  endpoints are not emitted.
- `claim_category` follows the same category set as nodes and must preserve the
  underlying source-fact meaning.
- `relation_status`, `support_type`, `confidence`, and `uncertainty` preserve existing
  relation/status values when the edge is derived from repository/entity,
  tested-subject, or document reconciliation rows. Structural edges use
  `relation_status: "derived"` and `support_type: "project_map_derivation"`.
- `relation_attributes` is a deterministic string map for bounded relation-row fields
  that do not change the graph edge taxonomy. It is `{}` for structural edges. For
  relation-backed edges or status rows, it may preserve existing row fields such as
  `relation_type`, `candidate_reference`, `target_class_name`, `target_module_id`,
  `target_entity_id`, `generic_type`, `signal`, `document_id`, `document_chunk_id`,
  `source_fact_kind`, `source_fact_id`, `subject_kind`, `subject_name`, and
  `match_basis`. These attributes are copied only from existing generated facts or
  relation/status rows; they must not contain source bodies, document bodies, config
  values, generated-source contents, raw command output, local absolute paths,
  credentials, tokens, or secret-looking values.
- `derivation` is required when an edge does not have a dedicated evidence-backed
  relation row. It must identify the source artifact, section, and field family used to
  derive the edge. For edges backed by an existing evidence-backed relation row,
  `derivation` may be `null`.
- `evidence_ids` contains existing evidence IDs from the underlying fact or relation
  when available. Structural ownership or containment edges that are supported only by a
  project-map field relationship must use `derivation` and an empty `evidence_ids` array;
  do not copy fact evidence IDs merely to prove the structural derivation.
- Status-only relation rows that lack a concrete graph target are emitted in
  `relation_statuses[]`, not as edges. This prevents unsupported, not-detected,
  ambiguous, or not-analyzed rows from looking like inferred target relations.

`relation_statuses[]` shape:

```json
{
  "id": "relation-status:tested_subject:node:test:root:com.example.orders.OrderControllerTest:no_supported_subject_signal",
  "relation_family": "tested_subject",
  "source_id": "node:test:root:com.example.orders.OrderControllerTest",
  "target_id": null,
  "relation_status": "not_detected",
  "support_type": "status_only",
  "confidence": "low",
  "uncertainty": "no_supported_subject_signal",
  "relation_attributes": {
    "relation_type": "not_detected"
  },
  "derivation": {
    "kind": "project_map_relation_status",
    "artifact": "project-map.json",
    "section": "tests.items[].tested_subjects"
  },
  "evidence_ids": []
}
```

Relation-status rules:

- `relation_statuses[]` preserves existing relation/status rows that cannot safely
  become edges because there is no concrete emitted target node or because the status is
  explicitly unsupported, not detected, ambiguous, uncertain, or not analyzed.
- `source_id` must resolve to an emitted source node when present. `target_id` is `null`
  when no concrete target is represented.
- `relation_attributes` follows the same bounded string-map rules as edge
  `relation_attributes` and preserves existing relation/status row details without
  promoting those details to graph targets.
- Relation statuses are navigation/status records only. They must not be treated as
  extracted facts, inferred target edges, coverage claims, source/spec agreement,
  documentation freshness, runtime behavior, or impact proof.

`warnings[]` shape:

```json
{
  "id": "graph-warning:cap:nodes",
  "category": "cap_reached",
  "severity": "warning",
  "message": "Graph node cap reached; lower-priority graph material was omitted.",
  "source_ref": null,
  "derivation": {
    "kind": "graph_cap",
    "artifact": "project-graph.json",
    "section": "nodes"
  },
  "evidence_ids": []
}
```

Graph-warning rules:

- Graph warnings are construction diagnostics for the graph artifact only. They are not
  project evidence, quality findings, security findings, vulnerabilities, runtime
  claims, or source facts.
- `id` is stable within a generated graph and uses
  `graph-warning:<category>:<deterministic_key>`.
- Initial warning categories include cap warnings and duplicate/collision omission
  warnings. Additional graph-warning categories require this contract to be updated.
- `severity` is `"warning"` for non-fatal graph construction diagnostics in the initial
  contract.
- `source_ref` identifies the source artifact or graph section when applicable and is
  `null` when the warning applies to the graph artifact as a whole.
- `derivation` explains the graph-construction condition that produced the warning. It
  is not evidence.
- `evidence_ids` is always an empty array for graph warnings.

Deterministic ID and sorting rules:

- Node keys and edge qualifiers reuse existing stable fact IDs when available. When no
  existing ID exists, they use normalized repository-relative paths, module IDs, fully
  qualified class names, operation keys, or status keys from the source fact.
- Graph ID key escaping should reuse the percent-encoding rules used for path-backed
  evidence IDs in this document: preserve the readable bounded key set and uppercase
  UTF-8 byte percent-encode separators or control characters that would make IDs
  ambiguous.
- If two generated IDs would otherwise collide, the implementation must add a
  deterministic `decl:<zero-padded-ordinal>` suffix based on source-order within the
  already sorted source collection or emit a graph warning and omit the duplicate.
- Nodes are sorted by `kind`, `id`.
- Edges are sorted by `type`, `source_id`, `target_id`, `id`.
- Relation statuses are sorted by `relation_family`, `source_id`, `target_id`, `id`.
- Graph warnings are sorted by warning category, source reference, and ID.
- JSON field order must remain stable for deterministic golden outputs.

Confidence and uncertainty rules:

- The graph uses the existing confidence labels `high`, `medium`, and `low`.
- Structural edges derived from deterministic project-map fields may use
  `confidence: "high"` only for the graph derivation, not for a stronger source claim.
- Inferred relation edges must preserve the confidence and uncertainty from the source
  relation row.
- Uncertain document reconciliation edges must remain low-confidence uncertain
  inspection hints.
- Missing uncertainty is represented as JSON `null`; unsupported or not-analyzed status
  is represented explicitly rather than by omitting a row.

Size and noise limits:

- The initial graph contract caps emitted graph material at 20,000 nodes, 50,000 edges,
  and 10,000 relation-status rows per scan.
- The implementation must apply caps deterministically and emit graph warnings when
  graph output is capped.
- Cap hits are not evidence and must not create evidence records.
- Cap priority should preserve higher-signal graph material first: module/type/fact
  nodes, structural ownership/declaration edges for emitted facts, existing inferred
  relation edges, relation statuses, and finally uncertain document-reference edges.
- Package nodes are emitted only for packages with emitted type nodes.
- Evidence records are not duplicated as graph nodes in the initial contract to avoid
  evidence fan-out.
- Document nodes are limited to the existing accepted document, heading, and chunk facts
  already emitted under the local document ingestion caps.
- Generated-source material remains metadata-only and must not read generated-source
  contents to populate graph nodes or edges.

Incremental cache interaction:

- `project-graph.json` is part of the base generated output set. Incremental cache
  output fingerprints include it as `.project-memory`-relative path
  `project-graph.json` with `output_kind: "project_graph"`.
- The cache schema can remain `cache_schema_version: "1.0"` if the cache file field
  shape remains unchanged; the tool-version and selected output fingerprint set still
  make older cache state miss safely.
- A validated cache hit must verify `project-graph.json` digest and size together with
  `project-map.json`, `evidence-index.jsonl`, `endpoints.md`, `agent-guide.md`, and any
  selected profile artifacts before skipping full analysis.
- A missing, stale, unsafe, or mismatched graph output fingerprint fails closed to full
  analysis. Full analysis then regenerates the complete selected output set.
- Normal scans without `--incremental` continue to ignore cache state.

Validation requirements:

- Focused model and serialization tests for graph schema, field order, nullability, and
  deterministic sorting.
- Focused node and edge builder tests for every documented node kind, edge type,
  relation-status row, ID escaping rule, collision rule, cap warning, and claim
  category.
- Golden output tests proving `project-graph.json` is stable and that existing
  `project-map.json`, `evidence-index.jsonl`, `endpoints.md`, and `agent-guide.md`
  behavior remains stable except for the intentional new graph artifact.
- Evidence-reference integrity checks proving every graph `evidence_ids` value resolves
  to `evidence-index.jsonl` and that graph derivation-only rows do not fabricate
  evidence IDs.
- Incremental cache tests proving graph output participates in selected output
  fingerprints, cache hits validate graph digests, graph mismatches fail closed to full
  analysis, and non-incremental scans ignore cache state.
- Content-safety checks proving graph output does not serialize source bodies, local
  document bodies, config contents, raw build-script bodies, generated-source contents,
  generated Markdown bodies, raw command logs, stack traces, local absolute paths,
  credentials, tokens, secret-looking values, downstream agent output, or LLM output.
- Representative fixture and packaged CLI checks for graph size/noise, duplicate IDs,
  dangling edges, unresolved evidence references, and deterministic repeated digests
  before release.
- Risk-based review is required before release for implementation that changes graph
  output paths, generated artifact ownership, cache output fingerprints, evidence
  reference handling, graph size limits, JSON rendering, filesystem/path behavior, CLI
  behavior, or config behavior.

Stop conditions for implementation:

- Graph semantics imply full call reachability, dependency reachability, runtime Spring
  wiring, runtime routing, runtime data access, source/spec agreement, documentation
  freshness, test coverage, CI status, assertion behavior, vulnerability, correctness,
  production impact, business priority, or complete architecture ownership.
- Graph nodes or edges cannot keep extracted, inferred, uncertain, document-backed,
  spec-backed, generated-source metadata-only, warning, and not-analyzed categories
  distinct.
- Node or edge identity cannot be made deterministic and stable enough for golden
  outputs.
- Evidence and derivation semantics are unclear, or derivation starts acting as
  evidence.
- Graph output would require source body serialization, local document body
  serialization, config value serialization, generated-source content scanning, build
  execution, runtime analysis, complete type solving, dependency resolution, network
  access, connectors, optional AI, repository chat, generic RAG, automatic code
  modification, query/impact commands, release automation, or publication automation.

### v0.9 CLI And Scan Configuration Contract

This section defines the v0.9 public output boundary for CLI/config behavior. The v0.9
implementation included the config parser and safe-defaults slice: root-local config
discovery, optional explicit config selection for `scan`, local Markdown-only
include/exclude refinement, reserved-mode rejection, redacted `scan` metadata, and the
no-tool-config-evidence decision. It also included help/version commands, bounded
command validation, stable exit codes, concise scan stdout, and a bounded CLI diagnostic
summary. Performance and distribution workflow polish did not add fields to this output
shape.

The v0.9 CLI/config contract uses:

- `schema_version: "0.9"` for output that preserves the v0.8 local Markdown/document
  contract and adds a top-level `scan` owner for redacted effective scan metadata.
- The same four output files under `.project-memory/`.
- Root-local YAML config discovery by default. The default discovered file name is
  `agent-project-memory.yml` at the scan root. Global config files, user-home config
  files, environment-variable config discovery, network-loaded config, and generated
  output directory config discovery are not part of the v0.9 design.
- Config precedence in this order: built-in defaults, then the selected scan-root config
  file, then explicit CLI flags. An explicit `--config` selection replaces default
  config-file discovery rather than merging multiple config files.
- A shared normalized repository-relative path policy for user include/exclude rules.
  The initial v0.9 implementation should apply user include/exclude rules only to local
  Markdown document discovery. Existing Java/Maven analyzers keep their documented
  supported-root behavior unless a later contract explicitly adds analyzer-specific path
  filtering.
- No `evidence-index.jsonl` evidence records for the tool config file. The selected
  scan config is execution metadata, not project evidence.

Current `project-map.json` excerpt. Unchanged v0.8 fields are omitted for focus:

```json
{
  "schema_version": "0.9",
  "scan": {
    "config": {
      "analysis_status": "analyzed",
      "source": "defaults_only",
      "config_file_path": null,
      "config_file_status": "not_detected",
      "cli_overrides_applied": false,
      "raw_values_serialized": false
    },
    "features": {
      "local_markdown": {
        "enabled": true,
        "source": "default"
      },
      "generated_sources": {
        "enabled": false,
        "status": "reserved_disabled"
      },
      "follow_symlinks": {
        "enabled": false,
        "status": "reserved_disabled"
      },
      "adapters": {
        "enabled": false,
        "selected_count": 0,
        "local_import_count": 0,
        "network_access": "disabled",
        "status": "disabled_by_default"
      }
    },
    "path_policy": {
      "path_format": "normalized_repository_relative",
      "case_sensitivity": "case_sensitive",
      "symlink_policy": "skip_symlinks",
      "default_exclusions_applied": true,
      "default_exclusion_override": "not_supported",
      "user_includes_applied": false,
      "user_include_count": 0,
      "user_excludes_applied": false,
      "user_exclude_count": 0
    },
    "diagnostics": {
      "analysis_status": "analyzed",
      "items": []
    }
  }
}
```

Current config file rules:

- The default config file is `<scan-root>/agent-project-memory.yml`.
- The selected config file, whether discovered by default or selected explicitly, must
  resolve to one regular YAML file under the scan root with a verifiable single-link
  identity and must not be a symlink or multi-link file.
- The current explicit config selection syntax is `scan <path> --config <path>`. The
  explicit config value is interpreted after scan-root validation as a normalized
  repository-relative path under the selected scan root. It must not be absolute, start
  with `./`, contain `.` or `..` path segments after normalization, use backslash
  separators, resolve outside the scan root, point into `.project-memory/`, point to a
  symlink, point to a multi-link file, or have an unverifiable link count.
- If the default config file is present and no explicit config is selected, it is the
  selected config. If no config is selected or discovered, built-in defaults apply.
- If an explicit config path is provided, default discovery is skipped. The explicit
  path must resolve to one regular YAML file under the scan root with a verifiable
  single-link identity.
- Config files are not merged. Multiple default config locations are intentionally not
  discovered in v0.9 so there is no hidden precedence between root-visible and hidden
  files.
- The current config format is YAML with a required bounded schema version:

  ```yaml
  version: 1
  features:
    local_markdown: true
    generated_sources: false
    follow_symlinks: false
  documents:
    include:
      - notes/**/*.md
    exclude:
      - docs/archive/**
  adapters:
    local_structured_import:
      enabled: false
  ```

- `version` is required and must be integer `1`.
- `features` is optional. `features.local_markdown` is an optional boolean. The reserved
  `features.generated_sources` and `features.follow_symlinks` keys may be present only
  with boolean `false`; attempts to set either to `true` are invalid config.
- `documents` is optional. `documents.include` and `documents.exclude` are optional
  lists of string path rules. Include rules must target Markdown files ending in `.md`
  or `.markdown`; exclude rules may target files or path trees.
- `adapters` is optional and disabled by default. The current v2 adapter import layer
  recognizes disabled-by-default `adapters.local_structured_import` and
  `adapters.git_hosting_import` blocks with `enabled` and `path`. The v2.2 connector
  import adds a disabled-by-default `adapters.connector_import` block with `enabled`
  and `path`.
- `adapters.local_structured_import.enabled` is optional and defaults to disabled. When
  it is `true`, `path` is required and must identify one existing repository-relative
  regular file under the scan root with a verifiable single-link identity. The path must
  not be absolute, start with `./`, use backslash separators, contain empty, `.` or
  `..` segments, point into `.project-memory/`, point to a directory, point to a
  symlink, pass through a symlinked path segment, point to a multi-link regular file,
  have an unverifiable link count, or resolve outside the scanned repository root. When
  the adapter is disabled, `path` must be omitted.
- When enabled, the local structured import adapter reads and parses the configured
  import file after config validation. The import file must be a JSON object with
  `format: "agent-project-memory.local_structured_import.v1"` and a `records` array.
  Each accepted record must use `source_type: "local_export"`, a stable safe
  `source_identity`, `status: "current"`, and a non-empty bounded `body`.
- When enabled, the Git hosting import adapter reads and parses the configured import
  file after config validation. The import file must be a JSON object with
  `format: "agent-project-memory.git_hosting_export.v1"` and a `records` array.
  Accepted records must use supported provider-normalized Git hosting fields, a stable
  safe provider/host/namespace/record identity, and `status: "current"`.
- When enabled, the connector import adapter reads and parses the configured import
  file after config validation. The import file must be a JSON object with
  `format: "agent-project-memory.connector_export.v1"` and a `records` array. Accepted
  records must use supported provider-normalized Jira, YouTrack, or Confluence fields,
  a stable safe provider/host/container/record identity, and `status: "current"`.
- The selected adapter emits `.project-memory/source-registry.json` and top-level
  `project-map.json` `adapter_context` as provenance-backed external/document context.
  Git hosting imports use `source_registry_schema_version: "1.1"` when provider
  metadata is emitted under `provenance[].git_hosting`. The connector import uses
  `source_registry_schema_version: "1.2"` when provider metadata is emitted under
  `provenance[].connector`. Adapter output does not serialize the configured import
  path, raw record bodies, raw connector or export contents, create evidence records,
  enable network access, accept credentials, load plugins, call AI providers, or upload
  source.
- Unknown top-level keys, unknown `features` or `documents` keys, unsupported values,
  unknown `adapters` keys, unsupported values, unsupported future-mode enables, invalid
  YAML, unsafe path values, oversized config files, YAML aliases that exceed parser
  limits, and non-scalar values where scalars are required fail as invalid config before
  output generation.
- Config parsing must not perform environment-variable interpolation, file includes,
  remote imports, command execution, credential lookup, plugin loading, or network
  access. Adapter import parsing happens only after the selected config has passed the
  bounded repository-relative local import path gate.
- Generated outputs must not serialize raw config values, raw user include/exclude
  patterns, config file contents, config excerpts, adapter import paths, raw connector
  or export contents, environment variables, decrypted values, credentials, tokens,
  secret-looking values, or local absolute paths.

Current feature toggle rules:

- `local_markdown` defaults to enabled to preserve the v0.8 no-config behavior. When it
  is disabled by config or CLI flag, local Markdown discovery, document structure,
  document evidence, document reconciliation, and local-document guide rendering are
  not run.
- When local Markdown is disabled, `documents.analysis_status` should be
  `"not_analyzed"` and `documents.reconciliation.analysis_status` should be
  `"not_analyzed"` if those shells are emitted. The reason may be represented in
  `scan.features.local_markdown` and `scan.diagnostics`, not by fabricating document
  evidence.
- `generated_sources` is reserved and disabled. A value or flag that attempts to enable
  generated-source scanning must be rejected until a later explicit generated-source
  scan mode defines its own analyzer, path policy, output contract, evidence semantics,
  focused tests, and review boundary.
- `follow_symlinks` is reserved and disabled. A value or flag that attempts to enable
  symlink following must be rejected until a later explicit symlink policy defines safe
  containment and evidence behavior.
- `adapters` defaults to disabled. When `local_structured_import` is explicitly enabled
  and its path is valid, `scan.features.adapters.enabled` is `true`,
  `selected_count` and `local_import_count` reflect the validated selection,
  `network_access` remains `"disabled"`, and `status` is one of:
  `local_import_read`, `local_import_read_with_rejections`,
  `local_import_read_with_partial_rejections`, or `disabled_by_default` when adapters
  are not enabled. The legacy `config_validated_no_reader` status is reserved for an
  enabled adapter configuration that validates but has no reader result.

Current include/exclude path semantics:

- User path rules use normalized repository-relative slash-separated paths. They must
  never be absolute, start with `./`, contain `.` or `..` path segments after
  normalization, use backslash separators, or resolve outside the scan root.
- Matching is case-sensitive and byte-stable. The contract does not promise
  filesystem-specific case folding.
- The initial supported pattern language is bounded: literal path segments, `*` inside
  one segment, and `**` only as a whole path segment. Brace expansion, character
  classes, extglob syntax, regex syntax, drive letters, and URL-like schemes are not
  part of the v0.9 design.
- Include rules add local Markdown candidates to the existing default-scope document
  candidate set. Exclude rules remove candidates from the default-plus-user candidate
  set. User excludes win over user includes.
- Built-in safety exclusions win over all user includes in v0.9. Hidden paths,
  `.project-memory/`, generated outputs, build outputs, dependency directories,
  private/internal paths, maintainer-like paths, secret-like path segments, symlinked
  files, and symlinked directories cannot be re-included by user config in the initial
  design.
- User include rules for local documents may accept only Markdown files in the supported
  local document formats. They must not add PDF, Word, external docs, connector docs,
  remote URLs, generated source files, binary files, or arbitrary source files as local
  document facts.
- User path rules change candidate selection only. They do not convert document
  evidence into code evidence, do not promote document mentions to source-backed facts,
  and do not change existing evidence semantics.
- `documents.discovery` may continue to list built-in default patterns, but it must not
  serialize raw user include/exclude patterns. Custom policy effects should be visible
  through `scan.path_policy` counts/statuses and per-document `discovery_source` values
  such as `"explicit_include"` when a document is accepted through a user include rule.

Current `scan.config` rules:

- `scan.config.analysis_status` is `"analyzed"` when config discovery and validation
  ran.
- `scan.config.source` is one of:
  - `"defaults_only"` when no config file or CLI override affected scan behavior.
  - `"config_file"` when a selected scan-root config file affected or confirmed
    behavior.
  - `"cli_overrides"` when CLI flags affected behavior without a config file.
  - `"config_file_and_cli_overrides"` when both a config file and CLI flags affected
    behavior.
- `config_file_path` is the normalized repository-relative path to the selected config
  file when it is safe to record and `null` otherwise. It must never be absolute or
  point outside the scan root.
- `config_file_status` is `"not_detected"`, `"applied"`, or `"explicit"`.
- `cli_overrides_applied` is a boolean.
- `raw_values_serialized` must be `false` for v0.9 output.

Current `scan.features` rules:

- Feature entries record effective enablement and the source of the effective value.
- `source` values are `"default"`, `"config_file"`, or `"cli_override"` for implemented
  toggles.
- Reserved disabled modes use `status: "reserved_disabled"` and `enabled: false`.
- Feature entries must not imply that an analyzer ran. Analyzer-specific
  `analysis_status` fields remain authoritative for generated fact sections.

Current `scan.path_policy` rules:

- `path_format` is `"normalized_repository_relative"`.
- `case_sensitivity` is `"case_sensitive"` in the v0.9 contract.
- `symlink_policy` is `"skip_symlinks"` until a later explicit mode changes it.
- `default_exclusions_applied` is `true` in normal v0.9 scans.
- `default_exclusion_override` is `"not_supported"` in the initial v0.9 design.
- User include/exclude counts record how many validated user rules were applied to local
  document candidate selection. They must not serialize the raw patterns.

Current `scan.diagnostics` rules:

- `scan.diagnostics.analysis_status` is `"analyzed"`.
- `scan.diagnostics.items[]` is empty when no bounded non-fatal diagnostic condition was
  observed. The current implementation emits warning items when aggregate local Markdown
  caps are reached, oversized Maven POM/root build-file inputs are skipped, or Java
  source inputs are skipped by bounded source parsing controls.
- Diagnostic items contain `id`, `severity`, `code`, `category`, `message`, nullable
  `path`, and nullable `count`. For current local Markdown aggregate cap diagnostics,
  `severity` is `"warning"`, `category` is `"documents"`, `path` is `null`, and `count`
  records the cap value that was reached.
- Current local Markdown cap diagnostic `code` values are
  `"local_markdown_document_count_cap_reached"`,
  `"local_markdown_document_bytes_cap_reached"`,
  `"local_markdown_heading_count_cap_reached"`,
  `"local_markdown_chunk_count_cap_reached"`,
  `"local_markdown_mention_count_cap_reached"`, and
  `"local_markdown_reconciliation_output_cap_reached"`.
- Current Maven POM/root build-file cap diagnostics use
  `code: "maven_pom_file_bytes_cap_exceeded"`, `severity: "warning"`,
  `category: "maven"`, `path` set to the normalized repository-relative POM path, and
  `count: 1048576`. The affected POM/root build-file input is skipped fail-closed: no
  Maven module declaration, Maven metadata, dependency, plugin, or root build-file
  evidence is emitted from skipped oversized POM bytes.
- Current Java source diagnostics use `severity: "warning"` and
  `category: "java_source"`. Per-file diagnostics set `path` to the normalized
  repository-relative Java source path; aggregate diagnostics set `path` to `null`.
  Current Java source diagnostic `code` values are
  `"java_source_file_bytes_cap_exceeded"`,
  `"java_source_file_lines_cap_exceeded"`,
  `"java_source_file_read_skipped"`,
  `"java_source_file_count_cap_reached"`,
  `"java_source_aggregate_bytes_cap_reached"`,
  `"java_source_aggregate_lines_cap_reached"`,
  `"java_source_ast_node_cap_exceeded"`, and
  `"java_source_parse_error"`. Java source files that exceed per-file byte, line,
  no-symlink stable regular-file read, AST node, parse, file-count, or aggregate source
  workload limits are skipped fail-closed before unbounded JavaParser or full source
  line materialization. Diagnostics are scan metadata and must not be referenced from
  `evidence_ids`.
- Later v0.9 diagnostics may add other bounded scan metadata for non-fatal conditions
  such as config defaults in use, user path rules accepted, user path rules matching no
  candidate, files skipped by built-in safety exclusions, disabled local docs, or
  generated-source roots remaining warning-only.
- Fatal usage, scan input, invalid config, output write, and unexpected internal errors
  are reported through CLI exit codes and stderr. A scan that fails before output
  generation should not create a partial `project-map.json` solely to record fatal
  diagnostics.
- Diagnostic messages must be deterministic and bounded. They must not include raw
  config values, raw include/exclude patterns, source excerpts, document bodies, config
  contents, environment variables, credentials, tokens, secret-looking values, stack
  traces, local absolute paths, timing measurements, or generated output contents.
- Diagnostics are not evidence. Diagnostic item IDs must not be referenced by
  `evidence_ids`.

Current v0.9 CLI behavior:

- `agent-project-memory --help`, `agent-project-memory help`,
  `agent-project-memory scan --help`, `agent-project-memory --version`, and
  `agent-project-memory version` succeed without scanning.
- Help and version output go to stdout and exit with code `0`.
- Version output is one line: `agent-project-memory <version>`.
- Usage errors such as a missing top-level command, unknown commands, unknown flags,
  duplicate mutually exclusive flags, or unexpected extra arguments go to stderr and exit
  with code `2`.
- Scan input errors such as invalid path syntax, missing scan path, non-directory scan
  path, unresolved scan root, invalid output directory, output symlink, or output path
  escaping the scan root go to stderr and exit with code `3`.
- Invalid config errors such as missing explicit config file, unsafe config path,
  invalid YAML, unsupported schema version, unknown keys, invalid value types, invalid
  include/exclude path rules, or attempts to enable reserved modes go to stderr and exit
  with code `4`.
- Output generation or write errors go to stderr and exit with code `5`.
- Unexpected internal errors go to stderr with a bounded generic message and exit with
  code `1` unless a later CLI contract defines a more specific code. Stack traces are
  not printed by default.
- Successful scans exit with code `0`, even when bounded non-fatal diagnostics are
  emitted.
- Normal stdout remains concise: `.project-memory` preparation, generated output file
  names, stable fact counts when outputs are written, a no-output line when no supported
  contract inputs are detected, a bounded generated profile artifact count when profile
  artifacts are written, and a bounded diagnostic summary such as
  `Diagnostics: none.` or `Diagnostics: N item(s).` Detailed diagnostics, when a flag is
  added, should still follow the redaction rules above.

### v1.6 Query CLI Contract

This section defines the public contract for the v1.6 local query/read-only explorer.
The published v1.6 command surface includes the read-only artifact-loading foundation
plus deterministic text output for `query <path> list modules`, `list endpoints`,
`list api-operations`, `list entities`, `list tests`, `explain evidence <id>`,
`find fact <term>`, `find symbol <term>`, and `relations <id>`. Relation lookup supports
`--direction incoming|outgoing|both` with `both` as the default. A `--format` flag and
stable JSON result envelope are not included in v1.6.0 and remain future work.

The query layer is a deterministic presentation and lookup layer over existing
generated artifacts. It does not create project facts, does not create evidence
records, does not mutate `.project-memory/`, does not read repository source files, and
does not run a scan. Query output is not evidence.

Implemented command grammar:

```text
agent-project-memory query <path> list modules
agent-project-memory query <path> list endpoints
agent-project-memory query <path> list api-operations
agent-project-memory query <path> list entities
agent-project-memory query <path> list tests
agent-project-memory query <path> explain evidence <evidence-id>
agent-project-memory query <path> find fact <term>
agent-project-memory query <path> find symbol <term>
agent-project-memory query <path> relations <id> [--direction incoming|outgoing|both]
```

The packaged-jar invocation uses the same arguments after `java -jar
agent-project-memory-X.Y.Z.jar`. The installed `agent-project-memory` command remains
future distribution work until a release note documents an installed channel.

Path and artifact input policy:

- `<path>` is required and must be one local directory argument.
- If `<path>` names a `.project-memory` directory, that directory is the artifact root.
  Otherwise `<path>` is treated as a repository root and the artifact root is
  `<path>/.project-memory`.
- The query layer reads only direct child artifact files from the artifact root. It
  must not create the artifact root, create missing artifacts, refresh cache metadata,
  generate profile artifacts, run scans, or write repository files.
- The required artifacts for non-graph commands are `project-map.json` and
  `evidence-index.jsonl`. Non-graph commands must not require, read, or validate
  `project-graph.json`.
- `source-registry.json` is not a query input source in this slice. The current query
  layer does not look up source-document IDs, provenance IDs, or adapter context rows.
- `project-graph.json` is required only for `relations`. `find fact` may include graph
  node, edge, relation-status, and graph-warning IDs only for graph ID-shaped lookup
  terms when graph output is present and valid. Other non-graph query commands must not
  require graph output, and all non-graph query commands must ignore a missing or
  malformed graph artifact.
- `endpoints.md`, `agent-guide.md`, `agent-profiles/`, and `cache/v1/` are not query
  input sources in the initial v1.6 contract. Generated Markdown, profile artifacts, cache
  metadata, diagnostics, graph derivation metadata, and query output are not evidence.
- Query path handling follows the same conservative local boundary as generated
  artifacts: artifact paths must remain repository-local or artifact-root-local,
  normalized with slash separators in outputs, and must not serialize local absolute
  paths in successful stdout.
- Query input directories and required artifact files must be stable local directories
  or regular files. The initial query design does not follow symlinked artifact roots or
  symlinked artifact files and rejects multi-link or link-count-unverifiable required
  artifact files before parsing.

Artifact validation policy:

- `project-map.json` must parse as JSON and use a supported `schema_version`.
  The initial v1.6 contract supports the current stable-line marker `"1.0"`.
  Adapter-enabled `schema_version: "2.0"` artifact sets are outside current query
  support unless a later query contract explicitly adds adapter-aware behavior.
- `evidence-index.jsonl` must parse as newline-delimited JSON with unique evidence
  `id` values and the documented evidence field set. Evidence `path` values must be
  normalized repository-relative safe paths: no local absolute paths, `./` prefixes,
  parent traversal, backslash separators, URL/file-URL values, blank or newline-bearing
  values, drive paths, or `.project-memory/` generated-output paths.
- `project-map.json` evidence reference fields named `evidence_ids` or ending in
  `_evidence_ids` must be arrays whose string values resolve to
  `evidence-index.jsonl`.
- `project-graph.json`, when required by `relations` or graph-backed `find fact`, must
  parse as JSON and use a supported `graph_schema_version`. The initial v1.6 contract
  supports `"1.0"`.
- Query commands fail closed for missing required artifacts, malformed JSON/JSONL,
  unsupported schema markers, duplicate IDs in a required lookup index, graph edges that
  reference missing graph nodes, and graph `evidence_ids` that do not resolve to
  `evidence-index.jsonl`.
- Query commands must not repair, rewrite, normalize in place, or silently regenerate an
  invalid artifact set.

List command behavior:

- The v1.6 list-command implementation supports deterministic text output only. A
  `--format text|json` flag and stable JSON result envelope are not implemented in
  v1.6.0.
- `list modules` reads `project.modules.items[]` and emits deterministic module rows
  with module ID, module path, build systems, support status, and available evidence ID
  references. Module path inventory remains a generated fact from `project-map.json`,
  not query evidence.
- `list endpoints` reads source-visible Spring MVC endpoint facts from top-level
  `endpoints[]` only. It must not merge spec-backed OpenAPI operations, generated-source
  signals, or hidden HTTP warnings into endpoint rows.
- `list api-operations` reads spec-backed declared operation facts from
  `api_surface.openapi.operations.items[]`. Rows must use declared/spec-backed wording
  and must not imply implementation, runtime routing, or source/spec agreement.
- `list entities` reads direct JPA entity facts and embeddable facts from the domain
  output sections and keeps entity and embeddable rows visibly distinct. Relationship
  targets, embedded targets, repository/entity links, and status-only rows must preserve
  inferred, uncertain, unsupported, ambiguous, or not-analyzed labels from the artifacts.
- `list tests` reads emitted tests from the tests inventory and preserves test class
  IDs, module ownership, direct framework/slice/mock signal categories, tested-subject
  relation/status labels, confidence, uncertainty, and evidence ID visibility where
  present. It must not claim execution, coverage, assertions, CI status, or runtime
  behavior.
- Empty list results are successful results with an empty result set.

Evidence explain behavior:

- The v1.6 evidence-explain implementation supports deterministic text output only. A
  `--format text|json` flag and stable JSON result envelope are not implemented in
  v1.6.0.
- `explain evidence <evidence-id>` performs an exact evidence ID lookup in
  `evidence-index.jsonl`.
- Successful output renders only the existing evidence record fields: `id`,
  `source_type`, `path`, `class_name`, `method_name`, `symbol_name`, `line_start`,
  `line_end`, `excerpt`, and `confidence`.
- The command must not open the referenced source file to expand excerpts, fill missing
  line ranges, infer additional symbols, or validate runtime behavior.
- A missing evidence ID is a no-result lookup, not an invitation to scan source files.

Fact and symbol lookup behavior:

- The v1.6 fact and symbol lookup implementation supports deterministic text output
  only. A `--format text|json` flag and stable JSON result envelope are not implemented
  in v1.6.0.
- `find fact <term>` performs exact, case-sensitive lookup over generated fact IDs and
  documented exact keys already present in generated artifacts, such as endpoint IDs,
  operation keys, entity IDs, repository IDs, test IDs, warning IDs, status IDs,
  document IDs, graph node IDs, graph edge IDs, relation-status IDs, and graph-warning
  IDs when graph output is present.
- Graph-backed fact lookup is selected only for graph ID-shaped terms such as `node:`,
  `edge:`, `relation-status:`, and `graph-warning:`. Other fact lookup terms use
  `project-map.json` without reading or validating `project-graph.json`.
- `find symbol <term>` performs exact, case-sensitive lookup over structured symbol
  fields already present in generated artifacts, such as fully qualified class names,
  simple class names when represented by a generated fact, method names tied to emitted
  endpoint or test facts, repository names, entity names, operation IDs, and evidence
  `symbol_name` values.
- Lookup is not substring search, fuzzy search, regex search, glob search,
  natural-language query, semantic search, or embedding search.
- Multiple exact matches are valid successful results. No-match lookup exits with the
  no-result exit code.
- Lookup results must identify the source artifact and section or graph `source_ref`
  that produced the row. These references are navigation metadata, not evidence.

Graph relation lookup behavior:

- The v1.6 relation lookup supports deterministic text output only. A
  `--format text|json` flag and stable JSON result envelope are not implemented in
  v1.6.0.
- `relations <id>` requires a valid `project-graph.json`.
- `<id>` may be either a graph node ID or a generated fact ID that can be mapped to a
  graph node through the node `source_ref`.
- The initial relation lookup is one-hop only. It has no depth flag and must not perform
  transitive traversal, reachability analysis, impact analysis, call-graph traversal, or
  dependency traversal.
- `--direction` defaults to `both`. `incoming` shows edges whose `target_id` is the
  selected node, `outgoing` shows edges whose `source_id` is the selected node, and
  `both` shows both sets in deterministic graph order.
- Relation output must keep graph edges and `relation_statuses[]` separate. Status-only
  rows must not be promoted to edges.
- Relation output must preserve `type`, `relation_family`, `claim_category`,
  `relation_status`, `support_type`, `confidence`, `uncertainty`,
  `relation_attributes`, `derivation`, and `evidence_ids` values as graph navigation
  metadata. `derivation` remains non-evidence.
- Missing graph output is an artifact input error for `relations` and is ignored by
  non-graph query commands.
- A missing relation subject ID is a no-result lookup in otherwise valid artifacts.

Text output and future JSON output behavior:

- Text output is the default and only implemented v1.6 output mode. It is deterministic,
  concise, human-readable, and safe for terminal use, but exact text layout is not the
  stable parser interface unless a later contract documents a specific text structure.
- `--format text` and `--format json` are not implemented in v1.6.0. A future JSON
  mode, if added, should emit a stable machine-readable query result envelope.
- Successful results go to stdout. Successful commands should not print to stderr.
- Usage errors, artifact input errors, invalid artifact errors, no-result lookup errors,
  and unexpected internal errors go to stderr and must not produce partial stdout.
- Error messages must be bounded and deterministic. They must not print stack traces,
  source bodies, document bodies, config contents, generated-source contents, generated
  Markdown bodies, raw command logs, local absolute paths, credentials, tokens, or
  secret-looking values.
- Future JSON output must use stable field order and JSON escaping. It must not include
  local absolute paths or raw user command text.
- The future JSON envelope is expected to follow this shape unless a later contract
  changes it:

```json
{
  "query_schema_version": "1.0",
  "command": "list",
  "subject": "modules",
  "source_artifacts": [
    {
      "name": "project-map.json",
      "schema_version": "1.0"
    },
    {
      "name": "evidence-index.jsonl",
      "record_count": 12
    }
  ],
  "result_count": 1,
  "results": [],
  "diagnostics": []
}
```

JSON envelope rules:

- `query_schema_version` is the machine-readable stdout contract marker for query
  results. The initial planned marker is `"1.0"`.
- `command` is the query verb such as `"list"`, `"explain"`, `"find"`, or
  `"relations"`.
- `subject` identifies the list kind, lookup kind, or relation subject.
- `source_artifacts[]` names only generated artifact filenames and their relevant schema
  markers or bounded counts. It must not contain local absolute paths.
- `results[]` contains command-specific rows copied or projected from generated
  artifacts. Rows may include existing `evidence_ids` references and existing bounded
  evidence excerpts for `explain evidence`, but query output itself remains
  non-evidence.
- `diagnostics[]` contains query-output diagnostics only. Query diagnostics are not
  project evidence, graph evidence, scan diagnostics, security findings, runtime claims,
  or generated facts.

Query exit codes:

- `0`: success, including successful empty list results.
- `1`: unexpected internal error.
- `2`: usage error, such as an unknown query subcommand, unknown flag, malformed
  command, invalid `--direction` value, or unexpected extra arguments. A future
  `--format` option should also use exit code `2` for invalid values.
- `3`: query input or artifact error, such as a missing query path, non-directory query
  path, missing `.project-memory/`, missing required artifact, symlinked artifact root
  or required artifact file, multi-link or link-count-unverifiable required artifact
  file, malformed JSON/JSONL, unsupported artifact schema marker, duplicate required
  IDs, unresolved required graph node references, or missing/invalid graph artifact for
  `relations`.
- `4`: invalid scan or workspace config. This existing config exit code is unchanged
  and is not used by read-only query commands.
- `5`: scan output generation or write error. This existing scan exit code is unchanged;
  read-only query commands should not write outputs.
- `6`: query no-result, such as an absent evidence ID, absent exact fact/symbol match,
  or absent relation subject ID in otherwise valid artifacts.

Validation requirements before v1.6 release:

- Focused CLI parser tests for every implemented query grammar branch and invalid argument
  shape.
- Artifact path tests for repository-root input, direct `.project-memory` input,
  missing path, missing artifact root, missing required artifacts, symlink rejection,
  malformed JSON/JSONL, unsupported schema markers, duplicate IDs, invalid graph
  references, and graph absence for non-graph commands.
- Deterministic stdout tests for text output. Stable field-order tests are required if a
  future JSON output mode is implemented.
- Focused list, evidence explain, fact lookup, symbol lookup, no-result, multiple-match,
  and graph relation tests over generated-memory fixtures.
- No-write tests proving query commands do not create, rewrite, delete, or refresh
  `.project-memory/`, cache metadata, profile artifacts, source files, docs, or config
  files.
- Content-safety tests proving query output does not serialize source bodies, local
  document bodies, config contents, raw build-script bodies, generated-source contents,
  generated Markdown bodies, raw command logs, stack traces, local absolute paths,
  credentials, tokens, secret-looking values, downstream agent output, or LLM output.
- Packaged CLI smoke before release for representative artifact sets, including a graph
  artifact set and a valid non-graph artifact set.

Stop conditions for implementation:

- Query behavior requires source scanning, scan refresh, artifact mutation, cache
  refresh, profile generation, repository writes, code modification, natural-language
  query, embeddings, vector search, generic RAG, connectors, network/auth, telemetry,
  SaaS, web UI, editor integration, agent server surfaces, or LLM calls.
- Query output starts treating query diagnostics, graph derivation metadata, generated
  Markdown, cache metadata, profile artifacts, downstream agent output, or LLM output as
  evidence.
- Relation lookup starts implying call reachability, dependency reachability, runtime
  Spring wiring, runtime routing, source/spec agreement, documentation freshness,
  coverage, CI status, assertion behavior, vulnerability, correctness, production
  impact, business priority, complete architecture ownership, or impact analysis.
- Artifact path behavior cannot be kept local, deterministic, read-only, and bounded to
  the approved generated artifacts.

### v2.4 Read-Only Agent Context Query Contract

This section defines the accepted v2.4 design boundary for the read-only agent
consumption surface. The published v2.4 implementation provides the CLI-only
query-layer expansion described here.

The first v2.4 surface is a CLI-only query command that renders deterministic
agent-context output over existing generated artifacts. It is the approved equivalent to
an MCP/server integration for the first slice because it preserves the current local
process, artifact-root, and no-write query boundary.

Implemented command grammar:

```text
agent-project-memory query <path> agent-context
```

The packaged-jar invocation uses the same arguments after
`java -jar agent-project-memory-X.Y.Z.jar`. No installed command, server process,
socket listener, editor plugin, MCP transport, or public API transport is part of this
first slice.

Path and artifact input policy:

- `<path>` follows the existing query path policy: it must be one local directory
  argument, either a repository directory containing `.project-memory/` or the
  `.project-memory/` directory itself.
- The command reads only documented direct child artifacts from the resolved artifact
  root. It must not create the artifact root, create missing artifacts, refresh cache
  metadata, generate profile artifacts, generate AI presentation artifacts, run scans,
  or write repository files.
- Required inputs for the first slice are `project-map.json` with
  `schema_version: "1.0"` and `evidence-index.jsonl`.
- `project-graph.json` may be used only as optional navigation metadata when present,
  valid, and supported. Missing or malformed graph output must not make the command
  read source files or infer relations outside generated artifacts.
- `source-registry.json`, adapter context rows, connector records, generated Markdown
  bodies, `agent-profiles/`, `ai-presentations/`, and `cache/v1/` are not fact input
  sources in the first slice. Adapter-aware agent context requires a later query and
  output contract update.
- Required artifact files must follow the existing query safety policy for local
  directories, regular files, symlink rejection, multi-link rejection, unsupported
  schema rejection, bounded diagnostics, and no local absolute path serialization in
  successful output.

Output policy:

- Output is deterministic stdout for agent/editor consumption. It is not a generated
  project-memory artifact, not evidence, and not a stable parser interface unless a
  later release explicitly defines a machine-readable output format.
- Successful output may include a source-artifact summary, reading order, supported
  query commands, bounded project orientation from generated facts, graph navigation
  hints when available, and existing evidence IDs.
- Successful output must not open referenced source files to expand evidence excerpts,
  fill missing line ranges, infer additional symbols, validate runtime behavior, or
  reinterpret generated facts.
- Successful output must not serialize raw repository source bodies, generated-source
  contents, local document bodies, generated Markdown bodies, raw connector exports,
  raw adapter input files, raw prompt transcripts, raw API requests or responses,
  environment values, credentials, tokens, cookies, authorization headers, command logs,
  stack traces, local absolute paths, downstream agent output, or LLM output.
- Error output follows the existing bounded query stderr policy and must not produce
  partial stdout.

Authority and evidence policy:

- Agent context output is navigation and presentation only. It must not create project
  facts, evidence records, evidence IDs, evidence fields, confidence labels, source
  references, connector truth, security findings, vulnerability proof, runtime claims,
  source/spec agreement claims, documentation-freshness claims, release evidence, or
  code-change authority.
- Existing evidence IDs remain the only evidence references. The command may point to
  `query <path> explain evidence <evidence-id>` for exact evidence inspection, but it
  must not repair, synthesize, or strengthen evidence.
- Graph derivation metadata, query output, generated Markdown, profile artifacts, cache
  metadata, adapter diagnostics, AI presentation, agent context output, downstream
  agent output, and LLM output remain non-evidence.
- AI presentation artifacts, when present, may be mentioned only as optional
  non-authoritative/non-evidence presentation. The first slice must not parse
  `ai-presentations/brief.md` as fact input or treat AI presentation as query truth.

Forbidden first-slice behavior:

- repository source readback;
- repository writes or generated artifact mutation;
- scan config edits or root instruction file edits;
- commits, branches, tags, releases, pull requests, issues, or external writes;
- automatic code modification;
- MCP/server/API/listener/daemon/editor/plugin runtime;
- network access, remote service calls, remote config, update checks, background sync,
  telemetry, credential lookup, credential storage, source upload, or real provider
  calls;
- adapter-aware query, provenance promotion, semantic search, embeddings, vector stores,
  generic RAG, repository chat, workspace memory, or change-impact claims.

Validation requirements:

- focused CLI parser tests for `query <path> agent-context` and invalid argument
  shapes;
- artifact path tests matching the existing query artifact-root safety policy;
- schema and malformed-artifact tests, including rejecting adapter-enabled
  `schema_version: "2.0"` until adapter-aware query is explicitly designed;
- deterministic stdout tests for representative generated-memory fixtures;
- no-write tests proving the command does not create, rewrite, delete, or refresh
  `.project-memory/`, cache metadata, profile artifacts, AI presentation artifacts,
  source files, repository docs, root instruction files, scan config, or external
  state;
- content-safety tests proving output does not serialize the forbidden raw inputs or
  sensitive surfaces listed above;
- focused release review for implementation changes that affect CLI behavior, artifact
  reads, output rendering, path/filesystem behavior, query behavior, evidence/provenance
  rendering, or generated-output boundaries.

Stop conditions for implementation:

- agent context requires source scanning, source readback, artifact mutation,
  repository writes, server/API behavior, editor integration, network/auth, credentials,
  telemetry, source upload, prompt transcript serialization, AI provider calls,
  semantic search, generic RAG, or automatic code modification;
- agent context output starts treating query output, generated Markdown, profile
  artifacts, AI presentation, graph derivation, cache metadata, adapter diagnostics,
  downstream agent output, prompts, or LLM output as evidence;
- path behavior cannot stay local, deterministic, read-only, and bounded to approved
  generated artifacts.

### v2.5 Workspace Output Design Contract

This section defines the accepted v2.5 workspace output boundary. The current
implementation includes the `workspace scan <config>` config/root-safety foundation and
workspace map aggregation from existing member artifacts.

Command shape:

```text
agent-project-memory workspace scan <config>
```

The packaged-jar invocation uses the same arguments after
`java -jar agent-project-memory-X.Y.Z.jar`. The installed `agent-project-memory`
command remains tied to the documented distribution channel for the release that ships
the workspace feature.

Workspace config and root policy:

- `<config>` is an explicit local YAML file argument. The first workspace boundary does
  not include default workspace config discovery, user-home config, environment-variable
  config, remote config, parent-directory crawling, organization discovery, provider
  discovery, or network-loaded config.
- The workspace root is the directory containing the config file. Member paths are
  normalized workspace-relative paths under that root. Absolute member paths, `./`
  prefixes, `..` escapes, path traversal, generated-output paths, hidden tool output
  roots, and local absolute path serialization are outside the first boundary. A member
  root must be a non-empty relative path made of safe path segments; `.` is not accepted
  as a member root in the current foundation.
- The accepted YAML shape is:

```yaml
version: 1
members:
  - repo_id: orders
    root: services/orders
```

- The top-level keys are `version` and `members`. Each member entry uses `repo_id` and
  `root`. Unknown keys, duplicate YAML keys, missing members, missing `repo_id`, and
  missing member roots fail closed.
- The config file and configured member roots must be local stable files or
  directories with conservative link handling. Symlinked config files, symlinked member
  roots, multi-link or link-count-unverifiable files that would be parsed as trusted
  workspace input, missing roots, duplicate roots, duplicate `repo_id` values, and
  ambiguous nested roots fail closed with bounded diagnostics before workspace output is
  trusted.
- Each member requires a configured logical `repo_id`. The first boundary has no
  path-derived fallback ID. `repo_id` is a stable workspace-local join key, not a local
  path, remote URL, branch name, package name, build coordinate, service discovery name,
  adapter source identity, or content hash. A display name may be added later as
  presentation metadata, but stable joins must use `repo_id`.
- Monorepo service roots and separate repository roots use the same member model:
  each configured member has one unique `repo_id`, one workspace-relative root path,
  and one resolved per-repo artifact root when artifacts are available.

Workspace artifact placement:

- The workspace artifact is written under the workspace root:
  `.project-memory/workspace-map.json`.
- `workspace scan <config>` may create the workspace-root `.project-memory/` directory
  and rewrites only the workspace-root `workspace-map.json` artifact.
- Per-repo generated artifacts remain under each member's own `.project-memory/`
  directory. The workspace artifact must not rename, move, merge, or rewrite
  per-repo `project-map.json`, `project-graph.json`, `evidence-index.jsonl`,
  `source-registry.json`, generated Markdown, cache metadata, agent profiles, or AI
  presentation artifacts.
- Running or refreshing child repo scans is a separate explicit write-scope decision;
  it is not implied by workspace map aggregation.

Current `workspace-map.json` shape:

```json
{
  "workspace_schema_version": "1.0",
  "workspace": {
    "root_kind": "config_directory",
    "config_source": {
      "path": "agent-project-memory-workspace.yml",
      "path_kind": "workspace_relative_file",
      "content_status": "not_serialized"
    },
    "member_count": 2
  },
  "members": [
    {
      "repo_id": "orders",
      "root_path": "services/orders",
      "root_path_kind": "workspace_relative_directory",
      "artifact_root": "services/orders/.project-memory",
      "artifact_status": "present",
      "project_map_schema_version": "1.0",
      "graph_schema_version": "1.0",
      "source_registry_schema_version": null,
      "evidence_record_count": 12,
      "sample_evidence_references": [
        {
          "repo_id": "orders",
          "evidence_id": "ev:src/main/java/com/example/OrderController.java:20-20:com.example.OrderController#get:@GetMapping",
          "artifact": "evidence-index.jsonl"
        }
      ]
    }
  ],
  "relations": {
    "analysis_status": "not_analyzed",
    "items": []
  },
  "diagnostics": []
}
```

Shape rules:

- `workspace_schema_version` is the machine-readable contract marker for
  `workspace-map.json`. The current marker is `"1.0"`.
- `workspace.config_source.path`, member `root_path`, and member `artifact_root` are
  workspace-relative paths. They must not be local absolute paths and must not escape
  the workspace root.
- `config_source.content_status: "not_serialized"` records that raw config contents are
  not copied into generated workspace output.
- `members[].repo_id` is required, unique, deterministic for the configured workspace,
  and safe to serialize.
- `members[].artifact_status` is `"present"` when required member artifacts are
  accepted, `"missing"` when the member artifact root is absent, and `"invalid"` when a
  required member artifact is missing, unsafe, malformed, or unsupported.
- Member artifact schema fields summarize accepted per-repo artifacts only. They are
  compatibility metadata, not evidence and not proof that a child repo scan is current.
- `evidence_record_count` is bounded artifact metadata. Workspace output must not copy
  full per-repo `evidence-index.jsonl` records into `workspace-map.json`.
- `sample_evidence_references[]` is a bounded navigation sample from the member's
  existing `evidence-index.jsonl`. Each item must use the composite workspace reference
  shape with `repo_id`, `evidence_id`, and `artifact: "evidence-index.jsonl"`. These
  references are not new evidence records and do not imply relation support.
- `relations.analysis_status: "not_analyzed"` and empty `items[]` are the first
  planned relation boundary. Cross-repo relation emission is parked until a later
  bounded goal accepts deterministic relation families and updates this contract.
- `diagnostics[]` contains bounded workspace diagnostics only. Diagnostics are not
  project evidence, security findings, runtime claims, relation claims, or release
  evidence.
- Workspace diagnostic items use bounded fields: `id`, `severity`, `category`,
  `repo_id`, `artifact`, and `message`. These fields must not contain local absolute
  paths, raw config contents, raw artifact contents, source bodies, credentials, tokens,
  command logs, or stack traces.

Workspace evidence-reference policy:

- Normal per-repo `evidence-index.jsonl` records keep their existing field set and
  repository-relative `path` values. The first workspace boundary does not add `repo_id`
  to per-repo evidence records and does not change single-repo evidence IDs.
- Workspace-level references to evidence must use a composite reference containing
  `repo_id` and the existing per-repo `evidence_id`, for example:

```json
{
  "repo_id": "orders",
  "evidence_id": "ev:src/main/java/com/example/OrderController.java:20-20:com.example.OrderController#get:@GetMapping",
  "artifact": "evidence-index.jsonl"
}
```

- Composite workspace evidence references are navigation keys into member artifacts.
  They are not new evidence records, do not strengthen evidence, and must not be
  resolved by copying evidence records into the workspace artifact.
- A future relation item, if accepted later, must carry evidence from every
  participating repo through composite evidence references or remain absent/uncertain.

Compatibility and non-input rules:

- Existing `scan <path>` behavior, no-adapter `project-map.json`
  `schema_version: "1.0"`, adapter-enabled `schema_version: "2.0"`, graph output,
  source registry output, cache metadata, agent profiles, AI presentation, generated
  Markdown, and current query commands remain unchanged unless a later implementation
  goal explicitly updates those contracts.
- `workspace-map.json` is not a query input source for the current v1.6/v2.4 query
  commands. Workspace query and workspace `agent-context` require a later query contract.
- Adapter provenance may be summarized as member artifact metadata only when present.
  Adapter records must not become workspace evidence, Java/Spring facts, cross-repo
  relation evidence, connector truth, runtime claims, documentation-freshness claims,
  source/spec agreement claims, or automatic code-modification input.
- AI presentation, query output, generated Markdown, graph derivation, cache metadata,
  profile output, adapter diagnostics, prompts, downstream agent output, LLM output,
  release notes, and maintainer notes must not create workspace facts or relations.

Forbidden first-slice behavior:

- remote clone, fetch, pull, provider API scan, organization crawler, background sync,
  remote cache, SaaS index, source upload, network access, credentials, telemetry,
  MCP/server/API/editor/plugin runtime, repository chat, generic RAG, semantic search,
  embeddings, vector stores, automatic code modification, speculative service
  dependency graphs, runtime dependency graphs, call graphs, source/spec agreement
  scoring, documentation-freshness scoring, vulnerability findings, correctness claims,
  release automation, or package publication behavior.

Validation requirements before workspace release:

- focused config grammar tests for accepted and rejected workspace YAML shapes;
- root containment tests for missing, escaping, duplicate, nested, symlinked,
  multi-link, link-count-unverifiable, and generated-output roots;
- tests proving required `repo_id` values, duplicate handling, and no path-derived
  fallback IDs;
- no-unintended-scan and no-unintended-write tests for the first root-safety slice;
- workspace aggregation tests over multi-repo and monorepo fixtures once aggregation is
  implemented;
- composite workspace evidence-reference resolution tests when workspace output
  references per-repo evidence;
- compatibility tests proving normal single-repo scan and query outputs remain stable.

Stop conditions for implementation:

- workspace root identity, member root identity, repo identity, artifact placement,
  path containment, link handling, or evidence-reference semantics are unclear;
- generated workspace output would need local absolute paths, configured raw path
  patterns, raw config contents, source bodies, document bodies, credentials, command
  logs, raw adapter input files, raw connector exports, prompts, or maintainer notes;
- workspace behavior requires remote scanning, network/auth, credentials, source upload,
  SaaS indexing, repository chat, generic RAG, semantic search, automatic code
  modification, or a server/API/plugin/editor runtime;
- cross-repo relations would be speculative or lack evidence from every participating
  repo.

### v2.6 Change-Impact Query Design Contract

This section defines the accepted v2.6 design boundary for the first change-impact
workflow. The current implementation includes the direct mapping foundation for this
command plus conservative one-hop projection: parser support, required no-adapter
artifact loading, changed-file path validation, direct matches, one-hop graph neighbors,
tied relation-status rows, low-confidence planning hints, explicit `not_represented`
rows, and bounded diagnostics. The command is read-only, single-repo, stdout-only, and
based on existing generated artifacts.

Command shape:

```text
agent-project-memory query <path> impact --files <changed-file> [<changed-file> ...]
```

The packaged-jar invocation uses the same arguments after
`java -jar agent-project-memory-X.Y.Z.jar`. A top-level `impact` command remains
parked for a later CLI design unless this contract is updated.

Path and artifact input policy:

- `<path>` follows the existing query artifact-root policy. If it names a
  `.project-memory` directory, that directory is the artifact root. Otherwise it is
  treated as a repository root and the artifact root is `<path>/.project-memory`.
- The command reads only direct child artifact files from the selected artifact root.
  It must not create the artifact root, create missing artifacts, refresh cache
  metadata, run scans, generate profile or AI presentation artifacts, write an impact
  report, or write repository files.
- `--files` is required and must contain at least one explicit changed-file path. Each
  changed-file value is a repository-relative file path string using slash separators.
- Changed-file values must not be local absolute paths, start with `./`, contain `..`
  segments, contain backslash separators, be URL-like values, be empty values, name
  `.project-memory/` generated-output paths, or require glob, shell, or regex
  expansion.
- Changed-file values are normalized and de-duplicated deterministically for matching
  and rendering. Duplicate inputs may produce a bounded diagnostic, but duplicates must
  not duplicate impact rows.
- The command does not open changed source files and does not require the changed file
  to exist. Changed-file inputs are matched only against repository-relative paths and
  source references already serialized in generated artifacts.
- Successful stdout must not serialize local absolute paths, raw command text, raw diff
  text, source bodies, document bodies, config values, generated-source contents,
  command logs, stack traces, credentials, tokens, or secret-looking values.
- `--from-git-diff`, standard-input diff parsing, raw diff-file input, Git working-tree
  inspection, branch comparison, commit comparison, and rename detection are parked
  until a later explicit path and command-behavior contract accepts them.

Required source artifacts for the first slice:

- `project-map.json` with no-adapter `schema_version: "1.0"`;
- `evidence-index.jsonl` with the documented evidence field set;
- `project-graph.json` with `graph_schema_version: "1.0"`.

The command fails closed with the query artifact error exit code when a required source
artifact is missing, malformed, unsafe, unsupported, has duplicate required IDs, has
unresolved graph node references, or has graph `evidence_ids` that do not resolve to
`evidence-index.jsonl`. The direct mapping foundation also fails closed when
`project-map.json` contains duplicate generated fact IDs or evidence references that do
not resolve to `evidence-index.jsonl`.

Out-of-scope source inputs for the first slice:

- adapter-enabled `project-map.json` `schema_version: "2.0"` sets;
- `source-registry.json` and adapter provenance;
- `workspace-map.json`;
- `endpoints.md`, `agent-guide.md`, generated profile Markdown, AI presentation
  artifacts, cache metadata, generated Markdown bodies, release notes, query output,
  downstream agent output, prompts, chat output, or LLM output.

Impact matching and projection behavior:

- Direct changed-file matches are created only when an input path equals an existing
  evidence `path`, a generated fact source reference path, or a graph node source path
  already present in accepted source artifacts.
- Generated fact source reference paths are accepted as direct matches only when the
  same path is also present in the fact or graph node's resolved evidence paths.
  Path-like fields without matching evidence remain generated metadata, not direct
  impact support.
- The direct mapping foundation renders `direct_match`, `not_represented`, and
  `diagnostic` rows. The conservative projection slice also renders `graph_neighbor`,
  `relation_status`, and `planning_hint` rows without changing the source artifact set.
- If no accepted artifact references a changed-file path, the output must report the
  file as not represented in generated memory. It must not infer hidden impact from
  package names, filenames, directory names, generated Markdown, adapter records, AI
  output, or query text.
- The start set for graph projection is the deterministic set of graph nodes that
  correspond to direct changed-file matches.
- Graph projection is one hop only from the start set. It may render incoming and
  outgoing graph edges, plus `relation_statuses[]` tied to start-set nodes. It has no
  depth flag in the first boundary.
- Graph projection must preserve graph edge `type`, `claim_category`,
  `relation_status`, `support_type`, `confidence`, `uncertainty`,
  `relation_attributes`, `derivation`, and `evidence_ids` as navigation metadata.
  `derivation` remains non-evidence.
- Structural `owns` and `declares` edges may orient the user to nearby emitted facts,
  but they are not proof of runtime reachability, dependency reachability, production
  impact, or complete ownership.
- Existing `quality.change_risk_signals` may be rendered only as planning hints tied
  to matched facts or graph nodes. They must not become coverage, CI, runtime,
  correctness, vulnerability, production-impact, or business-priority claims.
- The first boundary must apply deterministic caps. Initial caps are 256 changed-file
  inputs, 500 direct match rows, 1,000 graph projection rows, 256 planning-hint rows,
  and 256 diagnostics. Cap hits are diagnostics, not evidence.

Text output categories:

- `direct_match`: a changed file directly matches an existing evidence path, source
  reference, generated fact, or graph node.
- `graph_neighbor`: a one-hop graph edge from or to a direct-match graph node.
- `relation_status`: an existing graph relation-status row tied to a direct-match
  graph node.
- `planning_hint`: an existing quality/change-risk planning hint tied to a direct
  match or graph node.
- `not_represented`: a valid changed-file path has no representation in accepted
  generated memory.
- `diagnostic`: a bounded command, artifact, cap, duplicate, or unsupported-input
  diagnostic.

The first slice supports deterministic text stdout only. Exact text layout is not a
stable parser interface unless a later contract documents a specific structure. Stable
JSON stdout, generated Markdown, and `.project-memory/impact-report.json` remain
parked.

Confidence and uncertainty rules:

- Confidence labels describe support for the impact hint, not certainty of complete
  downstream impact.
- `high` is allowed for direct matches backed by an existing evidence path, existing
  fact source reference, or graph node derived from an extracted, spec-backed, or
  document-backed source fact.
- `medium` is allowed for one-hop graph neighbors when the graph edge or inferred
  relation support is already present in `project-graph.json`.
- `low` is required for status-only rows, uncertain document reconciliation rows,
  current `quality.change_risk_signals`, and structural orientation that should be read
  only as a planning hint.
- Missing uncertainty is represented as `null` where the underlying artifact uses JSON
  fields. Text output may render no uncertainty label for `null`, but must render
  explicit uncertainty/status labels when present.
- `not_represented`, unsupported input, unsupported schema, capped output, missing graph
  node, and not-analyzed relation/status cases must be explicit rather than silently
  omitted.

Exit-code rules:

- `0`: successful impact query, including valid inputs where every file is rendered as
  `not_represented`.
- `1`: unexpected internal error.
- `2`: usage error, including missing `--files`, empty file list, invalid changed-file
  path syntax, unknown flag, or unexpected argument shape.
- `3`: query input or artifact error, using the existing query artifact-error meaning
  for missing artifact roots, missing required artifacts, unsafe required artifact
  files, malformed JSON/JSONL, unsupported schema markers, duplicate required IDs,
  unresolved graph node references, or unresolved required evidence IDs.
- `4` and `5`: unchanged scan/workspace config and scan output generation meanings; the
  read-only impact query must not use them for successful read-only behavior.
- `6`: reserved for exact lookup no-result behavior in other query commands. A valid
  impact query with no represented changed files should render `not_represented` rows
  and exit `0`.

Workspace stance:

- Workspace impact is parked for the first v2.6 boundary.
- `workspace-map.json` remains workspace aggregation metadata and is not an impact
  input source.
- Per-member fan-out, workspace `query ... impact`, workspace graph output,
  cross-repo impact, and composite workspace impact references require a later explicit
  workspace query/impact contract before implementation.

Validation requirements before impact release:

- Focused CLI parser tests for accepted and rejected `query <path> impact --files`
  grammar.
- Changed-file path tests for relative, duplicate, unmatched, empty, absolute,
  `./`-prefixed, escaping, backslash, URL-like, generated-output, glob-like, and
  over-cap inputs.
- Artifact-loading tests for missing, malformed, unsafe, unsupported, duplicate-ID, and
  unresolved-reference `project-map.json`, `evidence-index.jsonl`, and
  `project-graph.json` inputs.
- Direct-match tests for evidence-path, source-reference, generated-fact, graph-node,
  and not-represented path cases.
- Projection tests for one-hop graph neighbors, incoming/outgoing orientation,
  relation-status rows, quality planning hints, deterministic sorting, and cap
  diagnostics.
- No-write tests proving impact queries do not scan source, create or mutate
  `.project-memory/`, refresh cache metadata, write impact reports, edit source files,
  edit repository docs, or modify configuration.
- Content-safety tests proving impact output does not serialize source bodies, local
  document bodies, config contents, generated-source contents, generated Markdown
  bodies, raw diff text, raw command logs, stack traces, local absolute paths,
  credentials, tokens, secret-looking values, downstream agent output, or LLM output.
- Regression tests proving existing `scan`, non-impact `query`, graph relation, and
  `workspace scan` behavior remain unchanged unless a later contract explicitly says
  otherwise.
- Risk-based review is required before release for implementation that changes query
  grammar, changed-file path handling, artifact reading, graph traversal, output
  rendering, diagnostics, evidence-reference rendering, query exit codes, or generated
  output write boundaries.

Stop conditions for implementation:

- Impact output implies complete impact analysis, runtime reachability, full dependency
  reachability, call graph coverage, source/spec agreement, documentation freshness,
  test coverage, CI status, assertion behavior, vulnerability, correctness,
  production impact, business priority, complete architecture ownership, or
  code-change authority.
- Changed-file path handling, artifact-root handling, graph projection, caps,
  confidence labels, uncertainty labels, or output authority cannot be made
  deterministic and explicit.
- Direct changed-file mapping would fabricate impact for files not represented by
  existing deterministic facts, evidence paths, source references, graph nodes,
  relation/status rows, or explicitly allowed planning hints.
- Graph projection cannot keep extracted, inferred, uncertain, document-backed,
  spec-backed, metadata-only, warning, not-analyzed, and structural categories
  distinct.
- Impact output would require source file readback, generated-source content scanning,
  build execution, runtime analysis, complete type solving, dependency resolution,
  adapter/source-registry joins, workspace relation inference, network access,
  connectors, optional AI, repository chat, generic RAG, semantic search, automatic
  code modification, release automation, or publication automation.

### v2.7 Policy Profile Contract

This section defines the accepted v2.7 boundary for policy profiles. A policy profile is
a local scan configuration preset and guardrail. It
is not an agent output profile, security certification, compliance mode, vulnerability
scanner, secret inventory, hosted policy service, enterprise policy enforcement system,
or complete safety proof.

Terminology and selector boundary:

- Use the public term `policy profile` for this scan configuration surface.
- Existing `scan <path> --agent-profile <profile>` selectors keep the v1.3 agent output
  profile meaning. They generate deterministic Markdown presentations under
  `.project-memory/agent-profiles/` and must not be used for policy profile selection.
- The CLI selector is a single optional
  `scan <path> --policy-profile <name>` flag.
- The root-local scan config selector is a single optional top-level
  `policy_profile: <name>` key in `agent-project-memory.yml`.
- The selector accepts only canonical names. Unknown names fail closed before
  output generation. Repeated CLI selectors are usage errors.
- The initial accepted policy profile names are `guarded-local`, `docs-focused`, and
  `adapter-local`.
- `strict`, `no-network`, `enterprise-local`, `oss`, `docs-heavy`, and
  `generated-source-enabled` are parked names. They either overstate guarantees, are too
  ambiguous for a first public contract, or imply behavior that is out of scope.

Default and precedence rules:

- A normal scan with no policy profile remains the default compatibility baseline. It
  preserves current local-first, no-default-network behavior and does not emit default
  policy metadata.
- Effective policy calculation is built-in defaults first, then the selected policy
  profile, then explicit root-local config keys, then explicit CLI flags.
- Explicit config keys or CLI flags may be stricter than the selected profile, but they
  must not weaken selected profile guardrails.
- If config and CLI both select a policy profile, the selected names must match exactly.
  A mismatch fails closed before output generation instead of silently replacing a
  repository-local policy choice.
- Policy profiles are single-selection presets. Composition, inheritance, aliases, and
  profile bundles are out of scope for the first boundary.

Initial profile behavior matrix:

| Profile | Purpose | Allowed optional surfaces | Rejected combinations |
| --- | --- | --- | --- |
| `guarded-local` | Keep the scan on the narrow built-in local-input path. | Built-in default-scope local Markdown; existing no-adapter scan behavior. | Adapters, AI presentation, user document include/exclude expansion, generated-source content scanning, symlink following, network access, credentials, telemetry, source upload. |
| `docs-focused` | Keep local-only analysis while allowing validated Markdown document refinement. | Built-in default-scope local Markdown plus validated Markdown-only `documents.include` and `documents.exclude` rules. | Adapters, AI presentation, generated-source content scanning, symlink following, network access, credentials, telemetry, source upload, non-Markdown document expansion. |
| `adapter-local` | Allow explicitly configured existing local import adapters under the current local-file validation rules. | At most one explicitly enabled existing local import adapter, plus normal local scan behavior. | Silent adapter enablement, network/API connectors, credentials, AI presentation, generated-source content scanning, symlink following, telemetry, source upload. |

These profiles do not enable an analyzer or adapter by themselves unless this table says
the selected profile allows the already documented explicit configuration surface. The
existing reserved config values `features.generated_sources: true` and
`features.follow_symlinks: true` remain invalid in v2.7.

Unsafe-combination behavior:

- Unsupported profile names, duplicated selectors, mismatched config and CLI selectors,
  and attempts to weaken selected profile guardrails fail closed before output
  generation.
- Policy profiles must not silently enable adapters, AI presentation, generated-source
  content scanning, symlink following, network access, credentials, telemetry,
  source upload, hosted policy management, server/API/editor/plugin runtime,
  repository chat, generic RAG, or automatic code modification.
- Redaction posture must not be weakened. Profile metadata, diagnostics, and conflict
  messages must not serialize raw config values, user include/exclude patterns, adapter
  import paths, source bodies, document bodies, generated-source contents, local
  absolute paths, command logs, stack traces, credentials, tokens, or secret-looking
  values.

`project-map.json` metadata shape when a policy profile is explicitly selected:

```json
{
  "schema_version": "1.0",
  "scan": {
    "policy_profile": {
      "analysis_status": "analyzed",
      "selected_profile": "guarded-local",
      "selection_source": "cli_override",
      "profile_version": "1.0",
      "authority": "local_configuration_preset",
      "evidence_policy": "execution_metadata_not_evidence",
      "conflict_policy": "fail_closed",
      "network_access": "disabled",
      "source_upload": "disabled",
      "credential_lookup": "disabled",
      "allowed_optional_surfaces": [
        "built_in_local_markdown"
      ],
      "rejected_optional_surfaces": [
        "adapters",
        "ai_presentation",
        "generated_source_content",
        "symlink_following"
      ]
    }
  }
}
```

Metadata rules:

- `scan.policy_profile` is emitted only when a policy profile is explicitly selected in
  the first boundary, unless a later compatibility decision accepts default metadata.
- `selected_profile` uses one of the canonical policy profile names.
- `selection_source` is one of `"config_file"`, `"cli_override"`, or
  `"config_file_and_cli_confirmed"`.
- `profile_version` is `"1.0"` for the first policy-profile metadata contract.
- `authority` is `"local_configuration_preset"`.
- `evidence_policy` is `"execution_metadata_not_evidence"`.
- `conflict_policy` is `"fail_closed"` for the first boundary.
- `network_access`, `source_upload`, and `credential_lookup` must be `"disabled"` for
  the accepted v2.7 profiles.
- Optional-surface arrays use bounded enum-like values only. They must not contain raw
  config values, paths, include/exclude rules, adapter import paths, provider names,
  local absolute paths, credentials, tokens, command text, or source excerpts.
- Selected policy metadata is additive scan execution metadata and does not require a
  `project-map.json` schema marker migration by itself. If `adapter-local` is selected
  and an existing local import adapter is explicitly enabled, the adapter context still
  follows the existing v2 adapter schema-marker rules.
- Consumers that do not understand `scan.policy_profile` may ignore it. Consumers must
  not treat policy profile metadata as evidence, proof of compliance, complete safety,
  vulnerability scanning, credential scanning, or security correctness.

Evidence and generated artifact decisions:

- Policy profiles do not add generated artifacts in the first boundary.
- Policy profiles do not create `evidence-index.jsonl` records, add evidence fields,
  add evidence types, create confidence labels, reinterpret evidence IDs, or reuse
  `config_file` evidence for the tool config.
- Policy metadata must not be referenced from `evidence_ids`.
- Agent profile artifacts, AI presentation artifacts, cache metadata, graph derivation,
  query output, adapter provenance, release notes, and downstream agent output remain
  non-evidence and cannot be used to satisfy policy profile evidence requirements.

Validation requirements:

- Focused CLI/config tests for accepted profile names, unsupported names, duplicated
  selectors, config selection, CLI selection, matching config-plus-CLI selection, and
  mismatched config-plus-CLI failure.
- Profile matrix tests for `guarded-local`, `docs-focused`, `adapter-local`, and the
  no-profile baseline.
- Unsafe-combination tests for adapters, AI presentation, reserved generated-source and
  symlink modes, local document include/exclude expansion, network/credential/source
  upload defaults, and redaction-sensitive diagnostics.
- Golden or structured output tests if `scan.policy_profile` metadata is emitted.
- Regression tests proving no-profile scans remain stable if the byte-stability
  compatibility decision is retained.
- Evidence tests proving policy profiles do not create evidence records, evidence
  fields, evidence types, or tool-config evidence.
- Risk-based review is required before release for implementation that changes CLI or
  config parsing, precedence, path or filesystem handling, adapter allowance,
  generated-output rendering, evidence serialization, redaction, diagnostics, network or
  credential posture, or output write boundaries.

Stop conditions for implementation:

- Policy wording or names imply security certification, compliance, enterprise
  enforcement, vulnerability scanning, secret inventory, production correctness, or
  complete safety.
- Defaults stop being local-first, no-network, no-source-upload, deterministic, and
  compatible for no-profile scans.
- Precedence, conflict behavior, output metadata authority, or evidence semantics
  cannot be made explicit and fail-closed.
- Profile behavior would require hosted policy management, remote configuration,
  user-home policy discovery, organization crawling, background sync, telemetry, update
  checks, credentials, network calls, provider AI, plugin loading, server/API/editor
  runtime, repository chat, generic RAG, generated-source content scanning, symlink
  following, source upload, release automation, package publication, or automatic code
  modification.

### v1.7 Redaction And Security Hardening Contract

This section records the v1.7 output-safety boundary. The v1.7.0 release artifacts
apply the initial redaction policy described here. Existing released
artifacts before this implementation are not guaranteed to have been generated with
this redaction policy.

Design decision:

- Redaction is deterministic output hardening for selected generated and rendered
  strings. It is not a repository-wide secret scan, vulnerability scanner, secret
  inventory, credential classifier, or proof that all secrets were found.
- The redaction marker is the exact plain text
  `[REDACTED_SECRET_LIKE_VALUE]`.
- The marker is stored or rendered inside existing string fields, most often evidence
  `excerpt` or generated Markdown/query text. The initial v1.7 design does not add a
  `redacted` boolean, new evidence fields, new evidence types, a new `project-map.json`
  schema marker, or a new `graph_schema_version` by itself.
- Evidence IDs, normalized repository-relative paths, class names, method names,
  symbol names, fact IDs, graph IDs, line ranges, confidence labels, uncertainty
  labels, relation statuses, claim categories, schema markers, and enum-like contract
  values should remain unredacted unless this contract explicitly defines a safe
  replacement. These values are needed for evidence navigation and deterministic joins.
  For fields whose join keys are derived from local Markdown heading text, the v1.7
  contract derives the key from the redaction-safe heading text before percent-encoding
  so raw secret-looking heading values do not leak through IDs, anchors, graph source
  references, or evidence symbol keys.

Secret-looking value policy:

- In scope are obvious output strings where a bounded value is associated with a
  credential-like key or header, such as password, token, secret, credential, private
  key, API key, client secret, access key, authorization, bearer, or basic credential
  forms.
- Authorization header or key/value forms must redact credentials for standard and
  non-standard schemes as well as schemeless authorization values.
- In scope are obvious private-key material markers when such material appears inside a
  selected excerpt or rendered output string.
- The redaction primitive should mask the value portion when the boundary is clear,
  preserving safe context such as the key name, delimiter, annotation symbol, element
  name, source path, or line range. When the value boundary is unclear, the whole
  selected excerpt or rendered value may be replaced by the marker.
- Multiple secret-looking values in one selected string should all be masked. The
  output must not include a hash, digest, prefix, suffix, length, or reversible
  transform of the masked value.
- Out of scope are entropy-only detection, unlabeled secrets, split or obfuscated
  secrets, semantic classification of credentials, validation against providers,
  claims that a value is active, and claims that a repository contains no secrets.

Generation-time handling:

- New scan-generated artifacts apply the shared redaction policy before writing
  selected source-derived free text or artifact-derived free text to generated JSON and
  Markdown surfaces. The initial implementation covers evidence excerpts,
  `project-map.json`, `project-graph.json`, `evidence-index.jsonl`, `endpoints.md`,
  `agent-guide.md`, selected profile Markdown derived from generated artifacts, and
  bounded CLI error text.
- Evidence excerpt construction should redact before final excerpt bounding and should
  still enforce the existing excerpt length and output escaping limits after redaction.
- Structured fields that already avoid values, such as schema markers, enum-like
  statuses, confidence labels, normalized repository-relative paths, and evidence ID
  references, should not be passed through a value-redaction step that would make them
  unstable or non-joinable. Structured IDs that intentionally include bounded
  source-derived free text, such as local Markdown heading IDs and related evidence
  keys, must derive that key from the redaction-safe value before serialization.

Query render-time handling:

- Query output applies the same redaction policy while rendering stdout or stderr,
  including `explain evidence` output for existing older artifacts that may contain
  unredacted excerpts.
- Query render-time redaction is presentation-only. It must not rewrite, repair,
  mutate, normalize in place, or regenerate `project-map.json`, `project-graph.json`,
  `evidence-index.jsonl`, generated Markdown, profile artifacts, or cache metadata.
- Future stable JSON query output, if implemented later, must apply the same redaction
  policy before serializing result strings.

Path, symlink, and hardlink audit matrix:

| Surface | Target output/path policy |
| --- | --- |
| Scan root | Resolve one local directory and keep generated outputs under the canonical scan root. |
| Output directory and files | Keep `.project-memory/` under the scan root; reject unsafe symlink, hardlink, or escaping output paths. |
| Root-local scan config | Accept only one bounded YAML file under the scan root; reject absolute, escaping, generated-output, symlinked, multi-link, or link-count-unverifiable config paths. |
| Java, Maven, Gradle, resource, and API-spec inputs | Read only documented local input classes through bounded parser policies, normalized repository-relative paths, no symlink following, and verified single-link regular-file checks. |
| Local Markdown documents | Keep default safety exclusions, local Markdown-only user rules, aggregate caps, no symlink following, and verified single-link regular-file checks. |
| Generated-source metadata | Keep generated-source roots as path-presence metadata only; do not read generated-source contents by default. |
| Cache metadata | Keep cache files under `.project-memory/cache/v1/`; fail closed on unsafe, stale, corrupt, mismatched, symlinked, hardlinked, or inconsistent cache state. |
| Graph output | Generate navigation metadata from existing facts, evidence IDs, relation/status rows, and derivation metadata only. |
| Agent profile output | Render selected deterministic Markdown from existing structured facts and evidence references only. |
| Query artifact root and files | Read only direct child artifacts required by the query command; resolve accepted query directories canonically; reject symlinked artifact roots and symlinked, multi-link, or link-count-unverifiable required artifact files; reject unsafe evidence paths and unresolved project-map evidence references; never write during query. |
| CLI stdout and stderr | Keep messages deterministic and bounded; do not print stack traces, local absolute paths, raw command text, source bodies, document bodies, config contents, generated-source contents, credentials, tokens, or secret-looking values. |

Validation requirements before v1.7 release:

- Focused redaction/excerpt tests using fake sentinel values only.
- Output serialization and generated Markdown tests proving the marker appears where a
  fake secret-looking value would otherwise be emitted and that evidence IDs, paths,
  symbols, line ranges, confidence, uncertainty, and relation/status labels remain
  useful.
- Query rendering tests proving older unredacted artifact excerpts are masked in stdout
  without mutating artifact files.
- Surface regression checks for `project-map.json`, `project-graph.json`,
  `evidence-index.jsonl`, `endpoints.md`, `agent-guide.md`, selected profile Markdown,
  cache metadata, scan stdout/stderr, and query stdout/stderr.
- Path/symlink audit coverage for scan inputs, output writes, config selection, local
  Markdown discovery, generated-source metadata, cache metadata, and query artifacts.

Stop conditions for implementation:

- The design or implementation starts promising complete secret detection, a secret
  inventory, vulnerability findings, credential validity, or security correctness.
- Redaction would destroy evidence navigation by masking IDs, paths, symbols, line
  ranges, confidence, uncertainty, relation statuses, or claim categories needed to
  inspect a generated fact.
- The implementation requires generated-source content scanning, default symlink
  following, environment-variable interpolation, config value extraction, network
  access, connector credentials, telemetry, SaaS, web UI, repository chat, generic RAG,
  LLM calls, automatic code modification, or broad filesystem traversal.

## `evidence-index.jsonl`

`evidence-index.jsonl` is newline-delimited JSON. Each line is one evidence record.
The implementation emits a stable field order:

```json
{"id":"ev:src/main/java/com/example/orders/OrderController.java:18-18:com.example.orders.OrderController:@RestController","source_type":"annotation","path":"src/main/java/com/example/orders/OrderController.java","class_name":"com.example.orders.OrderController","method_name":null,"symbol_name":"@RestController","line_start":18,"line_end":18,"excerpt":"@RestController","confidence":"high"}
{"id":"ev:src/main/java/com/example/orders/OrderController.java:20-20:com.example.orders.OrderController#getOrder:@GetMapping","source_type":"annotation","path":"src/main/java/com/example/orders/OrderController.java","class_name":"com.example.orders.OrderController","method_name":"getOrder","symbol_name":"@GetMapping","line_start":20,"line_end":20,"excerpt":"@GetMapping(\"/orders/{id}\")","confidence":"high"}
```

Evidence entries should follow `docs/architecture/EVIDENCE_MODEL.md`.

The current implementation emits:

- `build_file` evidence for root `pom.xml` when present.
- `annotation` evidence for extracted Spring MVC controller, endpoint, request parameter,
  and request body annotations.
- Source-visible interface-declared endpoint mappings reuse existing `annotation`
  evidence for interface mapping annotations and existing `code_symbol` evidence for
  interface and concrete handler symbols needed to prove the unique binding. No new
  evidence fields are required.
- `annotation` evidence for direct supported Spring component stereotype annotations on
  Java class or interface declarations. `@Controller` and `@RestController` evidence IDs
  use the same annotation ID convention as endpoint evidence so the same source
  annotation is not duplicated in `evidence-index.jsonl`.
- `annotation` evidence for direct `@Repository` Spring application surface repository
  stereotype facts. When the same source annotation also supports a component fact, the
  evidence ID resolves to the same `evidence-index.jsonl` record.
- `code_symbol` evidence for inferred Spring Data repository interface extension
  signals. The current repository slice emits one evidence record for the source-visible
  interface declaration and one `extends:<fully-qualified-spring-data-base-type>`
  evidence record for each supported directly visible Spring Data base type that led to
  the signal.
- `annotation` evidence for direct `@Configuration`, `@ConfigurationProperties`, and
  `@Bean` Spring application surface facts. When the same source-visible
  `@Configuration` annotation also supports a component fact, both facts reference the
  same evidence ID and `evidence-index.jsonl` emits a single record. Current
  configuration-properties facts do not emit `prefix` or `value` fields, and current
  bean method facts do not emit bean names, scopes, lifecycle, return type, parameter, or
  dependency graph facts.
- `annotation` evidence for direct `@Transactional`, `@Scheduled`, `@EventListener`,
  and Kafka/Rabbit listener annotation Spring application surface facts. Current
  behavior/messaging evidence excerpts record annotation symbols only for these facts
  and do not serialize destination-like messaging annotation values such as topics,
  queues, exchanges, routing keys, or group IDs.
- `annotation` evidence for direct JPA annotations that support entity facts, including
  class-level `@Entity`, class-level `@Table`, field-level `@Id`, and field-level
  `@Column`, `@Enumerated`, `@GeneratedValue`, `@Version`, and relationship annotations
  `@ManyToOne`, `@OneToMany`, `@OneToOne`, and `@ManyToMany`.
  Field-level evidence IDs include a `field:<field_name>` discriminator because the
  current evidence record field set does not add a separate field-name property.
- `test_file` evidence for emitted test-like Java class declarations under supported
  test roots.
- `code_symbol` evidence for production class declarations that are referenced by
  inferred or ambiguous `tested_subjects` relation/status rows.
- `code_symbol` evidence for directly visible test framework imports attached to
  top-level emitted test classes.
- `annotation` evidence for directly visible test framework annotations.
- `api_spec` evidence for local OpenAPI/Swagger spec file facts and extracted operation
  facts. The evidence path is the normalized repository-relative spec path,
  `symbol_name` is the bounded spec observation such as `openapi` or `swagger`, an
  operation symbol such as `operation:get:/orders/{id}`, or a bounded parser status
  symbol for invalid/unsupported specs. Line fields point to the directly visible
  version signal or operation line when available and are `null` when stable line mapping
  is unavailable. Excerpts are bounded observations rather than full spec content.
- Warning evidence for hidden HTTP surface signals:
  - `config_file` evidence for OpenAPI/Swagger spec filename presence, with the spec
    path as `path`, the filename as `symbol_name`, nullable line fields, and a bounded
    excerpt such as `filename detected: openapi.yml`. The scanner does not parse the
    file content.
  - `build_file` evidence for deterministic OpenAPI/Swagger Maven code generation
    plugin declarations in the root `pom.xml`, with the plugin artifact ID as
    `symbol_name` and the matching artifactId line as the excerpt.
  - `annotation` evidence for direct source-visible `@RepositoryRestResource`
    annotations.

Mapped-superclass identifier facts do not add new evidence fields. When an identifier
field uses `source_kind` set to `"mapped_superclass"`, it uses the existing `annotation`
evidence shape for the field-level `@Id` declaration and the class-level
`@MappedSuperclass` declaration on `declaring_class`, including when that declaring class
is reached through a conservative source-visible mapped-superclass chain.

v0.2 Maven module discovery also does not add new evidence fields. Root
`<modules>` entries and child POM files reuse `build_file` evidence:

- Root `<module>` declaration evidence uses `path: "pom.xml"`, `source_type:
  "build_file"`, and a `symbol_name` derived from the normalized module path such as
  `module:services/orders` when the declaration is valid.
- Child POM evidence uses the repository-relative child POM path such as
  `services/orders/pom.xml`, `source_type: "build_file"`, and `symbol_name: "pom.xml"`.
- Module discovery evidence supports only deterministic local POM observations. It does
  not prove effective POM contents, Maven profile activation, dependency graphs, generated
  sources, or runtime Spring behavior.
- Module discovery evidence paths must remain normalized repository-relative paths and
  must not be absolute, start with `./`, or escape the scanned repository root.
- The current v0.3 Maven metadata analyzer also reuses `build_file` evidence for
  direct source-visible module metadata and parent coordinate elements. Metadata evidence
  uses `symbol_name` values such as `maven:project:artifactId` or
  `maven:parent:version`, points to the module POM path, and supports only the direct POM
  text. It does not prove Maven defaults, inherited coordinates, profile activation, or
  effective POM values.
- The current v0.3 dependency analyzer reuses `build_file` evidence for direct
  source-visible dependency declarations, dependency-management declarations, and their
  directly declared coordinate, `scope`, `optional`, `type`, and `classifier` elements.
  Dependency evidence uses `symbol_name` values such as
  `maven:dependency:000001:artifactId` or
  `maven:dependency_management:000001:version`, points to the module POM path, and
  supports only direct POM text. It does not prove resolved versions, inherited or
  managed values, transitive dependencies, active profiles, repository availability, or
  effective dependency graphs.
- The current v0.3 plugin analyzer reuses `build_file` evidence for direct
  source-visible plugin declarations, plugin-management declarations, directly declared
  plugin coordinates, direct execution IDs/phases/goals, bounded configuration signal
  elements, and plugin-derived generator signals. Plugin declaration and execution
  evidence excerpts identify the bounded declaration only; bounded configuration signal
  evidence excerpts identify the signal element name and must not include arbitrary
  plugin configuration values. Plugin evidence does not prove resolved plugin versions,
  Maven lifecycle execution, inherited executions, generated source contents, OpenAPI
  operations, endpoint facts, active profiles, repository availability, or effective POM
  behavior.
- v0.4 API surface evidence adds `api_spec` evidence for local OpenAPI/Swagger spec
  files and extracted operation facts, plus `path_signal` evidence for generated-source
  path signals. Current `api_spec` evidence supports local declared API input and
  operation facts only; it does not prove source-visible endpoint implementation,
  generated source contents, or runtime behavior. `path_signal` evidence supports
  path-presence warnings only; it will not prove generated source contents or generated
  API operations. `api_spec`
  evidence IDs use
  `ev:<spec_path_key>:<line_range_key>:api_spec:<api_spec_symbol_key>`, where
  `line_range_key` is `<line_start>-<line_end>` when stable and `unknown` otherwise, and
  `api_spec_symbol_key` uses the same percent-encoded ID-key rules as v0.4 spec and
  operation fact IDs. Duplicate evidence ID collisions must be resolved with a
  deterministic `decl:<zero-padded-ordinal>` suffix or degraded to warnings.
- Current v0.8 local document ingestion emits `document` evidence for default-scope
  local Markdown file observations, headings, bounded chunk references, and
  reconciliation document-side mention observations. Document evidence supports document
  inventory and document-backed uncertain signals only. It must not be used as code,
  build, config, test, API-spec, runtime, or generated-output evidence.

Evidence entries are sorted deterministically by path, line range, class, method, symbol,
and ID. Nullable fields are emitted as JSON `null`; absent repeated values are emitted as
empty arrays in `project-map.json`.

## `endpoints.md`

`endpoints.md` is a human-readable endpoint inventory generated from deterministic endpoint facts.

It should include:

- HTTP method.
- Path.
- Controller class.
- Handler method.
- Mapping source when available.
- Request body type when detected.
- Response type when detected.
- Evidence reference.

If a `@RequestMapping` endpoint does not declare an HTTP method, the Markdown output
must say that the method was not declared instead of inventing one. If a method
expression is present but unsupported by deterministic source extraction, the output
must mark it as unsupported.

For interface-declared mappings, `endpoints.md` should say that the mapping source is a
source-visible interface method and name the interface method when `mapping_source` is
available. It must not describe generated OpenAPI operations, generated `*Api`
interfaces, or runtime handler mappings unless the source-visible Java interface is
present under supported production source roots and represented in `project-map.json`
with resolving evidence.

Example shape:

```md
# Endpoints

## GET /orders/{id}

- Controller: `com.example.orders.OrderController`
- Handler: `getOrder`
- Mapping source: `direct_handler_method`
- Response: `com.example.orders.OrderDto`
- Evidence: `src/main/java/com/example/orders/OrderController.java:20`
```

Current v0.2 `endpoints.md` behavior:

- Endpoint sections should be grouped by module in deterministic module order.
- The single-module root group may be omitted or rendered as the scan root when there is
  only `module:.`, but endpoint content must still resolve from module-aware
  `project-map.json` facts.
- Multi-module endpoint entries should include the module identity or module path near
  the endpoint heading or metadata.
- Module grouping must not claim architectural layers, service ownership, bounded
  contexts, or runtime routing behavior beyond the module identity recorded in
  `project-map.json`.

Current v0.4 `endpoints.md` behavior:

- The filename remains `endpoints.md`, but the content renders distinct API
  surface sections.
- Source-visible Spring MVC endpoint entries render from top-level `endpoints[]`.
- Direct handler mappings and source-visible interface-declared mappings are
  visibly separated or labeled by `api_surface_category` and `mapping_source.kind`.
- Declared OpenAPI operations render from `api_surface.openapi.operations.items[]` under
  a separate `Declared OpenAPI Operations` section.
- OpenAPI operation entries must use `Declared` or `Spec-backed` wording. They must not
  use `Detected endpoint`, `Implemented`, or other wording that implies runtime handler
  implementation.
- Generated-source API signals, repository-rest warnings, and hidden HTTP warnings
  render as warnings with category, signal, source path, and evidence references. They
  must not be rendered as endpoint or operation facts.
- A future endpoint/spec relation, if introduced, must be rendered as a separate
  evidence-backed relation and must not merge source-visible endpoint rows with
  spec-backed operation rows.

## `agent-guide.md`

`agent-guide.md` is a concise orientation file for AI coding agents and developers.

It is generated from `project-map.json` and `evidence-index.jsonl`, or from the same
structured in-memory facts that are serialized to those files. The guide generator must
not walk source files, call LLMs, call external services, directly ingest local
documentation, or invent architecture not represented by deterministic facts. Local
documentation guide rendering must use the structured `documents` facts and resolving
evidence already produced by the deterministic analyzer pipeline.

The minimal stable v0.1 section order is:

```md
# Agent Guide

Generated deterministically from `project-map.json` and `evidence-index.jsonl`.

## Detected Project Layout
## Detected Spring MVC Endpoints
## Detected Spring Components
## Detected JPA Entities
## Detected Tests
## Known Uncertainty And Limits
## Practical Inspection Order For Coding Agents
```

Content rules:

- The project layout section reports the detected build system, root build file, source
  roots, and test roots from `project-map.json`.
- Evidence-backed entries render readable evidence references by resolving
  `evidence_ids` through `evidence-index.jsonl`. References should include a source
  location such as `path:line` or `path:start-end` plus the evidence ID.
- Long Markdown evidence-reference lists are presentation-capped to keep the guide
  concise. When a list is capped, the guide must keep the first evidence references
  inline and add a suffix such as `... and N more evidence references in
  evidence-index.jsonl`. This does not remove or alter complete evidence records in
  `evidence-index.jsonl` or evidence IDs in `project-map.json`.
- Facts without dedicated evidence IDs, such as current source-root and test-root lists,
  must say that they are recorded in `project-map.json` and that no separate evidence ID
  is emitted by the current implementation.
- Endpoint entries use cautious `Detected` wording and include controller class, handler
  method, HTTP method status, paths, request parameters, request body, response type, and
  evidence references. When `mapping_source` is available, endpoint entries should state
  whether the mapping came from a direct handler method or from a uniquely bound
  source-visible interface method, without claiming complete runtime handler mapping
  behavior.
- Hidden HTTP surface warnings, when present, are rendered in the known-limits section
  with `Warning` wording, the warning category, signal, source path, deterministic
  message, and resolving evidence references. They must not be rendered as detected
  endpoint facts.
- Component entries use `Detected` wording and include direct stereotype annotations and
  evidence references. They must not claim Spring runtime wiring, component scanning,
  lifecycle, scopes, bean names, or dependency graphs.
- Entity entries use `Detected` wording for direct entity, table, and identifier facts.
  Identifier entries should include `declaring_class` and `source_kind` when that context
  matters. Mapped-superclass identifiers must be described as direct source-visible
  mapped-superclass facts, not as full ORM inheritance reconstruction. Relationship
  entries must preserve `relationship.target.target_resolution: declared_type_only` and
  `relationship.target.uncertainty: target_type_not_resolved` until a future target-link
  implementation supports otherwise, and should present relationship targets as
  `Uncertain`, not resolved entity links.
- Test entries use `Detected` wording for test classes and directly visible framework
  signals. `tested_subjects` entries must use `Inferred` wording only for inferred rows,
  render status-only rows as statuses, and show `relation_status`, `relation_type`,
  `support_type`, `confidence`, `candidate_reference`, and `uncertainty` when present.
- The known-limits section must explicitly call out `Not analyzed`, `Inferred`, and
  `Uncertain` areas, including Spring runtime behavior, ORM runtime behavior, test
  execution/coverage/assertion behavior, call graphs, complete subject mapping,
  connectors, LLM summaries, repository chat, generic RAG, Gradle execution, dynamic
  buildscript evaluation, effective Gradle model reconstruction, Kotlin source
  analysis, Maven profiles, effective POM reconstruction, dependency graphs, and
  recursive nested Maven modules. It should also call out that generated sources, OpenAPI operations, generated API
  reconstruction, classpath-only interfaces, and ambiguous interface endpoint bindings
  are not analyzed for the v0.1 interface-mapping decision, and that mapped-superclass identifier
  traversal skips unresolved, ambiguous, cyclic, and non-source-visible branches.
- The practical inspection order may suggest evidence paths from generated facts, but it
  must not introduce unsupported architecture, modules, domain flows, service layers, or
  source summaries. Long inline evidence path lists should be capped with a suffix that
  points readers back to `evidence-index.jsonl` for the complete source-backed evidence.

Current v0.2 `agent-guide.md` behavior:

- The detected project layout section should summarize `project.modules.items` in
  deterministic module order, including `module_id`, `module_path`, `pom_path`,
  `support_status`, source roots, test roots, and resolving evidence where available.
- Endpoint, component, entity, test, and warning sections should group or label facts by
  module using the module identity from `project-map.json`.
- Module warnings should appear in the known-limits section with `Warning` wording and
  resolving evidence references. They must not be rendered as application facts.
- The practical inspection order may use module paths as navigation hints, but it must
  not infer dependency direction, runtime Spring boundaries, ownership, generated API
  contents, or cross-module architecture unless future deterministic facts explicitly
  support those claims.

Current v0.3 build/config behavior:

- `project-map.json` includes module-owned Maven metadata under
  `project.modules.items[].build_config.maven.metadata`.
- `project-map.json` includes module-owned source-visible Maven dependency declarations
  under `project.modules.items[].build_config.maven.dependencies` and separate
  management declarations under
  `project.modules.items[].build_config.maven.dependency_management`.
- `project-map.json` includes module-owned source-visible Maven plugin declarations
  under `project.modules.items[].build_config.maven.plugins` and separate
  plugin-management declarations under
  `project.modules.items[].build_config.maven.plugin_management`.
- `project-map.json` includes module-owned standard resource roots under
  `project.modules.items[].build_config.resources` and path-only supported
  application/logging config-file inventory under
  `project.modules.items[].build_config.config_files`.
- `project-map.json` includes module-owned direct source-visible Spring Boot application
  signals under `project.modules.items[].build_config.spring_boot_applications`.
- Plugin-derived generated-source warnings are rendered in the known-limits section as
  warning facts and must not be rendered as endpoint, API-operation, or generated-source
  facts.
- `agent-guide.md` includes a dedicated `Build And Configuration Orientation` section
  generated from structured build/config facts only. It renders Maven metadata,
  dependencies, dependency-management declarations, plugins, plugin-management
  declarations, resource roots, config file paths, Spring Boot application signals, and
  concise module warning summaries without interpreting config values or claiming
  effective/resolved/runtime/generated behavior.

Current v0.3 `agent-guide.md` behavior:

- The guide should add a `Build And Configuration Orientation` section generated from
  v0.3 `build_config` facts.
- Maven metadata, dependency, and plugin sections must use `Source-visible` wording and
  must not claim effective POM coordinates, inherited values, transitive dependencies,
  resolved plugin versions, lifecycle execution, profile activation, or remote
  repository availability.
- Dependency-management and plugin-management declarations must be labeled as management
  declarations, not as active dependencies or active plugin executions.
- Resource and config summaries may list detected resource roots and config file paths,
  config kind, format, and filename-derived profile name. They must not print config
  file contents, property keys, property values, YAML node content, XML element content,
  environment placeholders, decrypted secrets, or config excerpts.
- Spring Boot application entries must use `Detected` wording for direct
  `@SpringBootApplication` and source-visible `main` method signals. They must not claim
  executable packaging, active profiles, runtime auto-configuration, bean graph,
  component scanning result, deployment behavior, or actual process entrypoint behavior.
- Generated-source, OpenAPI/Swagger, annotation-processor, and generator plugin signals
  should appear as warnings or known limits. They must not be rendered as detected
  endpoints, generated APIs, implemented API operations, or generated source contents.
- The known-limits section should explicitly state that v0.3 build/config facts are
  direct local source observations only, and that Maven execution, effective POM
  reconstruction, profile activation, remote dependency resolution, config value
  interpretation, secret extraction, and default generated-source scanning are not
  performed.

Current v0.4 API surface `agent-guide.md` behavior:

- The guide includes an `API Surface Interpretation` section generated from structured
  API surface facts and evidence only.
- The section distinguishes code-backed source-visible Spring MVC endpoint facts,
  code-backed source-visible interface-declared endpoint facts, spec-file declared API
  input facts, spec-backed OpenAPI operation facts, generated-source API warnings,
  repository-rest warnings, and hidden HTTP warnings.
- Source-visible Spring MVC entries may be described as detected endpoint facts only
  when they come from `endpoints[]`.
- OpenAPI/Swagger spec-file entries must be described as declared API inputs, not as
  parsed operations or implemented endpoints.
- OpenAPI operation entries must be described as declared/spec-backed operations, not
  implemented endpoints.
- Generated-source API signals must be described as warnings until explicit
  generated-source scanning is designed and enabled.
- Repository-rest and hidden HTTP warnings must be described as inspection hints, not
  detected endpoint facts.
- The guide must not claim runtime handler mappings, implementation coverage,
  source/spec agreement, service ownership, generated source contents, generated client
  SDKs, or OpenAPI/runtime agreement unless future deterministic relations explicitly
  support those claims.

Current Spring application surface `agent-guide.md` behavior:

- The guide includes a `Spring Application Surface` section generated from structured
  `spring_application_surface` facts and resolving evidence only.
- The section is grouped by module using module identity from `project-map.json`. Inside
  each module group, extracted facts, inferred signals, inferred repository/entity
  relations, explicit uncertain/not-analyzed statuses, and warnings are rendered as
  separate categories when present.
- Repository stereotype entries should be described as direct annotation observations.
- Spring Data repository interface signals should be described as inferred
  source-visible signals. Repository/entity relation rows, when present, should be
  described as inferred generic links. Neither category must be described as runtime
  repositories, entity ownership, query method behavior, database access facts, or
  runtime repository/entity verification.
- Configuration classes, configuration properties, and bean methods are described as
  source-visible Spring configuration signals. They must not claim runtime bean graph,
  conditional activation, active profiles, config binding success, config values, bean
  scopes, lifecycle, proxy behavior, or dependency graphs.
- Explicit status fields such as non-inferred `entity_relation_status` values,
  `binding_status: "not_analyzed"`, and `bean_name_status: "not_analyzed"` are rendered
  as uncertain or not-analyzed orientation signals, not as runtime relation, binding, or
  bean-name facts. `entity_relation_status: "inferred"` is rendered as an inferred
  source-visible Spring Data generic relation.
- Transaction, scheduled, event listener, and messaging listener annotations are
  described as operational change-surface signals. They must not claim runtime
  transaction behavior, transaction propagation, scheduler registration, scheduler
  frequency, event delivery, message destinations, message topology, queue/topic
  existence, consumer groups, delivery semantics, or broker behavior.
- Spring Security configuration warnings are described as inspection hints and
  change-risk signals. Empty security warning collections under an `"analyzed"` security
  subsection mean no bounded supported source-visible security configuration warning was
  emitted; they do not prove the absence of security configuration outside the supported
  analyzer scope. Security warning guidance must not claim security policy, endpoint
  protection, authentication behavior, authorization behavior, vulnerability, or
  correctness.

Current v0.6 JPA/domain `agent-guide.md` behavior:

- The guide may expand `Detected JPA Entities` or add a concise `Domain And Data Model`
  section generated from structured `entities` facts, repository/entity relation
  statuses, and resolving evidence only.
- Entity and embeddable entries must be grouped or labeled by module using
  `module_id`. Embeddables must be described as `@Embeddable` source-visible types, not
  as tables or standalone entities.
- Entity field metadata should use `Source-visible` or `Detected` wording for direct
  JPA annotations and must not claim complete persistent-property inventory, runtime
  access strategy, database columns, table existence, generated IDs, optimistic-locking
  correctness, or provider defaults.
- Identifier and embedded-id entries must show the explicit source-visible support
  boundary, such as simple `@Id`, mapped-superclass `@Id`, `@EmbeddedId`, or `@IdClass`
  signal. `@IdClass` must be rendered as a composite-id signal, not reconstructed
  composite-key semantics.
- Relationship entries must separate direct relationship annotation facts from inferred
  target links. Declared-type-only relationships should remain `Uncertain`; unique
  source-visible entity target links should be rendered as `Inferred`; ambiguous or
  unsupported targets should show the corresponding uncertainty/status.
- Relationship metadata such as `mappedBy`, join columns, join tables, optional, fetch,
  cascade, and orphan-removal values must be described as source-visible annotation
  attributes only. The guide must not claim ORM ownership correctness, foreign keys,
  database constraints, join-table existence, fetch behavior, cascade behavior, or
  runtime provider behavior.
- Repository/entity links should be rendered inside or near the Spring/Data/domain
  guidance as inferred Spring Data generic relations. The guide must show relation
  status values such as `inferred`, `not_detected`, `ambiguous`, `unsupported`, or
  `not_analyzed` without describing them as runtime repositories, query semantics, or
  database access facts.
- When no entity facts, embeddable facts, entity relationship facts, or inferred
  repository/entity relation objects are present, the guide omits the full
  `Detected JPA Entities` section and does not add a persistence inspection-order step
  that only reports no evidence paths. Domain-bearing outputs still render the JPA/domain
  section and persistence inspection guidance.
- The known-limits section should explicitly state that v0.6 JPA/domain facts do not
  perform database introspection, runtime Hibernate metadata analysis, DDL
  reconstruction, JPQL semantic parsing, migration interpretation, complete ORM model
  reconstruction, or runtime repository/entity verification.

Current v0.7 tests inventory `agent-guide.md` behavior:

- The guide expands `Detected Tests` from structured `tests` facts only.
- Test class and method entries must use `Detected` wording for direct source-visible
  test declarations and supported test annotations. Method entries must not describe
  assertions, expected behavior, execution status, pass/fail status, or coverage.
- Framework signal entries render direct source-visible framework classifications with
  `signal_kind` and evidence. They must not claim runtime engine execution, Spring
  context startup, CI behavior, assertion behavior, or coverage.
- Spring test slice entries render direct source-visible annotation classifications with
  `slice_kind`, `signal_kind`, and evidence. They must not claim runtime Spring context
  startup, bean graph contents, MockMvc setup, database access, or slice correctness.
- Mock annotation signal entries render direct source-visible annotation classifications
  with `mock_signal`, `signal_kind`, target kind/name, and evidence. They must not claim
  runtime Spring bean override behavior, Mockito behavior, bean graph contents, database
  access, or slice correctness.
- Tested-subject rows use `Inferred` wording only when `relation_status` is
  `"inferred"`. Ambiguous rows should be rendered as ambiguous candidates, and
  non-inferred status-only rows such as `"not_detected"` and `"unsupported"` should be
  rendered as tested-subject statuses with explicit wording that no coverage, execution,
  or runtime relation is claimed.
- Tested-subject guide rows must show `relation_status`, `relation_type`,
  `support_type`, `confidence`, `candidate_reference`, and `uncertainty` when present.
- Quality test-gap and change-risk rows render as conservative planning hints with
  status, basis, confidence, uncertainty, subject, module, and evidence. They must not
  claim coverage, execution, assertion behavior, CI status, runtime behavior,
  correctness, vulnerability, production impact, business priority, or complete subject
  mapping.
- The known-limits section should explicitly state that current v0.7 test facts do not
  perform test execution, CI analysis, coverage analysis, mutation testing, behavioral
  assertion understanding, runtime Spring context reconstruction, runtime
  repository/database verification, Mockito behavior analysis, slice correctness
  analysis, or full call graph reconstruction.

Current v0.8 local documentation `agent-guide.md` behavior:

- The guide adds a `Local Project Documentation` section generated from structured
  `documents` facts and resolving `document` evidence only when accepted local
  documents or reconciliation hints exist.
- The section summarizes deterministic document inventory, such as document path, module
  ownership when available, title source, heading count, chunk count, and evidence
  references. It must not summarize document prose, rewrite document content, or create
  AI-generated documentation summaries.
- Heading and chunk entries are bounded navigation references with IDs, locations,
  content status, and resolving evidence. They should not print chunk bodies, long
  document excerpts, paragraphs, arbitrary lists, tables, code blocks, or prose
  summaries.
- Reconciliation rows render as uncertain inspection hints with signal, status,
  confidence, uncertainty, subject, document path when applicable, source fact when
  applicable, and evidence. They must not say that documentation is stale, complete,
  authoritative, correct, vulnerable, business-critical, or implemented unless a future
  deterministic contract separately proves that claim.
- The known-limits section should explicitly state that local documentation is
  default-scope Markdown only, that hidden/private/generated/maintainer paths are
  excluded by default, that symlinks are not followed by default, and that external docs,
  PDFs, Word documents, connectors, generic RAG, repository chat, and LLM summaries are
  not part of the core analyzer.
- The practical inspection order may use document paths, headings, chunks, and
  reconciliation hints as navigation aids. It must still prefer code-backed facts for
  implementation truth and must not promote document-only mentions to code facts.

Markdown rendering safety:

- Markdown generators must render source-derived values through Markdown-safe presentation
  helpers before writing `endpoints.md` or `agent-guide.md`.
- Source-derived inline text, inline code, module labels and paths, endpoint paths,
  request parameter labels, warning paths and messages, evidence references, and evidence
  locations must not be able to introduce new Markdown headings, list items, evidence
  lines, tables, links, or HTML when the source value contains newlines, control
  characters, backticks, or Markdown punctuation.
- Markdown presentation may normalize control characters and line breaks into visible
  escaped sequences such as `\n`, `\r`, `\t`, or `\u001B`. This does not change the
  corresponding `project-map.json` or `evidence-index.jsonl` values, which keep their
  JSON/JSONL escaping and semantics.

## Contract Rules

- Output changes require updating this file.
- Evidence field changes require updating `docs/architecture/EVIDENCE_MODEL.md`.
- Generated facts must reference evidence IDs where possible.
- JSON and JSONL field names, nullability conventions, repeated-value conventions, and
  documented semantics are the stable machine-readable surface.
- Markdown outputs should remain deterministic, readable, cautious, and evidence-visible
  without being treated as the stable parser interface unless a specific Markdown
  structure is explicitly documented as contractual.
