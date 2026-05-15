package com.reqshift.rules.security;

import java.util.ArrayList;
import java.util.List;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;

public final class RuleNoBasicHttpAuth implements Rule {

    @Override
    public String id() {
        return "SEC001";
    }

    @Override
    public Severity severity() {
        return Severity.CRITICAL;
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
                            if (scheme.getType() == SecurityScheme.Type.HTTP
                                    && "basic".equalsIgnoreCase(scheme.getScheme())) {
                                violations.add(
                                        new Violation(
                                                id(),
                                                severity(),
                                                "Security scheme '%s' uses HTTP Basic authentication, which transmits credentials in plain text"
                                                        .formatted(name),
                                                "#/components/securitySchemes/" + name,
                                                "Use OAuth2, OpenID Connect, or a bearer token scheme instead"));
                            }
                        });
        return violations;
    }
}
