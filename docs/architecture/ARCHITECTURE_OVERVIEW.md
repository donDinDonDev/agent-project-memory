# Architecture Overview

`agent-project-memory` is organized around a deterministic local analysis pipeline.

The core analyzer must not depend on external APIs. It should be able to scan a local Java/Spring repository and produce project memory artifacts without network access and without sending source code anywhere.

## Conceptual Components

### CLI

Accepts user commands and local repository paths. The intended first command is:

```sh
agent-project-memory scan .
```

The CLI should coordinate scanning and artifact generation. It should not contain analyzer logic directly.

### Repository Scanner

Walks the local repository, applies ignore rules, identifies candidate project files, and provides normalized file references to analyzers.

### Build Detector

Detects Maven project structure and build metadata needed by analyzers. The current
implementation detects a root `pom.xml` when present and standard single-module Maven
source roots such as `src/main/java` and `src/test/java`. Full Maven module parsing is a
later roadmap item and is not implemented in the current slice.

### Java/Spring Analyzer

Uses JavaParser first to inspect Java source files. The current implementation extracts
Spring MVC endpoint facts and direct Spring stereotype component facts from supported
production source roots.

The basic component analyzer records only directly present class-level stereotypes such
as `@Component`, `@Service`, `@Repository`, `@Controller`, `@RestController`, and
`@Configuration`. It does not reconstruct Spring component scanning, bean lifecycle,
bean names, scopes, autowiring, conditional configuration, or other runtime behavior.

Future deeper analyzers may be added, but they must preserve deterministic evidence-backed behavior.

### Project Graph Builder

Builds a structured project map from extracted facts. The graph should describe known project elements and relationships, while distinguishing direct facts from inferred relations.

### Evidence Index Builder

Creates stable evidence records for important facts. Evidence may point to code symbols, annotations, config files, build files, test files, or documents.

### Memory Generator

Writes machine-readable memory artifacts such as `project-map.json` and `evidence-index.jsonl`.

### Agent Files Generator

Writes human-readable and agent-oriented Markdown artifacts such as `endpoints.md` and `agent-guide.md`.

These files must not invent architecture beyond the extracted facts and documented inferences.

### Future Docs/Issues Ingestors

Future ingestors may import local Markdown docs or external materials from systems such as YouTrack, Jira, Confluence, GitHub, and GitLab.

Connectors are input adapters. They should normalize external records into source documents and should not become part of the core Java/Spring analyzer.

### Optional LLM Layer

An optional LLM layer may be considered later for presentation, grouping, or summarization. It must not be required for core analysis, and it must not be treated as the source of truth.

Any LLM-generated output must be derived from deterministic facts and evidence references.

## Pipeline

The intended pipeline is:

```text
local repository
  -> repository scanner
  -> build detector
  -> Java/Spring analyzers
  -> project graph builder
  -> evidence index builder
  -> memory and agent file generators
  -> .project-memory/
```

## Architectural Constraints

- The core analyzer runs locally.
- The core analyzer does not call external APIs.
- Source code is not sent anywhere by default.
- Facts must be traceable to evidence.
- Output contracts must be explicit and documented.
- Optional future integrations must be adapters around the core, not dependencies inside it.
