# Release Process

This document defines the public release discipline for `agent-project-memory`.

The human maintainer owns release authority. Codex or another coding agent may prepare
release materials, run checks, and summarize state, but must not create commits, tags,
GitHub releases, or publish artifacts unless the maintainer explicitly asks for that
specific action.

## Versioning Policy

The project follows SemVer intent.

During the `0.x` line, the public output contract may still evolve. Even before `1.0.0`,
schema, output, and evidence changes must be explicit in the architecture docs, tests,
changelog, and release notes.

Use versions this way:

- Patch versions such as `0.1.1` or `0.2.1`: bug fixes, documentation corrections,
  packaging fixes, evaluation follow-up fixes, and narrow compatibility fixes.
- Minor versions such as `0.2.0` or `0.3.0`: new pre-1.0 capabilities or output contract
  expansions.
- `1.0.0`: stable Java/Spring local-first project memory product with documented
  compatibility expectations.
- Future major versions: substantial public API, schema, adapter, or platform changes
  that should not be treated as a minor release.

Release tags should use the format:

```text
vX.Y.Z
```

## Development Versions And Pre-Releases

After a stable release, active development may move `pom.xml` to the next planned
`-SNAPSHOT` version when the maintainer chooses to open that release track. For example,
after `0.1.0`, v0.2 development may use:

```text
0.2.0-SNAPSHOT
```

This is a maintainer decision and should happen as a focused versioning change or as part
of the first approved release-track development slice. Until that happens, local development
builds may still produce jars named with the last released version from `pom.xml`.

Use pre-release versions such as `0.2.0-alpha.1`, `0.2.0-beta.1`, or `0.2.0-rc.1` only
when the maintainer explicitly wants public pre-release artifacts. Do not create
pre-release tags or GitHub releases automatically.

Development commits are ordinary commits that capture a reviewed work slice. Release
commits prepare a specific version for tagging and publishing. A development commit must
not be treated as permission to tag, publish, or call the result a release.

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

Use these groups when applicable:

- `Added`
- `Changed`
- `Deprecated`
- `Removed`
- `Fixed`
- `Security`
- `Not Included`

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

## Release Readiness Checklist

Before preparing a release candidate:

- Confirm the intended version and release scope.
- Confirm `pom.xml` has the intended release version.
- Confirm `README.md` usage examples match the intended artifact name and version.
- Confirm `docs/product/ROADMAP.md` and relevant product docs match the release scope.
- Confirm `docs/architecture/OUTPUT_CONTRACT.md` is synchronized with generated output.
- Confirm `docs/architecture/EVIDENCE_MODEL.md` is synchronized with evidence records.
- Confirm `CHANGELOG.md` has a dated release section.
- Confirm release notes exist or are drafted for the release.
- Confirm evaluation summaries are updated when the release includes meaningful analyzer
  expansion.
- Before creating a release tag, finalize release documentation in release-ready wording.
  The tag must point to a commit whose `CHANGELOG.md`, roadmap/status docs, README
  version references, and release notes already describe the release as ready/released
  rather than pending.
- Run any risk-based security review defined by the active release track before tagging
  or publishing.
- Treat a broad release-candidate security review as an open-ended discovery baseline,
  not as routine fix verification. If the active release track identifies a final
  security discovery baseline, do not run another open-ended broad review for the same
  release unless the maintainer explicitly reopens that baseline because it is invalid
  or incomplete. Verify that every baseline finding is fixed, explicitly deferred, or
  maintainer-accepted using closed-set targeted verification or focused security
  re-review of fix changes.
- Confirm no connector, network, AI, SaaS, web UI, repo chat, generic RAG, or automatic
  code-modification scope entered the release accidentally.

Required local checks:

```sh
git status --short
git diff --check
mvn test
mvn package
git diff --stat
```

For binary releases, inspect the packaged jar and publish checksums when release assets
are attached.

## Release Procedure

1. Prepare a focused release branch or release-prep change.
2. Update the version in `pom.xml` when needed.
3. Update README usage examples if the artifact version changes.
4. Update `CHANGELOG.md`.
5. Update or create release notes under `docs/product/`.
6. Run the release readiness checks.
7. Review the full diff for scope drift, contract drift, evidence drift, and generated
   artifact hygiene.
8. Merge only after maintainer review.
9. Create the `vX.Y.Z` tag only after explicit maintainer approval.
10. Draft the GitHub release only after the tag is approved.
11. Attach the packaged jar and checksum files when publishing binary assets.
12. Verify the published release notes and download instructions.

## Agent Boundaries

Coding agents may:

- prepare release notes;
- update docs and changelog within an approved release-prep goal;
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

## Post-Release Follow-Up

After a release:

- Confirm the tag and GitHub release point to the intended commit.
- Confirm release artifacts and checksums are available when expected.
- Confirm README download instructions remain accurate.
- Open or update follow-up issues for known limitations and evaluation findings.
- Move unreleased changelog entries forward for the next development cycle.
