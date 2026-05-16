package com.reqshift.cli;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.reqshift.core.config.RuleConfig;
import com.reqshift.core.config.RuleConfigException;
import com.reqshift.core.config.RuleConfigLoader;
import com.reqshift.core.engine.RuleEngine;
import com.reqshift.core.model.AnalysisReport;
import com.reqshift.core.model.RuleResult;
import com.reqshift.core.model.Severity;
import com.reqshift.core.parse.OpenApiLoadException;
import com.reqshift.core.parse.OpenApiLoader;
import com.reqshift.output.ConsoleReportFormatter;
import com.reqshift.output.HtmlReportFormatter;
import com.reqshift.output.JsonReportFormatter;
import com.reqshift.output.SarifReportFormatter;
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
            description = "Output format: console (default), json, sarif, or html.")
    private String format;

    @Option(
            names = "--config",
            description =
                    "Path to a ReqShift configuration file (YAML). "
                            + "If omitted, .reqshift.yml is auto-detected in the current"
                            + " directory and its parent.")
    private Path configFile;

    @Option(
            names = "--disable",
            split = ",",
            description =
                    "Comma-separated list of rule IDs to disable "
                            + "(e.g. --disable SEC001,DES005). Cumulative with --config.")
    private List<String> disabledRules;

    @Option(
            names = "--severity",
            description =
                    "Override the severity of a rule (repeatable). "
                            + "Format: --severity RULE_ID=SEVERITY "
                            + "(e.g. --severity DES010=INFO). Cumulative with --config.")
    private Map<String, String> severityOverrides;

    @Override
    public Integer call() {
        String fmt = format == null ? "console" : format.toLowerCase();
        if (!fmt.equals("console")
                && !fmt.equals("json")
                && !fmt.equals("sarif")
                && !fmt.equals("html")) {
            System.err.println(
                    "Unsupported format: "
                            + format
                            + ". Supported formats: console, json, sarif, html.");
            return 2;
        }

        if (!Files.exists(file)) {
            System.err.println("Error: file not found: " + file);
            return 2;
        }

        RuleConfig effectiveConfig;
        try {
            effectiveConfig = resolveConfig();
        } catch (RuleConfigException e) {
            System.err.println("Error: " + e.getMessage());
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

        List<RuleResult> results =
                RuleEngine.fromRules(DefaultRules.all(), effectiveConfig).run(api);
        AnalysisReport report =
                new AnalysisReport(
                        results, new ScoreCalculator().compute(results), file.toString());

        String rendered =
                switch (fmt) {
                    case "json" -> new JsonReportFormatter().format(report);
                    case "sarif" ->
                            new SarifReportFormatter(ManifestVersionProvider.currentVersion())
                                    .format(report);
                    case "html" ->
                            new HtmlReportFormatter(ManifestVersionProvider.currentVersion())
                                    .format(report);
                    default -> new ConsoleReportFormatter().format(report);
                };
        System.out.println(rendered);

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

    private RuleConfig resolveConfig() {
        RuleConfigLoader loader = new RuleConfigLoader();
        RuleConfig base;
        if (configFile != null) {
            if (!Files.exists(configFile)) {
                throw new RuleConfigException("Configuration file not found: " + configFile);
            }
            base = loader.load(configFile);
        } else {
            Optional<Path> auto = loader.autodetect(Path.of(".").toAbsolutePath().normalize());
            base = auto.map(loader::load).orElse(RuleConfig.empty());
            auto.ifPresent(p -> log.info("Using configuration from {}", p));
        }
        return mergeCliOverrides(base);
    }

    private RuleConfig mergeCliOverrides(RuleConfig base) {
        Set<String> disabled = new HashSet<>(base.disabled());
        if (disabledRules != null) {
            for (String id : disabledRules) {
                if (id != null && !id.isBlank()) {
                    disabled.add(id.trim());
                }
            }
        }
        Map<String, Severity> overrides = new HashMap<>(base.severityOverrides());
        if (severityOverrides != null) {
            for (Map.Entry<String, String> e : severityOverrides.entrySet()) {
                String ruleId = e.getKey() == null ? null : e.getKey().trim();
                String raw = e.getValue();
                if (ruleId == null || ruleId.isBlank() || raw == null) {
                    continue;
                }
                try {
                    overrides.put(
                            ruleId,
                            Severity.valueOf(raw.trim().toUpperCase(java.util.Locale.ROOT)));
                } catch (IllegalArgumentException ex) {
                    throw new RuleConfigException(
                            "Invalid severity '"
                                    + raw
                                    + "' for rule "
                                    + ruleId
                                    + ". Expected one of INFO, WARNING, ERROR, CRITICAL.");
                }
            }
        }
        return new RuleConfig(disabled, overrides);
    }
}
