# Release Process

This document defines the public release discipline for `agent-project-memory`.

The human maintainer owns release authority. Coding agents may prepare release
materials, run checks, and summarize state, but must not create commits, tags, GitHub
releases, uploads, or published artifacts unless the maintainer explicitly asks for that
specific action.

## Versioning Policy

The project follows SemVer intent.

During the `0.x` line, the public output contract may still evolve. Even before `1.0.0`,
schema, output, and evidence changes must be explicit in architecture docs, tests,
changelog, and release notes.

Use versions this way:

- Patch versions such as `0.1.1` or `0.2.1`: bug fixes, documentation corrections,
  packaging fixes, evaluation follow-up fixes, and narrow compatibility fixes.
- Minor versions such as `0.2.0` or `0.3.0`: new pre-1.0 capabilities or output
  contract expansions.
- `1.0.0`: stable Java/Spring local-first project memory product with documented
  compatibility expectations.
- Future major versions: substantial public API, schema, adapter, or platform changes
  that should not be treated as minor releases.

Release tags should use the format:

```text
vX.Y.Z
```

After a stable release, active development may move `pom.xml` to the next planned
`-SNAPSHOT` version when the maintainer chooses to open that release track. Pre-release
versions such as `0.2.0-alpha.1`, `0.2.0-beta.1`, or `0.2.0-rc.1` should be used only
when public pre-release artifacts are intentionally planned.

## Changelog Rules

`CHANGELOG.md` is the release history source for user-visible changes.

Update `CHANGELOG.md` when a change includes:

- user-visible behavior;
- CLI command, flag, diagnostics, or exit-code behavior;
- generated output file additions, removals, renames, fields, or semantics;
- evidence shape or evidence semantics;
- analyzer behavior;
- release packaging or installation changes;
- meaningful public documentation that changes project direction or contributor
  workflow.

Documentation typo fixes usually do not need changelog entries unless they correct
release, contract, security, or usage guidance.

Keep unreleased changes under:

```text
## [Unreleased]
```

Before a release, move relevant entries into:

```text
## [X.Y.Z] - YYYY-MM-DD
```

Use standard groups such as `Added`, `Changed`, `Deprecated`, `Removed`, `Fixed`,
`Security`, and `Not Included` when applicable.

## Contract And Evidence Gates

Any output field addition, removal, rename, or semantic change requires:

- `docs/architecture/OUTPUT_CONTRACT.md` updated in the same logical change;
- focused tests or golden outputs updated;
- `CHANGELOG.md` updated;
- release notes mention the compatibility impact.

Any evidence shape or evidence semantic change requires:

- `docs/architecture/EVIDENCE_MODEL.md` updated in the same logical change;
- focused tests or golden outputs updated;
- `CHANGELOG.md` updated;
- release notes mention the compatibility impact.

LLM-generated text must not be used as authoritative evidence in any release line.

## Compatibility, Deprecation, And Migration Notes

Starting with the v1.0 compatibility line, generated JSON and JSONL field semantics are
the stable machine-readable surface. Markdown outputs remain deterministic,
evidence-visible, and cautious, but exact Markdown presentation is not a stable parser
interface unless a specific structure is explicitly documented in
`docs/architecture/OUTPUT_CONTRACT.md`.

Breaking output or evidence changes must be documented in the same logical change that
introduces them. The documentation set should identify:

- the affected generated file, field, evidence type, or behavior;
- the old behavior and the new behavior;
- whether the change is additive, deprecated, removed, renamed, or semantic;
- the compatibility impact for downstream consumers;
- the migration action, such as regenerating outputs, accepting a new
  `schema_version`, updating a parser, or changing evidence interpretation.

Deprecations should use a `Deprecated` changelog group when applicable and should also
appear in release notes. A deprecation note should name the affected field or behavior,
the replacement when available, the current support status, and removal conditions when
known.

Migration notes belong in release notes for released behavior and in README usage text
when everyday users need to change commands, installation steps, schema-version
allowlists, or generated-output consumption.

## Public Surface Review

Before release documentation or release metadata is published, public-facing text should
be reviewed for product, contract, validation, limitation, compatibility, and release
facts only.

Public surfaces include README usage text, changelog entries, product and release
documentation, public evaluation summaries, public review or risk summaries, release
notes, and GitHub Release body text.

Public text must not expose:

- local machine paths;
- raw command transcripts or unreviewed execution logs;
- internal task, goal, checkpoint, or decision IDs;
- maintainer-only workflow notes;
- tool-specific internal report names or local report paths.

If raw execution detail is useful for maintenance, keep it in ignored maintainer notes
rather than tracked public documentation.

## Release Readiness

Before a release, confirm that:

- the intended version and release scope are clear;
- `pom.xml`, README usage examples, release notes, roadmap/status docs, and
  `CHANGELOG.md` agree on the release version and status;
- output and evidence architecture docs match generated behavior;
- meaningful analyzer expansions have public validation summaries;
- focused tests, full local tests, packaging checks, and release artifact checks have
  passed for the release scope;
- release dry-run and CI validation, when present, run without secrets or write
  permissions and cannot publish, upload, sign, create tags, create releases, or mutate
  package registries;
- dependency and GitHub Actions update coverage is reviewed for the release scope, and
  any dependency/security workflow change has risk-based review before release;
