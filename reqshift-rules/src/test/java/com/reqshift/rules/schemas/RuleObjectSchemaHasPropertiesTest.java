package com.reqshift.rules.schemas;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.reqshift.core.parse.OpenApiLoader;

import io.swagger.v3.oas.models.OpenAPI;

class RuleObjectSchemaHasPropertiesTest {

    private final RuleObjectSchemaHasProperties rule = new RuleObjectSchemaHasProperties();

    @Test
    void passesWithProperties() {
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
        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void passesWithAdditionalProperties() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths: {}
                components:
                  schemas:
                    Metadata:
                      type: object
                      additionalProperties:
                        type: string
                """);
        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void flagsEmptyObjectBlob() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths: {}
                components:
                  schemas:
                    Blob:
                      type: object
                """);
        assertThat(rule.check(api)).hasSize(1);
        assertThat(rule.check(api).get(0).ruleId()).isEqualTo("SCHEMAS001");
    }
}
