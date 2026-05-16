package com.reqshift.rules.security;

import java.util.ArrayList;
import java.util.List;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;

public final class RuleApiKeyHasName implements Rule {

    @Override
    public String id() {
        return "SEC010";
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
                            if (scheme.getType() == SecurityScheme.Type.APIKEY
                                    && (scheme.getName() == null || scheme.getName().isBlank())) {
                                violations.add(
                                        new Violation(
                                                id(),
                                                severity(),
                                                "apiKey security scheme '"
                                                        + schemeName
                                                        + "' has no 'name' declared",
                                                "#/components/securitySchemes/"
                                                        + schemeName
                                                        + "/name",
                                                "Declare the header, query, or cookie name carrying the API key, e.g. name: X-API-Key."));
                            }
                        });
        return violations;
    }
}
