package com.reqshift.rules.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.reqshift.core.parse.OpenApiLoader;

import io.swagger.v3.oas.models.OpenAPI;

class RuleNoApiKeyInQueryTest {

    private final RuleNoApiKeyInQuery rule = new RuleNoApiKeyInQuery();

    @Test
    void passesWithApiKeyInHeader() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths: {}
                components:
                  securitySchemes:
                    apiKey:
                      type: apiKey
                      name: X-API-Key
                      in: header
                """);
        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void flagsApiKeyInQuery() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths: {}
                components:
                  securitySchemes:
                    apiKey:
                      type: apiKey
                      name: api_key
                      in: query
                """);
        assertThat(rule.check(api)).hasSize(1);
        assertThat(rule.check(api).get(0).ruleId()).isEqualTo("SEC005");
    }
}
