package com.reqshift.rules.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;
import com.reqshift.core.parse.OpenApiLoader;

import io.swagger.v3.oas.models.OpenAPI;

class RuleNoBasicHttpAuthTest {

    private final RuleNoBasicHttpAuth rule = new RuleNoBasicHttpAuth();

    @Test
    void flagsHttpBasicScheme() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths: {}
                components:
                  securitySchemes:
                    legacyAuth:
                      type: http
                      scheme: basic
                """);

        List<Violation> violations = rule.check(api);

        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).ruleId()).isEqualTo("SEC001");
        assertThat(violations.get(0).severity()).isEqualTo(Severity.CRITICAL);
        assertThat(violations.get(0).message()).contains("legacyAuth");
    }

    @Test
    void passesWithBearerScheme() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths: {}
                components:
                  securitySchemes:
                    bearerAuth:
                      type: http
                      scheme: bearer
                """);

        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void passesWithNoSecuritySchemes() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths: {}
                """);

        assertThat(rule.check(api)).isEmpty();
    }
}
