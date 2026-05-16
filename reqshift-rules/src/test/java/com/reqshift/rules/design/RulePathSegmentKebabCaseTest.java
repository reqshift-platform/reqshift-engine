package com.reqshift.rules.design;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.reqshift.core.parse.OpenApiLoader;

import io.swagger.v3.oas.models.OpenAPI;

class RulePathSegmentKebabCaseTest {

    private final RulePathSegmentKebabCase rule = new RulePathSegmentKebabCase();

    @Test
    void passesKebabCasePaths() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths:
                  /user-profiles:
                    get:
                      responses: {'200': {description: ok}}
                """);
        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void flagsCamelCaseSegment() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths:
                  /userProfiles:
                    get:
                      responses: {'200': {description: ok}}
                """);
        assertThat(rule.check(api))
                .hasSize(1)
                .first()
                .satisfies(v -> assertThat(v.ruleId()).isEqualTo("DES002"));
    }

    @Test
    void flagsSnakeCaseSegment() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths:
                  /user_profiles:
                    get:
                      responses: {'200': {description: ok}}
                """);
        assertThat(rule.check(api)).hasSize(1);
    }
}
