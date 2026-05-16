package com.reqshift.output;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reqshift.core.model.AnalysisReport;
import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.RuleResult;
import com.reqshift.core.model.Score;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;

class SarifReportFormatterTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void emptyReportProducesValidSarif() throws Exception {
        AnalysisReport report = perfectReport();

        String sarif = new SarifReportFormatter("1.2.3").format(report);
        JsonNode root = mapper.readTree(sarif);

        assertThat(root.get("version").asText()).isEqualTo("2.1.0");
        assertThat(root.get("$schema").asText()).contains("sarif");
        JsonNode runs = root.get("runs");
        assertThat(runs.isArray()).isTrue();
        assertThat(runs).hasSize(1);

        JsonNode driver = runs.get(0).get("tool").get("driver");
        assertThat(driver.get("name").asText()).isEqualTo("reqshift");
        assertThat(driver.get("version").asText()).isEqualTo("1.2.3");
        assertThat(driver.get("rules").isArray()).isTrue();
        assertThat(driver.get("rules")).isEmpty();

        assertThat(runs.get(0).get("results").isArray()).isTrue();
        assertThat(runs.get(0).get("results")).isEmpty();
    }

    @Test
    void mapsSeverityToSarifLevel() {
        assertThat(SarifReportFormatter.toLevel(Severity.CRITICAL)).isEqualTo("error");
        assertThat(SarifReportFormatter.toLevel(Severity.ERROR)).isEqualTo("error");
        assertThat(SarifReportFormatter.toLevel(Severity.WARNING)).isEqualTo("warning");
        assertThat(SarifReportFormatter.toLevel(Severity.INFO)).isEqualTo("note");
    }

    @Test
    void emitsRuleDescriptorsAndResults() throws Exception {
        Rule rule = fakeRule("SEC001", Severity.CRITICAL, Category.SECURITY);
        Violation v1 =
                new Violation(
                        "SEC001",
                        Severity.CRITICAL,
                        "Uses HTTP Basic",
                        "#/components/securitySchemes/legacy",
                        "Use OAuth2 instead");
        Violation v2 =
                new Violation("SEC001", Severity.CRITICAL, "Another instance", "#/elsewhere", null);
        AnalysisReport report =
                new AnalysisReport(
                        List.of(new RuleResult(rule, List.of(v1, v2))),
                        scoreOf('C', 75),
                        "petstore.yaml");

        JsonNode root = mapper.readTree(new SarifReportFormatter("0.1.0").format(report));
        JsonNode run = root.get("runs").get(0);

        JsonNode rules = run.get("tool").get("driver").get("rules");
        assertThat(rules).hasSize(1);
        assertThat(rules.get(0).get("id").asText()).isEqualTo("SEC001");
        assertThat(rules.get(0).get("defaultConfiguration").get("level").asText())
                .isEqualTo("error");
        assertThat(rules.get(0).get("properties").get("category").asText()).isEqualTo("SECURITY");

        JsonNode results = run.get("results");
        assertThat(results).hasSize(2);

        JsonNode first = results.get(0);
        assertThat(first.get("ruleId").asText()).isEqualTo("SEC001");
        assertThat(first.get("ruleIndex").asInt()).isZero();
        assertThat(first.get("level").asText()).isEqualTo("error");
        assertThat(first.get("message").get("text").asText())
                .contains("Uses HTTP Basic")
                .contains("Suggestion: Use OAuth2 instead");

        JsonNode location = first.get("locations").get(0);
        assertThat(location.get("physicalLocation").get("artifactLocation").get("uri").asText())
                .isEqualTo("petstore.yaml");
        assertThat(location.get("logicalLocations").get(0).get("fullyQualifiedName").asText())
                .isEqualTo("#/components/securitySchemes/legacy");

        assertThat(results.get(1).get("message").get("text").asText())
                .isEqualTo("Another instance");
    }

    @Test
    void deduplicatesRuleDescriptorsAcrossViolations() throws Exception {
        Rule rule = fakeRule("DES010", Severity.WARNING, Category.DESIGN);
        Violation v1 = new Violation("DES010", Severity.WARNING, "msg1", "#/a", null);
        Violation v2 = new Violation("DES010", Severity.WARNING, "msg2", "#/b", null);
        AnalysisReport report =
                new AnalysisReport(
                        List.of(new RuleResult(rule, List.of(v1, v2))),
                        scoreOf('A', 95),
                        "spec.yaml");

        JsonNode root = mapper.readTree(new SarifReportFormatter().format(report));
        JsonNode rules = root.get("runs").get(0).get("tool").get("driver").get("rules");
        assertThat(rules).hasSize(1);
        assertThat(root.get("runs").get(0).get("results")).hasSize(2);
    }

    @Test
    void omitsRulesWithoutViolations() throws Exception {
        Rule noisy = fakeRule("X001", Severity.WARNING, Category.DESIGN);
        Rule silent = fakeRule("X002", Severity.WARNING, Category.DESIGN);
        Violation v = new Violation("X001", Severity.WARNING, "m", "#/a", null);
        AnalysisReport report =
                new AnalysisReport(
                        List.of(
                                new RuleResult(noisy, List.of(v)),
                                new RuleResult(silent, List.of())),
                        scoreOf('A', 99),
                        "spec.yaml");

        JsonNode root = mapper.readTree(new SarifReportFormatter().format(report));
        JsonNode rules = root.get("runs").get(0).get("tool").get("driver").get("rules");
        assertThat(rules).hasSize(1);
        assertThat(rules.get(0).get("id").asText()).isEqualTo("X001");
    }

    @Test
    void defaultVersionWhenNullOrBlank() throws Exception {
        AnalysisReport report = perfectReport();

        JsonNode rootNull = mapper.readTree(new SarifReportFormatter(null).format(report));
        JsonNode rootBlank = mapper.readTree(new SarifReportFormatter("  ").format(report));

        assertThat(rootNull.get("runs").get(0).get("tool").get("driver").get("version").asText())
                .isEqualTo("dev");
        assertThat(rootBlank.get("runs").get(0).get("tool").get("driver").get("version").asText())
                .isEqualTo("dev");
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