- risk-based security review is complete where the release changes parser, path,
  filesystem, dependency, output, evidence, network/auth, security configuration, or
  packaging behavior;
- public docs and release metadata have passed public-surface review;
- no connector, network, AI, SaaS, web UI, repository chat, generic RAG, or automatic
  code-modification scope entered the release accidentally.

## Artifact And Checksum Discipline

Public binary releases are expected to ship:

- `agent-project-memory-X.Y.Z.jar`
- `SHA256SUMS`

The release jar should be built from the intended release version, smoke-checked as an
executable CLI, and verified against `SHA256SUMS`. Checksum files should contain release
asset filenames only, not absolute paths, parent directories, workspace-local paths, or
temporary paths.

Before publication, the candidate binary asset set should match the release notes. After
publication, downloaded assets should be verified against the published checksums before
the binary release is considered complete.

The artifact/checksum process is verification and packaging discipline. It must not be
treated as permission for automated commits, tags, pushes, uploads, GitHub Release
publication, credentials use, or other remote state changes.

Local dry-runs or read-only CI checks may validate the candidate jar filename, CLI
version output, manifest entrypoint, Maven artifact metadata, release asset list, and
filename-only `SHA256SUMS` contents when a release scope approves that work. Those
checks are validation only: they must not attach assets, upload artifacts, publish
checksums, sign files, create releases, move tags, or require secrets.

After `mvn package`, maintainers can run the local artifact-integrity dry-run:

```sh
bash scripts/release-artifact-integrity-dry-run.sh
```

The helper validates the expected candidate jar filename, packaged CLI `--version`
output, jar manifest `Main-Class`, embedded Maven `pom.properties` coordinates, the
local dry-run release asset list, and a `SHA256SUMS` file with the jar filename only.
It creates or refreshes only `target/release-artifact-dry-run/` by default. The helper
does not publish, upload, attach assets, sign files, generate an SBOM, create or move
tags, create or edit releases, deploy packages, use credentials, or contact remote
services.

## Installation Channel Policy

Until a future approved distribution channel changes this document, public binary
releases are expected to use the executable shaded jar and `SHA256SUMS`. README and
release notes should document:

```sh
java -jar agent-project-memory-X.Y.Z.jar ...
```

Shell wrappers, JBang catalogs, Homebrew taps, Maven Central publication, SDKMAN/asdf
plugins, native images, and container images are separate distribution channels. Adding
any of them requires an explicit scoped change to release checklist expectations,
user-facing installation documentation, and release notes. They must not be bundled into
ordinary release prep.

## Supply-Chain Hardening Boundary

The v2.8 distribution hardening baseline keeps the executable GitHub Release jar
and `SHA256SUMS` as the only supported public distribution channel until a later release
implements, validates, documents, and publishes another channel.

Accepted v2.8 hardening work may improve local artifact-integrity repeatability and
read-only CI validation, but only when the checks run without credentials, secrets,
write permissions, uploads, package registry access, signing authority, release
publication, or remote state mutation.

Signing remains parked until a later explicit design chooses a signing model, key
custody boundary, verification instructions, release asset status, and maintainer
approval gate. Signing keys, identities, account details, passphrases, tokens, and
secret names must not be stored in repository files, generated artifacts, public docs,
fixtures, scripts, logs, or release notes.

SBOM generation and publication remain parked until a later explicit design chooses the
tool, dependency-resolution boundary, generated-file status, release asset status,
security wording, validation expectations, and review gate. An SBOM must not be
presented as proof of vulnerability absence, license compliance, dependency freshness,
or release safety.

Package-manager channels, installed-command distribution, native images, and container
images remain unavailable until the specific channel is implemented, tested, documented,
and published in a release note. Public install docs must not show channel commands as
available before that happens.

## Release Approval Gates

Release dry-runs, artifact checks, dependency review, and documentation preparation are
not publication approval. Each action below needs an explicit maintainer approval for
that action and release:

- pushing a release branch or `main`;
- creating, moving, or deleting a tag;
- creating, publishing, or editing a GitHub Release;
- uploading, replacing, or deleting release assets;
- publishing checksums, signatures, SBOMs, package-manager artifacts, native images, or
  container images;
- enabling release automation or package publication automation;
- using release credentials, signing keys, package-registry tokens, or account-specific
  publication state.

## Publication Responsibility

Publication is manual maintainer authority unless explicitly delegated for a specific
action. A release is not complete merely because release materials are prepared or local
checks pass.

The maintainer should ensure that:

- the release tag points to the intended release-ready commit;
- the GitHub Release body matches audited public release notes;
- the packaged jar and checksum assets are attached when binary assets are expected;
- downloaded assets verify successfully;
- README download instructions remain accurate;
- known limitations and evaluation follow-ups remain visible.

## Agent Boundaries

Coding agents may:

- prepare release notes;
- update docs and changelog during approved release preparation;
- run validation commands;
- summarize release readiness;
- recommend a tag name or release title.

Coding agents must not, unless explicitly asked by the maintainer:

- create commits;
- create or move tags;
- push branches or tags;
- publish GitHub releases;
- upload release artifacts;
- sign artifacts;
- publish checksums, SBOMs, package-manager artifacts, native images, or container
  images;
- use release credentials, signing keys, package-registry tokens, or account-specific
  publication state;
- enable release automation or package publication automation;
- change release scope;
- bypass failed checks;
- treat external connector data or LLM output as release evidence.
