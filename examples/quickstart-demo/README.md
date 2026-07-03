# Quickstart Demo Input

This directory is a tiny fake Java/Spring Maven project for a first-run
`agent-project-memory` scan.

The demo source is original example material in this repository. It does not vendor an
external project, include private data, or include intentional secrets. It is an
inspectable input for the quickstart path, not a benchmark, adoption result, performance
result, security proof, or product-effectiveness claim.

From the repository root, build the packaged jar and scan this demo input:

```sh
mvn package
java -jar target/agent-project-memory-3.4.0.jar scan examples/quickstart-demo
```

The scan writes generated output under:

```text
examples/quickstart-demo/.project-memory/
```

That generated directory is ignored here so readers can regenerate it locally. For a
first read, open:

1. `.project-memory/artifact-set.json`
2. `.project-memory/agent-guide.md`
3. `.project-memory/endpoints.md`
4. `.project-memory/project-map.json`
5. `.project-memory/evidence-index.jsonl`

Only `evidence-index.jsonl` is the source-backed evidence artifact. Generated Markdown,
query output, graph metadata, and this README are navigation or presentation surfaces,
not evidence.
