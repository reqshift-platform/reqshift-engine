package com.reqshift.rules.httpcodes;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.reqshift.core.parse.OpenApiLoader;

import io.swagger.v3.oas.models.OpenAPI;

class RuleNoBodyOnGetOrDeleteTest {

    private final RuleNoBodyOnGetOrDelete rule = new RuleNoBodyOnGetOrDelete();

    @Test
    void passesGetWithoutBody() {
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
                """);
        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void flagsGetWithRequestBody() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths:
                  /pets:
                    get:
                      requestBody:
                        content:
                          application/json:
                            schema: {type: object}
                      responses: {'200': {description: ok}}
                """);
        assertThat(rule.check(api)).hasSize(1);
        assertThat(rule.check(api).get(0).ruleId()).isEqualTo("HTTP001");
    }

    @Test
    void flagsDeleteWithRequestBody() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths:
                  /pets/{id}:
                    delete:
                      parameters:
                        - {name: id, in: path, required: true, schema: {type: string}}
                      requestBody:
                        content:
                          application/json:
                            schema: {type: object}
                      responses: {'204': {description: gone}}
                """);
        assertThat(rule.check(api))
                .hasSize(1)
                .first()
                .satisfies(v -> assertThat(v.message()).contains("DELETE"));
    }
}
