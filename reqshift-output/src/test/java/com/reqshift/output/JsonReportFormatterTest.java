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

class JsonReportFormatterTest {

    private final JsonReportFormatter formatter = new JsonReportFormatter();
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void emptyReportProducesValidJson() throws Exception {
        Map<Category, Integer> perfect = new EnumMap<>(Category.class);
        for (Category c : Category.values()) perfect.put(c, 100);
        AnalysisReport report =
                new AnalysisReport(List.of(), new Score('A', perfect, 100, 100, null), "test.yaml");

        String json = formatter.format(report);
        JsonNode node = mapper.readTree(json);

        assertThat(node.get("source").asText()).isEqualTo("test.yaml");
        assertThat(node.get("score").get("grade").asText()).isEqualTo("A");
        assertThat(node.get("score").get("overall").asInt()).isEqualTo(100);
        assertThat(node.get("results").isArray()).isTrue();
        assertThat(node.get("results")).isEmpty();
    }

    @Test
    void reportWithViolationsSerializesAllFields() throws Exception {
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
                        List.of(result), new Score('A', scores, 99, 99, null), "petstore.yaml");

        String json = formatter.format(report);
        JsonNode node = mapper.readTree(json);

        JsonNode resultNode = node.get("results").get(0);
        assertThat(resultNode.get("rule").get("id").asText()).isEqualTo("TEST001");
        assertThat(resultNode.get("rule").get("severity").asText()).isEqualTo("WARNING");
        assertThat(resultNode.get("rule").get("category").asText()).isEqualTo("DESIGN");

        JsonNode violation = resultNode.get("violations").get(0);
        assertThat(violation.get("ruleId").asText()).isEqualTo("TEST001");
        assertThat(violation.get("severity").asText()).isEqualTo("WARNING");
        assertThat(violation.get("message").asText()).isEqualTo("Something wrong");
        assertThat(violation.get("location").asText()).isEqualTo("#/x");
        assertThat(violation.get("suggestion").asText()).isEqualTo("Do better");
    }

    @Test
    void outputIsIndentedJson() {
        Map<Category, Integer> perfect = new EnumMap<>(Category.class);
        for (Category c : Category.values()) perfect.put(c, 100);
        AnalysisReport report =
                new AnalysisReport(List.of(), new Score('A', perfect, 100, 100, null), "test.yaml");

        String json = formatter.format(report);

        assertThat(json).contains("\n");
        assertThat(json).contains("  ");
    }
}
