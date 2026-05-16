package com.reqshift.rules.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.reqshift.core.model.Violation;
import com.reqshift.core.parse.OpenApiLoader;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;

class RuleOpenIdConnectHasUrlTest {

    private final RuleOpenIdConnectHasUrl rule = new RuleOpenIdConnectHasUrl();

    @Test
    void passesWithDiscoveryUrl() {
        OpenAPI api =
                new OpenApiLoader()
                        .loadFromString(
                                """
                openapi: 3.0.3
                info: {title: T, version: 1.0.0}
                paths: {}
                components:
                  securitySchemes:
                    oidc:
                      type: openIdConnect
                      openIdConnectUrl: https://idp.example.com/.well-known/openid-configuration
                """);
        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void flagsMissingDiscoveryUrl() {
        // swagger-parser typically rejects an openIdConnect scheme without URL at parse time,
        // so we build the model directly.
        OpenAPI api = new OpenAPI();
        SecurityScheme scheme = new SecurityScheme().type(SecurityScheme.Type.OPENIDCONNECT);
        api.setComponents(new Components().addSecuritySchemes("oidc", scheme));

        java.util.List<Violation> violations = rule.check(api);
        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).ruleId()).isEqualTo("SEC012");
    }
}
