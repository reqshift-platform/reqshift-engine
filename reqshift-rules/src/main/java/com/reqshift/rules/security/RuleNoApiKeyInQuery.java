package com.reqshift.rules.security;

import java.util.ArrayList;
import java.util.List;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;

public final class RuleNoApiKeyInQuery implements Rule {

    @Override
    public String id() {
        return "SEC005";
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
                            if (scheme.getType() == SecurityScheme.Type.APIKEY
                                    && SecurityScheme.In.QUERY.equals(scheme.getIn())) {
                                violations.add(
                                        new Violation(
                                                id(),
                                                severity(),
                                                "API key '"
                                                        + name
                                                        + "' is passed in the query string, exposing it in access logs and referrer headers",
                                                "#/components/securitySchemes/" + name + "/in",
                                                "Move the apiKey to a header (in: header) or to a cookie (in: cookie)."));
                            }
                        });
        return violations;
    }
}
