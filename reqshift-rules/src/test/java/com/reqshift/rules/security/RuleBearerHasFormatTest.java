package com.reqshift.rules.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.reqshift.core.parse.OpenApiLoader;

import io.swagger.v3.oas.models.OpenAPI;

class RuleBearerHasFormatTest {

    private final RuleBearerHasFormat rule = new RuleBearerHasFormat();

    @Test
    void passesWithBearerFormatDeclared() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths: {}
                components:
                  securitySchemes:
                    bearer:
                      type: http
                      scheme: bearer
                      bearerFormat: JWT
                """);
        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void flagsBearerWithoutFormat() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths: {}
                components:
                  securitySchemes:
                    bearer:
                      type: http
                      scheme: bearer
                """);
        assertThat(rule.check(api)).hasSize(1);
        assertThat(rule.check(api).get(0).ruleId()).isEqualTo("SEC008");
    }
}
