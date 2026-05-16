package com.reqshift.rules.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.reqshift.core.parse.OpenApiLoader;

import io.swagger.v3.oas.models.OpenAPI;

class RuleMutatingOpsHaveSecurityTest {

    private final RuleMutatingOpsHaveSecurity rule = new RuleMutatingOpsHaveSecurity();

    @Test
    void passesWhenGlobalSecuritySet() {
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
                    post:
                      responses: {'201': {description: created}}
                components:
                  securitySchemes:
                    bearerAuth: {type: http, scheme: bearer}
                """);
        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void passesWhenOperationHasSecurity() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths:
                  /pets:
                    post:
                      security:
                        - bearerAuth: []
                      responses: {'201': {description: created}}
                components:
                  securitySchemes:
                    bearerAuth: {type: http, scheme: bearer}
                """);
        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void flagsMutatingOpWithoutSecurity() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths:
                  /pets:
                    post:
                      responses: {'201': {description: created}}
                    delete:
                      responses: {'204': {description: gone}}
                """);
        assertThat(rule.check(api)).hasSize(2);
        assertThat(rule.check(api).get(0).ruleId()).isEqualTo("SEC009");
    }

    @Test
    void ignoresGetOperation() {
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
}
