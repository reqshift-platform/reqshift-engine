package com.reqshift.rules.conformance;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.reqshift.core.parse.OpenApiLoader;

import io.swagger.v3.oas.models.OpenAPI;

class RulePathsNotEmptyTest {

    private final RulePathsNotEmpty rule = new RulePathsNotEmpty();

    @Test
    void passesWhenAtLeastOnePathDefined() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths:
                  /pets:
                    get:
                      responses: {'200': {description: ok}}
                """);
        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void flagsEmptyPaths() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths: {}
                """);
        assertThat(rule.check(api)).hasSize(1);
        assertThat(rule.check(api).get(0).ruleId()).isEqualTo("CONF006");
    }
}
