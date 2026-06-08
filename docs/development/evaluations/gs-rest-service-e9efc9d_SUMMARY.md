# Stage 8 Evaluation Summary: Spring Guide REST Service

Evaluation date: 2026-06-01

## Scope

This public summary records the v0.1 evaluation result for the pinned Spring Guide REST
Service project:

- Repository: `https://github.com/spring-guides/gs-rest-service.git`
- Ref: `e9efc9dfa0abe8cf8e15cf0e71830b5125322cae`
- Scan target: `complete/`

The evaluation checked a small source-visible Maven Spring MVC sample. It did not
include broader corpus coverage.

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
| Components | 1 |
| Entities | 0 |
| Tests | 1 |
| Evidence records | 12 |
| Missing evidence references | 0 |

## Scorecard

| Project/ref | Endpoints | Components | Entities | Tests | Evidence quality | `agent-guide.md` |
| --- | --- | --- | --- | --- | --- | --- |
| `gs-rest-service@e9efc9dfa0abe8cf8e15cf0e71830b5125322cae` (`complete/`) | `2` | `2` | `N/A` | `2` | `2` | `2` |

Score meanings: `2` means accurate and useful for the documented v0.1 scope; `1` means
usable with bounded concerns; `0` means misleading or contract-breaking; `N/A` means the
selected project did not exercise that signal.

## Observations

- The direct `GET /greeting` Spring MVC endpoint was emitted with request-parameter and
  response-type details.
- The direct `@RestController` component was emitted with resolving annotation evidence.
- No JPA entities were invented for a project that did not contain direct JPA signals.
- The single test class was emitted with direct framework signals and an inferred
  tested-subject relation.
- The guide stayed concise and did not invent broader architecture.

## Limitations

- This is a small sample and does not exercise module awareness, generated APIs, complex
  JPA models, or large test inventories.
