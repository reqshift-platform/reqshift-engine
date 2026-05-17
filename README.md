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

- **50 rules across 6 categories** (Conformance, Security, Design, HTTP codes, Documentation, Schemas). Full v1 catalog implemented.
- **A through F scoring**, weighted by category (Security 30%, Design 20%, Documentation 15%, Schemas 15%, Conformance 10%, HTTP Codes 10%) with a **severity cap**: one `CRITICAL` violation caps the grade at C, one `ERROR` caps it at B. The raw weighted score is still exposed so consumers can see how far below the cap the spec really is.
- **Four output formats**: console, JSON, SARIF 2.1.0 (GitHub Code Scanning), and standalone HTML.
- **CI-friendly exit codes**: 0 if clean, 1 if any `ERROR` or `CRITICAL` violation, 2 on usage errors.
- **Library-first**: every rule, scorer, and formatter is consumable as a Maven dependency. The CLI is just one consumer.
- **Fast startup** (Picocli, no Spring): the engine runs in milliseconds. A GraalVM native image is also published per release, with a sub-50 ms cold start.

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
Usage: reqshift analyze [-hV] [--config=<configFile>] [--format=<format>]
                        [--disable=<disabledRules>[,<disabledRules>...]]...
                        [--severity=<String=String>]... FILE
Analyse an OpenAPI file and report violations + score.
      FILE                       Path to the OpenAPI specification (YAML or JSON).
      --format=<...>             Output format: console (default), json, sarif, or html.
      --config=<configFile>      Path to a ReqShift configuration file (YAML).
                                 If omitted, .reqshift.yml is auto-detected in the
                                 current directory and its parent.
      --disable=<id,id,...>      Disable one or more rules by ID. Cumulative with --config.
      --severity=<ID=SEVERITY>   Override the severity of a rule (repeatable).
                                 Cumulative with --config.
  -h, --help                     Show this help message and exit.
  -V, --version                  Print version information and exit.
```

### Configuration

ReqShift can be customised through a YAML configuration file. Drop a `.reqshift.yml`
at the root of the project being analysed (it is auto-detected from the current
directory and its parent), or pass an explicit path via `--config`.

```yaml
rules:
  disabled:
    - SEC001
    - DES005
  severity:
    DES010: INFO
    DOC004: WARNING
```

CLI flags layer on top of the file and are cumulative:

```bash
reqshift analyze openapi.yaml \
  --config team-rules.yml \
  --disable SEC001,SEC003 \
  --severity DES010=INFO \
  --severity DOC004=WARNING
```

Severity values are `INFO`, `WARNING`, `ERROR`, `CRITICAL` (case-insensitive).

### Console output (default)

```text
ReqShift Analysis Report for examples/petstore.yaml
============================================================

Overall score: C (79/100) (capped from 89 due to CRITICAL violation)

  CONFORMANCE      97
  SECURITY         79
  DESIGN           91
  HTTP_CODES       88
  DOCUMENTATION    93
  SCHEMAS         100

Violations:

[SECURITY]
  ✗ SEC001 CRITICAL  Security scheme 'legacyAuth' uses HTTP Basic authentication...
    Location:   #/components/securitySchemes/legacyAuth
    Suggestion: Use OAuth2, OpenID Connect, or a bearer token scheme instead
  ⚠ SEC003 WARNING   No security requirement is applied anywhere in the API...
  ⚠ SEC009 WARNING   Mutating operation POST /pets has no security requirement...
