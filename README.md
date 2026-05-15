# ReqShift Engine

Open source engine for analysing, scoring, and securing REST/OpenAPI specifications.

> Lighthouse for APIs. SonarQube for APIs.

## Status

V0 bootstrap — Maven multi-module structure, 3 demo rules, console output.

## Quick start

```bash
mvn clean install
java -jar reqshift-cli/target/reqshift.jar analyze examples/petstore.yaml
```

## Modules

| Module             | Purpose                                                      |
|--------------------|--------------------------------------------------------------|
| `reqshift-core`    | Domain model, OpenAPI parsing, rule execution engine         |
| `reqshift-rules`   | Lint rules (one class per rule)                              |
| `reqshift-scoring` | A–F multi-axis score calculation                             |
| `reqshift-output`  | Report formatters (console; JSON/SARIF/HTML coming)          |
| `reqshift-cli`     | Picocli command-line entry point, fat JAR                    |
| `reqshift-bom`     | Bill of Materials for library consumers                      |

## License

Apache License 2.0 — see [LICENSE](LICENSE).
