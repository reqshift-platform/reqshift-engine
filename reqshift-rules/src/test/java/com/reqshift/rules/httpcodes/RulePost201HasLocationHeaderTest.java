package com.reqshift.rules.httpcodes;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.reqshift.core.parse.OpenApiLoader;

import io.swagger.v3.oas.models.OpenAPI;

class RulePost201HasLocationHeaderTest {

    private final RulePost201HasLocationHeader rule = new RulePost201HasLocationHeader();

    @Test
    void passesWithLocationHeader() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths:
                  /pets:
                    post:
                      responses:
                        '201':
                          description: created
                          headers:
                            Location:
                              schema: {type: string}
                """);
        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void flags201WithoutLocationHeader() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths:
                  /pets:
                    post:
                      responses:
                        '201': {description: created}
                """);
        assertThat(rule.check(api)).hasSize(1);
        assertThat(rule.check(api).get(0).ruleId()).isEqualTo("HTTP005");
    }

    @Test
    void ignoresPostWithout201() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths:
                  /pets:
                    post:
                      responses:
                        '200': {description: ok}
                """);
        assertThat(rule.check(api)).isEmpty();
    }
}