... (and other DESIGN, DOCUMENTATION, HTTP_CODES violations)
```

### JSON output

```json
{
  "source" : "examples/petstore.yaml",
  "score" : {
    "grade" : "C",
    "overall" : 79,
    "rawOverall" : 89,
    "cappedBy" : "CRITICAL",
    "byCategory" : { "SECURITY" : 79, "DESIGN" : 91, "CONFORMANCE" : 97, "..." : "..." }
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

### SARIF 2.1.0 output (GitHub Code Scanning)

```bash
reqshift analyze openapi.yaml --format sarif > reqshift.sarif
```

The output follows the OASIS SARIF 2.1.0 schema and can be uploaded to GitHub Code
Scanning via [`github/codeql-action/upload-sarif`](https://github.com/github/codeql-action):

```yaml
name: API lint
on: [push, pull_request]
jobs:
  reqshift:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: 25 }
      - run: |
          curl -L -o reqshift.jar https://github.com/reqshift-platform/reqshift-engine/releases/latest/download/reqshift.jar
          java -jar reqshift.jar analyze openapi.yaml --format sarif > reqshift.sarif || true
      - uses: github/codeql-action/upload-sarif@v3
        with:
          sarif_file: reqshift.sarif
```

Violations then surface in the GitHub Security tab as code-scanning alerts.

### HTML report

```bash
reqshift analyze openapi.yaml --format html > report.html
open report.html
```

A single self-contained HTML file (inline CSS, no JavaScript, no external assets)
with a score gauge, per-category breakdown, and collapsible violation details
grouped by category. Ideal to share an audit with non-developers.

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

### HTTP codes (5 rules)

| ID | Severity | Description |
|---|---|---|
| HTTP001 | ERROR | GET and DELETE must not declare a requestBody |
| HTTP002 | WARNING | each operation documents at least one 4xx, 5xx, or default response |
| HTTP003 | ERROR | status codes are 100-599, a 1XX-5XX wildcard, or default |
| HTTP004 | WARNING | 204 responses do not declare a content body |
| HTTP005 | WARNING | POST returning 201 declares a Location header |

### Design (10 rules)

| ID | Severity | Description |
|---|---|---|
| DES001 | WARNING | each operation has an operationId |
| DES002 | WARNING | path segments are kebab-case |
| DES003 | INFO | collection paths are plural (with allowlist for /health, /status, etc.) |
| DES004 | WARNING | no trailing slash |
| DES005 | WARNING | no verbs in path segments |
| DES006 | ERROR | operationId is unique across the API |
| DES007 | INFO | each operation has a summary |
| DES008 | INFO | each operation has at least one tag |
| DES009 | ERROR | each `{xxx}` template has a matching `in: path` parameter |
| DES010 | WARNING | operationId is camelCase |

### Documentation (8 rules)

| ID | Severity | Description |
|---|---|---|
| DOC001 | INFO | info.description is present and non-empty |
| DOC002 | INFO | info.contact has an email or url |
| DOC003 | INFO | info.license has a name |
| DOC004 | INFO | each operation has a description |
| DOC005 | INFO | each parameter has a description |
| DOC006 | INFO | each schema has a description |
| DOC007 | INFO | each schema has an example |
| DOC008 | INFO | each global tag has a description |

### Schemas (5 rules)

| ID | Severity | Description |
|---|---|---|
| SCHEMAS001 | WARNING | object schemas declare properties, additionalProperties, or composition |
| SCHEMAS002 | ERROR | required[] only references declared properties |
| SCHEMAS003 | ERROR | array schemas declare items |
| SCHEMAS004 | WARNING | string properties have maxLength, format, pattern, or enum |
| SCHEMAS005 | INFO | schemas in components/schemas are referenced via $ref |

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
| `reqshift-output`  | Report formatters (console, JSON, SARIF, HTML)               |
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

**v1 (50 rules)** is complete:
- Conformance: 10
- Security: 12
- HTTP codes: 5
- Design REST: 10
- Documentation: 8
- Schemas: 5

**Beyond v1**:
- Configurable rule selection and per-rule severity overrides (done)
- SARIF 2.1.0 output (GitHub Code Scanning integration) (done)
- HTML report (done)
- Native image GraalVM build (done)
- Docker image FROM scratch (done)
- Maven Central publication
- Homebrew, Scoop, install script

## How it compares

ReqShift sits next to existing OpenAPI linters but ships a few opinions:

- **Spectral** (Stoplight) is the de-facto standard. JavaScript runtime, very flexible custom rules, slower cold start. ReqShift targets the same job with native Java performance and a curated rule set out of the box.
- **Redocly CLI** mixes linting, bundling, and previewing. ReqShift focuses purely on linting and scoring.
- **42Crunch CLI** is excellent on security but commercial. ReqShift keeps a complete security ruleset under Apache 2.0.

## Native binary

A self-contained native executable (no JVM required) is published for each release
via GitHub Actions. Cold start is around 50 ms, on par with native CLIs.

| Asset                          | Platform           |
|--------------------------------|--------------------|
| `reqshift-linux-x64`           | Linux x86_64       |
| `reqshift-macos-arm64`         | macOS Apple Silicon|

> macOS Intel and Windows native binaries are planned for a later release.
> In the meantime, use the fat JAR (`reqshift.jar`, requires a JRE 25+).

Download the asset for your platform from the
[Releases page](https://github.com/reqshift-platform/reqshift-engine/releases),
make it executable, and run it:

```bash
curl -L -o reqshift https://github.com/reqshift-platform/reqshift-engine/releases/latest/download/reqshift-linux-x64
chmod +x reqshift
./reqshift analyze openapi.yaml
```

To build the native binary locally, install GraalVM CE 25 (for example via
`sdk install java 25.0.1-graalce`) and run:

```bash
mvn -B -Pnative -DskipTests -pl reqshift-cli -am package
./reqshift-cli/target/reqshift --version
```

The native build uses GraalVM hints shipped in
`reqshift-cli/src/main/resources/META-INF/native-image/`. Regenerate them with
the tracing agent if you add a code path that uses new reflection:

```bash
java -agentlib:native-image-agent=config-merge-dir=reqshift-cli/src/main/resources/META-INF/native-image/com.reqshift/reqshift-cli \
     -jar reqshift-cli/target/reqshift.jar analyze examples/petstore.yaml
```

## Docker

A `FROM scratch` image (~25 MB total) is published per release on GHCR. The binary
is fully statically linked against musl libc, so the image carries no OS, no shell,
no package manager — just the executable.

```bash
docker run --rm -v "$PWD:/work" ghcr.io/reqshift-platform/reqshift:latest \
    analyze /work/openapi.yaml
```

Tags : `latest`, `<major>.<minor>` (e.g. `0.1`), and `<full-version>` (e.g. `0.1.0`).
Release candidates are published as `0.1.0-rc1` etc. but do not move the `latest` tag.

Build the image locally (requires Docker):

```bash
docker build -t reqshift:local .
docker run --rm -v "$PWD/examples:/work" reqshift:local analyze /work/petstore.yaml
```

The Dockerfile uses a multi-stage build that compiles musl + static zlib in the
builder stage, then invokes `mvn -Pnative-static` to produce a fully static
binary, copied into a `FROM scratch` final image.

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
