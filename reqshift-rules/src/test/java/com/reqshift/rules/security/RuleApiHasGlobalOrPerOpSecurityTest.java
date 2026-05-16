package com.reqshift.rules.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.reqshift.core.parse.OpenApiLoader;

import io.swagger.v3.oas.models.OpenAPI;

class RuleApiHasGlobalOrPerOpSecurityTest {

    private final RuleApiHasGlobalOrPerOpSecurity rule = new RuleApiHasGlobalOrPerOpSecurity();

    @Test
    void passesWithGlobalSecurity() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                security:
                  - bearerAuth: []
                paths:
                  /pets:
                    get:
                      responses: {'200': {description: ok}}
                components:
                  securitySchemes:
                    bearerAuth: {type: http, scheme: bearer}
                """);
        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void passesWithPerOperationSecurity() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths:
                  /pets:
                    get:
                      security:
                        - bearerAuth: []
                      responses: {'200': {description: ok}}
                components:
                  securitySchemes:
                    bearerAuth: {type: http, scheme: bearer}
                """);
        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void flagsApiWithoutAnySecurity() {
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
        assertThat(rule.check(api)).hasSize(1);
        assertThat(rule.check(api).get(0).ruleId()).isEqualTo("SEC003");
    }
}
