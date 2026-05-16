# ReqShift Engine

[![CI](https://github.com/reqshift-platform/reqshift-engine/actions/workflows/ci.yml/badge.svg)](https://github.com/reqshift-platform/reqshift-engine/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-25%20LTS-orange)](https://openjdk.org/projects/jdk/25/)

Open source engine for analysing, scoring, and securing REST/OpenAPI specifications.

> Lighthouse for APIs. SonarQube for APIs.

ReqShift parses an OpenAPI 3.x document and runs a battery of conformance, security, design, and documentation rules over it. The output is a graded report (A through F) with actionable violations, suitable for local development, CI gates, and IDE integration.

## Quick start

```bash
# Build the fat JAR
mvn -B verify

# Analyse a spec, get a console report
java -jar reqshift-cli/target/reqshift.jar analyze examples/petstore.yaml

# Machine-readable JSON for CI
java -jar reqshift-cli/target/reqshift.jar analyze examples/petstore.yaml --format json
```

## Features

- **24 rules across 4 categories** (Conformance, Security, Design, Documentation). The catalog is growing toward 50 rules for v1.
- **A through F scoring**, weighted by category (Security 30%, Design 20%, Documentation 15%, Schemas 15%, Conformance 10%, HTTP Codes 10%).
- **Two output formats** today: human-friendly console and machine-readable JSON. SARIF and HTML are on the roadmap.
- **CI-friendly exit codes**: 0 if clean, 1 if any `ERROR` or `CRITICAL` violation, 2 on usage errors.
- **Library-first**: every rule, scorer, and formatter is consumable as a Maven dependency. The CLI is just one consumer.
- **Fast startup** (Picocli, no Spring): the moteur runs in milliseconds. Native image (GraalVM) packaging is planned to reach ~50 ms cold start.

## Usage

```
Usage: reqshift [-hV] [COMMAND]
Analyse an OpenAPI specification for design, security, and documentation
issues.
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
Commands:
  analyze  Analyse an OpenAPI file and report violations + score.
```

```
Usage: reqshift analyze [-hV] [--format=<format>] FILE
Analyse an OpenAPI file and report violations + score.
      FILE             Path to the OpenAPI specification (YAML or JSON).
      --format=<...>   Output format: console (default) or json.
  -h, --help           Show this help message and exit.
  -V, --version        Print version information and exit.
```

### Console output (default)

```text
ReqShift Analysis Report - examples/petstore.yaml
============================================================

Overall score: A (92/100)

  CONFORMANCE      97
  SECURITY         79
  DESIGN           94
  HTTP_CODES      100
  DOCUMENTATION    99
  SCHEMAS         100

Violations:

[SECURITY]
  ✗ SEC001 CRITICAL  Security scheme 'legacyAuth' uses HTTP Basic authentication...
    Location:   #/components/securitySchemes/legacyAuth
    Suggestion: Use OAuth2, OpenID Connect, or a bearer token scheme instead
  ⚠ SEC003 WARNING   No security requirement is applied anywhere in the API...
  ⚠ SEC009 WARNING   Mutating operation POST /pets has no security requirement...

[DESIGN]
  ⚠ DES001 WARNING   Operation GET /pets is missing an operationId...
  ⚠ DES001 WARNING   Operation POST /pets is missing an operationId...

[DOCUMENTATION]
  ℹ DOC001 INFO      API is missing a top-level info.description...
```

### JSON output

```json
{
  "source" : "examples/petstore.yaml",
  "score" : {
    "grade" : "A",
    "overall" : 92,
    "byCategory" : { "SECURITY" : 79, "DESIGN" : 94, "CONFORMANCE" : 97, "..." : "..." }
  },
  "results" : [
    {
      "rule" : { "id" : "SEC001", "severity" : "CRITICAL", "category" : "SECURITY" },
      "violations" : [
        {
          "ruleId" : "SEC001",
          "severity" : "CRITICAL",
          "message" : "Security scheme 'legacyAuth' uses HTTP Basic authentication...",
          "location" : "#/components/securitySchemes/legacyAuth",
          "suggestion" : "Use OAuth2, OpenID Connect, or a bearer token scheme instead"
        }
      ]
    }
  ]
}
```

## Rule catalog (current)

### Conformance (10 rules)

| ID | Severity | Description |
|---|---|---|
| CONF001 | ERROR | OpenAPI version is 3.0.x or 3.1.x |
| CONF002 | WARNING | info.title is present |
| CONF003 | WARNING | info.version is present |
| CONF004 | WARNING | servers is defined |
| CONF005 | ERROR | server URLs are valid http/https |
| CONF006 | WARNING | paths is not empty |
| CONF007 | ERROR | each path starts with a slash |
| CONF008 | ERROR | each operation declares responses |
| CONF009 | WARNING | each operation has a 2xx or default response |
| CONF010 | WARNING | each parameter has a schema or content |

### Security (12 rules)

| ID | Severity | Description |
|---|---|---|
| SEC001 | CRITICAL | no HTTP Basic authentication |
| SEC002 | WARNING | at least one security scheme defined if API has operations |
| SEC003 | WARNING | global or per-operation security requirement set |
| SEC004 | ERROR | no OAuth2 implicit flow (deprecated by OAuth 2.1) |
| SEC005 | ERROR | apiKey not passed in the query string |
| SEC006 | WARNING | each OAuth2 flow declares scopes |
| SEC007 | ERROR | server URLs use https (except localhost) |
| SEC008 | WARNING | http+bearer scheme declares bearerFormat |
| SEC009 | WARNING | mutating operations have a security requirement |
| SEC010 | ERROR | apiKey scheme declares its name |
| SEC011 | ERROR | OAuth2 flows (except implicit) have tokenUrl |
| SEC012 | ERROR | OpenIdConnect scheme has openIdConnectUrl |

### Design (1 rule today, 9 more planned)

| ID | Severity | Description |
|---|---|---|
| DES001 | WARNING | each operation has a unique operationId |

### Documentation (1 rule today, 7 more planned)

| ID | Severity | Description |
|---|---|---|
| DOC001 | INFO | info.description is present and non-empty |

## Exit codes

| Code | Meaning |
|---|---|
| 0 | Clean run, no `ERROR` or `CRITICAL` violations |
| 1 | At least one `ERROR` or `CRITICAL` violation found |
| 2 | Usage error (file not found, unsupported format, invalid spec) |

The split between 1 and 2 lets CI distinguish a real failure of the API spec from a misconfigured pipeline.

## Modules

ReqShift is built as a Maven multi-module project. Each module is independently consumable as a Maven Central artifact.

| Module             | Purpose                                                      |
|--------------------|--------------------------------------------------------------|
| `reqshift-core`    | Domain model, OpenAPI parsing, rule execution engine         |
| `reqshift-rules`   | Rule implementations, one class per rule                     |
| `reqshift-scoring` | A through F multi-axis score calculation                     |
| `reqshift-output`  | Report formatters (console, JSON; SARIF and HTML on the way) |
| `reqshift-cli`     | Picocli command-line entry point, fat JAR                    |
| `reqshift-bom`     | Bill of Materials for library consumers                      |

Use the BOM in your own `pom.xml` to consume multiple modules without version drift:

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.reqshift</groupId>
      <artifactId>reqshift-bom</artifactId>
      <version>0.1.0-SNAPSHOT</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

## Roadmap

**v1 (50 rules total)** is the immediate target. Distribution remains:
- Conformance: 10 (done)
- Security: 12 (done)
- Design REST: 10 (1 done, 9 to go)
- HTTP codes: 5
- Documentation: 8 (1 done, 7 to go)
- Schemas: 5

**Beyond v1**:
- SARIF 2.1.0 output (GitHub Code Scanning integration)
- HTML report
- Native image GraalVM build (Docker, Homebrew, Scoop, install script)
- Configurable rule selection and per-rule severity overrides

## How it compares

ReqShift sits next to existing OpenAPI linters but ships a few opinions:

- **Spectral** (Stoplight) is the de-facto standard. JavaScript runtime, very flexible custom rules, slower cold start. ReqShift targets the same job with native Java performance and a curated rule set out of the box.
- **Redocly CLI** mixes linting, bundling, and previewing. ReqShift focuses purely on linting and scoring.
- **42Crunch CLI** is excellent on security but commercial. ReqShift keeps a complete security ruleset under Apache 2.0.

## Building from source

```bash
git clone git@github.com:reqshift-platform/reqshift-engine.git
cd reqshift-engine
mvn -B verify
```

Requirements:
- JDK 25 (Temurin recommended)
- Maven 3.9+ (the project ships an enforcer rule)

Useful commands:
- `mvn spotless:apply` to auto-format Java sources (Google Java Format AOSP)
- `mvn -pl reqshift-core -am test` to run a single module
- Open `*/target/site/jacoco/index.html` after `mvn verify` for coverage

## License

Apache License 2.0. See [LICENSE](LICENSE).
