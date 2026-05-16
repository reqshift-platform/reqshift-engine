package com.reqshift.rules.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.reqshift.core.model.Violation;
import com.reqshift.core.parse.OpenApiLoader;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityScheme;

class RuleOAuth2FlowHasTokenUrlTest {

    private final RuleOAuth2FlowHasTokenUrl rule = new RuleOAuth2FlowHasTokenUrl();

    @Test
    void passesWithTokenUrl() {
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
                          scopes: {read: ok}
                """);
        assertThat(rule.check(api)).isEmpty();
    }

    @Test
    void flagsMissingTokenUrl() {
        // swagger-parser may reject a flow missing tokenUrl at parse time,
        // so we build the model directly.
        OpenAPI api = new OpenAPI();
        OAuthFlow flow = new OAuthFlow().scopes(new Scopes().addString("read", "ok"));
        SecurityScheme scheme =
                new SecurityScheme()
                        .type(SecurityScheme.Type.OAUTH2)
                        .flows(new OAuthFlows().clientCredentials(flow));
        api.setComponents(new Components().addSecuritySchemes("oauth", scheme));

        java.util.List<Violation> violations = rule.check(api);
        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).ruleId()).isEqualTo("SEC011");
    }
}
