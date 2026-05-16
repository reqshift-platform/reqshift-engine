package com.reqshift.rules.conformance;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.reqshift.core.parse.OpenApiLoader;

import io.swagger.v3.oas.models.OpenAPI;

class RuleInfoVersionRequiredTest {

    private final RuleInfoVersionRequired rule = new RuleInfoVersionRequired();

    @Test
    void passesWhenVersionPresent() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.2.3}
                paths: {}
                """);
        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void flagsMissingVersion() {
        OpenAPI api = new OpenAPI();
        api.setInfo(new io.swagger.v3.oas.models.info.Info().title("T"));
        assertThat(rule.check(api)).hasSize(1);
        assertThat(rule.check(api).get(0).ruleId()).isEqualTo("CONF003");
    }
}
