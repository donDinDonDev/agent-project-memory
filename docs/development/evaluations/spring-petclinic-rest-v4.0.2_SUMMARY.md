# Stage 8 Evaluation Summary: Spring PetClinic REST

Evaluation date: 2026-05-30

## Scope

This public summary records the v0.1 evaluation result for the pinned Spring
PetClinic REST project:

- Repository: `https://github.com/spring-petclinic/spring-petclinic-rest.git`
- Tag: `v4.0.2`
- Ref: `d8026bb5bcc58145b95a66a7f8e7694f0fae142f`
- Scan target: repository root

The evaluation checked v0.1 behavior on a larger REST project whose main operation
surface is declared through OpenAPI/generated interfaces.

## Result

The scan generated all four v0.1 contract artifacts:

```text
.project-memory/project-map.json
.project-memory/evidence-index.jsonl
.project-memory/endpoints.md
.project-memory/agent-guide.md
```

Generated counts:

| Signal | Count |
| --- | ---: |
| Endpoints | 1 |
| Components | 28 |
| Entities | 8 |
| Tests | 19 |
| Evidence records | 342 |
| Missing evidence references | 0 |

The emitted direct-source facts were evidence-backed. The main REST API operations were
not emitted as v0.1 endpoint facts because they were declared through generated
interfaces or OpenAPI inputs outside the v0.1 default source boundary.

## Scorecard

| Project/ref | Endpoints | Components | Entities | Tests | Evidence quality | `agent-guide.md` |
| --- | --- | --- | --- | --- | --- | --- |
| `spring-petclinic-rest@v4.0.2` / `d8026bb5bcc58145b95a66a7f8e7694f0fae142f` | `1` | `2` | `2` | `2` | `2` | `1` |

Score meanings: `2` means accurate and useful for the documented v0.1 scope; `1` means
usable with bounded concerns; `0` means misleading or contract-breaking; `N/A` means the
selected project did not exercise that signal.

## Observations

- The emitted endpoint was the direct source-visible root redirect handler.
- Direct Spring stereotype components, direct JPA entities, relationship facts, and test
  inventory were evidence-backed.
- Source-visible Java interface mappings are supported only when the interface source is
  under supported production roots and uniquely binds to concrete handlers.
- Generated-source scanning, Maven generation, OpenAPI parsing, and generated API
  reconstruction remained outside the v0.1 scope.

## Limitations

- The primary user-facing API surface was intentionally not reconstructed from OpenAPI
  specs or generated interfaces.
- The guide was useful but noisier on a larger test/component surface than on smaller
  samples.
- Runtime handler mappings, generated sources, security policy, test execution,
  coverage, and complete subject mapping were not analyzed.
