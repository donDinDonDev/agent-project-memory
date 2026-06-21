# Stage 3 Project Map Example

This directory contains a checked-in generated-output snapshot for the fake
`stage3-project-map` fixture. It is meant to help readers inspect the current base
`.project-memory/` output files without first running the CLI.

The snapshot is a documentation aid. It is not project evidence, not a second schema
contract, and not a stable Markdown parser contract. The authoritative generated-output
and evidence semantics remain:

- [Output Contract](../../docs/architecture/OUTPUT_CONTRACT.md)
- [Evidence Model](../../docs/architecture/EVIDENCE_MODEL.md)

## Source Pair

The example is derived from the deterministic repository test pair:

- input fixture: `src/test/resources/fixtures/stage3-project-map`
- expected output source: `src/test/resources/golden/stage3-project-map`

The fixture uses fake `com.example` source only. No standalone demo input project is
committed under `examples/`.

## Files Included

Only the base generated output set is committed:

```text
examples/stage3-project-map/.project-memory/artifact-set.json
examples/stage3-project-map/.project-memory/project-map.json
examples/stage3-project-map/.project-memory/project-graph.json
examples/stage3-project-map/.project-memory/evidence-index.jsonl
examples/stage3-project-map/.project-memory/endpoints.md
examples/stage3-project-map/.project-memory/agent-guide.md
```

The artifact-set manifest is contract/provenance metadata, not project evidence.
Opt-in agent profile artifacts, AI presentation artifacts, and incremental cache
metadata are intentionally omitted because they are optional generated presentations or
execution metadata, not the base output set.

## Regenerate And Compare

From the repository root, build the packaged jar, scan a temporary copy of the fixture,
and compare the generated base files with this example snapshot:

```sh
mvn package
workdir="$(mktemp -d)"
cp -R src/test/resources/fixtures/stage3-project-map "$workdir/stage3-project-map"
java -jar target/agent-project-memory-2.9.0.jar scan "$workdir/stage3-project-map"

for file in artifact-set.json project-map.json project-graph.json evidence-index.jsonl endpoints.md agent-guide.md; do
  diff -u \
    "examples/stage3-project-map/.project-memory/$file" \
    "$workdir/stage3-project-map/.project-memory/$file"
done
```

The comparison should be clean for the current checkout. If a future analyzer or output
contract change intentionally changes generated files, update the canonical tests,
goldens, public contracts, changelog, and this example in the same logical change.
