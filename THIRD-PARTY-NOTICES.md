# Third-Party Notices

`agent-project-memory` is licensed under Apache License, Version 2.0. See
[`LICENSE`](LICENSE).

The executable CLI jar is a shaded jar and includes runtime dependencies. This file is
an informational summary of third-party components included in the released runtime
artifact. The jar also carries dependency-provided license and notice resources under
`META-INF/`.

## Runtime Dependencies

| Component | Version | License |
| --- | --- | --- |
| `com.fasterxml.jackson.core:jackson-databind` | `2.17.2` | Apache License, Version 2.0 |
| `com.fasterxml.jackson.core:jackson-core` | `2.17.2` | Apache License, Version 2.0 |
| `com.fasterxml.jackson.core:jackson-annotations` | `2.17.2` | Apache License, Version 2.0 |
| `com.github.javaparser:javaparser-core` | `3.26.4` | Dual license declared by its Maven POM: GNU Lesser General Public License and Apache License, Version 2.0. This project uses the Apache License, Version 2.0 option. |
| `org.yaml:snakeyaml` | `2.5` | Apache License, Version 2.0 |

## Dependency-Provided Notices In The Shaded Jar

The shaded jar contains dependency-provided notice/license resources such as:

- `META-INF/LICENSE`
- `META-INF/NOTICE`
- `META-INF/FastDoubleParser-LICENSE`
- `META-INF/FastDoubleParser-NOTICE`
- `META-INF/thirdparty-LICENSE`

These resources are preserved from dependencies and should remain present in release
artifacts.

## Test-Only Dependencies

JUnit dependencies are used for tests and are not part of the released CLI runtime
artifact.
