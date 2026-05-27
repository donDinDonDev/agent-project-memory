# Evidence Model

Evidence is the basis for trust in `agent-project-memory`.

Every important generated fact should point back to one or more evidence references. A fact without evidence is either incomplete, inferred, uncertain, or out of scope for the deterministic project map.

## Evidence Reference

An evidence reference identifies the source that supports a generated fact.

Evidence may point to:

- a Java class,
- a Java method,
- an annotation,
- a Maven build file,
- a Spring configuration file,
- a test file,
- a local Markdown document,
- another explicit source reference.

## Evidence Types

Initial evidence types:

- `code_symbol`: a class, method, field, enum, interface, or other Java symbol.
- `annotation`: an annotation on a class, method, field, parameter, or configuration element.
- `config_file`: a configuration file such as `application.yml`, `application.properties`, or XML configuration.
- `build_file`: a build file such as `pom.xml`.
- `test_file`: a test source file or test resource.
- `document`: a local project document such as Markdown.

## Evidence Fields

Evidence entries use these fields:

- `id`: stable evidence identifier within one scan output.
- `source_type`: evidence type.
- `path`: repository-relative path to the source file.
- `class_name`: fully qualified class name when applicable.
- `method_name`: method name when applicable.
- `symbol_name`: symbol, annotation, property, file section, or document heading when applicable.
- `line_start`: first line supporting the evidence when known.
- `line_end`: last line supporting the evidence when known.
- `excerpt`: short source excerpt or normalized snippet.
- `confidence`: confidence label or score for the evidence reference.

Additional fields may be added later only when `OUTPUT_CONTRACT.md` and this document are updated.

## Fact Categories

### Extracted Facts

Extracted facts are directly observed in source files or documents.

Examples:

- A class annotated with `@RestController`.
- A method annotated with `@GetMapping("/orders/{id}")`.
- A Maven project with a root `pom.xml`.

Extracted facts should use strong evidence references and high confidence.

### Inferred Relations

Inferred relations are derived from multiple extracted facts or conventions.

Examples:

- A test class named `OrderControllerTest` likely covers `OrderController`.
- A service injected into a controller is likely involved in handling that controller's endpoints.

Inferred relations must be marked as inferred and must preserve the evidence that led to the relation.

### Uncertain Signals

Uncertain signals are observations that may be useful but should not be presented as facts.

Examples:

- A naming convention suggests a flow boundary, but no direct call or injection evidence was found.
- A Markdown document mentions a module name that does not clearly map to source code.

Uncertain signals should be labeled with lower confidence and should not be used as the sole basis for important generated claims.

## Evidence Discipline

- Do not fabricate evidence.
- Do not cite a file without a specific symbol, line range, excerpt, or documented reason when a more precise reference is available.
- Do not treat LLM output as evidence.
- Prefer exact source references over broad summaries.
- When line numbers are unavailable, make that explicit in the evidence entry and use the most precise available symbol reference.

