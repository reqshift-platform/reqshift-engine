package com.reqshift.rules.design;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.reqshift.core.parse.OpenApiLoader;

import io.swagger.v3.oas.models.OpenAPI;

class RulePathParameterDocumentedTest {

    private final RulePathParameterDocumented rule = new RulePathParameterDocumented();

    @Test
    void passesWhenParameterMatchesTemplate() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths:
                  /pets/{petId}:
                    get:
                      parameters:
                        - {name: petId, in: path, required: true, schema: {type: string}}
                      responses: {'200': {description: ok}}
                """);
        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void passesWhenParameterIsAtPathLevel() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths:
                  /pets/{petId}:
                    parameters:
                      - {name: petId, in: path, required: true, schema: {type: string}}
                    get:
                      responses: {'200': {description: ok}}
                """);
        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void flagsTemplateWithoutMatchingParameter() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths:
                  /pets/{petId}:
                    get:
                      responses: {'200': {description: ok}}
                """);
        assertThat(rule.check(api))
                .hasSize(1)
                .first()
                .satisfies(
                        v -> {
                            assertThat(v.ruleId()).isEqualTo("DES009");
                            assertThat(v.message()).contains("petId");
                        });
    }
}
