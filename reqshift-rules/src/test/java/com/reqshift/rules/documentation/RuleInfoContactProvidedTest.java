package com.reqshift.rules.documentation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.reqshift.core.parse.OpenApiLoader;

import io.swagger.v3.oas.models.OpenAPI;

class RuleInfoContactProvidedTest {

    private final RuleInfoContactProvided rule = new RuleInfoContactProvided();

    @Test
    void passesWithEmail() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info:
                  title: T
                  version: 1.0.0
                  contact: {email: api@example.com}
                paths: {}
                """);
        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void flagsMissingContact() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths: {}
                """);
        assertThat(rule.check(api)).hasSize(1);
        assertThat(rule.check(api).get(0).ruleId()).isEqualTo("DOC002");
    }
}
