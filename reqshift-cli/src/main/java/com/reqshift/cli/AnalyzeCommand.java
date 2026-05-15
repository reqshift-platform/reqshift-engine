package com.reqshift.cli;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.reqshift.core.engine.RuleEngine;
import com.reqshift.core.model.AnalysisReport;
import com.reqshift.core.model.RuleResult;
import com.reqshift.core.model.Severity;
import com.reqshift.core.parse.OpenApiLoadException;
import com.reqshift.core.parse.OpenApiLoader;
import com.reqshift.output.ConsoleReportFormatter;
import com.reqshift.rules.DefaultRules;
import com.reqshift.scoring.ScoreCalculator;

import io.swagger.v3.oas.models.OpenAPI;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
        name = "analyze",
        description = "Analyse an OpenAPI file and report violations + score.",
        mixinStandardHelpOptions = true)
public final class AnalyzeCommand implements Callable<Integer> {

    private static final Logger log = LoggerFactory.getLogger(AnalyzeCommand.class);

    @Parameters(
            index = "0",
            paramLabel = "FILE",
            description = "Path to the OpenAPI specification (YAML or JSON).")
    private Path file;

    @Option(
            names = "--format",
            defaultValue = "console",
            description = "Output format. Currently only 'console' is supported.")
    private String format;

    @Override
    public Integer call() {
        if (!"console".equalsIgnoreCase(format)) {
            System.err.println(
                    "Unsupported format: "
                            + format
                            + ". Only 'console' is implemented in this version.");
            return 2;
        }

        if (!Files.exists(file)) {
            System.err.println("Error: file not found: " + file);
            return 2;
        }

        log.info("Analysing {}", file);

        OpenAPI api;
        try {
            api = new OpenApiLoader().load(file);
        } catch (OpenApiLoadException e) {
            System.err.println("Error: " + e.getMessage());
            return 2;
        }

        List<RuleResult> results = new RuleEngine(DefaultRules.all()).run(api);
        AnalysisReport report =
                new AnalysisReport(
                        results, new ScoreCalculator().compute(results), file.toString());
        System.out.println(new ConsoleReportFormatter().format(report));

        boolean blocking =
                results.stream()
                        .flatMap(r -> r.violations().stream())
                        .anyMatch(
                                v ->
                                        v.severity() == Severity.ERROR
                                                || v.severity() == Severity.CRITICAL);
        log.debug("Exit code: {}", blocking ? 1 : 0);
        return blocking ? 1 : 0;
    }
}
