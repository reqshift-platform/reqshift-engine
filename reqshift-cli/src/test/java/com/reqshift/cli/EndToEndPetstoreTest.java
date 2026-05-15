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

import picocli.CommandLine;

class EndToEndPetstoreTest {

    private static final Path PETSTORE = Path.of("..", "examples", "petstore.yaml");

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
    void analyseBundledPetstoreExample() {
        assertThat(Files.exists(PETSTORE))
                .as("examples/petstore.yaml should ship with the project")
                .isTrue();

        int exitCode = new CommandLine(new ReqshiftCli()).execute("analyze", PETSTORE.toString());

        String output = capturedOut.toString(StandardCharsets.UTF_8);

        assertThat(exitCode)
                .as("petstore.yaml has a CRITICAL violation, so the CLI must exit 1")
                .isEqualTo(1);

        assertThat(output).contains("Overall score: A");
        assertThat(output).contains("(94/100)");

        // Expected violations
        assertThat(output).contains("SEC001");
        assertThat(output).contains("legacyAuth");
        assertThat(output).contains("DES001");
        assertThat(output).contains("GET /pets");
        assertThat(output).contains("POST /pets");
        assertThat(output).contains("DOC001");

        // /pets/{petId} has operationId, must not appear as a violation
        assertThat(output).doesNotContain("GET /pets/{petId} is missing");
    }
}
