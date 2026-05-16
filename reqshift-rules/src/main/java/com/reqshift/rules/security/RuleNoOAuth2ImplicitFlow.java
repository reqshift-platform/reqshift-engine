package com.reqshift.rules.security;

import java.util.ArrayList;
import java.util.List;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;

public final class RuleNoOAuth2ImplicitFlow implements Rule {

    @Override
    public String id() {
        return "SEC004";
    }

    @Override
    public Severity severity() {
        return Severity.ERROR;
    }

    @Override
    public Category category() {
        return Category.SECURITY;
    }

    @Override
    public List<Violation> check(OpenAPI openApi) {
        if (openApi.getComponents() == null
                || openApi.getComponents().getSecuritySchemes() == null) {
            return List.of();
        }
        List<Violation> violations = new ArrayList<>();
        openApi.getComponents()
                .getSecuritySchemes()
                .forEach(
                        (name, scheme) -> {
                            if (scheme.getType() == SecurityScheme.Type.OAUTH2
                                    && scheme.getFlows() != null
                                    && scheme.getFlows().getImplicit() != null) {
                                violations.add(
                                        new Violation(
                                                id(),
                                                severity(),
                                                "Security scheme '"
                                                        + name
                                                        + "' uses OAuth2 implicit flow, deprecated by OAuth 2.1 (token leakage risk)",
                                                "#/components/securitySchemes/"
                                                        + name
                                                        + "/flows/implicit",
                                                "Replace the implicit flow with authorizationCode + PKCE."));
                            }
                        });
        return violations;
    }
}
