package com.reqshift.rules.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.reqshift.core.parse.OpenApiLoader;

import io.swagger.v3.oas.models.OpenAPI;

class RuleNoOAuth2ImplicitFlowTest {

    private final RuleNoOAuth2ImplicitFlow rule = new RuleNoOAuth2ImplicitFlow();

    @Test
    void passesWithAuthorizationCodeFlow() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths: {}
                components:
                  securitySchemes:
                    oauth:
                      type: oauth2
                      flows:
                        authorizationCode:
                          authorizationUrl: https://idp.example.com/authorize
                          tokenUrl: https://idp.example.com/token
                          scopes: {read: ok}
                """);
        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void flagsImplicitFlow() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths: {}
                components:
                  securitySchemes:
                    oauth:
                      type: oauth2
                      flows:
                        implicit:
                          authorizationUrl: https://idp.example.com/authorize
                          scopes: {read: ok}
                """);
        assertThat(rule.check(api)).hasSize(1);
        assertThat(rule.check(api).get(0).ruleId()).isEqualTo("SEC004");
    }
}
