package com.reqshift.rules.security;

import java.util.List;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;

public final class RuleSecuritySchemesDefined implements Rule {

    @Override
    public String id() {
        return "SEC002";
    }

    @Override
    public Severity severity() {
        return Severity.WARNING;
    }

    @Override
    public Category category() {
        return Category.SECURITY;
    }

    @Override
    public List<Violation> check(OpenAPI openApi) {
        if (openApi.getPaths() == null || openApi.getPaths().isEmpty()) {
            return List.of();
        }
        var components = openApi.getComponents();
        if (components == null
                || components.getSecuritySchemes() == null
                || components.getSecuritySchemes().isEmpty()) {
            return List.of(
                    new Violation(
                            id(),
                            severity(),
                            "API exposes operations but defines no security schemes. Consumers cannot authenticate.",
                            "#/components/securitySchemes",
                            "Define at least one security scheme such as OAuth2, bearer JWT, or apiKey."));
        }
        return List.of();
    }
}
