package com.reqshift.output;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.reqshift.core.model.AnalysisReport;
import com.reqshift.core.model.RuleResult;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

public final class SarifReportFormatter {

    private static final String SARIF_VERSION = "2.1.0";
    private static final String SARIF_SCHEMA =
            "https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json";
    private static final String TOOL_NAME = "reqshift";
    private static final String INFORMATION_URI =
            "https://github.com/reqshift-platform/reqshift-engine";

    private final ObjectMapper mapper =
            new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private final String toolVersion;

    public SarifReportFormatter() {
        this("dev");
    }

    public SarifReportFormatter(String toolVersion) {
        this.toolVersion = toolVersion == null || toolVersion.isBlank() ? "dev" : toolVersion;
    }

    public String format(AnalysisReport report) {
        Map<String, Integer> ruleIndex = new LinkedHashMap<>();
        List<Map<String, Object>> ruleDescriptors = new ArrayList<>();
        List<Map<String, Object>> results = new ArrayList<>();

        Set<String> seen = new LinkedHashSet<>();
        for (RuleResult result : report.results()) {
            if (result.violations().isEmpty()) {
                continue;
            }
            String id = result.rule().id();
            if (seen.add(id)) {
                ruleIndex.put(id, ruleDescriptors.size());
                ruleDescriptors.add(toRuleDescriptor(result));
            }
            for (Violation v : result.violations()) {
                results.add(toResult(v, ruleIndex.get(id), report.openApiSource()));
            }
        }

        Map<String, Object> driver = new LinkedHashMap<>();
        driver.put("name", TOOL_NAME);
        driver.put("version", toolVersion);
        driver.put("informationUri", INFORMATION_URI);
        driver.put("rules", ruleDescriptors);

        Map<String, Object> tool = Map.of("driver", driver);
        Map<String, Object> run = new LinkedHashMap<>();
        run.put("tool", tool);
        run.put("results", results);

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("$schema", SARIF_SCHEMA);
        root.put("version", SARIF_VERSION);
        root.put("runs", List.of(run));

        try {
            return mapper.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new ReportSerializationException("Failed to serialize report to SARIF", e);
        }
    }

    private Map<String, Object> toRuleDescriptor(RuleResult result) {
        Map<String, Object> descriptor = new LinkedHashMap<>();
        descriptor.put("id", result.rule().id());
        descriptor.put("name", result.rule().id());
        String text =
                result.violations().isEmpty()
                        ? result.rule().id()
                        : result.violations().get(0).message();
        descriptor.put("shortDescription", Map.of("text", text));
        descriptor.put("defaultConfiguration", Map.of("level", toLevel(result.rule().severity())));
        descriptor.put(
                "properties",
                Map.of(
                        "category",
                        result.rule().category().name(),
                        "severity",
                        result.rule().severity().name()));
        return descriptor;
    }

    private Map<String, Object> toResult(Violation v, int ruleIndex, String source) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("ruleId", v.ruleId());
        entry.put("ruleIndex", ruleIndex);
        entry.put("level", toLevel(v.severity()));
        entry.put("message", Map.of("text", buildMessage(v)));

        Map<String, Object> physicalLocation = new LinkedHashMap<>();
        physicalLocation.put("artifactLocation", Map.of("uri", source == null ? "" : source));

        Map<String, Object> location = new LinkedHashMap<>();
        location.put("physicalLocation", physicalLocation);
        if (v.location() != null && !v.location().isBlank()) {
            location.put(
                    "logicalLocations",
                    List.of(Map.of("fullyQualifiedName", v.location(), "kind", "jsonPointer")));
        }
        entry.put("locations", List.of(location));
        return entry;
    }

    private static String buildMessage(Violation v) {
        if (v.suggestion() == null || v.suggestion().isBlank()) {
            return v.message();
        }
        return v.message() + " Suggestion: " + v.suggestion();
    }

    static String toLevel(Severity severity) {
        return switch (severity) {
            case CRITICAL, ERROR -> "error";
            case WARNING -> "warning";
            case INFO -> "note";
        };
    }
}
