package com.reqshift.rules.httpcodes;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.reqshift.core.parse.OpenApiLoader;

import io.swagger.v3.oas.models.OpenAPI;

class RuleNoContentOn204Test {

    private final RuleNoContentOn204 rule = new RuleNoContentOn204();

    @Test
    void passesWith204AndNoContent() {
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
                      responses:
                        '204': {description: gone}
                """);
        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void flags204WithContentBody() {
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
                      responses:
                        '204':
                          description: surprising body
                          content:
                            application/json:
                              schema: {type: object}
                """);
        assertThat(rule.check(api)).hasSize(1);
        assertThat(rule.check(api).get(0).ruleId()).isEqualTo("HTTP004");
    }
}
