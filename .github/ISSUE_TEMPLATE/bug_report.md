---
name: Bug report
about: Report a reproducible problem with agent-project-memory
title: "[Bug]: "
labels: bug
assignees: ""
---

## Summary

Describe the reproducible problem and the affected workflow.

## Environment

- Version or commit:
- OS:
- Java version:
- Maven version:
- Installation path:
  - [ ] Published release jar
  - [ ] Local `mvn package` jar
  - [ ] Source checkout

## Reproduction

Command and inputs:

- Command:
- Scan or query target shape:
- Config file used, if any:

Steps to reproduce the issue:

1.
2.
3.

## Expected Behavior

What should happen?

## Actual Behavior

What happened instead?

## Output Or Evidence Impact

- Affected `.project-memory/` files:
- Relevant evidence IDs or source references:
- Does this affect `project-map.json`, `project-graph.json`, `evidence-index.jsonl`,
  `endpoints.md`, `agent-guide.md`, agent profiles, cache metadata, or query output?
- Does this appear to change generated output fields, evidence shape, or evidence
  semantics?
- If generated output is wrong, what is the smallest excerpt needed to show the issue?

## Security Check

- [ ] This report does not include secrets, tokens, credentials, private repository
      data, or exploit details for a security vulnerability.
- [ ] If this is a vulnerability report, I will report it privately according to
      `SECURITY.md` instead of posting details here.
