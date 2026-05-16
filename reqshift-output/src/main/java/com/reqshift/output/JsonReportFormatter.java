package com.reqshift.output;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.reqshift.core.model.AnalysisReport;
import com.reqshift.core.model.RuleResult;

public final class JsonReportFormatter {

    private final ObjectMapper mapper =
            new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public String format(AnalysisReport report) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("source", report.openApiSource());

        Map<String, Object> score = new LinkedHashMap<>();
        score.put("grade", String.valueOf(report.score().grade()));
        score.put("overall", report.score().overall());
        score.put("rawOverall", report.score().rawOverall());
        score.put(
                "cappedBy",
                report.score().cappedBy() == null ? null : report.score().cappedBy().name());
        score.put("byCategory", report.score().byCategory());
        root.put("score", score);

        List<Map<String, Object>> results = new ArrayList<>();
        for (RuleResult result : report.results()) {
            if (result.violations().isEmpty()) {
                continue;
            }
            Map<String, Object> ruleInfo = new LinkedHashMap<>();
            ruleInfo.put("id", result.rule().id());
            ruleInfo.put("severity", result.rule().severity().name());
            ruleInfo.put("category", result.rule().category().name());

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("rule", ruleInfo);
            entry.put("violations", result.violations());
            results.add(entry);
        }
        root.put("results", results);

        try {
            return mapper.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new ReportSerializationException("Failed to serialize report to JSON", e);
        }
    }
}
