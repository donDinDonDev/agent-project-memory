# Security Policy

## Supported Versions

The supported release line will be `v0.1.x` once `v0.1.0` is released.

## Reporting A Vulnerability

Please report suspected vulnerabilities privately. Use GitHub private vulnerability
reporting for this repository when available, or contact the repository maintainer
through a private channel listed on the maintainer's GitHub profile.

Do not publish exploit details, proof-of-concept payloads, or sensitive reproduction
steps in public issues, pull requests, discussions, or comments. A public issue may be
opened after the vulnerability is resolved if it does not expose actionable exploit
details.

Please include:

- Affected version or commit.
- Local environment details, including OS, Java version, and Maven version when relevant.
- A minimal reproduction or description of the vulnerable behavior.
- Whether the issue can cause source disclosure, unsafe file writes, command execution,
  misleading generated evidence, or other local impact.

`agent-project-memory` is a local-first CLI. This project does not operate a SaaS
service, hosted scanner, hosted project-memory store, or public API service.
