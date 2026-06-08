# Stage 8 Evaluation Summary: Spring PetClinic

Evaluation date: 2026-05-30

## Scope

This public summary records the v0.1 evaluation result for the pinned Spring
PetClinic project:

- Repository: `https://github.com/spring-projects/spring-petclinic.git`
- Ref: `3c06fbfc1e42eb40802e0d0ca989bc9226755804`
- Scan target: repository root

The evaluation checked whether v0.1 generated deterministic, evidence-backed project
memory for a canonical source-visible Maven Spring application.

## Result

After bounded follow-up fixes during the v0.1 evaluation track, the project generated
all four v0.1 contract artifacts:

```text
.project-memory/project-map.json
.project-memory/evidence-index.jsonl
.project-memory/endpoints.md
.project-memory/agent-guide.md
```

Final generated counts:

| Signal | Count |
| --- | ---: |
| Endpoints | 17 |
| Components | 9 |
| Entities | 6 |
| Tests | 18 |
| Evidence records | 257 |
| Direct mapped-superclass identifier fields | 1 |

All referenced evidence IDs resolved. Evidence paths were repository-relative, and the
guide used cautious detected, inferred, and uncertain wording.

## Scorecard

| Project/ref | Endpoints | Components | Entities | Tests | Evidence quality | `agent-guide.md` |
| --- | --- | --- | --- | --- | --- | --- |
| `spring-petclinic@3c06fbfc1e42eb40802e0d0ca989bc9226755804` | `2` | `2` | `2` | `2` | `2` | `2` |

Score meanings: `2` means accurate and useful for the documented v0.1 scope; `1` means
usable with bounded concerns; `0` means misleading or contract-breaking; `N/A` means the
selected project did not exercise that signal.

## Observations

- Supported direct Spring MVC controller mappings matched bounded source inspection.
- Direct Spring stereotype components matched bounded source inspection.
- Direct JPA entity, table, relationship, and conservative immediate
  mapped-superclass identifier facts were emitted with resolving evidence.
- Test inventory omitted helper/support/configuration declarations without clear test
  naming or direct test-class markers.
- Naming-convention tested-subject relations remained inferred rather than direct facts.

## Limitations

- Multi-level mapped-superclass identifier inheritance remained outside v0.1.
- Spring runtime behavior, ORM runtime behavior, test execution, coverage, assertion
  behavior, call graphs, and complete subject mapping were not analyzed.
- The evaluation was a bounded real-project check, not a broad corpus audit.
