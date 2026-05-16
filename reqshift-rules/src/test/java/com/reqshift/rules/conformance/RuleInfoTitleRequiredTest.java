package com.reqshift.rules.conformance;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.reqshift.core.parse.OpenApiLoader;

import io.swagger.v3.oas.models.OpenAPI;

class RuleInfoTitleRequiredTest {

    private final RuleInfoTitleRequired rule = new RuleInfoTitleRequired();

    @Test
    void passesWhenTitlePresent() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: My API, version: 1.0.0}
                paths: {}
                """);
        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void flagsEmptyTitle() {
        OpenAPI api = new OpenAPI();
        api.setInfo(new io.swagger.v3.oas.models.info.Info().title(" ").version("1.0.0"));
        assertThat(rule.check(api)).hasSize(1);
        assertThat(rule.check(api).get(0).ruleId()).isEqualTo("CONF002");
    }
}
