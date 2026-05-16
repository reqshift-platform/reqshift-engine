package com.reqshift.output;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.reqshift.core.model.AnalysisReport;
import com.reqshift.core.model.Category;
import com.reqshift.core.model.RuleResult;
import com.reqshift.core.model.Violation;

public final class ConsoleReportFormatter {

    public String format(AnalysisReport report) {
        StringBuilder sb = new StringBuilder();
        sb.append("ReqShift Analysis Report for ").append(report.openApiSource()).append('\n');
        sb.append("============================================================\n\n");
        sb.append("Overall score: ")
                .append(report.score().grade())
                .append(" (")
                .append(report.score().overall())
                .append("/100)");
        if (report.score().isCapped()) {
            sb.append(" (capped from ")
                    .append(report.score().rawOverall())
                    .append(" due to ")
                    .append(report.score().cappedBy())
                    .append(" violation)");
        }
        sb.append("\n\n");

        report.score().byCategory().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> sb.append(String.format("  %-15s %3d%n", e.getKey(), e.getValue())));
        sb.append('\n');

        Map<Category, List<Violation>> violationsByCategory = new EnumMap<>(Category.class);
        for (RuleResult result : report.results()) {
            if (!result.violations().isEmpty()) {
                violationsByCategory
                        .computeIfAbsent(result.rule().category(), k -> new ArrayList<>())
                        .addAll(result.violations());
            }
        }

        if (violationsByCategory.isEmpty()) {
            sb.append("No violations found.\n");
            return sb.toString();
        }

        sb.append("Violations:\n\n");
        violationsByCategory.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(
                        e -> {
                            sb.append("[").append(e.getKey()).append("]\n");
                            e.getValue().stream()
                                    .sorted(
                                            Comparator.comparing(
                                                            (Violation v) -> v.severity().ordinal())
                                                    .reversed())
                                    .forEach(
                                            v -> {
                                                sb.append("  ")
                                                        .append(symbolFor(v))
                                                        .append(" ")
                                                        .append(v.ruleId())
                                                        .append(" ")
                                                        .append(v.severity())
                                                        .append("  ")
                                                        .append(v.message())
                                                        .append('\n');
                                                sb.append("    Location:   ")
                                                        .append(v.location())
                                                        .append('\n');
                                                sb.append("    Suggestion: ")
                                                        .append(v.suggestion())
                                                        .append('\n');
                                            });
                            sb.append('\n');
                        });

        return sb.toString();
    }

    private static String symbolFor(Violation v) {
        return switch (v.severity()) {
            case CRITICAL -> "✗";
            case ERROR -> "✗";
            case WARNING -> "⚠";
            case INFO -> "ℹ";
        };
    }
}
