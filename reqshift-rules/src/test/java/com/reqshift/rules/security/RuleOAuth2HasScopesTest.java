package com.reqshift.rules.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.reqshift.core.parse.OpenApiLoader;

import io.swagger.v3.oas.models.OpenAPI;

class RuleOAuth2HasScopesTest {

    private final RuleOAuth2HasScopes rule = new RuleOAuth2HasScopes();

    @Test
    void passesWhenFlowDeclaresScopes() {
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
                        clientCredentials:
                          tokenUrl: https://idp.example.com/token
                          scopes: {read:pets: 'Read pets'}
                """);
        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void flagsFlowWithEmptyScopes() {
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
                        clientCredentials:
                          tokenUrl: https://idp.example.com/token
                          scopes: {}
                """);
        assertThat(rule.check(api)).hasSize(1);
        assertThat(rule.check(api).get(0).ruleId()).isEqualTo("SEC006");
    }
}
