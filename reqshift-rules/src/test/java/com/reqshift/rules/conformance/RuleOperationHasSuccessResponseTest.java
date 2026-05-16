package com.reqshift.rules.conformance;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.reqshift.core.parse.OpenApiLoader;

import io.swagger.v3.oas.models.OpenAPI;

class RuleOperationHasSuccessResponseTest {

    private final RuleOperationHasSuccessResponse rule = new RuleOperationHasSuccessResponse();

    @Test
    void passesWith2xxResponse() {
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
                        default: {description: any}
                """);
        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void flagsOnlyErrorResponses() {
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
                        '404': {description: not found}
                        '500': {description: oops}
                """);
        assertThat(rule.check(api))
                .hasSize(1)
                .first()
                .satisfies(v -> assertThat(v.ruleId()).isEqualTo("CONF009"));
    }
}
