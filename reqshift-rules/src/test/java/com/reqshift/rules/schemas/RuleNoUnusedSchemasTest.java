package com.reqshift.rules.schemas;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.reqshift.core.parse.OpenApiLoader;

import io.swagger.v3.oas.models.OpenAPI;

class RuleNoUnusedSchemasTest {

    private final RuleNoUnusedSchemas rule = new RuleNoUnusedSchemas();

    @Test
    void passesWhenSchemaIsReferenced() {
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
                        '200':
                          description: ok
                          content:
                            application/json:
                              schema:
                                $ref: '#/components/schemas/Pet'
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
    void flagsOrphanSchema() {
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
                components:
                  schemas:
                    Orphan:
                      type: object
                      properties:
                        x: {type: string}
                """);
        assertThat(rule.check(api))
                .hasSize(1)
                .first()
                .satisfies(
                        v -> {
                            assertThat(v.ruleId()).isEqualTo("SCHEMAS005");
                            assertThat(v.message()).contains("Orphan");
                        });
    }
}
