package com.reqshift.rules.conformance;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.reqshift.core.parse.OpenApiLoader;

import io.swagger.v3.oas.models.OpenAPI;

class RuleServerUrlValidTest {

    private final RuleServerUrlValid rule = new RuleServerUrlValid();

    @Test
    void passesWithHttpsUrl() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                servers:
                  - url: https://api.example.com/v1
                paths: {}
                """);
        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void passesWithTemplatedUrl() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                servers:
                  - url: https://{tenant}.example.com
                    variables:
                      tenant: {default: acme}
                paths: {}
                """);
        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void flagsFtpScheme() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                servers:
                  - url: ftp://api.example.com
                paths: {}
                """);
        assertThat(rule.check(api))
                .hasSize(1)
                .first()
                .satisfies(v -> assertThat(v.ruleId()).isEqualTo("CONF005"));
    }
}
