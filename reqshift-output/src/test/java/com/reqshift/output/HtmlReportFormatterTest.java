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

class HtmlReportFormatterTest {

    @Test
    void emptyReportHasNoViolationsSection() {
        AnalysisReport report = perfectReport();
        String html = new HtmlReportFormatter("1.0.0").format(report);

        assertThat(html).startsWith("<!DOCTYPE html>");
        assertThat(html).contains("<title>ReqShift Report");
        assertThat(html).contains("test.yaml");
        assertThat(html).contains("No violations found");
        assertThat(html).contains("ReqShift 1.0.0");
        assertThat(html).contains(">A<");
        assertThat(html).contains("100 / 100");
    }

    @Test
    void reportWithViolationsRendersBadgesAndDetails() {
        Rule rule = fakeRule("SEC001", Severity.CRITICAL, Category.SECURITY);
        Violation v =
                new Violation(
                        "SEC001",
                        Severity.CRITICAL,
                        "Uses HTTP <Basic>",
                        "#/components/securitySchemes/legacy",
                        "Use OAuth2 \"instead\"");
        AnalysisReport report =
                new AnalysisReport(
                        List.of(new RuleResult(rule, List.of(v))),
                        scoreOf('C', 75),
                        "petstore.yaml");

        String html = new HtmlReportFormatter().format(report);

        assertThat(html).contains("SECURITY");
        assertThat(html).contains("class=\"badge\">CRITICAL");
        assertThat(html).contains("SEC001");
        assertThat(html).contains("Uses HTTP &lt;Basic&gt;");
        assertThat(html).contains("Use OAuth2 &quot;instead&quot;");
        assertThat(html).contains("#/components/securitySchemes/legacy");
    }

    @Test
    void cappedScoreIsDisplayed() {
        Map<Category, Integer> scores = new EnumMap<>(Category.class);
        for (Category c : Category.values()) {
            scores.put(c, 85);
        }
        AnalysisReport report =
                new AnalysisReport(
                        List.of(), new Score('C', scores, 79, 89, Severity.CRITICAL), "x.yaml");

        String html = new HtmlReportFormatter().format(report);

        assertThat(html).contains("Capped from 89");
        assertThat(html).contains("CRITICAL violation");
    }

    @Test
    void htmlIsSelfContainedSingleFile() {
        AnalysisReport report = perfectReport();
        String html = new HtmlReportFormatter().format(report);

        assertThat(html).contains("<style>");
        assertThat(html).doesNotContain("<script");
        assertThat(html).doesNotContain("<link ");
        assertThat(html).contains("</html>");
    }

    @Test
    void escapesHtmlSpecialChars() {
        assertThat(HtmlReportFormatter.escape("<a>&b</a>")).isEqualTo("&lt;a&gt;&amp;b&lt;/a&gt;");
        assertThat(HtmlReportFormatter.escape("\"x'y\"")).isEqualTo("&quot;x&#39;y&quot;");
        assertThat(HtmlReportFormatter.escape(null)).isEmpty();
    }

    @Test
    void severityColorsCoverAllSeverities() {
        assertThat(HtmlReportFormatter.colorForSeverity(Severity.CRITICAL)).isEqualTo("#d32f2f");
        assertThat(HtmlReportFormatter.colorForSeverity(Severity.ERROR)).isEqualTo("#f57c00");
        assertThat(HtmlReportFormatter.colorForSeverity(Severity.WARNING)).isEqualTo("#fbc02d");
        assertThat(HtmlReportFormatter.colorForSeverity(Severity.INFO)).isEqualTo("#1976d2");
    }

    @Test
    void gradeColorsCoverAllGrades() {
        assertThat(HtmlReportFormatter.colorForGrade('A')).isEqualTo("#2e7d32");
        assertThat(HtmlReportFormatter.colorForGrade('B')).isEqualTo("#689f38");
        assertThat(HtmlReportFormatter.colorForGrade('C')).isEqualTo("#fbc02d");
        assertThat(HtmlReportFormatter.colorForGrade('D')).isEqualTo("#f57c00");
        assertThat(HtmlReportFormatter.colorForGrade('F')).isEqualTo("#d32f2f");
    }

    private static Rule fakeRule(String id, Severity severity, Category category) {
        return new Rule() {
            @Override
            public String id() {
                return id;
            }

            @Override
            public Severity severity() {
                return severity;
            }

            @Override
            public Category category() {
                return category;
            }

            @Override
            public List<Violation> check(OpenAPI o) {
                return List.of();
            }
        };
    }

    private static AnalysisReport perfectReport() {
        Map<Category, Integer> perfect = new EnumMap<>(Category.class);
        for (Category c : Category.values()) {
            perfect.put(c, 100);
        }
        return new AnalysisReport(List.of(), new Score('A', perfect, 100, 100, null), "test.yaml");
    }

    private static Score scoreOf(char grade, int overall) {
        Map<Category, Integer> scores = new EnumMap<>(Category.class);
        for (Category c : Category.values()) {
            scores.put(c, overall);
        }
        return new Score(grade, scores, overall, overall, null);
    }
}
