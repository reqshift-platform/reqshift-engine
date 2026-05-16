package com.reqshift.rules.design;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.reqshift.core.parse.OpenApiLoader;

import io.swagger.v3.oas.models.OpenAPI;

class RuleOperationIdUniqueTest {

    private final RuleOperationIdUnique rule = new RuleOperationIdUnique();

    @Test
    void passesWithUniqueIds() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths:
                  /pets:
                    get:
                      operationId: listPets
                      responses: {'200': {description: ok}}
                    post:
                      operationId: createPet
                      responses: {'201': {description: created}}
                """);
        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void flagsDuplicateOperationId() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths:
                  /pets:
                    get:
                      operationId: listPets
                      responses: {'200': {description: ok}}
                  /animals:
                    get:
                      operationId: listPets
                      responses: {'200': {description: ok}}
                """);
        assertThat(rule.check(api))
                .hasSize(1)
                .first()
                .satisfies(
                        v -> {
                            assertThat(v.ruleId()).isEqualTo("DES006");
                            assertThat(v.message()).contains("listPets");
                        });
    }
}
