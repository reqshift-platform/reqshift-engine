package com.reqshift.rules.httpcodes;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.reqshift.core.parse.OpenApiLoader;

import io.swagger.v3.oas.models.OpenAPI;

class RuleOperationHasErrorResponseTest {

    private final RuleOperationHasErrorResponse rule = new RuleOperationHasErrorResponse();

    @Test
    void passesWith4xx() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths:
                  /pets:
                    get:
                      responses:
                        '200': {description: ok}
                        '404': {description: not found}
                """);
        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void passesWithDefaultResponse() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths:
                  /pets:
                    get:
                      responses:
                        '200': {description: ok}
                        default: {description: error}
                """);
        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void flagsOperationWithoutErrorResponse() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths:
                  /pets:
                    get:
                      responses:
                        '200': {description: ok}
                """);
        assertThat(rule.check(api)).hasSize(1);
        assertThat(rule.check(api).get(0).ruleId()).isEqualTo("HTTP002");
    }
}
