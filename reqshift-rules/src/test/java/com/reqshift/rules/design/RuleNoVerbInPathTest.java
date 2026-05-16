package com.reqshift.rules.design;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.reqshift.core.parse.OpenApiLoader;

import io.swagger.v3.oas.models.OpenAPI;

class RuleNoVerbInPathTest {

    private final RuleNoVerbInPath rule = new RuleNoVerbInPath();

    @Test
    void passesWithNounPath() {
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
    void flagsGetVerb() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths:
                  /getPets:
                    get:
                      responses: {'200': {description: ok}}
                """);
        assertThat(rule.check(api))
                .hasSize(1)
                .first()
                .satisfies(v -> assertThat(v.ruleId()).isEqualTo("DES005"));
    }

    @Test
    void flagsDeleteVerbInKebabCase() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths:
                  /delete-user:
                    post:
                      responses: {'200': {description: ok}}
                """);
        assertThat(rule.check(api)).hasSize(1);
    }
}
