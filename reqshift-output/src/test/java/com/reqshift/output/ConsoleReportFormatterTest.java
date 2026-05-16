package com.reqshift.output;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.reqshift.core.model.AnalysisReport;
import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.RuleResult;
import com.reqshift.core.model.Score;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;

class ConsoleReportFormatterTest {

    private final ConsoleReportFormatter formatter = new ConsoleReportFormatter();

    @Test
    void emptyReportShowsPerfectScore() {
        Map<Category, Integer> perfect = new EnumMap<>(Category.class);
        for (Category c : Category.values()) perfect.put(c, 100);
        AnalysisReport report =
                new AnalysisReport(List.of(), new Score('A', perfect, 100, 100, null), "test.yaml");

        String output = formatter.format(report);

        assertThat(output).contains("Overall score: A (100/100)");
        assertThat(output).contains("No violations found");
    }

    @Test
    void reportWithViolationGroupsByCategory() {
        Rule rule =
                new Rule() {
                    @Override
                    public String id() {
                        return "TEST001";
                    }

                    @Override
                    public Severity severity() {
                        return Severity.WARNING;
                    }

                    @Override
                    public Category category() {
                        return Category.DESIGN;
                    }

                    @Override
                    public List<Violation> check(OpenAPI o) {
                        return List.of();
                    }
                };
        Violation v =
                new Violation("TEST001", Severity.WARNING, "Something wrong", "#/x", "Do better");
        RuleResult result = new RuleResult(rule, List.of(v));
        Map<Category, Integer> scores = new EnumMap<>(Category.class);
        for (Category c : Category.values()) scores.put(c, 100);
        scores.put(Category.DESIGN, 97);

        AnalysisReport report =
                new AnalysisReport(
                        List.of(result), new Score('A', scores, 99, 99, null), "test.yaml");

        String output = formatter.format(report);

        assertThat(output).contains("[DESIGN]");
        assertThat(output).contains("TEST001");
        assertThat(output).contains("Something wrong");
        assertThat(output).contains("Do better");
    }
}
