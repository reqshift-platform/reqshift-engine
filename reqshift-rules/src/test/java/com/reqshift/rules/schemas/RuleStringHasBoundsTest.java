package com.reqshift.rules.schemas;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.reqshift.core.parse.OpenApiLoader;

import io.swagger.v3.oas.models.OpenAPI;

class RuleStringHasBoundsTest {

    private final RuleStringHasBounds rule = new RuleStringHasBounds();

    @Test
    void passesWithMaxLength() {
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
                        name: {type: string, maxLength: 50}
                """);
        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void passesWithBoundedFormat() {
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
                        id: {type: string, format: uuid}
                        email: {type: string, format: email}
                """);
        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void passesWithEnum() {
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
                        status: {type: string, enum: [available, sold]}
                """);
        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void flagsUnboundedString() {
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
        assertThat(rule.check(api).get(0).ruleId()).isEqualTo("SCHEMAS004");
    }
}
