# Security Policy

## Supported Versions

The latest published release line is the supported public line for vulnerability
reports. The latest published release is currently `v2.8.0`.

Older release lines are generally unsupported unless a maintainer explicitly states
otherwise in release notes or a security advisory.

## Reporting A Vulnerability

Please report suspected vulnerabilities privately. Use GitHub private vulnerability
reporting for this repository when available, or contact the repository maintainer
through a private channel listed on the maintainer's GitHub profile.

Do not publish exploit details, proof-of-concept payloads, sensitive reproduction
steps, or real secrets in public issues, pull requests, discussions, comments, fixtures,
or documentation.

Please include:

- Affected version or commit.
- Local environment details, including OS, Java version, and Maven version when
  relevant.
- A minimal reproduction or description of the vulnerable behavior.
- Whether the issue can cause source disclosure, unsafe file writes, command execution,
  misleading generated evidence, unsafe path traversal, local absolute path disclosure,
  or sensitive value disclosure in generated artifacts or terminal output.

## Product Security Boundary

`agent-project-memory` is a local-first CLI/devtool. This project does not operate a
SaaS service, hosted scanner, hosted project-memory store, or public API service.

The core analyzer and query layer are intended to run without source upload, network
access, external APIs, connectors, telemetry, repository chat, generic RAG, or LLM calls.
Future optional integrations must remain outside the deterministic core unless public
architecture documents explicitly define otherwise.

The tool is not a vulnerability scanner, security correctness engine, complete secret
detector, secret inventory, or credential classification tool. Security hardening is
focused on local path safety, bounded parsing, evidence integrity, and avoiding
accidental serialization of sensitive-looking values in generated or rendered output.

See [docs/development/THREAT_MODEL.md](docs/development/THREAT_MODEL.md) for the public
threat model, trust boundaries, and redaction limitations.
