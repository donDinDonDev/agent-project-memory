# v0.3.0 Release Notes

Release date: 2026-06-06

`agent-project-memory` v0.3.0 adds a deterministic build and configuration orientation
layer for Maven Java/Spring repositories. It keeps the local-first CLI boundary and the
same four generated `.project-memory/` files while expanding `project-map.json` to the
`schema_version: "0.3"` build/configuration contract.

## Highlights

- Module-owned Maven metadata for direct source-visible `groupId`, `artifactId`,
  `version`, `packaging`, and parent coordinates.
- Module-owned Maven dependency and dependency-management inventories for direct POM
  declarations.
- Module-owned Maven plugin and plugin-management inventories with bounded direct
  execution, goal, phase, and configuration signal observations.
- Conservative generated-source, OpenAPI/Swagger, annotation-processor, and
  build-helper add-source warning signals that remain warnings and do not create
  endpoint or generated API facts.
- Standard `src/main/resources` and `src/test/resources` resource-root discovery.
- Path-only supported Spring application and logging configuration file inventory.
- Direct source-visible Spring Boot application signals for supported
  `@SpringBootApplication` classes and bounded `main` method observations.
- `agent-guide.md` build/configuration orientation generated only from structured facts
  and evidence.

## Output Compatibility

v0.3.0 moves normal generated `project-map.json` output to `schema_version: "0.3"`.
The v0.3 contract builds on the v0.2 module-aware boundary:

- `project.modules` remains the module inventory boundary.
- Module-owned endpoint, warning, component, entity, and test facts keep direct
  `module_id` fields.
- Each module item includes a complete `build_config` shell with explicit
  `analysis_status` values.
- Maven values remain direct source-visible observations. The analyzer does not fill
  missing coordinates from Maven defaults, parent inheritance, profiles, or effective POM
  behavior.
- Config discovery is path-oriented and must not render config keys, values, YAML nodes,
  XML element content, environment placeholders, decrypted values, or secrets.
- Evidence records keep the existing `evidence-index.jsonl` field set. Source-derived
  excerpts are bounded and evidence paths remain repository-relative.

The generated files remain:

```text
.project-memory/project-map.json
.project-memory/evidence-index.jsonl
.project-memory/endpoints.md
.project-memory/agent-guide.md
```

## Validation

Release-prep validation covered:

- `mvn test`
- `mvn package`
- packaged CLI smoke from the Maven build
- `git diff --check`
- `git diff --stat`
- v0.3 real-project evaluation on pinned Maven Java/Spring projects
- v0.3 review and risk-based security assessment
- final post-fix release security baseline

Public evaluation summary:
[docs/development/evaluations/v0.3-build-config-real-projects_SUMMARY.md](../development/evaluations/v0.3-build-config-real-projects_SUMMARY.md).

The final post-fix release security baseline reported no release-blocking findings.

## Security Notes

v0.3.0 keeps the deterministic local analyzer boundary:

- no Maven execution during scan;
- no dependency or plugin resolution;
- no network, connector, SaaS, web UI, repository chat, generic RAG, or LLM call in the
  core analyzer;
- no config value extraction;
- no default generated-source scanning;
- no endpoint or API facts from generated-source/OpenAPI warning signals.

The release line includes a bounded-excerpt fix for generated evidence. Java annotation,
warning, test, Maven module discovery, and central evidence-index output paths now bound
source-derived excerpts so hostile repository input cannot inflate generated memory files
through oversized source excerpts.

## Not Included

- Gradle support.
- Maven execution.
- Effective POM reconstruction.
- Parent inheritance resolution into effective coordinates.
- Maven profile activation.
- Remote dependency or plugin resolution.
- Transitive dependency graph reconstruction.
- Full plugin lifecycle reconstruction.
- Runtime Spring configuration resolution.
- Config property key/value inventory or secret extraction.
- Default generated-source scanning.
- OpenAPI YAML/JSON parsing or generated API reconstruction.
- Endpoint creation from build, config, OpenAPI, or generated-source warning signals.
- Full Java symbol solving.
- Runtime Spring handler mapping, bean graph, or component scanning reconstruction.
- Full ORM runtime behavior.
- Test execution, coverage, CI, mutation testing, or call graph analysis.
- Local Markdown/document ingestion.
- Connectors for YouTrack, Jira, Confluence, GitHub, or GitLab.
- LLM calls in the core analyzer.
- SaaS, web UI, repository chat, generic RAG, or automatic code modification.

## Known Follow-Ups

The v0.3 real-project evaluation records bounded future work that is not required for
this release:

- Preserve v0.3 semantics while improving large-project module scale.
- Keep OpenAPI/generated API reconstruction in the future API-surface track.
- Add nested Maven module support in a later Maven expansion track.

## Publication Status

The `v0.3.0` tag and GitHub release are published:

- Release: <https://github.com/donDinDonDev/agent-project-memory/releases/tag/v0.3.0>
- Assets: `agent-project-memory-0.3.0.jar` and `SHA256SUMS`.
- Jar SHA-256:
  `35fe7a06f95ffa62e807461a73880bfb8391f174d5922072dda036d9a5a44cdd`.
