package com.reqshift.rules.schemas;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.reqshift.core.parse.OpenApiLoader;

import io.swagger.v3.oas.models.OpenAPI;

class RuleRequiredPropertiesExistTest {

    private final RuleRequiredPropertiesExist rule = new RuleRequiredPropertiesExist();

    @Test
    void passesWhenRequiredMatchesProperties() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths: {}
                components:
                  schemas:
                    Pet:
                      type: object
                      required: [name]
                      properties:
                        name: {type: string}
                """);
        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void flagsRequiredWithoutMatchingProperty() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths: {}
                components:
                  schemas:
                    Pet:
                      type: object
                      required: [name, age]
                      properties:
                        name: {type: string}
                """);
        assertThat(rule.check(api))
                .hasSize(1)
                .first()
                .satisfies(
                        v -> {
                            assertThat(v.ruleId()).isEqualTo("SCHEMAS002");
                            assertThat(v.message()).contains("age");
                        });
    }
}
