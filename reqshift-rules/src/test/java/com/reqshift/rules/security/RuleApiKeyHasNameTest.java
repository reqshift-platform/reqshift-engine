package com.reqshift.rules.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.reqshift.core.model.Violation;
import com.reqshift.core.parse.OpenApiLoader;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;

class RuleApiKeyHasNameTest {

    private final RuleApiKeyHasName rule = new RuleApiKeyHasName();

    @Test
    void passesWithName() {
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
    void flagsMissingName() {
        // swagger-parser rejects an apiKey scheme missing 'name' at parse time,
        // so we build the model directly to exercise the rule.
        OpenAPI api = new OpenAPI();
        SecurityScheme scheme =
                new SecurityScheme().type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER);
        api.setComponents(new Components().addSecuritySchemes("apiKey", scheme));

        java.util.List<Violation> violations = rule.check(api);
        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).ruleId()).isEqualTo("SEC010");
    }
}
