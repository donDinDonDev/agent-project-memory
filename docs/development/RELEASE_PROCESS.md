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
- change release scope;
- bypass failed checks;
- treat external connector data or LLM output as release evidence.
