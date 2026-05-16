package com.reqshift.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import picocli.CommandLine;

class AnalyzeCommandTest {

    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;
    private ByteArrayOutputStream capturedOut;
    private ByteArrayOutputStream capturedErr;

    @BeforeEach
    void redirect() {
        capturedOut = new ByteArrayOutputStream();
        capturedErr = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedOut, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(capturedErr, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void restore() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    void exitsZeroForCleanSpec(@TempDir Path tmp) throws Exception {
        Path spec = tmp.resolve("clean.yaml");
        Files.writeString(
                spec,
                """
                openapi: 3.0.3
                info:
                  title: Clean API
                  version: 1.0.0
                  description: A perfectly fine API description.
                  contact: {name: Team, email: api@example.com}
                  license: {name: Apache 2.0}
                servers:
                  - url: https://api.example.com
                security:
                  - bearerAuth: []
                paths:
                  /pets:
                    get:
                      summary: List all pets
                      description: Returns every pet currently in the store.
                      tags: [pets]
                      operationId: listPets
                      responses:
                        '200':
                          description: ok
                        '4XX':
                          description: client error
                components:
                  securitySchemes:
                    bearerAuth:
                      type: http
                      scheme: bearer
                      bearerFormat: JWT
                """);

        int exitCode = execute("analyze", spec.toString());

        assertThat(exitCode).isEqualTo(0);
        assertThat(stdout()).contains("Overall score: A");
        assertThat(stdout()).contains("No violations found");
    }

    @Test
    void exitsOneWhenCriticalViolationDetected(@TempDir Path tmp) throws Exception {
        Path spec = tmp.resolve("dirty.yaml");
        Files.writeString(
                spec,
                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths: {}
                components:
                  securitySchemes:
                    legacy:
                      type: http
                      scheme: basic
                """);

        int exitCode = execute("analyze", spec.toString());

        assertThat(exitCode).isEqualTo(1);
        assertThat(stdout()).contains("SEC001");
        assertThat(stdout()).contains("CRITICAL");
    }

    @Test
    void exitsTwoWhenFileMissing() {
        int exitCode = execute("analyze", "/path/does/not/exist.yaml");

        assertThat(exitCode).isEqualTo(2);
        assertThat(stderr()).contains("file not found");
    }

    @Test
    void exitsTwoForUnsupportedFormat(@TempDir Path tmp) throws Exception {
        Path spec = tmp.resolve("a.yaml");
        Files.writeString(spec, "openapi: 3.0.3\ninfo: {title: T, version: 1.0.0}\npaths: {}\n");

        int exitCode = execute("analyze", spec.toString(), "--format", "xml");

        assertThat(exitCode).isEqualTo(2);
        assertThat(stderr()).contains("Unsupported format");
    }

    @Test
    void jsonFormatProducesParseableOutput(@TempDir Path tmp) throws Exception {
        Path spec = tmp.resolve("clean.yaml");
        Files.writeString(
                spec,
                """
                openapi: 3.0.3
                info:
                  title: Clean API
                  version: 1.0.0
                  description: A perfectly fine API description.
                  contact: {name: Team, email: api@example.com}
                  license: {name: Apache 2.0}
                servers:
                  - url: https://api.example.com
                security:
                  - bearerAuth: []
                paths:
                  /pets:
                    get:
                      summary: List all pets
                      description: Returns every pet currently in the store.
                      tags: [pets]
                      operationId: listPets
                      responses:
                        '200':
                          description: ok
                        '4XX':
                          description: client error
                components:
                  securitySchemes:
                    bearerAuth:
                      type: http
                      scheme: bearer
                      bearerFormat: JWT
                """);

        int exitCode = execute("analyze", spec.toString(), "--format", "json");

        assertThat(exitCode).isEqualTo(0);
        String output = stdout();
        assertThat(output).startsWith("{");
        assertThat(output).contains("\"grade\" : \"A\"");
        assertThat(output).contains("\"overall\" : 100");
        assertThat(output).contains("\"results\" : [ ]");
    }

    @Test
    void sarifFormatProducesValidSarifJson(@TempDir Path tmp) throws Exception {
        Path spec = tmp.resolve("dirty.yaml");
        Files.writeString(
                spec,
                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths: {}
                components:
                  securitySchemes:
                    legacy:
                      type: http
                      scheme: basic
                """);

        int exitCode = execute("analyze", spec.toString(), "--format", "sarif");

        assertThat(exitCode).isEqualTo(1);
        String output = stdout();
        assertThat(output).startsWith("{");
        assertThat(output).contains("\"version\" : \"2.1.0\"");
        assertThat(output).contains("\"name\" : \"reqshift\"");
        assertThat(output).contains("\"ruleId\" : \"SEC001\"");
        assertThat(output).contains("\"level\" : \"error\"");
    }

    @Test
    void disableFlagHidesRule(@TempDir Path tmp) throws Exception {
        Path spec = tmp.resolve("dirty.yaml");
        Files.writeString(
                spec,
                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths: {}
                components:
                  securitySchemes:
                    legacy:
                      type: http
                      scheme: basic
                """);

        int exitCode = execute("analyze", spec.toString(), "--disable", "SEC001");

        assertThat(stdout()).doesNotContain("SEC001");
        assertThat(exitCode).isNotEqualTo(2);
    }

    @Test
    void severityFlagOverridesRule(@TempDir Path tmp) throws Exception {
        Path spec = tmp.resolve("dirty.yaml");
        Files.writeString(
                spec,
                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths: {}
                components:
                  securitySchemes:
                    legacy:
                      type: http
                      scheme: basic
                """);

        int exitCode = execute("analyze", spec.toString(), "--severity", "SEC001=INFO");

        assertThat(stdout()).contains("SEC001");
        assertThat(stdout()).doesNotContain("CRITICAL");
        assertThat(exitCode).isEqualTo(0);
    }

    @Test
    void configFlagReadsYamlFile(@TempDir Path tmp) throws Exception {
        Path cfg = tmp.resolve(".reqshift.yml");
        Files.writeString(
                cfg,
                """
                rules:
                  disabled: [SEC001]
                """);
        Path spec = tmp.resolve("dirty.yaml");
        Files.writeString(
                spec,
                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths: {}
                components:
                  securitySchemes:
                    legacy:
                      type: http
                      scheme: basic
                """);

        int exitCode = execute("analyze", spec.toString(), "--config", cfg.toString());

        assertThat(stdout()).doesNotContain("SEC001");
        assertThat(exitCode).isNotEqualTo(2);
    }

    @Test
    void exitsTwoWhenConfigFileMissing(@TempDir Path tmp) throws Exception {
        Path spec = tmp.resolve("a.yaml");
        Files.writeString(spec, "openapi: 3.0.3\ninfo: {title: T, version: 1.0.0}\npaths: {}\n");

        int exitCode =
                execute(
                        "analyze",
                        spec.toString(),
                        "--config",
                        tmp.resolve("missing.yml").toString());

        assertThat(exitCode).isEqualTo(2);
        assertThat(stderr()).contains("Configuration file not found");
    }

    @Test
    void exitsTwoForInvalidSeverityValue(@TempDir Path tmp) throws Exception {
        Path spec = tmp.resolve("a.yaml");
        Files.writeString(spec, "openapi: 3.0.3\ninfo: {title: T, version: 1.0.0}\npaths: {}\n");

        int exitCode = execute("analyze", spec.toString(), "--severity", "SEC001=NOPE");

        assertThat(exitCode).isEqualTo(2);
        assertThat(stderr()).contains("Invalid severity");
    }

    @Test
    void versionFlagReportsVersion() {
        int exitCode = execute("--version");

        assertThat(exitCode).isEqualTo(0);
        // Picocli writes --version to its own PrintWriter, which defaults to System.out
        assertThat(stdout()).contains("reqshift");
    }

    private int execute(String... args) {
        return new CommandLine(new ReqshiftCli()).execute(args);
    }

    private String stdout() {
        return capturedOut.toString(StandardCharsets.UTF_8);
    }

    private String stderr() {
        return capturedErr.toString(StandardCharsets.UTF_8);
    }
}
