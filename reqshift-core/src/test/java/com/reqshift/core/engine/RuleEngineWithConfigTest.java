package com.reqshift.core.engine;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.reqshift.core.config.RuleConfig;
import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.RuleResult;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;

class RuleEngineWithConfigTest {

    @Test
    void filtersOutDisabledRules() {
        OpenAPI api = new OpenAPI();
        Rule active = new FakeRule("KEEP001", Severity.ERROR);
        Rule disabled = new FakeRule("DROP001", Severity.ERROR);

        RuleConfig config = new RuleConfig(Set.of("DROP001"), Map.of());
        List<RuleResult> results = RuleEngine.fromRules(List.of(active, disabled), config).run(api);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).rule().id()).isEqualTo("KEEP001");
    }

    @Test
    void overridesSeverityOnRuleAndViolations() {
        OpenAPI api = new OpenAPI();
        Rule rule = new FakeRule("DES010", Severity.WARNING);

        RuleConfig config = new RuleConfig(Set.of(), Map.of("DES010", Severity.INFO));
        List<RuleResult> results = RuleEngine.fromRules(List.of(rule), config).run(api);

        assertThat(results).hasSize(1);
        RuleResult result = results.get(0);
        assertThat(result.rule().severity()).isEqualTo(Severity.INFO);
        assertThat(result.violations()).hasSize(1);
        assertThat(result.violations().get(0).severity()).isEqualTo(Severity.INFO);
    }

    @Test
    void keepsRuleAsIsWhenNoOverrideApplies() {
        OpenAPI api = new OpenAPI();
        Rule rule = new FakeRule("DES010", Severity.WARNING);

        RuleConfig config = new RuleConfig(Set.of(), Map.of("OTHER001", Severity.INFO));
        List<RuleResult> results = RuleEngine.fromRules(List.of(rule), config).run(api);

        assertThat(results.get(0).rule().severity()).isEqualTo(Severity.WARNING);
        assertThat(results.get(0).violations().get(0).severity()).isEqualTo(Severity.WARNING);
    }

    @Test
    void emptyConfigBehavesLikePlainConstructor() {
        OpenAPI api = new OpenAPI();
        Rule rule = new FakeRule("DES010", Severity.WARNING);

        List<RuleResult> withEmpty =
                RuleEngine.fromRules(List.of(rule), RuleConfig.empty()).run(api);
        List<RuleResult> plain = new RuleEngine(List.of(rule)).run(api);

        assertThat(withEmpty.get(0).rule().severity()).isEqualTo(plain.get(0).rule().severity());
        assertThat(withEmpty.get(0).violations()).hasSameSizeAs(plain.get(0).violations());
    }

    private static final class FakeRule implements Rule {
        private final String id;
        private final Severity severity;

        FakeRule(String id, Severity severity) {
            this.id = id;
            this.severity = severity;
        }

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
            return Category.DESIGN;
        }

        @Override
        public List<Violation> check(OpenAPI openApi) {
            return List.of(new Violation(id, severity, "msg", "/loc", "fix"));
        }
    }
}
