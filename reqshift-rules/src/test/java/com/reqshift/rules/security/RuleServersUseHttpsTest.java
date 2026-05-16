package com.reqshift.rules.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.reqshift.core.parse.OpenApiLoader;

import io.swagger.v3.oas.models.OpenAPI;

class RuleServersUseHttpsTest {

    private final RuleServersUseHttps rule = new RuleServersUseHttps();

    @Test
    void passesWithHttpsServer() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                servers:
                  - url: https://api.example.com
                paths: {}
                """);
        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void passesWithLocalhostHttp() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                servers:
                  - url: http://localhost:8080
                paths: {}
                """);
        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void flagsHttpProductionServer() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                servers:
                  - url: http://api.example.com
                paths: {}
                """);
        assertThat(rule.check(api))
                .hasSize(1)
                .first()
                .satisfies(v -> assertThat(v.ruleId()).isEqualTo("SEC007"));
    }
}
