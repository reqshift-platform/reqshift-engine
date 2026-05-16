package com.reqshift.rules.documentation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.reqshift.core.parse.OpenApiLoader;

import io.swagger.v3.oas.models.OpenAPI;

class RuleSchemaHasExampleTest {

    private final RuleSchemaHasExample rule = new RuleSchemaHasExample();

    @Test
    void passesWithExample() {
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
                      example: {name: Rex}
                      properties:
                        name: {type: string}
                """);
        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void flagsSchemaWithoutExample() {
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
                      properties:
                        name: {type: string}
                """);
        assertThat(rule.check(api)).hasSize(1);
        assertThat(rule.check(api).get(0).ruleId()).isEqualTo("DOC007");
    }
}
