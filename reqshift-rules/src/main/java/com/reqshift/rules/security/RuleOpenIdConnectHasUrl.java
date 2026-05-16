package com.reqshift.rules.security;

import java.util.ArrayList;
import java.util.List;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;

public final class RuleOpenIdConnectHasUrl implements Rule {

    @Override
    public String id() {
        return "SEC012";
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
                        (schemeName, scheme) -> {
                            if (scheme.getType() != SecurityScheme.Type.OPENIDCONNECT) {
                                return;
                            }
                            String url = scheme.getOpenIdConnectUrl();
                            if (url == null || url.isBlank()) {
                                violations.add(
                                        new Violation(
                                                id(),
                                                severity(),
                                                "OpenIdConnect security scheme '"
                                                        + schemeName
                                                        + "' has no openIdConnectUrl",
                                                "#/components/securitySchemes/"
                                                        + schemeName
                                                        + "/openIdConnectUrl",
                                                "Declare the OpenID Connect discovery URL (the .well-known/openid-configuration endpoint)."));
                            }
                        });
        return violations;
    }
}
